package de.bauloo.styles;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class GitHubPublisher {
   public static final long MAX_PACK = 99614720L;
   private static final String API = "https://api.github.com/repos/Bauloo0/bauloo-styles-cloud";
   private static final String BRANCH = "main";
   private static final String AUTHOR_NAME = "Bauloo0";
   private static final String AUTHOR_MAIL = "329405819+Bauloo0@users.noreply.github.com";
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15L)).build();
   private static final char[] BLOB_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

   private GitHubPublisher() {
   }

   public static Path tokenFile(Path keyFile) {
      return keyFile.resolveSibling("github-token.txt");
   }

   public static boolean hasToken(Path keyFile) {
      return readToken(keyFile) != null;
   }

   private static String readToken(Path keyFile) {
      try {
         Path file = tokenFile(keyFile);
         if (!Files.exists(file)) {
            return null;
         } else {
            String token = Files.readString(file).trim();
            return token.matches("[A-Za-z0-9_]{20,255}") ? token : null;
         }
      } catch (IOException var3) {
         return null;
      }
   }

   public static GitHubPublisher.Result check(String token) {
      try {
         HttpResponse<String> response = send(token, "GET", "", null);
         if (response.statusCode() != 200) {
            return new GitHubPublisher.Result(false, "GitHub did not accept this token (" + response.statusCode() + ")");
         } else {
            JsonObject repo = JsonParser.parseString(response.body()).getAsJsonObject();
            boolean push = repo.has("permissions") && repo.getAsJsonObject("permissions").get("push").getAsBoolean();
            return push
               ? new GitHubPublisher.Result(true, "GitHub token saved - big packs publish directly")
               : new GitHubPublisher.Result(false, "This token cannot write to bauloo-styles-cloud");
         }
      } catch (Exception var4) {
         return new GitHubPublisher.Result(false, "Could not reach GitHub");
      }
   }

   public static GitHubPublisher.Result publish(Path keyFile, String category, String name, Path zip, Path png) {
      String token = readToken(keyFile);
      if (token == null) {
         return new GitHubPublisher.Result(false, "No GitHub token on this PC");
      } else {
         try {
            byte[] pack = Files.readAllBytes(zip);
            if (pack.length != 0 && pack.length <= 99614720L) {
               byte[] preview = png != null && Files.exists(png) ? Files.readAllBytes(png) : null;
               String id = category + "_" + slug(name);
               String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pack));
               return commit(token, "Publish " + category + " style: " + name, index -> {
                  JsonArray styles = index.getAsJsonArray("styles");
                  JsonObject existing = find(styles, id);
                  JsonObject entry = new JsonObject();
                  entry.addProperty("id", id);
                  entry.addProperty("category", category);
                  entry.addProperty("name", name);
                  entry.addProperty("sha256", sha);
                  entry.addProperty("size", pack.length);
                  entry.addProperty("added", existing != null && existing.has("added") ? existing.get("added").getAsString() : LocalDate.now().toString());
                  List<GitHubPublisher.FileChange> files = new ArrayList<>();
                  files.add(new GitHubPublisher.FileChange("packs/" + id + ".zip", pack));
                  if (preview != null) {
                     entry.addProperty("preview", "previews/" + id + ".png");
                     files.add(new GitHubPublisher.FileChange("previews/" + id + ".png", preview));
                  } else if (existing != null && existing.has("preview")) {
                     entry.add("preview", existing.get("preview"));
                  }

                  if (existing != null && existing.has("by")) {
                     entry.add("by", existing.get("by"));
                  }

                  if (existing != null) {
                     styles.remove(existing);
                  }

                  styles.add(entry);
                  return files;
               }, "Published \"" + name + "\" for everyone");
            } else {
               return new GitHubPublisher.Result(false, "Pack must be under 95 MB");
            }
         } catch (Exception var10) {
            return new GitHubPublisher.Result(false, "Publishing failed: " + var10.getMessage());
         }
      }
   }

   public static GitHubPublisher.Result remove(Path keyFile, String id) {
      String token = readToken(keyFile);
      if (token == null) {
         return new GitHubPublisher.Result(false, "No GitHub token on this PC");
      } else {
         try {
            return commit(token, "Remove style " + id, index -> {
               JsonArray styles = index.getAsJsonArray("styles");
               JsonObject existing = find(styles, id);
               if (existing == null) {
                  throw new IOException("No such style");
               } else {
                  styles.remove(existing);
                  List<GitHubPublisher.FileChange> files = new ArrayList<>();
                  files.add(new GitHubPublisher.FileChange("packs/" + id + ".zip", null));
                  if (existing.has("preview")) {
                     files.add(new GitHubPublisher.FileChange(existing.get("preview").getAsString(), null));
                  }

                  return files;
               }
            }, "Removed for everyone");
         } catch (Exception var4) {
            return new GitHubPublisher.Result(false, "Removing failed: " + var4.getMessage());
         }
      }
   }

   private static GitHubPublisher.Result commit(String token, String message, GitHubPublisher.Change change, String success) throws Exception {
      for (int attempt = 0; attempt < 3; attempt++) {
         JsonObject ref = get(token, "/git/ref/heads/main");
         String head = ref.getAsJsonObject("object").get("sha").getAsString();
         String baseTree = get(token, "/git/commits/" + head).getAsJsonObject("tree").get("sha").getAsString();
         HttpResponse<String> current = send(token, "GET", "/contents/index.json?ref=" + head, null, true);
         JsonObject index;
         if (current.statusCode() == 200) {
            index = JsonParser.parseString(current.body()).getAsJsonObject();
         } else {
            index = new JsonObject();
            index.addProperty("version", 1);
            index.add("styles", new JsonArray());
         }

         List<GitHubPublisher.FileChange> files;
         try {
            files = change.apply(index);
         } catch (IOException var21) {
            return new GitHubPublisher.Result(false, var21.getMessage());
         }

         sortNewestFirst(index);
         JsonArray tree = new JsonArray();

         for (GitHubPublisher.FileChange file : files) {
            JsonObject item = entry(file.path());
            if (file.bytes() == null) {
               item.add("sha", JsonNull.INSTANCE);
            } else {
               JsonObject blob = new JsonObject();
               blob.addProperty("content", blobText(file.bytes()));
               blob.addProperty("encoding", "base64");
               item.addProperty("sha", post(token, "/git/blobs", blob, 201).get("sha").getAsString());
            }

            tree.add(item);
         }

         JsonObject indexItem = entry("index.json");
         indexItem.addProperty("content", new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(index) + "\n");
         tree.add(indexItem);
         JsonObject treeBody = new JsonObject();
         treeBody.addProperty("base_tree", baseTree);
         treeBody.add("tree", tree);
         String newTree = post(token, "/git/trees", treeBody, 201).get("sha").getAsString();
         JsonObject person = new JsonObject();
         person.addProperty("name", "Bauloo0");
         person.addProperty("email", "329405819+Bauloo0@users.noreply.github.com");
         JsonObject commitBody = new JsonObject();
         commitBody.addProperty("message", message);
         commitBody.addProperty("tree", newTree);
         JsonArray parents = new JsonArray();
         parents.add(head);
         commitBody.add("parents", parents);
         commitBody.add("author", person);
         commitBody.add("committer", person);
         String newCommit = post(token, "/git/commits", commitBody, 201).get("sha").getAsString();
         JsonObject move = new JsonObject();
         move.addProperty("sha", newCommit);
         move.addProperty("force", false);
         HttpResponse<String> update = send(token, "PATCH", "/git/refs/heads/main", move.toString());
         if (update.statusCode() == 200) {
            return new GitHubPublisher.Result(true, success);
         }

         if (update.statusCode() != 422) {
            throw new IOException("GitHub update " + update.statusCode());
         }
      }

      return new GitHubPublisher.Result(false, "Someone else published at the same moment - try again");
   }

   private static JsonObject entry(String path) {
      JsonObject item = new JsonObject();
      item.addProperty("path", path);
      item.addProperty("mode", "100644");
      item.addProperty("type", "blob");
      return item;
   }

   private static JsonObject find(JsonArray styles, String id) {
      for (JsonElement element : styles) {
         JsonObject s = element.getAsJsonObject();
         if (s.has("id") && id.equals(s.get("id").getAsString())) {
            return s;
         }
      }

      return null;
   }

   private static void sortNewestFirst(JsonObject index) {
      List<JsonElement> list = new ArrayList<>();
      index.getAsJsonArray("styles").forEach(list::add);
      list.sort((a, b) -> added(b).compareTo(added(a)));
      JsonArray sorted = new JsonArray();
      list.forEach(sorted::add);
      index.add("styles", sorted);
   }

   private static String added(JsonElement e) {
      JsonObject o = e.getAsJsonObject();
      return o.has("added") ? o.get("added").getAsString() : "";
   }

   static String slug(String text) {
      String s = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
      return s.length() > 40 ? s.substring(0, 40) : s;
   }

   private static JsonObject get(String token, String path) throws Exception {
      HttpResponse<String> response = send(token, "GET", path, null);
      if (response.statusCode() != 200) {
         throw new IOException("GitHub " + path + " " + response.statusCode());
      } else {
         return JsonParser.parseString(response.body()).getAsJsonObject();
      }
   }

   private static JsonObject post(String token, String path, JsonObject body, int expected) throws Exception {
      HttpResponse<String> response = send(token, "POST", path, body.toString());
      if (response.statusCode() != expected) {
         throw new IOException("GitHub " + path + " " + response.statusCode());
      } else {
         return JsonParser.parseString(response.body()).getAsJsonObject();
      }
   }

   private static HttpResponse<String> send(String token, String method, String path, String body) throws Exception {
      return send(token, method, path, body, false);
   }

   private static HttpResponse<String> send(String token, String method, String path, String body, boolean raw) throws Exception {
      Builder request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/Bauloo0/bauloo-styles-cloud" + path))
         .header("Authorization", "Bearer " + token)
         .header("Accept", raw ? "application/vnd.github.raw+json" : "application/vnd.github+json")
         .header("X-GitHub-Api-Version", "2022-11-28")
         .header("User-Agent", "bauloo-styles-mod")
         .timeout(Duration.ofMinutes(5L));
      if (body == null) {
         request.method(method, BodyPublishers.noBody());
      } else {
         request.header("Content-Type", "application/json").method(method, BodyPublishers.ofString(body));
      }

      return HTTP.send(request.build(), BodyHandlers.ofString());
   }

   private static String blobText(byte[] data) {
      StringBuilder out = new StringBuilder((data.length + 2) / 3 * 4);

      for (int i = 0; i < data.length; i += 3) {
         int b0 = data[i] & 255;
         int b1 = i + 1 < data.length ? data[i + 1] & 255 : 0;
         int b2 = i + 2 < data.length ? data[i + 2] & 255 : 0;
         out.append(BLOB_CHARS[b0 >> 2]);
         out.append(BLOB_CHARS[(b0 & 3) << 4 | b1 >> 4]);
         out.append(i + 1 < data.length ? BLOB_CHARS[(b1 & 15) << 2 | b2 >> 6] : '=');
         out.append(i + 2 < data.length ? BLOB_CHARS[b2 & 63] : '=');
      }

      return out.toString();
   }

   private interface Change {
      List<GitHubPublisher.FileChange> apply(JsonObject var1) throws IOException;
   }

   private record FileChange(String path, byte[] bytes) {
   }

   public record Result(boolean ok, String message) {
   }
}
