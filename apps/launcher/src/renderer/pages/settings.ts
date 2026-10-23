// Settings: section navigation with search, instant-apply rows (toggle /
// select / slider), accounts, rarities and about. Look & feel (themes,
// backgrounds, effects, presets) lives in the Designs page.

import { go, loadIdentity, retheme, state } from '../app.ts';
import { api, confirm, errorToast, html, mount, toast, type Raw } from '../ui.ts';
import { designsPage } from './designs.ts';
import { LANGS } from '../lang.ts';
import { icon } from '../icons.ts';
import { headFromSkin } from '../avatar.ts';
import { languageTemplate, t } from '../i18n.ts';
import { FLAGS } from '../flags.ts';
import { sanitizeLanguage } from '../../core/design.ts';
import type { Settings } from '../../core/settings.ts';
import { signInFlow } from './account.ts';

type Section = 'general' | 'language' | 'designs' | 'animations' | 'game' | 'ai' | 'accounts' | 'advanced' | 'about';
const SECTIONS: [Section, string, () => string][] = [
  ['general', 'sliders', () => t('set.general')],
  ['language', 'globe', () => t('set.language')],
  ['designs', 'brush', () => t('nav.designs')],
  ['animations', 'pumpkin', () => t('set.anim')],
  ['game', 'gamepad', () => t('set.game')],
  ['ai', 'sparkle', () => 'KI'],
  ['accounts', 'users', () => t('set.accounts')],
  ['advanced', 'terminal', () => t('set.advanced')],
  ['about', 'info', () => t('set.about')],
];
/** Sections made of searchable rows. */
const ROW_SECTIONS: Section[] = ['general', 'animations', 'game', 'advanced'];

function page(box: HTMLElement): HTMLElement {
  return box.closest<HTMLElement>('.view-enter') ?? (box.parentElement as HTMLElement);
}

const TRANSITION_NAMES: [Settings['transition'], () => string][] = [['lego', () => t('set.tr.lego')], ['slide', () => 'Slide'], ['fade', () => 'Fade'], ['zoom', () => 'Zoom'], ['glitch', () => 'Glitch'], ['pumpkin', () => t('set.tr.pumpkin')], ['none', () => t('set.tr.none')]];

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

let pending: Section | null = null;
/** Makes the next settings page open on `section`. */
export function openSettingsAt(section: Section): void { pending = section; }

export async function settingsPage(el: HTMLElement, section?: Section | string): Promise<void> {
  section ??= pending ?? 'general';
  pending = null;
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
  else if (sec === 'designs') { box.onclick = null; box.onchange = null; await designsPage(box); }
  else if (sec === 'language') languageTab(box);
  else if (sec === 'accounts') await accountsTab(box);
  else if (sec === 'ai') await aiTab(box);
  else await aboutTab(box);
}

