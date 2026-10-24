package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class Ski extends GameView {
   private static final float W = 320.0F;
   private static final float H = 300.0F;
   private final List<float[]> obj = new ArrayList<>();
   private float sx = 160.0F;
   private float speed;
   private float spawn;
   private float lean;
   private int lives;

   Ski() {
      super("ski");
   }

   @Override
   protected float boardW() {
      return 320.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Meter";
   }

   @Override
   protected void reset() {
      this.obj.clear();
      this.sx = 160.0F;
      this.speed = 120.0F;
      this.spawn = 0.0F;
      this.lives = 3;
   }

   @Override
   protected void update(float dt) {
      float tx = Math.max(10.0F, Math.min(310.0F, this.bmx()));
      this.lean = Math.max(-1.0F, Math.min(1.0F, (tx - this.sx) / 40.0F));
      this.sx = this.sx + (tx - this.sx) * Math.min(1.0F, dt * 5.0F);
      this.speed = Math.min(360.0F, this.speed + dt * 6.0F);
      this.score = (long)(this.time * this.speed / 25.0F);
      this.spawn = this.spawn - dt * this.speed / 120.0F;
      if (this.spawn <= 0.0F) {
         this.spawn = 0.45F;
         float r = this.rnd.nextFloat();
         if (r < 0.25F) {
            float gx = 40.0F + this.rnd.nextFloat() * 200.0F;
            this.obj.add(new float[]{gx, 320.0F, 1.0F, 0.0F});
         } else {
            this.obj.add(new float[]{this.rnd.nextFloat() * 320.0F, 320.0F, r < 0.85F ? 0.0F : 2.0F, 0.0F});
         }
      }

      for (float[] o : this.obj) {
         o[1] -= this.speed * dt;
      }

      for (float[] o : this.obj) {
         if (o[3] == 0.0F) {
            if (o[2] == 1.0F) {
               if (o[1] < 70.0F) {
                  o[3] = 1.0F;
                  if (this.sx > o[0] && this.sx < o[0] + 60.0F) {
                     this.score += 0L;
                     this.time++;
                     Sound.play("minecraft:entity.experience_orb.pickup", 1.4F, 0.3F);
                  }
               }
            } else if (Math.abs(o[0] - this.sx) < 9.0F && Math.abs(o[1] - 70.0F) < 9.0F) {
               o[3] = 1.0F;
               this.lives--;
               this.speed *= 0.5F;
               PackPuzzle.bad();
               if (this.lives <= 0) {
                  this.gameOver();
                  return;
               }
            }
         }
      }

      this.obj.removeIf(ox -> ox[1] < -30.0F);
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 320.0F, 300.0F, 0.0F, -722689);

      for (float[] o : this.obj) {
         if (o[2] == 0.0F) {
            this.box(o[0] - 1.5F, o[1] + 4.0F, 3.0F, 6.0F, 0.0F, -9811414);
            this.circle(o[0], o[1], 9.0F, -13730510);
            this.circle(o[0], o[1] - 6.0F, 6.0F, -12673462);
         } else if (o[2] == 1.0F) {
            this.line(o[0], o[1] - 12.0F, o[0], o[1] + 4.0F, 1.0F, -1900533);
            this.line(o[0] + 60.0F, o[1] - 12.0F, o[0] + 60.0F, o[1] + 4.0F, 1.0F, -16749385);
            this.box(o[0], o[1] - 12.0F, 7.0F, 5.0F, 0.0F, -1900533);
            this.box(o[0] + 53.0F, o[1] - 12.0F, 7.0F, 5.0F, 0.0F, -16749385);
         } else {
            this.circle(o[0], o[1], 8.0F, -7697774);
            this.circle(o[0] - 2.0F, o[1] - 2.0F, 3.0F, -5197640);
         }
      }

      this.line(this.sx - 6.0F + this.lean * 3.0F, 78.0F, this.sx + 6.0F + this.lean * 3.0F, 62.0F, 1.6F, -15000798);
      this.line(this.sx - 2.0F + this.lean * 3.0F, 80.0F, this.sx + 9.0F + this.lean * 3.0F, 64.0F, 1.6F, -15000798);
      this.circle(this.sx, 66.0F, 5.0F, -1900533);
      this.circle(this.sx, 59.0F, 3.5F, -12128);
      this.text(this.score + " m · Leben " + this.lives, 160.0F, 14.0F, 7.0F, 3, -15000798);
      this.text("Durch die Tore = Bonus", 160.0F, 290.0F, 6.0F, 2, -10853776);
   }
}
