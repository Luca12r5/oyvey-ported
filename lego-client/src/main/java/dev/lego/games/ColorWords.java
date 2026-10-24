package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.Locale;

final class ColorWords extends GameView {
   private static final String[] NAMES = new String[]{"ROT", "BLAU", "GELB", "GRÜN"};
   private static final int[] COLS = new int[]{-1900533, -13728034, -13053, -14176672};
   private int word;
   private int ink;
   private float left;
   private float flash;
   private boolean lastOk;

   ColorWords() {
      super("colorwords");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.left = 30.0F;
      this.next();
   }

   private void next() {
      this.word = this.rnd.nextInt(4);
      this.ink = this.rnd.nextInt(4);
      if (this.rnd.nextFloat() < 0.75F) {
         while (this.ink == this.word) {
            this.ink = this.rnd.nextInt(4);
         }
      }
   }

   private void answer(int i) {
      this.lastOk = i == this.ink;
      this.flash = 0.3F;
      if (this.lastOk) {
         this.score++;
         PackPuzzle.good();
      } else {
         this.score = Math.max(0L, this.score - 1L);
         PackPuzzle.bad();
      }

      this.next();
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < 4; i++) {
         if (in(x, y, 20 + i * 88, 190, 80, 40)) {
            this.answer(i);
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k >= 49 && k <= 52) {
         this.answer(k - 49);
      }
   }

   @Override
   protected void update(float dt) {
      this.flash = Math.max(0.0F, this.flash - dt);
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.text("Welche FARBE hat das Wort?", 190.0F, 30.0F, 8.0F, 3, Style.sub);
      this.text(NAMES[this.word], 190.0F, 100.0F, 34.0F, 3, COLS[this.ink]);
      if (this.flash > 0.0F) {
         this.text(this.lastOk ? "✔" : "✖", 330.0F, 100.0F, 20.0F, 3, Gx.withAlpha(this.lastOk ? -12987565 : -50356, this.flash / 0.3F));
      }

      for (int i = 0; i < 4; i++) {
         this.button(i + 1 + "  " + NAMES[i], 20 + i * 88, 190.0F, 80.0F, 40.0F, Gx.mix(COLS[i], -16777216, 0.2F));
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 350.0F, 20.0F, 9.0F, 3, this.left < 6.0F ? Style.danger : Style.text);
   }
}
