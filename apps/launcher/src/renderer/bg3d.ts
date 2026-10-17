// Real-time 3D launcher backgrounds (three.js): graveyard, forest, castle,
// portal, particle tunnel, sci-fi grid and abstract rooms. One renderer on its
// own canvas; scenes are rebuilt only when the scene or the particle density
// changes, everything else (speed, light, fog, glow, theme colours) applies
// live. The camera drifts slowly and follows the mouse a little for parallax.

import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { OutputPass } from 'three/examples/jsm/postprocessing/OutputPass.js';
import { BokehPass } from 'three/examples/jsm/postprocessing/BokehPass.js';
import { NOCTURNE } from '@lego/shared';
import { bat, dotTexture, lightning, lightShaft, portal, pumpkin } from './intro3d.ts';
import type { DesignFx } from '../core/design.ts';

export const SCENES_3D = ['graveyard', 'forest', 'castle', 'portal', 'tunnel', 'scifi', 'rooms'] as const;
export type Scene3DId = (typeof SCENES_3D)[number];
export const is3D = (id: string): id is Scene3DId => (SCENES_3D as readonly string[]).includes(id);

export interface Palette3D { bg: string; accent: string; accent2: string }

export interface Ctx { scene: THREE.Scene; camera: THREE.PerspectiveCamera; density: number; q: number; pal: Palette3D; dot: THREE.Texture }
export interface Built {
  /** Camera rest position and look-at target. */
  cam: THREE.Vector3;
  look: THREE.Vector3;
  fog: number;
  exposure: number;
  /** Lights whose intensity follows the "light" slider (base intensity stored). */
  lights: { l: THREE.Light; base: number }[];
  update(t: number, dt: number): void;
}

const R = (a: number, b: number) => a + Math.random() * (b - a);
const col = (c: string) => new THREE.Color(c);

// ---- shared pieces -------------------------------------------------------------

export function wisps(c: Ctx, n: number, box: { x: number; y: [number, number]; z: [number, number] }, colors: string[], size = 0.18): (t: number) => void {
  const count = Math.max(4, Math.round(n));
  const pos = new Float32Array(count * 3);
  const base = new Float32Array(count * 3);
  const ph = new Float32Array(count);
  const cl = new Float32Array(count * 3);
  for (let i = 0; i < count; i++) {
    base.set([R(-box.x, box.x), R(box.y[0], box.y[1]), R(box.z[0], box.z[1])], i * 3);
    ph[i] = Math.random() * 100;
    const k = col(colors[i % colors.length]!).multiplyScalar(1.6);
    cl.set([k.r, k.g, k.b], i * 3);
  }
  const geo = new THREE.BufferGeometry();
  geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
  geo.setAttribute('color', new THREE.BufferAttribute(cl, 3));
  const pts = new THREE.Points(geo, new THREE.PointsMaterial({ size, map: c.dot, vertexColors: true, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false }));
  c.scene.add(pts);
  return (t) => {
    for (let i = 0; i < count; i++) {
      const p = ph[i]!;
      pos[i * 3] = base[i * 3]! + Math.sin(t * 0.3 + p) * 0.8;
      pos[i * 3 + 1] = base[i * 3 + 1]! + Math.sin(t * 0.5 + p * 2) * 0.5;
      pos[i * 3 + 2] = base[i * 3 + 2]! + Math.cos(t * 0.25 + p) * 0.8;
    }
    geo.attributes.position!.needsUpdate = true;
    (pts.material as THREE.PointsMaterial).opacity = 0.75 + Math.sin(t * 2) * 0.15;
  };
}

export function moon(c: Ctx, at: THREE.Vector3, size: number): void {
  const disc = new THREE.Mesh(new THREE.CircleGeometry(size, 64), new THREE.MeshBasicMaterial({ color: new THREE.Color('#fff4e0').multiplyScalar(1.4), toneMapped: false, fog: false }));
  disc.position.copy(at);
  disc.lookAt(0, at.y * 0.4, 20);
  const glow = new THREE.Sprite(new THREE.SpriteMaterial({ map: c.dot, color: col('#c9b8ff'), transparent: true, opacity: 0.55, depthWrite: false, blending: THREE.AdditiveBlending, fog: false }));
  glow.scale.setScalar(size * 7);
  glow.position.copy(at).add(new THREE.Vector3(0, 0, -0.5));
  c.scene.add(disc, glow);
}

export function ground(c: Ctx, color: string, rough = 1): THREE.Mesh {
  const g = new THREE.PlaneGeometry(220, 220, 90, 90);
  g.rotateX(-Math.PI / 2);
  const p = g.attributes.position as THREE.BufferAttribute;
  for (let i = 0; i < p.count; i++) {
    const x = p.getX(i), z = p.getZ(i);
    p.setY(i, Math.sin(x * 0.11) * 0.35 + Math.cos(z * 0.09 + x * 0.05) * 0.45 + (z < -40 ? (Math.sin(x * 0.05) + 1) * (-z - 40) * 0.04 : 0));
  }
  g.computeVertexNormals();
  const m = new THREE.Mesh(g, new THREE.MeshStandardMaterial({ color, roughness: rough }));
  c.scene.add(m);
  return m;
}

