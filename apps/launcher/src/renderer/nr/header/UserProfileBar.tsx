// Ported from the NoRisk Client launcher (GPL-3.0): components/header/UserProfileBar.tsx.
// The account dropdown, friends sidebar and "more" menu are the launcher's own.
import { Icon } from '../lib/iconify.tsx';
import { cn, } from '../lib/utils.ts';
import { RunningInstancesIndicator } from './RunningInstancesIndicator.tsx';
import { CurrentAccountDisplay } from './CurrentAccountDisplay.tsx';
import { NotificationBell } from './NotificationBell.tsx';
import { IconButton } from '../ui/buttons/IconButton.tsx';
import { actions, useShell } from '../store/shell.ts';
import { pixel } from '../../icons.ts';

export function UserProfileBar({ className }: { className?: string }) {
  const coins = useShell((s) => s.coins);
  return (
    <div className={cn('relative flex items-center gap-3', className)}>
      <div className="profile-bar-container nr-drop-in flex items-center gap-2">
        <NotificationBell />
        <RunningInstancesIndicator />
        {coins !== null && (
          <div id="tbCoins" className="font-smallcaps flex items-center gap-1.5 px-2 h-10 text-white/90 text-sm" title="LEGO Coins">
            <span className="nr-px" dangerouslySetInnerHTML={{ __html: pixel('coin').__raw }} />
            <b className="font-normal">{coins.toLocaleString('de-DE')}</b>
          </div>
        )}
        <div id="acct">
          <CurrentAccountDisplay onClick={(e) => actions.account(e.currentTarget)} className="h-10" />
        </div>
        <IconButton
          id="tbFriends"
          icon={<Icon icon="solar:users-group-rounded-linear" className="w-5 h-5" />}
          onClick={() => actions.friends()}
          variant="flat"
          size="sm"
          aria-label="Freunde"
          title="Freunde"
          className="text-white/70 hover:text-white h-10 w-10"
        />
        <IconButton
          id="tbMore"
          icon={<Icon icon="solar:link-linear" className="w-5 h-5" />}
          onClick={(e) => actions.more(e.currentTarget)}
          variant="flat"
          size="sm"
          aria-label="Mehr"
          title="Shop, VR, Rewards, News …"
          className="text-white/70 hover:text-white h-10 w-10"
        />
      </div>
    </div>
  );
}
