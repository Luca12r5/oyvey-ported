// Contract between renderer (via preload) and main process.

import type { GameProfile, Settings } from './core/settings.ts';

export interface AuthState {
  signedIn: boolean;
  name: string | null;
  uuid: string | null;
  skinUrl: string | null;
  lego: { connected: boolean; userId: string | null; roles: string[]; error: string | null };
  secureStorage: boolean;
}

export interface DeviceCodeInfo { userCode: string; verificationUri: string; expiresAt: number }

export interface LaunchProgress { step: string; done?: number; total?: number; bytes?: number; totalBytes?: number }

export interface AppInfo {
  version: string;
  platform: string;
  arch: string;
  totalMemMb: number;
  freeMemMb: number;
  cpus: number;
  cpuModel: string;
  msClientConfigured: boolean;
  gameDir: string;
}

export interface LauncherApi {
  info(): Promise<AppInfo>;
  window(action: 'minimize' | 'maximize' | 'close'): void;
  getSettings(): Promise<Settings>;
  setSettings(patch: Partial<Settings>): Promise<Settings>;
  saveProfile(p: GameProfile): Promise<{ ok: boolean; errors: string[]; settings: Settings }>;
  deleteProfile(id: string): Promise<Settings>;
  authState(): Promise<AuthState>;
  signIn(): Promise<DeviceCodeInfo>;
  cancelSignIn(): void;
  signOut(): Promise<AuthState>;
  reconnectLego(): Promise<AuthState>;
  versions(): Promise<{ id: string; type: string; releaseTime: string }[]>;
  fabricLoaders(gameVersion: string): Promise<{ version: string; stable: boolean }[]>;
  launch(profileId: string): Promise<{ ok: boolean; error?: string }>;
  cancelLaunch(): void;
  gameRunning(): Promise<boolean>;
  openFolder(kind: 'game' | 'logs' | 'mods' | 'screenshots', profileId?: string): Promise<void>;
  lego<T = unknown>(method: string, path: string, body?: unknown): Promise<T>;
  openExternal(url: string): Promise<void>;
  diagnostics(): Promise<string>;
  checkUpdate(): Promise<{ available: boolean; version?: string; error?: string }>;
  installUpdate(): Promise<void>;
  on(channel: 'auth' | 'launch-progress' | 'game-log' | 'game-exit' | 'lego-event' | 'update', cb: (data: any) => void): () => void;
}
