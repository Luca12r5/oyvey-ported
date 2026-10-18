// Halloween dressing for the mine-cave intro: carved jack-o'-lanterns along the
// rails, glowing crystal clusters (purple, toxic green, orange) on the walls,
// cobwebs in the timber frames, floating spirit wisps, a bat swarm that flies
// past the cart and colourful spark bursts when the logo lands.
// Everything is real geometry (instanced where repeated) so it reads as 3D.

import * as THREE from 'three';

export interface HalloweenOpts {
  railPoint: (z: number) => THREE.Vector3;
  frameZ: number[];
  dot: THREE.Texture;
  q: number;
  cubeGeo: THREE.BufferGeometry;
  /** Where the logo sits (for the finale bursts). */
  logoAt: THREE.Vector3;
}

const PUMPKIN_FACE = [
  // 7 × 5 carved face on the front of a 7×6×7 pumpkin (1 = hole)
  '.1...1.',
  '111.111',
  '...1...',
  '1.....1',
  '.11111.',
];

export function addHalloween(scene: THREE.Scene, o: HalloweenOpts): (t: number, cam: THREE.Vector3, dt: number) => void {
  const hi = o.q > 0.5;
  const tickers: ((t: number, cam: THREE.Vector3, dt: number) => void)[] = [];
  const m4 = new THREE.Matrix4(), qt = new THREE.Quaternion(), v = new THREE.Vector3(), s = new THREE.Vector3(), eu = new THREE.Euler();

  // ---- jack-o'-lanterns beside the track (voxel shells of 1×1 bricks, face cut out, lit inside)
  const pumpkinZ = o.frameZ.filter((_, i) => i % 3 === 1).slice(0, hi ? 9 : 5);
  const shell: THREE.Vector3[] = [];
  for (let x = -3; x <= 3; x++) for (let y = 0; y < 6; y++) for (let z = -3; z <= 3; z++) {
    const r = Math.hypot(x / 3.4, (y - 2.5) / 3, z / 3.4);
    if (r > 1 || r < 0.72) continue;
    const fy = 4 - y, fx = x + 3;
    if (z === 3 && fy >= 0 && fy < 5 && PUMPKIN_FACE[fy]![fx] === '1') continue;
    shell.push(new THREE.Vector3(x, y, z));
  }
  const pMat = new THREE.MeshStandardMaterial({ color: '#ff7a1a', roughness: 0.5, emissive: '#ff5a00', emissiveIntensity: 0.05 });
  const pumpkins = new THREE.InstancedMesh(o.cubeGeo, pMat, shell.length * pumpkinZ.length);
  const stems = new THREE.InstancedMesh(o.cubeGeo, new THREE.MeshStandardMaterial({ color: '#3f6b1f', roughness: 0.7 }), pumpkinZ.length);
  const cores: THREE.Sprite[] = [];
  const pLights: THREE.PointLight[] = [];
  let pi = 0;
  pumpkinZ.forEach((z, k) => {
    const p = o.railPoint(z);
    const side = k % 2 ? 1 : -1;
    const base = new THREE.Vector3(p.x + side * 1.6, p.y - 0.2, z + 0.4);
    const size = 0.12;
    const yaw = -side * 0.6;
    qt.setFromEuler(eu.set(0, yaw, 0));
    for (const c of shell) {
      v.copy(c).multiplyScalar(size).applyQuaternion(qt).add(base);
      m4.compose(v, qt, s.setScalar(size * 0.98));
      pumpkins.setMatrixAt(pi++, m4);
    }
    m4.compose(v.set(0, 6.4 * size, 0).add(base), qt, s.set(size, size * 1.6, size));
    stems.setMatrixAt(k, m4);
    const core = new THREE.Sprite(new THREE.SpriteMaterial({ map: o.dot, color: new THREE.Color('#ffb347').multiplyScalar(3), blending: THREE.AdditiveBlending, depthWrite: false, transparent: true, toneMapped: false }));
    core.position.copy(base).add(new THREE.Vector3(0, 2.4 * size, 0));
    core.scale.setScalar(0.5);
    scene.add(core);
    cores.push(core);
    if (k < (hi ? 4 : 2)) {
      const l = new THREE.PointLight('#ff8a2a', 0, 7, 1.7);
      l.position.copy(core.position);
      scene.add(l);
      pLights.push(l);
    }
  });
  scene.add(pumpkins, stems);
  tickers.push((t) => {
    const on = THREE.MathUtils.smoothstep(t, 4.2, 5.2);
    pMat.emissiveIntensity = 0.05 + 0.25 * on;
    cores.forEach((c, i) => c.scale.setScalar(on * (0.45 + Math.sin(t * 11 + i) * 0.06 + Math.random() * 0.04)));
    pLights.forEach((l, i) => { l.intensity = on * (14 + Math.sin(t * 9 + i * 2) * 3); });
  });

  // ---- crystal clusters on the walls (emissive, three Halloween colours)
  const CRYST = ['#b05cff', '#62ff7a', '#ff7a1a', '#ff3fa4'];
  const crystGeo = new THREE.OctahedronGeometry(0.22, 0);
  crystGeo.scale(0.6, 2.2, 0.6);
  const crystMats = CRYST.map((c) => new THREE.MeshStandardMaterial({ color: c, emissive: c, emissiveIntensity: 0.9, roughness: 0.15, metalness: 0.1, transparent: true, opacity: 0.92 }));
  const perCluster = 6;
  const clusterZ = o.frameZ.filter((_, i) => i % 2 === 1);
  const crystMeshes = crystMats.map((m) => new THREE.InstancedMesh(crystGeo, m, clusterZ.length * perCluster));
  const counts = [0, 0, 0, 0];
  const crystLights: THREE.PointLight[] = [];
  clusterZ.forEach((z, k) => {
    const ci = k % CRYST.length;
    const p = o.railPoint(z);
    const side = k % 2 ? -1 : 1;
    const base = new THREE.Vector3(p.x + side * 2.25, p.y + 0.9 + (k % 3) * 0.55, z - 0.7);
    for (let j = 0; j < perCluster; j++) {
      const tilt = (j - perCluster / 2) * 0.28;
      qt.setFromEuler(eu.set(Math.random() * 0.5 - 0.25, Math.random() * 3, side * (0.7 + tilt)));
      v.set((Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.5).add(base);
      m4.compose(v, qt, s.setScalar(0.6 + Math.random() * 0.9));
      crystMeshes[ci]!.setMatrixAt(counts[ci]!++, m4);
    }
    if (k < (hi ? 6 : 3)) {
      const l = new THREE.PointLight(CRYST[ci]!, 0, 6, 1.8);
      l.position.copy(base).add(new THREE.Vector3(-side * 0.4, 0, 0));
      scene.add(l);
      crystLights.push(l);
    }
  });
  crystMeshes.forEach((m, i) => { m.count = counts[i]!; scene.add(m); });
  tickers.push((t) => {
    const on = THREE.MathUtils.smoothstep(t, 4.5, 6);
    crystMats.forEach((m, i) => { m.emissiveIntensity = 0.15 + on * (0.9 + Math.sin(t * 2.3 + i) * 0.3); });
    crystLights.forEach((l, i) => { l.intensity = on * (9 + Math.sin(t * 3 + i) * 3); });
  });

  // ---- cobwebs in the corners of the timber frames
  const webPts: number[] = [];
  o.frameZ.filter((_, i) => i % 2 === 0).forEach((z, k) => {
    const p = o.railPoint(z);
    const side = k % 2 ? 1 : -1;
    const cx = p.x + side * 1.6, cy = p.y + 3.0, cz = z;
    const R = 0.75;
    const spokes = 6;
    for (let i = 0; i <= spokes; i++) {
      const a = (i / spokes) * (Math.PI / 2);
      webPts.push(cx, cy, cz, cx - side * Math.cos(a) * R, cy - Math.sin(a) * R, cz);
    }
    for (let ring = 1; ring <= 4; ring++) {
      const rr = (ring / 4) * R;
      for (let i = 0; i < spokes; i++) {
        const a0 = (i / spokes) * (Math.PI / 2), a1 = ((i + 1) / spokes) * (Math.PI / 2);
        const sag = 0.92;
        webPts.push(cx - side * Math.cos(a0) * rr, cy - Math.sin(a0) * rr, cz, cx - side * Math.cos((a0 + a1) / 2) * rr * sag, cy - Math.sin((a0 + a1) / 2) * rr * sag, cz);
        webPts.push(cx - side * Math.cos((a0 + a1) / 2) * rr * sag, cy - Math.sin((a0 + a1) / 2) * rr * sag, cz, cx - side * Math.cos(a1) * rr, cy - Math.sin(a1) * rr, cz);
      }
    }
  });
  const webGeo = new THREE.BufferGeometry();
  webGeo.setAttribute('position', new THREE.Float32BufferAttribute(webPts, 3));
  const webMat = new THREE.LineBasicMaterial({ color: '#d9d4e8', transparent: true, opacity: 0 });
  scene.add(new THREE.LineSegments(webGeo, webMat));
  tickers.push((t) => { webMat.opacity = 0.35 * THREE.MathUtils.smoothstep(t, 3.6, 5); });

  // ---- spirit wisps drifting through the tunnel
  const WISP = hi ? 70 : 35;
  const wispCols = ['#7cff9b', '#b07cff', '#ff9a3a', '#7cd8ff'];
  const wisps = Array.from({ length: WISP }, (_, i) => {
    const z = -Math.random() * 100 + 4;
    const p = o.railPoint(z);
    const sp = new THREE.Sprite(new THREE.SpriteMaterial({ map: o.dot, color: new THREE.Color(wispCols[i % wispCols.length]!).multiplyScalar(2.2), blending: THREE.AdditiveBlending, depthWrite: false, transparent: true, opacity: 0, toneMapped: false }));
    sp.position.set(p.x + (Math.random() - 0.5) * 3.4, p.y + 0.6 + Math.random() * 2.6, z);
    sp.scale.setScalar(0.08 + Math.random() * 0.14);
    scene.add(sp);
    return { sp, ph: Math.random() * 10, base: sp.position.clone() };
  });
  tickers.push((t) => {
    const on = THREE.MathUtils.smoothstep(t, 4.8, 6.5) * (1 - THREE.MathUtils.smoothstep(t, 21.5, 22.5));
    for (const w of wisps) {
      w.sp.position.set(w.base.x + Math.sin(t * 0.7 + w.ph) * 0.4, w.base.y + Math.sin(t * 1.1 + w.ph * 2) * 0.3, w.base.z + Math.cos(t * 0.5 + w.ph) * 0.4);
      (w.sp.material as THREE.SpriteMaterial).opacity = on * (0.55 + Math.sin(t * 3 + w.ph) * 0.35);
    }
  });

  // ---- bat swarm: flaps past the cart twice (tunnel and chamber)
  const batGeo = new THREE.BufferGeometry();
  batGeo.setAttribute('position', new THREE.Float32BufferAttribute([
    0, 0, 0.12, 0, 0.03, -0.1, 0, -0.03, -0.1, // body
    0, 0, 0.05, -0.32, 0.08, -0.02, 0, 0, -0.08, // left wing
    0, 0, 0.05, 0, 0, -0.08, 0.32, 0.08, -0.02, // right wing
  ], 3));
  batGeo.computeVertexNormals();
  const BATS = hi ? 46 : 24;
  const bats = new THREE.InstancedMesh(batGeo, new THREE.MeshStandardMaterial({ color: '#120a14', roughness: 0.9, side: THREE.DoubleSide, emissive: '#2a0a33', emissiveIntensity: 0.4 }), BATS);
  const batData = Array.from({ length: BATS }, () => ({ off: new THREE.Vector3((Math.random() - 0.5) * 2.6, (Math.random() - 0.5) * 1.4, -Math.random() * 6), ph: Math.random() * 6, spd: 0.8 + Math.random() * 0.5 }));
  scene.add(bats);
  const wave = (t: number, a: number, b: number) => (t > a && t < b ? (t - a) / (b - a) : -1);
  tickers.push((t, cam) => {
    const k1 = wave(t, 8.2, 10.6), k2 = wave(t, 13.4, 15.6);
    const k = k1 >= 0 ? k1 : k2;
    batData.forEach((b, i) => {
      if (k < 0) { m4.makeScale(0, 0, 0); bats.setMatrixAt(i, m4); return; }
      const kk = Math.min(1, k * b.spd);
      // Fly from far ahead towards and past the camera, slightly above.
      v.set(b.off.x * (1 - kk * 0.4), 0.9 + b.off.y + Math.sin(t * 6 + b.ph) * 0.15, -14 + kk * 20 + b.off.z).add(cam);
      const flap = Math.sin(t * 28 + b.ph);
      qt.setFromEuler(eu.set(0.1, Math.PI + Math.sin(b.ph) * 0.3, flap * 0.15));
      m4.compose(v, qt, s.set(1, 1 + flap * 0.6, 1).multiplyScalar(1.2));
      bats.setMatrixAt(i, m4);
    });
    bats.instanceMatrix.needsUpdate = true;
  });

  // ---- finale: colourful spark bursts around the logo
  const BURST = hi ? 260 : 130;
  const bPos = new Float32Array(BURST * 3), bCol = new Float32Array(BURST * 3);
  const bVel: THREE.Vector3[] = [];
  const palette = ['#ff7a1a', '#b05cff', '#62ff7a', '#ffd23a', '#ff3fa4'].map((c) => new THREE.Color(c));
  for (let i = 0; i < BURST; i++) {
    const c = palette[i % palette.length]!;
    bCol.set([c.r * 3, c.g * 3, c.b * 3], i * 3);
    const dir = new THREE.Vector3().randomDirection();
    bVel.push(dir.multiplyScalar(3 + Math.random() * 5));
  }
  const bGeo = new THREE.BufferGeometry();
  bGeo.setAttribute('position', new THREE.BufferAttribute(bPos, 3));
  bGeo.setAttribute('color', new THREE.BufferAttribute(bCol, 3));
  const bMat = new THREE.PointsMaterial({ size: 0.35, map: o.dot, vertexColors: true, transparent: true, opacity: 0, blending: THREE.AdditiveBlending, depthWrite: false, toneMapped: false });
  scene.add(new THREE.Points(bGeo, bMat));
  const BURSTS = [19.6, 20.3, 20.9];
  tickers.push((t) => {
    let shown = false;
    for (let b = 0; b < BURSTS.length; b++) {
      const age = t - BURSTS[b]!;
      if (age < 0 || age > 1.6) continue;
      shown = true;
      const centre = v.copy(o.logoAt).add(new THREE.Vector3((b - 1) * 4.5, 1.5 + b * 0.8, 1));
      for (let i = b; i < BURST; i += BURSTS.length) {
        const vel = bVel[i]!;
        bPos[i * 3] = centre.x + vel.x * age;
        bPos[i * 3 + 1] = centre.y + vel.y * age - 2.2 * age * age;
        bPos[i * 3 + 2] = centre.z + vel.z * age;
      }
      bMat.opacity = Math.max(bMat.opacity, 1 - age / 1.6);
    }
    if (!shown) bMat.opacity = 0;
    bGeo.attributes.position!.needsUpdate = true;
  });

  return (t, cam, dt) => { for (const f of tickers) f(t, cam, dt); };
}

/** The intro runs longer than its 24 s script: the ride through the decorated
 *  tunnel is stretched (more time for the Halloween details) and the ending
 *  gets an extra beat. Maps real seconds → script seconds. */
export const REAL_DURATION = 33;
const KEYS: [number, number][] = [[0, 0], [4, 4], [17, 12], [27, 20], [33, 24]];
export function scriptTime(real: number): number {
  for (let i = 1; i < KEYS.length; i++) {
    const [r0, s0] = KEYS[i - 1]!, [r1, s1] = KEYS[i]!;
    if (real <= r1) return s0 + ((real - r0) / (r1 - r0)) * (s1 - s0);
  }
  return 24 + (real - REAL_DURATION);
}
/** Script seconds → real seconds (to schedule sounds). */
export function realTime(script: number): number {
  for (let i = 1; i < KEYS.length; i++) {
    const [r0, s0] = KEYS[i - 1]!, [r1, s1] = KEYS[i]!;
    if (script <= s1) return r0 + ((script - s0) / (s1 - s0)) * (r1 - r0);
  }
  return REAL_DURATION + (script - 24);
}
