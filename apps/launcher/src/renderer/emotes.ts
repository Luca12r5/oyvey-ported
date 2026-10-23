// Emotes for the 3D character on the play screen (skinview3d). Two kinds:
// - transformations: the body parts (head, body, arms, legs) fly apart and click
//   together into an object (car, chair, rocket ...), move as that object and
//   then build the figure again;
// - dances and gestures: classic poses and moves on the joints.
// Every emote is a function of time that returns where each part goes; the
// engine blends from the normal figure to that pose (parts one after another,
// with a little spring) and back. Extra pieces (wheels, flames, a guitar) are
// small blocks added for the emote and removed afterwards.

import { PlayerAnimation, type PlayerObject } from 'skinview3d';
import { BoxGeometry, ConeGeometry, CylinderGeometry, Euler, Group, Mesh, MeshLambertMaterial, MeshBasicMaterial, Quaternion, SphereGeometry, Vector3, type Object3D } from 'three';
import { brickClick, uiSoundsOn } from './nr/lib/sound.ts';

export type EmoteCat = 'transform' | 'dance' | 'gesture';
type V = [number, number, number];
export type Part = 'head' | 'body' | 'rightArm' | 'leftArm' | 'rightLeg' | 'leftLeg';
export const PARTS: Part[] = ['head', 'body', 'rightArm', 'leftArm', 'rightLeg', 'leftLeg'];

/**
 * Where a part goes. Joint style (default): `r` rotates the part at its joint
 * (shoulder, hip, neck), `d` moves it. Free style: `c` is the centre of the
 * part's box in skin space (y up, face towards +z, feet at y = -24), `r` its
 * rotation (order YXZ). `s` scales the box in both styles.
 */
export interface PartPose { c?: V; r?: V; d?: V; s?: V }
/** Turns a group of parts (all when `parts` is missing) around `c` by `r`, then moves it by `p`. */
export interface Asm { parts?: Part[]; c?: V; r?: V; p?: V }
export interface Frame { parts: Partial<Record<Part, PartPose>>; asm?: Asm[] }

export interface Ctx { t: number; k: number; dur: number }
export interface Extra { group: Group; update?: (c: Ctx) => void }

export interface Emote {
  id: string;
  name: string;
  cat: EmoteCat;
  dur: number;
  /** Shows the cape during the emote (off for most transformations). */
  cape?: boolean;
  sound?: Sfx;
  pose: (c: Ctx) => Frame;
  extras?: () => Extra;
}

const PI = Math.PI;
const H = PI / 2;
const S = Math.sin, C = Math.cos;
const clamp = (x: number, a = 0, b = 1) => Math.max(a, Math.min(b, x));
const smooth = (x: number) => { const t = clamp(x); return t * t * (3 - 2 * t); };
const outBack = (x: number) => { const t = clamp(x), c1 = 1.70158, c3 = c1 + 1; return 1 + c3 * (t - 1) ** 3 + c1 * (t - 1) ** 2; };
const tiny: V = [0.02, 0.02, 0.02];
const UPPER: Part[] = ['head', 'body', 'rightArm', 'leftArm'];

// ---- extra pieces ----------------------------------------------------------
const mat = (color: number, glow = false) => glow ? new MeshBasicMaterial({ color }) : new MeshLambertMaterial({ color });
function box(g: Group, w: number, h: number, d: number, color: number, p: V, r: V = [0, 0, 0], glow = false): Mesh {
  const m = new Mesh(new BoxGeometry(w, h, d), mat(color, glow));
  m.position.set(...p);
  m.rotation.set(r[0], r[1], r[2], 'YXZ');
  g.add(m);
  return m;
}
function cyl(g: Group, rad: number, h: number, color: number, p: V, r: V = [0, 0, 0], glow = false, top = rad): Mesh {
  const m = new Mesh(new CylinderGeometry(top, rad, h, 16), mat(color, glow));
  m.position.set(...p);
  m.rotation.set(r[0], r[1], r[2], 'YXZ');
  g.add(m);
  return m;
}
function ball(g: Group, rad: number, color: number, p: V, glow = false): Mesh {
  const m = new Mesh(new SphereGeometry(rad, 14, 10), mat(color, glow));
  m.position.set(...p);
  g.add(m);
  return m;
}
function flame(g: Group, p: V, size = 1): { group: Group; flick: (t: number) => void } {
  const f = new Group();
  f.position.set(...p);
  const outer = new Mesh(new ConeGeometry(2.6 * size, 7 * size, 12), new MeshBasicMaterial({ color: 0xff7a1a, transparent: true, opacity: 0.85 }));
  outer.rotation.x = PI;
  outer.position.y = -3.5 * size;
  const inner = new Mesh(new ConeGeometry(1.4 * size, 4.5 * size, 10), new MeshBasicMaterial({ color: 0xfff2a8 }));
  inner.rotation.x = PI;
  inner.position.y = -2.4 * size;
  f.add(outer, inner);
  g.add(f);
  return { group: f, flick: (t) => { const k = 0.8 + 0.3 * Math.abs(S(t * 31)) + 0.1 * S(t * 53); f.scale.set(1, k, 1); } };
}
const extra = (build: (g: Group) => ((c: Ctx) => void) | void): (() => Extra) => () => {
  const group = new Group();
  const update = build(group) ?? undefined;
  return { group, update };
};
/** Puffs that rise and fade from a point, looping. */
function puffs(g: Group, p: V, color = 0xdadada, n = 5): (c: Ctx) => void {
  const list = Array.from({ length: n }, () => { const m = ball(g, 1.6, color, p); (m.material as MeshLambertMaterial).transparent = true; return m; });
  return (c) => list.forEach((m, i) => {
    const k = ((c.t * 0.9 + i / n) % 1);
    m.position.set(p[0] + S(i * 2.3) * 1.5 * k, p[1] + k * 12, p[2] - k * 3);
    m.scale.setScalar(0.6 + k * 1.4);
    (m.material as MeshLambertMaterial).opacity = 0.8 * (1 - k);
  });
}

// ---- transformations -------------------------------------------------------
const lieX: V = [0, 0, H];       // limb or body lying along x
const lieZ: V = [H, 0, 0];       // lying along z
const flatX: V = [H, H, 0];      // body flat, long side along x (12 x 4 x 8)

