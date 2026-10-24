package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.Locale;

final class Fishing extends GameView {
   private static final float W = 380.0F;
   private static final float H = 260.0F;
   private static final String[] FISH = new String[]{"Kabeljau", "Lachs", "Tropenfisch", "Kugelfisch", "Alter Stiefel", "Goldfisch", "Riesenhecht"};
   private static final int[] PTS = new int[]{10, 20, 30, 25, 1, 60, 100};
   private int phase;
   private float timer;
   private float left;
   private float bob;
   private String msg = "";
   private int caught;

   Fishing() {
      super("fishing");
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
      return this.caught + " Fische gefangen";
   }

   @Override
   protected void reset() {
      this.phase = 0;
      this.left = 60.0F;
      this.caught = 0;
      this.msg = "Klick zum Auswerfen";
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.phase == 0) {
         this.phase = 1;
         this.timer = 1.5F + this.rnd.nextFloat() * 3.5F;
         this.msg = "Warte, bis der Schwimmer zuckt...";
         Sound.play("minecraft:entity.fishing_bobber.throw", 1.0F, 0.5F);
      } else if (this.phase == 1) {
         this.phase = 3;
         this.timer = 1.0F;
         this.msg = "Zu früh gezogen!";
         PackPuzzle.bad();
      } else if (this.phase == 2) {
         int f = this.rnd.nextFloat() < 0.04F ? 6 : (this.rnd.nextFloat() < 0.1F ? 5 : this.rnd.nextInt(5));
         this.score = this.score + PTS[f];
         this.caught++;
         this.phase = 3;
         this.timer = 1.2F;
         this.msg = "Gefangen: " + FISH[f] + " (+" + PTS[f] + ")";
         Sound.play("minecraft:entity.fishing_bobber.retrieve", 1.0F, 0.5F);
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      } else {
         this.timer -= dt;
         if (this.phase == 1 && this.timer <= 0.0F) {
            this.phase = 2;
            this.timer = 0.55F + (float)Math.random() * 0.2F;
            Sound.play("minecraft:entity.fishing_bobber.splash", 1.0F, 0.6F);
         } else if (this.phase == 2 && this.timer <= 0.0F) {
            this.phase = 3;
            this.timer = 1.0F;
            this.msg = "Der Fisch ist weg!";
         } else if (this.phase == 3 && this.timer <= 0.0F) {
            this.phase = 0;
            this.msg = "Klick zum Auswerfen";
         }

         this.bob = this.phase == 2 ? 6.0F : (float)Math.sin(this.time * 2.0F) * 1.2F;
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 110.0F, 0.0F, -20368, -8016);
      this.boxV(0.0F, 110.0F, 380.0F, 150.0F, 0.0F, -13728034, -16111014);
      this.box(0.0F, 90.0F, 90.0F, 25.0F, 0.0F, -7710160);
      this.line(70.0F, 90.0F, 140.0F, 30.0F, 2.0F, -9811414);
      if (this.phase == 1 || this.phase == 2) {
         this.line(140.0F, 30.0F, 260.0F, 112.0F + this.bob, 0.6F, -723724);
         this.circle(260.0F, 112.0F + this.bob, 4.0F, -1900533);
         this.circle(260.0F, 108.0F + this.bob, 2.5F, -723724);
         if (this.phase == 2) {
            this.ring(260.0F, 114.0F, 10.0F + (float)Math.sin(this.time * 20.0F) * 3.0F, 1.0F, -1426063361);
         }
      }

      for (int i = 0; i < 4; i++) {
         float fx = (this.time * (20 + i * 9) + i * 97) % 420.0F - 20.0F;
         float fy = 150 + i * 25;
         this.box(fx, fy, 16.0F, 7.0F, 3.0F, 1429891744);
      }

      this.text(this.msg, 190.0F, 60.0F, 9.0F, 3, this.phase == 2 ? -1900533 : -12966892);
      this.textL(String.format(Locale.ROOT, "%.0f s · %d Fische", this.left, this.caught), 8.0F, 12.0F, 7.5F, 3, -12966892);
   }
}
