// Spiele: arcade games and duel games (against the AI or another LEGO user via
// the LEGO server). Results earn LEGO Coins like the in-game minigames.

import { onLeave, state } from '../app.ts';
import { dialog, errorToast, html, lego, mount, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import { beep, type DuelLink, type GameCtx, type GameDef, type GameRun } from '../games/engine.ts';
import { ARCADE_GAMES } from '../games/arcade.ts';
import { DUEL_GAMES } from '../games/duel.ts';

const ALL = [...DUEL_GAMES, ...ARCADE_GAMES];
type Invite = { match: string; game: string; from: { id: string; uuid: string; name: string }; at: number };
/** Open invites received while the launcher runs (filled by app.ts lego-event handler). */
export const invites = new Map<string, Invite>();
type DuelMsg = { k: string; match: string; game: string; from: { id: string; uuid: string; name: string }; round?: number; value?: number; winner?: string | null; at: number };
const duelListeners = new Set<(m: DuelMsg) => void>();
export function onDuelEvent(m: DuelMsg): void {
  if (m.k === 'inv') invites.set(m.match, { match: m.match, game: m.game, from: m.from, at: m.at });
  if (m.k === 'bye' || m.k === 'dec') invites.delete(m.match);
  duelListeners.forEach((fn) => fn(m));
}

const best = (id: string): number | null => { try { const v = localStorage.getItem(`lego.best.${id}`); return v === null ? null : Number(v); } catch { return null; } };
const saveBest = (g: GameDef, score: number) => {
  const b = best(g.id);
  const better = b === null || (g.higherIsBetter === false ? score < b : score > b);
  if (better) try { localStorage.setItem(`lego.best.${g.id}`, String(score)); } catch { /* storage blocked */ }
  return better;
};

export async function gamesPage(el: HTMLElement): Promise<void> {
  const signed = state.auth.lego.connected;
  const render = () => mount(el, html`<div class="games">
    ${invites.size ? html`<section class="panel invites"><div class="panel-h">${icon('gamepad', 'sm')}<span>Duell-Einladungen</span></div>
      ${[...invites.values()].map((i) => html`<div class="inv-row"><b>${i.from.name}</b><span class="muted small">fordert dich zu ${ALL.find((g) => g.id === i.game)?.name ?? i.game} heraus</span><button class="btn sm primary" data-accept="${i.match}">Annehmen</button><button class="btn sm" data-decline="${i.match}">Ablehnen</button></div>`)}</section>` : ''}
    <section class="panel"><div class="panel-h">${icon('gamepad', 'sm')}<span>Duelle</span><span class="muted small">gegen die KI oder gegen Freunde – auch gegen Spieler im LEGO Client</span></div>
      <div class="game-grid">${DUEL_GAMES.map((g) => card(g, true))}</div></section>
    <section class="panel"><div class="panel-h">${icon('games', 'sm')}<span>Arcade</span><span class="muted small">Rekorde und LEGO Coins</span></div>
      <div class="game-grid">${ARCADE_GAMES.map((g) => card(g, false))}</div></section>
  </div>`);
  const card = (g: GameDef, duel: boolean) => html`<div class="game-card"><div class="gc-top">${icon(g.icon, 'sm')}<b>${g.name}</b></div><p class="small muted">${g.tagline}</p>
    <div class="small">${best(g.id) !== null ? `Rekord: ${best(g.id)}` : 'Noch nicht gespielt'}</div>
    <div class="row gap"><button class="btn sm ${duel ? '' : 'primary'}" data-play="${g.id}">${duel ? 'Gegen KI' : 'Spielen'}</button>${duel ? html`<button class="btn sm primary" data-duel="${g.id}" ${signed ? '' : 'disabled title="Dafür brauchst du ein Konto"'}>Duell…</button>` : ''}</div></div>`;
  render();
  const offDuel = (m: DuelMsg) => { if ((m.k === 'inv' || m.k === 'dec' || m.k === 'bye') && el.isConnected && !el.querySelector('.game-player')) render(); };
  duelListeners.add(offDuel);
  onLeave(() => duelListeners.delete(offDuel));

  el.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    if (b.dataset.play) return void openGame(el, ALL.find((g) => g.id === b.dataset.play)!, null, render);
    if (b.dataset.duel) return void chooseOpponent(el, ALL.find((g) => g.id === b.dataset.duel)!, render);
    if (b.dataset.accept) {
      const inv = invites.get(b.dataset.accept);
      invites.delete(b.dataset.accept);
      if (!inv) return;
      try {
        await lego('POST', `/api/duels/${inv.match}/accept`);
        openGame(el, ALL.find((g) => g.id === inv.game)!, { match: inv.match, you: 2, oppName: inv.from.name }, render);
      } catch (err) { errorToast(err); render(); }
    }
    if (b.dataset.decline) {
      const id = b.dataset.decline;
      invites.delete(id);
      void lego('POST', `/api/duels/${id}/decline`).catch(() => {});
      render();
    }
  };
}

