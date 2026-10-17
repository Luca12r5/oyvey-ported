# VR-Modus

Der VR-Modus des LEGO Launchers ist eine Halloween-Lobby, in die man hineingeht:
Steinplattform mit Runenring, Kürbiskreis, Grabsteine, toter Wald, Mond, ein summendes Portal
(Raumklang), Irrlichter und Fledermäuse. Darin schweben drei Panels:

| Panel | Was es tut |
|---|---|
| **Click GUI** | Module des LEGO-Client-Profils an/aus (schreibt `config/legoclient.json`, gilt ab dem nächsten Spielstart), Launcher-Effekte, Lobby-Optionen, Ansicht zurücksetzen, VR verlassen |
| **Browser** | Echter Chromium-Browser mit Google-Suche: Zurück/Vor, Neu laden, Startseite, Adressleiste, Klicken, Scrollen, Tippen |
| **Tastatur** | Virtuelle QWERTZ-Tastatur für Adressleiste und Webseiten |

Einstieg: Eine 3D-VR-Brille fliegt heran und legt sich über das Gesicht, ein Linsenblitz, danach
lichtet sich der Nebel und die Panels öffnen sich. Nach dem Einstieg bewegt sich die Kamera nie
von selbst (Komfort).

## Voraussetzungen für echte VR

- PC-VR-Brille, z. B. Meta Quest über Link/Air Link, Valve Index, HTC Vive, Windows Mixed Reality.
- Eine **OpenXR-Laufzeit** als Standard (Meta Quest Link, SteamVR oder Windows Mixed Reality).
- Der Launcher nutzt WebXR (`immersive-vr`) im eingebauten Chromium von Electron. Die VR-Seite
  prüft per `navigator.xr.isSessionSupported('immersive-vr')`, ob eine Brille bereit ist.
  Electron unterstützt WebXR nicht offiziell in allen Versionen/Plattformen. Wird keine Brille
  erkannt oder schlägt der Start fehl, läuft automatisch die **Desktop-VR-Vorschau** mit derselben
  Welt und denselben Panels.

**Getestet:** Desktop-Vorschau (Lobby, Panels, Browser-Rendering, Verlassen) automatisiert unter
Linux/xvfb. **Nicht getestet:** eine echte Brille. Dafür gab es in der Entwicklungsumgebung keine
Hardware.

## Steuerung

| | VR-Controller | Desktop-Vorschau |
|---|---|---|
| Zeigen | Laserstrahl | Maus |
| Klicken | Trigger | Linksklick |
| Panel greifen/bewegen | Griff-Taste halten | Shift + Ziehen (oder mittlere Taste) |
| Näher/weiter | Stick hoch/runter (beim Greifen) | – |
| Größer/kleiner | Stick links/rechts (beim Greifen) | Mausrad über dem Panel |
| Scrollen im Browser | – | Mausrad über der Seite |
| Umsehen | Kopf drehen | Rechte Maustaste ziehen |
| Verlassen | „VR verlassen“ oder Brille absetzen | „VR verlassen“ oder Esc |

## Sicherheit des Browsers

Der Browser läuft in einem unsichtbaren Offscreen-Fenster in einer **eigenen, abgeschotteten
Sitzung** (`persist:vrbrowser`, Sandbox, kein Preload, kein Zugriff auf den Launcher oder auf
Konten). Seine Bilder werden als JPEG an das 3D-Panel gestreamt.

- Nur `https`-Seiten (`http` wird auf `https` umgeschrieben, alles andere blockiert).
- **SafeSearch ist erzwungen**: Google-Suchen bekommen immer `safe=active`, Bing `adlt=strict`,
  YouTube wird mit `YouTube-Restrict: Strict` aufgerufen.
- Keine Downloads, keine Berechtigungen (Kamera, Mikrofon, Standort usw.), keine Pop-up-Fenster.
- Eingaben aus dem Renderer werden im Main-Prozess geprüft und begrenzt (`src/core/vrweb.ts`).
- Ist eine Seite nicht erreichbar, erscheint eine verständliche Fehlerseite statt einer leeren.

## Dateien

- `apps/launcher/src/renderer/vr/lobby.ts`: Szene, Einstieg, Panels, Zeiger, Controller.
- `apps/launcher/src/renderer/vr/panel.ts`: Canvas-Panels mit Klickbereichen.
- `apps/launcher/src/renderer/pages/vr.ts`: VR-Seite im Launcher.
- `apps/launcher/src/main/vrbrowser.ts`: Offscreen-Browser, Sitzungsregeln, Eingaben.
- `apps/launcher/src/core/vrweb.ts`, `apps/launcher/src/core/clientcfg.ts`: geprüfte Regeln (Unit-Tests in `test/vr.test.ts`).
