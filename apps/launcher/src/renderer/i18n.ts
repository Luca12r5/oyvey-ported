// Launcher translations (German default, English). Pages that are not yet
// translated stay German; keys missing in English fall back to German.

const de = {
  'nav.play': 'Spielen', 'nav.profiles': 'Profile', 'nav.mods': 'Mods', 'nav.cosmetics': 'Cosmetics', 'nav.shop': 'Shop', 'nav.friends': 'Freunde',
  'nav.rewards': 'Belohnungen', 'nav.news': 'News', 'nav.feedback': 'Feedback', 'nav.account': 'Konto', 'nav.dev': 'Entwickler', 'nav.settings': 'Einstellungen',
  'acc.signin': 'Anmelden', 'acc.add': 'Konto hinzufügen', 'acc.signout': 'Abmelden', 'acc.manage': 'Konten verwalten', 'acc.offline': 'LEGO offline',
  'play.play': 'SPIELEN', 'play.running': 'LÄUFT', 'play.installing': 'INSTALLIERT', 'play.signin': 'Mit Microsoft anmelden', 'play.welcome': 'Willkommen zurück',
  'play.hello': 'Hallo', 'play.notSigned': 'Nicht angemeldet', 'play.profile': 'Profil wählen', 'play.newProfile': 'Neues Profil', 'play.console': 'Konsole',
  'play.cancel': 'Abbrechen', 'play.news': 'Neuigkeiten', 'play.coins': 'LEGO Coins', 'play.today': 'Heute verdient', 'play.noNews': 'Keine News.',
  'play.newsOffline': 'News nicht verfügbar (offline).', 'play.daily': 'Tagesziel', 'play.dailyHint': 'Coins gibt es für Spielzeit und LEGO-Minispiele.',
  'play.anim.idle': 'Stehen', 'play.anim.walk': 'Gehen', 'play.anim.wave': 'Winken', 'play.anim.spin': 'Drehen', 'play.vanilla': 'Vanilla',
  'play.noClientId': 'Dieser Build hat keine Microsoft-Client-ID. Siehe docs/LAUNCHER.md.', 'play.fun': 'Viel Spaß!', 'play.lastPlayed': 'Zuletzt gespielt',
  'set.title': 'Einstellungen', 'set.search': 'Einstellungen durchsuchen…', 'set.general': 'Allgemein', 'set.appearance': 'Darstellung', 'set.background': 'Hintergrund',
  'set.game': 'Spiel', 'set.themes': 'Designs', 'set.editor': 'Theme-Editor', 'set.rarity': 'Seltenheiten', 'set.accounts': 'Konten', 'set.advanced': 'Erweitert', 'set.about': 'Über',
  'set.language': 'Sprache', 'set.language.d': 'Sprache der Launcher-Oberfläche.', 'set.closeOnLaunch': 'Beim Spielstart minimieren', 'set.closeOnLaunch.d': 'Der Launcher geht in die Taskleiste, solange Minecraft läuft.',
  'set.updates': 'Updates', 'set.updates.d': 'Wie neue Launcher-Versionen installiert werden.', 'set.upd.ask': 'Nachfragen', 'set.upd.auto': 'Automatisch', 'set.upd.off': 'Nicht prüfen',
  'set.accent': 'Akzentfarbe', 'set.accent.d': 'Überschreibt die Farbe des Designs. Zurücksetzen nutzt wieder die Design-Farbe.', 'set.reset': 'Zurücksetzen', 'set.custom': 'Eigene',
  'set.scale': 'UI-Skalierung', 'set.scale.d': 'Für hohe Auflösungen bis 4K.', 'set.motion': 'Animationen reduzieren', 'set.motion.d': 'Hintergrund und Charakter bewegen sich nicht.',
  'set.bgPick': 'Animierter Hintergrund', 'set.bgPick.d': 'Wird live hinter dem Launcher gezeichnet.', 'set.snow': 'Schnee', 'set.snow.d': 'Schneeflocken über dem Hintergrund.',
  'set.quality': 'Qualität', 'set.quality.d': 'Niedriger = weniger GPU-Last, höher = schärfer und mehr Partikel.',
  'set.snapshots': 'Snapshots anzeigen', 'set.snapshots.d': 'Entwicklerversionen in der Versionsliste.', 'set.historical': 'Historische Versionen', 'set.historical.d': 'Alpha- und Beta-Versionen vor 1.0.',
  'set.cfKey': 'CurseForge-API-Schlüssel', 'set.cfKey.d': 'Nur nötig, um CurseForge-Modpacks vollständig zu importieren (console.curseforge.com).',
  'set.backend': 'LEGO-Server', 'set.backend.d': 'Nur https. Leer = Standard-Server dieses Builds.', 'set.gameDir': 'Spielordner', 'set.gameDir.d': 'Versionen, Bibliotheken, Assets und Profile.',
  'set.msClient': 'Microsoft-Client-ID', 'set.msClient.d': 'Azure-App-ID für die Anmeldung (von Mojang freigeschaltet). Leer = die im Launcher eingebaute ID. Anleitung: docs/MICROSOFT-LOGIN.md.',
  'set.open': 'Öffnen', 'set.save': 'Speichern', 'set.saved': 'Gespeichert', 'set.diag': 'Diagnose', 'set.diag.d': 'Systeminfos für Fehlerberichte kopieren.', 'set.copy': 'Kopieren',
  'mods.title': 'Mods & Inhalte', 'mods.installed': 'Installiert', 'mods.mods': 'Mods', 'mods.resourcepacks': 'Ressourcenpakete', 'mods.shaders': 'Shader', 'mods.modpacks': 'Modpacks',
  'mods.search': 'Auf Modrinth suchen…', 'mods.install': 'Installieren', 'mods.installedOk': 'Installiert', 'mods.remove': 'Entfernen', 'mods.empty': 'Noch keine Mods in diesem Profil.',
  'mods.vanilla': 'Dieses Profil ist Vanilla. Wähle im Profil einen Mod-Loader (Fabric, Quilt, Forge oder NeoForge), um Mods zu installieren.',
  'mods.managed': 'vom Launcher', 'mods.folder': 'Ordner öffnen', 'mods.more': 'Mehr laden', 'mods.downloads': 'Downloads', 'mods.import': 'Importieren',
  'prof.title': 'Profile', 'prof.new': 'Neues Profil', 'prof.edit': 'Profil bearbeiten', 'prof.name': 'Name', 'prof.version': 'Minecraft-Version', 'prof.loader': 'Mod-Loader',
  'prof.loaderVersion': 'Loader-Version', 'prof.newest': 'Neueste stabile', 'prof.lego': 'LEGO Client', 'prof.lego.d': 'Installiert den LEGO Client + Fabric API (nur Fabric 1.21.11).',
  'prof.fps': 'FPS-Paket', 'prof.fps.d': 'Sodium, Lithium, FerriteCore, ImmediatelyFast und EntityCulling, soweit für Version und Loader verfügbar.',
  'prof.memory': 'Arbeitsspeicher', 'prof.jvm': 'JVM-Argumente (nur -X…, -XX:…, -D…)', 'prof.width': 'Fensterbreite', 'prof.height': 'Fensterhöhe', 'prof.default': 'Standard',
  'prof.icon': 'Symbol', 'prof.save': 'Speichern', 'prof.back': 'Zurück', 'prof.delete': 'Löschen', 'prof.saved': 'Profil gespeichert', 'prof.play': 'Spielen', 'prof.mods': 'Mods',
  'prof.import': 'Importieren', 'prof.importFile': 'Modpack-Datei (.mrpack / CurseForge .zip)', 'prof.importScan': 'Andere Launcher durchsuchen', 'prof.importFolder': 'Ordner wählen',
  'prof.importWorlds': 'Welten mitkopieren', 'prof.importNone': 'Keine Instanzen anderer Launcher gefunden.', 'prof.imported': 'Importiert', 'prof.experimental': 'experimentell',
  'prof.unsupported': 'nicht verfügbar für diese Version', 'prof.never': 'nie gespielt',
};

