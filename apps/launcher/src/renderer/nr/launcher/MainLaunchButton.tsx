// Ported from the NoRisk Client launcher (GPL-3.0): components/launcher/MainLaunchButton.tsx.
// The arrow opens the profile picker (which profile/version to play), like NoRisk.
import { useRef } from 'react';
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { Button } from '../ui/buttons/Button.tsx';
import { IconButton } from '../ui/buttons/IconButton.tsx';
import { launchProfile, useGame } from '../store/game.ts';
import { t } from '../../i18n.ts';

interface MainLaunchButtonProps {
  profileId: string;
  selectedVersionLabel?: string;
  signedIn: boolean;
  onSignIn: () => void;
  onPickProfile: (group: HTMLElement) => void;
  className?: string;
  maxWidth?: string;
  mainButtonWidth?: string;
  mainButtonHeight?: string;
  /** Size factor for big windows. */
  scale?: number;
}

export function MainLaunchButton({ profileId, selectedVersionLabel, signedIn, onSignIn, onPickProfile, className, maxWidth = '300px', mainButtonWidth, mainButtonHeight, scale = 1 }: MainLaunchButtonProps) {
  const h = Math.round(80 * scale);
  const groupRef = useRef<HTMLDivElement>(null);
  const isButtonLaunching = useGame((s) => s.launching);
  const running = useGame((s) => s.running);
  const buttonStatusMessage = useGame((s) => s.status);
  const error = useGame((s) => s.error);

  const handleLaunch = () => {
    if (!signedIn) return onSignIn();
    if (!profileId) return;
    void launchProfile(profileId);
  };

  const renderLaunchButtonContent = () => {
    const actionText = !signedIn ? t('acc.signin') : isButtonLaunching ? 'Stop' : running ? t('play.running') : t('play.play');
    let statusSubText: string | null = null;
    let statusColorClass = 'opacity-85';
    if (!signedIn) statusSubText = 'Microsoft-Konto mit Minecraft';
    else if (isButtonLaunching) {
      statusSubText = buttonStatusMessage || 'Launching...';
      statusColorClass = buttonStatusMessage ? 'opacity-90 text-white' : 'opacity-75';
    } else if (running) {
      statusSubText = 'Konsole öffnen';
      statusColorClass = 'text-green-400';
    } else if (error) {
      statusSubText = error;
      statusColorClass = 'text-red-400';
    }
    const displaySubText = statusSubText || selectedVersionLabel;
    return (
      <div className="w-full flex flex-col items-center justify-center leading-none -translate-y-[5px]">
        <span className="text-center" style={{ fontSize: `${Math.round(30 * scale)}px` }}>{actionText}</span>
        {displaySubText && (
          <span
            className={cn('text-xs font-minecraft tracking-normal text-center normal-case whitespace-nowrap overflow-hidden text-ellipsis', isButtonLaunching ? 'max-w-64' : 'max-w-[16rem]', statusColorClass)}
            title={displaySubText}
          >
            {displaySubText}
          </span>
        )}
      </div>
    );
  };

  const variant = isButtonLaunching ? 'destructive' : running ? 'success' : '3d';
  return (
    <div className={cn('relative flex flex-col justify-center', className)} style={{ maxWidth }}>
      <div className="flex flex-col gap-3">
        <div className="flex items-center relative nr-play-group" ref={groupRef}>
          <Button id={signedIn ? 'launch' : 'signin'} onClick={handleLaunch} disabled={signedIn && !profileId} size="xl" variant={variant} className={cn("flex-1 rounded-r-none", mainButtonWidth)} heightClassName={mainButtonHeight} style={{ height: `${h}px` }}>
            {renderLaunchButtonContent()}
          </Button>
          <IconButton
            id="drop"
            onClick={(e) => { e.stopPropagation(); if (!isButtonLaunching && groupRef.current) onPickProfile(groupRef.current); }}
            disabled={isButtonLaunching}
            size="xl"
            className={cn('rounded-l-none border-l-0', mainButtonHeight)}
            style={{ height: `${h}px`, width: `${Math.round(66 * scale)}px` }}
            icon={<Icon icon="solar:alt-arrow-down-bold" width="24" height="24" />}
            variant={variant === 'destructive' ? 'destructive' : variant === 'success' ? 'success' : '3d'}
            aria-label="Profil wählen"
            aria-haspopup="menu"
            title="Profil / Version wählen"
          />
        </div>
      </div>
    </div>
  );
}
