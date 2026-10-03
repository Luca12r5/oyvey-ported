// Feedback, bug reports and feature requests with voting, comments and
// optional attachments (PNG/JPEG screenshots, text logs).

import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { FEEDBACK_KINDS, FEEDBACK_STATUSES, hasPermission } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { badRequest, forbidden, HttpError, notFound } from '../http.ts';
import { cleanText, newId, obj, oneOf, sha256, str } from '../security.ts';

const MAX_ATTACHMENTS = 3;
const MAX_IMAGE = 2 * 1024 * 1024;
const MAX_LOG = 1024 * 1024;

interface Upload { filename: string; mime: string; data: Buffer }

/** Validates by content, not by the claimed type. */
export function validateAttachment(raw: unknown): Upload {
  const a = obj(raw);
  const filename = str(a, 'filename', 1, 80).replace(/[^A-Za-z0-9._ -]/g, '_');
  const b64 = str(a, 'data', 4, 4_000_000, { trim: false });
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(b64)) throw badRequest('invalid_attachment', 'Attachment must be base64');
  const data = Buffer.from(b64, 'base64');
  if (data.length >= 8 && data.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]))) {
    if (data.length > MAX_IMAGE) throw badRequest('attachment_too_large', 'Images up to 2 MB');
    return { filename, mime: 'image/png', data };
  }
  if (data.length >= 3 && data[0] === 0xff && data[1] === 0xd8 && data[2] === 0xff) {
    if (data.length > MAX_IMAGE) throw badRequest('attachment_too_large', 'Images up to 2 MB');
    return { filename, mime: 'image/jpeg', data };
  }
  // Otherwise it must be plain UTF-8 text (logs, crash reports).
  if (data.length > MAX_LOG) throw badRequest('attachment_too_large', 'Logs up to 1 MB');
  const text = data.toString('utf8');
  if (text.includes('\u0000') || Buffer.from(text, 'utf8').length !== data.length) {
    throw badRequest('invalid_attachment', 'Only PNG, JPEG or UTF-8 text files are accepted');
  }
  return { filename: filename.endsWith('.txt') || filename.endsWith('.log') ? filename : `${filename}.txt`, mime: 'text/plain', data };
}

