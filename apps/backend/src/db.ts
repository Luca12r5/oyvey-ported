// SQLite access via Node's built-in node:sqlite (no native dependency).
// WAL mode + foreign keys; numbered .sql migrations applied in order.

import { DatabaseSync, type SQLInputValue } from 'node:sqlite';
import { mkdirSync, readdirSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';

export type Params = Record<string, SQLInputValue>;
export type Row = Record<string, SQLInputValue>;

export class Db {
  readonly raw: DatabaseSync;
  private depth = 0;

  constructor(file: string) {
    if (file !== ':memory:') mkdirSync(dirname(file), { recursive: true });
    this.raw = new DatabaseSync(file);
    this.raw.exec('PRAGMA journal_mode = WAL; PRAGMA foreign_keys = ON; PRAGMA busy_timeout = 5000;');
  }

  run(sql: string, params: Params = {}): { changes: number; lastInsertRowid: number } {
    const r = this.raw.prepare(sql).run(params);
    return { changes: Number(r.changes), lastInsertRowid: Number(r.lastInsertRowid) };
  }

  get<T = Row>(sql: string, params: Params = {}): T | undefined {
    return this.raw.prepare(sql).get(params) as T | undefined;
  }

  all<T = Row>(sql: string, params: Params = {}): T[] {
    return this.raw.prepare(sql).all(params) as T[];
  }

  /**
   * Runs `fn` atomically. Nested calls use savepoints so services can compose.
   * node:sqlite is synchronous, so a transaction never interleaves with
   * another request on the same connection.
   */
  tx<T>(fn: () => T): T {
    const sp = `sp${this.depth}`;
    this.raw.exec(this.depth === 0 ? 'BEGIN IMMEDIATE' : `SAVEPOINT ${sp}`);
    this.depth++;
    try {
      const out = fn();
      this.depth--;
      this.raw.exec(this.depth === 0 ? 'COMMIT' : `RELEASE ${sp}`);
      return out;
    } catch (e) {
      this.depth--;
      this.raw.exec(this.depth === 0 ? 'ROLLBACK' : `ROLLBACK TO ${sp}; RELEASE ${sp}`);
      throw e;
    }
  }

  migrate(dir = join(dirname(new URL(import.meta.url).pathname), 'migrations')): number {
    this.raw.exec('CREATE TABLE IF NOT EXISTS schema_migrations (name TEXT PRIMARY KEY, applied_at INTEGER NOT NULL)');
    const done = new Set(this.all<{ name: string }>('SELECT name FROM schema_migrations').map((r) => r.name));
    let applied = 0;
    for (const name of readdirSync(dir).filter((f) => /^\d+_.+\.sql$/.test(f)).sort()) {
      if (done.has(name)) continue;
      const sql = readFileSync(join(dir, name), 'utf8');
      this.tx(() => {
        this.raw.exec(sql);
        this.run('INSERT INTO schema_migrations(name, applied_at) VALUES (:n, :t)', { n: name, t: Date.now() });
      });
      applied++;
    }
    return applied;
  }

  /** Consistent online backup (VACUUM INTO writes a clean copy). */
  backup(target: string): void {
    mkdirSync(dirname(target), { recursive: true });
    this.raw.prepare('VACUUM INTO ?').run(target);
  }

  close(): void {
    this.raw.close();
  }
}
