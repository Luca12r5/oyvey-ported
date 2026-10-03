package dev.lego.cosmetic;

final class Models2 {
   private Models2() {
   }

   static void register() {
      Cos.add("hat_fox", "Fuchsohren", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::foxEars);
      Cos.add("hat_cowboy", "Cowboyhut", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::cowboy);
      Cos.add("hat_beanie", "Bommelmütze", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::beanie);
      Cos.add("hat_flowers", "Blumenkranz", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::flowerCrown);
      Cos.add("hat_viking", "Wikingerhelm", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::viking);
      Cos.add("hat_propeller", "Propellermütze", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::propeller);
      Cos.add("hat_chef", "Kochmütze", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::chef);
      Cos.add("hat_party", "Partyhut", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::party);
      Cos.add("hat_wizard", "Zaubererhut", Cos.Slot.HAT, Cos.Rarity.LEGENDARY, HdHats::wizard);
      Cos.add("hat_alien", "Alien-Antennen", Cos.Slot.HAT, Cos.Rarity.RARE, HdHats::alien);
      Cos.add("hat_bow", "Große Schleife", Cos.Slot.HAT, Cos.Rarity.COMMON, HdHats::bow);
      Cos.add("hat_mushroom", "Pilzhut", Cos.Slot.HAT, Cos.Rarity.EPIC, HdHats::mushroom);
   }

   static void foxEars(G var0, Cos.A var1) {
      float var2 = (float)Math.max(0.0, Math.sin(var1.time * 2.7F) - 0.8F) * 70.0F;

      for (byte var3 = 1; var3 >= -1; var3 -= 2) {
         var0.push();
         var0.scale(var3, 1.0F, 1.0F);
         var0.translate(2.3F, 7.7F, 0.2F);
         var0.rotZ(-16.0F - (var3 > 0 ? var2 : 0.0F));
         var0.rotX(-8.0F);
         float var4 = 4.4F;
         float var5 = 5.4F;
         var0.push();
         var0.rotY(18.0F);
         var0.quad(
            "fox_ear",
            new float[]{-var4 / 2.0F, 0.0F, 0.0F},
            new float[]{0.0F, 0.0F, 0.0F},
            new float[]{0.0F, var5, 0.0F},
            new float[]{-var4 / 2.0F, var5, 0.0F},
            new float[]{0.0F, 1.0F, 0.5F, 1.0F, 0.5F, 0.0F, 0.0F, 0.0F}
         );
         var0.pop();
         var0.push();
         var0.rotY(-18.0F);
         var0.quad(
            "fox_ear",
            new float[]{0.0F, 0.0F, 0.0F},
            new float[]{var4 / 2.0F, 0.0F, 0.0F},
            new float[]{var4 / 2.0F, var5, 0.0F},
            new float[]{0.0F, var5, 0.0F},
            new float[]{0.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.5F, 0.0F}
         );
         var0.pop();
         var0.pop();
      }
   }

