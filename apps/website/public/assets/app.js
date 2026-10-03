// Website entry: session bootstrap, navigation and a small history router.

import { api, applyVars, esc, html, mount, state, toast, errorToast } from './core.js';
import * as pub from './pages/public.js';
import * as account from './pages/account.js';
import * as social from './pages/social.js';
import * as shop from './pages/shop.js';
import * as feedback from './pages/feedback.js';
import * as dev from './pages/dev.js';

const NAV = [
  ['/', 'Home'], ['/cosmetics', 'Cosmetics'], ['/shop', 'Shop'], ['/battle-pass', 'Battle Pass'],
  ['/leaderboards', 'Ranglisten'], ['/news', 'News'], ['/feedback', 'Feedback'],
];

const ROUTES = [
  [/^\/$/, pub.home], [/^\/features$/, pub.features], [/^\/download$/, pub.download], [/^\/news$/, pub.news],
  [/^\/changelog$/, pub.changelog], [/^\/status$/, pub.status], [/^\/legal\/(imprint|privacy|terms)$/, pub.legal],
  [/^\/leaderboards$/, pub.leaderboards], [/^\/support$/, pub.support],
  [/^\/cosmetics$/, shop.cosmetics], [/^\/shop$/, shop.shop], [/^\/battle-pass$/, shop.battlePass], [/^\/rewards$/, shop.rewards],
  [/^\/login$/, account.login], [/^\/account$/, account.settings], [/^\/stats$/, account.stats], [/^\/u\/([A-Za-z0-9_]{3,16})$/, account.profile],
  [/^\/friends$/, social.friends], [/^\/messages\/([A-Za-z0-9_-]+)$/, social.chat],
  [/^\/feedback$/, feedback.board], [/^\/feedback\/new$/, feedback.create], [/^\/feedback\/(\d+)$/, feedback.detail],
  [/^\/dev$/, dev.dashboard], [/^\/dev\/users\/([A-Za-z0-9_-]+)$/, dev.user],
];

const THEME_KEY = 'lego.theme';

export async function applyTheme(id) {
  try {
    if (!state.themes) state.themes = (await api('GET', '/api/public/themes')).themes;
    const t = state.themes.find((x) => x.id === id) ?? state.themes[0];
    const r = document.documentElement.style;
    const c = t.colors;
    for (const [k, v] of Object.entries({ bg: c.bg, bg2: c.bg2, surface: c.surface, 'surface-hover': c.surfaceHover, border: c.border, text: c.text, muted: c.textMuted, accent: c.accent, accent2: c.accent2, success: c.success, warning: c.warning, danger: c.danger })) r.setProperty(`--${k}`, v);
    r.setProperty('--radius', `${t.radius}px`);
    r.setProperty('--blur', `${t.blur}px`);
    r.setProperty('--surface-alpha', String(t.surfaceAlpha));
    document.documentElement.dataset.mode = t.mode;
    try { localStorage.setItem(THEME_KEY, t.id); } catch { /* storage may be blocked */ }
  } catch { /* keep default theme */ }
}

function renderChrome() {
  const nav = document.getElementById('nav');
  const path = location.pathname;
  const links = [...NAV];
  if (state.user) links.splice(4, 0, ['/friends', 'Freunde'], ['/rewards', 'Belohnungen']);
  if (state.user?.roles?.length) links.push(['/dev', 'Dashboard']);
  mount(nav, html`${links.map(([href, label]) => html`<a href="${href}" data-link class="${(href === '/' ? path === '/' : path.startsWith(href)) ? 'active' : ''}">${label}</a>`)}`);
  const acc = document.getElementById('account');
  if (state.user) {
    mount(acc, html`<a class="btn small primary nav-download" href="/download" data-link>Download</a><a class="btn small" href="/u/${state.user.name}" data-link>${state.user.name}</a><a class="btn small" href="/account" data-link title="Konto" aria-label="Konto">⚙</a>`);
  } else {
    mount(acc, html`<a class="btn small nav-download" href="/download" data-link>Download</a><a class="btn small primary" href="/login" data-link>Anmelden</a>`);
  }
}

export function navigate(path, replace = false) {
  if (replace) history.replaceState(null, '', path);
  else history.pushState(null, '', path);
  void render();
}

async function render() {
  renderChrome();
  document.getElementById('nav').classList.remove('open');
  const app = document.getElementById('app');
  const path = location.pathname.replace(/\/+$/, '') || '/';
  for (const [re, page] of ROUTES) {
    const m = path.match(re);
    if (m) {
      // Each render gets a fresh container so listeners never leak between pages.
      const view = document.createElement('div');
      view.innerHTML = '<div class="muted">Lädt…</div>';
      app.replaceChildren(view);
      try {
        await page(view, ...m.slice(1));
      } catch (e) {
        if (e?.status === 401) {
          mount(view, html`<div class="card center stack"><h2>Anmeldung erforderlich</h2><p class="muted">Bitte melde dich mit deinem LEGO-Launcher-Code an.</p><a class="btn primary" href="/login" data-link>Anmelden</a></div>`);
        } else {
          mount(view, html`<div class="card"><h2>Fehler</h2><p>${e?.message ?? String(e)}</p></div>`);
        }
      }
      document.title = `${document.querySelector('#app h1')?.textContent ?? 'LEGO Launcher'} · LEGO Launcher`;
      app.focus({ preventScroll: true });
      window.scrollTo(0, 0);
      return;
    }
  }
  mount(app, html`<div class="card center stack"><h1>404</h1><p class="muted">Diese Seite gibt es nicht.</p><a class="btn" href="/" data-link>Zur Startseite</a></div>`);
}

export async function refreshSession() {
  try {
    const s = await api('GET', '/api/auth/session');
    state.user = s.user;
    state.csrf = s.csrf;
  } catch {
    state.user = null;
    state.csrf = null;
  }
}

document.addEventListener('click', (e) => {
  const a = e.target.closest('a[data-link]');
  if (a && a.origin === location.origin && !e.metaKey && !e.ctrlKey && !e.shiftKey && e.button === 0) {
    e.preventDefault();
    navigate(a.pathname + a.search);
  }
});
window.addEventListener('popstate', () => void render());
document.getElementById('menuToggle').addEventListener('click', (e) => {
  const nav = document.getElementById('nav');
  nav.classList.toggle('open');
  e.currentTarget.setAttribute('aria-expanded', String(nav.classList.contains('open')));
});

(async () => {
  let theme = null;
  try { theme = localStorage.getItem(THEME_KEY); } catch { /* ignore */ }
  await Promise.all([applyTheme(theme ?? 'lego-graphite'), refreshSession()]);
  await render();
  if (state.user) connectEvents();
})();

function connectEvents() {
  const es = new EventSource('/api/events');
  es.addEventListener('message', (e) => {
    const m = JSON.parse(e.data);
    toast(`Neue Nachricht von ${m.fromName}`);
  });
  es.addEventListener('friend_request', (e) => toast(`Freundschaftsanfrage von ${JSON.parse(e.data).from.name}`));
  es.addEventListener('gift', (e) => toast(`Geschenk von ${JSON.parse(e.data).from.name}!`, 'ok'));
  es.addEventListener('credits', (e) => toast(`Coins aktualisiert: ${JSON.parse(e.data).balance}`, 'ok'));
  es.addEventListener('feedback_update', () => toast('Dein Feedback wurde aktualisiert'));
  es.onerror = () => { /* EventSource reconnects automatically */ };
}

export { esc, applyVars, errorToast };
