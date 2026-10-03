package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.Random;
import java.util.function.Consumer;

final class GameFx {
   static final int DOT = 0;
   static final int SQUARE = 1;
   static final int SPARK = 2;
   static final int RING = 3;
   static final int FLAKE = 4;
   static final int STREAK = 5;
   private static final int MAX = 700;
   private static final int MAX_POP = 24;
   private final Random rnd = new Random();
   private final float[] px = new float[700];
   private final float[] py = new float[700];
   private final float[] vx = new float[700];
   private final float[] vy = new float[700];
   private final float[] life = new float[700];
   private final float[] max = new float[700];
   private final float[] size = new float[700];
   private final float[] grav = new float[700];
   private final float[] drag = new float[700];
   private final int[] col = new int[700];
   private final int[] kind = new int[700];
   private int n = 0;
   private final String[] popText = new String[24];
   private final float[] popX = new float[24];
   private final float[] popY = new float[24];
   private final float[] popT = new float[24];
   private final float[] popS = new float[24];
   private final int[] popC = new int[24];
   private int pn = 0;
   private float trauma = 0.0F;
   private float shakeT = 0.0F;
   private float flash = 0.0F;
   private int flashCol = -1;
   float sx;
   float sy;

   void clear() {
      this.n = 0;
      this.pn = 0;
      this.trauma = 0.0F;
      this.flash = 0.0F;
      this.sx = this.sy = 0.0F;
   }

   void add(float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8, float var9, float var10) {
      int var11;
      if (this.n < 700) {
         var11 = this.n++;
      } else {
         var11 = this.rnd.nextInt(700);
      }

      this.px[var11] = var1;
      this.py[var11] = var2;
      this.vx[var11] = var3;
      this.vy[var11] = var4;
      this.life[var11] = 0.0F;
      this.max[var11] = var5;
      this.size[var11] = var6;
      this.col[var11] = var7;
      this.kind[var11] = var8;
      this.grav[var11] = var9;
      this.drag[var11] = var10;
   }

   void burst(float var1, float var2, int var3, int var4, float var5, float var6, float var7, float var8, int var9) {
      for (int var10 = 0; var10 < var3; var10++) {
         double var11 = this.rnd.nextDouble() * Math.PI * 2.0;
         float var13 = var5 * (0.35F + this.rnd.nextFloat() * 0.65F);
         this.add(
            var1,
            var2,
            (float)Math.cos(var11) * var13,
            (float)Math.sin(var11) * var13,
            var7 * (0.6F + this.rnd.nextFloat() * 0.4F),
            var6 * (0.6F + this.rnd.nextFloat() * 0.6F),
            var4,
            var9,
            var8,
            2.2F
         );
      }
   }

   void confetti(float var1, float var2, int var3, int[] var4, float var5, float var6) {
      for (int var7 = 0; var7 < var3; var7++) {
         double var8 = (-Math.PI / 2) + (this.rnd.nextDouble() - 0.5) * Math.PI * 1.4;
         float var10 = var5 * (0.4F + this.rnd.nextFloat() * 0.6F);
         this.add(
            var1,
            var2,
            (float)Math.cos(var8) * var10,
            (float)Math.sin(var8) * var10,
            0.9F + this.rnd.nextFloat() * 0.5F,
            var6 * (0.6F + this.rnd.nextFloat() * 0.7F),
            var4[this.rnd.nextInt(var4.length)],
            1,
            260.0F,
            1.6F
         );
      }
   }

   void ring(float var1, float var2, float var3, int var4, float var5) {
      this.add(var1, var2, 0.0F, 0.0F, var5, var3, var4, 3, 0.0F, 0.0F);
   }

   void popup(String var1, float var2, float var3, int var4, float var5) {
      int var6 = this.pn < 24 ? this.pn++ : 0;
      this.popText[var6] = var1;
      this.popX[var6] = var2;
      this.popY[var6] = var3;
      this.popT[var6] = 0.0F;
      this.popC[var6] = var4;
      this.popS[var6] = var5;
   }

   void shake(float var1) {
      this.trauma = Math.min(1.0F, this.trauma + var1);
   }

