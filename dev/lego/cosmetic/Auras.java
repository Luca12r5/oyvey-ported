package dev.lego.cosmetic;

final class Auras {
   private Auras() {
   }

   static void register() {
      Cos.add("aura_fire", "Feuer-Aura", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, HdAuras::fire);
      Cos.add("aura_frost", "Frost-Aura", Cos.Slot.AURA, Cos.Rarity.EPIC, HdAuras::frost);
      Cos.add("aura_hearts", "Herz-Aura", Cos.Slot.AURA, Cos.Rarity.RARE, HdAuras::hearts);
      Cos.add("aura_orbit", "Sternen-Orbit", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, HdAuras::orbit);
      Cos.add("aura_storm", "Gewitter-Aura", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, HdAuras::storm);
      Cos.add("aura_sakura", "Kirschblüten-Wind", Cos.Slot.AURA, Cos.Rarity.EPIC, HdAuras::sakura);
      Cos.add("aura_music", "Musik-Aura", Cos.Slot.AURA, Cos.Rarity.RARE, HdAuras::music);
      Cos.add("aura_bubbles", "Seifenblasen", Cos.Slot.AURA, Cos.Rarity.COMMON, HdAuras::bubbles);
      Cos.add("aura_rainbow", "Regenbogen-Ring", Cos.Slot.AURA, Cos.Rarity.EPIC, HdAuras::rainbowRing);
      Cos.add("aura_void", "Leere-Aura", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, HdAuras::voidAura);
      Cos.add("aura_sparkle", "Glitzerstaub", Cos.Slot.AURA, Cos.Rarity.RARE, HdAuras::sparkle);
      Cos.add("aura_leaves", "Herbstlaub", Cos.Slot.AURA, Cos.Rarity.COMMON, HdAuras::leaves);
      Cos.add("aura_runes", "Runenkreis", Cos.Slot.AURA, Cos.Rarity.LEGENDARY, HdAuras::runes);
      Cos.add("aura_bricks", "Stein-Orbit", Cos.Slot.AURA, Cos.Rarity.EPIC, HdAuras::bricks);
   }

   private static float fract(float v) {
      return v - (float)Math.floor(v);
   }

