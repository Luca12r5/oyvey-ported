// Duel-capable board games. Rules match the LEGO server (apps/backend/src/
// routes/duels.ts) so a launcher player and an in-game player can play
// together; against the AI the same rules run locally.

import { brick, label, type GameCtx, type GameDef, type GameRun } from './engine.ts';

type Rules = { board: number[]; winner: 0 | 1 | 2 | 3 } | 'illegal';

export function move(game: string, b0: number[], value: number, p: 1 | 2): Rules {
  const b = [...b0];
  if (game === 'tictactoe') {
    if (value < 0 || value > 8 || b[value]) return 'illegal';
    b[value] = p;
    for (const [x, y, z] of [[0, 1, 2], [3, 4, 5], [6, 7, 8], [0, 3, 6], [1, 4, 7], [2, 5, 8], [0, 4, 8], [2, 4, 6]]) if (b[x!] && b[x!] === b[y!] && b[y!] === b[z!]) return { board: b, winner: b[x!] as 1 | 2 };
    return { board: b, winner: b.every((c) => c) ? 3 : 0 };
  }
  if (game === 'connect4') {
    if (value < 0 || value > 6) return 'illegal';
    let row = -1;
    for (let r = 0; r < 6; r++) if (!b[r * 7 + value]) { row = r; break; }
    if (row < 0) return 'illegal';
    b[row * 7 + value] = p;
    const at = (r: number, c: number) => (r >= 0 && r < 6 && c >= 0 && c < 7 ? b[r * 7 + c] : -1);
    for (const [dr, dc] of [[0, 1], [1, 0], [1, 1], [1, -1]]) {
      let n = 1;
      for (const s of [1, -1]) { let r = row + dr! * s, c = value + dc! * s; while (at(r, c) === p) { n++; r += dr! * s; c += dc! * s; } }
      if (n >= 4) return { board: b, winner: p };
    }
    return { board: b, winner: b.every((c) => c) ? 3 : 0 };
  }
  if (game === 'nim') {
    const heap = Math.floor(value / 8), take = (value % 8) + 1;
    if (heap < 0 || heap >= b.length || take < 1 || take > 3 || b[heap]! < take) return 'illegal';
    b[heap]! -= take;
    return { board: b, winner: b.every((c) => c === 0) ? p : 0 };
  }
  if (game === 'reversi') {
    if (value < 0 || value > 63 || b[value]) return 'illegal';
    const o = p === 1 ? 2 : 1, flips: number[] = [];
    const x0 = value % 8, y0 = Math.floor(value / 8);
    for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1], [1, 1], [1, -1], [-1, 1], [-1, -1]]) {
      const line: number[] = [];
      let x = x0 + dx!, y = y0 + dy!;
      while (x >= 0 && x < 8 && y >= 0 && y < 8 && b[y * 8 + x] === o) { line.push(y * 8 + x); x += dx!; y += dy!; }
      if (line.length && x >= 0 && x < 8 && y >= 0 && y < 8 && b[y * 8 + x] === p) flips.push(...line);
    }
    if (!flips.length) return 'illegal';
    b[value] = p;
    for (const i of flips) b[i] = p;
    if (!b.every((c) => c)) return { board: b, winner: 0 };
    const n1 = b.filter((c) => c === 1).length, n2 = b.filter((c) => c === 2).length;
    return { board: b, winner: n1 === n2 ? 3 : n1 > n2 ? 1 : 2 };
  }
  return 'illegal';
}

export function initial(game: string): number[] {
  if (game === 'tictactoe') return Array(9).fill(0);
  if (game === 'connect4') return Array(42).fill(0);
  if (game === 'nim') return [3, 5, 7];
  if (game === 'reversi') { const b = Array(64).fill(0); b[27] = 2; b[28] = 1; b[35] = 1; b[36] = 2; return b; }
  return [0, 0];
}

const legal = (game: string, b: number[], p: 1 | 2): number[] => {
  const n = game === 'tictactoe' ? 9 : game === 'connect4' ? 7 : game === 'reversi' ? 64 : 24;
  const out: number[] = [];
  for (let v = 0; v < n; v++) if (move(game, b, v, p) !== 'illegal') out.push(v);
  return out;
};

