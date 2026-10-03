import { CATALOG, NAME_TAGS, RARITY_INFO, tagCharColor, type CatalogItem, type NameTagStyle, type Rarity } from '@lego/shared';
import { state } from '../app.ts';
import { confirm, errorToast, fmtNum, html, idem, lego, mount, slotIcon, toast, type Raw } from '../ui.ts';

const SLOTS: [string, string][] = [['', 'Alle'], ['cape', 'Capes'], ['wings', 'Flügel'], ['hat', 'Hüte'], ['face', 'Gesicht'], ['back', 'Rücken'], ['aura', 'Auren'], ['pet', 'Pets'], ['vehicle', 'Fahrzeuge'], ['nametag', 'Name Tags']];
const UNLOCK: Record<string, string> = { default: 'Kostenlos', shop: 'Shop', battlepass: 'Battle Pass', achievement: 'Erfolg', event: 'Event', staff: 'Team' };

export function rarityColor(r: Rarity): string {
  return state.settings.rarityColors[r] ?? RARITY_INFO[r].color;
}

export function tagHtml(style: NameTagStyle, text: string): Raw {
  const chars = [...text];
  const t = performance.now() / 1000;
  const spans = chars.map((ch, i) => html`<span data-color="${tagCharColor(style, i, chars.length, t)}">${ch}</span>`);
  return html`<span class="tag frame-${style.frame.style}" data-fc="${style.frame.color}">${style.icon ? html`<span data-color="${style.frame.color}">${style.icon}</span>` : ''}${style.text.bold ? html`<b>${spans}</b>` : spans}</span>`;
}

interface Inv { owned: string[]; equipped: Record<string, string> }

function card(i: CatalogItem, inv: Inv, mode: 'collection' | 'shop'): Raw {
  const owned = inv.owned.includes(i.id);
  const equipped = inv.equipped[i.slot] === i.id;
  const tag = i.kind === 'nametag' ? NAME_TAGS.find((t) => t.id === i.id) : undefined;
  const info = RARITY_INFO[i.rarity];
  return html`<div class="item ${info.animatedFrame ? 'anim' : ''}" data-c="${rarityColor(i.rarity)}">
    <div class="thumb">${tag ? tagHtml(tag, state.auth.name ?? 'Steve') : slotIcon(i.slot)}</div>
    <div class="row between"><b>${i.name}</b><span class="badge c" data-c="${rarityColor(i.rarity)}">${info.label.de}</span></div>
    <div class="small muted">${UNLOCK[i.unlock] ?? i.unlock}${i.price ? ` · ${fmtNum(i.price)} Credits` : ''}${i.seasonal ? ' · Saisonal' : ''}</div>
    <div class="row">${owned
      ? html`<button class="btn sm ${equipped ? '' : 'primary'}" data-equip="${i.id}" data-slot="${i.slot}" data-on="${equipped ? '0' : '1'}">${equipped ? 'Ablegen' : 'Ausrüsten'}</button>`
      : mode === 'shop' && i.unlock === 'shop' ? html`<button class="btn sm primary" data-buy="${i.id}">Kaufen</button>` : html`<span class="small muted">🔒 ${UNLOCK[i.unlock]}</span>`}</div>
  </div>`;
}

async function browser(el: HTMLElement, mode: 'collection' | 'shop'): Promise<void> {
  const [inv, me] = await Promise.all([lego<Inv>('GET', '/api/inventory'), lego<{ credits: number }>('GET', '/api/me')]);
  const f = { slot: '', rarity: '', q: '', owned: false };
  mount(el, html`<div class="row between"><h1>${mode === 'shop' ? 'Shop' : 'Cosmetics'}</h1><span class="badge">💰 <span id="bal">${fmtNum(me.credits)}</span> Credits</span></div>
    ${mode === 'collection'
      ? html`<p class="muted">Sammlung ${inv.owned.length} / ${CATALOG.length}. Ausgerüstete Cosmetics sehen andere Spieler mit LEGO Client auf jedem Server und in jedem Spielmodus, solange beide mit dem LEGO-Server verbunden sind. Spieler mit anderen Clients (Vanilla, NoRisk, Lunar …) sehen sie nicht.</p><div class="progress"><i data-p="${(inv.owned.length / CATALOG.length) * 100}"></i></div>`
      : html`<p class="muted">Feste Preise, keine Lootboxen. Gekauft wird mit LEGO Credits.</p>`}
    <div class="row gap"><input id="q" class="grow" placeholder="Suchen…"><select id="rar"><option value="">Alle Seltenheiten</option>${(Object.keys(RARITY_INFO) as Rarity[]).map((r) => html`<option value="${r}">${RARITY_INFO[r].label.de}</option>`)}</select>
      ${mode === 'collection' ? html`<label class="check"><input type="checkbox" id="owned"> Nur im Besitz</label>` : ''}</div>
    <div class="tabs gap" id="slots">${SLOTS.map(([id, l], i) => html`<button data-slot="${id}" class="${i === 0 ? 'active' : ''}">${l}</button>`)}</div>
    <div class="grid g4" id="items"></div>`);
  const list = el.querySelector<HTMLElement>('#items')!;
  const draw = () => {
    const items = CATALOG.filter((i) => (!f.slot || i.slot === f.slot) && (!f.rarity || i.rarity === f.rarity) && (!f.q || i.name.toLowerCase().includes(f.q))
      && (!f.owned || inv.owned.includes(i.id)) && (mode !== 'shop' || (i.unlock === 'shop' && !inv.owned.includes(i.id))));
    mount(list, items.length ? html`${items.slice(0, 300).map((i) => card(i, inv, mode))}` : html`<div class="empty">Keine Treffer.</div>`);
  };
  el.querySelector('#q')!.addEventListener('input', (e) => { f.q = (e.target as HTMLInputElement).value.toLowerCase(); draw(); });
  el.querySelector('#rar')!.addEventListener('change', (e) => { f.rarity = (e.target as HTMLSelectElement).value; draw(); });
  el.querySelector('#owned')?.addEventListener('change', (e) => { f.owned = (e.target as HTMLInputElement).checked; draw(); });
  el.querySelector('#slots')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    f.slot = b.dataset.slot!;
    el.querySelectorAll('#slots button').forEach((x) => x.classList.toggle('active', x === b));
    draw();
  });
  list.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    try {
      if (b.dataset.equip) {
        const r = await lego<{ equipped: Record<string, string> }>('PUT', '/api/equip', { slot: b.dataset.slot, itemId: b.dataset.on === '1' ? b.dataset.equip : null });
        inv.equipped = r.equipped;
      } else if (b.dataset.buy) {
        const item = CATALOG.find((i) => i.id === b.dataset.buy)!;
        if (!(await confirm('Kaufen?', `${item.name} für ${fmtNum(item.price)} Credits?`, 'Kaufen'))) return;
        const r = await lego<{ balance: number }>('POST', '/api/shop/buy', { itemId: item.id, idempotencyKey: idem() });
        inv.owned.push(item.id);
        el.querySelector('#bal')!.textContent = fmtNum(r.balance);
        toast(`${item.name} gekauft`, 'ok');
      }
      draw();
    } catch (err) { errorToast(err); }
  });
  draw();
}

export const cosmeticsPage = (el: HTMLElement) => browser(el, 'collection');
export const shopPage = (el: HTMLElement) => browser(el, 'shop');
