// Bundles main, preload and renderer with esbuild and copies static assets.
//   LEGO_MS_CLIENT_ID   Azure app (client) id approved for the Minecraft API
//   LEGO_BACKEND_URL    default LEGO server, e.g. https://api.example.com
//   LEGO_CURSEFORGE_KEY optional CurseForge API key for CurseForge modpack imports
import { build } from 'esbuild';
import { cpSync, mkdirSync, rmSync, copyFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { createRequire } from 'node:module';
import { readFileSync, writeFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import postcss from 'postcss';
import tailwind from 'tailwindcss';

const require = createRequire(import.meta.url);
const out = 'dist';
rmSync(out, { recursive: true, force: true });
mkdirSync(join(out, 'renderer', 'fonts'), { recursive: true });

const define = {
  __MS_CLIENT_ID__: JSON.stringify(process.env.LEGO_MS_CLIENT_ID ?? '134af811-c0d4-4390-91fb-299ddf5626ca'),
  __BACKEND_URL__: JSON.stringify(process.env.LEGO_BACKEND_URL ?? ''),
  __CURSEFORGE_KEY__: JSON.stringify(process.env.LEGO_CURSEFORGE_KEY ?? ''),
};
// Offline icon subset for the React UI (nr/), then the Tailwind stylesheet.
execFileSync(process.execPath, ['tools/gen-icons.mjs'], { stdio: 'inherit' });
const twConfig = {
  content: ['src/renderer/nr/**/*.{ts,tsx}'],
  corePlugins: { preflight: false },
  theme: {
    extend: {
      // From the NoRisk launcher's tailwind.config.js.
      colors: { brand: { DEFAULT: '#125a9d', light: '#1a6ebd', dark: '#0e4678' }, panel: { DEFAULT: '#1e1e1e', light: '#262626', border: '#3a3a3a' }, dark: '#121212' },
      fontFamily: { smallcaps: ['var(--font-smallcaps)'], minecraft: ['var(--font-minecraft)'] },
      backgroundImage: { 'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))' },
      boxShadow: { glow: '0 0 10px rgba(18, 90, 157, 0.4)', 'glow-lg': '0 0 20px rgba(18, 90, 157, 0.6)', 'inner-glow': 'inset 0 0 10px rgba(18, 90, 157, 0.4)' },
      keyframes: { float: { '0%, 100%': { transform: 'translateY(0)' }, '50%': { transform: 'translateY(-10px)' } } },
      animation: { float: 'float 3s ease-in-out infinite' },
    },
  },
};
const nrCss = await postcss([tailwind(twConfig)]).process(readFileSync('src/renderer/nr/nr.css', 'utf8'), { from: 'src/renderer/nr/nr.css' });

const common = { bundle: true, sourcemap: true, logLevel: 'warning', define, legalComments: 'none' };

await build({ ...common, entryPoints: ['src/main/main.ts'], outfile: `${out}/main.cjs`, platform: 'node', target: 'node22', format: 'cjs', external: ['electron'] });
await build({ ...common, entryPoints: ['src/preload/preload.ts'], outfile: `${out}/preload.cjs`, platform: 'node', target: 'node22', format: 'cjs', external: ['electron'] });
await build({ ...common, entryPoints: ['src/renderer/app.ts'], outfile: `${out}/renderer/app.js`, platform: 'browser', target: 'chrome130', format: 'esm', loader: { '.png': 'dataurl' }, jsx: 'automatic', minify: true });

cpSync('src/renderer/index.html', `${out}/renderer/index.html`);
cpSync('src/renderer/styles.css', `${out}/renderer/styles.css`);
// The LEGO client's 3D cosmetics (exported by tools/cosmetics3d/export.mjs).
if (existsSync('assets/cosmetics3d')) cpSync('assets/cosmetics3d', `${out}/renderer/cosmetics3d`, { recursive: true });
writeFileSync(`${out}/renderer/nr.css`, nrCss.css);
const fonts = [
  ['@fontsource/silkscreen/files/silkscreen-latin-400-normal.woff2', 'silkscreen-400.woff2'],
  ['@fontsource/jetbrains-mono/files/jetbrains-mono-latin-400-normal.woff2', 'jetbrains-mono-400.woff2'],
  ['@fontsource/nunito/files/nunito-latin-400-normal.woff2', 'nunito-400.woff2'],
  ['@fontsource/nunito/files/nunito-latin-700-normal.woff2', 'nunito-700.woff2'],
  ...['400', '500', '600', '700', '800'].map((w) => [`@fontsource/inter/files/inter-latin-${w}-normal.woff2`, `inter-${w}.woff2`]),
  ...['400', '600', '700'].map((w) => [`@fontsource/inter/files/inter-latin-ext-${w}-normal.woff2`, `inter-ext-${w}.woff2`]),
  ['@fontsource/creepster/files/creepster-latin-400-normal.woff2', 'creepster-400.woff2'],
  ['@fontsource/nosifer/files/nosifer-latin-400-normal.woff2', 'nosifer-400.woff2'],
  ['@fontsource/orbitron/files/orbitron-latin-500-normal.woff2', 'orbitron-500.woff2'],
  ['@fontsource/orbitron/files/orbitron-latin-800-normal.woff2', 'orbitron-800.woff2'],
  ['@fontsource/press-start-2p/files/press-start-2p-latin-400-normal.woff2', 'press-start-400.woff2'],
  ...['400', '500', '600', '700'].map((w) => [`@fontsource/pixelify-sans/files/pixelify-sans-latin-${w}-normal.woff2`, `pixelify-${w}.woff2`]),
];
for (const [src, name] of fonts) copyFileSync(require.resolve(src), join(out, 'renderer', 'fonts', name));
for (const w of ['Regular', 'Medium', 'Bold']) copyFileSync(`../../lego-client/src/main/resources/assets/legoclient/fonts/Poppins-${w}.ttf`, join(out, 'renderer', 'fonts', `Poppins-${w}.ttf`));
copyFileSync('../../lego-client/src/main/resources/assets/legoclient/fonts/OFL.txt', join(out, 'renderer', 'fonts', 'OFL-Poppins.txt'));
console.log('launcher built into', out, '(Microsoft client id: ' + (process.env.LEGO_MS_CLIENT_ID ? 'env override' : 'built-in') + ')');
