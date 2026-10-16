// Halloween cinematic splash: LEGO bricks assemble in a spooky room.
// ~20 seconds, canvas-based, skippable via click or key.

function rgba(hex: string, a: number): string {
  const m = /^#?([0-9a-f]{6})$/i.exec(hex.trim());
  if (!m) return `rgba(255,255,255,${a})`;
  const n = parseInt(m[1]!, 16);
  return `rgba(${(n >> 16) & 255},${(n >> 8) & 255},${n & 255},${a})`;
}

function easeOutQuart(x: number): number { return 1 - (1 - x) ** 4; }
function easeInOutCubic(x: number): number { return x < 0.5 ? 4 * x ** 3 : 1 - (-2 * x + 2) ** 3 / 2; }
function lerp(a: number, b: number, t: number): number { return a + (b - a) * t; }
function clamp01(x: number): number { return Math.max(0, Math.min(1, x)); }

// Bitmap font for "LEGO" – each letter is 5 wide × 5 tall
const FONT: Record<string, string[]> = {
  L: ['#....', '#....', '#....', '#....', '#####'],
  E: ['#####', '#....', '####.', '#....', '#####'],
  G: ['#####', '#....', '#.###', '#..##', '#####'],
  O: ['#####', '#...#', '#...#', '#...#', '#####'],
};

interface Particle { x: number; y: number; vx: number; vy: number; life: number; max: number; color: string; size: number }
interface FlyingBrick { sx: number; sy: number; sr: number; tx: number; ty: number; t0: number; dur: number; color: string; size: number; landed: boolean }

const COLORS = ['#f97316', '#e3000b', '#f2cd37', '#9333ea', '#0055bf', '#4ade80'];

function buildBrickTargets(cx: number, cy: number, brickSize: number): FlyingBrick[] {
  const bricks: FlyingBrick[] = [];
  const word = 'LEGO';
  const gap = 1;
  const letterW = 5;
  const totalW = word.length * letterW + (word.length - 1) * gap;
  const startX = cx - (totalW * brickSize) / 2;
  const startY = cy - (5 * brickSize) / 2;
  let ci = 0;
  for (let li = 0; li < word.length; li++) {
    const letter = FONT[word[li]!]!;
    const lx = startX + li * (letterW + gap) * brickSize;
    for (let row = 0; row < 5; row++) {
      for (let col = 0; col < letterW; col++) {
        if (letter[row]![col] !== '#') continue;
        const angle = Math.random() * Math.PI * 2;
        const dist = 400 + Math.random() * 600;
        bricks.push({
          sx: cx + Math.cos(angle) * dist,
          sy: cy + Math.sin(angle) * dist,
          sr: Math.random() * Math.PI * 4 - Math.PI * 2,
          tx: lx + col * brickSize,
          ty: startY + row * brickSize,
          t0: 8000 + li * 1500 + (row * 5 + col) * 60 + Math.random() * 200,
          dur: 500 + Math.random() * 300,
          color: COLORS[ci++ % COLORS.length]!,
          size: brickSize,
          landed: false,
        });
      }
    }
  }
  return bricks;
}

function drawBrick(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, color: string, rot: number, alpha: number): void {
  ctx.save();
  ctx.translate(x + s / 2, y + s / 2);
  ctx.rotate(rot);
  ctx.globalAlpha = alpha;
  ctx.fillStyle = color;
  ctx.beginPath();
  (ctx as CanvasRenderingContext2D).roundRect(-s / 2, -s * 0.35, s, s * 0.7, s * 0.08);
  ctx.fill();
  ctx.fillStyle = 'rgba(255,255,255,.2)';
  ctx.fillRect(-s / 2 + s * 0.1, -s * 0.55, s * 0.25, s * 0.15);
  ctx.fillRect(s * 0.15, -s * 0.55, s * 0.25, s * 0.15);
  ctx.restore();
}

