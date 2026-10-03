# Architektur

```
oyvey-ported/
├── lego-client/          Fabric-Mod „LEGO Client“ (Java 21, MC 1.21.11, eigener Gradle-Build)
├── apps/
│   ├── launcher/         LEGO Launcher (Electron + TypeScript, Windows-Installer via electron-builder)
│   ├── backend/          LEGO-Server (Node 22, node:http + node:sqlite, keine Laufzeit-Abhängigkeiten)
│   └── website/          Website + Entwickler-Dashboard (statisch, vom Backend ausgeliefert)
├── packages/shared/      Gemeinsame Daten & Regeln: Themes, Name Tags, Katalog, Seltenheiten,
│                         Battle Pass, Quests, Produkte, Rollen/Rechte
├── legacy/               Original-JAR legoclient 3.4.0 (Referenz)
├── tools/                Restore-/Export-Skripte
├── src/ (root)           Ursprüngliche OyVey-Basis (unverändert, wird nicht ausgeliefert)
└── docs/
```

## Datenfluss

```
           Microsoft (offizieller Login)          Mojang Session Server
                 │ Device Code                         ▲        │ hasJoined
                 ▼                                     │ join   ▼
┌──────────────────────────┐   nur Mojang sieht   ┌────────────────────────┐
│ LEGO Launcher (Electron) │── das MC-Token ──────│ LEGO Backend (Node)    │
│  main: Tokens (DPAPI),   │                      │  SQLite: Konten, Credits│
│  Downloads, Spielstart   │◄── LEGO-Token ───────│  Freunde, Shop, Pass…  │
│  renderer: UI (kein Node)│   (Bearer, gehasht   │  SSE-Events            │
└────────────┬─────────────┘    gespeichert)      └──────────┬─────────────┘
             │ startet java mit -Dlegoclient.api=…           │ /api/public/cosmetics/lookup
             ▼                                               │
┌──────────────────────────┐                                 │
│ Minecraft + LEGO Client  │─────────────────────────────────┘
│  rendert eigene und      │   Website (Browser): Login per Einmal-Code aus dem
│  fremde LEGO-Cosmetics   │   Launcher → HttpOnly-Cookie + CSRF-Token
└──────────────────────────┘
```

## Sicherheitsentscheidungen

| Thema | Umsetzung |
|---|---|
| Minecraft-Login | Offizieller Microsoft Device-Code-Flow (`consumers`, `XboxLive.signin offline_access`). Kein eigenes Passwortfeld. |
| Tokens im Launcher | Electron `safeStorage` (Windows: DPAPI). Ohne OS-Verschlüsselung nur im Arbeitsspeicher. Renderer hat keinen Zugriff auf Tokens; alle API-Aufrufe laufen über den Main-Prozess. |
| Identität beim LEGO-Server | Mojang-Join/hasJoined (wie ein Minecraft-Server). Der LEGO-Server bekommt das Minecraft-Token nie. |
| Sitzungen | Zufällige 256-Bit-Tokens, in der DB nur als SHA-256. Website: HttpOnly, SameSite=Strict, Secure (konfigurierbar), CSRF-Header + Origin-Prüfung. |
| Credits | Ledger mit `balance_after`, Transaktion pro Buchung, `CHECK (credits >= 0)`, eindeutiger Idempotenzschlüssel gegen Doppelbuchungen, Audit-Log für jede Admin-Aktion. |
| Admin | Rollen `admin/developer/moderator/support`, Rechte serverseitig geprüft. Große Credit-Änderungen nur mit Bestätigung. Erster Admin nur per CLI auf dem Server. |
| Zahlungen | Stripe Checkout (gehostet), Webhook mit HMAC-Signatur + Zeitfenster, Event-ID einmalig → keine doppelten Gutschriften. Keine Kartendaten auf dem Server. |
| Uploads | Inhaltsprüfung (PNG/JPEG-Signatur oder UTF-8-Text), Größenlimits, Download nur als Anhang mit `sandbox`-CSP. |
| Website | Strikte CSP (`script-src 'self'`, keine Inline-Skripte/-Styles), alles HTML-escaped. |
| Downloads im Launcher | Nur HTTPS, SHA-1/SHA-512-Prüfung vor dem Umbenennen, ZIP-Slip-Schutz beim Entpacken, nur `-X/-XX/-D` als eigene JVM-Argumente. |
| Updates | electron-updater, SHA-512 aus `latest.yml`, Authenticode-Signatur sobald ein Zertifikat hinterlegt ist; Download standardmäßig erst nach Zustimmung. |
| Rate Limits | Pro Route und Nutzer/IP (In-Memory, ein Prozess). |

## Warum diese Technologie

- **Electron** für den Launcher: der Client nutzt bereits Java, aber ein Launcher braucht eine moderne UI, sichere Token-Speicherung (DPAPI) und einen erprobten Windows-Installer/Updater. Electron liefert alle drei; der Main-Prozess ist strikt vom UI getrennt.
- **Backend ohne Framework:** `node:http` + `node:sqlite` (Node ≥ 22.18) → keine nativen Abhängigkeiten, leicht zu prüfen. SQLite im WAL-Modus reicht für eine Instanz; für mehrere Instanzen müssten Rate-Limiter und SSE-Hub in Redis o. Ä. wandern und die Datenbank auf PostgreSQL umziehen (Schema ist portabel gehalten).
- **Shared-Paket:** Themes, Name Tags, Katalog, Preise und Rechte existieren genau einmal; Backend, Launcher und Website importieren dieselben Daten. Der Client bekommt die Name-Tag-Stile als exportiertes JSON.