/** Small minimax (depth-limited) good enough to be fun; random among equals. */
function ai(game: string, b: number[], p: 1 | 2): number {
  const o = p === 1 ? 2 : 1;
  const depth = game === 'tictactoe' ? 9 : game === 'connect4' ? 4 : game === 'nim' ? 8 : 2;
  const evalBoard = (bd: number[]): number => {
    if (game === 'reversi') { const corners = [0, 7, 56, 63]; let s = 0; bd.forEach((c, i) => { const w = corners.includes(i) ? 8 : 1; if (c === p) s += w; else if (c === o) s -= w; }); return s; }
    if (game === 'connect4') { let s = 0; for (let r = 0; r < 6; r++) { if (bd[r * 7 + 3] === p) s += 3; else if (bd[r * 7 + 3] === o) s -= 3; } return s; }
    return 0;
  };
  const search = (bd: number[], who: 1 | 2, d: number, alpha: number, beta: number): number => {
    const moves = legal(game, bd, who);
    if (!moves.length || d === 0) return evalBoard(bd);
    let best = who === p ? -Infinity : Infinity;
    for (const v of moves) {
      const r = move(game, bd, v, who);
      if (r === 'illegal') continue;
      let sc: number;
      if (r.winner === p) sc = 1000 + d;
      else if (r.winner === o) sc = -1000 - d;
      else if (r.winner === 3) sc = 0;
      else sc = search(r.board, who === 1 ? 2 : 1, d - 1, alpha, beta);
      if (who === p) { best = Math.max(best, sc); alpha = Math.max(alpha, sc); } else { best = Math.min(best, sc); beta = Math.min(beta, sc); }
      if (beta <= alpha) break;
    }
    return best;
  };
  const moves = legal(game, b, p);
  let best = -Infinity, pick: number[] = [];
  for (const v of moves) {
    const r = move(game, b, v, p);
    if (r === 'illegal') continue;
    const sc = r.winner === p ? 2000 : search(r.board, o, depth - 1, -Infinity, Infinity) + Math.random() * 0.01;
    if (sc > best) { best = sc; pick = [v]; } else if (sc === best) pick.push(v);
  }
  return pick[Math.floor(Math.random() * pick.length)] ?? moves[0] ?? 0;
}

