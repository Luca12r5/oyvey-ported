// Crash doctor with AI: the launcher sends a cleaned-up log excerpt and the mod list when the
// player clicks "Mit KI prüfen". Nothing is stored; at most CRASH_AI_PER_DAY checks per player.

import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import { HttpError } from '../http.ts';
import { obj, str } from '../security.ts';
import type { CrashMod } from '../services/crashai.ts';

export const CRASH_AI_PER_DAY = 20;
const used = new Map<string, number>();

export function registerCrash(app: App): void {
  app.router.get('/api/crash/ai', async (_ctx: AppCtx) => ({ ai: app.crashAi !== null, perDay: CRASH_AI_PER_DAY }));

  app.router.post('/api/crash/ai', async (ctx: AppCtx) => {
    const u = requireUser(ctx);
    if (!app.crashAi) throw new HttpError(503, 'ai_unavailable', 'The crash AI is not configured on this server');
    const b = obj(ctx.body);
    const log = str(b, 'log', 20, 60_000);
    const modsRaw = Array.isArray(b.mods) ? b.mods.slice(0, 400) : [];
    const mods: CrashMod[] = modsRaw.flatMap((m) => {
      const o = (m && typeof m === 'object' ? m : {}) as Record<string, unknown>;
      const filename = typeof o.filename === 'string' && /^[^/\\]{1,200}$/.test(o.filename) ? o.filename : null;
      if (!filename) return [];
      const s = (v: unknown) => (typeof v === 'string' ? v.slice(0, 80) : null);
      return [{ filename, id: s(o.id), title: s(o.title), enabled: o.enabled !== false }];
    });
    const gameVersion = typeof b.gameVersion === 'string' && /^[\w.\- ]{1,24}$/.test(b.gameVersion) ? b.gameVersion : 'unknown';
    const loader = typeof b.loader === 'string' && /^[a-z]{1,12}$/.test(b.loader) ? b.loader : null;
    const memoryMb = Math.max(512, Math.min(65536, Math.round(Number(b.memoryMb) || 4096)));
    const day = new Date().toISOString().slice(0, 10);
    const key = `${u.id}:${day}`;
    const n = used.get(key) ?? 0;
    if (n >= CRASH_AI_PER_DAY) throw new HttpError(429, 'daily_limit', `At most ${CRASH_AI_PER_DAY} AI crash checks per day`);
    used.set(key, n + 1);
    if (used.size > 50_000) for (const k of used.keys()) if (!k.endsWith(day)) used.delete(k);
    try {
      const r = await app.crashAi({ log, mods, gameVersion, loader, memoryMb, legoClient: b.legoClient === true });
      return { ...r, left: CRASH_AI_PER_DAY - n - 1 };
    } catch {
      throw new HttpError(502, 'ai_failed', 'The AI could not read this log, try again later');
    }
  }, { rate: ['crashai', 6], bodyLimit: 200_000 });
}
