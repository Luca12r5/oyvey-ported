package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class BubbleShooter extends GameView {
   private static final int COLS = 11;
   private static final int ROWS = 14;
   private static final float R = 13.0F;
   private static final float W = 299.0F;
   private static final float H = 300.0F;
   private static final float LOSE = 260.0F;
   private int[][] g = new int[14][11];
   private float sx;
   private float sy;
   private float svx;
   private float svy;
   private int cur;
   private int next;
   private int shots;
   private boolean flying;
   private final List<float[]> falling = new ArrayList<>();

   BubbleShooter() {
      super("bubbles");
   }

   @Override
   protected float boardW() {
      return 299.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String overTitle() {
      return this.clearAll() ? "Alles abgeräumt!" : "Die Blasen sind unten";
   }

   private int colors() {
      return 5;
   }

   @Override
   protected void reset() {
      for (int[] r : this.g) {
         Arrays.fill(r, -1);
      }

      for (int r = 0; r < 5; r++) {
         for (int c = 0; c < 11 - r % 2; c++) {
            this.g[r][c] = this.rnd.nextInt(this.colors());
         }
      }

      this.cur = this.rnd.nextInt(this.colors());
      this.next = this.rnd.nextInt(this.colors());
      this.flying = false;
      this.shots = 0;
      this.falling.clear();
   }

   private float cx(int r, int c) {
      return 13.0F + c * 13.0F * 2.0F + r % 2 * 13.0F;
   }

   private float cy(int r) {
      return 13.0F + r * 13.0F * 1.73F;
   }

   private boolean clearAll() {
      for (int[] row : this.g) {
         for (int v : row) {
            if (v >= 0) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.flying) {
         float dx = x - 149.5F;
         float dy = Math.min(-20.0F, y - 282.0F);
         float n = (float)Math.hypot(dx, dy);
         this.sx = 149.5F;
         this.sy = 282.0F;
         this.svx = dx / n * 420.0F;
         this.svy = dy / n * 420.0F;
         this.flying = true;
         Sound.play("minecraft:entity.chicken.egg", 1.2F, 0.3F);
      }
   }

   private List<int[]> neighbors(int r, int c) {
      List<int[]> l = new ArrayList<>();
      int[][] d = r % 2 == 0
         ? new int[][]{{0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {1, -1}, {1, 0}}
         : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {-1, 1}, {1, 0}, {1, 1}};

      for (int[] q : d) {
         int rr = r + q[0];
         int cc = c + q[1];
         if (rr >= 0 && rr < 14 && cc >= 0 && cc < 11 - rr % 2) {
            l.add(new int[]{rr, cc});
         }
      }

      return l;
   }

   private void snap() {
      int br = 0;
      int bc = 0;
      float bd = 1.0E9F;

      for (int r = 0; r < 14; r++) {
         for (int c = 0; c < 11 - r % 2; c++) {
            if (this.g[r][c] < 0) {
               boolean attached = r == 0;

               for (int[] n : this.neighbors(r, c)) {
                  if (this.g[n[0]][n[1]] >= 0) {
                     attached = true;
                  }
               }

               if (attached) {
                  float d = (float)Math.hypot(this.sx - this.cx(r, c), this.sy - this.cy(r));
                  if (d < bd) {
                     bd = d;
                     br = r;
                     bc = c;
                  }
               }
            }
         }
      }

      this.g[br][bc] = this.cur;
      this.flying = false;
      List<int[]> grp = new ArrayList<>();
      boolean[][] seen = new boolean[14][11];
      ArrayDeque<int[]> q = new ArrayDeque<>();
      q.add(new int[]{br, bc});
      seen[br][bc] = true;

      while (!q.isEmpty()) {
         int[] p = q.poll();
         grp.add(p);

         for (int[] nx : this.neighbors(p[0], p[1])) {
            if (!seen[nx[0]][nx[1]] && this.g[nx[0]][nx[1]] == this.cur) {
               seen[nx[0]][nx[1]] = true;
               q.add(nx);
            }
         }
      }

      if (grp.size() >= 3) {
         for (int[] p : grp) {
            this.falling.add(new float[]{this.cx(p[0], p[1]), this.cy(p[0]), this.g[p[0]][p[1]], 0.0F});
            this.g[p[0]][p[1]] = -1;
         }

         this.score = this.score + grp.size() * 10L;
         PackArcade.pop();
         boolean[][] keep = new boolean[14][11];
         q.clear();

         for (int cx = 0; cx < 11; cx++) {
            if (this.g[0][cx] >= 0) {
               keep[0][cx] = true;
               q.add(new int[]{0, cx});
            }
         }

         while (!q.isEmpty()) {
            int[] p = q.poll();

            for (int[] nxx : this.neighbors(p[0], p[1])) {
               if (!keep[nxx[0]][nxx[1]] && this.g[nxx[0]][nxx[1]] >= 0) {
                  keep[nxx[0]][nxx[1]] = true;
                  q.add(nxx);
               }
            }
         }

         for (int r = 0; r < 14; r++) {
            for (int cxx = 0; cxx < 11 - r % 2; cxx++) {
               if (this.g[r][cxx] >= 0 && !keep[r][cxx]) {
                  this.falling.add(new float[]{this.cx(r, cxx), this.cy(r), this.g[r][cxx], 0.0F});
                  this.g[r][cxx] = -1;
                  this.score += 20L;
               }
            }
         }
      }

      this.shots++;
      if (this.shots % 7 == 0) {
         for (int r = 13; r >= 2; r--) {
            this.g[r] = (int[])this.g[r - 2].clone();
         }

         for (int r = 0; r < 2; r++) {
            Arrays.fill(this.g[r], -1);

            for (int cxxx = 0; cxxx < 11 - r % 2; cxxx++) {
               this.g[r][cxxx] = this.rnd.nextInt(this.colors());
            }
         }
      }

      this.cur = this.next;
      this.next = this.rnd.nextInt(this.colors());
      if (this.clearAll()) {
         this.score += 500L;
         PackPuzzle.win();
         this.gameOver();
      } else {
         for (int r = 0; r < 14; r++) {
            for (int cxxx = 0; cxxx < 11; cxxx++) {
               if (this.g[r][cxxx] >= 0 && this.cy(r) > 260.0F) {
                  PackPuzzle.bad();
                  this.gameOver();
                  return;
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      for (float[] f : this.falling) {
         f[3] += 600.0F * dt;
         f[1] += f[3] * dt;
      }

      this.falling.removeIf(f -> f[1] > 320.0F);
      if (this.flying) {
         for (int s = 0; s < 4; s++) {
            this.sx = this.sx + this.svx * dt / 4.0F;
            this.sy = this.sy + this.svy * dt / 4.0F;
            if (this.sx < 13.0F) {
               this.sx = 13.0F;
               this.svx = -this.svx;
            }

            if (this.sx > 286.0F) {
               this.sx = 286.0F;
               this.svx = -this.svx;
            }

            boolean hit = this.sy < 13.0F;

            for (int r = 0; r < 14 && !hit; r++) {
               for (int c = 0; c < 11 - r % 2; c++) {
                  if (this.g[r][c] >= 0 && Math.hypot(this.sx - this.cx(r, c), this.sy - this.cy(r)) < 22.1F) {
                     hit = true;
                     break;
                  }
               }
            }

            if (hit) {
               this.snap();
               return;
            }
         }
      }
   }

   private void bubble(float x, float y, int col) {
      this.circle(x, y, 12.0F, PackPuzzle.PAL[col]);
      this.circle(x - 4.0F, y - 4.0F, 3.5F, -1996488705);
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 299.0F, 300.0F, 0.0F, -15721936, -15062448);
      this.box(0.0F, 260.0F, 299.0F, 1.0F, 0.0F, 1728002892);

      for (int r = 0; r < 14; r++) {
         for (int c = 0; c < 11 - r % 2; c++) {
            if (this.g[r][c] >= 0) {
               this.bubble(this.cx(r, c), this.cy(r), this.g[r][c]);
            }
         }
      }

      for (float[] f : this.falling) {
         this.bubble(f[0], f[1], (int)f[2]);
      }

      if (!this.flying) {
         float dx = this.bmx() - 149.5F;
         float dy = Math.min(-20.0F, this.bmy() - 282.0F);
         float n = (float)Math.hypot(dx, dy);

         for (int i = 1; i < 8; i++) {
            this.circle(149.5F + dx / n * i * 14.0F, 282.0F + dy / n * i * 14.0F, 1.5F, 1728053247);
         }
      }

      this.bubble(this.flying ? this.sx : 149.5F, this.flying ? this.sy : 282.0F, this.cur);
      this.circle(30.0F, 286.0F, 8.0F, PackPuzzle.PAL[this.next]);
      this.textL("Nächste", 42.0F, 286.0F, 6.0F, 2, -1426063361);
   }
}
