// Launcher themes. Every theme is a complete, distinct configuration: its own
// palette plus family-specific style parameters (radius, glass blur, shadow,
// background pattern, font, motion). Derived colours (hover, border, muted
// text) are computed deterministically from the palette so the editor can
// regenerate them when a user changes a base colour.

import { contrast, isHexColor, mix } from './color.ts';

export const THEME_FAMILIES = [
  'dark-gaming', 'minimal', 'neon', 'cyberpunk', 'purple', 'blue', 'red', 'green', 'white', 'black',
  'glass', 'amoled', 'retro', 'futuristic', 'minecraft', 'desktop', 'seasonal',
] as const;
export type ThemeFamily = (typeof THEME_FAMILIES)[number];

export const BACKGROUNDS = ['solid', 'gradient', 'grid', 'noise', 'scanlines', 'stars', 'aurora', 'blocks', 'image'] as const;
export type BackgroundKind = (typeof BACKGROUNDS)[number];
export const FONTS = ['sans', 'rounded', 'mono', 'pixel'] as const;
export type ThemeFont = (typeof FONTS)[number];
export const MOTIONS = ['none', 'subtle', 'smooth', 'lively'] as const;
export type Motion = (typeof MOTIONS)[number];
export const SHADOWS = ['none', 'soft', 'glow', 'hard'] as const;
export type ShadowKind = (typeof SHADOWS)[number];

export interface ThemeColors {
  bg: string;
  bg2: string;
  surface: string;
  surfaceHover: string;
  border: string;
  text: string;
  textMuted: string;
  accent: string;
  accent2: string;
  success: string;
  warning: string;
  danger: string;
}

export interface Theme {
  id: string;
  name: string;
  family: ThemeFamily;
  mode: 'dark' | 'light';
  colors: ThemeColors;
  /** Corner radius in px for cards/buttons. */
  radius: number;
  /** Backdrop blur in px for panels (0 = no glass). */
  blur: number;
  /** Panel opacity 0..1 (below 1 shows the background through). */
  surfaceAlpha: number;
  borderWidth: number;
  shadow: ShadowKind;
  background: { kind: BackgroundKind; image?: string; intensity: number };
  font: ThemeFont;
  motion: Motion;
  /** UI scale multiplier (0.75..2). Users with 4K screens raise this. */
  scale: number;
}

interface FamilyStyle {
  radius: number;
  blur: number;
  surfaceAlpha: number;
  borderWidth: number;
  shadow: ShadowKind;
  background: BackgroundKind;
  intensity: number;
  font: ThemeFont;
  motion: Motion;
}

