// Cinematic start sequence (three.js / WebGL). One continuous camera move along
// a spline: darkness and drifting dust -> a portal materialises -> the camera
// flies through it into a tunnel of floating LEGO bricks with jack-o'-lanterns
// and bats passing close by -> bricks assemble the LEGO logo while light shafts
// and lightning hit -> the logo bursts apart as the camera dives through a
// second portal into the launcher UI. All sound is synthesised (WebAudio), no
// asset files. Positions of scene objects are derived from the same timing
// table as the camera, so camera, light, particles and sound stay in sync.

import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { NOCTURNE } from '@lego/shared';

export interface IntroOptions {
  sound: boolean;
  /** 0..1, from the background quality setting. */
  quality: number;
  /** Shown on the skip button. */
  skipLabel: string;
  /** Start this many seconds in (previews / checks of single scenes). */
  startAt?: number;
}

const DURATION = 18.6;
const FLASHES = [3.4, 8.7, 13.9];

export function webglAvailable(): boolean {
  try {
    const c = document.createElement('canvas');
    return !!c.getContext('webgl2');
  } catch {
    return false;
  }
}

// ---- timing -------------------------------------------------------------------

const clamp01 = (x: number) => Math.max(0, Math.min(1, x));
const smooth = (a: number, b: number, x: number) => { const t = clamp01((x - a) / (b - a)); return t * t * (3 - 2 * t); };
const easeOutBack = (t: number) => { const c = 1.6; return 1 + (c + 1) * (t - 1) ** 3 + c * (t - 1) ** 2; };
const easeInCubic = (t: number) => t * t * t;
const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

/** Camera speed over time (relative units); integrated into progress along the path. */
const SPEED: [number, number][] = [[0, 0.12], [2.5, 0.35], [5, 1], [6.4, 1.7], [9.8, 1.35], [11, 0.22], [15.4, 0.14], [16.2, 1.9], [17.6, 3.2], [DURATION, 3.2]];

function buildProgressTable(): Float32Array {
  const n = 1024;
  const table = new Float32Array(n + 1);
  const speedAt = (t: number) => {
    for (let i = 1; i < SPEED.length; i++) {
      const [t1, v1] = SPEED[i]!, [t0, v0] = SPEED[i - 1]!;
      if (t <= t1) return lerp(v0, v1, smooth(t0, t1, t));
    }
    return SPEED[SPEED.length - 1]![1];
  };
  let acc = 0;
  for (let i = 1; i <= n; i++) {
    acc += speedAt((i / n) * DURATION) * (DURATION / n);
    table[i] = acc;
  }
  for (let i = 0; i <= n; i++) table[i]! /= acc;
  return table;
}
const TABLE = buildProgressTable();
/** Progress 0..1 along the camera path at time t. */
const U = (t: number) => {
  const f = clamp01(t / DURATION) * (TABLE.length - 1);
  const i = Math.floor(f);
  return lerp(TABLE[i]!, TABLE[Math.min(TABLE.length - 1, i + 1)]!, f - i);
};

// ---- small factories ------------------------------------------------------------

export function dotTexture(): THREE.CanvasTexture {
  const c = document.createElement('canvas');
  c.width = c.height = 64;
  const g = c.getContext('2d')!;
  const grd = g.createRadialGradient(32, 32, 0, 32, 32, 32);
  grd.addColorStop(0, 'rgba(255,255,255,1)');
  grd.addColorStop(0.35, 'rgba(255,255,255,.45)');
  grd.addColorStop(1, 'rgba(255,255,255,0)');
  g.fillStyle = grd;
  g.fillRect(0, 0, 64, 64);
  return new THREE.CanvasTexture(c);
}

export function portal(radius: number): { group: THREE.Group; update: (t: number, reveal: number) => void } {
  const group = new THREE.Group();
  const uniforms = { uTime: { value: 0 }, uReveal: { value: 0 }, uA: { value: new THREE.Color(NOCTURNE.pumpkin) }, uB: { value: new THREE.Color(NOCTURNE.violet) } };
  const ring = new THREE.Mesh(
    new THREE.TorusGeometry(radius, radius * 0.035, 20, 200),
    new THREE.ShaderMaterial({
      uniforms, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false,
      vertexShader: 'varying vec2 vUv; void main(){ vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position,1.0); }',
      fragmentShader: `uniform float uTime; uniform float uReveal; uniform vec3 uA; uniform vec3 uB; varying vec2 vUv;
        void main(){
          float a = vUv.x;                       // 0..1 around the ring
          float sweep = smoothstep(uReveal, uReveal - 0.08, a);
          float pulse = 0.65 + 0.35 * sin(a * 40.0 - uTime * 6.0);
          vec3 c = mix(uA, uB, 0.5 + 0.5 * sin(a * 6.2832 * 2.0 + uTime));
          gl_FragColor = vec4(c * (1.8 + pulse), sweep);
        }`,
    }),
  );
  const disc = new THREE.Mesh(
    new THREE.CircleGeometry(radius * 0.98, 128),
    new THREE.ShaderMaterial({
      uniforms, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide, toneMapped: false,
      vertexShader: 'varying vec2 vUv; void main(){ vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position,1.0); }',
      fragmentShader: `uniform float uTime; uniform float uReveal; uniform vec3 uA; uniform vec3 uB; varying vec2 vUv;
        void main(){
          vec2 p = vUv - 0.5; float r = length(p) * 2.0; float ang = atan(p.y, p.x);
          float spiral = sin(ang * 5.0 + r * 14.0 - uTime * 3.5) * 0.5 + 0.5;
          float swirl = sin(ang * 3.0 - r * 9.0 + uTime * 2.2) * 0.5 + 0.5;
          float core = smoothstep(1.0, 0.0, r);
          vec3 c = mix(uB, uA, spiral) * (0.25 + 0.9 * swirl * core) + vec3(1.0, 0.85, 0.7) * pow(core, 6.0) * 1.4;
          float edge = smoothstep(1.0, 0.82, r);
          gl_FragColor = vec4(c, edge * uReveal * (0.35 + 0.65 * core));
        }`,
    }),
  );
  group.add(ring, disc);
  // Rune shards orbiting the ring.
  const shardGeo = new THREE.OctahedronGeometry(radius * 0.045);
  const shardMat = new THREE.MeshBasicMaterial({ color: new THREE.Color(NOCTURNE.ember).multiplyScalar(2.5), toneMapped: false, transparent: true });
  const shards: THREE.Mesh[] = [];
  for (let i = 0; i < 18; i++) {
    const m = new THREE.Mesh(shardGeo, shardMat);
    shards.push(m);
    group.add(m);
  }
  return {
    group,
    update(t, reveal) {
      uniforms.uTime.value = t;
      uniforms.uReveal.value = reveal;
      shardMat.opacity = reveal;
      shards.forEach((m, i) => {
        const a = (i / shards.length) * Math.PI * 2 + t * 0.6;
        const rr = radius * (1.12 + 0.05 * Math.sin(t * 2 + i));
        m.position.set(Math.cos(a) * rr, Math.sin(a) * rr, 0);
        m.rotation.set(t + i, t * 1.3, 0);
        m.scale.setScalar(reveal);
      });
    },
  };
}

