package dev.lego.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Fonts;
import dev.lego.ui.Tx;
import net.minecraft.class_10055;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_5250;
import net.minecraft.class_746;

/**
 * Your own name tag, the original Minecraft one, animated: moving colours, letters that pulse/glitch, optional LEGO badge.
 * A Spotify card (cover, song, artist, progress) floats above the name. Only you see it.
 */
public final class NameFx {
    private NameFx() { }

    public static Tag tag;

    public static void registerAll() { tag = Modules.register(new Tag()); }

    /** World pass: the Spotify card above the name. */
    public static void render(float td) {
        if (tag != null && tag.enabled) { tag.renderBrick(td); tag.renderCard(td); }
        if (tag != null && dev.lego.net.LegoSync.active()) tag.renderOthers(td);
    }

    /** Other players (Lego-Sync): their own name tag style, as they set it in their client. */
    public static void applyOther(class_10055 state, Object player) {
        try {
            if (!(player instanceof net.minecraft.class_1297 e)) return;
            java.util.Map<String, Object> r = dev.lego.net.LegoSync.remote(e.method_5667());
            if (r == null || state.field_53337 == null) return;
            String text = dev.spotifyhud.Json.str(r, "t", "");
            String s = text.isBlank() ? Mc.name(e) : text;
            state.field_53337 = build(s, (int) dev.spotifyhud.Json.num(r, "s", 0), (int) dev.spotifyhud.Json.num(r, "c1", 5), (int) dev.spotifyhud.Json.num(r, "c2", 7),
                    dev.spotifyhud.Json.bool(r, "b", true), dev.spotifyhud.Json.bool(r, "m", true), dev.spotifyhud.Json.num(r, "sp", 1));
        } catch (Throwable ignored) { }
    }

    /** Called from the player render state (PlayerRendererMixin): makes your own name tag show, animated. */
    public static void apply(class_10055 state) {
        if (tag == null || !tag.enabled) return;
        class_746 me = Mc.player();
        if (me == null || Mc.firstPerson() || me.method_5767()) return;
        state.field_53337 = tag.component(me);
        if (state.field_53338 == null) state.field_53338 = new class_243(0.0, Mc.height(me) + 0.5, 0.0);
    }

    private static int pal(int idx, double phase) {
        int[] p = Setting.Color.PALETTE;
        int i = Math.max(0, Math.min(p.length - 1, idx));
        if (i == p.length - 1) {
            double t = System.currentTimeMillis() / 2600.0 + phase;
            return java.awt.Color.HSBtoRGB((float) (t - Math.floor(t)), 0.75f, 1f) & 0xFFFFFF;
        }
        return p[i] & 0xFFFFFF;
    }

