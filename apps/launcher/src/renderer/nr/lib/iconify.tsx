// Offline Iconify icon (NoRisk launcher approach): only icons from the generated
// bundle render; nothing is fetched from the network.
import { Icon as IconifyIcon, type IconProps } from '@iconify/react/offline';
import './icon-bundle.ts';

export function Icon(props: IconProps) {
  return (
    <IconifyIcon {...props}>
      <svg className={props.className} width="1em" height="1em" aria-hidden="true" />
    </IconifyIcon>
  );
}
