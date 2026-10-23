// Own Minecraft server on this PC: create (version, vanilla/Fabric, RAM, game rules), accept
// the Minecraft EULA, start/stop, live console with commands, join with one click.

import { onLeave, state } from '../app.ts';
import { api, confirm, errorToast, html, mount, toast } from '../ui.ts';
import { icon } from '../icons.ts';
import type { LocalServer } from '../../core/server.ts';

type Srv = LocalServer & { running: boolean };

export async function serversPage(el: HTMLElement, selected?: string): Promise<void> {
  const list = await api.servers();
  const cur = list.find((s) => s.id === selected) ?? list[0] ?? null;
  const profiles = state.settings.profiles;
  mount(el, html`<div class="page-head"><div><h1>${icon('globe', 'sm')} Eigener Server</h1><p class="muted small">Ein Minecraft-Server auf diesem PC. Du und Freunde im selben Netzwerk können darauf spielen; für Freunde über das Internet muss der Port im Router freigegeben werden.</p></div>
      <button class="btn primary" id="newSrv">${icon('plus', 'sm')} Neuer Server</button></div>
    ${list.length ? html`<div class="srv">
      <aside class="srv-list">${list.map((s) => html`<button class="srv-item ${s.id === cur?.id ? 'on' : ''}" data-sel="${s.id}"><b>${s.name}</b><small>${s.gameVersion} · ${s.loader === 'fabric' ? 'Fabric' : 'Vanilla'} · Port ${s.port}</small><i class="dot ${s.running ? 'online' : 'offline'}"></i></button>`)}</aside>
      ${cur ? html`<section class="srv-main card">
        <div class="row between"><h2>${cur.name}</h2><span class="badge ${cur.running ? 'ok' : ''}" id="srvState">${cur.running ? 'Läuft' : 'Aus'}</span></div>
        <div class="row gap wrap">
          ${cur.running ? html`<button class="btn danger" id="stop">${icon('power', 'sm')} Stoppen</button>` : html`<button class="btn primary" id="start">${icon('play', 'sm')} Starten</button>`}
          <select id="joinProfile" class="compact" title="Mit diesem Profil beitreten">${profiles.filter((p) => p.gameVersion === cur.gameVersion).map((p) => html`<option value="${p.id}">${p.name}</option>`)}<option value="">— Profil mit ${cur.gameVersion} —</option></select>
          <button class="btn" id="join" ${cur.running ? '' : 'disabled'}>${icon('gamepad', 'sm')} Beitreten</button>
          <button class="btn" id="folder">${icon('folder', 'sm')} Ordner</button>
          <button class="btn" id="edit">${icon('settings', 'sm')} Einstellungen</button>
          <button class="btn danger" id="del" ${cur.running ? 'disabled' : ''}>${icon('trash', 'sm')} Löschen</button>
        </div>
        <p class="small muted">Adresse für Freunde im Netzwerk: <b>deine-PC-IP:${cur.port}</b> · auf diesem PC: <b>localhost:${cur.port}</b></p>
        <div class="srv-console" id="log" role="log"></div>
        <form class="row gap" id="cmdForm"><input id="cmd" class="grow" maxlength="256" placeholder="${cur.running ? 'Befehl, z. B. op DeinName, time set day, say Hallo' : 'Server ist aus'}" ${cur.running ? '' : 'disabled'} autocomplete="off"><button class="btn" ${cur.running ? '' : 'disabled'}>Senden</button></form>
      </section>` : ''}
    </div>` : html`<div class="empty">${icon('globe')}<p>Noch kein Server. Leg einen an: Version wählen, Start drücken, mitspielen.</p></div>`}`);

  el.querySelector('#newSrv')!.addEventListener('click', () => void editServer(el, null));
  el.querySelectorAll<HTMLButtonElement>('[data-sel]').forEach((b) => b.addEventListener('click', () => void serversPage(el, b.dataset.sel)));
  if (!cur) return;
  const logBox = el.querySelector<HTMLElement>('#log')!;
  const add = (line: string) => {
    const d = document.createElement('div');
    d.className = /ERROR|Exception/.test(line) ? 'err' : /WARN/.test(line) ? 'warn' : line.startsWith('[LEGO]') ? 'lego' : '';
    d.textContent = line;
    logBox.append(d);
    while (logBox.childElementCount > 1500) logBox.firstElementChild!.remove();
    if (logBox.scrollHeight - logBox.scrollTop - logBox.clientHeight < 120) logBox.scrollTop = logBox.scrollHeight;
  };
  for (const l of await api.serverLog(cur.id)) add(l);
  logBox.scrollTop = logBox.scrollHeight;
  const offLog = api.on('server-log', (e: { id: string; line: string }) => { if (e.id === cur.id) add(e.line); });
  const offState = api.on('server-state', (e: { id: string }) => { if (el.isConnected) void serversPage(el, e.id === cur.id ? cur.id : selected); });
  onLeave(() => { offLog(); offState(); });

  const run = (fn: () => Promise<unknown>) => fn().catch(errorToast);
  el.querySelector('#start')?.addEventListener('click', () => run(async () => {
    if (!cur.eulaAccepted) {
      if (!(await confirm('Minecraft-EULA', 'Für einen eigenen Server musst du der Minecraft-Endbenutzer-Lizenzvereinbarung (EULA) von Mojang zustimmen: https://aka.ms/MinecraftEULA. Zustimmen und starten?', 'Zustimmen'))) return;
      await api.saveServer({ id: cur.id, eulaAccepted: true });
    }
    toast('Server startet – beim ersten Mal werden Java und die Server-Datei geladen.', 'info');
    await api.startServer(cur.id);
  }));
  el.querySelector('#stop')?.addEventListener('click', () => run(async () => { await api.stopServer(cur.id); toast('Server speichert und stoppt …', 'info'); }));
  el.querySelector('#folder')!.addEventListener('click', () => run(() => api.serverFolder(cur.id)));
  el.querySelector('#edit')!.addEventListener('click', () => void editServer(el, cur));
  el.querySelector('#del')!.addEventListener('click', () => run(async () => {
    if (!(await confirm('Server löschen?', `„${cur.name}“ und seine Welt werden gelöscht.`, 'Löschen', true))) return;
    await api.deleteServer(cur.id);
    await serversPage(el);
  }));
  el.querySelector('#join')!.addEventListener('click', () => run(async () => {
    const pid = el.querySelector<HTMLSelectElement>('#joinProfile')!.value;
    if (!pid) { toast(`Leg zuerst ein Profil mit Minecraft ${cur.gameVersion} an.`, 'error'); return; }
    toast('Minecraft startet und verbindet sich mit deinem Server …', 'info');
    const r = await api.joinServer(cur.id, pid);
    if (!r.ok) throw new Error(r.error ?? 'Start fehlgeschlagen');
  }));
  el.querySelector('#cmdForm')!.addEventListener('submit', (e) => {
    e.preventDefault();
    const i = el.querySelector<HTMLInputElement>('#cmd')!;
    if (i.value.trim()) void api.serverCommand(cur.id, i.value.trim());
    i.value = '';
  });
}

