// Designs: the launcher's personalisation studio. Every change applies to the
// whole window immediately (the live background and UI are the preview) and
// is saved shortly after. Scenes, colours/themes, effects, presets and the
// theme editor live here; presets can be saved, loaded, exported and imported.

import {
  BACKGROUNDS, FONTS, MOTIONS, RARITY_INFO, SHADOWS, THEME_FAMILIES, readabilityWarnings, rederive, validateTheme,
  type Theme, type ThemeColors,
} from '@lego/shared';
import { retheme, state } from '../app.ts';
import { api, confirm, errorToast, html, mount, toast, type Raw } from '../ui.ts';
import { allThemes, applyTheme, findTheme } from '../theme.ts';
import { icon } from '../icons.ts';
import { BACKGROUNDS as LAUNCHER_BACKGROUNDS, drawPreview } from '../bg.ts';
import { is3D, thumbnail } from '../bg3d.ts';
import { getLang, t as tr } from '../i18n.ts';
import { DEFAULT_FX, sanitizePreset, type DesignFx, type DesignPreset } from '../../core/design.ts';
import type { Settings } from '../../core/settings.ts';

type Tab = 'scene' | 'colors' | 'effects' | 'presets' | 'editor';
const TABS: [Tab, string, string][] = [
  ['scene', 'image', 'Szene'],
  ['colors', 'palette', 'Farben & Themes'],
  ['effects', 'sliders', 'Effekte'],
  ['presets', 'star', 'Presets'],
  ['editor', 'brush', 'Theme-Editor'],
];
const ACCENTS = ['#ff7a1a', '#e11d48', '#8b5cf6', '#c084fc', '#39ff88', '#d946ef', '#6366f1', '#3b82f6', '#06b6d4', '#14b8a6', '#22c55e', '#eab308', '#e3000b', '#a1a1aa'];

/** Starting points that ship with the launcher. */
const BUILT_IN: DesignPreset[] = [
  { id: 'nocturne', name: 'Nocturne', themeId: 'halloween', accentColor: null, background: 'graveyard', snow: false, fx: { ...DEFAULT_FX }, surface: 'glass', stage: 'portal' },
  { id: 'blood-moon', name: 'Blutmond', themeId: 'halloween', accentColor: '#e11d48', background: 'castle', snow: false, fx: { ...DEFAULT_FX, light: 0.8, particles: 0.5 }, surface: 'glass', stage: 'portal' },
  { id: 'will-o-wisp', name: 'Irrlicht', themeId: 'halloween', accentColor: '#39ff88', background: 'forest', snow: false, fx: { ...DEFAULT_FX, particles: 0.9, speed: 0.8 }, surface: 'glass', stage: 'portal' },
  { id: 'hex-portal', name: 'Hexenportal', themeId: 'halloween', accentColor: '#c084fc', background: 'portal', snow: false, fx: { ...DEFAULT_FX, light: 1.2 }, surface: 'neon', stage: 'portal' },
  { id: 'synth-night', name: 'Synth-Nacht', themeId: 'halloween', accentColor: '#d946ef', background: 'scifi', snow: false, fx: { ...DEFAULT_FX, speed: 1.2 }, surface: 'neon', stage: 'plain' },
  { id: 'calm', name: 'Ruhig', themeId: 'halloween', accentColor: null, background: 'rooms', snow: false, fx: { ...DEFAULT_FX, particles: 0.2, speed: 0.5, glow: false, parallax: false }, surface: 'solid', stage: 'plain' },
];

function page(box: HTMLElement): HTMLElement {
  return box.closest<HTMLElement>('.view-enter') ?? (box.parentElement as HTMLElement);
}

