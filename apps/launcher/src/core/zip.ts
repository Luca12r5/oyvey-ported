// Minimal ZIP reader (stored + deflate), enough to extract native libraries
// from LWJGL jars. Rejects path traversal ("zip slip").

import { readFile, mkdir, writeFile } from 'node:fs/promises';
import { dirname, join, normalize, sep } from 'node:path';
import { inflateRawSync } from 'node:zlib';

export interface ZipEntry { name: string; method: number; compressedSize: number; size: number; offset: number }

export function listEntries(buf: Buffer): ZipEntry[] {
  // End of central directory: signature 0x06054b50, search backwards (comment <= 64 KiB).
  let eocd = -1;
  for (let i = buf.length - 22; i >= Math.max(0, buf.length - 22 - 0xffff); i--) {
    if (buf.readUInt32LE(i) === 0x06054b50) { eocd = i; break; }
  }
  if (eocd < 0) throw new Error('not a zip file');
  const count = buf.readUInt16LE(eocd + 10);
  let p = buf.readUInt32LE(eocd + 16);
  const out: ZipEntry[] = [];
  for (let n = 0; n < count; n++) {
    if (buf.readUInt32LE(p) !== 0x02014b50) throw new Error('corrupt central directory');
    const method = buf.readUInt16LE(p + 10);
    const compressedSize = buf.readUInt32LE(p + 20);
    const size = buf.readUInt32LE(p + 24);
    const nameLen = buf.readUInt16LE(p + 28);
    const extraLen = buf.readUInt16LE(p + 30);
    const commentLen = buf.readUInt16LE(p + 32);
    const offset = buf.readUInt32LE(p + 42);
    const name = buf.subarray(p + 46, p + 46 + nameLen).toString('utf8');
    out.push({ name, method, compressedSize, size, offset });
    p += 46 + nameLen + extraLen + commentLen;
  }
  return out;
}

export function readEntry(buf: Buffer, e: ZipEntry): Buffer {
  if (buf.readUInt32LE(e.offset) !== 0x04034b50) throw new Error('corrupt local header');
  const nameLen = buf.readUInt16LE(e.offset + 26);
  const extraLen = buf.readUInt16LE(e.offset + 28);
  const start = e.offset + 30 + nameLen + extraLen;
  const data = buf.subarray(start, start + e.compressedSize);
  if (e.method === 0) return Buffer.from(data);
  if (e.method === 8) return inflateRawSync(data);
  throw new Error(`unsupported zip method ${e.method}`);
}

/** Extracts entries accepted by `filter` into `destDir`. Returns extracted names. */
export async function extract(zipPath: string, destDir: string, filter: (name: string) => boolean): Promise<string[]> {
  const buf = await readFile(zipPath);
  const root = normalize(destDir) + sep;
  const done: string[] = [];
  for (const e of listEntries(buf)) {
    if (e.name.endsWith('/') || !filter(e.name)) continue;
    const target = normalize(join(destDir, e.name));
    if (!target.startsWith(root)) throw new Error(`blocked path traversal in ${zipPath}: ${e.name}`);
    await mkdir(dirname(target), { recursive: true });
    await writeFile(target, readEntry(buf, e));
    done.push(e.name);
  }
  return done;
}

/** Natives: shared libraries only, no META-INF, honouring legacy exclude lists. */
export function nativeFilter(excludes: string[] = []): (name: string) => boolean {
  return (name) => !name.startsWith('META-INF/') && !excludes.some((x) => name.startsWith(x)) && /\.(dll|so|dylib|jnilib)$/i.test(name);
}
