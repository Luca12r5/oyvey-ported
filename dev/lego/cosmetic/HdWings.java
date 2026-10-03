package dev.lego.cosmetic;

import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;
import java.util.ArrayList;
import java.util.List;

final class HdWings {
   private static float[][] BUP;
   private static float[][] BLO;
   private static float[][] FAIRY;

   private HdWings() {
   }

   private static float[] p(float var0, float var1, float var2) {
      return new float[]{var0, var1, var2};
   }

   static float[][] outline(Path2D var0, double var1, int var3) {
      ArrayList var4 = new ArrayList();
      double[] var5 = new double[6];

      for (FlatteningPathIterator var6 = new FlatteningPathIterator(var0.getPathIterator(null), var1); !var6.isDone(); var6.next()) {
         int var7 = var6.currentSegment(var5);
         if (var7 == 0 || var7 == 1) {
            float[] var8 = new float[]{(float)var5[0], (float)var5[1]};
            if (var4.isEmpty() || Math.hypot(var8[0] - ((float[])var4.get(var4.size() - 1))[0], var8[1] - ((float[])var4.get(var4.size() - 1))[1]) > 0.5) {
               var4.add(var8);
            }
         }
      }

      if (var4.size() > 2
         && Math.hypot(((float[])var4.get(0))[0] - ((float[])var4.get(var4.size() - 1))[0], ((float[])var4.get(0))[1] - ((float[])var4.get(var4.size() - 1))[1])
            < 1.0) {
         var4.remove(var4.size() - 1);
      }

      int var9 = Math.max(1, (int)Math.ceil((double)var4.size() / var3));
      ArrayList var10 = new ArrayList();

      for (int var11 = 0; var11 < var4.size(); var11 += var9) {
         var10.add((float[])var4.get(var11));
      }

      return var10.toArray(new float[0][]);
   }

