package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Snake extends GameView {
   private static final int C = 22;
   private static final int R = 16;
   private static final float CELL = 16.0F;
   private final ArrayDeque<int[]> body = new ArrayDeque<>();
   private final ArrayDeque<int[]> queue = new ArrayDeque<>();
   private int dx = 1;
   private int dy = 0;
   private int[] food;
   private float step = 0.0F;
   private float speed = 0.12F;
   private final List<float[]> pops = new ArrayList<>();

   public Snake() {
      super("snake");
   }

   @Override
   protected float boardW() {
      return 352.0F;
   }

   @Override
   protected float boardH() {
      return 256.0F;
   }

   @Override
   protected void reset() {
      this.body.clear();
      this.queue.clear();

      for (int var1 = 0; var1 < 4; var1++) {
         this.body.addFirst(new int[]{4 + var1, 8});
      }

      this.dx = 1;
      this.dy = 0;
      this.speed = 0.12F;
      this.step = 0.0F;
      this.pops.clear();
      this.placeFood();
   }

   private void placeFood() {
      int[] var1;
      boolean var2;
      do {
         var1 = new int[]{this.rnd.nextInt(22), this.rnd.nextInt(16)};
         var2 = false;

         for (int[] var4 : this.body) {
            if (var4[0] == var1[0] && var4[1] == var1[1]) {
               var2 = true;
               break;
            }
         }
      } while (var2);

      this.food = var1;
   }

   @Override
   protected void onKey(int var1) {
      int[] var2 = null;
      if (var1 == 263 || var1 == 65) {
         var2 = new int[]{-1, 0};
      }

      if (var1 == 262 || var1 == 68) {
         var2 = new int[]{1, 0};
      }

      if (var1 == 265 || var1 == 87) {
         var2 = new int[]{0, -1};
      }

      if (var1 == 264 || var1 == 83) {
         var2 = new int[]{0, 1};
      }

      if (var2 != null && this.queue.size() <= 2) {
         int[] var3 = this.queue.isEmpty() ? new int[]{this.dx, this.dy} : this.queue.peekLast();
         if (var2[0] != -var3[0] || var2[1] != -var3[1]) {
            if (var2[0] != var3[0] || var2[1] != var3[1]) {
               this.queue.add(var2);
            }
         }
      }
   }

   @Override
   protected void update(float var1) {
      for (float[] var3 : this.pops) {
         var3[2] += var1;
      }

      this.pops.removeIf(var0 -> var0[2] > 0.5F);
      this.step += var1;
      if (!(this.step < this.speed)) {
         this.step = this.step - this.speed;
         if (!this.queue.isEmpty()) {
            int[] var10 = this.queue.poll();
            this.dx = var10[0];
            this.dy = var10[1];
         }

         int[] var11 = this.body.peekFirst();
         int var12 = var11[0] + this.dx;
         int var4 = var11[1] + this.dy;
         if (var12 >= 0 && var4 >= 0 && var12 < 22 && var4 < 16) {
            boolean var5 = var12 == this.food[0] && var4 == this.food[1];
            int var6 = 0;
            int var7 = this.body.size();

            for (int[] var9 : this.body) {
               if (!var5 && var6 == var7 - 1) {
                  break;
               }

               if (var9[0] == var12 && var9[1] == var4) {
                  this.gameOver();
                  return;
               }

               var6++;
            }

            this.body.addFirst(new int[]{var12, var4});
            if (var5) {
               this.score += 10L;
               this.pops.add(new float[]{this.food[0], this.food[1], 0.0F});
               this.speed = Math.max(0.055F, this.speed * 0.965F);
               Sound.play("minecraft:entity.experience_orb.pickup", 1.2F + this.rnd.nextFloat() * 0.3F, 0.25F);
               this.placeFood();
            } else {
               this.body.removeLast();
            }
         } else {
            this.gameOver();
         }
      }
   }

   @Override
   protected void render() {
      for (int var1 = 0; var1 < 16; var1++) {
         for (int var2 = 0; var2 < 22; var2++) {
            if ((var2 + var1) % 2 == 0) {
               this.box(var2 * 16.0F, var1 * 16.0F, 16.0F, 16.0F, 0.0F, Style.light ? 167772160 : 150994943);
            }
         }
      }

      float var15 = 1.0F + 0.08F * (float)Math.sin(this.time * 8.0F);
      float var16 = 12.8F * var15;
      this.brick(this.food[0] * 16.0F + (16.0F - var16) / 2.0F, this.food[1] * 16.0F + (16.0F - var16) / 2.0F, var16, var16, -13053);
      int var3 = this.body.size();
      int var4 = 0;
      if (this.state == GameView.State.PLAYING) {
         Math.min(1.0F, this.step / this.speed);
      } else {
         float var10000 = 1.0F;
      }

      for (int[] var7 : this.body) {
         float var8 = (float)var4 / Math.max(1, var3 - 1);
         int var9 = Gx.mix(Style.accent, Gx.mix(Style.accent, -16777216, 0.35F), var8);
         float var10 = var4 == 0 ? 0.5F : 1.2F + var8 * 1.2F;
         this.brick(var7[0] * 16.0F + var10, var7[1] * 16.0F + var10, 16.0F - var10 * 2.0F, 16.0F - var10 * 2.0F, var9);
         if (var4 == 0) {
            float var11 = var7[0] * 16.0F + 8.0F + this.dx * 3;
            float var12 = var7[1] * 16.0F + 8.0F + this.dy * 3;
            float var13 = this.dy != 0 ? 3.2F : 0.0F;
            float var14 = this.dx != 0 ? 3.2F : 0.0F;
            this.circle(var11 + var13, var12 + var14, 1.8F, -1);
            this.circle(var11 - var13, var12 - var14, 1.8F, -1);
            this.circle(var11 + var13 + this.dx * 0.6F, var12 + var14 + this.dy * 0.6F, 0.9F, -15658735);
            this.circle(var11 - var13 + this.dx * 0.6F, var12 - var14 + this.dy * 0.6F, 0.9F, -15658735);
         }

         var4++;
      }

      for (float[] var18 : this.pops) {
         float var19 = 1.0F - var18[2] / 0.5F;
         this.text("+10", var18[0] * 16.0F + 8.0F, var18[1] * 16.0F - var18[2] * 30.0F, 8.0F, 3, Gx.withAlpha(-13053, var19));
      }
   }
}
