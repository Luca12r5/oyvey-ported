// Entry for the React UI ported from the NoRisk Client launcher (GPL-3.0).
// app.ts keeps routing, auth and IPC; it mounts the shell once and renders the
// React tabs into the content area.
import { createRoot, type Root } from 'react-dom/client';
import { AppLayout } from './layout/AppLayout.tsx';
import { PlayTab, type PlayTabProps } from './tabs/PlayTab.tsx';
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
