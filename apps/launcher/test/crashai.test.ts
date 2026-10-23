import { test } from 'node:test';
import assert from 'node:assert/strict';
import { sanitizeCrashResult, crashAiUserMessage } from '@lego/shared';
import { validAiKey } from '../src/main/crashai.ts';

test('AI key format check', () => {
  assert.equal(validAiKey('sk-ant-api03-' + 'a'.repeat(40)), true);
  for (const bad of ['', 'not-a-key', 'sk-ant-short', 42, null]) assert.equal(validAiKey(bad), false);
});

test('AI answer only keeps fixes for real files', () => {
  const mods = [{ filename: 'iris.jar', id: 'iris', title: 'Iris', enabled: true }];
  const r = sanitizeCrashResult({ summary: 'x', causes: [
    { title: 'Iris', detail: 'd', confidence: 'high', fix_kind: 'disable-mod', filename: 'iris.jar', slug: '', mb: 0, fix_label: 'Iris aus' },
    { title: 'Erfunden', detail: 'd', confidence: 'high', fix_kind: 'disable-mod', filename: 'fake.jar', slug: '', mb: 0, fix_label: '' },
  ] }, mods);
  assert.deepEqual(r.causes[0]!.fix, { kind: 'disable-mod', filename: 'iris.jar' });
  assert.equal(r.causes[1]!.fix, null);
  assert.match(crashAiUserMessage({ log: 'L', mods, gameVersion: '1.21.11', loader: 'fabric', memoryMb: 4096, legoClient: true }), /<mods>\niris\.jar \(id iris\)\n<\/mods>/);
});
