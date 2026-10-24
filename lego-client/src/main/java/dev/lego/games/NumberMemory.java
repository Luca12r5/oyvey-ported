package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;

final class NumberMemory extends GameView {
   private String number = "";
   private String input = "";
   private int digits;
   private float show;
   private float flash;
   private boolean wrong;

   NumberMemory() {
      super("numbermemory");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 220.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Ziffern";
   }

   @Override
   protected String overTitle() {
      return "Richtig wäre: " + this.number;
   }

   @Override
   protected void reset() {
      this.digits = 3;
      this.next();
   }

   private void next() {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < this.digits; i++) {
         sb.append(i == 0 ? 1 + this.rnd.nextInt(9) : this.rnd.nextInt(10));
      }

      this.number = sb.toString();
      this.input = "";
      this.show = 1.2F + this.digits * 0.45F;
      this.wrong = false;
   }

   @Override
   public boolean chr(String s) {
      if (this.state == GameView.State.PLAYING && this.show <= 0.0F) {
         for (char c : s.toCharArray()) {
            if (Character.isDigit(c) && this.input.length() < 30) {
               this.input = this.input + c;
            }
         }
      }

      return true;
   }

   @Override
   protected void onKey(int k) {
      if (!(this.show > 0.0F)) {
         if (k == 259 && !this.input.isEmpty()) {
            this.input = this.input.substring(0, this.input.length() - 1);
         }

         if (k == 257) {
            if (this.input.equals(this.number)) {
               this.score = this.digits;
               this.digits++;
               this.flash = 0.6F;
               PackPuzzle.win();
               this.next();
            } else {
               this.wrong = true;
               PackPuzzle.bad();
               this.gameOver();
            }
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.show -= dt;
      this.flash = Math.max(0.0F, this.flash - dt);
   }

   @Override
   protected void render() {
      this.text("Merke dir die Zahl (" + this.digits + " Ziffern)", 190.0F, 30.0F, 8.0F, 3, Style.sub);
      if (this.show > 0.0F) {
         this.text(this.number, 190.0F, 100.0F, Math.min(28.0F, 340.0F / this.digits * 1.6F), 3, Style.text);
         this.box(40.0F, 150.0F, 300.0F * Math.max(0.0F, this.show / (1.2F + this.digits * 0.45F)), 5.0F, 2.0F, Style.accent);
      } else {
         this.box(40.0F, 80.0F, 300.0F, 44.0F, 10.0F, Style.light ? -1644306 : -14934233);
         this.text(
            this.input.isEmpty() ? "Zahl eintippen + Enter" : this.input,
            190.0F,
            102.0F,
            this.input.isEmpty() ? 9.0F : Math.min(18.0F, 300.0F / Math.max(1, this.input.length()) * 1.6F),
            3,
            this.input.isEmpty() ? Style.muted : Style.accent
         );
      }

      if (this.flash > 0.0F) {
         this.text("Richtig!", 190.0F, 170.0F, 12.0F, 3, Gx.withAlpha(-12987565, this.flash / 0.6F));
      }
   }
}
