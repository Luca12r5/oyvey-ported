// Launcher backgrounds. 3D scenes (bg3d.ts, WebGL) are the default; the
// classic 2D canvas styles below remain as lightweight options and as the
// fallback when WebGL is missing or its context is lost. Colours come from the
// theme variables, quality scales resolution and particle counts, animation
// stops when the window is hidden or motion is reduced.

import { Bg3D, is3D } from './bg3d.ts';
import { DEFAULT_FX, type DesignFx } from '../core/design.ts';
import { EXTRA_BACKGROUNDS, EXTRA_DRAW } from './bg-extra.ts';

export interface BackgroundDef { id: string; name: { de: string; en: string }; kind: '3d' | '2d' }

/** 2D stand-ins when a 3D scene cannot be rendered. */
const FALLBACK_2D: Record<string, string> = { graveyard: 'haunted', forest: 'haunted', castle: 'haunted', portal: 'warp', tunnel: 'warp', scifi: 'grid', rooms: 'voxels' };

export const BACKGROUNDS: BackgroundDef[] = [
  { id: 'graveyard', name: { de: 'Nebliger Friedhof', en: 'Misty graveyard' }, kind: '3d' },
  { id: 'forest', name: { de: 'Halloween-Wald', en: 'Halloween forest' }, kind: '3d' },
  { id: 'castle', name: { de: 'Dunkles Schloss', en: 'Dark castle' }, kind: '3d' },
  { id: 'portal', name: { de: 'Magisches Portal', en: 'Magic portal' }, kind: '3d' },
  { id: 'tunnel', name: { de: 'Partikeltunnel', en: 'Particle tunnel' }, kind: '3d' },
  { id: 'scifi', name: { de: 'Sci-Fi-Horizont', en: 'Sci-fi horizon' }, kind: '3d' },
  { id: 'rooms', name: { de: 'Abstrakte Räume', en: 'Abstract rooms' }, kind: '3d' },
  { id: 'image', name: { de: 'Eigenes Bild', en: 'Custom image' }, kind: '2d' },
  { id: 'nebula', name: { de: 'Nebel', en: 'Nebula' }, kind: '2d' },
  { id: 'bricks', name: { de: 'Schwebende Steine', en: 'Floating bricks' }, kind: '2d' },
  { id: 'aurora', name: { de: 'Polarlicht', en: 'Aurora' }, kind: '2d' },
  { id: 'grid', name: { de: 'Neon-Raster', en: 'Neon grid' }, kind: '2d' },
  { id: 'constellation', name: { de: 'Sternbild', en: 'Constellation' }, kind: '2d' },
  { id: 'warp', name: { de: 'Warp', en: 'Warp' }, kind: '2d' },
  { id: 'waves', name: { de: 'Wellen', en: 'Waves' }, kind: '2d' },
  { id: 'voxels', name: { de: 'Voxel-Feld', en: 'Voxel field' }, kind: '2d' },
  { id: 'plain', name: { de: 'Schlicht', en: 'Plain' }, kind: '2d' },
  { id: 'haunted', name: { de: 'Spukhaus (2D)', en: 'Haunted (2D)' }, kind: '2d' },
  ...EXTRA_BACKGROUNDS.map((b) => ({ ...b, kind: '2d' as const })),
];

interface Palette { bg: string; bg2: string; accent: string; accent2: string; text: string }

function palette(): Palette {
  const cs = getComputedStyle(document.documentElement);
  const v = (n: string, d: string) => cs.getPropertyValue(n).trim() || d;
  return { bg: v('--bg', '#141519'), bg2: v('--bg2', '#1e1f25'), accent: v('--accent', '#d946ef'), accent2: v('--accent2', '#8b5cf6'), text: v('--text', '#ececf1') };
}

function rgba(hex: string, a: number): string {
  const m = /^#?([0-9a-f]{6})$/i.exec(hex.trim());
  if (!m) return `rgba(255,255,255,${a})`;
  const n = parseInt(m[1]!, 16);
  return `rgba(${(n >> 16) & 255},${(n >> 8) & 255},${n & 255},${a})`;
}

