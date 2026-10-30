# Duelcliping – Architecture

## 1. Technology choice

| Concern | Choice | Why |
|---|---|---|
| UI | **WPF on .NET 8** (C#) | Native Windows desktop, hardware-composited, mature, no browser engine (Electron would cost 150–300 MB RAM and a Chromium GPU process while you play). The window is created only when opened and destroyed when closed to the tray. |
| Screen capture | **DXGI Desktop Duplication** via ffmpeg `ddagrab` | The OS hands over the composed desktop as a GPU texture. No hooks or DLL injection into games (anti-cheat safe), works for borderless and most fullscreen games, and frames never leave GPU memory. |
| Encoding | **ffmpeg** with **NVENC / AMF / Quick Sync** | One battle-tested implementation of all three vendor SDKs, H.264/HEVC/AV1, rate control and muxing. It runs as a separate process: if a driver crashes the encoder, Duelcliping survives and restarts it. |
| Audio | **WASAPI** (NAudio) shared-mode loopback + capture | The native Windows audio API; event driven, no polling. |
| Hotkeys | `RegisterHotKey` | Handled by the OS, no keyboard hook, no added input latency, works while a game has focus. |
| Installer | **Inno Setup**, self-contained .NET publish | One `.exe` installer, no .NET runtime prerequisite, per-user install without admin rights. |

## 2. Modules

```
Duelcliping.Core (platform-neutral, unit tested on Linux + Windows CI)
├── Recording/   RecordingEngine  – supervisor: start, watchdog, restart, fallback chain
│                CapturePipeline  – one ffmpeg process + audio pipes + stream reader
│                ReplayBuffer     – in-memory rolling buffer of encoded MPEG-TS (GOP chunks)
│                ClipWriter       – snapshot → .ts.partial → remux → .mp4/.mkv
│                RecordingSink    – manual "full video" recording from the same stream
├── Encoders/    EncoderCatalog   – functional detection of NVENC/AMF/QSV/x264, selection
│                FfmpegCommandBuilder / PipelineConfig – zero-copy filter graphs, bitrates
├── Audio/       AudioPump        – format conversion, resampling, volume, real-time clocking
├── Library/     ClipLibrary      – index, ffprobe metadata, thumbnails, rename/delete/pin, recovery
│                ClipEditor/CutPlan – the built-in video cutter (multi-segment, lossless or exact)
├── Storage/     StorageManager   – quota, age-based retention
├── Performance/ PerformanceMonitor, BenchmarkRunner, PresentMon parsing, FrameTimeStats
├── Settings/    AppSettings, SettingsManager (atomic JSON)
└── Logging/     Log (background file logger with rotation)

Duelcliping.App (WPF, Windows only)
├── Services/    AppController (composition root), HotkeyManager, WasapiAudioSource,
│                StartupManager, WindowsSystemMetrics (CPU/GPU counters), GameActivityDetector
├── Interop/     Win32 + DXGI monitor enumeration
├── ViewModels/  Dashboard, Library, Editor (cutter), Performance, Settings
├── Views/       MainWindow, pages, RecordingOverlay, ToastWindow, dialogs
├── Controls/    CutTimeline (custom-drawn), converters
└── Theme/       Colors.Dark / Colors.Light / Controls (design system)
```

The engine knows nothing about the UI. The app talks to it only through `AppController`.

## 3. Recording pipeline

```
 GPU                                        ffmpeg process                       Duelcliping process
┌──────────────┐  D3D11 texture   ┌──────────────────────────────┐  MPEG-TS   ┌─────────────────────┐
│ Desktop Dup. │ ───────────────▶ │ ddagrab → (scale_cuda |      │ ─stdout──▶ │ ReplayBuffer (RAM)  │
│ (ddagrab)    │                  │ vpp_amf | vpp_qsv) → NVENC/  │            │  GOP chunks, ring   │
└──────────────┘                  │ AMF/QSV encoder → TS muxer   │            └────────┬────────────┘
 WASAPI loopback ─▶ AudioPump ─named pipe─▶ f32le → AAC ──────────┘                     │ on hotkey
 WASAPI mic      ─▶ AudioPump ─named pipe─▶ f32le → AAC (track 2)                       ▼
                                                                         ClipWriter: .ts.partial → remux → .mp4
                                                                         RecordingSink: chunks → disk (manual)
```

* **Zero copy**: with a hardware encoder the captured texture is mapped straight into the encoder
  (`hwmap` to CUDA / QSV, or D3D11 input for AMF). Scaling to 1080p etc. happens on the GPU. No
  frame is ever copied into system memory or touched by the CPU.
* **Fallback chain** (`RecordingEngine.BuildPlan`): if a path fails to deliver video it steps to the
  next one and tells the user: zero-copy → GPU capture + CPU conversion → GDI capture → CPU encoder
  (only if no hardware path works at all).
* **Watchdog**: no keyframe within 20 s, or no data for 10 s, kills and restarts the process.
  Display mode changes (games switching resolution) and resume from sleep trigger a clean restart.

## 4. The rolling buffer

The encoder runs continuously, so the buffer stores **already encoded** video – about 25 MB per
10 s at 1080p60 "High" – instead of raw frames (which would be ~3.7 GB per 10 s).

* ffmpeg writes MPEG-TS to stdout. A reader thread parses 188-byte packets, follows PAT → PMT to
  find the video PID, and detects keyframes via the random-access indicator. GOP length is one
  second, and every keyframe carries SPS/PPS (`dump_extra`), so **every chunk is a valid start**.
* Each keyframe starts a new `GopChunk`. Chunks live in fixed 256 KB blocks from a `BlockPool`, so
  the steady state allocates nothing and the GC never has to move video data while you play.
* Chunks older than the configured length are evicted and their blocks re-used. A hard memory cap
  (2× the expected size) protects against runaway bitrates.
* **Saving** takes a reference-counted snapshot of the needed chunks (O(1), no copying), writes them
  to `name.ts.partial`, remuxes with `-c copy` into MP4/MKV (no re-encoding, < 1 s for 30 s of
  video) and renames atomically. If remuxing ever fails the playable `.ts` is kept – clips are never
  dropped silently. Leftover `.ts.partial` files (crash, power loss) are recovered at start-up.
* Disk is touched **only when you save**. The replay buffer never writes to disk.

## 5. Manual recording ("full video")

Manual recording does **not** start a second encoder. `RecordingSink` subscribes to the same
chunk stream and appends every sealed chunk to a `.ts.partial` file on a background writer, then
remuxes on stop. With the replay buffer off, the capture runs only while recording. If the capture
restarts with a different resolution mid-recording, a new part file is started instead of
producing a corrupt file. The disk-space check stops and saves the recording before the drive fills.

## 6. The video cutter

`CutPlan` models the timeline as contiguous pieces that are kept or removed (split at playhead,
remove/keep part, trim start/end, undo/redo). `ClipEditor` turns the kept segments into a file:

* **Lossless**: each segment is stream-copied (cuts snap to the previous keyframe ≤ 1 s) into a
  temporary Matroska part, then joined with the concat demuxer – instant, no quality loss.
  (Matroska parts are used because TS/MP4 parts produce duplicate timestamps at the seams.)
* **Frame-exact**: one ffmpeg pass with `trim/atrim` per segment and the `concat` filter, encoded
  with the detected hardware encoder; optional downscale to 1080p/720p.

All audio tracks are preserved in both modes.

## 7. Hardware encoder integration

`EncoderCatalog` lists encoders reported by ffmpeg and **functionally probes** each one by encoding
a few frames – every ffmpeg build lists NVENC, but only a working driver/GPU passes. AV1 is
additionally verified to round-trip through MPEG-TS. Results are cached for 7 days ("Detect
encoders again" refreshes). Selection: preferred vendor → any hardware encoder for the codec →
hardware H.264 → CPU only if no hardware encoder works (with a visible warning).

Encoder settings favour the game: low-latency tuning, no B-frames, no look-ahead, VBR with a peak
cap, 1 s GOP. Performance mode picks the fastest preset (NVENC `p1`, AMF `speed`, QSV `veryfast`).

## 8. Minimising performance impact

* GPU: capture and encode stay on the GPU; NVENC/AMF/QSV run on dedicated encoder silicon, not the
  3D engine the game uses. Scaling uses the vendor's video processor.
* CPU: no per-frame CPU work in Duelcliping; the TS reader handles ~2–6 MB/s. ffmpeg and the app run
  at *below normal* priority in performance mode, so the game wins every scheduling decision.
* Memory: pooled buffers, workstation GC with conserve-memory mode, main window destroyed when
  closed, working set trimmed when a fullscreen game takes focus.
* Disk: zero writes until you save; clip saves are sequential 1 MB writes.
* Network: none. Duelcliping makes no network connections at all.
* Idle work: no polling loops except a 3 s foreground-window check (three Win32 calls); the
  performance monitor samples only while its page or the dashboard is visible; the overlay redraws
  at most once per second; thumbnails, scans and retention are deferred while a game is fullscreen.

## 9. Measuring overhead

The Performance page shows live, measured CPU/GPU/encoder/memory values for Duelcliping and ffmpeg
(GetSystemTimes, process CPU time, the "GPU Engine" counters used by Task Manager), the capture
frame rate, dropped/repeated frames, encoder speed and backlog, and the last clip's disk write speed.

The benchmark runs the same scene twice – recording **off**, then **on** – and reports the
differences. With PresentMon configured it also measures the game's average FPS, 1 % lows and
frame-time standard deviation from ETW present events. Reports are saved as JSON. Duelcliping does
not claim zero overhead; it shows you the measured numbers for your system.

## 10. Error handling

* Encoder start failures → automatic fallback with an explanation.
* Runtime failures → restart; repeated failures → "Faulted" with the ffmpeg error, retry every 30 s.
* Audio device unplugged/changed → capture re-opens automatically; silence keeps the track in sync.
* Disk full → save refused with a clear message; manual recording stops and is finalised.
* Unsupported resolution → never upscales; output size is always even and ≤ the monitor.
* Settings → atomic writes; corrupt files are backed up and replaced by defaults.
* Everything is logged to `%LOCALAPPDATA%\Duelcliping\logs` (5 rotating files × 5 MB).

## 11. Testing

* `tests/Duelcliping.Core.Tests` (xUnit, runs on Linux and Windows CI with real ffmpeg): TS
  parsing, buffer eviction, snapshots under concurrent eviction, MP4/MKV/AV1 remux, a live engine
  with two audio tracks, manual recording with and without replay buffer, restart after the encoder
  process is killed, audio clocking and resampling, the cut model and the cutter output.
* `Duelcliping.exe --selftest` (Windows CI): renders every page in both themes, reports binding
  errors, detects encoders with the bundled ffmpeg and runs a real cut through the editor.
