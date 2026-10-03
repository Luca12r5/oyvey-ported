// Modrinth integration: search mods/resource packs/shaders/modpacks, install a
// mod with its required dependencies into a profile, and install .mrpack
// modpacks. Every file is verified with the SHA-512 Modrinth publishes.

import { mkdir, readFile, rename, rm, writeFile } from 'node:fs/promises';
import { dirname, join, normalize, sep } from 'node:path';
import { downloadAll, type DownloadTask } from './download.ts';
import { fetchJson, type FetchLike } from './net.ts';
import type { LoaderType } from './settings.ts';
import { listEntries, readEntry } from './zip.ts';

export const MODRINTH = 'https://api.modrinth.com/v2';
export type ProjectType = 'mod' | 'resourcepack' | 'shader' | 'modpack';

export interface SearchHit {
  project_id: string;
  slug: string;
  title: string;
  description: string;
  icon_url: string | null;
  downloads: number;
  author: string;
  categories: string[];
  project_type: ProjectType;
}

export interface MrVersion {
  id: string;
  project_id: string;
  name: string;
  version_number: string;
  version_type: 'release' | 'beta' | 'alpha';
  loaders: string[];
  game_versions: string[];
  files: { url: string; filename: string; primary: boolean; size: number; hashes: { sha1: string; sha512: string } }[];
  dependencies: { version_id: string | null; project_id: string | null; dependency_type: 'required' | 'optional' | 'incompatible' | 'embedded' }[];
}

/** Metadata of mods the launcher installed into a profile (mods/.lego-mods.json). */
export interface InstalledMod {
  projectId: string | null;
  versionId: string | null;
  title: string;
  filename: string;
  sha512: string | null;
  iconUrl: string | null;
  source: 'modrinth' | 'modpack' | 'import' | 'manual';
}

const FOLDER: Record<ProjectType, string> = { mod: 'mods', resourcepack: 'resourcepacks', shader: 'shaderpacks', modpack: '' };

export async function search(f: FetchLike, q: { query: string; type: ProjectType; gameVersion: string | null; loader: LoaderType | null; offset?: number; limit?: number }): Promise<{ hits: SearchHit[]; total: number }> {
  const facets: string[][] = [[`project_type:${q.type}`]];
  if (q.gameVersion && q.type !== 'modpack') facets.push([`versions:${q.gameVersion}`]);
  if (q.loader && (q.type === 'mod' || q.type === 'modpack')) facets.push([`categories:${q.loader}`]);
  const params = new URLSearchParams({ query: q.query, facets: JSON.stringify(facets), limit: String(q.limit ?? 20), offset: String(q.offset ?? 0), index: q.query ? 'relevance' : 'downloads' });
  const r = await fetchJson<{ hits: SearchHit[]; total_hits: number }>(f, `${MODRINTH}/search?${params}`);
  return { hits: r.hits, total: r.total_hits };
}

export async function projectVersions(f: FetchLike, projectId: string, gameVersion: string | null, loader: LoaderType | null, type: ProjectType = 'mod'): Promise<MrVersion[]> {
  const p = new URLSearchParams();
  if (gameVersion) p.set('game_versions', JSON.stringify([gameVersion]));
  if (loader && type === 'mod') p.set('loaders', JSON.stringify([loader]));
  return fetchJson<MrVersion[]>(f, `${MODRINTH}/project/${encodeURIComponent(projectId)}/version?${p}`);
}

function pick(versions: MrVersion[]): MrVersion | undefined {
  return versions.find((v) => v.version_type === 'release') ?? versions[0];
}

async function readInstalled(dir: string): Promise<InstalledMod[]> {
  try {
    return JSON.parse(await readFile(join(dir, '.lego-mods.json'), 'utf8')) as InstalledMod[];
  } catch {
    return [];
  }
}

async function writeInstalled(dir: string, mods: InstalledMod[]): Promise<void> {
  await mkdir(dir, { recursive: true });
  await writeFile(join(dir, '.lego-mods.json'), JSON.stringify(mods, null, 2));
}

export async function installedMods(gameDir: string): Promise<InstalledMod[]> {
  return readInstalled(join(gameDir, 'mods'));
}

/**
 * Installs a project (and, for mods, all required dependencies) into the
 * profile's game directory. Returns the titles that were installed.
 */
