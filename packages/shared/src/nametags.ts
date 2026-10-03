// Name tag styles. Each style is a concrete render description that both the
// launcher (CSS) and the Fabric client (per-character colours + frame quad)
// implement. 100 styles across rarities; the special tier is never sold.

import type { Rarity } from './rarity.ts';

export const TAG_ANIMATIONS = ['none', 'rainbow', 'pulse', 'wave', 'shimmer', 'scroll'] as const;
export type TagAnimation = (typeof TAG_ANIMATIONS)[number];
export const FRAME_STYLES = ['none', 'line', 'double', 'corners', 'glow'] as const;
export type FrameStyle = (typeof FRAME_STYLES)[number];
export type UnlockMethod = 'default' | 'shop' | 'battlepass' | 'achievement' | 'event' | 'staff';

/** Glyphs that exist in Minecraft's default font, so the client can draw them. */
export const TAG_ICONS = ['★', '♦', '♥', '⚡', '☀', '☾', '♛', '⚔', '✿', '❄', '✦', '♫', '☠', '⚑', '✪', '☯', '♠', '♣', '⌘', '✚'] as const;

export interface NameTagStyle {
  id: string;
  name: string;
  rarity: Rarity;
  unlock: UnlockMethod;
  text: { colors: string[]; animation: TagAnimation; bold: boolean; italic: boolean };
  background: { color: string; alpha: number };
  frame: { style: FrameStyle; color: string; width: 0 | 1 | 2 };
  icon: string | null;
}

const BG = '#000000';

function tag(
  id: string,
  name: string,
  rarity: Rarity,
  colors: string[],
  opts: Partial<{ anim: TagAnimation; bold: boolean; italic: boolean; frame: FrameStyle; frameColor: string; width: 0 | 1 | 2; icon: string; bg: string; alpha: number; unlock: UnlockMethod }> = {},
): NameTagStyle {
  const frame = opts.frame ?? 'none';
  return {
    id: `tag_${id}`,
    name,
    rarity,
    unlock: opts.unlock ?? (rarity === 'special' ? 'staff' : 'shop'),
    text: { colors, animation: opts.anim ?? 'none', bold: opts.bold ?? false, italic: opts.italic ?? false },
    background: { color: opts.bg ?? BG, alpha: opts.alpha ?? 0.25 },
    frame: { style: frame, color: opts.frameColor ?? colors[0] ?? '#ffffff', width: frame === 'none' ? 0 : (opts.width ?? 1) },
    icon: opts.icon ?? null,
  };
}

// 20 solid colours (12 common, 8 uncommon)
const SOLID: NameTagStyle[] = [
  tag('default', 'Classic White', 'common', ['#ffffff'], { unlock: 'default' }),
  tag('stone', 'Stone', 'common', ['#a8a29e']),
  tag('sky', 'Sky', 'common', ['#7dd3fc']),
  tag('leaf', 'Leaf', 'common', ['#86efac']),
  tag('sun', 'Sunshine', 'common', ['#fde047']),
  tag('coral', 'Coral', 'common', ['#fb7185']),
  tag('lilac', 'Lilac', 'common', ['#c4b5fd']),
  tag('peach', 'Peach', 'common', ['#fdba74']),
  tag('mint', 'Mint', 'common', ['#6ee7b7']),
  tag('ice', 'Ice', 'common', ['#a5f3fc']),
  tag('rose', 'Rose', 'common', ['#f9a8d4']),
  tag('sand', 'Sand', 'common', ['#e7d3a8']),
  tag('crimson', 'Crimson Bold', 'uncommon', ['#ef4444'], { bold: true }),
  tag('cobalt', 'Cobalt Bold', 'uncommon', ['#3b82f6'], { bold: true }),
  tag('emerald', 'Emerald Bold', 'uncommon', ['#10b981'], { bold: true }),
  tag('gold', 'Gold Bold', 'uncommon', ['#f5b100'], { bold: true }),
  tag('violet', 'Violet Italic', 'uncommon', ['#a855f7'], { italic: true }),
  tag('cyan', 'Cyan Italic', 'uncommon', ['#22d3ee'], { italic: true }),
  tag('shadow', 'Shadow', 'uncommon', ['#d4d4d8'], { bg: '#000000', alpha: 0.6 }),
  tag('paper', 'Paper', 'uncommon', ['#111827'], { bg: '#ffffff', alpha: 0.85 }),
];

