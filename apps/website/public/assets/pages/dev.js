// Developer / admin dashboard. The UI hides what the user can't do, but every
// action is authorised again by the server (see apps/backend/src/routes/admin.ts).

import { api, avatar, confirmDialog, errorToast, fmtDate, fmtNum, hasPerm, html, idemKey, loadCatalog, mount, state, toast } from '../core.js';

const LARGE = 5000;

function guard() {
  if (!state.user) throw Object.assign(new Error('Login required'), { status: 401 });
  if (!state.user.roles?.length) throw new Error('Kein Zugriff: Dieses Dashboard ist nur für das Team.');
}

export async function dashboard(el) {
  guard();
  const tabs = [
    ['overview', 'Übersicht', 'users.view'], ['users', 'Spieler', 'users.view'], ['feedback', 'Feedback', 'feedback.manage'],
    ['reports', 'Meldungen', 'reports.manage'], ['news', 'News', 'news.manage'], ['promo', 'Aktionscodes', 'promo.manage'],
    ['audit', 'Audit-Log', 'audit.view'], ['system', 'System', 'system.backup'],
  ].filter(([, , p]) => hasPerm(p));
  const active = new URLSearchParams(location.search).get('tab') ?? tabs[0]?.[0];
  mount(el, html`<h1>Entwickler-Dashboard</h1><div class="tabs">${tabs.map(([id, label]) => html`<a class="btn small ${id === active ? 'primary' : ''}" href="/dev?tab=${id}" data-link>${label}</a>`)}</div><div id="tab"></div>`);
  const box = el.querySelector('#tab');
  const fn = { overview, users, feedback, reports, news, promo, audit, system }[active];
  if (fn) await fn(box);
}

async function overview(box) {
  const o = await api('GET', '/api/admin/overview');
  mount(box, html`<div class="grid cols-4">
    <div class="card"><div class="kpi">${fmtNum(o.users)}</div><div class="muted">Spieler</div></div>
    <div class="card"><div class="kpi">${fmtNum(o.activeToday)}</div><div class="muted">Aktiv (24 h)</div></div>
    <div class="card"><div class="kpi">${fmtNum(o.creditsInCirculation)}</div><div class="muted">Credits im Umlauf</div></div>
    <div class="card"><div class="kpi">${fmtNum(o.creditsGrantedToday)}</div><div class="muted">Heute vergeben (Admin)</div></div>
    <div class="card"><div class="kpi">${o.openReports}</div><div class="muted">Offene Meldungen</div></div>
    <div class="card"><div class="kpi">${o.newFeedback}</div><div class="muted">Neues Feedback</div></div>
    <div class="card"><div class="kpi">${o.maintenance ? 'AN' : 'aus'}</div><div class="muted">Wartungsmodus</div></div></div>
    <h2 class="spaced">Auffällige Credit-Einnahmen (24 h, ohne Admin/Käufe)</h2>
    ${o.suspicious.length ? html`<div class="card table-wrap"><table><thead><tr><th>Spieler</th><th>Verdient</th></tr></thead><tbody>${o.suspicious.map((s) => html`<tr><td><a href="/dev/users/${s.user_id}" data-link>${s.user_id}</a></td><td>${fmtNum(s.earned)}</td></tr>`)}</tbody></table></div>` : html`<div class="empty">Nichts Auffälliges.</div>`}`);
}

async function users(box) {
  mount(box, html`<form class="card row" id="s"><input name="q" placeholder="Name, UUID oder ID (mind. 2 Zeichen)" minlength="2" required><button class="btn primary">Suchen</button></form><div id="res" class="spaced"></div>`);
  box.querySelector('#s').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const r = await api('GET', `/api/admin/users?q=${encodeURIComponent(new FormData(e.currentTarget).get('q'))}`);
      mount(box.querySelector('#res'), r.users.length ? html`<div class="card table-wrap"><table><thead><tr><th>Spieler</th><th>Credits</th><th>Zuletzt</th><th></th></tr></thead><tbody>${r.users.map((u) => html`<tr><td><div class="row">${avatar(u.name)}<div><b>${u.name}</b><div class="small muted mono">${u.uuid}</div></div></div></td><td>${fmtNum(u.credits)}</td><td>${fmtDate(u.lastSeen)}${u.banned ? html` <span class="badge c" data-c="#ef4444">gesperrt</span>` : ''}</td><td><a class="btn small" href="/dev/users/${u.id}" data-link>Öffnen</a></td></tr>`)}</tbody></table></div>` : html`<div class="empty">Keine Treffer.</div>`);
    } catch (err) { errorToast(err); }
  });
}

