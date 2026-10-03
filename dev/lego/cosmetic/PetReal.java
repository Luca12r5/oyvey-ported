package dev.lego.cosmetic;

import java.util.Arrays;

final class PetReal {
   private PetReal() {
   }

   static void real4(G var0, Pets.P var1, PetReal.R4 var2) {
      float var3 = var1.gr;
      float var4 = (var1.preview ? var2.babyS * 1.4F : PetRig.lerp(var2.babyS, 1.0F, var3)) * var2.size;
      float var5 = PetRig.lerp(var2.babyHead, 1.0F, var3);
      float var6 = PetRig.lerp(var2.babyLeg, 1.0F, var3);
      float var7 = PetRig.lerp(var2.babyLen, 1.0F, var3);
      float var8 = PetRig.lerp(var2.babySnout, 1.0F, var3);
      float var9 = PetRig.lerp(var2.babyNeck, 1.0F, var3);
      float var10 = PetRig.lerp(1.18F, 1.0F, var3);
      float var11 = var2.L * var6;
      float var12 = var2.zs * var7;
      float var13 = var2.zh * var7;
      float var14 = var2.rx * var10;
      float var15 = var2.ry * var10;
      float var16 = var11 * 0.96F + var2.pawR * 0.85F;
      float var17 = var16 + var15 * 0.18F;
      float var18 = var17 - var15;
      var1.stepBob = 0.0F;
      float var19 = 0.0F;
      if (var2.grazer) {
         var19 = var1.sit;
         var1.sit = 0.0F;
         var1.bow = Math.max(var1.bow, var19 * 0.3F);
      }

      var0.push();
      var0.scale(var4);
      Pets.root(var0, var1);
      PetRig.Frame var20 = PetRig.frame(var1, var16, var12, var13, var11, var18);
      float var21 = PetRig.lerp(1.3F, 1.0F, var3);
      PetRig.LegStyle var22 = new PetRig.LegStyle(
         var2.legUp, var2.legLo, var2.paw, var2.r1f * var21, var2.r2f * var21, var2.pawR * PetRig.lerp(1.15F, 1.0F, var3)
      );
      PetRig.LegStyle var23 = new PetRig.LegStyle(
         var2.legUp, var2.legLo, var2.paw, var2.r1h * var21, var2.r2h * var21, var2.pawR * PetRig.lerp(1.15F, 1.0F, var3)
      );
      var22.hoof = var23.hoof = var2.hoof;
      var22.pawLen = var23.pawLen = var2.pawLen;
      var22.seg = var23.seg = 5;
      PetRig.legs(var0, var1, var20, var22, var23, var2.legX * PetRig.lerp(1.1F, 1.0F, var3), var12, var13, var16, var11, 0.5F, 0.46F);
      var0.push();
      var20.apply(var0);
      var0.rotZ(var1.roll);
      float var24 = var12 * 0.45F;
      float var25 = var13 * 0.5F;
      float var26 = (var12 - var13) * 0.5F;
      var0.push();
      var0.translate(0.0F, var17, (var24 + var25) / 2.0F);
      var0.scale(1.0F, 1.0F, var1.stretch);
      var0.translate(0.0F, 0.0F, -(var24 + var25) / 2.0F);
      PetGeo.blob(var0, var2.body, 0.0F, 0.0F, var24, var14, var15, var26 * 0.95F, 9, 5);
      PetGeo.blob(var0, var2.body, 0.0F, -var15 * 0.05F, var25, var14 * var2.hipF, var15 * var2.hipF, var26 * 0.85F, 9, 5);
      if (var2.bodyExtra != null) {
         var0.push();
         var0.translate(0.0F, 0.0F, 0.0F);
         var2.bodyExtra.draw(var0, var1);
         var0.pop();
      }

      if (var2.tail != null) {
         var0.push();
         var0.translate(0.0F, var15 * var2.hipF * 0.45F, var25 - var26 * 0.78F);
         var0.scale(PetRig.lerp(0.75F, 1.0F, var3));
         var2.tail.draw(var0, var1);
         var0.pop();
      }

      var0.pop();
      float var27 = var2.neckAng
         + 32 * (var1.is("sad") ? 1 : 0)
         + 18.0F * var1.crouch
         + 38.0F * var1.lie
         + 36.0F * var1.sniff
         + 28.0F * var1.bow
         - 10 * (var1.is("curious") ? 1 : 0)
         - 10.0F * var1.sit
         + 85.0F * var19;
      float var28 = var2.neckLen * var9 * (1.0F + 0.25F * var19);
      float var29 = var17 + var15 * 0.42F;
      float var30 = var24 + var26 * 0.55F;
      var0.push();
      var0.translate(0.0F, var29, var30);
      var0.rotX(var27);
      float var31 = var2.neckR * PetRig.lerp(1.1F, 1.0F, var3);
      PetGeo.blob(var0, var2.body, 0.0F, var28 * 0.5F, 0.0F, var31, var28 * 0.5F + var31 * 0.55F, var31 * 1.05F, 8, 5);
      if (var2.neckExtra != null) {
         var2.neckExtra.draw(var0, var1);
      }

      var0.push();
      var0.translate(0.0F, var28 * 0.12F + var31 * 0.2F, 0.0F);
      PetRig.collar(var0, var1, var31 * 1.02F, 0.2F + 0.05F * var3);
      var0.pop();
      var0.pop();
      float var32 = var29 + PetGeo.cos(Math.toRadians(var27)) * var28;
      float var33 = var30 + PetGeo.sin(Math.toRadians(var27)) * var28;
      var0.push();
      var0.translate(0.0F, var32 - var1.bob * var11 * 0.6F + var2.skRy * (var5 - 1.0F) * 0.35F, var33 + var2.skRz * (var5 - 1.0F) * 0.3F);
      Pets.headTurn(var0, var1);
      var0.rotX(var1.nod + var2.headDown * PetRig.lerp(0.4F, 1.0F, var3) + var19 * (45.0F + PetGeo.sin(var1.t * 5.0F) * 4.0F));
      var0.scale(var5);
      realHead(var0, var1, var2, var8, var3);
      var0.pop();
      Pets.fx(var0, var1, var32 + var2.skRy * var5 + 1.4F);
      var0.pop();
      PetRig.sparkles(var0, var1, Math.max(var26 * 1.4F, var14 + 1.2F));
      var0.pop();
   }