const TRANSFORMS: Emote[] = [
  {
    id: 'car', name: 'Auto', cat: 'transform', dur: 6, sound: 'engine',
    pose: ({ t }) => {
      const spin = t * 9, bump = S(t * 22) * 0.25;
      return {
        parts: {
          body: { c: [0, -16.5 + bump, 0], r: lieZ },
          head: { c: [0, -11.2 + bump, -1], s: [0.85, 0.85, 0.85] },
          rightArm: { c: [-5.6, -21.6, 4.5], r: [spin, 0, H], s: [1, 0.42, 1] },
          leftArm: { c: [5.6, -21.6, 4.5], r: [spin, 0, H], s: [1, 0.42, 1] },
          rightLeg: { c: [-5.6, -21.6, -4.5], r: [spin, 0, H], s: [1, 0.42, 1] },
          leftLeg: { c: [5.6, -21.6, -4.5], r: [spin, 0, H], s: [1, 0.42, 1] },
        },
        asm: [{ c: [0, -18, 0], r: [0, S(t * 0.9) * 0.6, 0], p: [S(t * 0.9) * 4, 0, 0] }],
      };
    },
    extras: extra((g) => {
      box(g, 1.6, 1.2, 0.6, 0xfff3b0, [-2.6, -16.5, 6.2], [0, 0, 0], true);
      box(g, 1.6, 1.2, 0.6, 0xfff3b0, [2.6, -16.5, 6.2], [0, 0, 0], true);
      box(g, 1.4, 1, 0.6, 0xff3030, [-2.8, -16.5, -6.2], [0, 0, 0], true);
      box(g, 1.4, 1, 0.6, 0xff3030, [2.8, -16.5, -6.2], [0, 0, 0], true);
      return puffs(g, [2.5, -19, -7], 0xbcbcbc, 4);
    }),
  },
  {
    id: 'chair', name: 'Stuhl', cat: 'transform', dur: 5, sound: 'clack',
    pose: ({ t }) => ({
      parts: {
        rightLeg: { c: [-3, -18, 3] }, leftLeg: { c: [3, -18, 3] },
        rightArm: { c: [-3, -18, -3] }, leftArm: { c: [3, -18, -3] },
        head: { c: [0, -10.4 + S(t * 3) * 0.15, 0], s: [1, 0.4, 1] },
        body: { c: [0, -2.4, -3.2], s: [1, 1, 0.5] },
      },
      asm: [{ c: [0, -24, 0], r: [0, 0.8, S(t * 2.4) * 0.04] }],
    }),
  },
  {
    id: 'table', name: 'Tisch', cat: 'transform', dur: 5, sound: 'clack',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -10, 0], r: flatX, s: [1.3, 1.3, 1] },
        rightArm: { c: [-6, -18, -3.6] }, leftArm: { c: [6, -18, -3.6] },
        rightLeg: { c: [-6, -18, 3.6] }, leftLeg: { c: [6, -18, 3.6] },
        head: { c: [0, -5.4, 0], r: [0, t * 1.5, 0], s: [0.6, 0.6, 0.6] },
      },
    }),
  },
  {
    id: 'rocket', name: 'Rakete', cat: 'transform', dur: 6.5, sound: 'rocket',
    pose: ({ t }) => {
      const up = t > 2.2 && t < 5.6 ? Math.min(30, (t - 2.2) ** 2 * 7) : 0;
      const shake = t > 1.6 && t < 5.6 ? S(t * 70) * 0.35 : 0;
      return {
        parts: {
          body: { c: [0, -10, 0] },
          head: { c: [0, 0.2, 0], r: [0, PI / 4, 0], s: [0.78, 1, 0.78] },
          rightLeg: { c: [-4.6, -19, 0], r: [0, 0, -0.35] }, leftLeg: { c: [4.6, -19, 0], r: [0, 0, 0.35] },
          rightArm: { c: [0, -19, 3.6], r: [0.35, 0, 0], s: [1, 1, 0.8] }, leftArm: { c: [0, -19, -3.6], r: [-0.35, 0, 0], s: [1, 1, 0.8] },
        },
        asm: [{ p: [shake, up, 0], c: [0, -10, 0], r: [0, t > 2.2 ? (t - 2.2) * 2 : 0, 0] }],
      };
    },
    extras: extra((g) => {
      const f = flame(g, [0, -16.5, 0], 1.3);
      const smoke = puffs(g, [0, -24, 0], 0xcfcfcf, 6);
      return (c) => { f.flick(c.t); f.group.visible = c.t > 1.4 && c.t < 5.8; smoke(c); };
    }),
  },
  {
    id: 'plane', name: 'Flugzeug', cat: 'transform', dur: 6, sound: 'whoosh',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -12, 0], r: lieZ },
        rightArm: { c: [-9.6, -12, 1], r: lieX }, leftArm: { c: [9.6, -12, 1], r: lieX },
        rightLeg: { c: [0, -8.2, -6], r: [-0.25, 0, 0], s: [0.5, 0.6, 1] },
        leftLeg: { c: [0, -12, -6.5], r: lieX, s: [1, 0.85, 0.5] },
        head: { c: [0, -12, 8], s: [0.75, 0.75, 0.75] },
      },
      asm: [{ c: [0, -12, 0], r: [S(t * 1.3) * 0.08, S(t * 0.7) * 0.5, S(t * 1.1) * 0.3], p: [0, 4 + S(t * 1.7) * 1.5, 0] }],
    }),
    extras: extra((g) => {
      const prop = box(g, 9, 0.8, 0.4, 0x3a3a3a, [0, -12, 11.3]);
      return (c) => { prop.rotation.z = c.t * 30; };
    }),
  },
  {
    id: 'heli', name: 'Hubschrauber', cat: 'transform', dur: 6, sound: 'chop',
    pose: ({ t }) => {
      const a = t * 14;
      const lift = smooth((t - 1.2) / 1.5) * 9 * (1 - smooth((t - 4.4) / 1));
      return {
        parts: {
          head: { c: [0, -16, 3] },
          body: { c: [0, -15, -8.5], r: lieZ, s: [0.5, 1, 0.5] },
          rightArm: { c: [0, -10.6, 2], r: [0, a, H], s: [0.6, 1.5, 0.3] },
          leftArm: { c: [0, -10.4, 2], r: [0, a + H, H], s: [0.6, 1.5, 0.3] },
          rightLeg: { c: [-4.2, -22, 2.5], r: lieZ, s: [0.5, 1, 0.5] },
          leftLeg: { c: [4.2, -22, 2.5], r: lieZ, s: [0.5, 1, 0.5] },
        },
        asm: [{ c: [0, -16, 0], r: [S(t * 2) * 0.06, S(t * 0.8) * 0.4, S(t * 1.6) * 0.05], p: [0, lift, 0] }],
      };
    },
    extras: extra((g) => {
      const tail = box(g, 0.4, 5, 0.8, 0x3a3a3a, [1.2, -15, -14.5]);
      return (c) => { tail.rotation.x = c.t * 30; };
    }),
  },
  {
    id: 'boat', name: 'Boot', cat: 'transform', dur: 6, sound: 'waves',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -21, 0], r: lieZ },
        head: { c: [0, -15, -3.2], s: [0.8, 0.8, 0.8] },
        rightArm: { c: [0, -12, 2.5] },
        leftArm: { c: [0, -5.5, 2.5], r: [0, 0, H], s: [0.5, 0.6, 0.5] },
        rightLeg: { c: [-6.3, -21.2, 0], r: [S(t * 3) * 0.5, 0, 1.15] },
        leftLeg: { c: [6.3, -21.2, 0], r: [S(t * 3) * 0.5, 0, -1.15] },
      },
      asm: [{ c: [0, -22, 0], r: [S(t * 1.2) * 0.06, 0, S(t * 1.6) * 0.12], p: [0, S(t * 2.1) * 0.6, 0] }],
    }),
    extras: extra((g) => {
      const sail = new Mesh(new ConeGeometry(4, 9, 3), mat(0xf4f1e6));
      sail.position.set(0, -10, 4.8);
      sail.scale.set(1, 1, 0.15);
      g.add(sail);
      box(g, 22, 0.4, 22, 0x2a78c8, [0, -23.6, 0]);
    }),
  },
  {
    id: 'train', name: 'Zug', cat: 'transform', dur: 6, sound: 'train',
    pose: ({ t }) => {
      const spin = t * 8;
      return {
        parts: {
          body: { c: [0, -17, 2], r: lieZ },
          head: { c: [0, -11, -2.5] },
          rightArm: { c: [0, -12.6, 6], s: [0.6, 0.45, 0.6] },
          leftArm: { c: [0, -19.5, 8.6], r: lieX, s: [1, 0.7, 0.5] },
          rightLeg: { c: [0, -21.6, 5], r: [spin, 0, H], s: [1, 1.1, 1] },
          leftLeg: { c: [0, -21.6, -2], r: [spin, 0, H], s: [1, 1.1, 1] },
        },
        asm: [{ c: [0, -18, 0], r: [0, -0.5 + S(t * 0.6) * 0.3, S(t * 18) * 0.01], p: [0, Math.abs(S(t * 16)) * 0.3, 0] }],
      };
    },
    extras: extra((g) => {
      box(g, 14, 0.6, 1, 0x7a5a3a, [0, -23.7, 7]);
      box(g, 14, 0.6, 1, 0x7a5a3a, [0, -23.7, -4]);
      return puffs(g, [0, -10, 6], 0xeeeeee, 6);
    }),
  },
  {
    id: 'tank', name: 'Panzer', cat: 'transform', dur: 6, sound: 'engine',
    pose: ({ t }) => {
      const a = S(t * 0.9) * 0.9;
      const recoil = t % 1.8 < 0.25 ? 1.6 * (1 - (t % 1.8) / 0.25) : 0;
      return {
        parts: {
          body: { c: [0, -18.6, 0], r: flatX },
          rightLeg: { c: [-7.6, -22, 0], r: lieZ }, leftLeg: { c: [7.6, -22, 0], r: lieZ },
          head: { c: [0, -13.4, 0], r: [0, a, 0], s: [0.8, 0.7, 0.8] },
          rightArm: { c: [S(a) * (8 - recoil), -13.6, C(a) * (8 - recoil)], r: [H, a, 0], s: [0.45, 0.75, 0.45] },
          leftArm: { c: [-2.6, -7.8, -2.2], s: [0.25, 0.55, 0.25] },
        },
        asm: [{ c: [0, -20, 0], r: [S(t * 9) * 0.01, 0, 0], p: [0, 0, S(t * 0.6) * 2] }],
      };
    },
    extras: extra((g) => {
      const muzzle = ball(g, 1.8, 0xffb040, [0, -13.6, 14], true);
      return (c) => { const k = c.t % 1.8; muzzle.visible = k < 0.12; muzzle.position.set(S(S(c.t * 0.9) * 0.9) * 14, -13.6, C(S(c.t * 0.9) * 0.9) * 14); };
    }),
  },
  {
    id: 'scooter', name: 'Roller', cat: 'transform', dur: 5.5, sound: 'whoosh',
    pose: ({ t }) => ({
      parts: {
        leftLeg: { c: [0, -20.6, 0], r: lieZ, s: [1, 1, 0.55] },
        rightLeg: { c: [0, -13.5, 5.6], r: [-0.15, 0, 0] },
        rightArm: { c: [0, -7.6, 6.4], r: lieX, s: [1, 0.8, 0.6] },
        head: { c: [0, -3.4, 6.6], s: [0.55, 0.55, 0.55] },
        body: { c: [0, -18.8, -4.5], r: lieZ, s: [0.6, 0.35, 0.6] },
        leftArm: { c: [0, -16.4, -6.4], s: [0.4, 0.3, 0.4] },
      },
      asm: [{ c: [0, -20, 0], r: [0, S(t * 0.8) * 0.7, S(t * 1.6) * 0.08], p: [S(t * 0.8) * 5, 0, 0] }],
    }),
    extras: extra((g) => {
      const w1 = cyl(g, 1.8, 1.4, 0x222222, [0, -22.2, 6.2], [0, 0, H]);
      const w2 = cyl(g, 1.8, 1.4, 0x222222, [0, -22.2, -6.2], [0, 0, H]);
      return (c) => { w1.rotation.x = w2.rotation.x = c.t * 9; };
    }),
  },
  {
    id: 'skateboard', name: 'Skateboard', cat: 'transform', dur: 6, sound: 'skate',
    pose: ({ t }) => {
      const flip = smooth((t - 2.4) / 0.55) * PI * 2 + smooth((t - 4) / 0.55) * PI * 2;
      const hop = (t > 2.3 && t < 3.05 ? S(clamp((t - 2.3) / 0.75) * PI) : 0) + (t > 3.9 && t < 4.65 ? S(clamp((t - 3.9) / 0.75) * PI) : 0);
      const w: V = [0.6, 0.2, 0.6];
      return {
        parts: {
          body: { c: [0, -20.6, 0], r: lieZ, s: [1, 1, 0.4] },
          rightArm: { c: [-3, -22.8, 4.4], s: w }, leftArm: { c: [3, -22.8, 4.4], s: w },
          rightLeg: { c: [-3, -22.8, -4.4], s: w }, leftLeg: { c: [3, -22.8, -4.4], s: w },
          head: { c: [0, -15.4 + Math.abs(S(t * 5)) * 0.6, 0] },
        },
        asm: [
          { parts: ['body', 'rightArm', 'leftArm', 'rightLeg', 'leftLeg'], c: [0, -21, 0], r: [0, 0, flip] },
          { c: [0, -21, 0], r: [0, S(t * 0.7) * 0.8, 0], p: [S(t * 0.7) * 5, hop * 7, 0] },
        ],
      };
    },
  },
  {
    id: 'tower', name: 'Turm', cat: 'transform', dur: 5.5, sound: 'clack',
    pose: ({ t }) => ({
      parts: {
        rightLeg: { c: [0, -22, 0], r: lieX }, leftLeg: { c: [0, -18, 0], r: lieZ },
        rightArm: { c: [0, -14, 0], r: lieX }, leftArm: { c: [0, -10, 0], r: lieZ },
        body: { c: [0, -2, 0] },
        head: { c: [0, 8, 0], r: [0, S(t * 2) * 0.4, 0] },
      },
      asm: [{ c: [0, -24, 0], r: [S(t * 2.6) * 0.03, 0, S(t * 2) * 0.05] }],
    }),
  },
  {
    id: 'bridge', name: 'Brücke', cat: 'transform', dur: 6, sound: 'clack',
    pose: ({ t }) => {
      const x = ((t * 8) % 34) - 17;
      const y = Math.abs(x) < 7 ? -5.6 : -5.6 - (Math.abs(x) - 7) * 0.62;
      return {
        parts: {
          rightLeg: { c: [-6, -18, 0] }, leftLeg: { c: [6, -18, 0] },
          body: { c: [0, -10, 0], r: flatX },
          rightArm: { c: [-11.4, -15.6, 0], r: [0, 0, H - 0.55] },
          leftArm: { c: [11.4, -15.6, 0], r: [0, 0, -(H - 0.55)] },
          head: { c: [x, y, 0], s: [0.45, 0.45, 0.45], r: [0, 0, Math.abs(x) < 7 ? 0 : (x < 0 ? 0.55 : -0.55)] },
        },
      };
    },
  },
  {
    id: 'snake', name: 'Schlange', cat: 'transform', dur: 6, sound: 'hiss',
    pose: ({ t }) => {
      const seg = (i: number): { c: V; r: V } => {
        const z = 15 - i * 10.5;
        const x = 4 * S(t * 4 - i * 0.9);
        const dx = 4 * C(t * 4 - i * 0.9) * 0.9 / 10.5 * 4;
        return { c: [x, -22, z], r: [H, Math.atan(dx), 0] };
      };
      return {
        parts: {
          head: { c: [4 * S(t * 4 + 0.9), -20 + S(t * 6) * 0.4, 24], r: [0.1, Math.atan(C(t * 4 + 0.9) * 0.4), 0], s: [0.9, 0.7, 1] },
          body: { ...seg(0), s: [0.8, 1, 1] }, rightArm: seg(1), leftArm: seg(2), rightLeg: seg(3), leftLeg: seg(4),
        },
        asm: [{ c: [0, -22, 0], r: [0, -0.7, 0] }],
      };
    },
    extras: extra((g) => {
      const tongue = box(g, 0.5, 0.3, 3, 0xe02040, [0, -21.5, 29]);
      return (c) => { tongue.visible = S(c.t * 7) > 0.3; tongue.position.x = 4 * S(c.t * 4 + 0.9); };
    }),
  },
  {
    id: 'ball', name: 'Ball', cat: 'transform', dur: 5.5, sound: 'boing',
    pose: ({ t }) => {
      const x = S(t * 1.2) * 7;
      const s: V = [1, 0.72, 1];
      return {
        parts: {
          head: { c: [0, -16, 0], s: [1.05, 1.05, 1.05] },
          body: { c: [0, -16, 0], s: [0.7, 0.55, 0.9] },
          rightArm: { c: [-4.6, -16, 0], s }, leftArm: { c: [4.6, -16, 0], s },
          rightLeg: { c: [0, -16, 4.6], r: lieZ, s }, leftLeg: { c: [0, -16, -4.6], r: lieZ, s },
        },
        asm: [
          { c: [0, -16, 0], r: [0, 0, -x / 8] },
          { p: [x, Math.abs(S(t * 3.6)) * 3, 0] },
        ],
      };
    },
  },
  {
    id: 'spider', name: 'Spinne', cat: 'transform', dur: 6, sound: 'hiss',
    pose: ({ t }) => {
      const w = (i: number) => S(t * 10 + i * PI / 2) * 0.3;
      return {
        parts: {
          body: { c: [0, -17, 0], r: lieZ },
          head: { c: [0, -16.5, 8], s: [0.7, 0.7, 0.7] },
          rightArm: { c: [-6.6, -19, 3], r: [0, 0.4 + w(0), H + 0.5] },
          rightLeg: { c: [-6.6, -19, -3], r: [0, -0.4 + w(1), H + 0.5] },
          leftArm: { c: [6.6, -19, 3], r: [0, -0.4 + w(2), -(H + 0.5)] },
          leftLeg: { c: [6.6, -19, -3], r: [0, 0.4 + w(3), -(H + 0.5)] },
        },
        asm: [{ c: [0, -18, 0], r: [0, S(t * 0.8) * 0.9, 0], p: [0, Math.abs(S(t * 10)) * 0.4, S(t * 0.8) * 3] }],
      };
    },
    extras: extra((g) => {
      ball(g, 0.7, 0xff1a1a, [-1.4, -15.4, 11], true);
      ball(g, 0.7, 0xff1a1a, [1.4, -15.4, 11], true);
      ball(g, 0.5, 0xff1a1a, [-0.6, -14.4, 11], true);
      ball(g, 0.5, 0xff1a1a, [0.6, -14.4, 11], true);
    }),
  },
  {
    id: 'crab', name: 'Krabbe', cat: 'transform', dur: 6, sound: 'snip',
    pose: ({ t }) => {
      const snip = Math.abs(S(t * 6)) * 0.5;
      return {
        parts: {
          body: { c: [0, -19, 0], r: flatX },
          head: { c: [0, -15.6, 1.5], s: [0.5, 0.5, 0.5] },
          rightArm: { c: [-8.5, -15, 6], r: [-1.1 - snip, -0.5, -0.4] },
          leftArm: { c: [8.5, -15, 6], r: [-1.1 - snip, 0.5, 0.4] },
          rightLeg: { c: [-8, -21.4, -1], r: [S(t * 12) * 0.3, 0, H + 0.4] },
          leftLeg: { c: [8, -21.4, -1], r: [-S(t * 12) * 0.3, 0, -(H + 0.4)] },
        },
        asm: [{ c: [0, -20, 0], r: [0, 0, S(t * 12) * 0.03], p: [S(t * 1.1) * 7, 0, 0] }],
      };
    },
    extras: extra((g) => {
      cyl(g, 0.4, 3, 0x222222, [-1.2, -12.5, 2]);
      cyl(g, 0.4, 3, 0x222222, [1.2, -12.5, 2]);
      ball(g, 0.9, 0x111111, [-1.2, -10.8, 2]);
      ball(g, 0.9, 0x111111, [1.2, -10.8, 2]);
    }),
  },
  {
    id: 'windmill', name: 'Windrad', cat: 'transform', dur: 6, sound: 'whoosh',
    pose: ({ t }) => {
      const hub: V = [0, -6, 4.5];
      const blade = (i: number): PartPose => {
        const a = t * 3 + i * H;
        return { c: [hub[0] - S(a) * 6.5, hub[1] + C(a) * 6.5, hub[2] + i * 0.05], r: [0, 0, a], s: [1, 1, 0.35] };
      };
      return {
        parts: {
          body: { c: [0, -18, 0], s: [0.8, 1, 1] },
          head: { c: [0, -8, 0], s: [0.9, 0.9, 0.9] },
          rightArm: blade(0), leftArm: blade(1), rightLeg: blade(2), leftLeg: blade(3),
        },
      };
    },
  },
  {
    id: 'cube', name: 'Würfel', cat: 'transform', dur: 5, sound: 'clack',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -18, 4] },
        rightLeg: { c: [-2, -18, 0] }, leftLeg: { c: [2, -18, 0] },
        rightArm: { c: [-2, -18, -4] }, leftArm: { c: [2, -18, -4] },
        head: { c: [0, -8, 0] },
      },
      asm: [{ c: [0, -16, 0], r: [0, smooth((t - 1.2) / 1.2) * H + smooth((t - 2.8) / 1) * H, 0], p: [0, Math.abs(S(t * 2.2)) * 1.2, 0] }],
    }),
  },
  {
    id: 'arrow', name: 'Pfeil', cat: 'transform', dur: 5, sound: 'whoosh',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -12, 0], s: [0.5, 1.2, 1] },
        head: { c: [0, 0.5, 0], r: [0, 0, PI / 4], s: [0.95, 0.95, 0.5] },
        rightArm: { c: [-3.2, -20.5, 0], r: [0, 0, -0.6], s: [1, 0.6, 0.5] },
        leftArm: { c: [3.2, -20.5, 0], r: [0, 0, 0.6], s: [1, 0.6, 0.5] },
        rightLeg: { c: [0, -20.5, 0], s: tiny }, leftLeg: { c: [0, -20.5, 0], s: tiny },
      },
      asm: [{ c: [0, -10, 0], r: [0, t * 2, 0], p: [0, 3 + S(t * 2.4) * 2, 0] }],
    }),
  },
  {
    id: 'sofa', name: 'Sofa', cat: 'transform', dur: 5, sound: 'clack',
    pose: ({ t }) => ({
      parts: {
        body: { c: [0, -20, 0.8], r: flatX },
        rightLeg: { c: [0, -16, -4], r: lieX }, leftLeg: { c: [0, -12, -4], r: lieX },
        rightArm: { c: [-8, -18, 0], r: lieZ, s: [1, 0.85, 1] }, leftArm: { c: [8, -18, 0], r: lieZ, s: [1, 0.85, 1] },
        head: { c: [-2.6, -15 + Math.abs(S(t * 3)) * 0.5, 0.6], r: [0.1, 0.3, 0.15], s: [0.55, 0.5, 0.4] },
      },
    }),
  },
  {
    id: 'bed', name: 'Bett', cat: 'transform', dur: 6, sound: 'snore',
    pose: () => ({
      parts: {
        body: { c: [0, -20.4, 0], r: lieZ },
        head: { c: [0, -17.2, -4.2], s: [0.9, 0.35, 0.5] },
        rightLeg: { c: [-6, -21, 0], r: lieZ, s: [0.5, 1.05, 1] }, leftLeg: { c: [6, -21, 0], r: lieZ, s: [0.5, 1.05, 1] },
        rightArm: { c: [-2, -16, -7], s: [1, 0.9, 0.5] }, leftArm: { c: [2, -16, -7], s: [1, 0.9, 0.5] },
      },
    }),
    extras: extra((g) => {
      box(g, 8.4, 0.6, 7.5, 0x3f6fd8, [0, -18.1, 2.2]);
      const z = [0, 1, 2].map((i) => box(g, 1.4 + i * 0.4, 1.4 + i * 0.4, 0.3, 0xffffff, [0, -14, -4], [0, 0, 0], true));
      return (c) => z.forEach((m, i) => { const k = (c.t * 0.5 + i / 3) % 1; m.position.set(2 + k * 4, -14 + k * 9, -4); m.scale.setScalar(0.5 + k); m.visible = c.t > 1; });
    }),
  },
  {
    id: 'throne', name: 'Thron', cat: 'transform', dur: 5.5, sound: 'fanfare',
    pose: ({ t }) => ({
      parts: {
        rightLeg: { c: [-3, -18, 2.6] }, leftLeg: { c: [3, -18, 2.6] },
        body: { c: [0, -10, 0], r: lieZ, s: [1, 0.7, 1] },
        rightArm: { c: [-3, -14, -3.6], s: [1, 1.15, 1] }, leftArm: { c: [3, -14, -3.6], s: [1, 1.15, 1] },
        head: { c: [0, -1.6 + S(t * 2) * 0.3, -3.6], r: [0, S(t * 1.4) * 0.2, 0], s: [0.8, 0.8, 0.8] },
      },
    }),
    extras: extra((g) => {
      box(g, 9.6, 12, 0.8, 0xd8a520, [0, -8, -6]);
      box(g, 1.2, 1.2, 1.2, 0xff2a4a, [0, -3, -5.4], [0, 0, PI / 4], true);
      [-3, -1, 1, 3].forEach((x) => box(g, 1, 2.4, 1, 0xf0c020, [x, 3.6, -3.6]));
      box(g, 7.6, 1.2, 7.6, 0xf0c020, [0, 2.2, -3.6]);
    }),
  },
  {
    id: 'lamp', name: 'Lampe', cat: 'transform', dur: 6, sound: 'click',
    pose: ({ t }) => {
      const nod = S(t * 1.6) * 0.2;
      return {
        parts: {
          body: { c: [0, -22, 0], r: flatX },
          rightLeg: { c: [0, -14, 0] },
          leftLeg: { c: [2.6, -4.2, 0], r: [0, 0, -0.45 + nod * 0.5] },
          rightArm: { c: [0, -24, 0], s: tiny },
          leftArm: { c: [0, -24, 0], s: tiny },
          head: { c: [6.6 + nod * 2, 1.2 + nod * 2, 0], r: [0, 0, -0.9 + nod], s: [0.9, 0.75, 0.9] },
        },
      };
    },
    extras: extra((g) => {
      const bulb = ball(g, 1.6, 0xfff6c0, [8.8, -2, 0], true);
      const beam = new Mesh(new ConeGeometry(7, 18, 18, 1, true), new MeshBasicMaterial({ color: 0xfff0a0, transparent: true, opacity: 0.18, depthWrite: false }));
      beam.position.set(12, -10, 0);
      beam.rotation.z = 0.5;
      g.add(beam);
      return (c) => { const nod = S(c.t * 1.6) * 0.2; bulb.position.set(8.8 + nod * 3, -2 + nod * 2.4, 0); beam.position.set(12.5 + nod * 4, -10 + nod * 2, 0); beam.rotation.z = 0.55 - nod; };
    }),
  },
  {
    id: 'robot', name: 'Roboter', cat: 'transform', dur: 6, sound: 'robot',
    pose: ({ t }) => {
      const st = Math.floor(t * 3) % 4;
      const legA = st === 1 ? -0.5 : st === 3 ? 0.5 : 0;
      return {
        parts: {
          head: { r: [0, (st - 1.5) * 0.25, 0], d: [0, 0.6, 0], s: [1.08, 1, 1.08] },
          body: { s: [1.1, 1, 1.25] },
          rightArm: { r: [-H, 0, 0], d: [-0.4, 0, 0] }, leftArm: { r: [st % 2 ? -H : -0.2, 0, 0], d: [0.4, 0, 0] },
          rightLeg: { r: [legA, 0, 0] }, leftLeg: { r: [-legA, 0, 0] },
        },
        asm: [{ r: [0, Math.floor(t * 1.5) * 0.4 - 0.6, 0] }],
      };
    },
    extras: extra((g) => {
      cyl(g, 0.3, 4, 0x9a9a9a, [0, 10.5, 0]);
      const tip = ball(g, 0.9, 0xff3030, [0, 12.8, 0], true);
      box(g, 6.4, 1.2, 0.4, 0x30e0ff, [0, 4.6, 4.3], [0, 0, 0], true);
      const lights = [-2, 0, 2].map((x) => box(g, 1, 1, 0.5, 0x40ff80, [x, -4, 2.8], [0, 0, 0], true));
      return (c) => { tip.visible = S(c.t * 8) > 0; lights.forEach((l, i) => { l.visible = Math.floor(c.t * 4 + i) % 3 !== 0; }); };
    }),
  },
  {
    id: 'star', name: 'Stern', cat: 'transform', dur: 5.5, sound: 'sparkle',
    pose: ({ t }) => {
      const hub: V = [0, -8, 0];
      const spin = t * 1.2;
      const pt = (i: number, s: V = [1, 1, 1]): PartPose => {
        const a = spin + i * (2 * PI / 5);
        return { c: [hub[0] - S(a) * 9.5, hub[1] + C(a) * 9.5, 0], r: [0, 0, a], s };
      };
      return {
        parts: {
          head: { c: hub, r: [0, 0, spin], s: [0.9, 0.9, 0.9] },
          body: pt(0, [0.55, 1, 1]), rightArm: pt(1), rightLeg: pt(2), leftLeg: pt(3), leftArm: pt(4),
        },
        asm: [{ p: [0, 4 + S(t * 2) * 1.5, 0] }],
      };
    },
    extras: extra((g) => {
      const sp = Array.from({ length: 10 }, () => box(g, 0.8, 0.8, 0.8, 0xfff1a0, [0, 0, 0], [0, 0, PI / 4], true));
      return (c) => sp.forEach((m, i) => { const a = c.t * 0.8 + i * 0.63; const r = 16 + S(c.t * 2 + i) * 2; m.position.set(C(a) * r, -4 + S(a) * r, -1); m.visible = S(c.t * 6 + i * 2) > -0.3; });
    }),
  },
];

