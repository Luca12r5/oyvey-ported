package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Nonogram extends GameView {
   private static final int N = 8;
   private static final float C = 24.0F;
   private static final float OX = 110.0F;
   private static final float OY = 66.0F;
   private boolean[][] sol = new boolean[8][8];
   private int[][] mark = new int[8][8];
   private final List<List<Integer>> rowClues = new ArrayList<>();
   private final List<List<Integer>> colClues = new ArrayList<>();
   private int mistakes;
   private int solved;

   Nonogram() {
      super("nonogram");
   }

   @Override
   protected float boardW() {
      return 340.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Rätsel";
   }

   @Override
   protected String overTitle() {
      return "3 Fehler - Rätsel verloren";
   }

   @Override
   protected void reset() {
      this.solved = 0;
      this.mistakes = 0;
      this.generate();
   }

   private void generate() {
      this.sol = new boolean[8][8];
      this.mark = new int[8][8];

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            this.sol[r][c] = this.rnd.nextFloat() < 0.58F;
         }
      }

      this.rowClues.clear();
      this.colClues.clear();

      for (int r = 0; r < 8; r++) {
         List<Integer> l = new ArrayList<>();
         int run = 0;

         for (int c = 0; c < 8; c++) {
            if (this.sol[r][c]) {
               run++;
            } else if (run > 0) {
               l.add(run);
               run = 0;
            }
         }

         if (run > 0) {
            l.add(run);
         }

         this.rowClues.add(l);
      }

      for (int cx = 0; cx < 8; cx++) {
         List<Integer> l = new ArrayList<>();
         int run = 0;

         for (int r = 0; r < 8; r++) {
            if (this.sol[r][cx]) {
               run++;
            } else if (run > 0) {
               l.add(run);
               run = 0;
            }
         }

         if (run > 0) {
            l.add(run);
         }

         this.colClues.add(l);
      }
   }

   private boolean complete() {
      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            if (this.sol[r][c] && this.mark[r][c] != 1) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int c = (int)((x - 110.0F) / 24.0F);
      int r = (int)((y - 66.0F) / 24.0F);
      if (!(x < 110.0F) && !(y < 66.0F) && c < 8 && r < 8 && this.mark[r][c] == 0) {
         if (b == 1) {
            this.mark[r][c] = 2;
         } else {
            if (this.sol[r][c]) {
               this.mark[r][c] = 1;
               PackPuzzle.click();
               if (this.complete()) {
                  this.solved++;
                  this.score = this.solved;
                  PackPuzzle.win();
                  this.generate();
               }
            } else {
               this.mark[r][c] = 2;
               this.mistakes++;
               PackPuzzle.bad();
               if (this.mistakes >= 3) {
                  this.gameOver();
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      for (int r = 0; r < 8; r++) {
         List<Integer> l = this.rowClues.get(r);
         String s = l.isEmpty() ? "0" : l.toString().replaceAll("[\\[\\],]", "");
         Gx.textMid(s, this.X(104.0F - Gx.width(s, 7.0F * this.u, 3) / this.u), this.Y(66.0F + r * 24.0F + 12.0F), 7.0F * this.u, 3, Style.text);
      }

      for (int c = 0; c < 8; c++) {
         List<Integer> l = this.colClues.get(c);
         if (l.isEmpty()) {
            l = List.of(0);
         }

         for (int i = 0; i < l.size(); i++) {
            this.text(String.valueOf(l.get(i)), 110.0F + c * 24.0F + 12.0F, 58.0F - (l.size() - 1 - i) * 10, 7.0F, 3, Style.text);
         }
      }

      for (int r = 0; r < 8; r++) {
         for (int c = 0; c < 8; c++) {
            float x = 110.0F + c * 24.0F;
            float y = 66.0F + r * 24.0F;
            this.box(x + 1.0F, y + 1.0F, 22.0F, 22.0F, 3.0F, this.mark[r][c] == 1 ? Style.accent : (Style.light ? -1644306 : -14539474));
            if (this.mark[r][c] == 2) {
               this.line(x + 7.0F, y + 7.0F, x + 24.0F - 7.0F, y + 24.0F - 7.0F, 1.5F, Style.muted);
               this.line(x + 24.0F - 7.0F, y + 7.0F, x + 7.0F, y + 24.0F - 7.0F, 1.5F, Style.muted);
            }
         }
      }

      this.textL("Fehler: " + this.mistakes + "/3", 8.0F, 12.0F, 7.0F, 3, this.mistakes > 0 ? Style.danger : Style.sub);
      this.textL("Gelöst: " + this.solved, 8.0F, 26.0F, 7.0F, 3, Style.sub);
      this.textL("Rechtsklick = Kreuz", 8.0F, 40.0F, 6.0F, 2, Style.muted);
   }
}
