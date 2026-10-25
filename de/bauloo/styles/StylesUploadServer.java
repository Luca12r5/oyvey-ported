package de.bauloo.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

public final class StylesUploadServer {
   private static final long MAX_UPLOAD = 314572800L;
   private static final long MAX_IMAGE = 8388608L;
   private static final long MAX_JSON = 65536L;
   private static final long IDLE_MS = 1800000L;
   private static ServerSocket server;
   private static String token;
   private static volatile long lastUse;
   private static final Map<String, StylesUploadServer.Pending> PENDING = new ConcurrentHashMap<>();
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static volatile String cachedRole;
   private static volatile String cachedName;
   private static volatile long cachedAt;
   private static volatile String lastKeyError;

   private StylesUploadServer() {
   }

   public static synchronized void open() {
      try {
         if (server == null || server.isClosed()) {
            start();
         }

         lastUse = System.currentTimeMillis();
         Util.getPlatform().openUri(URI.create("http://127.0.0.1:" + server.getLocalPort() + "/?t=" + token));
      } catch (IOException var1) {
         BaulooStylesClient.LOGGER.warn("Upload page could not start: {}", var1.toString());
      }
   }

   private static void start() throws IOException {
      server = new ServerSocket(0, 20, InetAddress.getLoopbackAddress());
      server.setSoTimeout(60000);
      byte[] bytes = new byte[16];
      new SecureRandom().nextBytes(bytes);
      token = HexFormat.of().formatHex(bytes);
      Thread thread = new Thread(StylesUploadServer::acceptLoop, "Bauloo Styles upload page");
      thread.setDaemon(true);
      thread.start();
   }

   private static void acceptLoop() {
      ServerSocket socket = server;

      while (!socket.isClosed()) {
         try {
            Socket client = socket.accept();
            Thread worker = new Thread(() -> handle(client), "Bauloo Styles upload request");
            worker.setDaemon(true);
            worker.start();
         } catch (SocketTimeoutException var3) {
            if (System.currentTimeMillis() - lastUse > 1800000L) {
               stop();
            }
         } catch (IOException var4) {
            return;
         }
      }
   }

   private static synchronized void stop() {
      try {
         if (server != null) {
            server.close();
         }
      } catch (IOException var2) {
      }

      for (StylesUploadServer.Pending pending : PENDING.values()) {
         deleteQuietly(pending.zip);
         deleteQuietly(pending.preview);
         pending.autoPreviews.values().forEach(StylesUploadServer::deleteQuietly);
      }

      PENDING.clear();
   }

   private static Path tempDir() throws IOException {
      Path dir = FabricLoader.getInstance().getConfigDir().resolve("bauloo-styles/upload-tmp");
      Files.createDirectories(dir);
      return dir;
   }

   private static boolean canPublish() {
      return GitHubPublisher.hasToken(keyFile());
   }

   public static boolean hasKeyRole() {
      return role(false) != null;
   }

   public static boolean ownerPc() {
      return canPublish();
   }

   static String deviceId() {
      Path file = keyFile().resolveSibling("device-id.txt");

      try {
         if (Files.exists(file)) {
            String id = Files.readString(file).trim();
            if (id.matches("[0-9a-f]{64}")) {
               return id;
            }
         }

         byte[] bytes = new byte[32];
         new SecureRandom().nextBytes(bytes);
         String id = HexFormat.of().formatHex(bytes);
         Files.createDirectories(file.getParent());
         Files.writeString(file, id);
         return id;
      } catch (IOException var3) {
         return "";
      }
   }

   private static Path keyFile() {
      return Path.of(System.getProperty("user.home"), ".bauloo-styles", "publish-token.txt");
   }

   private static String readKey() {
      try {
         Path file = keyFile();
         return Files.exists(file) ? Files.readString(file).trim() : "";
      } catch (IOException var1) {
         return "";
      }
   }

