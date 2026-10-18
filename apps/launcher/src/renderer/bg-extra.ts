// More 2D backgrounds beyond Halloween (round 7): sakura, underwater, synthwave,
// space, fireflies, rainy city, LEGO skyline, snowy mountains, lava lamp.
// Same contract as the scenes in bg.ts: pure functions of time, palette and quality.

interface Palette { bg: string; bg2: string; accent: string; accent2: string; text: string }
type Draw = (ctx: CanvasRenderingContext2D, w: number, h: number, t: number, p: Palette, q: number) => void;

function rgba(hex: string, a: number): string {
  const m = /^#?([0-9a-f]{6})$/i.exec(hex.trim());
  if (!m) return `rgba(255,255,255,${a})`;
  const n = parseInt(m[1]!, 16);
  return `rgba(${(n >> 16) & 255},${(n >> 8) & 255},${n & 255},${a})`;
}

function rng(seed: number): () => number {
  let s = seed >>> 0;
  return () => ((s = (s * 1664525 + 1013904223) >>> 0) / 4294967296);
}

function sky(ctx: CanvasRenderingContext2D, w: number, h: number, top: string, bottom: string): void {
  const g = ctx.createLinearGradient(0, 0, 0, h);
  g.addColorStop(0, top);
  g.addColorStop(1, bottom);
  ctx.fillStyle = g;
  ctx.fillRect(0, 0, w, h);
}

function stars(ctx: CanvasRenderingContext2D, w: number, h: number, t: number, n: number, seed: number, maxY = 1): void {
  const r = rng(seed);
  for (let i = 0; i < n; i++) {
    const x = r() * w, y = r() * h * maxY, s = 0.6 + r() * 1.4;
    ctx.fillStyle = `rgba(255,255,255,${0.25 + 0.6 * Math.abs(Math.sin(t * 0.0012 + i * 1.7))})`;
    ctx.fillRect(x, y, s, s);
  }
}

export const EXTRA_BACKGROUNDS: { id: string; name: { de: string; en: string } }[] = [
  { id: 'galaxy', name: { de: 'Galaxie', en: 'Galaxy' } },
  { id: 'sakura', name: { de: 'Kirschblüten', en: 'Cherry blossoms' } },
  { id: 'underwater', name: { de: 'Unterwasser', en: 'Underwater' } },
  { id: 'synthwave', name: { de: 'Synthwave-Sonne', en: 'Synthwave sun' } },
  { id: 'space', name: { de: 'Weltall', en: 'Outer space' } },
  { id: 'fireflies', name: { de: 'Glühwürmchen', en: 'Fireflies' } },
  { id: 'rain', name: { de: 'Regen in der Stadt', en: 'Rainy city' } },
  { id: 'legocity', name: { de: 'LEGO-Stadt', en: 'LEGO city' } },
  { id: 'mountains', name: { de: 'Schneeberge', en: 'Snowy mountains' } },
  { id: 'lavalamp', name: { de: 'Lavalampe', en: 'Lava lamp' } },
];

