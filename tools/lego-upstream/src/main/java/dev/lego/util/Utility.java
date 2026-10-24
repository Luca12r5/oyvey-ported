package dev.lego.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Sound;
import dev.lego.ui.Toasts;
import dev.lego.visual.Overlays;
import dev.lego.visual.R3;
import dev.lego.visual.Visuals2;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_3965;
import net.minecraft.class_418;
import net.minecraft.class_5498;
import net.minecraft.class_746;

/** Legit quality-of-life utilities (no advantages that break server rules). */
public final class Utility {
    private Utility() { }

    public static SeeModule invsee, ecsee;
    public static ToggleSprint sprint;
    public static ToggleSneak sneak;
    public static Freelook freelook;
    public static AutoGG autoGG;
    public static AutoRespawn autoRespawn;
    public static Waypoints waypoints;
    public static DeathPoint deathPoint;
    public static HitSound hitSound;
    public static KillSound killSound;
    public static MentionPing mention;
    public static AntiSpam antiSpam;
    public static WordFilter wordFilter;
    public static Macros macros;
    public static ShareCoords shareCoords;
    public static FakeCheck fakeCheck;
    public static Auction auction;
    public static FightPreset fightPreset;
    public static HeadSearch headSearch;

    public static void registerAll() {
        invsee = Modules.register(new SeeModule("invsee", "InvSee", "Spieler ansehen + V: öffnet sein Inventar (/invsee) - braucht Server-Rechte",
                new String[]{"invsee", "openinv", "inv"}));
        ecsee = Modules.register(new SeeModule("ecsee", "EcSee", "Spieler ansehen + G: öffnet seine Enderkiste (/ecsee) - braucht Server-Rechte",
                new String[]{"ecsee", "enderchest", "ec", "echest", "endersee"}));
        sprint = Modules.register(new ToggleSprint());
        sneak = Modules.register(new ToggleSneak());
        freelook = Modules.register(new Freelook());
        autoGG = Modules.register(new AutoGG());
        autoRespawn = Modules.register(new AutoRespawn());
        waypoints = Modules.register(new Waypoints());
        deathPoint = Modules.register(new DeathPoint());
        hitSound = Modules.register(new HitSound());
        killSound = Modules.register(new KillSound());
        mention = Modules.register(new MentionPing());
        antiSpam = Modules.register(new AntiSpam());
        wordFilter = Modules.register(new WordFilter());
        macros = Modules.register(new Macros());
        shareCoords = Modules.register(new ShareCoords());
        Waypoints.registerPersist();
        fakeCheck = Modules.register(new FakeCheck());
        auction = Modules.register(new Auction());
        fightPreset = Modules.register(new FightPreset());
        headSearch = Modules.register(new HeadSearch());
    }

    // ================================================================== events
    public static void onHit(class_1297 target) {
        try {
            if (hitSound.enabled) hitSound.play();
            dev.lego.cosmetic.PetWorld.onHit();
            if (Overlays.hitmarker != null && Overlays.hitmarker.enabled) Overlays.hitmarker.onHit();
        } catch (Throwable t) { LegoClient.LOG("onHit: " + t); }
    }

    private static final Set<Integer> KILLED = new HashSet<>();

    public static void onKill(class_1297 e) {
        try {
            if (!KILLED.add(Mc.id(e))) return;          // only once per entity
            if (KILLED.size() > 200) KILLED.clear();
            if (killSound.enabled) killSound.play();
            dev.lego.cosmetic.PetWorld.onKill();
            if (Overlays.hitmarker != null && Overlays.hitmarker.enabled) Overlays.hitmarker.onKill();
            if (autoGG.enabled && e instanceof class_1657) autoGG.trigger();
        } catch (Throwable t) { LegoClient.LOG("onKill: " + t); }
    }

    /** ClientReceiveMessageEvents.ALLOW_CHAT / ALLOW_GAME - false hides the message. */
    public static boolean allowMessage(class_2561 msg, boolean overlay) {
        if (overlay || msg == null) return true;
        try {
            String s = msg.getString();
            if (wordFilter.enabled && wordFilter.blocks(s)) return false;
            if (antiSpam.enabled && antiSpam.blocks(s)) return false;
        } catch (Throwable t) { LegoClient.LOG("Chat-Filter: " + t); }
        return true;
    }

