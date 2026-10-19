// Minecraft skins from a short description ("SkinSpec"): skin tone, hair,
// eyes, beard, glasses, top, trousers, shoes and one accessory. Used by
// "Skin aus Foto": the AI (or the offline colour sampler) fills the spec, this
// file paints a full 64×64 skin with both layers and soft shading.

export const HAIR_STYLES = ['short', 'buzz', 'long', 'curly', 'ponytail', 'bald'] as const;
export const TOP_KINDS = ['tshirt', 'hoodie', 'shirt', 'jacket'] as const;
export const TOP_PATTERNS = ['none', 'stripes', 'logo'] as const;
export const GLASSES = ['none', 'normal', 'sun'] as const;
export const ACCESSORIES = ['none', 'cap', 'beanie', 'headphones'] as const;

export interface SkinSpec {
  skin: string;
  hair: string;
  hairStyle: (typeof HAIR_STYLES)[number];
  eyes: string;
  beard: boolean;
  glasses: (typeof GLASSES)[number];
  top: { kind: (typeof TOP_KINDS)[number]; color: string; color2: string; pattern: (typeof TOP_PATTERNS)[number] };
  pants: string;
  shoes: string;
  accessory: (typeof ACCESSORIES)[number];
  accessoryColor: string;
  slim: boolean;
}

export const DEFAULT_SKIN_SPEC: SkinSpec = {
  skin: '#e0ac86', hair: '#4a2f1b', hairStyle: 'short', eyes: '#3b6fb6', beard: false, glasses: 'none',
  top: { kind: 'hoodie', color: '#2563eb', color2: '#f8fafc', pattern: 'none' }, pants: '#1f2937', shoes: '#f1f5f9',
  accessory: 'none', accessoryColor: '#111827', slim: false,
};

const HEX = /^#[0-9a-f]{6}$/i;
const hex = (v: unknown, d: string) => (typeof v === 'string' && HEX.test(v) ? v.toLowerCase() : d);
const pick = <T extends string>(list: readonly T[], v: unknown, d: T): T => ((list as readonly string[]).includes(String(v)) ? (v as T) : d);

export function sanitizeSkinSpec(x: unknown): SkinSpec {
  const o = (x && typeof x === 'object' ? x : {}) as Record<string, unknown>;
  const t = (o.top && typeof o.top === 'object' ? o.top : {}) as Record<string, unknown>;
  const d = DEFAULT_SKIN_SPEC;
  return {
    skin: hex(o.skin, d.skin), hair: hex(o.hair, d.hair), hairStyle: pick(HAIR_STYLES, o.hairStyle, d.hairStyle), eyes: hex(o.eyes, d.eyes),
    beard: o.beard === true, glasses: pick(GLASSES, o.glasses, 'none'),
    top: { kind: pick(TOP_KINDS, t.kind, d.top.kind), color: hex(t.color, d.top.color), color2: hex(t.color2, d.top.color2), pattern: pick(TOP_PATTERNS, t.pattern, 'none') },
    pants: hex(o.pants, d.pants), shoes: hex(o.shoes, d.shoes), accessory: pick(ACCESSORIES, o.accessory, 'none'), accessoryColor: hex(o.accessoryColor, d.accessoryColor),
    slim: o.slim === true,
  };
}

type Rgba = [number, number, number, number];
const rgb = (h: string): Rgba => { const n = parseInt(h.slice(1), 16); return [(n >> 16) & 255, (n >> 8) & 255, n & 255, 255]; };
const shade = (c: Rgba, f: number): Rgba => [Math.max(0, Math.min(255, Math.round(c[0] * f))), Math.max(0, Math.min(255, Math.round(c[1] * f))), Math.max(0, Math.min(255, Math.round(c[2] * f))), c[3]];
function noise(x: number, y: number, s: number): number {
  let n = (Math.imul(x, 374761393) + Math.imul(y, 668265263) + Math.imul(s, 982451653)) | 0;
  n = Math.imul(n ^ (n >>> 13), 1274126177);
  return (((n ^ (n >>> 16)) >>> 0) % 1000) / 1000;
}

type Face = 'top' | 'bottom' | 'right' | 'front' | 'left' | 'back';
/** Paints one pixel of a face; return null to leave it transparent (second layer). */
type Painter = (face: Face, x: number, y: number, w: number, h: number) => Rgba | null;

const FACE_LIGHT: Record<Face, number> = { top: 1.1, bottom: 0.8, right: 0.92, front: 1, left: 0.92, back: 0.86 };

