package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayDeque;

final class FloodIt extends GameView {
   private static final int N = 12;
   private static final int MAX = 22;
   private static final float C = 20.0F;
   private static final float OX = 12.0F;
   private static final float OY = 12.0F;
   private int[][] g = new int[12][12];
   private int moves;

   FloodIt() {
      super("floodit");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 264.0F;
   }

   @Override
   protected String overTitle() {
      return this.done() ? "Alles eine Farbe!" : "Keine Züge mehr";
   }

   @Override
   protected void reset() {
      for (int[] r : this.g) {
         for (int c = 0; c < 12; c++) {
            r[c] = this.rnd.nextInt(6);
         }
      }

      this.moves = 0;
   }

   private boolean done() {
      int v = this.g[0][0];

      for (int[] r : this.g) {
         for (int x : r) {
            if (x != v) {
               return false;
            }
         }
      }

      return true;
   }

   private void pick(int col) {
      int old = this.g[0][0];
      if (col != old) {
         ArrayDeque<int[]> q = new ArrayDeque<>();
         boolean[][] seen = new boolean[12][12];
         q.add(new int[]{0, 0});
         seen[0][0] = true;

         while (!q.isEmpty()) {
            int[] p = q.poll();
            this.g[p[0]][p[1]] = col;

            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
               int r = p[0] + d[0];
               int c = p[1] + d[1];
               if (r >= 0 && c >= 0 && r < 12 && c < 12 && !seen[r][c] && this.g[r][c] == old) {
                  seen[r][c] = true;
                  q.add(new int[]{r, c});
               }
            }
         }

         this.moves++;
         PackPuzzle.click();
         if (this.done()) {
            this.score = (22 - this.moves + 1) * 10;
            PackPuzzle.win();
            this.gameOver();
         } else if (this.moves >= 22) {
            this.score = 0L;
            PackPuzzle.bad();
            this.gameOver();
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < 6; i++) {
         if (Math.hypot(x - (300 + i % 2 * 44), y - (60 + i / 2 * 44)) < 18.0) {
            this.pick(i);
         }
      }

      int c = (int)((x - 12.0F) / 20.0F);
      int r = (int)((y - 12.0F) / 20.0F);
      if (x >= 12.0F && y >= 12.0F && c < 12 && r < 12) {
         this.pick(this.g[r][c]);
      }
   }

   @Override
   protected void onKey(int k) {
      if (k >= 49 && k <= 54) {
         this.pick(k - 49);
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      for (int r = 0; r < 12; r++) {
         for (int c = 0; c < 12; c++) {
            this.box(12.0F + c * 20.0F, 12.0F + r * 20.0F, 20.5F, 20.5F, 0.0F, PackPuzzle.PAL[this.g[r][c]]);
         }
      }

      for (int i = 0; i < 6; i++) {
         this.circle(300 + i % 2 * 44, 60 + i / 2 * 44, 17.0F, PackPuzzle.PAL[i]);
         this.text(String.valueOf(i + 1), 300 + i % 2 * 44, 61 + i / 2 * 44, 8.0F, 3, -855638017);
      }

      this.text("Züge: " + this.moves + " / 22", 322.0F, 210.0F, 9.0F, 3, this.moves > 18 ? Style.danger : Style.text);
      this.text("Färbe alles in eine Farbe", 322.0F, 236.0F, 6.2F, 1, Style.sub);
   }
}