type Key = keyof typeof de;

const en: Partial<Record<Key, string>> = {
  'nav.play': 'Play', 'nav.profiles': 'Profiles', 'nav.mods': 'Mods', 'nav.friends': 'Friends', 'nav.rewards': 'Rewards', 'nav.account': 'Account', 'nav.dev': 'Developer', 'nav.settings': 'Settings',
  'acc.signin': 'Sign in', 'acc.add': 'Add account', 'acc.signout': 'Sign out', 'acc.manage': 'Manage accounts', 'acc.offline': 'LEGO offline',
  'play.play': 'PLAY', 'play.running': 'RUNNING', 'play.installing': 'INSTALLING', 'play.signin': 'Sign in with Microsoft', 'play.welcome': 'Welcome back',
  'play.hello': 'Hello', 'play.notSigned': 'Not signed in', 'play.profile': 'Choose profile', 'play.newProfile': 'New profile', 'play.console': 'Console',
  'play.cancel': 'Cancel', 'play.news': 'News', 'play.coins': 'LEGO Coins', 'play.today': 'Earned today', 'play.noNews': 'No news.',
  'play.newsOffline': 'News unavailable (offline).', 'play.daily': 'Daily goal', 'play.dailyHint': 'Coins are earned by playtime and LEGO minigames.',
  'play.anim.idle': 'Idle', 'play.anim.walk': 'Walk', 'play.anim.wave': 'Wave', 'play.anim.spin': 'Spin', 'play.vanilla': 'Vanilla',
  'play.noClientId': 'This build has no Microsoft client id. See docs/LAUNCHER.md.', 'play.fun': 'Have fun!', 'play.lastPlayed': 'Last played',
  'set.title': 'Settings', 'set.search': 'Search settings…', 'set.general': 'General', 'set.appearance': 'Appearance', 'set.background': 'Background',
  'set.game': 'Game', 'set.themes': 'Themes', 'set.editor': 'Theme editor', 'set.rarity': 'Rarities', 'set.accounts': 'Accounts', 'set.advanced': 'Advanced', 'set.about': 'About',
  'set.language': 'Language', 'set.language.d': 'Language of the launcher interface.', 'set.closeOnLaunch': 'Minimise on game start', 'set.closeOnLaunch.d': 'The launcher goes to the taskbar while Minecraft runs.',
  'set.updates': 'Updates', 'set.updates.d': 'How new launcher versions are installed.', 'set.upd.ask': 'Ask', 'set.upd.auto': 'Automatic', 'set.upd.off': 'Do not check',
  'set.accent': 'Accent colour', 'set.accent.d': 'Overrides the theme colour. Reset uses the theme colour again.', 'set.reset': 'Reset', 'set.custom': 'Custom',
  'set.scale': 'UI scale', 'set.scale.d': 'For high resolutions up to 4K.', 'set.motion': 'Reduce motion', 'set.motion.d': 'Background and character stand still.',
  'set.bgPick': 'Animated background', 'set.bgPick.d': 'Drawn live behind the launcher.', 'set.snow': 'Snow', 'set.snow.d': 'Snowflakes above the background.',
  'set.quality': 'Quality', 'set.quality.d': 'Lower = less GPU load, higher = sharper and more particles.',
  'set.snapshots': 'Show snapshots', 'set.snapshots.d': 'Development versions in the version list.', 'set.historical': 'Historical versions', 'set.historical.d': 'Alpha and beta versions before 1.0.',
  'set.cfKey': 'CurseForge API key', 'set.cfKey.d': 'Only needed to fully import CurseForge modpacks (console.curseforge.com).',
  'set.backend': 'LEGO server', 'set.backend.d': 'https only. Empty = default server of this build.', 'set.gameDir': 'Game folder', 'set.gameDir.d': 'Versions, libraries, assets and profiles.',
  'set.msClient': 'Microsoft client id', 'set.msClient.d': 'Azure app id for sign-in (approved by Mojang). Empty = the id built into the launcher. Guide: docs/MICROSOFT-LOGIN.md.',
  'set.open': 'Open', 'set.save': 'Save', 'set.saved': 'Saved', 'set.diag': 'Diagnostics', 'set.diag.d': 'Copy system information for bug reports.', 'set.copy': 'Copy',
  'mods.title': 'Mods & content', 'mods.installed': 'Installed', 'mods.resourcepacks': 'Resource packs', 'mods.shaders': 'Shaders',
  'mods.search': 'Search Modrinth…', 'mods.install': 'Install', 'mods.installedOk': 'Installed', 'mods.remove': 'Remove', 'mods.empty': 'No mods in this profile yet.',
  'mods.vanilla': 'This profile is vanilla. Choose a mod loader (Fabric, Quilt, Forge or NeoForge) in the profile to install mods.',
  'mods.managed': 'by launcher', 'mods.folder': 'Open folder', 'mods.more': 'Load more', 'mods.import': 'Import',
  'prof.title': 'Profiles', 'prof.new': 'New profile', 'prof.edit': 'Edit profile', 'prof.name': 'Name', 'prof.version': 'Minecraft version', 'prof.loader': 'Mod loader',
  'prof.loaderVersion': 'Loader version', 'prof.newest': 'Newest stable', 'prof.lego.d': 'Installs the LEGO client + Fabric API (Fabric 1.21.11 only).',
  'prof.fps': 'FPS pack', 'prof.fps.d': 'Sodium, Lithium, FerriteCore, ImmediatelyFast and EntityCulling where available for version and loader.',
  'prof.memory': 'Memory', 'prof.jvm': 'JVM arguments (-X…, -XX:…, -D… only)', 'prof.width': 'Window width', 'prof.height': 'Window height', 'prof.default': 'Default',
  'prof.icon': 'Icon', 'prof.save': 'Save', 'prof.back': 'Back', 'prof.delete': 'Delete', 'prof.saved': 'Profile saved', 'prof.play': 'Play',
  'prof.import': 'Import', 'prof.importFile': 'Modpack file (.mrpack / CurseForge .zip)', 'prof.importScan': 'Scan other launchers', 'prof.importFolder': 'Choose folder',
  'prof.importWorlds': 'Copy worlds too', 'prof.importNone': 'No instances of other launchers found.', 'prof.imported': 'Imported', 'prof.experimental': 'experimental',
  'prof.unsupported': 'not available for this version', 'prof.never': 'never played',
};

let lang: 'de' | 'en' = 'de';

export function setLang(l: 'de' | 'en'): void {
  lang = l;
  document.documentElement.lang = l;
}

export function getLang(): 'de' | 'en' {
  return lang;
}

export function t(key: Key): string {
  return (lang === 'en' ? en[key] : undefined) ?? de[key];
}
