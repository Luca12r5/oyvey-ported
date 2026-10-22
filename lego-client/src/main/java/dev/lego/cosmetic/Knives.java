package dev.lego.cosmetic;

/**
 * Knives in the style of modern shooter knives, all modelled here from real knife shapes (no
 * game assets): 21 shapes × 8 own finishes. Drawn in the right hand (arm space, model units =
 * pixels, hand at about (1, -10.6, 0), blade pointing forward along +z, blade flat in the y-z
 * plane). Purely visual. Folding knives (butterfly, flip, navaja, stiletto, falchion) have moving
 * parts driven by {@link Cos.A#knifeOpen}; see {@link KnifeTricks} for the animations.
 *
 * The ids must match packages/shared/src/client-cosmetics.ts (KNIFE_TYPES × KNIFE_FINISHES).
 */
final class Knives {
   private Knives() {
   }

   /** One finish: colour of the blade at (u along the blade 0..1, v spine→edge 0..1). */
   interface Paint {
      int at(float u, float v);
   }

   record Finish(String id, String name, Cos.Rarity rarity, Paint paint, int edge, boolean glow, int handle, int handle2) {
   }

   static final String[][] TYPES = {
      {"karambit", "Karambit"}, {"butterfly", "Butterfly"}, {"m9", "M9-Bajonett"}, {"bayonet", "Bajonett"},
      {"flip", "Klappmesser"}, {"gut", "Gut-Hook"}, {"huntsman", "Huntsman"}, {"falchion", "Falchion"},
      {"bowie", "Bowie"}, {"daggers", "Push-Dolche"}, {"navaja", "Navaja"}, {"stiletto", "Stilett"},
      {"talon", "Talon"}, {"ranger", "Ranger"}, {"skeleton", "Skelett"}, {"nomad", "Nomade"},
      {"survival", "Survival"}, {"paracord", "Paracord"}, {"classic", "Klassik"}, {"kukri", "Kukri"},
      {"machete", "Machete"}
   };

