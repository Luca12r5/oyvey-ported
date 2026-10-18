// Skins tab: the account's real Minecraft skin in 3D (zoom, rotate, poses),
// upload a new one through the official API, and a local skin library.

import { loadIdentity, state } from '../app.ts';
import { api, confirm, dialog, errorToast, html, mount, toast, type Raw } from '../ui.ts';
import { icon } from '../icons.ts';
import { skinPoster } from '../avatar.ts';
import type { SkinEntry } from '../../core/design.ts';


function readPng(file: File): Promise<string> {
  return new Promise((res, rej) => {
    if (file.type !== 'image/png' || file.size > 64 * 1024) return rej(new Error('Bitte eine PNG-Datei (64×64, max. 64 KB) wählen.'));
    const r = new FileReader();
    r.onload = () => {
      const url = String(r.result);
      const img = new Image();
      img.onload = () => (img.width === 64 && (img.height === 64 || img.height === 32) ? res(url) : rej(new Error('Der Skin muss 64×64 Pixel groß sein.')));
      img.onerror = () => rej(new Error('Die Datei ist kein gültiges Bild.'));
      img.src = url;
    };
    r.onerror = () => rej(new Error('Datei konnte nicht gelesen werden.'));
    r.readAsDataURL(file);
  });
}

let sq = '';

/** Skins: grid of the skin library like the NoRisk launcher (posed 3D figures). Click = use in Minecraft. */
export async function skinsPage(el: HTMLElement): Promise<void> {
  const signed = state.auth.signedIn;
  const lib = (): SkinEntry[] => state.settings.skinLibrary ?? [];
  mount(el, html`<div class="skins-head">
      <div class="searchbar">${icon('search', 'sm')}<input id="sq" placeholder="Search skins…" value="${sq}" autocomplete="off"></div>
      <span class="grow"></span>
      <button class="btn" id="add">${icon('plus', 'sm')} ADD SKIN</button>
      <input type="file" id="file" accept="image/png" hidden>
    </div>
    <div class="page-sep"></div>
    ${signed ? '' : html`<div class="guest-skin"><span class="small muted">Als Gast: Minecraft-Namen eintragen, um deinen Skin zu sehen (nur Anzeige).</span><input id="gname" class="compact" maxlength="16" placeholder="z. B. LegoCpvp" value="${state.settings.guestName}"><button class="btn sm" id="gload">Laden</button></div>`}
    <div class="skin-grid" id="grid"></div>`);
  const grid = el.querySelector<HTMLElement>('#grid')!;
  const current = state.skin.skin;
  const draw = async () => {
    const q = sq.trim().toLowerCase();
    const items = lib().filter((s) => !q || s.name.toLowerCase().includes(q));
    mount(grid, html`<label class="skin-card add" id="addCard"><div class="sc-fig ghost"><img id="ghostImg" alt=""><span class="sc-plus">${icon('plus')}</span></div><div class="sc-name">Add New Skin</div><div class="sc-sub">${icon('upload', 'xs')} Upload or import a skin</div></label>
      ${current && !lib().some((s) => s.data === current) ? html`<div class="skin-card active" data-cur="1"><div class="sc-fig"><img data-poster="cur" alt=""></div><div class="sc-name">${state.auth.name ?? 'Aktiver Skin'}</div><div class="sc-sub">${icon('check', 'xs')} Aktiv in Minecraft</div>
        <div class="sc-tools"><button class="icon-btn sq" data-keep="1" title="In Bibliothek speichern">${icon('plus', 'sm')}</button></div></div>` : ''}
      ${items.map((s) => html`<div class="skin-card ${s.data === current ? 'active' : ''}" data-id="${s.id}"><div class="sc-fig"><img data-poster="${s.id}" alt=""></div>
        <div class="sc-name" title="${s.name}">${s.name}</div><div class="sc-sub">${icon('skins', 'xs')} ${s.variant === 'slim' ? 'Slim' : 'Classic'}${s.data === current ? ' · aktiv' : ''}</div>
        <div class="sc-tools"><button class="icon-btn sq" data-edit="${s.id}" title="Umbenennen / Modell">${icon('brush', 'sm')}</button><button class="icon-btn sq" data-del="${s.id}" title="Löschen">${icon('trash', 'sm')}</button></div></div>`)}`);
    const ghost = await skinPoster(null, 'classic').catch(() => '');
    const gi = grid.querySelector<HTMLImageElement>('#ghostImg');
    if (gi && ghost) gi.src = ghost;
    if (current) { const img = grid.querySelector<HTMLImageElement>('[data-poster="cur"]'); if (img) img.src = await skinPoster(current, state.skin.variant).catch(() => ''); }
    for (const s of items) {
      const img = grid.querySelector<HTMLImageElement>(`[data-poster="${s.id}"]`);
      if (img) img.src = await skinPoster(s.data, s.variant).catch(() => '');
    }
  };
  void draw();
  const saveLib = async (items: SkinEntry[]) => { state.settings = await api.setSettings({ skinLibrary: items.slice(-40) }); void draw(); };
  const file = el.querySelector<HTMLInputElement>('#file')!;
  const take = async (f: File | undefined) => {
    if (!f) return;
    try {
      const data = await readPng(f);
      if (lib().some((s) => s.data === data)) return toast('Schon in der Bibliothek.');
      const name = f.name.replace(/\.png$/i, '').slice(0, 40) || `Skin ${lib().length + 1}`;
      const variant = await askVariant();
      await saveLib([...lib(), { id: `s-${Date.now().toString(36)}`, name, data, variant }]);
      toast('Skin hinzugefügt', 'ok');
    } catch (e) { errorToast(e); }
  };
  file.addEventListener('change', () => { void take(file.files?.[0]); file.value = ''; });
  el.querySelector('#add')!.addEventListener('click', () => file.click());
  el.querySelector('#sq')!.addEventListener('input', (e) => { sq = (e.target as HTMLInputElement).value; void draw(); });
  grid.addEventListener('dragover', (e) => e.preventDefault());
  grid.addEventListener('drop', (e) => { e.preventDefault(); void take(e.dataTransfer?.files[0]); });
  el.querySelector('#gload')?.addEventListener('click', async () => {
    const name = el.querySelector<HTMLInputElement>('#gname')!.value.trim();
    if (name && !/^[A-Za-z0-9_]{3,16}$/.test(name)) return toast('Minecraft-Namen haben 3–16 Zeichen (Buchstaben, Zahlen, _).', 'error');
    state.settings = await api.setSettings({ guestName: name });
    await loadIdentity();
    if (name && !state.skin.skin) return toast('Diesen Namen gibt es nicht oder Mojang ist gerade nicht erreichbar.', 'error');
    void skinsPage(el);
  });
  grid.addEventListener('click', async (e) => {
    const t = e.target as HTMLElement;
    if (t.closest('#addCard')) { e.preventDefault(); return file.click(); }
    const del = t.closest<HTMLElement>('[data-del]');
    if (del) { if (await confirm('Skin löschen?', 'Der Skin wird aus deiner Bibliothek entfernt.', 'Löschen', true)) await saveLib(lib().filter((s) => s.id !== del.dataset.del)); return; }
    if (t.closest('[data-keep]') && current) { await saveLib([...lib(), { id: `s-${Date.now().toString(36)}`, name: state.auth.name ?? 'Mein Skin', data: current, variant: state.skin.variant }]); return; }
    const ed = t.closest<HTMLElement>('[data-edit]');
    if (ed) {
      const s = lib().find((x) => x.id === ed.dataset.edit);
      if (!s) return;
      const name = await form<string>('Skin bearbeiten', html`<label class="field"><span>Name</span><input id="dn" maxlength="40" value="${s.name}"></label>
        <div class="seg" id="dv"><button type="button" data-v="classic" class="${s.variant === 'classic' ? 'on' : ''}">Classic</button><button type="button" data-v="slim" class="${s.variant === 'slim' ? 'on' : ''}">Slim</button></div>`, (box) => {
        let v = s.variant;
        box.querySelector('#dv')!.addEventListener('click', (ev) => { const b = (ev.target as HTMLElement).closest<HTMLButtonElement>('[data-v]'); if (b) { v = b.dataset.v as 'classic' | 'slim'; box.querySelectorAll('#dv button').forEach((x) => x.classList.toggle('on', x === b)); } });
        return () => { s.variant = v; return (box.querySelector<HTMLInputElement>('#dn')!.value.trim() || s.name).slice(0, 40); };
      });
      if (name) await saveLib(lib().map((x) => (x.id === s.id ? { ...x, name, variant: s.variant } : x)));
      return;
    }
    const card = t.closest<HTMLElement>('[data-id]');
    const s = card && lib().find((x) => x.id === card.dataset.id);
    if (!s) return;
    if (!signed) return toast('Zum Übernehmen in Minecraft bitte anmelden.');
    if (!(await confirm('Skin übernehmen?', `„${s.name}“ wird dein Skin in Minecraft.`, 'Übernehmen'))) return;
    try {
      state.skin = await api.uploadSkin(s.data, s.variant);
      await loadIdentity();
      toast('Skin in Minecraft geändert', 'ok');
      void skinsPage(el);
    } catch (err) { errorToast(err); }
  });
}

