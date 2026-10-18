// Mods page (top level, NoRisk-style): content tabs, search with sort, source
// switch, collapsible filter column, result rows with INSTALL + profile picker.
// "Installiert" shows and manages the files of one profile.

import { go, state } from '../app.ts';
import { api, confirm, errorToast, fmtNum, html, mount, toast } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { t } from '../i18n.ts';
import type { ProjectType, SearchFilters, SearchHit } from '../../core/modrinth.ts';

type Tab = 'installed' | ProjectType;
let profileId: string | null = null;
let tab: Tab = 'mod';
const filters: Required<Pick<SearchFilters, 'versions' | 'categories' | 'loaders'>> & { env: 'client' | 'server' | null; sort: NonNullable<SearchFilters['sort']> } = {
  versions: [], categories: [], loaders: [], env: null, sort: 'downloads',
};
const open = new Set<string>();
let releases: string[] = [];

/** Opens the page on the installed files of one profile (from a profile row's MODS button). */
export function openModsFor(id: string): void {
  profileId = id;
  tab = 'installed';
}

const TABS: [Tab, string][] = [['modpack', 'Modpacks'], ['mod', 'Mods'], ['resourcepack', 'Resourcepacks'], ['shader', 'Shaders'], ['datapack', 'Datapacks'], ['installed', 'Installiert']];
const CATEGORIES: Record<ProjectType, string[]> = {
  mod: ['adventure', 'decoration', 'economy', 'equipment', 'food', 'game-mechanics', 'library', 'magic', 'management', 'minigame', 'mobs', 'optimization', 'social', 'storage', 'technology', 'transportation', 'utility', 'worldgen'],
  modpack: ['adventure', 'challenging', 'combat', 'kitchen-sink', 'lightweight', 'magic', 'multiplayer', 'optimization', 'quests', 'technology'],
  resourcepack: ['16x', '32x', '64x', '128x', 'blocks', 'combat', 'cursed', 'decoration', 'entities', 'fonts', 'gui', 'items', 'realistic', 'simplistic', 'themed', 'tweaks', 'vanilla-like'],
  shader: ['atmosphere', 'bloom', 'cartoon', 'colored-lighting', 'fantasy', 'foliage', 'high', 'low', 'medium', 'path-tracing', 'pbr', 'realistic', 'reflections', 'semi-realistic', 'shadows', 'vanilla-like'],
  datapack: ['adventure', 'decoration', 'equipment', 'game-mechanics', 'magic', 'mobs', 'utility', 'worldgen'],
};
const LOADERS = ['fabric', 'quilt', 'forge', 'neoforge'];
const nice = (s: string) => s.replace(/-/g, ' ');

