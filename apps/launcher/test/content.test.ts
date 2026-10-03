import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { buildArguments, resolveLibraries, type Env, type VersionJson } from '../src/core/mojang.ts';
import { loaderSupports, loaderVersions, installerInfo } from '../src/core/loaders.ts';
import { installMrPack, installProject, packLoader, readPackIndex, removeMod, safeJoin, toggleMod, installedMods, MODRINTH } from '../src/core/modrinth.ts';
import { cfLoader, copyInstance, parseVersionId, readCfManifest, readMmcInstance, readCurseForgeInstance, readOfficialProfiles } from '../src/core/importers.ts';
import { migrateProfile, validateProfile } from '../src/core/settings.ts';
import { fakeFetch, storedZip } from './helpers.ts';

const win: Env = { os: 'windows', arch: 'x64', osVersion: '10.0', features: { has_custom_resolution: true } };
const sha512 = (b: Buffer | string) => createHash('sha512').update(b).digest('hex');
const sha1 = (b: Buffer | string) => createHash('sha1').update(b).digest('hex');
const tmp = (p: string) => mkdtempSync(join(tmpdir(), `lego-${p}-`));

// Shape of the official 1.8.9 version JSON (trimmed to what matters).
const v189: VersionJson = {
  id: '1.8.9', type: 'release', mainClass: 'net.minecraft.client.main.Main', assets: '1.8',
  minecraftArguments: '--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type}',
  assetIndex: { id: '1.8', sha1: 'x', size: 1, url: 'https://piston-meta.mojang.com/1.8.json' },
  javaVersion: { component: 'jre-legacy', majorVersion: 8 },
  libraries: [
    { name: 'org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209', downloads: { artifact: { path: 'org/lwjgl/lwjgl/lwjgl/2.9.4/lwjgl.jar', sha1: 'a', size: 1, url: 'https://libraries.minecraft.net/lwjgl.jar' } } },
    {
      name: 'org.lwjgl.lwjgl:lwjgl-platform:2.9.4-nightly-20150209',
      natives: { linux: 'natives-linux', osx: 'natives-osx', windows: 'natives-windows' },
      extract: { exclude: ['META-INF/'] },
      downloads: {
        classifiers: {
          'natives-windows': { path: 'org/lwjgl/lwjgl-platform-natives-windows.jar', sha1: 'b', size: 1, url: 'https://libraries.minecraft.net/lwjgl-win.jar' },
          'natives-linux': { path: 'org/lwjgl/lwjgl-platform-natives-linux.jar', sha1: 'c', size: 1, url: 'https://libraries.minecraft.net/lwjgl-linux.jar' },
        },
      },
    },
    {
      name: 'tv.twitch:twitch-platform:6.5', natives: { windows: 'natives-windows-${arch}' }, rules: [{ action: 'allow' }, { action: 'disallow', os: { name: 'linux' } }],
      downloads: { classifiers: { 'natives-windows-64': { path: 'tv/twitch/twitch-platform-natives-windows-64.jar', sha1: 'd', size: 1, url: 'https://libraries.minecraft.net/twitch64.jar' } } },
    },
  ],
  downloads: { client: { sha1: 'e', size: 1, url: 'https://piston-data.mojang.com/client.jar' } },
};

