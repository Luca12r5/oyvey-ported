// Minimal HTTP layer on node:http: routing with :params, JSON bodies with a
// size limit, typed errors, security headers and static files. Kept small on
// purpose so every security-relevant behaviour is visible in one place.

import type { IncomingMessage, ServerResponse } from 'node:http';
import { createReadStream, statSync } from 'node:fs';
import { extname, join, normalize, sep } from 'node:path';

export class HttpError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: unknown;
  constructor(status: number, code: string, message?: string, details?: unknown) {
    super(message ?? code);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export const badRequest = (code: string, msg?: string, details?: unknown) => new HttpError(400, code, msg, details);
export const unauthorized = (msg = 'Login required') => new HttpError(401, 'unauthorized', msg);
export const forbidden = (msg = 'Not allowed') => new HttpError(403, 'forbidden', msg);
export const notFound = (msg = 'Not found') => new HttpError(404, 'not_found', msg);
export const conflict = (code: string, msg?: string) => new HttpError(409, code, msg);

export interface Ctx<U = unknown> {
  req: IncomingMessage;
  res: ServerResponse;
  method: string;
  path: string;
  query: URLSearchParams;
  params: Record<string, string>;
  ip: string;
  body: unknown;
  rawBody: Buffer | null;
  /** Set by the auth middleware. */
  user: U | null;
  sessionKind: 'client' | 'web' | null;
  /** Response headers to add (e.g. Set-Cookie). */
  headers: Record<string, string | string[]>;
  status: number;
}

export type Handler<U> = (ctx: Ctx<U>) => unknown | Promise<unknown>;
export type Middleware<U> = (ctx: Ctx<U>, route: RouteDef<U>) => void | Promise<void>;

export interface RouteOptions {
  /** 'user' requires a session, 'optional' parses it if present. */
  auth?: 'none' | 'optional' | 'user';
  permission?: string;
  /** Rate limit bucket name and allowance per minute (per user, else per IP). */
  rate?: [bucket: string, perMinute: number];
  /** Max JSON body size in bytes (default 64 KiB). */
  bodyLimit?: number;
  /** Keep the raw body (needed for webhook signature checks). */
  rawBody?: boolean;
  /** Accept non-JSON content types (raw body only). */
  anyContentType?: boolean;
}

export interface RouteDef<U> {
  method: string;
  parts: string[];
  handler: Handler<U>;
  opts: RouteOptions;
}

const SECURITY_HEADERS: Record<string, string> = {
  'X-Content-Type-Options': 'nosniff',
  'X-Frame-Options': 'DENY',
  'Referrer-Policy': 'no-referrer',
  'Cross-Origin-Opener-Policy': 'same-origin',
  'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
  'Content-Security-Policy':
    "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data: https://mc-heads.net; connect-src 'self'; font-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'",
};

export class Router<U> {
  private routes: RouteDef<U>[] = [];
  private middleware: Middleware<U>[] = [];

  use(mw: Middleware<U>): void {
    this.middleware.push(mw);
  }

  add(method: string, pattern: string, handler: Handler<U>, opts: RouteOptions = {}): void {
    this.routes.push({ method, parts: pattern.split('/').filter(Boolean), handler, opts });
  }
  get(p: string, h: Handler<U>, o?: RouteOptions) { this.add('GET', p, h, o); }
  post(p: string, h: Handler<U>, o?: RouteOptions) { this.add('POST', p, h, o); }
  put(p: string, h: Handler<U>, o?: RouteOptions) { this.add('PUT', p, h, o); }
  patch(p: string, h: Handler<U>, o?: RouteOptions) { this.add('PATCH', p, h, o); }
  delete(p: string, h: Handler<U>, o?: RouteOptions) { this.add('DELETE', p, h, o); }

  match(method: string, path: string): { route: RouteDef<U>; params: Record<string, string> } | null | 'method' {
    const segs = path.split('/').filter(Boolean);
    let methodMismatch = false;
    for (const r of this.routes) {
      if (r.parts.length !== segs.length) continue;
      const params: Record<string, string> = {};
      let ok = true;
      for (let i = 0; i < segs.length; i++) {
        const p = r.parts[i]!;
        const s = segs[i]!;
        if (p.startsWith(':')) {
          try {
            params[p.slice(1)] = decodeURIComponent(s);
          } catch {
            ok = false;
            break;
          }
        } else if (p !== s) {
          ok = false;
          break;
        }
      }
      if (!ok) continue;
      if (r.method !== method) {
        methodMismatch = true;
        continue;
      }
      return { route: r, params };
    }
    return methodMismatch ? 'method' : null;
  }

  async handle(req: IncomingMessage, res: ServerResponse, ip: string, fallback?: (ctx: Ctx<U>) => boolean): Promise<void> {
    const url = new URL(req.url ?? '/', 'http://local');
    const ctx: Ctx<U> = {
      req, res, method: req.method ?? 'GET', path: url.pathname, query: url.searchParams, params: {}, ip,
      body: null, rawBody: null, user: null, sessionKind: null, headers: {}, status: 200,
    };
    try {
      const m = this.match(ctx.method, ctx.path);
      if (m === null) {
        if (fallback && ctx.method === 'GET' && fallback(ctx)) return;
        throw notFound();
      }
      if (m === 'method') throw new HttpError(405, 'method_not_allowed');
      ctx.params = m.params;
      if (ctx.method !== 'GET' && ctx.method !== 'HEAD') {
        await readBody(ctx, m.route.opts);
      }
      for (const mw of this.middleware) await mw(ctx, m.route);
      const out = await m.route.handler(ctx);
      send(ctx, out === undefined ? 204 : ctx.status, out);
    } catch (e) {
      if (e instanceof HttpError) {
        send(ctx, e.status, { error: e.code, message: e.message, details: e.details });
      } else {
        console.error(`[http] ${ctx.method} ${ctx.path}`, e);
        send(ctx, 500, { error: 'internal', message: 'Internal server error' });
      }
    }
  }
}

async function readBody<U>(ctx: Ctx<U>, opts: RouteOptions): Promise<void> {
  const limit = opts.bodyLimit ?? 64 * 1024;
  const declared = Number(ctx.req.headers['content-length'] ?? 0);
  if (declared > limit) throw new HttpError(413, 'body_too_large');
  const chunks: Buffer[] = [];
  let size = 0;
  for await (const chunk of ctx.req) {
    size += (chunk as Buffer).length;
    if (size > limit) throw new HttpError(413, 'body_too_large');
    chunks.push(chunk as Buffer);
  }
  const raw = Buffer.concat(chunks);
  if (opts.rawBody) ctx.rawBody = raw;
  if (raw.length === 0) return;
  const type = String(ctx.req.headers['content-type'] ?? '').split(';')[0]!.trim().toLowerCase();
  if (type !== 'application/json') {
    if (opts.anyContentType) return;
    // Refusing other types also blocks HTML-form based CSRF.
    throw new HttpError(415, 'unsupported_media_type', 'Use application/json');
  }
  try {
    ctx.body = JSON.parse(raw.toString('utf8'));
  } catch {
    throw badRequest('invalid_json');
  }
}

export function send<U>(ctx: Ctx<U>, status: number, body: unknown): void {
  const { res } = ctx;
  if (res.headersSent) return;
  for (const [k, v] of Object.entries(SECURITY_HEADERS)) res.setHeader(k, v);
  for (const [k, v] of Object.entries(ctx.headers)) res.setHeader(k, v);
  res.setHeader('Cache-Control', 'no-store');
  if (status === 204 || body === undefined) {
    res.statusCode = 204;
    res.end();
    return;
  }
  const json = JSON.stringify(body);
  res.statusCode = status;
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.setHeader('Content-Length', Buffer.byteLength(json));
  res.end(json);
}

const MIME: Record<string, string> = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8',
  '.json': 'application/json', '.png': 'image/png', '.svg': 'image/svg+xml', '.ico': 'image/x-icon',
  '.woff2': 'font/woff2', '.webp': 'image/webp', '.txt': 'text/plain; charset=utf-8',
};

/** Serves files below `root` only; unknown paths fall back to index.html (SPA). */
export function serveStatic<U>(root: string, ctx: Ctx<U>): boolean {
  const rel = normalize(decodeURIComponent(ctx.path)).replace(/^([/\\])+/, '');
  let file = join(root, rel);
  if (!file.startsWith(root + sep) && file !== root) return false;
  let st;
  try {
    st = statSync(file);
    if (st.isDirectory()) {
      file = join(file, 'index.html');
      st = statSync(file);
    }
  } catch {
    if (extname(rel)) return false;
    file = join(root, 'index.html');
    try {
      st = statSync(file);
    } catch {
      return false;
    }
  }
  const { res } = ctx;
  for (const [k, v] of Object.entries(SECURITY_HEADERS)) res.setHeader(k, v);
  res.setHeader('Content-Type', MIME[extname(file)] ?? 'application/octet-stream');
  res.setHeader('Content-Length', st.size);
  res.setHeader('Cache-Control', extname(file) === '.html' ? 'no-cache' : 'public, max-age=3600');
  createReadStream(file).pipe(res);
  return true;
}
