package dev.lego.net;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.cosmetic.Cos;
import dev.lego.emote.EmoteLib;
import dev.lego.emote.Emotes;
import dev.lego.games.Duels;
import dev.lego.ui.Toasts;
import dev.lego.visual.NameFx;
import dev.spotifyhud.Json;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.client.player.LocalPlayer;

public final class LegoSync extends Module {
   public static LegoSync instance;
   private static final String BASE = "https://ntfy.sh/";
   private static final String SYNC_TOPIC = "legoclient-namesync-v1-q7x4m2";
   private static final String DUEL_TOPIC = "legoclient-duel-v1-q7x4m2";
   private static final String RELAY = "https://ntfy.sh/legoclient-namesync-v1-q7x4m2";
   public static final String DUEL_RELAY = "https://ntfy.sh/legoclient-duel-v1-q7x4m2";
   private static final long HEARTBEAT_MS = 480000L;
   private static final long MAX_AGE_S = 600L;
   private static final long MIN_POST_GAP_MS = 2500L;
   private static final long STALL_MS = 150000L;
   final Setting.Bool showOthers = this.add(new Setting.Bool("Andere Lego-Spieler anzeigen", true));
   private static final Map<UUID, LegoSync.Remote> REMOTE = new ConcurrentHashMap<>();
   static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   private volatile boolean posting;
   private volatile boolean greeted;
   private long nextBeat;
   private long lastPost;
   private String lastSent = "";
   private String lastEmote = "";
   private long emoteStartS;
   private static volatile int fails;
   private static volatile long lastError;
   private static volatile long lastLineAt;
   private static volatile long connectedAt;
   private static volatile String lastMsgId;
   private static volatile Thread streamThread;
   private static volatile Stream<String> openStream;

   public LegoSync() {
      super(
         "legosync",
         "Lego-Sync",
         "Alle Lego-Client-Spieler sehen gegenseitig Namensschild, Kosmetik, Character und Emotes (online, ohne eigenen Server)",
         Category.UTILITY
      );
      this.icon("users");
      this.fresh();
      this.enabled = true;
      instance = this;
   }

   public static void register() {
      Modules.register(new LegoSync());
   }

   public static boolean active() {
      return instance != null && instance.showOthers.get();
   }

   private static LegoSync.Remote get(UUID id) {
      if (!active()) {
         return null;
      } else {
         LegoSync.Remote r = REMOTE.get(id);
         return r != null && System.currentTimeMillis() / 1000L - r.seen() <= 600L ? r : null;
      }
   }

   public static Map<String, Object> remote(UUID id) {
      LegoSync.Remote r = get(id);
      return r == null ? null : Json.obj(r.data(), "tag");
   }

   public static List<String> cosmetics(UUID id) {
      LegoSync.Remote r = get(id);
      Object o = r == null ? null : r.data().get("cos");
      return o instanceof List ? (List)o : List.of();
   }

   public static int character(UUID id) {
      LegoSync.Remote r = get(id);
      return r == null ? 0 : (int)Json.num(r.data(), "char", 0L);
   }

   public static String emote(UUID id) {
      LegoSync.Remote r = get(id);
      if (r == null) {
         return null;
      } else {
         String e = Json.str(r.data(), "emote", "");
         return e.isEmpty() ? null : e;
      }
   }

   public static double emoteStart(UUID id) {
      LegoSync.Remote r = get(id);
      return r == null ? 0.0 : Json.dbl(r.data(), "emoteAt", 0.0);
   }

   public static String vehicle(UUID id) {
      LegoSync.Remote r = get(id);
      if (r == null) {
         return null;
      } else {
         String v = Json.str(r.data(), "veh", "");
         return v.isEmpty() ? null : v;
      }
   }

   public static String pet(UUID id) {
      LegoSync.Remote r = get(id);
      if (r == null) {
         return null;
      } else {
         String p = Json.str(r.data(), "pet", "");
         return p.isEmpty() ? null : p;
      }
   }

   public static String name(UUID id) {
      LegoSync.Remote r = get(id);
      return r == null ? null : Json.str(r.data(), "name", null);
   }

   public static List<UUID> players() {
      List<UUID> l = new ArrayList<>();
      if (!active()) {
         return l;
      } else {
         long now = System.currentTimeMillis() / 1000L;

         for (Entry<UUID, LegoSync.Remote> e : REMOTE.entrySet()) {
            if (now - e.getValue().seen() <= 600L) {
               l.add(e.getKey());
            }
         }

         return l;
      }
   }

   public static boolean connected() {
      return connectedAt > 0L && streamThread != null;
   }

