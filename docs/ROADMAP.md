# Roadmap & Status

Legende: ✅ umgesetzt und getestet · 🟡 teilweise / ungetestet im echten Spiel · ⬜ offen.
„Getestet“ heißt: automatisierter Test oder CI-Build ist tatsächlich gelaufen. Nichts hier ist geschätzt.

## Stand nach Sitzung 1 (2026-10-03)

| # | Bereich (Auftrag) | Status | Was konkret existiert / fehlt |
|---|---|---|---|
| 1 | Analyse | ✅ | `docs/ANALYSIS.md` |
| 2 | Eigenständiger Launcher als Produkt | 🟡 | Launcher, Backend, Website, Client, DB sind verbunden. Produktiv-Deployment, Domain, Azure-Freigabe fehlen (nur durch dich machbar). |
| 3 | Modernes Launcher-Design | ✅ | Seitenleiste wie im Screenshot, 11 Seiten, Animationen, UI-Skalierung bis 200 % (4K), reduzierte Animationen. Electron-UI-Test läuft in CI. |
| 3 | Fehlende Seiten | ⬜ | Eigene Seiten „Modules“ und „Games“ im Launcher (Module/Spiele leben im Client-Menü), Support als eigene Seite (Feedback deckt es ab), Statistik als eigene Seite (im Konto). |
| 4 | ≥ 100 Themes + Editor | ✅ | 101 unterschiedliche Themes (17 Stile, Test prüft Eindeutigkeit + WCAG-Kontrast), Editor mit Farben/Rundung/Glas/Transparenz/Rahmen/Schatten/Schrift/Animation/Hintergrund/eigenem Bild, Import/Export (validiert, keine Remote-URLs). Website nutzt dieselben Themes. |
| 5 | Modulsystem (Ziel 500) | 🟡 | ~87 echte Module im Client (85 aus 3.4.0 + LEGO-Netzwerk + Tacho) mit Einstellungen, Keybinds, Reset, Speicherung. **Weit unter 500** – bewusst keine Fake-Module. Favoriten/Suche: Suche vorhanden im Client-Menü (aus 3.4.0), Favoriten ⬜. |
| 6 | Cosmetics (Ziel 300) | ✅ | 311 echte Items: 211 3D-Cosmetics aus dem Client + 100 Name-Tag-Stile. Besitz, Kauf, Ausrüsten serverseitig geprüft. |
| 6 | 3D-Vorschau mit Rotation/Zoom | 🟡 | Im Client vorhanden (3.4.0). Im Launcher/Website nur Symbol-Vorschau (Modelle sind Java-Code, nicht exportiert) ⬜. |
| 6.1 | Capes für andere LEGO-Spieler sichtbar | 🟡 | Implementiert (`LegoNet` + Cape-Mixin), kompiliert in CI, **im Spiel noch nicht getestet**. Auf jedem Server/Spielmodus inkl. NoRisk-Servern, solange beide den LEGO Client nutzen. NoRisk-Client-Nutzer sehen sie nicht. |
| 6.2 | Pets mit Interaktionen | ⬜ | 40 Pets mit Modellen/Animationen existieren (3.4.0). Rufen/Streicheln/Apportieren/Tricks/Level ⬜; Multiplayer-Sync ⬜. |
| 6.3 | Duo-/Trio-Emotes | 🟡 | Backend-Einladung/Antwort per SSE ✅ (Freunde-only). Client-Synchronisation der Animation ⬜. |
| 6.4 | 100 Name Tags | ✅/🟡 | 100 Stile, Moderation für eigenen Text (LEGO+), Vorschau in Launcher/Website ✅. Im Spiel für andere LEGO-Spieler (Farben/Animation/Icon) implementiert 🟡 (ungetestet). Rahmen nur in Launcher/Website. |
| 6.5 | 100 Fahrzeuge | ⬜ | 13 kosmetische Fahrzeuge (3.4.0, Modell folgt dem Spieler). Echte Fahrphysik/Kollisionen/Garagen ⬜. |
| 7 | km/h-Tacho | ✅/🟡 | `Tacho`-HUD: reale Bewegung über echte Zeit, km/h/m/s/mph, Beschleunigung, Bremsen, 3 Designs, Position/Größe über HUD-Editor. Rechenkern per JUnit getestet; im Spiel ungetestet. |
| 8 | Item-Modelle & Animationen | ⬜ | Nicht begonnen (bestehende 3.4.0-Animationen unverändert). |
| 9 | Seltenheiten („Amore“) | ✅ | 7 Stufen mit Farben (konfigurierbar im Launcher), animierte Rahmen für Legendary+, Filter, Sammlungsfortschritt. Keine Zufallsbelohnungen → keine Wahrscheinlichkeiten nötig. |
| 10 | Freundes-/Social-System | ✅ | Anfragen, Status mit Datenschutz, Notizen, Blockieren, Melden, private Nachrichten, Partys mit Leiter/Kick/Chat, SSE-Echtzeit. Alles serverseitig, getestet. Einladung in gemeinsame Welt ⬜. |
| 11 | KI-Gegner 4 Stufen | ⬜ | |
| 12 | GTA-inspiriertes Spiel | ⬜ | Nicht begonnen. Wird als eigenes Projekt mit Prototyp-Stadt geplant. |
| 13 | Tennis | ⬜ | Rangliste „tennis“ im Backend vorbereitet, Spiel selbst fehlt. |
| 14 | 100 Minigames | 🟡 | 15+ Spiele aus 3.4.0. Ergebnis-Meldung ans Backend ⬜ (Endpunkt existiert). |
| 15 | Solar-Smash-artiger Modus | 🟡 | `SolarSmash` existiert im Client (3.4.0). Speichern/Laden von Szenarien ⬜. |
| 16 | Auto-Text | 🟡 | Makros (F6–F9, Numpad) + Anti-Spam + Wortfilter aus 3.4.0. Kategorien, Profile, Import/Export ⬜. |
| 17 | FPS-Boost | 🟡 | 3.4.0: Entity-Culling, Partikel-Limit, Hintergrund-FPS, Block-Entity-Culling, Leistungsmonitor. Profile Potato…Quality, Frametime-Diagramm, Messprotokoll vorher/nachher ⬜. **Keine Messung durchgeführt** (Spiel hier nicht startbar). |
| 18 | Ping-/Netzwerkdiagnose | 🟡 | Ping-HUD aus 3.4.0. Verlauf/Jitter/Diagramm ⬜. |
| 19 | Credits + Entwickler-Website | ✅ | Ledger, Tagesbelohnung, Quests, Battle Pass, Aktionscodes, Admin-Vergabe mit Grund/Bestätigung/Audit, Missbrauchs-Hinweis im Dashboard. Getestet (Backend + Chromium-E2E). |
| 20 | Shop, LEGO Pass, LEGO+, LEGO++, Battle Pass | ✅/🟡 | Credit-Shop, Geschenke, Battle Pass (50 Stufen, frei/premium), Quests ✅. Stripe Checkout + signierte Webhooks implementiert und mit synthetischen Events getestet; **nicht gegen echtes Stripe getestet**. Rechtliche Prüfung ⬜. |
| 21 | Microsoft-Login | 🟡 | Vollständig implementiert und mit gemockten Endpunkten getestet. Braucht eine von Mojang freigeschaltete Azure-App-ID (`docs/LAUNCHER.md`). |
| 22 | Versionen/Profile/Kompatibilität | ✅/🟡 | Versionsauswahl, Profile (RAM, JVM-Args, Auflösung, eigener Ordner), Fabric, Mods-Ordner, Crash-Hinweise. Resource-Pack-/Shader-Manager, Backups ⬜. NoRisk-Kompatibilität: Cosmetics sind client-seitig, kein Eingriff in fremde Clients. |
| 23 | Website | ✅ | Alle geforderten Seiten außer eigener Bug-Report-/Feature-Request-Unterseiten (im Feedback-Board zusammengefasst). Rechtstexte sind Platzhalter. |
| 24 | Windows-EXE + Updater | ✅/🟡 | NSIS-Installer wird in CI gebaut (`LEGO-Launcher-Setup-0.1.0.exe`), electron-updater mit SHA-512. Unsigniert, solange kein Zertifikat hinterlegt ist. Reparaturfunktion ⬜. |
| 25 | Backend & DB | ✅ | Siehe `docs/BACKEND.md`. |
| 26 | 50+ weitere Ideen | 🟡 | Umgesetzt: HUD-Editor, Keystrokes, CPS, FPS, Crosshair-Editor (3.4.0), Achievements, tägliche/wöchentliche Quests, Login-Streaks, Daily Rewards, Party-Chat, Freunde-Benachrichtigungen, Theme-Sharing (Export/Import), Cloud-Sync-Endpunkt, Feedback-Voting, Wartungsmodus, Statusseite, Meldesystem, Blockierliste, Datenschutz, UI-Skalierung, reduzierte Animationen, Patch-Notes/News im Launcher, Ranglisten, Benachrichtigungen. Rest ⬜. |
| 27 | Tests & Qualität | ✅ | 26 Backend-/Shared-Tests, 13 Launcher-Tests, 6 JUnit-Tests, Chromium-E2E, Electron-E2E, alle in CI. |

