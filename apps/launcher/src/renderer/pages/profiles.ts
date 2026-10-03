// Profiles: cards with pixel icons, an editor for every Minecraft version and
// mod loader, and imports (modpack files, other launchers, folders).

import { go, state } from '../app.ts';
import { api, confirm, dialog, errorToast, fmtDate, html, mount, toast } from '../ui.ts';
import { icon, pixel, PIXEL_ICONS } from '../icons.ts';
import { t } from '../i18n.ts';
import { LEGO_CLIENT_GAME_VERSION, LOADER_TYPES, loaderSupports, type GameProfile, type LoaderType } from '../../core/settings.ts';
import type { FoundInstance } from '../../core/importers.ts';
import { profileLine, takeNewProfileRequest } from './play.ts';
import { openModsFor } from './mods.ts';

const LOADER_LABEL: Record<LoaderType, string> = { fabric: 'Fabric', quilt: 'Quilt', forge: 'Forge', neoforge: 'NeoForge' };

export async function profilesPage(el: HTMLElement): Promise<void> {
  if (takeNewProfileRequest()) return editor(el, null);
  const s = state.settings;
  mount(el, html`<div class="page-head"><div><h1>${t('prof.title')}</h1><p class="muted small">Jedes Profil hat einen eigenen Spielordner mit Welten, Mods und Einstellungen.</p></div>
      <div class="row"><button class="btn" id="import">${icon('import', 'sm')} ${t('prof.import')}</button><button class="btn primary" id="new">${icon('plus', 'sm')} ${t('prof.new')}</button></div></div>
    <div class="profile-grid">${s.profiles.map((p) => html`<article class="pcard ${p.id === s.selectedProfile ? 'active' : ''}">
      <div class="pc-icon">${pixel(p.icon, 'lg')}</div>
      <div class="pc-body"><h3>${p.name}</h3><div class="small muted">${profileLine(p)}</div>
        <div class="tiny muted">${p.memoryMb} MB · ${p.lastPlayed ? `${t('play.lastPlayed')} ${fmtDate(p.lastPlayed)}` : t('prof.never')}${p.performancePack ? ' · FPS' : ''}</div></div>
      <div class="pc-actions">
        <button class="btn sm primary" data-play="${p.id}">${icon('play', 'sm')} ${t('prof.play')}</button>
        <button class="icon-btn" data-edit="${p.id}" title="${t('prof.edit')}">${icon('sliders', 'sm')}</button>
        <button class="icon-btn" data-mods="${p.id}" title="${t('prof.mods')}">${icon('mods', 'sm')}</button>
        <button class="icon-btn" data-folder="${p.id}" title="${t('mods.folder')}">${icon('folder', 'sm')}</button>
        ${s.profiles.length > 1 ? html`<button class="icon-btn danger" data-del="${p.id}" title="${t('prof.delete')}">${icon('trash', 'sm')}</button>` : ''}
      </div></article>`)}</div>`);
  el.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    const d = b.dataset;
    try {
      if (b.id === 'new') return await editor(el, null);
      if (b.id === 'import') return importDialog(el);
      if (d.edit) return await editor(el, s.profiles.find((p) => p.id === d.edit)!);
      if (d.play) { state.settings = await api.setSettings({ selectedProfile: d.play }); return void go('play'); }
      if (d.mods) { openModsFor(d.mods); return void go('mods'); }
      if (d.folder) return await api.openFolder('game', d.folder);
      if (d.del && await confirm('Profil löschen?', 'Das Profil wird aus der Liste entfernt. Der Spielordner bleibt auf der Festplatte erhalten.', t('prof.delete'), true)) {
        state.settings = await api.deleteProfile(d.del);
        return await profilesPage(el);
      }
    } catch (err) { errorToast(err); }
  };
}

type Version = { id: string; type: string; releaseTime: string };

