// Launcher settings and game profiles (stored as JSON in the user data dir).

export const LOADER_TYPES = ['fabric', 'quilt', 'forge', 'neoforge'] as const;
export type LoaderType = (typeof LOADER_TYPES)[number];

/** Whether the loader exists for this game version at all (cheap check, no network). */
export function loaderSupports(type: LoaderType, gameVersion: string): boolean {
  const m = /^1\.(\d+)(?:\.(\d+))?/.exec(gameVersion);
  const minor = m ? Number(m[1]) : 99;
  const patch = m?.[2] ? Number(m[2]) : 0;
  // Snapshots like "25w14a" are newer than every 1.x release.
  if (!m && !/^\d\dw\d\d[a-z]$/.test(gameVersion)) return false;
  if (type === 'fabric' || type === 'quilt') return minor >= 14;
  if (type === 'forge') return minor >= 13;
  // NeoForge publishes its own artifact from 1.20.2 on.
  if (type === 'neoforge') return minor > 20 || (minor === 20 && patch >= 2);
  return false;
}

export interface GameProfile {
  id: string;
  name: string;
  gameVersion: string;
  /** Mod loader type, or null for vanilla. */
  loaderType: LoaderType | null;
  /** Loader version; null = newest stable for the game version. */
  loader: string | null;
  /** Install the performance mods (Sodium, Lithium, FerriteCore, ImmediatelyFast, EntityCulling) where available. */
  performancePack: boolean;
  /** Profile icon id from the launcher's icon set. */
  icon: string;
  /** Install the LEGO client + Fabric API into this profile. */
  legoClient: boolean;
  memoryMb: number;
  jvmArgs: string;
  resolution: { width: number; height: number } | null;
  /** Custom game directory; default is instances/<id>. */
  gameDir: string | null;
  createdAt: number;
  lastPlayed: number | null;
}

export interface Settings {
  themeId: string;
  customThemes: unknown[];
  language: 'de' | 'en';
  backendUrl: string;
  selectedProfile: string;
  profiles: GameProfile[];
  closeOnLaunch: boolean;
  reducedMotion: boolean;
  uiScale: number;
  autoUpdate: 'ask' | 'auto' | 'off';
  rarityColors: Record<string, string>;
  /** Overrides the theme accent (null = theme colour). */
  accentColor: string | null;
  /** Animated launcher background id and quality 0..1. */
  background: string;
  backgroundQuality: number;
  snow: boolean;
  /** Show snapshots and historical versions in version pickers. */
  showSnapshots: boolean;
  showHistorical: boolean;
  /** Optional CurseForge API key (needed only to import CurseForge modpacks). */
  curseforgeKey: string;
  /** Azure app (client) id for Microsoft sign-in; empty = the one built into the launcher. */
  msClientId: string;
}

export const DEFAULT_PROFILE: GameProfile = {
  id: 'lego-default', name: 'LEGO Client', gameVersion: '1.21.11', loaderType: 'fabric', loader: null, performancePack: true, icon: 'brick', legoClient: true,
  memoryMb: 4096, jvmArgs: '', resolution: null, gameDir: null, createdAt: 0, lastPlayed: null,
};

/** The LEGO client is built for exactly this game version and loader. */
export const LEGO_CLIENT_GAME_VERSION = '1.21.11';

/** Brings profiles saved by older launcher versions to the current shape. */
export function migrateProfile(p: Partial<GameProfile> & { loader?: string | null }): GameProfile {
  const loaderType = p.loaderType !== undefined ? p.loaderType : p.loader || p.legoClient ? 'fabric' : null;
  return {
    ...DEFAULT_PROFILE,
    ...p,
    loaderType: (LOADER_TYPES as readonly string[]).includes(String(loaderType)) ? (loaderType as LoaderType) : null,
    loader: p.loader ?? null,
    performancePack: p.performancePack ?? false,
    icon: p.icon ?? 'grass',
    legoClient: !!p.legoClient && (p.gameVersion ?? DEFAULT_PROFILE.gameVersion) === LEGO_CLIENT_GAME_VERSION,
  } as GameProfile;
}

export function defaultSettings(): Settings {
  return {
    themeId: 'lego-graphite', customThemes: [], language: 'de', backendUrl: 'https://api.lego-launcher.example',
    selectedProfile: DEFAULT_PROFILE.id, profiles: [{ ...DEFAULT_PROFILE, createdAt: Date.now() }],
    closeOnLaunch: false, reducedMotion: false, uiScale: 1, autoUpdate: 'ask', rarityColors: {},
    accentColor: null, background: 'nebula', backgroundQuality: 0.6, snow: false, showSnapshots: false, showHistorical: false, curseforgeKey: '', msClientId: '',
  };
}

const SAFE_JVM_ARG = /^-(X[a-zA-Z]+[0-9a-zA-Z:+\-=.]*|XX:[+\-]?[A-Za-z0-9]+(=[A-Za-z0-9.:_\-]+)?|D[A-Za-z0-9._\-]+=[^\s"'`;&|<>$]*)$/;

/** Splits user JVM arguments and rejects anything that is not a -X/-XX/-D flag. */
export function parseJvmArgs(raw: string): { args: string[]; rejected: string[] } {
  const parts = raw.trim() ? raw.trim().split(/\s+/) : [];
  const args: string[] = [];
  const rejected: string[] = [];
  for (const p of parts) {
    // Memory is managed by the profile slider.
    if (/^-Xm[sx]/.test(p)) { rejected.push(p); continue; }
    (SAFE_JVM_ARG.test(p) ? args : rejected).push(p);
  }
  return { args, rejected };
}

export function validateProfile(p: Partial<GameProfile>, totalMemMb: number): string[] {
  const e: string[] = [];
  if (!p.name || p.name.length > 40) e.push('Name 1-40 Zeichen');
  if (!p.gameVersion || !/^[0-9a-z.\-_ ]{2,32}$/i.test(p.gameVersion)) e.push('Ungültige Minecraft-Version');
  if (p.loader !== null && p.loader !== undefined && !/^[0-9][0-9a-z.+\-]*$/i.test(p.loader)) e.push('Ungültige Loader-Version');
  if (p.loaderType !== null && p.loaderType !== undefined && !(LOADER_TYPES as readonly string[]).includes(p.loaderType)) e.push('Ungültiger Mod-Loader');
  if (p.legoClient && (p.gameVersion !== LEGO_CLIENT_GAME_VERSION || p.loaderType !== 'fabric')) e.push(`Der LEGO Client gibt es nur für Fabric ${LEGO_CLIENT_GAME_VERSION}`);
  if (!p.memoryMb || p.memoryMb < 1024 || p.memoryMb > Math.max(2048, totalMemMb - 1024)) e.push(`RAM 1024-${Math.max(2048, totalMemMb - 1024)} MB`);
  if (p.jvmArgs && parseJvmArgs(p.jvmArgs).rejected.length) e.push(`Nicht erlaubte JVM-Argumente: ${parseJvmArgs(p.jvmArgs).rejected.join(' ')}`);
  if (p.resolution && (p.resolution.width < 320 || p.resolution.height < 240 || p.resolution.width > 7680 || p.resolution.height > 4320)) e.push('Auflösung 320x240 bis 7680x4320');
  return e;
}
