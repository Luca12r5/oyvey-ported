// Capes gallery (NoRisk-style): tabs All / My capes / Favorites / Vanilla,
// search, template download, big cape fronts in a grid, favourite hearts.
// LEGO capes come from the catalog (equip/buy through the LEGO server); Vanilla
// shows the account's official Minecraft cape.

import { CATALOG, RARITY_INFO } from '@lego/shared';
import { go, onLeave, state } from '../app.ts';
import { renderCapes } from '../nr/mount.tsx';
import { errorToast, lego, toast } from '../ui.ts';
import { capeFront } from '../itemart.ts';

type Tab = 'all' | 'mine' | 'fav' | 'vanilla';
let tab: Tab = 'all';
const FAV_KEY = 'lego.capeFavs';

function favs(): Set<string> {
  try { return new Set(JSON.parse(localStorage.getItem(FAV_KEY) ?? '[]') as string[]); } catch { return new Set(); }
}
function saveFavs(s: Set<string>): void {
  try { localStorage.setItem(FAV_KEY, JSON.stringify([...s])); } catch { /* storage blocked */ }
}

/** Crops the front (10×16 at 1,1) out of a 64×32 / 64×64 cape texture. */
function vanillaFront(url: string): Promise<string> {
  return new Promise((res) => {
    const img = new Image();
    img.onload = () => {
      const s = img.width / 64;
      const c = document.createElement('canvas');
      c.width = 100; c.height = 160;
      const g = c.getContext('2d')!;
      g.imageSmoothingEnabled = false;
      g.drawImage(img, 1 * s, 1 * s, 10 * s, 16 * s, 0, 0, 100, 160);
      res(c.toDataURL());
    };
    img.onerror = () => res('');
    img.src = url;
  });
}

function template(): void {
  const c = document.createElement('canvas');
  c.width = 64; c.height = 32;
  const g = c.getContext('2d')!;
  const part = (x: number, y: number, w: number, h: number, col: string) => { g.fillStyle = col; g.fillRect(x, y, w, h); };
  part(0, 1, 1, 16, '#5b6b8a'); part(1, 0, 10, 1, '#7d8db0'); part(11, 0, 10, 1, '#4a5874');
  part(1, 1, 10, 16, '#3b82f6'); part(11, 1, 1, 16, '#5b6b8a'); part(12, 1, 10, 16, '#1e3a6e');
  const a = document.createElement('a');
  a.href = c.toDataURL('image/png');
  a.download = 'cape-template-64x32.png';
  a.click();
  toast('Vorlage gespeichert (64×32). Vorderseite = blaue Fläche oben links.', 'ok');
}

export async function capesPage(el: HTMLElement): Promise<void> {
  const capes = CATALOG.filter((i) => i.slot === 'cape');
  let inv: { owned: string[]; equipped: Record<string, string> } = { owned: [], equipped: {} };
  if (state.auth.lego.connected) inv = await lego<typeof inv>('GET', '/api/inventory').catch(() => inv);
  const vanillaUrl = state.skin.cape ? await vanillaFront(state.skin.cape) : '';
  let unmount: (() => void) | null = null;
  const draw = () => {
    const f = favs();
    const eq = inv.equipped.cape;
    const list = capes.filter((c) => tab === 'all' || (tab === 'mine' ? inv.owned.includes(c.id) : f.has(c.id)));
    unmount = renderCapes(el, {
      tab,
      capes: list.map((c) => {
        const r = RARITY_INFO[c.rarity];
        const owned = inv.owned.includes(c.id);
        return { id: c.id, name: c.name, art: capeFront(c.id, c.name).__raw, owned, equipped: eq === c.id, fav: f.has(c.id), sub: eq === c.id ? 'Angelegt' : owned ? r.label.de : c.price ? `${c.price} Coins` : r.label.de, subColor: r.color };
      }),
      noneEquipped: !eq,
      vanilla: { url: vanillaUrl || null, signedIn: state.auth.signedIn },
      emptyText: tab === 'fav' ? 'Noch keine Favoriten – tippe auf das Herz.' : tab === 'mine' ? (state.auth.lego.connected ? 'Du besitzt noch keine LEGO-Capes.' : 'Verbinde dich mit dem LEGO-Server, um deine Capes zu sehen.') : 'Keine Capes gefunden.',
      onTab: (t) => { tab = t; draw(); },
      onTemplate: template,
      onFav: (id) => { const s = favs(); if (s.has(id)) s.delete(id); else s.add(id); saveFavs(s); draw(); },
      onClick: (id) => void equip(id),
    });
  };
  const equip = async (id: string | null) => {
    if (!state.auth.lego.connected) return toast('Zum Anlegen von Capes mit dem LEGO-Server verbinden.');
    try {
      if (id === null) {
        inv.equipped = (await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: 'cape', itemId: null })).equipped;
        toast('Cape abgelegt', 'ok');
        return draw();
      }
      if (!inv.owned.includes(id)) { toast('Dieses Cape gibt es im Shop.'); return void go('shop'); }
      inv.equipped = (await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: 'cape', itemId: inv.equipped.cape === id ? null : id })).equipped;
      toast(inv.equipped.cape === id ? 'Cape angelegt – sichtbar im LEGO Client' : 'Cape abgelegt', 'ok');
      draw();
    } catch (err) { errorToast(err); }
  };
  draw();
  onLeave(() => unmount?.());
}
