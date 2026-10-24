package dev.lego.visual;

import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import net.minecraft.class_746;

/** Screen overlays drawn on the HUD with Gx (pixel coordinates): cinema bars, vignette, hitmarker. */
public final class Overlays {
    private Overlays() { }

    public static CinemaBars bars;
    public static Vignette vignette;
    public static Hitmarker hitmarker;
    public static MotionBlur motionBlur;

    public static void registerAll() {
        // bars removed
        vignette = Modules.register(new Vignette());
        hitmarker = Modules.register(new Hitmarker());
        motionBlur = Modules.register(new MotionBlur());
    }

    /** Gx must be active (McBackend.begin). W/H in physical pixels. */
    public static void render(int W, int H) {
        if (dev.lego.emote.Cutscenes.active()) return;
        if (motionBlur != null && motionBlur.enabled) motionBlur.draw(W, H);
        if (vignette != null && vignette.enabled) vignette.draw(W, H);
        if (bars != null) bars.draw(W, H);
        if (hitmarker != null && hitmarker.enabled) hitmarker.draw(W, H);
        if (Visuals.totem != null && Visuals.totem.enabled) Visuals.totem.drawScreenIcon(W, H);
    }

    /** Screen effects + a small title while a cutscene emote plays (no letterbox bars: the picture stays full screen). */
    public static void cutscene(int W, int H) {
        String cs = dev.lego.emote.Cutscenes.current();
        if ("cs_darkaura".equals(cs)) darkAura(W, H);
        else if ("cs_evillaugh".equals(cs)) evilLaugh(W, H);
        float t = dev.lego.emote.Cutscenes.time();
        float a = Ease.clamp((t - 0.6f) / 0.7f) * dev.lego.emote.Cutscenes.bars();
        if (a > 0.01f) {
            int S = Gx.S;
            String title = dev.lego.emote.Cutscenes.title().toUpperCase(java.util.Locale.ROOT);
            float size = 7.5f * S, sp = 3f * S, cy = H - 20f * S - (1 - Ease.outCubic(a)) * 4 * S;
            float tw = Gx.spacedWidth(title, size, dev.lego.ui.Fonts.BOLD, sp);
            int lw = Math.round(22 * S * Ease.outCubic(a)), gap = Math.round(9 * S), ly = Math.round(cy);
            Gx.pushAlpha(a);
            Gx.textSpaced(title, W / 2f + S * 0.5f, cy + S * 0.5f, size, dev.lego.ui.Fonts.BOLD, 0x99000000, sp);
            Gx.textSpaced(title, W / 2f, cy, size, dev.lego.ui.Fonts.BOLD, 0xF2FFFFFF, sp);
            int lx0 = Math.round(W / 2f - tw / 2f) - gap - lw, lx1 = Math.round(W / 2f + tw / 2f) + gap;
            Gx.fill(lx0, ly, lw, Math.max(1, S / 2), 0xB3FFFFFF);
            Gx.fill(lx1, ly, lw, Math.max(1, S / 2), 0xB3FFFFFF);
            Gx.popAlpha();
        }
    }

