import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  CATALOG, CLIENT_COSMETICS, NAME_TAGS, THEMES, checkCustomTagText, contrast, readabilityWarnings, rederive,
  tagCharColor, validateTheme, dailyRewardFor, SEASONS, getItem, RARITIES, RARITY_INFO,
} from '../src/index.ts';

test('101 distinct, valid themes', () => {
  assert.ok(THEMES.length >= 100);
  const ids = new Set(THEMES.map((t) => t.id));
  assert.equal(ids.size, THEMES.length, 'unique ids');
  // No two themes share the same palette + style fingerprint.
  const prints = new Set(THEMES.map((t) => JSON.stringify([t.colors.bg, t.colors.surface, t.colors.accent, t.colors.accent2, t.radius, t.font, t.background.kind])));
  assert.equal(prints.size, THEMES.length, 'every theme is a different configuration');
  for (const t of THEMES) {
    const v = validateTheme(t);
    assert.ok(v.ok, `${t.id}: ${!v.ok ? v.errors.join('; ') : ''}`);
  }
  assert.ok(new Set(THEMES.map((t) => t.family)).size >= 16, 'covers all requested styles');
});

test('built-in themes keep readable text', () => {
  const bad = THEMES.filter((t) => contrast(t.colors.text, t.colors.surface) < 4.5).map((t) => t.id);
  assert.deepEqual(bad, []);
  for (const t of THEMES) assert.deepEqual(readabilityWarnings(t).filter((w) => w.startsWith('Text')), [], t.id);
});

test('theme validation rejects unsafe or malformed imports', () => {
  const base = structuredClone(THEMES[0]!) as any;
  assert.equal(validateTheme({ ...base, colors: { ...base.colors, bg: 'red' } }).ok, false);
  assert.equal(validateTheme({ ...base, radius: 99 }).ok, false);
  assert.equal(validateTheme({ ...base, id: '../evil' }).ok, false);
  assert.equal(validateTheme({ ...base, background: { kind: 'image', intensity: 1, image: 'https://tracker.example/bg.png' } }).ok, false, 'no remote images');
  assert.equal(validateTheme({ ...base, background: { kind: 'image', intensity: 1, image: 'data:image/png;base64,iVBORw0KGgo=' } }).ok, true);
  const edited = rederive({ ...base, colors: { ...base.colors, surface: '#ffffff', text: '#000000' } });
  assert.notEqual(edited.colors.border, base.colors.border);
});

test('100 name tag styles with distinct looks', () => {
  assert.equal(NAME_TAGS.length, 100);
  assert.equal(new Set(NAME_TAGS.map((t) => t.id)).size, 100);
  const looks = new Set(NAME_TAGS.map((t) => JSON.stringify([t.text, t.frame, t.icon, t.background])));
  assert.equal(looks.size, 100);
  for (const r of RARITIES) assert.ok(NAME_TAGS.some((t) => t.rarity === r), `rarity ${r} present`);
  assert.ok(NAME_TAGS.filter((t) => t.rarity === 'special').every((t) => t.unlock !== 'shop'), 'special tags are never sold');
});

test('tag colour animation is deterministic and valid', () => {
  const rainbow = NAME_TAGS.find((t) => t.text.animation === 'rainbow')!;
  const c1 = tagCharColor(rainbow, 0, 6, 0);
  assert.match(c1, /^#[0-9a-f]{6}$/);
  assert.equal(tagCharColor(rainbow, 0, 6, 0), c1);
  assert.notEqual(tagCharColor(rainbow, 0, 6, 0.7), c1, 'animates over time');
});

test('custom tag moderation', () => {
  const taken = (n: string) => n.toLowerCase() === 'notch';
  assert.equal(checkCustomTagText('Builder', 'me', taken).ok, true);
  assert.equal(checkCustomTagText('x', 'me', taken).ok, false);
  assert.equal(checkCustomTagText('M0derator', 'me', taken).ok, false);
  assert.equal(checkCustomTagText('Official LEGO', 'me', taken).ok, false);
  assert.equal(checkCustomTagText('Notch', 'me', taken).ok, false);
  assert.equal(checkCustomTagText('<script>', 'me', taken).ok, false);
});

test('catalog: ≥300 real items, ids match the client, prices follow rarity', () => {
  assert.equal(CLIENT_COSMETICS.length, 211);
  assert.ok(CATALOG.length >= 300, `catalog has ${CATALOG.length}`);
  assert.equal(new Set(CATALOG.map((i) => i.id)).size, CATALOG.length);
  for (const i of CATALOG) {
    if (i.unlock === 'shop') assert.equal(i.price, RARITY_INFO[i.rarity].basePrice, i.id);
    else assert.equal(i.price, null, i.id);
  }
});

test('progression data references real items', () => {
  for (const s of SEASONS) {
    for (const t of s.tiers) for (const r of [t.free, t.premium]) if (r?.item) assert.ok(getItem(r.item), `${s.id} tier ${t.tier}: ${r.item}`);
  }
  assert.equal(dailyRewardFor(1), 50);
  assert.equal(dailyRewardFor(7), 200);
  assert.equal(dailyRewardFor(8), 50);
});