/** Deterministic pseudo random so previews look the same every time. */
function rng(seed: number): () => number {
  let s = seed >>> 0;
  return () => ((s = (s * 1664525 + 1013904223) >>> 0) / 4294967296);
}

type Draw = (ctx: CanvasRenderingContext2D, w: number, h: number, t: number, p: Palette, q: number) => void;

function brick(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, color: string, rot: number, alpha: number): void {
  ctx.save();
  ctx.translate(x, y);
  ctx.rotate(rot);
  ctx.globalAlpha = alpha;
  const w = s * 2, h = s * 1.2;
  const g = ctx.createLinearGradient(0, -h / 2, 0, h / 2);
  g.addColorStop(0, rgba(color, 0.95));
  g.addColorStop(1, rgba(color, 0.55));
  ctx.fillStyle = g;
  ctx.beginPath();
  ctx.roundRect(-w / 2, -h / 2, w, h, s * 0.15);
  ctx.fill();
  ctx.fillStyle = rgba(color, 1);
  for (const sx of [-w / 4, w / 4]) {
    ctx.beginPath();
    ctx.roundRect(sx - s * 0.32, -h / 2 - s * 0.28, s * 0.64, s * 0.32, s * 0.08);
    ctx.fill();
  }
  ctx.fillStyle = 'rgba(255,255,255,.18)';
  ctx.fillRect(-w / 2 + s * 0.12, -h / 2 + s * 0.1, w - s * 0.24, s * 0.08);
  ctx.restore();
}


// ---- haunted background helpers ----

function batShape(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, color: string, phase: number, alpha: number): void {
  ctx.save();
  ctx.translate(x, y);
  ctx.globalAlpha = alpha;
  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.arc(0, 0, s * 0.28, 0, Math.PI * 2);
  ctx.fill();
  const wy = Math.sin(phase) * s * 0.22;
  for (const dir of [-1, 1]) {
    ctx.beginPath();
    ctx.moveTo(dir * s * 0.2, 0);
    ctx.quadraticCurveTo(dir * s * 0.65, -s * 0.5 + wy, dir * s, wy * 0.4);
    ctx.quadraticCurveTo(dir * s * 0.5, s * 0.1 + wy * 0.3, dir * s * 0.2, 0);
    ctx.fill();
  }
  ctx.restore();
}

function pumpkinShape(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, color: string, glow: string, alpha: number): void {
  ctx.save();
  ctx.translate(x, y);
  ctx.globalAlpha = alpha;
  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.ellipse(0, 0, s, s * 0.78, 0, 0, Math.PI * 2);
  ctx.fill();
  const g = ctx.createRadialGradient(0, 0, 0, 0, 0, s * 0.55);
  g.addColorStop(0, rgba(glow, 0.6));
  g.addColorStop(1, rgba(glow, 0));
  ctx.fillStyle = g;
  ctx.fillRect(-s, -s, s * 2, s * 2);
  ctx.fillStyle = glow;
  for (const sx of [-s * 0.28, s * 0.12]) {
    ctx.beginPath();
    ctx.moveTo(sx, -s * 0.12);
    ctx.lineTo(sx + s * 0.18, -s * 0.12);
    ctx.lineTo(sx + s * 0.09, -s * 0.32);
    ctx.closePath();
    ctx.fill();
  }
  ctx.beginPath();
  ctx.moveTo(-s * 0.3, s * 0.12);
  for (let i = 0; i < 5; i++) ctx.lineTo(-s * 0.3 + (i + 0.5) * s * 0.12, i % 2 ? s * 0.28 : s * 0.08);
  ctx.lineTo(s * 0.3, s * 0.12);
  ctx.fill();
  ctx.restore();
}

