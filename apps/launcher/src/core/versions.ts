// Groups Minecraft versions for the version picker: year-based releases
// (26.1, 26.2 …) and classic ones (1.21.x … 1.8.x) newest first; snapshots and
// pre-1.8 versions only on request.

export interface GameVersion { id: string; type: string; releaseTime: string }

/** "26.1.2" → "26.x", "1.21.11" → "1.21.x", "26w05a"/"1.21-pre1" → "Snapshots", 1.7.10 → "Vor 1.8". */
export function versionGroup(v: GameVersion): string {
  if (v.type !== 'release') return v.type.startsWith('old_') ? 'Alpha/Beta' : 'Snapshots';
  const y = /^(\d{2})\.\d+/.exec(v.id);
  if (y) return `${y[1]}.x`;
  const c = /^1\.(\d+)/.exec(v.id);
  if (!c) return 'Andere';
  return Number(c[1]) >= 8 ? `1.${c[1]}.x` : 'Vor 1.8';
}

/** Releases from 1.8 on (always), plus snapshots/older versions when asked; newest first, grouped. */
export function groupVersions(list: GameVersion[], opts: { snapshots: boolean; historical: boolean; keep?: string }): { label: string; versions: GameVersion[] }[] {
  const sorted = [...list].sort((a, b) => (b.releaseTime || '').localeCompare(a.releaseTime || ''));
  const out = new Map<string, GameVersion[]>();
  for (const v of sorted) {
    const g = versionGroup(v);
    const show = v.id === opts.keep || (g === 'Snapshots' ? opts.snapshots : g === 'Vor 1.8' || g === 'Alpha/Beta' ? opts.historical : true);
    if (!show) continue;
    out.set(g, [...(out.get(g) ?? []), v]);
  }
  return [...out].map(([label, versions]) => ({ label, versions }));
}
