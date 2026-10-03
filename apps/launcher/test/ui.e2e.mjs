// Electron UI smoke test: runs the built launcher against a real backend
// (fake Mojang session server), clicks through every page and theme options,
// and fails on renderer errors. Needs a display (use xvfb-run on Linux).
//
//   node build.mjs && xvfb-run -a node test/ui.e2e.mjs
//   E2E_SCREENSHOTS=dir to keep screenshots.

import { createServer } from 'node:http';
import { mkdtempSync, mkdirSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { randomUUID } from 'node:crypto';
import { _electron as electron } from 'playwright-core';

const { createApp } = await import('../../backend/src/app.ts');
const { loadConfig } = await import('../../backend/src/config.ts');
const listen = (srv) => new Promise((r) => srv.listen(0, '127.0.0.1', () => r(srv.address().port)));

const joined = new Map();
const session = createServer((req, res) => {
  const url = new URL(req.url, 'http://x');
  const p = joined.get(url.searchParams.get('serverId'));
  if (p && p.name === url.searchParams.get('username')) { res.setHeader('Content-Type', 'application/json'); res.end(JSON.stringify(p)); }
  else { res.statusCode = 204; res.end(); }
});
const sessionPort = await listen(session);
const dataDir = mkdtempSync(join(tmpdir(), 'lego-ui-'));
const srv = createServer();
const port = await listen(srv);
const base = `http://127.0.0.1:${port}`;
const app = createApp(loadConfig({ LEGO_DATA_DIR: dataDir, LEGO_SECURE_COOKIES: '0', LEGO_PUBLIC_ORIGIN: base, LEGO_SESSION_SERVER: `http://127.0.0.1:${sessionPort}` }));
srv.on('request', (req, res) => void app.handle(req, res));

async function call(token, method, path, body) {
  const r = await fetch(base + path, { method, headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(body ? { 'Content-Type': 'application/json' } : {}) }, body: body ? JSON.stringify(body) : undefined });
  const t = await r.text();
  if (!r.ok) throw new Error(`${method} ${path} ${r.status} ${t}`);
  return t ? JSON.parse(t) : null;
}
async function login(name) {
  const uuid = randomUUID().replace(/-/g, '');
  const ch = await call(null, 'POST', '/api/auth/challenge');
  joined.set(ch.serverId, { id: uuid, name });
  const v = await call(null, 'POST', '/api/auth/verify', { challengeId: ch.challengeId, username: name });
  return { ...v, uuid };
}
const me = await login('LegoDev');
const friend = await login('Alex');
app.db.run("INSERT INTO roles(user_id, role, granted_at) VALUES (:u, 'developer', 0)", { u: me.user.id });
await call(me.token, 'POST', '/api/admin/credits', { userId: me.user.id, delta: 3000, reason: 'UI test funds', idempotencyKey: randomUUID() });
await call(me.token, 'POST', '/api/friends/requests', { player: 'Alex' });
await call(friend.token, 'POST', `/api/friends/requests/${me.user.id}/accept`);
await call(friend.token, 'POST', `/api/messages/${me.user.id}`, { body: 'Hi aus dem UI-Test' });
await call(me.token, 'POST', '/api/admin/news', { kind: 'news', title: 'Launcher-Test', body: 'News im Launcher' });

const userData = mkdtempSync(join(tmpdir(), 'lego-launcher-ud-'));
mkdirSync(userData, { recursive: true });
writeFileSync(join(userData, 'settings.json'), JSON.stringify({ backendUrl: base, themeId: 'nightfall' }));
const account = {
  msRefreshToken: 'test-refresh',
  mc: { accessToken: 'test-mc-token', expiresAt: Date.now() + 3_600_000, uuid: me.uuid, name: 'LegoDev', xuid: null, skinUrl: null },
  lego: { token: me.token, expiresAt: Date.now() + 86_400_000, userId: me.user.id, roles: [] },
};