export async function user(el, id) {
  guard();
  const [d, catalog] = await Promise.all([api('GET', `/api/admin/users/${id}`), loadCatalog()]);
  const u = d.user;
  const roles = ['admin', 'developer', 'moderator', 'support'];
  mount(el, html`<a class="btn small" href="/dev?tab=users" data-link>← Spieler</a>
    <div class="row spaced">${avatar(u.name, 'lg')}<div><h1>${u.name}</h1><div class="small muted mono">${u.uuid} · ${u.id}</div><div class="small muted">Seit ${fmtDate(u.createdAt)} · zuletzt ${fmtDate(u.lastSeen)} · Status ${d.presence.status}</div></div></div>
    <div class="grid cols-2 spaced">
      ${hasPerm('credits.grant') ? html`<form class="card stack" id="credits"><h2>Credits: ${fmtNum(u.credits)}</h2>
        <div class="field"><label>Änderung (negativ = abziehen${hasPerm('credits.deduct') ? '' : ', nur Admins'})</label><input name="delta" type="number" step="1" required></div>
        <div class="field"><label>Grund (Pflicht, landet im Audit-Log)</label><input name="reason" minlength="5" maxlength="300" required></div>
        <button class="btn primary">Buchen</button>
        <p class="small muted">Ab ${fmtNum(LARGE)} Credits ist eine zusätzliche Bestätigung nötig. Doppelklicks buchen nicht doppelt (Idempotenzschlüssel).</p></form>` : ''}
      ${hasPerm('credits.grant') ? html`<form class="card stack" id="item"><h2>Gegenstand vergeben</h2>
        <select name="itemId">${catalog.items.map((i) => html`<option value="${i.id}">${i.name} (${i.slot}, ${i.rarity})</option>`)}</select>
        <input name="reason" minlength="5" maxlength="300" placeholder="Grund" required><button class="btn">Vergeben</button></form>` : ''}
      ${hasPerm('roles.manage') ? html`<div class="card stack"><h2>Rollen</h2>${roles.map((r) => html`<label class="check"><input type="checkbox" data-role="${r}" ${d.roles.includes(r) ? 'checked' : ''}> ${r}</label>`)}</div>` : ''}
      ${hasPerm('users.ban') ? html`<form class="card stack" id="ban"><h2>Sperre</h2><p class="small muted">${u.bannedUntil && u.bannedUntil > Date.now() ? `Gesperrt bis ${u.bannedUntil > 8e15 ? 'dauerhaft' : fmtDate(u.bannedUntil)}: ${u.banReason ?? ''}` : 'Nicht gesperrt.'}</p>
        <select name="hours"><option value="24">24 Stunden</option><option value="168">7 Tage</option><option value="720">30 Tage</option><option value="perm">Dauerhaft</option><option value="0">Sperre aufheben</option></select>
        <input name="reason" minlength="3" maxlength="300" placeholder="Grund" required><button class="btn danger">Anwenden</button></form>` : ''}
    </div>
    <h2 class="spaced">Credits-Buchungen</h2>
    <div class="card table-wrap"><table><thead><tr><th>Datum</th><th>Änderung</th><th>Stand</th><th>Art</th><th>Grund</th></tr></thead><tbody>${d.ledger.map((l) => html`<tr><td>${fmtDate(l.created_at)}</td><td class="${l.delta > 0 ? 'pos' : 'neg'}">${l.delta > 0 ? '+' : ''}${fmtNum(l.delta)}</td><td>${fmtNum(l.balance_after)}</td><td>${l.kind}</td><td>${l.reason}</td></tr>`)}</tbody></table></div>
    <h2 class="spaced">Inventar (${d.inventory.length})</h2>
    <div class="card small">${d.inventory.length ? d.inventory.map((i) => html`<span class="badge">${i.item_id} · ${i.source}</span> `) : html`<span class="muted">Keine gekauften/vergebenen Gegenstände.</span>`}</div>
    <h2 class="spaced">Meldungen gegen diesen Spieler</h2>
    ${d.reports.length ? html`<div class="card table-wrap"><table><tbody>${d.reports.map((r) => html`<tr><td>${fmtDate(r.created_at)}</td><td>${r.reason}</td><td>${r.details}</td><td>${r.reporter}</td><td>${r.status}</td></tr>`)}</tbody></table></div>` : html`<div class="empty">Keine.</div>`}`);

  const reload = () => user(el, id);
  el.querySelector('#credits')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const delta = Number(f.get('delta'));
    const reason = String(f.get('reason'));
    if (!Number.isInteger(delta) || delta === 0) return toast('Bitte eine ganze Zahl ungleich 0 eingeben.', 'error');
    const large = Math.abs(delta) >= LARGE;
    const ok = await confirmDialog(delta > 0 ? 'Credits vergeben?' : 'Credits abziehen?', `${delta > 0 ? '+' : ''}${fmtNum(delta)} Credits für ${u.name}. Grund: ${reason}`,
      { danger: delta < 0, requireTyping: large ? String(Math.abs(delta)) : null });
    if (!ok) return;
    try {
      const r = await api('POST', '/api/admin/credits', { userId: u.id, delta, reason, idempotencyKey: idemKey(), confirm: large });
      toast(`Gebucht. Neuer Stand: ${fmtNum(r.balance)}`, 'ok');
      await reload();
    } catch (err) { errorToast(err); }
  });
  el.querySelector('#item')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    try { const r = await api('POST', '/api/admin/items', { userId: u.id, itemId: f.get('itemId'), reason: f.get('reason') }); toast(r.added ? 'Vergeben' : 'War schon im Besitz', 'ok'); await reload(); } catch (err) { errorToast(err); }
  });
  el.querySelectorAll('[data-role]').forEach((cb) => cb.addEventListener('change', async () => {
    const grant = cb.checked;
    if (!(await confirmDialog(grant ? 'Rolle vergeben?' : 'Rolle entziehen?', `${cb.dataset.role} für ${u.name}`, { danger: !grant }))) { cb.checked = !grant; return; }
    try { await api('PUT', '/api/admin/roles', { userId: u.id, role: cb.dataset.role, grant }); toast('Rollen aktualisiert', 'ok'); } catch (err) { cb.checked = !grant; errorToast(err); }
  }));
  el.querySelector('#ban')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const h = f.get('hours');
    if (!(await confirmDialog('Sperre anwenden?', `${u.name}: ${h === '0' ? 'Sperre aufheben' : h === 'perm' ? 'dauerhaft sperren' : `${h} Stunden sperren`}`, { danger: true }))) return;
    try { await api('POST', '/api/admin/ban', { userId: u.id, hours: h === 'perm' ? null : Number(h), reason: f.get('reason') }); toast('Gespeichert', 'ok'); await reload(); } catch (err) { errorToast(err); }
  });
}