// 30 gradients (18 rare, 12 epic)
const GRADIENT: NameTagStyle[] = [
  tag('sunset', 'Sunset', 'rare', ['#f97316', '#ec4899']),
  tag('ocean', 'Ocean', 'rare', ['#06b6d4', '#3b82f6']),
  tag('forest', 'Forest', 'rare', ['#22c55e', '#065f46']),
  tag('candy', 'Candy', 'rare', ['#f472b6', '#a78bfa']),
  tag('lava', 'Lava', 'rare', ['#fde047', '#dc2626']),
  tag('frost', 'Frost', 'rare', ['#ffffff', '#38bdf8']),
  tag('dusk', 'Dusk', 'rare', ['#6366f1', '#ec4899']),
  tag('lime', 'Lime Soda', 'rare', ['#bef264', '#22d3ee']),
  tag('peachy', 'Peachy', 'rare', ['#fecaca', '#fb923c']),
  tag('steel', 'Steel', 'rare', ['#e5e7eb', '#64748b']),
  tag('berry', 'Berry', 'rare', ['#be185d', '#7c3aed']),
  tag('tropic', 'Tropic', 'rare', ['#facc15', '#10b981']),
  tag('royal', 'Royal', 'rare', ['#1d4ed8', '#f5b100']),
  tag('mars', 'Mars', 'rare', ['#f97316', '#7f1d1d']),
  tag('aqua', 'Aqua', 'rare', ['#5eead4', '#0ea5e9']),
  tag('blossom', 'Blossom', 'rare', ['#fbcfe8', '#f43f5e']),
  tag('mono', 'Monochrome', 'rare', ['#ffffff', '#52525b']),
  tag('copper', 'Copper', 'rare', ['#fdba74', '#9a3412']),
  tag('aurora', 'Aurora', 'epic', ['#22d3ee', '#a3e635', '#e879f9'], { bold: true }),
  tag('galaxy', 'Galaxy', 'epic', ['#312e81', '#a855f7', '#f0abfc'], { bold: true }),
  tag('fire', 'Firestorm', 'epic', ['#fde047', '#f97316', '#dc2626'], { bold: true }),
  tag('tidal', 'Tidal', 'epic', ['#e0f2fe', '#0ea5e9', '#1e3a8a'], { bold: true }),
  tag('jungle', 'Jungle', 'epic', ['#facc15', '#22c55e', '#14532d'], { bold: true }),
  tag('vapor', 'Vapor', 'epic', ['#ff71ce', '#b967ff', '#01cdfe'], { bold: true }),
  tag('ember', 'Ember', 'epic', ['#fef08a', '#fb923c', '#7c2d12'], { italic: true }),
  tag('nebula', 'Nebula', 'epic', ['#c026d3', '#6366f1', '#22d3ee'], { italic: true }),
  tag('pastel', 'Pastel', 'epic', ['#fbcfe8', '#c7d2fe', '#bbf7d0']),
  tag('neonline', 'Neon Line', 'epic', ['#00f0ff', '#ff2bd6'], { bold: true, bg: '#0a0612', alpha: 0.6 }),
  tag('lego', 'Brick', 'epic', ['#c91a09', '#f2cd37', '#0055bf'], { bold: true }),
  tag('toxic', 'Toxic', 'epic', ['#d9f99d', '#4ade80', '#166534'], { bold: true }),
];

