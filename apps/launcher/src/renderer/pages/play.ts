// Play screen: 3D character on a glowing stage, gradient PLAY button with a
// profile dropdown, install progress, daily progress and news.

import { go, loadIdentity, onLeave, state } from '../app.ts';
import { api, errorToast, fmtDate, fmtNum, html, lego, mount, toast } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { createAvatar, type Anim, type Avatar } from '../avatar.ts';
import { t } from '../i18n.ts';
import type { GameProfile } from '../../core/settings.ts';
import type { LaunchProgress } from '../../ipc-types.ts';
import { signInFlow } from './account.ts';
import { officialFlow } from './official.ts';

const logLines: { line: string; stream: string }[] = [];
const skinAnimOn = () => { try { return localStorage.getItem('lego.skinAnim') !== '0'; } catch { return true; } };
let anim: Anim = 'idle';

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
    <div class="play-top">
      <div class="greet"><small>${a.signedIn ? t('play.welcome') : t('play.notSigned')}</small><h1>${a.signedIn ? a.name : 'LEGO Launcher'}</h1></div>
      <div class="row top-ctl">
      <label class="skin-anim">${t('play.skinAnim')}<span class="switch"><input type="checkbox" id="skinAnim" ${skinAnimOn() ? 'checked' : ''}><i></i></span></label>
      <div class="seg" id="anims" role="group">
        ${(['idle', 'walk', 'wave', 'spin'] as Anim[]).map((x) => html`<button class="${x === anim ? 'on' : ''}" data-anim="${x}" title="${t(`play.anim.${x}` as 'play.anim.idle')}">${icon(x === 'idle' ? 'users' : x === 'walk' ? 'gamepad' : x === 'wave' ? 'wave' : 'rotate', 'sm')}</button>`)}
      </div></div>
    </div>
    <section class="stage" aria-hidden="true">
      <div class="floor"></div>
      <div class="halo"></div><div class="halo ring"></div><div class="halo ring r2"></div>
      <div class="pedestal"></div>
      <div class="nametag">${pixel('brick', 'xs')}<span>${a.signedIn ? a.name : 'LEGO'}</span></div>
      <canvas id="avatar"></canvas>
    </section>
    <aside class="side">
      <div class="glass" id="progressCard"><div class="skeleton"></div></div>
      <div class="news"><div class="news-h">${icon('news', 'sm')}<span>${t('play.news')}</span><button class="link" id="allNews">${icon('chevronRight', 'sm')}</button></div><div id="news"><div class="skeleton"></div></div></div>
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
      : state.info.msClientConfigured
        ? html`<div class="play-group"><button class="play-btn" id="signin"><span class="pb-title">${t('acc.signin')}</span><span class="pb-sub">${t('play.signin')}</span></button></div>
          <button class="btn official" id="official">${icon('external', 'sm')} ${t('play.official')}</button>`
        : html`<div class="play-group"><button class="play-btn" id="launchOfficial"><span class="pb-title">${t('play.play')}</span><span class="pb-sub">${pixel(profile.icon, 'xs')}${profile.name} · ${t('play.viaOfficial')}</span></button><button class="play-drop" id="drop" aria-label="${t('play.profile')}" aria-haspopup="menu">${icon('chevron')}</button></div>`}
      <div class="status glass" id="status" hidden></div>
      <button class="link small" id="console">${icon('terminal', 'sm')} ${t('play.console')}</button>
    </div>
    <div class="glass console-card" id="consoleCard" hidden><div class="row between"><h3>Log</h3><div class="row"><button class="btn sm" id="openLogs">${icon('folder', 'sm')}</button><button class="btn sm" id="copyLog">Kopieren</button></div></div><div class="console" id="log"></div></div>
  </div>`);

  // ---- character
  let avatar: Avatar | null = null;
  try {
    avatar = createAvatar(el.querySelector<HTMLCanvasElement>('#avatar')!, s.reducedMotion);
    void avatar.setSkin(state.skin.skin, state.skin.variant, state.skin.cape).catch(() => {});
    avatar.play(anim);
    avatar.setPaused(!skinAnimOn());
  } catch (e) {
    console.warn('avatar unavailable', e);
  }
  el.querySelector('#skinAnim')!.addEventListener('change', (e) => {
    const on = (e.target as HTMLInputElement).checked;
    try { localStorage.setItem('lego.skinAnim', on ? '1' : '0'); } catch { /* storage blocked */ }
    avatar?.setPaused(!on);
  });
  el.querySelector('#anims')!.addEventListener('click', (e) => {
    const b = (e.target as HTMLElement).closest<HTMLButtonElement>('[data-anim]');
    if (!b) return;
    anim = b.dataset.anim as Anim;
    el.querySelectorAll('#anims button').forEach((x) => x.classList.toggle('on', x === b));
    avatar?.play(anim);
  });

  // ---- launch / progress / log
  el.querySelector('#signin')?.addEventListener('click', () => void signInFlow());
  el.querySelector('#official')?.addEventListener('click', () => void officialFlow(profile.id));
  el.querySelector('#launchOfficial')?.addEventListener('click', () => void officialFlow(profile.id));
  el.querySelector('#allNews')!.addEventListener('click', () => void go('news'));
  el.querySelector('#drop')?.addEventListener('click', (e) => profileMenu(e.currentTarget as HTMLElement));
  const statusBox = el.querySelector<HTMLElement>('#status')!;
  const logBox = el.querySelector<HTMLElement>('#log')!;
  const drawLog = () => {
    logBox.replaceChildren(...logLines.slice(-400).map((l) => {
      const d = document.createElement('div');
      if (l.stream === 'err') d.className = 'err';
      d.textContent = l.line;
      return d;
    }));
    logBox.scrollTop = logBox.scrollHeight;
  };
  drawLog();
  el.querySelector('#console')!.addEventListener('click', () => { const c = el.querySelector<HTMLElement>('#consoleCard')!; c.hidden = !c.hidden; if (!c.hidden) drawLog(); });
  el.querySelector('#openLogs')!.addEventListener('click', () => void api.openFolder('logs'));
  el.querySelector('#copyLog')!.addEventListener('click', () => void navigator.clipboard.writeText(logLines.map((l) => l.line).join('\n')).then(() => toast('Log kopiert', 'ok')));

  const unsub = [
    api.on('launch-progress', (p: LaunchProgress) => {
      const pct = p.total ? Math.round(((p.done ?? 0) / p.total) * 100) : null;
      statusBox.hidden = false;
      mount(statusBox, html`<div class="row between small"><b>${p.step}</b><span class="muted">${pct !== null ? `${pct} %` : ''}${p.totalBytes ? ` · ${Math.round((p.bytes ?? 0) / 1048576)} / ${Math.round(p.totalBytes / 1048576)} MB` : ''}</span></div>
        <div class="progress ${pct === null ? 'indet' : ''}"><i data-p="${pct ?? 100}"></i></div>${pct !== null && pct < 100 ? html`<button class="link small" id="cancel">${icon('close', 'sm')} ${t('play.cancel')}</button>` : ''}`);
      statusBox.querySelector('#cancel')?.addEventListener('click', () => api.cancelLaunch());
    }),
    api.on('game-log', (l: { line: string; stream: string }) => {
      logLines.push(l);
      if (logLines.length > 5000) logLines.splice(0, 1000);
      if (!el.querySelector<HTMLElement>('#consoleCard')!.hidden) drawLog();
    }),
    api.on('game-exit', (r: { code: number; hint: string | null }) => {
      if (r.code === 0) toast('Minecraft wurde beendet.');
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
    avatar?.play('walk');
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
      avatar?.play('wave');
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
  const newsBox = el.querySelector('#news')!;
  try {
    const n = await lego<{ news: { id: number; title: string; body: string; created_at: number; kind: string }[] }>('GET', '/api/public/news');
    const art = ['brick', 'diamond', 'rocket', 'creeper', 'trophy', 'sword', 'star', 'chest'];
    mount(newsBox, n.news.length ? html`${n.news.slice(0, 6).map((x, i) => html`<article class="news-card">
        <div class="nc-title">${x.title}</div>
        <div class="nc-banner" data-k="${i % 4}">${pixel(art[(x.id ?? i) % art.length]!, 'lg')}<span class="nc-date">${fmtDate(x.created_at)}</span></div>
        <p class="small">${x.body.slice(0, 120)}${x.body.length > 120 ? '…' : ''}</p></article>`)}` : html`<p class="small muted">${t('play.noNews')}</p>`);
  } catch {
    mount(newsBox, html`<p class="small muted">${t('play.newsOffline')}</p>`);
  }
}

/** Set by the profile menu; the profiles page opens the editor directly. */
export let newProfileRequested = false;
export function takeNewProfileRequest(): boolean {
  const v = newProfileRequested;
  newProfileRequested = false;
  return v;
}

function profileMenu(anchor: HTMLElement): void {
  document.querySelector('.menu')?.remove();
  const s = state.settings;
  const m = document.createElement('div');
  m.className = 'menu up';
  m.setAttribute('role', 'menu');
  const sorted = [...s.profiles].sort((a, b) => (b.lastPlayed ?? 0) - (a.lastPlayed ?? 0));
  mount(m, html`<div class="menu-h">${t('play.profile')}</div>
    <div class="menu-scroll">${sorted.map((p) => html`<button class="menu-i ${p.id === s.selectedProfile ? 'on' : ''}" data-profile="${p.id}" role="menuitem">${pixel(p.icon, 'sm')}<span class="grow"><b>${p.name}</b><small>${profileLine(p)}</small></span>${p.id === s.selectedProfile ? icon('check', 'sm') : ''}</button>`)}</div>
    <div class="menu-sep"></div>
    <button class="menu-i" data-act="new" role="menuitem">${icon('plus', 'sm')}<span class="grow">${t('play.newProfile')}</span></button>
    <button class="menu-i" data-act="manage" role="menuitem">${icon('profiles', 'sm')}<span class="grow">${t('nav.profiles')}</span></button>
    <button class="menu-i" data-act="official" role="menuitem">${icon('external', 'sm')}<span class="grow">${t('play.official')}</span></button>`);
  document.body.append(m);
  const group = anchor.parentElement!.getBoundingClientRect();
  m.style.left = `${group.left}px`;
  m.style.width = `${group.width}px`;
  m.style.bottom = `${innerHeight - group.top + 8}px`;
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
    } else if (b.dataset.act === 'official') void officialFlow();
    else void go('profiles');
  });
}
