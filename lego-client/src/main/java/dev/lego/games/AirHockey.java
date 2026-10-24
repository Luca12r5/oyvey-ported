package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

final class AirHockey extends GameView {
   private static final float W = 260.0F;
   private static final float H = 300.0F;
   private static final float PR = 15.0F;
   private static final float BR = 9.0F;
   private static final float GOAL = 80.0F;
   private float bx2;
   private float by2;
   private float bvx;
   private float bvy;
   private float px;
   private float py;
   private float ppx;
   private float ppy;
   private float ax;
   private float ay;
   private float flash;
   private int sp;
   private int sa;

   AirHockey() {
      super("airhockey");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 260.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Tore";
   }

   @Override
   protected String overTitle() {
      return this.sp > this.sa ? "Gewonnen " + this.sp + ":" + this.sa : "Verloren " + this.sp + ":" + this.sa;
   }

   @Override
   protected void reset() {
      this.sp = this.sa = 0;
      this.serve(true);
      this.px = 130.0F;
      this.py = 260.0F;
      this.ax = 130.0F;
      this.ay = 40.0F;
   }

   private void serve(boolean toAi) {
      this.bx2 = 130.0F;
      this.by2 = 150.0F;
      this.bvx = (this.rnd.nextFloat() - 0.5F) * 60.0F;
      this.bvy = toAi ? -90.0F : 90.0F;
   }

   private void hitBy(float mx2, float my2, float mvx, float mvy) {
      float dx = this.bx2 - mx2;
      float dy = this.by2 - my2;
      float d = (float)Math.hypot(dx, dy);
      if (d < 24.0F && d > 0.0F) {
         float nx = dx / d;
         float ny = dy / d;
         this.bx2 = mx2 + nx * 24.0F;
         this.by2 = my2 + ny * 24.0F;
         float rel = (this.bvx - mvx) * nx + (this.bvy - mvy) * ny;
         if (rel < 0.0F) {
            this.bvx -= 1.9F * rel * nx;
            this.bvy -= 1.9F * rel * ny;
            Sound.play("minecraft:block.wood.hit", 1.6F, 0.4F);
         }

         float sp2 = (float)Math.hypot(this.bvx, this.bvy);
         if (sp2 > 520.0F) {
            this.bvx *= 520.0F / sp2;
            this.bvy *= 520.0F / sp2;
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.flash = Math.max(0.0F, this.flash - dt);
      float tx = Math.max(15.0F, Math.min(245.0F, this.bmx()));
      float ty = Math.max(165.0F, Math.min(285.0F, this.bmy()));
      float mvx = (tx - this.px) / dt;
      float mvy = (ty - this.py) / dt;
      this.px = tx;
      this.py = ty;
      float speed = new float[]{110.0F, 170.0F, 240.0F, 330.0F}[this.aiLevel()];
      float targetX = this.by2 < 170.0F ? this.bx2 : 130.0F;
      float targetY = this.by2 < 150.0F ? Math.min(this.by2 - 4.0F, 135.0F) : 40.0F;
      if (this.bvy > 0.0F && this.by2 < 150.0F) {
         targetY = Math.max(15.0F, this.by2 - 18.0F);
      }

      float dxa = targetX - this.ax;
      float dya = targetY - this.ay;
      float da = (float)Math.hypot(dxa, dya);
      float avx = 0.0F;
      float avy = 0.0F;
      if (da > 1.0F) {
         float s = Math.min(da, speed * dt);
         avx = dxa / da * s / dt;
         avy = dya / da * s / dt;
         this.ax += dxa / da * s;
         this.ay += dya / da * s;
      }

      this.ay = Math.max(15.0F, Math.min(135.0F, this.ay));
      this.bx2 = this.bx2 + this.bvx * dt;
      this.by2 = this.by2 + this.bvy * dt;
      this.bvx *= 1.0F - 0.25F * dt;
      this.bvy *= 1.0F - 0.25F * dt;
      if (this.bx2 < 9.0F) {
         this.bx2 = 9.0F;
         this.bvx = -this.bvx;
      }

      if (this.bx2 > 251.0F) {
         this.bx2 = 251.0F;
         this.bvx = -this.bvx;
      }

      boolean inGoal = Math.abs(this.bx2 - 130.0F) < 40.0F;
      if (this.by2 < 9.0F) {
         if (inGoal) {
            this.sp++;
            this.score = this.sp;
            this.flash = 0.6F;
            Sound.play("minecraft:entity.player.levelup", 1.4F, 0.3F);
            this.serve(true);
         } else {
            this.by2 = 9.0F;
            this.bvy = -this.bvy;
         }
      }

      if (this.by2 > 291.0F) {
         if (inGoal) {
            this.sa++;
            this.flash = 0.6F;
            PackPuzzle.bad();
            this.serve(false);
         } else {
            this.by2 = 291.0F;
            this.bvy = -this.bvy;
         }
      }

      this.hitBy(this.px, this.py, mvx, mvy);
      this.hitBy(this.ax, this.ay, avx, avy);
      if (this.sp >= 7 || this.sa >= 7) {
         this.score = this.sp > this.sa ? this.sp * 10 - this.sa : 0L;
         this.gameOver();
      }
   }

   @Override
   protected boolean clickToStart() {
      return true;
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 260.0F, 300.0F, 12.0F, -1510145);
      this.line(0.0F, 150.0F, 260.0F, 150.0F, 1.5F, -1900533);
      this.ring(130.0F, 150.0F, 30.0F, 1.5F, -1900533);
      this.box(90.0F, 0.0F, 80.0F, 5.0F, 2.0F, -15000798);
      this.box(90.0F, 295.0F, 80.0F, 5.0F, 2.0F, -15000798);
      this.circle(this.bx2, this.by2, 9.0F, -15000798);
      this.circle(this.ax, this.ay, 15.0F, -1900533);
      this.circle(this.ax, this.ay, 7.5F, -5238758);
      this.circle(this.px, this.py, 15.0F, -16749385);
      this.circle(this.px, this.py, 7.5F, -16758134);
      this.text(this.sa + "", 244.0F, 136.0F, 12.0F, 3, -1998389237);
      this.text(this.sp + "", 244.0F, 164.0F, 12.0F, 3, -2013238089);
      if (this.flash > 0.0F) {
         this.text("TOR!", 130.0F, 150.0F, 20.0F, 3, Gx.withAlpha(-13053, this.flash / 0.6F));
      }
   }
}
