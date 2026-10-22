// Entry for the React UI ported from the NoRisk Client launcher (GPL-3.0).
// app.ts keeps routing, auth and IPC; it mounts the shell once and renders the
// React tabs into the content area.
import { createRoot, type Root } from 'react-dom/client';
import { AppLayout } from './layout/AppLayout.tsx';
import { PlayTab, type PlayTabProps } from './tabs/PlayTab.tsx';
import { ProfilesTab, type ProfilesTabProps } from './tabs/ProfilesTab.tsx';
import { ModsTab, type ModsTabProps } from './tabs/ModsTab.tsx';
import { SkinsTab, type SkinsTabProps } from './tabs/SkinsTab.tsx';
import { CapesTab, type CapesTabProps } from './tabs/CapesTab.tsx';
import { CapeEditor, type CapeEditorProps } from './capes/CapeEditor.tsx';
import { SkinDialog, type SkinDialogProps } from './skins/SkinDialog.tsx';
import { ColorPicker, type ColorPickerProps } from './ui/ColorPicker.tsx';
export { modsStore } from './tabs/ModsTab.tsx';
import { applyAccentToDOM, accentFrom, themeStore } from './store/theme.ts';

export { shellStore, actions, NAV_ICONS } from './store/shell.ts';
export { gameStore, initGame, launchProfile } from './store/game.ts';

/** Mounts the NoRisk-style frame (rail, header, content area); resolves with the content element. */
export function mountShell(host: HTMLElement): Promise<HTMLElement> {
  return new Promise((resolve) => {
    let done = false;
    createRoot(host).render(<AppLayout onView={(el) => { if (!done) { done = true; resolve(el); } }} />);
  });
}

export function setAccent(hex: string | null | undefined): void {
  themeStore.set({ accentColor: accentFrom(hex) });
  applyAccentToDOM();
}

const roots = new WeakMap<HTMLElement, Root>();

/** Renders a React tab into `el`; returns an unmount function. */
export function renderPlay(el: HTMLElement, props: PlayTabProps): () => void {
  el.classList.add('nr', 'h-full');
  let root = roots.get(el);
  if (!root) { root = createRoot(el); roots.set(el, root); }
  root.render(<PlayTab {...props} />);
  return () => { root.unmount(); roots.delete(el); };
}

/** Generic: renders any element into `el` as a React root; returns an unmount function. */
function renderInto(el: HTMLElement, node: React.ReactNode): () => void {
  el.classList.add('nr', 'h-full');
  let root = roots.get(el);
  if (!root) { root = createRoot(el); roots.set(el, root); }
  root.render(node);
  const r = root;
  return () => { r.unmount(); roots.delete(el); el.classList.remove('nr', 'h-full'); };
}

export function renderProfiles(el: HTMLElement, props: ProfilesTabProps): () => void {
  return renderInto(el, <ProfilesTab {...props} />);
}

export function renderMods(el: HTMLElement, props: ModsTabProps): () => void {
  return renderInto(el, <ModsTab {...props} />);
}

export function renderSkins(el: HTMLElement, props: SkinsTabProps): () => void {
  return renderInto(el, <SkinsTab {...props} />);
}

export function renderCapes(el: HTMLElement, props: CapesTabProps): () => void {
  return renderInto(el, <CapesTab {...props} />);
}

/** Opens the cape editor over the launcher; returns a close function. */
export function openCapeEditor(props: Omit<CapeEditorProps, 'onClose'> & { onClose?: () => void }): () => void {
  const host = document.createElement('div');
  document.body.append(host);
  const root = createRoot(host);
  const close = () => { root.unmount(); host.remove(); props.onClose?.(); };
  root.render(<CapeEditor {...props} onClose={close} />);
  return close;
}

/** Opens the HSV colour picker (NoRisk style); returns a close function. */
export function openColorPicker(props: Omit<ColorPickerProps, 'onClose'> & { onClose?: () => void }): () => void {
  const host = document.createElement('div');
  document.body.append(host);
  const root = createRoot(host);
  const close = () => { root.unmount(); host.remove(); props.onClose?.(); };
  root.render(<ColorPicker {...props} onClose={close} />);
  return close;
}

/** Opens the 3D skin preview dialog; returns a close function. */
export function openSkinDialog(props: Omit<SkinDialogProps, 'onClose'> & { onClose?: () => void }): () => void {
  const host = document.createElement('div');
  document.body.append(host);
  const root = createRoot(host);
  const close = () => { root.unmount(); host.remove(); props.onClose?.(); };
  root.render(<SkinDialog {...props} onClose={close} />);
  return close;
}