function drawBat(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, phase: number, alpha: number): void {
  ctx.save();
  ctx.translate(x, y);
  ctx.globalAlpha = alpha;
  ctx.fillStyle = '#1a1a2e';
  ctx.beginPath();
  ctx.arc(0, 0, s * 0.25, 0, Math.PI * 2);
  ctx.fill();
  const wy = Math.sin(phase) * s * 0.2;
  for (const d of [-1, 1]) {
    ctx.beginPath();
    ctx.moveTo(d * s * 0.18, 0);
    ctx.quadraticCurveTo(d * s * 0.6, -s * 0.45 + wy, d * s * 0.9, wy * 0.3);
    ctx.quadraticCurveTo(d * s * 0.45, s * 0.08 + wy * 0.2, d * s * 0.18, 0);
    ctx.fill();
  }
  ctx.restore();
}

function drawPumpkin(ctx: CanvasRenderingContext2D, x: number, y: number, s: number, glowAlpha: number): void {
  ctx.save();
  ctx.translate(x, y);
  ctx.globalAlpha = glowAlpha;
  ctx.fillStyle = '#f97316';
  ctx.beginPath();
  ctx.ellipse(0, 0, s, s * 0.78, 0, 0, Math.PI * 2);
  ctx.fill();
  const g = ctx.createRadialGradient(0, 0, 0, 0, 0, s * 1.8);
  g.addColorStop(0, rgba('#ff8c00', 0.25 * glowAlpha));
  g.addColorStop(1, rgba('#ff8c00', 0));
  ctx.globalAlpha = 1;
  ctx.fillStyle = g;
  ctx.fillRect(-s * 2, -s * 2, s * 4, s * 4);
  ctx.globalAlpha = glowAlpha;
  ctx.fillStyle = '#ffcc00';
  for (const sx of [-s * 0.25, s * 0.1]) {
    ctx.beginPath();
    ctx.moveTo(sx, -s * 0.1);
    ctx.lineTo(sx + s * 0.16, -s * 0.1);
    ctx.lineTo(sx + s * 0.08, -s * 0.3);
    ctx.closePath();
    ctx.fill();
  }
  ctx.beginPath();
  ctx.moveTo(-s * 0.28, s * 0.1);
  for (let i = 0; i < 5; i++) ctx.lineTo(-s * 0.28 + (i + 0.5) * s * 0.11, i % 2 ? s * 0.25 : s * 0.06);
  ctx.lineTo(s * 0.28, s * 0.1);
  ctx.fill();
  ctx.restore();
}

function drawWeb(ctx: CanvasRenderingContext2D, ox: number, oy: number, size: number, alpha: number, progress: number): void {
  ctx.save();
  ctx.globalAlpha = alpha * progress;
  ctx.strokeStyle = '#f5e6d0';
  ctx.lineWidth = 0.7;
  const rays = 8, rings = 5;
  const drawn = Math.floor(rays * progress);
  for (let r = 0; r < drawn; r++) {
    const a = (r / rays) * Math.PI * 0.5;
    ctx.beginPath();
    ctx.moveTo(ox, oy);
    ctx.lineTo(ox + Math.cos(a) * size, oy + Math.sin(a) * size);
    ctx.stroke();
  }
  const ringsDrawn = Math.floor(rings * progress);
  for (let ring = 1; ring <= ringsDrawn; ring++) {
    const d = (ring / rings) * size;
    ctx.beginPath();
    for (let r = 0; r <= drawn; r++) {
      const a = (Math.min(r, rays - 1) / rays) * Math.PI * 0.5;
      r === 0 ? ctx.moveTo(ox + Math.cos(a) * d, oy + Math.sin(a) * d) : ctx.lineTo(ox + Math.cos(a) * d, oy + Math.sin(a) * d);
    }
    ctx.stroke();
  }
  ctx.restore();
}

