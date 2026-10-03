// Test harness: real HTTP server on a random port, in-memory SQLite and a
// fake Mojang session server (the join/hasJoined handshake is simulated).

import { createServer, type Server } from 'node:http';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createHmac, randomUUID } from 'node:crypto';
import { createApp, type App } from '../src/app.ts';
import { loadConfig } from '../src/config.ts';
import { Db } from '../src/db.ts';

export interface Harness {
  app: App;
  base: string;
  /** serverId -> profile, filled by `join` like sessionserver.mojang.com would. */
  joined: Map<string, { id: string; name: string }>;
  close(): Promise<void>;
}

export async function start(env: Record<string, string> = {}): Promise<Harness> {
  const dataDir = mkdtempSync(join(tmpdir(), 'lego-test-'));
  const config = loadConfig({
    LEGO_DATA_DIR: dataDir, LEGO_SECURE_COOKIES: '0', LEGO_PUBLIC_ORIGIN: 'http://lego.test',
    LEGO_SESSION_SERVER: 'https://session.test', ...env,
  });
  const joined = new Map<string, { id: string; name: string }>();
  const fakeFetch = (async (input: string | URL | Request) => {
    const url = new URL(String(input));
    if (url.host === 'session.test' && url.pathname === '/session/minecraft/hasJoined') {
      const p = joined.get(url.searchParams.get('serverId') ?? '');
      if (!p || p.name !== url.searchParams.get('username')) return new Response(null, { status: 204 });
      return Response.json(p);
    }
    throw new Error(`unexpected fetch ${url}`);
  }) as typeof fetch;
  const app = createApp(config, { db: new Db(':memory:'), fetch: fakeFetch });
  const server: Server = createServer((req, res) => void app.handle(req, res));
  await new Promise<void>((r) => server.listen(0, '127.0.0.1', r));
  const addr = server.address();
  const port = typeof addr === 'object' && addr ? addr.port : 0;
  return {
    app, base: `http://127.0.0.1:${port}`, joined,
    async close() {
      app.events.close();
      server.closeAllConnections();
      await new Promise<void>((r) => server.close(() => r()));
      app.db.close();
      rmSync(dataDir, { recursive: true, force: true });
    },
  };
}

export interface Client {
  token: string;
  id: string;
  name: string;
  uuid: string;
  call(method: string, path: string, body?: unknown, headers?: Record<string, string>): Promise<{ status: number; body: any; headers: Headers }>;
}

export async function call(h: Harness, method: string, path: string, body?: unknown, headers: Record<string, string> = {}) {
  const res = await fetch(h.base + path, {
    method,
    headers: { ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}), ...headers },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let parsed: any = null;
  try {
    parsed = text ? JSON.parse(text) : null;
  } catch {
    parsed = text;
  }
  return { status: res.status, body: parsed, headers: res.headers };
}

/** Full launcher login: challenge -> (simulated) join -> verify. */
export async function login(h: Harness, name: string, uuid = randomUUID().replace(/-/g, '')): Promise<Client> {
  const ch = await call(h, 'POST', '/api/auth/challenge');
  h.joined.set(ch.body.serverId, { id: uuid, name });
  const v = await call(h, 'POST', '/api/auth/verify', { challengeId: ch.body.challengeId, username: name });
  if (v.status !== 200) throw new Error(`login failed ${v.status} ${JSON.stringify(v.body)}`);
  const token = v.body.token as string;
  return {
    token, id: v.body.user.id, name, uuid: v.body.user.uuid,
    call: (m, p, b, hd = {}) => call(h, m, p, b, { Authorization: `Bearer ${token}`, ...hd }),
  };
}

export function grantRole(h: Harness, userId: string, role: string): void {
  h.app.db.run('INSERT OR IGNORE INTO roles(user_id, role, granted_at) VALUES (:u, :r, :t)', { u: userId, r: role, t: Date.now() });
}

export function stripeSignature(body: string, secret: string, t = Math.floor(Date.now() / 1000)): string {
  return `t=${t},v1=${createHmac('sha256', secret).update(`${t}.${body}`).digest('hex')}`;
}

export const idem = () => randomUUID();
