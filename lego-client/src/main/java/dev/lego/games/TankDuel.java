package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class TankDuel extends GameView {
   private static final float W = 400.0F;
   private static final float H = 280.0F;
   private static final float[][] WALLS = new float[][]{
      {120.0F, 60.0F, 20.0F, 80.0F}, {260.0F, 140.0F, 20.0F, 80.0F}, {170.0F, 200.0F, 60.0F, 16.0F}, {170.0F, 64.0F, 60.0F, 16.0F}
   };
   private float px;
   private float py;
   private float pa;
   private float ax;
   private float ay;
   private float aa;
   private float cool;
   private float aiCool;
   private float aiTurn;
   private int php;
   private int ahp;
   private final List<float[]> bullets = new ArrayList<>();
   private boolean up;
   private boolean down;
   private boolean left;
   private boolean right;

   TankDuel() {
      super("tanks");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Treffer";
   }

   @Override
   protected String overTitle() {
      return this.ahp <= 0 ? "Gewonnen!" : "Zerstört";
   }

   @Override
   protected void reset() {
      this.px = 40.0F;
      this.py = 140.0F;
      this.ax = 360.0F;
      this.ay = 140.0F;
      this.php = this.ahp = 3;
      this.bullets.clear();
      this.cool = this.aiCool = 0.0F;
      this.score = 0L;
   }

   private boolean blocked(float x, float y) {
      if (!(x < 10.0F) && !(y < 10.0F) && !(x > 390.0F) && !(y > 270.0F)) {
         for (float[] w : WALLS) {
            if (x > w[0] - 9.0F && x < w[0] + w[2] + 9.0F && y > w[1] - 9.0F && y < w[1] + w[3] + 9.0F) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private boolean wall(float x, float y) {
      for (float[] w : WALLS) {
         if (x > w[0] && x < w[0] + w[2] && y > w[1] && y < w[1] + w[3]) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected void onKey(int k) {
      if (k == 87 || k == 265) {
         this.up = true;
      }

      if (k == 83 || k == 264) {
         this.down = true;
      }

      if (k == 65 || k == 263) {
         this.left = true;
      }

      if (k == 68 || k == 262) {
         this.right = true;
      }

      if (k == 32) {
         this.shoot();
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 87 || k == 265) {
         this.up = false;
      }

      if (k == 83 || k == 264) {
         this.down = false;
      }

      if (k == 65 || k == 263) {
         this.left = false;
      }

      if (k == 68 || k == 262) {
         this.right = false;
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.shoot();
   }

   private void shoot() {
      if (!(this.cool > 0.0F)) {
         this.cool = 0.45F;
         float a = (float)Math.atan2(this.bmy() - this.py, this.bmx() - this.px);
         this.bullets
            .add(
               new float[]{
                  this.px + (float)Math.cos(a) * 12.0F,
                  this.py + (float)Math.sin(a) * 12.0F,
                  (float)Math.cos(a) * 260.0F,
                  (float)Math.sin(a) * 260.0F,
                  1.0F,
                  0.0F
               }
            );
         Sound.play("minecraft:entity.firework_rocket.blast", 1.6F, 0.3F);
      }
   }

   @Override
   protected void update(float dt) {
      this.cool -= dt;
      this.aiCool -= dt;
      float mx = (this.right ? 1 : 0) - (this.left ? 1 : 0);
      float my = (this.down ? 1 : 0) - (this.up ? 1 : 0);
      if (mx != 0.0F || my != 0.0F) {
         float n = (float)Math.hypot(mx, my);
         float nx = this.px + mx / n * 90.0F * dt;
         float ny = this.py + my / n * 90.0F * dt;
         if (!this.blocked(nx, this.py)) {
            this.px = nx;
         }

         if (!this.blocked(this.px, ny)) {
            this.py = ny;
         }

         this.pa = (float)Math.atan2(my, mx);
      }

      int lv = this.aiLevel();
      this.aiTurn -= dt;
      if (this.aiTurn <= 0.0F) {
         this.aiTurn = 0.6F + this.rnd.nextFloat();
         this.aa = (float)(Math.atan2(this.py - this.ay, this.px - this.ax) + (this.rnd.nextBoolean() ? 1.6 : -1.6) + (this.rnd.nextFloat() - 0.5F));
      }

      float sp = new float[]{45.0F, 60.0F, 75.0F, 90.0F}[lv];
      float nxx = this.ax + (float)Math.cos(this.aa) * sp * dt;
      float nyx = this.ay + (float)Math.sin(this.aa) * sp * dt;
      if (!this.blocked(nxx, nyx)) {
         this.ax = nxx;
         this.ay = nyx;
      } else {
         this.aiTurn = 0.0F;
      }

      boolean sight = true;

      for (int i = 1; i < 20; i++) {
         float t = i / 20.0F;
         if (this.wall(this.ax + (this.px - this.ax) * t, this.ay + (this.py - this.ay) * t)) {
            sight = false;
         }
      }

      if (sight && this.aiCool <= 0.0F) {
         this.aiCool = new float[]{1.6F, 1.1F, 0.8F, 0.55F}[lv];
         float err = new float[]{0.3F, 0.18F, 0.09F, 0.03F}[lv];
         float a = (float)Math.atan2(this.py - this.ay, this.px - this.ax) + (this.rnd.nextFloat() - 0.5F) * err * 2.0F;
         this.bullets
            .add(
               new float[]{
                  this.ax + (float)Math.cos(a) * 12.0F,
                  this.ay + (float)Math.sin(a) * 12.0F,
                  (float)Math.cos(a) * 240.0F,
                  (float)Math.sin(a) * 240.0F,
                  2.0F,
                  0.0F
               }
            );
      }

      for (int ix = this.bullets.size() - 1; ix >= 0; ix--) {
         float[] b = this.bullets.get(ix);
         float ox = b[0];
         float oy = b[1];
         b[0] += b[2] * dt;
         b[1] += b[3] * dt;
         boolean hitWall = this.wall(b[0], b[1]) || b[0] < 0.0F || b[0] > 400.0F || b[1] < 0.0F || b[1] > 280.0F;
         if (hitWall) {
            if (b[5] >= 1.0F) {
               this.bullets.remove(ix);
            } else {
               b[5]++;
               if (!this.wall(b[0], oy) && !(b[0] < 0.0F) && !(b[0] > 400.0F)) {
                  b[3] = -b[3];
               } else {
                  b[2] = -b[2];
               }

               b[0] = ox;
               b[1] = oy;
            }
         } else if (b[4] == 1.0F && Math.hypot(b[0] - this.ax, b[1] - this.ay) < 11.0) {
            this.ahp--;
            this.score++;
            this.bullets.remove(ix);
            PackArcade.boom();
            if (this.ahp <= 0) {
               PackPuzzle.win();
               this.gameOver();
               return;
            }
         } else if (b[4] == 2.0F && Math.hypot(b[0] - this.px, b[1] - this.py) < 11.0) {
            this.php--;
            this.bullets.remove(ix);
            PackArcade.boom();
            if (this.php <= 0) {
               this.gameOver();
               return;
            }
         }
      }
   }

   private void tank(float x, float y, float body, float aim, int col) {
      this.box(x - 10.0F, y - 10.0F, 20.0F, 20.0F, 5.0F, col);
      this.box(x - 6.0F, y - 6.0F, 12.0F, 12.0F, 3.0F, Gx.mix(col, -1, 0.25F));
      this.line(x, y, x + (float)Math.cos(aim) * 15.0F, y + (float)Math.sin(aim) * 15.0F, 3.5F, -14012872);
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, -3624832);

      for (float[] w : WALLS) {
         this.brick(w[0], w[1], w[2], w[3], -7710160);
      }

      for (float[] b : this.bullets) {
         this.circle(b[0], b[1], 3.0F, b[4] == 1.0F ? -16749385 : -1900533);
      }

      this.tank(this.px, this.py, this.pa, (float)Math.atan2(this.bmy() - this.py, this.bmx() - this.px), -16749385);
      this.tank(this.ax, this.ay, this.aa, (float)Math.atan2(this.py - this.ay, this.px - this.ax), -1900533);

      for (int k = 0; k < 3; k++) {
         this.circle(14 + k * 12, 12.0F, 4.0F, k < this.php ? -16749385 : 1426063360);
         this.circle(386.0F - k * 12, 12.0F, 4.0F, k < this.ahp ? -1900533 : 1426063360);
      }
   }
}
