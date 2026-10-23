import { test } from 'node:test';
import assert from 'node:assert/strict';
import { classifyDrop, safeFileName } from '../src/core/dropimport.ts';

test('dropped files are recognised', () => {
  assert.equal(classifyDrop('Fabulously Optimized.mrpack'), 'modpack');
  assert.equal(classifyDrop('pack.zip', ['manifest.json', 'overrides/config/a.toml']), 'modpack');
  assert.equal(classifyDrop('sodium.jar'), 'mod');
  assert.equal(classifyDrop('Faithful.zip', ['pack.mcmeta', 'assets/minecraft/textures/block/stone.png']), 'resourcepack');
  assert.equal(classifyDrop('Complementary.zip', ['shaders/composite.fsh', 'shaders/lang/en_us.lang']), 'shaderpack');
  assert.equal(classifyDrop('Nested.zip', ['Nested/pack.mcmeta']), 'resourcepack');
  assert.equal(classifyDrop('steve.PNG'), 'skin');
  assert.equal(classifyDrop('mein-design.json'), 'design');
  assert.equal(classifyDrop('random.zip', ['a.txt']), 'unknown');
  assert.equal(classifyDrop('virus.exe'), 'unknown');
});

test('file names cannot leave the target folder', () => {
  assert.equal(safeFileName('../../evil.jar'), 'evil.jar');
  assert.equal(safeFileName('C:\\x\\..\\mod.jar'), 'mod.jar');
  assert.equal(safeFileName('...hidden.jar'), 'hidden.jar');
  assert.equal(safeFileName('ok mod (1.21).jar'), 'ok mod (1.21).jar');
});
