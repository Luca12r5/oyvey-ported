package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Runner extends GameView {
   private static final float W = 400.0F;
   private static final float H = 240.0F;
   private static final float GY = 196.0F;
   private static final float PX = 56.0F;
   private static final float GRAVITY = 1650.0F;
   private static final float JUMP_V = -470.0F;
   private static final float CUT_V = -170.0F;
   private float py;
   private float vy;
   private float runPhase;
   private float duckAnim;
   private float squash;
   private boolean onGround;
   private boolean jumpHeld;
   private boolean duck;
   private boolean fastFall;
   private float jumpBuffer;
   private float coyote;
   private float speed;
   private float dist;
   private float nextSpawn;
   private float shake;
   private float flash;
   private float deadT;
   private float milestoneT;
   private boolean dead;
   private long lastMilestone;
   private final List<Runner.Obstacle> obstacles = new ArrayList<>();
   private final List<float[]> parts = new ArrayList<>();
   private final float[] cloudX = new float[5];
   private final float[] cloudY = new float[5];
   private final float[] cloudS = new float[5];

   public Runner() {
      super("runner");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Distanz";
   }

   @Override
   protected void reset() {
      this.py = 196.0F;
      this.vy = 0.0F;
      this.onGround = true;
      this.jumpHeld = this.duck = this.fastFall = this.dead = false;
      this.jumpBuffer = this.coyote = 0.0F;
      this.runPhase = this.duckAnim = this.squash = 0.0F;
      this.speed = 165.0F;
      this.dist = 0.0F;
      this.nextSpawn = 260.0F;
      this.shake = this.flash = this.deadT = this.milestoneT = 0.0F;
      this.lastMilestone = 0L;
      this.obstacles.clear();
      this.parts.clear();

      for (int var1 = 0; var1 < this.cloudX.length; var1++) {
         this.cloudX[var1] = var1 * (400.0F / this.cloudX.length) + this.rnd.nextFloat() * 40.0F;
         this.cloudY[var1] = 22.0F + this.rnd.nextFloat() * 60.0F;
         this.cloudS[var1] = 0.7F + this.rnd.nextFloat() * 0.6F;
      }
   }

   private void pressJump() {
      this.jumpHeld = true;
      this.jumpBuffer = 0.12F;
   }

   private void releaseJump() {
      this.jumpHeld = false;
      if (!this.onGround && this.vy < -170.0F) {
         this.vy = -170.0F;
      }
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.pressJump();
      }

      if (var1 == 264 || var1 == 83) {
         this.duck = true;
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 32 || var1 == 265 || var1 == 87) {
         this.releaseJump();
      }

      if (var1 == 264 || var1 == 83) {
         this.duck = false;
      }
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.pressJump();
      }
   }

   @Override
   protected void onRelease(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.releaseJump();
      }
   }

   @Override
   protected void update(float var1) {
      this.shake = Math.max(0.0F, this.shake - var1 * 2.2F);
      this.flash = Math.max(0.0F, this.flash - var1 * 2.5F);
      this.milestoneT = Math.max(0.0F, this.milestoneT - var1);
      this.squash = Math.max(0.0F, this.squash - var1 * 5.0F);
      this.updateParticles(var1);
      if (this.dead) {
         this.deadT += var1;
         if (this.deadT > 0.75F) {
            this.gameOver();
         }
      } else {
         this.speed = Math.min(430.0F, this.speed + var1 * 4.2F);
         float var2 = this.speed * var1;
         this.dist += var2;
         this.score = (long)(this.dist / 10.0F);
         if (this.score / 100L > this.lastMilestone) {
            this.lastMilestone = this.score / 100L;
            this.milestoneT = 1.2F;
            Sound.play("minecraft:block.note_block.pling", 1.5F, 0.3F);
         }

         this.runPhase = this.runPhase + var1 * (8.0F + this.speed / 28.0F);

         for (int var3 = 0; var3 < this.cloudX.length; var3++) {
            this.cloudX[var3] = this.cloudX[var3] - var2 * 0.12F * this.cloudS[var3];
            if (this.cloudX[var3] < -70.0F) {
               this.cloudX[var3] = 420.0F + this.rnd.nextFloat() * 80.0F;
               this.cloudY[var3] = 22.0F + this.rnd.nextFloat() * 60.0F;
               this.cloudS[var3] = 0.7F + this.rnd.nextFloat() * 0.6F;
            }
         }

         this.jumpBuffer = Math.max(0.0F, this.jumpBuffer - var1);
         this.coyote = this.onGround ? 0.08F : Math.max(0.0F, this.coyote - var1);
         if (this.jumpBuffer > 0.0F && (this.onGround || this.coyote > 0.0F)) {
            this.vy = -470.0F;
            this.onGround = false;
            this.coyote = 0.0F;
            this.jumpBuffer = 0.0F;
            this.squash = 0.6F;
            this.dust(64.0F, 196.0F, 5);
            Sound.play("minecraft:entity.chicken.egg", 1.6F + this.rnd.nextFloat() * 0.2F, 0.18F);
            if (!this.jumpHeld) {
               this.vy = -272.0F;
            }
         }

         if (!this.onGround) {
            this.fastFall = this.duck;
            this.vy = this.vy + 1650.0F * (this.fastFall ? 2.6F : 1.0F) * var1;
            this.py = this.py + this.vy * var1;
            if (this.py >= 196.0F) {
               this.py = 196.0F;
               this.vy = 0.0F;
               this.onGround = true;
               this.squash = 1.0F;
               this.dust(64.0F, 196.0F, 7);
            }
         }

         this.duckAnim = this.duckAnim + ((this.duck && this.onGround ? 1 : 0) - this.duckAnim) * Math.min(1.0F, var1 * 22.0F);
         if (this.onGround && this.rnd.nextFloat() < 0.18F) {
            this.dust(59.0F, 196.0F, 1);
         }

         for (Runner.Obstacle var4 : this.obstacles) {
            var4.x = var4.x - var2 * (var4.bird ? 1.12F : 1.0F);
            var4.phase += var1 * 9.0F;
         }

         this.obstacles.removeIf(var0 -> var0.x + var0.w < -30.0F);
         this.nextSpawn -= var2;
         if (this.nextSpawn <= 0.0F) {
            this.spawn();
         }

         float[] var9 = this.playerBox();

         for (Runner.Obstacle var5 : this.obstacles) {
            float var6 = var5.bird ? 4.0F : 2.5F;
            float var7 = var5.bird ? 3.0F : 2.5F;
            if (var9[0] < var5.x + var5.w - var6 && var9[0] + var9[2] > var5.x + var6 && var9[1] < var5.y + var5.h - var7 && var9[1] + var9[3] > var5.y + var7) {
               this.die();
               return;
            }
         }
      }
   }

   private void die() {
      this.dead = true;
      this.deadT = 0.0F;
      this.shake = 1.0F;
      this.flash = 1.0F;
      Sound.play("minecraft:entity.player.hurt", 1.0F, 0.35F);
      Sound.play("minecraft:block.stone.break", 0.9F, 0.3F);
      float var1 = 64.0F;
      float var2 = this.py - 15.0F;
      int[] var3 = new int[]{Style.accent, -13053, -13931303, -1};

      for (int var4 = 0; var4 < 22; var4++) {
         float var5 = (float)(this.rnd.nextFloat() * Math.PI * 2.0);
         float var6 = 60.0F + this.rnd.nextFloat() * 160.0F;
         this.parts
            .add(
               new float[]{
                  var1,
                  var2,
                  (float)Math.cos(var5) * var6,
                  (float)Math.sin(var5) * var6 - 80.0F,
                  0.0F,
                  0.7F + this.rnd.nextFloat() * 0.5F,
                  2.0F + this.rnd.nextFloat() * 3.0F,
                  var3[this.rnd.nextInt(var3.length)]
               }
            );
      }
   }

   private void dust(float var1, float var2, int var3) {
      int var4 = Style.light ? -6647936 : -4607322;

      for (int var5 = 0; var5 < var3; var5++) {
         this.parts
            .add(
               new float[]{
                  var1 + this.rnd.nextFloat() * 8.0F - 4.0F,
                  var2 - 1.0F,
                  -this.speed * 0.3F - this.rnd.nextFloat() * 40.0F,
                  -20.0F - this.rnd.nextFloat() * 40.0F,
                  0.0F,
                  0.35F + this.rnd.nextFloat() * 0.25F,
                  1.2F + this.rnd.nextFloat() * 1.6F,
                  var4
               }
            );
      }
   }

   private void updateParticles(float var1) {
      for (float[] var3 : this.parts) {
         var3[0] += var3[2] * var1;
         var3[1] += var3[3] * var1;
         var3[3] += 500.0F * var1;
         var3[4] += var1;
         if (var3[1] > 199.0F && var3[3] > 0.0F) {
            var3[1] = 199.0F;
            var3[3] *= -0.35F;
            var3[2] *= 0.7F;
         }
      }

      this.parts.removeIf(var0 -> var0[4] >= var0[5]);
   }

   private void spawn() {
      Runner.Obstacle var1 = new Runner.Obstacle();
      boolean var2 = this.score > 220L;
      if (var2 && this.rnd.nextFloat() < 0.3F) {
         var1.bird = true;
         var1.w = 22.0F;
         var1.h = 13.0F;
         boolean var6 = this.rnd.nextBoolean();
         var1.y = var6 ? 159.0F : 175.0F;
         var1.x = 410.0F;
         var1.phase = this.rnd.nextFloat() * 6.0F;
      } else {
         int var3 = 1 + this.rnd.nextInt(this.score > 120L ? 3 : 2);
         var1.colW = 11 + this.rnd.nextInt(2) * 3;
         var1.cols = new int[var3];
         int var4 = 1;

         for (int var5 = 0; var5 < var3; var5++) {
            var1.cols[var5] = 1 + this.rnd.nextInt(this.score > 60L ? 4 : 3);
            var4 = Math.max(var4, var1.cols[var5]);
         }

         var1.w = var3 * var1.colW;
         var1.h = var4 * 10;
         var1.y = 196.0F - var1.h;
         var1.x = 410.0F;
      }

      this.obstacles.add(var1);
      float var7 = 150.0F + this.speed * 0.55F;
      this.nextSpawn = var7 + this.rnd.nextFloat() * (140.0F + this.speed * 0.3F);
      if (this.rnd.nextFloat() < 0.12F && !var1.bird) {
         this.nextSpawn = Math.max(34.0F, var1.w + 22.0F);
      }
   }

   private float[] playerBox() {
      return this.duckAnim > 0.5F ? new float[]{53.0F, this.py - 17.0F, 24.0F, 16.0F} : new float[]{58.0F, this.py - 30.0F, 13.0F, 29.0F};
   }

   @Override
   protected void render() {
      float var1 = this.time;
      float var2 = 0.0F;
      float var3 = 0.0F;
      if (this.shake > 0.0F) {
         float var4 = this.shake * this.shake * 5.0F;
         var2 = (float)Math.sin(var1 * 91.0F) * var4;
         var3 = (float)Math.cos(var1 * 77.0F) * var4;
      }

      Gx.push();
      Gx.translate(this.S(var2), this.S(var3));
      int var16 = Style.light ? -4204046 : -15590869;
      int var5 = Style.light ? -726824 : -14475213;
      int var6 = Style.light ? -3557478 : -14014943;
      int var7 = Style.light ? -5202818 : -15067372;
      this.boxV(0.0F, 182.0F, 400.0F, 58.0F, 12.0F, var6, var7);
      this.boxV(0.0F, 0.0F, 400.0F, 196.0F, 12.0F, var16, var5);
      this.box(0.0F, 182.0F, 400.0F, 15.0F, 0.0F, var5);
      Gx.glow(this.X(330.0F), this.Y(52.0F), this.S(70.0F), Gx.withAlpha(Style.light ? -6496 : Style.accent, Style.light ? 0.7F : 0.22F));
      this.circle(330.0F, 52.0F, 13.0F, Style.light ? -8054 : Gx.mix(Style.accent, -724506, 0.85F));
      if (!Style.light) {
         this.circle(326.0F, 49.0F, 2.5F, 402653184);
         this.circle(335.0F, 57.0F, 1.8F, 402653184);
      }

      for (int var8 = 0; var8 < this.cloudX.length; var8++) {
         this.cloud(this.cloudX[var8], this.cloudY[var8], this.cloudS[var8]);
      }

      this.mountains(this.dist * 0.08F, 150.0F, 0.6F, Style.light ? -4864291 : -14735816);
      this.mountains(this.dist * 0.2F + 97.0F, 110.0F, 0.85F, Style.light ? -7033910 : -13945774);
      this.box(0.0F, 194.8F, 400.0F, 2.4F, 0.0F, Style.light ? -8425899 : -9608620);
      float var17 = this.dist % 26.0F;

      for (float var9 = -var17; var9 < 426.0F; var9 += 26.0F) {
         this.box(var9, 203.0F, 12.0F, 2.0F, 1.0F, Style.light ? 1073741824 : 822083583);
      }

      float var18 = this.dist * 1.0F % 61.0F;

      for (float var10 = -var18; var10 < 461.0F; var10 += 61.0F) {
         this.box(var10 + 20.0F, 213.0F, 5.0F, 2.0F, 1.0F, Style.light ? 805306368 : 587202559);
         this.box(var10 + 44.0F, 225.0F, 8.0F, 2.0F, 1.0F, Style.light ? 637534208 : 452984831);
      }

      for (Runner.Obstacle var11 : this.obstacles) {
         if (var11.bird) {
            this.bird(var11);
         } else {
            this.cactus(var11);
         }
      }

      if (this.dead && !(this.deadT < 0.08F)) {
         float var20 = Math.max(0.0F, 1.0F - this.deadT * 3.0F);
         if (var20 > 0.0F) {
            Gx.pushAlpha(var20);
            this.player();
            Gx.popAlpha();
         }
      } else {
         this.player();
      }

      for (float[] var23 : this.parts) {
         float var12 = 1.0F - var23[4] / var23[5];
         this.box(var23[0] - var23[6] / 2.0F, var23[1] - var23[6] / 2.0F, var23[6], var23[6], var23[6] * 0.25F, Gx.withAlpha((int)var23[7], var12));
      }

      Gx.pop();
      String var22 = String.format("%05d", this.score);
      boolean var24 = this.milestoneT > 0.0F && (int)(this.milestoneT * 8.0F) % 2 == 0;
      int var25 = var24 ? Style.accent : (Style.light ? -12959926 : -1644306);
      this.box(314.0F, 8.0F, 78.0F, 22.0F, 11.0F, Style.light ? 1728053247 : 1426063360);
      Gx.icon("sprint", this.X(327.0F), this.Y(19.0F), this.S(10.0F), Gx.withAlpha(Style.accent, 0.9F));
      Gx.textMid(var22, this.X(338.0F), this.Y(19.0F), this.S(10.0F), 3, var25);
      if (GameInfo.hasBest(this.info.id)) {
         String var13 = "HI " + String.format("%05d", GameInfo.best(this.info.id));
         Gx.textRight(var13, this.X(308.0F), this.Y(19.0F), this.S(7.0F), 2, Style.light ? -1728053248 : -1711276033);
      }

      float var26 = this.speed / 165.0F;
      this.box(8.0F, 8.0F, 50.0F, 22.0F, 11.0F, Style.light ? 1728053247 : 1426063360);
      Gx.icon("speed", this.X(20.0F), this.Y(19.0F), this.S(10.0F), Gx.withAlpha(Style.accent, 0.9F));
      Gx.textMid(String.format(Locale.ROOT, "x%.1f", var26), this.X(28.0F), this.Y(19.0F), this.S(8.0F), 3, Style.light ? -12959926 : -1644306);
      if (this.milestoneT > 0.0F) {
         float var14 = Math.min(1.0F, (1.2F - this.milestoneT) / 0.25F);
         float var15 = Math.min(1.0F, this.milestoneT / 0.3F);
         this.text(this.lastMilestone * 100L + "!", 200.0F, 40.0F - 6.0F * var14, 14.0F, 3, Gx.withAlpha(Style.accent, var15));
      }

      if (this.flash > 0.0F) {
         this.box(-10.0F, -10.0F, 420.0F, 260.0F, 0.0F, Gx.withAlpha(-1, this.flash * this.flash * 0.75F));
      }

      if (this.dead) {
         this.box(-10.0F, -10.0F, 420.0F, 260.0F, 0.0F, Gx.withAlpha(-57296, Math.min(0.18F, this.deadT * 0.4F)));
      }
   }

   private void cloud(float var1, float var2, float var3) {
      int var4 = Style.light ? -855638017 : 587202559;
      this.box(var1, var2, 46.0F * var3, 12.0F * var3, 6.0F * var3, var4);
      this.box(var1 + 9.0F * var3, var2 - 7.0F * var3, 22.0F * var3, 12.0F * var3, 6.0F * var3, var4);
      this.box(var1 + 22.0F * var3, var2 - 4.0F * var3, 16.0F * var3, 10.0F * var3, 5.0F * var3, var4);
   }

   private void mountains(float var1, float var2, float var3, int var4) {
      float var5 = var1 % (var2 * 2.0F);

      for (float var6 = -var5 - var2; var6 < 400.0F + var2 * 2.0F; var6 += var2) {
         int var7 = (int)Math.floor((var6 + var1) / var2 + 0.5F);
         float var8 = (40 + (var7 * 37 & 31)) * var3;
         byte var9 = 5;
         float var10 = var8 / var9;

         for (int var11 = 0; var11 < var9; var11++) {
            float var12 = var2 * 0.9F * (1.0F - (float)var11 / var9);
            float var13 = var6 + (var2 * 0.9F - var12) / 2.0F;
            float var14 = 196.0F - (var11 + 1) * var10;
            this.box(var13, var14, var12, var10 + 1.0F, 1.5F, Gx.mix(var4, Style.light ? -1 : -16777216, var11 * 0.03F));
            this.box(var13, var14, var12, 1.2F, 0.6F, Gx.withAlpha(-1, Style.light ? 0.35F : 0.06F));
            if (var11 == var9 - 1) {
               float var15 = Math.min(8.0F, var12 * 0.4F);
               this.box(var13 + var12 / 2.0F - var15 / 2.0F, var14 - 2.4F, var15, 3.0F, 1.2F, Gx.mix(var4, -1, 0.08F));
            }
         }
      }
   }

   private void cactus(Runner.Obstacle var1) {
      int var2 = -13780916;

      for (int var3 = 0; var3 < var1.cols.length; var3++) {
         float var4 = var1.x + var3 * var1.colW;

         for (int var5 = 0; var5 < var1.cols[var3]; var5++) {
            int var6 = Gx.mix(var2, (var5 + var3) % 2 == 0 ? -14774470 : -11874462, 0.35F);
            this.brick(var4 + 0.3F, 196.0F - (var5 + 1) * 10, var1.colW - 0.6F, 10.0F, var6);
         }
      }

      this.box(var1.x - 2.0F, 195.5F, var1.w + 4.0F, 3.0F, 1.5F, 855638016);
   }

   private void bird(Runner.Obstacle var1) {
      float var2 = var1.x;
      float var3 = var1.y;
      float var4 = (float)Math.sin(var1.phase);
      int var5 = -1900533;
      int var6 = -5242870;
      this.box(var2 + 4.0F, 195.5F, 14.0F, 2.5F, 1.2F, 570425344);
      float var7 = var4 * 6.0F;
      this.box(var2 + 8.0F, var3 + 3.0F - Math.max(0.0F, var7), 8.0F, Math.abs(var7) + 3.0F, 1.5F, Gx.mix(var6, -16777216, 0.2F));
      this.boxV(var2 + 3.0F, var3 + 3.0F, 17.0F, 8.0F, 3.0F, Gx.mix(var5, -1, 0.2F), var5);
      this.boxV(var2, var3 + 1.0F, 8.0F, 7.0F, 2.5F, Gx.mix(var5, -1, 0.2F), var5);
      this.box(var2 + 1.5F, var3 - 0.8F, 4.0F, 2.0F, 0.8F, Gx.mix(var5, -1, 0.3F));
      this.box(var2 - 4.0F, var3 + 4.0F, 5.0F, 2.6F, 1.0F, -13053);
      this.circle(var2 + 2.6F, var3 + 3.6F, 1.1F, -1);
      this.circle(var2 + 2.3F, var3 + 3.6F, 0.55F, -15658735);
      this.box(var2 + 19.0F, var3 + 3.0F, 4.0F, 3.0F, 1.0F, var6);
      float var8 = -var4 * 7.0F;
      if (var8 < 0.0F) {
         this.box(var2 + 7.0F, var3 + 5.0F + var8, 9.0F, -var8 + 2.0F, 1.5F, var6);
      } else {
         this.box(var2 + 7.0F, var3 + 6.0F, 9.0F, var8 + 2.0F, 1.5F, var6);
      }
   }

   private void player() {
      float var1 = 56.0F;
      float var2 = this.py;
      short var3 = -13053;
      int var4 = Style.accent;
      int var5 = -13931303;
      int var6 = Gx.mix(var4, -16777216, 0.25F);
      int var7 = Gx.mix(var5, -16777216, 0.3F);
      float var8 = this.squash * (this.onGround ? 1.0F : -0.6F);
      float var9 = Math.max(0.0F, 196.0F - var2);
      float var10 = Math.max(6.0F, 18.0F - var9 * 0.12F);
      this.box(var1 + 8.0F - var10 / 2.0F, 195.0F, var10, 3.0F, 1.5F, Gx.withAlpha(-16777216, 0.28F * Math.max(0.2F, 1.0F - var9 / 90.0F)));
      Gx.push();
      float var11 = 1.0F - var8 * 0.12F;
      Gx.translate(0.0F, 0.0F);
      if (this.duckAnim > 0.02F) {
         float var12 = this.duckAnim;
         float var13 = (float)Math.sin(this.runPhase);
         this.box(var1 - 2.0F + var13 * 2.0F, var2 - 5.0F, 7.0F, 5.0F, 1.5F, var7);
         this.box(var1 + 6.0F - var13 * 2.0F, var2 - 5.0F, 7.0F, 5.0F, 1.5F, var5);
         float var14 = var2 - 5.0F - 9.0F * var12 - (1.0F - var12) * 14.0F;
         this.boxV(var1 - 4.0F, var14, 20.0F, 9.0F, 2.5F, Gx.mix(var4, -1, 0.15F), var6);
         this.box(var1 + 10.0F, var14 + 5.0F, 8.0F, 3.0F, 1.5F, var3);
         this.boxV(var1 + 14.0F, var14 - 3.0F, 9.0F, 9.0F, 3.0F, Gx.mix(var3, -1, 0.2F), var3);
         this.box(var1 + 16.0F, var14 - 5.0F, 5.0F, 2.5F, 1.0F, Gx.mix(var3, -1, 0.25F));
         this.circle(var1 + 20.5F, var14 + 0.8F, 0.9F, -15066598);
         this.box(var1 + 19.0F, var14 + 3.2F, 3.0F, 0.9F, 0.4F, -15066598);
      } else {
         float var21 = 30.0F * var11;
         float var22 = var21 / 30.0F;
         float var23 = this.onGround ? (float)Math.sin(this.runPhase) : 0.6F;
         float var15 = this.onGround ? Math.max(0.0F, var23) * 3.0F : 2.0F;
         float var16 = this.onGround ? Math.max(0.0F, -var23) * 3.0F : 0.0F;
         float var17 = var2 - 10.0F * var22;
         this.box(var1 + 2.0F + var23 * 3.0F, var17, 6.0F, 10.0F * var22 - var15, 1.5F, var7);
         this.box(var1 + 8.0F - var23 * 3.0F, var17, 6.0F, 10.0F * var22 - var16, 1.5F, var5);
         this.box(var1 + 1.5F, var17 - 1.0F, 13.0F, 3.0F, 1.0F, var5);
         float var18 = this.onGround ? -var23 : -1.0F;
         this.box(var1 + 5.0F + var18 * 3.0F, var17 - 11.0F * var22, 4.0F, 9.0F * var22, 2.0F, Gx.mix(var6, -16777216, 0.15F));
         float var19 = var17 - 11.0F * var22;
         this.boxV(var1 + 1.0F, var19, 15.0F, 11.0F * var22, 2.5F, Gx.mix(var4, -1, 0.18F), var6);
         this.box(var1 + 3.0F, var19 + 2.0F, 3.0F, 3.0F, 1.0F, Gx.withAlpha(-1, 0.35F));
         this.box(var1 + 8.0F - var18 * 3.0F, var19 + 1.0F, 4.0F, 8.0F * var22, 2.0F, var4);
         this.circle(var1 + 10.0F - var18 * 3.0F, var19 + 1.0F + 8.0F * var22, 2.0F, var3);
         float var20 = var19 - 9.0F * var22;
         this.box(var1 + 6.0F, var20 + 7.0F * var22, 5.0F, 2.0F, 0.8F, var3);
         this.boxV(var1 + 3.5F, var20, 10.0F, 8.0F * var22, 3.0F, Gx.mix(var3, -1, 0.22F), var3);
         this.box(var1 + 5.5F, var20 - 2.2F, 6.0F, 2.6F, 1.0F, Gx.mix(var3, -1, 0.28F));
         this.circle(var1 + 11.2F, var20 + 3.0F, 0.9F, -15066598);
         this.box(var1 + 9.6F, var20 + 5.4F, 3.0F, 0.9F, 0.4F, -15066598);
      }

      Gx.pop();
   }

   private static final class Obstacle {
      float x;
      float y;
      float w;
      float h;
      boolean bird;
      int[] cols;
      float colW;
      float phase;
   }
}
