import { state } from '../app.ts';
import { confirm, errorToast, html, lego, mount, prompt, toast } from '../ui.ts';

interface Friend { id: string; name: string; status: string; activity: string | null; gameVersion: string | null; note: string }
interface FriendList { friends: Friend[]; incoming: { id: string; name: string }[]; outgoing: { id: string; name: string }[] }
interface Party { party: { id: string; leaderId: string; members: { id: string; name: string; status: string }[] } | null; invites: { partyId: string; from: { name: string } }[] }

const LABEL: Record<string, string> = { online: 'Online', in_game: 'Im Spiel', away: 'Abwesend', offline: 'Offline', hidden: 'Verborgen' };

export async function friendsPage(el: HTMLElement, chatWith?: Friend): Promise<void> {
  const [f, p] = await Promise.all([lego<FriendList>('GET', '/api/friends'), lego<Party>('GET', '/api/party')]);
  const me = state.auth.lego.userId;
  const leader = p.party?.leaderId === me;
  mount(el, html`<h1>Freunde</h1><div class="grid g2">
    <div>
      <form class="card row" id="add"><input name="player" class="grow" placeholder="Minecraft-Name" minlength="3" maxlength="16" required><button class="btn primary">Anfrage senden</button></form>
      ${f.incoming.length ? html`<div class="card gap"><h3>Anfragen</h3>${f.incoming.map((r) => html`<div class="row"><b class="grow">${r.name}</b><button class="btn sm primary" data-accept="${r.id}">Annehmen</button><button class="btn sm" data-decline="${r.id}">Ablehnen</button></div>`)}</div>` : ''}
      <div class="card gap"><h3>Freunde (${f.friends.length})</h3>
        ${f.friends.length ? f.friends.map((x) => html`<div class="row gap"><span class="dot ${x.status}"></span><div class="grow"><b>${x.name}</b><div class="small muted">${LABEL[x.status] ?? x.status}${x.activity ? ` · ${x.activity}` : ''}${x.gameVersion ? ` · ${x.gameVersion}` : ''}${x.note ? ` · 📝 ${x.note}` : ''}</div></div>
          <button class="btn sm" data-chat="${x.id}">Chat</button>${leader ? html`<button class="btn sm" data-invite="${x.name}">Einladen</button>` : ''}<button class="btn sm" data-note="${x.id}" data-current="${x.note}">Notiz</button><button class="btn sm danger" data-remove="${x.id}" data-name="${x.name}">✕</button></div>`)
        : html`<div class="empty">Noch keine Freunde.</div>`}
      </div>
    </div>
    <div>
      <div class="card"><h3>Party</h3>
        ${p.party ? html`${p.party.members.map((m) => html`<div class="row"><span class="dot ${m.status}"></span><b class="grow">${m.name}</b>${m.id === p.party!.leaderId ? html`<span class="badge">Leiter</span>` : ''}${leader && m.id !== me ? html`<button class="btn sm" data-kick="${m.id}">Entfernen</button>` : ''}</div>`)}<button class="btn sm danger gap" id="leave">Verlassen</button>`
          : html`<p class="muted small">Keine Party.</p><button class="btn sm primary" id="create">Party erstellen</button>`}
        ${p.invites.map((i) => html`<div class="row gap"><span class="grow">Einladung von <b>${i.from.name}</b></span><button class="btn sm primary" data-join="${i.partyId}">Beitreten</button></div>`)}
      </div>
      <div class="card gap" id="chatCard">${chatWith ? html`<h3>Chat mit ${chatWith.name}</h3><div class="chat" id="chat"></div><form class="row gap" id="send"><input name="body" class="grow" maxlength="1000" required placeholder="Nachricht…"><button class="btn primary">Senden</button></form>` : html`<p class="muted small">Wähle einen Freund zum Chatten.</p>`}</div>
    </div></div>`);

  const reload = (w?: Friend) => friendsPage(el, w ?? chatWith);
  const act = async (fn: () => Promise<unknown>, msg?: string) => { try { await fn(); if (msg) toast(msg, 'ok'); await reload(); } catch (e) { errorToast(e); } };
  el.querySelector('#add')!.addEventListener('submit', (e) => {
    e.preventDefault();
    const player = new FormData(e.target as HTMLFormElement).get('player');
    void act(() => lego('POST', '/api/friends/requests', { player }), 'Anfrage gesendet');
  });
  el.onclick = async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    const d = b.dataset;
    if (d.accept) await act(() => lego('POST', `/api/friends/requests/${d.accept}/accept`), 'Freund hinzugefügt');
    else if (d.decline) await act(() => lego('POST', `/api/friends/requests/${d.decline}/decline`));
    else if (d.chat) await reload(f.friends.find((x) => x.id === d.chat));
    else if (d.invite) await act(() => lego('POST', '/api/party/invite', { player: d.invite }), 'Eingeladen');
    else if (d.kick) await act(() => lego('POST', '/api/party/kick', { userId: d.kick }));
    else if (d.join) await act(() => lego('POST', `/api/party/${d.join}/join`), 'Beigetreten');
    else if (d.note) { const n = await prompt('Notiz', 'Nur für dich sichtbar', d.current ?? ''); if (n !== null) await act(() => lego('PUT', `/api/friends/${d.note}/note`, { note: n })); }
    else if (d.remove) { if (await confirm('Entfernen?', `${d.name} aus der Freundesliste entfernen?`, 'Entfernen', true)) await act(() => lego('DELETE', `/api/friends/${d.remove}`)); }
    else if (b.id === 'create') await act(() => lego('POST', '/api/party'));
    else if (b.id === 'leave') await act(() => lego('POST', '/api/party/leave'));
  };
  if (chatWith) {
    const box = el.querySelector<HTMLElement>('#chat')!;
    const msgs = await lego<{ messages: { from_id: string; body: string }[] }>('GET', `/api/messages/${chatWith.id}`);
    for (const m of msgs.messages) {
      const div = document.createElement('div');
      div.className = `msg ${m.from_id === me ? 'mine' : ''}`;
      div.textContent = m.body;
      box.append(div);
    }
    box.scrollTop = box.scrollHeight;
    el.querySelector('#send')!.addEventListener('submit', async (e) => {
      e.preventDefault();
      const input = (e.target as HTMLFormElement).elements.namedItem('body') as HTMLInputElement;
      try {
        const m = await lego<{ body: string }>('POST', `/api/messages/${chatWith.id}`, { body: input.value });
        input.value = '';
        const div = document.createElement('div');
        div.className = 'msg mine';
        div.textContent = m.body;
        box.append(div);
        box.scrollTop = box.scrollHeight;
      } catch (err) { errorToast(err); }
    });
  }
}
