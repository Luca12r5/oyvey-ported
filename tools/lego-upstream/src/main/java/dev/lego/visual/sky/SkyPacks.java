package dev.lego.visual.sky;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dev.lego.LegoClient;
import dev.lego.core.Mc;
import dev.spotifyhud.Json;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;

/**
 * Reads sky resource packs (OptiFine "optifine/sky/world0/*.properties" and FabricSkyboxes "fabricskyboxes/sky/*.json")
 * from the active resource packs, so realistic photo skies work without OptiFine / FabricSkyboxes installed.
 */
public final class SkyPacks {
    private SkyPacks() { }

    /** One sky layer. Textures: either one strip (3x2 faces) or six single faces. */
    public static final class Layer {
        public class_2960 strip;
        public class_2960[] faces;          // bottom, top, south, west, north, east
        public int[] fade;                  // startIn, endIn, startOut, endOut in ticks (null = always on)
        public double[] axis = {0, 0, 1};
        public double speed = 1;
        public boolean rotate = true;
        public boolean add;                 // additive blend (drawn with lower alpha)
        public int priority;
        public String source;

        /** 0..1 visibility at the given time of day (ticks 0..24000). */
        public double alpha(long t) {
            if (fade == null) return 1;
            double tt = ((t % 24000) + 24000) % 24000;
            double sIn = fade[0], eIn = fade[1], sOut = fade[2], eOut = fade[3];
            if (in(tt, eIn, sOut)) return 1;
            if (in(tt, sIn, eIn)) return frac(tt, sIn, eIn);
            if (in(tt, sOut, eOut)) return 1 - frac(tt, sOut, eOut);
            return 0;
        }

        private static boolean in(double t, double a, double b) { return a <= b ? t >= a && t <= b : t >= a || t <= b; }
        private static double frac(double t, double a, double b) {
            double len = ((b - a) % 24000 + 24000) % 24000;
            if (len == 0) return 1;
            double d = ((t - a) % 24000 + 24000) % 24000;
            return Math.max(0, Math.min(1, d / len));
        }
    }

    private static List<Layer> layers = null;
    private static String info = "";

    public static void invalidate() { layers = null; }

    public static String info() { return info; }

    public static List<Layer> layers() {
        if (layers == null) {
            try { layers = scan(); } catch (Throwable t) { layers = new ArrayList<>(); info = "Fehler: " + t.getMessage(); LegoClient.LOG("Sky-Packs: " + t); }
        }
        return layers;
    }

    private static List<Layer> scan() {
        class_3300 rm = Mc.mc().method_1478();
        List<Layer> fsb = new ArrayList<>(), of = new ArrayList<>();
        // FabricSkyboxes
        Map<class_2960, class_3298> json = rm.method_14488("fabricskyboxes/sky", id -> id.method_12832().endsWith(".json"));
        for (Map.Entry<class_2960, class_3298> e : json.entrySet()) {
            try (InputStream in = e.getValue().method_14482()) {
                Layer l = parseFsb(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                if (l != null) { l.source = e.getKey().toString(); fsb.add(l); }
            } catch (Throwable t) { LegoClient.LOG("Sky-JSON " + e.getKey() + ": " + t); }
        }
        // OptiFine
        Map<class_2960, class_3298> props = rm.method_14488("optifine/sky/world0", id -> id.method_12832().endsWith(".properties"));
        for (Map.Entry<class_2960, class_3298> e : props.entrySet()) {
            try (InputStream in = e.getValue().method_14482()) {
                Properties p = new Properties();
                p.load(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                Layer l = parseOptifine(e.getKey(), p);
                if (l != null) { l.source = e.getKey().toString(); of.add(l); }
            } catch (Throwable t) { LegoClient.LOG("Sky-Properties " + e.getKey() + ": " + t); }
        }
        List<Layer> out = !fsb.isEmpty() ? fsb : of;
        out.sort((a, b) -> Integer.compare(a.priority, b.priority));
        info = out.isEmpty() ? "Kein Sky-Pack aktiv" : out.size() + " Himmel-Ebene(n) aus " + (!fsb.isEmpty() ? "FabricSkyboxes" : "OptiFine") + "-Format";
        LegoClient.LOG("Sky-Packs: " + info);
        return out;
    }

    // ------------------------------------------------------------------ OptiFine
    private static Layer parseOptifine(class_2960 file, Properties p) {
        String path = file.method_12832();
        String dir = path.substring(0, path.lastIndexOf('/') + 1);
        String name = path.substring(path.lastIndexOf('/') + 1).replace(".properties", "");
        String src = p.getProperty("source", name + ".png").trim();
        class_2960 tex;
        if (src.startsWith("./")) tex = class_2960.method_60655(file.method_12836(), dir + src.substring(2));
        else if (src.contains(":")) tex = class_2960.method_60654(src);
        else if (src.startsWith("assets/")) {
            String[] parts = src.substring(7).split("/", 2);
            tex = class_2960.method_60655(parts[0], parts[1]);
        } else tex = class_2960.method_60655(file.method_12836(), src.contains("/") ? src : dir + src);
        if (!tex.method_12832().endsWith(".png")) tex = class_2960.method_60655(tex.method_12836(), tex.method_12832() + ".png");
        Layer l = new Layer();
        l.strip = tex;
        String a = p.getProperty("startFadeIn"), b = p.getProperty("endFadeIn"), c = p.getProperty("startFadeOut"), d = p.getProperty("endFadeOut");
        if (a != null && b != null && d != null) {
            int ia = hm(a), ib = hm(b), id = hm(d);
            int ic = c != null ? hm(c) : ((id - (ib - ia)) % 24000 + 24000) % 24000;
            l.fade = new int[]{ia, ib, ic, id};
        }
        l.rotate = !"false".equalsIgnoreCase(p.getProperty("rotate", "true").trim());
        try { l.speed = Double.parseDouble(p.getProperty("speed", "1").trim()); } catch (NumberFormatException ignored) { }
        String ax = p.getProperty("axis");
        if (ax != null) {
            String[] s = ax.trim().split("\\s+");
            if (s.length == 3) try { l.axis = new double[]{Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2])}; } catch (NumberFormatException ignored) { }
        }
        String blend = p.getProperty("blend", "add").trim();
        l.add = blend.equals("add");
        try { l.priority = Integer.parseInt(name.replaceAll("\\D", "")); } catch (NumberFormatException ignored) { }
        return l;
    }

