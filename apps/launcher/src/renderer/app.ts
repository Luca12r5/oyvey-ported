// Launcher renderer: state, icon rail, title bar (coins + account switcher),
// animated background and page routing.

import type { AppInfo, AuthState } from '../ipc-types.ts';
import type { Settings } from '../core/settings.ts';
import { api, dialog, html, mount, toast, errorToast, fmtNum, lego } from './ui.ts';
import { applyTheme, findTheme } from './theme.ts';
import { brickLogo, icon, pixel } from './icons.ts';
import { Background } from './bg.ts';
import { headFromSkin } from './avatar.ts';
import { setLang, t } from './i18n.ts';
import { playPage, profileMenu, profileLine } from './pages/play.ts';
import { skinsPage } from './pages/skins.ts';
import { playSplash } from './splash.ts';
import { webglAvailable } from './intro3d.ts';
import { playIntroCave } from './introcave.ts';
import { armJumpscare } from './jumpscare.ts';
import { profilesPage } from './pages/profiles.ts';
import { cosmeticsPage, shopPage } from './pages/cosmetics.ts';
import { friendsPage } from './pages/social.ts';
import { rewardsPage } from './pages/rewards.ts';
import { accountPage, signInFlow } from './pages/account.ts';
import { openSettingsAt, settingsPage } from './pages/settings.ts';
import { mountSpotifyHud, updateSpotifyHud } from './spotifyhud.ts';
import { designsPage } from './pages/designs.ts';
import { vrPage } from './pages/vr.ts';
import { newsPage, feedbackPage, devPage } from './pages/misc.ts';

export interface State {
  settings: Settings;
  auth: AuthState;
  info: AppInfo;
  page: string;
  /** Face of the active account (data URL), shared by title bar and pages. */
  head: string;
  skin: { skin: string | null; variant: 'classic' | 'slim'; cape: string | null };
  coins: number | null;
}

export const state = { head: '', skin: { skin: null, variant: 'classic', cape: null }, coins: null } as unknown as State;

type Page = (el: HTMLElement) => Promise<void> | void;
/** `slot`: main dock, bottom of the dock, or the "Mehr" menu. */
interface PageDef { id: string; label: () => string; icon: string; page: Page; needsLego?: boolean; staff?: boolean; slot: 'main' | 'bottom' | 'more' | 'hidden' }
const PAGES: PageDef[] = [
  { id: 'play', label: () => t('nav.home'), icon: 'play', page: playPage, slot: 'main' },
  { id: 'profiles', label: () => t('nav.profiles'), icon: 'profiles', page: profilesPage, slot: 'main' },
  { id: 'skins', label: () => 'Skins', icon: 'skins', page: skinsPage, slot: 'main' },
  { id: 'friends', label: () => t('nav.friends'), icon: 'friends', page: friendsPage, needsLego: true, slot: 'main' },
  { id: 'cosmetics', label: () => t('nav.cosmetics'), icon: 'cosmetics', page: cosmeticsPage, needsLego: true, slot: 'main' },
  // Designs live under Optionen now; the route stays for deep links.
  { id: 'designs', label: () => t('nav.designs'), icon: 'brush', page: (el) => designsPage(el), slot: 'hidden' },
  { id: 'vr', label: () => t('nav.vr'), icon: 'vr', page: vrPage, slot: 'main' },
  { id: 'shop', label: () => t('nav.shop'), icon: 'shop', page: shopPage, needsLego: true, slot: 'more' },
  { id: 'rewards', label: () => t('nav.rewards'), icon: 'rewards', page: rewardsPage, needsLego: true, slot: 'more' },
  { id: 'news', label: () => t('nav.news'), icon: 'news', page: newsPage, slot: 'more' },
  { id: 'feedback', label: () => t('nav.feedback'), icon: 'feedback', page: feedbackPage, needsLego: true, slot: 'more' },
  { id: 'account', label: () => t('nav.account'), icon: 'account', page: accountPage, slot: 'more' },
  { id: 'dev', label: () => t('nav.dev'), icon: 'dev', page: devPage, staff: true, slot: 'more' },
  { id: 'settings', label: () => t('nav.settings'), icon: 'settings', page: settingsPage, slot: 'bottom' },
];

