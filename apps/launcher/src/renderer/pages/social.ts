// Friends: player search, incoming/outgoing requests, friend cards with face,
// live status and what they play, chat with live messages, party, block list
// and privacy settings. Everything comes from the LEGO server; nothing is
// simulated when it is offline.

import { onLeave, state } from '../app.ts';
import { api, confirm, dialog, errorToast, html, lego, mount, prompt, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import { headFromSkin } from '../avatar.ts';

interface Friend { id: string; uuid: string; name: string; status: string; activity: string | null; gameVersion: string | null; note: string }
interface Req { id: string; uuid: string; name: string; at: number }
interface FriendList { friends: Friend[]; incoming: Req[]; outgoing: Req[] }
interface Party { party: { id: string; leaderId: string; members: { id: string; name: string; status: string }[] } | null; invites: { partyId: string; from: { name: string } }[] }
interface Found { id: string; uuid: string; name: string; relation: 'friend' | 'requested' | 'incoming' | 'none'; acceptsRequests: boolean }
type Level = 'everyone' | 'friends' | 'nobody';
interface Privacy { profile: Level; status: Level; activity: Level; messages: Level; friendRequests: boolean }

interface ChatMsg { id?: number; from_id: string; body: string; image_id?: string | null }

const pictures = new Map<string, Promise<string>>();
/** A chat picture as data URL (loaded once per session; only sender and receiver may load it). */
function chatPicture(id: string): Promise<string> {
  let p = pictures.get(id);
  if (!p) {
    p = lego<{ mime: string; data: string }>('GET', `/api/messages/image/${encodeURIComponent(id)}`).then((r) => `data:${r.mime};base64,${r.data}`);
    pictures.set(id, p);
    p.catch(() => pictures.delete(id));
  }
  return p;
}

/** Base64 of a picture for sending; large ones are scaled to 2560 px and re-encoded as JPEG (≤ 5 MB). */
export async function pictureBase64(file: Blob): Promise<string> {
  const toB64 = async (b: Blob) => { const u = new Uint8Array(await b.arrayBuffer()); let s = ''; for (let i = 0; i < u.length; i += 0x8000) s += String.fromCharCode(...u.subarray(i, i + 0x8000)); return btoa(s); };
  if (file.size <= 4.5 * 1024 * 1024 && /^image\/(png|jpeg|webp)$/.test(file.type)) return toB64(file);
  const bmp = await createImageBitmap(file);
  const k = Math.min(1, 2560 / Math.max(bmp.width, bmp.height));
  const c = document.createElement('canvas');
  c.width = Math.round(bmp.width * k); c.height = Math.round(bmp.height * k);
  c.getContext('2d')!.drawImage(bmp, 0, 0, c.width, c.height);
  for (const q of [0.9, 0.8, 0.65, 0.5]) {
    const b = await new Promise<Blob | null>((r) => c.toBlob(r, 'image/jpeg', q));
    if (b && b.size <= 4.8 * 1024 * 1024) return toB64(b);
  }
  throw new Error('Bild ist zu groß.');
}

const LABEL: Record<string, string> = { online: 'Online', in_game: 'Im Spiel', away: 'Abwesend', offline: 'Offline', hidden: 'Verborgen' };
const ORDER: Record<string, number> = { in_game: 0, online: 1, away: 2, offline: 3, hidden: 4 };
const heads = new Map<string, Promise<string>>();

/** Face of a player (cached); falls back to the default figure. */
function face(uuid: string): Promise<string> {
  let p = heads.get(uuid);
  if (!p) {
    p = api.skin(uuid).then((s) => headFromSkin(s.skin, 48)).catch(() => headFromSkin(null, 48));
    heads.set(uuid, p);
  }
  return p;
}

function fillFaces(root: HTMLElement): void {
  root.querySelectorAll<HTMLElement>('.av[data-uuid]:not(.filled)').forEach((el) => {
    el.classList.add('filled');
    void face(el.dataset.uuid!).then((src) => { if (src) { const i = new Image(); i.src = src; i.alt = ''; el.append(i); } });
  });
}

export async function friendsPage(el: HTMLElement, chatWith?: Friend): Promise<void> {
  const [f, p] = await Promise.all([lego<FriendList>('GET', '/api/friends'), lego<Party>('GET', '/api/party')]);
  const me = state.auth.lego.userId;
  const leader = p.party?.leaderId === me;
  const friends = [...f.friends].sort((a, b) => (ORDER[a.status] ?? 9) - (ORDER[b.status] ?? 9) || a.name.localeCompare(b.name));
  const online = friends.filter((x) => x.status === 'online' || x.status === 'in_game').length;
  const playing = (x: Friend) => x.status === 'in_game' ? `Spielt ${x.activity ?? 'Minecraft'}${x.gameVersion ? ` ${x.gameVersion}` : ''}` : x.activity && x.status !== 'offline' ? x.activity : LABEL[x.status] ?? x.status;

  mount(el, html`<div class="page-head"><div><h1>Freunde</h1><p class="muted small">${online} von ${friends.length} online</p></div>
      <div class="row"><button class="btn" id="privacyBtn">${icon('shield', 'sm')} Privatsphäre</button><button class="btn" id="blockedBtn">${icon('block', 'sm')} Blockiert</button></div></div>
    <div class="social">
      <div class="social-main">
        <div class="searchbar">${icon('search', 'sm')}<input id="search" placeholder="Spieler suchen (mind. 3 Zeichen) …" maxlength="16" autocomplete="off" spellcheck="false"></div>
        <div id="results"></div>
        ${f.incoming.length ? html`<section class="card req-card"><h3>${icon('bell', 'sm')} Anfragen <span class="badge c" data-c="#ff7a1a">${f.incoming.length}</span></h3>
          ${f.incoming.map((r) => html`<div class="friend"><span class="av lg" data-uuid="${r.uuid}"></span><div class="grow"><b>${r.name}</b><small>möchte dein Freund sein</small></div><button class="btn sm primary" data-accept="${r.id}">${icon('check', 'sm')} Annehmen</button><button class="btn sm" data-decline="${r.id}">Ablehnen</button></div>`)}</section>` : ''}
        <section class="card"><h3>Freunde (${friends.length})</h3>
          ${friends.length ? html`<div class="friend-list">${friends.map((x) => html`<div class="friend ${x.status} ${chatWith?.id === x.id ? 'sel' : ''}">
              <span class="av lg" data-uuid="${x.uuid}"><i class="st ${x.status}"></i></span>
              <div class="grow"><b>${x.name}</b><small>${playing(x)}${x.note ? html` · <i>${x.note}</i>` : ''}</small></div>
              <button class="btn sm" data-chat="${x.id}">${icon('feedback', 'sm')} Chat</button>
              ${leader ? html`<button class="icon-btn" data-invite="${x.name}" title="In Party einladen">${icon('plus', 'sm')}</button>` : ''}
              <button class="icon-btn" data-note="${x.id}" data-current="${x.note}" title="Notiz">${icon('sliders', 'sm')}</button>
              <button class="icon-btn danger" data-remove="${x.id}" data-name="${x.name}" title="Entfernen">${icon('trash', 'sm')}</button>
              <button class="icon-btn danger" data-block="${x.name}" title="Blockieren">${icon('block', 'sm')}</button></div>`)}</div>`
          : html`<div class="empty">${icon('ghost')}<p>Noch keine Freunde – such oben nach Spielern.</p></div>`}
        </section>
        ${f.outgoing.length ? html`<section class="card"><h3>Gesendete Anfragen</h3>${f.outgoing.map((r) => html`<div class="friend"><span class="av" data-uuid="${r.uuid}"></span><b class="grow">${r.name}</b><button class="btn sm" data-cancel="${r.id}">Zurückziehen</button></div>`)}</section>` : ''}
      </div>
      <div class="social-side">
        <section class="card"><h3>Party</h3>
          ${p.party ? html`${p.party.members.map((m) => html`<div class="row"><span class="dot ${m.status}"></span><b class="grow">${m.name}</b>${m.id === p.party!.leaderId ? html`<span class="badge">Leiter</span>` : ''}${leader && m.id !== me ? html`<button class="btn sm" data-kick="${m.id}">Entfernen</button>` : ''}</div>`)}<p class="tiny muted gap">Gemeinsam spielen: Party-Mitglieder sehen, auf welchem Server der Leiter spielt, und können per Einladung beitreten.</p><button class="btn sm danger gap" id="leave">Verlassen</button>`
            : html`<p class="muted small">Keine Party.</p><button class="btn sm primary" id="create">${icon('plus', 'sm')} Party erstellen</button>`}
          ${p.invites.map((i) => html`<div class="row gap"><span class="grow">Einladung von <b>${i.from.name}</b></span><button class="btn sm primary" data-join="${i.partyId}">Beitreten</button></div>`)}
        </section>
        <section class="card" id="chatCard">${chatWith ? html`<div class="row between"><h3 class="row"><span class="av" data-uuid="${chatWith.uuid}"></span> ${chatWith.name}</h3><span class="small muted">${LABEL[chatWith.status] ?? ''}</span></div><div class="chat" id="chat"></div><input type="file" id="attachFile" accept="image/png,image/jpeg,image/webp" hidden><form class="row gap" id="send"><button type="button" class="icon-btn" id="attach" title="Bild anhängen (auch Strg+V oder hineinziehen)">${icon('image', 'sm')}</button><input name="body" class="grow" maxlength="1000" placeholder="Nachricht …" autocomplete="off"><button class="btn primary" title="Senden">${icon('chevronRight', 'sm')}</button></form>`
          : html`<p class="muted small">Wähle einen Freund zum Chatten.</p>`}</section>
      </div>
    </div>`);
  fillFaces(el);

  const reload = (w?: Friend) => friendsPage(el, w ?? chatWith);
  const act = async (fn: () => Promise<unknown>, msg?: string) => { try { await fn(); if (msg) toast(msg, 'ok'); await reload(); } catch (e) { errorToast(e); } };

  // Search with debounce.
  const results = el.querySelector<HTMLElement>('#results')!;
  let timer = 0;
  let seq = 0;
  el.querySelector('#search')!.addEventListener('input', (e) => {
    const q = (e.target as HTMLInputElement).value.trim();
    clearTimeout(timer);
    if (q.length < 3) { results.replaceChildren(); return; }
    timer = window.setTimeout(async () => {
      const mine = ++seq;
      try {
        const r = await lego<{ users: Found[] }>('GET', `/api/users/search?q=${encodeURIComponent(q)}`);
        if (mine !== seq) return;
        mount(results, r.users.length ? html`<div class="card search-res">${r.users.map((u) => html`<div class="friend"><span class="av" data-uuid="${u.uuid}"></span><b class="grow">${u.name}</b>
            ${u.relation === 'friend' ? html`<span class="badge">Freund</span>` : u.relation === 'requested' ? html`<span class="badge">Angefragt</span>` : u.relation === 'incoming' ? html`<button class="btn sm primary" data-accept="${u.id}">Annehmen</button>` : u.acceptsRequests ? html`<button class="btn sm primary" data-add="${u.name}">${icon('plus', 'sm')} Hinzufügen</button>` : html`<span class="badge">nimmt keine Anfragen an</span>`}</div>`)}</div>`
          : html`<div class="card search-res"><p class="small muted">Niemand gefunden. Spieler müssen sich einmal mit dem LEGO Launcher angemeldet haben.</p></div>`);
        fillFaces(results);
      } catch (err) { errorToast(err); }
    }, 300);
  });

  el.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    const d = b.dataset;
    if (d.accept) await act(() => lego('POST', `/api/friends/requests/${d.accept}/accept`), 'Freund hinzugefügt');
    else if (d.decline) await act(() => lego('POST', `/api/friends/requests/${d.decline}/decline`));
    else if (d.cancel) await act(() => lego('DELETE', `/api/friends/requests/${d.cancel}`), 'Anfrage zurückgezogen');
    else if (d.add) await act(() => lego('POST', '/api/friends/requests', { player: d.add }), 'Anfrage gesendet');
    else if (d.chat) await reload(f.friends.find((x) => x.id === d.chat));
    else if (d.invite) await act(() => lego('POST', '/api/party/invite', { player: d.invite }), 'Eingeladen');
    else if (d.kick) await act(() => lego('POST', '/api/party/kick', { userId: d.kick }));
    else if (d.join) await act(() => lego('POST', `/api/party/${d.join}/join`), 'Beigetreten');
    else if (d.note) { const n = await prompt('Notiz', 'Nur für dich sichtbar', d.current ?? ''); if (n !== null) await act(() => lego('PUT', `/api/friends/${d.note}/note`, { note: n })); }
    else if (d.remove) { if (await confirm('Entfernen?', `${d.name} aus der Freundesliste entfernen?`, 'Entfernen', true)) await act(() => lego('DELETE', `/api/friends/${d.remove}`)); }
    else if (d.block) { if (await confirm('Blockieren?', `${d.block} kann dir dann keine Anfragen oder Nachrichten mehr schicken und sieht deinen Status nicht.`, 'Blockieren', true)) await act(() => lego('POST', '/api/blocks', { player: d.block }), 'Blockiert'); }
    else if (b.id === 'create') await act(() => lego('POST', '/api/party'));
    else if (b.id === 'leave') await act(() => lego('POST', '/api/party/leave'));
    else if (b.id === 'privacyBtn') void privacyDialog();
    else if (b.id === 'blockedBtn') void blockedDialog(() => void reload());
  };

  const box = el.querySelector<HTMLElement>('#chat');
  const addMsg = (m: ChatMsg, mine: boolean) => {
    if (!box) return;
    const div = document.createElement('div');
    div.className = `msg ${mine ? 'mine' : ''}${m.image_id ? ' pic' : ''}`;
    if (m.image_id) {
      const img = new Image();
      img.alt = 'Bild';
      img.className = 'chat-pic';
      img.title = 'Klicken zum Vergrößern';
      void chatPicture(m.image_id).then((src) => { img.src = src; box.scrollTop = box.scrollHeight; }).catch(() => { img.alt = 'Bild nicht verfügbar'; });
      img.addEventListener('click', () => { if (img.src) dialog(html`<img class="chat-pic-big" src="${img.src}" alt="Bild">`); });
      div.append(img);
    }
    if (m.body) { const t = document.createElement('span'); t.textContent = m.body; div.append(t); }
    box.append(div);
    box.scrollTop = box.scrollHeight;
  };
  if (chatWith && box) {
    const msgs = await lego<{ messages: ChatMsg[] }>('GET', `/api/messages/${chatWith.id}`);
    for (const m of msgs.messages) addMsg(m, m.from_id === me);
    const form = el.querySelector<HTMLFormElement>('#send')!;
    const input = form.elements.namedItem('body') as HTMLInputElement;
    const sendPicture = async (file: Blob) => {
      try {
        toast('Bild wird gesendet …', 'info');
        const m = await lego<ChatMsg>('POST', `/api/messages/${chatWith.id}`, { image: { data: await pictureBase64(file) }, ...(input.value.trim() ? { body: input.value.trim() } : {}) });
        input.value = '';
        addMsg(m, true);
      } catch (err) { errorToast(err); }
    };
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!input.value.trim()) return;
      try {
        const m = await lego<ChatMsg>('POST', `/api/messages/${chatWith.id}`, { body: input.value });
        input.value = '';
        addMsg(m, true);
      } catch (err) { errorToast(err); }
    });
    const file = el.querySelector<HTMLInputElement>('#attachFile')!;
    el.querySelector('#attach')!.addEventListener('click', () => file.click());
    file.addEventListener('change', () => { const f0 = file.files?.[0]; file.value = ''; if (f0) void sendPicture(f0); });
    input.addEventListener('paste', (e) => {
      const it = [...(e.clipboardData?.items ?? [])].find((x) => x.type.startsWith('image/'));
      const blob = it?.getAsFile();
      if (blob) { e.preventDefault(); void sendPicture(blob); }
    });
    const card = el.querySelector<HTMLElement>('#chatCard')!;
    card.addEventListener('dragover', (e) => { if ([...(e.dataTransfer?.items ?? [])].some((x) => x.type.startsWith('image/'))) { e.preventDefault(); e.stopPropagation(); card.classList.add('drop'); } });
    card.addEventListener('dragleave', () => card.classList.remove('drop'));
    card.addEventListener('drop', (e) => {
      const f0 = [...(e.dataTransfer?.files ?? [])].find((x) => x.type.startsWith('image/'));
      card.classList.remove('drop');
      if (f0) { e.preventDefault(); e.stopPropagation(); void sendPicture(f0); }
    });
  }

  // Live updates while this page is open.
  let reloadTimer = 0;
  const off = api.on('lego-event', (ev: { type: string; data: Record<string, unknown> }) => {
    if (ev.type === 'message' && chatWith && ev.data.from_id === chatWith.id) return addMsg(ev.data as unknown as ChatMsg, false);
    if (['friend_request', 'friend_accepted', 'presence', 'party_invite', 'party_update'].includes(ev.type)) {
      clearTimeout(reloadTimer);
      reloadTimer = window.setTimeout(() => { if (el.isConnected) void reload(); }, 400);
    }
  });
  onLeave(() => { off(); clearTimeout(reloadTimer); });
}

