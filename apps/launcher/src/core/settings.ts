// Launcher settings and game profiles (stored as JSON in the user data dir).

export interface GameProfile {
  id: string;
  name: string;
  gameVersion: string;
  /** Fabric loader version, or null for vanilla. */
  loader: string | null;
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
}

export const DEFAULT_PROFILE: GameProfile = {
  id: 'lego-default', name: 'LEGO Client', gameVersion: '1.21.11', loader: null, legoClient: true,
  memoryMb: 4096, jvmArgs: '', resolution: null, gameDir: null, createdAt: 0, lastPlayed: null,
};

export function defaultSettings(): Settings {
  return {
    themeId: 'nightfall', customThemes: [], language: 'de', backendUrl: 'https://api.lego-launcher.example',
    selectedProfile: DEFAULT_PROFILE.id, profiles: [{ ...DEFAULT_PROFILE, createdAt: Date.now() }],
    closeOnLaunch: false, reducedMotion: false, uiScale: 1, autoUpdate: 'ask', rarityColors: {},
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
  if (p.loader !== null && p.loader !== undefined && !/^[0-9][0-9a-z.+\-]*$/i.test(p.loader)) e.push('Ungültige Fabric-Version');
  if (!p.memoryMb || p.memoryMb < 1024 || p.memoryMb > Math.max(2048, totalMemMb - 1024)) e.push(`RAM 1024-${Math.max(2048, totalMemMb - 1024)} MB`);
  if (p.jvmArgs && parseJvmArgs(p.jvmArgs).rejected.length) e.push(`Nicht erlaubte JVM-Argumente: ${parseJvmArgs(p.jvmArgs).rejected.join(' ')}`);
  if (p.resolution && (p.resolution.width < 320 || p.resolution.height < 240 || p.resolution.width > 7680 || p.resolution.height > 4320)) e.push('Auflösung 320x240 bis 7680x4320');
  return e;
}