   private static HttpResponse<String> worker(String method, String path, String key, String contentType, byte[] body) throws IOException, InterruptedException {
      String base = StylesCloud.publishUrl();
      if (base == null) {
         throw new IOException("Online publishing is not set up yet");
      } else {
         Builder request = HttpRequest.newBuilder(URI.create(base + path))
            .timeout(Duration.ofMinutes(2L))
            .header("Authorization", "Bearer " + key)
            .header("X-Device", deviceId());
         if (body == null) {
            request.method(method, BodyPublishers.noBody());
         } else {
            request.header("Content-Type", contentType).method(method, BodyPublishers.ofByteArray(body));
         }

         return HTTP.send(request.build(), BodyHandlers.ofString());
      }
   }

   public static boolean isAdmin() {
      return "admin".equals(role(false));
   }

   public static String keyRole() {
      return role(false);
   }

   public static String savedKey() {
      return readKey();
   }

   private static String role(boolean fresh) {
      String key = readKey();
      if (!key.isEmpty() && StylesCloud.publishUrl() != null) {
         if (!fresh && System.currentTimeMillis() - cachedAt < 60000L) {
            return cachedRole;
         } else {
            try {
               HttpResponse<String> response = worker("GET", "/whoami", key, null, null);
               JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
               boolean ok = response.statusCode() == 200 && body.get("ok").getAsBoolean();
               cachedRole = ok ? str(body, "role") : null;
               cachedName = ok ? str(body, "name") : null;
               lastKeyError = ok ? null : str(body, "message");
            } catch (Exception var5) {
               cachedRole = null;
               cachedName = null;
               lastKeyError = "Could not reach the publish server";
            }

            cachedAt = System.currentTimeMillis();
            return cachedRole;
         }
      } else {
         return null;
      }
   }

   private static void relay(OutputStream out, String method, String path, JsonObject body) throws IOException {
      try {
         HttpResponse<String> response = worker(
            method, path, readKey(), "application/json", body == null ? null : body.toString().getBytes(StandardCharsets.UTF_8)
         );
         JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
         json(out, response.statusCode() == 200 ? 200 : 400, answer);
      } catch (Exception var6) {
         json(out, 502, message(false, "Could not reach the publish server"));
      }
   }

   private static byte[] multipart(String boundary, Map<String, String> fields, Path pack, Path preview) throws IOException {
      ByteArrayOutputStream body = new ByteArrayOutputStream();

      for (Entry<String, String> field : fields.entrySet()) {
         body.write(
            ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n" + field.getValue() + "\r\n")
               .getBytes(StandardCharsets.UTF_8)
         );
      }

      body.write(
         ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"pack\"; filename=\"pack.zip\"\r\nContent-Type: application/zip\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8)
      );
      body.write(Files.readAllBytes(pack));
      body.write("\r\n".getBytes(StandardCharsets.UTF_8));
      if (preview != null && Files.exists(preview)) {
         body.write(
            ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"preview\"; filename=\"preview.png\"\r\nContent-Type: image/png\r\n\r\n")
               .getBytes(StandardCharsets.UTF_8)
         );
         body.write(Files.readAllBytes(preview));
         body.write("\r\n".getBytes(StandardCharsets.UTF_8));
      }

