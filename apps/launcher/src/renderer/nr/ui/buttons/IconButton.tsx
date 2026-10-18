// Icon button with the API of the NoRisk launcher's IconButton (GPL-3.0), restyled for the LEGO launcher.
import type React from 'react';
import { forwardRef, useState } from 'react';
import { cn } from '../../lib/utils.ts';
import { useThemeStore } from '../../store/theme.ts';
import { getAccessibilityProps, type ComponentSize, type ComponentVariant } from '../design-system.ts';
import { buttonStyle } from './style.ts';

interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ComponentVariant;
  displayVariant?: 'button' | 'ghost' | 'themed-surface';
  size?: ComponentSize;
  icon: React.ReactNode;
  shadowDepth?: 'default' | 'short' | 'none';
  label?: string;
  description?: string;
}

const SIZES: Record<ComponentSize, [string, string]> = {
  xs: ['h-[34px] w-[34px]', 'w-4 h-4'],
  sm: ['h-[40px] w-[40px]', 'w-5 h-5'],
  md: ['h-[46px] w-[46px]', 'w-5 h-5'],
  lg: ['h-[54px] w-[54px]', 'w-6 h-6'],
  xl: ['h-[64px] w-[64px]', 'w-7 h-7'],
};

export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>(
  ({ className, variant = 'default', displayVariant = 'button', size = 'md', disabled = false, icon, shadowDepth: _s, label, description, style, ...props }, ref) => {
    const accent = useThemeStore((s) => s.accentColor);
    const [hover, setHover] = useState(false);
    const [box, ic] = SIZES[size];
    return (
      <button
        ref={ref}
        disabled={disabled}
        className={cn('lx-btn relative flex items-center justify-center rounded-[var(--border-radius)] backdrop-blur-md transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/40 disabled:opacity-50 disabled:cursor-not-allowed', box, className)}
        style={{ ...buttonStyle(displayVariant === 'button' ? variant : 'ghost', accent, hover, disabled), ...style }}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => setHover(false)}
        {...getAccessibilityProps({ label, description, disabled })}
        {...props}
      >
        <span className={cn('relative z-10 flex items-center justify-center transition-transform duration-200', ic)} style={{ transform: hover && !disabled ? 'scale(1.08)' : undefined }} aria-hidden="true">{icon}</span>
      </button>
    );
  },
);
IconButton.displayName = 'IconButton';
