// Small procedural preview art for catalog items (shop / collection cards), so
// every item gets its own picture instead of the same slot icon. Colours come
// from keywords in the name (Feuer, Frost, Herz …) or a hash of the id; the shape
// from the slot and a few name keywords (Krone, Brille, Auto …).

import type { Raw } from './ui.ts';

type Pal = [string, string, string];

const KEYWORDS: [RegExp, Pal][] = [
  [/feuer|flamm|lava|inferno|phönix|phoenix|hölle/i, ['#ff5a1f', '#ffb020', '#7a1606']],
  [/frost|eis|schnee|winter|kristall|ice/i, ['#8fe3ff', '#e9fbff', '#2a6f9e']],
  [/herz|liebe|rose|valentin|pink/i, ['#ff4f8b', '#ffc2d6', '#8a1240']],
  [/gewitter|blitz|donner|sturm/i, ['#ffe14a', '#a78bfa', '#2b1a5a']],
  [/stern|galax|kosmos|nebel|orbit|mond/i, ['#a78bfa', '#f0e6ff', '#1b1240']],
  [/regenbogen|rainbow|prisma/i, ['#ff4d4d', '#ffd34d', '#4d9dff']],
  [/leere|void|schatten|nacht|dunkel/i, ['#6d28d9', '#c4b5fd', '#0b0614']],
  [/gold|krone|königlich|royal|legend/i, ['#ffc53d', '#fff1b8', '#8a5a00']],
  [/natur|wald|blatt|kirsch|blüte|grün/i, ['#4ade80', '#fda4af', '#14532d']],
  [/musik|note|beat/i, ['#38bdf8', '#f0abfc', '#0c4a6e']],
  [/kürbis|halloween|geist|grusel|zombie|blut/i, ['#ff7a1a', '#7c3aed', '#1a0d05']],
  [/weihnacht|xmas|santa|geschenk/i, ['#e11d48', '#22c55e', '#7f1d1d']],
  [/wasser|ozean|meer|welle|blase|seife/i, ['#22d3ee', '#cffafe', '#0e4f6a']],
  [/polizei/i, ['#1d4ed8', '#f8fafc', '#0b1a4a']],
  [/taxi/i, ['#facc15', '#111827', '#854d0e']],
];

function hash(s: string): number {
  let h = 2166136261;
  for (const c of s) { h ^= c.charCodeAt(0); h = Math.imul(h, 16777619); }
  return h >>> 0;
}

function palette(id: string, name: string): Pal {
  for (const [re, p] of KEYWORDS) if (re.test(name) || re.test(id)) return p;
  const h = hash(id) % 360;
  return [`hsl(${h} 80% 58%)`, `hsl(${(h + 40) % 360} 90% 78%)`, `hsl(${h} 60% 22%)`];
}

