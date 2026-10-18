// Ported from the NoRisk Client launcher (GPL-3.0): components/header/NotificationBell.tsx.
import { Icon } from '../lib/iconify.tsx';
import { NotificationBadge } from '../ui/NotificationBadge.tsx';
import { actions, useShell } from '../store/shell.ts';

export function NotificationBell() {
  const unreadCount = useShell((s) => s.unread);
  return (
    <button
      id="tbBell"
      onClick={(e) => actions.bell(e.currentTarget)}
      className="relative p-2 text-white/70 hover:text-white transition-colors cursor-pointer"
      aria-label="Benachrichtigungen"
      title="Benachrichtigungen"
    >
      <Icon icon="solar:bell-bold" className="w-5 h-5" />
      <NotificationBadge count={unreadCount} />
    </button>
  );
}