const FAMILY_STYLE: Record<ThemeFamily, FamilyStyle> = {
  'dark-gaming': { radius: 6, blur: 0, surfaceAlpha: 0.92, borderWidth: 1, shadow: 'glow', background: 'grid', intensity: 0.5, font: 'sans', motion: 'smooth' },
  minimal: { radius: 10, blur: 0, surfaceAlpha: 1, borderWidth: 1, shadow: 'soft', background: 'solid', intensity: 0, font: 'sans', motion: 'subtle' },
  neon: { radius: 8, blur: 0, surfaceAlpha: 0.88, borderWidth: 1, shadow: 'glow', background: 'noise', intensity: 0.7, font: 'sans', motion: 'lively' },
  cyberpunk: { radius: 2, blur: 0, surfaceAlpha: 0.94, borderWidth: 2, shadow: 'hard', background: 'scanlines', intensity: 0.6, font: 'mono', motion: 'lively' },
  purple: { radius: 12, blur: 6, surfaceAlpha: 0.85, borderWidth: 1, shadow: 'soft', background: 'gradient', intensity: 0.6, font: 'rounded', motion: 'smooth' },
  blue: { radius: 10, blur: 4, surfaceAlpha: 0.9, borderWidth: 1, shadow: 'soft', background: 'gradient', intensity: 0.5, font: 'sans', motion: 'smooth' },
  red: { radius: 8, blur: 0, surfaceAlpha: 0.92, borderWidth: 1, shadow: 'glow', background: 'gradient', intensity: 0.55, font: 'sans', motion: 'smooth' },
  green: { radius: 10, blur: 0, surfaceAlpha: 0.92, borderWidth: 1, shadow: 'soft', background: 'gradient', intensity: 0.5, font: 'rounded', motion: 'smooth' },
  white: { radius: 12, blur: 0, surfaceAlpha: 1, borderWidth: 1, shadow: 'soft', background: 'solid', intensity: 0, font: 'sans', motion: 'subtle' },
  black: { radius: 6, blur: 0, surfaceAlpha: 1, borderWidth: 1, shadow: 'none', background: 'solid', intensity: 0, font: 'sans', motion: 'subtle' },
  glass: { radius: 16, blur: 18, surfaceAlpha: 0.45, borderWidth: 1, shadow: 'soft', background: 'aurora', intensity: 0.9, font: 'rounded', motion: 'smooth' },
  amoled: { radius: 8, blur: 0, surfaceAlpha: 1, borderWidth: 1, shadow: 'none', background: 'solid', intensity: 0, font: 'sans', motion: 'subtle' },
  retro: { radius: 0, blur: 0, surfaceAlpha: 1, borderWidth: 2, shadow: 'hard', background: 'scanlines', intensity: 0.4, font: 'pixel', motion: 'subtle' },
  futuristic: { radius: 14, blur: 8, surfaceAlpha: 0.8, borderWidth: 1, shadow: 'glow', background: 'stars', intensity: 0.8, font: 'sans', motion: 'smooth' },
  minecraft: { radius: 0, blur: 0, surfaceAlpha: 0.95, borderWidth: 2, shadow: 'hard', background: 'blocks', intensity: 0.5, font: 'pixel', motion: 'subtle' },
  desktop: { radius: 6, blur: 0, surfaceAlpha: 1, borderWidth: 1, shadow: 'soft', background: 'solid', intensity: 0, font: 'sans', motion: 'subtle' },
  seasonal: { radius: 12, blur: 4, surfaceAlpha: 0.88, borderWidth: 1, shadow: 'glow', background: 'stars', intensity: 0.7, font: 'rounded', motion: 'smooth' },
};

// [id, name, family, bg, surface, text, accent, accent2, overrides?]
type Seed = [string, string, ThemeFamily, string, string, string, string, string, Partial<FamilyStyle>?];

