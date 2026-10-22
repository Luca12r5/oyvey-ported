// Custom capes as design data ("CapeSpec"): background, symbols, text, border
// and shine. Nobody uploads images; every client draws the cape from the spec
// with the same renderer (this file in the launcher and backend tests, a Java
// port in the LEGO client fed by the generated capes.json). The vector symbols
// and the 5×7 font are plain numbers so both renderers see identical shapes.

export const CAPE_BG_TYPES = ['solid', 'gradient', 'radial', 'stripes', 'checker', 'galaxy', 'waves', 'split'] as const;
export type CapeBgType = (typeof CAPE_BG_TYPES)[number];

export interface CapeSymbol {
  id: string;
  /** Centre in 0..1 of the cape front (x left→right, y top→bottom). */
  x: number;
  y: number;
  /** Diameter as a share of the front width (0.1..1). */
  size: number;
  /** Rotation in degrees (0..345, steps of 15). */
  rot: number;
  color: string;
  glow: boolean;
}

export interface CapeSpec {
  v: 1;
  bg: { type: CapeBgType; colors: string[]; angle: number };
  symbols: CapeSymbol[];
  border: { width: number; color: string };
  text: { value: string; color: string; y: number; size: number } | null;
  shine: boolean;
  /** Animated: symbols pulse and float, the shine sweeps, waves/stripes move, stars twinkle. */
  anim?: boolean;
}

/** Frames of an animated cape (one loop). */
export const CAPE_FRAMES = 16;

/** Front of the cape in texture pixels (10×16 Minecraft pixels × 8). */
export const CAPE_FRONT_W = 80;
export const CAPE_FRONT_H = 128;
/** Full cape sheet (64×32 layout × 8). */
export const CAPE_SHEET_W = 512;
export const CAPE_SHEET_H = 256;
export const CAPE_MAX_SYMBOLS = 6;
export const CAPE_MAX_TEXT = 10;

// ---------------------------------------------------------------- shapes

type Pt = [number, number];
export interface ShapePart { op: 'add' | 'sub'; poly: Pt[] }

const r4 = (n: number) => Math.round(n * 10000) / 10000 || 0;
const P = (pts: Pt[]): Pt[] => pts.map(([x, y]) => [r4(x), r4(y)]);
const add = (poly: Pt[]): ShapePart => ({ op: 'add', poly: P(poly) });
const sub = (poly: Pt[]): ShapePart => ({ op: 'sub', poly: P(poly) });
function ellipse(cx: number, cy: number, rx: number, ry: number, n = 36, rot = 0): Pt[] {
  const c = Math.cos(rot), s = Math.sin(rot);
  return Array.from({ length: n }, (_, i) => {
    const a = (i / n) * Math.PI * 2;
    const x = Math.cos(a) * rx, y = Math.sin(a) * ry;
    return [cx + x * c - y * s, cy + x * s + y * c] as Pt;
  });
}
const circle = (cx: number, cy: number, r: number, n = 36) => ellipse(cx, cy, r, r, n);
const rect = (x0: number, y0: number, x1: number, y1: number): Pt[] => [[x0, y0], [x1, y0], [x1, y1], [x0, y1]];
function star(n: number, ro: number, ri: number, cy = 0): Pt[] {
  return Array.from({ length: n * 2 }, (_, i) => {
    const a = -Math.PI / 2 + (i * Math.PI) / n;
    const r = i % 2 ? ri : ro;
    return [Math.cos(a) * r, cy + Math.sin(a) * r] as Pt;
  });
}
function regular(n: number, r: number, rot = -Math.PI / 2): Pt[] {
  return Array.from({ length: n }, (_, i) => [Math.cos(rot + (i * 2 * Math.PI) / n) * r, Math.sin(rot + (i * 2 * Math.PI) / n) * r] as Pt);
}
function rotated(poly: Pt[], deg: number): Pt[] {
  const a = (deg * Math.PI) / 180, c = Math.cos(a), s = Math.sin(a);
  return poly.map(([x, y]) => [x * c - y * s, x * s + y * c] as Pt);
}
function heart(): Pt[] {
  return Array.from({ length: 40 }, (_, i) => {
    const t = (i / 40) * Math.PI * 2;
    const x = 16 * Math.sin(t) ** 3;
    const y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t));
    return [x / 17, y / 17 + 0.08] as Pt;
  });
}

