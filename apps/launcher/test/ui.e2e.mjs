// Electron UI smoke test: runs the built launcher against a real backend
// (fake Mojang session server), clicks through every page and theme options,
// and fails on renderer errors. Needs a display (use xvfb-run on Linux).
//
//   node build.mjs && xvfb-run -a node test/ui.e2e.mjs
//   E2E_SCREENSHOTS=dir to keep screenshots.

import { createServer } from 'node:http';
import { mkdtempSync, mkdirSync, writeFileSync, rmSync, readFileSync } from 'node:fs';
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
// Wear a free hat, wings and knife so the play screen shows real 3D cosmetics.
const { CATALOG } = await import('@lego/shared');
for (const slot of ['hat', 'wings', 'knife']) {
  const item = CATALOG.find((c) => c.slot === slot && c.unlock === 'default');
  if (item) await call(me.token, 'PUT', '/api/equip', { slot, itemId: item.id });
}

const userData = mkdtempSync(join(tmpdir(), 'lego-launcher-ud-'));
mkdirSync(userData, { recursive: true });
// Settings as an older launcher version wrote them (blue theme with grid background).
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
await win.waitForSelector('#sidebar [data-page=play]');
// Wait until the stored LEGO session was validated (roles arrive -> Dev page appears in "Mehr").
await win.waitForSelector('body[data-staff="1"]', { timeout: 10_000 });

async function page(id, expect) {
  if (id === 'designs') {
    // Designs live under Optionen.
    await win.click('#sidebar [data-page=settings]');
    await win.waitForSelector('[data-tab=designs]');
    await win.click('[data-tab=designs]');
  } else {
    // Secondary pages live in the dock's "Mehr" menu.
    if (!(await win.locator(`#sidebar [data-page=${id}]`).count())) await win.click('#tbMore');
    await win.click(`[data-page=${id}]`);
  }
  await win.waitForTimeout(600);
  const text = await win.locator('#view').innerText();
  if (expect && !text.toLowerCase().includes(expect.toLowerCase())) problems.push(`${id}: expected "${expect}", got: ${text.slice(0, 160)}`);
  if (/Fehler: /.test(text)) problems.push(`${id}: error shown: ${text.slice(0, 200)}`);
  if (shots) await win.screenshot({ path: join(shots, `launcher-${id}.png`) });
}

