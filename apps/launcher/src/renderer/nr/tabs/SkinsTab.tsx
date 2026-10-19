// Ported from the NoRisk Client launcher (GPL-3.0): components/tabs/SkinsTab.tsx
// (SkinPreview, AddSkinCard, layout). Previews are rendered by the LEGO launcher's
// skinview3d poster renderer; the skin library and upload logic live in pages/skins.ts.
import { memo, useEffect, useState, type CSSProperties } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { SearchWithFilters } from '../ui/SearchWithFilters.tsx';
import { Button } from '../ui/buttons/Button.tsx';
import { useThemeStore } from '../store/theme.ts';
import { skinSprite, SPRITE_FRAMES } from '../../avatar.ts';
import type { SkinEntry } from '../../../core/design.ts';

const PREVIEW_WIDTH = 176;
const PREVIEW_HEIGHT = 280;

function useSprite(data: string | null, variant: 'classic' | 'slim'): string | null {
  const [url, setUrl] = useState<string | null>(null);
  useEffect(() => {
    let off = false;
    skinSprite(data, variant, PREVIEW_WIDTH, PREVIEW_HEIGHT).then((u) => { if (!off) setUrl(u); }).catch(() => { if (!off) setUrl(''); });
    return () => { off = true; };
  }, [data, variant]);
  return url;
}

