# LEGO Launcher – Build, Konfiguration, Release

## Voraussetzungen

- Node.js ≥ 22.18 und npm
- Für die EXE: Windows (lokal) **oder** GitHub Actions (`.github/workflows/launcher.yml`, Runner `windows-latest`)

## Entwickeln

```bash
npm ci
npm run build -w apps/launcher        # bündelt main/preload/renderer nach apps/launcher/dist
npm start -w apps/launcher            # startet Electron
npm test -w apps/launcher             # Unit-Tests (Login-Kette, Regeln, Downloads, ZIP, JVM-Args)
xvfb-run -a node apps/launcher/test/ui.e2e.mjs   # UI-Test gegen echtes Backend (Linux)
```

## Microsoft-Anmeldung einrichten (Pflicht für den Login)

1. Im [Azure-Portal](https://portal.azure.com) eine App-Registrierung anlegen:
   - Kontotypen: **„Persönliche Microsoft-Konten“** (der Launcher nutzt den `consumers`-Tenant).
   - Authentifizierung → „Öffentliche Clientflows zulassen“ = **Ja** (Device-Code-Flow).
   - Kein Client-Secret nötig (öffentlicher Client).
2. Die App-ID muss von Mojang/Microsoft für die **Minecraft-API freigeschaltet** werden (Antragsformular für Drittanbieter-Launcher). Ohne Freigabe antwortet `api.minecraftservices.com` mit 403; der Launcher zeigt dann „Azure app id is not approved“.
3. Die App-ID als GitHub-Secret `LEGO_MS_CLIENT_ID` hinterlegen (oder lokal als Umgebungsvariable beim Build). Sie wird beim Bündeln eingebettet; sie ist kein Geheimnis im kryptografischen Sinn.

Ablauf im Launcher: Device-Code → Microsoft-Seite im Browser → Xbox Live → XSTS (`rp://api.minecraftservices.com/`) → `login_with_xbox` → Lizenzprüfung (`entitlements/mcstore`) → Profil. Die Microsoft-Refresh-Tokens werden mit `safeStorage` (DPAPI) verschlüsselt gespeichert.

Hinweis: Die Endpunkte wurden aus der etablierten Dokumentation übernommen; der Abruf der aktuellen Doku-Seiten (learn.microsoft.com, minecraft.wiki) war aus der Build-Umgebung gesperrt. Vor dem Release einmal mit einem echten Konto testen.

## LEGO-Server einstellen

- Standard-URL beim Build: Repository-Variable `LEGO_BACKEND_URL` (z. B. `https://api.dein-lego.de`).
- Nutzer können sie unter Einstellungen → Allgemein überschreiben (nur `https://`).
- Der Launcher installiert den LEGO Client von der URL, die das Backend unter `/api/public/client-release` meldet (siehe `docs/BACKEND.md`).

## EXE bauen

**GitHub Actions (empfohlen):** Push auf `apps/launcher/**` oder „Run workflow“. Ergebnis: Artefakt `lego-launcher-windows` mit `LEGO-Launcher-Setup-<version>.exe`, `.blockmap` und `latest.yml`; die SHA-256 steht im Log-Schritt „Checksums“.

**Lokal unter Windows:**

```powershell
npm ci
$env:LEGO_MS_CLIENT_ID="<azure-app-id>"; $env:LEGO_BACKEND_URL="https://api.example"
npm run dist:win -w apps/launcher
# -> apps/launcher/release/LEGO-Launcher-Setup-<version>.exe
```

### Code-Signing

Ohne Zertifikat ist die EXE unsigniert; Windows SmartScreen warnt dann. Mit einem Authenticode-Zertifikat: Secrets `CSC_LINK` (Base64-PFX oder URL) und `CSC_KEY_PASSWORD` setzen – electron-builder signiert EXE, Installer und Uninstaller. electron-updater prüft bei signierten Builds den Herausgeber.

## Release & Auto-Update

1. Version in `apps/launcher/package.json` erhöhen.
2. EXE, `.blockmap` und `latest.yml` aus dem Artefakt als GitHub-Release (`v<version>`) im Repo veröffentlichen.
3. Installierte Launcher fragen beim nächsten Start nach (Standard „Nachfragen“), laden über HTTPS und prüfen die SHA-512 aus `latest.yml`.

## Installer-Verhalten

- NSIS, Installation pro Benutzer (keine Admin-Rechte), Zielordner wählbar, Start-Menü- und Desktop-Verknüpfung.
- Deinstallation entfernt das Programm; Einstellungen und Spielstände (`%APPDATA%/LEGO Launcher`) bleiben erhalten, bis der Nutzer sie löscht.
- Keine Dienste, keine Registry-Tweaks, keine Hintergrundprozesse außer dem sichtbaren Launcher und dem gestarteten Spiel.

## Spieldaten

`%APPDATA%/LEGO Launcher/minecraft/` enthält `versions/`, `libraries/`, `assets/`, `runtime/` (Mojang-Java) und `instances/<profil>/` (eigener Spielordner je Profil mit `mods/`). Vom Launcher installierte Mods stehen in `mods/.lego-managed.json`; nur diese werden bei Updates ersetzt – eigene Mods bleiben unangetastet.
