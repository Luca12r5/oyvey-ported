// Start sequence "LEGO Hell Cave" (three.js / WebGL, ~24 s).
//   0–3 s   darkness, heartbeat
//   3–4 s   a torch ignites next to the camera
//   4–12 s  flight through a cave built from LEGO bricks (studs, stalactites, torches)
//  12–16 s  the cave opens into a chamber over a lava river; embers rise, bricks crumble from the ceiling
//  15–18 s  a giant pumpkin assembles brick by brick, lit from inside
//  18–21 s  the LEGO logo snaps together above it
//  21–24 s  a roar, the cave shakes, the camera falls into the lava → launcher
// Lit only by torches and lava (dark, high contrast); bloom only catches the
// hot parts. All sound is synthesised with WebAudio.

import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js';
import { mergeGeometries } from 'three/examples/jsm/utils/BufferGeometryUtils.js';
import type { IntroOptions } from './intro3d.ts';

const DURATION = 24;
const clamp01 = (x: number) => Math.max(0, Math.min(1, x));
const smooth = (a: number, b: number, x: number) => { const t = clamp01((x - a) / (b - a)); return t * t * (3 - 2 * t); };
const easeOutBack = (t: number) => { const c = 1.7; return 1 + (c + 1) * (t - 1) ** 3 + c * (t - 1) ** 2; };
const lerp = (a: number, b: number, t: number) => a + (b - a) * t;
const rnd = (a: number, b: number) => a + Math.random() * (b - a);

/** A 2×1 LEGO brick (rounded edges + two studs) in a 1×0.6×0.5 box, studs on +Y. */
export function brickGeometry(segments = 2): THREE.BufferGeometry {
  const body = new RoundedBoxGeometry(1, 0.6, 0.5, segments, 0.04);
  const parts: THREE.BufferGeometry[] = [body];
  for (const x of [-0.25, 0.25]) {
    const s = new THREE.CylinderGeometry(0.15, 0.15, 0.12, 16);
    s.translate(x, 0.36, 0);
    parts.push(s);
  }
  // RoundedBoxGeometry is non-indexed, cylinders are indexed: merge needs one kind.
  const flat = parts.map((g) => (g.index ? g.toNonIndexed() : g));
  const merged = mergeGeometries(flat, false);
  [...parts, ...flat].forEach((g) => g.dispose());
  return merged ?? new THREE.BoxGeometry(1, 0.6, 0.5);
}

/** 1×1 brick (cube-ish with one stud), for voxel shapes. */
function cubeBrickGeometry(): THREE.BufferGeometry {
  const body = new RoundedBoxGeometry(1, 1, 1, 2, 0.06);
  const s = new THREE.CylinderGeometry(0.3, 0.3, 0.16, 18);
  s.translate(0, 0.58, 0);
  const s2 = s.toNonIndexed();
  const merged = mergeGeometries([body, s2], false);
  body.dispose();
  s.dispose();
  s2.dispose();
  return merged ?? new THREE.BoxGeometry(1, 1, 1);
}

const FONT: Record<string, string[]> = {
  L: ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
  E: ['#####', '#....', '#....', '####.', '#....', '#....', '#####'],
  G: ['.####', '#....', '#....', '#.###', '#...#', '#...#', '.###.'],
  O: ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
};
const LOGO_COLORS = ['#c91a1a', '#f2b632', '#c91a1a', '#f2b632'];

function glowDot(): THREE.CanvasTexture {
  const c = document.createElement('canvas');
  c.width = c.height = 64;
  const g = c.getContext('2d')!;
  const r = g.createRadialGradient(32, 32, 0, 32, 32, 32);
  r.addColorStop(0, 'rgba(255,255,255,1)');
  r.addColorStop(0.25, 'rgba(255,255,255,.75)');
  r.addColorStop(1, 'rgba(255,255,255,0)');
  g.fillStyle = r;
  g.fillRect(0, 0, 64, 64);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  return t;
}

/** Flowing lava: fbm noise, dark crust over glowing cracks (values >1 feed the bloom). */
function lavaMaterial(): THREE.ShaderMaterial {
  return new THREE.ShaderMaterial({
    uniforms: { uTime: { value: 0 }, uHeat: { value: 1 } },
    toneMapped: false,
    fog: false,
    vertexShader: `varying vec2 vUv; varying float vDist; void main() { vUv = uv; vec4 mv = modelViewMatrix * vec4(position, 1.0); vDist = -mv.z; gl_Position = projectionMatrix * mv; }`,
    fragmentShader: `
      uniform float uTime; uniform float uHeat; varying vec2 vUv; varying float vDist;
      float h(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
      float n(vec2 p) { vec2 i = floor(p), f = fract(p); f = f * f * (3.0 - 2.0 * f);
        return mix(mix(h(i), h(i + vec2(1, 0)), f.x), mix(h(i + vec2(0, 1)), h(i + vec2(1, 1)), f.x), f.y); }
      float fbm(vec2 p) { float v = 0.0, a = 0.5; for (int i = 0; i < 5; i++) { v += a * n(p); p = p * 2.03 + 7.1; a *= 0.5; } return v; }
      void main() {
        vec2 p = vUv * vec2(18.0, 60.0);
        p.y += uTime * 0.9;
        float w = fbm(p + fbm(p * 0.6 + uTime * 0.15) * 2.2);
        float cracks = smoothstep(0.42, 0.62, w);
        vec3 crust = vec3(0.05, 0.015, 0.01);
        vec3 hot = mix(vec3(1.4, 0.28, 0.03), vec3(3.2, 1.5, 0.35), smoothstep(0.62, 0.85, w));
        vec3 col = mix(crust, hot, cracks) * uHeat;
        float fog = exp(-vDist * 0.012);
        gl_FragColor = vec4(mix(vec3(0.12, 0.02, 0.0), col, fog), 1.0);
      }`,
  });
}


function noiseCanvas(w: number, h: number, paint: (g: CanvasRenderingContext2D, img: ImageData) => void): THREE.CanvasTexture {
  const c = document.createElement('canvas');
  c.width = w;
  c.height = h;
  const g = c.getContext('2d')!;
  const img = g.createImageData(w, h);
  paint(g, img);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  t.wrapS = t.wrapT = THREE.RepeatWrapping;
  t.anisotropy = 8;
  return t;
}

/** Rough rock / gravel: layered value noise with pits (also used as bump map). */
export function rockTexture(): THREE.CanvasTexture {
  const W = 256;
  const grid = (n: number) => Array.from({ length: n * n }, () => Math.random());
  const layers = [8, 16, 32, 64].map((n) => ({ n, g: grid(n) }));
  const sample = (l: { n: number; g: number[] }, x: number, y: number) => {
    const fx = (x / W) * l.n, fy = (y / W) * l.n;
    const x0 = Math.floor(fx) % l.n, y0 = Math.floor(fy) % l.n, x1 = (x0 + 1) % l.n, y1 = (y0 + 1) % l.n;
    const tx = fx - Math.floor(fx), ty = fy - Math.floor(fy);
    const a = l.g[y0 * l.n + x0]!, b = l.g[y0 * l.n + x1]!, c = l.g[y1 * l.n + x0]!, d = l.g[y1 * l.n + x1]!;
    return (a + (b - a) * tx) * (1 - ty) + (c + (d - c) * tx) * ty;
  };
  return noiseCanvas(W, W, (g, img) => {
    for (let y = 0; y < W; y++) for (let x = 0; x < W; x++) {
      let v = 0, amp = 0.5;
      for (const l of layers) { v += sample(l, x, y) * amp; amp *= 0.5; }
      v = v * 0.9 + Math.random() * 0.12;
      if (Math.random() < 0.004) v *= 0.4;
      const k = Math.max(0, Math.min(255, Math.round(70 + v * 150)));
      const i = (y * W + x) * 4;
      img.data[i] = k; img.data[i + 1] = k * 0.94; img.data[i + 2] = k * 0.9; img.data[i + 3] = 255;
    }
    g.putImageData(img, 0, 0);
  });
}