let saveTimer = 0;
let pending: Partial<Settings> = {};
/** Applies at once (live preview) and persists all pending changes after a short pause. */
function live(patch: Partial<Settings>): void {
  state.settings = { ...state.settings, ...patch };
  pending = { ...pending, ...patch };
  retheme();
  clearTimeout(saveTimer);
  saveTimer = window.setTimeout(() => {
    const send = pending;
    pending = {};
    void api.setSettings(send).then((s) => {
      // A newer change may be waiting; keep it on top of the saved state.
      state.settings = { ...s, ...pending };
    }).catch(errorToast);
  }, 250);
}
async function saveNow(patch: Partial<Settings>): Promise<void> {
  clearTimeout(saveTimer);
  patch = { ...pending, ...patch };
  pending = {};
  try {
    state.settings = await api.setSettings(patch);
    retheme();
  } catch (e) { errorToast(e); }
}

function current(): DesignPreset {
  const s = state.settings;
  return { id: 'current', name: 'Aktuell', themeId: s.themeId, accentColor: s.accentColor, background: s.background, snow: s.snow, fx: s.fx, surface: s.surface, stage: s.stage };
}

function presetPatch(p: DesignPreset): Partial<Settings> {
  return { themeId: p.themeId, accentColor: p.accentColor, background: p.background, snow: p.snow, fx: p.fx, surface: p.surface, stage: p.stage };
}

export async function designsPage(el: HTMLElement, tab: Tab | string = 'scene'): Promise<void> {
  const sec = (TABS.find(([id]) => id === tab)?.[0] ?? 'scene') as Tab;
  mount(el, html`<div class="page-head"><div><h1>${tr('nav.designs')}</h1><p class="muted small">Alles hier wirkt sofort auf den ganzen Launcher – der Hintergrund und die Oberfläche sind deine Live-Vorschau.</p></div>
      <div class="row"><button class="btn" id="quickSave">${icon('star', 'sm')} Als Preset speichern</button></div></div>
    <div class="designs">
      <nav class="seg design-tabs" id="tabs">${TABS.map(([id, ic, label]) => html`<button data-tab="${id}" class="${id === sec ? 'on' : ''}">${icon(ic, 'sm')} ${label}</button>`)}</nav>
      <div class="design-body" id="tab"></div>
      ${previewCard()}
    </div>`);
  el.querySelector('#tabs')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-tab]');
    if (b) void designsPage(el, b.dataset.tab);
  });
  el.querySelector('#quickSave')!.addEventListener('click', () => void designsPage(el, 'presets').then(() => el.querySelector<HTMLInputElement>('#pname')?.focus()));
  const box = el.querySelector<HTMLElement>('#tab')!;
  if (sec === 'scene') sceneTab(box);
  else if (sec === 'colors') { colorsTab(box); }
  else if (sec === 'effects') effectsTab(box);
  else if (sec === 'presets') presetsTab(box);
  else editorTab(box);
}

function previewCard(): Raw {
  return html`<aside class="card design-preview" aria-label="Vorschau"><div class="row between"><h3>Vorschau</h3><span class="badge">live</span></div>
    <div class="dp-ui">
      <div class="dp-rail"><i class="on"></i><i></i><i></i><i></i></div>
      <div class="dp-main"><div class="dp-card"><b>LEGO Client</b><small>Fabric · 1.21.11</small><div class="progress"><i data-p="62"></i></div></div>
        <div class="dp-card"><div class="row"><button class="btn primary sm" type="button">Spielen</button><button class="btn sm" type="button">Profil</button></div></div></div>
    </div>
    <p class="tiny muted">Panels, Akzentfarbe, Glow und Hintergrund siehst du außerdem überall im Launcher.</p></aside>`;
}

