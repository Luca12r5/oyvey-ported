// Real end-to-end game start, used in CI (needs internet and a display, e.g.
// xvfb). Installs Minecraft with the launcher's own core – version JSON,
// libraries, assets, natives, Mojang Java, mod loader, mods – starts the game
// and waits until it reached the title screen, then closes it.
//
//   node test/real-launch.ts --version 1.21.11 --loader fabric --lego path/to/legoclient.jar
//   node test/real-launch.ts --version 1.8.9
//   node test/real-launch.ts --version 1.20.1 --loader forge
//   node test/real-launch.ts --version 1.21.11 --loader fabric --lego legoclient.jar --shots
//
// --shots: a flat world is generated with the official server first, the game starts straight
// into it (quick play), the LEGO client walks through its looks (-Dlegoclient.ci.shots=1) and
// the screenshots are printed to the log as small base64 JPEGs (LEGOSHOT lines).
//
// The test player has no Microsoft session (access token "0"): enough to reach
// the title screen and load all mods, not to join online servers. This is a
// test harness only; the launcher itself always requires a Microsoft login.

import { mkdtempSync, existsSync, copyFileSync, mkdirSync, readFileSync, writeFileSync, readdirSync, cpSync, statSync } from 'node:fs';
import { spawn, execFileSync } from 'node:child_process';
import { tmpdir, release } from 'node:os';
import { join, basename } from 'node:path';
import { randomUUID } from 'node:crypto';
import { currentEnv, buildArguments, redactArgs } from '../src/core/mojang.ts';
import { getManifest, installJava, installVersion, layout, resolveLoaderVersion, syncMods, vanillaVersion, type ModFile } from '../src/core/install.ts';
import { projectVersions, performanceFiles } from '../src/core/modrinth.ts';
import { createLogCleaner, startGame } from '../src/core/launch.ts';
import { SessionBridge } from '../src/core/sessionbridge.ts';
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
const shots = process.argv.includes('--shots');
// Real VR with Vivecraft's headset-less NullVR provider, in the flat world (needs --mods vivecraft).
const vrNull = process.argv.includes('--vr-null');
// Switches the LEGO color filter on in game (post effect shaders) and logs the result.
const filterTest = process.argv.includes('--filter-test');
// In-game session renewal through the launcher's bridge (LEGO Client SessionRefresher).
const sessionTest = process.argv.includes('--session-test');
// Extra Modrinth projects for this loader/version, e.g. --mods vivecraft,lithium
const extraMods = (arg('mods') ?? '').split(',').filter(Boolean);
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
for (const slug of extraMods) {
  const vs = await projectVersions(fetch, slug, gameVersion, loaderType ?? 'fabric');
  const v = vs.find((x) => x.version_type === 'release') ?? vs[0];
  const f = v?.files.find((x) => x.primary) ?? v?.files[0];
  if (!f) throw new Error(`no ${slug} for ${gameVersion}`);
  step(`Mod ${slug}: ${f.filename}`);
  mods.push({ url: f.url, filename: f.filename, sha512: f.hashes.sha512, size: f.size });
}
if (mods.length) await syncMods(fetch, join(gameDir, 'mods'), mods, report);
if (legoJar) {
  mkdirSync(join(gameDir, 'mods'), { recursive: true });
  copyFileSync(legoJar, join(gameDir, 'mods', basename(legoJar)));
}