export function fogLayers(c: Ctx, y: number, tint: string): (t: number) => void {
  const uniforms = { uTime: { value: 0 }, uColor: { value: col(tint) } };
  const mat = new THREE.ShaderMaterial({
    uniforms, transparent: true, depthWrite: false,
    vertexShader: 'varying vec2 vUv; varying vec3 vW; void main(){ vUv = uv; vec4 w = modelMatrix * vec4(position,1.0); vW = w.xyz; gl_Position = projectionMatrix * viewMatrix * w; }',
    fragmentShader: `uniform float uTime; uniform vec3 uColor; varying vec2 vUv; varying vec3 vW;
      float h(vec2 p){ return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
      float n(vec2 p){ vec2 i = floor(p), f = fract(p); f = f*f*(3.0-2.0*f); return mix(mix(h(i), h(i+vec2(1,0)), f.x), mix(h(i+vec2(0,1)), h(i+vec2(1,1)), f.x), f.y); }
      void main(){
        vec2 p = vW.xz * 0.08 + vec2(uTime * 0.03, uTime * 0.012);
        float v = n(p) * 0.6 + n(p * 2.3 - uTime * 0.02) * 0.4;
        float edge = smoothstep(0.0, 0.25, vUv.x) * smoothstep(1.0, 0.75, vUv.x) * smoothstep(0.0, 0.25, vUv.y) * smoothstep(1.0, 0.75, vUv.y);
        gl_FragColor = vec4(uColor, smoothstep(0.35, 0.9, v) * 0.17 * edge);
      }`,
  });
  for (let i = 0; i < 3; i++) {
    const m = new THREE.Mesh(new THREE.PlaneGeometry(120, 90), mat);
    m.rotation.x = -Math.PI / 2;
    m.position.set(0, y + i * 0.45, -20);
    c.scene.add(m);
  }
  return (t) => { uniforms.uTime.value = t; };
}

/** Branch transforms of a dead tree (unit cylinders, for instancing). */
function treeMatrices(origin: THREE.Vector3, height: number, out: THREE.Matrix4[]): void {
  const grow = (from: THREE.Vector3, dir: THREE.Vector3, len: number, r: number, depth: number) => {
    const mid = from.clone().addScaledVector(dir, len / 2);
    const q = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir);
    out.push(new THREE.Matrix4().compose(mid, q, new THREE.Vector3(r, len, r)));
    if (depth === 0) return;
    const tip = from.clone().addScaledVector(dir, len);
    const kids = depth === 3 ? 3 : 2;
    for (let i = 0; i < kids; i++) {
      const d = dir.clone().add(new THREE.Vector3(R(-0.9, 0.9), R(0.1, 0.6), R(-0.9, 0.9))).normalize();
      grow(tip, d, len * R(0.5, 0.72), r * 0.6, depth - 1);
    }
  };
  grow(origin, new THREE.Vector3(R(-0.08, 0.08), 1, R(-0.08, 0.08)).normalize(), height * 0.45, height * 0.06, 3);
}

export function forestInstances(c: Ctx, trees: THREE.Vector3[], heights: number[], color = '#0d0912'): void {
  const ms: THREE.Matrix4[] = [];
  trees.forEach((p, i) => treeMatrices(p, heights[i]!, ms));
  const inst = new THREE.InstancedMesh(new THREE.CylinderGeometry(0.7, 1, 1, 6), new THREE.MeshStandardMaterial({ color, roughness: 1 }), ms.length);
  ms.forEach((m, i) => inst.setMatrixAt(i, m));
  c.scene.add(inst);
}

export function batFlock(c: Ctx, n: number, center: THREE.Vector3, radius: number): (t: number) => void {
  const flock = Array.from({ length: n }, () => {
    const b = bat();
    b.group.scale.setScalar(R(0.5, 0.9));
    c.scene.add(b.group);
    return { ...b, r: radius * R(0.6, 1.2), sp: R(0.25, 0.5) * (Math.random() < 0.5 ? -1 : 1), ph: R(0, 6), h: R(-1.5, 2.5) };
  });
  return (t) => flock.forEach((b) => {
    const a = t * b.sp + b.ph;
    b.group.position.set(center.x + Math.cos(a) * b.r, center.y + b.h + Math.sin(t * 1.3 + b.ph) * 0.6, center.z + Math.sin(a) * b.r * 0.6);
    const ahead = new THREE.Vector3(center.x + Math.cos(a + Math.sign(b.sp) * 0.1) * b.r, b.group.position.y, center.z + Math.sin(a + Math.sign(b.sp) * 0.1) * b.r * 0.6);
    b.group.lookAt(ahead);
    const f = Math.sin(t * 20 + b.ph) * 0.9;
    b.wings[0].rotation.z = f;
    b.wings[1].rotation.z = -f;
  });
}

export function stoneMat(): THREE.MeshStandardMaterial {
  return new THREE.MeshStandardMaterial({ color: '#4a4358', roughness: 0.92 });
}

export function tombstone(mat: THREE.Material): THREE.Group {
  const g = new THREE.Group();
  const w = R(0.8, 1.3), h = R(1.1, 1.9);
  const slab = new THREE.Mesh(new THREE.BoxGeometry(w, h, 0.25), mat);
  slab.position.y = h / 2;
  g.add(slab);
  if (Math.random() < 0.6) {
    const top = new THREE.Mesh(new THREE.CylinderGeometry(w / 2, w / 2, 0.25, 20, 1, false, 0, Math.PI), mat);
    top.rotation.set(Math.PI / 2, 0, Math.PI / 2);
    top.position.y = h;
    g.add(top);
  } else if (Math.random() < 0.5) {
    const v = new THREE.Mesh(new THREE.BoxGeometry(0.18, 0.9, 0.2), mat);
    const hz = new THREE.Mesh(new THREE.BoxGeometry(0.6, 0.18, 0.2), mat);
    v.position.y = h + 0.45;
    hz.position.y = h + 0.55;
    g.add(v, hz);
  }
  g.rotation.set(R(-0.12, 0.08), R(-0.3, 0.3), R(-0.12, 0.12));
  return g;
}

