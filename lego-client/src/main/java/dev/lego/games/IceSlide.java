package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.Arrays;

final class IceSlide extends GameView {
   private static final int N = 9;
   private static final float C = 26.0F;
   private static final float OX = 10.0F;
   private static final float OY = 10.0F;
   private boolean[][] rock;
   private int pr;
   private int pc;
   private int gr;
   private int gc;
   private int moves;
   private int level;
   private int par;
   private float doneT = -1.0F;

   IceSlide() {
      super("iceslide");
   }

   @Override
   protected float boardW() {
      return 374.0F;
   }

   @Override
   protected float boardH() {
      return 254.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Level";
   }

   @Override
   protected String overTitle() {
      return "Aufgegeben";
   }

   @Override
   protected void reset() {
      this.level = 0;
      this.next();
   }

   private void next() {
      this.level++;
      this.score = this.level - 1;

      while (true) {
         this.rock = new boolean[9][9];

         for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
               if (this.rnd.nextFloat() < 0.16F) {
                  this.rock[r][c] = true;
               }
            }
         }

         this.pr = this.rnd.nextInt(9);
         this.pc = this.rnd.nextInt(9);
         this.gr = this.rnd.nextInt(9);
         this.gc = this.rnd.nextInt(9);
         if (!this.rock[this.pr][this.pc] && !this.rock[this.gr][this.gc] && (this.pr != this.gr || this.pc != this.gc)) {
            int d = this.bfs();
            if (d >= 3 + Math.min(4, this.level / 2)) {
               this.par = d;
               this.moves = 0;
               this.doneT = -1.0F;
               return;
            }
         }
      }
   }

   private int[] slide(int r, int c, int dr, int dc) {
      while (true) {
         int nr = r + dr;
         int nc = c + dc;
         if (nr >= 0 && nc >= 0 && nr < 9 && nc < 9 && !this.rock[nr][nc]) {
            r = nr;
            c = nc;
            if (nr != this.gr || nc != this.gc) {
               continue;
            }

            return new int[]{nr, nc};
         }

         return new int[]{r, c};
      }
   }

   private int bfs() {
      int[][] dist = new int[9][9];

      for (int[] d : dist) {
         Arrays.fill(d, -1);
      }

      ArrayDeque<int[]> q = new ArrayDeque<>();
      q.add(new int[]{this.pr, this.pc});
      dist[this.pr][this.pc] = 0;

      while (!q.isEmpty()) {
         int[] p = q.poll();
         if (p[0] == this.gr && p[1] == this.gc) {
            return dist[p[0]][p[1]];
         }

         for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            int[] e = this.slide(p[0], p[1], d[0], d[1]);
            if (dist[e[0]][e[1]] < 0) {
               dist[e[0]][e[1]] = dist[p[0]][p[1]] + 1;
               q.add(e);
            }
         }
      }

      return -1;
   }

   @Override
   protected void onKey(int k) {
      if (!(this.doneT >= 0.0F)) {
         int[] d = k == 265 || k == 87
            ? new int[]{-1, 0}
            : (k == 264 || k == 83 ? new int[]{1, 0} : (k != 263 && k != 65 ? (k != 262 && k != 68 ? null : new int[]{0, 1}) : new int[]{0, -1}));
         if (k == 82) {
            this.next();
            this.level--;
            this.score = this.level - 1;
         } else if (d != null) {
            int[] e = this.slide(this.pr, this.pc, d[0], d[1]);
            if (e[0] != this.pr || e[1] != this.pc) {
               this.pr = e[0];
               this.pc = e[1];
               this.moves++;
               Sound.play("minecraft:block.glass.step", 1.4F, 0.3F);
               if (this.pr == this.gr && this.pc == this.gc) {
                  this.doneT = 0.0F;
                  PackPuzzle.win();
               }
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 0.9F) {
         this.next();
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 9; r++) {
         for (int c = 0; c < 9; c++) {
            float x = 10.0F + c * 26.0F;
            float y = 10.0F + r * 26.0F;
            this.box(x, y, 26.0F, 26.0F, 0.0F, (r + c) % 2 == 0 ? -3149825 : -4200710);
            if (this.rock[r][c]) {
               this.circle(x + 13.0F, y + 13.0F, 10.92F, -8750460);
               this.circle(x + 13.0F - 3.0F, y + 13.0F - 3.0F, 3.0F, -6645084);
            }
         }
      }

      float gx = 10.0F + this.gc * 26.0F + 13.0F;
      float gy = 10.0F + this.gr * 26.0F + 13.0F;
      this.box(gx - 8.0F, gy - 8.0F, 16.0F, 16.0F, 3.0F, -12987565);
      this.text("⌂", gx, gy, 9.0F, 3, -1);
      float x = 10.0F + this.pc * 26.0F + 13.0F;
      float y = 10.0F + this.pr * 26.0F + 13.0F;
      this.circle(x, y, 9.0F, -15000798);
      this.circle(x, y + 2.0F, 6.0F, -723724);
      this.box(x - 2.0F, y - 2.0F, 4.0F, 3.0F, 1.0F, -30208);
      float sx = 264.0F;
      this.textL("Level " + this.level, sx, 30.0F, 9.0F, 3, Style.text);
      this.textL("Züge " + this.moves + " (min. " + this.par + ")", sx, 50.0F, 7.0F, 2, Style.sub);
      this.textL("Der Pinguin rutscht,", sx, 90.0F, 6.4F, 2, Style.sub);
      this.textL("bis er gegen einen", sx, 102.0F, 6.4F, 2, Style.sub);
      this.textL("Stein stößt.", sx, 114.0F, 6.4F, 2, Style.sub);
      this.textL("Pfeiltasten · R = neu", sx, 140.0F, 6.4F, 2, Style.muted);
   }
}
