package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

final class Highway extends GameView {
   private static final float W = 260.0F;
   private static final float H = 320.0F;
   private final List<float[]> cars = new ArrayList<>();
   private float lane = 1.0F;
   private float laneT = 1.0F;
   private float roadY;
   private float spawn;
   private float speed;

   Highway() {
      super("highway");
   }

   @Override
   protected float boardW() {
      return 260.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Meter";
   }

   @Override
   protected void reset() {
      this.cars.clear();
      this.lane = this.laneT = 1.0F;
      this.spawn = 0.0F;
      this.speed = 200.0F;
   }

   @Override
   protected void onKey(int k) {
      if ((k == 263 || k == 65) && this.laneT > 0.0F) {
         this.laneT--;
      }

      if ((k == 262 || k == 68) && this.laneT < 3.0F) {
         this.laneT++;
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (x < 130.0F && this.laneT > 0.0F) {
         this.laneT--;
      } else if (x >= 130.0F && this.laneT < 3.0F) {
         this.laneT++;
      }
   }

   private float lx(float l) {
      return 40.0F + l * 60.0F;
   }

   @Override
   protected void update(float dt) {
      this.speed += dt * 6.0F;
      this.score = (long)(this.time * this.speed / 20.0F);
      this.lane = this.lane + (this.laneT - this.lane) * Math.min(1.0F, dt * 12.0F);
      this.roadY = (this.roadY + this.speed * dt) % 40.0F;
      this.spawn = this.spawn - dt * this.speed / 200.0F;
      if (this.spawn <= 0.0F) {
         this.spawn = 0.55F;
         this.cars.add(new float[]{this.rnd.nextInt(4), -40.0F, 60.0F + this.rnd.nextFloat() * 80.0F, this.rnd.nextInt(6)});
      }

      for (float[] c : this.cars) {
         c[1] += (this.speed - c[2]) * dt;
      }

      this.cars.removeIf(cx -> cx[1] > 370.0F);

      for (float[] c : this.cars) {
         if (Math.abs(this.lx(c[0]) - this.lx(this.lane)) < 30.0F && Math.abs(c[1] - 260.0F) < 40.0F) {
            PackArcade.boom();
            this.gameOver();
            return;
         }
      }
   }

   private void car(float x, float y, int col) {
      this.box(x - 14.0F, y - 22.0F, 28.0F, 44.0F, 7.0F, col);
      this.box(x - 10.0F, y - 12.0F, 20.0F, 12.0F, 3.0F, -861939457);
      this.box(x - 12.0F, y + 12.0F, 24.0F, 6.0F, 2.0F, 1426063360);
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 260.0F, 320.0F, 0.0F, -13420994);
      this.box(0.0F, 0.0F, 10.0F, 320.0F, 0.0F, -12673462);
      this.box(250.0F, 0.0F, 10.0F, 320.0F, 0.0F, -12673462);

      for (int l = 1; l < 4; l++) {
         for (float y = -40.0F + this.roadY; y < 320.0F; y += 40.0F) {
            this.box(this.lx(l) - 30.0F - 1.5F, y, 3.0F, 20.0F, 0.0F, -856361740);
         }
      }

      for (float[] c : this.cars) {
         this.car(this.lx(c[0]), c[1], PackPuzzle.PAL[(int)c[3]]);
      }

      this.car(this.lx(this.lane), 260.0F, -723724);
      this.text(this.score + " m", 130.0F, 14.0F, 9.0F, 3, -1);
   }
}
