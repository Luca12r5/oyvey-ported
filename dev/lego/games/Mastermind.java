package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.Arrays;

final class Mastermind extends GameView {
   private static final int ROWS = 10;
   private static final int PEGS = 4;
   private final int[] code = new int[4];
   private final int[][] guess = new int[10][4];
   private final int[][] fb = new int[10][2];
   private int row;
   private int slot;
   private int result;

   Mastermind() {
      super("mastermind");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String overTitle() {
      return this.result == 1 ? "Code geknackt!" : "Leider nicht";
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 4; i++) {
         this.code[i] = this.rnd.nextInt(6);
      }

      for (int[] g : this.guess) {
         Arrays.fill(g, -1);
      }

      for (int[] f : this.fb) {
         Arrays.fill(f, 0);
      }

      this.row = 0;
      this.slot = 0;
      this.result = 0;
   }

   @Override
   protected void update(float dt) {
   }

   private void check() {
      for (int v : this.guess[this.row]) {
         if (v < 0) {
            return;
         }
      }

      int black = 0;
      int white = 0;
      int[] cc = new int[6];
      int[] gc = new int[6];

      for (int i = 0; i < 4; i++) {
         if (this.guess[this.row][i] == this.code[i]) {
            black++;
         } else {
            cc[this.code[i]]++;
            gc[this.guess[this.row][i]]++;
         }
      }

      for (int c = 0; c < 6; c++) {
         white += Math.min(cc[c], gc[c]);
      }

      this.fb[this.row][0] = black;
      this.fb[this.row][1] = white;
      if (black == 4) {
         this.result = 1;
         this.score = (10 - this.row) * 10;
         PackPuzzle.win();
         this.row++;
         this.gameOver();
      } else {
         this.row++;
         this.slot = 0;
         if (this.row >= 10) {
            this.result = 2;
            this.score = 0L;
            PackPuzzle.bad();
            this.gameOver();
         } else {
            PackPuzzle.click();
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.row < 10 && this.result == 0) {
         for (int c = 0; c < 6; c++) {
            if (Math.hypot(x - (300 + c % 2 * 40), y - (60 + c / 2 * 40)) < 15.0) {
               this.guess[this.row][this.slot] = c;
               this.slot = Math.min(3, this.slot + 1);

               for (int i = 0; i < 4; i++) {
                  if (this.guess[this.row][i] < 0) {
                     this.slot = i;
                     break;
                  }
               }

               PackPuzzle.click();
               return;
            }
         }

         for (int ix = 0; ix < 4; ix++) {
            if (Math.hypot(x - (40 + ix * 26), y - (20 + this.row * 27)) < 12.0) {
               if (b == 1) {
                  this.guess[this.row][ix] = -1;
               }

               this.slot = ix;
               return;
            }
         }

         if (in(x, y, 280, 190, 80, 26)) {
            this.check();
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 257) {
         this.check();
      }

      if (k >= 49 && k <= 54) {
         this.guess[this.row][this.slot] = k - 49;
         this.slot = Math.min(3, this.slot + 1);
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 10; r++) {
         float y = 20 + r * 27;
         if (r == this.row && this.result == 0) {
            this.box(20.0F, y - 12.0F, 220.0F, 24.0F, 8.0F, Gx.withAlpha(Style.accent, 0.14F));
         }

         for (int i = 0; i < 4; i++) {
            int v = this.guess[r][i];
            this.circle(40 + i * 26, y, 10.0F, v < 0 ? (Style.light ? 570425344 : 587202559) : PackPuzzle.PAL[v]);
            if (r == this.row && i == this.slot && this.result == 0) {
               this.ring(40 + i * 26, y, 11.5F, 1.2F, Style.text);
            }
         }

         for (int ix = 0; ix < 4; ix++) {
            int col = ix < this.fb[r][0] ? -15658735 : (ix < this.fb[r][0] + this.fb[r][1] ? -1 : (Style.light ? 402653184 : 419430399));
            this.circle(160 + ix % 2 * 10, y - 4.0F + ix / 2 * 9, 3.5F, col);
            if (col == -1 || col == -15658735) {
               this.ring(160 + ix % 2 * 10, y - 4.0F + ix / 2 * 9, 3.5F, 0.6F, 1720223880);
            }
         }
      }

      for (int c = 0; c < 6; c++) {
         this.circle(300 + c % 2 * 40, 60 + c / 2 * 40, 14.0F, PackPuzzle.PAL[c]);
      }

      this.text("Farbe wählen (1-6)", 320.0F, 30.0F, 6.4F, 2, Style.sub);
      this.button("Prüfen", 280.0F, 190.0F, 80.0F, 26.0F, Style.accent);
      this.text("schwarz = richtig", 320.0F, 238.0F, 6.0F, 1, Style.sub);
      this.text("weiß = falscher Platz", 320.0F, 250.0F, 6.0F, 1, Style.sub);
      if (this.result != 0) {
         for (int ixx = 0; ixx < 4; ixx++) {
            this.circle(282 + ixx * 24, 280.0F, 9.0F, PackPuzzle.PAL[this.code[ixx]]);
         }
      }
   }
}