export async function installProject(
  f: FetchLike, gameDir: string, projectId: string, gameVersion: string, loader: LoaderType | null, type: ProjectType = 'mod',
  meta: { title?: string; iconUrl?: string | null } = {}, alreadyManaged: Set<string> = new Set(),
): Promise<string[]> {
  if (type === 'modpack') throw new Error('Modpacks werden als neues Profil installiert.');
  const dir = join(gameDir, FOLDER[type]);
  const installed = type === 'mod' ? await readInstalled(dir) : [];
  const have = new Set([...installed.map((m) => m.projectId).filter((x): x is string => !!x), ...alreadyManaged]);
  const queue: { projectId: string | null; versionId: string | null; title?: string; iconUrl?: string | null }[] = [{ projectId, versionId: null, ...meta }];
  const tasks: DownloadTask[] = [];
  const added: InstalledMod[] = [];
  const seen = new Set<string>();
  while (queue.length) {
    const item = queue.shift()!;
    let v: MrVersion | undefined;
    if (item.versionId) v = await fetchJson<MrVersion>(f, `${MODRINTH}/version/${encodeURIComponent(item.versionId)}`);
    else if (item.projectId) {
      if (have.has(item.projectId) && item.projectId !== projectId) continue;
      v = pick(await projectVersions(f, item.projectId, gameVersion, loader, type));
    }
    if (!v) {
      if (item.projectId === projectId) throw new Error(`Keine passende Version für Minecraft ${gameVersion}${loader ? ` (${loader})` : ''}.`);
      continue;
    }
    if (seen.has(v.project_id)) continue;
    seen.add(v.project_id);
    const file = v.files.find((x) => x.primary) ?? v.files[0];
    if (!file) continue;
    tasks.push({ url: file.url, dest: join(dir, file.filename), sha512: file.hashes.sha512, size: file.size, label: file.filename });
    added.push({ projectId: v.project_id, versionId: v.id, title: item.title ?? v.name, filename: file.filename, sha512: file.hashes.sha512, iconUrl: item.iconUrl ?? null, source: 'modrinth' });
    if (type === 'mod') {
      for (const d of v.dependencies) {
        if (d.dependency_type !== 'required') continue;
        if (d.project_id && have.has(d.project_id)) continue;
        queue.push({ projectId: d.project_id, versionId: d.version_id });
      }
    }
  }
  await downloadAll(f, tasks);
  if (type === 'mod') {
    // Replace older files of the same projects.
    const ids = new Set(added.map((a) => a.projectId));
    for (const old of installed.filter((m) => m.projectId && ids.has(m.projectId))) {
      if (!added.some((a) => a.filename === old.filename)) await rm(join(dir, old.filename), { force: true });
    }
    await writeInstalled(dir, [...installed.filter((m) => !m.projectId || !ids.has(m.projectId)), ...added]);
  }
  return added.map((a) => a.title);
}

export async function removeMod(gameDir: string, filename: string): Promise<void> {
  if (!/^[^/\\]+\.jar(\.disabled)?$/.test(filename)) throw new Error('invalid file name');
  const dir = join(gameDir, 'mods');
  await rm(join(dir, filename), { force: true });
  await writeInstalled(dir, (await readInstalled(dir)).filter((m) => m.filename !== filename && `${m.filename}.disabled` !== filename));
}

/** Enables/disables a mod by renaming it to/from ".jar.disabled". */
export async function toggleMod(gameDir: string, filename: string, enabled: boolean): Promise<string> {
  if (!/^[^/\\]+\.jar(\.disabled)?$/.test(filename)) throw new Error('invalid file name');
  const dir = join(gameDir, 'mods');
  const base = filename.replace(/\.disabled$/, '');
  const target = enabled ? base : `${base}.disabled`;
  if (target !== filename) await rename(join(dir, filename), join(dir, target));
  return target;
}

// ---- performance pack -----------------------------------------------------

/** Widely used, open-source client optimisation mods (installed from Modrinth on request). */
export const PERFORMANCE_PACK: { slug: string; title: string }[] = [
  { slug: 'sodium', title: 'Sodium' },
  { slug: 'lithium', title: 'Lithium' },
  { slug: 'ferrite-core', title: 'FerriteCore' },
  { slug: 'immediatelyfast', title: 'ImmediatelyFast' },
  { slug: 'entityculling', title: 'EntityCulling' },
];

/** Resolves the performance pack files that exist for this game version/loader. */
export interface PerfFile { projectId: string; title: string; url: string; filename: string; sha512: string; size: number }

