// Settings in the style of a modern launcher: section navigation with search,
// instant-apply rows (toggle / select / slider), accent colours, animated
// background picker, accounts, themes, theme editor, rarities and about.

import {
  BACKGROUNDS, FONTS, MOTIONS, RARITY_INFO, SHADOWS, THEME_FAMILIES, readabilityWarnings, rederive, validateTheme,
  type Rarity, type Theme, type ThemeColors,
} from '@lego/shared';
import { loadIdentity, retheme, state } from '../app.ts';
import { api, confirm, errorToast, html, mount, toast, type Raw } from '../ui.ts';
import { allThemes, applyTheme, findTheme } from '../theme.ts';
import { icon } from '../icons.ts';
import { BACKGROUNDS as LAUNCHER_BACKGROUNDS, drawPreview } from '../bg.ts';
import { headFromSkin } from '../avatar.ts';
import { getLang, t } from '../i18n.ts';
import type { Settings } from '../../core/settings.ts';
import { signInFlow } from './account.ts';

type Section = 'general' | 'appearance' | 'background' | 'game' | 'accounts' | 'themes' | 'editor' | 'rarity' | 'advanced' | 'about';
const SECTIONS: [Section, string, () => string][] = [
  ['general', 'sliders', () => t('set.general')],
  ['appearance', 'palette', () => t('set.appearance')],
  ['background', 'image', () => t('set.background')],
  ['themes', 'star', () => t('set.themes')],
  ['editor', 'palette', () => t('set.editor')],
  ['game', 'gamepad', () => t('set.game')],
  ['accounts', 'users', () => t('set.accounts')],
  ['rarity', 'cosmetics', () => t('set.rarity')],
  ['advanced', 'terminal', () => t('set.advanced')],
  ['about', 'info', () => t('set.about')],
];
/** Sections made of searchable rows. */
const ROW_SECTIONS: Section[] = ['general', 'appearance', 'background', 'game', 'advanced'];
const ACCENTS = ['#d946ef', '#8b5cf6', '#6366f1', '#3b82f6', '#06b6d4', '#14b8a6', '#22c55e', '#84cc16', '#eab308', '#f97316', '#ef4444', '#ec4899', '#e3000b', '#a1a1aa'];

function page(box: HTMLElement): HTMLElement {
  return box.closest<HTMLElement>('.view-enter') ?? (box.parentElement as HTMLElement);
}

async function save(patch: Partial<Settings>): Promise<void> {
  try {
    state.settings = await api.setSettings(patch);
    retheme();
  } catch (e) { errorToast(e); }
}

function row(title: string, desc: string, control: Raw, extra = ''): Raw {
  return html`<div class="setting ${extra}" data-s="${`${title} ${desc}`.toLowerCase()}"><div><b>${title}</b><small>${desc}</small></div><div class="ctl">${control}</div></div>`;
}
const sw = (key: keyof Settings, on: boolean) => html`<label class="switch"><input type="checkbox" data-key="${key}" ${on ? 'checked' : ''}><i></i></label>`;

export async function settingsPage(el: HTMLElement, section: Section | string = 'general'): Promise<void> {
  const sec = (SECTIONS.find(([id]) => id === section)?.[0] ?? 'general') as Section;
  mount(el, html`<div class="settings">
    <aside class="set-nav">
      <h1>${t('set.title')}</h1>
      <div class="searchbar sm">${icon('search', 'sm')}<input id="sq" placeholder="${t('set.search')}" autocomplete="off"></div>
      <nav id="tabs">${SECTIONS.map(([id, ic, label]) => html`<button data-tab="${id}" class="${id === sec ? 'active' : ''}">${icon(ic, 'sm')}<span>${label()}</span></button>`)}</nav>
    </aside>
    <div class="set-body" id="tab"></div></div>`);
  el.querySelector('#tabs')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-tab]');
    if (b) void settingsPage(el, b.dataset.tab);
  });
  const box = el.querySelector<HTMLElement>('#tab')!;
  el.querySelector('#sq')!.addEventListener('input', (e) => {
    const q = (e.target as HTMLInputElement).value.trim().toLowerCase();
    if (!q) return void settingsPage(el, sec);
    el.querySelectorAll('#tabs button').forEach((b) => b.classList.remove('active'));
    rowSections(box, q);
  });
  if (ROW_SECTIONS.includes(sec)) rowSections(box, '', sec);
  else if (sec === 'themes') themesTab(box);
  else if (sec === 'editor') editorTab(box);
  else if (sec === 'accounts') await accountsTab(box);
  else if (sec === 'rarity') rarityTab(box);
  else await aboutTab(box);
}

