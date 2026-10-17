package dev.lego.cosmetic;

/**
 * Halloween knives: own designs (karambit, butterfly, bayonet, talon, machete)
 * in three finishes. Drawn in the right hand (arm space, model units = pixels,
 * hand at about (1, -10.5, 0), blade pointing forward along +z). Purely visual.
 */
final class Knives {
   private Knives() {
   }

   /** blade colour (base), blade colour (tip), edge colour, glow edge. */
   private record Finish(int base, int tip, int edge, boolean glow, int handle) {
   }

   private static final Finish PUMPKIN = new Finish(0xFFFF7A1A, 0xFF7C3AED, 0xFFFFD9A8, false, 0xFF1B1820);
   private static final Finish BLOODMOON = new Finish(0xFF2A0A0E, 0xFF9E0F1F, 0xFFFF3348, true, 0xFF120A0C);
   private static final Finish GHOST = new Finish(0xFFB8E8FF, 0xFFEFFAFF, 0xFF8FE3FF, true, 0xFF2A3440);

   static void register() {
      String[][] types = {{"karambit", "Karambit"}, {"butterfly", "Butterfly"}, {"bayonet", "Bajonett"}, {"talon", "Talon"}, {"machete", "Machete"}};
      Object[][] finishes = {{"pumpkin", "Kürbis-Fade", PUMPKIN, Cos.Rarity.EPIC}, {"bloodmoon", "Blutmond", BLOODMOON, Cos.Rarity.LEGENDARY}, {"ghost", "Geisterglas", GHOST, Cos.Rarity.LEGENDARY}};
      for (String[] t : types) {
         for (Object[] f : finishes) {
            final String type = t[0];
            final Finish fin = (Finish)f[2];
            Cos.add("knife_" + type + "_" + f[0], t[1] + " | " + f[1], Cos.Slot.KNIFE, (Cos.Rarity)f[3], (g, a) -> draw(g, a, type, fin));
         }
      }
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   static void draw(G g, Cos.A a, String type, Finish f) {
      g.push();
      g.translate(1.0F, -10.6F, 0.0F);
      // A gentle idle sway; previews spin slowly.
      g.rotX(-8.0F + (float)Math.sin(a.time * 1.6F) * 2.0F);
      if (a.preview) g.rotY(a.time * 40.0F);
      switch (type) {
         case "karambit" -> karambit(g, f);
         case "butterfly" -> butterfly(g, f);
         case "bayonet" -> bayonet(g, f);
         case "talon" -> talon(g, f);
         default -> machete(g, f);
      }
      g.pop();
   }

   private static void handle(G g, Finish f, float z0, float z1, float w, float h) {
      g.glow(false).color(f.handle());
      g.box("white", -w / 2.0F, -h / 2.0F, z0, w / 2.0F, h / 2.0F, z1, 0.0F, 0.0F, 1.0F, 1.0F);
      // Grip grooves.
      g.color(0xFF000000 | ((f.handle() & 0xFEFEFE) >> 1));
      for (float z = z0 + 0.6F; z < z1 - 0.3F; z += 0.8F) {
         g.box("white", -w / 2.0F - 0.03F, -h / 2.0F - 0.03F, z, w / 2.0F + 0.03F, h / 2.0F + 0.03F, z + 0.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   /** Blade as a swept, flat, tapering profile along a path; two colour halves for the fade. */
   private static void blade(G g, Finish f, float[][] path, float height, float thick) {
      int n = path.length;
      float[] w = new float[n], h = new float[n], e = new float[n];
      for (int i = 0; i < n; i++) {
         float k = (float)i / (n - 1);
         w[i] = thick * (1.0F - k * 0.5F);
         h[i] = Math.max(0.05F, height * (1.0F - k * k * 0.9F));
         e[i] = 1.2F;
      }
      int mid = n / 2;
      float[][] a = java.util.Arrays.copyOfRange(path, 0, mid + 1);
      float[][] b = java.util.Arrays.copyOfRange(path, mid, n);
      g.glow(false).color(f.base());
      XmasGeo.sweep(g, "white", a, java.util.Arrays.copyOfRange(w, 0, mid + 1), java.util.Arrays.copyOfRange(h, 0, mid + 1), java.util.Arrays.copyOfRange(e, 0, mid + 1), 6, 1.0F);
      g.color(f.tip());
      XmasGeo.sweep(g, "white", b, java.util.Arrays.copyOfRange(w, mid, n), java.util.Arrays.copyOfRange(h, mid, n), java.util.Arrays.copyOfRange(e, mid, n), 6, 1.0F);
      // Sharpened edge: a thin bright strip along the lower side.
      float[][] edge = new float[n][];
      float[] ew = new float[n], eh = new float[n];
      for (int i = 0; i < n; i++) {
         edge[i] = p(path[i][0], path[i][1] - h[i] * 0.85F, path[i][2]);
         ew[i] = w[i] * 0.35F;
         eh[i] = Math.max(0.04F, h[i] * 0.16F);
      }
      g.glow(f.glow()).color(f.edge());
      XmasGeo.sweep(g, "white", edge, ew, eh, e, 4, 1.0F);
      g.glow(false);
   }

   private static void karambit(G g, Finish f) {
      handle(g, f, -2.6F, 1.4F, 0.9F, 1.2F);
      // Finger ring at the back.
      float[][] ring = new float[13][];
      float[] rw = new float[13], rh = new float[13];
      for (int i = 0; i <= 12; i++) {
         double t = Math.PI * 2 * i / 12;
         ring[i] = p(0.0F, (float)Math.sin(t) * 1.1F, -3.5F + (float)Math.cos(t) * 1.1F);
         rw[i] = 0.32F;
         rh[i] = 0.32F;
      }
      g.color(f.handle());
      XmasGeo.sweep(g, "white", ring, rw, rh, null, 6, 1.0F);
      // Hooked blade curving down.
      float[][] path = new float[9][];
      for (int i = 0; i < 9; i++) {
         double t = i / 8.0;
         path[i] = p(0.0F, (float)(-Math.sin(t * Math.PI * 0.7) * 2.6), 1.4F + (float)(Math.sin(t * Math.PI * 0.55) * 5.2));
      }
      blade(g, f, path, 0.9F, 0.22F);
   }

   private static void butterfly(G g, Finish f) {
      // Two handle halves with holes look; pivot pins.
      g.glow(false).color(f.handle());
      g.box("white", -0.7F, -0.6F, -4.0F, -0.1F, 0.6F, 0.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("white", 0.1F, -0.6F, -4.0F, 0.7F, 0.6F, 0.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(0xFF9CA3AF);
      g.box("white", -0.8F, -0.2F, 0.3F, 0.8F, 0.2F, 0.7F, 0.0F, 0.0F, 1.0F, 1.0F);
      float[][] path = new float[7][];
      for (int i = 0; i < 7; i++) path[i] = p(0.0F, 0.0F, 0.7F + i * 1.1F);
      blade(g, f, path, 0.85F, 0.2F);
   }

   private static void bayonet(G g, Finish f) {
      handle(g, f, -4.2F, 0.0F, 1.0F, 1.3F);
      g.glow(false).color(0xFF6B7280);
      g.box("white", -0.55F, -1.2F, 0.0F, 0.55F, 1.2F, 0.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      float[][] path = new float[9][];
      for (int i = 0; i < 9; i++) path[i] = p(0.0F, 0.1F, 0.45F + i * 1.15F);
      blade(g, f, path, 1.05F, 0.24F);
   }

   private static void talon(G g, Finish f) {
      handle(g, f, -3.2F, 0.2F, 0.9F, 1.2F);
      float[][] path = new float[9][];
      for (int i = 0; i < 9; i++) {
         double t = i / 8.0;
         path[i] = p(0.0F, (float)(-t * t * 1.6), 0.2F + (float)(t * 7.6));
      }
      blade(g, f, path, 1.0F, 0.22F);
   }

   private static void machete(G g, Finish f) {
      handle(g, f, -4.0F, 0.0F, 1.0F, 1.3F);
      float[][] path = new float[10][];
      for (int i = 0; i < 10; i++) {
         double t = i / 9.0;
         path[i] = p(0.0F, (float)(0.2 + Math.sin(t * Math.PI) * 0.4), (float)(t * 11.0));
      }
      blade(g, f, path, 1.7F, 0.2F);
   }
}
