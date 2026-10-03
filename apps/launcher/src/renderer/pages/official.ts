// "Play with the official Minecraft Launcher": installs the profile (Fabric,
// Fabric API, LEGO client, FPS pack) as an installation in the official
// launcher, which then signs in with its own Microsoft app.

import { api, dialog, errorToast, html, mount } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { state } from '../app.ts';

/** Renders a pixel icon to a 128 px PNG data URL (profile icon in the official launcher). */
function pixelPng(name: string): Promise<string | null> {
  return new Promise((resolve) => {
    const svg = pixel(name).__raw.replace('<svg ', '<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" ');
    const img = new Image();
    img.onload = () => {
      const c = document.createElement('canvas');
      c.width = c.height = 128;
      const ctx = c.getContext('2d')!;
      ctx.imageSmoothingEnabled = false;
      ctx.drawImage(img, 0, 0, 128, 128);
      resolve(c.toDataURL('image/png'));
    };
    img.onerror = () => resolve(null);
    img.src = `data:image/svg+xml;base64,${btoa(svg)}`;
  });
}

export async function officialFlow(profileId?: string): Promise<void> {
  const p = state.settings.profiles.find((x) => x.id === (profileId ?? state.settings.selectedProfile)) ?? state.settings.profiles[0]!;
  if (p.loaderType === 'forge' || p.loaderType === 'neoforge') {
    errorToast(new Error('Forge/NeoForge-Profile bitte direkt im LEGO Launcher spielen oder mit dem Forge-Installer einrichten.'));
    return;
  }
  const st = await api.officialStatus();
  if (!st.present) {
    const d = dialog(html`<h2>Minecraft Launcher nicht gefunden</h2>
      <p>Installiere zuerst den offiziellen Minecraft Launcher und starte ihn einmal. Danach kann der LEGO Launcher dort ein Profil „${p.name}“ anlegen.</p>
      <div class="row between gap"><button class="btn" id="close">Schließen</button><button class="btn primary" id="dl">${icon('external', 'sm')} minecraft.net öffnen</button></div>`);
    d.querySelector('#close')!.addEventListener('click', () => d.close());
    d.querySelector('#dl')!.addEventListener('click', () => void api.openExternal('https://www.minecraft.net/download'));
    return;
  }
  const d = dialog(html`<h2>${pixel(p.icon, 'sm')} ${p.name} einrichten</h2><p class="muted">Fabric, Fabric API${p.legoClient ? ', LEGO Client' : ''}${p.performancePack ? ' und FPS-Paket' : ''} werden vorbereitet …</p><div class="progress indet"><i data-p="100"></i></div><p class="small muted gap" id="step"></p>`);
  const off = api.on('launch-progress', (x: { step: string }) => { const s = d.querySelector('#step'); if (s) s.textContent = x.step; });
  try {
    const r = await api.exportToOfficial(p.id, await pixelPng(p.icon));
    mount(d, html`<h2>${icon('check', 'sm')} Fertig!</h2>
      <p>Im offiziellen Minecraft Launcher gibt es jetzt die Installation <b>„${r.name}“</b>.</p>
      <ol class="small"><li>Minecraft Launcher öffnen (anmelden wie immer).</li><li>Oben „Minecraft: Java Edition“ → links unten neben „Spielen“ die Installation <b>${r.name}</b> wählen.</li><li><b>Spielen</b> klicken.</li></ol>
      <p class="small muted">Mods, Einstellungen und Welten liegen im Ordner dieses LEGO-Profils – du kannst weiter beide Launcher benutzen.</p>
      <form method="dialog" class="row gap"><button class="btn primary">OK</button></form>`);
  } catch (e) {
    d.close();
    errorToast(e);
  } finally {
    off();
  }
}
