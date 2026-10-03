import { test } from 'node:test';
import assert from 'node:assert/strict';
import { CATALOG, LARGE_CREDIT_CHANGE } from '@lego/shared';
import { grantRole, idem, login, start, stripeSignature, call } from './helpers.ts';

const epic = CATALOG.find((i) => i.kind === 'cosmetic' && i.rarity === 'epic')!;
const freeItem = CATALOG.find((i) => i.kind === 'cosmetic' && i.unlock === 'default')!;

test('admin credit changes: permissions, confirmation, idempotency, audit', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const player = await login(h, 'Player1');
  const dev = await login(h, 'DevOne');
  const admin = await login(h, 'AdminOne');
  grantRole(h, dev.id, 'developer');
  grantRole(h, admin.id, 'admin');

  // Normal players can not use the dashboard API.
  assert.equal((await player.call('POST', '/api/admin/credits', { userId: player.id, delta: 100, reason: 'self service', idempotencyKey: idem() })).status, 403);

  const key = idem();
  const g1 = await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: 500, reason: 'Event winner', idempotencyKey: key });
  assert.equal(g1.status, 200);
  assert.equal(g1.body.balance, 500);
  // Same key again (double click) books nothing new.
  const g2 = await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: 500, reason: 'Event winner', idempotencyKey: key });
  assert.equal(g2.body.duplicate, true);
  assert.equal(g2.body.balance, 500);
  // Reusing a key with different data is refused.
  assert.equal((await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: 501, reason: 'Event winner', idempotencyKey: key })).status, 409);

  // Reason is mandatory.
  assert.equal((await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: 5, reason: '', idempotencyKey: idem() })).status, 400);
  // Large changes need confirmation.
  const big = await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: LARGE_CREDIT_CHANGE, reason: 'Big prize', idempotencyKey: idem() });
  assert.equal(big.status, 409);
  assert.equal(big.body.error, 'confirmation_required');
  assert.equal((await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: LARGE_CREDIT_CHANGE, reason: 'Big prize', idempotencyKey: idem(), confirm: true })).status, 200);

  // Developers may grant but not deduct; admins may deduct but not below zero.
  assert.equal((await dev.call('POST', '/api/admin/credits', { userId: player.id, delta: -100, reason: 'Chargeback', idempotencyKey: idem() })).status, 403);
  assert.equal((await admin.call('POST', '/api/admin/credits', { userId: player.id, delta: -100, reason: 'Chargeback', idempotencyKey: idem() })).status, 200);
  const over = await admin.call('POST', '/api/admin/credits', { userId: player.id, delta: -1_000_000, reason: 'Too much', idempotencyKey: idem(), confirm: true });
  assert.equal(over.status, 409);
  assert.equal(over.body.error, 'insufficient_credits');

  const me = await player.call('GET', '/api/me/credits');
  assert.equal(me.body.balance, 500 + LARGE_CREDIT_CHANGE - 100);
  assert.equal(me.body.history.length, 3);

  // Both developers and admins hold audit.view in the default role table.
  assert.equal((await dev.call('GET', `/api/admin/audit?target=${player.id}`)).status, 200);
  const audit = await admin.call('GET', `/api/admin/audit?target=${player.id}`);
  assert.equal(audit.body.entries.length, 3);
  assert.deepEqual(audit.body.entries.map((e: any) => e.action), ['credits.deduct', 'credits.grant', 'credits.grant']);
});

