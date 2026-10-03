import { api, confirmDialog, errorToast, fmtDate, fmtNum, html, idemKey, loadCatalog, mount, rarityBadge, slotIcon, state, tagPreview, toast } from '../core.js';

const SLOTS = [['', 'Alle'], ['cape', 'Capes'], ['wings', 'Flügel'], ['hat', 'Hüte'], ['face', 'Gesicht'], ['back', 'Rücken'], ['aura', 'Auren'], ['pet', 'Pets'], ['vehicle', 'Fahrzeuge'], ['nametag', 'Name Tags']];
const UNLOCK = { default: 'Kostenlos', shop: 'Shop', battlepass: 'Battle Pass', achievement: 'Erfolg', event: 'Event', staff: 'Team' };

async function inventory() {
  if (!state.user) return { owned: [], equipped: {} };
  try { return await api('GET', '/api/inventory'); } catch { return { owned: [], equipped: {} }; }
}

function itemCard(item, catalog, inv, mode) {
  const r = catalog.rarities[item.rarity];
  const owned = inv.owned.includes(item.id);
  const equipped = inv.equipped[item.slot] === item.id;
  const tag = item.kind === 'nametag' ? catalog.nameTags.find((t) => t.id === item.id) : null;
  return html`<div class="item ${r.animatedFrame ? 'animated' : ''}" data-c="${r.color}">
    <div class="thumb">${tag ? tagPreview(tag, state.user?.name ?? 'Steve') : slotIcon(item.slot)}</div>
    <div class="row between"><span class="name">${item.name}</span>${rarityBadge(item.rarity)}</div>
    <div class="small muted">${UNLOCK[item.unlock] ?? item.unlock}${item.seasonal ? ' · Saisonal' : ''}${item.price ? ` · ${fmtNum(item.price)} Coins` : ''}</div>
    ${state.user ? html`<div class="row">
      ${owned ? html`<span class="owned">✔ Im Besitz</span><span class="spacer"></span><button class="btn small ${equipped ? '' : 'primary'}" data-equip="${item.id}" data-slot="${item.slot}" data-on="${equipped ? '0' : '1'}">${equipped ? 'Ablegen' : 'Ausrüsten'}</button>`
        : mode === 'shop' && item.unlock === 'shop' ? html`<button class="btn small primary" data-buy="${item.id}">Kaufen</button><button class="btn small" data-gift="${item.id}">Verschenken</button>` : ''}
    </div>` : ''}
  </div>`;
}

