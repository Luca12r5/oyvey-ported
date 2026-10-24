package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Checkers extends GameView {
   private static final float C = 32.0F;
   private static final float OX = 10.0F;
   private static final float OY = 6.0F;
   private int[] b = new int[64];
   private int sel = -1;
   private int turn;
   private int chainFrom = -1;
   private int ended;
   private float think;

   Checkers() {
      super("checkers");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 396.0F;
   }

   @Override
   protected float boardH() {
      return 268.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Steine";
   }

   @Override
   protected String overTitle() {
      return this.ended == 1 ? "Gewonnen!" : "Verloren";
   }

   @Override
   protected void reset() {
      this.b = new int[64];

      for (int i = 0; i < 64; i++) {
         int r = i / 8;
         int c = i % 8;
         if ((r + c) % 2 == 1) {
            if (r < 3) {
               this.b[i] = 2;
            } else if (r > 4) {
               this.b[i] = 1;
            }
         }
      }

      this.turn = 1;
      this.sel = -1;
      this.chainFrom = -1;
      this.ended = 0;
      this.think = 0.0F;
   }

   private static int owner(int v) {
      return v == 0 ? 0 : (v != 1 && v != 3 ? 2 : 1);
   }

   private static boolean king(int v) {
      return v >= 3;
   }

   private static List<int[]> moves(int[] b, int who, int only) {
      List<int[]> caps = new ArrayList<>();
      List<int[]> plain = new ArrayList<>();

      for (int i = 0; i < 64; i++) {
         if (owner(b[i]) == who && (only < 0 || i == only)) {
            int r = i / 8;
            int c = i % 8;
            int[] drs = king(b[i]) ? new int[]{-1, 1} : (who == 1 ? new int[]{-1} : new int[]{1});

            for (int dr : drs) {
               for (int dc : new int[]{-1, 1}) {
                  int r1 = r + dr;
                  int c1 = c + dc;
                  int r2 = r + 2 * dr;
                  int c2 = c + 2 * dc;
                  if (r1 >= 0 && r1 <= 7 && c1 >= 0 && c1 <= 7) {
                     if (b[r1 * 8 + c1] == 0) {
                        plain.add(new int[]{i, r1 * 8 + c1, -1});
                     } else if (owner(b[r1 * 8 + c1]) == 3 - who && r2 >= 0 && r2 <= 7 && c2 >= 0 && c2 <= 7 && b[r2 * 8 + c2] == 0) {
                        caps.add(new int[]{i, r2 * 8 + c2, r1 * 8 + c1});
                     }
                  }
               }
            }
         }
      }

      return caps.isEmpty() ? (only >= 0 ? caps : plain) : caps;
   }

   private static boolean apply(int[] b, int[] m) {
      int v = b[m[0]];
      b[m[0]] = 0;
      b[m[1]] = v;
      if (m[2] >= 0) {
         b[m[2]] = 0;
      }

      int r = m[1] / 8;
      if (v == 1 && r == 0) {
         b[m[1]] = 3;
      }

      if (v == 2 && r == 7) {
         b[m[1]] = 4;
      }

      return m[2] >= 0;
   }

   private int count(int who) {
      int n = 0;

      for (int v : this.b) {
         if (owner(v) == who) {
            n++;
         }
      }

      return n;
   }

   private static int eval(int[] b) {
      int s = 0;

      for (int i = 0; i < 64; i++) {
         int v = b[i];
         if (v == 2) {
            s += 10 + i / 8;
         }

         if (v == 4) {
            s += 25;
         }

         if (v == 1) {
            s -= 10 + (7 - i / 8);
         }

         if (v == 3) {
            s -= 25;
         }
      }

      return s;
   }

   private int search(int[] bd, int depth, int who, int a, int be) {
      if (depth == 0) {
         return eval(bd);
      } else {
         List<int[]> ms = moves(bd, who, -1);
         if (ms.isEmpty()) {
            return who == 2 ? -1000 : 1000;
         } else {
            for (int[] m : ms) {
               int[] c = (int[])bd.clone();
               apply(c, m);
               int v = this.search(c, depth - 1, 3 - who, a, be);
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

   private void aiMove() {
      List<int[]> ms = moves(this.b, 2, this.chainFrom);
      if (ms.isEmpty()) {
         this.endTurn();
      } else {
         int lv = this.aiLevel();
         int[] pick = ms.get(this.rnd.nextInt(ms.size()));
         if (lv > 0) {
            int best = Integer.MIN_VALUE;
            int depth = new int[]{1, 2, 4, 6}[lv];

            for (int[] m : ms) {
               int[] c = (int[])this.b.clone();
               apply(c, m);
               int v = this.search(c, depth - 1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE) + this.rnd.nextInt(2);
               if (v > best) {
                  best = v;
                  pick = m;
               }
            }
         }

         boolean cap = apply(this.b, pick);
         Sound.play("minecraft:block.wood.place", 1.2F, 0.35F);
         if (cap && !moves(this.b, 2, pick[1]).isEmpty()) {
            this.chainFrom = pick[1];
            this.think = 0.2F;
         } else {
            this.endTurn();
         }
      }
   }

   private void endTurn() {
      this.chainFrom = -1;
      this.turn = 3 - this.turn;
      this.think = 0.0F;
      this.sel = -1;
      if (this.count(1) != 0 && (!moves(this.b, 1, -1).isEmpty() || this.turn != 1)) {
         if (this.count(2) == 0 || moves(this.b, 2, -1).isEmpty() && this.turn == 2) {
            this.ended = 1;
            this.score = this.count(1);
            PackPuzzle.win();
            this.gameOver();
         }
      } else {
         this.ended = 2;
         PackPuzzle.bad();
         this.gameOver();
      }
   }

   @Override
   protected void onClick(float x, float y, int bt) {
      if (this.turn == 1) {
         int c = (int)((x - 10.0F) / 32.0F);
         int r = (int)((y - 6.0F) / 32.0F);
         if (!(x < 10.0F) && !(y < 6.0F) && c <= 7 && r <= 7) {
            int i = r * 8 + c;
            if (owner(this.b[i]) == 1 && this.chainFrom < 0) {
               this.sel = i;
            } else if (this.sel >= 0) {
               for (int[] m : moves(this.b, 1, this.chainFrom >= 0 ? this.chainFrom : -1)) {
                  if (m[0] == this.sel && m[1] == i) {
                     boolean cap = apply(this.b, m);
                     Sound.play("minecraft:block.wood.place", 1.4F, 0.35F);
                     if (cap && !moves(this.b, 1, i).isEmpty()) {
                        this.chainFrom = i;
                        this.sel = i;
                        return;
                     }

                     this.endTurn();
                     return;
                  }
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      if (this.turn == 2 && (this.think += dt) > 0.6F) {
         this.aiMove();
      }
   }

   @Override
   protected void render() {
      List<int[]> legal = this.turn == 1 ? moves(this.b, 1, this.chainFrom) : List.of();

      for (int i = 0; i < 64; i++) {
         int r = i / 8;
         int c = i % 8;
         float x = 10.0F + c * 32.0F;
         float y = 6.0F + r * 32.0F;
         this.box(x, y, 32.0F, 32.0F, 0.0F, (r + c) % 2 == 0 ? -858424 : -7710160);

         for (int[] m : legal) {
            if (m[0] == this.sel && m[1] == i) {
               this.circle(x + 16.0F, y + 16.0F, 5.0F, -1724263597);
            }
         }

         int v = this.b[i];
         if (v != 0) {
            int col = owner(v) == 1 ? -723724 : -15000798;
            this.circle(x + 16.0F, y + 16.0F + 1.5F, 12.16F, 1996488704);
            this.circle(x + 16.0F, y + 16.0F, 12.16F, i == this.sel ? -7859 : col);
            this.ring(x + 16.0F, y + 16.0F, 8.32F, 1.0F, owner(v) == 1 ? 855638016 : 872415231);
            if (king(v)) {
               this.text("♛", x + 16.0F, y + 16.0F, 10.0F, 3, -15043);
            }
         }
      }

      float sx = 286.0F;
      this.textL("Du (weiß): " + this.count(1), sx, 30.0F, 8.0F, 3, Style.text);
      this.textL("KI (schwarz): " + this.count(2), sx, 48.0F, 8.0F, 3, Style.text);
      this.textL(this.turn == 1 ? "Du bist dran" : "KI denkt...", sx, 80.0F, 8.0F, 3, this.turn == 1 ? Style.ok : Style.sub);
      this.textL("Schlagen ist Pflicht", sx, 120.0F, 6.2F, 2, Style.sub);
      this.textL("Mehrfach-Sprünge möglich", sx, 132.0F, 6.2F, 2, Style.sub);
   }
}