const SkinPreview = memo(function SkinPreview({ skin, isApplied, onClick, onEditSkin, onDeleteSkin, onKeep, subtitle }: {
  skin: SkinEntry; isApplied: boolean; onClick: (s: SkinEntry) => void; onEditSkin?: (s: SkinEntry) => void; onDeleteSkin?: (s: SkinEntry) => void; onKeep?: () => void; subtitle?: string;
}) {
  const [isHovered, setIsHovered] = useState(false);
  const accentColor = useThemeStore((s) => s.accentColor);
  const url = useSprite(skin.data, skin.variant);
  return (
    <div
      data-id={skin.id}
      className="nr-skin-card relative flex flex-col gap-3 p-4 rounded-lg bg-black/20 border border-white/10 hover:border-white/20 transition-all duration-200 cursor-pointer nr-pop"
      style={{ backgroundColor: isHovered ? `${accentColor.value}20` : undefined, borderColor: isHovered ? `${accentColor.value}60` : undefined }}
      onClick={() => onClick(skin)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="absolute top-3 right-3 z-20 flex flex-col gap-1">
        {onKeep && (
          <button data-keep="1" onClick={(e) => { e.preventDefault(); e.stopPropagation(); onKeep(); }} className="w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-black/50 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded transition-all duration-200" title="In Bibliothek speichern">
            <Icon icon="solar:add-circle-bold" className="w-4 h-4" />
          </button>
        )}
        {onEditSkin && (
          <button data-edit={skin.id} onClick={(e) => { e.preventDefault(); e.stopPropagation(); onEditSkin(skin); }} className="w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-black/50 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded transition-all duration-200" title="Name / Modell ändern">
            <Icon icon="solar:pen-bold" className="w-4 h-4" />
          </button>
        )}
        {onDeleteSkin && (
          <button data-del={skin.id} onClick={(e) => { e.preventDefault(); e.stopPropagation(); onDeleteSkin(skin); }} className="w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-red-700/80 text-white/70 hover:text-white border border-white/10 hover:border-white/20 rounded transition-all duration-200" title="Skin löschen">
            <Icon icon="solar:trash-bin-trash-bold" className="w-4 h-4" />
          </button>
        )}
      </div>
      <div className="flex flex-col items-center gap-3 relative z-10 w-full">
        <div className="relative flex-shrink-0 rounded-lg flex items-center justify-center overflow-hidden border border-transparent" style={{ width: `${PREVIEW_WIDTH}px`, height: `${PREVIEW_HEIGHT}px` }}>
          {url === null ? <div className="w-8 h-8 border-4 border-t-transparent rounded-full animate-spin" style={{ borderColor: accentColor.value, borderTopColor: 'transparent' }} />
            : url ? <div className="nr-skin-anim w-full h-full" style={{ backgroundImage: `url(${url})`, ['--frames' as string]: SPRITE_FRAMES } as CSSProperties} /> : <Icon icon="solar:emoji-funny-circle-bold" className="w-16 h-16 text-white/30" />}
        </div>
        <div className="flex-grow min-w-0 w-full text-center">
          <h3 className="font-minecraft text-white text-base whitespace-nowrap overflow-hidden text-ellipsis max-w-full normal-case mb-1" title={skin.name}>{skin.name}</h3>
          <div className="flex items-center justify-center gap-2 text-xs font-minecraft">
            <div className="text-white/60 flex items-center gap-1"><Icon icon="solar:palette-bold" className="w-3 h-3 text-white/50" /><span>{subtitle ?? (skin.variant === 'slim' ? 'Slim' : 'Classic')}</span></div>
            {isApplied && <><div className="w-px h-3 bg-white/30" /><div className="text-green-400 flex items-center gap-1"><Icon icon="solar:check-circle-bold" className="w-3 h-3" /><span>Aktiv</span></div></>}
          </div>
        </div>
      </div>
    </div>
  );
});

function AddSkinCard({ onClick }: { onClick: () => void }) {
  const [isHovered, setIsHovered] = useState(false);
  const accentColor = useThemeStore((s) => s.accentColor);
  return (
    <div id="addCard" className="relative flex flex-col gap-3 p-4 rounded-lg bg-black/20 border border-dashed border-white/10 hover:border-white/30 transition-all duration-200 cursor-pointer nr-pop" onClick={onClick} onMouseEnter={() => setIsHovered(true)} onMouseLeave={() => setIsHovered(false)}>
      <div className="flex flex-col items-center gap-3 relative z-10 w-full">
        <div className="relative flex-shrink-0 rounded-lg flex items-center justify-center overflow-hidden border border-transparent" style={{ width: `${PREVIEW_WIDTH}px`, height: `${PREVIEW_HEIGHT}px` }}>
          <div className="absolute inset-0 flex items-center justify-center">
            <Icon icon="solar:add-circle-bold" className="w-16 h-16 text-white/50 transition-all duration-200" style={{ color: isHovered ? accentColor.value : undefined, transform: isHovered ? 'scale(1.1)' : undefined }} />
          </div>
        </div>
        <div className="flex-grow min-w-0 w-full text-center">
          <h3 className="font-minecraft text-white text-base whitespace-nowrap overflow-hidden text-ellipsis max-w-full normal-case mb-1">Add New Skin</h3>
          <div className="flex items-center justify-center gap-2 text-xs font-minecraft"><div className="text-white/60 flex items-center gap-1"><Icon icon="solar:upload-bold" className="w-3 h-3 text-white/50" /><span>Hochladen oder importieren</span></div></div>
        </div>
      </div>
    </div>
  );
}

export interface SkinsTabProps {
  signedIn: boolean;
  accountName: string;
  library: SkinEntry[];
  current: { skin: string | null; variant: 'classic' | 'slim' };
  guestName: string;
  onAdd: () => void;
  onPhoto: () => void;
  onDropFile: (f: File) => void;
  onApply: (s: SkinEntry) => void;
  onEdit: (s: SkinEntry) => void;
  onDelete: (s: SkinEntry) => void;
  onKeepCurrent: () => void;
  onGuestName: (name: string) => void;
}

export function SkinsTab(p: SkinsTabProps) {
  const [search, setSearch] = useState('');
  const [guest, setGuest] = useState(p.guestName);
  const q = search.trim().toLowerCase();
  const filtered = p.library.filter((s) => !q || s.name.toLowerCase().includes(q));
  const showCurrent = !!p.current.skin && !p.library.some((s) => s.data === p.current.skin);

  return (
    <div className="h-full flex flex-col overflow-hidden p-4 relative" onDragOver={(e) => e.preventDefault()} onDrop={(e) => { e.preventDefault(); const f = e.dataTransfer.files[0]; if (f) p.onDropFile(f); }}>
      <div className="flex-1 overflow-y-auto no-scrollbar">
        <div className="mb-6 pb-4 border-b border-white/10">
          <div className="flex items-center gap-4">
            <div className="flex-1"><SearchWithFilters placeholder="Skins durchsuchen …" searchValue={search} onSearchChange={setSearch} /></div>
            <div className="flex items-center gap-3">
              <Button id="photoSkin" variant="flat" size="sm" icon={<Icon icon="solar:palette-bold" className="w-5 h-5" />} onClick={p.onPhoto}>AUS FOTO (KI)</Button>
              <Button id="add" variant="flat" size="sm" icon={<Icon icon="solar:add-circle-bold" className="w-5 h-5" />} onClick={p.onAdd}>ADD SKIN</Button>
            </div>
          </div>
          {!p.signedIn && (
            <div className="flex items-center gap-3 mt-4 text-sm font-minecraft text-white/70">
              <Icon icon="solar:info-circle-bold" className="w-4 h-4" />
              <span className="flex-1">Als Gast: Minecraft-Namen eintragen, um deinen Skin zu sehen (nur Anzeige).</span>
              <div className="flex items-center gap-2 bg-black/50 rounded-lg border border-white/10 px-3 py-2"><input id="gname" value={guest} maxLength={16} placeholder="z. B. LegoCpvp" onChange={(e) => setGuest(e.target.value)} className="bg-transparent text-white placeholder-white/50 text-sm w-40 outline-none" /></div>
              <Button id="gload" size="xs" variant="flat" onClick={() => p.onGuestName(guest.trim())}>Laden</Button>
            </div>
          )}
        </div>
        <div className="space-y-5 text-center">
          {p.library.length > 0 && filtered.length === 0 ? <p className="text-white/70 italic font-smallcaps text-xs">Keine Skins passen zur Suche.</p> : (
            <div className="grid grid-cols-[repeat(auto-fill,minmax(220px,1fr))] gap-4" id="grid">
              <AddSkinCard onClick={p.onAdd} />
              {showCurrent && <SkinPreview skin={{ id: 'current', name: p.accountName || 'Aktiver Skin', data: p.current.skin!, variant: p.current.variant }} subtitle="Aktiv in Minecraft" isApplied onClick={() => {}} onKeep={p.onKeepCurrent} />}
              {filtered.map((s) => <SkinPreview key={s.id} skin={s} isApplied={s.data === p.current.skin} onClick={p.onApply} onEditSkin={p.onEdit} onDeleteSkin={p.onDelete} />)}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