function sceneTab(box: HTMLElement): void {
  const s = state.settings;
  const pal = (() => { const cs = getComputedStyle(document.documentElement); return { bg: cs.getPropertyValue('--bg').trim() || '#07060b', accent: cs.getPropertyValue('--accent').trim() || '#ff7a1a', accent2: cs.getPropertyValue('--accent2').trim() || '#8b5cf6' }; })();
  const three = LAUNCHER_BACKGROUNDS.filter((b) => b.kind === '3d');
  const flat = LAUNCHER_BACKGROUNDS.filter((b) => b.kind === '2d' && b.id !== 'image');
  const tile = (b: (typeof LAUNCHER_BACKGROUNDS)[number]) => html`<button class="bg-tile ${s.background === b.id ? 'on' : ''}" data-bg="${b.id}">${b.kind === '3d' ? html`<img alt="" data-thumb="${b.id}">` : html`<canvas data-preview="${b.id}"></canvas>`}<span>${b.name[getLang()]}${b.kind === '3d' ? html` <em>3D</em>` : ''}</span></button>`;
  mount(box, html`<section class="set-sec"><h2>3D-Szenen</h2><div class="bgpick">${three.map(tile)}
      <label class="bg-tile upload ${s.background === 'image' ? 'on' : ''}" data-bg-image>${s.bgImage ? html`<img alt="" src="${s.bgImage}">` : html`<span class="up-ic">${icon('upload')}</span>`}<span>Eigenes Bild</span><input type="file" id="bgFile" accept="image/png,image/jpeg,image/webp" hidden></label></div></section>
    <section class="set-sec"><h2>Klassisch (2D, sparsam)</h2><div class="bgpick">${flat.map(tile)}</div></section>
    <section class="set-sec"><h2>Qualität</h2><div class="glass rows"><div class="setting"><div><b>${tr('set.quality')}</b><small>${tr('set.quality.d')} (${Math.round(s.backgroundQuality * 100)} %)</small></div><div class="ctl"><input type="range" id="quality" min="0" max="1" step="0.05" value="${s.backgroundQuality}"></div></div></div></section>`);
  box.querySelectorAll<HTMLCanvasElement>('[data-preview]').forEach((c) => requestAnimationFrame(() => drawPreview(c, c.dataset.preview!)));
  // 3D thumbnails one after another so the UI stays responsive.
  const imgs = [...box.querySelectorAll<HTMLImageElement>('[data-thumb]')];
  const next = () => {
    const img = imgs.shift();
    if (!img || !img.isConnected) return;
    try { const id = img.dataset.thumb!; if (is3D(id)) img.src = thumbnail(id, pal); } catch { /* no WebGL: tile stays plain */ }
    setTimeout(next, 30);
  };
  setTimeout(next, 60);
  box.onclick = (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-bg]');
    if (!b) return;
    box.querySelectorAll('.bg-tile').forEach((x) => x.classList.toggle('on', x === b));
    void saveNow({ background: b.dataset.bg });
  };
  box.querySelector('#quality')!.addEventListener('change', (e) => void saveNow({ backgroundQuality: Number((e.target as HTMLInputElement).value) }).then(() => sceneTab(box)));
  box.querySelector('#bgFile')!.addEventListener('change', async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    if (file.size > 8_000_000) return errorToast(new Error('Bild zu groß (max. 8 MB)'));
    const url = await new Promise<string>((r) => { const fr = new FileReader(); fr.onload = () => r(String(fr.result)); fr.readAsDataURL(file); });
    await saveNow({ bgImage: url, background: 'image' });
    if (!state.settings.bgImage) return errorToast(new Error('Dieses Bildformat wird nicht unterstützt (PNG, JPEG oder WebP).'));
    toast('Hintergrundbild gesetzt', 'ok');
    sceneTab(box);
  });
}

