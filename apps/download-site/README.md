# LEGO Launcher – Download-Website

Statische Seite (kein Build-Schritt), die den neuesten Windows-Installer aus den
GitHub-Releases verlinkt. Version, Größe und – falls GitHub einen Asset-Digest
liefert – die SHA-256-Prüfsumme werden live über die GitHub-API angezeigt.

- Konfiguration: `config.js` (`repo`, `website` für Feedback/Bugs/Dashboard-Links)
- Deployment: `.github/workflows/download-site.yml` (GitHub Pages)
- Lokal ansehen: `npx serve apps/download-site` oder `python3 -m http.server -d apps/download-site`

Die Screenshots in `assets/` stammen aus dem Electron-UI-Test
(`E2E_SCREENSHOTS=dir xvfb-run -a node apps/launcher/test/ui.e2e.mjs`).
