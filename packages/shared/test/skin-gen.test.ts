import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { DEFAULT_SKIN_SPEC, renderSkin, sanitizeSkinSpec } from '../src/index.ts';

test('skin spec is sanitised', () => {
  const s = sanitizeSkinSpec({ skin: 'pink', hairStyle: 'mohawk', top: { kind: 'cape', color: '#ABCDEF' }, beard: 'yes', slim: true });
  assert.equal(s.skin, DEFAULT_SKIN_SPEC.skin);
  assert.equal(s.hairStyle, 'short');
  assert.equal(s.top.kind, 'hoodie');
  assert.equal(s.top.color, '#abcdef');
  assert.equal(s.beard, false);
  assert.equal(s.slim, true);
});

test('renders a complete, deterministic 64x64 skin', () => {
  const px = renderSkin(DEFAULT_SKIN_SPEC);
  assert.equal(px.length, 64 * 64 * 4);
  const alpha = (x: number, y: number) => px[(y * 64 + x) * 4 + 3];
  // every base-layer face is filled: head front, body front, both arms and legs
  for (const [x, y] of [[8, 8], [15, 15], [20, 20], [27, 31], [44, 20], [36, 52], [4, 20], [20, 52]] as const) assert.equal(alpha(x, y), 255, `${x},${y}`);
  // the eyes use the eye colour
  const eye = px.slice((12 * 64 + 10) * 4, (12 * 64 + 10) * 4 + 3);
  assert.ok(eye[2]! > eye[0]!, 'blue eye');
  assert.equal(createHash('sha1').update(renderSkin(structuredClone(DEFAULT_SKIN_SPEC))).digest('hex'), createHash('sha1').update(px).digest('hex'));
  // styles change the result
  assert.notDeepEqual(renderSkin({ ...DEFAULT_SKIN_SPEC, hairStyle: 'long', accessory: 'cap' }), px);
});
