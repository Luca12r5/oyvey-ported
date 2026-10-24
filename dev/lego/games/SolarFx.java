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

   SolarFx(SolarPlanet var1) {
      this.p = var1;
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

   double[] surface(float var1, float var2) {
      float var3 = this.p.scale;
      if (!(var3 <= 0.02F) && !this.p.destroyed) {
         double var4 = var1 / var3;
         double var6 = var2 / var3;
         return var4 * var4 + var6 * var6 > 1.0 ? null : this.p.unproject(var4, var6);
      } else {
         return null;
      }
   }

   void fire(SolarTools.Tool var1, float var2, float var3) {
      if (!this.p.destroyed || var1.kind == SolarTools.Kind.HOLE) {
         switch (var1.kind) {
            case BEAM:
               this.beam = var1;
               this.beamOn = true;
               this.beamX = var2;
               this.beamY = var3;
               this.beamTick = 0.0F;
               break;
            case SHOT:
               this.shot(var1, var2, var3, 0.0F);
               Sound.play(
                  var1.sound.equals("minecraft:entity.generic.explode") ? "minecraft:entity.firework_rocket.launch" : "minecraft:entity.firework_rocket.launch",
                  0.7F,
                  0.4F
               );
               break;
            case RAIN:
               for (int var13 = 0; var13 < var1.n; var13++) {
                  double var17 = this.rnd.nextDouble() * Math.PI * 2.0;
                  double var7 = Math.sqrt(this.rnd.nextDouble()) * 0.45 * (var1.n > 30 ? 1.6 : 1.0);
                  this.shot(
                     var1,
                     (float)(var2 + Math.cos(var17) * var7),
                     (float)(var3 + Math.sin(var17) * var7),
                     var1.dur * var13 / Math.max(1, var1.n) + this.rnd.nextFloat() * 0.1F
                  );
               }

               Sound.play("minecraft:entity.firework_rocket.launch", 0.6F, 0.4F);
               break;
            case SWARM:
               for (int var12 = 0; var12 < var1.n; var12++) {
                  SolarFx.Ship var16 = new SolarFx.Ship();
                  var16.t = var1;
                  var16.ang = (float)(var12 * Math.PI * 2.0 / var1.n + this.rnd.nextFloat() * 0.4F);
                  var16.rad = (var1.sprite == 1 ? 1.55F : 1.2F + this.rnd.nextFloat() * 0.45F) + 1.8F;
                  var16.bob = this.rnd.nextFloat() * 6.0F;
                  var16.life = var1.dur;
                  var16.fireT = 0.8F + this.rnd.nextFloat();
                  this.ships.add(var16);
               }

               Sound.play(var1.sound, 0.8F, 0.6F);
               break;
            case AREA:
               double[] var11 = this.surface(var2, var3);
               if (var11 == null) {
                  return;
               }

               SolarFx.Area var15 = new SolarFx.Area();
               var15.t = var1;
               var15.lon = var11[0];
               var15.lat = var11[1];
               this.areas.add(var15);
               Sound.play(var1.sound, 0.7F, 0.7F);
               break;
            case GLOBAL:
               SolarFx.Glob var10 = new SolarFx.Glob();
               var10.t = var1;
               this.globs.add(var10);
               this.tint = var1.color;
               Sound.play(var1.sound, 0.6F, 0.8F);
               break;
            case HOLE:
               SolarFx.Hole var9 = new SolarFx.Hole();
               var9.t = var1;
               float var14 = (float)Math.hypot(var2, var3);
               if (var14 < 1.4F) {
                  float var18 = 1.6F / Math.max(0.01F, var14);
                  var2 *= var18;
                  var3 *= var18;
                  if (var14 < 0.01F) {
                     var2 = 1.6F;
                     var3 = 0.0F;
                  }
               }

               var9.x = var2;
               var9.y = var3;
               this.holes.add(var9);
               Sound.play(var1.sound, 0.5F, 0.9F);
               break;
            case SPLIT:
               double[] var4 = this.surface(var2, var3);
               if (var4 == null) {
                  return;
               }

               int var5 = Math.round(3.0F * var1.width);

               for (int var6 = 0; var6 < var5; var6++) {
                  this.p
                     .crack(
                        var4[0] + (this.rnd.nextDouble() - 0.5) * 0.4,
                        var4[1] + (this.rnd.nextDouble() - 0.5) * 0.3,
                        260.0F * var1.width * this.sizeMul,
                        3.0F + 2.0F * var1.width,
                        var1.heat,
                        var1.kill * 0.3F,
                        this.rnd
                     );
               }

               this.p.core = this.p.core + 0.06F * var1.width;
               this.p.shake = Math.min(1.0F, this.p.shake + 0.5F * var1.width);
               this.flash(var2, var3, 0.4F * var1.width, var1.color);
               Sound.play(var1.sound, 0.6F, 0.9F);
               if (var1.width >= 5.0F) {
                  this.p.core += 0.5F;
               }
         }
      }
   }

   void release() {
      this.beamOn = false;
   }

   private void shot(SolarTools.Tool var1, float var2, float var3, float var4) {
      SolarFx.Shot var5 = new SolarFx.Shot();
      var5.t = var1;
      double var6 = -2.3 + this.rnd.nextDouble() * 0.6;
      if (var1.cat == 1 || var1.cat == 7) {
         var6 = -1.9 - this.rnd.nextDouble() * 1.2;
      }

      float var8 = 3.2F;
      var5.sx = (float)(var2 + Math.cos(var6) * var8);
      var5.sy = (float)(var3 + Math.sin(var6) * var8);
      var5.tx = var2;
      var5.ty = var3;
      var5.x = var5.sx;
      var5.y = var5.sy;
      var5.dur = Math.max(0.15F, 1.2F / var1.speed);
      var5.delay = var4;
      this.shots.add(var5);
   }

   void update(float var1) {
      this.time += var1;
      this.flash = Math.max(0.0F, this.flash - var1 * 2.5F);
      if (this.globs.isEmpty()) {
         this.tintA = Math.max(0.0F, this.tintA - var1 * 0.6F);
      }

      if (this.beamOn && this.beam != null && !this.p.destroyed) {
         this.beamTick -= var1;
         this.beamSnd -= var1;
         if (this.beamSnd <= 0.0F) {
            Sound.play(this.beam.sound, 1.4F + this.rnd.nextFloat() * 0.2F, 0.25F);
            this.beamSnd = 0.35F;
         }

         if (this.beamTick <= 0.0F) {
            this.beamTick = 0.05F / Math.max(0.5F, this.beam.speed);

            for (int var2 = 0; var2 < this.beam.n; var2++) {
               float var3 = this.beam.n == 1 ? 0.0F : (var2 - (this.beam.n - 1) / 2.0F) * 0.12F;
               double[] var4 = this.surface(this.beamX + var3, this.beamY);
               if (var4 != null) {
                  float var5 = this.beam.r * this.sizeMul * (0.85F + this.rnd.nextFloat() * 0.3F);
                  int var6 = this.beam.style == 19 ? hue(this.time) : this.beam.color;
                  this.p.paint(var4[0], var4[1], var5, this.beam.style, this.beam.depth * 0.35F, this.beam.heat, this.beam.kill * 0.2F, 1.6F, var6);
                  this.sparks(this.beamX + var3, this.beamY, 2, var6, 0.6F);
               }
            }
         }
      }

      for (int var12 = this.shots.size() - 1; var12 >= 0; var12--) {
         SolarFx.Shot var19 = this.shots.get(var12);
         if (var19.delay > 0.0F) {
            var19.delay -= var1;
         } else {
            var19.k = var19.k + var1 / var19.dur;
            float var26 = Math.min(1.0F, var19.k);
            var19.x = var19.sx + (var19.tx - var19.sx) * var26;
            var19.y = var19.sy + (var19.ty - var19.sy) * var26;
            if (this.rnd.nextFloat() < 0.8F) {
               this.trail(var19);
            }

            float var31 = var19.x * var19.x + var19.y * var19.y;
            float var37 = this.p.scale * this.p.scale;
            if (var26 >= 1.0F || var31 < var37 * 0.98F && var26 > 0.5F) {
               this.impact(var19.t, var19.x, var19.y);
               this.shots.remove(var12);
            }
         }
      }

      for (int var13 = this.ships.size() - 1; var13 >= 0; var13--) {
         SolarFx.Ship var20 = this.ships.get(var13);
         var20.life -= var1;
         float var27 = var20.t.sprite == 1 ? 1.55F : 1.25F + var13 % 5 * 0.08F;
         if (var20.life > 0.0F) {
            var20.rad = var20.rad + (var27 - var20.rad) * Math.min(1.0F, var1 * 1.4F);
         } else {
            var20.rad += var1 * 2.5F;
         }

         var20.ang = var20.ang + var1 * (var20.t.sprite == 1 ? 0.12F : 0.35F + var13 % 3 * 0.1F) * (var13 % 2 == 0 ? 1 : -1);
         var20.bob += var1;
         if (var20.rad > 4.0F) {
            this.ships.remove(var13);
         } else {
            var20.fireT = var20.fireT - var1 * Math.max(0.6F, var20.t.speed);
            if (var20.beamT > 0.0F) {
               var20.beamT -= var1;
               if (!this.p.destroyed) {
                  this.p
                     .paint(
                        var20.blon,
                        var20.blat,
                        var20.t.r * this.sizeMul * (var20.t.sprite == 1 ? 1.4F : 1.0F),
                        var20.t.style,
                        var20.t.depth * 0.3F,
                        var20.t.heat,
                        var20.t.kill * 0.25F,
                        1.5F,
                        var20.t.color
                     );
                  double[] var32 = this.p.project(var20.blon, var20.blat);
                  if (var32[2] <= 0.0) {
                     var20.beamT = 0.0F;
                  } else {
                     var20.bx = (float)var32[0] * this.p.scale;
                     var20.by = (float)var32[1] * this.p.scale;
                     if (this.rnd.nextFloat() < 0.5F) {
                        this.sparks(var20.bx, var20.by, 1, var20.t.color, 0.4F);
                     }
                  }
               }
            } else if (var20.fireT <= 0.0F && var20.life > 0.5F && Math.abs(var20.rad - var27) < 0.2F && !this.p.destroyed) {
               float var33 = (float)Math.cos(var20.ang) * 0.75F + (this.rnd.nextFloat() - 0.5F) * 0.4F;
               float var38 = (float)Math.sin(var20.ang) * 0.75F + (this.rnd.nextFloat() - 0.5F) * 0.4F;
               double[] var7 = this.surface(var33 * this.p.scale, var38 * this.p.scale);
               if (var7 != null) {
                  var20.blon = var7[0];
                  var20.blat = var7[1];
                  var20.beamT = var20.t.sprite == 1 ? 2.2F : 0.45F;
                  var20.bx = var33;
                  var20.by = var38;
               }

               var20.fireT = var20.t.sprite == 1 ? 3.0F : 1.1F + this.rnd.nextFloat();
            }
         }
      }

      for (int var14 = this.areas.size() - 1; var14 >= 0; var14--) {
         SolarFx.Area var21 = this.areas.get(var14);
         var21.age += var1;
         var21.tick -= var1;
         if (var21.tick <= 0.0F && !this.p.destroyed) {
            var21.tick = 0.12F;
            float var28 = Math.min(1.0F, var21.age / var21.t.dur);
            double var34 = var21.t.r / 2048.0 * Math.PI * 2.0 * this.sizeMul;

            for (int var41 = 0; var41 < 3; var41++) {
               double var8 = this.rnd.nextDouble() * Math.PI * 2.0;
               double var10 = Math.sqrt(this.rnd.nextDouble()) * var34 * var28;
               this.p
                  .paint(
                     var21.lon + Math.cos(var8) * var10 / Math.max(0.2, Math.cos(var21.lat)),
                     var21.lat + Math.sin(var8) * var10,
                     var21.t.r * 0.35F * this.sizeMul,
                     var21.t.style,
                     var21.t.depth * 0.4F,
                     var21.t.heat,
                     var21.t.kill * 0.3F,
                     1.4F,
                     var21.t.color
                  );
            }

            double[] var42 = this.p.project(var21.lon, var21.lat);
            if (var42[2] > 0.0) {
               this.sparks((float)var42[0] * this.p.scale, (float)var42[1] * this.p.scale, 3, var21.t.color, 0.8F);
            }
         }

         if (var21.age > var21.t.dur) {
            this.areas.remove(var14);
         }
      }

      for (int var15 = this.globs.size() - 1; var15 >= 0; var15--) {
         SolarFx.Glob var22 = this.globs.get(var15);
         var22.age += var1;
         var22.tick -= var1;
         this.tintA = Math.min(0.35F, this.tintA + var1 * 0.4F);
         if (var22.tick <= 0.0F && !this.p.destroyed) {
            var22.tick = 0.2F;
            float var29 = var22.t.kill * 0.2F / Math.max(1.0F, var22.t.dur * 5.0F) * 5.0F;
            this.p.killGlobal(var29 * 0.2F);
            int var35 = var22.t.style;
            if (var35 == 1) {
               this.p.tempOff += 4.0F;
            }

            if (var35 == 2) {
               this.p.tempOff -= 5.0F;
            }

            if (var35 == 14) {
               this.p.lightsOff = Math.min(1.0F, this.p.lightsOff + 0.25F);
            }

            if (var35 == 1 && var22.t.kill > 0.5F) {
               this.p.atmo = Math.max(0.0F, this.p.atmo - 0.03F);
            }

            if (var35 == 2 || var35 == 7 || var35 == 1) {
               for (int var39 = 0; var39 < 4; var39++) {
                  double var43 = this.rnd.nextDouble() * Math.PI * 2.0;
                  double var9 = (this.rnd.nextDouble() - 0.5) * (var35 == 2 ? 2.6 : 1.8);
                  this.p.paint(var43, var9, 40.0F + this.rnd.nextFloat() * 40.0F, var35, 3.0F, var35 == 1 ? 90.0F : 0.0F, 0.1F, 1.2F, var22.t.color);
               }
            }
         }

         if (var22.age > var22.t.dur) {
            this.globs.remove(var15);
         }
      }

      for (int var16 = this.holes.size() - 1; var16 >= 0; var16--) {
         SolarFx.Hole var23 = this.holes.get(var16);
         var23.age += var1;
         boolean var30 = var23.t.color == -1;
         float var36 = var23.t.width * var1 * (var30 ? 0.0F : 0.07F);
         if (!this.p.destroyed && var23.age < var23.t.dur) {
            this.p.scale = Math.max(0.0F, this.p.scale - var36 * (0.4F + Math.min(1.0F, var23.age / 3.0F)));
            this.p.killGlobal(var30 ? var1 * 0.02 : var1 * 0.06 * var23.t.width);
            this.p.shake = Math.min(1.0F, this.p.shake + var1 * 0.3F);
            if (var30) {
               this.p.tempOff += var1 * 8.0F;
            }

            if (this.rnd.nextFloat() < 0.8F) {
               SolarFx.Part var40 = this.part();
               double var44 = this.rnd.nextDouble() * Math.PI * 2.0;
               var40.x = (float)Math.cos(var44) * this.p.scale;
               var40.y = (float)Math.sin(var44) * this.p.scale;
               float var45 = var23.x - var40.x;
               float var46 = var23.y - var40.y;
               float var11 = (float)Math.hypot(var45, var46);
               var40.vx = var45 / var11 * 0.9F - var46 / var11 * 0.4F;
               var40.vy = var46 / var11 * 0.9F + var45 / var11 * 0.4F;
               if (var30) {
                  var40.vx = -var40.vx;
                  var40.vy = -var40.vy;
               }

               var40.max = var40.life = 1.4F;
               var40.size = 0.02F + this.rnd.nextFloat() * 0.03F;
               var40.color = var30 ? -2864 : this.p.avgColor();
               var40.sprite = 1;
            }

            if (this.p.scale < 0.06F) {
               this.explode();
            }
         }

         if (var23.age > var23.t.dur + 2.0F) {
            this.holes.remove(var16);
         }
      }

      for (int var17 = this.parts.size() - 1; var17 >= 0; var17--) {
         SolarFx.Part var24 = this.parts.get(var17);
         var24.life -= var1;
         if (var24.life <= 0.0F) {
            this.parts.remove(var17);
         } else {
            var24.x = var24.x + var24.vx * var1;
            var24.y = var24.y + var24.vy * var1;
            var24.vx *= 1.0F - var1 * 0.6F;
            var24.vy *= 1.0F - var1 * 0.6F;
         }
      }

      for (int var18 = this.rings.size() - 1; var18 >= 0; var18--) {
         SolarFx.Ring var25 = this.rings.get(var18);
         var25.age += var1;
         if (var25.age > var25.max) {
            this.rings.remove(var18);
         }
      }

      if (!this.p.destroyed && this.p.hi != null && this.p.integrity() <= 0.01F) {
         this.explode();
      }
   }

   private void impact(SolarTools.Tool var1, float var2, float var3) {
      double[] var4 = this.surface(var2, var3);
      float var5 = Math.min(3.0F, var1.r / 40.0F) * this.sizeMul;
      if (var4 != null) {
         this.p.paint(var4[0], var4[1], var1.r * this.sizeMul, var1.style, var1.depth, var1.heat, var1.kill, var1.scorch, var1.color);
         this.p.shake = Math.min(1.0F, this.p.shake + 0.12F + var5 * 0.25F);
         this.p.hitFlash = Math.min(1.0F, this.p.hitFlash + 0.3F * var5);
      } else if (this.p.ringed) {
         this.p.damageRing(Math.atan2(var3, var2), 0.4F * var5, 2.0F + var5);
      }

      this.flash(var2, var3, 0.08F + 0.12F * var5, var1.style == 2 ? -2099969 : (var1.cat == 6 ? var1.color : -12150));
      this.sparks(var2, var3, 8 + Math.round(var5 * 10.0F), var1.style == 2 ? -1509633 : this.p.avgColor(), 0.6F + var5 * 0.4F);
      SolarFx.Ring var6 = new SolarFx.Ring();
      var6.x = var2;
      var6.y = var3;
      var6.max = 0.6F + var5 * 0.2F;
      var6.size = 0.12F + var5 * 0.25F;
      var6.color = var1.color;
      this.rings.add(var6);
      Sound.play(var1.sound, Math.max(0.5F, 1.2F - var5 * 0.25F), Math.min(1.0F, 0.35F + var5 * 0.25F));
   }

   private void flash(float var1, float var2, float var3, int var4) {
      SolarFx.Part var5 = this.part();
      var5.x = var1;
      var5.y = var2;
      var5.max = var5.life = 0.45F;
      var5.size = var3;
      var5.color = var4;
      var5.sprite = 0;
      var5.glow = true;
   }

   private void sparks(float var1, float var2, int var3, int var4, float var5) {
      for (int var6 = 0; var6 < var3; var6++) {
         SolarFx.Part var7 = this.part();
         double var8 = this.rnd.nextDouble() * Math.PI * 2.0;
         double var10 = (0.2 + this.rnd.nextDouble()) * var5;
         var7.x = var1;
         var7.y = var2;
         var7.vx = (float)(Math.cos(var8) * var10);
         var7.vy = (float)(Math.sin(var8) * var10);
         var7.max = var7.life = 0.4F + this.rnd.nextFloat() * 0.6F;
         var7.size = 0.008F + this.rnd.nextFloat() * 0.018F;
         var7.color = var4;
         var7.sprite = this.rnd.nextFloat() < 0.5F ? 1 : 2;
         var7.glow = var7.sprite == 2;
      }
   }

   private void trail(SolarFx.Shot var1) {
      SolarFx.Part var2 = this.part();
      var2.x = var1.x + (this.rnd.nextFloat() - 0.5F) * 0.02F;
      var2.y = var1.y + (this.rnd.nextFloat() - 0.5F) * 0.02F;
      var2.max = var2.life = 0.35F + this.rnd.nextFloat() * 0.3F;
      var2.size = 0.02F + Math.min(0.08F, var1.t.r / 1500.0F);
      var2.color = var1.t.style == 2 ? -4200193 : (var1.t.cat == 6 ? -1 : -26038);
      var2.sprite = 2;
      var2.glow = true;
   }

   private SolarFx.Part part() {
      if (this.parts.size() > 1400) {
         this.parts.remove(0);
      }

      SolarFx.Part var1 = new SolarFx.Part();
      this.parts.add(var1);
      return var1;
   }

   void explode() {
      if (!this.p.destroyed) {
         this.p.destroyed = true;
         this.p.destroyedT = 0.0F;
         this.p.gAlive = 0.0;
         int var1 = this.p.avgColor();

         for (int var2 = 0; var2 < 260; var2++) {
            SolarFx.Part var3 = this.part();
            double var4 = this.rnd.nextDouble() * Math.PI * 2.0;
            double var6 = Math.sqrt(this.rnd.nextDouble()) * this.p.scale;
            double var8 = 0.3 + this.rnd.nextDouble() * 1.6;
            var3.x = (float)(Math.cos(var4) * var6);
            var3.y = (float)(Math.sin(var4) * var6);
            var3.vx = (float)(Math.cos(var4) * var8);
            var3.vy = (float)(Math.sin(var4) * var8);
            var3.max = var3.life = 3.0F + this.rnd.nextFloat() * 4.0F;
            var3.size = 0.02F + this.rnd.nextFloat() * this.rnd.nextFloat() * 0.18F;
            var3.color = this.rnd.nextFloat() < 0.25F ? -30166 : var1;
            var3.sprite = this.rnd.nextFloat() < 0.25F ? 2 : 1;
            var3.glow = var3.sprite == 2;
         }

         for (int var10 = 0; var10 < 3; var10++) {
            SolarFx.Ring var11 = new SolarFx.Ring();
            var11.max = 2.5F + var10;
            var11.size = 3.0F + var10 * 1.5F;
            var11.color = var10 == 0 ? -8032 : -30134;
            this.rings.add(var11);
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

   static int hue(float var0) {
      return 0xFF000000 | Color.HSBtoRGB(var0 * 0.4F % 1.0F, 0.8F, 1.0F) & 16777215;
   }

   private static Gx.Img glowImg() {
      return Gx.get("solar:fx:flare", 0, () -> SolarTex.flare(128));
   }

   private static Gx.Img softImg() {
      return Gx.get("solar:fx:soft", 0, () -> SolarTex.softRing(256));
   }

   private static Gx.Img rockImg(int var0) {
      int var1 = var0 & 3;
      return Gx.get("solar:fx:rock" + var1, 0, () -> SolarTex.rock(64, 11 + var1, -1));
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

   static Gx.Img shipImg(int var0) {
      if (var0 == 5) {
         return Gx.painted("solar:fx:sat", 96, 96, var0x -> SolarTex.satellite(var0x, 96, -38342));
      } else {
         return var0 == 6
            ? Gx.painted("solar:fx:sleigh", 128, 96, SolarFx::paintSleigh)
            : Gx.painted("solar:fx:ufo" + var0, 160, 160, var1 -> SolarTex.ufo(var1, 160, var0, -9633892));
      }
   }

   static Gx.Img projImg(SolarTools.Tool var0) {
      if (var0.sprite >= 0 && var0.cat == 6) {
         int var1 = var0.sprite;
         return Gx.painted("solar:fx:fun" + var1, 128, 128, var2 -> SolarTex.fun(var2, 128, var1, var0.color));
      } else if (var0.sprite == -2) {
         return Gx.painted("solar:fx:walker", 128, 128, var0x -> SolarTex.walker(var0x, 128, -7667860));
      } else if (var0.sprite == -3) {
         return Gx.painted("solar:fx:gift", 96, 96, SolarFx::paintGift);
      } else {
         return var0.sprite == -4 ? Gx.painted("solar:fx:snowman", 96, 96, SolarFx::paintSnowman) : null;
      }
   }

   void drawBack(float var1, float var2, float var3) {
      for (SolarFx.Hole var5 : this.holes) {
         float var6 = Math.min(1.0F, var5.age / 1.2F) * (var5.age > var5.t.dur ? Math.max(0.0F, 1.0F - (var5.age - var5.t.dur) / 2.0F) : 1.0F);
         float var7 = var3 * 0.9F * var5.t.width * var6;
         boolean var8 = var5.t.color == -1;
         float var9 = var1 + var5.x * var3;
         float var10 = var2 + var5.y * var3;
         Gx.Img var11 = accImg();
         if (var11 != null) {
            Gx.image(
               var11, Math.round(var9 - var7 * 2.0F), Math.round(var10 - var7 * 0.75F), Math.round(var7 * 4.0F), Math.round(var7 * 1.5F), var8 ? -1 : -20368
            );
         }

         Gx.Img var12 = swirlImg();
         if (var12 != null) {
            Gx.image(
               var12,
               Math.round(var9 - var7 * 1.3F),
               Math.round(var10 - var7 * 1.3F),
               Math.round(var7 * 2.6F),
               Math.round(var7 * 2.6F),
               var8 ? -855638017 : -1716489985
            );
         }

         Gx.glow(Math.round(var9), Math.round(var10), Math.round(var7 * 1.6F), var8 ? -1996488705 : 1728023098);
         if (!var8) {
            Gx.circle(Math.round(var9), Math.round(var10), Math.max(2, Math.round(var7 * 0.42F)), -16777216);
         } else {
            Gx.circle(Math.round(var9), Math.round(var10), Math.max(2, Math.round(var7 * 0.3F)), -1);
         }
      }
   }

   void drawFront(float var1, float var2, float var3) {
      Gx.Img var4 = glowImg();
      Gx.Img var5 = softImg();

      for (SolarFx.Ring var7 : this.rings) {
         float var8 = var7.age / var7.max;
         float var9 = var3 * var7.size * (0.2F + var8);
         if (var5 != null) {
            Gx.image(
               var5,
               Math.round(var1 + var7.x * var3 - var9),
               Math.round(var2 + var7.y * var3 - var9),
               Math.round(var9 * 2.0F),
               Math.round(var9 * 2.0F),
               Gx.withAlpha(var7.color, (1.0F - var8) * 0.9F)
            );
         }
      }

      for (SolarFx.Shot var20 : this.shots) {
         if (!(var20.delay > 0.0F)) {
            float var25 = var1 + var20.x * var3;
            float var30 = var2 + var20.y * var3;
            float var10 = var3 * Math.max(0.035F, Math.min(0.5F, var20.t.r / 700.0F)) * this.sizeMul;
            Gx.Img var11 = projImg(var20.t);
            if (var11 != null) {
               int var43 = Math.round(var10 * 2.2F);
               Gx.image(var11, Math.round(var25 - var43 / 2.0F), Math.round(var30 - var43 / 2.0F), var43, var43, -1);
            } else {
               if (var4 != null) {
                  int var12 = Math.round(var10 * 4.0F);
                  Gx.image(
                     var4,
                     Math.round(var25 - var12 / 2.0F),
                     Math.round(var30 - var12 / 2.0F),
                     var12,
                     var12,
                     Gx.withAlpha(var20.t.cat != 0 && var20.t.cat != 5 ? -26038 : var20.t.color, 0.8F)
                  );
               }

               boolean var42 = var20.t.cat == 4 || var20.t.cat == 5 || var20.t.style == 3;
               if (var42) {
                  Gx.circle(Math.round(var25), Math.round(var30), Math.max(2, Math.round(var10 * 0.8F)), var20.t.color);
               } else {
                  Gx.Img var13 = rockImg(var20.t.name.hashCode());
                  if (var13 != null) {
                     int var14 = Math.round(var10 * 2.0F);
                     Gx.image(var13, Math.round(var25 - var14 / 2.0F), Math.round(var30 - var14 / 2.0F), var14, var14, var20.t.color);
                  }
               }
            }
         }
      }

      for (SolarFx.Ship var21 : this.ships) {
         float var26 = var21.rad;
         float var31 = (float)Math.sin(var21.bob * 2.0F) * 0.03F;
         float var35 = var1 + (float)Math.cos(var21.ang) * var26 * var3;
         float var39 = var2 + ((float)Math.sin(var21.ang) * var26 + var31) * var3;
         if (var21.beamT > 0.0F) {
            beamLine(
               var35, var39, var1 + var21.bx * var3, var2 + var21.by * var3, var3 * 0.012F * var21.t.width * (var21.t.sprite == 1 ? 2.5F : 1.0F), var21.t.color
            );
         }

         int var44 = Math.round(var3 * (var21.t.sprite == 1 ? 0.55F : (var21.t.sprite == 5 ? 0.09F : 0.16F)));
         Gx.Img var47 = shipImg(var21.t.sprite);
         if (var47 != null) {
            Gx.image(var47, Math.round(var35 - var44 / 2.0F), Math.round(var39 - var44 / 2.0F), var44, var44 * (var21.t.sprite == 6 ? 3 : 4) / 4, -1);
         }

         if (var4 != null) {
            Gx.image(
               var4,
               Math.round(var35 - var44 * 0.4F),
               Math.round(var39 + var44 * 0.1F),
               Math.round(var44 * 0.8F),
               Math.round(var44 * 0.5F),
               Gx.withAlpha(var21.t.color, 0.35F)
            );
         }
      }

      if (this.beamOn && this.beam != null && !this.p.destroyed) {
         float var17 = var1 - var3 * 2.6F;
         float var22 = var2 - var3 * 1.7F;
         int var27 = this.beam.style == 19 ? hue(this.time) : this.beam.color;

         for (int var32 = 0; var32 < this.beam.n; var32++) {
            float var36 = this.beam.n == 1 ? 0.0F : (var32 - (this.beam.n - 1) / 2.0F) * 0.12F;
            beamLine(
               var17 + var36 * var3 * 3.0F,
               var22,
               var1 + (this.beamX + var36) * var3,
               var2 + this.beamY * var3,
               var3 * 0.014F * this.beam.width * this.sizeMul,
               var27
            );
         }
      }

      for (SolarFx.Area var23 : this.areas) {
         double[] var28 = this.p.project(var23.lon, var23.lat);
         if (!(var28[2] <= 0.0) && var4 != null) {
            float var33 = Math.min(1.0F, var23.age / var23.t.dur);
            float var37 = var3 * (0.1F + 0.3F * var33) * (var23.t.r / 80.0F) * this.sizeMul;
            float var40 = var1 + (float)var28[0] * this.p.scale * var3;
            float var45 = var2 + (float)var28[1] * this.p.scale * var3;
            Gx.image(
               var4,
               Math.round(var40 - var37),
               Math.round(var45 - var37),
               Math.round(var37 * 2.0F),
               Math.round(var37 * 2.0F),
               Gx.withAlpha(var23.t.color, 0.5F * (1.0F - var33 * 0.5F))
            );
         }
      }

      for (SolarFx.Part var24 : this.parts) {
         float var29 = var24.life / var24.max;
         float var34 = var1 + var24.x * var3;
         float var38 = var2 + var24.y * var3;
         float var41 = Math.max(1.0F, var24.size * var3 * (var24.glow ? 0.6F + 0.6F * var29 : 1.0F));
         if (var24.sprite == 0 && var4 != null) {
            Gx.image(
               var4,
               Math.round(var34 - var41),
               Math.round(var38 - var41),
               Math.round(var41 * 2.0F),
               Math.round(var41 * 2.0F),
               Gx.withAlpha(var24.color, Math.min(1.0F, var29 * 1.5F))
            );
         } else if (var24.sprite == 2 && var4 != null) {
            Gx.image(
               var4,
               Math.round(var34 - var41 * 1.5F),
               Math.round(var38 - var41 * 1.5F),
               Math.round(var41 * 3.0F),
               Math.round(var41 * 3.0F),
               Gx.withAlpha(var24.color, var29)
            );
         } else {
            Gx.Img var46 = rockImg((int)(var24.size * 1000.0F));
            if (var46 != null) {
               Gx.image(
                  var46,
                  Math.round(var34 - var41),
                  Math.round(var38 - var41),
                  Math.round(var41 * 2.0F),
                  Math.round(var41 * 2.0F),
                  Gx.withAlpha(var24.color, Math.min(1.0F, var29 * 2.0F))
               );
            }
         }
      }
   }

   static void beamLine(float var0, float var1, float var2, float var3, float var4, int var5) {
      Gx.Img var6 = glowImg();
      if (var6 != null) {
         float var7 = var2 - var0;
         float var8 = var3 - var1;
         float var9 = (float)Math.hypot(var7, var8);
         float var10 = Math.max(2.0F, var4 * 1.2F);
         int var11 = Math.min(260, Math.max(2, Math.round(var9 / var10)));
         int var12 = Math.max(3, Math.round(var4 * 5.0F));
         int var13 = Math.max(2, Math.round(var4 * 1.6F));

         for (int var14 = 0; var14 <= var11; var14++) {
            float var15 = (float)var14 / var11;
            int var16 = Math.round(var0 + var7 * var15);
            int var17 = Math.round(var1 + var8 * var15);
            Gx.image(var6, var16 - var12 / 2, var17 - var12 / 2, var12, var12, Gx.withAlpha(var5, 0.55F));
            Gx.image(var6, var16 - var13 / 2, var17 - var13 / 2, var13, var13, -419430401);
         }

         int var18 = Math.round(var4 * 14.0F);
         Gx.image(var6, Math.round(var2 - var18 / 2.0F), Math.round(var3 - var18 / 2.0F), var18, var18, Gx.withAlpha(var5, 0.9F));
         Gx.image(var6, Math.round(var0 - var18 / 3.0F), Math.round(var1 - var18 / 3.0F), var18 * 2 / 3, var18 * 2 / 3, Gx.withAlpha(var5, 0.8F));
      }
   }

   private static void paintGift(Graphics2D var0) {
      var0.setColor(new Color(14033464));
      var0.fill(new Double(14.0, 30.0, 68.0, 58.0, 8.0, 8.0));
      var0.setColor(new Color(11735583));
      var0.fill(new Double(10.0, 24.0, 76.0, 16.0, 6.0, 6.0));
      var0.setColor(new Color(16040274));
      var0.fillRect(42, 24, 12, 64);
      var0.fillRect(10, 29, 76, 6);
      var0.fill(new java.awt.geom.Ellipse2D.Double(26.0, 8.0, 22.0, 18.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(48.0, 8.0, 22.0, 18.0));
   }

   private static void paintSnowman(Graphics2D var0) {
      var0.setColor(new Color(16054527));
      var0.fill(new java.awt.geom.Ellipse2D.Double(20.0, 44.0, 56.0, 50.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(30.0, 18.0, 36.0, 34.0));
      var0.setColor(new Color(1710618));
      var0.fillRect(32, 6, 32, 6);
      var0.fillRect(38, -8, 20, 16);
      var0.fill(new java.awt.geom.Ellipse2D.Double(40.0, 30.0, 5.0, 5.0));
      var0.fill(new java.awt.geom.Ellipse2D.Double(52.0, 30.0, 5.0, 5.0));
      var0.setColor(new Color(16747050));
      var0.fill(new Polygon(new int[]{47, 62, 47}, new int[]{36, 40, 42}, 3));
      var0.setColor(new Color(14886459));
      var0.fillRect(30, 48, 36, 7);
   }

   private static void paintSleigh(Graphics2D var0) {
      var0.setColor(new Color(11569754));

      for (int var1 = 0; var1 < 3; var1++) {
         var0.fill(new java.awt.geom.Ellipse2D.Double(4 + var1 * 22, 30.0, 22.0, 12.0));
         var0.fillRect(8 + var1 * 22, 40, 3, 12);
         var0.fillRect(20 + var1 * 22, 40, 3, 12);
      }

      var0.setColor(new Color(14886459));
      var0.fill(new java.awt.geom.Ellipse2D.Double(24.0, 32.0, 5.0, 5.0));
      var0.setColor(new Color(11867436));
      var0.fill(new Double(74.0, 30.0, 46.0, 26.0, 10.0, 10.0));
      var0.setColor(new Color(16040274));
      var0.fillRect(70, 58, 54, 4);
      var0.setColor(new Color(8018490));
      var0.fill(new java.awt.geom.Ellipse2D.Double(92.0, 12.0, 26.0, 26.0));
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
