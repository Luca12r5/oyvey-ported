// Real end-to-end game start, used in CI (needs internet and a display, e.g.
// xvfb). Installs Minecraft with the launcher's own core – version JSON,
// libraries, assets, natives, Mojang Java, mod loader, mods – starts the game
// and waits until it reached the title screen, then closes it.
//
//   node test/real-launch.ts --version 1.21.11 --loader fabric --lego path/to/legoclient.jar
//   node test/real-launch.ts --version 1.8.9
//   node test/real-launch.ts --version 1.20.1 --loader forge
//
// The test player has no Microsoft session (access token "0"): enough to reach
// the title screen and load all mods, not to join online servers. This is a
// test harness only; the launcher itself always requires a Microsoft login.

import { mkdtempSync, existsSync, copyFileSync, mkdirSync, readFileSync } from 'node:fs';
import { tmpdir, release } from 'node:os';
import { join, basename } from 'node:path';
import { randomUUID } from 'node:crypto';
import { currentEnv, buildArguments, redactArgs } from '../src/core/mojang.ts';
import { getManifest, installJava, installVersion, layout, resolveLoaderVersion, syncMods, vanillaVersion, type ModFile } from '../src/core/install.ts';
import { projectVersions, performanceFiles } from '../src/core/modrinth.ts';
import { startGame } from '../src/core/launch.ts';
import type { LoaderType } from '../src/core/settings.ts';

const arg = (name: string, d: string | null = null) => {
  const i = process.argv.indexOf(`--${name}`);
  return i > 0 ? process.argv[i + 1]! : d;
};
let gameVersion = arg('version', '1.21.11')!;
if (gameVersion === 'latest') gameVersion = (await getManifest(fetch)).latest.release;
const loaderType = arg('loader') as LoaderType | null;
const legoJar = arg('lego');
const fps = process.argv.includes('--fps');
const timeoutMs = Number(arg('timeout', '600')) * 1000;
const root = arg('dir') ?? mkdtempSync(join(tmpdir(), 'lego-real-'));

const env = currentEnv(process.platform, process.arch, release());
const l = layout(root);
const gameDir = join(l.instances, 'ci');
const t0 = Date.now();
const step = (s: string) => console.log(`[${((Date.now() - t0) / 1000).toFixed(1)}s] ${s}`);
let lastPct = -10;
const report = (s: string, p?: { done: number; total: number }) => {
  if (!p) return step(s);
  const pct = Math.floor((p.done / Math.max(1, p.total)) * 100);
  if (pct >= lastPct + 25 || p.done === p.total) { lastPct = pct === 100 ? -10 : pct; step(`${s} ${p.done}/${p.total}`); }
};

step(`Minecraft ${gameVersion}${loaderType ? ` + ${loaderType}` : ''}${legoJar ? ' + LEGO Client' : ''} in ${root}`);
const base = await vanillaVersion(fetch, l, gameVersion);
const javaPath = await installJava(fetch, l, base.javaVersion?.component ?? 'jre-legacy', env, report);
step(`Java: ${javaPath}`);
if (loaderType === 'forge' || loaderType === 'neoforge') await installVersion(fetch, l, base, base.id, env, report, undefined, gameDir);
const merged = await resolveLoaderVersion(fetch, l, base, loaderType, null, javaPath, (line) => console.log(`  [installer] ${line}`));
step(`Version: ${merged.id}, main class ${merged.mainClass}, ${merged.libraries.length} libraries`);
const installed = await installVersion(fetch, l, merged, base.id, env, report, undefined, gameDir);

const mods: ModFile[] = [];
if (legoJar) {
  const api = await projectVersions(fetch, 'fabric-api', gameVersion, 'fabric');
  const v = api.find((x) => x.version_type === 'release') ?? api[0];
  const f = v?.files.find((x) => x.primary) ?? v?.files[0];
  if (!f) throw new Error('no Fabric API');
  mods.push({ url: f.url, filename: f.filename, sha512: f.hashes.sha512, size: f.size });
}
if (fps && loaderType) for (const p of await performanceFiles(fetch, gameVersion, loaderType)) mods.push({ url: p.url, filename: p.filename, sha512: p.sha512, size: p.size });
if (mods.length) await syncMods(fetch, join(gameDir, 'mods'), mods, report);
if (legoJar) {
  mkdirSync(join(gameDir, 'mods'), { recursive: true });
  copyFileSync(legoJar, join(gameDir, 'mods', basename(legoJar)));
}

