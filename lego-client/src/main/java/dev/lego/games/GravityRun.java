package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class GravityRun extends GameView {
   private static final float W = 400.0F;
   private static final float H = 240.0F;
   private static final float PX = 70.0F;
   private static final float PS = 14.0F;
   private final List<float[]> obs = new ArrayList<>();
   private float py;
   private float vy;
   private float spawn;
   private float speed;
   private boolean ceiling;

   GravityRun() {
      super("gravityrun");
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
      return "Meter";
   }

   @Override
   protected void reset() {
      this.obs.clear();
      this.py = 206.0F;
      this.vy = 0.0F;
      this.ceiling = false;
      this.spawn = 1.0F;
      this.speed = 150.0F;
   }

   private void flip() {
      if (Math.abs(this.vy) < 1.0F) {
         this.ceiling = !this.ceiling;
         this.vy = this.ceiling ? -1.0F : 1.0F;
         Sound.play("minecraft:block.note_block.hat", this.ceiling ? 1.8F : 1.2F, 0.3F);
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.flip();
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 265 || k == 87 || k == 264 || k == 83) {
         this.flip();
      }
   }

   @Override
   protected void update(float dt) {
      this.speed += dt * 4.0F;
      this.score = (long)(this.time * this.speed / 30.0F);
      this.vy = this.vy + (this.ceiling ? -900 : 900) * dt;
      this.py = this.py + this.vy * dt;
      if (this.py < 20.0F) {
         this.py = 20.0F;
         this.vy = 0.0F;
      }

      if (this.py > 206.0F) {
         this.py = 206.0F;
         this.vy = 0.0F;
      }

      this.spawn -= dt;
      if (this.spawn <= 0.0F) {
         this.spawn = 0.7F + this.rnd.nextFloat() * 0.8F - Math.min(0.4F, this.time / 120.0F);
         this.obs.add(new float[]{410.0F, this.rnd.nextBoolean() ? 1.0F : 0.0F, 26.0F + this.rnd.nextFloat() * 60.0F});
      }

      for (float[] o : this.obs) {
         o[0] -= this.speed * dt;
      }

      this.obs.removeIf(ox -> ox[0] < -30.0F);

      for (float[] o : this.obs) {
         float top = o[1] == 1.0F ? 20.0F : 220.0F - o[2];
         float bot = top + o[2];
         if (84.0F > o[0] && 70.0F < o[0] + 18.0F && this.py + 14.0F > top && this.py < bot) {
            PackArcade.boom();
            this.gameOver();
            return;
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 240.0F, 0.0F, -16119270, -15070678);
      this.box(0.0F, 0.0F, 400.0F, 20.0F, 0.0F, -12965286);
      this.box(0.0F, 220.0F, 400.0F, 20.0F, 0.0F, -12965286);

      for (float[] o : this.obs) {
         float top = o[1] == 1.0F ? 20.0F : 220.0F - o[2];
         this.box(o[0], top, 18.0F, o[2], 3.0F, -54678);
      }

      this.brick(70.0F, this.py, 14.0F, 14.0F, -12920577);
      this.text(this.score + " m", 360.0F, 10.0F, 7.0F, 3, -1);
   }
}
