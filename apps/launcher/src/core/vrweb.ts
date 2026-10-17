// Rules for the in-VR web browser: what the address bar turns into, which
// URLs may load, and enforced SafeSearch (the launcher is used by kids too).

export const HOME_URL = 'https://www.google.com/?safe=active&hl=de';

/** Address bar input -> URL. Plain words become a Google search. */
export function toTarget(input: string): string {
  const s = input.trim().slice(0, 2000);
  if (!s) return HOME_URL;
  if (/^https:\/\/\S+$/i.test(s)) return s;
  if (/^http:\/\//i.test(s)) return s.replace(/^http:/i, 'https:');
  if (!/\s/.test(s) && /^[a-z0-9-]+(\.[a-z0-9-]+)+(\/\S*)?$/i.test(s)) return `https://${s}`;
  return `https://www.google.com/search?q=${encodeURIComponent(s)}&safe=active&hl=de`;
}

/** Only https pages (plus about:blank) may load. */
export function allowed(url: string): boolean {
  if (url === 'about:blank') return true;
  try {
    const u = new URL(url);
    return u.protocol === 'https:';
  } catch {
    return false;
  }
}

const GOOGLE = /^(www\.)?google\.[a-z.]{2,8}$/i;

/** Google search URLs without safe=active get it added (null = leave as is). */
export function enforceSafeSearch(url: string): string | null {
  let u: URL;
  try { u = new URL(url); } catch { return null; }
  const isSearch = GOOGLE.test(u.hostname) && (u.pathname === '/search' || u.pathname === '/' || u.pathname.startsWith('/images'));
  const isBing = /(^|\.)bing\.com$/i.test(u.hostname) && u.pathname === '/search';
  if (isSearch && u.searchParams.get('safe') !== 'active') { u.searchParams.set('safe', 'active'); return u.toString(); }
  if (isBing && u.searchParams.get('adlt') !== 'strict') { u.searchParams.set('adlt', 'strict'); return u.toString(); }
  return null;
}

export type BrowserInput =
  | { type: 'mouseDown' | 'mouseUp' | 'mouseMove'; x: number; y: number; button?: 'left' | 'right' | 'middle' }
  | { type: 'mouseWheel'; x: number; y: number; deltaY: number }
  | { type: 'keyDown' | 'keyUp' | 'char'; keyCode: string };

/** Validates input events coming from the renderer before they reach the page. */
export function cleanInput(e: unknown, w: number, h: number): BrowserInput | null {
  if (!e || typeof e !== 'object') return null;
  const o = e as Record<string, unknown>;
  const x = Math.round(Math.max(0, Math.min(w - 1, Number(o.x) || 0)));
  const y = Math.round(Math.max(0, Math.min(h - 1, Number(o.y) || 0)));
  if (o.type === 'mouseDown' || o.type === 'mouseUp' || o.type === 'mouseMove') {
    const button = o.button === 'right' || o.button === 'middle' ? o.button : 'left';
    return { type: o.type, x, y, button };
  }
  if (o.type === 'mouseWheel') return { type: 'mouseWheel', x, y, deltaY: Math.max(-2000, Math.min(2000, Number(o.deltaY) || 0)) };
  if ((o.type === 'keyDown' || o.type === 'keyUp' || o.type === 'char') && typeof o.keyCode === 'string' && o.keyCode.length >= 1 && o.keyCode.length <= 16) {
    return { type: o.type, keyCode: o.keyCode };
  }
  return null;
}