function spiderWeb(ctx: CanvasRenderingContext2D, ox: number, oy: number, size: number, color: string, alpha: number): void {
  ctx.save();
  ctx.globalAlpha = alpha;
  ctx.strokeStyle = color;
  ctx.lineWidth = 0.8;
  const rays = 7, rings = 4;
  for (let r = 0; r < rays; r++) {
    const a = (r / rays) * Math.PI * 0.5;
    ctx.beginPath();
    ctx.moveTo(ox, oy);
    ctx.lineTo(ox + Math.cos(a) * size, oy + Math.sin(a) * size);
    ctx.stroke();
  }
  for (let ring = 1; ring <= rings; ring++) {
    const d = (ring / rings) * size;
    ctx.beginPath();
    for (let r = 0; r <= rays; r++) {
      const a = (Math.min(r, rays - 1) / rays) * Math.PI * 0.5;
      const px = ox + Math.cos(a) * d, py = oy + Math.sin(a) * d;
      r === 0 ? ctx.moveTo(px, py) : ctx.lineTo(px, py);
    }
    ctx.stroke();
  }
  ctx.restore();
}

const DRAW: Record<string, Draw> = {
  plain(ctx, w, h, _t, p) {
    const g = ctx.createRadialGradient(w * 0.5, h * 0.35, 0, w * 0.5, h * 0.35, Math.max(w, h) * 0.8);
    g.addColorStop(0, p.bg2);
    g.addColorStop(1, p.bg);
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, w, h);
  },
  nebula(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const blobs = [[0.2, 0.25, p.accent], [0.8, 0.7, p.accent2], [0.55, 0.15, p.accent2], [0.3, 0.85, p.accent]] as const;
    ctx.globalCompositeOperation = 'lighter';
    blobs.forEach(([bx, by, c], i) => {
      const x = w * (bx + Math.sin(t * 0.00011 + i * 2) * 0.08);
      const y = h * (by + Math.cos(t * 0.00009 + i) * 0.08);
      const r = Math.max(w, h) * (0.32 + 0.05 * Math.sin(t * 0.0002 + i));
      const g = ctx.createRadialGradient(x, y, 0, x, y, r);
      g.addColorStop(0, rgba(c, 0.11));
      g.addColorStop(1, rgba(c, 0));
      ctx.fillStyle = g;
      ctx.fillRect(0, 0, w, h);
    });
    ctx.globalCompositeOperation = 'source-over';
    const r = rng(7);
    const n = Math.round(60 * q) + 10;
    for (let i = 0; i < n; i++) {
      const x = r() * w, y = r() * h, a = 0.2 + 0.5 * Math.abs(Math.sin(t * 0.001 + i));
      ctx.fillStyle = rgba(p.text, a * 0.6);
      ctx.fillRect(x, y, 1.2, 1.2);
    }
  },
  bricks(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const r = rng(42);
    const n = Math.round(10 + 26 * q);
    const colors = [p.accent, p.accent2, '#e3000b', '#f2cd37', '#0055bf', '#4b9f4a'];
    for (let i = 0; i < n; i++) {
      const depth = 0.3 + r() * 0.7;
      const s = (10 + r() * 18) * depth * (w / 1280 + 0.4);
      const speed = 0.012 * depth;
      const x = (r() * w + Math.sin(t * 0.0003 + i) * 30) % w;
      const y = h + 60 - ((r() * (h + 120) + t * speed) % (h + 120));
      brick(ctx, x, y, s, colors[i % colors.length]!, Math.sin(t * 0.0004 + i) * 0.6, 0.18 + depth * 0.35);
    }
  },
  aurora(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    ctx.globalCompositeOperation = 'lighter';
    const bands = 3 + Math.round(q * 2);
    for (let b = 0; b < bands; b++) {
      const c = b % 2 ? p.accent2 : p.accent;
      ctx.beginPath();
      const base = h * (0.25 + b * 0.09);
      ctx.moveTo(0, h);
      for (let x = 0; x <= w; x += 12) {
        const y = base + Math.sin(x * 0.004 + t * 0.0004 * (1 + b * 0.3)) * 40 + Math.sin(x * 0.011 - t * 0.0006) * 16;
        ctx.lineTo(x, y);
      }
      ctx.lineTo(w, h);
      ctx.closePath();
      const g = ctx.createLinearGradient(0, base - 60, 0, base + 220);
      g.addColorStop(0, rgba(c, 0.16));
      g.addColorStop(1, rgba(c, 0));
      ctx.fillStyle = g;
      ctx.fill();
    }
    ctx.globalCompositeOperation = 'source-over';
  },
  grid(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const horizon = h * 0.55;
    const sun = ctx.createRadialGradient(w / 2, horizon, 0, w / 2, horizon, h * 0.5);
    sun.addColorStop(0, rgba(p.accent, 0.28));
    sun.addColorStop(1, rgba(p.accent, 0));
    ctx.fillStyle = sun;
    ctx.fillRect(0, 0, w, h);
    ctx.strokeStyle = rgba(p.accent2, 0.35);
    ctx.lineWidth = 1;
    const lines = 14 + Math.round(q * 10);
    for (let i = -lines; i <= lines; i++) {
      ctx.beginPath();
      ctx.moveTo(w / 2 + i * 8, horizon);
      ctx.lineTo(w / 2 + i * (w / lines), h);
      ctx.stroke();
    }
    const off = (t * 0.00025) % 1;
    for (let i = 0; i < 16; i++) {
      const z = (i + off) / 16;
      const y = horizon + (h - horizon) * z * z;
      ctx.strokeStyle = rgba(p.accent, 0.08 + z * 0.4);
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();
    }
  },
  constellation(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const r = rng(3);
    const n = Math.round(30 + 60 * q);
    const pts: [number, number][] = [];
    for (let i = 0; i < n; i++) {
      const x = (r() * w + Math.sin(t * 0.0002 + i) * 40 + w) % w;
      const y = (r() * h + Math.cos(t * 0.00017 + i * 1.3) * 40 + h) % h;
      pts.push([x, y]);
    }
    const max = 130 * (w / 1280 + 0.3);
    for (let i = 0; i < n; i++) {
      for (let j = i + 1; j < n; j++) {
        const dx = pts[i]![0] - pts[j]![0], dy = pts[i]![1] - pts[j]![1];
        const d = Math.hypot(dx, dy);
        if (d < max) {
          ctx.strokeStyle = rgba(i % 3 ? p.accent : p.accent2, (1 - d / max) * 0.35);
          ctx.beginPath();
          ctx.moveTo(pts[i]![0], pts[i]![1]);
          ctx.lineTo(pts[j]![0], pts[j]![1]);
          ctx.stroke();
        }
      }
    }
    ctx.fillStyle = rgba(p.text, 0.8);
    for (const [x, y] of pts) ctx.fillRect(x - 1, y - 1, 2, 2);
  },
  warp(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const r = rng(11);
    const n = Math.round(80 + 220 * q);
    const cx = w / 2, cy = h * 0.45;
    for (let i = 0; i < n; i++) {
      const a = r() * Math.PI * 2;
      const z = (r() + t * 0.00008 * (0.5 + r())) % 1;
      const d = z * z * Math.max(w, h) * 0.75;
      const len = 4 + z * 26;
      ctx.strokeStyle = rgba(i % 4 ? p.text : p.accent, z * 0.7);
      ctx.lineWidth = 0.5 + z * 1.5;
      ctx.beginPath();
      ctx.moveTo(cx + Math.cos(a) * d, cy + Math.sin(a) * d);
      ctx.lineTo(cx + Math.cos(a) * (d + len), cy + Math.sin(a) * (d + len));
      ctx.stroke();
    }
  },
  waves(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const layers = 4 + Math.round(q * 3);
    for (let l = 0; l < layers; l++) {
      ctx.beginPath();
      const base = h * (0.55 + l * 0.07);
      ctx.moveTo(0, h);
      for (let x = 0; x <= w; x += 10) ctx.lineTo(x, base + Math.sin(x * 0.006 + t * 0.0005 + l) * (18 + l * 4));
      ctx.lineTo(w, h);
      ctx.closePath();
      ctx.fillStyle = rgba(l % 2 ? p.accent2 : p.accent, 0.05 + l * 0.015);
      ctx.fill();
    }
  },
  voxels(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    const size = 26 - Math.round(q * 8);
    const cols = Math.ceil(w / size), rows = Math.ceil(h / size);
    for (let y = 0; y < rows; y++) {
      for (let x = 0; x < cols; x++) {
        const v = Math.sin(x * 0.35 + t * 0.0006) + Math.cos(y * 0.3 - t * 0.0004) + Math.sin((x + y) * 0.15 + t * 0.0003);
        if (v < 1.4) continue;
        ctx.fillStyle = rgba((x + y) % 2 ? p.accent : p.accent2, Math.min(0.28, (v - 1.4) * 0.25));
        ctx.fillRect(x * size + 1, y * size + 1, size - 2, size - 2);
      }
    }
  },
  haunted(ctx, w, h, t, p, q) {
    DRAW.plain!(ctx, w, h, t, p, q);
    // fog
    ctx.globalCompositeOperation = 'lighter';
    for (let i = 0; i < 3; i++) {
      const fx = w * (0.3 + i * 0.25 + Math.sin(t * 0.00008 + i * 2) * 0.15);
      const fy = h * (0.4 + Math.cos(t * 0.00006 + i) * 0.1);
      const fr = Math.max(w, h) * (0.35 + 0.08 * Math.sin(t * 0.0001 + i));
      const fg = ctx.createRadialGradient(fx, fy, 0, fx, fy, fr);
      fg.addColorStop(0, rgba(i % 2 ? p.accent2 : p.accent, 0.08));
      fg.addColorStop(1, rgba(i % 2 ? p.accent2 : p.accent, 0));
      ctx.fillStyle = fg;
      ctx.fillRect(0, 0, w, h);
    }
    ctx.globalCompositeOperation = 'source-over';
    // spider webs
    spiderWeb(ctx, 0, 0, Math.min(w, h) * 0.18, p.text, 0.07);
    spiderWeb(ctx, w, 0, Math.min(w, h) * 0.14, p.text, 0.05);
    // pumpkins
    const pr = rng(31);
    const pumpkins = [[0.15, 0.55], [0.5, 0.4], [0.82, 0.6]] as const;
    for (const [px, py] of pumpkins) {
      const bob = Math.sin(t * 0.0008 + px * 10) * 8;
      const gAlpha = 0.5 + 0.2 * Math.sin(t * 0.0015 + px * 5);
      pumpkinShape(ctx, w * px, h * py + bob, 18 + pr() * 10, p.accent, '#ffaa00', gAlpha * 0.3);
    }
    // bats
    const br = rng(666);
    const nb = Math.round(4 + 6 * q);
    for (let i = 0; i < nb; i++) {
      const bx = (br() * w + t * (0.02 + br() * 0.03)) % (w + 80) - 40;
      const by = h * (0.1 + br() * 0.35) + Math.sin(t * 0.002 + i * 1.7) * 30;
      batShape(ctx, bx, by, 10 + br() * 8, p.text, t * 0.008 + i * 2, 0.2 + br() * 0.15);
    }
    // embers
    const er = rng(13);
    const ne = Math.round(15 + 30 * q);
    for (let i = 0; i < ne; i++) {
      const ex = er() * w;
      const ey = h - ((er() * h + t * (0.01 + er() * 0.02)) % (h + 20));
      const ea = 0.15 + 0.25 * Math.abs(Math.sin(t * 0.002 + i));
      ctx.fillStyle = rgba(i % 3 === 0 ? '#4ade80' : p.accent, ea);
      ctx.fillRect(ex, ey, 1.6, 1.6);
    }
  },
};