await win.waitForSelector('#launch');
await win.waitForTimeout(1500); // character + background settle
await page('play', 'SPIELEN');
// Old settings are migrated to the night-blue look (NoRisk colours).
const bgColor = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--bg').trim());
if (bgColor !== '#070e19') problems.push(`old settings were not migrated to Nacht: --bg is ${bgColor}`);
await win.waitForSelector('#avatar[data-cos]', { timeout: 8000 }).catch(() => problems.push('equipped cosmetics not shown in 3D on the play screen'));
await win.waitForTimeout(800);
if (shots) await win.screenshot({ path: join(shots, 'launcher-play-default.png') });
// Emotes: the panel lists 50+ emotes; each one starts on the figure without errors.
{
  if (!(await win.locator('#emoteOpen').count())) problems.push('emote button missing');
  await win.click('#emoteOpen');
  await win.waitForSelector('#emotePanel');
  const ids = await win.$$eval('#emotePanel .lx-emote-tile', (els) => els.map((e) => e.getAttribute('data-emote')));
  if (ids.length < 50) problems.push(`only ${ids.length} emotes`);
  for (const id of ids) {
    await win.click(`#emotePanel .lx-emote-tile[data-emote="${id}"]`);
    const on = await win.waitForFunction((x) => document.querySelector('#avatar')?.getAttribute('data-emote') === x, id, { timeout: 3000 }).then(() => true, () => false);
    if (!on) { problems.push(`emote ${id} did not start`); continue; }
    await win.waitForTimeout(['car', 'chair', 'rocket'].includes(id) ? 2200 : 350);
    if (shots && ['car', 'chair', 'rocket', 'explode'].includes(id)) await win.screenshot({ path: join(shots, `launcher-emote-${id}.png`) });
  }
  await win.fill('#emoteSearch', 'auto');
  if ((await win.locator('#emotePanel .lx-emote-tile').count()) !== 1) problems.push('emote search does not filter');
  await win.keyboard.press('Escape');
  await win.keyboard.press('Escape');
  const stopped = await win.waitForFunction(() => !document.querySelector('#avatar')?.getAttribute('data-emote'), null, { timeout: 4000 }).then(() => true, () => false);
  if (!stopped) problems.push('Esc did not stop the emote');
  if (await win.locator('#emotePanel').count()) problems.push('Esc did not close the emote panel');
}
for (const id of ['play', 'profiles', 'mods', 'skins', 'capes']) if (!(await win.locator(`#sidebar [data-page=${id}]`).count())) problems.push(`${id} tab missing`);
if (await win.locator('#sidebar [data-page=designs]').count()) problems.push('designs should live under Optionen');
// Custom window controls: minimize, fullscreen, close (maximize = double-click on the header).
for (const w of ['minimize', 'fullscreen', 'close']) if (!(await win.locator(`[data-win=${w}]`).count())) problems.push(`window button ${w} missing`);
if (await win.locator('[data-win=maximize]').count()) problems.push('maximize button should be gone');
// The window layout must fill the window in every theme.
async function layoutOk(label) {
  const r = await win.evaluate(() => ({ shell: document.querySelector('.nr-launcher').getBoundingClientRect().width, bar: document.querySelector('.nr-titlebar').getBoundingClientRect().width, w: innerWidth }));
  if (r.shell < r.w - 2 || r.bar < r.w - 120) problems.push(`${label}: layout collapsed (shell ${r.shell}px, titlebar ${r.bar}px, window ${r.w}px)`);
}
await layoutOk('nocturne');
// Profile dropdown lists the profiles.
await win.click('#drop');
await win.waitForSelector('.menu [data-profile]');
await win.waitForTimeout(300);
if (shots) await win.screenshot({ path: join(shots, 'launcher-profile-menu.png') });
await win.keyboard.press('Escape');
await win.mouse.click(5, 300);
// Account switcher.
await win.click('#acctBtn');
await win.waitForSelector('.menu [data-act=add]');
await win.waitForTimeout(300);
if (shots) await win.screenshot({ path: join(shots, 'launcher-account-menu.png') });
await win.mouse.click(5, 300);
await page('profiles', 'LEGO Client');
await win.click('#new');
await win.waitForSelector('#loaders [data-loader=fabric]');
await win.waitForTimeout(400);
if (!(await win.locator('#f input[name=vr]').count())) problems.push('new profile has no VR (Vivecraft) switch');
if (shots) await win.screenshot({ path: join(shots, 'launcher-profile-editor.png') });
// A profile's content button opens its content view: tabs for everything installed.
await page('profiles', 'LEGO Client');
await win.locator('.nr-prof-row [data-action=mods]').first().click();
await win.waitForSelector('#profileContent #pcTabs', { timeout: 5000 }).catch(() => problems.push('profile content view did not open'));
for (const k of ['resourcepacks', 'shaderpacks', 'datapacks', 'saves', 'screenshots', 'mods']) {
  await win.locator(`#pcTabs [data-tab=${k}]`).click().catch(() => problems.push(`content tab ${k} missing`));
  await win.waitForTimeout(250);
}
if (shots) await win.screenshot({ path: join(shots, 'launcher-profile-mods.png') });
await win.click('#pcBack').catch(() => problems.push('content view has no back button'));
await page('mods', 'Modpacks');
await page('skins', 'Add New Skin');
// Photo → skin (offline designer here, no AI key): 3D dialog, adjust, save into the library.
const skinCards = await win.locator('.nr-skin-card').count();
const [chooser] = await Promise.all([win.waitForEvent('filechooser'), win.click('#photoSkin')]);
await chooser.setFiles(new URL('../src/renderer/assets/minifig.png', import.meta.url).pathname);
await win.waitForSelector('#skinDialog #skinSpec', { timeout: 10000 }).catch(() => problems.push('photo skin dialog did not open'));
await win.locator('#skinSpec select').first().selectOption('long').catch(() => {});
await win.waitForTimeout(1200);
if (shots) await win.screenshot({ path: join(shots, 'launcher-skin-photo.png') });
await win.click('#skinSave');
await win.waitForSelector('#skinDialog', { state: 'detached', timeout: 8000 }).catch(() => problems.push('skin dialog did not close after saving'));
await win.waitForTimeout(1500);
if ((await win.locator('.nr-skin-card').count()) <= skinCards) problems.push('saved photo skin is not in the library');
if (!(await win.locator('.nr-skin-anim').count())) problems.push('library skins are not animated');
if (shots) await win.screenshot({ path: join(shots, 'launcher-skins.png') });
await page('capes', 'No Cape');
// Cape editor: symbols change the cape, the offline designer builds one from a description.
await win.click('#editor');
await win.waitForSelector('#capeEditor');
const capeBefore = await win.getAttribute('#capeFront', 'src');
await win.click('[data-sym=star]');
await win.waitForTimeout(250);
if (capeBefore === (await win.getAttribute('#capeFront', 'src'))) problems.push('adding a symbol did not change the cape');
await win.fill('#capePrompt', 'lila Galaxie mit goldener Krone und "KING"');
await win.click('#capeAi');
await win.waitForSelector('#capeNote');
if (!(await win.locator('[data-symsel]').filter({ hasText: 'Krone' }).count())) problems.push('cape designer did not add the crown');
await win.waitForTimeout(600);
if (shots) await win.screenshot({ path: join(shots, 'launcher-cape-editor.png') });
await win.click('#capeEditor [aria-label="Schließen"]');
await page('cosmetics', 'Sammlung');
await win.waitForSelector('.item .thumb img', { timeout: 15000 }).catch(() => problems.push('no 3D cosmetic pictures on the cards'));
if (!(await win.locator('#tryOn').count())) problems.push('3D try-on missing');
await win.waitForTimeout(3000);
if (shots) await win.screenshot({ path: join(shots, 'launcher-cosmetics-cards.png') });
await win.locator('.item .thumb[data-slot=wings]').first().click().catch(() => {});
await win.waitForTimeout(1500);
if (shots) await win.screenshot({ path: join(shots, 'launcher-cosmetics.png') });
await page('shop', 'Shop');
await page('friends', 'Alex');
await page('rewards', 'Tägliche Belohnung');
await page('news', 'Launcher-Test');
await page('feedback', 'Neuer Beitrag');
await page('account', 'LegoDev');
await page('dev', 'Entwickler-Dashboard');
await page('settings', 'Einstellungen');
await win.fill('#sq', 'geräusch');
await win.waitForTimeout(200);
const found = await win.locator('.setting:not([hidden])').count();
if (found !== 1) problems.push(`settings search for "geräusch" shows ${found} rows`);
if (await win.locator('[data-tab=rarity], [data-tab=appearance]').count()) problems.push('removed settings tabs are still there');
if ((await win.locator('#introPlay').count()) > 1) problems.push('intro option appears twice');
await win.fill('#sq', '');
// Designs studio: accent override, 3D scene, effects, presets.
await page('designs', 'Designs');
await win.click('[data-tab=colors]');
await win.click('[data-accent="#ec4899"]');
await win.waitForTimeout(400);
const accent = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--accent').trim());
if (accent !== '#ec4899') problems.push(`accent override not applied: ${accent}`);
// Nothing may stay blue with a pink accent (window edge, rail, panels, switches, buttons).
const blue = await win.evaluate(() => {
  const rgb = (v) => {
    const n = (v.match(/[\d.]+/g) ?? []).map(Number);
    if (v.startsWith('color(')) return n.slice(0, 3).map((x) => x * 255);
    return n.slice(0, 3);
  };
  const isBlue = (v) => { if (!v || v === 'transparent' || v.startsWith('rgba(0, 0, 0, 0')) return false; const [r, g, b] = rgb(v); return b > r + 40 && b > g; };
  const out = [];
  const els = ['.nr-launcher', '#sidebar', '.nr-titlebar', '.set-body .rows', '.switch i', '.btn', '.set-nav nav button', '.set-nav .searchbar'];
  for (const sel of els) for (const el of [...document.querySelectorAll(sel)].slice(0, 6)) {
    const cs = getComputedStyle(el);
    for (const p of ['background-color', 'border-left-color', 'border-top-color', 'border-bottom-color']) if (isBlue(cs.getPropertyValue(p))) out.push(`${sel} ${p} ${cs.getPropertyValue(p)}`);
  }
  if (isBlue(getComputedStyle(document.body).backgroundColor)) out.push(`body ${getComputedStyle(document.body).backgroundColor}`);
  return out;
});
if (blue.length) problems.push(`blue left with a pink accent: ${blue.slice(0, 5).join('; ')}`);
if (shots) await win.screenshot({ path: join(shots, 'launcher-pink.png') });
// Own colour with the HSV picker (NoRisk style): drag in the field, apply.
await win.click('#customAccent');
await win.waitForSelector('#colorPicker #cpSv');
{
  const sv = await win.locator('#cpSv').boundingBox();
  const hue = await win.locator('#cpHue').boundingBox();
  await win.mouse.click(hue.x + hue.width / 2, hue.y + hue.height * 0.33); // green
  await win.mouse.click(sv.x + sv.width * 0.9, sv.y + sv.height * 0.1);
  await win.waitForTimeout(150);
  const hex = await win.inputValue('#cpHex');
  if (shots) await win.screenshot({ path: join(shots, 'launcher-colorpicker.png') });
  await win.click('#cpApply');
  await win.waitForSelector('#colorPicker', { state: 'detached' }).catch(() => problems.push('colour picker did not close'));
  const saved = await win.evaluate(() => window.lego.getSettings().then((s) => s.accentColor));
  if (!saved || saved.toUpperCase() !== hex.toUpperCase()) problems.push(`colour picker: saved ${saved}, picked ${hex}`);
  const g = parseInt(hex.slice(3, 5), 16), r = parseInt(hex.slice(1, 3), 16);
  if (!(g > r)) problems.push(`colour picker picked ${hex}, expected a green`);
}
await win.click('[data-accent=""]');
await win.waitForTimeout(300);
await win.click('[data-tab=scene]');
await win.waitForSelector('[data-bg=bricks]');
await win.click('[data-bg=bricks]');
await win.waitForTimeout(500);
if ((await win.evaluate(() => document.body.dataset.bg)) !== 'bricks') problems.push('2D background bricks not active');
await win.click('[data-bg=graveyard]');
await win.waitForTimeout(1200);
const bg3d = await win.evaluate(() => ({ id: document.body.dataset.bg, shown: !document.getElementById('bg3d').hidden }));
if (bg3d.id !== 'graveyard' && bg3d.id !== 'haunted') problems.push(`3D scene not selected: ${bg3d.id}`);
if (shots) await win.screenshot({ path: join(shots, 'launcher-designs-scene.png') });
await win.click('[data-tab=effects]');
await win.locator('[data-fx=particles]').fill('0.2');
await win.locator('[data-fx=glow]').uncheck();
let fx = '';
for (let i = 0; i < 25; i++) {
  await win.waitForTimeout(200);
  fx = readFileSync(join(userData, 'settings.json'), 'utf8');
  if (/"particles": 0\.2/.test(fx) && /"glow": false/.test(fx)) break;
}
if (!/"particles": 0\.2/.test(fx) || !/"glow": false/.test(fx)) problems.push('effect settings were not saved: ' + JSON.stringify(JSON.parse(fx).fx));
if (shots) await win.screenshot({ path: join(shots, 'launcher-designs-effects.png') });
await win.click('[data-tab=presets]');
await win.fill('#pname', 'E2E Nacht');
await win.click('#psave');
await win.waitForSelector('[data-apply][data-own="1"]');
await win.locator('[data-apply="blood-moon"]').click();
await win.waitForTimeout(600);
if ((await win.evaluate(() => document.body.dataset.bg)) === 'bricks') problems.push('built-in preset not applied');
await win.locator('[data-apply][data-own="1"]').first().click();
await win.waitForTimeout(400);
await win.click('[data-tab=colors]');
await win.waitForSelector('[data-theme=matrix]');

