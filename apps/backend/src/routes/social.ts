// Friends, requests, blocks, reports, private messages and parties.
// All checks are server side; the client only displays what it receives.

import { MAX_REPORT_PER_DAY, REPORT_REASONS } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { badRequest, conflict, forbidden, notFound } from '../http.ts';
import { cleanText, newId, obj, oneOf, str } from '../security.ts';
import { addMetric } from '../services/progress.ts';
import { areFriends, canSee, findUser, getUser, isBlocked, publicUser, type UserRow } from '../services/users.ts';

const MAX_FRIENDS = 300;
const MAX_PARTY = 8;

export function registerSocial(app: App): void {
  const r = app.router;
  const db = app.db;

  function target(ref: string): UserRow {
    const u = findUser(db, ref);
    if (!u) throw notFound('Player not found (they need to have logged in to LEGO once)');
    return u;
  }

  function friendView(viewerId: string, f: UserRow & { note?: string; since?: number }) {
    const presence = app.events.getPresence(f.id);
    const showStatus = canSee(db, f.privacy_status, f.id, viewerId);
    const showActivity = canSee(db, f.privacy_activity, f.id, viewerId);
    return {
      ...publicUser(f),
      note: f.note ?? '',
      since: f.since ?? null,
      status: showStatus ? presence.status : 'hidden',
      gameVersion: showStatus ? presence.gameVersion : null,
      activity: showActivity ? presence.activity : null,
    };
  }

  // ---- friends ----------------------------------------------------------
  r.get('/api/friends', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const rows = db.all<UserRow & { note: string; since: number }>(
      `SELECT users.*, friends.note AS note, friends.created_at AS since FROM friends JOIN users ON users.id = friends.friend_id
       WHERE friends.user_id = :u ORDER BY users.mc_name_lower`, { u: u.id });
    const incoming = db.all<UserRow & { at: number }>(
      'SELECT users.*, fr.created_at AS at FROM friend_requests fr JOIN users ON users.id = fr.from_id WHERE fr.to_id = :u ORDER BY fr.created_at DESC', { u: u.id });
    const outgoing = db.all<UserRow & { at: number }>(
      'SELECT users.*, fr.created_at AS at FROM friend_requests fr JOIN users ON users.id = fr.to_id WHERE fr.from_id = :u ORDER BY fr.created_at DESC', { u: u.id });
    return {
      friends: rows.map((f) => friendView(u.id, f)),
      incoming: incoming.map((x) => ({ ...publicUser(x), at: x.at })),
      outgoing: outgoing.map((x) => ({ ...publicUser(x), at: x.at })),
    };
  });

  r.post('/api/friends/requests', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const t = target(str(obj(ctx.body), 'player', 3, 40));
    if (t.id === u.id) throw badRequest('self', 'You cannot add yourself');
    if (areFriends(db, u.id, t.id)) throw conflict('already_friends', 'Already friends');
    if (isBlocked(db, t.id, u.id) || isBlocked(db, u.id, t.id)) throw forbidden('Cannot send a request to this player');
    if (!t.allow_friend_requests) throw forbidden('This player does not accept friend requests');
    const count = db.get<{ n: number }>('SELECT COUNT(*) AS n FROM friends WHERE user_id = :u', { u: u.id })!.n;
    if (count >= MAX_FRIENDS) throw conflict('friend_limit', `Friend limit of ${MAX_FRIENDS} reached`);
    // If they already asked us, accept instead of creating a second request.
    if (db.get('SELECT 1 FROM friend_requests WHERE from_id = :t AND to_id = :u', { t: t.id, u: u.id })) {
      return acceptRequest(u.id, t.id);
    }
    db.run('INSERT OR IGNORE INTO friend_requests(from_id, to_id, created_at) VALUES (:a, :b, :t)', { a: u.id, b: t.id, t: Date.now() });
    app.events.send(t.id, 'friend_request', { from: { id: u.id, name: u.mcName } });
    return { status: 'requested', player: publicUser(t) };
  }, { rate: ['friendreq', 20] });

  function acceptRequest(me: string, other: string) {
    return db.tx(() => {
      const del = db.run('DELETE FROM friend_requests WHERE from_id = :o AND to_id = :m', { o: other, m: me });
      if (del.changes === 0) throw notFound('No pending request from this player');
      db.run('DELETE FROM friend_requests WHERE from_id = :m AND to_id = :o', { o: other, m: me });
      const now = Date.now();
      db.run('INSERT OR IGNORE INTO friends(user_id, friend_id, created_at) VALUES (:a, :b, :t)', { a: me, b: other, t: now });
      db.run('INSERT OR IGNORE INTO friends(user_id, friend_id, created_at) VALUES (:a, :b, :t)', { a: other, b: me, t: now });
      addMetric(db, me, 'friends_added', 1, MAX_REPORT_PER_DAY.friends_added);
      addMetric(db, other, 'friends_added', 1, MAX_REPORT_PER_DAY.friends_added);
      app.events.send(other, 'friend_accepted', { userId: me });
      return { status: 'friends', player: publicUser(getUser(db, other)!) };
    });
  }

  r.post('/api/friends/requests/:id/accept', (ctx: AppCtx) => acceptRequest(requireUser(ctx).id, ctx.params.id!), { rate: ['friendreq', 30] });

  r.post('/api/friends/requests/:id/decline', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const n = db.run('DELETE FROM friend_requests WHERE from_id = :o AND to_id = :m', { o: ctx.params.id!, m: u.id }).changes;
    if (!n) throw notFound('No pending request');
    return { ok: true };
  });

  r.delete('/api/friends/requests/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    db.run('DELETE FROM friend_requests WHERE from_id = :m AND to_id = :o', { o: ctx.params.id!, m: u.id });
    return { ok: true };
  });

  r.delete('/api/friends/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    db.tx(() => {
      db.run('DELETE FROM friends WHERE user_id = :a AND friend_id = :b', { a: u.id, b: ctx.params.id! });
      db.run('DELETE FROM friends WHERE user_id = :b AND friend_id = :a', { a: u.id, b: ctx.params.id! });
    });
    return { ok: true };
  });

  r.put('/api/friends/:id/note', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const note = cleanText(str(obj(ctx.body), 'note', 0, 200));
    const n = db.run('UPDATE friends SET note = :n WHERE user_id = :u AND friend_id = :f', { n: note, u: u.id, f: ctx.params.id! }).changes;
    if (!n) throw notFound('Not a friend');
    return { ok: true };
  });

  // ---- blocks & reports -------------------------------------------------
  r.get('/api/blocks', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    return { blocked: db.all<UserRow>('SELECT users.* FROM blocks JOIN users ON users.id = blocks.blocked_id WHERE blocks.user_id = :u', { u: u.id }).map(publicUser) };
  });

  r.post('/api/blocks', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const t = target(str(obj(ctx.body), 'player', 3, 40));
    if (t.id === u.id) throw badRequest('self');
    db.tx(() => {
      db.run('INSERT OR IGNORE INTO blocks(user_id, blocked_id, created_at) VALUES (:a, :b, :t)', { a: u.id, b: t.id, t: Date.now() });
      db.run('DELETE FROM friends WHERE (user_id = :a AND friend_id = :b) OR (user_id = :b AND friend_id = :a)', { a: u.id, b: t.id });
      db.run('DELETE FROM friend_requests WHERE (from_id = :a AND to_id = :b) OR (from_id = :b AND to_id = :a)', { a: u.id, b: t.id });
    });
    return { ok: true };
  });

  r.delete('/api/blocks/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    db.run('DELETE FROM blocks WHERE user_id = :a AND blocked_id = :b', { a: u.id, b: ctx.params.id! });
    return { ok: true };
  });

  r.post('/api/reports', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const t = target(str(b, 'player', 3, 40));
    if (t.id === u.id) throw badRequest('self');
    const reason = oneOf(b, 'reason', REPORT_REASONS);
    const details = cleanText(str(b, 'details', 0, 1000));
    const recent = db.get('SELECT 1 FROM reports WHERE reporter_id = :a AND target_id = :b AND created_at > :t', { a: u.id, b: t.id, t: Date.now() - 3_600_000 });
    if (recent) throw conflict('duplicate_report', 'You already reported this player recently');
    db.run('INSERT INTO reports(reporter_id, target_id, reason, details, created_at) VALUES (:a, :b, :r, :d, :t)', { a: u.id, b: t.id, r: reason, d: details, t: Date.now() });
    return { ok: true };
  }, { rate: ['reports', 5] });

  // ---- private messages -------------------------------------------------
  r.get('/api/messages', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    // Conversation list: last message per partner.
    const rows = db.all<{ partner: string; last_id: number }>(
      `SELECT CASE WHEN from_id = :u THEN to_id ELSE from_id END AS partner, MAX(id) AS last_id
       FROM messages WHERE from_id = :u OR to_id = :u GROUP BY partner ORDER BY last_id DESC LIMIT 100`, { u: u.id });
    return {
      conversations: rows.map((c) => {
        const p = getUser(db, c.partner)!;
        const last = db.get<{ body: string; from_id: string; created_at: number }>('SELECT body, from_id, created_at FROM messages WHERE id = :i', { i: c.last_id })!;
        const unread = db.get<{ n: number }>('SELECT COUNT(*) AS n FROM messages WHERE from_id = :p AND to_id = :u AND read_at IS NULL', { p: c.partner, u: u.id })!.n;
        return { partner: publicUser(p), last: { body: last.body, mine: last.from_id === u.id, at: last.created_at }, unread };
      }),
    };
  });

  r.get('/api/messages/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const other = ctx.params.id!;
    const before = Number(ctx.query.get('before') ?? 0) || null;
    const msgs = db.all(
      `SELECT id, from_id, to_id, body, created_at, read_at FROM messages
       WHERE ((from_id = :u AND to_id = :o) OR (from_id = :o AND to_id = :u)) AND (:b IS NULL OR id < :b)
       ORDER BY id DESC LIMIT 50`, { u: u.id, o: other, b: before });
    db.run('UPDATE messages SET read_at = :t WHERE from_id = :o AND to_id = :u AND read_at IS NULL', { t: Date.now(), o: other, u: u.id });
    return { messages: msgs.reverse() };
  });

  r.post('/api/messages/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const to = getUser(db, ctx.params.id!);
    if (!to) throw notFound('Player not found');
    if (to.id === u.id) throw badRequest('self');
    if (isBlocked(db, to.id, u.id) || isBlocked(db, u.id, to.id)) throw forbidden('You cannot message this player');
    if (!canSee(db, to.privacy_messages, to.id, u.id)) throw forbidden('This player only accepts messages from friends');
    const body = cleanText(str(obj(ctx.body), 'body', 1, 1000));
    const now = Date.now();
    const id = db.run('INSERT INTO messages(from_id, to_id, body, created_at) VALUES (:f, :t, :b, :n)', { f: u.id, t: to.id, b: body, n: now }).lastInsertRowid;
    const msg = { id, from_id: u.id, to_id: to.id, body, created_at: now, read_at: null, fromName: u.mcName };
    app.events.send(to.id, 'message', msg);
    return msg;
  }, { rate: ['messages', 30] });

  // ---- parties ----------------------------------------------------------
  function myParty(userId: string): { id: string; leader_id: string } | undefined {
    return db.get('SELECT parties.id AS id, parties.leader_id AS leader_id FROM party_members JOIN parties ON parties.id = party_members.party_id WHERE party_members.user_id = :u', { u: userId });
  }

  function partyView(partyId: string) {
    const p = db.get<{ id: string; leader_id: string; created_at: number }>('SELECT * FROM parties WHERE id = :p', { p: partyId });
    if (!p) return null;
    const members = db.all<UserRow>('SELECT users.* FROM party_members JOIN users ON users.id = party_members.user_id WHERE party_id = :p ORDER BY joined_at', { p: partyId });
    const invites = db.all<UserRow>('SELECT users.* FROM party_invites JOIN users ON users.id = party_invites.user_id WHERE party_id = :p', { p: partyId });
    return { id: p.id, leaderId: p.leader_id, members: members.map((m) => ({ ...publicUser(m), status: app.events.getPresence(m.id).status })), invites: invites.map(publicUser) };
  }

  function notifyParty(partyId: string, type: string, data: unknown) {
    const ids = db.all<{ user_id: string }>('SELECT user_id FROM party_members WHERE party_id = :p', { p: partyId }).map((m) => m.user_id);
    app.events.sendMany(ids, type, data);
  }

  r.get('/api/party', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    const invites = db.all<{ party_id: string; invited_by: string }>('SELECT party_id, invited_by FROM party_invites WHERE user_id = :u', { u: u.id });
    return {
      party: p ? partyView(p.id) : null,
      invites: invites.map((i) => ({ partyId: i.party_id, from: publicUser(getUser(db, i.invited_by)!) })),
    };
  });

  r.post('/api/party', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    if (myParty(u.id)) throw conflict('already_in_party', 'Leave your current party first');
    const id = newId('p_');
    const now = Date.now();
    db.tx(() => {
      db.run('INSERT INTO parties(id, leader_id, created_at) VALUES (:id, :u, :t)', { id, u: u.id, t: now });
      db.run('INSERT INTO party_members(party_id, user_id, joined_at) VALUES (:id, :u, :t)', { id, u: u.id, t: now });
    });
    return partyView(id);
  }, { rate: ['party', 20] });

  r.post('/api/party/invite', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    if (!p) throw notFound('You are not in a party');
    if (p.leader_id !== u.id) throw forbidden('Only the party leader can invite');
    const t = target(str(obj(ctx.body), 'player', 3, 40));
    if (!areFriends(db, u.id, t.id)) throw forbidden('You can only invite friends');
    const size = db.get<{ n: number }>('SELECT COUNT(*) AS n FROM party_members WHERE party_id = :p', { p: p.id })!.n;
    if (size >= MAX_PARTY) throw conflict('party_full', `Parties are limited to ${MAX_PARTY} players`);
    db.run('INSERT OR IGNORE INTO party_invites(party_id, user_id, invited_by, created_at) VALUES (:p, :u, :b, :t)', { p: p.id, u: t.id, b: u.id, t: Date.now() });
    app.events.send(t.id, 'party_invite', { partyId: p.id, from: { id: u.id, name: u.mcName } });
    return { ok: true };
  }, { rate: ['party', 20] });

  r.post('/api/party/:id/join', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const partyId = ctx.params.id!;
    if (myParty(u.id)) throw conflict('already_in_party', 'Leave your current party first');
    return db.tx(() => {
      const inv = db.run('DELETE FROM party_invites WHERE party_id = :p AND user_id = :u', { p: partyId, u: u.id });
      if (!inv.changes) throw notFound('No invite for this party');
      const size = db.get<{ n: number }>('SELECT COUNT(*) AS n FROM party_members WHERE party_id = :p', { p: partyId })!.n;
      if (size === 0) throw notFound('Party no longer exists');
      if (size >= MAX_PARTY) throw conflict('party_full');
      db.run('INSERT INTO party_members(party_id, user_id, joined_at) VALUES (:p, :u, :t)', { p: partyId, u: u.id, t: Date.now() });
      notifyParty(partyId, 'party_update', { partyId });
      return partyView(partyId);
    });
  });

  r.post('/api/party/:id/decline', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    db.run('DELETE FROM party_invites WHERE party_id = :p AND user_id = :u', { p: ctx.params.id!, u: u.id });
    return { ok: true };
  });

  function leave(userId: string) {
    const p = myParty(userId);
    if (!p) throw notFound('You are not in a party');
    db.tx(() => {
      db.run('DELETE FROM party_members WHERE party_id = :p AND user_id = :u', { p: p.id, u: userId });
      const next = db.get<{ user_id: string }>('SELECT user_id FROM party_members WHERE party_id = :p ORDER BY joined_at LIMIT 1', { p: p.id });
      if (!next) db.run('DELETE FROM parties WHERE id = :p', { p: p.id });
      else if (p.leader_id === userId) db.run('UPDATE parties SET leader_id = :u WHERE id = :p', { u: next.user_id, p: p.id });
    });
    notifyParty(p.id, 'party_update', { partyId: p.id });
  }

  r.post('/api/party/leave', (ctx: AppCtx) => {
    leave(requireUser(ctx).id);
    return { ok: true };
  });

  r.post('/api/party/kick', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    if (!p || p.leader_id !== u.id) throw forbidden('Only the party leader can kick');
    const victim = str(obj(ctx.body), 'userId', 3, 40);
    if (victim === u.id) throw badRequest('self');
    const n = db.run('DELETE FROM party_members WHERE party_id = :p AND user_id = :v', { p: p.id, v: victim }).changes;
    if (!n) throw notFound('Not in your party');
    app.events.send(victim, 'party_update', { partyId: null });
    notifyParty(p.id, 'party_update', { partyId: p.id });
    return { ok: true };
  });

  r.post('/api/party/leader', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    if (!p || p.leader_id !== u.id) throw forbidden('Only the party leader can transfer leadership');
    const next = str(obj(ctx.body), 'userId', 3, 40);
    if (!db.get('SELECT 1 FROM party_members WHERE party_id = :p AND user_id = :u', { p: p.id, u: next })) throw notFound('Not in your party');
    db.run('UPDATE parties SET leader_id = :u WHERE id = :p', { u: next, p: p.id });
    notifyParty(p.id, 'party_update', { partyId: p.id });
    return { ok: true };
  });

  r.get('/api/party/messages', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    if (!p) throw notFound('You are not in a party');
    return { messages: db.all('SELECT id, from_id, body, created_at FROM party_messages WHERE party_id = :p ORDER BY id DESC LIMIT 100', { p: p.id }).reverse() };
  });

  r.post('/api/party/messages', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const p = myParty(u.id);
    if (!p) throw notFound('You are not in a party');
    const body = cleanText(str(obj(ctx.body), 'body', 1, 500));
    const now = Date.now();
    const id = db.run('INSERT INTO party_messages(party_id, from_id, body, created_at) VALUES (:p, :f, :b, :t)', { p: p.id, f: u.id, b: body, t: now }).lastInsertRowid;
    const msg = { id, partyId: p.id, from_id: u.id, fromName: u.mcName, body, created_at: now };
    notifyParty(p.id, 'party_message', msg);
    return msg;
  }, { rate: ['messages', 30] });

  // Duo/trio emote invitations are relayed to friends in real time; the
  // client starts the synchronised animation once all participants accepted.
  r.post('/api/emotes/invite', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const emote = str(b, 'emote', 2, 48, { pattern: /^[a-z0-9_]+$/ });
    const ids = b.players;
    if (!Array.isArray(ids) || ids.length < 1 || ids.length > 2) throw badRequest('invalid_field', 'players must list 1-2 friends');
    const session = newId('e_');
    for (const id of ids) {
      if (typeof id !== 'string' || !areFriends(db, u.id, id)) throw forbidden('Emote partners must be friends');
    }
    app.events.sendMany(ids as string[], 'emote_invite', { session, emote, from: { id: u.id, name: u.mcName }, players: [u.id, ...ids], expiresAt: Date.now() + 15_000 });
    return { session, expiresAt: Date.now() + 15_000 };
  }, { rate: ['emotes', 20] });

  r.post('/api/emotes/respond', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const session = str(b, 'session', 3, 40);
    const to = str(b, 'to', 3, 40);
    const accept = b.accept === true;
    if (!areFriends(db, u.id, to)) throw forbidden();
    app.events.send(to, 'emote_response', { session, from: u.id, accept });
    return { ok: true };
  }, { rate: ['emotes', 40] });

}