test('shop: buy, ownership, equip, gift, lookup for other clients', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Buyer');
  const b = await login(h, 'Friend');
  const admin = await login(h, 'Admin');
  grantRole(h, admin.id, 'admin');

  // Free items can be equipped without buying.
  assert.equal((await a.call('PUT', '/api/equip', { slot: freeItem.slot, itemId: freeItem.id })).status, 200);
  // Paid items can not.
  assert.equal((await a.call('PUT', '/api/equip', { slot: epic.slot, itemId: epic.id })).status, 403);
  // Wrong slot is rejected.
  assert.equal((await a.call('PUT', '/api/equip', { slot: 'nametag', itemId: epic.id })).status, 400);

  const poor = await a.call('POST', '/api/shop/buy', { itemId: epic.id, idempotencyKey: idem() });
  assert.equal(poor.status, 409);
  assert.equal(poor.body.error, 'insufficient_credits');

  await admin.call('POST', '/api/admin/credits', { userId: a.id, delta: 4000, reason: 'Test funds', idempotencyKey: idem() });
  const key = idem();
  const buy = await a.call('POST', '/api/shop/buy', { itemId: epic.id, idempotencyKey: key });
  assert.equal(buy.status, 200);
  assert.equal(buy.body.balance, 4000 - epic.price!);
  assert.equal((await a.call('POST', '/api/shop/buy', { itemId: epic.id, idempotencyKey: idem() })).body.error, 'already_owned');
  assert.equal((await a.call('PUT', '/api/equip', { slot: epic.slot, itemId: epic.id })).status, 200);

  // Free items are not sold.
  assert.equal((await a.call('POST', '/api/shop/buy', { itemId: freeItem.id, idempotencyKey: idem() })).status, 403);

  // Gifts only to friends.
  assert.equal((await a.call('POST', '/api/shop/gift', { itemId: 'tag_rainbow', to: b.id, idempotencyKey: idem() })).status, 403);
  await a.call('POST', '/api/friends/requests', { player: 'Friend' });
  await b.call('POST', `/api/friends/requests/${a.id}/accept`);
  const gift = await a.call('POST', '/api/shop/gift', { itemId: 'tag_rainbow', to: b.id, idempotencyKey: idem() });
  assert.equal(gift.status, 200);
  assert.equal((await b.call('PUT', '/api/equip', { slot: 'nametag', itemId: 'tag_rainbow' })).status, 200);

  // Other clients can look up equipped cosmetics by uuid (no login needed).
  const look = await call(h, 'POST', '/api/public/cosmetics/lookup', { uuids: [a.uuid, b.uuid.replace(/-/g, ''), 'not-a-uuid'] });
  assert.equal(look.status, 200);
  assert.equal(look.body.players[a.uuid].equipped[epic.slot], epic.id);
  assert.equal(look.body.players[b.uuid].equipped.nametag, 'tag_rainbow');

  const inv = await a.call('GET', '/api/inventory');
  assert.ok(inv.body.owned.includes(epic.id));
  assert.ok(inv.body.collection.total >= 300);
});

test('staff name tags follow roles, custom tag text needs LEGO+ and moderation', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const p = await login(h, 'Pleb');
  const s = await login(h, 'Staffer');
  grantRole(h, s.id, 'moderator');
  assert.equal((await p.call('PUT', '/api/equip', { slot: 'nametag', itemId: 'tag_staff' })).status, 403);
  assert.equal((await s.call('PUT', '/api/equip', { slot: 'nametag', itemId: 'tag_staff' })).status, 200);
  assert.equal((await s.call('PUT', '/api/equip', { slot: 'nametag', itemId: 'tag_developer' })).status, 403);

  assert.equal((await p.call('PUT', '/api/me/tag-text', { text: 'Cool' })).status, 403);
  h.app.db.run("INSERT INTO subscriptions(user_id, tier, expires_at, updated_at) VALUES (:u, 'lego_plus', :e, :t)", { u: p.id, e: Date.now() + 86_400_000, t: Date.now() });
  assert.equal((await p.call('PUT', '/api/me/tag-text', { text: 'Cool Builder' })).status, 200);
  assert.equal((await p.call('PUT', '/api/me/tag-text', { text: '4dm1n' })).status, 400);
  assert.equal((await p.call('PUT', '/api/me/tag-text', { text: 'Staffer' })).status, 400, 'may not impersonate another player');
});

test('daily rewards, quests with capped metrics and battle pass', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const c = await login(h, 'Grinder');

  const d1 = await c.call('POST', '/api/daily/claim');
  assert.equal(d1.status, 200);
  assert.equal(d1.body.streak, 1);
  assert.equal((await c.call('POST', '/api/daily/claim')).status, 409);

  assert.equal((await c.call('POST', '/api/quests/d_launch/claim')).body.error, 'quest_incomplete');
  await c.call('POST', '/api/me/metrics', { metric: 'launches', amount: 1 });
  const q = await c.call('POST', '/api/quests/d_launch/claim');
  assert.equal(q.status, 200);
  assert.equal((await c.call('POST', '/api/quests/d_launch/claim')).status, 409);

  // Client-reported metrics are capped per day.
  const r1 = await c.call('POST', '/api/me/metrics', { metric: 'launches', amount: 50 });
  assert.equal(r1.body.applied, 19);
  assert.equal((await c.call('POST', '/api/me/metrics', { metric: 'launches', amount: 5 })).body.applied, 0);
  // Play time and friends are server-measured only.
  assert.equal((await c.call('POST', '/api/me/metrics', { metric: 'playtime_minutes', amount: 50 })).status, 400);

  const prog = await c.call('GET', '/api/progress');
  assert.equal(prog.status, 200);
  if (prog.body.pass) {
    assert.ok(prog.body.pass.xp > 0);
    // Premium track requires a pass.
    h.app.db.run('UPDATE pass_progress SET xp = 5000 WHERE user_id = :u', { u: c.id });
    assert.equal((await c.call('POST', '/api/pass/claim', { tier: 5, track: 'premium' })).body.error, 'premium_required');
    assert.equal((await c.call('POST', '/api/pass/claim', { tier: 2, track: 'free' })).status, 200);
    assert.equal((await c.call('POST', '/api/pass/claim', { tier: 2, track: 'free' })).status, 409);
    assert.equal((await c.call('POST', '/api/pass/claim', { tier: 10, track: 'free' })).body.error, 'tier_locked');
  }
});

