// The NoRisk launcher's PlayerRig (GPL-3.0) layout, rendered with the LEGO
// launcher's own skinview3d character (avatar.ts) instead of nrc-skin-renderer.
import { useEffect, useRef } from 'react';
import { createAvatar, type Avatar } from '../../avatar.ts';
import { gameStore } from '../store/game.ts';

export const PLAYER_RIG_WIDTH = 225;
export const PLAYER_RIG_HEIGHT = 450;

interface PlayerRigProps {
  playerName: string;
  skin: string | null;
  variant: 'classic' | 'slim';
  cape: string | null;
  animated: boolean;
  reducedMotion: boolean;
  /** Rig height in px (follows the stage size). */
  height?: number;
}

export function PlayerRig({ playerName, skin, variant, cape, animated, reducedMotion, height = PLAYER_RIG_HEIGHT }: PlayerRigProps) {
  const k = height / PLAYER_RIG_HEIGHT;
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const avatarRef = useRef<Avatar | null>(null);

  useEffect(() => {
    let a: Avatar | null = null;
    try {
      a = createAvatar(canvasRef.current!, reducedMotion);
      avatarRef.current = a;
      setTimeout(() => a?.react('greet'), 700);
    } catch (e) {
      console.warn('avatar unavailable', e);
    }
    let last = gameStore.get().react?.at ?? 0;
    const off = gameStore.subscribe(() => {
      const r = gameStore.get().react;
      if (r && r.at !== last) { last = r.at; a?.react(r.kind); }
    });
    return () => { off(); a?.dispose(); avatarRef.current = null; };
  }, [reducedMotion]);

  useEffect(() => { void avatarRef.current?.setSkin(skin, variant, cape).catch(() => {}); }, [skin, variant, cape, reducedMotion]);
  useEffect(() => { avatarRef.current?.setPaused(!animated); }, [animated, reducedMotion]);

  return (
    <div className="relative flex-shrink-0" style={{ width: `${Math.round(PLAYER_RIG_WIDTH * k)}px`, height: `${Math.round(height)}px` }}>
      <div className="nr-nametag absolute left-1/2 -translate-x-1/2 -top-2 z-20 px-3 py-1 rounded-full bg-black/45 backdrop-blur-md border border-white/10 text-white text-sm font-medium whitespace-nowrap pointer-events-none" style={{ fontSize: `${Math.round(14 * Math.min(1.6, k))}px` }}>
        {playerName}
      </div>
      <canvas
        ref={canvasRef}
        id="avatar"
        className="absolute"
        style={{ left: '50%', top: '50%', transform: 'translate(-50%, -48%)', width: `${Math.round(520 * k)}px`, height: `${Math.round(640 * k)}px`, maxWidth: 'none', filter: 'drop-shadow(5px 10px 5px rgba(0,0,0,0.75))' }}
      />
    </div>
  );
}
