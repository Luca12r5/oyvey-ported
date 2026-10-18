// Rail tooltip in the LEGO launcher's glass style.
import type React from 'react';
import { forwardRef } from 'react';
import { cn } from '../../lib/utils.ts';

export const NavTooltip = forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement> & { variant?: string }>(({ className, children, variant: _v, ...props }, ref) => (
  <div ref={ref} className={cn('lx-glass px-3 py-1.5 rounded-xl text-white text-sm font-medium whitespace-nowrap shadow-xl', className)} {...props}>{children}</div>
));
NavTooltip.displayName = 'NavTooltip';