const SEEDS: Seed[] = [
  // Dark Gaming
  ['nightfall', 'Nightfall', 'dark-gaming', '#0b1020', '#141b33', '#e6ebff', '#4f7cff', '#22d3ee'],
  ['obsidian-ops', 'Obsidian Ops', 'dark-gaming', '#0d0d12', '#17171f', '#ececf1', '#ff4655', '#ffb547'],
  ['deep-ocean', 'Deep Ocean', 'dark-gaming', '#04141f', '#0a2233', '#dff6ff', '#00b4d8', '#48cae4', { background: 'gradient' }],
  ['crimson-arena', 'Crimson Arena', 'dark-gaming', '#12070a', '#1f0d12', '#ffe9ec', '#e11d48', '#fb7185'],
  ['toxic-ops', 'Toxic Ops', 'dark-gaming', '#0a120b', '#132016', '#e8ffe9', '#4ade80', '#a3e635', { background: 'noise' }],
  ['stealth-gray', 'Stealth Gray', 'dark-gaming', '#101215', '#1a1d22', '#e5e7eb', '#94a3b8', '#f8fafc', { shadow: 'soft' }],
  ['ember-forge', 'Ember Forge', 'dark-gaming', '#140c06', '#22150b', '#fff1e6', '#f97316', '#facc15'],
  // Minimal
  ['slate-minimal', 'Slate', 'minimal', '#0f172a', '#1e293b', '#f1f5f9', '#38bdf8', '#818cf8'],
  ['graphite', 'Graphite', 'minimal', '#18181b', '#27272a', '#fafafa', '#a1a1aa', '#f4f4f5'],
  ['mono-ink', 'Mono Ink', 'minimal', '#111111', '#1c1c1c', '#f5f5f5', '#ffffff', '#9ca3af', { radius: 4 }],
  ['sand-minimal', 'Sand', 'minimal', '#f5f1ea', '#ffffff', '#1f1b16', '#b7791f', '#2f855a'],
  ['paper', 'Paper', 'minimal', '#fafafa', '#ffffff', '#18181b', '#2563eb', '#7c3aed', { radius: 14 }],
  ['nordic', 'Nordic', 'minimal', '#2e3440', '#3b4252', '#eceff4', '#88c0d0', '#a3be8c'],
  // Neon
  ['neon-pink', 'Neon Pink', 'neon', '#0a0612', '#150c22', '#ffe6fb', '#ff2bd6', '#00f0ff'],
  ['neon-lime', 'Neon Lime', 'neon', '#060a04', '#0f1709', '#f0ffe0', '#b6ff00', '#00ffa3'],
  ['neon-sunset', 'Neon Sunset', 'neon', '#0e0610', '#1c0c1f', '#fff0f5', '#ff6ec7', '#ffb86c', { background: 'gradient' }],
  ['electric-blue', 'Electric Blue', 'neon', '#02040f', '#070d24', '#e0f2ff', '#00a2ff', '#7df9ff', { background: 'grid' }],
  ['laser-red', 'Laser Red', 'neon', '#0d0204', '#1d070b', '#ffe4e6', '#ff1744', '#ff9100'],
  ['ultraviolet', 'Ultraviolet', 'neon', '#07031a', '#120836', '#efe7ff', '#8b5cf6', '#f0abfc', { background: 'stars' }],
  // Cyberpunk
  ['night-city', 'Night City', 'cyberpunk', '#0b0a12', '#16141f', '#f6f2ff', '#fcee0a', '#00f0ff'],
  ['netrunner', 'Netrunner', 'cyberpunk', '#050b0e', '#0b1a20', '#d6fff6', '#00ffc6', '#ff2a6d'],
  ['chrome-heart', 'Chrome Heart', 'cyberpunk', '#0f0f14', '#1c1c26', '#e9e9f2', '#c0c0d8', '#ff007f', { font: 'sans' }],
  ['synth-wave', 'Synthwave', 'cyberpunk', '#1a0b2e', '#2b1250', '#fde7ff', '#ff3cac', '#2b86c5', { background: 'grid', radius: 6 }],
  ['corpo-sec', 'Corpo Sec', 'cyberpunk', '#0a0e14', '#121a24', '#e3edf7', '#3ddbd9', '#f1c21b', { borderWidth: 1 }],
  ['glitch', 'Glitch', 'cyberpunk', '#0a0a0a', '#161616', '#f2f2f2', '#ff0055', '#00ffee', { background: 'noise' }],
  // Purple
  ['amethyst', 'Amethyst', 'purple', '#120b1f', '#1e1433', '#f3e8ff', '#a855f7', '#e879f9'],
  ['lavender-night', 'Lavender Night', 'purple', '#15122a', '#221d40', '#ede9fe', '#a78bfa', '#f0abfc', { background: 'stars' }],
  ['royal-violet', 'Royal Violet', 'purple', '#0e0620', '#1a0d38', '#f5f0ff', '#7c3aed', '#c084fc', { shadow: 'glow' }],
  ['grape-soda', 'Grape Soda', 'purple', '#1a0820', '#2a0f35', '#ffe8ff', '#d946ef', '#8b5cf6', { radius: 18 }],
  ['plum-velvet', 'Plum Velvet', 'purple', '#1a0d16', '#2a1624', '#fbe9f4', '#be185d', '#a855f7', { blur: 0 }],
  ['lilac', 'Lilac', 'purple', '#f5f0ff', '#ffffff', '#2e1065', '#7c3aed', '#db2777', { background: 'solid', blur: 0 }],
  // Blue
  ['arctic', 'Arctic', 'blue', '#06121f', '#0c2135', '#e6f4ff', '#3b82f6', '#60a5fa'],
  ['cobalt', 'Cobalt', 'blue', '#050a1f', '#0b1640', '#e0e7ff', '#2563eb', '#22d3ee', { shadow: 'glow' }],
  ['midnight-navy', 'Midnight Navy', 'blue', '#070b18', '#0f172e', '#dbe4ff', '#6366f1', '#38bdf8', { background: 'stars' }],
  ['ice-crystal', 'Ice Crystal', 'blue', '#08141c', '#102632', '#e5fbff', '#67e8f9', '#a5f3fc', { blur: 12, surfaceAlpha: 0.7 }],
  ['sky-day', 'Sky Day', 'blue', '#eef6ff', '#ffffff', '#0b2545', '#1d4ed8', '#0ea5e9', { blur: 0, background: 'solid' }],
  ['lego-blue', 'LEGO Blue', 'blue', '#0a1a3f', '#12286b', '#eef3ff', '#0055bf', '#ffd500', { radius: 4, background: 'blocks', font: 'rounded' }],
  // Red
  ['inferno', 'Inferno', 'red', '#140405', '#24090b', '#ffe5e5', '#ef4444', '#f97316'],
  ['blood-moon', 'Blood Moon', 'red', '#0f0204', '#1f060a', '#ffe4e6', '#b91c1c', '#fda4af', { background: 'stars' }],
  ['ruby', 'Ruby', 'red', '#170610', '#280c1c', '#ffe4f1', '#e11d48', '#f472b6', { radius: 14, blur: 6 }],
  ['lego-red', 'LEGO Red', 'red', '#2a0606', '#4a0d0d', '#fff1f1', '#c91a09', '#f2cd37', { radius: 4, background: 'blocks', font: 'rounded' }],
  ['cherry-blossom', 'Cherry Blossom', 'red', '#fff1f3', '#ffffff', '#4c0519', '#e11d48', '#f472b6', { shadow: 'soft', background: 'solid' }],
  // Green
  ['emerald', 'Emerald', 'green', '#04130c', '#0a2418', '#e3fff0', '#10b981', '#34d399'],
  ['forest', 'Forest', 'green', '#0b140b', '#142214', '#e9f5e9', '#22c55e', '#84cc16', { background: 'noise' }],
  ['matrix', 'Matrix', 'green', '#000800', '#001400', '#b6ffb6', '#00ff41', '#00b52a', { font: 'mono', background: 'scanlines', radius: 0 }],
  ['jade-temple', 'Jade Temple', 'green', '#07140f', '#0f241c', '#e1fbef', '#059669', '#fbbf24', { shadow: 'glow' }],
  ['mint', 'Mint', 'green', '#effcf6', '#ffffff', '#052e1b', '#059669', '#0891b2', { background: 'solid' }],
  ['creeper', 'Creeper', 'green', '#0e1a0e', '#193019', '#eaffea', '#4caf50', '#8bc34a', { font: 'pixel', radius: 0, background: 'blocks' }],
  // White / light
  ['snow', 'Snow', 'white', '#f8fafc', '#ffffff', '#0f172a', '#3b82f6', '#8b5cf6'],
  ['porcelain', 'Porcelain', 'white', '#f4f4f5', '#ffffff', '#18181b', '#18181b', '#f59e0b', { radius: 6 }],
  ['cloud', 'Cloud', 'white', '#eef2f7', '#ffffff', '#1e293b', '#6366f1', '#ec4899', { radius: 18, background: 'gradient', intensity: 0.3 }],
  ['cream', 'Cream', 'white', '#fbf7ef', '#fffdf8', '#2b2118', '#c2410c', '#15803d', { font: 'rounded' }],
  ['frost-glass', 'Frost Glass', 'white', '#e9f1fb', '#ffffff', '#0f1d33', '#2563eb', '#06b6d4', { blur: 16, surfaceAlpha: 0.6, background: 'aurora', intensity: 0.4 }],
  // Black
  ['pitch', 'Pitch', 'black', '#050505', '#0f0f0f', '#f5f5f5', '#e5e5e5', '#737373'],
  ['onyx-gold', 'Onyx Gold', 'black', '#0a0a0a', '#161616', '#f5f0e1', '#d4af37', '#f5e6a8', { shadow: 'glow', radius: 10 }],
  ['carbon', 'Carbon', 'black', '#0c0c0e', '#18181c', '#e4e4e7', '#f43f5e', '#3b82f6', { background: 'noise', intensity: 0.3 }],
  ['shadow-teal', 'Shadow Teal', 'black', '#060808', '#111515', '#e6fbf8', '#14b8a6', '#5eead4', { radius: 12 }],
  // Glassmorphism
  ['glass-aurora', 'Glass Aurora', 'glass', '#0b1026', '#1b2550', '#f0f4ff', '#7c9cff', '#ff7ce5'],
  ['glass-ocean', 'Glass Ocean', 'glass', '#031a2b', '#08304f', '#e6f7ff', '#22d3ee', '#3b82f6'],
  ['glass-rose', 'Glass Rose', 'glass', '#1f0a17', '#3a1530', '#fff0f8', '#fb7185', '#f9a8d4'],
  ['glass-mint', 'Glass Mint', 'glass', '#04201b', '#0a3a31', '#e8fff8', '#2dd4bf', '#a3e635'],
  ['glass-sunrise', 'Glass Sunrise', 'glass', '#24100a', '#40210f', '#fff4ea', '#fb923c', '#facc15'],
  ['glass-frost', 'Glass Frost', 'glass', '#dfe9f5', '#ffffff', '#102033', '#3b82f6', '#a855f7', { surfaceAlpha: 0.55 }],
  // AMOLED
  ['amoled-blue', 'AMOLED Blue', 'amoled', '#000000', '#0a0a0a', '#e5e7eb', '#3b82f6', '#22d3ee'],
  ['amoled-red', 'AMOLED Red', 'amoled', '#000000', '#0a0a0a', '#f4f4f5', '#ef4444', '#f97316'],
  ['amoled-green', 'AMOLED Green', 'amoled', '#000000', '#0a0a0a', '#ecfdf5', '#22c55e', '#a3e635'],
  ['amoled-purple', 'AMOLED Purple', 'amoled', '#000000', '#0a0a0a', '#f5f3ff', '#a855f7', '#ec4899'],
  ['amoled-mono', 'AMOLED Mono', 'amoled', '#000000', '#080808', '#ffffff', '#ffffff', '#6b7280', { radius: 0 }],
  ['amoled-gold', 'AMOLED Gold', 'amoled', '#000000', '#0a0a0a', '#fefce8', '#eab308', '#f59e0b'],
  // Retro
  ['arcade-80s', 'Arcade 80s', 'retro', '#140a2e', '#241449', '#fff4d6', '#ff6b35', '#f7c548'],
  ['gameboy', 'Pocket Green', 'retro', '#0f380f', '#306230', '#9bbc0f', '#8bac0f', '#9bbc0f', { background: 'solid' }],
  ['cga', 'CGA', 'retro', '#000000', '#101010', '#ffffff', '#55ffff', '#ff55ff'],
  ['terminal-amber', 'Terminal Amber', 'retro', '#0d0800', '#1a1000', '#ffb000', '#ffb000', '#ff7b00', { font: 'mono' }],
  ['vaporwave', 'Vaporwave', 'retro', '#1b0f2e', '#2d1b4e', '#fdf2ff', '#ff71ce', '#01cdfe', { background: 'grid', font: 'sans' }],
  ['c64', 'Breadbin', 'retro', '#352879', '#40318d', '#a59ffc', '#7869c4', '#b8c76f'],
  ['sepia-photo', 'Sepia', 'retro', '#efe6d5', '#f8f1e3', '#3b2a1a', '#8b5e34', '#a0522d', { background: 'noise', font: 'rounded', shadow: 'soft' }],
  // Futuristic
  ['hologram', 'Hologram', 'futuristic', '#020a13', '#071a2b', '#dffcff', '#4df3ff', '#a78bfa'],
  ['starship', 'Starship', 'futuristic', '#05060f', '#0d1022', '#e7eaff', '#5b8cff', '#ff9e3d'],
  ['quantum', 'Quantum', 'futuristic', '#070512', '#120d2a', '#f1edff', '#7f5af0', '#2cb67d', { background: 'grid' }],
  ['solar-flare', 'Solar Flare', 'futuristic', '#120700', '#241000', '#fff3e0', '#ff8f00', '#ff3d00', { background: 'gradient' }],
  ['nebula', 'Nebula', 'futuristic', '#0a0418', '#170a30', '#f8eaff', '#c026d3', '#22d3ee', { background: 'aurora' }],
  ['orbit-white', 'Orbit White', 'futuristic', '#f2f5fb', '#ffffff', '#0a1022', '#4f46e5', '#06b6d4', { background: 'solid', shadow: 'soft' }],
  // Minecraft-inspired
  ['grass-block', 'Grass Block', 'minecraft', '#1b2a14', '#2c4220', '#f0ffe6', '#5d9b2f', '#8b5a2b'],
  ['nether', 'Nether', 'minecraft', '#1e0606', '#3a0d0d', '#ffe6d6', '#ff5722', '#ffb300'],
  ['the-end', 'The End', 'minecraft', '#0d0a14', '#1d1630', '#f4f0d8', '#c9b6ff', '#e8e3a9', { background: 'stars' }],
  ['deepslate', 'Deepslate', 'minecraft', '#121214', '#222226', '#e2e2e6', '#6e7b8a', '#57d9c1'],
  ['diamond', 'Diamond', 'minecraft', '#06141a', '#0c2a33', '#e3fbff', '#4ee6e6', '#ffffff', { shadow: 'glow' }],
  ['redstone', 'Redstone', 'minecraft', '#160303', '#2b0808', '#ffe3e3', '#ff1f1f', '#9e0000', { shadow: 'glow' }],
  ['cherry-grove', 'Cherry Grove', 'minecraft', '#2a1420', '#3f1f31', '#ffeaf3', '#f5a3c7', '#7ed957'],
  ['ocean-monument', 'Ocean Monument', 'minecraft', '#04161a', '#0a2c33', '#dcfffa', '#42c6b4', '#f2d16b'],
  // Desktop-style minimal
  ['workstation', 'Workstation', 'desktop', '#1e1e1e', '#252526', '#d4d4d4', '#0e639c', '#4ec9b0', { radius: 2 }],
  ['fluent-dark', 'Fluent Dark', 'desktop', '#202020', '#2b2b2b', '#ffffff', '#60cdff', '#ffb900', { blur: 20, surfaceAlpha: 0.85 }],
  ['fluent-light', 'Fluent Light', 'desktop', '#f3f3f3', '#ffffff', '#1a1a1a', '#005fb8', '#c42b1c', { blur: 20, surfaceAlpha: 0.85 }],
  ['dracula', 'Count Night', 'desktop', '#282a36', '#343746', '#f8f8f2', '#bd93f9', '#ff79c6'],
  ['solarized-dark', 'Solar Dark', 'desktop', '#002b36', '#073642', '#eee8d5', '#268bd2', '#b58900'],
  ['solarized-light', 'Solar Light', 'desktop', '#fdf6e3', '#eee8d5', '#073642', '#268bd2', '#cb4b16'],
  ['gruvbox', 'Retro Groove', 'desktop', '#282828', '#3c3836', '#ebdbb2', '#fabd2f', '#8ec07c', { font: 'mono' }],
  ['catppuccin-mocha', 'Mocha', 'desktop', '#1e1e2e', '#313244', '#cdd6f4', '#cba6f7', '#f38ba8', { radius: 10 }],
  ['tokyo-night', 'Tokyo Night', 'desktop', '#1a1b26', '#24283b', '#c0caf5', '#7aa2f7', '#bb9af7'],
  ['one-dark', 'One Dark', 'desktop', '#282c34', '#2f343f', '#abb2bf', '#61afef', '#e06c75'],
  // Seasonal (matches the client's existing winter menu)
  ['winter-holiday', 'Winter Holiday', 'seasonal', '#0b1730', '#13254a', '#eaf4ff', '#e53935', '#7cd1ff'],
];

