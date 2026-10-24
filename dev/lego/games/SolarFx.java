package dev.lego.games;

import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class SolarFx {
   final SolarPlanet p;
   final Random rnd = new Random();
   float time;
   float sizeMul = 1.0F;
   final List<SolarFx.Part> parts = new ArrayList<>();
   final List<SolarFx.Shot> shots = new ArrayList<>();
   final List<SolarFx.Ship> ships = new ArrayList<>();
   final List<SolarFx.Area> areas = new ArrayList<>();
   final List<SolarFx.Glob> globs = new ArrayList<>();
   final List<SolarFx.Hole> holes = new ArrayList<>();
   final List<SolarFx.Ring> rings = new ArrayList<>();
   int tint;
   float tintA;
   float flash;
   SolarTools.Tool beam;
   float beamX;
   float beamY;
   float beamTick;
   float beamSnd;
   boolean beamOn;

   SolarFx(SolarPlanet p) {
      this.p = p;
   }

   void clear() {
      this.parts.clear();
      this.shots.clear();
      this.ships.clear();
      this.areas.clear();
      this.globs.clear();
      this.holes.clear();
      this.rings.clear();
      this.beamOn = false;
      this.tintA = 0.0F;
   }

   double[] surface(float x, float y) {
      float s = this.p.scale;
      if (!(s <= 0.02F) && !this.p.destroyed) {
         double ux = x / s;
         double uy = y / s;
         return ux * ux + uy * uy > 1.0 ? null : this.p.unproject(ux, uy);
      } else {
         return null;
      }
   }

   void fire(SolarTools.Tool t, float x, float y) {
      if (!this.p.destroyed || t.kind == SolarTools.Kind.HOLE) {
         switch (t.kind) {
            case BEAM:
               this.beam = t;
               this.beamOn = true;
               this.beamX = x;
               this.beamY = y;
               this.beamTick = 0.0F;
               break;
            case SHOT:
               this.shot(t, x, y, 0.0F);
               Sound.play(
                  t.sound.equals("minecraft:entity.generic.explode") ? "minecraft:entity.firework_rocket.launch" : "minecraft:entity.firework_rocket.launch",
                  0.7F,
                  0.4F
               );
               break;
            case RAIN:
               for (int i = 0; i < t.n; i++) {
                  double a = this.rnd.nextDouble() * Math.PI * 2.0;
                  double r = Math.sqrt(this.rnd.nextDouble()) * 0.45 * (t.n > 30 ? 1.6 : 1.0);
                  this.shot(t, (float)(x + Math.cos(a) * r), (float)(y + Math.sin(a) * r), t.dur * i / Math.max(1, t.n) + this.rnd.nextFloat() * 0.1F);
               }

               Sound.play("minecraft:entity.firework_rocket.launch", 0.6F, 0.4F);
               break;
            case SWARM:
               for (int i = 0; i < t.n; i++) {
                  SolarFx.Ship s = new SolarFx.Ship();
                  s.t = t;
                  s.ang = (float)(i * Math.PI * 2.0 / t.n + this.rnd.nextFloat() * 0.4F);
                  s.rad = (t.sprite == 1 ? 1.55F : 1.2F + this.rnd.nextFloat() * 0.45F) + 1.8F;
                  s.bob = this.rnd.nextFloat() * 6.0F;
                  s.life = t.dur;
                  s.fireT = 0.8F + this.rnd.nextFloat();
                  this.ships.add(s);
               }

               Sound.play(t.sound, 0.8F, 0.6F);
               break;
            case AREA:
               double[] ll = this.surface(x, y);
               if (ll == null) {
                  return;
               }

               SolarFx.Area a = new SolarFx.Area();
               a.t = t;
               a.lon = ll[0];
               a.lat = ll[1];
               this.areas.add(a);
               Sound.play(t.sound, 0.7F, 0.7F);
               break;
            case GLOBAL:
               SolarFx.Glob g = new SolarFx.Glob();
               g.t = t;
               this.globs.add(g);
               this.tint = t.color;
               Sound.play(t.sound, 0.6F, 0.8F);
               break;
            case HOLE:
               SolarFx.Hole h = new SolarFx.Hole();
               h.t = t;
               float d = (float)Math.hypot(x, y);
               if (d < 1.4F) {
                  float k = 1.6F / Math.max(0.01F, d);
                  x *= k;
                  y *= k;
                  if (d < 0.01F) {
                     x = 1.6F;
                     y = 0.0F;
                  }
               }

               h.x = x;
               h.y = y;
               this.holes.add(h);
               Sound.play(t.sound, 0.5F, 0.9F);
               break;
            case SPLIT:
               double[] ll = this.surface(x, y);
               if (ll == null) {
                  return;
               }

               int n = Math.round(3.0F * t.width);

               for (int i = 0; i < n; i++) {
                  this.p
                     .crack(
                        ll[0] + (this.rnd.nextDouble() - 0.5) * 0.4,
                        ll[1] + (this.rnd.nextDouble() - 0.5) * 0.3,
                        260.0F * t.width * this.sizeMul,
                        3.0F + 2.0F * t.width,
                        t.heat,
                        t.kill * 0.3F,
                        this.rnd
                     );
               }

               this.p.core = this.p.core + 0.06F * t.width;
               this.p.shake = Math.min(1.0F, this.p.shake + 0.5F * t.width);
               this.flash(x, y, 0.4F * t.width, t.color);
               Sound.play(t.sound, 0.6F, 0.9F);
               if (t.width >= 5.0F) {
                  this.p.core += 0.5F;
               }
         }
      }
   }

   void release() {
      this.beamOn = false;
   }

   private void shot(SolarTools.Tool t, float x, float y, float delay) {
      SolarFx.Shot s = new SolarFx.Shot();
      s.t = t;
      double a = -2.3 + this.rnd.nextDouble() * 0.6;
      if (t.cat == 1 || t.cat == 7) {
         a = -1.9 - this.rnd.nextDouble() * 1.2;
      }

      float from = 3.2F;
      s.sx = (float)(x + Math.cos(a) * from);
      s.sy = (float)(y + Math.sin(a) * from);
      s.tx = x;
      s.ty = y;
      s.x = s.sx;
      s.y = s.sy;
      s.dur = Math.max(0.15F, 1.2F / t.speed);
      s.delay = delay;
      this.shots.add(s);
   }

   void update(float dt) {
      this.time += dt;
      this.flash = Math.max(0.0F, this.flash - dt * 2.5F);
      if (this.globs.isEmpty()) {
         this.tintA = Math.max(0.0F, this.tintA - dt * 0.6F);
      }

      if (this.beamOn && this.beam != null && !this.p.destroyed) {
         this.beamTick -= dt;
         this.beamSnd -= dt;
         if (this.beamSnd <= 0.0F) {
            Sound.play(this.beam.sound, 1.4F + this.rnd.nextFloat() * 0.2F, 0.25F);
            this.beamSnd = 0.35F;
         }

         if (this.beamTick <= 0.0F) {
            this.beamTick = 0.05F / Math.max(0.5F, this.beam.speed);

            for (int i = 0; i < this.beam.n; i++) {
               float off = this.beam.n == 1 ? 0.0F : (i - (this.beam.n - 1) / 2.0F) * 0.12F;
               double[] ll = this.surface(this.beamX + off, this.beamY);
               if (ll != null) {
                  float r = this.beam.r * this.sizeMul * (0.85F + this.rnd.nextFloat() * 0.3F);
                  int col = this.beam.style == 19 ? hue(this.time) : this.beam.color;
                  this.p.paint(ll[0], ll[1], r, this.beam.style, this.beam.depth * 0.35F, this.beam.heat, this.beam.kill * 0.2F, 1.6F, col);
                  this.sparks(this.beamX + off, this.beamY, 2, col, 0.6F);
               }
            }
         }
      }

      for (int ix = this.shots.size() - 1; ix >= 0; ix--) {
         SolarFx.Shot s = this.shots.get(ix);
         if (s.delay > 0.0F) {
            s.delay -= dt;
         } else {
            s.k = s.k + dt / s.dur;
            float k = Math.min(1.0F, s.k);
            s.x = s.sx + (s.tx - s.sx) * k;
            s.y = s.sy + (s.ty - s.sy) * k;
            if (this.rnd.nextFloat() < 0.8F) {
               this.trail(s);
            }

            float d2 = s.x * s.x + s.y * s.y;
            float rr = this.p.scale * this.p.scale;
            if (k >= 1.0F || d2 < rr * 0.98F && k > 0.5F) {
               this.impact(s.t, s.x, s.y);
               this.shots.remove(ix);
            }
         }
      }

      for (int ixx = this.ships.size() - 1; ixx >= 0; ixx--) {
         SolarFx.Ship s = this.ships.get(ixx);
         s.life -= dt;
         float target = s.t.sprite == 1 ? 1.55F : 1.25F + ixx % 5 * 0.08F;
         if (s.life > 0.0F) {
            s.rad = s.rad + (target - s.rad) * Math.min(1.0F, dt * 1.4F);
         } else {
            s.rad += dt * 2.5F;
         }

         s.ang = s.ang + dt * (s.t.sprite == 1 ? 0.12F : 0.35F + ixx % 3 * 0.1F) * (ixx % 2 == 0 ? 1 : -1);
         s.bob += dt;
         if (s.rad > 4.0F) {
            this.ships.remove(ixx);
         } else {
            s.fireT = s.fireT - dt * Math.max(0.6F, s.t.speed);
            if (s.beamT > 0.0F) {
               s.beamT -= dt;
               if (!this.p.destroyed) {
                  this.p
                     .paint(
                        s.blon,
                        s.blat,
                        s.t.r * this.sizeMul * (s.t.sprite == 1 ? 1.4F : 1.0F),
                        s.t.style,
                        s.t.depth * 0.3F,
                        s.t.heat,
                        s.t.kill * 0.25F,
                        1.5F,
                        s.t.color
                     );
                  double[] pr = this.p.project(s.blon, s.blat);
                  if (pr[2] <= 0.0) {
                     s.beamT = 0.0F;
                  } else {
                     s.bx = (float)pr[0] * this.p.scale;
                     s.by = (float)pr[1] * this.p.scale;
                     if (this.rnd.nextFloat() < 0.5F) {
                        this.sparks(s.bx, s.by, 1, s.t.color, 0.4F);
                     }
                  }
               }
            } else if (s.fireT <= 0.0F && s.life > 0.5F && Math.abs(s.rad - target) < 0.2F && !this.p.destroyed) {
               float sx = (float)Math.cos(s.ang) * 0.75F + (this.rnd.nextFloat() - 0.5F) * 0.4F;
               float sy = (float)Math.sin(s.ang) * 0.75F + (this.rnd.nextFloat() - 0.5F) * 0.4F;
               double[] ll = this.surface(sx * this.p.scale, sy * this.p.scale);
               if (ll != null) {
                  s.blon = ll[0];
                  s.blat = ll[1];
                  s.beamT = s.t.sprite == 1 ? 2.2F : 0.45F;
                  s.bx = sx;
                  s.by = sy;
               }

               s.fireT = s.t.sprite == 1 ? 3.0F : 1.1F + this.rnd.nextFloat();
            }
         }
      }

      for (int ixx = this.areas.size() - 1; ixx >= 0; ixx--) {
         SolarFx.Area a = this.areas.get(ixx);
         a.age += dt;
         a.tick -= dt;
         if (a.tick <= 0.0F && !this.p.destroyed) {
            a.tick = 0.12F;
            float f = Math.min(1.0F, a.age / a.t.dur);
            double sp = a.t.r / 2048.0 * Math.PI * 2.0 * this.sizeMul;

            for (int kx = 0; kx < 3; kx++) {
               double ang = this.rnd.nextDouble() * Math.PI * 2.0;
               double d = Math.sqrt(this.rnd.nextDouble()) * sp * f;
               this.p
                  .paint(
                     a.lon + Math.cos(ang) * d / Math.max(0.2, Math.cos(a.lat)),
                     a.lat + Math.sin(ang) * d,
                     a.t.r * 0.35F * this.sizeMul,
                     a.t.style,
                     a.t.depth * 0.4F,
                     a.t.heat,
                     a.t.kill * 0.3F,
                     1.4F,
                     a.t.color
                  );
            }

            double[] pr = this.p.project(a.lon, a.lat);
            if (pr[2] > 0.0) {
               this.sparks((float)pr[0] * this.p.scale, (float)pr[1] * this.p.scale, 3, a.t.color, 0.8F);
            }
         }

         if (a.age > a.t.dur) {
            this.areas.remove(ixx);
         }
      }

      for (int ixx = this.globs.size() - 1; ixx >= 0; ixx--) {
         SolarFx.Glob g = this.globs.get(ixx);
         g.age += dt;
         g.tick -= dt;
         this.tintA = Math.min(0.35F, this.tintA + dt * 0.4F);
         if (g.tick <= 0.0F && !this.p.destroyed) {
            g.tick = 0.2F;
            float share = g.t.kill * 0.2F / Math.max(1.0F, g.t.dur * 5.0F) * 5.0F;
            this.p.killGlobal(share * 0.2F);
            int st = g.t.style;
            if (st == 1) {
               this.p.tempOff += 4.0F;
            }

            if (st == 2) {
               this.p.tempOff -= 5.0F;
            }

            if (st == 14) {
               this.p.lightsOff = Math.min(1.0F, this.p.lightsOff + 0.25F);
            }

            if (st == 1 && g.t.kill > 0.5F) {
               this.p.atmo = Math.max(0.0F, this.p.atmo - 0.03F);
            }

            if (st == 2 || st == 7 || st == 1) {
               for (int kx = 0; kx < 4; kx++) {
                  double lon = this.rnd.nextDouble() * Math.PI * 2.0;
                  double lat = (this.rnd.nextDouble() - 0.5) * (st == 2 ? 2.6 : 1.8);
                  this.p.paint(lon, lat, 40.0F + this.rnd.nextFloat() * 40.0F, st, 3.0F, st == 1 ? 90.0F : 0.0F, 0.1F, 1.2F, g.t.color);
               }
            }
         }

         if (g.age > g.t.dur) {
            this.globs.remove(ixx);
         }
      }

      for (int ixx = this.holes.size() - 1; ixx >= 0; ixx--) {
         SolarFx.Hole h = this.holes.get(ixx);
         h.age += dt;
         boolean white = h.t.color == -1;
         float pull = h.t.width * dt * (white ? 0.0F : 0.07F);
         if (!this.p.destroyed && h.age < h.t.dur) {
            this.p.scale = Math.max(0.0F, this.p.scale - pull * (0.4F + Math.min(1.0F, h.age / 3.0F)));
            this.p.killGlobal(white ? dt * 0.02 : dt * 0.06 * h.t.width);
            this.p.shake = Math.min(1.0F, this.p.shake + dt * 0.3F);
            if (white) {
               this.p.tempOff += dt * 8.0F;
            }

            if (this.rnd.nextFloat() < 0.8F) {
               SolarFx.Part q = this.part();
               double ax = this.rnd.nextDouble() * Math.PI * 2.0;
               q.x = (float)Math.cos(ax) * this.p.scale;
               q.y = (float)Math.sin(ax) * this.p.scale;
               float dx = h.x - q.x;
               float dy = h.y - q.y;
               float dl = (float)Math.hypot(dx, dy);
               q.vx = dx / dl * 0.9F - dy / dl * 0.4F;
               q.vy = dy / dl * 0.9F + dx / dl * 0.4F;
               if (white) {
                  q.vx = -q.vx;
                  q.vy = -q.vy;
               }

               q.max = q.life = 1.4F;
               q.size = 0.02F + this.rnd.nextFloat() * 0.03F;
               q.color = white ? -2864 : this.p.avgColor();
               q.sprite = 1;
            }

            if (this.p.scale < 0.06F) {
               this.explode();
            }
         }

         if (h.age > h.t.dur + 2.0F) {
            this.holes.remove(ixx);
         }
      }

      for (int ixx = this.parts.size() - 1; ixx >= 0; ixx--) {
         SolarFx.Part q = this.parts.get(ixx);
         q.life -= dt;
         if (q.life <= 0.0F) {
            this.parts.remove(ixx);
         } else {
            q.x = q.x + q.vx * dt;
            q.y = q.y + q.vy * dt;
            q.vx *= 1.0F - dt * 0.6F;
            q.vy *= 1.0F - dt * 0.6F;
         }
      }

      for (int ixxx = this.rings.size() - 1; ixxx >= 0; ixxx--) {
         SolarFx.Ring r = this.rings.get(ixxx);
         r.age += dt;
         if (r.age > r.max) {
            this.rings.remove(ixxx);
         }
      }

      if (!this.p.destroyed && this.p.hi != null && this.p.integrity() <= 0.01F) {
         this.explode();
      }
   }

   private void impact(SolarTools.Tool t, float x, float y) {
      double[] ll = this.surface(x, y);
      float big = Math.min(3.0F, t.r / 40.0F) * this.sizeMul;
      if (ll != null) {
         this.p.paint(ll[0], ll[1], t.r * this.sizeMul, t.style, t.depth, t.heat, t.kill, t.scorch, t.color);
         this.p.shake = Math.min(1.0F, this.p.shake + 0.12F + big * 0.25F);
         this.p.hitFlash = Math.min(1.0F, this.p.hitFlash + 0.3F * big);
      } else if (this.p.ringed) {
         this.p.damageRing(Math.atan2(y, x), 0.4F * big, 2.0F + big);
      }

      this.flash(x, y, 0.08F + 0.12F * big, t.style == 2 ? -2099969 : (t.cat == 6 ? t.color : -12150));
      this.sparks(x, y, 8 + Math.round(big * 10.0F), t.style == 2 ? -1509633 : this.p.avgColor(), 0.6F + big * 0.4F);
      SolarFx.Ring r = new SolarFx.Ring();
      r.x = x;
      r.y = y;
      r.max = 0.6F + big * 0.2F;
      r.size = 0.12F + big * 0.25F;
      r.color = t.color;
      this.rings.add(r);
      Sound.play(t.sound, Math.max(0.5F, 1.2F - big * 0.25F), Math.min(1.0F, 0.35F + big * 0.25F));
   }

   private void flash(float x, float y, float size, int color) {
      SolarFx.Part q = this.part();
      q.x = x;
      q.y = y;
      q.max = q.life = 0.45F;
      q.size = size;
      q.color = color;
      q.sprite = 0;
      q.glow = true;
   }

   private void sparks(float x, float y, int n, int color, float speed) {
      for (int i = 0; i < n; i++) {
         SolarFx.Part q = this.part();
         double a = this.rnd.nextDouble() * Math.PI * 2.0;
         double v = (0.2 + this.rnd.nextDouble()) * speed;
         q.x = x;
         q.y = y;
         q.vx = (float)(Math.cos(a) * v);
         q.vy = (float)(Math.sin(a) * v);
         q.max = q.life = 0.4F + this.rnd.nextFloat() * 0.6F;
         q.size = 0.008F + this.rnd.nextFloat() * 0.018F;
         q.color = color;
         q.sprite = this.rnd.nextFloat() < 0.5F ? 1 : 2;
         q.glow = q.sprite == 2;
      }
   }

   private void trail(SolarFx.Shot s) {
      SolarFx.Part q = this.part();
      q.x = s.x + (this.rnd.nextFloat() - 0.5F) * 0.02F;
      q.y = s.y + (this.rnd.nextFloat() - 0.5F) * 0.02F;
      q.max = q.life = 0.35F + this.rnd.nextFloat() * 0.3F;
      q.size = 0.02F + Math.min(0.08F, s.t.r / 1500.0F);
      q.color = s.t.style == 2 ? -4200193 : (s.t.cat == 6 ? -1 : -26038);
      q.sprite = 2;
      q.glow = true;
   }

   private SolarFx.Part part() {
      if (this.parts.size() > 1400) {
         this.parts.remove(0);
      }

      SolarFx.Part q = new SolarFx.Part();
      this.parts.add(q);
      return q;
   }

   void explode() {
      if (!this.p.destroyed) {
         this.p.destroyed = true;
         this.p.destroyedT = 0.0F;
         this.p.gAlive = 0.0;
         int col = this.p.avgColor();

         for (int i = 0; i < 260; i++) {
            SolarFx.Part q = this.part();
            double a = this.rnd.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(this.rnd.nextDouble()) * this.p.scale;
            double v = 0.3 + this.rnd.nextDouble() * 1.6;
            q.x = (float)(Math.cos(a) * r);
            q.y = (float)(Math.sin(a) * r);
            q.vx = (float)(Math.cos(a) * v);
            q.vy = (float)(Math.sin(a) * v);
            q.max = q.life = 3.0F + this.rnd.nextFloat() * 4.0F;
            q.size = 0.02F + this.rnd.nextFloat() * this.rnd.nextFloat() * 0.18F;
            q.color = this.rnd.nextFloat() < 0.25F ? -30166 : col;
            q.sprite = this.rnd.nextFloat() < 0.25F ? 2 : 1;
            q.glow = q.sprite == 2;
         }

         for (int k = 0; k < 3; k++) {
            SolarFx.Ring r = new SolarFx.Ring();
            r.max = 2.5F + k;
            r.size = 3.0F + k * 1.5F;
            r.color = k == 0 ? -8032 : -30134;
            this.rings.add(r);
         }

         this.flash(0.0F, 0.0F, 3.0F, -3888);
         this.flash = 1.0F;
         Sound.play("minecraft:entity.generic.explode", 0.5F, 1.0F);
         Sound.play("minecraft:entity.warden.sonic_boom", 0.6F, 0.8F);
         this.ships.clear();
         this.areas.clear();
         this.globs.clear();
         this.beamOn = false;
      }
   }

   static int hue(float t) {
      return 0xFF000000 | Color.HSBtoRGB(t * 0.4F % 1.0F, 0.8F, 1.0F) & 16777215;
   }

   private static Gx.Img glowImg() {
      return Gx.get("solar:fx:flare", 0, () -> SolarTex.flare(128));
   }

   private static Gx.Img softImg() {
      return Gx.get("solar:fx:soft", 0, () -> SolarTex.softRing(256));
   }

   private static Gx.Img rockImg(int i) {
      int k = i & 3;
      return Gx.get("solar:fx:rock" + k, 0, () -> SolarTex.rock(64, 11 + k, -1));
   }

   private static Gx.Img haloImg() {
      return Gx.get("solar:fx:halo", 0, () -> SolarTex.halo(256));
   }

   private static Gx.Img swirlImg() {
      return Gx.get("solar:fx:swirl", 0, () -> SolarTex.swirl(256, 3));
   }

   private static Gx.Img accImg() {
      return Gx.get("solar:fx:acc", 0, () -> SolarTex.accretion(512, 192));
   }

   static Gx.Img shipImg(int kind) {
      if (kind == 5) {
         return Gx.painted("solar:fx:sat", 96, 96, g -> SolarTex.satellite(g, 96, -38342));
      } else {
         return kind == 6
            ? Gx.painted("solar:fx:sleigh", 128, 96, SolarFx::paintSleigh)
            : Gx.painted("solar:fx:ufo" + kind, 160, 160, g -> SolarTex.ufo(g, 160, kind, -9633892));
      }
   }

   static Gx.Img projImg(SolarTools.Tool t) {
      if (t.sprite >= 0 && t.cat == 6) {
         int k = t.sprite;
         return Gx.painted("solar:fx:fun" + k, 128, 128, g -> SolarTex.fun(g, 128, k, t.color));
      } else if (t.sprite == -2) {
         return Gx.painted("solar:fx:walker", 128, 128, g -> SolarTex.walker(g, 128, -7667860));
      } else if (t.sprite == -3) {
         return Gx.painted("solar:fx:gift", 96, 96, SolarFx::paintGift);
      } else {
         return t.sprite == -4 ? Gx.painted("solar:fx:snowman", 96, 96, SolarFx::paintSnowman) : null;
      }
   }

   void drawBack(float cx, float cy, float R) {
      for (SolarFx.Hole h : this.holes) {
         float grow = Math.min(1.0F, h.age / 1.2F) * (h.age > h.t.dur ? Math.max(0.0F, 1.0F - (h.age - h.t.dur) / 2.0F) : 1.0F);
         float s = R * 0.9F * h.t.width * grow;
         boolean white = h.t.color == -1;
         float hx = cx + h.x * R;
         float hy = cy + h.y * R;
         Gx.Img acc = accImg();
         if (acc != null) {
            Gx.image(acc, Math.round(hx - s * 2.0F), Math.round(hy - s * 0.75F), Math.round(s * 4.0F), Math.round(s * 1.5F), white ? -1 : -20368);
         }

         Gx.Img sw = swirlImg();
         if (sw != null) {
            Gx.image(sw, Math.round(hx - s * 1.3F), Math.round(hy - s * 1.3F), Math.round(s * 2.6F), Math.round(s * 2.6F), white ? -855638017 : -1716489985);
         }

         Gx.glow(Math.round(hx), Math.round(hy), Math.round(s * 1.6F), white ? -1996488705 : 1728023098);
         if (!white) {
            Gx.circle(Math.round(hx), Math.round(hy), Math.max(2, Math.round(s * 0.42F)), -16777216);
         } else {
            Gx.circle(Math.round(hx), Math.round(hy), Math.max(2, Math.round(s * 0.3F)), -1);
         }
      }
   }

   void drawFront(float cx, float cy, float R) {
      Gx.Img glow = glowImg();
      Gx.Img soft = softImg();

      for (SolarFx.Ring r : this.rings) {
         float k = r.age / r.max;
         float s = R * r.size * (0.2F + k);
         if (soft != null) {
            Gx.image(
               soft,
               Math.round(cx + r.x * R - s),
               Math.round(cy + r.y * R - s),
               Math.round(s * 2.0F),
               Math.round(s * 2.0F),
               Gx.withAlpha(r.color, (1.0F - k) * 0.9F)
            );
         }
      }

      for (SolarFx.Shot s : this.shots) {
         if (!(s.delay > 0.0F)) {
            float x = cx + s.x * R;
            float y = cy + s.y * R;
            float sz = R * Math.max(0.035F, Math.min(0.5F, s.t.r / 700.0F)) * this.sizeMul;
            Gx.Img pi = projImg(s.t);
            if (pi != null) {
               int d = Math.round(sz * 2.2F);
               Gx.image(pi, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d, -1);
            } else {
               if (glow != null) {
                  int d = Math.round(sz * 4.0F);
                  Gx.image(
                     glow, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d, Gx.withAlpha(s.t.cat != 0 && s.t.cat != 5 ? -26038 : s.t.color, 0.8F)
                  );
               }

               boolean energy = s.t.cat == 4 || s.t.cat == 5 || s.t.style == 3;
               if (energy) {
                  Gx.circle(Math.round(x), Math.round(y), Math.max(2, Math.round(sz * 0.8F)), s.t.color);
               } else {
                  Gx.Img rk = rockImg(s.t.name.hashCode());
                  if (rk != null) {
                     int d = Math.round(sz * 2.0F);
                     Gx.image(rk, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d, s.t.color);
                  }
               }
            }
         }
      }

      for (SolarFx.Ship sx : this.ships) {
         float rx = sx.rad;
         float bob = (float)Math.sin(sx.bob * 2.0F) * 0.03F;
         float x = cx + (float)Math.cos(sx.ang) * rx * R;
         float y = cy + ((float)Math.sin(sx.ang) * rx + bob) * R;
         if (sx.beamT > 0.0F) {
            beamLine(x, y, cx + sx.bx * R, cy + sx.by * R, R * 0.012F * sx.t.width * (sx.t.sprite == 1 ? 2.5F : 1.0F), sx.t.color);
         }

         int d = Math.round(R * (sx.t.sprite == 1 ? 0.55F : (sx.t.sprite == 5 ? 0.09F : 0.16F)));
         Gx.Img im = shipImg(sx.t.sprite);
         if (im != null) {
            Gx.image(im, Math.round(x - d / 2.0F), Math.round(y - d / 2.0F), d, d * (sx.t.sprite == 6 ? 3 : 4) / 4, -1);
         }

         if (glow != null) {
            Gx.image(glow, Math.round(x - d * 0.4F), Math.round(y + d * 0.1F), Math.round(d * 0.8F), Math.round(d * 0.5F), Gx.withAlpha(sx.t.color, 0.35F));
         }
      }

      if (this.beamOn && this.beam != null && !this.p.destroyed) {
         float ox = cx - R * 2.6F;
         float oy = cy - R * 1.7F;
         int col = this.beam.style == 19 ? hue(this.time) : this.beam.color;

         for (int i = 0; i < this.beam.n; i++) {
            float off = this.beam.n == 1 ? 0.0F : (i - (this.beam.n - 1) / 2.0F) * 0.12F;
            beamLine(ox + off * R * 3.0F, oy, cx + (this.beamX + off) * R, cy + this.beamY * R, R * 0.014F * this.beam.width * this.sizeMul, col);
         }
      }

      for (SolarFx.Area a : this.areas) {
         double[] pr = this.p.project(a.lon, a.lat);
         if (!(pr[2] <= 0.0) && glow != null) {
            float f = Math.min(1.0F, a.age / a.t.dur);
            float sx = R * (0.1F + 0.3F * f) * (a.t.r / 80.0F) * this.sizeMul;
            float xx = cx + (float)pr[0] * this.p.scale * R;
            float yx = cy + (float)pr[1] * this.p.scale * R;
            Gx.image(
               glow, Math.round(xx - sx), Math.round(yx - sx), Math.round(sx * 2.0F), Math.round(sx * 2.0F), Gx.withAlpha(a.t.color, 0.5F * (1.0F - f * 0.5F))
            );
         }
      }

      for (SolarFx.Part q : this.parts) {
         float k = q.life / q.max;
         float xx = cx + q.x * R;
         float yx = cy + q.y * R;
         float sx = Math.max(1.0F, q.size * R * (q.glow ? 0.6F + 0.6F * k : 1.0F));
         if (q.sprite == 0 && glow != null) {
            Gx.image(
               glow, Math.round(xx - sx), Math.round(yx - sx), Math.round(sx * 2.0F), Math.round(sx * 2.0F), Gx.withAlpha(q.color, Math.min(1.0F, k * 1.5F))
            );
         } else if (q.sprite == 2 && glow != null) {
            Gx.image(glow, Math.round(xx - sx * 1.5F), Math.round(yx - sx * 1.5F), Math.round(sx * 3.0F), Math.round(sx * 3.0F), Gx.withAlpha(q.color, k));
         } else {
            Gx.Img rk = rockImg((int)(q.size * 1000.0F));
            if (rk != null) {
               Gx.image(
                  rk, Math.round(xx - sx), Math.round(yx - sx), Math.round(sx * 2.0F), Math.round(sx * 2.0F), Gx.withAlpha(q.color, Math.min(1.0F, k * 2.0F))
               );
            }
         }
      }
   }

   static void beamLine(float x0, float y0, float x1, float y1, float w, int color) {
      Gx.Img glow = glowImg();
      if (glow != null) {
         float dx = x1 - x0;
         float dy = y1 - y0;
         float len = (float)Math.hypot(dx, dy);
         float stepLen = Math.max(2.0F, w * 1.2F);
         int n = Math.min(260, Math.max(2, Math.round(len / stepLen)));
         int d = Math.max(3, Math.round(w * 5.0F));
         int dc = Math.max(2, Math.round(w * 1.6F));

         for (int i = 0; i <= n; i++) {
            float t = (float)i / n;
            int x = Math.round(x0 + dx * t);
            int y = Math.round(y0 + dy * t);
            Gx.image(glow, x - d / 2, y - d / 2, d, d, Gx.withAlpha(color, 0.55F));
            Gx.image(glow, x - dc / 2, y - dc / 2, dc, dc, -419430401);
         }

         int e = Math.round(w * 14.0F);
         Gx.image(glow, Math.round(x1 - e / 2.0F), Math.round(y1 - e / 2.0F), e, e, Gx.withAlpha(color, 0.9F));
         Gx.image(glow, Math.round(x0 - e / 3.0F), Math.round(y0 - e / 3.0F), e * 2 / 3, e * 2 / 3, Gx.withAlpha(color, 0.8F));
      }
   }

   private static void paintGift(Graphics2D g) {
      g.setColor(new Color(14033464));
      g.fill(new Double(14.0, 30.0, 68.0, 58.0, 8.0, 8.0));
      g.setColor(new Color(11735583));
      g.fill(new Double(10.0, 24.0, 76.0, 16.0, 6.0, 6.0));
      g.setColor(new Color(16040274));
      g.fillRect(42, 24, 12, 64);
      g.fillRect(10, 29, 76, 6);
      g.fill(new java.awt.geom.Ellipse2D.Double(26.0, 8.0, 22.0, 18.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(48.0, 8.0, 22.0, 18.0));
   }

   private static void paintSnowman(Graphics2D g) {
      g.setColor(new Color(16054527));
      g.fill(new java.awt.geom.Ellipse2D.Double(20.0, 44.0, 56.0, 50.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(30.0, 18.0, 36.0, 34.0));
      g.setColor(new Color(1710618));
      g.fillRect(32, 6, 32, 6);
      g.fillRect(38, -8, 20, 16);
      g.fill(new java.awt.geom.Ellipse2D.Double(40.0, 30.0, 5.0, 5.0));
      g.fill(new java.awt.geom.Ellipse2D.Double(52.0, 30.0, 5.0, 5.0));
      g.setColor(new Color(16747050));
      g.fill(new Polygon(new int[]{47, 62, 47}, new int[]{36, 40, 42}, 3));
      g.setColor(new Color(14886459));
      g.fillRect(30, 48, 36, 7);
   }

   private static void paintSleigh(Graphics2D g) {
      g.setColor(new Color(11569754));

      for (int i = 0; i < 3; i++) {
         g.fill(new java.awt.geom.Ellipse2D.Double(4 + i * 22, 30.0, 22.0, 12.0));
         g.fillRect(8 + i * 22, 40, 3, 12);
         g.fillRect(20 + i * 22, 40, 3, 12);
      }

      g.setColor(new Color(14886459));
      g.fill(new java.awt.geom.Ellipse2D.Double(24.0, 32.0, 5.0, 5.0));
      g.setColor(new Color(11867436));
      g.fill(new Double(74.0, 30.0, 46.0, 26.0, 10.0, 10.0));
      g.setColor(new Color(16040274));
      g.fillRect(70, 58, 54, 4);
      g.setColor(new Color(8018490));
      g.fill(new java.awt.geom.Ellipse2D.Double(92.0, 12.0, 26.0, 26.0));
   }

   static final class Area {
      SolarTools.Tool t;
      double lon;
      double lat;
      float age;
      float tick;
   }

   static final class Glob {
      SolarTools.Tool t;
      float age;
      float tick;
   }

   static final class Hole {
      SolarTools.Tool t;
      float x;
      float y;
      float age;
   }

   static final class Part {
      float x;
      float y;
      float vx;
      float vy;
      float life;
      float max;
      float size;
      int color;
      int sprite;
      boolean glow;
   }

   static final class Ring {
      float x;
      float y;
      float age;
      float max;
      float size;
      int color;
   }

   static final class Ship {
      SolarTools.Tool t;
      float ang;
      float rad;
      float bob;
      float life;
      float fireT;
      float beamT;
      float bx;
      float by;
      double blon;
      double blat;
   }

   static final class Shot {
      SolarTools.Tool t;
      float x;
      float y;
      float sx;
      float sy;
      float tx;
      float ty;
      float k;
      float dur;
      float delay;
      double lon;
      double lat;
      boolean hit;
   }
}
