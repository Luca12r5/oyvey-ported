// The visual-VR lobby world: a calm, sunny Minecraft-style landscape made of
// 1 m blocks (grass hills, a lake, birch trees, slow clouds). Textures are
// painted here at 16×16 like block textures; nothing is loaded. Instanced, so
// the whole valley is a handful of draw calls.

import * as THREE from 'three';

const S = 16;

function rng(seed: number): () => number {
  let s = seed >>> 0;
  return () => { s = (s + 0x6d2b79f5) >>> 0; let t = s; t = Math.imul(t ^ (t >>> 15), t | 1); t ^= t + Math.imul(t ^ (t >>> 7), t | 61); return ((t ^ (t >>> 14)) >>> 0) / 4294967296; };
}

function tex(paint: (put: (x: number, y: number, c: string) => void, r: () => number) => void, seed: number): THREE.CanvasTexture {
  const c = document.createElement('canvas');
  c.width = c.height = S;
  const g = c.getContext('2d')!;
  const r = rng(seed);
  paint((x, y, col) => { g.fillStyle = col; g.fillRect(x, y, 1, 1); }, r);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  t.magFilter = THREE.NearestFilter;
  t.minFilter = THREE.NearestMipmapLinearFilter;
  return t;
}

const shade = (hex: string, k: number) => { const c = new THREE.Color(hex); c.multiplyScalar(k); return `#${c.getHexString()}`; };
const noisy = (base: string, amt: number) => (put: (x: number, y: number, c: string) => void, r: () => number) => {
  for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) put(x, y, shade(base, 1 - amt / 2 + r() * amt));
};

function textures() {
  const grassTop = tex(noisy('#6db04a', 0.28), 1);
  const dirt = tex((put, r) => { for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) put(x, y, r() < 0.08 ? '#6b4a2f' : shade('#8a6142', 0.85 + r() * 0.3)); }, 2);
  const grassSide = tex((put, r) => {
    for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) {
      const edge = 3 + Math.floor(r() * 2.2);
      put(x, y, y < edge ? shade('#6db04a', 0.8 + r() * 0.3) : r() < 0.08 ? '#6b4a2f' : shade('#8a6142', 0.85 + r() * 0.3));
    }
  }, 3);
  const sand = tex(noisy('#e0d39a', 0.14), 4);
  const stone = tex(noisy('#8c8c8c', 0.3), 5);
  const birch = tex((put, r) => {
    for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) put(x, y, shade('#e8e4d8', 0.9 + r() * 0.12));
    for (let i = 0; i < 7; i++) { const y = Math.floor(r() * S), x = Math.floor(r() * 12), w = 2 + Math.floor(r() * 3); for (let k = 0; k < w; k++) put(x + k, y, '#2b2b2b'); }
  }, 6);
  const birchTop = tex((put, r) => { for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) { const d = Math.max(Math.abs(x - 7.5), Math.abs(y - 7.5)); put(x, y, d > 6.5 ? '#e8e4d8' : shade('#c9b27a', 0.85 + (Math.floor(d) % 2) * 0.12 + r() * 0.05)); } }, 7);
  const leaves = tex((put, r) => {
    for (let y = 0; y < S; y++) for (let x = 0; x < S; x++) put(x, y, r() < 0.16 ? 'rgba(0,0,0,0)' : shade('#80a755', 0.65 + r() * 0.45));
  }, 8);
  return { grassTop, dirt, grassSide, sand, stone, birch, birchTop, leaves };
}

/** Smooth value noise in [0,1]. */
function noise2(seed: number) {
  const r = rng(seed);
  const N = 256, perm = new Uint8Array(N * 2), val = new Float32Array(N);
  for (let i = 0; i < N; i++) { perm[i] = i; val[i] = r(); }
  for (let i = N - 1; i > 0; i--) { const j = Math.floor(r() * (i + 1)); [perm[i], perm[j]] = [perm[j]!, perm[i]!]; }
  for (let i = 0; i < N; i++) perm[N + i] = perm[i]!;
  const v = (x: number, y: number) => val[perm[(x & 255) + perm[y & 255]!]!]!;
  const sm = (t: number) => t * t * (3 - 2 * t);
  return (x: number, y: number) => {
    const xi = Math.floor(x), yi = Math.floor(y), xf = sm(x - xi), yf = sm(y - yi);
    const a = v(xi, yi), b = v(xi + 1, yi), c = v(xi, yi + 1), d = v(xi + 1, yi + 1);
    return a + (b - a) * xf + (c - a) * yf + (a - b - c + d) * xf * yf;
  };
}

