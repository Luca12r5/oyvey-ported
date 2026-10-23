// The NoRisk Client launcher's mods tab (GPL-3.0: components/tabs/ModrinthTabV2.tsx,
// modrinth/v2/ModrinthSearchV2.tsx + ModrinthSearchControlsV2.tsx), rebuilt on the
// LEGO launcher's Modrinth search and installer. What is installed is shown per profile (ProfileContent).
import { useCallback, useEffect, useRef, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { GroupTabs, type GroupTab } from '../ui/GroupTabs.tsx';
import { SearchWithFilters } from '../ui/SearchWithFilters.tsx';
import { CustomDropdown } from '../ui/CustomDropdown.tsx';
import { TagBadge } from '../ui/TagBadge.tsx';
import { EmptyState } from '../ui/EmptyState.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { ModrinthProjectCardV2 } from '../modrinth/ModrinthProjectCardV2.tsx';
import { ModrinthFilterSidebarV2 } from '../modrinth/ModrinthFilterSidebarV2.tsx';
import { useThemeStore } from '../store/theme.ts';
import { createStore, useStore } from '../store/store.ts';
import { api, errorToast, toast } from '../../ui.ts';
import type { ProjectType, SearchFilters, SearchHit } from '../../../core/modrinth.ts';
import type { GameProfile } from '../../../core/settings.ts';

export type ModsTabId = ProjectType;
type Sort = NonNullable<SearchFilters['sort']>;

interface ModsState { tab: ModsTabId; profileId: string | null; versions: string[]; categories: string[]; loaders: string[]; env: 'client' | 'server' | null; sort: Sort; sidebar: boolean }
export const modsStore = createStore<ModsState>({ tab: 'mod', profileId: null, versions: [], categories: [], loaders: [], env: null, sort: 'downloads', sidebar: true });

const TABS: [ModsTabId, string][] = [['modpack', 'Modpacks'], ['mod', 'Mods'], ['resourcepack', 'Resourcepacks'], ['shader', 'Shaders'], ['datapack', 'Datapacks']];
const CATEGORIES: Record<ProjectType, string[]> = {
  mod: ['adventure', 'decoration', 'economy', 'equipment', 'food', 'game-mechanics', 'library', 'magic', 'management', 'minigame', 'mobs', 'optimization', 'social', 'storage', 'technology', 'transportation', 'utility', 'worldgen'],
  modpack: ['adventure', 'challenging', 'combat', 'kitchen-sink', 'lightweight', 'magic', 'multiplayer', 'optimization', 'quests', 'technology'],
  resourcepack: ['16x', '32x', '64x', '128x', 'blocks', 'combat', 'cursed', 'decoration', 'entities', 'fonts', 'gui', 'items', 'realistic', 'simplistic', 'themed', 'tweaks', 'vanilla-like'],
  shader: ['atmosphere', 'bloom', 'cartoon', 'colored-lighting', 'fantasy', 'foliage', 'high', 'low', 'medium', 'path-tracing', 'pbr', 'realistic', 'reflections', 'semi-realistic', 'shadows', 'vanilla-like'],
  datapack: ['adventure', 'decoration', 'equipment', 'game-mechanics', 'magic', 'mobs', 'utility', 'worldgen'],
};
const LOADERS = ['fabric', 'quilt', 'forge', 'neoforge'];
const SORTS = [
  { value: 'relevance', label: 'Relevanz', icon: 'solar:star-bold' },
  { value: 'downloads', label: 'Downloads', icon: 'solar:download-minimalistic-bold' },
  { value: 'follows', label: 'Beliebt', icon: 'solar:heart-bold' },
  { value: 'newest', label: 'Neu', icon: 'solar:calendar-add-bold' },
  { value: 'updated', label: 'Aktualisiert', icon: 'solar:refresh-bold' },
];

let releases: string[] = [];

export interface ModsTabProps {
  profiles: GameProfile[];
  selectedProfile: string;
  curseforge: boolean;
  onEditProfile: () => void;
  onModpackInstalled: (profileId: string) => void;
}

export function ModsTab(p: ModsTabProps) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const st = useStore(modsStore, (s) => s);
  const set = modsStore.set;
  const profile = p.profiles.find((x) => x.id === (st.profileId ?? p.selectedProfile)) ?? p.profiles[0]!;
  const [q, setQ] = useState('');
  const [versions, setVersions] = useState<string[]>(releases.length ? releases : [profile.gameVersion]);

  useEffect(() => {
    if (releases.length) return;
    api.versions().then((list) => { releases = list.filter((v) => v.type === 'release').map((v) => v.id).slice(0, 60); setVersions(releases); }).catch(() => {});
  }, []);

  const groups: GroupTab[] = TABS.map(([id, name]) => ({ id, name, count: 0 }));
  const type = st.tab as ProjectType;
  const totalFilters = st.versions.length + st.loaders.length + st.categories.length + (st.env ? 1 : 0);
  const toggle = (k: 'versions' | 'loaders' | 'categories', v: string) => set({ [k]: st[k].includes(v) ? st[k].filter((x) => x !== v) : [...st[k], v] } as Partial<ModsState>);

  return (
    <div className="h-full flex flex-col overflow-hidden p-4 relative">
      <div className="flex-1 overflow-hidden flex flex-row gap-3">
        <div className="flex flex-col flex-1 overflow-hidden min-w-0">
          <GroupTabs id="tabs" groups={groups} activeGroup={st.tab} onGroupChange={(g) => { set({ tab: g as ModsTabId, categories: [] }); setQ(''); }} showAddButton={false} />
          <div className="mb-4">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 flex-1 min-w-0">
                <SearchWithFilters
                  className="flex-1 min-w-0"
                  placeholder={`${TABS.find(([i]) => i === st.tab)![1]} durchsuchen …`}
                  searchValue={q}
                  onSearchChange={setQ}
                  sortOptions={SORTS}
                  sortValue={st.sort}
                  onSortChange={(v) => set({ sort: v as Sort })}
                />
                {(
                  <button id="filt" onClick={() => set({ sidebar: !st.sidebar })} className="flex items-center gap-2 px-4 py-2 bg-black/30 hover:bg-black/40 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded-lg font-smallcaps text-base transition-all duration-200 min-h-[2.5rem]" title={st.sidebar ? 'Filter ausblenden' : 'Filter einblenden'}>
                    <div className="w-4 h-8 flex items-center justify-center"><Icon icon="solar:filter-bold" className="w-6 h-6" /></div>
                  </button>
                )}
              </div>
              {(
                <div className="flex items-center gap-1 border border-white/10 rounded-lg p-0.5">
                  <button className="flex items-center gap-1.5 px-2 py-1 rounded-md font-smallcaps text-base transition-all duration-200 min-h-[2.5rem] bg-green-400/40 text-white border border-green-300/30" title="Modrinth">
                    <Icon icon="solar:box-bold" className="w-5 h-5" /><span className="hidden sm:inline">Modrinth</span>
                  </button>
                  <button disabled={!p.curseforge} className={cn('flex items-center gap-1.5 px-2 py-1 rounded-md font-smallcaps text-base transition-all duration-200 min-h-[2.5rem] border border-transparent', p.curseforge ? 'bg-black/30 text-white/70 hover:text-white hover:bg-black/40' : 'bg-black/20 text-white/30 cursor-not-allowed')} title={p.curseforge ? 'CurseForge' : 'Für CurseForge einen API-Schlüssel unter Optionen → Spiel eintragen'}>
                    <Icon icon="solar:fire-bold" className="w-5 h-5" /><span className="hidden sm:inline">CurseForge</span>
                  </button>
                </div>
              )}
              <div className="w-56 flex-shrink-0" id="prof" title="Ziel-Profil">
                <CustomDropdown label="" variant="search" size="sm" value={profile.id} onChange={(v) => set({ profileId: v })} options={p.profiles.map((x) => ({ value: x.id, label: `${x.name} · ${x.gameVersion}`, icon: 'solar:user-id-bold' }))} />
              </div>
            </div>
            {totalFilters > 0 && (
              <div className="flex items-center gap-2 mt-4 flex-wrap">
                <TagBadge variant="destructive" size="md" className="cursor-pointer hover:brightness-110 transition-all flex-shrink-0 flex items-center" onClick={() => set({ versions: [], loaders: [], categories: [], env: null })}>
                  <Icon icon="solar:trash-bin-trash-bold" className="w-4 h-4 mr-1" /><span>Alle Filter löschen</span>
                </TagBadge>
                {[...st.versions.map((v) => ['versions', v] as const), ...st.loaders.map((v) => ['loaders', v] as const), ...st.categories.map((v) => ['categories', v] as const)].map(([k, v]) => (
                  <TagBadge key={`${k}-${v}`} variant="filter" size="md" className="inline-flex whitespace-nowrap items-center" onClick={() => toggle(k, v)}>
                    <span>{v.replace(/-/g, ' ')}</span><Icon icon="solar:close-circle-bold" className="w-4 h-4 ml-1" />
                  </TagBadge>
                ))}
                {st.env && <TagBadge variant="filter" size="md" className="inline-flex whitespace-nowrap items-center" onClick={() => set({ env: null })}><span>{st.env}</span><Icon icon="solar:close-circle-bold" className="w-4 h-4 ml-1" /></TagBadge>}
              </div>
            )}
          </div>
          <div className="search-results-area flex-1 overflow-y-auto pr-1" id="body">
            <SearchResults key={`${type}|${profile.id}`} type={type} profile={profile} profiles={p.profiles} q={q} filters={st} onEditProfile={p.onEditProfile} onModpackInstalled={p.onModpackInstalled} />
          </div>
        </div>
        {st.sidebar && (
          <ModrinthFilterSidebarV2
            accentColor={accentColor}
            versions={versions}
            selectedVersions={st.versions}
            onVersionToggle={(v) => toggle('versions', v)}
            loaders={type === 'mod' || type === 'modpack' ? LOADERS : null}
            selectedLoaders={st.loaders}
            onLoaderToggle={(l) => toggle('loaders', l)}
            categories={CATEGORIES[type] ?? []}
            selectedCategories={st.categories}
            onCategoryToggle={(c) => toggle('categories', c)}
            env={st.env}
            onEnv={type === 'mod' || type === 'modpack' ? (e) => set({ env: e }) : null}
          />
        )}
      </div>
    </div>
  );
}

