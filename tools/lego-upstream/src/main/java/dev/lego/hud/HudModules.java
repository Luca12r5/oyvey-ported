package dev.lego.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.UiSettings;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_746;

/**
 * All HUD modules (clean style). Rule: key() runs on the render thread and snapshots the game state into
 * fields; paint() runs on the painter thread and only uses that snapshot.
 */
public final class HudModules {
    private HudModules() { }

    public static void registerAll() {
        Modules.register(new InfoBar());
        Modules.register(new Fps());
        Modules.register(new Ping());
        Modules.register(new Cps());
        Modules.register(new Coords());
        Modules.register(new Direction());
        Modules.register(new Clock());
        Modules.register(new GameTime());
        Modules.register(new Session());
        Modules.register(new Speed());
        Modules.register(new Memory());
        Modules.register(new Server());
        Modules.register(new Armor());
        Modules.register(new Effects());
        Modules.register(new Keystrokes());
        Modules.register(new Totems());
        Modules.register(new PvpItems());
        Modules.register(new Day());
        Modules.register(new Biome());
        Modules.register(new Light());
        Modules.register(new TargetHud());
        Modules.register(new Hunger());
        Modules.register(new Health());
        Modules.register(new Combo());
        Modules.register(new InventoryHud());
    }

    /** "value unit" panel. */
    abstract static class Simple extends HudModule {
        private final String icon;
        protected volatile String value = "", unit = "";
        Simple(String id, String name, String desc, String icon, double fx, double fy) {
            super(id, name, desc, fx, fy);
            this.icon = icon;
            icon(icon);
        }
        /** Sets value + unit. */
        abstract void compute();
        @Override public double w() { return simpleWidth(value, unit); }
        @Override public double h() { return PH; }
        @Override public String key() { compute(); return value + "|" + unit + "|" + icons(); }
        @Override public void paint(Graphics2D g, Color accent) { simplePaint(g, icon, value, unit, w(), accent); }
    }

    // ------------------------------------------------------------------ info bar (Essential style)
    static final class InfoBar extends HudModule {
        private final Setting.Bool fps = add(new Setting.Bool("FPS", true));
        private final Setting.Bool ping = add(new Setting.Bool("Ping", true));
        private final Setting.Bool cps = add(new Setting.Bool("CPS", false));
        private final Setting.Bool clock = add(new Setting.Bool("Uhrzeit", false));
        private final Setting.Bool session = add(new Setting.Bool("Spielzeit", true));
        private final Setting.Bool coords = add(new Setting.Bool("Koordinaten", false));
        private final Setting.Bool logo = add(new Setting.Bool("Lego-Logo", true));
        private volatile List<String> parts = new ArrayList<>();
        InfoBar() { super("infobar", "Info-Leiste", "Lego | FPS | Ping | Spielzeit – alles in einer sauberen Zeile", 0.006, 0.008); enabled = true; icon("list"); fresh(); }
        @Override public double w() {
            double w = 8;
            List<String> p = parts;
            if (logo.get()) w += H.w("Lego", H.BOLD, VAL) + 12;
            for (int i = 0; i < p.size(); i++) w += H.w(p.get(i), H.MED, 7.8f) + (i < p.size() - 1 ? 13 : 0);
            return Math.ceil((w + 8) / 4.0) * 4.0;
        }
        @Override public double h() { return 17; }
        @Override public String key() {
            List<String> p = new ArrayList<>();
            if (fps.get()) p.add(Mc.fps() + " FPS");
            if (ping.get()) { int pg = Mc.ping(); p.add(pg < 0 ? "- ms" : pg + "ms"); }
            if (cps.get()) p.add(ClickCounter.left() + " CPS");
            if (clock.get()) p.add(LocalTime.now().toString().substring(0, 5));
            if (session.get()) p.add(sessionTime());
            if (coords.get()) {
                class_746 pl = Mc.player();
                if (pl != null) p.add((int) Math.floor(Mc.x(pl)) + " " + (int) Math.floor(Mc.y(pl)) + " " + (int) Math.floor(Mc.z(pl)));
            }
            parts = p;
            return String.join("|", p) + logo.get();
        }
        @Override public void paint(Graphics2D g, Color accent) {
            double x = 8, base = H.base(0, 17, 7.8f);
            if (logo.get()) {
                // "Lego" in accent gradient
                g.setPaint(new java.awt.GradientPaint((float) x, 0, accent, (float) (x + 22), 0, H.mix(accent, Color.WHITE, 0.45)));
                g.setFont(dev.lego.ui.Fonts.get(H.BOLD, VAL));
                g.drawString("Lego", (float) x, (float) H.base(0, 17, VAL));
                x += H.w("Lego", H.BOLD, VAL) + 6;
                sep(g, x, accent);
                x += 6;
            }
            List<String> p = parts;
            for (int i = 0; i < p.size(); i++) {
                H.text(g, p.get(i), x, base, H.MED, 7.8f, H.TXT);
                x += H.w(p.get(i), H.MED, 7.8f);
                if (i < p.size() - 1) { x += 6.5; sep(g, x, accent); x += 6.5; }
            }
        }
        private static void sep(Graphics2D g, double x, Color accent) {
            g.setColor(new Color(255, 255, 255, 60));
            g.fill(new RoundRectangle2D.Double(x - 0.5, 4.5, 1, 8, 1, 1));
        }
    }

