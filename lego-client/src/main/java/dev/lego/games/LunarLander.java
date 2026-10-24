package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

final class LunarLander extends GameView {
   private static final float W = 400.0F;
   private static final float H = 270.0F;
   private float x;
   private float y;
   private float vx;
   private float vy;
   private float ang;
   private float fuel;
   private float padX;
   private float crashT = -1.0F;
   private float landT = -1.0F;
   private boolean thrust;
   private boolean left;
   private boolean right;
   private int level;
   private final float[] ground = new float[41];

   LunarLander() {
      super("lander");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Bruchlandung!";
   }

   @Override
   protected void reset() {
      this.level = 1;
      this.setup();
   }

   private void setup() {
      this.x = 40.0F + this.rnd.nextFloat() * 320.0F;
      this.y = 30.0F;
      this.vx = (this.rnd.nextFloat() - 0.5F) * 30.0F;
      this.vy = 0.0F;
      this.ang = 0.0F;
      this.fuel = 100.0F;
      this.crashT = -1.0F;
      this.landT = -1.0F;
      int pad = 4 + this.rnd.nextInt(32);
      this.padX = pad * 10;

      for (int i = 0; i <= 40; i++) {
         this.ground[i] = 210.0F + this.rnd.nextFloat() * 45.0F;
      }

      for (int i = pad; i <= pad + 4 && i <= 40; i++) {
         this.ground[i] = this.ground[pad];
      }
   }

   private float gy(float px) {
      int i = (int)Math.max(0.0F, Math.min(39.0F, px / 10.0F));
      float f = px / 10.0F - i;
      return this.ground[i] + (this.ground[i + 1] - this.ground[i]) * f;
   }

   @Override
   protected void onKey(int k) {
      if (k == 265 || k == 87 || k == 32) {
         this.thrust = true;
      }

      if (k == 263 || k == 65) {
         this.left = true;
      }

      if (k == 262 || k == 68) {
         this.right = true;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 265 || k == 87 || k == 32) {
         this.thrust = false;
      }

      if (k == 263 || k == 65) {
         this.left = false;
      }

      if (k == 262 || k == 68) {
         this.right = false;
      }
   }

   @Override
   protected void update(float dt) {
      if (this.landT >= 0.0F) {
         if ((this.landT += dt) > 1.5F) {
            this.level++;
            this.setup();
         }
      } else if (this.crashT >= 0.0F) {
         if ((this.crashT += dt) > 1.2F) {
            this.gameOver();
         }
      } else {
         if (this.left) {
            this.ang -= 120.0F * dt;
         }

         if (this.right) {
            this.ang += 120.0F * dt;
         }

         this.ang = Math.max(-80.0F, Math.min(80.0F, this.ang));
         float g = 22 + this.level * 2;
         this.vy += g * dt;
         if (this.thrust && this.fuel > 0.0F) {
            float a = (float)Math.toRadians(this.ang);
            this.vx = (float)(this.vx + Math.sin(a) * 55.0 * dt);
            this.vy = (float)(this.vy - Math.cos(a) * 55.0 * dt);
            this.fuel = Math.max(0.0F, this.fuel - 14.0F * dt);
         }

         this.x = this.x + this.vx * dt;
         this.y = this.y + this.vy * dt;
         if (this.x < 0.0F || this.x > 400.0F) {
            this.vx = -this.vx * 0.5F;
         }

         this.x = Math.max(0.0F, Math.min(400.0F, this.x));
         if (this.y + 8.0F >= this.gy(this.x)) {
            boolean onPad = this.x >= this.padX && this.x <= this.padX + 40.0F;
            if (onPad && this.vy < 30.0F && Math.abs(this.vx) < 18.0F && Math.abs(this.ang) < 12.0F) {
               this.landT = 0.0F;
               this.score = this.score + 100L + (long)this.fuel * 2L + this.level * 20;
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.35F);
            } else {
               this.crashT = 0.0F;
               PackArcade.boom();
            }

            this.y = this.gy(this.x) - 8.0F;
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 400.0F, 270.0F, 0.0F, -16447986);

      for (int i = 0; i < 40; i++) {
         this.circle(i * 97 % 400.0F, i * 53 % 150, 0.8F, -1711276033);
      }

      for (int i = 0; i < 40; i++) {
         for (float px = i * 10; px < i * 10 + 10; px += 2.0F) {
            this.box(px, this.gy(px), 2.2F, 270.0F - this.gy(px), 0.0F, -7697774);
         }
      }

      this.box(this.padX, this.gy(this.padX) - 2.0F, 40.0F, 3.0F, 1.0F, -12987565);
      if (this.crashT < 0.0F) {
         float sa = (float)Math.sin(Math.toRadians(this.ang));
         float ca = (float)Math.cos(Math.toRadians(this.ang));
         this.box(this.x - 7.0F, this.y - 8.0F, 14.0F, 12.0F, 4.0F, -2564378);
         this.box(this.x - 4.0F, this.y - 6.0F, 8.0F, 5.0F, 2.0F, -12604929);
         this.line(this.x - 6.0F, this.y + 4.0F, this.x - 9.0F, this.y + 9.0F, 1.2F, -2564378);
         this.line(this.x + 6.0F, this.y + 4.0F, this.x + 9.0F, this.y + 9.0F, 1.2F, -2564378);
         this.line(this.x, this.y - 2.0F, this.x + sa * 14.0F, this.y - 2.0F - ca * 14.0F, 1.0F, -1996488705);
         if (this.thrust && this.fuel > 0.0F) {
            this.circle(this.x - sa * 10.0F, this.y + ca * 10.0F + (float)Math.random() * 2.0F, 3.0F, -20432);
         }
      } else {
         this.circle(this.x, this.y, 6.0F + this.crashT * 30.0F, Gx.withAlpha(-38400, 1.0F - this.crashT));
      }

      this.textL("Treibstoff " + Math.round(this.fuel) + "%", 8.0F, 10.0F, 7.0F, 3, this.fuel < 20.0F ? -45730 : -1);
      this.textL("Fall " + Math.round(this.vy) + "  Seite " + Math.round(this.vx), 8.0F, 22.0F, 7.0F, 2, this.vy > 30.0F ? -45730 : -6642766);
      this.textL("Level " + this.level, 340.0F, 10.0F, 7.0F, 3, -1);
      if (this.landT >= 0.0F) {
         this.text("Sanft gelandet!", 200.0F, 110.0F, 14.0F, 3, -12987565);
      }
   }
}
