// Crash doctor dialog: what happened, in plain words, with one-click fixes
// (and an undo for each applied fix).

import { api, dialog, errorToast, html, mount, toast } from './ui.ts';
import { icon } from './icons.ts';
import type { Diagnosis, Fix } from '../core/crashdoc.ts';

type Undo = Fix | { kind: 'enable-mod'; filename: string } | null;

type Learned = Diagnosis & { learned?: { ok: number; fail: number } };

const item = (x: Learned, i: number) => html`<div class="cd-item" data-i="${i}">
      <div class="cd-head"><b>${x.title}</b><span class="badge">${x.confidence === 'high' ? 'sicher' : 'vermutlich'}</span>${x.learned ? html`<span class="badge ${x.learned.ok > x.learned.fail ? 'ok' : ''}">${x.learned.ok > 0 ? `hat bei dir ${x.learned.ok}× geholfen` : ''}${x.learned.ok > 0 && x.learned.fail > 0 ? ' · ' : ''}${x.learned.fail > 0 ? `${x.learned.fail}× nicht geholfen` : ''}</span>` : ''}</div>
      <p class="small">${x.detail}</p>
      <div class="row gap">${x.fix ? html`<button class="btn sm primary" data-fix="${i}">${icon('wrench', 'xs')} ${x.fixLabel ?? 'Automatisch reparieren'}</button>` : html`<span class="small muted">Das kann der Launcher nicht selbst ändern.</span>`}<span class="cd-done small" hidden></span></div>
    </div>`;

export function showCrashDoctor(profileId: string, diagnoses: Diagnosis[], code?: number): void {
  const d = dialog(html`<div class="crashdoc">
    <h2>${icon('wrench', 'sm')} Crash-Doktor</h2>
    <p class="muted small">${code !== undefined ? `Minecraft wurde mit Code ${code} beendet. ` : ''}${diagnoses.length ? 'Das habe ich im Log gefunden:' : 'Ich habe im Log keine bekannte Ursache gefunden. Schau in die Konsole (Fehler sind rot markiert) oder schick den Log im Feedback.'}</p>
    <div class="cd-list">${diagnoses.map((x, i) => item(x, i))}</div>
    <p class="small muted">Der Crash-Doktor lernt mit: Hilft eine Lösung, steht sie beim nächsten gleichen Absturz ganz oben. Alles bleibt auf deinem PC.</p>
    <div class="row between"><button class="btn sm" id="cdLogs">${icon('folder', 'sm')} Log-Ordner</button><button class="btn" id="cdClose">Schließen</button></div>
  </div>`);
  d.querySelector('#cdClose')!.addEventListener('click', () => d.close());
  d.querySelector('#cdLogs')!.addEventListener('click', () => void api.openFolder('logs'));
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
        const r = await api.crashFix(profileId, diagnoses[i]!.fix!, { title: diagnoses[i]!.title, fixLabel: diagnoses[i]!.fixLabel });
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
