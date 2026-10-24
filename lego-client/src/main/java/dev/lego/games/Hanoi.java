package dev.lego.games;

import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;

final class Hanoi extends GameView {
   private final List<List<Integer>> pegs = new ArrayList<>();
   private int n;
   private int moves;
   private int held = -1;
   private float doneT = -1.0F;

   Hanoi() {
      super("hanoi");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Scheiben";
   }

   @Override
   protected String overTitle() {
      return "Alle Türme geschafft!";
   }

   @Override
   protected void reset() {
      this.n = 3;
      this.setup();
      this.score = 0L;
   }

   private void setup() {
      this.pegs.clear();

      for (int i = 0; i < 3; i++) {
         this.pegs.add(new ArrayList<>());
      }

      for (int i = this.n; i >= 1; i--) {
         this.pegs.get(0).add(i);
      }

      this.moves = 0;
      this.held = -1;
      this.doneT = -1.0F;
   }

   private float px(int p) {
      return 75 + p * 135;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!(this.doneT >= 0.0F)) {
         int p = x < 142.0F ? 0 : (x < 277.0F ? 1 : 2);
         List<Integer> peg = this.pegs.get(p);
         if (this.held < 0) {
            if (!peg.isEmpty()) {
               this.held = p;
               PackPuzzle.click();
            }
         } else if (p == this.held) {
            this.held = -1;
         } else {
            List<Integer> from = this.pegs.get(this.held);
            int d = from.get(from.size() - 1);
            if (!peg.isEmpty() && peg.get(peg.size() - 1) < d) {
               PackPuzzle.bad();
               this.held = -1;
            } else {
               from.remove(from.size() - 1);
               peg.add(d);
               this.moves++;
               this.held = -1;
               PackPuzzle.click();
               if (this.pegs.get(2).size() == this.n) {
                  this.doneT = 0.0F;
                  this.score = this.n;
                  PackPuzzle.win();
               }
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      if (this.doneT >= 0.0F && (this.doneT += dt) > 1.2F) {
         if (this.n >= 7) {
            this.gameOver();
         } else {
            this.n++;
            this.setup();
         }
      }
   }

   @Override
   protected void render() {
      this.box(10.0F, 220.0F, 400.0F, 10.0F, 4.0F, -7710160);

      for (int p = 0; p < 3; p++) {
         this.box(this.px(p) - 3.0F, 70.0F, 6.0F, 150.0F, 3.0F, -5207462);
         List<Integer> peg = this.pegs.get(p);

         for (int i = 0; i < peg.size(); i++) {
            int d = peg.get(i);
            float w = 24 + d * 14;
            float y = 205 - i * 16;
            if (p == this.held && i == peg.size() - 1) {
               y = 40.0F;
            }

            this.brick(this.px(p) - w / 2.0F, y, w, 14.0F, PackPuzzle.PAL[(d - 1) % 6]);
         }
      }

      this.text(this.n + " Scheiben · Züge: " + this.moves + " (min. " + ((1 << this.n) - 1) + ")", 210.0F, 16.0F, 8.0F, 3, Style.text);
      if (this.doneT >= 0.0F) {
         this.text("Geschafft!", 210.0F, 110.0F, 16.0F, 3, Style.ok);
      }
   }
}
