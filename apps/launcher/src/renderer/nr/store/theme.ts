// Accent colour handling from the NoRisk launcher's useThemeStore (GPL-3.0),
// reduced to what the LEGO launcher uses. The accent comes from our settings.
import { createStore, useStore } from './store.ts';

export interface AccentColor {
  name: string;
  value: string;
  hoverValue: string;
  shadowValue: string;
  light: string;
  dark: string;
}

const hexToRgb = (hex: string) => {
  const m = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return m ? { r: parseInt(m[1]!, 16), g: parseInt(m[2]!, 16), b: parseInt(m[3]!, 16) } : null;
};
const rgbToHex = (r: number, g: number, b: number) => '#' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
const darken = (hex: string, amount: number) => {
  const c = hexToRgb(hex);
  return c ? rgbToHex(Math.max(0, Math.floor(c.r * (1 - amount))), Math.max(0, Math.floor(c.g * (1 - amount))), Math.max(0, Math.floor(c.b * (1 - amount)))) : hex;
};
const lighten = (hex: string, amount: number) => {
  const c = hexToRgb(hex);
  return c ? rgbToHex(Math.min(255, Math.floor(c.r + (255 - c.r) * amount)), Math.min(255, Math.floor(c.g + (255 - c.g) * amount)), Math.min(255, Math.floor(c.b + (255 - c.b) * amount))) : hex;
};

export const DEFAULT_ACCENT = '#4f8eff';

export function accentFrom(hex: string | null | undefined): AccentColor {
  const value = hex && hexToRgb(hex) ? hex.toLowerCase() : DEFAULT_ACCENT;
  if (value === DEFAULT_ACCENT) return { name: 'Blue', value, hoverValue: '#3a7aff', shadowValue: 'rgba(79, 142, 255, 0.5)', light: '#60a5fa', dark: '#2563eb' };
  const c = hexToRgb(value)!;
  return { name: 'Custom', value, hoverValue: darken(value, 0.1), shadowValue: `rgba(${c.r}, ${c.g}, ${c.b}, 0.5)`, light: lighten(value, 0.2), dark: darken(value, 0.2) };
}

export interface ThemeState {
  accentColor: AccentColor;
  showNavLabels: boolean;
  borderRadius: number;
  isBackgroundAnimationEnabled: boolean;
}

export const themeStore = createStore<ThemeState>({ accentColor: accentFrom(null), showNavLabels: true, borderRadius: 0, isBackgroundAnimationEnabled: true });

/** Same CSS variables the NoRisk launcher writes (scrollbars, glows). */
export function applyAccentToDOM(): void {
  const { accentColor: a, borderRadius } = themeStore.get();
  const c = hexToRgb(a.value) ?? { r: 79, g: 142, b: 255 };
  const root = document.documentElement.style;
  root.setProperty('--nr-accent', a.value);
  root.setProperty('--accent-hover', a.hoverValue);
  root.setProperty('--accent-shadow', a.shadowValue);
  root.setProperty('--accent-rgb', `${c.r}, ${c.g}, ${c.b}`);
  root.setProperty('--border-radius', `${borderRadius}px`);
}

export function useThemeStore<S>(pick: (s: ThemeState) => S): S {
  return useStore(themeStore, pick);
}
