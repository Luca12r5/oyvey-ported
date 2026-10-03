#!/usr/bin/env node
// Writes catalog data the Fabric client needs (name tag styles) as JSON into
// lego-client resources, so game and launcher render identical styles.
//   node tools/export-client-data.mjs
import { writeFileSync, mkdirSync } from 'node:fs';
const { NAME_TAGS } = await import('../packages/shared/src/nametags.ts');
const dir = new URL('../lego-client/src/main/resources/assets/legoclient/data/', import.meta.url).pathname;
mkdirSync(dir, { recursive: true });
const slim = NAME_TAGS.map((t) => ({ id: t.id, colors: t.text.colors, animation: t.text.animation, bold: t.text.bold, italic: t.text.italic, icon: t.icon, iconColor: t.frame.color }));
writeFileSync(`${dir}nametags.json`, `${JSON.stringify(slim)}\n`);
console.log(`wrote ${slim.length} name tag styles`);