    // ================================================================== "Dunkle Aura" screen fx
    private static void darkAura(int W, int H) {
        int shot = dev.lego.emote.Cutscenes.shot();
        float l = dev.lego.emote.Cutscenes.shotTime(), t = dev.lego.emote.Cutscenes.time();
        float out = Ease.clamp((7.6f - t) / 0.8f), in = Ease.clamp(t / 0.4f), k = in * out;
        // red grade + heavy red/black vignette
        float grade = shot == 3 ? 0.26f : shot == 4 ? 0.3f : 0.16f;
        Gx.fill(0, 0, W, H, Gx.withAlpha(0xFF6A0012, grade * k));
        Gx.Img vig = Gx.painted("da_vig", 512, 512, g -> {
            g.setPaint(new RadialGradientPaint(new Point2D.Float(256, 256), 362, new float[]{0f, 0.42f, 0.75f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                    MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g.fillRect(0, 0, 512, 512);
        });
        if (vig != null) Gx.image(vig, 0, 0, W, H, Gx.withAlpha(0xFFFFFFFF, (0.8f + 0.2f * (float) Math.sin(t * 4)) * k));
        // eye close-up: lens flare star at the eye (screen centre)
        if (shot == 3 || (shot == 4 && l < 0.25f)) {
            float e = shot == 3 ? Ease.outCubic(Ease.clamp((l - 0.35f) / 0.45f)) : 1 - l / 0.25f;
            float pulse = 0.9f + 0.1f * (float) Math.sin(t * 29);
            Gx.Img flare = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
            Gx.Img streak = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
            int cx = W / 2, cy = H / 2;
            if (streak != null) {
                int sw = Math.round(W * 1.3f * e), sh = Math.round(H * 0.06f);
                Gx.image(streak, cx - sw / 2, cy - sh / 2, sw, sh, Gx.withAlpha(0xFFFF2038, 0.9f * e));
            }
            if (flare != null) {
                int fs = Math.round(Math.min(W, H) * 0.9f * e * pulse);
                Gx.image(flare, cx - fs / 2, cy - fs / 2, fs, fs, Gx.withAlpha(0xFFFFFFFF, e));
            }
        }
        // slash streaks wipe across the screen
        if (shot == 4) {
            for (int i = 0; i < 3; i++) {
                float s0 = i * 0.09f, u = Ease.clamp((l - s0) / 0.12f), fade = 1 - Ease.clamp((l - s0 - 0.25f) / 0.4f);
                if (u <= 0 || fade <= 0) continue;
                final int idx = i;
                Gx.Img sl = Gx.painted("da_slash" + i, 1024, 576, g -> paintSlash(g, idx));
                if (sl == null) continue;
                Gx.clip(0, 0, Math.round(W * u), H);
                Gx.image(sl, 0, 0, W, H, Gx.withAlpha(0xFFFFFFFF, fade));
                Gx.unclip();
            }
            float flash = 1 - Ease.clamp(l / 0.12f);
            if (flash > 0) Gx.fill(0, 0, W, H, Gx.withAlpha(0xFFFF4050, 0.45f * flash));
        }
        // hard cut: 2 black frames + a quick red flash
        if (shot > 0 && l < 0.1f) {
            Gx.fill(0, 0, W, H, Gx.withAlpha(0xFF000000, l < 0.04f ? 1f : 1 - (l - 0.04f) / 0.06f));
        }
        // film grain lines
        long seed = (long) (t * 24);
        java.util.Random r = new java.util.Random(seed);
        for (int i = 0; i < 6; i++) {
            int x = r.nextInt(Math.max(1, W));
            Gx.fill(x, 0, Math.max(1, Gx.S / 2), H, Gx.withAlpha(0xFF000000, 0.12f * k));
        }
    }

    // ================================================================== "Böses Lachen" screen fx
    private static void evilLaugh(int W, int H) {
        int shot = dev.lego.emote.Cutscenes.shot();
        float l = dev.lego.emote.Cutscenes.shotTime(), t = dev.lego.emote.Cutscenes.time();
        float out = Ease.clamp((10f - t) / 0.8f), in = Ease.clamp(t / 0.4f), k = in * out;
        float hb = (float) dev.lego.emote.Cutscenes.heartbeat(t), bolt = dev.lego.emote.Cutscenes.bolt(t);
        // dark red grade, a bit crushed; the heartbeat pumps it
        float grade = (shot == 2 ? 0.3f : shot == 3 ? 0.26f : 0.2f) + 0.08f * hb;
        Gx.fill(0, 0, W, H, Gx.withAlpha(0xFF14000A, 0.22f * k));
        Gx.fill(0, 0, W, H, Gx.withAlpha(0xFF7A0010, grade * k));
        Gx.Img vig = Gx.painted("da_vig", 512, 512, g -> {
            g.setPaint(new RadialGradientPaint(new Point2D.Float(256, 256), 362, new float[]{0f, 0.42f, 0.75f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                    MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g.fillRect(0, 0, 512, 512);
        });
        if (vig != null) {
            float grow = 0.06f * hb;   // vignette closes in on every beat
            int gx = Math.round(W * grow), gy = Math.round(H * grow);
            Gx.image(vig, -gx, -gy, W + 2 * gx, H + 2 * gy, Gx.withAlpha(0xFFFFFFFF, (0.85f + 0.15f * hb) * k));
            if (hb > 0.02f) Gx.image(vig, 0, 0, W, H, Gx.withAlpha(0xFFFF2030, 0.35f * hb * k));
        }
        // the eye close-up: red lens flare + anamorphic streak on the eye (screen centre)
        if (shot == 2 || (shot == 3 && l < 0.3f)) {
            float e = shot == 2 ? Ease.outCubic(Ease.clamp((l - 0.25f) / 0.5f)) : 1 - l / 0.3f;
            float pulse = 0.9f + 0.1f * (float) Math.sin(t * 29) + 0.12f * hb;
            Gx.Img flare = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
            Gx.Img streak = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
            int cx = W / 2, cy = H / 2;
            if (streak != null) {
                int sw = Math.round(W * 1.4f * e), sh = Math.round(H * 0.07f);
                Gx.image(streak, cx - sw / 2, cy - sh / 2, sw, sh, Gx.withAlpha(0xFFFF2038, 0.9f * e));
            }
            if (flare != null) {
                int fs = Math.round(Math.min(W, H) * 0.95f * e * pulse);
                Gx.image(flare, cx - fs / 2, cy - fs / 2, fs, fs, Gx.withAlpha(0xFFFFFFFF, e));
            }
        }
        // lightning: white-red flash
        if (bolt > 0.01f) {
            Gx.fill(0, 0, W, H, Gx.withAlpha(0xFFFFD8DC, 0.4f * bolt * k));
            Gx.fill(0, 0, W, H, Gx.withAlpha(0xFFFF2040, 0.25f * bolt * k));
        }
        // hard cut: 2 black frames, then a red flash
        if (shot > 0 && l < 0.14f) {
            float b = l < 0.045f ? 1f : 1 - (l - 0.045f) / 0.095f;
            Gx.fill(0, 0, W, H, Gx.withAlpha(0xFF000000, b));
            if (l >= 0.045f) Gx.fill(0, 0, W, H, Gx.withAlpha(0xFFFF1030, 0.3f * b));
        }
        // film grain lines + dust specks
        java.util.Random r = new java.util.Random((long) (t * 24));
        for (int i = 0; i < 7; i++) {
            int x = r.nextInt(Math.max(1, W));
            Gx.fill(x, 0, Math.max(1, Gx.S / 2), H, Gx.withAlpha(0xFF000000, 0.14f * k));
        }
        for (int i = 0; i < 14; i++) {
            int s = Math.max(1, Gx.S * (1 + r.nextInt(2)));
            Gx.fill(r.nextInt(Math.max(1, W)), r.nextInt(Math.max(1, H)), s, s, Gx.withAlpha(0xFF000000, 0.35f * k));
        }
    }

    private static void paintFlare(java.awt.Graphics2D g) {
        int c = 256;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(c, c), 256, new float[]{0f, 0.05f, 0.18f, 0.5f, 1f},
                new Color[]{new Color(255, 255, 255, 255), new Color(255, 200, 200, 255), new Color(255, 30, 50, 170), new Color(160, 0, 20, 50), new Color(0, 0, 0, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, 512, 512);
        // 4 + 4 point star spikes
        for (int i = 0; i < 8; i++) {
            double an = i * Math.PI / 4 + Math.PI / 8 * 0;
            double len = i % 2 == 0 ? (i % 4 == 0 ? 250 : 170) : 90, w = i % 2 == 0 ? 7 : 4;
            java.awt.geom.Path2D.Double sp = new java.awt.geom.Path2D.Double();
            double dx = Math.cos(an), dy = Math.sin(an), nx = -dy, ny = dx;
            sp.moveTo(c + nx * w, c + ny * w);
            sp.lineTo(c + dx * len, c + dy * len);
            sp.lineTo(c - nx * w, c - ny * w);
            sp.closePath();
            g.setPaint(new java.awt.GradientPaint((float) c, (float) c, new Color(255, 235, 235, 255), (float) (c + dx * len), (float) (c + dy * len), new Color(255, 20, 40, 0)));
            g.fill(sp);
        }
        // small ring ghost
        g.setStroke(new java.awt.BasicStroke(3f));
        g.setColor(new Color(255, 60, 80, 90));
        g.drawOval(c - 70, c - 70, 140, 140);
    }

    private static void paintStreak(java.awt.Graphics2D g) {
        for (int y = 0; y < 64; y++) {
            double v = Math.exp(-Math.pow((y - 31.5) / 6.0, 2));
            for (int x = 0; x < 1024; x++) {
                double h = Math.exp(-Math.pow((x - 511.5) / 300.0, 2));
                int a = (int) Math.round(255 * v * h);
                if (a > 0) { g.setColor(new Color(255, 255, 255, a)); g.fillRect(x, y, 1, 1); }
            }
        }
    }

    private static void paintSlash(java.awt.Graphics2D g, int i) {
        // curved tapered blade streak: dark red edge, hot white core
        double[][] P = {{-60, 470, 1080, 60}, {-40, 150, 1070, 520}, {60, 560, 980, -20}};
        double[] p = P[i];
        double mx = (p[0] + p[2]) / 2, my = (p[1] + p[3]) / 2;
        double dx = p[2] - p[0], dy = p[3] - p[1], len = Math.hypot(dx, dy), nx = -dy / len, ny = dx / len;
        double bend = (i == 1 ? -1 : 1) * 70;
        double cx = mx + nx * bend, cy = my + ny * bend;
        for (int layer = 0; layer < 3; layer++) {
            double w = layer == 0 ? 34 : layer == 1 ? 16 : 5;
            Color col = layer == 0 ? new Color(120, 0, 12, 150) : layer == 1 ? new Color(255, 20, 45, 230) : new Color(255, 230, 230, 255);
            java.awt.geom.Path2D.Double s = new java.awt.geom.Path2D.Double();
            int N = 40;
            for (int j = 0; j <= N; j++) {
                double u = j / (double) N, a = 1 - u;
                double x = a * a * p[0] + 2 * a * u * cx + u * u * p[2], y = a * a * p[1] + 2 * a * u * cy + u * u * p[3];
                double ww = w * Math.sin(Math.PI * u);
                if (j == 0) s.moveTo(x + nx * ww, y + ny * ww); else s.lineTo(x + nx * ww, y + ny * ww);
            }
            for (int j = N; j >= 0; j--) {
                double u = j / (double) N, a = 1 - u;
                double x = a * a * p[0] + 2 * a * u * cx + u * u * p[2], y = a * a * p[1] + 2 * a * u * cy + u * u * p[3];
                double ww = w * Math.sin(Math.PI * u) * 0.35;
                s.lineTo(x - nx * ww, y - ny * ww);
            }
            s.closePath();
            g.setColor(col);
            g.fill(s);
        }
    }

    // ================================================================== cinema bars
    public static final class CinemaBars extends Module {
        final Setting.Num size = add(new Setting.Num("Balkenhöhe", 4, 18, 1, 10, "%"));
        final Setting.Bool onlyZoom = add(new Setting.Bool("Nur beim Zoomen", false));
        private float k = 0;
        CinemaBars() { super("cinemabars", "Kino-Balken", "Schwarze Filmbalken oben und unten (für Aufnahmen)", Category.VISUALS); icon("cinema"); fresh(); }
        void draw(int W, int H) {
            boolean on = enabled && (!onlyZoom.get() || (Visuals.zoom != null && Visuals.zoom.active()));
            k += ((on ? 1 : 0) - k) * Math.min(1, Gx.dt * 7);
            if (k < 0.005f) return;
            int h = Math.round(H * size.getF() / 100f * Ease.outCubic(k));
            Gx.fill(0, 0, W, h, 0xFF000000);
            Gx.fill(0, H - h, W, h, 0xFF000000);
        }
    }

    // ================================================================== vignette
    public static final class Vignette extends Module {
        final Setting.Num strength = add(new Setting.Num("Stärke", 10, 90, 5, 45, "%"));
        final Setting.Bool lowHp = add(new Setting.Bool("Rot pulsieren bei wenig Leben", true));
        final Setting.Num hpLimit = add(new Setting.Num("Leben-Grenze", 2, 12, 1, 6, " ♥"));
        private float red = 0;
        Vignette() { super("vignette", "Vignette", "Weicher dunkler Rand - bei wenig Leben pulsiert er rot", Category.VISUALS); icon("vignette"); fresh(); }
        void draw(int W, int H) {
            Gx.Img img = Gx.painted("vignette", 512, 512, g -> {
                g.setPaint(new RadialGradientPaint(new Point2D.Float(256, 256), 362, new float[]{0f, 0.55f, 0.8f, 1f},
                        new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 120), new Color(0, 0, 0, 255)},
                        MultipleGradientPaint.CycleMethod.NO_CYCLE));
                g.fillRect(0, 0, 512, 512);
            });
            if (img == null) return;
            Gx.image(img, 0, 0, W, H, Gx.withAlpha(0xFFFFFFFF, strength.getF() / 100f));
            class_746 p = Mc.player();
            float target = 0;
            if (lowHp.get() && p != null && !p.method_68878() && !Mc.isSpectator(p)) {
                float hp = Mc.health(p) + Mc.absorption(p);
                if (hp <= hpLimit.get() * 2 && hp > 0) target = 1 - hp / (float) (hpLimit.get() * 2) * 0.6f;
            }
            red += (target - red) * Math.min(1, Gx.dt * 4);
            if (red > 0.01f) {
                float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 1000.0 * 5.5);
                Gx.image(img, 0, 0, W, H, Gx.withAlpha(0xFFFF1E2E, Math.min(1, red * pulse)));
            }
        }
    }

    // ================================================================== hitmarker
    public static final class Hitmarker extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 8));
        final Setting.Num size = add(new Setting.Num("Größe", 4, 16, 1, 8, ""));
        final Setting.Bool killRed = add(new Setting.Bool("Kill = rot", true));
        private long at = 0;
        private boolean kill;
        Hitmarker() { super("hitmarker", "Hitmarker", "Kurzes X am Fadenkreuz, wenn du triffst", Category.VISUALS); icon("hitmarker"); fresh(); }
        public void onHit() { at = System.currentTimeMillis(); kill = false; }
        public void onKill() { at = System.currentTimeMillis(); kill = true; }
        void draw(int W, int H) {
            long age = System.currentTimeMillis() - at;
            if (age > 380) return;
            float t = age / 380f;
            float a = 1 - t * t * t;
            int S = Gx.S;
            float len = size.getF() * S * (0.8f + 0.35f * Ease.outBack(Math.min(1, t * 3)));
            float gap = 3.5f * S;
            int c = kill && killRed.get() ? 0xFFFF3B4C : color.argb();
            int th = Math.max(1, Math.round(1.1f * S));
            int cx = W / 2, cy = H / 2;
            Gx.pushAlpha(a);
            for (int i = 0; i < 4; i++) {
                int sx = (i & 1) == 0 ? 1 : -1, sy = (i & 2) == 0 ? 1 : -1;
                for (float d = gap; d < gap + len; d += 0.7f * th) {
                    int x = Math.round(cx + sx * d * 0.7071f), y = Math.round(cy + sy * d * 0.7071f);
                    Gx.fill(x - th / 2, y - th / 2, th + 1, th + 1, 0x80000000);
                    Gx.fill(x - th / 2, y - th / 2, th, th, c);
                }
            }
            Gx.popAlpha();
        }
    }

    // ================================================================== motion blur
    public static final class MotionBlur extends Module {
        final Setting.Num strength = add(new Setting.Num("Stärke", 10, 100, 5, 50, "%"));
        private float prevYaw = Float.NaN, prevPitch = Float.NaN;
        private double prevX = Double.NaN, prevZ = Double.NaN;
        private float intensity = 0;

        MotionBlur() { super("motionblur", "Motion Blur", "Bewegungsunschärfe am Bildschirmrand bei schneller Bewegung", Category.VISUALS); icon("blur"); fresh(); }

        @Override public void onDisable() {
            prevYaw = Float.NaN;
            intensity = 0;
        }

        void draw(int W, int H) {
            class_746 p = Mc.player();
            if (p == null) { prevYaw = Float.NaN; return; }
            float yaw = p.method_36454(), pitch = p.method_36455();
            double x = Mc.x(p), z = Mc.z(p);
            float target = 0;
            if (!Float.isNaN(prevYaw)) {
                float dYaw = Math.abs(yaw - prevYaw);
                if (dYaw > 180) dYaw = 360 - dYaw;
                float dPitch = Math.abs(pitch - prevPitch);
                float rotSpeed = dYaw + dPitch * 0.6f;
                double dx = x - prevX, dz = z - prevZ;
                float moveSpeed = (float) Math.sqrt(dx * dx + dz * dz) * 20f;
                target = Math.min(1f, (rotSpeed * 0.08f + moveSpeed * 0.15f));
            }
            prevYaw = yaw; prevPitch = pitch; prevX = x; prevZ = z;
            intensity += (target - intensity) * Math.min(1f, Gx.dt * (target > intensity ? 12f : 5f));
            if (intensity < 0.01f) return;
            float a = intensity * strength.getF() / 100f * 0.7f;
            Gx.Img blur = Gx.painted("mblur_radial", 512, 512, g -> {
                g.setPaint(new RadialGradientPaint(new Point2D.Float(256, 256), 362,
                        new float[]{0f, 0.3f, 0.6f, 0.85f, 1f},
                        new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 30), new Color(0, 0, 0, 140), new Color(0, 0, 0, 220)},
                        MultipleGradientPaint.CycleMethod.NO_CYCLE));
                g.fillRect(0, 0, 512, 512);
            });
            if (blur != null) Gx.image(blur, 0, 0, W, H, Gx.withAlpha(0xFFFFFFFF, Math.min(1f, a)));
        }
    }
}
