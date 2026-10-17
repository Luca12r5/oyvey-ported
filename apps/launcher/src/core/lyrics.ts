// Synced lyrics from LRCLIB (https://lrclib.net), the same source and the same
// matching rules as the in-game Spotify HUD (dev.spotifyhud.Lyrics).

import type { FetchLike } from './net.ts';

export type LyricsStatus = 'none' | 'loading' | 'synced' | 'plain' | 'not_found' | 'instrumental' | 'error';

export interface LyricLine {
  timeMs: number;
  text: string;
  words: string[];
  wordTimes: number[];
  exactWords: boolean;
}

export interface LyricsResult {
  status: LyricsStatus;
  lines: LyricLine[];
}

const TAG = /\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]/y;
const WORD_TAG = /<(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?>/g;
const OFFSET = /\[offset:\s*([+-]?\d+)\s*]/i;

function frac(f: string | undefined): number {
  if (!f) return 0;
  return f.length === 1 ? Number(f) * 100 : f.length === 2 ? Number(f) * 10 : Number(f);
}

const tagMs = (m: string, s: string, f?: string) => Number(m) * 60000 + Number(s) * 1000 + frac(f);

function addWords(words: string[], times: number[], text: string, t: number): void {
  let first = true;
  for (const w of text.trim().split(/\s+/)) {
    if (!w) continue;
    words.push(w);
    times.push(first ? t : t + 1);
    first = false;
  }
}

/** Parses LRC text (line tags, optional enhanced word tags, [offset:]). Sorted by time. */
export function parseLrc(src: string): LyricLine[] {
  const om = OFFSET.exec(src);
  const offset = om ? Number(om[1]) || 0 : 0;
  const out: LyricLine[] = [];
  for (const raw of src.split(/\r?\n/)) {
    const stamps: number[] = [];
    let pos = 0;
    TAG.lastIndex = 0;
    for (;;) {
      TAG.lastIndex = pos;
      const m = TAG.exec(raw);
      if (!m) break;
      stamps.push(tagMs(m[1]!, m[2]!, m[3]));
      pos = TAG.lastIndex;
    }
    if (!stamps.length) continue;
    const rest = raw.slice(pos);
    const words: string[] = [];
    const times: number[] = [];
    let last = -1;
    let t = 0;
    for (const m of rest.matchAll(WORD_TAG)) {
      if (last >= 0) addWords(words, times, rest.slice(last, m.index), t);
      t = tagMs(m[1]!, m[2]!, m[3]) - offset;
      last = m.index! + m[0].length;
    }
    if (last >= 0) addWords(words, times, rest.slice(last), t);
    const text = rest.replace(WORD_TAG, '').replace(/\s+/g, ' ').trim();
    for (const s of stamps) {
      const line: LyricLine = { timeMs: Math.max(0, s - offset), text, words: [], wordTimes: [], exactWords: false };
      if (words.length && stamps.length === 1) {
        line.words = words;
        line.wordTimes = times.map((x) => Math.max(0, x));
        line.exactWords = true;
      }
      out.push(line);
    }
  }
  out.sort((a, b) => a.timeMs - b.timeMs);
  estimateWords(out);
  return out;
}

/** Spreads word timings over a line when the LRC only has line timings. */
export function estimateWords(lines: LyricLine[]): void {
  lines.forEach((l, i) => {
    if (l.exactWords) return;
    const words = l.text ? l.text.split(/\s+/) : [];
    l.words = words;
    l.wordTimes = new Array(words.length).fill(0);
    if (!words.length) return;
    const end = i + 1 < lines.length ? lines[i + 1]!.timeMs : l.timeMs + 4000;
    const span = Math.max(300, end - l.timeMs);
    let dur = Math.min(Math.floor(span * 0.92), 450 * words.length + 900);
    dur = Math.max(Math.min(span, 250 * words.length), dur);
    const weights = words.map((w) => 1.5 + w.length);
    const total = weights.reduce((a, b) => a + b, 0);
    let acc = 0;
    words.forEach((_w, k) => {
      l.wordTimes[k] = l.timeMs + Math.round((dur * acc) / total);
      acc += weights[k]!;
    });
  });
}

/** Index of the line active at `ms`, or -1 before the first line. */
export function indexAt(lines: LyricLine[], ms: number): number {
  let lo = 0, hi = lines.length - 1, r = -1;
  while (lo <= hi) {
    const mid = (lo + hi) >>> 1;
    if (lines[mid]!.timeMs <= ms) { r = mid; lo = mid + 1; } else hi = mid - 1;
  }
  return r;
}

export function wordAt(line: LyricLine, ms: number): number {
  let r = -1;
  for (let i = 0; i < line.wordTimes.length && line.wordTimes[i]! <= ms; i++) r = i;
  return r;
}

export function firstArtist(a: string): string {
  let s = a ?? '';
  for (const sep of [', ', ' & ', ' feat. ', ' feat ', ' ft. ', ' x ', ';', ' / ']) {
    const i = s.toLowerCase().indexOf(sep);
    if (i > 0) s = s.slice(0, i);
  }
  return s.trim();
}

export function cleanTitle(t: string): string {
  let s = t.replace(/\s*[([](feat\.?|ft\.?|with) [^)\]]*[)\]]/gi, '');
  s = s.replace(/\s+-\s+.*(remaster|version|edit|live|mono|stereo|mix).*$/i, '');
  return s.trim() || t;
}

