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
import { rgbOf } from '../lib/utils.ts';

export function AppLayout({ onView }: { onView: (el: HTMLElement) => void }) {
  const themeAccentColor = useThemeStore((s) => s.accentColor);
  const nav = useShell((s) => s.nav);
  const activeTab = useShell((s) => s.page);
  const viewRef = useRef<HTMLElement>(null);

  useEffect(() => { if (viewRef.current) onView(viewRef.current); }, [onView]);

  const getComplementaryBackground = (alpha = 1) => {
    const rgb = rgbOf(themeAccentColor.value);
    const r = Math.min(Math.floor(rgb.r * 0.1), 30), g = Math.min(Math.floor(rgb.g * 0.1), 30), b = Math.min(Math.floor(rgb.b * 0.1), 30);
    return alpha === 1 ? `rgb(${r}, ${g}, ${b})` : `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };
  const backgroundColor = getComplementaryBackground();

  return (
    <div
      className="nr-launcher h-screen w-full backdrop-blur-lg border-2 border-solid overflow-hidden relative flex shadow-[0_0_25px_rgba(0,0,0,0.4)]"
      style={{
        borderColor: `${themeAccentColor.value}30`,
        boxShadow: `0 0 15px ${themeAccentColor.value}30, inset 0 0 10px ${themeAccentColor.value}20`,
      }}
      data-bg={backgroundColor}
    >
      {/* NoRisk's opaque launcher background; it turns see-through when an animated background is chosen (body.has-bg). */}
      <div
        className="nr-shell-bg absolute inset-0 pointer-events-none"
        style={{ backgroundColor, backgroundImage: `linear-gradient(to bottom right, ${backgroundColor}, rgba(0,0,0,0.9))` }}
      />
      <BorderGlowEffects accentColor={themeAccentColor.value} />
      <VerticalNavbar items={nav} activeItem={activeTab} onItemClick={(id) => actions.go(id)} onLogoClick={() => actions.credits()} className="h-full border-r-2 z-10" />
      <div className="flex-1 flex flex-col h-full overflow-hidden min-w-0">
        <HeaderBar />
        <div className="flex-1 relative overflow-hidden">
          <main ref={viewRef} id="view" className="relative z-10 h-full overflow-hidden custom-scrollbar" tabIndex={-1} />
        </div>
      </div>
    </div>
  );
}

function BorderGlowEffects({ accentColor }: { accentColor: string }) {
  return (
    <>
      <div className="absolute top-0 left-0 right-0 h-[2px] pointer-events-none" style={{ background: `linear-gradient(to right, transparent, ${accentColor}70, transparent)` }} />
      <div className="absolute bottom-0 left-0 right-0 h-[2px] pointer-events-none" style={{ background: `linear-gradient(to right, transparent, ${accentColor}70, transparent)` }} />
      <div className="absolute top-0 bottom-0 left-0 w-[2px] pointer-events-none" style={{ background: `linear-gradient(to bottom, transparent, ${accentColor}70, transparent)` }} />
      <div className="absolute top-0 bottom-0 right-0 w-[2px] pointer-events-none" style={{ background: `linear-gradient(to bottom, transparent, ${accentColor}70, transparent)` }} />
    </>
  );
}

function HeaderBar() {
  const accentColor = useThemeStore((s) => s.accentColor);
  const c = rgbOf(accentColor.value);
  return (
    <header
      className="nr nr-titlebar nr-drag h-20 flex-shrink-0 border-b-2 backdrop-blur-lg flex items-center justify-between px-8 z-10"
      style={{ borderColor: `${accentColor.value}40`, backgroundColor: `rgba(${c.r}, ${c.g}, ${c.b}, 0.01)` }}
    >
      <div className="flex items-center gap-4 min-w-0">
        <NavigationHistory />
        <div className="flex flex-col items-start min-w-0">
          <h1 className="font-smallcaps text-2xl tracking-wider font-bold text-shadow m-0 whitespace-nowrap flex items-center gap-2" aria-label="LEGO Launcher">
            <LegoText text="LEGO" colors={['red', 'yellow', 'blue', 'white']} height={26} />
            <span>Launcher</span>
          </h1>
          <HeaderInfoCarousel />
        </div>
      </div>
      <div className="flex items-center gap-4">
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
