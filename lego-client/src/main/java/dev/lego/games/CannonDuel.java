package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

final class CannonDuel extends GameView {
   private static final float W = 420.0F;
   private static final float H = 260.0F;
   private final float[] ground = new float[85];
   private float ang = 45.0F;
   private float pow = 60.0F;
   private float wind;
   private float think;
   private float aiAng;
   private float aiPow;
   private float shX;
   private float shY;
   private float shVx;
   private float shVy;
   private float boomX;
   private float boomY;
   private float boomT = -1.0F;
   private boolean flying;
   private int turn;
   private int hp1;
   private int hp2;
   private final float p1x = 50.0F;
   private final float p2x = 370.0F;

   CannonDuel() {
      super("cannons");
   }

   @Override
   protected boolean hasAi() {
      return true;
   }

   @Override
   protected float boardW() {
      return 420.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Treffer";
   }

   @Override
   protected String overTitle() {
      return this.hp2 <= 0 ? "Gewonnen!" : "Verloren";
   }

   @Override
   protected void reset() {
      float a = this.rnd.nextFloat() * 6.0F;
      float b = this.rnd.nextFloat() * 6.0F;

      for (int i = 0; i < this.ground.length; i++) {
         this.ground[i] = 190.0F - 35.0F * (float)Math.sin(i * 0.07 + a) - 20.0F * (float)Math.sin(i * 0.17 + b) - (i > 30 && i < 55 ? 30 : 0);
      }

      this.hp1 = this.hp2 = 3;
      this.turn = 1;
      this.flying = false;
      this.newWind();
      this.score = 0L;
   }

   private void newWind() {
      this.wind = (this.rnd.nextFloat() - 0.5F) * 40.0F;
   }

   private float gy(float x) {
      int i = (int)Math.max(0.0F, Math.min((float)(this.ground.length - 2), x / 5.0F));
      float f = x / 5.0F - i;
      return this.ground[i] + (this.ground[i + 1] - this.ground[i]) * f;
   }

   private void fire(float a, float p, boolean left) {
      float rad = (float)Math.toRadians(a);
      this.shX = left ? 50.0F : 370.0F;
      this.shY = this.gy(this.shX) - 12.0F;
      this.shVx = (float)Math.cos(rad) * p * 4.2F * (left ? 1 : -1);
      this.shVy = -((float)Math.sin(rad)) * p * 4.2F;
      this.flying = true;
      Sound.play("minecraft:entity.generic.explode", 2.0F, 0.3F);
   }

   private float sim(float a, float p) {
      float rad = (float)Math.toRadians(a);
      float x = 370.0F;
      float y = this.gy(370.0F) - 12.0F;
      float vx = -((float)Math.cos(rad)) * p * 4.2F;
      float vy = -((float)Math.sin(rad)) * p * 4.2F;

      for (int i = 0; i < 2000; i++) {
         vy += 2.0F;
         vx += this.wind * 0.01F;
         x += vx * 0.01F;
         y += vy * 0.01F;
         if (x < 0.0F || x > 420.0F || y >= this.gy(x)) {
            return x;
         }
      }

      return x;
   }

