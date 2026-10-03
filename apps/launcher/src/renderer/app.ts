// Launcher renderer: state, sidebar navigation, title bar and page routing.

import type { AppInfo, AuthState } from '../ipc-types.ts';
import type { Settings } from '../core/settings.ts';
import { api, html, mount, toast, errorToast } from './ui.ts';
import { applyTheme, findTheme } from './theme.ts';
import { playPage } from './pages/play.ts';
import { profilesPage } from './pages/profiles.ts';
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
}

export const state = {} as State;

type Page = (el: HTMLElement) => Promise<void> | void;
const PAGES: { id: string; label: string; icon: string; page: Page; needsLego?: boolean; staff?: boolean; bottom?: boolean }[] = [
  { id: 'play', label: 'Play', icon: '▶', page: playPage },
  { id: 'profiles', label: 'Profile', icon: '🗂', page: profilesPage },
  { id: 'cosmetics', label: 'Cosmetics', icon: '🧣', page: cosmeticsPage, needsLego: true },
  { id: 'shop', label: 'Shop', icon: '🛒', page: shopPage, needsLego: true },
  { id: 'friends', label: 'Freunde', icon: '👥', page: friendsPage, needsLego: true },
  { id: 'rewards', label: 'Rewards', icon: '🏆', page: rewardsPage, needsLego: true },
  { id: 'news', label: 'News', icon: '📰', page: newsPage },
  { id: 'feedback', label: 'Feedback', icon: '💬', page: feedbackPage, needsLego: true },
  { id: 'account', label: 'Konto', icon: '👤', page: accountPage, bottom: true },
  { id: 'dev', label: 'Dev', icon: '🛠', page: devPage, staff: true, bottom: true },
  { id: 'settings', label: 'Settings', icon: '⚙', page: settingsPage, bottom: true },
];

export function retheme(): void {
  applyTheme(findTheme(state.settings.themeId, state.settings.customThemes), { reducedMotion: state.settings.reducedMotion, uiScale: state.settings.uiScale });
}

function renderSidebar(): void {
  const staff = state.auth.lego.roles.length > 0;
  const item = (p: (typeof PAGES)[number]) => html`<button class="nav-item ${state.page === p.id ? 'active' : ''}" data-page="${p.id}" title="${p.label}"><span class="ic">${p.icon}</span>${p.label}</button>`;
  const visible = PAGES.filter((p) => !p.staff || staff);
  mount(document.getElementById('sidebar')!, html`${visible.filter((p) => !p.bottom).map(item)}<div class="spacer"></div>${visible.filter((p) => p.bottom).map(item)}`);
}

function renderTitlebar(): void {
  const chip = document.getElementById('tbAccount')!;
  chip.textContent = state.auth.signedIn ? `${state.auth.name}${state.auth.lego.connected ? '' : ' (offline)'}` : 'Anmelden';
  document.getElementById('tbVersion')!.textContent = `v${state.info.version}`;
}

export async function go(page: string): Promise<void> {
  state.page = page;
  renderSidebar();
  const def = PAGES.find((p) => p.id === page) ?? PAGES[0]!;
  const host = document.getElementById('view')!;
  const view = document.createElement('div');
  view.className = 'view-enter';
  host.replaceChildren(view);
  if (def.needsLego && !state.auth.lego.connected) {
    mount(view, html`<div class="card"><h1>${def.label}</h1><p class="muted">Diese Seite braucht eine Verbindung zum LEGO-Server.</p>
      <p>${state.auth.signedIn ? (state.auth.lego.error ?? 'Nicht verbunden.') : 'Melde dich zuerst mit deinem Microsoft-Konto an.'}</p>
      <div class="row">${state.auth.signedIn ? html`<button class="btn primary" id="reconnect">Erneut verbinden</button>` : html`<button class="btn primary" id="signin">Anmelden</button>`}</div></div>`);
    view.querySelector('#reconnect')?.addEventListener('click', async () => { state.auth = await api.reconnectLego(); refreshChrome(); void go(page); });
    view.querySelector('#signin')?.addEventListener('click', () => void signInFlow());
    return;
  }
  try {
    await def.page(view);
  } catch (e) {
    mount(view, html`<div class="card"><h1>${def.label}</h1><p>Fehler: ${e instanceof Error ? e.message : String(e)}</p></div>`);
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
  refreshChrome();
  document.getElementById('sidebar')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-page]');
    if (b) void go(b.dataset.page!);
  });
  document.querySelectorAll<HTMLButtonElement>('[data-win]').forEach((b) => b.addEventListener('click', () => api.window(b.dataset.win as 'minimize')));
  document.getElementById('tbAccount')!.addEventListener('click', () => (state.auth.signedIn ? void go('account') : void signInFlow()));
  api.on('auth', (a: AuthState & { error?: string }) => {
    state.auth = a;
    if (a.error) errorToast(new Error(a.error));
    refreshChrome();
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
  await go('play');
}

void boot();
