package dev.lego.cosmetic;

import java.util.Random;

final class VehBoard {
   static final float SKATE_Y = 2.5F;
   static final float SKATE_TH = 0.55F;
   static final float SKATE_TOP = 3.05F;
   static final float HOVER_Y = 3.8F;
   static final float HOVER_TH = 1.3F;
   static final float HOVER_TOP = 5.1F;
   static final float UFO_Y = 3.0F;
   static final float UFO_DECK = 3.4F;
   static final float UFO_TOP = 6.4F;
   static final float CLOUD_Y = 3.5F;
   static final float CLOUD_TOP = 6.0F;

   private VehBoard() {
   }

   private static float skW(float z) {
      float az = Math.abs(z);
      if (az <= 10.0F) {
         return 3.6F;
      } else {
         float t = (az - 10.0F) / 3.05F;
         return 3.6F * (float)Math.sqrt(Math.max(0.015F, 1.0F - t * t));
      }
   }

   private static float skKick(float z) {
      float az = Math.abs(z);
      if (az <= 8.4F) {
         return 0.0F;
      } else {
         float t = (az - 8.4F) / 4.6F;
         return (z > 0.0F ? 2.3F : 2.0F) * t * t;
      }
   }

   private static float concave(float x) {
      float t = x / 3.6F;
      return 0.32F * t * t;
   }

   static void skateboard(G g, Cos.A a) {
      g.push();
      float tilt = -a.lean * 6.0F;

      for (int s = -1; s <= 1; s += 2) {
         truck(g, a, s * 8.3F, s * a.lean * 9.0F);
      }

      g.translate(0.0F, 1.6F, 0.0F);
      g.rotZ(tilt);
      g.translate(0.0F, -1.6F, 0.0F);
      int m = 6;
      VehGeo.loft(g, VehGeo.range(-12.97F, 12.97F, 0.9F), zx -> {
         float w = skW(zx);
         float y0 = 2.5F + skKick(zx);
         float[][] r = new float[14][];

         for (int i = 0; i <= 6; i++) {
            float xxx = -w + 2.0F * w * i / 6.0F;
            r[i] = new float[]{xxx, y0 + concave(xxx)};
         }

         for (int i = 0; i <= 6; i++) {
            float xx = w - 2.0F * w * i / 6.0F;
            r[7 + i] = new float[]{xx, y0 + 0.55F + concave(xx)};
         }

         return r;
      }, (k, j, zx, pa, pb) -> j < 6 ? "veh_deckart" : (j != 6 && j != 13 ? "veh_griptape" : "veh_c:D9A86A"), true, true, -3.6F, 3.6F, true);

      for (int s = -1; s <= 1; s += 2) {
         for (int bx = -1; bx <= 1; bx += 2) {
            for (int bz = -1; bz <= 1; bz += 2) {
               float x = bx * 0.75F;
               float z = s * 8.3F + bz * 0.8F;
               float y = 3.05F + concave(x);
               VehGeo.rbox(g, "veh_chrome", x - 0.2F, y - 0.12F, z - 0.2F, x + 0.2F, y + 0.12F, z + 0.2F, 0.07F);
            }
         }
      }

      for (int s = -1; s <= 1; s += 2) {
         float z = s * 8.3F;
         VehGeo.rbox(g, "veh_alu", -1.25F, 2.05F, z - 1.3F, 1.25F, 2.58F, z + 1.3F, 0.2F);
         g.push();
         g.translate(0.0F, 1.55F, z - s * 0.25F);
         VehGeo.lathe(g, "veh_c:FFCD03", new float[][]{{0.01F, -0.3F}, {0.5F, -0.3F}, {0.55F, 0.0F}, {0.5F, 0.55F}, {0.01F, 0.55F}}, 10);
         g.pop();
      }

      g.pop();
   }

