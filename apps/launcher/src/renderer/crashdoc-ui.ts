// Crash doctor dialog: what happened, in plain words, with one-click fixes
// (and an undo for each applied fix).

import { api, dialog, errorToast, html, mount, toast } from './ui.ts';

/** Set by app.ts: opens Options on the AI section. */
export let openAiSettings: () => void = () => {};
export function setOpenAiSettings(fn: () => void): void { openAiSettings = fn; }
import { icon } from './icons.ts';
import type { Diagnosis, Fix } from '../core/crashdoc.ts';

type Undo = Fix | { kind: 'enable-mod'; filename: string } | null;

const item = (x: Diagnosis, i: number, ai: boolean) => html`<div class="cd-item" data-i="${i}">
      <div class="cd-head"><b>${x.title}</b><span class="badge">${ai ? 'KI · ' : ''}${x.confidence === 'high' ? 'sicher' : 'vermutlich'}</span></div>
      <p class="small">${x.detail}</p>
      <div class="row gap">${x.fix ? html`<button class="btn sm primary" data-fix="${i}">${icon('wrench', 'xs')} ${x.fixLabel ?? 'Automatisch reparieren'}</button>` : html`<span class="small muted">Das kann der Launcher nicht selbst ändern.</span>`}<span class="cd-done small" hidden></span></div>
    </div>`;

export function showCrashDoctor(profileId: string, diagnoses: Diagnosis[], code?: number): void {
  const d = dialog(html`<div class="crashdoc">
    <h2>${icon('wrench', 'sm')} Crash-Doktor</h2>
    <p class="muted small">${code !== undefined ? `Minecraft wurde mit Code ${code} beendet. ` : ''}${diagnoses.length ? 'Das habe ich im Log gefunden:' : 'Ich habe im Log keine bekannte Ursache gefunden. Schau in die Konsole (Fehler sind rot markiert) oder schick den Log im Feedback.'}</p>
    <div class="cd-list">${diagnoses.map((x, i) => item(x, i, false))}</div>
    <div class="cd-ai" id="cdAi"><p class="small muted">Nicht gefunden oder unsicher? Die KI (Claude) liest den Absturzbericht und schlägt eine Lösung vor. Dafür wird ein bereinigter Auszug des Logs (ohne Benutzernamen, Tokens, E-Mails und IP-Adressen) und deine Mod-Liste an die KI geschickt: mit deinem eigenen Schlüssel direkt, sonst über den LEGO-Server. Gespeichert wird nichts.</p>
      <button class="btn sm primary" id="cdAiGo">${icon('sparkle', 'xs')} Mit KI prüfen</button></div>
    <div class="row between"><button class="btn sm" id="cdLogs">${icon('folder', 'sm')} Log-Ordner</button><button class="btn" id="cdClose">Schließen</button></div>
  </div>`);
  d.querySelector('#cdClose')!.addEventListener('click', () => d.close());
  d.querySelector('#cdLogs')!.addEventListener('click', () => void api.openFolder('logs'));
  // AI check: with the player's own key directly at Claude, otherwise via the LEGO server.
  const aiBox = d.querySelector<HTMLElement>('#cdAi')!;
  d.querySelector('#cdAiGo')!.addEventListener('click', async (e) => {
    const b = e.currentTarget as HTMLButtonElement;
    b.disabled = true;
    b.textContent = 'KI liest den Absturz …';
    try {
      const r = await api.crashAi(profileId);
      const start = diagnoses.length;
      r.causes.forEach((c, k) => diagnoses.push({ ...c, id: `ai-${k}` }));
      mount(aiBox, html`<p><b>${icon('sparkle', 'xs')} KI:</b> ${r.summary || 'Keine eindeutige Ursache gefunden.'}</p>
        <div class="cd-list">${r.causes.map((c, k) => item({ ...c, id: `ai-${k}` }, start + k, true))}</div>
        <p class="small muted">${r.via === 'own' ? 'Mit deinem eigenen Claude-Schlüssel geprüft.' : `Noch ${r.left} KI-Prüfungen heute.`}</p>`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      const noAi = /NO_AI|503|unavailable/i.test(msg);
      mount(aiBox, html`<p class="small">${noAi ? 'Für die KI-Prüfung brauchst du einen Claude-API-Schlüssel (console.anthropic.com). Trag ihn unter Optionen → KI ein, dann geht es sofort.' : /401|anmeld|login|unauth/i.test(msg) ? 'Für die KI-Prüfung über den LEGO-Server musst du angemeldet sein, oder du trägst unter Optionen → KI einen eigenen Schlüssel ein.' : `KI-Prüfung fehlgeschlagen: ${msg.replace(/^Error invoking remote method '[^']+': (Error: )?/, '')}`}</p>
        ${noAi ? html`<button class="btn sm" id="cdAiSet">${icon('settings', 'xs')} Zu Optionen → KI</button>` : ''}`);
      aiBox.querySelector('#cdAiSet')?.addEventListener('click', () => { d.close(); openAiSettings(); });
    }
  });
  const undos = new Map<number, Undo>();
  d.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-fix],[data-undo]');
    if (!b) return;
    const i = Number(b.dataset.fix ?? b.dataset.undo);
    const item = d.querySelector<HTMLElement>(`.cd-item[data-i="${i}"]`)!;
    const done = item.querySelector<HTMLElement>('.cd-done')!;
    b.disabled = true;
    try {
      if (b.dataset.fix !== undefined) {
        const r = await api.crashFix(profileId, diagnoses[i]!.fix!);
        undos.set(i, r.undo);
        done.hidden = false;
        mount(done, html`${icon('check', 'xs')} ${r.message}${r.undo ? html` · <button class="link" data-undo="${i}">Rückgängig</button>` : ''}`);
        b.hidden = true;
        toast(r.message, 'ok');
      } else {
        const u = undos.get(i);
        if (u) { const r = await api.crashFix(profileId, u); toast(r.message, 'ok'); }
        done.hidden = true;
        const fixBtn = item.querySelector<HTMLButtonElement>('[data-fix]');
        if (fixBtn) { fixBtn.hidden = false; fixBtn.disabled = false; }
      }
    } catch (err) { errorToast(err); b.disabled = false; }
  });
}