export async function modsPage(el: HTMLElement): Promise<void> {
  const s = state.settings;
  const profile = s.profiles.find((p) => p.id === (profileId ?? s.selectedProfile)) ?? s.profiles[0]!;
  profileId = profile.id;
  if (!releases.length) {
    // Load the version list in the background; the page shows right away.
    releases = [profile.gameVersion];
    void api.versions().then((list) => {
      releases = list.filter((v) => v.type === 'release').map((v) => v.id).slice(0, 40);
      if (el.isConnected && open.has('versions')) void modsPage(el);
    }).catch(() => {});
  }
  const cf = !!s.curseforgeKey;
  mount(el, html`<div class="mods-page">
    <div class="mods-main">
      <div class="tabs" id="tabs">${TABS.map(([id, l]) => html`<button data-tab="${id}" class="${id === tab ? 'active' : ''}">${l}</button>`)}</div>
      <div class="mods-bar">
        <div class="searchbar">${icon('search', 'sm')}<input id="q" placeholder="${tab === 'installed' ? 'Installierte durchsuchen…' : `Search ${TABS.find(([i]) => i === tab)![1].toLowerCase()}…`}" autocomplete="off"><span class="sb-sep"></span>
          <select id="sort" title="Sortierung"><option value="downloads">Downloads</option><option value="relevance">Relevanz</option><option value="follows">Beliebt</option><option value="updated">Aktualisiert</option><option value="newest">Neu</option></select></div>
        <button class="icon-btn sq" id="filt" title="Filter">${icon('sliders', 'sm')}</button>
        <div class="src-switch"><button class="on" data-src="modrinth">${icon('package', 'sm')} Modrinth</button><button data-src="curseforge" ${cf ? '' : 'disabled'} title="${cf ? 'CurseForge' : 'Für CurseForge einen API-Schlüssel unter Optionen → Spiel eintragen'}">${icon('flame', 'sm')} CurseForge</button></div>
        <label class="mods-prof" title="Ziel-Profil">${icon('profiles', 'sm')}<select id="prof">${s.profiles.map((p) => html`<option value="${p.id}" ${p.id === profile.id ? 'selected' : ''}>${p.name}</option>`)}</select></label>
      </div>
      <div id="body" class="mods-list"></div>
    </div>
    <aside class="mods-filters" id="filters" ${tab === 'installed' ? 'hidden' : ''}>
      ${section('versions', 'Game Versions', releases.map((v) => [v, v]), filters.versions)}
      ${section('categories', 'Categories', (CATEGORIES[tab as ProjectType] ?? []).map((c) => [c, nice(c)]), filters.categories)}
      ${tab === 'mod' || tab === 'modpack' ? section('loaders', 'Mod Loaders', LOADERS.map((l) => [l, l]), filters.loaders) : ''}
      ${tab === 'mod' || tab === 'modpack' ? html`<details class="fsec" data-sec="env" ${open.has('env') ? 'open' : ''}><summary>Environment ${icon('chevron', 'xs')}</summary><div class="fopts">
        ${(['client', 'server'] as const).map((e) => html`<label class="check"><input type="checkbox" data-env="${e}" ${filters.env === e ? 'checked' : ''}> ${e}</label>`)}</div></details>` : ''}
      <button class="btn sm ghost" id="freset">${icon('refresh', 'sm')} Filter zurücksetzen</button>
    </aside>
  </div>`);
  (el.querySelector('#sort') as HTMLSelectElement).value = filters.sort;
  el.querySelector('#tabs')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-tab]');
    if (!b) return;
    tab = b.dataset.tab as Tab;
    filters.categories = [];
    void modsPage(el);
  });
  el.querySelector('#prof')!.addEventListener('change', (e) => { profileId = (e.target as HTMLSelectElement).value; void modsPage(el); });
  el.querySelector('#filt')!.addEventListener('click', () => { const f = el.querySelector<HTMLElement>('#filters')!; f.hidden = !f.hidden; });
  const body = el.querySelector<HTMLElement>('#body')!;
  if (tab === 'installed') return installed(body, profile.id, !!profile.loaderType, el.querySelector<HTMLInputElement>('#q')!);
  if (tab === 'mod' && !profile.loaderType && !filters.loaders.length) {
    mount(body, html`<div class="empty">${pixel('creeper', 'lg')}<p>${t('mods.vanilla')}</p><button class="btn primary" id="edit">${t('prof.edit')}</button></div>`);
    body.querySelector('#edit')!.addEventListener('click', () => void go('profiles'));
    return;
  }
  const reload = browse(body, profile.id, tab, el.querySelector<HTMLInputElement>('#q')!);
  el.querySelector('#sort')!.addEventListener('change', (e) => { filters.sort = (e.target as HTMLSelectElement).value as typeof filters.sort; reload(); });
  const side = el.querySelector<HTMLElement>('#filters')!;
  side.addEventListener('toggle', (e) => { const d = e.target as HTMLDetailsElement; if (d.dataset.sec) { if (d.open) open.add(d.dataset.sec); else open.delete(d.dataset.sec); } }, true);
  side.addEventListener('change', (e) => {
    const i = e.target as HTMLInputElement;
    if (i.dataset.env) { filters.env = i.checked ? (i.dataset.env as 'client' | 'server') : null; side.querySelectorAll<HTMLInputElement>('[data-env]').forEach((x) => { if (x !== i) x.checked = false; }); }
    const k = i.dataset.f as 'versions' | 'categories' | 'loaders' | undefined;
    if (k) filters[k] = i.checked ? [...filters[k], i.value] : filters[k].filter((x) => x !== i.value);
    reload();
  });
  el.querySelector('#freset')!.addEventListener('click', () => { filters.versions = []; filters.categories = []; filters.loaders = []; filters.env = null; void modsPage(el); });
}

