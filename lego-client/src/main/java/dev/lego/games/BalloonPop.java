package dev.lego.games;

import dev.lego.ui.Gx;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class BalloonPop extends GameView {
   private static final float W = 380.0F;
   private static final float H = 280.0F;
   private final List<float[]> b = new ArrayList<>();
   private float spawn;
   private float left;

   BalloonPop() {
      super("balloonpop");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.b.clear();
      this.spawn = 0.0F;
      this.left = 45.0F;
   }

   @Override
   protected void onClick(float x, float y, int bt) {
      for (int i = this.b.size() - 1; i >= 0; i--) {
         float[] o = this.b.get(i);
         if (!(o[5] >= 0.0F) && !(Math.hypot(x - o[0], (y - o[1]) * 0.85F) > 15.0)) {
            o[5] = 0.0F;
            if (o[4] == 1.0F) {
               this.score = Math.max(0L, this.score - 30L);
               PackArcade.boom();
            } else {
               this.score = this.score + (o[4] == 2.0F ? 50L : 10L);
               PackArcade.pop();
            }

            return;
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      } else {
         this.spawn = this.spawn - dt * (1.0F + (45.0F - this.left) / 25.0F);
         if (this.spawn <= 0.0F) {
            this.spawn = 0.45F;
            float r = this.rnd.nextFloat();
            this.b
               .add(
                  new float[]{
                     20.0F + this.rnd.nextFloat() * 340.0F,
                     300.0F,
                     40.0F + this.rnd.nextFloat() * 50.0F + (45.0F - this.left) * 1.5F,
                     this.rnd.nextInt(6),
                     r < 0.12F ? 1.0F : (r < 0.2F ? 2.0F : 0.0F),
                     -1.0F
                  }
               );
         }

         for (float[] o : this.b) {
            o[1] -= o[2] * dt;
            if (o[5] >= 0.0F) {
               o[5] += dt;
            }
         }

         this.b.removeIf(ox -> ox[1] < -40.0F || ox[5] > 0.3F);
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 280.0F, 0.0F, -7681793, -3872);

      for (float[] o : this.b) {
         if (o[5] >= 0.0F) {
            this.circle(o[0], o[1], 14.0F + o[5] * 40.0F, Gx.withAlpha(-1, 1.0F - o[5] / 0.3F));
         } else {
            this.line(o[0], o[1] + 16.0F, o[0] + (float)Math.sin(this.time * 3.0F + o[0]) * 3.0F, o[1] + 34.0F, 0.6F, -10853776);
            if (o[4] == 1.0F) {
               this.circle(o[0], o[1], 13.0F, -15000798);
               this.text("!", o[0], o[1], 10.0F, 3, -50356);
            } else {
               int c = o[4] == 2.0F ? -11713 : PackPuzzle.PAL[(int)o[3]];
               this.box(o[0] - 12.0F, o[1] - 15.0F, 24.0F, 30.0F, 12.0F, c);
               this.circle(o[0] - 4.0F, o[1] - 7.0F, 3.0F, -1996488705);
            }
         }
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 350.0F, 14.0F, 9.0F, 3, this.left < 10.0F ? -50356 : -15000798);
   }
}
