// Installs everything needed to play: vanilla client, libraries, assets, the
// Mojang Java runtime, the Fabric loader profile, Fabric API and the LEGO
// client mod. Every download is hash-verified (see download.ts).

import { mkdir, readFile, readdir, rm, writeFile, chmod, stat, copyFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { downloadAll, type DownloadTask, type Progress } from './download.ts';
import { fetchJson, type FetchLike } from './net.ts';
import {
  mergeVersions, resolveLibraries, RESOURCES, VERSION_MANIFEST,
  type Env, type Manifest, type VersionJson,
} from './mojang.ts';
import { extract, nativeFilter } from './zip.ts';
import { fillMavenHashes, installForgeLike, metaProfile, newestLoader } from './loaders.ts';
import type { LoaderType } from './settings.ts';

export const JAVA_RUNTIME_INDEX = 'https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json';
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

export { loaderVersions, newestLoader } from './loaders.ts';

interface AssetIndex { objects: Record<string, { hash: string; size: number }>; virtual?: boolean; map_to_resources?: boolean }

/** `clientId` is the vanilla version id that owns the client jar (the merged Fabric profile has its own id). */
export async function installVersion(f: FetchLike, l: Layout, version: VersionJson, clientId: string, env: Env, report: StepReporter, signal?: AbortSignal, gameDir?: string): Promise<{ clientJar: string; nativesDir: string; loggingConfig: string | null; virtualAssets: string | null }> {
  let virtualAssets: string | null = null;
  const baseId = clientId;
  const clientJar = join(l.versions, baseId, `${baseId}.jar`);
  const tasks: DownloadTask[] = [];
  if (!version.downloads?.client) throw new Error('version has no client download');
  tasks.push({ url: version.downloads.client.url, dest: clientJar, sha1: version.downloads.client.sha1, size: version.downloads.client.size, label: `${baseId}.jar` });
  const libs = resolveLibraries(version, env);
  for (const lib of libs) {
    // Forge/NeoForge generate some jars locally (empty URL); they must already exist.
    if (!lib.url) {
      try {
        await stat(join(l.libraries, lib.path));
      } catch {
        throw new Error(`Bibliothek fehlt (vom Loader-Installer erzeugt): ${lib.path}`);
      }
      continue;
    }
    tasks.push({ url: lib.url, dest: join(l.libraries, lib.path), sha1: lib.sha1, size: lib.size, label: lib.path.split('/').pop() });
  }

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
    // Very old versions (pre-1.7) read assets from a "virtual" tree or the resources folder.
    if (index.virtual || index.map_to_resources) {
      const target = index.virtual ? join(l.assets, 'virtual', ai.id) : join(gameDir ?? l.root, 'resources');
      for (const [name, o] of Object.entries(index.objects)) {
        const dest = join(target, name);
        if (!dest.startsWith(target)) continue;
        await mkdir(dirname(dest), { recursive: true });
        await copyFile(join(l.assets, 'objects', o.hash.slice(0, 2), o.hash), dest);
      }
      virtualAssets = target;
    }
  }

  // Extract native libraries into a per-version directory.
  const nativesDir = join(l.versions, version.id, 'natives');
  await rm(nativesDir, { recursive: true, force: true });
  await mkdir(nativesDir, { recursive: true });
  for (const lib of libs.filter((x) => x.native)) {
    await extract(join(l.libraries, lib.path), nativesDir, nativeFilter(lib.excludes));
  }
  return { clientJar, nativesDir, loggingConfig, virtualAssets };
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

/**
 * Resolves the version JSON for a profile. For Forge/NeoForge the vanilla
 * version must already be installed and `javaPath` must point to a JRE,
 * because the official installer runs headless.
 */
export async function resolveLoaderVersion(
  f: FetchLike, l: Layout, base: VersionJson, loaderType: LoaderType | null, loader: string | null, javaPath: string | null, onLine: (s: string) => void = () => {},
): Promise<VersionJson> {
  if (!loaderType) return base;
  const version = loader ?? (await newestLoader(f, loaderType, base.id));
  if (loaderType === 'fabric' || loaderType === 'quilt') {
    const p = await metaProfile(f, l, loaderType, base.id, version);
    await fillMavenHashes(f, p);
    return mergeVersions(base, p);
  }
  if (!javaPath) throw new Error('Java wird für den Loader-Installer benötigt.');
  const p = await installForgeLike(f, l, loaderType, base.id, version, javaPath, onLine);
  return mergeVersions(base, p);
}
