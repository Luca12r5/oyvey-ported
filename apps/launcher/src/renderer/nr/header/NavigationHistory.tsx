// Ported from the NoRisk Client launcher (GPL-3.0): components/ui/NavigationHistory.tsx.
// The history itself lives in app.ts (also used by mouse back/forward buttons).
import { useEffect } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { actions, useShell } from '../store/shell.ts';

function NavIconButton({ icon, onClick, disabled = false, label, id }: { icon: React.ReactNode; onClick: () => void; disabled?: boolean; label: string; id: string }) {
  return (
    <button
      id={id}
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      title={label}
      className={cn(
        'flex items-center justify-center h-8 w-8 rounded duration-150',
        'focus:outline-none focus:ring-0 active:bg-transparent',
        disabled ? 'opacity-30 cursor-not-allowed' : 'text-white/100 hover:text-white hover:bg-white/5 active:bg-transparent',
      )}
    >
      {icon}
    </button>
  );
}

export function NavigationHistory({ className }: { className?: string }) {
  const canGoBack = useShell((s) => s.canBack);
  const canGoForward = useShell((s) => s.canFwd);

  // Keyboard shortcut support: Alt+Arrow and Cmd+Arrow (macOS)
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const t = e.target as HTMLElement;
      if (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable) return;
      if ((e.altKey || e.metaKey) && e.key === 'ArrowLeft') { e.preventDefault(); actions.back(); }
      else if ((e.altKey || e.metaKey) && e.key === 'ArrowRight') { e.preventDefault(); actions.fwd(); }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  return (
    <div className={cn('flex items-center gap-1', className)}>
      <NavIconButton id="navBack" icon={<Icon icon="solar:arrow-left-bold" className="w-4 h-4 text-current" />} onClick={() => actions.back()} disabled={!canGoBack} label="Zurück" />
      <NavIconButton id="navFwd" icon={<Icon icon="solar:arrow-right-bold" className="w-4 h-4 text-current" />} onClick={() => actions.fwd()} disabled={!canGoForward} label="Vor" />
    </div>
  );
}
