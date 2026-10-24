package dev.lego.cosmetic;

final class Models3 {
   private Models3() {
   }

   static void registerFace() {
      Cos.add("face_hearts", "Herzbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::heartGlasses);
      Cos.add("face_stars", "Sternenbrille", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::starGlasses);
      Cos.add("face_monocle", "Monokel", Cos.Slot.FACE, Cos.Rarity.EPIC, HdFace::monocle);
      Cos.add("face_mustache", "Schnurrbart", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::mustache);
      Cos.add("face_clown", "Clownsnase", Cos.Slot.FACE, Cos.Rarity.COMMON, HdFace::clownNose);
      Cos.add("face_visor", "Cyber-Visier", Cos.Slot.FACE, Cos.Rarity.LEGENDARY, HdFace::visor);
      Cos.add("face_masq", "Maskenball", Cos.Slot.FACE, Cos.Rarity.EPIC, HdFace::masquerade);
      Cos.add("face_ninja", "Ninja-Maske", Cos.Slot.FACE, Cos.Rarity.RARE, HdFace::ninja);
   }

   static void registerBack() {
      Cos.add("back_guitar", "E-Gitarre", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::guitar);
      Cos.add("back_jetpack", "Jetpack", Cos.Slot.BACK, Cos.Rarity.LEGENDARY, HdBack::jetpack);
      Cos.add("back_shield", "Ritterschild", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::shield);
      Cos.add("back_quiver", "Köcher", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::quiver);
      Cos.add("back_rocket", "Rakete", Cos.Slot.BACK, Cos.Rarity.EPIC, HdBack::rocket);
      Cos.add("back_teddy", "Teddy", Cos.Slot.BACK, Cos.Rarity.COMMON, HdBack::teddy);
      Cos.add("back_balloon", "Herzballon", Cos.Slot.BACK, Cos.Rarity.RARE, HdBack::balloon);
   }

   private static void temples(G var0, float var1, float var2) {
      for (byte var3 = 1; var3 >= -1; var3 -= 2) {
         var0.box("felt_black", var3 * 4.12F - 0.16F, var1 - 0.22F, -3.5F, var3 * 4.12F + 0.16F, var1 + 0.22F, var2, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   static void heartGlasses(G var0, Cos.A var1) {
      float var2 = 1.0F + 0.06F * (float)Math.max(0.0, Math.sin(var1.time * 6.0F));
      var0.push();
      var0.translate(0.0F, 3.6F, 4.25F);
      var0.scale(var2, var2, 1.0F);
      var0.plane("heart_glasses", 0.0F, 0.0F, 9.2F, 3.45F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-2092705);
      temples(var0, 4.1F, 4.2F);
      var0.color(-1);
   }

   static void starGlasses(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.6F, 4.25F);
      var0.plane("star_glasses", 0.0F, 0.0F, 9.4F, 3.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(true);
      float var2 = 0.5F + 0.5F * Geo.sin(var1.time * 3.0F);
      var0.color(Geo.argb((int)(255.0F * var2), 16777215));
      Geo.sprite(var0, "spark", 3.6F, 1.2F, 0.2F, 1.2F + var2);
      var0.color(Geo.argb((int)(255.0F * (1.0F - var2)), 16777215));
      Geo.sprite(var0, "spark", -2.2F, -1.0F, 0.2F, 1.0F + (1.0F - var2));
      var0.glow(false).color(-1531888);
      var0.pop();
      temples(var0, 3.8F, 4.2F);
      var0.color(-1);
   }

   static void monocle(G var0, Cos.A var1) {
      var0.push();
      var0.translate(-1.9F, 3.7F, 4.25F);
      var0.color(-8054);
      var0.push();
      var0.rotX(90.0F);
      var0.torus("gold", 1.45F, 0.2F, 20, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
      var0.plane("monocle_glass", 0.0F, 0.0F, 2.9F, 2.9F, 0.0F, 0.0F, 1.0F, 1.0F);
      float var2 = Geo.sin(var1.time * 1.8) * 0.3F;
      byte var3 = 14;
      var0.color(-8054);

      for (int var4 = 0; var4 < var3; var4++) {
         float var5 = (float)var4 / (var3 - 1);
         float var6 = -1.2F - var5 * 2.2F + var2 * var5;
         float var7 = -1.4F - (float)Math.sin(var5 * Math.PI) * 2.8F - var5 * 0.5F;
         var0.push();
         var0.translate(var6, var7, 0.1F - var5 * 0.4F);
         var0.sphere("gold", 0.17F, 5, 3, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.color(-1);
      var0.pop();
   }

   static void mustache(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 1.55F, 4.22F);
      var0.rotZ(Geo.sin(var1.time * 2.2) * 1.5F);
      float var2 = Math.max(0.0F, Geo.sin(var1.time * 1.3)) * 0.1F;
      var0.scale(1.0F + var2, 1.0F, 1.0F);
      var0.push();
      var0.rotY(6.0F);
      var0.quad(
         "mustache",
         new float[]{-3.8F, 1.15F, 0.0F},
         new float[]{0.0F, 1.15F, 0.0F},
         new float[]{0.0F, -1.15F, 0.0F},
         new float[]{-3.8F, -1.15F, 0.0F},
         new float[]{0.0F, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.0F, 1.0F}
      );
      var0.pop();
      var0.push();
      var0.rotY(-6.0F);
      var0.quad(
         "mustache",
         new float[]{0.0F, 1.15F, 0.0F},
         new float[]{3.8F, 1.15F, 0.0F},
         new float[]{3.8F, -1.15F, 0.0F},
         new float[]{0.0F, -1.15F, 0.0F},
         new float[]{0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.5F, 1.0F}
      );
      var0.pop();
      var0.pop();
   }

   static void clownNose(G var0, Cos.A var1) {
      float var2 = 1.0F + 0.05F * Geo.sin(var1.time * 3.0F);
      var0.push();
      var0.translate(0.0F, 2.6F, 4.9F);
      var0.scale(var2, 1.0F / var2, var2);
      var0.color(-54742);
      var0.sphere("gloss", 1.25F, 14, 9, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.pop();
   }

   static void visor(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.7F, 0.0F);
      byte var2 = 10;
      float var3 = 5.0F;
      float var4 = 2.8F;

      for (int var5 = 0; var5 < var2; var5++) {
         double var6 = Math.PI * (0.12 + 0.76 * var5 / var2);
         double var8 = Math.PI * (0.12 + 0.76 * (var5 + 1) / var2);
         float[] var10 = Geo.sq(var6, var3, 5.0F);
         float[] var11 = Geo.sq(var8, var3, 5.0F);
         float var12 = 1.0F - (float)var5 / var2;
         float var13 = 1.0F - (var5 + 1.0F) / var2;
         var0.quad(
            "visor",
            new float[]{var10[0], var4 / 2.0F, var10[1]},
            new float[]{var11[0], var4 / 2.0F, var11[1]},
            new float[]{var11[0], -var4 / 2.0F, var11[1]},
            new float[]{var10[0], -var4 / 2.0F, var10[1]},
            new float[]{var12, 0.0F, var13, 0.0F, var13, 1.0F, var12, 1.0F}
         );
      }

      var0.glow(true);
      float var14 = 0.5F + 0.5F * Geo.sin(var1.time * 2.4);
      double var15 = Math.PI * (0.2 + 0.6 * var14);
      float[] var16 = Geo.sq(var15, var3 + 0.08F, 5.0F);
      var0.color(-50598);
      var0.push();
      var0.translate(var16[0], 0.0F, var16[1]);
      var0.rotY((float)(-Math.toDegrees(var15)) + 90.0F);
      var0.plane("glow_line", 0.0F, 0.0F, 1.6F, 1.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1275119014);
      var0.plane("orb", 0.0F, 0.0F, 4.0F, 3.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-13965569);

      for (byte var9 = 1; var9 >= -1; var9 -= 2) {
         var0.box("white", var9 * 4.5F - 0.15F, -0.2F, -1.4F, var9 * 4.5F + 0.15F, 0.2F, 1.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      var0.glow(false);
      var0.color(-14012874);
      var0.box("steel", -4.4F, -0.7F, -4.4F, -4.05F, 0.7F, -1.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.box("steel", 4.05F, -0.7F, -4.4F, 4.4F, 0.7F, -1.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.box("steel", -4.4F, -0.7F, -4.4F, 4.4F, 0.7F, -4.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.pop();
   }

   static void masquerade(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, 3.9F, 4.25F);
      var0.plane("mask_masq", 0.0F, 0.0F, 9.6F, 4.8F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.translate(3.6F, 1.8F, 0.1F);
      var0.rotZ(-28.0F + Geo.sin(var1.time * 2.0F) * 5.0F);
      var0.plane("feather_plume", 0.0F, 2.6F, 1.6F, 6.0F, 0.0F, 1.0F, 1.0F, 0.0F);
      var0.rotZ(22.0F);
      var0.plane("feather_plume", 0.0F, 2.2F, 1.4F, 5.0F, 0.0F, 1.0F, 1.0F, 0.0F);
      var0.pop();
   }

   static void ninja(G var0, Cos.A var1) {
      var0.push();
      var0.color(-14539732);
      Geo.sqTube(var0, "cloth", 4.35F, 4.35F, -0.1F, 3.0F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-3662294);
      Geo.sqTube(var0, "cloth", 4.45F, 4.45F, 5.4F, 6.6F, 5.0F, 20, 0.0F, 0.0F, 1.0F, 1.0F);
      float var2 = Geo.sin(var1.time * 4.0F) * 10.0F + var1.move * 25.0F;

      for (byte var3 = -1; var3 <= 1; var3 += 2) {
         var0.push();
         var0.translate(var3 * 0.6F, 6.0F, -4.4F);
         var0.rotY(var3 * 15);
         var0.rotX(-20.0F - var2 * 0.5F + var3 * 5);
         var0.quad(
            "cloth",
            new float[]{-0.5F, 0.0F, 0.0F},
            new float[]{0.5F, 0.0F, 0.0F},
            new float[]{0.4F, -5.0F, -0.2F},
            new float[]{-0.6F, -5.0F, -0.2F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.pop();
      }

      var0.color(-1);
      var0.pop();
   }

   static void guitar(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -7.0F, -2.9F);
      var0.rotZ(-32.0F + Geo.sin(var1.time * 1.5) * 1.2F);
      var0.rotY(180.0F);
      var0.color(-1);
      Geo.ellipsoid(var0, "guitar_body", 0.0F, -2.4F, 0.0F, 3.6F, 3.4F, 0.8F, 16, 8);
      Geo.ellipsoid(var0, "guitar_body", 0.0F, 1.6F, 0.0F, 2.7F, 2.5F, 0.78F, 16, 8);
      var0.box("wood", -0.55F, 3.5F, -0.25F, 0.55F, 12.5F, 0.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-15066594);
      var0.box("plastic", -0.95F, 12.5F, -0.25F, 0.95F, 15.0F, 0.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (int var2 = 0; var2 < 3; var2++) {
         for (byte var3 = -1; var3 <= 1; var3 += 2) {
            var0.box("steel", var3 * 0.95F - 0.25F, 12.9F + var2 * 0.7F, -0.05F, var3 * 0.95F + 0.25F, 13.2F + var2 * 0.7F, 0.15F, 0.0F, 0.0F, 1.0F, 1.0F);
         }
      }

      var0.quad(
         "strings",
         new float[]{-0.45F, 12.5F, 0.42F},
         new float[]{0.45F, 12.5F, 0.42F},
         new float[]{0.45F, -3.5F, 0.85F},
         new float[]{-0.45F, -3.5F, 0.85F},
         new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
      );
      var0.color(-15066594);
      var0.box("plastic", -1.3F, -3.9F, 0.6F, 1.3F, -3.3F, 0.95F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-14018030);
      var0.push();
      var0.rotZ(-40.0F);
      var0.box("leather_dark", -0.6F, -9.0F, 2.05F, 0.6F, 3.0F, 2.35F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.box("leather_dark", -0.6F, -9.0F, -2.35F, 0.6F, 3.0F, -2.05F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
   }

   static void jetpack(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -1.5F, -2.1F);
      var0.color(-12960184);
      var0.box("steel", -3.4F, -8.5F, -1.6F, 3.4F, -0.8F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);

      for (byte var2 = -1; var2 <= 1; var2 += 2) {
         var0.push();
         var0.translate(var2 * 2.1F, -1.5F, -2.9F);
         var0.color(-1512206);
         var0.cylinder("steel", 1.85F, 1.85F, -7.5F, 0.0F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.push();
         var0.scale(1.0F, 0.6F, 1.0F);
         var0.sphere("steel", 1.85F, 14, 6, 0.0F, 0.5F, 1.0F, 1.0F);
         var0.pop();
         var0.color(-1900533);
         var0.cylinder("plastic", 1.9F, 1.9F, -2.2F, -1.3F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.color(-14012874);
         var0.cylinder("iron", 1.3F, 0.8F, -9.2F, -7.5F, 12, false, 0.0F, 0.0F, 1.0F, 1.0F);
         float var3 = 1.0F + 0.25F * Geo.sin(var1.time * 22.0F + var2) + var1.move * 0.6F;
         var0.glow(true);
         var0.color(-1);
         var0.cross("flame", 0.0F, -9.2F - 2.6F * var3, 0.0F, 1.9F, 5.2F * var3, 0.0F, 1.0F, 1.0F, 0.0F);
         var0.color(-1711276033);
         var0.cross("orb", 0.0F, -9.4F, 0.0F, 3.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);

         for (int var4 = 0; var4 < 3; var4++) {
            float var5 = (var1.time * 2.2F + var4 / 3.0F + (var2 > 0 ? 0.5F : 0.0F)) % 1.0F;
            var0.color(Geo.argb((int)(140.0F * (1.0F - var5)), 16777215));
            Geo.sprite(
               var0, "orb_purple", Geo.sin(var4 * 2.1 + var1.time) * var5 * 1.5F, -11.0F - var5 * 7.0F * var3, Geo.cos(var4 * 2.1) * var5, 1.5F + var5 * 3.0F
            );
         }

         var0.glow(false);
         var0.pop();
      }

      var0.color(-1);

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         var0.color(-14012874);
         var0.box("leather_dark", var6 * 2.2F - 0.5F, -7.0F, 2.0F, var6 * 2.2F + 0.5F, 0.35F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.box("leather_dark", var6 * 2.2F - 0.5F, 0.0F, -2.1F, var6 * 2.2F + 0.5F, 0.35F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      var0.color(-1);
      var0.pop();
   }

   static void shield(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.0F, -6.2F, -2.5F);
      var0.rotZ(8.0F);
      var0.push();
      var0.rotX(90.0F);
      var0.color(-6643024);
      var0.cylinder("steel", 6.2F, 6.2F, -0.2F, 0.6F, 28, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (int var2 = 0; var2 < 28; var2++) {
         double var3 = (Math.PI * 2) * var2 / 28.0;
         double var5 = (Math.PI * 2) * (var2 + 1) / 28.0;

         for (float var10 : new float[]{-0.2F, 0.6F}) {
            Geo.tri(
               var0,
               "shield_face",
               new float[]{0.0F, var10, 0.0F},
               new float[]{Geo.cos(var3) * 6.2F, var10, Geo.sin(var3) * 6.2F},
               new float[]{Geo.cos(var5) * 6.2F, var10, Geo.sin(var5) * 6.2F},
               new float[]{0.5F, 0.5F},
               new float[]{0.5F + 0.5F * Geo.cos(var3), 0.5F + 0.5F * Geo.sin(var3)},
               new float[]{0.5F + 0.5F * Geo.cos(var5), 0.5F + 0.5F * Geo.sin(var5)}
            );
         }
      }

      var0.color(-2564892);
      var0.torus("steel", 6.2F, 0.5F, 28, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-8054);
      var0.translate(0.0F, -0.8F, 0.0F);
      var0.sphere("gold", 1.3F, 10, 6, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
      var0.pop();
   }

   static void quiver(G var0, Cos.A var1) {
      var0.push();
      var0.translate(1.5F, -5.5F, -3.4F);
      var0.rotZ(-24.0F);
      var0.color(-1);
      var0.cylinder("leather", 1.8F, 1.6F, -6.0F, 5.0F, 14, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-8054);
      var0.cylinder("gold", 1.9F, 1.9F, 4.0F, 4.8F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.cylinder("gold", 1.7F, 1.7F, -5.2F, -4.5F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);

      for (int var2 = 0; var2 < 5; var2++) {
         float var3 = -0.9F + var2 % 3 * 0.9F;
         float var4 = -0.5F + var2 / 3 * 1.0F;
         float var5 = 7.4F + var2 % 2 * 0.8F;
         var0.push();
         var0.translate(var3, 0.0F, var4);
         var0.rotZ((var2 - 2) * 3);
         var0.box("wood", -0.12F, 2.0F, -0.12F, 0.12F, var5, 0.12F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.plane("arrow_fletch", 0.0F, var5 - 0.6F, 1.2F, 2.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.rotY(90.0F);
         var0.plane("arrow_fletch", 0.0F, var5 - 0.6F, 1.2F, 2.4F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.pop();
      var0.color(-12966376);
      var0.push();
      var0.rotZ(38.0F);
      var0.box("leather_dark", -0.5F, -9.0F, 2.05F, 0.5F, 3.0F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
   }

   static void rocket(G var0, Cos.A var1) {
      var0.push();
      float var2 = var1.move * 0.15F * Geo.sin(var1.time * 40.0F);
      var0.translate(var2, -4.0F + 0.2F * Geo.sin(var1.time * 2.0F), -4.6F);
      var0.cylinder("rocket_body", 2.2F, 2.2F, -6.0F, 3.0F, 16, false, 0.0F, 1.0F, 1.0F, 0.0F);
      var0.color(-1900533);
      var0.cylinder("plastic", 2.2F, 0.05F, 3.0F, 7.0F, 16, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.push();
      var0.translate(0.0F, 0.8F, -2.18F);
      var0.rotY(180.0F);
      var0.plane("window", 0.0F, 0.0F, 2.2F, 2.2F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1900533);

      for (int var3 = 0; var3 < 4; var3++) {
         var0.push();
         var0.rotY(45 + var3 * 90);
         var0.quad(
            "plastic",
            new float[]{0.0F, -2.5F, 2.1F},
            new float[]{0.0F, -6.8F, 2.1F},
            new float[]{0.0F, -7.5F, 4.2F},
            new float[]{0.0F, -4.8F, 4.0F},
            new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}
         );
         var0.pop();
      }

      var0.color(-12960184);
      var0.cylinder("iron", 1.3F, 1.6F, -7.2F, -6.0F, 12, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(true);
      float var4 = 0.8F + 0.2F * Geo.sin(var1.time * 25.0F) + var1.move * 0.8F;
      var0.color(-1);
      var0.cross("flame", 0.0F, -7.2F - 2.4F * var4, 0.0F, 2.2F, 4.8F * var4, 0.0F, 1.0F, 1.0F, 0.0F);
      var0.color(-1996488705);
      var0.cross("orb", 0.0F, -7.4F, 0.0F, 3.6F, 3.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.glow(false).color(-1);
      var0.pop();
   }

   static void teddy(G var0, Cos.A var1) {
      var0.push();
      var0.translate(0.8F, -4.2F, -4.2F);
      var0.rotZ(Geo.sin(var1.time * 1.6) * 4.0F + var1.move * Geo.sin(var1.time * 8.0F) * 6.0F);
      var0.rotY(180.0F);
      Geo.ellipsoid(var0, "fur_brown", 0.0F, -2.2F, 0.0F, 2.4F, 2.8F, 1.9F, 12, 8);
      var0.color(-1521512);
      Geo.ellipsoid(var0, "fur_brown", 0.0F, -2.4F, 1.1F, 1.5F, 1.8F, 0.9F, 10, 6);
      var0.color(-1);
      Geo.ellipsoid(var0, "fur_brown", 0.0F, 1.8F, 0.0F, 2.3F, 2.1F, 2.0F, 14, 9);
      var0.push();
      var0.translate(0.0F, 1.6F, 2.02F);
      var0.plane("teddy_face", 0.0F, 0.0F, 3.4F, 3.4F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();

      for (byte var2 = -1; var2 <= 1; var2 += 2) {
         Geo.ellipsoid(var0, "fur_brown", var2 * 1.7F, 3.6F, 0.0F, 0.9F, 0.9F, 0.55F, 10, 6);
         Geo.ellipsoid(var0, "fur_brown", var2 * 2.4F, -1.4F, 0.4F, 0.8F, 1.5F, 0.8F, 8, 6);
         Geo.ellipsoid(var0, "fur_brown", var2 * 1.3F, -4.8F, 0.6F, 0.9F, 0.8F, 1.1F, 8, 6);
      }

      var0.color(-2080678);
      var0.push();
      var0.translate(0.0F, -0.2F, 1.6F);
      var0.box("satin", -1.0F, -0.35F, 0.0F, 1.0F, 0.35F, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-1);
      var0.pop();
   }

   static void balloon(G var0, Cos.A var1) {
      float var2 = var1.time;
      float var3 = 6.0F + Geo.sin(var2 * 0.9) * 1.0F;
      float var4 = 1.0F + Geo.sin(var2 * 1.3) * 0.8F;
      float var5 = -6.0F + Geo.cos(var2 * 0.7) * 1.0F;
      float[] var6 = new float[]{2.5F, -9.0F, -2.3F};
      byte var7 = 10;
      float[][] var8 = new float[var7][];
      float[] var9 = new float[var7];

      for (int var10 = 0; var10 < var7; var10++) {
         float var11 = (float)var10 / (var7 - 1);
         var8[var10] = new float[]{
            Geo.lerp(var6[0], var3, var11) + Geo.sin(var11 * Math.PI) * 0.8F * Geo.sin(var2 * 2.0F),
            Geo.lerp(var6[1], var4 - 3.4F, var11),
            Geo.lerp(var6[2], var5, var11)
         };
         var9[var10] = 0.06F;
      }

      var0.color(-723724);
      Geo.chain(var0, "white", var8, var9, 3);
      var0.push();
      var0.translate(var3, var4, var5);
      var0.rotZ(Geo.sin(var2 * 1.1) * 8.0F);
      var0.rotY(var2 * 25.0F);
      var0.color(-54694);
      Geo.ellipsoid(var0, "gloss", -1.3F, 0.6F, 0.0F, 1.9F, 1.9F, 1.6F, 16, 10);
      Geo.ellipsoid(var0, "gloss", 1.3F, 0.6F, 0.0F, 1.9F, 1.9F, 1.6F, 16, 10);
      var0.push();
      var0.rotZ(180.0F);
      var0.translate(0.0F, -0.2F, 0.0F);
      var0.scale(1.0F, 1.0F, 0.8F);
      var0.cylinder("gloss", 2.85F, 0.2F, 0.0F, 3.4F, 14, false, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.color(-2092998);
      var0.cylinder("plastic", 0.3F, 0.45F, -3.6F, -3.1F, 6, true, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.color(-1);
      var0.pop();
   }
}
