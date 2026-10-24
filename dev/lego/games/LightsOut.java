package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.Arrays;

final class LightsOut extends GameView {
   private static final int N = 5;
   private static final float C = 50.0F;
   private static final float OX = 20.0F;
   private static final float OY = 20.0F;
   private final boolean[][] on = new boolean[5][5];
   private int moves;
   private float doneT = -1.0F;

   LightsOut() {
      super("lightsout");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Züge";
   }

   @Override
   protected String overTitle() {
      return "Alle Lichter aus!";
   }

   @Override
   protected void reset() {
      for (boolean[] r : this.on) {
         Arrays.fill(r, false);
      }

      int k = 8 + this.rnd.nextInt(8);

      for (int i = 0; i < k; i++) {
         this.flip(this.rnd.nextInt(5), this.rnd.nextInt(5));
      }

      if (this.lit() == 0) {
         this.flip(2, 2);
      }

      this.moves = 0;
      this.doneT = -1.0F;
   }

   private void flip(int r, int c) {
      int[][] d = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

      for (int[] q : d) {
         int rr = r + q[0];
         int cc = c + q[1];
         if (rr >= 0 && cc >= 0 && rr < 5 && cc < 5) {
            this.on[rr][cc] = !this.on[rr][cc];
         }
      }
   }

   private int lit() {
      int n = 0;

      for (boolean[] r : this.on) {
         for (boolean b : r) {
            if (b) {
               n++;
            }
         }
      }

      return n;
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 0.8F) {
         this.score = this.moves;
         this.gameOver();
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!(this.doneT >= 0.0F)) {
         int c = (int)((x - 20.0F) / 50.0F);
         int r = (int)((y - 20.0F) / 50.0F);
         if (!(x < 20.0F) && !(y < 20.0F) && c < 5 && r < 5) {
            this.flip(r, c);
            this.moves++;
            this.score = this.moves;
            PackPuzzle.click();
            if (this.lit() == 0) {
               this.doneT = 0.0F;
               PackPuzzle.win();
            }
         }
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 5; r++) {
         for (int c = 0; c < 5; c++) {
            float x = 20.0F + c * 50.0F;
            float y = 20.0F + r * 50.0F;
            boolean l = this.on[r][c];
            if (l) {
               Gx.glow(this.X(x + 25.0F), this.Y(y + 25.0F), this.S(34.0F), 1442832717);
            }

            this.box(x + 3.0F, y + 3.0F, 44.0F, 44.0F, 9.0F, l ? -7859 : (Style.light ? -3091236 : -14539474));
            if (l) {
               this.circle(x + 25.0F, y + 25.0F, 7.0F, -2368);
            }
         }
      }

      float px = 300.0F;
      this.text("Lichter an", px + 40.0F, 40.0F, 7.0F, 2, Style.sub);
      this.text(String.valueOf(this.lit()), px + 40.0F, 62.0F, 18.0F, 3, Style.text);
      this.text("Züge", px + 40.0F, 100.0F, 7.0F, 2, Style.sub);
      this.text(String.valueOf(this.moves), px + 40.0F, 122.0F, 18.0F, 3, Style.text);
      this.text("Klick schaltet das", px + 40.0F, 180.0F, 6.4F, 1, Style.sub);
      this.text("Feld + Nachbarn um", px + 40.0F, 192.0F, 6.4F, 1, Style.sub);
   }
}
