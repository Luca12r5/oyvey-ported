package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.Locale;

final class MathQuiz extends GameView {
   private int a;
   private int b;
   private int op;
   private int answer;
   private String input = "";
   private float left;
   private float flash;
   private boolean lastOk;
   private int streak;

   MathQuiz() {
      super("mathquiz");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 250.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.left = 60.0F;
      this.streak = 0;
      this.next();
   }

   private void next() {
      int lvl = (int)Math.min(4L, this.score / 5L);
      this.op = this.rnd.nextInt(lvl >= 2 ? 4 : 2);
      int max = 10 + lvl * 15;
      switch (this.op) {
         case 0:
            this.a = this.rnd.nextInt(max) + 1;
            this.b = this.rnd.nextInt(max) + 1;
            this.answer = this.a + this.b;
            break;
         case 1:
            this.a = this.rnd.nextInt(max) + 1;
            this.b = this.rnd.nextInt(this.a) + 1;
            this.answer = this.a - this.b;
            break;
         case 2:
            this.a = this.rnd.nextInt(8 + lvl * 2) + 2;
            this.b = this.rnd.nextInt(9) + 2;
            this.answer = this.a * this.b;
            break;
         default:
            this.b = this.rnd.nextInt(9) + 2;
            this.answer = this.rnd.nextInt(10) + 1;
            this.a = this.b * this.answer;
      }

      this.input = "";
   }

   @Override
   public boolean chr(String s) {
      if (this.state == GameView.State.PLAYING) {
         for (char c : s.toCharArray()) {
            if (Character.isDigit(c) && this.input.length() < 5) {
               this.input = this.input + c;
            }
         }
      }

      return true;
   }

   @Override
   protected void onKey(int k) {
      if (k == 259 && !this.input.isEmpty()) {
         this.input = this.input.substring(0, this.input.length() - 1);
      }

      if (k == 257 && !this.input.isEmpty()) {
         this.lastOk = Integer.parseInt(this.input) == this.answer;
         this.flash = 0.4F;
         if (this.lastOk) {
            this.streak++;
            this.score++;
            this.left++;
            PackPuzzle.good();
         } else {
            this.streak = 0;
            this.left -= 3.0F;
            PackPuzzle.bad();
         }

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
      String[] ops = new String[]{"+", "−", "×", "÷"};
      this.text(this.a + " " + ops[this.op] + " " + this.b + " =", 190.0F, 80.0F, 26.0F, 3, Style.text);
      this.box(
         130.0F, 120.0F, 120.0F, 44.0F, 10.0F, this.flash > 0.0F ? Gx.withAlpha(this.lastOk ? -12987565 : -50356, 0.4F) : (Style.light ? -1644306 : -14934233)
      );
      this.text(this.input.isEmpty() ? "?" : this.input, 190.0F, 143.0F, 20.0F, 3, Style.accent);
      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 340.0F, 20.0F, 10.0F, 3, this.left < 10.0F ? Style.danger : Style.text);
      this.text("Serie: " + this.streak + "   (richtig +1 s, falsch −3 s)", 190.0F, 200.0F, 7.0F, 2, Style.sub);
      this.text("Zahl tippen, Enter", 190.0F, 222.0F, 6.6F, 1, Style.muted);
   }
}
