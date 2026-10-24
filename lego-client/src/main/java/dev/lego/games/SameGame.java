package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class SameGame extends GameView {
   private static final int COLS = 14;
   private static final int ROWS = 10;
   private static final float C = 21.0F;
   private static final float OX = 6.0F;
   private static final float OY = 8.0F;
   private int[][] g = new int[14][10];
   private int hover = 0;

   SameGame() {
      super("samegame");
   }

   @Override
   protected float boardW() {
      return 306.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String overTitle() {
      return "Keine Gruppen mehr";
   }

   @Override
   protected void reset() {
      for (int c = 0; c < 14; c++) {
         for (int r = 0; r < 10; r++) {
            this.g[c][r] = this.rnd.nextInt(4);
         }
      }
   }

   private List<int[]> group(int c, int r) {
      List<int[]> out = new ArrayList<>();
      if (c >= 0 && r >= 0 && c < 14 && r < 10 && this.g[c][r] >= 0) {
         int col = this.g[c][r];
         boolean[][] seen = new boolean[14][10];
         ArrayDeque<int[]> q = new ArrayDeque<>();
         q.add(new int[]{c, r});
         seen[c][r] = true;

         while (!q.isEmpty()) {
            int[] p = q.poll();
            out.add(p);

            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
               int a = p[0] + d[0];
               int b = p[1] + d[1];
               if (a >= 0 && b >= 0 && a < 14 && b < 10 && !seen[a][b] && this.g[a][b] == col) {
                  seen[a][b] = true;
                  q.add(new int[]{a, b});
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private int[] at(float x, float y) {
      int c = (int)((x - 6.0F) / 21.0F);
      int r = (int)((218.0F - y) / 21.0F);
      return new int[]{c, r};
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int[] p = this.at(x, y);
      List<int[]> gr = this.group(p[0], p[1]);
      if (gr.size() >= 2) {
         for (int[] q : gr) {
            this.g[q[0]][q[1]] = -1;
         }

         this.score = this.score + (long)(gr.size() - 1) * (gr.size() - 1);
         Sound.play("minecraft:entity.item.pickup", 0.8F + Math.min(1.0F, gr.size() / 15.0F), 0.4F);

         for (int c = 0; c < 14; c++) {
            int w = 0;

            for (int r = 0; r < 10; r++) {
               if (this.g[c][r] >= 0) {
                  this.g[c][w] = this.g[c][r];
                  if (w != r) {
                     this.g[c][r] = -1;
                  }

                  w++;
               }
            }
         }

         int w = 0;

         for (int c = 0; c < 14; c++) {
            if (this.g[c][0] >= 0) {
               if (w != c) {
                  this.g[w] = this.g[c];
                  this.g[c] = new int[10];
                  Arrays.fill(this.g[c], -1);
               }

               w++;
            }
         }

         boolean any = false;
         boolean empty = true;

         for (int cx = 0; cx < 14; cx++) {
            for (int rx = 0; rx < 10; rx++) {
               if (this.g[cx][rx] >= 0) {
                  empty = false;
               }

               if (this.group(cx, rx).size() >= 2) {
                  any = true;
               }
            }
         }

         if (empty) {
            this.score += 1000L;
         }

         if (!any) {
            PackPuzzle.win();
            this.gameOver();
         }
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      int[] p = this.at(this.bmx(), this.bmy());
      List<int[]> hg = this.group(p[0], p[1]);
      Set<Long> hs = new HashSet<>();
      if (hg.size() >= 2) {
         for (int[] q : hg) {
            hs.add(q[0] * 100L + q[1]);
         }
      }

      for (int c = 0; c < 14; c++) {
         for (int r = 0; r < 10; r++) {
            if (this.g[c][r] >= 0) {
               int col = PackPuzzle.PAL[this.g[c][r]];
               if (hs.contains(c * 100L + r)) {
                  col = Gx.mix(col, -1, 0.3F);
               }

               this.brick(6.0F + c * 21.0F + 0.5F, 8.0F + (9 - r) * 21.0F + 0.5F, 20.0F, 20.0F, col);
            }
         }
      }

      this.text(
         hg.size() >= 2 ? hg.size() + " Steine = +" + (hg.size() - 1) * (hg.size() - 1) : "Klick auf Gruppen ab 2 gleichen Steinen",
         153.0F,
         230.0F,
         7.0F,
         3,
         Style.sub
      );
   }
}