/** Symbol id → German label and shape (unit square -1..1, y down). */
export const CAPE_SYMBOLS: Record<string, { name: string; parts: ShapePart[] }> = {
  star: { name: 'Stern', parts: [add(star(5, 1, 0.42, 0.08))] },
  sparkle: { name: 'Funkeln', parts: [add(star(4, 1, 0.26))] },
  heart: { name: 'Herz', parts: [add(heart())] },
  bolt: { name: 'Blitz', parts: [add([[0.2, -1], [-0.6, 0.15], [-0.05, 0.15], [-0.3, 1], [0.6, -0.25], [0.05, -0.25], [0.35, -1]])] },
  crown: { name: 'Krone', parts: [add([[-1, 0.55], [-1, -0.55], [-0.5, 0.0], [0, -0.85], [0.5, 0.0], [1, -0.55], [1, 0.55]]), add(rect(-1, 0.62, 1, 0.9))] },
  moon: { name: 'Mond', parts: [add(circle(0, 0, 0.9)), sub(circle(0.42, -0.28, 0.74))] },
  gem: { name: 'Diamant', parts: [add([[-0.95, -0.3], [-0.5, -0.85], [0.5, -0.85], [0.95, -0.3], [0, 1]]), sub([[-0.95, -0.3], [0.95, -0.3], [0.95, -0.22], [-0.95, -0.22]])] },
  circle: { name: 'Kreis', parts: [add(circle(0, 0, 0.92))] },
  ring: { name: 'Ring', parts: [add(circle(0, 0, 0.95)), sub(circle(0, 0, 0.62))] },
  skull: {
    name: 'Schädel',
    parts: [add(circle(0, -0.18, 0.8)), add(rect(-0.45, 0.35, 0.45, 0.95)), sub(circle(-0.32, -0.15, 0.21, 20)), sub(circle(0.32, -0.15, 0.21, 20)),
      sub([[0, 0.12], [-0.11, 0.34], [0.11, 0.34]]), sub(rect(-0.18, 0.66, -0.1, 0.95)), sub(rect(0.1, 0.66, 0.18, 0.95))],
  },
  flame: { name: 'Flamme', parts: [add([[0, -1], [0.35, -0.45], [0.62, -0.1], [0.72, 0.35], [0.5, 0.8], [0, 1], [-0.5, 0.8], [-0.72, 0.35], [-0.55, -0.05], [-0.3, 0.15], [-0.25, -0.4]]), sub([[0, 0.05], [0.25, 0.45], [0.2, 0.75], [0, 0.85], [-0.2, 0.75], [-0.25, 0.45]])] },
  brick: { name: 'LEGO-Stein', parts: [add(rect(-1, -0.2, 1, 0.75)), add(ellipse(-0.5, -0.32, 0.3, 0.2)), add(rect(-0.8, -0.32, -0.2, -0.2)), add(ellipse(0.5, -0.32, 0.3, 0.2)), add(rect(0.2, -0.32, 0.8, -0.2))] },
  sword: { name: 'Schwert', parts: [add([[0, -1], [0.15, -0.78], [0.15, 0.35], [-0.15, 0.35], [-0.15, -0.78]]), add(rect(-0.52, 0.35, 0.52, 0.5)), add(rect(-0.09, 0.5, 0.09, 0.82)), add(circle(0, 0.9, 0.13, 16))] },
  shield: { name: 'Schild', parts: [add([[-0.85, -0.9], [0, -0.75], [0.85, -0.9], [0.85, 0.05], [0.6, 0.55], [0, 1], [-0.6, 0.55], [-0.85, 0.05]]), sub([[-0.6, -0.62], [0, -0.5], [0.6, -0.62], [0.6, 0.02], [0.42, 0.4], [0, 0.72], [-0.42, 0.4], [-0.6, 0.02]]), add([[-0.4, -0.38], [0, -0.3], [0.4, -0.38], [0.4, 0.0], [0.28, 0.25], [0, 0.45], [-0.28, 0.25], [-0.4, 0.0]])] },
  note: { name: 'Note', parts: [add(ellipse(-0.28, 0.62, 0.36, 0.28, 30, -0.4)), add(rect(0.0, -0.9, 0.13, 0.6)), add([[0.13, -0.9], [0.68, -0.58], [0.68, -0.3], [0.13, -0.6]])] },
  sun: { name: 'Sonne', parts: [add(circle(0, 0, 0.5)), ...Array.from({ length: 8 }, (_, i) => add(rotated([[-0.13, -0.62], [0.13, -0.62], [0, -1]], i * 45)))] },
  snowflake: { name: 'Schneeflocke', parts: [0, 60, 120].flatMap((d) => [add(rotated(rect(-0.08, -1, 0.08, 1), d)), add(rotated([[0, -0.62], [-0.3, -0.9], [-0.22, -0.98], [0, -0.78], [0.22, -0.98], [0.3, -0.9]], d)), add(rotated([[0, 0.62], [-0.3, 0.9], [-0.22, 0.98], [0, 0.78], [0.22, 0.98], [0.3, 0.9]], d))]) },
  plus: { name: 'Kreuz', parts: [add(rect(-0.26, -0.92, 0.26, 0.92)), add(rect(-0.92, -0.26, 0.92, 0.26))] },
  triangle: { name: 'Dreieck', parts: [add(regular(3, 1, -Math.PI / 2).map(([x, y]) => [x, y + 0.2] as Pt))] },
  hexagon: { name: 'Sechseck', parts: [add(regular(6, 0.95, 0))] },
  eye: { name: 'Auge', parts: [add(ellipse(0, 0, 1, 0.55)), sub(circle(0, 0, 0.42)), add(circle(0, 0, 0.24, 20))] },
  leaf: { name: 'Blatt', parts: [add(ellipse(0, 0, 0.95, 0.42, 36, -Math.PI / 4)), sub(rotated(rect(-0.9, -0.04, 0.9, 0.04), -45))] },
  paw: { name: 'Pfote', parts: [add(ellipse(0, 0.38, 0.5, 0.42)), add(circle(-0.62, -0.12, 0.2, 20)), add(circle(-0.25, -0.55, 0.21, 20)), add(circle(0.25, -0.55, 0.21, 20)), add(circle(0.62, -0.12, 0.2, 20))] },
  ghost: { name: 'Geist', parts: [add(circle(0, -0.25, 0.75)), add([[-0.75, -0.25], [0.75, -0.25], [0.75, 0.95], [0.38, 0.7], [0, 0.95], [-0.38, 0.7], [-0.75, 0.95]]), sub(ellipse(-0.28, -0.25, 0.13, 0.2, 20)), sub(ellipse(0.28, -0.25, 0.13, 0.2, 20))] },
  pumpkin: { name: 'Kürbis', parts: [add(ellipse(0, 0.12, 1, 0.78)), add(rect(-0.1, -0.88, 0.1, -0.6)), sub([[-0.55, -0.05], [-0.25, -0.05], [-0.4, -0.35]]), sub([[0.25, -0.05], [0.55, -0.05], [0.4, -0.35]]), sub([[-0.6, 0.25], [0.6, 0.25], [0.4, 0.55], [0.2, 0.42], [0, 0.6], [-0.2, 0.42], [-0.4, 0.55]])] },
  planet: { name: 'Planet', parts: [add(ellipse(0, 0, 1, 0.28, 40, -0.35)), sub(ellipse(0, 0, 0.78, 0.15, 40, -0.35)), add(circle(0, 0, 0.55))] },
  arrow: { name: 'Pfeil', parts: [add([[0, -1], [0.75, -0.2], [0.28, -0.2], [0.28, 1], [-0.28, 1], [-0.28, -0.2], [-0.75, -0.2]])] },
  wing: { name: 'Flügel', parts: [add([[-1, 0.2], [-0.6, -0.6], [0.1, -0.95], [1, -0.9], [0.7, -0.55], [0.95, -0.45], [0.6, -0.1], [0.85, 0.0], [0.4, 0.35], [0.6, 0.45], [0.0, 0.7], [-0.6, 0.65]])] },
};
export const CAPE_SYMBOL_IDS = Object.keys(CAPE_SYMBOLS);

