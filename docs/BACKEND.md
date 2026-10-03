# LEGO Backend & Website – Betrieb

## Starten

```bash
npm ci
cp apps/backend/.env.example .env   # Werte anpassen, dann z. B. mit `set -a; . ./.env; set +a`
npm run backend                      # http://127.0.0.1:8787 (API + Website)
npm test -w apps/backend             # 18 Integrationstests
npm run e2e                          # Website im echten Chromium gegen ein frisches Backend
```

Node ≥ 22.18 (nutzt `node:sqlite` und TypeScript-Type-Stripping). Keine Laufzeit-Abhängigkeiten.

## Hinter einem Reverse-Proxy (empfohlen)

TLS beendet ein Proxy (Caddy, nginx). Beispiel Caddy:

```
api.dein-lego.de {
  reverse_proxy 127.0.0.1:8787
}
```

Dazu `LEGO_TRUST_PROXY=1`, `LEGO_PUBLIC_ORIGIN=https://api.dein-lego.de`, `LEGO_SECURE_COOKIES=1`.

## Ersten Admin anlegen

1. Mit dem Launcher einmal anmelden (legt das Konto an).
2. Auf dem Server: `npm run cli -w apps/backend -- grant-role <MinecraftName> admin`
3. Im Launcher „Konto → Website-Anmeldung“ → Code auf der Website eingeben → „Dashboard“.

Weitere Rollen vergibt ein Admin im Dashboard. Der letzte Admin kann nicht entfernt werden.

## Credits vergeben

Dashboard → Spieler → Spieler öffnen → „Credits“: Betrag (negativ = abziehen, nur Admins), Pflicht-Grund. Ab 5 000 Credits muss der Betrag zur Bestätigung eingetippt werden. Jede Buchung landet im Ledger und im Audit-Log (wer, wann, IP, Grund). Doppelklicks buchen dank Idempotenzschlüssel nicht doppelt. Credits existieren nur auf dem Server; Client und Website können sie nicht verändern.

## Client-Download für den Launcher

Den gebauten Mod (`lego-client/build/libs/legoclient-<version>.jar`, Artefakt des Workflows „LEGO client“) als GitHub-Release-Asset hochladen und eintragen:

```
LEGO_CLIENT_URL=https://github.com/<owner>/<repo>/releases/download/client-v4.0.0/legoclient-4.0.0.jar
LEGO_CLIENT_VERSION=4.0.0
LEGO_CLIENT_SHA512=<sha512sum der Datei>
```

Ohne Hash liefert `/api/public/client-release` nichts aus – der Launcher installiert nur geprüfte Dateien.

## Zahlungen (optional)

Stripe-Konto anlegen, `STRIPE_SECRET_KEY` und `STRIPE_WEBHOOK_SECRET` setzen, Webhook-Endpunkt `https://<host>/api/payments/webhook` mit den Events `checkout.session.completed` und `invoice.paid` einrichten. Ohne Konfiguration sind Echtgeld-Käufe deaktiviert, der Credit-Shop funktioniert trotzdem.

**Vor dem Livegang rechtlich prüfen lassen:** Widerrufsrecht bei digitalen Inhalten, Preisangaben, Jugendschutz/Elternzustimmung bei Minderjährigen, Steuern. Platzhalter für Impressum, Datenschutz und AGB stehen unter `/legal/*`.

## Backups

- Dashboard → System → „Backup erstellen“ oder `npm run cli -w apps/backend -- backup`. Schreibt eine konsistente Kopie (`VACUUM INTO`) nach `<LEGO_DATA_DIR>/backups/`.
- Empfohlen: täglich per Cron + Kopie außer Haus. Wiederherstellen: Server stoppen, Backup als `lego.sqlite` einsetzen, starten.

## Skalierung

Eine Instanz mit SQLite (WAL) trägt einige tausend gleichzeitige Nutzer. Für mehrere Instanzen müssten Rate-Limiter, Präsenz und SSE-Hub in einen gemeinsamen Speicher (z. B. Redis) und die Daten nach PostgreSQL; die SQL-Migrationen sind bewusst einfach gehalten.

## API-Überblick

| Bereich | Endpunkte |
|---|---|
| Auth | `POST /api/auth/challenge`, `/verify`, `/web-code`, `/web-login`, `/logout`, `/logout-all`, `GET /api/auth/session`, `/sessions` |
| Konto | `GET/PATCH /api/me`, `PUT /api/me/privacy`, `GET /api/me/credits`, `/stats`, `POST /api/me/presence`, `/metrics`, `/game-results`, `GET/PUT /api/me/settings`, `PUT /api/me/tag-text` |
| Social | `/api/friends…`, `/api/blocks…`, `POST /api/reports`, `/api/messages…`, `/api/party…`, `/api/emotes/invite|respond`, `GET /api/events` (SSE) |
| Shop | `GET /api/inventory`, `PUT /api/equip`, `POST /api/shop/buy`, `/shop/gift`, `/payments/checkout`, `/payments/webhook` |
| Fortschritt | `GET /api/progress`, `POST /api/daily/claim`, `/quests/:id/claim`, `/pass/claim`, `/promo/redeem` |
| Feedback | `/api/feedback…` inkl. Votes, Kommentare, Status, Anhänge |
| Admin | `/api/admin/overview|users|credits|items|roles|ban|audit|reports|news|promo|backup|maintenance` |
| Öffentlich | `/api/public/status|catalog|themes|progression|news|profiles/:name|leaderboard/:game|cosmetics/lookup|client-release` |
