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
        'rounded-[var(--border-radius)] text-white',
        'flex items-center gap-3 px-4 py-1',
        'border cursor-pointer active:scale-[0.98]',
        className,
      )}
      onClick={onClick}
      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') (e.currentTarget as HTMLDivElement).click(); }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        backgroundColor: isHovered ? 'rgba(255,255,255,0.10)' : 'rgba(255,255,255,0.05)',
        borderColor: isHovered ? `${accentColor.value}99` : 'rgba(255,255,255,0.12)',
      }}
    >
      <div
        className="relative w-7 h-7 overflow-hidden rounded-lg flex-shrink-0 flex items-center justify-center"
        style={{ backgroundColor: `${accentColor.value}33` }}
      >
        {signedIn && head ? <img src={head} alt="" className="w-full h-full" style={{ imageRendering: 'pixelated' }} /> : <span className="text-white font-smallcaps text-xs">+</span>}
      </div>
      {!compact && (
        <div className="flex flex-col min-w-0">
          <span className={cn(signedIn ? 'text-base' : 'text-sm', 'text-white font-medium truncate')} title={username}>
            {signedIn ? username : 'Konto hinzufügen'}
          </span>
        </div>
      )}
      <Icon icon="solar:alt-arrow-down-bold" className="w-4 h-4 text-white/90 ml-1 flex-shrink-0" />
    </div>
  );
}
