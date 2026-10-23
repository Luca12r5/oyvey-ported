// Launcher renderer: state, icon rail, title bar (coins + account switcher),
// animated background and page routing.

import type { AppInfo, AuthState } from '../ipc-types.ts';
import type { Settings } from '../core/settings.ts';
import { api, dialog, html, mount, toast, errorToast, fmtNum, lego } from './ui.ts';
import { classifyDrop } from '../core/dropimport.ts';
import { sanitizePreset } from '../core/design.ts';
import { applyTheme, findTheme } from './theme.ts';
import { icon } from './icons.ts';
import { Background } from './bg.ts';
import { headFromSkin } from './avatar.ts';
import { setLang, t } from './i18n.ts';
import { playPage } from './pages/play.ts';
import { setUiSounds } from './nr/lib/sound.ts';
import { mountShell, setAccent, shellStore, actions, gameStore, initConsoleWindow, initGame, NAV_ICONS, renderConsoleWindow } from './nr/mount.tsx';
import { themeStore, applyAccentToDOM } from './nr/store/theme.ts';
import type { CapeSpec } from '@lego/shared';
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
import { settingsPage } from './pages/settings.ts';
import { setLegoClientVersions } from '../core/settings.ts';
import { modsPage } from './pages/mods.ts';
import { capesPage } from './pages/capes.ts';
import { designsPage } from './pages/designs.ts';
import { vrPage } from './pages/vr.ts';
import { serversPage } from './pages/servers.ts';
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
  /** Equipped LEGO cosmetics (slot -> item id) and the own designed cape, from the LEGO server. */
  equipped: Record<string, string>;
  customCape: CapeSpec | null;
}

export const state = { head: '', skin: { skin: null, variant: 'classic', cape: null }, coins: null, equipped: {}, customCape: null } as unknown as State;

