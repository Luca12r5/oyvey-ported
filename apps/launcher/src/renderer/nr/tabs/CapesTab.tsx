// Ported from the NoRisk Client launcher (GPL-3.0): components/capes/CapeBrowser.tsx
// (tabs, search bar) and CapeList.tsx (cape cards). Capes are the LEGO catalog's,
// equipped through the LEGO server; "Vanilla" shows the account's Minecraft cape.
import { useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { GroupTabs } from '../ui/GroupTabs.tsx';
import { SearchWithFilters } from '../ui/SearchWithFilters.tsx';
import { ActionButtons } from '../ui/ActionButtons.tsx';
import { EmptyState } from '../ui/EmptyState.tsx';
import { Tooltip } from '../ui/Tooltip.tsx';
import { useThemeStore } from '../store/theme.ts';

export type CapeTab = 'all' | 'mine' | 'fav' | 'vanilla';
export interface CapeCard { id: string; name: string; art: string; sub: string; subColor?: string; owned: boolean; equipped: boolean; fav: boolean }

export interface CapesTabProps {
  tab: CapeTab;
  capes: CapeCard[];
  noneEquipped: boolean;
  vanilla: { url: string | null; signedIn: boolean };
  emptyText: string;
  onTab: (t: CapeTab) => void;
  onClick: (id: string | null) => void;
  onFav: (id: string) => void;
  onTemplate: () => void;
}

const W = 120;
const H = 192;

function CapeItem({ c, onClick, onFav }: { c: CapeCard; onClick: () => void; onFav?: () => void }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const [isHovered, setIsHovered] = useState(false);
  return (
    <div
      data-cape={c.id}
      className={cn('nr-cape-card group relative flex flex-col gap-3 p-4 rounded-lg bg-black/20 border border-white/10 hover:border-white/20 transition-all duration-200 cursor-pointer nr-pop', !c.owned && c.id !== 'none' && 'opacity-80')}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={(e) => { e.preventDefault(); onClick(); }}
    >
      {onFav && (
        <div className="absolute top-3 right-3 z-20 flex flex-col gap-1">
          <button data-fav={c.id} onClick={(e) => { e.preventDefault(); e.stopPropagation(); onFav(); }} className="w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-black/50 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded transition-all duration-200" title={c.fav ? 'Aus Favoriten entfernen' : 'Favorit'}>
            <Icon icon={c.fav ? 'solar:heart-bold' : 'solar:heart-linear'} className="w-4 h-4" style={{ color: c.fav ? '#ef4444' : undefined }} />
          </button>
        </div>
      )}
      <div className="flex flex-col items-center gap-3 relative z-10 w-full">
        <div className="relative flex-shrink-0 rounded-lg flex items-center justify-center overflow-hidden border-2 transition-all duration-300 ease-out"
          style={{ width: `${W}px`, height: `${H}px`, backgroundColor: isHovered ? `${accentColor.value}20` : 'transparent', borderColor: c.equipped ? accentColor.value : isHovered ? `${accentColor.value}60` : 'transparent' }}>
          {c.id === 'none'
            ? <div className="w-full h-full flex items-center justify-center bg-white/5"><Icon icon="solar:close-circle-bold-duotone" className="w-10 h-10 text-white/20" /></div>
            : c.art.startsWith('data:') ? <img src={c.art} alt="" className="w-full h-full object-contain" style={{ imageRendering: 'pixelated' }} />
              : <div className="nr-cape-art w-full h-full" dangerouslySetInnerHTML={{ __html: c.art }} />}
          {c.equipped && (
            <div className="absolute top-2 right-2 z-30">
              <Tooltip content="Angelegt"><Icon icon="solar:check-circle-bold" className="w-4 h-4" style={{ color: accentColor.value }} /></Tooltip>
            </div>
          )}
          {!c.owned && c.id !== 'none' && c.id !== 'vanilla' && <div className="absolute bottom-0 inset-x-0 z-20 flex items-center justify-center gap-1.5 py-1.5 backdrop-blur-sm bg-black/40 border-t border-white/10"><Icon icon="solar:lock-keyhole-bold" className="w-3.5 h-3.5 text-white/70" /><span className="text-[11px] font-minecraft lowercase text-white/70">im Shop</span></div>}
        </div>
        <div className="flex-grow min-w-0 w-full text-center">
          <h3 className="font-minecraft text-white text-base whitespace-nowrap overflow-hidden text-ellipsis max-w-full normal-case" title={c.name}>{c.name}</h3>
          <div className="text-xs font-minecraft mt-0.5" style={{ color: c.subColor ?? 'rgba(255,255,255,0.6)' }}>{c.sub}</div>
        </div>
      </div>
    </div>
  );
}

export function CapesTab(p: CapesTabProps) {
  const [q, setQ] = useState('');
  const query = q.trim().toLowerCase();
  const list = p.capes.filter((c) => !query || c.name.toLowerCase().includes(query));
  return (
    <div className="h-full flex flex-col overflow-hidden p-4 relative">
      <div className="flex-1 overflow-y-auto no-scrollbar">
        <GroupTabs id="ctabs" groups={[{ id: 'all', name: 'all', count: 0 }, { id: 'mine', name: 'my capes', count: 0 }, { id: 'fav', name: 'favorites', count: 0 }, { id: 'vanilla', name: 'vanilla', count: 0 }]} activeGroup={p.tab} onGroupChange={(g) => p.onTab(g as CapeTab)} showAddButton={false} />
        <div className="mb-6 pb-4 border-b border-white/10">
          <div className="flex items-center gap-4">
            <div className="flex-1">{p.tab !== 'vanilla' && <SearchWithFilters placeholder="Capes durchsuchen …" searchValue={q} onSearchChange={setQ} />}</div>
            <ActionButtons actions={[
              { id: 'tpl', label: 'TEMPLATE', icon: 'solar:download-minimalistic-bold', tooltip: '64×32-Vorlage herunterladen', onClick: p.onTemplate },
              { id: 'up', label: 'UPLOAD', icon: 'solar:upload-bold', tooltip: 'Eigene Capes brauchen eine Moderation auf dem LEGO-Server – kommt später.', disabled: true, onClick: () => {} },
            ]} />
          </div>
        </div>
        {p.tab === 'vanilla' ? (
          p.vanilla.url
            ? <div className="grid grid-cols-[repeat(auto-fill,minmax(170px,1fr))] gap-4" id="cgrid"><CapeItem c={{ id: 'vanilla', name: 'Minecraft-Cape', art: p.vanilla.url, sub: 'Offiziell · aktiv', owned: true, equipped: true, fav: false }} onClick={() => {}} /></div>
            : <EmptyState icon="solar:shop-bold" message={p.vanilla.signedIn ? 'Kein offizielles Cape aktiv' : 'Melde dich an, um deine Minecraft-Capes zu sehen'} />
        ) : (
          <div className="grid grid-cols-[repeat(auto-fill,minmax(170px,1fr))] gap-4" id="cgrid">
            {p.tab === 'all' && !query && <CapeItem c={{ id: 'none', name: 'No Cape', art: '', sub: ' ', owned: true, equipped: p.noneEquipped, fav: false }} onClick={() => p.onClick(null)} />}
            {list.map((c) => <CapeItem key={c.id} c={c} onClick={() => p.onClick(c.id)} onFav={() => p.onFav(c.id)} />)}
            {list.length === 0 && <div className="col-span-full"><EmptyState icon="solar:shop-bold" message={p.emptyText} fullHeight={false} compact /></div>}
          </div>
        )}
      </div>
    </div>
  );
}