function shape(slot: string, name: string, p: Pal, seed: number): string {
  const [a, b, d] = p;
  const n = name.toLowerCase();
  switch (slot) {
    case 'cape': {
      const pat = seed % 3;
      const deco = pat === 0 ? `<path d="M40 30 L80 30 L76 64 L44 64Z" fill="${b}" opacity=".55"/>`
        : pat === 1 ? `<path d="M36 40 L84 28 M34 58 L86 46 M33 76 L87 64" stroke="${b}" stroke-width="5" opacity=".6"/>`
        : `<circle cx="60" cy="56" r="13" fill="${b}"/><circle cx="60" cy="56" r="6" fill="${a}"/>`;
      return `<path d="M38 18 L82 18 L92 98 L28 98Z" fill="${a}" stroke="${d}" stroke-width="3"/>${deco}<rect x="36" y="14" width="48" height="7" fill="${d}"/>`;
    }
    case 'wings': {
      const wing = (s: number) => `<g transform="translate(60 54) scale(${s} 1)"><path d="M4 0 C24 -30 52 -34 56 -14 C50 -8 46 2 52 10 C40 10 30 16 30 26 C20 20 10 14 4 0Z" fill="${a}" stroke="${d}" stroke-width="2.5"/><path d="M14 -2 C26 -16 40 -20 48 -14 M12 6 C24 4 34 6 42 10" stroke="${b}" stroke-width="2.5" fill="none" opacity=".8"/></g>`;
      return wing(1) + wing(-1);
    }
    case 'hat':
      if (/krone|crown|könig/.test(n)) return `<path d="M28 76 L32 36 L46 56 L60 30 L74 56 L88 36 L92 76Z" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="28" y="72" width="64" height="12" fill="${d}"/><circle cx="60" cy="62" r="5" fill="${b}"/>`;
      if (/heiligen|halo/.test(n)) return `<ellipse cx="60" cy="54" rx="34" ry="11" fill="none" stroke="${a}" stroke-width="7"/><ellipse cx="60" cy="54" rx="34" ry="11" fill="none" stroke="${b}" stroke-width="2"/>`;
      if (/horn|teufel/.test(n)) return `<path d="M34 80 C24 56 30 34 44 26 C40 44 44 60 52 74Z M86 80 C96 56 90 34 76 26 C80 44 76 60 68 74Z" fill="${a}" stroke="${d}" stroke-width="3"/>`;
      if (/kopfhörer|headphone|headset/.test(n)) return `<path d="M30 70 C30 34 90 34 90 70" stroke="${d}" stroke-width="7" fill="none"/><path d="M30 70 C30 34 90 34 90 70" stroke="${a}" stroke-width="3" fill="none"/><rect x="22" y="62" width="18" height="26" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="80" y="62" width="18" height="26" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="26" y="68" width="10" height="14" fill="${b}"/><rect x="84" y="68" width="10" height="14" fill="${b}"/>`;
      if (/mütze|beanie|zipfel/.test(n)) return `<path d="M30 80 C30 44 46 28 60 28 C74 28 90 44 90 80Z" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="26" y="74" width="68" height="12" fill="${b}"/><circle cx="60" cy="24" r="8" fill="${b}"/>`;
      return `<rect x="40" y="26" width="40" height="46" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="40" y="58" width="40" height="8" fill="${b}"/><rect x="24" y="70" width="72" height="10" fill="${d}"/>`;
    case 'face':
      if (/brille|glasses|sonnen/.test(n)) return `<rect x="24" y="44" width="30" height="22" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="66" y="44" width="30" height="22" fill="${a}" stroke="${d}" stroke-width="3"/><path d="M54 52 L66 52" stroke="${d}" stroke-width="4"/><path d="M28 48 L40 48" stroke="${b}" stroke-width="3"/>`;
      return `<path d="M26 40 C26 30 94 30 94 40 L90 66 C80 82 40 82 30 66Z" fill="${a}" stroke="${d}" stroke-width="3"/><path d="M38 50 L52 50 M68 50 L82 50" stroke="${d}" stroke-width="6"/><path d="M50 66 Q60 72 70 66" stroke="${b}" stroke-width="3" fill="none"/>`;
    case 'back':
      if (/schwert|sword|klinge|katana|säbel/.test(n)) return `<path d="M58 14 L62 14 L66 78 L54 78Z" fill="${b}" stroke="${d}" stroke-width="2.5"/><rect x="42" y="76" width="36" height="7" fill="${a}"/><rect x="56" y="83" width="8" height="18" fill="${d}"/>`;
      return `<rect x="34" y="28" width="52" height="62" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="40" y="56" width="40" height="24" fill="${b}" opacity=".7"/><path d="M44 28 C44 16 76 16 76 28" stroke="${d}" stroke-width="4" fill="none"/>`;
    case 'aura': {
      let dots = '';
      for (let i = 0; i < 10; i++) {
        const ang = ((seed % 97) / 97 + i / 10) * Math.PI * 2, r = 30 + ((seed >> i) & 7);
        dots += `<circle cx="${(60 + Math.cos(ang) * r).toFixed(1)}" cy="${(56 + Math.sin(ang) * r * 0.55).toFixed(1)}" r="${2 + ((seed >> (i + 3)) & 2)}" fill="${i % 2 ? a : b}"/>`;
      }
      return `<ellipse cx="60" cy="56" rx="36" ry="18" fill="none" stroke="${a}" stroke-width="4" opacity=".9"/><ellipse cx="60" cy="56" rx="24" ry="11" fill="none" stroke="${b}" stroke-width="2.5" opacity=".8"/>${dots}`;
    }
    case 'pet':
      return `<rect x="34" y="36" width="52" height="44" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="34" y="26" width="14" height="14" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="72" y="26" width="14" height="14" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="44" y="50" width="8" height="8" fill="${d}"/><rect x="68" y="50" width="8" height="8" fill="${d}"/><rect x="54" y="62" width="12" height="7" fill="${b}"/>`;
    case 'vehicle':
      if (/board|brett|skate|surf|teppich|besen|ufo|wolke/.test(n)) return `<path d="M18 66 C18 58 102 58 102 66 C102 74 18 74 18 66Z" fill="${a}" stroke="${d}" stroke-width="3"/><circle cx="36" cy="80" r="6" fill="${d}"/><circle cx="84" cy="80" r="6" fill="${d}"/><path d="M30 64 L90 64" stroke="${b}" stroke-width="3"/>`;
      if (/rad|bike|bmx|moto|roller|scooter|segway/.test(n)) return `<circle cx="34" cy="74" r="14" fill="none" stroke="${d}" stroke-width="5"/><circle cx="86" cy="74" r="14" fill="none" stroke="${d}" stroke-width="5"/><path d="M34 74 L54 46 L76 46 L86 74 M54 46 L60 74 L34 74" stroke="${a}" stroke-width="5" fill="none"/><path d="M72 36 L82 36" stroke="${b}" stroke-width="5"/>`;
      return `<path d="M14 72 L18 56 L40 52 L52 38 L82 38 L96 54 L106 58 L106 72Z" fill="${a}" stroke="${d}" stroke-width="3"/><path d="M56 42 L80 42 L90 54 L48 54Z" fill="${b}" opacity=".75"/><circle cx="36" cy="74" r="10" fill="${d}"/><circle cx="86" cy="74" r="10" fill="${d}"/><circle cx="36" cy="74" r="4" fill="${b}"/><circle cx="86" cy="74" r="4" fill="${b}"/>`;
    case 'knife':
      return `<path d="M20 70 C40 40 80 30 104 30 C92 46 70 62 44 72Z" fill="${a}" stroke="${d}" stroke-width="2.5"/><path d="M26 68 C46 46 78 36 98 33" stroke="${b}" stroke-width="2.5" fill="none"/><rect x="8" y="68" width="30" height="12" fill="${d}" transform="rotate(-18 23 74)"/>`;
    default:
      return `<rect x="30" y="30" width="60" height="50" fill="${a}" stroke="${d}" stroke-width="3"/><rect x="38" y="22" width="12" height="10" fill="${a}"/><rect x="70" y="22" width="12" height="10" fill="${a}"/>`;
  }
}

