// Visual-VR hands: two dark gloved hands in the lower corners of the view, each
// with a pointer ray. The right hand follows the mouse (its ray ends on what the
// mouse points at, with a glowing dot); the left hand rests and points ahead.
// Fingers curl a little, the index finger points while hovering, a click pokes.

import * as THREE from 'three';
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js';

export interface Hand {
  group: THREE.Group;
  /** Tip of the index finger in world space. */
  tip(out: THREE.Vector3): THREE.Vector3;
  pose(point: number, poke: number): void;
}

function hand(mat: THREE.Material, right: boolean): Hand {
  const g = new THREE.Group();
  const s = right ? 1 : -1;
  const palm = new THREE.Mesh(new RoundedBoxGeometry(0.082, 0.026, 0.09, 3, 0.011), mat);
  palm.position.z = -0.045;
  g.add(palm);
  const cuff = new THREE.Mesh(new RoundedBoxGeometry(0.07, 0.04, 0.07, 3, 0.016), mat);
  cuff.position.z = 0.02;
  g.add(cuff);
  const finger = (x: number, len: number) => {
    const root = new THREE.Group();
    root.position.set(x * s, 0, -0.09);
    const a = new THREE.Mesh(new THREE.CapsuleGeometry(0.0085, len * 0.55, 4, 8), mat);
    a.rotation.x = -Math.PI / 2;
    a.position.z = -len * 0.3;
    const mid = new THREE.Group();
    mid.position.z = -len * 0.58;
    const b = new THREE.Mesh(new THREE.CapsuleGeometry(0.008, len * 0.38, 4, 8), mat);
    b.rotation.x = -Math.PI / 2;
    b.position.z = -len * 0.22;
    mid.add(b);
    root.add(a, mid);
    g.add(root);
    return { root, mid };
  };
  const fingers = [finger(-0.028, 0.06), finger(-0.009, 0.068), finger(0.01, 0.064), finger(0.028, 0.052)];
  const thumb = new THREE.Group();
  thumb.position.set(-0.042 * s, -0.004, -0.03);
  const tm = new THREE.Mesh(new THREE.CapsuleGeometry(0.0095, 0.04, 4, 8), mat);
  tm.rotation.x = -Math.PI / 2;
  tm.position.z = -0.025;
  thumb.add(tm);
  thumb.rotation.y = 0.55 * s;
  g.add(thumb);
  g.traverse((o) => { (o as THREE.Mesh).castShadow = true; });
  const index = fingers[0]!;
  const tipLocal = new THREE.Vector3(0, 0, -0.04);
  return {
    group: g,
    tip: (out) => index.mid.localToWorld(out.copy(tipLocal)),
    pose(point, poke) {
      // 0 = relaxed curl, 1 = index straight while the others curl in.
      fingers.forEach((f, i) => {
        const curl = i === 0 ? 0.35 * (1 - point) : 0.45 + 0.6 * point;
        f.root.rotation.x = -curl;
        f.mid.rotation.x = -curl * 1.2;
      });
      thumb.rotation.x = -0.2 - 0.3 * point;
      palm.position.z = -0.045 - poke * 0.02;
    },
  };
}

export interface Hands {
  root: THREE.Group;
  right: Hand;
  left: Hand;
  rayR: THREE.Line;
  rayL: THREE.Line;
  dot: THREE.Mesh;
  dispose(): void;
}

export function makeHands(accent: string): Hands {
  const mat = new THREE.MeshStandardMaterial({ color: '#1d1f25', roughness: 0.55, metalness: 0.15 });
  const right = hand(mat, true), left = hand(mat, false);
  const root = new THREE.Group();
  root.add(right.group, left.group);
  right.group.scale.setScalar(0.8);
  left.group.scale.setScalar(0.8);
  const rayMat = (o: number) => new THREE.LineBasicMaterial({ color: accent, transparent: true, opacity: o, depthWrite: false, toneMapped: false });
  const geo = () => new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(), new THREE.Vector3(0, 0, -1)]);
  const rayR = new THREE.Line(geo(), rayMat(0.85));
  const rayL = new THREE.Line(geo(), rayMat(0.35));
  rayR.frustumCulled = rayL.frustumCulled = false;
  const dot = new THREE.Mesh(new THREE.SphereGeometry(0.012, 16, 12), new THREE.MeshBasicMaterial({ color: '#ffffff', toneMapped: false }));
  const halo = new THREE.Mesh(new THREE.RingGeometry(0.016, 0.026, 24), new THREE.MeshBasicMaterial({ color: accent, transparent: true, opacity: 0.8, side: THREE.DoubleSide, toneMapped: false, depthWrite: false }));
  dot.add(halo);
  dot.userData.halo = halo;
  return {
    root, right, left, rayR, rayL, dot,
    dispose() {
      root.traverse((o) => (o as THREE.Mesh).geometry?.dispose());
      mat.dispose();
      for (const l of [rayR, rayL]) { l.geometry.dispose(); (l.material as THREE.Material).dispose(); }
      dot.geometry.dispose(); (dot.material as THREE.Material).dispose();
      halo.geometry.dispose(); (halo.material as THREE.Material).dispose();
    },
  };
}

/** Stretch a unit ray line (0,0,0)→(0,0,-1) from `from` to `to` in world space. */
export function aimRay(line: THREE.Line, from: THREE.Vector3, to: THREE.Vector3): void {
  const pos = line.geometry.attributes.position as THREE.BufferAttribute;
  pos.setXYZ(0, from.x, from.y, from.z);
  pos.setXYZ(1, to.x, to.y, to.z);
  pos.needsUpdate = true;
  line.geometry.computeBoundingSphere();
}
