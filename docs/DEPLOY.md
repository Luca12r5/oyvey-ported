# Online stellen – Schritt für Schritt

Drei Teile gehen online:

| Teil | Wo | Kosten |
|---|---|---|
| **LEGO-Server + Website** (Konto, Shop, Freunde, Feedback/Bugs, Entwickler-Dashboard) | Docker-Host, z. B. Render oder Fly.io | ab ca. 5–7 €/Monat (persistente Festplatte nötig) |
| **Download-Seite** | GitHub Pages | kostenlos |
| **Launcher-Installer** | GitHub Releases | kostenlos |

Der Launcher funktioniert auch ohne LEGO-Server: Minecraft starten, Mods,
Modpacks und der LEGO Client (im Installer enthalten) gehen immer. Der Server
wird für Coins, Shop, Freunde, Chat, Cosmetics anderer Spieler und Feedback gebraucht.

## 1. LEGO-Server + Website

### Variante A: Render (am einfachsten)

1. Konto auf <https://render.com> anlegen und GitHub verbinden.
2. **New → Blueprint** → dieses Repository wählen. Render liest `render.yaml`.
3. Bei `LEGO_PUBLIC_ORIGIN` die spätere Adresse eintragen, z. B.
   `https://lego-server.onrender.com` (oder deine eigene Domain).
4. **Apply**. Nach dem Build ist die Website unter dieser Adresse erreichbar.
5. Eigene Domain (optional): Service → Settings → Custom Domains.

### Variante B: Fly.io

```bash
fly launch --no-deploy          # übernimmt fly.toml
fly volumes create lego_data --size 1
fly secrets set LEGO_PUBLIC_ORIGIN=https://<app>.fly.dev
fly deploy
```

### Variante C: eigener Server (VPS) mit Docker

```bash
docker build -t lego-server .
docker run -d --restart unless-stopped --name lego -p 127.0.0.1:8787:8787 \
  -v lego-data:/data -e LEGO_PUBLIC_ORIGIN=https://deine-domain.de lego-server
```

Davor einen Reverse-Proxy mit HTTPS (z. B. Caddy: `deine-domain.de { reverse_proxy 127.0.0.1:8787 }`).

### Dich selbst zum Admin machen (für das Entwickler-Dashboard)

1. Einmal im Launcher anmelden (damit dein Konto auf dem Server existiert).
2. Auf dem Server:
   - Render: Service → **Shell** → `node apps/backend/src/cli.ts grant-role DEINNAME admin`
   - Fly: `fly ssh console -C "node apps/backend/src/cli.ts grant-role DEINNAME admin"`
   - Docker: `docker exec lego node apps/backend/src/cli.ts grant-role DEINNAME admin`
3. Website neu laden → oben erscheint **Dashboard**: Coins geben/abziehen,
   Gegenstände vergeben, Rollen, Sperren, News, Aktionscodes, Audit-Log.

Backup: `node apps/backend/src/cli.ts backup` (schreibt nach `/data/backups/`).

## 2. Launcher mit dem Server verbinden

GitHub → Repository → Settings → Secrets and variables → Actions → **Variables**:
`LEGO_BACKEND_URL` = die Adresse aus Schritt 1. Jeder neue Launcher-Build
verbindet sich dann automatisch. (Nutzer können sie auch unter
Einstellungen → Erweitert eintragen.)

Microsoft-Login: siehe [MICROSOFT-LOGIN.md](MICROSOFT-LOGIN.md).

## 3. Download-Seite (GitHub Pages)

1. Den Branch in `main` mergen (der Pages-Workflow läuft nur dort).
2. GitHub → Settings → **Pages** → Source: **GitHub Actions**.
3. In `apps/download-site/config.js` bei `website` die Adresse aus Schritt 1
   eintragen (für die Links zu Feedback/Bugs und Dashboard).
4. Workflow „Download site (GitHub Pages)“ läuft automatisch; die Adresse steht
   danach unter Settings → Pages (z. B. `https://luca12r5.github.io/oyvey-ported/`).

## 4. Launcher veröffentlichen

```bash
# Version in apps/launcher/package.json setzen, committen, dann:
git tag launcher-v0.2.0
git push origin launcher-v0.2.0
```

Der Workflow baut den Installer (mit eingebautem LEGO Client), signiert ihn,
falls ein Zertifikat hinterlegt ist ([CODE-SIGNING.md](CODE-SIGNING.md)), und
legt ihn als GitHub-Release ab. Die Download-Seite zeigt ihn sofort an.
