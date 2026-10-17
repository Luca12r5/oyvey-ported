// Spatial UI panels for the VR lobby: a canvas texture on a plane in 3D, with
// clickable regions. Pointers (VR controller rays or the desktop mouse) hit the
// plane, the hit UV becomes canvas pixels, and the region under it reacts.

import * as THREE from 'three';
import { NOCTURNE } from '@lego/shared';

export interface Region { id: string; x: number; y: number; w: number; h: number; onClick?: () => void; onPointer?: (kind: PointerKind, px: number, py: number) => void }
export type PointerKind = 'move' | 'down' | 'up' | 'wheel';

export interface Pal { accent: string; accent2: string; text: string; muted: string; bg: string }

export class Panel {
  readonly canvas = document.createElement('canvas');
  readonly ctx: CanvasRenderingContext2D;
  readonly texture: THREE.CanvasTexture;
  readonly mesh: THREE.Mesh;
  readonly group = new THREE.Group();
  regions: Region[] = [];
  hover: string | null = null;
  pressed: string | null = null;
  /** Panel width in metres (height follows the canvas aspect). */
  readonly widthM: number;
  private drawFn: (p: Panel) => void;
  pal: Pal;

  constructor(pxW: number, pxH: number, widthM: number, draw: (p: Panel) => void, pal: Pal) {
    this.canvas.width = pxW;
    this.canvas.height = pxH;
    this.ctx = this.canvas.getContext('2d')!;
    this.texture = new THREE.CanvasTexture(this.canvas);
    this.texture.colorSpace = THREE.SRGBColorSpace;
    this.texture.anisotropy = 4;
    this.widthM = widthM;
    this.drawFn = draw;
    this.pal = pal;
    const h = (widthM * pxH) / pxW;
    this.mesh = new THREE.Mesh(new THREE.PlaneGeometry(widthM, h), new THREE.MeshBasicMaterial({ map: this.texture, transparent: true, toneMapped: false, side: THREE.DoubleSide }));
    this.mesh.userData.panel = this;
    // Glowing frame behind the panel for depth.
    const frame = new THREE.Mesh(new THREE.PlaneGeometry(widthM + 0.04, h + 0.04), new THREE.MeshBasicMaterial({ color: new THREE.Color(pal.accent2).multiplyScalar(0.9), transparent: true, opacity: 0.35, toneMapped: false }));
    frame.position.z = -0.006;
    this.group.add(frame, this.mesh);
    this.redraw();
  }

  redraw(): void {
    this.regions = [];
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.drawFn(this);
    this.texture.needsUpdate = true;
  }

  /** UV of a ray hit -> canvas pixels. */
  toPx(uv: THREE.Vector2): [number, number] {
    return [uv.x * this.canvas.width, (1 - uv.y) * this.canvas.height];
  }

  regionAt(px: number, py: number): Region | undefined {
    for (let i = this.regions.length - 1; i >= 0; i--) {
      const r = this.regions[i]!;
      if (px >= r.x && px <= r.x + r.w && py >= r.y && py <= r.y + r.h) return r;
    }
    return undefined;
  }

  pointer(kind: PointerKind, uv: THREE.Vector2 | null): void {
    if (!uv) { if (this.hover) { this.hover = null; this.redraw(); } return; }
    const [px, py] = this.toPx(uv);
    const r = this.regionAt(px, py);
    r?.onPointer?.(kind, px - r.x, py - r.y);
    if (kind === 'move') {
      const id = r?.onClick ? r.id : null;
      if (id !== this.hover) { this.hover = id; this.redraw(); }
    } else if (kind === 'down') {
      this.pressed = r?.id ?? null;
      this.redraw();
    } else if (kind === 'up') {
      const was = this.pressed;
      this.pressed = null;
      if (r && r.id === was) r.onClick?.();
      this.redraw();
    }
  }

  // ---- drawing helpers ------------------------------------------------------