/** Renders one row section, or all of them filtered by a search query. */
function rowSections(box: HTMLElement, q: string, only?: Section): void {
  const s = state.settings;
  const parts: Record<string, Raw> = {
    general: html`
      ${row(t('set.language'), t('set.language.d'), html`<div class="seg" data-seg="language"><button data-v="de" class="${s.language === 'de' ? 'on' : ''}">Deutsch</button><button data-v="en" class="${s.language === 'en' ? 'on' : ''}">English</button></div>`)}
      ${row(t('set.closeOnLaunch'), t('set.closeOnLaunch.d'), sw('closeOnLaunch', s.closeOnLaunch))}
      ${row(t('set.updates'), t('set.updates.d'), html`<select data-key="autoUpdate" class="compact"><option value="ask" ${s.autoUpdate === 'ask' ? 'selected' : ''}>${t('set.upd.ask')}</option><option value="auto" ${s.autoUpdate === 'auto' ? 'selected' : ''}>${t('set.upd.auto')}</option><option value="off" ${s.autoUpdate === 'off' ? 'selected' : ''}>${t('set.upd.off')}</option></select>`)}`,
    appearance: html`
      ${row(t('set.accent'), t('set.accent.d'), html`<div class="swatches">${ACCENTS.map((c) => html`<button class="swatch ${s.accentColor === c ? 'on' : ''}" data-accent="${c}" data-c="${c}" title="${c}"></button>`)}
        <label class="swatch custom" title="${t('set.custom')}">${icon('plus', 'xs')}<input type="color" id="customAccent" value="${s.accentColor ?? '#d946ef'}"></label>
        <button class="btn sm" data-accent="">${t('set.reset')}</button></div>`, 'stack')}
      ${row(t('set.scale'), `${t('set.scale.d')} (${Math.round(s.uiScale * 100)} %)`, html`<input type="range" data-key="uiScale" min="0.75" max="2" step="0.05" value="${s.uiScale}">`)}
      ${row(t('set.motion'), t('set.motion.d'), sw('reducedMotion', s.reducedMotion))}`,
    background: html`
      ${row(t('set.bgPick'), t('set.bgPick.d'), html`<div class="bg-grid">${LAUNCHER_BACKGROUNDS.map((b) => html`<button class="bg-tile ${s.background === b.id ? 'on' : ''}" data-bg="${b.id}"><canvas data-preview="${b.id}"></canvas><span>${b.name[getLang()]}</span></button>`)}</div>`, 'stack')}
      ${row(t('set.snow'), t('set.snow.d'), sw('snow', s.snow))}
      ${row(t('set.quality'), `${t('set.quality.d')} (${Math.round(s.backgroundQuality * 100)} %)`, html`<input type="range" data-key="backgroundQuality" min="0" max="1" step="0.05" value="${s.backgroundQuality}">`)}`,
    game: html`
      ${row(t('set.snapshots'), t('set.snapshots.d'), sw('showSnapshots', s.showSnapshots))}
      ${row(t('set.historical'), t('set.historical.d'), sw('showHistorical', s.showHistorical))}
      ${row(t('set.cfKey'), t('set.cfKey.d'), html`<input type="password" data-key="curseforgeKey" value="${s.curseforgeKey}" placeholder="$2a$10$…" autocomplete="off" class="compact wide">`)}`,
    advanced: html`
      ${row(t('set.msClient'), t('set.msClient.d'), html`<input data-key="msClientId" value="${s.msClientId}" placeholder="00000000-0000-0000-0000-000000000000" autocomplete="off" spellcheck="false" class="compact wide mono">`)}
      ${row(t('set.backend'), t('set.backend.d'), html`<input data-key="backendUrl" value="${s.backendUrl}" placeholder="https://…" class="compact wide">`)}
      ${row(t('set.gameDir'), `${t('set.gameDir.d')} ${state.info.gameDir}`, html`<button class="btn sm" id="game">${icon('folder', 'sm')} ${t('set.open')}</button>`)}
      ${row(t('set.diag'), t('set.diag.d'), html`<button class="btn sm" id="diag">${t('set.copy')}</button>`)}`,
  };
  const titles: Record<string, string> = { general: t('set.general'), appearance: t('set.appearance'), background: t('set.background'), game: t('set.game'), advanced: t('set.advanced') };
  const list = only ? [only] : ROW_SECTIONS;
  mount(box, html`${list.map((id) => html`<section class="set-sec" data-sec="${id}"><h2>${titles[id]}</h2><div class="glass rows">${parts[id]!}</div></section>`)}`);
  if (q) {
    box.querySelectorAll<HTMLElement>('.setting').forEach((r) => { r.hidden = !r.dataset.s!.includes(q); });
    box.querySelectorAll<HTMLElement>('.set-sec').forEach((sec) => { sec.hidden = !sec.querySelector('.setting:not([hidden])'); });
    if (!box.querySelector('.set-sec:not([hidden])')) mount(box, html`<div class="empty">Keine Einstellung gefunden.</div>`);
  }
  box.querySelectorAll<HTMLCanvasElement>('[data-preview]').forEach((c) => requestAnimationFrame(() => drawPreview(c, c.dataset.preview!)));
  const rerender = () => rowSections(box, q, only);
  box.onchange = async (e) => {
    const i = e.target as HTMLInputElement;
    if (i.id === 'customAccent') { await save({ accentColor: i.value }); return rerender(); }
    const key = i.dataset.key as keyof Settings | undefined;
    if (!key) return;
    const value = i.type === 'checkbox' ? i.checked : i.type === 'range' ? Number(i.value) : i.value.trim();
    await save({ [key]: value } as Partial<Settings>);
    if (key === 'msClientId') state.info = await api.info();
    if (key === 'backendUrl') { state.auth = await api.reconnectLego(); await loadIdentity(); }
    if (i.type !== 'text' && i.type !== 'password') rerender();
    toast(t('set.saved'), 'ok');
  };
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    if (b.dataset.accent !== undefined) { await save({ accentColor: b.dataset.accent || null }); return rerender(); }
    if (b.dataset.bg) { await save({ background: b.dataset.bg }); return rerender(); }
    if (b.parentElement?.dataset.seg === 'language') { await save({ language: b.dataset.v === 'en' ? 'en' : 'de' }); return void settingsPage(page(box), only ?? 'general'); }
    if (b.id === 'game') return void api.openFolder('game');
    if (b.id === 'diag') { await navigator.clipboard.writeText(await api.diagnostics()); toast('Diagnose in die Zwischenablage kopiert', 'ok'); }
  };
}

