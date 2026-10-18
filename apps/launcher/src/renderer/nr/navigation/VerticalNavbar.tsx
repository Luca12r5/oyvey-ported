// Ported from the NoRisk Client launcher (GPL-3.0): components/navigation/VerticalNavbar.tsx.
// Changes: LEGO logo, credits via the launcher, CSS entrance animation instead of gsap.
import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { Logo } from '../ui/Logo.tsx';
import { NavButton } from '../ui/nav/NavButton.tsx';
import { NavTooltip } from '../ui/nav/NavTooltip.tsx';
import { useThemeStore } from '../store/theme.ts';
import type { NavItem } from '../store/shell.ts';

/** Every rail tab is a brick in its own classic LEGO colour. */
const RAIL_COLORS: Record<string, string> = { play: '#d01012', profiles: '#0055bf', mods: '#f2cd37', skins: '#237841', capes: '#fe8a18', settings: '#6c6e68' };

interface VerticalNavbarProps {
  className?: string;
  items: NavItem[];
  activeItem?: string;
  onItemClick?: (id: string) => void;
  onLogoClick?: () => void;
}

export function VerticalNavbar({ className, items, activeItem, onItemClick, onLogoClick }: VerticalNavbarProps) {
  const [active, setActive] = useState(activeItem || items[0]?.id);
  const [showTooltip, setShowTooltip] = useState<string | null>(null);
  const buttonRefs = useRef<Record<string, HTMLDivElement | null>>({});
  const showNavLabels = useThemeStore((state) => state.showNavLabels);
  const [tooltipPosition, setTooltipPosition] = useState({ top: 0, left: 0 });

  useEffect(() => {
    if (activeItem) setActive(activeItem);
  }, [activeItem]);

  const handleItemClick = (id: string, isAction?: boolean) => {
    if (!isAction) setActive(id);
    onItemClick?.(id);
  };

  const handleMouseEnter = (id: string) => {
    const el = buttonRefs.current[id];
    if (el) {
      const rect = el.getBoundingClientRect();
      setTooltipPosition({ top: rect.top + rect.height / 2, left: rect.right + 12 });
    }
    setShowTooltip(id);
  };

  const renderItem = (item: NavItem, i: number) => (
    <div
      key={item.id}
      className="relative group nr-nav-item nr-stagger flex flex-col items-center"
      style={{ animationDelay: `${i * 0.05}s` }}
      ref={(el) => { buttonRefs.current[item.id] = el; }}
    >
      <NavButton
        icon={<Icon icon={item.icon} className="w-6 h-6" />}
        label={!item.isAction && showNavLabels ? item.label : undefined}
        isActive={active === item.id}
        brickColor={RAIL_COLORS[item.id]}
        onClick={() => handleItemClick(item.id, item.isAction)}
        onMouseEnter={() => handleMouseEnter(item.id)}
        onMouseLeave={() => setShowTooltip(null)}
        aria-label={item.label}
        data-page={item.id}
      />
    </div>
  );

  const main = items.filter((item) => !item.isAction);
  const bottom = items.filter((item) => item.isAction);
  return (
    <>
      <nav id="sidebar" className={cn('nr lx-glass lx-rail flex flex-col items-center py-4 w-[88px] gap-1', className)} aria-label="Navigation">
        <div className="mb-6 mt-1">
          <Logo size="sm" onClick={onLogoClick} />
        </div>
        <div className="flex-1 flex flex-col items-center gap-2 min-h-[300px]">{main.map(renderItem)}</div>
        {bottom.length > 0 && <div className="flex flex-col items-center gap-2 mt-4">{bottom.map((it, i) => renderItem(it, main.length + i))}</div>}
      </nav>
      {showTooltip &&
        createPortal(
          <div className="nr fixed pointer-events-none" style={{ top: `${tooltipPosition.top}px`, left: `${tooltipPosition.left}px`, zIndex: 9999, transform: 'translateY(-50%)' }}>
            <NavTooltip className="nr-tip-in">{items.find((item) => item.id === showTooltip)?.label}</NavTooltip>
          </div>,
          document.body,
        )}
    </>
  );
}
