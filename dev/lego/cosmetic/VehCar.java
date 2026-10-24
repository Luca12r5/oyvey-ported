package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class VehCar {
   static final float CAR_HIP = 5.5F;
   static final float KART_HIP = 3.6F;
   static final float[] CAR_HAND = new float[]{2.9F, 10.4F, 7.8F};
   static final float[] KART_HAND = new float[]{2.6F, 7.5F, 8.6F};
   private static final float[] KZ = new float[]{-30.0F, -28.5F, -25.0F, -18.5F, -12.0F, -6.0F, 0.0F, 6.0F, 12.0F, 19.5F, 25.0F, 29.0F, 31.0F, 32.0F};
   private static final float[] KHC = new float[]{7.4F, 9.4F, 10.5F, 10.9F, 11.1F, 11.3F, 11.3F, 11.4F, 11.5F, 10.3F, 9.3F, 8.2F, 7.5F, 6.2F};
   private static final float[] KCR = new float[]{7.4F, 9.8F, 11.4F, 12.5F, 11.9F, 11.7F, 11.7F, 11.7F, 11.9F, 12.2F, 11.1F, 9.0F, 7.5F, 6.2F};
   private static final float[] KW = new float[]{10.0F, 11.8F, 12.7F, 13.2F, 12.6F, 12.2F, 12.2F, 12.3F, 12.7F, 13.1F, 12.6F, 11.3F, 9.8F, 8.4F};
   private static final float[] KYB = new float[]{4.0F, 2.7F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.2F, 2.6F, 3.3F, 4.0F};
   static final float WR = 4.6F;
   static final float WW = 3.4F;
   static final float WX = 10.6F;
   static final float WZF = 19.5F;
   static final float WZR = -18.5F;
   static final float ARCH = 5.5F;
   private static final float FLOOR = 3.0F;

   private VehCar() {
   }

   static boolean isNight() {
      return VehExtra.isNight();
   }

   static String hex(int c) {
      return String.format("%06X", c & 16777215);
   }

   private static float cockpit(float z) {
      return VehGeo.smooth(-6.9F, -5.7F, z) * (1.0F - VehGeo.smooth(8.4F, 10.2F, z));
   }

   private static float arch(float z) {
      float best = -1.0F;

      for (float wz : new float[]{19.5F, -18.5F}) {
         float dz = z - wz;
         if (Math.abs(dz) < 5.5F) {
            best = Math.max(best, 4.6F + (float)Math.sqrt(30.25F - dz * dz));
         }
      }

      return best;
   }

   private static float[][] carHalf(float z) {
      float W = VehGeo.curve(z, KZ, KW);
      float hc = VehGeo.curve(z, KZ, KHC);
      float cr = VehGeo.curve(z, KZ, KCR);
      float yb = VehGeo.curve(z, KZ, KYB);
      float sill = yb + 0.8F;
      float ya = Math.max(sill, arch(z));
      float xin = Math.min(8.0F, W - 2.6F);
      float r = cockpit(z);
      float cw = Math.min(7.3F, W - 4.4F);
      float h = cr - ya;
      float t10 = VehGeo.lerp(cr, hc, 0.45F);
      return new float[][]{
         {0.0F, yb},
         {xin - 0.4F, yb},
         {xin, Math.max(ya, yb + 0.7F)},
         {W - 1.5F, ya},
         {W, ya + h * 0.36F},
         {W - 0.3F, ya + h * 0.74F},
         {W - 2.2F, cr},
         {cw + 1.2F, t10},
         {cw, t10 - 0.6F * r},
         {cw - 0.35F, VehGeo.lerp(t10, 3.0F, r)},
         {Math.min(3.0F, cw - 0.7F), VehGeo.lerp(hc, 3.0F, r)},
         {1.2F, VehGeo.lerp(hc, 3.0F, r)},
         {0.0F, VehGeo.lerp(hc, 3.0F, r)}
      };
   }

   static void car(G g, Cos.A a, int paint, int stripe) {
      String P = "veh_paint:" + hex(paint);
      String ST = "veh_paint:" + hex(stripe);
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 38.0F) * 0.04F * (1.0F - a.move * 0.5F), 0.0F);
      float steer = a.lean * 16.0F;

      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            g.push();
            g.translate(sx * 10.6F, 4.6F, sz > 0 ? 19.5F : -18.5F);
            if (sz > 0) {
               g.rotY(steer);
            }

            carWheel(g, a.wheel, sx < 0, 4.6F, 3.4F, 3.2F, 8, "veh_c:D8141B");
            g.pop();
         }
      }

      g.translate(0.0F, 4.6F, 0.0F);
      g.rotZ(-a.lean * 2.2F);
      g.translate(0.0F, -4.6F, 0.0F);
      List<Float> zl = new ArrayList<>();

      for (float wz : new float[]{19.5F, -18.5F}) {
         for (float z : VehGeo.range(wz - 5.5F + 0.02F, wz + 5.5F - 0.02F, 1.8F)) {
            zl.add(z);
         }

         zl.add(wz - 5.5F - 0.3F);
         zl.add(wz + 5.5F + 0.3F);
      }

      for (float z : new float[]{-30.0F, -28.5F, -6.9F, -5.7F, 8.4F, 10.2F, 29.0F, 31.0F, 32.0F}) {
         zl.add(z);
      }

      for (float z : VehGeo.range(-30.0F, 32.0F, 4.4F)) {
         if (Math.abs(z - 19.5F) > 6.7F && Math.abs(z - -18.5F) > 6.7F) {
            zl.add(z);
         }
      }

      Collections.sort(zl);
      float[] zs = new float[zl.size()];
      int nz = 0;

      for (float zx : zl) {
         if (nz == 0 || zx - zs[nz - 1] > 0.3F) {
            zs[nz++] = zx;
         }
      }

      zs = Arrays.copyOf(zs, nz);
      int N = 13;
      int NS = zs.length;
      VehGeo.loft(g, zs, zxx -> VehGeo.mirrorRing(carHalf(zxx)), (k, j, zxx, pa, pb) -> {
         if (k >= 0 && k < NS - 1) {
            int hs = j < 12 ? j : 23 - j;
            float r = cockpit(zxx);
            boolean inside = r > 0.3F || cockpit(zxx + 1.2F) > 0.3F;
            boolean isArch = arch(zxx) > 0.0F || arch(zxx + 1.0F) > 0.0F;
            if (hs == 0) {
               return null;
            } else if (hs <= 2) {
               return "veh_under";
            } else if (hs == 3) {
               return isArch ? P : "veh_carbon";
            } else if (hs >= 8 && inside) {
               return "veh_interior";
            } else {
               return hs == 10 && !inside ? ST : P;
            }
         } else {
            return P;
         }
      }, true, true, 13.2F, 2.0F);
      VehGeo.loft(g, VehGeo.range(-22.0F, -5.4F, 2.4F), zxx -> {
         float t = (zxx + 22.0F) / 16.6F;
         float rx = VehGeo.lerp(0.9F, 3.7F, (float)Math.sqrt(t));
         float top = VehGeo.lerp(10.6F, 16.3F, (float)Math.pow(t, 0.7));
         float base = 9.6F;
         float[][] o = new float[10][];
         o[0] = new float[]{0.0F, base};
         o[1] = new float[]{rx, base};

         for (int i = 1; i <= 7; i++) {
            double an = Math.PI * i / 8.0;
            o[1 + i] = new float[]{(float)Math.cos(an) * rx, base + (float)Math.sin(an) * (top - base)};
         }

         o[9] = new float[]{-rx, base};
         return o;
      }, (k, j, zxx, pa, pb) -> P, false, true, 17.0F, 9.0F);
      VehGeo.rbox(g, "veh_leather:2A2224", -4.3F, 2.6F, -4.2F, 4.3F, 3.6F, 3.6F, 0.45F);
      g.push();
      g.translate(0.0F, 3.2F, -4.1F);
      g.rotX(-11.0F);
      VehGeo.rbox(g, "veh_leather:2A2224", -4.3F, 0.0F, -1.5F, 4.3F, 10.6F, 0.2F, 0.5F);
      VehGeo.rbox(g, "veh_leather:C8102E", -4.8F, 0.4F, -1.2F, -3.6F, 8.5F, 1.3F, 0.4F);
      VehGeo.rbox(g, "veh_leather:C8102E", 3.6F, 0.4F, -1.2F, 4.8F, 8.5F, 1.3F, 0.4F);
      g.pop();
      VehGeo.rbox(g, "veh_interior", -7.2F, 7.6F, 9.0F, 7.2F, 11.2F, 11.4F, 0.5F);
      VehGeo.box6(g, "veh_flat:15161A", new String[]{null, "veh_gauges", null, null, null, null}, -3.4F, 8.5F, 8.7F, 3.4F, 10.7F, 9.2F);
      VehGeo.tube(g, "veh_gun", VehGeo.p(0.0F, 9.2F, 10.0F), VehGeo.p(0.0F, 10.2F, 8.1F), 0.35F, 0.35F, 6, false);
      g.push();
      g.translate(0.0F, 10.4F, 7.8F);
      g.rotX(-58.0F);
      g.rotY(-a.lean * 70.0F);
      VehGeo.torus(g, "veh_leather:1A1A1E", 2.9F, 0.34F, 12, 4);

      for (int i = 0; i < 3; i++) {
         double an = Math.toRadians(90 + i * 120);
         VehGeo.beam(
            g,
            "veh_gun",
            VehGeo.p((float)Math.cos(an) * 0.6F, 0.0F, (float)Math.sin(an) * 0.6F),
            VehGeo.p((float)Math.cos(an) * 2.7F, 0.0F, (float)Math.sin(an) * 2.7F),
            0.55F,
            0.25F,
            0.4F,
            0.2F,
            VehGeo.UP
         );
      }

      VehGeo.lathe(g, new String[]{"veh_gun", "veh_c:D8141B"}, new float[][]{{0.9F, -0.35F}, {0.9F, 0.2F}, {0.01F, 0.32F}}, 10);
      g.pop();

      for (int i = -1; i <= 1; i++) {
         g.push();
         g.translate(i * 4.6F, 10.9F, 11.2F - Math.abs(i) * 0.9F);
         g.rotY(-i * 28);
         g.rotX(-38.0F);
         VehGeo.box(g, "veh_glass", -2.4F, 0.0F, -0.08F, 2.4F, 3.6F, 0.08F);
         g.pop();
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, "veh_carbon", VehGeo.p(s * 11.4F, 11.5F, 9.4F), VehGeo.p(s * 13.4F, 12.6F, 9.9F), 0.45F, 0.35F);
         VehGeo.ellipsoid(g, P, s * 13.9F, 12.8F, 10.1F, 1.45F, 0.85F, 0.8F, 8, 5);
         VehGeo.ellipsoid(g, "veh_mirror", s * 13.9F, 12.8F, 9.45F, 1.2F, 0.66F, 0.2F, 8, 4);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, "veh_flat:111216", s * 12.1F, 7.6F, -10.8F, 0.75F, 1.5F, 3.4F, 8, 4);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.beam(g, "veh_carbon", VehGeo.p(s * 6, 9.6F, -25.0F), VehGeo.p(s * 6, 14.3F, -27.2F), 0.5F, 1.6F, 0.5F, 1.2F, VehGeo.p(0.0F, 0.0F, 1.0F));
         VehGeo.box(g, "veh_carbon", s < 0 ? -11.35F : 11.0F, 12.9F, -30.0F, s < 0 ? -11.0F : 11.35F, 16.2F, -25.0F);
      }

      g.push();
      g.translate(0.0F, 14.6F, -27.4F);
      g.rotY(90.0F);
      g.rotZ(-6.0F);
      VehGeo.loft(
         g,
         new float[]{-11.1F, 11.1F},
         zxx -> new float[][]{{2.3F, 0.0F}, {1.1F, 0.4F}, {-0.6F, 0.65F}, {-1.9F, 0.5F}, {-2.4F, 0.05F}, {-1.9F, -0.28F}, {0.0F, -0.25F}, {1.2F, -0.1F}},
         (k, j, zxx, pa, pb) -> "veh_carbon",
         true,
         true,
         1.0F,
         -1.0F
      );
      g.pop();
      boolean night = isNight();
      if (night) {
         g.glow(true);
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.ellipsoid(g, night ? "veh_glow:FFF6D8" : "veh_flat:DDDDAA", s * 7.9F, 7.6F, 28.3F, 2.3F, 0.95F, 2.2F, 8, 4);
         VehGeo.tube(g, night ? "veh_glow:FF2A2A" : "veh_flat:882222", VehGeo.p(s * 3.5F, 8.2F, -29.6F), VehGeo.p(s * 8.9F, 8.1F, -29.1F), 0.5F, 0.45F, 8, true);
      }

      VehGeo.tube(g, night ? "veh_glow:FF6A4A" : "veh_flat:884433", VehGeo.p(-3.5F, 8.2F, -29.6F), VehGeo.p(3.5F, 8.2F, -29.6F), 0.22F, 0.22F, 6, true);
      if (night) {
         g.glow(false);
      }

      VehGeo.box6(g, "veh_under", new String[]{"veh_grille", null, null, null, null, null}, -6.2F, 3.4F, 29.6F, 6.2F, 6.0F, 32.4F);
      VehGeo.box(g, "veh_carbon", -9.2F, 2.3F, 29.0F, 9.2F, 2.8F, 32.9F);
      VehGeo.box6(g, "veh_under", new String[]{null, "veh_plate", null, null, null, null}, -3.2F, 4.9F, -30.45F, 3.2F, 6.9F, -29.6F);
      VehGeo.box(g, "veh_carbon", -9.0F, 2.25F, -31.0F, 9.0F, 3.3F, -27.0F);

      for (int i = -2; i <= 2; i++) {
         VehGeo.box(g, "veh_carbon", i * 3.2F - 0.12F, 3.3F, -31.0F, i * 3.2F + 0.12F, 4.4F, -28.0F);
      }

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 6.6F, 3.9F, -30.2F);
         g.rotX(-90.0F);
         VehGeo.lathe(g, new String[]{"veh_chrome", "veh_chrome", "veh_under"}, new float[][]{{0.8F, -1.6F}, {0.8F, 1.1F}, {0.55F, 1.1F}, {0.55F, 0.6F}}, 10);
         VehGeo.lathe(g, "veh_under", new float[][]{{0.55F, 0.6F}, {0.01F, 0.6F}}, 10);
         g.pop();
      }

      g.pop();
   }

   static void carWheel(G g, float spin, boolean left, float R, float w, float rimR, int spokes, String caliper) {
      g.push();
      if (left) {
         g.rotY(180.0F);
      }

      VehGeo.wheel(g, R, w, rimR, spokes, left ? -spin : spin, "veh_tread", "veh_side", "veh_rim", caliper != null ? "veh_disc" : null, 10);
      if (caliper != null) {
         g.rotX(left ? 45.0F : -45.0F);
         float r0 = rimR * 0.5F;
         float r1 = rimR - 0.45F;
         VehGeo.box(g, caliper, -0.75F, r0, -0.7F, 0.45F, r1, 0.7F);
      }

      g.pop();
   }

   static void kart(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, VehGeo.sin(a.time * 45.0F) * 0.06F, 0.0F);
      String red = "veh_c:D81E1E";
      String yel = "veh_c:FFCD03";

      for (int sx = -1; sx <= 1; sx += 2) {
         g.push();
         g.translate(sx * 9.2F, 2.2F, 10.5F);
         g.rotY(a.lean * 20.0F);
         carWheel(g, a.wheel * 1.15F, sx < 0, 2.2F, 2.0F, 1.35F, 6, null);
         g.pop();
         g.push();
         g.translate(sx * 9.6F, 2.5F, -10.0F);
         carWheel(g, a.wheel, sx < 0, 2.5F, 3.0F, 1.55F, 6, null);
         g.pop();
      }

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.pipe(g, red, new float[][]{{s * 3.8F, 1.5F, -12.5F}, {s * 3.8F, 1.5F, 6.0F}, {s * 2.9F, 1.8F, 12.5F}, {s * 2.0F, 2.5F, 15.6F}}, 0.38F, 6, true);
         VehGeo.pipe(g, red, new float[][]{{s * 3.8F, 1.6F, -5.5F}, {s * 8.8F, 1.9F, -5.2F}, {s * 9.0F, 1.9F, 5.2F}, {s * 3.8F, 1.6F, 6.5F}}, 0.32F, 6, false);
         VehGeo.pipe(g, "veh_gun", new float[][]{{s * 3.2F, 1.7F, 10.5F}, {s * 8.0F, 2.2F, 10.5F}}, 0.3F, 6, false);
         VehGeo.tube(g, "veh_gun", VehGeo.p(s * 8.1F, 1.2F, 10.7F), VehGeo.p(s * 8.1F, 3.3F, 10.3F), 0.28F, 0.28F, 6, true);
      }

      for (float z : new float[]{-12.0F, -6.0F, 3.0F, 12.0F}) {
         VehGeo.tube(g, red, VehGeo.p(-3.8F, z == 12.0F ? 1.8F : 1.5F, z), VehGeo.p(3.8F, z == 12.0F ? 1.8F : 1.5F, z), 0.3F, 0.3F, 6, false);
      }

      VehGeo.pipe(g, red, new float[][]{{-2.0F, 2.5F, 15.6F}, {2.0F, 2.5F, 15.6F}}, 0.38F, 6, false);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-10.9F, 2.5F, -10.0F), VehGeo.p(10.9F, 2.5F, -10.0F), 0.42F, 0.42F, 8, true);
      VehGeo.pipe(g, "veh_chrome", new float[][]{{-7.0F, 2.2F, -12.5F}, {-7.0F, 2.8F, -15.0F}, {7.0F, 2.8F, -15.0F}, {7.0F, 2.2F, -12.5F}}, 0.36F, 6, true);
      VehGeo.box(g, "veh_alu", -3.8F, 1.32F, -6.2F, 3.8F, 1.6F, 13.0F);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, yel, s < 0 ? -8.9F : 5.0F, 1.6F, -6.6F, s < 0 ? -5.0F : 8.9F, 4.3F, 5.6F, 1.1F);
      }

      VehGeo.rbox(g, yel, -5.8F, 1.7F, 11.6F, 5.8F, 4.4F, 17.2F, 1.5F);
      VehGeo.rbox(g, yel, -3.2F, 1.7F, 11.2F, 3.2F, 7.0F, 12.4F, 0.4F);
      VehGeo.box6(g, "veh_flat:F4F4F4", new String[]{"veh_num", null, null, null, null, null}, -2.3F, 2.3F, 12.3F, 2.3F, 6.4F, 12.65F);
      VehGeo.rbox(g, "veh_flat:1A1B20", -3.6F, 1.45F, -5.6F, 3.6F, 2.6F, 1.6F, 0.6F);
      g.push();
      g.translate(0.0F, 2.0F, -5.2F);
      g.rotX(-14.0F);
      VehGeo.rbox(g, "veh_flat:1A1B20", -3.8F, 0.0F, -1.3F, 3.8F, 9.6F, 0.1F, 0.6F);
      g.pop();

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, "veh_flat:1A1B20", s < 0 ? -4.3F : 3.4F, 1.6F, -5.6F, s < 0 ? -3.4F : 4.3F, 5.4F, 0.8F, 0.35F);
      }

      VehGeo.tube(g, "veh_gun", VehGeo.p(0.0F, 3.0F, 12.0F), VehGeo.p(0.0F, 7.3F, 8.8F), 0.3F, 0.3F, 6, false);
      g.push();
      g.translate(0.0F, 7.5F, 8.6F);
      g.rotX(-52.0F);
      g.rotY(-a.lean * 80.0F);
      VehGeo.torus(g, "veh_flat:15151A", 2.6F, 0.36F, 16, 6);
      VehGeo.beam(g, "veh_gun", VehGeo.p(-2.5F, 0.0F, 0.0F), VehGeo.p(2.5F, 0.0F, 0.0F), 0.5F, 0.3F);
      VehGeo.lathe(g, new String[]{"veh_gun", yel}, new float[][]{{0.8F, -0.3F}, {0.8F, 0.2F}, {0.01F, 0.35F}}, 8);
      g.pop();
      g.push();
      g.translate(0.0F, 2.4F, 13.4F);
      VehGeo.lathe(
         g, "veh_flat:E8E8E0", new float[][]{{0.01F, -0.9F}, {1.3F, -0.8F}, {1.4F, 1.2F}, {0.9F, 1.6F}, {0.5F, 1.7F}, {0.5F, 2.2F}, {0.01F, 2.2F}}, 10
      );
      g.pop();
      VehGeo.rbox(g, "veh_gun", 4.3F, 1.8F, -10.2F, 7.6F, 5.4F, -6.2F, 0.5F);

      for (int i = 0; i < 5; i++) {
         VehGeo.box(g, "veh_alu", 4.0F, 5.4F + i * 0.55F, -9.4F, 7.9F, 5.65F + i * 0.55F, -6.9F);
      }

      VehGeo.tube(g, "veh_alu", VehGeo.p(5.9F, 5.2F, -8.2F), VehGeo.p(5.9F, 8.3F, -8.2F), 0.9F, 0.9F, 8, true);
      g.push();
      g.translate(8.0F, 4.6F, -7.4F);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, new String[]{red, "veh_flat:222228", red}, new float[][]{{0.01F, -0.1F}, {1.3F, -0.1F}, {1.3F, 2.2F}, {0.01F, 2.2F}}, 10);
      g.pop();
      VehGeo.pipe(g, "veh_chrome", new float[][]{{5.9F, 6.6F, -10.4F}, {5.9F, 6.6F, -12.2F}, {4.6F, 5.4F, -13.6F}, {2.6F, 4.6F, -13.8F}}, 0.35F, 6, false);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(3.0F, 4.6F, -13.8F), VehGeo.p(-2.4F, 4.6F, -13.8F), 0.8F, 0.8F, 8, true);
      g.push();
      g.translate(4.9F, 2.5F, -10.0F);
      g.rotZ(-90.0F);
      VehGeo.lathe(g, "veh_gun", new float[][]{{0.01F, -0.15F}, {1.6F, -0.15F}, {1.6F, 0.15F}, {0.01F, 0.15F}}, 12);
      g.pop();
      VehGeo.strip(g, "veh_chain", new float[][]{{4.9F, 4.1F, -10.0F}, {4.9F, 3.4F, -8.2F}}, 0.25F, 0.5F, VehGeo.p(1.0F, 0.0F, 0.0F), true);
      VehGeo.tube(g, "veh_alu", VehGeo.p(-7.0F, 2.8F, -14.6F), VehGeo.p(-7.0F, 17.5F, -14.6F), 0.18F, 0.18F, 6, true);

      for (int i = 0; i < 6; i++) {
         float x0 = -6.8F + i * 0.9F;
         float x1 = x0 + 0.9F;
         float w0 = VehGeo.sin(a.time * 7.0F - i * 0.9) * 0.35F * i / 5.0F;
         g.push();
         g.translate(0.0F, 0.0F, w0);
         VehGeo.box6(g, "veh_checker", null, x0, 13.8F - i * 0.05F, -14.65F, x1, 17.2F - i * 0.05F, -14.55F);
         g.pop();
      }

      g.pop();
   }
}
