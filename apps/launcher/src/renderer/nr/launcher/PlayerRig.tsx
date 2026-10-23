// The NoRisk launcher's PlayerRig (GPL-3.0) layout, rendered with the LEGO
// launcher's own skinview3d character (avatar.ts) instead of nrc-skin-renderer.
import { useEffect, useRef } from 'react';
import { createAvatar, type Avatar } from '../../avatar.ts';
import { gameStore } from '../store/game.ts';
import { emoteStore } from '../store/emote.ts';
import { emoteById } from '../../emotes.ts';

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
  /** Equipped cosmetics (item ids) worn in 3D. */
  cosmetics?: string[];
  /** Fills the whole parent (the stage) instead of a fixed box; the figure is framed by `zoom`. */
  fill?: boolean;
  /** Figure size factor when filling (1 = default). */
  zoom?: number;
}

export function PlayerRig({ playerName, skin, variant, cape, animated, reducedMotion, height = PLAYER_RIG_HEIGHT, showTag = true, cosmetics = [], fill = false, zoom = 1 }: PlayerRigProps) {
  const k = height / PLAYER_RIG_HEIGHT;
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const avatarRef = useRef<Avatar | null>(null);

  useEffect(() => {
    let a: Avatar | null = null;
    try {
      a = createAvatar(canvasRef.current!, reducedMotion, fill ? { zoom: zoom * 0.8, lift: 0.08 } : undefined);
      avatarRef.current = a;
      a.onEmote((id) => emoteStore.set({ playing: id }));
      setTimeout(() => a?.react('greet'), 700);
    } catch (e) {
      console.warn('avatar unavailable', e);
    }
    let last = gameStore.get().react?.at ?? 0;
    const off = gameStore.subscribe(() => {
      const r = gameStore.get().react;
      if (r && r.at !== last) { last = r.at; a?.react(r.kind); }
    });
    let lastEmote = emoteStore.get().request?.at ?? 0;
    const offEmote = emoteStore.subscribe(() => {
      const r = emoteStore.get().request;
      if (r && r.at !== lastEmote) { lastEmote = r.at; a?.emote(r.id); }
    });
    return () => { off(); offEmote(); a?.dispose(); avatarRef.current = null; emoteStore.set({ playing: null }); };
  }, [reducedMotion, fill, zoom]);

  useEffect(() => { void avatarRef.current?.setSkin(skin, variant, cape).catch(() => {}); }, [skin, variant, cape, reducedMotion]);
  useEffect(() => { avatarRef.current?.setPaused(!animated); }, [animated, reducedMotion]);
  const cosKey = cosmetics.join(',');
  useEffect(() => { avatarRef.current?.wear(cosKey ? cosKey.split(',') : []); }, [cosKey, reducedMotion]);

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
      if (tagRef.current) {
        tagRef.current.style.top = top == null ? '0px' : `max(8px, calc(${(top * 100).toFixed(2)}% - ${Math.round((fill ? 34 : 52) * Math.min(1.4, k))}px))`;
        // Hidden while a transformation emote turns the figure into an object.
        const em = emoteStore.get().playing;
        tagRef.current.style.opacity = em && emoteById(em)?.cat === 'transform' ? '0' : '1';
      }
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [showTag, k]);

  return (
    <div className={fill ? 'absolute inset-0' : 'relative flex-shrink-0'} style={fill ? undefined : { width: `${Math.round(height * 0.62)}px`, height: `${Math.round(height)}px` }}>
      {showTag && (
        <div ref={tagRef} className="nr-nametag transition-opacity duration-300 absolute left-1/2 -translate-x-1/2 z-20 px-2 py-0.5 bg-black/40 text-white font-minecraft whitespace-nowrap pointer-events-none" style={{ top: 0, fontSize: `${Math.max(18, Math.min(30, Math.round(20 * k)))}px` }}>
          {playerName}
        </div>
      )}
      <canvas
        ref={canvasRef}
        id="avatar"
        className="absolute inset-0"
        style={{ width: '100%', height: '100%', maxWidth: 'none', filter: fill ? 'drop-shadow(0 18px 18px rgba(0,0,0,0.55))' : 'drop-shadow(5px 10px 5px rgba(0,0,0,0.75))' }}
      />
    </div>
  );
}