   static void realHead(G var0, Pets.P var1, PetReal.R4 var2, float var3, float var4) {
      PetGeo.blob(var0, var2.head, 0.0F, 0.0F, 0.0F, var2.skRx, var2.skRy, var2.skRz, 11, 8);
      float var5 = var2.snLen * var3;
      float var6 = var2.skRz * 0.42F + var5 * 0.5F;
      float var7 = var2.snR * PetRig.lerp(1.1F, 1.0F, var4);
      float var8 = var2.snR * 0.8F;
      float var9 = var5 * 0.55F + var2.snR * 0.3F;
      PetGeo.blob(var0, var2.muzzle, 0.0F, var2.snY, var6, var7, var8, var9, 8, 5);
      PetGeo.blob(var0, Pets.gl(var2.nose), 0.0F, var2.snY + var8 * 0.45F, var6 + var9 * 0.93F, var7 * 0.42F, var8 * 0.34F, var7 * 0.3F, 6, 3);
      String var10 = PetRig.style(var2.style, var4);
      boolean var11 = var0.glow;
      int var12 = var0.color;
      var0.color(-1).glow(false);
      float var13 = 0.6F;
      float var14 = var2.span * (var13 * 192.0F) / 256.0F * var2.skRx / var2.skRy;
      float var15 = var2.eyeEl + 0.458F * var14;
      PetGeo.decal(
         var0, "pet_face:" + var10 + ":" + var1.emo, 0.0F, 0.0F, 0.0F, var2.skRx, var2.skRy, var2.skRz, 0.0F, var15, var2.span, 0.0F, 0.0F, 1.0F, var13, 1.035F
      );
      PetGeo.decal(
         var0,
         Pets.eyes(var10, var1),
         0.0F,
         0.0F,
         0.0F,
         var2.skRx,
         var2.skRy,
         var2.skRz,
         var1.lookX * 0.08F,
         var15 + var1.lookY * 0.05F,
         var2.span,
         0.0F,
         0.0F,
         1.0F,
         var13,
         1.05F
      );
      PetGeo.decal(
         var0, "pet_face:" + var10 + ":" + var1.emo, 0.0F, var2.snY, var6, var7, var8, var9, 0.0F, -0.32F, 0.95F, 0.25F, 0.57F, 0.75F, 0.8F, 1.04F, 4, 3
      );
      var0.color(var12).glow(var11);
      if (var2.ears != null) {
         var2.ears.draw(var0, var1);
      }

      if (var2.headExtra != null) {
         var2.headExtra.draw(var0, var1);
      }
   }

   static void chain(G var0, Pets.P var1, String[] var2, float[] var3, float var4, float var5, float var6, float var7, Pets.Part var8) {
      var0.push();
      var0.rotY(var1.tail * var7 + var1.tailSway);
      var0.rotX(var6 + var1.tailLift);

      for (int var9 = 0; var9 < var3.length; var9++) {
         var0.rotX(var5);
         var0.rotY(var1.tail * 0.3F * var7 + var1.tailSway * 0.4F);
         PetGeo.blob(var0, var2[Math.min(var9, var2.length - 1)], 0.0F, 0.0F, -var4 * 0.5F, var3[var9], var3[var9], var4 * 0.62F + var3[var9] * 0.3F, 6, 3);
         var0.translate(0.0F, 0.0F, -var4 * 0.82F);
      }

      if (var8 != null) {
         var8.draw(var0, var1);
      }

      var0.pop();
   }

   static String[] rep(String var0, int var1) {
      String[] var2 = new String[var1];
      Arrays.fill(var2, var0);
      return var2;
   }