async function chooseOpponent(el: HTMLElement, g: GameDef, back: () => void): Promise<void> {
  let friends: { id: string; name: string; status: string }[] = [];
  try { friends = (await lego<{ friends: { id: string; name: string; status: string }[] }>('GET', '/api/friends')).friends; } catch (e) { errorToast(e); return; }
  const d = dialog(html`<h2>${g.name}: Gegner wählen</h2>
    <p class="small muted">Auch Spieler im LEGO Client können annehmen. Wer nur Freunde-Duelle erlaubt oder dich blockiert hat, kann nicht eingeladen werden.</p>
    <div class="opp-list">${friends.length ? friends.map((f) => html`<button class="menu-i" data-to="${f.id}"><span class="dot s-${f.status}"></span><span class="grow">${f.name}</span><small>${f.status === 'in_game' ? 'im Spiel' : f.status === 'online' ? 'online' : f.status === 'away' ? 'abwesend' : f.status === 'hidden' ? '' : 'offline'}</small></button>`) : html`<p class="small muted">Noch keine Freunde – oder gib unten einen Minecraft-Namen ein.</p>`}</div>
    <div class="row gap"><input id="oppName" class="compact" placeholder="Minecraft-Name" maxlength="16"><button class="btn primary" id="oppGo">Einladen</button></div>
    <div class="row between"><span></span><button class="btn" id="oppClose">Abbrechen</button></div>`);
  const invite = async (to: string) => {
    try {
      const r = await lego<{ duel: { id: string; opponent: { name: string } } }>('POST', '/api/duels', { to, game: g.id });
      d.close();
      waitForAccept(el, g, r.duel.id, r.duel.opponent.name, back);
    } catch (e) { errorToast(e); }
  };
  d.querySelector('#oppClose')!.addEventListener('click', () => d.close());
  d.querySelector('#oppGo')!.addEventListener('click', () => { const n = d.querySelector<HTMLInputElement>('#oppName')!.value.trim(); if (/^[A-Za-z0-9_]{3,16}$/.test(n)) void invite(n); else toast('Ungültiger Name', 'error'); });
  d.querySelectorAll<HTMLButtonElement>('[data-to]').forEach((b) => b.addEventListener('click', () => void invite(b.dataset.to!)));
}

function waitForAccept(el: HTMLElement, g: GameDef, match: string, oppName: string, back: () => void): void {
  toast(`Einladung an ${oppName} gesendet – warte auf Antwort…`);
  const timer = setTimeout(() => { duelListeners.delete(fn); toast(`${oppName} hat nicht geantwortet`); }, 90_000);
  const fn = (m: DuelMsg) => {
    if (m.match !== match) return;
    if (m.k === 'acc') { clearTimeout(timer); duelListeners.delete(fn); openGame(el, g, { match, you: 1, oppName }, back); }
    if (m.k === 'dec') { clearTimeout(timer); duelListeners.delete(fn); toast(`${oppName} hat abgelehnt`); }
  };
  duelListeners.add(fn);
}

