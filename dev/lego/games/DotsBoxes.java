package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class DotsBoxes extends GameView {
   private static final int N = 4;
   private static final float S0 = 54.0F;
   private static final float OX = 30.0F;
   private static final float OY = 22.0F;
   private boolean[][] h = new boolean[5][4];
   private boolean[][] v = new boolean[4][5];
   private int[][] owner = new int[4][4];
   private int turn = 1;
   private int sp;
   private int sa;
   private float think;

   DotsBoxes() {
      super("dotsboxes");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Kästchen";
   }

   @Override
   protected String overTitle() {
      return this.sp > this.sa ? "Gewonnen!" : (this.sp < this.sa ? "Verloren" : "Unentschieden");
   }

   @Override
   protected void reset() {
      this.h = new boolean[5][4];
      this.v = new boolean[4][5];
      this.owner = new int[4][4];
      this.turn = 1;
      this.sp = this.sa = 0;
      this.think = 0.0F;
   }

   private int sides(int r, int c) {
      return (this.h[r][c] ? 1 : 0) + (this.h[r + 1][c] ? 1 : 0) + (this.v[r][c] ? 1 : 0) + (this.v[r][c + 1] ? 1 : 0);
   }

   private boolean draw(int[] l, int who) {
      if (l[0] == 0) {
         if (this.h[l[1]][l[2]]) {
            return false;
         }

         this.h[l[1]][l[2]] = true;
      } else {
         if (this.v[l[1]][l[2]]) {
            return false;
         }

         this.v[l[1]][l[2]] = true;
      }

      boolean scored = false;

      for (int r = 0; r < 4; r++) {
         for (int c = 0; c < 4; c++) {
            if (this.owner[r][c] == 0 && this.sides(r, c) == 4) {
               this.owner[r][c] = who;
               scored = true;
               if (who == 1) {
                  this.sp++;
               } else {
                  this.sa++;
               }
            }
         }
      }

      PackPuzzle.click();
      if (this.sp + this.sa == 16) {
         this.score = this.sp > this.sa ? this.sp : 0L;
         if (this.sp > this.sa) {
            PackPuzzle.win();
         }

         this.gameOver();
         return true;
      } else {
         if (!scored) {
            this.turn = 3 - this.turn;
         } else if (who == 1) {
            PackPuzzle.good();
         }

         this.think = 0.0F;
         return true;
      }
   }

   private List<int[]> free() {
      List<int[]> l = new ArrayList<>();

      for (int r = 0; r <= 4; r++) {
         for (int c = 0; c < 4; c++) {
            if (!this.h[r][c]) {
               l.add(new int[]{0, r, c});
            }
         }
      }

      for (int r = 0; r < 4; r++) {
         for (int cx = 0; cx <= 4; cx++) {
            if (!this.v[r][cx]) {
               l.add(new int[]{1, r, cx});
            }
         }
      }

      return l;
   }

   private int maxSidesAfter(int[] l) {
      int m = 0;
      if (l[0] == 0) {
         if (l[1] > 0) {
            m = Math.max(m, this.sides(l[1] - 1, l[2]) + 1);
         }

         if (l[1] < 4) {
            m = Math.max(m, this.sides(l[1], l[2]) + 1);
         }
      } else {
         if (l[2] > 0) {
            m = Math.max(m, this.sides(l[1], l[2] - 1) + 1);
         }

         if (l[2] < 4) {
            m = Math.max(m, this.sides(l[1], l[2]) + 1);
         }
      }

      return m;
   }

   private int[] ai() {
      List<int[]> f = this.free();
      int lv = this.aiLevel();
      if (lv == 0 && this.rnd.nextFloat() < 0.5F) {
         return f.get(this.rnd.nextInt(f.size()));
      } else {
         for (int[] l : f) {
            if (this.maxSidesAfter(l) == 4) {
               return l;
            }
         }

         List<int[]> safe = new ArrayList<>();

         for (int[] lx : f) {
            if (this.maxSidesAfter(lx) <= 2) {
               safe.add(lx);
            }
         }

         return safe.isEmpty() || lv < 1 && !this.rnd.nextBoolean() ? f.get(this.rnd.nextInt(f.size())) : safe.get(this.rnd.nextInt(safe.size()));
      }
   }

   @Override
   protected void update(float dt) {
      if (this.turn == 2 && (this.think += dt) > 0.45F) {
         this.draw(this.ai(), 2);
      }
   }

   private int[] lineAt(float x, float y) {
      int[] best = null;
      float bd = 9.0F;

      for (int r = 0; r <= 4; r++) {
         for (int c = 0; c < 4; c++) {
            float d = (float)Math.hypot(x - (30.0F + c * 54.0F + 27.0F), y - (22.0F + r * 54.0F));
            if (d < bd && !this.h[r][c]) {
               bd = d;
               best = new int[]{0, r, c};
            }
         }
      }

      for (int r = 0; r < 4; r++) {
         for (int cx = 0; cx <= 4; cx++) {
            float d = (float)Math.hypot(x - (30.0F + cx * 54.0F), y - (22.0F + r * 54.0F + 27.0F));
            if (d < bd && !this.v[r][cx]) {
               bd = d;
               best = new int[]{1, r, cx};
            }
         }
      }

      return best;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1) {
         int[] l = this.lineAt(x, y);
         if (l != null) {
            this.draw(l, 1);
         }
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 4; r++) {
         for (int c = 0; c < 4; c++) {
            if (this.owner[r][c] != 0) {
               this.box(30.0F + c * 54.0F + 3.0F, 22.0F + r * 54.0F + 3.0F, 48.0F, 48.0F, 6.0F, this.owner[r][c] == 1 ? 1715448319 : 1726152715);
            }
         }
      }

      int idle = Style.light ? 570425344 : 587202559;

      for (int r = 0; r <= 4; r++) {
         for (int cx = 0; cx < 4; cx++) {
            this.line(
               30.0F + cx * 54.0F,
               22.0F + r * 54.0F,
               30.0F + (cx + 1) * 54.0F,
               22.0F + r * 54.0F,
               this.h[r][cx] ? 3.0F : 1.2F,
               this.h[r][cx] ? Style.text : idle
            );
         }
      }

      for (int r = 0; r < 4; r++) {
         for (int cx = 0; cx <= 4; cx++) {
            this.line(
               30.0F + cx * 54.0F,
               22.0F + r * 54.0F,
               30.0F + cx * 54.0F,
               22.0F + (r + 1) * 54.0F,
               this.v[r][cx] ? 3.0F : 1.2F,
               this.v[r][cx] ? Style.text : idle
            );
         }
      }

      int[] hl = this.turn == 1 ? this.lineAt(this.bmx(), this.bmy()) : null;
      if (hl != null) {
         if (hl[0] == 0) {
            this.line(30.0F + hl[2] * 54.0F, 22.0F + hl[1] * 54.0F, 30.0F + (hl[2] + 1) * 54.0F, 22.0F + hl[1] * 54.0F, 3.0F, -1723880961);
         } else {
            this.line(30.0F + hl[2] * 54.0F, 22.0F + hl[1] * 54.0F, 30.0F + hl[2] * 54.0F, 22.0F + (hl[1] + 1) * 54.0F, 3.0F, -1723880961);
         }
      }

      for (int r = 0; r <= 4; r++) {
         for (int cx = 0; cx <= 4; cx++) {
            this.circle(30.0F + cx * 54.0F, 22.0F + r * 54.0F, 3.5F, Style.text);
         }
      }

      this.text("Du: " + this.sp, 330.0F, 60.0F, 11.0F, 3, -12604929);
      this.text("KI: " + this.sa, 330.0F, 90.0F, 11.0F, 3, -1900533);
      this.text(this.turn == 1 ? "Du bist dran" : "KI zieht...", 330.0F, 130.0F, 7.5F, 3, Style.sub);
      this.text("Kästchen zu = nochmal", 330.0F, 200.0F, 6.2F, 1, Style.sub);
   }
}
