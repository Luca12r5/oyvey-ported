package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayDeque;

final class LightCycles extends GameView {
   private static final int GW = 64;
   private static final int GH = 42;
   private static final float C = 6.25F;
   private int[][] g;
   private int px;
   private int py;
   private int pdx;
   private int pdy;
   private int ax;
   private int ay;
   private int adx;
   private int ady;
   private int winner;
   private int wins;
   private float step;
   private final ArrayDeque<int[]> q = new ArrayDeque<>();

   LightCycles() {
      super("lightcycles");
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
      return 262.5F;
   }

   @Override
   protected String scoreLabel() {
      return "Siege";
   }

   @Override
   protected String overTitle() {
      return this.winner == 1 ? "Gewonnen!" : (this.winner == 2 ? "Verloren" : "Beide gecrasht");
   }

   @Override
   protected void reset() {
      this.g = new int[64][42];
      this.px = 10;
      this.py = 21;
      this.pdx = 1;
      this.pdy = 0;
      this.ax = 53;
      this.ay = 21;
      this.adx = -1;
      this.ady = 0;
      this.winner = 0;
      this.step = 0.0F;
      this.q.clear();
      this.score = this.wins;
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

   private boolean free(int x, int y) {
      return x >= 0 && y >= 0 && x < 64 && y < 42 && this.g[x][y] == 0;
   }

   private int space(int x, int y, int limit) {
      if (!this.free(x, y)) {
         return 0;
      } else {
         boolean[][] seen = new boolean[64][42];
         ArrayDeque<int[]> f = new ArrayDeque<>();
         f.add(new int[]{x, y});
         seen[x][y] = true;
         int n = 0;

         while (!f.isEmpty() && n < limit) {
            int[] p = f.poll();
            n++;

            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
               int a = p[0] + d[0];
               int b = p[1] + d[1];
               if (this.free(a, b) && !seen[a][b]) {
                  seen[a][b] = true;
                  f.add(new int[]{a, b});
               }
            }
         }

         return n;
      }
   }

   private void aiTurn() {
      int lv = this.aiLevel();
      int[][] dirs = new int[][]{{this.adx, this.ady}, {this.ady, -this.adx}, {-this.ady, this.adx}};
      int[] best = dirs[0];
      int bs = -1;

      for (int[] d : dirs) {
         int s = lv == 0
            ? (this.free(this.ax + d[0], this.ay + d[1]) ? 1 + this.rnd.nextInt(3) : 0)
            : this.space(this.ax + d[0], this.ay + d[1], lv == 1 ? 40 : (lv == 2 ? 300 : 1200));
         if (lv >= 2 && s > 0) {
            s += d == dirs[0] ? 2 : 0;
         }

         if (lv == 3 && s > 0) {
            s -= (Math.abs(this.ax + d[0] - this.px) + Math.abs(this.ay + d[1] - this.py)) / 4;
         }

         if (s > bs) {
            bs = s;
            best = d;
         }
      }

      this.adx = best[0];
      this.ady = best[1];
   }

   @Override
   protected void update(float dt) {
      this.step += dt;
      if (!(this.step < 0.055F)) {
         this.step = 0.0F;
         if (!this.q.isEmpty()) {
            int[] d = this.q.poll();
            if (d[0] != -this.pdx || d[1] != -this.pdy) {
               this.pdx = d[0];
               this.pdy = d[1];
            }
         }

         this.aiTurn();
         this.g[this.px][this.py] = 1;
         this.g[this.ax][this.ay] = 2;
         this.px = this.px + this.pdx;
         this.py = this.py + this.pdy;
         this.ax = this.ax + this.adx;
         this.ay = this.ay + this.ady;
         boolean pd = !this.free(this.px, this.py) || this.px == this.ax && this.py == this.ay;
         boolean ad = !this.free(this.ax, this.ay) || this.px == this.ax && this.py == this.ay;
         if (pd || ad) {
            this.winner = pd && ad ? 3 : (pd ? 2 : 1);
            if (this.winner == 1) {
               this.wins++;
               this.score = this.wins;
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.3F);
            } else {
               PackArcade.boom();
            }

            this.gameOver();
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 400.0F, 262.5F, 0.0F, -16447729);

      for (int x = 0; x < 64; x += 4) {
         this.box(x * 6.25F, 0.0F, 0.6F, 262.5F, 0.0F, 570467290);
      }

      for (int y = 0; y < 42; y += 4) {
         this.box(0.0F, y * 6.25F, 400.0F, 0.6F, 0.0F, 570467290);
      }

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 42; y++) {
            if (this.g[x][y] != 0) {
               this.box(x * 6.25F, y * 6.25F, 6.25F, 6.25F, 0.0F, this.g[x][y] == 1 ? -12920577 : -34262);
            }
         }
      }

      this.box(this.px * 6.25F - 1.0F, this.py * 6.25F - 1.0F, 8.25F, 8.25F, 2.0F, -1);
      this.box(this.ax * 6.25F - 1.0F, this.ay * 6.25F - 1.0F, 8.25F, 8.25F, 2.0F, -8016);
   }
}
