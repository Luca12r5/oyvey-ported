package dev.lego.games;

import dev.lego.ui.Style;
import java.util.HashSet;
import java.util.Set;

final class Hangman extends GameView {
   private String word;
   private final Set<Character> tried = new HashSet<>();
   private int wrong;
   private int streak;
   private float nextT = -1.0F;

   Hangman() {
      super("hangman");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Wörter";
   }

   @Override
   protected String overTitle() {
      return "Gehängt! Wort: " + this.word;
   }

   @Override
   protected void reset() {
      this.streak = 0;
      this.next();
   }

   private void next() {
      this.word = Words.ANY[this.rnd.nextInt(Words.ANY.length)];
      this.tried.clear();
      this.wrong = 0;
      this.nextT = -1.0F;
   }

   private boolean solved() {
      for (char c : this.word.toCharArray()) {
         if (!this.tried.contains(c)) {
            return false;
         }
      }

      return true;
   }

   private void guess(char c) {
      if (!(this.nextT >= 0.0F) && c >= 'A' && c <= 'Z' && this.tried.add(c)) {
         if (this.word.indexOf(c) < 0) {
            this.wrong++;
            PackPuzzle.bad();
            if (this.wrong >= 8) {
               this.score = this.streak;
               this.gameOver();
            }
         } else {
            PackPuzzle.good();
            if (this.solved()) {
               this.streak++;
               this.score = this.streak;
               this.nextT = 0.0F;
               PackPuzzle.win();
            }
         }
      }
   }

   @Override
   public boolean chr(String s) {
      if (this.state == GameView.State.PLAYING) {
         for (char ch : Words.norm(s).toCharArray()) {
            this.guess(ch);
         }
      }

      return true;
   }

   @Override
   protected void update(float dt) {
      if (this.nextT >= 0.0F && (this.nextT += dt) > 1.4F) {
         this.next();
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < 26; i++) {
         if (in(x, y, 20 + i % 13 * 30, 220 + i / 13 * 32, 26, 26)) {
            this.guess((char)(65 + i));
         }
      }
   }

   @Override
   protected void render() {
      int st = Style.text;
      if (this.wrong > 0) {
         this.line(30.0F, 190.0F, 110.0F, 190.0F, 3.0F, st);
      }

      if (this.wrong > 1) {
         this.line(50.0F, 190.0F, 50.0F, 30.0F, 3.0F, st);
      }

      if (this.wrong > 2) {
         this.line(50.0F, 30.0F, 120.0F, 30.0F, 3.0F, st);
      }

      if (this.wrong > 3) {
         this.line(120.0F, 30.0F, 120.0F, 50.0F, 2.0F, st);
      }

      if (this.wrong > 4) {
         this.ring(120.0F, 62.0F, 12.0F, 2.0F, st);
      }

      if (this.wrong > 5) {
         this.line(120.0F, 74.0F, 120.0F, 120.0F, 2.4F, st);
      }

      if (this.wrong > 6) {
         this.line(120.0F, 85.0F, 102.0F, 105.0F, 2.0F, st);
         this.line(120.0F, 85.0F, 138.0F, 105.0F, 2.0F, st);
      }

      if (this.wrong > 7) {
         this.line(120.0F, 120.0F, 104.0F, 150.0F, 2.0F, st);
         this.line(120.0F, 120.0F, 136.0F, 150.0F, 2.0F, st);
      }

      float cw = Math.min(22.0F, 250.0F / this.word.length());
      float sx = 160.0F + (250.0F - cw * this.word.length()) / 2.0F;

      for (int i = 0; i < this.word.length(); i++) {
         char c = this.word.charAt(i);
         float x = sx + i * cw;
         this.box(x + 2.0F, 120.0F, cw - 4.0F, 2.5F, 1.0F, Style.sub);
         boolean show = this.tried.contains(c) || this.state == GameView.State.OVER;
         if (show) {
            this.text(String.valueOf(c), x + cw / 2.0F, 108.0F, cw * 0.62F, 3, this.tried.contains(c) ? st : Style.danger);
         }
      }

      this.text("Fehler: " + this.wrong + " / 8", 285.0F, 40.0F, 8.0F, 3, this.wrong >= 6 ? Style.danger : Style.sub);
      this.text("Serie: " + this.streak, 285.0F, 58.0F, 8.0F, 3, Style.sub);
      if (this.nextT >= 0.0F) {
         this.text("Richtig!", 285.0F, 160.0F, 12.0F, 3, Style.ok);
      }

      for (int ix = 0; ix < 26; ix++) {
         char c = (char)(65 + ix);
         float x = 20 + ix % 13 * 30;
         float y = 220 + ix / 13 * 32;
         boolean t = this.tried.contains(c);
         int col = !t ? (Style.light ? -3091236 : -14012872) : (this.word.indexOf(c) >= 0 ? -16743125 : -10872288);
         this.box(x, y, 26.0F, 26.0F, 6.0F, col);
         this.text(String.valueOf(c), x + 13.0F, y + 14.0F, 9.0F, 3, t ? -1426063361 : st);
      }
   }
}
