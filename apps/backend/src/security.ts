// Token generation/hashing, rate limiting and input validation helpers.

import { createHash, randomBytes, timingSafeEqual } from 'node:crypto';
import { badRequest, HttpError } from './http.ts';

export function randomToken(bytes = 32): string {
  return randomBytes(bytes).toString('base64url');
}

export function newId(prefix = ''): string {
  return prefix + randomBytes(12).toString('base64url');
}

export function sha256(s: string | Buffer): string {
  return createHash('sha256').update(s).digest('hex');
}

export function safeEqual(a: string, b: string): boolean {
  const x = Buffer.from(a);
  const y = Buffer.from(b);
  return x.length === y.length && timingSafeEqual(x, y);
}

/** Human-friendly one-time code without ambiguous characters. */
export function shortCode(len = 8): string {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  const bytes = randomBytes(len);
  let out = '';
  for (let i = 0; i < len; i++) out += alphabet[bytes[i]! % alphabet.length];
  return out;
}

/** Fixed-window limiter in memory. One backend process; see docs for scaling. */
export class RateLimiter {
  private hits = new Map<string, { count: number; reset: number }>();
  private lastSweep = 0;

  /** Returns seconds to wait, or 0 when allowed. */
  check(key: string, perMinute: number, now = Date.now()): number {
    if (now - this.lastSweep > 60_000) {
      for (const [k, v] of this.hits) if (v.reset <= now) this.hits.delete(k);
      this.lastSweep = now;
    }
    const e = this.hits.get(key);
    if (!e || e.reset <= now) {
      this.hits.set(key, { count: 1, reset: now + 60_000 });
      return 0;
    }
    if (e.count >= perMinute) return Math.ceil((e.reset - now) / 1000);
    e.count++;
    return 0;
  }
}

// ---- validation -----------------------------------------------------------

export function obj(v: unknown): Record<string, unknown> {
  if (typeof v !== 'object' || v === null || Array.isArray(v)) throw badRequest('invalid_body', 'JSON object expected');
  return v as Record<string, unknown>;
}

export function str(o: Record<string, unknown>, key: string, min: number, max: number, opts: { trim?: boolean; pattern?: RegExp } = {}): string {
  const v = o[key];
  if (typeof v !== 'string') throw badRequest('invalid_field', `${key} must be a string`, { field: key });
  const s = opts.trim === false ? v : v.trim();
  if (s.length < min || s.length > max) throw badRequest('invalid_field', `${key} must be ${min}-${max} characters`, { field: key });
  if (opts.pattern && !opts.pattern.test(s)) throw badRequest('invalid_field', `${key} has an invalid format`, { field: key });
  return s;
}

export function optStr(o: Record<string, unknown>, key: string, min: number, max: number, opts: { pattern?: RegExp } = {}): string | null {
  if (o[key] === undefined || o[key] === null) return null;
  return str(o, key, min, max, opts);
}

export function int(o: Record<string, unknown>, key: string, min: number, max: number): number {
  const v = o[key];
  if (typeof v !== 'number' || !Number.isInteger(v) || v < min || v > max) {
    throw badRequest('invalid_field', `${key} must be an integer between ${min} and ${max}`, { field: key });
  }
  return v;
}

export function oneOf<T extends string>(o: Record<string, unknown>, key: string, values: readonly T[]): T {
  const v = o[key];
  if (typeof v !== 'string' || !(values as readonly string[]).includes(v)) {
    throw badRequest('invalid_field', `${key} must be one of ${values.join(', ')}`, { field: key });
  }
  return v as T;
}

export function bool(o: Record<string, unknown>, key: string): boolean {
  const v = o[key];
  if (typeof v !== 'boolean') throw badRequest('invalid_field', `${key} must be a boolean`, { field: key });
  return v;
}

export function tooMany(seconds: number): HttpError {
  return new HttpError(429, 'rate_limited', `Too many requests, retry in ${seconds}s`, { retryAfter: seconds });
}

/** Strips control characters (keeps newlines/tabs) from user text. */
export function cleanText(s: string): string {
  return s.replace(/[\u0000-\u0008\u000b\u000c\u000e-\u001f\u007f‪-‮⁦-⁩]/g, '');
}

export const MC_NAME = /^[A-Za-z0-9_]{3,16}$/;