/** Draws a Minecraft box (texture origin u,v; size w×h×d) with a painter. */
function box(px: Uint8ClampedArray, u: number, v: number, w: number, h: number, d: number, paint: Painter, seed: number): void {
  const faces: [Face, number, number, number, number][] = [
    ['top', u + d, v, w, d], ['bottom', u + d + w, v, w, d],
    ['right', u, v + d, d, h], ['front', u + d, v + d, w, h], ['left', u + d + w, v + d, d, h], ['back', u + d + w + d, v + d, w, h],
  ];
  for (const [face, fx, fy, fw, fh] of faces) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
      const c = paint(face, x, y, fw, fh);
      if (!c) continue;
      const k = FACE_LIGHT[face] * (0.95 + noise(fx + x, fy + y, seed) * 0.1);
      const o = ((fy + y) * 64 + fx + x) * 4;
      const s = shade(c, k);
      px[o] = s[0]; px[o + 1] = s[1]; px[o + 2] = s[2]; px[o + 3] = s[3];
    }
  }
}

/** Paints a 64×64 RGBA skin from the spec. */
export function renderSkin(input: SkinSpec): Uint8ClampedArray {
  const s = sanitizeSkinSpec(input);
  const px = new Uint8ClampedArray(64 * 64 * 4);
  const SKIN = rgb(s.skin), HAIR = rgb(s.hair), EYES = rgb(s.eyes), WHITE: Rgba = [245, 245, 245, 255];
  const TOP = rgb(s.top.color), TOP2 = rgb(s.top.color2), PANTS = rgb(s.pants), SHOES = rgb(s.shoes), ACC = rgb(s.accessoryColor);
  const dark = (c: Rgba, f = 0.72) => shade(c, f);
  const style = s.hairStyle;
  const hairRows = style === 'bald' ? -1 : style === 'buzz' ? 0 : style === 'long' || style === 'ponytail' ? 7 : 3;
  const curly = style === 'curly';
  const longSleeve = s.top.kind !== 'tshirt';
  const arm = s.slim ? 3 : 4;

  // ---- head (base)
  box(px, 0, 0, 8, 8, 8, (face, x, y) => {
    if (style !== 'bald' && face === 'top') return curly && (x + y) % 3 === 0 ? dark(HAIR, 0.85) : HAIR;
    if (face === 'bottom') return dark(SKIN, 0.85);
    if (face === 'front') {
      if (style !== 'bald' && (y === 0 || (y === 1 && style !== 'buzz') || ((style === 'long' || style === 'ponytail' || curly) && y <= 3 && (x === 0 || x === 7)))) return curly && (x + y) % 2 ? dark(HAIR, 0.85) : HAIR;
      if (y === 3 && (x === 1 || x === 2 || x === 5 || x === 6) && style !== 'bald') return dark(HAIR, 0.9);
      if (y === 4) {
        if (x === 1 || x === 6) return WHITE;
        if (x === 2 || x === 5) return EYES;
      }
      if (y === 5 && (x === 3 || x === 4)) return dark(SKIN, 0.86);
      if (s.beard && (y >= 6 || (y === 5 && (x <= 1 || x >= 6)))) return y === 6 && (x === 3 || x === 4) ? dark(SKIN, 0.55) : HAIR;
      if (y === 6 && (x === 3 || x === 4)) return dark(SKIN, 0.62);
      return SKIN;
    }
    if (face === 'back') {
      if (style === 'bald') return SKIN;
      return y <= hairRows || style === 'buzz' && y === 0 ? (curly && (x + y) % 2 ? dark(HAIR, 0.85) : HAIR) : SKIN;
    }
    // sides
    if (style !== 'bald' && (y <= Math.max(1, hairRows - (style === 'long' || style === 'ponytail' ? 0 : 1)) || (y <= 2 && (face === 'right' ? x <= 1 : x >= 6)))) return curly && (x + y) % 2 ? dark(HAIR, 0.85) : HAIR;
    if (s.beard && y >= 5 && (face === 'right' ? x >= 5 : x <= 2)) return HAIR;
    return SKIN;
  }, 1);

  // ---- head (hat layer): glasses, hair volume, accessories
  box(px, 32, 0, 8, 8, 8, (face, x, y) => {
    if (s.glasses !== 'none' && face === 'front' && y === 4) {
      if (x === 0 || x === 3 || x === 4 || x === 7) return [30, 30, 34, 255];
      if (s.glasses === 'sun') return [18, 18, 22, 235];
      return null;
    }
    if (s.glasses !== 'none' && (face === 'right' || face === 'left') && y === 4) return [30, 30, 34, 255];
    if (s.accessory === 'cap') {
      if (face === 'top') return ACC;
      if (face === 'front' && y === 0) return ACC;
      if (face === 'front' && y === 1) return dark(ACC, 0.8);
      if ((face === 'right' || face === 'left' || face === 'back') && y <= 1) return ACC;
      return null;
    }
    if (s.accessory === 'beanie') {
      if (face === 'top') return (x + y) % 2 ? ACC : dark(ACC, 0.88);
      if (face !== 'bottom' && y <= 2) return y === 2 ? dark(ACC, 0.8) : ACC;
      return null;
    }
    if (s.accessory === 'headphones') {
      if (face === 'top' && (x === 3 || x === 4)) return ACC;
      if ((face === 'right' || face === 'left') && y >= 2 && y <= 5 && x >= 2 && x <= 5) return y === 2 || y === 5 ? dark(ACC, 0.8) : ACC;
      if ((face === 'right' || face === 'left') && y < 2 && (x === 3 || x === 4)) return ACC;
      return null;
    }
    if (curly && face !== 'bottom' && face !== 'front' && y <= 2 && (x + y) % 2 === 0) return dark(HAIR, 0.92);
    if (style === 'ponytail' && face === 'back' && y >= 2 && y <= 7 && (x === 3 || x === 4)) return dark(HAIR, 0.85);
    return null;
  }, 2);

  // ---- body
  const topPaint: Painter = (face, x, y, w) => {
    if (face === 'top') return TOP;
    if (face === 'bottom') return PANTS;
    if (y === 11) return dark(PANTS, 0.6); // belt
    if (face === 'front') {
      if (s.top.kind === 'shirt' && y === 0 && (x === 3 || x === 4)) return SKIN;
      if (s.top.kind === 'shirt' && (x === 3 || x === 4) && y % 3 === 1) return TOP2;
      if (s.top.kind === 'hoodie' && y >= 7 && y <= 9 && x >= 2 && x <= w - 3) return dark(TOP, 0.82);
      if (s.top.kind === 'hoodie' && y <= 3 && (x === 2 || x === 5)) return TOP2;
      if (s.top.kind === 'jacket' && x >= 3 && x <= 4) return TOP2;
      if (s.top.pattern === 'logo' && y >= 3 && y <= 5 && x >= 3 && x <= 4) return TOP2;
    }
    if (s.top.pattern === 'stripes' && y % 4 === 2) return TOP2;
    if (s.top.kind === 'hoodie' && face === 'back' && y <= 2) return dark(TOP, 0.85);
    return TOP;
  };
  box(px, 16, 16, 8, 12, 4, topPaint, 3);
  // jacket layer: open jacket edges / hoodie hood
  box(px, 16, 32, 8, 12, 4, (face, x, y, w) => {
    if (s.top.kind === 'jacket' && y < 11) {
      if (face === 'front') return x <= 2 || x >= w - 3 ? dark(TOP, 0.92) : null;
      if (face === 'top') return null;
      return face === 'bottom' ? null : dark(TOP, 0.92);
    }
    return null;
  }, 4);

  // ---- arms
  const armPaint = (layer: boolean): Painter => (face, _x, y) => {
    if (layer) return null;
    if (face === 'top') return TOP;
    if (face === 'bottom') return SKIN;
    const sleeveEnd = longSleeve ? 10 : 4;
    if (y < sleeveEnd) return s.top.pattern === 'stripes' && y % 4 === 2 ? TOP2 : s.top.kind === 'jacket' ? dark(TOP, 0.92) : TOP;
    if (longSleeve && y === sleeveEnd) return dark(TOP, 0.8);
    return SKIN;
  };
  box(px, 40, 16, arm, 12, 4, armPaint(false), 5);
  box(px, 32, 48, arm, 12, 4, armPaint(false), 6);

  // ---- legs
  const legPaint: Painter = (face, _x, y) => {
    if (face === 'top') return PANTS;
    if (face === 'bottom') return dark(SHOES, 0.7);
    if (y >= 10) return y === 11 ? dark(SHOES, 0.75) : SHOES;
    return PANTS;
  };
  box(px, 0, 16, 4, 12, 4, legPaint, 7);
  box(px, 16, 48, 4, 12, 4, legPaint, 8);
  return px;
}
