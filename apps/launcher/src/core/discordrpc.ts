// Discord Rich Presence over Discord's local IPC socket (no Discord library, no network):
// frames are [opcode u32 LE][length u32 LE][JSON]. The application id is the player's own
// Discord app (discord.com/developers), with an art asset named "lego" for the icon.

export const OP = { HANDSHAKE: 0, FRAME: 1, CLOSE: 2, PING: 3, PONG: 4 } as const;

export function encodeFrame(op: number, payload: unknown): Buffer {
  const json = Buffer.from(JSON.stringify(payload), 'utf8');
  const head = Buffer.alloc(8);
  head.writeUInt32LE(op, 0);
  head.writeUInt32LE(json.length, 4);
  return Buffer.concat([head, json]);
}

/** Splits a stream buffer into complete frames; returns the rest that is still incomplete. */
export function decodeFrames(buf: Buffer): { frames: { op: number; data: unknown }[]; rest: Buffer } {
  const frames: { op: number; data: unknown }[] = [];
  let off = 0;
  while (buf.length - off >= 8) {
    const op = buf.readUInt32LE(off);
    const len = buf.readUInt32LE(off + 4);
    if (len > 1 << 20) throw new Error('frame too large');
    if (buf.length - off - 8 < len) break;
    const raw = buf.subarray(off + 8, off + 8 + len).toString('utf8');
    let data: unknown = null;
    try { data = JSON.parse(raw); } catch { /* ignore broken frame */ }
    frames.push({ op, data });
    off += 8 + len;
  }
  return { frames, rest: buf.subarray(off) };
}

/** Discord application ids are snowflakes (17–20 digits). */
export function validAppId(id: unknown): id is string {
  return typeof id === 'string' && /^\d{17,20}$/.test(id.trim());
}

/** Socket paths Discord listens on (stable, PTB, Canary, Flatpak/Snap on Linux). */
export function ipcPaths(platform: NodeJS.Platform, env: NodeJS.ProcessEnv): string[] {
  const out: string[] = [];
  for (let i = 0; i < 10; i++) {
    if (platform === 'win32') out.push(`\\\\?\\pipe\\discord-ipc-${i}`);
    else {
      const base = (env.XDG_RUNTIME_DIR || env.TMPDIR || env.TMP || env.TEMP || '/tmp').replace(/\/$/, '');
      out.push(`${base}/discord-ipc-${i}`, `${base}/app/com.discordapp.Discord/discord-ipc-${i}`, `${base}/snap.discord/discord-ipc-${i}`);
    }
  }
  return out;
}

export interface Presence { details: string; state?: string; startedAt?: number }

export function activityPayload(pid: number, p: Presence | null, nonce: string): unknown {
  return {
    cmd: 'SET_ACTIVITY',
    nonce,
    args: {
      pid,
      activity: p ? {
        details: p.details.slice(0, 128),
        ...(p.state ? { state: p.state.slice(0, 128) } : {}),
        ...(p.startedAt ? { timestamps: { start: Math.floor(p.startedAt / 1000) } } : {}),
        assets: { large_image: 'lego', large_text: 'LEGO Launcher' },
        instance: false,
      } : null,
    },
  };
}
