// "Studio" layout: the navigation as a floating glass dock at the bottom of the
// window (instead of the rail on the left). Same items and ids as the rail, so the
// rest of the launcher does not care which layout is on.
import { useState } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { Logo } from '../ui/Logo.tsx';
import { brickClick } from '../lib/sound.ts';
import type { NavItem } from '../store/shell.ts';
import { useThemeStore } from '../store/theme.ts';

interface DockNavProps {
  items: NavItem[];
  activeItem?: string;
  onItemClick?: (id: string) => void;
  onLogoClick?: () => void;
}

export function DockNav({ items, activeItem, onItemClick, onLogoClick }: DockNavProps) {
  const [tip, setTip] = useState<string | null>(null);
  const labels = useThemeStore((s) => s.showNavLabels);
  const main = items.filter((i) => !i.isAction);
  const actions = items.filter((i) => i.isAction);
  const button = (it: NavItem, i: number) => (
    <button
      key={it.id}
      data-page={it.id}
      aria-label={it.label}
      className={`lx-dock-btn nr-stagger ${activeItem === it.id && !it.isAction ? 'is-on' : ''}`}
      style={{ animationDelay: `${0.05 * i}s` }}
      onMouseEnter={() => setTip(it.id)}
      onMouseLeave={() => setTip(null)}
      onClick={() => { brickClick(true); onItemClick?.(it.id); }}
    >
      <Icon icon={it.icon} className="w-7 h-7" />
      {labels && <span className="lx-dock-label">{it.label}</span>}
      {tip === it.id && !labels && <span className="lx-dock-tip">{it.label}</span>}
    </button>
  );
  return (
    <nav id="sidebar" className="nr lx-dock" aria-label="Navigation">
      <div className="lx-dock-logo"><Logo size="sm" onClick={onLogoClick} /></div>
      <span className="lx-dock-sep" />
      {main.map(button)}
      {actions.length > 0 && <span className="lx-dock-sep" />}
      {actions.map((it, i) => button(it, main.length + i))}
    </nav>
  );
}
