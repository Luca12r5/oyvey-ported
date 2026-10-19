#!/usr/bin/env node
// Exports the LEGO client's 3D cosmetics (geometry + textures) for the launcher.
// The client's model code runs as is; only the few Minecraft-dependent files are
// left out and replaced by stubs in tools/cosmetics3d/stubs.
//   node tools/cosmetics3d/export.mjs   (needs a JDK: javac + java)
import { execFileSync } from 'node:child_process';
import { cpSync, mkdirSync, mkdtempSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { gzipSync } from 'node:zlib';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const root = new URL('../../', import.meta.url).pathname;
const client = join(root, 'lego-client/src/main');
const out = join(root, 'apps/launcher/assets/cosmetics3d');
const work = mkdtempSync(join(tmpdir(), 'cos3d-'));
const src = join(work, 'src');
const MC = /net\.minecraft|com\.mojang|net\.fabricmc/;

const copy = (rel) => { mkdirSync(join(src, rel, '..'), { recursive: true }); cpSync(join(client, 'java', rel), join(src, rel)); };
for (const f of readdirSync(join(client, 'java/dev/lego/cosmetic'))) {
  const rel = `dev/lego/cosmetic/${f}`;
  if (f.endsWith('.java') && !MC.test(readFileSync(join(client, 'java', rel), 'utf8'))) copy(rel);
}
for (const rel of ['dev/lego/ui/Gx.java', 'dev/lego/ui/Art.java', 'dev/lego/ui/Fonts.java', 'dev/spotifyhud/Json.java']) copy(rel);
cpSync(join(root, 'tools/cosmetics3d/stubs'), src, { recursive: true });

const files = [];
const walk = (d) => { for (const e of readdirSync(d, { withFileTypes: true })) { const p = join(d, e.name); if (e.isDirectory()) walk(p); else if (p.endsWith('.java')) files.push(p); } };
walk(src);
const classes = join(work, 'out');
execFileSync('javac', ['-nowarn', '-encoding', 'UTF-8', '-d', classes, ...files], { stdio: ['ignore', 'ignore', 'inherit'] });
cpSync(join(client, 'resources/assets'), join(classes, 'assets'), { recursive: true });
rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });
execFileSync('java', ['-Djava.awt.headless=true', '-cp', classes, 'dev.lego.cosmetic.Export3D', out], { stdio: 'inherit' });
rmSync(work, { recursive: true, force: true });
// Geometry compresses ~4x; the launcher unpacks it with DecompressionStream.
const items = join(out, 'items');
for (const f of readdirSync(items).filter((n) => n.endsWith('.bin'))) {
  writeFileSync(join(items, `${f}.gz`), gzipSync(readFileSync(join(items, f)), { level: 9 }));
  rmSync(join(items, f));
}