function derive(mode: 'dark' | 'light', bg: string, surface: string, text: string, accent: string, accent2: string): ThemeColors {
  const towards = mode === 'dark' ? '#ffffff' : '#000000';
  return {
    bg,
    bg2: mix(bg, towards, 0.04),
    surface,
    surfaceHover: mix(surface, towards, mode === 'dark' ? 0.06 : 0.04),
    border: mix(surface, text, 0.16),
    text,
    textMuted: mix(text, bg, 0.42),
    accent,
    accent2,
    success: mode === 'dark' ? '#34d399' : '#059669',
    warning: mode === 'dark' ? '#fbbf24' : '#b45309',
    danger: mode === 'dark' ? '#f87171' : '#dc2626',
  };
}

function build(seed: Seed): Theme {
  const [id, name, family, bg, surface, text, accent, accent2, over] = seed;
  const base = { ...FAMILY_STYLE[family], ...over };
  const mode: 'dark' | 'light' = contrast(bg, '#000000') > contrast(bg, '#ffffff') ? 'light' : 'dark';
  return {
    id,
    name,
    family,
    mode,
    colors: derive(mode, bg, surface, text, accent, accent2),
    radius: base.radius,
    blur: base.blur,
    surfaceAlpha: base.surfaceAlpha,
    borderWidth: base.borderWidth,
    shadow: base.shadow,
    background: { kind: base.background, intensity: base.intensity },
    font: base.font,
    motion: base.motion,
    scale: 1,
  };
}

