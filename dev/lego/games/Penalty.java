package dev.lego.games;

import dev.lego.ui.Sound;

final class Penalty extends GameView {
   private static final float W = 380.0F;
   private static final float H = 260.0F;
   private static final float GX = 90.0F;
   private static final float GY = 50.0F;
   private static final float GW = 200.0F;
   private static final float GH = 90.0F;
   private float aimX;
   private float aimY;
   private float kx;
   private float ky;
   private float kt = -1.0F;
   private float keeperX;
   private float keeperTarget;
   private float power;
   private boolean charging;
   private int shots;
   private int goals;
   private String msg = "";

   Penalty() {
      super("penalty");
   }

   @Override
   protected boolean hasAi() {
      return true;
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
   protected String scoreLabel() {
      return "Tore";
   }

   @Override
   protected String overTitle() {
      return this.goals + " von 10 Elfmetern verwandelt";
   }

   @Override
   protected void reset() {
      this.shots = 0;
      this.goals = 0;
      this.kt = -1.0F;
      this.keeperX = 190.0F;
      this.msg = "";
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.kt < 0.0F) {
         this.charging = true;
         this.power = 0.0F;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.charging) {
         this.charging = false;
         float wob = (1.0F - Math.min(1.0F, this.power)) * 0.0F + Math.max(0.0F, this.power - 0.8F) * 120.0F;
         this.kx = this.aimX + (this.rnd.nextFloat() - 0.5F) * wob;
         this.ky = this.aimY - Math.max(0.0F, this.power - 0.8F) * 140.0F;
         this.kt = 0.0F;
         this.shots++;
         float skill = new float[]{0.25F, 0.45F, 0.65F, 0.82F}[this.aiLevel()];
         this.keeperTarget = this.rnd.nextFloat() < skill ? this.kx : 110.0F + this.rnd.nextFloat() * 160.0F;
         Sound.play("minecraft:entity.player.attack.strong", 1.0F, 0.5F);
      }
   }

   @Override
   protected void update(float dt) {
      this.aimX = Math.max(60.0F, Math.min(320.0F, this.bmx()));
      this.aimY = Math.max(20.0F, Math.min(140.0F, this.bmy()));
      if (this.charging) {
         this.power = Math.min(1.2F, this.power + dt * 0.9F);
      }

      if (!(this.kt < 0.0F)) {
         this.kt += dt;
         this.keeperX = this.keeperX + (this.keeperTarget - this.keeperX) * Math.min(1.0F, dt * 4.5F);
         if (this.kt >= 0.5F && this.msg.isEmpty()) {
            boolean inGoal = this.kx > 90.0F && this.kx < 290.0F && this.ky > 50.0F && this.ky < 140.0F;
            boolean saved = inGoal && Math.abs(this.keeperX - this.kx) < 26.0F;
            if (!inGoal) {
               this.msg = "Daneben!";
               PackPuzzle.bad();
            } else if (saved) {
               this.msg = "Gehalten!";
               PackPuzzle.bad();
            } else {
               this.msg = "TOR!";
               this.goals++;
               this.score = this.goals;
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.4F);
            }
         }

         if (this.kt > 1.5F) {
            this.kt = -1.0F;
            this.msg = "";
            this.keeperX = 190.0F;
            if (this.shots >= 10) {
               this.gameOver();
            }
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 260.0F, 0.0F, -13722038, -14779846);

      for (int i = 0; i < 8; i++) {
         this.box(0.0F, i * 34, 380.0F, 17.0F, 0.0F, 251658239);
      }

      for (int i = 0; i <= 10; i++) {
         this.line(90.0F + i * 200.0F / 10.0F, 50.0F, 90.0F + i * 200.0F / 10.0F, 140.0F, 0.5F, 1442840575);
      }

      for (int i = 0; i <= 5; i++) {
         this.line(90.0F, 50.0F + i * 90.0F / 5.0F, 290.0F, 50.0F + i * 90.0F / 5.0F, 0.5F, 1442840575);
      }

      this.line(90.0F, 50.0F, 290.0F, 50.0F, 3.0F, -723724);
      this.line(90.0F, 50.0F, 90.0F, 140.0F, 3.0F, -723724);
      this.line(290.0F, 50.0F, 290.0F, 140.0F, 3.0F, -723724);
      this.box(this.keeperX - 9.0F, 100.0F, 18.0F, 40.0F, 5.0F, -13053);
      this.circle(this.keeperX, 94.0F, 7.0F, -12128);
      this.line(this.keeperX - 9.0F, 106.0F, this.keeperX - 22.0F, 94.0F, 3.0F, -13053);
      this.line(this.keeperX + 9.0F, 106.0F, this.keeperX + 22.0F, 94.0F, 3.0F, -13053);
      float bx0 = 190.0F;
      float by0 = 225.0F;
      if (this.kt >= 0.0F) {
         float t = Math.min(1.0F, this.kt / 0.5F);
         this.circle(bx0 + (this.kx - bx0) * t, by0 + (this.ky - by0) * t, 7.0F - t * 2.0F, -1);
      } else {
         this.circle(bx0, by0, 7.0F, -1);
         this.ring(this.aimX, this.aimY, 7.0F, 1.2F, -50356);
      }

      if (this.charging) {
         this.box(20.0F, 230.0F, 100.0F, 8.0F, 4.0F, 1426063360);
         this.box(20.0F, 230.0F, 100.0F * Math.min(1.0F, this.power / 1.2F), 8.0F, 4.0F, this.power > 0.8F ? -50356 : -12987565);
         this.text("zu fest = ungenau", 70.0F, 220.0F, 5.5F, 2, -855638017);
      }

      if (!this.msg.isEmpty()) {
         this.text(this.msg, 190.0F, 160.0F, 16.0F, 3, this.msg.startsWith("TOR") ? -7859 : -1);
      }

      this.textL("Schuss " + Math.min(10, this.shots + (this.kt < 0.0F ? 1 : 0)) + "/10 · Tore " + this.goals, 8.0F, 12.0F, 7.5F, 3, -1);
   }
}