export function pumpkin(): THREE.Group {
  const g = new THREE.Group();
  const geo = new THREE.SphereGeometry(1, 40, 28);
  const pos = geo.attributes.position as THREE.BufferAttribute;
  for (let i = 0; i < pos.count; i++) {
    const x = pos.getX(i), y = pos.getY(i), z = pos.getZ(i);
    const rib = 1 - 0.07 * Math.abs(Math.sin(4 * Math.atan2(z, x)));
    pos.setXYZ(i, x * 1.28 * rib, y * 0.92 - (y > 0.85 ? 0.08 : 0), z * 1.28 * rib);
  }
  geo.computeVertexNormals();
  const body = new THREE.Mesh(geo, new THREE.MeshStandardMaterial({ color: NOCTURNE.pumpkin, roughness: 0.55, emissive: new THREE.Color('#8a3300'), emissiveIntensity: 1.1 }));
  const stem = new THREE.Mesh(new THREE.CylinderGeometry(0.09, 0.15, 0.45, 8), new THREE.MeshStandardMaterial({ color: '#3d2a12', roughness: 0.9 }));
  stem.position.y = 1.02;
  stem.rotation.z = 0.25;
  // Carved face: glowing decal just in front of the body.
  const c = document.createElement('canvas');
  c.width = c.height = 256;
  const x = c.getContext('2d')!;
  x.fillStyle = '#ffd27a';
  x.shadowColor = '#ff9a2a';
  x.shadowBlur = 24;
  const tri = (ax: number, ay: number, bx: number, by: number, cx: number, cy: number) => { x.beginPath(); x.moveTo(ax, ay); x.lineTo(bx, by); x.lineTo(cx, cy); x.closePath(); x.fill(); };
  tri(52, 112, 104, 112, 78, 66);
  tri(152, 112, 204, 112, 178, 66);
  tri(116, 140, 140, 140, 128, 118);
  x.beginPath();
  x.moveTo(44, 158);
  [[70, 180], [84, 166], [100, 186], [116, 170], [128, 190], [140, 170], [156, 186], [172, 166], [186, 180], [212, 158], [196, 206], [60, 206]].forEach(([a, b]) => x.lineTo(a!, b!));
  x.closePath();
  x.fill();
  const face = new THREE.Mesh(new THREE.PlaneGeometry(2.1, 2.1), new THREE.MeshBasicMaterial({ map: new THREE.CanvasTexture(c), transparent: true, blending: THREE.AdditiveBlending, depthWrite: false, toneMapped: false, color: new THREE.Color(2.2, 1.6, 1) }));
  face.position.z = 1.22;
  g.add(body, stem, face);
  return g;
}

export function bat(): { group: THREE.Group; wings: [THREE.Mesh, THREE.Mesh] } {
  const shape = new THREE.Shape();
  const pts: [number, number][] = [[0.5, 0.15], [0.9, 0.05], [1.2, 0.28], [1.12, -0.06], [0.86, -0.2], [0.62, -0.08], [0.36, -0.26], [0.1, -0.1]];
  shape.moveTo(0, 0);
  pts.forEach(([a, b]) => shape.lineTo(a, b));
  shape.closePath();
  const geo = new THREE.ShapeGeometry(shape);
  geo.rotateX(-Math.PI / 2);
  const mat = new THREE.MeshBasicMaterial({ color: '#050307', side: THREE.DoubleSide });
  const right = new THREE.Mesh(geo, mat);
  const left = new THREE.Mesh(geo, mat);
  left.scale.x = -1;
  const body = new THREE.Mesh(new THREE.SphereGeometry(0.12, 10, 8), mat);
  body.scale.set(0.8, 0.7, 1.4);
  const group = new THREE.Group();
  group.add(right, left, body);
  group.scale.setScalar(0.55);
  return { group, wings: [right, left] };
}

export function lightning(from: THREE.Vector3, length: number): THREE.Mesh {
  const pts: THREE.Vector3[] = [];
  const p = from.clone();
  for (let i = 0; i <= 14; i++) {
    pts.push(p.clone());
    p.add(new THREE.Vector3((Math.random() - 0.5) * 1.6, -length / 14, (Math.random() - 0.5) * 1.2));
  }
  return new THREE.Mesh(
    new THREE.TubeGeometry(new THREE.CatmullRomCurve3(pts, false, 'catmullrom', 0.1), 80, 0.06, 5, false),
    new THREE.MeshBasicMaterial({ color: new THREE.Color(3, 2.6, 3.4), toneMapped: false, transparent: true, opacity: 0 }),
  );
}

