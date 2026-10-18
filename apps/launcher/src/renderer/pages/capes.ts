// Capes gallery (NoRisk-style): tabs All / My capes / Favorites / Vanilla,
// search, template download, big cape fronts in a grid, favourite hearts.
// LEGO capes come from the catalog (equip/buy through the LEGO server); Vanilla
// shows the account's official Minecraft cape.

import { CATALOG, RARITY_INFO, type CatalogItem } from '@lego/shared';
import { go, state } from '../app.ts';
import { errorToast, html, lego, mount, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import { capeFront } from '../itemart.ts';

type Tab = 'all' | 'mine' | 'fav' | 'vanilla';
let tab: Tab = 'all';
let q = '';
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
  const TABS: [Tab, string][] = [['all', 'All'], ['mine', 'My Capes'], ['fav', 'Favorites'], ['vanilla', 'Vanilla']];
  mount(el, html`<div class="tabs" id="ctabs">${TABS.map(([id, l]) => html`<button data-t="${id}" class="${tab === id ? 'active' : ''}">${l}</button>`)}</div>
    <div class="prof-bar">
      <div class="searchbar">${icon('search', 'sm')}<input id="cq" placeholder="Search capes…" value="${q}" autocomplete="off"><span class="sb-sep"></span><button class="sb-ic" id="favOnly" title="Favoriten">${icon('heart', 'sm')}</button></div>
      <span class="grow"></span>
      <button class="btn" id="tpl">${icon('download', 'sm')} TEMPLATE</button>
      <button class="btn" id="up" disabled title="Eigene Capes brauchen eine Moderation auf dem LEGO-Server – kommt später.">${icon('upload', 'sm')} UPLOAD</button>
    </div>
    <div class="page-sep"></div>
    <div class="cape-grid" id="cgrid"></div>`);
  const grid = el.querySelector<HTMLElement>('#cgrid')!;
  const draw = async () => {
    const f = favs();
    const query = q.trim().toLowerCase();
    if (tab === 'vanilla') {
      const url = state.skin.cape;
      mount(grid, url ? html`<div class="cape-card active"><div class="cc-art"><img id="vimg" alt=""></div><div class="cc-name">Minecraft-Cape</div><div class="cc-sub">Offiziell · aktiv</div></div>`
        : html`<div class="empty">${state.auth.signedIn ? 'Dein Konto hat kein offizielles Minecraft-Cape aktiv.' : 'Melde dich an, um deine offiziellen Minecraft-Capes zu sehen.'}</div>`);
      if (url) { const v = await vanillaFront(url); const img = grid.querySelector<HTMLImageElement>('#vimg'); if (img && v) img.src = v; }
      return;
    }
    const list = capes.filter((c) => (tab === 'all' || (tab === 'mine' ? inv.owned.includes(c.id) : f.has(c.id))) && (!query || c.name.toLowerCase().includes(query)));
    const eq = inv.equipped.cape;
    mount(grid, html`${tab === 'all' && !query ? html`<div class="cape-card none ${eq ? '' : 'active'}" data-none="1"><div class="cc-art empty-cape">${icon('close', 'lg')}</div><div class="cc-name">No Cape</div><div class="cc-sub">&nbsp;</div></div>` : ''}
      ${list.length ? list.map((c) => card(c, inv.owned.includes(c.id), eq === c.id, f.has(c.id))) : html`<div class="empty">${tab === 'fav' ? 'Noch keine Favoriten – tippe auf das Herz.' : tab === 'mine' ? (state.auth.lego.connected ? 'Du besitzt noch keine LEGO-Capes.' : 'Verbinde dich mit dem LEGO-Server, um deine Capes zu sehen.') : 'Keine Capes gefunden.'}</div>`}`);
  };
  void draw();
  el.querySelector('#cq')!.addEventListener('input', (e) => { q = (e.target as HTMLInputElement).value; void draw(); });
  el.onclick = async (e) => {
    const t = e.target as HTMLElement;
    const tb = t.closest<HTMLButtonElement>('[data-t]');
    if (tb) { tab = tb.dataset.t as Tab; return void capesPage(el); }
    if (t.closest('#tpl')) return template();
    if (t.closest('#favOnly')) { tab = 'fav'; return void capesPage(el); }
    const fav = t.closest<HTMLElement>('[data-fav]');
    if (fav) {
      const f = favs();
      if (f.has(fav.dataset.fav!)) f.delete(fav.dataset.fav!); else f.add(fav.dataset.fav!);
      saveFavs(f);
      return void draw();
    }
    const none = t.closest('[data-none]');
    const cardEl = t.closest<HTMLElement>('[data-cape]');
    if (!none && !cardEl) return;
    if (!state.auth.lego.connected) return toast('Zum Anlegen von Capes mit dem LEGO-Server verbinden.');
    try {
      if (none) {
        inv.equipped = (await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: 'cape', itemId: null })).equipped;
        toast('Cape abgelegt', 'ok');
        return void draw();
      }
      const id = cardEl!.dataset.cape!;
      if (!inv.owned.includes(id)) { toast('Dieses Cape gibt es im Shop.'); return void go('shop'); }
      inv.equipped = (await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: 'cape', itemId: inv.equipped.cape === id ? null : id })).equipped;
      toast(inv.equipped.cape === id ? 'Cape angelegt – sichtbar im LEGO Client' : 'Cape abgelegt', 'ok');
      void draw();
    } catch (err) { errorToast(err); }
  };
}

function card(c: CatalogItem, owned: boolean, equipped: boolean, fav: boolean) {
  const r = RARITY_INFO[c.rarity];
  return html`<div class="cape-card ${equipped ? 'active' : ''} ${owned ? '' : 'locked'}" data-cape="${c.id}">
    <button class="cc-fav ${fav ? 'on' : ''}" data-fav="${c.id}" title="Favorit">${icon('heart', 'sm')}</button>
    <div class="cc-art">${capeFront(c.id, c.name)}</div>
    <div class="cc-name" title="${c.name}">${c.name}</div>
    <div class="cc-sub" data-c="${r.color}">${equipped ? 'Angelegt' : owned ? r.label.de : c.price ? html`${icon('coin', 'xs')} ${c.price}` : r.label.de}</div>
  </div>`;
}
