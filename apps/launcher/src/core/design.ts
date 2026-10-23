import { validSkinPng } from './msauth.ts';
// Design editor data: effect parameters, surface style and saved presets.
// Shared by the renderer (live preview) and the main process (validation).

export interface DesignFx {
  /** Particle density 0..1. */
  particles: number;
  /** Animation speed multiplier 0.25..2. */
  speed: number;
  /** Light intensity 0.2..1.6. */
  light: number;
  fog: boolean;
  glow: boolean;
  vignette: boolean;
  /** Soft-focus (blur) on the background and the frosted-glass panels over it. Off by default. */
  blur: boolean;
  /** Camera follows the mouse a little (parallax). */
  parallax: boolean;
}

export const SURFACES = ['glass', 'solid', 'neon'] as const;
export type SurfaceStyle = (typeof SURFACES)[number];
export const STAGES = ['portal', 'plain'] as const;
export type StageStyle = (typeof STAGES)[number];

export interface DesignPreset {
  id: string;
  name: string;
  themeId: string;
  accentColor: string | null;
  background: string;
  snow: boolean;
  fx: DesignFx;
  surface: SurfaceStyle;
  stage: StageStyle;
}

export const DEFAULT_FX: DesignFx = { particles: 0.6, speed: 1, light: 1, fog: true, glow: true, vignette: true, blur: false, parallax: true };

const HEX = /^#[0-9a-f]{6}$/i;
const num = (v: unknown, min: number, max: number, d: number) => {
  const n = Number(v);
  return Number.isFinite(n) ? Math.max(min, Math.min(max, n)) : d;
};
const bool = (v: unknown, d: boolean) => (typeof v === 'boolean' ? v : d);

export function sanitizeFx(x: unknown): DesignFx {
  const o = (x && typeof x === 'object' ? x : {}) as Record<string, unknown>;
  return {
    particles: num(o.particles, 0, 1, DEFAULT_FX.particles),
    speed: num(o.speed, 0.25, 2, DEFAULT_FX.speed),
    light: num(o.light, 0.2, 1.6, DEFAULT_FX.light),
    fog: bool(o.fog, DEFAULT_FX.fog),
    glow: bool(o.glow, DEFAULT_FX.glow),
    vignette: bool(o.vignette, DEFAULT_FX.vignette),
    blur: bool(o.blur, DEFAULT_FX.blur),
    parallax: bool(o.parallax, DEFAULT_FX.parallax),
  };
}

/** How a custom background image sits in the window. */
export interface ImageFit {
  /** 'cover' fills the window, 'contain' shows the whole image, 'stretch' distorts to fit. */
  mode: 'cover' | 'contain' | 'stretch';
  /** Zoom in percent (50..300). */
  zoom: number;
  /** Offset in percent of the window (-50..50). */
  x: number;
  y: number;
  /** Blur in px (0..20) and darkening in percent (0..80). */
  blur: number;
  dim: number;
  /** Slow drift ("Ken Burns"). */
  motion: boolean;
}

/** Free customisation of the launcher frame (Designs → Stil). */
export interface LookSettings {
  /** Opacity of the launcher over an animated background, percent (20..100). */
  shellAlpha: number;
  /** Corner radius in px (0..24). */
  radius: number;
  /** Accent-coloured glow lines on the window edge. */
  glowLines: boolean;
  /** Labels under the rail icons. */
  navLabels: boolean;
  /** Brick colours of the "LEGO" title (hex). */
  titleColors: string[];
  /** Show "LAUNCHER" next to the bricks. */
  titleSub: boolean;
  /** Size of the character on the play screen, percent (70..140). */
  rigScale: number;
  /** Name tag above the character. */
  nametag: boolean;
  imageFit: ImageFit;
  /** Window layout: "studio" (floating dock at the bottom) or "classic" (rail on the left). */
  layout: 'studio' | 'classic';
}

export const DEFAULT_IMAGE_FIT: ImageFit = { mode: 'cover', zoom: 100, x: 0, y: 0, blur: 0, dim: 20, motion: true };
export const DEFAULT_LOOK: LookSettings = {
  shellAlpha: 55, radius: 12, glowLines: true, navLabels: true, titleColors: ['#ffffff', '#f2cd37', '#0055bf', '#e9ecef'], titleSub: true, rigScale: 100, nametag: true, imageFit: DEFAULT_IMAGE_FIT, layout: 'studio',
};

export function sanitizeLook(x: unknown): LookSettings {
  const o = (x && typeof x === 'object' ? x : {}) as Record<string, unknown>;
  const f = (o.imageFit && typeof o.imageFit === 'object' ? o.imageFit : {}) as Record<string, unknown>;
  const d = DEFAULT_LOOK, df = DEFAULT_IMAGE_FIT;
  const colors = Array.isArray(o.titleColors) ? o.titleColors.filter((c): c is string => typeof c === 'string' && HEX.test(c)).slice(0, 4) : [];
  return {
    shellAlpha: num(o.shellAlpha, 20, 100, d.shellAlpha),
    radius: num(o.radius, 0, 24, d.radius),
    glowLines: bool(o.glowLines, d.glowLines),
    navLabels: bool(o.navLabels, d.navLabels),
    titleColors: colors.length === 4 ? colors : d.titleColors,
    titleSub: bool(o.titleSub, d.titleSub),
    rigScale: num(o.rigScale, 70, 140, d.rigScale),
    nametag: bool(o.nametag, d.nametag),
    layout: o.layout === 'classic' ? 'classic' : 'studio',
    imageFit: {
      mode: f.mode === 'contain' || f.mode === 'stretch' ? f.mode : 'cover',
      zoom: num(f.zoom, 50, 300, df.zoom),
      x: num(f.x, -50, 50, df.x),
      y: num(f.y, -50, 50, df.y),
      blur: num(f.blur, 0, 20, df.blur),
      dim: num(f.dim, 0, 80, df.dim),
      motion: bool(f.motion, df.motion),
    },
  };
}