export function playSplash(): Promise<void> {
  return new Promise((resolve) => {
    const overlay = document.createElement('div');
    overlay.style.cssText = 'position:fixed;inset:0;z-index:100;background:#000;cursor:pointer;';
    const canvas = document.createElement('canvas');
    canvas.style.cssText = 'width:100%;height:100%;display:block;';
    overlay.appendChild(canvas);
    document.body.appendChild(overlay);

    const ctx = canvas.getContext('2d')!;
    let w = 0, h = 0;

    function resize() {
      const dpr = Math.min(2, devicePixelRatio || 1);
      w = innerWidth; h = innerHeight;
      canvas.width = Math.round(w * dpr);
      canvas.height = Math.round(h * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    resize();
    addEventListener('resize', resize);

    const bricks = buildBrickTargets(w / 2, h * 0.4, Math.round(Math.min(w, h) * 0.028));
    const particles: Particle[] = [];
    const pumpkins = [
      { x: 0.12, y: 0.6, s: 28, t0: 4000 },
      { x: 0.52, y: 0.48, s: 34, t0: 5200 },
      { x: 0.85, y: 0.58, s: 24, t0: 6400 },
    ];
    const bats = Array.from({ length: 6 }, (_, i) => ({
      startX: Math.random() * w, y: h * (0.08 + Math.random() * 0.3),
      speed: 0.015 + Math.random() * 0.025, phase: Math.random() * 6,
      s: 14 + Math.random() * 10, t0: 4500 + i * 600,
    }));
    const floorBricks = Array.from({ length: 12 }, () => ({
      x: Math.random() * w, y: h * (0.7 + Math.random() * 0.25),
      s: 8 + Math.random() * 14, rot: Math.random() * 2 - 1,
      color: COLORS[Math.floor(Math.random() * COLORS.length)]!,
    }));

    let start = 0;
    let skipped = false;
    let done = false;

    function cleanup() {
      if (done) return;
      done = true;
      removeEventListener('resize', resize);
      overlay.style.transition = 'opacity .6s ease';
      overlay.style.opacity = '0';
      setTimeout(() => { overlay.remove(); resolve(); }, 600);
    }

    overlay.addEventListener('click', () => { skipped = true; });
    overlay.addEventListener('keydown', () => { skipped = true; });
    overlay.tabIndex = 0;
    overlay.focus();

    function addParticles(x: number, y: number, count: number, color: string) {
      for (let i = 0; i < count; i++) {
        const a = Math.random() * Math.PI * 2;
        const spd = 0.5 + Math.random() * 2;
        particles.push({ x, y, vx: Math.cos(a) * spd, vy: Math.sin(a) * spd, life: 1, max: 40 + Math.random() * 30, color, size: 1 + Math.random() * 2 });
      }
    }

    function frame(now: number) {
      if (!start) start = now;
      const t = skipped ? 20000 : now - start;

      if (t >= 19500) { cleanup(); return; }

      ctx.clearRect(0, 0, w, h);

      // ---- overall fade ----
      const masterAlpha = t < 500 ? t / 500 : t > 18000 ? 1 - (t - 18000) / 1500 : 1;
      ctx.globalAlpha = clamp01(masterAlpha);

      // ---- dark background ----
      ctx.fillStyle = '#0a0808';
      ctx.fillRect(0, 0, w, h);

      // ---- camera zoom ----
      const zoom = t < 8000 ? lerp(1.12, 1, easeInOutCubic(clamp01(t / 8000))) : 1;
      ctx.save();
      ctx.translate(w / 2, h / 2);
      ctx.scale(zoom, zoom);
      ctx.translate(-w / 2, -h / 2);

      // ---- phase 1: central light (0-4s) ----
      const lightAlpha = clamp01(t / 3000) * 0.35;
      const lg = ctx.createRadialGradient(w / 2, h * 0.45, 0, w / 2, h * 0.45, Math.max(w, h) * 0.6);
      lg.addColorStop(0, rgba('#ff8c00', lightAlpha));
      lg.addColorStop(0.5, rgba('#9333ea', lightAlpha * 0.3));
      lg.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.fillStyle = lg;
      ctx.fillRect(0, 0, w, h);

      // ---- spider webs (1-4s) ----
      const webProg = clamp01((t - 1000) / 3000);
      if (webProg > 0) {
        drawWeb(ctx, 0, 0, Math.min(w, h) * 0.22, 0.12, webProg);
        ctx.save();
        ctx.translate(w, 0);
        ctx.scale(-1, 1);
        drawWeb(ctx, 0, 0, Math.min(w, h) * 0.17, 0.09, webProg);
        ctx.restore();
      }

      // ---- floor bricks (2-4s) ----
      const floorAlpha = clamp01((t - 2000) / 2000) * 0.35;
      if (floorAlpha > 0) {
        for (const fb of floorBricks) {
          drawBrick(ctx, fb.x, fb.y, fb.s, fb.color, fb.rot, floorAlpha);
        }
      }

      // ---- fog wisps ----
      ctx.globalCompositeOperation = 'lighter';
      for (let i = 0; i < 3; i++) {
        const fx = (w * 0.3 + i * w * 0.25 + t * 0.008) % (w + 400) - 200;
        const fy = h * (0.5 + i * 0.12);
        const fAlpha = clamp01(t / 4000) * 0.06;
        const fg = ctx.createRadialGradient(fx, fy, 0, fx, fy, 200 + i * 60);
        fg.addColorStop(0, rgba('#9333ea', fAlpha));
        fg.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = fg;
        ctx.fillRect(0, 0, w, h);
      }
      ctx.globalCompositeOperation = 'source-over';

      // ---- phase 2: pumpkins (4-8s) ----
      for (const pk of pumpkins) {
        const pa = clamp01((t - pk.t0) / 1200);
        if (pa > 0) {
          const pulse = 0.6 + 0.4 * Math.sin(t * 0.002 + pk.x * 10);
          const bob = Math.sin(t * 0.001 + pk.x * 7) * 6;
          drawPumpkin(ctx, w * pk.x, h * pk.y + bob, pk.s * easeOutQuart(pa), pa * pulse);
        }
      }

      // ---- bats (4.5s+) ----
      for (const bt of bats) {
        const ba = clamp01((t - bt.t0) / 800);
        if (ba > 0) {
          const bx = (bt.startX + t * bt.speed) % (w + 100) - 50;
          const by = bt.y + Math.sin(t * 0.003 + bt.phase) * 25;
          drawBat(ctx, bx, by, bt.s, t * 0.008 + bt.phase, ba * 0.5);
        }
      }

      // ---- ghost silhouettes (6s+) ----
      const ghostA = clamp01((t - 6000) / 2000) * 0.08;
      if (ghostA > 0) {
        for (let i = 0; i < 3; i++) {
          const gx = w * (0.2 + i * 0.3) + Math.sin(t * 0.0004 + i * 2) * 40;
          const gy = h * (0.3 + i * 0.08) + Math.cos(t * 0.0005 + i) * 20 - t * 0.003;
          ctx.fillStyle = rgba('#f5e6d0', ghostA);
          ctx.beginPath();
          ctx.ellipse(gx, gy, 20, 30, 0, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      // ---- rising embers ----
      const emberA = clamp01((t - 3000) / 2000);
      if (emberA > 0) {
        for (let i = 0; i < 25; i++) {
          const ex = (Math.sin(i * 7.3) * 0.5 + 0.5) * w;
          const ey = h - ((i * 73 + t * 0.015) % (h + 20));
          const ea = emberA * (0.2 + 0.3 * Math.abs(Math.sin(t * 0.003 + i)));
          ctx.fillStyle = rgba(i % 3 === 0 ? '#4ade80' : '#f97316', ea);
          ctx.fillRect(ex, ey, 1.8, 1.8);
        }
      }

      // ---- phase 3: brick assembly (8-14s) ----
      for (const b of bricks) {
        const bp = clamp01((t - b.t0) / b.dur);
        if (t < b.t0 - 500) continue; // not yet visible
        const e = easeOutQuart(bp);
        const x = lerp(b.sx, b.tx, e);
        const y = lerp(b.sy, b.ty, e);
        const rot = lerp(b.sr, 0, e);
        const alpha = bp < 1 ? clamp01((t - b.t0 + 500) / 500) : 1;
        drawBrick(ctx, x, y, b.size, b.color, rot, alpha);

        // particle burst on landing
        if (bp >= 1 && !b.landed) {
          b.landed = true;
          addParticles(b.tx + b.size / 2, b.ty + b.size / 2, 4, b.color);
        }
      }

      // ---- glow behind assembled text (12s+) ----
      const textGlow = clamp01((t - 12000) / 2000);
      if (textGlow > 0) {
        const gg = ctx.createRadialGradient(w / 2, h * 0.4, 0, w / 2, h * 0.4, Math.min(w, h) * 0.35);
        gg.addColorStop(0, rgba('#9333ea', textGlow * 0.2));
        gg.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = gg;
        ctx.fillRect(0, 0, w, h);
      }

      // ---- phase 4: "LAUNCHER" text (14-17s) ----
      const textA = clamp01((t - 14000) / 1500);
      if (textA > 0) {
        const fontSize = Math.round(Math.min(w * 0.04, 48));
        ctx.font = `900 ${fontSize}px Inter, "Segoe UI", system-ui, sans-serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        const textY = h * 0.4 + Math.min(w, h) * 0.028 * 3 + 20;
        ctx.letterSpacing = `${fontSize * 0.35}px`;
        // gradient fill
        const tg = ctx.createLinearGradient(w / 2 - 120, 0, w / 2 + 120, 0);
        tg.addColorStop(0, '#f97316');
        tg.addColorStop(1, '#9333ea');
        ctx.fillStyle = tg;
        ctx.globalAlpha = clamp01(masterAlpha) * easeOutQuart(textA);
        ctx.fillText('LAUNCHER', w / 2, textY);
        ctx.letterSpacing = '0px';

        // underline sweep
        const lineW = Math.min(w * 0.3, 300) * clamp01((t - 15000) / 1000);
        if (lineW > 0) {
          ctx.strokeStyle = rgba('#f97316', 0.5 * textA);
          ctx.lineWidth = 2;
          ctx.beginPath();
          ctx.moveTo(w / 2 - lineW, textY + fontSize + 10);
          ctx.lineTo(w / 2 + lineW, textY + fontSize + 10);
          ctx.stroke();
        }
      }

      // ---- particles ----
      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i]!;
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.02;
        p.life--;
        if (p.life <= 0) { particles.splice(i, 1); continue; }
        ctx.fillStyle = rgba(p.color, (p.life / p.max) * 0.7);
        ctx.fillRect(p.x, p.y, p.size, p.size);
      }

      // ---- dust motes ----
      const dustA = clamp01(t / 2000) * 0.15;
      for (let i = 0; i < 30; i++) {
        const dx = (Math.sin(i * 5.1 + t * 0.0001) * 0.5 + 0.5) * w;
        const dy = (Math.cos(i * 3.7 + t * 0.00008) * 0.5 + 0.5) * h;
        ctx.fillStyle = rgba('#f5e6d0', dustA * Math.abs(Math.sin(t * 0.001 + i)));
        ctx.fillRect(dx, dy, 1.2, 1.2);
      }

      ctx.restore(); // camera zoom

      // ---- skip hint ----
      if (t > 2000 && t < 18000) {
        ctx.globalAlpha = 0.3;
        ctx.font = '13px Inter, system-ui, sans-serif';
        ctx.textAlign = 'right';
        ctx.fillStyle = '#f5e6d0';
        ctx.fillText('Klicke zum Überspringen', w - 24, h - 20);
      }

      ctx.globalAlpha = 1;
      requestAnimationFrame(frame);
    }

    requestAnimationFrame(frame);
  });
}
