package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CityRush extends GameView {
   private static final float W = 480.0F;
   private static final float H = 300.0F;
   private static final float T = 22.0F;
   private static final int MW = 42;
   private static final int MH = 30;
   private static final int ROAD = 0;
   private static final int BUILD = 1;
   private static final int PARK = 2;
   private static final int WATER = 3;
   private int[][] map;
   private int[][] col;
   private final Set<Integer> keys = new HashSet<>();
   private final List<CityRush.Car> cars = new ArrayList<>();
   private CityRush.Car mine;
   private float fx;
   private float fy;
   private float fang;
   private float heat;
   private float bust;
   private float flash;
   private final List<float[]> coins = new ArrayList<>();
   private float[] pick;
   private float[] drop;
   private boolean carrying;
   private float jobTime;
   private final List<float[]> fx2 = new ArrayList<>();
   private boolean bigMap;

   public CityRush() {
      super("cityrush");
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
      return "Geld";
   }

   @Override
   protected String scoreText() {
      return "$" + this.score;
   }

   @Override
   protected String overTitle() {
      return "Verhaftet!";
   }

   private int tile(float x, float y) {
      int tx = (int)Math.floor(x / 22.0F);
      int ty = (int)Math.floor(y / 22.0F);
      return tx >= 0 && ty >= 0 && tx < 42 && ty < 30 ? this.map[tx][ty] : 1;
   }

   private boolean solid(float x, float y, boolean onFoot) {
      int t = this.tile(x, y);
      return t == 1 || t == 3 || !onFoot && t == 2;
   }

   private boolean road(int tx, int ty) {
      return tx >= 0 && ty >= 0 && tx < 42 && ty < 30 && this.map[tx][ty] == 0;
   }

   private void genMap() {
      this.map = new int[42][30];
      this.col = new int[42][30];
      int[] pal = new int[]{-1900533, -13053, -16749385, -11821238, -723724, -30208, -6599222, -8760784, -12604929};

      for (int x = 0; x < 42; x++) {
         for (int y = 0; y < 30; y++) {
            boolean r = x % 6 == 0 || y % 6 == 0 || x == 41 || y == 29;
            this.map[x][y] = r ? 0 : 1;
         }
      }

      for (int bx = 0; bx < 42; bx += 6) {
         for (int by = 0; by < 30; by += 6) {
            boolean park = this.rnd.nextFloat() < 0.18F;
            int c = pal[this.rnd.nextInt(pal.length)];

            for (int x = bx + 1; x < Math.min(41, bx + 6); x++) {
               for (int y = by + 1; y < Math.min(29, by + 6); y++) {
                  if (park) {
                     this.map[x][y] = 2;
                  }

                  this.col[x][y] = this.rnd.nextFloat() < 0.15F ? pal[this.rnd.nextInt(pal.length)] : c;
               }
            }
         }
      }

      for (int y = 0; y < 30; y++) {
         for (int x = 21; x <= 22; x++) {
            if (this.map[x][y] != 0 || y % 12 != 0) {
               this.map[x][y] = 3;
            }
         }
      }
   }

   private float[] randomRoad() {
      for (int i = 0; i < 2000; i++) {
         int x = this.rnd.nextInt(42);
         int y = this.rnd.nextInt(30);
         if (this.map[x][y] == 0) {
            return new float[]{(x + 0.5F) * 22.0F, (y + 0.5F) * 22.0F};
         }
      }

      return new float[]{11.0F, 11.0F};
   }

   private CityRush.Car spawnCar(boolean police) {
      CityRush.Car c = new CityRush.Car();
      float[] p = this.randomRoad();
      c.x = p[0];
      c.y = p[1];
      c.tx = c.px = (int)(c.x / 22.0F);
      c.ty = c.py = (int)(c.y / 22.0F);
      int[] pal = new int[]{-1900533, -16749385, -13053, -723724, -14803422, -11821238, -36939, -30208};
      c.color = police ? -723724 : pal[this.rnd.nextInt(pal.length)];
      c.police = police;
      this.cars.add(c);
      return c;
   }

   private void newJob() {
      this.pick = this.randomRoad();

      do {
         this.drop = this.randomRoad();
      } while (Math.hypot(this.drop[0] - this.pick[0], this.drop[1] - this.pick[1]) < 300.0);

      this.carrying = false;
      this.jobTime = 0.0F;
   }

   @Override
   protected void reset() {
      this.genMap();
      this.cars.clear();
      this.coins.clear();
      this.fx2.clear();
      this.keys.clear();

      for (int i = 0; i < 22; i++) {
         this.spawnCar(false);
      }

      for (int i = 0; i < 26; i++) {
         float[] p = this.randomRoad();
         this.coins.add(new float[]{p[0] + (this.rnd.nextFloat() - 0.5F) * 8.0F, p[1] + (this.rnd.nextFloat() - 0.5F) * 8.0F});
      }

      int sx = 6 * (1 + this.rnd.nextInt(5));
      int sy = 6 * (1 + this.rnd.nextInt(3)) + 3;
      this.mine = this.spawnCar(false);
      this.mine.x = (sx + 0.5F) * 22.0F;
      this.mine.y = (sy + 0.5F) * 22.0F;
      this.mine.ang = (float) (Math.PI / 2);
      this.mine.player = true;
      this.fx = this.mine.x - 14.0F;
      this.fy = this.mine.y;
      this.heat = 0.0F;
      this.bust = 0.0F;
      this.newJob();
   }

   @Override
   protected void onKey(int k) {
      this.keys.add(k);
      if (k == 70) {
         this.toggleCar();
      }

      if (k == 77) {
         this.bigMap = !this.bigMap;
      }
   }

   @Override
   protected void onKeyUp(int k) {
      this.keys.remove(k);
   }

   private boolean down(int a, int b) {
      return this.keys.contains(a) || this.keys.contains(b);
   }

   private void toggleCar() {
      if (this.mine != null) {
         float sx = (float)(-Math.sin(this.mine.ang)) * 14.0F;
         float sy = (float)Math.cos(this.mine.ang) * 14.0F;
         this.fx = this.mine.x + sx;
         this.fy = this.mine.y + sy;
         if (this.solid(this.fx, this.fy, true)) {
            this.fx = this.mine.x - sx;
            this.fy = this.mine.y - sy;
         }

         this.mine.player = false;
         this.mine.speed = 0.0F;
         this.mine.stun = 9999.0F;
         this.mine = null;
      } else {
         CityRush.Car best = null;
         double bd = 26.0;

         for (CityRush.Car c : this.cars) {
            double d = Math.hypot(c.x - this.fx, c.y - this.fy);
            if (d < bd && !c.police) {
               bd = d;
               best = c;
            }
         }

         if (best != null) {
            if (best.stun < 9000.0F && best.speed > 1.0F) {
               this.heat = Math.min(5.0F, this.heat + 0.6F);
            }

            this.mine = best;
            this.mine.player = true;
            this.mine.stun = 0.0F;
            Sound.play("minecraft:block.iron_door.close", 1.4F, 0.3F);
         }
      }
   }

   @Override
   protected void update(float dt) {
      this.flash += dt;

      for (float[] f : this.fx2) {
         f[2] += dt;
      }

      this.fx2.removeIf(f -> f[2] > 0.8F);
      float px;
      float py;
      if (this.mine != null) {
         this.drivePlayer(this.mine, dt);
         px = this.mine.x;
         py = this.mine.y;
         if (this.mine.hp <= 0.0F) {
            this.fx2.add(new float[]{this.mine.x, this.mine.y, 0.0F, 1.0F});
            this.cars.remove(this.mine);
            this.fx = this.mine.x;
            this.fy = this.mine.y;
            this.mine = null;
            this.heat = Math.min(5.0F, this.heat + 0.5F);
         }
      } else {
         this.walk(dt);
         px = this.fx;
         py = this.fy;
      }

      for (CityRush.Car c : new ArrayList<>(this.cars)) {
         if (!c.player) {
            if (c.police) {
               this.police(c, px, py, dt);
            } else {
               this.traffic(c, dt);
            }
         }
      }

      this.collisions(px, py);

      for (float[] cx : this.coins) {
         if (Math.hypot(cx[0] - px, cx[1] - py) < 12.0) {
            this.score += 10L;
            this.fx2.add(new float[]{cx[0], cx[1], 0.0F, 0.0F});
            float[] p = this.randomRoad();
            cx[0] = p[0];
            cx[1] = p[1];
            Sound.play("minecraft:entity.experience_orb.pickup", 1.4F, 0.25F);
         }
      }

      this.jobTime += dt;
      if (!this.carrying && Math.hypot(this.pick[0] - px, this.pick[1] - py) < 16.0) {
         this.carrying = true;
         this.jobTime = 0.0F;
         Sound.play("minecraft:entity.item.pickup", 1.0F, 0.4F);
      }

      if (this.carrying) {
         float limit = this.limit();
         if (Math.hypot(this.drop[0] - px, this.drop[1] - py) < 16.0) {
            this.score = this.score + (100 + Math.max(0, Math.round((limit - this.jobTime) * 6.0F)));
            Sound.play("minecraft:ui.toast.challenge_complete", 1.3F, 0.3F);
            this.newJob();
         } else if (this.jobTime > limit) {
            this.newJob();
         }
      }

      int stars = (int)Math.ceil(this.heat);
      int cops = 0;
      double nearest = 9999.0;

      for (CityRush.Car cxx : this.cars) {
         if (cxx.police) {
            cops++;
            nearest = Math.min(nearest, Math.hypot(cxx.x - px, cxx.y - py));
         }
      }

      if (cops < Math.min(4, stars) && this.rnd.nextFloat() < dt * 0.6F) {
         CityRush.Car cxxx = this.spawnCar(true);
         if (Math.hypot(cxxx.x - px, cxxx.y - py) < 180.0) {
            this.cars.remove(cxxx);
         }
      }

      if (nearest > 220.0 || stars == 0) {
         this.heat = Math.max(0.0F, this.heat - dt * 0.08F);
      }

      if (stars == 0) {
         this.cars.removeIf(cxxx -> cxxx.police && Math.hypot(cxxx.x - px, cxxx.y - py) > 260.0);
      }

      float mySpeed = this.mine == null ? 0.0F : Math.abs(this.mine.speed);
      if (stars > 0 && nearest < 20.0 && mySpeed < 18.0F) {
         this.bust += dt;
      } else {
         this.bust = Math.max(0.0F, this.bust - dt);
      }

      if (this.bust > 1.6F) {
         this.gameOver();
      }
   }

   private float limit() {
      return 20.0F + (float)Math.hypot(this.drop[0] - this.pick[0], this.drop[1] - this.pick[1]) / 22.0F;
   }

   private void drivePlayer(CityRush.Car c, float dt) {
      boolean gas = this.down(87, 265);
      boolean brake = this.down(83, 264);
      boolean left = this.down(65, 263);
      boolean right = this.down(68, 262);
      boolean hand = this.keys.contains(32);
      if (gas) {
         c.speed += 150.0F * dt;
      }

      if (brake) {
         c.speed = c.speed - (c.speed > 0.0F ? 260 : 90) * dt;
      }

      c.speed = c.speed * (float)Math.pow(hand ? 0.25 : 0.55, dt);
      c.speed = Math.max(-50.0F, Math.min(190.0F, c.speed));
      float steer = (left ? -1 : 0) + (right ? 1 : 0);
      c.ang = c.ang + steer * dt * 2.8F * Math.min(1.0F, Math.abs(c.speed) / 60.0F) * Math.signum(c.speed == 0.0F ? 1.0F : c.speed) * (hand ? 1.6F : 1.0F);
      this.move(c, dt, false);
   }

   private void move(CityRush.Car c, float dt, boolean ai) {
      float nx = c.x + (float)Math.cos(c.ang) * c.speed * dt;
      float ny = c.y + (float)Math.sin(c.ang) * c.speed * dt;
      float fr = 9.0F * Math.signum(c.speed == 0.0F ? 1.0F : c.speed);
      if (!this.solid(nx + (float)Math.cos(c.ang) * fr, ny + (float)Math.sin(c.ang) * fr, false)) {
         c.x = nx;
         c.y = ny;
      } else {
         if (!ai && Math.abs(c.speed) > 40.0F) {
            c.hp = c.hp - Math.abs(c.speed) * 0.12F;
            this.fx2.add(new float[]{c.x + (float)Math.cos(c.ang) * 10.0F, c.y + (float)Math.sin(c.ang) * 10.0F, 0.0F, 2.0F});
            Sound.play("minecraft:entity.generic.explode", 1.8F, 0.15F);
         }

         if (!this.solid(nx, c.y, false)) {
            c.x = nx;
         } else if (!this.solid(c.x, ny, false)) {
            c.y = ny;
         }

         c.speed *= -0.25F;
      }
   }

   private void walk(float dt) {
      float vx = (this.down(68, 262) ? 1 : 0) - (this.down(65, 263) ? 1 : 0);
      float vy = (this.down(83, 264) ? 1 : 0) - (this.down(87, 265) ? 1 : 0);
      if (vx != 0.0F || vy != 0.0F) {
         float l = (float)Math.hypot(vx, vy);
         float sp = this.keys.contains(32) ? 95.0F : 60.0F;
         float nx = this.fx + vx / l * sp * dt;
         float ny = this.fy + vy / l * sp * dt;
         this.fang = (float)Math.atan2(vy, vx);
         if (!this.solid(nx, this.fy, true)) {
            this.fx = nx;
         }

         if (!this.solid(this.fx, ny, true)) {
            this.fy = ny;
         }
      }
   }

   private void traffic(CityRush.Car c, float dt) {
      if (c.stun > 0.0F) {
         c.stun -= dt;
         c.speed *= 0.9F;
      } else {
         float tx = (c.tx + 0.5F) * 22.0F;
         float ty = (c.ty + 0.5F) * 22.0F;
         if (Math.hypot(tx - c.x, ty - c.y) < 2.5) {
            List<int[]> next = new ArrayList<>();
            int cx = c.tx;
            int cy = c.ty;
            int[][] d = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            int sx = cx - c.px;
            int sy = cy - c.py;

            for (int[] o : d) {
               if (this.road(cx + o[0], cy + o[1]) && (cx + o[0] != c.px || cy + o[1] != c.py)) {
                  next.add(o);
                  if (o[0] == sx && o[1] == sy && this.rnd.nextFloat() < 0.6F) {
                     next.clear();
                     next.add(o);
                     break;
                  }
               }
            }

            int[] ox = next.isEmpty() ? new int[]{-sx, -sy} : next.get(this.rnd.nextInt(next.size()));
            c.px = cx;
            c.py = cy;
            c.tx = cx + ox[0];
            c.ty = cy + ox[1];
            if (!this.road(c.tx, c.ty)) {
               c.tx = cx;
               c.ty = cy;
            }

            tx = (c.tx + 0.5F) * 22.0F;
            ty = (c.ty + 0.5F) * 22.0F;
         }

         float want = (float)Math.atan2(ty - c.y, tx - c.x);
         c.ang = turn(c.ang, want, dt * 6.0F);
         float target = 55.0F;

         for (CityRush.Car ox : this.cars) {
            if (ox != c) {
               float dx = ox.x - c.x;
               float dy = ox.y - c.y;
               float ahead = dx * (float)Math.cos(c.ang) + dy * (float)Math.sin(c.ang);
               float side = Math.abs(-dx * (float)Math.sin(c.ang) + dy * (float)Math.cos(c.ang));
               if (ahead > 0.0F && ahead < 26.0F && side < 9.0F) {
                  target = 0.0F;
               }
            }
         }

         c.speed = c.speed + (target - c.speed) * Math.min(1.0F, dt * 3.0F);
         c.x = c.x + (float)Math.cos(c.ang) * c.speed * dt;
         c.y = c.y + (float)Math.sin(c.ang) * c.speed * dt;
      }
   }

   private void police(CityRush.Car c, float px, float py, float dt) {
      if (c.stun > 0.0F) {
         c.stun -= dt;
         c.speed *= 0.9F;
         this.move(c, dt, true);
      } else {
         float want = (float)Math.atan2(py - c.y, px - c.x);
         float[] tries = new float[]{0.0F, 0.8F, -0.8F, 1.6F, -1.6F};
         float best = want;

         for (float t : tries) {
            float a = want + t;
            if (!this.solid(c.x + (float)Math.cos(a) * 22.0F, c.y + (float)Math.sin(a) * 22.0F, false)) {
               best = a;
               break;
            }
         }

         c.ang = turn(c.ang, best, dt * 4.0F);
         float dist = (float)Math.hypot(px - c.x, py - c.y);
         c.speed = c.speed + ((dist < 30.0F ? 40 : 135) - c.speed) * Math.min(1.0F, dt * 2.0F);
         this.move(c, dt, true);
      }
   }

   private static float turn(float a, float b, float max) {
      float d = (float)Math.atan2(Math.sin(b - a), Math.cos(b - a));
      return a + Math.max(-max, Math.min(max, d));
   }

   private void collisions(float px, float py) {
      for (CityRush.Car c : this.cars) {
         if (c != this.mine) {
            if (this.mine != null) {
               double d = Math.hypot(c.x - this.mine.x, c.y - this.mine.y);
               if (d < 15.0) {
                  float imp = Math.abs(this.mine.speed);
                  if (imp > 25.0F) {
                     this.mine.hp -= imp * 0.08F;
                     c.stun = 2.5F;
                     c.hp -= imp * 0.2F;
                     if (!c.police) {
                        this.heat = Math.min(5.0F, this.heat + imp / 260.0F);
                     } else {
                        this.heat = Math.min(5.0F, this.heat + 0.4F);
                     }

                     this.fx2.add(new float[]{(c.x + this.mine.x) / 2.0F, (c.y + this.mine.y) / 2.0F, 0.0F, 2.0F});
                     Sound.play("minecraft:entity.generic.explode", 1.6F, 0.18F);
                  }

                  float nx = (float)((this.mine.x - c.x) / Math.max(0.01, d));
                  float ny = (float)((this.mine.y - c.y) / Math.max(0.01, d));
                  this.mine.x += nx * 3.0F;
                  this.mine.y += ny * 3.0F;
                  this.mine.speed *= 0.4F;
               }
            } else if (Math.hypot(c.x - px, c.y - py) < 9.0 && Math.abs(c.speed) > 30.0F && !c.police) {
               this.fx = this.fx + (float)(-Math.sin(c.ang)) * 10.0F;
               this.fy = this.fy + (float)Math.cos(c.ang) * 10.0F;
            }
         }
      }

      this.cars.removeIf(cx -> cx.hp <= 0.0F && cx != this.mine && !cx.player);
   }

   @Override
   protected void render() {
      float camX = this.mine != null ? this.mine.x : this.fx;
      float camY = this.mine != null ? this.mine.y : this.fy;
      if (this.mine != null) {
         camX += (float)Math.cos(this.mine.ang) * this.mine.speed * 0.25F;
         camY += (float)Math.sin(this.mine.ang) * this.mine.speed * 0.25F;
      }

      camX = Math.max(240.0F, Math.min(684.0F, camX));
      camY = Math.max(150.0F, Math.min(510.0F, camY));
      float ox = 240.0F - camX;
      float oy = 150.0F - camY;
      Gx.clip(this.X(0.0F), this.Y(0.0F), this.S(480.0F), this.S(300.0F));
      int x0 = Math.max(0, (int)((camX - 240.0F) / 22.0F) - 1);
      int x1 = Math.min(41, (int)((camX + 240.0F) / 22.0F) + 1);
      int y0 = Math.max(0, (int)((camY - 150.0F) / 22.0F) - 1);
      int y1 = Math.min(29, (int)((camY + 150.0F) / 22.0F) + 1);
      this.box(0.0F, 0.0F, 480.0F, 300.0F, 0.0F, -12960443);

      for (int bx6 = x0 / 6 * 6; bx6 <= x1; bx6 += 6) {
         for (int by6 = y0 / 6 * 6; by6 <= y1; by6 += 6) {
            if (bx6 + 1 < 42 && by6 + 1 < 30) {
               int ex = Math.min(41, bx6 + 6);
               int ey = Math.min(29, by6 + 6);
               this.box(
                  (bx6 + 1) * 22.0F + ox - 2.0F, (by6 + 1) * 22.0F + oy - 2.0F, (ex - bx6 - 1) * 22.0F + 4.0F, (ey - by6 - 1) * 22.0F + 4.0F, 0.0F, -9670792
               );
            }
         }
      }

      for (int tx = x0; tx <= x1; tx++) {
         for (int ty = y0; ty <= y1; ty++) {
            float sx = tx * 22.0F + ox;
            float sy = ty * 22.0F + oy;
            switch (this.map[tx][ty]) {
               case 0:
                  if (tx % 6 != 0 && ty % 6 == 0 && tx % 2 == 0) {
                     this.box(sx + 2.0F, sy + 11.0F - 0.6F, 18.0F, 1.2F, 0.0F, -855645875);
                  }

                  if (ty % 6 != 0 && tx % 6 == 0 && ty % 2 == 0) {
                     this.box(sx + 11.0F - 0.6F, sy + 2.0F, 1.2F, 18.0F, 0.0F, -855645875);
                  }
                  break;
               case 1:
               default:
                  int c = this.col[tx][ty];
                  this.box(sx + 1.0F, sy + 1.0F, 20.0F, 20.0F, 0.0F, Gx.mix(c, -16777216, 0.18F));
                  this.box(sx + 1.0F, sy + 1.0F, 20.0F, 18.0F, 0.0F, c);
                  int st = Gx.mix(c, -1, 0.25F);
                  this.box(sx + 4.84F, sy + 6.6000004F, 4.0F, 4.0F, 0.0F, st);
                  this.box(sx + 13.200001F, sy + 6.6000004F, 4.0F, 4.0F, 0.0F, st);
                  break;
               case 2:
                  this.box(sx, sy, 22.0F, 22.0F, 0.0F, -12677567);
                  if ((tx * 7 + ty * 13) % 5 == 0) {
                     this.circle(sx + 11.0F, sy + 11.0F, 6.0F, -13866193);
                  }
                  break;
               case 3:
                  this.box(sx, sy, 22.0F, 22.0F, 0.0F, (tx + ty + (int)(this.time * 2.0F) & 1) == 0 ? -14717000 : -13992764);
            }
         }
      }

      for (float[] c : this.coins) {
         float b = (float)Math.sin(this.time * 5.0F + c[0]) * 1.2F;
         this.circle(c[0] + ox, c[1] + oy + b, 4.0F, -13053);
         this.circle(c[0] + ox - 1.0F, c[1] + oy + b - 1.0F, 2.0F, -3424);
      }

      float[] goal = this.carrying ? this.drop : this.pick;
      float pulse = 10.0F + 3.0F * (float)Math.sin(this.time * 6.0F);
      this.circle(goal[0] + ox, goal[1] + oy, pulse, this.carrying ? 1714671994 : 1728032814);
      this.circle(goal[0] + ox, goal[1] + oy, 5.0F, this.carrying ? -13381254 : -20434);

      for (CityRush.Car c : this.cars) {
         this.drawCar(c, c.x + ox, c.y + oy);
      }

      if (this.mine == null) {
         this.circle(this.fx + ox + 1.0F, this.fy + oy + 1.5F, 5.0F, 1426063360);
         this.circle(this.fx + ox, this.fy + oy, 5.0F, -16749385);
         this.circle(this.fx + ox + (float)Math.cos(this.fang) * 1.5F, this.fy + oy + (float)Math.sin(this.fang) * 1.5F, 3.2F, -12898);

         for (CityRush.Car c : this.cars) {
            if (!c.police && Math.hypot(c.x - this.fx, c.y - this.fy) < 26.0) {
               this.text("F", c.x + ox, c.y + oy - 16.0F, 7.0F, 3, -1);
               break;
            }
         }
      }

      for (float[] f : this.fx2) {
         float a = 1.0F - f[2] / 0.8F;
         if (f[3] == 0.0F) {
            this.text("+$10", f[0] + ox, f[1] + oy - f[2] * 30.0F, 7.0F, 3, Gx.withAlpha(-13053, a));
         } else {
            this.circle(f[0] + ox, f[1] + oy, (f[3] == 1.0F ? 22 : 9) * (0.4F + f[2]), Gx.withAlpha(f[3] == 1.0F ? -38358 : -11654, a * 0.8F));
         }
      }

      float gx = goal[0] + ox;
      float gy = goal[1] + oy;
      if (gx < 10.0F || gy < 10.0F || gx > 470.0F || gy > 290.0F) {
         float ang = (float)Math.atan2(gy - 150.0F, gx - 240.0F);
         float ax = 240.0F + (float)Math.cos(ang) * 128.0F;
         float ay = 150.0F + (float)Math.sin(ang) * 128.0F;
         this.circle(ax, ay, 6.0F, this.carrying ? -13381254 : -20434);
         this.circle(ax + (float)Math.cos(ang) * 5.0F, ay + (float)Math.sin(ang) * 5.0F, 3.0F, -1);
      }

      this.hud();
      Gx.unclip();
   }

   private void drawCar(CityRush.Car c, float x, float y) {
      float cs = (float)Math.cos(c.ang);
      float sn = (float)Math.sin(c.ang);

      for (int k = -1; k <= 1; k++) {
         this.circle(x + cs * k * 6.0F + 1.2F, y + sn * k * 6.0F + 1.8F, 6.2F, 1426063360);
      }

      int body = c.hp < 30.0F ? Gx.mix(c.color, -14540254, 0.5F) : c.color;

      for (int k = -1; k <= 1; k++) {
         this.circle(x + cs * k * 6.0F, y + sn * k * 6.0F, 6.2F, body);
      }

      this.circle(x - cs * 1.5F, y - sn * 1.5F, 4.4F, Gx.mix(body, -16777216, 0.35F));
      this.circle(x + cs * 3.5F, y + sn * 3.5F, 3.0F, -6301441);
      float lx = -sn * 3.6F;
      float ly = cs * 3.6F;
      this.circle(x + cs * 11.0F + lx, y + sn * 11.0F + ly, 1.7F, -2360);
      this.circle(x + cs * 11.0F - lx, y + sn * 11.0F - ly, 1.7F, -2360);
      if (c.police) {
         boolean a = (int)(this.flash * 8.0F) % 2 == 0;
         this.circle(x - cs * 1.5F + lx * 0.6F, y - sn * 1.5F + ly * 0.6F, 2.0F, a ? -50373 : -12882945);
         this.circle(x - cs * 1.5F - lx * 0.6F, y - sn * 1.5F - ly * 0.6F, 2.0F, a ? -12882945 : -50373);
      }

      if (c.player) {
         this.circle(x, y - 13.0F, 2.0F, -1);
      }
   }

   private void hud() {
      if (this.mine != null) {
         int kmh = Math.round(Math.abs(this.mine.speed) * 1.2F);
         this.box(394.0F, 260.0F, 78.0F, 32.0F, 8.0F, -1072688874);
         this.text(kmh + " km/h", 433.0F, 270.0F, 9.0F, 3, -1);
         this.box(400.0F, 282.0F, 66.0F, 4.0F, 2.0F, 872415231);
         this.box(400.0F, 282.0F, 66.0F * Math.min(1.0F, kmh / 228.0F), 4.0F, 2.0F, kmh > 160 ? -45730 : -14756000);
         this.box(8.0F, 282.0F, 70.0F, 6.0F, 3.0F, 872415231);
         this.box(8.0F, 282.0F, 70.0F * Math.max(0.0F, this.mine.hp) / 100.0F, 6.0F, 3.0F, this.mine.hp > 40.0F ? -14756000 : -45730);
         this.text("Auto", 43.0F, 274.0F, 6.0F, 2, -1426063361);
      } else {
         this.text("Zu Fuß · F = einsteigen", 70.0F, 284.0F, 6.5F, 2, -855638017);
      }

      int stars = (int)Math.ceil(this.heat);

      for (int i = 0; i < 5; i++) {
         float sx = 406.0F + i * 14;
         this.circle(sx, 14.0F, 5.0F, i < stars ? -13053 : 872415231);
         if (i < stars) {
            this.circle(sx - 1.0F, 13.0F, 2.0F, -3424);
         }
      }

      if (this.bust > 0.2F) {
         this.text("Polizei hält dich fest …", 240.0F, 40.0F, 9.0F, 3, -45730);
      }

      String job = this.carrying ? String.format(Locale.ROOT, "Liefern! %.0f s", Math.max(0.0F, this.limit() - this.jobTime)) : "Paket abholen";
      this.box(180.0F, 6.0F, 120.0F, 18.0F, 9.0F, -1072688874);
      this.text(job, 240.0F, 15.0F, 7.5F, 3, this.carrying ? -13381254 : -20434);
      this.minimap();
   }

   private void minimap() {
      float s = this.bigMap ? 5.0F : 1.6F;
      float mw = 42.0F * s;
      float mh = 30.0F * s;
      float mx = this.bigMap ? (480.0F - mw) / 2.0F : 8.0F;
      float my = this.bigMap ? (300.0F - mh) / 2.0F : 8.0F;
      this.box(mx - 2.0F, my - 2.0F, mw + 4.0F, mh + 4.0F, 4.0F, -804253418);

      for (int x = 0; x < 42; x += 6) {
         this.box(mx + x * s + s * 0.2F, my, s * 0.6F, mh, 0.0F, 1442840575);
      }

      for (int y = 0; y < 30; y += 6) {
         this.box(mx, my + y * s + s * 0.2F, mw, s * 0.6F, 0.0F, 1442840575);
      }

      this.box(mx + 21.0F * s, my, 2.0F * s, mh, 0.0F, -2009093633);
      float[] goal = this.carrying ? this.drop : this.pick;
      this.circle(mx + goal[0] / 22.0F * s, my + goal[1] / 22.0F * s, this.bigMap ? 4.0F : 2.2F, this.carrying ? -13381254 : -20434);

      for (CityRush.Car c : this.cars) {
         if (c.police) {
            this.circle(mx + c.x / 22.0F * s, my + c.y / 22.0F * s, this.bigMap ? 3.0F : 1.6F, -50373);
         }
      }

      float px = this.mine != null ? this.mine.x : this.fx;
      float py = this.mine != null ? this.mine.y : this.fy;
      this.circle(mx + px / 22.0F * s, my + py / 22.0F * s, this.bigMap ? 4.0F : 2.2F, -1);
      if (this.bigMap) {
         this.text("M = Karte schließen", 240.0F, my + mh + 10.0F, 7.0F, 2, -855638017);
      }
   }

   private static final class Car {
      float x;
      float y;
      float ang;
      float speed;
      float hp = 100.0F;
      float stun;
      int color;
      boolean police;
      boolean player;
      int tx;
      int ty;
      int px;
      int py;
   }
}
