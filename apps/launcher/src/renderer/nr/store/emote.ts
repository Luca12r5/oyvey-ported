// Emote requests from the emote bar to the 3D character on the play screen.
import { createStore, useStore } from './store.ts';

export interface EmoteState {
  /** Emote that is playing right now (set by the character). */
  playing: string | null;
  /** Last request: an emote id, or null to stop. */
  request: { id: string | null; at: number } | null;
  favorites: string[];
}

const FAV_KEY = 'lego.emoteFavs';
const DEFAULT_FAVS = ['car', 'chair', 'rocket', 'dab', 'floss', 'explode'];
function loadFavs(): string[] {
  try {
    const v = JSON.parse(localStorage.getItem(FAV_KEY) ?? 'null') as unknown;
    return Array.isArray(v) ? v.filter((x): x is string => typeof x === 'string').slice(0, 12) : DEFAULT_FAVS;
  } catch { return DEFAULT_FAVS; }
}

export const emoteStore = createStore<EmoteState>({ playing: null, request: null, favorites: loadFavs() });
export const useEmote = <S,>(pick: (s: EmoteState) => S): S => useStore(emoteStore, pick);

export function playEmote(id: string | null): void {
  emoteStore.set({ request: { id, at: performance.now() } });
}

export function toggleFavorite(id: string): void {
  const f = emoteStore.get().favorites;
  const next = f.includes(id) ? f.filter((x) => x !== id) : [...f, id].slice(-12);
  emoteStore.set({ favorites: next });
  try { localStorage.setItem(FAV_KEY, JSON.stringify(next)); } catch { /* storage blocked */ }
}
