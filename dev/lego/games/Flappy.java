package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Flappy extends GameView {
   private static final float W = 360.0F;
   private static final float H = 280.0F;
   private static final float GROUND = 254.0F;
   private static final float BIRD_X = 88.0F;
   private static final float BW = 24.0F;
   private static final float BH = 17.0F;
   private static final float PW = 40.0F;
   private static final float ROW = 12.0F;
   private static final float SPACING = 165.0F;
   private static final float GRAVITY = 980.0F;
   private static final float FLAP = -268.0F;
   private static final float MAX_FALL = 420.0F;
   private static final int[] COLORS = new int[]{-3597815, -16755265, -11821238, -95720, -7194248, -13193537};
   private static final int BIRD = -864969;
   private final List<Flappy.Pillar> pillars = new ArrayList<>();
   private final List<float[]> puffs = new ArrayList<>();
   private final float[][] clouds = new float[7][3];
   private float y;
   private float vy;
   private float scroll;
   private float wing;
   private float flash;
   private float anim;
   private float scorePop;
   private boolean flying;
   private int colorIdx;

   public Flappy() {
      super("flappy");
      Random var1 = new Random(7L);

      for (int var2 = 0; var2 < this.clouds.length; var2++) {
         this.clouds[var2][0] = var2 * 480.0F / this.clouds.length + var1.nextFloat() * 30.0F;
         this.clouds[var2][1] = 22.0F + var1.nextFloat() * 80.0F;
         this.clouds[var2][2] = 0.7F + var1.nextFloat() * 0.6F;
      }
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Säulen";
   }

   @Override
   protected void reset() {
      this.pillars.clear();
      this.puffs.clear();
      this.y = 117.6F;
      this.vy = 0.0F;
      this.wing = 0.0F;
      this.flash = 0.0F;
      this.scorePop = 0.0F;
      this.flying = false;
      this.colorIdx = this.rnd.nextInt(COLORS.length);
   }

   private void flap() {
      if (!this.flying) {
         this.flying = true;
         this.addPillar(420.0F);
      }

      this.vy = -268.0F;
      this.wing = 1.0F;

      for (int var1 = 0; var1 < 4; var1++) {
         this.puffs.add(new float[]{78.0F, this.y + 4.0F, -40.0F - this.rnd.nextFloat() * 50.0F, 10.0F + this.rnd.nextFloat() * 40.0F - 20.0F, 0.0F});
      }

      Sound.play("minecraft:entity.bat.takeoff", 1.6F + this.rnd.nextFloat() * 0.2F, 0.12F);
   }

   private void addPillar(float var1) {
      Flappy.Pillar var2 = new Flappy.Pillar();
      var2.x = var1;
      var2.gap = Math.max(70.0F, 90.0F - (float)this.score * 0.8F);
      float var3 = 34.0F + var2.gap / 2.0F;
      float var4 = 224.0F - var2.gap / 2.0F;
      var2.gapY = var3 + this.rnd.nextFloat() * (var4 - var3);
      var2.color = COLORS[this.colorIdx++ % COLORS.length];
      this.pillars.add(var2);
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.flap();
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.flap();
      }
   }

   private float speed() {
      return 112.0F + Math.min(40.0F, (float)this.score * 1.2F);
   }

   @Override
   protected void update(float var1) {
      float var2 = this.speed();
      this.scroll += var2 * var1;
      this.wing = Math.max(0.0F, this.wing - var1 * 5.0F);
      this.scorePop = Math.max(0.0F, this.scorePop - var1 * 3.0F);

      for (float[] var4 : this.puffs) {
         var4[0] += var4[2] * var1;
         var4[1] += var4[3] * var1;
         var4[4] += var1;
      }

      this.puffs.removeIf(var0 -> var0[4] > 0.45F);
      if (!this.flying) {
         this.y = 117.6F + (float)Math.sin(this.time * 5.0F) * 5.0F;
      } else {
         this.vy = Math.min(420.0F, this.vy + 980.0F * var1);
         this.y = this.y + this.vy * var1;
         if (this.y < 8.5F) {
            this.y = 8.5F;
            this.vy = Math.max(this.vy, 0.0F);
         }

         for (Flappy.Pillar var14 : this.pillars) {
            var14.x -= var2 * var1;
         }

         this.pillars.removeIf(var0 -> var0.x < -60.0F);
         Flappy.Pillar var13 = this.pillars.isEmpty() ? null : this.pillars.get(this.pillars.size() - 1);
         if (var13 == null || var13.x < 215.0F) {
            this.addPillar(var13 == null ? 380.0F : var13.x + 165.0F);
         }

         float var15 = 79.0F;
         float var5 = 98.0F;
         float var6 = this.y - 8.5F + 3.0F;
         float var7 = this.y + 8.5F - 2.0F;

         for (Flappy.Pillar var9 : this.pillars) {
            if (!var9.passed && var9.x + 40.0F < 76.0F) {
               var9.passed = true;
               this.score++;
               this.scorePop = 1.0F;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.3F + this.rnd.nextFloat() * 0.2F, 0.25F);
            }

            float var10 = var9.x - 3.0F;
            float var11 = var9.x + 40.0F + 3.0F;
            if (var5 > var10 && var15 < var11 && (var6 < var9.gapY - var9.gap / 2.0F || var7 > var9.gapY + var9.gap / 2.0F)) {
               this.crash();
               return;
            }
         }

         if (var7 >= 254.0F) {
            this.y = 247.5F;
            this.crash();
         }
      }
   }

   private void crash() {
      this.flash = 1.0F;
      Sound.play("minecraft:entity.arrow.hit_player", 0.8F, 0.4F);
      this.gameOver();
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      this.flash = Math.max(0.0F, this.flash - var1 * 2.5F);
      if (this.state == GameView.State.READY) {
         this.y = 117.6F + (float)Math.sin(this.anim * 4.0F) * 5.0F;
      }

      this.boxV(0.0F, 0.0F, 360.0F, 266.0F, 12.0F, -14001261, -7091736);
      Gx.glow(this.X(290.0F), this.Y(52.0F), this.S(90.0F), 1442837444);
      this.circle(290.0F, 52.0F, 17.0F, -3656);
      this.circle(290.0F, 52.0F, 14.0F, -1828);
      float var2 = 480.0F;

      for (float[] var6 : this.clouds) {
         float var7 = ((var6[0] - this.scroll * 0.12F) % var2 + var2) % var2 - 60.0F;
         this.cloud(var7, var6[1], var6[2]);
      }

      this.hills(0.22F, 150.0F, 70.0F, 228.0F, -9459808, 1);
      this.hills(0.45F, 120.0F, 52.0F, 248.0F, -12612518, 2);

      for (Flappy.Pillar var11 : this.pillars) {
         this.pillar(var11);
      }

      this.box(0.0F, 254.0F, 360.0F, 26.0F, 12.0F, -9745874);
      this.boxV(0.0F, 254.0F, 360.0F, 12.0F, 0.0F, -13723058, -14714310);
      this.box(0.0F, 254.0F, 360.0F, 2.0F, 0.0F, -10763146);
      float var10 = this.scroll % 12.0F;

      for (float var12 = -var10; var12 < 372.0F; var12 += 12.0F) {
         this.boxV(var12 + 2.5F, 250.5F, 7.0F, 4.5F, 1.2F, -10303106, -13723058);
      }

      for (float var13 = -(this.scroll % 24.0F); var13 < 384.0F; var13 += 24.0F) {
         if (var13 > 6.0F && var13 < 354.0F) {
            this.box(var13, 266.0F, 1.2F, 10.0F, 0.0F, 855638016);
         }
      }

      this.box(0.0F, 266.0F, 360.0F, 1.5F, 0.0F, 1140850688);

      for (float[] var17 : this.puffs) {
         float var19 = 1.0F - var17[4] / 0.45F;
         this.circle(var17[0], var17[1], 2.0F + var17[4] * 8.0F, Gx.withAlpha(-1, var19 * 0.6F));
      }

      this.bird();
      if (this.flying || this.state == GameView.State.OVER) {
         float var16 = 1.0F + 0.25F * Ease.outCubic(this.scorePop);
         Gx.push();
         Gx.scaleAt(this.X(180.0F), this.Y(34.0F), var16);
         String var18 = String.valueOf(this.score);

         for (int var20 = 0; var20 < 8; var20++) {
            double var21 = var20 * Math.PI / 4.0;
            this.text(var18, 180.0F + (float)Math.cos(var21) * 1.4F, 34.0F + (float)Math.sin(var21) * 1.4F + 0.6F, 26.0F, 3, -1441060294);
         }

         this.text(var18, 180.0F, 34.0F, 26.0F, 3, -1);
         Gx.pop();
      } else if (this.state == GameView.State.PLAYING) {
         float var15 = 0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F);
         this.box(102.0F, 161.6F, 156.0F, 24.0F, 12.0F, 1426063360);
         this.text("Leertaste oder Klick zum Flattern", 180.0F, 173.6F, 7.6F, 3, Gx.withAlpha(-1, var15));
      }

      if (this.flash > 0.0F) {
         this.box(0.0F, 0.0F, 360.0F, 280.0F, 0.0F, Gx.withAlpha(-1, this.flash * 0.55F));
      }
   }

   private void cloud(float var1, float var2, float var3) {
      int var4 = -1116934;
      this.circle(var1, var2, 11.0F * var3, var4);
      this.circle(var1 + 13.0F * var3, var2 - 5.0F * var3, 14.0F * var3, var4);
      this.circle(var1 + 28.0F * var3, var2, 11.0F * var3, var4);
      this.box(var1, var2 - 1.0F * var3, 28.0F * var3, 11.0F * var3, 5.0F * var3, var4);
   }

   private void hills(float var1, float var2, float var3, float var4, int var5, int var6) {
      float var7 = this.scroll * var1 % var2;

      for (float var8 = -var7 - var2; var8 < 360.0F + var2; var8 += var2) {
         int var9 = Math.floorMod(Math.round((var8 + this.scroll * var1) / var2) * 31 + var6 * 7, 5);
         float var10 = var3 * (0.8F + var9 * 0.1F);
         this.circle(var8 + var2 / 2.0F, var4 + var10 * 0.55F, var10, var5);
         if (var6 == 2) {
            float var11 = var4 + var10 * 0.55F - var10;
            this.box(var8 + var2 / 2.0F - 9.0F, var11 - 2.5F, 7.0F, 3.5F, 1.0F, Gx.mix(var5, -1, 0.12F));
            this.box(var8 + var2 / 2.0F + 2.0F, var11 - 2.5F, 7.0F, 3.5F, 1.0F, Gx.mix(var5, -1, 0.12F));
         }
      }

      this.box(0.0F, var4, 360.0F, 254.0F - var4 + 1.0F, 0.0F, var5);
   }

   private void sideBrick(float var1, float var2, float var3, float var4, int var5, int var6) {
      if (var6 > 0) {
         float var7 = 7.0F;
         float var8 = (var3 - var6 * var7) / var6;

         for (int var9 = 0; var9 < var6; var9++) {
            float var10 = var1 + var8 / 2.0F + var9 * (var7 + var8);
            this.boxV(var10, var2 - 3.0F, var7, 4.0F, 1.2F, Gx.mix(var5, -1, 0.25F), var5);
         }
      }

      this.boxV(var1, var2, var3, var4, 2.0F, Gx.mix(var5, -1, 0.14F), Gx.mix(var5, -16777216, 0.16F));
      this.box(var1 + 1.5F, var2 + 1.0F, var3 - 3.0F, 1.2F, 0.6F, Gx.withAlpha(-1, 0.28F));
      Gx.outline(
         this.X(var1), this.Y(var2), this.S(var3), this.S(var4), this.S(2.0F), Math.max(1, this.S(0.6F)), Gx.withAlpha(Gx.mix(var5, -16777216, 0.55F), 0.7F)
      );
   }

   private void pillar(Flappy.Pillar var1) {
      float var2 = var1.gapY - var1.gap / 2.0F;
      float var3 = var1.gapY + var1.gap / 2.0F;
      int var4 = var1.color;
      int var5 = Gx.mix(var4, -16777216, 0.12F);
      Gx.shadow(this.X(var1.x), this.Y(-10.0F), this.S(40.0F), this.S(var2 + 10.0F), this.S(2.0F), this.S(4.0F), 0.25F);
      Gx.shadow(this.X(var1.x), this.Y(var3), this.S(40.0F), this.S(254.0F - var3), this.S(2.0F), this.S(4.0F), 0.25F);
      int var6 = 0;

      for (float var7 = var2 - 24.0F; var7 > -12.0F; var6++) {
         this.sideBrick(var1.x + (var6 % 2 == 0 ? 0 : 0), var7, 40.0F, 12.0F, var6 % 2 == 0 ? var4 : var5, 0);
         var7 -= 12.0F;
      }

      this.sideBrick(var1.x - 3.0F, var2 - 12.0F, 46.0F, 12.0F, Gx.mix(var4, -1, 0.08F), 0);
      var6 = 0;

      for (float var9 = var3 + 12.0F; var9 < 254.0F; var6++) {
         this.sideBrick(var1.x, var9, 40.0F, 12.0F, var6 % 2 == 0 ? var5 : var4, 0);
         var9 += 12.0F;
      }

      this.sideBrick(var1.x - 3.0F, var3, 46.0F, 12.0F, Gx.mix(var4, -1, 0.08F), 3);
   }

   private void bird() {
      float var1 = !this.flying && this.state != GameView.State.OVER
         ? (float)Math.sin(this.anim * 4.0F) * 4.0F
         : Math.max(-24.0F, Math.min(55.0F, this.vy / 420.0F * 60.0F));
      int var2 = Math.round(var1 / 3.0F) * 3;
      int var3 = Math.max(8, this.S(40.0F));
      double var4 = Math.toRadians(var2);
      Gx.glow(this.X(88.0F), this.Y(this.y), this.S(26.0F), 587199392);
      Gx.Img var6 = Gx.painted("flappyBird" + var2, var3, var3, var3x -> paintBird(var3x, var3, var4));
      Gx.image(var6, this.X(88.0F) - var3 / 2, this.Y(this.y) - var3 / 2, var3, var3, -1);
      float var7 = -4.0F;
      float var8 = 2.0F + (this.wing > 0.0F ? -4.0F * this.wing : 0.0F);
      float var9 = (float)Math.cos(var4);
      float var10 = (float)Math.sin(var4);
      float var11 = 88.0F + var7 * var9 - var8 * var10;
      float var12 = this.y + var7 * var10 + var8 * var9;
      float var13 = 4.0F + 3.0F * this.wing;
      this.boxV(var11 - 5.0F, var12 - var13 / 2.0F, 10.0F, var13, 2.0F, -7558, -2053860);
      Gx.outline(this.X(var11 - 5.0F), this.Y(var12 - var13 / 2.0F), this.S(10.0F), this.S(var13), this.S(2.0F), Math.max(1, this.S(0.5F)), -2005905664);
   }

   private static void paintBird(Graphics2D var0, int var1, double var2) {
      float var4 = var1 / 40.0F;
      var0.translate(var1 / 2.0, var1 / 2.0);
      var0.rotate(var2);
      var0.scale(var4, var4);
      Color var5 = new Color(-864969, true);
      Color var6 = new Color(Gx.mix(-864969, -1, 0.3F), true);
      Color var7 = new Color(Gx.mix(-864969, -16777216, 0.2F), true);
      Color var8 = new Color(Gx.mix(-864969, -16777216, 0.55F), true);
      var0.setStroke(new BasicStroke(0.8F));

      for (int var9 = 0; var9 < 2; var9++) {
         Float var10 = new Float(-9.5F + var9 * 11, -11.7F, 8.0F, 4.2F, 2.0F, 2.0F);
         var0.setPaint(new GradientPaint(0.0F, -11.5F, var6, 0.0F, -8.5F, var5));
         var0.fill(var10);
         var0.setColor(var8);
         var0.draw(var10);
      }

      Float var11 = new Float(-12.0F, -8.5F, 24.0F, 17.0F, 5.0F, 5.0F);
      var0.setPaint(new GradientPaint(0.0F, -8.5F, var6, 0.0F, 8.5F, var7));
      var0.fill(var11);
      var0.setColor(new Color(255, 255, 255, 80));
      var0.fill(new Float(-10.0F, -7.3F, 20.0F, 1.6F, 1.6F, 1.6F));
      var0.setColor(var8);
      var0.draw(var11);
      java.awt.geom.Path2D.Float var12 = new java.awt.geom.Path2D.Float();
      var12.moveTo(11.0, -1.5);
      var12.lineTo(18.0, 1.5);
      var12.lineTo(11.0, 4.5);
      var12.closePath();
      var0.setColor(new Color(16681496));
      var0.fill(var12);
      var0.setColor(new Color(10111488));
      var0.draw(var12);
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(3.2F, -6.2F, 7.0F, 7.0F));
      var0.setColor(new Color(8018432));
      var0.draw(new java.awt.geom.Ellipse2D.Float(3.2F, -6.2F, 7.0F, 7.0F));
      var0.setColor(new Color(1381914));
      var0.fill(new java.awt.geom.Ellipse2D.Float(6.4F, -4.4F, 3.2F, 3.4F));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(7.2F, -4.1F, 1.1F, 1.1F));
      var0.setColor(new Color(255, 110, 80, 90));
      var0.fill(new java.awt.geom.Ellipse2D.Float(2.5F, 1.8F, 5.0F, 3.0F));
   }

   private static final class Pillar {
      float x;
      float gapY;
      float gap;
      int color;
      boolean passed;
   }
}