// 20 framed styles (epic)
const FRAMED: NameTagStyle[] = [
  tag('framed_gold', 'Gold Frame', 'epic', ['#fde68a'], { frame: 'line', frameColor: '#f5b100', icon: '♛' }),
  tag('framed_silver', 'Silver Frame', 'epic', ['#f4f4f5'], { frame: 'line', frameColor: '#a1a1aa', icon: '✦' }),
  tag('framed_ruby', 'Ruby Frame', 'epic', ['#fecdd3'], { frame: 'double', frameColor: '#e11d48', icon: '♦' }),
  tag('framed_sapphire', 'Sapphire Frame', 'epic', ['#dbeafe'], { frame: 'double', frameColor: '#2563eb', icon: '♦' }),
  tag('framed_emerald', 'Emerald Frame', 'epic', ['#d1fae5'], { frame: 'double', frameColor: '#059669', icon: '♦' }),
  tag('framed_amethyst', 'Amethyst Frame', 'epic', ['#f3e8ff'], { frame: 'double', frameColor: '#9333ea', icon: '♦' }),
  tag('framed_blade', 'Blade', 'epic', ['#e5e7eb'], { frame: 'corners', frameColor: '#ef4444', icon: '⚔', bold: true }),
  tag('framed_bolt', 'Thunder', 'epic', ['#fef08a'], { frame: 'corners', frameColor: '#facc15', icon: '⚡', bold: true }),
  tag('framed_snow', 'Snowflake', 'epic', ['#e0f2fe'], { frame: 'corners', frameColor: '#7dd3fc', icon: '❄' }),
  tag('framed_flower', 'Flower', 'epic', ['#fce7f3'], { frame: 'corners', frameColor: '#f472b6', icon: '✿' }),
  tag('framed_moon', 'Moonlight', 'epic', ['#e0e7ff'], { frame: 'line', frameColor: '#818cf8', icon: '☾', bg: '#0b1026', alpha: 0.6 }),
  tag('framed_sun', 'Daybreak', 'epic', ['#fff7ed'], { frame: 'line', frameColor: '#fb923c', icon: '☀' }),
  tag('framed_heart', 'Heartfelt', 'epic', ['#ffe4e6'], { frame: 'line', frameColor: '#f43f5e', icon: '♥' }),
  tag('framed_music', 'Rhythm', 'epic', ['#ecfeff'], { frame: 'corners', frameColor: '#06b6d4', icon: '♫' }),
  tag('framed_flag', 'Captain', 'epic', ['#f8fafc'], { frame: 'double', frameColor: '#334155', icon: '⚑', bold: true }),
  tag('framed_star', 'Star', 'epic', ['#fefce8'], { frame: 'line', frameColor: '#eab308', icon: '★' }),
  tag('framed_spade', 'Ace', 'epic', ['#fafafa'], { frame: 'double', frameColor: '#18181b', icon: '♠', bg: '#ffffff', alpha: 0.2 }),
  tag('framed_clover', 'Lucky', 'epic', ['#dcfce7'], { frame: 'corners', frameColor: '#16a34a', icon: '♣' }),
  tag('framed_yin', 'Balance', 'epic', ['#ffffff', '#a1a1aa'], { frame: 'line', frameColor: '#ffffff', icon: '☯' }),
  tag('framed_medic', 'Medic', 'epic', ['#fef2f2'], { frame: 'line', frameColor: '#dc2626', icon: '✚' }),
];

// 15 animated (legendary)
const ANIMATED: NameTagStyle[] = [
  tag('rainbow', 'Rainbow', 'legendary', ['#ff0000', '#ffa500', '#ffff00', '#00ff00', '#00bfff', '#8a2be2'], { anim: 'rainbow', bold: true }),
  tag('rainbow_soft', 'Soft Rainbow', 'legendary', ['#fecaca', '#fde68a', '#bbf7d0', '#bfdbfe', '#e9d5ff'], { anim: 'rainbow' }),
  tag('pulse_red', 'Heartbeat', 'legendary', ['#ef4444', '#7f1d1d'], { anim: 'pulse', bold: true, icon: '♥' }),
  tag('pulse_blue', 'Sonar', 'legendary', ['#38bdf8', '#1e3a8a'], { anim: 'pulse', bold: true }),
  tag('pulse_gold', 'Treasure', 'legendary', ['#fde047', '#a16207'], { anim: 'pulse', bold: true, icon: '★' }),
  tag('wave_ocean', 'Wave', 'legendary', ['#e0f2fe', '#0ea5e9', '#1e40af'], { anim: 'wave' }),
  tag('wave_fire', 'Heat Wave', 'legendary', ['#fef08a', '#f97316', '#b91c1c'], { anim: 'wave', bold: true }),
  tag('wave_toxic', 'Ooze', 'legendary', ['#ecfccb', '#84cc16', '#365314'], { anim: 'wave' }),
  tag('shimmer_gold', 'Gold Shimmer', 'legendary', ['#a16207', '#fde68a'], { anim: 'shimmer', bold: true }),
  tag('shimmer_silver', 'Silver Shimmer', 'legendary', ['#52525b', '#ffffff'], { anim: 'shimmer', bold: true }),
  tag('shimmer_diamond', 'Diamond Shimmer', 'legendary', ['#0e7490', '#cffafe'], { anim: 'shimmer', bold: true, icon: '♦' }),
  tag('scroll_neon', 'Neon Scroll', 'legendary', ['#00f0ff', '#ff2bd6', '#fcee0a'], { anim: 'scroll', bold: true }),
  tag('scroll_aurora', 'Aurora Scroll', 'legendary', ['#22d3ee', '#a3e635', '#e879f9', '#6366f1'], { anim: 'scroll' }),
  tag('scroll_lego', 'Brick Scroll', 'legendary', ['#c91a09', '#f2cd37', '#0055bf', '#237841'], { anim: 'scroll', bold: true }),
  tag('pulse_void', 'Void', 'legendary', ['#a855f7', '#09090b'], { anim: 'pulse', bold: true, bg: '#000000', alpha: 0.7 }),
];

