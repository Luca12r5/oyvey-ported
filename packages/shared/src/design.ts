// "Nocturne" design system shared by launcher, website and (mirrored as Java
// constants in lego-client ui/Style.java) the in-game client, so all three use
// the same palette, timing and shapes.

export const NOCTURNE = {
  void: '#07060b',
  night: '#15101e',
  crypt: '#221933',
  bone: '#f3ecff',
  fog: '#a99bc4',
  pumpkin: '#ff7a1a',
  ember: '#ffb347',
  violet: '#8b5cf6',
  hex: '#c084fc',
  blood: '#e11d48',
  slime: '#39ff88',
} as const;

/** Durations in ms. */
export const MOTION = { fast: 140, base: 240, slow: 420, cinematic: 900 } as const;

/** CSS easing curves (the Java client uses the same control points in ui/Ease). */
export const EASE = {
  out: 'cubic-bezier(.2, .8, .2, 1)',
  inOut: 'cubic-bezier(.65, 0, .35, 1)',
  spring: 'cubic-bezier(.34, 1.56, .64, 1)',
} as const;

export const RADII = { sm: 8, md: 14, lg: 20, pill: 999 } as const;

/** Hex colour → [r, g, b] in 0..1 (for WebGL / three.js). */
export function rgb01(hex: string): [number, number, number] {
  const n = parseInt(hex.slice(1), 16);
  return [((n >> 16) & 255) / 255, ((n >> 8) & 255) / 255, (n & 255) / 255];
}
