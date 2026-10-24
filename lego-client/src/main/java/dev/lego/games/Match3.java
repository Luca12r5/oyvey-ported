package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.Locale;

final class Match3 extends GameView {
   private static final int N = 8;
   private static final float C = 31.0F;
   private static final float OX = 10.0F;
   private static final float OY = 10.0F;
   private int[][] g = new int[8][8];
   private final float[][] fall = new float[8][8];
   private int sr = -1;
   private int sc = -1;
   private int combo;
   private float left;
   private float settle;

   Match3() {
      super("match3");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 268.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            do {
               this.g[r][c] = this.rnd.nextInt(6);
            } while (
               (c < 2 || this.g[r][c] != this.g[r][c - 1] || this.g[r][c] != this.g[r][c - 2])
                  && (r < 2 || this.g[r][c] != this.g[r - 1][c] || this.g[r][c] != this.g[r - 2][c])
            );
         }
      }

      this.left = 90.0F;
      this.sr = -1;
      this.settle = 0.0F;
   }

   private boolean[][] matches() {
      boolean[][] m = new boolean[8][8];

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 6; c++) {
            if (this.g[r][c] >= 0 && this.g[r][c] == this.g[r][c + 1] && this.g[r][c] == this.g[r][c + 2]) {
               m[r][c] = m[r][c + 1] = m[r][c + 2] = true;
            }
         }
      }

      for (int cx = 0; cx < 8; cx++) {
         for (int r = 0; r < 6; r++) {
            if (this.g[r][cx] >= 0 && this.g[r][cx] == this.g[r + 1][cx] && this.g[r][cx] == this.g[r + 2][cx]) {
               m[r][cx] = m[r + 1][cx] = m[r + 2][cx] = true;
            }
         }
      }

      return m;
   }

   private int clear() {
      boolean[][] m = this.matches();
      int n = 0;

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            if (m[r][c]) {
               this.g[r][c] = -1;
               n++;
            }
         }
      }

      if (n == 0) {
         return 0;
      } else {
         for (int cx = 0; cx < 8; cx++) {
            int w = 7;

            for (int r = 7; r >= 0; r--) {
               if (this.g[r][cx] >= 0) {
                  if (w != r) {
                     this.g[w][cx] = this.g[r][cx];
                     this.fall[w][cx] = w - r;
                  }

                  w--;
               }
            }

            for (int rx = w; rx >= 0; rx--) {
               this.g[rx][cx] = this.rnd.nextInt(6);
               this.fall[rx][cx] = w + 2;
            }
         }

         return n;
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!(this.settle > 0.0F)) {
         int c = (int)((x - 10.0F) / 31.0F);
         int r = (int)((y - 10.0F) / 31.0F);
         if (!(x < 10.0F) && !(y < 10.0F) && c < 8 && r < 8) {
            if (this.sr < 0) {
               this.sr = r;
               this.sc = c;
            } else if (Math.abs(this.sr - r) + Math.abs(this.sc - c) != 1) {
               this.sr = r;
               this.sc = c;
            } else {
               int t = this.g[r][c];
               this.g[r][c] = this.g[this.sr][this.sc];
               this.g[this.sr][this.sc] = t;
               if (!this.matches()[r][c] && !this.matches()[this.sr][this.sc]) {
                  t = this.g[r][c];
                  this.g[r][c] = this.g[this.sr][this.sc];
                  this.g[this.sr][this.sc] = t;
                  PackPuzzle.bad();
               } else {
                  this.combo = 0;
                  this.settle = 0.01F;
               }

               this.sr = -1;
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            this.fall[r][c] = Math.max(0.0F, this.fall[r][c] - dt * 10.0F);
         }
      }

      if (this.settle > 0.0F && (this.settle -= dt) <= 0.0F) {
         int n = this.clear();
         if (n > 0) {
            this.combo++;
            this.score = this.score + n * 10L * this.combo;
            Sound.play("minecraft:block.amethyst_block.chime", 1.0F + this.combo * 0.15F, 0.5F);
            this.settle = 0.28F;
         }
      }

      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.box(7.0F, 7.0F, 254.0F, 254.0F, 8.0F, Style.light ? 335544320 : 352321535);

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            if (this.g[r][c] >= 0) {
               float x = 10.0F + c * 31.0F;
               float y = 10.0F + (r - this.fall[r][c]) * 31.0F;
               int col = PackPuzzle.PAL[this.g[r][c]];
               if (r == this.sr && c == this.sc) {
                  this.box(x, y, 31.0F, 31.0F, 6.0F, 1728053247);
               }

               switch (this.g[r][c] % 3) {
                  case 0:
                     this.circle(x + 15.5F, y + 15.5F, 11.78F, col);
                     break;
                  case 1:
                     this.box(x + 5.0F, y + 5.0F, 21.0F, 21.0F, 4.0F, col);
                     break;
                  default:
                     this.circle(x + 15.5F, y + 15.5F, 11.160001F, col);
                     this.box(x + 9.0F, y + 9.0F, 13.0F, 13.0F, 2.0F, Gx.mix(col, -1, 0.3F));
               }

               this.circle(x + 11.78F, y + 11.160001F, 2.5F, -1996488705);
            }
         }
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 330.0F, 40.0F, 12.0F, 3, this.left < 10.0F ? Style.danger : Style.text);
      if (this.combo > 1) {
         this.text("Combo x" + this.combo, 330.0F, 80.0F, 9.0F, 3, -13053);
      }

      this.text("Tausche zwei Nachbarn", 330.0F, 220.0F, 6.2F, 2, Style.sub);
      this.text("für 3 in einer Reihe", 330.0F, 232.0F, 6.2F, 2, Style.sub);
   }
}
