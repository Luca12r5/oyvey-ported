import { api, errorToast, fmtDate, hasPerm, html, mount, state, toast } from '../core.js';
import { navigate } from '../app.js';

const KINDS = { bug: 'Fehler', suggestion: 'Vorschlag', minigame: 'Minispiel-Idee', cosmetic: 'Cosmetic-Idee', theme: 'Design-Idee', performance: 'Performance', support: 'Support' };
const STATUS = { new: 'Neu', reviewing: 'Wird geprüft', planned: 'Geplant', in_progress: 'In Arbeit', done: 'Umgesetzt', rejected: 'Abgelehnt' };
const STATUS_COLOR = { new: '#9aa0a6', reviewing: '#3d8bff', planned: '#a855f7', in_progress: '#f5b100', done: '#3fbf5f', rejected: '#ef4444' };

export async function board(el) {
  const params = new URLSearchParams(location.search);
  const q = new URLSearchParams();
  for (const k of ['kind', 'status', 'mine', 'sort']) if (params.get(k)) q.set(k, params.get(k));
  const data = await api('GET', `/api/feedback?${q}`);
  mount(el, html`<div class="row between"><h1>Feedback & Ideen</h1><a class="btn primary" href="/feedback/new" data-link>Neuer Beitrag</a></div>
    <p class="muted">Vorschläge sind für alle sichtbar und können hochgevotet werden. Fehlerberichte und Support-Tickets sieht nur das Team.</p>
    <form class="card row" id="filters">
      <select name="kind"><option value="">Alle Arten</option>${Object.entries(KINDS).map(([k, v]) => html`<option value="${k}" ${params.get('kind') === k ? 'selected' : ''}>${v}</option>`)}</select>
      <select name="status"><option value="">Alle Status</option>${Object.entries(STATUS).map(([k, v]) => html`<option value="${k}" ${params.get('status') === k ? 'selected' : ''}>${v}</option>`)}</select>
      <select name="sort"><option value="">Neueste</option><option value="votes" ${params.get('sort') === 'votes' ? 'selected' : ''}>Meiste Stimmen</option></select>
      <label class="check"><input type="checkbox" name="mine" value="1" ${params.get('mine') ? 'checked' : ''}> Nur meine</label>
      <button class="btn">Filtern</button></form>
    <div class="stack spaced">${data.items.length ? data.items.map((f) => html`<a class="card row" href="/feedback/${f.id}" data-link>
      <div class="center"><div class="kpi">${f.votes}</div><div class="small muted">Stimmen</div></div>
      <div><div class="row"><span class="badge">${KINDS[f.kind]}</span><span class="badge c" data-c="${STATUS_COLOR[f.status]}">${STATUS[f.status]}</span></div><h3>${f.title}</h3><div class="small muted">${f.author} · ${fmtDate(f.created_at)}</div></div></a>`) : html`<div class="empty">Keine Beiträge.</div>`}</div>`);
  el.querySelector('#filters').addEventListener('submit', (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const out = new URLSearchParams();
    for (const [k, v] of f.entries()) if (v) out.set(k, String(v));
    navigate(`/feedback?${out}`);
  });
}

export async function create(el) {
  if (!state.user) return navigate('/login');
  const preset = new URLSearchParams(location.search).get('kind') ?? 'suggestion';
  mount(el, html`<h1>Neuer Beitrag</h1><form class="card stack" id="f">
    <div class="field"><label>Art</label><select name="kind">${Object.entries(KINDS).map(([k, v]) => html`<option value="${k}" ${k === preset ? 'selected' : ''}>${v}</option>`)}</select></div>
    <div class="field"><label>Titel</label><input name="title" minlength="4" maxlength="120" required></div>
    <div class="field"><label>Beschreibung</label><textarea name="body" minlength="10" maxlength="5000" required placeholder="Was ist passiert? Was hast du erwartet? Schritte zum Nachstellen?"></textarea></div>
    <div class="field"><label>Anhänge (optional, max. 3: PNG/JPEG bis 2 MB, Logs als .txt/.log bis 1 MB)</label><input type="file" name="files" multiple accept=".png,.jpg,.jpeg,.txt,.log"></div>
    <p class="muted small">Logs können persönliche Daten (z. B. deinen Benutzernamen oder Pfade) enthalten. Prüfe sie vor dem Hochladen.</p>
    <button class="btn primary">Absenden</button></form>`);
  el.querySelector('#f').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    const files = [...e.currentTarget.elements.files.files].slice(0, 3);
    try {
      const attachments = await Promise.all(files.map(async (file) => ({ filename: file.name, data: await toBase64(file) })));
      const r = await api('POST', '/api/feedback', { kind: f.get('kind'), title: f.get('title'), body: f.get('body'), attachments });
      toast('Danke! Dein Beitrag wurde gespeichert.', 'ok');
      navigate(`/feedback/${r.id}`);
    } catch (err) { errorToast(err); }
  });
}

function toBase64(file) {
  return new Promise((resolve, reject) => {
    const r = new FileReader();
    r.onload = () => resolve(String(r.result).split(',')[1] ?? '');
    r.onerror = () => reject(r.error);
    r.readAsDataURL(file);
  });
}

export async function detail(el, id) {
  const d = await api('GET', `/api/feedback/${id}`);
  const f = d.item;
  const staff = hasPerm('feedback.manage');
  mount(el, html`<a class="btn small" href="/feedback" data-link>← Übersicht</a>
    <div class="card stack spaced"><div class="row"><span class="badge">${KINDS[f.kind]}</span><span class="badge c" data-c="${STATUS_COLOR[f.status]}">${STATUS[f.status]}</span><span class="spacer"></span><button class="btn small" id="vote">▲ ${f.votes}</button></div>
      <h1>${f.title}</h1><div class="small muted">${f.author} · ${fmtDate(f.created_at)}</div><p class="prewrap">${f.body}</p>
      ${d.attachments.length ? html`<div class="row">${d.attachments.map((a) => html`<a class="btn small" href="/api/feedback/attachments/${a.id}">📎 ${a.filename} (${Math.ceil(a.size / 1024)} KB)</a>`)}</div>` : ''}
      ${staff ? html`<div class="row"><label for="st">Status</label><select id="st">${Object.entries(STATUS).map(([k, v]) => html`<option value="${k}" ${k === f.status ? 'selected' : ''}>${v}</option>`)}</select></div>` : ''}
    </div>
    <h2 class="spaced">Kommentare</h2>
    <div class="stack">${d.comments.map((c) => html`<div class="card"><div class="small muted">${c.author}${c.staff ? ' · Team' : ''} · ${fmtDate(c.created_at)}</div><p class="prewrap">${c.body}</p></div>`)}</div>
    <form class="card stack spaced" id="c"><textarea name="body" maxlength="2000" required placeholder="Kommentar schreiben"></textarea><button class="btn">Kommentieren</button></form>`);
  el.querySelector('#vote').addEventListener('click', async () => { try { await api('POST', `/api/feedback/${id}/vote`, { up: true }); await detail(el, id); } catch (e) { errorToast(e); } });
  el.querySelector('#st')?.addEventListener('change', async (e) => { try { await api('PATCH', `/api/feedback/${id}/status`, { status: e.target.value }); toast('Status geändert', 'ok'); } catch (err) { errorToast(err); } });
  el.querySelector('#c').addEventListener('submit', async (e) => {
    e.preventDefault();
    try { await api('POST', `/api/feedback/${id}/comments`, { body: new FormData(e.currentTarget).get('body') }); await detail(el, id); } catch (err) { errorToast(err); }
  });
}
