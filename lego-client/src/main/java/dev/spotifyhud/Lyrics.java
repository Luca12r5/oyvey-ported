package dev.spotifyhud;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lyrics {
   private static final Pattern WORD_TAG = Pattern.compile("<(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?>");
   private static final String UA = "SpotifyHUD-Minecraft/1.1 (Fabric mod)";
   private static final Pattern TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
   private static final Pattern OFFSET = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\s*]", 2);
   private final ExecutorService exec = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1 = new Thread(var0, "SpotifyHUD-Lyrics");
      var1.setDaemon(true);
      return var1;
   });
   private final Map<String, Lyrics.Result> cache = Collections.synchronizedMap(new LinkedHashMap<String, Lyrics.Result>(32, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Lyrics.Result> var1) {
         return this.size() > 40;
      }
   });
   private volatile Lyrics.Result current = new Lyrics.Result("", Lyrics.Status.NONE, List.of());
   private volatile String requested = "";
   private volatile long errorAt = 0L;

   public Lyrics.Result current() {
      return this.current;
   }

   public void update(PlayerState var1) {
      if (var1 != null && var1.hasTrack && var1.title != null && !var1.title.isBlank()) {
         String var2 = key(var1.title, var1.artist);
         if (var2.equals(this.requested)) {
            if (this.current.status != Lyrics.Status.ERROR || System.currentTimeMillis() - this.errorAt <= 20000L) {
               return;
            }

            this.requested = "";
         }

         this.requested = var2;
         Lyrics.Result var3 = this.cache.get(var2);
         if (var3 != null && var3.status != Lyrics.Status.ERROR) {
            this.current = var3;
         } else {
            this.current = new Lyrics.Result(var2, Lyrics.Status.LOADING, List.of());
            String var4 = var1.title;
            String var5 = var1.artist;
            long var6 = var1.durationMs;
            this.exec.execute(() -> {
               if (var2.equals(this.requested)) {
                  Lyrics.Result var6x = this.fetch(var2, var4, var5, var6);
                  if (var6x.status != Lyrics.Status.ERROR) {
                     this.cache.put(var2, var6x);
                  } else {
                     this.errorAt = System.currentTimeMillis();
                  }

                  if (var2.equals(this.requested)) {
                     this.current = var6x;
                  }
               }
            });
         }
      } else {
         if (!this.requested.isEmpty()) {
            this.requested = "";
            this.current = new Lyrics.Result("", Lyrics.Status.NONE, List.of());
         }
      }
   }

   public void retry() {
      String var1 = this.requested;
      this.requested = "";
      this.cache.remove(var1);
   }

   static String key(String var0, String var1) {
      return (var0 + var1).toLowerCase(Locale.ROOT);
   }

   private Lyrics.Result fetch(String var1, String var2, String var3, long var4) {
      String var6 = firstArtist(var3);
      String var7 = cleanTitle(var2);

      try {
         Lyrics.Http var8 = null;

         for (String var12 : var6.equals(var3) ? new String[]{var3} : new String[]{var3, var6}) {
            StringBuilder var13 = new StringBuilder("https://lrclib.net/api/get?track_name=").append(enc(var7)).append("&artist_name=").append(enc(var12));
            if (var4 > 0L) {
               var13.append("&duration=").append(Math.round(var4 / 1000.0));
            }

            var8 = get(var13.toString());
            if (((Lyrics.Http)var8).code == 200) {
               Lyrics.Result var14 = fromRecord(var1, Json.parse(((Lyrics.Http)var8).body));
               if (var14 != null && (var14.status == Lyrics.Status.SYNCED || var14.status == Lyrics.Status.INSTRUMENTAL)) {
                  return var14;
               }
            }
         }

         var8 = get("https://lrclib.net/api/search?track_name=" + enc(var7) + "&artist_name=" + enc(var6));
         Object var18 = var8.code == 200 ? pick(Json.parse(var8.body), var4) : null;
         if (var18 == null) {
            var8 = get("https://lrclib.net/api/search?q=" + enc(var7 + " " + var6));
            var18 = var8.code == 200 ? pick(Json.parse(var8.body), var4) : null;
         }

         if (var18 != null) {
            Lyrics.Result var19 = fromRecord(var1, var18);
            if (var19 != null) {
               return var19;
            }
         }

         return var8.code < 500 && var8.code != 429 && var8.code != -1
            ? new Lyrics.Result(var1, Lyrics.Status.NOT_FOUND, List.of())
            : new Lyrics.Result(var1, Lyrics.Status.ERROR, List.of());
      } catch (Exception var15) {
         SpotifyHudMod.LOG("Lyrics-Fehler: " + var15);
         return new Lyrics.Result(var1, Lyrics.Status.ERROR, List.of());
      }
   }

   private static Object pick(Object var0, long var1) {
      if (!(var0 instanceof List)) {
         return null;
      } else {
         Object var3 = null;
         double var4 = Double.MAX_VALUE;

         for (Object var7 : (List)var0) {
            String var8 = Json.str(var7, "syncedLyrics", null);
            boolean var9 = Json.bool(var7, "instrumental", false);
            String var10 = Json.str(var7, "plainLyrics", null);
            if (var8 != null || var9 || var10 != null) {
               double var11 = Json.dbl(var7, "duration", 0.0) * 1000.0;
               double var13 = var1 > 0L && var11 > 0.0 ? Math.abs(var11 - var1) : 5000.0;
               if (var1 <= 0L || !(var13 > 15000.0)) {
                  double var15 = var13 + (var8 != null ? 0 : '\uea60') + (var9 ? 30000 : 0);
                  if (var15 < var4) {
                     var4 = var15;
                     var3 = var7;
                  }
               }
            }
         }

         return var3;
      }
   }

   private static Lyrics.Result fromRecord(String var0, Object var1) {
      if (var1 == null) {
         return null;
      } else if (Json.bool(var1, "instrumental", false)) {
         return new Lyrics.Result(var0, Lyrics.Status.INSTRUMENTAL, List.of());
      } else {
         String var2 = Json.str(var1, "syncedLyrics", null);
         if (var2 != null && !var2.isBlank()) {
            List var3 = parseLrc(var2);
            if (!var3.isEmpty()) {
               return new Lyrics.Result(var0, Lyrics.Status.SYNCED, var3);
            }
         }

         String var9 = Json.str(var1, "plainLyrics", null);
         if (var9 != null && !var9.isBlank()) {
            ArrayList var4 = new ArrayList();

            for (String var8 : var9.split("\\r?\\n")) {
               if (!var8.isBlank()) {
                  var4.add(new Lyrics.Line(-1L, var8.trim()));
               }
            }

            return new Lyrics.Result(var0, Lyrics.Status.PLAIN, var4);
         } else {
            return null;
         }
      }
   }

   static List<Lyrics.Line> parseLrc(String var0) {
      long var1 = 0L;
      Matcher var3 = OFFSET.matcher(var0);
      if (var3.find()) {
         try {
            var1 = Long.parseLong(var3.group(1));
         } catch (NumberFormatException var25) {
         }
      }

      ArrayList var4 = new ArrayList();

      for (String var8 : var0.split("\\r?\\n")) {
         Matcher var9 = TAG.matcher(var8);
         ArrayList var10 = new ArrayList();

         int var11;
         for (var11 = 0; var9.find() && var9.start() == var11; var11 = var9.end()) {
            long var12 = Long.parseLong(var9.group(1));
            long var14 = Long.parseLong(var9.group(2));
            long var16 = 0L;
            if (var9.group(3) != null) {
               String var18 = var9.group(3);
               var16 = var18.length() == 1 ? Long.parseLong(var18) * 100L : (var18.length() == 2 ? Long.parseLong(var18) * 10L : Long.parseLong(var18));
            }

            var10.add(var12 * 60000L + var14 * 1000L + var16);
         }

         if (!var10.isEmpty()) {
            String var26 = var8.substring(var11);
            ArrayList var13 = new ArrayList();
            ArrayList var27 = new ArrayList();
            Matcher var15 = WORD_TAG.matcher(var26);
            int var28 = -1;

            long var17;
            for (var17 = 0L; var15.find(); var28 = var15.end()) {
               if (var28 >= 0) {
                  addWords(var13, var27, var26.substring(var28, var15.start()), var17);
               }

               var17 = tagMs(var15.group(1), var15.group(2), var15.group(3)) - var1;
            }

            if (var28 >= 0) {
               addWords(var13, var27, var26.substring(var28), var17);
            }

            String var19 = WORD_TAG.matcher(var26).replaceAll("").replaceAll("\\s+", " ").trim();

            for (long var21 : var10) {
               Lyrics.Line var23 = new Lyrics.Line(Math.max(0L, var21 - var1), var19);
               if (!var13.isEmpty() && var10.size() == 1) {
                  var23.words = var13.toArray(new String[0]);
                  var23.wordTimes = new long[var27.size()];

                  for (int var24 = 0; var24 < var27.size(); var24++) {
                     var23.wordTimes[var24] = Math.max(0L, (Long)var27.get(var24));
                  }

                  var23.exactWords = true;
               }

               var4.add(var23);
            }
         }
      }

      var4.sort((var0x, var1x) -> Long.compare(var0x.timeMs, var1x.timeMs));
      estimateWords(var4);
      return var4;
   }

   private static long tagMs(String var0, String var1, String var2) {
      long var3 = 0L;
      if (var2 != null) {
         var3 = var2.length() == 1 ? Long.parseLong(var2) * 100L : (var2.length() == 2 ? Long.parseLong(var2) * 10L : Long.parseLong(var2));
      }

      return Long.parseLong(var0) * 60000L + Long.parseLong(var1) * 1000L + var3;
   }

   private static void addWords(List<String> var0, List<Long> var1, String var2, long var3) {
      boolean var5 = true;

      for (String var9 : var2.trim().split("\\s+")) {
         if (!var9.isEmpty()) {
            var0.add(var9);
            var1.add(var5 ? var3 : var3 + 1L);
            var5 = false;
         }
      }
   }

   static void estimateWords(List<Lyrics.Line> var0) {
      for (int var1 = 0; var1 < var0.size(); var1++) {
         Lyrics.Line var2 = (Lyrics.Line)var0.get(var1);
         if (!var2.exactWords) {
            String[] var3 = var2.text.isEmpty() ? new String[0] : var2.text.split("\\s+");
            var2.words = var3;
            var2.wordTimes = new long[var3.length];
            if (var3.length != 0) {
               long var4 = var1 + 1 < var0.size() ? ((Lyrics.Line)var0.get(var1 + 1)).timeMs : var2.timeMs + 4000L;
               long var6 = Math.max(300L, var4 - var2.timeMs);
               long var8 = Math.min((long)(var6 * 0.92), 450L * var3.length + 900L);
               var8 = Math.max(Math.min(var6, 250L * var3.length), var8);
               double var10 = 0.0;
               double[] var12 = new double[var3.length];

               for (int var13 = 0; var13 < var3.length; var13++) {
                  var12[var13] = 1.5 + var3[var13].length();
                  var10 += var12[var13];
               }

               double var17 = 0.0;

               for (int var15 = 0; var15 < var3.length; var15++) {
                  var2.wordTimes[var15] = var2.timeMs + Math.round(var8 * var17 / var10);
                  var17 += var12[var15];
               }
            }
         }
      }
   }

   public static int indexAt(List<Lyrics.Line> var0, long var1) {
      int var3 = 0;
      int var4 = var0.size() - 1;
      int var5 = -1;

      while (var3 <= var4) {
         int var6 = var3 + var4 >>> 1;
         if (((Lyrics.Line)var0.get(var6)).timeMs <= var1) {
            var5 = var6;
            var3 = var6 + 1;
         } else {
            var4 = var6 - 1;
         }
      }

      return var5;
   }

   static String firstArtist(String var0) {
      if (var0 == null) {
         return "";
      } else {
         String var1 = var0;

         for (String var5 : new String[]{", ", " & ", " feat. ", " feat ", " ft. ", " x ", ";", " / "}) {
            int var6 = var1.toLowerCase(Locale.ROOT).indexOf(var5);
            if (var6 > 0) {
               var1 = var1.substring(0, var6);
            }
         }

         return var1.trim();
      }
   }

   static String cleanTitle(String var0) {
      String var1 = var0.replaceAll("(?i)\\s*[(\\[](feat\\.?|ft\\.?|with) [^)\\]]*[)\\]]", "");
      var1 = var1.replaceAll("(?i)\\s+-\\s+.*(remaster|version|edit|live|mono|stereo|mix).*$", "");
      return var1.trim().isEmpty() ? var0 : var1.trim();
   }

   private static Lyrics.Http get(String var0) {
      Lyrics.Http var1 = new Lyrics.Http();

      try {
         HttpURLConnection var2 = (HttpURLConnection)URI.create(var0).toURL().openConnection();

         try {
            var2.setConnectTimeout(6000);
            var2.setReadTimeout(10000);
            var2.setRequestProperty("User-Agent", "SpotifyHUD-Minecraft/1.1 (Fabric mod)");
            var2.setRequestProperty("Accept", "application/json");
            var1.code = var2.getResponseCode();
            InputStream var3 = var1.code >= 400 ? var2.getErrorStream() : var2.getInputStream();
            if (var3 != null) {
               try (InputStream var4 = var3) {
                  ByteArrayOutputStream var5 = new ByteArrayOutputStream();
                  byte[] var6 = new byte[8192];

                  int var7;
                  while ((var7 = var4.read(var6)) > 0) {
                     var5.write(var6, 0, var7);
                  }

                  var1.body = var5.toString(StandardCharsets.UTF_8);
               }
            }
         } finally {
            var2.disconnect();
         }
      } catch (Exception var16) {
         var1.code = -1;
      }

      return var1;
   }

   private static String enc(String var0) {
      return URLEncoder.encode(var0, StandardCharsets.UTF_8);
   }

   private static final class Http {
      int code;
      String body = "";
   }

   public static final class Line {
      public final long timeMs;
      public final String text;
      public String[] words = new String[0];
      public long[] wordTimes = new long[0];
      public boolean exactWords;

      Line(long var1, String var3) {
         this.timeMs = var1;
         this.text = var3;
      }

      public int wordAt(long var1) {
         int var3 = -1;
         int var4 = 0;

         while (var4 < this.wordTimes.length && this.wordTimes[var4] <= var1) {
            var3 = var4++;
         }

         return var3;
      }
   }

   public static final class Result {
      public final String key;
      public final Lyrics.Status status;
      public final List<Lyrics.Line> lines;

      Result(String var1, Lyrics.Status var2, List<Lyrics.Line> var3) {
         this.key = var1;
         this.status = var2;
         this.lines = var3;
      }
   }

   public static enum Status {
      NONE,
      LOADING,
      SYNCED,
      PLAIN,
      NOT_FOUND,
      INSTRUMENTAL,
      ERROR;
   }
}
