// Ported from the NoRisk Client launcher (GPL-3.0): components/profiles/ProfileCardV2.tsx.
// Wired to the LEGO launcher's profiles (pixel icons, LEGO Client flag, launch, mods page).
import { useRef, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { useThemeStore } from '../store/theme.ts';
import { ProfileIconV2 } from './ProfileIconV2.tsx';
import { ProfileActionButtons, type ProfileActionButton } from '../ui/ProfileActionButtons.tsx';
import { SettingsContextMenu, type ContextMenuItem } from '../ui/SettingsContextMenu.tsx';
import { Tooltip } from '../ui/Tooltip.tsx';
import { togglePin, usePins } from '../store/pins.ts';
import { launchProfile, useGame } from '../store/game.ts';
import { pixel } from '../../icons.ts';
import type { GameProfile as Profile } from '../../../core/settings.ts';

const LOADER: Record<string, string> = { fabric: 'Fabric', quilt: 'Quilt', forge: 'Forge', neoforge: 'NeoForge' };

export interface ProfileCardHandlers {
  onMods: (p: Profile) => void;
  onEdit: (p: Profile) => void;
  onDelete: (p: Profile) => void;
  onOpenFolder: (p: Profile) => void;
  onExport: (p: Profile) => void;
  onDuplicate: (p: Profile) => void;
  onSelect: (p: Profile) => void;
  /** Adds Vivecraft (real VR) to the profile. */
  onAddVr?: (p: Profile) => void;
}

function formatLastPlayed(ms: number | null): string {
  if (!ms) return 'Nie gespielt';
  const min = Math.floor((Date.now() - ms) / 60000);
  if (min < 1) return 'Gerade eben';
  if (min < 60) return `vor ${min} Min.`;
  const h = Math.floor(min / 60);
  if (h < 24) return `vor ${h} Std.`;
  const d = Math.floor(h / 24);
  if (d < 7) return `vor ${d} ${d === 1 ? 'Tag' : 'Tagen'}`;
  const w = Math.floor(d / 7);
  if (w < 4) return `vor ${w} Wo.`;
  const mo = Math.floor(d / 30);
  if (mo < 12) return `vor ${mo} Mon.`;
  return `vor ${Math.floor(d / 365)} J.`;
}

function Px({ id, size = 'xs' }: { id: string; size?: 'xs' | 'sm' }) {
  return <span className="nr-px inline-flex" dangerouslySetInnerHTML={{ __html: pixel(id, size).__raw }} />;
}

export function ProfileCardV2({ profile, selected, layoutMode = 'list', h }: { profile: Profile; selected: boolean; layoutMode?: 'list' | 'grid' | 'compact'; h: ProfileCardHandlers }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const pins = usePins();
  const pinned = pins.includes(profile.id);
  const [isHovered, setIsHovered] = useState(false);
  const [isContextMenuOpen, setIsContextMenuOpen] = useState(false);
  const [contextMenuPosition, setContextMenuPosition] = useState({ x: 0, y: 0 });
  const settingsButtonRef = useRef<HTMLButtonElement>(null);
  const launching = useGame((s) => s.launching);
  const status = useGame((s) => s.status);
  const launchingId = useGame((s) => s.launchingId);
  const isRunning = useGame((s) => s.runningIds.includes(profile.id));
  const isLaunching = launching && launchingId === profile.id;

  const contextMenuItems: ContextMenuItem[] = [
    { id: 'edit', label: 'Profil bearbeiten', icon: 'solar:settings-bold', onClick: (p) => h.onEdit(p) },
    { id: 'duplicate', label: 'Duplizieren', icon: 'solar:copy-bold', onClick: (p) => h.onDuplicate(p) },
    ...(h.onAddVr ? [{ id: 'vr', label: 'VR (Vivecraft) hinzufügen', icon: 'solar:glasses-bold', onClick: (p: Profile) => h.onAddVr!(p) }] : []),
    { id: 'export', label: 'In offiziellen Launcher', icon: 'solar:download-bold', onClick: (p) => h.onExport(p) },
    { id: 'open-folder', label: 'Ordner öffnen', icon: 'solar:folder-bold', onClick: (p) => h.onOpenFolder(p) },
    { id: 'delete', label: 'Löschen', icon: 'solar:trash-bin-trash-bold', destructive: true, separator: true, onClick: (p) => h.onDelete(p) },
  ];

  const play = () => { h.onSelect(profile); void launchProfile(profile.id); };

  const actionButtons: ProfileActionButton[] = [
    { id: 'play', label: isLaunching ? 'STOP' : isRunning ? 'LÄUFT' : 'PLAY', icon: isLaunching ? 'solar:stop-bold' : 'solar:play-bold', variant: isLaunching ? 'destructive' : 'primary', tooltip: isLaunching ? 'Abbrechen' : isRunning ? 'Läuft – Konsole öffnen' : 'Spielen', onClick: play },
    { id: 'mods', label: 'INHALT', icon: 'solar:box-bold', variant: 'secondary', tooltip: 'Inhalte: Mods, Packs, Welten, Screenshots', onClick: (p) => h.onMods(p) },
    {
      id: 'settings', label: 'SETTINGS', icon: 'solar:settings-bold', variant: 'icon-only', tooltip: 'Profil-Optionen',
      onClick: (_p, e) => {
        e.preventDefault();
        e.stopPropagation();
        const open = !isContextMenuOpen;
        setIsContextMenuOpen(open);
        if (open) {
          const b = (e.currentTarget as HTMLElement).getBoundingClientRect();
          const card = (e.currentTarget as HTMLElement).closest('.nr-pcard')?.getBoundingClientRect();
          if (card) setContextMenuPosition({ x: b.right - card.left - 200, y: b.bottom - card.top + 4 });
        }
      },
    },
  ];

  const onContextMenu = (e: React.MouseEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsContextMenuOpen(true);
    const r = e.currentTarget.getBoundingClientRect();
    setContextMenuPosition({ x: e.clientX - r.left, y: e.clientY - r.top });
  };

  const meta = (compact = false) => (
    <div className="flex items-center gap-2 text-xs font-minecraft" style={{ textShadow: '0 1px 2px rgba(0,0,0,0.5)' }}>
      <div className="text-white/70 flex items-center gap-1"><Px id="grass" /><span>{profile.gameVersion}</span></div>
      {!compact && <><div className="w-px h-3 bg-white/30" /><div className="text-white/60 flex items-center gap-1"><Icon icon="solar:box-bold" className="w-3 h-3" /><span>{profile.loaderType ? `${LOADER[profile.loaderType]}${profile.loader ? ` ${profile.loader}` : ''}` : 'Vanilla'}</span></div></>}
      <div className="w-px h-3 bg-white/30" />
      <div className="text-white/50">{formatLastPlayed(profile.lastPlayed)}</div>
      {profile.legoClient && !compact && <><div className="w-px h-3 bg-white/30" /><div className="flex items-center gap-1" style={{ color: accentColor.value }}><Px id="brick" />LEGO Client</div></>}
    </div>
  );

  const pinBtn = (pinned || isHovered) && (
    <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); togglePin(profile.id); }} className={`flex-shrink-0 transition-all duration-200 ${pinned ? 'text-white' : 'text-white/40 hover:text-white'}`} title={pinned ? 'Lösen' : 'Oben anheften'} data-action="pin">
      <Icon icon={pinned ? 'solar:pin-bold' : 'solar:pin-bold-duotone'} className="w-4 h-4" />
    </button>
  );

  const selectedStar = selected && (
    <div className="absolute -top-1 -right-1 z-10">
      <Tooltip content="Ausgewählt für den LAUNCH-Knopf auf der Play-Seite">
        <div className="flex items-center justify-center w-6 h-6 rounded-full"><Icon icon="solar:star-bold" className="w-4 h-4 text-yellow-400" /></div>
      </Tooltip>
    </div>
  );

  const menu = <SettingsContextMenu profile={profile} isOpen={isContextMenuOpen} position={contextMenuPosition} items={contextMenuItems} onClose={() => setIsContextMenuOpen(false)} triggerButtonRef={settingsButtonRef} />;

  if (layoutMode === 'grid' || layoutMode === 'compact') {
    const isCompact = layoutMode === 'compact';
    return (
      <div
        className={`nr-pcard nr-prof-row relative flex flex-col ${isCompact ? 'gap-2 p-3' : 'gap-3 p-4'} rounded-lg bg-black/20 border border-white/10 hover:border-white/20 transition-all duration-200 cursor-pointer`}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
        onClick={(e) => { const t = e.target as Element; if (!t.closest('button') && !t.closest('.play-overlay')) h.onMods(profile); }}
        onContextMenu={onContextMenu}
      >
        <div className={`absolute ${isCompact ? 'top-2 right-2' : 'top-3 right-3'} z-20 flex flex-col gap-1`}>
          <button ref={settingsButtonRef} data-action="settings" title="Profil-Optionen" className={`${isCompact ? 'w-6 h-6' : 'w-8 h-8'} flex items-center justify-center rounded transition-all duration-200 bg-black/30 hover:bg-black/50 text-white/70 hover:text-white border border-white/10 hover:border-white/20`}
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); const open = !isContextMenuOpen; setIsContextMenuOpen(open); if (open) { const b = e.currentTarget.getBoundingClientRect(); const c = e.currentTarget.closest('.nr-pcard')!.getBoundingClientRect(); setContextMenuPosition({ x: b.right - c.left - 200, y: b.bottom - c.top + 4 }); } }}>
            <Icon icon="solar:settings-bold" className={isCompact ? 'w-3 h-3' : 'w-4 h-4'} />
          </button>
          <button data-action="mods" title="Inhalte: Mods, Packs, Welten, Screenshots" className={`${isCompact ? 'w-6 h-6' : 'w-8 h-8'} flex items-center justify-center rounded transition-all duration-200 bg-black/30 hover:bg-black/50 text-white/70 hover:text-white border border-white/10 hover:border-white/20`} onClick={(e) => { e.preventDefault(); e.stopPropagation(); h.onMods(profile); }}>
            <Icon icon="solar:box-bold" className={isCompact ? 'w-3 h-3' : 'w-4 h-4'} />
          </button>
        </div>
        <div className={`flex items-center ${isCompact ? 'gap-3' : 'gap-4'} relative z-10 w-full`}>
          <div className={`relative ${isCompact ? 'w-16 h-16' : 'w-20 h-20'} flex-shrink-0 rounded-lg flex items-center justify-center overflow-hidden border-2 transition-all duration-200`} style={{ backgroundColor: isHovered ? `${accentColor.value}20` : 'transparent', borderColor: isHovered ? `${accentColor.value}60` : 'transparent' }}>
            <ProfileIconV2 profile={profile} size={isCompact ? 'md' : 'lg'} className="w-full h-full" />
            {(isLaunching || isHovered) && (
              <div className="play-overlay absolute inset-0 flex items-center justify-center bg-black/50 backdrop-blur-sm cursor-pointer rounded-lg">
                <button data-play={profile.id} onClick={play} className={`${isCompact ? 'w-8 h-8' : 'w-12 h-12'} flex items-center justify-center text-white hover:text-white/80 transition-colors`}>
                  <Icon icon={isLaunching ? 'solar:stop-bold' : 'solar:play-bold'} className={isCompact ? 'w-6 h-6' : 'w-8 h-8'} />
                </button>
              </div>
            )}
            {selectedStar}
          </div>
          <div className={`flex-grow min-w-0 mr-auto pr-2 ${isCompact ? 'max-w-[calc(100%-64px)]' : 'max-w-[calc(100%-80px)]'}`}>
            <div className="flex items-center gap-2 mb-0.5">
              <h3 className={`font-minecraft text-white ${isCompact ? 'text-base' : 'text-lg'} whitespace-nowrap overflow-hidden text-ellipsis normal-case`} style={{ textShadow: '0 2px 4px rgba(0,0,0,0.7)' }} title={profile.name}>{profile.name}</h3>
              {pinBtn}
            </div>
            {isLaunching ? <div className="text-white/60 text-xs font-minecraft opacity-70 truncate">{status || 'Startet …'}</div> : meta(isCompact)}
          </div>
        </div>
        {menu}
      </div>
    );
  }

  return (
    <div
      className="nr-pcard nr-prof-row relative flex items-center gap-4 p-3 rounded-lg bg-black/20 border border-white/10 hover:border-white/20 transition-all duration-200 cursor-pointer"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={(e) => { if (!(e.target as Element).closest('button')) h.onMods(profile); }}
      onContextMenu={onContextMenu}
    >
      <div className="relative">
        <ProfileIconV2 profile={profile} size="md" />
        {selectedStar}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <h3 className="pr-name text-white font-minecraft text-sm whitespace-nowrap overflow-hidden text-ellipsis normal-case" style={{ textShadow: '0 2px 4px rgba(0,0,0,0.7)' }} title={profile.name}>{profile.name}</h3>
          {pinBtn}
        </div>
        {isLaunching ? <div className="text-white/60 text-xs font-minecraft opacity-70 truncate max-w-full">{status || 'Startet …'}</div> : meta()}
      </div>
      <ProfileActionButtons profile={profile} actions={actionButtons} useFlexSpacer flexSpacerAfterIndex={1} />
      {menu}
    </div>
  );
}
