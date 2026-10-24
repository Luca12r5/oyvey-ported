package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class Catapult extends GameView {
   private static final float W = 420.0F;
   private static final float H = 270.0F;
   private static final float GROUND = 245.0F;
   private static final float SX = 60.0F;
   private static final float SY = 200.0F;
   private final List<float[]> blocks = new ArrayList<>();
   private float bxx;
   private float byy;
   private float vx;
   private float vy;
   private float settle;
   private boolean drag;
   private boolean flying;
   private int shots;
   private int level;

   Catapult() {
      super("catapult");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Keine Schüsse mehr";
   }

   @Override
   protected void reset() {
      this.level = 1;
      this.build();
   }

   private void build() {
      this.blocks.clear();
      float x0 = 250.0F + this.rnd.nextFloat() * 40.0F;
      int towers = 2 + Math.min(2, this.level / 2);

      for (int t = 0; t < towers; t++) {
         float x = x0 + t * 45;
         int hgt = 2 + this.rnd.nextInt(3);

         for (int k = 0; k < hgt; k++) {
            this.blocks.add(new float[]{x, 245.0F - (k + 1) * 22, 14.0F, 22.0F, 0.0F, 0.0F, 1.0F, 0.0F});
         }

         this.blocks.add(new float[]{x - 1.0F, 245.0F - hgt * 22 - 16.0F, 16.0F, 16.0F, 0.0F, 0.0F, 1.0F, 1.0F});
      }

      this.shots = 3;
      this.reset2();
   }

   private void reset2() {
      this.bxx = 60.0F;
      this.byy = 200.0F;
      this.vx = this.vy = 0.0F;
      this.flying = false;
      this.settle = 0.0F;
   }

   private int targets() {
      int n = 0;

      for (float[] b : this.blocks) {
         if (b[6] == 1.0F && b[7] == 1.0F) {
            n++;
         }
      }

      return n;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.flying && Math.hypot(x - 60.0F, y - 200.0F) < 30.0) {
         this.drag = true;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.drag) {
         this.drag = false;
         float dx = 60.0F - x;
         float dy = 200.0F - y;
         float d = (float)Math.min(60.0, Math.hypot(dx, dy));
         if (!(d < 8.0F)) {
            float n = (float)Math.hypot(dx, dy);
            this.vx = dx / n * d * 9.0F;
            this.vy = dy / n * d * 9.0F;
            this.flying = true;
            this.shots--;
            Sound.play("minecraft:entity.arrow.shoot", 0.8F, 0.4F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      for (float[] b : this.blocks) {
         if (b[6] != 0.0F) {
            b[5] += 500.0F * dt;
            b[1] += b[5] * dt;
            b[0] += b[4] * dt;
            b[4] *= 0.98F;
            float floor = 245.0F;

            for (float[] o : this.blocks) {
               if (o != b && o[6] == 1.0F && b[0] + b[2] > o[0] + 1.0F && b[0] < o[0] + o[2] - 1.0F && o[1] >= b[1] + b[3] - 4.0F) {
                  floor = Math.min(floor, o[1]);
               }
            }

            if (b[1] + b[3] > floor) {
               if (b[5] > 260.0F && b[7] == 1.0F) {
                  b[6] = 0.0F;
                  this.score += 500L;
                  Sound.play("minecraft:entity.creeper.death", 1.2F, 0.4F);
               }

               b[1] = floor - b[3];
               b[5] = 0.0F;
               b[4] *= 0.6F;
            }

            if (b[0] < -40.0F || b[0] > 460.0F) {
               b[6] = 0.0F;
            }
         }
      }

      if (this.flying) {
         this.vy += 500.0F * dt;
         this.bxx = this.bxx + this.vx * dt;
         this.byy = this.byy + this.vy * dt;

         for (float[] bx : this.blocks) {
            if (bx[6] == 1.0F && this.bxx + 7.0F > bx[0] && this.bxx - 7.0F < bx[0] + bx[2] && this.byy + 7.0F > bx[1] && this.byy - 7.0F < bx[1] + bx[3]) {
               if (bx[7] == 1.0F) {
                  bx[6] = 0.0F;
                  this.score += 500L;
                  Sound.play("minecraft:entity.creeper.death", 1.2F, 0.4F);
               } else {
                  bx[4] += this.vx * 0.6F;
                  bx[5] -= 80.0F;
                  this.score += 10L;
                  Sound.play("minecraft:block.wood.break", 1.0F, 0.4F);
               }

               this.vx *= 0.5F;
               this.vy *= 0.5F;
            }
         }

         if (this.byy > 238.0F) {
            this.byy = 238.0F;
            this.vy = -this.vy * 0.3F;
            this.vx *= 0.7F;
         }

         if (this.bxx > 450.0F || Math.abs(this.vx) < 15.0F && this.byy >= 237.0F) {
            this.settle += dt;
            if (this.settle > 0.8F) {
               this.after();
            }
         }
      }
   }

   private void after() {
      if (this.targets() == 0) {
         this.score = this.score + this.shots * 300L;
         this.level++;
         PackPuzzle.win();
         this.build();
      } else if (this.shots <= 0) {
         this.gameOver();
      } else {
         this.reset2();
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 420.0F, 270.0F, 0.0F, -7681793, -2559745);
      this.box(0.0F, 245.0F, 420.0F, 25.0F, 0.0F, -10634418);
      this.line(52.0F, 245.0F, 56.0F, 206.0F, 4.0F, -9811414);
      this.line(68.0F, 245.0F, 64.0F, 206.0F, 4.0F, -9811414);

      for (float[] b : this.blocks) {
         if (b[6] == 1.0F) {
            if (b[7] == 1.0F) {
               this.box(b[0], b[1], b[2], b[3], 2.0F, -12987565);
               this.box(b[0] + 3.0F, b[1] + 4.0F, 3.0F, 3.0F, 0.0F, -15000798);
               this.box(b[0] + b[2] - 6.0F, b[1] + 4.0F, 3.0F, 3.0F, 0.0F, -15000798);
               this.box(b[0] + 5.0F, b[1] + 9.0F, b[2] - 10.0F, 5.0F, 0.0F, -15000798);
            } else {
               this.brick(b[0], b[1], b[2], b[3], -3635398);
            }
         }
      }

      float x = this.bxx;
      float y = this.byy;
      if (this.drag) {
         float dx = 60.0F - this.bmx();
         float dy = 200.0F - this.bmy();
         float n = (float)Math.hypot(dx, dy);
         float d = Math.min(60.0F, n);
         if (n > 0.0F) {
            x = 60.0F - dx / n * d;
            y = 200.0F - dy / n * d;
         }

         this.line(56.0F, 200.0F, x, y, 1.5F, -12966892);
         this.line(64.0F, 200.0F, x, y, 1.5F, -12966892);
      }

      this.circle(x, y, 7.0F, -1900533);
      this.circle(x - 2.0F, y - 2.0F, 2.0F, -1426063361);
      this.textL("Level " + this.level + " · Schüsse " + this.shots + " · Creeper " + this.targets(), 8.0F, 12.0F, 7.5F, 3, -15000798);
   }
}
