package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CartHill extends GameView {
   private static final float W = 480.0F;
   private static final float H = 300.0F;
   private static final int SEG = 200;
   private static final int DRAW = 140;
   private static final int LANES = 3;
   private static final float ROAD = 1900.0F;
   private static final float CAM_H = 950.0F;
   private static final float DEPTH = 0.84F;
   private final List<CartHill.Seg> road = new ArrayList<>();
   private final Set<Integer> keys = new HashSet<>();
   private float pos;
   private float speed;
   private float px;
   private float air;
   private float vy;
   private float crash;
   private float stunt;
   private float lastStunt;
   private int lives;
   private float shake;

   public CartHill() {
      super("carthill");
   }

   @Override
   protected float boardW() {
      return 480.0F;
   }

   @Override
   protected float boardH() {
      return 300.0F;
   }

   @Override
   protected String scoreLabel() {
      return "Punkte";
   }

   @Override
   protected String overTitle() {
      return "Totalschaden!";
   }

   private CartHill.Seg seg(int i) {
      while (this.road.size() <= i + 140 + 2) {
         this.grow();
      }

      return this.road.get(i);
   }

   private void grow() {
      int n = 30 + this.rnd.nextInt(40);
      float curve = this.rnd.nextFloat() < 0.6F ? (this.rnd.nextFloat() - 0.5F) * 5.0F : 0.0F;
      float hill = this.rnd.nextFloat() < 0.5F ? (this.rnd.nextFloat() - 0.4F) * 2600.0F : 0.0F;
      float y0 = this.road.isEmpty() ? 0.0F : this.road.get(this.road.size() - 1).y;
      int base = this.road.size();

      for (int i = 0; i < n; i++) {
         CartHill.Seg s = new CartHill.Seg();
         float t = (float)i / n;
         float e = (float)(0.5 - 0.5 * Math.cos(Math.PI * t));
         s.curve = curve * (float)Math.sin(Math.PI * t);
         s.y = y0 + hill * e - (base + i) * 6.0F;
         int idx = base + i;
         if (idx > 40 && i % 6 == 0) {
            float r = this.rnd.nextFloat();
            float density = Math.min(0.75F, 0.35F + idx / 6000.0F);
            if (r < density) {
               s.kind = r < density * 0.45F ? 0 : (r < density * 0.7F ? 1 : (r < density * 0.85F ? 2 : 3));
               s.ox = (this.rnd.nextInt(3) - 1) * 0.62F;
            }
         }

         this.road.add(s);
      }
   }

   @Override
   protected void reset() {
      this.road.clear();
      this.keys.clear();
      this.pos = 0.0F;
      this.speed = 0.0F;
      this.px = 0.0F;
      this.air = 0.0F;
      this.vy = 0.0F;
      this.crash = 0.0F;
      this.stunt = 0.0F;
      this.lastStunt = 0.0F;
      this.shake = 0.0F;
      this.lives = 3;
      this.seg(0);
   }

   @Override
   protected void onKey(int k) {
      this.keys.add(k);
   }

   @Override
   protected void onKeyUp(int k) {
      this.keys.remove(k);
   }

   private boolean down(int a, int b) {
      return this.keys.contains(a) || this.keys.contains(b);
   }

   @Override
   protected void update(float dt) {
      this.shake = Math.max(0.0F, this.shake - dt * 3.0F);
      this.lastStunt = Math.max(0.0F, this.lastStunt - dt);
      float max = 9000.0F + Math.min(5000.0F, this.pos / 400.0F);
      if (this.crash > 0.0F) {
         this.crash -= dt;
         this.speed = this.speed * (float)Math.pow(0.2, dt);
      } else {
         if (this.down(87, 265)) {
            this.speed += 4200.0F * dt;
         } else {
            this.speed += 1400.0F * dt;
         }

         if (this.down(83, 264)) {
            this.speed -= 6000.0F * dt;
         }

         float steer = (this.down(68, 262) ? 1 : 0) - (this.down(65, 263) ? 1 : 0);
         this.px = this.px + steer * dt * 2.2F * Math.min(1.0F, this.speed / 4000.0F) * (this.air > 0.0F ? 0.4F : 1.0F);
      }

      CartHill.Seg cur = this.seg((int)(this.pos / 200.0F));
      this.px = this.px - cur.curve * dt * (this.speed / max) * 0.55F;
      if (Math.abs(this.px) > 1.05F && this.air <= 0.0F) {
         this.speed = this.speed * (float)Math.pow(0.35, dt);
      }

      this.px = Math.max(-2.2F, Math.min(2.2F, this.px));
      this.speed = Math.max(0.0F, Math.min(max, this.speed));
      this.pos = this.pos + this.speed * dt;
      this.score = (long)(this.pos / 100.0F) + (long)this.stunt;
      if (this.air > 0.0F || this.vy > 0.0F) {
         this.vy -= 2600.0F * dt;
         this.air = this.air + this.vy * dt;
         this.stunt += dt * 40.0F;
         if (this.air <= 0.0F) {
            this.air = 0.0F;
            this.vy = 0.0F;
            if (this.lastStunt <= 0.0F) {
               this.lastStunt = 1.2F;
            }

            Sound.play("minecraft:block.anvil.land", 1.6F, 0.15F);
         }
      }

      for (int k = 1; k <= 2; k++) {
         CartHill.Seg s = this.seg((int)(this.pos / 200.0F) + k);
         if (s.kind >= 0 && !s.taken && !(this.air > 60.0F) && !(Math.abs(s.ox - this.px) > 0.28F)) {
            float dz = (int)(this.pos / 200.0F + k) * 200 - this.pos;
            if (!(dz > 240.00002F)) {
               s.taken = true;
               switch (s.kind) {
                  case 0:
                     this.stunt += 25.0F;
                     Sound.play("minecraft:entity.experience_orb.pickup", 1.5F, 0.25F);
                     break;
                  case 3:
                     this.vy = 900.0F + this.speed * 0.06F;
                     this.air = 1.0F;
                     Sound.play("minecraft:entity.firework_rocket.launch", 1.2F, 0.3F);
                     break;
                  default:
                     this.crash = 1.1F;
                     this.shake = 1.0F;
                     this.speed *= 0.25F;
                     this.lives--;
                     Sound.play("minecraft:entity.generic.explode", 1.4F, 0.3F);
                     if (this.lives <= 0) {
                        this.gameOver();
                     }
               }
            }
         }
      }
   }

   private float[] project(float x, float y, float z, float camX, float camY, float camZ) {
      float dz = Math.max(1.0F, z - camZ);
      float sc = 0.84F / dz;
      return new float[]{240.0F + sc * (x - camX) * 480.0F / 2.0F, 150.0F - sc * (y - camY) * 300.0F / 2.0F, sc * 1900.0F * 480.0F / 2.0F};
   }

   @Override
   protected void render() {
      Gx.clip(this.X(0.0F), this.Y(0.0F), this.S(480.0F), this.S(300.0F));
      float sx0 = this.shake > 0.0F ? (float)Math.sin(this.time * 60.0F) * 4.0F * this.shake : 0.0F;
      this.boxV(0.0F, 0.0F, 480.0F, 300.0F, 0.0F, -11563048, -4201473);

      for (int i = 0; i < 7; i++) {
         float mx = ((i * 97 - this.pos * 0.004F) % 560.0F + 560.0F) % 560.0F - 60.0F;
         this.box(mx, 110.0F - i % 3 * 12, 90.0F, 60 + i % 3 * 12, 30.0F, -7362103);
         this.box(mx + 30.0F, 110.0F - i % 3 * 12, 30.0F, 12.0F, 6.0F, -722689);
      }

      int base = (int)(this.pos / 200.0F);
      float pct = this.pos % 200.0F / 200.0F;
      CartHill.Seg b = this.seg(base);
      float camY = b.y + (this.seg(base + 1).y - b.y) * pct + 950.0F + this.air;
      float camX = this.px * 1900.0F;
      float camZ = this.pos - 0.0F;
      float x = 0.0F;
      float dx = -b.curve * pct;
      float maxY = 300.0F;
      float[][] proj = new float[140][];
      float[] segX = new float[140];

      for (int n = 0; n < 140; n++) {
         int i = base + n;
         CartHill.Seg s = this.seg(i);
         float z1 = i * 200.0F;
         float z2 = (i + 1) * 200.0F;
         float[] p1 = this.project(x, s.y, z1, camX, camY, camZ);
         x += dx;
         dx += s.curve * 22.0F;
         float[] p2 = this.project(x, this.seg(i + 1).y, z2, camX, camY, camZ);
         segX[n] = x;
         proj[n] = p1;
         if (!(p1[1] <= p2[1]) && !(p2[1] >= maxY)) {
            float top = p2[1];
            float bot = Math.min(p1[1], maxY);
            boolean alt = i / 3 % 2 == 0;
            this.box(0.0F, top, 480.0F, bot - top + 1.0F, 0.0F, alt ? -1050881 : -1970693);
            float rw = p2[2] * 1.15F;
            this.box(p2[0] - rw + sx0, top, rw * 2.0F, bot - top + 1.0F, 0.0F, alt ? -1900533 : -1);
            this.box(p2[0] - p2[2] + sx0, top, p2[2] * 2.0F, bot - top + 1.0F, 0.0F, alt ? -11907496 : -11381153);
            if (alt) {
               float lw = Math.max(1.0F, p2[2] * 0.03F);
               this.box(p2[0] - p2[2] / 3.0F - lw / 2.0F + sx0, top, lw, bot - top + 1.0F, 0.0F, -855638017);
               this.box(p2[0] + p2[2] / 3.0F - lw / 2.0F + sx0, top, lw, bot - top + 1.0F, 0.0F, -855638017);
            }

            maxY = top;
         }
      }

      for (int nx = 139; nx >= 1; nx--) {
         CartHill.Seg s = this.seg(base + nx);
         if (s.kind >= 0 && !s.taken && proj[nx] != null) {
            float[] p = proj[nx];
            float sc = p[2] / 456000.0F;
            float ox = p[0] + s.ox * p[2] + sx0;
            float oy = p[1];
            if (!(oy > 320.0F) && !(oy < 0.0F)) {
               float u = Math.max(0.6F, p[2] * 0.12F);
               switch (s.kind) {
                  case 0:
                     this.circle(ox, oy - u * 1.2F, u * 0.7F, -13053);
                     this.circle(ox - u * 0.2F, oy - u * 1.4F, u * 0.3F, -3424);
                     break;
                  case 1:
                     this.box(ox - u * 0.6F, oy - u * 0.3F, u * 1.2F, u * 0.3F, 0.0F, -38400);
                     this.box(ox - u * 0.35F, oy - u * 1.6F, u * 0.7F, u * 1.3F, u * 0.2F, -30208);
                     this.box(ox - u * 0.36F, oy - u * 1.05F, u * 0.72F, u * 0.25F, 0.0F, -1);
                     break;
                  case 2:
                     this.box(ox - u, oy - u * 1.8F, u * 2.0F, u * 1.8F, u * 0.15F, -6657478);
                     this.box(ox - u, oy - u * 1.0F, u * 2.0F, u * 0.2F, 0.0F, -9549272);
                     break;
                  default:
                     this.box(ox - u * 1.4F, oy - u * 0.8F, u * 2.8F, u * 0.8F, u * 0.2F, -12604929);
                     this.box(ox - u * 1.4F, oy - u * 0.8F, u * 2.8F, u * 0.2F, 0.0F, -1);
                     this.text("▲", ox, oy - u * 0.4F, Math.max(4.0F, u * 0.5F), 3, -1);
               }
            }
         }
      }

      this.drawCart(240.0F + sx0, 266.0F - this.air * 0.05F);
      this.hud();
      Gx.unclip();
   }

   private void drawCart(float cx, float cy) {
      float lean = ((this.down(68, 262) ? 1 : 0) - (this.down(65, 263) ? 1 : 0)) * 4;
      boolean blink = this.crash > 0.0F && (int)(this.time * 14.0F) % 2 == 0;
      this.circle(cx, cy + 18.0F + this.air * 0.05F, 22.0F - Math.min(14.0F, this.air * 0.02F), 1140850688);
      int body = blink ? -1 : -1900533;
      this.box(cx - 24.0F + lean, cy - 6.0F, 48.0F, 20.0F, 4.0F, Gx.mix(body, -16777216, 0.25F));
      this.box(cx - 22.0F + lean, cy - 8.0F, 44.0F, 18.0F, 4.0F, body);

      for (int i = 0; i < 5; i++) {
         this.box(cx - 18.0F + i * 9 + lean, cy - 6.0F, 2.0F, 14.0F, 0.0F, 1728053247);
      }

      this.box(cx - 10.0F + lean * 1.4F, cy - 26.0F, 20.0F, 18.0F, 6.0F, -13053);
      this.circle(cx + lean * 1.6F, cy - 32.0F, 7.0F, -11654);
      this.circle(cx - 2.0F + lean * 1.6F, cy - 33.0F, 1.2F, -15658735);
      this.circle(cx + 2.0F + lean * 1.6F, cy - 33.0F, 1.2F, -15658735);
      this.circle(cx - 18.0F, cy + 14.0F, 4.0F, -14803422);
      this.circle(cx + 18.0F, cy + 14.0F, 4.0F, -14803422);
   }

   private void hud() {
      int kmh = Math.round(this.speed / 70.0F);
      this.box(388.0F, 260.0F, 84.0F, 32.0F, 8.0F, -1072688874);
      this.text(kmh + " km/h", 430.0F, 270.0F, 9.0F, 3, -1);
      this.box(394.0F, 282.0F, 72.0F, 4.0F, 2.0F, 872415231);
      this.box(394.0F, 282.0F, 72.0F * Math.min(1.0F, this.speed / 14000.0F), 4.0F, 2.0F, -14756000);
      this.box(8.0F, 8.0F, 94.0F, 22.0F, 8.0F, -1072688874);
      this.text(Math.round(this.pos / 100.0F) + " m", 55.0F, 19.0F, 8.0F, 3, -1);

      for (int i = 0; i < 3; i++) {
         this.circle(460.0F - i * 14, 18.0F, 5.0F, i < this.lives ? -45730 : 872415231);
      }

      if (this.air > 40.0F) {
         this.text("Sprung! +" + Math.round(this.stunt), 240.0F, 40.0F, 10.0F, 3, -13053);
      } else if (this.lastStunt > 0.0F) {
         this.text("Gelandet!", 240.0F, 40.0F, 9.0F, 3, Gx.withAlpha(-14756000, Math.min(1.0F, this.lastStunt)));
      }

      if (this.crash > 0.0F) {
         this.text("Crash!", 240.0F, 60.0F, 14.0F, 3, -45730);
      }
   }

   private static final class Seg {
      float curve;
      float y;
      int kind = -1;
      float ox;
      boolean taken;
   }
}
