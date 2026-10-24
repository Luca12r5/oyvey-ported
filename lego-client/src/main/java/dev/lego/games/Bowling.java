package dev.lego.games;

import dev.lego.ui.Sound;

final class Bowling extends GameView {
   private static final float W = 220.0F;
   private static final float H = 320.0F;
   private final float[][] pins = new float[10][];
   private float bx2;
   private float by2;
   private float bvx;
   private float bvy;
   private float aimX = 110.0F;
   private float curve;
   private float rollT = -1.0F;
   private int frame;
   private int roll;
   private int frameScore;
   private boolean drag;
   private float dsx;
   private float dsy;
   private String msg = "";

   Bowling() {
      super("bowling");
   }

   @Override
   protected float boardW() {
      return 220.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Kegel";
   }

   @Override
   protected String overTitle() {
      return "10 Durchgänge: " + this.score + " Kegel";
   }

   @Override
   protected void reset() {
      this.frame = 1;
      this.roll = 0;
      this.setPins();
      this.ball();
   }

   private void setPins() {
      int k = 0;

      for (int r = 0; r < 4; r++) {
         for (int i = 0; i <= r; i++) {
            this.pins[k++] = new float[]{110.0F + (i - r / 2.0F) * 20.0F, 60 - r * 17, 0.0F, 0.0F, 0.0F};
         }
      }
   }

   private void ball() {
      this.bx2 = 110.0F;
      this.by2 = 290.0F;
      this.bvx = this.bvy = 0.0F;
      this.rollT = -1.0F;
      this.curve = 0.0F;
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.rollT < 0.0F) {
         this.drag = true;
         this.dsx = x;
         this.dsy = y;
      }
   }

   @Override
   protected void onRelease(float x, float y, int b) {
      if (this.drag) {
         this.drag = false;
         float dy = this.dsy - y;
         if (!(dy < 10.0F)) {
            this.bvy = -Math.min(420.0F, 120.0F + dy * 3.0F);
            this.bvx = (x - this.dsx) * 0.6F;
            this.curve = (x - this.dsx) * -0.8F;
            this.rollT = 0.0F;
            Sound.play("minecraft:block.stone.place", 0.6F, 0.5F);
         }
      }
   }

   @Override
   protected void onKey(int k) {
      if (this.rollT < 0.0F && (k == 263 || k == 65)) {
         this.bx2 = Math.max(30.0F, this.bx2 - 6.0F);
      }

      if (this.rollT < 0.0F && (k == 262 || k == 68)) {
         this.bx2 = Math.min(190.0F, this.bx2 + 6.0F);
      }
   }

   @Override
   protected void update(float dt) {
      for (float[] p : this.pins) {
         if (p[2] == 1.0F) {
            p[0] += p[3] * dt;
            p[1] += p[4] * dt;
            p[3] *= 0.95F;
            p[4] *= 0.95F;
         }
      }

      if (!(this.rollT < 0.0F)) {
         this.rollT += dt;
         this.bvx = this.bvx + this.curve * dt;
         this.bx2 = this.bx2 + this.bvx * dt;
         this.by2 = this.by2 + this.bvy * dt;
         if (this.bx2 < 22.0F || this.bx2 > 198.0F) {
            this.bvx = 0.0F;
            this.bx2 = Math.max(16.0F, Math.min(204.0F, this.bx2));
         }

         for (float[] px : this.pins) {
            if (Math.hypot(px[0] - this.bx2, px[1] - this.by2) < 13.0 && px[2] == 0.0F) {
               px[2] = 1.0F;
               px[3] = (px[0] - this.bx2) * 8.0F + this.bvx * 0.3F;
               px[4] = this.bvy * 0.5F;
               Sound.play("minecraft:block.wood.break", 1.3F, 0.4F);
            }

            if (px[2] == 1.0F) {
               for (float[] q : this.pins) {
                  if (q[2] == 0.0F && Math.hypot(px[0] - q[0], px[1] - q[1]) < 10.0 && Math.hypot(px[3], px[4]) > 40.0) {
                     q[2] = 1.0F;
                     q[3] = px[3] * 0.7F + (q[0] - px[0]) * 4.0F;
                     q[4] = px[4] * 0.7F;
                  }
               }
            }
         }

         if (this.by2 < -10.0F || this.rollT > 3.0F) {
            int down = 0;

            for (float[] px : this.pins) {
               if (px[2] == 1.0F) {
                  down++;
               }
            }

            int got = down - this.frameScore;
            this.score += got;
            this.frameScore = down;
            this.msg = down == 10 && this.roll == 0 ? "STRIKE!" : (down == 10 ? "SPARE!" : got + " Kegel");
            if (down == 10) {
               Sound.play("minecraft:entity.player.levelup", 1.3F, 0.4F);
            }

            this.roll++;
            if (this.roll < 2 && down != 10) {
               for (float[] pxx : this.pins) {
                  if (pxx[2] == 1.0F) {
                     pxx[0] = -100.0F;
                     pxx[3] = pxx[4] = 0.0F;
                  }
               }
            } else {
               this.frame++;
               this.roll = 0;
               this.frameScore = 0;
               if (this.frame > 10) {
                  this.gameOver();
                  return;
               }

               this.setPins();
            }

            this.ball();
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 220.0F, 320.0F, 0.0F, -12966892);
      this.boxV(20.0F, 0.0F, 180.0F, 320.0F, 0.0F, -1523574, -2578328);

      for (int i = 0; i < 7; i++) {
         this.line(40 + i * 23, 0.0F, 40 + i * 23, 320.0F, 0.4F, 570425344);
      }

      for (int i = 0; i < 7; i++) {
         float ax = 40 + i * 23;
         this.text("▲", ax, 240.0F, 6.0F, 3, 1440940043);
      }

      for (float[] p : this.pins) {
         if (p[0] > -50.0F) {
            if (p[2] == 0.0F) {
               this.circle(p[0], p[1], 6.0F, -723724);
               this.box(p[0] - 5.0F, p[1] - 2.0F, 10.0F, 3.0F, 1.0F, -1900533);
            } else {
               this.circle(p[0], p[1], 5.0F, -1997212428);
            }
         }
      }

      this.circle(this.bx2, this.by2, 9.0F, -15062422);
      this.circle(this.bx2 - 2.0F, this.by2 - 3.0F, 1.5F, -16119264);
      this.circle(this.bx2 + 2.0F, this.by2 - 3.0F, 1.5F, -16119264);
      if (this.drag) {
         this.line(this.bx2, this.by2, this.bx2 + (this.bmx() - this.dsx), this.by2 - (this.dsy - this.bmy()), 1.2F, -1996488705);
      }

      this.textL("Frame " + Math.min(10, this.frame) + "/10 · Wurf " + (this.roll + 1), 24.0F, 12.0F, 6.8F, 3, -12966892);
      if (!this.msg.isEmpty() && this.rollT < 0.0F) {
         this.text(this.msg, 110.0F, 150.0F, 12.0F, 3, -12966892);
      }

      if (this.rollT < 0.0F) {
         this.text("A/D verschieben · nach oben ziehen", 110.0F, 312.0F, 5.6F, 2, -12966892);
      }
   }
}