export function lightShaft(height: number, radius: number, color: string): THREE.Mesh {
  const geo = new THREE.ConeGeometry(radius, height, 40, 1, true);
  geo.translate(0, -height / 2, 0);
  return new THREE.Mesh(geo, new THREE.ShaderMaterial({
    uniforms: { uColor: { value: new THREE.Color(color) }, uOpacity: { value: 0 } },
    transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide,
    vertexShader: 'varying float vY; varying vec3 vN; varying vec3 vV; void main(){ vY = uv.y; vN = normalize(normalMatrix * normal); vec4 mv = modelViewMatrix * vec4(position,1.0); vV = normalize(-mv.xyz); gl_Position = projectionMatrix * mv; }',
    fragmentShader: 'uniform vec3 uColor; uniform float uOpacity; varying float vY; varying vec3 vN; varying vec3 vV; void main(){ float rim = pow(abs(dot(vN, vV)), 1.6); gl_FragColor = vec4(uColor, uOpacity * vY * vY * rim * 0.55); }',
  }));
}

/** 5x7 bitmap letters; every "#" becomes one brick. */
const FONT: Record<string, string[]> = {
  L: ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
  E: ['#####', '#....', '#....', '####.', '#....', '#....', '#####'],
  G: ['.####', '#....', '#....', '#.###', '#...#', '#...#', '.###.'],
  O: ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
};
const LETTER_COLORS = ['#e3000b', NOCTURNE.pumpkin, NOCTURNE.violet, '#f5c518'];
/** Subtitle centre below the logo, in brick cells. */
const TXT_Y = -4.9;

// ---- sound ----------------------------------------------------------------------

class IntroAudio {
  private ctx: AudioContext | null = null;
  private master: GainNode | null = null;
  private noise: AudioBuffer | null = null;
  private lastTick = 0;

  start(enabled: boolean): void {
    try {
      this.ctx = new AudioContext();
      this.master = this.ctx.createGain();
      this.master.gain.value = enabled ? 0.55 : 0;
      this.master.connect(this.ctx.destination);
      const n = this.ctx.createBuffer(1, this.ctx.sampleRate * 2, this.ctx.sampleRate);
      const d = n.getChannelData(0);
      for (let i = 0; i < d.length; i++) d[i] = Math.random() * 2 - 1;
      this.noise = n;
      this.schedule();
    } catch {
      this.ctx = null;
    }
  }

  setMuted(muted: boolean): void {
    if (this.ctx && this.master) this.master.gain.setTargetAtTime(muted ? 0 : 0.55, this.ctx.currentTime, 0.05);
  }

  private at(t: number): number { return this.ctx!.currentTime + t; }

  private schedule(): void {
    const ctx = this.ctx!;
    // Drone: two detuned saws through a slowly breathing low-pass.
    const lp = ctx.createBiquadFilter();
    lp.type = 'lowpass';
    lp.frequency.value = 180;
    const lfo = ctx.createOscillator();
    const lfoGain = ctx.createGain();
    lfo.frequency.value = 0.15;
    lfoGain.gain.value = 90;
    lfo.connect(lfoGain).connect(lp.frequency);
    const g = ctx.createGain();
    g.gain.setValueAtTime(0, this.at(0));
    g.gain.linearRampToValueAtTime(0.28, this.at(3));
    g.gain.setValueAtTime(0.28, this.at(15.5));
    g.gain.linearRampToValueAtTime(0.4, this.at(16.6));
    g.gain.linearRampToValueAtTime(0, this.at(DURATION));
    for (const f of [55, 55.45, 82.4]) {
      const o = ctx.createOscillator();
      o.type = 'sawtooth';
      o.frequency.value = f;
      o.connect(lp);
      o.start(this.at(0));
      o.stop(this.at(DURATION + 0.5));
    }
    lfo.start(this.at(0));
    lfo.stop(this.at(DURATION + 0.5));
    lp.connect(g).connect(this.master!);
    this.whoosh(5.6, 1.6, 0.5);
    this.whoosh(10.0, 1.2, 0.3);
    this.whoosh(15.9, 2.2, 0.75);
    for (const t of FLASHES) this.thunder(t);
    this.swell(13.8);
    this.boom(16.9);
  }

  private noiseSource(t: number, dur: number): AudioBufferSourceNode {
    const s = this.ctx!.createBufferSource();
    s.buffer = this.noise;
    s.loop = true;
    s.start(this.at(t));
    s.stop(this.at(t + dur));
    return s;
  }

  private whoosh(t: number, dur: number, vol: number): void {
    const ctx = this.ctx!;
    const bp = ctx.createBiquadFilter();
    bp.type = 'bandpass';
    bp.Q.value = 1.4;
    bp.frequency.setValueAtTime(260, this.at(t));
    bp.frequency.exponentialRampToValueAtTime(2800, this.at(t + dur * 0.7));
    bp.frequency.exponentialRampToValueAtTime(600, this.at(t + dur));
    const g = ctx.createGain();
    g.gain.setValueAtTime(0, this.at(t));
    g.gain.linearRampToValueAtTime(vol, this.at(t + dur * 0.65));
    g.gain.linearRampToValueAtTime(0, this.at(t + dur));
    this.noiseSource(t, dur).connect(bp).connect(g).connect(this.master!);
  }

