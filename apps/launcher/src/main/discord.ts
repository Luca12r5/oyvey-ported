// Discord Rich Presence: connects to the local Discord app (if it runs) and shows what the
// player does in the LEGO Launcher. Reconnects quietly; does nothing without an app id.

import { connect, type Socket } from 'node:net';
import { activityPayload, decodeFrames, encodeFrame, ipcPaths, OP, validAppId, type Presence } from '../core/discordrpc.ts';

export class DiscordPresence {
  private sock: Socket | null = null;
  private ready = false;
  private appId: string | null = null;
  private presence: Presence | null = null;
  private retry: ReturnType<typeof setTimeout> | null = null;
  private buf: Buffer = Buffer.alloc(0);
  private nonce = 0;

  /** Sets the app id (null = off). */
  configure(appId: string | null): void {
    const id = appId && validAppId(appId) ? appId.trim() : null;
    if (id === this.appId) return;
    this.close();
    this.appId = id;
    if (id) void this.connect(0);
  }

  set(p: Presence | null): void {
    this.presence = p;
    this.push();
  }

  close(): void {
    if (this.retry) clearTimeout(this.retry);
    this.retry = null;
    this.ready = false;
    try { this.sock?.destroy(); } catch { /* already closed */ }
    this.sock = null;
  }

  private async connect(i: number): Promise<void> {
    const id = this.appId;
    if (!id) return;
    const paths = ipcPaths(process.platform, process.env);
    if (i >= paths.length) { this.later(); return; }
    const s = connect(paths[i]!);
    let opened = false;
    s.once('connect', () => {
      opened = true;
      this.sock = s;
      this.buf = Buffer.alloc(0);
      s.write(encodeFrame(OP.HANDSHAKE, { v: 1, client_id: id }));
    });
    s.on('data', (d: Buffer) => this.onData(d));
    s.on('error', () => { if (!opened) void this.connect(i + 1); });
    s.on('close', () => { if (opened && this.sock === s) { this.sock = null; this.ready = false; this.later(); } });
  }

  private later(): void {
    if (this.retry || !this.appId) return;
    this.retry = setTimeout(() => { this.retry = null; void this.connect(0); }, 30_000);
    this.retry.unref?.();
  }

  private onData(d: Buffer): void {
    try {
      const r = decodeFrames(Buffer.concat([this.buf, d]));
      this.buf = r.rest;
      for (const f of r.frames) {
        if (f.op === OP.PING) this.sock?.write(encodeFrame(OP.PONG, f.data));
        else if (f.op === OP.CLOSE) this.close();
        else if ((f.data as { evt?: string } | null)?.evt === 'READY') { this.ready = true; this.push(); }
      }
    } catch { this.close(); this.later(); }
  }

  private push(): void {
    if (!this.ready || !this.sock) return;
    this.sock.write(encodeFrame(OP.FRAME, activityPayload(process.pid, this.presence, String(++this.nonce))));
  }
}