interface Record { syncedLyrics?: string | null; plainLyrics?: string | null; instrumental?: boolean; duration?: number }

function fromRecord(r: Record | null): LyricsResult | null {
  if (!r) return null;
  if (r.instrumental) return { status: 'instrumental', lines: [] };
  if (r.syncedLyrics && r.syncedLyrics.trim()) {
    const lines = parseLrc(r.syncedLyrics);
    if (lines.length) return { status: 'synced', lines };
  }
  if (r.plainLyrics && r.plainLyrics.trim()) {
    return { status: 'plain', lines: r.plainLyrics.split(/\r?\n/).filter((l) => l.trim()).map((l) => ({ timeMs: -1, text: l.trim(), words: [], wordTimes: [], exactWords: false })) };
  }
  return null;
}

/** Best search hit: closest duration, synced lyrics preferred. */
export function pick(list: unknown, durMs: number): Record | null {
  if (!Array.isArray(list)) return null;
  let best: Record | null = null;
  let score = Infinity;
  for (const r of list as Record[]) {
    if (!r || (r.syncedLyrics == null && !r.instrumental && r.plainLyrics == null)) continue;
    const d = (r.duration ?? 0) * 1000;
    const diff = durMs > 0 && d > 0 ? Math.abs(d - durMs) : 5000;
    if (durMs > 0 && diff > 15000) continue;
    const s = diff + (r.syncedLyrics != null ? 0 : 60000) + (r.instrumental ? 30000 : 0);
    if (s < score) { score = s; best = r; }
  }
  return best;
}

const UA = 'LEGO-Launcher/1.0 (Spotify HUD; https://github.com/Luca12r5/oyvey-ported)';

async function get(f: FetchLike, url: string): Promise<{ code: number; body: unknown }> {
  try {
    const r = await f(url, { headers: { 'User-Agent': UA, Accept: 'application/json' }, signal: AbortSignal.timeout(10_000) });
    return { code: r.status, body: r.ok ? await r.json() : null };
  } catch {
    return { code: -1, body: null };
  }
}

export async function fetchLyrics(f: FetchLike, title: string, artist: string, durMs: number): Promise<LyricsResult> {
  const a1 = firstArtist(artist);
  const t = cleanTitle(title);
  const enc = encodeURIComponent;
  let last = { code: 0, body: null as unknown };
  for (const a of a1 === artist ? [artist] : [artist, a1]) {
    let url = `https://lrclib.net/api/get?track_name=${enc(t)}&artist_name=${enc(a)}`;
    if (durMs > 0) url += `&duration=${Math.round(durMs / 1000)}`;
    last = await get(f, url);
    if (last.code === 200) {
      const r = fromRecord(last.body as Record);
      if (r && (r.status === 'synced' || r.status === 'instrumental')) return r;
    }
  }
  last = await get(f, `https://lrclib.net/api/search?track_name=${enc(t)}&artist_name=${enc(a1)}`);
  let hit = last.code === 200 ? pick(last.body, durMs) : null;
  if (!hit) {
    last = await get(f, `https://lrclib.net/api/search?q=${enc(`${t} ${a1}`)}`);
    hit = last.code === 200 ? pick(last.body, durMs) : null;
  }
  const r = fromRecord(hit);
  if (r) return r;
  return last.code < 500 && last.code !== 429 && last.code !== -1 ? { status: 'not_found', lines: [] } : { status: 'error', lines: [] };
}