async function editor(el: HTMLElement, p: GameProfile | null): Promise<void> {
  el.onclick = null;
  const base: GameProfile = p ?? {
    id: '', name: t('prof.new'), gameVersion: LEGO_CLIENT_GAME_VERSION, loaderType: 'fabric', loader: null, performancePack: true, icon: 'grass',
    legoClient: false, memoryMb: Math.min(4096, Math.max(1024, state.info.totalMemMb - 2048)), jvmArgs: '', resolution: null, gameDir: null, createdAt: 0, lastPlayed: null,
  };
  const draft: GameProfile = { ...base };
  let versions: Version[] = [];
  try { versions = await api.versions(); } catch { versions = [{ id: base.gameVersion, type: 'release', releaseTime: '' }]; }
  const maxMem = Math.max(2048, state.info.totalMemMb - 1024);
  mount(el, html`<div class="page-head"><div class="row"><button class="icon-btn" id="back" title="${t('prof.back')}">${icon('chevronRight', 'sm flip')}</button><h1>${p ? t('prof.edit') : t('prof.new')}</h1></div></div>
    <form class="editor" id="f">
      <section class="glass">
        <label>${t('prof.icon')}</label>
        <div class="icon-pick" id="icons">${PIXEL_ICONS.map((i) => html`<button type="button" class="${i === draft.icon ? 'on' : ''}" data-icon="${i}" title="${i}">${pixel(i, 'md')}</button>`)}</div>
        <label>${t('prof.name')}</label><input name="name" value="${draft.name}" maxlength="40" required>
        <div class="row between"><label>${t('prof.version')}</label>
          <div class="row tiny"><label class="check"><input type="checkbox" id="snap" ${state.settings.showSnapshots ? 'checked' : ''}> Snapshots</label><label class="check"><input type="checkbox" id="hist" ${state.settings.showHistorical ? 'checked' : ''}> Alpha/Beta</label></div></div>
        <select name="gameVersion" id="gv"></select>
        <label>${t('prof.loader')}</label>
        <div class="seg wide" id="loaders">
          <button type="button" data-loader="">${icon('cube', 'sm')} ${t('play.vanilla')}</button>
          ${LOADER_TYPES.map((l) => html`<button type="button" data-loader="${l}">${LOADER_LABEL[l]}${l === 'forge' || l === 'neoforge' ? html`<sup>β</sup>` : ''}</button>`)}
        </div>
        <div id="loaderNote" class="tiny muted"></div>
        <div id="loaderRow"><label>${t('prof.loaderVersion')}</label><select name="loader" id="lv"><option value="">${t('prof.newest')}</option></select></div>
      </section>
      <section class="glass">
        <div class="setting"><div><b>${pixel('brick', 'xs')} ${t('prof.lego')}</b><small>${t('prof.lego.d')}</small></div><label class="switch"><input type="checkbox" name="legoClient" ${draft.legoClient ? 'checked' : ''}><i></i></label></div>
        <div class="setting"><div><b>${pixel('flame', 'xs')} ${t('prof.fps')}</b><small>${t('prof.fps.d')}</small></div><label class="switch"><input type="checkbox" name="performancePack" ${draft.performancePack ? 'checked' : ''}><i></i></label></div>
        <label>${t('prof.memory')}: <b id="memv">${draft.memoryMb}</b> MB <span class="muted">/ ${state.info.totalMemMb} MB</span></label>
        <input type="range" name="memoryMb" min="1024" max="${maxMem}" step="256" value="${draft.memoryMb}">
        <label>${t('prof.jvm')}</label><input name="jvmArgs" value="${draft.jvmArgs}" placeholder="-XX:+UseG1GC">
        <div class="row"><div class="grow"><label>${t('prof.width')}</label><input name="w" type="number" min="320" max="7680" value="${draft.resolution?.width ?? ''}" placeholder="${t('prof.default')}"></div><div class="grow"><label>${t('prof.height')}</label><input name="h" type="number" min="240" max="4320" value="${draft.resolution?.height ?? ''}" placeholder="${t('prof.default')}"></div></div>
        <div class="row gap"><button class="btn primary">${icon('check', 'sm')} ${t('prof.save')}</button><button class="btn" type="button" id="cancel">${t('prof.back')}</button></div>
      </section>
    </form>`);
  const f = el.querySelector<HTMLFormElement>('#f')!;
  const gv = el.querySelector<HTMLSelectElement>('#gv')!;
  const lv = el.querySelector<HTMLSelectElement>('#lv')!;
  const lego = f.elements.namedItem('legoClient') as HTMLInputElement;
  const fps = f.elements.namedItem('performancePack') as HTMLInputElement;

  const fillVersions = () => {
    const snap = el.querySelector<HTMLInputElement>('#snap')!.checked;
    const hist = el.querySelector<HTMLInputElement>('#hist')!.checked;
    const list = versions.filter((v) => v.type === 'release' || (snap && v.type === 'snapshot') || (hist && v.type.startsWith('old_')) || v.id === draft.gameVersion);
    mount(gv, html`${list.map((v) => html`<option value="${v.id}" ${v.id === draft.gameVersion ? 'selected' : ''}>${v.id}${v.type === 'release' ? '' : ` (${v.type.replace('old_', '')})`}</option>`)}`);
  };
  let loaderReq = 0;
  const fillLoader = async () => {
    const supported = (l: LoaderType) => loaderSupports(l, draft.gameVersion);
    if (draft.loaderType && !supported(draft.loaderType)) { draft.loaderType = null; draft.loader = null; }
    el.querySelectorAll<HTMLButtonElement>('#loaders button').forEach((b) => {
      const l = (b.dataset.loader || null) as LoaderType | null;
      b.classList.toggle('on', l === draft.loaderType);
      b.disabled = !!l && !supported(l);
      b.title = b.disabled ? t('prof.unsupported') : '';
    });
    el.querySelector<HTMLElement>('#loaderNote')!.textContent = draft.loaderType === 'forge' || draft.loaderType === 'neoforge'
      ? `${LOADER_LABEL[draft.loaderType]}: ${t('prof.experimental')} – der offizielle Installer läuft beim ersten Start im Hintergrund.` : '';
    el.querySelector<HTMLElement>('#loaderRow')!.hidden = !draft.loaderType;
    const legoOk = draft.gameVersion === LEGO_CLIENT_GAME_VERSION && draft.loaderType === 'fabric';
    if (!legoOk) lego.checked = false;
    lego.disabled = draft.gameVersion !== LEGO_CLIENT_GAME_VERSION;
    fps.disabled = !draft.loaderType;
    if (!draft.loaderType) fps.checked = false;
    lv.length = 1;
    if (!draft.loaderType) return;
    const req = ++loaderReq;
    try {
      const list = await api.loaderVersions(draft.loaderType, draft.gameVersion);
      if (req !== loaderReq) return;
      for (const l of list.slice(0, 40)) {
        const o = document.createElement('option');
        o.value = l.version;
        o.textContent = `${l.version}${l.stable ? '' : ' (beta)'}`;
        if (l.version === draft.loader) o.selected = true;
        lv.append(o);
      }
    } catch { /* offline: newest is resolved at launch */ }
  };
  fillVersions();
  void fillLoader();

  el.querySelector('#snap')!.addEventListener('change', fillVersions);
  el.querySelector('#hist')!.addEventListener('change', fillVersions);
  gv.addEventListener('change', () => { draft.gameVersion = gv.value; draft.loader = null; void fillLoader(); });
  lv.addEventListener('change', () => { draft.loader = lv.value || null; });
  el.querySelector('#loaders')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-loader]');
    if (!b || b.disabled) return;
    draft.loaderType = (b.dataset.loader || null) as LoaderType | null;
    draft.loader = null;
    void fillLoader();
  });
  lego.addEventListener('change', () => {
    if (lego.checked) { draft.loaderType = 'fabric'; void fillLoader(); }
  });
  el.querySelector('#icons')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-icon]');
    if (!b) return;
    draft.icon = b.dataset.icon!;
    el.querySelectorAll('#icons button').forEach((x) => x.classList.toggle('on', x === b));
  });
  (f.elements.namedItem('memoryMb') as HTMLInputElement).addEventListener('input', (e) => { el.querySelector('#memv')!.textContent = (e.target as HTMLInputElement).value; });
  el.querySelector('#back')!.addEventListener('click', () => void profilesPage(el));
  el.querySelector('#cancel')!.addEventListener('click', () => void profilesPage(el));
  f.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(f);
    const w = Number(fd.get('w')), h = Number(fd.get('h'));
    const profile: GameProfile = {
      ...draft, name: String(fd.get('name')), legoClient: lego.checked, performancePack: fps.checked,
      memoryMb: Number(fd.get('memoryMb')), jvmArgs: String(fd.get('jvmArgs') ?? ''), resolution: w && h ? { width: w, height: h } : null,
    };
    try {
      const r = await api.saveProfile(profile);
      if (!r.ok) return errorToast(new Error(r.errors.join(' · ')));
      state.settings = r.settings;
      toast(t('prof.saved'), 'ok');
      void profilesPage(el);
    } catch (err) { errorToast(err); }
  });
}

