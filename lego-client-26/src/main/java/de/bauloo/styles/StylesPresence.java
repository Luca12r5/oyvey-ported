package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;

public final class StylesPresence {
   private static final Identifier TAB_FONT = Identifier.fromNamespaceAndPath("bauloo_styles", "tab");
   private static final int RED = 16733525;
   private static final int BLUE = 5093631;
   private static final int GOLD = 16761405;
   private static final int PINK = 16740312;
   private static final int WHITE = 16777215;
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   private static final Map<UUID, String> ROLES = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> ASKED = new ConcurrentHashMap<>();
   private static final long PING_EVERY = 240000L;
   private static final long LOOKUP_EVERY = 30000L;
   private static volatile long lastPing;
   private static volatile long lastLookup;
   private static Object lastConnection;
   private static UUID lastId;
   private static volatile String myRole;

   private StylesPresence() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(StylesPresence::tick);
   }

   public static void settingChanged() {
      lastPing = 0L;
   }

   private static void tick(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null && StylesCloud.publishUrl() != null) {
         long now = System.currentTimeMillis();
         UUID id = mc.player.getUUID();
         if (mc.getConnection() != lastConnection || !id.equals(lastId)) {
            lastConnection = mc.getConnection();
            lastId = id;
            lastPing = now - 240000L + 3000L;
            lastLookup = now - 30000L + 5000L;
            ASKED.clear();
         }

         if (now - lastPing > 240000L) {
            lastPing = now;
            ping(mc);
         }

         boolean tabOpen = mc.options.keyPlayerList.isDown();
         if (now - lastLookup > (tabOpen ? 4000L : 30000L)) {
            lastLookup = now;
            lookup(mc);
         }
      }
   }

   private static Builder request(String path, JsonObject body) {
      return HttpRequest.newBuilder(URI.create(StylesCloud.publishUrl() + path))
         .timeout(Duration.ofSeconds(10L))
         .header("Content-Type", "application/json")
         .POST(BodyPublishers.ofString(body.toString()));
   }

   private static void ping(Minecraft mc) {
      UUID me = mc.player.getUUID();
      boolean show = StylesUiConfig.showInTab;
      JsonObject body = new JsonObject();
      body.addProperty("uuid", me.toString());
      body.addProperty("show", show);
      Builder b = request("/presence", body);
      String key = StylesUploadServer.savedKey();
      if (!key.isEmpty()) {
         b.header("Authorization", "Bearer " + key).header("X-Device", StylesUploadServer.deviceId());
      }

      HTTP.sendAsync(b.build(), BodyHandlers.ofString()).thenAccept(r -> {
         try {
            JsonElement role = JsonParser.parseString(r.body()).getAsJsonObject().get("role");
            myRole = role != null && !role.isJsonNull() ? role.getAsString() : null;
            if (show && role != null && !role.isJsonNull()) {
               ROLES.put(me, role.getAsString());
            } else {
               ROLES.remove(me);
            }
         } catch (RuntimeException var4x) {
         }
      });
   }

   private static void lookup(Minecraft mc) {
      long now = System.currentTimeMillis();
      List<UUID> ask = new ArrayList<>();

      for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
         UUID id = entry.getProfile().id();
         Long when = ASKED.get(id);
         if (when == null || now - when > 30000L) {
            ask.add(id);
            if (ask.size() == 100) {
               break;
            }
         }
      }

      if (!ask.isEmpty()) {
         JsonArray list = new JsonArray();

         for (UUID id : ask) {
            list.add(id.toString());
            ASKED.put(id, now);
         }

         JsonObject body = new JsonObject();
         body.add("uuids", list);
         HTTP.sendAsync(request("/presence/lookup", body).build(), BodyHandlers.ofString()).thenAccept(r -> {
            try {
               JsonObject players = JsonParser.parseString(r.body()).getAsJsonObject().getAsJsonObject("players");

               for (UUID idx : ask) {
                  JsonElement role = players.get(idx.toString().replace("-", ""));
                  if (role == null) {
                     ROLES.remove(idx);
                  } else {
                     ROLES.put(idx, role.getAsString());
                  }
               }
            } catch (RuntimeException var6x) {
            }
         });
      }
   }

   public static boolean isAdmin() {
      return "admin".equals(myRole);
   }

   public static boolean isVip() {
      String r = myRole;
      return "vip".equals(r) || "media".equals(r) || "uploader".equals(r) || "admin".equals(r);
   }

   public static Component icon(UUID id) {
      String role = ROLES.get(id);
      if (role == null) {
         return null;
      } else {
         int color = "admin".equals(role)
            ? 16733525
            : ("uploader".equals(role) ? 5093631 : ("vip".equals(role) ? 16761405 : ("media".equals(role) ? 16740312 : 16777215)));
         return Component.literal("\ue000").withStyle(style -> style.withFont(new Resource(TAB_FONT)).withColor(color));
      }
   }
}
