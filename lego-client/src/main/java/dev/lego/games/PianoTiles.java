package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class PianoTiles extends GameView {
   private static final float W = 240.0F;
   private static final float H = 300.0F;
   private static final float TH = 70.0F;
   private final List<float[]> tiles = new ArrayList<>();
   private float speed;
   private float wrongT = -1.0F;
   private int wrongCol = -1;

   PianoTiles() {
      super("pianotiles");
   }

   @Override
   protected float boardW() {
      return 240.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected void reset() {
      this.tiles.clear();
      this.speed = 150.0F;
      this.wrongT = -1.0F;

      for (int i = 0; i < 6; i++) {
         this.tiles.add(new float[]{this.rnd.nextInt(4), -i * 70.0F, 0.0F});
      }
   }

   private float[] lowest() {
      float[] l = null;

      for (float[] t : this.tiles) {
         if (t[2] == 0.0F && (l == null || t[1] > l[1])) {
            l = t;
         }
      }

      return l;
   }

   private void press(int col) {
      if (!(this.wrongT >= 0.0F)) {
         float[] l = this.lowest();
         if (l != null) {
            if ((int)l[0] == col) {
               l[2] = 1.0F;
               this.score++;
               Sound.play("minecraft:block.note_block.harp", 0.7F + (float)(this.score % 8L) * 0.12F, 0.5F);
               this.speed += 2.5F;
            } else {
               this.wrongCol = col;
               this.wrongT = 0.0F;
               PackPuzzle.bad();
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      this.press((int)Math.max(0.0F, Math.min(3.0F, x / 60.0F)));
   }

   @Override
   protected void onKey(int k) {
      if (k == 68) {
         this.press(0);
      } else if (k == 70) {
         this.press(1);
      } else if (k == 74) {
         this.press(2);
      } else if (k == 75) {
         this.press(3);
      }
   }

   @Override
   protected void update(float dt) {
      if (this.wrongT >= 0.0F) {
         if ((this.wrongT += dt) > 0.8F) {
            this.gameOver();
         }
      } else {
         for (float[] t : this.tiles) {
            t[1] += this.speed * dt;
         }

         float top = 0.0F;

         for (float[] t : this.tiles) {
            top = Math.min(top, t[1]);
         }

         while (top > -70.0F) {
            top -= 70.0F;
            this.tiles.add(new float[]{this.rnd.nextInt(4), top, 0.0F});
         }

         for (float[] t : this.tiles) {
            if (t[2] == 0.0F && t[1] > 300.0F) {
               this.wrongCol = (int)t[0];
               this.wrongT = 0.0F;
               PackPuzzle.bad();
               return;
            }
         }

         this.tiles.removeIf(tx -> tx[1] > 370.0F);
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 240.0F, 300.0F, 0.0F, -723724);

      for (int c = 1; c < 4; c++) {
         this.box(c * 240.0F / 4.0F, 0.0F, 0.8F, 300.0F, 0.0F, -3092264);
      }

      for (float[] t : this.tiles) {
         this.box(t[0] * 240.0F / 4.0F + 1.0F, t[1] + 1.0F, 58.0F, 68.0F, 3.0F, t[2] == 1.0F ? -5196612 : -15395302);
      }

      if (this.wrongT >= 0.0F && this.wrongCol >= 0) {
         this.box(this.wrongCol * 240.0F / 4.0F, 0.0F, 60.0F, 300.0F, 0.0F, Gx.withAlpha(-50356, 0.5F));
      }

      this.text(String.valueOf(this.score), 120.0F, 22.0F, 16.0F, 3, -1900533);
      this.text("D  F  J  K", 120.0F, 290.0F, 6.5F, 2, -6642766);
   }
}
