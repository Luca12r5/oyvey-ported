// Shared test helpers: an in-memory fetch and a minimal stored (uncompressed) zip writer.
import { crc32 } from 'node:zlib';

export function fakeFetch(files: Record<string, Buffer | string | object>, calls: string[] = []): typeof fetch {
  return (async (url: string | URL, init?: RequestInit) => {
    calls.push(String(url));
    const raw = files[String(url)];
    if (raw === undefined) return new Response('nope', { status: 404 });
    const data = Buffer.isBuffer(raw) ? raw : Buffer.from(typeof raw === 'string' ? raw : JSON.stringify(raw));
    const range = (init?.headers as Record<string, string> | undefined)?.Range;
    if (range) {
      const start = Number(/bytes=(\d+)-/.exec(range)![1]);
      return new Response(new Uint8Array(data.subarray(start)), { status: 206 });
    }
    return new Response(new Uint8Array(data), { status: 200 });
  }) as typeof fetch;
}

export function storedZip(entries: Record<string, string | Buffer>): Buffer {
  const locals: Buffer[] = [];
  const centrals: Buffer[] = [];
  let offset = 0;
  for (const [name, content] of Object.entries(entries)) {
    const data = Buffer.from(content);
    const nameBuf = Buffer.from(name);
    const crc = crc32(data);
    const lh = Buffer.alloc(30);
    lh.writeUInt32LE(0x04034b50, 0); lh.writeUInt16LE(20, 4); lh.writeUInt32LE(crc, 14); lh.writeUInt32LE(data.length, 18); lh.writeUInt32LE(data.length, 22); lh.writeUInt16LE(nameBuf.length, 26);
    const ch = Buffer.alloc(46);
    ch.writeUInt32LE(0x02014b50, 0); ch.writeUInt16LE(20, 4); ch.writeUInt16LE(20, 6); ch.writeUInt32LE(crc, 16); ch.writeUInt32LE(data.length, 20); ch.writeUInt32LE(data.length, 24); ch.writeUInt16LE(nameBuf.length, 28); ch.writeUInt32LE(offset, 42);
    locals.push(lh, nameBuf, data);
    centrals.push(ch, nameBuf);
    offset += 30 + nameBuf.length + data.length;
  }
  const cd = Buffer.concat(centrals);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0); end.writeUInt16LE(Object.keys(entries).length, 8); end.writeUInt16LE(Object.keys(entries).length, 10); end.writeUInt32LE(cd.length, 12); end.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, cd, end]);
}

