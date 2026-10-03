// Daily rewards, quests, battle pass and promo codes.

import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { conflict, notFound } from '../http.ts';
import { int, obj, oneOf, str } from '../security.ts';
import { applyCredits } from '../services/credits.ts';
import { claimDaily, claimPassTier, claimQuest, dailyStatus, grantItem, passStatus, questStatus } from '../services/progress.ts';

export function registerProgression(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/progress', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    return { daily: dailyStatus(db, u.id), quests: questStatus(db, u.id), pass: passStatus(db, u.id) };
  });

  r.post('/api/daily/claim', (ctx: AppCtx) => claimDaily(db, requireUser(ctx).id), { rate: ['claim', 20] });

  r.post('/api/quests/:id/claim', (ctx: AppCtx) => claimQuest(db, requireUser(ctx).id, ctx.params.id!), { rate: ['claim', 30] });

  r.post('/api/pass/claim', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    return claimPassTier(db, u.id, int(b, 'tier', 1, 200), oneOf(b, 'track', ['free', 'premium'] as const));
  }, { rate: ['claim', 60] });

  r.post('/api/promo/redeem', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const code = str(obj(ctx.body), 'code', 3, 32).toUpperCase();
    return db.tx(() => {
      const p = db.get<{ code: string; credits: number; item_id: string | null; max_uses: number; uses: number; expires_at: number | null }>(
        'SELECT * FROM promo_codes WHERE code = :c', { c: code });
      if (!p || (p.expires_at && p.expires_at < Date.now())) throw notFound('Invalid or expired code');
      if (p.uses >= p.max_uses) throw conflict('code_exhausted', 'This code has been fully redeemed');
      try {
        db.run('INSERT INTO promo_redemptions(code, user_id, created_at) VALUES (:c, :u, :t)', { c: code, u: u.id, t: Date.now() });
      } catch {
        throw conflict('already_redeemed', 'You already redeemed this code');
      }
      db.run('UPDATE promo_codes SET uses = uses + 1 WHERE code = :c', { c: code });
      const c = applyCredits(db, { userId: u.id, delta: p.credits, kind: 'promo', reason: `Promo code ${code}`, ref: code, idemKey: `promo:${code}:${u.id}` });
      if (p.item_id) grantItem(db, u.id, p.item_id, 'event');
      return { credits: p.credits, item: p.item_id, balance: c.balance };
    });
  }, { rate: ['promo', 10] });
}
