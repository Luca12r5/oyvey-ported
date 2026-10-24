package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class ColorSwitch extends GameView {
   private static final float W = 240.0F;
   private static final float H = 320.0F;
   private static final int[] COL = new int[]{-54678, -11713, -12920577, -6599169};
   private float by2;
   private float vy;
   private float cam;
   private int color;
   private final List<float[]> rings = new ArrayList<>();

   ColorSwitch() {
      super("colorswitch");
   }

   @Override
   protected float boardW() {
      return 240.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected void reset() {
      this.by2 = 260.0F;
      this.vy = 0.0F;
      this.cam = 0.0F;
      this.color = 0;
      this.rings.clear();

      for (int i = 0; i < 4; i++) {
         this.rings.add(new float[]{120 - i * 170, 1.0F + this.rnd.nextFloat(), 0.0F, 0.0F});
      }
   }

   private void jump() {
      this.vy = -260.0F;
      Sound.play("minecraft:block.note_block.hat", 1.5F, 0.2F);
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.jump();
   }

   @Override
   protected void onKey(int k) {
      if (k == 32 || k == 265 || k == 87) {
         this.jump();
      }
   }

   @Override
   protected void update(float dt) {
      if (this.score > 0L || this.vy != 0.0F) {
         this.vy += 700.0F * dt;
      }

      this.by2 = this.by2 + this.vy * dt;
      if (this.by2 - this.cam < 144.0F) {
         this.cam = this.by2 - 144.0F;
      }

      if (this.by2 - this.cam > 330.0F) {
         this.gameOver();
      } else {
         float top = 1.0E9F;

         for (float[] r : this.rings) {
            top = Math.min(top, r[0]);
         }

         if (top > this.cam - 200.0F) {
            this.rings.add(new float[]{top - 170.0F, 1.0F + this.rnd.nextFloat() * (1.0F + (float)this.score * 0.08F), 0.0F, 0.0F});
         }

         this.rings.removeIf(rx -> rx[0] - this.cam > 400.0F);

         for (float[] r : this.rings) {
            float rot = (float)(this.time * r[1] * 1.6);

            for (float edge : new float[]{r[0] - 46.0F, r[0] + 46.0F}) {
               if (Math.abs(this.by2 - edge) < 7.0F) {
                  double a = edge < r[0] ? -Math.PI / 2 : Math.PI / 2;
                  int seg = (int)Math.floor(((a - rot) % (Math.PI * 2) + (Math.PI * 4)) % (Math.PI * 2) / (Math.PI / 2));
                  if (seg != this.color) {
                     PackArcade.boom();
                     this.gameOver();
                     return;
                  }
               }
            }

            if (r[2] == 0.0F && this.by2 < r[0] - 46.0F) {
               r[2] = 1.0F;
               this.score++;
               Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
            }

            if (r[3] == 0.0F && Math.abs(this.by2 - (r[0] - 85.0F)) < 10.0F) {
               r[3] = 1.0F;

               int c;
               do {
                  c = this.rnd.nextInt(4);
               } while (c == this.color);

               this.color = c;
               Sound.play("minecraft:block.amethyst_block.chime", 1.4F, 0.5F);
            }
         }
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 240.0F, 320.0F, 0.0F, -15396321);

      for (float[] r : this.rings) {
         float cy = r[0] - this.cam;
         float rot = (float)(this.time * r[1] * 1.6);

         for (int i = 0; i < 40; i++) {
            double a = rot + (Math.PI * 2) * i / 40.0;
            int seg = (int)Math.floor(i / 40.0 * 4.0) % 4;
            this.circle(120.0F + (float)Math.cos(a) * 46.0F, cy + (float)Math.sin(a) * 46.0F, 4.2F, COL[seg]);
         }

         if (r[3] == 0.0F) {
            float sy = r[0] - 85.0F - this.cam;

            for (int i = 0; i < 4; i++) {
               this.circle(120.0F + (i % 2 == 0 ? -3 : 3), sy + (i / 2 == 0 ? -3 : 3), 3.5F, COL[i]);
            }
         }
      }

      this.circle(120.0F, this.by2 - this.cam, 7.0F, COL[this.color]);
      this.text(String.valueOf(this.score), 120.0F, 18.0F, 14.0F, 3, -1);
      if (this.vy == 0.0F && this.score == 0L) {
         this.text("Klick / Leertaste zum Springen", 120.0F, 300.0F, 6.6F, 2, -1426063361);
      }
   }
}
