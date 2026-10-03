// Installs everything needed to play: vanilla client, libraries, assets, the
// Mojang Java runtime, the Fabric loader profile, Fabric API and the LEGO
// client mod. Every download is hash-verified (see download.ts).

import { mkdir, readFile, readdir, rm, writeFile, chmod, stat } from 'node:fs/promises';
import { join } from 'node:path';
import { downloadAll, type DownloadTask, type Progress } from './download.ts';
import { fetchJson, type FetchLike } from './net.ts';
import {
  mergeVersions, resolveLibraries, RESOURCES, VERSION_MANIFEST, mavenPath,
  type Env, type Manifest, type VersionJson,
} from './mojang.ts';
import { extract, nativeFilter } from './zip.ts';

export const JAVA_RUNTIME_INDEX = 'https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json';
export const FABRIC_META = 'https://meta.fabricmc.net/v2';
export const MODRINTH = 'https://api.modrinth.com/v2';

export interface Layout {
  root: string;
  versions: string;
  libraries: string;
  assets: string;
  runtime: string;
  instances: string;
}

export function layout(root: string): Layout {
  return {
    root,
    versions: join(root, 'versions'),
    libraries: join(root, 'libraries'),
    assets: join(root, 'assets'),
    runtime: join(root, 'runtime'),
    instances: join(root, 'instances'),
  };
}

export type StepReporter = (step: string, p?: Progress) => void;

async function readJsonFile<T>(path: string): Promise<T | null> {
  try {
    return JSON.parse(await readFile(path, 'utf8')) as T;
  } catch {
    return null;
  }
}

export async function getManifest(f: FetchLike): Promise<Manifest> {
  return fetchJson<Manifest>(f, VERSION_MANIFEST);
}

/** Downloads (or reuses) a vanilla version JSON, verified by the manifest sha1. */
export async function vanillaVersion(f: FetchLike, l: Layout, id: string): Promise<VersionJson> {
  const file = join(l.versions, id, `${id}.json`);
  const manifest = await getManifest(f);
  const entry = manifest.versions.find((v) => v.id === id);
  if (!entry) throw new Error(`Minecraft ${id} is not in the version manifest`);
  await downloadAll(f, [{ url: entry.url, dest: file, sha1: entry.sha1, label: `${id}.json` }]);
  return (await readJsonFile<VersionJson>(file))!;
}

export interface FabricLoader { version: string; stable: boolean }

export async function fabricLoaders(f: FetchLike, gameVersion: string): Promise<FabricLoader[]> {
  const list = await fetchJson<{ loader: FabricLoader }[]>(f, `${FABRIC_META}/versions/loader/${encodeURIComponent(gameVersion)}`);
  return list.map((x) => x.loader);
}

/** Fabric profile JSON (inheritsFrom vanilla). Cached under versions/. */
export async function fabricProfile(f: FetchLike, l: Layout, gameVersion: string, loader: string): Promise<VersionJson> {
  const id = `fabric-loader-${loader}-${gameVersion}`;
  const file = join(l.versions, id, `${id}.json`);
  const cached = await readJsonFile<VersionJson>(file);
  if (cached) return cached;
  const p = await fetchJson<VersionJson>(f, `${FABRIC_META}/versions/loader/${encodeURIComponent(gameVersion)}/${encodeURIComponent(loader)}/profile/json`);
  await mkdir(join(l.versions, id), { recursive: true });
  await writeFile(file, JSON.stringify(p, null, 2));
  return p;
}

/**
 * Fabric meta lists maven coordinates; newer responses include sha1/size.
 * Where a hash is missing we fetch the maven ".sha1" companion file so the
 * jar is still verified.
 */
async function fillMavenHashes(f: FetchLike, v: VersionJson): Promise<void> {
  for (const lib of v.libraries) {
    if (lib.downloads || lib.sha1 || lib.url === undefined) continue;
    const url = `${lib.url.replace(/\/?$/, '/')}${mavenPath(lib.name)}.sha1`;
    const res = await f(url, { signal: AbortSignal.timeout(15_000) });
    if (res.ok) {
      const text = (await res.text()).trim().split(/\s+/)[0] ?? '';
      if (/^[0-9a-f]{40}$/i.test(text)) lib.sha1 = text.toLowerCase();
    }
    if (!lib.sha1) throw new Error(`No checksum available for ${lib.name}`);
  }
}

interface AssetIndex { objects: Record<string, { hash: string; size: number }> }

