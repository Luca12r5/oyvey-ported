// Single-player arcade games for the launcher (LEGO-brick look, keyboard + mouse).

import { brick, label, type GameCtx, type GameDef, type GameRun } from './engine.ts';

function snake(ctx: GameCtx): GameRun {
  const c = ctx.colors, N = 20;
  let body = [{ x: 8, y: 10 }, { x: 7, y: 10 }, { x: 6, y: 10 }];
  let dir = { x: 1, y: 0 }, next = dir, food = { x: 14, y: 10 }, acc = 0, score = 0, dead = false;
  const place = () => { do { food = { x: Math.floor(Math.random() * N), y: Math.floor(Math.random() * N) }; } while (body.some((p) => p.x === food.x && p.y === food.y)); };
  return {
    key(e, down) {
      if (!down) return;
      const m: Record<string, { x: number; y: number }> = { ArrowUp: { x: 0, y: -1 }, KeyW: { x: 0, y: -1 }, ArrowDown: { x: 0, y: 1 }, KeyS: { x: 0, y: 1 }, ArrowLeft: { x: -1, y: 0 }, KeyA: { x: -1, y: 0 }, ArrowRight: { x: 1, y: 0 }, KeyD: { x: 1, y: 0 } };
      const d = m[e.code];
      if (d && (d.x !== -dir.x || d.y !== -dir.y)) { next = d; e.preventDefault(); }
    },
    frame(dt) {
      const g = ctx.g, cell = Math.floor(Math.min(ctx.w, ctx.h - 30) / N), ox = (ctx.w - cell * N) / 2, oy = 30 + (ctx.h - 30 - cell * N) / 2;
      if (!dead) {
        acc += dt;
        const step = Math.max(0.055, 0.13 - score * 0.002);
        while (acc > step) {
          acc -= step;
          dir = next;
          const h = { x: (body[0]!.x + dir.x + N) % N, y: (body[0]!.y + dir.y + N) % N };
          if (body.some((p) => p.x === h.x && p.y === h.y)) { dead = true; ctx.sound('lose'); ctx.over('Game Over', score); break; }
          body.unshift(h);
          if (h.x === food.x && h.y === food.y) { score++; ctx.score(score); ctx.sound('point'); place(); } else body.pop();
        }
      }
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      g.fillStyle = 'rgba(255,255,255,.03)';
      g.fillRect(ox, oy, cell * N, cell * N);
      brick(g, ox + food.x * cell + 2, oy + food.y * cell + 2, cell - 4, cell - 4, c.accent);
      body.forEach((p, i) => brick(g, ox + p.x * cell + 1, oy + p.y * cell + 1, cell - 2, cell - 2, i === 0 ? c.green : '#2e8b57', i === 0));
      label(g, `Punkte ${score}`, ctx.w / 2, 14, 10, c.text);
    },
  };
}