async function editServer(el: HTMLElement, s: Srv | null): Promise<void> {
  let versions: string[] = [];
  try { versions = (await api.versions()).filter((v) => v.type === 'release').map((v) => v.id).slice(0, 40); } catch { versions = ['26.3']; }
  const v = s ?? { name: 'Mein Server', gameVersion: versions[0] ?? '26.3', loader: 'vanilla', memoryMb: 2048, port: 25565, motd: 'LEGO Server', maxPlayers: 10, gamemode: 'survival', difficulty: 'normal', pvp: true } as Partial<Srv>;
  const opt = (val: string, cur: unknown, label: string) => html`<option value="${val}" ${val === cur ? 'selected' : ''}>${label}</option>`;
  mount(el, html`<div class="page-head"><h1>${s ? 'Server bearbeiten' : 'Neuer Server'}</h1></div>
    <form class="editor glass" id="sf">
      <label>Name</label><input name="name" value="${v.name}" maxlength="40" required>
      <div class="row gap"><div class="grow"><label>Minecraft-Version</label><select name="gameVersion">${versions.map((x) => opt(x, v.gameVersion, x))}</select></div>
        <div class="grow"><label>Art</label><select name="loader">${opt('vanilla', v.loader, 'Vanilla')}${opt('fabric', v.loader, 'Fabric (für Server-Mods)')}</select></div></div>
      <div class="row gap"><div class="grow"><label>RAM (MB)</label><input name="memoryMb" type="number" min="1024" max="32768" step="512" value="${v.memoryMb}"></div>
        <div class="grow"><label>Port</label><input name="port" type="number" min="1025" max="65535" value="${v.port}"></div>
        <div class="grow"><label>Max. Spieler</label><input name="maxPlayers" type="number" min="1" max="200" value="${v.maxPlayers}"></div></div>
      <label>Beschreibung (MOTD)</label><input name="motd" value="${v.motd}" maxlength="59">
      <div class="row gap"><div class="grow"><label>Spielmodus</label><select name="gamemode">${opt('survival', v.gamemode, 'Überleben')}${opt('creative', v.gamemode, 'Kreativ')}${opt('adventure', v.gamemode, 'Abenteuer')}</select></div>
        <div class="grow"><label>Schwierigkeit</label><select name="difficulty">${opt('peaceful', v.difficulty, 'Friedlich')}${opt('easy', v.difficulty, 'Leicht')}${opt('normal', v.difficulty, 'Normal')}${opt('hard', v.difficulty, 'Schwer')}</select></div>
        <label class="check"><input type="checkbox" name="pvp" ${v.pvp ? 'checked' : ''}> PvP</label></div>
      <p class="tiny muted">Online-Modus bleibt immer an: nur echte Minecraft-Konten können beitreten.</p>
      <div class="row gap"><button class="btn primary">${icon('check', 'sm')} Speichern</button><button class="btn" type="button" id="cancel">Zurück</button></div>
    </form>`);
  el.querySelector('#cancel')!.addEventListener('click', () => void serversPage(el, s?.id));
  el.querySelector<HTMLFormElement>('#sf')!.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target as HTMLFormElement);
    try {
      const saved = await api.saveServer({
        ...(s ? { id: s.id } : {}), name: String(fd.get('name')), gameVersion: String(fd.get('gameVersion')), loader: fd.get('loader') as LocalServer['loader'],
        memoryMb: Number(fd.get('memoryMb')), port: Number(fd.get('port')), maxPlayers: Number(fd.get('maxPlayers')), motd: String(fd.get('motd')),
        gamemode: fd.get('gamemode') as LocalServer['gamemode'], difficulty: fd.get('difficulty') as LocalServer['difficulty'], pvp: fd.get('pvp') === 'on',
      });
      toast('Server gespeichert', 'ok');
      await serversPage(el, saved.id);
    } catch (err) { errorToast(err); }
  });
}
