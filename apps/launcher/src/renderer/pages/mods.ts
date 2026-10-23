// Mods page: the NoRisk-launcher mods tab (React, nr/tabs/ModsTab.tsx) with
// content tabs, search, filters, INSTALL + profile picker. Installed content is in the profile view.

import { go, onLeave, state } from '../app.ts';
import { api } from '../ui.ts';
import { modsStore, renderMods } from '../nr/mount.tsx';
import type { ProjectType } from '../../core/modrinth.ts';

/** Opens the Modrinth search for one profile and content type (from a profile's "add" button). */
export function openModsFor(id: string, tab: ProjectType = 'mod'): void {
  modsStore.set({ profileId: id, tab });
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