   public static int online() {
      long now = System.currentTimeMillis() / 1000L;
      int n = 0;

      for (LegoSync.Remote r : REMOTE.values()) {
         if (now - r.seen() <= 600L) {
            n++;
         }
      }

      return n;
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   @Override
   public void onDisable() {
      this.enabled = true;
   }

   public static void HTTP_POST(String url, String body) {
      if (active() && body.length() <= 3000) {
         HTTP.sendAsync(
            HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8L)).POST(BodyPublishers.ofString(body)).build(), BodyHandlers.discarding()
         );
      }
   }

   private static void closeStream() {
      Stream<String> s = openStream;
      if (s != null) {
         try {
            s.close();
         } catch (Throwable var2) {
         }
      }
   }

   private static int myCharacter() {
      try {
         return Class.forName("dev.creamykes.CreamyConfig").getField("character").getInt(null);
      } catch (Throwable var1) {
         return 0;
      }
   }

   private static Map<String, Object> myState(LocalPlayer me) {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("id", me.getUUID().toString());
      body.put("name", Mc.name(me));
      body.put("tag", NameFx.tag != null && NameFx.tag.enabled ? NameFx.tag.syncState() : new LinkedHashMap());
      List<Object> cos = new ArrayList<>();

      for (Cos.Slot s : Cos.Slot.values()) {
         Cos.Item it = Cos.equipped(s);
         if (it != null) {
            cos.add(it.id);
         }
      }

      body.put("cos", cos);
      Cos.Item veh = Cos.equipped(Cos.Slot.VEHICLE);
      body.put("veh", veh != null ? veh.id : "");
      Cos.Item pet = Cos.equipped(Cos.Slot.PET);
      body.put("pet", pet != null ? pet.id : "");
      body.put("char", (long)myCharacter());
      EmoteLib.Emote e = Emotes.current();
      body.put("emote", e == null ? "" : e.id);
      return body;
   }

   @Override
   public void tick() {
      LocalPlayer me = Mc.player();
      if (me != null) {
         long now = System.currentTimeMillis();
         Map<String, Object> state = myState(me);
         String json = Json.write(state);
         String emote = Json.str(state, "emote", "");
         if (!emote.equals(this.lastEmote)) {
            this.lastEmote = emote;
            this.emoteStartS = now / 1000L;
         }

         boolean changed = !json.equals(this.lastSent);
         if (!this.posting && now - this.lastPost >= 2500L && (changed || now >= this.nextBeat)) {
            this.posting = true;
            this.lastPost = now;
            if (!emote.isEmpty()) {
               state.put("emoteAt", (double)this.emoteStartS);
            }

            String send = Json.write(state);
            HTTP.sendAsync(
                  HttpRequest.newBuilder(URI.create("https://ntfy.sh/legoclient-namesync-v1-q7x4m2"))
                     .timeout(Duration.ofSeconds(8L))
                     .POST(BodyPublishers.ofString(send))
                     .build(),
                  BodyHandlers.discarding()
               )
               .whenComplete((res, err) -> {
                  this.posting = false;
                  if (err == null && res.statusCode() == 200) {
                     this.lastSent = json;
                     this.nextBeat = System.currentTimeMillis() + 480000L;
                  } else {
                     this.nextBeat = System.currentTimeMillis() + 60000L;
                     this.fail();
                  }
               });
         }

         Thread st = streamThread;
         if (st == null || !st.isAlive()) {
            Thread th = new Thread(LegoSync::streamLoop, "Lego-Sync");
            th.setDaemon(true);
            streamThread = th;
            th.start();
         } else if (lastLineAt > 0L && now - lastLineAt > 150000L) {
            lastLineAt = now;
            closeStream();
         }

         if (!this.greeted && connectedAt > 0L && now / 1000L - connectedAt >= 3L) {
            this.greeted = true;
            Toasts.show("legosync", "Lego-Sync", "Verbunden · " + online() + " andere Lego-Spieler online", -14756000, 3000L);
         }
      }
   }

   private static void streamLoop() {
      long backoff = 2000L;

      try {
         for (; active(); backoff = Math.min(backoff * 2L, 120000L)) {
            String since = lastMsgId != null ? lastMsgId : "10m";

            try {
               HttpResponse<Stream<String>> res = HTTP.send(
                  HttpRequest.newBuilder(URI.create("https://ntfy.sh/legoclient-namesync-v1-q7x4m2,legoclient-duel-v1-q7x4m2/json?since=" + since))
                     .timeout(Duration.ofSeconds(15L))
                     .GET()
                     .build(),
                  BodyHandlers.ofLines()
               );

               try (Stream<String> lines = res.body()) {
                  if (res.statusCode() != 200) {
                     throw new IOException("HTTP " + res.statusCode());
                  }

                  openStream = lines;
                  connectedAt = System.currentTimeMillis() / 1000L;
                  lastLineAt = System.currentTimeMillis();
                  fails = 0;
                  backoff = 2000L;
                  Iterator<String> it = lines.iterator();

                  while (it.hasNext() && active()) {
                     String line = it.next();
                     lastLineAt = System.currentTimeMillis();

                     try {
                        handle(line);
                     } catch (Throwable var16) {
                     }
                  }
               }
            } catch (Throwable var19) {
               failStatic();
            }

            openStream = null;
            connectedAt = 0L;
            if (!active()) {
               break;
            }

            try {
               Thread.sleep(backoff);
            } catch (InterruptedException var17) {
               break;
            }
         }
      } finally {
         streamThread = null;
         connectedAt = 0L;
      }
   }

   private static void handle(String line) {
      if (!line.isBlank() && line.length() <= 8000) {
         Object ev = Json.parse(line);
         if ("message".equals(Json.str(ev, "event", ""))) {
            String mid = Json.str(ev, "id", "");
            if (!mid.isEmpty() && mid.length() <= 32 && mid.matches("[A-Za-z0-9]+")) {
               lastMsgId = mid;
            }

            long time = Json.num(ev, "time", 0L);
            Object msg = Json.parse(Json.str(ev, "message", ""));
            LocalPlayer me = Mc.player();
            String myId = me == null ? "" : me.getUUID().toString();
            String topic = Json.str(ev, "topic", "");
            if (topic.equals("legoclient-duel-v1-q7x4m2")) {
               Duels.receive(msg, time, myId);
            } else if (topic.equals("legoclient-namesync-v1-q7x4m2")) {
               String id = Json.str(msg, "id", "");
               if (!id.equals(myId)) {
                  UUID uuid;
                  try {
                     uuid = UUID.fromString(id);
                  } catch (IllegalArgumentException var18) {
                     return;
                  }

                  Map<String, Object> data = clean(msg);
                  LegoSync.Remote old = REMOTE.get(uuid);
                  if (old == null || old.seen() <= time) {
                     String emote = Json.str(data, "emote", "");
                     double raw = Json.dbl(data, "emoteAt", 0.0);
                     long nowS = System.currentTimeMillis() / 1000L;
                     if (emote.isEmpty()) {
                        data.put("emoteAt", 0.0);
                     } else if (old != null && emote.equals(Json.str(old.data(), "emote", "")) && raw == Json.dbl(old.data(), "emoteRaw", -1.0)) {
                        data.put("emoteAt", Json.dbl(old.data(), "emoteAt", raw));
                     } else {
                        data.put("emoteAt", nowS - time < 30L ? System.currentTimeMillis() / 1000.0 : raw);
                     }

                     data.put("emoteRaw", raw);
                     REMOTE.put(uuid, new LegoSync.Remote(data, time));
                  }
               }
            }
         }
      }
   }

   private static void failStatic() {
      fails++;
      long now = System.currentTimeMillis();
      if (fails == 4 && now - lastError > 300000L) {
         lastError = now;
         Toasts.show("legosync", "Lego-Sync", "Keine Verbindung zum Sync-Dienst – versuche es später erneut", -45730, 3500L);
      }
   }

   private static boolean safeId(String s) {
      return s != null && s.length() <= 48 && s.matches("[a-z0-9_:\\-]+");
   }

   private static Map<String, Object> clean(Object msg) {
      Map<String, Object> c = new LinkedHashMap<>();
      Map<String, Object> t = Json.obj(msg, "tag");
      if (t != null && !t.isEmpty()) {
         Map<String, Object> tag = new LinkedHashMap<>();
         String text = Json.str(t, "t", "");
         tag.put("t", text.length() > 24 ? text.substring(0, 24) : text);
         tag.put("s", (double)Math.max(0, Math.min(7, (int)Json.num(t, "s", 0L))));
         tag.put("c1", (double)Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, (int)Json.num(t, "c1", 5L))));
         tag.put("c2", (double)Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, (int)Json.num(t, "c2", 7L))));
         tag.put("b", Json.bool(t, "b", true));
         tag.put("m", Json.bool(t, "m", true));
         tag.put("sp", Math.max(0.3, Math.min(3.0, Json.dbl(t, "sp", 1.0))));
         tag.put("lego", Json.bool(t, "lego", true));
         c.put("tag", tag);
      }

      List<String> cos = new ArrayList<>();
      Object co = msg instanceof Map ? ((Map)msg).get("cos") : null;
      if (co instanceof List) {
         for (Object o : (List)co) {
            if (o instanceof String s && safeId(s) && cos.size() < 12) {
               cos.add(s);
            }
         }
      }

      c.put("cos", cos);
      String nm = Json.str(msg, "name", "");
      if (nm.matches("[A-Za-z0-9_]{1,16}")) {
         c.put("name", nm);
      }

      String v = Json.str(msg, "veh", "");
      c.put("veh", safeId(v) ? v : "");
      String pt = Json.str(msg, "pet", "");
      c.put("pet", safeId(pt) ? pt : "");
      c.put("char", (double)Math.max(0, Math.min(64, (int)Json.num(msg, "char", 0L))));
      String e = Json.str(msg, "emote", "");
      c.put("emote", safeId(e) ? e : "");
      c.put("emoteAt", Json.dbl(msg, "emoteAt", 0.0));
      return c;
   }

   private void fail() {
      failStatic();
   }

   private record Remote(Map<String, Object> data, long seen) {
   }
}
