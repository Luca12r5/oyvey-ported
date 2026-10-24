package dev.lego.games;

import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Mill extends GameView {
   private static final float[][] P = new float[24][];
   private static final int[][] ADJ = new int[24][];
   private static final int[][] MILLS = new int[][]{
      {0, 1, 2},
      {3, 4, 5},
      {6, 7, 8},
      {9, 10, 11},
      {12, 13, 14},
      {15, 16, 17},
      {18, 19, 20},
      {21, 22, 23},
      {0, 9, 21},
      {3, 10, 18},
      {6, 11, 15},
      {1, 4, 7},
      {16, 19, 22},
      {8, 12, 17},
      {5, 13, 20},
      {2, 14, 23}
   };
   private int[] b = new int[24];
   private int[] hand = new int[3];
   private int[] onBoard = new int[3];
   private int turn;
   private int sel = -1;
   private int ended;
   private boolean removing;
   private float think;

   Mill() {
      super("mill");
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
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return this.ended == 1 ? "Gewonnen!" : "Verloren";
   }

   @Override
   protected void reset() {
      this.b = new int[24];
      this.hand = new int[]{0, 9, 9};
      this.onBoard = new int[3];
      this.turn = 1;
      this.sel = -1;
      this.removing = false;
      this.ended = 0;
      this.think = 0.0F;
   }

   private boolean inMill(int[] bd, int p) {
      for (int[] m : MILLS) {
         if ((m[0] == p || m[1] == p || m[2] == p) && bd[m[0]] != 0 && bd[m[0]] == bd[m[1]] && bd[m[1]] == bd[m[2]]) {
            return true;
         }
      }

      return false;
   }

   private boolean canFly(int who) {
      return this.hand[who] == 0 && this.onBoard[who] == 3;
   }

   private List<int[]> moves(int who) {
      List<int[]> l = new ArrayList<>();
      if (this.hand[who] > 0) {
         for (int i = 0; i < 24; i++) {
            if (this.b[i] == 0) {
               l.add(new int[]{-1, i});
            }
         }

         return l;
      } else {
         for (int ix = 0; ix < 24; ix++) {
            if (this.b[ix] == who) {
               if (this.canFly(who)) {
                  for (int j = 0; j < 24; j++) {
                     if (this.b[j] == 0) {
                        l.add(new int[]{ix, j});
                     }
                  }
               } else {
                  for (int jx : ADJ[ix]) {
                     if (this.b[jx] == 0) {
                        l.add(new int[]{ix, jx});
                     }
                  }
               }
            }
         }

         return l;
      }
   }

   private List<Integer> removable(int victim) {
      List<Integer> all = new ArrayList<>();
      List<Integer> free = new ArrayList<>();

      for (int i = 0; i < 24; i++) {
         if (this.b[i] == victim) {
            all.add(i);
            if (!this.inMill(this.b, i)) {
               free.add(i);
            }
         }
      }

      return free.isEmpty() ? all : free;
   }

   private boolean doMove(int[] m, int who) {
      if (m[0] < 0) {
         this.hand[who]--;
         this.onBoard[who]++;
      } else {
         this.b[m[0]] = 0;
      }

      this.b[m[1]] = who;
      Sound.play("minecraft:block.stone.place", 1.3F, 0.35F);
      return this.inMill(this.b, m[1]);
   }

   private void remove(int p, int victim) {
      this.b[p] = 0;
      this.onBoard[victim]--;
      Sound.play("minecraft:entity.item.pickup", 0.8F, 0.4F);
   }

   private void endTurn() {
      this.turn = 3 - this.turn;
      this.think = 0.0F;
      this.sel = -1;

      for (int w = 1; w <= 2; w++) {
         if (this.hand[w] == 0 && this.onBoard[w] < 3 || this.turn == w && this.moves(w).isEmpty()) {
            this.ended = 3 - w;
            this.score = this.ended == 1 ? this.onBoard[1] * 10L : 0L;
            if (this.ended == 1) {
               PackPuzzle.win();
            } else {
               PackPuzzle.bad();
            }

            this.gameOver();
            return;
         }
      }
   }

   private int scoreMove(int[] m) {
      int[] c = (int[])this.b.clone();
      if (m[0] >= 0) {
         c[m[0]] = 0;
      }

      c[m[1]] = 2;
      int s = this.inMill(c, m[1]) ? 100 : 0;
      int[] d = (int[])this.b.clone();
      if (m[0] >= 0) {
         d[m[0]] = 0;
      }

      d[m[1]] = 1;
      if (this.inMill(d, m[1])) {
         s += 60;
      }

      for (int[] mill : MILLS) {
         int mine = 0;
         int empty = 0;

         for (int p : mill) {
            if (c[p] == 2) {
               mine++;
            }

            if (c[p] == 0) {
               empty++;
            }
         }

         if (mine == 2 && empty == 1) {
            s += 15;
         }
      }

      return s + this.rnd.nextInt(5);
   }

   @Override
   protected void update(float dt) {
      if (this.turn == 2 && !((this.think += dt) < 0.7F)) {
         List<int[]> ms = this.moves(2);
         if (ms.isEmpty()) {
            this.endTurn();
         } else {
            int lv = this.aiLevel();
            int[] best = ms.get(this.rnd.nextInt(ms.size()));
            if (lv > 0 || this.rnd.nextBoolean()) {
               int bs = -1;

               for (int[] m : ms) {
                  int s = this.scoreMove(m) * (lv == 0 ? 0 : 1) + (lv == 1 ? this.rnd.nextInt(40) : 0);
                  if (s > bs) {
                     bs = s;
                     best = m;
                  }
               }
            }

            if (this.doMove(best, 2)) {
               List<Integer> rem = this.removable(1);
               int pick = rem.get(this.rnd.nextInt(rem.size()));
               if (lv >= 2) {
                  for (int p : rem) {
                     for (int[] mill : MILLS) {
                        int mine = 0;

                        for (int q : mill) {
                           if (this.b[q] == 1) {
                              mine++;
                           }
                        }

                        if (mine == 2 && (mill[0] == p || mill[1] == p || mill[2] == p)) {
                           pick = p;
                        }
                     }
                  }
               }

               this.remove(pick, 1);
            }

            this.endTurn();
         }
      }
   }

   private int at(float x, float y) {
      for (int i = 0; i < 24; i++) {
         if (Math.hypot(x - P[i][0], y - P[i][1]) < 12.0) {
            return i;
         }
      }

      return -1;
   }

   @Override
   protected void onClick(float x, float y, int bt) {
      if (this.turn == 1) {
         int p = this.at(x, y);
         if (p >= 0) {
            if (this.removing) {
               if (this.removable(2).contains(p)) {
                  this.remove(p, 2);
                  this.removing = false;
                  this.endTurn();
               }
            } else if (this.hand[1] > 0) {
               if (this.b[p] == 0) {
                  if (this.doMove(new int[]{-1, p}, 1)) {
                     this.removing = true;
                  } else {
                     this.endTurn();
                  }
               }
            } else if (this.b[p] == 1) {
               this.sel = p;
            } else {
               if (this.sel >= 0 && this.b[p] == 0) {
                  for (int[] m : this.moves(1)) {
                     if (m[0] == this.sel && m[1] == p) {
                        if (this.doMove(m, 1)) {
                           this.removing = true;
                        } else {
                           this.endTurn();
                        }

                        this.sel = -1;
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      int lc = Style.light ? -10862560 : -3628960;

      for (int[] m : MILLS) {
         this.line(P[m[0]][0], P[m[0]][1], P[m[2]][0], P[m[2]][1], 1.6F, lc);
      }

      for (int i = 0; i < 24; i++) {
         this.circle(P[i][0], P[i][1], 3.0F, lc);
         if (this.b[i] != 0) {
            this.circle(P[i][0], P[i][1] + 1.5F, 10.0F, 1711276032);
            this.circle(P[i][0], P[i][1], 10.0F, i == this.sel ? -7859 : (this.b[i] == 1 ? -723724 : -15000798));
         }

         if (this.removing && this.b[i] == 2 && this.removable(2).contains(i)) {
            this.ring(P[i][0], P[i][1], 12.0F, 1.5F, -50356);
         }
      }

      float sx = 290.0F;
      this.textL("Du (weiß)", sx, 30.0F, 8.0F, 3, Style.text);
      this.textL("Hand " + this.hand[1] + " · Brett " + this.onBoard[1], sx, 44.0F, 6.6F, 2, Style.sub);
      this.textL("KI (schwarz)", sx, 70.0F, 8.0F, 3, Style.text);
      this.textL("Hand " + this.hand[2] + " · Brett " + this.onBoard[2], sx, 84.0F, 6.6F, 2, Style.sub);
      String st = this.turn != 1
         ? "KI ist dran..."
         : (
            this.removing
               ? "Mühle! Nimm einen Stein"
               : (this.hand[1] > 0 ? "Setze einen Stein" : (this.canFly(1) ? "Du darfst springen" : "Ziehe einen Stein"))
         );
      this.textL(st, sx, 120.0F, 7.0F, 3, this.removing ? Style.danger : Style.ok);
      this.textL("3 in einer Linie = Mühle", sx, 160.0F, 6.0F, 2, Style.muted);
   }

   static {
      float[] xs = new float[]{20.0F, 50.0F, 80.0F};
      float[] size = new float[]{200.0F, 140.0F, 80.0F};
      int k = 0;
      float[][] pts = new float[][]{
         {0.0F, 0.0F},
         {3.0F, 0.0F},
         {6.0F, 0.0F},
         {1.0F, 1.0F},
         {3.0F, 1.0F},
         {5.0F, 1.0F},
         {2.0F, 2.0F},
         {3.0F, 2.0F},
         {4.0F, 2.0F},
         {0.0F, 3.0F},
         {1.0F, 3.0F},
         {2.0F, 3.0F},
         {4.0F, 3.0F},
         {5.0F, 3.0F},
         {6.0F, 3.0F},
         {2.0F, 4.0F},
         {3.0F, 4.0F},
         {4.0F, 4.0F},
         {1.0F, 5.0F},
         {3.0F, 5.0F},
         {5.0F, 5.0F},
         {0.0F, 6.0F},
         {3.0F, 6.0F},
         {6.0F, 6.0F}
      };

      for (float[] p : pts) {
         P[k++] = new float[]{20.0F + p[0] * 38.0F, 14.0F + p[1] * 38.0F};
      }

      List<int[]> edges = new ArrayList<>();
      int[][] lines = new int[][]{
         {0, 1, 2},
         {3, 4, 5},
         {6, 7, 8},
         {9, 10, 11},
         {12, 13, 14},
         {15, 16, 17},
         {18, 19, 20},
         {21, 22, 23},
         {0, 9, 21},
         {3, 10, 18},
         {6, 11, 15},
         {1, 4, 7},
         {16, 19, 22},
         {8, 12, 17},
         {5, 13, 20},
         {2, 14, 23}
      };

      for (int[] l : lines) {
         edges.add(new int[]{l[0], l[1]});
         edges.add(new int[]{l[1], l[2]});
      }

      for (int i = 0; i < 24; i++) {
         List<Integer> a = new ArrayList<>();

         for (int[] e : edges) {
            if (e[0] == i) {
               a.add(e[1]);
            }

            if (e[1] == i) {
               a.add(e[0]);
            }
         }

         ADJ[i] = a.stream().mapToInt(Integer::intValue).toArray();
      }

      if (xs.length + size.length < 0) {
         throw new IllegalStateException();
      }
   }
}
