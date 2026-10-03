import { api, avatar, confirmDialog, errorToast, fmtDate, fmtNum, html, mount, state, toast, tagPreview, loadCatalog, slotIcon } from '../core.js';
import { applyTheme, navigate, refreshSession } from '../app.js';

export async function login(el) {
  if (state.user) return navigate('/account', true);
  mount(el, html`<div class="card stack narrow center">
    <h1>Anmelden</h1>
    <p class="muted">Öffne den LEGO Launcher, gehe zu <b>Profil → Website-Anmeldung</b> und gib den angezeigten Code ein. Der Code ist 5 Minuten gültig und nur einmal nutzbar.</p>
    <form id="f" class="stack"><input id="code" class="code-input" maxlength="9" autocomplete="one-time-code" placeholder="ABCD2345" required aria-label="Anmeldecode"><button class="btn primary">Anmelden</button></form>
    <p class="muted small">Warum kein Passwort? Dein Konto ist an dein Minecraft-Konto gebunden. Der Launcher hat dich bereits über Microsoft angemeldet – so gibt es kein zusätzliches Passwort, das gestohlen werden kann.</p>
  </div>`);
  el.querySelector('#f').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const r = await api('POST', '/api/auth/web-login', { code: el.querySelector('#code').value.trim() });
      state.user = r.user;
      state.csrf = r.csrf;
      toast(`Willkommen, ${r.user.name}!`, 'ok');
      navigate('/account');
    } catch (err) {
      errorToast(err);
    }
  });
}

export async function settings(el) {
  const [me, sessions] = await Promise.all([api('GET', '/api/me'), api('GET', '/api/auth/sessions')]);
  const themes = state.themes ?? (await api('GET', '/api/public/themes')).themes;
  let current = 'nightfall';
  try { current = localStorage.getItem('lego.theme') ?? current; } catch { /* ignore */ }
  const p = me.privacy;
  const sel = (name, value) => html`<select name="${name}">${[['everyone', 'Alle'], ['friends', 'Nur Freunde'], ['nobody', 'Niemand']].map(([v, l]) => html`<option value="${v}" ${v === value ? 'selected' : ''}>${l}</option>`)}</select>`;
  mount(el, html`<h1>Konto</h1>
    <div class="grid cols-2">
      <div class="card stack">
        <div class="row">${avatar(me.name, 'lg')}<div><h2>${me.name}</h2><div class="muted small">Seit ${fmtDate(me.createdAt)} · ${me.roles.length ? me.roles.join(', ') : 'Spieler'}</div></div></div>
        <div class="row"><span class="kpi">${fmtNum(me.credits)}</span><span class="muted">LEGO Credits</span><span class="spacer"></span><a class="btn small" href="/rewards" data-link>Verlauf</a></div>
        <div class="muted small">${me.subscription ? `Abo: ${me.subscription.tier === 'lego_plus_plus' ? 'LEGO++' : 'LEGO+'} bis ${fmtDate(me.subscription.expiresAt)}` : 'Kein Abo aktiv'}</div>
        <form id="bioForm" class="stack"><label for="bio">Über mich</label><textarea id="bio" maxlength="280">${me.bio}</textarea><button class="btn">Speichern</button></form>
      </div>
      <form class="card stack" id="privacy"><h2>Datenschutz</h2>
        <div class="field"><label>Wer sieht mein Profil?</label>${sel('profile', p.profile)}</div>
        <div class="field"><label>Wer sieht meinen Online-Status?</label>${sel('status', p.status)}</div>
        <div class="field"><label>Wer sieht meine Aktivität und Statistiken?</label>${sel('activity', p.activity)}</div>
        <div class="field"><label>Wer darf mir schreiben?</label>${sel('messages', p.messages)}</div>
        <label class="check"><input type="checkbox" name="friendRequests" ${p.friendRequests ? 'checked' : ''}> Freundschaftsanfragen erlauben</label>
        <button class="btn primary">Datenschutz speichern</button></form>
      <div class="card stack"><h2>Website-Design</h2><p class="muted small">Dieselben 101 Designs wie im Launcher.</p>
        <select id="theme">${themes.map((t) => html`<option value="${t.id}" ${t.id === current ? 'selected' : ''}>${t.name} (${t.family})</option>`)}</select></div>
      <div class="card stack"><h2>Sitzungen</h2><div class="table-wrap"><table><thead><tr><th>Typ</th><th>Zuletzt</th><th>Gerät</th></tr></thead><tbody>
        ${sessions.sessions.map((s) => html`<tr><td>${s.kind === 'client' ? 'Launcher' : 'Website'}${s.current ? ' (diese)' : ''}</td><td>${fmtDate(s.last_used)}</td><td class="small muted">${(s.user_agent ?? '').slice(0, 60)}</td></tr>`)}</tbody></table></div>
        <div class="row"><button class="btn" id="logout">Abmelden</button><button class="btn danger" id="logoutAll">Überall abmelden</button></div></div>
    </div>`);
  el.querySelector('#bioForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try { await api('PATCH', '/api/me', { bio: el.querySelector('#bio').value }); toast('Gespeichert', 'ok'); } catch (err) { errorToast(err); }
  });
  el.querySelector('#privacy').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    try {
      await api('PUT', '/api/me/privacy', { profile: f.get('profile'), status: f.get('status'), activity: f.get('activity'), messages: f.get('messages'), friendRequests: f.get('friendRequests') === 'on' });
      toast('Datenschutz gespeichert', 'ok');
    } catch (err) { errorToast(err); }
  });
  el.querySelector('#theme').addEventListener('change', (e) => void applyTheme(e.target.value));
  el.querySelector('#logout').addEventListener('click', async () => { await api('POST', '/api/auth/logout'); await refreshSession(); navigate('/'); });
  el.querySelector('#logoutAll').addEventListener('click', async () => {
    if (!(await confirmDialog('Überall abmelden?', 'Alle Launcher- und Website-Sitzungen werden beendet.', { danger: true }))) return;
    await api('POST', '/api/auth/logout-all');
    await refreshSession();
    navigate('/');
  });
}

