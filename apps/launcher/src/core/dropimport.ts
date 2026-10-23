// What a file dropped onto the launcher is, from its name and (for zips) its entries.
export type DropKind = 'modpack' | 'mod' | 'resourcepack' | 'shaderpack' | 'skin' | 'design' | 'unknown';

export function classifyDrop(name: string, entries: string[] = []): DropKind {
  const ext = (/\.([a-z0-9]+)$/i.exec(name)?.[1] ?? '').toLowerCase();
  if (ext === 'png') return 'skin';
  if (ext === 'json') return 'design';
  if (ext === 'jar') return 'mod';
  if (ext === 'mrpack') return 'modpack';
  if (ext !== 'zip') return 'unknown';
  const set = new Set(entries);
  if (set.has('modrinth.index.json') || set.has('manifest.json')) return 'modpack';
  if (entries.some((e) => /^(?:[^/]+\/)?shaders\//.test(e))) return 'shaderpack';
  if (set.has('pack.mcmeta') || entries.some((e) => /^[^/]+\/pack\.mcmeta$/.test(e))) return 'resourcepack';
  return 'unknown';
}

/** File name that is safe to write into a profile folder. */
export function safeFileName(name: string): string {
  const base = name.split(/[\\/]/).pop() ?? 'file';
  const clean = base.replace(/[^\w.\- ()+[\]]/g, '_').replace(/^\.+/, '');
  return clean.slice(0, 120) || 'file';
}