// Theme switching really changes the CSS variables.
const before = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--accent'));
await win.click('[data-theme=matrix]');
await win.waitForTimeout(300);
const after = await win.evaluate(() => getComputedStyle(document.documentElement).getPropertyValue('--accent'));
const bodyClass = await win.evaluate(() => document.body.className);
if (before === after) problems.push('theme switch did not change --accent');
if (!bodyClass.includes('font-mono') || !bodyClass.includes('bg-scanlines')) problems.push(`matrix theme classes missing: ${bodyClass}`);
await layoutOk('matrix');
// Every theme background kind must keep the layout intact (regression: class name clash with "bg-grid").
for (const id of ['nightfall', 'matrix', 'lego-graphite']) {
  await win.click(`[data-theme=${id}]`);
  await win.waitForTimeout(150);
  await layoutOk(`theme ${id}`);
}
if (shots) await win.screenshot({ path: join(shots, 'launcher-theme-matrix.png') });

await win.click('[data-theme=lego-graphite]');
// Fonts, button styles and an own language.
await page('designs', 'Designs');
await win.click('[data-tab=style]');
await win.click('[data-btnstyle=brick]');
await win.click('[data-head=creepster]');
await win.waitForTimeout(400);
const cls = await win.evaluate(() => document.body.className);
if (!cls.includes('btn-brick') || !cls.includes('hf-creepster')) problems.push(`button/font style not applied: ${cls}`);
const headFont = await win.evaluate(() => getComputedStyle(document.querySelector('.set-nav h1')).fontFamily);
if (!headFont.includes('Creepster')) problems.push(`heading font choice not applied: ${headFont}`);
if (shots) await win.screenshot({ path: join(shots, 'launcher-designs-style.png') });
await page('play', 'LegoDev');
if (shots) await win.screenshot({ path: join(shots, 'launcher-play-brick.png') });
await page('designs', 'Designs');
await win.click('[data-tab=style]');
await win.click('[data-btnstyle=nocturne]');
await win.click('[data-head=poppins]');
await page('settings', 'Einstellungen');
await win.click('[data-tab=language]');
if ((await win.locator('#langGrid [data-lang]').count()) < 16) problems.push('language tiles missing');
await win.click('#customLang summary');
await win.fill('#lname', 'Testisch');
await win.fill('input[data-k="nav.home"]', 'Zuhause');
await win.click('#lsave');
await win.waitForTimeout(600);
if (!(await win.locator('#sidebar [data-page=play]').innerText()).toLowerCase().includes('zuhause')) problems.push('custom language not applied to the dock');
await win.click('#langGrid [data-lang=de]');
await win.waitForTimeout(400);

