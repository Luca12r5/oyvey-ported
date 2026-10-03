// Electron main process: window, IPC, sign-in, installation and launching.

import { app, BrowserWindow, ipcMain, shell, session, dialog } from 'electron';
import { cpus, freemem, release, totalmem } from 'node:os';
import { join } from 'node:path';
import { mkdir, readFile } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import { minecraftFromMicrosoft, pollDeviceCode, refreshMicrosoft, startDeviceCode, AuthError, type MinecraftSession } from '../core/msauth.ts';
import { LegoApi } from '../core/lego.ts';
import { currentEnv, buildArguments, redactArgs } from '../core/mojang.ts';
import {
  fabricApiFile, fabricLoaders, getManifest, installJava, installVersion, layout, resolveProfileVersion, syncMods, type ModFile,
} from '../core/install.ts';
import { diagnose, startGame } from '../core/launch.ts';
import { parseJvmArgs, validateProfile, type GameProfile, type Settings } from '../core/settings.ts';
import { encryptionAvailable, loadAccount, loadSettings, paths, saveAccount, saveSettings, type Account } from './store.ts';
import { setupUpdater, checkForUpdate, installUpdate } from './updater.ts';
import type { AuthState, LaunchProgress } from '../ipc-types.ts';
import { validateTheme } from '@lego/shared';

declare const __MS_CLIENT_ID__: string;
declare const __BACKEND_URL__: string;
const MS_CLIENT_ID = typeof __MS_CLIENT_ID__ === 'string' ? __MS_CLIENT_ID__ : '';
const BACKEND_URL = typeof __BACKEND_URL__ === 'string' ? __BACKEND_URL__ : '';

let win: BrowserWindow | null = null;
let settings: Settings;
let account: Account | null = null;
let legoError: string | null = null;
let signInAbort: AbortController | null = null;
let launchAbort: AbortController | null = null;
let gameRunning = false;
const env = currentEnv(process.platform, process.arch, release());

function send(channel: string, data: unknown): void {
  win?.webContents.send(`lego:${channel}`, data);
}

function backendUrl(): string {
  return settings.backendUrl && !settings.backendUrl.includes('.example') ? settings.backendUrl : BACKEND_URL;
}

function legoApi(): LegoApi {
  return new LegoApi(backendUrl(), fetch, account?.lego?.token ?? null);
}

function authState(): AuthState {
  return {
    signedIn: !!account,
    name: account?.mc.name ?? null,
    uuid: account?.mc.uuid ?? null,
    skinUrl: account?.mc.skinUrl ?? null,
    lego: { connected: !!account?.lego, userId: account?.lego?.userId ?? null, roles: account?.lego?.roles ?? [], error: legoError },
    secureStorage: encryptionAvailable(),
  };
}

/** Returns a valid Minecraft session, refreshing through Microsoft when it expired. */
async function freshSession(): Promise<MinecraftSession> {
  if (!account) throw new AuthError('signed_out', 'Bitte zuerst mit Microsoft anmelden.');
  if (account.mc.expiresAt - Date.now() > 5 * 60_000) return account.mc;
  const ms = await refreshMicrosoft(fetch, MS_CLIENT_ID, account.msRefreshToken);
  const mc = await minecraftFromMicrosoft(fetch, ms.accessToken);
  account = { ...account, msRefreshToken: ms.refreshToken, mc };
  await saveAccount(account);
  return mc;
}

/** Reuses a stored LEGO session if the server still accepts it, otherwise logs in again. */
async function restoreLego(): Promise<void> {
  if (account?.lego && account.lego.expiresAt > Date.now() && backendUrl()) {
    try {
      const s = await legoApi().call<{ user: { id: string; roles: string[] } }>('GET', '/api/auth/session');
      account = { ...account, lego: { ...account.lego, roles: s.user.roles } };
      legoError = null;
      send('auth', authState());
      return;
    } catch {
      account = { ...account, lego: null };
    }
  }
  await connectLego();
}

