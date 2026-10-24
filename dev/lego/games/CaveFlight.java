package dev.lego.games;

import dev.lego.ui.Gx;
import java.util.ArrayList;
import java.util.List;

final class CaveFlight extends GameView {
   private static final float W = 400.0F;
   private static final float H = 260.0F;
   private float y;
   private float vy;
   private float dist;
   private float seed;
   private boolean hold;
   private final List<float[]> smoke = new ArrayList<>();

   CaveFlight() {
      super("caveflight");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Meter";
   }

   @Override
   protected void reset() {
      this.y = 130.0F;
      this.vy = 0.0F;
      this.dist = 0.0F;
      this.seed = this.rnd.nextFloat() * 100.0F;
      this.smoke.clear();
   }

   private float top(float x) {
      float d = this.dist + x;
      return 30.0F
         + 30.0F * (float)Math.sin(d * 0.012 + this.seed)
         + 18.0F * (float)Math.sin(d * 0.031 + this.seed * 2.0F)
         + Math.min(50.0F, this.dist / 200.0F);
   }

   private float bot(float x) {
      float d = this.dist + x;
      return 230.0F
         - 30.0F * (float)Math.sin(d * 0.011 + this.seed + 2.0)
         - 18.0F * (float)Math.sin(d * 0.027 + this.seed * 3.0F)
         - Math.min(50.0F, this.dist / 200.0F);
   }

   @Override
   protected void onClick(float x, float yy, int b) {
      this.hold = true;
   }

   @Override
   protected void onRelease(float x, float yy, int b) {
      this.hold = false;
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.hold = true;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.hold = false;
      }
   }

   @Override
   protected void update(float dt) {
      float speed = 120.0F + Math.min(160.0F, this.dist / 25.0F);
      this.dist += speed * dt;
      this.vy = this.vy + (this.hold ? -520 : 420) * dt;
      this.vy = Math.max(-240.0F, Math.min(260.0F, this.vy));
      this.y = this.y + this.vy * dt;
      this.score = (long)(this.dist / 10.0F);
      if (this.rnd.nextFloat() < 0.5F) {
         this.smoke.add(new float[]{80.0F, this.y + 4.0F, 0.0F});
      }

      for (float[] s : this.smoke) {
         s[0] -= speed * dt;
         s[2] += dt;
      }

      this.smoke.removeIf(sx -> sx[2] > 0.8F);
      if (this.y - 7.0F < this.top(90.0F) || this.y + 7.0F > this.bot(90.0F)) {
         PackArcade.boom();
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 260.0F, 0.0F, -15069136, -12969408);

      for (float x = 0.0F; x < 400.0F; x += 8.0F) {
         float t = this.top(x);
         float b = this.bot(x);
         this.box(x, 0.0F, 8.5F, t, 0.0F, -9811398);
         this.box(x, b, 8.5F, 260.0F - b, 0.0F, -9811398);
      }

      for (float[] s : this.smoke) {
         this.circle(s[0], s[1], 3.0F + s[2] * 6.0F, Gx.withAlpha(-1, 0.3F * (1.0F - s[2] / 0.8F)));
      }

      float tilt = this.vy / 600.0F;
      this.box(80.0F, this.y - 6.0F + tilt * 4.0F, 24.0F, 12.0F, 5.0F, -1900533);
      this.box(98.0F, this.y - 4.0F, 8.0F, 6.0F, 2.0F, -6301441);
      this.line(84.0F, this.y - 9.0F, 102.0F, this.y - 9.0F, 1.5F, -15000798);
      float rot = (float)Math.sin(this.time * 40.0F) * 12.0F;
      this.line(93.0F - rot, this.y - 11.0F, 93.0F + rot, this.y - 11.0F, 1.2F, -15000798);
      this.text(this.score + " m", 360.0F, 14.0F, 8.0F, 3, -1);
   }
}
