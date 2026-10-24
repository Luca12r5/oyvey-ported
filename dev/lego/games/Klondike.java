package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class Klondike extends GameView {
   private static final String[] R = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "B", "D", "K"};
   private static final float CW = 34.0F;
   private static final float CH = 48.0F;
   private static final float GAP = 50.0F;
   private static final float TOPY = 8.0F;
   private static final float TABY = 66.0F;
   private final List<List<int[]>> tab = new ArrayList<>();
   private final List<Integer> stock = new ArrayList<>();
   private final List<Integer> waste = new ArrayList<>();
   private final int[] found = new int[4];
   private int selPile = -2;
   private int selIdx = -1;

   Klondike() {
      super("klondike");
   }

   @Override
   protected float boardW() {
      return 356.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Karten";
   }

   @Override
   protected String overTitle() {
      return this.score >= 52L ? "Gewonnen!" : "Aufgegeben";
   }

   @Override
   protected void reset() {
      List<Integer> d = new ArrayList<>();

      for (int i = 0; i < 52; i++) {
         d.add(i);
      }

      Collections.shuffle(d, this.rnd);
      this.tab.clear();
      this.stock.clear();
      this.waste.clear();
      Arrays.fill(this.found, 0);

      for (int p = 0; p < 7; p++) {
         List<int[]> pile = new ArrayList<>();

         for (int k = 0; k <= p; k++) {
            pile.add(new int[]{d.remove(0), k == p ? 1 : 0});
         }

         this.tab.add(pile);
      }

      this.stock.addAll(d);
      this.selPile = -2;
      this.score = 0L;
   }

   private static int suit(int c) {
      return c / 13;
   }

   private static int rank(int c) {
      return c % 13;
   }

   private static boolean red(int c) {
      return suit(c) >= 2;
   }

   private void drawCard(float x, float y, int c, boolean up, boolean sel) {
      if (!up) {
         this.box(x, y, 34.0F, 48.0F, 5.0F, -15054176);
         this.box(x + 3.0F, y + 3.0F, 28.0F, 42.0F, 4.0F, -13997360);
      } else {
         this.box(x, y, 34.0F, 48.0F, 5.0F, sel ? -3936 : -460552);
         Gx.outline(this.X(x), this.Y(y), this.S(34.0F), this.S(48.0F), this.S(5.0F), 1, 855638016);
         int col = red(c) ? -3141600 : -15395302;
         this.text(R[rank(c)], x + 8.0F, y + 8.0F, 7.5F, 3, col);
         this.text(PackMind.SUITS[suit(c)], x + 26.0F, y + 8.0F, 7.0F, 3, col);
         this.text(PackMind.SUITS[suit(c)], x + 17.0F, y + 31.0F, 14.0F, 3, col);
      }
   }

   private float px(int p) {
      return 4.0F + p * 50.0F;
   }

   private float step(int pile) {
      int n = this.tab.get(pile).size();
      return n <= 1 ? 16.0F : Math.min(16.0F, 182.0F / (n - 1));
   }

   private boolean canTab(int card, int pile) {
      List<int[]> t = this.tab.get(pile);
      if (t.isEmpty()) {
         return rank(card) == 12;
      } else {
         int[] top = t.get(t.size() - 1);
         return top[1] == 1 && red(top[0]) != red(card) && rank(top[0]) == rank(card) + 1;
      }
   }

   private boolean toFoundation(int card) {
      if (this.found[suit(card)] == rank(card)) {
         this.found[suit(card)]++;
         this.score++;
         Sound.play("minecraft:item.book.page_turn", 1.4F, 0.5F);
         if (this.score >= 52L) {
            PackPuzzle.win();
            this.gameOver();
         }

         return true;
      } else {
         return false;
      }
   }

   private void flipTops() {
      for (List<int[]> t : this.tab) {
         if (!t.isEmpty()) {
            t.get(t.size() - 1)[1] = 1;
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (in(x, y, this.px(0), 8.0F, 34.0F, 48.0F)) {
         if (this.stock.isEmpty()) {
            Collections.reverse(this.waste);
            this.stock.addAll(this.waste);
            this.waste.clear();
         } else {
            this.waste.add(this.stock.remove(this.stock.size() - 1));
         }

         this.selPile = -2;
         PackPuzzle.click();
      } else if (in(x, y, this.px(1), 8.0F, 34.0F, 48.0F) && !this.waste.isEmpty()) {
         int c = this.waste.get(this.waste.size() - 1);
         if ((b == 1 || this.selPile == -1) && this.toFoundation(c)) {
            this.waste.remove(this.waste.size() - 1);
            this.selPile = -2;
         } else {
            this.selPile = -1;
            this.selIdx = 0;
         }
      } else {
         for (int s = 0; s < 4; s++) {
            if (in(x, y, this.px(3 + s), 8.0F, 34.0F, 48.0F) && this.selPile != -2) {
               int c = this.selPile == -1 ? this.waste.get(this.waste.size() - 1) : this.tab.get(this.selPile).get(this.tab.get(this.selPile).size() - 1)[0];
               boolean single = this.selPile == -1 || this.selIdx == this.tab.get(this.selPile).size() - 1;
               if (single && this.toFoundation(c)) {
                  if (this.selPile == -1) {
                     this.waste.remove(this.waste.size() - 1);
                  } else {
                     this.tab.get(this.selPile).remove(this.tab.get(this.selPile).size() - 1);
                  }

                  this.flipTops();
               }

               this.selPile = -2;
               return;
            }
         }

         for (int p = 0; p < 7; p++) {
            if (!(x < this.px(p)) && !(x > this.px(p) + 34.0F)) {
               List<int[]> t = this.tab.get(p);
               int hit = -1;

               for (int i = t.size() - 1; i >= 0; i--) {
                  if (in(x, y, this.px(p), 66.0F + i * this.step(p), 34.0F, i == t.size() - 1 ? 48.0F : this.step(p))) {
                     hit = i;
                     break;
                  }
               }

               if (this.selPile != -2 && this.selPile != p) {
                  int moving = this.selPile == -1 ? this.waste.get(this.waste.size() - 1) : this.tab.get(this.selPile).get(this.selIdx)[0];
                  if (this.canTab(moving, p)) {
                     if (this.selPile == -1) {
                        t.add(new int[]{this.waste.remove(this.waste.size() - 1), 1});
                     } else {
                        List<int[]> src = this.tab.get(this.selPile);

                        while (src.size() > this.selIdx) {
                           t.add(src.remove(this.selIdx));
                        }
                     }

                     this.flipTops();
                     PackPuzzle.click();
                  }

                  this.selPile = -2;
                  return;
               }

               if (hit < 0) {
                  this.selPile = -2;
                  return;
               }

               if (t.get(hit)[1] == 0) {
                  return;
               }

               if (b == 1 && hit == t.size() - 1 && this.toFoundation(t.get(hit)[0])) {
                  t.remove(hit);
                  this.flipTops();
                  this.selPile = -2;
                  return;
               }

               this.selPile = p;
               this.selIdx = hit;
               return;
            }
         }

         this.selPile = -2;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 257) {
         this.score = Math.min(this.score, 51L);
         this.gameOver();
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, this.boardW(), this.boardH(), 8.0F, -14783686);
      if (this.stock.isEmpty()) {
         this.ring(this.px(0) + 17.0F, 32.0F, 10.0F, 1.5F, 1728053247);
      } else {
         this.drawCard(this.px(0), 8.0F, 0, false, false);
      }

      if (!this.waste.isEmpty()) {
         this.drawCard(this.px(1), 8.0F, this.waste.get(this.waste.size() - 1), true, this.selPile == -1);
      }

      for (int s = 0; s < 4; s++) {
         this.box(this.px(3 + s), 8.0F, 34.0F, 48.0F, 5.0F, 872415231);
         if (this.found[s] > 0) {
            this.drawCard(this.px(3 + s), 8.0F, s * 13 + this.found[s] - 1, true, false);
         } else {
            this.text(PackMind.SUITS[s], this.px(3 + s) + 17.0F, 32.0F, 14.0F, 3, 1442840575);
         }
      }

      for (int p = 0; p < 7; p++) {
         List<int[]> t = this.tab.get(p);
         if (t.isEmpty()) {
            this.box(this.px(p), 66.0F, 34.0F, 48.0F, 5.0F, 587202559);
         }

         for (int i = 0; i < t.size(); i++) {
            this.drawCard(this.px(p), 66.0F + i * this.step(p), t.get(i)[0], t.get(i)[1] == 1, this.selPile == p && i >= this.selIdx);
         }
      }

      this.text("Rechtsklick = auf Ablage · Enter = aufgeben", this.boardW() / 2.0F, 294.0F, 5.8F, 2, -1711276033);
   }
}
