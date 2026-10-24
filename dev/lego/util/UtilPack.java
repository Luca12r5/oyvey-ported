package dev.lego.util;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Sound;
import dev.lego.ui.Toasts;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ParticleStatus;

public final class UtilPack {
   private static final List<Module> MINE = new ArrayList<>();
   private static final Random RND = new Random();
   private static Set<String> lastTab = null;
   private static long lastMove = System.currentTimeMillis();
   private static double afkX = Double.NaN;
   private static double afkZ;
   private static boolean afk;
   static UtilPack.Flag timestamps;
   static UtilPack.Flag hideJoin;
   static UtilPack.Flag hideDeath;
   static UtilPack.Flag hideAdv;
   static UtilPack.Flag hideLinks;
   static UtilPack.Flag capsFilter;
   static UtilPack.Flag chatSound;
   static UtilPack.Flag pmSound;
   static UtilPack.Flag chatLog;
   static UtilPack.Flag emoji;
   static UtilPack.Flag capitalize;
   static UtilPack.Flag coordLog;
   static Setting.Text friends;
   static Module friendsMod;
   static Module joinLeave;
   static UtilPack.Command[] commands;
   static Module afkMod;
   static Module summaryMod;
   static Module alarmMod;
   static Setting.Text alarmTime;
   static final List<Object[]> TIMERS = new CopyOnWriteArrayList<>();
   private static long nextCoordLog;
   private static Set<String> lastTabFriends = null;
   private static final String[][] EMOJI = new String[][]{
      {":herz:", "❤"},
      {":stern:", "★"},
      {":haken:", "✔"},
      {":kreuz:", "✖"},
      {":pfeil:", "➜"},
      {":musik:", "♫"},
      {":sonne:", "☀"},
      {":smile:", "☺"},
      {":blitz:", "⚡"},
      {":schnee:", "❄"},
      {":krone:", "♛"},
      {":schwert:", "⚔"}
   };

   private UtilPack() {
   }

   private static <T extends Module> T reg(T m) {
      MINE.add(m);
      return Modules.register(m);
   }

   static void local(String s) {
      try {
         Mc.mc().gui.getChat().addMessage(Component.literal("[Lego] ").withStyle(ChatFormatting.GOLD).append(Component.literal(s)));
      } catch (Throwable var2) {
         LegoClient.LOG("chat: " + var2);
      }
   }

   private static String key(Component msg) {
      try {
         return msg.getContents() instanceof TranslatableContents tc ? tc.getKey() : "";
      } catch (Throwable var3) {
         return "";
      }
   }

   private static Path dir() {
      Path p = Mc.mc().gameDirectory.toPath().resolve("config").resolve("legoclient");

      try {
         Files.createDirectories(p);
      } catch (Exception var2) {
      }

      return p;
   }

