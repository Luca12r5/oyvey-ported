package dev.lego.games;

import dev.lego.ui.Sound;

final class KeepyUppy extends GameView {
   private static final float W = 300.0F;
   private static final float H = 300.0F;
   private static final float BR = 14.0F;
   private float bx2;
   private float by2;
   private float vx;
   private float vy;
   private float msgT;

   KeepyUppy() {
      super("keepyuppy");
   }

   @Override
   protected float boardW() {
      return 300.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Kicks";
   }

   @Override
   protected void reset() {
      this.bx2 = 150.0F;
      this.by2 = 80.0F;
      this.vx = 0.0F;
      this.vy = 0.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (Math.hypot(x - this.bx2, y - this.by2) < 22.0) {
         this.vy = (float)(-320L - Math.min(140L, this.score * 4L));
         this.vx = (this.bx2 - x) * 14.0F + (this.rnd.nextFloat() - 0.5F) * 40.0F;
         this.score++;
         this.msgT = 0.3F;
         Sound.play("minecraft:block.wool.hit", 1.4F, 0.6F);
      }
   }

   @Override
   protected void update(float dt) {
      this.msgT = Math.max(0.0F, this.msgT - dt);
      this.vy = this.vy + (float)(500L + Math.min(300L, this.score * 6L)) * dt;
      this.bx2 = this.bx2 + this.vx * dt;
      this.by2 = this.by2 + this.vy * dt;
      if (this.bx2 < 14.0F) {
         this.bx2 = 14.0F;
         this.vx = Math.abs(this.vx) * 0.8F;
      }

      if (this.bx2 > 286.0F) {
         this.bx2 = 286.0F;
         this.vx = -Math.abs(this.vx) * 0.8F;
      }

      if (this.by2 < 14.0F) {
         this.by2 = 14.0F;
         this.vy = Math.abs(this.vy) * 0.5F;
      }

      if (this.by2 > 286.0F) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 300.0F, 300.0F, 0.0F, -7681793, -3149825);
      this.box(0.0F, 280.0F, 300.0F, 20.0F, 0.0F, -12673462);
      this.circle(this.bx2, this.by2, 14.0F, -1);

      for (int i = 0; i < 5; i++) {
         double a = this.time * 4.0F + i * 1.256;
         this.circle(this.bx2 + (float)Math.cos(a) * 7.0F, this.by2 + (float)Math.sin(a) * 7.0F, 3.5F, -15000798);
      }

      this.text(String.valueOf(this.score), 150.0F, 40.0F, 22.0F, 3, -2011489502);
      if (this.score == 0L) {
         this.text("Klick auf den Ball!", 150.0F, 150.0F, 9.0F, 3, -15000798);
      }
   }
}
