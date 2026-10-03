// Electron main process: window, IPC, sign-in (multiple accounts), version and
// loader installation, mods/modpacks/imports and launching.

import { app, BrowserWindow, ipcMain, shell, session, dialog } from 'electron';
import { cpus, freemem, release, totalmem } from 'node:os';
import { join } from 'node:path';
import { mkdir, readFile, readdir, stat } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { createHash, randomUUID } from 'node:crypto';
import { authorizeRequest, exchangeCode, minecraftFromMicrosoft, parseRedirect, pollDeviceCode, refreshMicrosoft, startDeviceCode, AuthError, type MinecraftSession } from '../core/msauth.ts';
import { LegoApi } from '../core/lego.ts';
import { currentEnv, buildArguments, redactArgs } from '../core/mojang.ts';
import {
  getManifest, installJava, installVersion, layout, loaderVersions, resolveLoaderVersion, syncMods, vanillaVersion, type ModFile,
} from '../core/install.ts';
import { loaderSupports } from '../core/loaders.ts';
import {
  installedMods, installMrPack, installProject, packLoader, performanceFiles, projectVersions, readPackIndex, removeMod, search, toggleMod,
  type ProjectType,
} from '../core/modrinth.ts';
import { cfLoader, copyInstance, installCfPack, readCfManifest, readFolder, scanInstances, type FoundInstance } from '../core/importers.ts';
import { listEntries } from '../core/zip.ts';
import { fetchJson } from '../core/net.ts';
import { createLogCleaner, diagnose, startGame } from '../core/launch.ts';
import { DEFAULT_PROFILE, LOADER_TYPES, parseJvmArgs, validateProfile, type GameProfile, type LoaderType, type Settings } from '../core/settings.ts';
import { encryptionAvailable, loadAccounts, loadSettings, paths, saveAccounts, saveSettings, type Account, type AccountStore } from './store.ts';
import { setupUpdater, checkForUpdate, installUpdate } from './updater.ts';
import type { AuthState, LaunchProgress, ModEntry, SkinData } from '../ipc-types.ts';
import { validateTheme } from '@lego/shared';

declare const __MS_CLIENT_ID__: string;
declare const __BACKEND_URL__: string;
declare const __CURSEFORGE_KEY__: string;
const MS_CLIENT_ID = typeof __MS_CLIENT_ID__ === 'string' ? __MS_CLIENT_ID__ : '';
/** Azure app id: from the settings (for self-built setups) or baked into the build. */
const clientId = () => (settings?.msClientId || MS_CLIENT_ID).trim();
const BACKEND_URL = typeof __BACKEND_URL__ === 'string' ? __BACKEND_URL__ : '';
const CURSEFORGE_KEY = typeof __CURSEFORGE_KEY__ === 'string' ? __CURSEFORGE_KEY__ : '';

let win: BrowserWindow | null = null;
let settings: Settings;
let store: AccountStore = { accounts: [], active: null };
let legoError: string | null = null;
let signInAbort: AbortController | null = null;
let loginWin: BrowserWindow | null = null;
let launchAbort: AbortController | null = null;
let running: GameProfile | null = null;
/** Instance folders the user found via scan or picker; only these may be imported. */
const offeredInstances = new Map<string, FoundInstance>();
const env = currentEnv(process.platform, process.arch, release());

function send(channel: string, data: unknown): void {
  win?.webContents.send(`lego:${channel}`, data);
}

function backendUrl(): string {
  return settings.backendUrl && !settings.backendUrl.includes('.example') ? settings.backendUrl : BACKEND_URL;
}

function active(): Account | null {
  return store.accounts.find((a) => a.mc.uuid === store.active) ?? null;
}

/** Replaces (or adds) an account and persists the store. */
async function putAccount(acc: Account, makeActive = false): Promise<void> {
  const others = store.accounts.filter((a) => a.mc.uuid !== acc.mc.uuid);
  store = { accounts: [...others, acc], active: makeActive || !store.active ? acc.mc.uuid : store.active };
  await saveAccounts(store);
}

function legoApi(): LegoApi {
  return new LegoApi(backendUrl(), fetch, active()?.lego?.token ?? null);
}

function authState(): AuthState {
  const a = active();
  return {
    signedIn: !!a,
    name: a?.mc.name ?? null,
    uuid: a?.mc.uuid ?? null,
    skinUrl: a?.mc.skinUrl ?? null,
    accounts: store.accounts.map((x) => ({ uuid: x.mc.uuid, name: x.mc.name, active: x.mc.uuid === store.active })),
    lego: { connected: !!a?.lego, userId: a?.lego?.userId ?? null, roles: a?.lego?.roles ?? [], error: legoError },
    secureStorage: encryptionAvailable(),
  };
}