   static void cowboy(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 7.3F, 0.0F);
      var0.rotZ(-4.0F);
      var0.color(-3630486);
      byte var2 = 28;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = (Math.PI * 2) * var3 / var2;
         double var6 = (Math.PI * 2) * (var3 + 1) / var2;
         float var8 = Geo.cos(var4);
         float var9 = Geo.sin(var4);
         float var10 = Geo.cos(var6);
         float var11 = Geo.sin(var6);
         float var12 = 4.6F;
         float var13 = 8.4F;
         float var14 = 2.2F * var8 * var8 - 0.4F * var9;
         float var15 = 2.2F * var10 * var10 - 0.4F * var11;
         var0.quad(
            "leather",
            new float[]{var8 * var13, var14, var9 * var13 * 1.08F},
            new float[]{var10 * var13, var15, var11 * var13 * 1.08F},
            new float[]{var10 * var12, 0.0F, var11 * var12},
            new float[]{var8 * var12, 0.0F, var9 * var12},
            new float[]{(float)var3 / var2, 0.0F, (var3 + 1.0F) / var2, 0.0F, (var3 + 1.0F) / var2, 1.0F, (float)var3 / var2, 1.0F}
         );
      }

      Geo.sqTube(var0, "leather", 4.7F, 4.2F, 0.0F, 5.2F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqDome(var0, "leather", 4.2F, 0.9F, 5.2F, 5.0F, 20, 2, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.box("leather_dark", -0.4F, 5.6F, -3.2F, 0.4F, 6.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(var0, "leather_dark", 4.78F, 4.68F, 0.3F, 1.4F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.push();
      var0.translate(0.0F, 0.85F, 4.75F);
      var0.color(-8054);
      var0.box("gold", -0.8F, -0.6F, 0.0F, 0.8F, 0.6F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
      var0.pop();
   }

   static void beanie(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 5.2F, 0.0F);
      var0.color(-12948768);
      Geo.sqDome(var0, "knit", 4.75F, 4.4F, 1.8F, 5.0F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(var0, "knit", 4.75F, 4.75F, 0.0F, 1.8F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-723724);
      Geo.sqTube(var0, "knit", 4.95F, 4.95F, -0.2F, 1.9F, 5.0F, 22, 0.0F, 0.0F, 4.0F, 1.0F);
      float var2 = Geo.sin(var1.time * 3.0F) * 6.0F + var1.move * 10.0F;
      var0.translate(0.0F, 6.1F, 0.0F);
      var0.rotZ(var2);
      var0.rotX(Geo.sin(var1.time * 2.2) * 5.0F);
      var0.color(-1);
      var0.translate(0.0F, 0.9F, 0.0F);
      var0.sphere("fur_white", 1.8F, 12, 8, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }

   static void flowerCrown(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 8.15F, 0.0F);
      var0.torus("vine", 4.95F, 0.42F, 24, 5, 0.0F, 0.0F, 4.0F, 1.0F);
      String[] var2 = new String[]{"flower_pink", "flower_white", "flower_yellow", "flower_blue"};
      byte var3 = 12;

      for (int var4 = 0; var4 < var3; var4++) {
         double var5 = (Math.PI * 2) * var4 / var3;
         var0.push();
         var0.translate(Geo.cos(var5) * 5.0F, 0.35F + 0.2F * Geo.sin(var4 * 1.7), Geo.sin(var5) * 5.0F);
         var0.rotY((float)(-Math.toDegrees(var5)) + 90.0F);
         var0.rotX(-25.0F + Geo.sin(var1.time * 2.0F + var4) * 6.0F);
         var0.rotZ(var1.time * 10.0F + var4 * 40);
         float var7 = 2.2F + 0.5F * (var4 * 7 % 3) / 2.0F;
         var0.plane(var2[var4 % 4], 0.0F, 0.0F, var7, var7, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.pop();
   }

   static void viking(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 6.2F, 0.0F);
      var0.color(-2564892);
      Geo.sqDome(var0, "iron", 4.8F, 4.2F, 0.6F, 5.0F, 22, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1526694);
      Geo.sqTube(var0, "steel", 4.9F, 4.9F, -0.4F, 1.1F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (int var2 = 0; var2 < 12; var2++) {
         float[] var3 = Geo.sq((Math.PI * 2) * var2 / 12.0, 4.95F, 5.0F);
         var0.push();
         var0.translate(var3[0], 0.35F, var3[1]);
         var0.sphere("steel", 0.28F, 5, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1526694);
      var0.box("steel", -0.5F, -2.4F, 4.6F, 0.5F, 0.4F, 5.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (byte var7 = 1; var7 >= -1; var7 -= 2) {
         float[][] var8 = new float[7][];
         float[] var4 = new float[7];

         for (int var5 = 0; var5 < 7; var5++) {
            float var6 = var5 / 6.0F;
            var8[var5] = new float[]{var7 * (4.4F + var6 * 4.2F), 1.8F + var6 * var6 * 5.5F - var6 * 0.6F, 0.3F - var6 * 1.4F};
            var4[var5] = 1.25F * (1.0F - var6) + 0.08F;
         }

         Geo.chain(var0, "ivory", var8, var4, 8);
      }

      var0.pop();
   }

   static void propeller(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 6.3F, 0.0F);
      Geo.sqDome(var0, "cap_segments", 4.85F, 3.4F, 0.0F, 5.0F, 24, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-14787896);
      byte var2 = 10;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = Math.PI * (0.15 + 0.7 * var3 / var2);
         double var6 = Math.PI * (0.15 + 0.7 * (var3 + 1) / var2);
         var0.quad(
            "plastic",
            new float[]{Geo.cos(var4) * 4.9F, 0.1F, Geo.sin(var4) * 4.9F},
            new float[]{Geo.cos(var6) * 4.9F, 0.1F, Geo.sin(var6) * 4.9F},
            new float[]{Geo.cos(var6) * 8.3F, -0.5F, Geo.sin(var6) * 8.3F},
            new float[]{Geo.cos(var4) * 8.3F, -0.5F, Geo.sin(var4) * 8.3F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      var0.color(-13053);
      var0.translate(0.0F, 3.35F, 0.0F);
      var0.cylinder("plastic", 0.35F, 0.35F, 0.0F, 1.6F, 8, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(0.0F, 1.6F, 0.0F);
      var0.sphere("gloss", 0.7F, 8, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.rotY(var1.time * (var1.preview ? 260.0F : 720.0F + var1.move * 900.0F));
      int[] var8 = new int[]{-1900533, -16749385, -16736198};

      for (int var9 = 0; var9 < 3; var9++) {
         var0.push();
         var0.rotY(var9 * 120);
         var0.rotX(14.0F);
         var0.color(var8[var9]);
         var0.quad(
            "plastic",
            new float[]{0.4F, 0.0F, -0.9F},
            new float[]{5.4F, 0.0F, -1.2F},
            new float[]{5.4F, 0.0F, 1.2F},
            new float[]{0.4F, 0.0F, 0.9F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.pop();
      }

      var0.color(-1);
      var0.pop();
   }

   static void chef(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 6.6F, 0.0F);
      Geo.sqTube(var0, "cloth", 4.8F, 4.9F, 0.0F, 5.2F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1513236);
      Geo.sqTube(var0, "cloth", 4.95F, 4.95F, -0.1F, 1.3F, 5.0F, 22, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      float var2 = 1.0F + Geo.sin(var1.time * 1.5) * 0.03F;

      for (int var3 = 0; var3 < 6; var3++) {
         double var4 = (Math.PI * 2) * var3 / 6.0 + 0.3;
         Geo.ellipsoid(var0, "cloth", Geo.cos(var4) * 2.9F, 6.2F, Geo.sin(var4) * 2.9F, 2.9F * var2, 2.5F * var2, 2.9F * var2, 12, 8);
      }

      Geo.ellipsoid(var0, "cloth", 0.0F, 7.2F, 0.0F, 3.2F * var2, 2.6F * var2, 3.2F * var2, 12, 8);
      var0.pop();
   }

   static void party(G var0, Cos.A var1) {
      var0.push();
      var0.translate(1.2F, 7.9F, 0.0F);
      var0.rotZ(-14.0F + Geo.sin(var1.time * 2.2) * 4.0F);
      var0.cylinder("party_cone", 3.3F, 0.12F, 0.0F, 9.0F, 18, false, 0.0F, 1.0F, 1.0F, 0.0F);
      var0.color(-7606);
      var0.ring("cloth", 3.0F, 3.5F, 0.1F, 18, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.torus("fur_white", 3.35F, 0.45F, 18, 5, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(0.0F, 9.1F, 0.0F);
      var0.color(-38224);
      var0.sphere("fur_white", 1.3F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      int[] var2 = new int[]{-50550, -12924673, -7606, -9764998, -5215489};

      for (int var3 = 0; var3 < 12; var3++) {
         float var4 = (var1.time * 0.4F + var3 / 12.0F) % 1.0F;
         double var5 = var3 * 2.4 + var1.time * 0.5;
         var0.push();
         var0.translate(Geo.cos(var5) * (4 + var3 % 3), 20.0F - var4 * 16.0F, Geo.sin(var5) * (4 + var3 % 3));
         var0.rotY(var1.time * 200.0F + var3 * 40);
         var0.rotX(var1.time * 150.0F + var3 * 70);
         var0.color(Geo.argb((int)(255.0 * Math.min(1.0, Math.sin(var4 * Math.PI) * 2.0)), var2[var3 % 5]));
         var0.plane("plastic", 0.0F, 0.0F, 0.8F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1);
   }

   static void wizard(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 7.8F, 0.0F);
      var0.rotZ(-5.0F);
      var0.color(-14016886);
      byte var2 = 28;

      for (int var3 = 0; var3 < var2; var3++) {
         double var4 = (Math.PI * 2) * var3 / var2;
         double var6 = (Math.PI * 2) * (var3 + 1) / var2;
         float var8 = -0.5F * (1.0F + Geo.sin(var4 * 3.0 + 1.0)) * 0.5F;
         float var9 = -0.5F * (1.0F + Geo.sin(var6 * 3.0 + 1.0)) * 0.5F;
         var0.quad(
            "felt_purple",
            new float[]{Geo.cos(var4) * 9.0F, var8, Geo.sin(var4) * 9.0F},
            new float[]{Geo.cos(var6) * 9.0F, var9, Geo.sin(var6) * 9.0F},
            new float[]{Geo.cos(var6) * 4.3F, 0.2F, Geo.sin(var6) * 4.3F},
            new float[]{Geo.cos(var4) * 4.3F, 0.2F, Geo.sin(var4) * 4.3F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
      }

      var0.color(-1);
      float var11 = 4.5F;
      float var12 = 0.2F;
      float var5 = Geo.sin(var1.time * 1.4) * 4.0F;

      for (int var13 = 0; var13 < 5; var13++) {
         float var7 = var11 * (var13 < 4 ? 0.74F : 0.05F);
         float var16 = 3.0F - var13 * 0.25F;
         var0.cylinder("wizard_cone", var11, var7, 0.0F, var16, 18, false, 0.0F, 1.0F - var13 / 5.0F, 1.0F, 1.0F - (var13 + 1) / 5.0F);
         var0.translate(0.0F, var16, 0.0F);
         var0.rotZ(-6 - var13 * 5 + var5 * 0.3F);
         var11 = var7;
      }

      var0.glow(true);
      float var14 = 0.6F + 0.4F * Geo.sin(var1.time * 5.0F);
      var0.color(Geo.argb((int)(255.0F * var14), 16777215));
      Geo.sprite(var0, "spark", 0.0F, 0.4F, 0.0F, 2.4F + var14);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 8.4F, 0.0F);
      var0.color(-7558);
      var0.cylinder("gold", 4.46F, 4.3F, 0.0F, 1.3F, 20, false, 0.0F, 0.0F, 1.0F, 1.0F);

      for (int var15 = 0; var15 < 6; var15++) {
         float var17 = (var1.time * 0.35F + var15 / 6.0F) % 1.0F;
         double var18 = var15 * 1.05 + var1.time * 0.9;
         var0.color(Geo.argb((int)(255.0 * Math.sin(var17 * Math.PI)), 16771232));
         Geo.sprite(var0, "spark", Geo.cos(var18) * (6.0F + var17 * 2.0F), 2.0F + var17 * 9.0F, Geo.sin(var18) * (6.0F + var17 * 2.0F), 1.4F);
      }

      var0.color(-1).glow(false);
      var0.pop();
   }

   static void alien(G var0, Cos.A var1) {
      for (byte var2 = 1; var2 >= -1; var2 -= 2) {
         float[][] var3 = new float[6][];
         float[] var4 = new float[6];

         for (int var5 = 0; var5 < 6; var5++) {
            float var6 = var5 / 5.0F;
            float var7 = Geo.sin(var1.time * 3.2 + var2 + var6 * 1.5) * 1.4F * var6 * var6 + var1.move * var6 * var6 * 1.5F;
            var3[var5] = new float[]{var2 * (1.8F + var6 * 1.8F) + var7 * 0.5F, 7.9F + var6 * 7.0F, var7 * 0.8F - var6 * var6 * 0.8F};
            var4[var5] = 0.32F - var6 * 0.12F;
         }

         var0.color(-11870630);
         Geo.chain(var0, "plastic", var3, var4, 6);
         float[] var8 = var3[5];
         float var9 = 0.8F + 0.2F * Geo.sin(var1.time * 4.0F + var2);
         var0.push();
         var0.translate(var8[0], var8[1] + 0.9F, var8[2]);
         var0.color(-8585366);
         var0.sphere("gloss", 1.1F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.glow(true);
         var0.color(Geo.argb((int)(200.0F * var9), 16777215));
         Geo.sprite(var0, "orb", 0.0F, 0.0F, 0.0F, 4.2F * var9);
         var0.glow(false);
         var0.pop();
      }

      var0.color(-11870630);
      var0.box("plastic", -2.4F, 7.9F, -0.6F, 2.4F, 8.4F, 0.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
   }

   static void bow(G var0, Cos.A var1) {
      var0.push();
      var0.translate(1.6F, 8.6F, 1.2F);
      var0.rotZ(-14.0F + Geo.sin(var1.time * 2.0F) * 2.0F);
      var0.rotY(-10.0F);
      var0.color(-42342);

      for (byte var2 = 1; var2 >= -1; var2 -= 2) {
         var0.push();
         var0.translate(var2 * 2.2F, 0.3F, 0.0F);
         var0.rotZ(var2 * 18);
         var0.scale(2.5F, 1.7F, 0.9F);
         var0.sphere("satin", 1.0F, 14, 9, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
         var0.push();
         var0.translate(var2 * 0.8F, -0.6F, 0.2F);
         var0.rotZ(var2 * 25 + Geo.sin(var1.time * 2.5 + var2) * 5.0F);
         var0.quad(
            "satin",
            new float[]{-0.7F, 0.0F, 0.0F},
            new float[]{0.7F, 0.0F, 0.0F},
            new float[]{0.9F, -3.2F, 0.4F},
            new float[]{-0.4F, -3.0F, 0.4F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.pop();
      }

      var0.color(-2080646);
      var0.sphere("satin", 0.95F, 10, 7, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.pop();
   }

   static void mushroom(G var0, Cos.A var1) {
      var0.push();
      float var2 = Geo.sin(var1.time * 2.0F) * 0.15F;
      var0.translate(0.0F, 7.4F + var2, 0.0F);
      var0.rotZ(Geo.sin(var1.time * 1.3) * 2.0F);
      Geo.sqDome(var0, "mushroom_cap", 6.4F, 4.6F, 0.6F, 2.6F, 26, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      Geo.sqTube(var0, "mushroom_cap", 6.4F, 6.4F, 0.1F, 0.6F, 2.6F, 26, 0.0F, 0.9F, 1.0F, 1.0F);
      var0.ring("gill", 4.2F, 6.4F, 0.1F, 26, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
   }
}
