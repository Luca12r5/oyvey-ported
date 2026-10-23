// Persistent storage in the user data directory. Secrets (Microsoft refresh
// token, Minecraft and LEGO tokens) are encrypted with Electron safeStorage,
// which uses DPAPI on Windows and the OS keychain elsewhere. If encryption is
// unavailable, secrets are kept in memory only and never written in clear text.

import { app, safeStorage } from 'electron';
import { mkdir, readFile, rename, rm, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { defaultSettings, migrateProfile, migrateSettings, type Settings } from '../core/settings.ts';
import { sanitizeFx, sanitizeLook, sanitizeImage } from '../core/design.ts';
import type { MinecraftSession } from '../core/msauth.ts';

export interface Account {
  msRefreshToken: string;
  mc: MinecraftSession;
  lego: { token: string; expiresAt: number; userId: string; roles: string[] } | null;
  /** Azure app id used for this account's sign-in (refresh tokens are bound to it). */
  clientId?: string;
}

const dir = () => app.getPath('userData');
const settingsFile = () => join(dir(), 'settings.json');
const accountFile = () => join(dir(), 'account.bin');

async function atomicWrite(file: string, data: string | Buffer): Promise<void> {
  await mkdir(dir(), { recursive: true });
  await writeFile(`${file}.tmp`, data, { mode: 0o600 });
  await rename(`${file}.tmp`, file);
}

export async function loadSettings(): Promise<Settings> {
  try {
    const s = migrateSettings(JSON.parse(await readFile(settingsFile(), 'utf8')) as Partial<Settings>);
    const d = defaultSettings();
    return { ...d, ...s, fx: sanitizeFx(s.fx), bgImage: sanitizeImage(s.bgImage), look: sanitizeLook(s.look), profiles: s.profiles?.length ? s.profiles.map((p) => migrateProfile(p)) : d.profiles };
  } catch {
    return defaultSettings();
  }
}

export async function saveSettings(s: Settings): Promise<void> {
  await atomicWrite(settingsFile(), JSON.stringify(s, null, 2));
}

export interface AccountStore {
  accounts: Account[];
  /** Minecraft uuid of the active account. */
  active: string | null;
}

let memory: AccountStore = { accounts: [], active: null };
let loaded = false;

export function encryptionAvailable(): boolean {
  return safeStorage.isEncryptionAvailable();
}

/** Loads all accounts (older single-account files are migrated). */
export async function loadAccounts(): Promise<AccountStore> {
  if (loaded) return memory;
  loaded = true;
  if (!encryptionAvailable()) return memory;
  try {
    const buf = await readFile(accountFile());
    if (buf.length === 0) return memory;
    const data = JSON.parse(safeStorage.decryptString(buf)) as AccountStore | Account;
    memory = 'accounts' in data ? data : { accounts: [data], active: data.mc.uuid };
  } catch {
    memory = { accounts: [], active: null };
  }
  return memory;
}

export async function saveAccounts(store: AccountStore): Promise<void> {
  memory = store;
  loaded = true;
  if (!encryptionAvailable()) return;
  await atomicWrite(accountFile(), safeStorage.encryptString(JSON.stringify(store)));
}

export const paths = {
  game: () => join(dir(), 'minecraft'),
  logs: () => join(dir(), 'logs'),
};

// ---- the player's own Claude API key (crash doctor AI) ----------------------
// Entered by the player in Options → AI; never shipped with the launcher and never sent to
// the page. Stored only encrypted with safeStorage; without OS encryption it lives in memory.
const aiKeyFile = () => join(app.getPath('userData'), 'ai-key.bin');
let aiKeyMemory: string | null = null;

export async function loadAiKey(): Promise<string | null> {
  if (aiKeyMemory) return aiKeyMemory;
  if (!encryptionAvailable()) return null;
  try {
    aiKeyMemory = safeStorage.decryptString(await readFile(aiKeyFile())) || null;
  } catch {
    aiKeyMemory = null;
  }
  return aiKeyMemory;
}

export async function saveAiKey(key: string | null): Promise<void> {
  aiKeyMemory = key;
  if (!key) { await rm(aiKeyFile(), { force: true }); return; }
  if (encryptionAvailable()) await atomicWrite(aiKeyFile(), safeStorage.encryptString(key));
}
