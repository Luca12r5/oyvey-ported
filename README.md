<div align="center">

# LEGO Launcher

Minecraft-Launcher mit eigenem Fabric-Client, Konto, Freunden, Cosmetics, Credits, Battle Pass und Website.

</div>

| Teil | Ordner | Bauen / Testen |
|---|---|---|
| LEGO Client (Fabric, MC 1.21.11) | `lego-client/` | `./gradlew -p lego-client build` |
| LEGO Launcher (Electron, Windows-EXE) | `apps/launcher/` | `npm run build -w apps/launcher`, EXE: `npm run dist:win -w apps/launcher` |
| Backend + Website | `apps/backend/`, `apps/website/` | `npm run backend`, `npm test`, `npm run e2e` |
| Gemeinsame Daten (Themes, Katalog, Regeln) | `packages/shared/` | `npm test -w packages/shared` |

Dokumentation:

- [Bestandsaufnahme](docs/ANALYSIS.md) – was im alten Projekt war und was daran nicht stimmte
- [Architektur & Sicherheit](docs/ARCHITECTURE.md)
- [Roadmap & ehrlicher Status](docs/ROADMAP.md)
- [Launcher bauen, Microsoft-Login, Release](docs/LAUNCHER.md)
- [Backend betreiben, Admin, Credits, Zahlungen, Backups](docs/BACKEND.md)

`src/` im Wurzelverzeichnis ist die ursprüngliche OyVey-Client-Basis (von [@cattyngmd](https://github.com/cattyngmd)); sie bleibt unverändert und wird vom Launcher nicht installiert. `legacy/` enthält das Original-JAR von LEGO Client 3.4.0.

LEGO Launcher ist ein Fanprojekt und steht in keiner Verbindung zu Mojang Studios, Microsoft oder der LEGO Group. Der Name „LEGO“ ist eine Marke der LEGO Group – vor einer öffentlichen Veröffentlichung sollte der Produktname rechtlich geprüft werden.
