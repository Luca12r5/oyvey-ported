// Client for the LEGO backend. Login uses the Mojang join/hasJoined
// handshake: only Mojang receives the Minecraft token.

import { fetchJson, type FetchLike } from './net.ts';
import { sessionJoin, type MinecraftSession } from './msauth.ts';

export interface LegoUser { id: string; uuid: string; name: string; roles: string[] }

export class LegoApi {
  readonly base: string;
  private f: FetchLike;
  token: string | null;

  constructor(base: string, f: FetchLike, token: string | null = null) {
    this.base = base.replace(/\/$/, '');
    this.f = f;
    this.token = token;
  }

  async login(mc: MinecraftSession): Promise<{ token: string; user: LegoUser; expiresAt: number }> {
    const ch = await this.call<{ challengeId: string; serverId: string }>('POST', '/api/auth/challenge');
    await sessionJoin(this.f, mc, ch.serverId);
    const r = await this.call<{ token: string; user: LegoUser; expiresAt: number }>('POST', '/api/auth/verify', { challengeId: ch.challengeId, username: mc.name });
    this.token = r.token;
    return r;
  }

  call<T>(method: string, path: string, body?: unknown): Promise<T> {
    if (!path.startsWith('/api/')) throw new Error('invalid api path');
    const headers: Record<string, string> = {};
    if (this.token) headers.Authorization = `Bearer ${this.token}`;
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    return fetchJson<T>(this.f, this.base + path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined }, { retries: method === 'GET' ? 1 : 0 });
  }
}
