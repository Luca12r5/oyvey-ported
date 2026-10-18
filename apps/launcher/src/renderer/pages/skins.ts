// Skins tab: the account's real Minecraft skin in 3D (zoom, rotate, poses),
// upload a new one through the official API, and a local skin library.

import { loadIdentity, onLeave, state } from '../app.ts';
import { renderSkins } from '../nr/mount.tsx';
import { api, confirm, dialog, errorToast, html, toast, type Raw } from '../ui.ts';
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

/** Skins: the NoRisk-launcher skins grid (React, nr/tabs/SkinsTab.tsx). Click = use in Minecraft. */
export function skinsPage(el: HTMLElement): void {
  const lib = (): SkinEntry[] => state.settings.skinLibrary ?? [];
  let unmount: (() => void) | null = null;
  const file = document.createElement('input');
  file.type = 'file';
  file.accept = 'image/png';
  const rerender = () => {
    unmount = renderSkins(el, {
      signedIn: state.auth.signedIn,
      accountName: state.auth.name ?? '',
      library: lib(),
      current: { skin: state.skin.skin, variant: state.skin.variant },
      guestName: state.settings.guestName,
      onAdd: () => file.click(),
      onDropFile: (f) => void take(f),
      onApply: (s) => void apply(s),
      onEdit: (s) => void edit(s),
      onDelete: (s) => void (async () => { if (await confirm('Skin löschen?', 'Der Skin wird aus deiner Bibliothek entfernt.', 'Löschen', true)) await saveLib(lib().filter((x) => x.id !== s.id)); })(),
      onKeepCurrent: () => { if (state.skin.skin) void saveLib([...lib(), { id: `s-${Date.now().toString(36)}`, name: state.auth.name ?? 'Mein Skin', data: state.skin.skin, variant: state.skin.variant }]); },
      onGuestName: (name) => void guestName(name),
    });
  };
  const saveLib = async (items: SkinEntry[]) => { state.settings = await api.setSettings({ skinLibrary: items.slice(-40) }); rerender(); };
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
  const edit = async (s: SkinEntry) => {
    const name = await form<string>('Skin bearbeiten', html`<label class="field"><span>Name</span><input id="dn" maxlength="40" value="${s.name}"></label>
      <div class="seg" id="dv"><button type="button" data-v="classic" class="${s.variant === 'classic' ? 'on' : ''}">Classic</button><button type="button" data-v="slim" class="${s.variant === 'slim' ? 'on' : ''}">Slim</button></div>`, (box) => {
      let v = s.variant;
      box.querySelector('#dv')!.addEventListener('click', (ev) => { const b = (ev.target as HTMLElement).closest<HTMLButtonElement>('[data-v]'); if (b) { v = b.dataset.v as 'classic' | 'slim'; box.querySelectorAll('#dv button').forEach((x) => x.classList.toggle('on', x === b)); } });
      return () => { s = { ...s, variant: v }; return (box.querySelector<HTMLInputElement>('#dn')!.value.trim() || s.name).slice(0, 40); };
    });
    if (name) await saveLib(lib().map((x) => (x.id === s.id ? { ...x, name, variant: s.variant } : x)));
  };
  const apply = async (s: SkinEntry) => {
    if (!state.auth.signedIn) return toast('Zum Übernehmen in Minecraft bitte anmelden.');
    if (!(await confirm('Skin übernehmen?', `„${s.name}“ wird dein Skin in Minecraft.`, 'Übernehmen'))) return;
    try {
      state.skin = await api.uploadSkin(s.data, s.variant);
      await loadIdentity();
      toast('Skin in Minecraft geändert', 'ok');
      rerender();
    } catch (err) { errorToast(err); }
  };
  const guestName = async (name: string) => {
    if (name && !/^[A-Za-z0-9_]{3,16}$/.test(name)) return toast('Minecraft-Namen haben 3–16 Zeichen (Buchstaben, Zahlen, _).', 'error');
    state.settings = await api.setSettings({ guestName: name });
    await loadIdentity();
    if (name && !state.skin.skin) return toast('Diesen Namen gibt es nicht oder Mojang ist gerade nicht erreichbar.', 'error');
    rerender();
  };
  rerender();
  onLeave(() => unmount?.());
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