    static int colorAt(int style, int c1i, int c2i, int i, double t) {
        int c1 = pal(c1i, i * 0.07), c2 = pal(c2i, i * 0.07 + 0.3);
        switch (style) {
            case 0: return java.awt.Color.HSBtoRGB((float) ((t * 0.25 + i * 0.06) % 1.0), 0.7f, 1f) & 0xFFFFFF;
            case 1: return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 2 + i * 0.4));
            case 2: return lerp(0xFF3B1E, 0xFFD23B, 0.5 + 0.5 * Math.sin(t * 7 + i * 1.7));
            case 3: return lerp(0x7CF7FF, 0xFFFFFF, 0.5 + 0.5 * Math.sin(t * 2.5 + i * 0.5));
            case 4: return lerp(0x303030, c1, 0.55 + 0.45 * Math.sin(t * 3));
            case 5: return ((int) (t * 8 + i * 7) % 9 == 0) ? 0xFFFFFF : c1;
            case 6: return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 1.5 - i * 0.35));
            default: return lerp(0x0B8A3A, 0x3BFF7C, 0.5 + 0.5 * Math.sin(t * 9 + i * 2.3));
        }
    }

    /** Animated name component (shared by your own tag and synced tags of other players). */
    static class_2561 build(String s, int style, int c1, int c2, boolean boldAll, boolean moving, double speed) {
        if (s.length() > 24) s = s.substring(0, 24);
        double t = System.currentTimeMillis() / 1000.0 * speed;
        int n = s.length();
        class_5250 root = class_2561.method_43470("");
        // a "push" runs through the letters: the bold letter is wider, so its neighbours visibly slide
        double pos = (t * 5) % (n + 4) - 2;
        int glitch = (int) ((long) (t * 10) % Math.max(1, n));
        for (int i = 0; i < n; i++) {
            final int rgb = colorAt(style, c1, c2, i, t);
            final boolean bold = boldAll || (moving && Math.abs(i - pos) < 1.0);
            final boolean obf = moving && style == 5 && i == glitch && s.charAt(i) != ' ';
            root.method_10852(class_2561.method_43470(String.valueOf(s.charAt(i))).method_27694(st -> {
                class_2583 r = st.method_36139(rgb);
                if (bold) r = r.method_10982(Boolean.TRUE);
                if (obf) r = r.method_36141(Boolean.TRUE);
                return r;
            }));
        }
        return root;
    }

    private static int lerp(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t), g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t), bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    public static final class Tag extends Module {
        final Setting.Text text = add(new Setting.Text("Text (leer = dein Name)", "", 24, "Name"));
        final Setting.Mode style = add(new Setting.Mode("Animation", 0, "Regenbogen", "Welle", "Feuer", "Eis", "Neon-Puls", "Glitzer", "Verlauf", "Matrix"));
        final Setting.Bool moving = add(new Setting.Bool("Buchstaben bewegen sich", true));
        final Setting.Num speed = add(new Setting.Num("Tempo", 0.3, 3, 0.1, 1, "x"));
        final Setting.Color color = add(new Setting.Color("Farbe", 5));
        final Setting.Color color2 = add(new Setting.Color("Farbe 2", 7));
        final Setting.Bool bold = add(new Setting.Bool("Fetter Name", true));
        final Setting.Bool badge = add(new Setting.Bool("LEGO-Symbol", true));
        final Setting.Bool song = add(new Setting.Bool("Spotify-Karte über dem Namen", true));
        final Setting.Num cardSize = add(new Setting.Num("Karte: Größe", 0.5, 2, 0.1, 1, "x"));
        final Setting.Num cardLift = add(new Setting.Num("Karte: Höhe", -0.5, 1.5, 0.05, 0, " Blöcke"));
        final Setting.Num cardSide = add(new Setting.Num("Karte: seitlich", -1.5, 1.5, 0.05, 0, " Blöcke"));
        final Setting.Num cardAlpha = add(new Setting.Num("Karte: Deckkraft", 20, 100, 5, 100, "%"));
        final Setting.Color cardColor = add(new Setting.Color("Karte: Farbe", 0));
        final Setting.Num scale = add(new Setting.Num("Größe", 0.5, 3.0, 0.1, 1.0, "x"));
        final Setting.Bool outline = add(new Setting.Bool("Textumrandung", false));
        final Setting.Bool shadow = add(new Setting.Bool("Textschatten", true));
        final Setting.Bool background = add(new Setting.Bool("Hintergrund", true));
        final Setting.Num bgOpacity = add(new Setting.Num("Hintergrund-Deckkraft", 0, 100, 5, 40, "%"));

        Tag() { super("namefx", "Namensschild", "Dein Name-Tag animiert (nur für dich sichtbar) + Spotify-Karte darüber", Category.VISUALS); icon("tag"); fresh(); }

        private int colorAt(int i, double t) {
            int c1 = color.argb(i * 0.07) & 0xFFFFFF, c2 = color2.argb(i * 0.07 + 0.3) & 0xFFFFFF;
            switch (style.index) {
                case 0: return java.awt.Color.HSBtoRGB((float) ((t * 0.25 + i * 0.06) % 1.0), 0.7f, 1f) & 0xFFFFFF;
                case 1: return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 2 + i * 0.4));
                case 2: return lerp(0xFF3B1E, 0xFFD23B, 0.5 + 0.5 * Math.sin(t * 7 + i * 1.7));
                case 3: return lerp(0x7CF7FF, 0xFFFFFF, 0.5 + 0.5 * Math.sin(t * 2.5 + i * 0.5));
                case 4: return lerp(0x303030, c1, 0.55 + 0.45 * Math.sin(t * 3));
                case 5: return ((int) (t * 8 + i * 7) % 9 == 0) ? 0xFFFFFF : c1;
                case 6: return lerp(c1, c2, 0.5 + 0.5 * Math.sin(t * 1.5 - i * 0.35));
                default: return lerp(0x0B8A3A, 0x3BFF7C, 0.5 + 0.5 * Math.sin(t * 9 + i * 2.3));
            }
        }

        class_2561 component(class_746 me) {
            String s = text.get() == null || text.get().isBlank() ? Mc.name(me) : text.get();
            return build(s, style.index, color.index, color2.index, bold.get(), moving.get(), speed.get());
        }

        public float getScale() { return (float) scale.get(); }
        public boolean hasOutline() { return outline.get(); }
        public boolean hasShadow() { return shadow.get(); }
        public boolean hasBackground() { return background.get(); }
        public float getBgOpacity() { return (float) (bgOpacity.get() / 100.0); }

        /** What other Lego-Sync users get to see. */
        public java.util.Map<String, Object> syncState() {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("t", text.get() == null ? "" : text.get());
            m.put("s", (long) style.index);
            m.put("c1", (long) color.index);
            m.put("c2", (long) color2.index);
            m.put("b", bold.get());
            m.put("m", moving.get());
            m.put("sp", speed.get());
            m.put("lego", badge.get());
            m.put("sc", scale.get());
            return m;
        }
        // ------------------------------------------------------------------ LEGO brick symbol next to the name
        private class_2960 brickTex;

        private static double mcWidth(String s, boolean bold) {
            double w = 0;
            for (char c : s.toCharArray()) {
                int cw = "il!.,:;|'".indexOf(c) >= 0 ? 2 : "tI[] ".indexOf(c) >= 0 ? 4 : "fk<>(){}*\"".indexOf(c) >= 0 ? 5 : 6;
                w += cw + 1 + (bold ? 1 : 0);
            }
            return w - 1;
        }

        private class_2960 brick() {
            if (brickTex != null) return brickTex;
            int w = 160, h = 128;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                dev.lego.ui.Gx.hints(g);
                // studs
                for (int i = 0; i < 2; i++) {
                    int sx = 22 + i * 62;
                    g.setColor(new Color(0xB5, 0x00, 0x08));
                    g.fill(new RoundRectangle2D.Double(sx, 6, 54, 34, 14, 14));
                    g.setColor(new Color(0xFF, 0x4A, 0x52));
                    g.fill(new RoundRectangle2D.Double(sx + 3, 4, 48, 28, 14, 14));
                }
                // body
                g.setColor(new Color(0xB5, 0x00, 0x08));
                g.fill(new RoundRectangle2D.Double(6, 28, w - 12, h - 32, 22, 22));
                g.setColor(new Color(0xE3, 0x00, 0x0B));
                g.fill(new RoundRectangle2D.Double(6, 24, w - 12, h - 38, 22, 22));
                g.setColor(new Color(255, 255, 255, 70));
                g.fill(new RoundRectangle2D.Double(16, 32, w - 32, 10, 8, 8));
                g.setColor(new Color(0, 0, 0, 120));
                g.setStroke(new java.awt.BasicStroke(4f));
                g.draw(new RoundRectangle2D.Double(6, 24, w - 12, h - 38, 22, 22));
            } finally { g.dispose(); }
            class_2960 id = class_2960.method_60655("legoclient", "namebrick");
            Tx.register(id, img.getRGB(0, 0, w, h, null, 0, w), w, h, true);
            brickTex = id;
            return id;
        }

        void renderOthers(float td) {
            class_746 me = Mc.player();
            for (net.minecraft.class_1657 pl : Mc.players()) {
                if (pl == me) continue;
                java.util.Map<String, Object> r = dev.lego.net.LegoSync.remote(pl.method_5667());
                if (r == null || !dev.spotifyhud.Json.bool(r, "lego", true)) continue;
                double[] p = Mc.lerpPos(pl, td);
                double dx = p[0] - R3.camX(), dz = p[2] - R3.camZ();
                if (dx * dx + dz * dz > 48 * 48) continue;
                String t2 = dev.spotifyhud.Json.str(r, "t", "");
                String s = t2.isBlank() ? Mc.name(pl) : t2;
                double half = mcWidth(s, dev.spotifyhud.Json.bool(r, "b", true)) * 0.025 / 2;
                double ih = 0.22, iw = ih * 160 / 128.0, off = -(half + 0.04 + iw / 2);
                double yaw = Math.toRadians(R3.camYaw());
                R3.billboard(brick(), p[0] + off * -Math.cos(yaw), p[1] + Mc.height(pl) + 0.5 + 0.14, p[2] + off * -Math.sin(yaw), iw, ih, 0, 0, 1, 1, R3.argb(0xFFFFFF, 1.0));
            }
        }

        void renderBrick(float td) {
            class_746 me = Mc.player();
            if (!badge.get() || me == null || Mc.firstPerson() || me.method_5767()) return;
            String s = text.get() == null || text.get().isBlank() ? Mc.name(me) : text.get();
            if (s.length() > 24) s = s.substring(0, 24);
            double half = mcWidth(s, bold.get()) * 0.025 / 2;
            double ih = 0.22, iw = ih * 160 / 128.0;
            double off = -(half + 0.04 + iw / 2);
            double[] p = Mc.lerpPos(me, td);
            double yaw = Math.toRadians(R3.camYaw());
            double y = p[1] + Mc.height(me) + 0.5 + 0.14;
            R3.billboard(brick(), p[0] + off * -Math.cos(yaw), y, p[2] + off * -Math.sin(yaw), iw, ih, 0, 0, 1, 1, R3.argb(0xFFFFFF, 1.0));
        }
        // ------------------------------------------------------------------ Spotify card
        private static final ExecutorService PAINT = Executors.newSingleThreadExecutor(r -> {
            Thread th = new Thread(r, "Lego-NameCard");
            th.setDaemon(true);
            th.setPriority(Thread.NORM_PRIORITY - 1);
            return th;
        });
        private static final int CW = 640, CH = 176;
        private class_2960 cardTex;
        private int cardN = 0;
        private volatile int[] ready;
        private volatile boolean busy;
        private String paintedKey = "", requestedKey = "";
        private long fadeStart = 0;
        private boolean wasShowing;

        void renderCard(float td) {
            class_746 me = Mc.player();
            if (!song.get() || me == null || Mc.firstPerson() || me.method_5767()) return;
            dev.spotifyhud.PlayerState st = null;
            try { if (dev.spotifyhud.SpotifyHudMod.media() != null) st = dev.spotifyhud.SpotifyHudMod.media().state(); } catch (Throwable ignored) { }
            boolean show = st != null && st.hasTrack && st.title != null && !st.title.isEmpty();
            long now = System.currentTimeMillis();
            if (show && !wasShowing) fadeStart = now;
            wasShowing = show;
            if (!show) return;
            // take over a finished card
            int[] px = ready;
            if (px != null) {
                ready = null;
                class_2960 old = cardTex;
                class_2960 id = class_2960.method_60655("legoclient", "namecard/" + (cardN++));
                Tx.register(id, px, CW, CH, true);
                cardTex = id;
                paintedKey = requestedKey;
                if (old != null) Tx.destroy(old);
            }
            // request a new one when the content changed (progress once per second)
            long sec = st.currentProgress() / 1000;
            Object art = null;
            int artV = 0;
            try { art = dev.spotifyhud.SpotifyHudMod.media().art(); artV = dev.spotifyhud.SpotifyHudMod.media().artVersion(); } catch (Throwable ignored) { }
            final int accent = cardColor.argb() & 0xFFFFFF;
            String key = st.title + "|" + st.artist + "|" + artV + "|" + (st.playing ? sec : "p" + sec) + "|" + st.playing + "|" + accent;
            if (!key.equals(requestedKey) && !busy) {
                busy = true;
                requestedKey = key;
                final dev.spotifyhud.PlayerState fs = st;
                final Object fart = art;
                final long prog = st.currentProgress();
                PAINT.execute(() -> {
                    try { ready = paint(fs, fart, prog, accent); } catch (Throwable t) { dev.lego.LegoClient.LOG("Namenskarte: " + t); } finally { busy = false; }
                });
            }
            if (cardTex == null) return;
            double[] p = Mc.lerpPos(me, td);
            double k = cardSize.get();
            double w = 1.5 * k, h = w * CH / CW;
            double fade = Math.min(1, (now - fadeStart) / 400.0);
            double bob = Math.sin(now / 900.0) * 0.015;
            // sits directly on top of the name tag (tag is ~0.25 high above its attachment point)
            double y = p[1] + Mc.height(me) + 0.5 + 0.32 + h * 0.5 + bob + cardLift.get();
            double yaw = Math.toRadians(R3.camYaw());
            double side = cardSide.get();
            R3.billboard(cardTex, p[0] + side * -Math.cos(yaw), y, p[2] + side * -Math.sin(yaw), w, h, 0, 0, 1, 1, R3.argb(0xFFFFFF, fade * cardAlpha.get() / 100.0));
        }

        private static int[] paint(dev.spotifyhud.PlayerState s, Object art, long progress, int accent) {
            final Color acc = new Color(accent);
            BufferedImage img = new BufferedImage(CW, CH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                dev.lego.ui.Gx.hints(g);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                // body
                g.setColor(new Color(16, 17, 22, 238));
                g.fill(new RoundRectangle2D.Double(4, 4, CW - 8, CH - 8, 44, 44));
                g.setColor(new Color(255, 255, 255, 34));
                g.setStroke(new java.awt.BasicStroke(3f));
                g.draw(new RoundRectangle2D.Double(5.5, 5.5, CW - 11, CH - 11, 42, 42));
                // cover
                int cs = 128, cx = 24, cy = (CH - cs) / 2;
                RoundRectangle2D cover = new RoundRectangle2D.Double(cx, cy, cs, cs, 26, 26);
                if (art instanceof BufferedImage) {
                    java.awt.Shape old = g.getClip();
                    g.setClip(cover);
                    g.drawImage((BufferedImage) art, cx, cy, cs, cs, null);
                    g.setClip(old);
                } else {
                    g.setColor(new Color(0x1E, 0xD7, 0x60));
                    g.fill(cover);
                    g.setColor(new Color(11, 11, 11));
                    g.setFont(Fonts.get(Fonts.BOLD, 70));
                    g.drawString("♪", cx + 38, cy + 90);
                }
                int tx = cx + cs + 24, maxW = CW - tx - 70;
                // title + artist
                g.setColor(Color.WHITE);
                g.setFont(Fonts.get(Fonts.BOLD, 40));
                g.drawString(Fonts.ellipsize(s.title, Fonts.BOLD, 40, maxW), tx, 66);
                g.setColor(new Color(255, 255, 255, 170));
                g.setFont(Fonts.get(Fonts.MEDIUM, 30));
                g.drawString(Fonts.ellipsize(s.artist == null ? "" : s.artist, Fonts.MEDIUM, 30, maxW), tx, 106);
                // progress
                int by = 130, bw = CW - tx - 30;
                g.setColor(new Color(255, 255, 255, 46));
                g.fill(new RoundRectangle2D.Double(tx, by, bw, 10, 10, 10));
                double f = s.durationMs > 0 ? Math.max(0, Math.min(1, progress / (double) s.durationMs)) : 0;
                if (f > 0) {
                    g.setColor(acc);
                    g.fill(new RoundRectangle2D.Double(tx, by, Math.max(10, bw * f), 10, 10, 10));
                }
                // play / pause badge
                int px = CW - 56, py = 36;
                g.setColor(acc);
                g.fillOval(px, py, 34, 34);
                g.setColor(new Color(11, 11, 11));
                if (s.playing) { g.fillRect(px + 10, py + 9, 5, 16); g.fillRect(px + 19, py + 9, 5, 16); }
                else g.fillPolygon(new int[]{px + 12, px + 12, px + 25}, new int[]{py + 8, py + 26, py + 17}, 3);
            } finally { g.dispose(); }
            return img.getRGB(0, 0, CW, CH, null, 0, CW);
        }
    }
}