export function pumpkinPatch(c: Ctx, spots: THREE.Vector3[], lit: number): { lights: Built['lights']; update: (t: number) => void } {
  const lights: Built['lights'] = [];
  const ls: THREE.PointLight[] = [];
  spots.forEach((p, i) => {
    const g = pumpkin();
    g.scale.setScalar(R(0.5, 0.8));
    g.position.copy(p);
    g.rotation.y = R(-0.6, 0.6);
    c.scene.add(g);
    if (i < lit) {
      const l = new THREE.PointLight(NOCTURNE.pumpkin, 10, 9, 2);
      l.position.copy(p).add(new THREE.Vector3(0, 0.6, 1.2));
      c.scene.add(l);
      ls.push(l);
      lights.push({ l, base: 10 });
    }
  });
  return { lights, update: (t) => ls.forEach((l, i) => { l.intensity = lights[i]!.base * (0.85 + Math.sin(t * 13 + i * 3) * 0.08 + Math.sin(t * 5.1 + i) * 0.07) * (l.userData.k ?? 1); }) };
}

// ---- scenes --------------------------------------------------------------------------

const BUILD: Record<Scene3DId, (c: Ctx) => Built> = {
  graveyard(c) {
    ground(c, '#120d18');
    const mat = stoneMat();
    for (let i = 0; i < 30; i++) {
      const t = tombstone(mat);
      const row = Math.floor(i / 6);
      t.position.set((i % 6) * 4.4 - 11 + R(-1, 1), 0, -4 - row * 5.5 + R(-1, 1));
      c.scene.add(t);
    }
    const fence = new THREE.InstancedMesh(new THREE.BoxGeometry(0.12, 1.6, 0.12), new THREE.MeshStandardMaterial({ color: '#1b1622', roughness: 0.7, metalness: 0.4 }), 60);
    for (let i = 0; i < 60; i++) fence.setMatrixAt(i, new THREE.Matrix4().compose(new THREE.Vector3(-30 + i, 0.8, -32), new THREE.Quaternion().setFromEuler(new THREE.Euler(0, 0, R(-0.06, 0.06))), new THREE.Vector3(1, R(0.8, 1.15), 1)));
    c.scene.add(fence);
    forestInstances(c, [new THREE.Vector3(-14, 0, -10), new THREE.Vector3(15, 0, -14), new THREE.Vector3(-20, 0, -26), new THREE.Vector3(22, 0, -30), new THREE.Vector3(6, 0, -40)], [9, 11, 13, 12, 14]);
    moon(c, new THREE.Vector3(12, 18, -80), 6);
    const moonLight = new THREE.DirectionalLight('#a796ff', 1.3);
    moonLight.position.set(12, 18, -40);
    const amb = new THREE.HemisphereLight('#4b3a7a', '#0a0610', 0.55);
    c.scene.add(moonLight, amb);
    const patch = pumpkinPatch(c, [new THREE.Vector3(-3.2, 0.4, 2), new THREE.Vector3(3.6, 0.4, 0.5), new THREE.Vector3(-6, 0.4, -6), new THREE.Vector3(7, 0.4, -9), new THREE.Vector3(0.5, 0.4, -12)], c.q > 0.5 ? 3 : 2);
    const fogU = fogLayers(c, 0.25, '#b9a6ff');
    const w = wisps(c, 30 + 90 * c.density, { x: 18, y: [0.4, 4], z: [-30, 4] }, [NOCTURNE.slime, NOCTURNE.pumpkin, c.pal.accent2]);
    const bats = batFlock(c, Math.round(4 + 6 * c.density), new THREE.Vector3(10, 14, -60), 14);
    return {
      cam: new THREE.Vector3(0, 2.4, 10), look: new THREE.Vector3(0, 2.2, -12), fog: 0.035, exposure: 1.05,
      lights: [{ l: moonLight, base: 1.3 }, { l: amb, base: 0.55 }, ...patch.lights],
      update(t) { fogU(t); w(t); bats(t); patch.update(t); },
    };
  },

  forest(c) {
    ground(c, '#0f0b10');
    const trees: THREE.Vector3[] = [];
    const hs: number[] = [];
    const n = Math.round(26 + 34 * Math.max(0.3, c.q));
    for (let i = 0; i < n; i++) {
      const z = R(-70, 0);
      const x = R(3, 26) * (Math.random() < 0.5 ? -1 : 1);
      trees.push(new THREE.Vector3(x * (1 + -z / 80), 0, z));
      hs.push(R(9, 18));
    }
    forestInstances(c, trees, hs, '#0b0810');
    moon(c, new THREE.Vector3(-6, 22, -95), 7);
    const moonLight = new THREE.DirectionalLight('#9f8cff', 1.0);
    moonLight.position.set(-6, 22, -40);
    const amb = new THREE.HemisphereLight('#3a2c66', '#050308', 0.5);
    c.scene.add(moonLight, amb);
    const shafts = [lightShaft(40, 6, '#b7a4ff'), lightShaft(40, 4, '#8b5cf6'), lightShaft(36, 5, '#c9b8ff')];
    shafts.forEach((s, i) => { s.position.set(-10 + i * 9, 30, -18 - i * 10); s.rotation.z = 0.25 - i * 0.12; c.scene.add(s); });
    const fogU = fogLayers(c, 0.3, '#9d8cd6');
    const ff = wisps(c, 40 + 120 * c.density, { x: 22, y: [0.5, 6], z: [-50, 4] }, ['#ffd27a', NOCTURNE.slime, '#ffb347'], 0.14);
    const patch = pumpkinPatch(c, [new THREE.Vector3(-2.5, 0.35, 1), new THREE.Vector3(4, 0.35, -3)], 2);
    return {
      cam: new THREE.Vector3(0, 2.2, 10), look: new THREE.Vector3(0, 3.5, -20), fog: 0.045, exposure: 1.1,
      lights: [{ l: moonLight, base: 1.0 }, { l: amb, base: 0.5 }, ...patch.lights],
      update(t) {
        fogU(t); ff(t); patch.update(t);
        shafts.forEach((s, i) => { ((s.material as THREE.ShaderMaterial).uniforms.uOpacity!.value = 0.35 + Math.sin(t * 0.4 + i * 2) * 0.12); });
      },
    };
  },

  castle(c) {
    ground(c, '#0e0a13');
    const stone = new THREE.MeshStandardMaterial({ color: '#221c2d', roughness: 0.95 });
    const roof = new THREE.MeshStandardMaterial({ color: '#160f1f', roughness: 0.8 });
    const castle = new THREE.Group();
    const hill = new THREE.Mesh(new THREE.SphereGeometry(26, 40, 20, 0, Math.PI * 2, 0, Math.PI / 2), new THREE.MeshStandardMaterial({ color: '#100c15', roughness: 1 }));
    hill.scale.set(1.4, 0.45, 0.8);
    castle.add(hill);
    const block = (x: number, y: number, z: number, w: number, h: number, d: number) => { const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), stone); m.position.set(x, y + h / 2, z); castle.add(m); return m; };
    const tower = (x: number, z: number, h: number, r: number) => {
      const t = new THREE.Mesh(new THREE.CylinderGeometry(r, r * 1.08, h, 14), stone);
      t.position.set(x, 11 + h / 2, z);
      const cone = new THREE.Mesh(new THREE.ConeGeometry(r * 1.35, r * 2.6, 14), roof);
      cone.position.set(x, 11 + h + r * 1.3, z);
      castle.add(t, cone);
    };
    block(0, 11, 0, 22, 9, 8);
    block(0, 20, 0, 10, 6, 6);
    tower(-12, 0, 16, 2.4);
    tower(12, 0, 14, 2.2);
    tower(-4, -3, 22, 1.8);
    tower(6, -2, 19, 1.6);
    // Lit windows flicker.
    const winMat = new THREE.MeshBasicMaterial({ color: new THREE.Color('#ffb347').multiplyScalar(2), toneMapped: false });
    const windows: THREE.Mesh[] = [];
    for (let i = 0; i < 26; i++) {
      const wdw = new THREE.Mesh(new THREE.PlaneGeometry(0.7, 1.1), winMat.clone());
      wdw.position.set(R(-10, 10), R(12.5, 25), 4.05 + (Math.random() < 0.3 ? -1 : 0));
      if (wdw.position.y > 19.5 && Math.abs(wdw.position.x) > 5) wdw.position.y = R(12.5, 19);
      castle.add(wdw);
      windows.push(wdw);
    }
    castle.position.set(0, -4, -70);
    c.scene.add(castle);
    moon(c, new THREE.Vector3(22, 34, -110), 8);
    const moonLight = new THREE.DirectionalLight('#9c8cff', 0.9);
    moonLight.position.set(20, 30, -30);
    const amb = new THREE.HemisphereLight('#3a2c66', '#050308', 0.45);
    const flash = new THREE.DirectionalLight('#e8e0ff', 0);
    flash.position.set(-10, 40, -20);
    c.scene.add(moonLight, amb, flash);
    const mat = stoneMat();
    for (let i = 0; i < 10; i++) { const t = tombstone(mat); t.position.set(R(-14, 14), 0, R(-18, -2)); c.scene.add(t); }
    forestInstances(c, [new THREE.Vector3(-16, 0, -6), new THREE.Vector3(18, 0, -12)], [12, 10]);
    const bats = batFlock(c, Math.round(6 + 10 * c.density), new THREE.Vector3(0, 30, -70), 16);
    const w = wisps(c, 20 + 60 * c.density, { x: 18, y: [0.5, 5], z: [-20, 4] }, [NOCTURNE.pumpkin, c.pal.accent2]);
    const fogU = fogLayers(c, 0.3, '#b3a3e6');
    let bolt: THREE.Mesh | null = null;
    let nextStrike = 4;
    let strikeAt = -10;
    return {
      cam: new THREE.Vector3(0, 3, 12), look: new THREE.Vector3(0, 10, -50), fog: 0.018, exposure: 1.05,
      lights: [{ l: moonLight, base: 0.9 }, { l: amb, base: 0.45 }],
      update(t) {
        bats(t); w(t); fogU(t);
        windows.forEach((m, i) => { (m.material as THREE.MeshBasicMaterial).color.setScalar(0).add(new THREE.Color('#ffb347').multiplyScalar(1.4 + Math.sin(t * (3 + (i % 5)) + i) * 0.35 + (Math.sin(t * 0.3 + i * 7) > 0.92 ? -1.2 : 0))); });
        if (t > nextStrike) {
          strikeAt = t;
          nextStrike = t + R(6, 11);
          if (bolt) { c.scene.remove(bolt); bolt.geometry.dispose(); (bolt.material as THREE.Material).dispose(); }
          bolt = lightning(new THREE.Vector3(R(-30, 30), 48, R(-90, -60)), 40);
          c.scene.add(bolt);
        }
        const k = t - strikeAt;
        const f = k >= 0 && k < 0.6 ? (1 - k / 0.6) ** 2 * (Math.sin(k * 50) > -0.2 ? 1 : 0.3) : 0;
        flash.intensity = f * 3;
        if (bolt) (bolt.material as THREE.MeshBasicMaterial).opacity = f;
      },
    };
  },

  portal(c) {
    ground(c, '#100b16');
    const p = portal(6);
    p.group.position.set(0, 6.5, -14);
    c.scene.add(p.group);
    const runes = new THREE.Mesh(new THREE.TorusGeometry(9, 0.08, 8, 120), new THREE.MeshBasicMaterial({ color: new THREE.Color(c.pal.accent).multiplyScalar(2), toneMapped: false }));
    runes.rotation.x = -Math.PI / 2;
    runes.position.set(0, 0.15, -14);
    c.scene.add(runes);
    const rocks: THREE.Mesh[] = [];
    const rockMat = new THREE.MeshStandardMaterial({ color: '#2a2236', roughness: 0.9, flatShading: true });
    for (let i = 0; i < 14; i++) {
      const r = new THREE.Mesh(new THREE.DodecahedronGeometry(R(0.3, 1.1), 0), rockMat);
      r.userData = { a: R(0, 6.3), r: R(8, 13), h: R(1, 11), s: R(0.08, 0.2) };
      rocks.push(r);
      c.scene.add(r);
    }
    // Particles spiralling into the portal.
    const n = Math.round(300 + 1200 * c.density);
    const pos = new Float32Array(n * 3);
    const st = Array.from({ length: n }, () => ({ a: R(0, 6.3), r: R(1, 16), sp: R(0.4, 1.2) }));
    const geo = new THREE.BufferGeometry();
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    const ptsMat = new THREE.PointsMaterial({ size: 0.12, map: c.dot, color: new THREE.Color(c.pal.accent).multiplyScalar(1.6), transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false });
    c.scene.add(new THREE.Points(geo, ptsMat));
    const light = new THREE.PointLight(c.pal.accent2, 60, 40, 2);
    light.position.set(0, 6.5, -12);
    const amb = new THREE.HemisphereLight('#3a2c66', '#050308', 0.4);
    c.scene.add(light, amb);
    const fogU = fogLayers(c, 0.25, '#c4b5fd');
    return {
      cam: new THREE.Vector3(0, 4, 9), look: new THREE.Vector3(0, 6, -14), fog: 0.03, exposure: 1.0,
      lights: [{ l: light, base: 60 }, { l: amb, base: 0.4 }],
      update(t, dt) {
        p.update(t, 1);
        p.group.rotation.z = t * 0.05;
        runes.rotation.z = -t * 0.1;
        rocks.forEach((r) => { const u = r.userData as { a: number; r: number; h: number; s: number }; const a = u.a + t * u.s; r.position.set(Math.cos(a) * u.r, u.h + Math.sin(t + u.a) * 0.5, -14 + Math.sin(a) * u.r * 0.6); r.rotation.set(t * u.s * 3, t * u.s * 2, 0); });
        st.forEach((s, i) => {
          s.r -= dt * s.sp * 1.6;
          s.a += dt * s.sp * (2.5 / Math.max(0.6, s.r));
          if (s.r < 0.4) { s.r = R(10, 16); s.a = R(0, 6.3); }
          pos.set([Math.cos(s.a) * s.r, 6.5 + Math.sin(s.a) * s.r, -14 + s.r * 0.15], i * 3);
        });
        geo.attributes.position!.needsUpdate = true;
        fogU(t);
        light.intensity = 60 * (0.85 + Math.sin(t * 2) * 0.15) * (light.userData.k ?? 1);
      },
    };
  },

  tunnel(c) {
    const n = Math.round(800 + 3200 * c.density);
    const pos = new Float32Array(n * 3);
    const cl = new Float32Array(n * 3);
    const a1 = new THREE.Color(c.pal.accent), a2 = new THREE.Color(c.pal.accent2), bone = col(NOCTURNE.bone);
    for (let i = 0; i < n; i++) {
      const a = R(0, Math.PI * 2), r = R(2.5, 14);
      pos.set([Math.cos(a) * r, Math.sin(a) * r, R(-200, 10)], i * 3);
      const k = (Math.random() < 0.45 ? a1 : Math.random() < 0.8 ? a2 : bone).clone().multiplyScalar(1.5);
      cl.set([k.r, k.g, k.b], i * 3);
    }
    const geo = new THREE.BufferGeometry();
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    geo.setAttribute('color', new THREE.BufferAttribute(cl, 3));
    c.scene.add(new THREE.Points(geo, new THREE.PointsMaterial({ size: 0.16, map: c.dot, vertexColors: true, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, toneMapped: false })));
    const rings: THREE.Mesh[] = [];
    for (let i = 0; i < 12; i++) {
      const m = new THREE.Mesh(new THREE.TorusGeometry(R(6, 9), 0.05, 6, 90), new THREE.MeshBasicMaterial({ color: (i % 2 ? a1 : a2).clone().multiplyScalar(1.4), toneMapped: false, transparent: true, opacity: 0.7 }));
      m.position.z = -i * 18;
      rings.push(m);
      c.scene.add(m);
    }
    const amb = new THREE.AmbientLight('#ffffff', 0.4);
    c.scene.add(amb);
    return {
      cam: new THREE.Vector3(0, 0, 8), look: new THREE.Vector3(0, 0, -50), fog: 0.012, exposure: 1.0,
      lights: [{ l: amb, base: 0.4 }],
      update(t, dt) {
        for (let i = 0; i < n; i++) {
          let z = pos[i * 3 + 2]! + dt * 14;
          if (z > 10) z -= 210;
          pos[i * 3 + 2] = z;
        }
        geo.attributes.position!.needsUpdate = true;
        rings.forEach((m, i) => { m.position.z += dt * 14; if (m.position.z > 10) m.position.z -= 216; m.rotation.z = t * 0.2 + i; });
      },
    };
  },

  scifi(c) {
    const uniforms = { uTime: { value: 0 }, uA: { value: new THREE.Color(c.pal.accent2) }, uB: { value: new THREE.Color(c.pal.accent) } };
    const floor = new THREE.Mesh(new THREE.PlaneGeometry(400, 400), new THREE.ShaderMaterial({
      uniforms, transparent: true, toneMapped: false,
      vertexShader: 'varying vec3 vW; void main(){ vec4 w = modelMatrix * vec4(position,1.0); vW = w.xyz; gl_Position = projectionMatrix * viewMatrix * w; }',
      fragmentShader: `uniform float uTime; uniform vec3 uA; uniform vec3 uB; varying vec3 vW;
        void main(){
          vec2 g = vec2(vW.x, vW.z + uTime * 6.0) / 4.0;
          vec2 d = abs(fract(g - 0.5) - 0.5) / fwidth(g);
          float line = 1.0 - min(min(d.x, d.y), 1.0);
          float fade = smoothstep(-180.0, -20.0, vW.z) * smoothstep(30.0, 0.0, vW.z);
          vec3 c = mix(uA, uB, smoothstep(-120.0, 0.0, vW.z));
          gl_FragColor = vec4(c * 1.8 * line, line * fade * 0.9);
        }`,
    }));
    floor.rotation.x = -Math.PI / 2;
    c.scene.add(floor);
    const sun = new THREE.Mesh(new THREE.CircleGeometry(30, 96), new THREE.ShaderMaterial({
      uniforms, transparent: true, toneMapped: false, fog: false,
      vertexShader: 'varying vec2 vUv; void main(){ vUv = uv; gl_Position = projectionMatrix * modelViewMatrix * vec4(position,1.0); }',
      fragmentShader: `uniform float uTime; uniform vec3 uA; uniform vec3 uB; varying vec2 vUv;
        void main(){
          vec3 c = mix(uA, uB, vUv.y) * 1.6;
          float stripe = step(0.5, fract(vUv.y * 14.0 - uTime * 0.4)) + step(0.55, vUv.y);
          gl_FragColor = vec4(c, clamp(stripe, 0.0, 1.0));
        }`,
    }));
    sun.position.set(0, 18, -190);
    c.scene.add(sun);
    const mtn = new THREE.PlaneGeometry(400, 60, 80, 12);
    const mp = mtn.attributes.position as THREE.BufferAttribute;
    for (let i = 0; i < mp.count; i++) { const x = mp.getX(i), y = mp.getY(i); mp.setZ(i, y > -25 ? Math.abs(Math.sin(x * 0.05) * 18 + Math.sin(x * 0.13) * 8) * ((y + 30) / 60) : 0); }
    const mountains = new THREE.Mesh(mtn, new THREE.MeshBasicMaterial({ color: new THREE.Color(c.pal.accent2).multiplyScalar(1.3), wireframe: true, transparent: true, opacity: 0.35, toneMapped: false }));
    mountains.rotation.x = -Math.PI / 2;
    mountains.position.set(0, 0.1, -150);
    c.scene.add(mountains);
    const cubes: THREE.Mesh[] = [];
    for (let i = 0; i < 12; i++) {
      const m = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), new THREE.MeshStandardMaterial({ color: '#120a1f', emissive: i % 2 ? c.pal.accent : c.pal.accent2, emissiveIntensity: 0.7, roughness: 0.3, metalness: 0.6 }));
      m.position.set(R(-18, 18), R(2, 9), R(-60, -8));
      m.scale.setScalar(R(0.6, 1.8));
      cubes.push(m);
      c.scene.add(m);
    }
    const stars = wisps(c, 80 + 200 * c.density, { x: 120, y: [20, 70], z: [-200, -80] }, [NOCTURNE.bone, c.pal.accent2], 0.5);
    const amb = new THREE.AmbientLight('#ffffff', 0.5);
    c.scene.add(amb);
    return {
      cam: new THREE.Vector3(0, 3.2, 10), look: new THREE.Vector3(0, 6, -60), fog: 0.008, exposure: 1.0,
      lights: [{ l: amb, base: 0.5 }],
      update(t) { uniforms.uTime.value = t; stars(t); cubes.forEach((m, i) => { m.rotation.set(t * 0.3 + i, t * 0.4 + i, 0); m.position.y += Math.sin(t + i) * 0.004; }); },
    };
  },

  rooms(c) {
    const frames: THREE.Mesh[] = [];
    const a1 = new THREE.Color(c.pal.accent), a2 = new THREE.Color(c.pal.accent2);
    for (let i = 0; i < 16; i++) {
      const m = new THREE.Mesh(new THREE.TorusGeometry(7, 0.12, 4, 4), new THREE.MeshBasicMaterial({ color: (i % 3 === 0 ? a1 : a2).clone().multiplyScalar(1.5), toneMapped: false }));
      m.rotation.z = Math.PI / 4;
      m.position.z = -i * 9;
      frames.push(m);
      c.scene.add(m);
    }
    const floor = new THREE.Mesh(new THREE.PlaneGeometry(14, 400), new THREE.MeshStandardMaterial({ color: '#0d0a14', roughness: 0.25, metalness: 0.6 }));
    floor.rotation.x = -Math.PI / 2;
    floor.position.set(0, -4.9, -100);
    c.scene.add(floor);
    const blocks: THREE.Mesh[] = [];
    for (let i = 0; i < 18; i++) {
      const m = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), new THREE.MeshStandardMaterial({ color: '#1a1326', roughness: 0.4, metalness: 0.3 }));
      m.position.set(R(-4, 4), R(-3, 3), R(-120, -6));
      m.scale.set(R(0.3, 1.2), R(0.3, 1.2), R(0.3, 1.2));
      blocks.push(m);
      c.scene.add(m);
    }
    const mover = new THREE.PointLight(c.pal.accent, 40, 30, 2);
    const amb = new THREE.AmbientLight('#2a1d40', 0.6);
    c.scene.add(mover, amb);
    const w = wisps(c, 40 + 100 * c.density, { x: 5, y: [-4, 4], z: [-120, 0] }, [c.pal.accent, c.pal.accent2]);
    return {
      cam: new THREE.Vector3(0, 0, 8), look: new THREE.Vector3(0, 0, -40), fog: 0.03, exposure: 1.0,
      lights: [{ l: mover, base: 40 }, { l: amb, base: 0.6 }],
      update(t, dt) {
        frames.forEach((m) => { m.position.z += dt * 3; if (m.position.z > 8) m.position.z -= 144; });
        blocks.forEach((m, i) => { m.position.z += dt * 3; if (m.position.z > 8) m.position.z -= 128; m.rotation.set(t * 0.2 + i, t * 0.15 + i, 0); });
        mover.position.set(Math.sin(t * 0.7) * 4, Math.cos(t * 0.5) * 3, -10 + Math.sin(t * 0.3) * 8);
        w(t);
      },
    };
  },
};

