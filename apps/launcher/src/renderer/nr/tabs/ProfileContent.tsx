// One profile's content, like the profile view of the NoRisk Client launcher (GPL-3.0):
// tabs for everything installed in it (mods, resource packs, shaders, datapacks,
// worlds, screenshots), each filterable, with toggle, delete (to the trash) and "add".
import { useCallback, useEffect, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { GroupTabs, type GroupTab } from '../ui/GroupTabs.tsx';
import { SearchWithFilters } from '../ui/SearchWithFilters.tsx';
import { EmptyState } from '../ui/EmptyState.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { ActionButton } from '../ui/ActionButton.tsx';
import { ToggleSwitch } from '../ui/ToggleSwitch.tsx';
import { TagBadge } from '../ui/TagBadge.tsx';
import { useThemeStore } from '../store/theme.ts';
import { launchProfile } from '../store/game.ts';
import { api, confirm, errorToast } from '../../ui.ts';
import type { GameProfile } from '../../../core/settings.ts';
import type { ContentEntry, ContentKind } from '../../../core/content.ts';
import type { ModEntry } from '../../../ipc-types.ts';
import type { ProjectType } from '../../../core/modrinth.ts';

type Kind = 'mods' | ContentKind;
type Filter = 'all' | 'on' | 'off';
type Entry = ContentEntry & { preview: string | null };

const KINDS: { id: Kind; name: string; icon: string; add: ProjectType | null; empty: string }[] = [
  { id: 'mods', name: 'Mods', icon: 'solar:box-bold', add: 'mod', empty: 'Noch keine Mods in diesem Profil' },
  { id: 'resourcepacks', name: 'Resourcepacks', icon: 'solar:palette-bold', add: 'resourcepack', empty: 'Keine Resourcepacks installiert' },
  { id: 'shaderpacks', name: 'Shader', icon: 'solar:sun-bold', add: 'shader', empty: 'Keine Shader installiert' },
  { id: 'datapacks', name: 'Datapacks', icon: 'solar:layers-bold', add: 'datapack', empty: 'Keine Datapacks installiert' },
  { id: 'saves', name: 'Welten', icon: 'solar:global-bold', add: null, empty: 'Noch keine Welten: starte das Spiel und erstelle eine' },
  { id: 'screenshots', name: 'Screenshots', icon: 'solar:gallery-bold', add: null, empty: 'Noch keine Screenshots (F2 im Spiel)' },
];

const size = (b: number) => (b <= 0 ? '' : b > 1048576 ? `${(b / 1048576).toFixed(1)} MB` : `${Math.max(1, Math.round(b / 1024))} KB`);
const date = (ms: number) => new Date(ms).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });

export interface ProfileContentProps {
  profile: GameProfile;
  initialKind?: Kind;
  onBack: () => void;
  onEdit: () => void;
  /** Opens the Modrinth search for this profile and type. */
  onAdd: (type: ProjectType) => void;
}