function boardGame(game: 'tictactoe' | 'connect4' | 'nim' | 'reversi', ctx: GameCtx): GameRun {
  const c = ctx.colors;
  let b = initial(game);
  const me: 1 | 2 = ctx.duel ? ctx.duel.you : 1;
  const opp: 1 | 2 = me === 1 ? 2 : 1;
  let turn: 1 | 2 = 1;
  let done = false;
  let think = 0;
  let hover = -1;
  let nimSel = { heap: -1, n: 0 };
  const oppName = ctx.duel?.oppName ?? 'KI';
  const pieceColor = (p: number) => (p === 1 ? c.red : c.yellow);
  const finish = (w: 0 | 1 | 2 | 3) => {
    if (!w) return;
    done = true;
    if (w === me) { ctx.sound('win'); ctx.over('Gewonnen!', 1); } else if (w === 3) ctx.over('Unentschieden', 0); else { ctx.sound('lose'); ctx.over(`${oppName} gewinnt`, 0); }
  };
  const passIfStuck = () => {
    if (game !== 'reversi' || done) return;
    if (!legal('reversi', b, turn).length) {
      const other: 1 | 2 = turn === 1 ? 2 : 1;
      if (legal('reversi', b, other).length) turn = other;
      else { const n1 = b.filter((x) => x === 1).length, n2 = b.filter((x) => x === 2).length; finish(n1 === n2 ? 3 : n1 > n2 ? 1 : 2); }
    }
  };
  const play = (v: number, who: 1 | 2, local: boolean) => {
    const r = move(game, b, v, who);
    if (r === 'illegal') return false;
    b = r.board;
    ctx.sound('click');
    if (local && ctx.duel) ctx.duel.send(v);
    turn = who === 1 ? 2 : 1;
    finish(r.winner);
    passIfStuck();
    return true;
  };
  ctx.duel?.onMove((v) => { if (!done && turn === opp) play(v, opp, false); });

  // Layout.
  const geo = () => {
    const W = ctx.w, H = ctx.h - 40;
    if (game === 'tictactoe') { const s = Math.min(W, H) * 0.8; return { x: (W - s) / 2, y: 40 + (H - s) / 2, cell: s / 3, cols: 3, rows: 3 }; }
    if (game === 'connect4') { const cell = Math.min(W / 7.6, H / 6.6); return { x: (W - cell * 7) / 2, y: 40 + (H - cell * 6) / 2, cell, cols: 7, rows: 6 }; }
    if (game === 'reversi') { const cell = Math.min(W, H) * 0.86 / 8; return { x: (W - cell * 8) / 2, y: 40 + (H - cell * 8) / 2, cell, cols: 8, rows: 8 }; }
    const cell = Math.min(W / 9, H / 4.6); return { x: (W - cell * 7) / 2, y: 50 + (H - cell * 3.6) / 2, cell, cols: 7, rows: 3 };
  };
  const cellAt = (x: number, y: number) => {
    const G = geo();
    const cx = Math.floor((x - G.x) / G.cell), cy = Math.floor((y - G.y) / G.cell);
    if (cx < 0 || cy < 0 || cx >= G.cols || cy >= G.rows) return -1;
    if (game === 'connect4') return cx;
    if (game === 'nim') return cy * 16 + cx; // heap*16 + stick index
    return cy * G.cols + cx;
  };

  return {
    frame(dt) {
      const g = ctx.g, G = geo();
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      const status = done ? '' : turn === me ? 'Du bist dran' : ctx.duel ? `${oppName} ist dran…` : 'KI denkt…';
      label(g, status, ctx.w / 2, 20, 11, turn === me ? c.accent : c.muted);
      if (game === 'tictactoe') {
        for (let i = 0; i < 9; i++) {
          const x = G.x + (i % 3) * G.cell, y = G.y + Math.floor(i / 3) * G.cell;
          g.fillStyle = hover === i && !b[i] && turn === me ? c.panel : 'rgba(255,255,255,.03)';
          g.fillRect(x + 3, y + 3, G.cell - 6, G.cell - 6);
          if (b[i]) brick(g, x + G.cell * 0.18, y + G.cell * 0.18, G.cell * 0.64, G.cell * 0.64, pieceColor(b[i]!));
        }
      } else if (game === 'connect4') {
        g.fillStyle = '#1e3a8a';
        g.fillRect(G.x - 8, G.y - 8, G.cell * 7 + 16, G.cell * 6 + 16);
        for (let r = 0; r < 6; r++) for (let col = 0; col < 7; col++) {
          const x = G.x + col * G.cell, y = G.y + (5 - r) * G.cell, v = b[r * 7 + col];
          g.beginPath();
          g.arc(x + G.cell / 2, y + G.cell / 2, G.cell * 0.4, 0, Math.PI * 2);
          g.fillStyle = v ? pieceColor(v) : hover === col && turn === me ? 'rgba(255,255,255,.18)' : '#0b0910';
          g.fill();
        }
      } else if (game === 'reversi') {
        g.fillStyle = '#14532d';
        g.fillRect(G.x, G.y, G.cell * 8, G.cell * 8);
        const moves = turn === me && !done ? legal('reversi', b, me) : [];
        for (let i = 0; i < 64; i++) {
          const x = G.x + (i % 8) * G.cell, y = G.y + Math.floor(i / 8) * G.cell;
          g.strokeStyle = 'rgba(0,0,0,.35)';
          g.strokeRect(x, y, G.cell, G.cell);
          if (b[i]) { g.beginPath(); g.arc(x + G.cell / 2, y + G.cell / 2, G.cell * 0.4, 0, Math.PI * 2); g.fillStyle = b[i] === 1 ? '#111' : '#eee'; g.fill(); }
          else if (moves.includes(i)) { g.beginPath(); g.arc(x + G.cell / 2, y + G.cell / 2, G.cell * 0.1, 0, Math.PI * 2); g.fillStyle = hover === i ? c.accent : 'rgba(255,255,255,.25)'; g.fill(); }
        }
        label(g, `● ${b.filter((x) => x === 1).length}   ○ ${b.filter((x) => x === 2).length}`, ctx.w / 2, ctx.h - 12, 10, c.text);
      } else {
        // Nim: three heaps of LEGO sticks; click a stick = take it and all to its right (max 3).
        for (let h = 0; h < 3; h++) for (let s = 0; s < b[h]!; s++) {
          const x = G.x + s * G.cell, y = G.y + h * G.cell * 1.2;
          const sel = nimSel.heap === h && s >= b[h]! - nimSel.n;
          brick(g, x + 4, y + 4, G.cell - 8, G.cell * 0.9, sel ? c.accent : hover === h * 16 + s && turn === me ? c.yellow : c.blue);
        }
        label(g, 'Nimm 1–3 Steine aus einer Reihe · wer den letzten nimmt, gewinnt', ctx.w / 2, ctx.h - 12, 8, c.muted, 'center', 'Inter, sans-serif');
      }
      // AI turn.
      if (!ctx.duel && !done && turn === opp) {
        think += dt;
        if (think > 0.55) { think = 0; play(ai(game, b, opp), opp, false); }
      }
    },
    move(x, y) { hover = cellAt(x, y); },
    click(x, y) {
      if (done || turn !== me) return;
      const v = cellAt(x, y);
      if (v < 0) return;
      if (game === 'nim') {
        const heap = Math.floor(v / 16), idx = v % 16;
        const take = b[heap]! - idx;
        if (take < 1 || take > 3) { ctx.sound('hit'); return; }
        nimSel = { heap, n: take };
        play(heap * 8 + (take - 1), me, true);
        nimSel = { heap: -1, n: 0 };
        return;
      }
      if (!play(v, me, true)) ctx.sound('hit');
    },
    destroy() { ctx.duel?.leave(); },
  };
}

