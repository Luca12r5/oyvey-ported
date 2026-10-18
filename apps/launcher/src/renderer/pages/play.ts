// Play screen: the NoRisk-launcher play tab (React, nr/tabs/PlayTab.tsx) plus
// the profile picker menu used by its LAUNCH arrow.

import { go, loadIdentity, onLeave, state } from '../app.ts';
import { api, html, mount } from '../ui.ts';
import { icon, pixel } from '../icons.ts';
import { t } from '../i18n.ts';
import type { GameProfile } from '../../core/settings.ts';
import { signInFlow } from './account.ts';
import { renderPlay } from '../nr/mount.tsx';

const LOADER_NAMES: Record<string, string> = { fabric: 'Fabric', quilt: 'Quilt', forge: 'Forge', neoforge: 'NeoForge' };

export function profileLine(p: GameProfile): string {
  // The LEGO flag is shown unless the profile name already says so.
  const parts = [p.legoClient && !/lego/i.test(p.name) ? 'LEGO Client' : null, p.loaderType ? LOADER_NAMES[p.loaderType] : t('play.vanilla'), p.gameVersion];
  return parts.filter(Boolean).join(' · ');
}

export function playPage(el: HTMLElement): void {
  const s = state.settings;
  const profile = s.profiles.find((p) => p.id === s.selectedProfile) ?? s.profiles[0];
  const a = state.auth;
  const unmount = renderPlay(el, {
    profile,
    profileLabel: profile ? `${profile.name} · ${profileLine(profile)}` : '',
    playerName: (a.signedIn ? a.name : s.guestName) || 'Gast',
    signedIn: a.signedIn,
    connected: a.lego.connected,
    skin: state.skin,
    reducedMotion: s.reducedMotion,
    onSignIn: () => void signInFlow(),
    onPickProfile: (group) => profileMenu(group.querySelector('#drop') as HTMLElement ?? group),
    onOpenNews: () => void go('news'),
    onRefresh: () => void loadIdentity(),
  });
  onLeave(unmount);
}

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