export const EXTRA_DRAW: Record<string, Draw> = {
  galaxy(ctx, w, h, t, p, q) {
    sky(ctx, w, h, '#05030f', '#0a0620');
    // Nebula clouds in the theme colours plus magenta and cyan.
    ctx.globalCompositeOperation = 'lighter';
    const clouds = [[0.22, 0.3, '#7c3aed'], [0.78, 0.25, p.accent], [0.6, 0.75, '#db2777'], [0.15, 0.8, '#0ea5e9'], [0.5, 0.45, p.accent2]] as const;
    clouds.forEach(([cx, cy, c], i) => {
      const x = w * (cx + Math.sin(t * 0.00006 + i) * 0.04), y = h * (cy + Math.cos(t * 0.00005 + i * 2) * 0.04);
      const r = Math.max(w, h) * (0.36 + 0.04 * Math.sin(t * 0.0002 + i));
      const g = ctx.createRadialGradient(x, y, 0, x, y, r);
      g.addColorStop(0, rgba(c, 0.16));
      g.addColorStop(0.5, rgba(c, 0.06));
      g.addColorStop(1, rgba(c, 0));
      ctx.fillStyle = g;
      ctx.fillRect(0, 0, w, h);
    });
    // Slowly turning spiral galaxy.
    const gx = w * 0.62, gy = h * 0.42, R = Math.min(w, h) * 0.42, rot = t * 0.000025;
    const core = ctx.createRadialGradient(gx, gy, 0, gx, gy, R * 0.28);
    core.addColorStop(0, 'rgba(255,240,255,0.55)');
    core.addColorStop(0.4, rgba(p.accent2, 0.25));
    core.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = core;
    ctx.fillRect(gx - R, gy - R, R * 2, R * 2);
    const r = rng(101);
    const n = Math.round(700 + 1500 * q);
    for (let i = 0; i < n; i++) {
      const arm = i % 3, d = Math.pow(r(), 0.7);
      const a = arm * (Math.PI * 2 / 3) + d * 5.2 + rot * (1.6 - d) + (r() - 0.5) * 0.55;
      const x = gx + Math.cos(a) * d * R, y = gy + Math.sin(a) * d * R * 0.48;
      const tw = 0.35 + 0.65 * Math.abs(Math.sin(t * 0.0015 + i));
      ctx.fillStyle = i % 9 === 0 ? rgba(p.accent, 0.8 * tw) : i % 7 === 0 ? `rgba(255,170,230,${0.7 * tw})` : `rgba(230,235,255,${(0.35 + (1 - d) * 0.5) * tw})`;
      const s = d < 0.15 ? 1.6 : r() < 0.06 ? 1.8 : 1;
      ctx.fillRect(x, y, s, s);
    }
    ctx.globalCompositeOperation = 'source-over';
    stars(ctx, w, h, t, Math.round(150 + 200 * q), 77);
    // An occasional shooting star.
    const ph = (t % 9000) / 9000;
    if (ph < 0.12) {
      const k = ph / 0.12, sx = w * (0.1 + k * 0.5), sy = h * (0.08 + k * 0.25);
      const g = ctx.createLinearGradient(sx - 120, sy - 60, sx, sy);
      g.addColorStop(0, 'rgba(255,255,255,0)');
      g.addColorStop(1, `rgba(255,255,255,${0.8 * (1 - k)})`);
      ctx.strokeStyle = g;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(sx - 120, sy - 60);
      ctx.lineTo(sx, sy);
      ctx.stroke();
    }
  },
  sakura(ctx, w, h, t, p, q) {
    sky(ctx, w, h, '#1b0f24', '#3a1730');
    const g = ctx.createRadialGradient(w * 0.75, h * 0.25, 0, w * 0.75, h * 0.25, h * 0.6);
    g.addColorStop(0, 'rgba(255,170,200,0.22)');
    g.addColorStop(1, 'rgba(255,170,200,0)');
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, w, h);
    const r = rng(11);
    const n = Math.round(30 + 70 * q);
    for (let i = 0; i < n; i++) {
      const depth = 0.4 + r() * 0.6, sp = 0.02 + r() * 0.03;
      const y = ((r() * h + t * sp * depth) % (h + 40)) - 20;
      const x = (r() * w + Math.sin(t * 0.0008 + i) * 40 + t * 0.01 * depth) % (w + 40) - 20;
      const s = (4 + r() * 5) * depth, rot = t * 0.002 + i;
      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(rot);
      ctx.fillStyle = i % 4 ? `rgba(255,${170 + (i % 40)},${200 + (i % 30)},${0.45 + depth * 0.4})` : rgba(p.accent, 0.5);
      ctx.beginPath();
      ctx.ellipse(0, 0, s, s * 0.55, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    }
  },
  underwater(ctx, w, h, t, _p, q) {
    sky(ctx, w, h, '#06364a', '#020c18');
    ctx.globalCompositeOperation = 'lighter';
    for (let i = 0; i < 6; i++) {
      const x = w * (0.1 + i * 0.17) + Math.sin(t * 0.0003 + i) * 40;
      const g = ctx.createLinearGradient(x, 0, x + 120, h);
      g.addColorStop(0, 'rgba(120,220,255,0.10)');
      g.addColorStop(1, 'rgba(120,220,255,0)');
      ctx.fillStyle = g;
      ctx.beginPath();
      ctx.moveTo(x - 30, 0); ctx.lineTo(x + 40, 0); ctx.lineTo(x + 220, h); ctx.lineTo(x + 60, h);
      ctx.fill();
    }
    ctx.globalCompositeOperation = 'source-over';
    const r = rng(5);
    const n = Math.round(20 + 50 * q);
    for (let i = 0; i < n; i++) {
      const sp = 0.02 + r() * 0.04, s = 1.5 + r() * 4;
      const y = h - ((r() * h + t * sp) % (h + 20));
      const x = r() * w + Math.sin(t * 0.002 + i) * 6;
      ctx.strokeStyle = `rgba(200,240,255,${0.25 + r() * 0.35})`;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.arc(x, y, s, 0, Math.PI * 2);
      ctx.stroke();
    }
  },
  synthwave(ctx, w, h, t, p) {
    sky(ctx, w, h, '#120024', '#3b0a45');
    stars(ctx, w, h, t, 80, 3, 0.5);
    const cx = w * 0.5, cy = h * 0.58, R = Math.min(w, h) * 0.22;
    const g = ctx.createLinearGradient(0, cy - R, 0, cy + R);
    g.addColorStop(0, '#ffd319');
    g.addColorStop(0.5, '#ff2975');
    g.addColorStop(1, '#8c1eff');
    ctx.fillStyle = g;
    ctx.beginPath();
    ctx.arc(cx, cy, R, Math.PI, 0);
    ctx.fill();
    ctx.fillStyle = '#3b0a45';
    for (let i = 0; i < 6; i++) ctx.fillRect(cx - R, cy - R * 0.5 + i * R * 0.12, R * 2, 2 + i * 1.6);
    const horizon = cy;
    ctx.fillStyle = '#12001f';
    ctx.fillRect(0, horizon, w, h - horizon);
    ctx.strokeStyle = rgba(p.accent, 0.55);
    ctx.lineWidth = 1.2;
    for (let i = -20; i <= 20; i++) {
      ctx.beginPath();
      ctx.moveTo(cx + i * 30, horizon);
      ctx.lineTo(cx + i * 160, h);
      ctx.stroke();
    }
    const off = (t * 0.04) % 40;
    for (let y = 0; y < h - horizon; y += 40) {
      const yy = horizon + Math.pow((y + off) / (h - horizon), 2) * (h - horizon);
      ctx.beginPath();
      ctx.moveTo(0, yy);
      ctx.lineTo(w, yy);
      ctx.stroke();
    }
  },
  space(ctx, w, h, t, p, q) {
    sky(ctx, w, h, '#02030a', '#070b1d');
    stars(ctx, w, h, t, Math.round(120 + 180 * q), 9);
    const neb = ctx.createRadialGradient(w * 0.3, h * 0.4, 0, w * 0.3, h * 0.4, w * 0.5);
    neb.addColorStop(0, rgba(p.accent2, 0.16));
    neb.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = neb;
    ctx.fillRect(0, 0, w, h);
    const px = w * 0.72, py = h * 0.6, pr = Math.min(w, h) * 0.18;
    const pg = ctx.createRadialGradient(px - pr * 0.4, py - pr * 0.4, pr * 0.1, px, py, pr);
    pg.addColorStop(0, rgba(p.accent, 1));
    pg.addColorStop(1, '#0a0f2a');
    ctx.fillStyle = pg;
    ctx.beginPath();
    ctx.arc(px, py, pr, 0, Math.PI * 2);
    ctx.fill();
    ctx.strokeStyle = 'rgba(230,220,255,0.45)';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.ellipse(px, py, pr * 1.7, pr * 0.38, -0.3, 0, Math.PI * 2);
    ctx.stroke();
    const mx = px + Math.cos(t * 0.0002) * pr * 2.4, my = py + Math.sin(t * 0.0002) * pr * 0.6;
    ctx.fillStyle = '#cfd4e6';
    ctx.beginPath();
    ctx.arc(mx, my, pr * 0.14, 0, Math.PI * 2);
    ctx.fill();
  },
  fireflies(ctx, w, h, t, _p, q) {
    sky(ctx, w, h, '#06140f', '#0b2418');
    ctx.fillStyle = '#04100a';
    for (let i = 0; i < 14; i++) {
      const x = (i / 13) * w, th = h * (0.35 + ((i * 37) % 10) / 30);
      ctx.beginPath();
      ctx.moveTo(x - 50, h); ctx.lineTo(x, h - th); ctx.lineTo(x + 50, h);
      ctx.fill();
    }
    ctx.globalCompositeOperation = 'lighter';
    const r = rng(23);
    const n = Math.round(25 + 50 * q);
    for (let i = 0; i < n; i++) {
      const x = r() * w + Math.sin(t * 0.0007 + i) * 30, y = h * (0.3 + r() * 0.65) + Math.cos(t * 0.0009 + i * 2) * 20;
      const a = Math.max(0, Math.sin(t * 0.002 + i * 3)) * 0.9;
      const g = ctx.createRadialGradient(x, y, 0, x, y, 10);
      g.addColorStop(0, `rgba(230,255,120,${a})`);
      g.addColorStop(1, 'rgba(230,255,120,0)');
      ctx.fillStyle = g;
      ctx.fillRect(x - 10, y - 10, 20, 20);
    }
    ctx.globalCompositeOperation = 'source-over';
  },
  rain(ctx, w, h, t, p, q) {
    sky(ctx, w, h, '#0b0f1c', '#18142a');
    const r = rng(31);
    for (let i = 0; i < 26; i++) {
      const bw = 30 + r() * 70, bh = h * (0.2 + r() * 0.5), x = (i / 26) * w;
      ctx.fillStyle = '#0a0c16';
      ctx.fillRect(x, h - bh, bw, bh);
      for (let k = 0; k < 10; k++) {
        if (r() < 0.45) {
          ctx.fillStyle = r() < 0.5 ? rgba(p.accent, 0.55) : 'rgba(255,210,130,0.5)';
          ctx.fillRect(x + 4 + r() * (bw - 10), h - bh + 8 + r() * (bh - 16), 3, 4);
        }
      }
    }
    ctx.strokeStyle = 'rgba(170,190,255,0.35)';
    ctx.lineWidth = 1;
    const n = Math.round(80 + 200 * q);
    const rr = rng(77);
    for (let i = 0; i < n; i++) {
      const sp = 0.6 + rr() * 0.6, x = (rr() * w + t * 0.05) % w, y = (rr() * h + t * sp) % h;
      ctx.beginPath();
      ctx.moveTo(x, y);
      ctx.lineTo(x - 3, y + 14);
      ctx.stroke();
    }
  },
  legocity(ctx, w, h, t, _p) {
    sky(ctx, w, h, '#0f2a5a', '#ff9a5a');
    const colors = ['#e3000b', '#f2cd37', '#0055bf', '#4b9f4a', '#ffffff', '#a0a5a9'];
    const r = rng(14);
    let x = 0;
    while (x < w) {
      const bw = 50 + Math.floor(r() * 4) * 20, bh = h * (0.18 + r() * 0.45), c = colors[Math.floor(r() * colors.length)]!;
      ctx.fillStyle = c;
      ctx.fillRect(x, h - bh, bw - 4, bh);
      for (let s = 0; s < bw - 14; s += 16) {
        ctx.fillRect(x + 4 + s, h - bh - 5, 9, 5);
      }
      ctx.fillStyle = 'rgba(0,0,0,0.22)';
      for (let yy = h - bh + 12; yy < h - 10; yy += 22) for (let xx = x + 8; xx < x + bw - 14; xx += 18) ctx.fillRect(xx, yy, 9, 11);
      x += bw;
    }
    const cx = (t * 0.03) % (w + 200) - 100;
    ctx.fillStyle = 'rgba(255,255,255,0.75)';
    ctx.beginPath();
    ctx.ellipse(cx, h * 0.18, 60, 16, 0, 0, Math.PI * 2);
    ctx.ellipse(cx + 40, h * 0.16, 40, 14, 0, 0, Math.PI * 2);
    ctx.fill();
  },
  mountains(ctx, w, h, t, _p, q) {
    sky(ctx, w, h, '#0b1a33', '#4a6a9a');
    stars(ctx, w, h, t, 60, 2, 0.4);
    const layers = [['#2c3e5c', 0.45], ['#1d2b44', 0.6], ['#121c2e', 0.75]] as const;
    layers.forEach(([c, base], li) => {
      ctx.fillStyle = c;
      ctx.beginPath();
      ctx.moveTo(0, h);
      for (let x = 0; x <= w; x += 20) {
        const y = h * base - Math.abs(Math.sin(x * 0.004 * (li + 1) + li)) * h * 0.22;
        ctx.lineTo(x, y);
      }
      ctx.lineTo(w, h);
      ctx.fill();
    });
    ctx.fillStyle = 'rgba(255,255,255,0.8)';
    const r = rng(8);
    const n = Math.round(40 + 120 * q);
    for (let i = 0; i < n; i++) {
      const y = (r() * h + t * (0.02 + r() * 0.03)) % h, x = (r() * w + Math.sin(t * 0.001 + i) * 20) % w;
      ctx.fillRect(x, y, 2, 2);
    }
  },
  lavalamp(ctx, w, h, t, p) {
    sky(ctx, w, h, '#14061f', '#1c0a14');
    ctx.globalCompositeOperation = 'lighter';
    const cols = [p.accent, p.accent2, '#ff4d6d', '#ffb703', '#3a86ff'];
    for (let i = 0; i < 7; i++) {
      const x = w * (0.15 + ((i * 0.13) % 0.75)) + Math.sin(t * 0.0003 + i) * 80;
      const y = h * (0.5 + 0.4 * Math.sin(t * 0.00025 * (1 + i * 0.2) + i * 1.3));
      const r = Math.min(w, h) * (0.12 + 0.05 * Math.sin(t * 0.0005 + i));
      const g = ctx.createRadialGradient(x, y, 0, x, y, r);
      g.addColorStop(0, rgba(cols[i % cols.length]!, 0.55));
      g.addColorStop(0.6, rgba(cols[i % cols.length]!, 0.18));
      g.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.fillStyle = g;
      ctx.fillRect(x - r, y - r, r * 2, r * 2);
    }
    ctx.globalCompositeOperation = 'source-over';
  },
};