function rps(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  const names = ['Stein', 'Papier', 'Schere'];
  let mine = -1, theirs = -1, wins = [0, 0], msg = 'Wähle!', done = false, reveal = 0;
  const oppName = ctx.duel?.oppName ?? 'KI';
  const resolve = () => {
    const d = mine === theirs ? 0 : (mine - theirs + 3) % 3 === 1 ? 1 : -1;
    if (d > 0) wins[0]!++;
    if (d < 0) wins[1]!++;
    msg = d > 0 ? 'Punkt für dich!' : d < 0 ? `Punkt für ${oppName}` : 'Unentschieden';
    ctx.sound(d > 0 ? 'point' : d < 0 ? 'hit' : 'click');
    reveal = 1.4;
    if (wins[0]! >= 3 || wins[1]! >= 3) { done = true; ctx.over(wins[0]! >= 3 ? 'Gewonnen!' : `${oppName} gewinnt`, wins[0]); ctx.sound(wins[0]! >= 3 ? 'win' : 'lose'); }
  };
  ctx.duel?.onMove((v) => { theirs = v; if (mine >= 0) resolve(); });
  const btn = (i: number) => ({ x: ctx.w / 2 - 210 + i * 150, y: ctx.h - 150, w: 120, h: 110 });
  return {
    frame(dt) {
      const g = ctx.g;
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      label(g, `Du ${wins[0]} : ${wins[1]} ${oppName}`, ctx.w / 2, 30, 14, c.text);
      label(g, 'Erster mit 3 Punkten gewinnt', ctx.w / 2, 56, 8, c.muted, 'center', 'Inter, sans-serif');
      label(g, msg, ctx.w / 2, ctx.h / 2 - 50, 12, c.accent);
      if (reveal > 0 && mine >= 0 && theirs >= 0) {
        label(g, `${names[mine]}  vs  ${names[theirs]}`, ctx.w / 2, ctx.h / 2, 12, c.text);
        reveal -= dt;
        if (reveal <= 0 && !done) { mine = -1; theirs = -1; msg = 'Nächste Runde – wähle!'; }
      } else if (mine >= 0) label(g, `${names[mine]} gewählt – warte auf ${oppName}…`, ctx.w / 2, ctx.h / 2, 9, c.muted);
      for (let i = 0; i < 3; i++) {
        const B = btn(i);
        brick(g, B.x, B.y, B.w, B.h, mine === i ? c.accent : [c.red, c.blue, c.green][i]!);
        label(g, names[i]!, B.x + B.w / 2, B.y + B.h / 2 + 8, 9, '#fff');
      }
    },
    click(x, y) {
      if (done || mine >= 0) return;
      for (let i = 0; i < 3; i++) {
        const B = btn(i);
        if (x >= B.x && x <= B.x + B.w && y >= B.y && y <= B.y + B.h) {
          mine = i;
          ctx.sound('click');
          if (ctx.duel) ctx.duel.send(i);
          else { theirs = Math.floor(Math.random() * 3); resolve(); }
        }
      }
    },
    destroy() { ctx.duel?.leave(); },
  };
}

export const DUEL_GAMES: GameDef[] = [
  { id: 'connect4', name: 'Vier gewinnt', tagline: 'Vier in einer Reihe', icon: 'grid', duel: true, start: (c) => boardGame('connect4', c) },
  { id: 'tictactoe', name: 'Tic-Tac-Toe', tagline: 'Drei in einer Reihe', icon: 'grid', duel: true, start: (c) => boardGame('tictactoe', c) },
  { id: 'rps', name: 'Schere Stein Papier', tagline: 'Best of five', icon: 'gamepad', duel: true, start: rps },
  { id: 'reversi', name: 'Reversi', tagline: 'Dreh die Steine um', icon: 'grid', duel: true, start: (c) => boardGame('reversi', c) },
  { id: 'nim', name: 'Nim', tagline: 'Wer den letzten Stein nimmt, gewinnt', icon: 'brick', duel: true, start: (c) => boardGame('nim', c) },
];
