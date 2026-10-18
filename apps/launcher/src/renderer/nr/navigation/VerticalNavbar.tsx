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
  const accentColor = useThemeStore((state) => state.accentColor);
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
        icon={<Icon icon={item.icon} className="w-8 h-8" />}
        label={!item.isAction && showNavLabels ? item.label : undefined}
        isActive={active === item.id}
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
      <nav
        id="sidebar"
        className={cn('nr flex flex-col items-center py-6 w-24 backdrop-blur-lg', className)}
        style={{
          backgroundColor: `rgba(${parseInt(accentColor.value.slice(1, 3), 16)}, ${parseInt(accentColor.value.slice(3, 5), 16)}, ${parseInt(accentColor.value.slice(5, 7), 16)}, 0.4)`,
          borderRight: `2px solid ${accentColor.value}60`,
          borderLeft: `2px solid ${accentColor.value}60`,
          boxShadow: `0 0 10px ${accentColor.value}30 inset`,
        }}
        aria-label="Navigation"
      >
        <div className="mb-12">
          <Logo size="sm" onClick={onLogoClick} />
        </div>
        <div className="flex-1 flex flex-col items-center space-y-4 min-h-[300px]">{main.map(renderItem)}</div>
        {bottom.length > 0 && <div className="flex flex-col items-center space-y-4 mt-4">{bottom.map((it, i) => renderItem(it, main.length + i))}</div>}
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