test('Minecraft 1.8.9: legacy natives and minecraftArguments', () => {
  const libs = resolveLibraries(v189, win);
  const natives = libs.filter((l) => l.native).map((l) => l.path);
  assert.deepEqual(natives, ['org/lwjgl/lwjgl-platform-natives-windows.jar', 'tv/twitch/twitch-platform-natives-windows-64.jar']);
  assert.deepEqual(libs.find((l) => l.native)!.excludes, ['META-INF/']);
  const args = buildArguments({
    version: v189, env: win, javaPath: 'java', gameDir: 'C:/g', assetsDir: 'C:/a', librariesDir: 'C:/l', nativesDir: 'C:/n', clientJar: 'C:/v/1.8.9.jar',
    classpathSeparator: ';', player: { name: 'Steve', uuid: 'u', accessToken: 'T', xuid: null, userType: 'msa' },
    launcherName: 'lego', launcherVersion: '1', memoryMb: { min: 512, max: 2048 }, extraJvmArgs: [],
  });
  assert.ok(args.includes('-Djava.library.path=C:/n'));
  assert.equal(args[args.indexOf('-cp') + 1]!.split(';').at(-1), 'C:/v/1.8.9.jar');
  assert.equal(args[args.indexOf('--assetIndex') + 1], '1.8');
  assert.equal(args[args.indexOf('--userProperties') + 1], '{}');
  assert.ok(args.indexOf('net.minecraft.client.main.Main') > args.indexOf('-cp'));
  assert.ok(!args.some((a) => a.includes('${')));
});

test('loader availability and version lists', async () => {
  assert.equal(loaderSupports('fabric', '1.8.9'), false);
  assert.equal(loaderSupports('fabric', '1.14.4'), true);
  assert.equal(loaderSupports('forge', '1.12.2'), false, 'pre-1.13 Forge uses a different installer format');
  assert.equal(loaderSupports('neoforge', '1.20.1'), false);
  assert.equal(loaderSupports('neoforge', '1.20.4'), true);
  assert.equal(loaderSupports('fabric', '25w14a'), true);
  assert.equal(loaderSupports('fabric', '26.3'), true);
  assert.equal(loaderSupports('neoforge', '26.1-snapshot-2'), true);
  assert.equal(loaderSupports('fabric', 'b1.7.3'), false);
  assert.equal(loaderSupports('neoforge', '1.19.4'), false);
  const f = fakeFetch({
    'https://maven.minecraftforge.net/releases/net/minecraftforge/forge/maven-metadata.json': { '1.20.1': ['1.20.1-47.0.0', '1.20.1-47.2.0'], '1.20.2': ['1.20.2-48.0.0'] },
    'https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge': { versions: ['20.4.10', '21.1.9', '21.1.77', '21.1.100-beta', '21.10.3', '26.3.0.5', '26.3.0.12'] },
    'https://meta.quiltmc.org/v3/versions/loader/1.21': [{ loader: { version: '0.26.0' } }, { loader: { version: '0.26.1-beta.1' } }],
  });
  assert.deepEqual(await loaderVersions(f, 'forge', '1.20.1'), [{ version: '47.2.0', stable: true }, { version: '47.0.0', stable: true }]);
  assert.deepEqual((await loaderVersions(f, 'neoforge', '1.21.1')).map((x) => x.version), ['21.1.100-beta', '21.1.77', '21.1.9']);
  assert.deepEqual((await loaderVersions(f, 'neoforge', '1.21.10')).map((x) => x.version), ['21.10.3']);
  assert.deepEqual((await loaderVersions(f, 'neoforge', '26.3')).map((x) => x.version), ['26.3.0.12', '26.3.0.5']);
  assert.deepEqual(await loaderVersions(f, 'quilt', '1.21'), [{ version: '0.26.0', stable: true }, { version: '0.26.1-beta.1', stable: false }]);
  assert.equal(installerInfo('forge', '1.20.1', '47.2.0').url, 'https://maven.minecraftforge.net/releases/net/minecraftforge/forge/1.20.1-47.2.0/forge-1.20.1-47.2.0-installer.jar');
  assert.equal(installerInfo('neoforge', '1.21.1', '21.1.77').versionId, 'neoforge-21.1.77');
});

