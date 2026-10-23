import { test } from 'node:test';
import assert from 'node:assert/strict';
import { crashSignature, emptyDb, fixKey, parseDb, rank, recordApplied, recordCrash, recordOutcome } from '../src/core/crashlearn.ts';
import type { Diagnosis } from '../src/core/crashdoc.ts';

const LOG = `java.lang.RuntimeException: Mixin transformation failed
Caused by: org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException: Critical injection failure: Mixin [iris.mixins.json:MixinLevelRenderer] from mod iris
\tat org.spongepowered.asm.mixin.transformer.MixinProcessor.applyMixins(MixinProcessor.java:392)
\tat net.irisshaders.iris.pipeline.WorldRenderingPipeline.begin(WorldRenderingPipeline.java:77)
`;

test('crash signature is stable and ignores line numbers', () => {
  const a = crashSignature(LOG);
  assert.equal(a, 'org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException|net.irisshaders.iris|mod:iris');
  assert.equal(crashSignature(LOG.replace(/:\d+\)/g, ':1)')), a);
  assert.equal(crashSignature('all fine'), null);
});

test('fix keys ignore mod versions', () => {
  assert.equal(fixKey({ kind: 'disable-mod', filename: 'iris-fabric-1.8.1+mc1.21.jar' }), fixKey({ kind: 'disable-mod', filename: 'iris-fabric-1.9.0+mc1.21.jar' }));
});

test('a fix that helped comes first and is offered again', () => {
  const db = emptyDb();
  const sig = crashSignature(LOG);
  recordCrash(db, sig);
  const iris: Diagnosis = { id: 'x', title: 'Iris passt nicht', detail: '', fix: { kind: 'disable-mod', filename: 'iris-1.8.jar' }, fixLabel: 'Iris aus', confidence: 'medium' };
  recordApplied(db, 'p1', sig, iris);
  recordOutcome(db, 'p1', 0, null);
  const other: Diagnosis = { id: 'm', title: 'RAM', detail: '', fix: { kind: 'set-memory', mb: 6144 }, confidence: 'high' };
  const r = rank(db, sig, [other], [{ filename: 'iris-1.9.jar', enabled: true }]);
  assert.equal(r[0]!.fix?.kind, 'disable-mod');
  assert.deepEqual(r[0]!.fix, { kind: 'disable-mod', filename: 'iris-1.9.jar' });
  assert.deepEqual(r[0]!.learned, { ok: 1, fail: 0 });
  // The same crash again after the fix counts as "did not help".
  recordApplied(db, 'p1', sig, iris);
  recordOutcome(db, 'p1', 1, sig);
  assert.deepEqual(rank(db, sig, [iris])[0]!.learned, { ok: 1, fail: 1 });
  assert.deepEqual(parseDb(JSON.stringify(db)), db);
  assert.deepEqual(parseDb('{broken'), emptyDb());
});