export function sanitizePreset(x: unknown): DesignPreset | null {
  if (!x || typeof x !== 'object') return null;
  const o = x as Record<string, unknown>;
  const name = typeof o.name === 'string' ? o.name.trim().slice(0, 40) : '';
  const id = typeof o.id === 'string' && /^[a-z0-9-]{1,48}$/.test(o.id) ? o.id : '';
  const themeId = typeof o.themeId === 'string' && /^[a-z0-9-]{1,48}$/.test(o.themeId) ? o.themeId : '';
  const background = typeof o.background === 'string' && /^[a-z0-9-]{1,32}$/.test(o.background) ? o.background : '';
  if (!name || !id || !themeId || !background) return null;
  return {
    id, name, themeId, background,
    accentColor: typeof o.accentColor === 'string' && HEX.test(o.accentColor) ? o.accentColor : null,
    snow: bool(o.snow, false),
    fx: sanitizeFx(o.fx),
    surface: (SURFACES as readonly string[]).includes(String(o.surface)) ? (o.surface as SurfaceStyle) : 'glass',
    stage: (STAGES as readonly string[]).includes(String(o.stage)) ? (o.stage as StageStyle) : 'portal',
  };
}

/** Custom background image: a PNG/JPEG/WebP data URL up to ~8 MB, else empty. */
export function sanitizeImage(x: unknown): string {
  return typeof x === 'string' && x.length < 11_000_000 && /^data:image\/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+$/.test(x) ? x : '';
}

export const UI_FONTS = ['theme', 'inter', 'poppins', 'nunito', 'orbitron', 'mono', 'pixel', 'custom'] as const;
export type UiFont = (typeof UI_FONTS)[number];
export const HEAD_FONTS = ['poppins', 'creepster', 'nosifer', 'orbitron', 'pixel', 'press', 'inter', 'custom'] as const;
export type HeadFont = (typeof HEAD_FONTS)[number];
export const BUTTON_STYLES = ['crypt', 'nocturne', 'brick', 'neon', 'glass', 'pixel'] as const;
export type ButtonStyle = (typeof BUTTON_STYLES)[number];
/** Page transition animations (Optionen → Animationen). */
export const TRANSITIONS = ['lego', 'slide', 'fade', 'zoom', 'glitch', 'pumpkin', 'none'] as const;
export type Transition = (typeof TRANSITIONS)[number];

export interface CustomFont { name: string; data: string }
export interface CustomLanguage { name: string; strings: Record<string, string> }

/** User font file as data URL (woff2/woff/ttf/otf, up to ~3 MB). */
export function sanitizeFont(x: unknown): CustomFont | null {
  if (!x || typeof x !== 'object') return null;
  const o = x as Record<string, unknown>;
  const name = typeof o.name === 'string' ? o.name.replace(/[^\w .-]/g, '').trim().slice(0, 40) : '';
  const data = typeof o.data === 'string' && o.data.length < 4_200_000 && /^data:(font\/(woff2|woff|ttf|otf)|application\/(font-woff2?|x-font-(ttf|otf)|octet-stream|vnd\.ms-opentype));base64,[A-Za-z0-9+/=]+$/.test(o.data) ? o.data : '';
  return name && data ? { name, data } : null;
}

/** Translation pack: only "group.key" string keys and plain text values. */
export function sanitizeLanguage(x: unknown): CustomLanguage | null {
  if (!x || typeof x !== 'object') return null;
  const o = x as Record<string, unknown>;
  const name = typeof o.name === 'string' ? o.name.trim().slice(0, 30) : '';
  if (!name || !o.strings || typeof o.strings !== 'object') return null;
  const strings: Record<string, string> = {};
  for (const [k, v] of Object.entries(o.strings as Record<string, unknown>).slice(0, 800)) {
    if (/^[a-z]+\.[A-Za-z0-9.]{1,60}$/.test(k) && typeof v === 'string' && v.trim()) strings[k] = v.slice(0, 400);
  }
  return { name, strings };
}

export const pick = <T extends string>(list: readonly T[], v: unknown, d: T): T => ((list as readonly string[]).includes(String(v)) ? (v as T) : d);

export interface SkinEntry { id: string; name: string; data: string; variant: 'classic' | 'slim' }

/** Saved skins (Skins tab): PNG data URLs that pass the 64×64 header check. */
export function sanitizeSkinLib(v: unknown): SkinEntry[] {
  if (!Array.isArray(v)) return [];
  const out: SkinEntry[] = [];
  for (const raw of v.slice(0, 40)) {
    if (!raw || typeof raw !== 'object') continue;
    const e = raw as Record<string, unknown>;
    if (typeof e.data !== 'string' || !e.data.startsWith('data:image/png;base64,') || e.data.length > 120_000) continue;
    const bytes = Uint8Array.from(atob(e.data.slice(22)), (c) => c.charCodeAt(0));
    if (!validSkinPng(bytes)) continue;
    const id = typeof e.id === 'string' && /^[a-z0-9-]{1,40}$/.test(e.id) ? e.id : `s-${out.length}-${bytes.length}`;
    const name = typeof e.name === 'string' ? e.name.replace(/[\u0000-\u001f<>]/g, '').trim().slice(0, 32) || 'Skin' : 'Skin';
    out.push({ id, name, data: e.data, variant: e.variant === 'slim' ? 'slim' : 'classic' });
  }
  return out;
}