type Page = (el: HTMLElement) => Promise<void> | void;
/** `slot`: main dock, bottom of the dock, or the "Mehr" menu. */
interface PageDef { id: string; label: () => string; icon: string; page: Page; needsLego?: boolean; staff?: boolean; slot: 'main' | 'bottom' | 'more' | 'hidden' }
const PAGES: PageDef[] = [
  { id: 'play', label: () => t('nav.home'), icon: 'play', page: playPage, slot: 'main' },
  { id: 'profiles', label: () => (state.settings.language === 'en' ? 'Profiles' : 'Profile'), icon: 'profiles', page: profilesPage, slot: 'main' },
  { id: 'mods', label: () => 'Mods', icon: 'mods', page: modsPage, slot: 'main' },
  { id: 'skins', label: () => 'Skins', icon: 'skins', page: skinsPage, slot: 'main' },
  { id: 'capes', label: () => 'Capes', icon: 'cape', page: capesPage, slot: 'main' },
  // Designs live under Optionen now; the route stays for deep links.
  { id: 'designs', label: () => t('nav.designs'), icon: 'brush', page: (el) => designsPage(el), slot: 'hidden' },
  { id: 'friends', label: () => t('nav.friends'), icon: 'friends', page: friendsPage, needsLego: true, slot: 'more' },
  { id: 'cosmetics', label: () => t('nav.cosmetics'), icon: 'cosmetics', page: cosmeticsPage, needsLego: true, slot: 'more' },
  { id: 'shop', label: () => t('nav.shop'), icon: 'shop', page: shopPage, needsLego: true, slot: 'more' },
  { id: 'vr', label: () => t('nav.vr'), icon: 'vr', page: vrPage, slot: 'more' },
  { id: 'servers', label: () => (state.settings.language === 'en' ? 'Own server' : 'Eigener Server'), icon: 'globe', page: (el) => serversPage(el), slot: 'more' },
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
  shellStore.set({ unread: notes.filter((n) => !n.read).length });
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
  shellStore.set({ online });
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
  // The NoRisk-style UI follows the theme accent unless the user picked one.
  setAccent(s.accentColor || getComputedStyle(document.documentElement).getPropertyValue('--accent').trim());
  // Nacht tints its background from the accent like the NoRisk launcher (accent × 0.1), so a pink accent gives a dark pink, not navy.
  if (s.themeId === 'nacht') {
    const c = themeStore.get().accentColor.value;
    const ch = (i: number) => Math.min(Math.floor(parseInt(c.slice(i, i + 2), 16) * 0.1), 30).toString(16).padStart(2, '0');
    document.documentElement.style.setProperty('--bg', `#${ch(1)}${ch(3)}${ch(5)}`);
  }
  setUiSounds(s.uiSounds !== false && !state.info?.e2e);
  document.body.classList.toggle('reduced-motion', s.reducedMotion);
  document.body.classList.toggle('has-bg', s.background !== 'plain');
  bg ??= new Background(document.getElementById('bg') as HTMLCanvasElement, document.getElementById('bg3d') as HTMLCanvasElement);
  bg.configure({ id: s.background, quality: s.backgroundQuality, snow: s.snow, still: s.reducedMotion, fx: s.fx, image: s.bgImage, imageFit: s.look.imageFit });
  // Free customisation (Designs → Stil): frame, title, character.
  const look = s.look;
  themeStore.set({ borderRadius: look.radius, showNavLabels: look.navLabels, titleColors: look.titleColors, titleSub: look.titleSub, rigScale: look.rigScale / 100, nametag: look.nametag, glowLines: look.glowLines, layout: look.layout });
  document.body.classList.toggle('lx-studio', look.layout === 'studio');
  applyAccentToDOM();
  document.documentElement.style.setProperty('--shell-alpha', String(look.shellAlpha / 100));
  document.body.classList.toggle('no-blur', !s.fx.blur);
  // Drop the previous choice's classes before adding the current ones.
  for (const c of [...document.body.classList]) if (/^(surf|stage|uif|hf|btn)-/.test(c)) document.body.classList.remove(c);
  // The NoRisk-style layout is the launcher's layout, not a theme option.
  document.body.classList.add('nacht');
  document.body.classList.add('nocturne', 'crypt', `surf-${s.surface}`, `stage-${s.stage}`, `uif-${s.uiFont}`, `hf-${s.headFont}`, `btn-${s.buttonStyle}`);
  void loadCustomFont(s.customFont);
  document.body.classList.toggle('no-vignette', !s.fx.vignette);
  document.body.classList.toggle('no-glow', !s.fx.glow);
  if (state.info && !state.info.e2e) armJumpscare(s.jumpscares, s.reducedMotion);
}

function visiblePages(): PageDef[] {
  const staff = state.auth.lego.roles.length > 0;
  return PAGES.filter((p) => !p.staff || staff);
}

function renderSidebar(): void {
  document.body.dataset.staff = state.auth.lego.roles.length > 0 ? '1' : '0';
  const list = visiblePages();
  shellStore.set({
    page: state.page,
    nav: [...list.filter((p) => p.slot === 'main'), ...list.filter((p) => p.slot === 'bottom')].map((p) => ({ id: p.id, icon: NAV_ICONS[p.id] ?? 'solar:widget-bold', label: SHORT[p.id]?.() ?? p.label(), isAction: p.slot === 'bottom' })),
  });
}

function moreMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const m = document.createElement('div');
  m.className = 'menu more-menu';
  m.setAttribute('role', 'menu');
  mount(m, html`<div class="menu-h">${t('nav.more')}</div><div class="more-grid">${visiblePages().filter((p) => p.slot === 'more').map((p) => html`<button class="more-i ${state.page === p.id ? 'on' : ''}" data-page="${p.id}" role="menuitem">${icon(p.icon)}<span>${p.label()}</span></button>`)}</div>`);
  document.body.append(m);
  const r = anchor.getBoundingClientRect();
  if (r.left > innerWidth / 2) {
    // Opened from the title bar: drop down, right-aligned.
    m.style.left = `${Math.max(8, r.right - m.offsetWidth)}px`;
    m.style.top = `${r.bottom + 8}px`;
  } else {
    m.style.left = `${r.right + 12}px`;
    m.style.top = `${Math.max(60, Math.min(r.top - 40, innerHeight - m.offsetHeight - 16))}px`;
  }
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
  shellStore.set({ maximized: st.maximized, fullscreen: st.fullscreen });
  document.body.classList.toggle('is-fullscreen', st.fullscreen);
  document.body.classList.toggle('is-maximized', st.maximized);
}

