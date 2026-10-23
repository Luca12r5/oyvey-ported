import { test } from 'node:test';
import assert from 'node:assert/strict';
import { sanitizeServer, SERVER_DEFAULT, serverArgs, serverProperties, validCommand, type LocalServer } from '../src/core/server.ts';

const base: LocalServer = { ...SERVER_DEFAULT, id: 's1', createdAt: 1 };

test('server settings are clamped and cleaned', () => {
  const s = sanitizeServer({ name: '§cHack\u0000', port: 80, memoryMb: 999999, loader: 'forge', gamemode: 'creative', motd: 'x'.repeat(200) }, base);
  assert.equal(s.name, 'cHack');
  assert.equal(s.port, 1025);
  assert.equal(s.memoryMb, 32768);
  assert.equal(s.loader, 'vanilla');
  assert.equal(s.gamemode, 'creative');
  assert.equal(s.motd.length, 59);
  assert.equal(sanitizeServer({ eulaAccepted: 'yes' }, base).eulaAccepted, false);
});

test('server.properties keeps other values, forces online mode', () => {
  const p = serverProperties('#Minecraft server properties\nonline-mode=false\nview-distance=12\nserver-port=1\n', { ...base, port: 25570 });
  assert.match(p, /^#Minecraft server properties$/m);
  assert.match(p, /^online-mode=true$/m);
  assert.match(p, /^view-distance=12$/m);
  assert.match(p, /^server-port=25570$/m);
  assert.match(p, /^gamemode=survival$/m);
  assert.deepEqual(serverArgs({ ...base, memoryMb: 4096 }, 'server.jar').slice(-3), ['-jar', 'server.jar', 'nogui']);
  assert.equal(validCommand('say hi'), true);
  assert.equal(validCommand('op x\nstop'), false);
});