   private static void groundGlow(G g, int argb, float r) {
      g.color(argb);
      g.push();
      g.translate(0.0F, -23.9F, 0.0F);
      g.rotX(-90.0F);
      g.plane("orb", 0.0F, 0.0F, r * 2.0F, r * 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
   }

   static void fire(G g, Cos.A a) {
      g.glow(true);
      groundGlow(g, -1711310294, 11.0F);
      int n = 14;

      for (int i = 0; i < n; i++) {
         float ph = fract(a.time * 1.2F + i * 0.37F);
         double ang = (Math.PI * 2) * i / n + a.time * 0.5F;
         float r = 7.2F + Geo.sin(i * 1.7) * 1.2F - ph * 1.5F;
         float s = 5.2F * (1.0F - ph * 0.75F);
         g.color(Geo.argb((int)(255.0 * Math.sin(ph * Math.PI)), 16777215));
         g.cross("flame", Geo.cos(ang) * r, -24.0F + ph * 13.0F + s / 2.0F, Geo.sin(ang) * r, s * 0.65F, s, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      for (int i = 0; i < 12; i++) {
         float ph = fract(a.time * 0.6F + i * 0.29F);
         double ang = i * 2.1 + a.time * 0.8F;
         g.color(Geo.argb((int)(255.0F * (1.0F - ph)), 16777215));
         Geo.sprite(g, "orb", Geo.cos(ang) * (6.0F + ph * 3.0F), -22.0F + ph * 26.0F, Geo.sin(ang) * (6.0F + ph * 3.0F), 1.2F * (1.0F - ph) + 0.4F);
      }

      g.color(-1).glow(false);
   }

   static void frost(G g, Cos.A a) {
      g.glow(true);
      groundGlow(g, -2005075969, 11.0F);
      int n = 12;

      for (int i = 0; i < n; i++) {
         double ang = (Math.PI * 2) * i / n + a.time * 0.7F;
         float ph = fract(a.time * 0.18F + i * 0.41F);
         float y = -23.0F + ph * 26.0F;
         float r = 8.5F + Geo.sin(i * 2.3) * 0.8F;
         float s = 2.6F + i % 3 * 0.8F;
         g.push();
         g.translate(Geo.cos(ang) * r, y, Geo.sin(ang) * r);
         g.rotY((float)Math.toDegrees(-ang) + 90.0F);
         g.rotZ(a.time * 60.0F + i * 30);
         g.color(Geo.argb((int)(240.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 1.6)), 16777215));
         g.plane("snowflake", 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      for (int i = 0; i < 16; i++) {
         double ang = i * 2.4 - a.time * 0.4;
         float tw = 0.5F + 0.5F * Geo.sin(a.time * 4.0F + i * 1.3);
         g.color(Geo.argb((int)(255.0F * tw), 16777215));
         Geo.sprite(g, "spark", Geo.cos(ang) * (6 + i % 4), -22.0F + i * 1.7F % 24.0F, Geo.sin(ang) * (6 + i % 4), 0.8F + tw);
      }

      g.color(-1).glow(false);
   }

   static void hearts(G g, Cos.A a) {
      g.glow(true);
      int n = 8;

      for (int i = 0; i < n; i++) {
         float ph = fract(a.time * 0.3F + (float)i / n);
         double ang = (Math.PI * 2) * i / n + a.time * 0.6F;
         float r = 8.5F + Geo.sin(a.time * 1.5 + i) * 0.8F;
         g.push();
         g.translate(Geo.cos(ang) * r, -22.0F + ph * 26.0F, Geo.sin(ang) * r);
         g.rotY((float)Math.toDegrees(-ang) + 90.0F);
         g.rotZ(Geo.sin(a.time * 3.0F + i) * 12.0F);
         float beat = 1.0F + 0.12F * (float)Math.max(0.0, Math.sin(a.time * 7.0F + i));
         float s = (2.8F + i % 3 * 0.6F) * beat;
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 1.8)), 16777215));
         g.plane("heart", 0.0F, 0.0F, s, s, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1).glow(false);
   }

   static void orbit(G g, Cos.A a) {
      g.glow(true);
      String[] tex = new String[]{"orb_purple", "orb_cyan", "orb"};

      for (int i = 0; i < 3; i++) {
         g.push();
         g.translate(0.0F, -9.0F, 0.0F);
         g.rotX(25 + i * 55);
         g.rotY(a.time * (70 + i * 25) + i * 120);

         for (int k = 0; k < 8; k++) {
            g.push();
            g.rotY(-k * 7);
            g.translate(11.0F, 0.0F, 0.0F);
            g.color(Geo.argb((int)(240.0F * (1.0F - k / 8.0F)), 16777215));
            float s = 3.6F * (1.0F - k * 0.1F);
            Geo.sprite(g, tex[i], 0.0F, 0.0F, 0.0F, s);
            if (k == 0) {
               g.color(-1);
               Geo.sprite(g, "spark", 0.0F, 0.0F, 0.0F, 2.6F);
            }

            g.pop();
         }

         g.pop();
      }

      g.color(-1).glow(false);
   }

   static void storm(G g, Cos.A a) {
      g.glow(true);

      for (int i = 0; i < 10; i++) {
         double ang = (Math.PI * 2) * i / 10.0 + a.time * 0.6;
         g.color(1715096170);
         Geo.sprite(g, "orb_cyan", Geo.cos(ang) * 8.0F, 4.0F + Geo.sin(a.time * 2.0F + i) * 0.6F, Geo.sin(ang) * 8.0F, 6.0F);
      }

      int n = 5;

      for (int i = 0; i < n; i++) {
         float cycle = fract(a.time * 1.3F + i * 0.37F);
         if (!(cycle > 0.3F)) {
            double ang = (Math.PI * 2) * ((i * 0.61F + Math.floor(a.time * 1.3F + i * 0.37F) * 0.29F) % 1.0);
            float r = 7 + i % 2 * 2;
            g.color(Geo.argb((int)(255.0F * (1.0F - cycle / 0.3F)), 16777215));
            g.cross("bolt", Geo.cos(ang) * r, -10.0F, Geo.sin(ang) * r, 6.0F, 26.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         }
      }

      for (int ix = 0; ix < 10; ix++) {
         double ang = (Math.PI * 2) * ix / 10.0 - a.time * 1.2F;
         g.color(-1711276033);
         Geo.sprite(g, "orb_cyan", Geo.cos(ang) * 8.0F, -23.5F + Geo.sin(a.time * 3.0F + ix) * 0.6F, Geo.sin(ang) * 8.0F, 2.2F);
      }

      g.color(-1).glow(false);
   }

   static void sakura(G g, Cos.A a) {
      int n = 18;

      for (int i = 0; i < n; i++) {
         float ph = fract(a.time * 0.22F + i * 0.137F);
         double ang = i * 2.39 + a.time * 0.9 + ph * 3.0F;
         float r = 6.0F + i % 4 * 1.4F;
         g.push();
         g.translate(Geo.cos(ang) * r, 6.0F - ph * 30.0F, Geo.sin(ang) * r);
         g.rotY(a.time * 90.0F + i * 40);
         g.rotX(a.time * 70.0F + i * 25);
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 2.0)), 16777215));
         g.plane("petal", 0.0F, 0.0F, 2.8F, 2.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1);
   }

   static void music(G g, Cos.A a) {
      g.glow(true);
      int[] cols = new int[]{-9772801, -38192, -7606, -7667862};

      for (int i = 0; i < 9; i++) {
         float ph = fract(a.time * 0.35F + i / 9.0F);
         double ang = (Math.PI * 2) * i / 9.0 + a.time * 0.5;
         float r = 8.0F + Geo.sin(ph * 6.0F + i) * 1.0F;
         g.push();
         g.translate(Geo.cos(ang) * r, -18.0F + ph * 24.0F, Geo.sin(ang) * r);
         g.rotY((float)Math.toDegrees(-ang) + 90.0F);
         g.rotZ(Geo.sin(a.time * 5.0F + i) * 15.0F);
         int c = cols[i % 4];
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 2.0)), c));
         g.plane("note", 0.0F, 0.0F, 3.0F, 3.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1).glow(false);
   }

   static void bubbles(G g, Cos.A a) {
      for (int i = 0; i < 14; i++) {
         float ph = fract(a.time * 0.2F + i * 0.173F);
         double ang = i * 2.1 + ph * 2.0F;
         float r = 6 + i % 3 * 2;
         float s = 2.4F + i % 4 * 0.9F;
         float wob = 1.0F + 0.06F * Geo.sin(a.time * 6.0F + i);
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 2.5)), 16777215));
         g.push();
         g.translate(Geo.cos(ang) * r + Geo.sin(a.time * 2.0F + i) * 0.8F, -23.0F + ph * 30.0F, Geo.sin(ang) * r);
         g.scale(wob, 1.0F / wob, wob);
         Geo.sprite3(g, "bubble", 0.0F, 0.0F, 0.0F, s);
         g.pop();
      }

      g.color(-1);
   }

   static void rainbowRing(G g, Cos.A a) {
      g.glow(true);
      int seg = 36;

      for (int ring = 0; ring < 2; ring++) {
         g.push();
         g.translate(0.0F, ring == 0 ? -12.0F + Geo.sin(a.time * 1.5) * 2.0F : -20.0F + Geo.sin(a.time * 1.5 + 2.0) * 1.5F, 0.0F);
         g.rotX(ring == 0 ? 8.0F * Geo.sin(a.time) : -6.0F);
         float R = ring == 0 ? 10.0F : 8.5F;

         for (int i = 0; i < seg; i++) {
            double a0 = (Math.PI * 2) * i / seg;
            double a1 = (Math.PI * 2) * (i + 1) / seg;
            g.color(Geo.hue((float)i / seg + a.time * 0.2F * (ring == 0 ? 1 : -1), 0.65, 1.0));
            float h = 0.9F;
            g.quad(
               "ring_glow",
               new float[]{Geo.cos(a0) * R, h, Geo.sin(a0) * R},
               new float[]{Geo.cos(a1) * R, h, Geo.sin(a1) * R},
               new float[]{Geo.cos(a1) * R, -h, Geo.sin(a1) * R},
               new float[]{Geo.cos(a0) * R, -h, Geo.sin(a0) * R},
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
         }

         g.pop();
      }

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0 + a.time * 1.2;
         float tw = 0.5F + 0.5F * Geo.sin(a.time * 4.0F + i);
         g.color(Geo.argb((int)(255.0F * tw), 16777215));
         Geo.sprite(g, "spark", Geo.cos(an) * 10.0F, -12.0F + Geo.sin(a.time * 1.5) * 2.0F, Geo.sin(an) * 10.0F, 1.4F + tw);
      }

      g.color(-1).glow(false);
   }

   static void voidAura(G g, Cos.A a) {
      g.glow(true);
      groundGlow(g, -1433785601, 10.0F);

      for (int i = 0; i < 16; i++) {
         float ph = fract(a.time * 0.35F + i / 16.0F);
         double ang = i * 2.39 - ph * 5.0F - a.time * 0.4;
         float r = 11.0F * (1.0F - ph) + 2.0F;
         g.color(Geo.argb((int)(230.0F * Math.min(1.0F, ph * 3.0F) * (1.0F - ph * 0.6F)), 16777215));
         Geo.sprite(g, "void_orb", Geo.cos(ang) * r, -22.0F + ph * 20.0F + Geo.sin(i) * 2.0F, Geo.sin(ang) * r, 2.4F * (1.0F - ph * 0.5F));
      }

      g.push();
      g.translate(0.0F, -23.7F, 0.0F);
      g.rotY(-a.time * 50.0F);
      g.color(-860861697);
      g.ring("ring_glow", 7.5F, 9.5F, 0.0F, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();
      g.color(-1).glow(false);
   }

   static void sparkle(G g, Cos.A a) {
      g.glow(true);
      int[] cols = new int[]{-1, -5984, -4658945, -18200};

      for (int i = 0; i < 22; i++) {
         float ph = fract(a.time * 0.25F + i * 0.0913F);
         double ang = i * 2.39 + a.time * 0.3;
         float r = 5 + i * 7 % 5;
         float tw = 0.4F + 0.6F * Math.abs(Geo.sin(a.time * 3.0F + i * 1.7));
         int c = cols[i % 4];
         g.color(Geo.argb((int)(255.0F * tw * Math.min(1.0, Math.sin(ph * Math.PI) * 2.0)), c));
         Geo.sprite(g, "spark", Geo.cos(ang) * r, 4.0F - ph * 28.0F, Geo.sin(ang) * r, 0.8F + tw * 1.4F);
      }

      g.color(-1).glow(false);
   }

   static void leaves(G g, Cos.A a) {
      int[] cols = new int[]{-1, -12128, -24416, -2556006};

      for (int i = 0; i < 16; i++) {
         float ph = fract(a.time * 0.18F + i * 0.157F);
         double ang = i * 2.2 + a.time * 0.7 + ph * 2.5;
         float r = 7.0F + i % 3 * 1.5F;
         g.push();
         g.translate(Geo.cos(ang) * r, 4.0F - ph * 28.0F, Geo.sin(ang) * r);
         g.rotY(a.time * 80.0F + i * 50);
         g.rotZ(Geo.sin(a.time * 2.0F + i) * 40.0F);
         g.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(ph * Math.PI) * 2.5)), cols[i % 4]));
         g.plane("leaf", 0.0F, 0.0F, 2.3F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1);
   }

   static void runes(G g, Cos.A a) {
      g.glow(true);
      float pulse = 0.8F + 0.2F * Geo.sin(a.time * 2.5);
      g.push();
      g.translate(0.0F, -23.8F, 0.0F);
      g.rotY(a.time * 25.0F);
      g.rotX(-90.0F);
      g.color(Geo.argb((int)(255.0F * pulse), 16777215));
      g.plane("rune_circle", 0.0F, 0.0F, 22.0F, 22.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      g.pop();

      for (int i = 0; i < 8; i++) {
         float ph = fract(a.time * 0.3F + i / 8.0F);
         double an = (Math.PI * 2) * i / 8.0 + a.time * 0.4;
         g.push();
         g.translate(Geo.cos(an) * 8.5F, -23.0F + ph * 16.0F, Geo.sin(an) * 8.5F);
         g.rotY((float)Math.toDegrees(-an) + 90.0F);
         g.color(Geo.argb((int)(255.0 * Math.sin(ph * Math.PI)), 16777215));
         g.plane("glyph", 0.0F, 0.0F, 1.8F, 1.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         g.pop();
      }

      g.color(-1).glow(false);
   }

   static void bricks(G g, Cos.A a) {
      String[] tex = new String[]{"lego_red", "lego_yellow", "lego_blue", "lego_green"};

      for (int i = 0; i < 8; i++) {
         double an = (Math.PI * 2) * i / 8.0 + a.time * 0.8;
         float y = -10.0F + Geo.sin(a.time * 1.6 + i * 0.8) * 3.0F;
         g.push();
         g.translate(Geo.cos(an) * 10.0F, y, Geo.sin(an) * 10.0F);
         g.rotY(a.time * 60.0F + i * 45);
         g.rotX(Geo.sin(a.time + i) * 25.0F);
         String t = tex[i % 4];
         g.box(t, -1.6F, -0.7F, -0.8F, 1.6F, 0.7F, 0.8F, 0.0F, 0.0F, 1.0F, 1.0F);

         for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 0.8F, 0.7F, 0.0F);
            g.cylinder(t, 0.45F, 0.45F, 0.0F, 0.45F, 8, true, 0.1F, 0.1F, 0.9F, 0.9F);
            g.pop();
         }

         g.pop();
      }
   }
}
