// Spotify HUD in the launcher: a 1:1 port of the in-game HUD
// (lego-client dev.spotifyhud.HudPainter / Theme / HudLayout / HudRenderer).
// Design units: the panel is 207 x 70, the lyrics strip 207 x 40, 3 units apart.
// Now-playing data comes from the Windows media session (main/spotify.ts),
// lyrics from LRCLIB (core/lyrics.ts).

import { api } from './ui.ts';
import type { SpotifyState } from '../main/spotify.ts';
import type { LyricLine, LyricsResult } from '../core/lyrics.ts';
import type { SpotifyHudSettings } from '../core/settings.ts';

type Region = 'ART' | 'PROGRESS' | 'SPEAKER' | 'VOLUME' | 'PREV' | 'PLAY' | 'NEXT' | 'REPEAT' | 'SHUFFLE' | 'LIKE' | 'SETTINGS' | 'PANEL';

const W = 207, H = 70, LH = 40, GAP = 3, R = 15;
const rgba = (r: number, g: number, b: number, a = 255) => `rgba(${r},${g},${b},${a / 255})`;
const PANEL_EDGE = rgba(255, 255, 255, 13);
const PANEL_EDGE_HOVER = rgba(255, 255, 255, 60);
const TITLE = [234, 236, 235] as const;
const ARTIST = [179, 179, 179] as const;
const TIME = rgba(167, 167, 167);
const TRACK = rgba(62, 63, 63, 235);
const FILL = rgba(247, 249, 247);
const GREEN: RGB = [30, 215, 96];
const ICON = rgba(248, 250, 250);
const SPEAKER = rgba(255, 255, 255, 170);
const HOVER_BG = rgba(255, 255, 255, 26);
const ART_EMPTY = rgba(40, 40, 40);
const NOTE = rgba(127, 127, 127);
type RGB = readonly [number, number, number];
const css = (c: RGB, a = 1) => `rgba(${c[0]},${c[1]},${c[2]},${a})`;

const FAMILY = '"Segoe UI", "Helvetica Neue", Arial, "Liberation Sans", "DejaVu Sans", sans-serif';
const font = (bold: boolean, size: number) => (bold ? `600 ${size}px "Segoe UI Semibold", ${FAMILY}` : `400 ${size}px ${FAMILY}`);

// ------------------------------------------------------------------ state

let st: SpotifyState | null = null;
let art: HTMLImageElement | null = null;
let artKey = '';
let lyrics: LyricsResult = { status: 'none', lines: [] };
let lyricsFor = '';
let hover: Region | null = null;
let lyrHover = false;
let progressPreview = -1;
let volumePreview = -1;
let lastLyricKey = '';
let lastLyricIndex = -1;
let lyricChangedAt = 0;
let root: HTMLDivElement | null = null;
let hudC: HTMLCanvasElement;
let lyrC: HTMLCanvasElement;
let cfg: SpotifyHudSettings;
let raf = 0;
let openSettings: () => void = () => {};
const accentCache = new WeakMap<HTMLImageElement, RGB>();

function progress(s: SpotifyState): number {
  const p = s.playing ? s.posMs + (Date.now() - s.at) : s.posMs;
  return Math.max(0, s.durMs > 0 ? Math.min(p, s.durMs) : p);
}

function fmt(ms: number): string {
  const t = Math.max(0, Math.floor(ms / 1000));
  return `${Math.floor(t / 60)}:${String(t % 60).padStart(2, '0')}`;
}

// ------------------------------------------------------------------ theme (dev.spotifyhud.Theme)

function accent(theme: string): RGB {
  switch (theme) {
    case 'cover': return art ? coverAccent(art) : GREEN;
    case 'glass': return [255, 255, 255];
    case 'midnight': return [138, 180, 255];
    case 'purple': return [214, 140, 255];
    case 'christmas': return [255, 209, 102];
    case 'sunset': return [255, 177, 153];
    case 'ocean': return [79, 209, 197];
    default: return GREEN;
  }
}

function rgbToHsb(r: number, g: number, b: number): [number, number, number] {
  const max = Math.max(r, g, b), min = Math.min(r, g, b);
  const v = max / 255, s = max === 0 ? 0 : (max - min) / max;
  let h = 0;
  if (s > 0) {
    const d = max - min;
    const rc = (max - r) / d, gc = (max - g) / d, bc = (max - b) / d;
    h = r === max ? bc - gc : g === max ? 2 + rc - bc : 4 + gc - rc;
    h /= 6;
    if (h < 0) h += 1;
  }
  return [h, s, v];
}

function hsbToRgb(h: number, s: number, v: number): RGB {
  const i = Math.floor(h * 6), f = h * 6 - i;
  const p = v * (1 - s), q = v * (1 - s * f), t = v * (1 - s * (1 - f));
  const [r, g, b] = [[v, t, p], [q, v, p], [p, v, t], [p, q, v], [t, p, v], [v, p, q]][((i % 6) + 6) % 6]!;
  return [Math.round(r! * 255), Math.round(g! * 255), Math.round(b! * 255)];
}