/** Renders one row section, or all of them filtered by a search query. */
function rowSections(box: HTMLElement, q: string, only?: Section): void {
  const s = state.settings;
  const parts: Record<string, Raw> = {
    general: html`
      ${row(t('set.clicks'), t('set.clicks.d'), sw('uiSounds', s.uiSounds !== false))}
      ${row(t('set.closeOnLaunch'), t('set.closeOnLaunch.d'), sw('closeOnLaunch', s.closeOnLaunch))}
      ${row(t('set.scale'), `${t('set.scale.d')} (${Math.round(s.uiScale * 100)} %)`, html`<input type="range" data-key="uiScale" min="0.75" max="2" step="0.05" value="${s.uiScale}">`)}
      ${row(t('set.updates'), t('set.updates.d'), html`<select data-key="autoUpdate" class="compact"><option value="ask" ${s.autoUpdate === 'ask' ? 'selected' : ''}>${t('set.upd.ask')}</option><option value="auto" ${s.autoUpdate === 'auto' ? 'selected' : ''}>${t('set.upd.auto')}</option><option value="off" ${s.autoUpdate === 'off' ? 'selected' : ''}>${t('set.upd.off')}</option></select>`)}`,
    animations: html`
      ${row(t('set.transition'), t('set.transition.d'), html`<div class="seg" data-seg="transition">${TRANSITION_NAMES.map(([v, l]) => html`<button data-v="${v}" class="${s.transition === v ? 'on' : ''}">${l()}</button>`)}</div>`)}
      ${row(t('set.intro'), t('set.intro.d'), html`<div class="row"><button class="btn sm" id="introPlay">${icon('play', 'sm')} ${t('set.introPlay')}</button>${sw('introEnabled', s.introEnabled)}</div>`)}
      ${row(t('set.introSound'), t('set.introSound.d'), sw('introSound', s.introSound))}
      ${row(t('set.jumpscares'), t('set.jumpscares.d'), html`<div class="row"><button class="btn sm" id="scareTest">${t('set.preview')}</button>${sw('jumpscares', s.jumpscares)}</div>`)}
      ${row(t('set.motion'), t('set.motion.d'), sw('reducedMotion', s.reducedMotion))}`,
    game: html`
      ${row(t('set.autoConsole'), t('set.autoConsole.d'), sw('autoConsole', s.autoConsole))}
      ${row(t('set.snapshots'), t('set.snapshots.d'), sw('showSnapshots', s.showSnapshots))}
      ${row(t('set.historical'), t('set.historical.d'), sw('showHistorical', s.showHistorical))}
      ${row(t('set.cfKey'), t('set.cfKey.d'), html`<input type="password" data-key="curseforgeKey" value="${s.curseforgeKey}" placeholder="$2a$10$…" autocomplete="off" class="compact wide">`)}`,
    advanced: html`
      ${row(t('set.backend'), t('set.backend.d'), html`<input data-key="backendUrl" value="${s.backendUrl}" placeholder="https://…" class="compact wide">`)}
      ${row(t('set.gameDir'), `${t('set.gameDir.d')} ${state.info.gameDir}`, html`<button class="btn sm" id="game">${icon('folder', 'sm')} ${t('set.open')}</button>`)}
      ${row(t('set.diag'), t('set.diag.d'), html`<button class="btn sm" id="diag">${t('set.copy')}</button>`)}`,
  };
  const titles: Record<string, string> = { general: t('set.general'), animations: t('set.anim'), game: t('set.game'), advanced: t('set.advanced') };
  const list = only ? [only] : ROW_SECTIONS;
  mount(box, html`${list.map((id) => html`<section class="set-sec" data-sec="${id}"><h2>${titles[id]}</h2><div class="glass rows">${parts[id]!}</div></section>`)}`);
  if (q) {
    box.querySelectorAll<HTMLElement>('.setting').forEach((r) => { r.hidden = !r.dataset.s!.includes(q); });
    box.querySelectorAll<HTMLElement>('.set-sec').forEach((sec) => { sec.hidden = !sec.querySelector('.setting:not([hidden])'); });
    if (!box.querySelector('.set-sec:not([hidden])')) mount(box, html`<div class="empty">Keine Einstellung gefunden.</div>`);
  }
  const rerender = () => rowSections(box, q, only);
  box.onchange = async (e) => {
    const i = e.target as HTMLInputElement;
    const key = i.dataset.key as keyof Settings | undefined;
    if (!key) return;
    const value = i.type === 'checkbox' ? i.checked : i.type === 'range' ? Number(i.value) : i.value.trim();
    await save({ [key]: value } as Partial<Settings>);
    if (key === 'backendUrl') { state.auth = await api.reconnectLego(); await loadIdentity(); }
    if (key === 'language') return void settingsPage(page(box), only ?? 'general');
    if (i.type !== 'text' && i.type !== 'password') rerender();
    toast(t('set.saved'), 'ok');
  };
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    if (b.parentElement?.dataset.seg === 'transition') { await save({ transition: b.dataset.v as Settings['transition'] }); rerender(); toast(t('set.saved'), 'ok'); return; }
    if (b.parentElement?.dataset.seg === 'language') { await save({ language: b.dataset.v === 'en' ? 'en' : b.dataset.v === 'custom' ? 'custom' : 'de' }); return void settingsPage(page(box), only ?? 'general'); }
    if (b.id === 'game') return void api.openFolder('game');
    if (b.id === 'scareTest') { const { scare } = await import('../jumpscare.ts'); scare(); return; }
    if (b.id === 'introPlay') {
      const { webglAvailable } = await import('../intro3d.ts');
      const { playIntroCave } = await import('../introcave.ts');
      if (webglAvailable()) await playIntroCave({ sound: s.introSound, quality: s.backgroundQuality, skipLabel: t('intro.skip') });
      return;
    }
    if (b.id === 'diag') { await navigator.clipboard.writeText(await api.diagnostics()); toast('Diagnose in die Zwischenablage kopiert', 'ok'); }
  };
}

