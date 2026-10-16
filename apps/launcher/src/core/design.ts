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

export const DEFAULT_FX: DesignFx = { particles: 0.6, speed: 1, light: 1, fog: true, glow: true, vignette: true, parallax: true };

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
    parallax: bool(o.parallax, DEFAULT_FX.parallax),
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