export function ProfileContent(p: ProfileContentProps) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const [kind, setKind] = useState<Kind>(p.initialKind ?? 'mods');
  const [q, setQ] = useState('');
  const [filter, setFilter] = useState<Filter>('all');
  const [counts, setCounts] = useState<Partial<Record<Kind, number>>>({});
  const [rev, setRev] = useState(0);
  const reload = useCallback(() => setRev((r) => r + 1), []);

  useEffect(() => { api.contentCounts(p.profile.id).then(setCounts).catch(() => {}); }, [p.profile.id, rev]);

  const k = KINDS.find((x) => x.id === kind)!;
  const groups: GroupTab[] = KINDS.map((x) => ({ id: x.id, name: x.name, count: counts[x.id] ?? 0 }));
  const toggleable = kind !== 'saves' && kind !== 'screenshots';

  return (
    <div className="h-full flex flex-col overflow-hidden p-4" id="profileContent">
      <div className="flex items-center gap-4 mb-4">
        <button id="pcBack" onClick={p.onBack} title="Zurück" className="w-10 h-10 flex items-center justify-center rounded-lg bg-black/30 hover:bg-black/50 border border-white/10 hover:border-white/20 text-white/80 hover:text-white">
          <Icon icon="solar:arrow-left-bold" className="w-5 h-5" />
        </button>
        <div className="flex-1 min-w-0">
          <div className="text-white font-minecraft text-2xl truncate normal-case">{p.profile.name}</div>
          <div className="text-white/70 text-sm font-minecraft">
            Minecraft {p.profile.gameVersion}{p.profile.loaderType ? ` · ${p.profile.loaderType}` : ' · Vanilla'}{p.profile.legoClient ? ' · LEGO Client' : ''}
          </div>
        </div>
        <Button variant="secondary" size="md" onClick={p.onEdit} icon={<Icon icon="solar:settings-bold" className="w-5 h-5" />}>BEARBEITEN</Button>
        <Button variant="default" size="md" onClick={() => void launchProfile(p.profile.id)} icon={<Icon icon="solar:play-bold" className="w-5 h-5" />}>PLAY</Button>
      </div>
      <GroupTabs id="pcTabs" groups={groups} activeGroup={kind} onGroupChange={(g) => { setKind(g as Kind); setQ(''); setFilter('all'); }} showAddButton={false} />
      <div className="flex items-center gap-3 mb-4">
        <SearchWithFilters className="flex-1 min-w-0" placeholder={`${k.name} durchsuchen …`} searchValue={q} onSearchChange={setQ} showSort={false} />
        {toggleable && (
          <div className="flex items-center gap-1 border border-white/10 rounded-lg p-0.5" id="pcFilter">
            {([['all', 'Alle'], ['on', 'Aktiv'], ['off', 'Aus']] as const).map(([id, label]) => (
              <button key={id} data-filter={id} onClick={() => setFilter(id)} className={cn('px-3 py-1 rounded-md font-smallcaps text-base min-h-[2.5rem] border', filter === id ? 'text-white' : 'border-transparent text-white/70 hover:text-white bg-black/20')} style={filter === id ? { backgroundColor: `${accentColor.value}40`, borderColor: `${accentColor.value}90` } : undefined}>{label}</button>
            ))}
          </div>
        )}
        <Button variant="secondary" size="md" onClick={() => void api.openFolder(kind, p.profile.id)} icon={<Icon icon="solar:folder-bold" className="w-5 h-5" />}>ORDNER</Button>
        {k.add && <Button id="pcAdd" variant="default" size="md" onClick={() => p.onAdd(k.add!)} icon={<Icon icon="solar:add-circle-bold" className="w-5 h-5" />}>HINZUFÜGEN</Button>}
      </div>
      <div className="flex-1 overflow-y-auto pr-1" id="pcBody">
        {kind === 'mods'
          ? <ModList key={`m${p.profile.id}${rev}`} profile={p.profile} q={q} filter={filter} empty={k.empty} onChange={reload} />
          : <FileList key={`${kind}${p.profile.id}${rev}`} profile={p.profile} kind={kind} q={q} filter={filter} empty={k.empty} icon={k.icon} onChange={reload} />}
      </div>
    </div>
  );
}

function Skeleton() {
  return <div className="space-y-1">{[0, 1, 2].map((i) => <div key={i} className="h-[74px] rounded-lg bg-black/20 border border-white/10 animate-skeleton-pulse" />)}</div>;
}

const matches = (q: string, ...s: (string | null | undefined)[]) => { const f = q.trim().toLowerCase(); return !f || s.some((x) => (x ?? '').toLowerCase().includes(f)); };
const passes = (f: Filter, enabled: boolean) => f === 'all' || (f === 'on') === enabled;

function Row({ icon, image, title, sub, badge, enabled, children }: { icon: string; image?: string | null; title: string; sub: string; badge?: string; enabled: boolean; children?: React.ReactNode }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  return (
    <div className={cn('nr-mod-row relative flex items-center gap-4 p-3 rounded-lg bg-black/30 border border-white/10 hover:border-white/20 transition-colors duration-200', !enabled && 'opacity-70')}>
      <div className="relative w-12 h-12 flex-shrink-0 rounded-md overflow-hidden border" style={{ borderColor: `${accentColor.value}30`, backgroundColor: `${accentColor.value}10` }}>
        {image ? <img src={image} alt="" loading="lazy" className="w-full h-full object-cover" style={{ imageRendering: 'pixelated' }} /> : <div className="w-full h-full flex items-center justify-center"><Icon icon={icon} className="w-6 h-6 text-white/60" /></div>}
      </div>
      <div className="flex-1 min-w-0">
        <div className="text-white font-minecraft text-base truncate normal-case">{title}</div>
        <div className="text-sm text-white/65 truncate">{sub}</div>
        {badge && <TagBadge variant="info" size="sm" className="mt-1">{badge}</TagBadge>}
      </div>
      <div className="flex items-center gap-3">{children}</div>
    </div>
  );
}

