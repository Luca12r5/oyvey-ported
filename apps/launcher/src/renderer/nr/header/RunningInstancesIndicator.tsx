// Ported from the NoRisk Client launcher (GPL-3.0): components/process/RunningInstancesIndicator.tsx.
// The launcher runs one game at a time; the button opens the instance menu (console, stop).
import { Icon } from '../lib/iconify.tsx';
import { cn } from '../lib/utils.ts';
import { Button } from '../ui/buttons/Button.tsx';
import { actions, useShell } from '../store/shell.ts';

export function RunningInstancesIndicator({ className }: { className?: string }) {
  const instanceCount = useShell((s) => s.running);
  return (
    <div className={cn('relative', className)} id="tbInst">
      <Button
        id="instBtn"
        variant={instanceCount > 0 ? 'success' : 'flat'}
        size="sm"
        onClick={(e) => actions.instances(e.currentTarget)}
        icon={<Icon icon="solar:monitor-bold" className="w-4 h-4" />}
        className="h-10"
      >
        {instanceCount === 0 ? 'Keine Instanz' : `${instanceCount} Instanz`}
      </Button>
    </div>
  );
}
