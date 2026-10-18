// Button look of the LEGO launcher (Spotify-HUD style): rounded glass for quiet
// buttons, a filled accent gradient with a soft glow for primary ones.
import type React from 'react';
import type { AccentColor } from '../../store/theme.ts';
import type { ComponentVariant } from '../design-system.ts';

const SOLID: Partial<Record<ComponentVariant, [string, string]>> = {
  success: ['#22c55e', '#15803d'],
  destructive: ['#ef4444', '#b91c1c'],
  warning: ['#f59e0b', '#b45309'],
  info: ['#3b82f6', '#1d4ed8'],
};

export function buttonStyle(variant: ComponentVariant, accent: AccentColor, hovered: boolean, disabled: boolean): React.CSSProperties {
  const lift = hovered && !disabled;
  if (variant === 'ghost') return { backgroundColor: lift ? 'rgba(255,255,255,0.08)' : 'transparent', color: '#fff' };
  if (variant === 'flat' || variant === 'flat-secondary' || variant === 'secondary' || variant === 'default') {
    return {
      backgroundColor: lift ? 'rgba(255,255,255,0.10)' : 'rgba(255,255,255,0.05)',
      border: `1px solid ${lift ? `${accent.value}99` : 'rgba(255,255,255,0.12)'}`,
      color: '#fff',
      boxShadow: lift ? `0 6px 18px ${accent.value}26` : 'none',
    };
  }
  const [a, b] = SOLID[variant] ?? [accent.light, accent.dark];
  return {
    backgroundImage: `linear-gradient(135deg, ${a}, ${b})`,
    border: '1px solid rgba(255,255,255,0.18)',
    color: '#fff',
    boxShadow: lift ? `0 10px 30px ${a}66, inset 0 1px 0 rgba(255,255,255,0.35)` : `0 6px 20px ${a}40, inset 0 1px 0 rgba(255,255,255,0.25)`,
    filter: lift ? 'brightness(1.08)' : undefined,
  };
}
