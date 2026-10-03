import { test } from 'node:test';
import assert from 'node:assert/strict';
import { call, login, start } from './helpers.ts';

test('launcher login via session handshake', async (t) => {
  const h = await start();
  t.after(() => h.close());

  const c = await login(h, 'Steve');
  const me = await c.call('GET', '/api/me');
  assert.equal(me.status, 200);
  assert.equal(me.body.name, 'Steve');
  assert.equal(me.body.credits, 0);

  // Same Minecraft account logs in again -> same LEGO user, name updated.
  const again = await login(h, 'Steve2', c.uuid.replace(/-/g, ''));
  assert.equal(again.id, c.id);
  assert.equal((await again.call('GET', '/api/me')).body.name, 'Steve2');
});

test('verify rejects missing join, reused and unknown challenges', async (t) => {
  const h = await start();
  t.after(() => h.close());

  const ch = await call(h, 'POST', '/api/auth/challenge');
  // No join happened -> session server answers 204.
  const r1 = await call(h, 'POST', '/api/auth/verify', { challengeId: ch.body.challengeId, username: 'Alex' });
  assert.equal(r1.status, 401);
  // The challenge was consumed even though verification failed.
  h.joined.set(ch.body.serverId, { id: '0'.repeat(32), name: 'Alex' });
  const r2 = await call(h, 'POST', '/api/auth/verify', { challengeId: ch.body.challengeId, username: 'Alex' });
  assert.equal(r2.status, 401);
  const r3 = await call(h, 'POST', '/api/auth/verify', { challengeId: 'c_nope', username: 'Alex' });
  assert.equal(r3.status, 401);
  // Invalid names are rejected before any network call.
  const r4 = await call(h, 'POST', '/api/auth/verify', { challengeId: 'x', username: 'bad name!' });
  assert.equal(r4.status, 400);
});

test('protected routes need a valid token', async (t) => {
  const h = await start();
  t.after(() => h.close());
  assert.equal((await call(h, 'GET', '/api/me')).status, 401);
  assert.equal((await call(h, 'GET', '/api/me', undefined, { Authorization: 'Bearer forged' })).status, 401);
  const c = await login(h, 'Notch');
  await c.call('POST', '/api/auth/logout');
  assert.equal((await c.call('GET', '/api/me')).status, 401);
});

test('website login with one-time code, cookie session and CSRF', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const c = await login(h, 'Herobrine');

  const code = await c.call('POST', '/api/auth/web-code');
  assert.equal(code.status, 200);
  assert.match(code.body.code, /^[A-Z2-9]{8}$/);

  const web = await call(h, 'POST', '/api/auth/web-login', { code: code.body.code });
  assert.equal(web.status, 200);
  const setCookie = web.headers.get('set-cookie') ?? '';
  assert.match(setCookie, /lego_session=.+; Path=\/; HttpOnly; SameSite=Strict/);
  const cookie = setCookie.split(';')[0]!;
  const csrf = web.body.csrf as string;

  // Codes are single use.
  assert.equal((await call(h, 'POST', '/api/auth/web-login', { code: code.body.code })).status, 401);
  // A web session can not mint further codes.
  assert.equal((await call(h, 'POST', '/api/auth/web-code', {}, { Cookie: cookie, 'X-CSRF-Token': csrf })).status, 403);

  // GET works with the cookie alone.
  assert.equal((await call(h, 'GET', '/api/me', undefined, { Cookie: cookie })).status, 200);
  // State changes need the CSRF header...
  assert.equal((await call(h, 'PATCH', '/api/me', { bio: 'hi' }, { Cookie: cookie })).status, 403);
  assert.equal((await call(h, 'PATCH', '/api/me', { bio: 'hi' }, { Cookie: cookie, 'X-CSRF-Token': 'wrong' })).status, 403);
  // ...and are refused cross-origin even with the token.
  assert.equal((await call(h, 'PATCH', '/api/me', { bio: 'hi' }, { Cookie: cookie, 'X-CSRF-Token': csrf, Origin: 'https://evil.test' })).status, 403);
  assert.equal((await call(h, 'PATCH', '/api/me', { bio: 'hi' }, { Cookie: cookie, 'X-CSRF-Token': csrf, Origin: 'http://lego.test' })).status, 200);
});

test('request hygiene: content type, body size, rate limits, headers', async (t) => {
  const h = await start();
  t.after(() => h.close());
  const c = await login(h, 'Tester');

  const form = await fetch(`${h.base}/api/me`, { method: 'PATCH', headers: { Authorization: `Bearer ${c.token}`, 'Content-Type': 'application/x-www-form-urlencoded' }, body: 'bio=x' });
  assert.equal(form.status, 415);

  const big = await c.call('PATCH', '/api/me', { bio: 'x'.repeat(70_000) });
  assert.equal(big.status, 413);

  const res = await call(h, 'GET', '/api/public/status');
  assert.equal(res.headers.get('x-frame-options'), 'DENY');
  assert.match(res.headers.get('content-security-policy') ?? '', /default-src 'self'/);

  let limited = 0;
  for (let i = 0; i < 35; i++) {
    const r = await call(h, 'POST', '/api/auth/challenge');
    if (r.status === 429) limited++;
  }
  assert.ok(limited > 0, 'challenge endpoint should be rate limited');
});
