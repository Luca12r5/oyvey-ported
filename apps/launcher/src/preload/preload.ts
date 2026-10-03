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
  signInWithCode: () => ipcRenderer.invoke('auth:signin-code'),
  cancelSignIn: () => ipcRenderer.send('auth:cancel'),
  signOut: (uuid) => ipcRenderer.invoke('auth:signout', uuid),
  switchAccount: (uuid) => ipcRenderer.invoke('auth:switch', uuid),
  reconnectLego: () => ipcRenderer.invoke('auth:lego'),
  skin: (uuid) => ipcRenderer.invoke('skin', uuid),
  versions: () => ipcRenderer.invoke('game:versions'),
  loaderVersions: (type, gv) => ipcRenderer.invoke('game:loaders', type, gv),
  launch: (id) => ipcRenderer.invoke('game:launch', id),
  cancelLaunch: () => ipcRenderer.send('game:cancel'),
  gameRunning: () => ipcRenderer.invoke('game:running'),
  openFolder: (k, id) => ipcRenderer.invoke('folder', k, id),
  mods: (id) => ipcRenderer.invoke('mods:list', id),
  searchProjects: (id, q, t, o) => ipcRenderer.invoke('mods:search', id, q, t, o),
  installProject: (id, pid, t, title, icon) => ipcRenderer.invoke('mods:install', id, pid, t, title, icon),
  removeMod: (id, f) => ipcRenderer.invoke('mods:remove', id, f),
  toggleMod: (id, f, e) => ipcRenderer.invoke('mods:toggle', id, f, e),
  installModpack: (pid, title) => ipcRenderer.invoke('modpack:install', pid, title),
  importFile: () => ipcRenderer.invoke('import:file'),
  scanInstances: () => ipcRenderer.invoke('import:scan'),
  pickFolder: () => ipcRenderer.invoke('import:folder'),
  importInstance: (inst, w) => ipcRenderer.invoke('import:instance', inst, w),
  officialStatus: () => ipcRenderer.invoke('official:status'),
  exportToOfficial: (id, icon) => ipcRenderer.invoke('official:export', id, icon),
  openOfficialLauncher: () => ipcRenderer.invoke('official:open'),
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
