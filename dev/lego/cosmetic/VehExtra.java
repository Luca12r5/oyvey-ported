package dev.lego.cosmetic;

import dev.lego.core.Mc;

final class VehExtra {
   static final float BOBBY_HIP = 3.2F;
   static final float LIMO_HIP = 5.5F;
   static final float F1_HIP = 3.4F;
   static final float MONSTER_HIP = 9.0F;
   static final float QUAD_SEAT = 10.0F;
   static final float SNOW_SEAT = 9.8F;
   static final float TRIKE_SEAT = 9.0F;
   static final float LONG_TOP = 2.3F;
   static final float CARPET_TOP = 2.0F;
   static final float SURF_TOP = 2.5F;
   static final float SEGWAY_TOP = 2.8F;
   static final float[] LIMO_HAND = new float[]{2.9F, 10.4F, 7.8F};
   static final float[] F1_HAND = new float[]{2.4F, 7.0F, 8.0F};
   static final float[] MONSTER_HAND = new float[]{3.2F, 14.5F, 8.0F};
   static final float[] QUAD_HAND = new float[]{4.0F, 11.5F, 9.0F};
   static final float[] SNOW_HAND = new float[]{4.0F, 11.5F, 9.0F};
   static final float[] SEGWAY_HAND = new float[]{2.5F, 18.0F, 4.0F};
   static final float[] TRIKE_HAND = new float[]{5.0F, 11.0F, 8.0F};
   static final float BOBBY_WR = 2.4F;
   static final float MT_WR = 7.5F;
   static final float MT_WX = 11.0F;
   static final float F1_WR = 3.8F;
   static final float QUAD_WR = 3.6F;

   private VehExtra() {
   }

   static boolean isNight() {
      long t = Math.floorMod(Mc.timeOfDay(), 24000L);
      return t >= 13000L && t < 23000L;
   }

   private static void carXform(G g, Cos.A a) {
      g.translate(0.0F, VehGeo.sin(a.time * 38.0F) * 0.04F * (1.0F - a.move * 0.5F), 0.0F);
      g.translate(0.0F, 4.6F, 0.0F);
      g.rotZ(-a.lean * 2.2F);
      g.translate(0.0F, -4.6F, 0.0F);
   }

   static void police(G g, Cos.A a) {
      VehCar.car(g, a, 15921906, 2776031);
      g.push();
      carXform(g, a);
      VehGeo.rbox(g, "veh_gun", -3.2F, 11.2F, -2.5F, 3.2F, 11.8F, 2.5F, 0.2F);
      g.glow(true);
      boolean blue = (int)(a.time * 4.0F) % 2 == 0;
      String bl = "veh_glow:2A5BFF";
      String rd = "veh_glow:FF2A2A";
      VehGeo.ellipsoid(g, blue ? bl : rd, -1.8F, 12.2F, 0.0F, 0.7F, 0.5F, 0.7F, 8, 5);
      VehGeo.ellipsoid(g, blue ? rd : bl, 1.8F, 12.2F, 0.0F, 0.7F, 0.5F, 0.7F, 8, 5);
      g.glow(false);
      g.pop();
   }

   static void taxi(G g, Cos.A a) {
      VehCar.car(g, a, 16764163, 1710622);
      g.push();
      carXform(g, a);
      VehGeo.rbox(g, "veh_c:FFCD03", -1.8F, 11.3F, -0.5F, 1.8F, 12.5F, 1.5F, 0.4F);
      g.glow(true);
      VehGeo.box6(g, "veh_flat:0A0A0F", new String[]{null, null, null, null, "veh_glow:FFEE88", null}, -1.6F, 12.45F, -0.3F, 1.6F, 12.6F, 1.3F);
      g.glow(false);
      g.pop();
   }

