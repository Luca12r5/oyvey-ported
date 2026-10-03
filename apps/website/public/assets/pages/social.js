import { api, avatar, confirmDialog, errorToast, fmtDate, html, mount, state, toast } from '../core.js';

export async function friends(el) {
  const [f, party, blocks] = await Promise.all([api('GET', '/api/friends'), api('GET', '/api/party'), api('GET', '/api/blocks')]);
  const statusLabel = { online: 'Online', in_game: 'Im Spiel', away: 'Abwesend', offline: 'Offline', hidden: 'Verborgen' };
  mount(el, html`<h1>Freunde</h1>
    <div class="grid cols-2">
      <div class="stack">
        <form class="card row" id="add"><input name="player" placeholder="Minecraft-Name" required minlength="3" maxlength="16" aria-label="Spielername"><button class="btn primary">Hinzufügen</button></form>
        ${f.incoming.length ? html`<div class="card stack"><h3>Anfragen</h3>${f.incoming.map((r) => html`<div class="row">${avatar(r.name)}<b>${r.name}</b><span class="spacer"></span><button class="btn small primary" data-accept="${r.id}">Annehmen</button><button class="btn small" data-decline="${r.id}">Ablehnen</button></div>`)}</div>` : ''}
        ${f.outgoing.length ? html`<div class="card stack"><h3>Gesendet</h3>${f.outgoing.map((r) => html`<div class="row">${avatar(r.name)}<span>${r.name}</span><span class="spacer"></span><button class="btn small" data-cancel="${r.id}">Zurückziehen</button></div>`)}</div>` : ''}
        <div class="card stack"><h3>Freunde (${f.friends.length})</h3>
          ${f.friends.length ? f.friends.map((x) => html`<div class="row">${avatar(x.name)}<div><a href="/u/${x.name}" data-link><b>${x.name}</b></a><div class="small muted"><span class="dot ${x.status}"></span> ${statusLabel[x.status] ?? x.status}${x.activity ? ` · ${x.activity}` : ''}${x.gameVersion ? ` · ${x.gameVersion}` : ''}</div>${x.note ? html`<div class="small muted">📝 ${x.note}</div>` : ''}</div>
            <span class="spacer"></span><a class="btn small" href="/messages/${x.id}" data-link>Chat</a><button class="btn small" data-note="${x.id}" data-current="${x.note}">Notiz</button>${party.party && party.party.leaderId === state.user.id ? html`<button class="btn small" data-invite="${x.name}">Einladen</button>` : ''}<button class="btn small" data-remove="${x.id}" data-name="${x.name}">Entfernen</button><button class="btn small danger" data-block="${x.name}">Blockieren</button></div>`) : html`<div class="empty">Noch keine Freunde. Füge jemanden über den Minecraft-Namen hinzu.</div>`}
        </div>
      </div>
      <div class="stack">
        <div class="card stack" id="party"><h3>Party</h3>
          ${party.party ? html`<div class="stack">${party.party.members.map((m) => html`<div class="row">${avatar(m.name)}<b>${m.name}</b>${m.id === party.party.leaderId ? html`<span class="badge">Leiter</span>` : ''}<span class="spacer"></span>${party.party.leaderId === state.user.id && m.id !== state.user.id ? html`<button class="btn small" data-lead="${m.id}">Zum Leiter</button><button class="btn small" data-kick="${m.id}">Entfernen</button>` : ''}</div>`)}
            <div class="chat" id="partyChat"></div><form class="row" id="partySend"><input name="body" maxlength="500" placeholder="Nachricht an die Party" required><button class="btn">Senden</button></form>
            <button class="btn danger" id="leave">Party verlassen</button></div>`
            : html`<p class="muted">Du bist in keiner Party.</p><button class="btn primary" id="createParty">Party erstellen</button>`}
          ${party.invites.map((i) => html`<div class="row"><span>Einladung von <b>${i.from.name}</b></span><span class="spacer"></span><button class="btn small primary" data-join="${i.partyId}">Beitreten</button><button class="btn small" data-pdecline="${i.partyId}">Ablehnen</button></div>`)}
        </div>
        <div class="card stack"><h3>Blockiert</h3>${blocks.blocked.length ? blocks.blocked.map((b) => html`<div class="row"><span>${b.name}</span><span class="spacer"></span><button class="btn small" data-unblock="${b.id}">Aufheben</button></div>`) : html`<p class="muted small">Niemand blockiert.</p>`}</div>
      </div>
    </div>`);

  const reload = () => friends(el);
  const act = async (fn, msg) => { try { await fn(); if (msg) toast(msg, 'ok'); await reload(); } catch (e) { errorToast(e); } };
  el.querySelector('#add').addEventListener('submit', (e) => {
    e.preventDefault();
    const player = new FormData(e.currentTarget).get('player');
    void act(() => api('POST', '/api/friends/requests', { player }), 'Anfrage gesendet');
  });
  // Property handler: re-rendering replaces it instead of stacking listeners.
  el.onclick = async (e) => {
    const b = e.target.closest('button');
    if (!b) return;
    const d = b.dataset;
    if (d.accept) await act(() => api('POST', `/api/friends/requests/${d.accept}/accept`), 'Freund hinzugefügt');
    else if (d.decline) await act(() => api('POST', `/api/friends/requests/${d.decline}/decline`));
    else if (d.cancel) await act(() => api('DELETE', `/api/friends/requests/${d.cancel}`));
    else if (d.remove) { if (await confirmDialog('Freund entfernen?', `${d.name} wird aus deiner Freundesliste entfernt.`, { danger: true })) await act(() => api('DELETE', `/api/friends/${d.remove}`)); }
    else if (d.block) { if (await confirmDialog('Blockieren?', `${d.block} kann dir dann nicht mehr schreiben oder Anfragen senden.`, { danger: true })) await act(() => api('POST', '/api/blocks', { player: d.block }), 'Blockiert'); }
    else if (d.unblock) await act(() => api('DELETE', `/api/blocks/${d.unblock}`));
    else if (d.note) {
      const note = prompt('Notiz (nur für dich sichtbar)', d.current ?? '');
      if (note !== null) await act(() => api('PUT', `/api/friends/${d.note}/note`, { note }));
    }
    else if (d.invite) await act(() => api('POST', '/api/party/invite', { player: d.invite }), 'Eingeladen');
    else if (d.join) await act(() => api('POST', `/api/party/${d.join}/join`), 'Party beigetreten');
    else if (d.pdecline) await act(() => api('POST', `/api/party/${d.pdecline}/decline`));
    else if (d.kick) await act(() => api('POST', '/api/party/kick', { userId: d.kick }));
    else if (d.lead) await act(() => api('POST', '/api/party/leader', { userId: d.lead }));
    else if (b.id === 'createParty') await act(() => api('POST', '/api/party'), 'Party erstellt');
    else if (b.id === 'leave') await act(() => api('POST', '/api/party/leave'));
  };
  if (party.party) {
    const box = el.querySelector('#partyChat');
    const msgs = await api('GET', '/api/party/messages');
    const names = Object.fromEntries(party.party.members.map((m) => [m.id, m.name]));
    mount(box, html`${msgs.messages.map((m) => html`<div class="msg ${m.from_id === state.user.id ? 'mine' : ''}"><b class="small">${names[m.from_id] ?? '?'}</b><br>${m.body}</div>`)}`);
    box.scrollTop = box.scrollHeight;
    el.querySelector('#partySend').addEventListener('submit', async (e) => {
      e.preventDefault();
      const input = e.currentTarget.elements.body;
      try { await api('POST', '/api/party/messages', { body: input.value }); input.value = ''; await reload(); } catch (err) { errorToast(err); }
    });
  }
}

