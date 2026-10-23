import { test } from 'node:test';
import assert from 'node:assert/strict';
import { request } from 'node:http';
import { FORCE_PER_HOUR, SessionBridge, type BridgeSession } from '../src/core/sessionbridge.ts';

const UUID_A = '11111111111111111111111111111111';
const UUID_B = '22222222222222222222222222222222';

function call(port: number, method: string, path: string, key: string | null, body?: unknown, host = `127.0.0.1:${port}`): Promise<{ status: number; json: Record<string, unknown> }> {
  return new Promise((resolve, reject) => {
    const data = body === undefined ? '' : JSON.stringify(body);
    const req = request({ host: '127.0.0.1', port, method, path, headers: { Host: host, 'Content-Type': 'application/json', ...(key ? { 'X-Lego-Key': key } : {}) } }, (res) => {
      let s = '';
      res.setEncoding('utf8');
      res.on('data', (c: string) => { s += c; });
      res.on('end', () => resolve({ status: res.statusCode ?? 0, json: s ? JSON.parse(s) as Record<string, unknown> : {} }));
    });
    req.on('error', reject);
    req.end(data);
  });
}

function bridge(now: { t: number }) {
  const asked: { uuid: string; force: boolean }[] = [];
  const switches: { key: string; uuid: string; server: string | null }[] = [];
  const b = new SessionBridge({
    session: async (uuid, force): Promise<BridgeSession> => { asked.push({ uuid, force }); return { name: uuid === UUID_A ? 'Alex' : 'Bea', uuid, accessToken: `tok-${uuid}-${asked.length}`, xuid: null, expiresAt: now.t + 86_400_000 }; },
    accounts: () => [{ uuid: UUID_A, name: 'Alex' }, { uuid: UUID_B, name: 'Bea' }],
    requestSwitch: (key, uuid, server) => { switches.push({ key, uuid, server }); },
  }, () => now.t);
  return { b, asked, switches };
}

test('session bridge: only a launch key gets its own account session', async () => {
  const now = { t: 1_000_000 };
  const { b, asked } = bridge(now);
  const port = await b.start();
  try {
    const key = b.register(UUID_A);
    assert.equal(b.env(key).LEGO_SESSION_KEY, key);
    assert.equal(b.env(key).LEGO_SESSION_PORT, String(port));

    assert.equal((await call(port, 'POST', '/session', null)).status, 401);
    assert.equal((await call(port, 'POST', '/session', 'f'.repeat(64))).status, 401);
    // Wrong Host header (a web page reaching localhost through DNS rebinding).
    assert.equal((await call(port, 'POST', '/session', key, {}, `evil.example:${port}`)).status, 403);

    const ok = await call(port, 'POST', '/session', key, {});
    assert.equal(ok.status, 200);
    assert.equal(ok.json.uuid, UUID_A);
    assert.deepEqual(asked, [{ uuid: UUID_A, force: false }]);

    // Plain requests are rate limited, forced ones have their own budget.
    assert.equal((await call(port, 'POST', '/session', key, {})).status, 429);
    for (let i = 0; i < FORCE_PER_HOUR; i++) assert.equal((await call(port, 'POST', '/session', key, { force: true })).status, 200);
    assert.equal((await call(port, 'POST', '/session', key, { force: true })).status, 429);
    now.t += 3_600_001;
    assert.equal((await call(port, 'POST', '/session', key, { force: true })).status, 200);

    // Account list carries no tokens.
    const acc = await call(port, 'GET', '/accounts', key);
    assert.equal(acc.status, 200);
    assert.equal(acc.json.current, UUID_A);
    assert.ok(!JSON.stringify(acc.json).includes('tok-'));

    b.revoke(key);
    assert.equal((await call(port, 'POST', '/session', key, { force: true })).status, 401);
  } finally {
    await b.stop();
  }
});

test('session bridge: switch requests name a known account and a sane server', async () => {
  const { b, switches } = bridge({ t: 0 });
  const port = await b.start();
  try {
    const key = b.register(UUID_A);
    assert.equal((await call(port, 'POST', '/switch', key, { uuid: 'nobody' })).status, 404);
    assert.equal((await call(port, 'POST', '/switch', key, { uuid: UUID_B, server: 'play.example.net:25565' })).status, 200);
    assert.equal((await call(port, 'POST', '/switch', key, { uuid: UUID_B, server: 'bad host; rm -rf' })).status, 200);
    assert.deepEqual(switches, [{ key, uuid: UUID_B, server: 'play.example.net:25565' }, { key, uuid: UUID_B, server: null }]);
  } finally {
    await b.stop();
  }
});
