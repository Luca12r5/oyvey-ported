package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class NumberHunt extends GameView {
   private static final int N = 5;
   private final int[] cells = new int[25];
   private int next;
   private float flash = -1.0F;
   private int flashCell;

   NumberHunt() {
      super("numberhunt");
   }

   @Override
   protected float boardW() {
      return 300.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Zeit";
   }

   @Override
   protected String scoreText() {
      return String.format(Locale.ROOT, "%.1f s", this.time);
   }

   @Override
   protected String bestText(long best) {
      return String.format(Locale.ROOT, "%.1f s", best / 10.0);
   }

   @Override
   protected String overTitle() {
      return "Fertig in " + String.format(Locale.ROOT, "%.1f", this.time) + " s";
   }

   @Override
   protected void reset() {
      List<Integer> l = new ArrayList<>();

      for (int i = 1; i <= 25; i++) {
         l.add(i);
      }

      Collections.shuffle(l, this.rnd);

      for (int i = 0; i < l.size(); i++) {
         this.cells[i] = l.get(i);
      }

      this.next = 1;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      int c = (int)((x - 10.0F) / 56.0F);
      int r = (int)((y - 10.0F) / 56.0F);
      if (c >= 0 && r >= 0 && c < 5 && r < 5) {
         int i = r * 5 + c;
         if (this.cells[i] == this.next) {
            this.next++;
            PackPuzzle.click();
            if (this.next > 25) {
               this.score = (long)(this.time * 10.0F);
               PackPuzzle.win();
               this.gameOver();
            }
         } else {
            this.flash = 0.3F;
            this.flashCell = i;
            PackPuzzle.bad();
         }
      }
   }

   @Override
   protected void update(float dt) {
      if (this.flash >= 0.0F) {
         this.flash -= dt;
      }
   }

   @Override
   protected void render() {
      for (int i = 0; i < 25; i++) {
         float x = 10 + i % 5 * 56;
         float y = 10 + i / 5 * 56;
         boolean done = this.cells[i] < this.next;
         this.box(
            x + 2.0F,
            y + 2.0F,
            52.0F,
            52.0F,
            8.0F,
            this.flash > 0.0F && i == this.flashCell ? -5234646 : (done ? Gx.withAlpha(Style.ok, 0.35F) : Style.surface)
         );
         this.text(String.valueOf(this.cells[i]), x + 28.0F, y + 29.0F, 16.0F, 3, done ? Style.muted : Style.text);
      }

      this.text("Klick 1 bis 25 der Reihe nach · nächste: " + Math.min(25, this.next), 150.0F, 296.0F, 6.4F, 3, Style.sub);
   }
}