function renderTitlebar(): void {
  document.body.classList.toggle('guest', !state.auth.signedIn);
  const a = state.auth;
  shellStore.set({
    version: state.info.version, canBack: back.length > 0, canFwd: fwd.length > 0,
    signedIn: a.signedIn, name: a.signedIn ? a.name ?? '' : state.settings.guestName ?? '', head: state.head, coins: state.coins,
    running: gameRunning ? 1 : 0,
  });
}

let gameRunning = false;

/** Guest: small account dropdown (sign in, what works without an account). */
function guestMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const m = document.createElement('div');
  m.className = 'menu';
  m.setAttribute('role', 'menu');
  mount(m, html`<div class="menu-h">Gastmodus</div>
    <button class="menu-i" data-act="signin" role="menuitem">${icon('account', 'sm')}<span class="grow">${t('play.signin')}</span></button>
    <div class="menu-sep"></div>
    <p class="menu-note">Ohne Konto kannst du Profile, Mods, Skins-Vorschau, Designs und VR ausprobieren. Spielen, Freunde und Shop brauchen ein Minecraft-Konto.</p>`);
  document.body.append(m);
  const r = anchor.getBoundingClientRect();
  m.style.top = `${r.bottom + 6}px`;
  m.style.right = `${Math.max(8, innerWidth - r.right)}px`;
  const close = (e?: Event) => { if (e && m.contains(e.target as Node)) return; m.remove(); document.removeEventListener('mousedown', close); };
  setTimeout(() => document.addEventListener('mousedown', close));
  m.addEventListener('click', (e) => { if ((e.target as HTMLElement).closest('[data-act=signin]')) { close(); void signInFlow(); } });
}

