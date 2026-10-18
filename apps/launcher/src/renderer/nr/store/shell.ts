// Bridge between the vanilla launcher (app.ts owns routing, auth, IPC) and the
// React shell ported from the NoRisk launcher. app.ts pushes state in and
// registers the actions the shell buttons call.
import { createStore, useStore } from './store.ts';

/** Rail icons (mapped to the launcher's own icon set in lib/iconify.tsx). */
export const NAV_ICONS: Record<string, string> = {
  play: 'solar:play-bold',
  profiles: 'solar:user-id-bold',
  mods: 'solar:widget-bold',
  skins: 'solar:emoji-funny-circle-bold',
  capes: 'solar:shop-bold',
  settings: 'solar:settings-bold',
};

export interface NavItem { id: string; icon: string; label: string; isAction?: boolean }

export interface ShellState {
  page: string;
  nav: NavItem[];
  canBack: boolean;
  canFwd: boolean;
  version: string;
  online: number | null;
  signedIn: boolean;
  name: string;
  head: string;
  coins: number | null;
  running: number;
  unread: number;
  maximized: boolean;
  fullscreen: boolean;
}

export const shellStore = createStore<ShellState>({
  page: 'play', nav: [], canBack: false, canFwd: false, version: '', online: null,
  signedIn: false, name: '', head: '', coins: null, running: 0, unread: 0, maximized: false, fullscreen: false,
});

type Anchor = HTMLElement;
export interface ShellActions {
  go(id: string): void;
  back(): void;
  fwd(): void;
  bell(a: Anchor): void;
  account(a: Anchor): void;
  instances(a: Anchor): void;
  friends(): void;
  more(a: Anchor): void;
  credits(): void;
  window(cmd: 'minimize' | 'maximize' | 'fullscreen' | 'close'): void;
}

const noop = () => {};
export const actions: ShellActions = { go: noop, back: noop, fwd: noop, bell: noop, account: noop, instances: noop, friends: noop, more: noop, credits: noop, window: noop };

export function useShell<S>(pick: (s: ShellState) => S): S {
  return useStore(shellStore, pick);
}