function colorsTab(box: HTMLElement): void {
  const s = state.settings;
  mount(box, html`<section class="set-sec"><h2>${tr('set.accent')}</h2><div class="glass rows"><div class="setting stack"><div><small>${tr('set.accent.d')}</small></div>
      <div class="swatches">${ACCENTS.map((c) => html`<button class="swatch ${s.accentColor === c ? 'on' : ''}" data-accent="${c}" data-c="${c}" title="${c}"></button>`)}
        <label class="swatch custom" title="${tr('set.custom')}">${icon('plus', 'xs')}<input type="color" id="customAccent" value="${s.accentColor ?? '#ff7a1a'}"></label>
        <button class="btn sm" data-accent="">${tr('set.reset')}</button></div></div></div></section>
    <section class="set-sec"><h2>Themes</h2><div id="themes"></div></section>`);
  box.onclick = (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-accent]');
    if (!b) return;
    void saveNow({ accentColor: b.dataset.accent || null }).then(() => colorsTab(box));
  };
  box.querySelector('#customAccent')!.addEventListener('input', (e) => live({ accentColor: (e.target as HTMLInputElement).value }));
  themesTab(box.querySelector<HTMLElement>('#themes')!);
}

function effectsTab(box: HTMLElement): void {
  const s = state.settings;
  const fx = s.fx;
  const slider = (key: keyof DesignFx, label: string, desc: string, min: number, max: number, step: number) =>
    html`<div class="setting"><div><b>${label}</b><small>${desc}</small></div><div class="ctl row"><input type="range" data-fx="${key}" min="${min}" max="${max}" step="${step}" value="${String(fx[key])}"><output class="mono small" data-out="${key}">${Number(fx[key]).toFixed(2)}</output></div></div>`;
  const toggle = (key: keyof DesignFx, label: string, desc: string) =>
    html`<div class="setting"><div><b>${label}</b><small>${desc}</small></div><div class="ctl"><label class="switch"><input type="checkbox" data-fx="${key}" ${fx[key] ? 'checked' : ''}><i></i></label></div></div>`;
  const seg = (key: 'surface' | 'stage', opts: [string, string][]) => html`<div class="seg" data-seg="${key}">${opts.map(([v, l]) => html`<button data-v="${v}" class="${s[key] === v ? 'on' : ''}">${l}</button>`)}</div>`;
  mount(box, html`<section class="set-sec"><h2>Bewegung & Licht</h2><div class="glass rows">
      ${slider('particles', 'Partikeldichte', 'Staub, Irrlichter, Funken und Fledermäuse in den 3D-Szenen.', 0, 1, 0.05)}
      ${slider('speed', 'Animationsgeschwindigkeit', 'Wie schnell sich Hintergrund und Effekte bewegen.', 0.25, 2, 0.05)}
      ${slider('light', 'Lichtintensität', 'Helligkeit von Mond, Laternen, Portalen und Glühen.', 0.2, 1.6, 0.05)}</div></section>
    <section class="set-sec"><h2>Effekte</h2><div class="glass rows">
      ${toggle('fog', 'Nebel', 'Bodennebel und Tiefen-Dunst in den 3D-Szenen.')}
      ${toggle('glow', 'Glühen (Bloom)', 'Leuchtende Lichter und Neon-Schein. Aus = weniger GPU-Last.')}
      ${toggle('vignette', 'Vignette', 'Abgedunkelte Ränder für mehr Tiefe.')}
      ${toggle('parallax', 'Parallax', 'Die Kamera folgt leicht der Maus – echte räumliche Tiefe.')}
      <div class="setting"><div><b>${tr('set.snow')}</b><small>${tr('set.snow.d')}</small></div><div class="ctl"><label class="switch"><input type="checkbox" id="snow" ${s.snow ? 'checked' : ''}><i></i></label></div></div></div></section>
    <section class="set-sec"><h2>Oberfläche & Charakter</h2><div class="glass rows">
      <div class="setting"><div><b>Oberflächenstil</b><small>Glas (durchscheinend), Massiv (ruhig, schnell) oder Neon (leuchtende Kanten).</small></div><div class="ctl">${seg('surface', [['glass', 'Glas'], ['solid', 'Massiv'], ['neon', 'Neon']])}</div></div>
      <div class="setting"><div><b>Charakter-Bühne</b><small>Portal-Ringe und Gitterboden hinter deinem Charakter oder eine schlichte Bühne.</small></div><div class="ctl">${seg('stage', [['portal', 'Portal'], ['plain', 'Schlicht']])}</div></div>
      <div class="setting"><div><b>${tr('set.motion')}</b><small>${tr('set.motion.d')}</small></div><div class="ctl"><label class="switch"><input type="checkbox" id="reduced" ${s.reducedMotion ? 'checked' : ''}><i></i></label></div></div>
      <div class="setting"><div><b>Zurücksetzen</b><small>Alle Effekte auf die Standardwerte.</small></div><div class="ctl"><button class="btn sm" id="fxReset">${tr('set.reset')}</button></div></div></div></section>`);
  box.oninput = (e) => {
    const i = e.target as HTMLInputElement;
    const key = i.dataset.fx as keyof DesignFx | undefined;
    if (!key || i.type !== 'range') return;
    box.querySelector(`[data-out=${key}]`)!.textContent = Number(i.value).toFixed(2);
    live({ fx: { ...state.settings.fx, [key]: Number(i.value) } });
  };
  box.onchange = (e) => {
    const i = e.target as HTMLInputElement;
    const key = i.dataset.fx as keyof DesignFx | undefined;
    if (key && i.type === 'checkbox') live({ fx: { ...state.settings.fx, [key]: i.checked } });
    if (i.id === 'snow') live({ snow: i.checked });
    if (i.id === 'reduced') live({ reducedMotion: i.checked });
  };
  box.onclick = (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    const key = b.parentElement?.dataset.seg as 'surface' | 'stage' | undefined;
    if (key) { live({ [key]: b.dataset.v } as Partial<Settings>); b.parentElement!.querySelectorAll('button').forEach((x) => x.classList.toggle('on', x === b)); }
    if (b.id === 'fxReset') { live({ fx: { ...DEFAULT_FX } }); effectsTab(box); }
  };
}

