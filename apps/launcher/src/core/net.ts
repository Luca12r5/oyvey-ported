// HTTP helpers with timeouts and limited retries for idempotent GETs.

export type FetchLike = typeof fetch;

export const USER_AGENT = 'LEGO-Launcher/0.1 (+https://github.com/Luca12r5/oyvey-ported)';

export class HttpStatusError extends Error {
  readonly status: number;
  readonly body: string;
  constructor(url: string, status: number, body: string) {
    super(`${status} from ${new URL(url).host}${new URL(url).pathname}`);
    this.status = status;
    this.body = body;
  }
}

export async function fetchJson<T>(f: FetchLike, url: string, init: RequestInit = {}, opts: { retries?: number; timeoutMs?: number } = {}): Promise<T> {
  const retries = (init.method ?? 'GET') === 'GET' ? (opts.retries ?? 2) : 0;
  let lastErr: unknown;
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      const res = await f(url, {
        ...init,
        headers: { 'User-Agent': USER_AGENT, Accept: 'application/json', ...(init.headers as Record<string, string> | undefined) },
        signal: AbortSignal.timeout(opts.timeoutMs ?? 20_000),
      });
      const text = await res.text();
      if (!res.ok) throw new HttpStatusError(url, res.status, text);
      return (text ? JSON.parse(text) : null) as T;
    } catch (e) {
      lastErr = e;
      if (e instanceof HttpStatusError && e.status < 500 && e.status !== 429) throw e;
      if (attempt < retries) await new Promise((r) => setTimeout(r, 500 * 2 ** attempt));
    }
  }
  throw lastErr;
}

export function form(data: Record<string, string>): string {
  return new URLSearchParams(data).toString();
}
