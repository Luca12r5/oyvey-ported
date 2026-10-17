import { test } from 'node:test';
import assert from 'node:assert/strict';
import { allowed, cleanInput, enforceSafeSearch, toTarget } from '../src/core/vrweb.ts';
import { listModules, setModule } from '../src/core/clientcfg.ts';

test('address bar: URLs, domains and searches', () => {
  assert.equal(toTarget('https://minecraft.wiki/w/Creeper'), 'https://minecraft.wiki/w/Creeper');
  assert.equal(toTarget('http://example.com'), 'https://example.com');
  assert.equal(toTarget('minecraft.net'), 'https://minecraft.net');
  assert.equal(toTarget('lego halloween'), 'https://www.google.com/search?q=lego%20halloween&safe=active&hl=de');
  assert.match(toTarget(''), /^https:\/\/www\.google\.com\/\?safe=active/);
});

test('only https may load; SafeSearch is forced on Google and Bing', () => {
  assert.ok(allowed('https://www.google.com'));
  assert.ok(!allowed('http://example.com'));
  assert.ok(!allowed('file:///etc/passwd'));
  assert.ok(!allowed('javascript:alert(1)'));
  assert.equal(enforceSafeSearch('https://www.google.com/search?q=x&safe=off'), 'https://www.google.com/search?q=x&safe=active');
  assert.equal(enforceSafeSearch('https://www.google.de/search?q=x&safe=active'), null);
  assert.equal(enforceSafeSearch('https://www.bing.com/search?q=x'), 'https://www.bing.com/search?q=x&adlt=strict');
  assert.equal(enforceSafeSearch('https://minecraft.net/'), null);
});

test('browser input events are validated and clamped', () => {
  assert.deepEqual(cleanInput({ type: 'mouseDown', x: 5000, y: -3 }, 1280, 800), { type: 'mouseDown', x: 1279, y: 0, button: 'left' });
  assert.deepEqual(cleanInput({ type: 'char', keyCode: 'a' }, 1280, 800), { type: 'char', keyCode: 'a' });
  assert.equal(cleanInput({ type: 'navigate', url: 'x' }, 1280, 800), null);
  assert.equal(cleanInput({ type: 'keyDown', keyCode: '' }, 1280, 800), null);
});

test('client module config: list and toggle only existing modules', () => {
  const cfg = JSON.stringify({ theme: 'x', modules: { zoom: { on: true, key: 67, settings: {} }, fps: { on: false }, 'bad id!': { on: true } } });
  assert.deepEqual(listModules(cfg), [{ id: 'fps', on: false }, { id: 'zoom', on: true }]);
  const out = setModule(cfg, 'fps', true)!;
  assert.equal(JSON.parse(out).modules.fps.on, true);
  assert.equal(JSON.parse(out).modules.zoom.key, 67);
  assert.equal(setModule(cfg, 'nope', true), null);
  assert.deepEqual(listModules('not json'), []);
});
