// Mod loaders. Fabric and Quilt publish launcher profiles as JSON (fully
// supported). Forge and NeoForge are installed with their official installer
// in headless mode, which generates the version JSON and patched libraries
// (supported for Minecraft 1.13+; marked experimental in the UI).

import { spawn } from 'node:child_process';
import { mkdir, readdir, readFile, writeFile, stat } from 'node:fs/promises';
import { join } from 'node:path';
import { downloadAll } from './download.ts';
import { fetchJson, type FetchLike } from './net.ts';
import type { Layout } from './install.ts';
import { mavenPath, type VersionJson } from './mojang.ts';
import type { LoaderType } from './settings.ts';

export const FABRIC_META = 'https://meta.fabricmc.net/v2';
export const QUILT_META = 'https://meta.quiltmc.org/v3';
export const FORGE_MAVEN = 'https://maven.minecraftforge.net';
export const NEOFORGE_MAVEN = 'https://maven.neoforged.net';

export interface LoaderVersion { version: string; stable: boolean }

function cmpVersion(a: string, b: string): number {
  const pa = a.split(/[.\-+]/).map((x) => (/^\d+$/.test(x) ? Number(x) : x));
  const pb = b.split(/[.\-+]/).map((x) => (/^\d+$/.test(x) ? Number(x) : x));
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const x = pa[i] ?? 0, y = pb[i] ?? 0;
    if (x === y) continue;
    if (typeof x === 'number' && typeof y === 'number') return x - y;
    return String(x).localeCompare(String(y));
  }
  return 0;
}

/** Whether the loader exists for this game version at all (cheap check, no network). */
export function loaderSupports(type: LoaderType, gameVersion: string): boolean {
  const m = /^1\.(\d+)/.exec(gameVersion);
  const minor = m ? Number(m[1]) : 99;
  if (type === 'fabric' || type === 'quilt') return minor >= 14 || !m;
  if (type === 'forge') return minor >= 13 || !m;
  if (type === 'neoforge') return minor >= 20 || !m;
  return false;
}

export async function loaderVersions(f: FetchLike, type: LoaderType, gameVersion: string): Promise<LoaderVersion[]> {
  if (type === 'fabric') {
    const list = await fetchJson<{ loader: { version: string; stable: boolean } }[]>(f, `${FABRIC_META}/versions/loader/${encodeURIComponent(gameVersion)}`);
    return list.map((x) => x.loader);
  }
  if (type === 'quilt') {
    const list = await fetchJson<{ loader: { version: string } }[]>(f, `${QUILT_META}/versions/loader/${encodeURIComponent(gameVersion)}`);
    return list.map((x) => ({ version: x.loader.version, stable: !/beta|pre|rc/i.test(x.loader.version) }));
  }
  if (type === 'forge') {
    const all = await fetchJson<Record<string, string[]>>(f, `${FORGE_MAVEN}/net/minecraftforge/forge/maven-metadata.json`);
    return (all[gameVersion] ?? []).slice().reverse().map((v) => ({ version: v.slice(gameVersion.length + 1), stable: true }));
  }
  // NeoForge versions are "<minor>.<patch>.<build>" for game "1.<minor>.<patch>".
  const r = await fetchJson<{ versions: string[] }>(f, `${NEOFORGE_MAVEN}/api/maven/versions/releases/net/neoforged/neoforge`);
  const m = /^1\.(\d+)(?:\.(\d+))?$/.exec(gameVersion);
  if (!m) return [];
  const prefix = `${m[1]}.${m[2] ?? '0'}.`;
  return r.versions.filter((v) => v.startsWith(prefix)).sort(cmpVersion).reverse().map((v) => ({ version: v, stable: !/beta|alpha/i.test(v) }));
}

export async function newestLoader(f: FetchLike, type: LoaderType, gameVersion: string): Promise<string> {
  const list = await loaderVersions(f, type, gameVersion);
  const v = list.find((x) => x.stable) ?? list[0];
  if (!v) throw new Error(`${type} ist für Minecraft ${gameVersion} nicht verfügbar.`);
  return v.version;
}

async function readJson<T>(p: string): Promise<T | null> {
  try {
    return JSON.parse(await readFile(p, 'utf8')) as T;
  } catch {
    return null;
  }
}

