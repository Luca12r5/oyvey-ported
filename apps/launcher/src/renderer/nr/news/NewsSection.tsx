// Ported from the NoRisk Client launcher (GPL-3.0): components/news/NewsSection.tsx + ui/NewsCard.tsx.
// News come from the LEGO server (built-in notes offline); the card art is a
// small render of the launcher's own 3D scenes.
import { useCallback, useEffect, useRef, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { useThemeStore } from '../store/theme.ts';
import { lego, fmtDate } from '../../ui.ts';
import { drawPreview } from '../../bg.ts';

interface NewsItem { id: number; title: string; body: string; created_at: number }

export const BUILTIN_NEWS: NewsItem[] = [
  { id: -1, title: 'Neuer Launcher-Look', body: 'Leiste, Kopfzeile und Play-Seite im neuen Design – mit Profilwahl direkt am LAUNCH-Knopf.', created_at: Date.UTC(2026, 9, 18) },
  { id: -2, title: 'Halloween Crypt', body: 'Höllen-Intro, Skins-Tab und Seitenübergänge zum Auswählen.', created_at: Date.UTC(2026, 9, 17) },
  { id: -3, title: 'VR-Modus', body: 'Visueller VR-Modus mit Tastatur und Maus oder echtes VR mit Headset – mit Browser in der Lobby.', created_at: Date.UTC(2026, 9, 16) },
  { id: -4, title: 'Konsole & Crash-Hilfe', body: 'Die Konsole zeigt Fehler farbig an, der Crash-Doktor schlägt Fixes vor.', created_at: Date.UTC(2026, 9, 15) },
];
const SCENES = ['galaxy', 'space', 'synthwave', 'aurora', 'legocity', 'sakura'];

/** News art: a still of one of the launcher's own 2D backgrounds. */
function newsArt(id: string): string {
  const c = document.createElement('canvas');
  c.style.width = '320px';
  c.style.height = '180px';
  c.width = 640; c.height = 360;
  document.body.append(c);
  try { drawPreview(c, id, 20_000); return c.toDataURL('image/jpeg', 0.85); } finally { c.remove(); }
}

function loadWidth(): number {
  try { const v = Number(localStorage.getItem('lego.newsWidth')); return v >= 120 && v <= 500 ? v : 300; } catch { return 300; }
}

function NewsCard({ title, imageUrl, date, onClick }: { title: string; imageUrl: string; date: string; onClick: () => void }) {
  return (
    <div className="lx-news relative w-full h-full overflow-hidden rounded-[14px] border border-white/10 hover:border-white/25 hover:-translate-y-0.5 transition-all duration-200 cursor-pointer" onClick={onClick} title={title}>
      {imageUrl ? <img src={imageUrl} alt="" className="w-full h-full object-cover" /> : <div className="w-full h-full bg-white/5" />}
      <span className="absolute inset-x-0 bottom-0 h-1/2 bg-gradient-to-t from-black/70 to-transparent pointer-events-none" /><span className="absolute left-2 bottom-2 px-2 py-0.5 rounded-full bg-black/50 backdrop-blur text-white/85 text-[10px]">{date}</span>
    </div>
  );
}

export function NewsSection({ className, onOpenAll }: { className?: string; onOpenAll: () => void }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const [width, setWidth] = useState(loadWidth);
  const [posts, setPosts] = useState<NewsItem[] | null>(null);
  const [art, setArt] = useState<Record<number, string>>({});
  const drag = useRef<{ x: number; w: number } | null>(null);
  const [isResizing, setIsResizing] = useState(false);
  const fadeStrength = 90;

  useEffect(() => {
    let off = false;
    void lego<{ news: NewsItem[] }>('GET', '/api/public/news')
      .then((r) => r.news.slice(0, 6))
      .catch(() => [] as NewsItem[])
      .then((n) => { if (!off) setPosts(n.length ? n : BUILTIN_NEWS); });
    return () => { off = true; };
  }, []);

  // Card art one after another so the UI stays responsive.
  useEffect(() => {
    if (!posts) return;
    let i = 0;
    let timer = 0;
    const next = () => {
      const p = posts[i];
      if (!p) return;
      try { const src = newsArt(SCENES[i % SCENES.length]!); setArt((a) => ({ ...a, [p.id]: src })); } catch { /* canvas blocked */ }
      i++;
      timer = window.setTimeout(next, 40);
    };
    timer = window.setTimeout(next, 120);
    return () => clearTimeout(timer);
  }, [posts, accentColor.value]);

  const onMove = useCallback((e: MouseEvent) => {
    if (!drag.current) return;
    setWidth(Math.max(120, Math.min(500, drag.current.w - (e.clientX - drag.current.x))));
  }, []);
  const onUp = useCallback(() => {
    drag.current = null;
    setIsResizing(false);
    setWidth((w) => { try { localStorage.setItem('lego.newsWidth', String(w)); } catch { /* storage blocked */ } return w; });
  }, []);
  useEffect(() => {
    if (!isResizing) return;
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    return () => { document.removeEventListener('mousemove', onMove); document.removeEventListener('mouseup', onUp); };
  }, [isResizing, onMove, onUp]);

  return (
    <div
      className={cn('h-full flex flex-col !p-3 z-0 relative', className)}
      style={{ width: `${width}px`, flex: '0 0 auto' }}
    >
      <div className={cn('absolute left-0 top-0 bottom-0 w-1 cursor-ew-resize z-10', isResizing && 'bg-white/20')} style={{ backgroundColor: isResizing ? `${accentColor.value}40` : 'transparent' }} onMouseDown={(e) => { drag.current = { x: e.clientX, w: width }; setIsResizing(true); e.preventDefault(); }} />
      <div className="pb-1">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Icon icon="pixel:newspaper-solid" className="w-5 h-5 text-white" />
            <h2 className="text-base font-semibold text-white m-0">News</h2>
          </div>
          <button id="allNews" className="text-white/60 hover:text-white transition-colors" title="Alle News" onClick={onOpenAll}>
            <Icon icon="solar:alt-arrow-right-bold" className="w-5 h-5" />
          </button>
        </div>
        <hr className="mt-2 border-t-2" style={{ borderColor: `${accentColor.value}40` }} />
      </div>
      <div className="flex-1 overflow-y-auto no-scrollbar relative" id="news">
        {posts === null ? (
          <div className="flex flex-col space-y-2 w-full">{[0, 1, 2].map((i) => <div key={i} className="w-full pt-[56.25%] bg-white/5 animate-skeleton-pulse" />)}</div>
        ) : (
          <div className="flex flex-col space-y-1 w-full">
            {posts.map((post) => (
              <div key={post.id} className="news-item w-full flex flex-col">
                <p className="text-sm font-medium text-white/80 truncate m-0 mt-2 mb-1" title={post.title}>{post.title}</p>
                <div className="relative w-full pt-[56.25%]">
                  <div className="absolute top-0 left-0 w-full h-full">
                    <NewsCard title={`${post.title} – ${post.body}`} imageUrl={art[post.id] ?? ''} date={fmtDate(post.created_at)} onClick={onOpenAll} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        <div
          className="sticky bottom-0 left-0 right-0 h-20 pointer-events-none z-10"
          style={{ background: `linear-gradient(to top, rgba(0,0,0,${fadeStrength * 0.01}) 0%, rgba(0,0,0,${fadeStrength * 0.0075}) 25%, rgba(0,0,0,${fadeStrength * 0.005}) 50%, rgba(0,0,0,${fadeStrength * 0.0025}) 75%, rgba(0,0,0,0) 100%)` }}
        />
      </div>
    </div>
  );
}
