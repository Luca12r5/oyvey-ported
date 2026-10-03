// Exposes a narrow, typed API to the renderer. No Node.js access in the page.

import { contextBridge, ipcRenderer } from 'electron';
import type { LauncherApi } from '../ipc-types.ts';

const CHANNELS = new Set(['auth', 'launch-progress', 'game-log', 'game-exit', 'lego-event', 'update']);

const api: LauncherApi = {
  info: () => ipcRenderer.invoke('info'),
  window: (a) => ipcRenderer.send('window', a),
  getSettings: () => ipcRenderer.invoke('settings:get'),
  setSettings: (p) => ipcRenderer.invoke('settings:set', p),
  saveProfile: (p) => ipcRenderer.invoke('profile:save', p),
  deleteProfile: (id) => ipcRenderer.invoke('profile:delete', id),
  authState: () => ipcRenderer.invoke('auth:state'),
  signIn: () => ipcRenderer.invoke('auth:signin'),
  cancelSignIn: () => ipcRenderer.send('auth:cancel'),
  signOut: () => ipcRenderer.invoke('auth:signout'),
  reconnectLego: () => ipcRenderer.invoke('auth:lego'),
  versions: () => ipcRenderer.invoke('game:versions'),
  fabricLoaders: (gv) => ipcRenderer.invoke('game:loaders', gv),
  launch: (id) => ipcRenderer.invoke('game:launch', id),
  cancelLaunch: () => ipcRenderer.send('game:cancel'),
  gameRunning: () => ipcRenderer.invoke('game:running'),
  openFolder: (k, id) => ipcRenderer.invoke('folder', k, id),
  lego: (m, p, b) => ipcRenderer.invoke('lego', m, p, b),
  openExternal: (u) => ipcRenderer.invoke('open-external', u),
  diagnostics: () => ipcRenderer.invoke('diagnostics'),
  checkUpdate: () => ipcRenderer.invoke('update:check'),
  installUpdate: () => ipcRenderer.invoke('update:install'),
  on: (channel, cb) => {
    if (!CHANNELS.has(channel)) throw new Error('unknown channel');
    const listener = (_e: unknown, data: unknown) => cb(data);
    ipcRenderer.on(`lego:${channel}`, listener);
    return () => ipcRenderer.removeListener(`lego:${channel}`, listener);
  },
};

contextBridge.exposeInMainWorld('lego', api);
