// Generates the default launcher character: a LEGO-style minifigure as a
// 64x64 Minecraft skin (classic arms). Pure Node, no dependencies.
//   node tools/gen-minifig-skin.mjs  ->  src/renderer/assets/minifig.png
import { writeFileSync } from 'node:fs';
import { deflateSync, crc32 } from 'node:zlib';

const W = 64, H = 64;
const px = new Uint8Array(W * H * 4);
const hex = (h) => [parseInt(h.slice(1, 3), 16), parseInt(h.slice(3, 5), 16), parseInt(h.slice(5, 7), 16)];
const shade = (c, f) => c.map((v) => Math.max(0, Math.min(255, Math.round(v * f))));
function set(x, y, c, a = 255) { const i = (y * W + x) * 4; px[i] = c[0]; px[i + 1] = c[1]; px[i + 2] = c[2]; px[i + 3] = a; }
function rect(x, y, w, h, c, f = 1) { for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) set(x + i, y + j, shade(c, f)); }
/** A box face with a soft vertical gradient (moulded plastic look). */
function face(x, y, w, h, c, f = 1) {
  for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
    const g = 1.06 - (j / Math.max(1, h - 1)) * 0.14;
    set(x + i, y + j, shade(c, f * g));
  }
}
/** Box UV layout: top, bottom, right, front, left, back. */
function box(ox, oy, w, h, d, c) {
  face(ox + d, oy, w, d, c, 1.12);            // top
  face(ox + d + w, oy, w, d, c, 0.7);         // bottom
  face(ox, oy + d, d, h, c, 0.84);            // right
  face(ox + d, oy + d, w, h, c, 1.0);         // front
  face(ox + d + w, oy + d, d, h, c, 0.84);    // left
  face(ox + 2 * d + w, oy + d, w, h, c, 0.78);// back
}

const YELLOW = hex('#ffd43b');
const GRAPHITE = hex('#3a3d46');
const DARK = hex('#24262c');
const ACCENT = hex('#d946ef');
const ACCENT2 = hex('#8b5cf6');
const RED = hex('#e3000b');
const INK = hex('#1b1b1f');

// Head
box(0, 0, 8, 8, 8, YELLOW);
// Face: eyes with highlight, eyebrows, smile
for (const ex of [10, 13]) { rect(ex, 11, 1, 2, INK); }
set(10, 11, hex('#ffffff')); set(13, 11, hex('#ffffff'));
set(10, 11, INK); set(13, 11, INK);
rect(9, 10, 2, 1, shade(YELLOW, 0.72)); rect(13, 10, 2, 1, shade(YELLOW, 0.72));
set(9, 13, INK); rect(10, 14, 4, 1, INK); set(14, 13, INK);
set(9, 9, shade(YELLOW, 1.18)); // specular highlight

// Torso: graphite hoodie with accent stripes and LEGO brick emblem
box(16, 16, 8, 12, 4, GRAPHITE);
for (let y = 20; y < 32; y++) { set(20, y, shade(ACCENT2, 0.9)); set(27, y, shade(ACCENT, 0.9)); }
rect(21, 31, 6, 1, DARK); rect(20, 31, 8, 1, DARK); // hip line front
rect(32, 31, 8, 1, DARK);
// emblem: 4x3 red brick with two studs
rect(22, 23, 4, 3, RED); set(22, 22, shade(RED, 1.15)); set(25, 22, shade(RED, 1.15));
rect(22, 25, 4, 1, shade(RED, 0.75));
// collar
rect(21, 20, 6, 1, shade(GRAPHITE, 1.25));
// back: accent logo bar
rect(34, 23, 4, 1, ACCENT); rect(35, 24, 2, 1, ACCENT2);

// Arms: sleeves + yellow hands (C-shaped look via darker inner pixel)
function arm(ox, oy) {
  box(ox, oy, 4, 12, 4, GRAPHITE);
  for (const fx of [ox, ox + 4, ox + 8, ox + 12]) rect(fx, oy + 4 + 9, 4, 3, YELLOW, 0.95);
  rect(ox + 8, oy, 4, 4, YELLOW, 0.8); // bottom = hand
  for (const fx of [ox, ox + 4, ox + 8, ox + 12]) rect(fx, oy + 4 + 8, 4, 1, shade(GRAPHITE, 0.7));
  set(ox + 5, oy + 15, shade(YELLOW, 0.6));
}
arm(40, 16);
arm(32, 48);

// Legs: dark grey with hip
function leg(ox, oy) {
  box(ox, oy, 4, 12, 4, DARK);
  for (const fx of [ox, ox + 4, ox + 8, ox + 12]) rect(fx, oy + 4, 4, 2, shade(DARK, 1.18));
  for (const fx of [ox, ox + 4, ox + 8, ox + 12]) rect(fx, oy + 15, 4, 1, shade(DARK, 0.75));
}
leg(0, 16);
leg(16, 48);

// PNG encoding (RGBA, filter 0)
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; Buffer.from(px.buffer, y * W * 4, W * 4).copy(raw, y * (W * 4 + 1) + 1); }
const chunk = (type, data) => { const len = Buffer.alloc(4); len.writeUInt32BE(data.length); const td = Buffer.concat([Buffer.from(type), data]); const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td) >>> 0); return Buffer.concat([len, td, crc]); };
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
const png = Buffer.concat([Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]), chunk('IHDR', ihdr), chunk('IDAT', deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
const out = new URL('../src/renderer/assets/minifig.png', import.meta.url);
writeFileSync(out, png);
console.log('wrote', out.pathname, png.length, 'bytes');