function section(key: 'versions' | 'categories' | 'loaders', title: string, items: string[][], sel: string[]) {
  return html`<details class="fsec" data-sec="${key}" ${open.has(key) || sel.length ? 'open' : ''}><summary>${title}${sel.length ? html` <span class="fcount">${sel.length}</span>` : ''} ${icon('chevron', 'xs')}</summary>
    <div class="fopts">${items.map(([v, l]) => html`<label class="check"><input type="checkbox" data-f="${key}" value="${v}" ${sel.includes(v!) ? 'checked' : ''}> ${l}</label>`)}</div></details>`;
}

async function installed(body: HTMLElement, id: string, modded: boolean, q: HTMLInputElement): Promise<void> {
  const mods = await api.mods(id);
  if (!mods.length) {
    mount(body, html`<div class="empty">${pixel('chest', 'lg')}<p>${modded ? t('mods.empty') : t('mods.vanilla')}</p></div>`);
    return;
  }
  const draw = () => {
    const f = q.value.trim().toLowerCase();
    mount(body, html`${mods.filter((m) => !f || (m.title ?? '').toLowerCase().includes(f) || m.filename.toLowerCase().includes(f)).map((m) => html`<div class="mod-row ${m.enabled ? '' : 'off'}">
      <div class="mr-icon">${m.iconUrl ? html`<img src="${m.iconUrl}" alt="" loading="lazy">` : pixel('package', 'md')}</div>
      <div class="mr-body"><div class="mr-title">${m.title}</div><div class="mr-desc mono">${m.filename}</div>${m.managedByLauncher ? html`<div class="mr-tags"><span class="tag">${t('mods.managed')}</span></div>` : ''}</div>
      <div class="mr-side">${m.managedByLauncher ? '' : html`<label class="switch" title="${m.enabled ? 'An' : 'Aus'}"><input type="checkbox" data-toggle="${m.filename}" ${m.enabled ? 'checked' : ''}><i></i></label>
        <button class="icon-btn sq danger" data-remove="${m.filename}" title="${t('mods.remove')}">${icon('trash', 'sm')}</button>`}</div>
    </div>`)}`);
  };
  q.oninput = draw;
  draw();
  body.onchange = async (e) => {
    const i = (e.target as HTMLElement).closest<HTMLInputElement>('[data-toggle]');
    if (!i) return;
    try { await api.toggleMod(id, i.dataset.toggle!, i.checked); void installed(body, id, modded, q); } catch (err) { errorToast(err); }
  };
  body.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-remove]');
    if (!b || !(await confirm(t('mods.remove'), `${b.dataset.remove} wird gelöscht.`, t('mods.remove'), true))) return;
    try { await api.removeMod(id, b.dataset.remove!); void installed(body, id, modded, q); } catch (err) { errorToast(err); }
  };
}

