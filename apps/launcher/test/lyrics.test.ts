import { test } from 'node:test';
import assert from 'node:assert/strict';
import { cleanTitle, fetchLyrics, firstArtist, indexAt, parseLrc, pick, wordAt } from '../src/core/lyrics.ts';

test('parses line tags, offset and repeated stamps', () => {
  const l = parseLrc('[offset:+500]\n[00:01.00]Hello world\n[00:05.5][00:09.25]Again\nno tag');
  assert.deepEqual(l.map((x) => [x.timeMs, x.text]), [[500, 'Hello world'], [5000, 'Again'], [8750, 'Again']]);
  assert.equal(l[0]!.words.length, 2);
  assert.ok(l[0]!.wordTimes[1]! > l[0]!.wordTimes[0]!);
});

test('enhanced word tags are exact', () => {
  const l = parseLrc('[00:10.00]<00:10.00>Ich <00:10.40>bin <00:11.00>da');
  assert.deepEqual(l[0]!.words, ['Ich', 'bin', 'da']);
  assert.deepEqual(l[0]!.wordTimes, [10000, 10400, 11000]);
  assert.equal(l[0]!.text, 'Ich bin da');
  assert.equal(wordAt(l[0]!, 10500), 1);
});

test('indexAt and helpers', () => {
  const l = parseLrc('[00:01.00]a\n[00:03.00]b\n[00:06.00]c');
  assert.equal(indexAt(l, 0), -1);
  assert.equal(indexAt(l, 3500), 1);
  assert.equal(indexAt(l, 99_000), 2);
  assert.equal(firstArtist('A feat. B'), 'A');
  assert.equal(firstArtist('A, B & C'), 'A');
  assert.equal(cleanTitle('Song (feat. X) - 2011 Remaster'), 'Song');
});

test('pick prefers synced and close duration', () => {
  const r = pick([{ plainLyrics: 'x', duration: 200 }, { syncedLyrics: '[00:01]y', duration: 203 }, { syncedLyrics: '[00:01]z', duration: 260 }], 200_000);
  assert.equal(r?.syncedLyrics, '[00:01]y');
});

test('fetch: get endpoint hit, then not found', async () => {
  const ok = (body: unknown) => new Response(JSON.stringify(body), { status: 200 });
  const f1 = (async () => ok({ syncedLyrics: '[00:02.00]Hi' })) as typeof fetch;
  const r = await fetchLyrics(f1, 'T', 'A', 100_000);
  assert.equal(r.status, 'synced');
  assert.equal(r.lines[0]!.text, 'Hi');
  const f2 = (async (u: string) => (String(u).includes('/get?') ? new Response('', { status: 404 }) : ok([]))) as typeof fetch;
  assert.equal((await fetchLyrics(f2, 'T', 'A', 0)).status, 'not_found');
});