// ---- renderer ---------------------------------------------------------------------------

export interface Bg3DConfig { id: Scene3DId; quality: number; fx: DesignFx; still: boolean; pal: Palette3D }

function disposeScene(scene: THREE.Scene): void {
  scene.traverse((o) => {
    const m = o as THREE.Mesh;
    m.geometry?.dispose();
    const mats = Array.isArray(m.material) ? m.material : m.material ? [m.material] : [];
    mats.forEach((x) => x.dispose());
  });
  scene.clear();
}

/** low < 0.34 <= mid < 0.7 <= high < 0.9 <= ultra (4K: full device resolution, MSAA, depth of field). */
export type Tier = 'low' | 'mid' | 'high' | 'ultra';
export const tierOf = (q: number): Tier => (q < 0.34 ? 'low' : q < 0.7 ? 'mid' : q < 0.9 ? 'high' : 'ultra');
const TIER_DPR: Record<Tier, number> = { low: 0.75, mid: 1, high: 1.5, ultra: 2 };

export class Bg3D {
  private renderer: THREE.WebGLRenderer;
  private composer!: EffectComposer;
  private bloom!: UnrealBloomPass;
  private bokeh: BokehPass | null = null;
  private tier: Tier | null = null;
  private scene = new THREE.Scene();
  private camera = new THREE.PerspectiveCamera(55, 1, 0.1, 500);
  private built: Built | null = null;
  private cfg: Bg3DConfig | null = null;
  private key = '';
  private raf = 0;
  private t = 0;
  private last = 0;
  private lowPower = false;
  private mouse = { x: 0, y: 0, sx: 0, sy: 0 };
  private dot = dotTexture();
  /** Adaptive resolution: current scale of the tier's pixel ratio and frame time statistics. */
  private scale = 1;
  private frameMs: number[] = [];
  private lastAdapt = 0;
  readonly canvas: HTMLCanvasElement;
  lost = false;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.renderer = new THREE.WebGLRenderer({ canvas, antialias: false, powerPreference: 'high-performance', stencil: false });
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.outputColorSpace = THREE.SRGBColorSpace;
    canvas.addEventListener('webglcontextlost', (e) => { e.preventDefault(); this.fail(); });
    addEventListener('mousemove', (e) => { this.mouse.x = e.clientX / innerWidth - 0.5; this.mouse.y = e.clientY / innerHeight - 0.5; });
  }

  /** Any GPU problem: stop for good and let the owner fall back to 2D. */
  private fail(): void {
    if (this.lost) return;
    this.lost = true;
    this.stop();
    this.canvas.dispatchEvent(new Event('bg3d-lost'));
  }

  /** Post-processing chain for a quality tier (MSAA + depth of field on ultra). */
  private buildComposer(tier: Tier): void {
    this.composer?.dispose();
    const size = this.renderer.getDrawingBufferSize(new THREE.Vector2());
    const target = new THREE.WebGLRenderTarget(Math.max(1, size.x), Math.max(1, size.y), {
      type: THREE.HalfFloatType,
      samples: tier === 'ultra' ? 4 : tier === 'high' ? 2 : 0,
    });
    this.composer = new EffectComposer(this.renderer, target);
    this.composer.addPass(new RenderPass(this.scene, this.camera));
    this.bokeh = null;
    if (tier === 'ultra') {
      this.bokeh = new BokehPass(this.scene, this.camera, { focus: 14, aperture: 0.00004, maxblur: 0.006 });
      this.composer.addPass(this.bokeh);
    }
    this.bloom = new UnrealBloomPass(new THREE.Vector2(256, 256), tier === 'ultra' ? 0.8 : 0.7, 0.55, 0.2);
    this.composer.addPass(this.bloom);
    this.composer.addPass(new OutputPass());
    this.tier = tier;
  }

  configure(cfg: Bg3DConfig): void {
    if (this.lost) return;
    const tier = tierOf(cfg.quality);
    const key = `${cfg.id}|${Math.round(cfg.fx.particles * 4)}|${tier}|${cfg.pal.accent}|${cfg.pal.accent2}`;
    this.cfg = cfg;
    try {
      if (tier !== this.tier) { this.scale = 1; this.buildComposer(tier); }
      if (key !== this.key) {
        this.key = key;
        disposeScene(this.scene);
        this.scene.background = new THREE.Color(cfg.pal.bg).lerp(new THREE.Color(NOCTURNE.void), 0.5);
        // Ultra gets more detail (density is scaled up, never above what the slider allows x1.5).
        const density = tier === 'ultra' ? Math.min(1.5, cfg.fx.particles * 1.5) : cfg.fx.particles;
        this.built = BUILD[cfg.id]({ scene: this.scene, camera: this.camera, density, q: tier === 'ultra' ? 1 : cfg.quality, pal: cfg.pal, dot: this.dot });
        this.t = 8;
        this.renderer.compile(this.scene, this.camera);
      }
      this.applyLive();
      this.resize();
    } catch (e) {
      console.warn('3D background failed, using 2D', e);
      this.fail();
    }
  }

  /** Settings that never need a rebuild. */
  private applyLive(): void {
    const c = this.cfg!, b = this.built!;
    this.scene.fog = c.fx.fog ? new THREE.FogExp2(this.scene.background as THREE.Color, b.fog) : null;
    this.renderer.toneMappingExposure = b.exposure * (0.75 + c.fx.light * 0.25);
    b.lights.forEach(({ l, base }) => { l.intensity = base * c.fx.light; l.userData.k = c.fx.light; });
    this.bloom.enabled = c.fx.glow && c.quality >= 0.3;
    if (this.bokeh) {
      this.bokeh.enabled = c.fx.fog;
      (this.bokeh.uniforms as Record<string, { value: number }>).focus!.value = b.cam.distanceTo(b.look) * 0.55;
    }
  }

  resize(): void {
    if (!this.cfg || this.lost) return;
    const want = Math.min(devicePixelRatio || 1, TIER_DPR[this.tier ?? 'mid']) * this.scale;
    const dpr = Math.max(0.5, want);
    this.renderer.setPixelRatio(dpr);
    this.renderer.setSize(innerWidth, innerHeight, false);
    this.composer.setPixelRatio(dpr);
    this.composer.setSize(innerWidth, innerHeight);
    this.camera.aspect = innerWidth / Math.max(1, innerHeight);
    this.camera.updateProjectionMatrix();
    if (this.cfg.still) this.render(0);
  }

  /** Current internal resolution (for the quality label). */
  resolution(): { w: number; h: number; tier: Tier } {
    const v = this.renderer.getDrawingBufferSize(new THREE.Vector2());
    return { w: v.x, h: v.y, tier: this.tier ?? 'mid' };
  }

  setLowPower(on: boolean): void {
    this.lowPower = on;
  }

  /** Keeps the background smooth: lowers resolution when frames get slow, raises it again when there is headroom. */
  private adapt(ms: number, now: number): void {
    this.frameMs.push(ms);
    if (this.frameMs.length < 90 || now - this.lastAdapt < 2000) return;
    const sorted = [...this.frameMs].sort((a, b) => a - b);
    const p90 = sorted[Math.floor(sorted.length * 0.9)]!;
    this.frameMs = [];
    this.lastAdapt = now;
    if (p90 > 24 && this.scale > 0.6) { this.scale = Math.max(0.6, this.scale * 0.85); this.resize(); }
    else if (p90 < 13 && this.scale < 1) { this.scale = Math.min(1, this.scale * 1.15); this.resize(); }
  }

  private render(dt: number): void {
    const c = this.cfg!, b = this.built!;
    b.update(this.t, dt);
    const m = this.mouse;
    const k = c.fx.parallax && !c.still ? 1 : 0;
    m.sx += (m.x * k - m.sx) * Math.min(1, dt * 2 + (c.still ? 1 : 0));
    m.sy += (m.y * k - m.sy) * Math.min(1, dt * 2 + (c.still ? 1 : 0));
    // Slow breathing dolly + mouse parallax: the scene always has a little depth motion.
    const dolly = Math.sin(this.t * 0.05) * 0.8;
    this.camera.position.copy(b.cam).add(new THREE.Vector3(m.sx * 2.2 + Math.sin(this.t * 0.07) * 0.6, -m.sy * 1.1 + Math.sin(this.t * 0.11) * 0.25, dolly));
    this.camera.lookAt(b.look);
    if (this.bloom.enabled || this.bokeh?.enabled || this.tier === 'ultra' || this.tier === 'high') this.composer.render();
    else this.renderer.render(this.scene, this.camera);
  }

  private frame = (now: number): void => {
    this.raf = requestAnimationFrame(this.frame);
    const minGap = this.lowPower ? 1000 / 20 : 1000 / 62;
    if (now - this.last < minGap) return;
    const raw = now - (this.last || now);
    const dt = Math.min(0.1, raw / 1000) * (this.cfg?.fx.speed ?? 1);
    this.last = now;
    this.t += dt;
    const t0 = performance.now();
    try {
      this.render(dt);
    } catch (e) {
      console.warn('3D background frame failed', e);
      this.fail();
      return;
    }
    if (!this.lowPower) this.adapt(Math.max(raw, performance.now() - t0), now);
  };

  start(): void {
    this.stop();
    if (!this.cfg || this.lost || document.hidden) return;
    if (this.cfg.still) { this.render(0); return; }
    this.raf = requestAnimationFrame(this.frame);
  }

  stop(): void {
    cancelAnimationFrame(this.raf);
    this.raf = 0;
    this.last = 0;
  }
}

