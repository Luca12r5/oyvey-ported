// Rarity tiers shared by launcher, website, backend and (as JSON) the client.
// Colours are defaults; users can override them in launcher settings.

export const RARITIES = ['common', 'uncommon', 'rare', 'epic', 'legendary', 'mythic', 'special'] as const;
export type Rarity = (typeof RARITIES)[number];

export interface RarityInfo {
  id: Rarity;
  label: { en: string; de: string };
  color: string;
  /** Rank used for sorting and filters, 0 = most common. */
  rank: number;
  /** Animated frame in UIs (only the top tiers, to keep it meaningful). */
  animatedFrame: boolean;
  /** Default shop price in LEGO Credits; `null` = not sold (event/staff only). */
  basePrice: number | null;
}

export const RARITY_INFO: Record<Rarity, RarityInfo> = {
  common: { id: 'common', label: { en: 'Common', de: 'Gewöhnlich' }, color: '#9aa0a6', rank: 0, animatedFrame: false, basePrice: 150 },
  uncommon: { id: 'uncommon', label: { en: 'Uncommon', de: 'Ungewöhnlich' }, color: '#3fbf5f', rank: 1, animatedFrame: false, basePrice: 300 },
  rare: { id: 'rare', label: { en: 'Rare', de: 'Selten' }, color: '#3d8bff', rank: 2, animatedFrame: false, basePrice: 600 },
  epic: { id: 'epic', label: { en: 'Epic', de: 'Episch' }, color: '#a855f7', rank: 3, animatedFrame: false, basePrice: 1000 },
  legendary: { id: 'legendary', label: { en: 'Legendary', de: 'Legendär' }, color: '#f5b100', rank: 4, animatedFrame: true, basePrice: 1800 },
  mythic: { id: 'mythic', label: { en: 'Mythic', de: 'Mythisch' }, color: '#ef4444', rank: 5, animatedFrame: true, basePrice: 3000 },
  special: { id: 'special', label: { en: 'Special', de: 'Spezial' }, color: '#22d3ee', rank: 6, animatedFrame: true, basePrice: null },
};

export function isRarity(v: unknown): v is Rarity {
  return typeof v === 'string' && (RARITIES as readonly string[]).includes(v);
}

export function compareRarity(a: Rarity, b: Rarity): number {
  return RARITY_INFO[a].rank - RARITY_INFO[b].rank;
}
