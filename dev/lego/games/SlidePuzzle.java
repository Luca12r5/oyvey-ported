package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class SlidePuzzle extends GameView {
   private static final int N = 4;
   private static final float C = 62.0F;
   private static final float OX = 14.0F;
   private static final float OY = 14.0F;
   private final int[] t = new int[16];
   private final float[] ax = new float[16];
   private final float[] ay = new float[16];
   private int moves;
   private float doneT = -1.0F;

   SlidePuzzle() {
      super("slidepuzzle");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 276.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Züge";
   }

   @Override
   protected String overTitle() {
      return "Gelöst!";
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 16; i++) {
         this.t[i] = (i + 1) % 16;
      }

      int e = 15;
      int last = -1;

      for (int k = 0; k < 400; k++) {
         List<Integer> nb = new ArrayList<>();
         int er = e / 4;
         int ec = e % 4;
         if (er > 0) {
            nb.add(e - 4);
         }

         if (er < 3) {
            nb.add(e + 4);
         }

         if (ec > 0) {
            nb.add(e - 1);
         }

         if (ec < 3) {
            nb.add(e + 1);
         }

         nb.remove(Integer.valueOf(last));
         int n = nb.get(this.rnd.nextInt(nb.size()));
         this.t[e] = this.t[n];
         this.t[n] = 0;
         last = e;
         e = n;
      }

      for (int i = 0; i < 16; i++) {
         this.ax[this.t[i]] = i % 4;
         this.ay[this.t[i]] = i / 4;
      }

      this.moves = 0;
      this.doneT = -1.0F;
   }

   private int empty() {
      for (int i = 0; i < this.t.length; i++) {
         if (this.t[i] == 0) {
            return i;
         }
      }

      return -1;
   }

   private boolean solved() {
      for (int i = 0; i < 15; i++) {
         if (this.t[i] != i + 1) {
            return false;
         }
      }

      return true;
   }

   private void tryMove(int i) {
      int e = this.empty();
      if (i >= 0 && i < 16 && !(this.doneT >= 0.0F)) {
         if (Math.abs(i / 4 - e / 4) + Math.abs(i % 4 - e % 4) == 1) {
            this.t[e] = this.t[i];
            this.t[i] = 0;
            this.moves++;
            this.score = this.moves;
            PackPuzzle.click();
            if (this.solved()) {
               this.doneT = 0.0F;
               PackPuzzle.win();
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int c = (int)((x - 14.0F) / 62.0F);
      int r = (int)((y - 14.0F) / 62.0F);
      if (x >= 14.0F && y >= 14.0F && c < 4 && r < 4) {
         this.tryMove(r * 4 + c);
      }
   }

   @Override
   protected void onKey(int k) {
      int e = this.empty();
      if (k == 263 || k == 65) {
         if (e % 4 < 3) {
            this.tryMove(e + 1);
         }
      } else if (k != 262 && k != 68) {
         if (k == 265 || k == 87) {
            if (e / 4 < 3) {
               this.tryMove(e + 4);
            }
         } else if ((k == 264 || k == 83) && e / 4 > 0) {
            this.tryMove(e - 4);
         }
      } else if (e % 4 > 0) {
         this.tryMove(e - 1);
      }
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 0.8F) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      float k = 1.0F - (float)Math.exp(-Gx.dt * 18.0F);
      this.box(10.0F, 10.0F, 256.0F, 256.0F, 12.0F, Style.light ? 335544320 : 352321535);

      for (int i = 0; i < 16; i++) {
         int v = this.t[i];
         if (v != 0) {
            this.ax[v] = this.ax[v] + (i % 4 - this.ax[v]) * k;
            this.ay[v] = this.ay[v] + (i / 4 - this.ay[v]) * k;
            float x = 14.0F + this.ax[v] * 62.0F;
            float y = 14.0F + this.ay[v] * 62.0F;
            boolean right = v == i + 1;
            this.brick(x + 3.0F, y + 3.0F, 56.0F, 56.0F, right ? -16743125 : PackPuzzle.PAL[(v - 1) / 4 % 4]);
            this.text(String.valueOf(v), x + 31.0F, y + 31.0F + 4.0F, 15.0F, 3, -1);
         }
      }

      this.text("Züge: " + this.moves, 340.0F, 60.0F, 9.0F, 3, Style.text);
      this.text("Sortiere 1-15", 340.0F, 100.0F, 6.6F, 1, Style.sub);
      this.text("Klick oder Pfeile", 340.0F, 114.0F, 6.6F, 1, Style.sub);
   }
}