function importDialog(el: HTMLElement): void {
  const d = dialog(html`<h2>${t('prof.import')}</h2>
    <div class="import-opts">
      <button class="import-opt" data-imp="file">${pixel('package', 'md')}<span><b>${t('prof.importFile')}</b><small>Modrinth-Packs werden vollständig installiert, CurseForge-Packs mit Konfigurationen.</small></span></button>
      <button class="import-opt" data-imp="scan">${pixel('chest', 'md')}<span><b>${t('prof.importScan')}</b><small>Prism Launcher, CurseForge-App und offizieller Launcher – Mods, Configs, Ressourcen- und Shaderpakete, Optionen.</small></span></button>
      <button class="import-opt" data-imp="folder">${icon('folder')}<span><b>${t('prof.importFolder')}</b><small>Ein Instanz- oder .minecraft-Ordner.</small></span></button>
    </div>
    <div id="impList"></div>
    <form method="dialog" class="row between gap"><label class="check"><input type="checkbox" id="worlds"> ${t('prof.importWorlds')}</label><button class="btn">${t('prof.back')}</button></form>`);
  const list = d.querySelector<HTMLElement>('#impList')!;
  const finish = async (profileId: string, extra = '') => {
    state.settings = await api.setSettings({ selectedProfile: profileId });
    d.close();
    toast(`${t('prof.imported')}${extra}`, 'ok');
    void profilesPage(el);
  };
  const showInstances = (found: FoundInstance[]) => {
    mount(list, found.length ? html`<div class="inst-list">${found.map((x, i) => html`<div class="inst"><span class="grow"><b>${x.name}</b><small>${x.source} · ${x.loaderType ? LOADER_LABEL[x.loaderType] : t('play.vanilla')} · ${x.gameVersion ?? '?'}</small></span><button class="btn sm" data-inst="${i}">${t('mods.import')}</button></div>`)}</div>`
      : html`<p class="muted small">${t('prof.importNone')}</p>`);
    list.onclick = async (e) => {
      const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-inst]');
      if (!b) return;
      b.disabled = true;
      try {
        const r = await api.importInstance(found[Number(b.dataset.inst)]!, d.querySelector<HTMLInputElement>('#worlds')!.checked);
        await finish(r.profileId, `: ${r.copied.join(', ') || '–'}`);
      } catch (err) { errorToast(err); b.disabled = false; }
    };
  };
  d.querySelector('.import-opts')!.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-imp]');
    if (!b) return;
    try {
      if (b.dataset.imp === 'file') {
        mount(list, html`<div class="progress indet"><i data-p="100"></i></div>`);
        const r = await api.importFile();
        if (!r) { list.replaceChildren(); return; }
        await finish(r.profileId);
        if (r.missing.length) {
          dialog(html`<h2>Fast fertig</h2><p>Konfigurationen wurden übernommen. Diese Dateien konnten nicht automatisch geladen werden (CurseForge-API-Schlüssel fehlt oder der Autor erlaubt keine Drittanbieter-Downloads):</p>
            <ul class="small scroll-list">${r.missing.slice(0, 200).map((m) => html`<li>${m}</li>`)}</ul><p class="small muted">Du kannst sie im Mods-Tab über Modrinth suchen oder manuell in den Mods-Ordner legen.</p><form method="dialog"><button class="btn primary">OK</button></form>`);
        }
      } else if (b.dataset.imp === 'scan') {
        mount(list, html`<div class="progress indet"><i data-p="100"></i></div>`);
        showInstances(await api.scanInstances());
      } else {
        const inst = await api.pickFolder();
        if (inst) showInstances([inst]);
      }
    } catch (err) { errorToast(err); list.replaceChildren(); }
  });
}