/** 5×7 bitmap font for cape text (A–Z, 0–9, a few signs). */
export const CAPE_FONT: Record<string, string[]> = {
  A: ['01110', '10001', '10001', '11111', '10001', '10001', '10001'], B: ['11110', '10001', '10001', '11110', '10001', '10001', '11110'],
  C: ['01111', '10000', '10000', '10000', '10000', '10000', '01111'], D: ['11110', '10001', '10001', '10001', '10001', '10001', '11110'],
  E: ['11111', '10000', '10000', '11110', '10000', '10000', '11111'], F: ['11111', '10000', '10000', '11110', '10000', '10000', '10000'],
  G: ['01111', '10000', '10000', '10011', '10001', '10001', '01111'], H: ['10001', '10001', '10001', '11111', '10001', '10001', '10001'],
  I: ['11111', '00100', '00100', '00100', '00100', '00100', '11111'], J: ['00111', '00010', '00010', '00010', '00010', '10010', '01100'],
  K: ['10001', '10010', '10100', '11000', '10100', '10010', '10001'], L: ['10000', '10000', '10000', '10000', '10000', '10000', '11111'],
  M: ['10001', '11011', '10101', '10101', '10001', '10001', '10001'], N: ['10001', '11001', '10101', '10101', '10011', '10001', '10001'],
  O: ['01110', '10001', '10001', '10001', '10001', '10001', '01110'], P: ['11110', '10001', '10001', '11110', '10000', '10000', '10000'],
  Q: ['01110', '10001', '10001', '10001', '10101', '10010', '01101'], R: ['11110', '10001', '10001', '11110', '10100', '10010', '10001'],
  S: ['01111', '10000', '10000', '01110', '00001', '00001', '11110'], T: ['11111', '00100', '00100', '00100', '00100', '00100', '00100'],
  U: ['10001', '10001', '10001', '10001', '10001', '10001', '01110'], V: ['10001', '10001', '10001', '10001', '10001', '01010', '00100'],
  W: ['10001', '10001', '10001', '10101', '10101', '10101', '01010'], X: ['10001', '10001', '01010', '00100', '01010', '10001', '10001'],
  Y: ['10001', '10001', '01010', '00100', '00100', '00100', '00100'], Z: ['11111', '00001', '00010', '00100', '01000', '10000', '11111'],
  '0': ['01110', '10011', '10101', '10101', '10101', '11001', '01110'], '1': ['00100', '01100', '00100', '00100', '00100', '00100', '01110'],
  '2': ['01110', '10001', '00001', '00010', '00100', '01000', '11111'], '3': ['11110', '00001', '00001', '01110', '00001', '00001', '11110'],
  '4': ['00010', '00110', '01010', '10010', '11111', '00010', '00010'], '5': ['11111', '10000', '11110', '00001', '00001', '10001', '01110'],
  '6': ['01110', '10000', '10000', '11110', '10001', '10001', '01110'], '7': ['11111', '00001', '00010', '00100', '01000', '01000', '01000'],
  '8': ['01110', '10001', '10001', '01110', '10001', '10001', '01110'], '9': ['01110', '10001', '10001', '01111', '00001', '00001', '01110'],
  '!': ['00100', '00100', '00100', '00100', '00100', '00000', '00100'], '?': ['01110', '10001', '00001', '00010', '00100', '00000', '00100'],
  '-': ['00000', '00000', '00000', '11111', '00000', '00000', '00000'], '+': ['00000', '00100', '00100', '11111', '00100', '00100', '00000'],
  '.': ['00000', '00000', '00000', '00000', '00000', '00000', '00100'], ' ': ['00000', '00000', '00000', '00000', '00000', '00000', '00000'],
  '#': ['01010', '11111', '01010', '01010', '11111', '01010', '00000'], '*': ['00000', '10101', '01110', '11111', '01110', '10101', '00000'],
};

// ---------------------------------------------------------------- validation

const HEX = /^#[0-9a-f]{6}$/i;
const num = (v: unknown, min: number, max: number, d: number) => {
  const n = Number(v);
  return Number.isFinite(n) ? Math.max(min, Math.min(max, n)) : d;
};
const r3 = (n: number) => Math.round(n * 1000) / 1000;
const hex = (v: unknown, d: string) => (typeof v === 'string' && HEX.test(v) ? v.toLowerCase() : d);

/** Words that may not appear in cape text (kept short; the backend applies its own filter too). */
const BLOCKED = ['NAZI', 'HITLER', 'SS88', 'FICK', 'FUCK', 'SHIT', 'NIGG', 'HURE', 'KKK', 'CUNT', 'RAPE', 'PORN', 'SEX'];

export function cleanCapeText(v: unknown): string {
  const s = String(v ?? '').toUpperCase().replace(/[ÄÖÜ]/g, (c) => ({ Ä: 'AE', Ö: 'OE', Ü: 'UE' })[c] ?? c).replace(/ß/g, 'SS');
  const t = [...s].filter((c) => CAPE_FONT[c]).join('').replace(/\s+/g, ' ').trim().slice(0, CAPE_MAX_TEXT);
  const flat = t.replace(/[^A-Z0-9]/g, '');
  return BLOCKED.some((w) => flat.includes(w)) ? '' : t;
}

export const DEFAULT_CAPE: CapeSpec = {
  v: 1,
  bg: { type: 'gradient', colors: ['#1b1b3a', '#6d28d9'], angle: 90 },
  symbols: [{ id: 'brick', x: 0.5, y: 0.42, size: 0.55, rot: 0, color: '#f2cd37', glow: true }],
  border: { width: 3, color: '#f2cd37' },
  text: null,
  shine: true,
};

