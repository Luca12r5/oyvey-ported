package dev.lego.games;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Ellipse2D.Float;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Asteroids extends GameView {
   private static final float W = 400.0F;
   private static final float H = 290.0F;
   private static final float[] RAD = new float[]{0.0F, 7.5F, 14.0F, 25.0F};
   private static final int[] PTS = new int[]{0, 100, 50, 20};
   private static final int ANGLES = 48;
   private static final int ROCK_ANGLES = 24;
   private static final int VARIANTS = 6;
   private static final int NEON = -10685697;
   private static final int ROCK = -3561729;
   private static final int UFO_C = -41816;
   private static final int SHOT = -3462;
   private final List<Asteroids.Rock> rocks = new ArrayList<>();
   private final List<Asteroids.Shot> shots = new ArrayList<>();
   private final GameFx fx = new GameFx();
   private Asteroids.Ufo ufo;
   private float x;
   private float y;
   private float vx;
   private float vy;
   private float ang;
   private float fireT;
   private float invul;
   private float deadT;
   private float waveT;
   private float ufoT;
   private float anim;
   private float thrustT;
   private float bannerT = -1.0F;
   private boolean left;
   private boolean right;
   private boolean thrust;
   private boolean firing;
   private boolean dead;
   private int lives;
   private int wave;
   private long nextLife;

   public Asteroids() {
      super("asteroids");
   }

   @Override
   protected float boardW() {
      return 400.0F;
   }

   @Override
   protected float boardH() {
      return 290.0F;
   }

   @Override
   protected void reset() {
      this.rocks.clear();
      this.shots.clear();
      this.fx.clear();
      this.ufo = null;
      this.lives = 3;
      this.wave = 0;
      this.nextLife = 10000L;
      this.left = this.right = this.thrust = this.firing = false;
      this.spawnShip();
      this.nextWave();
   }

   private void spawnShip() {
      this.x = 200.0F;
      this.y = 145.0F;
      this.vx = this.vy = 0.0F;
      this.ang = (float) (-Math.PI / 2);
      this.invul = 2.2F;
      this.dead = false;
      this.deadT = 0.0F;
   }

   private void nextWave() {
      this.wave++;
      int n = Math.min(10, 3 + this.wave);

      for (int i = 0; i < n; i++) {
         float rx;
         float ry;
         do {
            rx = this.rnd.nextFloat() * 400.0F;
            ry = this.rnd.nextFloat() * 290.0F;
         } while (dist(rx, ry, this.x, this.y) < 90.0F);

         this.addRock(rx, ry, 3, null);
      }

      this.ufoT = Math.max(8, 22 - this.wave * 2) + this.rnd.nextFloat() * 6.0F;
      this.bannerT = 0.0F;
   }

   private void addRock(float rx, float ry, int size, Asteroids.Rock parent) {
      Asteroids.Rock r = new Asteroids.Rock();
      r.x = rx;
      r.y = ry;
      r.size = size;
      r.variant = this.rnd.nextInt(6);
      double a = this.rnd.nextDouble() * Math.PI * 2.0;
      float sp = (size == 3 ? 22 : (size == 2 ? 42 : 66)) * (1.0F + this.wave * 0.06F) * (0.7F + this.rnd.nextFloat() * 0.6F);
      r.vx = (float)Math.cos(a) * sp + (parent != null ? parent.vx * 0.4F : 0.0F);
      r.vy = (float)Math.sin(a) * sp + (parent != null ? parent.vy * 0.4F : 0.0F);
      r.ang = this.rnd.nextFloat() * 6.28F;
      r.spin = (this.rnd.nextFloat() - 0.5F) * 2.4F;
      this.rocks.add(r);
   }

   private static float dist(float ax, float ay, float bx, float by) {
      float dx = Math.abs(ax - bx);
      float dy = Math.abs(ay - by);
      dx = Math.min(dx, 400.0F - dx);
      dy = Math.min(dy, 290.0F - dy);
      return (float)Math.sqrt(dx * dx + dy * dy);
   }

   @Override
   protected void onKey(int k) {
      if (k == 263 || k == 65) {
         this.left = true;
      }

      if (k == 262 || k == 68) {
         this.right = true;
      }

      if (k == 265 || k == 87) {
         this.thrust = true;
      }

      if (k == 32) {
         if (!this.firing) {
            this.fireT = 0.0F;
         }

         this.firing = true;
      }

      if ((k == 264 || k == 83 || k == 340) && !this.dead) {
         this.hyperspace();
      }
   }

   @Override
   protected void onKeyUp(int k) {
      if (k == 263 || k == 65) {
         this.left = false;
      }

      if (k == 262 || k == 68) {
         this.right = false;
      }

      if (k == 265 || k == 87) {
         this.thrust = false;
      }

      if (k == 32) {
         this.firing = false;
      }
   }

   private void hyperspace() {
      this.fx.burst(this.x, this.y, 20, -10685697, 120.0F, 2.0F, 0.5F, 0.0F, 2);
      this.x = 20.0F + this.rnd.nextFloat() * 360.0F;
      this.y = 20.0F + this.rnd.nextFloat() * 250.0F;
      this.vx = this.vy = 0.0F;
      this.invul = Math.max(this.invul, 0.4F);
      this.fx.ring(this.x, this.y, 30.0F, -10685697, 0.4F);
      Sound.play("minecraft:entity.enderman.teleport", 1.4F, 0.35F);
   }

   @Override
   protected void update(float dt) {
      this.fx.update(dt);
      if (this.bannerT >= 0.0F) {
         this.bannerT += dt;
         if (this.bannerT > 1.8F) {
            this.bannerT = -1.0F;
         }
      }

      this.invul = Math.max(0.0F, this.invul - dt);
      this.fireT = Math.max(0.0F, this.fireT - dt);
      if (this.dead) {
         this.deadT += dt;
         if (this.deadT > 1.6F) {
            if (this.lives <= 0) {
               this.gameOver();
               return;
            }

            boolean clear = true;

            for (Asteroids.Rock r : this.rocks) {
               if (dist(r.x, r.y, 200.0F, 145.0F) < RAD[r.size] + 50.0F) {
                  clear = false;
               }
            }

            if (clear || this.deadT > 4.0F) {
               this.spawnShip();
            }
         }
      } else {
         if (this.left) {
            this.ang -= 4.4F * dt;
         }

         if (this.right) {
            this.ang += 4.4F * dt;
         }

         float cs = (float)Math.cos(this.ang);
         float sn = (float)Math.sin(this.ang);
         if (this.thrust) {
            this.vx += cs * 250.0F * dt;
            this.vy += sn * 250.0F * dt;
            this.thrustT += dt;
            if (this.thrustT > 0.02F) {
               this.thrustT = 0.0F;
               this.fx
                  .add(
                     this.x - cs * 9.0F,
                     this.y - sn * 9.0F,
                     -cs * 90.0F + this.vx * 0.5F + (this.rnd.nextFloat() - 0.5F) * 40.0F,
                     -sn * 90.0F + this.vy * 0.5F + (this.rnd.nextFloat() - 0.5F) * 40.0F,
                     0.35F,
                     1.8F + this.rnd.nextFloat(),
                     this.rnd.nextBoolean() ? -19641 : -38339,
                     2,
                     0.0F,
                     1.5F
                  );
            }
         }

         float sp = (float)Math.sqrt(this.vx * this.vx + this.vy * this.vy);
         if (sp > 240.0F) {
            this.vx *= 240.0F / sp;
            this.vy *= 240.0F / sp;
         }

         float d = (float)Math.exp(-0.45F * dt);
         this.vx *= d;
         this.vy *= d;
         this.x = wrapX(this.x + this.vx * dt);
         this.y = wrapY(this.y + this.vy * dt);
         if (this.firing && this.fireT <= 0.0F) {
            this.fire();
         }
      }

      for (Asteroids.Rock rx : this.rocks) {
         rx.x = wrapX(rx.x + rx.vx * dt);
         rx.y = wrapY(rx.y + rx.vy * dt);
         rx.ang = rx.ang + rx.spin * dt;
         rx.flash = Math.max(0.0F, rx.flash - dt * 6.0F);
      }

      for (int i = this.shots.size() - 1; i >= 0; i--) {
         Asteroids.Shot s = this.shots.get(i);
         s.x = wrapX(s.x + s.vx * dt);
         s.y = wrapY(s.y + s.vy * dt);
         s.life -= dt;
         if (s.life <= 0.0F) {
            this.shots.remove(i);
         } else {
            boolean used = false;
            if (!s.enemy) {
               for (int k = this.rocks.size() - 1; k >= 0; k--) {
                  Asteroids.Rock rx = this.rocks.get(k);
                  if (dist(s.x, s.y, rx.x, rx.y) < RAD[rx.size] * 0.92F) {
                     this.breakRock(k, s.vx, s.vy);
                     used = true;
                     break;
                  }
               }

               if (!used && this.ufo != null && dist(s.x, s.y, this.ufo.x, this.ufo.y) < (this.ufo.small ? 8 : 13)) {
                  this.killUfo();
                  used = true;
               }
            } else if (!this.dead && this.invul <= 0.0F && dist(s.x, s.y, this.x, this.y) < 7.0F) {
               this.crash();
               used = true;
            }

            if (used) {
               this.shots.remove(i);
            }
         }
      }

      if (!this.dead && this.invul <= 0.0F) {
         for (int kx = this.rocks.size() - 1; kx >= 0; kx--) {
            Asteroids.Rock rx = this.rocks.get(kx);
            if (dist(this.x, this.y, rx.x, rx.y) < RAD[rx.size] * 0.85F + 6.0F) {
               this.breakRock(kx, this.vx, this.vy);
               this.crash();
               break;
            }
         }

         if (!this.dead && this.ufo != null && dist(this.x, this.y, this.ufo.x, this.ufo.y) < 16.0F) {
            this.killUfo();
            this.crash();
         }
      }

      this.ufoT -= dt;
      if (this.ufo == null && this.ufoT <= 0.0F && this.wave >= 1) {
         this.ufo = new Asteroids.Ufo();
         this.ufo.small = this.wave >= 3 && this.rnd.nextFloat() < 0.45F;
         boolean fromLeft = this.rnd.nextBoolean();
         this.ufo.x = fromLeft ? -20.0F : 420.0F;
         this.ufo.vx = (fromLeft ? 1 : -1) * (this.ufo.small ? 75 : 55);
         this.ufo.y = 30.0F + this.rnd.nextFloat() * 230.0F;
         this.ufo.fireT = 1.2F;
         this.ufoT = Math.max(9, 24 - this.wave * 2) + this.rnd.nextFloat() * 8.0F;
         Sound.play("minecraft:block.beacon.activate", 1.8F, 0.3F);
      }

      if (this.ufo != null) {
         this.ufo.t += dt;
         this.ufo.x = this.ufo.x + this.ufo.vx * dt;
         this.ufo.y = wrapY(this.ufo.y + (float)Math.sin(this.ufo.t * 2.2F) * 40.0F * dt);
         this.ufo.fireT -= dt;
         if (this.ufo.fireT <= 0.0F && !this.dead) {
            this.ufo.fireT = this.ufo.small ? 0.9F : 1.3F;
            double a = this.ufo.small
               ? Math.atan2(this.y - this.ufo.y, this.x - this.ufo.x) + (this.rnd.nextFloat() - 0.5F) * 0.35F
               : this.rnd.nextDouble() * Math.PI * 2.0;
            Asteroids.Shot s = new Asteroids.Shot();
            s.enemy = true;
            s.x = this.ufo.x;
            s.y = this.ufo.y;
            s.vx = (float)Math.cos(a) * 170.0F;
            s.vy = (float)Math.sin(a) * 170.0F;
            s.life = 1.6F;
            this.shots.add(s);
            Sound.play("minecraft:block.note_block.bit", 0.7F, 0.25F);
         }

         if (this.ufo.x < -40.0F || this.ufo.x > 440.0F) {
            this.ufo = null;
         }
      }

      if (this.rocks.isEmpty() && !this.dead) {
         this.waveT += dt;
         if (this.waveT > 1.2F) {
            this.waveT = 0.0F;
            this.nextWave();
         }
      }

      if (this.score >= this.nextLife) {
         this.nextLife += 10000L;
         this.lives++;
         this.fx.popup("+1 Leben!", this.x, this.y - 16.0F, -6291531, 12.0F);
         Sound.play("minecraft:entity.player.levelup", 1.5F, 0.4F);
      }
   }

   private void fire() {
      int mine = 0;

      for (Asteroids.Shot s : this.shots) {
         if (!s.enemy) {
            mine++;
         }
      }

      if (mine < 6) {
         this.fireT = 0.17F;
         float cs = (float)Math.cos(this.ang);
         float sn = (float)Math.sin(this.ang);
         Asteroids.Shot sx = new Asteroids.Shot();
         sx.x = this.x + cs * 10.0F;
         sx.y = this.y + sn * 10.0F;
         sx.vx = cs * 380.0F + this.vx;
         sx.vy = sn * 380.0F + this.vy;
         sx.life = 0.85F;
         this.shots.add(sx);
         this.fx.add(sx.x, sx.y, 0.0F, 0.0F, 0.12F, 4.0F, -3462, 2, 0.0F, 0.0F);
         this.vx -= cs * 6.0F;
         this.vy -= sn * 6.0F;
         Sound.play("minecraft:block.note_block.hat", 2.0F, 0.25F);
      }
   }

   private void breakRock(int k, float hvx, float hvy) {
      Asteroids.Rock r = this.rocks.remove(k);
      int pts = PTS[r.size];
      this.score += pts;
      this.fx.popup("+" + pts, r.x, r.y - RAD[r.size] * 0.5F, -1, 8 + r.size);
      int n = 6 + r.size * 6;

      for (int i = 0; i < n; i++) {
         double a = this.rnd.nextDouble() * Math.PI * 2.0;
         float sp = 40.0F + this.rnd.nextFloat() * (60 + r.size * 30);
         this.fx
            .add(
               r.x,
               r.y,
               (float)Math.cos(a) * sp + hvx * 0.1F,
               (float)Math.sin(a) * sp + hvy * 0.1F,
               0.5F + this.rnd.nextFloat() * 0.5F,
               1.2F + this.rnd.nextFloat(),
               this.rnd.nextBoolean() ? -3561729 : -1,
               5,
               0.0F,
               1.8F
            );
      }

      this.fx.ring(r.x, r.y, RAD[r.size] * 1.8F, -3561729, 0.4F);
      this.fx.shake(0.08F * r.size);
      Sound.play("minecraft:entity.generic.explode", 1.9F - r.size * 0.3F, 0.12F + r.size * 0.06F);
      if (r.size > 1) {
         this.addRock(r.x, r.y, r.size - 1, r);
         this.addRock(r.x, r.y, r.size - 1, r);
      }
   }

   private void killUfo() {
      int pts = this.ufo.small ? 1000 : 200;
      this.score += pts;
      this.fx.burst(this.ufo.x, this.ufo.y, 40, -41816, 190.0F, 2.4F, 0.8F, 0.0F, 5);
      this.fx.ring(this.ufo.x, this.ufo.y, 40.0F, -41816, 0.5F);
      this.fx.popup("UFO! +" + pts, this.ufo.x, this.ufo.y - 12.0F, -41816, 12.0F);
      this.fx.shake(0.5F);
      Sound.play("minecraft:entity.generic.explode", 1.2F, 0.4F);
      this.ufo = null;
   }

   private void crash() {
      this.dead = true;
      this.deadT = 0.0F;
      this.lives--;
      this.fx.burst(this.x, this.y, 50, -10685697, 200.0F, 2.2F, 1.1F, 0.0F, 5);
      this.fx.burst(this.x, this.y, 24, -19641, 120.0F, 2.8F, 0.8F, 0.0F, 2);
      this.fx.ring(this.x, this.y, 50.0F, -10685697, 0.6F);
      this.fx.shake(0.9F);
      this.fx.flash(-1, 0.5F);
      Sound.play("minecraft:entity.generic.explode", 0.8F, 0.5F);
   }

   private static float wrapX(float v) {
      return v < 0.0F ? v + 400.0F : (v >= 400.0F ? v - 400.0F : v);
   }

   private static float wrapY(float v) {
      return v < 0.0F ? v + 290.0F : (v >= 290.0F ? v - 290.0F : v);
   }

   @Override
   protected void render() {
      float dt = Math.min(0.05F, Gx.dt);
      this.anim += dt;
      GameFx.sprite(this, "as_bg", 0.0F, 0.0F, 400.0F, 290.0F, 400.0F, 290.0F, Asteroids::paintSpace, -1);

      for (int i = 0; i < 24; i++) {
         float sx = i * 173.3F % 400.0F;
         float sy = (i * 91.7F + 13.0F) % 290.0F;
         float tw = 0.5F + 0.5F * (float)Math.sin(this.anim * (1.5F + i % 4) + i);
         this.circle(sx, sy, 0.7F + i % 3 * 0.3F, Gx.withAlpha(-1, 0.3F + 0.6F * tw));
      }

      Gx.push();
      Gx.translate(this.S(this.fx.sx), this.S(this.fx.sy));

      for (Asteroids.Rock r : this.rocks) {
         this.rock(r);
      }

      if (this.ufo != null) {
         boolean sm = this.ufo.small;
         float s = sm ? 20.0F : 30.0F;
         Gx.glow(this.X(this.ufo.x), this.Y(this.ufo.y), this.S(s), 872373416);
         GameFx.sprite(
            this,
            "as_ufo" + (int)(this.ufo.t * 8.0F) % 3,
            this.ufo.x - s / 2.0F - 2.0F,
            this.ufo.y - s * 0.35F - 2.0F,
            s + 4.0F,
            s * 0.7F + 4.0F,
            34.0F,
            24.0F,
            gr -> paintUfo(gr, (int)(this.ufo.t * 8.0F) % 3),
            -1
         );
      }

      for (Asteroids.Shot s : this.shots) {
         int c = s.enemy ? -41816 : -3462;
         Gx.glow(this.X(s.x), this.Y(s.y), this.S(6.0F), Gx.withAlpha(c, 0.5F));
         this.circle(s.x, s.y, 1.5F, -1);
         this.circle(s.x - s.vx * 0.012F, s.y - s.vy * 0.012F, 1.1F, Gx.withAlpha(c, 0.6F));
      }

      this.fx.draw(this, 0.0F, 0.0F);
      if (!this.dead && (this.invul <= 0.0F || (int)(this.anim * 10.0F) % 2 == 0 || this.state != GameView.State.PLAYING)) {
         this.ship(this.x, this.y);
      }

      Gx.pop();

      for (int i = 0; i < Math.min(this.lives, 6); i++) {
         int a = Math.round(36.0F) % 48;
         GameFx.sprite(this, "as_ship" + a, 8 + i * 13, 8.0F, 14.0F, 14.0F, 30.0F, 30.0F, gr -> paintShip(gr, a), -855638017);
      }

      GameFx.outlined(this, "Welle " + this.wave, 366.0F, 15.0F, 8.0F, -10685697, -1442840576);
      if (this.bannerT >= 0.0F) {
         float t = this.bannerT / 1.8F;
         float s = Ease.outBack(Math.min(1.0F, this.bannerT / 0.35F));
         float a = t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
         Gx.pushAlpha(a);
         Gx.push();
         Gx.scaleAt(this.X(200.0F), this.Y(87.0F), s);
         Gx.glow(this.X(200.0F), this.Y(87.0F), this.S(70.0F), 1146942207);
         GameFx.outlined(this, "WELLE " + this.wave, 200.0F, 87.0F, 20.0F, -10685697, -872411112);
         Gx.pop();
         Gx.popAlpha();
      }

      if (this.dead && this.lives > 0 && this.deadT > 0.8F) {
         this.text("Mach dich bereit…", 200.0F, 175.0F, 8.0F, 3, Gx.withAlpha(-1, 0.6F + 0.4F * (float)Math.sin(this.anim * 6.0F)));
      }

      this.fx.drawPopups(this, 0.0F, 0.0F);
      this.fx.drawFlash(this, 400.0F, 290.0F);
   }

   private void rock(Asteroids.Rock r) {
      float rad = RAD[r.size];
      int a = Math.floorMod(Math.round(r.ang / (float) (Math.PI * 2) * 24.0F), 24);
      int sz = r.size;
      int var = r.variant;
      String key = "as_rock" + sz + "_" + var + "_" + a;
      float box = rad * 2.0F + 6.0F;

      for (int ox = -1; ox <= 1; ox++) {
         for (int oy = -1; oy <= 1; oy++) {
            float cx = r.x + ox * 400.0F;
            float cy = r.y + oy * 290.0F;
            if (!(cx + box < 0.0F) && !(cx - box > 400.0F) && !(cy + box < 0.0F) && !(cy - box > 290.0F)) {
               GameFx.sprite(this, key, cx - box / 2.0F, cy - box / 2.0F, box, box, 64.0F, 64.0F, gr -> paintRock(gr, sz, var, a), -1);
            }
         }
      }
   }

   private void ship(float sx, float sy) {
      int a = Math.floorMod(Math.round(this.ang / (float) (Math.PI * 2) * 48.0F), 48);
      if (this.thrust && this.state == GameView.State.PLAYING) {
         Gx.glow(
            this.X(sx - (float)Math.cos(this.ang) * 8.0F),
            this.Y(sy - (float)Math.sin(this.ang) * 8.0F),
            this.S(10.0F + this.rnd.nextFloat() * 3.0F),
            1728023101
         );
      }

      Gx.glow(this.X(sx), this.Y(sy), this.S(18.0F), 576516863);

      for (int ox = -1; ox <= 1; ox++) {
         for (int oy = -1; oy <= 1; oy++) {
            float cx = sx + ox * 400.0F;
            float cy = sy + oy * 290.0F;
            if (!(cx + 14.0F < 0.0F) && !(cx - 14.0F > 400.0F) && !(cy + 14.0F < 0.0F) && !(cy - 14.0F > 290.0F)) {
               GameFx.sprite(this, "as_ship" + a, cx - 13.0F, cy - 13.0F, 26.0F, 26.0F, 30.0F, 30.0F, gr -> paintShip(gr, a), -1);
            }
         }
      }
   }

   private static void neon(Graphics2D g, Shape s, int argb, float w) {
      Color c = new Color(argb);
      g.setStroke(new BasicStroke(w * 3.2F, 1, 1));
      g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
      g.draw(s);
      g.setStroke(new BasicStroke(w * 1.8F, 1, 1));
      g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110));
      g.draw(s);
      g.setStroke(new BasicStroke(w, 1, 1));
      g.setColor(new Color(Gx.mix(argb, -1, 0.45F)));
      g.draw(s);
   }

   private static void paintShip(Graphics2D g, int a) {
      g.translate(15, 15);
      g.rotate(a * Math.PI * 2.0 / 48.0);
      GeneralPath p = new GeneralPath();
      p.moveTo(11.0F, 0.0F);
      p.lineTo(-8.0, -7.5);
      p.lineTo(-5.0F, 0.0F);
      p.lineTo(-8.0, 7.5);
      p.closePath();
      g.setColor(new Color(10, 40, 60, 200));
      g.fill(p);
      neon(g, p, -10685697, 1.4F);
      g.setColor(new Color(16777215));
      g.fill(new Float(1.0F, -1.6F, 3.2F, 3.2F));
   }

   private static void paintRock(Graphics2D g, int size, int variant, int a) {
      g.translate(32, 32);
      g.rotate(a * Math.PI * 2.0 / 24.0);
      float scale = 32.0F / (RAD[size] + 3.0F);
      Random r = new Random(variant * 31 + size * 7);
      int n = 9 + r.nextInt(3);
      GeneralPath p = new GeneralPath();

      for (int i = 0; i < n; i++) {
         double an = i * Math.PI * 2.0 / n;
         float rr = RAD[size] * (0.72F + r.nextFloat() * 0.34F) * scale;
         float px = (float)Math.cos(an) * rr;
         float py = (float)Math.sin(an) * rr;
         if (i == 0) {
            p.moveTo(px, py);
         } else {
            p.lineTo(px, py);
         }
      }

      p.closePath();
      g.setColor(new Color(40, 20, 70, 170));
      g.fill(p);
      g.setStroke(new BasicStroke(1.0F));
      g.setColor(new Color(201, 166, 255, 70));

      for (int ix = 0; ix < 2; ix++) {
         float cx = (r.nextFloat() - 0.5F) * RAD[size] * scale;
         float cy = (r.nextFloat() - 0.5F) * RAD[size] * scale;
         float cr = (2.0F + r.nextFloat() * 3.0F) * scale * 0.6F;
         g.draw(new Float(cx - cr, cy - cr, cr * 2.0F, cr * 2.0F));
      }

      neon(g, p, -3561729, 1.5F * scale * 0.55F + 0.4F);
   }

   private static void paintUfo(Graphics2D g, int frame) {
      g.translate(2, 2);
      GeneralPath body = new GeneralPath();
      body.moveTo(0.0F, 12.0F);
      body.lineTo(8.0F, 7.0F);
      body.lineTo(22.0F, 7.0F);
      body.lineTo(30.0F, 12.0F);
      body.lineTo(22.0F, 17.0F);
      body.lineTo(8.0F, 17.0F);
      body.closePath();
      GeneralPath dome = new GeneralPath();
      dome.moveTo(9.0F, 7.0F);
      dome.curveTo(10.0F, 0.0F, 20.0F, 0.0F, 21.0F, 7.0F);
      g.setColor(new Color(60, 10, 40, 190));
      g.fill(body);
      neon(g, body, -41816, 1.2F);
      neon(g, dome, -41816, 1.2F);
      g.setStroke(new BasicStroke(0.9F));
      g.setColor(new Color(16765416));
      g.drawLine(2, 12, 28, 12);

      for (int i = 0; i < 3; i++) {
         g.setColor(i == frame ? new Color(16773754) : new Color(255, 120, 190, 120));
         g.fill(new Float(8.5F + i * 5.5F, 13.0F, 2.6F, 2.6F));
      }
   }

   private static void paintSpace(Graphics2D g) {
      g.setColor(new Color(329231));
      g.fillRect(0, 0, 400, 290);
      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(90.0F, 70.0F), 150.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(90, 40, 150, 70), new Color(90, 40, 150, 0)}
         )
      );
      g.fillRect(0, 0, 400, 290);
      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(330.0F, 230.0F),
            160.0F,
            new float[]{0.0F, 1.0F},
            new Color[]{new Color(20, 110, 150, 60), new Color(20, 110, 150, 0)}
         )
      );
      g.fillRect(0, 0, 400, 290);
      Random r = new Random(21L);

      for (int i = 0; i < 260; i++) {
         float sx = r.nextFloat() * 400.0F;
         float sy = r.nextFloat() * 290.0F;
         float s = r.nextFloat() < 0.9F ? 0.5F + r.nextFloat() * 0.6F : 1.2F + r.nextFloat();
         g.setColor(new Color(255, 255, 255, 40 + r.nextInt(150)));
         g.fill(new Float(sx, sy, s, s));
      }

      g.setPaint(
         new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(342.0F, 52.0F), 26.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(6966210), new Color(1774144)}
         )
      );
      g.fill(new Float(322.0F, 34.0F, 40.0F, 40.0F));
      g.setColor(new Color(180, 150, 255, 90));
      g.setStroke(new BasicStroke(1.4F));
      g.draw(new Float(310.0F, 49.0F, 64.0F, 10.0F));
   }

   private static final class Rock {
      float x;
      float y;
      float vx;
      float vy;
      float ang;
      float spin;
      int size;
      int variant;
      float flash;
   }

   private static final class Shot {
      float x;
      float y;
      float vx;
      float vy;
      float life;
      boolean enemy;
   }

   private static final class Ufo {
      float x;
      float y;
      float vx;
      float t;
      float fireT;
      boolean small;
      int hp;
   }
}
