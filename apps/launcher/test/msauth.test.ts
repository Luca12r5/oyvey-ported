import { test } from 'node:test';
import assert from 'node:assert/strict';
import { AuthError, minecraftFromMicrosoft, pollDeviceCode, startDeviceCode } from '../src/core/msauth.ts';

type Handler = (url: string, init?: RequestInit) => Response | Promise<Response>;
function router(routes: Record<string, Handler>): typeof fetch {
  return (async (url: string | URL, init?: RequestInit) => {
    const u = String(url);
    const key = Object.keys(routes).find((k) => u.startsWith(k));
    if (!key) throw new Error(`unexpected ${u}`);
    return routes[key]!(u, init);
  }) as typeof fetch;
}

const happy: Record<string, Handler> = {
  'https://user.auth.xboxlive.com/user/authenticate': (_u, init) => {
    const b = JSON.parse(String(init!.body));
    assert.equal(b.Properties.RpsTicket, 'd=MS_TOKEN');
    return Response.json({ Token: 'XBL', DisplayClaims: { xui: [{ uhs: 'UHS' }] } });
  },
  'https://xsts.auth.xboxlive.com/xsts/authorize': (_u, init) => {
    const b = JSON.parse(String(init!.body));
    assert.equal(b.RelyingParty, 'rp://api.minecraftservices.com/');
    assert.deepEqual(b.Properties.UserTokens, ['XBL']);
    return Response.json({ Token: 'XSTS', DisplayClaims: { xui: [{ uhs: 'UHS', xid: '2535' }] } });
  },
  'https://api.minecraftservices.com/authentication/login_with_xbox': (_u, init) => {
    assert.equal(JSON.parse(String(init!.body)).identityToken, 'XBL3.0 x=UHS;XSTS');
    return Response.json({ access_token: 'MC', expires_in: 86400 });
  },
  'https://api.minecraftservices.com/entitlements/mcstore': (_u, init) => {
    assert.equal((init!.headers as Record<string, string>).Authorization, 'Bearer MC');
    return Response.json({ items: [{ name: 'product_minecraft' }, { name: 'game_minecraft' }] });
  },
  'https://api.minecraftservices.com/minecraft/profile': () => Response.json({ id: '069a79f444e94726a5befca90e38aaf5', name: 'Notch', skins: [{ state: 'ACTIVE', url: 'https://textures/x' }] }),
};

test('full Xbox/XSTS/Minecraft chain', async () => {
  const s = await minecraftFromMicrosoft(router(happy), 'MS_TOKEN');
  assert.equal(s.name, 'Notch');
  assert.equal(s.accessToken, 'MC');
  assert.equal(s.xuid, '2535');
  assert.equal(s.skinUrl, 'https://textures/x');
});

test('XSTS errors become readable messages', async () => {
  const f = router({ ...happy, 'https://xsts.auth.xboxlive.com/xsts/authorize': () => Response.json({ XErr: 2148916233 }, { status: 401 }) });
  await assert.rejects(minecraftFromMicrosoft(f, 'MS_TOKEN'), (e: unknown) => e instanceof AuthError && e.code === 'xsts_2148916233' && /Xbox profile/.test(e.message));
});

test('unapproved Azure app and missing licence are reported', async () => {
  const f1 = router({ ...happy, 'https://api.minecraftservices.com/authentication/login_with_xbox': () => new Response('{}', { status: 403 }) });
  await assert.rejects(minecraftFromMicrosoft(f1, 'MS_TOKEN'), (e: unknown) => e instanceof AuthError && e.code === 'app_not_approved');
  const f2 = router({ ...happy, 'https://api.minecraftservices.com/entitlements/mcstore': () => Response.json({ items: [] }) });
  await assert.rejects(minecraftFromMicrosoft(f2, 'MS_TOKEN'), (e: unknown) => e instanceof AuthError && e.code === 'no_license');
});

test('device code flow handles pending, slow_down and success', async () => {
  let polls = 0;
  const f = router({
    'https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode': (_u, init) => {
      assert.match(String(init!.body), /scope=XboxLive.signin\+offline_access/);
      return Response.json({ device_code: 'DC', user_code: 'ABCD-EFGH', verification_uri: 'https://microsoft.com/link', expires_in: 900, interval: 5, message: 'go' });
    },
    'https://login.microsoftonline.com/consumers/oauth2/v2.0/token': (_u, init) => {
      assert.match(String(init!.body), /device_code=DC/);
      polls++;
      if (polls === 1) return Response.json({ error: 'authorization_pending' }, { status: 400 });
      if (polls === 2) return Response.json({ error: 'slow_down' }, { status: 400 });
      return Response.json({ access_token: 'AT', refresh_token: 'RT', expires_in: 3600 });
    },
  });
  const dc = await startDeviceCode(f, 'client-id');
  assert.equal(dc.userCode, 'ABCD-EFGH');
  const waits: number[] = [];
  const tokens = await pollDeviceCode(f, 'client-id', dc, undefined, async (ms) => { waits.push(ms); });
  assert.equal(tokens.refreshToken, 'RT');
  assert.deepEqual(waits, [5000, 5000, 10000], 'slow_down increases the interval');
});

test('declined sign-in stops polling', async () => {
  const f = router({ 'https://login.microsoftonline.com/consumers/oauth2/v2.0/token': () => Response.json({ error: 'authorization_declined' }, { status: 400 }) });
  const dc = { deviceCode: 'DC', userCode: 'X', verificationUri: '', expiresAt: Date.now() + 60_000, intervalMs: 1, message: '' };
  await assert.rejects(pollDeviceCode(f, 'id', dc, undefined, async () => {}), (e: unknown) => e instanceof AuthError && e.code === 'declined');
});
