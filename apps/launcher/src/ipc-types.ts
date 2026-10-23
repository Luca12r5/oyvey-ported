// Contract between renderer (via preload) and main process.

import type { GameProfile, LoaderType, Settings } from './core/settings.ts';
import type { InstalledMod, ProjectType, SearchHit } from './core/modrinth.ts';
import type { Diagnosis, Fix } from './core/crashdoc.ts';
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
  msClientConfigured: boolean; msClientBuiltIn: boolean;
  gameDir: string;
  e2e?: boolean;
  /** e2e: show the first-start welcome anyway. */
  e2eWelcome?: boolean;
}

export interface VrbState { url: string; title: string; canBack: boolean; canForward: boolean; loading: boolean; zoom?: number; tabs?: { title: string; url: string }[]; active?: number }

export interface SkinData { skin: string | null; variant: 'classic' | 'slim'; cape: string | null }

export interface ModEntry extends Partial<InstalledMod> {
  filename: string;
  enabled: boolean;
  managedByLauncher: boolean;
}

export interface LauncherApi {
  info(): Promise<AppInfo>;
  window(action: 'minimize' | 'maximize' | 'fullscreen' | 'reset' | 'close'): void;
  windowState(): Promise<{ maximized: boolean; fullscreen: boolean }>;
  /** VR lobby browser: frames arrive on 'vrb-frame' (JPEG bytes), state on 'vrb-state'. */
  vrBrowserOpen(): Promise<{ w: number; h: number; state: VrbState }>;
  vrBrowserInput(e: unknown): void;
  vrBrowserNav(action: 'back' | 'forward' | 'reload' | 'home' | 'go' | 'newTab' | 'closeTab' | 'tab' | 'zoomIn' | 'zoomOut' | 'zoomReset', text?: string): Promise<VrbState>;
  vrBrowserClose(): void;
  vrBrowserSuggest(q: string): Promise<{ title: string; url: string }[]>;
  /** LEGO client modules of a profile (config/legoclient.json). */
  clientModules(profileId: string): Promise<{ available: boolean; modules: { id: string; on: boolean }[] }>;
  setClientModule(profileId: string, id: string, on: boolean): Promise<{ id: string; on: boolean }[]>;
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
  uploadSkin(dataUrl: string, variant: 'classic' | 'slim'): Promise<SkinData>;
  resetSkin(): Promise<SkinData>;
  vivecraft(gameVersion?: string): Promise<{ available: boolean; gameVersion: string; loader: string | null; suggestion?: string | null }>;
  vivecraftProfile(gameVersion: string, loader: string): Promise<{ profileId: string }>;
  vulkan(gameVersion?: string): Promise<{ available: boolean; gameVersion: string; suggestion?: string | null }>;
  vulkanProfile(gameVersion: string): Promise<{ profileId: string; mods: string[] }>;
  crashLast(): Promise<{ profileId: string; diagnoses: Diagnosis[] } | null>;
  crashAnalyze(profileId: string): Promise<{ profileId: string; diagnoses: Diagnosis[] }>;
  crashFix(profileId: string, fix: Fix | { kind: 'enable-mod'; filename: string }): Promise<{ undo: Fix | { kind: 'enable-mod'; filename: string } | null; message: string }>;
  versions(): Promise<{ id: string; type: string; releaseTime: string }[]>;
  loaderVersions(type: LoaderType, gameVersion: string): Promise<{ version: string; stable: boolean }[]>;
  launch(profileId: string): Promise<{ ok: boolean; error?: string }>;
  cancelLaunch(): void;
  gameRunning(): Promise<boolean>;
  openFolder(kind: 'game' | 'logs' | 'mods' | 'screenshots' | 'resourcepacks' | 'shaderpacks', profileId?: string): Promise<void>;
  mods(profileId: string): Promise<ModEntry[]>;
  searchProjects(profileId: string, query: string, type: ProjectType, offset: number, filters?: import('./core/modrinth.ts').SearchFilters): Promise<{ hits: SearchHit[]; total: number }>;
  installProject(profileId: string, projectId: string, type: ProjectType, title: string, iconUrl: string | null): Promise<string[]>;
  removeMod(profileId: string, filename: string): Promise<void>;
  toggleMod(profileId: string, filename: string, enabled: boolean): Promise<void>;
  installModpack(projectId: string, title: string): Promise<{ profileId: string }>;
  importFile(): Promise<{ profileId: string; missing: string[] } | null>;
  /** A file dropped onto the window (bytes read in the renderer). */
  importDrop(name: string, data: Uint8Array): Promise<{ kind: string; profileId: string; profileName?: string; file?: string; missing?: string[] }>;
  scanInstances(): Promise<FoundInstance[]>;
  pickFolder(): Promise<FoundInstance | null>;
  importInstance(inst: FoundInstance, includeWorlds: boolean): Promise<{ profileId: string; copied: string[] }>;
  officialStatus(): Promise<{ present: boolean; dir: string }>;
  exportToOfficial(profileId: string, icon: string | null): Promise<{ versionId: string; name: string }>;
  openOfficialLauncher(): Promise<boolean>;
  lego<T = unknown>(method: string, path: string, body?: unknown): Promise<T>;
  openExternal(url: string): Promise<void>;
  diagnostics(): Promise<string>;
  checkUpdate(): Promise<{ available: boolean; version?: string; error?: string }>;
  installUpdate(): Promise<void>;
  on(channel: 'auth' | 'launch-progress' | 'game-log' | 'game-exit' | 'lego-event' | 'update' | 'window-state' | 'vrb-frame' | 'vrb-state', cb: (data: any) => void): () => void;
}
