// Rail button (API of the NoRisk launcher's NavButton, GPL-3.0) in the LEGO launcher's look:
// rounded tile, active = accent gradient with glow.
import type React from 'react';
import { forwardRef, useState } from 'react';
import { cn } from '../../lib/utils.ts';
import { useThemeStore } from '../../store/theme.ts';

interface NavButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  icon: React.ReactNode;
  isActive?: boolean;
  variant?: 'default' | 'secondary' | 'ghost';
  label?: React.ReactNode;
  /** LEGO colour of this rail brick (each tab has its own). */
  brickColor?: string;
}

export const NavButton = forwardRef<HTMLButtonElement, NavButtonProps>(({ className, icon, isActive = false, variant: _v, label, brickColor, style, onMouseEnter, onMouseLeave, ...props }, ref) => {
  const accent = useThemeStore((s) => s.accentColor);
  const [hover, setHover] = useState(false);
  return (
    <button
      ref={ref}
      className={cn('lx-nav relative w-[62px] flex flex-col items-center justify-center gap-1 transition-colors duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/40', isActive && 'lx-nav-on', label ? 'py-2' : 'h-[58px]', className)}
      style={{
        ['--bc' as string]: brickColor ?? accent.value,
        color: isActive ? (brickColor === '#f2cd37' ? '#1b1b1b' : '#fff') : hover ? 'rgba(255,255,255,0.95)' : 'rgba(255,255,255,0.6)',
        ...style,
      }}
      onMouseEnter={(e) => { setHover(true); onMouseEnter?.(e); }}
      onMouseLeave={(e) => { setHover(false); onMouseLeave?.(e); }}
      {...props}
    >
      <span className="flex items-center justify-center w-7 h-7">{icon}</span>
      {label && <span className="text-[11px] font-medium leading-tight text-center whitespace-nowrap">{label}</span>}
    </button>
  );
});
NavButton.displayName = 'NavButton';
