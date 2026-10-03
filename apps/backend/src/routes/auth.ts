// Authentication.
//
// Launcher / client: Mojang session handshake (the same mechanism a Minecraft
// server uses), so the backend never receives the Minecraft access token:
//   1. POST /api/auth/challenge            -> { challengeId, serverId }
//   2. client POSTs sessionserver.mojang.com/session/minecraft/join
//      with its own access token and serverId
//   3. POST /api/auth/verify {challengeId, username}
//      backend asks /session/minecraft/hasJoined and issues a LEGO token.
//
// Website: the logged-in launcher creates a one-time code; entering it on the
// website creates an HttpOnly cookie session with a CSRF token.

import type { App, AppCtx } from '../app.ts';
import { requireUser, SESSION_COOKIE } from '../app.ts';
import { forbidden, HttpError, unauthorized } from '../http.ts';
import { MC_NAME, newId, obj, randomToken, sha256, shortCode, str } from '../security.ts';
import { dashUuid, rolesOf, upsertMinecraftUser } from '../services/users.ts';

const CHALLENGE_TTL = 2 * 60_000;
const CLIENT_SESSION_TTL = 30 * 86_400_000;
const WEB_SESSION_TTL = 7 * 86_400_000;
const WEB_CODE_TTL = 5 * 60_000;

export function createSession(app: App, userId: string, kind: 'client' | 'web', userAgent: string | null): { token: string; csrf: string; expiresAt: number } {
  const token = randomToken(32);
  const csrf = randomToken(24);
  const now = Date.now();
  const expiresAt = now + (kind === 'client' ? CLIENT_SESSION_TTL : WEB_SESSION_TTL);
  app.db.run(
    `INSERT INTO sessions(token_hash, user_id, kind, csrf, created_at, expires_at, last_used, user_agent)
     VALUES (:h, :u, :k, :c, :t, :e, :t, :ua)`,
    { h: sha256(token), u: userId, k: kind, c: csrf, t: now, e: expiresAt, ua: userAgent?.slice(0, 200) ?? null },
  );
  // Opportunistic cleanup of expired rows.
  app.db.run('DELETE FROM sessions WHERE expires_at < :t', { t: now });
  app.db.run('DELETE FROM auth_challenges WHERE created_at < :t', { t: now - CHALLENGE_TTL });
  app.db.run('DELETE FROM web_codes WHERE expires_at < :t', { t: now });
  return { token, csrf, expiresAt };
}

function cookie(app: App, value: string, maxAgeSec: number): string {
  const parts = [`${SESSION_COOKIE}=${encodeURIComponent(value)}`, 'Path=/', 'HttpOnly', 'SameSite=Strict', `Max-Age=${maxAgeSec}`];
  if (app.config.secureCookies) parts.push('Secure');
  return parts.join('; ');
}

function sessionPayload(app: App, userId: string) {
  const u = app.db.get<{ id: string; mc_uuid: string; mc_name: string }>('SELECT id, mc_uuid, mc_name FROM users WHERE id = :id', { id: userId })!;
  return { user: { id: u.id, uuid: u.mc_uuid, name: u.mc_name, roles: rolesOf(app.db, u.id) } };
}