/** `clientId` is the vanilla version id that owns the client jar (the merged Fabric profile has its own id). */
export async function installVersion(f: FetchLike, l: Layout, version: VersionJson, clientId: string, env: Env, report: StepReporter, signal?: AbortSignal): Promise<{ clientJar: string; nativesDir: string; loggingConfig: string | null }> {
  const baseId = clientId;
  const clientJar = join(l.versions, baseId, `${baseId}.jar`);
  const tasks: DownloadTask[] = [];
  if (!version.downloads?.client) throw new Error('version has no client download');
  tasks.push({ url: version.downloads.client.url, dest: clientJar, sha1: version.downloads.client.sha1, size: version.downloads.client.size, label: `${baseId}.jar` });
  const libs = resolveLibraries(version, env);
  for (const lib of libs) tasks.push({ url: lib.url, dest: join(l.libraries, lib.path), sha1: lib.sha1, size: lib.size, label: lib.path.split('/').pop() });

  let loggingConfig: string | null = null;
  if (version.logging?.client) {
    const lf = version.logging.client.file;
    loggingConfig = join(l.assets, 'log_configs', lf.id);
    tasks.push({ url: lf.url, dest: loggingConfig, sha1: lf.sha1, size: lf.size, label: lf.id });
  }

  report('Bibliotheken');
  await downloadAll(f, tasks, { onProgress: (p) => report('Bibliotheken', p), signal });

  if (version.assetIndex) {
    const ai = version.assetIndex;
    const idxFile = join(l.assets, 'indexes', `${ai.id}.json`);
    await downloadAll(f, [{ url: ai.url, dest: idxFile, sha1: ai.sha1, size: ai.size, label: `assets ${ai.id}` }], { signal });
    const index = (await readJsonFile<AssetIndex>(idxFile))!;
    const assetTasks = Object.values(index.objects).map((o) => ({
      url: `${RESOURCES}/${o.hash.slice(0, 2)}/${o.hash}`, dest: join(l.assets, 'objects', o.hash.slice(0, 2), o.hash), sha1: o.hash, size: o.size,
    }));
    report('Assets');
    await downloadAll(f, assetTasks, { concurrency: 16, onProgress: (p) => report('Assets', p), signal });
  }

  // Extract native libraries into a per-version directory.
  const nativesDir = join(l.versions, version.id, 'natives');
  await rm(nativesDir, { recursive: true, force: true });
  await mkdir(nativesDir, { recursive: true });
  for (const lib of libs.filter((x) => x.native)) {
    await extract(join(l.libraries, lib.path), nativesDir, nativeFilter(lib.excludes));
  }
  return { clientJar, nativesDir, loggingConfig };
}

// ---- Java runtime --------------------------------------------------------

type RuntimeIndex = Record<string, Record<string, { manifest: { sha1: string; size: number; url: string }; version: { name: string } }[]>>;
interface RuntimeManifest {
  files: Record<string, { type: 'file' | 'directory' | 'link'; executable?: boolean; target?: string; downloads?: { raw: { sha1: string; size: number; url: string } } }>;
}

export function runtimePlatform(env: Env): string {
  if (env.os === 'windows') return env.arch === 'arm64' ? 'windows-arm64' : env.arch === 'x86' ? 'windows-x86' : 'windows-x64';
  if (env.os === 'osx') return env.arch === 'arm64' ? 'mac-os-arm64' : 'mac-os';
  return env.arch === 'x86' ? 'linux-i386' : 'linux';
}

