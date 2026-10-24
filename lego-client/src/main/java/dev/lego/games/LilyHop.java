package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class LilyHop extends GameView {
   private static final float W = 380.0F;
   private static final float H = 240.0F;
   private final List<float[]> pads = new ArrayList<>();
   private float fx;
   private float fy;
   private float jumpT = -1.0F;
   private float charge;
   private float camX;
   private float tx;
   private float ty;
   private float sx;
   private float sy;
   private boolean charging;
   private int onPad;

   LilyHop() {
      super("lilyhop");
   }

   @Override
   protected float boardW() {
      return 380.0F;
   }

   @Override
   protected float boardH() {
      return 240.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Sprünge";
   }

   @Override
   protected String overTitle() {
      return "Platsch! Ins Wasser gefallen";
   }

   @Override
   protected void reset() {
      this.pads.clear();
      this.camX = 0.0F;
      float x = 40.0F;

      for (int i = 0; i < 6; i++) {
         this.pads.add(new float[]{x, 120.0F + (this.rnd.nextFloat() - 0.5F) * 80.0F, 16.0F + this.rnd.nextFloat() * 8.0F, this.rnd.nextFloat() * 6.0F});
         x += 60.0F + this.rnd.nextFloat() * 50.0F;
      }

      this.onPad = 0;
      this.fx = this.pads.get(0)[0];
      this.fy = this.pads.get(0)[1];
      this.jumpT = -1.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.jumpT < 0.0F) {
         this.charging = true;
         this.charge = 0.0F;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.charging) {
         this.charging = false;
         float dist = 30.0F + this.charge * 150.0F;
         float[] next = this.onPad + 1 < this.pads.size() ? this.pads.get(this.onPad + 1) : this.pads.get(this.onPad);
         float dx = next[0] - this.fx;
         float dy = next[1] - this.fy;
         float n = (float)Math.max(1.0, Math.hypot(dx, dy));
         this.sx = this.fx;
         this.sy = this.fy;
         this.tx = this.fx + dx / n * dist;
         this.ty = this.fy + dy / n * dist;
         this.jumpT = 0.0F;
         Sound.play("minecraft:entity.slime.jump", 1.3F, 0.4F);
      }
   }

   @Override
   protected void update(float dt) {
      if (this.charging) {
         this.charge = Math.min(1.0F, this.charge + dt * 0.8F);
      }

      for (float[] p : this.pads) {
         p[1] += (float)Math.sin(this.time * 1.5 + p[3]) * 8.0F * dt;
      }

      if (this.jumpT >= 0.0F) {
         this.jumpT += dt * 2.2F;
         float t = Math.min(1.0F, this.jumpT);
         this.fx = this.sx + (this.tx - this.sx) * t;
         this.fy = this.sy + (this.ty - this.sy) * t;
         if (this.jumpT >= 1.0F) {
            this.jumpT = -1.0F;
            int hitPad = -1;

            for (int i = 0; i < this.pads.size(); i++) {
               float[] p = this.pads.get(i);
               if (Math.hypot(this.fx - p[0], this.fy - p[1]) < p[2]) {
                  hitPad = i;
               }
            }

            if (hitPad < 0) {
               Sound.play("minecraft:entity.generic.splash", 1.0F, 0.5F);
               this.gameOver();
               return;
            }

            if (hitPad > this.onPad) {
               this.score = this.score + (hitPad - this.onPad);
               Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
            }

            this.onPad = hitPad;

            for (float[] last = this.pads.get(this.pads.size() - 1); this.pads.size() < this.onPad + 6; last = this.pads.get(this.pads.size() - 1)) {
               this.pads
                  .add(
                     new float[]{
                        last[0] + 60.0F + this.rnd.nextFloat() * (float)(50L + this.score),
                        120.0F + (this.rnd.nextFloat() - 0.5F) * 100.0F,
                        Math.max(10.0F, 22.0F - (float)this.score * 0.3F) + this.rnd.nextFloat() * 6.0F,
                        this.rnd.nextFloat() * 6.0F
                     }
                  );
            }
         }
      } else {
         float[] p = this.pads.get(this.onPad);
         this.fy = p[1];
         this.fx = p[0];
      }

      this.camX = this.camX + (this.fx - 80.0F - this.camX) * Math.min(1.0F, dt * 3.0F);
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 380.0F, 240.0F, 0.0F, -15046006, -16106918);

      for (int i = 0; i < 12; i++) {
         this.box(((i * 73 - this.camX * 0.5F) % 380.0F + 380.0F) % 380.0F, i * 41 % 240.0F, 26.0F, 2.0F, 1.0F, 587202559);
      }

      for (float[] p : this.pads) {
         float x = p[0] - this.camX;
         this.circle(x, p[1], p[2], -12673462);
         this.box(x, p[1] - 2.0F, p[2], 4.0F, 0.0F, -15046006);
         this.circle(x - p[2] * 0.3F, p[1] - p[2] * 0.4F, 2.5F, -25920);
      }

      float jh = this.jumpT >= 0.0F ? (float)Math.sin(Math.min(1.0F, this.jumpT) * Math.PI) * 26.0F : 0.0F;
      float x = this.fx - this.camX;
      this.circle(x, this.fy - jh + 6.0F, 9.0F, 855638016);
      this.circle(x, this.fy - jh, 9.0F, -12987565);
      this.circle(x - 4.0F, this.fy - jh - 6.0F, 3.0F, -1);
      this.circle(x + 4.0F, this.fy - jh - 6.0F, 3.0F, -1);
      this.circle(x - 4.0F, this.fy - jh - 6.0F, 1.4F, -15000798);
      this.circle(x + 4.0F, this.fy - jh - 6.0F, 1.4F, -15000798);
      if (this.charging) {
         this.box(20.0F, 220.0F, 100.0F, 8.0F, 4.0F, 1426063360);
         this.box(20.0F, 220.0F, 100.0F * this.charge, 8.0F, 4.0F, -13053);
      }

      this.text("Halten = Sprungweite", 190.0F, 226.0F, 6.0F, 2, -1426063361);
   }
}
