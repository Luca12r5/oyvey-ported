package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Asteroids extends GameView {
   private static final float W = 400.0F;
   private static final float H = 290.0F;
   private static final float[] RAD = new float[]{0.0F, 7.5F, 14.0F, 25.0F};
   private static final int[] PTS = new int[]{0, 100, 50, 20};
   private static final int ANGLES = 48;
   private static final int ROCK_ANGLES = 24;
   private static final int VARIANTS = 6;
   private static final int NEON = -10685697;
   private static final int ROCK = -3561729;
   private static final int UFO_C = -41816;
   private static final int SHOT = -3462;
   private final List<Asteroids.Rock> rocks = new ArrayList<>();
   private final List<Asteroids.Shot> shots = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private Asteroids.Ufo ufo;
   private float x;
   private float y;
   private float vx;
   private float vy;
   private float ang;
   private float fireT;
   private float invul;
   private float deadT;
   private float waveT;
   private float ufoT;
   private float anim;
   private float thrustT;
   private float bannerT = -1.0F;
   private boolean left;
   private boolean right;
   private boolean thrust;
   private boolean firing;
   private boolean dead;
   private int lives;
   private int wave;
   private long nextLife;

   public Asteroids() {
      super("asteroids");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.rocks.clear();
      this.shots.clear();
      this.fx.clear();
      this.ufo = null;
      this.lives = 3;
      this.wave = 0;
      this.nextLife = 10000L;
      this.left = this.right = this.thrust = this.firing = false;
      this.spawnShip();
      this.nextWave();
   }

   private void spawnShip() {
      this.x = 200.0F;
      this.y = 145.0F;
      this.vx = this.vy = 0.0F;
      this.ang = (float) (-Math.PI / 2);
      this.invul = 2.2F;
      this.dead = false;
      this.deadT = 0.0F;
   }

   private void nextWave() {
      this.wave++;
      int var1 = Math.min(10, 3 + this.wave);

      for (int var2 = 0; var2 < var1; var2++) {
         float var3;
         float var4;
         do {
            var3 = this.rnd.nextFloat() * 400.0F;
            var4 = this.rnd.nextFloat() * 290.0F;
         } while (dist(var3, var4, this.x, this.y) < 90.0F);

         this.addRock(var3, var4, 3, null);
      }

      this.ufoT = Math.max(8, 22 - this.wave * 2) + this.rnd.nextFloat() * 6.0F;
      this.bannerT = 0.0F;
   }

   private void addRock(float var1, float var2, int var3, Asteroids.Rock var4) {
      Asteroids.Rock var5 = new Asteroids.Rock();
      var5.x = var1;
      var5.y = var2;
      var5.size = var3;
      var5.variant = this.rnd.nextInt(6);
      double var6 = this.rnd.nextDouble() * Math.PI * 2.0;
      float var8 = (var3 == 3 ? 22 : (var3 == 2 ? 42 : 66)) * (1.0F + this.wave * 0.06F) * (0.7F + this.rnd.nextFloat() * 0.6F);
      var5.vx = (float)Math.cos(var6) * var8 + (var4 != null ? var4.vx * 0.4F : 0.0F);
      var5.vy = (float)Math.sin(var6) * var8 + (var4 != null ? var4.vy * 0.4F : 0.0F);
      var5.ang = this.rnd.nextFloat() * 6.28F;
      var5.spin = (this.rnd.nextFloat() - 0.5F) * 2.4F;
      this.rocks.add(var5);
   }

   private static float dist(float var0, float var1, float var2, float var3) {
      float var4 = Math.abs(var0 - var2);
      float var5 = Math.abs(var1 - var3);
      var4 = Math.min(var4, 400.0F - var4);
      var5 = Math.min(var5, 290.0F - var5);
      return (float)Math.sqrt(var4 * var4 + var5 * var5);
   }

   @Override
   protected void onKey(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = true;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = true;
      }

      if (var1 == 265 || var1 == 87) {
         this.thrust = true;
      }

      if (var1 == 32) {
         if (!this.firing) {
            this.fireT = 0.0F;
         }

         this.firing = true;
      }

      if ((var1 == 264 || var1 == 83 || var1 == 340) && !this.dead) {
         this.hyperspace();
      }
   }

   @Override
   protected void onKeyUp(int var1) {
      if (var1 == 263 || var1 == 65) {
         this.left = false;
      }

      if (var1 == 262 || var1 == 68) {
         this.right = false;
      }

      if (var1 == 265 || var1 == 87) {
         this.thrust = false;
      }

      if (var1 == 32) {
         this.firing = false;
      }
   }

   private void hyperspace() {
      this.fx.burst(this.x, this.y, 20, -10685697, 120.0F, 2.0F, 0.5F, 0.0F, 2);
      this.x = 20.0F + this.rnd.nextFloat() * 360.0F;
      this.y = 20.0F + this.rnd.nextFloat() * 250.0F;
      this.vx = this.vy = 0.0F;
      this.invul = Math.max(this.invul, 0.4F);
      this.fx.ring(this.x, this.y, 30.0F, -10685697, 0.4F);
      Sound.play("minecraft:entity.enderman.teleport", 1.4F, 0.35F);
   }

   @Override
   protected void update(float var1) {
      this.fx.update(var1);
      if (this.bannerT >= 0.0F) {
         this.bannerT += var1;
         if (this.bannerT > 1.8F) {
            this.bannerT = -1.0F;
         }
      }

      this.invul = Math.max(0.0F, this.invul - var1);
      this.fireT = Math.max(0.0F, this.fireT - var1);
      if (this.dead) {
         this.deadT += var1;
         if (this.deadT > 1.6F) {
            if (this.lives <= 0) {
               this.gameOver();
               return;
            }

            boolean var2 = true;

            for (Asteroids.Rock var4 : this.rocks) {
               if (dist(var4.x, var4.y, 200.0F, 145.0F) < RAD[var4.size] + 50.0F) {
                  var2 = false;
               }
            }

            if (var2 || this.deadT > 4.0F) {
               this.spawnShip();
            }
         }
      } else {
         if (this.left) {
            this.ang -= 4.4F * var1;
         }

         if (this.right) {
            this.ang += 4.4F * var1;
         }

         float var7 = (float)Math.cos(this.ang);
         float var13 = (float)Math.sin(this.ang);
         if (this.thrust) {
            this.vx += var7 * 250.0F * var1;
            this.vy += var13 * 250.0F * var1;
            this.thrustT += var1;
            if (this.thrustT > 0.02F) {
               this.thrustT = 0.0F;
               this.fx
                  .add(
                     this.x - var7 * 9.0F,
                     this.y - var13 * 9.0F,
                     -var7 * 90.0F + this.vx * 0.5F + (this.rnd.nextFloat() - 0.5F) * 40.0F,
                     -var13 * 90.0F + this.vy * 0.5F + (this.rnd.nextFloat() - 0.5F) * 40.0F,
                     0.35F,
                     1.8F + this.rnd.nextFloat(),
                     this.rnd.nextBoolean() ? -19641 : -38339,
                     2,
                     0.0F,
                     1.5F
                  );
            }
         }

         float var17 = (float)Math.sqrt(this.vx * this.vx + this.vy * this.vy);
         if (var17 > 240.0F) {
            this.vx *= 240.0F / var17;
            this.vy *= 240.0F / var17;
         }

         float var5 = (float)Math.exp(-0.45F * var1);
         this.vx *= var5;
         this.vy *= var5;
         this.x = wrapX(this.x + this.vx * var1);
         this.y = wrapY(this.y + this.vy * var1);
         if (this.firing && this.fireT <= 0.0F) {
            this.fire();
         }
      }

      for (Asteroids.Rock var14 : this.rocks) {
         var14.x = wrapX(var14.x + var14.vx * var1);
         var14.y = wrapY(var14.y + var14.vy * var1);
         var14.ang = var14.ang + var14.spin * var1;
         var14.flash = Math.max(0.0F, var14.flash - var1 * 6.0F);
      }

      for (int var9 = this.shots.size() - 1; var9 >= 0; var9--) {
         Asteroids.Shot var15 = this.shots.get(var9);
         var15.x = wrapX(var15.x + var15.vx * var1);
         var15.y = wrapY(var15.y + var15.vy * var1);
         var15.life -= var1;
         if (var15.life <= 0.0F) {
            this.shots.remove(var9);
         } else {
            boolean var18 = false;
            if (!var15.enemy) {
               for (int var20 = this.rocks.size() - 1; var20 >= 0; var20--) {
                  Asteroids.Rock var6 = this.rocks.get(var20);
                  if (dist(var15.x, var15.y, var6.x, var6.y) < RAD[var6.size] * 0.92F) {
                     this.breakRock(var20, var15.vx, var15.vy);
                     var18 = true;
                     break;
                  }
               }

               if (!var18 && this.ufo != null && dist(var15.x, var15.y, this.ufo.x, this.ufo.y) < (this.ufo.small ? 8 : 13)) {
                  this.killUfo();
                  var18 = true;
               }
            } else if (!this.dead && this.invul <= 0.0F && dist(var15.x, var15.y, this.x, this.y) < 7.0F) {
               this.crash();
               var18 = true;
            }

            if (var18) {
               this.shots.remove(var9);
            }
         }
      }

      if (!this.dead && this.invul <= 0.0F) {
         for (int var10 = this.rocks.size() - 1; var10 >= 0; var10--) {
            Asteroids.Rock var16 = this.rocks.get(var10);
            if (dist(this.x, this.y, var16.x, var16.y) < RAD[var16.size] * 0.85F + 6.0F) {
               this.breakRock(var10, this.vx, this.vy);
               this.crash();
               break;
            }
         }

         if (!this.dead && this.ufo != null && dist(this.x, this.y, this.ufo.x, this.ufo.y) < 16.0F) {
            this.killUfo();
            this.crash();
         }
      }

      this.ufoT -= var1;
      if (this.ufo == null && this.ufoT <= 0.0F && this.wave >= 1) {
         this.ufo = new Asteroids.Ufo();
         this.ufo.small = this.wave >= 3 && this.rnd.nextFloat() < 0.45F;
         boolean var11 = this.rnd.nextBoolean();
         this.ufo.x = var11 ? -20.0F : 420.0F;
         this.ufo.vx = (var11 ? 1 : -1) * (this.ufo.small ? 75 : 55);
         this.ufo.y = 30.0F + this.rnd.nextFloat() * 230.0F;
         this.ufo.fireT = 1.2F;
         this.ufoT = Math.max(9, 24 - this.wave * 2) + this.rnd.nextFloat() * 8.0F;
         Sound.play("minecraft:block.beacon.activate", 1.8F, 0.3F);
      }

      if (this.ufo != null) {
         this.ufo.t += var1;
         this.ufo.x = this.ufo.x + this.ufo.vx * var1;
         this.ufo.y = wrapY(this.ufo.y + (float)Math.sin(this.ufo.t * 2.2F) * 40.0F * var1);
         this.ufo.fireT -= var1;
         if (this.ufo.fireT <= 0.0F && !this.dead) {
            this.ufo.fireT = this.ufo.small ? 0.9F : 1.3F;
            double var12 = this.ufo.small
               ? Math.atan2(this.y - this.ufo.y, this.x - this.ufo.x) + (this.rnd.nextFloat() - 0.5F) * 0.35F
               : this.rnd.nextDouble() * Math.PI * 2.0;
            Asteroids.Shot var19 = new Asteroids.Shot();
            var19.enemy = true;
            var19.x = this.ufo.x;
            var19.y = this.ufo.y;
            var19.vx = (float)Math.cos(var12) * 170.0F;
            var19.vy = (float)Math.sin(var12) * 170.0F;
            var19.life = 1.6F;
            this.shots.add(var19);
            Sound.play("minecraft:block.note_block.bit", 0.7F, 0.25F);
         }

         if (this.ufo.x < -40.0F || this.ufo.x > 440.0F) {
            this.ufo = null;
         }
      }

      if (this.rocks.isEmpty() && !this.dead) {
         this.waveT += var1;
         if (this.waveT > 1.2F) {
            this.waveT = 0.0F;
            this.nextWave();
         }
      }

      if (this.score >= this.nextLife) {
         this.nextLife += 10000L;
         this.lives++;
         this.fx.popup("+1 Leben!", this.x, this.y - 16.0F, -6291531, 12.0F);
         Sound.play("minecraft:entity.player.levelup", 1.5F, 0.4F);
      }
   }

   private void fire() {
      int var1 = 0;

      for (Asteroids.Shot var3 : this.shots) {
         if (!var3.enemy) {
            var1++;
         }
      }

      if (var1 < 6) {
         this.fireT = 0.17F;
         float var5 = (float)Math.cos(this.ang);
         float var6 = (float)Math.sin(this.ang);
         Asteroids.Shot var4 = new Asteroids.Shot();
         var4.x = this.x + var5 * 10.0F;
         var4.y = this.y + var6 * 10.0F;
         var4.vx = var5 * 380.0F + this.vx;
         var4.vy = var6 * 380.0F + this.vy;
         var4.life = 0.85F;
         this.shots.add(var4);
         this.fx.add(var4.x, var4.y, 0.0F, 0.0F, 0.12F, 4.0F, -3462, 2, 0.0F, 0.0F);
         this.vx -= var5 * 6.0F;
         this.vy -= var6 * 6.0F;
         Sound.play("minecraft:block.note_block.hat", 2.0F, 0.25F);
      }
   }

   private void breakRock(int var1, float var2, float var3) {
      Asteroids.Rock var4 = this.rocks.remove(var1);
      int var5 = PTS[var4.size];
      this.score += var5;
      this.fx.popup("+" + var5, var4.x, var4.y - RAD[var4.size] * 0.5F, -1, 8 + var4.size);
      int var6 = 6 + var4.size * 6;

      for (int var7 = 0; var7 < var6; var7++) {
         double var8 = this.rnd.nextDouble() * Math.PI * 2.0;
         float var10 = 40.0F + this.rnd.nextFloat() * (60 + var4.size * 30);
         this.fx
            .add(
               var4.x,
               var4.y,
               (float)Math.cos(var8) * var10 + var2 * 0.1F,
               (float)Math.sin(var8) * var10 + var3 * 0.1F,
               0.5F + this.rnd.nextFloat() * 0.5F,
               1.2F + this.rnd.nextFloat(),
               this.rnd.nextBoolean() ? -3561729 : -1,
               5,
               0.0F,
               1.8F
            );
      }

      this.fx.ring(var4.x, var4.y, RAD[var4.size] * 1.8F, -3561729, 0.4F);
      this.fx.shake(0.08F * var4.size);
      Sound.play("minecraft:entity.generic.explode", 1.9F - var4.size * 0.3F, 0.12F + var4.size * 0.06F);
      if (var4.size > 1) {
         this.addRock(var4.x, var4.y, var4.size - 1, var4);
         this.addRock(var4.x, var4.y, var4.size - 1, var4);
      }
   }

   private void killUfo() {
      int var1 = this.ufo.small ? 1000 : 200;
      this.score += var1;
      this.fx.burst(this.ufo.x, this.ufo.y, 40, -41816, 190.0F, 2.4F, 0.8F, 0.0F, 5);
      this.fx.ring(this.ufo.x, this.ufo.y, 40.0F, -41816, 0.5F);
      this.fx.popup("UFO! +" + var1, this.ufo.x, this.ufo.y - 12.0F, -41816, 12.0F);
      this.fx.shake(0.5F);
      Sound.play("minecraft:entity.generic.explode", 1.2F, 0.4F);
      this.ufo = null;
   }

   private void crash() {
      this.dead = true;
      this.deadT = 0.0F;
      this.lives--;
      this.fx.burst(this.x, this.y, 50, -10685697, 200.0F, 2.2F, 1.1F, 0.0F, 5);
      this.fx.burst(this.x, this.y, 24, -19641, 120.0F, 2.8F, 0.8F, 0.0F, 2);
      this.fx.ring(this.x, this.y, 50.0F, -10685697, 0.6F);
      this.fx.shake(0.9F);
      this.fx.flash(-1, 0.5F);
      Sound.play("minecraft:entity.generic.explode", 0.8F, 0.5F);
   }

   private static float wrapX(float var0) {
      return var0 < 0.0F ? var0 + 400.0F : (var0 >= 400.0F ? var0 - 400.0F : var0);
   }

   private static float wrapY(float var0) {
      return var0 < 0.0F ? var0 + 290.0F : (var0 >= 290.0F ? var0 - 290.0F : var0);
   }

   @Override
   protected void render() {
      float var1 = Math.min(0.05F, Gx.dt);
      this.anim += var1;
      GameFx.sprite(this, "as_bg", 0.0F, 0.0F, 400.0F, 290.0F, 400.0F, 290.0F, Asteroids::paintSpace, -1);

      for (int var2 = 0; var2 < 24; var2++) {
         float var3 = var2 * 173.3F % 400.0F;
         float var4 = (var2 * 91.7F + 13.0F) % 290.0F;
         float var5 = 0.5F + 0.5F * (float)Math.sin(this.anim * (1.5F + var2 % 4) + var2);
         this.circle(var3, var4, 0.7F + var2 % 3 * 0.3F, Gx.withAlpha(-1, 0.3F + 0.6F * var5));
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (Asteroids.Rock var11 : this.rocks) {
         this.rock(var11);
      }

      if (this.ufo != null) {
         boolean var7 = this.ufo.small;
         float var12 = var7 ? 20.0F : 30.0F;
         Gx.glow(this.X(this.ufo.x), this.Y(this.ufo.y), this.S(var12), 872373416);
         GameFx.sprite(
            this,
            "as_ufo" + (int)(this.ufo.t * 8.0F) % 3,
            this.ufo.x - var12 / 2.0F - 2.0F,
            this.ufo.y - var12 * 0.35F - 2.0F,
            var12 + 4.0F,
            var12 * 0.7F + 4.0F,
            34.0F,
            24.0F,
            var1x -> paintUfo(var1x, (int)(this.ufo.t * 8.0F) % 3),
            -1
         );
      }

      for (Asteroids.Shot var13 : this.shots) {
         int var16 = var13.enemy ? -41816 : -3462;
         Gx.glow(this.X(var13.x), this.Y(var13.y), this.S(6.0F), Gx.withAlpha(var16, 0.5F));
         this.circle(var13.x, var13.y, 1.5F, -1);
         this.circle(var13.x - var13.vx * 0.012F, var13.y - var13.vy * 0.012F, 1.1F, Gx.withAlpha(var16, 0.6F));
      }

      this.fx.draw(this, 0.0F, 0.0F);
      if (!this.dead && (this.invul <= 0.0F || (int)(this.anim * 10.0F) % 2 == 0 || this.state != GameView.State.PLAYING)) {
         this.ship(this.x, this.y);
      }

      Gx.pop();

      for (int var9 = 0; var9 < Math.min(this.lives, 6); var9++) {
         int var14 = Math.round(36.0F) % 48;
         GameFx.sprite(this, "as_ship" + var14, 8 + var9 * 13, 8.0F, 14.0F, 14.0F, 30.0F, 30.0F, var1x -> paintShip(var1x, var14), -855638017);
      }

      GameFx.outlined(this, "Welle " + this.wave, 366.0F, 15.0F, 8.0F, -10685697, -1442840576);
      if (this.bannerT >= 0.0F) {
         float var10 = this.bannerT / 1.8F;
         float var15 = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float var17 = var10 < 0.7F ? 1.0F : 1.0F - (var10 - 0.7F) / 0.3F;
         Gx.pushAlpha(var17);
         Gx.push();
         Gx.scaleAt(this.X(200.0F), this.Y(87.0F), var15);
         Gx.glow(this.X(200.0F), this.Y(87.0F), this.S(70.0F), 1146942207);
         GameFx.outlined(this, "WELLE " + this.wave, 200.0F, 87.0F, 20.0F, -10685697, -872411112);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.dead && this.lives > 0 && this.deadT > 0.8F) {
         this.text("Mach dich bereit…", 200.0F, 175.0F, 8.0F, 3, Gx.withAlpha(-1, 0.6F + 0.4F * (float)Math.sin(this.anim * 6.0F)));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 400.0F, 290.0F);
   }

   private void rock(Asteroids.Rock var1) {
      float var2 = RAD[var1.size];
      int var3 = Math.floorMod(Math.round(var1.ang / (float) (Math.PI * 2) * 24.0F), 24);
      int var4 = var1.size;
      int var5 = var1.variant;
      String var6 = "as_rock" + var4 + "_" + var5 + "_" + var3;
      float var7 = var2 * 2.0F + 6.0F;

      for (int var8 = -1; var8 <= 1; var8++) {
         for (int var9 = -1; var9 <= 1; var9++) {
            float var10 = var1.x + var8 * 400.0F;
            float var11 = var1.y + var9 * 290.0F;
            if (!(var10 + var7 < 0.0F) && !(var10 - var7 > 400.0F) && !(var11 + var7 < 0.0F) && !(var11 - var7 > 290.0F)) {
               GameFx.sprite(this, var6, var10 - var7 / 2.0F, var11 - var7 / 2.0F, var7, var7, 64.0F, 64.0F, var3x -> paintRock(var3x, var4, var5, var3), -1);
            }
         }
      }
   }

   private void ship(float var1, float var2) {
      int var3 = Math.floorMod(Math.round(this.ang / (float) (Math.PI * 2) * 48.0F), 48);
      if (this.thrust && this.state == GameView.State.PLAYING) {
         Gx.glow(
            this.X(var1 - (float)Math.cos(this.ang) * 8.0F),
            this.Y(var2 - (float)Math.sin(this.ang) * 8.0F),
            this.S(10.0F + this.rnd.nextFloat() * 3.0F),
            1728023101
         );
      }

      Gx.glow(this.X(var1), this.Y(var2), this.S(18.0F), 576516863);

      for (int var4 = -1; var4 <= 1; var4++) {
         for (int var5 = -1; var5 <= 1; var5++) {
            float var6 = var1 + var4 * 400.0F;
            float var7 = var2 + var5 * 290.0F;
            if (!(var6 + 14.0F < 0.0F) && !(var6 - 14.0F > 400.0F) && !(var7 + 14.0F < 0.0F) && !(var7 - 14.0F > 290.0F)) {
               GameFx.sprite(this, "as_ship" + var3, var6 - 13.0F, var7 - 13.0F, 26.0F, 26.0F, 30.0F, 30.0F, var1x -> paintShip(var1x, var3), -1);
            }
         }
      }
   }

   private static void neon(Graphics2D var0, Shape var1, int var2, float var3) {
      Color var4 = new Color(var2);
      var0.setStroke(new BasicStroke(var3 * 3.2F, 1, 1));
      var0.setColor(new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 40));
      var0.draw(var1);
      var0.setStroke(new BasicStroke(var3 * 1.8F, 1, 1));
      var0.setColor(new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), 110));
      var0.draw(var1);
      var0.setStroke(new BasicStroke(var3, 1, 1));
      var0.setColor(new Color(Gx.mix(var2, -1, 0.45F)));
      var0.draw(var1);
   }

   private static void paintShip(Graphics2D var0, int var1) {
      var0.translate(15, 15);
      var0.rotate(var1 * Math.PI * 2.0 / 48.0);
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(11.0F, 0.0F);
      var2.lineTo(-8.0, -7.5);
      var2.lineTo(-5.0F, 0.0F);
      var2.lineTo(-8.0, 7.5);
      var2.closePath();
      var0.setColor(new Color(10, 40, 60, 200));
      var0.fill(var2);
      neon(var0, var2, -10685697, 1.4F);
      var0.setColor(new Color(16777215));
      var0.fill(new Float(1.0F, -1.6F, 3.2F, 3.2F));
   }

   private static void paintRock(Graphics2D var0, int var1, int var2, int var3) {
      var0.translate(32, 32);
      var0.rotate(var3 * Math.PI * 2.0 / 24.0);
      float var4 = 32.0F / (RAD[var1] + 3.0F);
      Random var5 = new Random(var2 * 31 + var1 * 7);
      int var6 = 9 + var5.nextInt(3);
      GeneralPath var7 = new GeneralPath();

      for (int var8 = 0; var8 < var6; var8++) {
         double var9 = var8 * Math.PI * 2.0 / var6;
         float var11 = RAD[var1] * (0.72F + var5.nextFloat() * 0.34F) * var4;
         float var12 = (float)Math.cos(var9) * var11;
         float var13 = (float)Math.sin(var9) * var11;
         if (var8 == 0) {
            var7.moveTo(var12, var13);
         } else {
            var7.lineTo(var12, var13);
         }
      }

      var7.closePath();
      var0.setColor(new Color(40, 20, 70, 170));
      var0.fill(var7);
      var0.setStroke(new BasicStroke(1.0F));
      var0.setColor(new Color(201, 166, 255, 70));

      for (int var14 = 0; var14 < 2; var14++) {
         float var15 = (var5.nextFloat() - 0.5F) * RAD[var1] * var4;
         float var10 = (var5.nextFloat() - 0.5F) * RAD[var1] * var4;
         float var16 = (2.0F + var5.nextFloat() * 3.0F) * var4 * 0.6F;
         var0.draw(new Float(var15 - var16, var10 - var16, var16 * 2.0F, var16 * 2.0F));
      }

      neon(var0, var7, -3561729, 1.5F * var4 * 0.55F + 0.4F);
   }

   private static void paintUfo(Graphics2D var0, int var1) {
      var0.translate(2, 2);
      GeneralPath var2 = new GeneralPath();
      var2.moveTo(0.0F, 12.0F);
      var2.lineTo(8.0F, 7.0F);
      var2.lineTo(22.0F, 7.0F);
      var2.lineTo(30.0F, 12.0F);
      var2.lineTo(22.0F, 17.0F);
      var2.lineTo(8.0F, 17.0F);
      var2.closePath();
      GeneralPath var3 = new GeneralPath();
      var3.moveTo(9.0F, 7.0F);
      var3.curveTo(10.0F, 0.0F, 20.0F, 0.0F, 21.0F, 7.0F);
      var0.setColor(new Color(60, 10, 40, 190));
      var0.fill(var2);
      neon(var0, var2, -41816, 1.2F);
      neon(var0, var3, -41816, 1.2F);
      var0.setStroke(new BasicStroke(0.9F));
      var0.setColor(new Color(16765416));
      var0.drawLine(2, 12, 28, 12);

      for (int var4 = 0; var4 < 3; var4++) {
         var0.setColor(var4 == var1 ? new Color(16773754) : new Color(255, 120, 190, 120));
         var0.fill(new Float(8.5F + var4 * 5.5F, 13.0F, 2.6F, 2.6F));
      }
   }

   private static void paintSpace(Graphics2D var0) {
      var0.setColor(new Color(329231));
      var0.fillRect(0, 0, 400, 290);
      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(90.0F, 70.0F), 150.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(90, 40, 150, 70), new Color(90, 40, 150, 0)}
         )
      );
      var0.fillRect(0, 0, 400, 290);
      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(330.0F, 230.0F),
            160.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(20, 110, 150, 60), new Color(20, 110, 150, 0)}
         )
      );
      var0.fillRect(0, 0, 400, 290);
      Random var1 = new Random(21L);

      for (int var2 = 0; var2 < 260; var2++) {
         float var3 = var1.nextFloat() * 400.0F;
         float var4 = var1.nextFloat() * 290.0F;
         float var5 = var1.nextFloat() < 0.9F ? 0.5F + var1.nextFloat() * 0.6F : 1.2F + var1.nextFloat();
         var0.setColor(new Color(255, 255, 255, 40 + var1.nextInt(150)));
         var0.fill(new Float(var3, var4, var5, var5));
      }

      var0.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(342.0F, 52.0F), 26.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(6966210), new Color(1774144)}
         )
      );
      var0.fill(new Float(322.0F, 34.0F, 40.0F, 40.0F));
      var0.setColor(new Color(180, 150, 255, 90));
      var0.setStroke(new BasicStroke(1.4F));
      var0.draw(new Float(310.0F, 49.0F, 64.0F, 10.0F));
   }

   private static final class Rock {
      float x;
      float y;
      float vx;
      float vy;
      float ang;
      float spin;
      int size;
      int variant;
      float flash;
   }

   private static final class Shot {
      float x;
      float y;
      float vx;
      float vy;
      float life;
      boolean enemy;
   }

   private static final class Ufo {
      float x;
      float y;
      float vx;
      float t;
      float fireT;
      boolean small;
      int hp;
   }
}
