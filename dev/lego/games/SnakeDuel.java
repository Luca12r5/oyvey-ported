package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;

final class SnakeDuel extends GameView {
   private static final int GW = 30;
   private static final int GH = 20;
   private static final float C = 13.0F;
   private final ArrayDeque<int[]> me = new ArrayDeque<>();
   private final ArrayDeque<int[]> ai = new ArrayDeque<>();
   private int dx;
   private int dy;
   private int adx;
   private int ady;
   private int winner;
   private int[] food;
   private float step;
   private final ArrayDeque<int[]> q = new ArrayDeque<>();

   SnakeDuel() {
      super("snakeduel");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 390.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Länge";
   }

   @Override
   protected String overTitle() {
      return this.winner == 1 ? "Die KI-Schlange ist gecrasht!" : (this.winner == 2 ? "Gecrasht!" : "Beide gecrasht");
   }

   @Override
   protected void reset() {
      this.me.clear();
      this.ai.clear();
      this.q.clear();

      for (int i = 0; i < 4; i++) {
         this.me.addFirst(new int[]{3 + i, 5});
         this.ai.addFirst(new int[]{26 - i, 14});
      }

      this.dx = 1;
      this.dy = 0;
      this.adx = -1;
      this.ady = 0;
      this.winner = 0;
      this.step = 0.0F;
      this.placeFood();
   }

   private boolean occupied(int x, int y) {
      for (int[] p : this.me) {
         if (p[0] == x && p[1] == y) {
            return true;
         }
      }

      for (int[] px : this.ai) {
         if (px[0] == x && px[1] == y) {
            return true;
         }
      }

      return false;
   }

   private void placeFood() {
      do {
         this.food = new int[]{this.rnd.nextInt(30), this.rnd.nextInt(20)};
      } while (this.occupied(this.food[0], this.food[1]));
   }

   private boolean free(int x, int y) {
      return x >= 0 && y >= 0 && x < 30 && y < 20 && !this.occupied(x, y);
   }

   @Override
   protected void onKey(int k) {
      int[] d = k == 263 || k == 65
         ? new int[]{-1, 0}
         : (k == 262 || k == 68 ? new int[]{1, 0} : (k != 265 && k != 87 ? (k != 264 && k != 83 ? null : new int[]{0, 1}) : new int[]{0, -1}));
      if (d != null && this.q.size() < 3) {
         this.q.add(d);
      }
   }

   private void aiDir() {
      int[] h = this.ai.peekFirst();
      int[][] dirs = new int[][]{{this.adx, this.ady}, {this.ady, -this.adx}, {-this.ady, this.adx}};
      int lv = this.aiLevel();
      int[] best = dirs[0];
      double bs = -1.0E9;

      for (int[] d : dirs) {
         int nx = h[0] + d[0];
         int ny = h[1] + d[1];
         if (this.free(nx, ny)) {
            double s = -(Math.abs(nx - this.food[0]) + Math.abs(ny - this.food[1]));
            if (lv >= 2) {
               int open = 0;

               for (int[] e : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                  if (this.free(nx + e[0], ny + e[1])) {
                     open++;
                  }
               }

               s += open * 2;
            }

            if (lv == 0) {
               s += this.rnd.nextInt(12);
            }

            if (s > bs) {
               bs = s;
               best = d;
            }
         }
      }

      this.adx = best[0];
      this.ady = best[1];
   }

   @Override
   protected void update(float dt) {
      this.step += dt;
      if (!(this.step < 0.11F)) {
         this.step = 0.0F;
         if (!this.q.isEmpty()) {
            int[] d = this.q.poll();
            if (d[0] != -this.dx || d[1] != -this.dy) {
               this.dx = d[0];
               this.dy = d[1];
            }
         }

         this.aiDir();
         int[] h = this.me.peekFirst();
         int[] a = this.ai.peekFirst();
         int nx = h[0] + this.dx;
         int ny = h[1] + this.dy;
         int ax = a[0] + this.adx;
         int ay = a[1] + this.ady;
         boolean pDead = !this.free(nx, ny) || nx == ax && ny == ay;
         boolean aDead = !this.free(ax, ay) || nx == ax && ny == ay;
         if (!pDead && !aDead) {
            this.me.addFirst(new int[]{nx, ny});
            this.ai.addFirst(new int[]{ax, ay});
            boolean ate = false;
            if (nx == this.food[0] && ny == this.food[1]) {
               ate = true;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
            } else {
               this.me.removeLast();
            }

            if (ax == this.food[0] && ay == this.food[1]) {
               ate = true;
            } else {
               this.ai.removeLast();
            }

            if (ate) {
               this.placeFood();
            }

            this.score = this.me.size();
         } else {
            this.winner = pDead && aDead ? 3 : (pDead ? 2 : 1);
            if (this.winner == 1) {
               PackPuzzle.win();
            } else {
               PackArcade.boom();
            }

            this.score = this.me.size();
            this.gameOver();
         }
      }
   }

   @Override
   protected void render() {
      for (int y = 0; y < 20; y++) {
         for (int x = 0; x < 30; x++) {
            if ((x + y) % 2 == 0) {
               this.box(x * 13.0F, y * 13.0F, 13.0F, 13.0F, 0.0F, Style.light ? 167772160 : 150994943);
            }
         }
      }

      this.brick(this.food[0] * 13.0F + 2.0F, this.food[1] * 13.0F + 2.0F, 9.0F, 9.0F, -13053);
      int i = 0;

      for (int[] p : this.me) {
         this.brick(p[0] * 13.0F + 1.0F, p[1] * 13.0F + 1.0F, 11.0F, 11.0F, i++ == 0 ? -12604929 : -16749385);
      }

      i = 0;

      for (int[] p : this.ai) {
         this.brick(p[0] * 13.0F + 1.0F, p[1] * 13.0F + 1.0F, 11.0F, 11.0F, i++ == 0 ? -38294 : -1900533);
      }

      this.text("Du " + this.me.size() + " · KI " + this.ai.size(), 195.0F, 8.0F, 6.5F, 3, Style.sub);
   }
}
