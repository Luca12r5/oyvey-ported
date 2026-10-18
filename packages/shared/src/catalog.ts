// The purchasable/unlockable catalog: client 3D cosmetics + name tag styles.

import { CLIENT_COSMETICS, type ClientCosmeticSeed, type CosmeticSlot } from './client-cosmetics.ts';
import { CAPE_PRESETS } from './cape.ts';
import { NAME_TAGS, type UnlockMethod } from './nametags.ts';
import { RARITY_INFO, type Rarity } from './rarity.ts';

export type ItemKind = 'cosmetic' | 'nametag';

export interface CatalogItem {
  id: string;
  kind: ItemKind;
  /** Equip slot. Name tags use the dedicated `nametag` slot. */
  slot: CosmeticSlot | 'nametag';
  name: string;
  rarity: Rarity;
  unlock: UnlockMethod;
  /** Price in LEGO Credits when sold in the shop, otherwise null. */
  price: number | null;
  seasonal: boolean;
}

/**
 * Rarities that stay free for everyone. In client 3.4.0 every cosmetic was free
 * and local-only; with the account system, common and rare items keep that
 * status and higher tiers are earned or bought. Change this list to adjust.
 */
export const FREE_RARITIES: readonly Rarity[] = ['common', 'rare'];

/** Modern capes drawn from specs (cape_m_*) and the slot for the player's own designed cape. */
export const CUSTOM_CAPE_ID = 'cape_custom';
const SPEC_CAPES: ClientCosmeticSeed[] = [
  ...Object.entries(CAPE_PRESETS).map(([id, p], i): ClientCosmeticSeed => [`cape_${id}`, p.name, 'cape', i % 3 === 0 ? 'epic' : 'rare']),
  [CUSTOM_CAPE_ID, 'Eigenes Cape', 'cape', 'common'],
];

function cosmeticItems(): CatalogItem[] {
  return [...CLIENT_COSMETICS, ...SPEC_CAPES].map(([id, name, slot, rarity]) => {
    const free = FREE_RARITIES.includes(rarity);
    return {
      id,
      kind: 'cosmetic' as const,
      slot,
      name,
      rarity,
      unlock: free ? ('default' as const) : ('shop' as const),
      price: free ? null : RARITY_INFO[rarity].basePrice,
      seasonal: id.startsWith('xm_') || id.includes('_xm_'),
    };
  });
}

function tagItems(): CatalogItem[] {
  return NAME_TAGS.map((t) => ({
    id: t.id,
    kind: 'nametag' as const,
    slot: 'nametag' as const,
    name: t.name,
    rarity: t.rarity,
    unlock: t.unlock,
    price: t.unlock === 'shop' ? RARITY_INFO[t.rarity].basePrice : null,
    seasonal: false,
  }));
}

export const CATALOG: readonly CatalogItem[] = Object.freeze([...cosmeticItems(), ...tagItems()]);
const BY_ID = new Map(CATALOG.map((i) => [i.id, i]));

export function getItem(id: string): CatalogItem | undefined {
  return BY_ID.get(id);
}

export const EQUIP_SLOTS = ['cape', 'wings', 'hat', 'face', 'back', 'aura', 'pet', 'vehicle', 'knife', 'nametag'] as const;
export type EquipSlot = (typeof EQUIP_SLOTS)[number];
