// Starts the game as a normal visible child process (no shell, no hidden
// window tricks), streams its log and reports crashes.

import { spawn, type ChildProcess } from 'node:child_process';
import { createWriteStream } from 'node:fs';
import { mkdir, readdir, stat } from 'node:fs/promises';
import { join } from 'node:path';

export interface GameProcess {
  child: ChildProcess;
  logFile: string;
  exited: Promise<{ code: number | null; crashReport: string | null; durationMs: number }>;
}

export async function startGame(javaPath: string, args: string[], gameDir: string, logsDir: string, onLine: (line: string, stream: 'out' | 'err') => void): Promise<GameProcess> {
  await mkdir(gameDir, { recursive: true });
  await mkdir(logsDir, { recursive: true });
  const logFile = join(logsDir, `launch-${new Date().toISOString().replace(/[:.]/g, '-')}.log`);
  const log = createWriteStream(logFile);
  const started = Date.now();
  const child = spawn(javaPath, args, { cwd: gameDir, stdio: ['ignore', 'pipe', 'pipe'], shell: false, windowsHide: false });
  const pipe = (stream: NodeJS.ReadableStream | null, kind: 'out' | 'err') => {
    let buf = '';
    stream?.setEncoding('utf8');
    stream?.on('data', (chunk: string) => {
      log.write(chunk);
      buf += chunk;
      let i;
      while ((i = buf.indexOf('\n')) >= 0) {
        onLine(buf.slice(0, i).replace(/\r$/, ''), kind);
        buf = buf.slice(i + 1);
      }
    });
  };
  pipe(child.stdout, 'out');
  pipe(child.stderr, 'err');
  const exited = new Promise<{ code: number | null; crashReport: string | null; durationMs: number }>((resolve, reject) => {
    child.once('error', (e) => {
      log.end();
      reject(e);
    });
    child.once('exit', async (code) => {
      log.end();
      const crashReport = code === 0 ? null : await newestCrashReport(gameDir, started);
      resolve({ code, crashReport, durationMs: Date.now() - started });
    });
  });
  return { child, logFile, exited };
}

export async function newestCrashReport(gameDir: string, since: number): Promise<string | null> {
  const dir = join(gameDir, 'crash-reports');
  try {
    let best: { file: string; mtime: number } | null = null;
    for (const name of await readdir(dir)) {
      if (!name.endsWith('.txt')) continue;
      const st = await stat(join(dir, name));
      if (st.mtimeMs >= since && (!best || st.mtimeMs > best.mtime)) best = { file: join(dir, name), mtime: st.mtimeMs };
    }
    return best?.file ?? null;
  } catch {
    return null;
  }
}

/** Short human hint for common crash causes found in a log or crash report. */
export function diagnose(text: string): string | null {
  const rules: [RegExp, string][] = [
    [/OutOfMemoryError/, 'Zu wenig Arbeitsspeicher für Minecraft. Erhöhe den RAM im Profil (z. B. 4096 MB).'],
    [/Incompatible mods found|requires .*fabric-api/i, 'Ein Mod ist inkompatibel oder Fabric API fehlt. Prüfe die Mods im Profil.'],
    [/UnsupportedClassVersionError/, 'Falsche Java-Version. Der Launcher installiert automatisch die passende – nutze das Standard-Java.'],
    [/GLFW error|No OpenGL context|WGL: The driver does not appear to support OpenGL/i, 'Grafiktreiber-Problem (OpenGL). Aktualisiere den Grafiktreiber.'],
    [/Mixin apply.*failed|MixinApplyError/i, 'Ein Mod konnte nicht geladen werden (Mixin-Fehler). Entferne zuletzt hinzugefügte Mods.'],
    [/Invalid session|Failed to login: Invalid session/i, 'Die Minecraft-Sitzung ist abgelaufen. Melde dich im Launcher neu an.'],
  ];
  for (const [re, hint] of rules) if (re.test(text)) return hint;
  return null;
}

/**
 * Turns the game's log4j XML output (used when the official logging config is
 * passed) into readable lines: "[12:34:56] [Render thread/INFO]: message".
 * Plain lines pass through unchanged. Returns null for pure XML markup.
 */
export function createLogCleaner(): (line: string) => string | null {
  let head = '';
  let buf: string[] | null = null;
  return (line: string) => {
    const ev = /<log4j:Event\b[^>]*\btimestamp="(\d+)"[^>]*\blevel="(\w+)"[^>]*\bthread="([^"]*)"/.exec(line);
    if (ev) {
      const d = new Date(Number(ev[1]));
      head = `[${d.toTimeString().slice(0, 8)}] [${ev[3]}/${ev[2]}]: `;
      return null;
    }
    if (buf) {
      const end = line.indexOf(']]>');
      if (end < 0) { buf.push(line); return null; }
      buf.push(line.slice(0, end));
      const text = buf.join('\n');
      buf = null;
      return text.trim() ? head + text.trim() : null;
    }
    const m = /<log4j:(Message|Throwable)><!\[CDATA\[(.*)$/.exec(line);
    if (m) {
      const rest = m[2]!;
      const end = rest.indexOf(']]>');
      if (end >= 0) return rest.slice(0, end).trim() ? head + rest.slice(0, end) : null;
      buf = [rest];
      return null;
    }
    if (/^\s*<\/?log4j:[A-Za-z]+[^>]*>\s*$/.test(line)) return null;
    return line;
  };
}