let thumbRenderer: THREE.WebGLRenderer | null = null;
const thumbs = new Map<string, string>();

/** Small still image of a 3D scene for the background picker (cached). */
export function thumbnail(id: Scene3DId, pal: Palette3D): string {
  const key = `${id}|${pal.accent}|${pal.accent2}`;
  const hit = thumbs.get(key);
  if (hit) return hit;
  thumbRenderer ??= new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: true, powerPreference: 'low-power' });
  thumbRenderer.setSize(320, 200, false);
  thumbRenderer.toneMapping = THREE.ACESFilmicToneMapping;
  thumbRenderer.outputColorSpace = THREE.SRGBColorSpace;
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(pal.bg).lerp(new THREE.Color(NOCTURNE.void), 0.5);
  const camera = new THREE.PerspectiveCamera(55, 1.6, 0.1, 500);
  const dot = dotTexture();
  const b = BUILD[id]({ scene, camera, density: 0.4, q: 0.3, pal, dot });
  scene.fog = new THREE.FogExp2(scene.background as THREE.Color, b.fog);
  thumbRenderer.toneMappingExposure = b.exposure;
  b.update(8, 0.016);
  camera.position.copy(b.cam);
  camera.lookAt(b.look);
  thumbRenderer.render(scene, camera);
  const url = thumbRenderer.domElement.toDataURL('image/jpeg', 0.82);
  disposeScene(scene);
  dot.dispose();
  thumbs.set(key, url);
  return url;
}
