// Small colour helpers (hex <-> rgb, mixing, contrast). No dependencies.

export interface Rgb { r: number; g: number; b: number }

const HEX = /^#([0-9a-f]{6})$/i;

export function isHexColor(v: unknown): v is string {
  return typeof v === 'string' && HEX.test(v);
}

export function hexToRgb(hex: string): Rgb {
  const m = HEX.exec(hex);
  if (!m || !m[1]) throw new Error(`invalid hex colour: ${hex}`);
  const n = parseInt(m[1], 16);
  return { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 };
}

export function rgbToHex({ r, g, b }: Rgb): string {
  const c = (x: number) => Math.max(0, Math.min(255, Math.round(x))).toString(16).padStart(2, '0');
  return `#${c(r)}${c(g)}${c(b)}`;
}

/** Linear mix: t=0 -> a, t=1 -> b. */
export function mix(a: string, b: string, t: number): string {
  const x = hexToRgb(a);
  const y = hexToRgb(b);
  return rgbToHex({ r: x.r + (y.r - x.r) * t, g: x.g + (y.g - x.g) * t, b: x.b + (y.b - x.b) * t });
}

function channel(c: number): number {
  const s = c / 255;
  return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4;
}

/** WCAG relative luminance. */
export function luminance(hex: string): number {
  const { r, g, b } = hexToRgb(hex);
  return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b);
}

/** WCAG contrast ratio between two colours (1..21). */
export function contrast(a: string, b: string): number {
  const la = luminance(a);
  const lb = luminance(b);
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

/** Packs a hex colour + alpha (0..1) into a signed ARGB int as used by Minecraft. */
export function toArgbInt(hex: string, alpha = 1): number {
  const { r, g, b } = hexToRgb(hex);
  const a = Math.max(0, Math.min(255, Math.round(alpha * 255)));
  return ((a << 24) | (r << 16) | (g << 8) | b) | 0;
}
