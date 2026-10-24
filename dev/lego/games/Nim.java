package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Nim extends GameView {
   private final int[] rows = new int[4];
   private int selRow = -1;
   private int selN = 0;
   private int turn = 1;
   private int winner;
   private float think;
   private int wins;
   private final Duels.Duel duel;
   private int duelRound = -1;

   Nim() {
      super("nim");
      this.duel = null;
   }

   Nim(Duels.Duel d) {
      super("nim");
      this.duel = d;
   }

   @Override
   protected void start() {
      if (this.duel != null && this.duel.oppLeft) {
         this.back();
      } else {
         super.start();
         if (this.duel != null) {
            this.duelRound++;
         }
      }
   }

   @Override
   protected void back() {
      if (this.duel != null) {
         this.duel.leave();
      }

      super.back();
   }

   @Override
   public void removed() {
      if (this.duel != null) {
         this.duel.leave();
      }

      super.removed();
   }

   @Override
   protected boolean hasAi() {
      return this.duel == null;
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Siege";
   }

   @Override
   protected String overTitle() {
      return this.winner == 1 ? "Gewonnen!" : "Verloren - du hattest das letzte Holz";
   }

   @Override
   protected void reset() {
      this.rows[0] = 1;
      this.rows[1] = 3;
      this.rows[2] = 5;
      this.rows[3] = 7;
      this.selRow = -1;
      this.selN = 0;
      this.turn = this.duel != null ? (this.duel.iStart ? 1 : 2) : (this.rnd.nextBoolean() ? 1 : 2);
      this.winner = 0;
      this.think = 0.0F;
   }

   private int total() {
      return this.rows[0] + this.rows[1] + this.rows[2] + this.rows[3];
   }

   private void take(int r, int n) {
      this.rows[r] = this.rows[r] - n;
      PackPuzzle.click();
      if (this.total() == 0) {
         this.winner = this.turn == 1 ? 2 : 1;
         if (this.winner == 1) {
            this.wins++;
            this.score = this.wins;
            PackPuzzle.win();
         } else {
            this.score = this.wins;
            PackPuzzle.bad();
         }

         this.gameOver();
      } else {
         this.turn = 3 - this.turn;
         this.think = 0.0F;
         this.selRow = -1;
         this.selN = 0;
         if (this.duel != null) {
            this.duelRound++;
         }
      }
   }

   private int[] aiMove() {
      int lv = this.aiLevel();
      if (lv < 3 && this.rnd.nextFloat() < new float[]{0.7F, 0.4F, 0.12F}[lv]) {
         List<Integer> nz = new ArrayList<>();

         for (int i = 0; i < 4; i++) {
            if (this.rows[i] > 0) {
               nz.add(i);
            }
         }

         int r = nz.get(this.rnd.nextInt(nz.size()));
         return new int[]{r, 1 + this.rnd.nextInt(this.rows[r])};
      } else {
         int big = 0;
         int x = 0;

         for (int v : this.rows) {
            if (v > 1) {
               big++;
            }

            x ^= v;
         }

         for (int ix = 0; ix < 4; ix++) {
            if (this.rows[ix] != 0) {
               for (int n = 1; n <= this.rows[ix]; n++) {
                  int[] c = (int[])this.rows.clone();
                  c[ix] -= n;
                  int b2 = 0;
                  int x2 = 0;
                  int ones = 0;

                  for (int v : c) {
                     if (v > 1) {
                        b2++;
                     }

                     x2 ^= v;
                     if (v == 1) {
                        ones++;
                     }
                  }

                  boolean good = b2 == 0 ? ones % 2 == 1 : x2 == 0;
                  if (good) {
                     return new int[]{ix, n};
                  }
               }
            }
         }

         for (int ixx = 0; ixx < 4; ixx++) {
            if (this.rows[ixx] > 0) {
               return new int[]{ixx, 1};
            }
         }

         return new int[]{0, 0};
      }
   }

   @Override
   protected void update(float dt) {
      if (this.duel != null && this.duel.oppLeft && this.winner == 0) {
         this.winner = 1;
         this.wins++;
         this.score = this.wins;
         this.gameOver(false);
      } else {
         if (this.turn == 2 && this.winner == 0) {
            if (this.duel != null) {
               Integer m = this.duel.take(this.duelRound);
               if (m != null) {
                  int r = m / 8;
                  int n = m % 8;
                  if (r >= 0 && r < 4 && n >= 1 && n <= this.rows[r]) {
                     this.take(r, n);
                  }
               }
            } else if ((this.think += dt) > 0.8F) {
               int[] m = this.aiMove();
               this.take(m[0], m[1]);
            }
         }
      }
   }

   private float mx(int r, int i) {
      return 200 + (i - r) * 28;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1 && this.winner == 0) {
         if (in(x, y, 150, 240, 100, 26) && this.selN > 0) {
            if (this.duel != null) {
               this.duel.send(this.duelRound, this.selRow * 8 + this.selN);
            }

            this.take(this.selRow, this.selN);
         } else {
            for (int r = 0; r < 4; r++) {
               for (int i = 0; i < this.rows[r]; i++) {
                  if (Math.abs(x - this.mx(r, i)) < 11.0F && Math.abs(y - (40 + r * 50)) < 22.0F) {
                     if (this.selRow != r) {
                        this.selRow = r;
                        this.selN = 0;
                     }

                     this.selN = this.rows[r] - i;
                     PackPuzzle.click();
                  }
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      for (int r = 0; r < 4; r++) {
         for (int i = 0; i < this.rows[r]; i++) {
            float x = this.mx(r, i);
            float y = 40 + r * 50;
            boolean sel = r == this.selRow && i >= this.rows[r] - this.selN;
            this.box(x - 2.5F, y - 18.0F, 5.0F, 36.0F, 2.0F, sel ? -13053 : -1521798);
            this.circle(x, y - 19.0F, 4.0F, sel ? -7859 : -2677187);
         }
      }

      String oppLabel = this.duel != null ? this.duel.oppName : "KI";
      if (this.turn == 1 && this.selN > 0) {
         this.button("Nimm " + this.selN, 150.0F, 240.0F, 100.0F, 26.0F, Style.accent);
      } else {
         this.text(this.turn == 1 ? "Wähle Hölzer aus einer Reihe" : oppLabel + " überlegt...", 200.0F, 253.0F, 8.0F, 3, this.turn == 1 ? Style.ok : Style.sub);
      }

      this.text("Wer das letzte Holz nimmt, verliert", 200.0F, 225.0F, 6.4F, 2, Style.sub);
   }
}