function SearchResults({ type, profile, profiles, q, filters, onEditProfile, onModpackInstalled }: { type: ProjectType; profile: GameProfile; profiles: GameProfile[]; q: string; filters: ModsState; onEditProfile: () => void; onModpackInstalled: (id: string) => void }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const [hits, setHits] = useState<SearchHit[] | null>(null);
  const [total, setTotal] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [installing, setInstalling] = useState<Record<string, boolean>>({});
  const [installed, setInstalled] = useState<Record<string, boolean>>({});
  const [pick, setPick] = useState<{ hit: SearchHit; x: number; y: number } | null>(null);
  const seq = useRef(0);
  const needsLoader = type === 'mod' && !profile.loaderType && !filters.loaders.length;

  const load = useCallback(async (offset: number) => {
    const my = ++seq.current;
    if (offset === 0) setHits(null);
    try {
      const r = await api.searchProjects(profile.id, q, type, offset, { versions: filters.versions, categories: filters.categories, loaders: filters.loaders, env: filters.env, sort: filters.sort });
      if (my !== seq.current) return;
      setError(null);
      setTotal(r.total);
      setHits((old) => (offset === 0 ? r.hits : [...(old ?? []), ...r.hits]));
    } catch (e) {
      if (my === seq.current) setError(e instanceof Error ? e.message : String(e));
    }
  }, [profile.id, q, type, filters.versions, filters.categories, filters.loaders, filters.env, filters.sort]);

  // What is already in the profile (also after leaving and coming back, or another profile).
  useEffect(() => {
    let live = true;
    setInstalled({});
    if (type !== 'modpack') api.installedIds(profile.id, type).then((ids) => { if (live) setInstalled(Object.fromEntries(ids.map((id) => [id, true]))); }).catch(() => {});
    return () => { live = false; };
  }, [profile.id, type]);

  useEffect(() => {
    if (needsLoader) return;
    const t = setTimeout(() => void load(0), q ? 300 : 0);
    return () => clearTimeout(t);
  }, [load, needsLoader, q]);

  useEffect(() => {
    if (!pick) return;
    const close = () => setPick(null);
    setTimeout(() => document.addEventListener('click', close, { once: true }));
    return () => document.removeEventListener('click', close);
  }, [pick]);

  const install = async (h: SearchHit, target: string) => {
    setInstalling((s) => ({ ...s, [h.project_id]: true }));
    try {
      if (type === 'modpack') {
        const r = await api.installModpack(h.project_id, h.title);
        toast(`${h.title}: als Profil importiert`, 'ok');
        onModpackInstalled(r.profileId);
        return;
      }
      const titles = await api.installProject(target, h.project_id, type, h.title, h.icon_url);
      const name = profiles.find((x) => x.id === target)?.name ?? '';
      toast(`Installiert: ${titles.join(', ')} → ${name}${type === 'datapack' ? ' (Ordner „datapacks“, in eine Welt kopieren)' : ''}`, 'ok');
      if (target === profile.id) setInstalled((s) => ({ ...s, [h.project_id]: true }));
    } catch (e) {
      errorToast(e);
    } finally {
      setInstalling((s) => ({ ...s, [h.project_id]: false }));
    }
  };

  if (needsLoader) {
    return <EmptyState icon="solar:box-bold" message="Dieses Profil ist Vanilla" description="Für Mods braucht das Profil einen Mod-Loader (z. B. Fabric)." smallDescription action={<Button size="sm" variant="3d" onClick={onEditProfile}>Profil bearbeiten</Button>} />;
  }
  if (error && !hits?.length) return <p className="p-4 text-red-400 text-center font-minecraft">Modrinth nicht erreichbar: {error}</p>;
  if (hits === null) return <div className="space-y-1">{[0, 1, 2, 3, 4].map((i) => <div key={i} className="h-[106px] rounded-lg bg-black/20 border border-white/10 animate-skeleton-pulse" />)}</div>;
  if (hits.length === 0) return <p className="p-4 text-center text-sm text-gray-400 font-minecraft">Keine Treffer.</p>;
  return (
    <>
      <div className="space-y-1">
        {hits.map((h) => <ModrinthProjectCardV2 key={h.project_id} hit={h} accentColor={accentColor} installed={!!installed[h.project_id]} installing={!!installing[h.project_id]} onInstall={(x) => void install(x, profile.id)} onPickProfile={(x, a) => { const r = a.getBoundingClientRect(); setPick({ hit: x, x: Math.max(8, r.right - 260), y: r.bottom + 4 }); }} />)}
      </div>
      {hits.length < total && <div className="flex justify-center py-3"><Button id="more" size="sm" variant="flat" onClick={() => void load(hits.length)}>Mehr laden</Button></div>}
      {pick && (
        <div className="nr fixed z-[1000] w-[260px] bg-black/90 backdrop-blur-sm border border-white/20 rounded-lg shadow-xl overflow-hidden" style={{ left: pick.x, top: pick.y }} onClick={(e) => e.stopPropagation()}>
          <div className="px-3 py-2 border-b border-white/10 text-white/60 font-minecraft text-xs">Installieren in</div>
          <div className="py-1 max-h-72 overflow-y-auto no-scrollbar">
            {profiles.map((x) => (
              <button key={x.id} data-to={x.id} onClick={() => { setPick(null); void install(pick.hit, x.id); }} className="w-full flex items-center gap-2.5 px-3 py-2 text-left font-minecraft text-sm text-white/80 hover:bg-white/5 hover:text-white">
                <Icon icon="solar:user-id-bold" className="w-4 h-4 text-white/70" /><span className="flex-1 truncate">{x.name}</span><span className="text-white/40 text-xs">{x.gameVersion}</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </>
  );
}