function g2048(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  let grid = Array(16).fill(0), score = 0, over = false;
  const add = () => { const free = grid.map((v, i) => (v ? -1 : i)).filter((i) => i >= 0); if (free.length) grid[free[Math.floor(Math.random() * free.length)]!] = Math.random() < 0.9 ? 2 : 4; };
  add(); add();
  const slide = (dx: number, dy: number) => {
    let moved = false;
    const get = (x: number, y: number) => grid[y * 4 + x]!, set = (x: number, y: number, v: number) => { grid[y * 4 + x] = v; };
    for (let k = 0; k < 4; k++) {
      const line: number[] = [];
      for (let i = 0; i < 4; i++) { const x = dx ? (dx > 0 ? 3 - i : i) : k, y = dy ? (dy > 0 ? 3 - i : i) : k; if (get(x, y)) line.push(get(x, y)); }
      const merged: number[] = [];
      for (let i = 0; i < line.length; i++) { if (line[i] === line[i + 1]) { merged.push(line[i]! * 2); score += line[i]! * 2; i++; } else merged.push(line[i]!); }
      for (let i = 0; i < 4; i++) { const x = dx ? (dx > 0 ? 3 - i : i) : k, y = dy ? (dy > 0 ? 3 - i : i) : k; const v = merged[i] ?? 0; if (get(x, y) !== v) moved = true; set(x, y, v); }
    }
    if (moved) { add(); ctx.score(score); ctx.sound('click'); }
    const canMove = grid.some((v, i) => !v || (i % 4 < 3 && v === grid[i + 1]) || (i < 12 && v === grid[i + 4]));
    if (!canMove && !over) { over = true; ctx.sound('lose'); ctx.over('Keine Züge mehr', score); }
  };
  const COLORS: Record<number, string> = { 2: '#e6dcc8', 4: '#e9d3a8', 8: '#f2a65a', 16: '#f08a3c', 32: '#ea6a2d', 64: '#dd4b1a', 128: '#e8c547', 256: '#e5bd2e', 512: '#e3b51a', 1024: '#c91a1a', 2048: '#7c3aed' };
  return {
    key(e, down) {
      if (!down || over) return;
      const m: Record<string, [number, number]> = { ArrowLeft: [-1, 0], KeyA: [-1, 0], ArrowRight: [1, 0], KeyD: [1, 0], ArrowUp: [0, -1], KeyW: [0, -1], ArrowDown: [0, 1], KeyS: [0, 1] };
      const d = m[e.code];
      if (d) { e.preventDefault(); slide(d[0], d[1]); }
    },
    frame() {
      const g = ctx.g, s = Math.min(ctx.w, ctx.h - 40) * 0.86, cell = s / 4, ox = (ctx.w - s) / 2, oy = 36 + (ctx.h - 36 - s) / 2;
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      g.fillStyle = c.panel;
      g.fillRect(ox - 6, oy - 6, s + 12, s + 12);
      grid.forEach((v, i) => {
        const x = ox + (i % 4) * cell, y = oy + Math.floor(i / 4) * cell;
        if (!v) { g.fillStyle = 'rgba(255,255,255,.04)'; g.fillRect(x + 4, y + 4, cell - 8, cell - 8); return; }
        brick(g, x + 4, y + 4, cell - 8, cell - 8, COLORS[v] ?? '#222');
        label(g, String(v), x + cell / 2, y + cell / 2 + 4, v >= 1000 ? cell * 0.16 : cell * 0.22, v <= 4 ? '#3a2a1a' : '#fff');
      });
      label(g, `Punkte ${score}`, ctx.w / 2, 16, 10, c.text);
    },
  };
}

function flappy(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  let y = ctx.h / 2, vy = 0, pipes: { x: number; gap: number; passed: boolean }[] = [], t = 0, score = 0, dead = false, started = false;
  const flap = () => { if (dead) return; started = true; vy = -330; ctx.sound('click'); };
  return {
    key(e, down) { if (down && (e.code === 'Space' || e.code === 'ArrowUp' || e.code === 'KeyW')) { e.preventDefault(); flap(); } },
    click() { flap(); },
    frame(dt) {
      const g = ctx.g, X = ctx.w * 0.3, R = 14, GAP = Math.max(120, 170 - score * 2);
      if (started && !dead) {
        vy += 900 * dt; y += vy * dt; t += dt;
        if (t > 1.5) { t = 0; pipes.push({ x: ctx.w + 40, gap: 80 + Math.random() * (ctx.h - 160 - GAP), passed: false }); }
        for (const p of pipes) {
          p.x -= 170 * dt;
          if (!p.passed && p.x + 30 < X) { p.passed = true; score++; ctx.score(score); ctx.sound('point'); }
          if (X + R > p.x && X - R < p.x + 60 && (y - R < p.gap || y + R > p.gap + GAP)) dead = true;
        }
        pipes = pipes.filter((p) => p.x > -80);
        if (y > ctx.h - R || y < R) dead = true;
        if (dead) { ctx.sound('lose'); ctx.over('Autsch!', score); }
      }
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      for (const p of pipes) {
        brick(g, p.x, 0, 60, p.gap, '#237841', false);
        brick(g, p.x, p.gap + GAP, 60, ctx.h - p.gap - GAP, '#237841', false);
      }
      // A little LEGO bat.
      g.save(); g.translate(X, y); g.rotate(Math.max(-0.5, Math.min(0.9, vy / 600)));
      brick(g, -R, -R * 0.7, R * 2, R * 1.4, c.accent);
      g.fillStyle = '#111'; g.fillRect(R * 0.3, -R * 0.35, 4, 4);
      g.restore();
      label(g, started ? String(score) : 'Leertaste / Klick zum Fliegen', ctx.w / 2, 30, started ? 18 : 10, c.text);
    },
  };
}

