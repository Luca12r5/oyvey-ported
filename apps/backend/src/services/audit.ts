import type { Db } from '../db.ts';

export function audit(db: Db, actorId: string | null, action: string, targetId: string | null, details: Record<string, unknown>, ip: string | null): void {
  db.run(
    'INSERT INTO audit_log(actor_id, action, target_id, details, ip, created_at) VALUES (:a, :ac, :t, :d, :ip, :ts)',
    { a: actorId, ac: action, t: targetId, d: JSON.stringify(details), ip, ts: Date.now() },
  );
}
