// Mods & content per profile: installed mods (toggle/remove), Modrinth search
// for mods, resource packs, shaders and modpacks (installed as new profiles).

import { go, state } from '../app.ts';
import { api, confirm, errorToast, fmtNum, html, mount, toast } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { t } from '../i18n.ts';
import type { ProjectType, SearchHit } from '../../core/modrinth.ts';
import { profileLine } from './play.ts';

let profileId: string | null = null;
let tab: 'installed' | ProjectType = 'installed';

export function openModsFor(id: string): void {
  profileId = id;
  tab = 'installed';
}

export async function modsPage(el: HTMLElement): Promise<void> {
  const s = state.settings;
  const profile = s.profiles.find((p) => p.id === (profileId ?? s.selectedProfile)) ?? s.profiles[0]!;
  profileId = profile.id;
  const tabs: ['installed' | ProjectType, string][] = [['installed', t('mods.installed')], ['mod', t('mods.mods')], ['resourcepack', t('mods.resourcepacks')], ['shader', t('mods.shaders')], ['modpack', t('mods.modpacks')]];
  mount(el, html`<div class="page-head"><div><h1>${t('mods.title')}</h1><p class="muted small">${pixel(profile.icon, 'xs')} ${profile.name} · ${profileLine(profile)}</p></div>
      <div class="row"><select id="prof" class="compact">${s.profiles.map((p) => html`<option value="${p.id}" ${p.id === profile.id ? 'selected' : ''}>${p.name}</option>`)}</select>
      <button class="icon-btn" id="folder" title="${t('mods.folder')}">${icon('folder', 'sm')}</button></div></div>
    <div class="tabs pill" id="tabs">${tabs.map(([id, l]) => html`<button data-tab="${id}" class="${id === tab ? 'active' : ''}">${l}</button>`)}</div>
    <div id="body"></div>`);
  el.querySelector('#prof')!.addEventListener('change', (e) => { profileId = (e.target as HTMLSelectElement).value; void modsPage(el); });
  el.querySelector('#folder')!.addEventListener('click', () => void api.openFolder(tab === 'resourcepack' ? 'resourcepacks' : tab === 'shader' ? 'shaderpacks' : 'mods', profile.id));
  el.querySelector('#tabs')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-tab]');
    if (!b) return;
    tab = b.dataset.tab as typeof tab;
    void modsPage(el);
  });
  const body = el.querySelector<HTMLElement>('#body')!;
  if (tab === 'installed') return installed(body, profile.id, !!profile.loaderType);
  if (tab === 'mod' && !profile.loaderType) {
    mount(body, html`<div class="empty">${pixel('creeper', 'lg')}<p>${t('mods.vanilla')}</p><button class="btn primary" id="edit">${t('prof.edit')}</button></div>`);
    body.querySelector('#edit')!.addEventListener('click', () => void go('profiles'));
    return;
  }
  browse(body, profile.id, tab);
}

async function installed(body: HTMLElement, id: string, modded: boolean): Promise<void> {
  const mods = await api.mods(id);
  if (!mods.length) {
    mount(body, html`<div class="empty">${pixel('chest', 'lg')}<p>${modded ? t('mods.empty') : t('mods.vanilla')}</p>${modded ? html`<button class="btn primary" id="browse">${icon('search', 'sm')} ${t('mods.mods')}</button>` : ''}</div>`);
    body.querySelector('#browse')?.addEventListener('click', () => { tab = 'mod'; void modsPage(body.parentElement as HTMLElement); });
    return;
  }
  mount(body, html`<div class="list">${mods.map((m) => html`<div class="list-row ${m.enabled ? '' : 'off'}">
      <div class="li-icon">${m.iconUrl ? html`<img src="${m.iconUrl}" alt="" loading="lazy">` : pixel('package', 'md')}</div>
      <div class="grow"><b>${m.title}</b><div class="tiny muted mono">${m.filename}${m.managedByLauncher ? html` · <span class="badge">${t('mods.managed')}</span>` : ''}</div></div>
      ${m.managedByLauncher ? '' : html`<label class="switch" title="${m.enabled ? 'An' : 'Aus'}"><input type="checkbox" data-toggle="${m.filename}" ${m.enabled ? 'checked' : ''}><i></i></label>
      <button class="icon-btn danger" data-remove="${m.filename}" title="${t('mods.remove')}">${icon('trash', 'sm')}</button>`}
    </div>`)}</div>`);
  body.onchange = async (e) => {
    const i = (e.target as HTMLElement).closest<HTMLInputElement>('[data-toggle]');
    if (!i) return;
    try {
      await api.toggleMod(id, i.dataset.toggle!, i.checked);
      void installed(body, id, modded);
    } catch (err) { errorToast(err); }
  };
  body.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-remove]');
    if (!b || !(await confirm(t('mods.remove'), `${b.dataset.remove} wird gelöscht.`, t('mods.remove'), true))) return;
    try {
      await api.removeMod(id, b.dataset.remove!);
      void installed(body, id, modded);
    } catch (err) { errorToast(err); }
  };
}

