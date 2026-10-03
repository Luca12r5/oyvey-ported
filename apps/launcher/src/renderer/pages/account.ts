import { icon, pixel } from '../icons.ts';
import { NAME_TAGS } from '@lego/shared';
import { go, refreshChrome, state } from '../app.ts';
import { api, confirm, dialog, errorToast, fmtDate, fmtNum, html, lego, mount, toast } from '../ui.ts';
import { tagHtml } from './cosmetics.ts';

/** Microsoft sign-in: opens Microsoft's login page in a launcher window. */
export async function signInFlow(): Promise<void> {
  if (!state.info.msClientConfigured) return notConfigured();
  try {
    await api.signIn();
  } catch (e) {
    errorToast(e);
  }
}

function notConfigured(): void {
  const d = dialog(html`<h2>Anmeldung noch nicht eingerichtet</h2>
    <p>Diesem Launcher-Build fehlt die Microsoft-App-ID. Der Betreiber muss sie einmalig bei Microsoft anlegen und von Mojang freischalten lassen – danach ist die Anmeldung für alle automatisch.</p>
    <p class="small muted">Anleitung: docs/MICROSOFT-LOGIN.md im Projekt. Hast du schon eine ID, trag sie unter Einstellungen → Erweitert ein.</p>
    <div class="row between gap"><button class="btn" id="close">Schließen</button><button class="btn primary" id="settings">${icon('settings', 'sm')} Einstellungen</button></div>`);
  d.querySelector('#close')!.addEventListener('click', () => d.close());
  d.querySelector('#settings')!.addEventListener('click', () => { d.close(); void go('settings').then(() => document.querySelector<HTMLButtonElement>('[data-tab=advanced]')?.click()); });
}

/** Fallback: device code entered on microsoft.com/link in any browser. */
export async function codeSignInFlow(): Promise<void> {
  if (!state.info.msClientConfigured) return notConfigured();
  try {
    const dc = await api.signInWithCode();
    const d = dialog(html`<h2>Mit Code anmelden</h2>
      <p>Im Browser hat sich die offizielle Microsoft-Seite geöffnet. Gib dort diesen Code ein:</p>
      <div class="code-big">${dc.userCode}</div>
      <p class="small muted gap">Seite nicht offen? <a href="#" id="open">${dc.verificationUri}</a> · Der Code ist bis ${new Date(dc.expiresAt).toLocaleTimeString('de-DE')} gültig. Dein Passwort gibst du nur bei Microsoft ein – der Launcher sieht es nie.</p>
      <div class="row between gap"><button class="btn" id="copy">Code kopieren</button><button class="btn" id="cancel">Abbrechen</button></div>`);
    d.dataset.kind = 'signin';
    d.querySelector('#open')!.addEventListener('click', (e) => { e.preventDefault(); void api.openExternal(dc.verificationUri); });
    d.querySelector('#copy')!.addEventListener('click', () => void navigator.clipboard.writeText(dc.userCode).then(() => toast('Code kopiert', 'ok')));
    d.querySelector('#cancel')!.addEventListener('click', () => { api.cancelSignIn(); d.close(); });
  } catch (e) { errorToast(e); }
}