async function accountsTab(box: HTMLElement): Promise<void> {
  const a = state.auth;
  mount(box, html`<section class="set-sec"><h2>${t('set.accounts')}</h2><div class="glass rows">
    ${a.accounts.map((x) => html`<div class="setting"><div class="row"><span class="av lg" data-uuid="${x.uuid}"></span><div><b>${x.name}</b><small class="mono">${x.uuid}</small></div></div>
      <div class="ctl row">${x.active ? html`<span class="badge">${icon('check', 'xs')} aktiv</span>` : html`<button class="btn sm" data-switch="${x.uuid}">${icon('swap', 'sm')} Wechseln</button>`}<button class="icon-btn danger" data-remove="${x.uuid}" title="${t('acc.signout')}">${icon('logout', 'sm')}</button></div></div>`)}
    <div class="setting"><div><b>${t('acc.add')}</b><small>Anmeldung über die offizielle Microsoft-Seite. Mehrere Konten werden verschlüsselt gespeichert.</small></div><div class="ctl"><button class="btn primary sm" id="add">${icon('plus', 'sm')} ${t('acc.add')}</button></div></div>
  </div></section>`);
  box.querySelectorAll<HTMLElement>('.av').forEach((el) => {
    void api.skin(el.dataset.uuid).then((s) => headFromSkin(s.skin, 48)).then((src) => { if (src) { const i = new Image(); i.src = src; el.append(i); } });
  });
  box.onchange = null;
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    try {
      if (b.id === 'add') return void signInFlow();
      if (b.dataset.switch) state.auth = await api.switchAccount(b.dataset.switch);
      if (b.dataset.remove) {
        if (!(await confirm(t('acc.signout'), 'Gespeicherte Anmeldedaten dieses Kontos werden gelöscht.', t('acc.signout'), true))) return;
        state.auth = await api.signOut(b.dataset.remove);
      }
      await loadIdentity();
      void accountsTab(box);
    } catch (err) { errorToast(err); }
  };
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
      void settingsPage(page(box), 'themes');
    } catch (err) { errorToast(err); }
  });
  box.querySelector('#reset')!.addEventListener('click', () => { retheme(); void settingsPage(page(box), 'editor'); });
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-delete]');
    if (!b || !(await confirm('Design löschen?', 'Das eigene Design wird entfernt.', 'Löschen', true))) return;
    const custom = state.settings.customThemes.filter((x) => (x as { id?: string }).id !== b.dataset.delete);
    state.settings = await api.setSettings({ customThemes: custom, themeId: state.settings.themeId === b.dataset.delete ? 'lego-graphite' : state.settings.themeId });
    retheme();
    void settingsPage(page(box), 'editor');
  };
  warn();
}

