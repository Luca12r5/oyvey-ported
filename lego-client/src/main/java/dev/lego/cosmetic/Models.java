package dev.lego.cosmetic;

final class Models {
   private Models() {
   }

   static void registerAll() {
      Cos.cape("lego", "Lego-Umhang", Cos.Rarity.RARE);
      Cos.cape("galaxy", "Galaxie", Cos.Rarity.EPIC);
      Cos.cape("flames", "Flammen", Cos.Rarity.RARE);
      Cos.cape("ice", "Eiskristall", Cos.Rarity.RARE);
      Cos.cape("aurora", "Polarlicht", Cos.Rarity.EPIC);
      Cos.cape("sunset", "Retro-Sonnenuntergang", Cos.Rarity.RARE);
      Cos.cape("carbon", "Carbon", Cos.Rarity.COMMON);
      Cos.cape("neon", "Neon-Grid", Cos.Rarity.EPIC);
      Cos.cape("spotify", "Spotify", Cos.Rarity.LEGENDARY);
      Cos.cape("sakura", "Kirschblüte", Cos.Rarity.EPIC);
      Cos.cape("lava", "Lavastrom", Cos.Rarity.EPIC);
      Cos.cape("ocean", "Tiefsee", Cos.Rarity.RARE);
      Cos.cape("matrix", "Matrix-Code", Cos.Rarity.EPIC);
      Cos.cape("storm", "Gewitter", Cos.Rarity.EPIC);
      Cos.cape("rainbow", "Regenbogen-Seide", Cos.Rarity.RARE);
      Cos.cape("snow", "Winternacht", Cos.Rarity.RARE);
      Cos.cape("fireflies", "Glühwürmchen", Cos.Rarity.EPIC);
      Cos.cape("heart", "Herzschlag", Cos.Rarity.RARE);
      Cos.cape("enchant", "Verzaubert", Cos.Rarity.LEGENDARY);
      Cos.cape("void", "Schwarzes Loch", Cos.Rarity.LEGENDARY);
      Cos.cape("royal", "Königsmantel", Cos.Rarity.LEGENDARY);
      Cos.cape("dragon", "Drachenschuppen", Cos.Rarity.EPIC);
      Cos.cape("camo", "Tarnmuster", Cos.Rarity.COMMON);
      Cos.cape("bricks", "Bausteine", Cos.Rarity.COMMON);
      Cos.cape("racing", "Zielflagge", Cos.Rarity.COMMON);
      Cos.cape("moon", "Mondnacht", Cos.Rarity.RARE);
      Wings.register();
      Cos.add("hat_crown", "Königskrone", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, HdHats::crown);
      Cos.add("hat_halo", "Heiligenschein", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::halo);
      Cos.add("hat_tophat", "Zylinder", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::tophat);
      Cos.add("hat_santa", "Weihnachtsmütze", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::santa);
      Cos.add("hat_witch", "Hexenhut", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::witch);
      Cos.add("hat_cat", "Katzenohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::catEars);
      Cos.add("hat_bunny", "Hasenohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::bunnyEars);
      Cos.add("hat_horns", "Teufelshörner", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::horns);
      Cos.add("hat_headphones", "Kopfhörer", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::headphones);
      Models2.register();
      Cos.add("face_sunglasses", "Sonnenbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::sunglasses);
      Cos.add("face_pixel", "Pixel-Brille", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::pixelGlasses);
      Models3.registerFace();
      Cos.add("back_lego", "Lego-Rucksack", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::legoBackpack);
      Cos.add("back_sword", "Kristallschwert", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::backSword);
      Models3.registerBack();
      Auras.register();
      Cos3Models.registerAll();
   }

   static void capePreview(G var0, Cos.A var1) {
   }

   static void capeGeometry(G var0, Cos.A var1, String var2) {
      String var3 = Capes.frameName(var2, var1.time);
      var0.push();
      var0.translate(0.0F, 0.0F, -2.25F);
      var0.rotX(var1.sneak ? 22.0F : 6.0F + var1.move * 20.0F + (float)Math.sin(var1.time * 1.3) * 1.5F);
      float var4 = 512.0F;
      float var5 = 256.0F;
      float var6 = 8.0F / var4;
      float var7 = 88.0F / var4;
      float var8 = 96.0F / var4;
      float var9 = 176.0F / var4;
      byte var10 = 4;
      float var11 = 0.0F;
      float var12 = 0.0F;

      for (int var13 = 0; var13 < var10; var13++) {
         float var14 = (float)Math.toRadians(var13 * 3.0 + Math.sin(var1.time * 1.7 - var13 * 0.8) * 1.2);
         float var15 = 16.0F / var10;
         float var16 = var11 - var15 * (float)Math.cos(var14);
         float var17 = var12 - var15 * (float)Math.sin(var14);
         float var18 = (8.0F + 128.0F * var13 / var10) / var5;
         float var19 = (8.0F + 128.0F * (var13 + 1) / var10) / var5;
         var0.quad(
            var3,
            new float[]{-5.0F, var11, var12 - 1.0F},
            new float[]{5.0F, var11, var12 - 1.0F},
            new float[]{5.0F, var16, var17 - 1.0F},
            new float[]{-5.0F, var16, var17 - 1.0F},
            new float[]{var6, var18, var7, var18, var7, var19, var6, var19}
         );
         var0.quad(
            var3,
            new float[]{5.0F, var11, var12},
            new float[]{-5.0F, var11, var12},
            new float[]{-5.0F, var16, var17},
            new float[]{5.0F, var16, var17},
            new float[]{var8, var18, var9, var18, var9, var19, var8, var19}
         );
         var0.quad(
            var3,
            new float[]{-5.0F, var11, var12},
            new float[]{-5.0F, var11, var12 - 1.0F},
            new float[]{-5.0F, var16, var17 - 1.0F},
            new float[]{-5.0F, var16, var17},
            new float[]{0.0F, var18, 8.0F / var4, var18, 8.0F / var4, var19, 0.0F, var19}
         );
         var0.quad(
            var3,
            new float[]{5.0F, var11, var12 - 1.0F},
            new float[]{5.0F, var11, var12},
            new float[]{5.0F, var16, var17},
            new float[]{5.0F, var16, var17 - 1.0F},
            new float[]{88.0F / var4, var18, 96.0F / var4, var18, 96.0F / var4, var19, 88.0F / var4, var19}
         );
         if (var13 == var10 - 1) {
            var0.quad(
               var3,
               new float[]{-5.0F, var16, var17},
               new float[]{5.0F, var16, var17},
               new float[]{5.0F, var16, var17 - 1.0F},
               new float[]{-5.0F, var16, var17 - 1.0F},
               new float[]{88.0F / var4, 0.0F, 168.0F / var4, 0.0F, 168.0F / var4, 8.0F / var5, 88.0F / var4, 8.0F / var5}
            );
         }

         var11 = var16;
         var12 = var17;
      }

      var0.quad(
         var3,
         new float[]{-5.0F, 0.0F, 0.0F},
         new float[]{5.0F, 0.0F, 0.0F},
         new float[]{5.0F, 0.0F, -1.0F},
         new float[]{-5.0F, 0.0F, -1.0F},
         new float[]{8.0F / var4, 0.0F, 88.0F / var4, 0.0F, 88.0F / var4, 8.0F / var5, 8.0F / var4, 8.0F / var5}
      );
      var0.pop();
   }

   static void crown(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 8.05F, 0.0F);
      var0.rotZ(-6.0F);
      var0.cylinder("gold", 4.55F, 4.55F, 0.0F, 2.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("gold", 4.35F, 4.35F, 0.0F, 2.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.ring("gold", 4.35F, 4.55F, 2.3F, 20, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int var2 = 0; var2 < 8; var2++) {
         var0.push();
         var0.rotY(var2 * 45 + 22.5F);
         var0.translate(0.0F, 2.3F, 4.45F);
         float var3 = var2 % 2 == 0 ? 3.2F : 2.3F;
         var0.quad(
            "gold",
            new float[]{-1.4F, 0.0F, 0.0F},
            new float[]{1.4F, 0.0F, 0.0F},
            new float[]{0.05F, var3, 0.0F},
            new float[]{-0.05F, var3, 0.0F},
            new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.5F, 0.0F, 0.5F, 0.0F}
         );
         var0.push();
         var0.translate(0.0F, var3 + 0.35F, 0.0F);
         var0.sphere("gems", 0.55F, 8, 5, var2 % 4 * 0.25F, 0.0F, var2 % 4 * 0.25F + 0.25F, 1.0F);
         var0.pop();
         var0.pop();
      }

      for (int var4 = 0; var4 < 4; var4++) {
         var0.push();
         var0.rotY(var4 * 90);
         var0.translate(0.0F, 1.15F, 4.62F);
         var0.box("gems", -0.6F, -0.6F, -0.2F, 0.6F, 0.6F, 0.25F, var4 % 4 * 0.25F, 0.0F, var4 % 4 * 0.25F + 0.25F, 1.0F);
         var0.pop();
      }

      var0.pop();
   }

   static void halo(G var0, Cos.A var1) {
      float var2 = (float)Math.sin(var1.time * 2.2F) * 0.45F;
      var0.push();
      var0.translate(0.0F, 11.4F + var2, 0.0F);
      var0.rotX(-10.0F);
      var0.rotY(var1.time * 40.0F);
      var0.glow(true);
      var0.torus("halo", 4.5F, 0.5F, 28, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      float var3 = 0.75F + 0.25F * (float)Math.sin(var1.time * 3.0F);
      var0.color((int)(110.0F * var3) << 24 | 16771496);
      var0.torus("orb", 4.5F, 1.4F, 20, 6, 0.2F, 0.2F, 0.8F, 0.8F);

      for (int var4 = 0; var4 < 5; var4++) {
         double var5 = (Math.PI * 2) * var4 / 5.0;
         float var7 = 0.5F + 0.5F * (float)Math.sin(var1.time * 4.0F + var4 * 1.9);
         var0.color((int)(255.0F * var7) << 24 | 16777215);
         Geo.sprite(var0, "spark", (float)Math.cos(var5) * 4.5F, 0.3F, (float)Math.sin(var5) * 4.5F, 1.0F + var7 * 1.4F);
      }

      var0.color(-1).glow(false);
      var0.pop();
   }

   static void tophat(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 7.95F, 0.0F);
      var0.rotZ(-4.0F);
      var0.cylinder("felt_black", 6.9F, 6.9F, 0.0F, 0.55F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("felt_black", 4.15F, 4.3F, 0.55F, 8.2F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("band_red", 4.25F, 4.33F, 0.6F, 2.2F, 24, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void santa(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 7.4F, 0.0F);
      var0.cylinder("felt_red", 4.35F, 3.1F, 1.4F, 6.2F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, 6.2F, 0.0F);
      float var2 = (float)Math.sin(var1.time * 1.6F) * 5.0F;
      var0.rotZ(-55.0F + var2);
      var0.rotX(-10.0F);
      var0.cylinder("felt_red", 3.1F, 0.5F, 0.0F, 5.5F, 16, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(0.0F, 5.6F, 0.0F);
      var0.sphere("fur_white", 1.35F, 12, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.cylinder("fur_white", 4.75F, 4.75F, 0.0F, 1.9F, 24, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void witch(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 7.8F, 0.0F);
      var0.rotZ(-6.0F);
      var0.cylinder("felt_purple", 8.2F, 8.2F, 0.0F, 0.45F, 28, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("felt_purple", 4.3F, 3.1F, 0.45F, 5.2F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("band_green", 4.28F, 4.0F, 0.5F, 1.8F, 22, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, 1.15F, 4.2F);
      var0.box("gold", -1.1F, -0.8F, 0.0F, 1.1F, 0.8F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.translate(0.0F, 5.2F, 0.0F);
      var0.rotZ(-18.0F);
      var0.rotX(8.0F);
      var0.cylinder("felt_purple", 3.1F, 1.5F, 0.0F, 4.2F, 18, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(0.0F, 4.2F, 0.0F);
      var0.rotZ(-35.0F);
      var0.cylinder("felt_purple", 1.5F, 0.1F, 0.0F, 3.6F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void catEars(G var0, Cos.A var1) {
      float var2 = (float)Math.max(0.0, Math.sin(var1.time * 3.1F) - 0.85F) * 60.0F;

      for (byte var3 = 1; var3 >= -1; var3 -= 2) {
         var0.push();
         var0.scale(var3, 1.0F, 1.0F);
         var0.translate(2.4F, 7.6F, 0.4F);
         var0.rotZ(-14.0F - (var3 > 0 ? var2 : 0.0F));
         var0.plane("cat_ear", 0.0F, 2.4F, 3.8F, 4.2F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }
   }

   static void bunnyEars(G var0, Cos.A var1) {
      for (byte var2 = 1; var2 >= -1; var2 -= 2) {
         var0.push();
         var0.scale(var2, 1.0F, 1.0F);
         var0.translate(1.8F, 7.8F, 0.0F);
         float var3 = (float)Math.sin(var1.time * 2.4F + var2) * 6.0F + var1.move * 12.0F;
         var0.rotX(-8.0F - var3);
         var0.rotZ(-10.0F);
         var0.plane("bunny_ear", 0.0F, 4.6F, 3.0F, 9.2F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }
   }

   static void horns(G var0, Cos.A var1) {
      for (byte var2 = 1; var2 >= -1; var2 -= 2) {
         var0.push();
         var0.scale(var2, 1.0F, 1.0F);
         var0.translate(2.6F, 7.6F, 1.2F);
         var0.rotZ(-35.0F);
         float var3 = 1.2F;

         for (int var4 = 0; var4 < 6; var4++) {
            float var5 = var3 * 0.78F;
            var0.cylinder("horn", var3, var5, 0.0F, 1.4F, 10, var4 == 5, 0.0F, 1.0F - var4 / 6.0F, 1.0F, 1.0F - (var4 + 1) / 6.0F);
            var0.translate(0.0F, 1.35F, 0.0F);
            var0.rotZ(14.0F);
            var0.rotX(-4.0F);
            var3 = var5;
         }

         var0.pop();
      }
   }

   static void headphones(G var0, Cos.A var1) {
      var0.push();
      byte var2 = 12;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = Math.PI * var3 / var2;
         double var6 = Math.PI * (var3 + 1) / var2;
         float var8 = 5.3F;
         float var9 = (float)Math.cos(var4) * var8;
         float var10 = 3.8F + (float)Math.sin(var4) * var8 * 0.95F;
         float var11 = (float)Math.cos(var6) * var8;
         float var12 = 3.8F + (float)Math.sin(var6) * var8 * 0.95F;
         float var13 = (float)Math.cos(var4) * 0.8F;
         float var14 = (float)Math.sin(var4) * 0.8F;
         float var15 = (float)Math.cos(var6) * 0.8F;
         float var16 = (float)Math.sin(var6) * 0.8F;
         float var17 = -0.9F;
         float var18 = 0.9F;
         var0.quad(
            "headphone",
            new float[]{var9 + var13, var10 + var14, var17},
            new float[]{var9 + var13, var10 + var14, var18},
            new float[]{var11 + var15, var12 + var16, var18},
            new float[]{var11 + var15, var12 + var16, var17},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.quad(
            "headphone",
            new float[]{var9, var10, var18},
            new float[]{var9, var10, var17},
            new float[]{var11, var12, var17},
            new float[]{var11, var12, var18},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.quad(
            "headphone",
            new float[]{var9, var10, var18},
            new float[]{var9 + var13, var10 + var14, var18},
            new float[]{var11 + var15, var12 + var16, var18},
            new float[]{var11, var12, var18},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.quad(
            "headphone",
            new float[]{var9 + var13, var10 + var14, var17},
            new float[]{var9, var10, var17},
            new float[]{var11, var12, var17},
            new float[]{var11 + var15, var12 + var16, var17},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      for (byte var19 = 1; var19 >= -1; var19 -= 2) {
         var0.push();
         var0.translate(var19 * 4.2F, 3.4F, 0.0F);
         var0.rotZ(var19 * -90);
         var0.cylinder("headphone", 2.5F, 2.5F, 0.0F, 1.8F, 18, true, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.translate(0.0F, 1.82F, 0.0F);
         var0.cylinder("cushion", 2.2F, 2.2F, 0.0F, 0.05F, 18, true, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.pop();
   }

   static void sunglasses(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.6F, 4.12F);
      var0.plane("sunglasses", 0.0F, 0.0F, 9.0F, 2.25F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (byte var2 = 1; var2 >= -1; var2 -= 2) {
         var0.push();
         var0.translate(var2 * 4.3F, 0.4F, 0.0F);
         var0.box("felt_black", -0.18F, -0.25F, -5.5F, 0.18F, 0.25F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.pop();
   }

   static void pixelGlasses(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.5F, 4.1F);
      var0.plane("pixelglasses", 0.0F, 0.0F, 8.0F, 2.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void legoBackpack(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -1.4F, -2.1F);
      float[][] var2 = new float[][]{
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F},
         {0.0F, 0.0F, 1.0F, 1.0F}
      };
      var0.box6("lego_red", -3.2F, -8.2F, -3.4F, 3.2F, 0.0F, 0.0F, var2);

      for (int var3 = 0; var3 < 2; var3++) {
         for (int var4 = 0; var4 < 2; var4++) {
            var0.push();
            var0.translate(-1.5F + var3 * 3, 0.0F, -0.85F - var4 * 1.7F);
            var0.cylinder("lego_red", 0.95F, 0.95F, 0.0F, 0.9F, 14, true, 0.1F, 0.1F, 0.9F, 0.9F);
            var0.pop();
         }
      }

      var0.box("lego_yellow", -2.2F, -6.6F, -3.55F, 2.2F, -4.4F, -3.3F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (byte var5 = 1; var5 >= -1; var5 -= 2) {
         var0.box("strap", var5 * 2.1F - 0.55F, -6.0F, 0.0F, var5 * 2.1F + 0.55F, 0.3F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.box("strap", var5 * 2.1F - 0.55F, 0.0F, 0.0F, var5 * 2.1F + 0.55F, 0.35F, 4.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.box("strap", var5 * 2.1F - 0.55F, -5.5F, 4.1F, var5 * 2.1F + 0.55F, 0.35F, 4.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      var0.pop();
   }

   static void backSword(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -5.5F, -2.6F);
      var0.rotZ(-40.0F);
      float var2 = 14.0F;
      float var3 = 1.3F;
      var0.glow(false);
      var0.quad(
         "sword_blade",
         new float[]{-var3, 2.0F, -0.1F},
         new float[]{var3, 2.0F, -0.1F},
         new float[]{var3, 2.0F + var2, -0.1F},
         new float[]{-var3, 2.0F + var2, -0.1F},
         new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F}
      );
      var0.quad(
         "sword_blade",
         new float[]{-var3, 2.0F + var2, -0.1F},
         new float[]{var3, 2.0F + var2, -0.1F},
         new float[]{0.0F, 4.0F + var2, -0.1F},
         new float[]{0.0F, 4.0F + var2, -0.1F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F}
      );
      var0.quad(
         "sword_blade",
         new float[]{-var3, 2.0F, -0.5F},
         new float[]{var3, 2.0F, -0.5F},
         new float[]{var3, 2.0F + var2, -0.5F},
         new float[]{-var3, 2.0F + var2, -0.5F},
         new float[]{0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F}
      );
      var0.quad(
         "sword_blade",
         new float[]{-var3, 2.0F + var2, -0.5F},
         new float[]{var3, 2.0F + var2, -0.5F},
         new float[]{0.0F, 4.0F + var2, -0.5F},
         new float[]{0.0F, 4.0F + var2, -0.5F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.5F, 0.0F}
      );
      var0.box("sword_blade", -var3, 2.0F, -0.5F, var3, 2.0F + var2, -0.1F, 0.4F, 0.1F, 0.6F, 0.9F);
      var0.glow(true);
      float var4 = var1.time * 0.6F % 1.0F;
      var0.color(-1432684801);
      var0.box("orb", -var3 * 0.5F, 2.0F + var2 * var4, -0.55F, var3 * 0.5F, 2.0F + var2 * var4 + 2.0F, -0.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false);
      var0.color(-10863968);
      var0.box("steel", -3.2F, 1.1F, -0.8F, 3.2F, 2.1F, 0.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.box("leather_dark", -0.5F, -2.8F, -0.6F, 0.5F, 1.1F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-8054);
      var0.push();
      var0.translate(0.0F, -3.4F, -0.3F);
      var0.sphere("gold", 0.85F, 10, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 1.6F, 0.25F);
      var0.color(-1);
      var0.sphere("gems", 0.5F, 8, 5, 0.25F, 0.0F, 0.5F, 1.0F);
      var0.pop();
      var0.color(-1);
      var0.pop();
   }
}
