// Optional Halloween jumpscare (Optionen → Animationen, off by default): at most
// once per session, at a random moment a few minutes in, a blocky LEGO zombie
// head lunges at the screen with a scream. Never while signing in, installing
// or launching, never with reduced motion, never while a dialog is open.

let armed = false;
let fired = false;
let timer = 0;

export function armJumpscare(enabled: boolean, reducedMotion: boolean): void {
  const want = enabled && !reducedMotion && !fired;
  if (want === armed && (timer || !want)) return; // unchanged: keep the running timer
  clearTimeout(timer);
  timer = 0;
  armed = want;
  if (!armed) return;
  const delay = (2 + Math.random() * 6) * 60_000;
  timer = window.setTimeout(tryFire, delay);
}

function busy(): boolean {
  return !!document.querySelector('.intro3d, .vr-root, .play-group.busy, dialog[open]') || document.hidden;
}

function tryFire(): void {
  if (!armed || fired) return;
  if (busy()) { timer = window.setTimeout(tryFire, 20_000); return; }
  fired = true;
  scare();
}

/** Draws the zombie head (8×8 pixel face scaled up crisp). */
function face(size: number): HTMLCanvasElement {
  const px = [
    'GGGGGGGG',
    'GgGGGgGG',
    'GGGGGGGg',
    'GwRGGRwG',
    'GGGddGGG',
    'GgdddddG',
    'GdBdBdBG',
    'GGGGGgGG',
  ];
  const pal: Record<string, string> = { G: '#3f7d2b', g: '#2f5f20', w: '#e8e8d8', R: '#d10f0f', d: '#1b2b12', B: '#e8e8d8' };
  const c = document.createElement('canvas');
  c.width = c.height = size;
  const g = c.getContext('2d')!;
  const cell = size / 8;
  px.forEach((row, y) => [...row].forEach((k, x) => { g.fillStyle = pal[k]!; g.fillRect(Math.floor(x * cell), Math.floor(y * cell), Math.ceil(cell), Math.ceil(cell)); }));
  // LEGO stud on top-left edge for the brick look.
  g.fillStyle = 'rgba(255,255,255,.08)';
  g.fillRect(0, 0, size, cell * 0.5);
  return c;
}

function scream(): void {
  try {
    const ctx = new AudioContext();
    const t = ctx.currentTime;
    const master = ctx.createGain();
    master.gain.setValueAtTime(0.0001, t);
    master.gain.exponentialRampToValueAtTime(0.5, t + 0.03);
    master.gain.exponentialRampToValueAtTime(0.0001, t + 1.1);
    master.connect(ctx.destination);
    for (const f of [520, 740, 1180]) {
      const o = ctx.createOscillator();
      o.type = 'sawtooth';
      o.frequency.setValueAtTime(f, t);
      o.frequency.exponentialRampToValueAtTime(f * 0.45, t + 1);
      const vib = ctx.createOscillator();
      const vg = ctx.createGain();
      vib.frequency.value = 31;
      vg.gain.value = f * 0.06;
      vib.connect(vg).connect(o.frequency);
      o.connect(master);
      o.start(t); o.stop(t + 1.1);
      vib.start(t); vib.stop(t + 1.1);
    }
    const n = ctx.createBuffer(1, ctx.sampleRate, ctx.sampleRate);
    const d = n.getChannelData(0);
    for (let i = 0; i < d.length; i++) d[i] = Math.random() * 2 - 1;
    const s = ctx.createBufferSource();
    s.buffer = n;
    const ng = ctx.createGain();
    ng.gain.value = 0.25;
    s.connect(ng).connect(master);
    s.start(t);
    setTimeout(() => void ctx.close().catch(() => {}), 1500);
  } catch { /* no audio */ }
}

export function scare(): void {
  const root = document.createElement('div');
  root.className = 'jumpscare';
  root.setAttribute('aria-hidden', 'true');
  const img = face(512);
  root.append(img);
  document.body.append(root);
  scream();
  const close = () => root.remove();
  root.addEventListener('click', close);
  setTimeout(close, 1100);
}
