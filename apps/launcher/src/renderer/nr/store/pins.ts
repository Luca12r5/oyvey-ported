// From the NoRisk launcher's usePinnedProfilesStore: pinned profiles stay on top.
import { createStore, useStore } from './store.ts';

function load(): string[] {
  try { const v = JSON.parse(localStorage.getItem('lego.pinnedProfiles') ?? '[]'); return Array.isArray(v) ? v.filter((x) => typeof x === 'string') : []; } catch { return []; }
}

export const pinStore = createStore<{ ids: string[] }>({ ids: load() });

export function togglePin(id: string): void {
  const ids = pinStore.get().ids;
  const next = ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id];
  pinStore.set({ ids: next });
  try { localStorage.setItem('lego.pinnedProfiles', JSON.stringify(next)); } catch { /* storage blocked */ }
}

export function usePins(): string[] {
  return useStore(pinStore, (s) => s.ids);
}