export async function accountPage(el: HTMLElement): Promise<void> {
  const a = state.auth;
  if (!a.signedIn) {
    mount(el, html`<h1>Konto</h1><div class="card"><p>Melde dich mit dem Microsoft-Konto an, das Minecraft: Java Edition besitzt.</p><div class="row"><button class="btn primary" id="signin">Mit Microsoft anmelden</button><button class="btn" id="code">Mit Code anmelden</button></div>
      ${a.secureStorage ? '' : html`<p class="small muted gap">Hinweis: Das System bietet keine sichere Speicherung – du musst dich nach jedem Start neu anmelden.</p>`}</div>`);
    el.querySelector('#signin')!.addEventListener('click', () => void signInFlow());
    el.querySelector('#code')!.addEventListener('click', () => void codeSignInFlow());
    return;
  }
  type Me = { credits: number; bio: string; customTag: string | null; subscription: { tier: string; expiresAt: number } | null; createdAt: number };
  type Stats = { totals: Record<string, number> };
  type Inv = { equipped: Record<string, string> };
  const data: { me: Me | null; stats: Stats | null; inv: Inv | null } = { me: null, stats: null, inv: null };
  if (a.lego.connected) {
    try {
      [data.me, data.stats, data.inv] = await Promise.all([lego<Me>('GET', '/api/me'), lego<Stats>('GET', '/api/me/stats'), lego<Inv>('GET', '/api/inventory')]);
    } catch { /* offline */ }
  }
  const { me, stats, inv } = data;
  const tag = NAME_TAGS.find((t) => t.id === inv?.equipped.nametag) ?? NAME_TAGS[0]!;
  const t = stats?.totals ?? {};
  mount(el, html`<h1>Konto</h1><div class="grid g2">
    <div class="card"><h2>${a.name}</h2><div class="row">${tagHtml(tag, me?.customTag ? `${a.name} · ${me.customTag}` : a.name!)}</div>
      <p class="small muted gap mono">${a.uuid}</p>
      <p class="small">LEGO-Server: ${a.lego.connected ? html`${icon('check', 'xs')} verbunden` : html`${icon('close', 'xs')} ${a.lego.error ?? 'nicht verbunden'}`}${a.lego.roles.length ? ` · Rollen: ${a.lego.roles.join(', ')}` : ''}</p>
      ${me ? html`<p>${pixel('coin', 'xs')} <b>${fmtNum(me.credits)}</b> Coins · ${me.subscription ? `${me.subscription.tier === 'lego_plus_plus' ? 'LEGO++' : 'LEGO+'} bis ${fmtDate(me.subscription.expiresAt)}` : 'kein Abo'}</p>` : ''}
      <div class="row gap">${a.lego.connected ? html`<button class="btn" id="webcode">Website-Anmeldung</button>` : html`<button class="btn primary" id="reconnect">LEGO-Server verbinden</button>`}<button class="btn danger" id="signout">Abmelden</button></div></div>
    <div class="card"><h2>Statistiken</h2><div class="grid g3">
      <div><div class="kpi">${((t.playtime_minutes ?? 0) / 60).toFixed(1)} h</div><div class="small muted">Spielzeit</div></div>
      <div><div class="kpi">${fmtNum(t.launches)}</div><div class="small muted">Starts</div></div>
      <div><div class="kpi">${fmtNum(t.minigame_wins)}</div><div class="small muted">Siege</div></div></div>
      <p class="small muted gap">Spielzeit misst der Server anhand der Online-Signale des Launchers.</p></div>
  </div>`);
  el.querySelector('#signout')!.addEventListener('click', async () => {
    if (!(await confirm('Abmelden?', 'Gespeicherte Anmeldedaten werden gelöscht.', 'Abmelden', true))) return;
    state.auth = await api.signOut();
    refreshChrome();
    void go('play');
  });
  el.querySelector('#reconnect')?.addEventListener('click', async () => { state.auth = await api.reconnectLego(); refreshChrome(); void accountPage(el); });
  el.querySelector('#webcode')?.addEventListener('click', async () => {
    try {
      const r = await lego<{ code: string; expiresAt: number }>('POST', '/api/auth/web-code');
      dialog(html`<h2>Website-Anmeldung</h2><p>Gib diesen Code auf der LEGO-Website unter <b>Anmelden</b> ein. Gültig bis ${new Date(r.expiresAt).toLocaleTimeString('de-DE')}, nur einmal nutzbar.</p><div class="code-big">${r.code}</div><form method="dialog" class="row gap"><button class="btn">Schließen</button></form>`);
    } catch (e) { errorToast(e); }
  });
}