function coverAccent(img: HTMLImageElement): RGB {
  const hit = accentCache.get(img);
  if (hit) return hit;
  const c = document.createElement('canvas');
  c.width = c.height = 24;
  const g = c.getContext('2d', { willReadFrequently: true })!;
  g.drawImage(img, 0, 0, 24, 24);
  const d = g.getImageData(0, 0, 24, 24).data;
  let best = -1;
  let hsb: [number, number, number] = [0.39, 0.8, 0.85];
  for (let i = 0; i < d.length; i += 4) {
    const x = rgbToHsb(d[i]!, d[i + 1]!, d[i + 2]!);
    const score = x[1] * (0.35 + x[2]);
    if (x[2] >= 0.18 && score > best) { best = score; hsb = x; }
  }
  const out: RGB = best < 0.12 ? [235, 235, 235] : hsbToRgb(hsb[0], Math.min(0.85, Math.max(0.45, hsb[1])), Math.max(0.85, hsb[2]));
  accentCache.set(img, out);
  return out;
}

function rr(g: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, arc: number): void {
  // Java RoundRectangle2D takes the arc *diameter*.
  const r = Math.min(arc / 2, w / 2, h / 2);
  g.beginPath();
  g.roundRect(x, y, w, h, r);
}

function paintTheme(g: CanvasRenderingContext2D, theme: string, h: number): void {
  const w = W;
  const shape = () => rr(g, 0, 0, w, h, R);
  const solid = (c: string) => { g.fillStyle = c; shape(); g.fill(); };
  const grad = (a: string, b: string) => { const p = g.createLinearGradient(0, 0, w, h); p.addColorStop(0, a); p.addColorStop(1, b); g.fillStyle = p; shape(); g.fill(); };
  const glow = (cx: number, cy: number, r: number, c: RGB, a: number) => { const p = g.createRadialGradient(cx, cy, 0, cx, cy, r); p.addColorStop(0, css(c, a / 255)); p.addColorStop(1, css(c, 0)); g.fillStyle = p; shape(); g.fill(); };
  switch (theme) {
    case 'cover':
      if (art) {
        g.save();
        shape(); g.clip();
        g.filter = 'blur(6px) saturate(1.2)';
        const s = Math.max(w / art.naturalWidth, h / art.naturalHeight) * 1.25;
        g.drawImage(art, (w - art.naturalWidth * s) / 2, (h - art.naturalHeight * s) / 2, art.naturalWidth * s, art.naturalHeight * s);
        g.filter = 'none';
        const p = g.createLinearGradient(0, 0, w, 0);
        p.addColorStop(0, 'rgba(0,0,0,0.41)'); p.addColorStop(1, 'rgba(0,0,0,0.59)');
        g.fillStyle = p; g.fillRect(0, 0, w, h);
        g.restore();
        return;
      }
      return solid(rgba(18, 18, 18, 234));
    case 'glass': {
      solid(rgba(22, 22, 28, 150));
      const p = g.createLinearGradient(0, 0, 0, h * 0.55);
      p.addColorStop(0, rgba(255, 255, 255, 38)); p.addColorStop(1, rgba(255, 255, 255, 0));
      g.fillStyle = p; shape(); g.fill();
      return;
    }
    case 'midnight': grad(rgba(12, 22, 58, 236), rgba(34, 12, 56, 236)); return glow(w * 0.85, h * 0.1, w * 0.6, [90, 120, 255], 55);
    case 'purple': grad(rgba(38, 8, 64, 238), rgba(88, 52, 150, 238)); return glow(w * 0.15, h * 0.9, w * 0.55, [200, 90, 255], 50);
    case 'christmas': grad(rgba(110, 14, 22, 238), rgba(12, 70, 38, 238)); return glow(w * 0.5, 0, w * 0.5, [255, 210, 120], 40);
    case 'sunset': {
      grad(rgba(140, 40, 30, 238), rgba(120, 18, 70, 238));
      const p = g.createLinearGradient(0, 0, 0, h);
      p.addColorStop(0, rgba(0, 0, 0, 30)); p.addColorStop(1, rgba(0, 0, 0, 90));
      g.fillStyle = p; shape(); g.fill();
      return;
    }
    case 'ocean': grad(rgba(10, 30, 40, 238), rgba(26, 70, 88, 238)); return glow(w * 0.2, h * 0.1, w * 0.5, [79, 209, 197], 40);
    default: return solid(rgba(18, 18, 18, 234));
  }
}

// ------------------------------------------------------------------ drawing helpers (dev.spotifyhud.HudPainter)

function ellipsize(g: CanvasRenderingContext2D, s: string, max: number): string {
  if (g.measureText(s).width <= max) return s;
  let lo = 0, hi = s.length;
  while (lo < hi) {
    const mid = (lo + hi + 1) >> 1;
    if (g.measureText(`${s.slice(0, mid).trim()}…`).width <= max) lo = mid; else hi = mid - 1;
  }
  return `${s.slice(0, lo).trim()}…`;
}