Object.assign(DRAW, EXTRA_DRAW);

/** Draws a single frame into any canvas (used for the picker previews). */
export function drawPreview(canvas: HTMLCanvasElement, id: string, t = 12_000): void {
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  const dpr = window.devicePixelRatio || 1;
  const w = canvas.clientWidth || 200, h = canvas.clientHeight || 120;
  canvas.width = Math.round(w * dpr);
  canvas.height = Math.round(h * dpr);
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  (DRAW[id] ?? DRAW.plain!)(ctx, w, h, t, palette(), 0.6);
}

interface Flake { x: number; y: number; r: number; s: number; d: number }

export interface BackgroundConfig { id: string; quality: number; snow: boolean; still: boolean; fx?: DesignFx; image?: string }

/** Full-window background: a 3D scene when possible, otherwise a 2D style. Snow is a 2D overlay on top. */
export class Background {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private canvas3d: HTMLCanvasElement;
  private bg3d: Bg3D | null = null;
  private use3d = false;
  private id = 'graveyard';
  private quality = 0.6;
  private snow = false;
  private still = false;
  private fx: DesignFx = { ...DEFAULT_FX };
  private image: HTMLImageElement | null = null;
  private imageSrc = '';
  private raf = 0;
  private pal = palette();
  private flakes: Flake[] = [];
  private last = 0;
  private resizeQueued = false;
  private cfg: BackgroundConfig | null = null;