/** Returns a valid Minecraft session for the active account, refreshing through Microsoft when it expired. */
async function freshSession(): Promise<MinecraftSession> {
  const a = active();
  if (!a) throw new AuthError('signed_out', 'Bitte zuerst mit Microsoft anmelden.');
  if (a.mc.expiresAt - Date.now() > 5 * 60_000) return a.mc;
  // Refresh tokens are bound to the app id that signed the account in.
  const ms = await refreshMicrosoft(fetch, a.clientId ?? clientId(), a.msRefreshToken);
  const mc = await minecraftFromMicrosoft(fetch, ms.accessToken);
  await putAccount({ ...a, msRefreshToken: ms.refreshToken, mc });
  return mc;
}

/** Reuses a stored LEGO session if the server still accepts it, otherwise logs in again. */
async function restoreLego(): Promise<void> {
  const a = active();
  if (a?.lego && a.lego.expiresAt > Date.now() && backendUrl()) {
    try {
      const s = await legoApi().call<{ user: { id: string; roles: string[] } }>('GET', '/api/auth/session');
      await putAccount({ ...a, lego: { ...a.lego, roles: s.user.roles } });
      legoError = null;
      send('auth', authState());
      return;
    } catch {
      await putAccount({ ...a, lego: null });
    }
  }
  await connectLego();
}

async function connectLego(): Promise<void> {
  if (!active() || !backendUrl()) {
    legoError = backendUrl() ? null : 'Kein LEGO-Server konfiguriert.';
    send('auth', authState());
    return;
  }
  try {
    const mc = await freshSession();
    const r = await new LegoApi(backendUrl(), fetch).login(mc);
    await putAccount({ ...active()!, lego: { token: r.token, expiresAt: r.expiresAt, userId: r.user.id, roles: r.user.roles } });
    legoError = null;
  } catch (e) {
    legoError = e instanceof Error ? e.message : String(e);
  }
  send('auth', authState());
}

// ---- presence heartbeat ----------------------------------------------------
setInterval(() => {
  if (!active()?.lego) return;
  void legoApi().call('POST', '/api/me/presence', {
    status: running ? 'in_game' : 'online', gameVersion: running?.gameVersion ?? null, activity: running ? 'Minecraft' : 'Launcher',
  }).catch(() => {});
}, 30_000).unref();

// ---- helpers ---------------------------------------------------------------
function profileById(id: string | undefined): GameProfile {
  const p = settings.profiles.find((x) => x.id === (id ?? settings.selectedProfile));
  if (!p) throw new Error('Profil nicht gefunden.');
  return p;
}

function profileDir(p: GameProfile): string {
  return p.gameDir ?? join(layout(paths.game()).instances, p.id);
}

function defaultMemory(): number {
  const total = Math.round(totalmem() / 1048576);
  return Math.max(1024, Math.min(4096, total - 2048));
}

async function addProfile(name: string, gameVersion: string, loaderType: LoaderType | null, loader: string | null, icon = 'chest'): Promise<GameProfile> {
  const p: GameProfile = {
    ...DEFAULT_PROFILE, id: randomUUID(), name: name.slice(0, 40) || 'Import', gameVersion, loaderType, loader, icon,
    legoClient: false, performancePack: false, memoryMb: defaultMemory(), createdAt: Date.now(), lastPlayed: null,
  };
  settings.profiles.push(p);
  settings.selectedProfile = p.id;
  await saveSettings(settings);
  return p;
}

async function latestRelease(): Promise<string> {
  return (await getManifest(fetch)).latest.release;
}

// ---- launching ---------------------------------------------------------------
/** The LEGO client jar shipped inside the installer (resources/legoclient.jar). */
function bundledLegoClient(): string | null {
  const candidates = [
    app.isPackaged ? join(process.resourcesPath, 'legoclient.jar') : null,
    !app.isPackaged ? process.env.LEGO_CLIENT_JAR ?? null : null,
  ];
  return candidates.find((p): p is string => !!p && existsSync(p)) ?? null;
}

/**
 * LEGO client: a release offered by the LEGO server (allows hotfixes without a
 * launcher update), otherwise the copy shipped with the launcher.
 */
async function legoClientFile(): Promise<ModFile> {
  if (backendUrl()) {
    try {
      const r = await legoApi().call<{ version: string; url: string; filename: string; sha512?: string; sha256?: string } | null>('GET', '/api/public/client-release');
      if (r?.url && /^[\w.+\-]+\.jar$/.test(r.filename) && (r.sha512 || r.sha256)) return { url: r.url, filename: r.filename, sha512: r.sha512, sha256: r.sha256 };
    } catch { /* server offline: use the bundled client */ }
  }
  const local = bundledLegoClient();
  if (!local) throw new Error('Der LEGO Client fehlt in dieser Installation und der LEGO-Server bietet keinen Download an. Bitte den Launcher neu installieren.');
  return { url: '', filename: 'legoclient.jar', localPath: local };
}