async function feedback(box) {
  const d = await api('GET', '/api/feedback?status=new');
  const all = await api('GET', '/api/feedback');
  const counts = all.items.reduce((m, f) => ((m[f.status] = (m[f.status] ?? 0) + 1), m), {});
  mount(box, html`<div class="row">${Object.entries(counts).map(([k, v]) => html`<a class="badge" href="/feedback?status=${k}" data-link>${k}: ${v}</a>`)}</div>
    <h2 class="spaced">Neu (${d.items.length})</h2>
    <div class="stack">${d.items.map((f) => html`<a class="card row" href="/feedback/${f.id}" data-link><span class="badge">${f.kind}</span><b>${f.title}</b><span class="spacer"></span><span class="small muted">${f.author} · ${fmtDate(f.created_at)}</span></a>`)}</div>
    ${d.items.length ? '' : html`<div class="empty">Kein neues Feedback.</div>`}`);
}

async function reports(box) {
  const d = await api('GET', '/api/admin/reports?status=open');
  mount(box, d.reports.length ? html`<div class="card table-wrap"><table><thead><tr><th>Datum</th><th>Gemeldet</th><th>Von</th><th>Grund</th><th>Details</th><th></th></tr></thead><tbody>
    ${d.reports.map((r) => html`<tr><td>${fmtDate(r.created_at)}</td><td><a href="/dev/users/${r.target_id}" data-link>${r.target_name}</a></td><td>${r.reporter_name}</td><td>${r.reason}</td><td>${r.details}</td>
      <td><button class="btn small" data-r="${r.id}" data-s="actioned">Erledigt</button><button class="btn small" data-r="${r.id}" data-s="dismissed">Verwerfen</button></td></tr>`)}</tbody></table></div>` : html`<div class="empty">Keine offenen Meldungen.</div>`);
  box.onclick = async (e) => {
    const b = e.target.closest('button[data-r]');
    if (!b) return;
    try { await api('PATCH', `/api/admin/reports/${b.dataset.r}`, { status: b.dataset.s }); await reports(box); } catch (err) { errorToast(err); }
  };
}

