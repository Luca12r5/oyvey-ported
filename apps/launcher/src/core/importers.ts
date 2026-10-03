// Import profiles from other launchers and CurseForge modpack zips. Copies
// mods, configs, resource/shader packs and options into a new LEGO profile.
// Nothing in the source launcher is modified.

import { cp, readdir, readFile, stat, mkdir, writeFile } from 'node:fs/promises';
import { homedir } from 'node:os';
import { basename, dirname, join } from 'node:path';
import { downloadAll, type DownloadTask } from './download.ts';
import { fetchJson, type FetchLike } from './net.ts';
import { safeJoin } from './modrinth.ts';
import type { LoaderType } from './settings.ts';
import { listEntries, readEntry } from './zip.ts';

export interface FoundInstance {
  source: 'prism' | 'multimc' | 'curseforge' | 'official' | 'folder';
  name: string;
  /** Directory containing mods/, config/, options.txt ... */
  gameDir: string;
  gameVersion: string | null;
  loaderType: LoaderType | null;
  loader: string | null;
}

async function exists(p: string): Promise<boolean> {
  try {
    await stat(p);
    return true;
  } catch {
    return false;
  }
}

async function json<T>(p: string): Promise<T | null> {
  try {
    return JSON.parse(await readFile(p, 'utf8')) as T;
  } catch {
    return null;
  }
}

/** Parses version ids like "fabric-loader-0.15.0-1.20.1", "1.20.1-forge-47.2.0", "neoforge-21.1.77", "quilt-loader-0.26.0-1.21". */
export function parseVersionId(id: string): { gameVersion: string | null; loaderType: LoaderType | null; loader: string | null } {
  let m = /^(fabric|quilt)-loader-([^-]+)-(.+)$/.exec(id);
  if (m) return { gameVersion: m[3]!, loaderType: m[1] as LoaderType, loader: m[2]! };
  m = /^(.+?)-forge-?(.+)$/.exec(id);
  if (m) return { gameVersion: m[1]!, loaderType: 'forge', loader: m[2]!.startsWith(`${m[1]}-`) ? m[2]!.slice(m[1]!.length + 1) : m[2]! };
  m = /^neoforge-(\d+)\.(\d+)\.(.+)$/.exec(id);
  if (m) return { gameVersion: `1.${m[1]}${m[2] === '0' ? '' : `.${m[2]}`}`, loaderType: 'neoforge', loader: `${m[1]}.${m[2]}.${m[3]}` };
  if (/^\d+\.\d+(\.\d+)?$/.test(id) || /^\d\dw\d\d[a-z]$/.test(id)) return { gameVersion: id, loaderType: null, loader: null };
  return { gameVersion: null, loaderType: null, loader: null };
}

/** Prism Launcher / MultiMC instance folder (instance.cfg + mmc-pack.json). */
export async function readMmcInstance(dir: string, source: 'prism' | 'multimc'): Promise<FoundInstance | null> {
  const pack = await json<{ components: { uid: string; version: string }[] }>(join(dir, 'mmc-pack.json'));
  if (!pack) return null;
  const cfg = await readFile(join(dir, 'instance.cfg'), 'utf8').catch(() => '');
  const name = /^name=(.*)$/m.exec(cfg)?.[1]?.trim() || basename(dir);
  const comp = (uid: string) => pack.components.find((c) => c.uid === uid)?.version ?? null;
  let loaderType: LoaderType | null = null;
  let loader: string | null = null;
  for (const [uid, t] of [['net.fabricmc.fabric-loader', 'fabric'], ['org.quiltmc.quilt-loader', 'quilt'], ['net.neoforged', 'neoforge'], ['net.minecraftforge', 'forge']] as const) {
    if (comp(uid)) { loaderType = t; loader = comp(uid); break; }
  }
  const gameDir = (await exists(join(dir, '.minecraft'))) ? join(dir, '.minecraft') : join(dir, 'minecraft');
  return { source, name, gameDir, gameVersion: comp('net.minecraft'), loaderType, loader };
}

/** CurseForge app instance (minecraftinstance.json). */
export async function readCurseForgeInstance(dir: string): Promise<FoundInstance | null> {
  const j = await json<{ name?: string; gameVersion?: string; baseModLoader?: { name?: string; minecraftVersion?: string } }>(join(dir, 'minecraftinstance.json'));
  if (!j) return null;
  const ml = j.baseModLoader?.name ?? '';
  const m = /^(forge|fabric|neoforge|quilt)-(.+?)(?:-(\d+\.\d+(?:\.\d+)?))?$/.exec(ml);
  return {
    source: 'curseforge', name: j.name ?? basename(dir), gameDir: dir, gameVersion: j.gameVersion ?? j.baseModLoader?.minecraftVersion ?? null,
    loaderType: (m?.[1] as LoaderType) ?? null, loader: m?.[2] ?? null,
  };
}

/** Official launcher profiles (launcher_profiles.json in .minecraft). */
export async function readOfficialProfiles(mcDir: string): Promise<FoundInstance[]> {
  const j = await json<{ profiles?: Record<string, { name?: string; lastVersionId?: string; gameDir?: string; type?: string }> }>(join(mcDir, 'launcher_profiles.json'));
  if (!j?.profiles) return [];
  const out: FoundInstance[] = [];
  for (const p of Object.values(j.profiles)) {
    if (!p.lastVersionId || p.lastVersionId.startsWith('latest-')) continue;
    const v = parseVersionId(p.lastVersionId);
    out.push({ source: 'official', name: p.name || p.lastVersionId, gameDir: p.gameDir || mcDir, ...v });
  }
  return out;
}

