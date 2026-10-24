package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.emote.Cutscenes;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D.Float;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.player.LocalPlayer;

public final class Overlays {
   public static Overlays.CinemaBars bars;
   public static Overlays.Vignette vignette;
   public static Overlays.Hitmarker hitmarker;
   public static Overlays.MotionBlur motionBlur;

   private Overlays() {
   }

   public static void registerAll() {
      vignette = Modules.register(new Overlays.Vignette());
      hitmarker = Modules.register(new Overlays.Hitmarker());
      motionBlur = Modules.register(new Overlays.MotionBlur());
   }

   public static void render(int W, int H) {
      if (!Cutscenes.active()) {
         if (motionBlur != null && motionBlur.enabled) {
            motionBlur.draw(W, H);
         }

         if (vignette != null && vignette.enabled) {
            vignette.draw(W, H);
         }

         if (bars != null) {
            bars.draw(W, H);
         }

         if (hitmarker != null && hitmarker.enabled) {
            hitmarker.draw(W, H);
         }

         if (Visuals.totem != null && Visuals.totem.enabled) {
            Visuals.totem.drawScreenIcon(W, H);
         }
      }
   }

   public static void cutscene(int W, int H) {
      String cs = Cutscenes.current();
      if ("cs_darkaura".equals(cs)) {
         darkAura(W, H);
      } else if ("cs_evillaugh".equals(cs)) {
         evilLaugh(W, H);
      }

      float t = Cutscenes.time();
      float a = Ease.clamp((t - 0.6F) / 0.7F) * Cutscenes.bars();
      if (a > 0.01F) {
         int S = Gx.S;
         String title = Cutscenes.title().toUpperCase(Locale.ROOT);
         float size = 7.5F * S;
         float sp = 3.0F * S;
         float cy = H - 20.0F * S - (1.0F - Ease.outCubic(a)) * 4.0F * S;
         float tw = Gx.spacedWidth(title, size, 3, sp);
         int lw = Math.round(22 * S * Ease.outCubic(a));
         int gap = Math.round((float)(9 * S));
         int ly = Math.round(cy);
         Gx.pushAlpha(a);
         Gx.textSpaced(title, W / 2.0F + S * 0.5F, cy + S * 0.5F, size, 3, -1728053248, sp);
         Gx.textSpaced(title, W / 2.0F, cy, size, 3, -218103809, sp);
         int lx0 = Math.round(W / 2.0F - tw / 2.0F) - gap - lw;
         int lx1 = Math.round(W / 2.0F + tw / 2.0F) + gap;
         Gx.fill(lx0, ly, lw, Math.max(1, S / 2), -1275068417);
         Gx.fill(lx1, ly, lw, Math.max(1, S / 2), -1275068417);
         Gx.popAlpha();
      }
   }

