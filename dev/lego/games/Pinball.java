package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

final class Pinball extends GameView {
   private static final float W = 260.0F;
   private static final float H = 310.0F;
   private static final float BR = 6.0F;
   private float bxx;
   private float byy;
   private float vx;
   private float vy;
   private float la;
   private float ra;
   private float lav;
   private float rav;
   private boolean lUp;
   private boolean rUp;
   private int balls;
   private final float[][] bumpers = new float[][]{
      {80.0F, 90.0F, 16.0F}, {180.0F, 90.0F, 16.0F}, {130.0F, 140.0F, 18.0F}, {60.0F, 170.0F, 11.0F}, {200.0F, 170.0F, 11.0F}
   };
   private final float[] bumpFx = new float[5];

   Pinball() {
      super("pinball");
   }

   @Override
   protected float boardW() {
      return 260.0F;
   }

   @Override
   protected float boardH() {
      return 310.0F;
   }

   @Override
   protected String overTitle() {
      return "Alle Kugeln verloren";
   }

   @Override
   protected void reset() {
      this.balls = 3;
      this.launch();
   }

   private void launch() {
      this.bxx = 244.0F;
      this.byy = 250.0F;
      this.vx = -40.0F - this.rnd.nextFloat() * 60.0F;
      this.vy = -520.0F;
   }

