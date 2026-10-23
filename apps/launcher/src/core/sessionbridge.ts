// Local bridge between the launcher and the games it started (LEGO Client).
//
// The game gets a random key per launch (environment variable, not the command line) and can
// then, over 127.0.0.1 only:
//   POST /session   – a fresh Minecraft session for the account it was started with (the same
//                     token the launch arguments carry, renewed when it is about to expire),
//   GET  /accounts  – names and uuids of the signed-in accounts (no tokens),
//   POST /switch    – ask the launcher to restart the game with another account.
// A key only ever yields its own account's session and dies with the game process.

import { createServer, type IncomingMessage, type Server, type ServerResponse } from 'node:http';
import { randomBytes, timingSafeEqual } from 'node:crypto';

export interface BridgeSession { name: string; uuid: string; accessToken: string; xuid: string | null; expiresAt: number }

export interface BridgeHooks {
  /** Session for this account; renewed when it runs out soon or `force` is set. */
  session(uuid: string, force: boolean): Promise<BridgeSession>;
  accounts(): { uuid: string; name: string }[];
  /** The game asks to be restarted with another account (and optionally rejoin a server). */
  requestSwitch(key: string, uuid: string, server: string | null): void;
}

interface Launch { uuid: string; lastAsk: number; forced: number[] }

/** Plain session requests per key: at most one per this many ms. */
export const ASK_GAP_MS = 20_000;
/** Forced renewals per key and hour. */
export const FORCE_PER_HOUR = 3;

export class SessionBridge {
  private server: Server | null = null;
  private launches = new Map<string, Launch>();
  private hooks: BridgeHooks;
  private now: () => number;
  port = 0;

  constructor(hooks: BridgeHooks, now: () => number = () => Date.now()) {
    this.hooks = hooks;
    this.now = now;
  }

  async start(): Promise<number> {
    if (this.server) return this.port;
    const server = createServer((req, res) => { void this.handle(req, res); });
    await new Promise<void>((resolve, reject) => {
      server.once('error', reject);
      server.listen(0, '127.0.0.1', () => resolve());
    });
    server.unref();
    this.server = server;
    const addr = server.address();
    this.port = typeof addr === 'object' && addr ? addr.port : 0;
    return this.port;
  }

  async stop(): Promise<void> {
    const s = this.server;
    this.server = null;
    this.launches.clear();
    if (s) await new Promise<void>((r) => s.close(() => r()));
  }

  /** New key for a game started with this account. */
  register(uuid: string): string {
    const key = randomBytes(32).toString('hex');
    this.launches.set(key, { uuid, lastAsk: 0, forced: [] });
    return key;
  }

  revoke(key: string): void {
    this.launches.delete(key);
  }

  /** Environment for the game process. */
  env(key: string): Record<string, string> {
    return { LEGO_SESSION_PORT: String(this.port), LEGO_SESSION_KEY: key };
  }

  private launchFor(req: IncomingMessage): [string, Launch] | null {
    const given = req.headers['x-lego-key'];
    if (typeof given !== 'string' || given.length !== 64) return null;
    const a = Buffer.from(given);
    for (const [key, l] of this.launches) {
      const b = Buffer.from(key);
      if (a.length === b.length && timingSafeEqual(a, b)) return [key, l];
    }
    return null;
  }

  private async handle(req: IncomingMessage, res: ServerResponse): Promise<void> {
    const reply = (code: number, body: unknown) => {
      res.writeHead(code, { 'Content-Type': 'application/json', 'Cache-Control': 'no-store' });
      res.end(JSON.stringify(body));
    };
    try {
      // Loopback only, and only addressed as 127.0.0.1:<port> (no DNS rebinding through a browser).
      const remote = req.socket.remoteAddress ?? '';
      if (!['127.0.0.1', '::1', '::ffff:127.0.0.1'].includes(remote)) return reply(403, { error: 'forbidden' });
      if (req.headers.host !== `127.0.0.1:${this.port}`) return reply(403, { error: 'forbidden' });
      const hit = this.launchFor(req);
      if (!hit) return reply(401, { error: 'unknown key' });
      const [key, launch] = hit;
      const body = req.method === 'POST' ? await readJson(req) : {};
      const url = req.url ?? '';

      if (req.method === 'POST' && url === '/session') {
        const force = body.force === true;
        const t = this.now();
        if (force) {
          launch.forced = launch.forced.filter((x) => t - x < 3_600_000);
          if (launch.forced.length >= FORCE_PER_HOUR) return reply(429, { error: 'too many renewals' });
          launch.forced.push(t);
        } else {
          if (t - launch.lastAsk < ASK_GAP_MS) return reply(429, { error: 'too soon' });
          launch.lastAsk = t;
        }
        const s = await this.hooks.session(launch.uuid, force);
        if (s.uuid !== launch.uuid) return reply(409, { error: 'account changed' });
        return reply(200, s);
      }
      if (req.method === 'GET' && url === '/accounts') {
        return reply(200, { current: launch.uuid, accounts: this.hooks.accounts().map((a) => ({ uuid: a.uuid, name: a.name })) });
      }
      if (req.method === 'POST' && url === '/switch') {
        const uuid = typeof body.uuid === 'string' ? body.uuid : '';
        if (!this.hooks.accounts().some((a) => a.uuid === uuid)) return reply(404, { error: 'unknown account' });
        const server = typeof body.server === 'string' && /^[\w.-]{1,253}(:\d{1,5})?$/.test(body.server) ? body.server : null;
        this.hooks.requestSwitch(key, uuid, server);
        return reply(200, { ok: true });
      }
      return reply(404, { error: 'not found' });
    } catch (e) {
      return reply(500, { error: e instanceof Error ? e.message : 'error' });
    }
  }
}

function readJson(req: IncomingMessage): Promise<Record<string, unknown>> {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks: Buffer[] = [];
    req.on('data', (c: Buffer) => {
      size += c.length;
      if (size > 4096) { reject(new Error('body too large')); req.destroy(); return; }
      chunks.push(c);
    });
    req.on('end', () => {
      if (!chunks.length) return resolve({});
      try {
        const v: unknown = JSON.parse(Buffer.concat(chunks).toString('utf8'));
        resolve(v && typeof v === 'object' && !Array.isArray(v) ? (v as Record<string, unknown>) : {});
      } catch { reject(new Error('bad json')); }
    });
    req.on('error', reject);
  });
}
