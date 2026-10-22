# LEGO Launcher – licence notice

The LEGO Launcher (this directory, `apps/launcher`) is licensed under the
**GNU General Public License v3.0 only** (see `LICENSE`).

Its user interface (`src/renderer/nr/`) is ported from the
[NoRisk Client launcher](https://github.com/NoRiskClient/noriskclient-launcher)
by the NoRiskClient team, which is licensed under GPL-3.0. Ported files say so in
their header and list what was changed (Tauri calls replaced by the launcher's
Electron IPC, gsap replaced by CSS transitions, NoRisk services removed).

Not taken over from the NoRisk launcher: the NoRisk name and logo, its images,
and its `SmallCaps`/`MinecraftTen` fonts. The launcher uses its own LEGO brick
logo and the free Pixelify Sans font (SIL OFL 1.1) instead.

Icon artwork: see `THIRD_PARTY_ICONS.md`.

The LEGO Client (`lego-client`) includes **Visor** by pickzugng (MIT License,
`lego-client/src/main/resources/assets/visor/licenses/Visor-MIT.txt`): the VR
headset with its curved menu, hands, browser, VR videos and DrDonutt
(`lego-client/src/main/java/dev/visor`). Changed for the LEGO Client: the menu
shows the LEGO client's modules, the headset key is H, and the browser follows
the launcher's rules (https only, safe search, no downloads). Visor bundles the
Inter font (SIL OFL 1.1) and Phosphor icons (MIT), see the same folder.
