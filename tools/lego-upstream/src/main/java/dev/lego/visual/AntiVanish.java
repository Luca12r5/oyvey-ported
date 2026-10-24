package dev.lego.visual;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Style;

import net.minecraft.class_2561;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

/**
 * Anti Vanish – detects vanished staff members on servers.
 * Ported from glazed antivanish to LegoClient module system.
 */
public final class AntiVanish extends Module {

    final Setting.Text staffNames = add(new Setting.Text("Staff-Namen", "", 512, "Name1,Name2,..."));
    final Setting.Bool completionProbe = add(new Setting.Bool("Completion-Probe", true));
    final Setting.Bool chatNotify = add(new Setting.Bool("Chat-Benachrichtigung", true));
    final Setting.Bool showHud = add(new Setting.Bool("HUD anzeigen", true));
    final Setting.Bool starDetect = add(new Setting.Bool("Stern-Erkennung", true));
    final Setting.Num hudScale = add(new Setting.Num("HUD-Größe", 50, 150, 5, 100, "%"));
    final Setting.Bool testMode = add(new Setting.Bool("Test-Modus", false));

    private static final String PROBE_COMMAND = "minecraft:msg";
    private static final long SIGNAL_WINDOW_MS = 15_000L;
    private static final long DETECTION_TTL_MS = 20_000L;
    private static final long ANNOUNCE_COOLDOWN_MS = 1500L;
    private static final long RECENT_MESSAGE_TTL_MS = 8000L;
    private static final long TRUSTED_LISTED_MS = 1500L;
    private static final double SENSOR_RANGE = 64.0;
    private static final int MAX_OBS_PER_TICK = 512;

    private final ConcurrentLinkedQueue<Obs> observations = new ConcurrentLinkedQueue<>();
    private final Map<UUID, KnownPlayer> knownPlayers = new HashMap<>();
    private final Map<String, Detection> detections = new LinkedHashMap<>();
    private final Map<String, Long> signalCooldowns = new HashMap<>();
    private final Map<String, Long> announceCooldowns = new HashMap<>();
    private final Deque<Long> announceTimes = new ArrayDeque<>();
    private final Deque<Signal> signals = new ArrayDeque<>();
    private final Deque<PendingVanish> pendingVanishes = new ArrayDeque<>();
    private final Map<String, HiddenSpell> hiddenSpells = new LinkedHashMap<>();
    private final Deque<RecentMsg> recentMessages = new ArrayDeque<>();
    private final Map<UUID, Long> listedSinceMs = new HashMap<>();
    private final Set<Integer> completionIds = ConcurrentHashMap.newKeySet();
    private final Set<String> staffSet = new HashSet<>();
    private final Set<String> starred = new HashSet<>();

    private volatile List<String> pendingCompNames;
    private boolean serverSendsLeave;
    private int nextCompId = 30000;
    private int tickCnt;
    private long lastCriticalMs, criticalUntilMs;
    private String criticalName = "";

    public AntiVanish() {
        super("antivanish", "Anti Vanish", "Erkennt unsichtbare Staff-Mitglieder auf dem Server", Category.UTILITY);
        icon("eye");
        fresh();
    }

    @Override public void onEnable() {
        resetAll();
        parseStaffNames();
        if (Mc.inGame()) trackListed();
    }

    @Override public void onDisable() {
        resetAll();
    }

    @Override public void tick() {
        if (!Mc.inGame()) { resetAll(); return; }
        tickCnt++;
        drainObs();
        processPendingVanishes();
        processCompletionProbe();
        if (completionProbe.get() && tickCnt % 100 == 0) sendCompletionProbe();
        if (tickCnt % 10 == 0) { trackListed(); reviewSpells(); parseStaffNames(); }
        if (tickCnt % 5 == 0) scanInvisible();
        pruneState();
    }

    // ================================================================== packet hooks (called from mixin)