/** Validates untrusted input (editor, AI, network) into a safe spec. Same rules everywhere. */
export function sanitizeCapeSpec(x: unknown): CapeSpec {
  const o = (x && typeof x === 'object' ? x : {}) as Record<string, unknown>;
  const bg = (o.bg && typeof o.bg === 'object' ? o.bg : {}) as Record<string, unknown>;
  const type = (CAPE_BG_TYPES as readonly string[]).includes(String(bg.type)) ? (bg.type as CapeBgType) : 'solid';
  const colorsIn = Array.isArray(bg.colors) ? bg.colors : [];
  const colors = colorsIn.map((c) => hex(c, '')).filter(Boolean).slice(0, 3);
  while (colors.length < 2) colors.push(colors.length === 0 ? '#1b1b3a' : '#6d28d9');
  const symbolsIn = Array.isArray(o.symbols) ? o.symbols : [];
  const symbols: CapeSymbol[] = symbolsIn.slice(0, CAPE_MAX_SYMBOLS).flatMap((s): CapeSymbol[] => {
    if (!s || typeof s !== 'object') return [];
    const y = s as Record<string, unknown>;
    if (typeof y.id !== 'string' || !CAPE_SYMBOLS[y.id]) return [];
    return [{
      id: y.id, x: r3(num(y.x, 0, 1, 0.5)), y: r3(num(y.y, 0, 1, 0.5)), size: r3(num(y.size, 0.1, 1, 0.5)),
      rot: (Math.round(num(y.rot, 0, 359, 0) / 15) * 15) % 360, color: hex(y.color, '#ffffff'), glow: y.glow === true,
    }];
  });
  const b = (o.border && typeof o.border === 'object' ? o.border : {}) as Record<string, unknown>;
  const t = o.text && typeof o.text === 'object' ? (o.text as Record<string, unknown>) : null;
  const value = t ? cleanCapeText(t.value) : '';
  return {
    v: 1,
    bg: { type, colors, angle: Math.round(num(bg.angle, 0, 359, 90)) },
    symbols,
    border: { width: Math.round(num(b.width, 0, 8, 0)), color: hex(b.color, '#ffffff') },
    text: t && value ? { value, color: hex(t.color, '#ffffff'), y: r3(num(t.y, 0, 1, 0.8)), size: Math.round(num(t.size, 1, 3, 2)) } : null,
    shine: o.shine === true,
    ...(o.anim === true ? { anim: true } : {}),
  };
}

/** Stable short id of a spec (texture cache key in the client). */
export function capeSpecId(spec: CapeSpec): string {
  const s = JSON.stringify(sanitizeCapeSpec(spec));
  let h = 0x811c9dc5;
  for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 0x01000193); }
  return (h >>> 0).toString(16).padStart(8, '0');
}

// ---------------------------------------------------------------- renderer

type Rgb = [number, number, number];
const rgb = (h: string): Rgb => { const n = parseInt(h.slice(1), 16); return [(n >> 16) & 255, (n >> 8) & 255, n & 255]; };
const lerp = (a: Rgb, b: Rgb, t: number): Rgb => [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t];
const clamp01 = (v: number) => (v < 0 ? 0 : v > 1 ? 1 : v);

/** Integer hash in 0..1 (same in the Java port: int multiply wraps like Math.imul). */
export function capeHash(x: number, y: number, seed: number): number {
  let n = (Math.imul(x, 374761393) + Math.imul(y, 668265263) + Math.imul(seed, 982451653)) | 0;
  n = Math.imul(n ^ (n >>> 13), 1274126177);
  n ^= n >>> 16;
  return (n >>> 0) / 4294967296;
}

function inPoly(poly: Pt[], x: number, y: number): boolean {
  let inside = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i]!, [xj, yj] = poly[j]!;
    if ((yi > y) !== (yj > y) && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
  }
  return inside;
}
function inShape(parts: ShapePart[], x: number, y: number): boolean {
  let on = false;
  for (const p of parts) if (inPoly(p.poly, x, y)) on = p.op === 'add';
  return on;
}

function multiStop(colors: Rgb[], t: number): Rgb {
  t = clamp01(t);
  if (colors.length < 3) return lerp(colors[0]!, colors[1]!, t);
  return t < 0.5 ? lerp(colors[0]!, colors[1]!, t * 2) : lerp(colors[1]!, colors[2]!, (t - 0.5) * 2);
}

function background(spec: CapeSpec, x: number, y: number, w: number, h: number, t: number): Rgb {
  const cs = spec.bg.colors.map(rgb);
  const u = (x + 0.5) / w, v = (y + 0.5) / h;
  const a = (spec.bg.angle * Math.PI) / 180, ca = Math.cos(a), sa = Math.sin(a);
  const p = Math.PI * 2 * t;
  switch (spec.bg.type) {
    case 'solid': return cs[0]!;
    case 'gradient': return multiStop(cs, ((u - 0.5) * ca + (v - 0.5) * sa) / (Math.abs(ca) * 0.5 + Math.abs(sa) * 0.5 || 1) * 0.5 + 0.5 + 0.12 * Math.sin(p));
    case 'radial': return multiStop(cs, Math.hypot(u - 0.5, (v - 0.38) * (h / w) * 0.62) / 0.62 * (1 + 0.08 * Math.sin(p)));
    case 'stripes': {
      const band = Math.floor((x * ca + y * sa) / (w / 5) + 1000 + t * 5);
      return cs[band % cs.length]!;
    }
    case 'checker': return cs[(Math.floor(x / 10) + Math.floor(y / 10)) % 2]!;
    case 'galaxy': {
      let c = lerp(cs[0]!, cs[1]!, clamp01(Math.hypot(u - 0.55, v - 0.45) * 1.6));
      const neb = cs[2] ?? cs[1]!;
      const d = Math.hypot((u - 0.32) * 1.2, v - 0.62);
      c = lerp(c, neb, clamp01(0.55 - d * 1.4));
      const s = capeHash(x, y, 7);
      if (s > 0.985) c = lerp(c, [255, 255, 255], (0.6 + (s - 0.985) * 26) * (0.55 + 0.45 * Math.sin(p * 2 + s * 400)));
      return c;
    }
    case 'waves': return multiStop(cs, 0.5 + 0.5 * Math.sin(v * Math.PI * 2 * 3 + Math.sin(u * Math.PI * 2) * 1.5 + p));
    case 'split': return u + v * (h / w) * 0.62 < 0.5 + 0.31 * (h / w) ? cs[0]! : cs[1]!;
  }
}

const SS: [number, number][] = [[0.25, 0.25], [0.75, 0.25], [0.25, 0.75], [0.75, 0.75]];