export const THEMES: readonly Theme[] = Object.freeze(SEEDS.map(build));
export const DEFAULT_THEME_ID = 'nightfall';

export function getTheme(id: string): Theme | undefined {
  return THEMES.find((t) => t.id === id);
}

/** Rebuilds derived colours after a user changed a base colour in the editor. */
export function rederive(theme: Theme): Theme {
  const c = theme.colors;
  return { ...theme, colors: { ...derive(theme.mode, c.bg, c.surface, c.text, c.accent, c.accent2) } };
}

const COLOR_KEYS: (keyof ThemeColors)[] = [
  'bg', 'bg2', 'surface', 'surfaceHover', 'border', 'text', 'textMuted', 'accent', 'accent2', 'success', 'warning', 'danger',
];

function num(v: unknown, min: number, max: number): number | null {
  return typeof v === 'number' && Number.isFinite(v) && v >= min && v <= max ? v : null;
}

function oneOf<T extends string>(v: unknown, list: readonly T[]): T | null {
  return typeof v === 'string' && (list as readonly string[]).includes(v) ? (v as T) : null;
}

export type ThemeValidation = { ok: true; theme: Theme } | { ok: false; errors: string[] };

/**
 * Validates an imported/user-created theme. Unknown keys are dropped. Image
 * backgrounds must be data: URLs of PNG/JPEG/WebP (no remote URLs, so an
 * imported theme cannot make the launcher contact third-party hosts).
 */
