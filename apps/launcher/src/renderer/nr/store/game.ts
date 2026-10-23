// Launch state, progress and the game log, kept for the whole session (not per
// page) so the console and the LAUNCH button survive tab switches.
import { createStore, useStore } from './store.ts';
import { api, errorToast, toast } from '../../ui.ts';
import { parseLine, type LogEntry } from '../../logparse.ts';
import { showCrashDoctor } from '../../crashdoc-ui.ts';
import type { Diagnosis } from '../../../core/crashdoc.ts';
import type { LaunchProgress } from '../../../ipc-types.ts';
import { t } from '../../i18n.ts';

export interface GameState {
  running: boolean;
  /** Profiles whose game is running (several at once with different accounts). */
  runningIds: string[];
  launching: boolean;
  /** Profile currently being installed/started. */
  launchingId: string | null;
  /** Current install/launch step for the LAUNCH button's sub line. */
  status: string | null;
  error: string | null;
  pct: number | null;
  mb: string | null;
  consoleOpen: boolean;
  log: LogEntry[];
  rev: number;
  /** Last avatar reaction request (play page listens). */
  react: { kind: 'launch' | 'success'; at: number } | null;
}

export const gameStore = createStore<GameState>({ running: false, runningIds: [], launching: false, launchingId: null, status: null, error: null, pct: null, mb: null, consoleOpen: false, log: [], rev: 0, react: null });

export function useGame<S>(pick: (s: GameState) => S): S {
  return useStore(gameStore, pick);
}

let hooks: { lowPower(on: boolean): void; refresh(): void; autoConsole(): boolean } = { lowPower() {}, refresh() {}, autoConsole: () => false };

/** The separate console window: the log so far, then live lines. */
export async function initConsoleWindow(): Promise<void> {
  const hist = await api.consoleHistory().catch(() => []);
  gameStore.set({ log: hist.map((l) => parseLine(l.line, l.stream)) });
  let pending: LogEntry[] = [];
  let queued = false;
  api.on('game-log', (l: { line: string; stream: string }) => {
    // A new launch starts with the launcher's command line: start a fresh log.
    if (l.line.startsWith('[installer] ') && !pending.length && gameStore.get().log.some((e) => !e.text.startsWith('[installer]'))) gameStore.set({ log: [] });
    pending.push(parseLine(l.line, l.stream));
    if (queued) return;
    queued = true;
    requestAnimationFrame(() => {
      queued = false;
      const log = gameStore.get().log.concat(pending);
      pending = [];
      gameStore.set({ log: log.length > 5000 ? log.slice(-4000) : log, rev: gameStore.get().rev + 1 });
    });
  });
}

/** Wires the IPC events once (called from app.ts boot). */
export function initGame(h: typeof hooks): void {
  hooks = h;
  void api.runningProfiles().then((ids) => gameStore.set({ runningIds: ids, running: ids.length > 0 })).catch(() => {});
  // Also sent when the launcher restarts a game by itself (account switch from in game).
  api.on('game-started', (r: { profileId: string }) => {
    const ids = [...new Set([...gameStore.get().runningIds, r.profileId])];
    gameStore.set({ runningIds: ids, running: true, launching: false, launchingId: null, status: null, pct: null, mb: null });
  });
  api.on('launch-progress', (p: LaunchProgress) => {
    const pct = p.total ? Math.round(((p.done ?? 0) / p.total) * 100) : null;
    const mb = p.totalBytes ? `${Math.round((p.bytes ?? 0) / 1048576)} / ${Math.round(p.totalBytes / 1048576)} MB` : null;
    gameStore.set({ status: `${p.step}${pct !== null ? ` · ${pct} %` : ''}`, pct, mb, error: null });
  });
  let pending: LogEntry[] = [];
  let queued = false;
  api.on('game-log', (l: { line: string; stream: string }) => {
    pending.push(parseLine(l.line, l.stream));
    if (queued) return;
    queued = true;
    // Batch log lines to one store update per frame.
    requestAnimationFrame(() => {
      queued = false;
      const log = gameStore.get().log.concat(pending);
      pending = [];
      gameStore.set({ log: log.length > 5000 ? log.slice(-4000) : log, rev: gameStore.get().rev + 1 });
    });
  });
  api.on('game-exit', (r: { code: number; hint: string | null; diagnoses?: Diagnosis[]; profileId?: string; switching?: boolean }) => {
    const ids = gameStore.get().runningIds.filter((x) => x !== r.profileId);
    if (!ids.length) hooks.lowPower(false);
    if (r.switching && r.profileId) {
      gameStore.set({ runningIds: ids, running: ids.length > 0, launching: true, launchingId: r.profileId, status: 'Konto wird gewechselt …' });
      toast('Konto wird gewechselt – Minecraft startet neu …', 'info');
      return;
    }
    gameStore.set({ runningIds: ids, running: ids.length > 0, launching: false, launchingId: null, status: null, pct: null, mb: null });
    if (r.code === 0) toast('Minecraft wurde beendet.');
    else if (r.profileId) showCrashDoctor(r.profileId, r.diagnoses ?? [], r.code);
    else toast(`Minecraft ist abgestürzt (Code ${r.code}). ${r.hint ?? 'Details im Log.'}`, 'error');
    hooks.refresh();
  });
}

export async function launchProfile(profileId: string): Promise<void> {
  const g = gameStore.get();
  if (g.launching) {
    if (g.launchingId === profileId) api.cancelLaunch();
    else toast('Warte kurz, bis das andere Spiel gestartet ist.', 'info');
    return;
  }
  // This profile already runs: show its console. Other profiles may start next to it.
  if (g.runningIds.includes(profileId)) { void api.openConsole(); return; }
  gameStore.set({ launching: true, launchingId: profileId, status: t('play.installing'), error: null, ...(g.running ? {} : { log: [] }), rev: g.rev + 1, react: { kind: 'launch', at: Date.now() } });
  // The console is its own window (never inside the game).
  if (hooks.autoConsole()) void api.openConsole();
  const r = await api.launch(profileId);
  if (!r.ok) {
    gameStore.set({ launching: false, launchingId: null, status: null, pct: null, mb: null, error: r.error ?? 'Start fehlgeschlagen' });
    errorToast(new Error(r.error ?? 'Start fehlgeschlagen'));
    return;
  }
  gameStore.set({ launching: false, launchingId: null, running: true, runningIds: [...new Set([...gameStore.get().runningIds, profileId])], status: null, pct: null, mb: null, react: { kind: 'success', at: Date.now() } });
  hooks.lowPower(true);
  confetti();
  toast(t('play.fun'), 'ok');
}

/** Short colourful burst over the window when the game starts (skipped with reduced motion). */
function confetti(): void {
  if (document.body.classList.contains('reduced-motion') || matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const box = document.createElement('div');
  box.className = 'lx-confetti';
  const cols = ['#3d8bfd', '#1ed760', '#ffd166', '#ff6f61', '#d68cff', '#4fd1c5', '#e3000b'];
  for (let i = 0; i < 90; i++) {
    const p = document.createElement('i');
    p.style.left = `${Math.random() * 100}%`;
    p.style.background = cols[i % cols.length]!;
    p.style.animationDelay = `${Math.random() * 0.4}s`;
    p.style.animationDuration = `${1.6 + Math.random() * 1.4}s`;
    p.style.setProperty('--dx', `${(Math.random() - 0.5) * 240}px`);
    p.style.setProperty('--rot', `${Math.random() * 900}deg`);
    box.append(p);
  }
  document.body.append(box);
  setTimeout(() => box.remove(), 3400);
}
