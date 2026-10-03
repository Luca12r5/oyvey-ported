// Copies the launcher icon set (apps/launcher/src/renderer/icons.ts) to the
// website as plain JavaScript so both use the same custom icons.
//   node tools/sync-icons.mjs [--check]
import { readFileSync, writeFileSync } from 'node:fs';
import { transformSync } from 'esbuild';

const src = new URL('../../launcher/src/renderer/icons.ts', import.meta.url);
const out = new URL('../public/assets/icons.js', import.meta.url);
const js = `// Generated from apps/launcher/src/renderer/icons.ts by apps/website/tools/sync-icons.mjs – do not edit.\n${transformSync(readFileSync(src, 'utf8'), { loader: 'ts', format: 'esm', target: 'es2022' }).code}`;
if (process.argv.includes('--check')) {
  if (readFileSync(out, 'utf8') !== js) { console.error('public/assets/icons.js is out of date: run node tools/sync-icons.mjs'); process.exit(1); }
  console.log('icons in sync');
} else {
  writeFileSync(out, js);
  console.log('wrote', out.pathname);
}