    /** ClientReceiveMessageEvents.CHAT / GAME. */
    public static void onMessage(class_2561 msg, boolean overlay) {
        if (overlay || msg == null) return;
        try {
            String s = msg.getString();
            if (mention.enabled) mention.check(s);
            if (auction != null) auction.onChat(s);
        } catch (Throwable t) { LegoClient.LOG("Chat: " + t); }
    }

    /** World render (inside R3.begin/end). */
    public static void renderWorld(float td) {
        if (waypoints != null) waypoints.render();
        if (headSearch != null && headSearch.enabled) headSearch.render();
    }

    /** Key presses while no screen is open (glfw key codes, edge-triggered by the caller). */
    public static void onKey(int key) {
        if (macros.enabled) macros.onKey(key);
    }

    static String dimension() {
        try { return Mc.world().method_27983().method_29177().toString(); } catch (Throwable t) { return "?"; }
    }

    static String dimName(String dim) {
        if (dim.endsWith("the_nether")) return "Nether";
        if (dim.endsWith("the_end")) return "End";
        if (dim.endsWith("overworld")) return "Oberwelt";
        return dim;
    }

    // ================================================================== InvSee / EcSee
    public static final class SeeModule extends Module {
        final Setting.Mode command;
        final Setting.Num range = add(new Setting.Num("Reichweite", 4, 128, 2, 64, " Blöcke"));

        SeeModule(String id, String name, String desc, String[] commands) {
            super(id, name, desc, Category.UTILITY);
            command = add(new Setting.Mode("Befehl", 0, commands));
            enabled = true;
            icon(id.equals("invsee") ? "backpack" : "box");
        }

        /** Called when the key is pressed. */
        public void use() {
            if (!enabled) return;
            class_1657 target = lookedAtPlayer(range.get());
            if (target == null) {
                Toasts.show("user", name, "Kein Spieler im Fadenkreuz", 0xFFFF4D5E, 1800);
                return;
            }
            Mc.command(command.get() + " " + Mc.name(target));
        }
    }

    /** The player closest to the crosshair ray (within its hitbox), up to maxRange blocks. */
    public static class_1657 lookedAtPlayer(double maxRange) {
        class_746 me = Mc.player();
        if (me == null) return null;
        class_243 eye = me.method_5836(1f), look = me.method_5828(1f);
        class_1657 best = null;
        double bestDist = Double.MAX_VALUE;
        for (class_1657 p : Mc.players()) {
            if (p == me || Mc.isSpectator(p)) continue;
            double cx = Mc.x(p) - eye.field_1352, cy = Mc.y(p) + Mc.height(p) / 2 - eye.field_1351, cz = Mc.z(p) - eye.field_1350;
            double along = cx * look.field_1352 + cy * look.field_1351 + cz * look.field_1350;
            if (along <= 0 || along > maxRange) continue;
            double px = cx - look.field_1352 * along, py = cy - look.field_1351 * along, pz = cz - look.field_1350 * along;
            double horiz = Math.sqrt(px * px + pz * pz);
            if (horiz <= Mc.width(p) / 2 + 0.3 && Math.abs(py) <= Mc.height(p) / 2 + 0.3 && along < bestDist) {
                bestDist = along;
                best = p;
            }
        }
        return best;
    }

    // ================================================================== movement
    public static final class ToggleSprint extends Module {
        final Setting.Bool hud = add(new Setting.Bool("Status in der Actionbar", false));
        ToggleSprint() { super("togglesprint", "Toggle Sprint", "Du sprintest automatisch, sobald du nach vorne läufst (wie Vanilla-Sprint gehalten)", Category.UTILITY); icon("sprint"); enabled = true; fresh(); }
        @Override public void tick() {
            class_746 p = Mc.player();
            if (p == null || Mc.screenOpen()) return;
            // vanilla "Sprint: Umschalten" already toggles - forcing the key would flicker it
            if (Boolean.TRUE.equals(Mc.options().method_42450().method_41753())) return;
            Mc.options().field_1867.method_23481(true);   // same as holding the sprint key - vanilla decides if sprinting is allowed
            if (hud.get() && p.method_5624()) Mc.actionBar("§7[§aSprint§7]");
        }
        @Override public void onDisable() { try { Mc.options().field_1867.method_23481(false); } catch (Throwable ignored) { } }
    }

