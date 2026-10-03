// Download queue: HTTPS only, hash verification, resumable .part files,
// atomic rename, bounded concurrency and progress reporting.

import { createHash } from 'node:crypto';
import { createReadStream } from 'node:fs';
import { mkdir, open, rename, rm, stat } from 'node:fs/promises';
import { dirname } from 'node:path';
import { USER_AGENT, type FetchLike } from './net.ts';

export interface DownloadTask {
  url: string;
  dest: string;
  /** Expected hash; at least one should be given for anything executable. */
  sha1?: string | null;
  sha256?: string | null;
  sha512?: string | null;
  size?: number | null;
  label?: string;
}

export interface Progress {
  done: number;
  total: number;
  bytes: number;
  totalBytes: number;
  current?: string;
}

export class IntegrityError extends Error {}

export async function hashFile(path: string, algo: 'sha1' | 'sha256' | 'sha512'): Promise<string> {
  const h = createHash(algo);
  for await (const chunk of createReadStream(path)) h.update(chunk as Buffer);
  return h.digest('hex');
}

function expected(t: DownloadTask): [('sha1' | 'sha256' | 'sha512'), string] | null {
  if (t.sha512) return ['sha512', t.sha512.toLowerCase()];
  if (t.sha256) return ['sha256', t.sha256.toLowerCase()];
  if (t.sha1) return ['sha1', t.sha1.toLowerCase()];
  return null;
}

/** True when the file exists and matches the expected hash (or size if no hash). */
export async function isValid(t: DownloadTask): Promise<boolean> {
  let st;
  try {
    st = await stat(t.dest);
  } catch {
    return false;
  }
  if (t.size && st.size !== t.size) return false;
  const exp = expected(t);
  if (!exp) return st.size > 0;
  return (await hashFile(t.dest, exp[0])) === exp[1];
}

async function fetchOne(f: FetchLike, t: DownloadTask, onBytes: (n: number) => void, signal?: AbortSignal): Promise<void> {
  if (!t.url.startsWith('https://')) throw new IntegrityError(`refusing non-HTTPS download ${t.url}`);
  await mkdir(dirname(t.dest), { recursive: true });
  const part = `${t.dest}.part`;
  let offset = 0;
  try {
    offset = (await stat(part)).size;
  } catch {
    offset = 0;
  }
  const headers: Record<string, string> = { 'User-Agent': USER_AGENT };
  if (offset > 0) headers.Range = `bytes=${offset}-`;
  const res = await f(t.url, { headers, signal: signal ?? AbortSignal.timeout(120_000) });
  if (res.status === 416) {
    // Server says our partial file is complete or invalid; restart cleanly.
    await rm(part, { force: true });
    return fetchOne(f, t, onBytes, signal);
  }
  if (!res.ok || !res.body) throw new Error(`HTTP ${res.status} for ${t.url}`);
  const append = res.status === 206 && offset > 0;
  const fh = await open(part, append ? 'a' : 'w');
  try {
    const reader = res.body.getReader();
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      await fh.write(value);
      onBytes(value.byteLength);
    }
  } finally {
    await fh.close();
  }
  const exp = expected(t);
  if (exp) {
    const got = await hashFile(part, exp[0]);
    if (got !== exp[1]) {
      await rm(part, { force: true });
      throw new IntegrityError(`${exp[0]} mismatch for ${t.label ?? t.url}: expected ${exp[1]}, got ${got}`);
    }
  } else if (t.size) {
    const st = await stat(part);
    if (st.size !== t.size) {
      await rm(part, { force: true });
      throw new IntegrityError(`size mismatch for ${t.label ?? t.url}`);
    }
  }
  await rename(part, t.dest);
}

export async function downloadAll(
  f: FetchLike,
  tasks: DownloadTask[],
  opts: { concurrency?: number; retries?: number; onProgress?: (p: Progress) => void; signal?: AbortSignal } = {},
): Promise<{ downloaded: number; skipped: number }> {
  const unique = [...new Map(tasks.map((t) => [t.dest, t])).values()];
  const progress: Progress = { done: 0, total: unique.length, bytes: 0, totalBytes: unique.reduce((s, t) => s + (t.size ?? 0), 0) };
  let downloaded = 0;
  let skipped = 0;
  let i = 0;
  const report = () => opts.onProgress?.({ ...progress });
  const worker = async () => {
    while (i < unique.length) {
      if (opts.signal?.aborted) throw new Error('cancelled');
      const t = unique[i++]!;
      progress.current = t.label ?? t.dest;
      if (await isValid(t)) {
        skipped++;
        progress.bytes += t.size ?? 0;
      } else {
        let attempt = 0;
        for (;;) {
          try {
            await fetchOne(f, t, (n) => { progress.bytes += n; }, opts.signal);
            downloaded++;
            break;
          } catch (e) {
            if (++attempt > (opts.retries ?? 3) || opts.signal?.aborted) throw e;
            await new Promise((r) => setTimeout(r, 400 * 2 ** attempt));
          }
        }
      }
      progress.done++;
      report();
    }
  };
  await Promise.all(Array.from({ length: Math.min(opts.concurrency ?? 8, unique.length || 1) }, worker));
  return { downloaded, skipped };
}
