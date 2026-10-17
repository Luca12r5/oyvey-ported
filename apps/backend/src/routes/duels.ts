// Duels between LEGO users (launcher or in-game client): invite → accept →
// moves. The server is the referee: it checks privacy and blocks, whose turn
// it is and whether a move is legal, and pushes every step to both players
// (SSE for the launcher, an inbox the in-game client polls). Matches live in
// memory only and expire; nothing about a duel is stored permanently.
// Synced emotes ride along here as well (POST /api/emotes/play).

import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { badRequest, conflict, forbidden, notFound } from '../http.ts';
import { newId, obj, str } from '../security.ts';
import { canSee, findUser, getUser, isBlocked, publicUser } from '../services/users.ts';

export const DUEL_GAMES = ['connect4', 'tictactoe', 'rps', 'reversi', 'nim'] as const;
type Game = (typeof DUEL_GAMES)[number];

interface Match {
  id: string;
  game: Game;
  /** a = inviter (moves first), b = invited. */
  a: string;
  b: string;
  status: 'invited' | 'active' | 'over';
  board: number[];
  round: number;
  /** rps: hidden choices of the current round. */
  pending: Record<string, number>;
  winner: string | null;
  createdAt: number;
  touchedAt: number;
}

interface InboxMsg { k: string; match: string; game: Game; from: { id: string; uuid: string; name: string }; round?: number; value?: number; winner?: string | null; at: number }

const INVITE_TTL = 90_000;
const IDLE_TTL = 10 * 60_000;

/** Pure rules, exported for tests. Returns the new board or an error string. */
export function applyMove(game: Game, board: number[], round: number, value: number, player: 1 | 2): { board: number[]; winner: 0 | 1 | 2 | 3 } | string {
  const b = [...board];
  if (!Number.isInteger(value)) return 'bad_value';
  if (game === 'tictactoe') {
    if (value < 0 || value > 8 || b[value] !== 0) return 'illegal';
    b[value] = player;
    const L = [[0, 1, 2], [3, 4, 5], [6, 7, 8], [0, 3, 6], [1, 4, 7], [2, 5, 8], [0, 4, 8], [2, 4, 6]];
    for (const [x, y, z] of L) if (b[x!] && b[x!] === b[y!] && b[y!] === b[z!]) return { board: b, winner: b[x!] as 1 | 2 };
    return { board: b, winner: b.every((c) => c) ? 3 : 0 };
  }
  if (game === 'connect4') {
    // 7 columns × 6 rows, row 0 at the bottom.
    if (value < 0 || value > 6) return 'illegal';
    let row = -1;
    for (let r = 0; r < 6; r++) if (b[r * 7 + value] === 0) { row = r; break; }
    if (row < 0) return 'illegal';
    b[row * 7 + value] = player;
    const at = (r: number, c: number) => (r >= 0 && r < 6 && c >= 0 && c < 7 ? b[r * 7 + c] : -1);
    for (const [dr, dc] of [[0, 1], [1, 0], [1, 1], [1, -1]] as const) {
      let n = 1;
      for (const s of [1, -1]) { let r = row + dr * s, c = value + dc * s; while (at(r, c) === player) { n++; r += dr * s; c += dc * s; } }
      if (n >= 4) return { board: b, winner: player };
    }
    return { board: b, winner: b.every((c) => c) ? 3 : 0 };
  }
  if (game === 'nim') {
    // Board: [heap sizes…]; value = heap * 8 + (take-1), take 1..3.
    const heap = Math.floor(value / 8), take = (value % 8) + 1;
    if (heap < 0 || heap >= b.length || take < 1 || take > 3 || b[heap]! < take) return 'illegal';
    b[heap]! -= take;
    // Whoever takes the last stick wins.
    return { board: b, winner: b.every((c) => c === 0) ? player : 0 };
  }
  if (game === 'reversi') {
    if (value < 0 || value > 63 || b[value] !== 0) return 'illegal';
    const other = player === 1 ? 2 : 1;
    const flips: number[] = [];
    const x0 = value % 8, y0 = Math.floor(value / 8);
    for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1], [1, 1], [1, -1], [-1, 1], [-1, -1]] as const) {
      const line: number[] = [];
      let x = x0 + dx, y = y0 + dy;
      while (x >= 0 && x < 8 && y >= 0 && y < 8 && b[y * 8 + x] === other) { line.push(y * 8 + x); x += dx; y += dy; }
      if (line.length && x >= 0 && x < 8 && y >= 0 && y < 8 && b[y * 8 + x] === player) flips.push(...line);
    }
    if (!flips.length) return 'illegal';
    b[value] = player;
    for (const i of flips) b[i] = player;
    const full = b.every((c) => c);
    if (!full) return { board: b, winner: 0 };
    const p1 = b.filter((c) => c === 1).length, p2 = b.filter((c) => c === 2).length;
    return { board: b, winner: p1 === p2 ? 3 : p1 > p2 ? 1 : 2 };
  }
  return 'bad_game';
}

