// Installs a LEGO Launcher profile into the official Minecraft Launcher (like
// the Fabric installer does): writes the loader version JSON into
// .minecraft/versions and adds a profile to launcher_profiles.json. The
// official launcher then handles sign-in, downloads and starting the game.

import { copyFile, mkdir, readFile, rename, stat, writeFile } from 'node:fs/promises';
import { homedir } from 'node:os';
import { join } from 'node:path';
import { layout } from './install.ts';
import { metaProfile, newestLoader } from './loaders.ts';
import type { FetchLike } from './net.ts';
import type { LoaderType } from './settings.ts';

/** Default .minecraft folder of the official launcher on this OS. */
export function officialMinecraftDir(env: NodeJS.ProcessEnv = process.env, platform: NodeJS.Platform = process.platform): string {
  if (platform === 'win32') return join(env.APPDATA ?? join(homedir(), 'AppData', 'Roaming'), '.minecraft');
  if (platform === 'darwin') return join(homedir(), 'Library', 'Application Support', 'minecraft');
  return join(homedir(), '.minecraft');
}

export async function officialLauncherPresent(mcDir: string): Promise<boolean> {
  try {
    await stat(join(mcDir, 'launcher_profiles.json'));
    return true;
  } catch {
    return false;
  }
}

export interface OfficialProfileOptions {
  /** Stable key in launcher_profiles.json (re-exporting updates the same profile). */
  key: string;
  name: string;
  gameVersion: string;
  loaderType: LoaderType | null;
  loader: string | null;
  /** Game directory with the profile's mods (the LEGO Launcher instance folder). */
  gameDir: string;
  memoryMb: number;
  /** PNG data URL shown as profile icon, or null for the default icon. */
  icon: string | null;
}

interface LauncherProfiles {
  profiles?: Record<string, Record<string, unknown>>;
  [k: string]: unknown;
}

/**
 * Adds or updates the profile. Only Fabric, Quilt and vanilla are supported:
 * Forge/NeoForge must be installed with their own installer.
 * Returns the version id the profile starts.
 */
export async function exportToOfficial(f: FetchLike, mcDir: string, o: OfficialProfileOptions, now = new Date()): Promise<string> {
  if (o.loaderType === 'forge' || o.loaderType === 'neoforge') throw new Error('Forge/NeoForge-Profile bitte mit dem offiziellen Forge-Installer einrichten.');
  const file = join(mcDir, 'launcher_profiles.json');
  let data: LauncherProfiles;
  try {
    data = JSON.parse(await readFile(file, 'utf8')) as LauncherProfiles;
  } catch (e) {
    if ((e as NodeJS.ErrnoException).code === 'ENOENT') throw new Error('Der offizielle Minecraft Launcher wurde nicht gefunden. Bitte einmal installieren und starten.');
    throw new Error('launcher_profiles.json des offiziellen Launchers ist beschädigt.');
  }
  let versionId = o.gameVersion;
  if (o.loaderType === 'fabric' || o.loaderType === 'quilt') {
    const loader = o.loader ?? (await newestLoader(f, o.loaderType, o.gameVersion));
    // Writes versions/<id>/<id>.json (inheritsFrom vanilla); the official launcher fetches the rest.
    versionId = (await metaProfile(f, layout(mcDir), o.loaderType, o.gameVersion, loader)).id;
  }
  const iso = now.toISOString();
  const prev = data.profiles?.[o.key] ?? {};
  const profile: Record<string, unknown> = {
    ...prev,
    name: o.name,
    type: 'custom',
    lastVersionId: versionId,
    gameDir: o.gameDir,
    javaArgs: `-Xmx${Math.round(o.memoryMb)}M -XX:+UseG1GC`,
    created: prev.created ?? iso,
    lastUsed: iso,
  };
  if (o.icon && /^data:image\/png;base64,[A-Za-z0-9+/=]+$/.test(o.icon) && o.icon.length < 200_000) profile.icon = o.icon;
  else if (!prev.icon) profile.icon = 'Crafting_Table';
  data.profiles = { ...(data.profiles ?? {}), [o.key]: profile };
  // Keep a backup and write atomically: the official launcher must never see a half-written file.
  await copyFile(file, `${file}.lego-backup`).catch(() => {});
  await mkdir(mcDir, { recursive: true });
  await writeFile(`${file}.tmp`, JSON.stringify(data, null, 2));
  await rename(`${file}.tmp`, file);
  return versionId;
}
