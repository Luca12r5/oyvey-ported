// Bakes the launcher's emotes (apps/launcher/src/renderer/emotes.ts) for the LEGO client:
// every emote is played on a skinview3d figure at 20 frames per second and, for each body
// part, the final transform (with the engine's blend in/out) is stored in Minecraft's model
// space. The client then plays exactly the same motion (moved, turned and scaled parts).
//
//   node --experimental-strip-types tools/emotes/bake.mts
//
// Output: lego-client/src/main/resources/assets/legoclient/emotes/baked.json + baked.bin
//   bin: per frame and part (head, body, rightArm, leftArm, rightLeg, leftLeg) 9 int16:
//        pivot x,y,z (×100), xRot,yRot,zRot (×10000, ModelPart order Z·Y·X), scale x,y,z (×1000)
import { mkdirSync, writeFileSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Euler, Matrix4, Quaternion, Vector3 } from 'three';
import { PlayerObject } from 'skinview3d';
import { EMOTES, EmoteAnimation, PARTS, type Part } from '../../apps/launcher/src/renderer/emotes.ts';

export const FPS = 20;
const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const out = join(root, 'lego-client', 'src', 'main', 'resources', 'assets', 'legoclient', 'emotes');

/** Box centre relative to the part's group origin in skinview3d (classic arms). */
const OFF_SV: Record<Part, [number, number, number]> = {
  head: [0, 4, 0], body: [0, 0, 0], rightArm: [-1, -4, 0], leftArm: [1, -4, 0], rightLeg: [0, -6, 0], leftLeg: [0, -6, 0],
};
/** Box centre relative to the ModelPart pivot in Minecraft (y down, face towards -z). */
const OFF_MC: Record<Part, [number, number, number]> = {
  head: [0, -4, 0], body: [0, 6, 0], rightArm: [-1, 4, 0], leftArm: [1, 4, 0], rightLeg: [0, 6, 0], leftLeg: [0, 6, 0],
};
const F = new Matrix4().makeScale(1, -1, -1);

export interface McPart { pivot: [number, number, number]; rot: [number, number, number]; scale: [number, number, number] }

/** Converts a skinview3d part transform (group position, rotation, scale) to a Minecraft ModelPart pose. */
export function toMc(part: Part, pos: Vector3, q: Quaternion, s: Vector3): McPart {
  const centre = new Vector3(...OFF_SV[part]).multiply(s).applyQuaternion(q).add(pos);
  const cMc = centre.clone().applyMatrix4(F);
  const r = new Matrix4().makeRotationFromQuaternion(q);
  const rMc = F.clone().multiply(r).multiply(F);
  const off = new Vector3(...OFF_MC[part]).multiply(s).applyMatrix4(rMc);
  const pivot = cMc.sub(off);
  const e = new Euler().setFromRotationMatrix(rMc, 'ZYX');
  return { pivot: [pivot.x, pivot.y, pivot.z], rot: [e.x, e.y, e.z], scale: [s.x, s.y, s.z] };
}

export interface Baked { id: string; name: string; cat: string; dur: number; frames: number; offset: number }

export function bake(): { meta: Baked[]; data: Int16Array } {
  const meta: Baked[] = [];
  const chunks: number[] = [];
  for (const e of EMOTES) {
    const p = new PlayerObject();
    const a = new EmoteAnimation(e, () => {});
    const frames = Math.floor(e.dur * FPS);
    meta.push({ id: e.id, name: e.name, cat: e.cat, dur: e.dur, frames, offset: chunks.length / 54 });
    for (let f = 0; f < frames; f++) {
      if (f > 0) a.update(p, 1 / FPS);
      else a.update(p, 1e-6);
      for (const part of PARTS) {
        const g = p.skin[part];
        const m = toMc(part, g.position, g.quaternion, g.scale);
        const q = (v: number, k: number) => Math.max(-32768, Math.min(32767, Math.round(v * k)));
        chunks.push(...m.pivot.map((v) => q(v, 100)), ...m.rot.map((v) => q(v, 10000)), ...m.scale.map((v) => q(v, 1000)));
      }
    }
    a.dispose(p);
  }
  return { meta, data: Int16Array.from(chunks) };
}

if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) {
  const { meta, data } = bake();
  mkdirSync(out, { recursive: true });
  writeFileSync(join(out, 'baked.json'), JSON.stringify({ fps: FPS, values: 54, emotes: meta }, null, 1));
  const buf = Buffer.alloc(data.length * 2);
  data.forEach((v, i) => buf.writeInt16BE(v, i * 2));
  writeFileSync(join(out, 'baked.bin'), buf);
  console.log(`baked ${meta.length} emotes, ${data.length / 54} frames, ${(buf.length / 1024).toFixed(0)} KB`);
}
