// Microsoft account -> Minecraft: Java Edition sign-in.
//
//   1. Microsoft identity platform, device code flow, consumers tenant,
//      scope "XboxLive.signin offline_access" (official Microsoft sign-in page,
//      the launcher never sees the password).
//   2. Xbox Live user token     (user.auth.xboxlive.com)
//   3. XSTS token for Minecraft (xsts.auth.xboxlive.com, rp://api.minecraftservices.com/)
//   4. Minecraft access token   (api.minecraftservices.com/authentication/login_with_xbox)
//   5. Entitlement + profile check.
//
// The Azure application (client id) must be registered by the operator and
// approved for the Minecraft API by Mojang; otherwise step 4 answers 403.

import { fetchJson, form, HttpStatusError, type FetchLike } from './net.ts';

export const MS_TENANT = 'consumers';
export const MS_SCOPE = 'XboxLive.signin offline_access';
const MS_BASE = `https://login.microsoftonline.com/${MS_TENANT}/oauth2/v2.0`;

export interface DeviceCode {
  deviceCode: string;
  userCode: string;
  verificationUri: string;
  expiresAt: number;
  intervalMs: number;
  message: string;
}

export interface MsTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface MinecraftSession {
  /** Minecraft services bearer token (passed to the game, never to LEGO servers). */
  accessToken: string;
  expiresAt: number;
  uuid: string; // undashed, as returned by the profile API
  name: string;
  xuid: string | null;
  skinUrl: string | null;
  skinVariant?: 'classic' | 'slim';
  capeUrl?: string | null;
}

export class AuthError extends Error {
  readonly code: string;
  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
}

const XSTS_ERRORS: Record<string, string> = {
  '2148916227': 'This Xbox account is banned.',
  '2148916229': 'This account needs parental permission for online play (Xbox family settings).',
  '2148916233': 'This Microsoft account has no Xbox profile yet. Sign in once at xbox.com to create one.',
  '2148916234': 'The Xbox Live terms of use have not been accepted for this account.',
  '2148916235': 'Xbox Live is not available in your country or region.',
  '2148916236': 'This account requires adult verification (South Korea).',
  '2148916237': 'This account requires adult verification (South Korea).',
  '2148916238': 'This is a child account. An adult must add it to a Microsoft family to play.',
};

export async function startDeviceCode(f: FetchLike, clientId: string): Promise<DeviceCode> {
  const r = await fetchJson<{ device_code: string; user_code: string; verification_uri: string; expires_in: number; interval: number; message: string }>(
    f, `${MS_BASE}/devicecode`,
    { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form({ client_id: clientId, scope: MS_SCOPE }) },
  );
  return {
    deviceCode: r.device_code, userCode: r.user_code, verificationUri: r.verification_uri,
    expiresAt: Date.now() + r.expires_in * 1000, intervalMs: Math.max(1, r.interval) * 1000, message: r.message,
  };
}

/** Polls the token endpoint until the user finished signing in (or cancels). */
export async function pollDeviceCode(f: FetchLike, clientId: string, dc: DeviceCode, signal?: AbortSignal, sleep = (ms: number) => new Promise((r) => setTimeout(r, ms))): Promise<MsTokens> {
  let interval = dc.intervalMs;
  while (Date.now() < dc.expiresAt) {
    if (signal?.aborted) throw new AuthError('cancelled', 'Sign-in cancelled');
    await sleep(interval);
    try {
      const r = await fetchJson<{ access_token: string; refresh_token: string; expires_in: number }>(
        f, `${MS_BASE}/token`,
        { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form({ grant_type: 'urn:ietf:params:oauth:grant-type:device_code', client_id: clientId, device_code: dc.deviceCode }) },
      );
      return { accessToken: r.access_token, refreshToken: r.refresh_token, expiresAt: Date.now() + r.expires_in * 1000 };
    } catch (e) {
      if (!(e instanceof HttpStatusError)) throw e;
      let err = '';
      try { err = (JSON.parse(e.body) as { error?: string }).error ?? ''; } catch { /* not JSON */ }
      if (err === 'authorization_pending') continue;
      if (err === 'slow_down') { interval += 5000; continue; }
      if (err === 'authorization_declined') throw new AuthError('declined', 'Sign-in was declined.');
      if (err === 'expired_token') throw new AuthError('expired', 'The code expired. Please start again.');
      throw new AuthError('ms_error', `Microsoft sign-in failed (${err || e.status}).`);
    }
  }
  throw new AuthError('expired', 'The code expired. Please start again.');
}

