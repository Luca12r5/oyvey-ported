// Inventory, equipping, the credit shop, gifts and custom name tag text.

import { CATALOG, checkCustomTagText, EQUIP_SLOTS, getItem, type CatalogItem } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import type { Db } from '../db.ts';
import { badRequest, conflict, forbidden, notFound } from '../http.ts';
import { obj, oneOf, str } from '../security.ts';
import { applyCredits } from '../services/credits.ts';
import { activeTier, grantItem } from '../services/progress.ts';
import { areFriends, getUser, rolesOf } from '../services/users.ts';

export function owns(db: Db, userId: string, item: CatalogItem, roles?: string[]): boolean {
  if (item.unlock === 'default') return true;
  if (db.get('SELECT 1 FROM inventory WHERE user_id = :u AND item_id = :i', { u: userId, i: item.id })) return true;
  if (item.id === 'tag_staff' || item.id === 'tag_developer') {
    const r = roles ?? rolesOf(db, userId);
    if (item.id === 'tag_staff') return r.length > 0;
    return r.includes('developer') || r.includes('admin');
  }
  return false;
}

export function equippedOf(db: Db, userId: string): Record<string, string> {
  const rows = db.all<{ slot: string; item_id: string }>('SELECT slot, item_id FROM equipped WHERE user_id = :u', { u: userId });
  return Object.fromEntries(rows.map((r) => [r.slot, r.item_id]));
}

export function registerCosmetics(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/inventory', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const owned = CATALOG.filter((i) => owns(db, u.id, i, u.roles)).map((i) => i.id);
    const total = CATALOG.length;
    return { owned, equipped: equippedOf(db, u.id), collection: { owned: owned.length, total } };
  });

  r.put('/api/equip', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const slot = oneOf(b, 'slot', EQUIP_SLOTS);
    if (b.itemId === null) {
      db.run('DELETE FROM equipped WHERE user_id = :u AND slot = :s', { u: u.id, s: slot });
      return { equipped: equippedOf(db, u.id) };
    }
    const item = getItem(str(b, 'itemId', 2, 64));
    if (!item) throw notFound('Unknown item');
    if (item.slot !== slot) throw badRequest('wrong_slot', `Item belongs to slot ${item.slot}`);
    if (!owns(db, u.id, item, u.roles)) throw forbidden('You do not own this item');
    db.run('INSERT INTO equipped(user_id, slot, item_id) VALUES (:u, :s, :i) ON CONFLICT(user_id, slot) DO UPDATE SET item_id = :i', { u: u.id, s: slot, i: item.id });
    return { equipped: equippedOf(db, u.id) };
  }, { rate: ['equip', 60] });

  r.post('/api/shop/buy', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const item = getItem(str(b, 'itemId', 2, 64));
    const idem = str(b, 'idempotencyKey', 8, 80);
    if (!item) throw notFound('Unknown item');
    if (item.price === null || item.unlock !== 'shop') throw forbidden('This item is not sold in the shop');
    return db.tx(() => {
      if (owns(db, u.id, item, u.roles)) throw conflict('already_owned', 'You already own this item');
      const c = applyCredits(db, { userId: u.id, delta: -item.price!, kind: 'purchase', reason: `Bought ${item.id}`, ref: item.id, idemKey: `buy:${u.id}:${idem}` });
      grantItem(db, u.id, item.id, 'shop');
      return { item: item.id, price: item.price, balance: c.balance };
    });
  }, { rate: ['shop', 30] });

  r.post('/api/shop/gift', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const item = getItem(str(b, 'itemId', 2, 64));
    const to = getUser(db, str(b, 'to', 3, 40));
    const idem = str(b, 'idempotencyKey', 8, 80);
    if (!item) throw notFound('Unknown item');
    if (!to) throw notFound('Player not found');
    if (to.id === u.id) throw badRequest('self', 'Use buy for yourself');
    if (item.price === null || item.unlock !== 'shop') throw forbidden('This item cannot be gifted');
    if (!areFriends(db, u.id, to.id)) throw forbidden('You can only gift friends');
    return db.tx(() => {
      if (owns(db, to.id, item)) throw conflict('already_owned', 'Your friend already owns this item');
      const c = applyCredits(db, { userId: u.id, delta: -item.price!, kind: 'purchase', reason: `Gift ${item.id} to ${to.mc_name}`, ref: `${item.id}:${to.id}`, idemKey: `gift:${u.id}:${idem}` });
      grantItem(db, to.id, item.id, 'gift');
      app.events.send(to.id, 'gift', { item: item.id, from: { id: u.id, name: u.mcName } });
      return { item: item.id, to: to.id, balance: c.balance };
    });
  }, { rate: ['shop', 10] });

  r.put('/api/me/tag-text', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    if (b.text === null) {
      db.run('UPDATE users SET custom_tag_text = NULL WHERE id = :u', { u: u.id });
      return { text: null };
    }
    if (activeTier(db, u.id) === null) throw forbidden('Custom tag text is a LEGO+ perk');
    const res = checkCustomTagText(str(b, 'text', 1, 40), u.mcName, (name) => !!db.get('SELECT 1 FROM users WHERE mc_name_lower = :n', { n: name.toLowerCase() }));
    if (!res.ok) throw badRequest('tag_rejected', res.reason);
    db.run('UPDATE users SET custom_tag_text = :t WHERE id = :u', { t: res.text, u: u.id });
    return { text: res.text };
  }, { rate: ['profile', 10] });
}