const shots = process.env.E2E_SCREENSHOTS;
if (shots) mkdirSync(shots, { recursive: true });
const eapp = await electron.launch({
  args: ['--no-sandbox', '.'],
  cwd: new URL('..', import.meta.url).pathname,
  env: { ...process.env, LEGO_USER_DATA: userData, LEGO_E2E: '1', LEGO_E2E_ACCOUNT: JSON.stringify(account), ELECTRON_DISABLE_SECURITY_WARNINGS: '1' },
});
const win = await eapp.firstWindow();
const problems = [];
win.on('pageerror', (e) => problems.push(`pageerror: ${e.message}`));
win.on('console', (m) => { if (m.type() === 'error') problems.push(`console: ${m.text()}`); });
await win.setViewportSize?.({ width: 1280, height: 800 });
await win.waitForSelector('.nav-item');
// Wait until the stored LEGO session was validated (roles arrive -> Dev tab appears).
await win.waitForSelector('[data-page=dev]', { timeout: 10_000 });

async function page(id, expect) {
  await win.click(`[data-page=${id}]`);
  await win.waitForTimeout(600);
  const text = await win.locator('#view').innerText();
  if (expect && !text.includes(expect)) problems.push(`${id}: expected "${expect}", got: ${text.slice(0, 160)}`);
  if (/Fehler: /.test(text)) problems.push(`${id}: error shown: ${text.slice(0, 200)}`);
  if (shots) await win.screenshot({ path: join(shots, `launcher-${id}.png`) });
}

await page('play', 'LAUNCH');
await page('profiles', 'LEGO Client');
await page('cosmetics', 'Sammlung');
await page('shop', 'Shop');
await page('friends', 'Alex');
await page('rewards', 'Tägliche Belohnung');
await page('news', 'Launcher-Test');
await page('feedback', 'Neuer Beitrag');
await page('account', 'LegoDev');
await page('dev', 'Entwickler-Dashboard');
await page('settings', 'Designs');

// Theme switching really changes the CSS variables.
const before = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--accent'));
await win.click('[data-theme=matrix]');
await win.waitForTimeout(300);
const after = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--accent'));
const bodyClass = await win.evaluate(() => document.body.className);
if (before === after) problems.push('theme switch did not change --accent');
if (!bodyClass.includes('font-mono') || !bodyClass.includes('bg-scanlines')) problems.push(`matrix theme classes missing: ${bodyClass}`);
if (shots) await win.screenshot({ path: join(shots, 'launcher-theme-matrix.png') });

// Buy something through the launcher shop.
await win.click('[data-theme=nightfall]');
await win.click('[data-page=shop]');
await win.waitForSelector('[data-buy]');
await win.locator('[data-buy]').first().click();
await win.click('dialog button[value=ok]');
await win.waitForSelector('.toast.ok');
const inv = await call(me.token, 'GET', '/api/inventory');
if (inv.owned.filter((id) => !id.startsWith('tag_')).length === 0 && inv.owned.length < 30) problems.push('purchase not reflected in inventory');

// Chat from the launcher.
await win.click('[data-page=friends]');
await win.click('[data-chat]');
await win.waitForSelector('#send');
await win.fill('#send input', 'Antwort aus dem Launcher');
await win.click('#send button');
await win.waitForTimeout(400);
const thread = await call(friend.token, 'GET', `/api/messages/${me.user.id}`);
if (!thread.messages.some((m) => m.body === 'Antwort aus dem Launcher')) problems.push('chat message not delivered');
if (shots) await win.screenshot({ path: join(shots, 'launcher-chat.png') });

await eapp.close();
srv.closeAllConnections(); srv.close(); session.close(); app.events.close(); app.db.close();
rmSync(dataDir, { recursive: true, force: true });
rmSync(userData, { recursive: true, force: true });
if (problems.length) {
  console.error(`LAUNCHER UI E2E FAILED (${problems.length}):\n${problems.join('\n')}`);
  process.exit(1);
}
console.log('LAUNCHER UI E2E OK: all pages rendered, theme switch, shop purchase and chat worked.');
