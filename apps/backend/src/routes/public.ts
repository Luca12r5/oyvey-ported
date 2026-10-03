// Public, unauthenticated (or optionally authenticated) endpoints used by the
// website, the launcher before login and the in-game client.

import {
  CATALOG, DAILY_REWARDS, NAME_TAGS, PRODUCTS, QUESTS, RARITY_INFO, SEASONS, THEMES, getItem,
} from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { badRequest, notFound } from '../http.ts';
import { obj } from '../security.ts';
import { canSee, dashUuid, findUser, publicUser, type UserRow } from '../services/users.ts';
import { equippedOf } from './cosmetics.ts';

export const API_VERSION = '1.0.0';

export function registerPublic(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/public/status', () => ({
    ok: true, apiVersion: API_VERSION, maintenance: app.config.maintenance, time: Date.now(),
    paymentsEnabled: !!(app.config.stripeSecretKey && app.config.stripeWebhookSecret),
  }), { auth: 'none' });

  r.get('/api/public/catalog', () => ({ rarities: RARITY_INFO, items: CATALOG, nameTags: NAME_TAGS }), { auth: 'none' });
  r.get('/api/public/themes', () => ({ themes: THEMES }), { auth: 'none' });
  r.get('/api/public/progression', () => ({ quests: QUESTS, seasons: SEASONS, products: PRODUCTS, dailyRewards: DAILY_REWARDS }), { auth: 'none' });

  r.get('/api/public/news', (ctx: AppCtx) => {
    const kind = ctx.query.get('kind');
    return {
      news: db.all(
        `SELECT n.id, n.title, n.body, n.kind, n.created_at, users.mc_name AS author FROM news n LEFT JOIN users ON users.id = n.author_id
         WHERE n.published = 1 AND (:k IS NULL OR n.kind = :k) ORDER BY n.id DESC LIMIT 50`, { k: kind }),
    };
  }, { auth: 'none' });

  r.get('/api/public/profiles/:name', (ctx: AppCtx) => {
    const u = findUser(db, ctx.params.name!);
    const viewer = ctx.user?.id ?? null;
    if (!u || !canSee(db, u.privacy_profile, u.id, viewer)) throw notFound('Profile not found or private');
    const activity = canSee(db, u.privacy_activity, u.id, viewer);
    const totals = activity
      ? Object.fromEntries(db.all<{ metric: string; value: number }>("SELECT metric, value FROM metrics WHERE user_id = :u AND period = 'total'", { u: u.id }).map((m) => [m.metric, m.value]))
      : null;
    const friendsVisible = canSee(db, u.privacy_activity, u.id, viewer);
    return {
      profile: {
        ...publicUser(u), bio: u.bio, createdAt: u.created_at, customTag: u.custom_tag_text,
        equipped: equippedOf(db, u.id),
        stats: totals,
        achievements: totals ? achievements(totals) : null,
        friendCount: friendsVisible ? db.get<{ n: number }>('SELECT COUNT(*) AS n FROM friends WHERE user_id = :u', { u: u.id })!.n : null,
        status: canSee(db, u.privacy_status, u.id, viewer) ? app.events.getPresence(u.id).status : 'hidden',
      },
    };
  }, { auth: 'optional', rate: ['profiles', 120] });

  r.get('/api/public/leaderboard/:game', (ctx: AppCtx) => {
    const game = ctx.params.game!;
    if (!/^[a-z0-9_]{2,32}$/.test(game)) throw badRequest('invalid_game');
    return {
      game,
      entries: db.all(
        `SELECT users.mc_name AS name, users.mc_uuid AS uuid, MAX(g.score) AS score, SUM(g.won) AS wins FROM game_results g
         JOIN users ON users.id = g.user_id WHERE g.game_id = :g AND users.privacy_profile = 'everyone'
         GROUP BY g.user_id ORDER BY score DESC LIMIT 50`, { g: game }),
    };
  }, { auth: 'none', rate: ['leaderboard', 120] });

  // Used by the in-game client to render other LEGO players' cosmetics and
  // name tags. Returns only equipped, owned items (ownership was checked when
  // equipping). Unknown uuids are simply absent from the result.
  r.post('/api/public/cosmetics/lookup', (ctx: AppCtx) => {
    const b = obj(ctx.body);
    const list = b.uuids;
    if (!Array.isArray(list) || list.length > 100) throw badRequest('invalid_field', 'uuids must be an array of at most 100');
    const out: Record<string, { name: string; equipped: Record<string, string>; customTag: string | null }> = {};
    for (const raw of list) {
      if (typeof raw !== 'string') continue;
      let uuid: string;
      try {
        uuid = dashUuid(raw);
      } catch {
        continue;
      }
      const u = db.get<UserRow>('SELECT * FROM users WHERE mc_uuid = :u', { u: uuid });
      if (!u || (u.banned_until && u.banned_until > Date.now())) continue;
      const equipped = equippedOf(db, u.id);
      for (const [slot, id] of Object.entries(equipped)) if (!getItem(id)) delete equipped[slot];
      out[uuid] = { name: u.mc_name, equipped, customTag: u.custom_tag_text };
    }
    return { players: out };
  }, { auth: 'none', rate: ['lookup', 120] });
}

/** Achievements derived from server-side totals (no client-claimed flags). */
export function achievements(t: Record<string, number>) {
  const defs: [string, string, string, number][] = [
    ['first_steps', 'First Steps', 'launches', 1],
    ['regular', 'Regular', 'launches', 50],
    ['hour_one', 'First Hour', 'playtime_minutes', 60],
    ['dedicated', 'Dedicated', 'playtime_minutes', 6000],
    ['gamer', 'Mini Gamer', 'minigames_played', 25],
    ['champion', 'Champion', 'minigame_wins', 100],
    ['social', 'Social Butterfly', 'friends_added', 10],
    ['expressive', 'Expressive', 'emotes_used', 100],
  ];
  return defs.map(([id, name, metric, target]) => ({ id, name, metric, target, progress: Math.min(target, t[metric] ?? 0), unlocked: (t[metric] ?? 0) >= target }));
}