  private thunder(t: number): void {
    const ctx = this.ctx!;
    const lp = ctx.createBiquadFilter();
    lp.type = 'lowpass';
    lp.frequency.setValueAtTime(900, this.at(t));
    lp.frequency.exponentialRampToValueAtTime(120, this.at(t + 2.2));
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.7, this.at(t + 0.04));
    g.gain.exponentialRampToValueAtTime(0.001, this.at(t + 2.4));
    this.noiseSource(t, 2.5).connect(lp).connect(g).connect(this.master!);
  }

  private swell(t: number): void {
    const ctx = this.ctx!;
    for (const f of [220, 261.63, 329.63, 440]) {
      const o = ctx.createOscillator();
      o.type = 'triangle';
      o.frequency.value = f;
      const g = ctx.createGain();
      g.gain.setValueAtTime(0, this.at(t));
      g.gain.linearRampToValueAtTime(0.06, this.at(t + 1.2));
      g.gain.linearRampToValueAtTime(0.0, this.at(t + 4));
      o.connect(g).connect(this.master!);
      o.start(this.at(t));
      o.stop(this.at(t + 4.1));
    }
  }

  private boom(t: number): void {
    const ctx = this.ctx!;
    const o = ctx.createOscillator();
    o.frequency.setValueAtTime(110, this.at(t));
    o.frequency.exponentialRampToValueAtTime(32, this.at(t + 0.9));
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.9, this.at(t));
    g.gain.exponentialRampToValueAtTime(0.001, this.at(t + 1.4));
    o.connect(g).connect(this.master!);
    o.start(this.at(t));
    o.stop(this.at(t + 1.5));
  }

  /** Brick snapping into place (rate limited). */
  tick(pitch: number): void {
    if (!this.ctx || this.ctx.currentTime - this.lastTick < 0.05) return;
    this.lastTick = this.ctx.currentTime;
    const o = this.ctx.createOscillator();
    o.type = 'square';
    o.frequency.value = 700 + pitch * 500;
    const g = this.ctx.createGain();
    g.gain.setValueAtTime(0.07, this.ctx.currentTime);
    g.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.07);
    o.connect(g).connect(this.master!);
    o.start();
    o.stop(this.ctx.currentTime + 0.08);
  }

  fadeOut(sec: number): void {
    if (this.ctx && this.master) this.master.gain.setTargetAtTime(0, this.ctx.currentTime, sec / 3);
  }

  close(): void {
    void this.ctx?.close().catch(() => {});
    this.ctx = null;
  }
}

// ---- the sequence -------------------------------------------------------------------