async function launch(profileId: string): Promise<{ ok: boolean; error?: string }> {
  if (running) return { ok: false, error: 'Minecraft läuft bereits.' };
  const profile = settings.profiles.find((p) => p.id === profileId);
  if (!profile) return { ok: false, error: 'Profil nicht gefunden.' };
  launchAbort = new AbortController();
  const signal = launchAbort.signal;
  const report = (step: string, p?: { done: number; total: number; bytes: number; totalBytes: number }) => send('launch-progress', { step, ...p } satisfies LaunchProgress);
  const log = (line: string) => send('game-log', { line: `[installer] ${line}`, stream: 'out' });
  try {
    report('Anmeldung prüfen');
    const mc = await freshSession();
    const l = layout(paths.game());
    const gameDir = profileDir(profile);
    await mkdir(gameDir, { recursive: true });

    report('Versionsdaten');
    const base = await vanillaVersion(fetch, l, profile.gameVersion);
    // Old versions without javaVersion run on Java 8 ("jre-legacy").
    const javaPath = await installJava(fetch, l, base.javaVersion?.component ?? 'jre-legacy', env, report, signal);
    if (profile.loaderType && !loaderSupports(profile.loaderType, profile.gameVersion)) {
      throw new Error(`${profile.loaderType} gibt es nicht für Minecraft ${profile.gameVersion}.`);
    }
    if (profile.loaderType === 'forge' || profile.loaderType === 'neoforge') {
      // The official installers need the vanilla files already in place.
      report('Vanilla für den Loader-Installer');
      await installVersion(fetch, l, base, base.id, env, report, signal, gameDir);
      report(`${profile.loaderType} installieren`);
    }
    const merged = await resolveLoaderVersion(fetch, l, base, profile.loaderType, profile.loader, javaPath, log);
    const installed = await installVersion(fetch, l, merged, base.id, env, report, signal, gameDir);

    const managed: ModFile[] = [];
    if (profile.legoClient) {
      report('LEGO Client');
      const api = await projectVersions(fetch, 'fabric-api', profile.gameVersion, 'fabric');
      const v = api.find((x) => x.version_type === 'release') ?? api[0];
      const file = v?.files.find((x) => x.primary) ?? v?.files[0];
      if (!file) throw new Error(`Keine Fabric API für Minecraft ${profile.gameVersion}.`);
      managed.push({ url: file.url, filename: file.filename, sha512: file.hashes.sha512, size: file.size }, await legoClientFile());
    }
    if (profile.performancePack && profile.loaderType) {
      report('FPS-Paket');
      const user = new Set((await installedMods(gameDir)).map((m) => m.projectId));
      for (const p of await performanceFiles(fetch, profile.gameVersion, profile.loaderType)) {
        if (!user.has(p.projectId)) managed.push({ url: p.url, filename: p.filename, sha512: p.sha512, size: p.size });
      }
    }
    if (profile.loaderType || managed.length) await syncMods(fetch, join(gameDir, 'mods'), managed, report, signal);

    const jvm = parseJvmArgs(profile.jvmArgs);
    const args = buildArguments({
      version: merged, env, javaPath, gameDir, assetsDir: l.assets, gameAssetsDir: installed.virtualAssets ?? undefined,
      librariesDir: l.libraries, nativesDir: installed.nativesDir,
      clientJar: installed.clientJar, classpathSeparator: process.platform === 'win32' ? ';' : ':',
      player: { name: mc.name, uuid: mc.uuid, accessToken: mc.accessToken, xuid: mc.xuid, userType: 'msa' },
      launcherName: 'lego-launcher', launcherVersion: app.getVersion(),
      memoryMb: { min: Math.min(1024, profile.memoryMb), max: profile.memoryMb },
      extraJvmArgs: [...jvm.args, ...(active()?.lego ? [`-Dlegoclient.api=${backendUrl()}`] : [])],
      resolution: profile.resolution ?? undefined, loggingConfigPath: installed.loggingConfig ?? undefined,
    });
    report('Starte Minecraft');
    send('game-log', { line: `[launcher] ${javaPath} ${redactArgs(args, mc.accessToken).join(' ')}`, stream: 'out' });
    const clean = createLogCleaner();
    const game = await startGame(javaPath, args, gameDir, paths.logs(), (raw, stream) => {
      const line = clean(raw);
      if (line !== null) send('game-log', { line: line.split(mc.accessToken).join('<redacted>'), stream });
    });
    running = profile;
    profile.lastPlayed = Date.now();
    await saveSettings(settings);
    if (active()?.lego) void legoApi().call('POST', '/api/me/metrics', { metric: 'launches', amount: 1 }).catch(() => {});
    if (settings.closeOnLaunch) win?.minimize();
    void game.exited.then(async (r) => {
      running = null;
      let hint: string | null = null;
      if (r.code !== 0) {
        const text = await readFile(r.crashReport ?? game.logFile, 'utf8').catch(() => '');
        hint = diagnose(text);
      }
      send('game-exit', { ...r, logFile: game.logFile, hint });
      if (settings.closeOnLaunch) win?.restore();
    }).catch((e) => {
      running = null;
      send('game-exit', { code: -1, crashReport: null, logFile: game.logFile, hint: String(e) });
    });
    return { ok: true };
  } catch (e) {
    return { ok: false, error: e instanceof Error ? e.message : String(e) };
  } finally {
    launchAbort = null;
  }
}

