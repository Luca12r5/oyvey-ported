// Renderer helpers: escaping templates, toasts, dialogs. Same approach as the
// website: no inline styles (CSP), colours applied through CSSOM.

import type { LauncherApi } from '../ipc-types.ts';

declare global {
  interface Window { lego: LauncherApi }
}
export const api = window.lego;

export interface Raw { __raw: string }
type Val = string | number | boolean | null | undefined | Raw | Val[];

export function esc(v: unknown): string {
  return String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]!);
}

function render(v: Val): string {
  if (Array.isArray(v)) return v.map(render).join('');
  if (v && typeof v === 'object' && '__raw' in v) return v.__raw;
  if (v === false || v === null || v === undefined) return '';
  return esc(v);
}

export function html(strings: TemplateStringsArray, ...values: Val[]): Raw {
  let out = '';
  strings.forEach((s, i) => {
    out += s;
    if (i < values.length) out += render(values[i]!);
  });
  return { __raw: out };
}

export function mount(el: Element, tpl: Raw): void {
  el.innerHTML = tpl.__raw;
  applyVars(el);
}

export function applyVars(root: Element): void {
  root.querySelectorAll<HTMLElement>('[data-c]').forEach((e) => e.style.setProperty('--c', e.dataset.c!));
  root.querySelectorAll<HTMLElement>('[data-fc]').forEach((e) => e.style.setProperty('--fc', e.dataset.fc!));
  root.querySelectorAll<HTMLElement>('[data-p]').forEach((e) => e.style.setProperty('--p', `${Math.max(0, Math.min(100, Number(e.dataset.p)))}%`));
  root.querySelectorAll<HTMLElement>('[data-color]').forEach((e) => { e.style.color = e.dataset.color!; });
}

export function toast(msg: string, kind: 'info' | 'ok' | 'error' = 'info'): void {
  const t = document.createElement('div');
  t.className = `toast ${kind}`;
  t.textContent = msg;
  document.getElementById('toasts')!.append(t);
  setTimeout(() => t.remove(), 5000);
}

export function errorToast(e: unknown): void {
  const m = e instanceof Error ? e.message.replace(/^Error invoking remote method '[^']+': (Error: )?/, '') : String(e);
  toast(m, 'error');
}

export function dialog(content: Raw): HTMLDialogElement {
  const d = document.getElementById('dialog') as HTMLDialogElement;
  mount(d, content);
  if (!d.open) d.showModal();
  return d;
}

export function confirm(title: string, message: string, ok = 'OK', danger = false): Promise<boolean> {
  const d = dialog(html`<form method="dialog"><h2>${title}</h2><p>${message}</p><div class="row between"><button class="btn" value="cancel">Abbrechen</button><button class="btn ${danger ? 'danger' : 'primary'}" value="ok">${ok}</button></div></form>`);
  return new Promise((resolve) => { d.onclose = () => resolve(d.returnValue === 'ok'); });
}

export function prompt(title: string, label: string, value = ''): Promise<string | null> {
  const d = dialog(html`<form method="dialog"><h2>${title}</h2><label>${label}</label><input name="v" value="${value}" autofocus><div class="row between gap"><button class="btn" value="cancel">Abbrechen</button><button class="btn primary" value="ok">OK</button></div></form>`);
  return new Promise((resolve) => { d.onclose = () => resolve(d.returnValue === 'ok' ? (d.querySelector<HTMLInputElement>('[name=v]')!.value) : null); });
}

export const fmtDate = (ms: number | null | undefined) => (ms ? new Date(ms).toLocaleString('de-DE', { dateStyle: 'medium', timeStyle: 'short' }) : '–');
export const fmtNum = (n: number | null | undefined) => Number(n ?? 0).toLocaleString('de-DE');
export const idem = () => crypto.randomUUID();

const SLOT_ICONS: Record<string, string> = { cape: '🧣', wings: '🪽', hat: '🎩', face: '🕶️', back: '🎒', aura: '✨', pet: '🐾', vehicle: '🚗', nametag: '🏷️' };
export const slotIcon = (s: string) => SLOT_ICONS[s] ?? '⬜';

/** Calls the LEGO backend through the main process; shows a hint when offline. */
export async function lego<T>(method: string, path: string, body?: unknown): Promise<T> {
  return api.lego<T>(method, path, body);
}