export async function performanceFiles(f: FetchLike, gameVersion: string, loader: LoaderType): Promise<PerfFile[]> {
  const out: PerfFile[] = [];
  for (const m of PERFORMANCE_PACK) {
    try {
      const v = pick(await projectVersions(f, m.slug, gameVersion, loader));
      const file = v?.files.find((x) => x.primary) ?? v?.files[0];
      if (file && v) out.push({ projectId: v.project_id, title: m.title, url: file.url, filename: file.filename, sha512: file.hashes.sha512, size: file.size });
    } catch {
      // Not available for this version: skip silently.
    }
  }
  return out;
}

// ---- .mrpack modpacks ------------------------------------------------------

export interface MrPackIndex {
  formatVersion: number;
  game: string;
  versionId: string;
  name: string;
  summary?: string;
  files: { path: string; hashes: { sha1: string; sha512?: string }; env?: { client?: string; server?: string }; downloads: string[]; fileSize?: number }[];
  dependencies: Record<string, string>;
}

const ALLOWED_PACK_HOSTS = new Set(['cdn.modrinth.com', 'github.com', 'raw.githubusercontent.com', 'gitlab.com']);

export function packLoader(deps: Record<string, string>): { gameVersion: string; loaderType: LoaderType | null; loader: string | null } {
  const gameVersion = deps.minecraft;
  if (!gameVersion) throw new Error('Modpack ohne Minecraft-Version');
  if (deps['fabric-loader']) return { gameVersion, loaderType: 'fabric', loader: deps['fabric-loader'] };
  if (deps['quilt-loader']) return { gameVersion, loaderType: 'quilt', loader: deps['quilt-loader'] };
  if (deps.neoforge) return { gameVersion, loaderType: 'neoforge', loader: deps.neoforge };
  if (deps.forge) return { gameVersion, loaderType: 'forge', loader: deps.forge };
  return { gameVersion, loaderType: null, loader: null };
}

/** Rejects absolute paths and traversal; returns the safe absolute target. */
export function safeJoin(root: string, rel: string): string {
  const target = normalize(join(root, rel));
  if (!target.startsWith(normalize(root) + sep) || rel.includes('\0')) throw new Error(`Unsicherer Pfad im Paket: ${rel}`);
  return target;
}

export function readPackIndex(zip: Buffer): MrPackIndex {
  const entry = listEntries(zip).find((e) => e.name === 'modrinth.index.json');
  if (!entry) throw new Error('Keine modrinth.index.json im Paket – ist das eine .mrpack-Datei?');
  const idx = JSON.parse(readEntry(zip, entry).toString('utf8')) as MrPackIndex;
  if (idx.game !== 'minecraft' || !Array.isArray(idx.files)) throw new Error('Ungültiges Modpack');
  return idx;
}

/** Installs the pack's files and overrides into `gameDir`. */
export async function installMrPack(f: FetchLike, zip: Buffer, gameDir: string, onProgress?: (done: number, total: number) => void): Promise<{ index: MrPackIndex; mods: number }> {
  const idx = readPackIndex(zip);
  const tasks: DownloadTask[] = [];
  const mods: InstalledMod[] = [];
  for (const file of idx.files) {
    if (file.env?.client === 'unsupported') continue;
    const dest = safeJoin(gameDir, file.path);
    const url = file.downloads.find((u) => {
      try {
        const h = new URL(u);
        return h.protocol === 'https:' && ALLOWED_PACK_HOSTS.has(h.hostname);
      } catch {
        return false;
      }
    });
    if (!url) throw new Error(`Datei ${file.path} hat keine erlaubte Download-Quelle.`);
    tasks.push({ url, dest, sha512: file.hashes.sha512 ?? null, sha1: file.hashes.sha1, size: file.fileSize ?? null, label: file.path });
    if (file.path.startsWith('mods/')) mods.push({ projectId: null, versionId: null, title: file.path.slice(5), filename: file.path.slice(5), sha512: file.hashes.sha512 ?? null, iconUrl: null, source: 'modpack' });
  }
  await downloadAll(f, tasks, { onProgress: (p) => onProgress?.(p.done, p.total) });
  // "client-overrides" take precedence over "overrides".
  for (const prefix of ['overrides/', 'client-overrides/']) {
    for (const e of listEntries(zip)) {
      if (!e.name.startsWith(prefix) || e.name.endsWith('/')) continue;
      const dest = safeJoin(gameDir, e.name.slice(prefix.length));
      await mkdir(dirname(dest), { recursive: true });
      await writeFile(dest, readEntry(zip, e));
    }
  }
  await writeInstalled(join(gameDir, 'mods'), mods);
  return { index: idx, mods: mods.length };
}