    static String sessionTime() {
        long s = LegoClient.sessionSeconds();
        return s >= 3600 ? String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60) : String.format("%02d:%02d", s / 60, s % 60);
    }

    // ------------------------------------------------------------------ simple panels
    static final class Fps extends Simple {
        Fps() { super("fps", "FPS", "Bilder pro Sekunde", "bolt", 0.006, 0.07); }
        @Override void compute() { value = String.valueOf(Mc.fps()); unit = "FPS"; }
    }

    static final class Ping extends Simple {
        Ping() { super("ping", "Ping", "Verbindung zum Server in Millisekunden", "wifi", 0.006, 0.115); }
        @Override void compute() { int p = Mc.ping(); value = p < 0 ? "-" : String.valueOf(p); unit = "ms"; }
    }

    static final class Cps extends Simple {
        Cps() { super("cps", "CPS", "Klicks pro Sekunde (links | rechts)", "mouse", 0.006, 0.16); }
        @Override void compute() { value = ClickCounter.left() + " | " + ClickCounter.right(); unit = "CPS"; }
    }

    static final class Clock extends Simple {
        private final Setting.Bool seconds = add(new Setting.Bool("Sekunden", false));
        private final Setting.Bool h12 = add(new Setting.Bool("12-Stunden (AM/PM)", false));
        Clock() { super("clock", "Uhrzeit", "Echte Uhrzeit", "clock", 0.006, 0.205); }
        @Override void compute() {
            LocalTime t = LocalTime.now();
            int h = t.getHour();
            String ampm = "";
            if (h12.get()) { ampm = h < 12 ? "AM" : "PM"; h = h % 12 == 0 ? 12 : h % 12; }
            value = seconds.get() ? String.format("%02d:%02d:%02d", h, t.getMinute(), t.getSecond()) : String.format("%02d:%02d", h, t.getMinute());
            unit = ampm;
        }
    }

    static final class GameTime extends Simple {
        GameTime() { super("gametime", "Minecraft-Zeit", "Uhrzeit in der Minecraft-Welt (Tag/Nacht)", "sun", 0.006, 0.25); }
        @Override void compute() {
            long t = (Mc.timeOfDay() + 6000) % 24000;
            int h = (int) (t / 1000), m = (int) ((t % 1000) * 60 / 1000);
            value = String.format("%02d:%02d", h, m);
            long d = Mc.timeOfDay() % 24000;
            unit = d >= 13000 && d < 23000 ? "Nacht" : "Tag";
        }
    }

    static final class Session extends Simple {
        Session() { super("session", "Spielzeit", "Wie lange du schon auf dem Server bist", "hourglass", 0.006, 0.295); }
        @Override void compute() { value = sessionTime(); unit = ""; }
    }

    static final class Speed extends Simple {
        Speed() { super("speed", "Geschwindigkeit", "Blöcke pro Sekunde", "speed", 0.006, 0.34); }
        @Override void compute() {
            class_746 p = Mc.player();
            if (p == null) { value = "-"; unit = ""; return; }
            double dx = Mc.x(p) - Mc.lastX(p), dz = Mc.z(p) - Mc.lastZ(p);
            value = String.format(Locale.ROOT, "%.1f", Math.sqrt(dx * dx + dz * dz) * 20);
            unit = "b/s";
        }
    }

    static final class Memory extends Simple {
        Memory() { super("memory", "Arbeitsspeicher", "RAM-Verbrauch von Minecraft", "chip", 0.006, 0.385); }
        @Override void compute() {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) >> 20, max = r.maxMemory() >> 20;
            value = (used * 100 / Math.max(1, max)) + "%";
            unit = (used / 10 * 10) + "/" + max + " MB";
        }
    }

    static final class Server extends Simple {
        Server() { super("server", "Server-IP", "Adresse des Servers", "globe", 0.006, 0.43); }
        @Override void compute() { value = Mc.serverAddress(); unit = ""; }
    }

    static final class Day extends Simple {
        Day() { super("day", "Tag-Zähler", "Wie viele Minecraft-Tage vergangen sind", "sun", 0.006, 0.475); }
        @Override void compute() { value = "Tag " + (Mc.timeOfDay() / 24000L + 1); unit = ""; }
    }

    static final class Biome extends Simple {
        Biome() { super("biome", "Biom", "Aktuelles Biom", "leaf", 0.006, 0.52); }
        @Override void compute() {
            String b = Mc.biome().replace('_', ' ');
            value = b.isEmpty() ? "-" : Character.toUpperCase(b.charAt(0)) + b.substring(1);
            unit = "";
        }
    }

    static final class Light extends Simple {
        Light() { super("light", "Lichtlevel", "Blocklicht an deinen Füßen (unter 1 können Monster spawnen)", "sun", 0.006, 0.565); fresh(); }
        @Override void compute() {
            int l = Mc.blockLight();
            value = l < 0 ? "-" : String.valueOf(l);
            unit = l == 0 ? "Monster!" : "Licht";
        }
    }

    static final class Combo extends Simple {
        Combo() { super("combo", "Combo & Reichweite", "Treffer in Folge und Abstand beim letzten Schlag", "target", 0.006, 0.61); }
        @Override void compute() {
            value = CombatTracker.combo() + "x";
            unit = String.format(Locale.ROOT, "%.2f m", CombatTracker.lastReach());
        }
    }

    // ------------------------------------------------------------------ coordinates
    static final class Coords extends HudModule {
        private final Setting.Bool nether = add(new Setting.Bool("Nether-Umrechnung", true));
        private final Setting.Bool facing = add(new Setting.Bool("Richtung", true));
        private volatile String l1 = "", l2 = "", dir = "";
        Coords() { super("coords", "Koordinaten", "X Y Z, Richtung und Nether-Umrechnung", 0.006, 0.66); enabled = true; icon("pin"); }
        @Override public double w() {
            double a = H.w(l1, H.BOLD, 8f) + (facing.get() ? H.w(dir, H.MED, 7f) + 8 : 0);
            double b = nether.get() ? H.w(l2, H.REG, 6.8f) : 0;
            return Math.ceil((Math.max(a, b) + (icons() ? 13 : 0) + 16) / 4) * 4;
        }
        @Override public double h() { return nether.get() ? 27 : 16; }
        @Override public String key() {
            class_746 p = Mc.player();
            if (p == null) { l1 = "-"; l2 = ""; return "none"; }
            int x = (int) Math.floor(Mc.x(p)), y = (int) Math.floor(Mc.y(p)), z = (int) Math.floor(Mc.z(p));
            l1 = x + "  " + y + "  " + z;
            boolean inNether = false;
            try { inNether = Mc.world().method_27983().method_29177().method_12832().contains("nether"); } catch (Throwable ignored) { }
            l2 = inNether ? "Oberwelt  " + (x * 8) + "  " + (z * 8) : "Nether  " + Math.floorDiv(x, 8) + "  " + Math.floorDiv(z, 8);
            float yaw = ((Mc.yaw(p) % 360) + 360) % 360;
            String[] d = {"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
            dir = d[Math.round(yaw / 45f) % 8];
            return l1 + "|" + (nether.get() ? l2 : "") + "|" + (facing.get() ? dir : "") + icons();
        }
        @Override public void paint(Graphics2D g, Color accent) {
            double x = 7;
            if (icons()) { H.icon(g, "pin", x + 4.5, 8, 9, accent); x += 13; }
            H.text(g, l1, x, H.base(0, 16, 8f), H.BOLD, 8f, H.TXT);
            if (facing.get()) H.text(g, dir, x + H.w(l1, H.BOLD, 8f) + 7, H.base(0, 16, 8f), H.MED, 7f, accent);
            if (nether.get()) H.text(g, l2, x, 22.5, H.REG, 6.8f, H.SUB);
        }
    }

    // ------------------------------------------------------------------ compass
    static final class Direction extends HudModule {
        private volatile float yaw;
        Direction() { super("direction", "Kompass", "Himmelsrichtung als Leiste oben", 0.5, 0.006); icon("compass"); }
        @Override public double w() { return 150; }
        @Override public double h() { return 17; }
        @Override public String key() { class_746 p = Mc.player(); yaw = p == null ? 0 : Mc.yaw(p); return String.valueOf(Math.round(yaw * 2)); }
        @Override public void paint(Graphics2D g, Color accent) {
            double w = w();
            double yw = ((yaw % 360) + 360) % 360;      // 0 = south in Minecraft
            java.awt.Shape old = g.getClip();
            g.clip(new RoundRectangle2D.Double(4, 0, w - 8, 17, 8, 8));
            String[] dirs = {"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
            for (int i = -16; i <= 16; i++) {
                double deg = Math.floor(yw / 15) * 15 + i * 15;
                double x = w / 2 + (deg - yw) * 1.25;
                double fade = Math.max(0, 1 - Math.abs(x - w / 2) / (w / 2));
                int d = (int) Math.round(((deg % 360) + 360) % 360);
                if (d % 45 == 0) {
                    String s = dirs[(d / 45) % 8];
                    boolean main = s.length() == 1;
                    H.center(g, s, x, 11.6, main ? H.BOLD : H.MED, main ? 8f : 6.6f, H.alpha(main ? H.TXT : H.SUB, (int) (255 * fade)));
                } else {
                    g.setColor(new Color(255, 255, 255, (int) (80 * fade)));
                    g.fill(new java.awt.geom.Rectangle2D.Double(x - 0.35, 6, 0.7, 5));
                }
            }
            g.setClip(old);
            g.setColor(accent);
            g.fill(new RoundRectangle2D.Double(w / 2 - 3, 14, 6, 1.8, 1.8, 1.8));
        }
    }

    // ------------------------------------------------------------------ armor
    static final class Armor extends HudModule {
        private final Setting.Bool hand = add(new Setting.Bool("Hauptitem zeigen", true));
        private final Setting.Mode layout = add(new Setting.Mode("Anordnung", 0, "Senkrecht", "Waagerecht"));
        private volatile String[] pct = new String[5];
        private volatile int[] pctVal = new int[5];
        private final class_1799[] stacks = new class_1799[5];
        Armor() { super("armor", "Rüstung", "Rüstung mit Haltbarkeit in Prozent", 0.006, 0.72); enabled = true; icon("armor"); }
        private boolean horiz() { return layout.index == 1; }
        private int n() { return hand.get() ? 5 : 4; }
        @Override public double w() { return horiz() ? n() * 22 + 4 : 44; }
        @Override public double h() { return horiz() ? 30 : n() * 19 + 3; }
        @Override public boolean visible() {
            for (int i = 0; i < 4; i++) if (!Mc.empty(Mc.armor(39 - i))) return true;
            return false;
        }
        @Override public String key() {
            StringBuilder k = new StringBuilder();
            String[] p = new String[5];
            int[] pv = new int[5];
            for (int i = 0; i < 5; i++) {
                class_1799 s;
                if (i < 4) s = Mc.armor(39 - i);
                else s = hand.get() && Mc.player() != null ? Mc.player().method_6047() : null;
                stacks[i] = s;
                pv[i] = 100;
                if (Mc.empty(s)) { p[i] = ""; k.append("-"); continue; }
                if (Mc.damageable(s) && Mc.maxDamage(s) > 0) {
                    int pc = (int) Math.round(100.0 * (Mc.maxDamage(s) - Mc.damage(s)) / Mc.maxDamage(s));
                    p[i] = pc + "%";
                    pv[i] = pc;
                } else p[i] = Mc.count(s) > 1 ? String.valueOf(Mc.count(s)) : "";
                k.append(Mc.itemKey(s)).append(p[i]).append(';');
            }
            pct = p;
            pctVal = pv;
            return k.toString() + layout.index;
        }
        @Override public void paint(Graphics2D g, Color accent) {
            String[] p = pct;
            int[] pv = pctVal;
            for (int i = 0; i < n(); i++) {
                if (p[i] == null || p[i].isEmpty()) continue;
                int v = pv[i];
                Color c = v > 60 ? H.TXT : v > 25 ? new Color(255, 205, 3) : new Color(255, 80, 80);
                if (horiz()) H.center(g, p[i], 2 + i * 22 + 11, 27.5, H.BOLD, 5.8f, c);
                else H.text(g, p[i], 21, i * 19 + 13, H.BOLD, 6.4f, c);
            }
        }
        @Override public void overlay(class_332 ctx, int x, int y, double k) {
            for (int i = 0; i < n(); i++) {
                class_1799 s = stacks[i];
                if (Mc.empty(s)) continue;
                if (horiz()) drawItem(ctx, s, x + (2 + i * 22 + 3) * k, y + 3 * k, k);
                else drawItem(ctx, s, x + 3 * k, y + (i * 19 + 2) * k, k);
            }
        }
    }

    /** Draws an item scaled by k (16 GUI units * k). */
    static void drawItem(class_332 ctx, class_1799 s, double x, double y, double k) {
        org.joml.Matrix3x2fStack m = ctx.method_51448();
        m.pushMatrix();
        m.translate((float) x, (float) y);
        m.scale((float) k, (float) k);
        ctx.method_51427(s, 0, 0);
        m.popMatrix();
    }

    // ------------------------------------------------------------------ status effects
    static final class Effects extends HudModule {
        private volatile List<String[]> rows = new ArrayList<>();
        Effects() { super("effects", "Effekte", "Aktive Trank-Effekte mit Restzeit", 0.994, 0.35); icon("potion"); }
        @Override public boolean visible() { return !rows.isEmpty(); }
        @Override public double w() {
            double m = 70;
            for (String[] r : rows) m = Math.max(m, H.w(r[0], H.MED, 7.2f) + H.w(r[1], H.BOLD, 7.2f) + 26);
            return Math.ceil(m / 4) * 4;
        }
        @Override public double h() { return Math.max(1, rows.size()) * 14 + 4; }
        @Override public String key() {
            List<String[]> out = new ArrayList<>();
            Collection<class_1293> eff = Mc.effects();
            for (class_1293 e : eff) {
                String name = class_2561.method_43471(e.method_5586()).getString();
                int amp = e.method_5578();
                if (amp > 0) name += " " + roman(amp + 1);
                int d = e.method_5584();
                String t = d < 0 || d > 20 * 60 * 60 * 5 ? "∞" : String.format("%d:%02d", d / 20 / 60, (d / 20) % 60);
                out.add(new String[]{name, t, d > 0 && d < 200 ? "1" : "0"});
            }
            rows = out;
            StringBuilder k = new StringBuilder();
            for (String[] r : out) k.append(r[0]).append(r[1]);
            return k.toString();
        }
        private static String roman(int n) { return new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}[Math.min(10, n)]; }
        @Override public void paint(Graphics2D g, Color accent) {
            List<String[]> r = rows;
            double w = w();
            for (int i = 0; i < r.size(); i++) {
                double y = 2 + i * 14;
                boolean ending = r.get(i)[2].equals("1");
                H.round(g, 6, y + 4.5, 5, 5, 2.5, ending ? new Color(255, 80, 80) : accent);
                H.text(g, r.get(i)[0], 15, y + 10, H.MED, 7.2f, H.TXT);
                H.right(g, r.get(i)[1], w - 7, y + 10, H.BOLD, 7.2f, ending ? new Color(255, 120, 120) : H.SUB);
            }
        }
    }

    // ------------------------------------------------------------------ keystrokes
    static final class Keystrokes extends HudModule {
        private final Setting.Bool mouse = add(new Setting.Bool("Maustasten + CPS", true));
        private final Setting.Bool space = add(new Setting.Bool("Leertaste", true));
        private volatile int mask;
        private volatile String cps = "";
        private final float[] glow = new float[7];
        private long lastPaint = System.nanoTime();
        Keystrokes() { super("keystrokes", "Tastenanzeige", "WASD, Maustasten und Leertaste mit Animation", 0.006, 0.83); icon("keys"); }
        @Override public double w() { return 64; }
        @Override public double h() { return 42 + (mouse.get() ? 21 : 0) + (space.get() ? 14 : 0); }
        @Override public String key() {
            net.minecraft.class_315 o = Mc.mc().field_1690;
            int m = 0;
            if (Mc.pressed(o.field_1894)) m |= 1;
            if (Mc.pressed(o.field_1913)) m |= 2;
            if (Mc.pressed(o.field_1881)) m |= 4;
            if (Mc.pressed(o.field_1849)) m |= 8;
            if (Mc.pressed(o.field_1903)) m |= 16;
            if (Mc.pressed(o.field_1886)) m |= 32;
            if (Mc.pressed(o.field_1904)) m |= 64;
            mask = m;
            cps = ClickCounter.left() + "|" + ClickCounter.right();
            // keep repainting while a key fades out
            boolean fading = false;
            for (float f : glow) if (f > 0.01f && f < 0.99f) fading = true;
            return m + "|" + cps + "|" + mouse.get() + space.get() + (fading ? System.nanoTime() / 16_000_000L : 0);
        }
        @Override public boolean panel() { return false; }
        @Override public void paint(Graphics2D g, Color accent) {
            long now = System.nanoTime();
            float dt = Math.min(0.1f, (now - lastPaint) / 1e9f);
            lastPaint = now;
            int m = mask;
            for (int i = 0; i < 7; i++) {
                boolean down = (m & (1 << i)) != 0;
                glow[i] = down ? 1 : Math.max(0, glow[i] - dt * 6);
            }
            key(g, "W", 22, 0, 20, 20, glow[0], accent);
            key(g, "A", 0, 21, 20, 20, glow[1], accent);
            key(g, "S", 22, 21, 20, 20, glow[2], accent);
            key(g, "D", 44, 21, 20, 20, glow[3], accent);
            double y = 42;
            if (mouse.get()) {
                String[] c = cps.split("\\|");
                key(g, "LMB", 0, y, 31, 20, glow[5], accent);
                key(g, "RMB", 33, y, 31, 20, glow[6], accent);
                H.center(g, (c.length > 0 ? c[0] : "0") + " CPS", 15.5, y + 17, H.MED, 4.6f, H.mix(H.SUB, Color.BLACK, glow[5]));
                H.center(g, (c.length > 1 ? c[1] : "0") + " CPS", 48.5, y + 17, H.MED, 4.6f, H.mix(H.SUB, Color.BLACK, glow[6]));
                y += 21;
            }
            if (space.get()) {
                key(g, "", 0, y, 64, 13, glow[4], accent);
                g.setColor(H.mix(H.TXT, new Color(20, 20, 20), glow[4]));
                g.fill(new RoundRectangle2D.Double(22, y + 5.7, 20, 1.6, 1.6, 1.6));
            }
        }
        private static void key(Graphics2D g, String s, double x, double y, double w, double h, float down, Color accent) {
            int a = (int) (255 * Math.max(0.35, UiSettings.hudOpacity + 0.2));
            g.setColor(H.mix(new Color(12, 12, 16, Math.min(255, a)), accent, down));
            g.fill(new RoundRectangle2D.Double(x, y, w, h, 7, 7));
            g.setColor(new Color(255, 255, 255, (int) (18 * (1 - down))));
            g.draw(new RoundRectangle2D.Double(x + 0.3, y + 0.3, w - 0.6, h - 0.6, 7, 7));
            if (!s.isEmpty()) {
                boolean small = s.length() > 1;
                H.center(g, s, x + w / 2, small ? y + 9.5 : y + h / 2 + 3, H.BOLD, small ? 6.2f : 8.4f, H.mix(H.TXT, new Color(18, 18, 18), down));
            }
        }
    }

    // ------------------------------------------------------------------ item counters
    static final class Totems extends HudModule {
        private volatile int n;
        Totems() { super("totems", "Totem-Zähler", "Wie viele Totems du hast", 0.44, 0.8); icon("totem"); }
        @Override public double w() { return 44; }
        @Override public double h() { return 20; }
        @Override public String key() { n = Mc.countItem("totem_of_undying"); return String.valueOf(n); }
        @Override public void paint(Graphics2D g, Color accent) {
            H.text(g, String.valueOf(n), 23, H.base(0, 20, 9f), H.BOLD, 9f, n == 0 ? new Color(255, 90, 90) : H.TXT);
        }
        @Override public void overlay(class_332 ctx, int x, int y, double k) {
            class_1799 icon = Mc.icon("totem_of_undying");
            if (icon != null) drawItem(ctx, icon, x + 4 * k, y + 2 * k, k);
        }
    }

    static final class PvpItems extends HudModule {
        private static final String[] IDS = {"end_crystal", "obsidian", "golden_apple", "enchanted_golden_apple", "ender_pearl", "experience_bottle"};
        private volatile int[] counts = new int[IDS.length];
        PvpItems() { super("pvpitems", "PvP-Items", "Kristalle, Obsidian, Äpfel, Perlen, XP", 0.56, 0.8); icon("sword"); }
        @Override public double w() { return IDS.length * 24 + 4; }
        @Override public double h() { return 29; }
        @Override public String key() {
            int[] c = new int[IDS.length];
            StringBuilder k = new StringBuilder();
            for (int i = 0; i < IDS.length; i++) { c[i] = Mc.countItem(IDS[i]); k.append(c[i]).append(','); }
            counts = c;
            return k.toString();
        }
        @Override public void paint(Graphics2D g, Color accent) {
            int[] c = counts;
            for (int i = 0; i < c.length; i++)
                H.center(g, String.valueOf(c[i]), 2 + i * 24 + 12, 26.5, H.BOLD, 6.2f, c[i] == 0 ? H.DIM : H.TXT);
        }
        @Override public void overlay(class_332 ctx, int x, int y, double k) {
            for (int i = 0; i < IDS.length; i++) {
                class_1799 icon = Mc.icon(IDS[i]);
                if (icon != null) drawItem(ctx, icon, x + (2 + i * 24 + 4) * k, y + 2 * k, k);
            }
        }
    }

    // ------------------------------------------------------------------ target HUD
    static final class TargetHud extends HudModule {
        private volatile String name = "", info = "";
        private volatile float hp, max, abs;
        private volatile boolean has;
        private volatile double shownHp = -1;
        TargetHud() { super("target", "Target HUD", "Leben und Abstand des Gegners mit Animation", 0.5, 0.62); enabled = true; icon("target"); }
        @Override public boolean visible() { return has; }
        @Override public double w() { return 130; }
        @Override public double h() { return 36; }
        @Override public String key() {
            class_1297 t = CombatTracker.target();
            class_746 p = Mc.player();
            if (!(t instanceof class_1309) || p == null) { has = false; shownHp = -1; return "none"; }
            class_1309 l = (class_1309) t;
            has = true;
            name = Mc.name(t);
            hp = Mc.health(l);
            max = Math.max(1, Mc.maxHealth(l));
            abs = Mc.absorption(l);
            info = String.format(Locale.ROOT, "%.1f m", Math.sqrt(p.method_5858(t)));
            if (shownHp < 0) shownHp = hp;
            boolean animating = Math.abs(shownHp - hp) > 0.05;
            return name + hp + "|" + abs + "|" + info + (animating ? "|" + System.nanoTime() / 16_000_000L : "");
        }
        @Override public void paint(Graphics2D g, Color accent) {
            double w = w();
            shownHp += (hp - shownHp) * 0.25;
            H.round(g, 5, 5, 26, 26, 8, H.alpha(accent, 50));
            H.center(g, name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT), 18, 22.5, H.BOLD, 13f, accent);
            H.text(g, H.ellipsize(name, w - 80, H.BOLD, 8f), 37, 14, H.BOLD, 8f, H.TXT);
            H.right(g, info, w - 7, 14, H.MED, 6.6f, H.SUB);
            double frac = hp / max, shown = shownHp / max;
            Color hc = frac > 0.6 ? accent : frac > 0.3 ? new Color(255, 205, 3) : new Color(255, 70, 70);
            H.bar(g, 37, 19, w - 44, 4.5, Math.max(frac, shown), new Color(255, 255, 255, 90));
            H.round(g, 37, 19, Math.max(4.5, (w - 44) * Math.max(0, Math.min(1, frac))), 4.5, 2.25, hc);
            if (abs > 0) H.round(g, 37, 19, Math.max(4.5, (w - 44) * Math.min(1, abs / max)), 4.5, 2.25, new Color(255, 205, 3, 210));
            H.text(g, String.format(Locale.ROOT, "%.1f", (hp + abs) / 2f), 37, 31.5, H.BOLD, 6.8f, H.TXT);
            H.text(g, "Herzen", 37 + H.w(String.format(Locale.ROOT, "%.1f", (hp + abs) / 2f), H.BOLD, 6.8f) + 3, 31.5, H.REG, 6.2f, H.SUB);
        }
    }

    // ------------------------------------------------------------------ hunger / health
    static final class Hunger extends HudModule {
        private volatile int food;
        private volatile float sat;
        Hunger() { super("hunger", "Hunger & Sättigung", "Essen und Sättigung als Balken", 0.994, 0.6); icon("food"); }
        @Override public double w() { return 100; }
        @Override public double h() { return 26; }
        @Override public String key() { food = Mc.food(); sat = Mc.saturation(); return food + "|" + Math.round(sat * 10); }
        @Override public void paint(Graphics2D g, Color accent) {
            H.text(g, "Hunger", 7, 10.5, H.MED, 6.6f, H.SUB);
            H.right(g, food + "/20  •  " + String.format(Locale.ROOT, "%.1f", sat), 93, 10.5, H.BOLD, 6.6f, H.TXT);
            H.bar(g, 7, 14, 86, 3.2, food / 20.0, new Color(255, 170, 60));
            H.bar(g, 7, 19.5, 86, 3.2, sat / 20.0, new Color(255, 215, 60));
        }
    }

    static final class Health extends HudModule {
        private volatile float hp, max, abs;
        Health() { super("health", "Leben", "Deine Herzen als Balken", 0.994, 0.67); icon("heart"); }
        @Override public double w() { return 100; }
        @Override public double h() { return 18; }
        @Override public String key() {
            class_746 p = Mc.player();
            if (p == null) return "-";
            hp = Mc.health(p); max = Math.max(1, Mc.maxHealth(p)); abs = Mc.absorption(p);
            return hp + "|" + abs + "|" + max;
        }
        @Override public void paint(Graphics2D g, Color accent) {
            H.icon(g, "heart-fill", 11, 9, 9, new Color(255, 70, 90));
            H.bar(g, 20, 7.5, 50, 3.5, hp / max, new Color(255, 70, 90));
            if (abs > 0) H.round(g, 20, 7.5, Math.max(3.5, 50 * Math.min(1, abs / max)), 3.5, 1.75, new Color(255, 205, 3, 220));
            H.right(g, String.format(Locale.ROOT, "%.1f", (hp + abs) / 2f), 93, H.base(0, 18, 7.6f), H.BOLD, 7.6f, H.TXT);
        }
    }

    // ------------------------------------------------------------------ inventory HUD
    static final class InventoryHud extends HudModule {
        private final Setting.Bool showHotbar = add(new Setting.Bool("Hotbar zeigen", true));
        private final Setting.Bool showArmor = add(new Setting.Bool("Rüstung zeigen", true));
        private final Setting.Bool showOffhand = add(new Setting.Bool("Offhand zeigen", true));
        private final class_1799[] items = new class_1799[41];
        private volatile int held;
        InventoryHud() { super("invhud", "Inventar HUD", "Dein Inventar als Overlay auf dem Bildschirm", 0.5, 0.75); icon("backpack"); }
        private int rows() { return (showHotbar.get() ? 4 : 3) + (showArmor.get() ? 1 : 0); }
        @Override public double w() { return 9 * 20 + 6; }
        @Override public double h() {
            int r = rows();
            double base = r * 20 + 6;
            if (showHotbar.get()) base += 4; // gap between hotbar and inv
            return base;
        }
        @Override public boolean visible() { return Mc.player() != null; }
        @Override public String key() {
            StringBuilder k = new StringBuilder();
            class_746 p = Mc.player();
            if (p == null) return "-";
            class_1799 main = p.method_6047();
            held = -1;
            for (int s = 0; s < 9; s++) { if (Mc.invStack(s) == main) { held = s; break; } }
            for (int i = 0; i < 36; i++) {
                items[i] = Mc.invStack(i);
                k.append(Mc.itemKey(items[i])).append(Mc.count(items[i])).append(';');
            }
            for (int i = 36; i < 40; i++) {
                items[i] = Mc.armor(i);
                k.append(Mc.itemKey(items[i])).append(';');
            }
            items[40] = p.method_6079();
            k.append(Mc.itemKey(items[40])).append(showHotbar.get()).append(showArmor.get()).append(showOffhand.get()).append(held);
            return k.toString();
        }
        @Override public void paint(Graphics2D g, Color accent) {
            Color slot = new Color(30, 32, 40, 140);
            Color slotA = new Color(30, 32, 40, 180);
            double y = 3;
            // armor row
            if (showArmor.get()) {
                for (int i = 0; i < 4; i++) H.round(g, 3 + i * 20, y, 18, 18, 3, slotA);
                if (showOffhand.get()) {
                    H.round(g, 3 + 5 * 20, y, 18, 18, 3, slotA);
                    H.center(g, "OH", 3 + 5 * 20 + 9, y + 14, H.REG, 4.5f, H.DIM);
                }
                y += 22;
            }
            // inventory rows 1-3 (slots 9-35)
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) H.round(g, 3 + col * 20, y, 18, 18, 3, slot);
                y += 20;
            }
            // hotbar (slots 0-8)
            if (showHotbar.get()) {
                y += 4;
                for (int col = 0; col < 9; col++) {
                    boolean sel = col == held;
                    H.round(g, 3 + col * 20, y, 18, 18, 3, sel ? H.alpha(accent, 80) : new Color(30, 32, 40, 160));
                    if (sel) H.round(g, 3 + col * 20, y, 18, 18, 3, H.alpha(accent, 40));
                }
            }
        }
        @Override public void overlay(class_332 ctx, int x, int y, double k) {
            double dy = 3 * k;
            // armor
            if (showArmor.get()) {
                for (int i = 0; i < 4; i++) {
                    class_1799 s = items[39 - i];
                    if (!Mc.empty(s)) drawItem(ctx, s, x + (3 + i * 20 + 1) * k, y + (dy + 1 * k), k);
                }
                if (showOffhand.get()) {
                    class_1799 s = items[40];
                    if (!Mc.empty(s)) drawItem(ctx, s, x + (3 + 5 * 20 + 1) * k, y + (dy + 1 * k), k);
                }
                dy += 22 * k;
            }
            // inventory
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    class_1799 s = items[9 + row * 9 + col];
                    if (!Mc.empty(s)) drawItem(ctx, s, x + (3 + col * 20 + 1) * k, y + dy + 1 * k, k);
                }
                dy += 20 * k;
            }
            // hotbar
            if (showHotbar.get()) {
                dy += 4 * k;
                for (int col = 0; col < 9; col++) {
                    class_1799 s = items[col];
                    if (!Mc.empty(s)) drawItem(ctx, s, x + (3 + col * 20 + 1) * k, y + dy + 1 * k, k);
                }
            }
        }
    }

    // ------------------------------------------------------------------ shared trackers
    /** Counts mouse clicks per second (sampled every frame). */
    public static final class ClickCounter {
        private static final java.util.ArrayDeque<Long> L = new java.util.ArrayDeque<>(), R = new java.util.ArrayDeque<>();
        private static boolean lastL, lastR;
        public static void frame() {
            boolean l = Mc.mouseDown(0), r = Mc.mouseDown(1);
            long now = System.currentTimeMillis();
            boolean count = !Mc.screenOpen();
            if (l && !lastL && count) L.add(now);
            if (r && !lastR && count) R.add(now);
            lastL = l; lastR = r;
            while (!L.isEmpty() && now - L.peekFirst() > 1000) L.pollFirst();
            while (!R.isEmpty() && now - R.peekFirst() > 1000) R.pollFirst();
        }
        public static int left() { return L.size(); }
        public static int right() { return R.size(); }
    }

    /** Remembers the last attacked entity, combo and reach. */
    public static final class CombatTracker {
        private static class_1297 last;
        private static long lastAt;
        private static int combo;
        private static double reach;
        public static void onAttack(class_1297 target) {
            class_746 p = Mc.player();
            long now = System.currentTimeMillis();
            if (target == last && now - lastAt < 2500) combo++; else combo = 1;
            last = target;
            lastAt = now;
            if (p != null) reach = Math.sqrt(p.method_5858(target)) - Mc.width(target) / 2.0;
        }
        public static class_1297 target() {
            class_1297 looked = Mc.mc().field_1692;
            if (looked instanceof class_1657) return looked;
            if (last != null && System.currentTimeMillis() - lastAt < 8000 && last.method_5805()) return last;
            return looked instanceof class_1309 ? looked : null;
        }
        public static int combo() { return System.currentTimeMillis() - lastAt > 3000 ? 0 : combo; }
        public static double lastReach() { return Math.max(0, reach); }
        public static class_1297 lastTarget() { return last; }
        public static long lastAttackAt() { return lastAt; }
    }
}
