// 3D player character (skinview3d / three.js, WebGL) with a transparent
// background, animations and drag-to-rotate. Falls back to a flat 2D render
// of the skin when WebGL is not available.

import { SkinViewer, IdleAnimation, WalkingAnimation, WaveAnimation, type PlayerAnimation } from 'skinview3d';
import minifig from './assets/minifig.png';

export const DEFAULT_SKIN: string = minifig;
export type Anim = 'idle' | 'walk' | 'wave' | 'spin';

export interface Avatar {
  setSkin(skin: string | null, variant: 'classic' | 'slim', cape: string | null): Promise<void>;
  play(a: Anim): void;
  /** Freezes or resumes the animation ("Skin-Animation" switch). */
  setPaused(paused: boolean): void;
  resize(): void;
  dispose(): void;
}

function webglAvailable(): boolean {
  try {
    const c = document.createElement('canvas');
    return !!(c.getContext('webgl2') ?? c.getContext('webgl'));
  } catch {
    return false;
  }
}

export function createAvatar(canvas: HTMLCanvasElement, reducedMotion: boolean): Avatar {
  if (!webglAvailable()) return flatAvatar(canvas);
  // Create the context ourselves with alpha so the stage glow shows through.
  canvas.getContext('webgl2', { alpha: true, premultipliedAlpha: true, antialias: true });
  const box = () => canvas.parentElement!.getBoundingClientRect();
  const viewer = new SkinViewer({ canvas, width: box().width, height: box().height, pixelRatio: 'match-device', skin: DEFAULT_SKIN, model: 'default', zoom: 0.62, fov: 40 });
  viewer.renderer.setClearColor(0x000000, 0);
  // Three-quarter view like a character select screen.
  viewer.playerWrapper.rotation.y = 0.5;
  viewer.playerObject.position.y = -1;
  viewer.controls.enableZoom = false;
  viewer.controls.enablePan = false;
  viewer.globalLight.intensity = 2.6;
  viewer.cameraLight.intensity = 1.1;
  viewer.autoRotateSpeed = 1.2;
  let current: Anim = 'idle';
  const make = (a: Anim): PlayerAnimation => {
    if (a === 'walk') return new WalkingAnimation();
    if (a === 'wave') {
      const w = new WaveAnimation('right');
      w.speed = 0.9;
      return w;
    }
    return new IdleAnimation();
  };
  let paused = reducedMotion;
  const play = (a: Anim) => {
    current = a;
    viewer.animation = make(a);
    viewer.autoRotate = a === 'spin' && !paused;
    if (paused && viewer.animation) viewer.animation.paused = true;
  };
  play('idle');
  const ro = new ResizeObserver(() => { const b = box(); viewer.setSize(b.width, b.height); });
  ro.observe(canvas.parentElement!);
  return {
    async setSkin(skin, variant, cape) {
      await viewer.loadSkin(skin ?? DEFAULT_SKIN, { model: skin ? (variant === 'slim' ? 'slim' : 'default') : 'default' });
      if (cape) await viewer.loadCape(cape);
      else viewer.resetCape();
      play(current);
    },
    play,
    setPaused(p) {
      paused = p || reducedMotion;
      if (viewer.animation) viewer.animation.paused = paused;
      viewer.autoRotate = current === 'spin' && !paused;
    },
    resize() { const b = box(); viewer.setSize(b.width, b.height); },
    dispose() { ro.disconnect(); viewer.dispose(); },
  };
}

/** Front view of the skin drawn pixel-exact on a 2D canvas (no WebGL). */
function flatAvatar(canvas: HTMLCanvasElement): Avatar {
  const ctx = canvas.getContext('2d')!;
  let img: HTMLImageElement | null = null;
  const draw = () => {
    const b = canvas.parentElement!.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    canvas.width = Math.round(b.width * dpr);
    canvas.height = Math.round(b.height * dpr);
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (!img) return;
    ctx.imageSmoothingEnabled = false;
    const s = Math.floor((canvas.height * 0.82) / 32);
    const ox = Math.round(canvas.width / 2 - 8 * s), oy = Math.round(canvas.height * 0.08);
    const part = (sx: number, sy: number, w: number, h: number, dx: number, dy: number) => ctx.drawImage(img!, sx, sy, w, h, ox + dx * s, oy + dy * s, w * s, h * s);
    part(8, 8, 8, 8, 4, 0); part(40, 8, 8, 8, 4, 0);       // head + hat layer
    part(20, 20, 8, 12, 4, 8);                               // body
    part(44, 20, 4, 12, 0, 8); part(36, 52, 4, 12, 12, 8);  // arms
    part(4, 20, 4, 12, 4, 20); part(20, 52, 4, 12, 8, 20);  // legs
  };
  const load = (src: string) => new Promise<void>((resolve) => {
    const i = new Image();
    i.onload = () => { img = i; draw(); resolve(); };
    i.onerror = () => resolve();
    i.src = src;
  });
  void load(DEFAULT_SKIN);
  const ro = new ResizeObserver(draw);
  ro.observe(canvas.parentElement!);
  return {
    async setSkin(skin) { await load(skin ?? DEFAULT_SKIN); },
    play() { /* static */ },
    setPaused() { /* static */ },
    resize: draw,
    dispose() { ro.disconnect(); },
  };
}

/** Crops the face (with hat layer) of a skin into a crisp square data URL. */
export function headFromSkin(skin: string | null, size = 64): Promise<string> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => {
      const c = document.createElement('canvas');
      c.width = c.height = size;
      const ctx = c.getContext('2d')!;
      ctx.imageSmoothingEnabled = false;
      ctx.drawImage(img, 8, 8, 8, 8, 0, 0, size, size);
      ctx.drawImage(img, 40, 8, 8, 8, 0, 0, size, size);
      resolve(c.toDataURL());
    };
    img.onerror = () => resolve('');
    img.src = skin ?? DEFAULT_SKIN;
  });
}