/**
 * Draws the cape front (80×128 RGBA). {@code frame} (0..CAPE_FRAMES-1) is the animation frame
 * of an animated cape; still capes ignore it.
 */
export function renderCapeFront(input: CapeSpec, frame = 0): Uint8ClampedArray {
  const spec = sanitizeCapeSpec(input);
  const W = CAPE_FRONT_W, H = CAPE_FRONT_H;
  const t = spec.anim ? (((frame % CAPE_FRAMES) + CAPE_FRAMES) % CAPE_FRAMES) / CAPE_FRAMES : 0;
  const p = Math.PI * 2 * t;
  const px: Rgb[] = new Array(W * H);
  for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) px[y * W + x] = background(spec, x, y, W, H, t);

  spec.symbols.forEach((s, si) => {
    const parts = CAPE_SYMBOLS[s.id]!.parts;
    // Animated: each symbol breathes (size) and floats (height), a little out of step.
    const pulse = spec.anim ? 1 + 0.07 * Math.sin(p + si * 1.3) : 1;
    const float = spec.anim ? 0.018 * Math.sin(p + si * 0.9) : 0;
    const glowK = spec.anim ? 0.4 + 0.25 * (0.5 + 0.5 * Math.sin(p * 2 + si)) : 0.55;
    const cx = s.x * W, cy = (s.y + float) * H, half = (s.size * pulse * W) / 2;
    const a = (s.rot * Math.PI) / 180, ca = Math.cos(a), sa = Math.sin(a);
    const cov = new Float32Array(W * H);
    const x0 = Math.max(0, Math.floor(cx - half * 1.5)), x1 = Math.min(W - 1, Math.ceil(cx + half * 1.5));
    const y0 = Math.max(0, Math.floor(cy - half * 1.5)), y1 = Math.min(H - 1, Math.ceil(cy + half * 1.5));
    for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
      let hit = 0;
      for (const [ox, oy] of SS) {
        const dx = x + ox - cx, dy = y + oy - cy;
        const uu = (dx * ca + dy * sa) / half, vv = (-dx * sa + dy * ca) / half;
        if (uu >= -1.01 && uu <= 1.01 && vv >= -1.01 && vv <= 1.01 && inShape(parts, uu, vv)) hit++;
      }
      cov[y * W + x] = hit / 4;
    }
    const col = rgb(s.color);
    if (s.glow) {
      const R = 4;
      for (let y = Math.max(0, y0 - R); y <= Math.min(H - 1, y1 + R); y++) for (let x = Math.max(0, x0 - R); x <= Math.min(W - 1, x1 + R); x++) {
        let g = 0;
        for (let dy = -R; dy <= R; dy++) for (let dx = -R; dx <= R; dx++) {
          const xx = x + dx, yy = y + dy;
          if (xx < 0 || yy < 0 || xx >= W || yy >= H) continue;
          const c = cov[yy * W + xx]!;
          if (c > 0) g = Math.max(g, c * (1 - Math.hypot(dx, dy) / (R + 1)));
        }
        if (g > 0) px[y * W + x] = lerp(px[y * W + x]!, col, g * glowK);
      }
    }
    for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
      const c = cov[y * W + x]!;
      if (c > 0) px[y * W + x] = lerp(px[y * W + x]!, col, c);
    }
  });

  if (spec.text) {
    const t = spec.text, chars = [...t.value];
    const scale = Math.max(1, Math.min(t.size * 2, Math.floor((W - 6) / Math.max(1, chars.length * 6 - 1))));
    const tw = (chars.length * 6 - 1) * scale, th = 7 * scale;
    const ox = Math.round((W - tw) / 2), oy = Math.round(t.y * H - th / 2);
    const col = rgb(t.color), shadow: Rgb = [0, 0, 0];
    for (const [pass, off, color, alpha] of [[0, 1, shadow, 0.45], [1, 0, col, 1]] as [number, number, Rgb, number][]) {
      void pass;
      chars.forEach((ch, i) => {
        const g = CAPE_FONT[ch] ?? CAPE_FONT[' ']!;
        for (let gy = 0; gy < 7; gy++) for (let gx = 0; gx < 5; gx++) {
          if (g[gy]![gx] !== '1') continue;
          for (let sy = 0; sy < scale; sy++) for (let sx = 0; sx < scale; sx++) {
            const x = ox + (i * 6 + gx) * scale + sx + off, y = oy + gy * scale + sy + off;
            if (x >= 0 && y >= 0 && x < W && y < H) px[y * W + x] = lerp(px[y * W + x]!, color, alpha);
          }
        }
      });
    }
  }

  if (spec.shine) {
    for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
      const d = (x + y * 0.55) / W;
      const at = spec.anim ? -0.3 + 1.9 * t : 0.62;
      const k = Math.max(0, 1 - Math.abs(d - at) / 0.14) * 0.22;
      if (k > 0) px[y * W + x] = lerp(px[y * W + x]!, [255, 255, 255], k);
    }
  }

  const bw = spec.border.width;
  if (bw > 0) {
    const bc = rgb(spec.border.color);
    for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
      if (x < bw || y < bw || x >= W - bw || y >= H - bw) px[y * W + x] = bc;
    }
  }

  const out = new Uint8ClampedArray(W * H * 4);
  px.forEach((c, i) => { out[i * 4] = Math.round(c[0]); out[i * 4 + 1] = Math.round(c[1]); out[i * 4 + 2] = Math.round(c[2]); out[i * 4 + 3] = 255; });
  return out;
}

