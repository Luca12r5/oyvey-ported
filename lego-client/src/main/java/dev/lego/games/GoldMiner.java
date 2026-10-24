package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class GoldMiner extends GameView {
   private static final float W = 400.0F;
   private static final float H = 280.0F;
   private static final float OX = 200.0F;
   private static final float OY = 40.0F;
   private final List<float[]> items = new ArrayList<>();
   private float ang;
   private float len;
   private float hookState;
   private float grabbed = -1.0F;
   private float left;
   private int dir = 1;
   private int level;
   private int goal;

   GoldMiner() {
      super("goldminer");
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
   protected String overTitle() {
      return this.score >= this.goal ? "Ziel geschafft!" : "Ziel verfehlt (" + this.goal + ")";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.score = 0L;
      this.nextLevel();
   }

   private void nextLevel() {
      this.level++;
      this.goal = (int)this.score + 300 + this.level * 150;
      this.left = 45.0F;
      this.items.clear();

      for (int i = 0; i < 10 + this.level; i++) {
         float r = this.rnd.nextFloat();
         float x = 30.0F + this.rnd.nextFloat() * 340.0F;
         float y = 110.0F + this.rnd.nextFloat() * 150.0F;
         if (r < 0.15F) {
            this.items.add(new float[]{x, y, 22.0F, 500.0F, 5.0F, 0.0F});
         } else if (r < 0.5F) {
            this.items.add(new float[]{x, y, 9.0F, 100.0F, 1.5F, 0.0F});
         } else if (r < 0.62F) {
            this.items.add(new float[]{x, y, 6.0F, 400.0F, 0.6F, 2.0F});
         } else {
            this.items.add(new float[]{x, y, 14.0F + this.rnd.nextFloat() * 6.0F, 20.0F, 3.5F, 1.0F});
         }
      }

      this.len = 20.0F;
      this.hookState = 0.0F;
      this.grabbed = -1.0F;
   }

   private void fire() {
      if (this.hookState == 0.0F) {
         this.hookState = 1.0F;
         Sound.play("minecraft:block.chain.place", 1.2F, 0.4F);
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.fire();
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 264 || k == 83) {
         this.fire();
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         if (this.score >= this.goal) {
            PackPuzzle.win();
            this.nextLevel();
         } else {
            this.gameOver();
         }
      } else if (this.hookState == 0.0F) {
         this.ang = this.ang + this.dir * 70 * dt;
         if (Math.abs(this.ang) > 70.0F) {
            this.dir = -this.dir;
            this.ang = Math.signum(this.ang) * 70.0F;
         }
      } else {
         float rad = (float)Math.toRadians(this.ang);
         float hx = 200.0F + (float)Math.sin(rad) * this.len;
         float hy = 40.0F + (float)Math.cos(rad) * this.len;
         if (this.hookState == 1.0F) {
            this.len += 260.0F * dt;

            for (int i = 0; i < this.items.size(); i++) {
               float[] it = this.items.get(i);
               if (Math.hypot(hx - it[0], hy - it[1]) < it[2] + 4.0F) {
                  this.grabbed = i;
                  this.hookState = 2.0F;
                  Sound.play("minecraft:block.stone.hit", 1.0F, 0.4F);
                  break;
               }
            }

            if (hx < 0.0F || hx > 400.0F || hy > 280.0F) {
               this.hookState = 2.0F;
            }
         } else {
            float w = this.grabbed >= 0.0F ? this.items.get((int)this.grabbed)[4] : 0.5F;
            this.len -= 260.0F / (1.0F + w) * dt;
            if (this.grabbed >= 0.0F) {
               float[] it = this.items.get((int)this.grabbed);
               it[0] = hx;
               it[1] = hy + it[2];
            }

            if (this.len <= 20.0F) {
               this.len = 20.0F;
               this.hookState = 0.0F;
               if (this.grabbed >= 0.0F) {
                  float[] it = this.items.remove((int)this.grabbed);
                  this.score = this.score + (long)it[3];
                  Sound.play(it[3] >= 400.0F ? "minecraft:entity.player.levelup" : "minecraft:entity.experience_orb.pickup", 1.2F, 0.4F);
                  this.grabbed = -1.0F;
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 50.0F, 0.0F, -12150, -20384);
      this.boxV(0.0F, 50.0F, 400.0F, 230.0F, 0.0F, -7710160, -11916780);

      for (float[] it : this.items) {
         int col = it[5] == 0.0F ? -11713 : (it[5] == 2.0F ? -10817304 : -7697774);
         if (it[5] == 2.0F) {
            this.circle(it[0], it[1], it[2], col);
            this.circle(it[0] - 2.0F, it[1] - 2.0F, 2.0F, -1);
         } else {
            this.circle(it[0], it[1], it[2], col);
            this.circle(it[0] - it[2] * 0.3F, it[1] - it[2] * 0.3F, it[2] * 0.3F, it[5] == 0.0F ? -3936 : -5197640);
         }
      }

      float rad = (float)Math.toRadians(this.ang);
      float hx = 200.0F + (float)Math.sin(rad) * this.len;
      float hy = 40.0F + (float)Math.cos(rad) * this.len;
      this.line(200.0F, 40.0F, hx, hy, 1.0F, -15000798);
      this.line(hx - 6.0F, hy, hx + 6.0F, hy, 2.0F, -10853776);
      this.line(hx - 6.0F, hy, hx - 6.0F, hy + 6.0F, 2.0F, -10853776);
      this.line(hx + 6.0F, hy, hx + 6.0F, hy + 6.0F, 2.0F, -10853776);
      this.box(188.0F, 16.0F, 24.0F, 20.0F, 5.0F, -1900533);
      this.circle(200.0F, 10.0F, 7.0F, -12128);
      this.textL("Geld " + this.score + " / Ziel " + this.goal, 8.0F, 12.0F, 7.5F, 3, -12966892);
      this.textL(String.format(Locale.ROOT, "Level %d · %.0f s", this.level, this.left), 300.0F, 12.0F, 7.5F, 3, -12966892);
   }
}
