package dev.lego.net;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.cosmetic.Cos;
import dev.spotifyhud.Json;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Fetches the equipped LEGO cosmetics and name tags of nearby players from the
 * LEGO backend (public lookup endpoint, no token needed) so other LEGO
 * players' items render on this client. Works on any server: it only depends
 * on both players using the LEGO client and the LEGO backend being reachable.
 * Players of other clients never see these cosmetics.
 */
public final class LegoNet extends Module {
   public static LegoNet INSTANCE;

   /** Lookup results per player. Missing key = not fetched yet; NONE = not a LEGO user. */
   private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();
   private static final Set<UUID> IN_FLIGHT = ConcurrentHashMap.newKeySet();
   private static final Entry NONE = new Entry(Map.of(), null, null, 0L);
   private static final long TTL_MS = 60_000L;
   private static final long MIN_INTERVAL_MS = 3_000L;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

   final Setting.Bool showCosmetics = this.add(new Setting.Bool("Cosmetics anderer LEGO-Spieler", true));
   final Setting.Bool showTags = this.add(new Setting.Bool("Name Tags anderer LEGO-Spieler", true));
   final Setting.Num maxPlayers = this.add(new Setting.Num("Max. Spieler mit Cosmetics", 4.0, 64.0, 1.0, 24.0, ""));
   final Setting.Text server = this.add(new Setting.Text("LEGO-Server (leer = vom Launcher)", "", 120, "https://…"));

   /** Nearest LEGO players allowed to render cosmetics this tick (bounded for FPS). */
   private static volatile Set<UUID> ALLOWED = Set.of();
   private long lastRequest = 0L;
   private volatile String lastError = null;

   public record Entry(Map<Cos.Slot, String> items, String nameTag, String customTag, long fetchedAt) {
   }

   public LegoNet() {
      super("legonet", "LEGO-Netzwerk", "Zeigt Cosmetics, Capes und Name Tags anderer LEGO-Spieler (über den LEGO-Server)", Category.UTILITY);
      this.icon("user");
      this.enabled = true;
      this.add(new Setting.Action("Cache", "Neu laden", CACHE::clear));
   }

   public static void register() {
      INSTANCE = Modules.register(new LegoNet());
   }

   /** Backend base URL: module setting, else -Dlegoclient.api passed by the LEGO Launcher. */
   public String baseUrl() {
      String s = this.server.get();
      if (s == null || s.isBlank()) s = System.getProperty("legoclient.api", "");
      s = s.trim();
      if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
      boolean local = s.startsWith("http://127.0.0.1") || s.startsWith("http://localhost");
      return s.startsWith("https://") || local ? s : "";
   }

   public static boolean cosmeticsEnabled() {
      return INSTANCE != null && INSTANCE.enabled && INSTANCE.showCosmetics.get();
   }

   public static boolean tagsEnabled() {
      return INSTANCE != null && INSTANCE.enabled && INSTANCE.showTags.get();
   }

   public static boolean allowed(UUID id) {
      return ALLOWED.contains(id);
   }

   public static int maxPlayers() {
      return INSTANCE == null ? 0 : (int)INSTANCE.maxPlayers.get();
   }

   /** Equipped items of a remote player, or null when unknown / not a LEGO user. */
   public static Entry get(UUID id) {
      Entry e = CACHE.get(id);
      return e == null || e == NONE ? null : e;
   }

   public String status() {
      if (this.baseUrl().isEmpty()) return "Kein LEGO-Server konfiguriert";
      long users = CACHE.values().stream().filter(e -> e != NONE).count();
      return lastError != null ? "Fehler: " + lastError : users + " LEGO-Spieler in der Nähe erkannt";
   }

   @Override
   public void tick() {
      String base = this.baseUrl();
      ClientLevel level = Mc.mc().level;
      if (base.isEmpty() || level == null) return;
      Player self = Mc.player();
      if (self != null) {
         List<AbstractClientPlayer> lego = new ArrayList<>();
         for (AbstractClientPlayer p : level.players()) {
            if (p != self && get(p.getUUID()) != null && p.distanceToSqr(self) < 64.0 * 64.0) lego.add(p);
         }
         lego.sort(java.util.Comparator.comparingDouble(p -> p.distanceToSqr(self)));
         Set<UUID> allowed = new java.util.HashSet<>();
         for (int i = 0; i < Math.min(lego.size(), maxPlayers()); i++) allowed.add(lego.get(i).getUUID());
         ALLOWED = allowed;
      }
      long now = System.currentTimeMillis();
      if (now - this.lastRequest < MIN_INTERVAL_MS) return;
      List<String> wanted = new ArrayList<>();
      for (AbstractClientPlayer p : level.players()) {
         if (p == self) continue;
         UUID id = p.getUUID();
         // Offline-mode servers give version-3 (name based) UUIDs that never match Microsoft accounts.
         if (id.version() != 4 || IN_FLIGHT.contains(id)) continue;
         Entry e = CACHE.get(id);
         if (e == null || now - e.fetchedAt() > TTL_MS) wanted.add(id.toString());
         if (wanted.size() >= 100) break;
      }
      if (wanted.isEmpty()) return;
      this.lastRequest = now;
      for (String w : wanted) IN_FLIGHT.add(UUID.fromString(w));
      String body = Json.write(Map.of("uuids", wanted));
      HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/public/cosmetics/lookup"))
         .timeout(Duration.ofSeconds(8))
         .header("Content-Type", "application/json")
         .header("User-Agent", "LEGO-Client")
         .POST(HttpRequest.BodyPublishers.ofString(body))
         .build();
      HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).whenComplete((res, err) -> {
         try {
            long t = System.currentTimeMillis();
            if (err != null || res.statusCode() != 200) {
               this.lastError = err != null ? err.getClass().getSimpleName() : "HTTP " + res.statusCode();
               return;
            }
            this.lastError = null;
            Map<String, Object> players = Json.obj(Json.parse(res.body()), "players");
            for (String w : wanted) {
               Object p = players == null ? null : players.get(w);
               CACHE.put(UUID.fromString(w), p instanceof Map ? parse(p, t) : new Entry(NONE.items(), null, null, t));
            }
         } catch (Throwable ex) {
            this.lastError = ex.getClass().getSimpleName();
            if (LegoClient.DEBUG) LegoClient.LOG("LegoNet: " + ex);
         } finally {
            for (String w : wanted) IN_FLIGHT.remove(UUID.fromString(w));
         }
      });
   }

   private static Entry parse(Object player, long t) {
      Map<Cos.Slot, String> items = new EnumMap<>(Cos.Slot.class);
      Map<String, Object> eq = Json.obj(player, "equipped");
      String tag = null;
      if (eq != null) {
         for (Map.Entry<String, Object> e : eq.entrySet()) {
            if (!(e.getValue() instanceof String id)) continue;
            if (e.getKey().equals("nametag")) {
               tag = id;
               continue;
            }
            try {
               Cos.Slot slot = Cos.Slot.valueOf(e.getKey().toUpperCase(java.util.Locale.ROOT));
               Cos.Item item = Cos.get(id);
               // Only items the client knows, in the slot they belong to.
               if (item != null && item.slot == slot) items.put(slot, id);
            } catch (IllegalArgumentException ignored) {
            }
         }
      }
      String custom = Json.str(player, "customTag", null);
      return new Entry(Map.copyOf(items), tag, custom, t);
   }

   /** Drops cached entries for players that left (called on world change). */
   public static void clear() {
      CACHE.clear();
   }
}
