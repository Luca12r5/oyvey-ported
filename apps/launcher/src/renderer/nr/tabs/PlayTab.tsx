// Ported from the NoRisk Client launcher (GPL-3.0): components/tabs/PlayTab.tsx and
// launcher/PlayerActionsDisplay.tsx, wired to the LEGO launcher's profiles,
// accounts, launch flow and console.
import { useEffect, useRef, useState } from 'react';
import { RetroGridEffect } from '../effects/RetroGridEffect.tsx';
import { ToggleSwitch } from '../ui/ToggleSwitch.tsx';
import { PlayerRig } from '../launcher/PlayerRig.tsx';
import { MainLaunchButton } from '../launcher/MainLaunchButton.tsx';
import { NewsSection } from '../news/NewsSection.tsx';
import { GameConsole } from '../launcher/GameConsole.tsx';
import { CoinsBanner } from '../launcher/CoinsBanner.tsx';
import { useThemeStore } from '../store/theme.ts';
import { useGame } from '../store/game.ts';
import { useShell } from '../store/shell.ts';
import type { GameProfile } from '../../../core/settings.ts';

export interface PlayTabProps {
  profile: GameProfile | undefined;
  profileLabel: string;
  playerName: string;
  signedIn: boolean;
  connected: boolean;
  skin: { skin: string | null; variant: 'classic' | 'slim'; cape: string | null };
  reducedMotion: boolean;
  onSignIn: () => void;
  onPickProfile: (group: HTMLElement) => void;
  onOpenNews: () => void;
  onRefresh: () => void;
}

function loadAnim(): boolean {
  try { return localStorage.getItem('lego.skinAnim') !== '0'; } catch { return true; }
}

export function PlayTab(p: PlayTabProps) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const consoleOpen = useGame((s) => s.consoleOpen);
  const coinsKey = useShell((s) => s.coins);
  const rigScale = useThemeStore((s) => s.rigScale);
  const nametag = useThemeStore((s) => s.nametag);
  const [anim, setAnim] = useState(loadAnim);
  // Character and LAUNCH grow with the window (fullscreen on big screens).
  const stageRef = useRef<HTMLDivElement>(null);
  const [rigH, setRigH] = useState(450);
  useEffect(() => {
    const el = stageRef.current;
    if (!el) return;
    const fit = () => setRigH(Math.round(Math.max(360, Math.min(1200, el.clientHeight * 0.8 * rigScale))));
    const ro = new ResizeObserver(fit);
    ro.observe(el);
    fit();
    return () => ro.disconnect();
  }, [rigScale]);
  const k = rigH / 450;

  return (
    <div className="flex h-full relative">
      <div ref={stageRef} className="flex-grow flex flex-col items-center justify-center p-8 relative min-w-0">
        <RetroGridEffect renderMode="both" isAnimationEnabled={!p.reducedMotion} customGridLineColor={`${accentColor.value}80`} />

        <div className="absolute top-3 left-3 z-20">
          <CoinsBanner key={String(coinsKey)} connected={p.connected} onChanged={p.onRefresh} />
        </div>

        <div className="absolute top-6 right-6 z-20 flex flex-col items-end gap-3">
          <div className="flex items-center gap-2">
            <span className="text-sm text-white/70 font-minecraft">Skin Animation</span>
            <ToggleSwitch checked={anim && !p.reducedMotion} disabled={p.reducedMotion} size="sm" onChange={(v) => { setAnim(v); try { localStorage.setItem('lego.skinAnim', v ? '1' : '0'); } catch { /* storage blocked */ } }} />
          </div>
        </div>

        <div className="relative z-10">
          <div className="flex flex-col items-center">
            <div className="relative w-full flex flex-col items-center" style={{ maxWidth: `${Math.round(500 * k)}px` }}>
              <PlayerRig playerName={p.playerName} skin={p.skin.skin} variant={p.skin.variant} cape={p.skin.cape} animated={anim && !p.reducedMotion} reducedMotion={p.reducedMotion} height={rigH} showTag={nametag} />
              <div className="absolute left-0 right-0 flex justify-center px-4 bottom-2 z-30">
                <div style={{ width: `${Math.round(Math.min(520, 330 * Math.min(1.5, k)))}px` }}>
                  <MainLaunchButton
                    profileId={p.profile?.id ?? ''}
                    selectedVersionLabel={p.profileLabel}
                    signedIn={p.signedIn}
                    onSignIn={p.onSignIn}
                    onPickProfile={p.onPickProfile}
                    maxWidth="none"
                    scale={Math.min(1.4, k)}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        {consoleOpen && p.profile && <GameConsole profileId={p.profile.id} />}
      </div>

      <NewsSection onOpenAll={p.onOpenNews} className="border-l-2 border-white/40 bg-black/10 backdrop-blur-lg p-5 overflow-hidden flex flex-col relative z-10" />
    </div>
  );
}
