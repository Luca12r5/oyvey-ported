package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class MissileCommand extends GameView {
   private static final float W = 400.0F;
   private static final float H = 270.0F;
   private static final float GY = 250.0F;
   private final List<float[]> missiles = new ArrayList<>();
   private final List<float[]> blasts = new ArrayList<>();
   private final List<float[]> shots = new ArrayList<>();
   private final boolean[] city = new boolean[6];
   private float spawn;
   private float wave;

   MissileCommand() {
      super("missiles");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 270.0F;
   }

   @Override
   protected String overTitle() {
      return "Alle Städte zerstört";
   }

   @Override
   protected void reset() {
      this.missiles.clear();
      this.blasts.clear();
      this.shots.clear();
      Arrays.fill(this.city, true);
      this.spawn = 1.0F;
      this.wave = 0.0F;
   }

   private float cx(int i) {
      return 40 + i * 64 + (i >= 3 ? 10 : 0);
   }

   @Override
   protected void onClick(float x, float y, int b) {
      if (y < 230.0F) {
         this.shots.add(new float[]{200.0F, 240.0F, x, y});
         Sound.play("minecraft:entity.firework_rocket.launch", 1.5F, 0.25F);
      }
   }

   @Override
   protected void update(float dt) {
      this.wave += dt;
      this.spawn = this.spawn - dt * (1.0F + this.wave / 40.0F);
      if (this.spawn <= 0.0F) {
         this.spawn = 1.6F;
         int t = this.rnd.nextInt(6);
         this.missiles
            .add(new float[]{this.rnd.nextFloat() * 400.0F, 0.0F, 0.0F, 0.0F, this.cx(t) + 12.0F, 22.0F + this.wave * 0.6F + this.rnd.nextFloat() * 10.0F});
         float[] m = this.missiles.get(this.missiles.size() - 1);
         m[2] = m[0];
         m[3] = 0.0F;
      }

      for (int i = this.shots.size() - 1; i >= 0; i--) {
         float[] s = this.shots.get(i);
         float dx = s[2] - s[0];
         float dy = s[3] - s[1];
         float d = (float)Math.hypot(dx, dy);
         float step = 320.0F * dt;
         if (d <= step) {
            this.blasts.add(new float[]{s[2], s[3], 0.0F});
            this.shots.remove(i);
            Sound.play("minecraft:entity.firework_rocket.blast", 1.2F, 0.3F);
         } else {
            s[0] += dx / d * step;
            s[1] += dy / d * step;
         }
      }

      for (float[] bl : this.blasts) {
         bl[2] += dt;
      }

      this.blasts.removeIf(blx -> blx[2] > 1.1F);

      for (int ix = this.missiles.size() - 1; ix >= 0; ix--) {
         float[] m = this.missiles.get(ix);
         float dx = m[4] - m[0];
         float dy = 250.0F;
         float d = (float)Math.hypot(dx, dy);
         m[2] += dx / d * m[5] * dt;
         m[3] += dy / d * m[5] * dt;
         boolean dead = false;

         for (float[] bl : this.blasts) {
            if (Math.hypot(m[2] - bl[0], m[3] - bl[1]) < this.radius(bl[2])) {
               dead = true;
            }
         }

         if (dead) {
            this.missiles.remove(ix);
            this.score += 25L;
         } else if (m[3] >= 246.0F) {
            this.missiles.remove(ix);
            PackArcade.boom();

            for (int c = 0; c < 6; c++) {
               if (this.city[c] && Math.abs(this.cx(c) + 12.0F - m[2]) < 22.0F) {
                  this.city[c] = false;
               }
            }

            boolean any = false;

            for (boolean b : this.city) {
               any |= b;
            }

            if (!any) {
               this.gameOver();
            }
         }
      }
   }

   private float radius(float t) {
      return t < 0.5F ? t / 0.5F * 26.0F : 26.0F * (1.0F - (t - 0.5F) / 0.6F);
   }

   @Override
   protected void render() {
      this.boxV(0.0F, 0.0F, 400.0F, 270.0F, 0.0F, -16381928, -15068096);
      this.box(0.0F, 250.0F, 400.0F, 20.0F, 0.0F, -3628960);

      for (int i = 0; i < 6; i++) {
         if (this.city[i]) {
            this.brick(this.cx(i), 236.0F, 24.0F, 14.0F, -12604929);
            this.box(this.cx(i) + 6.0F, 228.0F, 8.0F, 8.0F, 2.0F, -8730369);
         }
      }

      this.box(188.0F, 236.0F, 24.0F, 14.0F, 4.0F, -6642771);

      for (float[] m : this.missiles) {
         this.line(m[0], 0.0F, m[2], m[3], 1.0F, -1996534434);
         this.circle(m[2], m[3], 2.2F, -7859);
      }

      for (float[] s : this.shots) {
         this.line(200.0F, 240.0F, s[0], s[1], 1.0F, -2009476269);
         this.circle(s[0], s[1], 2.0F, -1);
      }

      for (float[] bl : this.blasts) {
         float r = this.radius(bl[2]);
         this.circle(bl[0], bl[1], r, Gx.withAlpha(-20432, 0.7F));
         this.circle(bl[0], bl[1], r * 0.6F, -855640384);
      }

      float hx = this.bmx();
      float hy = this.bmy();
      if (hy < 230.0F) {
         this.line(hx - 5.0F, hy, hx + 5.0F, hy, 1.0F, -1);
         this.line(hx, hy - 5.0F, hx, hy + 5.0F, 1.0F, -1);
      }
   }
}