/** Installs Mojang's Java runtime for the version's `javaVersion.component`. Returns the java executable. */
export async function installJava(f: FetchLike, l: Layout, component: string, env: Env, report: StepReporter, signal?: AbortSignal): Promise<string> {
  const platform = runtimePlatform(env);
  const dir = join(l.runtime, component, platform);
  const exe = join(dir, env.os === 'windows' ? 'bin/javaw.exe' : env.os === 'osx' ? 'jre.bundle/Contents/Home/bin/java' : 'bin/java');
  const index = await fetchJson<RuntimeIndex>(f, JAVA_RUNTIME_INDEX);
  const entry = index[platform]?.[component]?.[0];
  if (!entry) throw new Error(`Mojang provides no Java runtime "${component}" for ${platform}`);
  const marker = join(dir, '.lego-runtime');
  const installed = await readFile(marker, 'utf8').catch(() => '');
  if (installed.trim() === entry.manifest.sha1) {
    try {
      await stat(exe);
      return exe;
    } catch { /* reinstall */ }
  }
  const manifestFile = join(l.runtime, `${component}-${platform}.json`);
  await downloadAll(f, [{ url: entry.manifest.url, dest: manifestFile, sha1: entry.manifest.sha1, size: entry.manifest.size }], { signal });
  const m = (await readJsonFile<RuntimeManifest>(manifestFile))!;
  const tasks: DownloadTask[] = [];
  for (const [rel, file] of Object.entries(m.files)) {
    if (rel.includes('..')) throw new Error(`invalid runtime path ${rel}`);
    if (file.type === 'directory') await mkdir(join(dir, rel), { recursive: true });
    else if (file.type === 'file' && file.downloads?.raw) {
      tasks.push({ url: file.downloads.raw.url, dest: join(dir, rel), sha1: file.downloads.raw.sha1, size: file.downloads.raw.size, label: rel.split('/').pop() });
    }
  }
  report(`Java ${entry.version.name}`);
  await downloadAll(f, tasks, { onProgress: (p) => report(`Java ${entry.version.name}`, p), signal });
  if (env.os !== 'windows') {
    for (const [rel, file] of Object.entries(m.files)) if (file.type === 'file' && file.executable) await chmod(join(dir, rel), 0o755);
  }
  await writeFile(marker, entry.manifest.sha1);
  return exe;
}

// ---- Mods ----------------------------------------------------------------

export interface ModFile { url: string; filename: string; sha512?: string; sha256?: string; sha1?: string; size?: number }

export async function fabricApiFile(f: FetchLike, gameVersion: string): Promise<ModFile> {
  const q = `game_versions=${encodeURIComponent(JSON.stringify([gameVersion]))}&loaders=${encodeURIComponent(JSON.stringify(['fabric']))}`;
  const versions = await fetchJson<{ version_type: string; files: { url: string; filename: string; primary: boolean; size: number; hashes: { sha1: string; sha512: string } }[] }[]>(
    f, `${MODRINTH}/project/fabric-api/version?${q}`);
  const v = versions.find((x) => x.version_type === 'release') ?? versions[0];
  const file = v?.files.find((x) => x.primary) ?? v?.files[0];
  if (!file) throw new Error(`No Fabric API build for Minecraft ${gameVersion}`);
  return { url: file.url, filename: file.filename, sha512: file.hashes.sha512, sha1: file.hashes.sha1, size: file.size };
}

/** Installs the managed mods into an instance and removes stale managed copies. */
export async function syncMods(f: FetchLike, modsDir: string, files: ModFile[], report: StepReporter, signal?: AbortSignal): Promise<void> {
  await mkdir(modsDir, { recursive: true });
  const managedFile = join(modsDir, '.lego-managed.json');
  const previous = (await readJsonFile<string[]>(managedFile)) ?? [];
  const wanted = new Set(files.map((x) => x.filename));
  for (const old of previous) {
    // Only files the launcher installed itself are ever removed.
    if (!wanted.has(old) && /^[\w.+\-]+\.jar$/.test(old)) await rm(join(modsDir, old), { force: true });
  }
  report('Mods');
  await downloadAll(f, files.map((m) => ({ url: m.url, dest: join(modsDir, m.filename), sha512: m.sha512, sha256: m.sha256, sha1: m.sha1, size: m.size, label: m.filename })), { signal, onProgress: (p) => report('Mods', p) });
  await writeFile(managedFile, JSON.stringify([...wanted], null, 2));
}

export async function listUserMods(modsDir: string): Promise<string[]> {
  const managed = new Set((await readJsonFile<string[]>(join(modsDir, '.lego-managed.json'))) ?? []);
  try {
    return (await readdir(modsDir)).filter((n) => n.endsWith('.jar') && !managed.has(n));
  } catch {
    return [];
  }
}

/** Resolves a version id (vanilla or Fabric) to a fully merged version JSON. */
export async function resolveProfileVersion(f: FetchLike, l: Layout, gameVersion: string, loader: string | null): Promise<{ merged: VersionJson; base: VersionJson }> {
  const base = await vanillaVersion(f, l, gameVersion);
  if (!loader) return { merged: base, base };
  const fabric = await fabricProfile(f, l, gameVersion, loader);
  await fillMavenHashes(f, fabric);
  return { merged: mergeVersions(base, fabric), base };
}