// ---- skins -----------------------------------------------------------------
async function textureDataUrl(url: string | null | undefined): Promise<string | null> {
  if (!url) return null;
  const u = new URL(url.replace(/^http:/, 'https:'));
  if (u.hostname !== 'textures.minecraft.net') return null;
  const res = await fetch(u, { signal: AbortSignal.timeout(10_000) });
  if (!res.ok) return null;
  const buf = Buffer.from(await res.arrayBuffer());
  if (buf.length > 256 * 1024 || buf.subarray(1, 4).toString() !== 'PNG') return null;
  return `data:image/png;base64,${buf.toString('base64')}`;
}

async function skinFor(uuid?: string): Promise<SkinData> {
  const a = uuid ? store.accounts.find((x) => x.mc.uuid === uuid) : active();
  if (!a) return { skin: null, variant: 'classic', cape: null };
  try {
    // Ask the session server so skin changes made elsewhere show up.
    const p = await fetchJson<{ properties: { name: string; value: string }[] }>(fetch, `https://sessionserver.mojang.com/session/minecraft/profile/${a.mc.uuid}`);
    const tex = p.properties.find((x) => x.name === 'textures');
    const data = tex ? (JSON.parse(Buffer.from(tex.value, 'base64').toString('utf8')) as { textures: { SKIN?: { url: string; metadata?: { model?: string } }; CAPE?: { url: string } } }) : null;
    return {
      skin: await textureDataUrl(data?.textures.SKIN?.url ?? a.mc.skinUrl),
      variant: data?.textures.SKIN?.metadata?.model === 'slim' ? 'slim' : 'classic',
      cape: await textureDataUrl(data?.textures.CAPE?.url),
    };
  } catch {
    return { skin: await textureDataUrl(a.mc.skinUrl).catch(() => null), variant: a.mc.skinVariant ?? 'classic', cape: null };
  }
}

// ---- mods ------------------------------------------------------------------
async function listMods(p: GameProfile): Promise<ModEntry[]> {
  const dir = join(profileDir(p), 'mods');
  const meta = await installedMods(profileDir(p));
  const managed = new Set<string>(JSON.parse(await readFile(join(dir, '.lego-managed.json'), 'utf8').catch(() => '[]')) as string[]);
  const names = (await readdir(dir).catch(() => [] as string[])).filter((n) => /\.jar(\.disabled)?$/.test(n));
  return names.sort((a, b) => a.localeCompare(b)).map((filename) => {
    const base = filename.replace(/\.disabled$/, '');
    const m = meta.find((x) => x.filename === base);
    return { ...m, filename, enabled: !filename.endsWith('.disabled'), managedByLauncher: managed.has(base), title: m?.title ?? base.replace(/\.jar$/, '') };
  });
}

function sha512(buf: Buffer): string {
  return createHash('sha512').update(buf).digest('hex');
}

/** Imports a .mrpack or CurseForge zip as a new profile. */
async function importZip(buf: Buffer, fallbackName: string): Promise<{ profileId: string; missing: string[] }> {
  const names = new Set(listEntries(buf).map((e) => e.name));
  if (names.has('modrinth.index.json')) {
    const idx = readPackIndex(buf);
    const lv = packLoader(idx.dependencies);
    const p = await addProfile(idx.name || fallbackName, lv.gameVersion, lv.loaderType, lv.loader, 'package');
    await installMrPack(fetch, buf, profileDir(p), (done, total) => send('launch-progress', { step: `Modpack ${idx.name}`, done, total }));
    send('launch-progress', { step: 'Modpack installiert' });
    return { profileId: p.id, missing: [] };
  }
  if (names.has('manifest.json')) {
    const m = readCfManifest(buf);
    const lv = cfLoader(m);
    const p = await addProfile(m.name || fallbackName, lv.gameVersion, lv.loaderType, lv.loader, 'package');
    const r = await installCfPack(fetch, buf, profileDir(p), settings.curseforgeKey || CURSEFORGE_KEY);
    return { profileId: p.id, missing: r.missing };
  }
  throw new Error('Unbekanntes Paket: erwartet wird eine Modrinth-.mrpack oder ein CurseForge-Modpack (.zip).');
}

function rememberInstance(i: FoundInstance): FoundInstance {
  offeredInstances.set(i.gameDir, i);
  return i;
}

