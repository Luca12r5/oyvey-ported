// Developer/admin dashboard API. Every route checks a permission server side
// and every mutation writes an audit log entry in the same transaction.

import { join } from 'node:path';
import { getItem, hasPermission, LARGE_CREDIT_CHANGE, MAX_CREDIT_CHANGE, ROLES } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requirePermission } from '../app.ts';
import { badRequest, conflict, forbidden, notFound } from '../http.ts';
import { bool, cleanText, int, obj, oneOf, optStr, str } from '../security.ts';
import { audit } from '../services/audit.ts';
import { applyCredits, history, suspiciousEarners } from '../services/credits.ts';
import { grantItem } from '../services/progress.ts';
import { getUser, publicUser, rolesOf, type UserRow } from '../services/users.ts';

export function registerAdmin(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/admin/overview', (ctx: AppCtx) => {
    requirePermission(ctx, 'users.view');
    const n = (sql: string, p = {}) => db.get<{ n: number }>(sql, p)!.n;
    const dayAgo = Date.now() - 86_400_000;
    return {
      users: n('SELECT COUNT(*) AS n FROM users'),
      activeToday: n('SELECT COUNT(*) AS n FROM users WHERE last_seen > :t', { t: dayAgo }),
      creditsInCirculation: n('SELECT COALESCE(SUM(credits),0) AS n FROM users'),
      creditsGrantedToday: n("SELECT COALESCE(SUM(delta),0) AS n FROM credit_ledger WHERE kind = 'grant' AND created_at > :t", { t: dayAgo }),
      openReports: n("SELECT COUNT(*) AS n FROM reports WHERE status = 'open'"),
      newFeedback: n("SELECT COUNT(*) AS n FROM feedback WHERE status = 'new'"),
      suspicious: suspiciousEarners(db),
      maintenance: app.config.maintenance,
    };
  });

  r.get('/api/admin/users', (ctx: AppCtx) => {
    requirePermission(ctx, 'users.view');
    const q = (ctx.query.get('q') ?? '').trim().toLowerCase();
    if (q.length < 2) return { users: [] };
    const rows = db.all<UserRow>(
      `SELECT * FROM users WHERE mc_name_lower LIKE :p ESCAPE '\\' OR mc_uuid = :q OR id = :q ORDER BY mc_name_lower LIMIT 50`,
      { p: `${q.replace(/[\\%_]/g, (c) => `\\${c}`)}%`, q },
    );
    return { users: rows.map((u) => ({ ...publicUser(u), credits: u.credits, lastSeen: u.last_seen, banned: !!u.banned_until && u.banned_until > Date.now() })) };
  });

  r.get('/api/admin/users/:id', (ctx: AppCtx) => {
    requirePermission(ctx, 'users.view');
    const u = getUser(db, ctx.params.id!);
    if (!u) throw notFound();
    return {
      user: { ...publicUser(u), credits: u.credits, bio: u.bio, createdAt: u.created_at, lastSeen: u.last_seen, bannedUntil: u.banned_until, banReason: u.ban_reason, customTag: u.custom_tag_text },
      roles: rolesOf(db, u.id),
      ledger: history(db, u.id, 100),
      inventory: db.all('SELECT item_id, source, acquired_at FROM inventory WHERE user_id = :u ORDER BY acquired_at DESC', { u: u.id }),
      reports: db.all('SELECT r.id, r.reason, r.details, r.status, r.created_at, users.mc_name AS reporter FROM reports r JOIN users ON users.id = r.reporter_id WHERE target_id = :u ORDER BY r.id DESC LIMIT 50', { u: u.id }),
      presence: app.events.getPresence(u.id),
    };
  });

  // Credits: grant (credits.grant) or deduct (credits.deduct). Large changes
  // require confirm=true; every change needs a reason and an idempotency key
  // so a double-clicked button can never book twice.
  r.post('/api/admin/credits', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'credits.grant');
    const b = obj(ctx.body);
    const target = getUser(db, str(b, 'userId', 3, 40));
    if (!target) throw notFound('User not found');
    const delta = int(b, 'delta', -MAX_CREDIT_CHANGE, MAX_CREDIT_CHANGE);
    if (delta === 0) throw badRequest('invalid_field', 'delta must not be 0');
    if (delta < 0 && !hasPermission(actor.roles, 'credits.deduct')) throw forbidden('Missing permission credits.deduct');
    const reason = cleanText(str(b, 'reason', 5, 300));
    const idem = str(b, 'idempotencyKey', 8, 80);
    if (Math.abs(delta) >= LARGE_CREDIT_CHANGE && b.confirm !== true) {
      throw conflict('confirmation_required', `Changes of ${LARGE_CREDIT_CHANGE} credits or more must be confirmed`);
    }
    return db.tx(() => {
      const res = applyCredits(db, {
        userId: target.id, delta, kind: delta > 0 ? 'grant' : 'deduct', reason, actorId: actor.id, idemKey: `admin:${actor.id}:${idem}`,
      });
      if (!res.duplicate) audit(db, actor.id, delta > 0 ? 'credits.grant' : 'credits.deduct', target.id, { delta, reason, balance: res.balance }, ctx.ip);
      if (!res.duplicate) app.events.send(target.id, 'credits', { balance: res.balance, delta });
      return { balance: res.balance, duplicate: res.duplicate, ledgerId: res.ledgerId };
    });
  }, { rate: ['admin', 120] });

  r.post('/api/admin/items', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'credits.grant');
    const b = obj(ctx.body);
    const target = getUser(db, str(b, 'userId', 3, 40));
    if (!target) throw notFound('User not found');
    const item = getItem(str(b, 'itemId', 2, 64));
    if (!item) throw notFound('Unknown item');
    const reason = cleanText(str(b, 'reason', 5, 300));
    return db.tx(() => {
      const added = grantItem(db, target.id, item.id, 'grant');
      audit(db, actor.id, 'items.grant', target.id, { item: item.id, reason, added }, ctx.ip);
      return { added };
    });
  }, { rate: ['admin', 120] });

  r.put('/api/admin/roles', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'roles.manage');
    const b = obj(ctx.body);
    const target = getUser(db, str(b, 'userId', 3, 40));
    if (!target) throw notFound('User not found');
    const role = oneOf(b, 'role', ROLES);
    const grant = bool(b, 'grant');
    return db.tx(() => {
      if (grant) {
        db.run('INSERT OR IGNORE INTO roles(user_id, role, granted_by, granted_at) VALUES (:u, :r, :a, :t)', { u: target.id, r: role, a: actor.id, t: Date.now() });
      } else {
        if (role === 'admin') {
          const admins = db.get<{ n: number }>("SELECT COUNT(*) AS n FROM roles WHERE role = 'admin'")!.n;
          if (admins <= 1) throw conflict('last_admin', 'Cannot remove the last admin');
        }
        db.run('DELETE FROM roles WHERE user_id = :u AND role = :r', { u: target.id, r: role });
      }
      audit(db, actor.id, grant ? 'roles.grant' : 'roles.revoke', target.id, { role }, ctx.ip);
      return { roles: rolesOf(db, target.id) };
    });
  });

  r.post('/api/admin/ban', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'users.ban');
    const b = obj(ctx.body);
    const target = getUser(db, str(b, 'userId', 3, 40));
    if (!target) throw notFound('User not found');
    if (target.id === actor.id) throw badRequest('self', 'You cannot ban yourself');
    if (rolesOf(db, target.id).includes('admin') && !actor.roles.includes('admin')) throw forbidden('Only admins can ban admins');
    const hours = b.hours === null ? null : int(b, 'hours', 0, 24 * 365 * 10);
    const reason = cleanText(str(b, 'reason', 3, 300));
    const until = hours === null ? 8_640_000_000_000_000 : hours === 0 ? null : Date.now() + hours * 3_600_000;
    return db.tx(() => {
      db.run('UPDATE users SET banned_until = :u, ban_reason = :r WHERE id = :id', { u: until, r: until ? reason : null, id: target.id });
      if (until) db.run('DELETE FROM sessions WHERE user_id = :u', { u: target.id });
      audit(db, actor.id, until ? 'users.ban' : 'users.unban', target.id, { hours, reason }, ctx.ip);
      return { bannedUntil: until };
    });
  });

  r.get('/api/admin/audit', (ctx: AppCtx) => {
    requirePermission(ctx, 'audit.view');
    const target = ctx.query.get('target');
    const action = ctx.query.get('action');
    const before = Number(ctx.query.get('before') ?? 0) || null;
    return {
      entries: db.all(
        `SELECT a.*, actor.mc_name AS actor_name, target.mc_name AS target_name FROM audit_log a
         LEFT JOIN users actor ON actor.id = a.actor_id LEFT JOIN users target ON target.id = a.target_id
         WHERE (:t IS NULL OR a.target_id = :t) AND (:ac IS NULL OR a.action = :ac) AND (:b IS NULL OR a.id < :b)
         ORDER BY a.id DESC LIMIT 100`,
        { t: target, ac: action, b: before },
      ).map((e) => ({ ...e, details: JSON.parse(String(e.details)) })),
    };
  });

  r.get('/api/admin/reports', (ctx: AppCtx) => {
    requirePermission(ctx, 'reports.manage');
    const status = ctx.query.get('status') ?? 'open';
    return {
      reports: db.all(
        `SELECT r.*, a.mc_name AS reporter_name, t.mc_name AS target_name FROM reports r
         JOIN users a ON a.id = r.reporter_id JOIN users t ON t.id = r.target_id WHERE r.status = :s ORDER BY r.id DESC LIMIT 200`,
        { s: status },
      ),
    };
  });

  r.patch('/api/admin/reports/:id', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'reports.manage');
    const status = oneOf(obj(ctx.body), 'status', ['open', 'actioned', 'dismissed'] as const);
    const rep = db.get<{ target_id: string }>('SELECT target_id FROM reports WHERE id = :id', { id: Number(ctx.params.id) });
    if (!rep) throw notFound();
    db.tx(() => {
      db.run('UPDATE reports SET status = :s, handled_by = :a WHERE id = :id', { s: status, a: actor.id, id: Number(ctx.params.id) });
      audit(db, actor.id, 'reports.update', rep.target_id, { report: Number(ctx.params.id), status }, ctx.ip);
    });
    return { ok: true };
  });

  r.post('/api/admin/news', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'news.manage');
    const b = obj(ctx.body);
    const title = cleanText(str(b, 'title', 3, 120));
    const body = cleanText(str(b, 'body', 3, 10_000));
    const kind = oneOf(b, 'kind', ['news', 'changelog', 'maintenance'] as const);
    const id = db.tx(() => {
      const nid = db.run('INSERT INTO news(title, body, kind, author_id, created_at) VALUES (:t, :b, :k, :a, :n)', { t: title, b: body, k: kind, a: actor.id, n: Date.now() }).lastInsertRowid;
      audit(db, actor.id, 'news.create', null, { id: nid, title }, ctx.ip);
      return nid;
    });
    return { id };
  });

  r.delete('/api/admin/news/:id', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'news.manage');
    db.tx(() => {
      db.run('UPDATE news SET published = 0 WHERE id = :id', { id: Number(ctx.params.id) });
      audit(db, actor.id, 'news.unpublish', null, { id: Number(ctx.params.id) }, ctx.ip);
    });
    return { ok: true };
  });

  r.get('/api/admin/promo', (ctx: AppCtx) => {
    requirePermission(ctx, 'promo.manage');
    return { codes: db.all('SELECT * FROM promo_codes ORDER BY created_at DESC LIMIT 200') };
  });

  r.post('/api/admin/promo', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'promo.manage');
    const b = obj(ctx.body);
    const code = str(b, 'code', 3, 32, { pattern: /^[A-Za-z0-9-]+$/ }).toUpperCase();
    const credits = int(b, 'credits', 1, 100_000);
    const maxUses = int(b, 'maxUses', 1, 1_000_000);
    const itemId = optStr(b, 'itemId', 2, 64);
    if (itemId && !getItem(itemId)) throw badRequest('unknown_item');
    const expiresAt = b.expiresAt === undefined || b.expiresAt === null ? null : int(b, 'expiresAt', Date.now(), Date.now() + 366 * 86_400_000);
    db.tx(() => {
      try {
        db.run('INSERT INTO promo_codes(code, credits, item_id, max_uses, expires_at, created_by, created_at) VALUES (:c, :cr, :i, :m, :e, :a, :t)',
          { c: code, cr: credits, i: itemId, m: maxUses, e: expiresAt, a: actor.id, t: Date.now() });
      } catch {
        throw conflict('code_exists', 'Code already exists');
      }
      audit(db, actor.id, 'promo.create', null, { code, credits, maxUses, itemId, expiresAt }, ctx.ip);
    });
    return { code };
  });

  r.post('/api/admin/backup', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'system.backup');
    const file = join(app.config.dataDir, 'backups', `lego-${new Date().toISOString().replace(/[:.]/g, '-')}.sqlite`);
    db.backup(file);
    audit(db, actor.id, 'system.backup', null, { file }, ctx.ip);
    return { file };
  });

  r.put('/api/admin/maintenance', (ctx: AppCtx) => {
    const actor = requirePermission(ctx, 'roles.manage');
    const enabled = bool(obj(ctx.body), 'enabled');
    app.config.maintenance = enabled;
    db.run("INSERT INTO system_state(key, value) VALUES ('maintenance', :v) ON CONFLICT(key) DO UPDATE SET value = :v", { v: enabled ? '1' : '0' });
    audit(db, actor.id, 'system.maintenance', null, { enabled }, ctx.ip);
    return { maintenance: enabled };
  });
}
