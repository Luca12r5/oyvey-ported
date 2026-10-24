package dev.lego.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class Dodge extends GameView {
   private static final float W = 360.0F;
   private static final float H = 280.0F;
   private final List<float[]> rocks = new ArrayList<>();
   private float px = 180.0F;
   private float spawn;

   Dodge() {
      super("dodge");
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
      return "Sekunden";
   }

   @Override
   protected String scoreText() {
      return String.format(Locale.ROOT, "%.1f", this.time);
   }

   @Override
   protected String bestText(long best) {
      return String.format(Locale.ROOT, "%.1f", best / 10.0);
   }

   @Override
   protected void reset() {
      this.rocks.clear();
      this.px = 180.0F;
      this.spawn = 0.0F;
   }

   @Override
   protected void update(float dt) {
      this.px = this.px + (Math.max(10.0F, Math.min(350.0F, this.bmx())) - this.px) * Math.min(1.0F, dt * 14.0F);
      this.spawn = this.spawn - dt * (1.0F + this.time / 15.0F);
      if (this.spawn <= 0.0F) {
         this.spawn = 0.35F;
         this.rocks
            .add(
               new float[]{this.rnd.nextFloat() * 360.0F, -10.0F, 5.0F + this.rnd.nextFloat() * 10.0F, 90.0F + this.rnd.nextFloat() * 80.0F + this.time * 4.0F}
            );
      }

      for (float[] r : this.rocks) {
         r[1] += r[3] * dt;
      }

      this.rocks.removeIf(rx -> rx[1] > 300.0F);

      for (float[] r : this.rocks) {
         if (Math.hypot(r[0] - this.px, r[1] - 260.0F) < r[2] + 8.0F) {
            this.score = (long)(this.time * 10.0F);
            PackArcade.boom();
            this.gameOver();
            return;
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 360.0F, 280.0F, 0.0F, -15721936, -15062448);

      for (float[] r : this.rocks) {
         this.circle(r[0], r[1], r[2], -7706022);
         this.circle(r[0] - r[2] * 0.3F, r[1] - r[2] * 0.3F, r[2] * 0.3F, -5731718);
      }

      this.brick(this.px - 10.0F, 252.0F, 20.0F, 16.0F, -12604929);
      this.text(String.format(Locale.ROOT, "%.1f s", this.time), 180.0F, 16.0F, 10.0F, 3, -1);
   }
}