/** Shorter labels for the dock (full names stay in tooltips/aria). */
const SHORT: Record<string, () => string> = { vr: () => 'VR', settings: () => (state.settings.language === 'en' ? 'Settings' : 'Optionen'), cosmetics: () => 'Shop', friends: () => (state.settings.language === 'en' ? 'Friends' : 'Freunde') };

let bg: Background | null = null;
let leaveHooks: (() => void)[] = [];
const back: string[] = [];
const fwd: string[] = [];
/** Bell menu: recent events of this session (friend requests, invites, messages…). */
const notes: { text: string; at: number; read: boolean }[] = [];

export function notify(text: string): void {
  notes.unshift({ text, at: Date.now(), read: false });
  notes.length = Math.min(notes.length, 30);
  renderBell();
}

function renderBell(): void {
  const b = document.getElementById('tbBell');
  if (!b) return;
  const unread = notes.filter((n) => !n.read).length;
  mount(b, html`${icon('bell', 'sm')}${unread ? html`<i class="badge-n">${unread > 9 ? '9+' : unread}</i>` : ''}`);
}

function bellMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const m = document.createElement('div');
  m.className = 'menu bell-menu';
  m.setAttribute('role', 'menu');
  mount(m, html`<div class="menu-h">Benachrichtigungen</div>${notes.length
    ? html`<div class="menu-scroll">${notes.map((n) => html`<div class="note ${n.read ? '' : 'new'}"><span>${n.text}</span><small>${new Date(n.at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</small></div>`)}</div>`
    : html`<p class="small muted pad">Nichts Neues.</p>`}`);
  notes.forEach((n) => { n.read = true; });
  renderBell();
  document.body.append(m);
  const r = anchor.getBoundingClientRect();
  m.style.top = `${r.bottom + 6}px`;
  m.style.right = `${Math.max(8, innerWidth - r.right)}px`;
  const close = (e?: Event) => {
    if (e && (m.contains(e.target as Node) || anchor.contains(e.target as Node))) return;
    m.remove();
    document.removeEventListener('mousedown', close);
  };
  setTimeout(() => document.addEventListener('mousedown', close));
}

let online: number | null = null;
async function refreshOnline(): Promise<void> {
  try {
    const st = await lego<{ online?: number }>('GET', '/api/public/status');
    online = typeof st.online === 'number' ? st.online : null;
  } catch { online = null; }
  const el = document.getElementById('tbOnline');
  if (el) { el.hidden = online === null; el.textContent = online === null ? '' : fmtNum(online); }
}

/** Registers cleanup for the current page (runs on the next navigation). */
export function onLeave(fn: () => void): void {
  leaveHooks.push(fn);
}

export function backgroundInfo(): { w: number; h: number; tier: string } | null {
  return bg?.renderInfo() ?? null;
}

let fontKey = '';
/** Registers the user's font file as "LegoCustom" (from bytes, so no CSP change is needed). */
async function loadCustomFont(f: { name: string; data: string } | null): Promise<void> {
  const key = f ? `${f.name}:${f.data.length}` : '';
  if (key === fontKey) return;
  fontKey = key;
  for (const ff of [...document.fonts]) if (ff.family === 'LegoCustom') document.fonts.delete(ff);
  if (!f) return;
  try {
    const bin = Uint8Array.from(atob(f.data.slice(f.data.indexOf(',') + 1)), (c) => c.charCodeAt(0));
    const face = new FontFace('LegoCustom', bin.buffer);
    await face.load();
    document.fonts.add(face);
  } catch {
    errorToast(new Error('Die eigene Schrift konnte nicht geladen werden.'));
  }
}

