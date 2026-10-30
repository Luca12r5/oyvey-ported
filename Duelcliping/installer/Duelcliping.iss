; Moments installer (Inno Setup 6). Built by scripts/build.ps1 or CI.
#ifndef AppVersion
  #define AppVersion "1.0.0"
#endif
#ifndef PublishDir
  #define PublishDir "..\artifacts\publish"
#endif

[Setup]
AppId={{9B1E5D2A-7C44-4E61-A1F3-5D0C8E2B7A94}
AppName=Moments
AppVersion={#AppVersion}
AppPublisher=Moments
AppComments=Gaming clip recorder with replay buffer, full recording and built-in video cutter
DefaultDirName={autopf}\Moments
DefaultGroupName=Moments
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
MinVersion=10.0.17763
OutputBaseFilename=Moments-Setup-{#AppVersion}
SetupIconFile=..\src\Duelcliping.App\Assets\moments.ico
UninstallDisplayIcon={app}\Moments.exe
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
CloseApplications=yes
RestartApplications=no

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Shortcuts:"
Name: "autostart"; Description: "Start Moments with Windows (runs quietly in the tray)"; GroupDescription: "Startup:"

[Files]
Source: "{#PublishDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Moments"; Filename: "{app}\Moments.exe"
Name: "{group}\Uninstall Moments"; Filename: "{uninstallexe}"
Name: "{autodesktop}\Moments"; Filename: "{app}\Moments.exe"; Tasks: desktopicon

[Registry]
; Same entry the app manages from Settings → General → Start with Windows.
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "Moments"; \
    ValueData: """{app}\Moments.exe"" --background"; Flags: uninsdeletevalue; Tasks: autostart

[Run]
Filename: "{app}\Moments.exe"; Description: "Launch Moments"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{cmd}"; Parameters: "/C taskkill /IM Moments.exe /F"; Flags: runhidden; RunOnceId: "StopMoments"

[UninstallDelete]
Type: filesandordirs; Name: "{localappdata}\Moments\logs"
Type: files; Name: "{localappdata}\Moments\encoders.json"
