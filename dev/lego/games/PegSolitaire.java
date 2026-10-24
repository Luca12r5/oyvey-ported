package dev.lego.games;

import dev.lego.ui.Style;

final class PegSolitaire extends GameView {
   private static final float C = 32.0F;
   private static final float OX = 50.0F;
   private static final float OY = 18.0F;
   private final int[] g = new int[49];
   private int sel = -1;
   private int removed;

   PegSolitaire() {
      super("pegsolitaire");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 262.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Entfernt";
   }

   @Override
   protected String overTitle() {
      return this.removed == 31 ? "Perfekt - nur noch einer!" : "Keine Züge mehr";
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 49; i++) {
         int r = i / 7;
         int c = i % 7;
         this.g[i] = r >= 2 && r <= 4 || c >= 2 && c <= 4 ? 1 : -1;
      }

      this.g[24] = 0;
      this.sel = -1;
      this.removed = 0;
   }

   private boolean canMove() {
      for (int i = 0; i < 49; i++) {
         if (this.g[i] == 1) {
            for (int[] d : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}}) {
               int r = i / 7 + d[0] * 2;
               int c = i % 7 + d[1] * 2;
               if (r >= 0 && c >= 0 && r < 7 && c < 7 && this.g[r * 7 + c] == 0 && this.g[(i / 7 + d[0]) * 7 + i % 7 + d[1]] == 1) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int c = (int)((x - 50.0F) / 32.0F);
      int r = (int)((y - 18.0F) / 32.0F);
      if (!(x < 50.0F) && !(y < 18.0F) && c <= 6 && r <= 6) {
         int i = r * 7 + c;
         if (this.g[i] == 1) {
            this.sel = i;
            PackPuzzle.click();
         } else if (this.g[i] == 0 && this.sel >= 0) {
            int sr = this.sel / 7;
            int sc = this.sel % 7;
            if (Math.abs(sr - r) == 2 && sc == c || Math.abs(sc - c) == 2 && sr == r) {
               int mid = (sr + r) / 2 * 7 + (sc + c) / 2;
               if (this.g[mid] == 1) {
                  this.g[this.sel] = 0;
                  this.g[mid] = 0;
                  this.g[i] = 1;
                  this.sel = -1;
                  this.removed++;
                  this.score = this.removed;
                  PackPuzzle.good();
                  if (!this.canMove()) {
                     if (this.removed == 31) {
                        PackPuzzle.win();
                     }

                     this.gameOver();
                  }
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
      for (int i = 0; i < 49; i++) {
         if (this.g[i] >= 0) {
            float x = 50.0F + i % 7 * 32.0F + 16.0F;
            float y = 18.0F + i / 7 * 32.0F + 16.0F;
            this.circle(x, y, 11.0F, Style.light ? 570425344 : 855638016);
            this.circle(x, y, 7.0F, Style.light ? -5196612 : -15724008);
            if (this.g[i] == 1) {
               this.circle(x, y - 1.0F, 10.0F, i == this.sel ? -13053 : -1900533);
               this.circle(x - 3.0F, y - 4.0F, 3.0F, 1442840575);
            }
         }
      }

      this.text("Übrig: " + (32 - this.removed), 340.0F, 40.0F, 9.0F, 3, Style.text);
      this.text("Springe über einen", 340.0F, 80.0F, 6.2F, 1, Style.sub);
      this.text("Stift ins Loch dahinter", 340.0F, 92.0F, 6.2F, 1, Style.sub);
   }
}
