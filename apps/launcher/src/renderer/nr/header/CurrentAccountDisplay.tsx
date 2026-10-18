// Ported from the NoRisk Client launcher (GPL-3.0): components/account/CurrentAccountDisplay.tsx (flat variant).
import { useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { useThemeStore } from '../store/theme.ts';
import { useShell } from '../store/shell.ts';

interface CurrentAccountDisplayProps {
  onClick?: (e: React.MouseEvent<HTMLDivElement>) => void;
  className?: string;
  compact?: boolean;
}

export function CurrentAccountDisplay({ onClick, className, compact = false }: CurrentAccountDisplayProps) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const signedIn = useShell((s) => s.signedIn);
  const username = useShell((s) => s.name);
  const head = useShell((s) => s.head);
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div
      id="acctBtn"
      role="button"
      tabIndex={0}
      aria-haspopup="menu"
      className={cn(
        'font-smallcaps relative overflow-hidden backdrop-blur-md transition-all duration-200 nr-pop',
        'rounded-md text-white tracking-wider',
        'flex items-center gap-3 px-4 py-1',
        'text-shadow-sm border border-b-2 cursor-pointer hover:brightness-110 active:brightness-90',
        className,
      )}
      onClick={onClick}
      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') (e.currentTarget as HTMLDivElement).click(); }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        backgroundColor: `${accentColor.value}30`,
        borderColor: `${accentColor.value}80`,
        borderBottomColor: isHovered ? accentColor.hoverValue : accentColor.value,
        filter: isHovered ? 'brightness(1.1)' : 'brightness(1)',
      }}
    >
      <div
        className="relative w-7 h-7 overflow-hidden border-2 rounded-sm flex-shrink-0 flex items-center justify-center"
        style={{ borderColor: `${accentColor.value}60`, backgroundColor: `${accentColor.value}20` }}
      >
        {head ? <img src={head} alt="" className="w-full h-full" style={{ imageRendering: 'pixelated' }} /> : <span className="text-white font-smallcaps text-xs">+</span>}
      </div>
      {!compact && (
        <div className="flex flex-col min-w-0">
          <span className={cn(signedIn || username ? 'text-base' : 'text-sm', 'text-white font-smallcaps truncate')} title={username}>
            {signedIn ? username : username ? `Gast: ${username}` : 'Konto hinzufügen'}
          </span>
        </div>
      )}
      <Icon icon="solar:alt-arrow-down-bold" className="w-4 h-4 text-white/90 ml-1 flex-shrink-0" />
    </div>
  );
}
