// Ported from the NoRisk Client launcher (GPL-3.0): components/header/HeaderInfoCarousel.tsx.
// Alternates between the launcher version and the number of players online on the LEGO server.
import { useEffect, useRef, useState } from 'react';
import { useShell } from '../store/shell.ts';

const SLIDE_INTERVAL_MS = 8000;
const TEXT_CLASSES = 'text-white/70 font-minecraft text-[10px] font-normal leading-none';

export function HeaderInfoCarousel() {
  const version = useShell((s) => s.version);
  const online = useShell((s) => s.online);
  const [showPlayers, setShowPlayers] = useState(true);
  const hovering = useRef(false);

  useEffect(() => {
    setShowPlayers(true);
    if (online == null) return;
    const id = setInterval(() => { if (!hovering.current) setShowPlayers((p) => !p); }, SLIDE_INTERVAL_MS);
    return () => clearInterval(id);
  }, [online]);

  const playerSlideActive = showPlayers && online != null;
  return (
    <div className="relative inline-flex items-center h-3 min-w-[3.5rem]" onMouseEnter={() => { hovering.current = true; }} onMouseLeave={() => { hovering.current = false; }}>
      <span id="tbVersion" className={`${TEXT_CLASSES} absolute inset-0 flex items-center whitespace-nowrap transition-opacity duration-300 ease-out ${playerSlideActive ? 'opacity-0' : 'opacity-100'}`} aria-hidden={playerSlideActive}>
        v{version || '?.?.?'}
      </span>
      {online != null && (
        <span id="tbOnline" title="Spieler gerade online" className={`${TEXT_CLASSES} absolute inset-0 flex items-center whitespace-nowrap transition-opacity duration-300 ease-out ${playerSlideActive ? 'opacity-100' : 'opacity-0 pointer-events-none'}`} aria-hidden={!playerSlideActive}>
          <span className="inline-block w-1.5 h-1.5 rounded-full bg-emerald-400 mr-1 align-middle" style={{ boxShadow: '0 0 4px rgba(74, 222, 128, 0.7)' }} />
          {online.toLocaleString('de-DE')} online
        </span>
      )}
    </div>
  );
}
