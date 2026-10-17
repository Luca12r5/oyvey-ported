// Play screen: 3D character on a glowing stage, gradient PLAY button with a
// profile dropdown, install progress, daily progress and news.

import { backgroundLowPower, go, loadIdentity, onLeave, state } from '../app.ts';
import { api, errorToast, fmtDate, fmtNum, html, lego, mount, toast } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { thumbnail, type Scene3DId } from '../bg3d.ts';
import { createAvatar, type Anim, type Avatar } from '../avatar.ts';
import { t } from '../i18n.ts';
import type { GameProfile } from '../../core/settings.ts';
import type { LaunchProgress } from '../../ipc-types.ts';
import { signInFlow } from './account.ts';
import { isProblem, parseLine, type LogEntry } from '../logparse.ts';
import { showCrashDoctor } from '../crashdoc-ui.ts';
import type { Diagnosis } from '../../core/crashdoc.ts';

const logLines: LogEntry[] = [];
let logFilter: 'all' | 'warn' | 'error' = 'all';
const shown = (e: LogEntry) => logFilter === 'all' || (logFilter === 'error' ? isProblem(e) : e.level !== 'INFO' && e.level !== 'DEBUG');
const anim: Anim = 'idle';

const LOADER_NAMES: Record<string, string> = { fabric: 'Fabric', quilt: 'Quilt', forge: 'Forge', neoforge: 'NeoForge' };

export function profileLine(p: GameProfile): string {
  // The LEGO flag is shown unless the profile name already says so.
  const parts = [p.legoClient && !/lego/i.test(p.name) ? 'LEGO Client' : null, p.loaderType ? LOADER_NAMES[p.loaderType] : t('play.vanilla'), p.gameVersion];
  return parts.filter(Boolean).join(' · ');
}

interface Progress { daily: { claimedToday: boolean; streak: number; nextReward: number }; quests: { id: string; title: { de: string; en?: string }; period: string; progress: number; target: number; credits: number; complete: boolean; claimed: boolean }[] }

