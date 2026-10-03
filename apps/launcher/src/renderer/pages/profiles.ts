import { state } from '../app.ts';
import { api, confirm, errorToast, fmtDate, html, mount, toast } from '../ui.ts';
import type { GameProfile } from '../../core/settings.ts';

export async function profilesPage(el: HTMLElement): Promise<void> {
  const s = state.settings;
  mount(el, html`<div class="row between"><h1>Profile</h1><button class="btn primary" id="new">Neues Profil</button></div>
    <p class="muted">Jedes Profil hat einen eigenen Spielordner (Welten, Mods, Einstellungen). Der LEGO Client wird mit Fabric API automatisch installiert und per SHA-512 geprüft.</p>
    <div class="grid g3">${s.profiles.map((p) => html`<div class="card">
      <div class="row between"><h3>${p.name}</h3>${p.id === s.selectedProfile ? html`<span class="badge">Aktiv</span>` : ''}</div>
      <div class="small muted">${p.legoClient ? 'LEGO Client · ' : ''}${p.loader ? `Fabric ${p.loader}` : p.legoClient ? 'Fabric (neuester stabiler)' : 'Vanilla'} · ${p.gameVersion}</div>
      <div class="small muted">${p.memoryMb} MB RAM · zuletzt ${fmtDate(p.lastPlayed)}</div>
      <div class="row gap"><button class="btn sm" data-edit="${p.id}">Bearbeiten</button><button class="btn sm" data-select="${p.id}">Auswählen</button><button class="btn sm" data-mods="${p.id}">Mods-Ordner</button>${s.profiles.length > 1 ? html`<button class="btn sm danger" data-del="${p.id}">Löschen</button>` : ''}</div>
    </div>`)}</div>`);
  el.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    const d = b.dataset;
    if (b.id === 'new') return editor(el, null);
    if (d.edit) return editor(el, s.profiles.find((p) => p.id === d.edit)!);
    if (d.select) { state.settings = await api.setSettings({ selectedProfile: d.select }); toast('Profil ausgewählt', 'ok'); return profilesPage(el); }
    if (d.mods) return api.openFolder('mods', d.mods);
    if (d.del && await confirm('Profil löschen?', 'Das Profil wird aus der Liste entfernt. Der Spielordner bleibt auf der Festplatte erhalten.', 'Löschen', true)) {
      state.settings = await api.deleteProfile(d.del);
      return profilesPage(el);
    }
  };
}

async function editor(el: HTMLElement, p: GameProfile | null): Promise<void> {
  const base: GameProfile = p ?? { id: '', name: 'Neues Profil', gameVersion: '1.21.11', loader: null, legoClient: true, memoryMb: 4096, jvmArgs: '', resolution: null, gameDir: null, createdAt: 0, lastPlayed: null };
  let versions: { id: string; type: string }[] = [];
  try { versions = (await api.versions()).filter((v) => v.type === 'release').slice(0, 60); } catch { versions = [{ id: base.gameVersion, type: 'release' }]; }
  const maxMem = Math.max(2048, state.info.totalMemMb - 1024);
  mount(el, html`<h1>${p ? 'Profil bearbeiten' : 'Neues Profil'}</h1>
    <form class="card" id="f">
      <label>Name</label><input name="name" value="${base.name}" maxlength="40" required>
      <label>Minecraft-Version</label><select name="gameVersion">${versions.map((v) => html`<option ${v.id === base.gameVersion ? 'selected' : ''}>${v.id}</option>`)}</select>
      <label class="check gap"><input type="checkbox" name="legoClient" ${base.legoClient ? 'checked' : ''}> LEGO Client installieren (benötigt Fabric, derzeit nur für 1.21.11 gebaut)</label>
      <label>Fabric Loader</label><select name="loader"><option value="">${base.legoClient ? 'Neuester stabiler' : 'Kein Fabric (Vanilla)'}</option></select>
      <label>Arbeitsspeicher: <span id="memv">${base.memoryMb}</span> MB (System: ${state.info.totalMemMb} MB)</label><input type="range" name="memoryMb" min="1024" max="${maxMem}" step="256" value="${base.memoryMb}">
      <label>Zusätzliche JVM-Argumente (nur -X…, -XX:…, -D…)</label><input name="jvmArgs" value="${base.jvmArgs}" placeholder="-XX:+UseG1GC">
      <div class="row"><div class="grow"><label>Fensterbreite</label><input name="w" type="number" min="320" max="7680" value="${base.resolution?.width ?? ''}" placeholder="Standard"></div><div class="grow"><label>Fensterhöhe</label><input name="h" type="number" min="240" max="4320" value="${base.resolution?.height ?? ''}" placeholder="Standard"></div></div>
      <div class="row gap"><button class="btn primary">Speichern</button><button class="btn" type="button" id="back">Zurück</button></div>
    </form>`);
  const f = el.querySelector<HTMLFormElement>('#f')!;
  const loaderSel = f.elements.namedItem('loader') as HTMLSelectElement;
  const fillLoaders = async () => {
    const gv = (f.elements.namedItem('gameVersion') as HTMLSelectElement).value;
    try {
      const list = await api.fabricLoaders(gv);
      for (const l of list.slice(0, 25)) {
        const o = document.createElement('option');
        o.value = l.version;
        o.textContent = `${l.version}${l.stable ? '' : ' (beta)'}`;
        if (l.version === base.loader) o.selected = true;
        loaderSel.append(o);
      }
    } catch { /* offline: keep default */ }
  };
  void fillLoaders();
  (f.elements.namedItem('gameVersion') as HTMLSelectElement).addEventListener('change', () => { loaderSel.length = 1; void fillLoaders(); });
  (f.elements.namedItem('memoryMb') as HTMLInputElement).addEventListener('input', (e: Event) => { el.querySelector('#memv')!.textContent = (e.target as HTMLInputElement).value; });
  el.querySelector('#back')!.addEventListener('click', () => void profilesPage(el));
  f.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(f);
    const w = Number(fd.get('w')), h = Number(fd.get('h'));
    const profile: GameProfile = {
      ...base, name: String(fd.get('name')), gameVersion: String(fd.get('gameVersion')), legoClient: fd.get('legoClient') === 'on',
      loader: String(fd.get('loader') || '') || null, memoryMb: Number(fd.get('memoryMb')), jvmArgs: String(fd.get('jvmArgs') ?? ''),
      resolution: w && h ? { width: w, height: h } : null,
    };
    try {
      const r = await api.saveProfile(profile);
      if (!r.ok) return errorToast(new Error(r.errors.join(' · ')));
      state.settings = r.settings;
      toast('Profil gespeichert', 'ok');
      void profilesPage(el);
    } catch (err) { errorToast(err); }
  });
}