/** Full 512×256 cape texture in the standard 64×32 layout (×8): front, back, edges. */
export function renderCapeSheet(input: CapeSpec, frame = 0): Uint8ClampedArray {
  const spec = sanitizeCapeSpec(input);
  const W = CAPE_SHEET_W, H = CAPE_SHEET_H;
  const out = new Uint8ClampedArray(W * H * 4);
  const front = renderCapeFront(spec, frame);
  const base = rgb(spec.bg.colors[0]!);
  const back = lerp(base, [0, 0, 0], 0.35);
  const edge = spec.border.width > 0 ? rgb(spec.border.color) : lerp(base, [0, 0, 0], 0.2);
  const put = (x: number, y: number, c: Rgb) => { const i = (y * W + x) * 4; out[i] = Math.round(c[0]); out[i + 1] = Math.round(c[1]); out[i + 2] = Math.round(c[2]); out[i + 3] = 255; };
  for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) put(x, y, back);
  for (let y = 0; y < CAPE_FRONT_H; y++) for (let x = 0; x < CAPE_FRONT_W; x++) {
    const i = (y * CAPE_FRONT_W + x) * 4;
    put(8 + x, 8 + y, [front[i]!, front[i + 1]!, front[i + 2]!]);
  }
  const fill = (x0: number, y0: number, w: number, h: number, c: Rgb) => { for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) put(x, y, c); };
  fill(0, 8, 8, 128, edge);
  fill(88, 8, 8, 128, edge);
  fill(8, 0, 80, 8, edge);
  fill(88, 0, 80, 8, edge);
  return out;
}

// ---------------------------------------------------------------- presets

const sym = (id: string, x: number, y: number, size: number, color: string, glow = false, rot = 0): CapeSymbol => ({ id, x, y, size, rot, color, glow });
const spec = (type: CapeBgType, colors: string[], angle: number, symbols: CapeSymbol[], border: [number, string], shine = true, text: CapeSpec['text'] = null): CapeSpec =>
  ({ v: 1, bg: { type, colors, angle }, symbols, border: { width: border[0], color: border[1] }, text, shine, anim: true });

/** Modern catalog capes (ids cape_m_*), drawn from specs like custom capes. */
export const CAPE_PRESETS: Record<string, { name: string; spec: CapeSpec }> = {
  m_neon_bolt: { name: 'Neon-Blitz', spec: spec('gradient', ['#0b0b1e', '#1e1b4b'], 90, [sym('bolt', 0.5, 0.45, 0.7, '#22d3ee', true)], [2, '#22d3ee']) },
  m_galaxy_crown: { name: 'Galaxie-Krone', spec: spec('galaxy', ['#2e1065', '#020617', '#be185d'], 0, [sym('crown', 0.5, 0.3, 0.6, '#facc15', true), sym('sparkle', 0.25, 0.7, 0.2, '#ffffff', true), sym('sparkle', 0.75, 0.82, 0.14, '#ffffff', true)], [2, '#facc15']) },
  m_crystal: { name: 'Kristall', spec: spec('radial', ['#e0f2fe', '#38bdf8', '#0c4a6e'], 0, [sym('gem', 0.5, 0.42, 0.62, '#ffffff', true)], [2, '#bae6fd']) },
  m_glitch: { name: 'Glitch', spec: spec('stripes', ['#0f172a', '#111827', '#ec4899'], 90, [sym('eye', 0.5, 0.45, 0.7, '#22d3ee', true)], [0, '#000000'], false) },
  m_sakura: { name: 'Sakura', spec: spec('gradient', ['#fdf2f8', '#f9a8d4'], 90, [sym('leaf', 0.3, 0.3, 0.28, '#db2777', false, 30), sym('leaf', 0.68, 0.55, 0.32, '#be185d', false, 120), sym('leaf', 0.4, 0.8, 0.22, '#f472b6', false, 60)], [2, '#f472b6']) },
  m_carbon_pro: { name: 'Carbon Pro', spec: spec('checker', ['#18181b', '#27272a'], 0, [sym('hexagon', 0.5, 0.45, 0.5, '#ef4444', true)], [3, '#ef4444']) },
  m_sunset_wave: { name: 'Sonnenwelle', spec: spec('waves', ['#f97316', '#db2777', '#7c3aed'], 0, [sym('sun', 0.5, 0.35, 0.5, '#fde047', true)], [0, '#000000']) },
  m_ocean: { name: 'Ozean', spec: spec('waves', ['#082f49', '#0ea5e9'], 0, [sym('circle', 0.5, 0.3, 0.3, '#e0f2fe', true)], [2, '#7dd3fc']) },
  m_royal: { name: 'Königlich', spec: spec('gradient', ['#3b0764', '#7e22ce'], 90, [sym('crown', 0.5, 0.35, 0.62, '#fbbf24', false), sym('star', 0.5, 0.7, 0.25, '#fbbf24', true)], [4, '#fbbf24']) },
  m_inferno: { name: 'Inferno', spec: spec('gradient', ['#450a0a', '#ea580c', '#facc15'], 90, [sym('flame', 0.5, 0.5, 0.72, '#fde68a', true)], [0, '#000000']) },
  m_frost: { name: 'Frost', spec: spec('radial', ['#f0f9ff', '#7dd3fc', '#1e3a8a'], 0, [sym('snowflake', 0.5, 0.42, 0.66, '#ffffff', true)], [2, '#e0f2fe']) },
  m_toxic: { name: 'Toxisch', spec: spec('radial', ['#bef264', '#15803d', '#052e16'], 0, [sym('skull', 0.5, 0.42, 0.55, '#ecfccb', true)], [2, '#a3e635']) },
  m_space: { name: 'Weltraum', spec: spec('galaxy', ['#1e1b4b', '#000000', '#0e7490'], 0, [sym('planet', 0.5, 0.45, 0.66, '#fb923c', true)], [0, '#000000']) },
  m_heartbeat: { name: 'Herzschlag', spec: spec('gradient', ['#4c0519', '#e11d48'], 90, [sym('heart', 0.5, 0.42, 0.6, '#ffe4e6', true)], [2, '#fecdd3']) },
  m_lego_classic: { name: 'LEGO Classic', spec: spec('solid', ['#d01012', '#d01012'], 0, [sym('brick', 0.5, 0.4, 0.6, '#f2cd37', false)], [4, '#f2cd37'], true, { value: 'LEGO', color: '#ffffff', y: 0.78, size: 2 }) },
  m_shadow_wing: { name: 'Schattenflügel', spec: spec('gradient', ['#020617', '#334155'], 90, [sym('wing', 0.5, 0.45, 0.8, '#e2e8f0', true)], [2, '#64748b']) },
  m_knight: { name: 'Ritter', spec: spec('split', ['#1d4ed8', '#e5e7eb'], 0, [sym('shield', 0.5, 0.42, 0.6, '#facc15', false), sym('sword', 0.5, 0.44, 0.38, '#1f2937', false)], [3, '#facc15']) },
  m_beats: { name: 'Beats', spec: spec('gradient', ['#111827', '#4f46e5'], 45, [sym('note', 0.45, 0.42, 0.55, '#a5b4fc', true), sym('note', 0.72, 0.7, 0.28, '#e0e7ff', true, 15)], [0, '#000000']) },
  m_pumpkin_king: { name: 'Kürbiskönig', spec: spec('radial', ['#7c2d12', '#1c1917'], 0, [sym('pumpkin', 0.5, 0.48, 0.62, '#f97316', true), sym('crown', 0.5, 0.2, 0.3, '#facc15', true)], [2, '#f97316']) },
  m_paw: { name: 'Pfötchen', spec: spec('stripes', ['#fef3c7', '#fde68a'], 45, [sym('paw', 0.5, 0.45, 0.55, '#78350f', false)], [2, '#b45309']) },
};