    public static final class ToggleSneak extends Module {
        private Boolean saved;
        ToggleSneak() { super("togglesneak", "Toggle Sneak", "Schleichen-Taste einmal drücken = an, nochmal = aus", Category.UTILITY); icon("sneak"); fresh(); }
        @Override public void onEnable() {
            try { saved = Mc.options().method_42449().method_41753(); Mc.options().method_42449().method_41748(true); } catch (Throwable ignored) { }
        }
        @Override public void onDisable() {
            try { Mc.options().method_42449().method_41748(saved == null ? Boolean.FALSE : saved); } catch (Throwable ignored) { }
        }
    }

    /** Hold the key to look around your character without turning. Camera handled by CameraMixin / EntityMixin. */
    public static final class Freelook extends Module {
        final Setting.Mode keyMode = add(new Setting.Mode("Taste", 0, "Linke Alt", "Maus 4", "Maus 5", "Z", "X"));
        final Setting.Bool toggle = add(new Setting.Bool("Umschalten statt halten", false));
        final Setting.Bool front = add(new Setting.Bool("3. Person während Freelook", true));
        private boolean active, lastDown, toggled;
        private float yaw, pitch;
        private class_5498 savedPerspective;
        Freelook() { super("freelook", "Freelook", "Taste halten: Kamera frei um dich drehen, ohne dass du dich umdrehst", Category.UTILITY); icon("freelook"); fresh(); }

        private boolean keyDown() {
            switch (keyMode.index) {
                case 1: return Mc.mouseDown(3);
                case 2: return Mc.mouseDown(4);
                case 3: return Mc.keyDown(90);
                case 4: return Mc.keyDown(88);
                default: return Mc.keyDown(342);
            }
        }

        public boolean active() { return enabled && active; }
        public float yaw() { return yaw; }
        public float pitch() { return pitch; }

        /** From EntityMixin: mouse movement goes to the camera instead of the player. */
        public void look(double dx, double dy) {
            yaw += (float) dx * 0.15f;
            pitch = Math.max(-90, Math.min(90, pitch + (float) dy * 0.15f));
        }

        @Override public void tick() {
            class_746 p = Mc.player();
            if (p == null) return;
            boolean down = keyDown() && !Mc.screenOpen();
            boolean want;
            if (toggle.get()) { if (down && !lastDown) toggled = !toggled; want = toggled; }
            else want = down;
            lastDown = down;
            if (want && !active) {
                active = true;
                yaw = Mc.yaw(p);
                pitch = Mc.pitch(p);
                if (front.get()) { savedPerspective = Mc.perspective(); Mc.perspective(class_5498.field_26665); }
            } else if (!want && active) stop();
        }

        private void stop() {
            active = false;
            if (savedPerspective != null) { Mc.perspective(savedPerspective); savedPerspective = null; }
        }

        @Override public void onDisable() { toggled = false; if (active) stop(); }
    }

    // ================================================================== chat helpers
    public static final class AutoGG extends Module {
        final Setting.Text message = add(new Setting.Text("Nachricht", "gg", 100, "z.B. gg wp"));
        final Setting.Num delay = add(new Setting.Num("Verzögerung", 0, 5, 0.5, 1, " s"));
        final Setting.Num cooldown = add(new Setting.Num("Abklingzeit", 2, 60, 1, 10, " s"));
        private long last = 0, pendingAt = 0;
        AutoGG() { super("autogg", "Auto-GG", "Schreibt nach einem Spieler-Kill automatisch deine Nachricht", Category.UTILITY); icon("gg"); fresh(); }
        void trigger() {
            long now = System.currentTimeMillis();
            if (now - last < cooldown.get() * 1000) return;
            last = now;
            pendingAt = now + (long) (delay.get() * 1000);
        }
        @Override public void tick() {
            if (pendingAt != 0 && System.currentTimeMillis() >= pendingAt) {
                pendingAt = 0;
                String m = message.get().trim();
                if (!m.isEmpty() && Mc.mc().method_1562() != null) Mc.mc().method_1562().method_45729(m);
            }
        }
    }