async function privacyDialog(): Promise<void> {
  const me = await lego<{ privacy: Privacy }>('GET', '/api/me');
  const pv = me.privacy;
  const sel = (name: keyof Privacy, label: string, desc: string) => html`<div class="setting"><div><b>${label}</b><small>${desc}</small></div><select name="${name}" class="compact">${(['everyone', 'friends', 'nobody'] as Level[]).map((l) => html`<option value="${l}" ${pv[name] === l ? 'selected' : ''}>${l === 'everyone' ? 'Alle' : l === 'friends' ? 'Nur Freunde' : 'Niemand'}</option>`)}</select></div>`;
  const d = dialog(html`<form method="dialog" id="pv"><h2>${icon('shield', 'sm')} Privatsphäre</h2>
    ${sel('profile', 'Profil & Suche', 'Wer dich in der Suche findet und dein Profil sieht.')}
    ${sel('status', 'Online-Status', 'Wer sieht, ob du online oder im Spiel bist.')}
    ${sel('activity', 'Aktivität', 'Wer sieht, was du gerade spielst.')}
    ${sel('messages', 'Nachrichten', 'Wer dir Nachrichten schicken darf.')}
    <div class="setting"><div><b>Freundschaftsanfragen</b><small>Andere dürfen dir Anfragen schicken.</small></div><label class="switch"><input type="checkbox" name="friendRequests" ${pv.friendRequests ? 'checked' : ''}><i></i></label></div>
    <div class="row between gap"><button class="btn" value="cancel">Abbrechen</button><button class="btn primary" value="ok">Speichern</button></div></form>`);
  d.onclose = async () => {
    if (d.returnValue !== 'ok') return;
    const fd = new FormData(d.querySelector<HTMLFormElement>('#pv')!);
    try {
      await lego('PUT', '/api/me/privacy', { profile: fd.get('profile'), status: fd.get('status'), activity: fd.get('activity'), messages: fd.get('messages'), friendRequests: fd.get('friendRequests') === 'on' });
      toast('Privatsphäre gespeichert', 'ok');
    } catch (err) { errorToast(err); }
  };
}

async function blockedDialog(after: () => void): Promise<void> {
  const r = await lego<{ blocked: { id: string; uuid: string; name: string }[] }>('GET', '/api/blocks');
  const d = dialog(html`<h2>${icon('block', 'sm')} Blockierte Spieler</h2>
    ${r.blocked.length ? html`<div class="list">${r.blocked.map((u) => html`<div class="friend"><span class="av" data-uuid="${u.uuid}"></span><b class="grow">${u.name}</b><button class="btn sm" data-unblock="${u.id}">Freigeben</button></div>`)}</div>` : html`<p class="muted">Du hast niemanden blockiert.</p>`}
    <form method="dialog" class="row gap"><button class="btn">Schließen</button></form>`);
  fillFaces(d);
  d.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-unblock]');
    if (!b) return;
    try { await lego('DELETE', `/api/blocks/${b.dataset.unblock}`); b.closest('.friend')?.remove(); toast('Freigegeben', 'ok'); after(); } catch (err) { errorToast(err); }
  };
}
