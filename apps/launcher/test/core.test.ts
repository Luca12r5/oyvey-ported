import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { mkdtempSync, readFileSync, writeFileSync, existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { buildArguments, mavenPath, mergeVersions, redactArgs, resolveLibraries, ruleAllows, type Env, type VersionJson } from '../src/core/mojang.ts';
import { downloadAll, IntegrityError } from '../src/core/download.ts';
import { extract, nativeFilter } from '../src/core/zip.ts';
import { parseJvmArgs, validateProfile } from '../src/core/settings.ts';
import { diagnose } from '../src/core/launch.ts';
import { fakeFetch, storedZip } from './helpers.ts';

const win: Env = { os: 'windows', arch: 'x64', osVersion: '10.0', features: { has_custom_resolution: true } };
const linux: Env = { os: 'linux', arch: 'x64', osVersion: '6.1', features: {} };

test('rule evaluation follows the launcher semantics', () => {
  assert.equal(ruleAllows(undefined, win), true);
  assert.equal(ruleAllows([{ action: 'allow' }, { action: 'disallow', os: { name: 'osx' } }], win), true);
  assert.equal(ruleAllows([{ action: 'allow', os: { name: 'osx' } }], win), false);
  assert.equal(ruleAllows([{ action: 'allow', features: { is_demo_user: true } }], win), false);
  assert.equal(ruleAllows([{ action: 'allow', features: { has_custom_resolution: true } }], win), true);
  assert.equal(ruleAllows([{ action: 'allow', os: { name: 'windows', version: '^10\\.' } }], win), true);
});

test('maven paths', () => {
  assert.equal(mavenPath('net.fabricmc:fabric-loader:0.18.4'), 'net/fabricmc/fabric-loader/0.18.4/fabric-loader-0.18.4.jar');
  assert.equal(mavenPath('org.lwjgl:lwjgl:3.3.3:natives-windows'), 'org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-windows.jar');
});

const vanilla: VersionJson = {
  id: '1.21.11', type: 'release', mainClass: 'net.minecraft.client.main.Main',
  assetIndex: { id: '29', sha1: 'a', size: 1, url: 'https://x/29.json' },
  downloads: { client: { sha1: 'c', size: 1, url: 'https://x/client.jar' } },
  javaVersion: { component: 'java-runtime-delta', majorVersion: 21 },
  libraries: [
    { name: 'org.ow2.asm:asm:9.6', downloads: { artifact: { path: 'org/ow2/asm/asm/9.6/asm-9.6.jar', sha1: '1', size: 1, url: 'https://l/asm-9.6.jar' } } },
    { name: 'org.lwjgl:lwjgl:3.3.3:natives-windows', rules: [{ action: 'allow', os: { name: 'windows' } }], downloads: { artifact: { path: 'org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-windows.jar', sha1: '2', size: 1, url: 'https://l/n.jar' } } },
    { name: 'org.lwjgl:lwjgl:3.3.3:natives-linux', rules: [{ action: 'allow', os: { name: 'linux' } }], downloads: { artifact: { path: 'org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-linux.jar', sha1: '3', size: 1, url: 'https://l/l.jar' } } },
  ],
  arguments: {
    game: ['--username', '${auth_player_name}', '--version', '${version_name}', '--gameDir', '${game_directory}', '--accessToken', '${auth_access_token}',
      { rules: [{ action: 'allow', features: { is_demo_user: true } }], value: '--demo' },
      { rules: [{ action: 'allow', features: { has_custom_resolution: true } }], value: ['--width', '${resolution_width}', '--height', '${resolution_height}'] },
      { rules: [{ action: 'allow', features: { has_quick_plays_support: true } }], value: ['--quickPlayPath', '${quickPlayPath}'] }],
    jvm: [{ rules: [{ action: 'allow', os: { name: 'osx' } }], value: '-XstartOnFirstThread' }, '-Djava.library.path=${natives_directory}', '-cp', '${classpath}'],
  },
  logging: { client: { argument: '-Dlog4j.configurationFile=${path}', type: 'log4j2-xml', file: { id: 'client-1.21.2.xml', sha1: 'l', size: 1, url: 'https://x/log.xml' } } },
};

const fabric: VersionJson = {
  id: 'fabric-loader-0.18.4-1.21.11', inheritsFrom: '1.21.11', mainClass: 'net.fabricmc.loader.impl.launch.knot.KnotClient',
  libraries: [
    { name: 'net.fabricmc:fabric-loader:0.18.4', url: 'https://maven.fabricmc.net/', sha1: 'f' },
    { name: 'org.ow2.asm:asm:9.9', url: 'https://maven.fabricmc.net/', sha1: 'g' },
  ],
  arguments: { game: [], jvm: ['-DFabricMcEmu= net.minecraft.client.main.Main '] },
};

test('fabric profile merges over vanilla and overrides duplicate libraries', () => {
  const m = mergeVersions(vanilla, fabric);
  assert.equal(m.mainClass, fabric.mainClass);
  assert.equal(m.downloads?.client?.url, 'https://x/client.jar');
  const libs = resolveLibraries(m, win).map((l) => l.path);
  assert.ok(libs.includes('org/ow2/asm/asm/9.9/asm-9.9.jar'));
  assert.ok(!libs.includes('org/ow2/asm/asm/9.6/asm-9.6.jar'), 'older asm from vanilla is replaced');
  assert.ok(libs.some((l) => l.includes('natives-windows')));
  assert.ok(!libs.some((l) => l.includes('natives-linux')));
  const fl = resolveLibraries(m, win).find((l) => l.path.includes('fabric-loader'))!;
  assert.equal(fl.url, 'https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.18.4/fabric-loader-0.18.4.jar');
});

test('launch arguments are fully substituted and the token can be redacted', () => {
  const m = mergeVersions(vanilla, fabric);
  const args = buildArguments({
    version: m, env: win, javaPath: 'java', gameDir: 'C:/g', assetsDir: 'C:/a', librariesDir: 'C:/l', nativesDir: 'C:/n', clientJar: 'C:/v/1.21.11.jar',
    classpathSeparator: ';', player: { name: 'Steve', uuid: 'abc', accessToken: 'SECRET', xuid: null, userType: 'msa' },
    launcherName: 'lego', launcherVersion: '0.1', memoryMb: { min: 512, max: 4096 }, extraJvmArgs: ['-XX:+UseG1GC'], resolution: { width: 1920, height: 1080 },
    loggingConfigPath: 'C:/a/log_configs/client.xml',
  });
  assert.equal(args[0], '-Xms512M');
  assert.equal(args[1], '-Xmx4096M');
  assert.ok(args.includes('-XX:+UseG1GC'));
  assert.ok(!args.includes('-XstartOnFirstThread'));
  assert.ok(args.includes('-Dlog4j.configurationFile=C:/a/log_configs/client.xml'));
  const cp = args[args.indexOf('-cp') + 1]!;
  assert.ok(cp.endsWith('C:/v/1.21.11.jar'));
  assert.ok(cp.includes('fabric-loader-0.18.4.jar'));
  assert.ok(args.includes(fabric.mainClass));
  assert.deepEqual(args.slice(args.indexOf('--width'), args.indexOf('--width') + 4), ['--width', '1920', '--height', '1080']);
  assert.ok(!args.includes('--demo'));
  assert.ok(!args.some((a) => a.includes('${')), `unsubstituted: ${args.filter((a) => a.includes('${'))}`);
  assert.ok(!redactArgs(args, 'SECRET').some((a) => a.includes('SECRET')));
});

test('downloads verify hashes, resume partial files and refuse http', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'lego-dl-'));
  const good = Buffer.from('hello minecraft'.repeat(100));
  const sha1 = createHash('sha1').update(good).digest('hex');
  const f = fakeFetch({ 'https://cdn/a.jar': good, 'https://cdn/bad.jar': Buffer.from('tampered') });

  // Resume: a correct prefix already exists as .part.
  writeFileSync(join(dir, 'a.jar.part'), good.subarray(0, 100));
  const r = await downloadAll(f, [{ url: 'https://cdn/a.jar', dest: join(dir, 'a.jar'), sha1, size: good.length }]);
  assert.equal(r.downloaded, 1);
  assert.deepEqual(readFileSync(join(dir, 'a.jar')), good);
  // Second run skips the verified file.
  assert.equal((await downloadAll(f, [{ url: 'https://cdn/a.jar', dest: join(dir, 'a.jar'), sha1 }])).skipped, 1);

  await assert.rejects(downloadAll(f, [{ url: 'https://cdn/bad.jar', dest: join(dir, 'bad.jar'), sha1 }], { retries: 0 }), IntegrityError);
  assert.equal(existsSync(join(dir, 'bad.jar')), false, 'tampered file never lands at the destination');
  await assert.rejects(downloadAll(f, [{ url: 'http://cdn/a.jar', dest: join(dir, 'x.jar'), sha1 }], { retries: 0 }), IntegrityError);
});

