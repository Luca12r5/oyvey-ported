import { test } from 'node:test';
import assert from 'node:assert/strict';
import { DEFAULT_FX, sanitizeFx, sanitizeImage, sanitizePreset } from '../src/core/design.ts';

test('effect parameters are clamped and typed', () => {
  assert.deepEqual(sanitizeFx(null), DEFAULT_FX);
  const fx = sanitizeFx({ particles: 7, speed: -1, light: 'x', fog: 'yes', glow: false });
  assert.equal(fx.particles, 1);
  assert.equal(fx.speed, 0.25);
  assert.equal(fx.light, DEFAULT_FX.light);
  assert.equal(fx.fog, DEFAULT_FX.fog);
  assert.equal(fx.glow, false);
});

test('presets need id, name, theme and background; extras are dropped', () => {
  assert.equal(sanitizePreset({ name: 'x' }), null);
  assert.equal(sanitizePreset({ id: 'Bad Id', name: 'x', themeId: 'halloween', background: 'graveyard' }), null);
  const p = sanitizePreset({ id: 'p-1', name: '  Meins  ', themeId: 'halloween', background: 'graveyard', accentColor: 'red', surface: 'neon', stage: 'nope', evil: '<script>' });
  assert.ok(p);
  assert.equal(p.name, 'Meins');
  assert.equal(p.accentColor, null);
  assert.equal(p.surface, 'neon');
  assert.equal(p.stage, 'portal');
  assert.ok(!('evil' in p));
});

test('background images must be image data URLs', () => {
  assert.equal(sanitizeImage('https://example.com/a.png'), '');
  assert.equal(sanitizeImage('data:text/html;base64,PHNjcmlwdD4='), '');
  assert.equal(sanitizeImage('data:image/png;base64,iVBORw0KGgo='), 'data:image/png;base64,iVBORw0KGgo=');
});
