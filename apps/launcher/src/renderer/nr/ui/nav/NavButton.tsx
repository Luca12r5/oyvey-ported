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
}

export const NavButton = forwardRef<HTMLButtonElement, NavButtonProps>(({ className, icon, isActive = false, variant: _v, label, style, onMouseEnter, onMouseLeave, ...props }, ref) => {
  const accent = useThemeStore((s) => s.accentColor);
  const [hover, setHover] = useState(false);
  return (
    <button
      ref={ref}
      className={cn('lx-nav relative w-[62px] flex flex-col items-center justify-center gap-1 rounded-2xl transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/40', label ? 'py-2' : 'h-[58px]', className)}
      style={{
        color: isActive ? '#fff' : hover ? 'rgba(255,255,255,0.95)' : 'rgba(255,255,255,0.58)',
        backgroundImage: isActive ? `linear-gradient(140deg, ${accent.light}, ${accent.dark})` : undefined,
        backgroundColor: !isActive && hover ? 'rgba(255,255,255,0.08)' : undefined,
        boxShadow: isActive ? `0 8px 24px ${accent.value}59, inset 0 1px 0 rgba(255,255,255,0.3)` : undefined,
        transform: hover && !isActive ? 'scale(1.06)' : undefined,
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
