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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Synced lyrics from LRCLIB (free, no key). Fetched automatically whenever the song changes. */
public final class Lyrics {
    public enum Status { NONE, LOADING, SYNCED, PLAIN, NOT_FOUND, INSTRUMENTAL, ERROR }

    public static final class Line {
        public final long timeMs;
        public final String text;
        /** Words of the line and the time each word starts (ms). Filled for synced lyrics. */
        public String[] words = new String[0];
        public long[] wordTimes = new long[0];
        /** True if the word times came from the lyrics file (enhanced LRC), false if estimated. */
        public boolean exactWords;
        Line(long t, String s) { timeMs = t; text = s; }

        /** Index of the word being sung at time t (-1 before the first word). */
        public int wordAt(long t) {
            int ans = -1;
            for (int i = 0; i < wordTimes.length; i++) if (wordTimes[i] <= t) ans = i; else break;
            return ans;
        }
    }

    private static final Pattern WORD_TAG = Pattern.compile("<(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?>");

    public static final class Result {
        public final String key;
        public final Status status;
        public final List<Line> lines;
        Result(String key, Status status, List<Line> lines) { this.key = key; this.status = status; this.lines = lines; }
    }

    private static final String UA = "SpotifyHUD-Minecraft/1.1 (Fabric mod)";
    private static final Pattern TAG = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
    private static final Pattern OFFSET = Pattern.compile("\\[offset:\\s*([+-]?\\d+)\\s*]", Pattern.CASE_INSENSITIVE);

