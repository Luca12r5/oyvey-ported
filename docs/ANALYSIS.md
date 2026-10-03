# Bestandsaufnahme (Phase 1)

Stand: 2026-10-03. Analysiert wurden das Repository `oyvey-ported` und die hochgeladene Datei
`legoclient-3.4.0+1.21.11.jar` (jetzt unter `legacy/`).

## 1. Was vorhanden war

### 1.1 Repository `oyvey-ported` (Wurzelverzeichnis)

| Punkt | Befund |
|---|---|
| Art | Fabric-Client-Basis „OyVey“ (Port eines 1.12.2-Clients), Minecraft 1.21.11, Mojang-Mappings, Loom 1.14 |
| Umfang | ~130 Java-Dateien, 14 Module, Click-GUI, HUD-Editor, Befehlssystem, Event-Bus |
| Build | `./gradlew build` – in GitHub Actions grün. Lokal in dieser Sitzung nicht baubar, weil `maven.fabricmc.net` von der Netzwerk-Policy gesperrt ist (kein Fehler im Repo). |
| Module | Watermark, Coordinates, HudEditor, ClickGui, Notifications, **Criticals, Step, ReverseStep, FastPlace, Velocity, NoFall, KeyPearl**, BlockHighlight, MCF |
| Problem 1 | Die markierten Module sind Spielvorteile (Anti-Knockback, kein Fallschaden, Kritische Treffer erzwingen …). Sie verstoßen auf fast allen Servern gegen die Regeln. Laut Auftrag („keine versteckten Cheats“) werden sie **nicht** in den LEGO Client übernommen. Der OyVey-Code bleibt unverändert im Repo (nichts gelöscht), wird aber nicht vom Launcher installiert. |
| Problem 2 | `src/main/java/me/alpha432/oyvey/features/modules/render/block esp` ist eine Datei **ohne `.java`-Endung** mit Yarn-Namen (anderes Mapping) und fremdem Paket – wird nie kompiliert. Inhalt: ein Chest-ESP (Truhen durch Wände sehen) = Spielvorteil. Nicht übernommen. |

### 1.2 `legoclient-3.4.0+1.21.11.jar` (dein eigentlicher LEGO Client)

Nur kompilierte Klassen, kein Quellcode. ~500 Klassen in `dev.lego` und `dev.spotifyhud`, Fabric, MC 1.21.11.

| Bereich | Inhalt (verifiziert im Code) |
|---|---|
| Module | ~85 Module: 24 HUD, 34 Visuals (Custom Sky, Filter, Partikel-Effekte, Crosshair, Zoom, Fullbright …), 20 Utility (Toggle Sprint/Sneak, Freelook, Auto-GG, Wegpunkte, Warnungen, Makros, Anti-Spam …), 5 Performance, Spotify, Emotes |
| Cosmetics | **211** registrierte 3D-Cosmetics (44 Capes, 40 Pets, 37 Hüte, 22 Auren, 22 Rücken, 20 Flügel, 13 Gesicht, 13 Fahrzeuge), Seltenheiten COMMON/RARE/EPIC/LEGENDARY, prozedurale Modelle/Texturen |
| Emotes | 38 Emotes inkl. Cutscenes, Emote-Rad |
| Minispiele | 15+ Bildschirmspiele (Tetris, Snake, 2048, Asteroids, Breakout, Sudoku, Minesweeper, Pong, Flappy, Simon, Memory, Reaktion, Aim-Trainer, CPS-Test, Solar Smash, Weihnachtsspiele …) |
| UI | Eigenes Menü (Rechts-Shift), HUD-Editor, Weihnachts-Theme, Spotify-HUD mit Lyrics |
| Netzwerk | Nur Spotify-API und lrclib.net. **Keine Verbindung zu einem LEGO-Server.** |

**Befunde / Fehler im JAR:**

1. **Cosmetics sind rein lokal.** `CosRender.Feature` rendert nur, wenn die Entity der lokale Spieler ist. Andere Spieler sehen deine Cosmetics nie – auch keine anderen LEGO-Spieler.
2. **Alle Cosmetics sind gratis und ohne Besitzprüfung.** Es gibt kein Konto, keine Freischaltung.
3. **„TV-Browser“ fehlt.** `fabric.mod.json` und der Sprachschlüssel `key.legoclient.tv` versprechen einen Browser über Edge/Chrome; im JAR existieren aber nur **leere** Pakete `dev/lego/tv/cdp` und `dev/lego/tv/mc`. Die Funktion war nie enthalten.
4. **Eingebettetes `vex-debris-finder-1.0.0.jar`.** Scannt Chunks nach Antiker-Schutt-Hotspots → unfairer Vorteil (X-Ray-ähnlich). **Nicht übernommen.**
5. **Mixins zielten auf Intermediary-Namen mit `remap = false`.** Funktioniert nur im Produktions-JAR, nicht in einer Entwicklungsumgebung. Umgestellt (siehe unten).
6. **Performance:** `CosRender.draw` baut die gesamte Cosmetic-Geometrie alle 33 ms neu und glättet Normalen für bis zu 20 000 Quads (viele Allokationen pro Frame). Für einen Spieler okay, für viele teuer – deshalb rendert der neue Remote-Pfad nur die nächsten N LEGO-Spieler (Standard 24) im Umkreis von 64 Blöcken.
7. Mehrere Module nutzen Reflection mit festen Intermediary-Namen; jetzt über Fabrics `MappingResolver` (`dev.lego.util.Remap`).

## 2. Was in dieser Sitzung daraus gemacht wurde

1. **Quellcode wiederhergestellt:** CI-Workflow `restore-lego-sources.yml` remappt das JAR mit Loom auf Mojang-Namen und dekompiliert es mit Vineflower. Ergebnis unter `lego-client/` als eigener Gradle-Build. 60 Dekompilierungsfehler behoben; **der Client baut in GitHub Actions** (`lego-client.yml`).
2. Mixins auf Mojang-Namen umgestellt (Namen aus Looms Mappings nachgeschlagen, `tools/restore-lego/lookup.sh`); der Mixin-Annotationsprozessor meldet keine unauflösbaren Ziele.
3. Neues Backend, Website, Launcher, Shared-Katalog – siehe `docs/ARCHITECTURE.md` und `docs/ROADMAP.md`.

## 3. Grenzen dieser Umgebung (ehrlich)

- Mojang-, Fabric- und Modrinth-Server sind von hier aus gesperrt. Minecraft konnte daher **nicht gestartet** werden; die Client-Mod ist kompiliert, aber nicht im Spiel getestet. Die Installations- und Startlogik des Launchers ist mit Unit-Tests gegen echte Datenformate geprüft, aber nicht gegen die echten Server ausgeführt.
- Microsoft-Login kann ohne eine bei Mojang freigeschaltete Azure-App-ID nicht end-to-end getestet werden (siehe `docs/LAUNCHER.md`).
- Die Windows-EXE wird in GitHub Actions auf `windows-latest` gebaut (`launcher.yml`), nicht lokal.
