import { state } from '../app.ts';
import { api, errorToast, fmtDate, html, lego, mount, toast } from '../ui.ts';
import type { LaunchProgress } from '../../ipc-types.ts';
import { signInFlow } from './account.ts';

let unsub: (() => void)[] = [];
const logLines: { line: string; stream: string }[] = [];

export async function playPage(el: HTMLElement): Promise<void> {
  unsub.forEach((u) => u());
  unsub = [];
  const s = state.settings;
  const profile = s.profiles.find((p) => p.id === s.selectedProfile) ?? s.profiles[0]!;
  const running = await api.gameRunning();
  mount(el, html`<div class="play">
    <section class="stage">
      <div class="floor"></div>
      <div class="nameplate">${state.auth.name ?? 'Nicht angemeldet'}</div>
      <div class="avatar3d" aria-hidden="true"></div>
      ${state.auth.signedIn ? html`
        <div class="launch">
          <button class="launch-main" id="launch" ${running ? 'disabled' : ''}><b>${running ? 'LÄUFT' : 'LAUNCH'}</b><span>${profile.legoClient ? 'LEGO Client · ' : ''}${profile.loader || profile.legoClient ? 'Fabric ' : ''}${profile.gameVersion}</span></button>
          <select id="profile" aria-label="Profil">${s.profiles.map((p) => html`<option value="${p.id}" ${p.id === profile.id ? 'selected' : ''}>${p.name}</option>`)}</select>
        </div>
        <div class="launch-status" id="status"></div>`
      : html`<button class="btn primary" id="signin">Mit Microsoft anmelden</button>
        ${state.info.msClientConfigured ? '' : html`<p class="small muted">Dieser Build hat keine Microsoft-Client-ID. Siehe docs/LAUNCHER.md.</p>`}`}
    </section>
    <aside class="news-col">
      <div class="row between"><h2>News</h2><button class="btn sm" id="console">Konsole</button></div>
      <div id="news"><div class="muted small">Lädt…</div></div>
    </aside>
  </div>
  <div class="card gap" id="consoleCard" hidden><div class="row between"><h3>Spiel-Log</h3><div class="row"><button class="btn sm" id="openLogs">Log-Ordner</button><button class="btn sm" id="copyLog">Kopieren</button></div></div><div class="console" id="log"></div></div>`);

  el.querySelector('#signin')?.addEventListener('click', () => void signInFlow());
  el.querySelector('#profile')?.addEventListener('change', async (e) => {
    state.settings = await api.setSettings({ selectedProfile: (e.target as HTMLSelectElement).value });
    void playPage(el);
  });
  const statusBox = el.querySelector<HTMLElement>('#status');
  const logBox = el.querySelector<HTMLElement>('#log')!;
  const drawLog = () => {
    logBox.replaceChildren(...logLines.slice(-400).map((l) => {
      const d = document.createElement('div');
      if (l.stream === 'err') d.className = 'err';
      d.textContent = l.line;
      return d;
    }));
    logBox.scrollTop = logBox.scrollHeight;
  };
  drawLog();
  el.querySelector('#console')!.addEventListener('click', () => { const c = el.querySelector<HTMLElement>('#consoleCard')!; c.hidden = !c.hidden; });
  el.querySelector('#openLogs')!.addEventListener('click', () => void api.openFolder('logs'));
  el.querySelector('#copyLog')!.addEventListener('click', () => void navigator.clipboard.writeText(logLines.map((l) => l.line).join('\n')).then(() => toast('Log kopiert', 'ok')));

  unsub.push(api.on('launch-progress', (p: LaunchProgress) => {
    if (!statusBox) return;
    const pct = p.total ? Math.round(((p.done ?? 0) / p.total) * 100) : null;
    mount(statusBox, html`<div class="row between small"><span>${p.step}</span><span class="muted">${pct !== null ? `${p.done}/${p.total}` : ''}${p.totalBytes ? ` · ${Math.round((p.bytes ?? 0) / 1048576)} / ${Math.round(p.totalBytes / 1048576)} MB` : ''}</span></div>
      <div class="progress"><i data-p="${pct ?? 100}"></i></div>${pct !== null && pct < 100 ? html`<button class="btn sm gap" id="cancel">Abbrechen</button>` : ''}`);
    statusBox.querySelector('#cancel')?.addEventListener('click', () => api.cancelLaunch());
  }));
  unsub.push(api.on('game-log', (l: { line: string; stream: string }) => {
    logLines.push(l);
    if (logLines.length > 5000) logLines.splice(0, 1000);
    if (!el.querySelector<HTMLElement>('#consoleCard')!.hidden) drawLog();
  }));
  unsub.push(api.on('game-exit', (r: { code: number; hint: string | null; crashReport: string | null }) => {
    if (r.code === 0) toast('Minecraft wurde beendet.');
    else toast(`Minecraft ist abgestürzt (Code ${r.code}). ${r.hint ?? 'Details im Log.'}`, 'error');
    if (state.page === 'play') void playPage(el);
  }));

  el.querySelector('#launch')?.addEventListener('click', async () => {
    const btn = el.querySelector<HTMLButtonElement>('#launch')!;
    btn.disabled = true;
    logLines.length = 0;
    const r = await api.launch(profile.id);
    if (!r.ok) {
      errorToast(new Error(r.error ?? 'Start fehlgeschlagen'));
      btn.disabled = false;
      if (statusBox) statusBox.textContent = '';
    } else {
      mount(btn, html`<b>LÄUFT</b><span>Viel Spaß!</span>`);
    }
  });

  const newsBox = el.querySelector('#news')!;
  try {
    const n = await lego<{ news: { id: number; title: string; body: string; created_at: number; kind: string }[] }>('GET', '/api/public/news');
    mount(newsBox, n.news.length ? html`${n.news.slice(0, 8).map((x) => html`<div class="news-item"><div class="small muted">${fmtDate(x.created_at)} · ${x.kind}</div><h3>${x.title}</h3><p class="small">${x.body.slice(0, 180)}${x.body.length > 180 ? '…' : ''}</p></div>`)}` : html`<div class="empty small">Keine News.</div>`);
  } catch {
    mount(newsBox, html`<div class="empty small">News nicht verfügbar (offline).</div>`);
  }
}