export async function playPage(el: HTMLElement): Promise<void> {
  const s = state.settings;
  const profile = s.profiles.find((p) => p.id === s.selectedProfile) ?? s.profiles[0]!;
  const running = await api.gameRunning();
  const a = state.auth;
  mount(el, html`<div class="play">
    <section class="stage panel">
      <div class="grid-floor" aria-hidden="true"><i class="gf-floor"></i><i class="gf-ceil"></i></div>
      <div class="nametag">${icon('pumpkin', 'sm')}<span>${a.signedIn ? a.name : s.guestName || 'Gast'}</span></div>
      <canvas id="avatar"></canvas>
    </section>
    <aside class="side panel">
      <div class="panel-h">${icon('news', 'sm')}<span>${t('play.news')}</span><button class="link" id="allNews" title="Alle News">${icon('chevronRight', 'sm')}</button></div>
      <div id="progressCard" class="coin-row"><div class="skeleton"></div></div>
      <div class="news" id="news"><div class="skeleton"></div></div>
    </aside>
    <div class="launchbar">
      ${a.signedIn ? html`
        <div class="play-group ${running ? 'running' : ''}">
          <button class="play-btn" id="launch" ${running ? 'disabled' : ''}>
            <span class="pb-title">${running ? t('play.running') : t('play.play')}</span>
            <span class="pb-sub">${pixel(profile.icon, 'xs')}${profile.name} · ${profileLine(profile)}</span>
          </button>
          <button class="play-drop" id="drop" aria-label="${t('play.profile')}" aria-haspopup="menu">${icon('chevron')}</button>
        </div>`
      : html`<div class="play-group"><button class="play-btn" id="signin"><span class="pb-title">${t('acc.signin')}</span><span class="pb-sub">${t('play.signin')}</span></button></div>
        <div class="guest-note">${icon('ghost', 'sm')}<span><b>Gastmodus</b> – Oberfläche, Designs, Startanimation, Charakter und VR-Vorschau sind frei testbar. Zum Spielen brauchst du ein Microsoft-Konto mit Minecraft: Java Edition.</span></div>`}
      <div class="status glass" id="status" hidden></div>
      <button class="link small" id="console">${icon('terminal', 'sm')} ${t('play.console')}</button>
    </div>
    <div class="glass console-card" id="consoleCard" hidden>
      <div class="console-head"><h3>${icon('terminal', 'sm')} ${t('play.console')}</h3>
        <div class="seg" id="lvl"><button data-lvl="all">Alle</button><button data-lvl="warn">Warnungen <b id="cWarn">0</b></button><button data-lvl="error">Fehler <b id="cErr">0</b></button></div>
        <div class="row"><button class="btn sm" id="firstErr" disabled>Zum ersten Fehler</button><button class="btn sm" id="crashDoc">${icon('wrench', 'sm')} Crash-Analyse</button><button class="icon-btn" id="openLogs" title="Log-Ordner">${icon('folder', 'sm')}</button><button class="btn sm" id="copyLog">Kopieren</button><button class="icon-btn" id="closeConsole" title="Schließen">${icon('close', 'sm')}</button></div></div>
      <div class="console" id="log" role="log" aria-live="off"></div></div>
  </div>`);

  // ---- character
  let avatar: Avatar | null = null;
  try {
    avatar = createAvatar(el.querySelector<HTMLCanvasElement>('#avatar')!, s.reducedMotion);
    void avatar.setSkin(state.skin.skin, state.skin.variant, state.skin.cape).catch(() => {});
    avatar.play(anim);
    setTimeout(() => avatar?.react('greet'), 700);
  } catch (e) {
    console.warn('avatar unavailable', e);
  }
  // ---- launch / progress / log
  el.querySelector('#signin')?.addEventListener('click', () => void signInFlow());
  el.querySelector('#allNews')!.addEventListener('click', () => void go('news'));
  el.querySelector('#drop')?.addEventListener('click', (e) => profileMenu(e.currentTarget as HTMLElement));
  const statusBox = el.querySelector<HTMLElement>('#status')!;
  const logBox = el.querySelector<HTMLElement>('#log')!;
  const consoleCard = el.querySelector<HTMLElement>('#consoleCard')!;
  const row = (e: LogEntry) => {
    const d = document.createElement('div');
    d.className = `ln lv-${e.level}`;
    for (const [cls, txt] of [['ts', e.time], ['lv', e.level], ['src', e.source], ['tx', e.text]] as const) {
      const sp = document.createElement('span');
      sp.className = cls;
      sp.textContent = txt;
      d.append(sp);
    }
    return d;
  };
  const counts = () => {
    el.querySelector('#cWarn')!.textContent = String(logLines.filter((e) => e.level === 'WARN').length);
    const errs = logLines.filter(isProblem).length;
    el.querySelector('#cErr')!.textContent = String(errs);
    el.querySelector<HTMLButtonElement>('#firstErr')!.disabled = errs === 0;
  };
  const drawLog = () => {
    logBox.replaceChildren(...logLines.filter(shown).slice(-600).map(row));
    logBox.scrollTop = logBox.scrollHeight;
    el.querySelectorAll<HTMLButtonElement>('#lvl button').forEach((b) => b.classList.toggle('on', b.dataset.lvl === logFilter));
    counts();
  };
  const appendLog = (e: LogEntry) => {
    const stick = logBox.scrollHeight - logBox.scrollTop - logBox.clientHeight < 40;
    if (shown(e)) logBox.append(row(e));
    while (logBox.childElementCount > 600) logBox.firstElementChild!.remove();
    if (stick) logBox.scrollTop = logBox.scrollHeight;
    counts();
  };
  const openConsole = (open: boolean) => { consoleCard.hidden = !open; if (open) drawLog(); };
  drawLog();
  el.querySelector('#console')!.addEventListener('click', () => openConsole(consoleCard.hidden));
  el.querySelector('#closeConsole')!.addEventListener('click', () => openConsole(false));
  el.querySelector('#lvl')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-lvl]');
    if (b) { logFilter = b.dataset.lvl as typeof logFilter; drawLog(); }
  });
  el.querySelector('#firstErr')!.addEventListener('click', () => {
    if (logFilter !== 'all') { logFilter = 'all'; drawLog(); }
    const first = logBox.querySelector<HTMLElement>('.lv-ERROR, .lv-FATAL');
    first?.scrollIntoView({ block: 'center' });
    first?.classList.add('flash');
  });
  el.querySelector('#openLogs')!.addEventListener('click', () => void api.openFolder('logs'));
  el.querySelector('#crashDoc')!.addEventListener('click', async () => {
    try { const r = await api.crashAnalyze(profile.id); showCrashDoctor(r.profileId, r.diagnoses); } catch (e) { errorToast(e); }
  });
  el.querySelector('#copyLog')!.addEventListener('click', () => void navigator.clipboard.writeText(logLines.map((l) => `[${l.time}] [${l.source}/${l.level}] ${l.text}`).join('\n')).then(() => toast('Log kopiert', 'ok')));

  const unsub = [
    api.on('launch-progress', (p: LaunchProgress) => {
      const pct = p.total ? Math.round(((p.done ?? 0) / p.total) * 100) : null;
      statusBox.hidden = false;
      mount(statusBox, html`<div class="row between small"><b>${p.step}</b><span class="muted">${pct !== null ? `${pct} %` : ''}${p.totalBytes ? ` · ${Math.round((p.bytes ?? 0) / 1048576)} / ${Math.round(p.totalBytes / 1048576)} MB` : ''}</span></div>
        <div class="progress ${pct === null ? 'indet' : ''}"><i data-p="${pct ?? 100}"></i></div>${pct !== null && pct < 100 ? html`<button class="link small" id="cancel">${icon('close', 'sm')} ${t('play.cancel')}</button>` : ''}`);
      statusBox.querySelector('#cancel')?.addEventListener('click', () => api.cancelLaunch());
    }),
    api.on('game-log', (l: { line: string; stream: string }) => {
      const entry = parseLine(l.line, l.stream);
      logLines.push(entry);
      if (logLines.length > 5000) logLines.splice(0, 1000);
      if (!consoleCard.hidden) appendLog(entry);
    }),
    api.on('game-exit', (r: { code: number; hint: string | null; diagnoses?: Diagnosis[]; profileId?: string }) => {
      backgroundLowPower(false);
      if (r.code === 0) toast('Minecraft wurde beendet.');
      else if (r.profileId) showCrashDoctor(r.profileId, r.diagnoses ?? [], r.code);
      else toast(`Minecraft ist abgestürzt (Code ${r.code}). ${r.hint ?? 'Details im Log.'}`, 'error');
      void loadIdentity();
      if (state.page === 'play') void go('play');
    }),
  ];
  onLeave(() => {
    unsub.forEach((u) => u());
    avatar?.dispose();
  });

  el.querySelector('#launch')?.addEventListener('click', async () => {
    const btn = el.querySelector<HTMLButtonElement>('#launch')!;
    btn.disabled = true;
    btn.parentElement!.classList.add('busy');
    mount(btn.querySelector('.pb-title')!, html`${t('play.installing')}`);
    logLines.length = 0;
    avatar?.react('launch');
    if (s.autoConsole) openConsole(true);
    const r = await api.launch(profile.id);
    btn.parentElement!.classList.remove('busy');
    if (!r.ok) {
      errorToast(new Error(r.error ?? 'Start fehlgeschlagen'));
      btn.disabled = false;
      mount(btn.querySelector('.pb-title')!, html`${t('play.play')}`);
      statusBox.hidden = true;
      avatar?.play(anim);
    } else {
      btn.parentElement!.classList.add('running');
      mount(btn.querySelector('.pb-title')!, html`${t('play.running')}`);
      statusBox.hidden = true;
      avatar?.react('success');
      backgroundLowPower(true);
      toast(t('play.fun'), 'ok');
    }
  });

  // ---- side cards (real data from the LEGO server; hidden pieces when offline)
  const card = el.querySelector<HTMLElement>('#progressCard')!;
  if (!a.lego.connected) {
    mount(card, html`<div class="row">${pixel('coin', 'md')}<div><h3>${t('play.coins')}</h3><p class="small muted">${t('play.dailyHint')}</p></div></div>`);
  } else {
    try {
      const [p, c] = await Promise.all([lego<Progress>('GET', '/api/progress'), lego<{ balance: number; history: { delta: number; created_at: number }[] }>('GET', '/api/me/credits')]);
      const dayStart = new Date().setHours(0, 0, 0, 0);
      const today = c.history.filter((h) => h.created_at >= dayStart && h.delta > 0).reduce((n, h) => n + h.delta, 0);
      const q = p.quests.find((x) => x.period === 'daily' && !x.claimed) ?? p.quests.find((x) => !x.claimed);
      mount(card, html`<div class="row between"><div class="row">${pixel('coin', 'md')}<div><div class="kpi">${fmtNum(c.balance)}</div><div class="small muted">${t('play.coins')} · ${t('play.today')} +${fmtNum(today)}</div></div></div>
          <button class="btn sm ${p.daily.claimedToday ? '' : 'primary'}" id="daily" ${p.daily.claimedToday ? 'disabled' : ''}>${pixel('star', 'xs')} ${p.daily.claimedToday ? `${p.daily.streak}×` : `+${p.daily.nextReward}`}</button></div>
        ${q ? html`<div class="quest"><div class="row between small"><span>${q.title.de}</span><span class="muted">${q.progress}/${q.target}</span></div><div class="progress"><i data-p="${(q.progress / q.target) * 100}"></i></div><div class="small muted">+${q.credits} Coins</div></div>` : ''}`);
      card.querySelector('#daily')?.addEventListener('click', async () => {
        try {
          await lego('POST', '/api/daily/claim');
          toast('Tägliche Belohnung abgeholt', 'ok');
          await loadIdentity();
          void go('play');
        } catch (err) { errorToast(err); }
      });
    } catch {
      mount(card, html`<p class="small muted">${t('play.dailyHint')}</p>`);
    }
  }
  const newsBox = el.querySelector<HTMLElement>('#news')!;
  type News = { id: number; title: string; body: string; created_at: number };
  let items: News[] = [];
  try {
    items = (await lego<{ news: News[] }>('GET', '/api/public/news')).news.slice(0, 6);
  } catch { /* offline: built-in notes below */ }
  if (!items.length) items = BUILTIN_NEWS;
  const scenes: Scene3DId[] = ['graveyard', 'castle', 'portal', 'forest', 'tunnel', 'rooms'];
  mount(newsBox, html`${items.map((x, i) => html`<article class="news-card">
      <div class="nc-title">${x.title}</div>
      <div class="nc-img"><img data-scene="${scenes[i % scenes.length]!}" alt=""><span class="nc-date">${fmtDate(x.created_at)}</span>${icon('pumpkin', 'sm')}</div>
      <p class="small">${x.body.slice(0, 110)}${x.body.length > 110 ? '…' : ''}</p></article>`)}`);
  // News art: small renders of the 3D scenes, one after another to stay responsive.
  const cs = getComputedStyle(document.documentElement);
  const pal = { bg: cs.getPropertyValue('--bg').trim() || '#05040a', accent: cs.getPropertyValue('--accent').trim() || '#ff7a1a', accent2: cs.getPropertyValue('--accent2').trim() || '#7c3aed' };
  const imgs = [...newsBox.querySelectorAll<HTMLImageElement>('img[data-scene]')];
  const next = () => {
    const img = imgs.shift();
    if (!img || !img.isConnected) return;
    try { img.src = thumbnail(img.dataset.scene as Scene3DId, pal); } catch { /* no WebGL */ }
    setTimeout(next, 40);
  };
  setTimeout(next, 120);
}

