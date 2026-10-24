package dev.lego.games;

import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.List;

final class StarFighter extends GameView {
   private static final float W = 300.0F;
   private static final float H = 320.0F;
   private final List<float[]> shots = new ArrayList<>();
   private final List<float[]> enemies = new ArrayList<>();
   private final List<float[]> eShots = new ArrayList<>();
   private final List<float[]> stars = new ArrayList<>();
   private float px = 150.0F;
   private float cool;
   private float spawn;
   private int hp;

   StarFighter() {
      super("starfighter");
   }

   @Override
   protected float boardW() {
      return 300.0F;
   }

   @Override
   protected float boardH() {
      return 320.0F;
   }

   @Override
   protected void reset() {
      this.shots.clear();
      this.enemies.clear();
      this.eShots.clear();
      this.stars.clear();

      for (int i = 0; i < 40; i++) {
         this.stars.add(new float[]{this.rnd.nextFloat() * 300.0F, this.rnd.nextFloat() * 320.0F, 20.0F + this.rnd.nextFloat() * 80.0F});
      }

      this.hp = 3;
      this.spawn = 1.0F;
   }

   @Override
   protected void update(float dt) {
      this.px = this.px + (Math.max(12.0F, Math.min(288.0F, this.bmx())) - this.px) * Math.min(1.0F, dt * 12.0F);
      this.cool -= dt;
      if (this.cool <= 0.0F) {
         this.cool = 0.22F;
         this.shots.add(new float[]{this.px, 280.0F});
      }

      for (float[] s : this.stars) {
         s[1] += s[2] * dt;
         if (s[1] > 320.0F) {
            s[1] = 0.0F;
            s[0] = this.rnd.nextFloat() * 300.0F;
         }
      }

      this.spawn = this.spawn - dt * (1.0F + this.time / 40.0F);
      if (this.spawn <= 0.0F) {
         this.spawn = 0.9F;
         this.enemies
            .add(
               new float[]{
                  20.0F + this.rnd.nextFloat() * 260.0F,
                  -15.0F,
                  40.0F + this.rnd.nextFloat() * 40.0F,
                  this.rnd.nextFloat() * 6.0F,
                  this.rnd.nextFloat() < 0.2F ? 3.0F : 1.0F
               }
            );
      }

      for (float[] sx : this.shots) {
         sx[1] -= 360.0F * dt;
      }

      this.shots.removeIf(sx -> sx[1] < -10.0F);

      for (float[] e : this.enemies) {
         e[1] += e[2] * dt;
         e[0] += (float)Math.sin(this.time * 2.0F + e[3]) * 40.0F * dt;
         if (this.rnd.nextFloat() < dt * 0.5F) {
            this.eShots.add(new float[]{e[0], e[1] + 8.0F});
         }
      }

      for (float[] sx : this.eShots) {
         sx[1] += 170.0F * dt;
      }

      this.eShots.removeIf(sx -> sx[1] > 330.0F);

      for (int i = this.enemies.size() - 1; i >= 0; i--) {
         float[] ex = this.enemies.get(i);

         for (int j = this.shots.size() - 1; j >= 0; j--) {
            float[] sx = this.shots.get(j);
            if (Math.abs(sx[0] - ex[0]) < 12.0F && Math.abs(sx[1] - ex[1]) < 10.0F) {
               this.shots.remove(j);
               ex[4]--;
               if (ex[4] <= 0.0F) {
                  this.score = this.score + (ex[2] > 60.0F ? 30L : 20L);
                  PackArcade.boom();
                  this.enemies.remove(i);
               }
               break;
            }
         }
      }

      for (int i = this.enemies.size() - 1; i >= 0; i--) {
         float[] ex = this.enemies.get(i);
         if (ex[1] > 340.0F) {
            this.enemies.remove(i);
         } else if (Math.abs(ex[0] - this.px) < 16.0F && Math.abs(ex[1] - 290.0F) < 14.0F) {
            this.enemies.remove(i);
            this.hit();
         }
      }

      for (int ix = this.eShots.size() - 1; ix >= 0; ix--) {
         float[] sx = this.eShots.get(ix);
         if (Math.abs(sx[0] - this.px) < 10.0F && Math.abs(sx[1] - 290.0F) < 10.0F) {
            this.eShots.remove(ix);
            this.hit();
         }
      }
   }

   private void hit() {
      this.hp--;
      Sound.play("minecraft:entity.generic.hurt", 1.0F, 0.5F);
      if (this.hp <= 0) {
         this.gameOver();
      }
   }

   @Override
   protected void render() {
      this.box(0.0F, 0.0F, 300.0F, 320.0F, 0.0F, -16579572);

      for (float[] s : this.stars) {
         this.circle(s[0], s[1], s[2] > 70.0F ? 1.2F : 0.7F, -1426063361);
      }

      for (float[] s : this.shots) {
         this.box(s[0] - 1.2F, s[1] - 6.0F, 2.4F, 8.0F, 1.0F, -8587265);
      }

      for (float[] s : this.eShots) {
         this.circle(s[0], s[1], 2.5F, -38294);
      }

      for (float[] e : this.enemies) {
         this.box(e[0] - 11.0F, e[1] - 7.0F, 22.0F, 14.0F, 5.0F, e[4] >= 2.0F ? -5153537 : -1900533);
         this.circle(e[0], e[1], 3.5F, -7859);
      }

      this.line(this.px, 276.0F, this.px - 12.0F, 298.0F, 3.0F, -2564378);
      this.line(this.px, 276.0F, this.px + 12.0F, 298.0F, 3.0F, -2564378);
      this.circle(this.px, 290.0F, 4.0F, -12920577);
      this.circle(this.px, 302.0F + (float)Math.random() * 2.0F, 3.0F, -20432);

      for (int k = 0; k < 3; k++) {
         this.circle(12 + k * 12, 12.0F, 4.0F, k < this.hp ? -50356 : 1442840575);
      }

      this.text("Maus = steuern · schießt automatisch", 150.0F, 314.0F, 5.5F, 2, -1996488705);
   }
}
