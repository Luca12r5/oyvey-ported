// Emote bar on the play screen: favourites as quick buttons on the left, and a
// panel with every emote (search, categories, favourites). Esc stops an emote.
import { useEffect, useMemo, useState } from 'react';
import { EMOTES, EMOTE_CATS, type EmoteCat } from '../../emotes.ts';
import { icon } from '../../icons.ts';
import { emoteStore, playEmote, toggleFavorite, useEmote } from '../store/emote.ts';
import { brickClick } from '../lib/sound.ts';

const ICON: Record<string, string> = {
  car: 'car', rocket: 'rocket', plane: 'rocket', heli: 'rotate', boat: 'wave', train: 'car', tank: 'shield', scooter: 'car', skateboard: 'car',
  tower: 'brick', bridge: 'brick', snake: 'wave', ball: 'globe', spider: 'ghost', crab: 'ghost', windmill: 'rotate', cube: 'cube', arrow: 'forward',
  chair: 'chair', table: 'chair', sofa: 'home', bed: 'home', throne: 'trophy', lamp: 'sparkle', robot: 'dev', star: 'star',
  wave: 'wave', cheer: 'trophy', clap: 'users', sleep: 'snow', explode: 'bomb', headless: 'ghost', zombie: 'ghost', superhero: 'flame',
  guitar: 'music', drums: 'music', dj: 'music', disco: 'sparkle', meditate: 'sparkle', ninja: 'sword', karate: 'sword',
};
const CAT_ICON: Record<EmoteCat, string> = { transform: 'brick', dance: 'music', gesture: 'wave' };
const ic = (id: string, cat: EmoteCat) => icon(ICON[id] ?? CAT_ICON[cat]).__raw;
const byId = new Map(EMOTES.map((e) => [e.id, e]));

type Tab = 'all' | EmoteCat | 'fav';

export function EmoteBar() {
  const favorites = useEmote((s) => s.favorites);
  const playing = useEmote((s) => s.playing);
  const [open, setOpen] = useState(false);
  const [tab, setTab] = useState<Tab>('all');
  const [q, setQ] = useState('');

  useEffect(() => {
    const key = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      if (open) setOpen(false);
      else if (emoteStore.get().playing) playEmote(null);
    };
    addEventListener('keydown', key);
    return () => removeEventListener('keydown', key);
  }, [open]);

  const list = useMemo(() => {
    const s = q.trim().toLowerCase();
    return EMOTES.filter((e) => (tab === 'all' || (tab === 'fav' ? favorites.includes(e.id) : e.cat === tab)) && (!s || e.name.toLowerCase().includes(s) || e.id.includes(s)));
  }, [tab, q, favorites]);

  const go = (id: string) => { brickClick(false); playEmote(playing === id ? null : id); };

  return (
    <>
      <div id="emoteQuick" className="absolute left-5 bottom-6 z-30">
        <button id="emoteOpen" className={`lx-emote-open ${open ? 'is-on' : ''}`} aria-expanded={open} onClick={() => setOpen((o) => !o)}>
          <span className="lx-emote-ic" dangerouslySetInnerHTML={{ __html: icon('wave').__raw }} />
          <span>Emotes</span>
          {playing && <span className="lx-emote-now">{byId.get(playing)?.name}</span>}
        </button>
      </div>

      {open && (
        <div id="emotePanel" className="lx-emote-panel absolute left-5 top-[112px] bottom-[84px] z-40 flex flex-col" onMouseDown={(e) => e.stopPropagation()}>
          <div className="flex items-center gap-2 mb-3">
            <h3 className="text-white text-xl m-0 flex-1">Emotes</h3>
            <button className="lx-emote-x" aria-label="Schließen" onClick={() => setOpen(false)} dangerouslySetInnerHTML={{ __html: icon('close').__raw }} />
          </div>
          <input id="emoteSearch" className="lx-emote-search" placeholder="Emote suchen …" value={q} onChange={(e) => setQ(e.target.value)} />
          <div className="flex gap-1.5 my-3 flex-wrap">
            {([['all', 'Alle'], ...EMOTE_CATS.map((c) => [c.id, c.name]), ['fav', 'Favoriten']] as [Tab, string][]).map(([id, name]) => (
              <button key={id} data-tab={id} className={`lx-emote-tab ${tab === id ? 'is-on' : ''}`} onClick={() => setTab(id)}>{name}</button>
            ))}
          </div>
          <div className="lx-emote-grid flex-1 overflow-y-auto pr-1">
            {list.map((e) => (
              <div key={e.id} className={`lx-emote-tile ${playing === e.id ? 'is-on' : ''}`} data-emote={e.id} role="button" tabIndex={0}
                onClick={() => go(e.id)} onKeyDown={(k) => { if (k.key === 'Enter') go(e.id); }}>
                <span className="lx-emote-ic" dangerouslySetInnerHTML={{ __html: ic(e.id, e.cat) }} />
                <span className="lx-emote-name">{e.name}</span>
                <button className={`lx-emote-fav ${favorites.includes(e.id) ? 'is-on' : ''}`} title="Favorit" onClick={(ev) => { ev.stopPropagation(); toggleFavorite(e.id); }} dangerouslySetInnerHTML={{ __html: icon('star').__raw }} />
              </div>
            ))}
            {!list.length && <p className="text-white/70 text-sm">Kein Emote gefunden.</p>}
          </div>
          {playing && <p className="text-white/80 text-sm mt-2 mb-0">Esc stoppt das Emote.</p>}
        </div>
      )}
    </>
  );
}
