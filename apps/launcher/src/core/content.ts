// Everything installed in one profile's game folder, by kind: resource packs, shader packs,
// datapacks, worlds and screenshots (mods have their own list with metadata in main.ts).

import { readdir, stat } from 'node:fs/promises';
import { join } from 'node:path';

export type ContentKind = 'resourcepacks' | 'shaderpacks' | 'datapacks' | 'saves' | 'screenshots';
export const CONTENT_KINDS: readonly ContentKind[] = ['resourcepacks', 'shaderpacks', 'datapacks', 'saves', 'screenshots'];

export interface ContentEntry {
  /** File or folder name inside the kind's folder. */
  name: string;
  /** Shown name: without the extension (and the .disabled suffix). */
  title: string;
  folder: boolean;
  size: number;
  modified: number;
  enabled: boolean;
}

const ACCEPT: Record<ContentKind, (name: string, folder: boolean) => boolean> = {
  resourcepacks: (n, d) => d || /\.zip(\.disabled)?$/i.test(n),
  shaderpacks: (n, d) => d || /\.zip(\.disabled)?$/i.test(n),
  datapacks: (n, d) => d || /\.zip(\.disabled)?$/i.test(n),
  saves: (_n, d) => d,
  screenshots: (n, d) => !d && /\.png$/i.test(n),
};

/** A plain file/folder name inside a content folder (no path parts, no hidden files). */
export function validContentName(name: unknown): name is string {
  return typeof name === 'string' && name.length > 0 && name.length <= 200 && !/[\\/]/.test(name) && !name.startsWith('.') && name !== '..';
}

export async function listContent(gameDir: string, kind: ContentKind): Promise<ContentEntry[]> {
  const dir = join(gameDir, kind);
  const names = await readdir(dir).catch(() => [] as string[]);
  const out: ContentEntry[] = [];
  for (const name of names) {
    if (!validContentName(name)) continue;
    const s = await stat(join(dir, name)).catch(() => null);
    if (!s || !ACCEPT[kind](name, s.isDirectory())) continue;
    // A world is a folder with level.dat.
    if (kind === 'saves' && !(await stat(join(dir, name, 'level.dat')).catch(() => null))) continue;
    out.push({
      name, folder: s.isDirectory(), size: s.isDirectory() ? 0 : s.size, modified: s.mtimeMs,
      enabled: !name.endsWith('.disabled'), title: name.replace(/\.disabled$/, '').replace(/\.(zip|png)$/i, ''),
    });
  }
  // Worlds and screenshots: newest first; packs by name.
  return kind === 'saves' || kind === 'screenshots'
    ? out.sort((a, b) => b.modified - a.modified)
    : out.sort((a, b) => a.title.localeCompare(b.title));
}
