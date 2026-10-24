package dev.lego.visual.sky;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

/**
 * Realistic custom skies: 7 procedural HD skies, sky resource packs (OptiFine / FabricSkyboxes format)
 * and own images from config/legoclient/skies (3:2 cube strip or 2:1 panorama).
 */
public final class CustomSky extends Module {
    private static final String[] MODES;
    static {
        MODES = new String[SkyGen.NAMES.length + 3];
        System.arraycopy(SkyGen.NAMES, 0, MODES, 0, SkyGen.NAMES.length);
        MODES[SkyGen.NAMES.length] = "Resourcepack";
        MODES[SkyGen.NAMES.length + 1] = "Eigenes Bild";
        MODES[SkyGen.NAMES.length + 2] = "Automatisch (Tageszeit)";
    }
    private static final int[] RES = {768, 1024, 1536};

    public final Setting.Mode mode = add(new Setting.Mode("Himmel", 0, MODES));
    public final Setting.Mode quality = add(new Setting.Mode("Qualität", 1, "Normal", "HD", "Ultra 4K"));
    public final Setting.Mode daySky = add(new Setting.Mode("Auto: Tag-Himmel", idx("anime"), SkyGen.NAMES));
    public final Setting.Mode duskSky = add(new Setting.Mode("Auto: Abend-Himmel", idx("goldenhour"), SkyGen.NAMES));
    public final Setting.Mode nightSky = add(new Setting.Mode("Auto: Nacht-Himmel", idx("milkyway"), SkyGen.NAMES));
    public final Setting.Bool fog = add(new Setting.Bool("Nebel an Himmel anpassen", true));
    public final Setting.Num fogDensity = add(new Setting.Num("Nebel-Dichte", 0, 100, 5, 0, "%"));
    public final Setting.Color fogColor = add(new Setting.Color("Nebel-Farbe", 8));
    public final Setting.Num fogStart = add(new Setting.Num("Nebel-Start", 0, 100, 5, 20, "%"));
    public final Setting.Num speed = add(new Setting.Num("Bewegung", 0, 4, 0.1, 1, "x"));
    public final Setting.Num brightness = add(new Setting.Num("Helligkeit", 0.2, 1, 0.05, 0.9, ""));
    public final Setting.Bool shooting = add(new Setting.Bool("Sternschnuppen", true));
    public final Setting.Bool nightOnly = add(new Setting.Bool("Nur nachts einblenden", false));
    public final Setting.Bool overworldOnly = add(new Setting.Bool("Nur Oberwelt", true));

    private final AtomicReference<int[]> pending = new AtomicReference<>();
    private volatile String generating = null;
    private String loadedKey = null, pendingKey = null;
    private int pendingW, pendingH;
    private class_2960 tex = null;
    private boolean equirect = false;
    private int imageIndex = 0;

    private static int idx(String id) {
        for (int i = 0; i < SkyGen.IDS.length; i++) if (SkyGen.IDS[i].equals(id)) return i;
        return 0;
    }