/** Weathered timber: long grain streaks, knots and cracks. */
export function woodTexture(): THREE.CanvasTexture {
  const W = 128, H = 512;
  return noiseCanvas(W, H, (g, img) => {
    const knots = Array.from({ length: 3 }, () => ({ x: Math.random() * W, y: Math.random() * H, r: 6 + Math.random() * 10 }));
    for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
      let wx = x;
      for (const k of knots) { const d = Math.hypot(x - k.x, (y - k.y) * 0.5); if (d < k.r * 3) wx += (k.r * 3 - d) * 0.4 * Math.sign(x - k.x); }
      const grain = Math.sin(wx * 0.55 + Math.sin(y * 0.02) * 3) * 0.5 + 0.5;
      const fine = Math.random() * 0.25;
      let v = 0.45 + grain * 0.35 + fine;
      for (const k of knots) if (Math.hypot(x - k.x, (y - k.y) * 0.8) < k.r) v *= 0.55;
      if ((x * 7 + Math.floor(y / 40)) % 61 === 0) v *= 0.4; // cracks
      const i = (y * W + x) * 4;
      img.data[i] = Math.min(255, 120 * v + 40); img.data[i + 1] = Math.min(255, 82 * v + 26); img.data[i + 2] = Math.min(255, 52 * v + 16); img.data[i + 3] = 255;
    }
    g.putImageData(img, 0, 0);
  });
}

// ---- sound ------------------------------------------------------------------------

class CaveAudio {
  private ctx: AudioContext | null = null;
  private master: GainNode | null = null;
  private noise: AudioBuffer | null = null;
  private lastClack = 0;

  start(enabled: boolean): void {
    try {
      this.ctx = new AudioContext();
      this.master = this.ctx.createGain();
      this.master.gain.value = enabled ? 0.6 : 0;
      this.master.connect(this.ctx.destination);
      const len = this.ctx.sampleRate * 2;
      const b = this.ctx.createBuffer(1, len, this.ctx.sampleRate);
      const d = b.getChannelData(0);
      let last = 0;
      for (let i = 0; i < len; i++) { const w = Math.random() * 2 - 1; last = (last + 0.02 * w) / 1.02; d[i] = last * 3.5; } // brown noise
      this.noise = b;
      this.schedule();
    } catch { this.ctx = null; }
  }

  setMuted(m: boolean): void { if (this.ctx && this.master) this.master.gain.setTargetAtTime(m ? 0 : 0.6, this.ctx.currentTime, 0.05); }
  private at(t: number): number { return this.ctx!.currentTime + t; }

  private src(t: number, dur: number): AudioBufferSourceNode {
    const s = this.ctx!.createBufferSource();
    s.buffer = this.noise;
    s.loop = true;
    s.start(this.at(t));
    s.stop(this.at(t + dur));
    return s;
  }

  private thump(t: number, vol: number): void {
    const o = this.ctx!.createOscillator();
    o.frequency.setValueAtTime(62, this.at(t));
    o.frequency.exponentialRampToValueAtTime(38, this.at(t + 0.16));
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(0.0001, this.at(t));
    g.gain.exponentialRampToValueAtTime(vol, this.at(t + 0.015));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 0.22));
    o.connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 0.25));
  }

  private schedule(): void {
    const ctx = this.ctx!;
    // Heartbeat, faster as the chamber nears.
    let t = 0.4;
    while (t < 21) {
      const v = t < 4 ? 0.9 : 0.35;
      this.thump(t, v);
      this.thump(t + 0.24, v * 0.7);
      t += t < 12 ? 1.05 : 0.8;
    }
    // Cave rumble: brown noise, low-passed, swelling into the chamber.
    const lp = ctx.createBiquadFilter();
    lp.type = 'lowpass';
    lp.frequency.value = 140;
    const g = ctx.createGain();
    g.gain.setValueAtTime(0, this.at(0));
    g.gain.linearRampToValueAtTime(0.35, this.at(4));
    g.gain.linearRampToValueAtTime(0.6, this.at(14));
    g.gain.linearRampToValueAtTime(0.9, this.at(22));
    g.gain.linearRampToValueAtTime(0, this.at(DURATION));
    this.src(0, DURATION + 0.2).connect(lp).connect(g).connect(this.master!);
    // Dark drone (minor second).
    for (const [f, vol] of [[41.2, 0.12], [43.65, 0.08], [61.7, 0.05]] as const) {
      const o = ctx.createOscillator();
      o.type = 'sawtooth';
      o.frequency.value = f;
      const f2 = ctx.createBiquadFilter();
      f2.type = 'lowpass';
      f2.frequency.value = 220;
      const og = ctx.createGain();
      og.gain.setValueAtTime(0, this.at(0));
      og.gain.linearRampToValueAtTime(vol, this.at(6));
      og.gain.linearRampToValueAtTime(vol * 1.6, this.at(20));
      og.gain.linearRampToValueAtTime(0, this.at(DURATION));
      o.connect(f2).connect(og).connect(this.master!);
      o.start(this.at(0));
      o.stop(this.at(DURATION + 0.2));
    }
    this.ignite(3);
    // Torch crackle in the tunnel.
    for (let k = 0; k < 60; k++) this.crack(rnd(3.2, 13), rnd(0.02, 0.07));
    // Lava bubbling in the chamber.
    for (let k = 0; k < 30; k++) this.bubble(rnd(12, 22));
    for (const ct of [13.2, 14.6, 16.1]) this.crumble(ct);
    this.roar(21);
    this.splash(22.9);
  }

  private ignite(t: number): void {
    const bp = this.ctx!.createBiquadFilter();
    bp.type = 'bandpass';
    bp.frequency.setValueAtTime(400, this.at(t));
    bp.frequency.exponentialRampToValueAtTime(2400, this.at(t + 0.5));
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(0.0001, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.8, this.at(t + 0.08));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 1.1));
    this.src(t, 1.2).connect(bp).connect(g).connect(this.master!);
  }

  private crack(t: number, vol: number): void {
    const hp = this.ctx!.createBiquadFilter();
    hp.type = 'highpass';
    hp.frequency.value = 1800;
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(vol * 4, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 0.03));
    this.src(t, 0.04).connect(hp).connect(g).connect(this.master!);
  }

  private bubble(t: number): void {
    const o = this.ctx!.createOscillator();
    o.type = 'sine';
    const f = rnd(70, 140);
    o.frequency.setValueAtTime(f, this.at(t));
    o.frequency.exponentialRampToValueAtTime(f * 2.4, this.at(t + 0.09));
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(0.0001, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.12, this.at(t + 0.02));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 0.12));
    o.connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 0.14));
  }

  private crumble(t: number): void {
    for (let k = 0; k < 9; k++) this.clackAt(t + k * rnd(0.04, 0.11), rnd(0.3, 0.7), 0.25);
  }

  private clackAt(t: number, pitch: number, vol: number): void {
    const o = this.ctx!.createOscillator();
    o.type = 'square';
    o.frequency.setValueAtTime(900 + pitch * 900, this.at(t));
    o.frequency.exponentialRampToValueAtTime(300 + pitch * 200, this.at(t + 0.04));
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(vol, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 0.06));
    const bp = this.ctx!.createBiquadFilter();
    bp.type = 'bandpass';
    bp.frequency.value = 1600;
    o.connect(bp).connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 0.07));
  }

  /** A brick snapping into place (rate limited so 300 bricks don't clip). */
  clack(pitch: number): void {
    if (!this.ctx || this.ctx.currentTime - this.lastClack < 0.045) return;
    this.lastClack = this.ctx.currentTime;
    this.clackAt(0.001, pitch, 0.16);
  }

  private roar(t: number): void {
    const ctx = this.ctx!;
    const o = ctx.createOscillator();
    o.type = 'sawtooth';
    o.frequency.setValueAtTime(70, this.at(t));
    o.frequency.linearRampToValueAtTime(110, this.at(t + 0.6));
    o.frequency.exponentialRampToValueAtTime(42, this.at(t + 2.4));
    const vib = ctx.createOscillator();
    const vg = ctx.createGain();
    vib.frequency.value = 23;
    vg.gain.value = 9;
    vib.connect(vg).connect(o.frequency);
    const f = ctx.createBiquadFilter();
    f.type = 'lowpass';
    f.frequency.setValueAtTime(500, this.at(t));
    f.frequency.linearRampToValueAtTime(1400, this.at(t + 0.5));
    f.frequency.exponentialRampToValueAtTime(200, this.at(t + 2.5));
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.55, this.at(t + 0.25));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 2.6));
    o.connect(f).connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 2.7));
    vib.start(this.at(t));
    vib.stop(this.at(t + 2.7));
    const nlp = ctx.createBiquadFilter();
    nlp.type = 'lowpass';
    nlp.frequency.value = 900;
    const ng = ctx.createGain();
    ng.gain.setValueAtTime(0.0001, this.at(t));
    ng.gain.exponentialRampToValueAtTime(0.8, this.at(t + 0.3));
    ng.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 2.8));
    this.src(t, 2.9).connect(nlp).connect(ng).connect(this.master!);
  }

  private splash(t: number): void {
    const o = this.ctx!.createOscillator();
    o.frequency.setValueAtTime(90, this.at(t));
    o.frequency.exponentialRampToValueAtTime(28, this.at(t + 1));
    const g = this.ctx!.createGain();
    g.gain.setValueAtTime(0.9, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.0001, this.at(t + 1.2));
    o.connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 1.3));
  }

  fadeOut(sec: number): void { if (this.ctx && this.master) this.master.gain.setTargetAtTime(0, this.ctx.currentTime, sec / 3); }
  close(): void { void this.ctx?.close().catch(() => {}); this.ctx = null; }
}