function presetsTab(box: HTMLElement): void {
  const mine = state.settings.designPresets;
  const card = (p: DesignPreset, own: boolean) => html`<article class="preset" data-c="${p.accentColor ?? '#ff7a1a'}">
      <div class="pr-sw"><i data-c="${findTheme(p.themeId, state.settings.customThemes).colors.bg}"></i><i data-c="${p.accentColor ?? findTheme(p.themeId, state.settings.customThemes).colors.accent}"></i><i data-c="${findTheme(p.themeId, state.settings.customThemes).colors.accent2}"></i></div>
      <div class="pr-meta"><b>${p.name}</b><small>${LAUNCHER_BACKGROUNDS.find((b) => b.id === p.background)?.name[getLang()] ?? p.background} · ${p.surface}</small></div>
      <div class="row"><button class="btn sm primary" data-apply="${p.id}" data-own="${own ? '1' : ''}">Anwenden</button>${own ? html`<button class="icon-btn" data-export="${p.id}" title="Exportieren">${icon('download', 'sm')}</button><button class="icon-btn danger" data-del="${p.id}" title="Löschen">${icon('trash', 'sm')}</button>` : ''}</div></article>`;
  mount(box, html`<section class="set-sec"><h2>Aktuelles Design speichern</h2><div class="glass rows"><div class="setting"><div class="grow"><input id="pname" maxlength="40" placeholder="Name, z. B. Kürbisnacht"></div><div class="ctl row"><button class="btn primary" id="psave">${icon('star', 'sm')} Speichern</button><label class="btn">${icon('import', 'sm')} Importieren<input type="file" id="pimport" accept="application/json" hidden></label></div></div></div></section>
    <section class="set-sec"><h2>Meine Presets (${mine.length})</h2>${mine.length ? html`<div class="presets">${mine.map((p) => card(p, true))}</div>` : html`<div class="empty">Noch keine eigenen Presets.</div>`}</section>
    <section class="set-sec"><h2>Mitgeliefert</h2><div class="presets">${BUILT_IN.map((p) => card(p, false))}</div></section>`);
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    if (b.id === 'psave') {
      const name = box.querySelector<HTMLInputElement>('#pname')!.value.trim();
      if (!name) return errorToast(new Error('Bitte einen Namen eingeben.'));
      const p = sanitizePreset({ ...current(), id: `p-${Date.now().toString(36)}`, name });
      if (!p) return errorToast(new Error('Preset ungültig.'));
      await saveNow({ designPresets: [...mine, p].slice(-30) });
      toast(`Preset „${p.name}“ gespeichert`, 'ok');
      return presetsTab(box);
    }
    if (b.dataset.apply) {
      const p = (b.dataset.own ? mine : BUILT_IN).find((x) => x.id === b.dataset.apply);
      if (p) { await saveNow(presetPatch(p)); toast(`„${p.name}“ angewendet`, 'ok'); }
      return;
    }
    if (b.dataset.export) {
      const p = mine.find((x) => x.id === b.dataset.export);
      if (!p) return;
      const a = document.createElement('a');
      a.href = URL.createObjectURL(new Blob([JSON.stringify(p, null, 2)], { type: 'application/json' }));
      a.download = `${p.name.replace(/[^\w-]+/g, '_')}.lego-design.json`;
      a.click();
      URL.revokeObjectURL(a.href);
      return;
    }
    if (b.dataset.del && await confirm('Preset löschen?', 'Das Preset wird entfernt.', 'Löschen', true)) {
      await saveNow({ designPresets: mine.filter((x) => x.id !== b.dataset.del) });
      presetsTab(box);
    }
  };
  box.querySelector('#pimport')!.addEventListener('change', async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file || file.size > 200_000) return errorToast(new Error('Datei zu groß oder leer.'));
    try {
      const p = sanitizePreset({ ...JSON.parse(await file.text()), id: `p-${Date.now().toString(36)}` });
      if (!p) return errorToast(new Error('Das ist kein gültiges Design-Preset.'));
      await saveNow({ designPresets: [...mine, p].slice(-30) });
      toast(`Preset „${p.name}“ importiert`, 'ok');
      presetsTab(box);
    } catch (err) { errorToast(err); }
  });
}

