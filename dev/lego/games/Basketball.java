package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.Locale;

final class Basketball extends GameView {
   private static final float W = 400.0F;
   private static final float H = 280.0F;
   private static final float BR = 9.0F;
   private float bx2;
   private float by2;
   private float vx;
   private float vy;
   private float hx;
   private float hy;
   private float left;
   private float swish;
   private boolean flying;
   private boolean drag;
   private boolean scored;
   private float dsx;
   private float dsy;

   Basketball() {
      super("basketball");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      this.left = 45.0F;
      this.newBall();
      this.hx = 300.0F;
      this.hy = 90.0F;
   }

   private void newBall() {
      this.bx2 = 70.0F;
      this.by2 = 220.0F;
      this.vx = this.vy = 0.0F;
      this.flying = false;
      this.scored = false;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.flying && Math.hypot(x - this.bx2, y - this.by2) < 30.0) {
         this.drag = true;
         this.dsx = x;
         this.dsy = y;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.drag) {
         this.drag = false;
         this.vx = (this.dsx - x) * 4.2F;
         this.vy = (this.dsy - y) * 4.2F;
         if (Math.hypot(this.vx, this.vy) > 40.0) {
            this.flying = true;
            Sound.play("minecraft:entity.snowball.throw", 1.0F, 0.3F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      this.swish = Math.max(0.0F, this.swish - dt);
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      } else if (this.flying) {
         this.vy += 520.0F * dt;
         this.bx2 = this.bx2 + this.vx * dt;
         this.by2 = this.by2 + this.vy * dt;

         for (float rx : new float[]{this.hx - 24.0F, this.hx + 24.0F}) {
            float dx = this.bx2 - rx;
            float dy = this.by2 - this.hy;
            float d = (float)Math.hypot(dx, dy);
            if (d < 11.0F && d > 0.0F) {
               float nx = dx / d;
               float ny = dy / d;
               this.bx2 = rx + nx * 11.0F;
               this.by2 = this.hy + ny * 11.0F;
               float dot = this.vx * nx + this.vy * ny;
               if (dot < 0.0F) {
                  this.vx -= 1.6F * dot * nx;
                  this.vy -= 1.6F * dot * ny;
               }

               Sound.play("minecraft:block.chain.hit", 1.4F, 0.3F);
            }
         }

         if (this.bx2 + 9.0F > this.hx + 34.0F && this.by2 > this.hy - 60.0F && this.by2 < this.hy + 10.0F && this.vx > 0.0F) {
            this.bx2 = this.hx + 34.0F - 9.0F;
            this.vx = -this.vx * 0.6F;
         }

         if (!this.scored && this.vy > 0.0F && Math.abs(this.bx2 - this.hx) < 18.0F && Math.abs(this.by2 - this.hy) < 6.0F) {
            this.scored = true;
            this.score = this.score + (Math.hypot(this.hx - 70.0F, this.hy - 220.0F) > 260.0 ? 3L : 2L);
            this.swish = 1.0F;
            Sound.play("minecraft:entity.player.levelup", 1.5F, 0.3F);
         }

         if (this.by2 > 300.0F || this.bx2 > 420.0F || this.bx2 < -20.0F) {
            if (this.scored) {
               this.hx = 220.0F + this.rnd.nextFloat() * 140.0F;
               this.hy = 60.0F + this.rnd.nextFloat() * 90.0F;
            }

            this.newBall();
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 280.0F, 0.0F, -14665120, -15721936);
      this.box(0.0F, 250.0F, 400.0F, 30.0F, 0.0F, -3635398);
      this.box(this.hx + 34.0F, this.hy - 60.0F, 5.0F, 70.0F, 2.0F, -723724);
      this.box(this.hx + 37.0F, this.hy + 10.0F, 4.0F, 250.0F - this.hy - 10.0F, 0.0F, -10853776);

      for (int i = 0; i < 5; i++) {
         this.line(this.hx - 22.0F + i * 4, this.hy, this.hx - 14.0F + i * 7, this.hy + 22.0F, 0.8F, -855638017);
      }

      this.line(this.hx - 24.0F, this.hy, this.hx + 24.0F, this.hy, 2.2F, -38400);
      if (this.drag) {
         this.line(this.bx2, this.by2, this.bx2 + (this.dsx - this.bmx()), this.by2 + (this.dsy - this.bmy()), 1.2F, -1996488705);
      }

      this.circle(this.bx2, this.by2, 9.0F, -1543126);
      this.line(this.bx2 - 9.0F, this.by2, this.bx2 + 9.0F, this.by2, 0.8F, -12969462);
      if (this.swish > 0.0F) {
         this.text("Korb!", this.hx, this.hy - 30.0F - (1.0F - this.swish) * 20.0F, 14.0F, 3, Gx.withAlpha(-13053, this.swish));
      }

      this.textL(String.format(Locale.ROOT, "%.0f s", this.left), 8.0F, 12.0F, 9.0F, 3, this.left < 10.0F ? -45730 : -1);
      this.text("Ball ziehen und loslassen", 120.0F, 265.0F, 6.6F, 2, -12969462);
   }
}