// VR mode: page, desktop preview (no headset in CI), click GUI + browser panels, exit with Esc.
await page('vr', 'VR-Modus');
await win.waitForSelector('#vrStatus:not(:has(.spinner))', { timeout: 10_000 }).catch(() => problems.push('VR status never resolved'));
await win.click('#vrPreview');
await win.waitForSelector('.vr-root');
await win.waitForFunction(() => Number(document.querySelector('.vr-root')?.dataset.t) >= 4, null, { timeout: 120_000, polling: 200 }).catch(() => problems.push('VR lobby did not run'));
// Headset-style desktop: key hints, dock; the dock's browser button closes and reopens the window.
if (!(await win.isVisible('.vr-keys'))) problems.push('VR key hints missing');
const vrClick = async () => {
  // Wait until the dock has stopped moving (it slides in and follows the view).
  let pos = null;
  for (let i = 0; i < 40; i++) {
    const a = await win.evaluate(() => document.querySelector('.vr-root')?.dataset.compass ?? null);
    await win.waitForTimeout(500);
    const b = await win.evaluate(() => document.querySelector('.vr-root')?.dataset.compass ?? null);
    if (a && a === b) { pos = b; break; }
  }
  if (!pos) { problems.push('VR dock not on screen'); return; }
  const [x, y] = pos.split(',').map(Number);
  await win.mouse.move(x, y);
  await win.mouse.down();
  await win.mouse.up();
};
await win.waitForFunction(() => document.querySelector('.vr-root')?.dataset.browser === '1', null, { timeout: 20_000 }).catch(() => problems.push('VR browser window not shown'));
await vrClick();
await win.waitForFunction(() => document.querySelector('.vr-root')?.dataset.browser === '0', null, { timeout: 20_000 }).catch(async () => problems.push(`VR dock did not close the browser (hit ${await win.evaluate(() => document.querySelector('.vr-root')?.dataset.lastHit)}, dock ${await win.evaluate(() => document.querySelector('.vr-root')?.dataset.compass)} ${await win.evaluate(() => document.querySelector('.vr-root')?.dataset.dockInfo)})`));
await vrClick();
await win.waitForFunction(() => document.querySelector('.vr-root')?.dataset.browser === '1', null, { timeout: 20_000 }).catch(() => problems.push('VR dock did not reopen the browser'));
await win.waitForTimeout(1500);
if (shots) await win.screenshot({ path: join(shots, 'launcher-vr-lobby.png') });
await win.keyboard.press('Escape');
await win.waitForSelector('.vr-root', { state: 'detached', timeout: 8000 }).catch(() => problems.push('VR did not close on Esc'));

