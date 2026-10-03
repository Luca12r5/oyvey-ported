import type { Db } from '../db.ts';
import { newId } from '../security.ts';

export interface UserRow {
  id: string;
  mc_uuid: string;
  mc_name: string;
  credits: number;
  bio: string;
  banned_until: number | null;
  ban_reason: string | null;
  created_at: number;
  last_seen: number;
  privacy_profile: 'everyone' | 'friends' | 'nobody';
  privacy_status: 'everyone' | 'friends' | 'nobody';
  privacy_activity: 'everyone' | 'friends' | 'nobody';
  privacy_messages: 'everyone' | 'friends' | 'nobody';
  allow_friend_requests: number;
  custom_tag_text: string | null;
}

/** Formats a 32-char hex uuid from the session server as dashed lowercase. */
export function dashUuid(id: string): string {
  const h = id.replace(/-/g, '').toLowerCase();
  if (!/^[0-9a-f]{32}$/.test(h)) throw new Error('invalid uuid');
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
}

export function upsertMinecraftUser(db: Db, mcUuid: string, mcName: string): UserRow {
  return db.tx(() => {
    const now = Date.now();
    const existing = db.get<UserRow>('SELECT * FROM users WHERE mc_uuid = :u', { u: mcUuid });
    if (existing) {
      if (existing.mc_name !== mcName) {
        db.run('UPDATE users SET mc_name = :n, mc_name_lower = :l WHERE id = :id', { n: mcName, l: mcName.toLowerCase(), id: existing.id });
      }
      db.run('UPDATE users SET last_seen = :t WHERE id = :id', { t: now, id: existing.id });
      return { ...existing, mc_name: mcName, last_seen: now };
    }
    const id = newId('u_');
    db.run(
      `INSERT INTO users(id, mc_uuid, mc_name, mc_name_lower, created_at, last_seen) VALUES (:id, :u, :n, :l, :t, :t)`,
      { id, u: mcUuid, n: mcName, l: mcName.toLowerCase(), t: now },
    );
    return db.get<UserRow>('SELECT * FROM users WHERE id = :id', { id })!;
  });
}

export function getUser(db: Db, id: string): UserRow | undefined {
  return db.get<UserRow>('SELECT * FROM users WHERE id = :id', { id });
}

export function findUser(db: Db, ref: string): UserRow | undefined {
  const r = ref.trim();
  if (/^u_[A-Za-z0-9_-]+$/.test(r)) return getUser(db, r);
  if (/^[0-9a-fA-F-]{32,36}$/.test(r)) {
    try {
      return db.get<UserRow>('SELECT * FROM users WHERE mc_uuid = :u', { u: dashUuid(r) });
    } catch {
      return undefined;
    }
  }
  return db.get<UserRow>('SELECT * FROM users WHERE mc_name_lower = :n', { n: r.toLowerCase() });
}

export function rolesOf(db: Db, userId: string): string[] {
  return db.all<{ role: string }>('SELECT role FROM roles WHERE user_id = :u ORDER BY role', { u: userId }).map((r) => r.role);
}

export function areFriends(db: Db, a: string, b: string): boolean {
  return !!db.get('SELECT 1 FROM friends WHERE user_id = :a AND friend_id = :b', { a, b });
}

export function isBlocked(db: Db, by: string, target: string): boolean {
  return !!db.get('SELECT 1 FROM blocks WHERE user_id = :a AND blocked_id = :b', { a: by, b: target });
}

/** Whether `viewer` may see something governed by `level` of `owner`. */
export function canSee(db: Db, level: 'everyone' | 'friends' | 'nobody', ownerId: string, viewerId: string | null): boolean {
  if (viewerId === ownerId) return true;
  if (viewerId && isBlocked(db, ownerId, viewerId)) return false;
  if (level === 'everyone') return true;
  if (level === 'friends') return viewerId !== null && areFriends(db, ownerId, viewerId);
  return false;
}

export function publicUser(u: UserRow) {
  return { id: u.id, uuid: u.mc_uuid, name: u.mc_name };
}