/** Looks for instances of other launchers in their default locations (Windows paths first). */
export async function scanInstances(env: NodeJS.ProcessEnv = process.env): Promise<FoundInstance[]> {
  const appData = env.APPDATA ?? join(homedir(), '.config');
  const found: FoundInstance[] = [];
  const scanDir = async (root: string, read: (d: string) => Promise<FoundInstance | null>) => {
    for (const name of await readdir(root).catch(() => [] as string[])) {
      const inst = await read(join(root, name)).catch(() => null);
      if (inst) found.push(inst);
    }
  };
  await scanDir(join(appData, 'PrismLauncher', 'instances'), (d) => readMmcInstance(d, 'prism'));
  await scanDir(join(homedir(), '.local', 'share', 'PrismLauncher', 'instances'), (d) => readMmcInstance(d, 'prism'));
  await scanDir(join(homedir(), 'curseforge', 'minecraft', 'Instances'), readCurseForgeInstance);
  found.push(...(await readOfficialProfiles(join(appData, '.minecraft'))));
  return found;
}

/** Reads any folder the user picked: an instance folder of a known launcher, or a plain .minecraft-like folder. */
export async function readFolder(dir: string): Promise<FoundInstance> {
  return (await readMmcInstance(dir, 'multimc')) ?? (await readCurseForgeInstance(dir)) ?? {
    source: 'folder', name: basename(dir), gameDir: dir, gameVersion: null, loaderType: null, loader: null,
  };
}

export const COPY_ITEMS = ['mods', 'config', 'resourcepacks', 'shaderpacks', 'options.txt', 'optionsof.txt', 'optionsshaders.txt', 'servers.dat', 'defaultconfigs', 'kubejs'] as const;

/** Copies the instance's content into `targetDir`. Worlds only if requested. */
export async function copyInstance(src: string, targetDir: string, includeWorlds: boolean): Promise<string[]> {
  await mkdir(targetDir, { recursive: true });
  const copied: string[] = [];
  for (const item of [...COPY_ITEMS, ...(includeWorlds ? ['saves'] : [])]) {
    const from = join(src, item);
    if (!(await exists(from))) continue;
    await cp(from, join(targetDir, item), { recursive: true, force: false, errorOnExist: false });
    copied.push(item);
  }
  return copied;
}

// ---- CurseForge modpack zip --------------------------------------------------

export interface CfManifest {
  minecraft: { version: string; modLoaders: { id: string; primary?: boolean }[] };
  manifestType: string;
  name: string;
  version?: string;
  files: { projectID: number; fileID: number; required: boolean }[];
  overrides?: string;
}

export function readCfManifest(zip: Buffer): CfManifest {
  const e = listEntries(zip).find((x) => x.name === 'manifest.json');
  if (!e) throw new Error('Keine manifest.json – ist das ein CurseForge-Modpack?');
  const m = JSON.parse(readEntry(zip, e).toString('utf8')) as CfManifest;
  if (m.manifestType !== 'minecraftModpack') throw new Error('Unbekanntes CurseForge-Format');
  return m;
}

export function cfLoader(m: CfManifest): { gameVersion: string; loaderType: LoaderType | null; loader: string | null } {
  const id = (m.minecraft.modLoaders.find((l) => l.primary) ?? m.minecraft.modLoaders[0])?.id ?? '';
  const x = /^(forge|fabric|neoforge|quilt)-(.+)$/.exec(id);
  return { gameVersion: m.minecraft.version, loaderType: (x?.[1] as LoaderType) ?? null, loader: x?.[2] ?? null };
}

/**
 * Installs a CurseForge pack. Downloading the listed mods needs a CurseForge
 * API key; without one only the overrides (configs etc.) are imported and the
 * missing mods are returned so the user can add them via the mod browser.
 */
export async function installCfPack(f: FetchLike, zip: Buffer, gameDir: string, apiKey: string): Promise<{ manifest: CfManifest; downloaded: number; missing: string[] }> {
  const m = readCfManifest(zip);
  const overrides = (m.overrides ?? 'overrides').replace(/\/$/, '');
  for (const e of listEntries(zip)) {
    if (!e.name.startsWith(`${overrides}/`) || e.name.endsWith('/')) continue;
    const dest = safeJoin(gameDir, e.name.slice(overrides.length + 1));
    await mkdir(dirname(dest), { recursive: true });
    await writeFile(dest, readEntry(zip, e));
  }
  const missing: string[] = [];
  let downloaded = 0;
  if (!apiKey) {
    missing.push(...m.files.map((x) => `CurseForge-Projekt ${x.projectID} (Datei ${x.fileID})`));
    return { manifest: m, downloaded, missing };
  }
  const r = await fetchJson<{ data: { id: number; modId: number; fileName: string; downloadUrl: string | null; fileLength: number; hashes: { value: string; algo: number }[] }[] }>(
    f, 'https://api.curseforge.com/v1/mods/files',
    { method: 'POST', headers: { 'x-api-key': apiKey, 'Content-Type': 'application/json' }, body: JSON.stringify({ fileIds: m.files.map((x) => x.fileID) }) },
  );
  const tasks: DownloadTask[] = [];
  for (const file of r.data) {
    const sha1 = file.hashes.find((h) => h.algo === 1)?.value ?? null;
    if (!file.downloadUrl || !sha1 || !/^[^/\\]+$/.test(file.fileName)) {
      missing.push(`${file.fileName} (der Autor erlaubt keinen Download über Drittanbieter)`);
      continue;
    }
    const folder = /\.zip$/i.test(file.fileName) ? 'resourcepacks' : 'mods';
    tasks.push({ url: file.downloadUrl, dest: join(gameDir, folder, file.fileName), sha1, size: file.fileLength, label: file.fileName });
  }
  await downloadAll(f, tasks);
  downloaded = tasks.length;
  return { manifest: m, downloaded, missing };
}
