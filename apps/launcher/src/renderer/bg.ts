// Animated launcher backgrounds drawn on one full-window canvas. Colours come
// from the theme variables, quality scales resolution and particle counts,
// and animation stops when the window is hidden or motion is reduced.

export interface BackgroundDef { id: string; name: { de: string; en: string } }

export const BACKGROUNDS: BackgroundDef[] = [
  { id: 'nebula', name: { de: 'Nebel', en: 'Nebula' } },
  { id: 'bricks', name: { de: 'Schwebende Steine', en: 'Floating bricks' } },
  { id: 'aurora', name: { de: 'Polarlicht', en: 'Aurora' } },
  { id: 'grid', name: { de: 'Neon-Raster', en: 'Neon grid' } },
  { id: 'constellation', name: { de: 'Sternbild', en: 'Constellation' } },
  { id: 'warp', name: { de: 'Warp', en: 'Warp' } },
  { id: 'waves', name: { de: 'Wellen', en: 'Waves' } },
  { id: 'voxels', name: { de: 'Voxel-Feld', en: 'Voxel field' } },
  { id: 'plain', name: { de: 'Schlicht', en: 'Plain' } },
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
};

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

export class Background {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private id = 'nebula';
  private quality = 0.6;
  private snow = false;
  private still = false;
  private raf = 0;
  private pal = palette();
  private flakes: Flake[] = [];
  private last = 0;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d')!;
    window.addEventListener('resize', () => this.resize());
    document.addEventListener('visibilitychange', () => (document.hidden ? this.stop() : this.start()));
    this.resize();
  }

  configure(o: { id: string; quality: number; snow: boolean; still: boolean }): void {
    this.id = DRAW[o.id] ? o.id : 'nebula';
    this.quality = Math.max(0, Math.min(1, o.quality));
    this.snow = o.snow;
    this.still = o.still;
    this.pal = palette();
    this.resize();
    this.start();
  }

  private resize(): void {
    // Quality controls the internal resolution: 50 %..100 % of device pixels.
    const scale = (window.devicePixelRatio || 1) * (0.5 + this.quality * 0.5);
    this.canvas.width = Math.round(innerWidth * scale);
    this.canvas.height = Math.round(innerHeight * scale);
    this.ctx.setTransform(scale, 0, 0, scale, 0, 0);
    const n = Math.round(40 + this.quality * 160);
    const r = rng(99);
    this.flakes = Array.from({ length: n }, () => ({ x: r() * innerWidth, y: r() * innerHeight, r: 0.6 + r() * 2.2, s: 0.2 + r() * 0.8, d: r() * Math.PI * 2 }));
    if (this.still) this.frame(performance.now());
  }

  private frame = (t: number): void => {
    const dt = Math.min(64, t - (this.last || t));
    this.last = t;
    const w = innerWidth, h = innerHeight;
    (DRAW[this.id] ?? DRAW.plain!)(this.ctx, w, h, this.still ? 12_000 : t, this.pal, this.quality);
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
    if (!this.still) this.raf = requestAnimationFrame(this.frame);
  };

  start(): void {
    this.stop();
    if (document.hidden) return;
    if (this.still) this.frame(performance.now());
    else this.raf = requestAnimationFrame(this.frame);
  }

  stop(): void {
    cancelAnimationFrame(this.raf);
    this.raf = 0;
    this.last = 0;
  }
}