   static void bobby(G g, Cos.A a) {
      String red = "veh_c:D8141B";
      String blk = "veh_flat:1C1D22";
      String yel = "veh_c:FFCD03";
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 40.0F) * 0.03F, 0.0F);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            g.push();
            g.translate(sx * 5.5F, 2.4F, sz > 0 ? 7.0F : -7.0F);
            if (sz > 0) {
               g.rotY(a.lean * 20.0F);
            }

            VehCar.carWheel(g, a.wheel * 1.5F, sx < 0, 2.4F, 2.0F, 1.5F, 0, null);
            g.pop();
         }
      }

      VehGeo.rbox(g, red, -5.0F, 3.0F, -8.0F, 5.0F, 5.5F, 10.0F, 1.2F);
      VehGeo.rbox(g, red, -4.5F, 5.0F, -6.0F, 4.5F, 8.0F, 4.0F, 1.0F);
      VehGeo.rbox(g, blk, -3.5F, 4.5F, -5.0F, 3.5F, 15.2F, 1.0F, 0.6F);
      VehGeo.tube(g, yel, VehGeo.p(-4.5F, 8.2F, 7.0F), VehGeo.p(4.5F, 8.2F, 7.0F), 0.5F, 0.5F, 8, true);
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 3.5F, 5.2F, 10.2F, 0.7F, 0.5F, 0.3F, 8, 4);
      }

      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   static void monster(G g, Cos.A a) {
      String P = "veh_c:2FA58C";
      String gun = "veh_gun";
      String cr = "veh_chrome";
      String carbon = "veh_carbon";
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 34.0F) * 0.08F * (1.0F - a.move * 0.4F), 0.0F);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            g.push();
            g.translate(sx * 11.0F, 7.5F, sz > 0 ? 16.0F : -16.0F);
            if (sz > 0) {
               g.rotY(a.lean * 14.0F);
            }

            VehCar.carWheel(g, a.wheel * 0.7F, sx < 0, 7.5F, 4.5F, 4.8F, 8, null);
            g.pop();
         }
      }

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            float wz = sz > 0 ? 16.0F : -16.0F;
            VehGeo.tube(g, gun, VehGeo.p(sx * 9.0F, 6.5F, wz - 2.0F), VehGeo.p(sx * 11.0F, 6.5F, wz), 0.55F, 0.45F, 6, false);
            VehGeo.tube(g, gun, VehGeo.p(sx * 9.0F, 6.5F, wz + 2.0F), VehGeo.p(sx * 11.0F, 8.5F, wz), 0.55F, 0.45F, 6, false);
            VehGeo.tube(g, cr, VehGeo.p(sx * 9.5F, 8.0F, wz), VehGeo.p(sx * 11.0F * 0.95F, 10.5F, wz), 0.35F, 0.25F, 6, true);
            VehGeo.tube(g, "veh_c:D81E1E", VehGeo.p(sx * 9.5F, 8.0F, wz), VehGeo.p(sx * 19.95F / 2.0F, 9.25F, wz), 0.2F, 0.2F, 6, true);
         }
      }

      g.translate(0.0F, 10.5F, 0.0F);
      g.rotZ(-a.lean * 2.5F);
      g.translate(0.0F, -10.5F, 0.0F);
      VehGeo.rbox(g, P, -9.0F, 6.0F, -18.0F, 9.0F, 10.0F, 20.0F, 1.2F);
      VehGeo.rbox(g, P, -8.5F, 9.5F, -16.0F, 8.5F, 16.0F, 14.0F, 1.0F);
      VehGeo.rbox(g, gun, -9.3F, 4.5F, -16.0F, 9.3F, 6.5F, 18.0F, 0.3F);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            float wz = sz > 0 ? 16.0F : -16.0F;
            VehGeo.rbox(g, P, sx < 0 ? -10.5F : 7.5F, 6.0F, wz - 5.0F, sx < 0 ? -7.5F : 10.5F, 9.0F, wz + 5.0F, 0.6F);
         }
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.box(g, "veh_glass", s < 0 ? -8.7F : 8.2F, 10.5F, -10.0F, s < 0 ? -8.2F : 8.7F, 14.5F, 10.0F);
      }

      VehGeo.box(g, "veh_glass", -5.5F, 10.5F, 13.5F, 5.5F, 14.5F, 14.2F);
      VehGeo.box(g, "veh_glass", -5.5F, 10.5F, -16.3F, 5.5F, 14.5F, -15.5F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(
            g, gun, new float[][]{{s * 8.5F, 10.0F, -15.0F}, {s * 8.5F, 17.5F, -14.0F}, {s * 8.5F, 18.0F, 13.0F}, {s * 8.5F, 10.0F, 14.0F}}, 0.35F, 6, false
         );
         VehGeo.tube(g, gun, VehGeo.p(s * 8.5F, 14.0F, -14.5F), VehGeo.p(s * 8.5F, 14.0F, 13.5F), 0.25F, 0.25F, 6, false);
      }

      VehGeo.tube(g, gun, VehGeo.p(-8.5F, 17.5F, -5.0F), VehGeo.p(8.5F, 17.5F, -5.0F), 0.3F, 0.3F, 6, false);
      VehGeo.tube(g, gun, VehGeo.p(-8.5F, 17.8F, 5.0F), VehGeo.p(8.5F, 17.8F, 5.0F), 0.3F, 0.3F, 6, false);
      VehGeo.rbox(g, gun, -7.0F, 17.5F, -2.0F, 7.0F, 18.5F, 8.0F, 0.3F);
      VehGeo.tube(g, cr, VehGeo.p(-8.0F, 6.5F, 20.0F), VehGeo.p(-8.0F, 12.0F, 20.5F), 0.4F, 0.4F, 8, true);
      VehGeo.tube(g, cr, VehGeo.p(8.0F, 6.5F, 20.0F), VehGeo.p(8.0F, 12.0F, 20.5F), 0.4F, 0.4F, 8, true);
      VehGeo.tube(g, cr, VehGeo.p(-8.0F, 12.0F, 20.5F), VehGeo.p(8.0F, 12.0F, 20.5F), 0.35F, 0.35F, 8, false);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -5.0F, 7.0F, -5.0F, 5.0F, 9.0F, 2.0F, 0.5F);
      VehGeo.rbox(g, "veh_interior", -7.0F, 11.0F, 6.0F, 7.0F, 14.5F, 9.0F, 0.4F);
      VehGeo.box6(g, gun, new String[]{"veh_grille", null, null, null, null, null}, -5.5F, 6.5F, 19.5F, 5.5F, 10.0F, 20.5F);
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 4 - 2.0F, 7.5F, 20.3F, s * 4 + 2.0F, 10.0F, 20.8F, 0.2F);
         VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", s * 4 - 1.5F, 7.5F, -18.5F, s * 4 + 1.5F, 9.5F, -17.8F, 0.2F);
      }

      for (int i = -3; i <= 3; i++) {
         VehGeo.rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", i * 1.8F - 0.6F, 18.2F, 2.0F, i * 1.8F + 0.6F, 18.5F, 6.0F, 0.08F);
      }

      if (night) {
         g.glow(false);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, cr, VehGeo.p(s * 8.0F, 10.0F, -17.0F), VehGeo.p(s * 8.0F, 18.0F, -17.0F), 0.65F, 0.55F, 8, true);
      }

      g.pop();
   }

   static void f1(G g, Cos.A a) {
      String P = "veh_c:E3242B";
      String cr = "veh_chrome";
      String gun = "veh_gun";
      String carbon = "veh_carbon";
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 42.0F) * 0.03F * (1.0F - a.move * 0.5F), 0.0F);

      for (int sx = -1; sx <= 1; sx += 2) {
         g.push();
         g.translate(sx * 12.5F, 3.8F, 14.0F);
         g.rotY(a.lean * 16.0F);
         VehCar.carWheel(g, a.wheel, sx < 0, 3.8F, 2.8F, 2.4F, 5, null);
         g.pop();
         g.push();
         g.translate(sx * 12.0F, 4.3F, -14.0F);
         VehCar.carWheel(g, a.wheel, sx < 0, 4.3F, 3.5F, 2.8F, 5, null);
         g.pop();
      }

      g.translate(0.0F, 3.8F, 0.0F);
      g.rotZ(-a.lean * 3.0F);
      g.translate(0.0F, -3.8F, 0.0F);
      VehGeo.loft(g, VehGeo.range(-20.0F, 22.0F, 2.5F), z -> {
         float t = (z + 20.0F) / 42.0F;
         float w = VehGeo.lerp(1.5F, 3.8F, VehGeo.smooth(0.0F, 0.3F, t)) - VehGeo.smooth(0.7F, 1.0F, t) * 1.8F;
         float h = VehGeo.lerp(4.0F, 6.5F, VehGeo.smooth(0.0F, 0.35F, t)) - VehGeo.smooth(0.6F, 1.0F, t) * 2.5F;
         return new float[][]{{-w, 1.8F}, {w, 1.8F}, {w, h}, {0.0F, h + 0.8F}, {-w, h}};
      }, (k, j, z, pa, pb) -> P, true, true, 7.0F, 1.5F);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -2.2F, 2.0F, -6.0F, 2.2F, 15.4F, -1.0F, 0.4F);
      VehGeo.rbox(g, "veh_interior", -3.0F, 5.5F, 1.0F, 3.0F, 7.0F, 5.0F, 0.3F);
      VehGeo.tube(g, gun, VehGeo.p(0.0F, 3.5F, 6.0F), VehGeo.p(0.0F, 7.0F, 8.0F), 0.3F, 0.3F, 6, false);
      g.push();
      g.translate(0.0F, 7.0F, 8.0F);
      g.rotX(-55.0F);
      g.rotY(-a.lean * 80.0F);
      VehGeo.torus(g, "veh_flat:15151A", 2.4F, 0.3F, 14, 5);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.box(g, carbon, s * 3.0F, 2.0F, 12.0F, s * 12.0F, 2.8F, 15.0F);
         VehGeo.box(g, carbon, s * 3.0F, 2.0F, -12.0F, s * 11.5F, 2.8F, -15.0F);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, carbon, VehGeo.p(s * 6, 6.0F, -19.0F), VehGeo.p(s * 6, 10.5F, -21.0F), 0.5F, 1.5F, 0.5F, 1.0F, VehGeo.p(0.0F, 0.0F, 1.0F));
      }

      g.push();
      g.translate(0.0F, 10.8F, -21.0F);
      g.rotY(90.0F);
      g.rotZ(-6.0F);
      VehGeo.box(g, carbon, -10.5F, -0.1F, -1.5F, 10.5F, 0.1F, 1.0F);
      g.pop();
      VehGeo.tube(g, cr, VehGeo.p(-3.0F, 3.2F, -19.0F), VehGeo.p(3.0F, 3.2F, -19.0F), 0.9F, 0.9F, 10, true);
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 1.5F, 4.5F, 22.2F, 0.8F, 0.5F, 0.3F, 8, 4);
      }

      VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -1.8F, 5.0F, -20.3F, 1.8F, 6.5F, -19.5F, 0.15F);
      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   static void quad(G g, Cos.A a) {
      String P = "veh_c:D81E1E";
      String gun = "veh_gun";
      String alu = "veh_alu";
      float spin = a.wheel * 0.9F;
      g.push();
      g.rotZ(-a.lean * 10.0F);

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            g.push();
            g.translate(sx * 7.5F, 3.6F, sz > 0 ? 8.0F : -8.5F);
            if (sz > 0) {
               g.rotY(a.lean * 16.0F);
            }

            VehCar.carWheel(g, spin, sx < 0, 3.6F, 3.0F, 2.2F, 0, null);
            g.pop();
         }
      }

      VehGeo.rbox(g, P, -5.0F, 4.5F, -10.0F, 5.0F, 9.5F, 10.0F, 1.0F);
      VehGeo.rbox(g, gun, -5.3F, 3.5F, -8.0F, 5.3F, 5.0F, 8.0F, 0.3F);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -3.2F, 8.8F, -6.0F, 3.2F, 10.0F, 1.0F, 0.5F);
      VehGeo.rbox(g, P, -4.0F, 8.0F, 7.0F, 4.0F, 12.5F, 11.0F, 0.8F);
      VehGeo.tube(g, gun, VehGeo.p(0.0F, 10.0F, 11.0F), VehGeo.p(0.0F, 11.5F, 9.0F), 0.4F, 0.4F, 6, false);
      VehGeo.tube(g, alu, VehGeo.p(-4.5F, 11.5F, 9.0F), VehGeo.p(4.5F, 11.5F, 9.0F), 0.35F, 0.35F, 8, true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 3.5F, 11.5F, 9.0F), VehGeo.p(s * 5.2F, 11.5F, 9.0F), 0.55F, 0.55F, 8, true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, gun, VehGeo.p(s * 5.0F, 5.0F, -8.0F), VehGeo.p(s * 7.5F, 3.6F, -8.5F), 0.5F, 0.4F, 6, false);
         VehGeo.tube(g, gun, VehGeo.p(s * 5.0F, 5.0F, 8.0F), VehGeo.p(s * 7.5F, 3.6F, 8.0F), 0.5F, 0.4F, 6, false);
      }

      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 2.5F, 11.5F, 11.5F, 0.8F, 0.5F, 0.3F, 8, 4);
      }

      VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -2.0F, 8.5F, -10.5F, 2.0F, 9.8F, -9.8F, 0.15F);
      if (night) {
         g.glow(false);
      }

      VehGeo.rbox(g, gun, 3.5F, 4.0F, -9.5F, 5.5F, 6.5F, -6.0F, 0.3F);
      VehGeo.pipe(g, "veh_chrome", new float[][]{{5.0F, 5.5F, -9.5F}, {5.0F, 5.5F, -11.0F}, {4.0F, 4.5F, -12.0F}}, 0.4F, 6, true);
      g.pop();
   }

   static void limo(G g, Cos.A a) {
      VehCar.car(g, a, 1710622, 15263976);
      g.push();
      carXform(g, a);
      VehGeo.rbox(g, "veh_c:1A1A1E", -12.0F, 2.5F, -38.0F, 12.0F, 11.5F, -26.0F, 1.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.box(g, "veh_tint", s < 0 ? -12.2F : 11.7F, 7.5F, -36.0F, s < 0 ? -11.7F : 12.2F, 10.5F, -28.0F);
         g.push();
         g.translate(sx(s, 10.5F), 2.5F, -32.0F);
         VehCar.carWheel(g, a.wheel, s < 0, 4.6F, 3.4F, 3.2F, 8, null);
         g.pop();
      }

      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", s * 3 - 1, 7.0F, -38.5F, s * 3 + 1, 9.0F, -37.8F, 0.15F);
      }

      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   private static float sx(int s, float v) {
      return s < 0 ? -v : v;
   }

   static void longboard(G g, Cos.A a) {
      g.push();
      g.rotZ(-a.lean * 4.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehBoard.truck(g, a, s * 10.5F, s * a.lean * 7.0F);
      }

      float y = 2.0F;
      VehGeo.rbox(g, "veh_deckart", -4.4F, y, -15.0F, 4.4F, y + 0.5F, 15.0F, 0.4F);
      VehGeo.box(g, "veh_griptape", -4.0F, y + 0.45F, -13.0F, 4.0F, y + 2.3F - y, 13.0F);
      g.pop();
   }

   static void surfboard(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 1.5F + VehGeo.sin(a.time * 1.8) * 0.4F, 0.0F);
      g.rotZ(-a.lean * 5.0F);
      g.rotX(-a.move * 3.0F);
      VehGeo.loft(g, VehGeo.range(-14.0F, 14.0F, 1.2F), zx -> {
         float az = Math.abs(zx);
         float w = az <= 8.0F ? 3.8F : 3.8F * (float)Math.sqrt(Math.max(0.01F, 1.0F - (az - 8.0F) / 6.0F * ((az - 8.0F) / 6.0F)));
         return new float[][]{{-w, 0.0F}, {0.0F, -0.3F}, {w, 0.0F}, {0.0F, 0.5F}};
      }, (k, j, zx, pa, pb) -> j < 2 ? "veh_c:F2F2F2" : "veh_c:24C8E0", true, true, 0.5F, -0.3F);

      for (int i = 0; i < 3; i++) {
         float z = -12.0F + i * 1.5F;
         VehGeo.box(g, "veh_c:24C8E0", -0.08F, -0.3F, z, 0.08F, -1.2F, z + 0.6F);
      }

      g.pop();
   }

   static void carpet(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 1.5F + VehGeo.sin(a.time * 2.0) * 0.3F, 0.0F);
      g.rotZ(-a.lean * 6.0F);
      g.rotX(-a.move * 5.0F);

      for (int zz = -6; zz <= 6; zz += 2) {
         for (int xx = -5; xx <= 5; xx += 2) {
            float wave = VehGeo.sin(a.time * 3.0F + xx * 0.3F + zz * 0.4F) * 0.25F;
            boolean edge = Math.abs(xx) >= 4 || Math.abs(zz) >= 5;
            boolean corner = Math.abs(xx) >= 4 && Math.abs(zz) >= 5;
            String col = corner ? "veh_c:FFD700" : (edge ? "veh_c:8B1A1A" : ((xx + zz) % 4 == 0 ? "veh_c:C62828" : "veh_c:FFB300"));
            VehGeo.box(g, col, xx - 1, wave - 0.08F, zz - 1, xx + 1, wave + 0.08F, zz + 1);
         }
      }

      for (int s = -1; s <= 1; s += 2) {
         float z = s * 7;

         for (int x = -4; x <= 4; x += 2) {
            float swing = VehGeo.sin(a.time * 4.0F + x * 0.5F) * 0.5F;
            VehGeo.box(g, "veh_c:FFD700", x - 0.15F, -0.1F, z, x + 0.15F, -1.0F - swing, z + s * 0.8F);
         }
      }

      g.pop();
   }

   static void broom(G g, Cos.A a) {
      String wood = "veh_c:8B6914";
      String straw = "veh_c:E8C94A";
      g.push();
      g.translate(0.0F, 4.0F + VehGeo.sin(a.time * 2.2) * 0.35F, 0.0F);
      g.rotZ(-a.lean * 8.0F);
      g.rotX(-a.move * 6.0F);
      VehGeo.tube(g, wood, VehGeo.p(0.0F, 0.0F, -14.0F), VehGeo.p(0.0F, 0.0F, 14.0F), 0.5F, 0.45F, 8, true);

      for (int i = 0; i < 12; i++) {
         float an = (float)((Math.PI * 2) * i / 12.0);
         float sw = VehGeo.sin(a.time * 5.0F + i) * 0.3F;
         float x = VehGeo.cos(an) * 0.8F;
         float y = VehGeo.sin(an) * 0.8F;
         VehGeo.tube(g, straw, VehGeo.p(x * 0.5F, y * 0.5F, -14.0F), VehGeo.p(x + sw, y + sw * 0.5F, -18.0F), 0.12F, 0.08F, 4, false);
      }

      VehGeo.rbox(g, "veh_c:4A3210", -0.8F, -0.2F, -14.5F, 0.8F, 0.6F, -13.5F, 0.15F);
      g.pop();
   }

   static void segway(G g, Cos.A a) {
      String blk = "veh_flat:1C1D22";
      String wht = "veh_c:E8E8E8";
      float wr = 3.2F;
      float spin = a.wheel * 1.2F;
      g.push();
      g.rotZ(-a.lean * 6.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehRide.wheelAt(g, s * 4.8F, wr, 0.0F, wr, 1.8F, 2.0F, 0, spin, "veh_gun", null);
      }

      VehGeo.tube(g, "veh_chrome", VehGeo.p(-5.2F, wr, 0.0F), VehGeo.p(5.2F, wr, 0.0F), 0.3F, 0.3F, 8, true);
      VehGeo.rbox(g, blk, -3.2F, 1.2F, -2.5F, 3.2F, 2.8F, 2.5F, 0.5F);
      VehGeo.box(g, "veh_rubber", -2.8F, 2.7F, -2.0F, 2.8F, 2.95F, 2.0F);
      VehGeo.tube(g, wht, VehGeo.p(0.0F, 2.8F, 1.5F), VehGeo.p(0.0F, 18.5F, 3.5F), 0.5F, 0.5F, 8, false);
      VehGeo.rbox(g, blk, -2.8F, 17.5F, 3.0F, 2.8F, 19.0F, 4.5F, 0.3F);
      VehGeo.tube(g, wht, VehGeo.p(-3.0F, 18.0F, 4.0F), VehGeo.p(3.0F, 18.0F, 4.0F), 0.35F, 0.35F, 8, true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 2.4F, 18.0F, 4.0F), VehGeo.p(s * 3.5F, 18.0F, 4.0F), 0.55F, 0.55F, 8, true);
      }

      g.glow(true);
      VehGeo.rbox(g, "veh_glow:2AE0FF", -1.5F, 1.4F, -2.7F, 1.5F, 1.7F, 2.7F, 0.1F);
      g.glow(false);
      g.pop();
   }

   static void snowmobile(G g, Cos.A a) {
      String P = "veh_c:FF4B00";
      String blk = "veh_flat:1C1D22";
      String gun = "veh_gun";
      String cr = "veh_chrome";
      g.push();
      g.rotZ(-a.lean * 10.0F);
      g.translate(0.0F, VehGeo.sin(a.time * 36.0F) * 0.04F, 0.0F);
      VehGeo.rbox(g, P, -5.5F, 3.0F, -12.0F, 5.5F, 9.0F, 14.0F, 1.0F);
      VehGeo.rbox(g, blk, -5.8F, 2.0F, -10.0F, 5.8F, 3.5F, 12.0F, 0.3F);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -3.5F, 8.5F, -8.0F, 3.5F, 9.8F, 2.0F, 0.6F);
      g.push();
      g.translate(0.0F, 8.5F, -7.0F);
      g.rotX(-12.0F);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -3.8F, 0.0F, -1.5F, 3.8F, 6.5F, 0.1F, 0.5F);
      g.pop();
      VehGeo.rbox(g, P, -4.5F, 6.0F, 10.0F, 4.5F, 11.0F, 16.0F, 0.8F);
      VehGeo.tube(g, gun, VehGeo.p(0.0F, 8.5F, 15.0F), VehGeo.p(0.0F, 11.5F, 9.0F), 0.4F, 0.4F, 6, false);
      VehGeo.tube(g, cr, VehGeo.p(-4.5F, 11.5F, 9.0F), VehGeo.p(4.5F, 11.5F, 9.0F), 0.35F, 0.35F, 8, true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 3.5F, 11.5F, 9.0F), VehGeo.p(s * 5.2F, 11.5F, 9.0F), 0.55F, 0.55F, 8, true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, gun, s < 0 ? -7.5F : 5.0F, 0.5F, -12.0F, s < 0 ? -5.0F : 7.5F, 1.5F, 14.0F, 0.3F);
         VehGeo.rbox(g, cr, s < 0 ? -7.2F : 5.3F, 0.0F, -13.0F, s < 0 ? -5.3F : 7.2F, 0.5F, 15.0F, 0.15F);
      }

      VehGeo.rbox(g, blk, -3.5F, 1.0F, -14.0F, 3.5F, 3.0F, -10.0F, 0.4F);

      for (int i = 0; i < 8; i++) {
         VehGeo.rbox(g, gun, -3.3F, 1.2F, -13.5F + i * 1.5F, 3.3F, 2.8F, -12.5F + i * 1.5F, 0.15F);
      }

      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 2.5F, 9.5F, 16.5F, 0.8F, 0.5F, 0.3F, 8, 4);
      }

      VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -1.5F, 7.0F, -12.5F, 1.5F, 8.5F, -11.8F, 0.1F);
      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   static void dreirad(G g, Cos.A a) {
      String red = "veh_c:E3242B";
      String gun = "veh_gun";
      String cr = "veh_chrome";
      float spin = a.wheel * 1.4F;
      float wr = 2.5F;
      g.push();
      g.rotZ(-a.lean * 5.0F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 5.0F, wr, -7.0F);
         VehCar.carWheel(g, spin, s < 0, wr, 1.5F, 1.6F, 0, null);
         g.pop();
      }

      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 5.5F, 7.5F), 6.0F, a.lean * 25.0F);
      g.push();
      g.translate(0.0F, wr + 0.5F, 8.0F);
      VehCar.carWheel(g, spin * 0.9F, false, wr + 0.5F, 1.8F, 2.0F, 0, null);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, red, VehGeo.p(s * 0.7F, 5.0F, 7.8F), VehGeo.p(s * 0.7F, wr + 0.5F, 8.0F), 0.3F, 0.3F, 6, false);
      }

      VehGeo.tube(g, red, VehGeo.p(0.0F, 5.5F, 7.5F), VehGeo.p(0.0F, 11.0F, 8.0F), 0.4F, 0.4F, 8, false);
      VehGeo.tube(g, cr, VehGeo.p(-5.5F, 11.0F, 8.0F), VehGeo.p(5.5F, 11.0F, 8.0F), 0.35F, 0.35F, 8, true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 4.2F, 11.0F, 8.0F), VehGeo.p(s * 6.0F, 11.0F, 8.0F), 0.55F, 0.55F, 8, true);
      }

      g.pop();
      VehGeo.tube(g, red, VehGeo.p(0.0F, 4.5F, 6.0F), VehGeo.p(0.0F, 5.5F, -2.0F), 0.5F, 0.4F, 8, false);
      VehGeo.tube(g, red, VehGeo.p(0.0F, 5.5F, -2.0F), VehGeo.p(0.0F, 8.0F, -3.0F), 0.4F, 0.4F, 8, false);
      VehGeo.rbox(g, "veh_leather:2A2224", -2.5F, 7.5F, -5.0F, 2.5F, 9.0F, -1.5F, 0.5F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, red, VehGeo.p(s * 0.6F, 4.5F, 0.0F), VehGeo.p(s * 4.5F, wr, -7.0F), 0.3F, 0.3F, 6, false);
      }

      VehGeo.tube(g, cr, VehGeo.p(-5.5F, wr, -7.0F), VehGeo.p(5.5F, wr, -7.0F), 0.25F, 0.25F, 6, true);
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -0.5F, 7.5F, -5.3F, 0.5F, 8.5F, -4.8F, 0.1F);
      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   static void inline(G g, Cos.A a) {
      String boot = "veh_c:2A5BDF";
      String frame = "veh_alu";
      float spin = a.wheel * 3.0F;
      g.push();

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 1.6F, 0.0F, 0.0F);
         VehGeo.rbox(g, boot, -1.2F, 1.5F, -2.0F, 1.2F, 5.5F, 3.0F, 0.4F);
         VehGeo.rbox(g, frame, -0.6F, 0.3F, -3.5F, 0.6F, 1.2F, 4.5F, 0.15F);

         for (int i = 0; i < 4; i++) {
            float z = -2.5F + i * 2.0F;
            g.push();
            g.translate(0.0F, 0.8F, z);
            g.rotZ(-90.0F);
            VehGeo.lathe(
               g,
               new String[]{"veh_side", "veh_urethane", "veh_urethane", "veh_side"},
               new float[][]{{0.01F, -0.35F}, {0.5F, -0.35F}, {0.75F, -0.2F}, {0.75F, 0.2F}, {0.5F, 0.35F}, {0.01F, 0.35F}},
               10
            );
            g.pop();
         }

         VehGeo.rbox(g, "veh_c:FF4B4B", -1.0F, 3.5F, 2.5F, 0.5F, 5.0F, 3.5F, 0.15F);
         g.pop();
      }

      g.pop();
   }

   static void einrad(G g, Cos.A a) {
      String cr = "veh_chrome";
      String gun = "veh_gun";
      float wr = 4.5F;
      float spin = a.wheel * 0.7F;
      g.push();
      g.rotZ(-a.lean * 8.0F);
      g.push();
      g.translate(0.0F, wr, 0.0F);
      g.rotX(spin);
      g.rotZ(-90.0F);
      VehGeo.lathe(
         g,
         new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
         new float[][]{{3.5F, -0.4F}, {wr - 0.3F, -0.5F}, {wr, 0.0F}, {wr - 0.3F, 0.5F}, {3.5F, 0.4F}},
         22
      );
      int n = 16;

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n;
         VehGeo.tube(
            g,
            cr,
            VehGeo.p(VehGeo.cos(an) * 0.35F, i % 2 == 0 ? -0.3F : 0.3F, VehGeo.sin(an) * 0.35F),
            VehGeo.p(VehGeo.cos(an) * 3.4F, 0.0F, VehGeo.sin(an) * 3.4F),
            0.06F,
            0.06F,
            3,
            false
         );
      }

      g.pop();
      VehGeo.tube(g, cr, VehGeo.p(0.0F, wr, 0.0F), VehGeo.p(0.0F, wr + 11.0F, 0.0F), 0.35F, 0.35F, 8, false);
      VehGeo.rbox(g, "veh_leather:2A2224", -2.5F, wr + 10.5F, -1.8F, 2.5F, wr + 11.5F, 1.8F, 0.4F);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(0.0F, wr, 0.0F);
         g.rotX(spin + (s > 0 ? 0.0F : (float) Math.PI));
         float px = s * 2.2F;
         float py = -4.2F;
         VehGeo.tube(g, cr, VehGeo.p(0.0F, 0.0F, 0.0F), VehGeo.p(0.0F, py, 0.0F), 0.18F, 0.18F, 6, false);
         VehGeo.rbox(g, gun, -0.7F, py - 0.2F, -1.0F, 0.7F, py + 0.2F, 1.0F, 0.08F);
         g.pop();
      }

      g.pop();
   }

   static void rennrad(G g, Cos.A a) {
      String fr = "veh_c:E3242B";
      String cr = "veh_chrome";
      String gun = "veh_gun";
      float spin = a.wheel * 0.55F;
      g.push();
      g.rotZ(-a.lean * 6.0F);
      g.push();
      g.translate(VehRide.RA[0], VehRide.RA[1], VehRide.RA[2]);
      VehRide.bikeWheel(g, spin);
      g.pop();
      g.push();
      g.translate(1.25F, VehRide.RA[1], VehRide.RA[2]);
      g.rotX(spin);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, gun, new float[][]{{0.01F, -0.12F}, {0.95F, -0.12F}, {0.95F, 0.12F}, {0.01F, 0.12F}}, 12);
      g.pop();
      VehGeo.pipe(g, fr, new float[][]{VehRide.BB, {0.0F, 11.8F, -0.25F}}, 0.3F, 8, false);
      VehGeo.pipe(g, fr, new float[][]{{0.0F, 12.0F, 10.5F}, {0.0F, 7.5F, 9.0F}, {0.0F, 5.0F, 5.5F}, {0.0F, 4.3F, 2.8F}}, 0.3F, 8, false);
      VehGeo.pipe(g, fr, new float[][]{{0.0F, 12.0F, 10.5F}, {0.0F, 9.0F, 8.0F}, {0.0F, 7.6F, 4.5F}, {0.0F, 7.4F, 1.5F}}, 0.28F, 8, false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.3F, 4.4F, 2.3F}, {s * 0.78F, 5.4F, -4.2F}}, 0.22F, 6, false);
         VehGeo.pipe(g, fr, new float[][]{{s * 0.22F, 11.1F, -0.1F}, {s * 0.78F, 5.4F, -4.2F}}, 0.2F, 6, false);
      }

      VehGeo.tube(g, fr, VehGeo.p(0.0F, 9.8F, 11.0F), VehGeo.p(0.0F, 12.8F, 10.3F), 0.45F, 0.45F, 10, true);
      VehGeo.tube(g, fr, VehGeo.p(-0.9F, VehRide.BB[1], VehRide.BB[2]), VehGeo.p(0.9F, VehRide.BB[1], VehRide.BB[2]), 0.5F, 0.5F, 10, true);
      VehGeo.tube(g, cr, VehGeo.p(0.0F, 11.5F, -0.2F), VehGeo.p(0.0F, 13.0F, -0.5F), 0.22F, 0.22F, 6, false);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -1.5F, 12.8F, -2.5F, 1.5F, 13.3F, 0.0F, 0.3F);
      float crank = Vehicles.pedalAngle(a.wheel);
      g.push();
      g.translate(0.0F, VehRide.BB[1], VehRide.BB[2]);
      g.rotX(crank);
      VehGeo.tube(g, "veh_alu", VehGeo.p(-1.75F, 0.0F, 0.0F), VehGeo.p(1.75F, 0.0F, 0.0F), 0.25F, 0.25F, 6, true);
      VehGeo.beam(g, "veh_alu", VehGeo.p(1.65F, 0.0F, 0.0F), VehGeo.p(1.65F, -2.6F, 0.0F), 0.35F, 0.45F, 0.3F, 0.4F, VehGeo.p(0.0F, 0.0F, 1.0F));
      VehGeo.beam(g, "veh_alu", VehGeo.p(-1.65F, 0.0F, 0.0F), VehGeo.p(-1.65F, 2.6F, 0.0F), 0.35F, 0.45F, 0.3F, 0.4F, VehGeo.p(0.0F, 0.0F, 1.0F));
      g.pop();
      float[][] ped = Vehicles.pedals("veh_bike", a.wheel);
      if (ped != null) {
         for (float[] pp : ped) {
            float sx = Math.signum(pp[0]);
            VehGeo.rbox(g, "veh_flat:1C1D22", pp[0] - 0.7F, pp[1] - 0.15F, pp[2] - 0.5F, pp[0] + 0.7F, pp[1] + 0.15F, pp[2] + 0.5F, 0.08F);
         }
      }

      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 11.5F, 10.75F), 17.8F, a.lean * 22.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.75F, 9.5F, 11.3F}, {s * 0.75F, 7.3F, 12.25F}, {s * 0.8F, VehRide.FA[1], VehRide.FA[2]}}, 0.22F, 6, false);
      }

      g.push();
      g.translate(VehRide.FA[0], VehRide.FA[1], VehRide.FA[2]);
      VehRide.bikeWheel(g, spin);
      g.pop();
      VehGeo.tube(g, cr, VehGeo.p(0.0F, 12.6F, 10.35F), VehGeo.p(0.0F, 13.5F, 10.1F), 0.25F, 0.25F, 6, true);
      VehGeo.pipe(
         g, cr, new float[][]{{-4.5F, 13.5F, 7.5F}, {-2.5F, 13.4F, 9.5F}, {0.0F, 13.2F, 10.0F}, {2.5F, 13.4F, 9.5F}, {4.5F, 13.5F, 7.5F}}, 0.2F, 6, false
      );

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 3.8F, 13.5F, 8.2F), VehGeo.p(s * 5.0F, 13.5F, 7.2F), 0.42F, 0.42F, 8, true);
      }

      g.pop();
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      VehGeo.rbox(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", -0.5F, 10.2F, -9.75F, 0.5F, 10.65F, -9.35F, 0.1F);
      if (night) {
         g.glow(false);
      }

      g.pop();
   }

   static void bmx(G g, Cos.A a) {
      String fr = "veh_c:1ED760";
      String cr = "veh_chrome";
      String gun = "veh_gun";
      float spin = a.wheel * 0.7F;
      float bwr = 3.8F;
      g.push();
      g.rotZ(-a.lean * 8.0F);

      for (int sz = -1; sz <= 1; sz += 2) {
         float z = sz > 0 ? 9.5F : -6.0F;
         g.push();
         g.translate(0.0F, bwr, z);
         g.rotX(sz > 0 ? spin : spin);
         g.rotZ(-90.0F);
         VehGeo.lathe(
            g,
            new String[]{"veh_side", "veh_tread", "veh_tread", "veh_side"},
            new float[][]{{2.4F, -0.3F}, {bwr - 0.2F, -0.4F}, {bwr, 0.0F}, {bwr - 0.2F, 0.4F}, {2.4F, 0.3F}},
            18
         );
         int n = 16;

         for (int i = 0; i < n; i++) {
            double an = (Math.PI * 2) * i / n;
            float hy = i % 4 < 2 ? -0.45F : 0.45F;
            VehGeo.tube(
               g,
               cr,
               VehGeo.p(VehGeo.cos(an) * 0.5F, hy, VehGeo.sin(an) * 0.5F),
               VehGeo.p(VehGeo.cos(an) * 2.3F, 0.0F, VehGeo.sin(an) * 2.3F),
               0.06F,
               0.06F,
               3,
               false
            );
         }

         g.pop();
      }

      VehGeo.pipe(g, fr, new float[][]{{0.0F, 3.8F, 1.5F}, {0.0F, 9.5F, -0.5F}}, 0.3F, 8, false);
      VehGeo.pipe(g, fr, new float[][]{{0.0F, 9.0F, 7.5F}, {0.0F, 6.5F, 5.5F}, {0.0F, 4.5F, 2.5F}}, 0.3F, 8, false);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.25F, 4.0F, 1.5F}, {s * 0.65F, bwr, -6.0F}}, 0.2F, 6, false);
         VehGeo.pipe(g, fr, new float[][]{{s * 0.2F, 9.0F, -0.5F}, {s * 0.65F, bwr, -6.0F}}, 0.18F, 6, false);
      }

      VehGeo.tube(g, fr, VehGeo.p(-0.75F, 3.8F, 1.5F), VehGeo.p(0.75F, 3.8F, 1.5F), 0.45F, 0.45F, 8, true);
      float crank = Vehicles.pedalAngle(a.wheel);
      g.push();
      g.translate(0.0F, 3.8F, 1.5F);
      g.rotX(crank);
      VehGeo.tube(g, "veh_alu", VehGeo.p(-1.5F, 0.0F, 0.0F), VehGeo.p(1.5F, 0.0F, 0.0F), 0.22F, 0.22F, 6, true);
      g.pop();
      float[][] ped = Vehicles.pedals("veh_bike", a.wheel);
      if (ped != null) {
         for (float[] pp : ped) {
            VehGeo.rbox(g, "veh_flat:1C1D22", pp[0] - 0.6F, pp[1] - 0.15F, pp[2] - 0.5F, pp[0] + 0.6F, pp[1] + 0.15F, pp[2] + 0.5F, 0.08F);
         }
      }

      VehGeo.tube(g, cr, VehGeo.p(0.0F, 9.0F, -0.5F), VehGeo.p(0.0F, 10.0F, -0.7F), 0.2F, 0.2F, 6, false);
      VehGeo.rbox(g, "veh_leather:1A1A1E", -2.0F, 9.8F, -2.5F, 2.0F, 10.3F, -0.5F, 0.3F);
      g.push();
      VehGeo.steer(g, VehGeo.p(0.0F, 8.5F, 7.8F), 12.0F, a.lean * 28.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, fr, new float[][]{{s * 0.6F, 8.0F, 8.0F}, {s * 0.6F, bwr, 9.5F}}, 0.2F, 6, false);
      }

      VehGeo.tube(g, fr, VehGeo.p(0.0F, 7.5F, 8.3F), VehGeo.p(0.0F, 14.5F, 7.8F), 0.35F, 0.35F, 8, false);
      VehGeo.tube(g, cr, VehGeo.p(-5.0F, 14.5F, 7.8F), VehGeo.p(5.0F, 14.5F, 7.8F), 0.3F, 0.3F, 8, true);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, "veh_gripr", VehGeo.p(s * 3.8F, 14.5F, 7.8F), VehGeo.p(s * 5.5F, 14.5F, 7.8F), 0.52F, 0.52F, 8, true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.tube(g, cr, VehGeo.p(s * 5.3F, bwr, 9.5F), VehGeo.p(s * 5.3F, bwr + 5.0F, 9.5F), 0.2F, 0.2F, 6, true);
      }

      g.pop();
      g.pop();
   }

   static void cabrio(G g, Cos.A a) {
      VehCar.car(g, a, 13111344, 15921906);
      g.push();
      carXform(g, a);
      VehGeo.box(g, "veh_interior", -7.0F, 9.8F, -5.0F, 7.0F, 10.0F, 8.0F);
      g.pop();
   }

   static void hotrod(G g, Cos.A a) {
      VehCar.car(g, a, 10824234, 16764163);
      g.push();
      carXform(g, a);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, "veh_chrome", new float[][]{{s * 5.0F, 6.0F, 28.0F}, {s * 5.0F, 6.0F, 32.0F}, {s * 5.5F, 12.0F, 35.0F}}, 0.5F, 8, true);
      }

      g.pop();
   }

   static void lambo(G g, Cos.A a) {
      String P = "veh_paint:" + VehCar.hex(16764163);
      String carbon = "veh_carbon";
      String gun = "veh_gun";
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 40.0F) * 0.035F * (1.0F - a.move * 0.5F), 0.0F);
      float steer = a.lean * 16.0F;

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            g.push();
            g.translate(sx * 11.0F, 4.6F, sz > 0 ? 20.0F : -19.0F);
            if (sz > 0) {
               g.rotY(steer);
            }

            VehCar.carWheel(g, a.wheel, sx < 0, 4.6F, 3.4F, 3.2F, 8, "veh_c:D8141B");
            g.pop();
         }
      }

      g.translate(0.0F, 4.6F, 0.0F);
      g.rotZ(-a.lean * 2.0F);
      g.translate(0.0F, -4.6F, 0.0F);
      VehGeo.loft(
         g,
         VehGeo.range(-30.0F, 34.0F, 2.8F),
         z -> {
            float t = (z + 30.0F) / 64.0F;
            float w = VehGeo.lerp(5.0F, 13.5F, VehGeo.smooth(0.0F, 0.2F, t)) - VehGeo.smooth(0.85F, 1.0F, t) * 5.5F;
            float hc = VehGeo.lerp(6.0F, 10.5F, VehGeo.smooth(0.0F, 0.3F, t)) - VehGeo.smooth(0.5F, 1.0F, t) * 2.5F;
            float yb = VehGeo.lerp(4.5F, 2.0F, VehGeo.smooth(0.0F, 0.25F, t)) + VehGeo.smooth(0.8F, 1.0F, t) * 2.0F;
            float wTop = w * 0.7F;
            return new float[][]{
               {0.0F, yb},
               {w * 0.4F, yb},
               {w, yb + 1.5F},
               {w, hc * 0.6F},
               {wTop, hc},
               {0.0F, hc + 0.3F},
               {-wTop, hc},
               {-w, hc * 0.6F},
               {-w, yb + 1.5F},
               {-w * 0.4F, yb}
            };
         },
         (k, j, z, pa, pb) -> {
            if (j == 0 || j == 9) {
               return null;
            } else if (j == 1 || j == 8) {
               return "veh_under";
            } else if (j >= 4 && j <= 5) {
               return z > -5.0F && z < 10.0F ? "veh_glass" : P;
            } else {
               return P;
            }
         },
         true,
         true,
         11.0F,
         2.0F
      );
      VehGeo.rbox(g, "veh_leather:1A1A1E", -4.3F, 2.6F, -4.0F, 4.3F, 3.6F, 3.5F, 0.4F);
      VehGeo.rbox(g, "veh_interior", -7.0F, 7.5F, 8.5F, 7.0F, 11.0F, 11.0F, 0.4F);
      VehGeo.tube(g, gun, VehGeo.p(0.0F, 9.2F, 10.0F), VehGeo.p(0.0F, 10.2F, 8.0F), 0.3F, 0.3F, 6, false);
      g.push();
      g.translate(0.0F, 10.4F, 7.8F);
      g.rotX(-58.0F);
      g.rotY(-a.lean * 70.0F);
      VehGeo.torus(g, "veh_leather:1A1A1E", 2.9F, 0.3F, 12, 4);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, carbon, VehGeo.p(s * 6, 8.0F, -25.0F), VehGeo.p(s * 6, 12.0F, -27.0F), 0.5F, 1.2F, 0.5F, 1.0F, VehGeo.p(0.0F, 0.0F, 1.0F));
      }

      VehGeo.box(g, carbon, -10.8F, 12.2F, -29.0F, 10.8F, 12.6F, -25.0F);
      VehGeo.box(g, carbon, -10.0F, 2.0F, 30.0F, 10.0F, 2.8F, 34.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, gun, s * 12.0F, 6.5F, -11.0F, 0.7F, 1.3F, 3.0F, 8, 4);
      }

      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 3 - 2.5F, 4.5F, 33.5F, s * 3 + 2.5F, 6.5F, 34.2F, 0.15F);
         VehGeo.tube(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", VehGeo.p(s * 3.5F, 5.5F, -30.5F), VehGeo.p(s * 8.0F, 5.2F, -30.2F), 0.4F, 0.35F, 8, true);
      }

      if (night) {
         g.glow(false);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, gun, VehGeo.p(s * 11.5F, 9.5F, 8.0F), VehGeo.p(s * 13.5F, 10.2F, 8.5F), 0.35F, 0.3F);
         VehGeo.ellipsoid(g, "veh_mirror", s * 14.0F, 10.3F, 8.5F, 1.2F, 0.65F, 0.6F, 8, 4);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, carbon, s < 0 ? -13.0F : 11.5F, 4.0F, -14.0F, s < 0 ? -11.5F : 13.0F, 6.5F, -8.0F, 0.2F);
         VehGeo.rbox(g, "veh_flat:0A0A0F", s < 0 ? -12.8F : 11.7F, 4.3F, -13.5F, s < 0 ? -11.7F : 12.8F, 6.2F, -8.5F, 0.1F);
      }

      VehGeo.box(g, carbon, -12.5F, 1.8F, 32.0F, 12.5F, 2.3F, 35.5F);

      for (int i = -3; i <= 3; i++) {
         VehGeo.box(g, carbon, i * 3.0F - 1.0F, 1.8F, -30.0F, i * 3.0F + 1.0F, 3.0F, -26.0F);
      }

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int i = 0; i < 2; i++) {
            float xo = sx * (2.5F + i * 2.8F);
            VehGeo.tube(g, "veh_chrome", VehGeo.p(xo, 3.2F, -29.0F), VehGeo.p(xo, 3.2F, -31.5F), 0.55F, 0.65F, 8, true);
            VehGeo.tube(g, "veh_flat:0A0A0F", VehGeo.p(xo, 3.2F, -29.5F), VehGeo.p(xo, 3.2F, -31.3F), 0.4F, 0.5F, 8, true);
         }
      }

      g.glow(true);
      float glowPulse = 0.6F + 0.15F * VehGeo.sin(a.time * 3.5F);
      String ug = "veh_glow:" + VehCar.hex(16764163);
      VehGeo.rbox(g, ug, -10.0F, 1.6F, -24.0F, 10.0F, 1.9F, 28.0F, 0.05F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, ug, s < 0 ? -12.5F : 11.0F, 1.6F, -18.0F, s < 0 ? -11.0F : 12.5F, 1.9F, 20.0F, 0.05F);
      }

      if (a.move > 0.3F) {
         float fl = a.move * (0.8F + 0.4F * VehGeo.sin(a.time * 22.0F));

         for (int sx = -1; sx <= 1; sx += 2) {
            for (int i = 0; i < 2; i++) {
               float xo = sx * (2.5F + i * 2.8F);
               float len = 1.5F + fl * 3.0F;
               VehGeo.ellipsoid(g, "veh_glow:FF6600", xo, 3.2F, -31.5F - len * 0.5F, 0.35F, 0.35F, len * 0.5F, 6, 3);
               VehGeo.ellipsoid(g, "veh_glow:FFCC00", xo, 3.2F, -31.5F - len * 0.25F, 0.2F, 0.2F, len * 0.3F, 6, 3);
            }
         }
      }

      g.glow(false);
      g.pop();
   }
}
