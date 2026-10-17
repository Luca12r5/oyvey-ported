// Small canvas game engine for the launcher's Spiele page. Every game draws
// into one canvas (HiDPI), gets keyboard/mouse input and reports its score.
// Duel games can play against the built-in AI or against another LEGO user
// through the LEGO server (moves are checked there).

export interface DuelLink {
  you: 1 | 2;
  oppName: string;
  send(value: number): void;
  /** Called with the opponent's moves (server order). */
  onMove(cb: (value: number) => void): void;
  leave(): void;
}

export interface GameCtx {
  canvas: HTMLCanvasElement;
  g: CanvasRenderingContext2D;
  /** CSS pixel size of the play area. */
  w: number;
  h: number;
  score(n: number): void;
  over(text: string, score?: number): void;
  sound(kind: 'click' | 'win' | 'lose' | 'hit' | 'point'): void;
  duel: DuelLink | null;
  colors: { bg: string; panel: string; line: string; text: string; muted: string; accent: string; accent2: string; red: string; yellow: string; blue: string; green: string };
}

export interface GameRun {
  frame?(dt: number): void;
  key?(e: KeyboardEvent, down: boolean): void;
  click?(x: number, y: number): void;
  move?(x: number, y: number): void;
  destroy?(): void;
}

export interface GameDef {
  id: string;
  name: string;
  tagline: string;
  icon: string;
  /** Can be played as a duel through the LEGO server (same ids as the server). */
  duel?: boolean;
  higherIsBetter?: boolean;
  start(ctx: GameCtx): GameRun;
}

let audio: AudioContext | null = null;
export function beep(kind: 'click' | 'win' | 'lose' | 'hit' | 'point'): void {
  try {
    audio ??= new AudioContext();
    const a = audio, t = a.currentTime;
    const o = a.createOscillator(), g = a.createGain();
    const spec = { click: [660, 0.04, 'square'], hit: [220, 0.06, 'square'], point: [880, 0.08, 'triangle'], win: [523, 0.4, 'triangle'], lose: [196, 0.45, 'sawtooth'] } as const;
    const [f, d, type] = spec[kind];
    o.type = type;
    o.frequency.setValueAtTime(f, t);
    if (kind === 'win') { o.frequency.setValueAtTime(659, t + 0.12); o.frequency.setValueAtTime(784, t + 0.24); }
    if (kind === 'lose') o.frequency.exponentialRampToValueAtTime(90, t + d);
    g.gain.setValueAtTime(0.06, t);
    g.gain.exponentialRampToValueAtTime(0.0001, t + d);
    o.connect(g).connect(a.destination);
    o.start(t);
    o.stop(t + d + 0.02);
  } catch { /* no audio */ }
}

/** A LEGO brick (with studs) for game boards. */
export function brick(g: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, color: string, studs = true): void {
  g.fillStyle = color;
  g.fillRect(x, y, w, h);
  g.fillStyle = 'rgba(255,255,255,.18)';
  g.fillRect(x, y, w, Math.max(1, h * 0.08));
  g.fillStyle = 'rgba(0,0,0,.28)';
  g.fillRect(x, y + h - Math.max(1, h * 0.12), w, Math.max(1, h * 0.12));
  if (studs && w > 10) {
    const n = Math.max(1, Math.round(w / h));
    for (let i = 0; i < n; i++) {
      const cx = x + (w / n) * (i + 0.5), cy = y + h * 0.42, r = Math.min(w / n, h) * 0.24;
      g.beginPath();
      g.arc(cx, cy, r, 0, Math.PI * 2);
      g.fillStyle = 'rgba(255,255,255,.12)';
      g.fill();
      g.strokeStyle = 'rgba(0,0,0,.25)';
      g.lineWidth = 1;
      g.stroke();
    }
  }
}

export function label(g: CanvasRenderingContext2D, text: string, x: number, y: number, size: number, color: string, align: CanvasTextAlign = 'center', font = '"Press Start 2P", monospace'): void {
  g.font = `${size}px ${font}`;
  g.fillStyle = color;
  g.textAlign = align;
  g.textBaseline = 'middle';
  g.fillText(text, x, y);
}
