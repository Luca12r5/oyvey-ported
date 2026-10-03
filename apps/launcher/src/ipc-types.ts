// Contract between renderer (via preload) and main process.

import type { GameProfile, LoaderType, Settings } from './core/settings.ts';
import type { InstalledMod, ProjectType, SearchHit } from './core/modrinth.ts';
import type { FoundInstance } from './core/importers.ts';

export interface AccountInfo { uuid: string; name: string; active: boolean }

export interface AuthState {
  signedIn: boolean;
  name: string | null;
  uuid: string | null;
  skinUrl: string | null;
  accounts: AccountInfo[];
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

export interface SkinData { skin: string | null; variant: 'classic' | 'slim'; cape: string | null }

export interface ModEntry extends Partial<InstalledMod> {
  filename: string;
  enabled: boolean;
  managedByLauncher: boolean;
}

export interface LauncherApi {
  info(): Promise<AppInfo>;
  window(action: 'minimize' | 'maximize' | 'close'): void;
  getSettings(): Promise<Settings>;
  setSettings(patch: Partial<Settings>): Promise<Settings>;
  saveProfile(p: GameProfile): Promise<{ ok: boolean; errors: string[]; settings: Settings }>;
  deleteProfile(id: string): Promise<Settings>;
  authState(): Promise<AuthState>;
  /** Opens the Microsoft sign-in window; the result arrives as an 'auth' event. */
  signIn(): Promise<{ window: true }>;
  /** Fallback: device code to enter on microsoft.com/link. */
  signInWithCode(): Promise<DeviceCodeInfo>;
  cancelSignIn(): void;
  signOut(uuid?: string): Promise<AuthState>;
  switchAccount(uuid: string): Promise<AuthState>;
  reconnectLego(): Promise<AuthState>;
  skin(uuid?: string): Promise<SkinData>;
  versions(): Promise<{ id: string; type: string; releaseTime: string }[]>;
  loaderVersions(type: LoaderType, gameVersion: string): Promise<{ version: string; stable: boolean }[]>;
  launch(profileId: string): Promise<{ ok: boolean; error?: string }>;
  cancelLaunch(): void;
  gameRunning(): Promise<boolean>;
  openFolder(kind: 'game' | 'logs' | 'mods' | 'screenshots' | 'resourcepacks' | 'shaderpacks', profileId?: string): Promise<void>;
  mods(profileId: string): Promise<ModEntry[]>;
  searchProjects(profileId: string, query: string, type: ProjectType, offset: number): Promise<{ hits: SearchHit[]; total: number }>;
  installProject(profileId: string, projectId: string, type: ProjectType, title: string, iconUrl: string | null): Promise<string[]>;
  removeMod(profileId: string, filename: string): Promise<void>;
  toggleMod(profileId: string, filename: string, enabled: boolean): Promise<void>;
  installModpack(projectId: string, title: string): Promise<{ profileId: string }>;
  importFile(): Promise<{ profileId: string; missing: string[] } | null>;
  scanInstances(): Promise<FoundInstance[]>;
  pickFolder(): Promise<FoundInstance | null>;
  importInstance(inst: FoundInstance, includeWorlds: boolean): Promise<{ profileId: string; copied: string[] }>;
  officialStatus(): Promise<{ present: boolean; dir: string }>;
  exportToOfficial(profileId: string, icon: string | null): Promise<{ versionId: string; name: string }>;
  lego<T = unknown>(method: string, path: string, body?: unknown): Promise<T>;
  openExternal(url: string): Promise<void>;
  diagnostics(): Promise<string>;
  checkUpdate(): Promise<{ available: boolean; version?: string; error?: string }>;
  installUpdate(): Promise<void>;
  on(channel: 'auth' | 'launch-progress' | 'game-log' | 'game-exit' | 'lego-event' | 'update', cb: (data: any) => void): () => void;
}
