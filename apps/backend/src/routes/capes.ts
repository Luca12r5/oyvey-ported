// Player-designed capes: the design data (CapeSpec) of the own custom cape and
// the optional AI designer. Specs are validated with the shared sanitizer, so
// everything stored or sent to other clients is safe to render.

import { CUSTOM_CAPE_ID, capeSpecId, sanitizeCapeSpec, type CapeSpec } from '@lego/shared';
import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';
import type { Db } from '../db.ts';
import { HttpError } from '../http.ts';
import { obj, str } from '../security.ts';

export const CAPE_AI_PER_DAY = 10;

export function customCapeOf(db: Db, userId: string): { id: string; spec: CapeSpec } | null {
  const row = db.get<{ spec_json: string; spec_id: string }>('SELECT spec_json, spec_id FROM custom_capes WHERE user_id = :u', { u: userId });
  if (!row) return null;
  try {
    return { id: row.spec_id, spec: sanitizeCapeSpec(JSON.parse(row.spec_json)) };
  } catch {
    return null;
  }
}

/** The custom cape to show to others: only while the player has it equipped. */
export function shownCustomCape(db: Db, userId: string, equipped: Record<string, string>): { id: string; spec: CapeSpec } | null {
  return equipped.cape === CUSTOM_CAPE_ID ? customCapeOf(db, userId) : null;
}

export function registerCapes(app: App): void {
  const r = app.router;
  const db = app.db;

  r.get('/api/me/cape-spec', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    return { cape: customCapeOf(db, u.id), ai: app.capeAi !== null };
  });

  r.put('/api/me/cape-spec', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const spec = sanitizeCapeSpec(obj(ctx.body).spec);
    const id = capeSpecId(spec);
    db.run(
      'INSERT INTO custom_capes(user_id, spec_json, spec_id, updated_at) VALUES (:u, :j, :i, :t) ON CONFLICT(user_id) DO UPDATE SET spec_json = :j, spec_id = :i, updated_at = :t',
      { u: u.id, j: JSON.stringify(spec), i: id, t: Date.now() },
    );
    return { cape: { id, spec } };
  }, { rate: ['capespec', 30] });

  r.post('/api/capes/ai', async (ctx: AppCtx) => {
    const u = requireUser(ctx);
    if (!app.capeAi) throw new HttpError(503, 'ai_unavailable', 'AI cape design is not configured on this server');
    const prompt = cleanPrompt(str(obj(ctx.body), 'prompt', 2, 300));
    const day = new Date().toISOString().slice(0, 10);
    const used = db.get<{ count: number }>('SELECT count FROM cape_ai_usage WHERE user_id = :u AND day = :d', { u: u.id, d: day })?.count ?? 0;
    if (used >= CAPE_AI_PER_DAY) throw new HttpError(429, 'daily_limit', `At most ${CAPE_AI_PER_DAY} AI designs per day`);
    db.run('INSERT INTO cape_ai_usage(user_id, day, count) VALUES (:u, :d, 1) ON CONFLICT(user_id, day) DO UPDATE SET count = count + 1', { u: u.id, d: day });
    try {
      const spec = sanitizeCapeSpec(await app.capeAi(prompt));
      return { spec, left: CAPE_AI_PER_DAY - used - 1 };
    } catch {
      throw new HttpError(502, 'ai_failed', 'The AI could not design this cape, try another description');
    }
  }, { rate: ['capeai', 6] });
}

/** Plain text only, no markup or control characters; text meant for the cape is cleaned again later. */
function cleanPrompt(p: string): string {
  return p.normalize('NFKC').replace(/[\u0000-\u001f\u007f<>]/g, ' ').replace(/\s+/g, ' ').trim();
}