test('native extraction keeps libraries only and blocks zip slip', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'lego-zip-'));
  const jar = join(dir, 'natives.jar');
  writeFileSync(jar, storedZip({ 'META-INF/MANIFEST.MF': 'x', 'windows/x64/org/lwjgl/lwjgl.dll': 'DLL', 'readme.txt': 'r' }));
  const out = join(dir, 'out');
  const names = await extract(jar, out, nativeFilter());
  assert.deepEqual(names, ['windows/x64/org/lwjgl/lwjgl.dll']);
  const evil = join(dir, 'evil.jar');
  writeFileSync(evil, storedZip({ '../../escape.dll': 'x' }));
  await assert.rejects(extract(evil, out, nativeFilter()), /path traversal/);
});

test('JVM arguments are restricted to safe flags', () => {
  assert.deepEqual(parseJvmArgs('-XX:+UseG1GC -Dfoo=bar').args, ['-XX:+UseG1GC', '-Dfoo=bar']);
  assert.deepEqual(parseJvmArgs('-Xmx99G').rejected, ['-Xmx99G']);
  assert.deepEqual(parseJvmArgs('-javaagent:evil.jar').rejected, ['-javaagent:evil.jar']);
  assert.deepEqual(parseJvmArgs('-Dx=$(calc)').rejected, ['-Dx=$(calc)']);
  assert.deepEqual(validateProfile({ name: 'A', gameVersion: '1.21.11', loader: null, memoryMb: 4096, jvmArgs: '', resolution: null }, 16384), []);
  assert.ok(validateProfile({ name: 'A', gameVersion: '1.21.11', loader: null, memoryMb: 32000, jvmArgs: '' }, 16384).length > 0);
});

test('crash diagnosis hints', () => {
  assert.match(diagnose('java.lang.OutOfMemoryError: Java heap space')!, /Arbeitsspeicher/);
  assert.equal(diagnose('all good'), null);
});