async function browser(el, mode) {
  const [catalog, inv] = await Promise.all([loadCatalog(), inventory()]);
  const filter = { slot: '', rarity: '', q: '', owned: false };
  const title = mode === 'shop' ? 'Shop' : 'Cosmetics';
  let balance = null;
  if (state.user) balance = (await api('GET', '/api/me')).credits;
  mount(el, html`<div class="row between"><h1>${title}</h1>${balance !== null ? html`<span class="badge">💰 ${fmtNum(balance)} Coins</span>` : ''}</div>
    ${mode === 'shop' ? html`<p class="muted">Gegenstände werden mit LEGO Coins gekauft. Coins gibt es über tägliche Belohnungen, Quests, den Battle Pass – oder als Paket. Keine Lootboxen, keine Zufallsbelohnungen: du siehst immer genau, was du bekommst.</p>` : html`<p class="muted">Sammlung: ${inv.owned.length} / ${catalog.items.length} freigeschaltet.</p><div class="progress"><span data-p="${(inv.owned.length / catalog.items.length) * 100}"></span></div>`}
    <div class="card row spaced">
      <input id="q" placeholder="Suchen…" aria-label="Suche">
      <select id="rarity" aria-label="Seltenheit"><option value="">Alle Seltenheiten</option>${Object.values(catalog.rarities).map((r) => html`<option value="${r.id}">${r.label.de}</option>`)}</select>
      ${state.user ? html`<label class="check"><input type="checkbox" id="owned"> Nur im Besitz</label>` : ''}
    </div>
    <div class="tabs spaced" id="slots">${SLOTS.map(([id, label], i) => html`<button data-slot="${id}" class="${i === 0 ? 'active' : ''}">${label}</button>`)}</div>
    <div class="grid cols-4" id="items"></div>
    ${mode === 'shop' ? html`<h2 class="spaced">Coins & Mitgliedschaften</h2><div id="products" class="grid cols-3"></div>` : ''}`);
  const list = el.querySelector('#items');
  const draw = () => {
    const items = catalog.items.filter((i) =>
      (!filter.slot || i.slot === filter.slot) && (!filter.rarity || i.rarity === filter.rarity) &&
      (!filter.q || i.name.toLowerCase().includes(filter.q)) && (!filter.owned || inv.owned.includes(i.id)) &&
      (mode !== 'shop' || i.unlock === 'shop'));
    mount(list, items.length ? html`${items.slice(0, 400).map((i) => itemCard(i, catalog, inv, mode))}` : html`<div class="empty">Keine Treffer.</div>`);
  };
  el.querySelector('#q').addEventListener('input', (e) => { filter.q = e.target.value.trim().toLowerCase(); draw(); });
  el.querySelector('#rarity').addEventListener('change', (e) => { filter.rarity = e.target.value; draw(); });
  el.querySelector('#owned')?.addEventListener('change', (e) => { filter.owned = e.target.checked; draw(); });
  el.querySelector('#slots').addEventListener('click', (e) => {
    const b = e.target.closest('button');
    if (!b) return;
    filter.slot = b.dataset.slot;
    el.querySelectorAll('#slots button').forEach((x) => x.classList.toggle('active', x === b));
    draw();
  });
  list.addEventListener('click', async (e) => {
    const b = e.target.closest('button');
    if (!b) return;
    const d = b.dataset;
    try {
      if (d.equip) {
        const r = await api('PUT', '/api/equip', { slot: d.slot, itemId: d.on === '1' ? d.equip : null });
        inv.equipped = r.equipped;
        toast(d.on === '1' ? 'Ausgerüstet – im Spiel sichtbar für andere LEGO-Spieler' : 'Abgelegt', 'ok');
      } else if (d.buy) {
        const item = catalog.items.find((i) => i.id === d.buy);
        if (!(await confirmDialog('Kaufen?', `${item.name} für ${fmtNum(item.price)} Coins kaufen?`, { confirmText: 'Kaufen' }))) return;
        const r = await api('POST', '/api/shop/buy', { itemId: item.id, idempotencyKey: idemKey() });
        inv.owned.push(item.id);
        balance = r.balance;
        toast(`Gekauft! Neuer Kontostand: ${fmtNum(r.balance)}`, 'ok');
      } else if (d.gift) {
        const item = catalog.items.find((i) => i.id === d.gift);
        const friends = (await api('GET', '/api/friends')).friends;
        if (!friends.length) return toast('Du kannst nur Freunden etwas schenken.', 'error');
        const name = prompt(`An welchen Freund verschenken?\n${friends.map((f) => f.name).join(', ')}`);
        const friend = friends.find((f) => f.name.toLowerCase() === (name ?? '').trim().toLowerCase());
        if (!friend) return;
        if (!(await confirmDialog('Verschenken?', `${item.name} für ${fmtNum(item.price)} Coins an ${friend.name} schenken?`, { confirmText: 'Verschenken' }))) return;
        await api('POST', '/api/shop/gift', { itemId: item.id, to: friend.id, idempotencyKey: idemKey() });
        toast(`Geschenk an ${friend.name} gesendet`, 'ok');
      }
      draw();
    } catch (err) { errorToast(err); }
  });
  draw();
  if (mode === 'shop') {
    const prog = await api('GET', '/api/public/progression');
    const status = await api('GET', '/api/public/status');
    const box = el.querySelector('#products');
    mount(box, html`${prog.products.map((p) => html`<div class="card stack"><h3>${p.name}</h3><div class="kpi">${(p.priceCents / 100).toFixed(2).replace('.', ',')} €</div>
      <div class="muted small">${p.kind === 'subscription' ? 'pro Monat, monatlich kündbar' : 'einmalig'} · inkl. MwSt.</div>
      ${p.perks.length ? html`<ul class="small">${p.perks.map((x) => html`<li>${x}</li>`)}</ul>` : ''}
      <button class="btn ${status.paymentsEnabled && state.user ? 'primary' : ''}" data-product="${p.id}" ${status.paymentsEnabled && state.user ? '' : 'disabled'}>${status.paymentsEnabled ? (state.user ? 'Zur Kasse' : 'Anmelden zum Kaufen') : 'Zahlungen nicht verfügbar'}</button></div>`)}`);
    box.addEventListener('click', async (e) => {
      const b = e.target.closest('button[data-product]');
      if (!b) return;
      try { const r = await api('POST', '/api/payments/checkout', { productId: b.dataset.product }); location.assign(r.url); } catch (err) { errorToast(err); }
    });
  }
}

export const cosmetics = (el) => browser(el, 'cosmetics');
export const shop = (el) => browser(el, 'shop');