## Nächste Schritte (Vorschlag, in dieser Reihenfolge)

1. **Im echten Spiel testen**: Client-JAR aus CI + Fabric API in einer 1.21.11-Instanz starten, Remote-Cosmetics mit zwei Konten prüfen, Tacho kalibrieren.
2. **Smoke-Test in CI**: Minecraft mit Loom `runClient` unter Xvfb starten und prüfen, dass alle Mixins greifen.
3. Azure-App beantragen, Backend deployen, `LEGO_CLIENT_*` setzen, erstes Release.
4. Client-Seitig: Minigame-Ergebnisse und Emote-Nutzung ans Backend melden; Duo-Emote-Synchronisation; eigene Cosmetics aus dem Backend statt lokal (Besitzprüfung im Spiel).
5. Tennis als erstes neues Spiel (Bildschirmspiel im Client, 4 KI-Stufen) – danach KI-Framework für weitere Spiele.
6. FPS-Profile + Frametime-Graph + reproduzierbares Messprotokoll.
7. Launcher: Skin-3D-Vorschau (skinview3d), Modules-/Games-Seiten, Resource-Pack-Manager.
8. TV-Browser (Chrome DevTools Protocol mit dem installierten Edge/Chrome) – in 3.4.0 angekündigt, aber nie enthalten.
9. GTA-artiges Spiel und Fahrzeug-Physik als eigenes Teilprojekt.