// Buy something through the launcher shop.
await win.click('#tbMore');
await win.click('[data-page=shop]');
await win.waitForSelector('[data-buy]');
await win.locator('[data-buy]').first().click();
await win.click('dialog button[value=ok]');
await win.waitForSelector('.toast.ok');
const inv = await call(me.token, 'GET', '/api/inventory');
if (inv.owned.filter((id) => !id.startsWith('tag_')).length === 0 && inv.owned.length < 30) problems.push('purchase not reflected in inventory');

// Chat from the launcher.
await win.click('#tbFriends');
await win.click('[data-chat]');
await win.waitForSelector('#send');
await win.fill('#send input', 'Antwort aus dem Launcher');
await win.click('#send button');
await win.waitForTimeout(400);
const thread = await call(friend.token, 'GET', `/api/messages/${me.user.id}`);
if (!thread.messages.some((m) => m.body === 'Antwort aus dem Launcher')) problems.push('chat message not delivered');
if (shots) await win.screenshot({ path: join(shots, 'launcher-chat.png') });

await eapp.close();

// Fresh first start: no settings, no account. Welcome → guest mode, NoRisk layout, LEGO Client
// profile ready, a new profile can be created, no errors.
{
  const freshData = mkdtempSync(join(tmpdir(), 'lego-launcher-fresh-'));
  const fresh = await electron.launch({
    args: ['--no-sandbox', '.'],
    cwd: new URL('..', import.meta.url).pathname,
    env: { ...process.env, LEGO_USER_DATA: freshData, LEGO_E2E: '1', LEGO_E2E_WELCOME: '1', ELECTRON_DISABLE_SECURITY_WARNINGS: '1' },
  });
  const fw = await fresh.firstWindow();
  const fp = [];
  fw.on('pageerror', (e) => fp.push(`fresh pageerror: ${e.message}`));
  fw.on('console', (m) => { if (m.type() === 'error' && !/Failed to load resource|net::ERR/.test(m.text())) fp.push(`fresh console: ${m.text()}`); });
  await fw.setViewportSize?.({ width: 1280, height: 800 });
  await fw.waitForSelector('#guestGo', { timeout: 20_000 }).catch(() => fp.push('fresh start: welcome dialog missing'));
  if (await fw.locator('#guestGo').count()) await fw.click('#guestGo');
  await fw.waitForSelector('#sidebar [data-page=play]');
  const layout = await fw.getAttribute('[data-layout]', 'data-layout');
  if (layout !== 'classic') fp.push(`fresh start: layout is ${layout}, expected the NoRisk layout`);
  await fw.click('#sidebar [data-page=profiles]');
  await fw.waitForTimeout(600);
  if (!(await fw.locator('#view').innerText()).includes('LEGO Client')) fp.push('fresh start: LEGO Client profile missing');
  await fw.click('#new');
  await fw.waitForSelector('#f [name=name]');
  await fw.fill('#f [name=name]', 'Erster Start');
  await fw.locator('#f button.btn.primary').first().click();
  await fw.waitForTimeout(800);
  const st = await fw.evaluate(() => window.lego.getSettings());
  if (!st.guest) fp.push('fresh start: guest mode not stored');
  if (!st.profiles.some((p) => p.name === 'Erster Start')) fp.push('fresh start: new profile not saved');
  if (shots) await fw.screenshot({ path: join(shots, 'launcher-fresh-start.png') });
  await fresh.close();
  rmSync(freshData, { recursive: true, force: true });
  problems.push(...fp);
}
srv.closeAllConnections(); srv.close(); session.close(); app.events.close(); app.db.close();
rmSync(dataDir, { recursive: true, force: true });
rmSync(userData, { recursive: true, force: true });
if (problems.length) {
  console.error(`LAUNCHER UI E2E FAILED (${problems.length}):\n${problems.join('\n')}`);
  process.exit(1);
}
console.log('LAUNCHER UI E2E OK: all pages rendered, theme switch, shop purchase and chat worked.');