    private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SpotifyHUD-Lyrics");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, Result> cache = Collections.synchronizedMap(new LinkedHashMap<String, Result>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Result> e) { return size() > 40; }
    });
    private volatile Result current = new Result("", Status.NONE, List.of());
    private volatile String requested = "";
    private volatile long errorAt = 0;

    public Result current() { return current; }

    /** Called every frame/tick with the current song; starts a fetch when the song changes. */
    public void update(PlayerState st) {
        if (st == null || !st.hasTrack || st.title == null || st.title.isBlank()) {
            if (!requested.isEmpty()) { requested = ""; current = new Result("", Status.NONE, List.of()); }
            return;
        }
        String key = key(st.title, st.artist);
        if (key.equals(requested)) {
            if (current.status == Status.ERROR && System.currentTimeMillis() - errorAt > 20000) requested = "";
            else return;
        }
        requested = key;
        Result cached = cache.get(key);
        if (cached != null && cached.status != Status.ERROR) { current = cached; return; }
        current = new Result(key, Status.LOADING, List.of());
        final String title = st.title, artist = st.artist;
        final long dur = st.durationMs;
        exec.execute(() -> {
            if (!key.equals(requested)) return;
            Result r = fetch(key, title, artist, dur);
            if (r.status != Status.ERROR) cache.put(key, r); else errorAt = System.currentTimeMillis();
            if (key.equals(requested)) current = r;
        });
    }

    public void retry() {
        String k = requested;
        requested = "";
        cache.remove(k);
    }

    static String key(String title, String artist) {
        return (title + "\u0001" + artist).toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------ fetching
    private Result fetch(String key, String title, String artist, long durMs) {
        String mainArtist = firstArtist(artist);
        String cleanTitle = cleanTitle(title);
        try {
            // 1) exact match (full artist string first, then the main artist)
            Http h = null;
            for (String a : mainArtist.equals(artist) ? new String[]{artist} : new String[]{artist, mainArtist}) {
                StringBuilder u = new StringBuilder("https://lrclib.net/api/get?track_name=").append(enc(cleanTitle))
                        .append("&artist_name=").append(enc(a));
                if (durMs > 0) u.append("&duration=").append(Math.round(durMs / 1000.0));
                h = get(u.toString());
                if (h.code == 200) {
                    Result r = fromRecord(key, Json.parse(h.body));
                    if (r != null && (r.status == Status.SYNCED || r.status == Status.INSTRUMENTAL)) return r;
                }
            }
            // 2) search, choose best candidate (synced + closest duration)
            h = get("https://lrclib.net/api/search?track_name=" + enc(cleanTitle) + "&artist_name=" + enc(mainArtist));
            Object best = h.code == 200 ? pick(Json.parse(h.body), durMs) : null;
            if (best == null) {
                h = get("https://lrclib.net/api/search?q=" + enc(cleanTitle + " " + mainArtist));
                best = h.code == 200 ? pick(Json.parse(h.body), durMs) : null;
            }
            if (best != null) {
                Result r = fromRecord(key, best);
                if (r != null) return r;
            }
            if (h.code >= 500 || h.code == 429 || h.code == -1) return new Result(key, Status.ERROR, List.of());
            return new Result(key, Status.NOT_FOUND, List.of());
        } catch (Exception e) {
            SpotifyHudMod.LOG("Lyrics-Fehler: " + e);
            return new Result(key, Status.ERROR, List.of());
        }
    }

    private static Object pick(Object arr, long durMs) {
        if (!(arr instanceof List)) return null;
        Object best = null;
        double bestScore = Double.MAX_VALUE;
        for (Object o : (List<?>) arr) {
            String synced = Json.str(o, "syncedLyrics", null);
            boolean instrumental = Json.bool(o, "instrumental", false);
            String plain = Json.str(o, "plainLyrics", null);
            if (synced == null && !instrumental && plain == null) continue;
            double d = Json.dbl(o, "duration", 0) * 1000.0;
            double diff = durMs > 0 && d > 0 ? Math.abs(d - durMs) : 5000;
            if (durMs > 0 && diff > 15000) continue;          // clearly a different version
            double score = diff + (synced != null ? 0 : 60000) + (instrumental ? 30000 : 0);
            if (score < bestScore) { bestScore = score; best = o; }
        }
        return best;
    }

    private static Result fromRecord(String key, Object o) {
        if (o == null) return null;
        if (Json.bool(o, "instrumental", false)) return new Result(key, Status.INSTRUMENTAL, List.of());
        String synced = Json.str(o, "syncedLyrics", null);
        if (synced != null && !synced.isBlank()) {
            List<Line> lines = parseLrc(synced);
            if (!lines.isEmpty()) return new Result(key, Status.SYNCED, lines);
        }
        String plain = Json.str(o, "plainLyrics", null);
        if (plain != null && !plain.isBlank()) {
            List<Line> lines = new ArrayList<>();
            for (String l : plain.split("\\r?\\n")) if (!l.isBlank()) lines.add(new Line(-1, l.trim()));
            return new Result(key, Status.PLAIN, lines);
        }
        return null;
    }

    /** Parses LRC text: supports multiple time tags per line and the [offset:] tag. */
    static List<Line> parseLrc(String lrc) {
        long offset = 0;
        Matcher om = OFFSET.matcher(lrc);
        if (om.find()) { try { offset = Long.parseLong(om.group(1)); } catch (NumberFormatException ignored) { } }
        List<Line> out = new ArrayList<>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = TAG.matcher(raw);
            List<Long> times = new ArrayList<>();
            int end = 0;
            while (m.find() && m.start() == end) {
                long min = Long.parseLong(m.group(1));
                long sec = Long.parseLong(m.group(2));
                long frac = 0;
                if (m.group(3) != null) {
                    String f = m.group(3);
                    frac = f.length() == 1 ? Long.parseLong(f) * 100 : f.length() == 2 ? Long.parseLong(f) * 10 : Long.parseLong(f);
                }
                times.add(min * 60000 + sec * 1000 + frac);
                end = m.end();
            }
            if (times.isEmpty()) continue;
            String rest = raw.substring(end);
            // enhanced LRC: "<mm:ss.xx>word <mm:ss.xx>word"
            List<String> ew = new ArrayList<>();
            List<Long> et = new ArrayList<>();
            Matcher wm = WORD_TAG.matcher(rest);
            int last = -1; long lastT = 0;
            while (wm.find()) {
                if (last >= 0) addWords(ew, et, rest.substring(last, wm.start()), lastT);
                lastT = tagMs(wm.group(1), wm.group(2), wm.group(3)) - offset;
                last = wm.end();
            }
            if (last >= 0) addWords(ew, et, rest.substring(last), lastT);
            String text = WORD_TAG.matcher(rest).replaceAll("").replaceAll("\\s+", " ").trim();
            for (long t : times) {
                Line l = new Line(Math.max(0, t - offset), text);
                if (!ew.isEmpty() && times.size() == 1) {
                    l.words = ew.toArray(new String[0]);
                    l.wordTimes = new long[et.size()];
                    for (int i = 0; i < et.size(); i++) l.wordTimes[i] = Math.max(0, et.get(i));
                    l.exactWords = true;
                }
                out.add(l);
            }
        }
        out.sort((a, b) -> Long.compare(a.timeMs, b.timeMs));
        estimateWords(out);
        return out;
    }

    private static long tagMs(String m, String s, String f) {
        long frac = 0;
        if (f != null) frac = f.length() == 1 ? Long.parseLong(f) * 100 : f.length() == 2 ? Long.parseLong(f) * 10 : Long.parseLong(f);
        return Long.parseLong(m) * 60000 + Long.parseLong(s) * 1000 + frac;
    }

    private static void addWords(List<String> words, List<Long> times, String chunk, long t) {
        boolean first = true;
        for (String w : chunk.trim().split("\\s+")) {
            if (w.isEmpty()) continue;
            words.add(w);
            times.add(first ? t : t + 1);   // several words in one chunk start together
            first = false;
        }
    }

    /**
     * Estimates when each word is sung: the line's singing time (until the next line, capped so long
     * instrumental gaps don't stretch it) is split proportionally to the word lengths.
     */
    static void estimateWords(List<Line> lines) {
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            if (l.exactWords) continue;
            String[] words = l.text.isEmpty() ? new String[0] : l.text.split("\\s+");
            l.words = words;
            l.wordTimes = new long[words.length];
            if (words.length == 0) continue;
            long next = i + 1 < lines.size() ? lines.get(i + 1).timeMs : l.timeMs + 4000;
            long gap = Math.max(300, next - l.timeMs);
            long singing = Math.min((long) (gap * 0.92), 450L * words.length + 900);
            singing = Math.max(Math.min(gap, 250L * words.length), singing);
            double total = 0;
            double[] weight = new double[words.length];
            for (int k = 0; k < words.length; k++) { weight[k] = 1.5 + words[k].length(); total += weight[k]; }
            double acc = 0;
            for (int k = 0; k < words.length; k++) {
                l.wordTimes[k] = l.timeMs + Math.round(singing * acc / total);
                acc += weight[k];
            }
        }
    }

    /** Index of the line active at time t (or -1 before the first line). */
    public static int indexAt(List<Line> lines, long t) {
        int lo = 0, hi = lines.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (lines.get(mid).timeMs <= t) { ans = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return ans;
    }

    static String firstArtist(String artist) {
        if (artist == null) return "";
        String a = artist;
        for (String sep : new String[]{", ", " & ", " feat. ", " feat ", " ft. ", " x ", ";", " / "}) {
            int i = a.toLowerCase(Locale.ROOT).indexOf(sep);
            if (i > 0) a = a.substring(0, i);
        }
        return a.trim();
    }

    static String cleanTitle(String title) {
        String t = title;
        // "Song - Remastered 2011", "Song (feat. X)", "Song - Radio Edit"
        t = t.replaceAll("(?i)\\s*[(\\[](feat\\.?|ft\\.?|with) [^)\\]]*[)\\]]", "");
        t = t.replaceAll("(?i)\\s+-\\s+.*(remaster|version|edit|live|mono|stereo|mix).*$", "");
        return t.trim().isEmpty() ? title : t.trim();
    }

    // ------------------------------------------------------------------ http
    private static final class Http { int code; String body = ""; }

    private static Http get(String url) {
        Http h = new Http();
        try {
            HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
            try {
                c.setConnectTimeout(6000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", UA);
                c.setRequestProperty("Accept", "application/json");
                h.code = c.getResponseCode();
                InputStream in = h.code >= 400 ? c.getErrorStream() : c.getInputStream();
                if (in != null) {
                    try (InputStream i = in) {
                        ByteArrayOutputStream o = new ByteArrayOutputStream();
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = i.read(buf)) > 0) o.write(buf, 0, n);
                        h.body = o.toString(StandardCharsets.UTF_8);
                    }
                }
            } finally {
                c.disconnect();
            }
        } catch (Exception e) {
            h.code = -1;
        }
        return h;
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
