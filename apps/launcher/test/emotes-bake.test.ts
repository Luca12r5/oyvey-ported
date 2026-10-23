import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Quaternion, Vector3 } from 'three';
import { toMc, bake } from '../../../tools/emotes/bake.ts';
import { EMOTES } from '../src/renderer/emotes.ts';

const close = (a: number[], b: number[]) => a.forEach((v, i) => assert.ok(Math.abs(v - b[i]!) < 1e-6, `${a} vs ${b}`));

test('rest pose of the skinview3d figure maps to the Minecraft player pivots', () => {
  const id = new Quaternion(), one = new Vector3(1, 1, 1);
  close(toMc('head', new Vector3(0, 0, 0), id, one).pivot, [0, 0, 0]);
  close(toMc('body', new Vector3(0, -6, 0), id, one).pivot, [0, 0, 0]);
  close(toMc('rightArm', new Vector3(-5, -2, 0), id, one).pivot, [-5, 2, 0]);
  close(toMc('leftArm', new Vector3(5, -2, 0), id, one).pivot, [5, 2, 0]);
  close(toMc('rightLeg', new Vector3(-1.9, -12, 0), id, one).pivot, [-1.9, 12, 0]);
  close(toMc('rightLeg', new Vector3(-1.9, -12, 0), id, one).rot, [0, 0, 0]);
});

test('a forward arm swing keeps its direction in Minecraft space', () => {
  // skinview3d: rotation.x = -PI/2 points the arm forward (+z); in Minecraft forward is -z, xRot = -PI/2 as well.
  const q = new Quaternion().setFromAxisAngle(new Vector3(1, 0, 0), -Math.PI / 2);
  const m = toMc('rightArm', new Vector3(-5, -2, 0), q, new Vector3(1, 1, 1));
  close(m.pivot, [-5, 2, 0]);
  close(m.rot, [-Math.PI / 2, 0, 0]);
});

test('baking is deterministic and covers every emote', () => {
  const a = bake(), b = bake();
  assert.equal(a.meta.length, EMOTES.length);
  assert.ok(a.meta.length >= 50);
  assert.deepEqual(Array.from(a.data.slice(0, 2000)), Array.from(b.data.slice(0, 2000)));
});

test('the client\'s baked emotes are up to date (run: node --experimental-strip-types tools/emotes/bake.ts)', async () => {
  const { readFileSync } = await import('node:fs');
  const dir = new URL('../../../lego-client/src/main/resources/assets/legoclient/emotes/', import.meta.url);
  const meta = JSON.parse(readFileSync(new URL('baked.json', dir), 'utf8')) as { emotes: { id: string; frames: number }[] };
  const { meta: now, data } = bake();
  assert.deepEqual(meta.emotes.map((e) => [e.id, e.frames]), now.map((e) => [e.id, e.frames]));
  const bin = readFileSync(new URL('baked.bin', dir));
  assert.equal(bin.length, data.length * 2);
  for (let i = 0; i < data.length; i += 997) assert.equal(bin.readInt16BE(i * 2), data[i], `value ${i}`);
});