   private static void darkAura(int W, int H) {
      int shot = Cutscenes.shot();
      float l = Cutscenes.shotTime();
      float t = Cutscenes.time();
      float out = Ease.clamp((7.6F - t) / 0.8F);
      float in = Ease.clamp(t / 0.4F);
      float k = in * out;
      float grade = shot == 3 ? 0.26F : (shot == 4 ? 0.3F : 0.16F);
      Gx.fill(0, 0, W, H, Gx.withAlpha(-9830382, grade * k));
      Gx.Img vig = Gx.painted(
         "da_vig",
         512,
         512,
         g -> {
            g.setPaint(
               new RadialGradientPaint(
                  new Float(256.0F, 256.0F),
                  362.0F,
                  new float[]{0.0F, 0.42F, 0.75F, 1.0F},
                  new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                  CycleMethod.NO_CYCLE
               )
            );
            g.fillRect(0, 0, 512, 512);
         }
      );
      if (vig != null) {
         Gx.image(vig, 0, 0, W, H, Gx.withAlpha(-1, (0.8F + 0.2F * (float)Math.sin(t * 4.0F)) * k));
      }

      if (shot == 3 || shot == 4 && l < 0.25F) {
         float e = shot == 3 ? Ease.outCubic(Ease.clamp((l - 0.35F) / 0.45F)) : 1.0F - l / 0.25F;
         float pulse = 0.9F + 0.1F * (float)Math.sin(t * 29.0F);
         Gx.Img flare = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
         Gx.Img streak = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
         int cx = W / 2;
         int cy = H / 2;
         if (streak != null) {
            int sw = Math.round(W * 1.3F * e);
            int sh = Math.round(H * 0.06F);
            Gx.image(streak, cx - sw / 2, cy - sh / 2, sw, sh, Gx.withAlpha(-57288, 0.9F * e));
         }

         if (flare != null) {
            int fs = Math.round(Math.min(W, H) * 0.9F * e * pulse);
            Gx.image(flare, cx - fs / 2, cy - fs / 2, fs, fs, Gx.withAlpha(-1, e));
         }
      }

      if (shot == 4) {
         for (int i = 0; i < 3; i++) {
            float s0 = i * 0.09F;
            float u = Ease.clamp((l - s0) / 0.12F);
            float fade = 1.0F - Ease.clamp((l - s0 - 0.25F) / 0.4F);
            if (!(u <= 0.0F) && !(fade <= 0.0F)) {
               int idx = i;
               Gx.Img sl = Gx.painted("da_slash" + i, 1024, 576, g -> paintSlash(g, idx));
               if (sl != null) {
                  Gx.clip(0, 0, Math.round(W * u), H);
                  Gx.image(sl, 0, 0, W, H, Gx.withAlpha(-1, fade));
                  Gx.unclip();
               }
            }
         }

         float flash = 1.0F - Ease.clamp(l / 0.12F);
         if (flash > 0.0F) {
            Gx.fill(0, 0, W, H, Gx.withAlpha(-49072, 0.45F * flash));
         }
      }

      if (shot > 0 && l < 0.1F) {
         Gx.fill(0, 0, W, H, Gx.withAlpha(-16777216, l < 0.04F ? 1.0F : 1.0F - (l - 0.04F) / 0.06F));
      }

      long seed = (long)(t * 24.0F);
      Random r = new Random(seed);

      for (int ix = 0; ix < 6; ix++) {
         int x = r.nextInt(Math.max(1, W));
         Gx.fill(x, 0, Math.max(1, Gx.S / 2), H, Gx.withAlpha(-16777216, 0.12F * k));
      }
   }

   private static void evilLaugh(int W, int H) {
      int shot = Cutscenes.shot();
      float l = Cutscenes.shotTime();
      float t = Cutscenes.time();
      float out = Ease.clamp((10.0F - t) / 0.8F);
      float in = Ease.clamp(t / 0.4F);
      float k = in * out;
      float hb = (float)Cutscenes.heartbeat(t);
      float bolt = Cutscenes.bolt(t);
      float grade = (shot == 2 ? 0.3F : (shot == 3 ? 0.26F : 0.2F)) + 0.08F * hb;
      Gx.fill(0, 0, W, H, Gx.withAlpha(-15466486, 0.22F * k));
      Gx.fill(0, 0, W, H, Gx.withAlpha(-8781808, grade * k));
      Gx.Img vig = Gx.painted(
         "da_vig",
         512,
         512,
         g -> {
            g.setPaint(
               new RadialGradientPaint(
                  new Float(256.0F, 256.0F),
                  362.0F,
                  new float[]{0.0F, 0.42F, 0.75F, 1.0F},
                  new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                  CycleMethod.NO_CYCLE
               )
            );
            g.fillRect(0, 0, 512, 512);
         }
      );
      if (vig != null) {
         float grow = 0.06F * hb;
         int gx = Math.round(W * grow);
         int gy = Math.round(H * grow);
         Gx.image(vig, -gx, -gy, W + 2 * gx, H + 2 * gy, Gx.withAlpha(-1, (0.85F + 0.15F * hb) * k));
         if (hb > 0.02F) {
            Gx.image(vig, 0, 0, W, H, Gx.withAlpha(-57296, 0.35F * hb * k));
         }
      }

      if (shot == 2 || shot == 3 && l < 0.3F) {
         float e = shot == 2 ? Ease.outCubic(Ease.clamp((l - 0.25F) / 0.5F)) : 1.0F - l / 0.3F;
         float pulse = 0.9F + 0.1F * (float)Math.sin(t * 29.0F) + 0.12F * hb;
         Gx.Img flare = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
         Gx.Img streak = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
         int cx = W / 2;
         int cy = H / 2;
         if (streak != null) {
            int sw = Math.round(W * 1.4F * e);
            int sh = Math.round(H * 0.07F);
            Gx.image(streak, cx - sw / 2, cy - sh / 2, sw, sh, Gx.withAlpha(-57288, 0.9F * e));
         }

         if (flare != null) {
            int fs = Math.round(Math.min(W, H) * 0.95F * e * pulse);
            Gx.image(flare, cx - fs / 2, cy - fs / 2, fs, fs, Gx.withAlpha(-1, e));
         }
      }

      if (bolt > 0.01F) {
         Gx.fill(0, 0, W, H, Gx.withAlpha(-10020, 0.4F * bolt * k));
         Gx.fill(0, 0, W, H, Gx.withAlpha(-57280, 0.25F * bolt * k));
      }

      if (shot > 0 && l < 0.14F) {
         float b = l < 0.045F ? 1.0F : 1.0F - (l - 0.045F) / 0.095F;
         Gx.fill(0, 0, W, H, Gx.withAlpha(-16777216, b));
         if (l >= 0.045F) {
            Gx.fill(0, 0, W, H, Gx.withAlpha(-61392, 0.3F * b));
         }
      }

      Random r = new Random((long)(t * 24.0F));

      for (int i = 0; i < 7; i++) {
         int x = r.nextInt(Math.max(1, W));
         Gx.fill(x, 0, Math.max(1, Gx.S / 2), H, Gx.withAlpha(-16777216, 0.14F * k));
      }

      for (int i = 0; i < 14; i++) {
         int s = Math.max(1, Gx.S * (1 + r.nextInt(2)));
         Gx.fill(r.nextInt(Math.max(1, W)), r.nextInt(Math.max(1, H)), s, s, Gx.withAlpha(-16777216, 0.35F * k));
      }
   }

