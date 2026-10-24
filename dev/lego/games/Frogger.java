package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.Arrays;

final class Frogger extends GameView {
   private static final int COLS = 13;
   private static final int ROWS = 11;
   private static final float C = 24.0F;
   private int fx;
   private int fy;
   private int lives;
   private int level;
   private float rideX;
   private final float[] laneOff = new float[11];
   private float hurt;

   Frogger() {
      super("frogger");
   }

   @Override
   protected float boardW() {
      return 312.0F;
   }

   @Override
   protected float boardH() {
      return 264.0F;
   }

   @Override
   protected void reset() {
      this.lives = 3;
      this.level = 1;
      this.home();
      Arrays.fill(this.laneOff, 0.0F);
   }

   private void home() {
      this.fx = 6;
      this.fy = 10;
      this.rideX = 0.0F;
   }

   private int type(int r) {
      return r != 0 && r != 5 && r != 10 ? (r < 5 ? 2 : 1) : 0;
   }

   private float speed(int r) {
      return (r % 2 == 0 ? 1 : -1) * (1.4F + r * 37 % 5 * 0.35F) * (0.85F + this.level * 0.15F);
   }

   private int len(int r) {
      return this.type(r) == 2 ? 3 + r % 2 : 1 + (r % 3 == 0 ? 1 : 0);
   }

   private int gap(int r) {
      return this.type(r) == 2 ? 3 : 4 - r % 2;
   }

   private boolean covered(int r, float x) {
      float period = this.len(r) + this.gap(r);
      float p = ((x - this.laneOff[r]) % period + period) % period;
      return p < this.len(r);
   }

   @Override
   protected void onKey(int k) {
      if (!(this.hurt > 0.0F)) {
         if (k == 265 || k == 87) {
            this.fy--;
         } else if (k == 264 || k == 83) {
            this.fy = Math.min(10, this.fy + 1);
         } else if (k != 263 && k != 65) {
            if (k != 262 && k != 68) {
               return;
            }

            this.fx = Math.min(12, this.fx + 1);
         } else {
            this.fx = Math.max(0, this.fx - 1);
         }

         Sound.play("minecraft:block.wool.step", 1.4F, 0.3F);
         this.rideX = 0.0F;
         if (this.fy <= 0) {
            this.score = this.score + 100 * this.level;
            this.level++;
            Sound.play("minecraft:entity.player.levelup", 1.4F, 0.3F);
            this.home();
         }
      }
   }

   private void die() {
      this.lives--;
      this.hurt = 0.8F;
      Sound.play("minecraft:entity.slime.squish", 1.0F, 0.4F);
      if (this.lives <= 0) {
         this.gameOver();
      }
   }

   @Override
   protected void update(float dt) {
      for (int r = 0; r < 11; r++) {
         this.laneOff[r] = this.laneOff[r] + this.speed(r) * dt;
      }

      if (this.hurt > 0.0F) {
         this.hurt -= dt;
         if (this.hurt <= 0.0F) {
            this.home();
         }
      } else {
         float x = this.fx + 0.5F + this.rideX;
         int t = this.type(this.fy);
         if (t == 1 && this.covered(this.fy, x)) {
            this.die();
         }

         if (t == 2) {
            if (!this.covered(this.fy, x)) {
               this.die();
            } else {
               this.rideX = this.rideX + this.speed(this.fy) * dt;
               if (this.fx + this.rideX < -0.4F || this.fx + this.rideX > 12.4F) {
                  this.die();
               }
            }
         }

         if (t == 2 && this.hurt <= 0.0F && Math.abs(this.rideX) >= 1.0F) {
            int s = (int)Math.signum(this.rideX);
            this.fx += s;
            this.rideX -= s;
         }
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 11; r++) {
         int t = this.type(r);
         this.box(0.0F, r * 24.0F, 312.0F, 24.0F, 0.0F, t == 0 ? -12677567 : (t == 1 ? -13420994 : -15050080));
         if (t != 0) {
            float period = this.len(r) + this.gap(r);

            for (float s = -period * 2.0F; s < 13.0F + period; s += period) {
               float x0 = s + this.laneOff[r] % period;
               if (t == 1) {
                  int col = PackPuzzle.PAL[r % 6];
                  this.brick(x0 * 24.0F + 2.0F, r * 24.0F + 4.0F, this.len(r) * 24.0F - 4.0F, 16.0F, col);
               } else {
                  this.box(x0 * 24.0F + 1.0F, r * 24.0F + 4.0F, this.len(r) * 24.0F - 2.0F, 16.0F, 6.0F, -7710160);
               }
            }
         }
      }

      float x = (this.fx + this.rideX) * 24.0F;
      if (this.hurt <= 0.0F || (int)(this.hurt * 12.0F) % 2 == 0) {
         this.box(x + 4.0F, this.fy * 24.0F + 4.0F, 16.0F, 16.0F, 7.0F, -12987565);
         this.circle(x + 8.0F, this.fy * 24.0F + 7.0F, 2.5F, -1);
         this.circle(x + 24.0F - 8.0F, this.fy * 24.0F + 7.0F, 2.5F, -1);
      }

      this.text("Leben " + this.lives + " · Level " + this.level, 156.0F, 12.0F, 7.0F, 3, -1);
   }
}