export async function stats(el) {
  const s = await api('GET', '/api/me/stats');
  const t = s.totals;
  const hours = ((t.playtime_minutes ?? 0) / 60).toFixed(1);
  mount(el, html`<h1>Statistiken</h1>
    <div class="grid cols-4">
      <div class="card"><div class="kpi">${hours} h</div><div class="muted">Spielzeit (vom Server gemessen)</div></div>
      <div class="card"><div class="kpi">${fmtNum(t.launches)}</div><div class="muted">Starts</div></div>
      <div class="card"><div class="kpi">${fmtNum(t.minigames_played)}</div><div class="muted">Minispiele</div></div>
      <div class="card"><div class="kpi">${fmtNum(t.minigame_wins)}</div><div class="muted">Siege</div></div>
    </div>
    <h2 class="spaced">Minispiele</h2>
    ${s.games.length ? html`<div class="card table-wrap"><table><thead><tr><th>Spiel</th><th>Gespielt</th><th>Siege</th><th>Bestwert</th></tr></thead><tbody>${s.games.map((g) => html`<tr><td>${g.game_id}</td><td>${g.played}</td><td>${g.wins}</td><td>${g.best}</td></tr>`)}</tbody></table></div>` : html`<div class="empty">Noch keine Spiele.</div>`}`);
}

export async function profile(el, name) {
  const [{ profile: p }, catalog] = await Promise.all([api('GET', `/api/public/profiles/${encodeURIComponent(name)}`), loadCatalog()]);
  const equipped = Object.entries(p.equipped).map(([slot, id]) => ({ slot, item: catalog.items.find((i) => i.id === id) })).filter((x) => x.item);
  const tagId = p.equipped.nametag;
  const tag = catalog.nameTags.find((t) => t.id === tagId) ?? catalog.nameTags[0];
  mount(el, html`<div class="row">${avatar(p.name, 'lg')}<div><h1>${p.name}</h1><div class="row">${tagPreview(tag, p.customTag ? `${p.name} · ${p.customTag}` : p.name)}${p.status !== 'hidden' ? html`<span class="badge"><span class="dot ${p.status}"></span>${p.status}</span>` : ''}</div></div>
    ${state.user && state.user.name !== p.name ? html`<span class="spacer"></span><button class="btn" id="addFriend">Als Freund hinzufügen</button>` : ''}</div>
    ${p.bio ? html`<p class="card spaced">${p.bio}</p>` : ''}
    <h2 class="spaced">Ausgerüstet</h2>
    ${equipped.length ? html`<div class="grid cols-4">${equipped.map(({ slot, item }) => html`<div class="item" data-c="${catalog.rarities[item.rarity].color}"><div class="thumb">${slotIcon(slot)}</div><div class="name">${item.name}</div><div class="muted small">${slot}</div></div>`)}</div>` : html`<div class="empty">Nichts ausgerüstet.</div>`}
    ${p.achievements ? html`<h2 class="spaced">Erfolge</h2><div class="grid cols-4">${p.achievements.map((a) => html`<div class="card ${a.unlocked ? '' : 'muted'}"><b>${a.unlocked ? '🏆' : '🔒'} ${a.name}</b><div class="progress spaced"><span data-p="${(a.progress / a.target) * 100}"></span></div><div class="small muted">${a.progress} / ${a.target}</div></div>`)}</div>` : ''}
    <p class="muted small spaced">Dabei seit ${fmtDate(p.createdAt)}${p.friendCount !== null ? ` · ${p.friendCount} Freunde` : ''}</p>`);
  el.querySelector('#addFriend')?.addEventListener('click', async () => {
    try { const r = await api('POST', '/api/friends/requests', { player: p.name }); toast(r.status === 'friends' ? 'Ihr seid jetzt befreundet' : 'Anfrage gesendet', 'ok'); } catch (e) { errorToast(e); }
  });
}