function browse(body: HTMLElement, id: string, type: ProjectType): void {
  let q = '';
  let offset = 0;
  let hits: SearchHit[] = [];
  let total = 0;
  let timer = 0;
  mount(body, html`<div class="searchbar">${icon('search', 'sm')}<input id="q" placeholder="${t('mods.search')}" autocomplete="off"></div><div id="res" class="results"></div><div class="center gap"><button class="btn" id="more" hidden>${t('mods.more')}</button></div>`);
  const res = body.querySelector<HTMLElement>('#res')!;
  const more = body.querySelector<HTMLButtonElement>('#more')!;
  const load = async (append: boolean) => {
    if (!append) { offset = 0; hits = []; mount(res, html`${Array.from({ length: 6 }, () => html`<div class="mcard skeleton"></div>`)}`); }
    try {
      const r = await api.searchProjects(id, q, type, offset);
      hits = [...hits, ...r.hits];
      total = r.total;
      offset = hits.length;
      mount(res, hits.length ? html`${hits.map((h, i) => html`<article class="mcard">
          <div class="mc-icon">${h.icon_url ? html`<img src="${h.icon_url}" alt="" loading="lazy">` : pixel('package', 'md')}</div>
          <div class="grow"><b>${h.title}</b><div class="tiny muted">${h.author} · ${icon('download', 'xs')} ${fmtNum(h.downloads)}</div><p class="small clamp">${h.description}</p></div>
          <button class="btn sm primary" data-i="${i}">${icon('download', 'sm')} ${t('mods.install')}</button></article>`)}` : html`<div class="empty">Keine Treffer.</div>`);
      more.hidden = hits.length >= total;
    } catch (err) {
      mount(res, html`<div class="empty">Modrinth nicht erreichbar: ${err instanceof Error ? err.message : String(err)}</div>`);
    }
  };
  body.querySelector('#q')!.addEventListener('input', (e) => {
    q = (e.target as HTMLInputElement).value;
    clearTimeout(timer);
    timer = window.setTimeout(() => void load(false), 300);
  });
  more.addEventListener('click', () => void load(true));
  res.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-i]');
    if (!b) return;
    const h = hits[Number(b.dataset.i)]!;
    b.disabled = true;
    mount(b, html`<span class="spinner"></span>`);
    try {
      if (type === 'modpack') {
        const r = await api.installModpack(h.project_id, h.title);
        state.settings = await api.getSettings();
        state.settings = await api.setSettings({ selectedProfile: r.profileId });
        toast(`${h.title}: ${t('prof.imported')}`, 'ok');
        void go('profiles');
        return;
      }
      const titles = await api.installProject(id, h.project_id, type, h.title, h.icon_url);
      toast(`${t('mods.installedOk')}: ${titles.join(', ')}`, 'ok');
      mount(b, html`${icon('check', 'sm')} ${t('mods.installedOk')}`);
    } catch (err) {
      errorToast(err);
      b.disabled = false;
      mount(b, html`${icon('download', 'sm')} ${t('mods.install')}`);
    }
  };
  void load(false);
}
