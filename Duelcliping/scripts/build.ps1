# Builds Duelcliping: tests, self-contained publish, ffmpeg, installer.
# Usage: pwsh scripts/build.ps1 [-SkipTests] [-SkipInstaller]
param([switch]$SkipTests, [switch]$SkipInstaller, [string]$Version = "1.0.0")
$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$out = Join-Path $root "artifacts"
$publish = Join-Path $out "publish"
$ffmpeg = Join-Path $root "third_party\ffmpeg"

if (-not (Test-Path (Join-Path $ffmpeg "ffmpeg.exe"))) { & (Join-Path $PSScriptRoot "fetch-ffmpeg.ps1") -Destination $ffmpeg }
$env:PATH = "$ffmpeg;$env:PATH"

if (-not $SkipTests) {
    dotnet test (Join-Path $root "tests\Duelcliping.Core.Tests") -c Release
    if ($LASTEXITCODE -ne 0) { throw "Tests failed" }
}

if (Test-Path $publish) { Remove-Item $publish -Recurse -Force }
dotnet publish (Join-Path $root "src\Duelcliping.App\Duelcliping.App.csproj") -c Release -r win-x64 --self-contained true `
    -p:PublishReadyToRun=true -p:Version=$Version -o $publish
if ($LASTEXITCODE -ne 0) { throw "Publish failed" }
New-Item -ItemType Directory -Force -Path (Join-Path $publish "ffmpeg") | Out-Null
Copy-Item (Join-Path $ffmpeg "*") (Join-Path $publish "ffmpeg") -Force

if (-not $SkipInstaller) {
    $iscc = (Get-Command iscc.exe -ErrorAction SilentlyContinue).Source
    if (-not $iscc) { $iscc = "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe" }
    & $iscc "/DAppVersion=$Version" "/DPublishDir=$publish" "/O$out" (Join-Path $root "installer\Duelcliping.iss")
    if ($LASTEXITCODE -ne 0) { throw "Installer build failed" }
}
Write-Host "Done. Output in $out"