   static void card(G var0, String var1, float[][] var2, float var3, float var4, float var5, float var6, float var7, float var8, String var9, int var10) {
      float[][] var11 = HdGeo.ccw(flipY(var2));
      int var12 = var11.length;
      float[][] var13 = new float[var12][];

      for (int var14 = 0; var14 < var12; var14++) {
         var13[var14] = wp(var11[var14], var3, var4, var5, var6, var7);
      }

      List var22 = Cos3Geo.triangulate(var11);
      int var15 = var0.color;

      for (int[] var17 : var22) {
         float[] var18 = var13[var17[0]];
         float[] var19 = var13[var17[1]];
         float[] var20 = var13[var17[2]];
         float[] var21 = new float[]{
            uvx(var11[var17[0]], var3),
            uvy(var11[var17[0]], var4),
            uvx(var11[var17[1]], var3),
            uvy(var11[var17[1]], var4),
            uvx(var11[var17[2]], var3),
            uvy(var11[var17[2]], var4)
         };
         var0.quad(
            var1,
            off(var18, var8),
            off(var19, var8),
            off(var20, var8),
            off(var20, var8),
            new float[]{var21[0], var21[1], var21[2], var21[3], var21[4], var21[5], var21[4], var21[5]}
         );
         var0.quad(
            var1,
            off(var20, -var8),
            off(var19, -var8),
            off(var18, -var8),
            off(var18, -var8),
            new float[]{var21[4], var21[5], var21[2], var21[3], var21[0], var21[1], var21[0], var21[1]}
         );
      }

      var0.color(var10);

      for (int var23 = 0; var23 < var12; var23++) {
         float[] var24 = var13[var23];
         float[] var25 = var13[(var23 + 1) % var12];
         var0.quad(
            var9,
            off(var24, -var8 * 1.6F),
            off(var25, -var8 * 1.6F),
            off(var25, var8 * 1.6F),
            off(var24, var8 * 1.6F),
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      var0.color(var15);
   }

   private static float[][] flipY(float[][] var0) {
      float[][] var1 = new float[var0.length][];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = new float[]{var0[var2][0], -var0[var2][1]};
      }

      return var1;
   }

   private static float uvx(float[] var0, float var1) {
      return var0[0] / var1;
   }

   private static float uvy(float[] var0, float var1) {
      return -var0[1] / var1;
   }

   private static float[] wp(float[] var0, float var1, float var2, float var3, float var4, float var5) {
      float var6 = var0[0] / var1 * var3;
      float var7 = var0[1] / var2 * var4;
      return p(var6, var7, -var5 * (var6 * var6 + var7 * var7 * 0.4F));
   }

   private static float[] off(float[] var0, float var1) {
      return p(var0[0], var0[1], var0[2] + var1);
   }

   private static synchronized void shapes() {
      if (BUP == null) {
         Double var0 = new Double();
         var0.moveTo(6.0, 30.0);
         var0.curveTo(90.0, -30.0, 250.0, -10.0, 246.0, 80.0);
         var0.curveTo(242.0, 140.0, 150.0, 170.0, 8.0, 150.0);
         var0.closePath();
         Double var1 = new Double();
         var1.moveTo(8.0, 150.0);
         var1.curveTo(120.0, 150.0, 210.0, 200.0, 190.0, 270.0);
         var1.curveTo(170.0, 330.0, 60.0, 320.0, 6.0, 230.0);
         var1.closePath();
         Double var2 = new Double();
         var2.moveTo(2.0, 64.0);
         var2.curveTo(60.0, 0.0, 200.0, -10.0, 250.0, 40.0);
         var2.curveTo(262.0, 70.0, 200.0, 112.0, 120.0, 110.0);
         var2.curveTo(60.0, 108.0, 20.0, 90.0, 2.0, 64.0);
         BUP = outline(var0, 1.5, 30);
         BLO = outline(var1, 1.5, 26);
         FAIRY = outline(var2, 1.5, 30);
      }
   }

   static void butterfly(G var0, Cos.A var1) {
      shapes();
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      float var4 = var1.sneak
         ? 72.0F
         : Geo.lerp(18.0F + 22.0F * (0.5F + 0.5F * Cos3Geo.sin(var2 * 2.6F)), 20.0F + 45.0F * (0.5F + 0.5F * Cos3Geo.sin(var2 * 12.0F)), var3);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         var0.push();
         var0.scale(var5, 1.0F, 1.0F);
         var0.translate(0.55F, 2.5F, -2.9F);
         var0.rotY(var4);
         var0.rotZ(8.0F);
         var0.color(-1);
         card(var0, "wing_butterfly2", BUP, 256.0F, 320.0F, 13.5F, 17.0F, 0.014F, 0.07F, "hd_rubber", -16316913);
         var0.push();
         var0.rotY(6.0F);
         card(var0, "wing_butterfly2", BLO, 256.0F, 320.0F, 13.5F, 17.0F, 0.012F, 0.07F, "hd_rubber", -16316913);
         var0.pop();
         var0.pop();
      }

      var0.color(-15068124);
      Geo.ellipsoid(var0, "hd_fur", 0.0F, 1.2F, -3.0F, 0.75F, 1.6F, 0.75F, 10, 6);

      for (int var8 = 0; var8 < 5; var8++) {
         Geo.ellipsoid(var0, "hd_fur", 0.0F, -1.2F - var8 * 1.25F, -3.0F + var8 * 0.05F, 0.62F - var8 * 0.07F, 0.75F, 0.62F - var8 * 0.07F, 8, 5);
      }

      Cos3Geo.ball(var0, "hd_fur", 0.0F, 3.3F, -3.1F, 0.62F, 8);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         float var6 = Cos3Geo.sin(var2 * 2.2F + var9) * 0.3F;
         float[][] var7 = new float[][]{{var9 * 0.2F, 3.7F, -3.1F}, {var9 * 0.9F, 5.2F, -3.3F + var6}, {var9 * 1.5F, 6.4F, -3.6F + var6}};
         Cos3Geo.limb(var0, "hd_plastic", var7, new float[]{0.08F, 0.07F, 0.06F}, 4);
         Cos3Geo.ball(var0, "hd_gloss", var7[2][0], var7[2][1], var7[2][2], 0.22F, 6);
      }

