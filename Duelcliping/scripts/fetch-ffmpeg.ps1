# Downloads a GPL ffmpeg build for Windows (BtbN/FFmpeg-Builds, release branch) into third_party/ffmpeg.
# The build includes NVENC, AMF, QSV and libx264. Duelcliping runs ffmpeg as a separate process.
param([string]$Destination = (Join-Path $PSScriptRoot "..\third_party\ffmpeg"))
$ErrorActionPreference = "Stop"
$headers = @{ "User-Agent" = "duelcliping-build" }
if ($env:GITHUB_TOKEN) { $headers["Authorization"] = "Bearer $env:GITHUB_TOKEN" }
$release = Invoke-RestMethod -Headers $headers -Uri "https://api.github.com/repos/BtbN/FFmpeg-Builds/releases/tags/latest"
$asset = $release.assets |
    Where-Object { $_.name -match '^ffmpeg-n(\d+)\.(\d+)-latest-win64-gpl-\d+\.\d+\.zip$' } |
    Sort-Object { [int]($_.name -replace '^ffmpeg-n(\d+)\.(\d+).*','$1') * 100 + [int]($_.name -replace '^ffmpeg-n(\d+)\.(\d+).*','$2') } -Descending |
    Select-Object -First 1
if (-not $asset) { throw "No suitable ffmpeg release asset found" }
Write-Host "Using $($asset.name)"
$zip = Join-Path $env:TEMP $asset.name
Invoke-WebRequest -Headers $headers -Uri $asset.browser_download_url -OutFile $zip
$extract = Join-Path $env:TEMP "ffmpeg-extract"
if (Test-Path $extract) { Remove-Item $extract -Recurse -Force }
Expand-Archive $zip -DestinationPath $extract
$root = Get-ChildItem $extract -Directory | Select-Object -First 1
New-Item -ItemType Directory -Force -Path $Destination | Out-Null
Copy-Item (Join-Path $root.FullName "bin\ffmpeg.exe") $Destination -Force
Copy-Item (Join-Path $root.FullName "bin\ffprobe.exe") $Destination -Force
Copy-Item (Join-Path $root.FullName "LICENSE.txt") (Join-Path $Destination "FFMPEG-LICENSE.txt") -Force
Set-Content -Path (Join-Path $Destination "VERSION.txt") -Value $asset.name
& (Join-Path $Destination "ffmpeg.exe") -hide_banner -version | Select-Object -First 1