    public static final class AutoRespawn extends Module {
        final Setting.Num delay = add(new Setting.Num("Verzögerung", 0, 3, 0.25, 0.5, " s"));
        private long seen = 0;
        AutoRespawn() { super("autorespawn", "Auto-Respawn", "Respawnt dich automatisch nach dem Tod", Category.UTILITY); icon("respawn"); fresh(); }
        @Override public void tick() {
            class_746 p = Mc.player();
            if (p == null) return;
            if (Mc.mc().field_1755 instanceof class_418 || Mc.isDead(p)) {
                if (seen == 0) seen = System.currentTimeMillis();
                if (System.currentTimeMillis() - seen >= delay.get() * 1000) {
                    p.method_7331();
                    if (Mc.mc().field_1755 instanceof class_418) Mc.mc().method_1507(null);
                    seen = 0;
                }
            } else seen = 0;
        }
    }

    // ================================================================== waypoints
    public static final class Waypoint {
        public String name, dim;
        public double x, y, z;
        public int color;
        public boolean death;
        Waypoint(String name, String dim, double x, double y, double z, int color, boolean death) {
            this.name = name; this.dim = dim; this.x = x; this.y = y; this.z = z; this.color = color; this.death = death;
        }
    }

    public static final List<Waypoint> POINTS = new ArrayList<>();
    private static final int[] WP_COLORS = {0xFF1ED760, 0xFF00A3DA, 0xFFFFCD03, 0xFFFF6FB5, 0xFF9B4DCA, 0xFFFF8A00, 0xFF7CF7FF};

    public static final class Waypoints extends Module {
        final Setting.Bool beams = add(new Setting.Bool("Lichtstrahl", true));
        final Setting.Bool labels = add(new Setting.Bool("Name + Entfernung", true));
        final Setting.Num maxDist = add(new Setting.Num("Max. Entfernung", 100, 10000, 100, 3000, " Blöcke"));
        Waypoints() {
            super("waypoints", "Wegpunkte", "Markiere Orte mit Lichtstrahl und Entfernung - nur für dich sichtbar", Category.UTILITY);
            icon("waypoint"); enabled = true; fresh();
            add(new Setting.Action("Hier setzen", "Wegpunkt setzen", Waypoints::addHere));
            add(new Setting.Action("Letzten löschen", "Löschen", () -> {
                for (int i = POINTS.size() - 1; i >= 0; i--) if (POINTS.get(i).dim.equals(dimension())) {
                    Toasts.show("trash", "Wegpunkt gelöscht", POINTS.remove(i).name);
                    Modules.scheduleSave();
                    return;
                }
                Toasts.show("waypoint", "Wegpunkte", "Keine Wegpunkte in dieser Dimension");
            }));
            add(new Setting.Action("Alle löschen", "Alle löschen", () -> {
                POINTS.removeIf(w -> w.dim.equals(dimension()));
                Modules.scheduleSave();
                Toasts.show("trash", "Wegpunkte", "Alle Wegpunkte dieser Dimension gelöscht");
            }));
        }

        public static void addHere() {
            class_746 p = Mc.player();
            if (p == null) { Toasts.show("waypoint", "Wegpunkte", "Nur in einer Welt möglich"); return; }
            int n = 1;
            for (Waypoint w : POINTS) if (!w.death) n++;
            String name = "Punkt " + n;
            POINTS.add(new Waypoint(name, dimension(), Math.floor(Mc.x(p)) + 0.5, Math.floor(Mc.y(p)), Math.floor(Mc.z(p)) + 0.5, WP_COLORS[(n - 1) % WP_COLORS.length], false));
            Modules.scheduleSave();
            Toasts.show("waypoint", "Wegpunkt gesetzt", name + " · " + (int) Math.floor(Mc.x(p)) + " " + (int) Math.floor(Mc.y(p)) + " " + (int) Math.floor(Mc.z(p)));
        }