export function registerAuth(app: App): void {
  const r = app.router;

  r.post('/api/auth/challenge', () => {
    const id = newId('c_');
    // Random hex id; the session server accepts any string up to 40 chars.
    const serverId = sha256(randomToken(32)).slice(0, 40);
    app.db.run('INSERT INTO auth_challenges(id, server_id, created_at) VALUES (:id, :s, :t)', { id, s: serverId, t: Date.now() });
    return { challengeId: id, serverId };
  }, { auth: 'none', rate: ['auth', 30] });

  r.post('/api/auth/verify', async (ctx: AppCtx) => {
    const b = obj(ctx.body);
    const challengeId = str(b, 'challengeId', 3, 64);
    const username = str(b, 'username', 3, 16, { pattern: MC_NAME });
    const ch = app.db.get<{ server_id: string; created_at: number; used: number }>(
      'SELECT server_id, created_at, used FROM auth_challenges WHERE id = :id', { id: challengeId });
    if (!ch || ch.used || Date.now() - ch.created_at > CHALLENGE_TTL) throw unauthorized('Challenge expired, start again');
    // Single use, even if verification fails.
    app.db.run('UPDATE auth_challenges SET used = 1 WHERE id = :id', { id: challengeId });

    const url = `${app.config.sessionServer}/session/minecraft/hasJoined?username=${encodeURIComponent(username)}&serverId=${encodeURIComponent(ch.server_id)}`;
    let res: Response;
    try {
      res = await app.fetch(url, { signal: AbortSignal.timeout(8000) });
    } catch {
      throw new HttpError(502, 'session_server_unreachable', 'Could not reach the Minecraft session server');
    }
    if (res.status === 204 || res.status === 404) throw unauthorized('Minecraft session not verified (join call missing or wrong account)');
    if (!res.ok) throw new HttpError(502, 'session_server_error', `Session server answered ${res.status}`);
    const profile = (await res.json()) as { id?: string; name?: string };
    if (!profile.id || !profile.name || !MC_NAME.test(profile.name)) throw new HttpError(502, 'session_server_error', 'Malformed profile');
    const user = upsertMinecraftUser(app.db, dashUuid(profile.id), profile.name);
    const s = createSession(app, user.id, 'client', String(ctx.req.headers['user-agent'] ?? ''));
    return { token: s.token, expiresAt: s.expiresAt, ...sessionPayload(app, user.id) };
  }, { auth: 'none', rate: ['auth', 20] });

  r.post('/api/auth/web-code', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    if (ctx.sessionKind !== 'client') throw forbidden('Create website codes from the launcher');
    const code = shortCode(8);
    const expiresAt = Date.now() + WEB_CODE_TTL;
    app.db.run('INSERT INTO web_codes(code_hash, user_id, expires_at) VALUES (:h, :u, :e)', { h: sha256(code), u: u.id, e: expiresAt });
    return { code, expiresAt };
  }, { rate: ['webcode', 10] });

  r.post('/api/auth/web-login', (ctx: AppCtx) => {
    const b = obj(ctx.body);
    const code = str(b, 'code', 8, 9).replace('-', '').toUpperCase();
    const row = app.db.get<{ user_id: string; expires_at: number; used: number }>(
      'SELECT user_id, expires_at, used FROM web_codes WHERE code_hash = :h', { h: sha256(code) });
    if (!row || row.used || row.expires_at < Date.now()) throw unauthorized('Invalid or expired code');
    app.db.run('UPDATE web_codes SET used = 1 WHERE code_hash = :h', { h: sha256(code) });
    const s = createSession(app, row.user_id, 'web', String(ctx.req.headers['user-agent'] ?? ''));
    ctx.headers['Set-Cookie'] = cookie(app, s.token, Math.floor(WEB_SESSION_TTL / 1000));
    return { csrf: s.csrf, ...sessionPayload(app, row.user_id) };
  }, { auth: 'none', rate: ['weblogin', 10] });

  r.get('/api/auth/session', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    return { csrf: ctx.sessionKind === 'web' ? u.csrf : null, kind: ctx.sessionKind, ...sessionPayload(app, u.id) };
  });

  r.post('/api/auth/logout', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    app.db.run('DELETE FROM sessions WHERE token_hash = :h', { h: u.tokenHash });
    ctx.headers['Set-Cookie'] = cookie(app, '', 0);
    return { ok: true };
  });

  r.post('/api/auth/logout-all', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const n = app.db.run('DELETE FROM sessions WHERE user_id = :u', { u: u.id }).changes;
    ctx.headers['Set-Cookie'] = cookie(app, '', 0);
    return { ok: true, sessionsRevoked: n };
  });

  r.get('/api/auth/sessions', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    return {
      sessions: app.db.all(
        'SELECT substr(token_hash, 1, 12) AS id, kind, created_at, last_used, expires_at, user_agent FROM sessions WHERE user_id = :u ORDER BY last_used DESC',
        { u: u.id },
      ).map((s) => ({ ...s, current: (s.id as string) === u.tokenHash.slice(0, 12) })),
    };
  });

}