   @Override
   protected void onKey(int k) {
      if (this.turn == 1 && !this.flying) {
         if (k == 265 || k == 87) {
            this.ang = Math.min(85.0F, this.ang + 2.0F);
         }

         if (k == 264 || k == 83) {
            this.ang = Math.max(5.0F, this.ang - 2.0F);
         }

         if (k == 262 || k == 68) {
            this.pow = Math.min(100.0F, this.pow + 2.0F);
         }

         if (k == 263 || k == 65) {
            this.pow = Math.max(10.0F, this.pow - 2.0F);
         }

         if (k == 32 || k == 257) {
            this.fire(this.ang, this.pow, true);
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.turn == 1 && !this.flying && in(x, y, 170, 10, 80, 24)) {
         this.fire(this.ang, this.pow, true);
      }
   }

   @Override
   protected void update(float dt) {
      if (this.boomT >= 0.0F && (this.boomT += dt) > 0.6F) {
         this.boomT = -1.0F;
      }

      if (!this.flying) {
         if (this.turn == 2 && (this.think += dt) > 1.0F) {
            this.fire(this.aiAng, this.aiPow, false);
         }
      } else {
         for (int s = 0; s < 3; s++) {
            float h = dt / 3.0F;
            this.shVy += 200.0F * h;
            this.shVx = this.shVx + this.wind * h;
            this.shX = this.shX + this.shVx * h;
            this.shY = this.shY + this.shVy * h;
            if (this.shX < -20.0F || this.shX > 440.0F || this.shY >= this.gy(Math.max(0.0F, Math.min(420.0F, this.shX)))) {
               this.flying = false;
               this.boomX = this.shX;
               this.boomY = this.shY;
               this.boomT = 0.0F;
               Sound.play("minecraft:entity.generic.explode", 1.2F, 0.35F);

               for (int i = 0; i < this.ground.length; i++) {
                  float d = Math.abs(i * 5 - this.shX);
                  if (d < 16.0F) {
                     this.ground[i] = Math.min(255.0F, this.ground[i] + (16.0F - d) * 0.6F);
                  }
               }

               if (Math.abs(this.shX - 370.0F) < 18.0F) {
                  this.hp2--;
                  this.score++;
               }

               if (Math.abs(this.shX - 50.0F) < 18.0F) {
                  this.hp1--;
               }

               if (this.hp1 > 0 && this.hp2 > 0) {
                  this.turn = 3 - this.turn;
                  this.think = 0.0F;
                  this.newWind();
                  if (this.turn == 2) {
                     this.planAi();
                  }
                  break;
               }

               if (this.hp2 <= 0) {
                  PackPuzzle.win();
               } else {
                  PackPuzzle.bad();
               }

               this.gameOver();
               return;
            }
         }
      }
   }

   private void planAi() {
      float best = 1.0E9F;

      for (float a = 15.0F; a <= 75.0F; a += 3.0F) {
         for (float p = 20.0F; p <= 100.0F; p += 2.0F) {
            float d = Math.abs(this.sim(a, p) - 50.0F);
            if (d < best) {
               best = d;
               this.aiAng = a;
               this.aiPow = p;
            }
         }
      }

      float err = new float[]{14.0F, 7.0F, 3.0F, 1.0F}[this.aiLevel()];
      this.aiAng = this.aiAng + (this.rnd.nextFloat() - 0.5F) * err;
      this.aiPow = this.aiPow + (this.rnd.nextFloat() - 0.5F) * err;
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 420.0F, 260.0F, 0.0F, -7681793, -2034945);

      for (int i = 0; i < this.ground.length - 1; i++) {
         this.box(i * 5, this.ground[i], 5.5F, 260.0F - this.ground[i], 0.0F, -10634418);
      }

      for (int i = 0; i < 2; i++) {
         float x = i == 0 ? 50.0F : 370.0F;
         float y = this.gy(x);
         this.box(x - 10.0F, y - 10.0F, 20.0F, 10.0F, 3.0F, i == 0 ? -16749385 : -1900533);
         float a = (float)Math.toRadians(i == 0 ? this.ang : this.aiAng);
         this.line(x, y - 8.0F, x + (float)Math.cos(a) * 16.0F * (i == 0 ? 1 : -1), y - 8.0F - (float)Math.sin(a) * 16.0F, 3.0F, -14012872);
         int hp = i == 0 ? this.hp1 : this.hp2;

         for (int k = 0; k < 3; k++) {
            this.circle(x - 8.0F + k * 8, y - 26.0F, 3.0F, k < hp ? -50356 : 1426063360);
         }
      }

      if (this.flying) {
         this.circle(this.shX, this.shY, 3.0F, -15000798);
      }

      if (this.boomT >= 0.0F) {
         this.circle(this.boomX, this.boomY, 6.0F + this.boomT * 30.0F, Gx.withAlpha(-30208, 1.0F - this.boomT / 0.6F));
      }

      this.textL("Winkel " + Math.round(this.ang) + "°  Kraft " + Math.round(this.pow), 8.0F, 14.0F, 7.5F, 3, -15000798);
      this.textL("Wind " + (this.wind > 0.0F ? "→ " : "← ") + Math.round(Math.abs(this.wind)), 8.0F, 28.0F, 7.0F, 2, -15000798);
      if (this.turn == 1 && !this.flying) {
         this.button("Feuer!", 170.0F, 10.0F, 80.0F, 24.0F, -1900533);
      } else {
         this.text(this.turn == 2 ? "Gegner zielt..." : "", 210.0F, 22.0F, 8.0F, 3, -15000798);
      }
   }
}
