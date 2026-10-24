package dev.lego.cosmetic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

final class Cos3Models {
   private Cos3Models() {
   }

   static void registerAll() {
      Cos.cape("c3void", "Leerenmantel", Cos.Rarity.LEGENDARY);
      Cos.cape("c3nether", "Netherglut", Cos.Rarity.EPIC);
      Cos.cape("c3heaven", "Himmelsgold", Cos.Rarity.LEGENDARY);
      Cos.cape("c3sunflower", "Sonnenblume", Cos.Rarity.RARE);
      Cos.cape("c3pumpkin", "Kürbislaterne", Cos.Rarity.EPIC);
      Cos.cape("c3ghost", "Geisterchen", Cos.Rarity.EPIC);
      Cos.cape("c3skull", "Leuchtschädel", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_purple", "Neonblitz Lila", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_blue", "Neonblitz Blau", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_green", "Neonblitz Grün", Cos.Rarity.EPIC);
      Cos.cape("c3bolt_orange", "Neonblitz Orange", Cos.Rarity.EPIC);
      Cos.cape("c3embertree", "Glutbaum", Cos.Rarity.LEGENDARY);
      Cos.add("hat_c3_crystalcrown", "Schwebende Kristallkrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::crystalCrown);
      Cos.add("hat_c3_planetring", "Planetenring", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::planetRing);
      Cos.add("hat_c3_rayhalo", "Strahlenkranz", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::rayHalo);
      Cos.add("hat_c3_flamecrown", "Flammenkrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, Cos3Models::flameCrown);
      Cos.add("hat_c3_bubble", "Blasenhelm", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::bubbleHelmet);
      Cos.add("hat_c3_catphones", "Katzen-Kopfhörer", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::catPhones);
      Cos.add("hat_c3_snowglobe", "Schneekugel", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::snowGlobe);
      Cos.add("hat_c3_pumpkin", "Kürbiskopf", Cos.Slot.HAT, Cos.Rarity.EPIC, Cos3Models::pumpkinHat);
      Cos.add("hat_c3_band_bolt", "Blitz-Bandana", Cos.Slot.HAT, Cos.Rarity.RARE, (g, a) -> bandana(g, a, 0));
      Cos.add("hat_c3_band_fire", "Flammen-Bandana", Cos.Slot.HAT, Cos.Rarity.RARE, (g, a) -> bandana(g, a, 1));
      Cos.add("hat_c3_band_ice", "Frost-Stirnband", Cos.Slot.HAT, Cos.Rarity.RARE, (g, a) -> bandana(g, a, 2));
      Cos.add("face_c3_holovisor", "Holo-Visier", Cos.Slot.FACE, Cos.Rarity.LEGENDARY, Cos3Models::holoVisor);
      Cos.add("face_c3_neonshades", "Neon-Shades", Cos.Slot.FACE, Cos.Rarity.EPIC, Cos3Models::neonShades);
      Cos3Back.register();
      Cos3Aura.register();
   }

   static void flame(G g, float h, float r, int outer, int inner) {
      boolean gl = g.glow;
      int c = g.color;
      g.glow(true);
      g.color(outer);
      Cos3Geo.lathe(
         g, "c3_flamegrad", new float[]{0.0F, r * 0.85F, r, r * 0.72F, r * 0.36F, 0.0F}, new float[]{0.0F, h * 0.09F, h * 0.24F, h * 0.48F, h * 0.74F, h}, 10
      );
      g.color(inner);
      Cos3Geo.lathe(g, "white", new float[]{0.0F, r * 0.5F, r * 0.58F, r * 0.4F, 0.0F}, new float[]{h * 0.04F, h * 0.12F, h * 0.26F, h * 0.46F, h * 0.68F}, 7);
      g.glow(gl);
      g.color(c);
   }

   static float[] loopPt(float c, float R, double a) {
      float ca = (float)Math.cos(a);
      float sa = (float)Math.sin(a);
      float sx = Math.abs(ca) < 1.0E-4F ? 0.0F : Math.signum(ca);
      float sz = Math.abs(sa) < 1.0E-4F ? 0.0F : Math.signum(sa);
      return new float[]{sx * c + R * ca, sz * c + R * sa};
   }

   static void flatRing(G g, String t, float r0, float r1, float y0, float y1, int seg) {
      Cos3Geo.lathe(g, t, new float[]{r0, r1, r1, r0, r0}, new float[]{y0, y0, y1, y1, y0}, seg);
   }

   static void sparkle(G g, float x, float y, float z, float s, int argb) {
      boolean gl = g.glow;
      int c = g.color;
      g.glow(true).color(argb);
      g.push();
      g.translate(x, y, z);
      Cos3Geo.gem(g, "white", s * 0.5F, s, s, 4);
      g.pop();
      g.glow(gl).color(c);
   }

   static void crystalCrown(G g, Cos.A a) {
      float t = a.time;
      float bob = Cos3Geo.sin(t * 2.0F) * 0.35F;
      g.push();
      g.translate(0.0F, 9.7F + bob, 0.0F);
      g.rotY(t * 16.0F);
      g.color(-11414);
      flatRing(g, "c3_metal", 4.3F, 5.0F, 0.0F, 0.75F, 24);
      g.color(-5720);
      g.push();
      g.translate(0.0F, 0.75F, 0.0F);
      g.torus("c3_metal", 4.65F, 0.2F, 24, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();

      for (int i = 0; i < 8; i++) {
         float ang = i * 45;
         boolean big = i % 2 == 0;
         g.push();
         g.rotY(ang);
         g.translate(4.65F, 0.55F, 0.0F);
         g.rotZ(big ? 8.0F : 14.0F);
         g.glow(true).color(big ? -527767297 : -523728129);
         Cos3Geo.shard(g, "c3_crystal", big ? 0.62F : 0.45F, big ? 3.8F : 2.4F);
         g.glow(false);
         g.pop();
         g.push();
         g.rotY(ang + 22.5F);
         g.translate(5.0F, 0.38F, 0.0F);
         g.rotZ(-90.0F);
         g.glow(true).color(i % 2 == 0 ? -46470 : -11867905);
         Cos3Geo.gem(g, "c3_crystal", 0.3F, 0.28F, 0.12F, 6);
         g.glow(false);
         g.pop();
      }

      g.push();
      g.translate(0.0F, 3.2F + Cos3Geo.sin(t * 2.6F) * 0.3F, 0.0F);
      g.rotY(-t * 50.0F);
      g.glow(true).color(-253168385);
      Cos3Geo.gem(g, "c3_crystal", 1.0F, 1.9F, 1.5F, 6);
      g.pop();
      Cos3Geo.halo(g, 0.0F, 3.2F, 0.0F, 4.5F, -2139432705);
      g.pop();

      for (int i = 0; i < 5; i++) {
         float ph = Cos3Geo.fract(t * 0.35F + i * 0.2F);
         float ang = i * 72 + t * 30.0F;
         float x = Cos3Geo.cos(Math.toRadians(ang)) * 5.8F;
         float z = Cos3Geo.sin(Math.toRadians(ang)) * 5.8F;
         sparkle(g, x, 9.2F + ph * 5.0F, z, 0.35F * (1.0F - ph) + 0.1F, Cos3Geo.alpha(1.0F - ph, 14218495));
      }
   }

   static void planetRing(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 5.2F, 0.0F);
      g.rotZ(16.0F);
      g.rotX(-9.0F);
      g.push();
      g.rotY(t * 12.0F);
      g.color(-1);
      g.ring("c3_ring", 6.7F, 9.8F, 0.14F, 48, 0.0F, 0.0F, 1.0F, 1.0F);
      g.ring("c3_ring", 6.7F, 9.8F, -0.14F, 48, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1519456);
      g.cylinder("white", 9.8F, 9.8F, -0.14F, 0.14F, 48, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.cylinder("white", 6.7F, 6.7F, -0.14F, 0.14F, 48, false, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(true).color(-931469057);
      g.torus("white", 6.45F, 0.09F, 40, 3, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.pop();
      String[] tex = new String[]{"c3_planet_gas", "c3_planet_ice", "c3_planet_lava"};
      float[] rad = new float[]{1.15F, 0.85F, 0.95F};
      float[] sp = new float[]{26.0F, -34.0F, 40.0F};
      float[] dist = new float[]{11.3F, 11.0F, 11.6F};

      for (int i = 0; i < 3; i++) {
         float ang = t * sp[i] + i * 120;
         g.push();
         g.rotY(ang);
         g.translate(dist[i], Cos3Geo.sin(t * 1.5F + i) * 0.4F, 0.0F);
         g.rotY(-t * 60.0F);
         g.color(-1);
         g.sphere(tex[i], rad[i], 14, 8, 0.0F, 0.0F, 1.0F, 1.0F);
         if (i == 2) {
            Cos3Geo.halo(g, 0.0F, 0.0F, 0.0F, rad[i] * 4.0F, 1627351594);
         }

         if (i == 0) {
            g.rotZ(20.0F);
            g.color(-3880);
            g.ring("c3_ring", rad[i] * 1.35F, rad[i] * 2.0F, 0.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
         }

         g.pop();
      }

      g.pop();
   }

   static void rayHalo(G g, Cos.A a) {
      float t = a.time;
      float bob = Cos3Geo.sin(t * 1.8F) * 0.3F;
      g.push();
      g.translate(0.0F, 11.8F + bob, -0.4F);
      g.rotX(8.0F);
      g.glow(true).color(-7558);
      g.torus("c3_metal", 4.3F, 0.42F, 32, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(-1325402416);
      g.torus("white", 5.3F, 0.1F, 32, 3, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 16; i++) {
         float ang = i * 22.5F + t * 14.0F;
         float len = 2.2F + 1.8F * (0.5F + 0.5F * Cos3Geo.sin(t * 3.0F + i * 1.7F)) + (i % 2 == 0 ? 1.2F : 0.0F);
         g.push();
         g.rotY(ang);
         g.color(Cos3Geo.alpha(0.75, i % 2 == 0 ? 16774320 : 16769162));
         limbRay(g, 4.85F, len, i % 2 == 0 ? 0.26F : 0.18F);
         g.pop();
      }

      g.glow(false);
      g.pop();

      for (int i = 0; i < 4; i++) {
         float ph = Cos3Geo.fract(t * 0.4F + i * 0.25F);
         float ang = i * 90 + t * 20.0F;
         sparkle(
            g,
            Cos3Geo.cos(Math.toRadians(ang)) * 5.6F,
            12.6F - ph * 2.5F,
            Cos3Geo.sin(Math.toRadians(ang)) * 5.6F - 0.4F,
            0.3F,
            Cos3Geo.alpha(1.0F - ph, 16774336)
         );
      }
   }

   private static void limbRay(G g, float r0, float len, float w) {
      Geo.tube(g, "c3_ray", new float[]{r0, 0.0F, 0.0F}, new float[]{r0 + len, 0.0F, 0.0F}, w, 0.02F, 4);
   }

   static void flameCrown(G g, Cos.A a) {
      float t = a.time;
      float c = 2.6F;
      float r = 2.25F;
      float th = 0.55F;
      g.color(-13355970);
      Cos3Geo.band(g, "c3_metal", c, r, th, 6.4F, 8.7F, 0.0F, 3.0F);
      g.color(-16310);
      Cos3Geo.band(g, "c3_metal", c, r + th - 0.05F, 0.2F, 6.2F, 6.65F, 0.0F, 3.0F);
      Cos3Geo.band(g, "c3_metal", c, r + th - 0.05F, 0.2F, 8.45F, 8.9F, 0.0F, 3.0F);

      for (int i = 0; i < 8; i++) {
         double ang = i * Math.PI / 4.0;
         float[] p = loopPt(c, r + th * 0.5F, ang);
         g.push();
         g.translate(p[0], 8.7F, p[1]);
         g.color(-14013902);
         Cos3Geo.shard(g, "c3_metal", 0.42F, 1.5F);
         g.pop();
         float fl = 1.0F + 0.2F * Cos3Geo.sin(t * 9.0F + i * 1.3F) + 0.1F * Cos3Geo.sin(t * 14.3F + i * 2.1F);
         g.push();
         g.translate(p[0], 9.6F, p[1]);
         g.rotZ(Cos3Geo.sin(t * 3.0F + i) * 6.0F);
         g.scale(1.0F, fl, 1.0F);
         flame(g, i % 2 == 0 ? 3.6F : 2.8F, 0.85F, -385910246, -251662176);
         g.pop();
         float[] q = loopPt(c, r + th + 0.05F, ang + (Math.PI / 8));
         g.push();
         g.translate(q[0], 7.55F, q[1]);
         g.rotY((float)(-Math.toDegrees(ang + (Math.PI / 8))));
         g.rotZ(-90.0F);
         g.glow(true).color(-50646);
         Cos3Geo.gem(g, "c3_crystal", 0.42F, 0.34F, 0.14F, 6);
         g.glow(false);
         g.pop();
      }

      for (int i = 0; i < 9; i++) {
         float ph = Cos3Geo.fract(t * 0.7F + i * 0.111F);
         float[] p = loopPt(c, r + th * 0.5F, i * 0.7);
         sparkle(
            g, p[0] * (1.0F - ph * 0.3F), 11.0F + ph * 5.0F, p[1] * (1.0F - ph * 0.3F), 0.28F, Cos3Geo.alpha((1.0F - ph) * 0.9, ph < 0.4 ? 16769146 : 16742954)
         );
      }
   }

   static void bubbleHelmet(G g, Cos.A a) {
      float t = a.time;
      g.color(-1512204);
      g.push();
      g.translate(0.0F, 0.75F, 0.0F);
      g.torus("c3_metal", 6.4F, 0.45F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int i = 0; i < 8; i++) {
         double an = i * Math.PI / 4.0 + (Math.PI / 8);
         boolean on = ((int)Math.floor(t * 3.0F) + i) % 4 != 0;
         g.glow(true).color(on ? (i % 2 == 0 ? -11867905 : -38184) : -14009788);
         Cos3Geo.ball(g, "white", (float)Math.cos(an) * 6.4F, 0.4F, (float)Math.sin(an) * 6.4F, 0.22F, 6);
      }

      g.glow(false);
      g.pop();
      int n = 11;
      float R = 7.4F;
      float cy = 4.0F;
      float[] rr = new float[n + 1];
      float[] yy = new float[n + 1];
      double p0 = Math.asin((0.75 - cy) / R);

      for (int i = 0; i <= n; i++) {
         double p = p0 + ((Math.PI / 2) - p0) * i / n;
         rr[i] = (float)Math.cos(p) * R;
         yy[i] = cy + (float)Math.sin(p) * R;
      }

      rr[n] = 0.0F;
      g.color(-3617064);
      Geo.tube(g, "c3_metal", new float[]{0.0F, 11.2F, 0.0F}, new float[]{0.0F, 13.6F, 0.0F}, 0.16F, 0.12F, 6);
      g.glow(true).color((int)(t * 2.0F) % 2 == 0 ? -46486 : -8775126);
      Cos3Geo.ball(g, "white", 0.0F, 13.8F, 0.0F, 0.42F, 8);
      Cos3Geo.halo(g, 0.0F, 13.8F, 0.0F, 2.2F, 1895778922);
      g.glow(false).color(-2558209);
      g.push();
      g.rotY(t * 8.0F);
      Cos3Geo.lathe(g, "c3_glass", rr, yy, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void catPhones(G g, Cos.A a) {
      float t = a.time;
      int neon = 0xFF000000 | Capes.hsv(0.88 + 0.08 * Math.sin(t * 1.5), 0.55, 1.0);
      float[][] half = new float[][]{{4.9F, 3.0F}, {4.95F, 6.0F}, {4.75F, 8.3F}, {3.4F, 9.2F}, {1.6F, 9.45F}, {0.0F, 9.5F}};
      float[][] pts = new float[half.length * 2 - 1][];

      for (int i = 0; i < half.length; i++) {
         pts[i] = new float[]{half[i][0], half[i][1], 0.0F};
         pts[pts.length - 1 - i] = new float[]{-half[i][0], half[i][1], 0.0F};
      }

      float[] rad = new float[pts.length];
      Arrays.fill(rad, 0.4F);
      g.color(-855818);
      Geo.chain(g, "c3_metal", pts, rad, 8);
      float[][] cp = new float[][]{{-3.0F, 9.35F, 0.0F}, {-1.5F, 9.55F, 0.0F}, {0.0F, 9.6F, 0.0F}, {1.5F, 9.55F, 0.0F}, {3.0F, 9.35F, 0.0F}};
      g.color(-12961210);
      Cos3Geo.limb(g, "c3_fur", cp, new float[]{0.5F, 0.55F, 0.55F, 0.55F, 0.5F}, 8);

      for (int s = -1; s <= 1; s += 2) {
         g.push();
         g.scale(s, 1.0F, 1.0F);
         g.color(-13355970);
         Cos3Geo.bbox(g, "c3_fur", 4.4F, 1.5F, -2.1F, 5.4F, 5.9F, 2.1F, 0.4F);
         g.color(-14112);
         Cos3Geo.bbox(g, "c3_metal", 5.3F, 1.2F, -2.4F, 6.6F, 6.2F, 2.4F, 0.55F);
         g.push();
         g.translate(6.62F, 3.7F, 0.0F);
         g.rotZ(90.0F);
         g.glow(true).color(neon);
         g.torus("white", 1.45F, 0.18F, 20, 4, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(-1);
         g.pop();
         g.glow(false).color(-1);
         g.push();
         g.translate(6.55F, 3.7F, 0.0F);
         g.scale(0.25F, 1.0F, 1.0F);
         g.sphere("c3_grille", 1.0F, 12, 6, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
         g.push();
         g.translate(2.7F, 9.1F, 0.0F);
         g.rotZ(-14.0F);
         float[][] ear = new float[][]{{-1.7F, 0.0F}, {1.6F, 0.0F}, {0.35F, 3.3F}};
         g.color(-855818);
         Cos3Geo.extrude(g, "c3_metal", ear, -0.5F, 0.5F);
         float[][] in = new float[][]{{-1.0F, 0.45F}, {0.95F, 0.45F}, {0.25F, 2.35F}};
         g.glow(true).color(neon);
         Cos3Geo.extrude(g, "white", in, 0.2F, 0.66F);
         Cos3Geo.extrude(g, "white", in, -0.66F, -0.2F);
         g.glow(false);
         g.pop();
         g.pop();
      }

      for (int i = 0; i < 2; i++) {
         float ph = Cos3Geo.fract(t * 0.45F + i * 0.5F);
         float s = i == 0 ? 1.0F : -1.0F;
         g.push();
         g.translate(s * (7.6F + ph * 1.5F), 5.0F + ph * 6.0F, Cos3Geo.sin(t * 2.0F + i) * 0.8F);
         g.glow(true).color(Cos3Geo.alpha(1.0F - ph, neon & 16777215));
         Cos3Geo.ball(g, "white", 0.0F, 0.0F, 0.0F, 0.42F, 6);
         Geo.tube(g, "white", new float[]{0.35F, 0.0F, 0.0F}, new float[]{0.35F, 1.6F, 0.0F}, 0.1F, 0.1F, 4);
         Cos3Geo.bbox(g, "white", 0.3F, 1.3F, -0.1F, 1.2F, 1.65F, 0.1F, 0.05F);
         g.glow(false);
         g.pop();
      }
   }

   static void snowGlobe(G g, Cos.A a) {
      float t = a.time;
      g.push();
      g.translate(0.0F, 8.6F, 0.0F);
      g.rotZ(Cos3Geo.sin(t * 1.4F) * 3.0F);
      g.rotX(Cos3Geo.sin(t * 1.1F + 1.0F) * 2.0F);
      g.color(-9814498);
      Cos3Geo.lathe(g, "c3_lacquer", new float[]{0.0F, 3.5F, 3.7F, 3.4F, 3.0F, 2.8F, 0.0F}, new float[]{0.0F, 0.0F, 0.8F, 1.5F, 1.9F, 2.3F, 2.3F}, 20);
      g.color(-11414);
      g.push();
      g.translate(0.0F, 1.55F, 0.0F);
      g.torus("c3_metal", 3.42F, 0.2F, 22, 4, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      float cy = 5.85F;
      float R = 3.8F;
      g.color(-722689);
      Geo.ellipsoid(g, "c3_fur", 0.0F, 3.25F, 0.0F, 2.55F, 0.75F, 2.55F, 14, 6);
      g.color(-13993398);
      Cos3Geo.lathe(g, "c3_fur", new float[]{0.0F, 1.7F, 0.0F}, new float[]{3.6F, 4.3F, 6.2F}, 10);
      g.color(-13397926);
      Cos3Geo.lathe(g, "c3_fur", new float[]{0.0F, 1.35F, 0.0F}, new float[]{5.2F, 5.8F, 7.4F}, 10);
      g.color(-12933016);
      Cos3Geo.lathe(g, "c3_fur", new float[]{0.0F, 0.95F, 0.0F}, new float[]{6.6F, 7.1F, 8.4F}, 10);
      g.push();
      g.translate(0.0F, 8.55F, 0.0F);
      g.rotY(t * 60.0F);
      g.glow(true).color(-7558);
      Cos3Geo.gem(g, "white", 0.38F, 0.4F, 0.3F, 5);
      g.pop();
      Cos3Geo.halo(g, 0.0F, 8.55F, 0.0F, 1.8F, -1862278534);

      for (int i = 0; i < 7; i++) {
         float yy = 4.6F + i * 0.55F;
         float rr = 1.65F - i * 0.2F;
         float an = i * 2.4F;
         boolean on = ((int)(t * 3.0F) + i) % 3 != 0;
         g.glow(true).color(on ? (i % 3 == 0 ? -46518 : (i % 3 == 1 ? -11867905 : -8118)) : -12566464);
         Cos3Geo.ball(g, "white", Cos3Geo.cos(an) * rr, yy, Cos3Geo.sin(an) * rr, 0.13F, 4);
      }

      g.glow(false);
      g.color(-1560502);
      Cos3Geo.bbox(g, "white", 1.2F, 3.3F, 0.5F, 2.0F, 4.1F, 1.3F, 0.08F);
      g.color(-7558);
      Cos3Geo.bbox(g, "white", 1.51F, 3.28F, 0.48F, 1.69F, 4.14F, 1.32F, 0.03F);
      Random rn = new Random(5L);
      g.color(-1);

      for (int i = 0; i < 26; i++) {
         float h = rn.nextFloat();
         float an = rn.nextFloat() * 6.28F + t * (0.6F + rn.nextFloat() * 0.6F);
         float y = 3.6F + Cos3Geo.fract(h - t * 0.12F) * 5.4F;
         float dy = y - cy;
         float maxR = (float)Math.sqrt(Math.max(0.2, (double)((R - 0.5F) * (R - 0.5F) - dy * dy)));
         float rr = maxR * (0.3F + 0.7F * rn.nextFloat());
         g.push();
         g.translate(Cos3Geo.cos(an) * rr, y, Cos3Geo.sin(an) * rr);
         g.rotY(an * 57.0F + t * 90.0F);
         g.box("white", -0.1F, -0.1F, -0.1F, 0.1F, 0.1F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1509121);
      g.push();
      g.translate(0.0F, cy, 0.0F);
      g.rotY(20.0F);
      g.sphere("c3_glass", R, 22, 12, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.pop();
   }

   static void pumpkinHat(G g, Cos.A a) {
      float t = a.time;
      float fl = 0.8F + 0.12F * Cos3Geo.sin(t * 11.0F) + 0.08F * Cos3Geo.sin(t * 17.3F);
      g.push();
      g.translate(0.0F, 8.4F, 0.0F);
      g.rotZ(6.0F);
      g.translate(0.0F, 2.95F, 0.0F);
      g.color(-30182);

      for (int i = 0; i < 8; i++) {
         g.push();
         g.rotY(-i * 45);
         g.color(i % 2 == 0 ? -30182 : -1017326);
         Geo.ellipsoid(g, "c3_pumpkin", 1.6F, 0.0F, 0.0F, 2.1F, 2.7F, 1.8F, 12, 8);
         g.pop();
      }

      g.color(-10847702);
      Cos3Geo.limb(
         g, "c3_fur", new float[][]{{0.0F, 2.3F, 0.0F}, {0.1F, 3.2F, 0.0F}, {0.5F, 3.9F, 0.1F}, {1.0F, 4.1F, 0.2F}}, new float[]{0.55F, 0.45F, 0.35F, 0.3F}, 7
      );
      g.color(-12940758);
      float[][] vine = new float[10][];

      for (int i = 0; i < 10; i++) {
         float an = i * 0.7F;
         float rr = 1.1F - i * 0.08F;
         vine[i] = new float[]{-0.3F - Cos3Geo.cos(an) * rr, 2.8F + i * 0.08F, Cos3Geo.sin(an) * rr};
      }

      float[] vr = new float[10];
      Arrays.fill(vr, 0.12F);
      Geo.chain(g, "white", vine, vr, 4);
      g.push();
      g.translate(0.5F, 2.9F, -0.6F);
      g.rotX(-70.0F);
      g.rotZ(30.0F);
      g.color(-11884486);
      Cos3Geo.extrude(g, "c3_fur", new float[][]{{0.0F, 0.0F}, {0.9F, 0.5F}, {1.8F, 0.1F}, {2.2F, -0.6F}, {1.1F, -0.7F}}, -0.08F, 0.08F);
      g.pop();
      g.glow(true).color(0xFF000000 | Capes.mix(16742928, 16771194, fl));
      float[][] eyeL = new float[][]{{0.5F, 0.2F}, {1.9F, 0.2F}, {1.2F, 1.35F}};
      float[][] eyeR = new float[][]{{-0.5F, 0.2F}, {-1.2F, 1.35F}, {-1.9F, 0.2F}};
      float[][] nose = new float[][]{{0.3F, -0.25F}, {0.0F, 0.25F}, {-0.3F, -0.25F}};
      float[][] mouth = new float[][]{
         {2.0F, -0.55F},
         {1.2F, -1.0F},
         {0.8F, -0.75F},
         {0.4F, -1.1F},
         {0.0F, -0.85F},
         {-0.4F, -1.1F},
         {-0.8F, -0.75F},
         {-1.2F, -1.0F},
         {-2.0F, -0.55F},
         {-1.1F, -1.75F},
         {0.0F, -1.95F},
         {1.1F, -1.75F}
      };
      Cos3Geo.extrude(g, "white", eyeL, 2.3F, 3.62F);
      Cos3Geo.extrude(g, "white", eyeR, 2.3F, 3.62F);
      Cos3Geo.extrude(g, "white", nose, 2.3F, 3.85F);
      Cos3Geo.extrude(g, "white", mouth, 2.3F, 3.72F);
      Cos3Geo.halo(g, 0.0F, 0.0F, 4.2F, 5.0F, Cos3Geo.alpha(0.35 * fl, 16751146));
      g.glow(false);
      g.pop();
   }

   static void bandana(G g, Cos.A a, int kind) {
      float t = a.time;
      String tex = kind == 0 ? "c3_band_bolt" : (kind == 1 ? "c3_band_fire" : "c3_band_ice");
      int glowC = kind == 0 ? -4653206 : (kind == 1 ? -20438 : -4656897);
      float c = 2.5F;
      float r = 2.4F;
      float th = 0.35F;
      float y0 = 4.5F;
      float y1 = 6.9F;
      g.color(-1);
      Cos3Geo.band(g, tex, c, r, th, y0, y1, 0.0F, 2.0F);
      float out = c + r + th;
      g.push();
      g.translate(0.0F, 5.75F, -out - 0.25F);
      g.rotZ(8.0F);
      Cos3Geo.bbox(g, tex, -0.85F, -0.8F, -0.45F, 0.85F, 0.8F, 0.45F, 0.35F, new float[]{0.1F, 0.2F, 0.2F, 0.8F});
      g.pop();
      float wind = a.preview ? 0.4F : 0.25F + a.move * 1.2F;

      for (int s = -1; s <= 1; s += 2) {
         int n = 7;
         float[][] yz = new float[n][];
         float[] w = new float[n];

         for (int i = 0; i < n; i++) {
            float f = (float)i / (n - 1);
            float flap = Cos3Geo.sin(t * (4.0F + wind * 4.0F) - f * 3.2F + (s > 0 ? 0.0F : 1.3F)) * (0.25F + wind * 0.5F) * f;
            float back = f * f * (1.2F + wind * 3.2F);
            yz[i] = new float[]{5.6F - f * (5.2F - wind * 1.8F), -out - 0.5F - f * 1.4F - back + flap};
            w[i] = 1.25F - f * 0.35F;
         }

         Cos3Geo.ribbon(g, tex, s * 0.72F, yz, w, 0.2F);
      }

      for (int e = 0; e < 3; e++) {
         double ang = (Math.PI / 2) + (e - 1) * 0.9;
         float[] p = loopPt(c, r + th - 0.1F, ang);
         g.push();
         g.translate(p[0], 5.7F, p[1]);
         g.rotY((float)Math.toDegrees((Math.PI / 2) - ang));
         float pul = 0.75F + 0.25F * Cos3Geo.sin(t * 4.0F + e * 2);
         g.glow(true).color(Cos3Geo.alpha(pul, glowC));
         if (kind == 0) {
            float[][] bolt = new float[][]{{0.35F, 1.0F}, {-0.45F, -0.05F}, {0.0F, -0.05F}, {-0.3F, -1.0F}, {0.5F, 0.2F}, {0.05F, 0.2F}, {0.5F, 1.0F}};
            Cos3Geo.extrude(g, "white", bolt, -0.1F, 0.28F);
         } else if (kind == 1) {
            g.push();
            g.translate(0.0F, -0.9F, 0.15F);
            g.scale(1.0F, 0.9F + 0.2F * Cos3Geo.sin(t * 10.0F + e), 1.0F);
            g.glow(false);
            flame(g, 1.9F, 0.5F, -520136166, -251666310);
            g.pop();
         } else {
            g.rotX(90.0F);
            Cos3Geo.shard(g, "c3_crystal", 0.28F, 0.9F);
            g.rotY(60.0F);
            g.push();
            g.rotZ(50.0F);
            Cos3Geo.shard(g, "c3_crystal", 0.2F, 0.7F);
            g.pop();
            g.push();
            g.rotZ(-50.0F);
            Cos3Geo.shard(g, "c3_crystal", 0.2F, 0.7F);
            g.pop();
         }

         g.glow(false);
         g.pop();
      }

      if (kind == 0) {
         int step = (int)Math.floor(t * 5.0F);

         for (int i = 0; i < 2; i++) {
            double h = Capes.hash(step, i, 17);
            if (!(h < 0.3)) {
               double a0 = Capes.hash(step, i, 18) * Math.PI * 2.0;
               double a1 = a0 + 0.5 + h * 0.8;
               float[] p0 = loopPt(c, r + th + 0.45F, a0);
               float[] p1 = loopPt(c, r + th + 0.45F, a1);
               Cos3Geo.arc(
                  g,
                  new float[]{p0[0], 5.7F + (float)(h - 0.5) * 1.5F, p0[1]},
                  new float[]{p1[0], 5.7F - (float)(h - 0.5) * 1.5F, p1[1]},
                  0.6F,
                  0.06F,
                  step * 3 + i,
                  t,
                  -1061617814
               );
            }
         }
      } else if (kind == 1) {
         for (int ix = 0; ix < 5; ix++) {
            float ph = Cos3Geo.fract(t * 0.8F + ix * 0.2F);
            float[] p = loopPt(c, r + th, (Math.PI / 2) + (ix - 2) * 0.55);
            sparkle(g, p[0], 7.2F + ph * 3.0F, p[1], 0.22F, Cos3Geo.alpha(1.0F - ph, ph < 0.3 ? 16769146 : 16738842));
         }
      } else {
         for (int ix = 0; ix < 6; ix++) {
            float ph = Cos3Geo.fract(t * 0.3F + ix / 6.0F);
            float an = ix * 60 + t * 25.0F;
            sparkle(
               g,
               Cos3Geo.cos(Math.toRadians(an)) * 6.6F,
               5.7F - ph * 2.5F + 1.2F,
               Cos3Geo.sin(Math.toRadians(an)) * 6.6F,
               0.25F,
               Cos3Geo.alpha(1.0F - ph, 15268607)
            );
         }
      }
   }

   static void arcBand(G g, String tex, float R, float n, float a0, float a1, float y0, float y1, float th, int seg) {
      float[][] in = new float[seg + 1][];
      float[][] out = new float[seg + 1][];

      for (int i = 0; i <= seg; i++) {
         double a = Math.toRadians(a0 + (a1 - a0) * i / seg);
         in[i] = Geo.sq(a, R, n);
         out[i] = Geo.sq(a, R + th, n);
      }

      for (int i = 0; i < seg; i++) {
         float ua = (float)i / seg;
         float ub = (float)(i + 1) / seg;
         g.quad(
            tex,
            new float[]{out[i][0], y1, out[i][1]},
            new float[]{out[i + 1][0], y1, out[i + 1][1]},
            new float[]{out[i + 1][0], y0, out[i + 1][1]},
            new float[]{out[i][0], y0, out[i][1]},
            new float[]{1.0F - ua, 0.0F, 1.0F - ub, 0.0F, 1.0F - ub, 1.0F, 1.0F - ua, 1.0F}
         );
         g.quad(
            tex,
            new float[]{in[i + 1][0], y1, in[i + 1][1]},
            new float[]{in[i][0], y1, in[i][1]},
            new float[]{in[i][0], y0, in[i][1]},
            new float[]{in[i + 1][0], y0, in[i + 1][1]},
            new float[]{1.0F - ub, 0.0F, 1.0F - ua, 0.0F, 1.0F - ua, 1.0F, 1.0F - ub, 1.0F}
         );
         g.quad(
            tex,
            new float[]{in[i][0], y1, in[i][1]},
            new float[]{in[i + 1][0], y1, in[i + 1][1]},
            new float[]{out[i + 1][0], y1, out[i + 1][1]},
            new float[]{out[i][0], y1, out[i][1]},
            new float[]{1.0F - ua, 0.0F, 1.0F - ub, 0.0F, 1.0F - ub, 0.05F, 1.0F - ua, 0.05F}
         );
         g.quad(
            tex,
            new float[]{out[i][0], y0, out[i][1]},
            new float[]{out[i + 1][0], y0, out[i + 1][1]},
            new float[]{in[i + 1][0], y0, in[i + 1][1]},
            new float[]{in[i][0], y0, in[i][1]},
            new float[]{1.0F - ua, 0.95F, 1.0F - ub, 0.95F, 1.0F - ub, 1.0F, 1.0F - ua, 1.0F}
         );
      }

      for (int e = 0; e <= seg; e += seg) {
         g.quad(
            tex,
            new float[]{in[e][0], y1, in[e][1]},
            new float[]{out[e][0], y1, out[e][1]},
            new float[]{out[e][0], y0, out[e][1]},
            new float[]{in[e][0], y0, in[e][1]},
            new float[]{0.0F, 0.0F, 0.05F, 0.0F, 0.05F, 1.0F, 0.0F, 1.0F}
         );
      }
   }

   static void holoVisor(G g, Cos.A a) {
      float t = a.time;
      float R = 4.95F;
      float n = 6.0F;
      g.glow(true).color(-1);
      arcBand(g, "c3_visor", R, n, -12.0F, 192.0F, 2.6F, 5.1F, 0.28F, 36);
      g.glow(false);

      for (int e = 0; e < 2; e++) {
         float y = e == 0 ? 5.18F : 2.52F;
         int seg = 28;
         float[][] pts = new float[seg + 1][];

         for (int i = 0; i <= seg; i++) {
            float[] p = Geo.sq(Math.toRadians(-14.0 + 208.0 * i / seg), R + 0.14F, n);
            pts[i] = new float[]{p[0], y, p[1]};
         }

         float[] rr = new float[seg + 1];
         Arrays.fill(rr, 0.17F);
         g.color(-14012872);
         Geo.chain(g, "c3_metal", pts, rr, 5);
      }

      for (int s = -1; s <= 1; s += 2) {
         g.color(-14012872);
         Cos3Geo.bbox(g, "c3_metal", s > 0 ? 4.45F : -5.75F, 2.2F, -1.9F, s > 0 ? 5.75F : -4.45F, 5.5F, 0.6F, 0.4F);

         for (int i = 0; i < 3; i++) {
            boolean on = ((int)(t * 4.0F) + i + (s > 0 ? 0 : 1)) % 3 != 0;
            g.glow(true).color(on ? -11867905 : -15054246);
            Cos3Geo.ball(g, "white", s * 5.8F, 4.6F - i * 0.8F, -0.6F, 0.2F, 5);
         }

         g.glow(false);
      }

      double sa = Math.toRadians(90.0 + 78.0 * Math.sin(t * 1.8));
      float[] sp = Geo.sq(sa, R + 0.33F, n);
      g.push();
      g.translate(sp[0], 3.85F, sp[1]);
      g.rotY((float)Math.toDegrees((Math.PI / 2) - sa));
      g.glow(true).color(-521601025);
      Cos3Geo.bbox(g, "white", -0.09F, -1.2F, -0.05F, 0.09F, 1.2F, 0.07F, 0.02F);
      g.pop();
      Cos3Geo.halo(g, sp[0] * 1.02F, 3.85F, sp[1] * 1.02F, 1.6F, 1615522047);
      g.push();
      g.translate(-2.0F, 3.9F, R + 0.95F);
      g.rotZ(t * 40.0F);
      g.glow(true).color(-1337267969);
      g.plane("c3_hex", 0.0F, 0.0F, 1.8F, 1.8F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.glow(false);
      g.pop();
   }

   static void neonShades(G g, Cos.A a) {
      float t = a.time;
      int neon = 0xFF000000 | Capes.hsv(0.8 + 0.12 * Math.sin(t * 1.2), 0.75, 1.0);
      int seg = 18;

      for (int s = -1; s <= 1; s += 2) {
         float cx = s * 2.1F;
         float cy = 3.75F;
         float[][] poly = new float[seg][];
         float[][] rim = new float[seg + 1][];

         for (int i = 0; i < seg; i++) {
            double an = (Math.PI * 2) * i / seg;
            float[] q = Geo.sq(an, 1.0F, 3.2F);
            float wx = 1.65F;
            float wy = q[1] < 0.0F ? 1.25F : 1.0F;
            float xx = q[0] * wx;
            float yy = q[1] * wy;
            if (s < 0) {
               xx = -xx;
            }

            poly[i] = new float[]{cx + xx, cy + yy};
            rim[i] = new float[]{cx + xx * 1.07F, cy + yy * 1.08F, 4.5F};
         }

         if (s < 0) {
            Collections.reverse(Arrays.asList(poly));
         }

         rim[seg] = rim[0];
         g.color(-520093697);
         Cos3Geo.extrude(g, "c3_lens", poly, 4.36F, 4.6F);
         float[] rr = new float[seg + 1];
         Arrays.fill(rr, 0.2F);
         g.glow(true).color(neon);
         Geo.chain(g, "white", rim, rr, 5);
         float[][] arm = new float[][]{{s * 3.85F, 4.35F, 4.45F}, {s * 4.5F, 4.35F, 3.7F}, {s * 4.52F, 4.3F, 0.0F}, {s * 4.52F, 3.6F, -2.2F}};
         Geo.chain(g, "white", arm, new float[]{0.19F, 0.18F, 0.16F, 0.14F}, 5);
         g.glow(false);
      }

      g.glow(true).color(neon);
      Geo.chain(g, "white", new float[][]{{0.45F, 4.25F, 4.5F}, {0.0F, 4.45F, 4.5F}, {-0.45F, 4.25F, 4.5F}}, new float[]{0.2F, 0.2F, 0.2F}, 5);
      g.glow(false);
      float ph = Cos3Geo.fract(t * 0.35F);
      if (ph < 0.3F) {
         float x = 4.0F - ph / 0.3F * 8.0F;
         sparkle(g, x, 4.3F, 4.75F, 0.35F, -520093697);
      }
   }
}
