#!/usr/bin/env node
// Regenerates packages/shared/src/client-cosmetics.ts from the client's
// cosmetic registration calls so backend/launcher ids match the game.
import { readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const dir = new URL('../lego-client/src/main/java/dev/lego/cosmetic/', import.meta.url).pathname;
const out = new URL('../packages/shared/src/client-cosmetics.ts', import.meta.url).pathname;
const items = [];
for (const f of readdirSync(dir).filter((n) => n.endsWith('.java')).sort()) {
  const s = readFileSync(join(dir, f), 'utf8');
  for (const m of s.matchAll(/(?:Cos\.)?\badd\("([a-z0-9_]+)", "([^"]+)", Cos\.Slot\.([A-Z]+), Cos\.Rarity\.([A-Z]+)/g)) {
    items.push([m[1], m[2], m[3].toLowerCase(), m[4].toLowerCase()]);
  }
  for (const m of s.matchAll(/(?<![A-Za-z])(Cos\.)?cape\("([a-z0-9_]+)", "([^"]+)", Cos\.Rarity\.([A-Z]+)/g)) {
    const prefix = f === 'XmasCos.java' && !m[1] ? 'xm_cape_' : 'cape_';
    items.push([prefix + m[2], m[3], 'cape', m[4].toLowerCase()]);
  }
}
const ids = new Set(items.map((i) => i[0]));
if (ids.size !== items.length) throw new Error('duplicate cosmetic ids');
const esc = (v) => v.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
const head = readFileSync(out, 'utf8').split('export const CLIENT_COSMETICS')[0];
writeFileSync(out, `${head}export const CLIENT_COSMETICS: readonly ClientCosmeticSeed[] = [\n${items
  .map(([a, b, c, d]) => `  ['${a}', '${esc(b)}', '${c}', '${d}'],`).join('\n')}\n];\n`);
console.log(`wrote ${items.length} cosmetics`);
