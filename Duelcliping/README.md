# Duelcliping

**Gaming clip recorder for Windows** – background replay buffer, full-session recording and a
built-in video cutter, designed to cost your game as little performance as possible.

* Save the last **15 s, 30 s, 1 min, 2 min, 5 min** or a custom length with one key (default **F8** / **F9**).
* **Record a whole session** like a normal video (**F10**) – uses the same encoder, no extra load.
* **Cut your videos right in the app**: split at the playhead, remove parts, trim start/end, undo –
  save lossless (instant) or frame-exact, keep all audio tracks.
* Up to **4K** and **144 FPS**, **H.264 / HEVC / AV1**, **MP4 / MKV**, Low → Ultra or custom bitrate.
* **Hardware encoding** with NVIDIA NVENC, AMD AMF or Intel Quick Sync – detected and tested automatically.
* Game audio and microphone on **separate tracks** with independent volume and device selection.
* Clip library with thumbnails, hover previews, search, sort, rename, delete, pin, export.
* Optional click-through recording overlay, tray icon, start with Windows, dark and light theme.
* Live performance monitor and an on/off **benchmark** (with PresentMon: game FPS and 1 % lows).

## Install

1. Download `Duelcliping-Setup-x.y.z.exe` (GitHub → Actions → latest *Duelcliping* run → artifact
   **Duelcliping-Setup**, or build it yourself, see below).
2. Run it. No admin rights or .NET installation are needed; ffmpeg is included.
3. Duelcliping starts in the tray and the replay buffer is on. Press **F8** in a game to save the
   last 30 seconds. Clips go to `Videos\Duelcliping`.

Requirements: Windows 10 1809 or newer (Windows 11 recommended), 64-bit. A GPU with a hardware
encoder (NVIDIA GTX 600+, AMD GCN+, Intel 6th gen+) is strongly recommended.

## Default hotkeys

| Action | Key |
|---|---|
| Save last 30 seconds | F8 |
| Save last 60 seconds | F9 |
| Start / stop recording | F10 |
| Open Duelcliping | F11 |

All are configurable in *Settings → Hotkeys*. Letters, digits, Space, Shift, Ctrl etc. without a
modifier are rejected because games use them.

## Cutter shortcuts

`Space` play/pause · `S` split · `Del` remove part · `K` keep part · `I` / `O` trim start / end ·
`Ctrl+Z` / `Ctrl+Y` undo / redo · `←` `→` 5 s · `Shift+←/→` one frame. Drop any video file onto the
cutter to edit it.

## Build from source

Requirements: Windows, .NET 8 SDK, PowerShell 7, [Inno Setup 6](https://jrsoftware.org/isinfo.php) (for the installer).

```powershell
pwsh scripts/build.ps1            # tests, publish, ffmpeg download, installer → artifacts/
pwsh scripts/build.ps1 -SkipInstaller
```

`scripts/fetch-ffmpeg.ps1` downloads a GPL ffmpeg build (BtbN) with NVENC/AMF/QSV into
`third_party/ffmpeg`. Core tests also run on Linux with a system ffmpeg:

```bash
dotnet test tests/Duelcliping.Core.Tests
```

## Notes and limits

* The overlay and notifications are excluded from recordings and never take focus. Windows cannot
  show any overlay above *exclusive* fullscreen games – use borderless mode if you want to see it.
* Duelcliping records the selected monitor (Desktop Duplication); it does not inject into games, so
  it is safe with anti-cheat systems.
* In-game FPS in the benchmark requires [PresentMon](https://github.com/GameTechDev/PresentMon) and
  administrator rights (or membership in *Performance Log Users*).
* Previewing HEVC/AV1 clips in the app needs the matching Windows video extensions; recording and
  cutting work without them.
* Duelcliping makes no network connections.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for how it works. ffmpeg is licensed under the GPL
(`ffmpeg/FFMPEG-LICENSE.txt` in the install folder).
