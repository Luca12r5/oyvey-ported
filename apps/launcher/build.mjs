// Bundles main, preload and renderer with esbuild and copies static assets.
//   LEGO_MS_CLIENT_ID   Azure app (client) id approved for the Minecraft API
//   LEGO_BACKEND_URL    default LEGO server, e.g. https://api.example.com
import { build } from 'esbuild';
import { cpSync, mkdirSync, rmSync, copyFileSync } from 'node:fs';
import { join } from 'node:path';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const out = 'dist';
rmSync(out, { recursive: true, force: true });
mkdirSync(join(out, 'renderer', 'fonts'), { recursive: true });

const define = {
  __MS_CLIENT_ID__: JSON.stringify(process.env.LEGO_MS_CLIENT_ID ?? ''),
  __BACKEND_URL__: JSON.stringify(process.env.LEGO_BACKEND_URL ?? ''),
};
const common = { bundle: true, sourcemap: true, logLevel: 'warning', define, legalComments: 'none' };

await build({ ...common, entryPoints: ['src/main/main.ts'], outfile: `${out}/main.cjs`, platform: 'node', target: 'node22', format: 'cjs', external: ['electron'] });
await build({ ...common, entryPoints: ['src/preload/preload.ts'], outfile: `${out}/preload.cjs`, platform: 'node', target: 'node22', format: 'cjs', external: ['electron'] });
await build({ ...common, entryPoints: ['src/renderer/app.ts'], outfile: `${out}/renderer/app.js`, platform: 'browser', target: 'chrome130', format: 'esm' });

cpSync('src/renderer/index.html', `${out}/renderer/index.html`);
cpSync('src/renderer/styles.css', `${out}/renderer/styles.css`);
const fonts = [
  ['@fontsource/silkscreen/files/silkscreen-latin-400-normal.woff2', 'silkscreen-400.woff2'],
  ['@fontsource/jetbrains-mono/files/jetbrains-mono-latin-400-normal.woff2', 'jetbrains-mono-400.woff2'],
  ['@fontsource/nunito/files/nunito-latin-400-normal.woff2', 'nunito-400.woff2'],
  ['@fontsource/nunito/files/nunito-latin-700-normal.woff2', 'nunito-700.woff2'],
];
for (const [src, name] of fonts) copyFileSync(require.resolve(src), join(out, 'renderer', 'fonts', name));
for (const w of ['Regular', 'Medium', 'Bold']) copyFileSync(`../../lego-client/src/main/resources/assets/legoclient/fonts/Poppins-${w}.ttf`, join(out, 'renderer', 'fonts', `Poppins-${w}.ttf`));
copyFileSync('../../lego-client/src/main/resources/assets/legoclient/fonts/OFL.txt', join(out, 'renderer', 'fonts', 'OFL-Poppins.txt'));
console.log('launcher built into', out, process.env.LEGO_MS_CLIENT_ID ? '(Microsoft client id set)' : '(no Microsoft client id: sign-in disabled)');
