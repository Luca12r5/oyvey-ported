package dev.lego.net;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.spotifyhud.Json;
import net.minecraft.class_746;

/**
 * Lego-Sync: every Lego Client user sees the other Lego users' name tag, LEGO symbol, cosmetics, character model and
 * emotes - no own server, no room code. Uses the free public relay ntfy.sh on one shared channel (anyone who knows the
 * channel can read it). Sent: UUID, name, name-tag style, equipped cosmetic ids, character model, current emote.
 * Published only when something changes + a heartbeat every 8 min. Receiving is one live stream (instant emotes / duel moves). Data from others is untrusted: only known fields
 * with safe sizes/values are kept. Always active (cannot be toggled off).
 */
public final class LegoSync extends Module {
    public static LegoSync instance;
    private static final String BASE = "https://ntfy.sh/", SYNC_TOPIC = "legoclient-namesync-v1-q7x4m2", DUEL_TOPIC = "legoclient-duel-v1-q7x4m2";
    private static final String RELAY = BASE + SYNC_TOPIC;
    public static final String DUEL_RELAY = BASE + DUEL_TOPIC;
    private static final long HEARTBEAT_MS = 8 * 60_000, MAX_AGE_S = 10 * 60, MIN_POST_GAP_MS = 2_500, STALL_MS = 150_000;

    final Setting.Bool showOthers = add(new Setting.Bool("Andere Lego-Spieler anzeigen", true));

    private record Remote(Map<String, Object> data, long seen) { }

    private static final Map<UUID, Remote> REMOTE = new ConcurrentHashMap<>();
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private volatile boolean posting, greeted;
    private long nextBeat, lastPost;
    private String lastSent = "", lastEmote = "";
    private long emoteStartS;
    private static volatile int fails;
    private static volatile long lastError, lastLineAt, connectedAt;
    private static volatile String lastMsgId;
    private static volatile Thread streamThread;
    private static volatile java.util.stream.Stream<String> openStream;

    public LegoSync() {
        super("legosync", "Lego-Sync", "Alle Lego-Client-Spieler sehen gegenseitig Namensschild, Kosmetik, Character und Emotes (online, ohne eigenen Server)", Category.UTILITY);
        icon("users");
        fresh();
        enabled = true;
        instance = this;
    }

    public static void register() { Modules.register(new LegoSync()); }

    public static boolean active() { return instance != null && instance.showOthers.get(); }

    private static Remote get(UUID id) {
        if (!active()) return null;
        Remote r = REMOTE.get(id);
        return r == null || System.currentTimeMillis() / 1000 - r.seen() > MAX_AGE_S ? null : r;
    }

    /** Name-tag style of another Lego user (or null). */
    public static Map<String, Object> remote(UUID id) {
        Remote r = get(id);
        return r == null ? null : Json.obj(r.data(), "tag");
    }

    /** Equipped cosmetic ids of another Lego user (empty if unknown). */
    @SuppressWarnings("unchecked")
    public static List<String> cosmetics(UUID id) {
        Remote r = get(id);
        Object o = r == null ? null : r.data().get("cos");
        return o instanceof List ? (List<String>) o : List.of();
    }

    /** Character model index of another Lego user (0 = none). */
    public static int character(UUID id) { Remote r = get(id); return r == null ? 0 : (int) Json.num(r.data(), "char", 0); }

    /** Emote id another Lego user is playing right now + seconds since it started, or null. */
    public static String emote(UUID id) {
        Remote r = get(id);
        if (r == null) return null;
        String e = Json.str(r.data(), "emote", "");
        return e.isEmpty() ? null : e;
    }

    public static double emoteStart(UUID id) { Remote r = get(id); return r == null ? 0 : Json.dbl(r.data(), "emoteAt", 0); }

    /** Equipped vehicle id of another Lego user (or null if none). */
    public static String vehicle(UUID id) { Remote r = get(id); if (r == null) return null; String v = Json.str(r.data(), "veh", ""); return v.isEmpty() ? null : v; }

    /** Equipped pet id of another Lego user (or null if none). */
    public static String pet(UUID id) { Remote r = get(id); if (r == null) return null; String p = Json.str(r.data(), "pet", ""); return p.isEmpty() ? null : p; }

    /** Minecraft name another Lego user sent (or null). */
    public static String name(UUID id) { Remote r = get(id); return r == null ? null : Json.str(r.data(), "name", null); }

    /** All Lego users seen in the last 10 minutes (not you). */
    public static List<UUID> players() {
        List<UUID> l = new ArrayList<>();
        if (!active()) return l;
        long now = System.currentTimeMillis() / 1000;
        for (Map.Entry<UUID, Remote> e : REMOTE.entrySet()) if (now - e.getValue().seen() <= MAX_AGE_S) l.add(e.getKey());
        return l;
    }

    /** true while the live connection to the relay is open. */
    public static boolean connected() { return connectedAt > 0 && streamThread != null; }

    public static int online() {
        long now = System.currentTimeMillis() / 1000;
        int n = 0;
        for (Remote r : REMOTE.values()) if (now - r.seen() <= MAX_AGE_S) n++;
        return n;
    }

