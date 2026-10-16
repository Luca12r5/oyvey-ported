// Launcher renderer: state, icon rail, title bar (coins + account switcher),
// animated background and page routing.

import type { AppInfo, AuthState } from '../ipc-types.ts';
import type { Settings } from '../core/settings.ts';
import { api, html, mount, toast, errorToast, fmtNum, lego } from './ui.ts';
import { applyTheme, findTheme } from './theme.ts';
import { brickLogo, icon, pixel } from './icons.ts';
import { Background } from './bg.ts';
import { headFromSkin } from './avatar.ts';
import { setLang, t } from './i18n.ts';
import { playPage } from './pages/play.ts';
import { playSplash } from './splash.ts';
import { playIntro3d, webglAvailable } from './intro3d.ts';
import { profilesPage } from './pages/profiles.ts';
import { cosmeticsPage, shopPage } from './pages/cosmetics.ts';
import { friendsPage } from './pages/social.ts';
import { rewardsPage } from './pages/rewards.ts';
import { accountPage, signInFlow } from './pages/account.ts';
import { settingsPage } from './pages/settings.ts';
import { designsPage } from './pages/designs.ts';
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
interface PageDef { id: string; label: () => string; icon: string; page: Page; needsLego?: boolean; staff?: boolean; slot: 'main' | 'bottom' | 'more' }
const PAGES: PageDef[] = [
  { id: 'play', label: () => t('nav.home'), icon: 'home', page: playPage, slot: 'main' },
  { id: 'profiles', label: () => t('nav.profiles'), icon: 'profiles', page: profilesPage, slot: 'main' },
  { id: 'friends', label: () => t('nav.friends'), icon: 'friends', page: friendsPage, needsLego: true, slot: 'main' },
  { id: 'cosmetics', label: () => t('nav.cosmetics'), icon: 'cosmetics', page: cosmeticsPage, needsLego: true, slot: 'main' },
  { id: 'designs', label: () => t('nav.designs'), icon: 'brush', page: (el) => designsPage(el), slot: 'main' },
  { id: 'shop', label: () => t('nav.shop'), icon: 'shop', page: shopPage, needsLego: true, slot: 'more' },
  { id: 'rewards', label: () => t('nav.rewards'), icon: 'rewards', page: rewardsPage, needsLego: true, slot: 'more' },
  { id: 'news', label: () => t('nav.news'), icon: 'news', page: newsPage, slot: 'more' },
  { id: 'feedback', label: () => t('nav.feedback'), icon: 'feedback', page: feedbackPage, needsLego: true, slot: 'more' },
  { id: 'account', label: () => t('nav.account'), icon: 'account', page: accountPage, slot: 'more' },
  { id: 'dev', label: () => t('nav.dev'), icon: 'dev', page: devPage, staff: true, slot: 'more' },
  { id: 'settings', label: () => t('nav.settings'), icon: 'settings', page: settingsPage, slot: 'bottom' },
];

/** Shorter labels for the dock (full names stay in tooltips/aria). */
const SHORT: Record<string, () => string> = { settings: () => (state.settings.language === 'en' ? 'Settings' : 'Optionen'), cosmetics: () => 'Cosmetics' };

let bg: Background | null = null;
let leaveHooks: (() => void)[] = [];

/** Registers cleanup for the current page (runs on the next navigation). */
export function onLeave(fn: () => void): void {
  leaveHooks.push(fn);
}

/** Lower frame rate while Minecraft runs. */
export function backgroundLowPower(on: boolean): void {
  bg?.setLowPower(on);
}

export function retheme(): void {
  const s = state.settings;
  setLang(s.language);
  applyTheme(findTheme(s.themeId, s.customThemes), { reducedMotion: s.reducedMotion, uiScale: s.uiScale });
  if (s.accentColor) document.documentElement.style.setProperty('--accent', s.accentColor);
  bg ??= new Background(document.getElementById('bg') as HTMLCanvasElement, document.getElementById('bg3d') as HTMLCanvasElement);
  bg.configure({ id: s.background, quality: s.backgroundQuality, snow: s.snow, still: s.reducedMotion, fx: s.fx, image: s.bgImage });
  document.body.classList.add('nocturne', `surf-${s.surface}`, `stage-${s.stage}`);
  document.body.classList.toggle('no-vignette', !s.fx.vignette);
  document.body.classList.toggle('no-glow', !s.fx.glow);
}

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
  mount(document.getElementById('sidebar')!, html`<span class="rail-ind" aria-hidden="true"></span>
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
  state.skin = state.auth.signedIn ? await api.skin().catch(() => ({ skin: null, variant: 'classic' as const, cape: null })) : { skin: null, variant: 'classic', cape: null };
  state.head = state.auth.signedIn ? await headFromSkin(state.skin.skin, 64) : '';
  state.coins = null;
  if (state.auth.lego.connected) {
    try {
      state.coins = (await lego<{ balance: number }>('GET', '/api/me/credits')).balance;
    } catch { /* offline */ }
  }
  refreshChrome();
}

export async function go(page: string): Promise<void> {
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
  view.className = 'view-enter';
  host.replaceChildren(view);
  if (def.needsLego && !state.auth.lego.connected) {
    mount(view, html`<div class="card narrow"><h1>${def.label()}</h1><p class="muted">Diese Seite braucht eine Verbindung zum LEGO-Server.</p>
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
}

function startIntro(): Promise<void> {
  const s = state.settings;
  let shown = false;
  try { shown = !!sessionStorage.getItem('intro-shown'); sessionStorage.setItem('intro-shown', '1'); } catch { /* storage blocked */ }
  if (shown || !s.introEnabled || s.reducedMotion || state.info.e2e) return Promise.resolve();
  bg?.stop();
  if (!webglAvailable()) return playSplash();
  return playIntro3d({ sound: s.introSound, quality: s.backgroundQuality, skipLabel: t('intro.skip') }).catch((e) => console.warn('intro failed', e));
}

void boot();
