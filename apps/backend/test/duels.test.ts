import { test } from 'node:test';
import assert from 'node:assert/strict';
import { call, login, start } from './helpers.ts';
import { applyMove, rpsResult } from '../src/routes/duels.ts';

test('rules: connect4 win, tictactoe illegal move, nim, reversi flips', () => {
  let b = Array(42).fill(0);
  for (const c of [0, 1, 0, 1, 0, 1]) { const r = applyMove('connect4', b, 0, c, c === 0 ? 1 : 2); assert.ok(typeof r !== 'string'); b = r.board; }
  const win = applyMove('connect4', b, 0, 0, 1);
  assert.ok(typeof win !== 'string' && win.winner === 1);
  const t = applyMove('tictactoe', [1, 0, 0, 0, 0, 0, 0, 0, 0], 0, 0, 2);
  assert.equal(t, 'illegal');
  const n = applyMove('nim', [0, 0, 2], 0, 2 * 8 + 1, 1);
  assert.ok(typeof n !== 'string' && n.winner === 1);
  const rv = Array(64).fill(0); rv[27] = 2; rv[28] = 1; rv[35] = 1; rv[36] = 2;
  const m = applyMove('reversi', rv, 0, 19, 1);
  assert.ok(typeof m !== 'string' && m.board[27] === 1);
  assert.equal(applyMove('reversi', rv, 0, 0, 1), 'illegal');
  assert.equal(rpsResult(1, 0), 1);
  assert.equal(rpsResult(0, 1), -1);
  assert.equal(rpsResult(2, 2), 0);
});

test('duel flow: privacy, invite, turns, win, inbox, blocks', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Alice');
  const b = await login(h, 'Bob');
  assert.equal((await a.call('POST', '/api/duels', { to: 'Alice', game: 'tictactoe' })).status, 400);
  assert.equal((await a.call('POST', '/api/duels', { to: 'Bob', game: 'chess' })).status, 400);
  // Bob only allows friends.
  await b.call('PUT', '/api/me/privacy', { profile: 'everyone', status: 'friends', activity: 'friends', messages: 'friends', friendRequests: true, duels: 'friends' });
  assert.equal((await a.call('POST', '/api/duels', { to: 'Bob', game: 'tictactoe' })).status, 403);
  await b.call('PUT', '/api/me/privacy', { profile: 'everyone', status: 'friends', activity: 'friends', messages: 'friends', friendRequests: true, duels: 'everyone' });
  const inv = await a.call('POST', '/api/duels', { to: 'bob', game: 'tictactoe' });
  assert.equal(inv.status, 200);
  const id = inv.body.duel.id;
  const box = await b.call('GET', '/api/duels/inbox');
  assert.equal(box.body.messages[0].k, 'inv');
  assert.equal(box.body.messages[0].from.name, 'Alice');
  assert.equal((await a.call('POST', `/api/duels/${id}/move`, { value: 0 })).status, 409); // not accepted yet
  assert.equal((await b.call('POST', `/api/duels/${id}/accept`)).status, 200);
  assert.equal((await b.call('POST', `/api/duels/${id}/move`, { value: 4 })).status, 409); // Alice starts
  for (const [who, v] of [[a, 0], [b, 3], [a, 1], [b, 4]] as const) assert.equal((await who.call('POST', `/api/duels/${id}/move`, { value: v })).status, 200);
  assert.equal((await b.call('POST', `/api/duels/${id}/move`, { value: 5 })).status, 409);
  assert.equal((await a.call('POST', `/api/duels/${id}/move`, { value: 1 })).status, 400); // taken
  const fin = await a.call('POST', `/api/duels/${id}/move`, { value: 2 });
  assert.equal(fin.body.duel.status, 'over');
  assert.equal(fin.body.duel.winner, a.id);
  // A stranger cannot see the duel.
  const c = await login(h, 'Carl');
  assert.equal((await c.call('GET', `/api/duels/${id}`)).status, 404);
  // Blocked players cannot invite.
  await b.call('POST', '/api/blocks', { player: 'Carl' });
  assert.equal((await c.call('POST', '/api/duels', { to: 'Bob', game: 'rps' })).status, 403);
  // Emotes show up in the public lookup.
  await a.call('POST', '/api/emotes/play', { emote: 'wave' });
  const lk = await call(h, 'POST', '/api/public/cosmetics/lookup', { uuids: [a.uuid] });
  assert.equal(Object.values(lk.body.players as Record<string, { emote: { id: string } | null }>)[0]!.emote?.id, 'wave');
});

test('rock paper scissors: best of five, choices revealed together', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const a = await login(h, 'Rita');
  const b = await login(h, 'Sam');
  const id = (await a.call('POST', '/api/duels', { to: 'Sam', game: 'rps' })).body.duel.id;
  await b.call('POST', `/api/duels/${id}/accept`);
  for (let i = 0; i < 3; i++) {
    const first = await a.call('POST', `/api/duels/${id}/move`, { value: 1 });
    assert.equal(first.body.waiting, true);
    await b.call('POST', `/api/duels/${id}/move`, { value: 0 });
  }
  const d = await a.call('GET', `/api/duels/${id}`);
  assert.equal(d.body.duel.status, 'over');
  assert.equal(d.body.duel.winner, a.id);
});
