// Real web browser for the VR lobby: an invisible offscreen Chromium window in
// its own session. Its frames are streamed to the renderer (JPEG) and shown on
// a 3D panel; mouse/keyboard input comes back and is injected into the page.
// Safety: https only, SafeSearch forced, no downloads, no permissions, no
// popups, no access to the launcher (separate sandboxed session, no preload).

import { BrowserWindow, session } from 'electron';
import { allowed, cleanInput, enforceSafeSearch, HOME_URL, toTarget } from '../core/vrweb.ts';

export const VRB_W = 1600;
export const VRB_H = 1000;
const MAX_TABS = 4;
const PARTITION = 'persist:vrbrowser';

export interface VrbState { url: string; title: string; canBack: boolean; canForward: boolean; loading: boolean; zoom?: number; tabs?: { title: string; url: string }[]; active?: number }

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
  private tabs: BrowserWindow[] = [];
  private active = 0;
  private lastFrame = 0;
  private pending: NodeJS.Timeout | null = null;
  /** Visited pages of this session (for address-bar suggestions; never leaves the PC). */
  private history: { title: string; url: string }[] = [];
  private readonly send: (channel: string, data: unknown) => void;

  constructor(send: (channel: string, data: unknown) => void) {
    this.send = send;
  }

  private get win(): BrowserWindow | null {
    const w = this.tabs[this.active];
    return w && !w.isDestroyed() ? w : null;
  }

  private createTab(url = HOME_URL): BrowserWindow {
    prepareSession();
    const win = new BrowserWindow({
      show: false, width: VRB_W, height: VRB_H, useContentSize: true,
      webPreferences: { offscreen: true, partition: PARTITION, sandbox: true, contextIsolation: true, nodeIntegration: false, webviewTag: false, spellcheck: false, backgroundThrottling: false },
    });
    const wc = win.webContents;
    wc.setFrameRate(30);
    wc.setWindowOpenHandler(({ url: u }) => {
      // Popups open as a new tab (if there is room), otherwise in place.
      if (allowed(u)) { if (this.tabs.length < MAX_TABS) this.openTab(u); else void wc.loadURL(u); }
      return { action: 'deny' };
    });
    wc.on('will-navigate', (e, u) => { if (!allowed(u)) e.preventDefault(); });
    let latest: Electron.NativeImage | null = null;
    const emit = () => {
      this.lastFrame = Date.now();
      this.pending = null;
      if (latest && !win.isDestroyed() && win === this.win) this.send('vrb-frame', latest.toJPEG(84));
      latest = null;
    };
    wc.on('paint', (_e, _dirty, image) => {
      if (win !== this.win) return; // background tabs don't stream
      latest = image;
      const now = Date.now();
      if (now - this.lastFrame >= 33) emit();
      else if (!this.pending) this.pending = setTimeout(emit, 33 - (now - this.lastFrame));
    });
    for (const ev of ['did-navigate', 'did-navigate-in-page', 'page-title-updated', 'did-start-loading', 'did-stop-loading'] as const) {
      wc.on(ev as 'did-stop-loading', () => {
        if (ev === 'page-title-updated' || ev === 'did-stop-loading') this.remember(wc.getTitle(), wc.getURL());
        this.send('vrb-state', this.state());
      });
    }
    wc.on('did-fail-load', (_e, code, desc, failed, isMain) => {
      if (!isMain || code === -3) return;
      const safe = (v: string) => v.replace(/[&<>"']/g, (ch) => `&#${ch.charCodeAt(0)};`);
      const page = `<body style="margin:0;height:100vh;display:grid;place-items:center;background:#0b0910;color:#ece6f4;font:26px system-ui,sans-serif;text-align:center"><div><h1 style="color:#ff7a1a;font-weight:600">Seite nicht erreichbar</h1><p>${safe(failed)}</p><p style="color:#9a90ab">${safe(desc)} (${code})</p><p style="color:#9a90ab">Prüfe die Internetverbindung und tippe auf „Neu laden“.</p></div></body>`;
      void wc.executeJavaScript(`document.documentElement.innerHTML = ${JSON.stringify(page)}`).catch(() => {});
      this.send('vrb-state', { ...this.state(), title: 'Seite nicht erreichbar', loading: false });
    });
    wc.on('render-process-gone', () => this.send('vrb-state', { ...this.state(), title: 'Seite abgestürzt – neu laden' }));
    void wc.loadURL(url);
    return win;
  }

  private remember(title: string, url: string): void {
    if (!url.startsWith('https://') || !title) return;
    this.history = [{ title: title.slice(0, 120), url }, ...this.history.filter((h) => h.url !== url)].slice(0, 200);
  }

  private openTab(url?: string): void {
    this.tabs.push(this.createTab(url));
    this.active = this.tabs.length - 1;
    this.send('vrb-state', this.state());
  }

  open(): { w: number; h: number; state: VrbState } {
    this.tabs = this.tabs.filter((w) => !w.isDestroyed());
    if (!this.tabs.length) { this.tabs = [this.createTab()]; this.active = 0; }
    this.win?.webContents.focus();
    return { w: VRB_W, h: VRB_H, state: this.state() };
  }

  state(): VrbState {
    const wc = this.win?.webContents ?? null;
    const tabs = this.tabs.filter((w) => !w.isDestroyed()).map((w) => ({ title: w.webContents.getTitle(), url: w.webContents.getURL() }));
    if (!wc) return { url: '', title: '', canBack: false, canForward: false, loading: false, tabs, active: this.active };
    return { url: wc.getURL(), title: wc.getTitle(), canBack: wc.navigationHistory.canGoBack(), canForward: wc.navigationHistory.canGoForward(), loading: wc.isLoading(), zoom: Math.round(wc.getZoomFactor() * 100) / 100, tabs, active: this.active };
  }

  /** Suggestions from this session's history (local only). */
  suggest(q: string): { title: string; url: string }[] {
    const k = q.trim().toLowerCase();
    if (k.length < 2) return [];
    return this.history.filter((h) => h.title.toLowerCase().includes(k) || h.url.toLowerCase().includes(k)).slice(0, 5);
  }

  input(raw: unknown): void {
    const wc = this.win?.webContents ?? null;
    const e = cleanInput(raw, VRB_W, VRB_H);
    if (!wc || !e) return;
    if (e.type === 'mouseWheel') wc.sendInputEvent({ type: 'mouseWheel', x: e.x, y: e.y, deltaX: 0, deltaY: -e.deltaY, canScroll: true });
    else if (e.type === 'mouseDown' || e.type === 'mouseUp') wc.sendInputEvent({ type: e.type, x: e.x, y: e.y, button: e.button ?? 'left', clickCount: 1 });
    else if (e.type === 'mouseMove') wc.sendInputEvent({ type: 'mouseMove', x: e.x, y: e.y });
    else if (e.type === 'keyDown' || e.type === 'keyUp' || e.type === 'char') wc.sendInputEvent({ type: e.type, keyCode: e.keyCode });
  }

  nav(action: string, text?: string): VrbState {
    if (action === 'newTab') { if (this.tabs.length < MAX_TABS) this.openTab(); return this.state(); }
    if (action === 'closeTab') {
      const w = this.tabs[this.active];
      if (w && this.tabs.length > 1) { this.tabs.splice(this.active, 1); w.destroy(); this.active = Math.min(this.active, this.tabs.length - 1); this.win?.webContents.invalidate(); }
      return this.state();
    }
    if (action === 'tab' && typeof text === 'string') {
      const i = Number(text);
      if (Number.isInteger(i) && i >= 0 && i < this.tabs.length) { this.active = i; this.win?.webContents.invalidate(); }
      return this.state();
    }
    const wc = this.win?.webContents ?? null;
    if (!wc) return this.state();
    if (action === 'back' && wc.navigationHistory.canGoBack()) wc.navigationHistory.goBack();
    else if (action === 'forward' && wc.navigationHistory.canGoForward()) wc.navigationHistory.goForward();
    else if (action === 'reload') wc.reload();
    else if (action === 'home') void wc.loadURL(HOME_URL);
    else if (action === 'go' && typeof text === 'string') void wc.loadURL(toTarget(text));
    else if (action === 'zoomIn') wc.setZoomFactor(Math.min(3, wc.getZoomFactor() + 0.15));
    else if (action === 'zoomOut') wc.setZoomFactor(Math.max(0.5, wc.getZoomFactor() - 0.15));
    else if (action === 'zoomReset') wc.setZoomFactor(1);
    return this.state();
  }

  close(): void {
    if (this.pending) clearTimeout(this.pending);
    this.pending = null;
    for (const w of this.tabs) if (!w.isDestroyed()) w.destroy();
    this.tabs = [];
    this.active = 0;
  }
}