function breakout(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  const cols = 10, rows = 6;
  let bricks = Array(cols * rows).fill(1), px = ctx.w / 2, ball = { x: ctx.w / 2, y: ctx.h - 80, vx: 220, vy: -300 }, score = 0, lives = 3, stuck = true;
  const colors = [c.red, c.accent, c.yellow, c.green, c.blue, '#7c3aed'];
  return {
    move(x) { px = x; },
    click() { stuck = false; },
    key(e, down) { if (down && e.code === 'Space') { stuck = false; e.preventDefault(); } if (down && e.code === 'ArrowLeft') px -= 40; if (down && e.code === 'ArrowRight') px += 40; },
    frame(dt) {
      const g = ctx.g, bw = ctx.w / cols, bh = 22, top = 50, PW = 110;
      px = Math.max(PW / 2, Math.min(ctx.w - PW / 2, px));
      if (stuck) { ball.x = px; ball.y = ctx.h - 48; } else {
        ball.x += ball.vx * dt; ball.y += ball.vy * dt;
        if (ball.x < 8 || ball.x > ctx.w - 8) { ball.vx *= -1; ball.x = Math.max(8, Math.min(ctx.w - 8, ball.x)); }
        if (ball.y < 8) ball.vy = Math.abs(ball.vy);
        if (ball.y > ctx.h - 40 && ball.y < ctx.h - 26 && Math.abs(ball.x - px) < PW / 2 + 8 && ball.vy > 0) { ball.vy = -Math.abs(ball.vy) * 1.02; ball.vx = (ball.x - px) * 6; ctx.sound('click'); }
        const ci = Math.floor(ball.x / bw), ri = Math.floor((ball.y - top) / bh);
        if (ri >= 0 && ri < rows && ci >= 0 && ci < cols && bricks[ri * cols + ci]) { bricks[ri * cols + ci] = 0; ball.vy *= -1; score += 10; ctx.score(score); ctx.sound('point'); }
        if (ball.y > ctx.h) { lives--; stuck = true; ctx.sound('hit'); if (!lives) { ctx.sound('lose'); ctx.over('Game Over', score); lives = 0; } }
        if (bricks.every((b) => !b)) { ctx.sound('win'); ctx.over('Alle Steine weg!', score); bricks = Array(cols * rows).fill(1); }
      }
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      bricks.forEach((b, i) => { if (b) brick(g, (i % cols) * bw + 2, top + Math.floor(i / cols) * bh + 2, bw - 4, bh - 4, colors[Math.floor(i / cols)]!); });
      brick(g, px - PW / 2, ctx.h - 36, PW, 14, '#cbd5e1', false);
      g.beginPath(); g.arc(ball.x, ball.y, 8, 0, Math.PI * 2); g.fillStyle = '#fff'; g.fill();
      label(g, `Punkte ${score}   Leben ${'♥'.repeat(Math.max(0, lives))}`, ctx.w / 2, 20, 10, c.text);
    },
  };
}

function memory(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  const icons = ['#c91a1a', '#f2b632', '#1e5aa8', '#237841', '#7c3aed', '#ff7a1a', '#e5e7eb', '#0ea5e9'];
  let cards = [...icons, ...icons].sort(() => Math.random() - 0.5).map((col) => ({ col, open: false, done: false }));
  let first = -1, wait = 0, moves = 0, found = 0;
  const geo = () => { const s = Math.min(ctx.w, ctx.h - 40) * 0.88, cell = s / 4; return { ox: (ctx.w - s) / 2, oy: 36 + (ctx.h - 36 - s) / 2, cell }; };
  return {
    click(x, y) {
      if (wait > 0) return;
      const G = geo(), i = Math.floor((y - G.oy) / G.cell) * 4 + Math.floor((x - G.ox) / G.cell);
      const cd = cards[i];
      if (!cd || cd.open || cd.done || x < G.ox || y < G.oy) return;
      cd.open = true; ctx.sound('click');
      if (first < 0) first = i;
      else {
        moves++;
        if (cards[first]!.col === cd.col) { cards[first]!.done = cd.done = true; found++; first = -1; ctx.sound('point'); if (found === 8) { ctx.sound('win'); ctx.over(`Geschafft in ${moves} Zügen`, moves); } }
        else wait = 0.8;
      }
    },
    frame(dt) {
      if (wait > 0) { wait -= dt; if (wait <= 0) { cards.forEach((cd) => { if (!cd.done) cd.open = false; }); first = -1; } }
      const g = ctx.g, G = geo();
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      cards.forEach((cd, i) => {
        const x = G.ox + (i % 4) * G.cell, y = G.oy + Math.floor(i / 4) * G.cell;
        if (cd.open || cd.done) brick(g, x + 5, y + 5, G.cell - 10, G.cell - 10, cd.col);
        else { g.fillStyle = c.panel; g.fillRect(x + 5, y + 5, G.cell - 10, G.cell - 10); label(g, '?', x + G.cell / 2, y + G.cell / 2, G.cell * 0.25, c.muted); }
      });
      label(g, `Züge ${moves}`, ctx.w / 2, 16, 10, c.text);
    },
  };
}

