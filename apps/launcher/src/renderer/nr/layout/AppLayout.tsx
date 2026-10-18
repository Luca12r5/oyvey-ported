// Ported from the NoRisk Client launcher (GPL-3.0): components/layout/AppLayout.tsx.
// Changes: LEGO name, the launcher's own background canvases (bg.ts) instead of
// the NoRisk effect components, Electron window controls, no NoRisk services.
import { useEffect, useRef } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { VerticalNavbar } from '../navigation/VerticalNavbar.tsx';
import { UserProfileBar } from '../header/UserProfileBar.tsx';
import { NavigationHistory } from '../header/NavigationHistory.tsx';
import { HeaderInfoCarousel } from '../header/HeaderInfoCarousel.tsx';
import { LegoText } from '../ui/LegoText.tsx';
import { useThemeStore } from '../store/theme.ts';
import { actions, useShell } from '../store/shell.ts';

export function AppLayout({ onView }: { onView: (el: HTMLElement) => void }) {
  const accent = useThemeStore((s) => s.accentColor);
  const nav = useShell((s) => s.nav);
  const activeTab = useShell((s) => s.page);
  const viewRef = useRef<HTMLElement>(null);

  useEffect(() => { if (viewRef.current) onView(viewRef.current); }, [onView]);

  return (
    <div
      className="nr-launcher lx-shell h-screen w-full overflow-hidden relative flex"
      style={{
        // See-through so the animated background (bg.ts) shows; the theme adds soft glows on top.
        backgroundImage: `linear-gradient(180deg, ${accent.value}38 0%, ${accent.value}0d 32%, transparent 55%), linear-gradient(160deg, rgba(10,10,10,0.2), rgba(0,0,0,0.45))`,
      }}
    >
      <VerticalNavbar items={nav} activeItem={activeTab} onItemClick={(id) => actions.go(id)} onLogoClick={() => actions.credits()} className="h-full z-10" />
      <div className="flex-1 flex flex-col h-full overflow-hidden min-w-0">
        <HeaderBar />
        <div className="flex-1 relative overflow-hidden">
          <main ref={viewRef} id="view" className="relative z-10 h-full overflow-hidden custom-scrollbar" tabIndex={-1} />
        </div>
      </div>
    </div>
  );
}

function HeaderBar() {
  return (
    <header className="nr nr-titlebar nr-drag lx-glass lx-bar h-16 flex-shrink-0 flex items-center justify-between pl-5 pr-4 z-10">
      <div className="flex items-center gap-4 min-w-0">
        <NavigationHistory />
        <div className="flex items-center gap-3 min-w-0">
          <h1 className="lx-title m-0 whitespace-nowrap" aria-label="LEGO Launcher">
            <LegoText text="LEGO" colors={['red', 'yellow', 'blue', 'white']} height={30} />
            <span className="lx-title-rest">Launcher</span>
          </h1>
          <HeaderInfoCarousel />
        </div>
      </div>
      <div className="flex items-center gap-3">
        <UserProfileBar />
        <WindowControls />
      </div>
    </header>
  );
}

function WindowControls() {
  const fullscreen = useShell((s) => s.fullscreen);
  const btn = 'titlebar-button-borderless w-5 h-5 flex items-center justify-center text-white/60 hover:text-white transition-colors cursor-pointer';
  return (
    <div className="flex items-center gap-3 ml-4" id="win">
      <button className={btn} data-win="minimize" title="Minimieren" aria-label="Minimieren" onClick={() => actions.window('minimize')}>
        <Icon icon="pixel:minus-solid" className="w-4 h-4" />
      </button>
      <button className={btn} data-win="fullscreen" title="Vollbild (F11)" aria-label="Vollbild" onClick={() => actions.window('fullscreen')}>
        <Icon icon={fullscreen ? 'solar:quit-full-screen-bold' : 'solar:full-screen-bold'} className="w-4 h-4" />
      </button>
      <button className={btn.replace('hover:text-white', 'hover:text-red-500')} data-win="close" title="Schließen" aria-label="Schließen" onClick={() => actions.window('close')}>
        <Icon icon="pixel:window-close-solid" className="w-4 h-4" />
      </button>
    </div>
  );
}
