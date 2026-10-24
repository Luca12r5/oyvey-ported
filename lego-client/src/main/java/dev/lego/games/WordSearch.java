package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WordSearch extends GameView {
   private static final int N = 11;
   private static final float C = 22.0F;
   private static final float OX = 10.0F;
   private static final float OY = 10.0F;
   private char[][] g;
   private final List<String> words = new ArrayList<>();
   private final List<int[]> found = new ArrayList<>();
   private final List<String> foundW = new ArrayList<>();
   private int sr = -1;
   private int sc = -1;
   private float left;

   WordSearch() {
      super("wordsearch");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 262.0F;
   }

   @Override
   protected String overTitle() {
      return this.foundW.size() == this.words.size() ? "Alle Wörter gefunden!" : "Zeit um!";
   }

   @Override
   protected void reset() {
      this.g = new char[11][11];
      this.words.clear();
      this.found.clear();
      this.foundW.clear();
      int[][] dirs = new int[][]{{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
      int tries = 0;

      while (this.words.size() < 7 && tries++ < 500) {
         String w = Words.ANY[this.rnd.nextInt(Words.ANY.length)];
         if (w.length() <= 9 && !this.words.contains(w)) {
            int[] d = dirs[this.rnd.nextInt(dirs.length)];
            int r = this.rnd.nextInt(11);
            int c = this.rnd.nextInt(11);
            int er = r + d[0] * (w.length() - 1);
            int ec = c + d[1] * (w.length() - 1);
            if (er >= 0 && er < 11 && ec < 11) {
               boolean ok = true;

               for (int i = 0; i < w.length(); i++) {
                  char ch = this.g[r + d[0] * i][c + d[1] * i];
                  if (ch != 0 && ch != w.charAt(i)) {
                     ok = false;
                  }
               }

               if (ok) {
                  for (int ix = 0; ix < w.length(); ix++) {
                     this.g[r + d[0] * ix][c + d[1] * ix] = w.charAt(ix);
                  }

                  this.words.add(w);
               }
            }
         }
      }

      for (int r = 0; r < 11; r++) {
         for (int c = 0; c < 11; c++) {
            if (this.g[r][c] == 0) {
               this.g[r][c] = (char)(65 + this.rnd.nextInt(26));
            }
         }
      }

      this.left = 180.0F;
      this.sr = -1;
   }

   private int[] cell(float x, float y) {
      int c = (int)((x - 10.0F) / 22.0F);
      int r = (int)((y - 10.0F) / 22.0F);
      return !(x < 10.0F) && !(y < 10.0F) && c < 11 && r < 11 ? new int[]{r, c} : null;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int[] p = this.cell(x, y);
      if (p != null) {
         this.sr = p[0];
         this.sc = p[1];
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      int[] p = this.cell(x, y);
      if (p != null && this.sr >= 0) {
         int dr = Integer.signum(p[0] - this.sr);
         int dc = Integer.signum(p[1] - this.sc);
         int len = Math.max(Math.abs(p[0] - this.sr), Math.abs(p[1] - this.sc)) + 1;
         if (p[0] - this.sr != 0 && p[1] - this.sc != 0 && Math.abs(p[0] - this.sr) != Math.abs(p[1] - this.sc)) {
            this.sr = -1;
         } else {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < len; i++) {
               sb.append(this.g[this.sr + dr * i][this.sc + dc * i]);
            }

            String s = sb.toString();
            String rev = sb.reverse().toString();

            for (String w : this.words) {
               if (!this.foundW.contains(w) && (w.equals(s) || w.equals(rev))) {
                  this.foundW.add(w);
                  this.found.add(new int[]{this.sr, this.sc, p[0], p[1]});
                  this.score = this.score + w.length() * 10;
                  PackPuzzle.good();
                  if (this.foundW.size() == this.words.size()) {
                     this.score = this.score + (long)this.left;
                     PackPuzzle.win();
                     this.gameOver();
                  }
               }
            }

            this.sr = -1;
         }
      } else {
         this.sr = -1;
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      for (int[] f : this.found) {
         this.line(10.0F + f[1] * 22.0F + 11.0F, 10.0F + f[0] * 22.0F + 11.0F, 10.0F + f[3] * 22.0F + 11.0F, 10.0F + f[2] * 22.0F + 11.0F, 15.0F, 1715065683);
      }

      if (this.sr >= 0) {
         int[] p = this.cell(this.bmx(), this.bmy());
         if (p != null) {
            this.line(
               10.0F + this.sc * 22.0F + 11.0F, 10.0F + this.sr * 22.0F + 11.0F, 10.0F + p[1] * 22.0F + 11.0F, 10.0F + p[0] * 22.0F + 11.0F, 15.0F, 1430235647
            );
         }
      }

      for (int r = 0; r < 11; r++) {
         for (int c = 0; c < 11; c++) {
            this.text(String.valueOf(this.g[r][c]), 10.0F + c * 22.0F + 11.0F, 10.0F + r * 22.0F + 11.0F, 9.0F, 3, Style.text);
         }
      }

      float y = 30.0F;

      for (String w : this.words) {
         boolean f = this.foundW.contains(w);
         this.textL(w, 268.0F, y, 7.5F, 3, f ? Style.ok : Style.text);
         if (f) {
            this.box(266.0F, y, Gx.width(w, 7.5F, 3) + 4.0F, 1.2F, 0.0F, Style.ok);
         }

         y += 18.0F;
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 330.0F, 240.0F, 9.0F, 3, this.left < 20.0F ? Style.danger : Style.sub);
      this.textL("Ziehe über ein Wort", 268.0F, 210.0F, 6.4F, 2, Style.muted);
   }
}