function bar(g: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, f: number, hot: boolean, acc: RGB): void {
  f = Math.max(0, Math.min(1, f));
  g.fillStyle = TRACK; rr(g, x, y, w, h, h); g.fill();
  const fw = w * f;
  if (fw >= 0.35) { g.fillStyle = hot ? css(acc) : FILL; rr(g, x, y, fw, h, Math.min(h, fw)); g.fill(); }
  if (hot) {
    const cx = x + w * f, cy = y + h / 2, r = 3;
    g.fillStyle = 'rgba(0,0,0,0.275)'; g.beginPath(); g.arc(cx, cy + 0.6, r + 0.2, 0, Math.PI * 2); g.fill();
    g.fillStyle = '#fff'; g.beginPath(); g.arc(cx, cy, r, 0, Math.PI * 2); g.fill();
  }
}

function hoverBg(g: CanvasRenderingContext2D, r: Region, x: number): void {
  if (hover !== r) return;
  g.fillStyle = HOVER_BG; g.beginPath(); g.arc(x, 57, 5.6, 0, Math.PI * 2); g.fill();
}

function dot(g: CanvasRenderingContext2D, x: number): void { g.beginPath(); g.arc(x, 61.75, 0.65, 0, Math.PI * 2); g.fill(); }

function drawPause(g: CanvasRenderingContext2D, x: number, y: number): void {
  const h = 4.8, w = 1.35, gap = 1.35;
  rr(g, x - gap / 2 - w, y - h / 2, w, h, 0.5); g.fill();
  rr(g, x + gap / 2, y - h / 2, w, h, 0.5); g.fill();
}
function drawPlay(g: CanvasRenderingContext2D, x: number, y: number): void {
  const h = 5.2, w = 4.6;
  g.beginPath(); g.moveTo(x - w / 2 + 0.4, y - h / 2); g.lineTo(x + w / 2 + 0.4, y); g.lineTo(x - w / 2 + 0.4, y + h / 2); g.closePath(); g.fill();
}
function drawPrev(g: CanvasRenderingContext2D, x: number, y: number): void {
  const h = 4.6;
  rr(g, x - 2.9, y - h / 2, 1, h, 0.4); g.fill();
  g.beginPath(); g.moveTo(x + 2.7, y - h / 2); g.lineTo(x - 1.7, y); g.lineTo(x + 2.7, y + h / 2); g.closePath(); g.fill();
}
function drawNext(g: CanvasRenderingContext2D, x: number, y: number): void {
  const h = 4.6;
  rr(g, x + 1.9, y - h / 2, 1, h, 0.4); g.fill();
  g.beginPath(); g.moveTo(x - 2.7, y - h / 2); g.lineTo(x + 1.7, y); g.lineTo(x - 2.7, y + h / 2); g.closePath(); g.fill();
}
function drawRepeat(g: CanvasRenderingContext2D, x: number, y: number, one: boolean): void {
  const w = 5.4, h = 4, x0 = x - w / 2, x1 = x + w / 2, y0 = y - h / 2, y1 = y + h / 2, r = 1;
  g.lineWidth = 0.95; g.lineCap = 'round'; g.lineJoin = 'round';
  g.beginPath();
  g.moveTo(x1 - 1.9, y0); g.lineTo(x0 + r, y0); g.quadraticCurveTo(x0, y0, x0, y0 + r); g.lineTo(x0, y1 - r);
  g.quadraticCurveTo(x0, y1, x0 + r, y1); g.lineTo(x1 - r, y1); g.quadraticCurveTo(x1, y1, x1, y1 - r); g.lineTo(x1, y0 + r + 0.3);
  g.strokeStyle = g.fillStyle as string; g.stroke();
  g.beginPath(); g.moveTo(x1 - 1.9, y0 - 1.35); g.lineTo(x1 - 0.2, y0); g.lineTo(x1 - 1.9, y0 + 1.35); g.closePath(); g.fill();
  if (one) { g.font = `700 3.2px sans-serif`; const tw = g.measureText('1').width; g.fillText('1', x - tw / 2, y + 1.15); }
}
function arrowHead(g: CanvasRenderingContext2D, x: number, y: number): void {
  g.beginPath(); g.moveTo(x - 1.6, y - 1.25); g.lineTo(x, y); g.lineTo(x - 1.6, y + 1.25); g.closePath(); g.fill();
}
function drawShuffle(g: CanvasRenderingContext2D, x: number, y: number): void {
  g.lineWidth = 0.9; g.lineCap = 'round'; g.lineJoin = 'round'; g.strokeStyle = g.fillStyle as string;
  const x0 = x - 2.9, x1 = x + 2.1, y0 = y - 1.7, y1 = y + 1.7;
  g.beginPath(); g.moveTo(x0, y0); g.bezierCurveTo(x - 0.8, y0, x - 0.4, y1, x1, y1); g.stroke();
  g.beginPath(); g.moveTo(x0, y1); g.bezierCurveTo(x - 0.8, y1, x - 0.4, y0, x1, y0); g.stroke();
  arrowHead(g, x1 + 0.9, y0); arrowHead(g, x1 + 0.9, y1);
}
function drawHeart(g: CanvasRenderingContext2D, x: number, y: number): void {
  const w = 5.4, h = 4.7, x0 = x - w / 2, y0 = y - h / 2 + 0.1;
  g.beginPath();
  g.moveTo(x, y0 + h);
  g.bezierCurveTo(x0 - 0.2, y0 + h * 0.55, x0 - 0.1, y0 + 0.1, x0 + w * 0.27, y0);
  g.bezierCurveTo(x0 + w * 0.42, y0 - 0.05, x - 0.05, y0 + 0.6, x, y0 + 1.1);
  g.bezierCurveTo(x + 0.05, y0 + 0.6, x0 + w * 0.58, y0 - 0.05, x0 + w * 0.73, y0);
  g.bezierCurveTo(x0 + w + 0.1, y0 + 0.1, x0 + w + 0.2, y0 + h * 0.55, x, y0 + h);
  g.closePath(); g.fill();
}
function drawSpeaker(g: CanvasRenderingContext2D, x: number, y: number, vol: number): void {
  g.fillStyle = SPEAKER; g.strokeStyle = SPEAKER;
  const x0 = x - 3;
  g.beginPath(); g.moveTo(x0, y - 1.1); g.lineTo(x0 + 1.3, y - 1.1); g.lineTo(x0 + 3, y - 2.6); g.lineTo(x0 + 3, y + 2.6); g.lineTo(x0 + 1.3, y + 1.1); g.lineTo(x0, y + 1.1); g.closePath(); g.fill();
  g.lineWidth = 0.75; g.lineCap = 'round';
  // Java Arc2D: start -55°, extent 110° (counter-clockwise, y up) around the box centre.
  const arc = (bx: number, by: number, s: number) => { g.beginPath(); g.arc(bx + s / 2, by + s / 2, s / 2, (-55 * Math.PI) / 180, (55 * Math.PI) / 180); g.stroke(); };
  if (vol === 0) {
    const xx = x + 1.7;
    g.beginPath(); g.moveTo(xx - 0.1, y - 1.2); g.lineTo(xx + 2.2, y + 1.2); g.moveTo(xx - 0.1, y + 1.2); g.lineTo(xx + 2.2, y - 1.2); g.stroke();
  } else {
    arc(x - 1.4, y - 1.6, 3.2);
    if (vol > 50 || vol < 0) arc(x - 2.1, y - 2.8, 5.6);
  }
}
function drawNote(g: CanvasRenderingContext2D, x: number, y: number): void {
  g.fillStyle = NOTE; g.strokeStyle = NOTE;
  g.beginPath(); g.ellipse(x - 5.5 + 2.2, y + 2.2 + 1.7, 2.2, 1.7, 0, 0, Math.PI * 2); g.fill();
  g.beginPath(); g.ellipse(x + 1.6 + 2.2, y + 0.8 + 1.7, 2.2, 1.7, 0, 0, Math.PI * 2); g.fill();
  g.lineWidth = 1.1;
  g.beginPath(); g.moveTo(x - 1.6, y + 3.8); g.lineTo(x - 1.6, y - 5); g.moveTo(x + 5.5, y + 2.4); g.lineTo(x + 5.5, y - 6.4); g.stroke();
  g.beginPath(); g.moveTo(x - 2.15, y - 5); g.lineTo(x + 6.05, y - 6.4); g.lineTo(x + 6.05, y - 4.6); g.lineTo(x - 2.15, y - 3.2); g.closePath(); g.fill();
}
function drawGear(g: CanvasRenderingContext2D, x: number, y: number, r: number): void {
  const n = 8;
  g.beginPath();
  for (let i = 0; i < n * 2; i++) {
    const a0 = (Math.PI * 2 * i) / (n * 2), a1 = (Math.PI * 2 * (i + 1)) / (n * 2), rad = i % 2 === 0 ? r : r * 0.76;
    if (i === 0) g.moveTo(x + Math.cos(a0) * rad, y + Math.sin(a0) * rad); else g.lineTo(x + Math.cos(a0) * rad, y + Math.sin(a0) * rad);
    g.lineTo(x + Math.cos(a1) * rad, y + Math.sin(a1) * rad);
  }
  g.closePath();
  g.arc(x, y, r * 0.36, 0, Math.PI * 2, true);
  g.fill('evenodd');
}

