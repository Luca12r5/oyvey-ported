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
   private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "SpotifyHUD-Lyrics");
      t.setDaemon(true);
      return t;
   });
   private final Map<String, Lyrics.Result> cache = Collections.synchronizedMap(new LinkedHashMap<String, Lyrics.Result>(32, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Lyrics.Result> e) {
         return this.size() > 40;
      }
   });
   private volatile Lyrics.Result current = new Lyrics.Result("", Lyrics.Status.NONE, List.of());
   private volatile String requested = "";
   private volatile long errorAt = 0L;

   public Lyrics.Result current() {
      return this.current;
   }

   public void update(PlayerState st) {
      if (st != null && st.hasTrack && st.title != null && !st.title.isBlank()) {
         String key = key(st.title, st.artist);
         if (key.equals(this.requested)) {
            if (this.current.status != Lyrics.Status.ERROR || System.currentTimeMillis() - this.errorAt <= 20000L) {
               return;
            }

            this.requested = "";
         }

         this.requested = key;
         Lyrics.Result cached = this.cache.get(key);
         if (cached != null && cached.status != Lyrics.Status.ERROR) {
            this.current = cached;
         } else {
            this.current = new Lyrics.Result(key, Lyrics.Status.LOADING, List.of());
            String title = st.title;
            String artist = st.artist;
            long dur = st.durationMs;
            this.exec.execute(() -> {
               if (key.equals(this.requested)) {
                  Lyrics.Result r = this.fetch(key, title, artist, dur);
                  if (r.status != Lyrics.Status.ERROR) {
                     this.cache.put(key, r);
                  } else {
                     this.errorAt = System.currentTimeMillis();
                  }

                  if (key.equals(this.requested)) {
                     this.current = r;
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
      String k = this.requested;
      this.requested = "";
      this.cache.remove(k);
   }

   static String key(String title, String artist) {
      return (title + artist).toLowerCase(Locale.ROOT);
   }

   private Lyrics.Result fetch(String key, String title, String artist, long durMs) {
      String mainArtist = firstArtist(artist);
      String cleanTitle = cleanTitle(title);

      try {
         Lyrics.Http h = null;

         for (String a : mainArtist.equals(artist) ? new String[]{artist} : new String[]{artist, mainArtist}) {
            StringBuilder u = new StringBuilder("https://lrclib.net/api/get?track_name=").append(enc(cleanTitle)).append("&artist_name=").append(enc(a));
            if (durMs > 0L) {
               u.append("&duration=").append(Math.round(durMs / 1000.0));
            }

            h = get(u.toString());
            if (h.code == 200) {
               Lyrics.Result r = fromRecord(key, Json.parse(h.body));
               if (r != null && (r.status == Lyrics.Status.SYNCED || r.status == Lyrics.Status.INSTRUMENTAL)) {
                  return r;
               }
            }
         }

         h = get("https://lrclib.net/api/search?track_name=" + enc(cleanTitle) + "&artist_name=" + enc(mainArtist));
         Object best = h.code == 200 ? pick(Json.parse(h.body), durMs) : null;
         if (best == null) {
            h = get("https://lrclib.net/api/search?q=" + enc(cleanTitle + " " + mainArtist));
            best = h.code == 200 ? pick(Json.parse(h.body), durMs) : null;
         }

         if (best != null) {
            Lyrics.Result r = fromRecord(key, best);
            if (r != null) {
               return r;
            }
         }

         return h.code < 500 && h.code != 429 && h.code != -1
            ? new Lyrics.Result(key, Lyrics.Status.NOT_FOUND, List.of())
            : new Lyrics.Result(key, Lyrics.Status.ERROR, List.of());
      } catch (Exception var15) {
         SpotifyHudMod.LOG("Lyrics-Fehler: " + var15);
         return new Lyrics.Result(key, Lyrics.Status.ERROR, List.of());
      }
   }

   private static Object pick(Object arr, long durMs) {
      if (!(arr instanceof List)) {
         return null;
      } else {
         Object best = null;
         double bestScore = Double.MAX_VALUE;

         for (Object o : (List)arr) {
            String synced = Json.str(o, "syncedLyrics", null);
            boolean instrumental = Json.bool(o, "instrumental", false);
            String plain = Json.str(o, "plainLyrics", null);
            if (synced != null || instrumental || plain != null) {
               double d = Json.dbl(o, "duration", 0.0) * 1000.0;
               double diff = durMs > 0L && d > 0.0 ? Math.abs(d - durMs) : 5000.0;
               if (durMs <= 0L || !(diff > 15000.0)) {
                  double score = diff + (synced != null ? 0 : '\uea60') + (instrumental ? 30000 : 0);
                  if (score < bestScore) {
                     bestScore = score;
                     best = o;
                  }
               }
            }
         }

         return best;
      }
   }

   private static Lyrics.Result fromRecord(String key, Object o) {
      if (o == null) {
         return null;
      } else if (Json.bool(o, "instrumental", false)) {
         return new Lyrics.Result(key, Lyrics.Status.INSTRUMENTAL, List.of());
      } else {
         String synced = Json.str(o, "syncedLyrics", null);
         if (synced != null && !synced.isBlank()) {
            List<Lyrics.Line> lines = parseLrc(synced);
            if (!lines.isEmpty()) {
               return new Lyrics.Result(key, Lyrics.Status.SYNCED, lines);
            }
         }

         String plain = Json.str(o, "plainLyrics", null);
         if (plain != null && !plain.isBlank()) {
            List<Lyrics.Line> lines = new ArrayList<>();

            for (String l : plain.split("\\r?\\n")) {
               if (!l.isBlank()) {
                  lines.add(new Lyrics.Line(-1L, l.trim()));
               }
            }

            return new Lyrics.Result(key, Lyrics.Status.PLAIN, lines);
         } else {
            return null;
         }
      }
   }

   static List<Lyrics.Line> parseLrc(String lrc) {
      long offset = 0L;
      Matcher om = OFFSET.matcher(lrc);
      if (om.find()) {
         try {
            offset = Long.parseLong(om.group(1));
         } catch (NumberFormatException var25) {
         }
      }

      List<Lyrics.Line> out = new ArrayList<>();

      for (String raw : lrc.split("\\r?\\n")) {
         Matcher m = TAG.matcher(raw);
         List<Long> times = new ArrayList<>();

         int end;
         for (end = 0; m.find() && m.start() == end; end = m.end()) {
            long min = Long.parseLong(m.group(1));
            long sec = Long.parseLong(m.group(2));
            long frac = 0L;
            if (m.group(3) != null) {
               String f = m.group(3);
               frac = f.length() == 1 ? Long.parseLong(f) * 100L : (f.length() == 2 ? Long.parseLong(f) * 10L : Long.parseLong(f));
            }

            times.add(min * 60000L + sec * 1000L + frac);
         }

         if (!times.isEmpty()) {
            String rest = raw.substring(end);
            List<String> ew = new ArrayList<>();
            List<Long> et = new ArrayList<>();
            Matcher wm = WORD_TAG.matcher(rest);
            int last = -1;

            long lastT;
            for (lastT = 0L; wm.find(); last = wm.end()) {
               if (last >= 0) {
                  addWords(ew, et, rest.substring(last, wm.start()), lastT);
               }

               lastT = tagMs(wm.group(1), wm.group(2), wm.group(3)) - offset;
            }

            if (last >= 0) {
               addWords(ew, et, rest.substring(last), lastT);
            }

            String text = WORD_TAG.matcher(rest).replaceAll("").replaceAll("\\s+", " ").trim();

            for (long t : times) {
               Lyrics.Line l = new Lyrics.Line(Math.max(0L, t - offset), text);
               if (!ew.isEmpty() && times.size() == 1) {
                  l.words = ew.toArray(new String[0]);
                  l.wordTimes = new long[et.size()];

                  for (int i = 0; i < et.size(); i++) {
                     l.wordTimes[i] = Math.max(0L, et.get(i));
                  }

                  l.exactWords = true;
               }

               out.add(l);
            }
         }
      }

      out.sort((a, b) -> Long.compare(a.timeMs, b.timeMs));
      estimateWords(out);
      return out;
   }

   private static long tagMs(String m, String s, String f) {
      long frac = 0L;
      if (f != null) {
         frac = f.length() == 1 ? Long.parseLong(f) * 100L : (f.length() == 2 ? Long.parseLong(f) * 10L : Long.parseLong(f));
      }

      return Long.parseLong(m) * 60000L + Long.parseLong(s) * 1000L + frac;
   }

   private static void addWords(List<String> words, List<Long> times, String chunk, long t) {
      boolean first = true;

      for (String w : chunk.trim().split("\\s+")) {
         if (!w.isEmpty()) {
            words.add(w);
            times.add(first ? t : t + 1L);
            first = false;
         }
      }
   }

   static void estimateWords(List<Lyrics.Line> lines) {
      for (int i = 0; i < lines.size(); i++) {
         Lyrics.Line l = lines.get(i);
         if (!l.exactWords) {
            String[] words = l.text.isEmpty() ? new String[0] : l.text.split("\\s+");
            l.words = words;
            l.wordTimes = new long[words.length];
            if (words.length != 0) {
               long next = i + 1 < lines.size() ? lines.get(i + 1).timeMs : l.timeMs + 4000L;
               long gap = Math.max(300L, next - l.timeMs);
               long singing = Math.min((long)(gap * 0.92), 450L * words.length + 900L);
               singing = Math.max(Math.min(gap, 250L * words.length), singing);
               double total = 0.0;
               double[] weight = new double[words.length];

               for (int k = 0; k < words.length; k++) {
                  weight[k] = 1.5 + words[k].length();
                  total += weight[k];
               }

               double acc = 0.0;

               for (int k = 0; k < words.length; k++) {
                  l.wordTimes[k] = l.timeMs + Math.round(singing * acc / total);
                  acc += weight[k];
               }
            }
         }
      }
   }

   public static int indexAt(List<Lyrics.Line> lines, long t) {
      int lo = 0;
      int hi = lines.size() - 1;
      int ans = -1;

      while (lo <= hi) {
         int mid = lo + hi >>> 1;
         if (lines.get(mid).timeMs <= t) {
            ans = mid;
            lo = mid + 1;
         } else {
            hi = mid - 1;
         }
      }

      return ans;
   }

   static String firstArtist(String artist) {
      if (artist == null) {
         return "";
      } else {
         String a = artist;

         for (String sep : new String[]{", ", " & ", " feat. ", " feat ", " ft. ", " x ", ";", " / "}) {
            int i = a.toLowerCase(Locale.ROOT).indexOf(sep);
            if (i > 0) {
               a = a.substring(0, i);
            }
         }

         return a.trim();
      }
   }

   static String cleanTitle(String title) {
      String t = title.replaceAll("(?i)\\s*[(\\[](feat\\.?|ft\\.?|with) [^)\\]]*[)\\]]", "");
      t = t.replaceAll("(?i)\\s+-\\s+.*(remaster|version|edit|live|mono|stereo|mix).*$", "");
      return t.trim().isEmpty() ? title : t.trim();
   }

   private static Lyrics.Http get(String url) {
      Lyrics.Http h = new Lyrics.Http();

      try {
         HttpURLConnection c = (HttpURLConnection)URI.create(url).toURL().openConnection();

         try {
            c.setConnectTimeout(6000);
            c.setReadTimeout(10000);
            c.setRequestProperty("User-Agent", "SpotifyHUD-Minecraft/1.1 (Fabric mod)");
            c.setRequestProperty("Accept", "application/json");
            h.code = c.getResponseCode();
            InputStream in = h.code >= 400 ? c.getErrorStream() : c.getInputStream();
            if (in != null) {
               try (InputStream i = in) {
                  ByteArrayOutputStream o = new ByteArrayOutputStream();
                  byte[] buf = new byte[8192];

                  int n;
                  while ((n = i.read(buf)) > 0) {
                     o.write(buf, 0, n);
                  }

                  h.body = o.toString(StandardCharsets.UTF_8);
               }
            }
         } finally {
            c.disconnect();
         }
      } catch (Exception var16) {
         h.code = -1;
      }

      return h;
   }

   private static String enc(String s) {
      return URLEncoder.encode(s, StandardCharsets.UTF_8);
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

      Line(long t, String s) {
         this.timeMs = t;
         this.text = s;
      }

      public int wordAt(long t) {
         int ans = -1;
         int i = 0;

         while (i < this.wordTimes.length && this.wordTimes[i] <= t) {
            ans = i++;
         }

         return ans;
      }
   }

   public static final class Result {
      public final String key;
      public final Lyrics.Status status;
      public final List<Lyrics.Line> lines;

      Result(String key, Lyrics.Status status, List<Lyrics.Line> lines) {
         this.key = key;
         this.status = status;
         this.lines = lines;
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
