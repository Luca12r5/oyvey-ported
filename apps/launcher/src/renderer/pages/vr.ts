// VR page: visual VR (keyboard + mouse, any PC), real VR (headset via
// WebXR/OpenXR) and VR inside Minecraft (VR look module of the LEGO client,
// real VR through the Vivecraft mod when it exists for the version).

import { go, state } from '../app.ts';
import { api, errorToast, html, mount, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import { enterVr, vrRequirements, xrSupported } from '../vr/lobby.ts';

export async function vrPage(el: HTMLElement): Promise<void> {
  const prof = state.settings.profiles.find((p) => p.id === state.settings.selectedProfile) ?? state.settings.profiles[0];
  mount(el, html`<div class="vr-page">
    <div class="vr-modes">
      <section class="panel vr-mode">
        <div class="panel-h">${icon('vr', 'sm')}<span>Visueller VR-Modus</span></div>
        <div class="vr-mode-b">
          <p>Wie in einer VR-Brille: ein sonniges Minecraft-Tal, ein schwebendes Browser-Fenster, eine Dock-Leiste mit Uhr und Apps, Hände mit Zeigestrahl. Gesteuert mit Tastatur und Maus, läuft auf jedem PC.</p>
          <ul class="keys small"><li><kbd>W A S D</kbd> laufen · <kbd>Shift</kbd> rennen · <kbd>Leertaste</kbd> springen</li><li><kbd>Linksklick</kbd> bedienen · <kbd>Rechtsklick halten</kbd> greifen · <kbd>Mausrad</kbd> näher/weiter</li><li><kbd>E</kbd> Menü · <kbd>Esc</kbd> verlassen</li></ul>
          <button class="btn primary" id="vrPreview">${icon('play', 'sm')} Visuell starten</button>
        </div>
      </section>
      <section class="panel vr-mode">
        <div class="panel-h">${icon('vr', 'sm')}<span>Echtes VR</span><span class="vr-status small" id="vrStatus">Suche Brille …</span></div>
        <div class="vr-mode-b">
          <p>Mit Headset: Controller oder Hand-Tracking, Laufen mit dem Stick, Drehen in Schritten, Teleport mit A. Alles lässt sich greifen, mit zwei Händen vergrößern und werfen.</p>
          <ul class="small muted">${vrRequirements().slice(1).map((r) => html`<li>${r}</li>`)}</ul>
          <button class="btn" id="vrStart" disabled>${icon('vr', 'sm')} VR starten</button>
        </div>
      </section>
    </div>
    <section class="panel vr-mc">
      <div class="panel-h">${icon('cube', 'sm')}<span>VR in Minecraft</span></div>
      <div class="vr-mc-grid">
        <div>
          <h3>Visor-Brille (LEGO Client)</h3>
          <p class="small muted">Im Spiel <kbd>Rechts-Shift</kbd>: Du setzt die Visor-Brille auf und siehst deine Welt am aktuellen Ort, mit gebogenem Menü, Dock-Leiste, Händen, einem echten Browser (SafeSearch, nur https), VR-Videos und DrDonutt. Das LEGO-Menü liegt jetzt auf <kbd>Rechts-Strg</kbd>. Normale Steuerung mit Tastatur und Maus.</p>
        </div>
        <div>
          <h3>Echtes VR (Vivecraft)</h3>
          <p class="small muted" id="viveInfo">Prüfe, ob Vivecraft für ${prof?.gameVersion ?? '1.21.11'} verfügbar ist …</p>
          <button class="btn sm" id="viveBtn" hidden>${icon('plus', 'sm')} VR-Profil erstellen</button>
        </div>
      </div>
    </section>
  </div>`);
  const status = el.querySelector<HTMLElement>('#vrStatus')!;
  const start = el.querySelector<HTMLButtonElement>('#vrStart')!;
  const run = (xr: boolean) => void enterVr({ xr, sound: state.settings.introSound, quality: state.settings.backgroundQuality });
  start.addEventListener('click', () => run(true));
  el.querySelector('#vrPreview')!.addEventListener('click', () => run(false));
  void xrSupported().then((ok) => {
    if (!el.isConnected) return;
    status.textContent = ok ? 'Brille bereit' : 'keine Brille gefunden';
    status.classList.toggle('ok', ok);
    start.disabled = !ok;
    if (ok) start.classList.add('primary');
  });

  // Vivecraft (real VR in Minecraft) for the selected profile's version.
  const info = el.querySelector<HTMLElement>('#viveInfo')!;
  const btn = el.querySelector<HTMLButtonElement>('#viveBtn')!;
  try {
    const gv = prof?.gameVersion ?? '1.21.11';
    const r = await api.vivecraft(gv);
    if (!el.isConnected) return;
    if (r.available) {
      info.textContent = `Vivecraft gibt es für Minecraft ${r.gameVersion} (${r.loader}). Der Launcher legt ein eigenes VR-Profil an. Zum Spielen brauchst du SteamVR oder Meta Quest Link.`;
      btn.hidden = false;
      btn.addEventListener('click', async () => {
        btn.disabled = true;
        try {
          const p = await api.vivecraftProfile(r.gameVersion, r.loader!);
          state.settings = await api.getSettings();
          toast('VR-Profil erstellt', 'ok');
          state.settings = await api.setSettings({ selectedProfile: p.profileId });
          void go('play');
        } catch (e) { errorToast(e); btn.disabled = false; }
      });
    } else {
      info.textContent = `Vivecraft gibt es (noch) nicht für Minecraft ${r.gameVersion}.${r.suggestion ? ` Neueste unterstützte Version: ${r.suggestion} – leg dafür ein Profil an, dann kann der Launcher es einrichten.` : ''} Bis dahin: die Visor-Brille im LEGO Client (Rechts-Shift).`;
    }
  } catch {
    info.textContent = 'Konnte Modrinth nicht erreichen (offline?).';
  }
}
