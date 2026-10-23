// Icons for the React UI: the launcher's own line icons (icons.ts) wherever there
// is one, so the new UI keeps the LEGO look. Iconify names that have no own
// counterpart (flags) come from the offline bundle; nothing is fetched online.
import { Icon as IconifyIcon, type IconProps } from '@iconify/react/offline';
import { icon as ownIcon } from '../../icons.ts';
import './icon-bundle.ts';

/** Iconify name → name in icons.ts. */
const OWN: Record<string, string> = {
  'solar:play-bold': 'play', 'solar:stop-bold': 'close', 'solar:user-id-bold': 'profiles', 'solar:widget-bold': 'mods',
  'solar:widget-add-bold': 'plus', 'solar:emoji-funny-circle-bold': 'skins', 'solar:shop-bold': 'cape', 'solar:settings-bold': 'settings',
  'solar:bell-bold': 'bell', 'solar:monitor-bold': 'terminal', 'solar:users-group-rounded-linear': 'friends', 'solar:link-linear': 'grid',
  'solar:alt-arrow-down-bold': 'chevron', 'solar:alt-arrow-up-bold': 'chevron', 'solar:alt-arrow-right-bold': 'chevronRight',
  'solar:arrow-left-bold': 'back', 'solar:arrow-right-bold': 'forward', 'solar:arrow-down-bold': 'download', 'solar:arrow-up-bold': 'upload',
  'solar:magnifer-bold': 'search', 'solar:close-circle-bold': 'close', 'solar:close-circle-bold-duotone': 'close', 'solar:check-circle-bold': 'check',
  'solar:trash-bin-trash-bold': 'trash', 'solar:folder-bold': 'folder', 'solar:download-bold': 'download', 'solar:download-minimalistic-bold': 'download',
  'solar:upload-bold': 'upload', 'solar:import-bold': 'import', 'solar:refresh-bold': 'refresh', 'solar:box-bold': 'package', 'solar:gift-bold': 'star',
  'solar:star-bold': 'star', 'solar:heart-bold': 'heart', 'solar:heart-linear': 'heart', 'solar:palette-bold': 'palette', 'solar:pen-bold': 'brush',
  'solar:copy-bold': 'swap', 'solar:pin-bold': 'star', 'solar:pin-bold-duotone': 'star', 'solar:filter-bold': 'sliders', 'solar:list-bold': 'grid',
  'solar:text-bold': 'tag', 'solar:clock-circle-bold': 'refresh', 'solar:calendar-add-bold': 'plus', 'solar:gamepad-bold': 'gamepad',
  'solar:fire-bold': 'flame', 'solar:lock-keyhole-bold': 'lock', 'solar:shield-warning-bold': 'wrench', 'solar:info-circle-bold': 'info',
  'solar:add-circle-bold': 'plus', 'solar:full-screen-bold': 'fullscreen', 'solar:quit-full-screen-bold': 'exitFullscreen',
  'pixel:minus-solid': 'minimize', 'pixel:expand-solid': 'maximize', 'pixel:window-restore-solid': 'restore', 'pixel:window-close-solid': 'close',
  'solar:gallery-bold': 'image', 'solar:global-bold': 'globe', 'solar:sun-bold': 'sparkle', 'solar:layers-bold': 'block', 'solar:database-bold': 'cube', 'solar:eye-bold': 'external',
  'pixel:newspaper-solid': 'news', 'pixel:exclamation-triangle-solid': 'info',
};

export function Icon(props: IconProps) {
  const name = typeof props.icon === 'string' ? OWN[props.icon] : undefined;
  if (name) {
    const size = props.width ? { width: `${props.width}px`, height: `${props.height ?? props.width}px` } : undefined;
    return <span aria-hidden="true" className={`lx-ic ${props.className ?? ''}`} style={{ ...size, ...props.style }} dangerouslySetInnerHTML={{ __html: ownIcon(name).__raw }} />;
  }
  return (
    <IconifyIcon {...props}>
      <svg className={props.className} width="1em" height="1em" aria-hidden="true" />
    </IconifyIcon>
  );
}
