// VR mode page: checks for a headset, explains requirements and starts the
// immersive lobby (or the desktop preview when no headset is available).

import { state } from '../app.ts';
import { html, mount } from '../ui.ts';
import { icon } from '../icons.ts';
import { enterVr, vrRequirements, xrSupported } from '../vr/lobby.ts';

export async function vrPage(el: HTMLElement): Promise<void> {
  mount(el, html`<div class="vr-page">
    <div class="vr-hero card">
      <div class="vr-art" aria-hidden="true"><div class="vr-glasses">${icon('vr')}</div></div>
      <div class="vr-copy">
        <h1>VR-Modus</h1>
        <p>Setz die Brille auf und steh mitten in der Halloween-Lobby: Kürbisse, Nebel, ein summendes Portal – und vor dir schweben die Click GUI, ein echter Browser mit Google-Suche und eine Tastatur. Alles lässt sich greifen, verschieben und vergrößern.</p>
        <div class="vr-status" id="vrStatus"><span class="spinner"></span> Suche VR-Brille …</div>
        <div class="row gap"><button class="btn primary" id="vrStart" disabled>${icon('vr', 'sm')} VR starten</button><button class="btn" id="vrPreview">${icon('play', 'sm')} Desktop-Vorschau</button></div>
      </div>
    </div>
    <div class="grid g3 gap">
      <div class="card"><h3>${icon('sliders', 'sm')} Click GUI im Raum</h3><p class="small muted">Schalte die Module deines LEGO-Client-Profils, Launcher-Effekte und die Lobby direkt in VR. Änderungen an Modulen gelten ab dem nächsten Spielstart.</p></div>
      <div class="card"><h3>${icon('globe', 'sm')} Browser</h3><p class="small muted">Echter Chromium-Browser mit Google-Suche, Zurück/Vor, Scrollen und Tippen. SafeSearch ist immer an, Downloads sind gesperrt, nur sichere https-Seiten.</p></div>
      <div class="card"><h3>${icon('shield', 'sm')} Komfort</h3><p class="small muted">Keine erzwungenen Kamerafahrten nach dem Einstieg, Ansicht jederzeit zurücksetzen, „VR verlassen“ immer erreichbar (oder Esc / Brille absetzen).</p></div>
    </div>
    <div class="card gap"><h3>Voraussetzungen</h3><ul class="small">${vrRequirements().map((r) => html`<li>${r}</li>`)}</ul></div>
  </div>`);
  const status = el.querySelector<HTMLElement>('#vrStatus')!;
  const start = el.querySelector<HTMLButtonElement>('#vrStart')!;
  const ok = await xrSupported();
  if (!el.isConnected) return;
  mount(status, ok ? html`${icon('check', 'sm')} VR-Brille bereit` : html`${icon('info', 'sm')} Keine VR-Brille gefunden – die Desktop-Vorschau zeigt dieselbe Welt.`);
  status.classList.toggle('ok', ok);
  start.disabled = !ok;
  const run = (xr: boolean) => void enterVr({ xr, sound: state.settings.introSound, quality: state.settings.backgroundQuality });
  start.addEventListener('click', () => run(true));
  el.querySelector('#vrPreview')!.addEventListener('click', () => run(false));
}
