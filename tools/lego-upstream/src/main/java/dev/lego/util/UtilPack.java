package dev.lego.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Sound;
import dev.lego.ui.Toasts;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2588;
import net.minecraft.class_7172;
import net.minecraft.class_746;

/**
 * Paket 4: useful helpers - warnings, chat tools, local chat commands (.rechne, .würfel ...), reminders,
 * camera / option toggles and performance presets. Nothing here gives an unfair advantage: only information,
 * reminders and vanilla options.
 */
public final class UtilPack {
    private UtilPack() { }

    private static final List<Module> MINE = new ArrayList<>();

    private static <T extends Module> T reg(T m) { MINE.add(m); return Modules.register(m); }

    // ================================================================== generic module types
    /**
     * Vanilla option set while the module is on. Several modules may change the same option: the original value is
     * remembered once and restored when the last of them is switched off; otherwise the newest active one wins.
     */
    static final class Opt<T> extends Module {
        private static final java.util.Map<Object, java.util.ArrayDeque<Opt<?>>> ACTIVE = new java.util.IdentityHashMap<>();
        private static final java.util.Map<Object, Object> ORIGINAL = new java.util.IdentityHashMap<>();
        private final Supplier<class_7172<T>> opt;
        private final T on, fallback;
        private boolean applied;

        Opt(String id, String name, String desc, String icon, Category cat, Supplier<class_7172<T>> opt, T on, T fallback) {
            super("u4_" + id, name, desc, cat);
            icon(icon);
            fresh();
            this.opt = opt; this.on = on; this.fallback = fallback;
        }

        @Override public void tick() { apply(); }
        @Override public void onEnable() { apply(); }

        private void apply() {
            if (applied || Mc.options() == null) return;
            try {
                class_7172<T> o = opt.get();
                java.util.ArrayDeque<Opt<?>> st = ACTIVE.computeIfAbsent(o, k -> new java.util.ArrayDeque<>());
                if (st.isEmpty()) ORIGINAL.put(o, o.method_41753());
                st.remove(this);
                st.push(this);
                o.method_41748(on);
                applied = true;
            } catch (Throwable t) { LegoClient.LOG(id + ": " + t); }
        }

        @SuppressWarnings("unchecked")
        @Override public void onDisable() {
            if (!applied || Mc.options() == null) { applied = false; return; }
            applied = false;
            try {
                class_7172<T> o = opt.get();
                java.util.ArrayDeque<Opt<?>> st = ACTIVE.get(o);
                if (st != null) st.remove(this);
                if (st == null || st.isEmpty()) {
                    Object orig = ORIGINAL.remove(o);
                    o.method_41748(orig != null ? (T) orig : fallback);
                } else o.method_41748((T) st.peek().on);
            } catch (Throwable t) { LegoClient.LOG(id + ": " + t); }
        }
    }

    /** Several options at once (performance presets). */
    static final class Preset extends Module {
        private final Opt<?>[] parts;
        Preset(String id, String name, String desc, Opt<?>... parts) { super("u4_" + id, name, desc, Category.PERFORMANCE); icon("boost"); fresh(); this.parts = parts; }
        @Override public void onEnable() { for (Opt<?> o : parts) o.onEnable(); }
        @Override public void tick() { for (Opt<?> o : parts) o.tick(); }
        @Override public void onDisable() { for (Opt<?> o : parts) o.onDisable(); }
    }

    /** Local chat command: ".name args" is handled here and never sent to the server. */
    interface Cmd { String run(String args) throws Exception; }

    static final class Command extends Module {
        final String[] names;
        final Cmd cmd;
        final String usage;
        Command(String id, String name, String desc, String icon, String usage, Cmd cmd, String... names) {
            super("u4_" + id, name, desc, Category.UTILITY);
            icon(icon);
            fresh();
            enabled = true;
            this.names = names; this.cmd = cmd; this.usage = usage;
        }
    }

    /** Message-based toggle (filters, sounds) - logic lives in the event handlers below. */
    static final class Flag extends Module {
        Flag(String id, String name, String desc, String icon) { super("u4_" + id, name, desc, Category.UTILITY); icon(icon); fresh(); }
    }

    // ================================================================== chat helpers
    static void local(String s) {
        try { Mc.mc().field_1705.method_1743().method_1812(class_2561.method_43470("[Lego] ").method_27692(class_124.field_1065).method_10852(class_2561.method_43470(s))); } catch (Throwable t) { LegoClient.LOG("chat: " + t); }
    }

    private static String key(class_2561 msg) {
        try { return msg.method_10851() instanceof class_2588 tc ? tc.method_11022() : ""; } catch (Throwable t) { return ""; }
    }