// ------------------------------------------------------------------ panels

function edge(g: CanvasRenderingContext2D, h: number, hot: boolean, scale: number): void {
  g.strokeStyle = hot ? PANEL_EDGE_HOVER : PANEL_EDGE;
  g.lineWidth = 1 / scale;
  const o = 0.5 / scale;
  rr(g, o, o, W - 2 * o, h - 2 * o, R);
  g.stroke();
}

function drawHud(g: CanvasRenderingContext2D, s: SpotifyState, scale: number): void {
  const acc = accent(cfg.theme);
  paintTheme(g, cfg.theme, H);
  edge(g, H, hover === 'PANEL' || hover === 'SETTINGS', scale);
  if (art) {
    g.save(); rr(g, 9, 9, 37, 37, 8); g.clip();
    g.drawImage(art, 9, 9, 37, 37);
    g.restore();
  } else {
    g.fillStyle = ART_EMPTY; rr(g, 9, 9, 37, 37, 8); g.fill();
    drawNote(g, 27.5, 27.5);
  }
  let timeW = 0;
  if (cfg.showTime) {
    const p = progressPreview >= 0 ? progressPreview * s.durMs : progress(s);
    const t = `${fmt(p)} / ${fmt(s.durMs)}`;
    g.font = font(false, 7.8);
    timeW = g.measureText(t).width;
    g.fillStyle = TIME; g.fillText(t, 197 - timeW, 29);
  }
  g.font = font(true, 9.9);
  g.letterSpacing = '0.2px';
  g.fillStyle = css(TITLE);
  const interactive = hover !== null;
  g.fillText(ellipsize(g, s.title, 145 - (interactive ? 9 : 0)), 53, 18.2);
  g.letterSpacing = '0px';
  g.font = font(false, 8.1);
  g.fillStyle = css(ARTIST);
  g.fillText(ellipsize(g, s.artist, 144 - (timeW > 0 ? timeW + 6 : 0)), 53, 29);
  if (interactive) {
    if (hover === 'SETTINGS') { g.fillStyle = HOVER_BG; g.beginPath(); g.arc(199.2, 8.2, 5, 0, Math.PI * 2); g.fill(); }
    g.fillStyle = hover === 'SETTINGS' ? ICON : 'rgba(255,255,255,0.59)';
    drawGear(g, 199.2, 8.2, 3.1);
  }
  const frac = progressPreview >= 0 ? progressPreview : s.durMs > 0 ? progress(s) / s.durMs : 0;
  bar(g, 52.5, 36.6, 146.5, 3.4, frac, hover === 'PROGRESS' || progressPreview >= 0, acc);
  if (cfg.showControls) {
    const vol = volumePreview >= 0 ? volumePreview : s.volume;
    hoverBg(g, 'SPEAKER', 14);
    drawSpeaker(g, 14, 57, vol);
    bar(g, 21, 55.4, 37.5, 3.2, vol < 0 ? 0 : vol / 100, hover === 'VOLUME' || volumePreview >= 0, acc);
    hoverBg(g, 'PREV', 88.8); hoverBg(g, 'PLAY', 103.3); hoverBg(g, 'NEXT', 118); hoverBg(g, 'REPEAT', 167.2); hoverBg(g, 'SHUFFLE', 181.3); hoverBg(g, 'LIKE', 195.8);
    g.fillStyle = ICON;
    drawPrev(g, 88.8, 57);
    if (s.playing) drawPause(g, 103.3, 57); else drawPlay(g, 103.3, 57);
    drawNext(g, 118, 57);
    const rep = s.repeat !== 'off';
    g.fillStyle = rep ? css(acc) : ICON;
    drawRepeat(g, 167.2, 57, s.repeat === 'track');
    if (rep) dot(g, 167.2);
    g.fillStyle = s.shuffle ? css(acc) : ICON;
    drawShuffle(g, 181.3, 57);
    if (s.shuffle) dot(g, 181.3);
    g.fillStyle = ICON;
    drawHeart(g, 195.8, 57);
  }
}

