// Runtime configuration from environment variables. Secrets are never
// hard-coded; see apps/backend/.env.example for the full list.

import { join } from 'node:path';

export interface Config {
  port: number;
  host: string;
  dataDir: string;
  /** Public origin of the website, used for cookies/CSRF origin checks. */
  publicOrigin: string;
  /** Set Secure on cookies. Disable only for local http development. */
  secureCookies: boolean;
  /** Mojang session server base URL (overridable for tests). */
  sessionServer: string;
  /** Trust X-Forwarded-For from a reverse proxy. */
  trustProxy: boolean;
  stripeSecretKey: string | null;
  stripeWebhookSecret: string | null;
  websiteDir: string;
  maintenance: boolean;
}

function bool(v: string | undefined, def: boolean): boolean {
  if (v === undefined || v === '') return def;
  return v === '1' || v.toLowerCase() === 'true';
}

export function loadConfig(env: NodeJS.ProcessEnv = process.env): Config {
  const root = new URL('../../', import.meta.url).pathname;
  return {
    port: Number(env.LEGO_PORT ?? 8787),
    host: env.LEGO_HOST ?? '127.0.0.1',
    dataDir: env.LEGO_DATA_DIR ?? join(root, 'backend', 'data'),
    publicOrigin: env.LEGO_PUBLIC_ORIGIN ?? 'http://localhost:8787',
    secureCookies: bool(env.LEGO_SECURE_COOKIES, true),
    sessionServer: env.LEGO_SESSION_SERVER ?? 'https://sessionserver.mojang.com',
    trustProxy: bool(env.LEGO_TRUST_PROXY, false),
    stripeSecretKey: env.STRIPE_SECRET_KEY || null,
    stripeWebhookSecret: env.STRIPE_WEBHOOK_SECRET || null,
    websiteDir: env.LEGO_WEBSITE_DIR ?? join(root, 'website', 'public'),
    maintenance: bool(env.LEGO_MAINTENANCE, false),
  };
}