// ---- IPC -----------------------------------------------------------------------
const PROJECT_TYPES: ProjectType[] = ['mod', 'resourcepack', 'shader', 'modpack'];
const HEX = /^#[0-9a-f]{6}$/i;

function registerIpc(): void {
  ipcMain.handle('info', () => ({
    version: app.getVersion(), platform: process.platform, arch: process.arch,
    totalMemMb: Math.round(totalmem() / 1048576), freeMemMb: Math.round(freemem() / 1048576),
    cpus: cpus().length, cpuModel: cpus()[0]?.model ?? '', msClientConfigured: !!clientId(), gameDir: paths.game(),
  }));
  ipcMain.on('window', (_e, action: string) => {
    if (action === 'minimize') win?.minimize();
    else if (action === 'maximize') win?.isMaximized() ? win.unmaximize() : win?.maximize();
    else if (action === 'close') win?.close();
  });
  ipcMain.handle('settings:get', () => settings);
  ipcMain.handle('settings:set', async (_e, patch: Partial<Settings>) => {
    const allowed: (keyof Settings)[] = [
      'themeId', 'customThemes', 'language', 'backendUrl', 'selectedProfile', 'closeOnLaunch', 'reducedMotion', 'uiScale', 'autoUpdate', 'rarityColors',
      'accentColor', 'background', 'backgroundQuality', 'snow', 'showSnapshots', 'showHistorical', 'curseforgeKey', 'msClientId',
    ];
    for (const k of allowed) if (k in patch) (settings as unknown as Record<string, unknown>)[k] = patch[k];
    // Never trust the renderer: re-validate imported themes and clamp numbers.
    settings.customThemes = (Array.isArray(settings.customThemes) ? settings.customThemes : []).map((t) => validateTheme(t)).filter((v) => v.ok).map((v) => (v as { theme: unknown }).theme).slice(0, 50);
    settings.uiScale = Math.max(0.75, Math.min(2, Number(settings.uiScale) || 1));
    settings.backgroundQuality = Math.max(0, Math.min(1, Number(settings.backgroundQuality) || 0));
    if (settings.accentColor !== null && !HEX.test(String(settings.accentColor))) settings.accentColor = null;
    if (typeof settings.background !== 'string' || !/^[a-z0-9-]{1,32}$/.test(settings.background)) settings.background = 'nebula';
    if (!['de', 'en'].includes(settings.language)) settings.language = 'de';
    for (const k of ['snow', 'showSnapshots', 'showHistorical', 'closeOnLaunch', 'reducedMotion'] as const) settings[k] = !!settings[k];
    settings.msClientId = typeof settings.msClientId === 'string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(settings.msClientId.trim()) ? settings.msClientId.trim().toLowerCase() : '';
    settings.curseforgeKey = typeof settings.curseforgeKey === 'string' ? settings.curseforgeKey.trim().slice(0, 200) : '';
    if (!['ask', 'auto', 'off'].includes(settings.autoUpdate)) settings.autoUpdate = 'ask';
    if (typeof settings.backendUrl === 'string' && settings.backendUrl && !/^https:\/\//.test(settings.backendUrl) && !/^http:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(settings.backendUrl)) {
      settings.backendUrl = '';
    }
    await saveSettings(settings);
    return settings;
  });
  ipcMain.handle('profile:save', async (_e, p: GameProfile) => {
    const errors = validateProfile(p, Math.round(totalmem() / 1048576));
    if (errors.length) return { ok: false, errors, settings };
    const clean: GameProfile = {
      ...DEFAULT_PROFILE, ...p, id: p.id || randomUUID(), createdAt: p.createdAt || Date.now(),
      icon: /^[a-z0-9-]{1,24}$/.test(String(p.icon)) ? p.icon : 'grass', performancePack: !!p.performancePack && !!p.loaderType,
    };
    const i = settings.profiles.findIndex((x) => x.id === clean.id);
    if (i >= 0) settings.profiles[i] = { ...clean, gameDir: settings.profiles[i]!.gameDir };
    else settings.profiles.push({ ...clean, gameDir: null });
    await saveSettings(settings);
    return { ok: true, errors: [], settings };
  });
  ipcMain.handle('profile:delete', async (_e, id: string) => {
    if (settings.profiles.length > 1) settings.profiles = settings.profiles.filter((p) => p.id !== id);
    if (!settings.profiles.some((p) => p.id === settings.selectedProfile)) settings.selectedProfile = settings.profiles[0]!.id;
    await saveSettings(settings);
    return settings;
  });

  ipcMain.handle('auth:state', () => authState());
  const finishSignIn = async (id: string, ms: { refreshToken: string; accessToken: string }) => {
    const mc = await minecraftFromMicrosoft(fetch, ms.accessToken);
    await putAccount({ msRefreshToken: ms.refreshToken, mc, lego: null, clientId: id }, true);
    send('auth', authState());
    await connectLego();
  };
  const requireClientId = () => {
    const id = clientId();
    if (!id) throw new Error('Keine Microsoft-Client-ID: in Einstellungen → Erweitert eintragen oder beim Build LEGO_MS_CLIENT_ID setzen (docs/MICROSOFT-LOGIN.md).');
    return id;
  };
  // Default: Microsoft's sign-in page in a launcher window (like other launchers).
  ipcMain.handle('auth:signin', async () => {
    const id = requireClientId();
    loginWin?.close();
    const req = await authorizeRequest(id);
    // A fresh, in-memory session: no cookies of earlier logins, nothing written to disk.
    const part = session.fromPartition(`mslogin-${randomUUID()}`, { cache: false });
    part.setPermissionRequestHandler((_wc, _p, cb) => cb(false));
    loginWin = new BrowserWindow({
      parent: win ?? undefined, modal: false, width: 520, height: 700, title: 'Mit Microsoft anmelden', autoHideMenuBar: true, backgroundColor: '#ffffff',
      webPreferences: { session: part, sandbox: true, contextIsolation: true, nodeIntegration: false, javascript: true },
    });
    const lw = loginWin;
    let done = false;
    const handle = (url: string, prevent: () => void) => {
      let code: string | null;
      try {
        code = parseRedirect(url, req.state);
      } catch (e) {
        prevent();
        done = true;
        lw.close();
        send('auth', { ...authState(), error: e instanceof Error ? e.message : String(e) });
        return;
      }
      if (code === null) {
        // Only Microsoft/Xbox/Live pages may load in this window.
        try {
          const host = new URL(url).hostname;
          if (!/(^|\.)(microsoftonline\.com|live\.com|microsoft\.com|xboxlive\.com|xbox\.com|msauth\.net|msftauth\.net|aadcdn\.msftauth\.net|gfx\.ms)$/.test(host)) prevent();
        } catch { prevent(); }
        return;
      }
      prevent();
      done = true;
      lw.close();
      void exchangeCode(fetch, id, code, req.verifier).then((ms) => finishSignIn(id, ms)).catch((e) => send('auth', { ...authState(), error: e instanceof Error ? e.message : String(e) }));
    };
    lw.webContents.on('will-redirect', (e, url) => handle(url, () => e.preventDefault()));
    lw.webContents.on('will-navigate', (e, url) => handle(url, () => e.preventDefault()));
    lw.webContents.setWindowOpenHandler(({ url }) => {
      if (url.startsWith('https://')) void shell.openExternal(url);
      return { action: 'deny' };
    });
    lw.on('closed', () => {
      if (loginWin === lw) loginWin = null;
      if (!done) send('auth', { ...authState(), cancelled: true });
      void part.clearStorageData().catch(() => {});
    });
    await lw.loadURL(req.url);
    return { window: true };
  });
  // Fallback: device code (code entered on microsoft.com/link in any browser).
  ipcMain.handle('auth:signin-code', async () => {
    const id = requireClientId();
    signInAbort?.abort();
    signInAbort = new AbortController();
    const dc = await startDeviceCode(fetch, id);
    const signal = signInAbort.signal;
    void (async () => {
      try {
        await finishSignIn(id, await pollDeviceCode(fetch, id, dc, signal));
      } catch (e) {
        send('auth', { ...authState(), error: e instanceof Error ? e.message : String(e) });
      }
    })();
    void shell.openExternal(dc.verificationUri);
    return { userCode: dc.userCode, verificationUri: dc.verificationUri, expiresAt: dc.expiresAt };
  });
  ipcMain.on('auth:cancel', () => { signInAbort?.abort(); loginWin?.close(); });
  ipcMain.handle('auth:signout', async (_e, uuid?: string) => {
    const target = uuid ?? store.active;
    const acc = store.accounts.find((a) => a.mc.uuid === target);
    if (acc?.lego && backendUrl()) await new LegoApi(backendUrl(), fetch, acc.lego.token).call('POST', '/api/auth/logout').catch(() => {});
    const rest = store.accounts.filter((a) => a.mc.uuid !== target);
    store = { accounts: rest, active: store.active === target ? (rest[0]?.mc.uuid ?? null) : store.active };
    await saveAccounts(store);
    if (store.active && store.active !== acc?.mc.uuid) void restoreLego();
    return authState();
  });
  ipcMain.handle('auth:switch', async (_e, uuid: string) => {
    if (running) throw new Error('Während Minecraft läuft kann das Konto nicht gewechselt werden.');
    if (!store.accounts.some((a) => a.mc.uuid === uuid)) throw new Error('Konto nicht gefunden.');
    store = { ...store, active: uuid };
    await saveAccounts(store);
    legoError = null;
    void restoreLego();
    return authState();
  });
  ipcMain.handle('auth:lego', async () => {
    await connectLego();
    return authState();
  });
  ipcMain.handle('skin', (_e, uuid?: string) => skinFor(uuid));

  ipcMain.handle('game:versions', async () => (await getManifest(fetch)).versions.map((v) => ({ id: v.id, type: v.type, releaseTime: v.releaseTime })));
  ipcMain.handle('game:loaders', async (_e, type: LoaderType, gv: string) => {
    if (!(LOADER_TYPES as readonly string[]).includes(type) || !loaderSupports(type, gv)) return [];
    return loaderVersions(fetch, type, gv).catch(() => []);
  });
  ipcMain.handle('game:launch', (_e, id: string) => launch(id));
  ipcMain.on('game:cancel', () => launchAbort?.abort());
  ipcMain.handle('game:running', () => !!running);
  ipcMain.handle('folder', async (_e, kind: string, profileId?: string) => {
    const gameDir = profileDir(profileById(profileId));
    const sub: Record<string, string> = { mods: 'mods', screenshots: 'screenshots', resourcepacks: 'resourcepacks', shaderpacks: 'shaderpacks' };
    const target = kind === 'logs' ? paths.logs() : sub[kind] ? join(gameDir, sub[kind]!) : gameDir;
    await mkdir(target, { recursive: true });
    await shell.openPath(target);
  });

  ipcMain.handle('mods:list', (_e, profileId: string) => listMods(profileById(profileId)));
  ipcMain.handle('mods:search', async (_e, profileId: string, query: string, type: ProjectType, offset: number) => {
    if (!PROJECT_TYPES.includes(type)) throw new Error('invalid type');
    const p = profileById(profileId);
    return search(fetch, { query: String(query).slice(0, 100), type, gameVersion: p.gameVersion, loader: p.loaderType, offset: Math.max(0, Math.floor(Number(offset) || 0)) });
  });
  ipcMain.handle('mods:install', async (_e, profileId: string, projectId: string, type: ProjectType, title: string, iconUrl: string | null) => {
    if (!PROJECT_TYPES.includes(type) || !/^[\w-]{1,64}$/.test(projectId)) throw new Error('invalid project');
    const p = profileById(profileId);
    if (type === 'mod' && !p.loaderType) throw new Error('Dieses Profil ist Vanilla. Wähle zuerst einen Mod-Loader (z. B. Fabric) im Profil.');
    const icon = typeof iconUrl === 'string' && iconUrl.startsWith('https://cdn.modrinth.com/') ? iconUrl : null;
    return installProject(fetch, profileDir(p), projectId, p.gameVersion, p.loaderType, type, { title: String(title).slice(0, 80), iconUrl: icon });
  });
  ipcMain.handle('mods:remove', (_e, profileId: string, filename: string) => removeMod(profileDir(profileById(profileId)), filename));
  ipcMain.handle('mods:toggle', async (_e, profileId: string, filename: string, enabled: boolean) => {
    await toggleMod(profileDir(profileById(profileId)), filename, !!enabled);
  });
  ipcMain.handle('modpack:install', async (_e, projectId: string, title: string) => {
    if (!/^[\w-]{1,64}$/.test(projectId)) throw new Error('invalid project');
    const versions = await projectVersions(fetch, projectId, null, null, 'modpack');
    const v = versions.find((x) => x.version_type === 'release') ?? versions[0];
    const file = v?.files.find((x) => x.primary && x.filename.endsWith('.mrpack')) ?? v?.files.find((x) => x.filename.endsWith('.mrpack'));
    if (!file) throw new Error('Dieses Modpack hat keine .mrpack-Datei.');
    send('launch-progress', { step: `Lade ${title}` });
    const res = await fetch(file.url, { signal: AbortSignal.timeout(120_000) });
    if (!res.ok) throw new Error(`Download fehlgeschlagen (${res.status})`);
    const buf = Buffer.from(await res.arrayBuffer());
    if (sha512(buf) !== file.hashes.sha512) throw new Error('Prüfsumme des Modpacks stimmt nicht.');
    const r = await importZip(buf, String(title));
    return { profileId: r.profileId };
  });
  ipcMain.handle('import:file', async () => {
    const r = await dialog.showOpenDialog(win!, { properties: ['openFile'], filters: [{ name: 'Modpack', extensions: ['mrpack', 'zip'] }] });
    const file = r.filePaths[0];
    if (r.canceled || !file) return null;
    if ((await stat(file)).size > 1024 * 1024 * 1024) throw new Error('Datei ist größer als 1 GB.');
    return importZip(await readFile(file), file.split(/[\\/]/).pop()!.replace(/\.(mrpack|zip)$/i, ''));
  });
  ipcMain.handle('import:scan', async () => (await scanInstances()).map(rememberInstance));
  ipcMain.handle('import:folder', async () => {
    const r = await dialog.showOpenDialog(win!, { properties: ['openDirectory'] });
    const dir = r.filePaths[0];
    if (r.canceled || !dir) return null;
    return rememberInstance(await readFolder(dir));
  });
  ipcMain.handle('import:instance', async (_e, inst: FoundInstance, includeWorlds: boolean) => {
    const known = offeredInstances.get(inst?.gameDir);
    if (!known) throw new Error('Unbekannte Instanz – bitte erneut suchen.');
    const gameVersion = known.gameVersion ?? (await latestRelease());
    const p = await addProfile(known.name, gameVersion, known.loaderType, known.loader, 'chest');
    const copied = await copyInstance(known.gameDir, profileDir(p), !!includeWorlds);
    return { profileId: p.id, copied };
  });

  // The renderer never sees the LEGO token; it asks main to call the API.
  ipcMain.handle('lego', async (_e, method: string, path: string, body?: unknown) => {
    if (!['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].includes(method) || typeof path !== 'string' || !path.startsWith('/api/') || path.includes('..')) throw new Error('invalid request');
    if (!backendUrl()) throw new Error('Kein LEGO-Server konfiguriert.');
    return legoApi().call(method, path, body);
  });
  ipcMain.handle('open-external', async (_e, url: string) => {
    const u = new URL(url);
    if (u.protocol !== 'https:') throw new Error('only https links');
    await shell.openExternal(u.toString());
  });
  ipcMain.handle('diagnostics', async () => {
    const a = active();
    const lines = [
      `LEGO Launcher ${app.getVersion()} (Electron ${process.versions.electron}, Chrome ${process.versions.chrome})`,
      `OS: ${process.platform} ${release()} ${process.arch}`,
      `CPU: ${cpus()[0]?.model} x${cpus().length}`,
      `RAM: ${Math.round(totalmem() / 1048576)} MB total, ${Math.round(freemem() / 1048576)} MB free`,
      `Secure storage: ${encryptionAvailable()}`,
      `Accounts: ${store.accounts.length}, signed in: ${!!a} (LEGO: ${!!a?.lego}${legoError ? `, error: ${legoError}` : ''})`,
      `Backend: ${backendUrl() || '(none)'}`,
      `Profiles: ${settings.profiles.map((p) => `${p.name} [${p.gameVersion}${p.loaderType ? ` ${p.loaderType} ${p.loader ?? 'latest'}` : ''}, ${p.memoryMb} MB${p.legoClient ? ', LEGO' : ''}${p.performancePack ? ', FPS' : ''}]`).join('; ')}`,
    ];
    return lines.join('\n');
  });
  ipcMain.handle('update:check', () => checkForUpdate());
  ipcMain.handle('update:install', () => installUpdate());
}

function createWindow(): void {
  win = new BrowserWindow({
    width: 1280, height: 800, minWidth: 1024, minHeight: 680, frame: false, backgroundColor: '#141519', show: false,
    title: 'LEGO Launcher',
    webPreferences: { preload: join(__dirname, 'preload.cjs'), contextIsolation: true, sandbox: true, nodeIntegration: false, webviewTag: false, spellcheck: false },
  });
  win.once('ready-to-show', () => win?.show());
  // No in-app navigation or popups: external links open in the system browser.
  win.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith('https://')) void shell.openExternal(url);
    return { action: 'deny' };
  });
  win.webContents.on('will-navigate', (e) => e.preventDefault());
  void win.loadFile(join(__dirname, 'renderer', 'index.html'));
}