interface LyricsView { message: string | null; prev: string; next: string; words: string[]; cur: string; wordIdx: number; grow: number; anim: number }

function lyricsView(s: SpotifyState): LyricsView {
  const v: LyricsView = { message: null, prev: '', next: '', words: [], cur: '', wordIdx: -1, grow: 1, anim: 1 };
  switch (lyrics.status) {
    case 'loading': v.message = 'Lyrics werden geladen...'; break;
    case 'not_found': v.message = 'Keine Lyrics gefunden'; break;
    case 'instrumental': v.message = '♪ Instrumental ♪'; break;
    case 'error': v.message = 'Lyrics-Server nicht erreichbar'; break;
    case 'plain': v.message = 'Nur ungesyncte Lyrics verfügbar'; break;
    case 'none': v.message = s.has ? 'Lyrics werden geladen...' : 'Keine Wiedergabe'; break;
    case 'synced': {
      const lines = lyrics.lines;
      const ms = progress(s) + cfg.lyricsOffsetMs;
      let idx = -1;
      for (let lo = 0, hi = lines.length - 1; lo <= hi;) { const m = (lo + hi) >> 1; if (lines[m]!.timeMs <= ms) { idx = m; lo = m + 1; } else hi = m - 1; }
      const now = performance.now();
      if (lyricsFor !== lastLyricKey) { lastLyricKey = lyricsFor; lastLyricIndex = idx; lyricChangedAt = 0; }
      if (idx !== lastLyricIndex) { lyricChangedAt = idx === lastLyricIndex + 1 ? now : 0; lastLyricIndex = idx; }
      const text = (l: LyricLine | undefined) => (l ? (l.text.trim() ? l.text : '♪') : '');
      v.prev = text(lines[idx - 1]);
      v.next = text(lines[idx + 1]);
      if (idx >= 0) {
        const l = lines[idx]!;
        v.cur = text(l);
        v.words = l.words;
        let w = -1;
        for (let i = 0; i < l.wordTimes.length && l.wordTimes[i]! <= ms; i++) w = i;
        v.wordIdx = w;
        v.grow = w >= 0 ? Math.min(1, (ms - l.wordTimes[w]!) / 150) : 1;
      } else v.cur = '♪';
      v.anim = lyricChangedAt === 0 ? 1 : Math.max(0, Math.min(1, (now - lyricChangedAt) / 260));
      break;
    }
  }
  return v;
}