async function connectLego(): Promise<void> {
  if (!account || !backendUrl()) {
    legoError = backendUrl() ? null : 'Kein LEGO-Server konfiguriert.';
    return;
  }
  try {
    const mc = await freshSession();
    const r = await new LegoApi(backendUrl(), fetch).login(mc);
    account = { ...account, lego: { token: r.token, expiresAt: r.expiresAt, userId: r.user.id, roles: r.user.roles } };
    legoError = null;
    await saveAccount(account);
  } catch (e) {
    legoError = e instanceof Error ? e.message : String(e);
  }
  send('auth', authState());
}

// ---- presence heartbeat ----------------------------------------------------
setInterval(() => {
  if (!account?.lego) return;
  void legoApi().call('POST', '/api/me/presence', { status: gameRunning ? 'in_game' : 'online', gameVersion: gameRunning ? '1.21.11' : null, activity: gameRunning ? 'Minecraft' : 'Launcher' }).catch(() => {});
}, 30_000).unref();

// ---- launching ---------------------------------------------------------------
async function legoClientFile(): Promise<ModFile> {
  const r = await legoApi().call<{ version: string; url: string; filename: string; sha512?: string; sha256?: string } | null>('GET', '/api/public/client-release');
  if (!r || !r.url) throw new Error('Der LEGO-Server bietet noch keinen Client-Download an (LEGO_CLIENT_URL nicht gesetzt).');
  return { url: r.url, filename: r.filename, sha512: r.sha512, sha256: r.sha256 };
}

async function launch(profileId: string): Promise<{ ok: boolean; error?: string }> {
  if (gameRunning) return { ok: false, error: 'Minecraft läuft bereits.' };
  const profile = settings.profiles.find((p) => p.id === profileId);
  if (!profile) return { ok: false, error: 'Profil nicht gefunden.' };
  launchAbort = new AbortController();
  const signal = launchAbort.signal;
  const report = (step: string, p?: { done: number; total: number; bytes: number; totalBytes: number }) => send('launch-progress', { step, ...p } satisfies LaunchProgress);
  try {
    report('Anmeldung prüfen');
    const mc = await freshSession();
    const l = layout(paths.game());
    const loader = profile.legoClient ? (profile.loader ?? (await fabricLoaders(fetch, profile.gameVersion)).find((x) => x.stable)?.version ?? null) : profile.loader;
    if (profile.legoClient && !loader) throw new Error(`Kein Fabric Loader für ${profile.gameVersion} verfügbar.`);
    report('Versionsdaten');
    const { merged, base } = await resolveProfileVersion(fetch, l, profile.gameVersion, loader);
    const installed = await installVersion(fetch, l, merged, base.id, env, report, signal);
    const javaComponent = merged.javaVersion?.component ?? 'java-runtime-delta';
    const javaPath = await installJava(fetch, l, javaComponent, env, report, signal);
    const gameDir = profile.gameDir ?? join(l.instances, profile.id);
    await mkdir(gameDir, { recursive: true });
    if (profile.legoClient) {
      report('LEGO Client');
      const mods = [await fabricApiFile(fetch, profile.gameVersion), await legoClientFile()];
      await syncMods(fetch, join(gameDir, 'mods'), mods, report, signal);
    }
    const jvm = parseJvmArgs(profile.jvmArgs);
    const args = buildArguments({
      version: merged, env, javaPath, gameDir, assetsDir: l.assets, librariesDir: l.libraries, nativesDir: installed.nativesDir,
      clientJar: installed.clientJar, classpathSeparator: process.platform === 'win32' ? ';' : ':',
      player: { name: mc.name, uuid: mc.uuid, accessToken: mc.accessToken, xuid: mc.xuid, userType: 'msa' },
      launcherName: 'lego-launcher', launcherVersion: app.getVersion(),
      memoryMb: { min: Math.min(1024, profile.memoryMb), max: profile.memoryMb },
      extraJvmArgs: [...jvm.args, ...(account?.lego ? [`-Dlegoclient.api=${backendUrl()}`] : [])],
      resolution: profile.resolution ?? undefined, loggingConfigPath: installed.loggingConfig ?? undefined,
    });
    report('Starte Minecraft');
    send('game-log', { line: `[launcher] ${javaPath} ${redactArgs(args, mc.accessToken).join(' ')}`, stream: 'out' });
    const game = await startGame(javaPath, args, gameDir, paths.logs(), (line, stream) => send('game-log', { line: line.split(mc.accessToken).join('<redacted>'), stream }));
    gameRunning = true;
    profile.lastPlayed = Date.now();
    await saveSettings(settings);
    if (account?.lego) void legoApi().call('POST', '/api/me/metrics', { metric: 'launches', amount: 1 }).catch(() => {});
    if (settings.closeOnLaunch) win?.minimize();
    void game.exited.then(async (r) => {
      gameRunning = false;
      let hint: string | null = null;
      if (r.code !== 0) {
        const text = await readFile(r.crashReport ?? game.logFile, 'utf8').catch(() => '');
        hint = diagnose(text);
      }
      send('game-exit', { ...r, logFile: game.logFile, hint });
      if (settings.closeOnLaunch) win?.restore();
    }).catch((e) => {
      gameRunning = false;
      send('game-exit', { code: -1, crashReport: null, logFile: game.logFile, hint: String(e) });
    });
    return { ok: true };
  } catch (e) {
    return { ok: false, error: e instanceof Error ? e.message : String(e) };
  } finally {
    launchAbort = null;
  }
}