function whack(ctx: GameCtx): GameRun {
  const c = ctx.colors;
  let holes = Array(9).fill(0), t = 0, time = 30, score = 0, spawn = 0, done = false;
  const geo = () => { const s = Math.min(ctx.w, ctx.h - 40) * 0.8, cell = s / 3; return { ox: (ctx.w - s) / 2, oy: 40 + (ctx.h - 40 - s) / 2, cell }; };
  return {
    click(x, y) {
      if (done) return;
      const G = geo(), i = Math.floor((y - G.oy) / G.cell) * 3 + Math.floor((x - G.ox) / G.cell);
      if (i >= 0 && i < 9 && holes[i]! > 0) { holes[i] = 0; score++; ctx.score(score); ctx.sound('point'); } else ctx.sound('hit');
    },
    frame(dt) {
      if (!done) {
        t += dt; time -= dt; spawn -= dt;
        holes = holes.map((h) => Math.max(0, h - dt));
        if (spawn <= 0) { spawn = Math.max(0.35, 0.9 - t * 0.015); holes[Math.floor(Math.random() * 9)] = 1.1; }
        if (time <= 0) { done = true; ctx.sound('win'); ctx.over(`${score} Zombies erwischt`, score); }
      }
      const g = ctx.g, G = geo();
      g.fillStyle = c.bg;
      g.fillRect(0, 0, ctx.w, ctx.h);
      holes.forEach((h, i) => {
        const x = G.ox + (i % 3) * G.cell, y = G.oy + Math.floor(i / 3) * G.cell;
        g.fillStyle = '#1a120c';
        g.beginPath(); g.ellipse(x + G.cell / 2, y + G.cell * 0.75, G.cell * 0.36, G.cell * 0.12, 0, 0, Math.PI * 2); g.fill();
        if (h > 0) {
          const up = Math.min(1, h * 4, (1.1 - h) * 6 + 0.2);
          const zy = y + G.cell * 0.75 - G.cell * 0.5 * up;
          brick(g, x + G.cell * 0.3, zy, G.cell * 0.4, G.cell * 0.4, '#3f7d2b', false);
          g.fillStyle = '#d10f0f'; g.fillRect(x + G.cell * 0.38, zy + G.cell * 0.12, G.cell * 0.06, G.cell * 0.06); g.fillRect(x + G.cell * 0.56, zy + G.cell * 0.12, G.cell * 0.06, G.cell * 0.06);
        }
      });
      label(g, `Punkte ${score}   Zeit ${Math.max(0, Math.ceil(time))}`, ctx.w / 2, 18, 10, c.text);
    },
  };
}

export const ARCADE_GAMES: GameDef[] = [
  { id: 'snake', name: 'Snake', tagline: 'Friss Steine, werd länger', icon: 'gamepad', higherIsBetter: true, start: snake },
  { id: '2048', name: '2048', tagline: 'Schieb gleiche Steine zusammen', icon: 'grid', higherIsBetter: true, start: g2048 },
  { id: 'flappy', name: 'Flappy Bat', tagline: 'Flieg durch die Lücken', icon: 'ghost', higherIsBetter: true, start: flappy },
  { id: 'breakout', name: 'Breakout', tagline: 'Zerschlag die Steinwand', icon: 'brick', higherIsBetter: true, start: breakout },
  { id: 'memory', name: 'Memory', tagline: 'Finde die Paare', icon: 'grid', higherIsBetter: false, start: memory },
  { id: 'whack', name: 'Zombie hauen', tagline: '30 Sekunden, so viele wie möglich', icon: 'pumpkin', higherIsBetter: true, start: whack },
];