      var0.color(-1);
   }

   static void fairy(G var0, Cos.A var1) {
      shapes();
      float var2 = var1.time;
      float var3 = var1.preview ? 0.0F : var1.move;
      float var4 = var1.sneak
         ? 70.0F
         : Geo.lerp(22.0F + 18.0F * (0.5F + 0.5F * Cos3Geo.sin(var2 * 3.4F)), 20.0F + 40.0F * (0.5F + 0.5F * Cos3Geo.sin(var2 * 16.0F)), var3);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         for (int var6 = 0; var6 < 2; var6++) {
            var0.push();
            var0.scale(var5, 1.0F, 1.0F);
            var0.translate(0.6F, var6 == 0 ? -1.5F : -4.0F, -2.5F);
            var0.rotY(var4 + (var6 == 0 ? 0 : 8) + Cos3Geo.sin(var2 * 3.4F + var6) * 3.0F);
            var0.rotZ(var6 == 0 ? 28.0F : -34.0F);
            float var7 = var6 == 0 ? 17.0F : 12.0F;
            float var8 = var6 == 0 ? 8.5F : 6.0F;
            var0.translate(0.0F, var8 / 2.0F, 0.0F);
            var0.glow(true).color(-419430401);
            card(var0, "wing_fairy", FAIRY, 256.0F, 128.0F, var7, var8, 0.012F, 0.04F, "white", -1510145);
            var0.color(-4656897);

            for (int var9 = 0; var9 < 3; var9++) {
               float[][] var10 = new float[6][];

               for (int var11 = 0; var11 < 6; var11++) {
                  float var12 = var11 / 5.0F;
                  float var13 = var12 * var7 * (0.62F + 0.15F * var9);
                  float var14 = -var8 * 0.5F + var8 * (0.1F * var9 + 0.25F) * (float)Math.sin(var12 * Math.PI * 0.8) + var8 * (0.1F - 0.2F * var9) * var12;
                  var10[var11] = p(var13, var14, -0.012F * var13 * var13 + 0.06F);
               }

               Geo.chain(var0, "white", var10, new float[]{0.08F, 0.07F, 0.06F, 0.05F, 0.04F, 0.03F}, 3);
            }

            var0.glow(false);
            var0.pop();
         }
      }

      for (int var15 = 0; var15 < 10; var15++) {
         float var16 = Cos3Geo.fract(var2 * 0.5F + var15 / 10.0F);
         float var17 = Cos3Geo.sin(var16 * Math.PI);
         float var18 = Cos3Geo.sin(var15 * 2.7F) * 9.0F + Cos3Geo.sin(var2 * 2.0F + var15) * 1.2F;
         float var19 = -4.0F - var15 % 3 * 1.5F;
         Cos3Models.sparkle(var0, var18, -2.0F - var16 * 16.0F, var19, 0.4F * var17, Cos3Geo.alpha(var17, var15 % 2 == 0 ? 16777215 : 10154239));
         if (var15 % 3 == 0) {
            Cos3Geo.halo(var0, var18, -2.0F - var16 * 16.0F, var19, 2.2F * var17, Cos3Geo.alpha(0.4 * var17, 12120319));
         }
      }

      var0.color(-1);
   }

   static void cyber(G var0, Cos.A var1) {
      Wings.Pose var2 = Wings.pose(var1, 0.8F);
      float var3 = 17.0F;

      for (byte var4 = 1; var4 >= -1; var4 -= 2) {
         var0.push();
         var0.scale(var4, 1.0F, 1.0F);
         var0.translate(1.0F, -2.0F, -2.45F);
         var0.rotY(var2.sweep);
         var0.rotZ(var2.raise);
         var0.rotX(-8.0F);
         float[] var5 = new float[]{0.0F, 0.0F, 0.0F};
         float[] var6 = new float[]{var3 * 0.4F, var3 * 0.22F, -0.3F};
         float[] var7 = new float[]{var3 * 0.82F * (1.0F - 0.15F * var2.fold), var3 * 0.26F, -0.8F};
         var0.color(-12959668);
         Cos3Geo.limb(var0, "hd_steel", new float[][]{var5, var6}, new float[]{0.95F, 0.8F}, 8);
         Cos3Geo.limb(var0, "hd_steel", new float[][]{var6, var7}, new float[]{0.8F, 0.55F}, 8);
         var0.color(-2564376);

         for (float[] var11 : new float[][]{var5, var6, var7}) {
            Cos3Geo.ball(var0, "hd_chrome", var11[0], var11[1], var11[2], 1.05F, 10);
         }

         var0.glow(true).color(-13965569);
         Cos3Geo.limb(
            var0,
            "white",
            new float[][]{{var5[0], var5[1] + 0.85F, var5[2]}, {var6[0], var6[1] + 0.75F, var6[2]}, {var7[0], var7[1] + 0.55F, var7[2]}},
            new float[]{0.12F, 0.12F, 0.1F},
            4
         );
         Cos3Geo.halo(var0, var6[0], var6[1], var6[2] - 0.9F, 2.8F + 0.6F * Cos3Geo.sin(var2.t * 4.0F), Cos3Geo.alpha(0.6, 2811647));
         Cos3Geo.halo(var0, var7[0], var7[1], var7[2] - 0.9F, 2.4F + 0.6F * Cos3Geo.sin(var2.t * 4.0F + 1.0F), Cos3Geo.alpha(0.6, 2811647));
         var0.glow(false);
         byte var16 = 8;

         for (int var17 = 0; var17 < var16; var17++) {
            float var18 = (float)var17 / (var16 - 1);
            float[] var19 = new float[]{
               Geo.lerp(1.5F, var7[0], var18), Geo.lerp(0.5F, var7[1], var18) - 0.3F, Geo.lerp(0.0F, var7[2], var18) - 0.2F + var17 * 0.06F
            };
            float var12 = Geo.lerp(-4.0F, 62.0F, var18) * (1.0F - 0.6F * var2.fold) + Cos3Geo.sin(var2.t * 2.0F + var17) * 1.5F;
            float var13 = var3 * (0.5F + 0.42F * Cos3Geo.sin(Math.PI * (0.25 + 0.6 * var18)));
            float var14 = var3 * 0.07F;
            var0.push();
            var0.translate(var19[0], var19[1], var19[2]);
            var0.rotZ(var12);
            var0.rotY(-8.0F);
            float[][] var15 = new float[][]{{-var14, 0.2F}, {var14, 0.2F}, {var14 * 0.95F, -var13 * 0.78F}, {0.0F, -var13}, {-var14 * 0.95F, -var13 * 0.78F}};
            var0.color(-3617064);
            HdGeo.puff(var0, "hd_steel", var15, 0.36F, 0.14F, 1);
            var0.glow(true).color(Cos3Geo.alpha(0.8 + 0.2 * Cos3Geo.sin(var2.t * 5.0F + var17), 2811647));
            var0.push();
            var0.translate(0.0F, 0.0F, -0.2F);
            Cos3Geo.cbox(var0, "white", 0.0F, -var13 * 0.42F, 0.0F, 0.14F, var13 * 0.72F, 0.06F, 0.02F);
            var0.pop();
            var0.glow(false);
            var0.pop();
         }

         var0.pop();
      }

      var0.color(-12959668);
      Cos3Geo.bbox(var0, "hd_steel", -2.2F, -5.5F, -3.3F, 2.2F, -0.8F, -2.05F, 0.3F);
      var0.color(-2564376);
      var0.push();
      var0.translate(0.0F, -3.1F, -3.3F);
      var0.rotX(-90.0F);
      Cos3Geo.lathe(var0, "hd_chrome", new float[]{1.2F, 1.2F, 0.9F, 0.0F}, new float[]{0.0F, 0.35F, 0.45F, 0.46F}, 14);
      var0.pop();
      var0.glow(true).color(-13965569);
      var0.push();
      var0.translate(0.0F, -3.1F, -3.78F);
      var0.rotX(90.0F);
      var0.torus("white", 0.62F, 0.13F, 14, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.glow(false);
      Cos3Geo.halo(var0, 0.0F, -3.1F, -4.2F, 3.2F, Cos3Geo.alpha(0.5, 2811647));
      var0.color(-1);
   }
}