// ---- dances and gestures ---------------------------------------------------
const dance = (id: string, name: string, dur: number, pose: (c: Ctx) => Frame, more: Partial<Emote> = {}): Emote => ({ id, name, cat: 'dance', dur, cape: true, pose, sound: 'beat', ...more });
const gesture = (id: string, name: string, dur: number, pose: (c: Ctx) => Frame, more: Partial<Emote> = {}): Emote => ({ id, name, cat: 'gesture', dur, cape: true, pose, ...more });
const bounce = (t: number, f = 4, a = 0.8): Asm => ({ p: [0, Math.abs(S(t * f)) * a, 0] });

const DANCES: Emote[] = [
  gesture('wave', 'Winken', 3, ({ t }) => ({ parts: { rightArm: { r: [0, 0, PI * 0.85 + S(t * 12) * 0.25] }, head: { r: [0, 0, S(t * 2) * 0.08] } } }), { sound: 'pop' }),
  dance('dab', 'Dab', 2.8, ({ t }) => {
    const k = smooth((t - 0.2) / 0.25) * (1 - smooth((t - 2.3) / 0.3));
    return { parts: { rightArm: { r: [-0.6 * k, 0, -2.3 * k] }, leftArm: { r: [-1.2 * k, -0.6 * k, -0.9 * k] }, head: { r: [0.7 * k, 0.5 * k, 0] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [0.12 * k, 0, 0] }] };
  }, { sound: 'pop' }),
  dance('floss', 'Floss', 4, ({ t }) => {
    const sw = S(t * 9), hip = S(t * 9 + H) * 0.3;
    return { parts: { rightArm: { r: [sw * 0.5, 0, 0.35 + sw * 0.35] }, leftArm: { r: [-sw * 0.5, 0, -0.35 + sw * 0.35] }, rightLeg: { d: [hip * 2, 0, 0] }, leftLeg: { d: [hip * 2, 0, 0] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [0, 0, -hip * 0.4] }] };
  }),
  dance('moonwalk', 'Moonwalk', 5, ({ t }) => {
    const ph = S(t * 6);
    return { parts: { rightLeg: { r: [0.35 * Math.max(0, ph), 0, 0], d: [0, Math.max(0, ph) * 1.2, 0] }, leftLeg: { r: [0.35 * Math.max(0, -ph), 0, 0], d: [0, Math.max(0, -ph) * 1.2, 0] }, rightArm: { r: [ph * 0.3, 0, 0.1] }, leftArm: { r: [-ph * 0.3, 0, -0.1] }, head: { r: [0, 0, S(t * 3) * 0.1] } }, asm: [{ r: [0, -H, 0] }, { p: [-8 + ((t * 4) % 16), 0, 0] }] };
  }, { sound: 'beat' }),
  dance('breakdance', 'Breakdance', 5, ({ t }) => {
    const k = smooth((t - 0.3) / 0.5) * (1 - smooth((t - 4.2) / 0.5));
    return { parts: { rightLeg: { r: [0, 0, -0.9 - S(t * 8) * 0.3] }, leftLeg: { r: [0, 0, 0.9 + S(t * 8) * 0.3] }, rightArm: { r: [0, 0, PI * 0.95] }, leftArm: { r: [0, 0, -PI * 0.95] } }, asm: [{ c: [0, 4, 0], r: [0, t * 9 * k, PI * k] }, { p: [0, -24 * k, 0] }] };
  }),
  dance('backflip', 'Salto', 3, ({ t }) => {
    const k = smooth((t - 0.6) / 0.9);
    const jump = t > 0.5 && t < 1.6 ? S(clamp((t - 0.5) / 1.1) * PI) * 14 : 0;
    const tuck = S(k * PI);
    return { parts: { rightLeg: { r: [-1.6 * tuck, 0, 0] }, leftLeg: { r: [-1.6 * tuck, 0, 0] }, rightArm: { r: [-2.6 * tuck - (t < 0.6 ? 0.8 * smooth(t / 0.5) : 0), 0, 0] }, leftArm: { r: [-2.6 * tuck - (t < 0.6 ? 0.8 * smooth(t / 0.5) : 0), 0, 0] } }, asm: [{ c: [0, -12, 0], r: [-k * 2 * PI, 0, 0], p: [0, jump - (t < 0.5 ? 2 * S(t / 0.5 * PI) : 0), 0] }] };
  }, { sound: 'whoosh' }),
  dance('cartwheel', 'Radschlag', 3.6, ({ t }) => {
    const k = smooth((t - 0.3) / 2.6);
    return { parts: { rightArm: { r: [0, 0, -PI * 0.95] }, leftArm: { r: [0, 0, PI * 0.95] }, rightLeg: { r: [0, 0, -0.55] }, leftLeg: { r: [0, 0, 0.55] } }, asm: [{ c: [0, -12, 0], r: [0, 0, -k * 2 * PI], p: [-10 + 20 * k, Math.abs(S(k * PI * 2)) * 2, 0] }] };
  }, { sound: 'whoosh' }),
  gesture('pushups', 'Liegestütze', 5, ({ t }) => {
    const k = smooth(t / 0.6) * (1 - smooth((t - 4.3) / 0.6));
    const th = (0.99 + ((S(t * 4) + 1) / 2) * 0.28) * k;
    return { parts: { rightArm: { r: [-th, 0, 0] }, leftArm: { r: [-th, 0, 0] }, head: { r: [-0.5 * k, 0, 0] } }, asm: [{ c: [0, -24, 0], r: [th, 0, 0], p: [0, 0, -8 * k] }] };
  }, { sound: 'boing', cape: false }),
  gesture('meditate', 'Meditieren', 6, ({ t }) => {
    const float = 3 + S(t * 1.6) * 1.2;
    return { parts: { rightLeg: { r: [-H, 0.8, 0], d: [0, 0, 2] }, leftLeg: { r: [-H, -0.8, 0], d: [0, 0, 2] }, rightArm: { r: [-0.5, 0, 0.6] }, leftArm: { r: [-0.5, 0, -0.6] }, head: { r: [0.15, 0, 0] } }, asm: [{ p: [0, -8 + float, 0] }] };
  }, { sound: 'om' }),
  gesture('cheer', 'Jubeln', 3, ({ t }) => ({ parts: { rightArm: { r: [0, 0, 2.8 + S(t * 14) * 0.15] }, leftArm: { r: [0, 0, -2.8 - S(t * 14) * 0.15] }, head: { r: [-0.25, 0, 0] } }, asm: [{ p: [0, Math.abs(S(t * 6)) * 4, 0] }] }), { sound: 'tada' }),
  gesture('clap', 'Applaus', 3, ({ t }) => {
    const c = Math.abs(S(t * 11));
    return { parts: { rightArm: { r: [-1.2, -0.7 + c * 0.35, 0] }, leftArm: { r: [-1.2, 0.7 - c * 0.35, 0] }, head: { r: [0.1, 0, S(t * 3) * 0.1] } } };
  }, { sound: 'clap' }),
  gesture('bow', 'Verbeugung', 3, ({ t }) => {
    const k = smooth(t / 0.7) * (1 - smooth((t - 2.1) / 0.7));
    return { parts: { rightArm: { r: [-0.4 * k, 0, 0.9 * k] }, leftArm: { r: [-1.0 * k, 0.4 * k, -0.3 * k] }, head: { r: [0.3 * k, 0, 0] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [0.9 * k, 0, 0] }] };
  }, { sound: 'pop' }),
  dance('guitar', 'Gitarre', 5, ({ t }) => ({ parts: { rightArm: { r: [-0.7 + S(t * 16) * 0.25, 0.3, 0.2] }, leftArm: { r: [-1.2, -0.9, 0] }, head: { r: [0.25 + Math.abs(S(t * 6)) * 0.35, 0, 0] }, rightLeg: { r: [-0.15 * Math.abs(S(t * 6)), 0, 0] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [-0.12, 0, 0] }] }), {
    sound: 'guitar',
    extras: extra((g) => {
      const gt = new Group();
      box(gt, 6, 5, 1.2, 0xc0392b, [0, 0, 0]);
      box(gt, 4.4, 3.8, 1.3, 0xe74c3c, [0, 0, 0]);
      box(gt, 1, 11, 0.8, 0x6b4426, [0, 8, 0]);
      box(gt, 2, 2.2, 1, 0x3a2614, [0, 14, 0]);
      gt.position.set(1, -9, 4.2);
      gt.rotation.z = -1.0;
      g.add(gt);
    }),
  }),
  dance('drums', 'Schlagzeug', 5, ({ t }) => ({ parts: { rightArm: { r: [-1.0 - Math.max(0, S(t * 14)) * 0.6, 0, 0] }, leftArm: { r: [-1.0 - Math.max(0, -S(t * 14)) * 0.6, 0, 0] }, head: { r: [Math.abs(S(t * 7)) * 0.3, 0, 0] }, rightLeg: { r: [-H, 0, 0] }, leftLeg: { r: [-H, 0, 0] } }, asm: [{ p: [0, -5.5, 0] }] }), {
    sound: 'drums',
    extras: extra((g) => {
      cyl(g, 3.2, 2.4, 0xdddddd, [-3.5, -14, 8]);
      cyl(g, 3.2, 2.4, 0xdddddd, [3.5, -14, 8]);
      cyl(g, 5, 6, 0x2b5fd8, [0, -21, 11], [H, 0, 0]);
      const cym = cyl(g, 3.6, 0.3, 0xe8c040, [7, -8, 6]);
      return (c) => { cym.rotation.x = S(c.t * 14) * 0.15; };
    }),
  }),
  dance('dj', 'DJ', 5, ({ t }) => ({ parts: { rightArm: { r: [-1.1 + S(t * 8) * 0.15, -0.3, 0] }, leftArm: { r: [-0.4 + Math.max(0, S(t * 4)) * -2.4, 0, 0] }, head: { r: [Math.abs(S(t * 8)) * 0.35, 0, 0] } }, asm: [bounce(t, 8, 0.6)] }), {
    sound: 'beat',
    extras: extra((g) => {
      box(g, 14, 2, 6, 0x1c1c22, [0, -11, 7]);
      box(g, 14.4, 9, 5.6, 0x26262e, [0, -16.6, 7.2]);
      const d1 = cyl(g, 2.2, 0.4, 0x0a0a0a, [-3.6, -9.8, 7]);
      const d2 = cyl(g, 2.2, 0.4, 0x0a0a0a, [3.6, -9.8, 7]);
      const lights = [-5, -2.5, 0, 2.5, 5].map((x, i) => box(g, 1.6, 1.6, 0.4, [0xff2a6d, 0x2affd5, 0xffd52a, 0x8a2aff, 0x2a8aff][i]!, [x, -16, 10.1], [0, 0, 0], true));
      return (c) => { d1.rotation.y = c.t * 8; d2.rotation.y = -c.t * 8; lights.forEach((l, i) => { l.visible = Math.floor(c.t * 8 + i * 3) % 4 !== 0; }); };
    }),
  }),
  gesture('zombie', 'Zombie', 5, ({ t }) => ({ parts: { rightArm: { r: [-H + S(t * 2) * 0.1, 0, 0] }, leftArm: { r: [-H - S(t * 2.2) * 0.1, 0, 0] }, head: { r: [0.2, 0, S(t * 1.5) * 0.35] }, rightLeg: { r: [S(t * 2.5) * 0.35, 0, 0] }, leftLeg: { r: [-S(t * 2.5) * 0.35, 0, 0] } }, asm: [{ r: [0, 0, S(t * 2.5) * 0.06] }, { p: [0, 0, S(t * 0.6) * 3] }] }), { sound: 'groan' }),
  dance('ninja', 'Ninja', 4.5, ({ t }) => {
    const chop = t % 1.5 < 0.35 ? smooth((t % 1.5) / 0.2) : 1 - smooth((t % 1.5 - 0.35) / 0.6);
    return { parts: { rightArm: { r: [-1.4 - chop * 0.8, 0, -0.3] }, leftArm: { r: [-1.2, 0.6, 0] }, rightLeg: { r: [-0.5, 0, -0.3] }, leftLeg: { r: [0.4, 0, 0.3] }, head: { r: [0.1, -0.3, 0] } }, asm: [{ r: [0, 0.6 + Math.floor(t / 1.5) * 1.2, 0] }, { p: [0, -2.2, 0] }] };
  }, {
    sound: 'whoosh', extras: extra((g) => {
      const band = box(g, 8.6, 1.4, 8.6, 0x111111, [0, 5.6, 0]);
      const tail = box(g, 0.6, 1, 5, 0x111111, [0, 5.6, -6]);
      return (c) => { tail.rotation.x = S(c.t * 10) * 0.3; band.visible = true; };
    }),
  }),
  dance('karate', 'Karate', 4.5, ({ t }) => {
    const k = (t % 1.5) / 1.5;
    const kick = S(clamp((k - 0.2) / 0.4) * PI);
    return { parts: { rightLeg: { r: [-1.7 * kick, 0, 0] }, rightArm: { r: [-1.4 + kick * 0.5, 0, 0.4] }, leftArm: { r: [-H * (1 - kick), 0, -0.3] }, head: { r: [-0.1, 0, 0] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [-0.3 * kick, 0, 0] }, { r: [0, Math.floor(t / 1.5) * H * 0.7 - 0.7, 0] }] };
  }, { sound: 'hiya' }),
  dance('superhero', 'Superheld', 5, ({ t }) => {
    const k = smooth((t - 0.4) / 0.8) * (1 - smooth((t - 4) / 0.8));
    return { parts: { rightArm: { r: [-PI * k, 0, 0] }, leftArm: { r: [0, 0, -0.2 * k] }, rightLeg: { r: [0.1 * k, 0, 0] }, leftLeg: { r: [-0.1 * k, 0, 0] }, head: { r: [-0.9 * k, 0, 0] } }, asm: [{ c: [0, -12, 0], r: [H * k, S(t) * 0.6 * k, S(t * 1.5) * 0.2 * k], p: [0, 10 * k + S(t * 2) * 1.5 * k, 0] }] };
  }, { sound: 'whoosh', cape: true }),
  gesture('headless', 'Kopf-ab-Trick', 5, ({ t }) => {
    const up = smooth((t - 0.6) / 0.6) * (1 - smooth((t - 3.8) / 0.6));
    const a = (t - 1.2) * 2.2;
    return { parts: { head: { d: [S(a) * 6 * up, 8 * up + S(t * 4) * up, C(a) * 3 * up], r: [S(t * 3) * up * 0.4, t * 3 * up, 0] }, rightArm: { r: [0, 0, 2.4 * up + S(t * 6) * 0.15 * up] }, leftArm: { r: [0, 0, -2.4 * up - S(t * 6 + 1) * 0.15 * up] } } };
  }, { sound: 'pop' }),
  dance('explode', 'Explosion', 4.5, ({ t }) => {
    const k = t < 0.4 ? 0 : t < 1.0 ? outBack((t - 0.4) / 0.6) : t < 3.2 ? 1 : 1 - smooth((t - 3.2) / 0.9);
    const shake = t < 0.4 ? S(t * 90) * 0.4 : 0;
    const o = (x: number, y: number, z: number): V => [x * k, y * k, z * k];
    const spin = (i: number): V => [t * (2 + i) * k, t * (1.5 + i * 0.5) * k, t * (1 + i * 0.3) * k];
    return { parts: { head: { d: o(0, 12, 2), r: spin(0) }, body: { d: o(0, 2, -8), r: spin(1) }, rightArm: { d: o(-12, 4, 2), r: spin(2) }, leftArm: { d: o(12, 4, 2), r: spin(3) }, rightLeg: { d: o(-8, -2, 6), r: spin(1.5) }, leftLeg: { d: o(8, -2, 6), r: spin(2.5) } }, asm: [{ p: [shake, 0, 0] }] };
  }, {
    sound: 'boom', cape: false,
    extras: extra((g) => {
      const flash = ball(g, 6, 0xffc040, [0, -6, 0], true);
      (flash.material as MeshBasicMaterial).transparent = true;
      return (c) => { const k = clamp((c.t - 0.4) / 0.5); flash.visible = c.t > 0.4 && c.t < 0.9; flash.scale.setScalar(0.5 + k * 3); (flash.material as MeshBasicMaterial).opacity = 1 - k; };
    }),
  }),
  dance('jelly', 'Wackelpudding', 4, ({ t }) => {
    const w = S(t * 14) * Math.exp(-((t % 2) * 1.2));
    return { parts: { head: { s: [1 + w * 0.15, 1 - w * 0.12, 1 + w * 0.15], r: [0, 0, w * 0.2] }, body: { s: [1 - w * 0.12, 1 + w * 0.1, 1 - w * 0.12] }, rightArm: { r: [0, 0, 0.3 + w * 0.6] }, leftArm: { r: [0, 0, -0.3 - w * 0.6] } }, asm: [{ c: [0, -24, 0], r: [w * 0.1, 0, w * 0.18] }] };
  }, { sound: 'boing' }),
  dance('robotdance', 'Roboter-Tanz', 5, ({ t }) => {
    const st = Math.floor(t * 4) % 8;
    const poses: [number, number, number, number][] = [[-H, 0, 0, 0], [-H, -H, 0, 0.3], [0, -H, 0.3, 0], [-PI, 0, 0, -0.3], [-H, -PI, -0.3, 0], [0, 0, 0, 0], [-0.8, -0.8, 0.4, 0.4], [-H, -H, -0.4, -0.4]];
    const p = poses[st]!;
    return { parts: { rightArm: { r: [p[0], 0, 0] }, leftArm: { r: [p[1], 0, 0] }, head: { r: [0, p[2] * 2, 0] }, body: { r: [0, p[3], 0] } }, asm: [{ p: [0, st % 2 ? 0.6 : 0, 0] }] };
  }, { sound: 'robot' }),
  gesture('hop', 'Hüpfen', 3.5, ({ t }) => {
    const h = Math.abs(S(t * 5));
    return { parts: { rightArm: { r: [0, 0, 0.4 + h * 0.6] }, leftArm: { r: [0, 0, -0.4 - h * 0.6] }, rightLeg: { r: [-0.3 * (1 - h), 0, 0] }, leftLeg: { r: [0.3 * (1 - h), 0, 0] } }, asm: [{ p: [0, h * 6, 0] }] };
  }, { sound: 'boing' }),
  gesture('sleep', 'Schlafen', 6, ({ t }) => {
    const k = smooth((t - 0.2) / 0.8) * (1 - smooth((t - 5) / 0.8));
    const br = S(t * 1.8) * 0.03;
    return { parts: { rightArm: { r: [0, 0, 0.2 * k] }, leftArm: { r: [-1.2 * k, 0, 0] }, head: { r: [0, 0.5 * k, 0] }, body: { s: [1 + br, 1, 1 + br] } }, asm: [{ c: [0, -24, 0], r: [-H * k, 0, 0], p: [0, 2 * k, 16 * k] }] };
  }, {
    sound: 'snore', cape: false,
    extras: extra((g) => {
      const z = [0, 1, 2].map((i) => box(g, 1.2 + i * 0.5, 1.2 + i * 0.5, 0.3, 0xffffff, [0, 0, 0], [0, 0, 0], true));
      return (c) => z.forEach((m, i) => { const k = (c.t * 0.45 + i / 3) % 1; m.position.set(3 + k * 5, 8, 4 + k * 10); m.scale.setScalar(0.4 + k); m.visible = c.t > 1.2 && c.t < 5; });
    }),
  }),
  gesture('salute', 'Salutieren', 3, ({ t }) => {
    const k = smooth(t / 0.4) * (1 - smooth((t - 2.4) / 0.4));
    return { parts: { rightArm: { r: [-0.2 * k, -0.9 * k, 2.2 * k] }, head: { r: [-0.1 * k, 0, 0] }, rightLeg: { d: [0.8 * k, 0, 0] }, leftLeg: { d: [-0.8 * k, 0, 0] } } };
  }, { sound: 'fanfare' }),
  gesture('think', 'Nachdenken', 4, ({ t }) => {
    const k = smooth(t / 0.5) * (1 - smooth((t - 3.4) / 0.5));
    return { parts: { rightArm: { r: [-2.1 * k, -0.4 * k, -0.4 * k] }, leftArm: { r: [-0.8 * k, 0.9 * k, 0] }, head: { r: [-0.15 * k, 0.3 * k, 0.15 * k + S(t * 2) * 0.05 * k] }, rightLeg: { r: [-S(t * 4) * 0.08 * k, 0, 0] } } };
  }, { sound: 'hmm' }),
  gesture('facepalm', 'Facepalm', 3.4, ({ t }) => {
    const k = smooth((t - 0.2) / 0.4) * (1 - smooth((t - 2.8) / 0.4));
    return { parts: { rightArm: { r: [-2.5 * k, -0.6 * k, -0.2 * k] }, head: { r: [0.45 * k, 0, S(t * 3) * 0.12 * k] } }, asm: [{ parts: UPPER, c: [0, -12, 0], r: [0.12 * k, 0, 0] }] };
  }, { sound: 'sigh' }),
  dance('disco', 'Disco', 5, ({ t }) => {
    const up = S(t * 4) > 0;
    return { parts: { rightArm: { r: up ? [0, 0, -2.6] : [-0.4, 0, 0.8] }, leftArm: { r: [0, 0, -0.3] }, rightLeg: { r: [0, 0, -0.15 * (up ? 1 : 0)] }, head: { r: [0, 0, up ? -0.2 : 0.15] } }, asm: [{ c: [0, -24, 0], r: [0, 0, up ? 0.08 : -0.08] }, bounce(t, 8, 0.5)] };
  }, {
    sound: 'beat', extras: extra((g) => {
      const ballM = ball(g, 3.4, 0xd8d8e8, [0, 22, 0]);
      const dots = Array.from({ length: 8 }, (_, i) => box(g, 1.5, 1.5, 0.2, [0xff4fd8, 0x4fd8ff, 0xffe44f, 0x7bff4f][i % 4]!, [0, 0, 0], [0, 0, 0], true));
      return (c) => { ballM.rotation.y = c.t * 2; dots.forEach((d, i) => { const a = c.t * 1.5 + i * 0.8; d.position.set(C(a) * 16, -6 + S(a * 1.3) * 10, S(a) * 6 - 6); }); };
    }),
  }),
  dance('jumpingjack', 'Hampelmann', 4, ({ t }) => {
    const o = (1 - C(t * 9)) / 2;
    return { parts: { rightArm: { r: [0, 0, -2.8 * o] }, leftArm: { r: [0, 0, 2.8 * o] }, rightLeg: { r: [0, 0, -0.45 * o] }, leftLeg: { r: [0, 0, 0.45 * o] } }, asm: [{ p: [0, S(t * 9) > 0 ? S(t * 9) * 2 : 0, 0] }] };
  }, { sound: 'boing' }),
  gesture('point', 'Zeigen', 3, ({ t }) => {
    const k = smooth(t / 0.35) * (1 - smooth((t - 2.5) / 0.4));
    return { parts: { rightArm: { r: [-H * k, 0.35 * k, 0] }, head: { r: [-0.1 * k, 0.2 * k, 0] } }, asm: [{ r: [0, -0.4 * k, 0] }] };
  }, { sound: 'pop' }),
  dance('chicken', 'Ententanz', 4.5, ({ t }) => {
    const f = Math.abs(S(t * 10));
    return { parts: { rightArm: { r: [0.2, 0, -0.5 - f * 0.6] }, leftArm: { r: [0.2, 0, 0.5 + f * 0.6] }, head: { d: [0, 0, S(t * 10) * 1.2], r: [0.1, 0, 0] }, rightLeg: { r: [0, 0, -0.25] }, leftLeg: { r: [0, 0, 0.25] } }, asm: [{ p: [0, -2 + f * 0.6, 0] }, { c: [0, -24, 0], r: [0, 0, S(t * 5) * 0.06] }] };
  }, { sound: 'quack' }),
];

