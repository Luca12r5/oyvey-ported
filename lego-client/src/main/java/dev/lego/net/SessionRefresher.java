package dev.lego.net;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.spotifyhud.Json;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

/**
 * Renews the Minecraft session while the game runs (like the NoRisk client), so joining a server
 * after many hours does not fail with "Invalid session". The LEGO Launcher started this game and
 * hands its random per-launch key over the environment; the renewed session comes from the
 * launcher's bridge on 127.0.0.1 and is only ever the account this game already plays with.
 * Also offers "Konto wechseln": the launcher restarts the game with another signed-in account.
 * Without the launcher (no environment variables) all of this stays off.
 */
public final class SessionRefresher {
   private static final String PORT = System.getenv("LEGO_SESSION_PORT");
   private static final String KEY = System.getenv("LEGO_SESSION_KEY");
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).proxy(java.net.ProxySelector.of(null)).build();
   /** Renew when less than this is left (the launcher renews below 30 min). */
   private static final long RENEW_BEFORE_MS = 20 * 60_000L;
   private static volatile long expiresAt = 0L;
   private static volatile boolean warned = false;
   private static Thread worker;
   private static Module module;

   private SessionRefresher() {
   }

   public static boolean available() {
      return PORT != null && PORT.matches("\\d{1,5}") && KEY != null && KEY.matches("[0-9a-f]{64}");
   }

   public static void init() {
      module = Modules.register(new SessionModule());
      if (!available() || worker != null) return;
      worker = new Thread(SessionRefresher::loop, "LEGO-Session");
      worker.setDaemon(true);
      worker.start();
   }

   private static void loop() {
      try {
         Thread.sleep(Boolean.getBoolean("legoclient.ci.session") ? 10_000L : 60_000L);
      } catch (InterruptedException e) {
         return;
      }
      if (Boolean.getBoolean("legoclient.ci.session")) renew(true);
      while (true) {
         try {
            boolean on = module == null || module.enabled;
            // expiresAt 0: not known yet, ask once (the launcher answers without renewing when there is time left).
            if (on && (expiresAt == 0L || expiresAt - System.currentTimeMillis() < RENEW_BEFORE_MS)) renew(false).join();
            // Short steps: a PC waking from sleep is noticed within 5 minutes.
            Thread.sleep(5 * 60_000L);
         } catch (InterruptedException e) {
            return;
         } catch (Throwable t) {
            LegoClient.LOG("Sitzung: " + t.getMessage());
         }
      }
   }

   private static HttpRequest.Builder req(String path) {
      return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + PORT + path)).timeout(Duration.ofSeconds(20)).header("X-Lego-Key", KEY).header("Content-Type", "application/json");
   }

   /** Asks the launcher for this account's session and puts it into the running game. */
   public static CompletableFuture<Boolean> renew(boolean force) {
      if (!available()) return CompletableFuture.completedFuture(false);
      String body = force ? "{\"force\":true}" : "{}";
      return HTTP.sendAsync(req("/session").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString()).thenApply(r -> {
         if (r.statusCode() == 429) return false;
         if (r.statusCode() != 200) throw new IllegalStateException("Launcher antwortet " + r.statusCode());
         Object o = Json.parse(r.body());
         String token = Json.str(o, "accessToken", null);
         String uuid = Json.str(o, "uuid", "");
         long exp = Json.num(o, "expiresAt", 0L);
         if (token == null || token.isEmpty()) throw new IllegalStateException("leere Sitzung");
         Minecraft mc = Mc.mc();
         mc.execute(() -> {
            try {
               User old = mc.getUser();
               if (!sameId(old.getProfileId(), uuid)) {
                  LegoClient.LOG("Sitzung: anderes Konto, nicht übernommen");
                  return;
               }
               boolean changed = !token.equals(old.getAccessToken());
               if (changed) {
                  replaceToken(mc, old, token);
                  // The key service may ask Mojang over the network: not on the render thread.
                  CompletableFuture.runAsync(() -> refreshKeys(mc, token));
                  LegoClient.LOG("Sitzung erneuert (uuid gleich)");
                  Toasts.show("check", "Sitzung erneuert", "Du kannst weiter Servern beitreten – ohne Neustart.", -16735270, 4000L);
               }
               expiresAt = exp;
               warned = false;
            } catch (Throwable t) {
               LegoClient.LOG("Sitzung übernehmen: " + t);
            }
         });
         return true;
      }).exceptionally(t -> {
         LegoClient.LOG("Sitzung erneuern: " + t.getMessage());
         if (!warned) {
            warned = true;
            Toasts.show("info", "Sitzung nicht erneuert", "Ist der LEGO Launcher noch offen und das Konto angemeldet?", -30208, 6000L);
         }
         return false;
      });
   }

   private static boolean sameId(UUID id, String uuid) {
      return id != null && id.toString().replace("-", "").equalsIgnoreCase(uuid.replace("-", ""));
   }

   /** Puts the new token into the game's User (works for plain classes and records, any mapping). */
   private static void replaceToken(Minecraft mc, User old, String token) throws ReflectiveOperationException {
      String oldToken = old.getAccessToken();
      List<Field> fields = new ArrayList<>();
      for (Field f : User.class.getDeclaredFields()) if (!Modifier.isStatic(f.getModifiers())) fields.add(f);
      Object[] values = new Object[fields.size()];
      Class<?>[] types = new Class<?>[fields.size()];
      for (int i = 0; i < fields.size(); i++) {
         Field f = fields.get(i);
         f.setAccessible(true);
         Object v = f.get(old);
         values[i] = oldToken.equals(v) ? token : v;
         types[i] = f.getType();
      }
      // New User with all old values except the token, then swapped into Minecraft's (final) field.
      Constructor<?> ctor = User.class.getDeclaredConstructor(types);
      ctor.setAccessible(true);
      User fresh = (User)ctor.newInstance(values);
      setFieldOfType(mc, User.class, fresh);
   }

   /** New chat-signing keys and user API for the new token; skipped quietly where it does not fit. */
   private static void refreshKeys(Minecraft mc, String token) {
      try {
         // The game's user API object (authlib), rebuilt with the same parts but the new token.
         Field apiField = null;
         for (Field f : Minecraft.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType().isInterface() && f.getType().getName().endsWith(".UserApiService")) {
               apiField = f;
               break;
            }
         }
         if (apiField == null) throw new IllegalStateException("kein UserApiService");
         apiField.setAccessible(true);
         Object oldApi = apiField.get(mc);
         Object api = withToken(oldApi, token);
         apiField.set(mc, api);
         Class<?> apiType = apiField.getType();
         Path dir = FabricLoader.getInstance().getGameDir();
         for (Field f : Minecraft.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            for (Method m : f.getType().getDeclaredMethods()) {
               Class<?>[] p = m.getParameterTypes();
               if (Modifier.isStatic(m.getModifiers()) && m.getReturnType() == f.getType() && p.length == 3 && p[0] == apiType && p[1] == User.class && p[2] == Path.class) {
                  m.setAccessible(true);
                  Object keys = m.invoke(null, api, mc.getUser(), dir);
                  f.setAccessible(true);
                  f.set(mc, keys);
                  return;
               }
            }
         }
      } catch (Throwable t) {
         LegoClient.LOG("Sitzung: Chat-Schlüssel bleiben alt (" + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
      }
   }

   /**
    * A copy of an authlib service object whose access token is replaced: its constructor gets the
    * new token for the String parameter and the old object's values (by type) for the rest.
    */
   private static Object withToken(Object old, String token) throws ReflectiveOperationException {
      List<Field> fields = new ArrayList<>();
      for (Class<?> c = old.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
         for (Field f : c.getDeclaredFields()) if (!Modifier.isStatic(f.getModifiers())) {
            f.setAccessible(true);
            fields.add(f);
         }
      }
      Constructor<?> best = null;
      for (Constructor<?> c : old.getClass().getDeclaredConstructors()) {
         int strings = 0;
         for (Class<?> t : c.getParameterTypes()) if (t == String.class) strings++;
         if (strings == 1 && (best == null || c.getParameterCount() > best.getParameterCount())) best = c;
      }
      if (best == null) throw new NoSuchMethodException(old.getClass().getSimpleName() + "(String, …)");
      Class<?>[] types = best.getParameterTypes();
      Object[] args = new Object[types.length];
      List<Field> unused = new ArrayList<>(fields);
      for (int i = 0; i < types.length; i++) {
         if (types[i] == String.class) {
            args[i] = token;
            continue;
         }
         Field match = null;
         for (Field f : unused) if (types[i].isAssignableFrom(f.getType()) || (types[i].isPrimitive() && f.getType() == types[i])) { match = f; break; }
         if (match == null) throw new NoSuchFieldException(types[i].getSimpleName());
         unused.remove(match);
         args[i] = match.get(old);
      }
      best.setAccessible(true);
      return best.newInstance(args);
   }

   private static void setFieldOfType(Object target, Class<?> type, Object value) throws IllegalAccessException {
      for (Field f : target.getClass().getDeclaredFields()) {
         if (!Modifier.isStatic(f.getModifiers()) && f.getType() == type) {
            f.setAccessible(true);
            f.set(target, value);
            return;
         }
      }
      throw new IllegalStateException("kein Feld " + type.getSimpleName());
   }

   /** Signed-in accounts of the launcher (names only), current one first. */
   public static CompletableFuture<List<Map<String, String>>> accounts() {
      if (!available()) return CompletableFuture.completedFuture(List.of());
      return HTTP.sendAsync(req("/accounts").GET().build(), HttpResponse.BodyHandlers.ofString()).thenApply(r -> {
         List<Map<String, String>> out = new ArrayList<>();
         if (r.statusCode() != 200) return out;
         Object o = Json.parse(r.body());
         String current = Json.str(o, "current", "");
         for (Object a : Json.arr(o, "accounts")) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("uuid", Json.str(a, "uuid", ""));
            m.put("name", Json.str(a, "name", "?"));
            m.put("current", String.valueOf(m.get("uuid").equals(current)));
            out.add(m);
         }
         return out;
      });
   }

   /** The launcher restarts the game with this account and, on a server, joins it again. */
   public static void switchTo(String uuid, String name) {
      String server = Mc.mc().isLocalServer() ? null : Mc.serverAddress();
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("uuid", uuid);
      if (server != null && !"-".equals(server)) body.put("server", server);
      HTTP.sendAsync(req("/switch").POST(HttpRequest.BodyPublishers.ofString(Json.write(body))).build(), HttpResponse.BodyHandlers.discarding()).thenAccept(r -> {
         if (r.statusCode() != 200) {
            Toasts.show("info", "Wechsel nicht möglich", "Der Launcher hat abgelehnt (" + r.statusCode() + ").", -30208, 5000L);
            return;
         }
         Toasts.show("user", "Wechsel zu " + name, "Minecraft startet gleich mit diesem Konto neu …", -16735270, 3000L);
         Minecraft mc = Mc.mc();
         // A clean stop: settings are saved (CLIENT_STOPPING) and the launcher sees exit code 0.
         mc.execute(mc::stop);
      }).exceptionally(t -> {
         Toasts.show("info", "Wechsel nicht möglich", "Läuft der LEGO Launcher noch?", -30208, 5000L);
         return null;
      });
   }

   /** Module "Konto & Sitzung" (Utility): automatic renewal plus the account switch. */
   static final class SessionModule extends Module {
      private List<Map<String, String>> choices = List.of();
      private int pick = -1;
      private long pickedAt = 0L;

      SessionModule() {
         super("sessionrefresh", "Konto & Sitzung", "Erneuert die Minecraft-Sitzung im Spiel (kein Neustart nach Stunden) und wechselt das Konto über den LEGO Launcher", Category.UTILITY);
         this.icon("user");
         this.enabled = true;
         this.add(new Setting.Action("Sitzung", "Jetzt erneuern", () -> {
            if (!available()) {
               Toasts.show("info", "Nur mit dem LEGO Launcher", "Starte das Spiel über den LEGO Launcher.", -30208, 4000L);
               return;
            }
            renew(true);
         }));
         this.add(new Setting.Action("Konto", "Konto wechseln (2× klicken)", this::switchStep));
      }

      /** First click picks the next account and shows it, a second click within 6 s confirms. */
      private void switchStep() {
         if (!available()) {
            Toasts.show("info", "Nur mit dem LEGO Launcher", "Starte das Spiel über den LEGO Launcher.", -30208, 4000L);
            return;
         }
         long now = System.currentTimeMillis();
         if (this.pick >= 0 && now - this.pickedAt < 6000L && this.pick < this.choices.size()) {
            Map<String, String> c = this.choices.get(this.pick);
            this.pick = -1;
            switchTo(c.get("uuid"), c.get("name"));
            return;
         }
         accounts().thenAccept(list -> {
            List<Map<String, String>> others = new ArrayList<>();
            for (Map<String, String> a : list) if (!"true".equals(a.get("current"))) others.add(a);
            if (others.isEmpty()) {
               Toasts.show("info", "Kein anderes Konto", "Füge im LEGO Launcher oben rechts ein Konto hinzu.", -30208, 5000L);
               return;
            }
            this.choices = others;
            this.pick = this.pick < 0 ? 0 : (this.pick + 1) % others.size();
            this.pickedAt = System.currentTimeMillis();
            Toasts.show("user", "Wechseln zu " + others.get(this.pick).get("name") + "?", "Nochmal klicken zum Bestätigen – das Spiel startet neu.", -16735270, 6000L);
         });
      }
   }
}