export async function refreshMicrosoft(f: FetchLike, clientId: string, refreshToken: string): Promise<MsTokens> {
  try {
    const r = await fetchJson<{ access_token: string; refresh_token: string; expires_in: number }>(
      f, `${MS_BASE}/token`,
      { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form({ grant_type: 'refresh_token', client_id: clientId, refresh_token: refreshToken, scope: MS_SCOPE }) },
    );
    return { accessToken: r.access_token, refreshToken: r.refresh_token ?? refreshToken, expiresAt: Date.now() + r.expires_in * 1000 };
  } catch (e) {
    if (e instanceof HttpStatusError && e.status === 400) throw new AuthError('relogin', 'Your Microsoft sign-in expired. Please sign in again.');
    throw e;
  }
}

/** Steps 2-5: Microsoft access token -> verified Minecraft session. */
export async function minecraftFromMicrosoft(f: FetchLike, msAccessToken: string): Promise<MinecraftSession> {
  const xbl = await fetchJson<{ Token: string; DisplayClaims: { xui: { uhs: string }[] } }>(f, 'https://user.auth.xboxlive.com/user/authenticate', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ Properties: { AuthMethod: 'RPS', SiteName: 'user.auth.xboxlive.com', RpsTicket: `d=${msAccessToken}` }, RelyingParty: 'http://auth.xboxlive.com', TokenType: 'JWT' }),
  });
  const uhs = xbl.DisplayClaims.xui[0]?.uhs;
  if (!uhs) throw new AuthError('xbl', 'Xbox Live returned no user hash.');

  let xsts: { Token: string; DisplayClaims: { xui: { uhs: string; xid?: string }[] } };
  try {
    xsts = await fetchJson(f, 'https://xsts.auth.xboxlive.com/xsts/authorize', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ Properties: { SandboxId: 'RETAIL', UserTokens: [xbl.Token] }, RelyingParty: 'rp://api.minecraftservices.com/', TokenType: 'JWT' }),
    });
  } catch (e) {
    if (e instanceof HttpStatusError && e.status === 401) {
      let xerr = '';
      try { xerr = String((JSON.parse(e.body) as { XErr?: number }).XErr ?? ''); } catch { /* ignore */ }
      throw new AuthError(`xsts_${xerr || 'denied'}`, XSTS_ERRORS[xerr] ?? `Xbox Live denied access (${xerr || 'unknown reason'}).`);
    }
    throw e;
  }

  let mc: { access_token: string; expires_in: number };
  try {
    mc = await fetchJson(f, 'https://api.minecraftservices.com/authentication/login_with_xbox', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identityToken: `XBL3.0 x=${uhs};${xsts.Token}` }),
    });
  } catch (e) {
    if (e instanceof HttpStatusError && e.status === 403) {
      throw new AuthError('app_not_approved', 'Minecraft services rejected this launcher build. The Azure app id is not approved for the Minecraft API (see docs/LAUNCHER.md).');
    }
    throw e;
  }
  const auth = { Authorization: `Bearer ${mc.access_token}` };

  const ent = await fetchJson<{ items?: { name: string }[] }>(f, 'https://api.minecraftservices.com/entitlements/mcstore', { headers: auth });
  const names = new Set((ent.items ?? []).map((i) => i.name));
  if (!names.has('game_minecraft') && !names.has('product_minecraft')) {
    throw new AuthError('no_license', 'This Microsoft account does not own Minecraft: Java Edition.');
  }

  let profile: { id: string; name: string; skins?: { state: string; url: string; variant?: string }[]; capes?: { state: string; url: string }[] };
  try {
    profile = await fetchJson(f, 'https://api.minecraftservices.com/minecraft/profile', { headers: auth });
  } catch (e) {
    if (e instanceof HttpStatusError && e.status === 404) throw new AuthError('no_profile', 'Minecraft is owned but no player name was created yet. Start the official launcher once.');
    throw e;
  }
  return {
    accessToken: mc.access_token,
    expiresAt: Date.now() + mc.expires_in * 1000,
    uuid: profile.id,
    name: profile.name,
    xuid: xsts.DisplayClaims.xui[0]?.xid ?? null,
    skinUrl: profile.skins?.find((s) => s.state === 'ACTIVE')?.url ?? null,
    skinVariant: profile.skins?.find((s) => s.state === 'ACTIVE')?.variant?.toLowerCase() === 'slim' ? 'slim' : 'classic',
    capeUrl: profile.capes?.find((c) => c.state === 'ACTIVE')?.url ?? null,
  };
}

