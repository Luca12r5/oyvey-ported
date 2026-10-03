import { FEEDBACK_KINDS } from '@lego/shared';
import { state } from '../app.ts';
import { api, dialog, errorToast, fmtDate, html, lego, mount, toast } from '../ui.ts';

export async function newsPage(el: HTMLElement): Promise<void> {
  let news: { title: string; body: string; kind: string; created_at: number }[] = [];
  try { news = (await lego<{ news: typeof news }>('GET', '/api/public/news')).news; } catch { /* offline */ }
  mount(el, html`<h1>News & Changelog</h1>${news.length ? html`<div class="grid g2">${news.map((n) => html`<article class="card"><div class="small muted">${fmtDate(n.created_at)} · ${n.kind}</div><h2>${n.title}</h2><p>${n.body}</p></article>`)}</div>` : html`<div class="empty">Keine News verfügbar.</div>`}`);
}

const KIND_LABEL: Record<string, string> = { bug: 'Fehler', suggestion: 'Vorschlag', minigame: 'Minispiel-Idee', cosmetic: 'Cosmetic-Idee', theme: 'Design-Idee', performance: 'Performance', support: 'Support' };

export async function feedbackPage(el: HTMLElement): Promise<void> {
  const mine = await lego<{ items: { id: number; kind: string; title: string; status: string; votes: number; created_at: number }[] }>('GET', '/api/feedback?mine=1');
  mount(el, html`<h1>Feedback & Support</h1><div class="grid g2">
    <form class="card" id="f"><h2>Neuer Beitrag</h2>
      <label>Art</label><select name="kind">${FEEDBACK_KINDS.map((k) => html`<option value="${k}">${KIND_LABEL[k]}</option>`)}</select>
      <label>Titel</label><input name="title" minlength="4" maxlength="120" required>
      <label>Beschreibung</label><textarea name="body" minlength="10" maxlength="5000" required></textarea>
      <label class="check gap"><input type="checkbox" name="diag"> Diagnose-Infos anhängen (System, Launcher-Version, Profile – keine Tokens)</label>
      <button class="btn primary gap">Senden</button></form>
    <div class="card"><h2>Meine Beiträge</h2>${mine.items.length ? html`<table><tbody>${mine.items.map((i) => html`<tr><td>${KIND_LABEL[i.kind]}</td><td>${i.title}</td><td><span class="badge">${i.status}</span></td><td class="small muted">${fmtDate(i.created_at)}</td></tr>`)}</tbody></table>` : html`<div class="empty">Noch keine Beiträge.</div>`}</div></div>`);
  el.querySelector('#f')!.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target as HTMLFormElement);
    const attachments = fd.get('diag') === 'on' ? [{ filename: 'diagnostics.txt', data: btoa(unescape(encodeURIComponent(await api.diagnostics()))) }] : [];
    try {
      await lego('POST', '/api/feedback', { kind: fd.get('kind'), title: fd.get('title'), body: fd.get('body'), attachments });
      toast('Danke! Beitrag gesendet.', 'ok');
      void feedbackPage(el);
    } catch (err) { errorToast(err); }
  });
}

export async function devPage(el: HTMLElement): Promise<void> {
  const settings = state.settings;
  mount(el, html`<h1>Entwickler-Dashboard</h1><div class="card">
    <p>Das Dashboard (Spielersuche, Coins vergeben/abziehen, Rollen, Sperren, Feedback, Meldungen, News, Aktionscodes, Audit-Log, Backups) läuft auf der LEGO-Website, damit jede Aktion zentral protokolliert wird.</p>
    <p class="small muted">Rollen: ${state.auth.lego.roles.join(', ')}</p>
    <button class="btn primary" id="open">Anmeldecode erzeugen & Dashboard öffnen</button></div>`);
  el.querySelector('#open')!.addEventListener('click', async () => {
    try {
      const r = await lego<{ code: string }>('POST', '/api/auth/web-code');
      const base = (settings.backendUrl && !settings.backendUrl.includes('.example')) ? settings.backendUrl : '';
      dialog(html`<h2>Code für die Website</h2><div class="code-big">${r.code}</div><p class="small gap">Die Anmeldeseite öffnet sich im Browser. Code einfügen, danach „Dashboard“ wählen.</p><form method="dialog"><button class="btn">Schließen</button></form>`);
      await navigator.clipboard.writeText(r.code);
      if (base) void api.openExternal(`${base.replace(/\/$/, '')}/login`);
    } catch (e) { errorToast(e); }
  });
}
