package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AimTrainer extends GameView {
   private static final float W = 380.0F;
   private static final float H = 270.0F;
   private static final float TOP = 34.0F;
   private static final float DURATION = 30.0F;
   private static final float MAX_R = 18.0F;
   private final List<AimTrainer.Target> targets = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final List<float[]> rings = new ArrayList<>();
   private final List<float[]> pops = new ArrayList<>();
   private final List<float[]> misses = new ArrayList<>();
   private int hits;
   private int shots;
   private int expired;
   private int combo;
   private int bestCombo;
   private float spawnT;
   private float comboPop;

   public AimTrainer() {
      super("aim");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.targets.clear();
      this.parts.clear();
      this.rings.clear();
      this.pops.clear();
      this.misses.clear();
      this.hits = this.shots = this.expired = this.combo = this.bestCombo = 0;
      this.spawnT = 0.35F;
      this.comboPop = 0.0F;
   }

   private int accuracy() {
      return this.shots == 0 ? 100 : Math.round(this.hits * 100.0F / this.shots);
   }

   private void spawn() {
      float var1 = 24.0F;

      for (int var2 = 0; var2 < 30; var2++) {
         float var3 = var1 + this.rnd.nextFloat() * (380.0F - var1 * 2.0F);
         float var4 = 34.0F + var1 + this.rnd.nextFloat() * (236.0F - var1 * 2.0F);
         boolean var5 = true;

         for (AimTrainer.Target var7 : this.targets) {
            float var8 = var7.x - var3;
            float var9 = var7.y - var4;
            if (var8 * var8 + var9 * var9 < 2190.24F) {
               var5 = false;
               break;
            }
         }

         if (var5 || var2 == 29) {
            float var10 = 1.45F - 0.3F * Math.min(1.0F, this.time / 30.0F);
            this.targets.add(new AimTrainer.Target(var3, var4, var10));
            return;
         }
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         if (!(var2 < 30.0F)) {
            this.shots++;
            AimTrainer.Target var4 = null;
            float var5 = Float.MAX_VALUE;

            for (AimTrainer.Target var7 : this.targets) {
               float var8 = var7.radius();
               float var9 = var1 - var7.x;
               float var10 = var2 - var7.y;
               float var11 = (float)Math.sqrt(var9 * var9 + var10 * var10);
               if (var11 <= var8 + 1.2F && var11 / Math.max(1.0F, var8) < var5) {
                  var5 = var11 / Math.max(1.0F, var8);
                  var4 = var7;
               }
            }

            if (var4 == null) {
               this.combo = 0;
               this.misses.add(new float[]{var1, var2, 0.0F});
               Sound.play("minecraft:block.note_block.hat", 0.6F, 0.25F);
            } else {
               this.targets.remove(var4);
               int var12 = var5 <= 0.34F ? 100 : (var5 <= 0.67F ? 70 : 40);
               this.score += var12;
               this.hits++;
               this.combo++;
               this.bestCombo = Math.max(this.bestCombo, this.combo);
               this.comboPop = 1.0F;
               int var13 = var12 == 100 ? -15043 : (var12 == 70 ? Style.accent : -1);
               this.pops.add(new float[]{var4.x, var4.y, 0.0F, var12});
               this.rings.add(new float[]{var4.x, var4.y, 0.0F, var4.radius(), var13});

               for (int var14 = 0; var14 < 16; var14++) {
                  float var15 = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
                  float var16 = 50.0F + this.rnd.nextFloat() * 120.0F;
                  int var17 = var14 % 3 == 0 ? -1 : (var14 % 3 == 1 ? this.info.color : var13);
                  this.parts
                     .add(
                        new float[]{
                           var4.x,
                           var4.y,
                           (float)Math.cos(var15) * var16,
                           (float)Math.sin(var15) * var16 - 30.0F,
                           0.0F,
                           0.45F + this.rnd.nextFloat() * 0.35F,
                           2.0F + this.rnd.nextFloat() * 2.5F,
                           var17
                        }
                     );
               }

               Sound.play("minecraft:entity.arrow.hit_player", var12 == 100 ? 1.25F : (var12 == 70 ? 1.05F : 0.85F), 0.35F);
               if (var12 == 100) {
                  Sound.play("minecraft:entity.player.attack.crit", 1.2F, 0.2F);
               }

               if (this.targets.isEmpty()) {
                  this.spawnT = Math.min(this.spawnT, 0.08F);
               }
            }
         }
      }
   }

   @Override
   protected void update(float var1) {
      this.comboPop = Math.max(0.0F, this.comboPop - var1 * 4.0F);

      for (AimTrainer.Target var3 : this.targets) {
         var3.age += var1;
      }

      for (int var4 = this.targets.size() - 1; var4 >= 0; var4--) {
         AimTrainer.Target var10 = this.targets.get(var4);
         if (var10.age >= var10.life) {
            this.targets.remove(var4);
            this.expired++;
            this.combo = 0;
            this.rings.add(new float[]{var10.x, var10.y, 0.0F, 4.0F, -7696228.0F});
         }
      }

      for (float[] var11 : this.parts) {
         var11[0] += var11[2] * var1;
         var11[1] += var11[3] * var1;
         var11[3] += 360.0F * var1;
         var11[2] *= 0.98F;
         var11[4] += var1;
      }

      this.parts.removeIf(var0 -> var0[4] >= var0[5]);

      for (float[] var12 : this.rings) {
         var12[2] += var1;
      }

      this.rings.removeIf(var0 -> var0[2] > 0.45F);

      for (float[] var13 : this.pops) {
         var13[2] += var1;
      }

      this.pops.removeIf(var0 -> var0[2] > 0.8F);

      for (float[] var14 : this.misses) {
         var14[2] += var1;
      }

      this.misses.removeIf(var0 -> var0[2] > 0.5F);
      float var9 = Math.min(1.0F, this.time / 30.0F);
      int var15 = var9 < 0.3F ? 2 : 3;
      this.spawnT -= var1;
      if (this.targets.isEmpty() && this.spawnT > 0.12F) {
         this.spawnT = 0.12F;
      }

      if (this.spawnT <= 0.0F && this.targets.size() < var15) {
         this.spawn();
         this.spawnT = 0.62F - 0.22F * var9 + this.rnd.nextFloat() * 0.12F;
      }

      if (this.time + var1 >= 30.0F) {
         this.targets.clear();
         this.gameOver(true);
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 270.0F, 12.0F, Style.light ? -1117963 : -15724006, Style.light ? -2038806 : -16119024);
      int var1 = Style.light ? 201326592 : 184549375;

      for (float var2 = 20.0F; var2 < 380.0F; var2 += 20.0F) {
         this.box(var2, 34.0F, 0.6F, 236.0F, 0.0F, var1);
      }

      for (float var9 = 52.0F; var9 < 270.0F; var9 += 20.0F) {
         this.box(0.0F, var9, 380.0F, 0.6F, 0.0F, var1);
      }

      Gx.glow(this.X(190.0F), this.Y(145.0F), this.S(170.0F), Gx.withAlpha(this.info.color, 0.05F));

      for (float[] var3 : this.rings) {
         float var4 = var3[2] / 0.45F;
         float var5 = Ease.outCubic(var4);
         float var6 = var3[3] + var5 * 16.0F;
         Gx.ringAt(
            this.X(var3[0]), this.Y(var3[1]), this.S(var6), Math.max(1, this.S(2.2F * (1.0F - var4) + 0.4F)), Gx.withAlpha((int)var3[4], 0.8F * (1.0F - var4))
         );
      }

      for (AimTrainer.Target var15 : this.targets) {
         this.drawTarget(var15);
      }

      for (float[] var16 : this.parts) {
         float var19 = 1.0F - var16[4] / var16[5];
         this.box(var16[0] - var16[6] / 2.0F, var16[1] - var16[6] / 2.0F, var16[6], var16[6], var16[6] * 0.25F, Gx.withAlpha((int)var16[7], var19));
      }

      for (float[] var17 : this.misses) {
         float var20 = var17[2] / 0.5F;
         float var22 = 1.0F - var20;
         float var24 = 3.0F + Ease.outCubic(var20) * 2.0F;
         Gx.push();
         Gx.pushAlpha(var22);
         Gx.icon("x", this.X(var17[0]), this.Y(var17[1]), this.S(var24 * 2.4F), Style.danger);
         Gx.popAlpha();
         Gx.pop();
      }

      for (float[] var18 : this.pops) {
         float var21 = var18[2] / 0.8F;
         float var23 = var21 < 0.6F ? 1.0F : 1.0F - (var21 - 0.6F) / 0.4F;
         float var25 = Ease.outBack(Math.min(1.0F, var18[2] / 0.18F));
         int var7 = (int)var18[3];
         int var8 = var7 == 100 ? -15043 : (var7 == 70 ? Style.accent : -1);
         Gx.push();
         Gx.scaleAt(this.X(var18[0]), this.Y(var18[1] - 16.0F - var21 * 14.0F), 0.6F + 0.4F * var25);
         this.text("+" + var7, var18[0], var18[1] - 16.0F - var21 * 14.0F, var7 == 100 ? 11.0F : 9.0F, 3, Gx.withAlpha(var8, var23));
         Gx.pop();
      }

      this.hud();
      this.crosshair();
   }

   private void drawTarget(AimTrainer.Target var1) {
      float var2 = var1.radius();
      if (!(var2 < 0.6F)) {
         float var3 = var1.age / var1.life;
         int var4 = this.info.color;
         int var5 = Style.accent;
         Gx.glow(this.X(var1.x), this.Y(var1.y), this.S(var2 * 2.1F), Gx.withAlpha(var4, 0.28F));
         this.circle(var1.x, var1.y + var2 * 0.08F, var2 * 1.02F, 1426063360);
         this.circle(var1.x, var1.y, var2, Gx.mix(var4, -16777216, 0.12F));
         this.circle(var1.x, var1.y, var2 * 0.84F, -723465);
         this.circle(var1.x, var1.y, var2 * 0.67F, var4);
         this.circle(var1.x, var1.y, var2 * 0.5F, -723465);
         this.circle(var1.x, var1.y, var2 * 0.34F, var5);
         this.circle(var1.x, var1.y - var2 * 0.08F, var2 * 0.2F, Gx.mix(var5, -1, 0.55F));
         if (var3 > 0.6F) {
            float var6 = (var3 - 0.6F) / 0.4F;
            Gx.ringAt(this.X(var1.x), this.Y(var1.y), this.S(var2 + 2.5F), Math.max(1, this.S(0.8F)), Gx.withAlpha(Style.danger, 0.6F * var6));
         }
      }
   }

   private void hud() {
      float var1 = Math.min(1.0F, this.time / 30.0F);
      float var2 = Math.max(0.0F, 30.0F - this.time);
      this.box(0.0F, 0.0F, 380.0F, 30.0F, 12.0F, Style.light ? 251658240 : 1073741824);
      this.box(0.0F, 12.0F, 380.0F, 18.0F, 0.0F, Style.light ? 251658240 : 1073741824);
      this.box(0.0F, 30.0F, 380.0F, 0.7F, 0.0F, Style.stroke);
      int var3 = var2 < 5.0F ? Gx.mix(Style.danger, -1, (float)(0.5 + 0.5 * Math.sin(this.time * 12.0F)) * 0.3F) : Style.accent;
      this.box(0.0F, 28.5F, 380.0F, 3.0F, 0.0F, Style.light ? 335544320 : 352321535);
      this.box(0.0F, 28.5F, 380.0F * (1.0F - var1), 3.0F, 0.0F, var3);
      Gx.glow(this.X(380.0F * (1.0F - var1)), this.Y(30.0F), this.S(10.0F), Gx.withAlpha(var3, 0.5F));
      float var4 = 14.5F;
      String var5 = String.format(Locale.ROOT, "%.1f s", var2);
      Gx.icon("clock", this.X(166.0F), this.Y(var4), this.S(9.0F), var2 < 5.0F ? Style.danger : Style.sub);
      Gx.textMid(var5, this.X(173.0F), this.Y(var4), this.S(10.0F), 3, var2 < 5.0F ? Style.danger : Style.text);
      this.stat(10.0F, var4, "target", "Treffer", String.valueOf(this.hits));
      this.stat(84.0F, var4, "crosshair", "Genauigkeit", this.accuracy() + "%");
      float var6 = 1.0F + 0.25F * Ease.outCubic(this.comboPop);
      String var7 = "x" + this.combo;
      float var8 = Gx.width(var7, this.S(10.0F), 3) / this.u;
      float var9 = 370.0F;
      Gx.push();
      Gx.scaleAt(this.X(var9 - var8 / 2.0F), this.Y(var4), var6);
      Gx.textRight(var7, this.X(var9), this.Y(var4), this.S(10.0F), 3, this.combo >= 5 ? -15043 : (this.combo > 0 ? Style.accent : Style.muted));
      Gx.pop();
      Gx.textRight("COMBO", this.X(var9 - var8 - 5.0F), this.Y(var4), this.S(6.0F), 3, Style.muted);
      if (this.combo >= 5) {
         Gx.icon("flame", this.X(var9 - var8 - 40.0F), this.Y(var4), this.S(9.0F), -23235);
      }
   }

   private void stat(float var1, float var2, String var3, String var4, String var5) {
      Gx.icon(var3, this.X(var1 + 5.0F), this.Y(var2), this.S(9.0F), Style.sub);
      Gx.textMid(var4, this.X(var1 + 13.0F), this.Y(var2 - 4.5F), this.S(5.4F), 3, Style.muted);
      Gx.textMid(var5, this.X(var1 + 13.0F), this.Y(var2 + 4.2F), this.S(8.5F), 3, Style.text);
   }

   private void crosshair() {
      float var1 = (this.mx - this.bx) / this.u;
      float var2 = (this.my - this.by) / this.u;
      if (this.state == GameView.State.PLAYING && !(var1 < 0.0F) && !(var2 < 34.0F) && !(var1 > 380.0F) && !(var2 > 270.0F)) {
         int var3 = Math.round(this.mx);
         int var4 = Math.round(this.my);
         int var5 = this.S(4.5F);
         int var6 = this.S(2.2F);
         int var7 = Math.max(1, this.S(0.9F));
         int var8 = -2013265920;
         byte var9 = -1;

         for (int var10 = 0; var10 < 2; var10++) {
            int var11 = var10 == 0 ? Math.max(1, this.S(0.5F)) : 0;
            int var12 = var10 == 0 ? var8 : var9;
            int var13 = var7 + (var10 == 0 ? 2 * var11 : 0);
            Gx.fill(var3 - var6 - var5 - var11, var4 - var13 / 2, var5 + 2 * var11, var13, var12);
            Gx.fill(var3 + var6 - var11, var4 - var13 / 2, var5 + 2 * var11, var13, var12);
            Gx.fill(var3 - var13 / 2, var4 - var6 - var5 - var11, var13, var5 + 2 * var11, var12);
            Gx.fill(var3 - var13 / 2, var4 + var6 - var11, var13, var5 + 2 * var11, var12);
         }

         Gx.circle(var3, var4, Math.max(1, this.S(0.8F)), Style.accent);
      }
   }

   private static final class Target {
      float x;
      float y;
      float age;
      float life;

      Target(float var1, float var2, float var3) {
         this.x = var1;
         this.y = var2;
         this.life = var3;
      }

      float radius() {
         float var1 = 0.14F;
         return this.age < var1 ? 18.0F * Math.max(0.0F, Ease.outBack(this.age / var1)) : 18.0F * Math.max(0.0F, 1.0F - (this.age - var1) / (this.life - var1));
      }
   }
}
