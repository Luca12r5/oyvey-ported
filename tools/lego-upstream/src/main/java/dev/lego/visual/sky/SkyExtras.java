package dev.lego.visual.sky;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1921;
import net.minecraft.class_2960;

/** Extra sky features: a person / figure from a PNG shown big in the sky (background removed automatically). */
public final class SkyExtras {
    private SkyExtras() { }

    public static SkyPerson person;

    public static void registerAll() { person = Modules.register(new SkyPerson()); }

    public static final class SkyPerson extends Module {
        final Setting.Mode background = add(new Setting.Mode("Hintergrund", 0, "Automatisch entfernen", "Bild ist schon freigestellt", "Nicht entfernen"));
        final Setting.Num tolerance = add(new Setting.Num("Toleranz (Hintergrund)", 10, 120, 5, 48, ""));
        final Setting.Mode look = add(new Setting.Mode("Look", 1, "Normal", "Leuchtend", "Geist", "Sonnenuntergang"));
        final Setting.Num azimuth = add(new Setting.Num("Richtung", 0, 355, 5, 180, "°"));
        final Setting.Num elevation = add(new Setting.Num("Höhe", 0, 70, 1, 22, "°"));
        final Setting.Num size = add(new Setting.Num("Größe", 20, 140, 5, 75, "°"));
        final Setting.Num opacity = add(new Setting.Num("Deckkraft", 10, 100, 5, 85, "%"));
        final Setting.Mode when = add(new Setting.Mode("Zeigen", 0, "Immer", "Nur am Tag", "Nur am Abend", "Nur nachts"));
        final Setting.Bool fadeBottom = add(new Setting.Bool("Unten in die Wolken ausblenden", true));
        final Setting.Bool bob = add(new Setting.Bool("Sanft schweben", true));

        private final AtomicReference<int[][]> pending = new AtomicReference<>();
        private volatile boolean working;
        private String wantKey, loadedKey;
        private class_2960 tex, glowTex;
        private int tw = 1, th = 1;
        private int index = 0;
        private long settingsHash;

