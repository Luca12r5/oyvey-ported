// Persistent storage in the user data directory. Secrets (Microsoft refresh
// token, Minecraft and LEGO tokens) are encrypted with Electron safeStorage,
// which uses DPAPI on Windows and the OS keychain elsewhere. If encryption is
// unavailable, secrets are kept in memory only and never written in clear text.

import { app, safeStorage } from 'electron';
import { mkdir, readFile, rename, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { defaultSettings, type Settings } from '../core/settings.ts';
import type { MinecraftSession } from '../core/msauth.ts';

export interface Account {
  msRefreshToken: string;
  mc: MinecraftSession;
  lego: { token: string; expiresAt: number; userId: string; roles: string[] } | null;
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
    const s = JSON.parse(await readFile(settingsFile(), 'utf8')) as Partial<Settings>;
    const d = defaultSettings();
    return { ...d, ...s, profiles: s.profiles?.length ? s.profiles : d.profiles };
  } catch {
    return defaultSettings();
  }
}

export async function saveSettings(s: Settings): Promise<void> {
  await atomicWrite(settingsFile(), JSON.stringify(s, null, 2));
}

let memoryAccount: Account | null = null;

export function encryptionAvailable(): boolean {
  return safeStorage.isEncryptionAvailable();
}

export async function loadAccount(): Promise<Account | null> {
  if (memoryAccount) return memoryAccount;
  if (!encryptionAvailable()) return null;
  try {
    const buf = await readFile(accountFile());
    memoryAccount = JSON.parse(safeStorage.decryptString(buf)) as Account;
    return memoryAccount;
  } catch {
    return null;
  }
}

export async function saveAccount(a: Account | null): Promise<void> {
  memoryAccount = a;
  if (!encryptionAvailable()) return;
  if (!a) {
    await atomicWrite(accountFile(), Buffer.alloc(0));
    return;
  }
  await atomicWrite(accountFile(), safeStorage.encryptString(JSON.stringify(a)));
}

export const paths = {
  game: () => join(dir(), 'minecraft'),
  logs: () => join(dir(), 'logs'),
};
