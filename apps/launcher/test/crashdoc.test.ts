import { test } from 'node:test';
import assert from 'node:assert/strict';
import { analyzeLog, type ModInfo } from '../src/core/crashdoc.ts';

const mods: ModInfo[] = [
  { filename: 'sodium-fabric-0.6.0+mc1.21.jar', id: 'sodium', title: 'Sodium', enabled: true },
  { filename: 'coolmod-1.2.jar', id: 'coolmod', title: 'Cool Mod', enabled: true },
  { filename: 'iris-1.8.jar', id: 'iris', title: 'Iris', enabled: true },
  { filename: 'iris-1.7.jar', id: 'iris', title: 'Iris', enabled: true },
  { filename: 'lego-client.jar', id: 'legoclient', title: 'LEGO Client', enabled: true },
];
const ctx = { mods, memoryMb: 4096 };

test('missing fabric api is installed', () => {
  const log = `Incompatible mods found!\n - Mod 'Cool Mod' (coolmod) 1.2 requires any version of fabric-api, which is missing!`;
  const d = analyzeLog(log, ctx);
  assert.equal(d[0]!.fix?.kind, 'install-mod');
  assert.deepEqual(d[0]!.fix, { kind: 'install-mod', slug: 'fabric-api' });
});

test('wrong minecraft version disables the mod', () => {
  const log = ` - Mod 'Sodium' (sodium) 0.5.0 requires version 1.20.1 of minecraft, but only the wrong version is present: 1.21.11!`;
  const d = analyzeLog(log, ctx);
  assert.deepEqual(d[0]!.fix, { kind: 'disable-mod', filename: 'sodium-fabric-0.6.0+mc1.21.jar' });
});

test('mixin failure names the mod', () => {
  const log = `org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError: An unexpected critical error was encountered\nCaused by: org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException: Critical injection failure: @Inject annotation on render could not find any targets matching 'x' in net.minecraft.class_757. Using refmap coolmod-refmap.json [PREINJECT Applicator Phase -> coolmod.mixins.json:GameRendererMixin from mod coolmod]`;
  const d = analyzeLog(log, ctx);
  assert.equal(d[0]!.fix?.kind, 'disable-mod');
  assert.equal((d[0]!.fix as { filename: string }).filename, 'coolmod-1.2.jar');
});

test('out of memory raises RAM', () => {
  const d = analyzeLog('Exception in thread "Render thread" java.lang.OutOfMemoryError: Java heap space', ctx);
  assert.deepEqual(d[0]!.fix, { kind: 'set-memory', mb: 6144 });
});

test('heap too large lowers RAM', () => {
  const d = analyzeLog('Error occurred during initialization of VM\nCould not reserve enough space for object heap', { mods, memoryMb: 8192 });
  assert.deepEqual(d[0]!.fix, { kind: 'set-memory', mb: 5120 });
});

test('duplicate mod disables the older copy', () => {
  const d = analyzeLog('Mod ID `iris` is provided by multiple mods', ctx);
  assert.deepEqual(d[0]!.fix, { kind: 'disable-mod', filename: 'iris-1.7.jar' });
});

test('java version has an explanation but no fake fix', () => {
  const d = analyzeLog('java.lang.UnsupportedClassVersionError: x has been compiled by a more recent version of the Java Runtime (class file version 65.0)', ctx);
  assert.equal(d[0]!.id, 'java');
  assert.match(d[0]!.detail, /Java 21/);
  assert.equal(d[0]!.fix, null);
});

test('gpu driver crash', () => {
  const d = analyzeLog('GLFW error 65542: WGL: The driver does not appear to support OpenGL', ctx);
  assert.equal(d[0]!.id, 'gpu');
});

test('stack fallback never blames the LEGO client', () => {
  assert.equal(analyzeLog('at dev.lego.Foo.bar(Foo.java:1) ~[lego-client.jar:?]', ctx).length, 0);
  const d = analyzeLog('java.lang.NullPointerException\n\tat com.cool.Thing.tick(Thing.java:3) ~[coolmod-1.2.jar:?]', ctx);
  assert.equal((d[0]!.fix as { filename: string }).filename, 'coolmod-1.2.jar');
});

test('clean log gives nothing', () => {
  assert.equal(analyzeLog('[12:00:00] [Render thread/INFO]: Stopping!', ctx).length, 0);
});

test('crash excerpt for the AI removes personal data and stays small', async () => {
  const { crashExcerpt } = await import('../src/core/crashdoc.ts');
  const log = 'Loading C:\\Users\\Max Mustermann\\AppData\\x.jar and /home/maxi/.minecraft/mods\n--accessToken eyJabc.def --uuid 1234abcd\nmail max@example.com from 85.12.3.4 and 127.0.0.1\n' + 'x'.repeat(100_000);
  const e = crashExcerpt('---- Minecraft Crash Report ----\nDescription: boom', log);
  assert.ok(e.length < 60_000);
  assert.ok(e.startsWith('---- Minecraft Crash Report'));
  const head = crashExcerpt('', log.slice(0, 300));
  assert.ok(!head.includes('Mustermann') && !head.includes('maxi') && !head.includes('eyJabc') && !head.includes('1234abcd'), head);
  assert.ok(!head.includes('max@example.com') && !head.includes('85.12.3.4') && head.includes('127.0.0.1'), head);
});

test('Vivecraft without a running VR runtime is explained', () => {
  const d = analyzeLog('[Render thread/ERROR]: org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR: VR Initialization failed: VRInitError_Init_HmdNotFound', { mods: [], memoryMb: 4096 });
  assert.equal(d[0]?.id, 'vr-runtime');
});