        SkyPerson() {
            super("skyperson", "Sky-Figur", "Ein PNG von einem Menschen (oder was du willst) riesig am Himmel - Hintergrund wird entfernt", Category.VISUALS);
            icon("image"); fresh();
            add(new Setting.Action("Bild", "Nächstes Bild", () -> { index++; loadedKey = null; }));
            add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
                Path p = folder();
                Mc.clipboard(p.toString());
                Toasts.show("copy", "Ordner kopiert", "PNG/JPG hier ablegen: " + p);
            }));
            add(new Setting.Action("Neu freistellen", "Neu berechnen", () -> loadedKey = null));
        }

        static Path folder() {
            Path p = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("sky_people");
            try { Files.createDirectories(p); } catch (Throwable ignored) { }
            return p;
        }

        @Override public void onDisable() {
            if (tex != null) Tx.destroy(tex);
            if (glowTex != null) Tx.destroy(glowTex);
            tex = glowTex = null;
            loadedKey = null;
        }

        private void ensure() {
            long h = background.index * 1000L + tolerance.getI() + (fadeBottom.get() ? 100000 : 0);
            String key = index + "|" + h;
            int[][] done = pending.getAndSet(null);
            if (done != null) {
                if (tex != null) Tx.destroy(tex);
                if (glowTex != null) Tx.destroy(glowTex);
                tex = glowTex = null;
                if (done[0].length > 1) {
                    tw = done[2][0];
                    th = done[2][1];
                    tex = class_2960.method_60655("legoclient", "sky/person_" + (System.nanoTime() & 0xFFFFFF));
                    glowTex = class_2960.method_60655("legoclient", "sky/person_glow_" + (System.nanoTime() & 0xFFFFFF));
                    Tx.register(tex, done[0], tw, th, true);
                    Tx.register(glowTex, done[1], tw, th, true);
                }
                loadedKey = wantKey;
            }
            if (key.equals(loadedKey) || working) return;
            wantKey = key;
            working = true;
            final int bg = background.index, tol = tolerance.getI(), idx = index;
            final boolean fade = fadeBottom.get();
            Thread t = new Thread(() -> {
                try { pending.set(process(idx, bg, tol, fade)); }
                catch (Throwable e) { LegoClient.LOG("Sky-Figur: " + e); pending.set(new int[][]{{0}}); }
                finally { working = false; }
            }, "LegoClient-SkyPerson");
            t.setDaemon(true);
            t.start();
        }

        // ------------------------------------------------------------------ image processing (background thread)
        private static int[][] process(int idx, int bgMode, int tol, boolean fade) throws Exception {
            List<File> files = new ArrayList<>();
            File[] list = folder().toFile().listFiles();
            if (list != null) for (File f : list) {
                String n = f.getName().toLowerCase(java.util.Locale.ROOT);
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) files.add(f);
            }
            files.sort(java.util.Comparator.comparing(File::getName));
            if (files.isEmpty()) {
                Toasts.show("image", "Sky-Figur: kein Bild", "Leg ein PNG in " + folder(), 0, 6000);
                return new int[][]{{0}};
            }
            File f = files.get(Math.floorMod(idx, files.size()));
            BufferedImage src = javax.imageio.ImageIO.read(f);
            if (src == null) throw new IllegalStateException("Bild nicht lesbar: " + f.getName());
            int w = src.getWidth(), h = src.getHeight();
            double k = Math.min(1, 1024.0 / Math.max(w, h));
            int W = Math.max(8, (int) Math.round(w * k)), H = Math.max(8, (int) Math.round(h * k));
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(src, 0, 0, W, H, null);
            g.dispose();
            int[] px = img.getRGB(0, 0, W, H, null, 0, W);

            int transparent = 0;
            for (int c : px) if ((c >>> 24) < 250) transparent++;
            boolean hasAlpha = transparent > px.length * 0.02;
            if (bgMode == 0 && !hasAlpha) removeBackground(px, W, H, tol);
            else if (bgMode == 2) for (int i = 0; i < px.length; i++) px[i] |= 0xFF000000;

            if (fade) {
                int from = (int) (H * 0.78);
                for (int y = from; y < H; y++) {
                    double a = 1 - (y - from) / (double) (H - from);
                    a = a * a * (3 - 2 * a);
                    for (int x = 0; x < W; x++) {
                        int c = px[y * W + x];
                        px[y * W + x] = ((int) ((c >>> 24) * a) << 24) | (c & 0xFFFFFF);
                    }
                }
            }
            // glow: blurred white silhouette
            float[] al = new float[W * H];
            for (int i = 0; i < px.length; i++) al[i] = (px[i] >>> 24) / 255f;
            int r = Math.max(4, W / 40);
            for (int pass = 0; pass < 3; pass++) al = blur(al, W, H, r);
            int[] glow = new int[W * H];
            for (int i = 0; i < glow.length; i++) glow[i] = ((int) Math.min(255, al[i] * 255 * 1.4) << 24) | 0xFFFFFF;
            Toasts.show("image", "Sky-Figur bereit", f.getName() + (bgMode == 0 && !hasAlpha ? " · Hintergrund entfernt" : ""), 0, 3000);
            return new int[][]{px, glow, {W, H}};
        }

        /** Flood fill from the border over background-like colors, then feather + decontaminate the edges. */
        static void removeBackground(int[] px, int W, int H, int tol) {
            // background estimate = median of the border pixels
            List<Integer> border = new ArrayList<>();
            for (int x = 0; x < W; x++) { border.add(px[x]); border.add(px[(H - 1) * W + x]); }
            for (int y = 0; y < H; y++) { border.add(px[y * W]); border.add(px[y * W + W - 1]); }
            int[] rs = new int[border.size()], gs = new int[border.size()], bs = new int[border.size()];
            for (int i = 0; i < border.size(); i++) { int c = border.get(i); rs[i] = (c >> 16) & 255; gs[i] = (c >> 8) & 255; bs[i] = c & 255; }
            java.util.Arrays.sort(rs); java.util.Arrays.sort(gs); java.util.Arrays.sort(bs);
            int br = rs[rs.length / 2], bgc = gs[gs.length / 2], bb = bs[bs.length / 2];

            boolean[] bg = new boolean[W * H];
            ArrayDeque<Integer> q = new ArrayDeque<>();
            double t1 = tol, t2 = tol * 1.4;
            for (int x = 0; x < W; x++) { seed(px, bg, q, x, 0, W, br, bgc, bb, t2); seed(px, bg, q, x, H - 1, W, br, bgc, bb, t2); }
            for (int y = 0; y < H; y++) { seed(px, bg, q, 0, y, W, br, bgc, bb, t2); seed(px, bg, q, W - 1, y, W, br, bgc, bb, t2); }
            int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
            while (!q.isEmpty()) {
                int i = q.poll();
                int x = i % W, y = i / W;
                for (int d = 0; d < 4; d++) {
                    int nx = x + dx[d], ny = y + dy[d];
                    if (nx < 0 || ny < 0 || nx >= W || ny >= H) continue;
                    int j = ny * W + nx;
                    if (bg[j]) continue;
                    // similar to the background colour, or a smooth continuation of the neighbour (gradients, soft shadows)
                    double dj = dist(px[j], br, bgc, bb);
                    if (dj < t1 || (dist(px[j], px[i]) < t1 * 0.3 && dj < t2 && dist(px[i], br, bgc, bb) < t1 * 0.6)) {
                        bg[j] = true;
                        q.add(j);
                    }
                }
            }
            // keep only big foreground blobs (removes specks)
            int[] comp = new int[W * H];
            java.util.Arrays.fill(comp, -1);
            List<Integer> sizes = new ArrayList<>();
            for (int i = 0; i < W * H; i++) {
                if (bg[i] || comp[i] >= 0) continue;
                int id = sizes.size(), n = 0;
                comp[i] = id; q.add(i);
                while (!q.isEmpty()) {
                    int c = q.poll(); n++;
                    int x = c % W, y = c / W;
                    for (int d = 0; d < 4; d++) {
                        int nx = x + dx[d], ny = y + dy[d];
                        if (nx < 0 || ny < 0 || nx >= W || ny >= H) continue;
                        int j = ny * W + nx;
                        if (!bg[j] && comp[j] < 0) { comp[j] = id; q.add(j); }
                    }
                }
                sizes.add(n);
            }
            int biggest = 0;
            for (int s : sizes) biggest = Math.max(biggest, s);
            float[] a = new float[W * H];
            for (int i = 0; i < W * H; i++) a[i] = !bg[i] && sizes.get(comp[i]) >= Math.max(40, biggest * 0.04) ? 1f : 0f;
            a = blur(a, W, H, 1);
            for (int i = 0; i < W * H; i++) {
                float al = a[i];
                // tighten the edge a little
                al = Math.max(0, Math.min(1, (al - 0.2f) / 0.75f));
                int c = px[i];
                int r = (c >> 16) & 255, gg = (c >> 8) & 255, b = c & 255;
                if (al > 0.02f && al < 0.98f) {           // remove background colour from edge pixels
                    r = clamp((r - br * (1 - al)) / al);
                    gg = clamp((gg - bgc * (1 - al)) / al);
                    b = clamp((b - bb * (1 - al)) / al);
                }
                px[i] = ((int) (al * 255) << 24) | (r << 16) | (gg << 8) | b;
            }
        }

        private static void seed(int[] px, boolean[] bg, ArrayDeque<Integer> q, int x, int y, int W, int br, int bgc, int bb, double t) {
            int i = y * W + x;
            if (!bg[i] && dist(px[i], br, bgc, bb) < t) { bg[i] = true; q.add(i); }
        }

        private static double dist(int c, int r, int g, int b) {
            int dr = ((c >> 16) & 255) - r, dg = ((c >> 8) & 255) - g, db = (c & 255) - b;
            return Math.sqrt(dr * dr + dg * dg + db * db);
        }

        private static double dist(int c1, int c2) { return dist(c1, (c2 >> 16) & 255, (c2 >> 8) & 255, c2 & 255); }

        private static int clamp(float v) { return Math.max(0, Math.min(255, Math.round(v))); }

        private static float[] blur(float[] src, int W, int H, int r) {
            float[] tmp = new float[W * H], out = new float[W * H];
            for (int y = 0; y < H; y++) {
                float s = 0;
                for (int x = -r; x <= r; x++) s += src[y * W + Math.max(0, Math.min(W - 1, x))];
                for (int x = 0; x < W; x++) {
                    tmp[y * W + x] = s / (2 * r + 1);
                    s += src[y * W + Math.min(W - 1, x + r + 1)] - src[y * W + Math.max(0, x - r)];
                }
            }
            for (int x = 0; x < W; x++) {
                float s = 0;
                for (int y = -r; y <= r; y++) s += tmp[Math.max(0, Math.min(H - 1, y)) * W + x];
                for (int y = 0; y < H; y++) {
                    out[y * W + x] = s / (2 * r + 1);
                    s += tmp[Math.min(H - 1, y + r + 1) * W + x] - tmp[Math.max(0, y - r) * W + x];
                }
            }
            return out;
        }

        // ------------------------------------------------------------------ rendering (inside R3 begin/end, before terrain)
        public void render() {
            ensure();
            if (tex == null) return;
            double[] w = CustomSky.dayWeights(Mc.timeOfDay());
            double vis;
            switch (when.index) {
                case 1: vis = w[0]; break;
                case 2: vis = w[1]; break;
                case 3: vis = w[2]; break;
                default: vis = 1;
            }
            if (vis < 0.01) return;
            double far = 256;
            try { far = Mc.mc().field_1773.method_32796(); } catch (Throwable ignored) { }
            double D = R3.skyRadius > 0 ? R3.skyRadius * 0.85 : Math.max(60, far * 0.42);
            double t = System.currentTimeMillis() / 1000.0;
            double el = Math.toRadians(elevation.get() + (bob.get() ? Math.sin(t * 0.35) * 1.2 : 0));
            double az = Math.toRadians(azimuth.get());
            double[] dir = {Math.sin(az) * Math.cos(el), Math.sin(el), -Math.cos(az) * Math.cos(el)};
            double[] right = {Math.cos(az), 0, Math.sin(az)};
            double[] up = {dir[1] * right[2] - dir[2] * right[1], dir[2] * right[0] - dir[0] * right[2], dir[0] * right[1] - dir[1] * right[0]};
            if (up[1] < 0) { up[0] = -up[0]; up[1] = -up[1]; up[2] = -up[2]; }
            double hh = D * Math.tan(Math.toRadians(size.get()) / 2), hw = hh * tw / (double) th;
            double cx = R3.camX() + dir[0] * D, cy = R3.camY() + dir[1] * D, cz = R3.camZ() + dir[2] * D;
            double alpha = opacity.get() / 100.0 * vis;
            int tint, glowTint;
            switch (look.index) {
                case 1: tint = R3.argb(0xFFFFFF, alpha); glowTint = R3.argb(0xFFF1D6, alpha * 0.45); break;
                case 2: tint = R3.argb(0xBFD8FF, alpha * 0.55); glowTint = R3.argb(0x9CC6FF, alpha * 0.55); break;
                case 3: tint = R3.argb(0xFFC7A0, alpha * 0.9); glowTint = R3.argb(0xFF8A4A, alpha * 0.5); break;
                default: tint = R3.argb(0xFFFFFF, alpha); glowTint = 0;
            }
            if (glowTint != 0) quad(R3.skyLayer(glowTex), cx, cy, cz, right, up, hw * 1.12, hh * 1.12, glowTint);
            quad(R3.skyLayer(tex), cx, cy, cz, right, up, hw, hh, tint);
        }

        private static void quad(class_1921 layer, double cx, double cy, double cz, double[] r, double[] u, double hw, double hh, int c) {
            double[] p0 = {cx - r[0] * hw + u[0] * hh, cy - r[1] * hw + u[1] * hh, cz - r[2] * hw + u[2] * hh};
            double[] p1 = {cx + r[0] * hw + u[0] * hh, cy + r[1] * hw + u[1] * hh, cz + r[2] * hw + u[2] * hh};
            double[] p2 = {cx + r[0] * hw - u[0] * hh, cy + r[1] * hw - u[1] * hh, cz + r[2] * hw - u[2] * hh};
            double[] p3 = {cx - r[0] * hw - u[0] * hh, cy - r[1] * hw - u[1] * hh, cz - r[2] * hw - u[2] * hh};
            R3.skyQuad(layer, p0, p1, p2, p3, new float[]{0, 0, 1, 0, 1, 1, 0, 1}, c);
        }
    }
}
