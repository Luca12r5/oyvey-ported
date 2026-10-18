// Ported from the NoRisk Client launcher (GPL-3.0): components/tabs/ProfilesTabV2.tsx.
// Groups are the LEGO launcher's (LEGO Client, modded, vanilla); the list is a
// plain scroll container instead of react-virtuoso (profile counts are small).
import { useMemo, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { GroupTabs, type GroupTab } from '../ui/GroupTabs.tsx';
import { SearchWithFilters } from '../ui/SearchWithFilters.tsx';
import { MultiVersionFilter } from '../ui/MultiVersionFilter.tsx';
import { ActionButtons, type ActionButton } from '../ui/ActionButtons.tsx';
import { EmptyState } from '../ui/EmptyState.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { ProfileCardV2, type ProfileCardHandlers } from '../profiles/ProfileCardV2.tsx';
import { usePins } from '../store/pins.ts';
import type { GameProfile as Profile } from '../../../core/settings.ts';

type Layout = 'list' | 'grid' | 'compact';
type Prefs = { group: string; sort: string; versions: string[]; layout: Layout };

function loadPrefs(): Prefs {
  const d: Prefs = { group: 'all', sort: 'last_played', versions: [], layout: 'list' };
  try { return { ...d, ...JSON.parse(localStorage.getItem('lego.profilesTab') ?? '{}') }; } catch { return d; }
}

const toVersionMajor = (v?: string | null) => {
  if (!v) return '?';
  const parts = v.split('.');
  return parts.length >= 2 ? `${parts[0]}.${parts[1]}` : parts[0]!;
};

export interface ProfilesTabProps extends ProfileCardHandlers {
  profiles: Profile[];
  selectedId: string;
  onCreate: () => void;
  onImport: () => void;
}

export function ProfilesTab(p: ProfilesTabProps) {
  const [prefs, setPrefsState] = useState(loadPrefs);
  const setPrefs = (patch: Partial<Prefs>) => setPrefsState((old) => {
    const next = { ...old, ...patch };
    try { localStorage.setItem('lego.profilesTab', JSON.stringify(next)); } catch { /* storage blocked */ }
    return next;
  });
  const [searchQuery, setSearchQuery] = useState('');
  const pins = usePins();
  const { group: activeGroup, sort: sortBy, versions: selectedVersions, layout: layoutMode } = prefs;

  const matchesGroup = (pr: Profile) => activeGroup === 'all' || (activeGroup === 'lego' ? pr.legoClient : activeGroup === 'modded' ? !!pr.loaderType && !pr.legoClient : !pr.loaderType);
  const groups: GroupTab[] = [
    { id: 'all', name: 'All', count: p.profiles.length },
    { id: 'lego', name: 'LEGO', count: p.profiles.filter((x) => x.legoClient).length },
    { id: 'modded', name: 'MODDED', count: p.profiles.filter((x) => !!x.loaderType && !x.legoClient).length },
    { id: 'vanilla', name: 'VANILLA', count: p.profiles.filter((x) => !x.loaderType).length },
  ];

  const versionOptions = useMemo(() => {
    const counts = new Map<string, number>();
    p.profiles.filter(matchesGroup).forEach((x) => { const m = toVersionMajor(x.gameVersion); counts.set(m, (counts.get(m) ?? 0) + 1); });
    return [...counts.entries()].sort((a, b) => b[0].localeCompare(a[0], undefined, { numeric: true })).map(([value, count]) => ({ value, count }));
  }, [p.profiles, activeGroup]);

  const sorted = useMemo(() => {
    const q = searchQuery.toLowerCase();
    return p.profiles
      .filter((x) => (!q || x.name.toLowerCase().includes(q) || x.gameVersion.includes(q)) && matchesGroup(x) && (selectedVersions.length === 0 || selectedVersions.includes(toVersionMajor(x.gameVersion))))
      .sort((a, b) => {
        const ap = pins.includes(a.id), bp = pins.includes(b.id);
        if (ap !== bp) return ap ? -1 : 1;
        switch (sortBy) {
          case 'name': return a.name.localeCompare(b.name);
          case 'date_created': return b.createdAt - a.createdAt;
          case 'version_newest': return b.gameVersion.localeCompare(a.gameVersion, undefined, { numeric: true }) || a.name.localeCompare(b.name);
          case 'version_oldest': return a.gameVersion.localeCompare(b.gameVersion, undefined, { numeric: true }) || a.name.localeCompare(b.name);
          default: return (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0) || b.createdAt - a.createdAt || a.name.localeCompare(b.name);
        }
      });
  }, [p.profiles, searchQuery, activeGroup, selectedVersions, sortBy, pins]);

  const sections = useMemo(() => {
    if (selectedVersions.length === 0) return [{ version: null as string | null, list: sorted }];
    const m = new Map<string, Profile[]>();
    sorted.forEach((x) => { const v = toVersionMajor(x.gameVersion); if (!m.has(v)) m.set(v, []); m.get(v)!.push(x); });
    return [...m.entries()].sort((a, b) => b[0].localeCompare(a[0], undefined, { numeric: true })).map(([version, list]) => ({ version: version as string | null, list }));
  }, [sorted, selectedVersions]);

  const actionButtons: ActionButton[] = [
    { id: 'import', label: 'IMPORT', icon: 'solar:import-bold', tooltip: 'Modpack-Datei, anderen Launcher oder Ordner importieren', onClick: p.onImport },
    { id: 'new', label: 'CREATE', icon: 'solar:widget-add-bold', tooltip: 'Neues Profil erstellen', onClick: p.onCreate },
  ];
  const gridColsClass = layoutMode === 'compact' ? 'grid-cols-3' : layoutMode === 'grid' ? 'grid-cols-2' : 'grid-cols-1';

  if (p.profiles.length === 0) {
    return <EmptyState icon="solar:widget-bold" message="Keine Profile gefunden" action={<Button variant="default" size="md" onClick={p.onCreate} icon={<Icon icon="solar:widget-add-bold" className="w-5 h-5" />}>Neues Profil erstellen</Button>} />;
  }

  return (
    <div className="h-full flex flex-col overflow-hidden p-4 relative">
      <GroupTabs groups={groups} activeGroup={activeGroup} onGroupChange={(g) => setPrefs({ group: g })} showAddButton={false} />
      <div className="mb-6 pb-4 border-b border-white/10">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 flex-1">
            <SearchWithFilters
              placeholder="Profile durchsuchen …"
              searchValue={searchQuery}
              onSearchChange={setSearchQuery}
              sortOptions={[
                { value: 'name', label: 'Name', icon: 'solar:text-bold' },
                { value: 'last_played', label: 'Zuletzt gespielt', icon: 'solar:clock-circle-bold' },
                { value: 'date_created', label: 'Erstellt', icon: 'solar:calendar-add-bold' },
                { value: 'version_newest', label: 'Version (neueste)', icon: 'solar:arrow-down-bold' },
                { value: 'version_oldest', label: 'Version (älteste)', icon: 'solar:arrow-up-bold' },
              ]}
              sortValue={sortBy}
              onSortChange={(v) => setPrefs({ sort: v })}
              dropdownSize="sm"
              filterSlot={<MultiVersionFilter options={versionOptions} selected={selectedVersions} onToggle={(v) => setPrefs({ versions: selectedVersions.includes(v) ? selectedVersions.filter((x) => x !== v) : [...selectedVersions, v] })} onClear={() => setPrefs({ versions: [] })} size="sm" />}
            />
            <button
              onClick={() => setPrefs({ layout: layoutMode === 'list' ? 'grid' : layoutMode === 'grid' ? 'compact' : 'list' })}
              className="flex items-center gap-2 px-4 py-2 bg-black/30 hover:bg-black/40 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded-lg font-smallcaps text-base transition-all duration-200 min-h-[2.5rem]"
              title={layoutMode === 'list' ? 'Rasteransicht' : layoutMode === 'grid' ? 'Kompakte Ansicht' : 'Listenansicht'}
            >
              <div className="w-4 h-8 flex items-center justify-center"><Icon icon="solar:list-bold" className="w-8 h-8" /></div>
            </button>
          </div>
          <ActionButtons actions={actionButtons} />
        </div>
      </div>
      <div className="flex-1 min-h-0 h-full overflow-y-auto custom-scrollbar pr-1" id="plist">
        {sorted.length === 0 ? <EmptyState icon="solar:widget-bold" message="Keine Profile gefunden" /> : sections.map((sec) => (
          <div key={sec.version ?? 'all'}>
            {sec.version && (
              <div className="flex items-center gap-3 mb-3">
                <div className="h-px w-6 bg-white/10" />
                <span className="font-smallcaps text-base text-white/60 whitespace-nowrap">{sec.version}</span>
                <div className="h-px flex-1 bg-white/10" />
              </div>
            )}
            <div className={`grid ${gridColsClass} gap-3 mb-3`}>
              {sec.list.map((pr) => <ProfileCardV2 key={pr.id} profile={pr} selected={pr.id === p.selectedId} layoutMode={layoutMode} h={p} />)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