  constructor(canvas: HTMLCanvasElement, canvas3d: HTMLCanvasElement) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d')!;
    this.canvas3d = canvas3d;
    // Debounced to one resize per frame; also catches DPR changes (moving to another monitor).
    const queue = () => {
      if (this.resizeQueued) return;
      this.resizeQueued = true;
      requestAnimationFrame(() => { this.resizeQueued = false; this.resize(); this.bg3d?.resize(); });
    };
    window.addEventListener('resize', queue);
    const watchDpr = () => matchMedia(`(resolution: ${devicePixelRatio}dppx)`).addEventListener('change', () => { queue(); watchDpr(); }, { once: true });
    watchDpr();
    document.addEventListener('visibilitychange', () => (document.hidden ? this.stop() : this.start()));
    canvas3d.addEventListener('bg3d-lost', () => { if (this.cfg) this.configure(this.cfg); });
  }

  configure(o: BackgroundConfig): void {
    this.cfg = o;
    const wanted = DRAW[o.id] || is3D(o.id) || o.id === 'image' ? o.id : 'graveyard';
    this.quality = Math.max(0, Math.min(1, o.quality));
    this.snow = o.snow;
    this.still = o.still;
    this.fx = o.fx ?? { ...DEFAULT_FX };
    this.pal = palette();
    if (o.image !== undefined && o.image !== this.imageSrc) {
      this.imageSrc = o.image;
      this.image = null;
      if (o.image) { const img = new Image(); img.onload = () => { this.image = img; if (this.still) this.frame(performance.now()); }; img.src = o.image; }
    }
    this.use3d = false;
    if (is3D(wanted)) {
      try {
        this.bg3d ??= new Bg3D(this.canvas3d);
        if (!this.bg3d.lost) {
          this.bg3d.configure({ id: wanted, quality: this.quality, fx: this.fx, still: this.still, pal: { bg: this.pal.bg, accent: this.pal.accent, accent2: this.pal.accent2 } });
          this.use3d = true;
        }
      } catch (e) {
        console.warn('3D background unavailable, using 2D', e);
      }
    }
    this.id = this.use3d ? wanted : is3D(wanted) ? FALLBACK_2D[wanted]! : wanted;
    this.canvas3d.hidden = !this.use3d;
    if (!this.use3d) this.bg3d?.stop();
    this.canvas.hidden = this.use3d && !this.snow;
    document.body.dataset.bg = this.id;
    this.resize();
    this.start();
  }

  setLowPower(on: boolean): void {
    this.bg3d?.setLowPower(on);
  }

  /** Internal 3D resolution and tier, or null in 2D mode. */
  renderInfo(): { w: number; h: number; tier: string } | null {
    return this.use3d && this.bg3d ? this.bg3d.resolution() : null;
  }

  private resize(): void {
    // Quality controls the internal resolution: 50 %..100 % of device pixels.
    const scale = (window.devicePixelRatio || 1) * (0.75 + this.quality * 0.25);
    this.canvas.width = Math.round(innerWidth * scale);
    this.canvas.height = Math.round(innerHeight * scale);
    this.ctx.setTransform(scale, 0, 0, scale, 0, 0);
    if (this.flakes.length === 0 || Math.abs(this.flakes.length - Math.round(40 + this.quality * 160)) > 1) {
      const n = Math.round(40 + this.quality * 160);
      const r = rng(99);
      this.flakes = Array.from({ length: n }, () => ({ x: r() * innerWidth, y: r() * innerHeight, r: 0.6 + r() * 2.2, s: 0.2 + r() * 0.8, d: r() * Math.PI * 2 }));
    } else {
      for (const f of this.flakes) { f.x = Math.min(f.x, innerWidth); f.y = Math.min(f.y, innerHeight); }
    }
    if (this.still) this.frame(performance.now());
  }

  private drawImage(w: number, h: number, t: number): void {
    const c = this.ctx;
    c.fillStyle = this.pal.bg;
    c.fillRect(0, 0, w, h);
    const img = this.image;
    if (!img) return;
    // Slow "Ken Burns" drift for depth.
    const k = 1.08 + Math.sin(t * 0.00005 * this.fx.speed) * 0.04;
    const s = Math.max(w / img.width, h / img.height) * k;
    const dx = (w - img.width * s) / 2 + Math.sin(t * 0.00007 * this.fx.speed) * w * 0.02;
    const dy = (h - img.height * s) / 2 + Math.cos(t * 0.00006 * this.fx.speed) * h * 0.02;
    c.globalAlpha = 0.55 + this.fx.light * 0.25;
    c.drawImage(img, dx, dy, img.width * s, img.height * s);
    c.globalAlpha = 1;
  }

  private frame = (t: number): void => {
    const dt = Math.min(64, t - (this.last || t)) * this.fx.speed;
    this.last = t;
    const w = innerWidth, h = innerHeight;
    if (this.use3d) this.ctx.clearRect(0, 0, w, h);
    else if (this.id === 'image') this.drawImage(w, h, this.still ? 0 : t);
    else (DRAW[this.id] ?? DRAW.plain!)(this.ctx, w, h, this.still ? 12_000 : t * this.fx.speed, this.pal, this.quality * (0.4 + this.fx.particles * 0.6));
    if (this.snow) {
      this.ctx.fillStyle = 'rgba(255,255,255,.85)';
      for (const f of this.flakes) {
        if (!this.still) {
          f.y += f.s * dt * 0.05;
          f.x += Math.sin(t * 0.001 + f.d) * 0.3;
          if (f.y > h + 4) { f.y = -4; f.x = Math.random() * w; }
        }
        this.ctx.globalAlpha = 0.35 + f.r / 4;
        this.ctx.beginPath();
        this.ctx.arc(f.x, f.y, f.r, 0, Math.PI * 2);
        this.ctx.fill();
      }
      this.ctx.globalAlpha = 1;
    }
    // In 3D mode the 2D loop only runs for the snow overlay.
    if (!this.still && (!this.use3d || this.snow)) this.raf = requestAnimationFrame(this.frame);
  };

  start(): void {
    this.stop();
    if (document.hidden) return;
    if (this.use3d) this.bg3d?.start();
    if (this.still) this.frame(performance.now());
    else if (!this.use3d || this.snow) this.raf = requestAnimationFrame(this.frame);
  }

  stop(): void {
    cancelAnimationFrame(this.raf);
    this.raf = 0;
    this.last = 0;
    this.bg3d?.stop();
  }
}