export function registerFeedback(app: App): void {
  const r = app.router;
  const db = app.db;
  const dir = join(app.config.dataDir, 'attachments');

  r.post('/api/feedback', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const b = obj(ctx.body);
    const kind = oneOf(b, 'kind', FEEDBACK_KINDS);
    const title = cleanText(str(b, 'title', 4, 120));
    const body = cleanText(str(b, 'body', 10, 5000));
    const rawAtt = b.attachments ?? [];
    if (!Array.isArray(rawAtt) || rawAtt.length > MAX_ATTACHMENTS) throw badRequest('invalid_field', `Up to ${MAX_ATTACHMENTS} attachments`);
    const uploads = rawAtt.map(validateAttachment);
    const now = Date.now();
    const id = db.tx(() => {
      const fid = db.run('INSERT INTO feedback(user_id, kind, title, body, created_at, updated_at) VALUES (:u, :k, :ti, :b, :t, :t)', { u: u.id, k: kind, ti: title, b: body, t: now }).lastInsertRowid;
      if (uploads.length) mkdirSync(dir, { recursive: true });
      for (const up of uploads) {
        const aid = newId('a_');
        writeFileSync(join(dir, aid), up.data, { mode: 0o600 });
        db.run('INSERT INTO attachments(id, feedback_id, filename, mime, size, sha256, created_at) VALUES (:id, :f, :n, :m, :s, :h, :t)',
          { id: aid, f: fid, n: up.filename, m: up.mime, s: up.data.length, h: sha256(up.data), t: now });
      }
      return fid;
    });
    return { id, status: 'new' };
  }, { rate: ['feedback', 5], bodyLimit: 12 * 1024 * 1024 });

  // Public board: suggestions/requests are visible to every signed-in user;
  // bug, performance and support tickets only to their author and staff.
  r.get('/api/feedback', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const kind = ctx.query.get('kind');
    const status = ctx.query.get('status');
    const mine = ctx.query.get('mine') === '1';
    const staff = hasPermission(u.roles, 'feedback.manage');
    const rows = db.all(
      `SELECT f.id, f.kind, f.title, f.body, f.status, f.votes, f.created_at, f.updated_at, f.user_id, users.mc_name AS author,
              EXISTS(SELECT 1 FROM feedback_votes v WHERE v.feedback_id = f.id AND v.user_id = :u) AS voted
       FROM feedback f JOIN users ON users.id = f.user_id
       WHERE (:kind IS NULL OR f.kind = :kind) AND (:status IS NULL OR f.status = :status)
         AND (:mine = 0 OR f.user_id = :u)
         AND (:staff = 1 OR f.user_id = :u OR f.kind IN ('suggestion','minigame','cosmetic','theme'))
       ORDER BY ${ctx.query.get('sort') === 'votes' ? 'f.votes DESC,' : ''} f.id DESC LIMIT 200`,
      { u: u.id, kind, status, mine: mine ? 1 : 0, staff: staff ? 1 : 0 },
    );
    return { items: rows.map((x) => ({ ...x, voted: x.voted === 1 })) };
  });

  function load(ctx: AppCtx, id: number) {
    const u = requireUser(ctx);
    const f = db.get<{ id: number; user_id: string; kind: string }>('SELECT * FROM feedback WHERE id = :id', { id });
    if (!f) throw notFound();
    const isPublic = ['suggestion', 'minigame', 'cosmetic', 'theme'].includes(f.kind);
    if (!isPublic && f.user_id !== u.id && !hasPermission(u.roles, 'feedback.manage')) throw notFound();
    return f;
  }

  r.get('/api/feedback/:id', (ctx: AppCtx) => {
    const f = load(ctx, Number(ctx.params.id));
    const u = requireUser(ctx);
    const canFiles = f.user_id === u.id || hasPermission(u.roles, 'feedback.manage');
    return {
      item: db.get('SELECT f.*, users.mc_name AS author FROM feedback f JOIN users ON users.id = f.user_id WHERE f.id = :id', { id: f.id }),
      comments: db.all('SELECT c.id, c.body, c.staff, c.created_at, users.mc_name AS author FROM feedback_comments c JOIN users ON users.id = c.user_id WHERE feedback_id = :id ORDER BY c.id', { id: f.id }),
      attachments: canFiles ? db.all('SELECT id, filename, mime, size FROM attachments WHERE feedback_id = :id', { id: f.id }) : [],
    };
  });

  r.post('/api/feedback/:id/vote', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const f = load(ctx, Number(ctx.params.id));
    const up = obj(ctx.body).up !== false;
    db.tx(() => {
      if (up) {
        const n = db.run('INSERT OR IGNORE INTO feedback_votes(feedback_id, user_id) VALUES (:f, :u)', { f: f.id, u: u.id }).changes;
        if (n) db.run('UPDATE feedback SET votes = votes + 1 WHERE id = :f', { f: f.id });
      } else {
        const n = db.run('DELETE FROM feedback_votes WHERE feedback_id = :f AND user_id = :u', { f: f.id, u: u.id }).changes;
        if (n) db.run('UPDATE feedback SET votes = votes - 1 WHERE id = :f', { f: f.id });
      }
    });
    return { votes: db.get<{ votes: number }>('SELECT votes FROM feedback WHERE id = :f', { f: f.id })!.votes };
  }, { rate: ['vote', 60] });

  r.post('/api/feedback/:id/comments', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const f = load(ctx, Number(ctx.params.id));
    const body = cleanText(str(obj(ctx.body), 'body', 1, 2000));
    const staff = hasPermission(u.roles, 'feedback.manage');
    db.run('INSERT INTO feedback_comments(feedback_id, user_id, body, staff, created_at) VALUES (:f, :u, :b, :s, :t)', { f: f.id, u: u.id, b: body, s: staff ? 1 : 0, t: Date.now() });
    db.run('UPDATE feedback SET updated_at = :t WHERE id = :f', { t: Date.now(), f: f.id });
    if (f.user_id !== u.id) app.events.send(f.user_id, 'feedback_update', { id: f.id });
    return { ok: true };
  }, { rate: ['feedback', 20] });

  r.patch('/api/feedback/:id/status', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    if (!hasPermission(u.roles, 'feedback.manage')) throw forbidden();
    const f = load(ctx, Number(ctx.params.id));
    const status = oneOf(obj(ctx.body), 'status', FEEDBACK_STATUSES);
    db.run('UPDATE feedback SET status = :s, updated_at = :t WHERE id = :f', { s: status, t: Date.now(), f: f.id });
    db.run('INSERT INTO audit_log(actor_id, action, target_id, details, ip, created_at) VALUES (:a, :ac, :t, :d, :ip, :ts)',
      { a: u.id, ac: 'feedback.status', t: f.user_id, d: JSON.stringify({ feedback: f.id, status }), ip: ctx.ip, ts: Date.now() });
    app.events.send(f.user_id, 'feedback_update', { id: f.id, status });
    return { ok: true };
  });

  r.get('/api/feedback/attachments/:id', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const a = db.get<{ id: string; feedback_id: number; filename: string; mime: string }>('SELECT * FROM attachments WHERE id = :id', { id: ctx.params.id! });
    if (!a) throw notFound();
    const f = db.get<{ user_id: string }>('SELECT user_id FROM feedback WHERE id = :id', { id: a.feedback_id })!;
    if (f.user_id !== u.id && !hasPermission(u.roles, 'feedback.manage')) throw notFound();
    let data: Buffer;
    try {
      data = readFileSync(join(dir, a.id));
    } catch {
      throw new HttpError(410, 'gone', 'Attachment file missing');
    }
    const res = ctx.res;
    res.setHeader('Content-Type', a.mime === 'text/plain' ? 'text/plain; charset=utf-8' : a.mime);
    res.setHeader('Content-Disposition', `attachment; filename="${a.filename.replace(/"/g, '')}"`);
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('Content-Security-Policy', "default-src 'none'; sandbox");
    res.setHeader('Cache-Control', 'private, no-store');
    res.end(data);
    return undefined;
  });
}
