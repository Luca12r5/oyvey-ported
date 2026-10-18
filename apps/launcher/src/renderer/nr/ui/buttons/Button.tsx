// Button with the API of the NoRisk launcher's Button (GPL-3.0), restyled for the LEGO launcher.
import type React from 'react';
import { forwardRef, useState } from 'react';
import { cn } from '../../lib/utils.ts';
import { useThemeStore } from '../../store/theme.ts';
import { getAccessibilityProps, type ComponentSize, type ComponentVariant } from '../design-system.ts';
import { buttonStyle, isBrick } from './style.ts';
import { brickClick } from '../../lib/sound.ts';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ComponentVariant;
  size?: ComponentSize;
  icon?: React.ReactNode;
  iconPosition?: 'left' | 'right';
  shadowDepth?: 'default' | 'short' | 'none';
  widthClassName?: string;
  heightClassName?: string;
  label?: string;
  description?: string;
}

const SIZES: Record<ComponentSize, string> = {
  xs: 'h-[34px] px-4 text-xs',
  sm: 'h-[40px] px-5 text-sm',
  md: 'h-[46px] px-6 text-sm',
  lg: 'h-[54px] px-8 text-base',
  xl: 'h-[64px] px-10 text-lg',
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ children, className, variant = 'default', size = 'md', disabled = false, icon, iconPosition = 'left', shadowDepth: _s, widthClassName, heightClassName, label, description, style, ...props }, ref) => {
    const accent = useThemeStore((s) => s.accentColor);
    const [hover, setHover] = useState(false);
    const [press, setPress] = useState(false);
    return (
      <button
        ref={ref}
        type="button"
        disabled={disabled}
        className={cn(
          isBrick(variant) ? 'lx-brick' : 'lx-tile',
          'lx-btn relative font-semibold tracking-wide transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-white/40',
          'flex items-center justify-center gap-2 backdrop-blur-md whitespace-nowrap rounded-[var(--border-radius)]',
          SIZES[size],
          disabled && 'opacity-50 cursor-not-allowed',
          widthClassName, heightClassName, className,
        )}
        style={{ ...buttonStyle(variant, accent, hover, disabled), ...style }}
        onMouseEnter={() => setHover(true)}
        onMouseLeave={() => { setHover(false); setPress(false); }}
        onMouseDown={() => { if (!disabled) { setPress(true); brickClick(isBrick(variant)); } }}
        onMouseUp={() => setPress(false)}
        data-press={press ? '1' : undefined}
        {...getAccessibilityProps({ label, description, disabled })}
        {...props}
      >
        {icon && iconPosition === 'left' && <span className="flex items-center">{icon}</span>}
        <span>{children}</span>
        {icon && iconPosition === 'right' && <span className="flex items-center">{icon}</span>}
      </button>
    );
  },
);
Button.displayName = 'Button';
