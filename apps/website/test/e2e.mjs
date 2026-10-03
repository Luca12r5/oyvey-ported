// End-to-end check of the website against a real backend instance.
// Starts a fake Mojang session server + the backend in-process, seeds data
// through the public API, then drives Chromium through every page and fails
// on JS errors, CSP violations or failed API calls.
//
//   npm run e2e            (CHROMIUM_PATH overrides the browser binary)
//   E2E_SCREENSHOTS=dir    saves a screenshot per page

import { createServer } from 'node:http';
import { mkdtempSync, mkdirSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { randomUUID } from 'node:crypto';
import { chromium } from 'playwright-core';

const { createApp } = await import('../../backend/src/app.ts');
const { loadConfig } = await import('../../backend/src/config.ts');

const listen = (srv) => new Promise((r) => srv.listen(0, '127.0.0.1', () => r(srv.address().port)));

// --- fake session server ---------------------------------------------------
const joined = new Map();
const session = createServer((req, res) => {
  const url = new URL(req.url, 'http://x');
  const p = joined.get(url.searchParams.get('serverId'));
  if (url.pathname === '/session/minecraft/hasJoined' && p && p.name === url.searchParams.get('username')) {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(p));
  } else {
    res.statusCode = 204;
    res.end();
  }
});
const sessionPort = await listen(session);

// --- backend ---------------------------------------------------------------
const dataDir = mkdtempSync(join(tmpdir(), 'lego-e2e-'));
const backendServer = createServer();
const port = await listen(backendServer);
const base = `http://127.0.0.1:${port}`;
const app = createApp(loadConfig({
  LEGO_DATA_DIR: dataDir, LEGO_SECURE_COOKIES: '0', LEGO_PUBLIC_ORIGIN: base, LEGO_SESSION_SERVER: `http://127.0.0.1:${sessionPort}`,
}));
backendServer.on('request', (req, res) => void app.handle(req, res));