export const EMOTES: Emote[] = [...TRANSFORMS, ...DANCES];
export const EMOTE_CATS: { id: EmoteCat; name: string }[] = [
  { id: 'transform', name: 'Verwandlung' },
  { id: 'dance', name: 'Tanz' },
  { id: 'gesture', name: 'Geste' },
];
export const emoteById = (id: string): Emote | undefined => EMOTES.find((e) => e.id === id);

// ---- engine ------------------------------------------------------------------
interface Rest { pos: Vector3; off: Vector3 }
function rests(slim: boolean): Record<Part, Rest> {
  const v = (x: number, y: number, z: number) => new Vector3(x, y, z);
  const ax = slim ? 0.5 : 1;
  return {
    head: { pos: v(0, 0, 0), off: v(0, 4, 0) },
    body: { pos: v(0, -6, 0), off: v(0, 0, 0) },
    rightArm: { pos: v(-5, -2, 0), off: v(-ax, -4, 0) },
    leftArm: { pos: v(5, -2, 0), off: v(ax, -4, 0) },
    rightLeg: { pos: v(-1.9, -12, -0.1), off: v(0, -6, 0) },
    leftLeg: { pos: v(1.9, -12, -0.1), off: v(0, -6, 0) },
  };
}

const ORDER: Part[] = ['rightLeg', 'leftLeg', 'body', 'rightArm', 'leftArm', 'head'];
const qa = new Quaternion(), qb = new Quaternion(), ea = new Euler(), va = new Vector3(), vb = new Vector3();