export async function battlePass(el) {
  const [catalog, pub] = await Promise.all([loadCatalog(), api('GET', '/api/public/progression')]);
  let mine = null;
  if (state.user) mine = (await api('GET', '/api/progress')).pass;
  const season = mine?.season ?? pub.seasons[0];
  const level = mine?.level ?? 0;
  const reward = (r) => !r ? html`<span class="muted">–</span>` : r.credits ? html`💰 ${r.credits}` : html`${slotIcon(catalog.items.find((i) => i.id === r.item)?.slot)} ${catalog.items.find((i) => i.id === r.item)?.name ?? r.item}`;
  mount(el, html`<h1>${season.name}</h1>
    <p class="muted">Bis ${season.endsAt} · ${season.xpPerTier} XP pro Stufe. XP gibt es für Quests.${mine ? ` Du bist auf Stufe ${level} (${fmtNum(mine.xp)} XP)${mine.premium ? ' mit Premium.' : '.'}` : ''}</p>
    ${mine ? html`<div class="progress"><span data-p="${((mine.xp % season.xpPerTier) / season.xpPerTier) * 100}"></span></div>` : ''}
    <div class="card table-wrap spaced"><table><thead><tr><th>Stufe</th><th>Kostenlos</th><th>Premium</th></tr></thead><tbody>
      ${season.tiers.map((t) => {
        const can = (track) => mine && level >= t.tier && (track === 'free' || mine.premium) && !mine.claimed.includes(`${t.tier}:${track}`);
        const cell = (r, track) => html`<td>${reward(r)} ${r && mine?.claimed.includes(`${t.tier}:${track}`) ? html`<span class="owned">✔</span>` : r && can(track) ? html`<button class="btn small primary" data-tier="${t.tier}" data-track="${track}">Abholen</button>` : ''}</td>`;
        return html`<tr class="${level >= t.tier ? '' : 'muted'}"><td>${t.tier}</td>${cell(t.free, 'free')}${cell(t.premium, 'premium')}</tr>`;
      })}
    </tbody></table></div>`);
  el.onclick = async (e) => {
    const b = e.target.closest('button[data-tier]');
    if (!b) return;
    try { await api('POST', '/api/pass/claim', { tier: Number(b.dataset.tier), track: b.dataset.track }); toast('Belohnung abgeholt', 'ok'); await battlePass(el); } catch (err) { errorToast(err); }
  };
}

export async function rewards(el) {
  const [p, credits] = await Promise.all([api('GET', '/api/progress'), api('GET', '/api/me/credits')]);
  mount(el, html`<h1>Belohnungen</h1>
    <div class="grid cols-2">
      <div class="card stack"><h2>Tägliche Belohnung</h2>
        <p>Serie: <b>${p.daily.streak}</b> Tag(e). Nächste Belohnung: <b>${p.daily.nextReward} Coins</b>.</p>
        <button class="btn primary" id="daily" ${p.daily.claimedToday ? 'disabled' : ''}>${p.daily.claimedToday ? 'Heute abgeholt' : 'Abholen'}</button></div>
      <form class="card stack" id="promo"><h2>Aktionscode</h2><input name="code" maxlength="32" placeholder="CODE" required><button class="btn">Einlösen</button></form>
    </div>
    <h2 class="spaced">Quests</h2>
    <div class="grid cols-3">${p.quests.map((q) => html`<div class="card stack"><div class="row between"><b>${q.title.de}</b><span class="badge">${q.period === 'daily' ? 'Täglich' : 'Wöchentlich'}</span></div>
      <div class="progress"><span data-p="${(q.progress / q.target) * 100}"></span></div><div class="small muted">${q.progress} / ${q.target} · ${q.credits} Coins · ${q.xp} XP</div>
      ${q.claimed ? html`<span class="owned">✔ Abgeholt</span>` : html`<button class="btn small ${q.complete ? 'primary' : ''}" data-quest="${q.id}" ${q.complete ? '' : 'disabled'}>Abholen</button>`}</div>`)}</div>
    <h2 class="spaced">Coins-Verlauf</h2>
    <div class="card table-wrap"><table><thead><tr><th>Datum</th><th>Änderung</th><th>Stand</th><th>Grund</th></tr></thead><tbody>
      ${credits.history.map((h) => html`<tr><td>${fmtDate(h.created_at)}</td><td class="${h.delta > 0 ? 'pos' : 'neg'}">${h.delta > 0 ? '+' : ''}${fmtNum(h.delta)}</td><td>${fmtNum(h.balance_after)}</td><td>${h.reason}</td></tr>`)}
    </tbody></table>${credits.history.length ? '' : html`<div class="empty">Noch keine Buchungen.</div>`}</div>`);
  el.querySelector('#daily').addEventListener('click', async () => {
    try { const r = await api('POST', '/api/daily/claim'); toast(`+${r.credits} Coins (Serie ${r.streak})`, 'ok'); await rewards(el); } catch (e) { errorToast(e); }
  });
  el.querySelector('#promo').addEventListener('submit', async (e) => {
    e.preventDefault();
    try { const r = await api('POST', '/api/promo/redeem', { code: new FormData(e.currentTarget).get('code') }); toast(`+${r.credits} Coins`, 'ok'); await rewards(el); } catch (err) { errorToast(err); }
  });
  el.querySelectorAll('[data-quest]').forEach((b) => b.addEventListener('click', async () => {
    try { const r = await api('POST', `/api/quests/${b.dataset.quest}/claim`); toast(`+${r.credits} Coins, +${r.xp} XP`, 'ok'); await rewards(el); } catch (e) { errorToast(e); }
  }));
}

