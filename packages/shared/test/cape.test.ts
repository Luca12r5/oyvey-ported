import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import {
  CAPE_FRAMES, CAPE_PRESETS, CAPE_SYMBOL_IDS, CATALOG, DEFAULT_CAPE, capeAssets, capeFromPrompt, capeSpecId, cleanCapeText, renderCapeFront, renderCapeSheet, sanitizeCapeSpec,
} from '../src/index.ts';

test('cape specs are sanitised (no unknown symbols, clamped numbers, short clean text)', () => {
  const s = sanitizeCapeSpec({ bg: { type: 'evil', colors: ['red', '#ABCDEF'] }, symbols: [{ id: 'nope' }, { id: 'star', x: 9, size: -1, rot: 400, color: 'x', glow: 'yes' }], border: { width: 99 }, text: { value: 'hallo welt was geht' }, shine: 1 });
  assert.equal(s.bg.type, 'solid');
  assert.deepEqual(s.bg.colors, ['#abcdef', '#6d28d9']);
  assert.equal(s.symbols.length, 1);
  assert.deepEqual(s.symbols[0], { id: 'star', x: 1, y: 0.5, size: 0.1, rot: 0, color: '#ffffff', glow: false });
  assert.equal(s.border.width, 8);
  assert.equal(s.text?.value, 'HALLO WELT');
  assert.equal(s.shine, false);
  assert.equal(cleanCapeText('f u c k'), '', 'blocked words are removed even with spaces');
  assert.equal(sanitizeCapeSpec({ symbols: Array.from({ length: 20 }, () => ({ id: 'star' })) }).symbols.length, 6);
});

test('renderer is deterministic and draws the full 64x32 layout at x8', () => {
  const front = renderCapeFront(DEFAULT_CAPE);
  assert.equal(front.length, 80 * 128 * 4);
  const a = createHash('sha1').update(front).digest('hex');
  assert.equal(createHash('sha1').update(renderCapeFront(structuredClone(DEFAULT_CAPE))).digest('hex'), a);
  const sheet = renderCapeSheet(DEFAULT_CAPE);
  assert.equal(sheet.length, 512 * 256 * 4);
  for (let i = 3; i < sheet.length; i += 4) assert.equal(sheet[i], 255, 'opaque everywhere');
  // the symbol is drawn: the centre pixel of the front differs from the background corner
  const px = (x: number, y: number) => [...front.slice((y * 80 + x) * 4, (y * 80 + x) * 4 + 3)].join();
  assert.notEqual(px(40, 54), px(10, 54));
});

test('every symbol renders something and presets are valid catalog capes', () => {
  for (const id of CAPE_SYMBOL_IDS) {
    const f = renderCapeFront({ ...DEFAULT_CAPE, bg: { type: 'solid', colors: ['#000000', '#000000'], angle: 0 }, symbols: [{ id, x: 0.5, y: 0.5, size: 0.8, rot: 0, color: '#ffffff', glow: false }], border: { width: 0, color: '#000000' }, text: null, shine: false });
    let lit = 0;
    for (let i = 0; i < f.length; i += 4) if (f[i]! > 128) lit++;
    assert.ok(lit > 200, `${id} draws pixels (${lit})`);
  }
  for (const id of Object.keys(CAPE_PRESETS)) {
    assert.ok(CATALOG.some((c) => c.id === `cape_${id}` && c.slot === 'cape'), id);
    assert.deepEqual(sanitizeCapeSpec(CAPE_PRESETS[id]!.spec), CAPE_PRESETS[id]!.spec, `${id} is already sanitised`);
  }
  assert.ok(CATALOG.some((c) => c.id === 'cape_custom' && c.unlock === 'default'));
});

test('offline generator understands colours, symbols, styles and quoted text', () => {
  const s = capeFromPrompt('lila Galaxie mit goldener Krone und "KING"');
  assert.equal(s.bg.type, 'galaxy');
  assert.ok(s.symbols.some((x) => x.id === 'crown'));
  assert.equal(s.text?.value, 'KING');
  assert.ok(s.bg.colors.includes('#8b5cf6'));
  assert.equal(s.symbols.find((x) => x.id === 'crown')?.color, '#fbbf24', 'gold belongs to the crown');
  assert.ok(!s.bg.colors.includes('#fbbf24'));
  assert.equal(s.bg.colors[0], '#0b0b12', 'galaxy starts dark');
  assert.deepEqual(capeFromPrompt('blauer blitz'), capeFromPrompt('blauer blitz'), 'deterministic');
  assert.notDeepEqual(capeFromPrompt('blauer blitz', 1), capeFromPrompt('blauer blitz', 2), 'seed changes the result');
  assert.equal(capeSpecId(s), capeSpecId(structuredClone(s)));
  assert.equal(capeFromPrompt('rotes Feuer').symbols[0]?.color, '#e11d48', 'German endings, colour goes to the symbol');
  assert.ok(capeFromPrompt('blauer Hintergrund mit Stern').bg.colors.includes('#2563eb'));
});

test('client capes.json matches the shared definitions', () => {
  const file = new URL('../../../lego-client/src/main/resources/assets/legoclient/data/capes.json', import.meta.url);
  assert.deepEqual(JSON.parse(readFileSync(file, 'utf8')), capeAssets(), 'run: node tools/gen-cape-assets.mjs');
});

test('animated capes change from frame to frame and loop; still capes do not', () => {
  const anim = { ...CAPE_PRESETS.m_neon_bolt!.spec, anim: true };
  const h = (f: number, s = anim) => createHash('sha1').update(renderCapeFront(s, f)).digest('hex');
  assert.notEqual(h(0), h(4), 'frames differ');
  assert.equal(h(3), h(3 + CAPE_FRAMES), 'the loop repeats');
  const still = { ...anim, anim: false };
  assert.equal(h(0, still), h(7, still), 'a still cape ignores the frame');
  assert.ok(Object.values(CAPE_PRESETS).every((p) => p.spec.anim), 'catalog capes are animated');
  assert.equal(sanitizeCapeSpec({ anim: 'yes' }).anim, undefined, 'only true counts');
});