// ---------------------------------------------------------------- offline "AI"

const COLOR_WORDS: [RegExp, string][] = [
  [/\b(rot|red|rojo|rouge)\b/, '#e11d48'], [/\b(blau|blue|azul|bleu)\b/, '#2563eb'], [/\b(hellblau|cyan|türkis|turquoise|aqua)\b/, '#22d3ee'],
  [/\b(grün|gruen|green|verde|vert)\b/, '#22c55e'], [/\b(gelb|yellow|amarillo|jaune)\b/, '#facc15'], [/\b(gold|golden|goldene?n?)\b/, '#fbbf24'],
  [/\b(orange|naranja)\b/, '#f97316'], [/\b(lila|violett|purple|violet|morado)\b/, '#8b5cf6'], [/\b(pink|rosa|rose)\b/, '#ec4899'],
  [/\b(schwarz|black|negro|noir|dunkel|dark)\b/, '#0b0b12'], [/\b(weiß|weiss|white|blanco|blanc|hell)\b/, '#f8fafc'], [/\b(grau|grey|gray|gris|silber|silver)\b/, '#94a3b8'],
  [/\b(braun|brown|marrón)\b/, '#92400e'], [/\b(mint)\b/, '#6ee7b7'], [/\b(bordeaux|weinrot)\b/, '#7f1d1d'],
];
const SYMBOL_WORDS: [RegExp, string][] = [
  [/stern|star|estrella|étoile/, 'star'], [/funkel|sparkle|glitzer/, 'sparkle'], [/herz|heart|corazón|coeur|liebe|love/, 'heart'],
  [/blitz|bolt|lightning|donner|thunder/, 'bolt'], [/kron|crown|könig|king|queen|corona/, 'crown'], [/mond|moon|luna|lune/, 'moon'],
  [/diamant|diamond|kristall|crystal|gem|edelstein/, 'gem'], [/schädel|schaedel|skull|totenkopf|calavera/, 'skull'], [/flamm|feuer|fire|flame|fuego|feu/, 'flame'],
  [/lego|stein|brick|baustein/, 'brick'], [/schwert|sword|espada|épée/, 'sword'], [/schild|shield|wappen/, 'shield'], [/note|musik|music|beat/, 'note'],
  [/sonne|sun|sol|soleil/, 'sun'], [/schnee|snow|eis|ice|frost|winter/, 'snowflake'], [/kreuz|plus|cross/, 'plus'], [/dreieck|triangle/, 'triangle'],
  [/sechseck|hexagon|wabe/, 'hexagon'], [/auge|eye|ojo/, 'eye'], [/blatt|leaf|natur|nature|sakura|blüte|bluete|blume|flower/, 'leaf'],
  [/pfote|paw|katze|cat|hund|dog/, 'paw'], [/geist|ghost|gespenst/, 'ghost'], [/kürbis|kuerbis|pumpkin|halloween/, 'pumpkin'],
  [/planet|saturn|weltall|space|kosmos/, 'planet'], [/pfeil|arrow/, 'arrow'], [/flügel|fluegel|wing|engel|angel/, 'wing'], [/kreis|circle|punkt/, 'circle'], [/ring/, 'ring'],
];

