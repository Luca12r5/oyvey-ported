package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Reversi extends GameView {
   private static final float C = 33.0F;
   private static final float OX = 10.0F;
   private static final float OY = 10.0F;
   private static final int[] W = new int[]{
      100,
      -20,
      10,
      5,
      5,
      10,
      -20,
      100,
      -20,
      -50,
      -2,
      -2,
      -2,
      -2,
      -50,
      -20,
      10,
      -2,
      1,
      1,
      1,
      1,
      -2,
      10,
      5,
      -2,
      1,
      0,
      0,
      1,
      -2,
      5,
      5,
      -2,
      1,
      0,
      0,
      1,
      -2,
      5,
      10,
      -2,
      1,
      1,
      1,
      1,
      -2,
      10,
      -20,
      -50,
      -2,
      -2,
      -2,
      -2,
      -50,
      -20,
      100,
      -20,
      10,
      5,
      5,
      10,
      -20,
      100
   };
   private int[] b = new int[64];
   private int turn;
   private int ended;
   private float think;
   private String msg = "";
   private final Duels.Duel duel;
   private int duelRound = -1;

   Reversi() {
      super("reversi");
      this.duel = null;
   }

   Reversi(Duels.Duel d) {
      super("reversi");
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
      return 284.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Steine";
   }

   @Override
   protected String overTitle() {
      return this.ended == 1 ? "Gewonnen!" : (this.ended == 2 ? "Verloren" : "Unentschieden");
   }

   @Override
   protected void reset() {
      this.b = new int[64];
      this.b[27] = this.b[36] = 2;
      this.b[28] = this.b[35] = 1;
      this.turn = 1;
      this.ended = 0;
      this.think = 0.0F;
      this.msg = "";
   }

   private static List<Integer> flips(int[] b, int p, int who) {
      List<Integer> out = new ArrayList<>();
      if (b[p] != 0) {
         return out;
      } else {
         int r = p / 8;
         int c = p % 8;

         for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
               if (dr != 0 || dc != 0) {
                  List<Integer> line = new ArrayList<>();
                  int rr = r + dr;

                  int cc;
                  for (cc = c + dc; rr >= 0 && cc >= 0 && rr < 8 && cc < 8 && b[rr * 8 + cc] == 3 - who; cc += dc) {
                     line.add(rr * 8 + cc);
                     rr += dr;
                  }

                  if (!line.isEmpty() && rr >= 0 && cc >= 0 && rr < 8 && cc < 8 && b[rr * 8 + cc] == who) {
                     out.addAll(line);
                  }
               }
            }
         }

         return out;
      }
   }

   private static List<Integer> moves(int[] b, int who) {
      List<Integer> m = new ArrayList<>();

      for (int i = 0; i < 64; i++) {
         if (!flips(b, i, who).isEmpty()) {
            m.add(i);
         }
      }

      return m;
   }

   private static void play(int[] b, int p, int who) {
      for (int f : flips(b, p, who)) {
         b[f] = who;
      }

      b[p] = who;
   }

   private int count(int who) {
      int n = 0;

      for (int v : this.b) {
         if (v == who) {
            n++;
         }
      }

      return n;
   }

   private static int eval(int[] b) {
      int s = 0;

      for (int i = 0; i < 64; i++) {
         s += b[i] == 2 ? W[i] : (b[i] == 1 ? -W[i] : 0);
      }

      return s + (moves(b, 2).size() - moves(b, 1).size()) * 3;
   }

   private static int mini(int[] b, int d, int who, int a, int be) {
      if (d == 0) {
         return eval(b);
      } else {
         List<Integer> ms = moves(b, who);
         if (ms.isEmpty()) {
            if (moves(b, 3 - who).isEmpty()) {
               int n = 0;

               for (int v : b) {
                  n += v == 2 ? 1 : (v == 1 ? -1 : 0);
               }

               return n * 1000;
            } else {
               return mini(b, d - 1, 3 - who, a, be);
            }
         } else {
            for (int m : ms) {
               int[] c = (int[])b.clone();
               play(c, m, who);
               int v = mini(c, d - 1, 3 - who, a, be);
               if (who == 2) {
                  a = Math.max(a, v);
               } else {
                  be = Math.min(be, v);
               }

               if (a >= be) {
                  break;
               }
            }

            return who == 2 ? a : be;
         }
      }
   }

   private int ai() {
      List<Integer> ms = moves(this.b, 2);
      int lv = this.aiLevel();
      if (lv == 0 && this.rnd.nextFloat() < 0.6F) {
         return ms.get(this.rnd.nextInt(ms.size()));
      } else {
         int depth = new int[]{1, 1, 3, 5}[lv];
         int best = Integer.MIN_VALUE;
         int pick = ms.get(0);

         for (int m : ms) {
            int[] c = (int[])this.b.clone();
            play(c, m, 2);
            int v = depth == 1 ? eval(c) + this.rnd.nextInt(3) : mini(c, depth - 1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (v > best) {
               best = v;
               pick = m;
            }
         }

         return pick;
      }
   }

   private void advance() {
      this.turn = 3 - this.turn;
      if (moves(this.b, this.turn).isEmpty()) {
         if (moves(this.b, 3 - this.turn).isEmpty()) {
            this.finish();
            return;
         }

         String oppLabel = this.duel != null ? this.duel.oppName : "KI";
         this.msg = this.turn == 1 ? "Du musst aussetzen" : oppLabel + " muss aussetzen";
         this.turn = 3 - this.turn;
      } else {
         this.msg = "";
      }

      this.think = 0.0F;
      if (this.duel != null) {
         this.duelRound++;
      }
   }

   private void finish() {
      int p = this.count(1);
      int k = this.count(2);
      this.ended = p > k ? 1 : (p < k ? 2 : 3);
      this.score = this.ended == 1 ? p : 0L;
      if (this.ended == 1) {
         PackPuzzle.win();
      } else {
         PackPuzzle.bad();
      }

      this.gameOver();
   }

   @Override
   protected void update(float dt) {
      if (this.duel != null && this.duel.oppLeft && this.ended == 0) {
         this.ended = 1;
         this.score = this.count(1);
         this.gameOver(false);
      } else {
         if (this.turn == 2 && this.ended == 0) {
            if (this.duel != null) {
               Integer m = this.duel.take(this.duelRound);
               if (m != null && m >= 0 && m < 64 && !flips(this.b, m, 2).isEmpty()) {
                  play(this.b, m, 2);
                  PackPuzzle.click();
                  this.advance();
               }
            } else {
               this.think += dt;
               if (this.think > 0.5F) {
                  play(this.b, this.ai(), 2);
                  PackPuzzle.click();
                  this.advance();
               }
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int bt) {
      if (this.turn == 1 && this.ended == 0) {
         int c = (int)((x - 10.0F) / 33.0F);
         int r = (int)((y - 10.0F) / 33.0F);
         if (!(x < 10.0F) && !(y < 10.0F) && c <= 7 && r <= 7) {
            int pos = r * 8 + c;
            if (!flips(this.b, pos, 1).isEmpty()) {
               play(this.b, pos, 1);
               PackPuzzle.click();
               if (this.duel != null) {
                  this.duel.send(this.duelRound, pos);
               }

               this.advance();
            }
         }
      }
   }

   @Override
   protected void render() {
      this.box(6.0F, 6.0F, 272.0F, 272.0F, 8.0F, -14783686);
      List<Integer> legal = this.turn == 1 ? moves(this.b, 1) : List.of();

      for (int i = 0; i < 64; i++) {
         float x = 10.0F + i % 8 * 33.0F;
         float y = 10.0F + i / 8 * 33.0F;
         this.box(x + 1.0F, y + 1.0F, 31.0F, 31.0F, 3.0F, -13726898);
         if (this.b[i] != 0) {
            this.circle(x + 16.5F, y + 16.5F + 1.5F, 12.54F, 1711276032);
            this.circle(x + 16.5F, y + 16.5F, 12.54F, this.b[i] == 1 ? -15395302 : -723724);
         } else if (legal.contains(i)) {
            this.circle(x + 16.5F, y + 16.5F, 3.5F, 1442840575);
         }
      }

      float px = 300.0F;
      String oppLabel = this.duel != null ? this.duel.oppName : "KI";
      this.circle(px + 10.0F, 40.0F, 10.0F, -15395302);
      this.textL("Du: " + this.count(1), px + 26.0F, 40.0F, 9.0F, 3, Style.text);
      this.circle(px + 10.0F, 70.0F, 10.0F, -723724);
      this.textL(oppLabel + ": " + this.count(2), px + 26.0F, 70.0F, 9.0F, 3, Style.text);
      this.text(this.turn == 1 ? "Du bist dran" : oppLabel + " denkt...", px + 45.0F, 110.0F, 8.0F, 3, this.turn == 1 ? Style.ok : Style.sub);
      if (!this.msg.isEmpty()) {
         this.text(this.msg, px + 45.0F, 130.0F, 6.6F, 2, Style.danger);
      }

      if (this.duel == null) {
         this.text("KI: " + GameInfo.LEVELS[this.aiLevel()], px + 45.0F, 260.0F, 6.6F, 2, Style.sub);
      }
   }
}
