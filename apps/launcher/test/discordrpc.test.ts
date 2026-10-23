import { test } from 'node:test';
import assert from 'node:assert/strict';
import { activityPayload, decodeFrames, encodeFrame, ipcPaths, OP, validAppId } from '../src/core/discordrpc.ts';

test('frames round-trip and split across chunks', () => {
  const a = encodeFrame(OP.HANDSHAKE, { v: 1, client_id: '123456789012345678' });
  const b = encodeFrame(OP.FRAME, { cmd: 'DISPATCH', evt: 'READY' });
  const all = Buffer.concat([a, b]);
  const part = decodeFrames(all.subarray(0, a.length + 5));
  assert.equal(part.frames.length, 1);
  assert.deepEqual(part.frames[0], { op: 0, data: { v: 1, client_id: '123456789012345678' } });
  const rest = decodeFrames(Buffer.concat([part.rest, all.subarray(a.length + 5)]));
  assert.deepEqual(rest.frames[0], { op: 1, data: { cmd: 'DISPATCH', evt: 'READY' } });
  assert.equal(rest.rest.length, 0);
});

test('app id, socket paths and activity', () => {
  assert.equal(validAppId('1234567890123456789'), true);
  assert.equal(validAppId('abc'), false);
  assert.equal(ipcPaths('win32', {})[0], '\\\\?\\pipe\\discord-ipc-0');
  assert.equal(ipcPaths('linux', { XDG_RUNTIME_DIR: '/run/user/1000' })[0], '/run/user/1000/discord-ipc-0');
  const p = activityPayload(42, { details: 'Spielt Minecraft 26.3', state: 'Profil LEGO', startedAt: 1_700_000_000_000 }, 'n') as { args: { activity: { timestamps: { start: number }; assets: { large_image: string } } } };
  assert.equal(p.args.activity.timestamps.start, 1_700_000_000);
  assert.equal(p.args.activity.assets.large_image, 'lego');
});