   static final Finish[] FINISHES = {
      new Finish("pumpkin", "Kürbis-Fade", Cos.Rarity.EPIC, (u, v) -> mix(0xFFFF7A1A, 0xFF7C3AED, smooth(u)), 0xFFFFD9A8, false, 0xFF1B1820, 0xFF2A2430),
      new Finish("bloodmoon", "Blutmond", Cos.Rarity.LEGENDARY, (u, v) -> mix(0xFF2A0A0E, 0xFF9E0F1F, smooth(u * 1.2F)), 0xFFFF3348, true, 0xFF120A0C, 0xFF3A0E14),
      new Finish("ghost", "Geisterglas", Cos.Rarity.LEGENDARY, (u, v) -> mix(0xFFB8E8FF, 0xFFEFFAFF, u), 0xFF8FE3FF, true, 0xFF2A3440, 0xFF3C4A58),
      new Finish("sunset", "Sonnenverlauf", Cos.Rarity.EPIC, (u, v) -> {
         float t = Math.max(0.0F, Math.min(1.0F, u * 0.8F + v * 0.35F));
         return t < 0.5F ? mix(0xFFFFD23F, 0xFFFF4F9A, t * 2.0F) : mix(0xFFFF4F9A, 0xFF6D28D9, (t - 0.5F) * 2.0F);
      }, 0xFFFFF4E0, false, 0xFF1E1B24, 0xFF33293F),
      new Finish("damast", "Damast", Cos.Rarity.LEGENDARY, (u, v) -> {
         float w = (float)Math.sin(u * 19.0F + Math.sin(v * 5.0F + u * 6.0F) * 2.0F);
         return mix(0xFF5B6270, 0xFFC9CED8, 0.5F + w * 0.5F);
      }, 0xFFF2F4F8, false, 0xFF3B2A1E, 0xFF5A4030),
      new Finish("tiger", "Tigerstreifen", Cos.Rarity.RARE, (u, v) -> {
         float s = (float)Math.sin(u * 30.0F + v * 9.0F + Math.sin(u * 11.0F) * 1.3F);
         return s > 0.55F ? 0xFF1A1208 : mix(0xFFFF9F1C, 0xFFFFC34D, v);
      }, 0xFFFFE7B0, false, 0xFF1A1208, 0xFF33240F),
      new Finish("marble", "Marmor-Verlauf", Cos.Rarity.LEGENDARY, (u, v) -> {
         float s = 0.5F + 0.5F * (float)Math.sin(u * 9.0F + Math.sin(v * 5.0F + u * 4.0F) * 2.2F);
         return mix(0xFFE11D48, 0xFF2563EB, s);
      }, 0xFFF5F7FF, true, 0xFF15171E, 0xFF262A36),
      new Finish("gold", "Gold", Cos.Rarity.LEGENDARY, (u, v) -> mix(0xFFFFE08A, 0xFFB7791F, v * 0.9F + u * 0.1F), 0xFFFFF6D5, false, 0xFF2B2116, 0xFFB7791F),
      new Finish("emerald", "Smaragd", Cos.Rarity.LEGENDARY, (u, v) -> {
         float s = 0.5F + 0.5F * (float)Math.sin(u * 14.0F + v * 4.0F);
         return mix(0xFF064E3B, 0xFF34D399, 0.35F + s * 0.5F);
      }, 0xFFA7F3D0, true, 0xFF0B1F18, 0xFF14532D),
      new Finish("sapphire", "Saphir", Cos.Rarity.LEGENDARY, (u, v) -> {
         float s = 0.5F + 0.5F * (float)Math.sin(u * 14.0F - v * 4.0F);
         return mix(0xFF1E3A8A, 0xFF60A5FA, 0.3F + s * 0.55F);
      }, 0xFFBFDBFE, true, 0xFF0F172A, 0xFF1E3A8A),
      new Finish("carbon", "Karbon", Cos.Rarity.RARE, (u, v) -> (((int)(u * 34.0F) + (int)(v * 6.0F)) & 1) == 0 ? 0xFF2A2D33 : 0xFF3A3E46, 0xFFD1D5DB, false, 0xFF16181C, 0xFF2A2D33),
      new Finish("night", "Nachtschwarz", Cos.Rarity.EPIC, (u, v) -> mix(0xFF0B0B10, 0xFF1F1B2E, u), 0xFFA855F7, true, 0xFF0B0B10, 0xFF1F1B2E)
   };

   static void register() {
      for (String[] t : TYPES) {
         for (Finish f : FINISHES) {
            final String type = t[0];
            Cos.add("knife_" + type + "_" + f.id(), t[1] + " | " + f.name(), Cos.Slot.KNIFE, f.rarity(), (g, a) -> draw(g, a, type, f));
         }
      }
   }

   // ------------------------------------------------------------------------------------------
   // helpers

