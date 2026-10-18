// Game console (the launcher's own feature) in the NoRisk launcher's panel style.
import { useEffect, useMemo, useRef, useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { Button } from '../ui/buttons/Button.tsx';
import { IconButton } from '../ui/buttons/IconButton.tsx';
import { useThemeStore } from '../store/theme.ts';
import { gameStore, useGame } from '../store/game.ts';
import { isProblem, type LogEntry } from '../../logparse.ts';
import { api, errorToast, toast } from '../../ui.ts';
import { showCrashDoctor } from '../../crashdoc-ui.ts';

type Filter = 'all' | 'warn' | 'error';
const shown = (f: Filter) => (e: LogEntry) => f === 'all' || (f === 'error' ? isProblem(e) : e.level !== 'INFO' && e.level !== 'DEBUG');

export function GameConsole({ profileId }: { profileId: string }) {
  const accent = useThemeStore((s) => s.accentColor);
  const log = useGame((s) => s.log);
  const [filter, setFilter] = useState<Filter>('all');
  const box = useRef<HTMLDivElement>(null);
  const lines = useMemo(() => log.filter(shown(filter)).slice(-600), [log, filter]);
  const warn = useMemo(() => log.filter((e) => e.level === 'WARN').length, [log]);
  const errs = useMemo(() => log.filter(isProblem).length, [log]);

  useEffect(() => {
    const el = box.current;
    if (el && el.scrollHeight - el.scrollTop - el.clientHeight < 80) el.scrollTop = el.scrollHeight;
  }, [lines]);

  const seg = (id: Filter, label: string, n?: number) => (
    <button key={id} data-lvl={id} onClick={() => setFilter(id)} className={cn('px-3 h-8 font-smallcaps text-sm border transition-colors', filter === id ? 'text-white' : 'text-white/60 hover:text-white border-transparent')} style={filter === id ? { borderColor: `${accent.value}90`, backgroundColor: `${accent.value}30` } : undefined}>
      {label}{n !== undefined && <b className="ml-1 font-normal">{n}</b>}
    </button>
  );

  return (
    <div className="absolute left-6 right-6 bottom-6 z-40 flex flex-col backdrop-blur-lg border-2 border-b-4 nr-slide-up" id="consoleCard" style={{ height: '46%', backgroundColor: 'rgba(0,0,0,0.72)', borderColor: `${accent.value}80`, borderBottomColor: accent.dark }}>
      <div className="flex items-center gap-3 px-3 py-2 border-b-2" style={{ borderColor: `${accent.value}40` }}>
        <Icon icon="solar:monitor-bold" className="w-5 h-5" />
        <h3 className="font-smallcaps text-base m-0">Konsole</h3>
        <div className="flex items-center gap-1 ml-2" id="lvl">{seg('all', 'Alle')}{seg('warn', 'Warnungen', warn)}{seg('error', 'Fehler', errs)}</div>
        <div className="flex-1" />
        <Button size="xs" variant="flat" id="firstErr" disabled={errs === 0} onClick={() => { setFilter('all'); requestAnimationFrame(() => box.current?.querySelector('.lv-ERROR, .lv-FATAL')?.scrollIntoView({ block: 'center' })); }}>Zum ersten Fehler</Button>
        <Button size="xs" variant="flat" id="crashDoc" icon={<Icon icon="solar:shield-warning-bold" className="w-4 h-4" />} onClick={async () => { try { const r = await api.crashAnalyze(profileId); showCrashDoctor(r.profileId, r.diagnoses); } catch (e) { errorToast(e); } }}>Crash-Analyse</Button>
        <IconButton size="xs" variant="flat" id="openLogs" title="Log-Ordner" aria-label="Log-Ordner" icon={<Icon icon="solar:folder-bold" className="w-4 h-4" />} onClick={() => void api.openFolder('logs')} />
        <Button size="xs" variant="flat" id="copyLog" onClick={() => void navigator.clipboard.writeText(gameStore.get().log.map((l) => `[${l.time}] [${l.source}/${l.level}] ${l.text}`).join('\n')).then(() => toast('Log kopiert', 'ok'))}>Kopieren</Button>
        <IconButton size="xs" variant="flat" id="closeConsole" title="Schließen" aria-label="Schließen" icon={<Icon icon="solar:close-circle-bold" className="w-4 h-4" />} onClick={() => gameStore.set({ consoleOpen: false })} />
      </div>
      <div ref={box} className="nr-console flex-1 overflow-auto custom-scrollbar px-3 py-2 font-mono text-xs" id="log" role="log" aria-live="off">
        {lines.length === 0 ? <p className="text-white/50 font-smallcaps text-sm m-0">Noch keine Ausgabe. Starte Minecraft, dann erscheint hier das Log.</p>
          : lines.map((e, i) => (
            <div key={i} className={`ln lv-${e.level}`}><span className="ts">{e.time}</span><span className="lv">{e.level}</span><span className="src">{e.source}</span><span className="tx">{e.text}</span></div>
          ))}
      </div>
    </div>
  );
}
