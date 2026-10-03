// Metrics, quests, daily rewards and battle pass logic.

import {
  currentSeason, dailyRewardFor, getItem, QUESTS, xpMultiplier,
  type Metric, type QuestDef, type Season, type Tier,
} from '@lego/shared';
import type { Db } from '../db.ts';
import { conflict, HttpError, notFound } from '../http.ts';
import { applyCredits } from './credits.ts';

export function dayKey(d = new Date()): string {
  return `d:${d.toISOString().slice(0, 10)}`;
}

/** ISO-8601 week key, e.g. w:2026-W40. */
export function weekKey(d = new Date()): string {
  const t = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()));
  const dayNum = t.getUTCDay() || 7;
  t.setUTCDate(t.getUTCDate() + 4 - dayNum);
  const yearStart = new Date(Date.UTC(t.getUTCFullYear(), 0, 1));
  const week = Math.ceil(((t.getTime() - yearStart.getTime()) / 86_400_000 + 1) / 7);
  return `w:${t.getUTCFullYear()}-W${String(week).padStart(2, '0')}`;
}

function bump(db: Db, userId: string, metric: string, period: string, amount: number): void {
  db.run(
    `INSERT INTO metrics(user_id, metric, period, value) VALUES (:u, :m, :p, :a)
     ON CONFLICT(user_id, metric, period) DO UPDATE SET value = value + :a`,
    { u: userId, m: metric, p: period, a: amount },
  );
}

export function metricValue(db: Db, userId: string, metric: Metric, period: string): number {
  return db.get<{ value: number }>('SELECT value FROM metrics WHERE user_id = :u AND metric = :m AND period = :p', { u: userId, m: metric, p: period })?.value ?? 0;
}

/** Adds to day/week/total counters, never exceeding `dailyCap` per day. Returns the applied amount. */
export function addMetric(db: Db, userId: string, metric: Metric, amount: number, dailyCap: number, now = new Date()): number {
  return db.tx(() => {
    const d = dayKey(now);
    const today = metricValue(db, userId, metric, d);
    const applied = Math.max(0, Math.min(amount, dailyCap - today));
    if (applied === 0) return 0;
    bump(db, userId, metric, d, applied);
    bump(db, userId, metric, weekKey(now), applied);
    bump(db, userId, metric, 'total', applied);
    return applied;
  });
}

export function questPeriod(q: QuestDef, now = new Date()): string {
  return q.period === 'daily' ? dayKey(now) : weekKey(now);
}

export function questStatus(db: Db, userId: string, now = new Date()) {
  return QUESTS.map((q) => {
    const period = questPeriod(q, now);
    const progress = metricValue(db, userId, q.metric, period);
    const claimed = !!db.get('SELECT 1 FROM quest_claims WHERE user_id = :u AND quest_id = :q AND period = :p', { u: userId, q: q.id, p: period });
    return { ...q, progress: Math.min(progress, q.target), complete: progress >= q.target, claimed };
  });
}

export function activeTier(db: Db, userId: string, now = Date.now()): Tier | null {
  const s = db.get<{ tier: Tier }>('SELECT tier FROM subscriptions WHERE user_id = :u AND expires_at > :t', { u: userId, t: now });
  return s?.tier ?? null;
}

export function addPassXp(db: Db, userId: string, baseXp: number, now = new Date()): number {
  const season = currentSeason(now);
  if (!season) return 0;
  const xp = Math.round(baseXp * xpMultiplier(activeTier(db, userId, now.getTime())));
  db.run(
    `INSERT INTO pass_progress(user_id, season_id, xp) VALUES (:u, :s, :x)
     ON CONFLICT(user_id, season_id) DO UPDATE SET xp = xp + :x`,
    { u: userId, s: season.id, x: xp },
  );
  return xp;
}

export function claimQuest(db: Db, userId: string, questId: string, now = new Date()) {
  const q = QUESTS.find((x) => x.id === questId);
  if (!q) throw notFound('Unknown quest');
  const period = questPeriod(q, now);
  return db.tx(() => {
    if (metricValue(db, userId, q.metric, period) < q.target) throw conflict('quest_incomplete', 'Quest not completed yet');
    try {
      db.run('INSERT INTO quest_claims(user_id, quest_id, period, claimed_at) VALUES (:u, :q, :p, :t)', { u: userId, q: q.id, p: period, t: now.getTime() });
    } catch {
      throw conflict('already_claimed', 'Reward already claimed');
    }
    const c = applyCredits(db, { userId, delta: q.credits, kind: 'quest', reason: `Quest ${q.id}`, ref: period, idemKey: `quest:${userId}:${q.id}:${period}` });
    const xp = addPassXp(db, userId, q.xp, now);
    return { credits: q.credits, xp, balance: c.balance };
  });
}

