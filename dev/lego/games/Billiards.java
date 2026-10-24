package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class Billiards extends GameView {
   private static final float W = 400.0F;
   private static final float H = 230.0F;
   private static final float R = 7.0F;
   private static final float M = 18.0F;
   private final List<float[]> balls = new ArrayList<>();
   private final float[][] pockets = new float[][]{{18.0F, 18.0F}, {200.0F, 15.0F}, {382.0F, 18.0F}, {18.0F, 212.0F}, {200.0F, 215.0F}, {382.0F, 212.0F}};
   private boolean aiming;
   private int shotsLeft;

   Billiards() {
      super("billiards");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 230.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Kugeln";
   }

   @Override
   protected String overTitle() {
      return this.potted() == 9 ? "Alle Kugeln versenkt!" : "Keine Stöße mehr";
   }

   @Override
   protected void reset() {
      this.balls.clear();
      this.balls.add(new float[]{100.0F, 115.0F, 0.0F, 0.0F, -1.0F, 0.0F});
      int k = 0;

      for (int r = 0; r < 4 && k < 9; r++) {
         for (int i = 0; i <= r && k < 9; i++) {
            this.balls.add(new float[]{270.0F + r * 7.0F * 1.8F, 115.0F + (i - r / 2.0F) * 7.0F * 2.05F, 0.0F, 0.0F, k++ % 6, 0.0F});
         }
      }

      this.balls.add(new float[]{320.4F, 115.0F, 0.0F, 0.0F, 2.0F, 0.0F});
      this.shotsLeft = 15;
   }

   private int potted() {
      int n = 0;

      for (float[] b : this.balls) {
         if (b[4] >= 0.0F && b[5] == 1.0F) {
            n++;
         }
      }

      return n;
   }

   private boolean moving() {
      for (float[] b : this.balls) {
         if (b[5] == 0.0F && Math.hypot(b[2], b[3]) > 3.0) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.moving()) {
         this.aiming = true;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.aiming) {
         this.aiming = false;
         float[] cue = this.balls.get(0);
         float dx = cue[0] - x;
         float dy = cue[1] - y;
         float d = (float)Math.min(140.0, Math.hypot(dx, dy));
         float n = (float)Math.hypot(dx, dy);
         if (!(n < 5.0F)) {
            cue[2] = dx / n * d * 5.0F;
            cue[3] = dy / n * d * 5.0F;
            this.shotsLeft--;
            Sound.play("minecraft:block.wood.hit", 1.8F, 0.4F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      for (int s = 0; s < 4; s++) {
         float h = dt / 4.0F;

         for (float[] b : this.balls) {
            if (b[5] != 1.0F) {
               b[0] += b[2] * h;
               b[1] += b[3] * h;
               float f = (float)Math.pow(0.45, h);
               b[2] *= f;
               b[3] *= f;
               if (b[0] < 25.0F) {
                  b[0] = 25.0F;
                  b[2] = Math.abs(b[2]) * 0.8F;
               }

               if (b[0] > 375.0F) {
                  b[0] = 375.0F;
                  b[2] = -Math.abs(b[2]) * 0.8F;
               }

               if (b[1] < 25.0F) {
                  b[1] = 25.0F;
                  b[3] = Math.abs(b[3]) * 0.8F;
               }

               if (b[1] > 205.0F) {
                  b[1] = 205.0F;
                  b[3] = -Math.abs(b[3]) * 0.8F;
               }

               for (float[] p : this.pockets) {
                  if (Math.hypot(b[0] - p[0], b[1] - p[1]) < 13.0) {
                     if (b[4] < 0.0F) {
                        b[0] = 100.0F;
                        b[1] = 115.0F;
                        b[2] = b[3] = 0.0F;
                        PackPuzzle.bad();
                     } else {
                        b[5] = 1.0F;
                        this.score = this.potted();
                        Sound.play("minecraft:entity.item.pickup", 0.9F, 0.4F);
                     }
                  }
               }
            }
         }

         for (int i = 0; i < this.balls.size(); i++) {
            for (int j = i + 1; j < this.balls.size(); j++) {
               float[] a = this.balls.get(i);
               float[] c = this.balls.get(j);
               if (a[5] != 1.0F && c[5] != 1.0F) {
                  float dx = c[0] - a[0];
                  float dy = c[1] - a[1];
                  float d = (float)Math.hypot(dx, dy);
                  if (d < 14.0F && d > 0.0F) {
                     float nx = dx / d;
                     float ny = dy / d;
                     float ov = 14.0F - d;
                     a[0] -= nx * ov / 2.0F;
                     a[1] -= ny * ov / 2.0F;
                     c[0] += nx * ov / 2.0F;
                     c[1] += ny * ov / 2.0F;
                     float rel = (a[2] - c[2]) * nx + (a[3] - c[3]) * ny;
                     if (rel > 0.0F) {
                        a[2] -= rel * nx;
                        a[3] -= rel * ny;
                        c[2] += rel * nx;
                        c[3] += rel * ny;
                        Sound.play("minecraft:block.stone.hit", 1.8F, Math.min(0.5F, rel / 400.0F));
                     }
                  }
               }
            }
         }
      }

      if (!this.moving()) {
         if (this.potted() == 9) {
            this.score = this.score + this.shotsLeft * 2L;
            PackPuzzle.win();
            this.gameOver();
         } else if (this.shotsLeft <= 0) {
            this.gameOver();
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 400.0F, 230.0F, 12.0F, -9815526);
      this.box(12.0F, 12.0F, 376.0F, 206.0F, 8.0F, -14779846);

      for (float[] p : this.pockets) {
         this.circle(p[0], p[1], 11.0F, -16119286);
      }

      for (float[] b : this.balls) {
         if (b[5] == 0.0F) {
            this.circle(b[0], b[1], 7.0F, b[4] < 0.0F ? -460552 : PackPuzzle.PAL[(int)b[4]]);
            this.circle(b[0] - 2.0F, b[1] - 2.0F, 2.0F, -1711276033);
         }
      }

      float[] cue = this.balls.get(0);
      if (this.aiming) {
         float dx = cue[0] - this.bmx();
         float dy = cue[1] - this.bmy();
         float n = (float)Math.hypot(dx, dy);
         if (n > 0.0F) {
            this.line(cue[0], cue[1], cue[0] + dx / n * 120.0F, cue[1] + dy / n * 120.0F, 0.6F, -1996488705);
            this.line(
               cue[0] - dx / n * (12.0F + Math.min(140.0F, n) * 0.3F),
               cue[1] - dy / n * (12.0F + Math.min(140.0F, n) * 0.3F),
               cue[0] - dx / n * 120.0F,
               cue[1] - dy / n * 120.0F,
               3.0F,
               -3628960
            );
         }
      }

      this.textL("Versenkt " + this.potted() + "/9 · Stöße " + this.shotsLeft, 22.0F, 8.0F, 6.6F, 3, -1);
   }
}