test('version ids of other launchers are understood', () => {
  assert.deepEqual(parseVersionId('fabric-loader-0.16.10-1.21.4'), { gameVersion: '1.21.4', loaderType: 'fabric', loader: '0.16.10' });
  assert.deepEqual(parseVersionId('quilt-loader-0.26.0-1.20.1'), { gameVersion: '1.20.1', loaderType: 'quilt', loader: '0.26.0' });
  assert.deepEqual(parseVersionId('1.20.1-forge-47.2.0'), { gameVersion: '1.20.1', loaderType: 'forge', loader: '47.2.0' });
  assert.deepEqual(parseVersionId('1.12.2-forge1.12.2-14.23.5.2860'), { gameVersion: '1.12.2', loaderType: 'forge', loader: '14.23.5.2860' });
  assert.deepEqual(parseVersionId('neoforge-21.1.77'), { gameVersion: '1.21.1', loaderType: 'neoforge', loader: '21.1.77' });
  assert.deepEqual(parseVersionId('neoforge-20.4.10'), { gameVersion: '1.20.4', loaderType: 'neoforge', loader: '20.4.10' });
  assert.deepEqual(parseVersionId('26.3'), { gameVersion: '26.3', loaderType: null, loader: null });
  assert.deepEqual(parseVersionId('1.8.9'), { gameVersion: '1.8.9', loaderType: null, loader: null });
  assert.deepEqual(parseVersionId('OptiFine-something'), { gameVersion: null, loaderType: null, loader: null });
});

test('profiles: migration and LEGO client restriction', () => {
  const old = migrateProfile({ id: 'x', name: 'Alt', gameVersion: '1.21.11', loader: '0.16.0', legoClient: true } as never);
  assert.equal(old.loaderType, 'fabric');
  assert.equal(old.legoClient, true);
  assert.equal(migrateProfile({ id: 'y', name: 'V', gameVersion: '1.8.9', loader: null, legoClient: false } as never).loaderType, null);
  const p = { ...old, gameVersion: '1.20.1' };
  assert.ok(validateProfile(p, 16384).some((e) => e.includes('LEGO Client')));
  assert.deepEqual(validateProfile({ ...old, legoClient: false, gameVersion: '1.8.9', loaderType: null }, 16384), []);
  assert.ok(validateProfile({ ...old, loaderType: 'rift' as never }, 16384).some((e) => e.includes('Mod-Loader')));
});

