# SpotifyHUD media bridge (Windows 10/11).
# Reads the currently playing media from the Windows "System Media Transport Controls" (the same source as the
# Windows media flyout) - no Spotify login needed. Prints one JSON object per line on stdout, reads commands on stdin.
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$utf8 = New-Object System.Text.UTF8Encoding($false)
$stdout = [Console]::OpenStandardOutput()
$stdin = New-Object System.IO.StreamReader([Console]::OpenStandardInput(), $utf8)

function Send($obj) {
    $json = ConvertTo-Json -InputObject $obj -Compress -Depth 4
    $bytes = $utf8.GetBytes($json + "`n")
    try { $stdout.Write($bytes, 0, $bytes.Length); $stdout.Flush() } catch { exit 0 }
}
function Log($m) { Send @{ t = 'log'; msg = "$m" } }
function NowMs() { return [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() }

# ---------------------------------------------------------------- WinRT
try {
    Add-Type -AssemblyName System.Runtime.WindowsRuntime
    $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
    $null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]
    $null = [Windows.Media.MediaPlaybackAutoRepeatMode, Windows.Media, ContentType = WindowsRuntime]
    $null = [Windows.Storage.Streams.IRandomAccessStreamWithContentType, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $null = [Windows.Storage.Streams.DataReader, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $null = [Windows.Storage.Streams.IRandomAccessStream, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $null = [Windows.Storage.Streams.IInputStream, Windows.Storage.Streams, ContentType = WindowsRuntime]
    $script:asStreamForRead = [System.IO.WindowsRuntimeStreamExtensions].GetMethod('AsStreamForRead', [Type[]]@([Windows.Storage.Streams.IInputStream]))
} catch {
    Send @{ t = 'fatal'; msg = "WinRT nicht verfuegbar: $($_.Exception.Message)" }
    exit 2
}

$asTaskGeneric = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
} | Select-Object -First 1

function Await($op, [Type]$type) {
    $task = $asTaskGeneric.MakeGenericMethod($type).Invoke($null, @($op))
    if (-not $task.Wait(4000)) { throw 'Timeout' }
    return $task.Result
}

# ---------------------------------------------------------------- per-app volume (Windows Core Audio)
$volOk = $false
$cs = @"
using System;
using System.Diagnostics;
using System.Runtime.InteropServices;
namespace SHud {
  [ComImport, Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")] class MMDeviceEnumeratorCom { }

  [ComImport, Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface IMMDeviceEnumerator {
    [PreserveSig] int EnumAudioEndpoints(int dataFlow, int stateMask, out IntPtr devices);
    [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IMMDevice device);
  }
  [ComImport, Guid("D666063F-1587-4E43-81F1-B948E807363F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface IMMDevice {
    [PreserveSig] int Activate(ref Guid iid, int clsCtx, IntPtr activationParams, [MarshalAs(UnmanagedType.IUnknown)] out object iface);
  }
  [ComImport, Guid("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface IAudioSessionManager2 {
    [PreserveSig] int GetAudioSessionControl(IntPtr a, int b, out IntPtr c);
    [PreserveSig] int GetSimpleAudioVolume(IntPtr a, int b, out IntPtr c);
    [PreserveSig] int GetSessionEnumerator(out IAudioSessionEnumerator sessionEnum);
  }
  [ComImport, Guid("E2F5BB11-0570-40CA-ACDD-3AA01277DEE8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface IAudioSessionEnumerator {
    [PreserveSig] int GetCount(out int count);
    [PreserveSig] int GetSession(int index, out IAudioSessionControl2 session);
  }
  [ComImport, Guid("bfb7ff88-7239-4fc9-8fa2-07c950be9c6d"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface IAudioSessionControl2 {
    [PreserveSig] int GetState(out int state);
    [PreserveSig] int GetDisplayName(out IntPtr name);
    [PreserveSig] int SetDisplayName(IntPtr name, IntPtr ctx);
    [PreserveSig] int GetIconPath(out IntPtr path);
    [PreserveSig] int SetIconPath(IntPtr path, IntPtr ctx);
    [PreserveSig] int GetGroupingParam(out Guid g);
    [PreserveSig] int SetGroupingParam(IntPtr g, IntPtr ctx);
    [PreserveSig] int RegisterAudioSessionNotification(IntPtr n);
    [PreserveSig] int UnregisterAudioSessionNotification(IntPtr n);
    [PreserveSig] int GetSessionIdentifier(out IntPtr id);
    [PreserveSig] int GetSessionInstanceIdentifier(out IntPtr id);
    [PreserveSig] int GetProcessId(out uint pid);
    [PreserveSig] int IsSystemSoundsSession();
    [PreserveSig] int SetDuckingPreference(bool optOut);
  }
  [ComImport, Guid("87CE5498-68D6-44E5-9215-6DA47EF883D8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
  interface ISimpleAudioVolume {
    [PreserveSig] int SetMasterVolume(float level, ref Guid ctx);
    [PreserveSig] int GetMasterVolume(out float level);
    [PreserveSig] int SetMute(bool mute, ref Guid ctx);
    [PreserveSig] int GetMute(out bool mute);
  }

  public static class Vol {
    static System.Collections.Generic.List<ISimpleAudioVolume> Find(string proc) {
      var list = new System.Collections.Generic.List<ISimpleAudioVolume>();
      if (string.IsNullOrEmpty(proc)) return list;
      proc = proc.ToLowerInvariant();
      var en = (IMMDeviceEnumerator)(new MMDeviceEnumeratorCom());
      IMMDevice dev;
      if (en.GetDefaultAudioEndpoint(0, 1, out dev) != 0 || dev == null) return list;
      Guid iid = typeof(IAudioSessionManager2).GUID;
      object o;
      if (dev.Activate(ref iid, 23, IntPtr.Zero, out o) != 0 || o == null) return list;
      var mgr = (IAudioSessionManager2)o;
      IAudioSessionEnumerator se;
      if (mgr.GetSessionEnumerator(out se) != 0 || se == null) return list;
      int n; se.GetCount(out n);
      for (int i = 0; i < n; i++) {
        IAudioSessionControl2 c;
        if (se.GetSession(i, out c) != 0 || c == null) continue;
        uint pid; if (c.GetProcessId(out pid) != 0 || pid == 0) continue;
        string name;
        try { name = Process.GetProcessById((int)pid).ProcessName.ToLowerInvariant(); } catch { continue; }
        if (name == proc || name.StartsWith(proc)) {
          var v = c as ISimpleAudioVolume;
          if (v != null) list.Add(v);
        }
      }
      return list;
    }
    // returns -1 if no session, otherwise 0..100 (+1000 if muted)
    public static int Get(string proc) {
      foreach (var v in Find(proc)) {
        float f; bool m;
        if (v.GetMasterVolume(out f) != 0) continue;
        v.GetMute(out m);
        int p = (int)Math.Round(f * 100f);
        return m ? p + 1000 : p;
      }
      return -1;
    }
    public static bool Set(string proc, int percent) {
      Guid g = Guid.Empty; bool ok = false;
      float f = Math.Max(0, Math.Min(100, percent)) / 100f;
      foreach (var v in Find(proc)) {
        if (v.SetMasterVolume(f, ref g) == 0) ok = true;
        if (percent > 0) v.SetMute(false, ref g);
      }
      return ok;
    }
    public static bool ToggleMute(string proc) {
      Guid g = Guid.Empty; bool ok = false; bool? target = null;
      foreach (var v in Find(proc)) {
        bool m; v.GetMute(out m);
        if (target == null) target = !m;
        if (v.SetMute(target.Value, ref g) == 0) ok = true;
      }
      return ok;
    }
  }
}
"@
try { Add-Type -TypeDefinition $cs -Language CSharp; $volOk = $true } catch { Log "Lautstaerke-Steuerung nicht verfuegbar: $($_.Exception.Message)" }

# ---------------------------------------------------------------- session handling
try {
    $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
} catch {
    Send @{ t = 'fatal'; msg = "Media-Session-Manager: $($_.Exception.Message)" }
    exit 3
}

function Get-Session {
    try {
        foreach ($s in $mgr.GetSessions()) {
            if ($s.SourceAppUserModelId -match 'spotify') { return $s }
        }
        return $mgr.GetCurrentSession()
    } catch { return $null }
}

function Proc-Name($aumid) {
    if ($aumid -match 'spotify') { return 'spotify' }
    $n = $aumid
    if ($n -match '!') { $n = $n.Split('!')[-1] }
    if ($n -match '\\') { $n = $n.Split('\')[-1] }
    if ($n.ToLower().EndsWith('.exe')) { $n = $n.Substring(0, $n.Length - 4) }
    return $n.ToLower()
}

function Read-Thumb($props) {
    $stream = Await ($props.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])
    try {
        try {
            # PowerShell can't bind WinRT interfaces directly -> call through reflection
            $net = $script:asStreamForRead.Invoke($null, @($stream))
            $ms = New-Object System.IO.MemoryStream
            $net.CopyTo($ms)
            return $ms.ToArray()
        } catch {
            $size = [uint64][Windows.Storage.Streams.IRandomAccessStream].GetProperty('Size').GetValue($stream)
            $in = [Windows.Storage.Streams.IRandomAccessStream].GetMethod('GetInputStreamAt').Invoke($stream, @([uint64]0))
            $reader = [Windows.Storage.Streams.DataReader]::new($in)
            $null = Await ($reader.LoadAsync([uint32]$size)) ([uint32])
            $bytes = New-Object byte[] ([int]$size)
            $reader.ReadBytes($bytes)
            return $bytes
        }
    } finally {
        try { [System.IDisposable].GetMethod('Dispose').Invoke($stream, @()) } catch { }
    }
}

function Handle($line, $s) {
    $cmd = $line.Trim()
    if ($cmd -eq '') { return }
    $ok = $false
    if ($cmd -eq 'ping') { Send @{ t = 'pong'; now = (NowMs) }; return }
    if ($null -eq $s -and -not ($cmd -like 'vol*' -or $cmd -eq 'mute')) { Send @{ t = 'ack'; cmd = $cmd; ok = $false }; return }
    $proc = if ($null -ne $s) { Proc-Name $s.SourceAppUserModelId } else { 'spotify' }
    if ($cmd -eq 'toggle') { $ok = Await ($s.TryTogglePlayPauseAsync()) ([bool]) }
    elseif ($cmd -eq 'play') { $ok = Await ($s.TryPlayAsync()) ([bool]) }
    elseif ($cmd -eq 'pause') { $ok = Await ($s.TryPauseAsync()) ([bool]) }
    elseif ($cmd -eq 'next') { $ok = Await ($s.TrySkipNextAsync()) ([bool]) }
    elseif ($cmd -eq 'prev') { $ok = Await ($s.TrySkipPreviousAsync()) ([bool]) }
    elseif ($cmd -match '^seek (\d+)$') { $ok = Await ($s.TryChangePlaybackPositionAsync([long]$matches[1] * 10000)) ([bool]) }
    elseif ($cmd -match '^shuffle (true|false)$') { $ok = Await ($s.TryChangeShuffleActiveAsync($matches[1] -eq 'true')) ([bool]) }
    elseif ($cmd -match '^repeat ([012])$') { $ok = Await ($s.TryChangeAutoRepeatModeAsync([Windows.Media.MediaPlaybackAutoRepeatMode][int]$matches[1])) ([bool]) }
    elseif ($cmd -match '^vol (\d+)$') { if ($volOk) { $ok = [SHud.Vol]::Set($proc, [int]$matches[1]) } }
    elseif ($cmd -eq 'mute') { if ($volOk) { $ok = [SHud.Vol]::ToggleMute($proc) } }
    Send @{ t = 'ack'; cmd = $cmd; ok = [bool]$ok }
}

# ---------------------------------------------------------------- main loop
Send @{ t = 'hello'; vol = $volOk; now = (NowMs) }
$pending = $stdin.ReadLineAsync()
$lastState = ''
$lastSend = [DateTime]::MinValue
$lastThumbKey = $null
$props = $null
$propsAt = [DateTime]::MinValue
$propsSession = ''
$thumbCheckAt = $null
$thumbHash = ''
$thumbTries = 0
$volume = -1
$volAt = [DateTime]::MinValue

while ($true) {
    $s = Get-Session

    while ($pending.IsCompleted) {
        $line = $null
        try { $line = $pending.Result } catch { exit 0 }
        if ($null -eq $line) { exit 0 }  # Minecraft closed the pipe
        $pending = $stdin.ReadLineAsync()
        try { Handle $line $s } catch { Send @{ t = 'ack'; cmd = "$line"; ok = $false; err = "$($_.Exception.Message)" } }
        $propsAt = [DateTime]::MinValue
    }

    if ($null -eq $s) {
        $json = '{"t":"state","has":false}'
        if ($json -ne $lastState -or ((Get-Date) - $lastSend).TotalMilliseconds -gt 2000) {
            Send @{ t = 'state'; has = $false; now = (NowMs) }
            $lastState = $json; $lastSend = Get-Date
        }
        Start-Sleep -Milliseconds 400
        continue
    }

    try {
        $now = Get-Date
        $aumidNow = "$($s.SourceAppUserModelId)"
        if ($null -eq $props -or $propsSession -ne $aumidNow -or ($now - $propsAt).TotalMilliseconds -gt 600) {
            $props = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
            $propsAt = $now
            $propsSession = $aumidNow
        }
        $pb = $s.GetPlaybackInfo()
        $tl = $s.GetTimelineProperties()
        $aumid = "$($s.SourceAppUserModelId)"
        if ($volOk -and ($now - $volAt).TotalMilliseconds -gt 500) {
            try { $volume = [SHud.Vol]::Get((Proc-Name $aumid)) } catch { $volume = -1 }
            $volAt = $now
        }

        $shuffle = $null; $repeat = $null
        try { if ($null -ne $pb.IsShuffleActive) { $shuffle = [bool]$pb.IsShuffleActive } } catch { }
        try { if ($null -ne $pb.AutoRepeatMode) { $repeat = [int]$pb.AutoRepeatMode } } catch { }
        $upd = 0
        try { $upd = $tl.LastUpdatedTime.ToUnixTimeMilliseconds() } catch { }

        $st = [ordered]@{
            t       = 'state'
            has     = $true
            app     = $aumid
            title   = "$($props.Title)"
            artist  = "$($props.Artist)"
            album   = "$($props.AlbumTitle)"
            status  = [int]$pb.PlaybackStatus
            pos     = [long]$tl.Position.TotalMilliseconds
            dur     = [long]($tl.EndTime - $tl.StartTime).TotalMilliseconds
            upd     = [long]$upd
            shuffle = $shuffle
            repeat  = $repeat
            vol     = $volume
            cShuf   = [bool]$pb.Controls.IsShuffleEnabled
            cRep    = [bool]$pb.Controls.IsRepeatEnabled
            cSeek   = [bool]$pb.Controls.IsPlaybackPositionEnabled
        }
        $json = ConvertTo-Json -InputObject $st -Compress
        if ($json -ne $lastState -or ($now - $lastSend).TotalMilliseconds -gt 2000) {
            $st['now'] = NowMs
            Send $st
            $lastState = $json; $lastSend = $now
        }

        $key = "$aumid|$($props.Title)|$($props.Artist)|$($props.AlbumTitle)"
        $tkey = "$($props.Title)|$($props.Artist)"
        if ($key -ne $lastThumbKey) {
            # new track: send cover now and re-check once later (Windows sometimes delivers the new cover late)
            $lastThumbKey = $key
            $thumbHash = ''
            $thumbTries = 0
            $thumbCheckAt = $now
        }
        if ($null -ne $thumbCheckAt -and $now -ge $thumbCheckAt) {
            $thumbTries++
            $data = ''
            if ($null -ne $props.Thumbnail) {
                try { $data = [Convert]::ToBase64String((Read-Thumb $props)) } catch { if ($thumbTries -eq 5) { Log "Cover: $($_.Exception.Message)" } }
            }
            if ($data -ne $thumbHash) {
                Send @{ t = 'thumb'; key = $tkey; data = $data }
                $thumbHash = $data
            }
            if ($thumbTries -lt 5) { $thumbCheckAt = $now.AddMilliseconds(900) }
            elseif ($data -eq '') { $thumbCheckAt = $now.AddSeconds(8) }   # still no cover: keep trying slowly
            else { $thumbCheckAt = $null }
        }
    } catch {
        Log "Status: $($_.Exception.Message)"
        $props = $null
        Start-Sleep -Milliseconds 500
    }
    Start-Sleep -Milliseconds 150
}