    public CustomSky() {
        super("sky", "Custom Sky", "Realistische HD-Himmel, Sky-Resourcepacks und eigene Bilder", Category.VISUALS);
        icon("cloud");
        fresh();
        add(new Setting.Action("Sky-Packs", "Neu laden", () -> { SkyPacks.invalidate(); SkyPacks.layers(); Toasts.show("cloud", "Custom Sky", SkyPacks.info()); }));
        add(new Setting.Action("Eigene Bilder", "Nächstes Bild", () -> { imageIndex++; loadedKey = null; }));
        add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
            Path p = folder();
            Mc.clipboard(p.toString());
            Toasts.show("copy", "Ordner kopiert", p.toString());
        }));
    }

    public static Path folder() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skies");
        try { Files.createDirectories(p); } catch (Throwable ignored) { }
        return p;
    }

    private boolean builtIn() { return mode.index < SkyGen.IDS.length; }
    private boolean packs() { return mode.index == SkyGen.IDS.length; }
    private boolean auto() { return mode.index == SkyGen.IDS.length + 2; }

    @Override public void onDisable() { free(); }

    /** Loaded sky textures by key (preset@res or img@n) - at most 3 kept. */
    private final java.util.LinkedHashMap<String, class_2960> loaded = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, Boolean> equi = new java.util.HashMap<>();

    private void free() {
        for (class_2960 t : loaded.values()) Tx.destroy(t);
        loaded.clear();
        tex = null;
        loadedKey = null;
    }

    private static Path cacheDir() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skycache");
        try { Files.createDirectories(p); } catch (Throwable ignored) { }
        return p;
    }

    /** Texture for a key or null while it is still being generated (starts the job). Render thread. */
    private class_2960 texture(String key) {
        class_2960 t = loaded.get(key);
        if (t != null) return t;
        int[] px = pending.get();
        if (px != null && key.equals(pendingKey)) {
            pending.set(null);
            t = class_2960.method_60655("legoclient", "sky/" + key.replace('@', '_').replace('.', '_').toLowerCase(java.util.Locale.ROOT));
            Tx.register(t, px, pendingW, pendingH, true);
            loaded.put(key, t);
            equi.put(key, equirect);
            while (loaded.size() > 3) {
                String first = loaded.keySet().iterator().next();
                Tx.destroy(loaded.remove(first));
            }
            return t;
        }
        if (generating != null) return null;
        generating = key;
        final String k = key;
        Thread th = new Thread(() -> {
            try {
                if (k.startsWith("img@")) loadImage(k);
                else {
                    String id = k.substring(0, k.indexOf('@'));
                    int n = Integer.parseInt(k.substring(k.indexOf('@') + 1));
                    java.io.File f = cacheDir().resolve(id + "_" + n + "_v3.png").toFile();
                    int[] img = null;
                    if (f.isFile()) {
                        try {
                            java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(f);
                            if (bi != null && bi.getWidth() == n * 3 && bi.getHeight() == n * 2) img = bi.getRGB(0, 0, n * 3, n * 2, null, 0, n * 3);
                        } catch (Throwable ignored) { }
                    }
                    if (img == null) {
                        img = SkyGen.generate(id, n);
                        try {
                            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(n * 3, n * 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
                            bi.setRGB(0, 0, n * 3, n * 2, img, 0, n * 3);
                            javax.imageio.ImageIO.write(bi, "png", f);
                        } catch (Throwable ignored) { }
                    }
                    for (int i = 0; i < img.length; i++) img[i] |= 0xFF000000;
                    pendingW = n * 3;
                    pendingH = n * 2;
                    equirect = false;
                    pendingKey = k;
                    pending.set(img);
                }
            } catch (Throwable e) {
                LegoClient.LOG("Sky-Generierung: " + e);
            } finally {
                generating = null;
            }
        }, "LegoClient-Sky");
        th.setDaemon(true);
        th.setPriority(Thread.MIN_PRIORITY);
        th.start();
        return null;
    }

    private String presetKey(int i) { return SkyGen.IDS[Math.max(0, Math.min(SkyGen.IDS.length - 1, i))] + "@" + RES[quality.index]; }

    /** Sky(s) to draw now with their weights (auto mode blends day / dusk / night). */
    private java.util.List<Object[]> plan(long tod) {
        java.util.List<Object[]> out = new java.util.ArrayList<>();
        if (auto()) {
            double[] w = dayWeights(tod);
            int[] ids = {daySky.index, duskSky.index, nightSky.index};
            for (int i = 0; i < 3; i++) if (w[i] > 0.001) out.add(new Object[]{presetKey(ids[i]), w[i], SkyGen.IDS[ids[i]]});
            out.sort((x, y) -> Double.compare((Double) y[1], (Double) x[1]));
        } else if (builtIn()) out.add(new Object[]{presetKey(mode.index), 1.0, SkyGen.IDS[mode.index]});
        else out.add(new Object[]{"img@" + imageIndex, 1.0, null});
        return out;
    }

    /** Weights for day, dusk (sunset + sunrise), night from the time of day. */
    static double[] dayWeights(long t) {
        t = Math.floorMod(t, 24000L);
        double dusk = Math.max(bump(t, 12600, 1400), bump(t, 23300, 1200) + bump(t - 24000, 23300 - 24000, 1200));
        double night = t >= 13600 && t <= 22400 ? 1 : (t > 12600 && t < 13600 ? (t - 12600) / 1000.0 : (t > 22400 && t < 23400 ? 1 - (t - 22400) / 1000.0 : 0));
        double day = Math.max(0, 1 - night - dusk);
        night = Math.max(0, night - dusk * 0.5);
        double s = day + dusk + night;
        return new double[]{day / s, dusk / s, night / s};
    }

    private static double bump(double t, double c, double halfWidth) {
        double d = Math.abs(t - c) / halfWidth;
        return d >= 1 ? 0 : 0.5 + 0.5 * Math.cos(d * Math.PI);
    }

    /** Horizon color of the sky currently shown (0 = none) - used to tint the fog. */
    public int fogTint() {
        if (!enabled || !fog.get() || packs() || !inOverworld()) return 0;
        java.util.List<Object[]> p = plan(Mc.timeOfDay());
        double r = 0, g = 0, b = 0, ws = 0;
        for (Object[] e : p) {
            if (e[2] == null || !loaded.containsKey((String) e[0])) continue;
            int c = SkyGen.fogColor((String) e[2]);
            double w = (Double) e[1];
            r += ((c >> 16) & 255) * w; g += ((c >> 8) & 255) * w; b += (c & 255) * w; ws += w;
        }
        if (ws <= 0) return 0;
        return 0xFF000000 | ((int) (r / ws) << 16) | ((int) (g / ws) << 8) | (int) (b / ws);
    }

    /** True when the custom sky replaces the vanilla sky right now (texture ready). */
    public boolean replacesVanilla() {
        if (!enabled || !inOverworld()) return false;
        if (packs()) return !SkyPacks.layers().isEmpty();
        for (Object[] e : plan(Mc.timeOfDay())) if (loaded.containsKey((String) e[0])) return true;
        return false;
    }

    private boolean inOverworld() {
        if (!overworldOnly.get()) return true;
        try { return Mc.world().method_27983().method_29177().method_12832().equals("overworld"); } catch (Throwable ignored) { return true; }
    }

    private void loadImage(String key) throws Exception {
        List<File> files = new ArrayList<>();
        File[] list = folder().toFile().listFiles();
        if (list != null) for (File f : list) {
            String n = f.getName().toLowerCase(java.util.Locale.ROOT);
            if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) files.add(f);
        }
        files.sort(java.util.Comparator.comparing(File::getName));
        if (files.isEmpty()) {
            Toasts.show("image", "Keine Bilder", "Leg Himmel-Bilder in " + folder());
            pendingW = 1;
            pendingH = 1;
            equirect = false;
            pendingKey = key;
            pending.set(new int[]{0});
            return;
        }
        File f = files.get(Math.floorMod(imageIndex, files.size()));
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
        int w = img.getWidth(), h = img.getHeight();
        // limit to 8192 px wide
        if (w > 8192) {
            int nh = h * 8192 / w;
            java.awt.image.BufferedImage s = new java.awt.image.BufferedImage(8192, nh, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = s.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, 8192, nh, null);
            g.dispose();
            img = s;
            w = 8192;
            h = nh;
        }
        equirect = Math.abs(w / (double) h - 2.0) < 0.2;
        pendingW = w;
        pendingH = h;
        pendingKey = key;
        pending.set(img.getRGB(0, 0, w, h, null, 0, w));
        Toasts.show("image", "Himmel geladen", f.getName());
    }

    // ------------------------------------------------------------------ rendering
    public void render() {
        if (!inOverworld()) return;
        double far = 256;
        try { far = Mc.mc().field_1773.method_32796(); } catch (Throwable ignored) { }
        double view = Mc.mc().field_1690.method_38521() * 16.0;
        double R = R3.skyRadius > 0 ? R3.skyRadius : Math.max(96, Math.min(far * 0.8, Math.max(view * 1.8, 140)));   // beyond the farthest terrain, inside the far plane
        long tod = Mc.timeOfDay() % 24000;
        double vis = 1;
        if (nightOnly.get()) vis = nightFactor(tod);
        if (vis <= 0.001) return;
        double bright = brightness.get();

        if (packs()) {
            for (SkyPacks.Layer l : SkyPacks.layers()) {
                double a = l.alpha(tod) * vis;
                if (a <= 0.003) continue;
                double ang = l.rotate ? celestial(tod) * 360 * l.speed : 0;
                int c = argb(bright * (l.add ? 0.9 : 1), a * (l.add ? 0.8 : 1));
                if (l.strip != null) {
                    linearOnce(l.strip);
                    drawCube(R3.skyLayer(l.strip), null, R, l.axis, ang, c);
                } else if (l.faces != null) {
                    class_1921[] fl = new class_1921[6];
                    for (int i = 0; i < 6; i++) if (l.faces[i] != null) { linearOnce(l.faces[i]); fl[i] = R3.skyLayer(l.faces[i]); }
                    drawCube(null, fl, R, l.axis, ang, c);
                }
            }
            return;
        }
        double t = System.currentTimeMillis() / 1000.0;
        double ang = t * 0.6 * speed.get();
        boolean anyStorm = false, stars = false;
        for (Object[] e : plan(tod)) {
            String key = (String) e[0];
            double w = (Double) e[1];
            String id = (String) e[2];
            class_2960 tx = texture(key);
            if (tx == null) continue;
            boolean storm = id != null && (id.equals("storm") || id.equals("thunder"));
            double flash = storm ? lightning(t) : 0;
            anyStorm |= flash > 0.01;
            String kind = id == null ? "" : SkyGen.kind(id);
            stars |= w > 0.4 && (kind.equals("night") || kind.equals("space"));
            int c = argb(Math.min(1, bright + flash * 0.4), vis * w);
            class_1921 layer = R3.skyLayer(tx);
            if (Boolean.TRUE.equals(equi.get(key))) drawSphere(layer, R, ang, c);
            else drawCube(layer, null, R, new double[]{0, 1, 0}, ang, c);
            if (flash > 0.01) {
                int fc = R3.argb(0xDDE6FF, flash * 0.35 * vis * w);
                double r = R * 0.5;
                R3.box(R3.camX() - r, R3.camY() - r * 0.2, R3.camZ() - r, R3.camX() + r, R3.camY() + r, R3.camZ() + r, fc);
            }
        }
        if (shooting.get() && stars) shootingStars(R * 0.97, vis);
    }

    private static final java.util.Set<class_2960> LINEAR = new java.util.HashSet<>();

    private static void linearOnce(class_2960 id) {
        if (LINEAR.add(id)) {
            try { Tx.linear(Tx.tm().method_4619(id)); } catch (Throwable ignored) { }
        }
    }

    private static double celestial(long tod) {
        double d = ((tod / 24000.0) - 0.25);
        d -= Math.floor(d);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (d * 2.0 + e) / 3.0;
    }

    private static double nightFactor(long t) {
        if (t >= 13500 && t <= 22500) return 1;
        if (t > 12000 && t < 13500) return (t - 12000) / 1500.0;
        if (t > 22500 && t < 24000) return 1 - (t - 22500) / 1500.0;
        return 0;
    }

    private static double lightning(double t) {
        int slot = (int) Math.floor(t / 7.0);
        double local = t - slot * 7.0;
        if (dev.spotifyhud.Anim.rnd(slot, 77) > 0.55) return 0;
        double start = dev.spotifyhud.Anim.rnd(slot, 78) * 5;
        double x = local - start;
        if (x < 0 || x > 0.5) return 0;
        return Math.max(0, Math.sin(x / 0.5 * Math.PI)) * (0.6 + 0.4 * Math.sin(x * 60));
    }

    private static int argb(double bright, double alpha) {
        int v = (int) Math.max(0, Math.min(255, Math.round(bright * 255)));
        int a = (int) Math.max(0, Math.min(255, Math.round(alpha * 255)));
        return (a << 24) | (v << 16) | (v << 8) | v;
    }

    /** Rotates v around the (normalized) axis by deg. */
    private static double[] rot(double[] v, double[] axis, double deg) {
        if (deg == 0) return v;
        double l = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
        if (l < 1e-9) return v;
        double kx = axis[0] / l, ky = axis[1] / l, kz = axis[2] / l;
        double a = Math.toRadians(deg), c = Math.cos(a), s = Math.sin(a);
        double dot = kx * v[0] + ky * v[1] + kz * v[2];
        double cx = ky * v[2] - kz * v[1], cy = kz * v[0] - kx * v[2], cz = kx * v[1] - ky * v[0];
        return new double[]{v[0] * c + cx * s + kx * dot * (1 - c), v[1] * c + cy * s + ky * dot * (1 - c), v[2] * c + cz * s + kz * dot * (1 - c)};
    }

    /** Cube: strip layer (OptiFine layout) or six face layers. */
    private static void drawCube(class_1921 strip, class_1921[] faces, double R, double[] axis, double ang, int color) {
        double[] d = new double[3];
        double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
        for (int f = 0; f < 6; f++) {
            class_1921 layer = strip != null ? strip : (faces == null ? null : faces[f]);
            if (layer == null) continue;
            int col = f % 3, row = f / 3;
            int segs = 4;        // subdivide faces a bit (better precision at large radius)
            for (int i = 0; i < segs; i++)
                for (int j = 0; j < segs; j++) {
                    double u0 = i / (double) segs, u1 = (i + 1) / (double) segs, v0 = j / (double) segs, v1 = (j + 1) / (double) segs;
                    double[][] p = new double[4][];
                    double[][] uvs = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
                    float[] uv = new float[8];
                    for (int k = 0; k < 4; k++) {
                        SkyGen.dir(f, uvs[k][0], uvs[k][1], d);
                        // cube (not sphere) positions: scale so the largest component is 1
                        double m = Math.max(Math.abs(d[0]), Math.max(Math.abs(d[1]), Math.abs(d[2])));
                        double[] q = rot(new double[]{d[0] / m, d[1] / m, d[2] / m}, axis, ang);
                        p[k] = new double[]{cx + q[0] * R * 0.577, cy + q[1] * R * 0.577, cz + q[2] * R * 0.577};
                        if (strip != null) {
                            double inset = 0.5 / 2048.0;
                            double uu = Math.max(inset, Math.min(1 - inset, uvs[k][0])), vv = Math.max(inset, Math.min(1 - inset, uvs[k][1]));
                            uv[k * 2] = (float) ((col + uu) / 3.0);
                            uv[k * 2 + 1] = (float) ((row + vv) / 2.0);
                        } else {
                            uv[k * 2] = (float) uvs[k][0];
                            uv[k * 2 + 1] = (float) uvs[k][1];
                        }
                    }
                    R3.skyQuad(layer, p[0], p[1], p[2], p[3], uv, color);
                }
        }
    }

    /** Equirectangular panorama on a sphere. */
    private static void drawSphere(class_1921 layer, double R, double ang, int color) {
        int lon = 48, lat = 24;
        double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ(), r = R * 0.9;
        for (int i = 0; i < lon; i++)
            for (int j = 0; j < lat; j++) {
                double u0 = i / (double) lon, u1 = (i + 1) / (double) lon, v0 = j / (double) lat, v1 = (j + 1) / (double) lat;
                double[][] p = {sph(u0, v0, ang), sph(u1, v0, ang), sph(u1, v1, ang), sph(u0, v1, ang)};
                for (double[] q : p) { q[0] = cx + q[0] * r; q[1] = cy + q[1] * r; q[2] = cz + q[2] * r; }
                R3.skyQuad(layer, p[0], p[1], p[2], p[3], new float[]{(float) u0, (float) v0, (float) u1, (float) v0, (float) u1, (float) v1, (float) u0, (float) v1}, color);
            }
    }

    private static double[] sph(double u, double v, double ang) {
        double lonA = u * Math.PI * 2 + Math.toRadians(ang), latA = Math.PI / 2 - v * Math.PI;
        return new double[]{Math.cos(latA) * Math.sin(lonA), Math.sin(latA), -Math.cos(latA) * Math.cos(lonA)};
    }

    private static void shootingStars(double R, double vis) {
        double cx = R3.camX(), cy = R3.camY(), cz = R3.camZ();
        for (int k = 0; k < 2; k++) {
            double period = k == 0 ? 3.1 : 4.7, dur = 1.1;
            double tt = System.currentTimeMillis() / 1000.0 + k * 1.7;
            int slot = (int) Math.floor(tt / period);
            double local = tt - slot * period;
            if (local > dur || dev.spotifyhud.Anim.rnd(slot, 90 + k) > 0.7) continue;
            double p = local / dur;
            double az = dev.spotifyhud.Anim.rnd(slot, 91 + k) * Math.PI * 2, h = 0.35 + dev.spotifyhud.Anim.rnd(slot, 92 + k) * 0.45;
            double len = 0.12, fade = p < 0.2 ? p / 0.2 : (1 - p) / 0.8;
            double hh = h - p * 0.25, a2 = az + p * 0.35;
            double[] head = dir(Math.asin(hh), a2), tail = dir(Math.asin(Math.min(1, hh + len * 0.6)), a2 - len);
            R3.line(cx + tail[0] * R, cy + tail[1] * R, cz + tail[2] * R, cx + head[0] * R, cy + head[1] * R, cz + head[2] * R,
                    R * 0.004, R3.argb(0xB0C8FF, 0), R3.argb(0xFFFFFF, 0.95 * fade * vis));
        }
    }

    private static double[] dir(double elev, double az) {
        double c = Math.cos(elev);
        return new double[]{Math.cos(az) * c, Math.sin(elev), Math.sin(az) * c};
    }
}
