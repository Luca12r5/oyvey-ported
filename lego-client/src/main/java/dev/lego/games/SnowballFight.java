package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class SnowballFight extends GameView {
   private static final float W = 400.0F;
   private static final float H = 270.0F;
   private static final float G = 400.0F;
   private static final float BALL = 3.6F;
   private static final float ROUND = 60.0F;
   private static final float OX = 64.0F;
   private static final float OY = 192.0F;
   private static final float[] SLOT_X = new float[]{150.0F, 222.0F, 294.0F, 360.0F};
   private static final float[] SLOT_G = new float[]{238.0F, 222.0F, 204.0F, 184.0F};
   private static final float WALL_W = 38.0F;
   private static final float WALL_H = 20.0F;
   private static final float ELF_H = 30.0F;
   private final SnowballFight.Elf[] elves = new SnowballFight.Elf[4];
   private final List<SnowballFight.Ball> balls = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private final float[][] splats = new float[6][4];
   private SnowballFight.Balloon balloon;
   private float spawnT;
   private float reload;
   private float throwAnim;
   private float anim;
   private float balloonT;
   private float hintT;
   private float dragX;
   private float dragY;
   private float curX;
   private float curY;
   private boolean aiming;
   private int combo;
   private int hits;
   private int throwsN;
   private int bestCombo;

   public SnowballFight() {
      super("snowball");
   }

   @Override
   protected float boardW() {
      return 400.0F;
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
      for (int var1 = 0; var1 < this.elves.length; var1++) {
         this.elves[var1] = null;
      }

      this.balls.clear();
      this.fx.clear();

      for (float[] var4 : this.splats) {
         var4[3] = 0.0F;
      }

      this.balloon = null;
      this.spawnT = 0.8F;
      this.reload = 0.0F;
      this.throwAnim = 0.0F;
      this.balloonT = 9.0F;
      this.hintT = 0.0F;
      this.aiming = false;
      this.combo = this.hits = this.throwsN = this.bestCombo = 0;
   }

   private float left() {
      return Math.max(0.0F, 60.0F - this.time);
   }

   private int mult() {
      return Math.min(5, 1 + this.combo / 3);
   }

   private static float groundAt(float var0) {
      float var1 = 246.0F;

      for (int var2 = 0; var2 < SLOT_X.length; var2++) {
         float var3 = (var0 - SLOT_X[var2]) / 62.0F;
         if (Math.abs(var3) < 1.6F) {
            var1 = Math.min(var1, SLOT_G[var2] + var3 * var3 * 22.0F);
         }
      }

      if (var0 < 92.0F) {
         var1 = Math.min(var1, 240.0F);
      }

      return var1;
   }

   @Override
   protected void onClick(float var1, float var2, int var3) {
      if (var3 == 0) {
         this.aiming = true;
         this.dragX = this.curX = var1;
         this.dragY = this.curY = var2;
      }
   }

   @Override
   protected void onDrag(float var1, float var2) {
      if (this.aiming) {
         this.curX = var1;
         this.curY = var2;
      }
   }

   @Override
   protected void onRelease(float var1, float var2, int var3) {
      if (this.aiming) {
         this.aiming = false;
         this.curX = var1;
         this.curY = var2;
         float[] var4 = this.launchVel();
         if (var4 != null) {
            if (!(this.reload > 0.0F)) {
               SnowballFight.Ball var5 = new SnowballFight.Ball();
               var5.x = 64.0F;
               var5.y = 192.0F;
               var5.vx = var4[0];
               var5.vy = var4[1];
               this.balls.add(var5);
               this.reload = 0.32F;
               this.throwAnim = 1.0F;
               this.throwsN++;
               Sound.play("minecraft:entity.snowball.throw", 0.8F + var4[2] * 0.4F, 0.5F);
            }
         }
      }
   }

   private float[] launchVel() {
      float var1 = this.dragX - this.curX;
      float var2 = this.dragY - this.curY;
      float var3 = (float)Math.sqrt(var1 * var1 + var2 * var2);
      if (var3 < 6.0F) {
         return null;
      } else {
         float var4 = Math.min(1.0F, var3 / 90.0F);
         float var5 = 150.0F + 300.0F * var4;
         return new float[]{var1 / var3 * var5, var2 / var3 * var5, var4};
      }
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      this.hintT += var1;
      this.reload = Math.max(0.0F, this.reload - var1);
      this.throwAnim = Math.max(0.0F, this.throwAnim - var1 * 4.0F);

      for (float[] var5 : this.splats) {
         var5[3] = Math.max(0.0F, var5[3] - var1 * 0.55F);
      }

      if (this.left() <= 0.0F) {
         this.gameOver();
      } else {
         float var9 = Ease.clamp(this.time / 60.0F);
         this.spawnT -= var1;
         if (this.spawnT <= 0.0F) {
            this.spawnT = Ease.lerp(1.1F, 0.5F, var9) * (0.7F + this.rnd.nextFloat() * 0.6F);
            int var10 = this.rnd.nextInt(4);
            if (this.elves[var10] == null) {
               SnowballFight.Elf var13 = new SnowballFight.Elf();
               var13.slot = var10;
               var13.stay = Ease.lerp(2.0F, 1.05F, var9) * (0.8F + this.rnd.nextFloat() * 0.4F);
               float var16 = this.rnd.nextFloat();
               var13.gold = var16 < 0.08F;
               var13.deer = !var13.gold && var16 < 0.2F;
               this.elves[var10] = var13;
            }
         }

         for (int var11 = 0; var11 < this.elves.length; var11++) {
            SnowballFight.Elf var14 = this.elves[var11];
            if (var14 != null) {
               var14.t += var1;
               if (var14.hitT >= 0.0F) {
                  var14.hitT += var1;
                  var14.up = Math.max(0.0F, var14.up - var1 * 3.0F);
                  if (var14.hitT > 0.6F) {
                     this.elves[var11] = null;
                  }
               } else {
                  if (var14.t < 0.22F) {
                     var14.up = Ease.outBack(var14.t / 0.22F);
                  } else if (var14.t < 0.22F + var14.stay) {
                     var14.up = 1.0F;
                  } else {
                     var14.up = Math.max(0.0F, 1.0F - (var14.t - 0.22F - var14.stay) / 0.2F);
                     if (var14.up <= 0.0F) {
                        this.elves[var11] = null;
                        continue;
                     }
                  }

                  if (!var14.deer && !var14.thrown && var14.t > 0.22F + var14.stay * 0.55F && this.rnd.nextFloat() < 0.012F + var9 * 0.02F) {
                     var14.thrown = true;
                     SnowballFight.Ball var17 = new SnowballFight.Ball();
                     var17.enemy = true;
                     var17.x = SLOT_X[var11] + 6.0F;
                     var17.y = SLOT_G[var11] - 20.0F - 18.0F;
                     float var6 = 1.25F - var9 * 0.25F;
                     float var7 = 58.0F;
                     float var8 = 186.0F;
                     var17.vx = (var7 - var17.x) / var6;
                     var17.vy = (var8 - var17.y) / var6 - 200.0F * var6;
                     this.balls.add(var17);
                     Sound.play("minecraft:entity.snowball.throw", 1.3F, 0.3F);
                  }
               }
            }
         }

         this.balloonT -= var1;
         if (this.balloon == null && this.balloonT <= 0.0F) {
            this.balloon = new SnowballFight.Balloon();
            this.balloon.x = 420.0F;
            this.balloon.y = 40.0F + this.rnd.nextFloat() * 40.0F;
            this.balloonT = 12.0F + this.rnd.nextFloat() * 8.0F;
         }

         if (this.balloon != null) {
            this.balloon.t += var1;
            if (this.balloon.hit) {
               this.balloon.hitT += var1;
               this.balloon.y = this.balloon.y + 90.0F * var1 * (1.0F + this.balloon.hitT * 4.0F);
               if (this.balloon.hitT > 1.0F) {
                  this.balloon = null;
               }
            } else {
               this.balloon.x -= 38.0F * var1;
               if (this.balloon.x < -30.0F) {
                  this.balloon = null;
               }
            }
         }

         for (int var12 = this.balls.size() - 1; var12 >= 0; var12--) {
            SnowballFight.Ball var15 = this.balls.get(var12);
            var15.t += var1;
            var15.vy += 400.0F * var1;
            var15.x = var15.x + var15.vx * var1;
            var15.y = var15.y + var15.vy * var1;
            if (!var15.enemy && var15.t % 0.05F < var1) {
               this.fx.add(var15.x, var15.y, 0.0F, 0.0F, 0.25F, 1.6F, -1996488705, 0, 0.0F, 0.0F);
            }

            if (this.collide(var15)) {
               this.balls.remove(var12);
            } else if (var15.x > 420.0F || var15.x < -20.0F || var15.y > 290.0F) {
               this.balls.remove(var12);
               if (!var15.enemy) {
                  this.miss();
               }
            }
         }
      }
   }

   private boolean collide(SnowballFight.Ball var1) {
      if (var1.enemy) {
         if (var1.x < 84.0F && var1.y > 168.0F) {
            this.splat(var1.x, var1.y);

            for (float[] var18 : this.splats) {
               if (var18[3] <= 0.05F) {
                  var18[0] = 60.0F + this.rnd.nextFloat() * 280.0F;
                  var18[1] = 50.0F + this.rnd.nextFloat() * 160.0F;
                  var18[2] = 38.0F + this.rnd.nextFloat() * 26.0F;
                  var18[3] = 1.0F;
                  break;
               }
            }

            this.time += 3.0F;
            this.fx.popup("-3 s", 70.0F, 160.0F, -30070, 12.0F);
            this.fx.shake(0.6F);
            this.combo = 0;
            Sound.play("minecraft:block.snow.break", 0.6F, 0.9F);
            return true;
         }

         for (SnowballFight.Ball var3 : this.balls) {
            if (!var3.enemy && var3 != var1) {
               float var4 = var3.x - var1.x;
               float var5 = var3.y - var1.y;
               if (var4 * var4 + var5 * var5 < 81.0F) {
                  var3.t = 99.0F;
                  var3.x = -999.0F;
                  this.score = this.score + 5L * this.mult();
                  this.fx.burst(var1.x, var1.y, 18, -1, 130.0F, 3.0F, 0.5F, 200.0F, 4);
                  this.fx.ring(var1.x, var1.y, 18.0F, -6301441, 0.3F);
                  this.fx.popup("Abgefangen! +" + 5 * this.mult(), var1.x, var1.y - 8.0F, -6301441, 9.0F);
                  Sound.play("minecraft:block.powder_snow.break", 1.4F, 0.6F);
                  return true;
               }
            }
         }
      } else {
         for (int var7 = 0; var7 < this.elves.length; var7++) {
            SnowballFight.Elf var11 = this.elves[var7];
            if (var11 != null && !(var11.hitT >= 0.0F) && !(var11.up < 0.35F)) {
               float var15 = SLOT_G[var7] - 20.0F - 30.0F * var11.up;
               if (var1.x > SLOT_X[var7] - 11.0F && var1.x < SLOT_X[var7] + 11.0F && var1.y > var15 - 2.0F && var1.y < SLOT_G[var7] - 20.0F + 2.0F) {
                  this.hitElf(var11, var1);
                  return true;
               }
            }
         }

         if (this.balloon != null && !this.balloon.hit) {
            float var8 = var1.x - this.balloon.x;
            float var12 = var1.y - (this.balloon.y - 6.0F);
            if (var8 * var8 + var12 * var12 < 150.0F) {
               this.balloon.hit = true;
               this.combo++;
               int var17 = 40 * this.mult();
               this.score += var17;
               this.fx.confetti(this.balloon.x, this.balloon.y, 30, new int[]{-1890757, -736942, -14703780, -1}, 170.0F, 4.0F);
               this.fx.popup("Ballon! +" + var17, this.balloon.x, this.balloon.y - 14.0F, -736942, 12.0F);
               this.fx.shake(0.3F);
               Sound.play("minecraft:entity.firework_rocket.blast", 1.2F, 0.5F);
               return true;
            }
         }
      }

      for (int var9 = 0; var9 < SLOT_X.length; var9++) {
         float var13 = SLOT_X[var9] - 19.0F;
         float var16 = SLOT_G[var9] - 20.0F;
         if (var1.x > var13 && var1.x < var13 + 38.0F && var1.y > var16 && var1.y < SLOT_G[var9] + 6.0F) {
            this.splat(var1.x, var1.y);
            if (!var1.enemy) {
               this.miss();
            }

            return true;
         }
      }

      if (var1.x > 20.0F && var1.x < 84.0F && var1.y > 196.0F && !var1.enemy) {
         return false;
      } else if (var1.y > groundAt(var1.x)) {
         this.splat(var1.x, groundAt(var1.x));
         if (!var1.enemy) {
            this.miss();
         }

         return true;
      } else {
         return false;
      }
   }

   private void hitElf(SnowballFight.Elf var1, SnowballFight.Ball var2) {
      var1.hitT = 0.0F;
      float var3 = SLOT_X[var1.slot];
      float var4 = SLOT_G[var1.slot] - 20.0F - 18.0F;
      this.fx.burst(var2.x, var2.y, 16, -1, 140.0F, 3.2F, 0.55F, 250.0F, 4);
      if (var1.deer) {
         this.combo = 0;
         this.score = Math.max(0L, this.score - 30L);
         this.fx.popup("Nicht das Rentier! -30", var3, var4 - 16.0F, -30070, 10.0F);
         this.fx.shake(0.4F);
         Sound.play("minecraft:entity.goat.hurt", 1.2F, 0.5F);
      } else {
         this.combo++;
         this.hits++;
         this.bestCombo = Math.max(this.bestCombo, this.combo);
         int var5 = 10 + var1.slot * 5 + (var1.gold ? 40 : 0);
         int var6 = var5 * this.mult();
         this.score += var6;
         if (var1.gold) {
            this.time -= 3.0F;
            this.fx.popup("+3 s", var3, var4 - 30.0F, -6291531, 10.0F);
         }

         this.fx.popup("+" + var6 + (this.mult() > 1 ? " x" + this.mult() : ""), var3, var4 - 14.0F, var1.gold ? -736942 : -1, 11.0F);
         this.fx.burst(var3, var4 - 6.0F, 8, var1.gold ? -736942 : -8090, 90.0F, 2.2F, 0.6F, 0.0F, 2);
         this.fx.shake(0.18F);
         if (this.combo % 5 == 0) {
            this.fx.popup(this.combo + "er Serie!", 200.0F, 60.0F, -33835, 15.0F);
            Sound.play("minecraft:entity.player.levelup", 1.4F, 0.3F);
         }

         Sound.play("minecraft:entity.player.hurt_freeze", 1.3F, 0.5F);
         Sound.play("minecraft:entity.experience_orb.pickup", 1.0F + Math.min(1.0F, this.combo * 0.05F), 0.3F);
      }
   }

   private void splat(float var1, float var2) {
      this.fx.burst(var1, var2, 10, -1, 90.0F, 2.8F, 0.45F, 300.0F, 4);
      Sound.play("minecraft:block.snow.hit", 1.2F, 0.4F);
   }

   private void miss() {
      this.combo = 0;
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      GameFx.sprite(this, "sb_bg", 0.0F, 0.0F, 400.0F, 270.0F, 400.0F, 270.0F, SnowballFight::paintSky, -1);
      if (this.balloon != null) {
         float var2 = this.balloon.y + (float)Math.sin(this.balloon.t * 2.0F) * 4.0F;
         Gx.pushAlpha(this.balloon.hit ? Math.max(0.0F, 1.0F - this.balloon.hitT) : 1.0F);
         GameFx.sprite(
            this,
            "sb_balloon" + (this.balloon.hit ? 1 : 0),
            this.balloon.x - 12.0F,
            var2 - 22.0F,
            24.0F,
            40.0F,
            24.0F,
            40.0F,
            var1x -> paintBalloon(var1x, this.balloon.hit),
            -1
         );
         Gx.popAlpha();
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (int var9 = SLOT_X.length - 1; var9 >= 0; var9--) {
         SnowballFight.Elf var3 = this.elves[var9];
         float var4 = SLOT_G[var9] - 20.0F;
         if (var3 != null) {
            this.elf(var3, var9, var4);
         }

         GameFx.sprite(this, "sb_hill", SLOT_X[var9] - 100.0F, SLOT_G[var9] - 3.0F, 200.0F, 70.0F, 200.0F, 70.0F, SnowballFight::paintHill, -1);
         GameFx.sprite(this, "sb_wall", SLOT_X[var9] - 19.0F - 2.0F, var4 - 4.0F, 42.0F, 28.0F, 42.0F, 28.0F, SnowballFight::paintWall, -1);
      }

      this.box(0.0F, 240.0F, 400.0F, 40.0F, 0.0F, -1511172);
      this.kid();
      GameFx.sprite(this, "sb_fort", 16.0F, 196.0F, 74.0F, 50.0F, 74.0F, 50.0F, SnowballFight::paintFort, -1);

      for (SnowballFight.Ball var14 : this.balls) {
         if (!(var14.x < -100.0F)) {
            this.circle(var14.x + 0.8F, var14.y + 1.0F, 3.6F, 857747536);
            this.circle(var14.x, var14.y, 3.6F, var14.enemy ? -2234120 : -1);
            this.circle(var14.x - 1.0F, var14.y - 1.0F, 1.6199999F, -1);
         }
      }

      if (this.aiming && this.state == GameView.State.PLAYING) {
         float[] var11 = this.launchVel();
         if (var11 != null) {
            float var15 = 64.0F;
            float var18 = 192.0F;
            float var5 = var11[0];
            float var6 = var11[1];

            for (int var7 = 0; var7 < 22; var7++) {
               for (int var8 = 0; var8 < 4; var8++) {
                  var6 += 4.8F;
                  var15 += var5 * 0.012F;
                  var18 += var6 * 0.012F;
               }

               if (var18 > groundAt(var15) || var15 > 400.0F) {
                  break;
               }

               float var25 = (1.0F - var7 / 22.0F) * (this.reload > 0.0F ? 0.35F : 0.9F);
               this.circle(var15, var18, 2.1F - var7 * 0.04F, Gx.withAlpha(-15057007, var25 * 0.5F));
               this.circle(var15, var18, 1.4F - var7 * 0.03F, Gx.withAlpha(-1, var25));
            }

            int var23 = Gx.mix(-11872659, -45730, var11[2]);
            this.box(20.0F, 150.0F, 6.0F, 34.0F, 3.0F, 1711276032);
            this.box(20.0F, 150.0F + 34.0F * (1.0F - var11[2]), 6.0F, 34.0F * var11[2], 3.0F, var23);
         }

         this.circle(this.dragX, this.dragY, 3.0F, 1728053247);
         Gx.ringAt(this.X(this.dragX), this.Y(this.dragY), this.S(7.0F), Math.max(1, this.S(1.0F)), 1728053247);
         this.circle(this.curX, this.curY, 2.5F, -1426063361);
      }

      this.fx.draw(this, 0.0F, 0.0F);
      Gx.pop();

      for (float[] var21 : this.splats) {
         if (!(var21[3] <= 0.0F)) {
            float var22 = Math.min(1.0F, var21[3] * 1.6F);
            float var24 = var21[2] * (1.0F + 0.15F * (1.0F - Ease.clamp((1.0F - var21[3]) * 8.0F)));
            GameFx.sprite(
               this,
               "sb_splat",
               var21[0] - var24 / 2.0F,
               var21[1] - var24 / 2.0F + (1.0F - var21[3]) * 20.0F,
               var24,
               var24,
               64.0F,
               64.0F,
               SnowballFight::paintSplat,
               Gx.withAlpha(-1, var22)
            );
         }
      }

      float var13 = this.left() / 60.0F;
      this.box(8.0F, 8.0F, 384.0F, 7.0F, 3.5F, 1426063360);
      int var17 = var13 < 0.2F ? Gx.mix(-45730, -1, 0.5F + 0.5F * (float)Math.sin(this.anim * 12.0F)) : -6301441;
      this.box(8.0F, 8.0F, 384.0F * var13, 7.0F, 3.5F, var17);
      this.box(182.0F, 18.0F, 36.0F, 15.0F, 7.5F, 1711276032);
      this.text(String.format(Locale.ROOT, "%.0f s", Math.ceil(this.left())), 200.0F, 25.5F, 7.5F, 3, -1);
      this.box(8.0F, 18.0F, 104.0F, 15.0F, 7.5F, 1711276032);
      this.text("Treffer " + this.hits + (this.throwsN > 0 ? "  •  " + Math.round(100.0F * this.hits / this.throwsN) + " %" : ""), 60.0F, 25.5F, 6.8F, 2, -1);
      if (this.combo >= 2) {
         this.box(322.0F, 18.0F, 70.0F, 15.0F, 7.5F, Gx.withAlpha(-1890757, 0.9F));
         this.text("Serie " + this.combo + "  x" + this.mult(), 357.0F, 25.5F, 6.8F, 3, -1);
      }

      if (this.reload > 0.0F) {
         this.box(54.0F, 212.0F, 20.0F * (1.0F - this.reload / 0.32F), 2.0F, 1.0F, -1426063361);
      }

      if (this.state == GameView.State.PLAYING && this.hintT < 4.0F && !this.aiming) {
         float var20 = Math.min(1.0F, 4.0F - this.hintT) * (0.6F + 0.4F * (float)Math.sin(this.anim * 5.0F));
         this.box(80.0F, 110.0F, 240.0F, 22.0F, 11.0F, Gx.withAlpha(-16777216, 0.45F * var20));
         this.text("Maus gedrückt halten, zurückziehen & loslassen!", 200.0F, 121.0F, 7.4F, 3, Gx.withAlpha(-1, var20));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 400.0F, 270.0F);
   }

   private void elf(SnowballFight.Elf var1, int var2, float var3) {
      float var4 = SLOT_X[var2];
      float var5 = var3 - 30.0F * var1.up + (var1.hitT >= 0.0F ? var1.hitT * 10.0F : 0.0F);
      String var6 = var1.deer ? "sb_deer" : (var1.gold ? "sb_elf_g" : "sb_elf");
      boolean var7 = var1.hitT >= 0.0F;
      if (var1.gold) {
         Gx.glow(this.X(var4), this.Y(var5 + 10.0F), this.S(26.0F), 1156890962);
      }

      float var8 = var7 ? 0.0F : (float)Math.sin(var1.t * 9.0F) * 0.6F;
      GameFx.sprite(
         this, var6 + (var7 ? "h" : ""), var4 - 14.0F + var8, var5 - 4.0F, 28.0F, 38.0F, 28.0F, 38.0F, var2x -> paintElf(var2x, var1.deer, var1.gold, var7), -1
      );
      if (var7 && !var1.deer) {
         for (int var9 = 0; var9 < 3; var9++) {
            double var10 = this.anim * 6.0F + var9 * 2.1;
            Gx.icon("star-fill", this.X(var4 + (float)Math.cos(var10) * 10.0F), this.Y(var5 + 2.0F + (float)Math.sin(var10) * 3.0F), 5.0F * this.u, -8090);
         }
      }
   }

   private void kid() {
      float var1 = Ease.outCubic(this.throwAnim) * 3.0F;
      float var2 = this.aiming ? -1.5F : 0.0F;
      GameFx.sprite(
         this,
         "sb_kid" + (this.throwAnim > 0.3F ? 1 : 0),
         38.0F + var2,
         164.0F - var1,
         34.0F,
         44.0F,
         34.0F,
         44.0F,
         var1x -> paintKid(var1x, this.throwAnim > 0.3F),
         -1
      );
   }

   private static void paintSky(Graphics2D var0) {
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(7317736), 0.0F, 200.0F, new Color(14281983)));
      var0.fillRect(0, 0, 400, 270);
      var0.setPaint(
         new RadialGradientPaint(
            new Float(330.0F, 50.0F), 70.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(255, 250, 225, 200), new Color(255, 250, 225, 0)}
         )
      );
      var0.fill(new java.awt.geom.Ellipse2D.Float(260.0F, -20.0F, 140.0F, 140.0F));
      var0.setColor(new Color(16775388));
      var0.fill(new java.awt.geom.Ellipse2D.Float(318.0F, 38.0F, 24.0F, 24.0F));
      var0.setColor(new Color(11126764));
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(0.0F, 190.0F);
      var1.lineTo(40.0F, 140.0F);
      var1.lineTo(80.0F, 165.0F);
      var1.lineTo(140.0F, 110.0F);
      var1.lineTo(200.0F, 160.0F);
      var1.lineTo(260.0F, 120.0F);
      var1.lineTo(320.0F, 150.0F);
      var1.lineTo(370.0F, 115.0F);
      var1.lineTo(400.0F, 135.0F);
      var1.lineTo(400.0F, 270.0F);
      var1.lineTo(0.0F, 270.0F);
      var1.closePath();
      var0.fill(var1);
      var0.setColor(new Color(16054527));
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(128.0F, 121.0F);
      var2.lineTo(140.0F, 110.0F);
      var2.lineTo(154.0F, 124.0F);
      var2.lineTo(146.0F, 121.0F);
      var2.lineTo(140.0F, 126.0F);
      var2.lineTo(134.0F, 120.0F);
      var2.closePath();
      var2.moveTo(250.0F, 128.0F);
      var2.lineTo(260.0F, 120.0F);
      var2.lineTo(272.0F, 130.0F);
      var2.lineTo(262.0F, 128.0F);
      var2.closePath();
      var2.moveTo(360.0F, 123.0F);
      var2.lineTo(370.0F, 115.0F);
      var2.lineTo(382.0F, 124.0F);
      var2.lineTo(371.0F, 122.0F);
      var2.closePath();
      var0.fill(var2);
      var0.setColor(new Color(255, 255, 255, 200));
      var0.fill(
         GameFx.union(
            new java.awt.geom.Ellipse2D.Float(40.0F, 40.0F, 40.0F, 18.0F),
            new java.awt.geom.Ellipse2D.Float(58.0F, 30.0F, 36.0F, 24.0F),
            new java.awt.geom.Ellipse2D.Float(80.0F, 40.0F, 36.0F, 18.0F)
         )
      );
      var0.fill(
         GameFx.union(
            new java.awt.geom.Ellipse2D.Float(190.0F, 60.0F, 34.0F, 14.0F),
            new java.awt.geom.Ellipse2D.Float(204.0F, 52.0F, 30.0F, 20.0F),
            new java.awt.geom.Ellipse2D.Float(222.0F, 60.0F, 30.0F, 14.0F)
         )
      );
      Random var3 = new Random(3L);

      for (int var4 = 0; var4 < 30; var4++) {
         float var5 = var4 * 14 + var3.nextFloat() * 6.0F;
         float var6 = 186.0F + var3.nextFloat() * 10.0F;
         float var7 = 0.5F + var3.nextFloat() * 0.4F;
         var0.setColor(new Color(6194867));
         var0.fillPolygon(
            new int[]{(int)var5, (int)(var5 - 9.0F * var7), (int)(var5 + 9.0F * var7)}, new int[]{(int)(var6 - 34.0F * var7), (int)var6, (int)var6}, 3
         );
      }

      var0.setColor(new Color(13623541));
      var0.fillRect(0, 192, 400, 80);
   }

   private static void paintHill(Graphics2D var0) {
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(0.0F, 70.0F);

      for (byte var2 = 0; var2 <= 200; var2 += 5) {
         float var3 = (var2 - 100) / 62.0F;
         var1.lineTo(var2, 3.0F + var3 * var3 * 22.0F);
      }

      var1.lineTo(200.0F, 70.0F);
      var1.closePath();
      var0.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 60.0F, new Color(12177390)));
      var0.fill(var1);
      var0.setColor(new Color(150, 180, 225, 120));
      var0.setStroke(new BasicStroke(1.2F));
      var0.draw(var1);
   }

   private static void paintWall(Graphics2D var0) {
      var0.setColor(new Color(60, 90, 140, 50));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(2.0F, 8.0F, 40.0F, 20.0F, 6.0F, 6.0F));

      for (int var1 = 0; var1 < 2; var1++) {
         for (int var2 = 0; var2 < 3 - var1; var2++) {
            float var3 = 2 + var2 * 13 + var1 * 6.5F;
            float var4 = 16 - var1 * 9;
            java.awt.geom.RoundRectangle2D.Float var5 = new java.awt.geom.RoundRectangle2D.Float(var3, var4, 12.5F, 10.0F, 5.0F, 5.0F);
            var0.setPaint(new GradientPaint(0.0F, var4, new Color(16777215), 0.0F, var4 + 10.0F, new Color(13031666)));
            var0.fill(var5);
            var0.setColor(new Color(150, 180, 225));
            var0.setStroke(new BasicStroke(0.8F));
            var0.draw(var5);
         }
      }

      var0.setColor(new Color(16054527));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(0.0F, 24.0F, 42.0F, 4.0F, 4.0F, 4.0F));
   }

   private static void paintFort(Graphics2D var0) {
      GeneralPath var1 = new GeneralPath();
      var1.moveTo(0.0F, 50.0F);
      var1.lineTo(0.0F, 20.0F);
      var1.curveTo(4.0F, 10.0F, 14.0F, 10.0F, 18.0F, 16.0F);
      var1.curveTo(24.0F, 6.0F, 36.0F, 6.0F, 42.0F, 16.0F);
      var1.curveTo(48.0F, 8.0F, 60.0F, 10.0F, 66.0F, 20.0F);
      var1.lineTo(74.0F, 50.0F);
      var1.closePath();
      var0.setPaint(new GradientPaint(0.0F, 8.0F, new Color(16777215), 0.0F, 50.0F, new Color(12111339)));
      var0.fill(var1);
      var0.setColor(new Color(150, 180, 225));
      var0.setStroke(new BasicStroke(1.0F));
      var0.draw(var1);

      for (int var2 = 0; var2 < 4; var2++) {
         float var3 = 8 + var2 * 8 - (var2 == 3 ? 12 : 0);
         float var4 = var2 == 3 ? 36.0F : 42.0F;
         var0.setPaint(
            new RadialGradientPaint(new Float(var3 + 2.0F, var4 + 2.0F), 5.0F, new float[]{0.0F, 1.0F}, new Color[]{Color.WHITE, new Color(13228786)})
         );
         var0.fill(new java.awt.geom.Ellipse2D.Float(var3, var4, 8.0F, 8.0F));
      }
   }

   private static void paintKid(Graphics2D var0, boolean var1) {
      Color var2 = new Color(3108832);
      Color var3 = new Color(1720209);
      var0.setPaint(new GradientPaint(0.0F, 20.0F, var2, 0.0F, 44.0F, var3));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 18.0F, 20.0F, 8.0F, 8.0F));
      var0.setColor(new Color(14886459));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 20.0F, 18.0F, 5.0F, 4.0F, 4.0F));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 5.0F, 10.0F, 3.0F, 3.0F));
      var0.setColor(new Color(16767420));
      var0.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 6.0F, 16.0F, 16.0F));
      var0.setColor(new Color(1776418));
      var0.fill(new java.awt.geom.Ellipse2D.Float(18.0F, 12.0F, 2.4F, 2.8F));
      var0.setColor(new Color(255, 120, 120, 120));
      var0.fill(new java.awt.geom.Ellipse2D.Float(19.0F, 16.0F, 4.0F, 2.4F));
      var0.setColor(new Color(6961690));
      var0.setStroke(new BasicStroke(1.2F, 1, 1));
      var0.drawArc(17, 15, 5, 3, 200, 140);
      var0.setColor(new Color(14886459));
      var0.fill(new java.awt.geom.Arc2D.Float(8.0F, 3.0F, 18.0F, 16.0F, 0.0F, 180.0F, 1));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 9.0F, 18.0F, 4.0F, 4.0F, 4.0F));
      var0.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 0.0F, 6.0F, 6.0F));
      var0.setColor(var2);
      var0.setStroke(new BasicStroke(5.0F, 1, 1));
      if (var1) {
         var0.drawLine(22, 26, 31, 16);
      } else {
         var0.drawLine(12, 27, 5, 21);
      }

      var0.setColor(Color.WHITE);
      if (!var1) {
         var0.fill(new java.awt.geom.Ellipse2D.Float(0.5F, 15.0F, 7.0F, 7.0F));
         var0.setColor(new Color(13228786));
         var0.fill(new java.awt.geom.Ellipse2D.Float(3.0F, 19.0F, 3.0F, 2.0F));
      }
   }

   private static void paintElf(Graphics2D var0, boolean var1, boolean var2, boolean var3) {
      if (var1) {
         var0.setColor(new Color(7226914));
         var0.setStroke(new BasicStroke(2.0F, 1, 1));
         var0.drawLine(9, 10, 5, 2);
         var0.drawLine(6, 5, 2, 5);
         var0.drawLine(19, 10, 23, 2);
         var0.drawLine(22, 5, 26, 5);
         var0.setPaint(new GradientPaint(0.0F, 8.0F, new Color(10514492), 0.0F, 30.0F, new Color(7226914)));
         var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 8.0F, 16.0F, 18.0F));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(8.0F, 22.0F, 12.0F, 16.0F, 6.0F, 6.0F));
         var0.setColor(new Color(14267530));
         var0.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 16.0F, 10.0F, 9.0F));
         var0.setColor(new Color(14886459));
         var0.fill(new java.awt.geom.Ellipse2D.Float(11.0F, 18.0F, 6.0F, 5.0F));
         var0.setColor(Color.WHITE);
         var0.fill(new java.awt.geom.Ellipse2D.Float(15.0F, 19.0F, 1.5F, 1.5F));
         var0.setColor(new Color(1776418));
         var0.fill(new java.awt.geom.Ellipse2D.Float(9.0F, 13.0F, 3.0F, 3.0F));
         var0.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 13.0F, 3.0F, 3.0F));
      } else {
         Color var4 = var2 ? new Color(16040274) : new Color(2073436);
         Color var5 = var2 ? new Color(12092970) : new Color(941620);
         var0.setPaint(new GradientPaint(0.0F, 24.0F, var4, 0.0F, 38.0F, var5));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(7.0F, 24.0F, 14.0F, 14.0F, 6.0F, 6.0F));
         var0.setColor(new Color(14886459));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(6.0F, 23.0F, 16.0F, 4.0F, 3.0F, 3.0F));
         var0.setColor(new Color(16239013));
         var0.fillPolygon(new int[]{7, 1, 8}, new int[]{15, 11, 19}, 3);
         var0.fillPolygon(new int[]{21, 27, 20}, new int[]{15, 11, 19}, 3);
         var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 10.0F, 16.0F, 15.0F));
         var0.setColor(new Color(1776418));
         if (var3) {
            var0.setStroke(new BasicStroke(1.2F, 1, 1));
            var0.drawLine(9, 14, 12, 17);
            var0.drawLine(12, 14, 9, 17);
            var0.drawLine(16, 14, 19, 17);
            var0.drawLine(19, 14, 16, 17);
            var0.setColor(Color.WHITE);
            var0.fill(GameFx.union(new java.awt.geom.Ellipse2D.Float(5.0F, 11.0F, 12.0F, 10.0F), new java.awt.geom.Ellipse2D.Float(11.0F, 14.0F, 10.0F, 9.0F)));
         } else {
            var0.fill(new java.awt.geom.Ellipse2D.Float(9.5F, 14.0F, 2.6F, 3.0F));
            var0.fill(new java.awt.geom.Ellipse2D.Float(16.0F, 14.0F, 2.6F, 3.0F));
            var0.setColor(new Color(11879727));
            var0.setStroke(new BasicStroke(1.1F, 1, 1));
            var0.drawArc(11, 17, 6, 4, 200, 140);
            var0.setColor(new Color(255, 120, 120, 120));
            var0.fill(new java.awt.geom.Ellipse2D.Float(7.5F, 18.0F, 3.5F, 2.0F));
            var0.fill(new java.awt.geom.Ellipse2D.Float(17.5F, 18.0F, 3.5F, 2.0F));
         }

         GeneralPath var6 = new GeneralPath();
         var6.moveTo(5.0F, 12.0F);
         var6.curveTo(8.0F, 2.0F, 14.0F, -1.0F, 25.0F, 3.0F);
         var6.lineTo(22.0F, 6.0F);
         var6.curveTo(22.0F, 8.0F, 23.0F, 10.0F, 23.0F, 12.0F);
         var6.closePath();
         var0.setPaint(new GradientPaint(5.0F, 0.0F, var4, 23.0F, 12.0F, var5));
         var0.fill(var6);
         var0.setColor(var2 ? Color.WHITE : new Color(16040274));
         var0.fill(new java.awt.geom.Ellipse2D.Float(23.0F, 1.0F, 5.0F, 5.0F));
         var0.setColor(new Color(14886459));
         var0.fill(new java.awt.geom.RoundRectangle2D.Float(4.5F, 10.0F, 19.0F, 3.5F, 3.0F, 3.0F));
      }
   }

   private static void paintBalloon(Graphics2D var0, boolean var1) {
      if (!var1) {
         var0.setPaint(new RadialGradientPaint(new Float(9.0F, 7.0F), 14.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(16743048), new Color(12917294)}));
         var0.fill(new java.awt.geom.Ellipse2D.Float(2.0F, 0.0F, 20.0F, 24.0F));
         var0.setColor(new Color(255, 255, 255, 150));
         var0.fill(new java.awt.geom.Ellipse2D.Float(6.0F, 4.0F, 5.0F, 7.0F));
      }

      var0.setColor(new Color(4477030));
      var0.setStroke(new BasicStroke(0.7F));
      var0.drawLine(12, 24, 12, 30);
      var0.setColor(new Color(2073436));
      var0.fill(new java.awt.geom.RoundRectangle2D.Float(6.0F, 29.0F, 12.0F, 10.0F, 3.0F, 3.0F));
      var0.setColor(new Color(16040274));
      var0.fillRect(11, 29, 2, 10);
      var0.fillRect(6, 32, 12, 2);
   }

   private static void paintSplat(Graphics2D var0) {
      Random var1 = new Random(5L);
      var0.setColor(new Color(255, 255, 255, 235));
      var0.fill(new java.awt.geom.Ellipse2D.Float(14.0F, 14.0F, 36.0F, 34.0F));

      for (int var2 = 0; var2 < 9; var2++) {
         double var3 = var2 * Math.PI * 2.0 / 9.0 + var1.nextDouble() * 0.4;
         float var5 = 18.0F + var1.nextFloat() * 10.0F;
         float var6 = 7.0F + var1.nextFloat() * 7.0F;
         var0.fill(
            new java.awt.geom.Ellipse2D.Float(
               32.0F + (float)Math.cos(var3) * var5 - var6 / 2.0F, 32.0F + (float)Math.sin(var3) * var5 - var6 / 2.0F, var6, var6
            )
         );
      }

      var0.setColor(new Color(200, 220, 245, 200));
      var0.fill(new java.awt.geom.Ellipse2D.Float(24.0F, 34.0F, 18.0F, 8.0F));
      var0.setColor(Color.WHITE);
      var0.fill(new java.awt.geom.Ellipse2D.Float(22.0F, 20.0F, 10.0F, 8.0F));
   }

   private static final class Ball {
      float x;
      float y;
      float vx;
      float vy;
      float t;
      boolean enemy;
   }

   private static final class Balloon {
      float x;
      float y;
      float t;
      boolean hit;
      float hitT;
   }

   private static final class Elf {
      int slot;
      float t;
      float up;
      float stay;
      float hitT = -1.0F;
      boolean gold;
      boolean deer;
      boolean thrown;
   }
}
