#!/usr/bin/env node
// Writes the cape symbols, font and modern cape presets as JSON into the client
// resources, so the Java cape renderer draws exactly what the launcher shows.
//   node tools/gen-cape-assets.mjs
import { writeFileSync, mkdirSync } from 'node:fs';
const { capeAssets } = await import('../packages/shared/src/cape.ts');
const dir = new URL('../lego-client/src/main/resources/assets/legoclient/data/', import.meta.url).pathname;
mkdirSync(dir, { recursive: true });
const data = capeAssets();
writeFileSync(`${dir}capes.json`, `${JSON.stringify(data)}\n`);
console.log(`wrote ${Object.keys(data.symbols).length} symbols, ${Object.keys(data.presets).length} presets`);