   static void truck(G g, Cos.A a, float z, float yaw) {
      g.push();
      g.translate(0.0F, 1.2F, z);
      g.rotY(yaw);
      VehGeo.beam(g, "veh_alu", VehGeo.p(-2.7F, 0.0F, 0.0F), VehGeo.p(2.7F, 0.0F, 0.0F), 0.62F, 0.7F);
      VehGeo.rbox(g, "veh_alu", -1.05F, -0.45F, -0.5F, 1.05F, 0.62F, 0.5F, 0.22F);
      VehGeo.tube(g, "veh_chrome", VehGeo.p(-4.0F, 0.0F, 0.0F), VehGeo.p(4.0F, 0.0F, 0.0F), 0.16F, 0.16F, 6, true);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(s * 3.35F, 0.0F, 0.0F);
         if (s < 0) {
            g.rotY(180.0F);
         }

         g.rotX(s < 0 ? -a.wheel * 2.3F : a.wheel * 2.3F);
         g.rotZ(-90.0F);
         VehGeo.lathe(
            g,
            new String[]{
               "veh_gun", "veh_gun", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_urethane", "veh_gun", "veh_gun"
            },
            new float[][]{
               {0.01F, -0.5F},
               {0.45F, -0.5F},
               {0.5F, -0.56F},
               {1.0F, -0.56F},
               {1.17F, -0.38F},
               {1.2F, 0.0F},
               {1.17F, 0.38F},
               {1.0F, 0.56F},
               {0.5F, 0.56F},
               {0.45F, 0.5F},
               {0.01F, 0.5F}
            },
            14
         );
         g.pop();
      }