test('mrpack: loader detection, path safety, allowed hosts, override precedence', async () => {
  assert.deepEqual(packLoader({ minecraft: '1.20.1', 'fabric-loader': '0.15.0' }), { gameVersion: '1.20.1', loaderType: 'fabric', loader: '0.15.0' });
  assert.deepEqual(packLoader({ minecraft: '1.21.1', neoforge: '21.1.77' }), { gameVersion: '1.21.1', loaderType: 'neoforge', loader: '21.1.77' });
  const root = tmp('pack');
  assert.throws(() => safeJoin(root, '../evil.jar'), /Unsicherer Pfad/);
  assert.throws(() => safeJoin(root, '/etc/passwd'.replace(/^\//, '../../')), /Unsicherer Pfad/);
  assert.ok(safeJoin(root, 'mods/a.jar').startsWith(root));

  const jar = Buffer.from('mod-bytes');
  const index = {
    formatVersion: 1, game: 'minecraft', versionId: '1', name: 'Test Pack', dependencies: { minecraft: '1.20.1', 'fabric-loader': '0.15.0' },
    files: [
      { path: 'mods/a.jar', hashes: { sha1: sha1(jar), sha512: sha512(jar) }, downloads: ['https://cdn.modrinth.com/data/a.jar'], fileSize: jar.length },
      { path: 'mods/server-only.jar', hashes: { sha1: 'x' }, env: { client: 'unsupported' }, downloads: ['https://cdn.modrinth.com/s.jar'] },
    ],
  };
  const zip = storedZip({
    'modrinth.index.json': JSON.stringify(index),
    'client-overrides/config/x.json': 'client',
    'overrides/config/x.json': 'common',
    'overrides/options.txt': 'fov:90',
  });
  assert.equal(readPackIndex(zip).name, 'Test Pack');
  const game = join(root, 'game');
  const r = await installMrPack(fakeFetch({ 'https://cdn.modrinth.com/data/a.jar': jar }), zip, game);
  assert.equal(r.mods, 1);
  assert.deepEqual(readFileSync(join(game, 'mods', 'a.jar')), jar);
  assert.equal(existsSync(join(game, 'mods', 'server-only.jar')), false);
  assert.equal(readFileSync(join(game, 'config', 'x.json'), 'utf8'), 'client');
  assert.equal(readFileSync(join(game, 'options.txt'), 'utf8'), 'fov:90');

  const evilHost = storedZip({ 'modrinth.index.json': JSON.stringify({ ...index, files: [{ ...index.files[0], downloads: ['https://evil.example/a.jar'] }] }) });
  await assert.rejects(installMrPack(fakeFetch({}), evilHost, join(root, 'g2')), /keine erlaubte Download-Quelle/);
  const evilPath = storedZip({ 'modrinth.index.json': JSON.stringify({ ...index, files: [{ ...index.files[0], path: '../../x.jar' }] }) });
  await assert.rejects(installMrPack(fakeFetch({}), evilPath, join(root, 'g3')), /Unsicherer Pfad/);
  const evilOverride = storedZip({ 'modrinth.index.json': JSON.stringify({ ...index, files: [] }), 'overrides/../../x.txt': 'x' });
  await assert.rejects(installMrPack(fakeFetch({}), evilOverride, join(root, 'g4')), /Unsicherer Pfad/);
});

test('Modrinth install resolves required dependencies; toggle and remove', async () => {
  const game = tmp('mods');
  const a = Buffer.from('A'), dep = Buffer.from('DEP');
  const ver = (id: string, project: string, file: string, data: Buffer, deps: object[] = []) => ({
    id, project_id: project, name: `${project} ${id}`, version_number: '1', version_type: 'release', loaders: ['fabric'], game_versions: ['1.21.1'],
    files: [{ url: `https://cdn.modrinth.com/${file}`, filename: file, primary: true, size: data.length, hashes: { sha1: sha1(data), sha512: sha512(data) } }],
    dependencies: deps,
  });
  const q = `game_versions=${encodeURIComponent('["1.21.1"]')}&loaders=${encodeURIComponent('["fabric"]')}`;
  const f = fakeFetch({
    [`${MODRINTH}/project/AAA/version?${q}`]: [ver('v1', 'AAA', 'a.jar', a, [{ project_id: 'DEP', version_id: null, dependency_type: 'required' }, { project_id: 'OPT', version_id: null, dependency_type: 'optional' }])],
    [`${MODRINTH}/project/DEP/version?${q}`]: [ver('v2', 'DEP', 'dep.jar', dep)],
    'https://cdn.modrinth.com/a.jar': a,
    'https://cdn.modrinth.com/dep.jar': dep,
  });
  const titles = await installProject(f, game, 'AAA', '1.21.1', 'fabric', 'mod', { title: 'Mod A' });
  assert.deepEqual(titles, ['Mod A', 'DEP v2']);
  assert.ok(existsSync(join(game, 'mods', 'dep.jar')));
  assert.deepEqual((await installedMods(game)).map((m) => m.projectId), ['AAA', 'DEP']);
  assert.equal(await toggleMod(game, 'a.jar', false), 'a.jar.disabled');
  assert.ok(existsSync(join(game, 'mods', 'a.jar.disabled')));
  await removeMod(game, 'a.jar.disabled');
  assert.deepEqual((await installedMods(game)).map((m) => m.projectId), ['DEP']);
  await assert.rejects(removeMod(game, '../x.jar'), /invalid file name/);
});

test('imports: Prism/MultiMC, CurseForge app, official launcher, copy without worlds', async () => {
  const root = tmp('import');
  const inst = join(root, 'MyPack');
  mkdirSync(join(inst, '.minecraft', 'mods'), { recursive: true });
  mkdirSync(join(inst, '.minecraft', 'config'), { recursive: true });
  mkdirSync(join(inst, '.minecraft', 'saves', 'World'), { recursive: true });
  writeFileSync(join(inst, 'instance.cfg'), 'InstanceType=OneSix\nname=Mein Pack\n');
  writeFileSync(join(inst, 'mmc-pack.json'), JSON.stringify({ components: [{ uid: 'net.minecraft', version: '1.20.1' }, { uid: 'net.fabricmc.fabric-loader', version: '0.15.11' }] }));
  writeFileSync(join(inst, '.minecraft', 'mods', 'sodium.jar'), 'x');
  writeFileSync(join(inst, '.minecraft', 'config', 'sodium.json'), '{}');
  writeFileSync(join(inst, '.minecraft', 'options.txt'), 'fov:80');
  const found = (await readMmcInstance(inst, 'prism'))!;
  assert.deepEqual({ ...found, gameDir: '' }, { source: 'prism', name: 'Mein Pack', gameDir: '', gameVersion: '1.20.1', loaderType: 'fabric', loader: '0.15.11' });
  const target = join(root, 'target');
  const copied = await copyInstance(found.gameDir, target, false);
  assert.deepEqual(copied.sort(), ['config', 'mods', 'options.txt']);
  assert.equal(existsSync(join(target, 'saves')), false);

  const cf = join(root, 'cf');
  mkdirSync(cf);
  writeFileSync(join(cf, 'minecraftinstance.json'), JSON.stringify({ name: 'ATM', gameVersion: '1.20.1', baseModLoader: { name: 'forge-47.2.0', minecraftVersion: '1.20.1' } }));
  assert.deepEqual({ ...(await readCurseForgeInstance(cf))!, gameDir: '' }, { source: 'curseforge', name: 'ATM', gameDir: '', gameVersion: '1.20.1', loaderType: 'forge', loader: '47.2.0' });

  const mc = join(root, '.minecraft');
  mkdirSync(mc);
  writeFileSync(join(mc, 'launcher_profiles.json'), JSON.stringify({ profiles: { a: { name: 'Fabric', lastVersionId: 'fabric-loader-0.16.0-1.21' }, b: { lastVersionId: 'latest-release' } } }));
  const off = await readOfficialProfiles(mc);
  assert.equal(off.length, 1);
  assert.equal(off[0]!.loaderType, 'fabric');

  const manifest = { manifestType: 'minecraftModpack', name: 'CF Pack', minecraft: { version: '1.20.1', modLoaders: [{ id: 'forge-47.2.0', primary: true }] }, files: [{ projectID: 1, fileID: 2, required: true }], overrides: 'overrides' };
  const zip = storedZip({ 'manifest.json': JSON.stringify(manifest) });
  assert.deepEqual(cfLoader(readCfManifest(zip)), { gameVersion: '1.20.1', loaderType: 'forge', loader: '47.2.0' });
});

test('log4j XML output becomes readable lines', async () => {
  const { createLogCleaner } = await import('../src/core/launch.ts');
  const c = createLogCleaner();
  const out = [
    '<log4j:Event logger="net.minecraft.client.Minecraft" timestamp="1791064360861" level="INFO" thread="Render thread">',
    '  <log4j:Message><![CDATA[Setting user: LegoCI]]></log4j:Message>',
    '</log4j:Event>',
    '<log4j:Event logger="x" timestamp="1791064360900" level="ERROR" thread="main">',
    '  <log4j:Throwable><![CDATA[java.lang.RuntimeException: boom',
    '\tat a.b(C.java:1)',
    ']]></log4j:Throwable>',
    '</log4j:Event>',
    'plain stdout line',
  ].map(c).filter((x) => x !== null);
  assert.equal(out.length, 3);
  assert.match(out[0]!, /^\[\d\d:\d\d:\d\d\] \[Render thread\/INFO\]: Setting user: LegoCI$/);
  assert.match(out[1]!, /\[main\/ERROR\]: java.lang.RuntimeException: boom\n\tat a.b/);
  assert.equal(out[2], 'plain stdout line');
});
