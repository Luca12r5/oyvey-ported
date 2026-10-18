// Ported from the NoRisk Client launcher (GPL-3.0): components/modrinth/v2/ModrinthProjectCardV2.tsx.
// The version list is replaced by the profile picker of the LEGO launcher (arrow next to INSTALL).
import { memo } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { TagBadge } from '../ui/TagBadge.tsx';
import { ActionButton } from '../ui/ActionButton.tsx';
import type { AccentColor } from '../store/theme.ts';
import type { SearchHit } from '../../../core/modrinth.ts';
import { api } from '../../ui.ts';

export interface ModrinthProjectCardV2Props {
  hit: SearchHit;
  accentColor: AccentColor;
  installed: boolean;
  installing: boolean;
  onInstall: (hit: SearchHit) => void;
  onPickProfile: (hit: SearchHit, anchor: HTMLElement) => void;
}

export const ModrinthProjectCardV2 = memo(function ModrinthProjectCardV2({ hit, accentColor, installed, installing, onInstall, onPickProfile }: ModrinthProjectCardV2Props) {
  const open = () => void api.openExternal(`https://modrinth.com/${hit.project_type}/${hit.slug}`);
  return (
    <div>
      <div onClick={open} title="Auf Modrinth öffnen" className={cn('nr-mod-row relative flex items-center gap-4 p-3 rounded-lg bg-black/20 border border-white/10 hover:border-white/20 transition-all duration-200 cursor-pointer', installed && 'border-l-green-500')}>
        <div className="absolute top-3 right-3 flex items-center space-x-2 text-xs text-gray-400 font-minecraft">
          <div className="text-white/50 flex items-center gap-0.5">
            <Icon icon="solar:download-minimalistic-bold" className="h-3 w-3" />
            <span>{hit.downloads.toLocaleString('de-DE')}</span>
          </div>
        </div>
        <div className="relative w-20 h-20 flex-shrink-0 rounded-md overflow-hidden border" style={{ borderColor: `${accentColor.value}30`, backgroundColor: `${accentColor.value}10` }}>
          {hit.icon_url ? <img src={hit.icon_url} alt="" loading="lazy" className="w-full h-full object-cover" /> : <div className="w-full h-full bg-gray-700/50 flex items-center justify-center"><span className="text-gray-500 text-xl">?</span></div>}
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex flex-row items-baseline space-x-1.5 mb-1">
            <span className="text-white font-minecraft text-lg whitespace-nowrap overflow-hidden text-ellipsis normal-case hover:underline cursor-pointer text-left transition-colors">{hit.title}</span>
            {hit.author && (
              <a href={`https://modrinth.com/user/${hit.author}`} onClick={(e) => { e.preventDefault(); e.stopPropagation(); void api.openExternal(`https://modrinth.com/user/${hit.author}`); }} className="text-xs text-gray-400 truncate font-minecraft flex-shrink min-w-0 hover:text-gray-200 hover:underline cursor-pointer">
                by {hit.author}
              </a>
            )}
          </div>
          <p className="text-xs text-gray-300 line-clamp-2 font-minecraft leading-tight mb-2 min-h-[2rem]">{hit.description}</p>
          <div className="flex items-center gap-1 text-sm font-minecraft">
            {installed && <TagBadge variant="success" size="sm">Installiert</TagBadge>}
            {hit.categories.filter((c) => c !== 'minecraft').slice(0, 3).map((c) => <TagBadge key={c} size="sm">{c.replace(/-/g, ' ')}</TagBadge>)}
          </div>
        </div>
        <div className="flex items-center space-x-1">
          <ActionButton
            id="install"
            label={installing ? 'Installiert …' : installed ? 'Installiert' : 'Install'}
            icon={installing ? 'solar:refresh-bold' : installed ? 'solar:check-circle-bold' : 'solar:download-minimalistic-bold'}
            iconClassName={installing ? 'animate-spin' : ''}
            variant={installing || installed ? 'secondary' : 'primary'}
            disabled={installing}
            onClick={(e) => { e.stopPropagation(); onInstall(hit); }}
            size="sm"
          />
          <ActionButton id="pick" icon="solar:alt-arrow-down-bold" variant="icon-only" tooltip="In ein anderes Profil installieren" disabled={installing}
            onClick={(e) => { e.stopPropagation(); onPickProfile(hit, e.currentTarget as HTMLElement); }} size="sm" />
        </div>
      </div>
    </div>
  );
});
