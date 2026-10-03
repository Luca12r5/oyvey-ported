// Wires config, database, auth middleware and all route modules together.

import type { IncomingMessage, ServerResponse } from 'node:http';
import { join } from 'node:path';
import { hasPermission, type Permission } from '@lego/shared';
import type { Config } from './config.ts';
import { Db } from './db.ts';
import { EventHub } from './events.ts';
import { forbidden, HttpError, Router, serveStatic, unauthorized, type Ctx } from './http.ts';
import { RateLimiter, safeEqual, sha256, tooMany } from './security.ts';
import { registerAuth } from './routes/auth.ts';
import { registerMe } from './routes/me.ts';
import { registerSocial } from './routes/social.ts';
import { registerCosmetics } from './routes/cosmetics.ts';
import { registerProgression } from './routes/progression.ts';
import { registerFeedback } from './routes/feedback.ts';
import { registerAdmin } from './routes/admin.ts';
import { registerPublic } from './routes/public.ts';
import { registerPayments } from './routes/payments.ts';
import { registerEvents } from './routes/events.ts';

export interface AuthUser {
  id: string;
  mcUuid: string;
  mcName: string;
  roles: string[];
  csrf: string;
  tokenHash: string;
}

export type AppCtx = Ctx<AuthUser>;

export interface App {
  config: Config;
  db: Db;
  router: Router<AuthUser>;
  limiter: RateLimiter;
  events: EventHub;
  /** Injected for tests; defaults to global fetch. */
  fetch: typeof fetch;
  handle(req: IncomingMessage, res: ServerResponse): Promise<void>;
}

export const SESSION_COOKIE = 'lego_session';

export function parseCookies(header: string | undefined): Record<string, string> {
  const out: Record<string, string> = {};
  if (!header) return out;
  for (const part of header.split(';')) {
    const i = part.indexOf('=');
    if (i < 0) continue;
    const k = part.slice(0, i).trim();
    const v = part.slice(i + 1).trim();
    if (k) {
      try {
        out[k] = decodeURIComponent(v);
      } catch {
        // ignore malformed cookie
      }
    }
  }
  return out;
}

export function requirePermission(ctx: AppCtx, p: Permission): AuthUser {
  if (!ctx.user) throw unauthorized();
  if (!hasPermission(ctx.user.roles, p)) throw forbidden(`Missing permission ${p}`);
  return ctx.user;
}

export function requireUser(ctx: AppCtx): AuthUser {
  if (!ctx.user) throw unauthorized();
  return ctx.user;
}

interface SessionRow {
  token_hash: string;
  user_id: string;
  kind: 'client' | 'web';
  csrf: string;
  expires_at: number;
  mc_uuid: string;
  mc_name: string;
  banned_until: number | null;
}

export function createApp(config: Config, opts: { db?: Db; fetch?: typeof fetch } = {}): App {
  const db = opts.db ?? new Db(join(config.dataDir, 'lego.sqlite'));
  db.migrate();
  const router = new Router<AuthUser>();
  const app: App = {
    config,
    db,
    router,
    limiter: new RateLimiter(),
    events: new EventHub(),
    fetch: opts.fetch ?? fetch,
    async handle(req, res) {
      const ip = clientIp(req, config.trustProxy);
      await router.handle(req, res, ip, (ctx) => !ctx.path.startsWith('/api/') && serveStatic(config.websiteDir, ctx));
    },
  };

  // --- auth + CSRF + rate limit middleware -------------------------------
  router.use((ctx, route) => {
    const authHeader = String(ctx.req.headers.authorization ?? '');
    const cookies = parseCookies(ctx.req.headers.cookie);
    let token: string | null = null;
    let viaCookie = false;
    if (authHeader.startsWith('Bearer ')) token = authHeader.slice(7).trim();
    else if (cookies[SESSION_COOKIE]) {
      token = cookies[SESSION_COOKIE]!;
      viaCookie = true;
    }
    if (token) {
      const hash = sha256(token);
      const s = db.get<SessionRow>(
        `SELECT s.token_hash, s.user_id, s.kind, s.csrf, s.expires_at, u.mc_uuid, u.mc_name, u.banned_until
         FROM sessions s JOIN users u ON u.id = s.user_id WHERE s.token_hash = :h`,
        { h: hash },
      );
      const now = Date.now();
      if (s && s.expires_at > now) {
        if (s.banned_until && s.banned_until > now) {
          throw new HttpError(403, 'banned', 'This account is banned', { until: s.banned_until });
        }
        const roles = db.all<{ role: string }>('SELECT role FROM roles WHERE user_id = :u', { u: s.user_id }).map((r) => r.role);
        ctx.user = { id: s.user_id, mcUuid: s.mc_uuid, mcName: s.mc_name, roles, csrf: s.csrf, tokenHash: hash };
        ctx.sessionKind = s.kind;
        db.run('UPDATE sessions SET last_used = :t WHERE token_hash = :h', { t: now, h: hash });
        db.run('UPDATE users SET last_seen = :t WHERE id = :u', { t: now, u: s.user_id });
      }
    }
    const auth = route.opts.auth ?? 'user';
    if (auth === 'user' && !ctx.user) throw unauthorized();

    // Cookie sessions must prove same-origin intent for state changes.
    if (viaCookie && ctx.user && ctx.method !== 'GET' && ctx.method !== 'HEAD') {
      const origin = ctx.req.headers.origin;
      if (origin && origin !== config.publicOrigin) throw forbidden('Cross-origin request rejected');
      const header = String(ctx.req.headers['x-csrf-token'] ?? '');
      if (!header || !safeEqual(header, ctx.user.csrf)) throw forbidden('Missing or invalid CSRF token');
    }

    if (config.maintenance && !ctx.path.startsWith('/api/public') && !(ctx.user && ctx.user.roles.length > 0)) {
      throw new HttpError(503, 'maintenance', 'LEGO services are in maintenance mode');
    }

    if (route.opts.permission) requirePermission(ctx, route.opts.permission as Permission);

    if (route.opts.rate) {
      const [bucket, perMinute] = route.opts.rate;
      const key = `${bucket}:${ctx.user ? ctx.user.id : ctx.ip}`;
      const wait = app.limiter.check(key, perMinute);
      if (wait > 0) {
        ctx.headers['Retry-After'] = String(wait);
        throw tooMany(wait);
      }
    }
  });

  registerPublic(app);
  registerAuth(app);
  registerMe(app);
  registerSocial(app);
  registerCosmetics(app);
  registerProgression(app);
  registerFeedback(app);
  registerAdmin(app);
  registerPayments(app);
  registerEvents(app);
  return app;
}

function clientIp(req: IncomingMessage, trustProxy: boolean): string {
  if (trustProxy) {
    const xf = String(req.headers['x-forwarded-for'] ?? '').split(',')[0]?.trim();
    if (xf) return xf;
  }
  return req.socket.remoteAddress ?? 'unknown';
}