    private static Path dir() {
        Path p = Mc.mc().field_1697.toPath().resolve("config").resolve("legoclient");
        try { Files.createDirectories(p); } catch (Exception ignored) { }
        return p;
    }

    private static void append(String file, String line) {
        try { Files.writeString(dir().resolve(file), line + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND); } catch (Exception e) { LegoClient.LOG(file + ": " + e); }
    }

    // ================================================================== state for the warnings
    private static final Random RND = new Random();
    private static Set<String> lastTab = null;
    private static long lastMove = System.currentTimeMillis();
    private static double afkX = Double.NaN, afkZ;
    private static boolean afk;

    private static class_746 me() { return Mc.player(); }


    private static Set<String> tab() {
        Set<String> s = new HashSet<>();
        for (var pi : Mc.mc().method_1562().method_2880()) s.add(pi.method_2966().name());
        return s;
    }

    // ================================================================== modules
    static Flag timestamps, hideJoin, hideDeath, hideAdv, hideLinks, capsFilter, chatSound, pmSound, chatLog, emoji, capitalize, coordLog;
    static Setting.Text friends;
    static Module friendsMod, joinLeave;
    static Command[] commands;
    static Module afkMod, summaryMod, alarmMod;
    static Setting.Text alarmTime;

    public static void registerAll() {
        // ---------------------------------------------------------------- server / friends
        joinLeave = reg(new Module("u4_joinleave", "Join/Leave-Anzeige", "Kleine Meldung, wenn Spieler den Server betreten oder verlassen", Category.UTILITY) {
            { icon("users"); fresh(); }
            @Override public void tick() { tabDiff(false); }
            @Override public void onDisable() { lastTab = null; }
        });
        friendsMod = reg(new Module("u4_friends", "Freunde-Meldung", "Meldet, wenn deine Freunde online kommen (Namen in den Einstellungen)", Category.UTILITY) {
            { icon("heart"); fresh(); friends = add(new Setting.Text("Freunde", "", 200, "Name1, Name2, ...")); }
            @Override public void tick() { tabDiff(true); }
        });
        // ---------------------------------------------------------------- chat display
        timestamps = reg(new Flag("timestamps", "Chat-Zeitstempel", "Zeigt die Uhrzeit vor jeder Chat-Nachricht", "clock"));
        hideJoin = reg(new Flag("hidejoin", "Beitritte ausblenden", "Blendet \"... hat das Spiel betreten/verlassen\" aus", "users"));
        hideDeath = reg(new Flag("hidedeath", "Todesnachrichten ausblenden", "Blendet die Todesnachrichten anderer Spieler aus", "skull"));
        hideAdv = reg(new Flag("hideadv", "Erfolge ausblenden", "Blendet \"... hat den Fortschritt erzielt\" aus", "trophy"));
        hideLinks = reg(new Flag("hidelinks", "Link-Filter", "Blendet Chat-Nachrichten mit Links (Werbung) aus", "filter"));
        capsFilter = reg(new Flag("caps", "Schrei-Filter", "Zeigt Nachrichten in GROSSBUCHSTABEN klein an", "mute"));
        chatSound = reg(new Flag("chatsound", "Chat-Ton", "Leiser Ton bei jeder Chat-Nachricht", "sound"));
        pmSound = reg(new Flag("pmsound", "Privatnachricht-Ton", "Ton + Meldung bei privaten Nachrichten (/msg)", "chat"));
        chatLog = reg(new Flag("chatlog", "Chat-Protokoll", "Speichert den Chat in config/legoclient/chat.log", "clipboard"));
        emoji = reg(new Flag("emoji", "Emoji-Ersetzer", "Ersetzt :herz: :stern: :haken: :pfeil: :musik: :sonne: :smile: in deinen Nachrichten", "emote"));
        capitalize = reg(new Flag("capitalize", "Auto-Großschreibung", "Macht den ersten Buchstaben deiner Nachrichten groß", "chat"));
        // ---------------------------------------------------------------- local chat commands
        commands = new Command[]{
                reg(new Command("calc", "Rechner", "Tippe .rechne 12*(3+4) in den Chat - das Ergebnis siehst nur du", "number", ".rechne <Aufgabe>", a -> {
                    double r = new Calc(a).parse();
                    return a.trim() + " = " + (r == Math.rint(r) && Math.abs(r) < 1e15 ? String.valueOf((long) r) : String.format(Locale.ROOT, "%.6g", r));
                }, "rechne", "calc")),
                reg(new Command("dice", "Würfel", ".würfel oder .würfel 20 - würfelt eine Zahl (nur für dich)", "gamepad", ".würfel [Seiten]", a -> {
                    int s = a.isBlank() ? 6 : Math.max(2, Math.min(1000, Integer.parseInt(a.trim())));
                    return "W" + s + ": " + (1 + RND.nextInt(s));
                }, "würfel", "wuerfel", "dice")),
                reg(new Command("coin", "Münzwurf", ".münze - Kopf oder Zahl", "gem", ".münze", a -> RND.nextBoolean() ? "Kopf" : "Zahl", "münze", "muenze", "coin")),
                reg(new Command("random", "Zufallszahl", ".zufall 1 100 - Zufallszahl im Bereich", "number", ".zufall <von> <bis>", a -> {
                    String[] p = a.trim().split("\\s+");
                    int lo = Integer.parseInt(p[0]), hi = Integer.parseInt(p[1]);
                    if (hi < lo) { int x = lo; lo = hi; hi = x; }
                    return "Zufall " + lo + "-" + hi + ": " + (lo + RND.nextInt(hi - lo + 1));
                }, "zufall", "random")),
                reg(new Command("choose", "Entscheider", ".wähle Pizza, Nudeln, Döner - wählt zufällig eins aus", "star", ".wähle a, b, c", a -> {
                    String[] p = a.split(",");
                    return "Gewählt: " + p[RND.nextInt(p.length)].trim();
                }, "wähle", "waehle", "choose")),
                reg(new Command("nether", "Portal-Rechner", ".nether x z oder .oberwelt x z - rechnet Portal-Koordinaten um", "compass", ".nether <x> <z>", a -> {
                    String[] p = a.trim().split("\\s+");
                    double x = Double.parseDouble(p[0]), z = Double.parseDouble(p[1]);
                    return "Nether: " + Math.round(x / 8) + " " + Math.round(z / 8) + "  ·  Oberwelt: " + Math.round(x * 8) + " " + Math.round(z * 8);
                }, "nether", "oberwelt", "portal")),
                reg(new Command("dist", "Entfernungs-Rechner", ".dist x z - wie weit ein Ort von dir weg ist und in welche Richtung", "waypoint", ".dist <x> <z>", a -> {
                    String[] p = a.trim().split("\\s+");
                    double x = Double.parseDouble(p[0]), z = Double.parseDouble(p[1]);
                    double dx = x - Mc.x(me()), dz = z - Mc.z(me());
                    String[] dirs = {"Süden", "Südwesten", "Westen", "Nordwesten", "Norden", "Nordosten", "Osten", "Südosten"};
                    double ang = Math.toDegrees(Math.atan2(-dx, dz));
                    return Math.round(Math.hypot(dx, dz)) + " Blöcke Richtung " + dirs[(int) Math.floorMod(Math.round(ang / 45), 8)];
                }, "dist", "entfernung")),
                reg(new Command("note", "Notizen", ".notiz Text speichert eine Notiz, .notizen zeigt alle", "bookmark", ".notiz <Text>", a -> {
                    if (a.isBlank()) return "Benutzung: .notiz <Text>";
                    append("notes.txt", java.time.LocalDate.now() + " " + LocalTime.now().withNano(0) + "  " + a.trim());
                    return "Notiz gespeichert";
                }, "notiz", "note")),
                reg(new Command("notes", "Notizen anzeigen", ".notizen - zeigt deine letzten Notizen", "bookmark", ".notizen", a -> {
                    Path f = dir().resolve("notes.txt");
                    if (!Files.exists(f)) return "Noch keine Notizen";
                    List<String> l = Files.readAllLines(f, StandardCharsets.UTF_8);
                    StringBuilder sb = new StringBuilder("Notizen:");
                    for (int i = Math.max(0, l.size() - 8); i < l.size(); i++) sb.append("\n  ").append(l.get(i));
                    return sb.toString();
                }, "notizen", "notes")),
                reg(new Command("remind", "Erinnerung", ".erinnere 10 Ofen leeren - erinnert dich in 10 Minuten", "bell", ".erinnere <Minuten> <Text>", a -> {
                    String[] p = a.trim().split("\\s+", 2);
                    double min = Double.parseDouble(p[0].replace(',', '.'));
                    String text = p.length > 1 ? p[1] : "Erinnerung";
                    TIMERS.add(new Object[]{System.currentTimeMillis() + (long) (min * 60_000), text});
                    return "Ich erinnere dich in " + p[0] + " min: " + text;
                }, "erinnere", "remind")),
                reg(new Command("countdown", "Countdown", ".countdown 30 - zählt 30 Sekunden runter", "hourglass", ".countdown <Sekunden>", a -> {
                    int s = Math.max(1, Math.min(36000, Integer.parseInt(a.trim())));
                    TIMERS.add(new Object[]{System.currentTimeMillis() + s * 1000L, "Countdown " + s + " s ist abgelaufen!"});
                    return "Countdown läuft: " + s + " s";
                }, "countdown", "timer")),
                reg(new Command("time", "Zeit-Befehl", ".zeit - zeigt echte Uhrzeit und Minecraft-Uhrzeit", "clock", ".zeit", a -> {
                    long t = (Mc.world().method_8532() + 6000) % 24000;
                    return "Echt: " + LocalTime.now().withNano(0) + "  ·  Minecraft: " + String.format(Locale.ROOT, "%02d:%02d", t / 1000, (t % 1000) * 60 / 1000) + "  ·  Tag " + (Mc.world().method_8532() / 24000 + 1);
                }, "zeit", "time")),
                reg(new Command("pos", "Position-Befehl", ".pos - zeigt deine Position (nur für dich) und kopiert sie", "pin", ".pos", a -> {
                    String s = (int) Math.floor(Mc.x(me())) + " " + (int) Math.floor(Mc.y(me())) + " " + (int) Math.floor(Mc.z(me()));
                    Mc.clipboard(s);
                    return "Position " + s + " (kopiert)";
                }, "pos", "position")),
                reg(new Command("help", "Lego-Befehle", ".lego - zeigt alle Lego-Chat-Befehle", "info", ".lego", a -> {
                    StringBuilder sb = new StringBuilder("Befehle (nur für dich sichtbar):");
                    for (Command c : commands) if (c.enabled && c.cmd != null && !c.id.equals("u4_help")) sb.append("\n  ").append(c.usage).append(" - ").append(c.name);
                    return sb.toString();
                }, "lego", "befehle")),
        };
        // ---------------------------------------------------------------- tools
        alarmMod = reg(new Module("u4_alarm", "Wecker", "Klingelt zur eingestellten echten Uhrzeit (z. B. 18:30)", Category.UTILITY) {
            private String fired = "";
            { icon("bell"); fresh(); alarmTime = add(new Setting.Text("Uhrzeit", "18:00", 5, "HH:MM")); }
            @Override public void tick() {
                String now = String.format(Locale.ROOT, "%02d:%02d", LocalTime.now().getHour(), LocalTime.now().getMinute());
                if (now.equals(alarmTime.get().trim()) && !fired.equals(now)) {
                    fired = now;
                    Toasts.show("bell", "Wecker", "Es ist " + now, 0xFFFFC53D, 10000);
                    for (int i = 0; i < 3; i++) Sound.play("minecraft:block.note_block.bell", 1.0f + i * 0.2f, 0.9f);
                }
            }
        });
        afkMod = reg(new Module("u4_afk", "AFK-Erkennung", "Merkt, wenn du nicht am PC warst, und sagt dir danach, wie lange", Category.UTILITY) {
            final Setting.Num after = add(new Setting.Num("AFK nach", 1, 30, 1, 3, " min"));
            { icon("hourglass"); fresh(); }
            @Override public void tick() {
                class_746 p = me();
                if (p == null) return;
                double x = Mc.x(p), z = Mc.z(p);
                boolean moved = Double.isNaN(afkX) || Math.abs(x - afkX) > 0.05 || Math.abs(z - afkZ) > 0.05 || Mc.mouseDown(0) || Mc.mouseDown(1);
                afkX = x; afkZ = z;
                long now = System.currentTimeMillis();
                if (moved) {
                    if (afk) { afk = false; Toasts.show("user", "Willkommen zurück", "Du warst " + Math.max(1, (now - lastMove) / 60_000) + " min AFK", 0xFF39D353, 4000); }
                    lastMove = now;
                } else if (!afk && now - lastMove > after.get() * 60_000) afk = true;
            }
        });
        coordLog = reg(new Flag("coordlog", "Weg-Protokoll", "Speichert alle 2 Minuten deine Koordinaten in config/legoclient/wege.txt (damit du zurückfindest)", "trail"));
        summaryMod = reg(new Module("u4_summary", "Sitzungs-Bericht", "Zeigt beim Verlassen einer Welt, wie weit du gelaufen bist und wie lange du da warst", Category.UTILITY) {
            private long joined;
            private double dist, lx = Double.NaN, lz;
            { icon("list"); fresh(); }
            @Override public void tick() {
                class_746 p = me();
                if (p == null) {
                    if (joined != 0) {
                        Toasts.show("list", "Sitzung beendet", (System.currentTimeMillis() - joined) / 60_000 + " min · " + Math.round(dist) + " Blöcke gelaufen", 0xFF3FA9FF, 6000);
                        joined = 0; dist = 0; lx = Double.NaN;
                    }
                    return;
                }
                if (joined == 0) joined = System.currentTimeMillis();
                double x = Mc.x(p), z = Mc.z(p);
                if (!Double.isNaN(lx)) { double d = Math.hypot(x - lx, z - lz); if (d < 20) dist += d; }
                lx = x; lz = z;
            }
        });
        // ---------------------------------------------------------------- camera & display (vanilla options while on)
        reg(new Opt<>("sniper", "Sniper-Maus", "Sehr langsame Maus zum genauen Zielen (z. B. auf eine Taste legen)", "crosshair", Category.UTILITY, () -> Mc.options().method_42495(), 0.12, 0.5));
        reg(new Opt<>("fastmouse", "Schnelle Maus", "Höhere Mausempfindlichkeit, solange an", "mouse", Category.UTILITY, () -> Mc.options().method_42495(), 0.85, 0.5));
        reg(new Opt<>("farview", "Weitsicht", "Sichtweite auf 24 Chunks, solange an", "eye", Category.VISUALS, () -> Mc.options().method_42503(), 24, 12));
        reg(new Opt<>("nearview", "Nahsicht", "Sichtweite auf 6 Chunks (spart viel Leistung)", "eye", Category.PERFORMANCE, () -> Mc.options().method_42503(), 6, 12));
        reg(new Opt<>("nobob", "Kein Wackeln beim Laufen", "Schaltet das Kopf-Wippen beim Laufen aus", "run", Category.VISUALS, () -> Mc.options().method_42448(), false, true));
        reg(new Opt<>("widefov", "Weites Sichtfeld", "Sichtfeld 100, solange an", "eye", Category.VISUALS, () -> Mc.options().method_41808(), 100, 70));
        reg(new Opt<>("quakefov", "Quake-Sichtfeld", "Sichtfeld 110 für maximalen Überblick", "eye", Category.VISUALS, () -> Mc.options().method_41808(), 110, 70));
        reg(new Opt<>("nofoveffect", "Kein Sprint-Zoom", "Kein FOV-Wechsel beim Sprinten und durch Effekte", "speed", Category.VISUALS, () -> Mc.options().method_42454(), 0.0, 1.0));
        reg(new Opt<>("noscreenfx", "Keine Bildschirm-Verzerrung", "Kein Wabern durch Übelkeit oder Portale", "visuals", Category.VISUALS, () -> Mc.options().method_42453(), 0.0, 1.0));
        reg(new Module("u4_cinematic", "Kino-Kamera", "Weiche, filmische Kamerabewegung (gut für Videos)", Category.VISUALS) {
            { icon("camera"); fresh(); }
            @Override public void tick() { if (Mc.options() != null) Mc.options().field_1914 = true; }
            @Override public void onDisable() { if (Mc.options() != null) Mc.options().field_1914 = false; }
        });
        reg(new Opt<>("vsync", "VSync", "Kein Bildreißen (Tearing), FPS an den Monitor gebunden", "fps", Category.PERFORMANCE, () -> Mc.options().method_42433(), true, false));
        reg(new Opt<>("noshadows", "Entity-Schatten aus", "Keine runden Schatten unter Mobs (mehr FPS)", "boost", Category.PERFORMANCE, () -> Mc.options().method_42435(), false, true));
        reg(new Opt<>("bright", "Helligkeit hell", "Helligkeit auf \"Hell\", solange an", "sun", Category.VISUALS, () -> Mc.options().method_42473(), 1.0, 0.5));
        // ---------------------------------------------------------------- performance (vanilla options)
        Opt<net.minecraft.class_4063> cloudsOff = new Opt<>("clouds", "Wolken aus", "Keine Wolken (mehr FPS)", "cloud", Category.PERFORMANCE, () -> Mc.options().method_42528(), net.minecraft.class_4063.field_18162, net.minecraft.class_4063.field_18164);
        Opt<net.minecraft.class_4066> partMin = new Opt<>("particlesmin", "Partikel minimal", "Nur die nötigsten Partikel (viel mehr FPS bei Explosionen)", "particles", Category.PERFORMANCE, () -> Mc.options().method_42475(), net.minecraft.class_4066.field_18199, net.minecraft.class_4066.field_18197);
        Opt<Boolean> shadowsOff = new Opt<>("noshadows2", "-", "-", "boost", Category.PERFORMANCE, () -> Mc.options().method_42435(), false, true);
        Opt<Integer> blendOff = new Opt<>("noblend", "Biom-Übergang aus", "Keine weichen Farbübergänge zwischen Biomen (schnelleres Laden)", "biome", Category.PERFORMANCE, () -> Mc.options().method_41805(), 0, 2);
        Opt<Double> entDist = new Opt<>("entitydist", "Entity-Distanz 50 %", "Mobs werden nur halb so weit angezeigt (mehr FPS)", "entity", Category.PERFORMANCE, () -> Mc.options().method_42517(), 0.5, 1.0);
        Opt<Integer> render8 = new Opt<>("render8", "-", "-", "eye", Category.PERFORMANCE, () -> Mc.options().method_42503(), 8, 12);
        reg(cloudsOff);
        reg(new Opt<>("cloudsfast", "Schnelle Wolken", "Einfache flache Wolken statt 3D-Wolken", "cloud", Category.PERFORMANCE, () -> Mc.options().method_42528(), net.minecraft.class_4063.field_18163, net.minecraft.class_4063.field_18164));
        reg(partMin);
        reg(new Opt<>("particlesless", "Partikel reduziert", "Weniger Partikel (guter Mittelweg)", "particles", Category.PERFORMANCE, () -> Mc.options().method_42475(), net.minecraft.class_4066.field_18198, net.minecraft.class_4066.field_18197));
        reg(blendOff);
        reg(new Opt<>("noao", "Weiches Licht aus", "Keine weichen Schatten an Blöcken (mehr FPS auf schwachen PCs)", "light", Category.PERFORMANCE, () -> Mc.options().method_41792(), false, true));
        reg(entDist);
        reg(new Opt<>("simdist", "Simulation 5 Chunks", "Weniger simulierte Chunks im Einzelspieler (weniger Ruckler)", "cpu", Category.PERFORMANCE, () -> Mc.options().method_42510(), 5, 12));
        reg(new Opt<>("battery", "Akku-Sparmodus", "FPS auf 60 begrenzt - Laptop bleibt kühler und leiser", "boost", Category.PERFORMANCE, () -> Mc.options().method_42524(), 60, 120));
        reg(new Opt<>("fps30", "Leise-Modus", "FPS auf 30 begrenzt - für AFK-Farmen oder sehr schwache PCs", "boost", Category.PERFORMANCE, () -> Mc.options().method_42524(), 30, 120));
        reg(new Opt<>("unlimited", "FPS unbegrenzt", "Hebt das FPS-Limit auf (maximale Bildrate)", "fps", Category.PERFORMANCE, () -> Mc.options().method_42524(), 260, 120));
        reg(new Preset("ultra", "Ultra-Leistung", "Ein Klick für maximale FPS: keine Wolken, minimale Partikel, keine Schatten, kein Biom-Übergang, halbe Entity-Distanz, Sichtweite 8",
                cloudsOff, partMin, shadowsOff, blendOff, entDist, render8));
        reg(new Preset("balanced", "Ausgewogen", "Gute FPS bei schöner Grafik: schnelle Wolken, reduzierte Partikel, Sichtweite 10",
                new Opt<>("cloudsfast2", "-", "-", "cloud", Category.PERFORMANCE, () -> Mc.options().method_42528(), net.minecraft.class_4063.field_18163, net.minecraft.class_4063.field_18164),
                new Opt<>("particlesless2", "-", "-", "particles", Category.PERFORMANCE, () -> Mc.options().method_42475(), net.minecraft.class_4066.field_18198, net.minecraft.class_4066.field_18197),
                new Opt<>("render10", "-", "-", "eye", Category.PERFORMANCE, () -> Mc.options().method_42503(), 10, 12)));
        // ---------------------------------------------------------------- new utility modules
        reg(new Module("u4_copycoords", "Koordinaten kopieren", "Kopiert deine XYZ-Koordinaten in die Zwischenablage (einmal drücken)", Category.UTILITY) {
            { icon("compass"); fresh(); }
            @Override public boolean isToggleable() { return false; }
            @Override public void onEnable() {
                class_746 p = me();
                if (p == null) { enabled = false; return; }
                String s = Math.round(Mc.x(p)) + " " + Math.round(Mc.y(p)) + " " + Math.round(Mc.z(p));
                try { java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(s), null); } catch (Throwable ignored) {}
                Toasts.show("compass", "Koordinaten kopiert", s, 0xFF3FA9FF, 2500);
                enabled = false;
            }
        });
        reg(new Module("u4_deathcoords", "Todes-Koordinaten", "Merkt sich, wo du gestorben bist, und zeigt es als Toast", Category.UTILITY) {
            double dx = Double.NaN, dy, dz;
            String dim = "";
            { icon("skull"); fresh(); }
            @Override public void tick() {
                class_746 p = me();
                if (p == null) return;
                if (p.method_29504() && !Double.isNaN(dx)) {
                    String d = dim.contains("nether") ? " (Nether)" : dim.contains("end") ? " (End)" : "";
                    Toasts.show("skull", "Gestorben bei", Math.round(dx) + " " + Math.round(dy) + " " + Math.round(dz) + d, 0xFFFF4D5E, 8000);
                    dx = Double.NaN;
                }
                if (p.method_29504()) return;
                dx = Mc.x(p); dy = Mc.y(p); dz = Mc.z(p);
                dim = Mc.world() != null ? Mc.world().method_27983().method_29177().toString() : "";
            }
        });
        reg(new Module("u4_chunkborder", "Chunk-Grenzen", "Zeigt die Chunk-Grenzen als grüne Linien um dich herum (wie F3+G, aber hübscher)", Category.UTILITY) {
            { icon("grid"); fresh(); }
        });
        registerEvents();
    }

    // ================================================================== timers (.erinnere / .countdown) + periodic stuff
    static final List<Object[]> TIMERS = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static long nextCoordLog;

    /** Called every client tick (LegoClient). */
    public static void tick() {
        long now = System.currentTimeMillis();
        for (Object[] t : TIMERS)
            if (now >= (long) t[0]) {
                TIMERS.remove(t);
                Toasts.show("bell", "Erinnerung", (String) t[1], 0xFFFFC53D, 10000);
                Sound.play("minecraft:block.note_block.bell", 1.2f, 0.9f);
                local("⏰ " + t[1]);
            }
        if (coordLog != null && coordLog.enabled && me() != null && now >= nextCoordLog) {
            nextCoordLog = now + 120_000;
            append("wege.txt", java.time.LocalDate.now() + " " + LocalTime.now().withNano(0) + "  " + (int) Math.floor(Mc.x(me())) + " " + (int) Math.floor(Mc.y(me())) + " " + (int) Math.floor(Mc.z(me()))
                    + "  " + Utility.dimName(Utility.dimension()) + "  " + Mc.serverAddress());
        }
    }

    private static Set<String> lastTabFriends = null;

    private static void tabDiff(boolean friendsOnly) {
        try {
            if (Mc.mc().method_1562() == null) { if (friendsOnly) lastTabFriends = null; else lastTab = null; return; }
            Set<String> now = tab(), before = friendsOnly ? lastTabFriends : lastTab;
            if (before != null) {
                if (friendsOnly) {
                    Set<String> fr = new HashSet<>();
                    for (String s : friends.get().split(",")) if (!s.isBlank()) fr.add(s.trim().toLowerCase(Locale.ROOT));
                    for (String n : now)
                        if (!before.contains(n) && fr.contains(n.toLowerCase(Locale.ROOT))) { Toasts.show("heart", "Freund online", n + " ist jetzt online", 0xFFFF4FA3, 5000); Sound.play("minecraft:block.note_block.pling", 1.6f, 0.8f); }
                } else {
                    int shown = 0;
                    for (String n : now) if (!before.contains(n) && shown++ < 3) Toasts.show("users", "Beigetreten", n, 0xFF39D353, 2000);
                    for (String n : before) if (!now.contains(n) && shown++ < 3) Toasts.show("users", "Verlassen", n, 0xFF9AA3B2, 2000);
                }
            }
            if (friendsOnly) lastTabFriends = now; else lastTab = now;
        } catch (Throwable t) { if (friendsOnly) lastTabFriends = null; else lastTab = null; }
    }

    // ================================================================== chat events
    private static void registerEvents() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((msg, overlay) -> overlay || receive(msg, true));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((msg, signed, sender, params, ts) -> receive(msg, false));
        ClientSendMessageEvents.ALLOW_CHAT.register(UtilPack::command);
        ClientSendMessageEvents.MODIFY_CHAT.register(UtilPack::modify);
    }

    /** false = hide (or re-added in a changed form). */
    private static boolean receive(class_2561 msg, boolean game) {
        try {
            String k = key(msg), s = msg.getString();
            if (hideJoin.enabled && (k.startsWith("multiplayer.player.joined") || k.equals("multiplayer.player.left"))) return false;
            if (hideDeath.enabled && k.startsWith("death.")) return false;
            if (hideAdv.enabled && k.startsWith("chat.type.advancement")) return false;
            if (hideLinks.enabled && !game && (s.contains("http://") || s.contains("https://") || s.matches(".*\\b[a-z0-9-]+\\.(de|com|net|gg|eu|org|io)\\b.*"))) return false;
            if (chatLog.enabled) append("chat.log", LocalTime.now().withNano(0) + " " + s);
            if (chatSound.enabled && !game) Sound.play("minecraft:block.note_block.hat", 1.6f, 0.25f);
            String low = s.toLowerCase(Locale.ROOT);
            if (pmSound.enabled && (low.contains("whispers to you") || low.contains("flüstert dir") || low.contains("-> mir") || low.contains("-> me") || k.startsWith("commands.message.display.incoming"))) {
                Sound.play("minecraft:entity.experience_orb.pickup", 1.4f, 0.8f);
                Toasts.show("chat", "Privatnachricht", s.length() > 80 ? s.substring(0, 80) + "…" : s, 0xFF3FA9FF, 4000);
            }
            boolean caps = capsFilter.enabled && !game && s.length() >= 8 && s.chars().filter(Character::isLetter).count() >= 6 && s.equals(s.toUpperCase(Locale.ROOT)) && !s.equals(low);
            if (timestamps.enabled || caps) {
                Utility.onMessage(msg, false);   // mention ping etc. still see it
                class_2561 body = caps ? class_2561.method_43470(low) : msg;
                class_2561 out = timestamps.enabled
                        ? class_2561.method_43470(String.format(Locale.ROOT, "[%02d:%02d] ", LocalTime.now().getHour(), LocalTime.now().getMinute())).method_27692(class_124.field_1063).method_10852(body)
                        : body;
                Mc.mc().field_1705.method_1743().method_1812(out);
                return false;
            }
        } catch (Throwable t) { LegoClient.LOG("Chat-Paket: " + t); }
        return true;
    }

    /** Local commands: false = not sent. */
    private static boolean command(String msg) {
        if (!msg.startsWith(".") || msg.length() < 2) return true;
        String body = msg.substring(1);
        String name = body.split("\\s+", 2)[0].toLowerCase(Locale.ROOT), args = body.contains(" ") ? body.substring(body.indexOf(' ') + 1) : "";
        for (Command c : commands) {
            if (!c.enabled) continue;
            for (String n : c.names)
                if (n.equals(name)) {
                    String r;
                    try { r = c.cmd.run(args); } catch (Throwable t) { r = "Benutzung: " + c.usage; }
                    for (String line : r.split("\n")) local(line);
                    return false;
                }
        }
        return true;
    }

    private static final String[][] EMOJI = {{":herz:", "❤"}, {":stern:", "★"}, {":haken:", "✔"}, {":kreuz:", "✖"}, {":pfeil:", "➜"}, {":musik:", "♫"}, {":sonne:", "☀"}, {":smile:", "☺"}, {":blitz:", "⚡"}, {":schnee:", "❄"}, {":krone:", "♛"}, {":schwert:", "⚔"}};

    private static String modify(String msg) {
        String s = msg;
        if (emoji.enabled) for (String[] e : EMOJI) s = s.replace(e[0], e[1]);
        if (capitalize.enabled && !s.isEmpty() && !s.startsWith("/") && Character.isLowerCase(s.charAt(0))) s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        return s;
    }

    // ================================================================== tiny calculator (+ - * / % ^, parentheses, sqrt, pi)
    static final class Calc {
        private final String s;
        private int i;
        Calc(String s) { this.s = s.replace(" ", "").replace(',', '.').replace('x', '*').replace(':', '/').toLowerCase(Locale.ROOT); }
        double parse() { double v = expr(); if (i != s.length()) throw new IllegalArgumentException("Rest: " + s.substring(i)); return v; }
        private double expr() { double v = term(); while (i < s.length()) { char c = s.charAt(i); if (c == '+') { i++; v += term(); } else if (c == '-') { i++; v -= term(); } else break; } return v; }
        private double term() { double v = pow(); while (i < s.length()) { char c = s.charAt(i); if (c == '*') { i++; v *= pow(); } else if (c == '/') { i++; v /= pow(); } else if (c == '%') { i++; v %= pow(); } else break; } return v; }
        private double pow() { double v = unary(); if (i < s.length() && s.charAt(i) == '^') { i++; v = Math.pow(v, pow()); } return v; }
        private double unary() { if (i < s.length() && s.charAt(i) == '-') { i++; return -unary(); } if (i < s.length() && s.charAt(i) == '+') { i++; return unary(); } return atom(); }
        private double atom() {
            if (s.startsWith("sqrt", i) || s.startsWith("wurzel", i)) { i += s.startsWith("sqrt", i) ? 4 : 6; return Math.sqrt(atom()); }
            if (s.startsWith("pi", i)) { i += 2; return Math.PI; }
            if (i < s.length() && s.charAt(i) == '(') { i++; double v = expr(); if (i >= s.length() || s.charAt(i) != ')') throw new IllegalArgumentException(")"); i++; return v; }
            int st = i;
            while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) i++;
            if (st == i) throw new IllegalArgumentException("Zahl erwartet");
            return Double.parseDouble(s.substring(st, i));
        }
    }
}
