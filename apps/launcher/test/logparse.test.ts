import { test } from 'node:test';
import assert from 'node:assert/strict';
import { isProblem, parseLine } from '../src/renderer/logparse.ts';

const fixed = () => '12:00:00';

test('log4j lines keep time, thread and level', () => {
  const e = parseLine('[18:04:11] [Render thread/WARN]: Missing sound for event', 'out', fixed);
  assert.deepEqual(e, { time: '18:04:11', level: 'WARN', source: 'Render thread', text: 'Missing sound for event' });
  assert.equal(parseLine('[18:04:12] [main/ERROR]: Crash', 'out').level, 'ERROR');
  assert.equal(parseLine('[18:04:12] [main/SEVERE]: old style', 'out').level, 'ERROR');
});

test('launcher and installer lines are tagged', () => {
  assert.deepEqual(parseLine('[installer] Lade Fabric 0.18.4', 'out', fixed), { time: '12:00:00', level: 'INFO', source: 'installer', text: 'Lade Fabric 0.18.4' });
  assert.equal(parseLine('[launcher] Start fehlgeschlagen', 'out', fixed).level, 'ERROR');
});

test('stack traces and stderr get sensible levels', () => {
  assert.ok(isProblem(parseLine('java.lang.NullPointerException: boom', 'err', fixed)));
  assert.ok(isProblem(parseLine('\tat net.minecraft.client.Main.main(Main.java:10)', 'err', fixed)));
  assert.equal(parseLine('some noise', 'err', fixed).level, 'WARN');
  assert.equal(parseLine('hello', 'out', fixed).level, 'INFO');
});
