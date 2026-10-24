package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.Arrays;

final class Kniffel extends GameView {
   private static final String[] CAT = new String[]{
      "Einer", "Zweier", "Dreier", "Vierer", "Fünfer", "Sechser", "Dreierpasch", "Viererpasch", "Full House", "Kl. Straße", "Gr. Straße", "Kniffel", "Chance"
   };
   private final int[] dice = new int[5];
   private final boolean[] hold = new boolean[5];
   private final int[] sheet = new int[13];
   private int rolls;
   private float rollAnim;

   Kniffel() {
      super("kniffel");
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String overTitle() {
      return "Endstand: " + this.score;
   }

   @Override
   protected void reset() {
      Arrays.fill(this.sheet, -1);
      Arrays.fill(this.hold, false);
      this.rolls = 0;
      this.roll();
   }

   private void roll() {
      if (this.rolls < 3) {
         for (int i = 0; i < 5; i++) {
            if (!this.hold[i]) {
               this.dice[i] = 1 + this.rnd.nextInt(6);
            }
         }

         this.rolls++;
         this.rollAnim = 0.35F;
         Sound.play("minecraft:block.wood.hit", 1.6F, 0.35F);
      }
   }

   private int value(int cat) {
      int[] cnt = new int[7];
      int sum = 0;

      for (int d : this.dice) {
         cnt[d]++;
         sum += d;
      }

      int max = 0;
      boolean three = false;
      boolean two = false;

      for (int v = 1; v <= 6; v++) {
         max = Math.max(max, cnt[v]);
         if (cnt[v] == 3) {
            three = true;
         }

         if (cnt[v] == 2) {
            two = true;
         }
      }

      int run = 0;
      int best = 0;

      for (int v = 1; v <= 6; v++) {
         run = cnt[v] > 0 ? run + 1 : 0;
         best = Math.max(best, run);
      }
      return switch (cat) {
         case 0, 1, 2, 3, 4, 5 -> cnt[cat + 1] * (cat + 1);
         case 6 -> max >= 3 ? sum : 0;
         case 7 -> max >= 4 ? sum : 0;
         case 8 -> (!three || !two) && max != 5 ? 0 : 25;
         case 9 -> best >= 4 ? 30 : 0;
         case 10 -> best >= 5 ? 40 : 0;
         case 11 -> max == 5 ? 50 : 0;
         default -> sum;
      };
   }

   private int total() {
      int up = 0;
      int all = 0;

      for (int i = 0; i < 13; i++) {
         if (this.sheet[i] >= 0) {
            all += this.sheet[i];
            if (i < 6) {
               up += this.sheet[i];
            }
         }
      }

      return all + (up >= 63 ? 35 : 0);
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < 5; i++) {
         if (in(x, y, 20 + i * 46, 30, 40, 40) && this.rolls > 0) {
            this.hold[i] = !this.hold[i];
            PackPuzzle.click();
         }
      }

      if (in(x, y, 60, 90, 140, 30)) {
         this.roll();
      }

      for (int ix = 0; ix < 13; ix++) {
         if (this.sheet[ix] < 0 && in(x, y, 250.0F, 10.0F + ix * 19.5F, 160.0F, 18.0F)) {
            this.sheet[ix] = this.value(ix);
            this.score = this.total();
            PackPuzzle.good();
            Arrays.fill(this.hold, false);
            this.rolls = 0;
            boolean done = true;

            for (int s : this.sheet) {
               if (s < 0) {
                  done = false;
               }
            }

            if (done) {
               PackPuzzle.win();
               this.gameOver();
            } else {
               this.roll();
            }

            return;
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 82) {
         this.roll();
      }

      if (k >= 49 && k <= 53) {
         this.hold[k - 49] = !this.hold[k - 49];
      }
   }

   @Override
   protected void update(float dt) {
      this.rollAnim = Math.max(0.0F, this.rollAnim - dt);
   }

   private void die(float x, float y, int v, boolean held) {
      this.box(x, y, 40.0F, 40.0F, 8.0F, held ? -7859 : -460552);
      int[][] pip = new int[][]{new int[0], {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};

      for (int p : pip[v]) {
         this.circle(x + 10.0F + p % 3 * 10, y + 10.0F + p / 3 * 10, 3.2F, -15395302);
      }
   }

   @Override
   protected void render() {
      for (int i = 0; i < 5; i++) {
         float j = this.rollAnim > 0.0F && !this.hold[i] ? (float)Math.sin(this.rollAnim * 60.0F + i) * 3.0F : 0.0F;
         this.die(20 + i * 46, 30.0F + j, this.rollAnim > 0.0F && !this.hold[i] ? 1 + this.rnd.nextInt(6) : this.dice[i], this.hold[i]);
      }

      if (this.rolls < 3) {
         this.button("Würfeln (" + (3 - this.rolls) + ")", 60.0F, 90.0F, 140.0F, 30.0F, Style.accent);
      } else {
         this.text("Wähle rechts eine Kategorie", 130.0F, 105.0F, 7.0F, 3, Style.sub);
      }

      this.text("Klick auf Würfel = halten", 130.0F, 140.0F, 6.4F, 2, Style.muted);
      int up = 0;

      for (int i = 0; i < 6; i++) {
         if (this.sheet[i] > 0) {
            up += this.sheet[i];
         }
      }

      this.text("Oben: " + up + " / 63 (Bonus 35)", 130.0F, 175.0F, 7.0F, 3, up >= 63 ? Style.ok : Style.sub);
      this.text("Gesamt: " + this.total(), 130.0F, 200.0F, 11.0F, 3, Style.text);

      for (int ix = 0; ix < 13; ix++) {
         float y = 10.0F + ix * 19.5F;
         boolean free = this.sheet[ix] < 0;
         boolean hv = free && in(this.bmx(), this.bmy(), 250.0F, y, 160.0F, 18.0F);
         this.box(250.0F, y, 160.0F, 18.0F, 5.0F, hv ? Style.surfaceHover : Style.surface);
         this.textL(CAT[ix], 256.0F, y + 9.0F, 6.8F, 2, Style.text);
         String v = free ? (this.rolls > 0 ? String.valueOf(this.value(ix)) : "") : String.valueOf(this.sheet[ix]);
         this.text(v, 395.0F, y + 9.0F, 7.0F, 3, free ? Gx.withAlpha(Style.accent, 0.8F) : Style.text);
      }
   }
}
