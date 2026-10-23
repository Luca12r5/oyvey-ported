// Skins tab: the account's real Minecraft skin in 3D (zoom, rotate, poses),
// upload a new one through the official API, and a local skin library.

import { go, loadIdentity, onLeave, state } from '../app.ts';
import { openSkinDialog, renderSkins } from '../nr/mount.tsx';
import { sanitizeSkinSpec, type SkinSpec } from '@lego/shared';
import { api, confirm, dialog, errorToast, html, lego, toast, type Raw } from '../ui.ts';
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

let pendingSkin: File | null = null;

/** A skin PNG dropped anywhere: open the skins page and show it in the 3D preview. */
export function openSkinFile(f: File): void {
  pendingSkin = f;
  void go('skins');
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
      accountName: state.auth.signedIn ? state.auth.name ?? '' : state.settings.guestName,
      library: lib(),
      current: { skin: state.skin.skin, variant: state.skin.variant },
      guestName: state.settings.guestName,
      onAdd: () => file.click(),
      onPhoto: () => photo.click(),
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
      // Look at it in 3D first; saving puts it into the library.
      closeDialog = openSkinDialog({ title: 'Skin ansehen', data, name, variant: 'classic', onSave: saveFromDialog });
    } catch (e) { errorToast(e); }
  };
  let closeDialog: (() => void) | null = null;
  const saveFromDialog = async (data: string, variant: 'classic' | 'slim', name: string) => {
    await saveLib([...lib(), { id: `s-${Date.now().toString(36)}`, name, data, variant }]);
    closeDialog?.();
    toast('Skin in der Bibliothek gespeichert', 'ok');
  };
  file.addEventListener('change', () => { void take(file.files?.[0]); file.value = ''; });
  // Photo → skin: the server AI when configured, otherwise colours sampled from the photo.
  const photo = document.createElement('input');
  photo.type = 'file';
  photo.accept = 'image/png,image/jpeg,image/webp';
  photo.addEventListener('change', () => { const f = photo.files?.[0]; photo.value = ''; if (f) void fromPhoto(f); });
  const fromPhoto = async (f: File) => {
    try {
      if (f.size > 12_000_000) throw new Error('Foto zu groß (max. 12 MB).');
      const img = await loadImage(f);
      let spec: SkinSpec | null = null;
      let note = 'Aus den Farben deines Fotos erstellt (offline). Passe alles an, bis es dir gefällt.';
      if (state.auth.lego.connected) {
        const ai = await lego<{ ai: boolean }>('GET', '/api/me/cape-spec').then((r) => r.ai).catch(() => false);
        if (ai) {
          toast('KI schaut sich dein Foto an …');
          spec = await lego<{ spec: SkinSpec }>('POST', '/api/skins/from-photo', { image: downscale(img, 768) }).then((r) => r.spec).catch((e) => { errorToast(e); return null; });
          if (spec) note = 'Von der KI aus deinem Foto erstellt – jetzt nach Lust anpassen. Das Foto wurde nicht gespeichert.';
        }
      }
      spec ??= sampleSpec(img);
      closeDialog = openSkinDialog({ title: 'Skin aus Foto', spec, note, name: f.name.replace(/\.[^.]+$/, '').slice(0, 40) || 'Foto-Skin', variant: 'classic', onSave: saveFromDialog });
    } catch (e) { errorToast(e); }
  };
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
  if (pendingSkin) { const f = pendingSkin; pendingSkin = null; void take(f); }
  onLeave(() => { unmount?.(); closeDialog?.(); });
}

/** Small form dialog: `setup` wires the controls and returns the value getter used on OK. */
function form<T>(title: string, body: Raw, setup: (box: HTMLElement) => () => T): Promise<T | null> {
  const d = dialog(html`<form method="dialog"><h2>${title}</h2>${body}<div class="row between gap"><button class="btn" value="cancel">Abbrechen</button><button class="btn primary" value="ok">OK</button></div></form>`);
  const get = setup(d);
  return new Promise((resolve) => { d.onclose = () => resolve(d.returnValue === 'ok' ? get() : null); });
}

function loadImage(f: File): Promise<HTMLImageElement> {
  return new Promise((res, rej) => {
    const url = URL.createObjectURL(f);
    const img = new Image();
    img.onload = () => res(img);
    img.onerror = () => rej(new Error('Die Datei ist kein gültiges Bild.'));
    img.src = url;
  });
}

/** JPEG data URL of at most `max` px (the AI does not need more). */
function downscale(img: HTMLImageElement, max: number): string {
  const k = Math.min(1, max / Math.max(img.naturalWidth, img.naturalHeight));
  const c = document.createElement('canvas');
  c.width = Math.max(1, Math.round(img.naturalWidth * k));
  c.height = Math.max(1, Math.round(img.naturalHeight * k));
  c.getContext('2d')!.drawImage(img, 0, 0, c.width, c.height);
  return c.toDataURL('image/jpeg', 0.85);
}

/** Offline guess: average colours of the hair (top), face (middle) and clothes (bottom) areas. */
export function sampleSpec(img: HTMLImageElement): SkinSpec {
  const c = document.createElement('canvas');
  c.width = 48; c.height = 64;
  const g = c.getContext('2d', { willReadFrequently: true })!;
  g.drawImage(img, 0, 0, 48, 64);
  const avg = (x0: number, y0: number, x1: number, y1: number) => {
    const d = g.getImageData(x0, y0, x1 - x0, y1 - y0).data;
    let r = 0, gg = 0, b = 0, n = 0;
    for (let i = 0; i < d.length; i += 4) { r += d[i]!; gg += d[i + 1]!; b += d[i + 2]!; n++; }
    const h = (v: number) => Math.round(v / n).toString(16).padStart(2, '0');
    return `#${h(r)}${h(gg)}${h(b)}`;
  };
  return sanitizeSkinSpec({
    skin: avg(18, 22, 30, 32), hair: avg(16, 4, 32, 10), hairStyle: 'short', eyes: '#4b3621', beard: false, glasses: 'none',
    top: { kind: 'hoodie', color: avg(8, 48, 40, 60), color2: '#f1f5f9', pattern: 'none' }, pants: '#27303f', shoes: '#f1f5f9', accessory: 'none', accessoryColor: '#111827', slim: false,
  });
}
