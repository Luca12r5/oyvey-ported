package dev.lego.core;

import java.util.HashMap;
import java.util.Map;

public final class Lang {
   public static final String[] LANGUAGES = new String[]{"Deutsch", "English"};
   public static final String[] LANG_CODES = new String[]{"de", "en"};
   private static int langIndex = 0;
   private static final Map<String, String[]> TRANSLATIONS = new HashMap<>();

   private Lang() {
   }

   public static void setLanguage(int idx) {
      langIndex = Math.max(0, Math.min(idx, LANG_CODES.length - 1));
   }

   public static int languageIndex() {
      return langIndex;
   }

   public static String languageCode() {
      return LANG_CODES[langIndex];
   }

   public static String get(String key) {
      String[] t = TRANSLATIONS.get(key);
      return t != null && langIndex < t.length ? t[langIndex] : null;
   }

   public static String get(String key, String fallback) {
      String v = get(key);
      return v != null ? v : fallback;
   }

   static void put(String key, String de, String en) {
      TRANSLATIONS.put(key, new String[]{de, en});
   }

   static {
      put("cat.hud", "HUD", "HUD");
      put("cat.hud.sub", "Saubere Anzeigen auf dem Bildschirm", "Clean displays on screen");
      put("cat.visuals", "Visuals", "Visuals");
      put("cat.visuals.sub", "Himmel, Effekte, Filter, Partikel", "Sky, effects, filters, particles");
      put("cat.utility", "Utility", "Utility");
      put("cat.utility.sub", "Legit Helfer: Sprint, InvSee, Wegpunkte ...", "Legit helpers: sprint, InvSee, waypoints ...");
      put("cat.performance", "Leistung", "Performance");
      put("cat.performance.sub", "Mehr FPS, weniger Ruckler", "More FPS, less stutter");
      put("cat.spotify", "Spotify", "Spotify");
      put("cat.spotify.sub", "Spotify HUD + Karaoke-Lyrics", "Spotify HUD + karaoke lyrics");
      put("cat.cosmetics", "Kosmetik", "Cosmetics");
      put("cat.cosmetics.sub", "Umhänge, Flügel, Hüte ...", "Capes, wings, hats ...");
      put("cat.emotes", "Emotes", "Emotes");
      put("cat.emotes.sub", "Animationen für deinen Spieler", "Animations for your player");
      put("cat.games", "Minispiele", "Mini Games");
      put("cat.games.sub", "Spiele für Zwischendurch", "Games to pass the time");
      put("cat.world_fx", "Welt-Effekte", "World Effects");
      put("cat.world_fx.sub", "Partikel in der Welt", "Particles in the world");
      put("ui.all", "ALLE", "ALL");
      put("ui.perf", "LEISTUNG", "PERFORMANCE");
      put("ui.settings", "Einstellungen", "Settings");
      put("ui.nomods", "Keine Module gefunden", "No modules found");
      put("ui.active", "aktiv", "active");
      put("ui.of", "von", "of");
      put("ui.modules", "Modulen", "modules");
      put("ui.hint", "Klick = an/aus   •   Rechtsklick = Einstellungen", "Click = on/off   •   Right-click = settings");
      put("ui.presskey", "Taste drücken…", "Press key…");
      put("ui.key", "Taste", "Key");
      put("ui.done", "Fertig", "Done");
      put("ui.rename", "Umbenennen", "Rename");
      put("ui.show", "Einblenden", "Show");
      put("ui.hide", "Ausblenden", "Hide");
      put("ui.nosettings", "Dieses Modul hat keine Einstellungen.", "This module has no settings.");
      put("ui.reset", "Standard wiederherstellen", "Restore defaults");
      put("ui.new", "NEU", "NEW");
      put("mod.infobar", "Info-Leiste", "Info Bar");
      put("mod.infobar.desc", "Lego | FPS | Ping | Spielzeit – alles in einer sauberen Zeile", "Lego | FPS | Ping | Session – all in one clean bar");
      put("mod.fps", "FPS", "FPS");
      put("mod.fps.desc", "Bilder pro Sekunde", "Frames per second");
      put("mod.ping", "Ping", "Ping");
      put("mod.ping.desc", "Verbindung zum Server in Millisekunden", "Connection to server in milliseconds");
      put("mod.cps", "CPS", "CPS");
      put("mod.cps.desc", "Klicks pro Sekunde (links | rechts)", "Clicks per second (left | right)");
      put("mod.clock", "Uhrzeit", "Clock");
      put("mod.clock.desc", "Echte Uhrzeit", "Real time clock");
      put("mod.gametime", "Minecraft-Zeit", "Minecraft Time");
      put("mod.gametime.desc", "Uhrzeit in der Minecraft-Welt (Tag/Nacht)", "Time in the Minecraft world (day/night)");
      put("mod.session", "Spielzeit", "Session Time");
      put("mod.session.desc", "Wie lange du schon auf dem Server bist", "How long you've been on the server");
      put("mod.speed", "Geschwindigkeit", "Speed");
      put("mod.speed.desc", "Blöcke pro Sekunde", "Blocks per second");
      put("mod.memory", "Arbeitsspeicher", "Memory");
      put("mod.memory.desc", "RAM-Verbrauch von Minecraft", "Minecraft RAM usage");
      put("mod.server", "Server-IP", "Server IP");
      put("mod.server.desc", "Adresse des Servers", "Server address");
      put("mod.day", "Tag-Zähler", "Day Counter");
      put("mod.day.desc", "Wie viele Minecraft-Tage vergangen sind", "How many Minecraft days have passed");
      put("mod.biome", "Biom", "Biome");
      put("mod.biome.desc", "Aktuelles Biom", "Current biome");
      put("mod.light", "Lichtlevel", "Light Level");
      put("mod.light.desc", "Blocklicht an deinen Füßen (unter 1 können Monster spawnen)", "Block light at your feet (below 1 monsters can spawn)");
      put("mod.combo", "Combo & Reichweite", "Combo & Reach");
      put("mod.combo.desc", "Treffer in Folge und Abstand beim letzten Schlag", "Consecutive hits and distance of last hit");
      put("mod.coords", "Koordinaten", "Coordinates");
      put("mod.coords.desc", "X Y Z, Richtung und Nether-Umrechnung", "X Y Z, direction and nether conversion");
      put("mod.direction", "Kompass", "Compass");
      put("mod.direction.desc", "Himmelsrichtung als Leiste oben", "Compass direction bar at the top");
      put("mod.armor", "Rüstung", "Armor");
      put("mod.armor.desc", "Rüstung mit Haltbarkeit in Prozent", "Armor with durability in percent");
      put("mod.effects", "Effekte", "Effects");
      put("mod.effects.desc", "Aktive Trank-Effekte mit Restzeit", "Active potion effects with remaining time");
      put("mod.keystrokes", "Tastenanzeige", "Keystrokes");
      put("mod.keystrokes.desc", "WASD, Maustasten und Leertaste mit Animation", "WASD, mouse buttons and space bar with animation");
      put("mod.totems", "Totem-Zähler", "Totem Counter");
      put("mod.totems.desc", "Wie viele Totems du hast", "How many totems you have");
      put("mod.pvpitems", "PvP-Items", "PvP Items");
      put("mod.pvpitems.desc", "Kristalle, Obsidian, Äpfel, Perlen, XP", "Crystals, obsidian, apples, pearls, XP");
      put("mod.target", "Target HUD", "Target HUD");
      put("mod.target.desc", "Leben und Abstand des Gegners mit Animation", "Enemy health and distance with animation");
      put("mod.hunger", "Hunger & Sättigung", "Hunger & Saturation");
      put("mod.hunger.desc", "Essen und Sättigung als Balken", "Food and saturation as bars");
      put("mod.health", "Leben", "Health");
      put("mod.health.desc", "Deine Herzen als Balken", "Your hearts as a bar");
      put("mod.h2_invcounter", "Inventar-Zähler", "Inventory Counter");
      put(
         "mod.h2_invcounter.desc",
         "Zählt in EINEM Panel, was du im Inventar hast: Totems, Perlen, Gaps, Kristalle, Pfeile ... (in den Einstellungen wählen)",
         "Counts in ONE panel what's in your inventory: totems, pearls, gaps, crystals, arrows ... (choose in settings)"
      );
      put("mod.h2_gaps", "Goldäpfel", "Golden Apples");
      put("mod.h2_gaps.desc", "Goldäpfel | verzauberte Goldäpfel", "Golden apples | enchanted golden apples");
      put("mod.h2_nearby", "Spieler in der Nähe", "Nearby Players");
      put("mod.h2_nearby.desc", "Wie viele Spieler im Umkreis von 64 Blöcken sind", "How many players are within 64 blocks");
      put("mod.h2_closest", "Nächster Spieler", "Closest Player");
      put("mod.h2_closest.desc", "Name und Entfernung des nächsten Spielers", "Name and distance of the closest player");
      put("mod.h2_entities", "Entitäten", "Entities");
      put("mod.h2_entities.desc", "Geladene Entitäten um dich (Lag-Check)", "Loaded entities around you (lag check)");
      put("mod.h2_freeslots", "Freie Plätze", "Free Slots");
      put("mod.h2_freeslots.desc", "Freie Plätze im Inventar", "Free slots in inventory");
      put("mod.h2_deaths", "Tode", "Deaths");
      put("mod.h2_deaths.desc", "Wie oft du in dieser Sitzung gestorben bist", "How many times you died this session");
      put("mod.h4_date", "Datum", "Date");
      put("mod.h4_date.desc", "Heutiges Datum", "Today's date");
      put("mod.h4_stopwatch", "Stoppuhr", "Stopwatch");
      put("mod.h4_stopwatch.desc", "Läuft, solange das Modul an ist - aus/an setzt sie zurück", "Runs while the module is on - off/on resets it");
      put("mod.h4_tps", "TPS (geschätzt)", "TPS (estimated)");
      put("mod.h4_tps.desc", "Geschätzte Server-TPS aus der Weltzeit (20 = flüssig)", "Estimated server TPS from world time (20 = smooth)");
      put("mod.h4_online", "Spieler online", "Players Online");
      put("mod.h4_online.desc", "Wie viele Spieler auf dem Server sind (Tab-Liste)", "How many players are on the server (tab list)");
      put("mod.sky", "Custom Sky", "Custom Sky");
      put("mod.sky.desc", "Realistische HD-Himmel, Sky-Resourcepacks und eigene Bilder", "Realistic HD skies, sky resource packs and custom images");
      put("mod.colorfilter", "Farbsättigung & Filter", "Color & Filter");
      put("mod.colorfilter.desc", "Mehr Farbe, Kino-Look, Schwarz-Weiß, Warm, Kalt, Vintage ...", "More color, cinematic look, B&W, warm, cool, vintage ...");
      put("mod.jumpcircles", "Jump Circles", "Jump Circles");
      put(
         "mod.jumpcircles.desc",
         "12 Effekte beim Springen: Ringe, Nova, Spirale, Funken, Blitze ...",
         "12 jump effects: rings, nova, spiral, sparks, lightning ..."
      );
      put("mod.crystals", "Custom Kristalle", "Custom Crystals");
      put("mod.crystals.desc", "Farbige Aura und Glow um End-Kristalle", "Colored aura and glow around end crystals");
      put("mod.totem", "Custom Totem", "Custom Totem");
      put("mod.totem.desc", "Ringe, Partikel und Pop-Zähler bei Totems", "Rings, particles and pop counter for totems");
      put("mod.hitparticles", "Hit-Partikel", "Hit Particles");
      put("mod.hitparticles.desc", "Extra Partikel wenn du triffst", "Extra particles when you hit");
      put("mod.trails", "Trails", "Trails");
      put("mod.trails.desc", "Partikelspur hinter dir", "Particle trail behind you");
      put("mod.killeffect", "Kill-Effekt", "Kill Effect");
      put("mod.killeffect.desc", "Partikel-Explosion und Lichtstrahl wenn dein Gegner stirbt", "Particle explosion and beam when your enemy dies");
      put("mod.fullbright", "Fullbright", "Fullbright");
      put("mod.fullbright.desc", "Alles hell (wie Nachtsicht, nur für dich)", "Everything bright (like night vision, only for you)");
      put("mod.zoom", "Zoom", "Zoom");
      put("mod.zoom.desc", "Taste C halten zum Zoomen, Mausrad = stärker/schwächer", "Hold C to zoom, scroll wheel = more/less");
      put("mod.nohurtcam", "Kein Wackeln", "No Hurt Cam");
      put("mod.nohurtcam.desc", "Kein Bildschirm-Wackeln bei Schaden", "No screen shake on damage");
      put("mod.timechanger", "Zeit ändern", "Time Changer");
      put("mod.timechanger.desc", "Tag oder Nacht nur für dich", "Day or night just for you");
      put("mod.clearweather", "Kein Regen", "Clear Weather");
      put("mod.clearweather.desc", "Regen und Gewitter nur für dich aus", "Rain and thunder off just for you");
      put("mod.crosshair", "Custom Crosshair", "Custom Crosshair");
      put("mod.crosshair.desc", "Eigenes Fadenkreuz", "Custom crosshair");
      put("mod.blockoutline", "Block-Outline", "Block Outline");
      put("mod.blockoutline.desc", "Schöner Rahmen und Füllung um den anvisierten Block", "Nice outline and fill around the targeted block");
      put("mod.damagenumbers", "Schadenszahlen", "Damage Numbers");
      put("mod.damagenumbers.desc", "Fliegende Zahlen zeigen, wie viel Schaden du machst", "Flying numbers show how much damage you deal");
      put("mod.targetmarker", "Ziel-Markierung", "Target Marker");
      put("mod.targetmarker.desc", "Markiert den Gegner, den du gerade bekämpfst", "Marks the enemy you're currently fighting");
      put("mod.playerrings", "Spieler-Ringe", "Player Rings");
      put("mod.playerrings.desc", "Leuchtende Ringe unter anderen Spielern", "Glowing rings under other players");
      put("mod.chinahat", "China Hat", "China Hat");
      put("mod.chinahat.desc", "Gooba-Style konischer Hut über dem Spieler", "Gooba-style conical hat above the player");
      put("mod.shaderhands", "Shader Hands", "Shader Hands");
      put("mod.shaderhands.desc", "GPU Glow-Outline auf den Händen", "GPU glow outline on hands");
      put("mod.shadersky", "Shader Sky", "Shader Sky");
      put("mod.shadersky.desc", "Gooba-Style Shader-Himmel mit Aurora, Sternen und mehr", "Gooba-style shader sky with aurora, stars and more");
      put("mod.projectiletrails", "Geschoss-Spuren", "Projectile Trails");
      put("mod.projectiletrails.desc", "Leuchtende Spuren hinter Pfeilen, Perlen und Dreizacken", "Glowing trails behind arrows, pearls and tridents");
      put("mod.dynamiccamera", "Dynamische Kamera", "Dynamic Camera");
      put("mod.dynamiccamera.desc", "Leichte Kamera-Neigung beim seitlichen Laufen", "Slight camera tilt when strafing");
      put("mod.lowfire", "Low Fire", "Low Fire");
      put("mod.lowfire.desc", "Feuer-Overlay tiefer, damit du besser siehst", "Fire overlay lowered so you can see better");
      put("mod.floatingcubes", "Schwebende Würfel", "Floating Cubes");
      put("mod.floatingcubes.desc", "Leuchtende Würfel mit Physik schweben um dich herum", "Glowing cubes with physics float around you");
      put("mod.worldgrid", "Welt-Gitter", "World Grid");
      put("mod.worldgrid.desc", "Leuchtende 3D-Gitterlinien in der Welt (12-Block-Zellen)", "Glowing 3D grid lines in the world (12 block cells)");
      put("mod.totemghost", "Item Transparent", "Item Transparent");
      put("mod.totemghost.desc", "Macht das Totem in der Hand durchsichtig", "Makes the totem in your hand transparent");
      put("mod.trajectories", "Flugbahnen", "Trajectories");
      put("mod.trajectories.desc", "Zeigt die vorhergesagte Flugbahn von Projektilen", "Shows predicted trajectory of projectiles");
      put("mod.cinemabars", "Kino-Balken", "Cinema Bars");
      put("mod.cinemabars.desc", "Schwarze Filmbalken oben und unten (für Aufnahmen)", "Black film bars top and bottom (for recordings)");
      put("mod.vignette", "Vignette", "Vignette");
      put("mod.vignette.desc", "Weicher dunkler Rand - bei wenig Leben pulsiert er rot", "Soft dark border - pulses red on low health");
      put("mod.hitmarker", "Hitmarker", "Hitmarker");
      put("mod.hitmarker.desc", "Kurzes X am Fadenkreuz, wenn du triffst", "Brief X on crosshair when you hit");
      put("mod.namefx", "Namensschild", "Name Tag");
      put(
         "mod.namefx.desc",
         "Dein Name-Tag animiert (nur für dich sichtbar) + Spotify-Karte darüber",
         "Your name tag animated (only visible to you) + Spotify card above"
      );
      put("mod.skyperson", "Sky-Figur", "Sky Figure");
      put(
         "mod.skyperson.desc",
         "Ein PNG von einem Menschen (oder was du willst) riesig am Himmel - Hintergrund wird entfernt",
         "A PNG of a person (or whatever) giant in the sky - background is removed"
      );
      put("mod.worldeffects", "Welt-Effekte", "World Effects");
      put(
         "mod.worldeffects.desc",
         "Alle Partikel in der Welt in einem Modul: Glühwürmchen, Vogelschwarm, Schnee, Laternen ... einzeln an/aus",
         "All world particles in one module: fireflies, birds, snow, lanterns ... toggle individually"
      );
      put("mod.snowfall", "Schnee & Blüten", "Snow & Blossoms");
      put(
         "mod.snowfall.desc",
         "HD-Schnee, Kirschblüten, Herbstlaub, Glitzer ... fallen um dich herum",
         "HD snow, cherry blossoms, autumn leaves, glitter ... falling around you"
      );
      put("mod.fireflies", "Glühwürmchen", "Fireflies");
      put("mod.fireflies.desc", "Leuchtende Glühwürmchen schwirren um dich herum", "Glowing fireflies buzzing around you");
      put("mod.lanterns", "Himmelslaternen", "Sky Lanterns");
      put("mod.lanterns.desc", "Leuchtende Laternen steigen langsam in den Himmel", "Glowing lanterns slowly rising into the sky");
      put("mod.motes", "Lichtstaub", "Light Dust");
      put("mod.motes.desc", "Feiner leuchtender Staub schwebt in der Luft (sehr cinematic)", "Fine glowing dust floating in the air (very cinematic)");
      put("mod.bubbles", "Seifenblasen", "Soap Bubbles");
      put("mod.bubbles.desc", "Schillernde Seifenblasen schweben um dich", "Iridescent soap bubbles floating around you");
      put("mod.butterflies", "Schmetterlinge", "Butterflies");
      put("mod.butterflies.desc", "Bunte Schmetterlinge flattern um dich herum (tagsüber)", "Colorful butterflies fluttering around you (daytime)");
      put("mod.rainbow", "Regenbogen", "Rainbow");
      put("mod.rainbow.desc", "Ein echter Regenbogen gegenüber der Sonne - nach Regen oder immer", "A real rainbow opposite the sun - after rain or always");
      put("mod.aurora", "Nordlicht", "Northern Lights");
      put("mod.aurora.desc", "Wehende Polarlicht-Vorhänge am Nachthimmel (echt 3D)", "Waving aurora curtains in the night sky (real 3D)");
      put("mod.fpsboost", "FPS-Boost", "FPS Boost");
      put("mod.fpsboost.desc", "VSync aus, FPS unbegrenzt, Grafik auf Leistung", "VSync off, FPS unlimited, graphics on performance");
      put("mod.entityculling", "Entity-Culling", "Entity Culling");
      put(
         "mod.entityculling.desc",
         "Rendert Mobs/Items nicht, wenn sie weit weg oder hinter Blöcken sind",
         "Skips rendering mobs/items when far away or behind blocks"
      );
      put("mod.particlelimit", "Partikel-Limit", "Particle Limit");
      put("mod.particlelimit.desc", "Begrenzt Partikel-Explosionen (TNT, Farmen) - weniger Ruckler", "Limits particle explosions (TNT, farms) - less stutter");
      put("mod.backgroundfps", "Hintergrund-FPS", "Background FPS");
      put(
         "mod.backgroundfps.desc",
         "Weniger FPS, wenn Minecraft nicht im Vordergrund ist (spart Strom & Hitze)",
         "Less FPS when Minecraft is in the background (saves power & heat)"
      );
      put("mod.freememory", "RAM freigeben", "Free Memory");
      put("mod.freememory.desc", "Gibt regelmäßig ungenutzten Arbeitsspeicher frei", "Regularly frees unused memory");
      put("mod.monitor", "Leistungs-Monitor", "Performance Monitor");
      put("mod.monitor.desc", "FPS-Graph und Leistungs-Infos", "FPS graph and performance info");
      put("mod.guard", "Crash-Schutz", "Crash Guard");
      put("mod.guard.desc", "Schützt vor Einfrieren und Abstürzen bei Lag", "Protects against freezes and crashes during lag");
      put("mod.invsee", "InvSee", "InvSee");
      put(
         "mod.invsee.desc",
         "Spieler ansehen + V: öffnet sein Inventar (/invsee) - braucht Server-Rechte",
         "Look at player + V: opens their inventory (/invsee) - needs server permissions"
      );
      put("mod.ecsee", "EcSee", "EcSee");
      put("mod.ecsee.desc", "Spieler ansehen + V: öffnet seine Endertruhe (/ecsee)", "Look at player + V: opens their ender chest (/ecsee)");
      put("mod.togglesprint", "Toggle Sprint", "Toggle Sprint");
      put(
         "mod.togglesprint.desc",
         "Du sprintest automatisch, sobald du nach vorne läufst (wie Vanilla-Sprint gehalten)",
         "You sprint automatically when walking forward (like vanilla sprint held)"
      );
      put("mod.togglesneak", "Toggle Sneak", "Toggle Sneak");
      put("mod.togglesneak.desc", "Schleichen-Taste einmal drücken = an, nochmal = aus", "Press sneak key once = on, again = off");
      put("mod.freelook", "Freelook", "Freelook");
      put(
         "mod.freelook.desc", "Taste halten: Kamera frei um dich drehen, ohne dass du dich umdrehst", "Hold key: rotate camera freely without turning yourself"
      );
      put("mod.autogg", "Auto-GG", "Auto GG");
      put("mod.autogg.desc", "Schreibt nach einem Spieler-Kill automatisch deine Nachricht", "Automatically sends your message after a player kill");
      put("mod.autorespawn", "Auto-Respawn", "Auto Respawn");
      put("mod.autorespawn.desc", "Respawnt dich automatisch nach dem Tod", "Automatically respawns you after death");
      put("mod.waypoints", "Wegpunkte", "Waypoints");
      put(
         "mod.waypoints.desc",
         "Markiere Orte mit Lichtstrahl und Entfernung - nur für dich sichtbar",
         "Mark locations with beam and distance - only visible to you"
      );
      put("mod.deathpoint", "Todespunkt", "Death Point");
      put("mod.deathpoint.desc", "Merkt sich, wo du gestorben bist (roter Wegpunkt)", "Remembers where you died (red waypoint)");
      put("mod.hitsound", "Hit-Sound", "Hit Sound");
      put("mod.hitsound.desc", "Spielt einen Ton, wenn du triffst", "Plays a sound when you hit");
      put("mod.killsound", "Kill-Sound", "Kill Sound");
      put("mod.killsound.desc", "Spielt einen Ton, wenn du jemanden besiegst", "Plays a sound when you defeat someone");
      put("mod.mention", "Erwähnungs-Ping", "Mention Ping");
      put("mod.mention.desc", "Ton + Hinweis bei @Name im Chat", "Sound + notification when @Name in chat");
      put("mod.antispam", "Anti-Spam", "Anti Spam");
      put("mod.antispam.desc", "Blendet gleiche Nachrichten aus, die kurz hintereinander kommen", "Hides identical messages that come in quick succession");
      put("mod.wordfilter", "Wort-Filter", "Word Filter");
      put("mod.wordfilter.desc", "Versteckt Chat-Nachrichten mit bestimmten Wörtern", "Hides chat messages with certain words");
      put("mod.macros", "Auto Text", "Auto Text");
      put("mod.macros.desc", "Taste drücken = Nachricht oder /Befehl senden (z.B. /home, /spawn)", "Press key = send message or /command (e.g. /home, /spawn)");
      put("mod.sharecoords", "Koordinaten teilen", "Share Coords");
      put("mod.sharecoords.desc", "Taste belegen: kopiert deine Koordinaten oder sendet sie in den Chat", "Bind key: copies your coords or sends them in chat");
      put("mod.u4_hunger", "Hunger-Warnung", "Hunger Warning");
      put("mod.u4_hunger.desc", "Meldet sich, wenn dein Hunger auf 3 Keulen fällt", "Warns when your hunger drops to 3 drumsticks");
      put("mod.u4_air", "Luft-Warnung", "Air Warning");
      put("mod.u4_air.desc", "Warnt, bevor dir unter Wasser die Luft ausgeht", "Warns before you run out of air underwater");
      put("mod.u4_fire", "Feuer-Warnung", "Fire Warning");
      put("mod.u4_fire.desc", "Warnt, wenn du brennst", "Warns when you are on fire");
      put("mod.u4_lava", "Lava-Warnung", "Lava Warning");
      put("mod.u4_lava.desc", "Warnt, wenn Lava direkt neben oder unter dir ist", "Warns when lava is right next to or below you");
      put("mod.u4_fall", "Fall-Warnung", "Fall Warning");
      put("mod.u4_fall.desc", "Warnt, wenn du tief fällst (über 8 Blöcke)", "Warns when you fall deep (over 8 blocks)");
      put("mod.u4_tool", "Werkzeug-Warnung", "Tool Warning");
      put("mod.u4_tool.desc", "Warnt, wenn dein Werkzeug fast kaputt ist (unter 8 %)", "Warns when your tool is almost broken (below 8%)");
      put("mod.u4_shieldlow", "Schild-Warnung", "Shield Warning");
      put("mod.u4_shieldlow.desc", "Warnt, wenn dein Schild fast kaputt ist", "Warns when your shield is almost broken");
      put("mod.u4_elytralow", "Elytra-Warnung", "Elytra Warning");
      put("mod.u4_elytralow.desc", "Warnt, wenn deine Elytra fast kaputt ist", "Warns when your elytra is almost broken");
      put("mod.u4_invfull", "Inventar-voll-Warnung", "Inventory Full Warning");
      put("mod.u4_invfull.desc", "Sagt Bescheid, wenn dein Inventar voll ist", "Notifies when your inventory is full");
      put("mod.u4_night", "Nacht-Warnung", "Night Warning");
      put("mod.u4_night.desc", "Meldet, wenn es bald Nacht wird (Monster!)", "Warns when night is approaching (monsters!)");
      put("mod.u4_dawn", "Morgen-Meldung", "Dawn Notification");
      put("mod.u4_dawn.desc", "Meldet den Sonnenaufgang", "Notifies at sunrise");
      put("mod.u4_storm", "Gewitter-Warnung", "Storm Warning");
      put("mod.u4_storm.desc", "Meldet, wenn ein Gewitter beginnt", "Notifies when a thunderstorm starts");
      put("mod.u4_creeper", "Creeper-Warnung", "Creeper Warning");
      put("mod.u4_creeper.desc", "Warnt, wenn ein Creeper näher als 7 Blöcke ist", "Warns when a creeper is closer than 7 blocks");
      put("mod.u4_mobs", "Monster-Warnung", "Mob Warning");
      put("mod.u4_mobs.desc", "Warnt, wenn 3 oder mehr Monster in deiner Nähe sind", "Warns when 3 or more hostiles are near you");
      put("mod.u4_void", "Void-Warnung", "Void Warning");
      put("mod.u4_void.desc", "Warnt, wenn du kurz über der Leere bist", "Warns when you're above the void");
      put("mod.u4_lowfps", "Low-FPS-Warnung", "Low FPS Warning");
      put("mod.u4_lowfps.desc", "Meldet, wenn deine FPS 5 Sekunden lang unter 25 liegen", "Notifies when your FPS stays below 25 for 5 seconds");
      put("mod.u4_highping", "Ping-Warnung", "Ping Warning");
      put("mod.u4_highping.desc", "Meldet, wenn dein Ping über 250 ms steigt", "Notifies when your ping exceeds 250 ms");
      put("mod.u4_approach", "Spieler-Annäherung", "Player Approach");
      put("mod.u4_approach.desc", "Meldet, wenn ein neuer Spieler in 32 Blöcke Nähe kommt", "Notifies when a new player comes within 32 blocks");
      put("mod.u4_level30", "Level-30-Meldung", "Level 30 Notice");
      put("mod.u4_level30.desc", "Meldet, wenn du Level 30 erreichst (bereit zum Verzaubern)", "Notifies when you reach level 30 (ready to enchant)");
      put("mod.u4_arrows", "Pfeil-Warnung", "Arrow Warning");
      put("mod.u4_arrows.desc", "Warnt, wenn du einen Bogen hältst und weniger als 8 Pfeile hast", "Warns when holding a bow with fewer than 8 arrows");
      put("mod.u4_totemhand", "Totem-Hand-Hinweis", "Totem Hand Reminder");
      put("mod.u4_totemhand.desc", "Erinnert dich, wenn du Totems hast, aber keins in der Zweithand", "Reminds you when you have totems but none in offhand");
      put("mod.u4_sleepnow", "Schlaf-Hinweis", "Sleep Reminder");
      put("mod.u4_sleepnow.desc", "Erinnert dich, wenn du im Bett schlafen kannst", "Reminds you when you can sleep in a bed");
      put("mod.u4_rarefind", "Fund-Meldung", "Rare Find Notice");
      put("mod.u4_rarefind.desc", "Meldet, wenn du Diamanten, Smaragde oder Netherit bekommst", "Notifies when you get diamonds, emeralds or netherite");
      put("mod.u4_joinleave", "Join/Leave-Anzeige", "Join/Leave Display");
      put(
         "mod.u4_joinleave.desc", "Kleine Meldung, wenn Spieler den Server betreten oder verlassen", "Small notification when players join or leave the server"
      );
      put("mod.u4_friends", "Freunde-Meldung", "Friends Notification");
      put(
         "mod.u4_friends.desc",
         "Meldet, wenn deine Freunde online kommen (Namen in den Einstellungen)",
         "Notifies when your friends come online (names in settings)"
      );
      put("mod.u4_timestamps", "Chat-Zeitstempel", "Chat Timestamps");
      put("mod.u4_timestamps.desc", "Zeigt die Uhrzeit vor jeder Chat-Nachricht", "Shows the time before every chat message");
      put("mod.u4_hidejoin", "Beitritte ausblenden", "Hide Join Messages");
      put("mod.u4_hidejoin.desc", "Blendet \"... hat das Spiel betreten/verlassen\" aus", "Hides \"... joined/left the game\" messages");
      put("mod.u4_hidedeath", "Todesnachrichten ausblenden", "Hide Death Messages");
      put("mod.u4_hidedeath.desc", "Blendet die Todesnachrichten anderer Spieler aus", "Hides other players' death messages");
      put("mod.u4_hideadv", "Erfolge ausblenden", "Hide Achievements");
      put("mod.u4_hideadv.desc", "Blendet \"... hat den Fortschritt erzielt\" aus", "Hides \"... has made the advancement\" messages");
      put("mod.u4_hidelinks", "Link-Filter", "Link Filter");
      put("mod.u4_hidelinks.desc", "Blendet Chat-Nachrichten mit Links (Werbung) aus", "Hides chat messages with links (ads)");
      put("mod.u4_caps", "Schrei-Filter", "Caps Filter");
      put("mod.u4_caps.desc", "Zeigt Nachrichten in GROSSBUCHSTABEN klein an", "Shows ALL CAPS messages in lowercase");
      put("mod.u4_chatsound", "Chat-Ton", "Chat Sound");
      put("mod.u4_chatsound.desc", "Leiser Ton bei jeder Chat-Nachricht", "Quiet sound on every chat message");
      put("mod.u4_pmsound", "Privatnachricht-Ton", "PM Sound");
      put("mod.u4_pmsound.desc", "Ton + Meldung bei privaten Nachrichten (/msg)", "Sound + notification on private messages (/msg)");
      put("mod.u4_chatlog", "Chat-Protokoll", "Chat Log");
      put("mod.u4_chatlog.desc", "Speichert den Chat in config/legoclient/chat.log", "Saves chat to config/legoclient/chat.log");
      put("mod.u4_emoji", "Emoji-Ersetzer", "Emoji Replacer");
      put(
         "mod.u4_emoji.desc",
         "Ersetzt :herz: :stern: :haken: :pfeil: :musik: :sonne: :smile: in deinen Nachrichten",
         "Replaces :heart: :star: :check: :arrow: :music: :sun: :smile: in your messages"
      );
      put("mod.u4_capitalize", "Auto-Großschreibung", "Auto Capitalize");
      put("mod.u4_capitalize.desc", "Macht den ersten Buchstaben deiner Nachrichten groß", "Capitalizes the first letter of your messages");
      put("mod.u4_calc", "Rechner", "Calculator");
      put("mod.u4_calc.desc", "Tippe .rechne 12*(3+4) in den Chat - das Ergebnis siehst nur du", "Type .calc 12*(3+4) in chat - only you see the result");
      put("mod.u4_dice", "Würfel", "Dice");
      put("mod.u4_dice.desc", ".würfel oder .würfel 20 - würfelt eine Zahl (nur für dich)", ".dice or .dice 20 - rolls a number (only you)");
      put("mod.u4_coin", "Münzwurf", "Coin Flip");
      put("mod.u4_coin.desc", ".münze - Kopf oder Zahl", ".coin - heads or tails");
      put("mod.u4_random", "Zufallszahl", "Random Number");
      put("mod.u4_random.desc", ".zufall 1 100 - Zufallszahl im Bereich", ".random 1 100 - random number in range");
      put("mod.u4_choose", "Entscheider", "Chooser");
      put("mod.u4_choose.desc", ".wähle Pizza, Nudeln, Döner - wählt zufällig eins aus", ".choose pizza, pasta, kebab - picks one randomly");
      put("mod.u4_nether", "Portal-Rechner", "Portal Calculator");
      put("mod.u4_nether.desc", ".nether x z oder .oberwelt x z - rechnet Portal-Koordinaten um", ".nether x z or .overworld x z - converts portal coords");
      put("mod.u4_dist", "Entfernungs-Rechner", "Distance Calculator");
      put("mod.u4_dist.desc", ".dist x z - wie weit ein Ort von dir weg ist und in welche Richtung", ".dist x z - how far a location is and in which direction");
      put("mod.u4_note", "Notizen", "Notes");
      put("mod.u4_note.desc", ".notiz Text speichert eine Notiz, .notizen zeigt alle", ".note text saves a note, .notes shows all");
      put("mod.u4_notes", "Notizen anzeigen", "Show Notes");
      put("mod.u4_remind", "Erinnerung", "Reminder");
      put("mod.u4_remind.desc", ".erinnere 10 Ofen leeren - erinnert dich in 10 Minuten", ".remind 10 empty furnace - reminds you in 10 minutes");
      put("mod.u4_countdown", "Countdown", "Countdown");
      put("mod.u4_countdown.desc", ".countdown 30 - zählt 30 Sekunden runter", ".countdown 30 - counts down 30 seconds");
      put("mod.u4_time", "Zeit-Befehl", "Time Command");
      put("mod.u4_pos", "Position-Befehl", "Position Command");
      put("mod.u4_help", "Lego-Befehle", "Lego Commands");
      put("mod.u4_alarm", "Wecker", "Alarm Clock");
      put("mod.u4_alarm.desc", "Klingelt zur eingestellten echten Uhrzeit (z. B. 18:30)", "Rings at the set real time (e.g. 18:30)");
      put("mod.u4_afk", "AFK-Erkennung", "AFK Detection");
      put("mod.u4_afk.desc", "Merkt, wenn du nicht am PC warst, und sagt dir danach, wie lange", "Notices when you were AFK and tells you for how long");
      put("mod.u4_coordlog", "Weg-Protokoll", "Path Log");
      put(
         "mod.u4_coordlog.desc",
         "Speichert alle 2 Minuten deine Koordinaten in config/legoclient/wege.txt",
         "Saves your coordinates every 2 minutes to config/legoclient/wege.txt"
      );
      put("mod.u4_summary", "Sitzungs-Bericht", "Session Summary");
      put(
         "mod.u4_summary.desc",
         "Zeigt beim Verlassen einer Welt, wie weit du gelaufen bist und wie lange du da warst",
         "Shows how far you walked and how long you played when leaving a world"
      );
      put("mod.u4_copycoords", "Koordinaten kopieren", "Copy Coords");
      put("mod.u4_copycoords.desc", "Kopiert deine XYZ-Koordinaten in die Zwischenablage (einmal drücken)", "Copies your XYZ coords to clipboard (press once)");
      put("mod.u4_deathcoords", "Todes-Koordinaten", "Death Coords");
      put("mod.u4_deathcoords.desc", "Merkt sich, wo du gestorben bist, und zeigt es als Toast", "Remembers where you died and shows it as a toast");
      put("mod.u4_chunkborder", "Chunk-Grenzen", "Chunk Borders");
      put(
         "mod.u4_chunkborder.desc",
         "Zeigt die Chunk-Grenzen als grüne Linien um dich herum (wie F3+G, aber hübscher)",
         "Shows chunk borders as green lines around you (like F3+G, but prettier)"
      );
      put("mod.u4_sniper", "Sniper-Maus", "Sniper Mouse");
      put("mod.u4_fastmouse", "Schnelle Maus", "Fast Mouse");
      put("mod.u4_farview", "Weitsicht", "Far View");
      put("mod.u4_nearview", "Nahsicht", "Near View");
      put("mod.u4_nobob", "Kein Wackeln beim Laufen", "No View Bobbing");
      put("mod.u4_widefov", "Weites Sichtfeld", "Wide FOV");
      put("mod.u4_quakefov", "Quake-Sichtfeld", "Quake FOV");
      put("mod.u4_nofoveffect", "Kein Sprint-Zoom", "No Sprint Zoom");
      put("mod.u4_noscreenfx", "Keine Bildschirm-Verzerrung", "No Screen Distortion");
      put("mod.u4_cinematic", "Kino-Kamera", "Cinematic Camera");
      put("mod.u4_vsync", "VSync", "VSync");
      put("mod.u4_noshadows", "Entity-Schatten aus", "Entity Shadows Off");
      put("mod.u4_bright", "Helligkeit hell", "Brightness High");
      put("mod.u4_cloudsfast", "Schnelle Wolken", "Fast Clouds");
      put("mod.u4_particlesless", "Partikel reduziert", "Particles Reduced");
      put("mod.u4_noao", "Weiches Licht aus", "Ambient Occlusion Off");
      put("mod.u4_simdist", "Simulation 5 Chunks", "Simulation 5 Chunks");
      put("mod.u4_battery", "Akku-Sparmodus", "Battery Saver");
      put("mod.u4_fps30", "Leise-Modus", "Quiet Mode");
      put("mod.u4_unlimited", "FPS unbegrenzt", "Unlimited FPS");
      put("mod.u4_ultra", "Ultra-Leistung", "Ultra Performance");
      put(
         "mod.u4_ultra.desc",
         "Ein Klick für maximale FPS: keine Wolken, minimale Partikel, keine Schatten, kein Biom-Übergang, halbe Entity-Distanz, Sichtweite 8",
         "One click for max FPS: no clouds, minimal particles, no shadows, no biome blending, half entity distance, render distance 8"
      );
      put("mod.u4_balanced", "Ausgewogen", "Balanced");
      put(
         "mod.u4_balanced.desc",
         "Gute FPS bei schöner Grafik: schnelle Wolken, reduzierte Partikel, Sichtweite 10",
         "Good FPS with nice graphics: fast clouds, reduced particles, render distance 10"
      );
      put("ui.design.sub", "Thema, Farbe, HUD, UI-Größe und Animations-Tempo", "Theme, color, HUD, UI size and animation speed");
      put("ui.d.bg", "CLICK-GUI HINTERGRUND", "CLICK GUI BACKGROUND");
      put("ui.d.glass", "Glas-Deckkraft", "Glass Opacity");
      put("ui.d.line", "Leuchtende Linie oben am Fenster", "Glowing line at top of window");
      put("ui.d.bgint", "Animations-Stärke", "Animation Intensity");
      put("ui.d.nobg", "Kein Hintergrundbild", "No background image");
      put("ui.d.theme", "THEMA", "THEME");
      put("ui.d.accent", "AKZENTFARBE", "ACCENT COLOR");
      put("ui.d.hudstyle", "HUD-Stil", "HUD Style");
      put("ui.d.hudop", "Hintergrund-Deckkraft", "Background Opacity");
      put("ui.d.hudicons", "Icons in der HUD", "Icons in HUD");
      put("ui.d.shadow", "Schatten", "Shadows");
      put("ui.d.accentline", "Akzent-Linie links", "Accent line left");
      put("ui.d.menu", "MENÜ", "MENU");
      put("ui.d.sounds", "Klick-Sounds", "Click Sounds");
      put("ui.d.blur", "Hintergrund verschwimmen (Blur)", "Background blur");
      put("ui.d.uiscale", "UI-Größe", "UI Size");
      put("ui.d.animspeed", "Animations-Tempo", "Animation Speed");
      put("ui.d.anims", "Animationen", "Animations");
      put("ui.d.intro", "Start-Animation (Lego-Steine)", "Startup animation (Lego bricks)");
      put("ui.d.visorgui", "Menü-Taste öffnet den Visor (VR) - Shift + Taste: klassisch", "Menu key opens the Visor (VR) - Shift + key: classic");
      put("ui.d.xmas", "Weihnachts-Deko (Schnee, Lichter, Mützen)", "Christmas deco (snow, lights, hats)");
      put("s.Farbe", "Farbe", "Color");
      put("s.Stärke", "Stärke", "Strength");
      put("s.Größe", "Größe", "Size");
      put("s.Pulsieren", "Pulsieren", "Pulse");
      put("s.Dauer", "Dauer", "Duration");
      put("s.Aufstieg", "Aufstieg", "Rise Height");
      put("s.Deckkraft", "Deckkraft", "Opacity");
      put("s.Helligkeit", "Helligkeit", "Brightness");
      put("s.Bewegung", "Bewegung", "Motion");
      put("s.Modus", "Modus", "Mode");
      put("s.Nebel-Dichte", "Nebel-Dichte", "Fog Density");
      put("s.Nebel-Farbe", "Nebel-Farbe", "Fog Color");
      put("s.Nebel-Start", "Nebel-Start", "Fog Start");
      put("s.Ton", "Ton", "Sound");
      put("s.Anzahl", "Anzahl", "Count");
      put("s.Würfelgröße", "Würfelgröße", "Cube Size");
      put("s.Abstand", "Abstand", "Distance");
      put("s.Breite", "Breite", "Width");
      put("s.Höhe", "Höhe", "Height");
      put("s.Radius", "Radius", "Radius");
      put("s.Geschwindigkeit", "Geschwindigkeit", "Speed");
      put("s.Stufe", "Stufe", "Level");
      put("s.Wolken aus", "Wolken aus", "Clouds Off");
      put("s.Entity-Schatten aus", "Entity-Schatten aus", "Entity Shadows Off");
      put("s.View-Bobbing aus", "View-Bobbing aus", "View Bobbing Off");
      put("s.FOV-Effekte aus", "FOV-Effekte aus", "FOV Effects Off");
      put("s.Hinter Wänden nicht rendern", "Hinter Wänden nicht rendern", "Don't render behind walls");
      put("s.Items / XP bis", "Items / XP bis", "Items / XP up to");
      put("s.Mobs bis", "Mobs bis", "Mobs up to");
      put("s.Spieler immer rendern", "Spieler immer rendern", "Always render players");
      put("s.Max. neue pro Tick", "Max. neue pro Tick", "Max new per tick");
      put("s.FPS im Hintergrund", "FPS im Hintergrund", "FPS in background");
      put("s.RAM-Wächter", "RAM-Wächter", "RAM Guard");
      put("s.FPS", "FPS", "FPS");
      put("s.Ping", "Ping", "Ping");
      put("s.CPS", "CPS", "CPS");
      put("s.Uhrzeit", "Uhrzeit", "Clock");
      put("s.Spielzeit", "Spielzeit", "Session Time");
      put("s.Koordinaten", "Koordinaten", "Coordinates");
      put("s.Lego-Logo", "Lego-Logo", "Lego Logo");
      put("s.Sekunden", "Sekunden", "Seconds");
      put("s.12-Stunden (AM/PM)", "12-Stunden (AM/PM)", "12 Hours (AM/PM)");
      put("s.Nether-Umrechnung", "Nether-Umrechnung", "Nether Conversion");
      put("s.Richtung", "Richtung", "Direction");
      put("s.Hauptitem zeigen", "Hauptitem zeigen", "Show Main Item");
      put("s.Anordnung", "Anordnung", "Layout");
      put("s.Maustasten + CPS", "Maustasten + CPS", "Mouse Buttons + CPS");
      put("s.Leertaste", "Leertaste", "Space Bar");
      put("s.Nur Totem", "Nur Totem", "Only Totem");
      put("s.Füllung", "Füllung", "Fill");
      put("s.Kontur", "Kontur", "Outline");
      put("s.Partikel", "Partikel", "Particles");
      put("s.Spur-Partikel", "Spur-Partikel", "Trail Particles");
      put("s.Animation", "Animation", "Animation");
      put("s.Smooth", "Smooth", "Smooth");
      put("s.Intensität", "Intensität", "Intensity");
      put("s.Neigung", "Neigung", "Tilt");
      put("s.Sprint-Zoom", "Sprint-Zoom", "Sprint Zoom");
      put("s.Typ", "Typ", "Type");
      put("s.Selbst", "Selbst", "Self");
      put("s.Andere", "Andere", "Others");
      put("s.Regen", "Regen", "Rain");
      put("bgfx.0", "Keine", "None");
      put("bgfx.1", "Aurora", "Aurora");
      put("bgfx.2", "Partikel", "Particles");
      put("bgfx.3", "Sterne", "Stars");
      put("bgfx.4", "Wellen", "Waves");
      put("bgfx.5", "Bokeh", "Bokeh");
      put("bgfx.6", "Synthwave", "Synthwave");
      put("bgfx.7", "Schnee", "Snow");
      put("bgfx.8", "Nebel", "Nebula");
      put("bgfx.9", "Matrix", "Matrix");
      put("bgfx.10", "Warp 3D", "Warp 3D");
      put("bgfx.11", "Würfel 3D", "Cubes 3D");
      put("bgfx.12", "Bausteine 3D", "Bricks 3D");
      put("bgfx.13", "Tunnel 3D", "Tunnel 3D");
      put("theme.0", "Winterzauber", "Winter Magic");
      put("theme.1", "Standard", "Standard");
      put("theme.2", "Mitternacht", "Midnight");
      put("theme.3", "Glas", "Glass");
      put("theme.4", "Lila", "Purple");
      put("theme.5", "Ozean", "Ocean");
      put("theme.6", "Sunset", "Sunset");
      put("theme.7", "Weihnachten", "Christmas");
      put("theme.8", "Hell", "Light");
      put("color.0", "Grün", "Green");
      put("color.1", "Rot", "Red");
      put("color.2", "Gelb", "Yellow");
      put("color.3", "Blau", "Blue");
      put("color.4", "Hellblau", "Light Blue");
      put("color.5", "Lila", "Purple");
      put("color.6", "Pink", "Pink");
      put("color.7", "Orange", "Orange");
      put("color.8", "Weiß", "White");
      put("color.9", "Eis", "Ice");
      put("color.10", "Lime", "Lime");
      put("color.11", "Regenbogen", "Rainbow");
      put("key.none", "Keine", "None");
      put("key.mouse_l", "Maus L", "Mouse L");
      put("key.mouse_r", "Maus R", "Mouse R");
      put("key.mouse_m", "Maus M", "Mouse M");
      put("key.mouse_4", "Maus 4", "Mouse 4");
      put("key.mouse_5", "Maus 5", "Mouse 5");
      put("key.space", "Leertaste", "Space");
      put("key.lshift", "L-Shift", "L-Shift");
      put("key.rshift", "R-Shift", "R-Shift");
      put("key.lctrl", "L-Strg", "L-Ctrl");
      put("key.rctrl", "R-Strg", "R-Ctrl");
      put("key.lalt", "L-Alt", "L-Alt");
      put("key.ralt", "Alt Gr", "R-Alt");
      put("key.tab", "Tab", "Tab");
      put("key.caps", "Feststell", "Caps Lock");
      put("key.enter", "Enter", "Enter");
      put("key.right", "Rechts", "Right");
      put("key.left", "Links", "Left");
      put("key.down", "Runter", "Down");
      put("key.up", "Hoch", "Up");
      put("key.insert", "Einfg", "Insert");
      put("key.delete", "Entf", "Delete");
      put("key.home", "Pos1", "Home");
      put("key.end", "Ende", "End");
      put("key.pgup", "Bild hoch", "Page Up");
      put("key.pgdn", "Bild runter", "Page Down");
      put("key.unknown", "Taste", "Key");
      put("ui.style.0", "Standard", "Standard");
      put("ui.style.1", "Modern", "Modern");
   }
}