export interface World {
  /** Top of the ground (or water surface) at a world position, in metres. */
  heightAt(x: number, z: number): number;
  /** Advance clouds and water shimmer. */
  update(t: number): void;
  sun: THREE.DirectionalLight;
  dispose(): void;
}

export const WATER = -2;
const R = 44;

export function buildWorld(scene: THREE.Scene, shadows: boolean): World {
  const T = textures();
  const n1 = noise2(11), n2 = noise2(23);
  const H = new Map<string, number>();
  const hAt = (x: number, z: number): number => {
    const k = `${x},${z}`;
    let h = H.get(k);
    if (h !== undefined) return h;
    const d = Math.hypot(x, z);
    // Flat meadow where you stand, hills further out, a lake to the front-left.
    let raw = (n1(x / 14, z / 14) - 0.45) * 9 + (n2(x / 5, z / 5) - 0.5) * 2.2;
    raw += Math.max(0, d - 18) * 0.18;
    const lake = Math.hypot(x + 14, z + 16);
    if (lake < 11) raw -= (11 - lake) * 0.75;
    const flat = Math.max(0, Math.min(1, (d - 5) / 8));
    h = Math.round(raw * flat);
    H.set(k, h);
    return h;
  };

  type Inst = { m: THREE.Matrix4[] };
  const grass: Inst = { m: [] }, dirt: Inst = { m: [] }, sand: Inst = { m: [] }, stone: Inst = { m: [] }, logs: Inst = { m: [] }, leaves: Inst = { m: [] };
  const at = (x: number, y: number, z: number) => new THREE.Matrix4().makeTranslation(x, y - 0.5, z);
  const r = rng(77);
  for (let x = -R; x <= R; x++) for (let z = -R; z <= R; z++) {
    if (x * x + z * z > R * R) continue;
    const h = hAt(x, z);
    const low = Math.min(h, hAt(x + 1, z), hAt(x - 1, z), hAt(x, z + 1), hAt(x, z - 1));
    const beach = h <= WATER + 1;
    (beach ? sand : h > 9 ? stone : grass).m.push(at(x, h, z));
    for (let y = h - 1; y >= Math.min(low, h - 1); y--) (beach ? sand : h > 9 ? stone : dirt).m.push(at(x, y, z));
  }
  // Birches on the meadow edge and hills (not in the lake, not where you stand).
  const trees: [number, number][] = [];
  for (let i = 0; i < 400 && trees.length < 34; i++) {
    const a = r() * Math.PI * 2, d = 9 + r() * 30;
    const x = Math.round(Math.cos(a) * d), z = Math.round(Math.sin(a) * d);
    const h = hAt(x, z);
    if (h <= WATER + 1 || h > 8 || trees.some(([tx, tz]) => Math.abs(tx - x) + Math.abs(tz - z) < 5)) continue;
    // keep the view to the window free
    if (z < -3 && Math.abs(x) < 7) continue;
    trees.push([x, z]);
    const th = 5 + Math.floor(r() * 3);
    for (let y = 1; y <= th; y++) logs.m.push(at(x, h + y, z));
    for (let dy = th - 2; dy <= th + 1; dy++) {
      const rad = dy >= th ? 1 : 2;
      for (let dx = -rad; dx <= rad; dx++) for (let dz = -rad; dz <= rad; dz++) {
        if ((dx === 0 && dz === 0 && dy <= th)) continue;
        if (Math.abs(dx) === rad && Math.abs(dz) === rad && (dy === th + 1 || r() < 0.5)) continue;
        leaves.m.push(at(x + dx, h + dy, z + dz));
      }
    }
  }

  const box = new THREE.BoxGeometry(1, 1, 1);
  const mats: THREE.Material[] = [];
  const mk = (o: THREE.MeshStandardMaterialParameters) => { const m = new THREE.MeshStandardMaterial({ roughness: 0.95, ...o }); mats.push(m); return m; };
  const side = (t: THREE.Texture, top: THREE.Texture, bottom: THREE.Texture) => [mk({ map: t }), mk({ map: t }), mk({ map: top }), mk({ map: bottom }), mk({ map: t }), mk({ map: t })];
  const meshes: THREE.InstancedMesh[] = [];
  const add = (inst: Inst, mat: THREE.Material | THREE.Material[], cast = false) => {
    if (!inst.m.length) return;
    const im = new THREE.InstancedMesh(box, mat, inst.m.length);
    inst.m.forEach((m, i) => im.setMatrixAt(i, m));
    im.receiveShadow = true;
    im.castShadow = cast && shadows;
    scene.add(im);
    meshes.push(im);
  };
  add(grass, side(T.grassSide, T.grassTop, T.dirt));
  add(dirt, mk({ map: T.dirt }));
  add(sand, mk({ map: T.sand }));
  add(stone, mk({ map: T.stone }));
  add(logs, side(T.birch, T.birchTop, T.birchTop), true);
  add(leaves, mk({ map: T.leaves, alphaTest: 0.5, side: THREE.DoubleSide, color: '#9fd06a' }), true);

  // Water: one plane at the lake surface, gently shimmering.
  const water = new THREE.Mesh(new THREE.PlaneGeometry(2 * R, 2 * R), new THREE.MeshStandardMaterial({ color: '#3f76e4', transparent: true, opacity: 0.78, roughness: 0.15, metalness: 0.1 }));
  water.rotation.x = -Math.PI / 2;
  water.position.y = WATER - 0.12;
  water.receiveShadow = true;
  scene.add(water);

  // Sky, sun, light.
  scene.background = new THREE.Color('#8ec3ff');
  scene.fog = new THREE.Fog('#b9dbff', 26, 70);
  const sun = new THREE.DirectionalLight('#fff3dc', 2.4);
  sun.position.set(18, 30, 12);
  if (shadows) {
    sun.castShadow = true;
    sun.shadow.mapSize.set(2048, 2048);
    Object.assign(sun.shadow.camera, { left: -24, right: 24, top: 24, bottom: -24, near: 1, far: 90 });
    sun.shadow.bias = -0.0006;
    sun.shadow.normalBias = 0.03;
  }
  const hemi = new THREE.HemisphereLight('#cfe6ff', '#5b7a3a', 1.1);
  scene.add(sun, hemi);
  const sunDisk = new THREE.Mesh(new THREE.PlaneGeometry(7, 7), new THREE.MeshBasicMaterial({ color: '#fff6c8', fog: false, toneMapped: false }));
  sunDisk.position.set(-40, 48, -70);
  sunDisk.lookAt(0, 1.6, 0);
  scene.add(sunDisk);

  // Flat Minecraft clouds that drift (no rotation).
  const cloudMat = new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0.9, fog: false });
  mats.push(cloudMat);
  const clouds = new THREE.Group();
  const cr = rng(5);
  for (let i = 0; i < 26; i++) {
    const c = new THREE.Group();
    const parts = 2 + Math.floor(cr() * 4);
    for (let k = 0; k < parts; k++) {
      const m = new THREE.Mesh(box, cloudMat);
      m.scale.set(6 + cr() * 10, 1.2, 4 + cr() * 8);
      m.position.set(cr() * 10 - 5, 0, cr() * 8 - 4);
      c.add(m);
    }
    c.position.set(cr() * 160 - 80, 34 + cr() * 6, cr() * 160 - 80);
    clouds.add(c);
  }
  scene.add(clouds);

  return {
    heightAt(x, z) { return Math.max(WATER - 0.12, hAt(Math.round(x), Math.round(z))); },
    update(t) {
      for (const c of clouds.children) { c.position.x += 0.012; if (c.position.x > 90) c.position.x -= 180; }
      (water.material as THREE.MeshStandardMaterial).opacity = 0.76 + Math.sin(t * 0.8) * 0.03;
    },
    sun,
    dispose() {
      meshes.forEach((m) => m.dispose());
      box.dispose();
      mats.forEach((m) => m.dispose());
      Object.values(T).forEach((t) => t.dispose());
      water.geometry.dispose(); (water.material as THREE.Material).dispose();
      sunDisk.geometry.dispose(); (sunDisk.material as THREE.Material).dispose();
    },
  };
}
