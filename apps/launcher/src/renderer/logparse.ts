// Turns raw game/launcher output lines into structured console entries with a
// timestamp, a level and the source, so the console can colour and filter them.

export type Level = 'DEBUG' | 'INFO' | 'WARN' | 'ERROR' | 'FATAL';
export interface LogEntry { time: string; level: Level; source: string; text: string }

const MC = /^\[(\d{2}:\d{2}:\d{2})\] \[([^\]/]+)\/(TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL|SEVERE)\]:? ?([\s\S]*)$/;
const TAGGED = /^\[(installer|launcher)\] ([\s\S]*)$/;

function now(): string {
  return new Date().toTimeString().slice(0, 8);
}

function normalise(level: string): Level {
  if (level === 'TRACE') return 'DEBUG';
  if (level === 'WARNING') return 'WARN';
  if (level === 'SEVERE') return 'ERROR';
  return level as Level;
}

/** Guesses a level for lines without a log4j header (stack traces, plain stderr). */
function guess(text: string, stream: string): Level {
  if (/^\s*(at |Caused by:|\.\.\. \d+ more)/.test(text) || /(Exception|Error)(:|\s|$)/.test(text)) return 'ERROR';
  if (/\bWARN(ING)?\b/i.test(text)) return 'WARN';
  return stream === 'err' ? 'WARN' : 'INFO';
}

export function parseLine(line: string, stream: string, time: () => string = now): LogEntry {
  const m = MC.exec(line);
  if (m) return { time: m[1]!, level: normalise(m[3]!), source: m[2]!, text: m[4]! };
  const tag = TAGGED.exec(line);
  if (tag) return { time: time(), level: /fehl|fail|error/i.test(tag[2]!) ? 'ERROR' : 'INFO', source: tag[1]!, text: tag[2]! };
  return { time: time(), level: guess(line, stream), source: stream === 'err' ? 'stderr' : 'game', text: line };
}

export function isProblem(e: LogEntry): boolean {
  return e.level === 'ERROR' || e.level === 'FATAL';
}