async function news(box) {
  const d = await api('GET', '/api/public/news');
  mount(box, html`<form class="card stack" id="n"><h2>Beitrag veröffentlichen</h2>
    <select name="kind"><option value="news">News</option><option value="changelog">Changelog</option><option value="maintenance">Wartung</option></select>
    <input name="title" minlength="3" maxlength="120" placeholder="Titel" required><textarea name="body" maxlength="10000" placeholder="Text" required></textarea><button class="btn primary">Veröffentlichen</button></form>
    <div class="stack spaced">${d.news.map((n) => html`<div class="card row"><span class="badge">${n.kind}</span><b>${n.title}</b><span class="spacer"></span><span class="small muted">${fmtDate(n.created_at)}</span><button class="btn small danger" data-del="${n.id}">Zurückziehen</button></div>`)}</div>`);
  box.querySelector('#n').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    try { await api('POST', '/api/admin/news', { kind: f.get('kind'), title: f.get('title'), body: f.get('body') }); toast('Veröffentlicht', 'ok'); await news(box); } catch (err) { errorToast(err); }
  });
  box.onclick = async (e) => {
    const b = e.target.closest('button[data-del]');
    if (!b || !(await confirmDialog('Zurückziehen?', 'Der Beitrag wird nicht mehr angezeigt.', { danger: true }))) return;
    try { await api('DELETE', `/api/admin/news/${b.dataset.del}`); await news(box); } catch (err) { errorToast(err); }
  };
}

async function promo(box) {
  const d = await api('GET', '/api/admin/promo');
  mount(box, html`<form class="card row" id="p"><input name="code" placeholder="CODE" pattern="[A-Za-z0-9-]{3,32}" required><input name="credits" type="number" min="1" max="100000" placeholder="Credits" required><input name="maxUses" type="number" min="1" placeholder="Max. Einlösungen" required><input name="days" type="number" min="1" max="366" placeholder="Gültig (Tage, optional)"><button class="btn primary">Erstellen</button></form>
    <div class="card table-wrap spaced"><table><thead><tr><th>Code</th><th>Credits</th><th>Genutzt</th><th>Läuft ab</th></tr></thead><tbody>${d.codes.map((c) => html`<tr><td class="mono">${c.code}</td><td>${c.credits}</td><td>${c.uses} / ${c.max_uses}</td><td>${c.expires_at ? fmtDate(c.expires_at) : '–'}</td></tr>`)}</tbody></table></div>`);
  box.querySelector('#p').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const days = Number(f.get('days'));
    try {
      await api('POST', '/api/admin/promo', { code: f.get('code'), credits: Number(f.get('credits')), maxUses: Number(f.get('maxUses')), expiresAt: days ? Date.now() + days * 86_400_000 : null });
      toast('Code erstellt', 'ok');
      await promo(box);
    } catch (err) { errorToast(err); }
  });
}

async function audit(box) {
  const d = await api('GET', '/api/admin/audit');
  mount(box, html`<div class="card table-wrap"><table><thead><tr><th>Zeit</th><th>Wer</th><th>Aktion</th><th>Ziel</th><th>Details</th><th>IP</th></tr></thead><tbody>
    ${d.entries.map((a) => html`<tr><td>${fmtDate(a.created_at)}</td><td>${a.actor_name ?? 'System'}</td><td class="mono">${a.action}</td><td>${a.target_id ? html`<a href="/dev/users/${a.target_id}" data-link>${a.target_name ?? a.target_id}</a>` : '–'}</td><td class="small mono">${JSON.stringify(a.details)}</td><td class="small muted">${a.ip ?? ''}</td></tr>`)}
  </tbody></table></div>`);
}

async function system(box) {
  const o = await api('GET', '/api/admin/overview');
  mount(box, html`<div class="grid cols-2">
    <div class="card stack"><h2>Backup</h2><p class="muted small">Erstellt eine konsistente Kopie der Datenbank im Datenverzeichnis des Servers.</p><button class="btn" id="backup">Backup erstellen</button></div>
    <div class="card stack"><h2>Wartungsmodus</h2><p class="muted small">Sperrt alle Endpunkte außer den öffentlichen für Nutzer ohne Rolle.</p><button class="btn ${o.maintenance ? 'primary' : 'danger'}" id="maint">${o.maintenance ? 'Wartung beenden' : 'Wartung starten'}</button></div></div>`);
  box.querySelector('#backup').addEventListener('click', async () => { try { const r = await api('POST', '/api/admin/backup'); toast(`Backup: ${r.file}`, 'ok'); } catch (e) { errorToast(e); } });
  box.querySelector('#maint').addEventListener('click', async () => {
    if (!(await confirmDialog('Wartungsmodus ändern?', o.maintenance ? 'Dienste wieder freigeben.' : 'Spieler können den Dienst dann nicht nutzen.', { danger: !o.maintenance }))) return;
    try { await api('PUT', '/api/admin/maintenance', { enabled: !o.maintenance }); await system(box); } catch (e) { errorToast(e); }
  });
}

