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
  /** Name tag above the head. */
  showTag?: boolean;
}

export function PlayerRig({ playerName, skin, variant, cape, animated, reducedMotion, height = PLAYER_RIG_HEIGHT, showTag = true }: PlayerRigProps) {
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

  // Name tag follows the head (projected from the 3D model), so it sits just above it at any size or zoom.
  const tagRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!showTag) return;
    let raf = 0, last = 0;
    const tick = (t: number) => {
      raf = requestAnimationFrame(tick);
      if (t - last < 80) return;
      last = t;
      const top = avatarRef.current?.headTop();
      if (tagRef.current) tagRef.current.style.top = top == null ? '0px' : `calc(${(top * 100).toFixed(2)}% - ${Math.round(34 * Math.min(1.6, k))}px)`;
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [showTag, k]);

  return (
    <div className="relative flex-shrink-0" style={{ width: `${Math.round(height * 0.62)}px`, height: `${Math.round(height)}px` }}>
      {showTag && (
        <div ref={tagRef} className="nr-nametag absolute left-1/2 -translate-x-1/2 z-20 px-2 py-0.5 bg-black/40 text-white font-minecraft whitespace-nowrap pointer-events-none" style={{ top: 0, fontSize: `${Math.round(16 * Math.min(1.6, k))}px` }}>
          {playerName}
        </div>
      )}
      <canvas
        ref={canvasRef}
        id="avatar"
        className="absolute inset-0"
        style={{ width: '100%', height: '100%', maxWidth: 'none', filter: 'drop-shadow(5px 10px 5px rgba(0,0,0,0.75))' }}
      />
    </div>
  );
}
