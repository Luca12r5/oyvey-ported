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
   public static Overlays.VrLook vrLook;

   private Overlays() {
   }

   public static void registerAll() {
      bars = Modules.register(new Overlays.CinemaBars());
      vignette = Modules.register(new Overlays.Vignette());
      hitmarker = Modules.register(new Overlays.Hitmarker());
      // The VR look is now the real Visor headset (dev.visor, key H).
   }

   public static void render(int var0, int var1) {
      if (!Cutscenes.active()) {
         if (vignette != null && vignette.enabled) {
            vignette.draw(var0, var1);
         }

         if (bars != null) {
            bars.draw(var0, var1);
         }

         if (hitmarker != null && hitmarker.enabled) {
            hitmarker.draw(var0, var1);
         }

         if (vrLook != null && vrLook.enabled) {
            vrLook.draw(var0, var1);
         }
      }
   }

   public static void cutscene(int var0, int var1) {
      String var2 = Cutscenes.current();
      if ("cs_darkaura".equals(var2)) {
         darkAura(var0, var1);
      } else if ("cs_evillaugh".equals(var2)) {
         evilLaugh(var0, var1);
      }

      float var3 = Cutscenes.time();
      float var4 = Ease.clamp((var3 - 0.6F) / 0.7F) * Cutscenes.bars();
      if (var4 > 0.01F) {
         int var5 = Gx.S;
         String var6 = Cutscenes.title().toUpperCase(Locale.ROOT);
         float var7 = 7.5F * var5;
         float var8 = 3.0F * var5;
         float var9 = var1 - 20.0F * var5 - (1.0F - Ease.outCubic(var4)) * 4.0F * var5;
         float var10 = Gx.spacedWidth(var6, var7, 3, var8);
         int var11 = Math.round(22 * var5 * Ease.outCubic(var4));
         int var12 = Math.round((float)(9 * var5));
         int var13 = Math.round(var9);
         Gx.pushAlpha(var4);
         Gx.textSpaced(var6, var0 / 2.0F + var5 * 0.5F, var9 + var5 * 0.5F, var7, 3, -1728053248, var8);
         Gx.textSpaced(var6, var0 / 2.0F, var9, var7, 3, -218103809, var8);
         int var14 = Math.round(var0 / 2.0F - var10 / 2.0F) - var12 - var11;
         int var15 = Math.round(var0 / 2.0F + var10 / 2.0F) + var12;
         Gx.fill(var14, var13, var11, Math.max(1, var5 / 2), -1275068417);
         Gx.fill(var15, var13, var11, Math.max(1, var5 / 2), -1275068417);
         Gx.popAlpha();
      }
   }

   private static void darkAura(int var0, int var1) {
      int var2 = Cutscenes.shot();
      float var3 = Cutscenes.shotTime();
      float var4 = Cutscenes.time();
      float var5 = Ease.clamp((7.6F - var4) / 0.8F);
      float var6 = Ease.clamp(var4 / 0.4F);
      float var7 = var6 * var5;
      float var8 = var2 == 3 ? 0.26F : (var2 == 4 ? 0.3F : 0.16F);
      Gx.fill(0, 0, var0, var1, Gx.withAlpha(-9830382, var8 * var7));
      Gx.Img var9 = Gx.painted(
         "da_vig",
         512,
         512,
         var0x -> {
            var0x.setPaint(
               new RadialGradientPaint(
                  new Float(256.0F, 256.0F),
                  362.0F,
                  new float[]{0.0F, 0.42F, 0.75F, 1.0F},
                  new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                  CycleMethod.NO_CYCLE
               )
            );
            var0x.fillRect(0, 0, 512, 512);
         }
      );
      if (var9 != null) {
         Gx.image(var9, 0, 0, var0, var1, Gx.withAlpha(-1, (0.8F + 0.2F * (float)Math.sin(var4 * 4.0F)) * var7));
      }

      if (var2 == 3 || var2 == 4 && var3 < 0.25F) {
         float var10 = var2 == 3 ? Ease.outCubic(Ease.clamp((var3 - 0.35F) / 0.45F)) : 1.0F - var3 / 0.25F;
         float var11 = 0.9F + 0.1F * (float)Math.sin(var4 * 29.0F);
         Gx.Img var12 = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
         Gx.Img var13 = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
         int var14 = var0 / 2;
         int var15 = var1 / 2;
         if (var13 != null) {
            int var16 = Math.round(var0 * 1.3F * var10);
            int var17 = Math.round(var1 * 0.06F);
            Gx.image(var13, var14 - var16 / 2, var15 - var17 / 2, var16, var17, Gx.withAlpha(-57288, 0.9F * var10));
         }

         if (var12 != null) {
            int var29 = Math.round(Math.min(var0, var1) * 0.9F * var10 * var11);
            Gx.image(var12, var14 - var29 / 2, var15 - var29 / 2, var29, var29, Gx.withAlpha(-1, var10));
         }
      }

      if (var2 == 4) {
         for (int var18 = 0; var18 < 3; var18++) {
            float var21 = var18 * 0.09F;
            float var22 = Ease.clamp((var3 - var21) / 0.12F);
            float var24 = 1.0F - Ease.clamp((var3 - var21 - 0.25F) / 0.4F);
            if (!(var22 <= 0.0F) && !(var24 <= 0.0F)) {
               int var26 = var18;
               Gx.Img var28 = Gx.painted("da_slash" + var18, 1024, 576, var1x -> paintSlash(var1x, var26));
               if (var28 != null) {
                  Gx.clip(0, 0, Math.round(var0 * var22), var1);
                  Gx.image(var28, 0, 0, var0, var1, Gx.withAlpha(-1, var24));
                  Gx.unclip();
               }
            }
         }

         float var19 = 1.0F - Ease.clamp(var3 / 0.12F);
         if (var19 > 0.0F) {
            Gx.fill(0, 0, var0, var1, Gx.withAlpha(-49072, 0.45F * var19));
         }
      }

      if (var2 > 0 && var3 < 0.1F) {
         Gx.fill(0, 0, var0, var1, Gx.withAlpha(-16777216, var3 < 0.04F ? 1.0F : 1.0F - (var3 - 0.04F) / 0.06F));
      }

      long var20 = (long)(var4 * 24.0F);
      Random var23 = new Random(var20);

      for (int var25 = 0; var25 < 6; var25++) {
         int var27 = var23.nextInt(Math.max(1, var0));
         Gx.fill(var27, 0, Math.max(1, Gx.S / 2), var1, Gx.withAlpha(-16777216, 0.12F * var7));
      }
   }

   private static void evilLaugh(int var0, int var1) {
      int var2 = Cutscenes.shot();
      float var3 = Cutscenes.shotTime();
      float var4 = Cutscenes.time();
      float var5 = Ease.clamp((10.0F - var4) / 0.8F);
      float var6 = Ease.clamp(var4 / 0.4F);
      float var7 = var6 * var5;
      float var8 = (float)Cutscenes.heartbeat(var4);
      float var9 = Cutscenes.bolt(var4);
      float var10 = (var2 == 2 ? 0.3F : (var2 == 3 ? 0.26F : 0.2F)) + 0.08F * var8;
      Gx.fill(0, 0, var0, var1, Gx.withAlpha(-15466486, 0.22F * var7));
      Gx.fill(0, 0, var0, var1, Gx.withAlpha(-8781808, var10 * var7));
      Gx.Img var11 = Gx.painted(
         "da_vig",
         512,
         512,
         var0x -> {
            var0x.setPaint(
               new RadialGradientPaint(
                  new Float(256.0F, 256.0F),
                  362.0F,
                  new float[]{0.0F, 0.42F, 0.75F, 1.0F},
                  new Color[]{new Color(0, 0, 0, 0), new Color(40, 0, 6, 40), new Color(60, 0, 8, 190), new Color(0, 0, 0, 255)},
                  CycleMethod.NO_CYCLE
               )
            );
            var0x.fillRect(0, 0, 512, 512);
         }
      );
      if (var11 != null) {
         float var12 = 0.06F * var8;
         int var13 = Math.round(var0 * var12);
         int var14 = Math.round(var1 * var12);
         Gx.image(var11, -var13, -var14, var0 + 2 * var13, var1 + 2 * var14, Gx.withAlpha(-1, (0.85F + 0.15F * var8) * var7));
         if (var8 > 0.02F) {
            Gx.image(var11, 0, 0, var0, var1, Gx.withAlpha(-57296, 0.35F * var8 * var7));
         }
      }

      if (var2 == 2 || var2 == 3 && var3 < 0.3F) {
         float var20 = var2 == 2 ? Ease.outCubic(Ease.clamp((var3 - 0.25F) / 0.5F)) : 1.0F - var3 / 0.3F;
         float var23 = 0.9F + 0.1F * (float)Math.sin(var4 * 29.0F) + 0.12F * var8;
         Gx.Img var26 = Gx.painted("da_flare", 512, 512, Overlays::paintFlare);
         Gx.Img var15 = Gx.painted("da_streak", 1024, 64, Overlays::paintStreak);
         int var16 = var0 / 2;
         int var17 = var1 / 2;
         if (var15 != null) {
            int var18 = Math.round(var0 * 1.4F * var20);
            int var19 = Math.round(var1 * 0.07F);
            Gx.image(var15, var16 - var18 / 2, var17 - var19 / 2, var18, var19, Gx.withAlpha(-57288, 0.9F * var20));
         }

         if (var26 != null) {
            int var29 = Math.round(Math.min(var0, var1) * 0.95F * var20 * var23);
            Gx.image(var26, var16 - var29 / 2, var17 - var29 / 2, var29, var29, Gx.withAlpha(-1, var20));
         }
      }

      if (var9 > 0.01F) {
         Gx.fill(0, 0, var0, var1, Gx.withAlpha(-10020, 0.4F * var9 * var7));
         Gx.fill(0, 0, var0, var1, Gx.withAlpha(-57280, 0.25F * var9 * var7));
      }

      if (var2 > 0 && var3 < 0.14F) {
         float var21 = var3 < 0.045F ? 1.0F : 1.0F - (var3 - 0.045F) / 0.095F;
         Gx.fill(0, 0, var0, var1, Gx.withAlpha(-16777216, var21));
         if (var3 >= 0.045F) {
            Gx.fill(0, 0, var0, var1, Gx.withAlpha(-61392, 0.3F * var21));
         }
      }

      Random var22 = new Random((long)(var4 * 24.0F));

      for (int var24 = 0; var24 < 7; var24++) {
         int var27 = var22.nextInt(Math.max(1, var0));
         Gx.fill(var27, 0, Math.max(1, Gx.S / 2), var1, Gx.withAlpha(-16777216, 0.14F * var7));
      }

      for (int var25 = 0; var25 < 14; var25++) {
         int var28 = Math.max(1, Gx.S * (1 + var22.nextInt(2)));
         Gx.fill(var22.nextInt(Math.max(1, var0)), var22.nextInt(Math.max(1, var1)), var28, var28, Gx.withAlpha(-16777216, 0.35F * var7));
      }
   }

   private static void paintFlare(Graphics2D var0) {
      short var1 = 256;
      var0.setPaint(
         new RadialGradientPaint(
            new Float(var1, var1),
            256.0F,
            new float[]{0.0F, 0.05F, 0.18F, 0.5F, 1.0F},
            new Color[]{
               new Color(255, 255, 255, 255), new Color(255, 200, 200, 255), new Color(255, 30, 50, 170), new Color(160, 0, 20, 50), new Color(0, 0, 0, 0)
            },
            CycleMethod.NO_CYCLE
         )
      );
      var0.fillRect(0, 0, 512, 512);

      for (int var2 = 0; var2 < 8; var2++) {
         double var3 = var2 * Math.PI / 4.0 + 0.0;
         double var5 = var2 % 2 == 0 ? (var2 % 4 == 0 ? 250 : 170) : 90.0;
         double var7 = var2 % 2 == 0 ? 7.0 : 4.0;
         Double var9 = new Double();
         double var10 = Math.cos(var3);
         double var12 = Math.sin(var3);
         double var14 = -var12;
         var9.moveTo(var1 + var14 * var7, var1 + var10 * var7);
         var9.lineTo(var1 + var10 * var5, var1 + var12 * var5);
         var9.lineTo(var1 - var14 * var7, var1 - var10 * var7);
         var9.closePath();
         var0.setPaint(
            new GradientPaint(var1, var1, new Color(255, 235, 235, 255), (float)(var1 + var10 * var5), (float)(var1 + var12 * var5), new Color(255, 20, 40, 0))
         );
         var0.fill(var9);
      }

      var0.setStroke(new BasicStroke(3.0F));
      var0.setColor(new Color(255, 60, 80, 90));
      var0.drawOval(var1 - 70, var1 - 70, 140, 140);
   }

   private static void paintStreak(Graphics2D var0) {
      for (int var1 = 0; var1 < 64; var1++) {
         double var2 = Math.exp(-Math.pow((var1 - 31.5) / 6.0, 2.0));

         for (int var4 = 0; var4 < 1024; var4++) {
            double var5 = Math.exp(-Math.pow((var4 - 511.5) / 300.0, 2.0));
            int var7 = (int)Math.round(255.0 * var2 * var5);
            if (var7 > 0) {
               var0.setColor(new Color(255, 255, 255, var7));
               var0.fillRect(var4, var1, 1, 1);
            }
         }
      }
   }

   private static void paintSlash(Graphics2D var0, int var1) {
      double[][] var2 = new double[][]{{-60.0, 470.0, 1080.0, 60.0}, {-40.0, 150.0, 1070.0, 520.0}, {60.0, 560.0, 980.0, -20.0}};
      double[] var3 = var2[var1];
      double var4 = (var3[0] + var3[2]) / 2.0;
      double var6 = (var3[1] + var3[3]) / 2.0;
      double var8 = var3[2] - var3[0];
      double var10 = var3[3] - var3[1];
      double var12 = Math.hypot(var8, var10);
      double var14 = -var10 / var12;
      double var16 = var8 / var12;
      double var18 = (var1 == 1 ? -1 : 1) * 70;
      double var20 = var4 + var14 * var18;
      double var22 = var6 + var16 * var18;

      for (int var24 = 0; var24 < 3; var24++) {
         double var25 = var24 == 0 ? 34.0 : (var24 == 1 ? 16.0 : 5.0);
         Color var27 = var24 == 0 ? new Color(120, 0, 12, 150) : (var24 == 1 ? new Color(255, 20, 45, 230) : new Color(255, 230, 230, 255));
         Double var28 = new Double();
         byte var29 = 40;

         for (int var30 = 0; var30 <= var29; var30++) {
            double var31 = (double)var30 / var29;
            double var33 = 1.0 - var31;
            double var35 = var33 * var33 * var3[0] + 2.0 * var33 * var31 * var20 + var31 * var31 * var3[2];
            double var37 = var33 * var33 * var3[1] + 2.0 * var33 * var31 * var22 + var31 * var31 * var3[3];
            double var39 = var25 * Math.sin(Math.PI * var31);
            if (var30 == 0) {
               var28.moveTo(var35 + var14 * var39, var37 + var16 * var39);
            } else {
               var28.lineTo(var35 + var14 * var39, var37 + var16 * var39);
            }
         }

         for (int var41 = var29; var41 >= 0; var41--) {
            double var42 = (double)var41 / var29;
            double var43 = 1.0 - var42;
            double var44 = var43 * var43 * var3[0] + 2.0 * var43 * var42 * var20 + var42 * var42 * var3[2];
            double var45 = var43 * var43 * var3[1] + 2.0 * var43 * var42 * var22 + var42 * var42 * var3[3];
            double var46 = var25 * Math.sin(Math.PI * var42) * 0.35;
            var28.lineTo(var44 - var14 * var46, var45 - var16 * var46);
         }

         var28.closePath();
         var0.setColor(var27);
         var0.fill(var28);
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

      void draw(int var1, int var2) {
         boolean var3 = this.enabled && (!this.onlyZoom.get() || Visuals.zoom != null && Visuals.zoom.active());
         this.k = this.k + ((var3 ? 1 : 0) - this.k) * Math.min(1.0F, Gx.dt * 7.0F);
         if (!(this.k < 0.005F)) {
            int var4 = Math.round(var2 * this.size.getF() / 100.0F * Ease.outCubic(this.k));
            Gx.fill(0, 0, var1, var4, -16777216);
            Gx.fill(0, var2 - var4, var1, var4, -16777216);
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

      void draw(int var1, int var2) {
         long var3 = System.currentTimeMillis() - this.at;
         if (var3 <= 380L) {
            float var5 = (float)var3 / 380.0F;
            float var6 = 1.0F - var5 * var5 * var5;
            int var7 = Gx.S;
            float var8 = this.size.getF() * var7 * (0.8F + 0.35F * Ease.outBack(Math.min(1.0F, var5 * 3.0F)));
            float var9 = 3.5F * var7;
            int var10 = this.kill && this.killRed.get() ? -50356 : this.color.argb();
            int var11 = Math.max(1, Math.round(1.1F * var7));
            int var12 = var1 / 2;
            int var13 = var2 / 2;
            Gx.pushAlpha(var6);

            for (int var14 = 0; var14 < 4; var14++) {
               int var15 = (var14 & 1) == 0 ? 1 : -1;
               int var16 = (var14 & 2) == 0 ? 1 : -1;

               for (float var17 = var9; var17 < var9 + var8; var17 += 0.7F * var11) {
                  int var18 = Math.round(var12 + var15 * var17 * 0.7071F);
                  int var19 = Math.round(var13 + var16 * var17 * 0.7071F);
                  Gx.fill(var18 - var11 / 2, var19 - var11 / 2, var11 + 1, var11 + 1, Integer.MIN_VALUE);
                  Gx.fill(var18 - var11 / 2, var19 - var11 / 2, var11, var11, var10);
               }
            }

            Gx.popAlpha();
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

      void draw(int var1, int var2) {
         Gx.Img var3 = Gx.painted(
            "vignette",
            512,
            512,
            var0 -> {
               var0.setPaint(
                  new RadialGradientPaint(
                     new Float(256.0F, 256.0F),
                     362.0F,
                     new float[]{0.0F, 0.55F, 0.8F, 1.0F},
                     new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 120), new Color(0, 0, 0, 255)},
                     CycleMethod.NO_CYCLE
                  )
               );
               var0.fillRect(0, 0, 512, 512);
            }
         );
         if (var3 != null) {
            Gx.image(var3, 0, 0, var1, var2, Gx.withAlpha(-1, this.strength.getF() / 100.0F));
            LocalPlayer var4 = Mc.player();
            float var5 = 0.0F;
            if (this.lowHp.get() && var4 != null && !var4.isCreative() && !Mc.isSpectator(var4)) {
               float var6 = Mc.health(var4) + Mc.absorption(var4);
               if (var6 <= this.hpLimit.get() * 2.0 && var6 > 0.0F) {
                  var5 = 1.0F - var6 / (float)(this.hpLimit.get() * 2.0) * 0.6F;
               }
            }

            this.red = this.red + (var5 - this.red) * Math.min(1.0F, Gx.dt * 4.0F);
            if (this.red > 0.01F) {
               float var7 = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 1000.0 * 5.5);
               Gx.image(var3, 0, 0, var1, var2, Gx.withAlpha(-57810, Math.min(1.0F, this.red * var7)));
            }
         }
      }
   }

   /**
    * "VR look": your world at the current spot as seen in a headset's home view -
    * a rounded dock at the bottom (time, connection, fps · ms, name, coordinates,
    * heading), key hints top left that light up (WASD, LMB, RMB), two dark hands
    * in the lower corners with a pointer ray to the crosshair. The lens frame is
    * optional. Keyboard + mouse as usual.
    */
   public static final class VrLook extends Module {
      final Setting.Num edge = this.add(new Setting.Num("Linsen-Rand", 0.0, 100.0, 5.0, 0.0, "%"));
      final Setting.Bool dock = this.add(new Setting.Bool("Dock-Leiste", true));
      final Setting.Bool keys = this.add(new Setting.Bool("Tasten-Hinweise", true));
      final Setting.Bool hands = this.add(new Setting.Bool("Hände mit Strahl", true));
      final Setting.Bool breathe = this.add(new Setting.Bool("Atmen", true));
      private long shownAt;
      private final float[] lit = new float[6];
      private long lastNs;

      VrLook() {
         super("vrlook", "VR-Look", "Wie in einer VR-Brille - Dock-Leiste, Tasten-Hinweise, Hände mit Strahl", Category.VISUALS);
         this.icon("vignette");
         this.fresh();
      }

      @Override
      public void onEnable() {
         this.shownAt = System.currentTimeMillis();
      }

      private static Gx.Img handImg(boolean right) {
         return Gx.painted(right ? "vrlook-hand-r" : "vrlook-hand-l", 256, 256, g -> {
            if (!right) {
               g.translate(256, 0);
               g.scale(-1, 1);
            }
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            // Pointing glove: wrist bottom right, index finger toward the top left.
            g.rotate(-0.62, 150, 170);
            Color base = new Color(32, 34, 41), lite = new Color(62, 65, 76);
            g.setPaint(new GradientPaint(110.0F, 120.0F, lite, 170.0F, 230.0F, base));
            g.fill(new java.awt.geom.RoundRectangle2D.Float(118.0F, 176.0F, 66.0F, 80.0F, 26.0F, 26.0F)); // cuff
            g.fill(new java.awt.geom.RoundRectangle2D.Float(104.0F, 112.0F, 92.0F, 84.0F, 38.0F, 38.0F)); // palm
            g.fill(new java.awt.geom.RoundRectangle2D.Float(110.0F, 28.0F, 24.0F, 100.0F, 24.0F, 24.0F)); // index
            for (int i = 0; i < 3; i++) {
               g.fill(new java.awt.geom.RoundRectangle2D.Float(134.0F + i * 21.0F, 98.0F + i * 4.0F, 22.0F, 42.0F, 22.0F, 22.0F)); // curled fingers
            }
            g.fill(new java.awt.geom.RoundRectangle2D.Float(78.0F, 128.0F, 44.0F, 22.0F, 22.0F, 22.0F)); // thumb
            g.setColor(new Color(255, 255, 255, 34));
            g.setStroke(new BasicStroke(3.0F));
            g.draw(new java.awt.geom.RoundRectangle2D.Float(110.0F, 28.0F, 24.0F, 100.0F, 24.0F, 24.0F));
            g.draw(new java.awt.geom.RoundRectangle2D.Float(104.0F, 112.0F, 92.0F, 84.0F, 38.0F, 38.0F));
         });
      }

      void draw(int w, int h) {
         int s = Gx.S;
         long now = System.currentTimeMillis();
         float in = Ease.outCubic(Ease.clamp((now - this.shownAt) / 600.0F));
         long ns = System.nanoTime();
         float dt = this.lastNs == 0L ? 0.016F : Math.min(0.1F, (ns - this.lastNs) / 1.0E9F);
         this.lastNs = ns;
         float a = this.edge.getF() / 100.0F;
         if (a > 0.0F) {
            Gx.Img lens = Gx.painted("vrlook-lens", 1024, 512, g -> {
               g.setColor(Color.BLACK);
               g.fillRect(0, 0, 1024, 512);
               g.setComposite(java.awt.AlphaComposite.Clear);
               g.fill(new java.awt.geom.Ellipse2D.Float(40.0F, 20.0F, 500.0F, 472.0F));
               g.fill(new java.awt.geom.Ellipse2D.Float(484.0F, 20.0F, 500.0F, 472.0F));
               g.setComposite(java.awt.AlphaComposite.SrcOver);
               Double nose = new Double();
               nose.moveTo(462.0, 512.0);
               nose.curveTo(470.0, 400.0, 554.0, 400.0, 562.0, 512.0);
               nose.closePath();
               g.fill(nose);
               for (float cx : new float[]{290.0F, 734.0F}) {
                  g.setPaint(new RadialGradientPaint(new Float(cx, 256.0F), 250.0F, new float[]{0.0F, 0.82F, 1.0F}, new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 200)}, CycleMethod.NO_CYCLE));
                  g.fill(new java.awt.geom.Ellipse2D.Float(cx - 250.0F, 20.0F, 500.0F, 472.0F));
               }
            });
            float br = this.breathe.get() ? (float)Math.sin(now / 1000.0 * 1.3) * 0.006F : 0.0F;
            int ow = Math.round(w * (1.06F + br)), oh = Math.round(h * (1.06F + br));
            if (lens != null) {
               Gx.image(lens, (w - ow) / 2, (h - oh) / 2, ow, oh, Gx.withAlpha(-1, a));
            }
         }
         LocalPlayer p = Mc.player();
         if (p == null) {
            return;
         }
         float bob = this.breathe.get() ? (float)Math.sin(now / 1000.0 * 1.6) * 2.0F * s : 0.0F;
         // Hands with rays to the crosshair (drawn first so the dock sits on top).
         if (this.hands.get()) {
            int hs = Math.round(Math.min(w, h) * 0.3F);
            int cx = w / 2, cy = h / 2;
            Gx.Img hr = handImg(true), hl = handImg(false);
            int ry = Math.round(h - hs * 0.78F + (1.0F - in) * hs + bob);
            int rxR = Math.round(w - hs * 1.05F), rxL = Math.round(hs * 0.05F);
            // index fingertip in the painted image (after the rotation) ≈ (0.30, 0.16)
            float tipRx = rxR + hs * 0.30F, tipRy = ry + hs * 0.16F;
            float tipLx = rxL + hs * 0.70F, tipLy = ry + hs * 0.16F;
            int accent = 0xFF6EA8FF;
            ray(tipRx, tipRy, cx, cy, Gx.withAlpha(accent, 0.75F * in), s);
            ray(tipLx, tipLy, tipLx + (cx - tipLx) * 0.35F, tipLy + (cy - tipLy) * 0.35F - h * 0.05F, Gx.withAlpha(accent, 0.3F * in), s);
            Gx.glow(cx, cy, 7 * s, Gx.withAlpha(accent, 0.6F * in));
            Gx.circle(cx, cy, Math.max(2, 2 * s), Gx.withAlpha(-1, in));
            if (hr != null) {
               Gx.image(hr, rxR, ry, hs, hs, -1);
            }
            if (hl != null) {
               Gx.image(hl, rxL, ry, hs, hs, -1);
            }
         }
         if (this.keys.get()) {
            net.minecraft.client.Options o = Mc.mc().options;
            boolean[] down = {Mc.pressed(o.keyUp), Mc.pressed(o.keyLeft), Mc.pressed(o.keyDown), Mc.pressed(o.keyRight), Mc.pressed(o.keyAttack), Mc.pressed(o.keyUse)};
            for (int i = 0; i < 6; i++) {
               this.lit[i] += ((down[i] ? 1.0F : 0.0F) - this.lit[i]) * Math.min(1.0F, dt * 18.0F);
            }
            int k = 13 * s, gap = 3 * s, x0 = 10 * s, y0 = 10 * s;
            int pw = 3 * k + 4 * gap, ph = 3 * k + 4 * gap + 9 * s;
            Gx.pushAlpha(in);
            Gx.shadow(x0, y0, pw, ph, 8 * s, 6 * s, 0.25F);
            Gx.rect(x0, y0, pw, ph, 8 * s, 0xA018191F);
            key(x0 + gap + (k + gap), y0 + gap, k, k, "W", this.lit[0], s);
            key(x0 + gap, y0 + gap * 2 + k, k, k, "A", this.lit[1], s);
            key(x0 + gap + (k + gap), y0 + gap * 2 + k, k, k, "S", this.lit[2], s);
            key(x0 + gap + 2 * (k + gap), y0 + gap * 2 + k, k, k, "D", this.lit[3], s);
            int mw = (3 * k + gap) / 2;
            key(x0 + gap, y0 + gap * 3 + 2 * k, mw, k, "LMB", this.lit[4], s);
            key(x0 + gap * 2 + mw, y0 + gap * 3 + 2 * k, mw, k, "RMB", this.lit[5], s);
            Gx.text("E Inventar · Esc Menü", x0 + gap, y0 + gap * 3 + 3 * k + 2 * s, 5.0F * s, 2, 0xD0FFFFFF);
            Gx.popAlpha();
         }
         if (this.dock.get()) {
            String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            int fps = Mc.fps();
            int ping = Mc.ping();
            String perf = fps + " fps · " + (ping >= 0 ? ping + " ms" : "lokal");
            String name = Mc.name(p);
            String pos = String.format(Locale.ROOT, "%d / %d / %d", (int)Math.floor(Mc.x(p)), (int)Math.floor(Mc.y(p)), (int)Math.floor(Mc.z(p)));
            float yaw = ((Mc.yaw(p) % 360.0F) + 360.0F) % 360.0F;
            String[] dirs = {"S", "SW", "W", "NW", "N", "NO", "O", "SO"};
            String dir = dirs[Math.round(yaw / 45.0F) & 7];
            float fs = 6.5F * s, fsSmall = 5.5F * s;
            int pad = 10 * s, ic = 9 * s, sep = 9 * s;
            float wTime = Gx.width(time, fs, 3), wPerf = Gx.width(perf, fsSmall, 2), wName = Gx.width(name, fs, 3), wPos = Gx.width(pos, fsSmall, 2), wDir = Gx.width(dir, fs, 3);
            int dw = Math.round(pad * 2 + wTime + sep + ic + 4 * s + wPerf + sep * 2 + ic + 4 * s + wName + sep * 2 + ic + 4 * s + wPos + sep + ic + 4 * s + wDir);
            int dh = 22 * s;
            int dx = (w - dw) / 2;
            int dy = Math.round(h - dh - 10 * s + (1.0F - in) * (dh + 14 * s));
            Gx.shadow(dx, dy, dw, dh, dh / 2, 8 * s, 0.35F);
            Gx.rect(dx, dy, dw, dh, dh / 2, 0xF01E1F26);
            Gx.outline(dx, dy, dw, dh, dh / 2, Math.max(1, s / 2), 0x22FFFFFF);
            float x = dx + pad, my = dy + dh / 2.0F;
            x += Gx.textMid(time, x, my, fs, 3, -1) + sep;
            Gx.icon("wifi", x + ic / 2.0F, my, ic, ping >= 0 && ping < 150 ? -1 : 0xFFFFC266);
            x += ic + 4 * s;
            x += Gx.textMid(perf, x, my, fsSmall, 2, 0xFFB8BCC8) + sep;
            Gx.fill(Math.round(x), Math.round(my - 6 * s), Math.max(1, s / 2), 12 * s, 0x30FFFFFF);
            x += sep;
            Gx.icon("user", x + ic / 2.0F, my, ic, -1);
            x += ic + 4 * s;
            x += Gx.textMid(name, x, my, fs, 3, -1) + sep;
            Gx.fill(Math.round(x), Math.round(my - 6 * s), Math.max(1, s / 2), 12 * s, 0x30FFFFFF);
            x += sep;
            Gx.icon("map-pin", x + ic / 2.0F, my, ic, -1);
            x += ic + 4 * s;
            x += Gx.textMid(pos, x, my, fsSmall, 2, 0xFFB8BCC8) + sep;
            Gx.icon("compass", x + ic / 2.0F, my, ic, -1);
            x += ic + 4 * s;
            Gx.textMid(dir, x, my, fs, 3, -1);
         }
      }

      private static void key(int x, int y, int kw, int kh, String label, float lit, int s) {
         int bg = Gx.mix(0x24FFFFFF, 0xFF6EA8FF, lit);
         int off = Math.round(lit * s);
         Gx.rect(x, y + off, kw, kh, 3 * s, bg);
         Gx.outline(x, y + off, kw, kh, 3 * s, Math.max(1, s / 2), 0x40FFFFFF);
         Gx.textCenter(label, x + kw / 2.0F, y + off + kh / 2.0F, (label.length() > 1 ? 4.5F : 6.0F) * s, 3, -1);
      }

      /** A dotted light ray between two screen points. */
      private static void ray(float x0, float y0, float x1, float y1, int color, int s) {
         float len = (float)Math.hypot(x1 - x0, y1 - y0);
         int n = Math.max(2, Math.round(len / (3.0F * s)));
         int r = Math.max(1, Math.round(s * 0.7F));
         for (int i = 0; i <= n; i++) {
            float t = (float)i / n;
            Gx.circle(Math.round(x0 + (x1 - x0) * t), Math.round(y0 + (y1 - y0) * t), r, color);
         }
      }
   }
}
