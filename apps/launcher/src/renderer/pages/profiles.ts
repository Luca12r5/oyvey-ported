// Profiles: cards with pixel icons, an editor for every Minecraft version and
// mod loader, and imports (modpack files, other launchers, folders).

import { groupVersions } from '../../core/versions.ts';
import { go, onLeave, state } from '../app.ts';
import { renderProfileContent, renderProfiles } from '../nr/mount.tsx';
import { api, confirm, dialog, errorToast, html, mount, toast } from '../ui.ts';
import { icon, pixel, PIXEL_ICONS } from '../icons.ts';
import { t } from '../i18n.ts';
import { LEGO_CLIENT_GAME_VERSION, legoClientFits, legoClientVersions, LOADER_TYPES, loaderSupports, newestLegoClientVersion, type GameProfile, type LoaderType } from '../../core/settings.ts';
import type { FoundInstance } from '../../core/importers.ts';
import { takeNewProfileRequest } from './play.ts';
import { openModsFor } from './mods.ts';

const LOADER_LABEL: Record<LoaderType, string> = { fabric: 'Fabric', quilt: 'Quilt', forge: 'Forge', neoforge: 'NeoForge' };

let unmountList: (() => void) | null = null;

export async function profilesPage(el: HTMLElement): Promise<void> {
  unmountList?.();
  unmountList = null;
  if (takeNewProfileRequest()) return editor(el, null);
  const s = state.settings;
  const leave = (fn: () => unknown) => { unmountList?.(); unmountList = null; return fn(); };
  const run = (fn: () => Promise<unknown>) => { fn().catch(errorToast); };
  unmountList = renderProfiles(el, {
    profiles: s.profiles,
    selectedId: s.selectedProfile,
    onCreate: () => leave(() => editor(el, null)),
    onImport: () => importDialog(el),
    onVulkan: () => vulkanDialog(el),
    onEdit: (p) => leave(() => editor(el, p)),
    onMods: (p) => leave(() => contentView(el, p)),
    onOpenFolder: (p) => run(() => api.openFolder('game', p.id)),
    onSelect: (p) => run(async () => { state.settings = await api.setSettings({ selectedProfile: p.id }); }),
    onExport: (p) => run(async () => { const r = await api.exportToOfficial(p.id, null); toast(`Im offiziellen Launcher als „${r.name}“ angelegt`, 'ok'); }),
    onAddVr: (p) => run(async () => {
      toast(`Installiere Vivecraft in „${p.name}“ …`, 'info');
      state.settings = (await api.addVivecraft(p.id)).settings;
      toast(`VR für „${p.name}“ fertig: Vivecraft ist drin. Zum Spielen SteamVR oder Quest Link starten.`, 'ok');
      await profilesPage(el);
    }),
    onDuplicate: (p) => run(async () => {
      const copy: GameProfile = { ...p, id: crypto.randomUUID(), name: `${p.name} (Kopie)`, createdAt: Date.now(), lastPlayed: null, gameDir: null };
      const r = await api.saveProfile(copy);
      if (!r.ok) throw new Error(r.errors.join(', '));
      state.settings = r.settings;
      toast('Profil dupliziert', 'ok');
      await profilesPage(el);
    }),
    onDelete: (p) => run(async () => {
      if (state.settings.profiles.length <= 1) { toast('Das letzte Profil kann nicht gelöscht werden.', 'error'); return; }
      if (!await confirm('Profil löschen?', 'Das Profil wird aus der Liste entfernt. Der Spielordner bleibt auf der Festplatte erhalten.', t('prof.delete'), true)) return;
      state.settings = await api.deleteProfile(p.id);
      await profilesPage(el);
    }),
  });
  onLeave(() => { unmountList?.(); unmountList = null; });
}

type Version = { id: string; type: string; releaseTime: string };

/** A profile's content: mods, resource packs, shaders, datapacks, worlds and screenshots. */
function contentView(el: HTMLElement, p: GameProfile): void {
  el.onclick = null;
  const leave = (fn: () => unknown) => { unmountList?.(); unmountList = null; return fn(); };
  unmountList = renderProfileContent(el, {
    profile: p,
    onBack: () => leave(() => profilesPage(el)),
    onEdit: () => leave(() => editor(el, p)),
    onAdd: (type) => leave(() => { openModsFor(p.id, type); void go('mods'); }),
  });
}