// ---- IPC -----------------------------------------------------------------------
function registerIpc(): void {
  ipcMain.handle('info', () => ({
    version: app.getVersion(), platform: process.platform, arch: process.arch,
    totalMemMb: Math.round(totalmem() / 1048576), freeMemMb: Math.round(freemem() / 1048576),
    cpus: cpus().length, cpuModel: cpus()[0]?.model ?? '', msClientConfigured: !!MS_CLIENT_ID, gameDir: paths.game(),
  }));
  ipcMain.on('window', (_e, action: string) => {
    if (action === 'minimize') win?.minimize();
    else if (action === 'maximize') win?.isMaximized() ? win.unmaximize() : win?.maximize();
    else if (action === 'close') win?.close();
  });
  ipcMain.handle('settings:get', () => settings);
  ipcMain.handle('settings:set', async (_e, patch: Partial<Settings>) => {
    const allowed: (keyof Settings)[] = ['themeId', 'customThemes', 'language', 'backendUrl', 'selectedProfile', 'closeOnLaunch', 'reducedMotion', 'uiScale', 'autoUpdate', 'rarityColors'];
    for (const k of allowed) if (k in patch) (settings as unknown as Record<string, unknown>)[k] = patch[k];
    // Never trust the renderer: re-validate imported themes and clamp numbers.
    settings.customThemes = (Array.isArray(settings.customThemes) ? settings.customThemes : []).map((t) => validateTheme(t)).filter((v) => v.ok).map((v) => (v as { theme: unknown }).theme).slice(0, 50);
    settings.uiScale = Math.max(0.75, Math.min(2, Number(settings.uiScale) || 1));
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
    const clean: GameProfile = { ...p, id: p.id || randomUUID(), createdAt: p.createdAt || Date.now() };
    const i = settings.profiles.findIndex((x) => x.id === clean.id);
    if (i >= 0) settings.profiles[i] = clean;
    else settings.profiles.push(clean);
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
  ipcMain.handle('auth:signin', async () => {
    if (!MS_CLIENT_ID) throw new Error('Dieser Build hat keine Microsoft-Client-ID (LEGO_MS_CLIENT_ID). Siehe docs/LAUNCHER.md.');
    signInAbort?.abort();
    signInAbort = new AbortController();
    const dc = await startDeviceCode(fetch, MS_CLIENT_ID);
    const signal = signInAbort.signal;
    void (async () => {
      try {
        const ms = await pollDeviceCode(fetch, MS_CLIENT_ID, dc, signal);
        const mc = await minecraftFromMicrosoft(fetch, ms.accessToken);
        account = { msRefreshToken: ms.refreshToken, mc, lego: null };
        await saveAccount(account);
        send('auth', authState());
        await connectLego();
      } catch (e) {
        send('auth', { ...authState(), error: e instanceof Error ? e.message : String(e) });
      }
    })();
    void shell.openExternal(dc.verificationUri);
    return { userCode: dc.userCode, verificationUri: dc.verificationUri, expiresAt: dc.expiresAt };
  });
  ipcMain.on('auth:cancel', () => signInAbort?.abort());
  ipcMain.handle('auth:signout', async () => {
    if (account?.lego) await legoApi().call('POST', '/api/auth/logout').catch(() => {});
    account = null;
    await saveAccount(null);
    return authState();
  });
  ipcMain.handle('auth:lego', async () => {
    await connectLego();
    return authState();
  });
  ipcMain.handle('game:versions', async () => (await getManifest(fetch)).versions.map((v) => ({ id: v.id, type: v.type, releaseTime: v.releaseTime })));
  ipcMain.handle('game:loaders', (_e, gv: string) => fabricLoaders(fetch, gv));
  ipcMain.handle('game:launch', (_e, id: string) => launch(id));
  ipcMain.on('game:cancel', () => launchAbort?.abort());
  ipcMain.handle('game:running', () => gameRunning);
  ipcMain.handle('folder', async (_e, kind: string, profileId?: string) => {
    const l = layout(paths.game());
    const p = settings.profiles.find((x) => x.id === (profileId ?? settings.selectedProfile));
    const gameDir = p?.gameDir ?? join(l.instances, p?.id ?? 'default');
    const target = kind === 'logs' ? paths.logs() : kind === 'mods' ? join(gameDir, 'mods') : kind === 'screenshots' ? join(gameDir, 'screenshots') : gameDir;
    await mkdir(target, { recursive: true });
    await shell.openPath(target);
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
    const lines = [
      `LEGO Launcher ${app.getVersion()} (Electron ${process.versions.electron}, Chrome ${process.versions.chrome})`,
      `OS: ${process.platform} ${release()} ${process.arch}`,
      `CPU: ${cpus()[0]?.model} x${cpus().length}`,
      `RAM: ${Math.round(totalmem() / 1048576)} MB total, ${Math.round(freemem() / 1048576)} MB free`,
      `Secure storage: ${encryptionAvailable()}`,
      `Signed in: ${!!account} (LEGO: ${!!account?.lego}${legoError ? `, error: ${legoError}` : ''})`,
      `Backend: ${backendUrl() || '(none)'}`,
      `Profiles: ${settings.profiles.map((p) => `${p.name} [${p.gameVersion}${p.loader ? ` fabric ${p.loader}` : ''}, ${p.memoryMb} MB${p.legoClient ? ', LEGO' : ''}]`).join('; ')}`,
    ];
    return lines.join('\n');
  });
  ipcMain.handle('update:check', () => checkForUpdate());
  ipcMain.handle('update:install', () => installUpdate());
}

function createWindow(): void {
  win = new BrowserWindow({
    width: 1280, height: 800, minWidth: 1024, minHeight: 680, frame: false, backgroundColor: '#0b1020', show: false,
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
    account = await loadAccount();
    if (!app.isPackaged && process.env.LEGO_E2E === '1' && process.env.LEGO_E2E_ACCOUNT) account = JSON.parse(process.env.LEGO_E2E_ACCOUNT) as Account;
    registerIpc();
    createWindow();
    setupUpdater(send, () => settings.autoUpdate);
    if (account) void restoreLego();
    if (!encryptionAvailable()) {
      void dialog.showMessageBox({ type: 'warning', message: 'Sichere Speicherung nicht verfügbar', detail: 'Das Betriebssystem bietet keine Verschlüsselung an. Deine Anmeldung wird nur bis zum Schließen des Launchers gespeichert.' });
    }
  });
  app.on('window-all-closed', () => app.quit());
}