/** Lower frame rate while Minecraft runs. */
export function backgroundLowPower(on: boolean): void {
  bg?.setLowPower(on);
}

let lastLang = '';

export function retheme(): void {
  const s = state.settings;
  setLang(s.language, s.customLanguage);
  // Language changes must show up in the dock and title bar right away.
  const langKey = `${s.language}|${JSON.stringify(s.customLanguage?.strings ?? null)}`;
  if (langKey !== lastLang) {
    const first = lastLang === '';
    lastLang = langKey;
    if (!first && state.auth) refreshChrome();
  }
  applyTheme(findTheme(s.themeId, s.customThemes), { reducedMotion: s.reducedMotion, uiScale: s.uiScale });
  if (s.accentColor) document.documentElement.style.setProperty('--accent', s.accentColor);
  bg ??= new Background(document.getElementById('bg') as HTMLCanvasElement, document.getElementById('bg3d') as HTMLCanvasElement);
  bg.configure({ id: s.background, quality: s.backgroundQuality, snow: s.snow, still: s.reducedMotion, fx: s.fx, image: s.bgImage });
  document.body.classList.add('nocturne', 'crypt', `surf-${s.surface}`, `stage-${s.stage}`, `uif-${s.uiFont}`, `hf-${s.headFont}`, `btn-${s.buttonStyle}`);
  void loadCustomFont(s.customFont);
  document.body.classList.toggle('no-vignette', !s.fx.vignette);
  document.body.classList.toggle('no-glow', !s.fx.glow);
  if (state.info && !state.info.e2e) armJumpscare(s.jumpscares, s.reducedMotion);
  if (spotifyMounted) updateSpotifyHud(s.spotifyHud);
}
let spotifyMounted = false;

function visiblePages(): PageDef[] {
  const staff = state.auth.lego.roles.length > 0;
  return PAGES.filter((p) => !p.staff || staff);
}

function renderSidebar(): void {
  document.body.dataset.staff = state.auth.lego.roles.length > 0 ? '1' : '0';
  const more = visiblePages().filter((p) => p.slot === 'more');
  const inMore = more.some((p) => p.id === state.page);
  const item = (p: PageDef) => html`<button class="nav-item ${state.page === p.id ? 'active' : ''}" data-page="${p.id}" aria-label="${p.label()}" title="${p.label()}">${icon(p.icon)}<span class="lbl">${SHORT[p.id]?.() ?? p.label()}</span></button>`;
  const list = visiblePages();
  mount(document.getElementById('sidebar')!, html`<div class="rail-logo" title="LEGO Launcher">${icon('pumpkin')}</div><span class="rail-ind" aria-hidden="true"></span>
    ${list.filter((p) => p.slot === 'main').map(item)}<div class="spacer"></div>
    <button class="nav-item ${inMore ? 'active' : ''}" id="more" aria-haspopup="menu" aria-label="${t('nav.more')}" title="${t('nav.more')}">${icon('grid')}<span class="lbl">${t('nav.more')}</span></button>
    ${list.filter((p) => p.slot === 'bottom').map(item)}`);
  requestAnimationFrame(moveIndicator);
}

/** Slides the glowing marker to the active dock item. */
function moveIndicator(): void {
  const rail = document.getElementById('sidebar');
  const ind = rail?.querySelector<HTMLElement>('.rail-ind');
  const act = rail?.querySelector<HTMLElement>('.nav-item.active');
  if (!rail || !ind) return;
  ind.classList.toggle('on', !!act);
  if (act) ind.style.transform = `translateY(${act.offsetTop}px)`;
}

function moreMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const m = document.createElement('div');
  m.className = 'menu more-menu';
  m.setAttribute('role', 'menu');
  mount(m, html`<div class="menu-h">${t('nav.more')}</div><div class="more-grid">${visiblePages().filter((p) => p.slot === 'more').map((p) => html`<button class="more-i ${state.page === p.id ? 'on' : ''}" data-page="${p.id}" role="menuitem">${icon(p.icon)}<span>${p.label()}</span></button>`)}</div>`);
  document.body.append(m);
  const r = anchor.getBoundingClientRect();
  m.style.left = `${r.right + 12}px`;
  m.style.top = `${Math.max(60, Math.min(r.top - 40, innerHeight - m.offsetHeight - 16))}px`;
  const close = (e?: Event) => {
    if (e && (m.contains(e.target as Node) || anchor.contains(e.target as Node))) return;
    m.remove();
    document.removeEventListener('mousedown', close);
  };
  setTimeout(() => document.addEventListener('mousedown', close));
  m.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-page]');
    if (!b) return;
    close();
    void go(b.dataset.page!);
  });
}

function renderWindowButtons(st: { maximized: boolean; fullscreen: boolean }): void {
  mount(document.getElementById('win')!, html`<button class="tb-btn" data-win="minimize" aria-label="Minimieren" title="Minimieren">${icon('minimize', 'sm')}</button>
    <button class="tb-btn" data-win="fullscreen" aria-label="Vollbild" title="Vollbild (F11)">${icon(st.fullscreen ? 'exitFullscreen' : 'fullscreen', 'sm')}</button>
    <button class="tb-btn" data-win="maximize" aria-label="${st.maximized || st.fullscreen ? 'Wiederherstellen' : 'Maximieren'}" title="${st.maximized || st.fullscreen ? 'Wiederherstellen' : 'Maximieren'}">${icon(st.maximized || st.fullscreen ? 'restore' : 'maximize', 'sm')}</button>
    <button class="tb-btn close" data-win="close" aria-label="Schließen" title="Schließen">${icon('close', 'sm')}</button>`);
  document.body.classList.toggle('is-fullscreen', st.fullscreen);
  document.body.classList.toggle('is-maximized', st.maximized);
}

function renderTitlebar(): void {
  document.getElementById('tbVersion')!.textContent = `v${state.info.version}`;
  mount(document.getElementById('navBack')!, icon('back', 'sm'));
  mount(document.getElementById('navFwd')!, icon('forward', 'sm'));
  (document.getElementById('navBack') as HTMLButtonElement).disabled = back.length === 0;
  (document.getElementById('navFwd') as HTMLButtonElement).disabled = fwd.length === 0;
  mount(document.getElementById('tbFriends')!, icon('friends', 'sm'));
  renderBell();
  const sel = state.settings.profiles.find((p) => p.id === state.settings.selectedProfile) ?? state.settings.profiles[0];
  mount(document.getElementById('tbInst')!, html`<button class="tb-chip" id="instBtn" aria-haspopup="menu" title="${sel ? profileLine(sel) : ''}">${icon('cube', 'sm')}<span>${sel ? sel.name : 'Keine Instanz'}</span>${icon('chevron', 'xs')}</button>`);
  document.body.classList.toggle('guest', !state.auth.signedIn);
  const coins = document.getElementById('tbCoins')!;
  coins.hidden = state.coins === null;
  if (state.coins !== null) mount(coins, html`${pixel('coin')}<b>${fmtNum(state.coins)}</b>`);
  const a = state.auth;
  const acct = document.getElementById('acct')!;
  mount(acct, a.signedIn
    ? html`<button class="acct-chip" id="acctBtn" aria-haspopup="menu">${state.head ? html`<img src="${state.head}" alt="">` : html`<span class="ph"></span>`}<span class="nm"><b>${a.name}</b><small>${a.lego.connected ? 'Microsoft' : t('acc.offline')}</small></span>${icon('chevron', 'sm')}</button>`
    : html`<button class="acct-chip add" id="acctBtn">${icon('plus', 'sm')}<span class="nm"><b>${t('acc.signin')}</b></span></button>`);
}

function accountMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const a = state.auth;
  const m = document.createElement('div');
  m.className = 'menu';
  m.setAttribute('role', 'menu');
  mount(m, html`<div class="menu-h">${t('acc.manage')}</div>
    ${a.accounts.map((x) => html`<button class="menu-i ${x.active ? 'on' : ''}" data-switch="${x.uuid}" role="menuitem"><span class="av" data-uuid="${x.uuid}"></span><span class="grow">${x.name}</span>${x.active ? icon('check', 'sm') : ''}</button>`)}
    <button class="menu-i" data-act="add" role="menuitem">${icon('plus', 'sm')}<span class="grow">${t('acc.add')}</span></button>
    <div class="menu-sep"></div>
    <button class="menu-i" data-act="account" role="menuitem">${icon('account', 'sm')}<span class="grow">${t('nav.account')}</span></button>
    <button class="menu-i danger" data-act="signout" role="menuitem">${icon('logout', 'sm')}<span class="grow">${t('acc.signout')}</span></button>`);
  document.body.append(m);
  const r = anchor.getBoundingClientRect();
  m.style.top = `${r.bottom + 6}px`;
  m.style.right = `${Math.max(8, innerWidth - r.right)}px`;
  m.querySelectorAll<HTMLElement>('.av').forEach((el) => {
    void api.skin(el.dataset.uuid).then((s) => headFromSkin(s.skin, 32)).then((src) => { if (src) { const i = new Image(); i.src = src; el.append(i); } });
  });
  const close = (e?: Event) => {
    if (e && m.contains(e.target as Node)) return;
    m.remove();
    document.removeEventListener('mousedown', close);
  };
  setTimeout(() => document.addEventListener('mousedown', close));
  m.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    close();
    try {
      if (b.dataset.switch && b.dataset.switch !== a.uuid) {
        state.auth = await api.switchAccount(b.dataset.switch);
        await loadIdentity();
        toast(`${state.auth.name}`, 'ok');
        void go(state.page);
      } else if (b.dataset.act === 'add') void signInFlow();
      else if (b.dataset.act === 'account') void go('account');
      else if (b.dataset.act === 'signout') {
        state.auth = await api.signOut();
        await loadIdentity();
        void go('play');
      }
    } catch (err) { errorToast(err); }
  });
}

/** Skin, face and coin balance of the active account. */
export async function loadIdentity(): Promise<void> {
  // Signed in: the skin you play with. Guest: the public skin of the name entered on the Skins tab.
  state.skin = state.auth.signedIn || state.settings.guestName ? await api.skin().catch(() => ({ skin: null, variant: 'classic' as const, cape: null })) : { skin: null, variant: 'classic', cape: null };
  state.head = state.auth.signedIn ? await headFromSkin(state.skin.skin, 64) : '';
  state.coins = null;
  if (state.auth.lego.connected) {
    try {
      state.coins = (await lego<{ balance: number }>('GET', '/api/me/credits')).balance;
    } catch { /* offline */ }
  }
  refreshChrome();
}