export async function chat(el, partnerId) {
  const data = await api('GET', `/api/messages/${partnerId}`);
  const f = await api('GET', '/api/friends');
  const partner = f.friends.find((x) => x.id === partnerId);
  const name = partner?.name ?? 'Spieler';
  mount(el, html`<div class="row"><a class="btn small" href="/friends" data-link>← Freunde</a><h1>${name}</h1></div>
    <div class="chat" id="box">${data.messages.map((m) => html`<div class="msg ${m.from_id === state.user.id ? 'mine' : ''}" title="${fmtDate(m.created_at)}">${m.body}</div>`)}</div>
    <form class="row spaced" id="send"><input name="body" maxlength="1000" placeholder="Nachricht…" required autocomplete="off"><button class="btn primary">Senden</button></form>`);
  const box = el.querySelector('#box');
  box.scrollTop = box.scrollHeight;
  el.querySelector('#send').addEventListener('submit', async (e) => {
    e.preventDefault();
    const input = e.currentTarget.elements.body;
    try {
      const m = await api('POST', `/api/messages/${partnerId}`, { body: input.value });
      input.value = '';
      const div = document.createElement('div');
      div.className = 'msg mine';
      div.textContent = m.body;
      box.append(div);
      box.scrollTop = box.scrollHeight;
    } catch (err) { errorToast(err); }
  });
}
