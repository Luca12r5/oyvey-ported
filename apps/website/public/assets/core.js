// Shared helpers for the website: API client, escaping, toasts, dialogs.
// All dynamic text goes through esc(); colours are applied via CSSOM (allowed
// by the strict CSP) instead of inline style attributes.

export const state = { user: null, csrf: null, catalog: null, themes: null };

export function esc(v) {
  return String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

/** Tagged template that escapes interpolations; use raw() to opt out. */
export function html(strings, ...values) {
  let out = '';
  strings.forEach((s, i) => {
    out += s;
    if (i < values.length) {
      const v = values[i];
      if (Array.isArray(v)) out += v.map((x) => (x && x.__raw !== undefined ? x.__raw : esc(x))).join('');
      else out += v && v.__raw !== undefined ? v.__raw : esc(v);
    }
  });
  return { __raw: out };
}
export const raw = (s) => ({ __raw: s });

export function mount(el, tpl) {
  el.innerHTML = tpl.__raw;
  applyVars(el);
}

/** data-c="#hex" -> --c, data-fc -> --fc, data-p="40" -> --p:40% */
export function applyVars(root) {
  root.querySelectorAll('[data-c]').forEach((e) => e.style.setProperty('--c', e.dataset.c));
  root.querySelectorAll('[data-fc]').forEach((e) => e.style.setProperty('--fc', e.dataset.fc));
  root.querySelectorAll('[data-p]').forEach((e) => e.style.setProperty('--p', `${Math.max(0, Math.min(100, Number(e.dataset.p)))}%`));
  root.querySelectorAll('[data-color]').forEach((e) => { e.style.color = e.dataset.color; });
}

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `HTTP ${status}`);
    this.status = status;
    this.code = body?.error;
    this.body = body;
  }
}

export async function api(method, path, body) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (method !== 'GET' && state.csrf) headers['X-CSRF-Token'] = state.csrf;
  const res = await fetch(path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined, credentials: 'same-origin' });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}

export function toast(msg, kind = 'info') {
  const box = document.getElementById('toasts');
  const t = document.createElement('div');
  t.className = `toast ${kind}`;
  t.textContent = msg;
  box.append(t);
  setTimeout(() => t.remove(), 4500);
}

export function errorToast(e) {
  toast(e instanceof ApiError ? e.message : String(e), 'error');
}

/** Promise-based confirm dialog with optional typed confirmation. */
export function confirmDialog(title, message, { confirmText = 'Bestätigen', danger = false, requireTyping = null } = {}) {
  const d = document.getElementById('dialog');
  mount(d, html`<form method="dialog" class="stack">
    <h2>${title}</h2><p>${message}</p>
    ${requireTyping ? html`<div class="field"><label>Tippe <code>${requireTyping}</code> zur Bestätigung</label><input name="typed" autocomplete="off"></div>` : ''}
    <div class="row between"><button class="btn" value="cancel">Abbrechen</button><button class="btn ${danger ? 'danger' : 'primary'}" value="ok">${confirmText}</button></div>
  </form>`);
  return new Promise((resolve) => {
    d.onclose = () => {
      const typed = d.querySelector('[name=typed]')?.value ?? '';
      resolve(d.returnValue === 'ok' && (!requireTyping || typed === requireTyping));
    };
    d.showModal();
  });
}

export function fmtDate(ms) {
  if (!ms) return '–';
  return new Date(ms).toLocaleString('de-DE', { dateStyle: 'medium', timeStyle: 'short' });
}

export function fmtNum(n) {
  return Number(n ?? 0).toLocaleString('de-DE');
}

export function avatar(name, size = '') {
  let hash = 0;
  for (const ch of String(name)) hash = (hash * 31 + ch.charCodeAt(0)) | 0;
  const hue = Math.abs(hash) % 360;
  const c = `hsl(${hue} 60% 45%)`;
  return html`<span class="avatar ${size}" data-c="${c}">${String(name).slice(0, 1).toUpperCase()}</span>`;
}

export function idemKey() {
  return crypto.randomUUID();
}

export async function loadCatalog() {
  if (!state.catalog) state.catalog = await api('GET', '/api/public/catalog');
  return state.catalog;
}

export function rarityBadge(rarity) {
  const info = state.catalog?.rarities?.[rarity];
  return html`<span class="badge c" data-c="${info?.color ?? '#888'}">${info?.label?.de ?? rarity}</span>`;
}

const SLOT_ICONS = { cape: '🧣', wings: '🪽', hat: '🎩', face: '🕶️', back: '🎒', aura: '✨', pet: '🐾', vehicle: '🚗', nametag: '🏷️' };
export const slotIcon = (s) => SLOT_ICONS[s] ?? '⬜';

/** Name tag preview: per-character colours like the in-game renderer. */
export function tagPreview(style, text) {
  const cols = style.text.colors;
  const chars = [...text];
  const span = chars.map((ch, i) => {
    const t = chars.length <= 1 ? 0 : i / (chars.length - 1);
    const seg = t * (cols.length - 1);
    const k = Math.min(cols.length - 2, Math.floor(seg));
    const color = cols.length === 1 ? cols[0] : lerp(cols[k], cols[k + 1], seg - k);
    return html`<span data-color="${color}">${ch}</span>`;
  });
  const cls = `tagpreview frame-${style.frame.style}`;
  const weight = style.text.bold ? 'b' : '';
  return html`<span class="${cls}" data-fc="${style.frame.color}">${style.icon ? html`<span data-color="${style.frame.color}">${style.icon}</span>` : ''}${weight ? html`<b>${span}</b>` : span}</span>`;
}

function lerp(a, b, t) {
  const pa = parseInt(a.slice(1), 16), pb = parseInt(b.slice(1), 16);
  const ch = (s) => Math.round(((pa >> s) & 255) + ((((pb >> s) & 255) - ((pa >> s) & 255)) * t));
  return `#${((ch(16) << 16) | (ch(8) << 8) | ch(0)).toString(16).padStart(6, '0')}`;
}

export function hasPerm(p) {
  const roles = state.user?.roles ?? [];
  const table = {
    admin: ['*'],
    developer: ['users.view', 'credits.grant', 'feedback.manage', 'news.manage', 'audit.view', 'promo.manage'],
    moderator: ['users.view', 'users.ban', 'reports.manage', 'feedback.manage'],
    support: ['users.view', 'feedback.manage'],
  };
  return roles.some((r) => (table[r] ?? []).includes('*') || (table[r] ?? []).includes(p));
}