export async function go(page: string, how: 'push' | 'back' | 'fwd' | 'replace' = 'push'): Promise<void> {
  const from = state.page;
  if (from && from !== page) {
    if (how === 'push') { back.push(from); fwd.length = 0; }
    else if (how === 'back') fwd.push(from);
    else if (how === 'fwd') back.push(from);
    if (back.length > 30) back.shift();
  }
  state.page = page;
  for (const fn of leaveHooks.splice(0)) {
    try { fn(); } catch { /* ignore */ }
  }
  renderSidebar();
  document.querySelector('.menu')?.remove();
  const def = PAGES.find((p) => p.id === page) ?? PAGES[0]!;
  const host = document.getElementById('view')!;
  host.dataset.page = def.id;
  const view = document.createElement('div');
  view.className = `view-enter tr-${state.settings.reducedMotion ? 'none' : state.settings.transition ?? 'slide'}`;
  renderTitlebar();
  host.replaceChildren(view);
  if (def.needsLego && !state.auth.lego.connected) {
    mount(view, html`<div class="card narrow"><h1>${def.label()}</h1>${state.auth.signedIn ? '' : html`<p><span class="badge c" data-c="#ff7a1a">${icon('ghost', 'xs')} Gastmodus</span></p>`}<p class="muted">Diese Seite braucht ein Konto und eine Verbindung zum LEGO-Server. Im Gastmodus kannst du Home, Profile, Designs, Startanimation, VR-Vorschau und alle Einstellungen ausprobieren.</p>
      <p>${state.auth.signedIn ? (state.auth.lego.error ?? 'Nicht verbunden.') : 'Melde dich zuerst mit deinem Microsoft-Konto an.'}</p>
      <div class="row">${state.auth.signedIn ? html`<button class="btn primary" id="reconnect">Erneut verbinden</button>` : html`<button class="btn primary" id="signin">${t('play.signin')}</button>`}</div></div>`);
    view.querySelector('#reconnect')?.addEventListener('click', async () => { state.auth = await api.reconnectLego(); await loadIdentity(); void go(page); });
    view.querySelector('#signin')?.addEventListener('click', () => void signInFlow());
    return;
  }
  try {
    await def.page(view);
  } catch (e) {
    mount(view, html`<div class="card"><h1>${def.label()}</h1><p>Fehler: ${e instanceof Error ? e.message : String(e)}</p></div>`);
  }
  host.focus({ preventScroll: true });
}

export function refreshChrome(): void {
  renderSidebar();
  renderTitlebar();
}

async function boot(): Promise<void> {
  [state.settings, state.auth, state.info] = await Promise.all([api.getSettings(), api.authState(), api.info()]);
  retheme();
  mountSpotifyHud(state.settings.spotifyHud, () => { openSettingsAt('spotify'); void go('settings'); }, (p) => {
    void api.setSettings({ spotifyHud: { ...state.settings.spotifyHud, ...p } }).then((next) => { state.settings = next; });
  });
  spotifyMounted = true;
  mount(document.getElementById('tbLogo')!, brickLogo());
  renderWindowButtons(await api.windowState());
  api.on('window-state', renderWindowButtons);
  refreshChrome();
  document.getElementById('sidebar')!.addEventListener('click', (e) => {
    const more = (e.target as HTMLElement).closest<HTMLButtonElement>('#more');
    if (more) return moreMenu(more);
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-page]');
    if (b) void go(b.dataset.page!);
  });
  document.getElementById('win')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-win]');
    if (b) api.window(b.dataset.win as 'minimize');
  });
  addEventListener('resize', moveIndicator);
  document.getElementById('navBack')!.addEventListener('click', () => { const p = back.pop(); if (p) void go(p, 'back'); });
  document.getElementById('navFwd')!.addEventListener('click', () => { const p = fwd.pop(); if (p) void go(p, 'fwd'); });
  addEventListener('mouseup', (e) => {
    if (e.button === 3) { const p = back.pop(); if (p) void go(p, 'back'); }
    if (e.button === 4) { const p = fwd.pop(); if (p) void go(p, 'fwd'); }
  });
  document.getElementById('tbBell')!.addEventListener('click', (e) => bellMenu(e.currentTarget as HTMLElement));
  document.getElementById('tbFriends')!.addEventListener('click', () => void go('friends'));
  document.getElementById('tbInst')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLElement>('#instBtn');
    if (b) profileMenu(b, 'down');
  });
  void refreshOnline();
  setInterval(() => void refreshOnline(), 60_000);
  document.getElementById('acct')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('#acctBtn');
    if (!b) return;
    if (state.auth.signedIn) accountMenu(b);
    else void signInFlow();
  });
  api.on('auth', (a: AuthState & { error?: string }) => {
    const changed = a.uuid !== state.auth.uuid || a.lego.connected !== state.auth.lego.connected;
    state.auth = a;
    if (a.error) errorToast(new Error(a.error));
    refreshChrome();
    if (changed) void loadIdentity().then(() => { if (state.page === 'play') void go('play'); });
    const d = document.getElementById('dialog') as HTMLDialogElement;
    if (a.signedIn && d.open && d.dataset.kind === 'signin') {
      d.close();
      toast(`Angemeldet als ${a.name}`, 'ok');
      void go(state.page);
    }
  });
  api.on('lego-event', (ev: { type: string; data: { from?: { name?: string }; from_id?: string; body?: string } }) => {
    if (ev.type === 'duel') {
      // Games live in the client only; the launcher just tells you about the invite.
      const m = ev.data as unknown as { k?: string; from?: { name?: string } };
      if (m.k === 'inv') { toast(`${m.from?.name ?? 'Jemand'} fordert dich zu einem Duell heraus – im Spiel annehmen`, 'ok'); notify(`Duell-Einladung von ${m.from?.name ?? 'Jemand'}`); }
      return;
    }
    const say = (text: string, kind?: 'ok') => { toast(text, kind); notify(text); };
    if (ev.type === 'friend_request') say(`${ev.data.from?.name ?? 'Jemand'} möchte dein Freund sein`, 'ok');
    else if (ev.type === 'friend_accepted') say('Freundschaftsanfrage angenommen', 'ok');
    else if (ev.type === 'party_invite') say(`${ev.data.from?.name ?? 'Jemand'} lädt dich in eine Party ein`);
    else if (ev.type === 'message' && state.page !== 'friends') say(`Neue Nachricht: ${String(ev.data.body ?? '').slice(0, 60)}`);
  });
  api.on('update', (u: { state: string; version?: string; error?: string; percent?: number }) => {
    if (u.state === 'available' && state.settings.autoUpdate === 'ask') toast(`Update ${u.version} verfügbar – Einstellungen → Über`);
    if (u.state === 'ready') toast(`Update ${u.version} bereit. Wird beim Beenden installiert.`, 'ok');
  });
  // The intro covers the window while the real UI is built underneath it, so
  // the final portal opens straight into the launcher.
  const intro = startIntro();
  await loadIdentity();
  await go('play');
  await intro;
  bg?.start();
  if (!state.auth.signedIn && !state.settings.guest && !state.info.e2e) welcome();
}

