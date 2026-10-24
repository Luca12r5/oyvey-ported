package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Thumbs;
import java.util.ArrayList;
import java.util.List;

final class Darts extends GameView {
   private static final float W = 360.0F;
   private static final float H = 280.0F;
   private static final float CX = 180.0F;
   private static final float CY = 140.0F;
   private static final float R = 115.0F;
   private static final int[] NUM = new int[]{20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5};
   private int darts;
   private final List<float[]> stuck = new ArrayList<>();
   private String msg = "";
   private float msgT;
   private float wob;

   Darts() {
      super("darts");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 280.0F;
   }

   @Override
   protected String overTitle() {
      return "9 Darts geworfen";
   }

   @Override
   protected void reset() {
      this.darts = 9;
      this.stuck.clear();
   }

   private float sx() {
      return this.bmx() + (float)Math.sin(this.time * 2.3) * 9.0F + (float)Math.sin(this.time * 5.1) * 4.0F;
   }

   private float sy() {
      return this.bmy() + (float)Math.cos(this.time * 1.9) * 9.0F + (float)Math.sin(this.time * 4.3) * 4.0F;
   }

   private int points(float x, float y) {
      float dx = x - 180.0F;
      float dy = y - 140.0F;
      float d = (float)Math.hypot(dx, dy) / 115.0F;
      if (d < 0.05F) {
         return 50;
      } else if (d < 0.1F) {
         return 25;
      } else if (d > 1.0F) {
         return 0;
      } else {
         double a = Math.toDegrees(Math.atan2(dx, -dy));
         int seg = (int)Math.floor((a + 9.0 + 360.0) % 360.0 / 18.0);
         int v = NUM[seg];
         if (d > 0.93F) {
            return v * 2;
         } else {
            return d > 0.55F && d < 0.62F ? v * 3 : v;
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (this.darts > 0) {
         float hx = this.sx();
         float hy = this.sy();
         int p = this.points(hx, hy);
         this.score += p;
         this.darts--;
         this.stuck.add(new float[]{hx, hy});
         this.msg = p == 0 ? "Daneben" : (p == 50 ? "BULLSEYE! 50" : String.valueOf(p));
         this.msgT = 1.0F;
         Sound.play("minecraft:entity.arrow.hit", 1.3F, 0.4F);
         if (this.darts == 0) {
            this.wob = 1.2F;
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.msgT = Math.max(0.0F, this.msgT - dt);
      if (this.wob > 0.0F && (this.wob -= dt) <= 0.0F) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 360.0F, 280.0F, 0.0F, -14018030);
      this.circle(180.0F, 140.0F, 129.0F, -15000798);
      int px = Math.max(8, this.S(230.0F));
      Gx.Img img = Thumbs.get("dartboard:" + px, px, px, () -> {
         int[] out = new int[px * px];

         for (int yy = 0; yy < px; yy++) {
            for (int xx = 0; xx < px; xx++) {
               float dx = (xx + 0.5F) / px * 2.0F - 1.0F;
               float dy = (yy + 0.5F) / px * 2.0F - 1.0F;
               float d = (float)Math.hypot(dx, dy);
               if (!(d > 1.0F)) {
                  int seg = (int)Math.floor((Math.toDegrees(Math.atan2(dx, -dy)) + 9.0 + 360.0) % 360.0 / 18.0);
                  int base = seg % 2 == 0 ? -15000798 : -858424;
                  int ring = seg % 2 == 0 ? -1900533 : -16743125;
                  out[yy * px + xx] = d < 0.05F ? -1900533 : (d < 0.1F ? -16743125 : (!(d > 0.93F) && (!(d > 0.55F) || !(d < 0.62F)) ? base : ring));
               }
            }
         }

         return out;
      });
      if (img != null) {
         Gx.image(img, this.X(65.0F), this.Y(25.0F), px, px, -1);
      }

      for (int i = 0; i < 20; i++) {
         double am = Math.toRadians(i * 18 - 90);
         this.text(String.valueOf(NUM[i]), 180.0F + (float)Math.cos(am) * 123.0F, 140.0F + (float)Math.sin(am) * 123.0F, 6.0F, 3, -1);
      }

      for (float[] s : this.stuck) {
         this.circle(s[0], s[1], 2.2F, -13053);
         this.line(s[0], s[1], s[0] + 6.0F, s[1] - 8.0F, 1.0F, -6642771);
      }

      if (this.darts > 0) {
         float x = this.sx();
         float y = this.sy();
         this.ring(x, y, 5.0F, 1.0F, -1);
         this.line(x - 8.0F, y, x + 8.0F, y, 0.6F, -1);
         this.line(x, y - 8.0F, x, y + 8.0F, 0.6F, -1);
      }

      this.textL("Darts: " + this.darts, 8.0F, 12.0F, 8.0F, 3, -1);
      if (this.msgT > 0.0F) {
         this.text(this.msg, 300.0F, 20.0F, 12.0F, 3, Gx.withAlpha(-13053, this.msgT));
      }
   }
}