export function validateTheme(input: unknown): ThemeValidation {
  const errors: string[] = [];
  if (typeof input !== 'object' || input === null) return { ok: false, errors: ['theme must be an object'] };
  const o = input as Record<string, unknown>;
  const id = typeof o.id === 'string' && /^[a-z0-9][a-z0-9-]{1,47}$/.test(o.id) ? o.id : null;
  if (!id) errors.push('id must be 2-48 chars of a-z, 0-9, -');
  const name = typeof o.name === 'string' && o.name.trim().length >= 1 && o.name.length <= 40 ? o.name.trim() : null;
  if (!name) errors.push('name must be 1-40 chars');
  const family = oneOf(o.family, THEME_FAMILIES) ?? 'minimal';
  const mode = oneOf(o.mode, ['dark', 'light'] as const);
  if (!mode) errors.push('mode must be dark or light');
  const colorsIn = (typeof o.colors === 'object' && o.colors !== null ? o.colors : {}) as Record<string, unknown>;
  const colors = {} as ThemeColors;
  for (const k of COLOR_KEYS) {
    const v = colorsIn[k];
    if (isHexColor(v)) colors[k] = v.toLowerCase();
    else errors.push(`colors.${k} must be #rrggbb`);
  }
  const radius = num(o.radius, 0, 32);
  if (radius === null) errors.push('radius must be 0-32');
  const blur = num(o.blur, 0, 40);
  if (blur === null) errors.push('blur must be 0-40');
  const surfaceAlpha = num(o.surfaceAlpha, 0.2, 1);
  if (surfaceAlpha === null) errors.push('surfaceAlpha must be 0.2-1');
  const borderWidth = num(o.borderWidth, 0, 4);
  if (borderWidth === null) errors.push('borderWidth must be 0-4');
  const shadow = oneOf(o.shadow, SHADOWS);
  if (!shadow) errors.push('invalid shadow');
  const font = oneOf(o.font, FONTS);
  if (!font) errors.push('invalid font');
  const motion = oneOf(o.motion, MOTIONS);
  if (!motion) errors.push('invalid motion');
  const scale = num(o.scale ?? 1, 0.75, 2);
  if (scale === null) errors.push('scale must be 0.75-2');
  const bgIn = (typeof o.background === 'object' && o.background !== null ? o.background : {}) as Record<string, unknown>;
  const kind = oneOf(bgIn.kind, BACKGROUNDS);
  if (!kind) errors.push('invalid background.kind');
  const intensity = num(bgIn.intensity ?? 0.5, 0, 1);
  if (intensity === null) errors.push('background.intensity must be 0-1');
  let image: string | undefined;
  if (kind === 'image') {
    const img = bgIn.image;
    if (typeof img === 'string' && /^data:image\/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+$/.test(img) && img.length <= 8_000_000) image = img;
    else errors.push('background.image must be a PNG/JPEG/WebP data URL up to ~6 MB');
  }
  if (errors.length) return { ok: false, errors };
  const background: Theme['background'] = { kind: kind!, intensity: intensity! };
  if (image) background.image = image;
  return {
    ok: true,
    theme: {
      id: id!, name: name!, family, mode: mode!, colors, radius: radius!, blur: blur!, surfaceAlpha: surfaceAlpha!,
      borderWidth: borderWidth!, shadow: shadow!, background, font: font!, motion: motion!, scale: scale!,
    },
  };
}

/** Readability check used by the editor to warn about unreadable custom themes. */
export function readabilityWarnings(theme: Theme): string[] {
  const w: string[] = [];
  const c = theme.colors;
  if (contrast(c.text, c.surface) < 4.5) w.push('Text on panels is below WCAG AA contrast (4.5:1).');
  if (contrast(c.text, c.bg) < 4.5) w.push('Text on background is below WCAG AA contrast (4.5:1).');
  if (contrast(c.accent, c.surface) < 2) w.push('Accent colour is hard to distinguish from panels.');
  return w;
}
