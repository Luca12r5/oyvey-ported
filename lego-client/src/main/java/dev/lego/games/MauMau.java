package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MauMau extends GameView {
   private final List<int[]> deck = new ArrayList<>();
   private final List<int[]> pile = new ArrayList<>();
   private final List<int[]> me = new ArrayList<>();
   private final List<int[]> ai = new ArrayList<>();
   private int turn;
   private int wish = -1;
   private int draw2;
   private boolean choosing;
   private boolean skipNext;
   private float think;
   private String msg = "";
   private int wins;

   MauMau() {
      super("maumau");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Siege";
   }

   @Override
   protected String overTitle() {
      return this.me.isEmpty() ? "Mau-Mau! Gewonnen" : "Die KI hat gewonnen";
   }

   @Override
   protected void reset() {
      this.deck.clear();
      this.pile.clear();
      this.me.clear();
      this.ai.clear();

      for (int s = 0; s < 4; s++) {
         for (int r = 5; r <= 12; r++) {
            this.deck.add(new int[]{s, r});
         }
      }

      Collections.shuffle(this.deck, this.rnd);

      for (int i = 0; i < 5; i++) {
         this.me.add(this.deck.remove(0));
         this.ai.add(this.deck.remove(0));
      }

      int[] top;
      do {
         top = this.deck.remove(0);
         if (top[1] == 9 || top[1] == 5 || top[1] == 6) {
            this.deck.add(top);
         }
      } while (top[1] == 9 || top[1] == 5 || top[1] == 6);

      this.pile.add(top);
      this.turn = 1;
      this.wish = -1;
      this.draw2 = 0;
      this.choosing = false;
      this.think = 0.0F;
      this.msg = "";
      this.score = this.wins;
   }

   private int[] top() {
      return this.pile.get(this.pile.size() - 1);
   }

   private boolean fits(int[] c) {
      if (this.draw2 > 0) {
         return c[1] == 5;
      } else if (c[1] == 9) {
         return true;
      } else {
         int suit = this.wish >= 0 ? this.wish : this.top()[0];
         return c[0] == suit || this.wish < 0 && c[1] == this.top()[1];
      }
   }

   private int[] drawCard() {
      if (this.deck.isEmpty()) {
         int[] t = this.pile.remove(this.pile.size() - 1);
         this.deck.addAll(this.pile);
         this.pile.clear();
         this.pile.add(t);
         Collections.shuffle(this.deck, this.rnd);
      }

      return this.deck.isEmpty() ? null : this.deck.remove(0);
   }

   private void play(List<int[]> hand, int[] c, int who) {
      hand.remove(c);
      this.pile.add(c);
      this.wish = -1;
      Sound.play("minecraft:item.book.page_turn", 1.2F, 0.5F);
      if (hand.isEmpty()) {
         if (who == 1) {
            this.wins++;
            this.score = this.wins;
            PackPuzzle.win();
         } else {
            PackPuzzle.bad();
         }

         this.gameOver();
      } else {
         if (c[1] == 5) {
            this.draw2 += 2;
         }

         if (c[1] == 6) {
            this.skipNext = true;
         }

         if (c[1] == 9) {
            if (who == 1) {
               this.choosing = true;
               return;
            }

            int[] cnt = new int[4];

            for (int[] k : this.ai) {
               cnt[k[0]]++;
            }

            int best = 0;

            for (int s = 1; s < 4; s++) {
               if (cnt[s] > cnt[best]) {
                  best = s;
               }
            }

            this.wish = best;
            this.msg = "KI wünscht sich " + PackMind.SUIT_NAMES[best];
         }

         this.endTurn();
      }
   }

   private void endTurn() {
      if (this.skipNext) {
         this.skipNext = false;
         this.msg = this.turn == 1 ? "KI muss aussetzen" : "Du musst aussetzen";
         this.think = 0.0F;
      } else {
         this.turn = 3 - this.turn;
         this.think = 0.0F;
      }
   }

   private void takePenalty(List<int[]> hand) {
      int n = Math.max(1, this.draw2);

      for (int i = 0; i < n; i++) {
         int[] c = this.drawCard();
         if (c != null) {
            hand.add(c);
         }
      }

      this.draw2 = 0;
   }

   @Override
   protected void update(float dt) {
      if (this.turn == 2 && !this.choosing) {
         if (!((this.think += dt) < 0.9F)) {
            List<int[]> ok = new ArrayList<>();

            for (int[] c : this.ai) {
               if (this.fits(c)) {
                  ok.add(c);
               }
            }

            if (ok.isEmpty()) {
               this.takePenalty(this.ai);
               this.msg = "KI zieht";
               this.endTurn();
            } else {
               int lv = this.aiLevel();
               int[] pick = ok.get(this.rnd.nextInt(ok.size()));
               if (lv >= 2) {
                  for (int[] cx : ok) {
                     if (cx[1] != 9) {
                        pick = cx;
                        if (cx[1] == 5 || cx[1] == 6) {
                           break;
                        }
                     }
                  }
               }

               this.play(this.ai, pick, 2);
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1) {
         if (this.choosing) {
            for (int s = 0; s < 4; s++) {
               if (in(x, y, 100 + s * 56, 120, 50, 40)) {
                  this.wish = s;
                  this.choosing = false;
                  this.msg = "Du wünschst dir " + PackMind.SUIT_NAMES[s];
                  this.endTurn();
               }
            }
         } else if (in(x, y, 250, 110, 34, 48)) {
            this.takePenalty(this.me);
            this.msg = "";
            this.endTurn();
         } else {
            float w = Math.min(38.0F, 380.0F / Math.max(1, this.me.size()));

            for (int i = this.me.size() - 1; i >= 0; i--) {
               if (in(x, y, 20.0F + i * w, 220.0F, 34.0F, 48.0F)) {
                  int[] c = this.me.get(i);
                  if (this.fits(c)) {
                     this.play(this.me, c, 1);
                  } else {
                     PackPuzzle.bad();
                  }

                  return;
               }
            }
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 420.0F, 290.0F, 10.0F, -14783686);

      for (int i = 0; i < this.ai.size(); i++) {
         PackMind.card(this, 20.0F + i * Math.min(30.0F, 380.0F / this.ai.size()), 10.0F, 0, 0, false, false);
      }

      PackMind.card(this, 190.0F, 110.0F, this.top()[0], this.top()[1], true, false);
      PackMind.card(this, 250.0F, 110.0F, 0, 0, false, false);
      this.text("ziehen", 267.0F, 166.0F, 6.0F, 2, -855638017);
      if (this.wish >= 0) {
         this.text("Wunsch: " + PackMind.SUITS[this.wish] + " " + PackMind.SUIT_NAMES[this.wish], 100.0F, 134.0F, 8.0F, 3, -7859);
      }

      if (this.draw2 > 0) {
         this.text("+" + this.draw2 + " ziehen!", 100.0F, 150.0F, 8.0F, 3, -38294);
      }

      float w = Math.min(38.0F, 380.0F / Math.max(1, this.me.size()));

      for (int i = 0; i < this.me.size(); i++) {
         int[] c = this.me.get(i);
         boolean hv = this.turn == 1 && in(this.bmx(), this.bmy(), 20.0F + i * w, 220.0F, Math.min(w, 34.0F), 48.0F);
         PackMind.card(this, 20.0F + i * w, hv ? 212.0F : 220.0F, c[0], c[1], true, this.turn == 1 && this.fits(c));
      }

      this.text(this.turn == 1 ? (this.choosing ? "Wähle eine Farbe" : "Du bist dran") : "KI überlegt...", 340.0F, 134.0F, 8.0F, 3, -1);
      if (!this.msg.isEmpty()) {
         this.text(this.msg, 340.0F, 152.0F, 6.5F, 2, -855638017);
      }

      if (this.choosing) {
         this.box(90.0F, 110.0F, 240.0F, 60.0F, 10.0F, -300936168);

         for (int s = 0; s < 4; s++) {
            this.button(PackMind.SUITS[s], 100 + s * 56, 120.0F, 50.0F, 40.0F, s >= 2 ? -5234646 : -14012872);
         }
      }

      this.text("7 = +2 · 8 = Aussetzen · Bube = Wünschen", 210.0F, 200.0F, 6.0F, 2, -1426063361);
   }
}