/** Friends open as a panel from the right (list, requests, search, chat). */
async function friendsDrawer(): Promise<void> {
  let d = document.getElementById('drawer');
  if (d?.classList.contains('open')) { d.classList.remove('open'); return; }
  if (!d) {
    d = document.createElement('aside');
    d.id = 'drawer';
    d.className = 'drawer';
    document.body.append(d);
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') d!.classList.remove('open'); });
    document.addEventListener('mousedown', (e) => {
      const t = e.target as HTMLElement;
      if (d!.classList.contains('open') && !d!.contains(t) && !t.closest('#tbFriends, .menu, dialog')) d!.classList.remove('open');
    });
  }
  mount(d, html`<div class="drawer-h"><span>${icon('friends', 'sm')} Freunde</span><button class="icon-btn" id="drawerX" aria-label="Schließen">${icon('close', 'sm')}</button></div><div class="drawer-b" id="drawerB"></div>`);
  d.querySelector('#drawerX')!.addEventListener('click', () => d!.classList.remove('open'));
  requestAnimationFrame(() => d!.classList.add('open'));
  const body = d.querySelector<HTMLElement>('#drawerB')!;
  if (!state.auth.lego.connected) {
    mount(body, html`<div class="drawer-empty">${icon('friends', 'lg')}<h2>Spiel mit Freunden</h2><p class="muted">Sieh, wer online ist, chatte, lade in deine Party ein und fordere Freunde zu Duellen heraus. Dafür brauchst du ein Minecraft-Konto und die Verbindung zum LEGO-Server.</p>
      ${state.auth.signedIn ? html`<p class="small muted">${state.auth.lego.error ?? 'Nicht mit dem LEGO-Server verbunden.'}</p><button class="btn primary" id="dRe">Erneut verbinden</button>` : html`<button class="btn primary" id="dIn">${icon('account', 'sm')} ${t('play.signin')}</button>`}</div>`);
    body.querySelector('#dIn')?.addEventListener('click', () => void signInFlow());
    body.querySelector('#dRe')?.addEventListener('click', async () => { state.auth = await api.reconnectLego(); await loadIdentity(); void friendsDrawer().then(() => friendsDrawer()); });
    return;
  }
  try { await friendsPage(body); } catch (e) { mount(body, html`<div class="drawer-empty"><p>${e instanceof Error ? e.message : String(e)}</p></div>`); }
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
  // Guests who loaded a skin by name see that name and face everywhere too.
  state.head = state.auth.signedIn || (state.settings.guestName && state.skin.skin) ? await headFromSkin(state.skin.skin, 64) : '';
  state.coins = null;
  state.equipped = {};
  if (state.auth.lego.connected) {
    try {
      state.coins = (await lego<{ balance: number }>('GET', '/api/me/credits')).balance;
    } catch { /* offline */ }
    // Equipped cosmetics are shown in 3D on the play screen.
    const [inv, cape] = await Promise.all([
      lego<{ equipped: Record<string, string> }>('GET', '/api/inventory').catch(() => null),
      lego<{ cape: { spec: CapeSpec } | null }>('GET', '/api/me/cape-spec').catch(() => null),
    ]);
    state.equipped = inv?.equipped ?? {};
    state.customCape = cape?.cape?.spec ?? null;
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
  const host = viewEl;
  host.dataset.page = def.id;
  const view = document.createElement('div');
  view.className = `view-enter tr-${state.settings.reducedMotion ? 'none' : state.settings.transition ?? 'slide'}`;
  renderTitlebar();
  host.replaceChildren(view);
  if (def.needsLego && !state.auth.lego.connected) {
    mount(view, html`<div class="gate"><div class="gate-ic">${icon(def.icon, 'lg')}</div><h1>${def.label()}</h1>
      <p class="muted">${state.auth.signedIn ? (state.auth.lego.error ?? 'Diese Seite braucht die Verbindung zum LEGO-Server.') : 'Dafür brauchst du ein Minecraft-Konto. Im Gastmodus kannst du Play, Profile, Mods, Skins, Capes, Designs und VR ausprobieren.'}</p>
      <div class="row center">${state.auth.signedIn ? html`<button class="btn primary" id="reconnect">${icon('refresh', 'sm')} Erneut verbinden</button>` : html`<button class="btn primary" id="signin">${icon('account', 'sm')} ${t('play.signin')}</button>`}</div></div>`);
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

let viewEl: HTMLElement = document.createElement('main');

export function refreshChrome(): void {
  renderSidebar();
  renderTitlebar();
}

/** The separate console window (same page, ?console=1): only the game log. */
async function bootConsole(): Promise<void> {
  [state.settings, state.auth, state.info] = await Promise.all([api.getSettings(), api.authState(), api.info()]);
  retheme();
  document.body.classList.add('console-window');
  document.title = 'LEGO Launcher · Konsole';
  await initConsoleWindow();
  renderConsoleWindow(document.getElementById('nrRoot')!, state.settings.selectedProfile);
}

async function boot(): Promise<void> {
  if (new URLSearchParams(location.search).get('console') === '1') return bootConsole();
  [state.settings, state.auth, state.info] = await Promise.all([api.getSettings(), api.authState(), api.info()]);
  setLegoClientVersions(state.info.legoClientVersions ?? []);
  retheme();
  viewEl = await mountShell(document.getElementById('nrRoot')!);
  renderWindowButtons(await api.windowState());
  api.on('window-state', renderWindowButtons);
  refreshChrome();
  actions.go = (id) => void go(id);
  actions.back = () => { const p = back.pop(); if (p) void go(p, 'back'); };
  actions.fwd = () => { const p = fwd.pop(); if (p) void go(p, 'fwd'); };
  actions.window = (cmd) => api.window(cmd);
  actions.bell = (a) => bellMenu(a);
  actions.friends = () => void friendsDrawer();
  actions.more = (a) => moreMenu(a);
  actions.account = (a) => { if (state.auth.signedIn) accountMenu(a); else guestMenu(a); };
  actions.instances = () => void api.openConsole();
  actions.credits = credits;
  addEventListener('mouseup', (e) => {
    if (e.button === 3) actions.back();
    if (e.button === 4) actions.fwd();
  });
  initGame({ lowPower: backgroundLowPower, refresh: () => void loadIdentity(), autoConsole: () => state.settings.autoConsole });
  gameStore.subscribe(() => {
    const r = gameStore.get().running;
    if (r !== gameRunning) { gameRunning = r; renderTitlebar(); }
  });
  void refreshOnline();
  setInterval(() => void refreshOnline(), 60_000);
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
  if (!state.auth.signedIn && !state.settings.guest && (!state.info.e2e || state.info.e2eWelcome)) welcome();
}

/** Logo click: credits and licence (the UI is based on the NoRisk Client launcher, GPL-3.0). */
function credits(): void {
  const d = dialog(html`<h2>LEGO Launcher v${state.info.version}</h2>
    <p>Freie Software unter der GNU GPL v3. Den Quellcode und alle Lizenzhinweise findest du im Repository.</p>
    <ul class="small muted"><li>Icons, Schriften und Bibliotheken: siehe THIRD_PARTY_ICONS.md und NOTICE.md</li><li>Schrift: Poppins (OFL)</li><li>skinview3d, three.js, React (MIT)</li></ul>
    <div class="row between gap"><button class="btn" id="crSrc">${icon('link', 'sm')} Quellcode &amp; Lizenzen</button><button class="btn primary" id="crOk">OK</button></div>`);
  d.querySelector('#crOk')!.addEventListener('click', () => d.close());
  d.querySelector('#crSrc')!.addEventListener('click', () => void api.openExternal('https://github.com/Luca12r5/oyvey-ported/tree/main/apps/launcher'));
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

// Drop files anywhere: modpacks, mods, resource/shader packs go into profiles, a PNG opens the
// skin preview, a .json is imported as design preset. Pages with their own drop area handle it first.
function installDropImport(): void {
  const hasFiles = (e: DragEvent) => !!e.dataTransfer && [...e.dataTransfer.types].includes('Files');
  let depth = 0;
  addEventListener('dragenter', (e) => { if (hasFiles(e)) { depth++; document.body.classList.add('drop-hover'); } });
  addEventListener('dragleave', (e) => { if (hasFiles(e) && --depth <= 0) { depth = 0; document.body.classList.remove('drop-hover'); } });
  addEventListener('dragover', (e) => { if (hasFiles(e)) e.preventDefault(); });
  addEventListener('drop', (e) => {
    depth = 0;
    document.body.classList.remove('drop-hover');
    if (!hasFiles(e) || e.defaultPrevented) return; // a page's own drop area took it
    e.preventDefault();
    const f = e.dataTransfer!.files[0];
    if (!f) return;
    void (async () => {
      try {
        const kind = classifyDrop(f.name);
        if (kind === 'skin') { if (state.page !== 'skins') { const { openSkinFile } = await import('./pages/skins.ts'); openSkinFile(f); } return; }
        if (kind === 'design') {
          if (f.size > 200_000) throw new Error('Datei zu groß.');
          const p = sanitizePreset({ ...JSON.parse(await f.text()), id: `p-${Date.now().toString(36)}` });
          if (!p) throw new Error('Das ist kein gültiges Design-Preset.');
          state.settings = await api.setSettings({ designPresets: [...(state.settings.designPresets ?? []), p].slice(-30) });
          toast(`Design „${p.name}“ importiert (Optionen → Designs → Presets)`, 'ok');
          return;
        }
        toast(`Importiere ${f.name} …`);
        const r = await api.importDrop(f.name, new Uint8Array(await f.arrayBuffer()));
        if (r.kind === 'modpack') {
          state.settings = await api.setSettings({ selectedProfile: r.profileId });
          toast(`Modpack importiert${r.missing?.length ? ` (fehlt: ${r.missing.join(', ')})` : ''}`, 'ok');
          void go('profiles');
        } else {
          const what = r.kind === 'mod' ? 'Mod' : r.kind === 'shaderpack' ? 'Shaderpaket' : 'Ressourcenpaket';
          toast(`${what} „${r.file}“ in „${r.profileName}“ importiert`, 'ok');
        }
      } catch (err) { errorToast(err); }
    })();
  });
}

installDropImport();
void boot();
