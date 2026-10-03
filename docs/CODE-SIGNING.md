# Code-Signatur, SmartScreen und Antivirus

## Warum ein Installer als „Virus“ gemeldet werden kann

Windows SmartScreen und viele Virenscanner bewerten neue Programme nach
**Reputation**. Ein unsignierter Installer, den noch kaum jemand heruntergeladen
hat, bekommt deshalb oft eine Warnung („Der Computer wurde durch Windows
geschützt“) oder eine heuristische Meldung – auch wenn er harmlos ist.

Was der LEGO Launcher tut, um nicht verdächtig zu wirken:

- keine Admin-Rechte (NSIS per Benutzer, `allowElevation: false`)
- keine Registry-Tweaks, keine Dienste, kein Autostart, keine versteckten Prozesse
- keine gepackten/verschleierten Binärdateien; Quellcode öffentlich
- Downloads nur über HTTPS, jede Datei per SHA-1/SHA-512 geprüft
- Java wird von Mojang geladen, nicht mitgeliefert

**Was wirklich hilft, ist eine Code-Signatur.** Ohne sie kann niemand
garantieren, dass es keine Warnungen gibt. Mit Signatur verschwinden die
Warnungen meist sofort (EV/Azure Trusted Signing) oder nach etwas Reputation.

## Option A – Azure Trusted Signing (empfohlen, günstig)

1. Azure-Konto anlegen, Ressource **Trusted Signing Account** erstellen,
   Identitätsprüfung durchführen, ein **Certificate Profile** (Public Trust) anlegen.
2. App-Registrierung (Service Principal) mit der Rolle
   *Trusted Signing Certificate Profile Signer* auf dem Konto.
3. In GitHub → Settings → Secrets and variables → Actions:
   - Secrets: `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`
   - Variables: `AZURE_SIGN_ENDPOINT` (z. B. `https://weu.codesigning.azure.net`),
     `AZURE_SIGN_ACCOUNT`, `AZURE_SIGN_PROFILE`, `SIGN_PUBLISHER`
     (muss exakt dem Namen im Zertifikat entsprechen)
4. Der Workflow `.github/workflows/launcher.yml` signiert dann automatisch EXE,
   Installer und Uninstaller über electron-builder (`win.azureSignOptions`).

## Option B – klassisches Authenticode-Zertifikat (OV/EV)

- Secrets `CSC_LINK` (Base64 der .pfx oder HTTPS-URL) und `CSC_KEY_PASSWORD`,
  Variable `SIGN_PUBLISHER`.
- EV-Zertifikate liegen heute meist auf Hardware-Token/HSM und lassen sich in
  gehosteten Runnern nur über einen Cloud-HSM-Anbieter nutzen.

## Updates

electron-updater prüft bei signierten Builds, dass ein Update vom selben
Herausgeber (`SIGN_PUBLISHER`) stammt. Unsignierte Builds aktualisieren sich
weiterhin, aber ohne diese Prüfung (die Update-Datei wird per SHA-512 aus
`latest.yml` geprüft).

## Veröffentlichen

```bash
# Version in apps/launcher/package.json erhöhen, committen, dann:
git tag launcher-v0.2.0 && git push origin launcher-v0.2.0
```

Der Workflow prüft, dass Tag und `package.json` übereinstimmen, baut, signiert
(falls konfiguriert) und lädt Installer, Blockmap und `latest.yml` in ein
GitHub-Release. Die Download-Seite (`apps/download-site`) zeigt automatisch die
neueste Version und – wenn GitHub einen Asset-Digest liefert – die SHA-256.

## Fehlalarm melden

Falls ein Scanner trotz Signatur anschlägt: Datei bei den Herstellern als
False Positive einreichen (z. B. Microsoft: https://www.microsoft.com/wdsi/filesubmission).