/** skinview3d animation that plays one emote and then calls `done`. */
export class EmoteAnimation extends PlayerAnimation {
  private t = 0;
  private extra: Extra | null = null;
  private landed = new Set<Part>();
  private finished = false;
  readonly emote: Emote;
  private readonly done: () => void;
  private readonly shape: boolean;
  private stopSound: (() => void) | null = null;
  constructor(emote: Emote, done: () => void) {
    super();
    this.emote = emote;
    this.done = done;
    this.shape = emote.cat === 'transform';
  }

  /** Blend 0..1(+) of a part at time t: parts arrive one after another and leave in reverse. */
  private blend(i: number): number {
    const { t } = this;
    const d = this.emote.dur;
    const inT = this.shape ? 0.5 : 0.28, step = this.shape ? 0.08 : 0;
    const a = (t - i * step) / inT;
    const outStart = d - (this.shape ? 0.75 : 0.3) + (5 - i) * step * 0.6;
    const b = 1 - smooth((t - outStart) / (this.shape ? 0.45 : 0.3));
    return (this.shape ? outBack(a) : smooth(a)) * b;
  }

  /** Stops early (the parts still go back smoothly). */
  stop(): void {
    if (this.t < this.emote.dur - 0.8) this.t = this.emote.dur - 0.8;
  }

