// Mods page: the NoRisk-launcher mods tab (React, nr/tabs/ModsTab.tsx) with
// content tabs, search, filters, INSTALL + profile picker and the installed list.

import { go, onLeave, state } from '../app.ts';
import { api } from '../ui.ts';
import { modsStore, renderMods } from '../nr/mount.tsx';

/** Opens the page on the installed files of one profile (from a profile's MODS button). */
export function openModsFor(id: string): void {
  modsStore.set({ profileId: id, tab: 'installed' });
}

export function modsPage(el: HTMLElement): void {
  const s = state.settings;
  onLeave(renderMods(el, {
    profiles: s.profiles,
    selectedProfile: s.selectedProfile,
    curseforge: !!s.curseforgeKey,
    onEditProfile: () => void go('profiles'),
    onModpackInstalled: async (profileId) => {
      state.settings = await api.setSettings({ selectedProfile: profileId });
      void go('profiles');
    },
  }));
}
