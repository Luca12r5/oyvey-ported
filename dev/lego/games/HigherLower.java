package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;

final class HigherLower extends GameView {
   private int cur;
   private int curS;
   private int next;
   private int nextS;
   private int streak;
   private float reveal = -1.0F;
   private boolean ok;

   HigherLower() {
      super("higherlower");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Serie";
   }

   @Override
   protected String overTitle() {
      return "Falsch geraten!";
   }

   @Override
   protected void reset() {
      this.cur = this.rnd.nextInt(13);
      this.curS = this.rnd.nextInt(4);
      this.streak = 0;
      this.reveal = -1.0F;
   }

   private void guess(boolean higher) {
      if (!(this.reveal >= 0.0F)) {
         do {
            this.next = this.rnd.nextInt(13);
            this.nextS = this.rnd.nextInt(4);
         } while (this.next == this.cur && this.nextS == this.curS);

         this.ok = this.next == this.cur || (higher ? this.next > this.cur : this.next < this.cur);
         this.reveal = 0.0F;
         if (this.ok) {
            this.streak++;
            this.score = this.streak;
            PackPuzzle.good();
         } else {
            PackPuzzle.bad();
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (in(x, y, 220, 60, 120, 36)) {
         this.guess(true);
      }

      if (in(x, y, 220, 110, 120, 36)) {
         this.guess(false);
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 265 || k == 87) {
         this.guess(true);
      }

      if (k == 264 || k == 83) {
         this.guess(false);
      }
   }

   @Override
   protected void update(float dt) {
      if (this.reveal >= 0.0F && (this.reveal += dt) > 1.0F) {
         if (!this.ok) {
            this.gameOver();
            return;
         }

         this.cur = this.next;
         this.curS = this.nextS;
         this.reveal = -1.0F;
      }
   }

   @Override
   protected void render() {
      Gx.push();
      Gx.scaleAt(this.X(57.0F), this.Y(94.0F), 2.0F);
      PackMind.card(this, 40.0F, 70.0F, this.curS, this.cur, true, false);
      Gx.pop();
      if (this.reveal >= 0.0F) {
         Gx.push();
         Gx.scaleAt(this.X(157.0F), this.Y(94.0F), 1.6F);
         PackMind.card(this, 140.0F, 70.0F, this.nextS, this.next, true, false);
         Gx.pop();
         this.text(this.ok ? "✔" : "✖", 157.0F, 150.0F, 18.0F, 3, this.ok ? -12987565 : -50356);
      }

      this.button("▲ Höher", 220.0F, 60.0F, 120.0F, 36.0F, -13722038);
      this.button("▼ Tiefer", 220.0F, 110.0F, 120.0F, 36.0F, -5234646);
      this.text("Serie: " + this.streak, 280.0F, 180.0F, 10.0F, 3, Style.text);
      this.text("Gleiche Karte zählt als richtig · Ass ist hoch", 190.0F, 222.0F, 6.4F, 2, Style.muted);
   }
}