async function call(token, method, path, body) {
  const res = await fetch(base + path, {
    method, headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(body ? { 'Content-Type': 'application/json' } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`${method} ${path} -> ${res.status} ${text}`);
  return text ? JSON.parse(text) : null;
}

async function login(name) {
  const ch = await call(null, 'POST', '/api/auth/challenge');
  joined.set(ch.serverId, { id: randomUUID().replace(/-/g, ''), name });
  const v = await call(null, 'POST', '/api/auth/verify', { challengeId: ch.challengeId, username: name });
  return { token: v.token, id: v.user.id, name };
}

// --- seed --------------------------------------------------------------------
const dev = await login('LegoDev');
const alex = await login('Alex');
app.db.run("INSERT INTO roles(user_id, role, granted_at) VALUES (:u, 'admin', 0)", { u: dev.id });
await call(dev.token, 'POST', '/api/admin/credits', { userId: dev.id, delta: 2500, reason: 'E2E seed funds', idempotencyKey: randomUUID() });
await call(dev.token, 'POST', '/api/friends/requests', { player: 'Alex' });
await call(alex.token, 'POST', `/api/friends/requests/${dev.id}/accept`);
await call(alex.token, 'POST', `/api/messages/${dev.id}`, { body: 'Hallo vom E2E-Test!' });
await call(alex.token, 'POST', '/api/feedback', { kind: 'minigame', title: 'Minigolf bitte', body: 'Ein Minigolf-Modus wäre super.' });
await call(alex.token, 'POST', '/api/reports', { player: 'LegoDev', reason: 'spam', details: 'nur ein Test' });
await call(dev.token, 'POST', '/api/admin/news', { kind: 'news', title: 'Willkommen', body: 'Der LEGO Launcher ist da.' });
await call(alex.token, 'POST', '/api/me/game-results', { gameId: 'tennis', score: 6, won: true });
const { code } = await call(dev.token, 'POST', '/api/auth/web-code');

// --- browser -----------------------------------------------------------------
const shots = process.env.E2E_SCREENSHOTS;
if (shots) mkdirSync(shots, { recursive: true });
const browser = await chromium.launch({ executablePath: process.env.CHROMIUM_PATH ?? '/opt/pw-browsers/chromium' });
const page = await browser.newPage({ viewport: { width: 1360, height: 900 } });
const problems = [];
page.on('pageerror', (e) => problems.push(`pageerror: ${e.message}`));
page.on('console', (m) => { if (m.type() === 'error') problems.push(`console: ${m.text()}`); });
page.on('response', (r) => { if (r.url().includes('/api/') && r.status() >= 400) problems.push(`api ${r.status()} ${r.url()}`); });

async function visit(path, expectText) {
  await page.goto(base + path, { waitUntil: 'networkidle' });
  await page.waitForFunction(() => !document.querySelector('#app .muted')?.textContent?.startsWith('Lädt'), null, { timeout: 5000 });
  const text = await page.locator('#app').innerText();
  if (expectText && !text.includes(expectText)) problems.push(`${path}: expected "${expectText}"`);
  const errorCard = await page.locator('#app > div > .card > h2', { hasText: /^Fehler$/ }).count();
  if (errorCard) problems.push(`${path}: error card shown: ${text.slice(0, 200)}`);
  if (shots) await page.screenshot({ path: join(shots, `${path.replace(/[/?=]/g, '_') || 'home'}.png`), fullPage: false });
}

await visit('/', 'Dein Minecraft');
await visit('/features', 'Features');
await visit('/cosmetics', 'Sammlung');
await visit('/download', 'Download');
await visit('/news', 'Willkommen');
await visit('/leaderboards', 'Ranglisten');
await visit('/status', 'API');
await visit('/legal/privacy', 'Datenschutz');

// Login with the launcher code.
await visit('/login', 'Anmelden');
await page.fill('#code', code);
await page.click('#app button.primary');
await page.waitForURL('**/account');
await page.waitForSelector('text=Datenschutz');

await visit('/account', 'LegoDev');
await visit('/shop', 'Shop');
await visit('/battle-pass', 'Season 1');
await visit('/rewards', 'Tägliche Belohnung');
await visit('/friends', 'Alex');
await visit(`/messages/${alex.id}`, 'Hallo vom E2E-Test!');
await visit('/feedback', 'Minigolf bitte');
await visit('/feedback/new', 'Neuer Beitrag');
await visit('/u/Alex', 'Alex');
await visit('/stats', 'Statistiken');
await visit('/dev', 'Entwickler-Dashboard');
for (const tab of ['users', 'feedback', 'reports', 'news', 'promo', 'audit', 'system']) await visit(`/dev?tab=${tab}`);

// Claim the daily reward through the UI.
await visit('/rewards');
await page.click('#daily');
await page.waitForSelector('text=Heute abgeholt');

// Buy an item through the shop UI (confirm dialog).
await visit('/shop');
await page.locator('[data-buy]').first().click();
await page.click('dialog button[value=ok]');
await page.waitForSelector('.toast.ok');

// Grant credits to Alex through the dashboard, including the typed confirmation for large amounts.
await visit(`/dev/users/${alex.id}`, 'Credits');
await page.fill('#credits input[name=delta]', '6000');
await page.fill('#credits input[name=reason]', 'Turniersieger E2E');
await page.click('#credits button');
await page.fill('dialog input[name=typed]', '6000');
await page.click('dialog button[value=ok]');
await page.waitForSelector('text=Gebucht');
const alexMe = await call(alex.token, 'GET', '/api/me');
if (alexMe.credits !== 6000) problems.push(`expected Alex to have 6000 credits, has ${alexMe.credits}`);
const auditLog = await call(dev.token, 'GET', `/api/admin/audit?target=${alex.id}`);
if (!auditLog.entries.some((e) => e.action === 'credits.grant' && e.details.reason === 'Turniersieger E2E')) problems.push('audit entry missing');

// Mobile layout smoke check.
await page.setViewportSize({ width: 390, height: 844 });
await visit('/', 'Dein Minecraft');
await page.click('#menuToggle');
if (!(await page.locator('#nav.open').isVisible())) problems.push('mobile menu does not open');
if (shots) await page.screenshot({ path: join(shots, 'mobile.png') });

await browser.close();
backendServer.closeAllConnections();
backendServer.close();
session.close();
app.events.close();
app.db.close();
rmSync(dataDir, { recursive: true, force: true });

// The login page's first visit happens while logged out; one 401 from /api/auth/session is expected.
const real = problems.filter((p) => !p.includes('/api/auth/session') && !/status of 401/.test(p));
if (real.length) {
  console.error(`E2E FAILED (${real.length}):\n${real.join('\n')}`);
  process.exit(1);
}
console.log('E2E OK: all pages rendered without JS/CSP/API errors; daily claim, purchase and admin credit grant worked.');