  dispose(p: PlayerObject): void {
    this.stopSound?.();
    this.stopSound = null;
    if (this.extra) {
      p.skin.remove(this.extra.group);
      this.extra.group.traverse((o: Object3D) => { const m = o as Mesh; if (m.isMesh) { m.geometry.dispose(); (m.material as MeshBasicMaterial).dispose(); } });
      this.extra = null;
    }
    for (const part of PARTS) p.skin[part].scale.set(1, 1, 1);
    p.cape.visible = true;
    if (this.shape && p.parent) p.parent.rotation.x = 0;
    p.position.set(0, 0, 0);
    p.rotation.set(0, 0, 0);
  }

  protected animate(p: PlayerObject, delta: number): void {
    if (this.finished) return;
    if (this.t === 0) {
      if (this.emote.extras) { this.extra = this.emote.extras(); p.skin.add(this.extra.group); }
      this.stopSound = playSfx(this.emote.sound, this.emote.dur);
    }
    this.t += Math.min(0.05, delta);
    const { t } = this;
    const e = this.emote;
    if (t >= e.dur) {
      this.finished = true;
      this.dispose(p);
      p.skin.resetJoints();
      this.done();
      return;
    }
    const ctx: Ctx = { t, k: t / e.dur, dur: e.dur };
    const f = e.pose(ctx);
    const rest = rests(p.skin.modelType === 'slim');
    p.position.set(0, 0, 0);
    p.rotation.set(0, 0, 0);
    let minB = 1;
    for (const part of PARTS) {
      const i = ORDER.indexOf(part);
      const b = this.blend(i);
      minB = Math.min(minB, b);
      const r = rest[part];
      const pp = f.parts[part] ?? {};
      const sc = pp.s ?? [1, 1, 1];
      const tq = qa.setFromEuler(ea.set(...(pp.r ?? [0, 0, 0]), pp.c ? 'YXZ' : 'XYZ'));
      const tp = va;
      if (pp.c) {
        // Group origin so that the box centre lands on c: c - R * (S * offset)
        vb.copy(r.off).multiply(new Vector3(...sc)).applyQuaternion(tq);
        tp.set(...pp.c).sub(vb);
      } else {
        tp.copy(r.pos).add(new Vector3(...(pp.d ?? [0, 0, 0])));
      }
      for (const a of f.asm ?? []) {
        if (a.parts && !a.parts.includes(part)) continue;
        const c = new Vector3(...(a.c ?? [0, 0, 0]));
        const aq = qb.setFromEuler(ea.set(...(a.r ?? [0, 0, 0]), 'YXZ'));
        tp.sub(c).applyQuaternion(aq).add(c).add(new Vector3(...(a.p ?? [0, 0, 0])));
        tq.premultiply(aq);
      }
      const g = p.skin[part];
      g.position.copy(r.pos).lerp(tp, b);
      g.quaternion.identity().slerp(tq, clamp(b));
      g.scale.set(1 + (sc[0] - 1) * clamp(b), 1 + (sc[1] - 1) * clamp(b), 1 + (sc[2] - 1) * clamp(b));
      if (this.shape && b >= 0.98 && !this.landed.has(part) && t < e.dur / 2) { this.landed.add(part); brickClick(true); }
      if (this.shape && b < 0.5 && t > e.dur / 2) this.landed.delete(part);
    }
    if (this.extra) {
      const eb = clamp(minB);
      const g = this.extra.group;
      g.visible = eb > 0.02;
      g.position.set(0, 0, 0);
      g.quaternion.identity();
      // Whole-figure turns and moves carry the extras along.
      for (const a of f.asm ?? []) {
        if (a.parts) continue;
        const c = new Vector3(...(a.c ?? [0, 0, 0]));
        const aq = new Quaternion().setFromEuler(new Euler(...(a.r ?? [0, 0, 0]), 'YXZ'));
        g.position.sub(c).applyQuaternion(aq).add(c).add(new Vector3(...(a.p ?? [0, 0, 0])));
        g.quaternion.premultiply(aq);
      }
      g.scale.setScalar(Math.max(0.001, eb));
      this.extra.update?.(ctx);
    }
    // Transformations: look a little from above so tops (seats, decks) show.
    if (this.shape && p.parent) p.parent.rotation.x = 0.32 * clamp(minB);
    p.cape.visible = (e.cape ?? false) || minB < 0.15;
    if (e.cape) p.cape.rotation.x = 0.25 + Math.abs(S(t * 3)) * 0.15;
  }
}

