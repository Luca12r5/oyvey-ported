// Server-Sent Events hub + in-memory presence. SSE needs no extra protocol
// library and works in browsers, Electron and Java's HttpClient alike.

import type { ServerResponse } from 'node:http';

export type PresenceStatus = 'online' | 'in_game' | 'away' | 'offline';

export interface Presence {
  status: PresenceStatus;
  /** e.g. "1.21.11" */
  gameVersion: string | null;
  /** Free text like "Tennis" or "Singleplayer"; server address only if the user shares it. */
  activity: string | null;
  updatedAt: number;
}

const PRESENCE_TTL = 90_000;

export class EventHub {
  private streams = new Map<string, Set<ServerResponse>>();
  private presence = new Map<string, Presence>();
  private timer: NodeJS.Timeout | null = null;

  attach(userId: string, res: ServerResponse): () => void {
    let set = this.streams.get(userId);
    if (!set) {
      set = new Set();
      this.streams.set(userId, set);
    }
    set.add(res);
    this.ensureTimer();
    return () => {
      set!.delete(res);
      if (set!.size === 0) this.streams.delete(userId);
    };
  }

  send(userId: string, type: string, data: unknown): void {
    const set = this.streams.get(userId);
    if (!set) return;
    const frame = `event: ${type}\ndata: ${JSON.stringify(data)}\n\n`;
    for (const res of set) res.write(frame);
  }

  sendMany(userIds: Iterable<string>, type: string, data: unknown): void {
    for (const id of userIds) this.send(id, type, data);
  }

  setPresence(userId: string, p: Omit<Presence, 'updatedAt'>): void {
    this.presence.set(userId, { ...p, updatedAt: Date.now() });
  }

  getPresence(userId: string, now = Date.now()): Presence {
    const p = this.presence.get(userId);
    if (p && now - p.updatedAt < PRESENCE_TTL) return p;
    if (this.streams.has(userId)) return { status: 'online', gameVersion: null, activity: null, updatedAt: now };
    return { status: 'offline', gameVersion: null, activity: null, updatedAt: p?.updatedAt ?? 0 };
  }

  connected(userId: string): boolean {
    return this.streams.has(userId);
  }

  private ensureTimer(): void {
    if (this.timer) return;
    this.timer = setInterval(() => {
      for (const set of this.streams.values()) for (const res of set) res.write(': ping\n\n');
      if (this.streams.size === 0 && this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    }, 25_000);
    this.timer.unref();
  }

  close(): void {
    for (const set of this.streams.values()) for (const res of set) res.end();
    this.streams.clear();
    if (this.timer) clearInterval(this.timer);
    this.timer = null;
  }
}
