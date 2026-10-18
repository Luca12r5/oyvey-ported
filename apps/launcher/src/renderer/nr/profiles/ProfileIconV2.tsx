// Ported from the NoRisk Client launcher (GPL-3.0): components/profiles/ProfileIconV2.tsx.
// Shows the profile's pixel icon from the LEGO launcher's icon set.
import { useThemeStore } from '../store/theme.ts';
import { pixel } from '../../icons.ts';
import type { GameProfile } from '../../../core/settings.ts';

export function ProfileIconV2({ profile, size = 'md', className = '' }: { profile: GameProfile; size?: 'sm' | 'md' | 'lg'; className?: string }) {
  const accentColor = useThemeStore((s) => s.accentColor);
  const sizeClasses = { sm: 'w-12 h-12', md: 'w-16 h-16', lg: 'w-20 h-20' };
  const px = { sm: 'md', md: 'lg', lg: 'lg' } as const;
  return (
    <div
      className={`${sizeClasses[size]} rounded-lg border-2 flex items-center justify-center overflow-hidden nr-px-icon ${className}`}
      style={{ backgroundColor: `${accentColor.value}20`, borderColor: `${accentColor.value}60` }}
      dangerouslySetInnerHTML={{ __html: pixel(profile.icon, px[size]).__raw }}
    />
  );
}
