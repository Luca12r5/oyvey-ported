// NoRisk launcher Logo component with the LEGO brick mark instead of the NoRisk image.
import { cn } from '../lib/utils.ts';
import { brickLogo } from '../../icons.ts';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  onClick?: () => void;
}

export function Logo({ size = 'md', className, onClick }: LogoProps) {
  const sizeClasses = { sm: 'w-10 h-10', md: 'w-16 h-16', lg: 'w-24 h-24' };
  return (
    <div
      className={cn('relative nr-logo', sizeClasses[size], className, onClick && 'cursor-pointer hover:scale-105 transition-transform duration-200')}
      onClick={onClick}
      title="LEGO Launcher"
      dangerouslySetInnerHTML={{ __html: brickLogo().__raw }}
    />
  );
}