      body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
      return body.toByteArray();
   }

   private static void handle(Socket socket) {
      try {
         Socket e = socket;

         try {
            socket.setSoTimeout(120000);
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();
            String requestLine = readLine(in);
            if (requestLine != null) {
               String[] parts = requestLine.split(" ");
               if (parts.length >= 2) {
                  String method = parts[0];
                  String target = parts[1];
                  Map<String, String> headers = new HashMap<>();

                  String line;
                  while ((line = readLine(in)) != null && !line.isEmpty()) {
                     int colon = line.indexOf(58);
                     if (colon > 0) {
                        headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT), line.substring(colon + 1).trim());
                     }
                  }

                  lastUse = System.currentTimeMillis();
                  int port = socket.getLocalPort();
                  String host = headers.getOrDefault("host", "");
                  if (!host.equals("127.0.0.1:" + port) && !host.equals("localhost:" + port)) {
                     send(out, 403, "text/plain", "Forbidden");
                  } else {
                     String origin = headers.get("origin");
                     if (origin != null && !origin.equals("http://127.0.0.1:" + port) && !origin.equals("http://localhost:" + port)) {
                        send(out, 403, "text/plain", "Forbidden");
                     } else {
                        String path = target;
                        Map<String, String> query = new HashMap<>();
                        int q = target.indexOf(63);
                        if (q >= 0) {
                           path = target.substring(0, q);

                           for (String pair : target.substring(q + 1).split("&")) {
                              int eq = pair.indexOf(61);
                              if (eq > 0) {
                                 query.put(
                                    URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                                    URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8)
                                 );
                              }
                           }
                        }

                        String given = query.getOrDefault("t", "");
                        if (!MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                           send(out, 403, "text/plain", "This link has expired. Open the upload page from the styles menu again.");
                        } else {
                           long length = 0L;

                           try {
                              length = Long.parseLong(headers.getOrDefault("content-length", "0"));
                           } catch (NumberFormatException var22) {
                           }

                           if (length >= 0L && length <= 314572800L) {
                              route(method, path, query, length, in, out);
                           } else {
                              send(out, 413, "text/plain", "Too big");
                           }
                        }
                     }
                  }
               }
            }
         } catch (Throwable var23) {
            if (socket != null) {
               try {
                  e.close();
               } catch (Throwable var21) {
                  var23.addSuppressed(var21);
               }
            }

            throw var23;
         }

         if (socket != null) {
            socket.close();
         }
      } catch (Exception var24) {
         BaulooStylesClient.LOGGER.warn("Upload page request failed: {}", var24.toString());
      }
   }

   private static void route(String method, String path, Map<String, String> query, long length, InputStream in, OutputStream out) throws IOException {
      if ("GET".equals(method) && "/".equals(path)) {
         try (InputStream page = StylesUploadServer.class.getResourceAsStream("/assets/bauloo_styles/upload/index.html")) {
            if (page != null) {
               String html = new String(page.readAllBytes(), StandardCharsets.UTF_8);
               sendBytes(out, 200, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
               return;
            }

            send(out, 500, "text/plain", "Page missing");
         }

         return;
      } else if ("GET".equals(method) && path.matches("/img/[a-z0-9_]+\\.png")) {
         try (InputStream image = StylesUploadServer.class.getResourceAsStream("/assets/bauloo_styles/textures/preview/" + path.substring(5))) {
            if (image == null) {
               send(out, 404, "text/plain", "Not found");
            } else {
               sendBytes(out, 200, "image/png", image.readAllBytes());
            }
         }
      } else if ("GET".equals(method) && "/api/icon".equals(path)) {
         StylesUploadServer.Pending pending = PENDING.get(query.getOrDefault("id", ""));
         Path picture = pending == null ? null : picture(pending, query.getOrDefault("category", ""));
         if (picture != null && Files.exists(picture)) {
            sendBytes(out, 200, "image/png", Files.readAllBytes(picture));
         } else {
            send(out, 404, "text/plain", "Not found");
         }
      } else if ("GET".equals(method) && "/api/state".equals(path)) {
         JsonObject state = new JsonObject();
         String role = role(false);
         state.addProperty("canPublish", canPublish() || role != null && !"vip".equals(role) && !"media".equals(role));
         state.addProperty("git", canPublish());
         state.addProperty("online", StylesCloud.publishUrl() != null);
         if (role != null) {
            state.addProperty("role", role);
            state.addProperty("who", cachedName);
         }

         state.addProperty("accent", String.format("#%06X", StylesUiConfig.accent & 16777215));
         JsonArray cloudCategories = new JsonArray();

         for (CloudCategories.Def d : CloudCategories.all()) {
            JsonObject c = new JsonObject();
            c.addProperty("id", d.id());
            c.addProperty("name", d.name());
            cloudCategories.add(c);
         }

         state.add("categories", cloudCategories);
         JsonArray uploads = new JsonArray();

         for (String category : StylesPackFilter.categories()) {
            for (Path file : StylesUploadManager.list(category)) {
               String name = file.getFileName().toString();
               if (name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                  JsonObject o = new JsonObject();
                  o.addProperty("category", category);
                  o.addProperty("file", name);
                  o.addProperty("name", name.substring(0, name.length() - 4));
                  uploads.add(o);
               }
            }
         }

         state.add("uploads", uploads);
         json(out, 200, state);
      } else if (!"POST".equals(method)) {
         send(out, 404, "text/plain", "Not found");
      } else {
         switch (path) {
            case "/api/analyze":
               analyze(query, length, in, out);
               break;
            case "/api/preview":
               preview(query, length, in, out);
               break;
            case "/api/add":
               add(readJson(in, length), out, false);
               break;
            case "/api/publish":
               add(readJson(in, length), out, true);
               break;
            case "/api/key":
               JsonObject bodyx = readJson(in, length);
               String key = bodyx == null ? "" : str(bodyx, "key").trim();
               if (key.length() < 16 || key.length() > 200 || !key.matches("[\\x21-\\x7E]+")) {
                  json(out, 400, message(false, "That does not look like a key"));
                  return;
               }

               Path filex = keyFile();
               String previous = readKey();
               Files.createDirectories(filex.getParent());
               Files.writeString(filex, key);
               String role = role(true);
               if (role == null) {
                  if (previous.isEmpty()) {
                     Files.deleteIfExists(filex);
                  } else {
                     Files.writeString(filex, previous);
                  }

                  cachedAt = 0L;
                  String reason = StylesCloud.publishUrl() == null
                     ? "Online publishing is not set up yet"
                     : (lastKeyError != null && !lastKeyError.isEmpty() && !"Unknown key".equals(lastKeyError) ? lastKeyError : "This key is not valid");
                  json(out, 401, message(false, reason));
                  return;
               }

               JsonObject result = message(true, "Key saved - welcome, " + cachedName);
               result.addProperty("role", role);
               MyPacks.recheck();
               StylesPresence.settingChanged();
               json(out, 200, result);
               break;
            case "/api/github":
               JsonObject body = readJson(in, length);
               String gh = body == null ? "" : str(body, "token").trim();
               if (!gh.matches("[A-Za-z0-9_]{20,255}")) {
                  json(out, 400, message(false, "That does not look like a GitHub token"));
                  return;
               }

               GitHubPublisher.Result check = GitHubPublisher.check(gh);
               if (check.ok()) {
                  Path filex = GitHubPublisher.tokenFile(keyFile());
                  Files.createDirectories(filex.getParent());
                  Files.writeString(filex, gh);
               }

               json(out, check.ok() ? 200 : 400, message(check.ok(), check.message()));
               break;
            case "/api/github/clear":
               Files.deleteIfExists(GitHubPublisher.tokenFile(keyFile()));
               json(out, 200, message(true, "GitHub token removed from this PC"));
               break;
            case "/api/stats":
               JsonObject stats = StylesStats.fetch();
               if (stats == null) {
                  json(out, 502, message(false, "No stats"));
               } else {
                  json(out, 200, stats);
               }
               break;
            case "/api/online":
               if (!"admin".equals(role(false))) {
                  json(out, 403, message(false, "Only bauloo can see who plays"));
                  return;
               }

               relay(out, "GET", "/admin/online", null);
               break;
            case "/api/key/clear":
               Files.deleteIfExists(keyFile());
               cachedRole = null;
               cachedName = null;
               cachedAt = 0L;
               MyPacks.recheck();
               StylesPresence.settingChanged();
               json(out, 200, message(true, "Key removed from this PC"));
               break;
            case "/api/keys/list":
            case "/api/keys/create":
            case "/api/keys/revoke":
               JsonObject keysBody = readJson(in, length);
               if (!"admin".equals(role(false))) {
                  json(out, 403, message(false, "Only bauloo can manage keys"));
                  return;
               }
               String workerMethod = switch (path) {
                  case "/api/keys/list" -> "GET";
                  case "/api/keys/create" -> "POST";
                  default -> "DELETE";
               };
               relay(out, workerMethod, "/admin/keys", "GET".equals(workerMethod) ? null : keysBody);
               break;
            case "/api/cloud/list":
               cloudList(out);
               break;
            case "/api/cloud/remove":
               cloudRemove(readJson(in, length), out);
               break;
            case "/api/delete":
               JsonObject delBody = readJson(in, length);
               boolean ok = delBody != null && StylesUploadManager.delete(str(delBody, "category"), str(delBody, "file"));
               json(out, ok ? 200 : 400, message(ok, ok ? "Removed" : "Could not remove that upload"));
               break;
            default:
               send(out, 404, "text/plain", "Not found");
         }
      }
   }

   private static void cloudList(OutputStream out) throws IOException {
      String role = role(false);
      boolean git = canPublish();
      JsonObject result = new JsonObject();
      result.addProperty("ok", true);
      JsonArray list = new JsonArray();

      try {
         HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/Bauloo0/bauloo-styles-cloud/contents/index.json?ref=main"))
            .header("Accept", "application/vnd.github.raw+json")
            .header("User-Agent", "bauloo-styles-mod")
            .timeout(Duration.ofSeconds(15L))
            .GET()
            .build();
         HttpResponse<String> response = HTTP.send(request, BodyHandlers.ofString());
         if (response.statusCode() != 200) {
            json(out, 502, message(false, "Could not load the published styles"));
            return;
         }

         JsonObject index = JsonParser.parseString(response.body()).getAsJsonObject();

         for (JsonElement element : index.getAsJsonArray("styles")) {
            JsonObject s = element.getAsJsonObject();
            String by = str(s, "by");
            boolean mine = "admin".equals(role) || role != null && !by.isEmpty() && by.equals(cachedName) || git && role == null;
            JsonObject o = new JsonObject();
            o.addProperty("id", str(s, "id"));
            o.addProperty("category", str(s, "category"));
            o.addProperty("name", str(s, "name"));
            o.addProperty("by", by.isEmpty() ? "bauloo" : by);
            o.addProperty("added", str(s, "added"));
            o.addProperty("canRemove", mine);
            list.add(o);
         }
      } catch (Exception var14) {
         json(out, 502, message(false, "Could not load the published styles"));
         return;
      }

      result.add("styles", list);
      json(out, 200, result);
   }

   private static void cloudRemove(JsonObject body, OutputStream out) throws IOException {
      String id = body == null ? "" : str(body, "id");
      String category = id.contains("_") ? id.substring(0, id.indexOf(95)) : "";
      if (!id.matches("[a-z][a-z0-9]{1,15}_[a-z0-9_]{1,40}") || !CloudCategories.known(category)) {
         json(out, 400, message(false, "Unknown style"));
      } else if (canPublish()) {
         GitHubPublisher.Result result = GitHubPublisher.remove(keyFile(), id);
         if (result.ok()) {
            StylesCloud.refresh(true);
         }

         json(out, result.ok() ? 200 : 400, message(result.ok(), result.message()));
      } else if (role(false) != null) {
         JsonObject payload = new JsonObject();
         payload.addProperty("id", id);

         try {
            HttpResponse<String> response = worker("POST", "/remove", readKey(), "application/json", payload.toString().getBytes(StandardCharsets.UTF_8));
            JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
            if (response.statusCode() == 200) {
               StylesCloud.refresh(true);
            }

            json(out, response.statusCode() == 200 ? 200 : 400, answer);
         } catch (Exception var7) {
            json(out, 502, message(false, "Could not reach the publish server"));
         }
      } else {
         json(out, 403, message(false, "Removing needs a key from bauloo"));
      }
   }

   private static void analyze(Map<String, String> query, long length, InputStream in, OutputStream out) throws IOException {
      String fileName = query.getOrDefault("name", "pack.zip");
      if (!fileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
         json(out, 400, message(false, "Please drop a .zip texture pack"));
      } else {
         byte[] idBytes = new byte[8];
         new SecureRandom().nextBytes(idBytes);
         String id = HexFormat.of().formatHex(idBytes);
         Path zip = tempDir().resolve(id + ".zip");
         copyExactly(in, zip, length);

         Map<String, Integer> found;
         try {
            found = StylesPackFilter.analyze(zip);
         } catch (IOException var19) {
            deleteQuietly(zip);
            json(out, 400, message(false, "That file is not a readable zip"));
            return;
         }

         StylesUploadServer.Pending pending = new StylesUploadServer.Pending(zip, fileName);
         PENDING.put(id, pending);
         Path icon = tempDir().resolve(id + ".png");
         if (packIcon(zip, icon)) {
            pending.preview = icon;
         }

         JsonArray auto = new JsonArray();

         for (String category : StylesPackFilter.categories()) {
            if (found.getOrDefault(category, 0) != 0) {
               Path picture = tempDir().resolve(id + "-" + category + ".png");
               boolean made = "sky".equals(category) ? skyPreview(zip, picture) : StylesAutoPreview.render(zip, category, picture);
               if (made) {
                  pending.autoPreviews.put(category, picture);
                  auto.add(category);
               }
            }
         }

         JsonObject result = new JsonObject();
         result.addProperty("ok", true);
         result.addProperty("icon", pending.preview != null);
         result.add("auto", auto);
         result.addProperty("id", id);
         result.addProperty("name", cleanName(fileName.substring(0, fileName.length() - 4)));
         JsonObject counts = new JsonObject();
         found.forEach(counts::addProperty);
         result.add("found", counts);
         json(out, 200, result);
      }
   }

   private static boolean packIcon(Path zip, Path target) {
      try {
         boolean var10;
         try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
               if ("pack.png".equalsIgnoreCase(entry.getName())) {
                  byte[] data = in.readNBytes(8388609);
                  if (data.length > 8388608L) {
                     return false;
                  }

                  BufferedImage source = ImageIO.read(new ByteArrayInputStream(data));
                  if (source == null) {
                     return false;
                  }

                  writeSquare(source, target);
                  return true;
               }
            }

            var10 = false;
         }

         return var10;
      } catch (RuntimeException | IOException var9) {
         return false;
      }
   }

   static boolean skyPreview(Path zip, Path target) {
      try {
         boolean var41;
         try (ZipFile file = new ZipFile(zip.toFile())) {
            ZipEntry best = null;
            long bestScore = 0L;

            for (ZipEntry entry : Collections.list(file.entries())) {
               String name = entry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
               boolean skyTexture = (name.startsWith("assets/minecraft/optifine/sky/") || name.startsWith("assets/minecraft/mcpatcher/sky/"))
                  && name.endsWith(".png");
               if (!entry.isDirectory() && skyTexture) {
                  int[] size = imageSize(file, entry);
                  if (size != null && size[0] * 2 == size[1] * 3 && size[0] >= 300) {
                     String base = name.substring(name.lastIndexOf(47) + 1);
                     boolean overlay = base.contains("star")
                        || base.contains("night")
                        || base.contains("flare")
                        || base.contains("rainbow")
                        || base.contains("moon")
                        || base.contains("sun");
                     long score = (long)size[0] * size[1] + Math.min(entry.getSize(), 2147483647L) / 1024L;
                     if (overlay) {
                        score /= 16L;
                     }

                     if (score > bestScore) {
                        bestScore = score;
                        best = entry;
                     }
                  }
               }
            }

            if (best == null) {
               return false;
            }

            try (
               InputStream in = file.getInputStream(best);
               ImageInputStream stream = ImageIO.createImageInputStream(in);
            ) {
               Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
               if (!readers.hasNext()) {
                  return false;
               }

               ImageReader reader = readers.next();

               try {
                  reader.setInput(stream);
                  int face = reader.getWidth(0) / 3;
                  ImageReadParam param = reader.getDefaultReadParam();
                  param.setSourceRegion(new Rectangle(face, face, face, face));
                  int step = Math.max(1, face / 512);
                  param.setSourceSubsampling(step, step, 0, 0);
                  writeSquare(reader.read(0, param), target);
                  var41 = true;
               } finally {
                  reader.dispose();
               }
            }
         }

         return var41;
      } catch (RuntimeException | IOException var31) {
         return false;
      }
   }

   private static int[] imageSize(ZipFile file, ZipEntry entry) {
      try {
         Object var20;
         try (
            InputStream in = file.getInputStream(entry);
            ImageInputStream stream = ImageIO.createImageInputStream(in);
         ) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (readers.hasNext()) {
               ImageReader reader = readers.next();

               try {
                  reader.setInput(stream);
                  return new int[]{reader.getWidth(0), reader.getHeight(0)};
               } finally {
                  reader.dispose();
               }
            }

            var20 = null;
         }

         return (int[])var20;
      } catch (RuntimeException | IOException var19) {
         return null;
      }
   }

   private static void writeSquare(BufferedImage source, Path target) throws IOException {
      int side = Math.min(source.getWidth(), source.getHeight());
      BufferedImage square = new BufferedImage(256, 256, 2);
      Graphics2D g = square.createGraphics();
      g.setRenderingHint(
         RenderingHints.KEY_INTERPOLATION, side < 256 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BICUBIC
      );
      g.drawImage(
         source,
         0,
         0,
         256,
         256,
         (source.getWidth() - side) / 2,
         (source.getHeight() - side) / 2,
         (source.getWidth() + side) / 2,
         (source.getHeight() + side) / 2,
         null
      );
      g.dispose();
      ImageIO.write(square, "png", target.toFile());
   }

   private static Path picture(StylesUploadServer.Pending pending, String category) {
      Path auto = pending.autoPreviews.get(category);
      return !pending.customPreview && auto != null ? auto : pending.preview;
   }

   private static void preview(Map<String, String> query, long length, InputStream in, OutputStream out) throws IOException {
      StylesUploadServer.Pending pending = PENDING.get(query.getOrDefault("id", ""));
      if (pending != null && length <= 8388608L) {
         byte[] data = readExact(in, length);
         BufferedImage source = ImageIO.read(new ByteArrayInputStream(data));
         if (source == null) {
            json(out, 400, message(false, "Use a PNG or JPG picture"));
         } else {
            Path png = tempDir().resolve(query.get("id") + ".png");
            writeSquare(source, png);
            pending.preview = png;
            pending.customPreview = true;
            json(out, 200, message(true, "Picture ready"));
         }
      } else {
         json(out, 400, message(false, "Picture could not be used"));
      }
   }

   private static void add(JsonObject body, OutputStream out, boolean publish) throws IOException {
      if (body == null) {
         json(out, 400, message(false, "Bad request"));
      } else {
         StylesUploadServer.Pending pending = PENDING.get(str(body, "id"));
         String category = str(body, "category");
         String name = cleanName(str(body, "name"));
         if (pending == null || !List.of(StylesPackFilter.categories()).contains(category)) {
            json(out, 400, message(false, "Drop the pack again - the upload expired"));
         } else if (name.isEmpty()) {
            json(out, 400, message(false, "Give the style a name"));
         } else {
            boolean viaGit = publish && canPublish();
            if (!publish || viaGit || role(false) != null && !"vip".equals(role(false)) && !"media".equals(role(false))) {
               Path folder = publish ? tempDir().resolve("publish") : StylesUploadManager.root().resolve(category);
               Files.createDirectories(folder);
               String base = name;
               int n = 2;

               while (!publish && Files.exists(folder.resolve(base + ".zip"))) {
                  base = name + " (" + n++ + ")";
               }

               Path zip = folder.resolve(base + ".zip");

               int textures;
               try {
                  textures = StylesPackFilter.extract(pending.zip, category, zip);
               } catch (IOException var31) {
                  json(out, 400, message(false, "The pack could not be read: " + var31.getMessage()));
                  return;
               }

               if (textures == 0) {
                  json(out, 400, message(false, "This pack has no " + category + " textures"));
               } else {
                  Path png = folder.resolve(base + ".png");
                  Path picture = picture(pending, category);
                  if (picture != null && Files.exists(picture)) {
                     Files.copy(picture, png, StandardCopyOption.REPLACE_EXISTING);
                  }

                  if (!publish) {
                     StylesUploadManager.changed();
                     JsonObject result = message(true, "Added \"" + base + "\" with " + textures + " textures");
                     result.addProperty("textures", textures);
                     json(out, 200, result);
                  } else if (!viaGit) {
                     try {
                        String boundary = "----bauloo" + Long.toHexString(new SecureRandom().nextLong());
                        Map<String, String> fields = new LinkedHashMap<>();
                        fields.put("category", category);
                        fields.put("name", name);
                        HttpResponse<String> response = worker(
                           "POST", "/publish", readKey(), "multipart/form-data; boundary=" + boundary, multipart(boundary, fields, zip, png)
                        );
                        JsonObject answer = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (response.statusCode() == 200) {
                           StylesCloud.refresh(true);
                        }

                        json(out, response.statusCode() == 200 ? 200 : 400, answer);
                     } catch (Exception var29) {
                        json(out, 502, message(false, "Could not reach the publish server"));
                     } finally {
                        deleteQuietly(zip);
                        deleteQuietly(png);
                     }
                  } else {
                     try {
                        GitHubPublisher.Result result = GitHubPublisher.publish(keyFile(), category, name, zip, png);
                        if (result.ok()) {
                           StylesCloud.refresh(true);
                        }

                        json(out, result.ok() ? 200 : 400, message(result.ok(), result.message()));
                     } finally {
                        deleteQuietly(zip);
                        deleteQuietly(png);
                     }
                  }
               }
            } else {
               json(out, 403, message(false, "Publishing for everyone needs an upload key from bauloo"));
            }
         }
      }
   }

   private static String cleanName(String value) {
      if (value == null) {
         return "";
      } else {
         String cleaned = value.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "").replace('_', ' ').trim();
         return cleaned.length() > 40 ? cleaned.substring(0, 40).trim() : cleaned;
      }
   }

   private static String str(JsonObject o, String key) {
      return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : "";
   }

   private static JsonObject message(boolean ok, String text) {
      JsonObject o = new JsonObject();
      o.addProperty("ok", ok);
      o.addProperty("message", text);
      return o;
   }

   private static JsonObject readJson(InputStream in, long length) {
      try {
         if (length > 65536L) {
            return null;
         } else {
            byte[] data = readExact(in, length);
            return JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
         }
      } catch (Exception var4) {
         return null;
      }
   }

   private static byte[] readExact(InputStream in, long length) throws IOException {
      byte[] data = new byte[(int)length];
      int offset = 0;

      while (offset < data.length) {
         int read = in.read(data, offset, data.length - offset);
         if (read < 0) {
            throw new IOException("Upload ended early");
         }

         offset += read;
      }

      return data;
   }

   private static void copyExactly(InputStream in, Path file, long length) throws IOException {
      try (OutputStream fileOut = Files.newOutputStream(file)) {
         byte[] buffer = new byte[65536];
         long left = length;

         while (left > 0L) {
            int read = in.read(buffer, 0, (int)Math.min((long)buffer.length, left));
            if (read < 0) {
               throw new IOException("Upload ended early");
            }

            fileOut.write(buffer, 0, read);
            left -= read;
         }
      }
   }

   private static String readLine(InputStream in) throws IOException {
      ByteArrayOutputStream line = new ByteArrayOutputStream();

      int b;
      while ((b = in.read()) != -1 && b != 10) {
         if (b != 13) {
            line.write(b);
         }

         if (line.size() > 8192) {
            throw new IOException("Header too long");
         }
      }

      return b == -1 && line.size() == 0 ? null : line.toString(StandardCharsets.UTF_8);
   }

   private static void json(OutputStream out, int status, JsonObject body) throws IOException {
      sendBytes(out, status, "application/json", body.toString().getBytes(StandardCharsets.UTF_8));
   }

   private static void send(OutputStream out, int status, String type, String body) throws IOException {
      sendBytes(out, status, type + "; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
   }

   private static void sendBytes(OutputStream out, int status, String type, byte[] body) throws IOException {
      String head = "HTTP/1.1 "
         + status
         + " "
         + (status == 200 ? "OK" : "Error")
         + "\r\nContent-Type: "
         + type
         + "\r\nContent-Length: "
         + body.length
         + "\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nReferrer-Policy: no-referrer\r\nContent-Security-Policy: default-src 'self'; img-src 'self' blob: data:; style-src 'unsafe-inline'; script-src 'unsafe-inline'; connect-src 'self'\r\nConnection: close\r\n\r\n";
      out.write(head.getBytes(StandardCharsets.US_ASCII));
      out.write(body);
      out.flush();
   }

   private static void deleteQuietly(Path file) {
      if (file != null) {
         try {
            Files.deleteIfExists(file);
         } catch (IOException var2) {
         }
      }
   }

   private static final class Pending {
      final Path zip;
      final String fileName;
      Path preview;
      final Map<String, Path> autoPreviews = new ConcurrentHashMap<>();
      boolean customPreview;

      Pending(Path zip, String fileName) {
         this.zip = zip;
         this.fileName = fileName;
      }
   }
}