function askVariant(): Promise<'classic' | 'slim'> {
  return form<'classic' | 'slim'>('Modell wählen', html`<p class="small muted">Classic = 4 Pixel breite Arme, Slim = 3 Pixel (Alex).</p><div class="seg" id="dv"><button type="button" data-v="classic" class="on">Classic</button><button type="button" data-v="slim">Slim</button></div>`, (box) => {
    let v: 'classic' | 'slim' = 'classic';
    box.querySelector('#dv')!.addEventListener('click', (ev) => { const b = (ev.target as HTMLElement).closest<HTMLButtonElement>('[data-v]'); if (b) { v = b.dataset.v as 'classic' | 'slim'; box.querySelectorAll('#dv button').forEach((x) => x.classList.toggle('on', x === b)); } });
    return () => v;
  }).then((v) => v ?? 'classic');
}

/** Small form dialog: `setup` wires the controls and returns the value getter used on OK. */
function form<T>(title: string, body: Raw, setup: (box: HTMLElement) => () => T): Promise<T | null> {
  const d = dialog(html`<form method="dialog"><h2>${title}</h2>${body}<div class="row between gap"><button class="btn" value="cancel">Abbrechen</button><button class="btn primary" value="ok">OK</button></div></form>`);
  const get = setup(d);
  return new Promise((resolve) => { d.onclose = () => resolve(d.returnValue === 'ok' ? get() : null); });
}