/** Crash doctor AI: the player's own Claude API key (stored encrypted, never shown again). */
async function aiTab(box: HTMLElement): Promise<void> {
  box.onclick = null; box.onchange = null;
  const st = await api.aiStatus();
  mount(box, html`<section class="set-sec"><h2>${icon('sparkle', 'sm')} KI für den Crash-Doktor</h2><div class="glass rows">
    <div class="setting"><div><b>Status</b><small>${st.ownKey ? 'Eigener Claude-Schlüssel gespeichert: die KI-Prüfung läuft direkt bei Claude.' : st.server ? 'Kein eigener Schlüssel: die KI-Prüfung läuft über den LEGO-Server (Anmeldung nötig, 20 pro Tag).' : 'Noch kein KI-Zugang. Trag unten einen Claude-API-Schlüssel ein.'}</small></div>
      <div class="ctl"><span class="badge ${st.ownKey || st.server ? 'ok' : ''}">${st.ownKey ? 'Eigener Schlüssel' : st.server ? 'LEGO-Server' : 'Aus'}</span></div></div>
    <div class="setting"><div><b>Claude-API-Schlüssel</b><small>Bekommst du auf console.anthropic.com unter „API Keys“ (beginnt mit sk-ant-). Jede Prüfung kostet dort ein paar Cent. Der Schlüssel wird verschlüsselt auf diesem PC gespeichert${st.persistent ? '' : ' (hier ist keine Verschlüsselung verfügbar, er gilt nur bis zum Neustart)'} und nur an api.anthropic.com geschickt.</small></div>
      <div class="ctl row gap"><input type="password" id="aiKey" placeholder="${st.ownKey ? '•••••••• (gespeichert)' : 'sk-ant-…'}" autocomplete="off" spellcheck="false" class="compact wide"><button class="btn sm primary" id="aiSave">${icon('check', 'xs')} Testen &amp; speichern</button>${st.ownKey ? html`<button class="btn sm" id="aiDel">${icon('trash', 'xs')} Entfernen</button>` : ''}</div></div>
    <div class="setting"><div><b>Was geschickt wird</b><small>Nur wenn du im Crash-Doktor auf „Mit KI prüfen“ klickst: das Ende von latest.log und der letzte Crash-Report, vorher bereinigt (Benutzernamen in Pfaden, Tokens, E-Mails, IP-Adressen entfernt), dazu deine Mod-Liste und Version, Loader und RAM des Profils.</small></div></div>
  </div></section>`);
  const input = box.querySelector<HTMLInputElement>('#aiKey')!;
  box.querySelector('#aiSave')!.addEventListener('click', async (e) => {
    const b = e.currentTarget as HTMLButtonElement;
    if (!input.value.trim()) { toast('Bitte zuerst den Schlüssel einfügen.', 'error'); return; }
    b.disabled = true; b.textContent = 'Prüfe Schlüssel …';
    try {
      const r = await api.setAiKey(input.value.trim());
      input.value = '';
      if (!r.ok) { toast(r.error ?? 'Schlüssel funktioniert nicht.', 'error'); b.disabled = false; b.textContent = 'Testen & speichern'; return; }
      toast('Schlüssel funktioniert und ist gespeichert.', 'ok');
      await aiTab(box);
    } catch (err) { errorToast(err); b.disabled = false; }
  });
  box.querySelector('#aiDel')?.addEventListener('click', async () => {
    await api.setAiKey(null);
    toast('Schlüssel entfernt.', 'ok');
    await aiTab(box);
  });
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

/** Own language: translate every text of the launcher, save, export and import packs. */
function languageTab(box: HTMLElement): void {
  const s = state.settings;
  const rows = languageTemplate();
  const draft: Record<string, string> = { ...(s.customLanguage?.strings ?? {}) };
  let q = '';
  const done = () => rows.filter((r) => draft[r.key]?.trim()).length;
  mount(box, html`<section class="set-sec"><h2>${t('set.language')}</h2>
      <div class="lang-grid" id="langGrid">${LANGS.map(([c, n]) => html`<button class="lang-tile ${s.language === c ? 'on' : ''}" data-lang="${c}" title="${n}"><span class="lang-flag">${{ __raw: FLAGS[c] ?? '' }}</span><span class="grow">${n}</span>${s.language === c ? icon('check', 'sm') : ''}</button>`)}
      ${s.customLanguage ? html`<button class="lang-tile ${s.language === 'custom' ? 'on' : ''}" data-lang="custom"><span class="lang-flag">${icon('globe')}</span><span class="grow">${s.customLanguage.name}</span>${s.language === 'custom' ? icon('check', 'sm') : ''}</button>` : ''}</div></section>
    <details class="set-sec lang-more" id="customLang"><summary>Eigene Übersetzung (Erweitert)</summary><div class="card">
      <p class="small muted">Übersetze die Texte des Launchers in deine eigene Sprache (oder schreib sie einfach so, wie du sie magst). Leere Felder bleiben Deutsch. Sprachpakete kannst du exportieren und mit Freunden teilen.</p>
      <div class="row"><input id="lname" class="grow" maxlength="30" placeholder="Name der Sprache, z. B. Türkçe" value="${s.customLanguage?.name ?? ''}"><span class="badge" id="lcount">${done()} / ${rows.length}</span></div>
      <div class="row gap"><button class="btn primary" id="lsave">${icon('check', 'sm')} Speichern & aktivieren</button><button class="btn" id="lexport">${icon('download', 'sm')} Exportieren</button><label class="btn">${icon('import', 'sm')} Importieren<input type="file" id="limport" accept="application/json" hidden></label>${s.customLanguage ? html`<button class="btn danger" id="ldel">${icon('trash', 'sm')} Löschen</button>` : ''}</div>
      <div class="searchbar sm gap">${icon('search', 'sm')}<input id="lq" placeholder="Text suchen …" autocomplete="off"></div>
      <div class="lang-table" id="ltable"></div></div></details>`);
  const table = box.querySelector<HTMLElement>('#ltable')!;
  const draw = () => {
    const list = rows.filter((r) => !q || `${r.key} ${r.de} ${r.en} ${draft[r.key] ?? ''}`.toLowerCase().includes(q)).slice(0, 400);
    mount(table, html`${list.map((r) => html`<label class="lang-row"><span class="lr-ref"><small class="mono">${r.key}</small><span>${r.de}</span>${r.en ? html`<span class="muted">${r.en}</span>` : ''}</span><input data-k="${r.key}" value="${draft[r.key] ?? ''}" placeholder="${r.de}" maxlength="400"></label>`)}`);
  };
  draw();
  box.querySelector('#lq')!.addEventListener('input', (e) => { q = (e.target as HTMLInputElement).value.trim().toLowerCase(); draw(); });
  table.addEventListener('input', (e) => {
    const i = e.target as HTMLInputElement;
    if (!i.dataset.k) return;
    if (i.value.trim()) draft[i.dataset.k] = i.value; else delete draft[i.dataset.k];
    box.querySelector('#lcount')!.textContent = `${done()} / ${rows.length}`;
  });
  const pack = () => ({ name: box.querySelector<HTMLInputElement>('#lname')!.value.trim() || 'Eigene Sprache', strings: draft });
  box.onchange = null;
  box.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    if (b.dataset.lang) { await save({ language: b.dataset.lang }); return void settingsPage(page(box), 'language'); }
    if (b.id === 'lsave') {
      const p = sanitizeLanguage(pack());
      if (!p) return errorToast(new Error('Bitte einen Namen eingeben.'));
      await save({ customLanguage: p, language: 'custom' });
      toast(`Sprache „${p.name}“ aktiv`, 'ok');
      return void settingsPage(page(box), 'language');
    }
    if (b.id === 'lexport') {
      // Export contains every key; untranslated ones carry the German text as a hint for translators.
      const full = { name: pack().name, strings: Object.fromEntries(rows.map((r) => [r.key, draft[r.key] ?? ''])), reference: Object.fromEntries(rows.map((r) => [r.key, { de: r.de, en: r.en }])) };
      const a = document.createElement('a');
      a.href = URL.createObjectURL(new Blob([JSON.stringify(full, null, 2)], { type: 'application/json' }));
      a.download = `${full.name.replace(/[^\w-]+/g, '_')}.lego-lang.json`;
      a.click();
      return URL.revokeObjectURL(a.href);
    }
    if (b.id === 'ldel' && await confirm('Sprache löschen?', 'Das eigene Sprachpaket wird entfernt.', 'Löschen', true)) {
      await save({ customLanguage: null, language: s.language === 'custom' ? 'de' : s.language });
      return void settingsPage(page(box), 'language');
    }
  };
  box.querySelector('#limport')!.addEventListener('change', async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file || file.size > 500_000) return errorToast(new Error('Datei zu groß oder leer.'));
    try {
      const p = sanitizeLanguage(JSON.parse(await file.text()));
      if (!p) return errorToast(new Error('Das ist kein gültiges Sprachpaket.'));
      await save({ customLanguage: p, language: 'custom' });
      toast(`Sprache „${p.name}“ importiert (${Object.keys(p.strings).length} Texte)`, 'ok');
      void settingsPage(page(box), 'language');
    } catch (err) { errorToast(err); }
  });
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