// ---- scene ------------------------------------------------------------------------

const STONE = ['#2b2526', '#221d1e', '#332a28', '#1b1617', '#3a2622', '#2a1a17'];

export function playIntroCave(opts: IntroOptions): Promise<void> {
  return new Promise((resolve) => {
    const q = clamp01(opts.quality);
    const root = document.createElement('div');
    root.className = 'intro3d cave';
    const canvas = document.createElement('canvas');
    const flash = document.createElement('div');
    flash.className = 'intro-flash';
    const ui = document.createElement('div');
    ui.className = 'intro-ui';
    const mute = document.createElement('button');
    mute.className = 'intro-btn';
    mute.type = 'button';
    const skip = document.createElement('button');
    skip.className = 'intro-btn skip';
    skip.type = 'button';
    skip.textContent = opts.skipLabel;
    ui.append(mute, skip);
    root.append(canvas, flash, ui);
    document.body.append(root);
    let muted = !opts.sound;
    const drawMute = () => { mute.textContent = muted ? 'Ton an' : 'Ton aus'; mute.setAttribute('aria-pressed', String(muted)); };
    drawMute();

    const renderer = new THREE.WebGLRenderer({ canvas, antialias: q > 0.35, powerPreference: 'high-performance' });
    renderer.setPixelRatio(Math.min(devicePixelRatio || 1, q > 0.85 ? 2 : q > 0.5 ? 1.5 : 1));
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 0;
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.shadowMap.enabled = q > 0.55;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    const scene = new THREE.Scene();
    scene.background = new THREE.Color('#000000');
    const fog = new THREE.FogExp2('#0a0302', 0.045);
    scene.fog = fog;
    const camera = new THREE.PerspectiveCamera(55, 1, 0.05, 300);
    const dot = glowDot();

    scene.add(new THREE.AmbientLight('#1a0c08', 0.25));
    const lavaHemi = new THREE.HemisphereLight('#120606', '#ff5a10', 0);
    scene.add(lavaHemi);

    // Shared brick geometry/material (instanced everywhere).
    const brickGeo = brickGeometry(q > 0.5 ? 3 : 2);
    const cubeGeo = cubeBrickGeometry();
    const rockTex = rockTexture();
    const stoneMat = new THREE.MeshStandardMaterial({ roughness: 0.78, metalness: 0.02, map: rockTex, bumpMap: rockTex, bumpScale: 0.6 });
    const woodTex = woodTexture();
    const woodMat = new THREE.MeshStandardMaterial({ map: woodTex, bumpMap: woodTex, bumpScale: 0.8, roughness: 0.85, metalness: 0, color: '#9a7a5a' });
    const steelMat = new THREE.MeshStandardMaterial({ color: '#6b6560', roughness: 0.32, metalness: 0.92 });
    const rustMat = new THREE.MeshStandardMaterial({ color: '#4a3a30', roughness: 0.6, metalness: 0.65, bumpMap: rockTex, bumpScale: 0.3 });
    const m4 = new THREE.Matrix4(), qt = new THREE.Quaternion(), eu = new THREE.Euler(), v3 = new THREE.Vector3(), sc = new THREE.Vector3();
    const col = new THREE.Color();

    // ---- tunnel: z 6 … -72 along a gentle S-curve
    const tunnelPath = (z: number) => new THREE.Vector3(Math.sin(z * 0.045) * 3.2, Math.cos(z * 0.06) * 0.7, z);
    const rings = Math.round(70 + 40 * q);
    const perRing = 30;
    const tunnel = new THREE.InstancedMesh(brickGeo, stoneMat, rings * perRing + 160);
    tunnel.receiveShadow = true;
    let ti = 0;
    for (let r = 0; r < rings; r++) {
      const z = lerp(8, -74, r / (rings - 1));
      const c = tunnelPath(z);
      for (let k = 0; k < perRing; k++) {
        const a = (k / perRing) * Math.PI * 2 + (r % 2) * (Math.PI / perRing);
        if (Math.sin(a) < -0.72) continue; // floor: gravel bed and rails
        const rad = rnd(4.1, 5.0);
        v3.set(c.x + Math.cos(a) * rad * 1.15, c.y + Math.sin(a) * rad, z + rnd(-0.3, 0.3));
        // Studs face the inside of the cave.
        const inward = new THREE.Vector3(-Math.cos(a), -Math.sin(a), 0);
        qt.setFromUnitVectors(new THREE.Vector3(0, 1, 0), inward);
        eu.set(0, rnd(-0.2, 0.2), rnd(-0.12, 0.12));
        qt.multiply(new THREE.Quaternion().setFromEuler(eu));
        const s = rnd(1.05, 1.5);
        sc.set(s, s * rnd(0.9, 1.2), s * 1.5);
        m4.compose(v3, qt, sc);
        tunnel.setMatrixAt(ti, m4);
        tunnel.setColorAt(ti, col.set(STONE[(r * 7 + k * 3) % STONE.length]!).multiplyScalar(rnd(0.8, 1.15)));
        ti++;
      }
    }
    // Stalactites: stacks of shrinking bricks hanging from the ceiling.
    for (let k = 0; k < 26 && ti < tunnel.count - 6; k++) {
      const z = rnd(2, -70);
      const c = tunnelPath(z);
      const x = c.x + rnd(-2.2, 2.2);
      let y = c.y + 4.2;
      let s = rnd(1.1, 1.5);
      const n = 3 + Math.floor(Math.random() * 4);
      for (let i = 0; i < n && ti < tunnel.count; i++) {
        y -= 0.6 * s * 0.9;
        qt.setFromEuler(eu.set(Math.PI, rnd(0, 6), 0));
        m4.compose(v3.set(x, y, z), qt, sc.set(s, s, s * 1.4));
        tunnel.setMatrixAt(ti, m4);
        tunnel.setColorAt(ti, col.set(STONE[(k + i) % STONE.length]!));
        ti++;
        s *= 0.72;
      }
    }
    tunnel.count = ti;
    scene.add(tunnel);

    // ---- the mine: gravel bed, rails on sleepers, timber supports, lanterns
    const floorY = (z: number) => tunnelPath(Math.max(z, -74)).y - 2.9;
    const RAIL_END = -99;
    const railPoint = (z: number) => {
      const c = tunnelPath(Math.max(z, -74));
      const x = z >= -74 ? c.x : lerp(c.x, 0, smooth(-74, -84, z));
      return new THREE.Vector3(x, floorY(z), z);
    };
    // Gravel ribbon under the tunnel track.
    {
      const pts: number[] = [], uvs: number[] = [], idx: number[] = [];
      let n = 0;
      for (let z = 8; z >= -74; z -= 1) {
        const p = railPoint(z);
        pts.push(p.x - 4.4, p.y - 0.05, z, p.x + 4.4, p.y - 0.05, z);
        uvs.push(0, z * 0.25, 3, z * 0.25);
        if (n) idx.push((n - 1) * 2, n * 2, (n - 1) * 2 + 1, (n - 1) * 2 + 1, n * 2, n * 2 + 1);
        n++;
      }
      const g = new THREE.BufferGeometry();
      g.setAttribute('position', new THREE.Float32BufferAttribute(pts, 3));
      g.setAttribute('uv', new THREE.Float32BufferAttribute(uvs, 2));
      g.setIndex(idx);
      g.computeVertexNormals();
      const gravelTex = rockTex.clone();
      gravelTex.wrapS = gravelTex.wrapT = THREE.RepeatWrapping;
      gravelTex.needsUpdate = true;
      const ground = new THREE.Mesh(g, new THREE.MeshStandardMaterial({ map: gravelTex, bumpMap: gravelTex, bumpScale: 1.2, roughness: 0.95, color: '#5a4a40', side: THREE.DoubleSide }));
      ground.receiveShadow = true;
      scene.add(ground);
    }
    // Rails (two steel profiles) on wooden sleepers, from the entrance to the buffer stop on the bridge.
    const railSegs: THREE.Matrix4[] = [], sleepers: THREE.Matrix4[] = [];
    const up = new THREE.Vector3(0, 1, 0);
    for (let z = 8; z > RAIL_END; z -= 0.6) {
      const p0 = railPoint(z), p1 = railPoint(z - 0.6);
      const dir = p1.clone().sub(p0);
      const len = dir.length();
      dir.normalize();
      const side = new THREE.Vector3().crossVectors(dir, up).normalize();
      const mid = p0.clone().add(p1).multiplyScalar(0.5);
      const rot = new THREE.Quaternion().setFromRotationMatrix(new THREE.Matrix4().makeBasis(side, up, dir.clone().negate()));
      for (const o of [-0.62, 0.62]) railSegs.push(new THREE.Matrix4().compose(mid.clone().addScaledVector(side, o).add(new THREE.Vector3(0, 0.2, 0)), rot, new THREE.Vector3(0.09, 0.14, len + 0.02)));
      sleepers.push(new THREE.Matrix4().compose(mid.clone().add(new THREE.Vector3(0, 0.06, 0)), rot.clone().multiply(new THREE.Quaternion().setFromEuler(new THREE.Euler(0, rnd(-0.04, 0.04), 0))), new THREE.Vector3(1.9, 0.14, 0.26)));
    }
    const unit = new THREE.BoxGeometry(1, 1, 1);
    const rails = new THREE.InstancedMesh(unit, steelMat, railSegs.length);
    railSegs.forEach((m, i) => rails.setMatrixAt(i, m));
    const sleeperMesh = new THREE.InstancedMesh(unit, woodMat, sleepers.length);
    sleepers.forEach((m, i) => { sleeperMesh.setMatrixAt(i, m); sleeperMesh.setColorAt(i, col.setScalar(rnd(0.7, 1.05))); });
    rails.receiveShadow = sleeperMesh.receiveShadow = true;
    scene.add(rails, sleeperMesh);
    // Buffer stop at the end of the bridge.
    const stop = new THREE.Group();
    const stopBeam = new THREE.Mesh(unit, woodMat);
    stopBeam.scale.set(2.2, 0.45, 0.45);
    stopBeam.position.y = 0.7;
    const stopPostL = new THREE.Mesh(unit, woodMat);
    stopPostL.scale.set(0.3, 1.1, 0.3);
    stopPostL.position.set(-0.8, 0.4, 0.2);
    const stopPostR = stopPostL.clone();
    stopPostR.position.x = 0.8;
    stop.add(stopBeam, stopPostL, stopPostR);
    stop.position.copy(railPoint(RAIL_END - 0.4));
    scene.add(stop);
    // Timber frames every 5 m: two posts, a cap beam and corner braces (rough wood).
    const timbers: THREE.Matrix4[] = [];
    const frameZ: number[] = [];
    for (let z = 5; z > -72; z -= 5) frameZ.push(z);
    for (const z of frameZ) {
      const p = railPoint(z);
      const tilt = rnd(-0.03, 0.03);
      const q0 = new THREE.Quaternion().setFromEuler(new THREE.Euler(0, rnd(-0.05, 0.05), tilt));
      for (const sx of [-2.7, 2.7]) timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x + sx, p.y + 1.75, z), q0, new THREE.Vector3(0.34, 3.5, 0.34)));
      timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x, p.y + 3.62, z), q0, new THREE.Vector3(6.2, 0.38, 0.4)));
      for (const sx of [-1, 1]) timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x + sx * 2.25, p.y + 3.2, z), new THREE.Quaternion().setFromEuler(new THREE.Euler(0, 0, sx * 0.78)), new THREE.Vector3(1.1, 0.22, 0.24)));
    }
    // Trestle under the bridge: posts down into the lava and cross braces.
    for (let z = -76; z > RAIL_END; z -= 3) {
      const p = railPoint(z);
      for (const sx of [-0.95, 0.95]) timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x + sx, p.y - 2.5, z), new THREE.Quaternion(), new THREE.Vector3(0.3, 5, 0.3)));
      timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x, p.y - 2.3, z), new THREE.Quaternion().setFromEuler(new THREE.Euler(0, 0, 0.9)), new THREE.Vector3(2.6, 0.18, 0.2)));
      timbers.push(new THREE.Matrix4().compose(new THREE.Vector3(p.x, p.y - 0.2, z), new THREE.Quaternion(), new THREE.Vector3(2.4, 0.28, 0.32)));
    }
    const timberMesh = new THREE.InstancedMesh(unit, woodMat, timbers.length);
    timbers.forEach((m, i) => { timberMesh.setMatrixAt(i, m); timberMesh.setColorAt(i, col.setScalar(rnd(0.65, 1))); });
    timberMesh.castShadow = timberMesh.receiveShadow = true;
    scene.add(timberMesh);
    // Mine lanterns hanging from every second cap beam: metal cage, glass glow, warm light.
    const torches = frameZ.filter((_, i) => i % 2 === 0).map((z, i) => {
      const p = railPoint(z);
      const pos = new THREE.Vector3(p.x + (i % 2 ? 0.9 : -0.9), p.y + 3.05, z + 0.25);
      const cage = new THREE.Group();
      cage.position.copy(pos);
      const top = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.16, 0.1, 10), rustMat);
      top.position.y = 0.2;
      const bottom = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.05, 10), rustMat);
      bottom.position.y = -0.2;
      const glass = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.34, 12, 1, true), new THREE.MeshPhysicalMaterial({ color: '#ffd9a0', transparent: true, opacity: 0.35, roughness: 0.1, transmission: 0.4, side: THREE.DoubleSide }));
      const wire = new THREE.Mesh(new THREE.CylinderGeometry(0.01, 0.01, 0.4, 4), rustMat);
      wire.position.y = 0.45;
      cage.add(top, bottom, glass, wire);
      for (let k = 0; k < 4; k++) {
        const bar = new THREE.Mesh(new THREE.BoxGeometry(0.018, 0.4, 0.018), rustMat);
        const ang = (k / 4) * Math.PI * 2;
        bar.position.set(Math.cos(ang) * 0.14, 0, Math.sin(ang) * 0.14);
        cage.add(bar);
      }
      scene.add(cage);
      const flameGroup = new THREE.Group();
      cage.add(flameGroup);
      const sprites = [0, 1, 2].map((j) => {
        const sp = new THREE.Sprite(new THREE.SpriteMaterial({ map: dot, color: new THREE.Color(j === 0 ? '#fff1c0' : j === 1 ? '#ffb347' : '#ff7a1a').multiplyScalar(j === 0 ? 3 : 2), blending: THREE.AdditiveBlending, depthWrite: false, transparent: true, toneMapped: false }));
        sp.scale.setScalar(0.12 + j * 0.12);
        flameGroup.add(sp);
        return sp;
      });
      const light = i < (q > 0.5 ? 8 : 5) ? new THREE.PointLight('#ffa050', 0, 14, 1.7) : null;
      if (light) {
        light.position.copy(pos).add(new THREE.Vector3(0, -0.15, 0));
        if (i === 0 && renderer.shadowMap.enabled) { light.castShadow = true; light.shadow.mapSize.set(512, 512); light.shadow.bias = -0.002; }
        scene.add(light);
      }
      return { flameGroup, sprites, light, cage, ph: Math.random() * 10, on: i === 0 ? 3 : 4 + i * 0.55 };
    });

    // The minecart the camera rides in (rim and corners visible at the bottom of the frame).
    const cart = new THREE.Group();
    const wall = (w: number, h: number, d: number, x: number, y: number, z: number) => {
      const m = new THREE.Mesh(new RoundedBoxGeometry(w, h, d, 2, 0.02), rustMat);
      m.position.set(x, y, z);
      cart.add(m);
    };
    wall(1.5, 0.55, 0.07, 0, -0.78, -0.75);
    wall(0.07, 0.55, 1.3, -0.74, -0.78, -0.12);
    wall(0.07, 0.55, 1.3, 0.74, -0.78, -0.12);
    wall(1.56, 0.05, 0.11, 0, -0.5, -0.75);
    for (const x of [-0.74, 0.74]) wall(0.11, 0.05, 1.32, x, -0.5, -0.12);
    for (const [x, z] of [[-0.66, -0.68], [0.66, -0.68]] as const) {
      const rivet = new THREE.Mesh(new THREE.SphereGeometry(0.025, 8, 6), steelMat);
      rivet.position.set(x, -0.6, z + 0.04);
      cart.add(rivet);
    }
    camera.add(cart);
    scene.add(camera);

    // ---- chamber: lava river, huge brick walls and ceiling
    const CH = { z0: -74, z1: -150, lavaY: -7 };
    const lavaMat = lavaMaterial();
    const lava = new THREE.Mesh(new THREE.PlaneGeometry(90, 120, 1, 1), lavaMat);
    lava.rotation.x = -Math.PI / 2;
    lava.position.set(0, CH.lavaY, -112);
    scene.add(lava);
    const lavaLights = [-96, -116, -136].map((z, i) => {
      const l = new THREE.PointLight('#ff5a12', 0, 46, 1.5);
      l.position.set(i === 1 ? 0 : (i ? 12 : -12), CH.lavaY + 2.5, z);
      scene.add(l);
      return l;
    });
    const wallCount = Math.round(260 + 260 * q);
    const walls = new THREE.InstancedMesh(brickGeo, stoneMat, wallCount);
    for (let i = 0; i < wallCount; i++) {
      const z = rnd(CH.z0 - 2, CH.z1);
      const sideX = Math.random() < 0.5 ? -1 : 1;
      const isCeil = Math.random() < 0.3;
      const s = rnd(3, 6.5);
      if (isCeil) {
        v3.set(rnd(-30, 30), rnd(17, 22), z);
        qt.setFromEuler(eu.set(Math.PI + rnd(-0.2, 0.2), rnd(0, 6), rnd(-0.2, 0.2)));
      } else {
        v3.set(sideX * rnd(20, 30), rnd(CH.lavaY - 2, 20), z);
        qt.setFromUnitVectors(new THREE.Vector3(0, 1, 0), new THREE.Vector3(-sideX, rnd(-0.3, 0.3), 0).normalize());
      }
      m4.compose(v3, qt, sc.set(s, s * rnd(0.8, 1.4), s * 1.5));
      walls.setMatrixAt(i, m4);
      walls.setColorAt(i, col.set(STONE[i % STONE.length]!).multiplyScalar(rnd(0.7, 1.1)));
    }
    scene.add(walls);
    // Hanging stalactites in the chamber.
    const bigStal = new THREE.InstancedMesh(brickGeo, stoneMat, 120);
    let bs = 0;
    for (let k = 0; k < 22; k++) {
      const x = rnd(-20, 20), z = rnd(-84, -146);
      let y = 18;
      let s = rnd(2.4, 3.4);
      for (let i = 0; i < 5 && bs < 120; i++) {
        y -= 0.6 * s;
        qt.setFromEuler(eu.set(Math.PI, rnd(0, 6), 0));
        m4.compose(v3.set(x, y, z), qt, sc.set(s, s, s * 1.4));
        bigStal.setMatrixAt(bs, m4);
        bigStal.setColorAt(bs, col.set(STONE[(k + i) % STONE.length]!));
        bs++;
        s *= 0.7;
      }
    }
    bigStal.count = bs;
    scene.add(bigStal);
    // A rock ledge the camera flies over, with a lava fall at the back.
    const fallMat = lavaMaterial();
    const lavaFall = new THREE.Mesh(new THREE.PlaneGeometry(12, 40), fallMat);
    lavaFall.position.set(0, 6, -149);
    scene.add(lavaFall);

    // Embers rising from the lava.
    const EMB = Math.round(600 + 1200 * q);
    const embPos = new Float32Array(EMB * 3), embVel = new Float32Array(EMB);
    for (let i = 0; i < EMB; i++) { embPos.set([rnd(-28, 28), rnd(CH.lavaY, 18), rnd(-80, -148)], i * 3); embVel[i] = rnd(0.6, 2.4); }
    const embGeo = new THREE.BufferGeometry();
    embGeo.setAttribute('position', new THREE.BufferAttribute(embPos, 3));
    const embMat = new THREE.PointsMaterial({ size: 0.16, map: dot, color: new THREE.Color('#ff7a1a').multiplyScalar(2.4), transparent: true, opacity: 0, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false });
    scene.add(new THREE.Points(embGeo, embMat));
    // Dust in the tunnel (lit only near torches).
    const DUST = Math.round(500 + 900 * q);
    const dPos = new Float32Array(DUST * 3);
    for (let i = 0; i < DUST; i++) { const z = rnd(8, -74); const c = tunnelPath(z); dPos.set([c.x + rnd(-3.5, 3.5), c.y + rnd(-2.5, 3), z], i * 3); }
    const dGeo = new THREE.BufferGeometry();
    dGeo.setAttribute('position', new THREE.BufferAttribute(dPos, 3));
    const dMat = new THREE.PointsMaterial({ size: 0.035, map: dot, color: '#b48a6a', transparent: true, opacity: 0, depthWrite: false, blending: THREE.AdditiveBlending });
    scene.add(new THREE.Points(dGeo, dMat));

    // Bricks crumbling from the ceiling.
    const CRUMBS = 48;
    const crumbs = new THREE.InstancedMesh(brickGeo, stoneMat, CRUMBS);
    const cr = Array.from({ length: CRUMBS }, (_, i) => ({ t0: 12.6 + (i / CRUMBS) * 9 + rnd(0, 0.4), x: rnd(-14, 14), z: rnd(-92, -128), s: rnd(0.8, 1.8), spin: new THREE.Vector3(rnd(-3, 3), rnd(-3, 3), rnd(-3, 3)), color: STONE[i % STONE.length]! }));
    cr.forEach((c, i) => crumbs.setColorAt(i, col.set(c.color)));
    scene.add(crumbs);

    // ---- the pumpkin (voxel shell from 1×1 bricks), facing the camera
    const PK = new THREE.Vector3(0, 1.4, -118);
    const pkCell = 1.05;
    const pkVox: { target: THREE.Vector3; start: THREE.Vector3; t0: number; color: THREE.Color; rot: THREE.Euler }[] = [];
    const R = 5.2, Ry = 4.3;
    for (let x = -6; x <= 6; x++) for (let y = -5; y <= 5; y++) for (let z = -6; z <= 6; z++) {
      const d = (x * x) / (R * R) + (y * y) / (Ry * Ry) + (z * z) / (R * R);
      const di = (x * x) / ((R - 1.4) ** 2) + (y * y) / ((Ry - 1.4) ** 2) + (z * z) / ((R - 1.4) ** 2);
      if (d > 1 || di < 1) continue;
      // Carved face on the +z side: two triangle eyes and a jagged mouth.
      if (z > 2) {
        const eye = (ex: number) => y >= 0.5 && y <= 2.5 && Math.abs(x - ex) <= (2.5 - y) * 0.7 + 0.4;
        const mouth = y >= -2.6 && y <= -0.8 && Math.abs(x) <= 3.4 && !((x === -1 || x === 2) && y === -0.8 + 0) && !(Math.abs(x) % 2 === 0 && y <= -2.4);
        if (eye(-2) || eye(2) || mouth) continue;
      }
      const ribs = Math.abs(Math.sin(Math.atan2(z, x) * 4)) < 0.3;
      pkVox.push({
        target: new THREE.Vector3(x * pkCell, y * pkCell, z * pkCell),
        start: new THREE.Vector3(x * 4 + rnd(-6, 6), CH.lavaY - 4 - rnd(0, 6), z * 4 + rnd(-6, 6)),
        t0: 0,
        color: new THREE.Color(ribs ? '#c2410c' : '#ea580c').multiplyScalar(rnd(0.85, 1.05)),
        rot: new THREE.Euler(rnd(0, 7), rnd(0, 7), rnd(0, 7)),
      });
    }
    const stem = [[0, 5, 0], [0, 6, 0], [1, 6.6, 0]];
    stem.forEach(([x, y, z]) => pkVox.push({ target: new THREE.Vector3(x! * pkCell, y! * pkCell, z! * pkCell), start: new THREE.Vector3(0, 30, 0), t0: 0, color: new THREE.Color('#3f6212'), rot: new THREE.Euler(1, 2, 3) }));
    pkVox.sort((a, b) => a.target.y - b.target.y);
    pkVox.forEach((p, i) => { p.t0 = 15 + (i / pkVox.length) * 3.1 + rnd(0, 0.12); });
    const pkMat = new THREE.MeshStandardMaterial({ roughness: 0.45, metalness: 0.02 });
    const pumpkinMesh = new THREE.InstancedMesh(cubeGeo, pkMat, pkVox.length);
    pkVox.forEach((p, i) => pumpkinMesh.setColorAt(i, p.color));
    const pkGroup = new THREE.Group();
    pkGroup.position.copy(PK);
    pkGroup.add(pumpkinMesh);
    const pkCore = new THREE.Mesh(new THREE.SphereGeometry(2.6, 24, 16), new THREE.MeshBasicMaterial({ color: new THREE.Color('#ffb347').multiplyScalar(0), toneMapped: false }));
    pkGroup.add(pkCore);
    const pkLight = new THREE.PointLight('#ff9a3a', 0, 30, 1.6);
    pkLight.position.set(0, 0, 3.5);
    pkGroup.add(pkLight);
    scene.add(pkGroup);
    const pkDone = new Uint8Array(pkVox.length);
    // Rock island in the lava that carries the pumpkin.
    const island = new THREE.InstancedMesh(brickGeo, stoneMat, 70);
    for (let i = 0; i < 70; i++) {
      const a = Math.random() * Math.PI * 2, r = Math.sqrt(Math.random()) * 7;
      const y = lerp(CH.lavaY - 1, PK.y - 4.6, 1 - r / 7) + rnd(-0.5, 0.3);
      qt.setFromEuler(eu.set(rnd(-0.15, 0.15), rnd(0, 6), rnd(-0.15, 0.15)));
      const s2 = rnd(2.2, 3.6);
      m4.compose(v3.set(PK.x + Math.cos(a) * r, y, PK.z + Math.sin(a) * r), qt, sc.set(s2, s2, s2 * 1.4));
      island.setMatrixAt(i, m4);
      island.setColorAt(i, col.set(STONE[i % STONE.length]!).multiplyScalar(0.8));
    }
    scene.add(island);

    // ---- LEGO logo above the pumpkin
    const LG = new THREE.Vector3(0, 10.8, -116);
    const lgCell = 0.86;
    const letters = 'LEGO';
    const cols = letters.length * 5 + (letters.length - 1);
    const lgBricks: { target: THREE.Vector3; start: THREE.Vector3; t0: number; color: THREE.Color }[] = [];
    letters.split('').forEach((ch, li) => FONT[ch]!.forEach((row, y) => [...row].forEach((px, x) => {
      if (px !== '#') return;
      const tx = (li * 6 + x - (cols - 1) / 2) * lgCell;
      const ty = (3 - y) * lgCell;
      lgBricks.push({ target: new THREE.Vector3(tx, ty, 0), start: new THREE.Vector3(tx * 3 + rnd(-8, 8), ty + rnd(14, 26), rnd(-8, 4)), t0: 0, color: new THREE.Color(LOGO_COLORS[li]!) });
    })));
    lgBricks.forEach((b, i) => { b.t0 = 18.1 + (i / lgBricks.length) * 2.2 + rnd(0, 0.1); });
    const lgMat = new THREE.MeshStandardMaterial({ roughness: 0.3, metalness: 0.05, emissive: '#000000' });
    const logoMesh = new THREE.InstancedMesh(cubeGeo, lgMat, lgBricks.length);
    lgBricks.forEach((b, i) => logoMesh.setColorAt(i, b.color));
    const lgGroup = new THREE.Group();
    lgGroup.position.copy(LG);
    lgGroup.add(logoMesh);
    const lgLight = new THREE.PointLight('#ffd27a', 0, 26, 1.6);
    lgLight.position.set(0, 0, 6);
    lgGroup.add(lgLight);
    scene.add(lgGroup);
    const lgDone = new Uint8Array(lgBricks.length);
    // "LAUNCHER" in glowing pixel type below the logo.
    const tc = document.createElement('canvas');
    tc.width = 2048;
    tc.height = 256;
    const tTex = new THREE.CanvasTexture(tc);
    tTex.colorSpace = THREE.SRGBColorSpace;
    const drawTxt = () => {
      const g = tc.getContext('2d')! as CanvasRenderingContext2D & { letterSpacing: string };
      g.clearRect(0, 0, 2048, 256);
      g.textAlign = 'center';
      g.textBaseline = 'middle';
      g.letterSpacing = '36px';
      g.font = '400 120px "Press Start 2P", monospace';
      g.shadowColor = '#ff5a10';
      g.shadowBlur = 36;
      g.fillStyle = '#ffd9a8';
      g.fillText('LAUNCHER', 1024, 128);
      tTex.needsUpdate = true;
    };
    drawTxt();
    void document.fonts?.load('400 64px "Press Start 2P"').then(drawTxt).catch(() => {});
    const txtMat = new THREE.MeshBasicMaterial({ map: tTex, transparent: true, opacity: 0, depthWrite: false, toneMapped: false, color: new THREE.Color(1.6, 1.4, 1.2) });
    const txt = new THREE.Mesh(new THREE.PlaneGeometry(cols * lgCell * 0.9, (cols * lgCell * 0.9) / 8), txtMat);
    txt.position.set(0, -3.6 * lgCell - 1.2, 0.6);
    lgGroup.add(txt);

    // Sparks when bricks land.
    const SP = 600;
    const spPos = new Float32Array(SP * 3), spVel = new Float32Array(SP * 3), spLife = new Float32Array(SP);
    const spGeo = new THREE.BufferGeometry();
    spGeo.setAttribute('position', new THREE.BufferAttribute(spPos, 3));
    scene.add(new THREE.Points(spGeo, new THREE.PointsMaterial({ size: 0.14, map: dot, color: new THREE.Color('#ffb347').multiplyScalar(2.6), transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false })));
    let spNext = 0;
    const sparks = (at: THREE.Vector3, n: number) => {
      for (let i = 0; i < n; i++) {
        const k = spNext++ % SP;
        spPos.set([at.x, at.y, at.z], k * 3);
        const a = Math.random() * Math.PI * 2, s = rnd(1.5, 4.5);
        spVel.set([Math.cos(a) * s, rnd(1, 4), Math.sin(a) * s], k * 3);
        spLife[k] = rnd(0.4, 0.9);
      }
    };

    // ---- camera: seated in the cart, riding the rails, then tipping into the lava
    const SEAT = 1.55;
    const rideZ = (t: number) => {
      const k = clamp01((t - 4.6) / (16.4 - 4.6));
      // Accelerate down the tunnel, roll across the bridge, brake hard at the buffer.
      const e = k < 0.15 ? (k / 0.15) ** 2 * 0.06 : k < 0.85 ? 0.06 + ((k - 0.15) / 0.7) * 0.86 : 0.92 + (1 - (1 - (k - 0.85) / 0.15) ** 2) * 0.08;
      return lerp(7, RAIL_END + 0.9, e);
    };
    const pkLook = new THREE.Vector3(0, 1.5, -118);
    const logoLook = new THREE.Vector3(0, 10, -116);

    // ---- post
    const composer = new EffectComposer(renderer);
    composer.addPass(new RenderPass(scene, camera));
    const bloom = new UnrealBloomPass(new THREE.Vector2(256, 256), 0.7, 0.55, 0.62);
    if (q >= 0.25) composer.addPass(bloom);
    composer.addPass(new OutputPass());
    const resize = () => {
      const w = innerWidth, h = Math.max(1, innerHeight);
      renderer.setSize(w, h, false);
      composer.setSize(w, h);
      camera.aspect = w / h;
      camera.fov = camera.aspect < 1.3 ? 66 : 55;
      camera.updateProjectionMatrix();
    };
    resize();
    addEventListener('resize', resize);

    const audio = new CaveAudio();
    audio.start(!muted);

    let t = 0, last = performance.now(), raf = 0, ending = false, finished = false;
    const dummy = new THREE.Object3D();
    const look = new THREE.Vector3();
    const finish = () => {
      if (finished) return;
      finished = true;
      cancelAnimationFrame(raf);
      clearInterval(watchdog);
      removeEventListener('resize', resize);
      removeEventListener('keydown', onKey, true);
      document.removeEventListener('visibilitychange', onHidden);
      audio.close();
      scene.traverse((o) => {
        const m = o as THREE.Mesh;
        m.geometry?.dispose();
        const mats = Array.isArray(m.material) ? m.material : m.material ? [m.material] : [];
        mats.forEach((x) => { (x as THREE.MeshBasicMaterial).map?.dispose(); x.dispose(); });
      });
      brickGeo.dispose();
      cubeGeo.dispose();
      dot.dispose();
      composer.dispose();
      renderer.dispose();
      renderer.forceContextLoss();
      root.remove();
      resolve();
    };
    const end = (fast: boolean) => {
      if (ending) return;
      ending = true;
      audio.fadeOut(fast ? 0.5 : 1.2);
      root.classList.add(fast ? 'out-fast' : 'out');
      setTimeout(finish, fast ? 520 : 1300);
    };
    const onKey = (e: KeyboardEvent) => { if (['Escape', 'Enter', ' '].includes(e.key)) { e.preventDefault(); e.stopPropagation(); end(true); } };
    const onHidden = () => { if (document.hidden) end(true); };
    addEventListener('keydown', onKey, true);
    document.addEventListener('visibilitychange', onHidden);
    canvas.addEventListener('click', () => end(true));
    skip.addEventListener('click', () => end(true));
    mute.addEventListener('click', () => { muted = !muted; audio.setMuted(muted); drawMute(); });
    let lastSeen = -1;
    const watchdog = setInterval(() => { if (finished) return clearInterval(watchdog); if (t === lastSeen) end(true); lastSeen = t; }, 4000);

    const frame = (now: number) => {
      raf = requestAnimationFrame(frame);
      const dt = Math.min(0.1, (now - last) / 1000);
      t += dt;
      last = now;
      if (!ending && t >= DURATION - 1.1) end(false);

      // Exposure stays low: the scene is lit by fire, not by a grading curve.
      const torchOn = smooth(3, 3.5, t);
      renderer.toneMappingExposure = 0.15 + torchOn * 0.75 + smooth(12, 14, t) * 0.15 + smooth(22.8, 23.6, t) * 2.2;
      flash.style.opacity = String(smooth(22.9, 23.5, t) * 0.9);
      fog.density = lerp(0.07, 0.045, torchOn) - smooth(11.5, 14, t) * 0.03;
      fog.color.setRGB(0.04 + smooth(11, 14, t) * 0.06, 0.012 + smooth(11, 14, t) * 0.01, 0.008);

      // Camera + shake.
      const z = rideZ(t);
      const seat = railPoint(z).add(new THREE.Vector3(0, SEAT, 0));
      camera.position.copy(seat);
      const ahead = railPoint(z - 9).add(new THREE.Vector3(0, SEAT - 0.1, 0));
      if (t < 4.6) look.copy(railPoint(4).add(new THREE.Vector3(-0.9, 3, 0.25))).lerp(ahead, smooth(3.8, 4.9, t));
      else look.copy(ahead);
      look.lerp(pkLook, smooth(-80, -96, z) * 0.9);
      look.lerp(logoLook, smooth(18.6, 20.4, t));
      // Fall: the bridge gives way, the cart tips forward into the lava.
      const tip = smooth(21.4, 23.4, t);
      camera.position.add(new THREE.Vector3(0, -tip * tip * 9, -tip * 4));
      look.lerp(new THREE.Vector3(0, CH.lavaY - 6, -108), tip);
      // Rail rumble (stronger with speed), roar shake.
      const speed = Math.abs(rideZ(t + 0.05) - z) * 20;
      const shake = speed * 0.006 + smooth(21, 21.4, t) * (1 - smooth(22.8, 23.3, t)) * 0.3 + (t > 13 && t < 16.4 ? 0.02 : 0);
      camera.position.x += (Math.random() - 0.5) * shake;
      camera.position.y += (Math.random() - 0.5) * shake + Math.sin(t * 9) * speed * 0.004;
      camera.up.set(Math.sin(z * 0.08) * 0.04 * Math.min(1, speed), 1, 0).normalize();
      camera.lookAt(look);

      // Lanterns flicker and swing a little as the cart passes.
      torches.forEach((tc2) => {
        tc2.cage.rotation.z = Math.sin(t * 1.6 + tc2.ph) * 0.06;
        const on = smooth(tc2.on, tc2.on + 0.35, t);
        const fl = 0.82 + Math.sin(t * 13 + tc2.ph) * 0.08 + Math.sin(t * 31 + tc2.ph * 2) * 0.06 + (Math.random() - 0.5) * 0.08;
        if (tc2.light) tc2.light.intensity = 22 * on * fl;
        tc2.sprites.forEach((sp, j) => {
          sp.position.set(Math.sin(t * 9 + j * 2 + tc2.ph) * 0.01, j * 0.02, 0);
          sp.scale.setScalar((0.12 + j * 0.12) * on * (0.9 + Math.sin(t * 17 + j * 3 + tc2.ph) * 0.12));
        });
      });

      // Chamber light, lava, embers.
      const chamber = smooth(11.2, 14, t);
      lavaLights.forEach((l, i) => { l.intensity = 120 * chamber * (0.85 + Math.sin(t * 1.7 + i * 2) * 0.1); });
      lavaHemi.intensity = chamber * 0.55;
      lavaMat.uniforms.uTime!.value = t;
      lavaMat.uniforms.uHeat!.value = 0.6 + chamber * 0.5 + smooth(21, 23, t) * 0.6;
      fallMat.uniforms.uTime!.value = t * 2.2;
      embMat.opacity = chamber * 0.9;
      dMat.opacity = torchOn * 0.6 * (1 - chamber);
      for (let i = 0; i < EMB; i++) {
        embPos[i * 3 + 1]! += embVel[i]! * dt;
        embPos[i * 3]! += Math.sin(t * 0.8 + i) * 0.006;
        if (embPos[i * 3 + 1]! > 19) embPos[i * 3 + 1] = CH.lavaY;
      }
      embGeo.attributes.position!.needsUpdate = true;

      // Crumbling ceiling bricks.
      cr.forEach((c, i) => {
        const age = t - c.t0;
        if (age < 0 || age > 3) { dummy.scale.setScalar(0.0001); dummy.position.set(0, -999, 0); } else {
          dummy.position.set(c.x, 18 - 4.9 * age * age, c.z);
          dummy.rotation.set(c.spin.x * age, c.spin.y * age, c.spin.z * age);
          dummy.scale.setScalar(c.s);
          if (dummy.position.y < CH.lavaY) dummy.scale.setScalar(0.0001);
        }
        dummy.updateMatrix();
        crumbs.setMatrixAt(i, dummy.matrix);
      });
      crumbs.instanceMatrix.needsUpdate = true;

      // Pumpkin assembles; the inner light wakes up when the face is complete.
      const fall = smooth(21.5, 23, t);
      pkVox.forEach((p, i) => {
        const k = clamp01((t - p.t0) / 0.9);
        const e = easeOutBack(k);
        dummy.position.lerpVectors(p.start, p.target, e);
        dummy.rotation.set(p.rot.x * (1 - e), p.rot.y * (1 - e), p.rot.z * (1 - e));
        dummy.scale.setScalar(k > 0 ? pkCell * 0.97 : 0.0001);
        if (fall > 0) { dummy.position.y -= fall * fall * (6 + (i % 7)); dummy.rotation.x += fall * (i % 5); }
        dummy.updateMatrix();
        pumpkinMesh.setMatrixAt(i, dummy.matrix);
        if (k >= 1 && !pkDone[i]) {
          pkDone[i] = 1;
          audio.clack(0.2 + Math.random() * 0.3);
          if (i % 6 === 0) sparks(v3.copy(p.target).add(PK), 4);
        }
      });
      pumpkinMesh.instanceMatrix.needsUpdate = true;
      const glow = smooth(17.6, 18.6, t) * (1 - fall * 0.6);
      pkLight.intensity = 90 * glow * (0.85 + Math.sin(t * 14) * 0.08 + Math.sin(t * 5.3) * 0.07);
      (pkCore.material as THREE.MeshBasicMaterial).color.set('#ffb347').multiplyScalar(2.6 * glow);
      pkGroup.rotation.y = Math.sin(t * 0.4) * 0.06;

      // Logo bricks snap in from above.
      lgBricks.forEach((b, i) => {
        const k = clamp01((t - b.t0) / 0.75);
        const e = easeOutBack(k);
        dummy.position.lerpVectors(b.start, b.target, e);
        dummy.rotation.set((1 - e) * 4, (1 - e) * 3, 0);
        dummy.scale.setScalar(k > 0 ? lgCell * 0.96 : 0.0001);
        if (fall > 0) { dummy.position.y -= fall * fall * (10 + (i % 9)); dummy.rotation.z += fall * (i % 4); }
        dummy.updateMatrix();
        logoMesh.setMatrixAt(i, dummy.matrix);
        if (k >= 1 && !lgDone[i]) {
          lgDone[i] = 1;
          audio.clack(0.6 + Math.random() * 0.4);
          sparks(v3.copy(b.target).add(LG), 5);
        }
      });
      logoMesh.instanceMatrix.needsUpdate = true;
      lgMat.emissive.set('#ff4a10').multiplyScalar(0.12 * smooth(20, 20.8, t) + Math.max(0, Math.sin(t * 3)) * 0.04 * smooth(20, 21, t));
      lgLight.intensity = 34 * smooth(18.2, 19.5, t) * (1 - fall);
      txtMat.opacity = smooth(20.2, 20.9, t) * (1 - smooth(21.3, 21.8, t));

      // Sparks.
      for (let i = 0; i < SP; i++) {
        if (spLife[i]! <= 0) { spPos[i * 3 + 1] = -999; continue; }
        spLife[i]! -= dt;
        spVel[i * 3 + 1]! -= 9 * dt;
        spPos[i * 3]! += spVel[i * 3]! * dt;
        spPos[i * 3 + 1]! += spVel[i * 3 + 1]! * dt;
        spPos[i * 3 + 2]! += spVel[i * 3 + 2]! * dt;
      }
      spGeo.attributes.position!.needsUpdate = true;

      composer.render();
      root.dataset.t = t.toFixed(1);
    };
    raf = requestAnimationFrame((n) => { last = n; frame(n); });
  });
}
