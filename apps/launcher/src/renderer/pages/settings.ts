import {
  BACKGROUNDS, FONTS, MOTIONS, RARITY_INFO, SHADOWS, THEME_FAMILIES, readabilityWarnings, rederive, validateTheme,
  type Rarity, type Theme, type ThemeColors,
} from '@lego/shared';
import { retheme, state } from '../app.ts';
import { api, confirm, errorToast, html, mount, toast } from '../ui.ts';
import { allThemes, applyTheme, findTheme } from '../theme.ts';

export async function settingsPage(el: HTMLElement, tab = 'themes'): Promise<void> {
  const tabs: [string, string][] = [['themes', 'Designs'], ['editor', 'Theme-Editor'], ['general', 'Allgemein'], ['rarity', 'Seltenheiten'], ['about', 'Über & Updates']];
  mount(el, html`<h1>Einstellungen</h1><div class="tabs" id="tabs">${tabs.map(([id, l]) => html`<button data-tab="${id}" class="${id === tab ? 'active' : ''}">${l}</button>`)}</div><div id="tab"></div>`);
  el.querySelector('#tabs')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-tab]');
    if (b) void settingsPage(el, b.dataset.tab);
  });
  const box = el.querySelector<HTMLElement>('#tab')!;
  if (tab === 'themes') themesTab(box);
  else if (tab === 'editor') editorTab(box);
  else if (tab === 'general') await generalTab(box);
  else if (tab === 'rarity') rarityTab(box);
  else await aboutTab(box);
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
  const warn = () => { mount(box.querySelector('#warn')!, html`${readabilityWarnings(t).map((w) => html`<div class="small">⚠ ${w}</div>`)}`); };
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
      void settingsPage(box.parentElement as HTMLElement, 'themes');
    } catch (err) { errorToast(err); }
  });
  box.querySelector('#reset')!.addEventListener('click', () => { retheme(); void settingsPage(box.parentElement as HTMLElement, 'editor'); });
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-delete]');
    if (!b || !(await confirm('Design löschen?', 'Das eigene Design wird entfernt.', 'Löschen', true))) return;
    const custom = state.settings.customThemes.filter((x) => (x as { id?: string }).id !== b.dataset.delete);
    state.settings = await api.setSettings({ customThemes: custom, themeId: state.settings.themeId === b.dataset.delete ? 'nightfall' : state.settings.themeId });
    retheme();
    void settingsPage(box.parentElement as HTMLElement, 'editor');
  };
  warn();
}

async function generalTab(box: HTMLElement): Promise<void> {
  const s = state.settings;
  mount(box, html`<form class="card" id="f">
    <label>Sprache</label><select name="language"><option value="de" ${s.language === 'de' ? 'selected' : ''}>Deutsch</option><option value="en" ${s.language === 'en' ? 'selected' : ''}>English (in Arbeit)</option></select>
    <label>UI-Skalierung (${Math.round(s.uiScale * 100)} %) – für hohe Auflösungen bis 4K</label><input type="range" name="uiScale" min="0.75" max="2" step="0.05" value="${s.uiScale}">
    <label class="check gap"><input type="checkbox" name="reducedMotion" ${s.reducedMotion ? 'checked' : ''}> Animationen reduzieren</label>
    <label class="check"><input type="checkbox" name="closeOnLaunch" ${s.closeOnLaunch ? 'checked' : ''}> Launcher beim Spielstart minimieren</label>
    <label>Updates</label><select name="autoUpdate"><option value="ask" ${s.autoUpdate === 'ask' ? 'selected' : ''}>Nachfragen</option><option value="auto" ${s.autoUpdate === 'auto' ? 'selected' : ''}>Automatisch herunterladen</option><option value="off" ${s.autoUpdate === 'off' ? 'selected' : ''}>Nicht prüfen</option></select>
    <label>LEGO-Server (erweitert, nur https)</label><input name="backendUrl" value="${s.backendUrl}" placeholder="https://…">
    <div class="row gap"><button class="btn primary">Speichern</button><button type="button" class="btn" id="game">Spielordner öffnen</button></div></form>`);
  box.querySelector('#f')!.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target as HTMLFormElement);
    state.settings = await api.setSettings({
      language: fd.get('language') === 'en' ? 'en' : 'de', uiScale: Number(fd.get('uiScale')), reducedMotion: fd.get('reducedMotion') === 'on',
      closeOnLaunch: fd.get('closeOnLaunch') === 'on', autoUpdate: String(fd.get('autoUpdate')) as 'ask', backendUrl: String(fd.get('backendUrl') ?? '').trim(),
    });
    retheme();
    toast('Gespeichert', 'ok');
  });
  box.querySelector('#game')!.addEventListener('click', () => void api.openFolder('game'));
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