function themesTab(box: HTMLElement): void {
  let family = '';
  let q = '';
  const draw = () => {
    const list = allThemes(state.settings.customThemes).filter((t) => (!family || t.family === family) && (!q || t.name.toLowerCase().includes(q)));
    mount(box.querySelector('#grid')!, html`${list.map((t) => html`<button class="theme-tile ${t.id === state.settings.themeId ? 'active' : ''}" data-theme="${t.id}">
      <div class="sw"><i data-c="${t.colors.bg}"></i><i data-c="${t.colors.accent}"></i><i data-c="${t.colors.accent2}"></i></div>
      <div class="meta"><b>${t.name}</b><div class="muted">${t.family} · ${t.font} · ${t.background.kind}</div></div></button>`)}`);
  };
  mount(box, html`<p class="muted">${allThemes(state.settings.customThemes).length} Designs. Klick zum Anwenden – jedes Design ändert Farben, Rundungen, Schrift, Hintergrund, Schatten und Animationen.</p>
    <div class="row"><input id="q" class="grow" placeholder="Suchen…"><select id="fam"><option value="">Alle Stile</option>${THEME_FAMILIES.map((f) => html`<option>${f}</option>`)}</select></div>
    <div class="grid g4 gap" id="grid"></div>`);
  box.querySelector('#q')!.addEventListener('input', (e) => { q = (e.target as HTMLInputElement).value.toLowerCase(); draw(); });
  box.querySelector('#fam')!.addEventListener('change', (e) => { family = (e.target as HTMLSelectElement).value; draw(); });
  box.querySelector('#grid')!.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-theme]');
    if (!b) return;
    state.settings = await api.setSettings({ themeId: b.dataset.theme });
    retheme();
    draw();
  });
  draw();
}