    public static void onPlayerRemove(List<UUID> ids) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        if (ids.size() >= 4) return;
        for (UUID id : ids) m.observations.offer(Obs.tabRemove(id));
    }

    public static void onPlayerUpdate(UUID id, boolean listed, String name, String gameMode) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        if (!listed && name == null && gameMode == null) return;
        if (name != null && listed) return;
        if (!listed && name == null) m.observations.offer(Obs.tabHide(id));
        if (gameMode != null) m.observations.offer(Obs.gamemode(id, gameMode));
    }

    public static void onSystemChat(String text) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        if (text != null && !text.isBlank()) m.observations.offer(Obs.systemChat(text));
    }

    public static boolean onCommandSuggestions(int id, List<String> names) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return false;
        if (!m.completionIds.remove(id)) return false;
        m.pendingCompNames = new ArrayList<>(names);
        return true;
    }

    public static void onEntityMetadata(int entityId) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        m.observations.offer(Obs.entityMeta(entityId));
    }

    public static void onSound(double x, double y, double z, String soundId) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        if (suspiciousSound(soundId)) m.observations.offer(Obs.position(ObsType.SOUND, x, y, z, soundId));
    }

    public static void onParticle(double x, double y, double z, String particleId) {
        AntiVanish m = instance(); if (m == null || !m.enabled) return;
        if (suspiciousParticle(particleId)) m.observations.offer(Obs.position(ObsType.PARTICLE, x, y, z, particleId));
    }

    // ================================================================== HUD rendering

    public void renderHud(int W, int H) {
        if (!showHud.get() || !Mc.inGame()) return;
        long now = System.currentTimeMillis();
        List<HudRow> rows = hudRows(now);
        if (rows.isEmpty()) return;

        float scale = hudScale.getF() / 100f;
        int S = Gx.S;
        int pad = Math.round(8 * S * scale);
        int headSz = Math.round(16 * S * scale);
        int rowH = Math.round(22 * S * scale);
        int panelW = Math.round(180 * S * scale);
        int headerH = Math.round(20 * S * scale);
        int panelH = headerH + rows.size() * rowH + pad;
        int px = W - panelW - pad, py = pad;

        Gx.rect(px, py, panelW, panelH, Math.round(8 * S * scale), Gx.withAlpha(Style.bg, 0.85f));
        Gx.outline(px, py, panelW, panelH, Math.round(8 * S * scale), Math.max(1, S), Style.stroke);

        float titleFs = 7.5f * S * scale;
        Gx.textMid("ANTI VANISH", px + pad, py + headerH / 2f, titleFs, Fonts.BOLD, Style.muted);
        String cnt = String.valueOf(rows.size());
        Gx.textRight(cnt, px + panelW - pad, py + headerH / 2f, titleFs, Fonts.BOLD, Style.muted);

        int ry = py + headerH;
        for (HudRow row : rows) {
            boolean alert = "CRITICAL".equals(row.tag);
            int tagCol = alert ? 0xFFFF453A : 0xFFFFAA00;
            float nameFs = 8.5f * S * scale;
            float tagFs = 6.5f * S * scale;

            Gx.Img face = FaceSkin.getFace(row.name);
            if (face != null) {
                Gx.image(face, px + pad, ry + (rowH - headSz) / 2, headSz, headSz, 0xFFFFFFFF);
            } else {
                Gx.rect(px + pad, ry + (rowH - headSz) / 2, headSz, headSz, Math.round(2 * S * scale), 0xFF2A2A2A);
            }

            int textX = px + pad + headSz + Math.round(6 * S * scale);
            Gx.textMid(row.name, textX, ry + rowH / 2f, nameFs, Fonts.BOLD, Style.text);

            String tag = row.tag;
            float tagW = Gx.width(tag, tagFs, Fonts.MEDIUM);
            Gx.textRight(tag, px + panelW - pad, ry + rowH / 2f, tagFs, Fonts.MEDIUM, tagCol);

            ry += rowH;
        }
    }

    // ================================================================== internals

    private static AntiVanish instance() {
        for (Module m : Modules.ALL) if (m instanceof AntiVanish) return (AntiVanish) m;
        return null;
    }

    private void parseStaffNames() {
        staffSet.clear();
        for (String n : staffNames.get().split("[,;\\s]+")) {
            String t = n.trim();
            if (!t.isEmpty()) staffSet.add(t.toLowerCase(Locale.ROOT));
        }
    }

    private boolean isStaff(String name) {
        if (name == null) return false;
        String key = name.toLowerCase(Locale.ROOT);
        return staffSet.contains(key) || (starDetect.get() && starred.contains(key));
    }

    private void trackListed() {
        class_634 nh = Mc.mc().method_1562();
        if (nh == null) return;
        long now = System.currentTimeMillis();
        for (class_640 info : nh.method_2880()) {
            if (info == null || info.method_2966() == null) continue;
            UUID uuid = info.method_2966().id();
            if (Mc.player() != null && Mc.player().method_5667().equals(uuid)) continue;
            listedSinceMs.putIfAbsent(uuid, now);
            String name = info.method_2966().name();
            if (name != null) {
                if (!knownPlayers.containsKey(uuid)) knownPlayers.put(uuid, new KnownPlayer(uuid, name));
                if (starDetect.get()) learnStar(name, info);
                endSpell(name, false, 1000L);
            }
        }
        if (listedSinceMs.size() > 1024) {
            Set<UUID> connected = new HashSet<>();
            for (class_640 info : nh.method_2880()) if (info != null && info.method_2966() != null) connected.add(info.method_2966().id());
            listedSinceMs.keySet().retainAll(connected);
            knownPlayers.keySet().retainAll(connected);
        }
    }

    @SuppressWarnings("deprecation")
    private void learnStar(String name, class_640 info) {
        try {
            net.minecraft.class_2561 display = info.method_2971();
            if (display != null && hasLeadingStar(display.getString())) starred.add(name.toLowerCase(Locale.ROOT));
        } catch (Throwable ignored) {}
    }

    private static boolean hasLeadingStar(String text) {
        if (text == null || text.isEmpty()) return false;
        String clean = stripCodes(text);
        for (int i = 0; i < clean.length(); ) {
            int cp = clean.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isWhitespace(cp) || cp == 0x200B || cp == 0xFE0F) continue;
            return "★☆✦✧✪✯✰⭐".indexOf(cp) >= 0;
        }
        return false;
    }

    private boolean trustedListed(UUID uuid) {
        Long since = listedSinceMs.get(uuid);
        return since != null && System.currentTimeMillis() - since >= TRUSTED_LISTED_MS;
    }

    private boolean credible(UUID uuid, String name) {
        return uuid != null && isUsername(name) && isStaff(name) && trustedListed(uuid);
    }

    private void drainObs() {
        Obs o; int i = 0;
        while (i < MAX_OBS_PER_TICK && (o = observations.poll()) != null) { processObs(o); i++; }
        while (observations.size() > 4096) observations.poll();
    }

    private void processObs(Obs o) {
        switch (o.type) {
            case TAB_REMOVE -> handleTabRemove(o.uuid);
            case TAB_HIDE -> handleTabHide(o.uuid);
            case PLAYER_LEFT -> { serverSendsLeave = true; confirmDeparture(o.detail); }
            case SYSTEM_CHAT -> cacheMessage(o.detail);
            case ENTITY_META -> inspectInvisible(o.entityId);
            case SOUND -> inspectSound(o);
            case PARTICLE -> inspectParticle(o);
            case GAMEMODE -> announceGamemode(o.uuid, o.detail);
        }
    }

    private void handleTabRemove(UUID uuid) {
        if (uuid == null || Mc.player().method_5667().equals(uuid)) return;
        String name = knownName(uuid);
        if (!credible(uuid, name) || !isPlausibleName(name)) return;
        pendingVanishes.removeIf(p -> p.uuid.equals(uuid));
        pendingVanishes.addLast(new PendingVanish(uuid, name, tickCnt + 20));
    }

    private void handleTabHide(UUID uuid) {
        if (uuid == null || Mc.player().method_5667().equals(uuid)) return;
        KnownPlayer kp = knownPlayers.get(uuid);
        String name = kp != null ? kp.name : knownName(uuid);
        if (!credible(uuid, name) || !isPlausibleName(name)) return;
        if (recentMsgMentions(name)) return;
        reportHidden(uuid, name, "Vanish: aus TAB versteckt", 100);
    }

    private String knownName(UUID uuid) {
        KnownPlayer kp = knownPlayers.get(uuid);
        if (kp != null && kp.name != null) return kp.name;
        class_634 nh = Mc.mc().method_1562();
        if (nh != null) {
            class_640 info = nh.method_2871(uuid);
            if (info != null && info.method_2966() != null) return info.method_2966().name();
        }
        return null;
    }

    private UUID knownUuid(String name) {
        for (KnownPlayer kp : knownPlayers.values()) if (kp.name.equalsIgnoreCase(name)) return kp.uuid;
        return null;
    }

    private void confirmDeparture(String text) {
        for (KnownPlayer kp : knownPlayers.values()) {
            if (containsName(text, kp.name)) {
                pendingVanishes.removeIf(p -> p.uuid.equals(kp.uuid));
                hiddenSpells.remove(kp.name.toLowerCase(Locale.ROOT));
                detections.remove(kp.uuid.toString());
            }
        }
    }

    private void processPendingVanishes() {
        class_634 nh = Mc.mc().method_1562();
        if (nh == null) return;
        while (!pendingVanishes.isEmpty() && pendingVanishes.peekFirst().dueTick <= tickCnt) {
            PendingVanish pv = pendingVanishes.removeFirst();
            if (nh.method_2871(pv.uuid) != null) continue;
            if (recentMsgMentions(pv.name)) { serverSendsLeave = true; continue; }
            class_1657 remaining = Mc.world().method_73285(pv.uuid);
            if (remaining != null && remaining.method_5765()) {
                reportHidden(pv.uuid, pv.name, "Vanish: Entity noch da", 100);
                continue;
            }
            int score = serverSendsLeave ? 100 : 30;
            reportHidden(pv.uuid, pv.name, "Vanish: still aus TAB entfernt", score);
        }
    }

    private void sendCompletionProbe() {
        class_634 nh = Mc.mc().method_1562();
        if (nh == null) return;
        int id = nextCompId++;
        if (nextCompId > 40000) nextCompId = 30000;
        completionIds.add(id);
        while (completionIds.size() > 8) completionIds.remove(completionIds.iterator().next());
        try {
            nh.method_48296().method_10743(new net.minecraft.class_2805(id, PROBE_COMMAND + " "));
        } catch (Throwable ignored) {}
    }

    private void processCompletionProbe() {
        List<String> names = pendingCompNames;
        if (names == null) return;
        pendingCompNames = null;
        class_634 nh = Mc.mc().method_1562();
        if (nh == null) return;
        Set<String> tabNames = new HashSet<>();
        for (class_640 info : nh.method_2880()) {
            if (info != null && info.method_2966() != null && info.method_2966().name() != null)
                tabNames.add(info.method_2966().name().toLowerCase(Locale.ROOT));
        }
        String self = Mc.player().method_5477().getString();
        List<String> hidden = new ArrayList<>();
        for (String n : names) {
            if (!isStaff(n) || !isPlausibleName(n) || n.equalsIgnoreCase(self) || tabNames.contains(n.toLowerCase(Locale.ROOT)) || recentMsgMentions(n)) continue;
            hidden.add(n);
        }
        if (hidden.isEmpty() || hidden.size() > 3) return;
        for (String n : hidden) reportHidden(knownUuid(n), n, "Vanish: versteckt aber ansprechbar", 90);
    }

    private void scanInvisible() {
        double rSq = SENSOR_RANGE * SENSOR_RANGE;
        for (class_1657 p : Mc.players()) {
            if (p == null || p == Mc.player()) continue;
            try {
                if (!p.method_5767()) continue;
            } catch (Throwable t) { continue; }
            if (p.method_5739(Mc.player()) > rSq) continue;
            String name = profileName(p);
            if (!credible(p.method_5667(), name)) continue;
            addSignal(SignalType.INVISIBLE, name, "Unsichtbare Entity", 35, 5000L);
        }
    }

    private void inspectInvisible(int entityId) {
        class_1297 e = Mc.world().method_8469(entityId);
        if (!(e instanceof class_1657 p) || p == Mc.player()) return;
        try { if (!p.method_5767()) return; } catch (Throwable t) { return; }
        if (p.method_5739(Mc.player()) > SENSOR_RANGE * SENSOR_RANGE) return;
        String name = profileName(p);
        if (!credible(p.method_5667(), name)) return;
        addSignal(SignalType.INVISIBLE, name, "Unsichtbare Entity: Metadata", 35, 5000L);
    }

    private void inspectSound(Obs o) {
        class_243 src = new class_243(o.x, o.y, o.z);
        if (src.method_1022(pos(Mc.player())) > SENSOR_RANGE * SENSOR_RANGE) return;
        if (hasVisibleCause(src)) return;
        String dir = locateDir(src);
        addSignal(SignalType.SOUND, dir, "Sound: " + shortId(o.detail), 14, 3000L);
    }

    private void inspectParticle(Obs o) {
        class_243 src = new class_243(o.x, o.y, o.z);
        if (src.method_1022(pos(Mc.player())) > SENSOR_RANGE * SENSOR_RANGE) return;
        if (hasVisibleCause(src)) return;
        addSignal(SignalType.PARTICLE, locateDir(src), "Partikel: " + shortId(o.detail), 16, 3000L);
    }

    private void announceGamemode(UUID uuid, String mode) {
        if (uuid == null || (Mc.player() != null && Mc.player().method_5667().equals(uuid))) return;
        KnownPlayer kp = knownPlayers.get(uuid);
        if (kp == null || !isStaff(kp.name)) return;
        if ("spectator".equals(mode) && credible(uuid, kp.name)) {
            reportHidden(uuid, kp.name, "Vanish: Zuschauermodus", 100);
        } else if (chatNotify.get()) {
            chatInfo(kp.name + " wechselte zu " + mode);
        }
    }

    private void reportHidden(UUID uuid, String name, String reason, int score) {
        String key = uuid != null ? uuid.toString() : name;
        upsertDetection(key, name, reason, score, System.currentTimeMillis() + DETECTION_TTL_MS);
        FaceSkin.preload(name);
        String spellKey = name.toLowerCase(Locale.ROOT);
        HiddenSpell spell = hiddenSpells.get(spellKey);
        if (spell != null) return;
        hiddenSpells.put(spellKey, new HiddenSpell(uuid, name, reason, tickCnt));
    }

    private void reviewSpells() {
        Iterator<HiddenSpell> it = hiddenSpells.values().iterator();
        while (it.hasNext()) {
            HiddenSpell sp = it.next();
            if (sp.told || sp.sinceTick >= tickCnt) continue;
            sp.told = true;
            chatInfo(sp.name + " ist gevanished (" + sp.reason + ")");
        }
    }

    private boolean endSpell(String name, boolean spectator, long settleMs) {
        String key = name.toLowerCase(Locale.ROOT);
        HiddenSpell sp = hiddenSpells.get(key);
        if (sp == null || System.currentTimeMillis() - sp.sinceMs < settleMs) return false;
        hiddenSpells.remove(key);
        if (sp.uuid != null) detections.remove(sp.uuid.toString());
        if (sp.told && chatNotify.get()) chatInfo(sp.name + " ist wieder sichtbar");
        return true;
    }

    private void addSignal(SignalType type, String subject, String reason, int weight, long cooldownMs) {
        long now = System.currentTimeMillis();
        String coolKey = type.name() + "|" + subject.toLowerCase(Locale.ROOT);
        Long last = signalCooldowns.get(coolKey);
        if (last != null && now - last < cooldownMs) return;
        signalCooldowns.put(coolKey, now);
        signals.addLast(new Signal(type, subject, reason, weight, now));
        upsertDetection("signal:" + coolKey, subject, reason, weight, now + DETECTION_TTL_MS);
        announceSignal(coolKey, subject, reason);
    }

    private void announceSignal(String key, String subject, String reason) {
        long now = System.currentTimeMillis();
        Long last = announceCooldowns.get(key);
        if (last != null && now - last < ANNOUNCE_COOLDOWN_MS) return;
        while (!announceTimes.isEmpty() && now - announceTimes.peekFirst() > 4000L) announceTimes.removeFirst();
        if (announceTimes.size() >= 6) return;
        announceCooldowns.put(key, now);
        announceTimes.addLast(now);
        if (chatNotify.get()) chatInfo(reason + " (" + subject + ")");
    }

    private void upsertDetection(String key, String name, String reason, int score, long expires) {
        Detection d = detections.get(key);
        if (d == null) { detections.put(key, new Detection(name, reason, score, expires)); return; }
        d.name = name;
        d.expiresAt = Math.max(d.expiresAt, expires);
        if (score >= d.score) { d.reason = reason; d.score = score; }
    }

    private void cacheMessage(String text) {
        if (text == null || text.isBlank()) return;
        long now = System.currentTimeMillis();
        recentMessages.addLast(new RecentMsg(text, now));
        while (!recentMessages.isEmpty() && (now - recentMessages.peekFirst().at > RECENT_MESSAGE_TTL_MS || recentMessages.size() > 64)) recentMessages.removeFirst();
    }

    private boolean recentMsgMentions(String name) {
        if (name == null) return false;
        long now = System.currentTimeMillis();
        for (RecentMsg rm : recentMessages) {
            if (now - rm.at > RECENT_MESSAGE_TTL_MS) continue;
            if (looksLikeLeave(rm.text, name)) return true;
        }
        return false;
    }

    private void pruneState() {
        long now = System.currentTimeMillis();
        detections.entrySet().removeIf(e -> e.getValue().expiresAt <= now);
        signalCooldowns.entrySet().removeIf(e -> now - e.getValue() > 60000L);
        announceCooldowns.entrySet().removeIf(e -> now - e.getValue() > 60000L);
        while (!signals.isEmpty() && now - signals.peekFirst().timeMs > SIGNAL_WINDOW_MS) signals.removeFirst();
    }

    private List<HudRow> hudRows(long now) {
        List<HudRow> out = new ArrayList<>();
        if (testMode.get()) {
            out.add(new HudRow(null, "TestAdmin", "CRITICAL"));
            out.add(new HudRow(null, "StaffMember", "VANISH"));
            out.add(new HudRow(null, "HiddenMod", "INVIS"));
            return out;
        }
        if (now < criticalUntilMs) out.add(new HudRow(null, criticalName, "CRITICAL"));
        List<Detection> sorted = new ArrayList<>();
        for (Detection d : detections.values()) if (d.expiresAt > now && worthShowing(d)) sorted.add(d);
        sorted.sort(Comparator.<Detection>comparingInt(d -> d.score).reversed());
        int added = 0;
        for (Detection d : sorted) {
            if (added >= 5) break;
            String tag = hudTag(d.reason);
            out.add(new HudRow(knownUuid(d.name), d.name, tag));
            added++;
        }
        return out;
    }

    private static boolean worthShowing(Detection d) {
        return d.name != null && !d.name.isBlank() && !"Unknown".equalsIgnoreCase(d.name);
    }

    private static String hudTag(String reason) {
        if (reason == null) return "WATCH";
        String l = reason.toLowerCase(Locale.ROOT);
        if (l.startsWith("vanish")) return "VANISH";
        if (l.contains("unsichtbar")) return "INVIS";
        if (l.startsWith("sound")) return "SOUND";
        if (l.startsWith("partikel")) return "PARTICLE";
        return "WATCH";
    }

    private boolean hasVisibleCause(class_243 src) {
        for (class_1297 e : Mc.entities()) {
            if (e == Mc.player()) continue;
            if (e instanceof class_1657 p && !p.method_5767() && pos(e).method_1022(src) <= 16) return true;
        }
        return false;
    }

    private String locateDir(class_243 src) {
        class_243 me = pos(Mc.player());
        double dx = src.field_1352 - me.field_1352, dz = src.field_1350 - me.field_1350;
        long dist = Math.round(Math.sqrt(dx * dx + dz * dz));
        String ns = dz < -1 ? "N" : (dz > 1 ? "S" : "");
        String ew = dx > 1 ? "E" : (dx < -1 ? "W" : "");
        return dist + "m " + ns + ew;
    }

    private static class_243 pos(class_1297 e) {
        return new class_243(Mc.x(e), Mc.y(e), Mc.z(e));
    }

    private static String profileName(class_1657 p) {
        return p.method_7334() != null ? p.method_7334().name() : p.method_5477().getString();
    }

    private void chatInfo(String msg) {
        if (!chatNotify.get()) return;
        Mc.chat("§7[§cAnti Vanish§7] §f" + msg);
    }

    private void resetAll() {
        observations.clear(); knownPlayers.clear(); detections.clear();
        signalCooldowns.clear(); announceCooldowns.clear(); announceTimes.clear();
        signals.clear(); pendingVanishes.clear(); hiddenSpells.clear();
        recentMessages.clear(); listedSinceMs.clear(); completionIds.clear();
        starred.clear(); pendingCompNames = null; serverSendsLeave = false;
        nextCompId = 30000; tickCnt = 0; lastCriticalMs = 0; criticalUntilMs = 0;
        criticalName = "";
    }

    // ================================================================== text helpers

    private static boolean suspiciousSound(String id) {
        String p = shortId(id);
        return p.contains("chest.open") || p.contains("chest.close") || p.contains("barrel.open") || p.contains("barrel.close")
                || p.contains("shulker_box.open") || p.contains("shulker_box.close") || p.contains("door.open") || p.contains("door.close")
                || p.contains("fence_gate.open") || p.contains("fence_gate.close") || p.contains("button.click") || p.contains("lever.click");
    }

    private static boolean suspiciousParticle(String id) {
        String p = shortId(id);
        return p.contains("crit") || p.contains("enchanted_hit") || p.contains("damage_indicator") || p.contains("smoke") || p.equals("block");
    }

    private static String shortId(String id) {
        if (id == null) return "unknown";
        int i = id.indexOf(':');
        return (i >= 0 && i + 1 < id.length() ? id.substring(i + 1) : id).toLowerCase(Locale.ROOT);
    }

    static boolean isUsername(String name) {
        if (name == null || name.length() < 2 || name.length() > 32) return false;
        boolean hasAlnum = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) { hasAlnum = true; continue; }
            if (c == '_' || c == '.') continue;
            return false;
        }
        return hasAlnum;
    }

    static boolean isPlausibleName(String name) {
        if (name == null) return false;
        String t = name.trim();
        return t.length() >= 3 && t.length() <= 16 && t.indexOf(' ') < 0;
    }

    static boolean containsName(String text, String name) {
        if (text == null || name == null) return false;
        return text.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT));
    }

    static boolean looksLikeLeave(String message, String name) {
        if (!containsName(message, name)) return false;
        String l = message.toLowerCase(Locale.ROOT);
        return l.contains("left the game") || l.contains("left") || l.contains("quit") || l.contains("disconnected");
    }

    static String stripCodes(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) { i++; continue; }
            out.append(c);
        }
        return out.toString();
    }

    // ================================================================== face skin loader

    public static final class FaceSkin {
        private static final HttpClient HTTP = HttpClient.newHttpClient();
        private static final Map<String, int[]> FACE_PIXELS = new ConcurrentHashMap<>();
        private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
        private static final int FACE_SIZE = 8;

        public static Gx.Img getFace(String name) {
            if (name == null || name.isBlank()) return null;
            String key = "av_face_" + name.trim().toLowerCase(Locale.ROOT);
            Gx.Img cached = Gx.peek(key);
            if (cached != null) return cached;
            int[] px = FACE_PIXELS.get(name.trim().toLowerCase(Locale.ROOT));
            if (px == null) { preload(name); return null; }
            return Gx.get(key, FACE_SIZE, () -> new int[][]{px, {FACE_SIZE, FACE_SIZE}});
        }

        public static void preload(String name) {
            if (name == null || name.isBlank()) return;
            String key = name.trim().toLowerCase(Locale.ROOT);
            if (FACE_PIXELS.containsKey(key) || !LOADING.add(key)) return;
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://minotar.net/helm/" + URLEncoder.encode(name.trim(), StandardCharsets.UTF_8) + "/8.png"))
                    .header("User-Agent", "Mozilla/5.0").build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray()).thenAccept(resp -> {
                if (resp.statusCode() != 200) { LOADING.remove(key); return; }
                try {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(resp.body()));
                    if (img == null) { LOADING.remove(key); return; }
                    int w = img.getWidth(), h = img.getHeight();
                    int[] pixels = new int[FACE_SIZE * FACE_SIZE];
                    for (int y = 0; y < FACE_SIZE; y++) {
                        for (int x = 0; x < FACE_SIZE; x++) {
                            int sx = x * w / FACE_SIZE, sy = y * h / FACE_SIZE;
                            pixels[y * FACE_SIZE + x] = img.getRGB(sx, sy);
                        }
                    }
                    FACE_PIXELS.put(key, pixels);
                } catch (Throwable ignored) {}
                LOADING.remove(key);
            }).exceptionally(e -> { LOADING.remove(key); return null; });
        }
    }

    // ================================================================== records

    private enum ObsType { TAB_REMOVE, TAB_HIDE, PLAYER_LEFT, SYSTEM_CHAT, ENTITY_META, SOUND, PARTICLE, GAMEMODE }
    private enum SignalType { VANISH, INVISIBLE, SOUND, PARTICLE }

    private record Obs(ObsType type, UUID uuid, int entityId, double x, double y, double z, String detail) {
        static Obs tabRemove(UUID id) { return new Obs(ObsType.TAB_REMOVE, id, -1, 0, 0, 0, ""); }
        static Obs tabHide(UUID id) { return new Obs(ObsType.TAB_HIDE, id, -1, 0, 0, 0, ""); }
        static Obs systemChat(String t) { return new Obs(ObsType.SYSTEM_CHAT, null, -1, 0, 0, 0, t); }
        static Obs gamemode(UUID id, String m) { return new Obs(ObsType.GAMEMODE, id, -1, 0, 0, 0, m); }
        static Obs entityMeta(int id) { return new Obs(ObsType.ENTITY_META, null, id, 0, 0, 0, ""); }
        static Obs position(ObsType t, double x, double y, double z, String d) { return new Obs(t, null, -1, x, y, z, d); }
    }

    private record KnownPlayer(UUID uuid, String name) {}
    private record PendingVanish(UUID uuid, String name, int dueTick) {}
    private record Signal(SignalType type, String subject, String reason, int weight, long timeMs) {}
    private record RecentMsg(String text, long at) {}
    private record HudRow(UUID uuid, String name, String tag) {}

    private static final class Detection {
        String name, reason; int score; long expiresAt;
        Detection(String n, String r, int s, long e) { name = n; reason = r; score = s; expiresAt = e; }
    }

    private static final class HiddenSpell {
        final UUID uuid; final String name, reason; final int sinceTick; final long sinceMs = System.currentTimeMillis();
        boolean told;
        HiddenSpell(UUID u, String n, String r, int t) { uuid = u; name = n; reason = r; sinceTick = t; }
    }
}