   private static void append(String file, String line) {
      try {
         Files.writeString(dir().resolve(file), line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (Exception var3) {
         LegoClient.LOG(file + ": " + var3);
      }
   }

   private static LocalPlayer me() {
      return Mc.player();
   }

   private static Set<String> tab() {
      Set<String> s = new HashSet<>();

      for (PlayerInfo pi : Mc.mc().getConnection().getOnlinePlayers()) {
         s.add(pi.getProfile().name());
      }

      return s;
   }

   public static void registerAll() {
      joinLeave = reg(new Module("u4_joinleave", "Join/Leave-Anzeige", "Kleine Meldung, wenn Spieler den Server betreten oder verlassen", Category.UTILITY) {
         {
            this.icon("users");
            this.fresh();
         }

         @Override
         public void tick() {
            UtilPack.tabDiff(false);
         }

         @Override
         public void onDisable() {
            UtilPack.lastTab = null;
         }
      });
      friendsMod = reg(new Module("u4_friends", "Freunde-Meldung", "Meldet, wenn deine Freunde online kommen (Namen in den Einstellungen)", Category.UTILITY) {
         {
            this.icon("heart");
            this.fresh();
            UtilPack.friends = this.add(new Setting.Text("Freunde", "", 200, "Name1, Name2, ..."));
         }

         @Override
         public void tick() {
            UtilPack.tabDiff(true);
         }
      });
      timestamps = reg(new UtilPack.Flag("timestamps", "Chat-Zeitstempel", "Zeigt die Uhrzeit vor jeder Chat-Nachricht", "clock"));
      hideJoin = reg(new UtilPack.Flag("hidejoin", "Beitritte ausblenden", "Blendet \"... hat das Spiel betreten/verlassen\" aus", "users"));
      hideDeath = reg(new UtilPack.Flag("hidedeath", "Todesnachrichten ausblenden", "Blendet die Todesnachrichten anderer Spieler aus", "skull"));
      hideAdv = reg(new UtilPack.Flag("hideadv", "Erfolge ausblenden", "Blendet \"... hat den Fortschritt erzielt\" aus", "trophy"));
      hideLinks = reg(new UtilPack.Flag("hidelinks", "Link-Filter", "Blendet Chat-Nachrichten mit Links (Werbung) aus", "filter"));
      capsFilter = reg(new UtilPack.Flag("caps", "Schrei-Filter", "Zeigt Nachrichten in GROSSBUCHSTABEN klein an", "mute"));
      chatSound = reg(new UtilPack.Flag("chatsound", "Chat-Ton", "Leiser Ton bei jeder Chat-Nachricht", "sound"));
      pmSound = reg(new UtilPack.Flag("pmsound", "Privatnachricht-Ton", "Ton + Meldung bei privaten Nachrichten (/msg)", "chat"));
      chatLog = reg(new UtilPack.Flag("chatlog", "Chat-Protokoll", "Speichert den Chat in config/legoclient/chat.log", "clipboard"));
      emoji = reg(new UtilPack.Flag("emoji", "Emoji-Ersetzer", "Ersetzt :herz: :stern: :haken: :pfeil: :musik: :sonne: :smile: in deinen Nachrichten", "emote"));
      capitalize = reg(new UtilPack.Flag("capitalize", "Auto-Großschreibung", "Macht den ersten Buchstaben deiner Nachrichten groß", "chat"));
      commands = new UtilPack.Command[]{
         reg(new UtilPack.Command("calc", "Rechner", "Tippe .rechne 12*(3+4) in den Chat - das Ergebnis siehst nur du", "number", ".rechne <Aufgabe>", a -> {
            double r = new UtilPack.Calc(a).parse();
            return a.trim() + " = " + (r == Math.rint(r) && Math.abs(r) < 1.0E15 ? String.valueOf((long)r) : String.format(Locale.ROOT, "%.6g", r));
         }, "rechne", "calc")),
         reg(new UtilPack.Command("dice", "Würfel", ".würfel oder .würfel 20 - würfelt eine Zahl (nur für dich)", "gamepad", ".würfel [Seiten]", a -> {
            int s = a.isBlank() ? 6 : Math.max(2, Math.min(1000, Integer.parseInt(a.trim())));
            return "W" + s + ": " + (1 + RND.nextInt(s));
         }, "würfel", "wuerfel", "dice")),
         reg(
            new UtilPack.Command(
               "coin", "Münzwurf", ".münze - Kopf oder Zahl", "gem", ".münze", a -> RND.nextBoolean() ? "Kopf" : "Zahl", "münze", "muenze", "coin"
            )
         ),
         reg(new UtilPack.Command("random", "Zufallszahl", ".zufall 1 100 - Zufallszahl im Bereich", "number", ".zufall <von> <bis>", a -> {
            String[] p = a.trim().split("\\s+");
            int lo = Integer.parseInt(p[0]);
            int hi = Integer.parseInt(p[1]);
            if (hi < lo) {
               int x = lo;
               lo = hi;
               hi = x;
            }

            return "Zufall " + lo + "-" + hi + ": " + (lo + RND.nextInt(hi - lo + 1));
         }, "zufall", "random")),
         reg(new UtilPack.Command("choose", "Entscheider", ".wähle Pizza, Nudeln, Döner - wählt zufällig eins aus", "star", ".wähle a, b, c", a -> {
            String[] p = a.split(",");
            return "Gewählt: " + p[RND.nextInt(p.length)].trim();
         }, "wähle", "waehle", "choose")),
         reg(
            new UtilPack.Command(
               "nether", "Portal-Rechner", ".nether x z oder .oberwelt x z - rechnet Portal-Koordinaten um", "compass", ".nether <x> <z>", a -> {
                  String[] p = a.trim().split("\\s+");
                  double x = Double.parseDouble(p[0]);
                  double z = Double.parseDouble(p[1]);
                  return "Nether: " + Math.round(x / 8.0) + " " + Math.round(z / 8.0) + "  ·  Oberwelt: " + Math.round(x * 8.0) + " " + Math.round(z * 8.0);
               }, "nether", "oberwelt", "portal"
            )
         ),
         reg(
            new UtilPack.Command(
               "dist", "Entfernungs-Rechner", ".dist x z - wie weit ein Ort von dir weg ist und in welche Richtung", "waypoint", ".dist <x> <z>", a -> {
                  String[] p = a.trim().split("\\s+");
                  double x = Double.parseDouble(p[0]);
                  double z = Double.parseDouble(p[1]);
                  double dx = x - Mc.x(me());
                  double dz = z - Mc.z(me());
                  String[] dirs = new String[]{"Süden", "Südwesten", "Westen", "Nordwesten", "Norden", "Nordosten", "Osten", "Südosten"};
                  double ang = Math.toDegrees(Math.atan2(-dx, dz));
                  return Math.round(Math.hypot(dx, dz)) + " Blöcke Richtung " + dirs[Math.floorMod(Math.round(ang / 45.0), 8)];
               }, "dist", "entfernung"
            )
         ),
         reg(new UtilPack.Command("note", "Notizen", ".notiz Text speichert eine Notiz, .notizen zeigt alle", "bookmark", ".notiz <Text>", a -> {
            if (a.isBlank()) {
               return "Benutzung: .notiz <Text>";
            } else {
               append("notes.txt", LocalDate.now() + " " + LocalTime.now().withNano(0) + "  " + a.trim());
               return "Notiz gespeichert";
            }
         }, "notiz", "note")),
         reg(new UtilPack.Command("notes", "Notizen anzeigen", ".notizen - zeigt deine letzten Notizen", "bookmark", ".notizen", a -> {
            Path f = dir().resolve("notes.txt");
            if (!Files.exists(f)) {
               return "Noch keine Notizen";
            } else {
               List<String> l = Files.readAllLines(f, StandardCharsets.UTF_8);
               StringBuilder sb = new StringBuilder("Notizen:");

               for (int i = Math.max(0, l.size() - 8); i < l.size(); i++) {
                  sb.append("\n  ").append(l.get(i));
               }

               return sb.toString();
            }
         }, "notizen", "notes")),
         reg(
            new UtilPack.Command("remind", "Erinnerung", ".erinnere 10 Ofen leeren - erinnert dich in 10 Minuten", "bell", ".erinnere <Minuten> <Text>", a -> {
               String[] p = a.trim().split("\\s+", 2);
               double min = Double.parseDouble(p[0].replace(',', '.'));
               String text = p.length > 1 ? p[1] : "Erinnerung";
               TIMERS.add(new Object[]{System.currentTimeMillis() + (long)(min * 60000.0), text});
               return "Ich erinnere dich in " + p[0] + " min: " + text;
            }, "erinnere", "remind")
         ),
         reg(new UtilPack.Command("countdown", "Countdown", ".countdown 30 - zählt 30 Sekunden runter", "hourglass", ".countdown <Sekunden>", a -> {
            int s = Math.max(1, Math.min(36000, Integer.parseInt(a.trim())));
            TIMERS.add(new Object[]{System.currentTimeMillis() + s * 1000L, "Countdown " + s + " s ist abgelaufen!"});
            return "Countdown läuft: " + s + " s";
         }, "countdown", "timer")),
         reg(
            new UtilPack.Command(
               "time",
               "Zeit-Befehl",
               ".zeit - zeigt echte Uhrzeit und Minecraft-Uhrzeit",
               "clock",
               ".zeit",
               a -> {
                  long t = (Mc.world().getDayTime() + 6000L) % 24000L;
                  return "Echt: "
                     + LocalTime.now().withNano(0)
                     + "  ·  Minecraft: "
                     + String.format(Locale.ROOT, "%02d:%02d", t / 1000L, t % 1000L * 60L / 1000L)
                     + "  ·  Tag "
                     + (Mc.world().getDayTime() / 24000L + 1L);
               },
               "zeit",
               "time"
            )
         ),
         reg(new UtilPack.Command("pos", "Position-Befehl", ".pos - zeigt deine Position (nur für dich) und kopiert sie", "pin", ".pos", a -> {
            String s = (int)Math.floor(Mc.x(me())) + " " + (int)Math.floor(Mc.y(me())) + " " + (int)Math.floor(Mc.z(me()));
            Mc.clipboard(s);
            return "Position " + s + " (kopiert)";
         }, "pos", "position")),
         reg(new UtilPack.Command("help", "Lego-Befehle", ".lego - zeigt alle Lego-Chat-Befehle", "info", ".lego", a -> {
            StringBuilder sb = new StringBuilder("Befehle (nur für dich sichtbar):");

            for (UtilPack.Command c : commands) {
               if (c.enabled && c.cmd != null && !c.id.equals("u4_help")) {
                  sb.append("\n  ").append(c.usage).append(" - ").append(c.name);
               }
            }

            return sb.toString();
         }, "lego", "befehle"))
      };
      alarmMod = reg(new Module("u4_alarm", "Wecker", "Klingelt zur eingestellten echten Uhrzeit (z. B. 18:30)", Category.UTILITY) {
         private String fired = "";

         {
            this.icon("bell");
            this.fresh();
            UtilPack.alarmTime = this.add(new Setting.Text("Uhrzeit", "18:00", 5, "HH:MM"));
         }

         @Override
         public void tick() {
            String now = String.format(Locale.ROOT, "%02d:%02d", LocalTime.now().getHour(), LocalTime.now().getMinute());
            if (now.equals(UtilPack.alarmTime.get().trim()) && !this.fired.equals(now)) {
               this.fired = now;
               Toasts.show("bell", "Wecker", "Es ist " + now, -15043, 10000L);

               for (int i = 0; i < 3; i++) {
                  Sound.play("minecraft:block.note_block.bell", 1.0F + i * 0.2F, 0.9F);
               }
            }
         }
      });
      afkMod = reg(
         new Module("u4_afk", "AFK-Erkennung", "Merkt, wenn du nicht am PC warst, und sagt dir danach, wie lange", Category.UTILITY) {
            final Setting.Num after = this.add(new Setting.Num("AFK nach", 1.0, 30.0, 1.0, 3.0, " min"));

            {
               this.icon("hourglass");
               this.fresh();
            }

            @Override
            public void tick() {
               LocalPlayer p = UtilPack.me();
               if (p != null) {
                  double x = Mc.x(p);
                  double z = Mc.z(p);
                  boolean moved = Double.isNaN(UtilPack.afkX)
                     || Math.abs(x - UtilPack.afkX) > 0.05
                     || Math.abs(z - UtilPack.afkZ) > 0.05
                     || Mc.mouseDown(0)
                     || Mc.mouseDown(1);
                  UtilPack.afkX = x;
                  UtilPack.afkZ = z;
                  long now = System.currentTimeMillis();
                  if (moved) {
                     if (UtilPack.afk) {
                        UtilPack.afk = false;
                        Toasts.show("user", "Willkommen zurück", "Du warst " + Math.max(1L, (now - UtilPack.lastMove) / 60000L) + " min AFK", -12987565, 4000L);
                     }

                     UtilPack.lastMove = now;
                  } else if (!UtilPack.afk && now - UtilPack.lastMove > this.after.get() * 60000.0) {
                     UtilPack.afk = true;
                  }
               }
            }
         }
      );
      coordLog = reg(
         new UtilPack.Flag(
            "coordlog", "Weg-Protokoll", "Speichert alle 2 Minuten deine Koordinaten in config/legoclient/wege.txt (damit du zurückfindest)", "trail"
         )
      );
      summaryMod = reg(
         new Module("u4_summary", "Sitzungs-Bericht", "Zeigt beim Verlassen einer Welt, wie weit du gelaufen bist und wie lange du da warst", Category.UTILITY) {
            private long joined;
            private double dist;
            private double lx = Double.NaN;
            private double lz;

            {
               this.icon("list");
               this.fresh();
            }

            @Override
            public void tick() {
               LocalPlayer p = UtilPack.me();
               if (p == null) {
                  if (this.joined != 0L) {
                     Toasts.show(
                        "list",
                        "Sitzung beendet",
                        (System.currentTimeMillis() - this.joined) / 60000L + " min · " + Math.round(this.dist) + " Blöcke gelaufen",
                        -12604929,
                        6000L
                     );
                     this.joined = 0L;
                     this.dist = 0.0;
                     this.lx = Double.NaN;
                  }
               } else {
                  if (this.joined == 0L) {
                     this.joined = System.currentTimeMillis();
                  }

                  double x = Mc.x(p);
                  double z = Mc.z(p);
                  if (!Double.isNaN(this.lx)) {
                     double d = Math.hypot(x - this.lx, z - this.lz);
                     if (d < 20.0) {
                        this.dist += d;
                     }
                  }

                  this.lx = x;
                  this.lz = z;
               }
            }
         }
      );
      reg(
         new UtilPack.Opt<>(
            "sniper",
            "Sniper-Maus",
            "Sehr langsame Maus zum genauen Zielen (z. B. auf eine Taste legen)",
            "crosshair",
            Category.UTILITY,
            () -> Mc.options().sensitivity(),
            0.12,
            0.5
         )
      );
      reg(
         new UtilPack.Opt<>(
            "fastmouse", "Schnelle Maus", "Höhere Mausempfindlichkeit, solange an", "mouse", Category.UTILITY, () -> Mc.options().sensitivity(), 0.85, 0.5
         )
      );
      reg(
         new UtilPack.Opt<>(
            "farview", "Weitsicht", "Sichtweite auf 24 Chunks, solange an", "eye", Category.VISUALS, () -> Mc.options().renderDistance(), 24, 12
         )
      );
      reg(
         new UtilPack.Opt<>(
            "nearview", "Nahsicht", "Sichtweite auf 6 Chunks (spart viel Leistung)", "eye", Category.PERFORMANCE, () -> Mc.options().renderDistance(), 6, 12
         )
      );
      reg(
         new UtilPack.Opt<>(
            "nobob", "Kein Wackeln beim Laufen", "Schaltet das Kopf-Wippen beim Laufen aus", "run", Category.VISUALS, () -> Mc.options().bobView(), false, true
         )
      );
      reg(new UtilPack.Opt<>("widefov", "Weites Sichtfeld", "Sichtfeld 100, solange an", "eye", Category.VISUALS, () -> Mc.options().fov(), 100, 70));
      reg(
         new UtilPack.Opt<>("quakefov", "Quake-Sichtfeld", "Sichtfeld 110 für maximalen Überblick", "eye", Category.VISUALS, () -> Mc.options().fov(), 110, 70)
      );
      reg(
         new UtilPack.Opt<>(
            "nofoveffect",
            "Kein Sprint-Zoom",
            "Kein FOV-Wechsel beim Sprinten und durch Effekte",
            "speed",
            Category.VISUALS,
            () -> Mc.options().fovEffectScale(),
            0.0,
            1.0
         )
      );
      reg(
         new UtilPack.Opt<>(
            "noscreenfx",
            "Keine Bildschirm-Verzerrung",
            "Kein Wabern durch Übelkeit oder Portale",
            "visuals",
            Category.VISUALS,
            () -> Mc.options().screenEffectScale(),
            0.0,
            1.0
         )
      );
      reg(new Module("u4_cinematic", "Kino-Kamera", "Weiche, filmische Kamerabewegung (gut für Videos)", Category.VISUALS) {
         {
            this.icon("camera");
            this.fresh();
         }

         @Override
         public void tick() {
            if (Mc.options() != null) {
               Mc.options().smoothCamera = true;
            }
         }

         @Override
         public void onDisable() {
            if (Mc.options() != null) {
               Mc.options().smoothCamera = false;
            }
         }
      });
      reg(
         new UtilPack.Opt<>(
            "vsync",
            "VSync",
            "Kein Bildreißen (Tearing), FPS an den Monitor gebunden",
            "fps",
            Category.PERFORMANCE,
            () -> Mc.options().enableVsync(),
            true,
            false
         )
      );
      reg(
         new UtilPack.Opt<>(
            "noshadows",
            "Entity-Schatten aus",
            "Keine runden Schatten unter Mobs (mehr FPS)",
            "boost",
            Category.PERFORMANCE,
            () -> Mc.options().entityShadows(),
            false,
            true
         )
      );
      reg(new UtilPack.Opt<>("bright", "Helligkeit hell", "Helligkeit auf \"Hell\", solange an", "sun", Category.VISUALS, () -> Mc.options().gamma(), 1.0, 0.5));
      UtilPack.Opt<CloudStatus> cloudsOff = new UtilPack.Opt<>(
         "clouds", "Wolken aus", "Keine Wolken (mehr FPS)", "cloud", Category.PERFORMANCE, () -> Mc.options().cloudStatus(), CloudStatus.OFF, CloudStatus.FANCY
      );
      UtilPack.Opt<ParticleStatus> partMin = new UtilPack.Opt<>(
         "particlesmin",
         "Partikel minimal",
         "Nur die nötigsten Partikel (viel mehr FPS bei Explosionen)",
         "particles",
         Category.PERFORMANCE,
         () -> Mc.options().particles(),
         ParticleStatus.MINIMAL,
         ParticleStatus.ALL
      );
      UtilPack.Opt<Boolean> shadowsOff = new UtilPack.Opt<>(
         "noshadows2", "-", "-", "boost", Category.PERFORMANCE, () -> Mc.options().entityShadows(), false, true
      );
      UtilPack.Opt<Integer> blendOff = new UtilPack.Opt<>(
         "noblend",
         "Biom-Übergang aus",
         "Keine weichen Farbübergänge zwischen Biomen (schnelleres Laden)",
         "biome",
         Category.PERFORMANCE,
         () -> Mc.options().biomeBlendRadius(),
         0,
         2
      );
      UtilPack.Opt<Double> entDist = new UtilPack.Opt<>(
         "entitydist",
         "Entity-Distanz 50 %",
         "Mobs werden nur halb so weit angezeigt (mehr FPS)",
         "entity",
         Category.PERFORMANCE,
         () -> Mc.options().entityDistanceScaling(),
         0.5,
         1.0
      );
      UtilPack.Opt<Integer> render8 = new UtilPack.Opt<>("render8", "-", "-", "eye", Category.PERFORMANCE, () -> Mc.options().renderDistance(), 8, 12);
      reg(cloudsOff);
      reg(
         new UtilPack.Opt<>(
            "cloudsfast",
            "Schnelle Wolken",
            "Einfache flache Wolken statt 3D-Wolken",
            "cloud",
            Category.PERFORMANCE,
            () -> Mc.options().cloudStatus(),
            CloudStatus.FAST,
            CloudStatus.FANCY
         )
      );
      reg(partMin);
      reg(
         new UtilPack.Opt<>(
            "particlesless",
            "Partikel reduziert",
            "Weniger Partikel (guter Mittelweg)",
            "particles",
            Category.PERFORMANCE,
            () -> Mc.options().particles(),
            ParticleStatus.DECREASED,
            ParticleStatus.ALL
         )
      );
      reg(blendOff);
      reg(
         new UtilPack.Opt<>(
            "noao",
            "Weiches Licht aus",
            "Keine weichen Schatten an Blöcken (mehr FPS auf schwachen PCs)",
            "light",
            Category.PERFORMANCE,
            () -> Mc.options().ambientOcclusion(),
            false,
            true
         )
      );
      reg(entDist);
      reg(
         new UtilPack.Opt<>(
            "simdist",
            "Simulation 5 Chunks",
            "Weniger simulierte Chunks im Einzelspieler (weniger Ruckler)",
            "cpu",
            Category.PERFORMANCE,
            () -> Mc.options().simulationDistance(),
            5,
            12
         )
      );
      reg(
         new UtilPack.Opt<>(
            "battery",
            "Akku-Sparmodus",
            "FPS auf 60 begrenzt - Laptop bleibt kühler und leiser",
            "boost",
            Category.PERFORMANCE,
            () -> Mc.options().framerateLimit(),
            60,
            120
         )
      );
      reg(
         new UtilPack.Opt<>(
            "fps30",
            "Leise-Modus",
            "FPS auf 30 begrenzt - für AFK-Farmen oder sehr schwache PCs",
            "boost",
            Category.PERFORMANCE,
            () -> Mc.options().framerateLimit(),
            30,
            120
         )
      );
      reg(
         new UtilPack.Opt<>(
            "unlimited",
            "FPS unbegrenzt",
            "Hebt das FPS-Limit auf (maximale Bildrate)",
            "fps",
            Category.PERFORMANCE,
            () -> Mc.options().framerateLimit(),
            260,
            120
         )
      );
      reg(
         new UtilPack.Preset(
            "ultra",
            "Ultra-Leistung",
            "Ein Klick für maximale FPS: keine Wolken, minimale Partikel, keine Schatten, kein Biom-Übergang, halbe Entity-Distanz, Sichtweite 8",
            cloudsOff,
            partMin,
            shadowsOff,
            blendOff,
            entDist,
            render8
         )
      );
      reg(
         new UtilPack.Preset(
            "balanced",
            "Ausgewogen",
            "Gute FPS bei schöner Grafik: schnelle Wolken, reduzierte Partikel, Sichtweite 10",
            new UtilPack.Opt<>("cloudsfast2", "-", "-", "cloud", Category.PERFORMANCE, () -> Mc.options().cloudStatus(), CloudStatus.FAST, CloudStatus.FANCY),
            new UtilPack.Opt<>(
               "particlesless2", "-", "-", "particles", Category.PERFORMANCE, () -> Mc.options().particles(), ParticleStatus.DECREASED, ParticleStatus.ALL
            ),
            new UtilPack.Opt<>("render10", "-", "-", "eye", Category.PERFORMANCE, () -> Mc.options().renderDistance(), 10, 12)
         )
      );
      reg(new Module("u4_copycoords", "Koordinaten kopieren", "Kopiert deine XYZ-Koordinaten in die Zwischenablage (einmal drücken)", Category.UTILITY) {
         {
            this.icon("compass");
            this.fresh();
         }

         @Override
         public boolean isToggleable() {
            return false;
         }

         @Override
         public void onEnable() {
            LocalPlayer p = UtilPack.me();
            if (p == null) {
               this.enabled = false;
            } else {
               String s = Math.round(Mc.x(p)) + " " + Math.round(Mc.y(p)) + " " + Math.round(Mc.z(p));

               try {
                  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
               } catch (Throwable var4) {
               }

               Toasts.show("compass", "Koordinaten kopiert", s, -12604929, 2500L);
               this.enabled = false;
            }
         }
      });
      reg(new Module("u4_deathcoords", "Todes-Koordinaten", "Merkt sich, wo du gestorben bist, und zeigt es als Toast", Category.UTILITY) {
         double dx = Double.NaN;
         double dy;
         double dz;
         String dim = "";

         {
            this.icon("skull");
            this.fresh();
         }

         @Override
         public void tick() {
            LocalPlayer p = UtilPack.me();
            if (p != null) {
               if (p.isDeadOrDying() && !Double.isNaN(this.dx)) {
                  String d = this.dim.contains("nether") ? " (Nether)" : (this.dim.contains("end") ? " (End)" : "");
                  Toasts.show("skull", "Gestorben bei", Math.round(this.dx) + " " + Math.round(this.dy) + " " + Math.round(this.dz) + d, -45730, 8000L);
                  this.dx = Double.NaN;
               }

               if (!p.isDeadOrDying()) {
                  this.dx = Mc.x(p);
                  this.dy = Mc.y(p);
                  this.dz = Mc.z(p);
                  this.dim = Mc.world() != null ? Mc.world().dimension().identifier().toString() : "";
               }
            }
         }
      });
      reg(new Module("u4_chunkborder", "Chunk-Grenzen", "Zeigt die Chunk-Grenzen als grüne Linien um dich herum (wie F3+G, aber hübscher)", Category.UTILITY) {
         {
            this.icon("grid");
            this.fresh();
         }
      });
      registerEvents();
   }

   public static void tick() {
      long now = System.currentTimeMillis();

      for (Object[] t : TIMERS) {
         if (now >= (Long)t[0]) {
            TIMERS.remove(t);
            Toasts.show("bell", "Erinnerung", (String)t[1], -15043, 10000L);
            Sound.play("minecraft:block.note_block.bell", 1.2F, 0.9F);
            local("⏰ " + t[1]);
         }
      }

      if (coordLog != null && coordLog.enabled && me() != null && now >= nextCoordLog) {
         nextCoordLog = now + 120000L;
         append(
            "wege.txt",
            LocalDate.now()
               + " "
               + LocalTime.now().withNano(0)
               + "  "
               + (int)Math.floor(Mc.x(me()))
               + " "
               + (int)Math.floor(Mc.y(me()))
               + " "
               + (int)Math.floor(Mc.z(me()))
               + "  "
               + Utility.dimName(Utility.dimension())
               + "  "
               + Mc.serverAddress()
         );
      }
   }

   private static void tabDiff(boolean friendsOnly) {
      try {
         if (Mc.mc().getConnection() == null) {
            if (friendsOnly) {
               lastTabFriends = null;
            } else {
               lastTab = null;
            }

            return;
         }

         Set<String> now = tab();
         Set<String> before = friendsOnly ? lastTabFriends : lastTab;
         if (before != null) {
            if (friendsOnly) {
               Set<String> fr = new HashSet<>();

               for (String s : friends.get().split(",")) {
                  if (!s.isBlank()) {
                     fr.add(s.trim().toLowerCase(Locale.ROOT));
                  }
               }

               for (String n : now) {
                  if (!before.contains(n) && fr.contains(n.toLowerCase(Locale.ROOT))) {
                     Toasts.show("heart", "Freund online", n + " ist jetzt online", -45149, 5000L);
                     Sound.play("minecraft:block.note_block.pling", 1.6F, 0.8F);
                  }
               }
            } else {
               int shown = 0;

               for (String nx : now) {
                  if (!before.contains(nx) && shown++ < 3) {
                     Toasts.show("users", "Beigetreten", nx, -12987565, 2000L);
                  }
               }

               for (String nxx : before) {
                  if (!now.contains(nxx) && shown++ < 3) {
                     Toasts.show("users", "Verlassen", nxx, -6642766, 2000L);
                  }
               }
            }
         }

         if (friendsOnly) {
            lastTabFriends = now;
         } else {
            lastTab = now;
         }
      } catch (Throwable var8) {
         if (friendsOnly) {
            lastTabFriends = null;
         } else {
            lastTab = null;
         }
      }
   }

   private static void registerEvents() {
      ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> overlay || receive(msg, true));
      ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, ts) -> receive(msg, false));
      ClientSendMessageEvents.ALLOW_CHAT.register(UtilPack::command);
      ClientSendMessageEvents.MODIFY_CHAT.register(UtilPack::modify);
   }

   private static boolean receive(Component msg, boolean game) {
      try {
         String k = key(msg);
         String s = msg.getString();
         if (hideJoin.enabled && (k.startsWith("multiplayer.player.joined") || k.equals("multiplayer.player.left"))) {
            return false;
         }

         if (hideDeath.enabled && k.startsWith("death.")) {
            return false;
         }

         if (hideAdv.enabled && k.startsWith("chat.type.advancement")) {
            return false;
         }

         if (hideLinks.enabled && !game && (s.contains("http://") || s.contains("https://") || s.matches(".*\\b[a-z0-9-]+\\.(de|com|net|gg|eu|org|io)\\b.*"))) {
            return false;
         }

         if (chatLog.enabled) {
            append("chat.log", LocalTime.now().withNano(0) + " " + s);
         }

         if (chatSound.enabled && !game) {
            Sound.play("minecraft:block.note_block.hat", 1.6F, 0.25F);
         }

         String low = s.toLowerCase(Locale.ROOT);
         if (pmSound.enabled
            && (
               low.contains("whispers to you")
                  || low.contains("flüstert dir")
                  || low.contains("-> mir")
                  || low.contains("-> me")
                  || k.startsWith("commands.message.display.incoming")
            )) {
            Sound.play("minecraft:entity.experience_orb.pickup", 1.4F, 0.8F);
            Toasts.show("chat", "Privatnachricht", s.length() > 80 ? s.substring(0, 80) + "…" : s, -12604929, 4000L);
         }

         boolean caps = capsFilter.enabled
            && !game
            && s.length() >= 8
            && s.chars().filter(Character::isLetter).count() >= 6L
            && s.equals(s.toUpperCase(Locale.ROOT))
            && !s.equals(low);
         if (timestamps.enabled || caps) {
            Utility.onMessage(msg, false);
            Component body = (Component)(caps ? Component.literal(low) : msg);
            Component out = (Component)(timestamps.enabled
               ? Component.literal(String.format(Locale.ROOT, "[%02d:%02d] ", LocalTime.now().getHour(), LocalTime.now().getMinute()))
                  .withStyle(ChatFormatting.DARK_GRAY)
                  .append(body)
               : body);
            Mc.mc().gui.getChat().addMessage(out);
            return false;
         }
      } catch (Throwable var8) {
         LegoClient.LOG("Chat-Paket: " + var8);
      }

      return true;
   }

   private static boolean command(String msg) {
      if (msg.startsWith(".") && msg.length() >= 2) {
         String body = msg.substring(1);
         String name = body.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
         String args = body.contains(" ") ? body.substring(body.indexOf(32) + 1) : "";

         for (UtilPack.Command c : commands) {
            if (c.enabled) {
               for (String n : c.names) {
                  if (n.equals(name)) {
                     String r;
                     try {
                        r = c.cmd.run(args);
                     } catch (Throwable var17) {
                        r = "Benutzung: " + c.usage;
                     }

                     for (String line : r.split("\n")) {
                        local(line);
                     }

                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private static String modify(String msg) {
      String s = msg;
      if (emoji.enabled) {
         for (String[] e : EMOJI) {
            s = s.replace(e[0], e[1]);
         }
      }

      if (capitalize.enabled && !s.isEmpty() && !s.startsWith("/") && Character.isLowerCase(s.charAt(0))) {
         s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
      }

      return s;
   }

   static final class Calc {
      private final String s;
      private int i;

      Calc(String s) {
         this.s = s.replace(" ", "").replace(',', '.').replace('x', '*').replace(':', '/').toLowerCase(Locale.ROOT);
      }

      double parse() {
         double v = this.expr();
         if (this.i != this.s.length()) {
            throw new IllegalArgumentException("Rest: " + this.s.substring(this.i));
         } else {
            return v;
         }
      }

      private double expr() {
         double v = this.term();

         while (this.i < this.s.length()) {
            char c = this.s.charAt(this.i);
            if (c == '+') {
               this.i++;
               v += this.term();
            } else {
               if (c != '-') {
                  break;
               }

               this.i++;
               v -= this.term();
            }
         }

         return v;
      }

      private double term() {
         double v = this.pow();

         while (this.i < this.s.length()) {
            char c = this.s.charAt(this.i);
            if (c == '*') {
               this.i++;
               v *= this.pow();
            } else if (c == '/') {
               this.i++;
               v /= this.pow();
            } else {
               if (c != '%') {
                  break;
               }

               this.i++;
               v %= this.pow();
            }
         }

         return v;
      }

      private double pow() {
         double v = this.unary();
         if (this.i < this.s.length() && this.s.charAt(this.i) == '^') {
            this.i++;
            v = Math.pow(v, this.pow());
         }

         return v;
      }

      private double unary() {
         if (this.i < this.s.length() && this.s.charAt(this.i) == '-') {
            this.i++;
            return -this.unary();
         } else if (this.i < this.s.length() && this.s.charAt(this.i) == '+') {
            this.i++;
            return this.unary();
         } else {
            return this.atom();
         }
      }

      private double atom() {
         if (this.s.startsWith("sqrt", this.i) || this.s.startsWith("wurzel", this.i)) {
            this.i = this.i + (this.s.startsWith("sqrt", this.i) ? 4 : 6);
            return Math.sqrt(this.atom());
         } else if (this.s.startsWith("pi", this.i)) {
            this.i += 2;
            return Math.PI;
         } else if (this.i < this.s.length() && this.s.charAt(this.i) == '(') {
            this.i++;
            double v = this.expr();
            if (this.i < this.s.length() && this.s.charAt(this.i) == ')') {
               this.i++;
               return v;
            } else {
               throw new IllegalArgumentException(")");
            }
         } else {
            int st = this.i;

            while (this.i < this.s.length() && (Character.isDigit(this.s.charAt(this.i)) || this.s.charAt(this.i) == '.')) {
               this.i++;
            }

            if (st == this.i) {
               throw new IllegalArgumentException("Zahl erwartet");
            } else {
               return Double.parseDouble(this.s.substring(st, this.i));
            }
         }
      }
   }

   interface Cmd {
      String run(String var1) throws Exception;
   }

   static final class Command extends Module {
      final String[] names;
      final UtilPack.Cmd cmd;
      final String usage;

      Command(String id, String name, String desc, String icon, String usage, UtilPack.Cmd cmd, String... names) {
         super("u4_" + id, name, desc, Category.UTILITY);
         this.icon(icon);
         this.fresh();
         this.enabled = true;
         this.names = names;
         this.cmd = cmd;
         this.usage = usage;
      }
   }

   static final class Flag extends Module {
      Flag(String id, String name, String desc, String icon) {
         super("u4_" + id, name, desc, Category.UTILITY);
         this.icon(icon);
         this.fresh();
      }
   }

   static final class Opt<T> extends Module {
      private static final Map<Object, ArrayDeque<UtilPack.Opt<?>>> ACTIVE = new IdentityHashMap<>();
      private static final Map<Object, Object> ORIGINAL = new IdentityHashMap<>();
      private final Supplier<OptionInstance<T>> opt;
      private final T on;
      private final T fallback;
      private boolean applied;

      Opt(String id, String name, String desc, String icon, Category cat, Supplier<OptionInstance<T>> opt, T on, T fallback) {
         super("u4_" + id, name, desc, cat);
         this.icon(icon);
         this.fresh();
         this.opt = opt;
         this.on = on;
         this.fallback = fallback;
      }

      @Override
      public void tick() {
         this.apply();
      }

      @Override
      public void onEnable() {
         this.apply();
      }

      private void apply() {
         if (!this.applied && Mc.options() != null) {
            try {
               OptionInstance<T> o = this.opt.get();
               ArrayDeque<UtilPack.Opt<?>> st = ACTIVE.computeIfAbsent(o, k -> new ArrayDeque<>());
               if (st.isEmpty()) {
                  ORIGINAL.put(o, o.get());
               }

               st.remove(this);
               st.push(this);
               o.set(this.on);
               this.applied = true;
            } catch (Throwable var3) {
               LegoClient.LOG(this.id + ": " + var3);
            }
         }
      }

      @Override
      public void onDisable() {
         if (this.applied && Mc.options() != null) {
            this.applied = false;

            try {
               OptionInstance<T> o = this.opt.get();
               ArrayDeque<UtilPack.Opt<?>> st = ACTIVE.get(o);
               if (st != null) {
                  st.remove(this);
               }

               if (st != null && !st.isEmpty()) {
                  o.set(st.peek().on);
               } else {
                  Object orig = ORIGINAL.remove(o);
                  o.set((T)(orig != null ? orig : this.fallback));
               }
            } catch (Throwable var4) {
               LegoClient.LOG(this.id + ": " + var4);
            }
         } else {
            this.applied = false;
         }
      }
   }

   static final class Preset extends Module {
      private final UtilPack.Opt<?>[] parts;

      Preset(String id, String name, String desc, UtilPack.Opt<?>... parts) {
         super("u4_" + id, name, desc, Category.PERFORMANCE);
         this.icon("boost");
         this.fresh();
         this.parts = parts;
      }

      @Override
      public void onEnable() {
         for (UtilPack.Opt<?> o : this.parts) {
            o.onEnable();
         }
      }

      @Override
      public void tick() {
         for (UtilPack.Opt<?> o : this.parts) {
            o.tick();
         }
      }

      @Override
      public void onDisable() {
         for (UtilPack.Opt<?> o : this.parts) {
            o.onDisable();
         }
      }
   }
}
