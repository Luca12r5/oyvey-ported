package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class Anagram extends GameView {
   private String word;
   private String shuffled;
   private String input = "";
   private float left;
   private float flash;
   private boolean lastOk;
   private int skips;

   Anagram() {
      super("anagram");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 250.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um! Letztes Wort: " + this.word;
   }

   @Override
   protected void reset() {
      this.left = 90.0F;
      this.skips = 3;
      this.next();
   }

   private void next() {
      do {
         this.word = Words.ANY[this.rnd.nextInt(Words.ANY.length)];
      } while (this.word.length() > 9);

      List<Character> cs = new ArrayList<>();

      for (char c : this.word.toCharArray()) {
         cs.add(c);
      }

      do {
         Collections.shuffle(cs, this.rnd);
         StringBuilder sb = new StringBuilder();

         for (char c : cs) {
            sb.append(c);
         }

         this.shuffled = sb.toString();
      } while (this.shuffled.equals(this.word) && this.word.length() > 1);

      this.input = "";
   }

   @Override
   public boolean chr(String s) {
      if (this.state != GameView.State.PLAYING) {
         return true;
      } else {
         for (char c : Words.norm(s).toCharArray()) {
            if (c >= 'A' && c <= 'Z' && this.input.length() < this.word.length()) {
               this.input = this.input + c;
            }
         }

         if (this.input.equals(this.word)) {
            this.score = this.score + this.word.length();
            this.lastOk = true;
            this.flash = 0.5F;
            PackPuzzle.win();
            this.next();
         } else if (this.input.length() == this.word.length()) {
            this.lastOk = false;
            this.flash = 0.4F;
            PackPuzzle.bad();
            this.input = "";
         }

         return true;
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 259 && !this.input.isEmpty()) {
         this.input = this.input.substring(0, this.input.length() - 1);
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (in(x, y, 150, 200, 100, 26) && this.skips > 0) {
         this.skips--;
         this.next();
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
      float cw = Math.min(36.0F, 360.0F / this.shuffled.length());
      float sx = 200.0F - cw * this.shuffled.length() / 2.0F;

      for (int i = 0; i < this.shuffled.length(); i++) {
         this.brick(sx + i * cw + 2.0F, 40.0F, cw - 4.0F, cw - 4.0F, PackPuzzle.PAL[i % 6]);
         this.text(String.valueOf(this.shuffled.charAt(i)), sx + i * cw + cw / 2.0F, 40.0F + cw / 2.0F, cw * 0.45F, 3, -1);
      }

      for (int i = 0; i < this.word.length(); i++) {
         this.box(sx + i * cw + 3.0F, 130.0F, cw - 6.0F, 2.5F, 1.0F, Style.sub);
         if (i < this.input.length()) {
            this.text(String.valueOf(this.input.charAt(i)), sx + i * cw + cw / 2.0F, 116.0F, cw * 0.5F, 3, Style.text);
         }
      }

      if (this.flash > 0.0F) {
         this.text(
            this.lastOk ? "Richtig!" : "Falsch", 200.0F, 165.0F, 12.0F, 3, Gx.withAlpha(this.lastOk ? -12987565 : -50356, Math.min(1.0F, this.flash * 2.0F))
         );
      }

      if (this.skips > 0) {
         this.button("Überspringen (" + this.skips + ")", 150.0F, 200.0F, 100.0F, 26.0F, -10853776);
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 370.0F, 15.0F, 9.0F, 3, this.left < 10.0F ? Style.danger : Style.text);
   }
}
