package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class Plinko extends GameView {
   private static final float W = 360.0F;
   private static final float H = 290.0F;
   private static final int[] BINS = new int[]{50, 20, 10, 5, 1, 5, 10, 20, 50};
   private final List<float[]> balls = new ArrayList<>();
   private int left;
   private float lastBin = -1.0F;
   private float binFx;

   Plinko() {
      super("plinko");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected String overTitle() {
      return "Alle Kugeln gespielt";
   }

   @Override
   protected void reset() {
      this.balls.clear();
      this.left = 10;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.left > 0 && !(y > 40.0F)) {
         this.left--;
         this.balls.add(new float[]{Math.max(20.0F, Math.min(340.0F, x)), 12.0F, (this.rnd.nextFloat() - 0.5F) * 10.0F, 0.0F});
         PackArcade.pop();
      }
   }

   private float pegX(int r, int c) {
      return 30.0F + c * 37.5F + r % 2 * 18.75F;
   }

   private float pegY(int r) {
      return 55 + r * 22;
   }

   @Override
   protected void update(float dt) {
      this.binFx = Math.max(0.0F, this.binFx - dt);

      for (int i = this.balls.size() - 1; i >= 0; i--) {
         float[] b = this.balls.get(i);
         b[3] += 380.0F * dt;
         b[0] += b[2] * dt;
         b[1] += b[3] * dt;
         if (b[0] < 6.0F) {
            b[0] = 6.0F;
            b[2] = Math.abs(b[2]) * 0.6F;
         }

         if (b[0] > 354.0F) {
            b[0] = 354.0F;
            b[2] = -Math.abs(b[2]) * 0.6F;
         }

         for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
               float dx = b[0] - this.pegX(r, c);
               float dy = b[1] - this.pegY(r);
               float d = (float)Math.hypot(dx, dy);
               if (d < 8.0F && d > 0.0F) {
                  float nx = dx / d;
                  float ny = dy / d;
                  b[0] = this.pegX(r, c) + nx * 8.0F;
                  b[1] = this.pegY(r) + ny * 8.0F;
                  float dot = b[2] * nx + b[3] * ny;
                  if (dot < 0.0F) {
                     b[2] -= 1.5F * dot * nx;
                     b[3] -= 1.5F * dot * ny;
                     b[2] += (this.rnd.nextFloat() - 0.5F) * 30.0F;
                     Sound.play("minecraft:block.note_block.hat", 1.6F + this.rnd.nextFloat() * 0.3F, 0.12F);
                  }
               }
            }
         }

         if (b[1] > 268.0F) {
            int bin = Math.max(0, Math.min(8, (int)(b[0] / 40.0F)));
            this.score = this.score + BINS[bin];
            this.lastBin = bin;
            this.binFx = 1.0F;
            Sound.play(BINS[bin] >= 20 ? "minecraft:entity.player.levelup" : "minecraft:entity.experience_orb.pickup", 1.4F, 0.3F);
            this.balls.remove(i);
         }
      }

      if (this.left <= 0 && this.balls.isEmpty()) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 360.0F, 290.0F, 0.0F, -15068096, -14017968);

      for (int r = 0; r < 9; r++) {
         for (int c = 0; c < 9; c++) {
            if (this.pegX(r, c) < 350.0F) {
               this.circle(this.pegX(r, c), this.pegY(r), 3.2F, -2564378);
            }
         }
      }

      float bw = 40.0F;

      for (int i = 0; i < 9; i++) {
         int col = BINS[i] >= 50 ? -13053 : (BINS[i] >= 20 ? -30208 : (BINS[i] >= 10 ? -12604929 : -10853776));
         if (i == this.lastBin && this.binFx > 0.0F) {
            col = Gx.mix(col, -1, this.binFx * 0.6F);
         }

         this.box(i * bw + 2.0F, 268.0F, bw - 4.0F, 20.0F, 4.0F, col);
         this.text(String.valueOf(BINS[i]), i * bw + bw / 2.0F, 279.0F, 8.0F, 3, -15724528);
      }

      for (float[] b : this.balls) {
         this.circle(b[0], b[1], 5.0F, -1900533);
      }

      if (this.left > 0) {
         float x = Math.max(20.0F, Math.min(340.0F, this.bmx()));
         this.circle(x, 14.0F, 5.0F, -1998389237);
      }

      this.textL("Kugeln: " + this.left, 8.0F, 10.0F, 7.0F, 3, -1);
   }
}
