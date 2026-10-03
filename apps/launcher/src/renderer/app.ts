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
import { profilesPage } from './pages/profiles.ts';
import { modsPage } from './pages/mods.ts';
import { cosmeticsPage, shopPage } from './pages/cosmetics.ts';
import { friendsPage } from './pages/social.ts';
import { rewardsPage } from './pages/rewards.ts';
import { accountPage, signInFlow } from './pages/account.ts';
import { settingsPage } from './pages/settings.ts';
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
interface PageDef { id: string; label: () => string; icon: string; page: Page; needsLego?: boolean; staff?: boolean; bottom?: boolean }
const PAGES: PageDef[] = [
  { id: 'play', label: () => t('nav.play'), icon: 'play', page: playPage },
  { id: 'profiles', label: () => t('nav.profiles'), icon: 'profiles', page: profilesPage },
  { id: 'mods', label: () => t('nav.mods'), icon: 'mods', page: modsPage },
  { id: 'cosmetics', label: () => t('nav.cosmetics'), icon: 'cosmetics', page: cosmeticsPage, needsLego: true },
  { id: 'shop', label: () => t('nav.shop'), icon: 'shop', page: shopPage, needsLego: true },
  { id: 'friends', label: () => t('nav.friends'), icon: 'friends', page: friendsPage, needsLego: true },
  { id: 'rewards', label: () => t('nav.rewards'), icon: 'rewards', page: rewardsPage, needsLego: true },
  { id: 'news', label: () => t('nav.news'), icon: 'news', page: newsPage },
  { id: 'feedback', label: () => t('nav.feedback'), icon: 'feedback', page: feedbackPage, needsLego: true },
  { id: 'account', label: () => t('nav.account'), icon: 'account', page: accountPage, bottom: true },
  { id: 'dev', label: () => t('nav.dev'), icon: 'dev', page: devPage, staff: true, bottom: true },
  { id: 'settings', label: () => t('nav.settings'), icon: 'settings', page: settingsPage, bottom: true },
];

/** Shorter labels for the sidebar (full names stay in tooltips/aria). */
const SHORT: Record<string, () => string> = { rewards: () => 'Rewards', settings: () => (state.settings.language === 'en' ? 'Settings' : 'Optionen'), dev: () => 'Dev', cosmetics: () => 'Cosmetics' };

let bg: Background | null = null;
let leaveHooks: (() => void)[] = [];

/** Registers cleanup for the current page (runs on the next navigation). */
export function onLeave(fn: () => void): void {
  leaveHooks.push(fn);
}

export function retheme(): void {
  const s = state.settings;
  setLang(s.language);
  applyTheme(findTheme(s.themeId, s.customThemes), { reducedMotion: s.reducedMotion, uiScale: s.uiScale });
  if (s.accentColor) document.documentElement.style.setProperty('--accent', s.accentColor);
  bg ??= new Background(document.getElementById('bg') as HTMLCanvasElement);
  bg.configure({ id: s.background, quality: s.backgroundQuality, snow: s.snow, still: s.reducedMotion });
}

function renderSidebar(): void {
  const staff = state.auth.lego.roles.length > 0;
  const item = (p: PageDef) => html`<button class="nav-item ${state.page === p.id ? 'active' : ''}" data-page="${p.id}" aria-label="${p.label()}" title="${p.label()}">${icon(p.icon)}<span class="lbl">${SHORT[p.id]?.() ?? p.label()}</span></button>`;
  const visible = PAGES.filter((p) => !p.staff || staff);
  mount(document.getElementById('sidebar')!, html`${visible.filter((p) => !p.bottom).map(item)}<div class="spacer"></div>${visible.filter((p) => p.bottom).map(item)}`);
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
  mount(document.getElementById('win')!, html`<button class="tb-btn" data-win="minimize" aria-label="Minimieren">${icon('minimize', 'sm')}</button><button class="tb-btn" data-win="maximize" aria-label="Maximieren">${icon('maximize', 'sm')}</button><button class="tb-btn close" data-win="close" aria-label="Schließen">${icon('close', 'sm')}</button>`);
  refreshChrome();
  document.getElementById('sidebar')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-page]');
    if (b) void go(b.dataset.page!);
  });
  document.querySelectorAll<HTMLButtonElement>('[data-win]').forEach((b) => b.addEventListener('click', () => api.window(b.dataset.win as 'minimize')));
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
  await loadIdentity();
  await go('play');
}

void boot();
