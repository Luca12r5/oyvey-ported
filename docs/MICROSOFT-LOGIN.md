# Microsoft-Anmeldung freischalten (einmalig, ca. 15 Minuten + Wartezeit)

Ohne diese Schritte kann sich niemand im LEGO Launcher anmelden. Das kann nur
der Betreiber (du) machen: Microsoft und Mojang verlangen dafür ein eigenes,
geprüftes Konto. Fremde App-IDs (z. B. von anderen Launchern) zu benutzen ist
nicht erlaubt und würde jederzeit gesperrt werden.

## 1. App im Azure-Portal registrieren (kostenlos)

1. Auf <https://portal.azure.com> mit einem Microsoft-Konto anmelden.
2. Oben suchen: **„App-Registrierungen“** → **„Neue Registrierung“**.
3. Name: `LEGO Launcher`.
4. Unterstützte Kontotypen: **„Nur persönliche Microsoft-Konten“**.
5. Umleitungs-URI: leer lassen → **Registrieren**.
6. Auf der Übersichtsseite die **„Anwendungs-ID (Client)“** kopieren
   (Format `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).
7. Links **„Authentifizierung“** → ganz unten **„Öffentliche Clientflows
   zulassen“** auf **Ja** → Speichern. (Der Launcher nutzt den Device-Code-Flow,
   ein Client-Geheimnis ist nicht nötig.)

## 2. Freischaltung für die Minecraft-API beantragen

Neue Azure-Apps dürfen die Minecraft-API erst nach Prüfung durch Mojang nutzen.
Antrag: <https://aka.ms/mce-reviewappid> – dort die Client-ID aus Schritt 1,
den Namen „LEGO Launcher“ und eine kurze Beschreibung („Minecraft-Launcher für
den eigenen LEGO Client, Microsoft-Login per Device Code“) angeben.

Bis zur Freischaltung meldet der Launcher beim Login:
*„Minecraft services rejected this launcher build. The Azure app id is not approved …“*
– das ist dann kein Fehler im Launcher, sondern die noch fehlende Freigabe.

## 3. Client-ID eintragen

Zwei Möglichkeiten, beide gleichwertig:

- **Sofort, ohne neuen Build:** Launcher → Einstellungen → Erweitert →
  „Microsoft-Client-ID“ einfügen.
- **Fest eingebaut (für alle Nutzer):** GitHub → Repository → Settings →
  Secrets and variables → Actions → *New repository secret*
  `LEGO_MS_CLIENT_ID` = die Client-ID. Jeder neue Build (Workflow
  „LEGO Launcher (Windows)“) enthält sie dann.

Die Client-ID ist kein Passwort; sie darf im Programm stehen.

## 4. Testen

Launcher starten → oben rechts **Anmelden** → im Browser öffnet sich
`microsoft.com/link`, den angezeigten Code eingeben, mit dem Konto anmelden,
das Minecraft: Java Edition besitzt. Danach erscheint dein Name und Skin oben
rechts, und **SPIELEN** startet das Spiel.

## Was der Launcher dabei macht

Device-Code → Xbox Live → XSTS (`rp://api.minecraftservices.com/`) →
`login_with_xbox` → Lizenzprüfung → Profil. Das Microsoft-Refresh-Token wird mit
Windows-DPAPI verschlüsselt gespeichert; dein Passwort sieht der Launcher nie.
Der LEGO-Server bekommt das Minecraft-Token nie (Anmeldung dort über den
Mojang-Join-Handshake).
