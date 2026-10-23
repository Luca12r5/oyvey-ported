// Crash doctor dialog: what happened, in plain words, with one-click fixes
// (and an undo for each applied fix).

import { api, dialog, errorToast, html, lego, mount, toast } from './ui.ts';
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
    <div class="cd-ai" id="cdAi"><p class="small muted">Nicht gefunden oder unsicher? Die KI liest den Absturzbericht und schlägt eine Lösung vor. Dafür wird ein bereinigter Auszug des Logs (ohne Benutzernamen, Tokens und IP-Adressen) und deine Mod-Liste an den LEGO-Server geschickt; gespeichert wird nichts.</p>
      <button class="btn sm primary" id="cdAiGo">${icon('sparkle', 'xs')} Mit KI prüfen</button></div>
    <div class="row between"><button class="btn sm" id="cdLogs">${icon('folder', 'sm')} Log-Ordner</button><button class="btn" id="cdClose">Schließen</button></div>
  </div>`);
  d.querySelector('#cdClose')!.addEventListener('click', () => d.close());
  d.querySelector('#cdLogs')!.addEventListener('click', () => void api.openFolder('logs'));
  // AI check through the LEGO server (needs a LEGO login; the key stays on the server).
  const aiBox = d.querySelector<HTMLElement>('#cdAi')!;
  d.querySelector('#cdAiGo')!.addEventListener('click', async (e) => {
    const b = e.currentTarget as HTMLButtonElement;
    b.disabled = true;
    b.textContent = 'KI liest den Absturz …';
    try {
      const x = await api.crashExcerpt(profileId);
      const r = await lego<{ summary: string; causes: Omit<Diagnosis, 'id'>[]; left: number }>('POST', '/api/crash/ai', x);
      const start = diagnoses.length;
      r.causes.forEach((c, k) => diagnoses.push({ ...c, id: `ai-${k}` }));
      mount(aiBox, html`<p><b>${icon('sparkle', 'xs')} KI:</b> ${r.summary || 'Keine eindeutige Ursache gefunden.'}</p>
        <div class="cd-list">${r.causes.map((c, k) => item({ ...c, id: `ai-${k}` }, start + k, true))}</div>
        <p class="small muted">Noch ${r.left} KI-Prüfungen heute.</p>`);
    } catch (err) {
      const msg = err instanceof Error ? err.message : String(err);
      mount(aiBox, html`<p class="small">${/401|anmeld|login|unauth/i.test(msg) ? 'Für die KI-Prüfung musst du mit deinem Konto angemeldet sein.' : /503|unavailable/i.test(msg) ? 'Die KI ist auf diesem Server noch nicht eingerichtet (ANTHROPIC_API_KEY fehlt). Die Prüfung oben funktioniert trotzdem offline.' : `KI-Prüfung fehlgeschlagen: ${msg}`}</p>`);
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
