import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { listContent, validContentName } from '../src/core/content.ts';

test('lists packs, worlds and screenshots of a game folder', async () => {
  const g = await mkdtemp(join(tmpdir(), 'lego-content-'));
  await mkdir(join(g, 'resourcepacks', 'Folder Pack'), { recursive: true });
  await writeFile(join(g, 'resourcepacks', 'Faithful.zip'), 'x');
  await writeFile(join(g, 'resourcepacks', 'Old.zip.disabled'), 'x');
  await writeFile(join(g, 'resourcepacks', 'notes.txt'), 'x');
  await mkdir(join(g, 'saves', 'Welt 1'), { recursive: true });
  await writeFile(join(g, 'saves', 'Welt 1', 'level.dat'), 'x');
  await mkdir(join(g, 'saves', 'kaputt'), { recursive: true });
  await mkdir(join(g, 'screenshots'), { recursive: true });
  await writeFile(join(g, 'screenshots', '2026-10-01.png'), 'x');
  const rp = await listContent(g, 'resourcepacks');
  assert.deepEqual(rp.map((x) => [x.title, x.enabled]), [['Faithful', true], ['Folder Pack', true], ['Old', false]]);
  assert.deepEqual((await listContent(g, 'saves')).map((x) => x.name), ['Welt 1']);
  assert.deepEqual((await listContent(g, 'screenshots')).map((x) => x.title), ['2026-10-01']);
  assert.deepEqual(await listContent(g, 'shaderpacks'), []);
});

test('content names cannot leave the folder', () => {
  for (const bad of ['', '..', '../x', 'a/b', 'a\\b', '.hidden', 5]) assert.equal(validContentName(bad), false);
  assert.equal(validContentName('Welt 1'), true);
});

test('installed project ids per content type survive leaving the page', async () => {
  const { installedProjectIds } = await import('../src/core/modrinth.ts');
  const g = await mkdtemp(join(tmpdir(), 'lego-ids-'));
  await mkdir(join(g, 'resourcepacks'), { recursive: true });
  await writeFile(join(g, 'resourcepacks', 'Faithful.zip'), 'x');
  await writeFile(join(g, 'resourcepacks', '.lego-mods.json'), JSON.stringify([
    { projectId: 'faithful', versionId: 'v', title: 'Faithful', filename: 'Faithful.zip', sha512: null, iconUrl: null, source: 'modrinth' },
    { projectId: 'gone', versionId: 'v', title: 'Gone', filename: 'Gone.zip', sha512: null, iconUrl: null, source: 'modrinth' },
  ]));
  assert.deepEqual(await installedProjectIds(g, 'resourcepack'), ['faithful']);
  assert.deepEqual(await installedProjectIds(g, 'shader'), []);
});
