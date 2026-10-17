// Now-playing bridge for the Spotify HUD: runs the same Windows media-session
// script as the in-game HUD (lego-client assets/spotifyhud/smtc.ps1) and
// forwards its JSON lines. Windows only; other systems report "unavailable".

import { spawn, type ChildProcessWithoutNullStreams } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

export interface SpotifyState {
  available: boolean;
  fatal: string | null;
  has: boolean;
  app: string;
  isSpotify: boolean;
  title: string;
  artist: string;
  playing: boolean;
  /** Progress in ms at local time `at`. */
  posMs: number;
  at: number;
  durMs: number;
  shuffle: boolean;
  repeat: 'off' | 'track' | 'context';
  volume: number;
  canShuffle: boolean;
  canRepeat: boolean;
  canSeek: boolean;
}

const EMPTY: SpotifyState = {
  available: false, fatal: null, has: false, app: '', isSpotify: false, title: '', artist: '', playing: false, posMs: 0, at: 0, durMs: 0,
  shuffle: false, repeat: 'off', volume: -1, canShuffle: false, canRepeat: false, canSeek: false,
};

const COMMANDS = /^(toggle|next|prev|mute|seek \d{1,9}|shuffle (true|false)|repeat [012]|vol \d{1,3})$/;

export class SpotifyBridge {
  private proc: ChildProcessWithoutNullStreams | null = null;
  private state: SpotifyState = { ...EMPTY };
  private stopped = true;
  private fails = 0;
  private artKey = '';
  private latency = 0;

  private readonly scriptSrc: string;
  private readonly workDir: string;
  private readonly emit: (channel: 'spotify' | 'spotify-art', data: unknown) => void;

  constructor(scriptSrc: string, workDir: string, emit: (channel: 'spotify' | 'spotify-art', data: unknown) => void) {
    this.scriptSrc = scriptSrc;
    this.workDir = workDir;
    this.emit = emit;
  }

  current(): SpotifyState {
    return this.state;
  }

  start(): void {
    if (!this.stopped) return;
    this.stopped = false;
    if (process.platform !== 'win32') {
      this.set({ ...EMPTY, fatal: 'Nur unter Windows verfügbar' });
      return;
    }
    this.run();
  }

  stop(): void {
    this.stopped = true;
    try { this.proc?.kill(); } catch { /* already gone */ }
    this.proc = null;
  }

  command(cmd: string): boolean {
    if (!COMMANDS.test(cmd) || !this.proc) return false;
    try { this.proc.stdin.write(`${cmd}\n`); return true; } catch { return false; }
  }

  private set(s: SpotifyState): void {
    this.state = s;
    this.emit('spotify', s);
  }

  /** Copies the script out of the asar so PowerShell can read it (with a UTF-8 BOM, as the client does). */
  private script(): string {
    mkdirSync(this.workDir, { recursive: true });
    const target = join(this.workDir, 'smtc.ps1');
    const body = Buffer.concat([Buffer.from([0xef, 0xbb, 0xbf]), readFileSync(this.scriptSrc)]);
    if (!existsSync(target) || !readFileSync(target).equals(body)) writeFileSync(target, body);
    return target;
  }

  private run(): void {
    if (this.stopped) return;
    const started = Date.now();
    let file: string;
    try { file = this.script(); } catch (e) { this.set({ ...EMPTY, fatal: `Skript fehlt: ${(e as Error).message}` }); return; }
    const root = process.env.SystemRoot;
    const ps = root && existsSync(join(root, 'System32', 'WindowsPowerShell', 'v1.0', 'powershell.exe')) ? join(root, 'System32', 'WindowsPowerShell', 'v1.0', 'powershell.exe') : 'powershell.exe';
    const p = spawn(ps, ['-NoLogo', '-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-WindowStyle', 'Hidden', '-File', file], { windowsHide: true });
    this.proc = p;
    this.set({ ...this.state, available: true, fatal: null });
    let buf = '';
    p.stdout.setEncoding('utf8');
    p.stdout.on('data', (chunk: string) => {
      buf += chunk;
      let i: number;
      while ((i = buf.indexOf('\n')) >= 0) {
        const line = buf.slice(0, i).replace(/^﻿/, '').trim();
        buf = buf.slice(i + 1);
        if (line) { try { this.handle(JSON.parse(line)); } catch { /* ignore a bad line */ } }
      }
    });
    p.stderr.on('data', () => { /* diagnostics only */ });
    const restart = () => {
      if (this.proc !== p) return;
      this.proc = null;
      this.set({ ...EMPTY });
      this.fails = Date.now() - started < 15_000 ? this.fails + 1 : 0;
      if (!this.stopped) setTimeout(() => this.run(), Math.min(60_000, 2000 * Math.max(1, this.fails)));
    };
    p.on('exit', restart);
    p.on('error', (e) => { this.set({ ...EMPTY, fatal: `PowerShell nicht startbar: ${e.message}` }); restart(); });
  }

  private handle(m: Record<string, unknown>): void {
    const now = Date.now();
    const sent = Number(m.now) || 0;
    if (sent > 0) {
      const lat = Math.max(0, Math.min(2000, now - sent));
      this.latency = this.latency === 0 ? lat : (this.latency * 7 + lat) / 8;
    }
    switch (m.t) {
      case 'fatal': this.set({ ...this.state, fatal: String(m.msg ?? 'Fehler') }); break;
      case 'thumb': {
        const key = String(m.key ?? '');
        const data = typeof m.data === 'string' ? m.data : '';
        this.artKey = key;
        const mime = data.startsWith('iVBOR') ? 'image/png' : data.startsWith('R0lG') ? 'image/gif' : 'image/jpeg';
        this.emit('spotify-art', data ? { key, url: `data:${mime};base64,${data}` } : { key, url: null });
        break;
      }
      case 'state': {
        if (!m.has) { this.set({ ...this.state, available: true, has: false, title: '', artist: '', playing: false }); break; }
        const upd = Number(m.upd) || 0;
        const at = upd > 946684800000 && upd <= now + 1000 ? upd : now - this.latency;
        const rep = Number(m.repeat);
        const vol = Number(m.vol ?? -1);
        const muted = vol >= 1000;
        const title = String(m.title ?? '');
        const artist = String(m.artist ?? '');
        const app = String(m.app ?? '');
        if (this.artKey && this.artKey !== `${title}|${artist}`) { this.artKey = ''; this.emit('spotify-art', { key: '', url: null }); }
        this.set({
          available: true, fatal: null, has: true, app, isSpotify: app.toLowerCase().includes('spotify'), title, artist,
          playing: Number(m.status) === 4, posMs: Number(m.pos) || 0, at, durMs: Math.max(0, Number(m.dur) || 0),
          shuffle: m.shuffle === true, repeat: rep === 1 ? 'track' : rep === 2 ? 'context' : 'off',
          volume: vol < 0 ? -1 : muted ? 0 : Math.min(100, vol), canShuffle: !!m.cShuf, canRepeat: !!m.cRep, canSeek: !!m.cSeek,
        });
        break;
      }
      default: break;
    }
  }
}