// 10 mythic: animated + glowing frame + icon
const MYTHIC: NameTagStyle[] = [
  tag('m_inferno', 'Inferno Crown', 'mythic', ['#fef08a', '#f97316', '#dc2626'], { anim: 'wave', bold: true, frame: 'glow', frameColor: '#f97316', icon: '♛' }),
  tag('m_frozen', 'Frozen Throne', 'mythic', ['#ffffff', '#7dd3fc', '#1d4ed8'], { anim: 'shimmer', bold: true, frame: 'glow', frameColor: '#7dd3fc', icon: '❄' }),
  tag('m_storm', 'Stormcaller', 'mythic', ['#fef9c3', '#facc15', '#6366f1'], { anim: 'pulse', bold: true, frame: 'glow', frameColor: '#facc15', icon: '⚡' }),
  tag('m_reaper', 'Reaper', 'mythic', ['#e4e4e7', '#7f1d1d'], { anim: 'pulse', bold: true, frame: 'glow', frameColor: '#991b1b', icon: '☠', bg: '#000000', alpha: 0.7 }),
  tag('m_cosmos', 'Cosmos', 'mythic', ['#312e81', '#c026d3', '#22d3ee', '#fef08a'], { anim: 'scroll', bold: true, frame: 'glow', frameColor: '#a855f7', icon: '✪' }),
  tag('m_prism', 'Prism', 'mythic', ['#ff0000', '#ffff00', '#00ff00', '#00ffff', '#0000ff', '#ff00ff'], { anim: 'rainbow', bold: true, frame: 'glow', frameColor: '#ffffff', icon: '✦' }),
  tag('m_bloom', 'Eternal Bloom', 'mythic', ['#fce7f3', '#f472b6', '#86efac'], { anim: 'wave', bold: true, frame: 'glow', frameColor: '#f472b6', icon: '✿' }),
  tag('m_champion', 'Champion', 'mythic', ['#fde68a', '#f5b100', '#a16207'], { anim: 'shimmer', bold: true, frame: 'glow', frameColor: '#f5b100', icon: '⚔' }),
  tag('m_nightsky', 'Night Sky', 'mythic', ['#0f172a', '#6366f1', '#e0e7ff'], { anim: 'scroll', bold: true, frame: 'glow', frameColor: '#818cf8', icon: '☾' }),
  tag('m_brickmaster', 'Brick Master', 'mythic', ['#c91a09', '#f2cd37', '#0055bf'], { anim: 'rainbow', bold: true, frame: 'glow', frameColor: '#f2cd37', icon: '⌘' }),
];

// 5 special (never sold; granted by role or event)
const SPECIAL: NameTagStyle[] = [
  tag('staff', 'Staff', 'special', ['#22d3ee', '#3b82f6'], { bold: true, frame: 'double', frameColor: '#22d3ee', icon: '✪', unlock: 'staff' }),
  tag('developer', 'Developer', 'special', ['#a3e635', '#22c55e'], { bold: true, frame: 'double', frameColor: '#84cc16', icon: '⌘', unlock: 'staff' }),
  tag('founder', 'Founder', 'special', ['#fde68a', '#f5b100'], { bold: true, anim: 'shimmer', frame: 'glow', frameColor: '#f5b100', icon: '♛', unlock: 'event' }),
  tag('beta', 'Beta Tester', 'special', ['#f0abfc', '#a855f7'], { bold: true, frame: 'line', frameColor: '#c084fc', icon: '⚑', unlock: 'event' }),
  tag('creator', 'Creator', 'special', ['#fda4af', '#e11d48'], { bold: true, frame: 'line', frameColor: '#fb7185', icon: '★', unlock: 'event' }),
];

export const NAME_TAGS: readonly NameTagStyle[] = Object.freeze([...SOLID, ...GRADIENT, ...FRAMED, ...ANIMATED, ...MYTHIC, ...SPECIAL]);
export const DEFAULT_NAME_TAG = 'tag_default';

