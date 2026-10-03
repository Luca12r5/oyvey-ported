import { icon, pixel } from '../icons.ts';
import { CATALOG } from '@lego/shared';
import { errorToast, fmtDate, fmtNum, html, lego, mount, slotIcon, toast } from '../ui.ts';

interface Progress {
  daily: { claimedToday: boolean; streak: number; nextReward: number };
  quests: { id: string; title: { de: string }; period: string; progress: number; target: number; credits: number; xp: number; complete: boolean; claimed: boolean }[];
  pass: null | { season: { name: string; endsAt: string; xpPerTier: number; tiers: { tier: number; free: { credits?: number; item?: string } | null; premium: { credits?: number; item?: string } | null }[] }; xp: number; level: number; premium: boolean; claimed: string[] };
}

export async function rewardsPage(el: HTMLElement): Promise<void> {
  const [p, c] = await Promise.all([lego<Progress>('GET', '/api/progress'), lego<{ balance: number; history: { delta: number; reason: string; created_at: number }[] }>('GET', '/api/me/credits')]);
  const reward = (r: { credits?: number; item?: string } | null) => !r ? '–' : r.credits ? html`${pixel('coin', 'xs')} ${r.credits}` : html`${slotIcon(CATALOG.find((i) => i.id === r.item)?.slot ?? '', 'sm')} ${CATALOG.find((i) => i.id === r.item)?.name ?? r.item}`;
  mount(el, html`<div class="row between"><h1>Rewards</h1><span class="badge">${pixel('coin', 'xs')} ${fmtNum(c.balance)} Coins</span></div>
    <div class="grid g3">
      <div class="card"><h3>Tägliche Belohnung</h3><p class="small">Serie ${p.daily.streak} · nächste: <b>${p.daily.nextReward}</b> Coins</p><button class="btn primary" id="daily" ${p.daily.claimedToday ? 'disabled' : ''}>${p.daily.claimedToday ? 'Heute abgeholt' : 'Abholen'}</button></div>
      <form class="card" id="promo"><h3>Aktionscode</h3><div class="row"><input name="code" class="grow" maxlength="32" required><button class="btn">Einlösen</button></div></form>
      ${p.pass ? html`<div class="card"><h3>${p.pass.season.name}</h3><p class="small">Stufe ${p.pass.level} · ${fmtNum(p.pass.xp)} XP${p.pass.premium ? ' · Premium' : ''}</p><div class="progress"><i data-p="${((p.pass.xp % p.pass.season.xpPerTier) / p.pass.season.xpPerTier) * 100}"></i></div></div>` : ''}
    </div>
    <h2 class="gap">Quests</h2>
    <div class="grid g3">${p.quests.map((q) => html`<div class="card"><div class="row between"><b>${q.title.de}</b><span class="badge">${q.period === 'daily' ? 'Täglich' : 'Woche'}</span></div>
      <div class="progress gap"><i data-p="${(q.progress / q.target) * 100}"></i></div><div class="small muted">${q.progress}/${q.target} · ${q.credits} Coins · ${q.xp} XP</div>
      ${q.claimed ? html`<span class="small">${icon('check', 'xs')} abgeholt</span>` : html`<button class="btn sm gap ${q.complete ? 'primary' : ''}" data-quest="${q.id}" ${q.complete ? '' : 'disabled'}>Abholen</button>`}</div>`)}</div>
    ${p.pass ? html`<h2 class="gap">Battle Pass</h2><div class="card"><table><thead><tr><th>Stufe</th><th>Kostenlos</th><th>Premium</th></tr></thead><tbody>
      ${p.pass.season.tiers.map((t) => {
        const cell = (r: typeof t.free, track: 'free' | 'premium') => {
          const claimed = p.pass!.claimed.includes(`${t.tier}:${track}`);
          const can = r && !claimed && p.pass!.level >= t.tier && (track === 'free' || p.pass!.premium);
          return html`<td>${reward(r)} ${claimed ? icon('check', 'sm') : can ? html`<button class="btn sm primary" data-tier="${t.tier}" data-track="${track}">Abholen</button>` : ''}</td>`;
        };
        return html`<tr class="${p.pass!.level >= t.tier ? '' : 'muted'}"><td>${t.tier}</td>${cell(t.free, 'free')}${cell(t.premium, 'premium')}</tr>`;
      })}</tbody></table></div>` : ''}
    <h2 class="gap">Coins-Verlauf</h2>
    <div class="card"><table><tbody>${c.history.slice(0, 30).map((h) => html`<tr><td>${fmtDate(h.created_at)}</td><td>${h.delta > 0 ? '+' : ''}${fmtNum(h.delta)}</td><td>${h.reason}</td></tr>`)}</tbody></table>${c.history.length ? '' : html`<div class="empty">Noch keine Buchungen.</div>`}</div>`);
  const run = async (fn: () => Promise<unknown>, msg: string) => { try { await fn(); toast(msg, 'ok'); await rewardsPage(el); } catch (e) { errorToast(e); } };
  el.querySelector('#daily')!.addEventListener('click', () => void run(() => lego('POST', '/api/daily/claim'), 'Belohnung abgeholt'));
  el.querySelector('#promo')!.addEventListener('submit', (e) => { e.preventDefault(); const code = new FormData(e.target as HTMLFormElement).get('code'); void run(() => lego('POST', '/api/promo/redeem', { code }), 'Code eingelöst'); });
  el.onclick = (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (b?.dataset.quest) void run(() => lego('POST', `/api/quests/${b.dataset.quest}/claim`), 'Quest-Belohnung abgeholt');
    if (b?.dataset.tier) void run(() => lego('POST', '/api/pass/claim', { tier: Number(b.dataset.tier), track: b.dataset.track }), 'Battle-Pass-Belohnung abgeholt');
  };
}
