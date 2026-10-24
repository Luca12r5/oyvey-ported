package dev.lego.games;

import dev.lego.ui.Sound;

final class Squash extends GameView {
   private static final float W = 320.0F;
   private static final float H = 300.0F;
   private static final float PW = 60.0F;
   private float bx2;
   private float by2;
   private float vx;
   private float vy;
   private float px;

   Squash() {
      super("squash");
   }

   @Override
   protected float boardW() {
      return 320.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Treffer";
   }

   @Override
   protected void reset() {
      this.bx2 = 160.0F;
      this.by2 = 100.0F;
      this.vx = 120 * (this.rnd.nextBoolean() ? 1 : -1);
      this.vy = 160.0F;
      this.px = 160.0F;
   }

   @Override
   protected void update(float dt) {
      this.px = this.px + (Math.max(30.0F, Math.min(290.0F, this.bmx())) - this.px) * Math.min(1.0F, dt * 18.0F);
      this.bx2 = this.bx2 + this.vx * dt;
      this.by2 = this.by2 + this.vy * dt;
      if (this.bx2 < 6.0F) {
         this.bx2 = 6.0F;
         this.vx = Math.abs(this.vx);
      }

      if (this.bx2 > 314.0F) {
         this.bx2 = 314.0F;
         this.vx = -Math.abs(this.vx);
      }

      if (this.by2 < 6.0F) {
         this.by2 = 6.0F;
         this.vy = Math.abs(this.vy);
         Sound.play("minecraft:block.wood.hit", 1.3F, 0.25F);
      }

      if (this.vy > 0.0F && this.by2 > 274.0F && this.by2 < 286.0F && Math.abs(this.bx2 - this.px) < 35.0F) {
         this.vy = -Math.abs(this.vy) * 1.04F;
         this.vx = this.vx + (this.bx2 - this.px) * 4.0F;
         this.score++;
         Sound.play("minecraft:block.note_block.hat", 1.2F + Math.min(0.8F, (float)this.score * 0.03F), 0.4F);
      }

      if (this.by2 > 310.0F) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 320.0F, 300.0F, 0.0F, -14931392);
      this.box(0.0F, 0.0F, 320.0F, 4.0F, 0.0F, -1900533);
      this.box(this.px - 30.0F, 280.0F, 60.0F, 8.0F, 4.0F, -12987565);
      this.circle(this.bx2, this.by2, 6.0F, -1);
      this.text(String.valueOf(this.score), 160.0F, 150.0F, 30.0F, 3, 587202559);
   }
}