   void flash(int var1, float var2) {
      this.flashCol = var1;
      this.flash = Math.max(this.flash, var2);
   }

   float trauma() {
      return this.trauma;
   }

   void update(float var1) {
      for (int var2 = 0; var2 < this.n; var2++) {
         this.life[var2] = this.life[var2] + var1;
         if (this.life[var2] >= this.max[var2]) {
            this.n--;
            this.copy(this.n, var2);
            var2--;
         } else {
            float var3 = Math.max(0.0F, 1.0F - this.drag[var2] * var1);
            this.vx[var2] = this.vx[var2] * var3;
            this.vy[var2] = this.vy[var2] * var3;
            this.vy[var2] = this.vy[var2] + this.grav[var2] * var1;
            this.px[var2] = this.px[var2] + this.vx[var2] * var1;
            this.py[var2] = this.py[var2] + this.vy[var2] * var1;
         }
      }

      for (int var4 = 0; var4 < this.pn; var4++) {
         this.popT[var4] = this.popT[var4] + var1;
         if (this.popT[var4] > 1.1F) {
            this.pn--;
            this.popText[var4] = this.popText[this.pn];
            this.popX[var4] = this.popX[this.pn];
            this.popY[var4] = this.popY[this.pn];
            this.popT[var4] = this.popT[this.pn];
            this.popC[var4] = this.popC[this.pn];
            this.popS[var4] = this.popS[this.pn];
            var4--;
         }
      }

      this.trauma = Math.max(0.0F, this.trauma - var1 * 1.6F);
      this.flash = Math.max(0.0F, this.flash - var1 * 3.0F);
      this.shakeT += var1;
      float var5 = this.trauma * this.trauma * 7.0F;
      this.sx = var5 * (float)(Math.sin(this.shakeT * 83.1) * 0.6 + Math.sin(this.shakeT * 51.7) * 0.4);
      this.sy = var5 * (float)(Math.cos(this.shakeT * 71.3) * 0.6 + Math.sin(this.shakeT * 43.9) * 0.4);
   }

   private void copy(int var1, int var2) {
      this.px[var2] = this.px[var1];
      this.py[var2] = this.py[var1];
      this.vx[var2] = this.vx[var1];
      this.vy[var2] = this.vy[var1];
      this.life[var2] = this.life[var1];
      this.max[var2] = this.max[var1];
      this.size[var2] = this.size[var1];
      this.col[var2] = this.col[var1];
      this.kind[var2] = this.kind[var1];
      this.grav[var2] = this.grav[var1];
      this.drag[var2] = this.drag[var1];
   }

   void draw(GameView var1, float var2, float var3) {
      for (int var4 = 0; var4 < this.n; var4++) {
         float var5 = this.life[var4] / this.max[var4];
         float var6 = 1.0F - var5;
         float var7 = this.px[var4] - var2;
         float var8 = this.py[var4] - var3;
         float var9 = this.size[var4];
         int var10 = this.col[var4];
         switch (this.kind[var4]) {
            case 1:
               var1.box(var7 - var9 / 2.0F, var8 - var9 / 2.0F, var9, var9 * 0.7F, var9 * 0.2F, Gx.withAlpha(var10, Math.min(1.0F, var6 * 1.6F)));
               break;
            case 2:
               Gx.glow(var1.X(var7), var1.Y(var8), Math.max(1, var1.S(var9 * 2.4F)), Gx.withAlpha(var10, var6 * 0.5F));
               var1.circle(var7, var8, var9 * (1.0F - var5 * 0.6F), Gx.withAlpha(Gx.mix(var10, -1, 0.5F), var6));
               break;
            case 3:
               float var16 = var9 * Ease.outCubic(var5);
               Gx.ringAt(var1.X(var7), var1.Y(var8), Math.max(2, var1.S(var16)), Math.max(1, var1.S(Math.max(0.6F, 2.2F * var6))), Gx.withAlpha(var10, var6));
               break;
            case 4:
               var1.circle(var7, var8, var9 * (0.6F + 0.4F * var6), Gx.withAlpha(var10, var6 * 0.9F));
               break;
            case 5:
               float var11 = Math.min(4.0F, (Math.abs(this.vx[var4]) + Math.abs(this.vy[var4])) * 0.02F + 1.0F);
               float var12 = this.vx[var4];
               float var13 = this.vy[var4];
               float var14 = (float)Math.sqrt(var12 * var12 + var13 * var13) + 0.001F;

               for (int var15 = 0; var15 < 3; var15++) {
                  var1.circle(
                     var7 - var12 / var14 * var15 * var9 * var11 * 0.5F,
                     var8 - var13 / var14 * var15 * var9 * var11 * 0.5F,
                     var9 * (1.0F - var15 * 0.25F),
                     Gx.withAlpha(var10, var6 * (1.0F - var15 * 0.3F))
                  );
               }
               break;
            default:
               var1.circle(var7, var8, var9 * (0.5F + 0.5F * var6), Gx.withAlpha(var10, var6));
         }
      }
   }

