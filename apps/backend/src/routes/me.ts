// The signed-in user's own account: profile, privacy, credits history,
// presence heartbeat, metric reports, game results and settings sync.

import { MAX_REPORT_PER_DAY, METRICS, type Metric } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { badRequest } from '../http.ts';
import { cleanText, int, obj, oneOf, optStr, str } from '../security.ts';
import { balance, history } from '../services/credits.ts';
import { addMetric, awardGameCoins, awardPlaytimeCoins, dayKey } from '../services/progress.ts';
import { getUser, rolesOf } from '../services/users.ts';

const PRIVACY = ['everyone', 'friends', 'nobody'] as const;

export function registerMe(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/me', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const row = getUser(db, u.id)!;
    const sub = db.get<{ tier: string; expires_at: number }>('SELECT tier, expires_at FROM subscriptions WHERE user_id = :u AND expires_at > :t', { u: u.id, t: Date.now() });
    return {
      id: row.id, uuid: row.mc_uuid, name: row.mc_name, credits: row.credits, bio: row.bio, roles: rolesOf(db, u.id),
      createdAt: row.created_at, customTag: row.custom_tag_text,
      privacy: {
        profile: row.privacy_profile, status: row.privacy_status, activity: row.privacy_activity,
        messages: row.privacy_messages, friendRequests: row.allow_friend_requests === 1,
      },
      subscription: sub ? { tier: sub.tier, expiresAt: sub.expires_at } : null,
    };
  });

  r.patch('/api/me', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    if (b.bio !== undefined) {
      const bio = cleanText(optStr(b, 'bio', 0, 280) ?? '');
      db.run('UPDATE users SET bio = :b WHERE id = :u', { b: bio, u: u.id });
    }
    return { ok: true };
  }, { rate: ['profile', 20] });

  r.put('/api/me/privacy', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const fr = b.friendRequests;
    if (typeof fr !== 'boolean') throw badRequest('invalid_field', 'friendRequests must be boolean');
    db.run(
      `UPDATE users SET privacy_profile = :p, privacy_status = :s, privacy_activity = :a, privacy_messages = :m, allow_friend_requests = :f WHERE id = :u`,
      {
        p: oneOf(b, 'profile', PRIVACY), s: oneOf(b, 'status', PRIVACY), a: oneOf(b, 'activity', PRIVACY),
        m: oneOf(b, 'messages', PRIVACY), f: fr ? 1 : 0, u: u.id,
      },
    );
    return { ok: true };
  }, { rate: ['profile', 20] });

  r.get('/api/me/credits', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const before = ctx.query.get('before');
    return { balance: balance(db, u.id), history: history(db, u.id, 50, before ? Number(before) : undefined) };
  });

  // Presence heartbeat from launcher/client, at most every 20 s. Play time is
  // measured here (server side) from consecutive in-game heartbeats.
  r.post('/api/me/presence', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const status = oneOf(b, 'status', ['online', 'in_game', 'away'] as const);
    const gameVersion = optStr(b, 'gameVersion', 1, 32, { pattern: /^[0-9A-Za-z.\-_ ]+$/ });
    const activity = optStr(b, 'activity', 1, 64);
    const prev = app.events.getPresence(u.id);
    const now = Date.now();
    if (status === 'in_game' && prev.status === 'in_game') {
      const minutes = Math.floor(Math.min(now - prev.updatedAt, 120_000) / 60_000 * 100) / 100;
      creditPlaytime(app, u.id, minutes);
    }
    app.events.setPresence(u.id, { status, gameVersion, activity: activity ? cleanText(activity) : null });
    const friends = db.all<{ friend_id: string }>('SELECT friend_id FROM friends WHERE user_id = :u', { u: u.id }).map((f) => f.friend_id);
    app.events.sendMany(friends, 'presence', { userId: u.id, status });
    return { ok: true };
  }, { rate: ['presence', 6] });

  // Client-reported metrics (launches, mini games, emotes). Capped per day
  // because the client can not be trusted for these numbers.
  r.post('/api/me/metrics', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const metric = oneOf(b, 'metric', METRICS.filter((m) => m !== 'playtime_minutes' && m !== 'friends_added') as Metric[]);
    const amount = int(b, 'amount', 1, 50);
    const applied = addMetric(db, u.id, metric, amount, MAX_REPORT_PER_DAY[metric]);
    return { applied };
  }, { rate: ['metrics', 60] });

  r.post('/api/me/game-results', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const gameId = str(b, 'gameId', 2, 32, { pattern: /^[a-z0-9_]+$/ });
    const score = int(b, 'score', 0, 10_000_000);
    const won = b.won === true;
    db.run('INSERT INTO game_results(user_id, game_id, score, won, created_at) VALUES (:u, :g, :s, :w, :t)', { u: u.id, g: gameId, s: score, w: won ? 1 : 0, t: Date.now() });
    addMetric(db, u.id, 'minigames_played', 1, MAX_REPORT_PER_DAY.minigames_played);
    if (won) addMetric(db, u.id, 'minigame_wins', 1, MAX_REPORT_PER_DAY.minigame_wins);
    const coins = awardGameCoins(db, u.id, won);
    if (coins) app.events.send(u.id, 'credits', { delta: coins, reason: 'game' });
    return { ok: true, coins };
  }, { rate: ['results', 30] });

  r.get('/api/me/stats', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const totals = db.all<{ metric: string; value: number }>("SELECT metric, value FROM metrics WHERE user_id = :u AND period = 'total'", { u: u.id });
    const games = db.all('SELECT game_id, COUNT(*) AS played, SUM(won) AS wins, MAX(score) AS best FROM game_results WHERE user_id = :u GROUP BY game_id ORDER BY played DESC', { u: u.id });
    const today = db.all<{ metric: string; value: number }>('SELECT metric, value FROM metrics WHERE user_id = :u AND period = :p', { u: u.id, p: dayKey() });
    return { totals: Object.fromEntries(totals.map((t) => [t.metric, t.value])), today: Object.fromEntries(today.map((t) => [t.metric, t.value])), games };
  });

  // Cloud sync of launcher/client settings (themes, HUD layout, keybinds).
  r.get('/api/me/settings', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const row = db.get<{ data: string; updated_at: number }>('SELECT data, updated_at FROM settings_sync WHERE user_id = :u', { u: u.id });
    return row ? { data: JSON.parse(row.data), updatedAt: row.updated_at } : { data: null, updatedAt: 0 };
  });

  r.put('/api/me/settings', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    if (typeof b.data !== 'object' || b.data === null) throw badRequest('invalid_field', 'data must be an object');
    const json = JSON.stringify(b.data);
    const now = Date.now();
    db.run('INSERT INTO settings_sync(user_id, data, updated_at) VALUES (:u, :d, :t) ON CONFLICT(user_id) DO UPDATE SET data = :d, updated_at = :t', { u: u.id, d: json, t: now });
    return { updatedAt: now };
  }, { bodyLimit: 512 * 1024, rate: ['settings', 20] });
}

function creditPlaytime(app: App, userId: string, minutes: number): void {
  // Store hundredths of minutes to avoid losing partial heartbeats.
  const hundredths = Math.round(minutes * 100);
  if (hundredths <= 0) return;
  const key = `playtime_frac`;
  const row = app.db.get<{ value: number }>("SELECT value FROM metrics WHERE user_id = :u AND metric = :m AND period = 'total'", { u: userId, m: key });
  const total = (row?.value ?? 0) + hundredths;
  const whole = Math.floor(total / 100);
  app.db.run(
    "INSERT INTO metrics(user_id, metric, period, value) VALUES (:u, :m, 'total', :v) ON CONFLICT(user_id, metric, period) DO UPDATE SET value = :v",
    { u: userId, m: key, v: total % 100 },
  );
  if (whole > 0) {
    addMetric(app.db, userId, 'playtime_minutes', whole, MAX_REPORT_PER_DAY.playtime_minutes);
    const coins = awardPlaytimeCoins(app.db, userId);
    if (coins) app.events.send(userId, 'credits', { delta: coins, reason: 'playtime' });
  }
}
