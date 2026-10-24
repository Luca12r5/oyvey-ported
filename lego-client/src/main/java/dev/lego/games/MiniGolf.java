package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

final class MiniGolf extends GameView {
   private static final float W = 400.0F;
   private static final float H = 280.0F;
   private static final float BR = 5.0F;
   private static final float HR = 8.0F;
   private static final float[][][] WALLS = new float[][][]{
      new float[0][],
      {{180.0F, 60.0F, 20.0F, 160.0F}},
      {{120.0F, 0.0F, 20.0F, 180.0F}, {260.0F, 100.0F, 20.0F, 180.0F}},
      {{150.0F, 90.0F, 100.0F, 20.0F}, {150.0F, 170.0F, 100.0F, 20.0F}},
      {{100.0F, 60.0F, 20.0F, 220.0F}, {200.0F, 0.0F, 20.0F, 200.0F}, {300.0F, 80.0F, 20.0F, 200.0F}},
      {{170.0F, 100.0F, 60.0F, 80.0F}}
   };
   private static final float[][] START = new float[][]{{50.0F, 140.0F}, {50.0F, 220.0F}, {50.0F, 40.0F}, {50.0F, 140.0F}, {50.0F, 240.0F}, {50.0F, 50.0F}};
   private static final float[][] HOLE = new float[][]{
      {350.0F, 140.0F}, {350.0F, 60.0F}, {350.0F, 240.0F}, {350.0F, 140.0F}, {360.0F, 40.0F}, {350.0F, 230.0F}
   };
   private int hole;
   private int strokes;
   private int total;
   private float bxx;
   private float byy;
   private float vx;
   private float vy;
   private float sunk = -1.0F;
   private boolean drag;

   MiniGolf() {
      super("minigolf");
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
   protected String scoreLabel() {
      return "Schläge";
   }

   @Override
   protected String overTitle() {
      return "Runde vorbei: " + this.total + " Schläge";
   }

   @Override
   protected void reset() {
      this.hole = 0;
      this.total = 0;
      this.place();
   }

   private void place() {
      this.bxx = START[this.hole][0];
      this.byy = START[this.hole][1];
      this.vx = this.vy = 0.0F;
      this.strokes = 0;
      this.sunk = -1.0F;
   }

   private boolean moving() {
      return Math.hypot(this.vx, this.vy) > 2.0;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (!this.moving() && this.sunk < 0.0F) {
         this.drag = true;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.drag) {
         this.drag = false;
         float dx = this.bxx - x;
         float dy = this.byy - y;
         float d = (float)Math.min(120.0, Math.hypot(dx, dy));
         if (!(d < 5.0F)) {
            float n = (float)Math.hypot(dx, dy);
            this.vx = dx / n * d * 3.2F;
            this.vy = dy / n * d * 3.2F;
            this.strokes++;
            this.total++;
            this.score = this.total;
            Sound.play("minecraft:block.wood.hit", 1.5F, 0.4F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      if (this.sunk >= 0.0F) {
         if ((this.sunk += dt) > 1.0F) {
            this.hole++;
            if (this.hole >= START.length) {
               this.gameOver();
            } else {
               this.place();
            }
         }
      } else {
         this.bxx = this.bxx + this.vx * dt;
         this.byy = this.byy + this.vy * dt;
         float f = (float)Math.pow(0.35, dt);
         this.vx *= f;
         this.vy *= f;
         if (!this.moving()) {
            this.vx = this.vy = 0.0F;
         }

         if (this.bxx < 5.0F) {
            this.bxx = 5.0F;
            this.vx = -this.vx;
         }

         if (this.bxx > 395.0F) {
            this.bxx = 395.0F;
            this.vx = -this.vx;
         }

         if (this.byy < 5.0F) {
            this.byy = 5.0F;
            this.vy = -this.vy;
         }

         if (this.byy > 275.0F) {
            this.byy = 275.0F;
            this.vy = -this.vy;
         }

         for (float[] w : WALLS[this.hole]) {
            if (this.bxx + 5.0F > w[0] && this.bxx - 5.0F < w[0] + w[2] && this.byy + 5.0F > w[1] && this.byy - 5.0F < w[1] + w[3]) {
               float ol = this.bxx + 5.0F - w[0];
               float orr = w[0] + w[2] - (this.bxx - 5.0F);
               float ot = this.byy + 5.0F - w[1];
               float ob = w[1] + w[3] - (this.byy - 5.0F);
               float m = Math.min(Math.min(ol, orr), Math.min(ot, ob));
               if (m == ol) {
                  this.bxx -= ol;
                  this.vx = -Math.abs(this.vx);
               } else if (m == orr) {
                  this.bxx += orr;
                  this.vx = Math.abs(this.vx);
               } else if (m == ot) {
                  this.byy -= ot;
                  this.vy = -Math.abs(this.vy);
               } else {
                  this.byy += ob;
                  this.vy = Math.abs(this.vy);
               }

               Sound.play("minecraft:block.wood.hit", 1.8F, 0.2F);
            }
         }

         float[] h = HOLE[this.hole];
         if (Math.hypot(this.bxx - h[0], this.byy - h[1]) < 8.0 && Math.hypot(this.vx, this.vy) < 260.0) {
            this.sunk = 0.0F;
            Sound.play("minecraft:entity.player.levelup", 1.5F, 0.3F);
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 400.0F, 280.0F, 8.0F, -12673462);

      for (int i = 0; i < 20; i++) {
         this.box(i * 41 % 400.0F, i * 67 % 280.0F, 30.0F, 2.0F, 1.0F, 452984831);
      }

      for (float[] w : WALLS[this.hole]) {
         this.box(w[0], w[1], w[2], w[3], 3.0F, -7710160);
         this.box(w[0] + 2.0F, w[1] + 2.0F, w[2] - 4.0F, w[3] - 4.0F, 2.0F, -5738432);
      }

      float[] h = HOLE[this.hole];
      this.circle(h[0], h[1], 8.0F, -15724528);
      this.line(h[0], h[1], h[0], h[1] - 28.0F, 1.0F, -723724);
      this.box(h[0], h[1] - 28.0F, 12.0F, 7.0F, 0.0F, -1900533);
      if (this.drag) {
         float dx = this.bxx - this.bmx();
         float dy = this.bymY();
         float n = (float)Math.hypot(dx, dy);
         float d = Math.min(120.0F, n);
         if (n > 0.0F) {
            this.line(this.bxx, this.byy, this.bxx + dx / n * d * 0.6F, this.byy + dy / n * d * 0.6F, 1.6F, Gx.mix(-1, -50356, d / 120.0F));
         }
      }

      if (this.sunk < 0.0F) {
         this.circle(this.bxx, this.byy, 5.0F, -1);
      }

      this.textL("Loch " + (this.hole + 1) + "/" + START.length + " · Schläge " + this.strokes, 8.0F, 10.0F, 7.5F, 3, -1);
   }

   private float bymY() {
      return this.byy - this.bmy();
   }
}