   void drawPopups(GameView var1, float var2, float var3) {
      for (int var4 = 0; var4 < this.pn; var4++) {
         float var5 = this.popT[var4] / 1.1F;
         float var6 = var5 < 0.18F ? Ease.outBack(var5 / 0.18F) : 1.0F;
         float var7 = var5 < 0.65F ? 1.0F : 1.0F - (var5 - 0.65F) / 0.35F;
         float var8 = this.popX[var4] - var2;
         float var9 = this.popY[var4] - var3 - Ease.outCubic(var5) * 18.0F;
         Gx.push();
         Gx.scaleAt(var1.X(var8), var1.Y(var9), Math.max(0.05F, var6));
         outlined(var1, this.popText[var4], var8, var9, this.popS[var4], Gx.withAlpha(this.popC[var4], var7), Gx.withAlpha(-15723748, var7 * 0.8F));
         Gx.pop();
      }
   }

   void drawFlash(GameView var1, float var2, float var3) {
      if (this.flash > 0.01F) {
         var1.box(0.0F, 0.0F, var2, var3, 0.0F, Gx.withAlpha(this.flashCol, this.flash * 0.45F));
      }
   }

   static void sprite(GameView var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7, Consumer<Graphics2D> var8, int var9) {
      int var10 = Math.max(2, var0.S(var4));
      int var11 = Math.max(2, var0.S(var5));
      Gx.Img var12 = Gx.painted(var1, var10, var11, var5x -> {
         var5x.scale(var10 / var6, var11 / var7);
         var8.accept(var5x);
      });
      Gx.image(var12, var0.X(var2), var0.Y(var3), var10, var11, var9);
   }

   static void spriteAt(
      GameView var0,
      String var1,
      float var2,
      float var3,
      float var4,
      float var5,
      Consumer<Graphics2D> var6,
      float var7,
      float var8,
      float var9,
      float var10,
      int var11
   ) {
      int var12 = Math.max(2, var0.S(var2));
      int var13 = Math.max(2, var0.S(var3));
      Gx.Img var14 = Gx.painted(var1, var12, var13, var5x -> {
         var5x.scale(var12 / var4, var13 / var5);
         var6.accept(var5x);
      });
      Gx.image(var14, var0.X(var7), var0.Y(var8), Math.max(1, var0.S(var9)), Math.max(1, var0.S(var10)), var11);
   }

   static Area union(Shape... var0) {
      Area var1 = new Area();

      for (Shape var5 : var0) {
         var1.add(new Area(var5));
      }

      return var1;
   }

   static Color c(int var0) {
      return new Color(var0, true);
   }

   static void outlined(GameView var0, String var1, float var2, float var3, float var4, int var5, int var6) {
      float var7 = Math.max(0.6F, var4 * 0.07F);

      for (int var8 = 0; var8 < 8; var8++) {
         double var9 = var8 * Math.PI / 4.0;
         var0.text(var1, var2 + (float)Math.cos(var9) * var7, var3 + (float)Math.sin(var9) * var7 + var7 * 0.3F, var4, 3, var6);
      }

      var0.text(var1, var2, var3, var4, 3, var5);
   }
}