  bg(title?: string): void {
    const { ctx: c, canvas } = this;
    const g = c.createLinearGradient(0, 0, 0, canvas.height);
    g.addColorStop(0, 'rgba(26,18,40,.94)');
    g.addColorStop(1, 'rgba(9,7,14,.94)');
    round(c, 0, 0, canvas.width, canvas.height, 36);
    c.fillStyle = g;
    c.fill();
    c.lineWidth = 3;
    const e = c.createLinearGradient(0, 0, canvas.width, canvas.height);
    e.addColorStop(0, this.pal.accent2);
    e.addColorStop(1, this.pal.accent);
    c.strokeStyle = e;
    c.stroke();
    if (title) this.text(title, 40, 64, { size: 40, weight: 700, font: 'Poppins' });
  }

  text(s: string, x: number, y: number, o: { size?: number; weight?: number; color?: string; align?: CanvasTextAlign; font?: string; max?: number } = {}): void {
    const c = this.ctx;
    c.font = `${o.weight ?? 500} ${o.size ?? 26}px ${o.font ?? 'Inter'}, sans-serif`;
    c.fillStyle = o.color ?? this.pal.text;
    c.textAlign = o.align ?? 'left';
    c.textBaseline = 'middle';
    let t = s;
    if (o.max) while (t.length > 1 && c.measureText(t).width > o.max) t = `${t.slice(0, -2)}…`;
    c.fillText(t, x, y);
  }

  button(id: string, x: number, y: number, w: number, h: number, label: string, onClick: () => void, o: { primary?: boolean; active?: boolean; size?: number } = {}): void {
    const c = this.ctx;
    const hot = this.hover === id, down = this.pressed === id;
    round(c, x, y + (down ? 3 : 0), w, h, 18);
    if (o.primary || o.active) {
      const g = c.createLinearGradient(x, y, x + w, y + h);
      g.addColorStop(0, this.pal.accent);
      g.addColorStop(1, this.pal.accent2);
      c.fillStyle = g;
    } else c.fillStyle = hot ? 'rgba(255,255,255,.16)' : 'rgba(255,255,255,.07)';
    c.fill();
    if (hot) { c.lineWidth = 3; c.strokeStyle = this.pal.accent; c.stroke(); }
    this.text(label, x + w / 2, y + h / 2 + (down ? 3 : 0), { size: o.size ?? 26, weight: 600, align: 'center', color: o.primary || o.active ? '#fff' : this.pal.text, max: w - 16 });
    this.regions.push({ id, x, y, w, h, onClick });
  }

  toggle(id: string, x: number, y: number, w: number, label: string, on: boolean, onClick: () => void, sub?: string): void {
    const c = this.ctx;
    const h = 74;
    const hot = this.hover === id;
    round(c, x, y, w, h, 18);
    c.fillStyle = hot ? 'rgba(255,255,255,.1)' : 'rgba(255,255,255,.04)';
    c.fill();
    this.text(label, x + 22, y + (sub ? 26 : h / 2), { size: 26, weight: 600, max: w - 140 });
    if (sub) this.text(sub, x + 22, y + 52, { size: 19, color: this.pal.muted, max: w - 140 });
    const sx = x + w - 96, sy = y + h / 2 - 18;
    round(c, sx, sy, 72, 36, 18);
    c.fillStyle = on ? this.pal.accent : 'rgba(255,255,255,.2)';
    c.fill();
    c.beginPath();
    c.arc(on ? sx + 54 : sx + 18, sy + 18, 13, 0, Math.PI * 2);
    c.fillStyle = '#fff';
    c.fill();
    this.regions.push({ id, x, y, w, h, onClick });
  }

  dispose(): void {
    this.texture.dispose();
    this.group.traverse((o) => { const m = o as THREE.Mesh; m.geometry?.dispose(); (m.material as THREE.Material | undefined)?.dispose(); });
  }
}

export function round(c: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number): void {
  c.beginPath();
  c.roundRect(x, y, w, h, r);
}

export function palette(): Pal {
  const cs = getComputedStyle(document.documentElement);
  const v = (n: string, d: string) => cs.getPropertyValue(n).trim() || d;
  return { accent: v('--accent', NOCTURNE.pumpkin), accent2: v('--accent2', NOCTURNE.violet), text: v('--text', NOCTURNE.bone), muted: v('--muted', NOCTURNE.fog), bg: v('--bg', NOCTURNE.void) };
}
