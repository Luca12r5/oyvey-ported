import { test } from 'node:test';
import assert from 'node:assert/strict';
import { groupVersions, versionGroup } from '../src/core/versions.ts';

const V = (id: string, type: string, t: string) => ({ id, type, releaseTime: t });
const list = [
  V('26.3', 'release', '2026-09-01'), V('26.2', 'release', '2026-06-01'), V('26w14a', 'snapshot', '2026-04-01'), V('26.1', 'release', '2026-03-01'),
  V('1.21.11', 'release', '2025-12-01'), V('1.21.10', 'release', '2025-10-01'), V('1.8.9', 'release', '2015-12-01'), V('1.7.10', 'release', '2014-06-01'), V('b1.7.3', 'old_beta', '2011-07-01'),
];

test('groups', () => {
  assert.equal(versionGroup(list[0]!), '26.x');
  assert.equal(versionGroup(list[4]!), '1.21.x');
  assert.equal(versionGroup(list[6]!), '1.8.x');
  assert.equal(versionGroup(list[7]!), 'Vor 1.8');
  assert.equal(versionGroup(list[2]!), 'Snapshots');
});

test('default: every release from 1.8 to 26.x, newest first', () => {
  const g = groupVersions(list, { snapshots: false, historical: false });
  assert.deepEqual(g.map((x) => x.label), ['26.x', '1.21.x', '1.8.x']);
  assert.deepEqual(g[0]!.versions.map((v) => v.id), ['26.3', '26.2', '26.1']);
  const all = groupVersions(list, { snapshots: true, historical: true });
  assert.ok(all.some((x) => x.label === 'Snapshots') && all.some((x) => x.label === 'Vor 1.8'));
});
