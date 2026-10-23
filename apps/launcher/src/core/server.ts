// A Minecraft server on this PC, made and started by the launcher: vanilla (Mojang's
// server.jar, checked by sha1) or Fabric (Fabric's server launcher). The EULA is only written
// after the player agreed in the launcher. Online mode stays on (real accounts only).

export interface LocalServer {
  id: string;
  name: string;
  gameVersion: string;
  loader: 'vanilla' | 'fabric';
  memoryMb: number;
  port: number;
  motd: string;
  maxPlayers: number;
  gamemode: 'survival' | 'creative' | 'adventure';
  difficulty: 'peaceful' | 'easy' | 'normal' | 'hard';
  pvp: boolean;
  eulaAccepted: boolean;
  createdAt: number;
}

export const SERVER_DEFAULT: Omit<LocalServer, 'id' | 'createdAt'> = {
  name: 'Mein Server', gameVersion: '26.3', loader: 'vanilla', memoryMb: 2048, port: 25565, motd: 'LEGO Server', maxPlayers: 10,
  gamemode: 'survival', difficulty: 'normal', pvp: true, eulaAccepted: false,
};

const clamp = (n: unknown, lo: number, hi: number, d: number) => { const x = Math.round(Number(n)); return Number.isFinite(x) ? Math.max(lo, Math.min(hi, x)) : d; };
const pick = <T extends string>(v: unknown, list: readonly T[], d: T): T => (list.includes(v as T) ? (v as T) : d);
const text = (v: unknown, max: number, d: string) => (typeof v === 'string' ? v.replace(/[\u0000-\u001f\u007f§]/g, '').trim().slice(0, max) : '') || d;

/** Cleans a server definition from the page (never trust the renderer). */
export function sanitizeServer(raw: unknown, base: LocalServer): LocalServer {
  const r = (raw && typeof raw === 'object' ? raw : {}) as Record<string, unknown>;
  return {
    ...base,
    name: text(r.name, 40, base.name),
    gameVersion: typeof r.gameVersion === 'string' && /^[\w.\-]{1,24}$/.test(r.gameVersion) ? r.gameVersion : base.gameVersion,
    loader: pick(r.loader, ['vanilla', 'fabric'] as const, base.loader),
    memoryMb: clamp(r.memoryMb, 1024, 32768, base.memoryMb),
    port: clamp(r.port, 1025, 65535, base.port),
    motd: text(r.motd, 59, base.motd),
    maxPlayers: clamp(r.maxPlayers, 1, 200, base.maxPlayers),
    gamemode: pick(r.gamemode, ['survival', 'creative', 'adventure'] as const, base.gamemode),
    difficulty: pick(r.difficulty, ['peaceful', 'easy', 'normal', 'hard'] as const, base.difficulty),
    pvp: typeof r.pvp === 'boolean' ? r.pvp : base.pvp,
    eulaAccepted: r.eulaAccepted === true || base.eulaAccepted,
  };
}

/**
 * server.properties: the launcher's values are set, everything else the player (or the server)
 * wrote stays as it is. Online mode is always on.
 */
export function serverProperties(existing: string, s: LocalServer): string {
  const want: Record<string, string> = {
    'server-port': String(s.port), motd: s.motd, 'max-players': String(s.maxPlayers), gamemode: s.gamemode,
    difficulty: s.difficulty, pvp: String(s.pvp), 'online-mode': 'true', 'enforce-secure-profile': 'true',
  };
  const lines = existing.split(/\r?\n/).filter((l) => l.length);
  const seen = new Set<string>();
  const out = lines.map((l) => {
    const m = /^([\w.-]+)=(.*)$/.exec(l);
    if (!m || !(m[1]! in want)) return l;
    seen.add(m[1]!);
    return `${m[1]}=${want[m[1]!]}`;
  });
  for (const [k, v] of Object.entries(want)) if (!seen.has(k)) out.push(`${k}=${v}`);
  return `${out.join('\n')}\n`;
}

export const EULA_URL = 'https://aka.ms/MinecraftEULA';
export function eulaFile(now = new Date()): string {
  return `# Accepted in the LEGO Launcher (${EULA_URL}) on ${now.toISOString()}\neula=true\n`;
}

/** JVM + server arguments. */
export function serverArgs(s: LocalServer, jar: string): string[] {
  return [`-Xms${Math.min(1024, s.memoryMb)}M`, `-Xmx${s.memoryMb}M`, '-XX:+UseG1GC', '-jar', jar, 'nogui'];
}

/** A console command the player may send (one line, printable). */
export function validCommand(c: unknown): c is string {
  return typeof c === 'string' && c.length > 0 && c.length <= 256 && !/[\u0000-\u001f\u007f]/.test(c);
}