// Development/test hooks; ignored in packaged (installed) builds.
if (!app.isPackaged && process.env.LEGO_USER_DATA) app.setPath('userData', process.env.LEGO_USER_DATA);

if (!app.requestSingleInstanceLock()) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (win) {
      if (win.isMinimized()) win.restore();
      win.focus();
    }
  });
  app.whenReady().then(async () => {
    session.defaultSession.setPermissionRequestHandler((_wc, _perm, cb) => cb(false));
    settings = await loadSettings();
    store = await loadAccounts();
    if (!app.isPackaged && process.env.LEGO_E2E === '1' && process.env.LEGO_E2E_ACCOUNT) {
      const acc = JSON.parse(process.env.LEGO_E2E_ACCOUNT) as Account;
      store = { accounts: [acc], active: acc.mc.uuid };
    }
    registerIpc();
    createWindow();
    setupUpdater(send, () => settings.autoUpdate);
    if (active()) void restoreLego();
    if (!encryptionAvailable()) {
      void dialog.showMessageBox({ type: 'warning', message: 'Sichere Speicherung nicht verfügbar', detail: 'Das Betriebssystem bietet keine Verschlüsselung an. Deine Anmeldung wird nur bis zum Schließen des Launchers gespeichert.' });
    }
  });
  app.on('window-all-closed', () => app.quit());
}