      g.pop();
   }

   private static float hvW(float z) {
      float az = Math.abs(z);
      if (az <= 8.5F) {
         return 4.0F;
      } else {
         float t = (az - 8.5F) / 4.55F;
         return 4.0F * (float)Math.sqrt(Math.max(0.02F, 1.0F - t * t));
      }
   }

   private static float hvKick(float z) {
      if (z > 7.5F) {
         float t = (z - 7.5F) / 5.5F;
         return 1.5F * t * t;
      } else if (z < -8.5F) {
         float t = (-8.5F - z) / 4.5F;
         return 0.7F * t * t;
      } else {
         return 0.0F;
      }
   }

   static void hoverboard(G g, Cos.A a) {
      float bob = Vehicles.hoverBob(a.time);
      g.push();
      g.translate(0.0F, 3.8F + bob, 0.0F);
      g.rotZ(-a.lean * 10.0F);
      g.rotX(-a.move * 4.0F);
      VehGeo.loft(
         g,
         VehGeo.range(-13.0F, 13.0F, 0.8F),
         z -> {
            float w = hvW(z);
            float y = hvKick(z);
            float e = Math.min(0.6F, w * 0.4F);
            return new float[][]{
               {-w + e, y},
               {0.0F, y},
               {w - e, y},
               {w, y + 0.45F},
               {w, y + 0.85F},
               {w - e, y + 1.3F},
               {0.0F, y + 1.3F},
               {-w + e, y + 1.3F},
               {-w, y + 0.85F},
               {-w, y + 0.45F}
            };
         },
         (k, j, z, pa, pb) -> j < 2 ? "veh_c:2A2D3A" : (j >= 5 && j <= 6 ? "veh_paint:FF3EA5" : "veh_c:1B1D26"),
         true,
         true,
         1.8F,
         -0.2F
      );
      float pulse = 0.75F + 0.25F * VehGeo.sin(a.time * 5.0F);
      g.glow(true).color(VehGeo.argb((int)(255.0F * pulse), 16777215));
      VehGeo.loft(g, VehGeo.range(-13.2F, 13.2F, 0.8F), z -> {
         float w = hvW(z * 13.0F / 13.25F) + 0.22F;
         float y = hvKick(z * 13.0F / 13.25F);
         return new float[][]{{-w, y + 0.5F}, {w, y + 0.5F}, {w, y + 0.78F}, {-w, y + 0.78F}};
      }, (k, j, z, pa, pb) -> "veh_glow:24E0FF", true, true, 1.0F, 0.0F);
      g.glow(false).color(-1);

      for (int s = -1; s <= 1; s += 2) {
         VehGeo.rbox(g, "veh_rubber", -3.0F, 1.1999999F, s < 0 ? -7.6F : 2.0F, 3.0F, 1.5799999F, s < 0 ? -2.0F : 7.6F, 0.12F);
      }

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.translate(0.0F, 0.0F, s * 7);
         VehGeo.lathe(
            g,
            new String[]{"veh_gun", "veh_c:1B1D26", "veh_chrome", "veh_c:1B1D26"},
            new float[][]{{0.01F, -1.4F}, {1.8F, -1.25F}, {2.5F, -0.75F}, {2.7F, -0.2F}, {2.6F, 0.3F}},
            16
         );
         float fl = 0.7F + 0.3F * VehGeo.sin(a.time * 11.0F + s);
         g.glow(true).color(VehGeo.argb((int)(255.0F * fl), 16777215));
         VehGeo.lathe(g, "veh_glow:7CF7FF", new float[][]{{0.01F, -1.52F}, {1.5F, -1.52F}}, 16);
         g.color(VehGeo.argb((int)(150.0F * fl), 16777215));
         g.translate(0.0F, -1.2F, 0.0F);
         VehGeo.torus(g, "veh_glow:24E0FF", 2.15F, 0.22F, 20, 6);
         g.glow(false).color(-1);
         g.pop();
      }

      g.pop();
   }

   static void ufo(G g, Cos.A a) {
      float bob = Vehicles.hoverBob(a.time) * 1.4F;
      g.push();
      g.translate(0.0F, 3.0F + bob, 0.0F);
      g.rotZ(-a.lean * 8.0F);
      String[] hullTex = new String[]{
         "veh_gun", "veh_gun", "veh_ufo", "veh_ufo", "veh_chrome", "veh_chrome", "veh_ufo", "veh_ufo", "veh_ufo", "veh_flat:2A2D36"
      };
      VehGeo.lathe(
         g,
         hullTex,
         new float[][]{
            {0.01F, -3.0F},
            {4.0F, -2.9F},
            {10.0F, -2.2F},
            {15.0F, -1.1F},
            {19.3F, -0.3F},
            {20.0F, 0.25F},
            {19.4F, 0.9F},
            {15.0F, 2.2F},
            {11.6F, 3.1F},
            {10.9F, 3.4F},
            {0.01F, 3.4F}
         },
         30
      );
      g.glow(true);
      int n = 14;

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n + a.time * 1.5;
         float on = 0.5F + 0.5F * VehGeo.sin(a.time * 6.0F + i);
         g.color(VehGeo.argb((int)(150.0F + 105.0F * on), 16777215));
         VehGeo.ellipsoid(
            g, i % 2 == 0 ? "veh_glow:7CFF6B" : "veh_glow:FFE14D", (float)Math.cos(an) * 19.1F, 0.4F, (float)Math.sin(an) * 19.1F, 0.75F, 0.55F, 0.75F, 8, 5
         );
      }

      g.color(-1);
      g.push();
      g.translate(0.0F, -2.0F, 0.0F);
      VehGeo.torus(g, "veh_glow:7CFF6B", 10.3F, 0.32F, 28, 5);
      g.pop();
      g.color(-1711276033);
      VehGeo.lathe(g, "veh_glow:7CFF6B", new float[][]{{0.01F, -3.1F}, {3.6F, -3.1F}}, 20);
      g.glow(false).color(-1);

      for (int i = 0; i < 3; i++) {
         double an = (Math.PI * 2) * i / 3.0 + 0.5;
         VehGeo.ellipsoid(g, "veh_chrome", (float)Math.cos(an) * 13.5F, -1.5F, (float)Math.sin(an) * 13.5F, 1.3F, 0.7F, 1.3F, 10, 5);
      }

      g.push();
      g.translate(0.0F, 3.4F, 5.6F);
      VehGeo.rbox(g, "veh_gun", -2.6F, -0.1F, -0.8F, 2.6F, 9.0F, 0.8F, 0.4F);
      g.translate(0.0F, 9.0F, 0.0F);
      g.rotX(-30.0F);
      VehGeo.rbox(g, "veh_gun", -3.2F, -0.4F, -1.4F, 3.2F, 0.3F, 1.4F, 0.25F);
      g.glow(true);

      for (int i = 0; i < 5; i++) {
         float on = Math.floorMod((int)(a.time * 4.0F) + i * 3, 5) < 3 ? 1.0F : 0.35F;
         g.color(VehGeo.argb((int)(255.0F * on), 16777215));
         VehGeo.box(g, i % 2 == 0 ? "veh_glow:FF4B6E" : "veh_glow:7CF7FF", -2.4F + i * 1.0F, 0.25F, -0.4F, -1.8F + i * 1.0F, 0.45F, 0.3F);
      }

      g.glow(false).color(-1);
      g.pop();
      g.push();
      g.translate(0.0F, 3.5F, 0.0F);
      VehGeo.torus(g, "veh_chrome", 10.6F, 0.45F, 28, 6);
      g.pop();
      float d = 3.4F;
      VehGeo.lathe(
         g,
         "veh_glass",
         new float[][]{
            {10.4F, d - 0.2F},
            {11.0F, d + 2.0F},
            {10.7F, d + 4.5F},
            {9.9F, d + 6.8F},
            {9.6F, d + 7.0F},
            {9.4F, d + 6.6F},
            {10.1F, d + 4.4F},
            {10.3F, d + 2.0F},
            {9.8F, d - 0.15F}
         },
         30
      );
      g.push();
      g.translate(0.0F, d + 6.95F, 0.0F);
      VehGeo.torus(g, "veh_chrome", 9.75F, 0.28F, 30, 5);
      g.pop();
      VehGeo.tube(g, "veh_chrome", VehGeo.p(0.0F, d + 6.9F, -9.75F), VehGeo.p(0.0F, d + 13.5F, -11.0F), 0.16F, 0.1F, 6, false);
      g.glow(true).color(VehGeo.argb((int)(160.0F + 95.0F * VehGeo.sin(a.time * 8.0F)), 16777215));
      VehGeo.ball(g, "veh_glow:FF4B6E", 0.0F, d + 13.7F, -11.05F, 0.5F, 8, 5);
      g.glow(false).color(-1);
      g.pop();
   }

   static void cloud(G g, Cos.A a) {
      g.push();
      g.translate(0.0F, 3.5F + Vehicles.cloudBob(a.time), 0.0F);
      PetGeo.blob2(g, "veh_cloud", 0.0F, 0.0F, 0.0F, 8.5F, 2.6F, 1.4F, 10.5F, 16, 8);
      Random r = new Random(7L);
      int n = 9;

      for (int i = 0; i < n; i++) {
         double an = (Math.PI * 2) * i / n + r.nextFloat() * 0.3;
         float d = 9.0F + r.nextFloat() * 2.5F;
         float s = 3.8F + r.nextFloat() * 1.8F;
         float wob = VehGeo.sin(a.time * 1.2 + i) * 0.25F;
         PetGeo.blob2(g, "veh_cloud", (float)Math.cos(an) * d * 0.8F, 0.3F + wob, (float)Math.sin(an) * d, s, s * 0.8F, s * 0.35F, s, 12, 7);
      }

      for (int i = 0; i < 4; i++) {
         double an = (Math.PI * 2) * i / 4.0 + 0.7;
         float wob = VehGeo.sin(a.time * 1.5 + i * 2) * 0.2F;
         PetGeo.blob2(g, "veh_cloud", (float)Math.cos(an) * 8.5F, 2.2F + wob, (float)Math.sin(an) * 9.5F, 3.2F, 2.6F, 0.8F, 3.2F, 10, 6);
      }

      for (int i = 0; i < 3; i++) {
         float z = -15.0F - i * 3.2F - a.time * 2.0F % 3.2F * a.move;
         float s = 2.2F - i * 0.55F;
         PetGeo.blob2(g, "veh_cloud", VehGeo.sin(a.time + i) * 1.5F, 0.6F, z, s, s * 0.8F, s * 0.5F, s, 10, 6);
      }

      g.pop();
   }
}