async function editor(el: HTMLElement, p: GameProfile | null): Promise<void> {
  el.onclick = null;
  const base: GameProfile = p ?? {
    id: '', name: t('prof.new'), gameVersion: newestLegoClientVersion(), loaderType: 'fabric', loader: null, performancePack: true, icon: 'grass',
    legoClient: true, memoryMb: Math.min(4096, Math.max(1024, state.info.totalMemMb - 2048)), jvmArgs: '', resolution: null, gameDir: null, createdAt: 0, lastPlayed: null, account: null,
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
        ${html`<div class="setting"><div><b>${icon('vr', 'xs')} VR (Vivecraft)</b><small>Echtes VR mit Brille: installiert Vivecraft, nimmt nur VR-sichere FPS-Mods und mindestens 4 GB RAM. Der LEGO Client läuft im VR-Modus mit.</small></div><label class="switch"><input type="checkbox" name="vr"><i></i></label></div>`}
        <div class="setting"><div><b>${pixel('flame', 'xs')} ${t('prof.fps')}</b><small>${t('prof.fps.d')}</small></div><label class="switch"><input type="checkbox" name="performancePack" ${draft.performancePack ? 'checked' : ''}><i></i></label></div>
        <label>Konto</label>
        <select name="account">
          <option value="">Aktives Konto (oben rechts gewählt)</option>
          ${state.auth.accounts.map((a) => html`<option value="${a.uuid}" ${draft.account === a.uuid ? 'selected' : ''}>${a.name}</option>`)}
        </select>
        <small class="tiny muted">Mit festem Konto kannst du zwei Spiele gleichzeitig mit verschiedenen Konten spielen.</small>
        <label>${t('prof.memory')}: <b id="memv">${draft.memoryMb}</b> MB <span class="muted">/ ${state.info.totalMemMb} MB</span></label>
        <input type="range" name="memoryMb" min="1024" max="${maxMem}" step="256" value="${draft.memoryMb}">
        <label>${t('prof.jvm')}</label><input name="jvmArgs" value="${draft.jvmArgs}" placeholder="-XX:+UseG1GC">
        <div class="row"><div class="grow"><label>${t('prof.width')}</label><input name="w" type="number" min="320" max="7680" value="${draft.resolution?.width ?? ''}" placeholder="${t('prof.default')}"></div><div class="grow"><label>${t('prof.height')}</label><input name="h" type="number" min="240" max="4320" value="${draft.resolution?.height ?? ''}" placeholder="${t('prof.default')}"></div></div>
        <div class="row gap"><button class="btn primary">${icon('check', 'sm')} ${t('prof.save')}</button>${p ? html`<button class="btn" type="button" id="editMods">${icon('mods', 'sm')} ${t('mods.title')}</button>` : ''}<button class="btn" type="button" id="cancel">${t('prof.back')}</button></div>
      </section>
    </form>`);
  const f = el.querySelector<HTMLFormElement>('#f')!;
  const gv = el.querySelector<HTMLSelectElement>('#gv')!;
  const lv = el.querySelector<HTMLSelectElement>('#lv')!;
  const lego = f.elements.namedItem('legoClient') as HTMLInputElement;
  const fps = f.elements.namedItem('performancePack') as HTMLInputElement;
  const vr = f.elements.namedItem('vr') as HTMLInputElement | null;

  const fillVersions = () => {
    const snap = el.querySelector<HTMLInputElement>('#snap')!.checked;
    const hist = el.querySelector<HTMLInputElement>('#hist')!.checked;
    // Every release from 1.8 up to the newest (26.x …), grouped; snapshots/older on request.
    const groups = groupVersions(versions, { snapshots: snap, historical: hist, keep: draft.gameVersion });
    mount(gv, html`${groups.map((g) => html`<optgroup label="${g.label}">${g.versions.map((v) => html`<option value="${v.id}" ${v.id === draft.gameVersion ? 'selected' : ''}>${v.id}${v.type === 'release' ? '' : ` (${v.type.replace('old_', '')})`}</option>`)}</optgroup>`)}`);
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
    const legoOk = legoClientFits(draft);
    if (!legoOk) lego.checked = false;
    // Back on Fabric 1.21.11: the LEGO Client is on again unless the player switched it off.
    else if (lego.dataset.userOff !== '1') lego.checked = true;
    lego.disabled = !legoClientVersions().includes(draft.gameVersion);
    fps.disabled = !draft.loaderType;
    if (!draft.loaderType) fps.checked = false;
    if (vr) { vr.disabled = !draft.loaderType; if (!draft.loaderType) vr.checked = false; }
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
  vr?.addEventListener('change', () => {
    if (!vr.checked) return;
    if (!draft.loaderType) { draft.loaderType = 'fabric'; void fillLoader(); }
    const mem = f.elements.namedItem('memoryMb') as HTMLInputElement;
    if (Number(mem.value) < 4096 && Number(mem.max) >= 4096) { mem.value = '4096'; el.querySelector('#memv')!.textContent = '4096'; }
  });
  lego.addEventListener('change', () => {
    lego.dataset.userOff = lego.checked ? '' : '1';
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
  el.querySelector('#editMods')?.addEventListener('click', () => contentView(el, p!));
  f.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(f);
    const w = Number(fd.get('w')), h = Number(fd.get('h'));
    const profile: GameProfile = {
      ...draft, id: draft.id || crypto.randomUUID(), name: String(fd.get('name')), legoClient: lego.checked, performancePack: fps.checked, account: String(fd.get('account') ?? '') || null,
      memoryMb: Number(fd.get('memoryMb')), jvmArgs: String(fd.get('jvmArgs') ?? ''), resolution: w && h ? { width: w, height: h } : null,
    };
    try {
      const r = await api.saveProfile(profile);
      if (!r.ok) return errorToast(new Error(r.errors.join(' · ')));
      state.settings = r.settings;
      if (vr?.checked) {
        toast('Installiere Vivecraft …', 'info');
        try { state.settings = (await api.addVivecraft(profile.id)).settings; toast('VR-Profil fertig: Vivecraft installiert', 'ok'); } catch (err) { errorToast(err); }
      } else toast(t('prof.saved'), 'ok');
      void profilesPage(el);
    } catch (err) { errorToast(err); }
  });
}

/** Max FPS: a profile with the Vulkan renderer (VulkanMod) if it exists for the version. */
function vulkanDialog(el: HTMLElement): void {
  const gv = state.settings.profiles.find((x) => x.id === state.settings.selectedProfile)?.gameVersion ?? LEGO_CLIENT_GAME_VERSION;
  const d = dialog(html`<h2>Max FPS mit Vulkan</h2>
    <p class="muted" id="vkInfo">Prüfe, ob VulkanMod für Minecraft ${gv} verfügbar ist …</p>
    <form method="dialog" class="row between gap"><button class="btn" id="vkGo" hidden type="button">Profil anlegen</button><button class="btn">${t('prof.back')}</button></form>`);
  const info = d.querySelector<HTMLElement>('#vkInfo')!;
  const goBtn = d.querySelector<HTMLButtonElement>('#vkGo')!;
  void api.vulkan(gv).then((r) => {
    if (r.available) {
      info.textContent = `VulkanMod gibt es für Minecraft ${r.gameVersion}. Der Launcher legt ein eigenes Profil an: VulkanMod (Vulkan statt OpenGL), Lithium, FerriteCore, ModernFix und Dynamic FPS${r.gameVersion === LEGO_CLIENT_GAME_VERSION ? ' plus LEGO Client (im Vulkan-Modus ohne eigene Shader-Effekte)' : ''}. Sodium ist nicht dabei, weil es mit VulkanMod nicht zusammen läuft. Wie viel FPS es bringt, hängt von deiner Grafikkarte ab; sie muss Vulkan 1.2 können.`;
      goBtn.hidden = false;
    } else {
      info.textContent = `VulkanMod gibt es (noch) nicht für Minecraft ${r.gameVersion}.${r.suggestion ? ` Neueste unterstützte Version: ${r.suggestion}.` : ''} Bis dahin bringt das FPS-Paket (Sodium & Co.) im normalen Profil am meisten.`;
    }
  }).catch((e) => { info.textContent = `Prüfung fehlgeschlagen: ${(e as Error).message}`; });
  goBtn.onclick = async () => {
    goBtn.disabled = true;
    goBtn.textContent = 'Wird eingerichtet …';
    try {
      const r = await api.vulkanProfile(gv);
      state.settings = await api.setSettings({ selectedProfile: r.profileId });
      d.close();
      toast(`Max-FPS-Profil angelegt: ${r.mods.join(', ')}`, 'ok');
      void profilesPage(el);
    } catch (err) { errorToast(err); goBtn.disabled = false; goBtn.textContent = 'Profil anlegen'; }
  };
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
            <ul class="small scroll-list">${r.missing.slice(0, 200).map((m) => html`<li>${m}</li>`)}</ul><p class="small muted">Du kannst sie unter Profile → Mods über Modrinth suchen oder manuell in den Mods-Ordner legen.</p><form method="dialog"><button class="btn primary">OK</button></form>`);
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