export function playIntro3d(opts: IntroOptions): Promise<void> {
  return new Promise((resolve) => {
    const q = clamp01(opts.quality);
    const root = document.createElement('div');
    root.className = 'intro3d';
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

    const renderer = new THREE.WebGLRenderer({ canvas, antialias: q > 0.4, powerPreference: 'high-performance' });
    renderer.setPixelRatio(Math.min(devicePixelRatio || 1, q > 0.6 ? 1.75 : q > 0.3 ? 1.25 : 1));
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 0;
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(NOCTURNE.void);
    scene.fog = new THREE.FogExp2(NOCTURNE.void, 0.03);
    const camera = new THREE.PerspectiveCamera(50, 1, 0.05, 400);

    // Camera path; scene objects are placed by time on the same path.
    const path = new THREE.CatmullRomCurve3([
      [0, 0.3, 16], [0, 0, 2], [0.5, 0.2, -12], [0, 0, -26], [-3.2, 1.6, -42], [3.2, -1.3, -58], [-1.2, 0.7, -74], [0, 0.4, -88], [0, 0.5, -100], [0, 0.5, -112], [0, 0.5, -124], [0, 0.5, -136],
    ].map(([a, b, c]) => new THREE.Vector3(a, b, c)), false, 'catmullrom', 0.5);
    const P = (u: number) => path.getPointAt(clamp01(u));
    const T = (u: number) => path.getTangentAt(clamp01(u));
    const atTime = (t: number) => P(U(t));

    scene.add(new THREE.AmbientLight('#2a1d40', 0.7));
    const hemi = new THREE.HemisphereLight('#7c5cff', '#120a06', 0.45);
    scene.add(hemi);
    const lantern = new THREE.PointLight(NOCTURNE.ember, 40, 26, 2);
    scene.add(lantern);
    const flashLight = new THREE.DirectionalLight('#d9d0ff', 0);
    flashLight.position.set(2, 10, 4);
    scene.add(flashLight);

    const dot = dotTexture();

    // Dust along the whole path.
    const dustCount = Math.round(900 + 2400 * q);
    const dustPos = new Float32Array(dustCount * 3);
    const dustCol = new Float32Array(dustCount * 3);
    const cA = new THREE.Color(NOCTURNE.violet), cB = new THREE.Color(NOCTURNE.pumpkin), cC = new THREE.Color(NOCTURNE.bone);
    for (let i = 0; i < dustCount; i++) {
      const p = P(Math.random());
      const a = Math.random() * Math.PI * 2, r = 0.8 + Math.random() * 9;
      dustPos.set([p.x + Math.cos(a) * r, p.y + Math.sin(a) * r * 0.7, p.z + (Math.random() - 0.5) * 4], i * 3);
      const c = Math.random() < 0.55 ? cA : Math.random() < 0.7 ? cB : cC;
      dustCol.set([c.r, c.g, c.b], i * 3);
    }
    const dustGeo = new THREE.BufferGeometry();
    dustGeo.setAttribute('position', new THREE.BufferAttribute(dustPos, 3));
    dustGeo.setAttribute('color', new THREE.BufferAttribute(dustCol, 3));
    const dustMat = new THREE.PointsMaterial({ size: 0.09, map: dot, vertexColors: true, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, opacity: 0 });
    const dust = new THREE.Points(dustGeo, dustMat);
    scene.add(dust);

    // Portal 1.
    const p1 = portal(3.4);
    const p1u = U(6.35);
    p1.group.position.copy(P(p1u));
    p1.group.lookAt(P(p1u).add(T(p1u)));
    scene.add(p1.group);

    // Brick tunnel.
    const brickGeo = new THREE.BoxGeometry(1.6, 0.6, 0.8);
    const studGeo = new THREE.CylinderGeometry(0.19, 0.19, 0.2, 14);
    const brickMat = new THREE.MeshStandardMaterial({ roughness: 0.32, metalness: 0.05 });
    const tunnelColors = ['#2b1d44', '#3a1f5c', '#1d1528', NOCTURNE.pumpkin, '#7a0f22', '#2b1d44', '#4c1d95'];
    const ringStart = U(6.9), ringEnd = U(10.3);
    const rings = Math.round(18 + 16 * q);
    const perRing = 14;
    const tunnel = new THREE.InstancedMesh(brickGeo, brickMat, rings * perRing);
    const tunnelStuds = new THREE.InstancedMesh(studGeo, brickMat, rings * perRing * 2);
    const m4 = new THREE.Matrix4(), qt = new THREE.Quaternion(), e = new THREE.Euler(), v = new THREE.Vector3(), s1 = new THREE.Vector3(1, 1, 1);
    const col = new THREE.Color();
    let bi = 0;
    for (let r = 0; r < rings; r++) {
      const u = lerp(ringStart, ringEnd, r / (rings - 1));
      const c = P(u), tg = T(u);
      const side = new THREE.Vector3(0, 1, 0).cross(tg).normalize();
      const up = tg.clone().cross(side).normalize();
      for (let k = 0; k < perRing; k++) {
        const a = (k / perRing) * Math.PI * 2 + r * 0.37;
        const rad = 5.6 + Math.random() * 1.6;
        v.copy(c).addScaledVector(side, Math.cos(a) * rad).addScaledVector(up, Math.sin(a) * rad);
        e.set(Math.random() * 6, Math.random() * 6, Math.random() * 6);
        qt.setFromEuler(e);
        m4.compose(v, qt, s1);
        tunnel.setMatrixAt(bi, m4);
        tunnel.setColorAt(bi, col.set(tunnelColors[(k + r) % tunnelColors.length]!));
        for (const sx of [-0.4, 0.4]) {
          const sm = new THREE.Matrix4().compose(new THREE.Vector3(sx, 0.4, 0), new THREE.Quaternion(), s1);
          tunnelStuds.setMatrixAt(bi * 2 + (sx < 0 ? 0 : 1), m4.clone().multiply(sm));
          tunnelStuds.setColorAt(bi * 2 + (sx < 0 ? 0 : 1), col);
        }
        bi++;
      }
    }
    scene.add(tunnel, tunnelStuds);

    // Pumpkins passing close to the camera.
    const pumpkins: { g: THREE.Group; base: THREE.Vector3; ph: number }[] = [];
    const pumpkinLights: THREE.PointLight[] = [];
    [4.6, 5.4, 7.4, 8.3, 9.2, 10.0].forEach((t, i) => {
      const u = U(t) + 0.035;
      const c = P(u), tg = T(u);
      const side = new THREE.Vector3(0, 1, 0).cross(tg).normalize();
      const g = pumpkin();
      g.scale.setScalar(0.75 + Math.random() * 0.35);
      const base = c.clone().addScaledVector(side, (i % 2 ? 1 : -1) * (2.4 + Math.random() * 1.4)).add(new THREE.Vector3(0, -0.9 + Math.random() * 1.6, 0));
      g.position.copy(base);
      g.lookAt(P(u - 0.05));
      scene.add(g);
      if (i < 3) {
        const l = new THREE.PointLight(NOCTURNE.pumpkin, 0, 9, 2);
        l.position.copy(base).addScaledVector(T(u), -1.5);
        scene.add(l);
        pumpkinLights.push(l);
      }
      pumpkins.push({ g, base, ph: Math.random() * 6 });
    });

    // Bats flying towards (and past) the camera.
    const bats = Array.from({ length: Math.round(8 + 10 * q) }, (_, i) => {
      const b = bat();
      scene.add(b.group);
      const t0 = 3 + (i / 18) * 8 + Math.random() * 0.6;
      return { ...b, t0, side: (Math.random() < 0.5 ? -1 : 1) * (0.9 + Math.random() * 2.6), up: -0.4 + Math.random() * 2.2, ph: Math.random() * 6 };
    });

    // Logo: built where the camera arrives when it dives, so the hold distance is exact.
    const logoU = U(16.35);
    const holdU = U(13);
    const logoCenter = P(logoU);
    const holdDist = logoCenter.distanceTo(P(holdU));
    const logo = new THREE.Group();
    logo.position.copy(logoCenter);
    logo.lookAt(P(holdU));
    scene.add(logo);
    const letters = 'LEGO';
    const cols = letters.length * 5 + (letters.length - 1);
    let cell = 1;
    const fitLogo = () => {
      const vh = 2 * holdDist * Math.tan(THREE.MathUtils.degToRad(camera.fov / 2));
      cell = Math.min((vh * camera.aspect * 0.62) / cols, (vh * 0.34) / 7);
    };
    camera.aspect = innerWidth / Math.max(1, innerHeight);
    fitLogo();
    const logoBricks: { target: THREE.Vector3; start: THREE.Vector3; rot0: THREE.Euler; t0: number; landed: boolean; color: THREE.Color; burst: THREE.Vector3; letter: number }[] = [];
    letters.split('').forEach((ch, li) => {
      FONT[ch]!.forEach((row, y) => {
        [...row].forEach((px, x) => {
          if (px !== '#') return;
          const tx = (li * 6 + x - (cols - 1) / 2) * cell;
          const ty = (3 - y) * cell + cell * 1.2;
          const ang = Math.random() * Math.PI * 2;
          logoBricks.push({
            target: new THREE.Vector3(tx, ty, 0),
            start: new THREE.Vector3(Math.cos(ang) * (8 + Math.random() * 10), Math.sin(ang) * (6 + Math.random() * 8), holdDist * (0.6 + Math.random() * 0.5)),
            rot0: new THREE.Euler(Math.random() * 7, Math.random() * 7, Math.random() * 7),
            t0: 0,
            landed: false,
            color: new THREE.Color(LETTER_COLORS[li]!),
            burst: new THREE.Vector3(tx, ty, 0).normalize().multiplyScalar(6 + Math.random() * 8).add(new THREE.Vector3(0, 0, holdDist * (0.8 + Math.random() * 0.6))),
            letter: li,
          });
        });
      });
    });
    logoBricks.sort((a, b) => a.letter - b.letter || b.target.y - a.target.y);
    logoBricks.forEach((b, i) => { b.t0 = 10.7 + (i / logoBricks.length) * 3.0 + Math.random() * 0.15; });
    const lbGeo = new THREE.BoxGeometry(0.96, 0.96, 0.55);
    const lsGeo = new THREE.CylinderGeometry(0.3, 0.3, 0.2, 18).rotateX(Math.PI / 2);
    const lbMat = new THREE.MeshStandardMaterial({ roughness: 0.28, metalness: 0.08, emissive: '#000000' });
    const logoMesh = new THREE.InstancedMesh(lbGeo, lbMat, logoBricks.length);
    const logoStuds = new THREE.InstancedMesh(lsGeo, lbMat, logoBricks.length);
    logoBricks.forEach((b, i) => { logoMesh.setColorAt(i, b.color); logoStuds.setColorAt(i, b.color); });
    logo.add(logoMesh, logoStuds);
    const logoLight = new THREE.PointLight(NOCTURNE.violet, 0, 30, 2);
    logoLight.position.set(0, 0, holdDist * 0.35);
    logo.add(logoLight);
    const backGlow = new THREE.Mesh(new THREE.PlaneGeometry(1, 1), new THREE.MeshBasicMaterial({ map: dot, color: new THREE.Color(NOCTURNE.violet).multiplyScalar(1.4), transparent: true, opacity: 0, blending: THREE.AdditiveBlending, depthWrite: false, toneMapped: false }));
    backGlow.position.z = -1.2;
    logo.add(backGlow);

    // "LAUNCHER · HALLOWEEN EDITION" as a canvas texture.
    const txtCanvas = document.createElement('canvas');
    txtCanvas.width = 2048;
    txtCanvas.height = 400;
    const txtTex = new THREE.CanvasTexture(txtCanvas);
    txtTex.colorSpace = THREE.SRGBColorSpace;
    const drawText = () => {
      const g = txtCanvas.getContext('2d')! as CanvasRenderingContext2D & { letterSpacing: string };
      g.clearRect(0, 0, 2048, 400);
      g.textAlign = 'center';
      g.textBaseline = 'middle';
      g.letterSpacing = '42px';
      g.font = '700 190px Poppins, Inter, sans-serif';
      const grd = g.createLinearGradient(300, 0, 1748, 0);
      grd.addColorStop(0, NOCTURNE.ember);
      grd.addColorStop(0.5, NOCTURNE.pumpkin);
      grd.addColorStop(1, NOCTURNE.hex);
      g.shadowColor = NOCTURNE.pumpkin;
      g.shadowBlur = 40;
      g.fillStyle = grd;
      g.fillText('LAUNCHER', 1024, 140);
      g.letterSpacing = '30px';
      g.font = '500 64px Poppins, Inter, sans-serif';
      g.shadowColor = NOCTURNE.violet;
      g.fillStyle = NOCTURNE.bone;
      g.fillText('HALLOWEEN EDITION', 1024, 320);
      txtTex.needsUpdate = true;
    };
    drawText();
    void document.fonts?.load('700 64px Poppins').then(drawText).catch(() => {});
    const txtMat = new THREE.MeshBasicMaterial({ map: txtTex, transparent: true, opacity: 0, depthWrite: false, toneMapped: false, color: new THREE.Color(1.2, 1.2, 1.2) });
    const txt = new THREE.Mesh(new THREE.PlaneGeometry(1, 400 / 2048), txtMat);
    logo.add(txt);
    const layoutLogo = () => {
      const w = cols * cell;
      txt.scale.setScalar(w * 1.05);
      txt.position.set(0, TXT_Y * cell, 0.2);
      backGlow.scale.set(w * 1.9, w * 1.1, 1);
      logoLight.distance = holdDist * 2;
    };
    layoutLogo();

    // Final portal right behind the logo; shafts and lightning.
    const p2 = portal(1);
    const p2u = U(17.3);
    p2.group.position.copy(P(p2u));
    p2.group.lookAt(P(p2u).add(T(p2u)));
    scene.add(p2.group);
    const shafts = [lightShaft(26, 4.5, NOCTURNE.violet), lightShaft(26, 3.2, NOCTURNE.pumpkin), lightShaft(30, 5.5, NOCTURNE.hex)];
    shafts[0]!.position.copy(atTime(4)).add(new THREE.Vector3(-3, 12, -6));
    shafts[0]!.rotation.z = 0.35;
    shafts[1]!.position.copy(atTime(8.5)).add(new THREE.Vector3(3, 12, -4));
    shafts[1]!.rotation.z = -0.3;
    shafts[2]!.position.copy(logoCenter).add(new THREE.Vector3(0, 16, -3));
    shafts.forEach((m) => scene.add(m));
    const bolts = FLASHES.map((t) => {
      const ahead = P(Math.min(1, U(t) + 0.08));
      const b = lightning(ahead.add(new THREE.Vector3((Math.random() < 0.5 ? -1 : 1) * (5 + Math.random() * 4), 9, -6)), 13);
      scene.add(b);
      return b;
    });

    // Sparks when bricks land; embers rising around the logo.
    const SPARKS = 700;
    const spPos = new Float32Array(SPARKS * 3), spVel = new Float32Array(SPARKS * 3), spLife = new Float32Array(SPARKS), spCol = new Float32Array(SPARKS * 3);
    const spGeo = new THREE.BufferGeometry();
    spGeo.setAttribute('position', new THREE.BufferAttribute(spPos, 3));
    spGeo.setAttribute('color', new THREE.BufferAttribute(spCol, 3));
    const sparks = new THREE.Points(spGeo, new THREE.PointsMaterial({ size: 0.12, map: dot, vertexColors: true, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false }));
    logo.add(sparks);
    let spNext = 0;
    const burst = (at: THREE.Vector3, c: THREE.Color, n: number) => {
      for (let i = 0; i < n; i++) {
        const k = spNext++ % SPARKS;
        spPos.set([at.x, at.y, at.z + 0.3], k * 3);
        const a = Math.random() * Math.PI * 2, sp = 1.5 + Math.random() * 4;
        spVel.set([Math.cos(a) * sp, Math.sin(a) * sp, Math.random() * 3], k * 3);
        spLife[k] = 0.5 + Math.random() * 0.6;
        spCol.set([c.r * 2.2 + 0.4, c.g * 2.2 + 0.3, c.b * 2.2 + 0.2], k * 3);
      }
    };

    // Post processing.
    const composer = new EffectComposer(renderer);
    composer.addPass(new RenderPass(scene, camera));
    const bloom = new UnrealBloomPass(new THREE.Vector2(256, 256), 0.85, 0.6, 0.18);
    if (q >= 0.3) composer.addPass(bloom);
    composer.addPass(new OutputPass());

    const resize = () => {
      const w = innerWidth, h = Math.max(1, innerHeight);
      renderer.setSize(w, h, false);
      composer.setSize(w, h);
      camera.aspect = w / h;
      // Keep the logo inside narrow windows.
      camera.fov = camera.aspect < 1.3 ? 60 : 50;
      camera.updateProjectionMatrix();
    };
    resize();
    addEventListener('resize', resize);

    const audio = new IntroAudio();
    audio.start(!muted);

    // ---- frame loop
    let t = 0;
    let last = performance.now();
    let raf = 0;
    let ending = false;
    let finished = false;
    const lookTarget = new THREE.Vector3();
    const tmp = new THREE.Vector3();
    const dummy = new THREE.Object3D();
    const landed = new THREE.Color();

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
    const onKey = (e: KeyboardEvent) => {
      if (['Escape', 'Enter', ' '].includes(e.key)) { e.preventDefault(); e.stopPropagation(); end(true); }
    };
    const onHidden = () => { if (document.hidden) end(true); };
    addEventListener('keydown', onKey, true);
    document.addEventListener('visibilitychange', onHidden);
    canvas.addEventListener('click', () => end(true));
    skip.addEventListener('click', () => end(true));
    mute.addEventListener('click', () => { muted = !muted; audio.setMuted(muted); drawMute(); });
    // Safety net: only bail out if frames stop coming (slow PCs just play slower).
    let lastSeen = -1;
    const watchdog = setInterval(() => {
      if (finished) return clearInterval(watchdog);
      if (t === lastSeen) end(true);
      lastSeen = t;
    }, 4000);

    const frame = (now: number) => {
      raf = requestAnimationFrame(frame);
      t += Math.min(0.1, (now - last) / 1000);
      last = now;
      if (!ending && t >= DURATION - 1.2) end(false);

      // Exposure: fade up from black; brightening flash on the dive.
      const flashAmt = FLASHES.reduce((m, ft) => Math.max(m, t >= ft && t < ft + 0.5 ? (1 - (t - ft) / 0.5) ** 2 * (Math.sin((t - ft) * 60) > -0.3 ? 1 : 0.4) : 0), 0);
      renderer.toneMappingExposure = smooth(0.2, 2.8, t) * 1.15 + smooth(16.6, 17.2, t) * 1.6 + flashAmt * 0.35;
      flash.style.opacity = String(flashAmt * 0.35 + smooth(16.8, 17.3, t) * (1 - smooth(17.3, 18.2, t)) * 0.85);
      flashLight.intensity = flashAmt * 4;
      bolts.forEach((b, i) => { (b.material as THREE.MeshBasicMaterial).opacity = t >= FLASHES[i]! && t < FLASHES[i]! + 0.35 ? flashAmt : 0; });
      dustMat.opacity = smooth(0.3, 3, t) * 0.9;

      // Camera.
      const u = U(t);
      const pos = P(u);
      const hold = smooth(10.4, 11.4, t) * (1 - smooth(15.5, 16.2, t));
      pos.x += Math.sin(t * 0.5) * 0.6 * hold;
      pos.y += Math.cos(t * 0.43) * 0.3 * hold;
      camera.position.copy(pos);
      lookTarget.copy(P(Math.min(1, u + 0.02)));
      tmp.copy(logoCenter);
      lookTarget.lerp(tmp, hold);
      camera.up.set(0, 1, 0);
      camera.lookAt(lookTarget);
      const tunnelRoll = smooth(6.6, 7.4, t) * (1 - smooth(9.8, 10.8, t));
      camera.rotateZ(Math.sin(t * 0.8) * 0.16 * tunnelRoll);
      lantern.position.copy(camera.position).addScaledVector(T(u), 2.5);
      lantern.intensity = 25 + 20 * smooth(5.5, 7, t);

      // Portals.
      p1.update(t, smooth(2.2, 5.4, t) * (1 - smooth(7.2, 8, t)));
      const r2 = smooth(14.4, 15.6, t);
      p2.group.scale.setScalar(lerp(0.01, 7.5, r2));
      p2.update(t, r2);

      // Pumpkins bob, flicker.
      pumpkins.forEach((p, i) => {
        p.g.position.copy(p.base).add(tmp.set(0, Math.sin(t * 1.3 + p.ph) * 0.25, 0));
        p.g.rotation.y += 0.004;
        const l = pumpkinLights[i];
        if (l) l.intensity = (14 + Math.sin(t * 17 + p.ph) * 3 + Math.sin(t * 7.3) * 2) * smooth(3.5, 5, t);
      });

      // Bats: towards the camera, wings flapping.
      bats.forEach((b) => {
        const age = t - b.t0;
        b.group.visible = age > 0 && age < 3.2;
        if (!b.group.visible) return;
        const bu = U(b.t0) + 0.06 - age * 0.032;
        const c = P(bu), tg = T(bu);
        const side = tmp.set(0, 1, 0).cross(tg).normalize();
        b.group.position.copy(c).addScaledVector(side, b.side + Math.sin(age * 3 + b.ph) * 0.4).add(new THREE.Vector3(0, b.up + Math.sin(age * 5 + b.ph) * 0.2, 0));
        b.group.lookAt(P(bu - 0.02).addScaledVector(side, b.side));
        const f = Math.sin(t * 22 + b.ph) * 0.9;
        b.wings[0].rotation.z = f;
        b.wings[1].rotation.z = -f;
      });

      // Light shafts.
      (shafts[0]!.material as THREE.ShaderMaterial).uniforms.uOpacity!.value = smooth(2.5, 4.5, t) * (1 - smooth(7, 8, t)) + flashAmt * 0.4;
      (shafts[1]!.material as THREE.ShaderMaterial).uniforms.uOpacity!.value = smooth(7.5, 8.5, t) * (1 - smooth(10.2, 11, t));
      (shafts[2]!.material as THREE.ShaderMaterial).uniforms.uOpacity!.value = smooth(11, 13, t) * (1 - smooth(16, 16.6, t)) * (0.85 + Math.sin(t * 2) * 0.15);

      // Logo assembly, then burst apart for the dive.
      const blast = easeInCubic(smooth(15.7, 16.9, t));
      let landedNow = 0;
      logoBricks.forEach((b, i) => {
        const k = clamp01((t - b.t0) / 1.1);
        const e2 = easeOutBack(k);
        dummy.position.lerpVectors(b.start, b.target, e2);
        dummy.rotation.set(b.rot0.x * (1 - e2), b.rot0.y * (1 - e2), b.rot0.z * (1 - e2));
        dummy.scale.setScalar(k > 0 ? cell * 0.94 : 0.0001);
        if (blast > 0) {
          dummy.position.addScaledVector(b.burst, blast);
          dummy.rotation.x += blast * (3 + (i % 5));
          dummy.rotation.y += blast * (2 + (i % 3));
        }
        dummy.updateMatrix();
        logoMesh.setMatrixAt(i, dummy.matrix);
        dummy.translateZ(0.28);
        dummy.updateMatrix();
        logoStuds.setMatrixAt(i, dummy.matrix);
        if (k >= 1 && !b.landed) {
          b.landed = true;
          landedNow++;
          burst(b.target, b.color, q > 0.5 ? 9 : 5);
          audio.tick(b.letter / 3 + Math.random() * 0.2);
          logoMesh.setColorAt(i, landed.copy(b.color).multiplyScalar(1.8));
        } else if (b.landed && blast === 0) {
          logoMesh.setColorAt(i, landed.copy(b.color).multiplyScalar(0.85 + 0.12 * Math.sin(t * 3 + b.target.x)));
        }
      });
      logoMesh.instanceMatrix.needsUpdate = true;
      logoStuds.instanceMatrix.needsUpdate = true;
      if (logoMesh.instanceColor) logoMesh.instanceColor.needsUpdate = true;
      lbMat.emissive.set(NOCTURNE.violet).multiplyScalar(0.06 * smooth(13.6, 14.4, t) + flashAmt * 0.2);
      logoLight.intensity = 18 * smooth(10.6, 12, t) * (1 - blast);
      (backGlow.material as THREE.MeshBasicMaterial).opacity = smooth(11, 13.5, t) * (0.3 + 0.08 * Math.sin(t * 2.4)) * (1 - blast);
      txtMat.opacity = smooth(13.7, 14.6, t) * (1 - smooth(15.6, 16.2, t));
      txt.position.y = TXT_Y * cell - (1 - smooth(13.7, 14.6, t)) * cell * 1.5;
      if (landedNow > 3) camera.position.x += (Math.random() - 0.5) * 0.04;

      // Sparks.
      const dt = 1 / 60;
      for (let i = 0; i < SPARKS; i++) {
        if (spLife[i]! <= 0) { spPos[i * 3 + 2] = -9999; continue; }
        spLife[i]! -= dt;
        spVel[i * 3 + 1]! -= 6 * dt;
        spPos[i * 3]! += spVel[i * 3]! * dt;
        spPos[i * 3 + 1]! += spVel[i * 3 + 1]! * dt;
        spPos[i * 3 + 2]! += spVel[i * 3 + 2]! * dt;
      }
      spGeo.attributes.position!.needsUpdate = true;
      spGeo.attributes.color!.needsUpdate = true;

      composer.render();
      root.dataset.t = t.toFixed(1);
    };
    raf = requestAnimationFrame((n) => { last = n; frame(n); });
  });
}