function openGame(el: HTMLElement, g: GameDef, duel: { match: string; you: 1 | 2; oppName: string } | null, back: () => void): void {
  mount(el, html`<div class="game-player panel">
    <div class="panel-h">${icon(g.icon, 'sm')}<span>${g.name}</span>${duel ? html`<span class="muted small">Duell gegen ${duel.oppName}</span>` : ''}<span class="gp-score" id="gpScore"></span><button class="btn sm" id="gpBack">${icon('back', 'xs')} Zurück</button></div>
    <div class="gp-stage"><canvas id="gpCanvas"></canvas><div class="gp-over" id="gpOver" hidden></div></div>
  </div>`);
  const canvas = el.querySelector<HTMLCanvasElement>('#gpCanvas')!;
  const g2 = canvas.getContext('2d')!;
  const cs = getComputedStyle(document.documentElement);
  const v = (n: string, d: string) => cs.getPropertyValue(n).trim() || d;
  let run: GameRun | null = null;
  let raf = 0, last = performance.now();
  const listeners: ((v: number) => void)[] = [];
  let duelOff: (() => void) | null = null;
  const link: DuelLink | null = duel ? {
    you: duel.you, oppName: duel.oppName,
    send: (value) => { void lego('POST', `/api/duels/${duel.match}/move`, { value }).catch((e) => errorToast(e)); },
    onMove: (cb) => listeners.push(cb),
    leave: () => { void lego('POST', `/api/duels/${duel.match}/leave`).catch(() => {}); },
  } : null;
  if (duel) {
    const fn = (m: DuelMsg) => {
      if (m.match !== duel.match) return;
      if (m.k === 'mv' && typeof m.value === 'number') listeners.forEach((cb) => cb(m.value!));
      if (m.k === 'bye') showOver(`${duel.oppName} hat das Duell verlassen`);
    };
    duelListeners.add(fn);
    duelOff = () => duelListeners.delete(fn);
  }
  const overBox = el.querySelector<HTMLElement>('#gpOver')!;
  const showOver = (text: string, score?: number) => {
    overBox.hidden = false;
    const record = score !== undefined && !duel && saveBest(g, score);
    mount(overBox, html`<div class="gp-over-box"><h2>${text}</h2>${score !== undefined ? html`<div class="small muted">Punkte: ${score}</div>` : ''}${record ? html`<div class="small gp-record">Neuer Rekord!</div>` : ''}<div class="row gap"><button class="btn primary" id="gpAgain">${duel ? 'Zurück zur Übersicht' : 'Nochmal'}</button></div></div>`);
    overBox.querySelector('#gpAgain')!.addEventListener('click', () => { if (duel) close(); else restart(); });
    if (state.auth.lego.connected && score !== undefined) {
      void lego<{ coins: number }>('POST', '/api/me/game-results', { gameId: g.id, score: Math.max(0, Math.round(score)), won: /Gewonnen|Rekord|Geschafft|Alle/.test(text) })
        .then((r) => { if (r.coins > 0) toast(`+${r.coins} LEGO Coins`, 'ok'); }).catch(() => {});
    }
  };
  const ctx: GameCtx = {
    canvas, g: g2, w: 0, h: 0,
    score: (n) => { el.querySelector('#gpScore')!.textContent = String(n); },
    over: (t, s) => showOver(t, s),
    sound: beep,
    duel: link,
    colors: { bg: v('--bg', '#05040a'), panel: 'rgba(255,255,255,.07)', line: v('--line', '#241b33'), text: v('--text', '#e6dfef'), muted: v('--muted', '#9a90ab'), accent: v('--accent', '#ff7a1a'), accent2: v('--accent2', '#7c3aed'), red: '#c91a1a', yellow: '#f2b632', blue: '#1e5aa8', green: '#237841' },
  };
  const size = () => {
    const r = canvas.parentElement!.getBoundingClientRect();
    const dpr = Math.min(2, devicePixelRatio || 1);
    canvas.width = Math.round(r.width * dpr);
    canvas.height = Math.round(r.height * dpr);
    canvas.style.width = `${r.width}px`;
    canvas.style.height = `${r.height}px`;
    g2.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.w = r.width;
    ctx.h = r.height;
  };
  const restart = () => { run?.destroy?.(); overBox.hidden = true; size(); run = g.start(ctx); };
  const frame = (now: number) => {
    raf = requestAnimationFrame(frame);
    const dt = Math.min(0.05, (now - last) / 1000);
    last = now;
    run?.frame?.(dt);
  };
  const local = (e: MouseEvent) => { const r = canvas.getBoundingClientRect(); return [e.clientX - r.left, e.clientY - r.top] as const; };
  canvas.addEventListener('mousedown', (e) => { const [x, y] = local(e); run?.click?.(x, y); });
  canvas.addEventListener('mousemove', (e) => { const [x, y] = local(e); run?.move?.(x, y); });
  const kd = (e: KeyboardEvent) => { if (!(e.target as HTMLElement).closest('input,textarea')) run?.key?.(e, true); };
  const ku = (e: KeyboardEvent) => run?.key?.(e, false);
  addEventListener('keydown', kd);
  addEventListener('keyup', ku);
  addEventListener('resize', size);
  const close = () => { cleanup(); back(); };
  const cleanup = () => {
    cancelAnimationFrame(raf);
    run?.destroy?.();
    run = null;
    duelOff?.();
    removeEventListener('keydown', kd);
    removeEventListener('keyup', ku);
    removeEventListener('resize', size);
  };
  el.querySelector('#gpBack')!.addEventListener('click', close);
  onLeave(cleanup);
  requestAnimationFrame(() => { restart(); raf = requestAnimationFrame(frame); });
}