const token = '0';
const args = buildArguments({
  version: merged, env, javaPath, gameDir, assetsDir: l.assets, gameAssetsDir: installed.virtualAssets ?? undefined,
  librariesDir: l.libraries, nativesDir: installed.nativesDir, clientJar: installed.clientJar, classpathSeparator: process.platform === 'win32' ? ';' : ':',
  player: { name: 'LegoCI', uuid: randomUUID().replace(/-/g, ''), accessToken: token, xuid: null, userType: 'msa' },
  launcherName: 'lego-launcher', launcherVersion: 'ci', memoryMb: { min: 1024, max: 3072 }, extraJvmArgs: [],
  resolution: { width: 854, height: 480 }, loggingConfigPath: installed.loggingConfig ?? undefined,
});
step(`Launch: ${redactArgs(args, token).slice(0, 6).join(' ')} … (${args.length} args)`);

// Markers that the client finished loading and shows the title screen.
const READY = [/Sound engine started/, /Created: \d+x\d+x\d+ minecraft:textures\/atlas\/blocks\.png-atlas/, /Created: \d+x\d+ textures-atlas/, /Reloading ResourceManager/];
const lines: string[] = [];
let ready = false, legoLoaded = false, modCount: string | null = null;
const game = await startGame(javaPath, args, gameDir, join(root, 'logs'), (line) => {
  lines.push(line);
  if (lines.length > 4000) lines.splice(0, 1000);
  if (/\[LegoClient\] geladen/.test(line)) { legoLoaded = true; step(`LEGO: ${line.trim()}`); }
  const m = /Loading (\d+) mods/.exec(line);
  if (m) { modCount = m[1]!; step(line.trim()); }
  if (/Exception|ERROR|FATAL|Mixin apply failed/.test(line) && !/Failed to (verify|fetch|load) (authentication|profile|user properties)|realms|telemetry|YggdrasilUserApi|Couldn't connect|AL lib|OpenAL|sound/i.test(line)) console.log(`  ! ${line.slice(0, 300)}`);
  if (!ready && READY.some((r) => r.test(line))) { ready = true; step(`ready: ${line.trim().slice(0, 160)}`); }
});

const outcome = await new Promise<{ ok: boolean; why: string }>((resolve) => {
  const timer = setTimeout(() => resolve({ ok: false, why: `timeout after ${timeoutMs / 1000}s` }), timeoutMs);
  void game.exited.then((r) => { clearTimeout(timer); resolve({ ok: false, why: `game exited early with code ${r.code}${r.crashReport ? `, crash report ${r.crashReport}` : ''}` }); });
  const poll = setInterval(() => {
    if (!ready) return;
    clearInterval(poll);
    // Stay alive a while on the title screen: catches render-time crashes of mods.
    setTimeout(() => { clearTimeout(timer); resolve({ ok: true, why: 'title screen reached and stable for 25 s' }); }, 25_000);
  }, 500);
});

game.child.kill('SIGTERM');
setTimeout(() => game.child.kill('SIGKILL'), 10_000).unref();
await game.exited.catch(() => {});

if (legoJar && !legoLoaded) Object.assign(outcome, { ok: false, why: `${outcome.why}; but "[LegoClient] geladen" never appeared` });
console.log(`\nRESULT ${outcome.ok ? 'OK' : 'FAIL'}: Minecraft ${gameVersion}${loaderType ? ` ${loaderType}` : ''}${modCount ? ` (${modCount} mods)` : ''} – ${outcome.why} – ${((Date.now() - t0) / 1000).toFixed(0)}s total`);
if (!outcome.ok) {
  console.log('--- last log lines ---');
  console.log(lines.slice(-120).join('\n'));
  const crashDir = join(gameDir, 'crash-reports');
  if (existsSync(crashDir)) for (const f of (await import('node:fs')).readdirSync(crashDir)) console.log(`--- ${f} ---\n${readFileSync(join(crashDir, f), 'utf8').slice(0, 8000)}`);
  process.exit(1);
}
process.exit(0);