   private static void paintFlare(Graphics2D g) {
      int c = 256;
      g.setPaint(
         new RadialGradientPaint(
            new Float(c, c),
            256.0F,
            new float[]{0.0F, 0.05F, 0.18F, 0.5F, 1.0F},
            new Color[]{
               new Color(255, 255, 255, 255), new Color(255, 200, 200, 255), new Color(255, 30, 50, 170), new Color(160, 0, 20, 50), new Color(0, 0, 0, 0)
            },
            CycleMethod.NO_CYCLE
         )
      );
      g.fillRect(0, 0, 512, 512);

      for (int i = 0; i < 8; i++) {
         double an = i * Math.PI / 4.0 + 0.0;
         double len = i % 2 == 0 ? (i % 4 == 0 ? 250 : 170) : 90.0;
         double w = i % 2 == 0 ? 7.0 : 4.0;
         Double sp = new Double();
         double dx = Math.cos(an);
         double dy = Math.sin(an);
         double nx = -dy;
         sp.moveTo(c + nx * w, c + dx * w);
         sp.lineTo(c + dx * len, c + dy * len);
         sp.lineTo(c - nx * w, c - dx * w);
         sp.closePath();
         g.setPaint(new GradientPaint(c, c, new Color(255, 235, 235, 255), (float)(c + dx * len), (float)(c + dy * len), new Color(255, 20, 40, 0)));
         g.fill(sp);
      }

      g.setStroke(new BasicStroke(3.0F));
      g.setColor(new Color(255, 60, 80, 90));
      g.drawOval(c - 70, c - 70, 140, 140);
   }

   private static void paintStreak(Graphics2D g) {
      for (int y = 0; y < 64; y++) {
         double v = Math.exp(-Math.pow((y - 31.5) / 6.0, 2.0));

         for (int x = 0; x < 1024; x++) {
            double h = Math.exp(-Math.pow((x - 511.5) / 300.0, 2.0));
            int a = (int)Math.round(255.0 * v * h);
            if (a > 0) {
               g.setColor(new Color(255, 255, 255, a));
               g.fillRect(x, y, 1, 1);
            }
         }
      }
   }

   private static void paintSlash(Graphics2D g, int i) {
      double[][] P = new double[][]{{-60.0, 470.0, 1080.0, 60.0}, {-40.0, 150.0, 1070.0, 520.0}, {60.0, 560.0, 980.0, -20.0}};
      double[] p = P[i];
      double mx = (p[0] + p[2]) / 2.0;
      double my = (p[1] + p[3]) / 2.0;
      double dx = p[2] - p[0];
      double dy = p[3] - p[1];
      double len = Math.hypot(dx, dy);
      double nx = -dy / len;
      double ny = dx / len;
      double bend = (i == 1 ? -1 : 1) * 70;
      double cx = mx + nx * bend;
      double cy = my + ny * bend;

      for (int layer = 0; layer < 3; layer++) {
         double w = layer == 0 ? 34.0 : (layer == 1 ? 16.0 : 5.0);
         Color col = layer == 0 ? new Color(120, 0, 12, 150) : (layer == 1 ? new Color(255, 20, 45, 230) : new Color(255, 230, 230, 255));
         Double s = new Double();
         int N = 40;

         for (int j = 0; j <= N; j++) {
            double u = (double)j / N;
            double a = 1.0 - u;
            double x = a * a * p[0] + 2.0 * a * u * cx + u * u * p[2];
            double y = a * a * p[1] + 2.0 * a * u * cy + u * u * p[3];
            double ww = w * Math.sin(Math.PI * u);
            if (j == 0) {
               s.moveTo(x + nx * ww, y + ny * ww);
            } else {
               s.lineTo(x + nx * ww, y + ny * ww);
            }
         }

         for (int jx = N; jx >= 0; jx--) {
            double u = (double)jx / N;
            double a = 1.0 - u;
            double x = a * a * p[0] + 2.0 * a * u * cx + u * u * p[2];
            double y = a * a * p[1] + 2.0 * a * u * cy + u * u * p[3];
            double ww = w * Math.sin(Math.PI * u) * 0.35;
            s.lineTo(x - nx * ww, y - ny * ww);
         }

         s.closePath();
         g.setColor(col);
         g.fill(s);
      }
   }