/** Fabric/Quilt: download the profile JSON (cached). */
export async function metaProfile(f: FetchLike, l: Layout, type: 'fabric' | 'quilt', gameVersion: string, loader: string): Promise<VersionJson> {
  const id = `${type}-loader-${loader}-${gameVersion}`;
  const file = join(l.versions, id, `${id}.json`);
  const cached = await readJson<VersionJson>(file);
  if (cached) return cached;
  const base = type === 'fabric' ? FABRIC_META : QUILT_META;
  const p = await fetchJson<VersionJson>(f, `${base}/versions/loader/${encodeURIComponent(gameVersion)}/${encodeURIComponent(loader)}/profile/json`);
  p.id = id;
  await mkdir(join(l.versions, id), { recursive: true });
  await writeFile(file, JSON.stringify(p, null, 2));
  return p;
}

export function installerInfo(type: 'forge' | 'neoforge', gameVersion: string, loader: string): { url: string; versionId: string; artifact: string } {
  if (type === 'forge') {
    const full = `${gameVersion}-${loader}`;
    return { url: `${FORGE_MAVEN}/net/minecraftforge/forge/${full}/forge-${full}-installer.jar`, versionId: `${gameVersion}-forge-${loader}`, artifact: `net.minecraftforge:forge:${full}` };
  }
  return { url: `${NEOFORGE_MAVEN}/releases/net/neoforged/neoforge/${loader}/neoforge-${loader}-installer.jar`, versionId: `neoforge-${loader}`, artifact: `net.neoforged:neoforge:${loader}` };
}

/**
 * Forge/NeoForge: runs the official installer headless into the launcher's
 * game root. The installer jar is verified against the maven .sha1 first.
 * Returns the generated version JSON (inheritsFrom vanilla).
 */
export async function installForgeLike(
  f: FetchLike, l: Layout, type: 'forge' | 'neoforge', gameVersion: string, loader: string, javaPath: string, onLine: (s: string) => void,
): Promise<VersionJson> {
  const info = installerInfo(type, gameVersion, loader);
  const existing = await readJson<VersionJson>(join(l.versions, info.versionId, `${info.versionId}.json`));
  if (existing) return existing;
  const sha1Res = await f(`${info.url}.sha1`, { signal: AbortSignal.timeout(15_000) });
  const sha1 = sha1Res.ok ? (await sha1Res.text()).trim().split(/\s+/)[0] : '';
  if (!/^[0-9a-f]{40}$/i.test(sha1 ?? '')) throw new Error(`Keine Prüfsumme für den ${type}-Installer gefunden.`);
  const installer = join(l.root, 'installers', `${type}-${loader}-installer.jar`);
  await downloadAll(f, [{ url: info.url, dest: installer, sha1, label: `${type} installer` }]);
  // The installers expect an official-launcher style directory.
  const profiles = join(l.root, 'launcher_profiles.json');
  try {
    await stat(profiles);
  } catch {
    await writeFile(profiles, JSON.stringify({ profiles: {}, settings: {}, version: 3 }));
  }
  const before = new Set(await readdir(l.versions).catch(() => [] as string[]));
  const flag = type === 'forge' ? '--installClient' : '--install-client';
  await new Promise<void>((resolve, reject) => {
    const p = spawn(javaPath.replace(/javaw\.exe$/i, 'java.exe'), ['-jar', installer, flag, l.root], { cwd: l.root, shell: false, windowsHide: true });
    const pipe = (s: NodeJS.ReadableStream | null) => s?.on('data', (d: Buffer) => d.toString().split(/\r?\n/).filter(Boolean).forEach(onLine));
    pipe(p.stdout);
    pipe(p.stderr);
    p.once('error', reject);
    p.once('exit', (code) => (code === 0 ? resolve() : reject(new Error(`${type}-Installer beendet mit Code ${code}`))));
  });
  const after = await readdir(l.versions);
  const created = after.find((d) => !before.has(d) && (d === info.versionId || d.includes(loader)));
  const id = created ?? info.versionId;
  const json = await readJson<VersionJson>(join(l.versions, id, `${id}.json`));
  if (!json) throw new Error(`Der ${type}-Installer hat keine Versionsdatei erzeugt.`);
  return json;
}

/** Fills missing sha1 values of maven-style libraries from the ".sha1" companion files. */
export async function fillMavenHashes(f: FetchLike, v: VersionJson): Promise<void> {
  for (const lib of v.libraries) {
    if (lib.downloads || lib.sha1 || lib.url === undefined) continue;
    const url = `${lib.url.replace(/\/?$/, '/')}${mavenPath(lib.name)}.sha1`;
    const res = await f(url, { signal: AbortSignal.timeout(15_000) });
    if (res.ok) {
      const text = (await res.text()).trim().split(/\s+/)[0] ?? '';
      if (/^[0-9a-f]{40}$/i.test(text)) lib.sha1 = text.toLowerCase();
    }
    if (!lib.sha1) throw new Error(`Keine Prüfsumme für ${lib.name}`);
  }
}
