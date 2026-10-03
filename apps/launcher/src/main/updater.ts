// Launcher self-update via electron-updater (GitHub Releases). Downloads are
// HTTPS, verified against the SHA-512 in latest.yml and, for signed builds,
// against the Authenticode publisher. Nothing is downloaded without consent
// unless the user chose "auto".

import { app } from 'electron';
import electronUpdater from 'electron-updater';

const { autoUpdater } = electronUpdater;
let mode: () => 'ask' | 'auto' | 'off' = () => 'ask';
let ready = false;

export function setupUpdater(send: (channel: string, data: unknown) => void, getMode: () => 'ask' | 'auto' | 'off'): void {
  mode = getMode;
  autoUpdater.autoDownload = false;
  autoUpdater.autoInstallOnAppQuit = true;
  autoUpdater.on('update-available', (i) => {
    send('update', { state: 'available', version: i.version });
    if (mode() === 'auto') void autoUpdater.downloadUpdate();
  });
  autoUpdater.on('download-progress', (p) => send('update', { state: 'downloading', percent: Math.round(p.percent) }));
  autoUpdater.on('update-downloaded', (i) => {
    ready = true;
    send('update', { state: 'ready', version: i.version });
  });
  autoUpdater.on('error', (e) => send('update', { state: 'error', error: String(e?.message ?? e) }));
  if (app.isPackaged && mode() !== 'off') setTimeout(() => void autoUpdater.checkForUpdates().catch(() => {}), 10_000);
}

export async function checkForUpdate(): Promise<{ available: boolean; version?: string; error?: string }> {
  if (!app.isPackaged) return { available: false, error: 'Updates gibt es nur in installierten Builds.' };
  try {
    const r = await autoUpdater.checkForUpdates();
    const v = r?.updateInfo.version;
    return { available: !!v && v !== app.getVersion(), version: v };
  } catch (e) {
    return { available: false, error: String(e instanceof Error ? e.message : e) };
  }
}

export async function installUpdate(): Promise<void> {
  if (!ready) {
    await autoUpdater.downloadUpdate();
    return;
  }
  autoUpdater.quitAndInstall(false, true);
}
