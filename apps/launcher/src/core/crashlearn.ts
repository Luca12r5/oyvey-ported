// The crash doctor learns on this PC: every crash gets a signature (exception, the first
// frame of mod code, the failing mixin), and when a fix is applied the next game start tells
// whether it helped (clean exit or a different crash) or not (the same crash again). Fixes
// that helped before come first and are offered again even if no rule matches; nothing
// leaves the PC.

import type { Diagnosis, Fix } from './crashdoc.ts';

interface Score { ok: number; fail: number; fix: Fix; label?: string; title: string }
export interface LearnDb {
  v: 1;
  sigs: Record<string, { seen: number; last: number; fixes: Record<string, Score> }>;
  /** A fix applied after a crash, waiting for the next start of that profile. */
  pending: Record<string, { sig: string; key: string; at: number }>;
}
export type Learned = Diagnosis & { learned?: { ok: number; fail: number } };

export const emptyDb = (): LearnDb => ({ v: 1, sigs: {}, pending: {} });

const PLATFORM = /^(java|javax|jdk|sun|com\.sun|net\.minecraft|com\.mojang|org\.spongepowered|net\.fabricmc|cpw|net\.neoforged|net\.minecraftforge|org\.lwjgl|io\.netty|com\.google|org\.apache|it\.unimi|org\.slf4j|kotlin)\./;
const norm = (s: string) => s.replace(/\$\$?[\w$]*/g, '').replace(/\d+/g, '#').trim();

/** Stable signature of a crash, or null when the text has no recognisable error. */
export function crashSignature(text: string): string | null {
  const causes = [...text.matchAll(/Caused by: ([\w.$]+(?:Exception|Error|Throwable))/g)].map((m) => m[1]!);
  const ex = causes.at(-1) ?? /(?:^|\n)\s*(?:Exception in thread "[^"]*" )?([\w.$]+(?:Exception|Error))(?::|\n)/.exec(text)?.[1] ?? null;
  const mixin = /Mixin \[([\w.$/-]+?)(?:\.json)?:([\w.$]+)\] from mod ([\w-]+)/.exec(text) ?? /from mod ([\w-]+)/.exec(text);
  let frame: string | null = null;
  for (const m of text.matchAll(/\n\s+at ([\w.$]+)\.[\w$<>]+\(/g)) {
    if (!PLATFORM.test(m[1]!)) { frame = m[1]!.split('.').slice(0, 3).join('.'); break; }
  }
  const parts = [ex && norm(ex), frame && norm(frame), mixin && `mod:${mixin.at(-1)}`].filter(Boolean);
  return parts.length ? parts.join('|').slice(0, 200) : null;
}

/** The same fix across versions of a mod file (sodium-0.6.1.jar ≙ sodium-0.6.2.jar). */
export function fixKey(fix: Fix): string {
  switch (fix.kind) {
    case 'disable-mod': return `disable:${fix.filename.toLowerCase().replace(/\.jar(\.disabled)?$/, '').replace(/[-_+]?(mc)?\d[\w.+-]*$/, '')}`;
    case 'install-mod': return `install:${fix.slug}`;
    case 'set-memory': return `memory:${Math.round(fix.mb / 1024)}`;
    default: return fix.kind;
  }
}

/** Sorts the rule results by what helped before and adds fixes that helped for this crash. */
export function rank(db: LearnDb, sig: string | null, found: Diagnosis[], mods: { filename: string; enabled: boolean }[] = []): Learned[] {
  const entry = sig ? db.sigs[sig] : undefined;
  const out: Learned[] = found.map((d) => {
    const s = d.fix && entry?.fixes[fixKey(d.fix)];
    return s ? { ...d, learned: { ok: s.ok, fail: s.fail } } : d;
  });
  for (const [key, s] of Object.entries(entry?.fixes ?? {})) {
    if (s.ok <= s.fail || out.some((d) => d.fix && fixKey(d.fix) === key)) continue;
    let fix = s.fix;
    if (fix.kind === 'disable-mod') {
      // The file name may have changed with an update: find the current file of that mod.
      const cur = mods.find((m) => m.enabled && fixKey({ kind: 'disable-mod', filename: m.filename }) === key);
      if (!cur) continue;
      fix = { kind: 'disable-mod', filename: cur.filename };
    }
    out.push({ id: `learned-${key}`, title: s.title, detail: 'Diese Lösung hat bei genau diesem Absturz auf deinem PC schon geholfen.', fix, fixLabel: s.label, confidence: 'high', learned: { ok: s.ok, fail: s.fail } });
  }
  const score = (d: Learned) => (d.learned ? d.learned.ok - d.learned.fail * 2 : 0) + (d.confidence === 'high' ? 0.5 : 0);
  return out.map((d, i) => ({ d, i })).sort((a, b) => score(b.d) - score(a.d) || a.i - b.i).map((x) => x.d);
}

/** Remembers a crash (counts how often a signature was seen). */
export function recordCrash(db: LearnDb, sig: string | null, now = Date.now()): void {
  if (!sig) return;
  const e = (db.sigs[sig] ??= { seen: 0, last: 0, fixes: {} });
  e.seen++;
  e.last = now;
}

export function recordApplied(db: LearnDb, profileId: string, sig: string | null, d: Pick<Diagnosis, 'fix' | 'fixLabel' | 'title'>, now = Date.now()): void {
  if (!sig || !d.fix) return;
  const e = (db.sigs[sig] ??= { seen: 1, last: now, fixes: {} });
  const key = fixKey(d.fix);
  e.fixes[key] ??= { ok: 0, fail: 0, fix: d.fix, label: d.fixLabel, title: d.title };
  db.pending[profileId] = { sig, key, at: now };
}

export function recordUndo(db: LearnDb, profileId: string): void {
  delete db.pending[profileId];
}

/** After the next game of a profile ends: did the applied fix help? */
export function recordOutcome(db: LearnDb, profileId: string, code: number, sig: string | null): void {
  const p = db.pending[profileId];
  if (!p) return;
  delete db.pending[profileId];
  const s = db.sigs[p.sig]?.fixes[p.key];
  if (!s) return;
  // A clean exit or a different crash means the old problem is gone.
  if (code === 0 || sig !== p.sig) s.ok++;
  else s.fail++;
}

/** Keeps the file small: at most 300 signatures, the most recent ones. */
export function prune(db: LearnDb, max = 300): LearnDb {
  const keys = Object.keys(db.sigs);
  if (keys.length <= max) return db;
  keys.sort((a, b) => db.sigs[b]!.last - db.sigs[a]!.last);
  for (const k of keys.slice(max)) delete db.sigs[k];
  return db;
}

export function parseDb(raw: string): LearnDb {
  try {
    const d = JSON.parse(raw) as LearnDb;
    if (d && d.v === 1 && typeof d.sigs === 'object' && typeof d.pending === 'object') return d;
  } catch { /* broken file: start over */ }
  return emptyDb();
}
