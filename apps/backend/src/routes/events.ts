// GET /api/events: Server-Sent Events stream for the signed-in user
// (messages, friend requests, party updates, presence, purchases...).

import type { App, AppCtx } from '../app.ts';
import { requireUser } from '../app.ts';

const MAX_STREAMS_PER_USER = 5;

export function registerEvents(app: App): void {
  const streams = new Map<string, number>();

  app.router.get('/api/events', (ctx: AppCtx) => {
    const u = requireUser(ctx);
    const open = streams.get(u.id) ?? 0;
    if (open >= MAX_STREAMS_PER_USER) {
      ctx.status = 429;
      return { error: 'too_many_streams' };
    }
    const res = ctx.res;
    res.writeHead(200, {
      'Content-Type': 'text/event-stream; charset=utf-8',
      'Cache-Control': 'no-store',
      Connection: 'keep-alive',
      'X-Accel-Buffering': 'no',
    });
    res.write(`event: hello\ndata: ${JSON.stringify({ userId: u.id })}\n\n`);
    streams.set(u.id, open + 1);
    const detach = app.events.attach(u.id, res);
    ctx.req.on('close', () => {
      detach();
      const n = (streams.get(u.id) ?? 1) - 1;
      if (n <= 0) streams.delete(u.id);
      else streams.set(u.id, n);
    });
    return undefined;
  });
}
