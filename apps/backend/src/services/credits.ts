// LEGO Credits ledger. Every change writes a ledger row and updates the
// balance in one transaction; the CHECK (credits >= 0) constraint and the
// unique idempotency key make double spending and double booking impossible.

import type { Db } from '../db.ts';
import { conflict, HttpError } from '../http.ts';

export type CreditKind =
  | 'grant' | 'deduct' | 'purchase' | 'daily' | 'quest' | 'battlepass' | 'promo' | 'payment' | 'subscription' | 'refund';

export interface CreditChange {
  userId: string;
  delta: number;
  kind: CreditKind;
  reason: string;
  ref?: string | null;
  actorId?: string | null;
  /** Same key twice => the second call is a no-op returning the first result. */
  idemKey?: string | null;
}

export interface LedgerRow {
  id: number;
  user_id: string;
  delta: number;
  balance_after: number;
  kind: string;
  reason: string;
  ref: string | null;
  actor_id: string | null;
  created_at: number;
}

export function balance(db: Db, userId: string): number {
  const r = db.get<{ credits: number }>('SELECT credits FROM users WHERE id = :id', { id: userId });
  if (!r) throw new HttpError(404, 'user_not_found');
  return r.credits;
}

/** Applies a credit change atomically. Throws 409 insufficient_credits. */
export function applyCredits(db: Db, c: CreditChange): { balance: number; ledgerId: number; duplicate: boolean } {
  if (!Number.isSafeInteger(c.delta) || c.delta === 0) throw new HttpError(400, 'invalid_delta');
  return db.tx(() => {
    if (c.idemKey) {
      const prev = db.get<LedgerRow>('SELECT * FROM credit_ledger WHERE idem_key = :k', { k: c.idemKey });
      if (prev) {
        if (prev.user_id !== c.userId || prev.delta !== c.delta) throw conflict('idempotency_mismatch', 'Idempotency key reused with different data');
        return { balance: balance(db, c.userId), ledgerId: prev.id, duplicate: true };
      }
    }
    const current = balance(db, c.userId);
    const next = current + c.delta;
    if (next < 0) throw conflict('insufficient_credits', `Not enough credits (have ${current}, need ${-c.delta})`);
    db.run('UPDATE users SET credits = :n WHERE id = :id', { n: next, id: c.userId });
    const r = db.run(
      `INSERT INTO credit_ledger(user_id, delta, balance_after, kind, reason, ref, actor_id, idem_key, created_at)
       VALUES (:u, :d, :b, :k, :r, :ref, :a, :i, :t)`,
      { u: c.userId, d: c.delta, b: next, k: c.kind, r: c.reason, ref: c.ref ?? null, a: c.actorId ?? null, i: c.idemKey ?? null, t: Date.now() },
    );
    return { balance: next, ledgerId: r.lastInsertRowid, duplicate: false };
  });
}

export function history(db: Db, userId: string, limit = 50, before?: number): LedgerRow[] {
  return db.all<LedgerRow>(
    `SELECT id, user_id, delta, balance_after, kind, reason, ref, actor_id, created_at FROM credit_ledger
     WHERE user_id = :u AND (:b IS NULL OR id < :b) ORDER BY id DESC LIMIT :l`,
    { u: userId, b: before ?? null, l: Math.min(200, Math.max(1, limit)) },
  );
}

/**
 * Simple abuse signal for the dashboard: users whose earned (non-admin,
 * non-payment) credits in the last 24h exceed `threshold`.
 */
export function suspiciousEarners(db: Db, threshold = 3000): { user_id: string; earned: number }[] {
  return db.all(
    `SELECT user_id, SUM(delta) AS earned FROM credit_ledger
     WHERE delta > 0 AND kind IN ('daily','quest','battlepass','promo') AND created_at > :since
     GROUP BY user_id HAVING earned > :th ORDER BY earned DESC LIMIT 100`,
    { since: Date.now() - 86_400_000, th: threshold },
  );
}
