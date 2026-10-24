package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class TimeSense extends GameView {
   private float target;
   private float start = -1.0F;
   private float result = -1.0F;
   private int round;
   private final List<Float> errors = new ArrayList<>();

   TimeSense() {
      super("timesense");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 230.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Punkte";
   }

   @Override
   protected String overTitle() {
      return "5 Runden geschafft";
   }

   @Override
   protected void reset() {
      this.round = 0;
      this.errors.clear();
      this.next();
   }

   private void next() {
      this.target = 3 + this.rnd.nextInt(8);
      this.start = -1.0F;
      this.result = -1.0F;
   }

   private void press() {
      if (this.result >= 0.0F) {
         if (this.round >= 5) {
            this.gameOver();
         } else {
            this.next();
         }
      } else if (this.start < 0.0F) {
         this.start = this.time;
         Sound.play("minecraft:block.note_block.pling", 1.0F, 0.4F);
      } else {
         this.result = this.time - this.start;
         float err = Math.abs(this.result - this.target);
         this.errors.add(err);
         this.score = this.score + Math.max(0, Math.round(100.0F - err * 100.0F));
         this.round++;
         Sound.play(err < 0.1F ? "minecraft:entity.player.levelup" : "minecraft:block.note_block.bell", 1.2F, 0.4F);
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.press();
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 257) {
         this.press();
      }
   }

   @Override
   protected void update(float dt) {
   }

   @Override
   protected void render() {
      this.text("Runde " + Math.min(5, this.round + (this.result >= 0.0F ? 0 : 1)) + " / 5", 190.0F, 20.0F, 8.0F, 3, Style.sub);
      this.text("Stoppe nach genau", 190.0F, 55.0F, 9.0F, 2, Style.text);
      this.text(String.format(Locale.ROOT, "%.0f Sekunden", this.target), 190.0F, 85.0F, 20.0F, 3, Style.accent);
      if (this.start < 0.0F) {
         this.text("Klick = Start", 190.0F, 140.0F, 10.0F, 3, Style.ok);
      } else if (this.result < 0.0F) {
         this.text("Läuft... (ohne Uhr!)", 190.0F, 140.0F, 10.0F, 3, Style.sub);
         float p = (float)Math.sin(this.time * 3.0F) * 0.5F + 0.5F;
         this.circle(190.0F, 175.0F, 8.0F + p * 3.0F, Gx.withAlpha(Style.accent, 0.5F));
      } else {
         float err = this.result - this.target;
         this.text(
            String.format(Locale.ROOT, "%.2f s  (%s%.2f)", this.result, err >= 0.0F ? "+" : "", err),
            190.0F,
            140.0F,
            14.0F,
            3,
            Math.abs(err) < 0.15F ? Style.ok : Style.text
         );
         this.text("Klick = weiter", 190.0F, 175.0F, 7.0F, 2, Style.muted);
      }
   }
}