    /** "hh:mm" -> world ticks (0 = 06:00). */
    private static int hm(String s) {
        String[] p = s.trim().split(":");
        int h = Integer.parseInt(p[0].trim()), m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
        return ((h * 1000 + m * 1000 / 60 - 6000) % 24000 + 24000) % 24000;
    }

    // ------------------------------------------------------------------ FabricSkyboxes (schema v2)
    @SuppressWarnings("unchecked")
    private static Layer parseFsb(String text) {
        Object root = Json.parse(text);
        if (!(root instanceof Map)) return null;
        Map<String, Object> m = (Map<String, Object>) root;
        String type = Json.str(root, "type", "");
        Layer l = new Layer();
        if (type.equals("single-sprite-square-textured") || type.equals("single-sprite-animated-square-textured")) {
            String t = Json.str(root, "texture", null);
            if (t == null) return null;
            l.strip = id(t);
        } else if (type.equals("square-textured")) {
            Map<String, Object> tx = Json.obj(root, "textures");
            if (tx == null) return null;
            l.faces = new class_2960[]{id(Json.str(tx, "bottom", "")), id(Json.str(tx, "top", "")), id(Json.str(tx, "south", "")),
                    id(Json.str(tx, "west", "")), id(Json.str(tx, "north", "")), id(Json.str(tx, "east", ""))};
        } else return null;   // monocolor / decorations only / animated are ignored
        Map<String, Object> props = Json.obj(root, "properties");
        if (props != null) {
            l.priority = (int) Json.num(props, "priority", 0);
            Map<String, Object> fade = Json.obj(props, "fade");
            if (fade != null && !Json.bool(fade, "alwaysOn", false) && fade.containsKey("startFadeIn")) {
                l.fade = new int[]{(int) Json.dbl(fade, "startFadeIn", 0), (int) Json.dbl(fade, "endFadeIn", 0),
                        (int) Json.dbl(fade, "startFadeOut", 0), (int) Json.dbl(fade, "endFadeOut", 0)};
            }
            Map<String, Object> rot = Json.obj(props, "rotation");
            if (rot != null) {
                java.util.List<Object> axis = Json.arr(rot, "axis");
                if (axis != null && axis.size() == 3) {
                    l.axis = new double[]{num(axis.get(0)), num(axis.get(1)), num(axis.get(2))};
                    double len = Math.sqrt(l.axis[0] * l.axis[0] + l.axis[1] * l.axis[1] + l.axis[2] * l.axis[2]);
                    l.rotate = len > 1e-6;
                } else l.rotate = false;
                l.speed = Json.dbl(rot, "rotationSpeed", 1);
            } else l.rotate = false;
        }
        Map<String, Object> blend = Json.obj(root, "blend");
        l.add = blend != null && "add".equals(Json.str(blend, "type", ""));
        return l;
    }

    private static double num(Object o) { return o instanceof Number ? ((Number) o).doubleValue() : 0; }

    private static class_2960 id(String s) {
        if (s == null || s.isEmpty()) return null;
        return s.contains(":") ? class_2960.method_60654(s) : class_2960.method_60655("minecraft", s);
    }
}
