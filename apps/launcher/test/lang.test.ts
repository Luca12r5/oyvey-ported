import { test } from 'node:test';
import assert from 'node:assert/strict';
import { LANGS, PACK_KEY_COUNT, packFor, packSizes } from '../src/renderer/lang.ts';

test('every built-in pack has one text per key', () => {
  for (const [code, n] of Object.entries(packSizes())) assert.equal(n, PACK_KEY_COUNT, `${code} has ${n} texts, expected ${PACK_KEY_COUNT}`);
});

test('all listed languages resolve', () => {
  assert.equal(LANGS.length, 16);
  for (const [code] of LANGS) {
    if (code === 'de' || code === 'en') assert.equal(packFor(code), null);
    else assert.ok(packFor(code)?.['nav.settings'], code);
  }
});
