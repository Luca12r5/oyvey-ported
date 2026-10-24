package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.Locale;

public final class WhackAMole extends GameView {
   private static final float W = 360.0F;
   private static final float H = 260.0F;
   private final float[] up = new float[9];
   private final float[] timer = new float[9];
   private final float[] hitFx = new float[9];
   private final int[] kind = new int[9];
   private float spawn;
   private float left;
   private int combo;

   public WhackAMole() {
      super("whackamole");
   }

   @Override
   protected float boardW() {
      return 360.0F;
   }

   @Override
   protected float boardH() {
      return 260.0F;
   }

   @Override
   protected String overTitle() {
      return "Zeit um!";
   }

   @Override
   protected void reset() {
      for (int i = 0; i < 9; i++) {
         this.up[i] = 0.0F;
         this.timer[i] = 0.0F;
         this.hitFx[i] = 0.0F;
      }

      this.spawn = 0.6F;
      this.left = 45.0F;
      this.combo = 0;
   }

   private float hx(int i) {
      return 70 + i % 3 * 110;
   }

   private float hy(int i) {
      return 70 + i / 3 * 70;
   }

   @Override
   protected void update(float dt) {
      this.left -= dt;
      if (this.left <= 0.0F) {
         this.left = 0.0F;
         this.gameOver();
      } else {
         float pace = 1.0F + (45.0F - this.left) / 30.0F;
         this.spawn -= dt * pace;
         if (this.spawn <= 0.0F) {
            this.spawn = 0.5F + this.rnd.nextFloat() * 0.5F;
            int i = this.rnd.nextInt(9);
            if (this.timer[i] <= 0.0F) {
               this.timer[i] = 0.9F + this.rnd.nextFloat() * 0.6F / pace;
               float r = this.rnd.nextFloat();
               this.kind[i] = r < 0.1F ? 1 : (r < 0.22F ? 2 : 0);
            }
         }

         for (int i = 0; i < 9; i++) {
            this.hitFx[i] = Math.max(0.0F, this.hitFx[i] - dt * 2.5F);
            if (this.timer[i] > 0.0F) {
               this.timer[i] = this.timer[i] - dt;
               this.up[i] = Math.min(1.0F, this.up[i] + dt * 7.0F);
               if (this.timer[i] <= 0.0F && this.kind[i] != 2) {
                  this.combo = 0;
               }
            } else {
               this.up[i] = Math.max(0.0F, this.up[i] - dt * 7.0F);
            }
         }
      }
   }

   @Override
   protected void onClick(float x, float y, int b) {
      for (int i = 0; i < 9; i++) {
         if (!(this.up[i] < 0.5F) && !(this.timer[i] <= 0.0F) && !(Math.hypot(x - this.hx(i), y - (this.hy(i) - 14.0F)) > 24.0)) {
            this.timer[i] = 0.0F;
            this.hitFx[i] = 1.0F;
            if (this.kind[i] == 2) {
               this.score = Math.max(0L, this.score - 50L);
               this.combo = 0;
               Sound.play("minecraft:entity.generic.explode", 1.5F, 0.3F);
            } else {
               this.combo++;
               this.score = this.score + (this.kind[i] == 1 ? 30 : 10) * Math.min(5, 1 + this.combo / 4);
               Sound.play("minecraft:entity.item.pickup", 1.4F + Math.min(0.6F, this.combo * 0.05F), 0.35F);
            }

            return;
         }
      }

      this.combo = 0;
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 360.0F, 260.0F, 10.0F, -10634418, -12677567);

      for (int i = 0; i < 9; i++) {
         float x = this.hx(i);
         float y = this.hy(i);
         this.circle(x, y + 6.0F, 26.0F, -13805021);
         this.circle(x, y + 4.0F, 24.0F, -14674934);
         float h = this.up[i] * 26.0F;
         if (h > 1.0F) {
            int body = this.kind[i] == 2 ? -14803422 : (this.kind[i] == 1 ? -13053 : -7710158);
            Gx.clip(this.X(x - 26.0F), this.Y(y - 60.0F), this.S(52.0F), this.S(64.0F));
            this.box(x - 15.0F, y - h - 4.0F, 30.0F, 30.0F, 10.0F, body);
            if (this.kind[i] == 2) {
               this.circle(x, y - h + 8.0F, 10.0F, -14013906);
               this.box(x - 1.0F, y - h - 10.0F, 2.0F, 8.0F, 0.0F, -30208);
               this.circle(x, y - h - 11.0F, 2.5F, -7859);
            } else {
               this.circle(x - 6.0F, y - h + 4.0F, 3.0F, -1);
               this.circle(x + 6.0F, y - h + 4.0F, 3.0F, -1);
               this.circle(x - 6.0F, y - h + 4.5F, 1.5F, -15658735);
               this.circle(x + 6.0F, y - h + 4.5F, 1.5F, -15658735);
               this.circle(x, y - h + 10.0F, 3.0F, -28752);
               this.box(x - 4.0F, y - h - 6.0F, 8.0F, 4.0F, 2.0F, Gx.mix(body, -1, 0.3F));
            }

            Gx.unclip();
         }

         this.box(x - 26.0F, y + 4.0F, 52.0F, 10.0F, 5.0F, -13805021);
         if (this.hitFx[i] > 0.0F) {
            this.text(
               this.kind[i] == 2 ? "-50" : "BONK!",
               x,
               y - 40.0F - (1.0F - this.hitFx[i]) * 16.0F,
               9.0F,
               3,
               Gx.withAlpha(this.kind[i] == 2 ? -45730 : -1, this.hitFx[i])
            );
         }
      }

      this.text(String.format(Locale.ROOT, "%.0f s", this.left), 326.0F, 16.0F, 9.0F, 3, this.left < 10.0F ? -45730 : -1);
      if (this.combo >= 4) {
         this.text("Combo x" + Math.min(5, 1 + this.combo / 4), 50.0F, 16.0F, 8.0F, 3, -13053);
      }
   }
}