const easeOut = (t: number) => 1 - (1 - Math.max(0, Math.min(1, t))) ** 3;

function wordsWidth(g: CanvasRenderingContext2D, words: string[]): number {
  const sp = g.measureText(' ').width;
  return words.reduce((a, w, i) => a + g.measureText(w).width + (i > 0 ? sp : 0), 0);
}

function centered(g: CanvasRenderingContext2D, s: string, y: number): void {
  g.fillText(s, (W - g.measureText(s).width) / 2, y);
}

function drawLine(g: CanvasRenderingContext2D, s: string, y: number, alpha: number, max: number): void {
  if (!s || alpha <= 0.01) return;
  g.font = font(false, 7.6);
  g.fillStyle = css(ARTIST, Math.min(1, alpha));
  centered(g, ellipsize(g, s, max), y);
}

function karaokeRow(g: CanvasRenderingContext2D, words: string[], from: number, to: number, y: number, v: LyricsView, acc: RGB, fade: number, size: number): void {
  const sp = g.measureText(' ').width;
  const grow = cfg.wordGrow ? 1 + 0.18 * easeOut(v.grow) : 1;
  const widths: number[] = [], eff: number[] = [];
  let total = 0;
  for (let i = from; i < to; i++) {
    const w = g.measureText(words[i]!).width;
    widths.push(w);
    eff.push(w * (cfg.karaoke && i === v.wordIdx ? grow : 1));
    total += eff[i - from]! + (i > from ? sp : 0);
  }
  const max = 187;
  let x = (W - Math.min(total, max)) / 2;
  const pad = 4.5, ph = size * 1.45;
  g.fillStyle = css(acc, (38 * fade) / 255);
  rr(g, x - pad, y - size * 1.02, Math.min(total, max) + 2 * pad, ph, ph);
  g.fill();
  for (let i = from; i < to; i++) {
    const word = words[i]!, w = widths[i - from]!;
    const wx = x + (eff[i - from]! - w) / 2;
    const isCur = cfg.karaoke && i === v.wordIdx;
    let col: RGB, a = 1;
    if (!cfg.karaoke || i < v.wordIdx) col = TITLE;
    else if (isCur) col = acc;
    else { col = [255, 255, 255]; a = 120 / 255; }
    if (!isCur) {
      g.fillStyle = css(col, a * fade);
      g.fillText(word, wx, y);
    } else {
      g.save();
      const cx = wx + w / 2, cy = y - size * 0.35;
      g.translate(cx, cy); g.scale(grow, grow); g.translate(-cx, -cy);
      g.fillStyle = css(col, (70 / 255) * fade);
      for (const [dx, dy] of [[-0.35, 0], [0.35, 0], [0, -0.35], [0, 0.35]] as const) g.fillText(word, wx + dx, y + dy);
      g.fillStyle = css(col, fade);
      g.fillText(word, wx, y);
      g.restore();
    }
    x += eff[i - from]! + sp;
  }
}

function drawLyrics(g: CanvasRenderingContext2D, s: SpotifyState, scale: number): void {
  const acc = accent(cfg.theme);
  paintTheme(g, cfg.theme, LH);
  edge(g, LH, lyrHover, scale);
  g.save();
  g.beginPath(); g.rect(3, 1.5, 201, 37); g.clip();
  const v = lyricsView(s);
  const max = 187;
  if (v.message) {
    g.font = font(false, 8.2);
    g.fillStyle = css(ARTIST);
    centered(g, ellipsize(g, v.message, max), 23.5);
  } else {
    const e = 1 - (1 - v.anim) ** 3;
    const off = (1 - e) * 12;
    const words = v.words.length ? v.words : [v.cur || '♪'];
    let size = 9.6;
    g.font = font(true, size);
    while (size > 7.4 && wordsWidth(g, words) > max) { size -= 0.3; g.font = font(true, size); }
    let split = words.length;
    if (wordsWidth(g, words) > max && words.length > 1) {
      const total = wordsWidth(g, words), sp = g.measureText(' ').width;
      let acc2 = 0;
      split = Math.floor(words.length / 2);
      for (let i = 0; i < words.length; i++) {
        acc2 += g.measureText(words[i]!).width + sp;
        if (acc2 >= total / 2) { split = Math.max(1, Math.min(words.length - 1, i + 1)); break; }
      }
    }
    const two = split < words.length;
    if (!two) { drawLine(g, v.prev, 11 + off, 0.42 * e, max); g.font = font(true, size); }
    const ys = two ? [19.2 + off, 29 + off] : [24.5 + off];
    const ranges = two ? [[0, split], [split, words.length]] : [[0, words.length]];
    const fade = 0.55 + 0.45 * e;
    ranges.forEach(([a, b], i) => { g.font = font(true, size); karaokeRow(g, words, a!, b!, ys[i]!, v, acc, fade, size); });
    drawLine(g, v.next, (two ? 37.6 : 35.5) + off, 0.55 * e, max);
  }
  g.restore();
}