function initialBoard(game: Game): number[] {
  if (game === 'tictactoe') return Array(9).fill(0);
  if (game === 'connect4') return Array(42).fill(0);
  if (game === 'nim') return [3, 5, 7];
  if (game === 'reversi') { const b = Array(64).fill(0); b[27] = 2; b[28] = 1; b[35] = 1; b[36] = 2; return b; }
  return [0, 0]; // rps: wins of a, wins of b (best of 5)
}

/** rps: 0 rock, 1 paper, 2 scissors → 1 if x beats y, -1 if y beats x, 0 draw. */
export function rpsResult(x: number, y: number): number {
  if (x === y) return 0;
  return (x - y + 3) % 3 === 1 ? 1 : -1;
}

export function registerDuels(app: App): void {
  const r = app.router;
  const db = app.db;
  const matches = new Map<string, Match>();
  const inbox = new Map<string, InboxMsg[]>();

  const gc = () => {
    const now = Date.now();
    for (const [id, m] of matches) if ((m.status === 'invited' && now - m.createdAt > INVITE_TTL) || now - m.touchedAt > IDLE_TTL) matches.delete(id);
    for (const [u, list] of inbox) { const keep = list.filter((x) => now - x.at < 120_000); if (keep.length) inbox.set(u, keep); else inbox.delete(u); }
  };
  const push = (to: string, msg: InboxMsg) => {
    const list = inbox.get(to) ?? [];
    list.push(msg);
    inbox.set(to, list.slice(-50));
    app.events.send(to, 'duel', msg);
  };
  const who = (id: string) => { const u = getUser(db, id)!; return { id: u.id, uuid: u.mc_uuid, name: u.mc_name }; };
  const mine = (ctx: AppCtx, id: string) => {
    const u = requireUser(ctx);
    const m = matches.get(id);
    if (!m || (m.a !== u.id && m.b !== u.id)) throw notFound('Duel not found');
    m.touchedAt = Date.now();
    return { u, m, opp: m.a === u.id ? m.b : m.a };
  };
  const view = (m: Match, me: string) => ({
    id: m.id, game: m.game, status: m.status, round: m.round, board: m.board, winner: m.winner,
    you: m.a === me ? 1 : 2, opponent: publicUser(getUser(db, m.a === me ? m.b : m.a)!),
  });

  // In-game client polls this (the launcher also gets the same messages via SSE).
  r.get('/api/duels/inbox', (ctx: AppCtx) => {
    gc();
    const u = requireUser(ctx);
    const since = Number(ctx.query.get('since') ?? 0) || 0;
    return { messages: (inbox.get(u.id) ?? []).filter((x) => x.at > since), now: Date.now() };
  }, { rate: ['duel-inbox', 120] });

  r.get('/api/duels/games', () => ({ games: DUEL_GAMES }), { auth: 'none' });

  r.post('/api/duels', (ctx: AppCtx) => {
    gc();
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const game = str(b, 'game', 2, 16) as Game;
    if (!DUEL_GAMES.includes(game)) throw badRequest('invalid_field', 'Unknown game');
    const target = findUser(db, str(b, 'to', 2, 40));
    if (!target) throw notFound('Player not found (they need to use the LEGO launcher or client)');
    if (target.id === u.id) throw badRequest('invalid_field', 'You cannot duel yourself');
    if (isBlocked(db, target.id, u.id) || isBlocked(db, u.id, target.id)) throw forbidden('Not possible');
    if (!canSee(db, target.privacy_duels ?? 'everyone', target.id, u.id)) throw forbidden(target.privacy_duels === 'nobody' ? 'This player does not accept duels' : 'This player only accepts duels from friends');
    const open = [...matches.values()].filter((m) => m.a === u.id && m.status === 'invited');
    if (open.length >= 3) throw conflict('Too many open invites');
    const m: Match = { id: newId('d_'), game, a: u.id, b: target.id, status: 'invited', board: initialBoard(game), round: 0, pending: {}, winner: null, createdAt: Date.now(), touchedAt: Date.now() };
    matches.set(m.id, m);
    push(target.id, { k: 'inv', match: m.id, game, from: who(u.id), at: Date.now() });
    return { duel: view(m, u.id) };
  }, { rate: ['duel-invite', 20] });

  r.post('/api/duels/:id/accept', (ctx: AppCtx) => {
    const { u, m, opp } = mine(ctx, ctx.params.id!);
    if (m.b !== u.id || m.status !== 'invited') throw conflict('Not an open invite');
    m.status = 'active';
    push(opp, { k: 'acc', match: m.id, game: m.game, from: who(u.id), at: Date.now() });
    return { duel: view(m, u.id) };
  }, { rate: ['duel', 120] });

  r.post('/api/duels/:id/decline', (ctx: AppCtx) => {
    const { u, m, opp } = mine(ctx, ctx.params.id!);
    matches.delete(m.id);
    push(opp, { k: 'dec', match: m.id, game: m.game, from: who(u.id), at: Date.now() });
    return { ok: true };
  }, { rate: ['duel', 120] });

  r.post('/api/duels/:id/leave', (ctx: AppCtx) => {
    const { u, m, opp } = mine(ctx, ctx.params.id!);
    if (m.status !== 'over') { m.status = 'over'; m.winner = opp; }
    push(opp, { k: 'bye', match: m.id, game: m.game, from: who(u.id), winner: m.winner, at: Date.now() });
    return { ok: true };
  }, { rate: ['duel', 120] });

  r.post('/api/duels/:id/move', (ctx: AppCtx) => {
    const { u, m, opp } = mine(ctx, ctx.params.id!);
    if (m.status !== 'active') throw conflict('Duel is not running');
    const b = obj(ctx.body);
    const value = Number(b.value);
    const me = m.a === u.id ? 1 : 2;
    if (m.game === 'rps') {
      if (!Number.isInteger(value) || value < 0 || value > 2) throw badRequest('invalid_field', 'Illegal move');
      if (m.pending[u.id] !== undefined) throw conflict('Already chosen');
      m.pending[u.id] = value;
      if (m.pending[opp] === undefined) return { duel: view(m, u.id), waiting: true };
      const va = m.pending[m.a]!, vb = m.pending[m.b]!;
      const res = rpsResult(va, vb);
      if (res > 0) m.board[0]!++;
      if (res < 0) m.board[1]!++;
      const round = m.round++;
      m.pending = {};
      if (m.board[0]! >= 3 || m.board[1]! >= 3) { m.status = 'over'; m.winner = m.board[0]! >= 3 ? m.a : m.b; }
      // Both see both choices only after both chose.
      for (const [to, from, v] of [[m.a, m.b, vb], [m.b, m.a, va]] as const) push(to, { k: 'mv', match: m.id, game: m.game, from: who(from), round, value: v, winner: m.winner, at: Date.now() });
      return { duel: view(m, u.id) };
    }
    const turn = m.round % 2 === 0 ? 1 : 2;
    // Reversi: a player without a legal move passes automatically.
    if (turn !== me) throw conflict('Not your turn');
    const res = applyMove(m.game, m.board, m.round, value, me);
    if (typeof res === 'string') throw badRequest('invalid_field', 'Illegal move');
    m.board = res.board;
    const round = m.round++;
    if (res.winner) { m.status = 'over'; m.winner = res.winner === 3 ? null : res.winner === 1 ? m.a : m.b; }
    if (m.game === 'reversi' && m.status === 'active') {
      const next = m.round % 2 === 0 ? 1 : 2;
      const canMove = (p: 1 | 2) => m.board.some((c, i) => c === 0 && typeof applyMove('reversi', m.board, 0, i, p) !== 'string');
      if (!canMove(next)) {
        if (canMove(next === 1 ? 2 : 1)) m.round++;
        else { const p1 = m.board.filter((c) => c === 1).length, p2 = m.board.filter((c) => c === 2).length; m.status = 'over'; m.winner = p1 === p2 ? null : p1 > p2 ? m.a : m.b; }
      }
    }
    push(opp, { k: 'mv', match: m.id, game: m.game, from: who(u.id), round, value, winner: m.winner, at: Date.now() });
    return { duel: view(m, u.id) };
  }, { rate: ['duel-move', 240] });

  r.get('/api/duels/:id', (ctx: AppCtx) => { const { u, m } = mine(ctx, ctx.params.id!); return { duel: view(m, u.id) }; });

  // Synced emotes: other LEGO players see them through the cosmetics lookup.
  r.post('/api/emotes/play', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const id = str(obj(ctx.body), 'emote', 1, 40);
    if (!/^[a-z0-9_]+$/.test(id)) throw badRequest('invalid_field', 'Invalid emote');
    app.events.setEmote(u.id, id);
    return { ok: true };
  }, { rate: ['emote', 30] });
}
