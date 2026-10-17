// Skins tab: the account's real Minecraft skin in 3D (zoom, rotate, poses),
// upload a new one through the official API, and a local skin library.

import { loadIdentity, onLeave, state } from '../app.ts';
import { api, errorToast, html, mount, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import { createAvatar, type Anim, type Avatar } from '../avatar.ts';
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

export async function skinsPage(el: HTMLElement): Promise<void> {
  const signed = state.auth.signedIn;
  let preview: { data: string | null; variant: 'classic' | 'slim' } = { data: state.skin.skin, variant: state.skin.variant };
  const anim: Anim = 'idle';
  const lib = (): SkinEntry[] => state.settings.skinLibrary ?? [];

  mount(el, html`<div class="skins">
    <section class="panel skins-stage">
      <div class="panel-h"><span>${icon('skins', 'sm')} Dein Skin</span><span class="muted small" id="skinSrc">${signed ? `Aktiv in Minecraft · ${state.auth.name}` : state.settings.guestName ? `Skin von ${state.settings.guestName}` : 'Gastmodus – Standard-Skin'}</span></div>
      <div class="grid-floor" aria-hidden="true"><i class="gf-floor"></i><i class="gf-ceil"></i></div>
      <canvas id="skinView"></canvas>
      <p class="small muted hint">Mausrad = Zoom · Ziehen = Drehen</p>
    </section>
    <aside class="panel skins-side">
      <div class="panel-h"><span>${icon('upload', 'sm')} Skin ändern</span></div>
      <label class="drop" id="drop"><input type="file" id="file" accept="image/png" hidden>${icon('upload')}<b>PNG hier ablegen</b><small>oder klicken · 64×64 Pixel</small></label>
      <div class="row gap"><span class="small">Modell</span><div class="seg" id="variant"><button data-v="classic">Classic</button><button data-v="slim">Slim</button></div></div>
      <div class="row gap wrap">
        <button class="btn primary" id="apply" ${signed ? '' : 'disabled'}>${icon('check', 'sm')} In Minecraft übernehmen</button>
        <button class="btn" id="save">${icon('plus', 'sm')} In Bibliothek</button>
        <button class="btn ghost" id="reset" ${signed ? '' : 'disabled'}>Standard-Skin</button>
      </div>
      ${signed ? '' : html`<div class="panel-h sub"><span>Dein Minecraft-Name</span></div>
        <p class="small muted">Als Gast zeigen wir den Skin, den du in Minecraft spielst – trag einfach deinen Namen ein (nur Anzeige, kein Login).</p>
        <div class="row gap"><input id="gname" class="compact" maxlength="16" placeholder="z. B. LegoCpvp" value="${state.settings.guestName}"><button class="btn" id="gload">Laden</button></div>`}
      <div class="panel-h sub"><span>Bibliothek</span><span class="muted small" id="libCount"></span></div>
      <div class="skin-lib" id="lib"></div>
    </aside>
  </div>`);

  let avatar: Avatar | null = null;
  try {
    avatar = createAvatar(el.querySelector<HTMLCanvasElement>('#skinView')!, state.settings.reducedMotion);
    avatar.play(anim);
  } catch (e) { console.warn('avatar unavailable', e); }
  onLeave(() => avatar?.dispose());
  const show = () => {
    void avatar?.setSkin(preview.data, preview.variant, state.skin.cape).catch(() => {});
    el.querySelectorAll<HTMLButtonElement>('#variant button').forEach((b) => b.classList.toggle('on', b.dataset.v === preview.variant));
  };
  show();

  const drawLib = () => {
    const items = lib();
    el.querySelector('#libCount')!.textContent = `${items.length}/40`;
    mount(el.querySelector('#lib')!, items.length
      ? html`${items.map((s) => html`<div class="lib-i ${preview.data === s.data ? 'on' : ''}" data-id="${s.id}" title="${s.name}"><img src="${s.data}" alt=""><span>${s.name}</span><button class="x" data-del="${s.id}" aria-label="Löschen">${icon('close', 'xs')}</button></div>`)}`
      : html`<p class="small muted">Noch keine Skins gespeichert.</p>`);
  };
  drawLib();

  const saveLib = async (items: SkinEntry[]) => { state.settings = await api.setSettings({ skinLibrary: items }); drawLib(); };

  el.querySelector('#variant')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-v]');
    if (b) { preview = { ...preview, variant: b.dataset.v as 'classic' | 'slim' }; show(); }
  });
  const take = async (f: File | undefined) => {
    if (!f) return;
    try { preview = { ...preview, data: await readPng(f) }; show(); drawLib(); toast('Vorschau geladen', 'ok'); } catch (e) { errorToast(e); }
  };
  const drop = el.querySelector<HTMLElement>('#drop')!;
  el.querySelector<HTMLInputElement>('#file')!.addEventListener('change', (e) => void take((e.target as HTMLInputElement).files?.[0]));
  drop.addEventListener('dragover', (e) => { e.preventDefault(); drop.classList.add('over'); });
  drop.addEventListener('dragleave', () => drop.classList.remove('over'));
  drop.addEventListener('drop', (e) => { e.preventDefault(); drop.classList.remove('over'); void take(e.dataTransfer?.files[0]); });

  el.querySelector('#gload')?.addEventListener('click', async () => {
    const name = el.querySelector<HTMLInputElement>('#gname')!.value.trim();
    if (name && !/^[A-Za-z0-9_]{3,16}$/.test(name)) return toast('Minecraft-Namen haben 3–16 Zeichen (Buchstaben, Zahlen, _).', 'error');
    state.settings = await api.setSettings({ guestName: name });
    await loadIdentity();
    if (name && !state.skin.skin) return toast('Diesen Namen gibt es nicht oder Mojang ist gerade nicht erreichbar.', 'error');
    preview = { data: state.skin.skin, variant: state.skin.variant };
    show();
    toast(name ? `Skin von ${name} geladen` : 'Standard-Skin', 'ok');
  });
  el.querySelector('#apply')!.addEventListener('click', async (e) => {
    if (!preview.data) return toast('Wähle zuerst einen Skin.');
    const b = e.currentTarget as HTMLButtonElement;
    b.disabled = true;
    try {
      state.skin = await api.uploadSkin(preview.data, preview.variant);
      await loadIdentity();
      toast('Skin in Minecraft geändert', 'ok');
    } catch (err) { errorToast(err); } finally { b.disabled = false; }
  });
  el.querySelector('#reset')!.addEventListener('click', async () => {
    try {
      state.skin = await api.resetSkin();
      preview = { data: state.skin.skin, variant: state.skin.variant };
      show();
      await loadIdentity();
      toast('Standard-Skin aktiv', 'ok');
    } catch (err) { errorToast(err); }
  });
  el.querySelector('#save')!.addEventListener('click', async () => {
    if (!preview.data) return toast('Kein Skin zum Speichern.');
    if (lib().some((s) => s.data === preview.data)) return toast('Schon in der Bibliothek.');
    const name = `Skin ${lib().length + 1}`;
    await saveLib([...lib(), { id: `s-${Date.now().toString(36)}`, name, data: preview.data, variant: preview.variant }].slice(-40));
    toast('Gespeichert', 'ok');
  });
  el.querySelector('#lib')!.addEventListener('click', async (e) => {
    const t = e.target as HTMLElement;
    const del = t.closest<HTMLElement>('[data-del]');
    if (del) { await saveLib(lib().filter((s) => s.id !== del.dataset.del)); return; }
    const it = t.closest<HTMLElement>('[data-id]');
    const s = it && lib().find((x) => x.id === it.dataset.id);
    if (s) { preview = { data: s.data, variant: s.variant }; show(); drawLib(); }
  });
}