export function getNameTag(id: string): NameTagStyle | undefined {
  return NAME_TAGS.find((t) => t.id === id);
}

/**
 * Per-character colour for a tag at time `t` (seconds). Same maths as the
 * client's renderer so launcher previews match the game.
 */
export function tagCharColor(style: NameTagStyle, index: number, length: number, t: number): string {
  const cols = style.text.colors;
  if (cols.length === 0) return '#ffffff';
  if (cols.length === 1) return cols[0]!;
  const sample = (pos: number) => {
    const p = ((pos % 1) + 1) % 1;
    const seg = p * (cols.length - 1);
    const i = Math.min(cols.length - 2, Math.floor(seg));
    return lerpHex(cols[i]!, cols[i + 1]!, seg - i);
  };
  const base = length <= 1 ? 0 : index / (length - 1);
  switch (style.text.animation) {
    case 'rainbow':
    case 'scroll':
      return sample(base * 0.999 + t * (style.text.animation === 'rainbow' ? 0.5 : 0.25));
    case 'wave':
      return sample(0.5 + 0.5 * Math.sin(base * Math.PI * 2 + t * 3));
    case 'pulse':
      return lerpHex(cols[0]!, cols[cols.length - 1]!, 0.5 + 0.5 * Math.sin(t * 4));
    case 'shimmer': {
      const head = (t * 0.8) % 1.6 - 0.3;
      const d = Math.abs(base - head);
      return lerpHex(cols[0]!, cols[cols.length - 1]!, Math.max(0, 1 - d * 5));
    }
    default:
      return sample(base * 0.999);
  }
}

function lerpHex(a: string, b: string, t: number): string {
  const pa = parseInt(a.slice(1), 16);
  const pb = parseInt(b.slice(1), 16);
  const ch = (s: number) => Math.round(((pa >> s) & 255) + ((((pb >> s) & 255) - ((pa >> s) & 255)) * t));
  return `#${((ch(16) << 16) | (ch(8) << 8) | ch(0)).toString(16).padStart(6, '0')}`;
}

// ---- Custom tag text moderation -------------------------------------------

const RESERVED = ['admin', 'administrator', 'mod', 'moderator', 'staff', 'dev', 'developer', 'owner', 'lego', 'official', 'support', 'mojang', 'microsoft', 'system', 'server'];
const BLOCKED = ['nigger', 'nigga', 'faggot', 'hitler', 'nazi', 'heil', 'kys', 'retard', 'hurensohn', 'fotze', 'missgeburt', 'spast', 'schwuchtel'];

/** Folds common look-alike characters so "4dm1n" is treated like "admin". */
function fold(s: string): string {
  return s
    .toLowerCase()
    .replace(/[0@]/g, 'o').replace(/[1!|]/g, 'i').replace(/3/g, 'e').replace(/4/g, 'a').replace(/5|\$/g, 's').replace(/7/g, 't')
    .replace(/[^a-z]/g, '');
}

export type CustomTagCheck = { ok: true; text: string } | { ok: false; reason: string };

/**
 * Checks user-chosen tag text. `otherNames` are Minecraft names that must not
 * be impersonated (e.g. every known player name); staff bypass is handled by
 * the caller via the `special` styles, never through free text.
 */
export function checkCustomTagText(raw: string, ownName: string, isTakenName: (name: string) => boolean): CustomTagCheck {
  const text = raw.normalize('NFKC').trim();
  if (text.length < 2 || text.length > 16) return { ok: false, reason: 'Tag must be 2-16 characters.' };
  if (!/^[A-Za-z0-9 _\-.!?★♦♥⚡☀☾♛⚔✿❄✦♫✪]+$/.test(text)) return { ok: false, reason: 'Only letters, digits, spaces, _-.!? and the tag icons are allowed.' };
  if (/\s{2,}/.test(text)) return { ok: false, reason: 'No repeated spaces.' };
  const f = fold(text);
  if (BLOCKED.some((b) => f.includes(b))) return { ok: false, reason: 'This tag contains blocked words.' };
  if (RESERVED.some((r) => f === r || f.startsWith(r) || f.endsWith(r))) return { ok: false, reason: 'Tags may not imitate staff or official accounts.' };
  const plain = text.replace(/[^A-Za-z0-9_]/g, '');
  if (plain.length >= 3 && plain.toLowerCase() !== ownName.toLowerCase() && isTakenName(plain)) {
    return { ok: false, reason: 'Tags may not use another player\'s name.' };
  }
  return { ok: true, text };
}