export function claimDaily(db: Db, userId: string, now = new Date()) {
  const today = now.toISOString().slice(0, 10);
  const yesterday = new Date(now.getTime() - 86_400_000).toISOString().slice(0, 10);
  return db.tx(() => {
    const row = db.get<{ last_day: string; streak: number }>('SELECT last_day, streak FROM daily_claims WHERE user_id = :u', { u: userId });
    if (row?.last_day === today) throw conflict('already_claimed', 'Daily reward already claimed today');
    const streak = row && row.last_day === yesterday ? row.streak + 1 : 1;
    db.run(
      `INSERT INTO daily_claims(user_id, last_day, streak) VALUES (:u, :d, :s)
       ON CONFLICT(user_id) DO UPDATE SET last_day = :d, streak = :s`,
      { u: userId, d: today, s: streak },
    );
    const amount = dailyRewardFor(streak);
    const c = applyCredits(db, { userId, delta: amount, kind: 'daily', reason: `Daily reward (streak ${streak})`, idemKey: `daily:${userId}:${today}` });
    return { credits: amount, streak, balance: c.balance };
  });
}

export function dailyStatus(db: Db, userId: string, now = new Date()) {
  const today = now.toISOString().slice(0, 10);
  const yesterday = new Date(now.getTime() - 86_400_000).toISOString().slice(0, 10);
  const row = db.get<{ last_day: string; streak: number }>('SELECT last_day, streak FROM daily_claims WHERE user_id = :u', { u: userId });
  const claimedToday = row?.last_day === today;
  const nextStreak = claimedToday ? row!.streak + 1 : row && row.last_day === yesterday ? row.streak + 1 : 1;
  return { claimedToday, streak: claimedToday ? row!.streak : row && row.last_day === yesterday ? row.streak : 0, nextReward: dailyRewardFor(nextStreak) };
}

export function passStatus(db: Db, userId: string, now = new Date()) {
  const season = currentSeason(now);
  if (!season) return null;
  const p = db.get<{ xp: number; premium: number }>('SELECT xp, premium FROM pass_progress WHERE user_id = :u AND season_id = :s', { u: userId, s: season.id });
  const xp = p?.xp ?? 0;
  const premium = (p?.premium ?? 0) === 1 || activeTier(db, userId, now.getTime()) !== null;
  const claims = db.all<{ tier: number; track: string }>('SELECT tier, track FROM pass_claims WHERE user_id = :u AND season_id = :s', { u: userId, s: season.id });
  return {
    season: { id: season.id, name: season.name, endsAt: season.endsAt, xpPerTier: season.xpPerTier, tiers: season.tiers },
    xp, level: Math.min(season.tiers.length, Math.floor(xp / season.xpPerTier)), premium,
    claimed: claims.map((c) => `${c.tier}:${c.track}`),
  };
}

export function grantItem(db: Db, userId: string, itemId: string, source: string): boolean {
  if (!getItem(itemId)) throw new HttpError(400, 'unknown_item');
  const r = db.run('INSERT OR IGNORE INTO inventory(user_id, item_id, source, acquired_at) VALUES (:u, :i, :s, :t)', { u: userId, i: itemId, s: source, t: Date.now() });
  return r.changes > 0;
}

export function claimPassTier(db: Db, userId: string, tier: number, track: 'free' | 'premium', now = new Date()) {
  const status = passStatus(db, userId, now);
  if (!status) throw notFound('No active season');
  const season: Season = currentSeason(now)!;
  const t = season.tiers.find((x) => x.tier === tier);
  if (!t) throw notFound('Unknown tier');
  if (status.level < tier) throw conflict('tier_locked', 'Tier not reached yet');
  if (track === 'premium' && !status.premium) throw conflict('premium_required', 'Premium track requires LEGO Pass or LEGO+');
  const reward = track === 'free' ? t.free : t.premium;
  if (!reward) throw notFound('No reward on this track');
  return db.tx(() => {
    try {
      db.run('INSERT INTO pass_claims(user_id, season_id, tier, track, claimed_at) VALUES (:u, :s, :t, :k, :n)', { u: userId, s: season.id, t: tier, k: track, n: now.getTime() });
    } catch {
      throw conflict('already_claimed', 'Already claimed');
    }
    let balanceAfter: number | null = null;
    if (reward.credits) {
      balanceAfter = applyCredits(db, { userId, delta: reward.credits, kind: 'battlepass', reason: `${season.id} tier ${tier} (${track})`, idemKey: `pass:${userId}:${season.id}:${tier}:${track}` }).balance;
    }
    if (reward.item) grantItem(db, userId, reward.item, 'battlepass');
    return { reward, balance: balanceAfter };
  });
}