   static void wolf(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("wolf");
      var3.head = Pets.head("wolf");
      var3.muzzle = Pets.col(14473942);
      var3.legUp = Pets.col(9343902);
      var3.legLo = Pets.col(12172484);
      var3.paw = Pets.col(14080220);
      var3.style = "wolf";
      var3.L = 5.4F;
      var3.legX = 1.25F;
      var3.zs = 3.0F;
      var3.zh = -3.1F;
      var3.rx = 1.95F;
      var3.ry = 2.25F;
      var3.hipF = 0.86F;
      var3.neckLen = 2.3F;
      var3.neckAng = 44.0F;
      var3.neckR = 1.3F;
      var3.snLen = 2.1F;
      var3.snR = 0.78F;
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(8291214), Pets.col(15329250), Pets.col(5133150), var2x * 1.0F, 1.2F, -0.35F, var2x, 0.72F, 1.6F, 8.0F);
         }
      };
      var3.neckExtra = (var1x, var2x) -> {
         float var3x = PetRig.smooth(0.2F, 1.0F, var2x.gr);
         PetGeo.blob(var1x, var3.body, 0.0F, 0.4F, 0.35F, 1.6F * var3x + 0.1F, 1.5F * var3x + 0.1F, 1.25F * var3x + 0.1F, 7, 4);
      };
      String var4 = Pets.col(9343902);
      String var5 = Pets.col(5133150);
      String var6 = Pets.col(14473942);
      var3.tail = (var2x, var3x) -> chain(
         var2x, var3x, new String[]{var4, var4, var4, var5, var5}, new float[]{0.5F, 0.7F, 0.78F, 0.7F, 0.48F}, 1.05F, 4.0F, -38.0F, 0.6F, null
      );
      real4(var0, var2, var3);
   }

   static void retriever(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("retr");
      var3.head = Pets.head("retr");
      var3.muzzle = Pets.col(15582856);
      var3.legUp = Pets.col(14920798);
      var3.legLo = Pets.col(15580792);
      var3.paw = Pets.col(15779976);
      var3.style = "retr";
      var3.L = 4.5F;
      var3.legX = 1.25F;
      var3.zs = 2.8F;
      var3.zh = -2.9F;
      var3.rx = 2.0F;
      var3.ry = 2.15F;
      var3.neckLen = 2.0F;
      var3.neckAng = 40.0F;
      var3.neckR = 1.2F;
      var3.skRx = 1.72F;
      var3.skRy = 1.62F;
      var3.skRz = 1.75F;
      var3.snLen = 1.8F;
      var3.snR = 0.85F;
      var3.snY = -0.5F;
      var3.nose = 2760222;
      var3.babyS = 0.6F;
      var3.ears = (var0x, var1x) -> {
         float var2x = Math.max(0.0F, 1.0F - var1x.ear) * 15.0F;
         float var3x = Math.max(0.0F, var1x.ear - 1.0F) * 90.0F;

         for (byte var4x = -1; var4x <= 1; var4x += 2) {
            var0x.push();
            var0x.translate(var4x * 1.45F, 0.75F, -0.3F);
            var0x.rotZ(var4x * (14.0F + var2x - var3x + var1x.earTwitch * (var4x > 0 ? 1 : 0)) + PetGeo.sin(var1x.t * 9.0F) * 4.0F * var1x.walkW);
            var0x.rotX(-6.0F);
            PetGeo.blob(var0x, Pets.col(14064206), var4x * 0.12F, -0.95F, 0.0F, 0.42F, 1.15F, 0.82F, 7, 4);
            var0x.pop();
         }
      };
      var3.neckExtra = (var0x, var1x) -> {
         float var2x = PetRig.smooth(0.3F, 1.0F, var1x.gr);
         PetGeo.blob(var0x, Pets.col(15912852), 0.0F, 0.2F, 0.55F, 1.15F * var2x + 0.1F, 1.4F * var2x + 0.1F, 0.9F * var2x + 0.1F, 7, 4);
      };
      String var4 = Pets.col(14920798);
      String var5 = Pets.col(15779976);
      var3.tail = (var2x, var3x) -> chain(
         var2x, var3x, new String[]{var4, var4, var5, var5, var5}, new float[]{0.42F, 0.58F, 0.66F, 0.58F, 0.4F}, 1.0F, 9.0F, -8.0F, 1.0F, null
      );
      real4(var0, var2, var3);
   }

   static void arcticFox(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("afox");
      var3.head = Pets.head("afox");
      var3.muzzle = Pets.col(16514303);
      var3.legUp = Pets.col(15790840);
      var3.legLo = Pets.col(15133426);
      var3.paw = Pets.col(16777215);
      var3.style = "afox";
      var3.size = 0.86F;
      var3.L = 3.8F;
      var3.legX = 1.1F;
      var3.zs = 2.5F;
      var3.zh = -2.5F;
      var3.rx = 1.75F;
      var3.ry = 1.95F;
      var3.neckLen = 1.6F;
      var3.neckAng = 46.0F;
      var3.neckR = 1.15F;
      var3.skRx = 1.6F;
      var3.skRy = 1.45F;
      var3.skRz = 1.6F;
      var3.snLen = 1.55F;
      var3.snR = 0.62F;
      var3.snY = -0.42F;
      var3.nose = 1972770;
      var3.r1f = 0.62F;
      var3.r2f = 0.42F;
      var3.r1h = 0.85F;
      var3.r2h = 0.44F;
      var3.pawR = 0.55F;
      var3.babyS = 0.66F;
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(15790840), Pets.col(14212842), var2x * 0.95F, 1.1F, -0.3F, var2x, 0.7F, 1.05F, 14.0F);
         }
      };
      var3.neckExtra = (var1x, var2x) -> PetGeo.blob(var1x, var3.body, 0.0F, 0.35F, 0.3F, 1.55F, 1.35F, 1.3F, 8, 5);
      String var4 = Pets.col(16185596);
      String var5 = Pets.col(15133942);
      var3.tail = (var2x, var3x) -> chain(
         var2x, var3x, new String[]{var4, var4, var5, var4, var4}, new float[]{0.75F, 1.12F, 1.28F, 1.16F, 0.8F}, 1.15F, 9.0F, -18.0F, 0.8F, null
      );
      real4(var0, var2, var3);
   }

   static void horse(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("horse");
      var3.head = Pets.head("horse");
      var3.muzzle = Pets.col(9065014);
      var3.legUp = Pets.col(10707002);
      var3.legLo = Pets.col(4860958);
      var3.paw = Pets.gl(2760736);
      var3.style = "horse";
      var3.nose = 3811876;
      var3.grazer = true;
      var3.L = 7.2F;
      var3.legX = 1.2F;
      var3.zs = 3.6F;
      var3.zh = -3.6F;
      var3.rx = 2.15F;
      var3.ry = 2.55F;
      var3.hipF = 0.96F;
      var3.neckLen = 3.8F;
      var3.neckAng = 30.0F;
      var3.neckR = 1.12F;
      var3.skRx = 1.3F;
      var3.skRy = 1.4F;
      var3.skRz = 1.45F;
      var3.snLen = 2.8F;
      var3.snR = 0.92F;
      var3.snY = -0.3F;
      var3.headDown = 38.0F;
      var3.r1f = 0.72F;
      var3.r2f = 0.36F;
      var3.r1h = 0.98F;
      var3.r2h = 0.38F;
      var3.pawR = 0.46F;
      var3.hoof = true;
      var3.span = 1.1F;
      var3.eyeEl = 0.3F;
      var3.babyS = 0.55F;
      var3.babyLeg = 0.78F;
      var3.babyHead = 1.6F;
      var3.babyNeck = 0.6F;
      var3.babySnout = 0.55F;
      String var4 = Pets.col(2364688);
      var3.ears = (var1x, var2x) -> {
         for (byte var3x = -1; var3x <= 1; var3x += 2) {
            Pets.earPointy(var1x, var2x, Pets.col(10707002), Pets.col(5911584), var3x * 0.7F, 1.15F, -0.4F, var3x, 0.45F, 1.15F, 6.0F);
         }

         var1x.push();
         var1x.translate(0.0F, 1.3F, 0.35F);
         var1x.rotX(40.0F + PetGeo.sin(var2x.t * 2.0F) * 4.0F);
         PetGeo.blob(var1x, var4, 0.0F, 0.3F, 0.0F, 0.5F, 0.75F, 0.3F, 6, 3);
         var1x.pop();
      };
      var3.neckExtra = (var1x, var2x) -> {
         float var3x = PetRig.lerp(0.5F, 1.0F, var2x.gr);
         float var4x = 3.8F * PetRig.lerp(0.6F, 1.0F, var2x.gr);

         for (int var5 = 0; var5 < 5; var5++) {
            float var6 = var5 / 4.0F;
            var1x.push();
            var1x.translate(0.0F, var4x * (0.05F + var6 * 0.95F), -1.0F);
            var1x.rotZ(PetGeo.sin(var2x.t * 3.0F + var5) * 5.0F * (0.3F + var2x.walkW));
            PetGeo.blob(var1x, var4, 0.0F, 0.0F, 0.0F, 0.32F * var3x, 0.62F * var3x, 0.55F * var3x, 5, 3);
            var1x.pop();
         }
      };
      var3.tail = (var1x, var2x) -> chain(
         var1x, var2x, rep(var4, 5), new float[]{0.4F, 0.58F, 0.64F, 0.56F, 0.4F}, 1.1F, -6.0F - 6.0F * var2x.walkW, -58.0F + 25.0F * var2x.runW, 0.5F, null
      );
      real4(var0, var2, var3);
   }

   static void deer(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      boolean var4 = var1.growth < 0.5F;
      var3.body = Pets.fur(var4 ? "fawn" : "deer");
      var3.head = Pets.head("deer");
      var3.muzzle = Pets.col(12092504);
      var3.legUp = Pets.col(11104586);
      var3.legLo = Pets.col(12618334);
      var3.paw = Pets.gl(2760736);
      var3.style = "deer";
      var3.nose = 1709076;
      var3.grazer = true;
      var3.L = 6.6F;
      var3.legX = 1.02F;
      var3.zs = 3.0F;
      var3.zh = -3.1F;
      var3.rx = 1.7F;
      var3.ry = 2.1F;
      var3.hipF = 0.95F;
      var3.neckLen = 3.2F;
      var3.neckAng = 26.0F;
      var3.neckR = 0.92F;
      var3.skRx = 1.3F;
      var3.skRy = 1.3F;
      var3.skRz = 1.45F;
      var3.snLen = 1.9F;
      var3.snR = 0.68F;
      var3.snY = -0.32F;
      var3.headDown = 16.0F;
      var3.r1f = 0.55F;
      var3.r2f = 0.28F;
      var3.r1h = 0.8F;
      var3.r2h = 0.3F;
      var3.pawR = 0.38F;
      var3.hoof = true;
      var3.span = 1.05F;
      var3.eyeEl = 0.26F;
      var3.babyS = 0.55F;
      var3.babyLeg = 0.75F;
      var3.babyHead = 1.65F;
      int var5 = Math.max(1, Math.min(5, var1.level));
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(11104586), Pets.col(16181466), var2x * 0.95F, 0.85F, -0.4F, var2x, 0.72F, 1.75F, 55.0F);
         }
      };
      var3.headExtra = (var1x, var2x) -> antlers(var1x, var2x, var5, var2x.gr);
      var3.tail = (var0x, var1x) -> {
         var0x.rotX(35.0F + var1x.tailLift * 0.5F + (!var1x.is("excited") && !var1x.is("angry") ? 0 : 40));
         PetGeo.blob(var0x, Pets.col(16777215), 0.0F, 0.0F, -0.45F, 0.42F, 0.35F, 0.62F, 6, 3);
      };
      real4(var0, var2, var3);
   }

   static void antlers(G var0, Pets.P var1, int var2, float var3) {
      if (var2 >= 2) {
         String var4 = Pets.col(15259576);
         float var5 = var2 == 2 ? 0.55F : (var2 == 3 ? 1.6F : (var2 == 4 ? 2.5F : 3.3F));

         for (byte var6 = -1; var6 <= 1; var6 += 2) {
            var0.push();
            var0.translate(var6 * 0.6F, 1.05F, -0.1F);
            var0.rotZ(-var6 * 22);
            var0.rotX(-18.0F);
            var0.cylinder(var4, 0.17F, 0.11F, 0.0F, var5, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
            if (var2 >= 4) {
               for (int var7 = 0; var7 < (var2 == 5 ? 3 : 2); var7++) {
                  var0.push();
                  var0.translate(0.0F, var5 * (0.35F + var7 * 0.25F), 0.0F);
                  var0.rotX(48.0F);
                  var0.rotZ(-var6 * 10);
                  var0.cylinder(var4, 0.1F, 0.05F, 0.0F, var5 * (0.42F - var7 * 0.08F), 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
                  var0.pop();
               }

               var0.push();
               var0.translate(0.0F, var5, 0.0F);
               var0.rotZ(-var6 * 25);
               var0.cylinder(var4, 0.1F, 0.04F, 0.0F, var5 * 0.35F, 4, false, 0.0F, 0.0F, 1.0F, 1.0F);
               var0.pop();
            } else {
               var0.translate(0.0F, var5, 0.0F);
               PetGeo.blob(var0, var4, 0.0F, 0.0F, 0.0F, 0.14F, 0.14F, 0.14F, 4, 2);
            }

            var0.pop();
         }
      }
   }

   static void lion(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("lion");
      var3.head = Pets.head("lion");
      var3.muzzle = Pets.col(16180160);
      var3.legUp = Pets.col(14723429);
      var3.legLo = Pets.col(15251577);
      var3.paw = Pets.col(15781014);
      var3.style = "lion";
      var3.nose = 11560026;
      var3.L = 4.9F;
      var3.legX = 1.45F;
      var3.zs = 3.2F;
      var3.zh = -3.3F;
      var3.rx = 2.3F;
      var3.ry = 2.4F;
      var3.hipF = 0.88F;
      var3.neckLen = 1.9F;
      var3.neckAng = 50.0F;
      var3.neckR = 1.5F;
      var3.skRx = 1.9F;
      var3.skRy = 1.8F;
      var3.skRz = 1.9F;
      var3.snLen = 1.4F;
      var3.snR = 1.05F;
      var3.snY = -0.6F;
      var3.r1f = 0.95F;
      var3.r2f = 0.62F;
      var3.r1h = 1.2F;
      var3.r2h = 0.6F;
      var3.pawR = 0.75F;
      var3.pawLen = 1.15F;
      var3.span = 0.95F;
      String var4 = Pets.col(10114598);
      String var5 = Pets.col(12085806);
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earRound(var0x, var1x, Pets.col(14195285), Pets.col(16180160), var2x * 1.35F, 1.3F, -0.35F, var2x, 0.58F);
         }
      };
      var3.headExtra = (var2x, var3x) -> {
         float var4x = PetRig.smooth(0.15F, 1.0F, var3x.gr);
         if (!(var4x <= 0.01F)) {
            for (int var5x = 0; var5x < 10; var5x++) {
               double var6x = (Math.PI * 2) * var5x / 10.0 + 0.3;
               float var8 = PetGeo.sin(var3x.t * 2.2F + var5x) * 0.06F;
               var2x.push();
               var2x.translate(PetGeo.cos(var6x) * 1.75F, PetGeo.sin(var6x) * 1.7F + 0.1F, -0.55F);
               var2x.rotZ((float)Math.toDegrees(var6x) - 90.0F);
               var2x.rotX(-25.0F);
               var2x.scale(var4x * (1.0F + var8));
               PetGeo.cone(var2x, var5x % 2 == 0 ? var4 : var5, 0.95F, 2.0F, 4, false);
               var2x.pop();
            }

            PetGeo.blob(var2x, var4, 0.0F, 0.1F, -0.9F, 2.1F * var4x + 0.4F, 2.1F * var4x + 0.4F, 1.3F * var4x + 0.3F, 7, 4);
         }
      };
      var3.neckExtra = (var1x, var2x) -> {
         float var3x = PetRig.smooth(0.15F, 1.0F, var2x.gr);
         if (var3x > 0.01F) {
            PetGeo.blob(var1x, var4, 0.0F, 1.2F, -0.2F, 2.0F * var3x, 1.9F * var3x + 0.3F, 1.9F * var3x, 7, 4);
         }
      };
      String var6 = Pets.col(14723429);
      var3.tail = (var2x, var3x) -> chain(
         var2x,
         var3x,
         rep(var6, 5),
         new float[]{0.3F, 0.27F, 0.25F, 0.24F, 0.22F},
         1.4F,
         -7.0F,
         -15.0F,
         0.8F,
         (var1xx, var2xx) -> PetGeo.blob(var1xx, var4, 0.0F, 0.0F, 0.1F, 0.48F, 0.48F, 0.62F, 5, 3)
      );
      real4(var0, var2, var3);
   }

   static void tiger(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      var3.body = Pets.fur("tiger");
      var3.head = Pets.head("tiger");
      var3.muzzle = Pets.col(16774890);
      var3.legUp = Pets.fur("tiger");
      var3.legLo = Pets.col(15763756);
      var3.paw = Pets.col(16773600);
      var3.style = "tiger";
      var3.nose = 13132906;
      var3.L = 5.0F;
      var3.legX = 1.4F;
      var3.zs = 3.3F;
      var3.zh = -3.4F;
      var3.rx = 2.15F;
      var3.ry = 2.3F;
      var3.hipF = 0.9F;
      var3.neckLen = 1.9F;
      var3.neckAng = 52.0F;
      var3.neckR = 1.4F;
      var3.skRx = 1.85F;
      var3.skRy = 1.72F;
      var3.skRz = 1.85F;
      var3.snLen = 1.35F;
      var3.snR = 1.0F;
      var3.snY = -0.58F;
      var3.r1f = 0.92F;
      var3.r2f = 0.6F;
      var3.r1h = 1.15F;
      var3.r2h = 0.58F;
      var3.pawR = 0.72F;
      var3.pawLen = 1.15F;
      var3.span = 0.95F;
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earRound(var0x, var1x, Pets.col(14712862), Pets.col(16774890), var2x * 1.3F, 1.25F, -0.3F, var2x, 0.55F);
         }

         float var4x = PetRig.lerp(0.4F, 1.0F, var1x.gr);

         for (byte var3x = -1; var3x <= 1; var3x += 2) {
            var0x.push();
            var0x.translate(var3x * 1.5F, -0.55F, -0.2F);
            var0x.rotY(-var3x * 35);
            var0x.rotZ(-var3x * 118);
            var0x.scale(1.0F, 1.0F, 0.5F);
            PetGeo.cone(var0x, Pets.col(16774890), 0.5F * var4x, 0.75F * var4x, 4, false);
            var0x.pop();
         }
      };
      String var4 = Pets.col(15763756);
      String var5 = Pets.col(2365980);
      var3.tail = (var2x, var3x) -> chain(
         var2x, var3x, new String[]{var4, var5, var4, var5, var4}, new float[]{0.36F, 0.34F, 0.33F, 0.32F, 0.3F}, 1.35F, -5.0F, -18.0F, 0.8F, null
      );
      real4(var0, var2, var3);
   }

   static void drake(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      PetReal.R4 var3 = new PetReal.R4();
      int var4 = Math.max(1, Math.min(5, var1.level));
      var3.body = Pets.fur("drake");
      var3.head = Pets.head("drake");
      var3.muzzle = Pets.fur("drake");
      var3.legUp = Pets.col(8150230);
      var3.legLo = Pets.col(6966464);
      var3.paw = Pets.col(5914792);
      var3.style = "drake";
      var3.nose = 5126814;
      var3.L = 4.6F;
      var3.legX = 1.6F;
      var3.zs = 3.2F;
      var3.zh = -3.3F;
      var3.rx = 2.2F;
      var3.ry = 2.3F;
      var3.hipF = 0.95F;
      var3.neckLen = 3.0F;
      var3.neckAng = 36.0F;
      var3.neckR = 1.05F;
      var3.skRx = 1.55F;
      var3.skRy = 1.4F;
      var3.skRz = 1.8F;
      var3.snLen = 2.0F;
      var3.snR = 0.85F;
      var3.snY = -0.3F;
      var3.r1f = 0.8F;
      var3.r2f = 0.55F;
      var3.r1h = 1.15F;
      var3.r2h = 0.62F;
      var3.pawR = 0.68F;
      var3.pawLen = 1.5F;
      var3.babyS = 0.58F;
      String var5 = Pets.col(15917752);
      String var6 = Pets.col(15915424);
      boolean var7 = var4 >= 3 && (var2.is("angry") || var2.is("excited"));
      var3.ears = (var2x, var3x) -> {
         float var4x = 0.8F + var4 * 0.45F;

         for (byte var5x = -1; var5x <= 1; var5x += 2) {
            var2x.push();
            var2x.translate(var5x * 0.75F, 1.0F, -0.6F);
            var2x.rotZ(-var5x * 18);
            var2x.rotX(-55.0F);
            PetGeo.cone(var2x, var5, 0.38F, var4x, 7, false);
            var2x.pop();
            if (var4 >= 5) {
               var2x.push();
               var2x.translate(var5x * 1.25F, 0.4F, -0.7F);
               var2x.rotZ(-var5x * 60);
               var2x.rotX(-50.0F);
               PetGeo.cone(var2x, var5, 0.26F, var4x * 0.55F, 6, false);
               var2x.pop();
            }
         }
      };
      var3.headExtra = (var1x, var2x) -> {
         if (var7) {
            for (int var3x = 0; var3x < 4; var3x++) {
               float var4x = PetRig.frac(var2x.t * 2.2F + var3x * 0.25F);
               int var5x = (int)(230.0F * (1.0F - var4x));
               PetGeo.sprite(
                  var1x,
                  "pet_star",
                  PetGeo.sin(var2x.t * 9.0F + var3x) * 0.3F,
                  -0.4F + var4x * 0.6F,
                  3.6F + var4x * 3.2F,
                  0.6F + var4x * 1.2F,
                  var5x << 24 | (var3x % 2 == 0 ? 16751146 : 16765514)
               );
            }
         }
      };
      var3.bodyExtra = (var2x, var3x) -> {
         for (int var4x = 0; var4x < 5; var4x++) {
            var2x.push();
            var2x.translate(0.0F, 2.2F - Math.abs(var4x - 1.5F) * 0.12F, 2.2F - var4x * 1.3F);
            var2x.rotX(-25.0F);
            var2x.scale(0.45F, 1.0F, 1.0F);
            PetGeo.cone(var2x, var6, 0.5F, 0.75F + (var4 >= 4 ? 0.35F : 0.0F), 5, false);
            var2x.pop();
         }

         float var10 = PetRig.lerp(2.4F, 6.2F, var3x.gr) + (var4 >= 5 ? 1.8F : 0.0F);
         float var5x = Math.max(Math.max(var3x.runW, var3x.is("excited") ? 1.0F : 0.0F), var4 >= 5 ? 0.35F + 0.65F * var3x.walkW : 0.25F * var3x.walkW);
         float[] var6x = PetRig.flap(var3x, 1.6F);
         float var7x = var6x[0] * 38.0F * var5x;
         String var8x = "pet_dwing:4E3A9E:" + (var4 >= 5 ? "E07AE8" : "B89AF5");

         for (byte var9x = -1; var9x <= 1; var9x += 2) {
            var2x.push();
            var2x.translate(var9x * 1.3F, 1.9F, 1.6F);
            var2x.scale(var9x, 1.0F, 1.0F);
            var2x.rotY(PetRig.lerp(-70.0F, 5.0F, var5x));
            var2x.rotZ(PetRig.lerp(-8.0F, 22.0F, var5x) + var7x);
            var2x.rotX(PetRig.lerp(70.0F, 88.0F, var5x));
            var2x.planeTL(var8x, var10, var10 * 0.72F, 0.0F, 0.0F, 1.0F, 1.0F);
            var2x.pop();
         }
      };
      String var8 = Pets.col(8150230);
      String var9 = Pets.col(6966464);
      var3.tail = (var3x, var4x) -> chain(
         var3x,
         var4x,
         new String[]{var8, var8, var9, var9, var9, var9},
         new float[]{0.85F, 0.7F, 0.56F, 0.45F, 0.36F, 0.28F},
         1.3F,
         3.0F,
         -14.0F,
         0.9F,
         (var1xx, var2xx) -> {
            var1xx.push();
            var1xx.rotX(-90.0F);
            var1xx.scale(1.0F, 1.0F, 0.3F);
            PetGeo.cone(var1xx, var6, 0.7F, 1.2F, 4, false);
            var1xx.pop();
         }
      );
      real4(var0, var2, var3);
   }

   static void eagle(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.gr;
      float var4 = var2.preview ? 1.0F : PetRig.lerp(0.72F, 1.4F, var3);
      float var5 = PetRig.smooth(0.15F, 0.5F, var2.sp);
      float[] var6 = PetRig.flap(var2, 2.1F);
      float var7 = PetRig.window(PetRig.frac(var2.t / 3.1F), 0.45F, 0.95F, 0.1F) * var2.runW;
      float var8 = (var2.is("sleepy") ? 12 : 42) * (1.0F - var7 * 0.85F);
      float var9 = PetRig.window(PetRig.frac(var2.t / 14.0F), 0.62F, 0.76F, 0.04F) * var2.idle * (var2.is("sleepy") ? 0 : 1);
      float var10 = var2.is("excited") ? 1.0F : 0.0F;
      float var11 = Math.max(var5, Math.max(var9, var10 * 0.8F));
      var2.stepBob = 0.0F;
      var0.push();
      var0.scale(var4);
      float var12 = var5 * (6.5F + var6[1] * 0.9F * (1.0F - var7));
      var0.translate(0.0F, var12, 0.0F);
      Pets.root(var0, var2);
      float var13 = PetRig.lerp(1.2F, 1.9F, var3);
      String var14 = Pets.gl(15906874);

      for (byte var15 = -1; var15 <= 1; var15 += 2) {
         float var16 = PetRig.frac(var2.t * 2.1F + (var15 > 0 ? 0.0F : 0.5F));
         float var17 = (1.0F - var5) * var2.walkW * Math.max(0.0F, PetGeo.sin((Math.PI * 2) * var16)) * 0.5F;
         var0.push();
         var0.translate(var15 * 0.75F, var13 + 0.25F, -0.2F);
         var0.rotX(var5 * 70.0F - (1.0F - var5) * var2.walkW * PetGeo.sin((Math.PI * 2) * var16) * 22.0F);
         var0.translate(0.0F, var17, 0.0F);
         var0.cylinder(var14, 0.2F, 0.17F, -var13, 0.0F, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.translate(0.0F, -var13, 0.0F);

         for (int var18 = -1; var18 <= 1; var18++) {
            var0.push();
            var0.rotY(var18 * 28);
            PetGeo.blob(var0, var14, 0.0F, -0.1F, 0.4F, 0.14F, 0.12F, 0.45F, 5, 2);
            var0.pop();
         }

         var0.pop();
      }

      float var29 = PetRig.lerp(40.0F, 4.0F, var5) + var2.waddle * 0.0F;
      var0.push();
      var0.translate(0.0F, var13 + 1.6F, 0.0F);
      var0.rotZ(var2.waddle * (1.0F - var5) * 0.8F + var2.roll);
      var0.rotX(-var29);
      float var30 = PetRig.lerp(1.35F, 1.45F, var3);
      PetGeo.blob(var0, Pets.fur("eagle"), 0.0F, 0.0F, 0.0F, var30, var30 * 1.05F, PetRig.lerp(2.0F, 2.7F, var3), 11, 6);
      var0.push();
      var0.translate(0.0F, 0.1F, -PetRig.lerp(1.8F, 2.5F, var3));
      var0.rotX(80.0F - var5 * 70.0F + var2.tailLift * 0.3F);
      var0.rotY(180.0F + var2.tailSway * 0.5F);
      var0.plane(
         "pet_tailfan:F4F0E8:D8D0C0",
         0.0F,
         -PetRig.lerp(1.0F, 1.5F, var3),
         PetRig.lerp(1.9F, 2.6F, var3),
         PetRig.lerp(2.0F, 3.0F, var3),
         0.0F,
         0.0F,
         1.0F,
         1.0F
      );
      var0.pop();
      float var31 = PetRig.lerp(3.2F, 7.4F, var3);
      float var32 = PetRig.lerp(1.9F, 3.1F, var3);
      float var19 = 1.0F - var11;

      for (byte var20 = -1; var20 <= 1; var20 += 2) {
         var0.push();
         var0.translate(var20 * var30 * 0.8F, var30 * 0.55F, 0.9F);
         var0.scale(var20, 1.0F, 1.0F);
         float var21 = var5 > 0.01F ? var6[0] * var8 * var5 : 0.0F;
         float var22 = var9 * (25.0F + PetGeo.sin(var2.t * 3.0F) * 8.0F) + var10 * (30.0F + PetGeo.sin(var2.t * 12.0F) * 25.0F);
         var0.rotY(80.0F * var19);
         var0.rotZ(PetRig.lerp(-12.0F, 6.0F, var11) + var21 + var22 - (var2.is("sad") ? 10 : 0));
         var0.rotX(90.0F - var19 * 10.0F);
         var0.planeTL("pet_feather:5B3B25:2A1A10", var31 * PetRig.lerp(0.62F, 1.0F, var11), var32, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.pop();
      var0.push();
      float var33 = var13 + 1.6F + PetRig.lerp(1.9F, 0.5F, var5);
      float var34 = PetRig.lerp(0.9F, 2.9F, var5) * PetRig.lerp(1.0F, 1.2F, var3);
      var0.translate(0.0F, var33 - var6[1] * 0.2F * var5, var34);
      Pets.headTurn(var0, var2);
      float var35 = PetRig.lerp(1.45F, 1.0F, var3);
      var0.scale(var35);
      float var23 = 1.15F;
      float var24 = 1.1F;
      float var25 = 1.2F;
      PetGeo.blob(var0, Pets.fur("eagleh"), 0.0F, 0.0F, 0.0F, var23, var24, var25, 12, 8);
      String var26 = PetRig.style("eagle", var3);
      boolean var27 = var0.glow;
      var0.color(-1).glow(false);
      PetGeo.decal(var0, "pet_face:" + var26 + ":" + var2.emo, 0.0F, 0.0F, 0.0F, var23, var24, var25, 0.0F, 0.25F, 1.15F, 0.0F, 0.0F, 1.0F, 0.6F, 1.035F);
      PetGeo.decal(
         var0,
         Pets.eyes(var26, var2),
         0.0F,
         0.0F,
         0.0F,
         var23,
         var24,
         var25,
         var2.lookX * 0.08F,
         0.25F + var2.lookY * 0.05F,
         1.15F,
         0.0F,
         0.0F,
         1.0F,
         0.6F,
         1.05F
      );
      var0.glow(var27);
      var0.push();
      var0.translate(0.0F, -0.1F, var25 * 0.8F);
      var0.rotX(80.0F);
      var0.scale(1.0F, 1.0F, 0.8F);
      PetGeo.cone(var0, Pets.gl(15906874), 0.42F, 0.95F, 7, true);
      var0.pop();
      var0.push();
      var0.translate(0.0F, -0.25F, var25 * 0.8F + 0.85F);
      var0.rotX(160.0F);
      PetGeo.cone(var0, Pets.gl(3811872), 0.14F, 0.35F, 5, false);
      var0.pop();
      float var28 = var2.is("excited") ? 0.5F + 0.5F * Math.abs(PetGeo.sin(var2.t * 7.0F)) : (var2.is("angry") ? 0.6F : 0.0F);
      var0.push();
      var0.translate(0.0F, -0.35F, var25 * 0.75F);
      var0.rotX(95.0F + var28 * 25.0F);
      var0.scale(1.0F, 1.0F, 0.5F);
      PetGeo.cone(var0, Pets.gl(15247402), 0.32F, 0.6F, 6, false);
      var0.pop();
      var0.pop();
      Pets.fx(var0, var2, var33 + var24 * var35 + 1.3F);
      var0.pop();
      if (var5 < 0.5F) {
         PetRig.sparkles(var0, var2, 3.2F);
      }
   }

   static final class R4 {
      String body;
      String head;
      String muzzle;
      String legUp;
      String legLo;
      String paw;
      String style = "wolf";
      int nose = 1972766;
      float size = 1.0F;
      float L = 5.0F;
      float legX = 1.3F;
      float zs = 3.0F;
      float zh = -3.0F;
      float rx = 2.0F;
      float ry = 2.2F;
      float hipF = 0.9F;
      float neckLen = 2.3F;
      float neckAng = 42.0F;
      float neckR = 1.2F;
      float skRx = 1.65F;
      float skRy = 1.55F;
      float skRz = 1.75F;
      float snLen = 2.0F;
      float snR = 0.8F;
      float snY = -0.45F;
      float headDown = 0.0F;
      float r1f = 0.7F;
      float r2f = 0.45F;
      float r1h = 1.0F;
      float r2h = 0.48F;
      float pawR = 0.55F;
      float pawLen = 1.3F;
      boolean hoof;
      boolean grazer;
      float span = 0.9F;
      float eyeEl = 0.22F;
      Pets.Part ears;
      Pets.Part tail;
      Pets.Part headExtra;
      Pets.Part bodyExtra;
      Pets.Part neckExtra;
      float babyS = 0.56F;
      float babyHead = 1.75F;
      float babyLeg = 0.5F;
      float babyLen = 0.72F;
      float babySnout = 0.45F;
      float babyNeck = 0.45F;
   }
}