test('promo codes are single use per player and limited overall', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const admin = await login(h, 'PromoAdmin');
  grantRole(h, admin.id, 'developer');
  const a = await login(h, 'UserA');
  const b = await login(h, 'UserB');
  assert.equal((await admin.call('POST', '/api/admin/promo', { code: 'launch-2026', credits: 250, maxUses: 1 })).status, 200);
  const ra = await a.call('POST', '/api/promo/redeem', { code: 'LAUNCH-2026' });
  assert.equal(ra.body.balance, 250);
  assert.equal((await a.call('POST', '/api/promo/redeem', { code: 'launch-2026' })).status, 409);
  assert.equal((await b.call('POST', '/api/promo/redeem', { code: 'launch-2026' })).body.error, 'code_exhausted');
});

test('stripe webhook: signature required, fulfilment exactly once', async (t) => {
  const secret = 'whsec_test';
  const h = await start({ STRIPE_SECRET_KEY: 'sk_test_x', STRIPE_WEBHOOK_SECRET: secret });
  t.after(() => h.close());
  const c = await login(h, 'Payer');
  const event = {
    id: 'evt_1', type: 'checkout.session.completed',
    data: { object: { payment_status: 'paid', amount_total: 999, currency: 'eur', metadata: { user_id: c.id, product_id: 'credits_1100' } } },
  };
  const body = JSON.stringify(event);
  const post = (sig: string) => fetch(`${h.base}/api/payments/webhook`, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Stripe-Signature': sig }, body });

  assert.equal((await post('t=1,v1=deadbeef')).status, 400);
  assert.equal((await post(stripeSignature(body, 'wrong'))).status, 400);
  assert.equal((await post(stripeSignature(body, secret, Math.floor(Date.now() / 1000) - 3600))).status, 400, 'old timestamps are rejected');
  const ok = await post(stripeSignature(body, secret));
  assert.equal(ok.status, 200);
  assert.equal(((await ok.json()) as { granted: boolean }).granted, true);
  const dup = await post(stripeSignature(body, secret));
  assert.equal(((await dup.json()) as { granted: boolean }).granted, false);
  assert.equal((await c.call('GET', '/api/me')).body.credits, 1100);

  // Subscription grants tier + period credits.
  const sub = JSON.stringify({ id: 'evt_2', type: 'checkout.session.completed', data: { object: { payment_status: 'paid', amount_total: 399, currency: 'eur', metadata: { user_id: c.id, product_id: 'lego_plus' } } } });
  await fetch(`${h.base}/api/payments/webhook`, { method: 'POST', headers: { 'Content-Type': 'application/json', 'Stripe-Signature': stripeSignature(sub, secret) }, body: sub });
  const me = await c.call('GET', '/api/me');
  assert.equal(me.body.subscription.tier, 'lego_plus');
  assert.equal(me.body.credits, 1500);
});

test('payments are disabled without configuration', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const c = await login(h, 'NoPay');
  assert.equal((await c.call('POST', '/api/payments/checkout', { productId: 'credits_500' })).status, 503);
});

test('LEGO Coins for playing: per game, wins, daily cap, play time', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const c = await login(h, 'Gamer');
  const r1 = await c.call('POST', '/api/me/game-results', { gameId: 'tetris', score: 1200, won: false });
  assert.equal(r1.body.coins, 2);
  const r2 = await c.call('POST', '/api/me/game-results', { gameId: 'tetris', score: 5000, won: true });
  assert.equal(r2.body.coins, 5);
  assert.equal((await c.call('GET', '/api/me')).body.credits, 7);
  // Daily cap of 100 coins from games.
  let total = 7;
  for (let i = 0; i < 30; i++) total += (await c.call('POST', '/api/me/game-results', { gameId: 'snake', score: 10, won: true })).body.coins ?? 0;
  assert.equal(total, 100);

  // Play time measured by the server: simulate 31 minutes of in-game presence.
  const { awardPlaytimeCoins, addMetric } = await import('../src/services/progress.ts');
  addMetric(h.app.db, c.id, 'playtime_minutes', 31, 1440);
  assert.equal(awardPlaytimeCoins(h.app.db, c.id), 10, 'two full 15-minute blocks');
  assert.equal(awardPlaytimeCoins(h.app.db, c.id), 0, 'never paid twice');
});
