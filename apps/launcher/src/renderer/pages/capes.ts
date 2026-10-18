// Capes gallery (NoRisk-style): tabs All / My capes / Favorites / Vanilla,
// search, template download, big cape fronts in a grid, favourite hearts.
// LEGO capes come from the catalog (equip/buy through the LEGO server); Vanilla
// shows the account's official Minecraft cape.

import { CATALOG, CUSTOM_CAPE_ID, RARITY_INFO, capeFromPrompt, type CapeSpec } from '@lego/shared';
import { go, onLeave, state } from '../app.ts';
import { openCapeEditor, renderCapes } from '../nr/mount.tsx';
import { capeFrontUrl, loadLocalCape, presetSpec, saveLocalCape } from '../capeart.ts';
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
  // Own cape and the modern capes first, then the classic ones.
  const rank = (id: string) => (id === CUSTOM_CAPE_ID ? 0 : id.startsWith('cape_m_') ? 1 : 2);
  const capes = CATALOG.filter((i) => i.slot === 'cape').sort((a, b) => rank(a.id) - rank(b.id));
  let inv: { owned: string[]; equipped: Record<string, string> } = { owned: [], equipped: {} };
  if (state.auth.lego.connected) inv = await lego<typeof inv>('GET', '/api/inventory').catch(() => inv);
  const vanillaUrl = state.skin.cape ? await vanillaFront(state.skin.cape) : '';
  // The own designed cape: from the LEGO server when connected, else the local copy.
  let own: CapeSpec | null = loadLocalCape();
  let aiOnServer = false;
  if (state.auth.lego.connected) {
    const r = await lego<{ cape: { spec: CapeSpec } | null; ai: boolean }>('GET', '/api/me/cape-spec').catch(() => null);
    if (r?.cape) own = r.cape.spec;
    aiOnServer = !!r?.ai;
  }
  const art = (id: string, name: string) => {
    const spec = id === CUSTOM_CAPE_ID ? own : presetSpec(id);
    return spec ? capeFrontUrl(spec) : capeFront(id, name).__raw;
  };
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
        return { id: c.id, name: c.name, art: art(c.id, c.name), owned, equipped: eq === c.id, fav: f.has(c.id), sub: eq === c.id ? 'Angelegt' : owned ? r.label.de : c.price ? `${c.price} Coins` : r.label.de, subColor: r.color };
      }),
      noneEquipped: !eq,
      vanilla: { url: vanillaUrl || null, signedIn: state.auth.signedIn },
      emptyText: tab === 'fav' ? 'Noch keine Favoriten – tippe auf das Herz.' : tab === 'mine' ? (state.auth.lego.connected ? 'Du besitzt noch keine LEGO-Capes.' : 'Verbinde dich mit dem LEGO-Server, um deine Capes zu sehen.') : 'Keine Capes gefunden.',
      onTab: (t) => { tab = t; draw(); },
      onTemplate: template,
      onEditor: openEditor,
      onFav: (id) => { const s = favs(); if (s.has(id)) s.delete(id); else s.add(id); saveFavs(s); draw(); },
      onClick: (id) => void equip(id),
    });
  };
  const editor = () => openCapeEditor({
    initial: own,
    skin: state.skin.skin,
    variant: state.skin.variant,
    online: state.auth.lego.connected,
    onAi: async (prompt, seed) => {
      // Server AI when configured (the key stays on the server); otherwise the offline generator.
      if (aiOnServer && seed === 1) {
        try {
          const r = await lego<{ spec: CapeSpec; left: number }>('POST', '/api/capes/ai', { prompt });
          return { spec: r.spec, source: 'server' as const, left: r.left };
        } catch (err) { errorToast(err); }
      }
      return { spec: capeFromPrompt(prompt, seed), source: 'offline' as const };
    },
    onSave: async (spec) => {
      own = spec;
      saveLocalCape(spec);
      if (state.auth.lego.connected) {
        try {
          await lego('PUT', '/api/me/cape-spec', { spec });
          inv.equipped = (await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: 'cape', itemId: CUSTOM_CAPE_ID })).equipped;
          if (!inv.owned.includes(CUSTOM_CAPE_ID)) inv.owned.push(CUSTOM_CAPE_ID);
          toast('Eigenes Cape gespeichert und angelegt – sichtbar im LEGO Client', 'ok');
        } catch (err) { errorToast(err); }
      } else toast('Cape lokal gespeichert. Mit dem LEGO-Server verbinden, damit andere es sehen.', 'ok');
      closeEditor?.();
      draw();
    },
  });
  let closeEditor: (() => void) | null = null;
  const openEditor = () => { closeEditor = editor(); };
  const equip = async (id: string | null) => {
    if (id === CUSTOM_CAPE_ID && !own) return openEditor();
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
  onLeave(() => { unmount?.(); closeEditor?.(); });
}