/**
 * Proves account ownership to a server (here: the LEGO backend) the same way
 * the game does when joining a server: the token goes to Mojang only.
 */
export async function sessionJoin(f: FetchLike, s: MinecraftSession, serverId: string): Promise<void> {
  const res = await f('https://sessionserver.mojang.com/session/minecraft/join', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ accessToken: s.accessToken, selectedProfile: s.uuid, serverId }),
    signal: AbortSignal.timeout(15_000),
  });
  if (res.status !== 204 && !res.ok) throw new AuthError('session_join', `Mojang session server refused the join (${res.status}).`);
}

// ---- interactive sign-in (authorization code + PKCE) -----------------------
// Used by the in-app Microsoft login window. The redirect URI must be added to
// the Azure app under "Mobile and desktop applications".

export const MS_REDIRECT_URI = 'https://login.microsoftonline.com/common/oauth2/nativeclient';

export interface PkceRequest { url: string; state: string; verifier: string }

function b64url(buf: Uint8Array): string {
  return Buffer.from(buf).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** Builds the authorize URL with a fresh PKCE verifier and state. */
export async function authorizeRequest(clientId: string, rand: (n: number) => Uint8Array = (n) => crypto.getRandomValues(new Uint8Array(n))): Promise<PkceRequest> {
  const verifier = b64url(rand(48));
  const state = b64url(rand(24));
  const challenge = b64url(new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier))));
  const q = new URLSearchParams({
    client_id: clientId, response_type: 'code', redirect_uri: MS_REDIRECT_URI, response_mode: 'query',
    scope: MS_SCOPE, state, code_challenge: challenge, code_challenge_method: 'S256', prompt: 'select_account',
  });
  return { url: `${MS_BASE}/authorize?${q}`, state, verifier };
}

/** Parses the redirect: returns the code, throws on error/state mismatch, null if not the redirect. */
export function parseRedirect(url: string, expectedState: string): string | null {
  if (!url.startsWith(MS_REDIRECT_URI)) return null;
  const q = new URL(url).searchParams;
  const err = q.get('error');
  if (err) {
    if (err === 'access_denied') throw new AuthError('declined', 'Anmeldung abgebrochen.');
    throw new AuthError('ms_error', `Microsoft-Anmeldung fehlgeschlagen (${q.get('error_description') ?? err}).`);
  }
  if (q.get('state') !== expectedState) throw new AuthError('state', 'Ungültige Antwort von Microsoft (state).');
  const code = q.get('code');
  if (!code) throw new AuthError('ms_error', 'Microsoft hat keinen Code geliefert.');
  return code;
}

export async function exchangeCode(f: FetchLike, clientId: string, code: string, verifier: string): Promise<MsTokens> {
  try {
    const r = await fetchJson<{ access_token: string; refresh_token: string; expires_in: number }>(
      f, `${MS_BASE}/token`,
      { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: form({ grant_type: 'authorization_code', client_id: clientId, code, redirect_uri: MS_REDIRECT_URI, code_verifier: verifier, scope: MS_SCOPE }) },
      { retries: 0 },
    );
    return { accessToken: r.access_token, refreshToken: r.refresh_token, expiresAt: Date.now() + r.expires_in * 1000 };
  } catch (e) {
    if (e instanceof HttpStatusError) {
      let err = '';
      try { err = (JSON.parse(e.body) as { error_description?: string; error?: string }).error_description ?? ''; } catch { /* ignore */ }
      if (/AADSTS50011|redirect/i.test(err)) throw new AuthError('redirect_uri', `In der Azure-App fehlt die Umleitungs-URI ${MS_REDIRECT_URI} (Plattform „Mobil- und Desktopanwendungen“).`);
      throw new AuthError('ms_error', `Microsoft-Anmeldung fehlgeschlagen (${err.split('\n')[0] || e.status}).`);
    }
    throw e;
  }
}
