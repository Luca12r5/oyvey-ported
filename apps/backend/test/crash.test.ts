import { test } from 'node:test';
import assert from 'node:assert/strict';
import { start, login } from './helpers.ts';
import { sanitizeCrashResult } from '../src/services/crashai.ts';

const mods = [{ filename: 'sodium-0.6.jar', id: 'sodium', title: 'Sodium', enabled: true }, { filename: 'vivecraft-1.21.jar', id: 'vivecraft', title: 'Vivecraft', enabled: true }];

test('crash AI answers are limited to safe fixes on real files', () => {
  const r = sanitizeCrashResult({
    summary: 'Sodium passt nicht zu Vivecraft.',
    causes: [
      { title: 'Sodium inkompatibel', detail: 'x', confidence: 'high', fix_kind: 'disable-mod', filename: 'sodium-0.6.jar', slug: '', mb: 0, fix_label: 'Sodium deaktivieren' },
      { title: 'Erfundene Datei', detail: 'y', confidence: 'high', fix_kind: 'disable-mod', filename: '../../windows/system32.jar', slug: '', mb: 0, fix_label: 'weg' },
      { title: 'RAM', detail: 'z', confidence: 'medium', fix_kind: 'set-memory', filename: '', slug: '', mb: 99999, fix_label: '' },
      { title: 'Abhängigkeit', detail: 'a', confidence: 'high', fix_kind: 'install-mod', filename: '', slug: 'fabric-api', mb: 0, fix_label: 'Fabric API installieren' },
      { title: 'zu viel', detail: '', confidence: 'high', fix_kind: 'none', filename: '', slug: '', mb: 0, fix_label: '' },
    ],
  }, mods);
  assert.equal(r.causes.length, 3, 'at most three causes');
  assert.deepEqual(r.causes[0]!.fix, { kind: 'disable-mod', filename: 'sodium-0.6.jar' });
  assert.equal(r.causes[1]!.fix, null, 'files that are not installed are never touched');
  assert.equal(r.causes[2]!.fix, null, 'RAM outside 1-16 GB is rejected');
});

test('crash AI route: signed in, validated input, daily limit, unavailable without key', async (t) => {
  const seen: { log: string; mods: number }[] = [];
  const h = await start({}, null, null, async (input) => {
    seen.push({ log: input.log, mods: input.mods.length });
    return { summary: 'ok', causes: [{ title: 'Sodium', detail: 'd', confidence: 'high', fix: { kind: 'disable-mod', filename: 'sodium-0.6.jar' } }] };
  });
  t.after(() => h.close());
  const c = await login(h, 'Crashy');
  assert.equal((await c.call('GET', '/api/crash/ai')).body.ai, true);
  const body = { log: 'java.lang.NullPointerException at org.vivecraft.render', mods: [...mods, { filename: '../bad/x.jar' }], gameVersion: '1.21.11', loader: 'fabric', memoryMb: 4096 };
  const r = await c.call('POST', '/api/crash/ai', body);
  assert.equal(r.status, 200);
  assert.equal(r.body.causes[0].title, 'Sodium');
  assert.equal(seen[0]!.mods, 2, 'file names with paths are dropped');
  for (let i = 0; i < 19; i++) await c.call('POST', '/api/crash/ai', body);
  assert.equal((await c.call('POST', '/api/crash/ai', body)).status, 429);

  const none = await start({}, null);
  t.after(() => none.close());
  const c2 = await login(none, 'NoKeyCrash');
  assert.equal((await c2.call('POST', '/api/crash/ai', body)).status, 503);
});
