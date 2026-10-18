// Tiny external store (the NoRisk launcher uses zustand; this is the same idea
// in a few lines, so the vanilla parts of the launcher can push state in).
import { useSyncExternalStore } from 'react';

export interface Store<T> {
  get(): T;
  set(patch: Partial<T>): void;
  subscribe(fn: () => void): () => void;
}

export function createStore<T extends object>(initial: T): Store<T> {
  let state = initial;
  const subs = new Set<() => void>();
  return {
    get: () => state,
    set(patch) {
      state = { ...state, ...patch };
      for (const fn of subs) fn();
    },
    subscribe(fn) {
      subs.add(fn);
      return () => subs.delete(fn);
    },
  };
}

export function useStore<T, S>(store: Store<T>, pick: (s: T) => S): S {
  return useSyncExternalStore(store.subscribe, () => pick(store.get()));
}