   @Override
   protected void onKey(int k) {
      if (k == 263 || k == 65) {
         this.lUp = true;
      }

      if (k == 262 || k == 68) {
         this.rUp = true;
      }

      if (k == 32) {
         this.lUp = this.rUp = true;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 263 || k == 65) {
         this.lUp = false;
      }

      if (k == 262 || k == 68) {
         this.rUp = false;
      }

      if (k == 32) {
         this.lUp = this.rUp = false;
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (x < 130.0F) {
         this.lUp = true;
      } else {
         this.rUp = true;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      this.lUp = this.rUp = false;
   }

   private float[] flipper(boolean left, float a) {
      float px = left ? 70.0F : 190.0F;
      float py = 270.0F;
      float ang = (float)Math.toRadians(left ? 25.0F - a : 155.0F + a);
      return new float[]{px, py, px + (float)Math.cos(ang) * 52.0F, py + (float)Math.sin(ang) * 52.0F};
   }

   private void segment(float[] s, float angVel, boolean left) {
      float dx = s[2] - s[0];
      float dy = s[3] - s[1];
      float len2 = dx * dx + dy * dy;
      float t = Math.max(0.0F, Math.min(1.0F, ((this.bxx - s[0]) * dx + (this.byy - s[1]) * dy) / len2));
      float cx = s[0] + dx * t;
      float cy = s[1] + dy * t;
      float ddx = this.bxx - cx;
      float ddy = this.byy - cy;
      float d = (float)Math.hypot(ddx, ddy);
      if (d < 9.0F && d > 0.0F) {
         float nx = ddx / d;
         float ny = ddy / d;
         this.bxx = cx + nx * 9.0F;
         this.byy = cy + ny * 9.0F;
         float sv = (float)Math.toRadians(angVel) * t * 52.0F;
         float rel = this.vx * nx + this.vy * ny;
         if (rel < 0.0F) {
            this.vx -= 1.5F * rel * nx;
            this.vy -= 1.5F * rel * ny;
         }

         if (sv > 0.0F) {
            this.vx += nx * sv * 0.9F;
            this.vy += ny * sv * 0.9F;
         }
      }
   }

   @Override
   protected void update(float dt) {
      float nla = Math.max(0.0F, Math.min(50.0F, this.la + (this.lUp ? 900 : -500) * dt));
      float nra = Math.max(0.0F, Math.min(50.0F, this.ra + (this.rUp ? 900 : -500) * dt));
      this.lav = (nla - this.la) / dt;
      this.rav = (nra - this.ra) / dt;
      this.la = nla;
      this.ra = nra;

      for (int i = 0; i < 5; i++) {
         this.bumpFx[i] = Math.max(0.0F, this.bumpFx[i] - dt * 4.0F);
      }

      int sub = 4;

      for (int s = 0; s < sub; s++) {
         float h = dt / sub;
         this.vy += 420.0F * h;
         this.bxx = this.bxx + this.vx * h;
         this.byy = this.byy + this.vy * h;
         if (this.bxx < 6.0F) {
            this.bxx = 6.0F;
            this.vx = Math.abs(this.vx) * 0.8F;
         }

         if (this.bxx > 254.0F) {
            this.bxx = 254.0F;
            this.vx = -Math.abs(this.vx) * 0.8F;
         }

         if (this.byy < 6.0F) {
            this.byy = 6.0F;
            this.vy = Math.abs(this.vy) * 0.8F;
         }

         for (int i = 0; i < 5; i++) {
            float[] bm = this.bumpers[i];
            float dx = this.bxx - bm[0];
            float dy = this.byy - bm[1];
            float d = (float)Math.hypot(dx, dy);
            if (d < bm[2] + 6.0F && d > 0.0F) {
               float nx = dx / d;
               float ny = dy / d;
               this.bxx = bm[0] + nx * (bm[2] + 6.0F);
               this.byy = bm[1] + ny * (bm[2] + 6.0F);
               float rel = this.vx * nx + this.vy * ny;
               this.vx -= 2.0F * rel * nx;
               this.vy -= 2.0F * rel * ny;
               this.vx += nx * 140.0F;
               this.vy += ny * 140.0F;
               this.score += 100L;
               this.bumpFx[i] = 1.0F;
               Sound.play("minecraft:block.note_block.bell", 1.2F + i * 0.1F, 0.3F);
            }
         }

         this.segment(new float[]{0.0F, 220.0F, 70.0F, 270.0F}, 0.0F, true);
         this.segment(new float[]{260.0F, 220.0F, 190.0F, 270.0F}, 0.0F, false);
         this.segment(this.flipper(true, this.la), this.lav, true);
         this.segment(this.flipper(false, this.ra), this.rav, false);
         float sp = (float)Math.hypot(this.vx, this.vy);
         if (sp > 700.0F) {
            this.vx *= 700.0F / sp;
            this.vy *= 700.0F / sp;
         }
      }

      if (this.byy > 320.0F) {
         this.balls--;
         Sound.play("minecraft:block.note_block.bass", 0.7F, 0.4F);
         if (this.balls <= 0) {
            this.gameOver();
         } else {
            this.launch();
         }
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 260.0F, 310.0F, 0.0F, -15070672, -16116672);

      for (int i = 0; i < 5; i++) {
         float[] bm = this.bumpers[i];
         if (this.bumpFx[i] > 0.0F) {
            this.circle(bm[0], bm[1], bm[2] + 6.0F * this.bumpFx[i], Gx.withAlpha(-7859, 0.5F * this.bumpFx[i]));
         }

         this.circle(bm[0], bm[1], bm[2], PackPuzzle.PAL[i]);
         this.circle(bm[0], bm[1], bm[2] * 0.55F, -723724);
      }

      this.line(0.0F, 220.0F, 70.0F, 270.0F, 4.0F, -6642771);
      this.line(260.0F, 220.0F, 190.0F, 270.0F, 4.0F, -6642771);
      float[] l = this.flipper(true, this.la);
      float[] r = this.flipper(false, this.ra);
      this.line(l[0], l[1], l[2], l[3], 8.0F, -1900533);
      this.line(r[0], r[1], r[2], r[3], 8.0F, -1900533);
      this.circle(this.bxx, this.byy, 6.0F, -2564378);
      this.circle(this.bxx - 2.0F, this.byy - 2.0F, 2.0F, -1);
      this.textL("Kugeln: " + this.balls, 6.0F, 10.0F, 7.0F, 3, -1);
   }
}