function editorTab(box: HTMLElement): void {
  const base = findTheme(state.settings.themeId, state.settings.customThemes);
  let t: Theme = structuredClone(base);
  if (!t.id.startsWith('custom-')) { t.id = `custom-${Date.now().toString(36)}`; t.name = `${base.name} (eigenes)`; }
  const colorKeys: (keyof ThemeColors)[] = ['bg', 'surface', 'text', 'accent', 'accent2'];
  const preview = () => applyTheme(t, { reducedMotion: state.settings.reducedMotion, uiScale: state.settings.uiScale });
  const warn = () => { mount(box.querySelector('#warn')!, html`${readabilityWarnings(t).map((w) => html`<div class="small">${icon('info', 'xs')} ${w}</div>`)}`); };
  mount(box, html`<div class="grid g2"><form class="card" id="f">
      <label>Name</label><input name="name" value="${t.name}" maxlength="40">
      <div class="grid g3">${colorKeys.map((k) => html`<div><label>${k}</label><input type="color" name="c_${k}" value="${t.colors[k]}"></div>`)}</div>
      <label>Modus</label><select name="mode"><option value="dark" ${t.mode === 'dark' ? 'selected' : ''}>Dunkel</option><option value="light" ${t.mode === 'light' ? 'selected' : ''}>Hell</option></select>
      <label>Rundung (${t.radius}px)</label><input type="range" name="radius" min="0" max="32" value="${t.radius}">
      <label>Glas-Unschärfe (${t.blur}px)</label><input type="range" name="blur" min="0" max="40" value="${t.blur}">
      <label>Transparenz der Panels</label><input type="range" name="surfaceAlpha" min="0.2" max="1" step="0.05" value="${t.surfaceAlpha}">
      <label>Rahmenstärke</label><input type="range" name="borderWidth" min="0" max="4" value="${t.borderWidth}">
      <div class="grid g3">
        <div><label>Schatten</label><select name="shadow">${SHADOWS.map((s) => html`<option ${s === t.shadow ? 'selected' : ''}>${s}</option>`)}</select></div>
        <div><label>Schrift</label><select name="font">${FONTS.map((s) => html`<option ${s === t.font ? 'selected' : ''}>${s}</option>`)}</select></div>
        <div><label>Animationen</label><select name="motion">${MOTIONS.map((s) => html`<option ${s === t.motion ? 'selected' : ''}>${s}</option>`)}</select></div>
      </div>
      <label>Hintergrund</label><select name="bgkind">${BACKGROUNDS.map((s) => html`<option ${s === t.background.kind ? 'selected' : ''}>${s}</option>`)}</select>
      <label>Hintergrund-Intensität</label><input type="range" name="intensity" min="0" max="1" step="0.05" value="${t.background.intensity}">
      <label>Eigenes Hintergrundbild (PNG/JPEG/WebP, max. 6 MB)</label><input type="file" name="image" accept="image/png,image/jpeg,image/webp">
      <div id="warn" class="gap"></div>
      <div class="row gap"><button class="btn primary">Speichern & anwenden</button><button type="button" class="btn" id="export">Exportieren</button><label class="btn">Importieren<input type="file" id="import" accept="application/json" hidden></label><button type="button" class="btn" id="reset">Verwerfen</button></div>
    </form>
    <div class="card"><h3>Vorschau</h3><p>Die Änderungen werden sofort auf den ganzen Launcher angewendet.</p><div class="row"><button class="btn primary" type="button">Primär</button><button class="btn" type="button">Sekundär</button><span class="badge c" data-c="${RARITY_INFO.legendary.color}">Legendär</span></div>
      <div class="progress gap"><i data-p="64"></i></div><p class="small muted gap">Eigene Designs: ${state.settings.customThemes.length}</p>
      ${state.settings.customThemes.length ? html`<div class="row">${allThemes(state.settings.customThemes).filter((x) => x.id.startsWith('custom-')).map((x) => html`<button class="btn sm danger" data-delete="${x.id}">${x.name} löschen</button>`)}</div>` : ''}</div></div>`);
  const f = box.querySelector<HTMLFormElement>('#f')!;
  f.addEventListener('input', async (e) => {
    const fd = new FormData(f);
    const colors = { ...t.colors };
    for (const k of colorKeys) colors[k] = String(fd.get(`c_${k}`));
    t = rederive({
      ...t, name: String(fd.get('name') || t.name), mode: fd.get('mode') === 'light' ? 'light' : 'dark', colors,
      radius: Number(fd.get('radius')), blur: Number(fd.get('blur')), surfaceAlpha: Number(fd.get('surfaceAlpha')), borderWidth: Number(fd.get('borderWidth')),
      shadow: String(fd.get('shadow')) as Theme['shadow'], font: String(fd.get('font')) as Theme['font'], motion: String(fd.get('motion')) as Theme['motion'],
      background: { ...t.background, kind: String(fd.get('bgkind')) as Theme['background']['kind'], intensity: Number(fd.get('intensity')) },
    });
    const file = (e.target as HTMLInputElement).name === 'image' ? (e.target as HTMLInputElement).files?.[0] : undefined;
    if (file) {
      if (file.size > 6_000_000) return errorToast(new Error('Bild zu groß (max. 6 MB)'));
      t.background = { kind: 'image', intensity: Math.max(t.background.intensity, 0.6), image: await new Promise<string>((r) => { const fr = new FileReader(); fr.onload = () => r(String(fr.result)); fr.readAsDataURL(file); }) };
      (f.elements.namedItem('bgkind') as HTMLSelectElement).value = 'image';
    }
    preview();
    warn();
  });
  f.addEventListener('submit', async (e) => {
    e.preventDefault();
    const v = validateTheme(t);
    if (!v.ok) return errorToast(new Error(v.errors.join('; ')));
    const custom = state.settings.customThemes.filter((x) => (x as { id?: string }).id !== t.id);
    state.settings = await api.setSettings({ customThemes: [...custom, v.theme], themeId: v.theme.id });
    retheme();
    toast('Design gespeichert', 'ok');
  });
  box.querySelector('#export')!.addEventListener('click', () => {
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([JSON.stringify(t, null, 2)], { type: 'application/json' }));
    a.download = `${t.id}.lego-theme.json`;
    a.click();
    URL.revokeObjectURL(a.href);
  });
  box.querySelector('#import')!.addEventListener('change', async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file || file.size > 9_000_000) return;
    try {
      const v = validateTheme(JSON.parse(await file.text()));
      if (!v.ok) return errorToast(new Error(`Ungültiges Design: ${v.errors.join('; ')}`));
      const theme = { ...v.theme, id: v.theme.id.startsWith('custom-') ? v.theme.id : `custom-${v.theme.id}`.slice(0, 48) };
      state.settings = await api.setSettings({ customThemes: [...state.settings.customThemes.filter((x) => (x as { id?: string }).id !== theme.id), theme], themeId: theme.id });
      retheme();
      toast(`Design „${theme.name}“ importiert`, 'ok');
      void designsPage(page(box), 'colors');
    } catch (err) { errorToast(err); }
  });
  box.querySelector('#reset')!.addEventListener('click', () => { retheme(); void designsPage(page(box), 'editor'); });
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-delete]');
    if (!b || !(await confirm('Design löschen?', 'Das eigene Design wird entfernt.', 'Löschen', true))) return;
    const custom = state.settings.customThemes.filter((x) => (x as { id?: string }).id !== b.dataset.delete);
    state.settings = await api.setSettings({ customThemes: custom, themeId: state.settings.themeId === b.dataset.delete ? 'halloween' : state.settings.themeId });
    retheme();
    void designsPage(page(box), 'editor');
  };
  warn();
}