function rarityTab(box: HTMLElement): void {
  const keys = Object.keys(RARITY_INFO) as Rarity[];
  mount(box, html`<form class="card" id="f"><p class="muted">Farben der Seltenheitsstufen im Launcher (Barrierefreiheit, z. B. bei Farbsehschwäche).</p>
    <div class="grid g3">${keys.map((k) => html`<div><label>${RARITY_INFO[k].label.de}</label><input type="color" name="${k}" value="${state.settings.rarityColors[k] ?? RARITY_INFO[k].color}"></div>`)}</div>
    <div class="row gap"><button class="btn primary">Speichern</button><button type="button" class="btn" id="reset">Standard</button></div></form>`);
  box.querySelector('#f')!.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target as HTMLFormElement);
    state.settings = await api.setSettings({ rarityColors: Object.fromEntries(keys.map((k) => [k, String(fd.get(k))])) });
    toast('Gespeichert', 'ok');
  });
  box.querySelector('#reset')!.addEventListener('click', async () => { state.settings = await api.setSettings({ rarityColors: {} }); rarityTab(box); });
}

async function aboutTab(box: HTMLElement): Promise<void> {
  const i = state.info;
  mount(box, html`<div class="grid g2"><div class="card"><h3>LEGO Launcher ${i.version}</h3>
      <p class="small">${i.platform} ${i.arch} · ${i.cpuModel} (${i.cpus} Threads) · ${i.totalMemMb} MB RAM</p>
      <p class="small muted">Spieldaten: <span class="mono">${i.gameDir}</span></p>
      <div class="row gap"><button class="btn primary" id="upd">Nach Updates suchen</button><button class="btn" id="diag">Diagnose kopieren</button></div><div id="updState" class="small gap"></div></div>
    <div class="card"><h3>Datenschutz & Sicherheit</h3><ul class="small"><li>Anmeldung nur über die offizielle Microsoft-Seite.</li><li>Tokens werden mit der Betriebssystem-Verschlüsselung gespeichert${state.auth.secureStorage ? '' : ' (auf diesem System nicht verfügbar)'}.</li><li>Der LEGO-Server erhält nie dein Minecraft-Token (Mojang-Join-Verfahren).</li><li>Alle Downloads werden per SHA-1/SHA-512 geprüft.</li><li>Keine Änderungen an Windows-Einstellungen, Registry oder Diensten.</li></ul></div></div>`);
  box.querySelector('#upd')!.addEventListener('click', async () => {
    const r = await api.checkUpdate();
    const st = box.querySelector('#updState')!;
    if (r.error) st.textContent = r.error;
    else if (r.available) {
      mount(st, html`Version ${r.version} verfügbar. <button class="btn sm primary" id="inst">Herunterladen & installieren</button>`);
      st.querySelector('#inst')!.addEventListener('click', () => void api.installUpdate());
    } else st.textContent = 'Du hast die neueste Version.';
  });
  box.querySelector('#diag')!.addEventListener('click', async () => { await navigator.clipboard.writeText(await api.diagnostics()); toast('Diagnose in die Zwischenablage kopiert', 'ok'); });
}