// ------------------------------------------------------------------ hit testing (dev.spotifyhud.HudLayout)

function hit(x: number, y: number): Region | null {
  if (x < 0 || y < 0 || x > W || y > H) return null;
  const near = (cx: number, cy: number, r: number) => Math.abs(x - cx) <= r && Math.abs(y - cy) <= r;
  if (near(199.2, 8.2, 5.5)) return 'SETTINGS';
  if (cfg.showControls && y >= 50 && y <= 64.5) {
    if (near(14, 57, 5.5)) return 'SPEAKER';
    if (x >= 19.5 && x <= 60.5) return 'VOLUME';
    if (near(88.8, 57, 6.5)) return 'PREV';
    if (near(103.3, 57, 6.5)) return 'PLAY';
    if (near(118, 57, 6.5)) return 'NEXT';
    if (near(167.2, 57, 6.5)) return 'REPEAT';
    if (near(181.3, 57, 6.5)) return 'SHUFFLE';
    if (near(195.8, 57, 6.5)) return 'LIKE';
  }
  if (x >= 51 && x <= 200.5 && y >= 32.6 && y <= 44) return 'PROGRESS';
  return x >= 9 && x <= 46 && y >= 9 && y <= 46 ? 'ART' : 'PANEL';
}

// ------------------------------------------------------------------ mounting

function size(c: HTMLCanvasElement, h: number): { g: CanvasRenderingContext2D; scale: number } {
  const css = W * cfg.scale;
  const dpr = window.devicePixelRatio || 1;
  c.style.width = `${css}px`;
  c.style.height = `${h * cfg.scale}px`;
  const pw = Math.round(css * dpr), ph = Math.round(h * cfg.scale * dpr);
  if (c.width !== pw || c.height !== ph) { c.width = pw; c.height = ph; }
  const g = c.getContext('2d')!;
  g.setTransform(1, 0, 0, 1, 0, 0);
  g.clearRect(0, 0, pw, ph);
  const scale = pw / W;
  g.scale(scale, scale);
  g.textBaseline = 'alphabetic';
  return { g, scale };
}

function place(): void {
  if (!root) return;
  const totalH = (H + (cfg.lyrics ? GAP + LH : 0)) * cfg.scale;
  const x = Math.max(0, Math.min(window.innerWidth - W * cfg.scale, cfg.x * window.innerWidth));
  const y = Math.max(0, Math.min(window.innerHeight - totalH, cfg.y * window.innerHeight));
  root.style.transform = `translate(${Math.round(x)}px, ${Math.round(y)}px)`;
}

function frame(): void {
  raf = 0;
  if (!root) return;
  const s = st;
  const show = !!(cfg.enabled && s && s.available && s.has && !s.fatal);
  root.hidden = !show;
  if (show) {
    const a = size(hudC, H);
    drawHud(a.g, s, a.scale);
    lyrC.hidden = !cfg.lyrics;
    if (cfg.lyrics) { const b = size(lyrC, LH); drawLyrics(b.g, s, b.scale); }
    place();
  }
  if (show) raf = window.setTimeout(() => { raf = requestAnimationFrame(frame); }, 33) as unknown as number;
}

function kick(): void {
  if (!raf) raf = requestAnimationFrame(frame);
}

async function loadLyrics(s: SpotifyState): Promise<void> {
  const key = `${s.title}|${s.artist}`.toLowerCase();
  if (!s.has || !s.title) { lyricsFor = ''; lyrics = { status: 'none', lines: [] }; return; }
  if (key === lyricsFor) return;
  lyricsFor = key;
  lyrics = { status: 'loading', lines: [] };
  try {
    const r = await api.spotifyLyrics(s.title, s.artist, s.durMs);
    if (lyricsFor === key) lyrics = r;
  } catch {
    if (lyricsFor === key) lyrics = { status: 'error', lines: [] };
  }
}

function cmd(c: string): void { void api.spotifyCommand(c); }