        void render() {
            class_746 p = Mc.player();
            if (p == null) return;
            String dim = dimension();
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            double t = System.currentTimeMillis() / 1000.0;
            for (Waypoint w : new ArrayList<>(POINTS)) {
                if (!w.dim.equals(dim)) continue;
                if (w.death ? !deathPoint.enabled : !enabled) continue;
                double dx = w.x - cx, dy = w.y + 1 - cy, dz = w.z - cz;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > maxDist.get()) continue;
                int rgb = w.color & 0xFFFFFF;
                // draw far points on a sphere of radius 96 around the camera so they stay visible
                double k = dist > 96 ? 96 / dist : 1;
                double x = cx + dx * k, z = cz + dz * k, baseY = cy + (w.y - cy) * k;
                if (beams.get()) {
                    double bw = 0.18 * k * Math.max(1, dist / 24);
                    double pulse = 0.35 + 0.1 * Math.sin(t * 2 + w.x);
                    R3.box(x - bw, baseY, z - bw, x + bw, baseY + 220 * k, z + bw, R3.argb(rgb, pulse));
                    R3.box(x - bw * 0.35, baseY, z - bw * 0.35, x + bw * 0.35, baseY + 220 * k, z + bw * 0.35, R3.argb(0xFFFFFF, 0.5));
                    R3.ring(x, baseY + 0.03 * k, z, 0.3 * k, 0.9 * k * Math.max(1, dist / 24), 32, R3.argb(rgb, 0.6), R3.argb(rgb, 0), t);
                }
                if (labels.get()) {
                    long dd = dist < 50 ? Math.round(dist) : dist < 500 ? Math.round(dist / 5) * 5 : Math.round(dist / 25) * 25;
                    String s = w.name + "  " + dd + "m";
                    class_2960 tex = Visuals2.TextTex.get(s, 0xFF000000 | rgb);
                    double h = Math.max(0.35, dist * k * 0.045);
                    R3.billboard(tex, x, baseY + 2.4 * k + h, z, h * Visuals2.TextTex.aspect(s, 0xFF000000 | rgb), h, 0, 0, 1, 1, 0xFFFFFFFF);
                }
            }
        }