    @Override public boolean isToggleable() { return false; }
    @Override public void onDisable() { enabled = true; }

    /** Fire-and-forget POST to the relay (used by duels). */
    public static void HTTP_POST(String url, String body) {
        if (!active() || body.length() > 3000) return;
        HTTP.sendAsync(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.discarding());
    }

    private static void closeStream() {
        java.util.stream.Stream<String> s = openStream;
        if (s != null) try { s.close(); } catch (Throwable ignored) { }
    }

    private static int myCharacter() {
        try { return Class.forName("dev.creamykes.CreamyConfig").getField("character").getInt(null); } catch (Throwable t) { return 0; }
    }

    private static Map<String, Object> myState(class_746 me) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", me.method_5667().toString());
        body.put("name", Mc.name(me));
        body.put("tag", dev.lego.visual.NameFx.tag != null && dev.lego.visual.NameFx.tag.enabled ? dev.lego.visual.NameFx.tag.syncState() : new LinkedHashMap<>());
        List<Object> cos = new ArrayList<>();
        for (dev.lego.cosmetic.Cos.Slot s : dev.lego.cosmetic.Cos.Slot.values()) {
            dev.lego.cosmetic.Cos.Item it = dev.lego.cosmetic.Cos.equipped(s);
            if (it != null) cos.add(it.id);
        }
        body.put("cos", cos);
        dev.lego.cosmetic.Cos.Item veh = dev.lego.cosmetic.Cos.equipped(dev.lego.cosmetic.Cos.Slot.VEHICLE);
        body.put("veh", veh != null ? veh.id : "");
        dev.lego.cosmetic.Cos.Item pet = dev.lego.cosmetic.Cos.equipped(dev.lego.cosmetic.Cos.Slot.PET);
        body.put("pet", pet != null ? pet.id : "");
        body.put("char", (long) myCharacter());
        dev.lego.emote.EmoteLib.Emote e = dev.lego.emote.Emotes.current();
        body.put("emote", e == null ? "" : e.id);
        return body;
    }

    @Override public void tick() {
        class_746 me = Mc.player();
        if (me == null) return;
        long now = System.currentTimeMillis();
        Map<String, Object> state = myState(me);
        String json = Json.write(state);
        String emote = Json.str(state, "emote", "");
        if (!emote.equals(lastEmote)) { lastEmote = emote; emoteStartS = now / 1000; }
        // the start time of an emote is not part of the comparison
        boolean changed = !json.equals(lastSent);
        if (!posting && now - lastPost >= MIN_POST_GAP_MS && (changed || now >= nextBeat)) {
            posting = true;
            lastPost = now;
            if (!emote.isEmpty()) state.put("emoteAt", (double) emoteStartS);
            final String send = Json.write(state), cmp = json;
            HTTP.sendAsync(HttpRequest.newBuilder(URI.create(RELAY)).timeout(Duration.ofSeconds(8)).POST(HttpRequest.BodyPublishers.ofString(send)).build(), HttpResponse.BodyHandlers.discarding())
                    .whenComplete((res, err) -> {
                        posting = false;
                        if (err == null && res.statusCode() == 200) { lastSent = cmp; nextBeat = System.currentTimeMillis() + HEARTBEAT_MS; }
                        else { nextBeat = System.currentTimeMillis() + 60_000; fail(); }
                    });
        }
        // live stream: (re)start it, restart it when it stalls (the relay sends a keepalive every ~45 s)
        Thread st = streamThread;
        if (st == null || !st.isAlive()) {
            Thread th = new Thread(LegoSync::streamLoop, "Lego-Sync");
            th.setDaemon(true);
            streamThread = th;
            th.start();
        } else if (lastLineAt > 0 && now - lastLineAt > STALL_MS) { lastLineAt = now; closeStream(); }
        if (!greeted && connectedAt > 0 && now / 1000 - connectedAt >= 3) {
            greeted = true;
            Toasts.show("legosync", "Lego-Sync", "Verbunden · " + online() + " andere Lego-Spieler online", 0xFF1ED760, 3000);
        }
    }

    private static void streamLoop() {
        long backoff = 2_000;
        try {
            while (active()) {
                String since = lastMsgId != null ? lastMsgId : "10m";
                try {
                    HttpResponse<java.util.stream.Stream<String>> res = HTTP.send(HttpRequest.newBuilder(URI.create(BASE + SYNC_TOPIC + "," + DUEL_TOPIC + "/json?since=" + since))
                            .timeout(Duration.ofSeconds(15)).GET().build(), HttpResponse.BodyHandlers.ofLines());
                    try (java.util.stream.Stream<String> lines = res.body()) {
                        if (res.statusCode() != 200) throw new java.io.IOException("HTTP " + res.statusCode());
                        openStream = lines;
                        connectedAt = System.currentTimeMillis() / 1000;
                        lastLineAt = System.currentTimeMillis();
                        fails = 0;
                        backoff = 2_000;
                        java.util.Iterator<String> it = lines.iterator();
                        while (it.hasNext() && active()) {
                            String line = it.next();
                            lastLineAt = System.currentTimeMillis();
                            try { handle(line); } catch (Throwable ignored) { }
                        }
                    }
                } catch (Throwable t) { failStatic(); }
                openStream = null;
                connectedAt = 0;
                if (!active()) break;
                try { Thread.sleep(backoff); } catch (InterruptedException e) { break; }
                backoff = Math.min(backoff * 2, 120_000);
            }
        } finally { streamThread = null; connectedAt = 0; }
    }

    private static void handle(String line) {
        if (line.isBlank() || line.length() > 8000) return;
        Object ev = Json.parse(line);
        if (!"message".equals(Json.str(ev, "event", ""))) return;
        String mid = Json.str(ev, "id", "");
        if (!mid.isEmpty() && mid.length() <= 32 && mid.matches("[A-Za-z0-9]+")) lastMsgId = mid;
        long time = (long) Json.num(ev, "time", 0);
        Object msg = Json.parse(Json.str(ev, "message", ""));
        class_746 me = Mc.player();
        String myId = me == null ? "" : me.method_5667().toString();
        String topic = Json.str(ev, "topic", "");
        if (topic.equals(DUEL_TOPIC)) { dev.lego.games.Duels.receive(msg, time, myId); return; }
        if (!topic.equals(SYNC_TOPIC)) return;
        String id = Json.str(msg, "id", "");
        if (id.equals(myId)) return;
        UUID uuid;
        try { uuid = UUID.fromString(id); } catch (IllegalArgumentException e) { return; }
        Map<String, Object> data = clean(msg);
        Remote old = REMOTE.get(uuid);
        if (old != null && old.seen() > time) return;
        // emotes play from the moment they arrive (no clock differences between PCs)
        String emote = Json.str(data, "emote", "");
        double raw = Json.dbl(data, "emoteAt", 0);
        long nowS = System.currentTimeMillis() / 1000;
        if (emote.isEmpty()) data.put("emoteAt", 0.0);
        else if (old != null && emote.equals(Json.str(old.data(), "emote", "")) && raw == Json.dbl(old.data(), "emoteRaw", -1)) data.put("emoteAt", Json.dbl(old.data(), "emoteAt", raw));
        else data.put("emoteAt", nowS - time < 30 ? (double) System.currentTimeMillis() / 1000.0 : raw);
        data.put("emoteRaw", raw);
        REMOTE.put(uuid, new Remote(data, time));
    }

    private static void failStatic() {
        fails++;
        long now = System.currentTimeMillis();
        if (fails == 4 && now - lastError > 300_000) {
            lastError = now;
            Toasts.show("legosync", "Lego-Sync", "Keine Verbindung zum Sync-Dienst – versuche es später erneut", 0xFFFF4D5E, 3500);
        }
    }

    private static boolean safeId(String s) { return s != null && s.length() <= 48 && s.matches("[a-z0-9_:\\-]+"); }

    /** Keeps only known fields with safe sizes and value ranges. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> clean(Object msg) {
        Map<String, Object> c = new LinkedHashMap<>();
        Map<String, Object> t = Json.obj(msg, "tag");
        if (t != null && !t.isEmpty()) {
            Map<String, Object> tag = new LinkedHashMap<>();
            String text = Json.str(t, "t", "");
            tag.put("t", text.length() > 24 ? text.substring(0, 24) : text);
            tag.put("s", (double) Math.max(0, Math.min(7, (int) Json.num(t, "s", 0))));
            tag.put("c1", (double) Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, (int) Json.num(t, "c1", 5))));
            tag.put("c2", (double) Math.max(0, Math.min(Setting.Color.PALETTE.length - 1, (int) Json.num(t, "c2", 7))));
            tag.put("b", Json.bool(t, "b", true));
            tag.put("m", Json.bool(t, "m", true));
            tag.put("sp", Math.max(0.3, Math.min(3, Json.dbl(t, "sp", 1))));
            tag.put("lego", Json.bool(t, "lego", true));
            c.put("tag", tag);
        }
        List<String> cos = new ArrayList<>();
        Object co = msg instanceof Map ? ((Map<String, Object>) msg).get("cos") : null;
        if (co instanceof List) for (Object o : (List<Object>) co) if (o instanceof String s && safeId(s) && cos.size() < 12) cos.add(s);
        c.put("cos", cos);
        String nm = Json.str(msg, "name", "");
        if (nm.matches("[A-Za-z0-9_]{1,16}")) c.put("name", nm);
        String v = Json.str(msg, "veh", "");
        c.put("veh", safeId(v) ? v : "");
        String pt = Json.str(msg, "pet", "");
        c.put("pet", safeId(pt) ? pt : "");
        c.put("char", (double) Math.max(0, Math.min(64, (int) Json.num(msg, "char", 0))));
        String e = Json.str(msg, "emote", "");
        c.put("emote", safeId(e) ? e : "");
        c.put("emoteAt", Json.dbl(msg, "emoteAt", 0));
        return c;
    }

    private void fail() { failStatic(); }
}