/** Creates the floating HUD once. `settings` opens the launcher's Spotify settings. */
export function mountSpotifyHud(initial: SpotifyHudSettings, onSettings: () => void, save: (p: Partial<SpotifyHudSettings>) => void): void {
  cfg = initial;
  openSettings = onSettings;
  if (root) { kick(); return; }
  root = document.createElement('div');
  root.id = 'spotifyHud';
  root.hidden = true;
  hudC = document.createElement('canvas');
  lyrC = document.createElement('canvas');
  lyrC.style.marginTop = `${GAP * cfg.scale}px`;
  root.append(hudC, lyrC);
  document.body.append(root);

  const local = (e: MouseEvent, c: HTMLCanvasElement) => { const r = c.getBoundingClientRect(); return { x: ((e.clientX - r.left) / r.width) * W, y: ((e.clientY - r.top) / r.height) * H }; };
  let drag: { dx: number; dy: number; moved: boolean } | null = null;
  let scrub: 'PROGRESS' | 'VOLUME' | null = null;
  const fracOf = (x: number, r: 'PROGRESS' | 'VOLUME') => (r === 'PROGRESS' ? Math.max(0, Math.min(1, (x - 52.5) / 146.5)) : Math.max(0, Math.min(1, (x - 21) / 37.5)));

  hudC.addEventListener('mousemove', (e) => {
    const p = local(e, hudC);
    if (scrub === 'PROGRESS') progressPreview = fracOf(p.x, 'PROGRESS');
    else if (scrub === 'VOLUME') volumePreview = Math.round(fracOf(p.x, 'VOLUME') * 100);
    hover = hit(p.x, p.y);
    hudC.style.cursor = hover && hover !== 'PANEL' && hover !== 'ART' ? 'pointer' : 'grab';
    kick();
  });
  hudC.addEventListener('mouseleave', () => { if (!scrub) hover = null; kick(); });
  lyrC.addEventListener('mouseenter', () => { lyrHover = true; kick(); });
  lyrC.addEventListener('mouseleave', () => { lyrHover = false; kick(); });
  const startDrag = (e: MouseEvent) => {
    const r = root!.getBoundingClientRect();
    drag = { dx: e.clientX - r.left, dy: e.clientY - r.top, moved: false };
  };
  lyrC.addEventListener('mousedown', (e) => { if (e.button === 0) startDrag(e); });
  hudC.addEventListener('mousedown', (e) => {
    if (e.button !== 0) return;
    const p = local(e, hudC);
    const r = hit(p.x, p.y);
    if (r === 'PROGRESS' && st?.canSeek) { scrub = 'PROGRESS'; progressPreview = fracOf(p.x, 'PROGRESS'); }
    else if (r === 'VOLUME' && st && st.volume >= 0) { scrub = 'VOLUME'; volumePreview = Math.round(fracOf(p.x, 'VOLUME') * 100); }
    else if (r === 'PANEL' || r === 'ART') startDrag(e);
    kick();
  });
  window.addEventListener('mousemove', (e) => {
    if (!drag || !root) return;
    drag.moved = true;
    cfg = { ...cfg, x: (e.clientX - drag.dx) / window.innerWidth, y: (e.clientY - drag.dy) / window.innerHeight };
    place();
  });
  window.addEventListener('mouseup', (e) => {
    if (drag) { if (drag.moved) save({ x: cfg.x, y: cfg.y }); drag = null; return; }
    if (scrub && st) {
      if (scrub === 'PROGRESS' && progressPreview >= 0) cmd(`seek ${Math.round(progressPreview * Math.max(0, st.durMs - 300))}`);
      if (scrub === 'VOLUME' && volumePreview >= 0) cmd(`vol ${volumePreview}`);
      scrub = null; progressPreview = -1; volumePreview = -1;
      const p = local(e, hudC); hover = hit(p.x, p.y);
      kick();
    }
  });
  hudC.addEventListener('click', (e) => {
    const p = local(e, hudC);
    const s = st;
    if (!s) return;
    switch (hit(p.x, p.y)) {
      case 'PLAY': st = { ...s, playing: !s.playing, posMs: progress(s), at: Date.now() }; cmd('toggle'); break;
      case 'PREV': cmd('prev'); break;
      case 'NEXT': cmd('next'); break;
      case 'SHUFFLE': if (s.canShuffle) { st = { ...s, shuffle: !s.shuffle }; cmd(`shuffle ${!s.shuffle}`); } break;
      case 'REPEAT': if (s.canRepeat) { const n = s.repeat === 'off' ? 'context' : s.repeat === 'context' ? 'track' : 'off'; st = { ...s, repeat: n }; cmd(`repeat ${n === 'track' ? 1 : n === 'context' ? 2 : 0}`); } break;
      case 'SPEAKER': cmd('mute'); break;
      case 'SETTINGS': openSettings(); break;
      default: break;
    }
    kick();
  });
  window.addEventListener('resize', place);

  api.on('spotify', (s: SpotifyState) => { st = s; void loadLyrics(s); kick(); });
  api.on('spotify-art', (a: { key: string; url: string | null }) => {
    artKey = a.key;
    if (!a.url) { art = null; kick(); return; }
    const img = new Image();
    img.onload = () => { if (artKey === a.key) { art = img; kick(); } };
    img.src = a.url;
  });
  void api.spotifyState().then((s) => { if (s) { st = s; void loadLyrics(s); } kick(); });
}

/** Applies changed settings (theme, scale, lyrics …). */
export function updateSpotifyHud(next: SpotifyHudSettings): void {
  cfg = next;
  if (lyrC) lyrC.style.marginTop = `${GAP * cfg.scale}px`;
  kick();
}