/** Inline SVG preview for one catalog item. */
export function itemArt(id: string, name: string, slot: string): Raw {
  const p = palette(id, name);
  const seed = hash(id);
  const gid = `g${seed.toString(36)}`;
  return { __raw: `<svg class="item-art" viewBox="0 0 120 110" aria-hidden="true"><defs><radialGradient id="${gid}" cx="50%" cy="55%" r="60%"><stop offset="0" stop-color="${p[0]}" stop-opacity=".28"/><stop offset="1" stop-color="${p[0]}" stop-opacity="0"/></radialGradient></defs><rect width="120" height="110" fill="url(#${gid})"/><g shape-rendering="geometricPrecision">${shape(slot, name, p, seed)}</g></svg>` };
}

/** Pixel-art front of a cape (10 × 16 texels, like the real cape texture) for the Capes gallery. */
export function capeFront(id: string, name: string): Raw {
  const [a, b, d] = palette(id, name);
  const seed = hash(id);
  const kind = seed % 6;
  const cells: string[] = [];
  const rnd = (i: number) => ((seed >>> (i % 24)) ^ (i * 2654435761)) >>> 0;
  for (let y = 0; y < 16; y++) {
    for (let x = 0; x < 10; x++) {
      let c = a;
      const cx = x - 4.5, cy = y - 7.5;
      switch (kind) {
        case 0: c = y < 5 ? b : y > 11 ? d : a; break; // three bands
        case 1: c = (x + y) % 4 < 2 ? a : b; break; // diagonal stripes
        case 2: c = cx * cx + cy * cy < 9 ? b : cx * cx + cy * cy < 16 ? d : a; break; // emblem
        case 3: c = x === 0 || x === 9 || y === 0 || y === 15 ? d : (y + (x % 2)) % 5 === 0 ? b : a; break; // framed
        case 4: c = Math.abs(cx) < 1.2 || Math.abs(cy + 2) < 1.2 ? b : a; break; // cross
        default: c = rnd(y * 10 + x) % 7 === 0 ? b : y > 10 && rnd(x) % 3 === 0 ? d : a; break; // sparkle
      }
      // Soft vertical shading like cloth.
      const shade = y / 15 * 0.28;
      cells.push(`<rect x="${x}" y="${y}" width="1.02" height="1.02" fill="${c}"/>${shade > 0.02 ? `<rect x="${x}" y="${y}" width="1.02" height="1.02" fill="#000" opacity="${shade.toFixed(2)}"/>` : ''}`);
    }
  }
  return { __raw: `<svg class="cape-art" viewBox="0 0 10 16" shape-rendering="crispEdges" aria-hidden="true">${cells.join('')}</svg>` };
}
