package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class Make24 extends GameView {
   private final List<Double> nums = new ArrayList<>();
   private final List<String> labels = new ArrayList<>();
   private int sel = -1;
   private int op = -1;
   private int solved;
   private float left;
   private float flash;
   private int[] start;

   Make24() {
      super("make24");
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
   protected String scoreLabel() {
      return "Gelöst";
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.solved = 0;
      this.left = 120.0F;
      this.deal();
   }

   private static boolean solvable(List<Double> n) {
      if (n.size() == 1) {
         return Math.abs(n.get(0) - 24.0) < 1.0E-6;
      } else {
         for (int i = 0; i < n.size(); i++) {
            for (int j = 0; j < n.size(); j++) {
               if (i != j) {
                  List<Double> rest = new ArrayList<>();

                  for (int k = 0; k < n.size(); k++) {
                     if (k != i && k != j) {
                        rest.add(n.get(k));
                     }
                  }

                  double a = n.get(i);
                  double b = n.get(j);
                  double[] cand = new double[]{a + b, a - b, a * b, b != 0.0 ? a / b : Double.NaN};

                  for (double v : cand) {
                     if (!Double.isNaN(v)) {
                        rest.add(v);
                        if (solvable(rest)) {
                           return true;
                        }

                        rest.remove(rest.size() - 1);
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   private void deal() {
      List<Double> n;
      do {
         n = new ArrayList<>();
         this.start = new int[4];

         for (int i = 0; i < 4; i++) {
            this.start[i] = 1 + this.rnd.nextInt(9);
            n.add((double)this.start[i]);
         }
      } while (!solvable(n));

      this.restart();
   }

   private void restart() {
      this.nums.clear();
      this.labels.clear();

      for (int v : this.start) {
         this.nums.add((double)v);
         this.labels.add(String.valueOf(v));
      }

      this.sel = -1;
      this.op = -1;
   }

   private String fmt(double v) {
      return Math.abs(v - Math.rint(v)) < 1.0E-9 ? String.valueOf((long)Math.rint(v)) : String.format(Locale.ROOT, "%.2f", v);
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < this.nums.size(); i++) {
         if (in(x, y, 40 + i * 85, 60, 70, 70)) {
            if (this.sel >= 0 && this.op >= 0 && i != this.sel) {
               double a = this.nums.get(this.sel);
               double c = this.nums.get(i);
               double r = this.op == 0 ? a + c : (this.op == 1 ? a - c : (this.op == 2 ? a * c : (c == 0.0 ? Double.NaN : a / c)));
               if (Double.isNaN(r)) {
                  return;
               }

               int keep = Math.min(this.sel, i);
               int drop = Math.max(this.sel, i);
               this.nums.set(keep, r);
               this.labels.set(keep, this.fmt(r));
               this.nums.remove(drop);
               this.labels.remove(drop);
               this.sel = keep;
               this.op = -1;
               PackPuzzle.click();
               if (this.nums.size() == 1) {
                  if (Math.abs(r - 24.0) < 1.0E-6) {
                     this.solved++;
                     this.score = this.solved;
                     this.flash = 0.8F;
                     PackPuzzle.win();
                     this.deal();
                  } else {
                     PackPuzzle.bad();
                     this.restart();
                  }
               }

               return;
            }

            this.sel = i;
            this.op = -1;
            PackPuzzle.click();
            return;
         }
      }

      for (int k = 0; k < 4; k++) {
         if (in(x, y, 70 + k * 70, 150, 50, 36) && this.sel >= 0) {
            this.op = k;
            PackPuzzle.click();
         }
      }

      if (in(x, y, 70, 200, 110, 28)) {
         this.restart();
      }

      if (in(x, y, 220, 200, 110, 28)) {
         this.left -= 10.0F;
         this.deal();
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
      this.text("Mach aus den Zahlen genau 24", 200.0F, 20.0F, 9.0F, 3, Style.text);

      for (int i = 0; i < this.nums.size(); i++) {
         this.brick(40 + i * 85, 60.0F, 70.0F, 70.0F, i == this.sel ? -13053 : -16749385);
         this.text(this.labels.get(i), 75 + i * 85, 97.0F, this.labels.get(i).length() > 3 ? 12.0F : 20.0F, 3, -1);
      }

      String[] ops = new String[]{"+", "−", "×", "÷"};

      for (int k = 0; k < 4; k++) {
         this.button(ops[k], 70 + k * 70, 150.0F, 50.0F, 36.0F, this.op == k ? -1900533 : -10853776);
      }

      this.button("Neu anfangen", 70.0F, 200.0F, 110.0F, 28.0F, -10853776);
      this.button("Überspringen (−10 s)", 220.0F, 200.0F, 110.0F, 28.0F, -10853776);
      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 370.0F, 20.0F, 9.0F, 3, this.left < 15.0F ? Style.danger : Style.sub);
      if (this.flash > 0.0F) {
         this.text("24!", 200.0F, 45.0F, 14.0F, 3, Gx.withAlpha(-12987565, this.flash / 0.8F));
      }
   }
}