// A flat test world made by the official server (no client-side world creation needed).
if (shots || vrNull) {
  const serverUrl = (base as unknown as { downloads?: { server?: { url: string } } }).downloads?.server?.url;
  if (!serverUrl) throw new Error('no server download for this version');
  const srvDir = join(root, 'server');
  mkdirSync(srvDir, { recursive: true });
  const jar = join(srvDir, 'server.jar');
  writeFileSync(jar, Buffer.from(await (await fetch(serverUrl)).arrayBuffer()));
  writeFileSync(join(srvDir, 'eula.txt'), 'eula=true\n');
  writeFileSync(join(srvDir, 'server.properties'), ['level-name=world', 'level-type=minecraft\\:flat', 'online-mode=false', 'generate-structures=false', 'spawn-protection=0', 'server-port=25599', 'spawn-monsters=false', ''].join('\n'));
  step('generating a flat world with the server');
  await new Promise<void>((resolve, reject) => {
    const srv = spawn(javaPath, ['-Xmx1G', '-jar', jar, '--nogui'], { cwd: srvDir, stdio: ['pipe', 'pipe', 'pipe'] });
    const timer = setTimeout(() => { srv.kill('SIGKILL'); reject(new Error('server did not finish in 180 s')); }, 180_000);
    const onData = (d: Buffer) => { if (/Done \(/.test(d.toString())) srv.stdin.write('stop\n'); };
    srv.stdout.on('data', onData);
    srv.stderr.on('data', onData);
    srv.on('exit', () => { clearTimeout(timer); resolve(); });
  });
  mkdirSync(join(gameDir, 'saves'), { recursive: true });
  // A fresh game dir shows the accessibility onboarding on first start, which keeps quick play from
  // opening the world. Skip it (and the other first-start hints) like a returning player.
  writeFileSync(join(gameDir, 'options.txt'), ['onboardAccessibility:false', 'skipMultiplayerWarning:true', 'joinedFirstServer:true', 'tutorialStep:none', 'pauseOnLostFocus:false', 'narrator:0',
    // Bindings like the ones old 26.3 builds saved: the client has to put its keys back once.
    ...(shots ? ['key_key.visor.toggle:key.keyboard.unknown', 'key_key.legoclient.gui:key.keyboard.f24'] : []), ''].join('\n'));
  cpSync(join(srvDir, 'world'), join(gameDir, 'saves', 'ci'), { recursive: true });
  step('flat world ready');
}

const token = 'lego-ci-no-session';
const renewed = 'lego-ci-renewed-session';
const playerUuid = randomUUID().replace(/-/g, '');
let sessionEnv: Record<string, string> | undefined;
let bridge: SessionBridge | null = null;
if (sessionTest) {
  bridge = new SessionBridge({
    session: async (uuid) => ({ name: 'LegoCI', uuid, accessToken: renewed, xuid: null, expiresAt: Date.now() + 86_400_000 }),
    accounts: () => [{ uuid: playerUuid, name: 'LegoCI' }],
    requestSwitch: () => {},
  });
  await bridge.start();
  sessionEnv = bridge.env(bridge.register(playerUuid));
}
const args = buildArguments({
  version: merged, env, javaPath, gameDir, assetsDir: l.assets, gameAssetsDir: installed.virtualAssets ?? undefined,
  librariesDir: l.libraries, nativesDir: installed.nativesDir, clientJar: installed.clientJar, classpathSeparator: process.platform === 'win32' ? ';' : ':',
  player: { name: 'LegoCI', uuid: playerUuid, accessToken: token, xuid: null, userType: 'msa' },
  launcherName: 'lego-launcher', launcherVersion: 'ci', memoryMb: { min: 1024, max: 3072 }, extraJvmArgs: [...(shots ? ['-Dlegoclient.ci.shots=1'] : []), ...(sessionTest ? ['-Dlegoclient.ci.session=1'] : []), ...(vrNull ? ['-Dlegoclient.ci.vr=1'] : []), ...(filterTest ? ['-Dlegoclient.ci.filter=1'] : [])],
  resolution: { width: 854, height: 480 }, loggingConfigPath: installed.loggingConfig ?? undefined,
});
if (shots || vrNull) args.push('--quickPlaySingleplayer', 'ci');
step(`Launch: ${redactArgs(args, token).slice(0, 6).join(' ')} … (${args.length} args)`);

// Markers that the client finished loading and shows the title screen.
const READY = [/Sound engine started/, /Created: \d+x\d+x\d+ minecraft:textures\/atlas\/blocks\.png-atlas/, /Created: \d+x\d+ textures-atlas/, /Reloading ResourceManager/];
const lines: string[] = [];
let ready = false, legoLoaded = false, modCount: string | null = null, noBackend = false, shotsDone = false, sessionRenewed = false, vrRunning = false, vrWorld = false;
const shotNames: string[] = [];
const clean = createLogCleaner();
let leaked = false;
const game = await startGame(javaPath, args, gameDir, join(root, 'logs'), (raw) => {
  const line = clean(raw);
  if (line === null) return;
  lines.push(line);
  if (lines.length > 4000) lines.splice(0, 1000);
  if (/\[LegoClient\] geladen/.test(line)) { legoLoaded = true; step(`LEGO: ${line.trim()}`); }
  if (line.includes(renewed)) { console.log('  ! renewed session token leaked into the log'); leaked = true; }
  if (/Sitzung erneuert \(uuid gleich\)|Sitzung/.test(line) && /\[LegoClient\]/.test(line)) step(line.trim());
  if (/Sitzung erneuert \(uuid gleich\)/.test(line)) sessionRenewed = true;
  const shot = /\[LegoClient\] CI shot ([a-z0-9-]+)$/.exec(line.trim());
  if (shot) { shotNames.push(shot[1]!); step(`shot ${shot[1]}`); }
  // The client asks for a real key press (X11 test events into the game window, like a keyboard).
  const press = /\[LegoClient\] CI press ([A-Za-z0-9_]+)$/.exec(line.trim());
  if (press) {
    const key = press[1]!;
    try {
      const win = execFileSync('xdotool', ['search', '--onlyvisible', '--name', 'Minecraft'], { encoding: 'utf8' }).trim().split('\n').pop();
      if (win) execFileSync('xdotool', ['windowactivate', '--sync', win], { stdio: 'ignore', timeout: 3000 });
    } catch { /* no window manager: the game window has the focus anyway */ }
    try {
      execFileSync('xdotool', ['keydown', key], { timeout: 3000 });
      setTimeout(() => { try { execFileSync('xdotool', ['keyup', key], { timeout: 3000 }); } catch (e) { console.log(`  ! xdotool keyup ${key}: ${e}`); } }, 150);
      step(`pressed ${key}`);
    } catch (e) { console.log(`  ! xdotool ${key}: ${e}`); }
  }
  if (/\[LegoClient\] CI (in world|shots done|waiting|key)/.test(line)) step(line.trim());
  if (/\[LegoClient\] CI shots done/.test(line)) shotsDone = true;
  if (/\[LegoClient\] CI VR/.test(line)) step(line.trim());
  if (/\[LegoClient\] CI filter/.test(line)) step(line.trim());
  if (/\[LegoClient\] (CI (visor|bauloo|custom txt|bench|cosmetic|txt)|Custom TxT|Visor|Werkbank|Tasten)/.test(line)) step(line.trim().slice(0, 400));
  if (/Bauloo Styles/.test(line) && /error|warn|fail/i.test(line)) console.log(`  ! bauloo: ${line.slice(0, 300)}`);
  if (filterTest && /post|shader|PostChain/i.test(line) && /error|fail|exception/i.test(line)) console.log(`  ! filter: ${line.slice(0, 300)}`);
  if (/\[LegoClient\] CI VR running/.test(line)) vrRunning = true;
  if (/\[LegoClient\] CI VR world ok/.test(line)) vrWorld = true;
  const m = /Loading (\d+) mods/.exec(line);
  if (m) { modCount = m[1]!; step(line.trim()); }
  if (/Exception|ERROR|FATAL|Mixin apply failed/.test(line) && !/Failed to (verify|fetch|load) (authentication|profile|user properties)|realms|telemetry|YggdrasilUserApi|Couldn't connect|AL lib|OpenAL|sound/i.test(line)) console.log(`  ! ${line.slice(0, 300)}`);
  // Minecraft 26.x tries OpenGL, then Vulkan; if neither works it shows a dialog and waits.
  if (/Failed to create backend Vulkan/.test(line) && lines.some((l) => /Failed to create backend OpenGL/.test(l))) noBackend = true;
  if (!ready && READY.some((r) => r.test(line))) { ready = true; step(`ready: ${line.trim().slice(0, 160)}`); }
}, sessionEnv);

const outcome = await new Promise<{ ok: boolean; why: string }>((resolve) => {
  const timer = setTimeout(() => resolve({ ok: false, why: `timeout after ${timeoutMs / 1000}s` }), timeoutMs);
  void game.exited.then((r) => {
    clearTimeout(timer);
    if (shots && shotsDone) { resolve({ ok: true, why: `world joined, ${shotNames.length} screenshots taken` }); return; }
    resolve({ ok: false, why: `game exited early with code ${r.code}${r.crashReport ? `, crash report ${r.crashReport}` : ''}` });
  });
  const poll = setInterval(() => {
    if (noBackend) { clearInterval(poll); clearTimeout(timer); resolve({ ok: false, why: 'no usable graphics backend (OpenGL and Vulkan both failed)' }); return; }
    if (vrNull) {
      if (!vrWorld) return;
      clearInterval(poll); clearTimeout(timer);
      resolve({ ok: true, why: 'Vivecraft VR (NullVR) running in a world for 30 s' });
      return;
    }
    if (!ready || shots) return;
    clearInterval(poll);
    // Stay alive a while on the title screen: catches render-time crashes of mods.
    // With --session-test also wait (up to 60 s) for the in-game session renewal.
    const t0 = Date.now();
    const settle = setInterval(() => {
      const waited = Date.now() - t0;
      if (waited < 25_000 || (sessionTest && !sessionRenewed && waited < 60_000)) return;
      clearInterval(settle);
      clearTimeout(timer);
      resolve({ ok: true, why: `title screen reached and stable for ${Math.round(waited / 1000)} s${sessionTest ? (sessionRenewed ? ', session renewed in game' : '') : ''}` });
    }, 500);
  }, 500);
});

if (!outcome.ok && /timeout/.test(outcome.why) && game.child.pid) {
  // Hung: ask the JVM for a thread dump (goes to the game's stdout) and print the render thread.
  const mark = lines.length;
  try { process.kill(game.child.pid, 'SIGQUIT'); } catch { /* gone */ }
  await new Promise((r) => setTimeout(r, 4000));
  const dump = lines.slice(mark);
  const at = dump.findIndex((l) => /"Render thread"/.test(l));
  console.log('--- thread dump: render thread ---');
  console.log((at >= 0 ? dump.slice(at, at + 60) : dump.slice(0, 80)).join('\n'));
}
game.child.kill('SIGTERM');
setTimeout(() => game.child.kill('SIGKILL'), 10_000).unref();
await game.exited.catch(() => {});

if (legoJar && !legoLoaded) Object.assign(outcome, { ok: false, why: `${outcome.why}; but "[LegoClient] geladen" never appeared` });
if (vrNull && !vrRunning) Object.assign(outcome, { ok: false, why: `${outcome.why}; VR never started` });
if (sessionTest && !sessionRenewed) Object.assign(outcome, { ok: false, why: `${outcome.why}; but the session was not renewed in game` });
if (leaked) Object.assign(outcome, { ok: false, why: `${outcome.why}; renewed token appeared in the log` });
await bridge?.stop();

// Screenshots → small JPEGs → base64 in the log (LEGOSHOT <name> <i>/<n> <data>).
if (shots) {
  const dir = join(gameDir, 'screenshots');
  const files = existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.png')).sort((a, b) => statSync(join(dir, a)).mtimeMs - statSync(join(dir, b)).mtimeMs) : [];
  step(`${files.length} screenshots`);
  files.forEach((f, k) => {
    const name = shotNames[k] ?? f;
    try {
      let b64: string;
      try {
        const jpg = join(dir, `${f}.jpg`);
        execFileSync('convert', [join(dir, f), '-resize', '640x', '-quality', '62', jpg]);
        b64 = readFileSync(jpg).toString('base64');
      } catch {
        // No ImageMagick on the runner: the PNG as it is (larger, still fine for a few pictures).
        b64 = readFileSync(join(dir, f)).toString('base64');
      }
      const parts = Math.ceil(b64.length / 8000);
      for (let i = 0; i < parts; i++) console.log(`LEGOSHOT ${name} ${i + 1}/${parts} ${b64.slice(i * 8000, (i + 1) * 8000)}`);
    } catch (e) {
      console.log(`LEGOSHOT ${name} failed: ${(e as Error).message}`);
    }
  });
  if (outcome.ok && files.length === 0) Object.assign(outcome, { ok: false, why: `${outcome.why}; but no screenshots were saved` });
}
console.log(`\nRESULT ${outcome.ok ? 'OK' : 'FAIL'}: Minecraft ${gameVersion}${loaderType ? ` ${loaderType}` : ''}${modCount ? ` (${modCount} mods)` : ''} – ${outcome.why} – ${((Date.now() - t0) / 1000).toFixed(0)}s total`);
if (!outcome.ok) {
  console.log('--- last log lines ---');
  console.log(lines.slice(-120).join('\n'));
  const crashDir = join(gameDir, 'crash-reports');
  if (existsSync(crashDir)) for (const f of (await import('node:fs')).readdirSync(crashDir)) console.log(`--- ${f} ---\n${readFileSync(join(crashDir, f), 'utf8').slice(0, 8000)}`);
  process.exit(1);
}
process.exit(0);
