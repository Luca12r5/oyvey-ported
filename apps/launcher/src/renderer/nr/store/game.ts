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
  launching: boolean;
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

export const gameStore = createStore<GameState>({ running: false, launching: false, status: null, error: null, pct: null, mb: null, consoleOpen: false, log: [], rev: 0, react: null });

export function useGame<S>(pick: (s: GameState) => S): S {
  return useStore(gameStore, pick);
}

let hooks: { lowPower(on: boolean): void; refresh(): void; autoConsole(): boolean } = { lowPower() {}, refresh() {}, autoConsole: () => false };

/** Wires the IPC events once (called from app.ts boot). */
export function initGame(h: typeof hooks): void {
  hooks = h;
  void api.gameRunning().then((r) => gameStore.set({ running: !!r }));
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
  api.on('game-exit', (r: { code: number; hint: string | null; diagnoses?: Diagnosis[]; profileId?: string }) => {
    hooks.lowPower(false);
    gameStore.set({ running: false, launching: false, status: null, pct: null, mb: null });
    if (r.code === 0) toast('Minecraft wurde beendet.');
    else if (r.profileId) showCrashDoctor(r.profileId, r.diagnoses ?? [], r.code);
    else toast(`Minecraft ist abgestürzt (Code ${r.code}). ${r.hint ?? 'Details im Log.'}`, 'error');
    hooks.refresh();
  });
}

export async function launchProfile(profileId: string): Promise<void> {
  const g = gameStore.get();
  if (g.launching) { api.cancelLaunch(); return; }
  if (g.running) { gameStore.set({ consoleOpen: true }); return; }
  gameStore.set({ launching: true, status: t('play.installing'), error: null, log: [], rev: g.rev + 1, react: { kind: 'launch', at: Date.now() }, consoleOpen: hooks.autoConsole() || g.consoleOpen });
  const r = await api.launch(profileId);
  if (!r.ok) {
    gameStore.set({ launching: false, status: null, pct: null, mb: null, error: r.error ?? 'Start fehlgeschlagen' });
    errorToast(new Error(r.error ?? 'Start fehlgeschlagen'));
    return;
  }
  gameStore.set({ launching: false, running: true, status: null, pct: null, mb: null, react: { kind: 'success', at: Date.now() } });
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