   public static final class CinemaBars extends Module {
      final Setting.Num size = this.add(new Setting.Num("Balkenhöhe", 4.0, 18.0, 1.0, 10.0, "%"));
      final Setting.Bool onlyZoom = this.add(new Setting.Bool("Nur beim Zoomen", false));
      private float k = 0.0F;

      CinemaBars() {
         super("cinemabars", "Kino-Balken", "Schwarze Filmbalken oben und unten (für Aufnahmen)", Category.VISUALS);
         this.icon("cinema");
         this.fresh();
      }

      void draw(int W, int H) {
         boolean on = this.enabled && (!this.onlyZoom.get() || Visuals.zoom != null && Visuals.zoom.active());
         this.k = this.k + ((on ? 1 : 0) - this.k) * Math.min(1.0F, Gx.dt * 7.0F);
         if (!(this.k < 0.005F)) {
            int h = Math.round(H * this.size.getF() / 100.0F * Ease.outCubic(this.k));
            Gx.fill(0, 0, W, h, -16777216);
            Gx.fill(0, H - h, W, h, -16777216);
         }
      }
   }

   public static final class Hitmarker extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 8));
      final Setting.Num size = this.add(new Setting.Num("Größe", 4.0, 16.0, 1.0, 8.0, ""));
      final Setting.Bool killRed = this.add(new Setting.Bool("Kill = rot", true));
      private long at = 0L;
      private boolean kill;

      Hitmarker() {
         super("hitmarker", "Hitmarker", "Kurzes X am Fadenkreuz, wenn du triffst", Category.VISUALS);
         this.icon("hitmarker");
         this.fresh();
      }

      public void onHit() {
         this.at = System.currentTimeMillis();
         this.kill = false;
      }

      public void onKill() {
         this.at = System.currentTimeMillis();
         this.kill = true;
      }

      void draw(int W, int H) {
         long age = System.currentTimeMillis() - this.at;
         if (age <= 380L) {
            float t = (float)age / 380.0F;
            float a = 1.0F - t * t * t;
            int S = Gx.S;
            float len = this.size.getF() * S * (0.8F + 0.35F * Ease.outBack(Math.min(1.0F, t * 3.0F)));
            float gap = 3.5F * S;
            int c = this.kill && this.killRed.get() ? -50356 : this.color.argb();
            int th = Math.max(1, Math.round(1.1F * S));
            int cx = W / 2;
            int cy = H / 2;
            Gx.pushAlpha(a);

            for (int i = 0; i < 4; i++) {
               int sx = (i & 1) == 0 ? 1 : -1;
               int sy = (i & 2) == 0 ? 1 : -1;

               for (float d = gap; d < gap + len; d += 0.7F * th) {
                  int x = Math.round(cx + sx * d * 0.7071F);
                  int y = Math.round(cy + sy * d * 0.7071F);
                  Gx.fill(x - th / 2, y - th / 2, th + 1, th + 1, Integer.MIN_VALUE);
                  Gx.fill(x - th / 2, y - th / 2, th, th, c);
               }
            }

            Gx.popAlpha();
         }
      }
   }

   public static final class MotionBlur extends Module {
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 50.0, "%"));
      private float prevYaw = java.lang.Float.NaN;
      private float prevPitch = java.lang.Float.NaN;
      private double prevX = java.lang.Double.NaN;
      private double prevZ = java.lang.Double.NaN;
      private float intensity = 0.0F;

      MotionBlur() {
         super("motionblur", "Motion Blur", "Bewegungsunschärfe am Bildschirmrand bei schneller Bewegung", Category.VISUALS);
         this.icon("blur");
         this.fresh();
      }

      @Override
      public void onDisable() {
         this.prevYaw = java.lang.Float.NaN;
         this.intensity = 0.0F;
      }

      void draw(int W, int H) {
         LocalPlayer p = Mc.player();
         if (p == null) {
            this.prevYaw = java.lang.Float.NaN;
         } else {
            float yaw = p.getYRot();
            float pitch = p.getXRot();
            double x = Mc.x(p);
            double z = Mc.z(p);
            float target = 0.0F;
            if (!java.lang.Float.isNaN(this.prevYaw)) {
               float dYaw = Math.abs(yaw - this.prevYaw);
               if (dYaw > 180.0F) {
                  dYaw = 360.0F - dYaw;
               }

               float dPitch = Math.abs(pitch - this.prevPitch);
               float rotSpeed = dYaw + dPitch * 0.6F;
               double dx = x - this.prevX;
               double dz = z - this.prevZ;
               float moveSpeed = (float)Math.sqrt(dx * dx + dz * dz) * 20.0F;
               target = Math.min(1.0F, rotSpeed * 0.08F + moveSpeed * 0.15F);
            }

            this.prevYaw = yaw;
            this.prevPitch = pitch;
            this.prevX = x;
            this.prevZ = z;
            this.intensity = this.intensity + (target - this.intensity) * Math.min(1.0F, Gx.dt * (target > this.intensity ? 12.0F : 5.0F));
            if (!(this.intensity < 0.01F)) {
               float a = this.intensity * this.strength.getF() / 100.0F * 0.7F;
               Gx.Img blur = Gx.painted(
                  "mblur_radial",
                  512,
                  512,
                  g -> {
                     g.setPaint(
                        new RadialGradientPaint(
                           new Float(256.0F, 256.0F),
                           362.0F,
                           new float[]{0.0F, 0.3F, 0.6F, 0.85F, 1.0F},
                           new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 30), new Color(0, 0, 0, 140), new Color(0, 0, 0, 220)},
                           CycleMethod.NO_CYCLE
                        )
                     );
                     g.fillRect(0, 0, 512, 512);
                  }
               );
               if (blur != null) {
                  Gx.image(blur, 0, 0, W, H, Gx.withAlpha(-1, Math.min(1.0F, a)));
               }
            }
         }
      }
   }

   public static final class Vignette extends Module {
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 90.0, 5.0, 45.0, "%"));
      final Setting.Bool lowHp = this.add(new Setting.Bool("Rot pulsieren bei wenig Leben", true));
      final Setting.Num hpLimit = this.add(new Setting.Num("Leben-Grenze", 2.0, 12.0, 1.0, 6.0, " ♥"));
      private float red = 0.0F;

      Vignette() {
         super("vignette", "Vignette", "Weicher dunkler Rand - bei wenig Leben pulsiert er rot", Category.VISUALS);
         this.icon("vignette");
         this.fresh();
      }

      void draw(int W, int H) {
         Gx.Img img = Gx.painted(
            "vignette",
            512,
            512,
            g -> {
               g.setPaint(
                  new RadialGradientPaint(
                     new Float(256.0F, 256.0F),
                     362.0F,
                     new float[]{0.0F, 0.55F, 0.8F, 1.0F},
                     new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 120), new Color(0, 0, 0, 255)},
                     CycleMethod.NO_CYCLE
                  )
               );
               g.fillRect(0, 0, 512, 512);
            }
         );
         if (img != null) {
            Gx.image(img, 0, 0, W, H, Gx.withAlpha(-1, this.strength.getF() / 100.0F));
            LocalPlayer p = Mc.player();
            float target = 0.0F;
            if (this.lowHp.get() && p != null && !p.isCreative() && !Mc.isSpectator(p)) {
               float hp = Mc.health(p) + Mc.absorption(p);
               if (hp <= this.hpLimit.get() * 2.0 && hp > 0.0F) {
                  target = 1.0F - hp / (float)(this.hpLimit.get() * 2.0) * 0.6F;
               }
            }

            this.red = this.red + (target - this.red) * Math.min(1.0F, Gx.dt * 4.0F);
            if (this.red > 0.01F) {
               float pulse = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 1000.0 * 5.5);
               Gx.image(img, 0, 0, W, H, Gx.withAlpha(-57810, Math.min(1.0F, this.red * pulse)));
            }
         }
      }
   }
}