   static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
      int gg = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
      int bb = Math.round((a & 255) * (1 - t) + (b & 255) * t);
      return 0xFF000000 | r << 16 | gg << 8 | bb;
   }

   static float smooth(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   private static int shade(int c, float k) {
      int r = Math.min(255, Math.round(((c >> 16) & 255) * k));
      int gg = Math.min(255, Math.round(((c >> 8) & 255) * k));
      int bb = Math.min(255, Math.round((c & 255) * k));
      return 0xFF000000 | r << 16 | gg << 8 | bb;
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   private static final float[] UV = {0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F};

   /**
    * A blade from its outline: at each station {@code z[i]} the spine is at height {@code top[i]}
    * and the cutting edge at {@code bot[i]} (top may be below bot for blades that hang down, the
    * maths does not care). The flats are {@code thick} wide at the spine and grind to a sharp edge,
    * with a visible grind line (lighter bevel) and a bright edge strip.
    */
   static void blade(G g, Finish f, float[] z, float[] top, float[] bot, float thick) {
      int n = z.length;
      final int rows = 4;
      float[] vs = {0.0F, 0.45F, 0.7F, 0.9F, 1.0F};
      float zLen = Math.max(1.0E-3F, z[n - 1] - z[0]);
      for (int side = -1; side <= 1; side += 2) {
         for (int i = 0; i + 1 < n; i++) {
            for (int r = 0; r < rows; r++) {
               float v0 = vs[r], v1 = vs[r + 1];
               float[] a = bp(z[i], top[i], bot[i], v0, thick, side, i, n);
               float[] b = bp(z[i + 1], top[i + 1], bot[i + 1], v0, thick, side, i + 1, n);
               float[] c = bp(z[i + 1], top[i + 1], bot[i + 1], v1, thick, side, i + 1, n);
               float[] d = bp(z[i], top[i], bot[i], v1, thick, side, i, n);
               float u = ((z[i] + z[i + 1]) * 0.5F - z[0]) / zLen;
               float vm = (v0 + v1) * 0.5F;
               int col;
               boolean glow = false;
               if (r == rows - 1) {
                  col = f.edge();
                  glow = f.glow();
               } else {
                  // Flat (darker), then the bevel below the grind line (lighter, catches light).
                  col = shade(f.paint().at(u, vm), r == 0 ? 0.92F : 1.08F + r * 0.05F);
               }
               g.glow(glow).color(col);
               if (side > 0) {
                  g.quad("white", a, b, c, d, UV);
               } else {
                  g.quad("white", d, c, b, a, UV);
               }
            }
         }
      }
      // Spine.
      for (int i = 0; i + 1 < n; i++) {
         float u = ((z[i] + z[i + 1]) * 0.5F - z[0]) / zLen;
         g.glow(false).color(shade(f.paint().at(u, 0.0F), 0.8F));
         float h0 = halfThick(thick, 0.0F, i, n), h1 = halfThick(thick, 0.0F, i + 1, n);
         g.quad("white", p(-h0, top[i], z[i]), p(-h1, top[i + 1], z[i + 1]), p(h1, top[i + 1], z[i + 1]), p(h0, top[i], z[i]), UV);
      }
      // Back of the blade (towards the handle).
      float h = halfThick(thick, 0.0F, 0, n);
      g.color(shade(f.paint().at(0.0F, 0.5F), 0.7F));
      g.quad("white", p(-h, top[0], z[0]), p(h, top[0], z[0]), p(0.0F, bot[0], z[0]), p(0.0F, bot[0], z[0]), UV);
      g.glow(false);
   }

   private static float halfThick(float thick, float v, int i, int n) {
      float along = 1.0F - 0.55F * i / Math.max(1, n - 1);
      return thick * 0.5F * along * (1.0F - (float)Math.pow(v, 1.6));
   }

   private static float[] bp(float z, float top, float bot, float v, float thick, int side, int i, int n) {
      return p(side * halfThick(thick, v, i, n), top + (bot - top) * v, z);
   }

   /** Tapering blade outline helper: straight spine, curved belly to a point. */
   static void simpleBlade(G g, Finish f, float z0, float len, float height, float drop, float thick, float belly) {
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      // Spine: straight, dropping by `drop` towards the point. Edge: straight at first, then
      // sweeping up to the point; more `belly` = a longer, rounder curve.
      float tip = height * 0.5F - drop - height * 0.1F;
      float power = 3.2F - 2.0F * Math.max(0.0F, Math.min(1.0F, belly));
      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         z[i] = z0 + t * len;
         top[i] = height * 0.5F - drop * (float)Math.pow(t, 2.2);
         bot[i] = -height * 0.5F + (tip + height * 0.5F) * (float)Math.pow(t, power);
         if (bot[i] > top[i] - 0.03F) bot[i] = top[i] - 0.03F;
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F;
      blade(g, f, z, top, bot, thick);
   }

   /** Rounded handle along z from z0 to z1 (boxy superellipse section). */
   static void grip(G g, int color, float z0, float z1, float w, float h, float y) {
      float[][] path = {p(0.0F, y, z0), p(0.0F, y, (z0 * 2 + z1) / 3), p(0.0F, y, (z0 + z1 * 2) / 3), p(0.0F, y, z1)};
      float[] ws = {w * 0.46F, w * 0.5F, w * 0.5F, w * 0.48F}, hs = {h * 0.46F, h * 0.5F, h * 0.5F, h * 0.48F}, es = {3.2F, 3.2F, 3.2F, 3.2F};
      g.glow(false).color(color);
      XmasGeo.sweep(g, "white", path, ws, hs, es, 16, 1.0F);
      // End caps.
      g.box("white", -w * 0.46F, y - h * 0.46F, z0 - 0.08F, w * 0.46F, y + h * 0.46F, z0, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   /** Grip with alternating wraps (paracord, grooves). */
   static void wrapped(G g, int a, int b, float z0, float z1, float w, float h, float y, float step) {
      grip(g, a, z0, z1, w, h, y);
      g.color(b);
      for (float z = z0 + step * 0.5F; z < z1 - step * 0.3F; z += step) {
         g.box("white", -w * 0.53F, y - h * 0.53F, z, w * 0.53F, y + h * 0.53F, z + step * 0.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   static void guard(G g, int color, float z, float w, float h, float t) {
      g.glow(false).color(color);
      g.box("white", -w * 0.5F, -h * 0.5F, z - t, w * 0.5F, h * 0.5F, z, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void ring(G g, int color, float cy, float cz, float r, float tube) {
      float[][] path = new float[17][];
      float[] rw = new float[17], rh = new float[17];
      for (int i = 0; i <= 16; i++) {
         double t = Math.PI * 2 * i / 16;
         path[i] = p(0.0F, cy + (float)Math.sin(t) * r, cz + (float)Math.cos(t) * r);
         rw[i] = tube;
         rh[i] = tube;
      }
      g.glow(false).color(color);
      XmasGeo.sweep(g, "white", path, rw, rh, null, 12, 1.0F);
   }

   private static final int STEEL = 0xFFB9BEC8;
   private static final int DARK_STEEL = 0xFF5D636E;

   // ------------------------------------------------------------------------------------------
   // the knives

   static void draw(G g, Cos.A a, String type, Finish f) {
      g.push();
      g.translate(1.0F, -10.6F, 0.0F);
      // A gentle idle sway; previews turn slowly.
      g.rotX(-8.0F + (float)Math.sin(a.time * 1.6F) * 2.0F);
      if (a.preview) g.rotY(a.time * 40.0F);
      float open = Math.max(0.0F, Math.min(1.0F, a.knifeOpen));
      switch (type) {
         case "karambit" -> karambit(g, f);
         case "butterfly" -> butterfly(g, f, open);
         case "m9" -> m9(g, f);
         case "bayonet" -> bayonet(g, f);
         case "flip" -> folding(g, f, open, 5.4F, 1.25F, 0.0F);
         case "gut" -> gut(g, f);
         case "huntsman" -> huntsman(g, f);
         case "falchion" -> falchion(g, f, open);
         case "bowie" -> bowie(g, f);
         case "daggers" -> daggers(g, f);
         case "navaja" -> navaja(g, f, open);
         case "stiletto" -> stiletto(g, f, open);
         case "talon" -> talon(g, f);
         case "ranger" -> ranger(g, f);
         case "skeleton" -> skeleton(g, f);
         case "nomad" -> nomad(g, f);
         case "survival" -> survival(g, f);
         case "paracord" -> paracord(g, f);
         case "classic" -> classic(g, f);
         case "kukri" -> kukri(g, f);
         default -> machete(g, f);
      }
      g.pop();
   }

   private static void karambit(G g, Finish f) {
      grip(g, f.handle(), -2.6F, 1.0F, 0.9F, 1.3F, 0.0F);
      ring(g, f.handle2(), 0.0F, -3.5F, 1.15F, 0.34F);
      // Hooked blade curving down like a claw.
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      for (int i = 0; i < n; i++) {
         double t = i / (n - 1.0);
         z[i] = 1.0F + (float)(Math.sin(t * Math.PI * 0.55) * 5.6);
         float c = (float)(-Math.sin(t * Math.PI * 0.72) * 3.2);
         float h = 1.55F * (1.0F - (float)Math.pow(t, 1.4) * 0.8F);
         top[i] = c + h * 0.2F;
         bot[i] = c - h * 0.8F + (float)t * 0.5F;
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F - 0.2F;
      blade(g, f, z, top, bot, 0.26F);
   }

   private static void butterfly(G g, Finish f, float open) {
      // Blade fixed; the two handle halves swing round their pins (x axis) over the blade when closed.
      simpleBlade(g, f, 0.6F, 6.6F, 1.0F, 0.5F, 0.22F, 0.15F);
      guard(g, DARK_STEEL, 0.65F, 0.5F, 1.2F, 0.35F);
      for (int side = -1; side <= 1; side += 2) {
         g.push();
         float pinY = side * 0.38F;
         g.translate(0.0F, pinY, 0.35F);
         g.rotX(side * 180.0F * (1.0F - open));
         g.translate(0.0F, -pinY, -0.35F);
         float x0 = side < 0 ? -0.52F : 0.04F, x1 = side < 0 ? -0.04F : 0.52F;
         g.glow(false).color(f.handle());
         g.box("white", x0, -0.62F, -4.3F, x1, 0.62F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
         // Cut-outs and latch.
         g.color(f.handle2());
         g.box("white", x0 - 0.02F, -0.25F, -3.6F, x1 + 0.02F, 0.25F, -1.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.color(STEEL);
         g.box("white", -0.58F, pinY - 0.12F, 0.18F, 0.58F, pinY + 0.12F, 0.42F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }
   }

   private static void m9(G g, Finish f) {
      grip(g, f.handle(), -4.4F, -0.1F, 1.05F, 1.4F, 0.0F);
      guard(g, DARK_STEEL, 0.25F, 0.6F, 2.4F, 0.4F);
      g.color(DARK_STEEL);
      g.box("white", -0.3F, 0.7F, 0.0F, 0.3F, 1.5F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F); // ring lug
      simpleBlade(g, f, 0.25F, 8.2F, 1.35F, 0.45F, 0.3F, 0.05F);
      // Saw-back teeth on the spine.
      g.color(shade(f.paint().at(0.3F, 0.0F), 0.75F));
      for (int i = 0; i < 6; i++) {
         float z = 1.2F + i * 0.55F;
         g.box("white", -0.1F, 0.67F, z, 0.1F, 0.9F, z + 0.28F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   private static void bayonet(G g, Finish f) {
      grip(g, f.handle(), -4.2F, -0.1F, 1.0F, 1.3F, 0.0F);
      guard(g, DARK_STEEL, 0.3F, 0.55F, 2.3F, 0.4F);
      simpleBlade(g, f, 0.3F, 8.4F, 1.15F, 0.35F, 0.26F, 0.05F);
   }

   private static void folding(G g, Finish f, float open, float len, float height, float drop) {
      // Handle with liners; the blade swings out of it around the pivot (x axis) at the front.
      grip(g, f.handle(), -4.2F, 0.3F, 0.85F, 1.25F, -0.1F);
      g.color(STEEL);
      g.box("white", -0.5F, -0.2F, 0.05F, 0.5F, 0.2F, 0.45F, 0.0F, 0.0F, 1.0F, 1.0F); // pivot
      g.push();
      g.translate(0.0F, 0.0F, 0.25F);
      g.rotX(-170.0F * (1.0F - open));
      g.translate(0.0F, 0.0F, -0.25F);
      simpleBlade(g, f, 0.3F, len, height, 0.45F + drop, 0.22F, 0.1F);
      g.color(DARK_STEEL);
      g.box("white", -0.12F, 0.55F, 0.6F, 0.12F, 0.85F, 1.1F, 0.0F, 0.0F, 1.0F, 1.0F); // flipper
      g.pop();
   }

   private static void gut(G g, Finish f) {
      grip(g, f.handle(), -4.0F, -0.1F, 1.0F, 1.35F, 0.0F);
      guard(g, DARK_STEEL, 0.2F, 0.55F, 1.9F, 0.3F);
      simpleBlade(g, f, 0.2F, 6.4F, 1.5F, -0.25F, 0.28F, 0.25F);
      // Gut hook notch on the spine.
      ring(g, shade(f.paint().at(0.5F, 0.0F), 0.8F), 0.85F, 3.6F, 0.35F, 0.13F);
   }

   private static void huntsman(G g, Finish f) {
      wrapped(g, f.handle(), f.handle2(), -4.6F, -0.1F, 1.0F, 1.35F, 0.0F, 0.7F);
      guard(g, STEEL, 0.25F, 0.6F, 2.6F, 0.35F);
      simpleBlade(g, f, 0.25F, 8.0F, 1.6F, 0.7F, 0.3F, 0.18F);
      g.color(shade(f.paint().at(0.25F, 0.0F), 0.7F));
      for (int i = 0; i < 7; i++) {
         float z = 0.9F + i * 0.5F;
         g.box("white", -0.1F, 0.78F, z, 0.1F, 1.02F, z + 0.25F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   private static void falchion(G g, Finish f, float open) {
      folding(g, f, open, 6.6F, 1.45F, -0.2F);
   }

   private static void bowie(G g, Finish f) {
      grip(g, f.handle(), -4.6F, -0.1F, 1.1F, 1.45F, 0.0F);
      guard(g, 0xFFC9A227, 0.25F, 0.7F, 3.0F, 0.4F);
      // Clip point: the spine drops in the last third.
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         z[i] = 0.25F + t * 9.0F;
         top[i] = 0.85F - (t > 0.62F ? (t - 0.62F) * 2.6F : 0.0F);
         bot[i] = Math.min(top[i] - 0.03F, -0.85F + (float)Math.pow(t, 3.0) * 0.75F);
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F;
      blade(g, f, z, top, bot, 0.32F);
   }

   private static void daggers(G g, Finish f) {
      // A pair of push daggers: T-handles across the fist, blades forward.
      for (int k = 0; k < 2; k++) {
         g.push();
         g.translate(k == 0 ? 0.0F : -0.2F, k == 0 ? 0.0F : -1.6F, k == 0 ? 0.0F : -0.4F);
         g.glow(false).color(f.handle());
         g.box("white", -0.45F, -1.1F, -0.6F, 0.45F, 1.1F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F);
         simpleBlade(g, f, 0.1F, 4.8F, 1.2F, 0.1F, 0.24F, 0.3F);
         g.pop();
      }
   }

   private static void navaja(G g, Finish f, float open) {
      // Long curved handle with a bolster; slim blade folds out.
      grip(g, f.handle(), -5.0F, 0.0F, 0.8F, 1.15F, -0.2F);
      g.color(STEEL);
      g.box("white", -0.45F, -0.65F, -0.4F, 0.45F, 0.45F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.push();
      g.translate(0.0F, 0.0F, 0.2F);
      g.rotX(-170.0F * (1.0F - open));
      g.translate(0.0F, 0.0F, -0.2F);
      simpleBlade(g, f, 0.25F, 6.8F, 1.0F, 0.8F, 0.2F, 0.05F);
      g.pop();
   }

   private static void stiletto(G g, Finish f, float open) {
      grip(g, f.handle(), -4.6F, 0.0F, 0.7F, 1.0F, 0.0F);
      guard(g, STEEL, 0.1F, 0.4F, 1.5F, 0.25F);
      g.push();
      g.translate(0.0F, 0.0F, 0.1F);
      g.rotX(-175.0F * (1.0F - open));
      g.translate(0.0F, 0.0F, -0.1F);
      simpleBlade(g, f, 0.1F, 7.2F, 0.75F, 0.0F, 0.2F, 0.0F);
      g.pop();
   }

   private static void talon(G g, Finish f) {
      grip(g, f.handle(), -3.4F, 0.2F, 0.9F, 1.25F, -0.1F);
      ring(g, f.handle2(), -0.1F, -4.2F, 0.75F, 0.25F);
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         z[i] = 0.2F + t * 7.0F;
         float c = -t * t * 1.9F;
         top[i] = c + 0.5F * (1 - t * 0.7F);
         bot[i] = c - 0.6F * (1 - t * 0.8F);
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F;
      blade(g, f, z, top, bot, 0.24F);
   }

   private static void ranger(G g, Finish f) {
      wrapped(g, f.handle(), f.handle2(), -4.4F, 0.0F, 1.0F, 1.35F, 0.0F, 0.9F);
      simpleBlade(g, f, 0.0F, 7.0F, 1.55F, 0.35F, 0.32F, 0.25F);
   }

   private static void skeleton(G g, Finish f) {
      // Open skeleton handle (frame) with a ring at the end.
      g.glow(false).color(STEEL);
      g.box("white", -0.18F, 0.4F, -4.0F, 0.18F, 0.6F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.box("white", -0.18F, -0.6F, -4.0F, 0.18F, -0.4F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.color(f.handle2());
      g.box("white", -0.2F, -0.42F, -3.0F, 0.2F, 0.42F, -2.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      ring(g, STEEL, 0.0F, -4.6F, 0.6F, 0.2F);
      simpleBlade(g, f, 0.0F, 6.4F, 1.2F, 0.45F, 0.24F, 0.2F);
   }

   private static void nomad(G g, Finish f) {
      grip(g, f.handle(), -4.2F, 0.0F, 0.95F, 1.3F, -0.15F);
      g.color(STEEL);
      g.box("white", -0.45F, -0.5F, -0.3F, 0.45F, 0.5F, 0.15F, 0.0F, 0.0F, 1.0F, 1.0F);
      simpleBlade(g, f, 0.15F, 6.8F, 1.3F, 0.25F, 0.26F, 0.28F);
   }

   private static void survival(G g, Finish f) {
      wrapped(g, f.handle(), 0xFF2B2B2B, -4.4F, -0.1F, 1.05F, 1.4F, 0.0F, 0.55F);
      guard(g, DARK_STEEL, 0.2F, 0.55F, 1.8F, 0.3F);
      simpleBlade(g, f, 0.2F, 7.0F, 1.35F, 0.15F, 0.3F, 0.15F);
      g.color(shade(f.paint().at(0.3F, 0.0F), 0.75F));
      for (int i = 0; i < 5; i++) {
         float z = 1.0F + i * 0.55F;
         g.box("white", -0.12F, 0.68F, z, 0.12F, 0.94F, z + 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   private static void paracord(G g, Finish f) {
      wrapped(g, f.handle(), f.handle2(), -4.6F, 0.0F, 0.95F, 1.2F, 0.0F, 0.42F);
      ring(g, f.handle2(), 0.0F, -5.0F, 0.45F, 0.16F);
      simpleBlade(g, f, 0.0F, 6.6F, 1.3F, 0.45F, 0.26F, 0.2F);
   }

   private static void classic(G g, Finish f) {
      grip(g, f.handle(), -4.2F, -0.1F, 1.0F, 1.3F, 0.0F);
      guard(g, STEEL, 0.2F, 0.6F, 2.0F, 0.3F);
      simpleBlade(g, f, 0.2F, 7.6F, 1.25F, 0.2F, 0.26F, 0.08F);
   }

   private static void kukri(G g, Finish f) {
      grip(g, f.handle(), -4.4F, -0.1F, 1.05F, 1.4F, 0.0F);
      guard(g, 0xFFC9A227, 0.2F, 0.55F, 1.8F, 0.3F);
      // Forward-bent blade, widest near the tip.
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         z[i] = 0.2F + t * 8.4F;
         float bend = -(float)Math.sin(t * Math.PI * 0.8) * 1.4F;
         float h = 0.9F + (float)Math.sin(t * Math.PI * 0.85) * 0.9F;
         top[i] = bend + 0.55F;
         bot[i] = bend + 0.55F - h;
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F;
      blade(g, f, z, top, bot, 0.32F);
   }

   private static void machete(G g, Finish f) {
      grip(g, f.handle(), -4.2F, -0.1F, 1.0F, 1.35F, 0.0F);
      int n = 26;
      float[] z = new float[n], top = new float[n], bot = new float[n];
      for (int i = 0; i < n; i++) {
         float t = (float)i / (n - 1);
         z[i] = t * 11.0F;
         top[i] = 0.7F + (float)Math.sin(t * Math.PI) * 0.35F;
         bot[i] = -1.0F - (float)Math.sin(t * Math.PI * 0.9) * 0.45F + (t > 0.8F ? (t - 0.8F) * 7.0F : 0.0F);
      }
      top[n - 1] = bot[n - 1] = (top[n - 2] + bot[n - 2]) * 0.5F;
      blade(g, f, z, top, bot, 0.22F);
   }

   // ------------------------------------------------------------------------------------------
   // the hand in first person

   /** Skin texture area of the right hand (front of the arm's lower end), 0..1. */
   private static final float SU0 = 44.0F / 64.0F, SV0 = 28.0F / 64.0F, SU1 = 48.0F / 64.0F, SV1 = 32.0F / 64.0F;

   private static void bone(G g, float w, float len, float d) {
      g.box("@skin", -w / 2.0F, -len, -d / 2.0F, w / 2.0F, 0.0F, d / 2.0F, SU0, SV0, SU1, SV1);
   }

   /**
    * Four fingers wrapped round the grip (palm on the -x side, fingers round the underside to
    * the +x side) and the thumb along the top. {@code a.curl[i]}: 1 = closed round the grip,
    * 0 = stretched out; {@code a.thumb}: 1 = on the grip, 0 = lifted.
    */
   static void fingers(G g, Cos.A a) {
      g.push();
      g.translate(1.0F, -10.6F, 0.0F);
      g.rotX(-8.0F + (float)Math.sin(a.time * 1.6F) * 2.0F);
      g.color(-1).glow(false);
      float[] zs = {-0.6F, -1.5F, -2.4F, -3.25F};
      float[] len = {1.0F, 1.08F, 1.0F, 0.82F};
      for (int i = 0; i < 4; i++) {
         float c = Math.max(0.0F, Math.min(1.0F, a.curl[i]));
         g.push();
         g.translate(-0.78F, 0.3F, zs[i]);
         g.rotZ(-12.0F * (1.0F - c));
         bone(g, 0.46F, 0.95F * len[i], 0.4F);
         g.translate(0.0F, -0.95F * len[i], 0.0F);
         g.rotZ(10.0F + 80.0F * c);
         bone(g, 0.42F, 0.85F * len[i], 0.38F);
         g.translate(0.0F, -0.85F * len[i], 0.0F);
         g.rotZ(8.0F + 72.0F * c);
         bone(g, 0.38F, 0.6F * len[i], 0.36F);
         g.pop();
      }
      // Thumb along the top of the grip, pointing forward.
      g.push();
      g.translate(0.45F, 0.6F, -1.4F);
      g.rotX(-25.0F * (1.0F - Math.max(0.0F, Math.min(1.0F, a.thumb))));
      g.box("@skin", -0.22F, -0.2F, 0.0F, 0.22F, 0.22F, 1.15F, SU0, SV0, SU1, SV1);
      g.translate(0.0F, 0.0F, 1.15F);
      g.rotX(-10.0F);
      g.box("@skin", -0.2F, -0.18F, 0.0F, 0.2F, 0.2F, 0.75F, SU0, SV0, SU1, SV1);
      g.pop();
      // Palm block behind the fingers.
      g.box("@skin", -1.05F, -0.75F, -3.75F, -0.62F, 0.75F, -0.15F, SU0, SV0, SU1, SV1);
      g.pop();
   }
}