/** Builds a spec from a description without any server (keywords + seeded randomness). */
export function capeFromPrompt(prompt: string, seed = 0): CapeSpec {
  const p = prompt.toLowerCase();
  let h = 2166136261 ^ seed;
  for (let i = 0; i < p.length; i++) { h ^= p.charCodeAt(i); h = Math.imul(h, 16777619); }
  let st = h >>> 0;
  const rnd = () => { st = (Math.imul(st ^ (st >>> 15), 2246822507) + 0x9e3779b9) >>> 0; return st / 4294967296; };
  const pickArr = <T,>(a: readonly T[]): T => a[Math.floor(rnd() * a.length)]!;

  // German adjectives get endings ("rotes", "blauen"): drop them before matching colour words.
  const pc = p.replace(/([a-zäöüß]{3,})(es|en|er|em|e)\b/g, (m, stem: string) => (COLOR_WORDS.some(([re]) => re.test(stem)) ? stem : m));
  // A colour right before a symbol word belongs to that symbol ("goldene Krone"); the rest colour the background.
  const words = pc.split(/[^a-zäöüß0-9]+/).filter(Boolean);
  const colorOf = (w: string) => COLOR_WORDS.find(([re]) => re.test(w))?.[1];
  const symbolOf = (w: string) => SYMBOL_WORDS.find(([re]) => re.test(w))?.[1];
  const symColors = new Map<string, string>();
  const used = new Set<number>();
  words.forEach((w, i) => {
    const sid = symbolOf(w);
    if (!sid) return;
    for (const k of [i - 1, i - 2]) {
      const c = k >= 0 && !used.has(k) ? colorOf(words[k]!) : undefined;
      if (c && c !== '#0b0b12') { symColors.set(sid, c); used.add(k); break; }
    }
  });
  const colors = [...new Set(words.flatMap((w, i) => (used.has(i) ? [] : [colorOf(w)].filter((c): c is string => !!c))))];
  const symbols = [...new Set(words.map(symbolOf).filter((x): x is string => !!x))].slice(0, 3);
  const PALETTES = [['#0f172a', '#4f46e5', '#22d3ee'], ['#1a0b2e', '#7c3aed', '#f472b6'], ['#052e16', '#16a34a', '#bef264'], ['#450a0a', '#dc2626', '#fbbf24'], ['#0c4a6e', '#38bdf8', '#e0f2fe'], ['#111827', '#374151', '#f9fafb']];
  const pal = pickArr(PALETTES);
  const dark = colors.find((c) => c === '#0b0b12');
  const main = colors.filter((c) => c !== '#0b0b12');
  const bgColors = main.length >= 2 ? [dark ?? '#0b0b12', main[0]!, main[1]!].slice(dark ? 0 : 1) : main.length === 1 ? [dark ?? '#0b0b12', main[0]!] : pal.slice(0, 2);
  let type: CapeBgType = /galax|weltall|space|kosmos|sterne|stars/.test(p) ? 'galaxy' : /streif|stripe|rayas/.test(p) ? 'stripes' : /well|wave|ozean|ocean|wasser|water/.test(p) ? 'waves'
    : /karo|checker|schach/.test(p) ? 'checker' : /geteilt|split|halb/.test(p) ? 'split' : /einfarbig|solid|schlicht|plain/.test(p) ? 'solid' : /kreis|radial|leucht|glow/.test(p) ? 'radial' : pickArr(['gradient', 'radial', 'gradient', 'waves'] as const);
  if (type === 'galaxy') {
    if (bgColors[0] !== '#0b0b12' && bgColors[0] !== '#0f172a') bgColors.unshift('#0b0b12');
    if (bgColors.length < 3) bgColors.push(pickArr(['#be185d', '#0e7490', '#7c3aed']));
  }
  const accent = main[main.length > 1 ? 1 : 0] ?? pal[2]!;
  const symColor = main.length > 2 ? main[2]! : /gold|golden/.test(p) ? '#fbbf24' : accent === bgColors[bgColors.length - 1] ? '#ffffff' : accent;
  const glow = /glow|leucht|neon|glüh|shine|glänz/.test(p) || rnd() > 0.4;
  const ids = symbols.length ? symbols : [pickArr(CAPE_SYMBOL_IDS)];
  const layouts: [number, number, number][][] = [[[0.5, 0.42, 0.62]], [[0.5, 0.32, 0.5], [0.5, 0.72, 0.3]], [[0.5, 0.3, 0.42], [0.3, 0.7, 0.28], [0.7, 0.7, 0.28]]];
  const lay = layouts[ids.length - 1]!;
  const quoted = /"([^"]{1,20})"|„([^“"]{1,20})[“"]/.exec(prompt);
  const text = quoted ? cleanCapeText(quoted[1] ?? quoted[2]) : '';
  return sanitizeCapeSpec({
    v: 1,
    bg: { type, colors: bgColors, angle: pickArr([90, 90, 45, 135, 0]) },
    symbols: ids.map((id, i) => ({ id, x: lay[i]![0], y: text && lay[i]![1] > 0.6 ? lay[i]![1] - 0.08 : lay[i]![1], size: lay[i]![2], rot: 0, color: symColors.get(id) ?? (i === 0 ? symColor : '#ffffff'), glow })),
    border: { width: /rand|border|rahmen/.test(p) || rnd() > 0.5 ? pickArr([2, 3, 4]) : 0, color: symColor },
    text: text ? { value: text, color: '#ffffff', y: 0.86, size: 2 } : null,
    shine: !/matt|matte|flat/.test(p),
    anim: !/still|statisch|ruhig|static/.test(p),
  });
}

/** JSON schema of a CapeSpec for an AI tool call (the server validates the answer with sanitizeCapeSpec). */
export const CAPE_SPEC_SCHEMA = {
  type: 'object',
  properties: {
    bg: {
      type: 'object',
      properties: {
        type: { type: 'string', enum: [...CAPE_BG_TYPES] },
        colors: { type: 'array', items: { type: 'string', pattern: '^#[0-9a-fA-F]{6}$' }, minItems: 2, maxItems: 3 },
        angle: { type: 'integer', minimum: 0, maximum: 359 },
      },
      required: ['type', 'colors', 'angle'],
    },
    symbols: {
      type: 'array',
      maxItems: CAPE_MAX_SYMBOLS,
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', enum: CAPE_SYMBOL_IDS },
          x: { type: 'number', minimum: 0, maximum: 1 },
          y: { type: 'number', minimum: 0, maximum: 1 },
          size: { type: 'number', minimum: 0.1, maximum: 1 },
          rot: { type: 'integer', minimum: 0, maximum: 345 },
          color: { type: 'string', pattern: '^#[0-9a-fA-F]{6}$' },
          glow: { type: 'boolean' },
        },
        required: ['id', 'x', 'y', 'size', 'rot', 'color', 'glow'],
      },
    },
    border: { type: 'object', properties: { width: { type: 'integer', minimum: 0, maximum: 8 }, color: { type: 'string', pattern: '^#[0-9a-fA-F]{6}$' } }, required: ['width', 'color'] },
    text: {
      type: ['object', 'null'],
      properties: { value: { type: 'string', maxLength: CAPE_MAX_TEXT }, color: { type: 'string' }, y: { type: 'number' }, size: { type: 'integer', minimum: 1, maximum: 3 } },
    },
    shine: { type: 'boolean' },
    anim: { type: 'boolean' },
  },
  required: ['bg', 'symbols', 'border', 'text', 'shine', 'anim'],
} as const;

/** Everything the Java renderer needs (written to the client's capes.json by tools/gen-cape-assets.mjs). */
export function capeAssets(): { symbols: Record<string, ShapePart[]>; font: Record<string, string[]>; presets: Record<string, CapeSpec>; names: Record<string, string> } {
  return {
    names: Object.fromEntries(Object.entries(CAPE_PRESETS).map(([id, p]) => [id, p.name])),
    symbols: Object.fromEntries(Object.entries(CAPE_SYMBOLS).map(([id, s]) => [id, s.parts])),
    font: CAPE_FONT,
    presets: Object.fromEntries(Object.entries(CAPE_PRESETS).map(([id, p]) => [id, sanitizeCapeSpec(p.spec)])),
  };
}
