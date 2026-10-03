package dev.lego.cosmetic;

import java.util.ArrayDeque;

public final class G {
   private final G.Sink sink;
   private float[] m = identity();
   private final ArrayDeque<float[]> stack = new ArrayDeque<>();
   public int color = -1;
   public boolean glow = false;

   public G(G.Sink var1) {
      this.sink = var1;
   }

   private static float[] identity() {
      return new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
   }

   public void push() {
      this.stack.push((float[])this.m.clone());
   }

   public void pop() {
      this.m = this.stack.pop();
   }

   private void mul(float[] var1) {
      float[] var2 = new float[16];

      for (int var3 = 0; var3 < 4; var3++) {
         for (int var4 = 0; var4 < 4; var4++) {
            float var5 = 0.0F;

            for (int var6 = 0; var6 < 4; var6++) {
               var5 += this.m[var3 * 4 + var6] * var1[var6 * 4 + var4];
            }

            var2[var3 * 4 + var4] = var5;
         }
      }

      this.m = var2;
   }

   public G translate(float var1, float var2, float var3) {
      this.mul(new float[]{1.0F, 0.0F, 0.0F, var1, 0.0F, 1.0F, 0.0F, var2, 0.0F, 0.0F, 1.0F, var3, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G scale(float var1, float var2, float var3) {
      this.mul(new float[]{var1, 0.0F, 0.0F, 0.0F, 0.0F, var2, 0.0F, 0.0F, 0.0F, 0.0F, var3, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G scale(float var1) {
      return this.scale(var1, var1, var1);
   }

   public G rotX(float var1) {
      float var2 = (float)Math.cos(Math.toRadians(var1));
      float var3 = (float)Math.sin(Math.toRadians(var1));
      this.mul(new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, var2, -var3, 0.0F, 0.0F, var3, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G rotY(float var1) {
      float var2 = (float)Math.cos(Math.toRadians(var1));
      float var3 = (float)Math.sin(Math.toRadians(var1));
      this.mul(new float[]{var2, 0.0F, var3, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -var3, 0.0F, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   public G rotZ(float var1) {
      float var2 = (float)Math.cos(Math.toRadians(var1));
      float var3 = (float)Math.sin(Math.toRadians(var1));
      this.mul(new float[]{var2, -var3, 0.0F, 0.0F, var3, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F});
      return this;
   }

   private float[] tp(float var1, float var2, float var3) {
      return new float[]{
         this.m[0] * var1 + this.m[1] * var2 + this.m[2] * var3 + this.m[3],
         this.m[4] * var1 + this.m[5] * var2 + this.m[6] * var3 + this.m[7],
         this.m[8] * var1 + this.m[9] * var2 + this.m[10] * var3 + this.m[11]
      };
   }

   public G color(int var1) {
      this.color = var1;
      return this;
   }

   public G glow(boolean var1) {
      this.glow = var1;
      return this;
   }

   public void quad(String var1, float[] var2, float[] var3, float[] var4, float[] var5, float[] var6) {
      float[] var7 = this.tp(var2[0], var2[1], var2[2]);
      float[] var8 = this.tp(var3[0], var3[1], var3[2]);
      float[] var9 = this.tp(var4[0], var4[1], var4[2]);
      float[] var10 = this.tp(var5[0], var5[1], var5[2]);
      float var11 = var8[0] - var7[0];
      float var12 = var8[1] - var7[1];
      float var13 = var8[2] - var7[2];
      float var14 = var10[0] - var7[0];
      float var15 = var10[1] - var7[1];
      float var16 = var10[2] - var7[2];
      float var17 = var12 * var16 - var13 * var15;
      float var18 = var13 * var14 - var11 * var16;
      float var19 = var11 * var15 - var12 * var14;
      float var20 = (float)Math.sqrt(var17 * var17 + var18 * var18 + var19 * var19);
      if (var20 < 1.0E-6F) {
         var17 = 0.0F;
         var18 = 1.0F;
         var19 = 0.0F;
      } else {
         var17 /= var20;
         var18 /= var20;
         var19 /= var20;
      }

      this.sink
         .quad(
            var1,
            new float[]{var7[0], var7[1], var7[2], var8[0], var8[1], var8[2], var9[0], var9[1], var9[2], var10[0], var10[1], var10[2]},
            var6,
            this.color,
            this.glow,
            var17,
            var18,
            var19
         );
   }

   public void plane(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      float var10 = var2 - var4 / 2.0F;
      float var11 = var2 + var4 / 2.0F;
      float var12 = var3 - var5 / 2.0F;
      float var13 = var3 + var5 / 2.0F;
      this.quad(
         var1,
         new float[]{var10, var13, 0.0F},
         new float[]{var11, var13, 0.0F},
         new float[]{var11, var12, 0.0F},
         new float[]{var10, var12, 0.0F},
         new float[]{var6, var7, var8, var7, var8, var9, var6, var9}
      );
   }

   public void planeTL(String var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.quad(
         var1,
         new float[]{0.0F, 0.0F, 0.0F},
         new float[]{var2, 0.0F, 0.0F},
         new float[]{var2, -var3, 0.0F},
         new float[]{0.0F, -var3, 0.0F},
         new float[]{var4, var5, var6, var5, var6, var7, var4, var7}
      );
   }

   public void box(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11) {
      float[] var12 = new float[]{var8, var9, var10, var9, var10, var11, var8, var11};
      this.quad(var1, new float[]{var2, var6, var7}, new float[]{var5, var6, var7}, new float[]{var5, var3, var7}, new float[]{var2, var3, var7}, var12);
      this.quad(var1, new float[]{var5, var6, var4}, new float[]{var2, var6, var4}, new float[]{var2, var3, var4}, new float[]{var5, var3, var4}, var12);
      this.quad(var1, new float[]{var2, var6, var4}, new float[]{var2, var6, var7}, new float[]{var2, var3, var7}, new float[]{var2, var3, var4}, var12);
      this.quad(var1, new float[]{var5, var6, var7}, new float[]{var5, var6, var4}, new float[]{var5, var3, var4}, new float[]{var5, var3, var7}, var12);
      this.quad(var1, new float[]{var2, var6, var4}, new float[]{var5, var6, var4}, new float[]{var5, var6, var7}, new float[]{var2, var6, var7}, var12);
      this.quad(var1, new float[]{var2, var3, var7}, new float[]{var5, var3, var7}, new float[]{var5, var3, var4}, new float[]{var2, var3, var4}, var12);
   }

   public void box6(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float[][] var8) {
      this.quad(var1, new float[]{var2, var6, var7}, new float[]{var5, var6, var7}, new float[]{var5, var3, var7}, new float[]{var2, var3, var7}, r(var8[0]));
      this.quad(var1, new float[]{var5, var6, var4}, new float[]{var2, var6, var4}, new float[]{var2, var3, var4}, new float[]{var5, var3, var4}, r(var8[1]));
      this.quad(var1, new float[]{var2, var6, var4}, new float[]{var2, var6, var7}, new float[]{var2, var3, var7}, new float[]{var2, var3, var4}, r(var8[2]));
      this.quad(var1, new float[]{var5, var6, var7}, new float[]{var5, var6, var4}, new float[]{var5, var3, var4}, new float[]{var5, var3, var7}, r(var8[3]));
      this.quad(var1, new float[]{var2, var6, var4}, new float[]{var5, var6, var4}, new float[]{var5, var6, var7}, new float[]{var2, var6, var7}, r(var8[4]));
      this.quad(var1, new float[]{var2, var3, var7}, new float[]{var5, var3, var7}, new float[]{var5, var3, var4}, new float[]{var2, var3, var4}, r(var8[5]));
   }

   private static float[] r(float[] var0) {
      return new float[]{var0[0], var0[1], var0[2], var0[1], var0[2], var0[3], var0[0], var0[3]};
   }

   public void cylinder(String var1, float var2, float var3, float var4, float var5, int var6, boolean var7, float var8, float var9, float var10, float var11) {
      for (int var12 = 0; var12 < var6; var12++) {
         double var13 = (Math.PI * 2) * var12 / var6;
         double var15 = (Math.PI * 2) * (var12 + 1) / var6;
         float var17 = (float)Math.cos(var13);
         float var18 = (float)Math.sin(var13);
         float var19 = (float)Math.cos(var15);
         float var20 = (float)Math.sin(var15);
         float var21 = var8 + (var10 - var8) * var12 / var6;
         float var22 = var8 + (var10 - var8) * (var12 + 1) / var6;
         this.quad(
            var1,
            new float[]{var17 * var3, var5, var18 * var3},
            new float[]{var19 * var3, var5, var20 * var3},
            new float[]{var19 * var2, var4, var20 * var2},
            new float[]{var17 * var2, var4, var18 * var2},
            new float[]{var21, var9, var22, var9, var22, var11, var21, var11}
         );
         if (var7) {
            float var23 = (var8 + var10) / 2.0F;
            float var24 = (var9 + var11) / 2.0F;
            this.quad(
               var1,
               new float[]{0.0F, var5, 0.0F},
               new float[]{0.0F, var5, 0.0F},
               new float[]{var19 * var3, var5, var20 * var3},
               new float[]{var17 * var3, var5, var18 * var3},
               new float[]{var23, var24, var23, var24, var23, var9, var23, var9}
            );
            if (var2 > 0.01F) {
               this.quad(
                  var1,
                  new float[]{0.0F, var4, 0.0F},
                  new float[]{var17 * var2, var4, var18 * var2},
                  new float[]{var19 * var2, var4, var20 * var2},
                  new float[]{0.0F, var4, 0.0F},
                  new float[]{var23, var24, var23, var11, var23, var11, var23, var24}
               );
            }
         }
      }
   }

   public void ring(String var1, float var2, float var3, float var4, int var5, float var6, float var7, float var8, float var9) {
      for (int var10 = 0; var10 < var5; var10++) {
         double var11 = (Math.PI * 2) * var10 / var5;
         double var13 = (Math.PI * 2) * (var10 + 1) / var5;
         float var15 = (float)Math.cos(var11);
         float var16 = (float)Math.sin(var11);
         float var17 = (float)Math.cos(var13);
         float var18 = (float)Math.sin(var13);
         float var19 = var6 + (var8 - var6) * var10 / var5;
         float var20 = var6 + (var8 - var6) * (var10 + 1) / var5;
         this.quad(
            var1,
            new float[]{var15 * var3, var4, var16 * var3},
            new float[]{var17 * var3, var4, var18 * var3},
            new float[]{var17 * var2, var4, var18 * var2},
            new float[]{var15 * var2, var4, var16 * var2},
            new float[]{var19, var7, var20, var7, var20, var9, var19, var9}
         );
      }
   }

   public void torus(String var1, float var2, float var3, int var4, int var5, float var6, float var7, float var8, float var9) {
      for (int var10 = 0; var10 < var4; var10++) {
         double var11 = (Math.PI * 2) * var10 / var4;
         double var13 = (Math.PI * 2) * (var10 + 1) / var4;

         for (int var15 = 0; var15 < var5; var15++) {
            double var16 = (Math.PI * 2) * var15 / var5;
            double var18 = (Math.PI * 2) * (var15 + 1) / var5;
            float[] var20 = tor(var2, var3, var11, var16);
            float[] var21 = tor(var2, var3, var13, var16);
            float[] var22 = tor(var2, var3, var13, var18);
            float[] var23 = tor(var2, var3, var11, var18);
            float var24 = var6 + (var8 - var6) * var10 / var4;
            float var25 = var6 + (var8 - var6) * (var10 + 1) / var4;
            float var26 = var7 + (var9 - var7) * var15 / var5;
            float var27 = var7 + (var9 - var7) * (var15 + 1) / var5;
            this.quad(var1, var20, var21, var22, var23, new float[]{var24, var26, var25, var26, var25, var27, var24, var27});
         }
      }
   }

   private static float[] tor(float var0, float var1, double var2, double var4) {
      double var6 = var0 + var1 * Math.cos(var4);
      return new float[]{(float)(Math.cos(var2) * var6), (float)(var1 * Math.sin(var4)), (float)(Math.sin(var2) * var6)};
   }

   public void sphere(String var1, float var2, int var3, int var4, float var5, float var6, float var7, float var8) {
      for (int var9 = 0; var9 < var4; var9++) {
         double var10 = Math.PI * var9 / var4 - (Math.PI / 2);
         double var12 = Math.PI * (var9 + 1) / var4 - (Math.PI / 2);

         for (int var14 = 0; var14 < var3; var14++) {
            double var15 = (Math.PI * 2) * var14 / var3;
            double var17 = (Math.PI * 2) * (var14 + 1) / var3;
            float[] var19 = sph(var2, var10, var15);
            float[] var20 = sph(var2, var10, var17);
            float[] var21 = sph(var2, var12, var17);
            float[] var22 = sph(var2, var12, var15);
            float var23 = var5 + (var7 - var5) * var14 / var3;
            float var24 = var5 + (var7 - var5) * (var14 + 1) / var3;
            float var25 = var8 - (var8 - var6) * var9 / var4;
            float var26 = var8 - (var8 - var6) * (var9 + 1) / var4;
            this.quad(var1, var22, var21, var20, var19, new float[]{var23, var26, var24, var26, var24, var25, var23, var25});
         }
      }
   }

   private static float[] sph(float var0, double var1, double var3) {
      return new float[]{(float)(Math.cos(var1) * Math.cos(var3) * var0), (float)(Math.sin(var1) * var0), (float)(Math.cos(var1) * Math.sin(var3) * var0)};
   }

   public void cross(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
      this.push();
      this.translate(var2, var3, var4);
      this.plane(var1, 0.0F, 0.0F, var5, var6, var7, var8, var9, var10);
      this.rotY(90.0F);
      this.plane(var1, 0.0F, 0.0F, var5, var6, var7, var8, var9, var10);
      this.pop();
   }

   public interface Sink {
      void quad(String var1, float[] var2, float[] var3, int var4, boolean var5, float var6, float var7, float var8);
   }
}
