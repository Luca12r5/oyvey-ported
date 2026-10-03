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

   private static float fract(float var0) {
      return var0 - (float)Math.floor(var0);
   }

   private static void groundGlow(G var0, int var1, float var2) {
      var0.color(var1);
      var0.push();
      var0.translate(0.0F, -23.9F, 0.0F);
      var0.rotX(-90.0F);
      var0.plane("orb", 0.0F, 0.0F, var2 * 2.0F, var2 * 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void fire(G var0, Cos.A var1) {
      var0.glow(true);
      groundGlow(var0, -1711310294, 11.0F);
      byte var2 = 14;

      for (int var3 = 0; var3 < var2; var3++) {
         float var4 = fract(var1.time * 1.2F + var3 * 0.37F);
         double var5 = (Math.PI * 2) * var3 / var2 + var1.time * 0.5F;
         float var7 = 7.2F + Geo.sin(var3 * 1.7) * 1.2F - var4 * 1.5F;
         float var8 = 5.2F * (1.0F - var4 * 0.75F);
         var0.color(Geo.argb((int)(255.0 * Math.sin(var4 * Math.PI)), 16777215));
         var0.cross("flame", Geo.cos(var5) * var7, -24.0F + var4 * 13.0F + var8 / 2.0F, Geo.sin(var5) * var7, var8 * 0.65F, var8, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      for (int var9 = 0; var9 < 12; var9++) {
         float var10 = fract(var1.time * 0.6F + var9 * 0.29F);
         double var11 = var9 * 2.1 + var1.time * 0.8F;
         var0.color(Geo.argb((int)(255.0F * (1.0F - var10)), 16777215));
         Geo.sprite(
            var0, "orb", Geo.cos(var11) * (6.0F + var10 * 3.0F), -22.0F + var10 * 26.0F, Geo.sin(var11) * (6.0F + var10 * 3.0F), 1.2F * (1.0F - var10) + 0.4F
         );
      }

      var0.color(-1).glow(false);
   }

   static void frost(G var0, Cos.A var1) {
      var0.glow(true);
      groundGlow(var0, -2005075969, 11.0F);
      byte var2 = 12;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = (Math.PI * 2) * var3 / var2 + var1.time * 0.7F;
         float var6 = fract(var1.time * 0.18F + var3 * 0.41F);
         float var7 = -23.0F + var6 * 26.0F;
         float var8 = 8.5F + Geo.sin(var3 * 2.3) * 0.8F;
         float var9 = 2.6F + var3 % 3 * 0.8F;
         var0.push();
         var0.translate(Geo.cos(var4) * var8, var7, Geo.sin(var4) * var8);
         var0.rotY((float)Math.toDegrees(-var4) + 90.0F);
         var0.rotZ(var1.time * 60.0F + var3 * 30);
         var0.color(Geo.argb((int)(240.0 * Math.min(1.0, Math.sin(var6 * Math.PI) * 1.6)), 16777215));
         var0.plane("snowflake", 0.0F, 0.0F, var9, var9, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      for (int var10 = 0; var10 < 16; var10++) {
         double var11 = var10 * 2.4 - var1.time * 0.4;
         float var12 = 0.5F + 0.5F * Geo.sin(var1.time * 4.0F + var10 * 1.3);
         var0.color(Geo.argb((int)(255.0F * var12), 16777215));
         Geo.sprite(var0, "spark", Geo.cos(var11) * (6 + var10 % 4), -22.0F + var10 * 1.7F % 24.0F, Geo.sin(var11) * (6 + var10 % 4), 0.8F + var12);
      }

      var0.color(-1).glow(false);
   }

   static void hearts(G var0, Cos.A var1) {
      var0.glow(true);
      byte var2 = 8;

      for (int var3 = 0; var3 < var2; var3++) {
         float var4 = fract(var1.time * 0.3F + (float)var3 / var2);
         double var5 = (Math.PI * 2) * var3 / var2 + var1.time * 0.6F;
         float var7 = 8.5F + Geo.sin(var1.time * 1.5 + var3) * 0.8F;
         var0.push();
         var0.translate(Geo.cos(var5) * var7, -22.0F + var4 * 26.0F, Geo.sin(var5) * var7);
         var0.rotY((float)Math.toDegrees(-var5) + 90.0F);
         var0.rotZ(Geo.sin(var1.time * 3.0F + var3) * 12.0F);
         float var8 = 1.0F + 0.12F * (float)Math.max(0.0, Math.sin(var1.time * 7.0F + var3));
         float var9 = (2.8F + var3 % 3 * 0.6F) * var8;
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var4 * Math.PI) * 1.8)), 16777215));
         var0.plane("heart", 0.0F, 0.0F, var9, var9, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1).glow(false);
   }

   static void orbit(G var0, Cos.A var1) {
      var0.glow(true);
      String[] var2 = new String[]{"orb_purple", "orb_cyan", "orb"};

      for (int var3 = 0; var3 < 3; var3++) {
         var0.push();
         var0.translate(0.0F, -9.0F, 0.0F);
         var0.rotX(25 + var3 * 55);
         var0.rotY(var1.time * (70 + var3 * 25) + var3 * 120);

         for (int var4 = 0; var4 < 8; var4++) {
            var0.push();
            var0.rotY(-var4 * 7);
            var0.translate(11.0F, 0.0F, 0.0F);
            var0.color(Geo.argb((int)(240.0F * (1.0F - var4 / 8.0F)), 16777215));
            float var5 = 3.6F * (1.0F - var4 * 0.1F);
            Geo.sprite(var0, var2[var3], 0.0F, 0.0F, 0.0F, var5);
            if (var4 == 0) {
               var0.color(-1);
               Geo.sprite(var0, "spark", 0.0F, 0.0F, 0.0F, 2.6F);
            }

            var0.pop();
         }

         var0.pop();
      }

      var0.color(-1).glow(false);
   }

   static void storm(G var0, Cos.A var1) {
      var0.glow(true);

      for (int var2 = 0; var2 < 10; var2++) {
         double var3 = (Math.PI * 2) * var2 / 10.0 + var1.time * 0.6;
         var0.color(1715096170);
         Geo.sprite(var0, "orb_cyan", Geo.cos(var3) * 8.0F, 4.0F + Geo.sin(var1.time * 2.0F + var2) * 0.6F, Geo.sin(var3) * 8.0F, 6.0F);
      }

      byte var8 = 5;

      for (int var9 = 0; var9 < var8; var9++) {
         float var4 = fract(var1.time * 1.3F + var9 * 0.37F);
         if (!(var4 > 0.3F)) {
            double var5 = (Math.PI * 2) * ((var9 * 0.61F + Math.floor(var1.time * 1.3F + var9 * 0.37F) * 0.29F) % 1.0);
            float var7 = 7 + var9 % 2 * 2;
            var0.color(Geo.argb((int)(255.0F * (1.0F - var4 / 0.3F)), 16777215));
            var0.cross("bolt", Geo.cos(var5) * var7, -10.0F, Geo.sin(var5) * var7, 6.0F, 26.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         }
      }

      for (int var10 = 0; var10 < 10; var10++) {
         double var11 = (Math.PI * 2) * var10 / 10.0 - var1.time * 1.2F;
         var0.color(-1711276033);
         Geo.sprite(var0, "orb_cyan", Geo.cos(var11) * 8.0F, -23.5F + Geo.sin(var1.time * 3.0F + var10) * 0.6F, Geo.sin(var11) * 8.0F, 2.2F);
      }

      var0.color(-1).glow(false);
   }

   static void sakura(G var0, Cos.A var1) {
      byte var2 = 18;

      for (int var3 = 0; var3 < var2; var3++) {
         float var4 = fract(var1.time * 0.22F + var3 * 0.137F);
         double var5 = var3 * 2.39 + var1.time * 0.9 + var4 * 3.0F;
         float var7 = 6.0F + var3 % 4 * 1.4F;
         var0.push();
         var0.translate(Geo.cos(var5) * var7, 6.0F - var4 * 30.0F, Geo.sin(var5) * var7);
         var0.rotY(var1.time * 90.0F + var3 * 40);
         var0.rotX(var1.time * 70.0F + var3 * 25);
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var4 * Math.PI) * 2.0)), 16777215));
         var0.plane("petal", 0.0F, 0.0F, 2.8F, 2.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1);
   }

   static void music(G var0, Cos.A var1) {
      var0.glow(true);
      int[] var2 = new int[]{-9772801, -38192, -7606, -7667862};

      for (int var3 = 0; var3 < 9; var3++) {
         float var4 = fract(var1.time * 0.35F + var3 / 9.0F);
         double var5 = (Math.PI * 2) * var3 / 9.0 + var1.time * 0.5;
         float var7 = 8.0F + Geo.sin(var4 * 6.0F + var3) * 1.0F;
         var0.push();
         var0.translate(Geo.cos(var5) * var7, -18.0F + var4 * 24.0F, Geo.sin(var5) * var7);
         var0.rotY((float)Math.toDegrees(-var5) + 90.0F);
         var0.rotZ(Geo.sin(var1.time * 5.0F + var3) * 15.0F);
         int var8 = var2[var3 % 4];
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var4 * Math.PI) * 2.0)), var8));
         var0.plane("note", 0.0F, 0.0F, 3.0F, 3.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1).glow(false);
   }

   static void bubbles(G var0, Cos.A var1) {
      for (int var2 = 0; var2 < 14; var2++) {
         float var3 = fract(var1.time * 0.2F + var2 * 0.173F);
         double var4 = var2 * 2.1 + var3 * 2.0F;
         float var6 = 6 + var2 % 3 * 2;
         float var7 = 2.4F + var2 % 4 * 0.9F;
         float var8 = 1.0F + 0.06F * Geo.sin(var1.time * 6.0F + var2);
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var3 * Math.PI) * 2.5)), 16777215));
         var0.push();
         var0.translate(Geo.cos(var4) * var6 + Geo.sin(var1.time * 2.0F + var2) * 0.8F, -23.0F + var3 * 30.0F, Geo.sin(var4) * var6);
         var0.scale(var8, 1.0F / var8, var8);
         Geo.sprite3(var0, "bubble", 0.0F, 0.0F, 0.0F, var7);
         var0.pop();
      }

      var0.color(-1);
   }

   static void rainbowRing(G var0, Cos.A var1) {
      var0.glow(true);
      byte var2 = 36;

      for (int var3 = 0; var3 < 2; var3++) {
         var0.push();
         var0.translate(0.0F, var3 == 0 ? -12.0F + Geo.sin(var1.time * 1.5) * 2.0F : -20.0F + Geo.sin(var1.time * 1.5 + 2.0) * 1.5F, 0.0F);
         var0.rotX(var3 == 0 ? 8.0F * Geo.sin(var1.time) : -6.0F);
         float var4 = var3 == 0 ? 10.0F : 8.5F;

         for (int var5 = 0; var5 < var2; var5++) {
            double var6 = (Math.PI * 2) * var5 / var2;
            double var8 = (Math.PI * 2) * (var5 + 1) / var2;
            var0.color(Geo.hue((float)var5 / var2 + var1.time * 0.2F * (var3 == 0 ? 1 : -1), 0.65, 1.0));
            float var10 = 0.9F;
            var0.quad(
               "ring_glow",
               new float[]{Geo.cos(var6) * var4, var10, Geo.sin(var6) * var4},
               new float[]{Geo.cos(var8) * var4, var10, Geo.sin(var8) * var4},
               new float[]{Geo.cos(var8) * var4, -var10, Geo.sin(var8) * var4},
               new float[]{Geo.cos(var6) * var4, -var10, Geo.sin(var6) * var4},
               new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
            );
         }

         var0.pop();
      }

      for (int var11 = 0; var11 < 8; var11++) {
         double var12 = (Math.PI * 2) * var11 / 8.0 + var1.time * 1.2;
         float var13 = 0.5F + 0.5F * Geo.sin(var1.time * 4.0F + var11);
         var0.color(Geo.argb((int)(255.0F * var13), 16777215));
         Geo.sprite(var0, "spark", Geo.cos(var12) * 10.0F, -12.0F + Geo.sin(var1.time * 1.5) * 2.0F, Geo.sin(var12) * 10.0F, 1.4F + var13);
      }

      var0.color(-1).glow(false);
   }

   static void voidAura(G var0, Cos.A var1) {
      var0.glow(true);
      groundGlow(var0, -1433785601, 10.0F);

      for (int var2 = 0; var2 < 16; var2++) {
         float var3 = fract(var1.time * 0.35F + var2 / 16.0F);
         double var4 = var2 * 2.39 - var3 * 5.0F - var1.time * 0.4;
         float var6 = 11.0F * (1.0F - var3) + 2.0F;
         var0.color(Geo.argb((int)(230.0F * Math.min(1.0F, var3 * 3.0F) * (1.0F - var3 * 0.6F)), 16777215));
         Geo.sprite(var0, "void_orb", Geo.cos(var4) * var6, -22.0F + var3 * 20.0F + Geo.sin(var2) * 2.0F, Geo.sin(var4) * var6, 2.4F * (1.0F - var3 * 0.5F));
      }

      var0.push();
      var0.translate(0.0F, -23.7F, 0.0F);
      var0.rotY(-var1.time * 50.0F);
      var0.color(-860861697);
      var0.ring("ring_glow", 7.5F, 9.5F, 0.0F, 28, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1).glow(false);
   }

   static void sparkle(G var0, Cos.A var1) {
      var0.glow(true);
      int[] var2 = new int[]{-1, -5984, -4658945, -18200};

      for (int var3 = 0; var3 < 22; var3++) {
         float var4 = fract(var1.time * 0.25F + var3 * 0.0913F);
         double var5 = var3 * 2.39 + var1.time * 0.3;
         float var7 = 5 + var3 * 7 % 5;
         float var8 = 0.4F + 0.6F * Math.abs(Geo.sin(var1.time * 3.0F + var3 * 1.7));
         int var9 = var2[var3 % 4];
         var0.color(Geo.argb((int)(255.0F * var8 * Math.min(1.0, Math.sin(var4 * Math.PI) * 2.0)), var9));
         Geo.sprite(var0, "spark", Geo.cos(var5) * var7, 4.0F - var4 * 28.0F, Geo.sin(var5) * var7, 0.8F + var8 * 1.4F);
      }

      var0.color(-1).glow(false);
   }

   static void leaves(G var0, Cos.A var1) {
      int[] var2 = new int[]{-1, -12128, -24416, -2556006};

      for (int var3 = 0; var3 < 16; var3++) {
         float var4 = fract(var1.time * 0.18F + var3 * 0.157F);
         double var5 = var3 * 2.2 + var1.time * 0.7 + var4 * 2.5;
         float var7 = 7.0F + var3 % 3 * 1.5F;
         var0.push();
         var0.translate(Geo.cos(var5) * var7, 4.0F - var4 * 28.0F, Geo.sin(var5) * var7);
         var0.rotY(var1.time * 80.0F + var3 * 50);
         var0.rotZ(Geo.sin(var1.time * 2.0F + var3) * 40.0F);
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var4 * Math.PI) * 2.5)), var2[var3 % 4]));
         var0.plane("leaf", 0.0F, 0.0F, 2.3F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1);
   }

   static void runes(G var0, Cos.A var1) {
      var0.glow(true);
      float var2 = 0.8F + 0.2F * Geo.sin(var1.time * 2.5);
      var0.push();
      var0.translate(0.0F, -23.8F, 0.0F);
      var0.rotY(var1.time * 25.0F);
      var0.rotX(-90.0F);
      var0.color(Geo.argb((int)(255.0F * var2), 16777215));
      var0.plane("rune_circle", 0.0F, 0.0F, 22.0F, 22.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();

      for (int var3 = 0; var3 < 8; var3++) {
         float var4 = fract(var1.time * 0.3F + var3 / 8.0F);
         double var5 = (Math.PI * 2) * var3 / 8.0 + var1.time * 0.4;
         var0.push();
         var0.translate(Geo.cos(var5) * 8.5F, -23.0F + var4 * 16.0F, Geo.sin(var5) * 8.5F);
         var0.rotY((float)Math.toDegrees(-var5) + 90.0F);
         var0.color(Geo.argb((int)(255.0 * Math.sin(var4 * Math.PI)), 16777215));
         var0.plane("glyph", 0.0F, 0.0F, 1.8F, 1.8F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1).glow(false);
   }

   static void bricks(G var0, Cos.A var1) {
      String[] var2 = new String[]{"lego_red", "lego_yellow", "lego_blue", "lego_green"};

      for (int var3 = 0; var3 < 8; var3++) {
         double var4 = (Math.PI * 2) * var3 / 8.0 + var1.time * 0.8;
         float var6 = -10.0F + Geo.sin(var1.time * 1.6 + var3 * 0.8) * 3.0F;
         var0.push();
         var0.translate(Geo.cos(var4) * 10.0F, var6, Geo.sin(var4) * 10.0F);
         var0.rotY(var1.time * 60.0F + var3 * 45);
         var0.rotX(Geo.sin(var1.time + var3) * 25.0F);
         String var7 = var2[var3 % 4];
         var0.box(var7, -1.6F, -0.7F, -0.8F, 1.6F, 0.7F, 0.8F, 0.0F, 0.0F, 1.0F, 1.0F);

         for (byte var8 = -1; var8 <= 1; var8 += 2) {
            var0.push();
            var0.translate(var8 * 0.8F, 0.7F, 0.0F);
            var0.cylinder(var7, 0.45F, 0.45F, 0.0F, 0.45F, 8, true, 0.1F, 0.1F, 0.9F, 0.9F);
            var0.pop();
         }

         var0.pop();
      }
   }
}