/** Search results; returns a function that reloads from the first page. */
function browse(body: HTMLElement, id: string, type: ProjectType, qIn: HTMLInputElement): () => void {
  let offset = 0;
  let hits: SearchHit[] = [];
  let total = 0;
  let timer = 0;
  let seq = 0;
  const load = async (append: boolean) => {
    const my = ++seq;
    if (!append) { offset = 0; hits = []; mount(body, html`${Array.from({ length: 5 }, () => html`<div class="mod-row skeleton"></div>`)}`); }
    try {
      const r = await api.searchProjects(id, qIn.value, type, offset, { versions: filters.versions, categories: filters.categories, loaders: filters.loaders, env: filters.env, sort: filters.sort });
      if (my !== seq) return;
      hits = [...hits, ...r.hits];
      total = r.total;
      offset = hits.length;
      mount(body, hits.length ? html`${hits.map((h, i) => html`<article class="mod-row">
          <div class="mr-icon">${h.icon_url ? html`<img src="${h.icon_url}" alt="" loading="lazy">` : pixel('package', 'md')}</div>
          <div class="mr-body"><div class="mr-title">${h.title} <span class="mr-by">by ${h.author}</span></div><p class="mr-desc">${h.description}</p>
            <div class="mr-tags">${h.categories.filter((c) => !['minecraft'].includes(c)).slice(0, 4).map((c) => html`<span class="tag">${nice(c)}</span>`)}</div></div>
          <div class="mr-side"><div class="mr-dl">${icon('download', 'xs')} ${fmtNum(h.downloads)}</div>
            <div class="inst"><button class="btn sm" data-i="${i}">${icon('download', 'sm')} ${t('mods.install').toUpperCase()}</button><button class="btn sm drop" data-pick="${i}" title="Profil wählen">${icon('chevron', 'xs')}</button></div></div>
        </article>`)}${hits.length < total ? html`<div class="center"><button class="btn" id="more">${t('mods.more')}</button></div>` : ''}` : html`<div class="empty">Keine Treffer.</div>`);
    } catch (err) {
      if (my === seq) mount(body, html`<div class="empty">Modrinth nicht erreichbar: ${err instanceof Error ? err.message : String(err)}</div>`);
    }
  };
  qIn.oninput = () => { clearTimeout(timer); timer = window.setTimeout(() => void load(false), 300); };
  const install = async (b: HTMLButtonElement, h: SearchHit, target: string) => {
    b.disabled = true;
    mount(b, html`<span class="spinner"></span>`);
    try {
      if (type === 'modpack') {
        const r = await api.installModpack(h.project_id, h.title);
        state.settings = await api.setSettings({ selectedProfile: r.profileId });
        toast(`${h.title}: ${t('prof.imported')}`, 'ok');
        void go('profiles');
        return;
      }
      const titles = await api.installProject(target, h.project_id, type, h.title, h.icon_url);
      const name = state.settings.profiles.find((p) => p.id === target)?.name ?? '';
      toast(`${t('mods.installedOk')}: ${titles.join(', ')} → ${name}${type === 'datapack' ? ' (Ordner „datapacks“, in eine Welt kopieren)' : ''}`, 'ok');
      mount(b, html`${icon('check', 'sm')} ${t('mods.installedOk').toUpperCase()}`);
    } catch (err) {
      errorToast(err);
      b.disabled = false;
      mount(b, html`${icon('download', 'sm')} ${t('mods.install').toUpperCase()}`);
    }
  };
  body.onclick = (e) => {
    const el = e.target as HTMLElement;
    if (el.closest('#more')) return void load(true);
    const pick = el.closest<HTMLButtonElement>('[data-pick]');
    if (pick) {
      document.querySelector('.menu')?.remove();
      const m = document.createElement('div');
      m.className = 'menu';
      const r = pick.getBoundingClientRect();
      m.style.cssText = `position:fixed;top:${r.bottom + 4}px;right:${window.innerWidth - r.right}px;z-index:1000`;
      mount(m, html`<div class="menu-h">Installieren in</div>${state.settings.profiles.map((p) => html`<button class="menu-i" data-to="${p.id}">${icon('profiles', 'sm')} ${p.name} <small class="muted">${p.gameVersion}</small></button>`)}`);
      document.body.append(m);
      m.addEventListener('click', (ev) => {
        const to = (ev.target as HTMLElement).closest<HTMLButtonElement>('[data-to]');
        if (!to) return;
        m.remove();
        const btn = body.querySelector<HTMLButtonElement>(`[data-i="${pick.dataset.pick}"]`)!;
        void install(btn, hits[Number(pick.dataset.pick)]!, to.dataset.to!);
      });
      setTimeout(() => document.addEventListener('click', () => m.remove(), { once: true }));
      return;
    }
    const b = el.closest<HTMLButtonElement>('[data-i]');
    if (b) void install(b, hits[Number(b.dataset.i)]!, id);
  };
  void load(false);
  return () => void load(false);
}