// ---- sounds (Web Audio, short and quiet) -------------------------------------
export type Sfx = 'engine' | 'clack' | 'rocket' | 'whoosh' | 'chop' | 'waves' | 'train' | 'skate' | 'hiss' | 'boing' | 'snip' | 'snore' | 'fanfare' | 'click' | 'robot' | 'sparkle'
  | 'pop' | 'beat' | 'om' | 'tada' | 'clap' | 'guitar' | 'drums' | 'groan' | 'hiya' | 'boom' | 'hmm' | 'sigh' | 'quack';

let actx: AudioContext | null = null;
/** Plays the emote sound; returns a function that stops it. */
export function playSfx(kind: Sfx | undefined, dur: number): () => void {
  if (!kind || !uiSoundsOn()) return () => {};
  try {
    actx ??= new AudioContext();
    const a = actx;
    const t0 = a.currentTime;
    const out = a.createGain();
    out.gain.value = 0.12;
    out.connect(a.destination);
    const nodes: AudioScheduledSourceNode[] = [];
    const osc = (type: OscillatorType, f: number, at: number, len: number, g = 1, f2?: number) => {
      const o = a.createOscillator(), v = a.createGain();
      o.type = type;
      o.frequency.setValueAtTime(f, t0 + at);
      if (f2) o.frequency.exponentialRampToValueAtTime(f2, t0 + at + len);
      v.gain.setValueAtTime(0, t0 + at);
      v.gain.linearRampToValueAtTime(g, t0 + at + Math.min(0.02, len / 4));
      v.gain.exponentialRampToValueAtTime(0.001, t0 + at + len);
      o.connect(v).connect(out);
      o.start(t0 + at);
      o.stop(t0 + at + len + 0.05);
      nodes.push(o);
      return o;
    };
    const noise = (at: number, len: number, freq: number, g = 1, q = 1, f2?: number) => {
      const n = Math.floor(a.sampleRate * len);
      const buf = a.createBuffer(1, n, a.sampleRate);
      const d = buf.getChannelData(0);
      for (let i = 0; i < n; i++) d[i] = Math.random() * 2 - 1;
      const s = a.createBufferSource(), bp = a.createBiquadFilter(), v = a.createGain();
      s.buffer = buf;
      bp.type = 'bandpass';
      bp.Q.value = q;
      bp.frequency.setValueAtTime(freq, t0 + at);
      if (f2) bp.frequency.exponentialRampToValueAtTime(f2, t0 + at + len);
      v.gain.setValueAtTime(0, t0 + at);
      v.gain.linearRampToValueAtTime(g, t0 + at + Math.min(0.05, len / 3));
      v.gain.exponentialRampToValueAtTime(0.001, t0 + at + len);
      s.connect(bp).connect(v).connect(out);
      s.start(t0 + at);
      nodes.push(s);
    };
    const beats = (f: (i: number, at: number) => void, every: number, from = 0.3, until = dur - 0.6) => { for (let i = 0, at = from; at < until; i++, at += every) f(i, at); };
    switch (kind) {
      case 'engine': case 'train': {
        const o = osc('sawtooth', kind === 'train' ? 70 : 55, 0.4, dur - 1, 0.5);
        const lfo = a.createOscillator(), lg = a.createGain();
        lfo.frequency.value = kind === 'train' ? 6 : 14;
        lg.gain.value = 12;
        lfo.connect(lg).connect(o.frequency);
        lfo.start(t0 + 0.4); lfo.stop(t0 + dur);
        nodes.push(lfo);
        if (kind === 'train') { osc('square', 660, 0.6, 0.5, 0.25); osc('square', 550, 0.6, 0.5, 0.25); }
        break;
      }
      case 'rocket': noise(1.5, dur - 1.8, 400, 1.4, 0.6, 1200); osc('sine', 80, 1.6, dur - 2, 0.6, 40); break;
      case 'whoosh': noise(0.2, 0.9, 600, 1.2, 1.4, 2400); noise(dur - 1.2, 0.8, 2000, 0.8, 1.4, 500); break;
      case 'chop': beats((_, at) => noise(at, 0.08, 300, 1.2, 1.5), 0.09, 0.6); break;
      case 'waves': beats((_, at) => noise(at, 1.4, 500, 0.6, 0.5, 250), 1.6, 0.4); break;
      case 'skate': noise(0.4, dur - 1, 900, 0.5, 0.8); osc('square', 220, 2.4, 0.08, 0.4); osc('square', 220, 3.9, 0.08, 0.4); break;
      case 'hiss': noise(0.8, 1.6, 5200, 0.6, 2); break;
      case 'boing': beats((i, at) => osc('sine', 220 + (i % 2) * 60, at, 0.3, 0.6, 520), 0.55); break;
      case 'snip': beats((_, at) => noise(at, 0.05, 3800, 1, 3), 0.52); break;
      case 'snore': beats((_, at) => { noise(at, 1.1, 180, 1, 2); osc('sine', 1200, at + 1.3, 0.4, 0.15, 1800); }, 2.2, 0.8); break;
      case 'fanfare': [523, 659, 784, 1046].forEach((f, i) => osc('square', f, 0.3 + i * 0.14, i === 3 ? 0.6 : 0.16, 0.3)); break;
      case 'click': osc('square', 2400, 0.6, 0.03, 0.4); break;
      case 'robot': beats((i, at) => osc('square', [330, 392, 294, 440][i % 4]!, at, 0.12, 0.25), 0.33); break;
      case 'sparkle': beats((i, at) => osc('sine', 1400 + (i * 270) % 1100, at, 0.25, 0.25), 0.18, 0.5); break;
      case 'pop': osc('sine', 400, 0.15, 0.12, 0.7, 900); break;
      case 'beat': beats((i, at) => { osc('sine', 120, at, 0.18, 0.9, 50); if (i % 2) noise(at, 0.06, 7000, 0.4, 1); }, 0.42); break;
      case 'om': osc('sine', 136, 0.5, dur - 1, 0.5); osc('sine', 204, 0.5, dur - 1, 0.25); break;
      case 'tada': [392, 523, 659].forEach((f) => osc('triangle', f, 0.2, 0.9, 0.35)); break;
      case 'clap': beats((_, at) => noise(at, 0.06, 1500, 1.2, 1), 0.285); break;
      case 'guitar': beats((i, at) => [196, 247, 294].forEach((f) => osc('sawtooth', f * (i % 4 === 3 ? 1.33 : 1), at, 0.35, 0.15)), 0.4); break;
      case 'drums': beats((i, at) => { if (i % 2 === 0) osc('sine', 110, at, 0.2, 0.9, 45); else noise(at, 0.12, 1800, 1, 0.8); if (i % 4 === 3) noise(at, 0.4, 6000, 0.5, 0.5); }, 0.22); break;
      case 'groan': osc('sawtooth', 110, 0.4, 1.4, 0.4, 70); osc('sawtooth', 100, 2.4, 1.6, 0.4, 65); break;
      case 'hiya': beats((_, at) => { noise(at, 0.25, 900, 0.9, 1.2, 2500); osc('square', 520, at + 0.05, 0.15, 0.2, 330); }, 1.5, 0.3); break;
      case 'boom': noise(0.4, 1.8, 120, 2.2, 0.4, 40); osc('sine', 70, 0.4, 1.2, 1, 30); break;
      case 'hmm': osc('sine', 180, 0.5, 1.2, 0.4, 160); break;
      case 'sigh': noise(0.4, 1.2, 900, 0.7, 0.6, 300); break;
      case 'quack': beats((_, at) => osc('sawtooth', 520, at, 0.14, 0.3, 300), 0.6); break;
      case 'clack': default: break;
    }
    return () => { out.gain.setTargetAtTime(0, a.currentTime, 0.05); nodes.forEach((n) => { try { n.stop(a.currentTime + 0.2); } catch { /* already stopped */ } }); };
  } catch {
    return () => {};
  }
}