/** First start without an account: sign in, or try everything as a guest. */
function welcome(): void {
  const d = dialog(html`<h2>Willkommen beim LEGO Launcher</h2>
    <p>Melde dich mit dem Microsoft-Konto an, das Minecraft: Java Edition besitzt – oder schau dich erst als Gast um.</p>
    <ul class="small muted"><li>Als Gast: Designs, 3D-Hintergründe, Startanimation, Charakter, Profile, Einstellungen und VR-Vorschau ausprobieren.</li><li>Mit Konto: Spielen, Freunde, Cosmetics, Shop und Belohnungen.</li></ul>
    <div class="row between gap"><button class="btn" id="guestGo">${icon('ghost', 'sm')} Als Gast ausprobieren</button><button class="btn primary" id="signGo">${t('play.signin')}</button></div>`);
  d.querySelector('#guestGo')!.addEventListener('click', async () => { d.close(); state.settings = await api.setSettings({ guest: true }); toast('Gastmodus aktiv – viel Spaß beim Umschauen!', 'ok'); });
  d.querySelector('#signGo')!.addEventListener('click', () => { d.close(); void signInFlow(); });
}

function startIntro(): Promise<void> {
  const s = state.settings;
  let shown = false;
  try { shown = !!sessionStorage.getItem('intro-shown'); sessionStorage.setItem('intro-shown', '1'); } catch { /* storage blocked */ }
  if (shown || !s.introEnabled || s.reducedMotion || state.info.e2e) return Promise.resolve();
  bg?.stop();
  if (!webglAvailable()) return playSplash();
  return playIntroCave({ sound: s.introSound, quality: s.backgroundQuality, skipLabel: t('intro.skip') }).catch((e) => console.warn('intro failed', e));
}

void boot();
