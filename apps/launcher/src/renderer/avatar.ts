// 3D player character (skinview3d / three.js, WebGL) with a transparent
// background. The default "living" animation breathes, shifts its weight,
// looks at the mouse (and at the viewer when the mouse rests) and plays a
// random gesture every few seconds, so it never loops the same movement.
// Reactions (greeting, launch cheer, success wave) are layered on top. Falls
// back to a flat 2D render of the skin when WebGL is not available.

import { SkinViewer, WalkingAnimation, WaveAnimation, PlayerAnimation, type PlayerObject } from 'skinview3d';
import { AmbientLight, CanvasTexture, DirectionalLight, Mesh, MeshBasicMaterial, PlaneGeometry, Box3 } from 'three';
import minifig from './assets/minifig.png';

export const DEFAULT_SKIN: string = minifig;
export type Anim = 'idle' | 'walk' | 'wave' | 'spin';
export type Reaction = 'greet' | 'launch' | 'success' | 'nod';

export interface Avatar {
  setSkin(skin: string | null, variant: 'classic' | 'slim', cape: string | null): Promise<void>;
  play(a: Anim): void;
  /** One-off gesture on top of the living idle. */
  react(r: Reaction): void;
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

type Gesture = { name: string; dur: number; pose: (p: PlayerObject, k: number, env: number) => void };
const env = (k: number) => Math.sin(Math.PI * Math.min(1, Math.max(0, k)));

const GESTURES: Record<string, Gesture> = {
  wave: { name: 'wave', dur: 2.2, pose: (p, k, e) => { p.skin.rightArm.rotation.z = Math.PI * 0.85 * e + Math.sin(k * 26) * 0.22 * e; p.skin.rightArm.rotation.x = -0.15 * e; } },
  look: { name: 'look', dur: 2.6, pose: (p, k, e) => { p.skin.head.rotation.y += Math.sin(k * Math.PI * 2) * 0.7 * e; p.skin.head.rotation.x += -0.12 * e; } },
  stretch: { name: 'stretch', dur: 2.4, pose: (p, _k, e) => { p.skin.rightArm.rotation.z = 2.8 * e; p.skin.leftArm.rotation.z = -2.8 * e; p.skin.head.rotation.x += -0.35 * e; p.position.y += 0.5 * e; } },
  shrug: { name: 'shrug', dur: 1.6, pose: (p, _k, e) => { p.skin.rightArm.rotation.z = 0.45 * e; p.skin.leftArm.rotation.z = -0.45 * e; p.skin.rightArm.rotation.x = -0.5 * e; p.skin.leftArm.rotation.x = -0.5 * e; p.skin.head.rotation.z += 0.18 * e; } },
  tap: { name: 'tap', dur: 1.8, pose: (p, k, e) => { p.skin.rightLeg.rotation.x = -Math.abs(Math.sin(k * 18)) * 0.28 * e; } },
  nod: { name: 'nod', dur: 1.0, pose: (p, k, e) => { p.skin.head.rotation.x += Math.sin(k * Math.PI * 3) * 0.3 * e; } },
  cheer: { name: 'cheer', dur: 2.0, pose: (p, k, e) => { p.skin.rightArm.rotation.z = 2.7 * e; p.skin.leftArm.rotation.z = -2.7 * e; p.position.y += Math.abs(Math.sin(k * Math.PI * 3)) * 3 * e; } },
  // October only: zombie arms + slow head roll.
  spook: { name: 'spook', dur: 2.8, pose: (p, k, e) => { p.skin.rightArm.rotation.x = -1.5 * e; p.skin.leftArm.rotation.x = -1.5 * e; p.skin.head.rotation.z += Math.sin(k * 6) * 0.25 * e; p.skin.body.rotation.y += Math.sin(k * 4) * 0.08 * e; } },
};

/** Idle that breathes, shifts weight, follows a look target and plays gestures. */
class LivingAnimation extends PlayerAnimation {
  private t = 0;
  private gesture: { g: Gesture; start: number } | null = null;
  private next = 3 + Math.random() * 3;
  private yaw = 0;
  private pitch = 0;
  private lastPick = '';
  private readonly look: () => { yaw: number; pitch: number };
  constructor(look: () => { yaw: number; pitch: number }) {
    super();
    this.look = look;
  }
  trigger(name: string): void {
    const g = GESTURES[name];
    if (g) { this.gesture = { g, start: this.t }; this.next = this.t + g.dur + 4 + Math.random() * 4; }
  }
  protected animate(p: PlayerObject, delta: number): void {
    this.t += delta;
    const t = this.t;
    const s = p.skin;
    // breathing + weight shift
    const breath = Math.sin(t * 2.1);
    const sway = Math.sin(t * 0.55);
    s.body.rotation.set(0, 0, sway * 0.025);
    s.body.scale.set(1 + breath * 0.012, 1 + breath * 0.006, 1 + breath * 0.012);
    p.position.y = breath * 0.12;
    p.rotation.z = sway * 0.02;
    s.rightArm.rotation.set(Math.sin(t * 1.1) * 0.04, 0, 0.05 + breath * 0.025);
    s.leftArm.rotation.set(-Math.sin(t * 1.1) * 0.04, 0, -0.05 - breath * 0.025);
    s.rightLeg.rotation.set(sway > 0 ? sway * 0.05 : 0, 0, 0.02 * sway);
    s.leftLeg.rotation.set(sway < 0 ? -sway * 0.05 : 0, 0, 0.02 * sway);
    // eyes/head follow the look target, smoothly
    const target = this.look();
    this.yaw += (target.yaw - this.yaw) * Math.min(1, delta * 4);
    this.pitch += (target.pitch - this.pitch) * Math.min(1, delta * 4);
    s.head.rotation.set(this.pitch, this.yaw, sway * -0.03);
    p.cape.rotation.x = 0.12 + breath * 0.03 + Math.abs(sway) * 0.05;
    // gestures: random, never the same twice in a row
    if (!this.gesture && t >= this.next) {
      const pool = ['wave', 'look', 'stretch', 'shrug', 'tap', ...(new Date().getMonth() === 9 ? ['spook', 'spook'] : [])].filter((x) => x !== this.lastPick);
      const pick = pool[Math.floor(Math.random() * pool.length)]!;
      this.lastPick = pick;
      this.trigger(pick);
    }
    if (this.gesture) {
      const k = (t - this.gesture.start) / this.gesture.g.dur;
      if (k >= 1) this.gesture = null;
      else this.gesture.g.pose(p, k, env(k));
    }
  }
}

function shadowTexture(): CanvasTexture {
  const c = document.createElement('canvas');
  c.width = c.height = 128;
  const g = c.getContext('2d')!;
  const grd = g.createRadialGradient(64, 64, 0, 64, 64, 64);
  grd.addColorStop(0, 'rgba(0,0,0,.75)');
  grd.addColorStop(0.6, 'rgba(0,0,0,.3)');
  grd.addColorStop(1, 'rgba(0,0,0,0)');
  g.fillStyle = grd;
  g.fillRect(0, 0, 128, 128);
  return new CanvasTexture(c);
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
  viewer.globalLight.intensity = 2.2;
  viewer.cameraLight.intensity = 0.9;
  viewer.autoRotateSpeed = 1.2;
  // Halloween lighting: warm key from the side, violet rim from behind.
  const key = new DirectionalLight(0xffa25a, 1.1);
  key.position.set(18, 14, 10);
  const rim = new DirectionalLight(0x9b6bff, 1.8);
  rim.position.set(-14, 10, -18);
  const fill = new AmbientLight(0x2a1d40, 0.6);
  viewer.scene.add(key, rim, fill);
  // Soft contact shadow under the feet.
  const shadow = new Mesh(new PlaneGeometry(22, 22), new MeshBasicMaterial({ map: shadowTexture(), transparent: true, depthWrite: false }));
  shadow.rotation.x = -Math.PI / 2;
  viewer.playerWrapper.add(shadow);
  const placeShadow = () => { const b = new Box3().setFromObject(viewer.playerObject.skin); if (Number.isFinite(b.min.y)) shadow.position.y = b.min.y - viewer.playerWrapper.position.y + 0.05; };

  // Look target: the mouse when it moves, otherwise the viewer (camera).
  const mouse = { x: 0, y: 0, at: 0 };
  const onMove = (e: MouseEvent) => {
    const r = canvas.getBoundingClientRect();
    mouse.x = Math.max(-1, Math.min(1, (e.clientX - (r.left + r.width / 2)) / (r.width / 2)));
    mouse.y = Math.max(-1, Math.min(1, (e.clientY - (r.top + r.height * 0.3)) / (r.height / 2)));
    mouse.at = performance.now();
  };
  addEventListener('mousemove', onMove);
  const look = () => {
    const cam = viewer.camera.position;
    const toCam = Math.atan2(cam.x, cam.z) - viewer.playerWrapper.rotation.y;
    const wrap = (a: number) => Math.atan2(Math.sin(a), Math.cos(a));
    const active = performance.now() - mouse.at < 4000;
    const yaw = wrap(toCam) + (active ? mouse.x * 0.75 : 0);
    return { yaw: Math.max(-0.9, Math.min(0.9, yaw)), pitch: active ? Math.max(-0.4, Math.min(0.5, mouse.y * 0.45)) : 0.04 };
  };

  let living = new LivingAnimation(look);
  let current: Anim = 'idle';
  let paused = reducedMotion;
  const make = (a: Anim): PlayerAnimation => {
    if (a === 'walk') return new WalkingAnimation();
    if (a === 'wave') {
      const w = new WaveAnimation('right');
      w.speed = 0.9;
      return w;
    }
    living = new LivingAnimation(look);
    return living;
  };
  const play = (a: Anim) => {
    current = a;
    viewer.animation = make(a);
    viewer.autoRotate = a === 'spin' && !paused;
    if (paused && viewer.animation) viewer.animation.paused = true;
  };
  play('idle');
  placeShadow();
  const ro = new ResizeObserver(() => { const b = box(); viewer.setSize(b.width, b.height); });
  ro.observe(canvas.parentElement!);
  return {
    async setSkin(skin, variant, cape) {
      await viewer.loadSkin(skin ?? DEFAULT_SKIN, { model: skin ? (variant === 'slim' ? 'slim' : 'default') : 'default' });
      if (cape) await viewer.loadCape(cape);
      else viewer.resetCape();
      play(current);
      placeShadow();
    },
    play,
    react(r) {
      if (paused) return;
      if (current !== 'idle' && current !== 'spin') play('idle');
      living.trigger(r === 'greet' ? 'wave' : r === 'launch' ? 'cheer' : r === 'success' ? 'wave' : 'nod');
    },
    setPaused(p) {
      paused = p || reducedMotion;
      if (viewer.animation) viewer.animation.paused = paused;
      viewer.autoRotate = current === 'spin' && !paused;
    },
    resize() { const b = box(); viewer.setSize(b.width, b.height); },
    dispose() {
      removeEventListener('mousemove', onMove);
      ro.disconnect();
      (shadow.material as MeshBasicMaterial).map?.dispose();
      shadow.geometry.dispose();
      (shadow.material as MeshBasicMaterial).dispose();
      viewer.dispose();
    },
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
    react() { /* static */ },
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
