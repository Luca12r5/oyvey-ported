package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Snake extends GameView {
   private static final int C = 22;
   private static final int R = 16;
   private static final float CELL = 16.0F;
   private final ArrayDeque<int[]> body = new ArrayDeque<>();
   private final ArrayDeque<int[]> queue = new ArrayDeque<>();
   private int dx = 1;
   private int dy = 0;
   private int[] food;
   private float step = 0.0F;
   private float speed = 0.12F;
   private final List<float[]> pops = new ArrayList<>();

   public Snake() {
      super("snake");
   }

   @Override
   protected float boardW() {
      return 352.0F;
   }

   @Override
   protected float boardH() {
      return 256.0F;
   }

   @Override
   protected void reset() {
      this.body.clear();
      this.queue.clear();

      for (int i = 0; i < 4; i++) {
         this.body.addFirst(new int[]{4 + i, 8});
      }

      this.dx = 1;
      this.dy = 0;
      this.speed = 0.12F;
      this.step = 0.0F;
      this.pops.clear();
      this.placeFood();
   }

   private void placeFood() {
      int[] f;
      boolean hit;
      do {
         f = new int[]{this.rnd.nextInt(22), this.rnd.nextInt(16)};
         hit = false;

         for (int[] b : this.body) {
            if (b[0] == f[0] && b[1] == f[1]) {
               hit = true;
               break;
            }
         }
      } while (hit);

      this.food = f;
   }

   @Override
   protected void onKey(int k) {
      int[] d = null;
      if (k == 263 || k == 65) {
         d = new int[]{-1, 0};
      }

      if (k == 262 || k == 68) {
         d = new int[]{1, 0};
      }

      if (k == 265 || k == 87) {
         d = new int[]{0, -1};
      }

      if (k == 264 || k == 83) {
         d = new int[]{0, 1};
      }

      if (d != null && this.queue.size() <= 2) {
         int[] last = this.queue.isEmpty() ? new int[]{this.dx, this.dy} : this.queue.peekLast();
         if (d[0] != -last[0] || d[1] != -last[1]) {
            if (d[0] != last[0] || d[1] != last[1]) {
               this.queue.add(d);
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      for (float[] p : this.pops) {
         p[2] += dt;
      }

      this.pops.removeIf(p -> p[2] > 0.5F);
      this.step += dt;
      if (!(this.step < this.speed)) {
         this.step = this.step - this.speed;
         if (!this.queue.isEmpty()) {
            int[] d = this.queue.poll();
            this.dx = d[0];
            this.dy = d[1];
         }

         int[] head = this.body.peekFirst();
         int nx = head[0] + this.dx;
         int ny = head[1] + this.dy;
         if (nx >= 0 && ny >= 0 && nx < 22 && ny < 16) {
            boolean grow = nx == this.food[0] && ny == this.food[1];
            int i = 0;
            int n = this.body.size();

            for (int[] b : this.body) {
               if (!grow && i == n - 1) {
                  break;
               }

               if (b[0] == nx && b[1] == ny) {
                  this.gameOver();
                  return;
               }

               i++;
            }

            this.body.addFirst(new int[]{nx, ny});
            if (grow) {
               this.score += 10L;
               this.pops.add(new float[]{this.food[0], this.food[1], 0.0F});
               this.speed = Math.max(0.055F, this.speed * 0.965F);
               Sound.play("minecraft:entity.experience_orb.pickup", 1.2F + this.rnd.nextFloat() * 0.3F, 0.25F);
               this.placeFood();
            } else {
               this.body.removeLast();
            }
         } else {
            this.gameOver();
         }
      }
   }

   @Override
   protected void render() {
      for (int y = 0; y < 16; y++) {
         for (int x = 0; x < 22; x++) {
            if ((x + y) % 2 == 0) {
               this.box(x * 16.0F, y * 16.0F, 16.0F, 16.0F, 0.0F, Style.light ? 167772160 : 150994943);
            }
         }
      }

      float pulse = 1.0F + 0.08F * (float)Math.sin(this.time * 8.0F);
      float fs = 12.8F * pulse;
      this.brick(this.food[0] * 16.0F + (16.0F - fs) / 2.0F, this.food[1] * 16.0F + (16.0F - fs) / 2.0F, fs, fs, -13053);
      int n = this.body.size();
      int i = 0;
      if (this.state == GameView.State.PLAYING) {
         Math.min(1.0F, this.step / this.speed);
      } else {
         float var10000 = 1.0F;
      }

      for (int[] b : this.body) {
         float t = (float)i / Math.max(1, n - 1);
         int col = Gx.mix(Style.accent, Gx.mix(Style.accent, -16777216, 0.35F), t);
         float pad = i == 0 ? 0.5F : 1.2F + t * 1.2F;
         this.brick(b[0] * 16.0F + pad, b[1] * 16.0F + pad, 16.0F - pad * 2.0F, 16.0F - pad * 2.0F, col);
         if (i == 0) {
            float ex = b[0] * 16.0F + 8.0F + this.dx * 3;
            float ey = b[1] * 16.0F + 8.0F + this.dy * 3;
            float ox = this.dy != 0 ? 3.2F : 0.0F;
            float oy = this.dx != 0 ? 3.2F : 0.0F;
            this.circle(ex + ox, ey + oy, 1.8F, -1);
            this.circle(ex - ox, ey - oy, 1.8F, -1);
            this.circle(ex + ox + this.dx * 0.6F, ey + oy + this.dy * 0.6F, 0.9F, -15658735);
            this.circle(ex - ox + this.dx * 0.6F, ey - oy + this.dy * 0.6F, 0.9F, -15658735);
         }

         i++;
      }

      for (float[] p : this.pops) {
         float a = 1.0F - p[2] / 0.5F;
         this.text("+10", p[0] * 16.0F + 8.0F, p[1] * 16.0F - p[2] * 30.0F, 8.0F, 3, Gx.withAlpha(-13053, a));
      }
   }
}