function ModList({ profile, q, filter, empty, onChange }: { profile: GameProfile; q: string; filter: Filter; empty: string; onChange: () => void }) {
  const [mods, setMods] = useState<ModEntry[] | null>(null);
  useEffect(() => { api.mods(profile.id).then(setMods).catch((e) => { errorToast(e); setMods([]); }); }, [profile.id]);
  if (mods === null) return <Skeleton />;
  if (mods.length === 0) return <EmptyState icon="solar:box-bold" message={profile.loaderType ? empty : 'Vanilla-Profil ohne Mods'} />;
  const list = mods.filter((m) => matches(q, m.title, m.filename) && passes(filter, m.enabled));
  if (!list.length) return <EmptyState icon="solar:magnifer-bold" message="Nichts gefunden" />;
  return (
    <div className="space-y-1">
      {list.map((m) => (
        <Row key={m.filename} icon="solar:box-bold" image={m.iconUrl} title={m.title ?? m.filename} sub={m.filename} badge={m.managedByLauncher ? 'vom Launcher verwaltet' : undefined} enabled={m.enabled}>
          {!m.managedByLauncher && <>
            <ToggleSwitch checked={m.enabled} size="sm" onChange={async (v) => { try { await api.toggleMod(profile.id, m.filename, v); onChange(); } catch (e) { errorToast(e); } }} />
            <ActionButton icon="solar:trash-bin-trash-bold" variant="destructive" tooltip="Entfernen" size="sm" onClick={async () => { if (!(await confirm('Entfernen', `${m.filename} wird gelöscht.`, 'Entfernen', true))) return; try { await api.removeMod(profile.id, m.filename); onChange(); } catch (e) { errorToast(e); } }} />
          </>}
        </Row>
      ))}
    </div>
  );
}

function FileList({ profile, kind, q, filter, empty, icon, onChange }: { profile: GameProfile; kind: ContentKind; q: string; filter: Filter; empty: string; icon: string; onChange: () => void }) {
  const [items, setItems] = useState<Entry[] | null>(null);
  useEffect(() => { api.content(profile.id, kind).then(setItems).catch((e) => { errorToast(e); setItems([]); }); }, [profile.id, kind]);
  if (items === null) return <Skeleton />;
  if (items.length === 0) return <EmptyState icon={icon} message={empty} />;
  const list = items.filter((x) => matches(q, x.title, x.name) && passes(filter, x.enabled));
  if (!list.length) return <EmptyState icon="solar:magnifer-bold" message="Nichts gefunden" />;
  const remove = async (x: Entry) => {
    const what = kind === 'saves' ? `Die Welt „${x.title}“ kommt in den Papierkorb.` : `${x.name} kommt in den Papierkorb.`;
    if (!(await confirm('Entfernen', what, 'Entfernen', true))) return;
    try { await api.removeContent(profile.id, kind, x.name); onChange(); } catch (e) { errorToast(e); }
  };
  if (kind === 'screenshots') {
    return (
      <div className="grid gap-3" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))' }}>
        {list.map((x) => (
          <div key={x.name} className="group rounded-lg overflow-hidden bg-black/30 border border-white/10 hover:border-white/25">
            <button className="block w-full aspect-video bg-black/40" onClick={() => void api.showContent(profile.id, kind, x.name)} title="Im Ordner zeigen">
              {x.preview ? <img src={x.preview} alt="" loading="lazy" className="w-full h-full object-cover" /> : <Icon icon="solar:gallery-bold" className="w-8 h-8 text-white/40 mx-auto" />}
            </button>
            <div className="flex items-center gap-2 p-2">
              <div className="flex-1 min-w-0 text-sm text-white/85 truncate">{date(x.modified)}</div>
              <ActionButton icon="solar:trash-bin-trash-bold" variant="destructive" tooltip="In den Papierkorb" size="sm" onClick={() => void remove(x)} />
            </div>
          </div>
        ))}
      </div>
    );
  }
  return (
    <div className="space-y-1">
      {list.map((x) => (
        <Row key={x.name} icon={icon} image={x.preview} title={x.title} enabled={x.enabled}
          sub={[kind === 'saves' ? `Zuletzt gespielt ${date(x.modified)}` : x.folder ? 'Ordner' : x.name, size(x.size)].filter(Boolean).join(' · ')}>
          {kind !== 'saves' && <ToggleSwitch checked={x.enabled} size="sm" onChange={async (v) => { try { await api.toggleContent(profile.id, kind, x.name, v); onChange(); } catch (e) { errorToast(e); } }} />}
          <ActionButton icon="solar:folder-bold" tooltip="Im Ordner zeigen" size="sm" onClick={() => void api.showContent(profile.id, kind, x.name)} />
          <ActionButton icon="solar:trash-bin-trash-bold" variant="destructive" tooltip="In den Papierkorb" size="sm" onClick={() => void remove(x)} />
        </Row>
      ))}
    </div>
  );
}
