// Button look of the LEGO launcher: primary buttons are real bricks (studs on
// top, lit top edge, dark bottom edge, press-down on click); quiet buttons are
// smooth "tiles" like LEGO flat tiles.
import type React from 'react';
import type { AccentColor } from '../../store/theme.ts';
import type { ComponentVariant } from '../design-system.ts';

/** Brick colours per variant (LEGO palette). */
const BRICK: Partial<Record<ComponentVariant, string>> = {
  success: '#237841',
  destructive: '#c4161c',
  warning: '#f2cd37',
  info: '#0055bf',
};

export function isBrick(variant: ComponentVariant): boolean {
  return !(variant === 'ghost' || variant === 'flat' || variant === 'flat-secondary' || variant === 'secondary' || variant === 'default');
}

export function buttonStyle(variant: ComponentVariant, accent: AccentColor, hovered: boolean, disabled: boolean): React.CSSProperties {
  const lift = hovered && !disabled;
  if (variant === 'ghost') return { backgroundColor: lift ? 'rgba(255,255,255,0.08)' : 'transparent', color: '#fff' };
  if (!isBrick(variant)) {
    return {
      backgroundColor: lift ? '#30353c' : '#262a30',
      border: '1px solid rgba(255,255,255,0.08)',
      boxShadow: `inset 0 1px 0 rgba(255,255,255,0.10), inset 0 -3px 0 rgba(0,0,0,0.35)${lift ? `, 0 0 0 1px ${accent.value}66` : ''}`,
      color: '#fff',
    };
  }
  const c = BRICK[variant] ?? accent.value;
  // Light accents (Spotify green, yellow) get black text like Spotify's buttons.
  const light = /^#(1ed760|f2cd37|ffd166|ffd500|4fd1c5|a1a1aa|e4e4e7)$/i.test(c);
  return { ['--bc' as string]: c, color: light || variant === 'warning' ? '#000' : '#fff', fontWeight: 700 } as React.CSSProperties;
}
