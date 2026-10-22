// Cape textures from design data (CapeSpec) for previews: the front as an image
// for cards, the full 64×32-layout sheet (×8) for the 3D model.
import { CAPE_FRAMES, CAPE_FRONT_H, CAPE_FRONT_W, CAPE_PRESETS, CAPE_SHEET_H, CAPE_SHEET_W, renderCapeFront, renderCapeSheet, sanitizeCapeSpec, type CapeSpec } from '@lego/shared';

const cache = new Map<string, string>();

function toUrl(px: Uint8ClampedArray, w: number, h: number): string {
  const c = document.createElement('canvas');
  c.width = w;
  c.height = h;
  c.getContext('2d')!.putImageData(new ImageData(new Uint8ClampedArray(px), w, h), 0, 0);
  return c.toDataURL('image/png');
}

export function capeFrontUrl(spec: CapeSpec): string {
  const key = `f${JSON.stringify(spec)}`;
  let u = cache.get(key);
  if (!u) {
    u = toUrl(renderCapeFront(spec), CAPE_FRONT_W, CAPE_FRONT_H);
    if (cache.size > 120) cache.clear();
    cache.set(key, u);
  }
  return u;
}

/** Sheet URLs handed out for animated capes, so the 3D figure can play their frames. */
const animatedSheets = new Map<string, CapeSpec>();

export function capeSheetUrl(spec: CapeSpec): string {
  const url = toUrl(renderCapeSheet(spec), CAPE_SHEET_W, CAPE_SHEET_H);
  if (sanitizeCapeSpec(spec).anim) {
    if (animatedSheets.size > 40) animatedSheets.clear();
    animatedSheets.set(url, spec);
  }
  return url;
}

const frameCache = new Map<string, HTMLCanvasElement[]>();

/** All frames of an animated cape sheet (as canvases), or null for a still cape / unknown URL. */
export function capeSheetFrames(url: string): HTMLCanvasElement[] | null {
  const spec = animatedSheets.get(url);
  if (!spec) return null;
  const key = JSON.stringify(spec);
  let frames = frameCache.get(key);
  if (!frames) {
    frames = Array.from({ length: CAPE_FRAMES }, (_, f) => {
      const c = document.createElement('canvas');
      c.width = CAPE_SHEET_W;
      c.height = CAPE_SHEET_H;
      c.getContext('2d')!.putImageData(new ImageData(new Uint8ClampedArray(renderCapeSheet(spec, f)), CAPE_SHEET_W, CAPE_SHEET_H), 0, 0);
      return c;
    });
    if (frameCache.size > 12) frameCache.clear();
    frameCache.set(key, frames);
  }
  return frames;
}

/** Front image of a cape at an animation frame (cards). */
export function capeFrontFrameUrl(spec: CapeSpec, frame: number): string {
  return toUrl(renderCapeFront(spec, frame), CAPE_FRONT_W, CAPE_FRONT_H);
}

/** Spec of a catalog cape drawn from data (cape_m_*), or null. */
export function presetSpec(itemId: string): CapeSpec | null {
  return itemId.startsWith('cape_') ? CAPE_PRESETS[itemId.slice(5)]?.spec ?? null : null;
}

const LOCAL_KEY = 'lego.customCape';
export function loadLocalCape(): CapeSpec | null {
  try { const v = localStorage.getItem(LOCAL_KEY); return v ? (JSON.parse(v) as CapeSpec) : null; } catch { return null; }
}
export function saveLocalCape(spec: CapeSpec): void {
  try { localStorage.setItem(LOCAL_KEY, JSON.stringify(spec)); } catch { /* storage blocked */ }
}
