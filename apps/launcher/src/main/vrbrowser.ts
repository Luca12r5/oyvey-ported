// Real web browser for the VR lobby: an invisible offscreen Chromium window in
// its own session. Its frames are streamed to the renderer (JPEG) and shown on
// a 3D panel; mouse/keyboard input comes back and is injected into the page.
// Safety: https only, SafeSearch forced, no downloads, no permissions, no
// popups, no access to the launcher (separate sandboxed session, no preload).

import { BrowserWindow, session } from 'electron';
import { allowed, cleanInput, enforceSafeSearch, HOME_URL, toTarget } from '../core/vrweb.ts';

export const VRB_W = 1280;
export const VRB_H = 800;
const PARTITION = 'persist:vrbrowser';

export interface VrbState { url: string; title: string; canBack: boolean; canForward: boolean; loading: boolean }

let sessionReady = false;
function prepareSession(): void {
  if (sessionReady) return;
  sessionReady = true;
  const ses = session.fromPartition(PARTITION);
  ses.setPermissionRequestHandler((_wc, _p, cb) => cb(false));
  ses.setPermissionCheckHandler(() => false);
  ses.on('will-download', (e) => e.preventDefault());
  ses.webRequest.onBeforeRequest({ urls: ['<all_urls>'] }, (d, cb) => {
    const frame = d.resourceType === 'mainFrame' || d.resourceType === 'subFrame';
    if (frame && !allowed(d.url)) return cb({ cancel: true });
    if (d.resourceType === 'mainFrame') {
      const safe = enforceSafeSearch(d.url);
      if (safe) return cb({ redirectURL: safe });
    }
    cb({});
  });
  ses.webRequest.onBeforeSendHeaders({ urls: ['https://*.youtube.com/*', 'https://youtube.com/*', 'https://*.youtube-nocookie.com/*'] }, (d, cb) => {
    cb({ requestHeaders: { ...d.requestHeaders, 'YouTube-Restrict': 'Strict' } });
  });
}

export class VrBrowser {
  private win: BrowserWindow | null = null;
  private lastFrame = 0;
  private pending: NodeJS.Timeout | null = null;
  private readonly send: (channel: string, data: unknown) => void;

  constructor(send: (channel: string, data: unknown) => void) {
    this.send = send;
  }

  open(): { w: number; h: number; state: VrbState } {
    if (!this.win || this.win.isDestroyed()) {
      prepareSession();
      const win = new BrowserWindow({
        show: false, width: VRB_W, height: VRB_H, useContentSize: true,
        webPreferences: { offscreen: true, partition: PARTITION, sandbox: true, contextIsolation: true, nodeIntegration: false, webviewTag: false, spellcheck: false, backgroundThrottling: false },
      });
      const wc = win.webContents;
      wc.setFrameRate(30);
      wc.setWindowOpenHandler(({ url }) => {
        if (allowed(url)) void wc.loadURL(url);
        return { action: 'deny' };
      });
      wc.on('will-navigate', (e, url) => { if (!allowed(url)) e.preventDefault(); });
      let latest: Electron.NativeImage | null = null;
      const emit = () => {
        this.lastFrame = Date.now();
        this.pending = null;
        if (latest && !win.isDestroyed()) this.send('vrb-frame', latest.toJPEG(82));
        latest = null;
      };
      wc.on('paint', (_e, _dirty, image) => {
        // At most ~30 frames per second over IPC; always the newest picture.
        latest = image;
        const now = Date.now();
        if (now - this.lastFrame >= 33) emit();
        else if (!this.pending) this.pending = setTimeout(emit, 33 - (now - this.lastFrame));
      });
      for (const ev of ['did-navigate', 'did-navigate-in-page', 'page-title-updated', 'did-start-loading', 'did-stop-loading'] as const) {
        wc.on(ev as 'did-stop-loading', () => this.send('vrb-state', this.state()));
      }
      // Electron shows a blank page on network errors; show a readable one instead.
      wc.on('did-fail-load', (_e, code, desc, url, isMain) => {
        if (!isMain || code === -3) return; // -3 = aborted by a new navigation
        const safe = (v: string) => v.replace(/[&<>"']/g, (ch) => `&#${ch.charCodeAt(0)};`);
        const page = `<body style="margin:0;height:100vh;display:grid;place-items:center;background:#120c1c;color:#f3ecff;font:24px system-ui,sans-serif;text-align:center"><div><div style="font-size:64px">🎃</div><h1 style="color:#ff7a1a">Seite nicht erreichbar</h1><p>${safe(url)}</p><p style="color:#a99bc4">${safe(desc)} (${code}) – prüfe deine Internetverbindung und lade neu.</p></div></body>`;
        void wc.executeJavaScript(`document.documentElement.innerHTML = ${JSON.stringify(page)}`).catch(() => {});
        this.send('vrb-state', { ...this.state(), title: 'Seite nicht erreichbar', loading: false });
      });
      wc.on('render-process-gone', () => this.send('vrb-state', { ...this.state(), title: 'Seite abgestürzt – neu laden' }));
      void wc.loadURL(HOME_URL);
      this.win = win;
    }
    this.win.webContents.focus();
    return { w: VRB_W, h: VRB_H, state: this.state() };
  }

  state(): VrbState {
    const wc = this.win && !this.win.isDestroyed() ? this.win.webContents : null;
    if (!wc) return { url: '', title: '', canBack: false, canForward: false, loading: false };
    return { url: wc.getURL(), title: wc.getTitle(), canBack: wc.navigationHistory.canGoBack(), canForward: wc.navigationHistory.canGoForward(), loading: wc.isLoading() };
  }

  input(raw: unknown): void {
    const wc = this.win && !this.win.isDestroyed() ? this.win.webContents : null;
    const e = cleanInput(raw, VRB_W, VRB_H);
    if (!wc || !e) return;
    if (e.type === 'mouseWheel') wc.sendInputEvent({ type: 'mouseWheel', x: e.x, y: e.y, deltaX: 0, deltaY: -e.deltaY, canScroll: true });
    else if (e.type === 'mouseDown' || e.type === 'mouseUp') wc.sendInputEvent({ type: e.type, x: e.x, y: e.y, button: e.button ?? 'left', clickCount: 1 });
    else if (e.type === 'mouseMove') wc.sendInputEvent({ type: 'mouseMove', x: e.x, y: e.y });
    else if (e.type === 'keyDown' || e.type === 'keyUp' || e.type === 'char') wc.sendInputEvent({ type: e.type, keyCode: e.keyCode });
  }

  nav(action: string, text?: string): VrbState {
    const wc = this.win && !this.win.isDestroyed() ? this.win.webContents : null;
    if (!wc) return this.state();
    if (action === 'back' && wc.navigationHistory.canGoBack()) wc.navigationHistory.goBack();
    else if (action === 'forward' && wc.navigationHistory.canGoForward()) wc.navigationHistory.goForward();
    else if (action === 'reload') wc.reload();
    else if (action === 'home') void wc.loadURL(HOME_URL);
    else if (action === 'go' && typeof text === 'string') void wc.loadURL(toTarget(text));
    return this.state();
  }

  close(): void {
    if (this.pending) clearTimeout(this.pending);
    this.pending = null;
    if (this.win && !this.win.isDestroyed()) this.win.destroy();
    this.win = null;
  }
}
