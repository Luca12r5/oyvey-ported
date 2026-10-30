; Duelcliping installer (Inno Setup 6). Built by scripts/build.ps1 or CI.
#ifndef AppVersion
  #define AppVersion "1.0.0"
#endif
#ifndef PublishDir
  #define PublishDir "..\artifacts\publish"
#endif

[Setup]
AppId={{6C2F7B0E-4E0B-4F7A-9B57-2D1C0E5A9F31}
AppName=Duelcliping
AppVersion={#AppVersion}
AppPublisher=Duelcliping
AppComments=Gaming clip recorder with replay buffer, full recording and built-in video cutter
DefaultDirName={autopf}\Duelcliping
DefaultGroupName=Duelcliping
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
PrivilegesRequiredOverridesAllowed=dialog
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
MinVersion=10.0.17763
OutputBaseFilename=Duelcliping-Setup-{#AppVersion}
SetupIconFile=..\src\Duelcliping.App\Assets\duelcliping.ico
UninstallDisplayIcon={app}\Duelcliping.exe
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
CloseApplications=yes
RestartApplications=no

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Shortcuts:"
Name: "autostart"; Description: "Start Duelcliping with Windows (runs quietly in the tray)"; GroupDescription: "Startup:"

[Files]
Source: "{#PublishDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Duelcliping"; Filename: "{app}\Duelcliping.exe"
Name: "{group}\Uninstall Duelcliping"; Filename: "{uninstallexe}"
Name: "{autodesktop}\Duelcliping"; Filename: "{app}\Duelcliping.exe"; Tasks: desktopicon

[Registry]
; Same entry the app manages from Settings → General → Start with Windows.
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "Duelcliping"; \
    ValueData: """{app}\Duelcliping.exe"" --background"; Flags: uninsdeletevalue; Tasks: autostart

[Run]
Filename: "{app}\Duelcliping.exe"; Description: "Launch Duelcliping"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{cmd}"; Parameters: "/C taskkill /IM Duelcliping.exe /F"; Flags: runhidden; RunOnceId: "StopDuelcliping"

[UninstallDelete]
Type: filesandordirs; Name: "{localappdata}\Duelcliping\logs"
Type: files; Name: "{localappdata}\Duelcliping\encoders.json"
