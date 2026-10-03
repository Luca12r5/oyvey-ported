// Daily rewards, quests, battle pass and paid products. All values are data so
// the backend enforces them and launcher/website only display them.

export const DAILY_REWARDS = [50, 60, 70, 80, 100, 120, 200] as const;

/** Credits for claiming on streak day `streak` (1-based), cycling weekly. */
export function dailyRewardFor(streak: number): number {
  const i = (Math.max(1, streak) - 1) % DAILY_REWARDS.length;
  return DAILY_REWARDS[i]!;
}

/**
 * Metrics the backend can attribute to a user. `playtime_minutes` is measured
 * server-side from presence heartbeats; the others are reported by the client
 * and therefore capped per period (see QUESTS[].target and MAX_REPORT_PER_DAY).
 */
export const METRICS = ['playtime_minutes', 'launches', 'minigames_played', 'minigame_wins', 'friends_added', 'emotes_used'] as const;
export type Metric = (typeof METRICS)[number];

export const MAX_REPORT_PER_DAY: Record<Metric, number> = {
  playtime_minutes: 24 * 60,
  launches: 20,
  minigames_played: 60,
  minigame_wins: 40,
  friends_added: 20,
  emotes_used: 200,
};

export interface QuestDef {
  id: string;
  period: 'daily' | 'weekly';
  metric: Metric;
  target: number;
  credits: number;
  xp: number;
  title: { en: string; de: string };
}

export const QUESTS: readonly QuestDef[] = [
  { id: 'd_play30', period: 'daily', metric: 'playtime_minutes', target: 30, credits: 40, xp: 150, title: { en: 'Play for 30 minutes', de: '30 Minuten spielen' } },
  { id: 'd_launch', period: 'daily', metric: 'launches', target: 1, credits: 15, xp: 50, title: { en: 'Launch the game', de: 'Spiel starten' } },
  { id: 'd_mini3', period: 'daily', metric: 'minigames_played', target: 3, credits: 30, xp: 120, title: { en: 'Play 3 mini games', de: '3 Minispiele spielen' } },
  { id: 'd_emote5', period: 'daily', metric: 'emotes_used', target: 5, credits: 20, xp: 80, title: { en: 'Use 5 emotes', de: '5 Emotes benutzen' } },
  { id: 'w_play300', period: 'weekly', metric: 'playtime_minutes', target: 300, credits: 250, xp: 1000, title: { en: 'Play for 5 hours', de: '5 Stunden spielen' } },
  { id: 'w_win10', period: 'weekly', metric: 'minigame_wins', target: 10, credits: 200, xp: 800, title: { en: 'Win 10 mini games', de: '10 Minispiele gewinnen' } },
  { id: 'w_friend', period: 'weekly', metric: 'friends_added', target: 1, credits: 100, xp: 300, title: { en: 'Add a friend', de: 'Einen Freund hinzufügen' } },
];

export interface PassReward {
  credits?: number;
  item?: string;
}

export interface PassTier {
  tier: number;
  free: PassReward | null;
  premium: PassReward | null;
}

export interface Season {
  id: string;
  name: string;
  startsAt: string; // ISO date
  endsAt: string;
  xpPerTier: number;
  tiers: PassTier[];
}

function seasonOneTiers(): PassTier[] {
  // Premium items reference real catalog ids (epic/legendary client cosmetics
  // and tags) so every reward can actually be equipped.
  const premiumItems = [
    'tag_aurora', 'tag_galaxy', 'tag_fire', 'tag_framed_gold', 'tag_framed_bolt', 'tag_rainbow', 'tag_wave_ocean',
    'tag_shimmer_gold', 'tag_scroll_neon', 'tag_m_cosmos',
  ];
  const tiers: PassTier[] = [];
  for (let t = 1; t <= 50; t++) {
    const free: PassReward | null = t % 2 === 0 ? { credits: 25 + Math.floor(t / 10) * 15 } : null;
    let premium: PassReward | null;
    if (t % 5 === 0) premium = { item: premiumItems[t / 5 - 1]! };
    else premium = { credits: 40 + Math.floor(t / 10) * 20 };
    tiers.push({ tier: t, free, premium });
  }
  return tiers;
}

export const SEASONS: readonly Season[] = [
  { id: 's1', name: 'Season 1 — Bricks & Beats', startsAt: '2026-10-01', endsAt: '2027-01-31', xpPerTier: 1000, tiers: seasonOneTiers() },
];

export function currentSeason(now = new Date()): Season | undefined {
  const d = now.toISOString().slice(0, 10);
  return SEASONS.find((s) => s.startsAt <= d && d <= s.endsAt);
}

// ---- Paid products ---------------------------------------------------------

export type Tier = 'lego_plus' | 'lego_plus_plus';

export interface Product {
  id: string;
  kind: 'credits' | 'subscription' | 'pass';
  name: string;
  /** Price in euro cents, shown incl. VAT. */
  priceCents: number;
  credits?: number;
  tier?: Tier;
  /** Subscription period in days. */
  periodDays?: number;
  perks: string[];
}

export const PRODUCTS: readonly Product[] = [
  { id: 'credits_500', kind: 'credits', name: '500 LEGO Credits', priceCents: 499, credits: 500, perks: [] },
  { id: 'credits_1100', kind: 'credits', name: '1,100 LEGO Credits', priceCents: 999, credits: 1100, perks: ['+10% bonus'] },
  { id: 'credits_2400', kind: 'credits', name: '2,400 LEGO Credits', priceCents: 1999, credits: 2400, perks: ['+20% bonus'] },
  { id: 'lego_pass_s1', kind: 'pass', name: 'LEGO Pass (Season 1)', priceCents: 799, perks: ['Premium battle pass track for Season 1'] },
  {
    id: 'lego_plus', kind: 'subscription', name: 'LEGO+', priceCents: 399, tier: 'lego_plus', periodDays: 30,
    perks: ['Premium battle pass track while active', '400 credits each period', 'Custom name tag text', '+10% battle pass XP'],
  },
  {
    id: 'lego_plus_plus', kind: 'subscription', name: 'LEGO++', priceCents: 799, tier: 'lego_plus_plus', periodDays: 30,
    perks: ['Everything in LEGO+', '1,000 credits each period', 'Animated profile banners', '+25% battle pass XP'],
  },
];

export function getProduct(id: string): Product | undefined {
  return PRODUCTS.find((p) => p.id === id);
}

export function xpMultiplier(tier: Tier | null): number {
  return tier === 'lego_plus_plus' ? 1.25 : tier === 'lego_plus' ? 1.1 : 1;
}