const BUILTIN_NEWS = [
  { id: -1, title: 'Halloween Crypt', body: 'Neuer dunkler Look, Höllen-Intro, Skins-Tab und Seitenübergänge zum Auswählen.', created_at: Date.UTC(2026, 9, 17) },
  { id: -2, title: 'VR-Modus', body: 'Visueller VR-Modus mit Tastatur und Maus oder echtes VR mit Headset – mit Browser in der Lobby.', created_at: Date.UTC(2026, 9, 16) },
  { id: -3, title: 'Konsole & Crash-Hilfe', body: 'Die Konsole öffnet sich beim Spielstart und zeigt Fehler farbig an.', created_at: Date.UTC(2026, 9, 15) },
];

/** Set by the profile menu; the profiles page opens the editor directly. */
export let newProfileRequested = false;
export function takeNewProfileRequest(): boolean {
  const v = newProfileRequested;
  newProfileRequested = false;
  return v;
}

export function profileMenu(anchor: HTMLElement, dir: 'up' | 'down' = 'up'): void {
  document.querySelector('.menu')?.remove();
  const s = state.settings;
  const m = document.createElement('div');
  m.className = `menu ${dir}`;
  m.setAttribute('role', 'menu');
  const sorted = [...s.profiles].sort((a, b) => (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0));
  mount(m, html`<div class="menu-h">${t('play.profile')}</div>
    <div class="menu-scroll">${sorted.map((p) => html`<button class="menu-i ${p.id === s.selectedProfile ? 'on' : ''}" data-profile="${p.id}" role="menuitem">${pixel(p.icon, 'sm')}<span class="grow"><b>${p.name}</b><small>${profileLine(p)}</small></span>${p.id === s.selectedProfile ? icon('check', 'sm') : ''}</button>`)}</div>
    <div class="menu-sep"></div>
    <button class="menu-i" data-act="new" role="menuitem">${icon('plus', 'sm')}<span class="grow">${t('play.newProfile')}</span></button>
    <button class="menu-i" data-act="manage" role="menuitem">${icon('profiles', 'sm')}<span class="grow">${t('nav.profiles')}</span></button>`);
  document.body.append(m);
  if (dir === 'up') {
    const group = anchor.parentElement!.getBoundingClientRect();
    m.style.left = `${group.left}px`;
    m.style.width = `${group.width}px`;
    m.style.bottom = `${innerHeight - group.top + 8}px`;
  } else {
    const r = anchor.getBoundingClientRect();
    m.style.top = `${r.bottom + 6}px`;
    m.style.left = `${Math.max(8, Math.min(r.left, innerWidth - 340))}px`;
    m.style.width = '320px';
  }
  const close = (e?: Event) => {
    if (e && m.contains(e.target as Node)) return;
    m.remove();
    document.removeEventListener('mousedown', close);
  };
  setTimeout(() => document.addEventListener('mousedown', close));
  m.addEventListener('click', async (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('button');
    if (!b) return;
    close();
    if (b.dataset.profile) {
      state.settings = await api.setSettings({ selectedProfile: b.dataset.profile });
      void go('play');
    } else if (b.dataset.act === 'new') {
      newProfileRequested = true;
      void go('profiles');
    } else void go('profiles');
  });
}