        static void registerPersist() {
            Modules.persist("waypoints", new Modules.Persist() {
                @Override public Object save() {
                    List<Object> out = new ArrayList<>();
                    for (Waypoint w : POINTS) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", w.name); m.put("dim", w.dim); m.put("x", w.x); m.put("y", w.y); m.put("z", w.z);
                        m.put("color", (long) w.color); m.put("death", w.death);
                        m.put("server", Mc.inGame() ? Mc.serverAddress() : "");
                        out.add(m);
                    }
                    return out;
                }
                @SuppressWarnings("unchecked")
                @Override public void load(Object o) {
                    if (!(o instanceof List)) return;
                    POINTS.clear();
                    for (Object e : (List<Object>) o) {
                        if (!(e instanceof Map)) continue;
                        Map<String, Object> m = (Map<String, Object>) e;
                        try {
                            POINTS.add(new Waypoint(String.valueOf(m.get("name")), String.valueOf(m.get("dim")),
                                    ((Number) m.get("x")).doubleValue(), ((Number) m.get("y")).doubleValue(), ((Number) m.get("z")).doubleValue(),
                                    ((Number) m.get("color")).intValue(), Boolean.TRUE.equals(m.get("death"))));
                        } catch (Throwable ignored) { }
                    }
                }
            });
        }
    }

    public static final class DeathPoint extends Module {
        final Setting.Bool chat = add(new Setting.Bool("Koordinaten in den Chat (nur für dich)", true));
        private boolean wasDead;
        DeathPoint() { super("deathpoint", "Todespunkt", "Merkt sich, wo du gestorben bist (roter Wegpunkt)", Category.UTILITY); icon("skull"); enabled = true; fresh(); }
        @Override public void tick() {
            class_746 p = Mc.player();
            if (p == null) return;
            boolean dead = Mc.isDead(p);
            if (dead && !wasDead) {
                POINTS.removeIf(w -> w.death);
                double x = Math.floor(Mc.x(p)) + 0.5, y = Math.floor(Mc.y(p)), z = Math.floor(Mc.z(p)) + 0.5;
                POINTS.add(new Waypoint("Tod", dimension(), x, y, z, 0xFFFF3B4C, true));
                Modules.scheduleSave();
                String c = (int) x + " " + (int) y + " " + (int) z;
                Toasts.show("skull", "Todespunkt gespeichert", c + " · " + dimName(dimension()), 0xFFFF4D5E, 5000);
                if (chat.get()) Mc.chat("§c☠ §7Todespunkt: §f" + c + " §8(" + dimName(dimension()) + ")");
            }
            wasDead = dead;
        }
    }


    // ================================================================== sounds
    static final String[] SOUND_NAMES = {"Pling", "Glocke", "Klick", "XP", "Amethyst", "Bass"};
    static final String[] SOUND_IDS = {"minecraft:block.note_block.pling", "minecraft:block.note_block.bell", "minecraft:block.note_block.hat",
            "minecraft:entity.experience_orb.pickup", "minecraft:block.amethyst_block.chime", "minecraft:block.note_block.bass"};

    public static final class HitSound extends Module {
        final Setting.Mode sound = add(new Setting.Mode("Ton", 2, SOUND_NAMES));
        final Setting.Num volume = add(new Setting.Num("Lautstärke", 10, 100, 5, 50, "%"));
        final Setting.Num pitch = add(new Setting.Num("Tonhöhe", 0.5, 2, 0.1, 1.4, ""));
        HitSound() { super("hitsound", "Hit-Sound", "Spielt einen Ton, wenn du triffst", Category.UTILITY); icon("sound"); fresh(); }
        void play() { Sound.play(SOUND_IDS[sound.index], pitch.getF(), volume.getF() / 100f); }
    }

    public static final class KillSound extends Module {
        final Setting.Mode sound = add(new Setting.Mode("Ton", 3, SOUND_NAMES));
        final Setting.Num volume = add(new Setting.Num("Lautstärke", 10, 100, 5, 70, "%"));
        KillSound() { super("killsound", "Kill-Sound", "Spielt einen Ton, wenn du jemanden besiegst", Category.UTILITY); icon("kill"); fresh(); }
        void play() { Sound.play(SOUND_IDS[sound.index], 1.0f, volume.getF() / 100f); Sound.play(SOUND_IDS[sound.index], 1.5f, volume.getF() / 140f); }
    }

    public static final class MentionPing extends Module {
        final Setting.Text extra = add(new Setting.Text("Weitere Wörter", "", 120, "Komma-getrennt, z.B. Clan,Spitzname"));
        final Setting.Bool toast = add(new Setting.Bool("Hinweis anzeigen", true));
        private long last = 0;
        MentionPing() { super("mention", "Erwähnungs-Ping", "Ton + Hinweis bei @Name im Chat", Category.UTILITY); icon("bell"); enabled = true; fresh(); }
        void check(String s) {
            class_746 p = Mc.player();
            if (p == null) return;
            String me = Mc.name(p);
            String low = s.toLowerCase(Locale.ROOT);
            // ignore own messages ("<Name> ..." or "Name: ...")
            if (low.startsWith("<" + me.toLowerCase(Locale.ROOT) + ">") || low.startsWith(me.toLowerCase(Locale.ROOT) + ":")) return;
            boolean hit = low.contains("@" + me.toLowerCase(Locale.ROOT));
            for (String w : extra.get().split(",")) { w = w.trim().toLowerCase(Locale.ROOT); if (w.length() >= 2 && low.contains("@" + w)) hit = true; }
            if (!hit || System.currentTimeMillis() - last < 1500) return;
            last = System.currentTimeMillis();
            Sound.play("minecraft:block.note_block.pling", 1.8f, 0.8f);
            if (toast.get()) Toasts.show("chat", "Du wurdest erwähnt", s.length() > 80 ? s.substring(0, 80) + "…" : s);
        }
    }

    public static final class AntiSpam extends Module {
        final Setting.Num window = add(new Setting.Num("Zeitfenster", 2, 60, 1, 10, " s"));
        private final ArrayDeque<Object[]> recent = new ArrayDeque<>();
        private int hidden = 0;
        AntiSpam() { super("antispam", "Anti-Spam", "Blendet gleiche Nachrichten aus, die kurz hintereinander kommen", Category.UTILITY); icon("filter"); fresh(); }
        boolean blocks(String s) {
            long now = System.currentTimeMillis();
            while (!recent.isEmpty() && now - (Long) recent.peekFirst()[1] > window.get() * 1000) recent.pollFirst();
            String k = s.trim().toLowerCase(Locale.ROOT);
            if (k.isEmpty()) return false;
            for (Object[] r : recent) if (r[0].equals(k)) { hidden++; return true; }
            recent.add(new Object[]{k, now});
            if (recent.size() > 100) recent.pollFirst();
            return false;
        }
    }

    public static final class WordFilter extends Module {
        final Setting.Text words = add(new Setting.Text("Wörter", "", 200, "Komma-getrennt"));
        WordFilter() { super("wordfilter", "Wort-Filter", "Versteckt Chat-Nachrichten mit bestimmten Wörtern", Category.UTILITY); icon("filter"); fresh(); }
        boolean blocks(String s) {
            String low = s.toLowerCase(Locale.ROOT);
            for (String w : words.get().split(",")) { w = w.trim().toLowerCase(Locale.ROOT); if (w.length() >= 2 && low.contains(w)) return true; }
            return false;
        }
    }

    // ================================================================== macros / sharing
    static final String[] KEY_NAMES = {"Aus", "F6", "F7", "F8", "F9", "Num 1", "Num 2", "Num 3", "Num 4", "Num 5", "Num 6", "Num 7", "Num 8", "Num 9"};
    static final int[] KEY_CODES = {-1, 295, 296, 297, 298, 321, 322, 323, 324, 325, 326, 327, 328, 329};

    public static final class Macros extends Module {
        final Setting.Text[] text = new Setting.Text[4];
        final Setting.Mode[] key = new Setting.Mode[4];
        private long last = 0;
        Macros() {
            super("macros", "Auto Text", "Taste drücken = Nachricht oder /Befehl senden (z.B. /home, /spawn)", Category.UTILITY);
            icon("macro"); fresh();
            String[] defs = {"/spawn", "/home", "", ""};
            for (int i = 0; i < 4; i++) {
                text[i] = add(new Setting.Text("Auto Text " + (i + 1), defs[i], 120, "Text oder /befehl"));
                key[i] = add(new Setting.Mode("Taste " + (i + 1), i < 2 ? i + 1 : 0, KEY_NAMES));
            }
        }
        void onKey(int k) {
            if (System.currentTimeMillis() - last < 400) return;
            for (int i = 0; i < 4; i++) {
                if (KEY_CODES[key[i].index] != k) continue;
                String s = text[i].get().trim();
                if (s.isEmpty() || Mc.mc().method_1562() == null) continue;
                last = System.currentTimeMillis();
                if (s.startsWith("/")) Mc.command(s.substring(1)); else Mc.mc().method_1562().method_45729(s);
                Toasts.show("macro", "Auto Text " + (i + 1), s, 0, 1500);
            }
        }
    }

    public static final class ShareCoords extends Module {
        final Setting.Mode target = add(new Setting.Mode("Ziel", 0, "Zwischenablage", "Chat senden"));
        ShareCoords() {
            super("sharecoords", "Koordinaten teilen", "Taste belegen: kopiert deine Koordinaten oder sendet sie in den Chat", Category.UTILITY);
            icon("copy"); fresh();
            add(new Setting.Action("Jetzt", "Koordinaten teilen", this::share));
        }
        @Override public boolean isToggleable() { return false; }
        public void share() {
            class_746 p = Mc.player();
            if (p == null) { Toasts.show("copy", name, "Nur in einer Welt möglich"); return; }
            String s = "X: " + (int) Math.floor(Mc.x(p)) + " Y: " + (int) Math.floor(Mc.y(p)) + " Z: " + (int) Math.floor(Mc.z(p)) + " (" + dimName(dimension()) + ")";
            if (target.index == 1 && Mc.mc().method_1562() != null) Mc.mc().method_1562().method_45729(s);
            else { Mc.clipboard(s); Toasts.show("copy", "Koordinaten kopiert", s); }
        }
    }


    // ================================================================== head search
    public static final class HeadSearch extends Module {
        final Setting.Mode mode = add(new Setting.Mode("Modus", 0, "Profil", "FindEventHead"));
        final Setting.Color color = add(new Setting.Color("ESP-Farbe", 0));
        final Setting.Num range = add(new Setting.Num("Reichweite", 16, 256, 8, 128, " Blöcke"));
        private final Set<Long> collected = java.util.concurrent.ConcurrentHashMap.newKeySet();
        private boolean lastUse;
        private long lastCount;

        HeadSearch() {
            super("headsearch", "Head Search", "Zeigt Spieler-Köpfe mit ESP durch Wände", Category.UTILITY);
            icon("search"); fresh();
            add(new Setting.Action("Gesammelte zurücksetzen", "Reset", () -> {
                collected.clear();
                Toasts.show("search", "Head Search", "Alle Köpfe wieder sichtbar");
            }));
        }

        @Override public void onEnable() {
            lastCount = System.currentTimeMillis();
        }

        @Override public void tick() {
            if (!enabled) return;
            class_746 p = Mc.player();
            if (p == null || Mc.screenOpen()) { lastUse = false; return; }
            boolean use = Mc.mouseDown(1);
            if (use && !lastUse) {
                class_239 hit = Mc.mc().field_1765;
                if (hit instanceof class_3965 && hit.method_17783() == class_239.class_240.field_1332) {
                    class_2338 pos = ((class_3965) hit).method_17777();
                    long packed = pos.method_10063();
                    if (!collected.contains(packed)) {
                        collected.add(packed);
                        Toasts.show("search", "Head Search", "Eingesammelt (" + collected.size() + ")", 0xFF00A3DA, 1500);
                    }
                }
            }
            lastUse = use;
        }

        private boolean matchesFilter(net.minecraft.class_2631 skull) {
            try {
                net.minecraft.class_9296 owner = skull.method_11334();
                if (owner == null) return false;
                if ("Profil".equals(mode.get())) return true;
                java.util.Optional<String> name = owner.method_73317();
                if (name.isPresent() && name.get().equals("FindEventHead")) return true;
                com.mojang.authlib.GameProfile gp = owner.method_73313();
                if (gp != null && "FindEventHead".equals(gp.name())) return true;
                return false;
            } catch (Throwable e) { return false; }
        }

        void render() {
            class_746 p = Mc.player();
            if (p == null) return;
            double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
            double maxR = range.get();
            int rgb = color.argb() & 0xFFFFFF;
            double t = System.currentTimeMillis() / 1000.0;
            double pulse = 0.6 + 0.2 * Math.sin(t * 3);
            int pcx = (int) Math.floor(Mc.x(p)) >> 4;
            int pcz = (int) Math.floor(Mc.z(p)) >> 4;
            int cr = (int) Math.ceil(maxR / 16);
            int found = 0;
            try {
                for (int ddx = -cr; ddx <= cr; ddx++) {
                    for (int ddz = -cr; ddz <= cr; ddz++) {
                        net.minecraft.class_2818 chunk;
                        try { chunk = Mc.world().method_8497(pcx + ddx, pcz + ddz); } catch (Throwable e) { continue; }
                        if (chunk == null) continue;
                        Map<class_2338, class_2586> bes;
                        try { bes = chunk.method_12214(); } catch (Throwable e) { continue; }
                        if (bes == null || bes.isEmpty()) continue;
                        for (Map.Entry<class_2338, class_2586> entry : bes.entrySet()) {
                            if (!(entry.getValue() instanceof net.minecraft.class_2631 skull)) continue;
                            if (!matchesFilter(skull)) continue;
                            class_2338 pos = entry.getKey();
                            if (collected.contains(pos.method_10063())) continue;
                            double bx = pos.method_10263() + 0.5 - cx, by = pos.method_10264() + 0.5 - cy, bz = pos.method_10260() + 0.5 - cz;
                            if (bx * bx + by * by + bz * bz > maxR * maxR) continue;
                            found++;
                            double x0 = pos.method_10263(), y0 = pos.method_10264(), z0 = pos.method_10260();
                            R3.noDepthBox(x0 - 0.01, y0 - 0.01, z0 - 0.01, x0 + 1.01, y0 + 1.01, z0 + 1.01, R3.argb(rgb, 0.3 * pulse));
                            R3.noDepthBoxEdges(x0 - 0.01, y0 - 0.01, z0 - 0.01, x0 + 1.01, y0 + 1.01, z0 + 1.01, 0.012, R3.argb(rgb, 0.95));
                        }
                    }
                }
            } catch (Throwable ignored) { }
            if (System.currentTimeMillis() - lastCount > 5000) {
                lastCount = System.currentTimeMillis();
                Mc.actionBar("§7Head Search: §a" + found + " §7Köpfe gefunden");
            }
        }
    }

    /** Non-toggle modules: key press triggers the action. */
    public static boolean runAction(Module m) {
        if (m == shareCoords) { shareCoords.share(); return true; }
        return false;
    }

}
