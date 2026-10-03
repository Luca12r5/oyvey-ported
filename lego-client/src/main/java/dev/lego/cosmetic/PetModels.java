package dev.lego.cosmetic;

final class PetModels {
   private PetModels() {
   }

   static void cat(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("cat");
      var2.head = Pets.head("cat");
      var2.leg = Pets.col(16773602);
      var2.style = "cat";
      var2.nose = 16748456;
      var2.noseR = 0.26F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(16229457), Pets.col(16757702), var2x * 1.85F, 2.05F, -0.25F, var2x, 1.08F, 1.95F, 10.0F);
         }
      };
      var2.tail = (var0x, var1x) -> Pets.tail(
         var0x,
         var1x,
         new String[]{Pets.col(16229457), Pets.col(16229457), Pets.col(15239740), Pets.col(14251051)},
         new float[]{0.47F, 0.44F, 0.41F, 0.39F},
         1.15F,
         19.0F,
         30.0F,
         1.0F
      );
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void shiba(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("shiba");
      var2.head = Pets.head("shiba");
      var2.leg = Pets.col(16775148);
      var2.style = "dog";
      var2.nose = 2760740;
      var2.noseR = 0.34F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(15636296), Pets.col(16773604), var2x * 1.8F, 2.0F, -0.2F, var2x, 1.12F, 1.7F, 9.0F);
         }
      };
      var2.tail = (var0x, var1x) -> Pets.tail(
         var0x,
         var1x,
         new String[]{Pets.col(15636296), Pets.col(15636296), Pets.col(16035952), Pets.col(16775148)},
         new float[]{0.72F, 0.68F, 0.6F, 0.48F},
         1.0F,
         44.0F,
         62.0F,
         0.8F
      );
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void fox(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("fox");
      var2.head = Pets.head("fox");
      var2.leg = Pets.col(4860450);
      var2.style = "amber";
      var2.nose = 2760740;
      var2.noseR = 0.3F;
      var2.headRx = 3.45F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(16221754), Pets.col(16774120), Pets.col(4860450), var2x * 1.9F, 2.0F, -0.3F, var2x, 1.25F, 2.35F, 14.0F);
         }
      };
      var2.tail = (var0x, var1x) -> Pets.tail(
         var0x,
         var1x,
         new String[]{Pets.col(16221754), Pets.col(16221754), Pets.col(16221754), Pets.col(16774892)},
         new float[]{0.85F, 1.12F, 1.12F, 0.8F},
         1.25F,
         12.0F,
         18.0F,
         1.0F
      );
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void redPanda(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("redpanda");
      var2.head = Pets.head("redpanda");
      var2.leg = Pets.col(4858904);
      var2.style = "std";
      var2.nose = 2759192;
      var2.noseR = 0.3F;
      var2.headRx = 3.5F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(13852718), Pets.col(16774890), var2x * 2.0F, 1.9F, -0.3F, var2x, 1.2F, 1.4F, 22.0F);
         }
      };
      String var3 = Pets.col(13852718);
      String var4 = Pets.col(8008728);
      var2.tail = (var2x, var3x) -> Pets.tail(
         var2x, var3x, new String[]{var3, var4, var3, var4, var3}, new float[]{0.75F, 0.84F, 0.86F, 0.78F, 0.62F}, 1.0F, 9.0F, 16.0F, 1.0F
      );
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void panda(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("panda");
      var2.head = Pets.head("panda");
      var2.leg = Pets.fur("pdark");
      var2.style = "rim";
      var2.nose = 2763315;
      var2.noseR = 0.32F;
      var2.bodyRx = 2.4F;
      var2.headRx = 3.45F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earRound(var0x, var1x, Pets.fur("pdark"), null, var2x * 2.25F, 1.95F, -0.3F, var2x, 1.0F);
         }
      };
      var2.tail = (var0x, var1x) -> PetGeo.blob(var0x, Pets.col(16514039), 0.0F, 0.4F, 0.0F, 0.6F, 0.6F, 0.5F, 7, 4);
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void bear(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("bear");
      var2.head = Pets.head("bear");
      var2.leg = Pets.col(10381888);
      var2.style = "std";
      var2.nose = 4860446;
      var2.noseR = 0.36F;
      var2.bodyRx = 2.4F;
      var2.headRx = 3.4F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earRound(var0x, var1x, Pets.col(12157006), Pets.col(15782568), var2x * 2.25F, 1.9F, -0.25F, var2x, 0.95F);
         }
      };
      var2.tail = (var0x, var1x) -> PetGeo.blob(var0x, Pets.col(12157006), 0.0F, 0.3F, 0.0F, 0.55F, 0.55F, 0.5F, 7, 4);
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void hamster(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("hamster");
      var2.head = Pets.head("hamster");
      var2.leg = Pets.col(16770262);
      var2.style = "std";
      var2.nose = 16748456;
      var2.noseR = 0.24F;
      var2.bodyY = 2.3F;
      var2.bodyRx = 2.7F;
      var2.bodyRy = 2.25F;
      var2.bodyRz = 2.5F;
      var2.headY = 5.25F;
      var2.headZ = 0.75F;
      var2.headRx = 3.7F;
      var2.headRy = 2.85F;
      var2.headRz = 2.85F;
      var2.legR = 0.55F;
      var2.legH = 0.8F;
      var2.legX = 1.4F;
      var2.span = 0.95F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earRound(var0x, var1x, Pets.col(15773796), Pets.col(16757702), var2x * 2.1F, 2.0F, -0.4F, var2x, 0.78F);
         }
      };
      var2.bodyExtra = (var1x, var2x) -> {
         var1x.push();
         var1x.translate(0.0F, 0.55F, var2.bodyRz + 0.05F);
         var1x.rotX(-10.0F);
         PetGeo.blob(var1x, "pet_berry", 0.0F, 0.0F, 0.1F, 0.62F, 0.7F, 0.5F, 6, 4);
         var1x.push();
         var1x.translate(0.0F, 0.55F, 0.1F);
         var1x.scale(1.0F, 0.5F, 1.0F);
         PetGeo.cone(var1x, Pets.col(6079578), 0.5F, 0.6F, 6, false);
         var1x.pop();

         for (byte var3 = -1; var3 <= 1; var3 += 2) {
            PetGeo.blob(var1x, Pets.col(16770262), var3 * 0.62F, -0.05F, 0.15F, 0.36F, 0.32F, 0.32F, 6, 3);
         }

         var1x.pop();
      };
      var2.tail = (var0x, var1x) -> PetGeo.blob(var0x, Pets.col(15773796), 0.0F, 0.3F, 0.0F, 0.4F, 0.4F, 0.4F, 6, 4);
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void bunny(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float[] var3 = PetRig.hop(var2);
      var2.y = var2.y + var3[0] * (2.2F + var2.gr * 1.5F);
      var2.sy = var2.sy * (1.0F + var3[1] * 0.14F);
      var2.sx = var2.sx * (1.0F - var3[1] * 0.07F);
      var2.pitch = var3[2] * 14.0F - Math.max(0.0F, -var3[1]) * 6.0F;
      var2.walkW = 0.0F;
      var2.runW = 0.0F;
      var2.bob = 0.0F;
      Pets.Q4 var4 = new Pets.Q4();
      var4.body = Pets.fur("bunny");
      var4.head = Pets.head("bunny");
      var4.leg = Pets.col(16776181);
      var4.style = "std";
      var4.nose = 16748456;
      var4.noseR = 0.24F;
      var4.bodyRx = 2.4F;
      var4.bodyRy = 2.1F;
      var4.headY = 5.9F;
      var4.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            var0x.push();
            var0x.translate(var2x * 1.15F, 2.2F, -0.45F);
            float var3x = Math.max(0.0F, 1.0F - var1x.ear);
            var0x.rotZ(-var2x * (7.0F + var3x * 72.0F + var1x.earTwitch * (var2x > 0 ? 1 : 0)) + PetGeo.sin(var1x.t * 1.6F + var2x) * 2.0F);
            var0x.rotX(-8.0F - var3x * 42.0F + (var1x.ear > 1.0F ? 6 : 0));
            var0x.scale(1.0F, 1.0F + var1x.gr * 0.15F, 1.0F);
            PetGeo.blob(var0x, Pets.col(15850694), 0.0F, 2.0F, 0.0F, 0.78F, 2.35F, 0.44F, 8, 6);
            PetGeo.blob(var0x, Pets.col(16758728), 0.0F, 2.0F, 0.24F, 0.46F, 1.85F, 0.24F, 6, 4);
            var0x.pop();
         }
      };
      var4.tail = (var0x, var1x) -> PetGeo.blob(var0x, Pets.col(16777215), 0.0F, 0.5F, 0.0F, 0.8F, 0.8F, 0.7F, 7, 4);
      Pets.quad4(var0, var2, var4);
   }

   static void unicorn(G var0, Cos.A var1) {
      Pets.Q4 var2 = new Pets.Q4();
      var2.body = Pets.fur("unicorn");
      var2.head = Pets.head("unicorn");
      var2.leg = Pets.col(16774399);
      var2.style = "gem";
      var2.legH = 1.55F;
      var2.legR = 0.62F;
      var2.bodyY = 2.95F;
      var2.headY = 6.45F;
      var2.headZ = 1.1F;
      var2.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            Pets.earPointy(var0x, var1x, Pets.col(16776191), Pets.col(16763108), var2x * 1.95F, 1.95F, -0.45F, var2x, 0.8F, 1.4F, 18.0F);
         }
      };
      int[] var3 = new int[]{16751560, 16763274, 16773280, 11071680, 10277631, 13150463};
      var2.headExtra = (var1x, var2x) -> {
         var1x.push();
         var1x.translate(0.0F, 2.55F, 0.95F);
         var1x.rotX(18.0F);
         var1x.glow(true);
         PetGeo.cone(var1x, "pet_horn", 0.5F + var2x.gr * 0.15F, 2.3F + var2x.gr * 1.6F + (var2x.lvl >= 5 ? 0.6F : 0.0F), 8, false);
         var1x.glow(false);
         var1x.pop();

         for (int var3x = 0; var3x < 4; var3x++) {
            double var4 = Math.toRadians(84 + var3x * 34);
            var1x.push();
            var1x.translate(-0.12F + var3x % 2 * 0.24F, PetGeo.sin(var4) * 2.8F, PetGeo.cos(var4) * 2.65F - 0.1F);
            var1x.rotX((float)(90.0 - Math.toDegrees(var4)));
            var1x.rotZ(PetGeo.sin(var2x.t * 2.0F + var3x) * 4.0F);
            PetGeo.blob(var1x, Pets.col(var3[var3x + 1]), 0.0F, 0.1F, 0.0F, 1.1F, 0.6F, 1.35F, 6, 3);
            var1x.pop();
         }

         var1x.push();
         var1x.translate(0.75F, 2.35F, 1.25F);
         var1x.rotZ(-35.0F);
         PetGeo.blob(var1x, Pets.col(var3[0]), 0.0F, 0.0F, 0.0F, 0.75F, 0.55F, 0.6F, 7, 4);
         var1x.pop();
      };
      var2.tail = (var1x, var2x) -> Pets.tail(
         var1x, var2x, new String[]{Pets.col(var3[0]), Pets.col(var3[4])}, new float[]{0.7F, 0.8F}, 1.3F, -10.0F, 30.0F, 0.8F
      );
      Pets.quad4(var0, Pets.pose(var1), var2);
   }

   static void miniDragon(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      Pets.Q4 var3 = new Pets.Q4();
      var3.body = Pets.fur("dragon");
      var3.head = Pets.head("dragon");
      var3.leg = Pets.col(10125548);
      var3.style = "amber";
      var3.nose = 8611808;
      var3.noseR = 0.16F;
      var3.noseEl = -0.1F;
      var3.ears = (var0x, var1x) -> {
         for (byte var2x = -1; var2x <= 1; var2x += 2) {
            var0x.push();
            var0x.translate(var2x * 1.3F, 2.3F, -0.5F);
            var0x.rotZ(-var2x * 18);
            var0x.rotX(-30.0F);
            PetGeo.cone(var0x, Pets.col(16771504), 0.45F, 1.5F, 8, false);
            var0x.pop();
            Pets.earPointy(var0x, var1x, Pets.col(11440887), Pets.col(16763368), var2x * 2.5F, 1.2F, -0.4F, var2x, 0.7F, 1.3F, 55.0F);
         }
      };
      var3.bodyExtra = (var0x, var1x) -> {
         float var2x = var1x.flap * (var1x.is("sleepy") ? 6.0F : 26.0F + 20.0F * var1x.runW);
         float var3x = 1.0F + var1x.gr * 0.5F + (var1x.lvl >= 5 ? 0.2F : 0.0F);

         for (byte var4 = -1; var4 <= 1; var4 += 2) {
            var0x.push();
            var0x.translate(var4 * 0.9F, 1.45F, -0.5F);
            var0x.scale(var4 * var3x, var3x, var3x);
            var0x.rotY(30.0F);
            var0x.rotZ(22.0F + var2x);
            var0x.planeTL("pet_dragon_wing", 4.2F, 3.2F, 0.0F, 0.0F, 1.0F, 1.0F);
            var0x.pop();
         }

         for (int var5 = 0; var5 < 3; var5++) {
            var0x.push();
            var0x.translate(0.0F, 1.85F - var5 * 0.25F, -0.4F - var5 * 0.9F);
            var0x.rotX(-20 - var5 * 10);
            var0x.scale(0.5F, 1.0F, 1.0F);
            PetGeo.cone(var0x, Pets.col(16766632), 0.45F, 0.8F, 6, false);
            var0x.pop();
         }
      };
      var3.tail = (var0x, var1x) -> Pets.tail(
         var0x,
         var1x,
         new String[]{Pets.col(11440887), Pets.col(11440887), Pets.col(10125548), Pets.col(16757722)},
         new float[]{0.7F, 0.56F, 0.44F, 0.4F},
         1.1F,
         6.0F,
         8.0F,
         1.2F
      );
      Pets.quad4(var0, var2, var3);
   }

   private static void beak(G var0, float var1, float var2, float var3, float var4, int var5, int var6) {
      var0.push();
      var0.translate(0.0F, var1, var2);
      var0.push();
      var0.rotX(90.0F - var4 * 18.0F);
      var0.scale(1.0F, 1.0F, 0.62F);
      PetGeo.cone(var0, Pets.gl(var5), 0.62F * var3, 1.0F * var3, 8, true);
      var0.pop();
      var0.push();
      var0.translate(0.0F, -0.12F * var3, 0.0F);
      var0.rotX(90.0F + var4 * 26.0F);
      var0.scale(1.0F, 1.0F, 0.5F);
      PetGeo.cone(var0, Pets.gl(var6), 0.5F * var3, 0.72F * var3, 8, true);
      var0.pop();
      var0.pop();
   }

   private static float beakOpen(Pets.P var0) {
      String var1 = var0.emo;
      switch (var1) {
         case "excited":
            return 0.6F + 0.4F * Math.abs(PetGeo.sin(var0.t * 7.0F));
         case "happy":
            return 0.45F;
         case "angry":
            return 0.25F + 0.2F * Math.abs(PetGeo.sin(var0.t * 12.0F));
         case "curious":
            return 0.2F;
         default:
            return 0.05F;
      }
   }

   private static float wingFlap(Pets.P var0) {
      String var1 = var0.emo;
      switch (var1) {
         case "excited":
            return 55.0F;
         case "happy":
            return 22.0F;
         case "love":
            return 14.0F;
         case "angry":
            return 30.0F;
         case "sad":
         case "sleepy":
            return 3.0F;
         default:
            return 10.0F;
      }
   }

   private static void feet2(G var0, Pets.P var1, String var2, float var3, float var4, float var5) {
      PetRig.feet(var0, var1, var2, var3, var4, var5, 0.9F);
   }

   private static void birdLegs(G var0, Pets.P var1, String var2, float var3, float var4) {
      if (!(var4 < 0.2F)) {
         for (byte var5 = -1; var5 <= 1; var5 += 2) {
            float var6 = PetRig.frac(var1.t * 2.1F + (var5 > 0 ? 0.0F : 0.5F));
            float var7 = var1.walkW * Math.max(0.0F, PetGeo.sin((Math.PI * 2) * var6)) * 0.45F;
            var0.push();
            var0.translate(var5 * var3, 0.3F + var7, 0.3F);
            var0.cylinder(var2, 0.2F, 0.16F, 0.0F, var4 + 0.2F, 5, false, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }
      }
   }

   static void chick(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.gr;
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, var2.waddleBob, 0.0F);
      float var4 = var3 * 1.3F;
      feet2(var0, var2, Pets.gl(16753724), 1.1F, 0.4F, 0.55F + var3 * 0.1F);
      birdLegs(var0, var2, Pets.gl(16753724), 1.0F, var4);
      float var5 = 3.15F + var4 - var2.lie * 0.5F;
      float var6 = 3.0F;
      float var7 = 2.85F + var3 * 0.2F;
      float var8 = 2.85F + var3 * 0.6F;
      var0.push();
      var0.translate(0.0F, var5, 0.0F);
      var0.rotZ(var2.waddle);
      var0.rotY(var2.waddle * 0.4F);
      float var9 = wingFlap(var2);
      float var10 = var2.is("excited") ? 18.0F : 9.0F;

      for (byte var11 = -1; var11 <= 1; var11 += 2) {
         var0.push();
         var0.translate(var11 * 2.7F, 0.1F, -0.2F);
         var0.rotZ(
            var11 * (12.0F + var9 * (0.5F + 0.5F * PetGeo.sin(var2.t * var10)))
               + (var2.is("sad") ? -var11 * 10 : 0)
               + var11 * var2.walkW * 10.0F * Math.abs(PetGeo.sin(var2.t * 9.0F))
         );
         PetGeo.blob(var0, Pets.col(16767050), var11 * 0.1F, -0.9F, 0.0F, 0.45F, 1.2F + var3 * 0.3F, 0.95F + var3 * 0.5F, 7, 4);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 0.3F + var3 * 0.8F, -var8 + 0.2F);
      var0.rotX(-30.0F - var3 * 25.0F + var2.tailSway * 0.5F);
      PetGeo.blob(var0, Pets.col(var3 > 0.5F ? 16762938 : 16767050), 0.0F, 0.0F, -0.3F, 0.8F + var3 * 0.3F, 0.5F + var3 * 0.9F, 0.6F + var3 * 0.4F, 7, 4);
      var0.pop();
      var0.rotY(var2.headYaw * 0.6F);
      var0.rotX(var2.headPitch * 0.5F + var2.walkW * 6.0F * PetGeo.sin(var2.t * Math.PI * 2.0 * 2.1F * 2.0));
      var0.rotZ(var2.headRoll);
      PetGeo.blob(var0, Pets.fur("chick"), 0.0F, 0.0F, 0.0F, var6, var7, var8, 16, 11);
      Pets.face(var0, var2, PetRig.style("beak", var3), 0.0F, 0.0F, 0.0F, var6, var7, var8, 0.95F, 0.28F);
      beak(var0, PetGeo.sin(0.16F) * var7, PetGeo.cos(0.16F) * var8 * 0.95F, 0.85F, beakOpen(var2), 16753724, 15764010);
      if (var3 > 0.35F) {
         float var13 = Math.min(1.0F, (var3 - 0.35F) / 0.45F);

         for (int var12 = -1; var12 <= 1; var12++) {
            PetGeo.blob(
               var0,
               Pets.gl(15216698),
               0.0F,
               var7 - 0.1F + (1 - Math.abs(var12)) * 0.35F * var13,
               0.9F + var12 * 0.7F,
               0.25F * var13 + 0.05F,
               0.55F * var13 + 0.05F,
               0.4F * var13 + 0.05F,
               5,
               3
            );
         }

         PetGeo.blob(var0, Pets.gl(15216698), 0.0F, -0.6F, var8 * 0.95F + 0.15F, 0.25F * var13, 0.45F * var13, 0.2F * var13, 5, 3);
      } else {
         for (int var14 = -1; var14 <= 1; var14++) {
            var0.push();
            var0.translate(var14 * 0.3F, var7 - 0.2F, 0.2F);
            var0.rotZ(-var14 * 28 + PetGeo.sin(var2.t * 3.0F) * 6.0F);
            PetGeo.blob(var0, Pets.col(16770154), 0.0F, 0.55F, 0.0F, 0.26F, 0.7F, 0.26F, 6, 4);
            var0.pop();
         }
      }

      var0.pop();
      Pets.fx(var0, var2, var5 + var7 + 1.5F);
      var0.pop();
   }

   static void penguin(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.gr;
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, var2.waddleBob * 0.7F, 0.0F);
      feet2(var0, var2, Pets.gl(16753724), 1.2F, 0.3F, 0.6F + var3 * 0.15F);
      float var4 = 3.55F + var3 * 1.1F - var2.lie * 0.6F;
      float var5 = 2.75F;
      float var6 = 3.3F + var3 * 1.3F;
      float var7 = 2.55F;
      var0.push();
      var0.translate(0.0F, var4, 0.0F);
      var0.rotZ(var2.waddle * 1.2F);
      var0.rotY(var2.waddle * 0.5F);
      float var8 = wingFlap(var2);
      float var9 = var2.is("excited") ? 16.0F : 7.0F;

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         var0.push();
         var0.translate(var10 * 2.45F, 0.5F + var3 * 0.5F, -0.1F);
         var0.rotZ(
            var10 * (14.0F + var8 * (0.5F + 0.5F * PetGeo.sin(var2.t * var9)) + var2.walkW * 18.0F) - var2.waddle * 0.8F + (var2.is("sad") ? -var10 * 8 : 0)
         );
         PetGeo.blob(var0, Pets.col(3424874), var10 * 0.15F, -1.2F - var3 * 0.4F, 0.0F, 0.42F, 1.6F + var3 * 0.6F, 0.9F, 7, 4);
         var0.pop();
      }

      var0.rotY(var2.headYaw * 0.6F);
      var0.rotX(var2.headPitch * 0.4F);
      var0.rotZ(var2.headRoll * 0.8F);
      PetGeo.blob(var0, Pets.fur("penguin"), 0.0F, 0.0F, 0.0F, var5, var6, var7, 18, 12);
      Pets.face(var0, var2, PetRig.style("beak", var3), 0.0F, (var6 - 3.3F) * 0.6F, 0.0F, var5, 3.3F, var7, 0.9F, 0.42F);
      beak(var0, PetGeo.sin(0.3F) * 3.3F + (var6 - 3.3F) * 0.6F, PetGeo.cos(0.3F) * var7 * 0.95F, 0.85F + var3 * 0.2F, beakOpen(var2), 16753724, 15764010);
      if (var3 > 0.4F) {
         float var12 = Math.min(1.0F, (var3 - 0.4F) / 0.4F);

         for (byte var11 = -1; var11 <= 1; var11 += 2) {
            PetGeo.blob(var0, Pets.gl(16761402), var11 * 1.9F, var6 * 0.45F, 1.2F, 0.35F * var12, 0.8F * var12, 0.5F * var12, 5, 3);
         }
      }

      var0.pop();
      Pets.fx(var0, var2, var4 + var6 + 1.2F);
      var0.pop();
   }

   static void owl(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float var3 = var2.gr;
      float var4 = PetRig.smooth(0.45F, 0.85F, var2.sp);
      float[] var5 = PetRig.flap(var2, 2.4F);
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, var4 * (5.0F + var5[1] * 0.8F) + var2.waddleBob * (1.0F - var4), 0.0F);
      if (var4 < 0.9F) {
         feet2(var0, var2, Pets.gl(15905354), 1.0F, 0.5F, 0.45F + var3 * 0.1F);
      }

      float var6 = 2.75F + var3 * 0.4F - var2.lie * 0.4F;
      var0.push();
      var0.translate(0.0F, var6, 0.0F);
      var0.rotZ(var2.waddle * (1.0F - var4));
      var0.rotX(var4 * 25.0F);
      PetGeo.blob(var0, Pets.fur("owl"), 0.0F, 0.0F, 0.0F, 2.8F, 2.6F + var3 * 0.6F, 2.6F, 14, 8);
      float var7 = wingFlap(var2);
      float var8 = var2.is("excited") ? 16.0F : 8.0F;

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         var0.push();
         var0.translate(var9 * 2.5F, 1.0F, -0.3F);
         float var10 = var9 * (8.0F + var7 * (0.5F + 0.5F * PetGeo.sin(var2.t * var8))) + (var2.is("sad") ? -var9 * 6 : 0);
         float var11 = var9 * (70.0F + var5[0] * 55.0F);
         var0.rotZ(var10 * (1.0F - var4) + var11 * var4);
         PetGeo.blob(var0, Pets.col(9068608), var9 * 0.15F, -1.1F - var3 * 0.3F, 0.0F, 0.5F, 1.6F + var3 * 0.7F + var4 * 0.6F, 1.3F + var3 * 0.3F, 7, 4);
         var0.pop();
      }

      var0.pop();
      float var14 = 1.0F - var3 * 0.12F;
      var0.push();
      var0.translate(0.0F, 5.75F + var3 * 0.7F - var2.lie * 0.7F, 0.25F);
      Pets.headTurn(var0, var2);
      var0.scale(var14);
      float var15 = 3.2F;
      float var16 = 2.6F;
      float var12 = 2.75F;
      PetGeo.blob(var0, Pets.head("owl"), 0.0F, 0.0F, 0.0F, var15, var16, var12, 17, 11);
      Pets.face(var0, var2, PetRig.style("owl", var3), 0.0F, 0.0F, 0.0F, var15, var16, var12, 1.0F, 0.0F);
      var0.push();
      var0.translate(0.0F, -0.25F, var12 * 0.96F);
      var0.rotX(150.0F);
      var0.scale(1.0F, 1.0F, 0.7F);
      PetGeo.cone(var0, Pets.gl(15905354), 0.42F, 1.0F, 8, true);
      var0.pop();

      for (byte var13 = -1; var13 <= 1; var13 += 2) {
         Pets.earPointy(var0, var2, Pets.col(9068608), Pets.col(10975311), var13 * 2.1F, 1.7F, -0.2F, var13, 0.75F + var3 * 0.15F, 1.6F + var3 * 0.9F, 28.0F);
      }

      var0.pop();
      Pets.fx(var0, var2, 5.75F + var3 * 0.7F + 2.6F * var14 + 1.4F);
      var0.pop();
   }

   static void frog(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float[] var3 = PetRig.hop(var2);
      var2.y = var2.y + var3[0] * 2.4F;
      var2.sy = var2.sy * (1.0F + var3[1] * 0.12F);
      var2.sx = var2.sx * (1.0F - var3[1] * 0.06F);
      var2.stepBob = 0.0F;
      float var4 = var3[2];
      var0.push();
      Pets.root(var0, var2);
      var0.rotX(-var4 * 18.0F);
      String var5 = Pets.col(8835424);
      String var6 = Pets.col(10936446);
      float var7 = var2.lie * 0.4F + Math.max(0.0F, -var3[1]) * 0.35F;

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         PetGeo.blob(
            var0, var5, var8 * 2.15F, 1.05F - var7 - var4 * 0.3F, -0.9F - var4 * 1.0F, 1.05F - var4 * 0.2F, 0.95F - var4 * 0.25F, 1.5F + var4 * 0.9F, 7, 4
         );
         PetGeo.blob(var0, var6, var8 * 2.45F, 0.22F + var4 * 0.1F, 0.35F - var4 * 2.6F, 0.85F, 0.24F, 1.15F, 8, 4);
         var0.push();
         var0.translate(var8 * 1.3F, 1.4F - var7, 1.35F);
         var0.rotX(var8 * var2.walk * 0.6F);
         PetGeo.blob(var0, var5, 0.0F, -0.6F, 0.0F, 0.42F, 0.8F, 0.42F, 6, 3);
         PetGeo.blob(var0, var6, 0.0F, -1.25F, 0.2F, 0.55F, 0.18F, 0.55F, 5, 3);
         var0.pop();
      }

      PetGeo.blob(var0, Pets.fur("frog"), 0.0F, 2.1F - var7, -0.3F, 2.6F, 2.0F, 2.45F, 11, 6);
      var0.push();
      var0.translate(0.0F, 4.25F - var7 * 1.6F, 0.55F);
      Pets.headTurn(var0, var2);
      float var19 = 3.5F;
      float var9 = 2.2F;
      float var10 = 2.85F;
      PetGeo.blob(var0, Pets.head("frog"), 0.0F, 0.0F, 0.0F, var19, var9, var10, 15, 10);
      boolean var11 = var0.glow;
      PetGeo.decal(var0, "pet_face:blob:" + var2.emo, 0.0F, 0.0F, 0.0F, var19, var9, var10, 0.0F, 0.28F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.035F);
      String var12 = Pets.eyes("blob", var2);

      for (byte var13 = -1; var13 <= 1; var13 += 2) {
         float var14 = var13 * 1.55F;
         float var15 = 1.65F;
         float var16 = 0.5F;
         float var17 = 1.3F;
         PetGeo.blob(var0, var5, var14, var15, var16, var17, var17 * 0.95F, var17, 9, 6);
         float var18 = var13 > 0 ? 0.0546875F : 0.4453125F;
         PetGeo.decal(
            var0,
            var12,
            var14,
            var15,
            var16,
            var17,
            var17 * 0.95F,
            var17,
            var2.lookX * 0.1F,
            0.12F + var2.lookY * 0.06F,
            1.2F,
            var18,
            0.104166664F,
            var18 + 0.5F,
            0.7708333F,
            1.05F
         );
      }

      var0.glow(var11);
      var0.pop();
      Pets.fx(var0, var2, 7.2F);
      var0.pop();
   }

   static void axolotl(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      String var3 = Pets.col(16758990);

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         for (int var5 = 0; var5 < 2; var5++) {
            var0.push();
            float var6 = var5 == 0 ? 0.9F : -2.1F;
            var0.translate(var4 * 1.65F, 0.75F - var2.lie * 0.3F, var6);
            var0.rotZ(-var4 * 35);
            var0.rotX((var5 == 0 ? var4 : -var4) * var2.walk);
            PetGeo.blob(var0, var3, 0.0F, -0.35F, 0.0F, 0.36F, 0.62F, 0.36F, 7, 4);
            var0.pop();
         }
      }

      PetGeo.blob(var0, Pets.fur("axolotl"), 0.0F, 1.6F - var2.lie * 0.35F, -0.6F, 2.0F, 1.45F, 2.9F, 12, 7);
      var0.push();
      var0.translate(0.0F, 1.85F - var2.lie * 0.35F, -3.0F);
      var0.rotY(var2.tail * 1.2F + PetGeo.sin(var2.t * 3.0F) * 8.0F + PetGeo.sin(var2.t * 9.0F) * 16.0F * var2.walkW);
      PetGeo.blob(var0, Pets.col(16763096), 0.0F, 0.1F, -1.5F, 0.32F, 1.05F, 1.9F, 8, 5);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 3.45F - var2.lie * 0.6F, 1.35F);
      Pets.headTurn(var0, var2);
      float var10 = 3.4F;
      float var11 = 2.35F;
      float var12 = 2.55F;
      PetGeo.blob(var0, Pets.head("axolotl"), 0.0F, 0.0F, 0.0F, var10, var11, var12, 15, 10);
      Pets.face(var0, var2, "blob", 0.0F, 0.0F, 0.0F, var10, var11, var12, 1.05F, -0.02F);
      float var7 = Math.max(0.0F, 1.0F - var2.ear) * 30.0F;

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         for (int var9 = 0; var9 < 3; var9++) {
            var0.push();
            var0.translate(var8 * 2.75F, 1.0F - var9 * 0.8F, -0.6F);
            var0.rotZ(-var8 * (62 - var9 * 42 + var7) + PetGeo.sin(var2.t * 4.0F + var9) * 7.0F);
            var0.scale(1.0F, 1.0F + var2.gr * 0.5F, 1.0F);
            PetGeo.blob(var0, Pets.col(15890335), 0.0F, 0.9F, 0.0F, 0.32F, 1.05F, 0.28F, 6, 4);
            var0.pop();
         }
      }

      var0.pop();
      Pets.fx(var0, var2, 7.0F);
      var0.pop();
   }

   static void turtle(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      String var3 = Pets.col(10476418);
      Pets.legs4(var0, var2, var3, 2.0F, 1.6F, -1.6F, 0.72F, 1.05F);
      float var4 = var2.lie * 0.6F;
      PetGeo.blob2(var0, "pet_shell", 0.0F, 1.95F - var4, -0.2F, 3.25F, 2.55F, 0.75F, 3.45F, 16, 10);
      var0.push();
      var0.translate(0.0F, 1.5F - var4, -3.4F);
      var0.rotY(var2.tail);
      var0.rotX(-100.0F);
      PetGeo.cone(var0, var3, 0.4F, 0.9F, 6, false);
      var0.pop();
      var0.push();
      var0.translate(0.0F, 3.0F - var4, 3.45F - var2.lie * 1.2F);
      Pets.headTurn(var0, var2);
      float var5 = 2.65F;
      float var6 = 2.3F;
      float var7 = 2.3F;
      PetGeo.blob(var0, Pets.head("turtle"), 0.0F, 0.0F, 0.0F, var5, var6, var7, 17, 11);
      Pets.face(var0, var2, "std", 0.0F, 0.0F, 0.0F, var5, var6, var7, 1.05F, 0.02F);
      var0.pop();
      Pets.fx(var0, var2, 6.2F);
      var0.pop();
   }

   static void bee(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, 4.3F * var2.hover + PetGeo.sin(var2.t * 2.6F) * 0.4F - var2.flap * 0.18F, 0.0F);
      var0.rotX(var2.sp * 16.0F);
      var0.push();
      var0.translate(0.0F, -0.3F, -1.3F);
      var0.rotX(-12.0F + PetGeo.sin(var2.t * 2.0F) * 4.0F);
      PetGeo.blob(var0, Pets.fur("bee"), 0.0F, 0.0F, -0.5F, 2.15F, 2.0F, 2.5F, 14, 8);
      var0.push();
      var0.translate(0.0F, -0.3F, -2.9F);
      var0.rotX(-95.0F);
      PetGeo.cone(var0, Pets.col(3811882), 0.35F, 0.8F, 6, false);
      var0.pop();
      var0.pop();
      float var3 = var2.flap * (var2.is("sleepy") ? 10 : 34);
      int var4 = var0.color;
      var0.color(-654311425).glow(true);

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         for (int var6 = 0; var6 < 2; var6++) {
            var0.push();
            var0.translate(var5 * 0.7F, 1.6F, -0.9F - var6 * 0.6F);
            var0.scale(var5, 1.0F, 1.0F);
            var0.rotY(35 + var6 * 20);
            var0.rotZ(25 - var6 * 12 + var3);
            var0.scale(1.0F + var2.gr * 0.3F);
            var0.plane("pet_wing", 1.4F - var6 * 0.3F, 0.5F, 2.8F - var6 * 0.7F, 1.8F - var6 * 0.5F, 0.0F, 0.0F, 1.0F, 1.0F);
            var0.pop();
         }
      }

      var0.color(var4).glow(false);
      var0.push();
      var0.translate(0.0F, 0.9F, 1.15F);
      Pets.headTurn(var0, var2);
      float var10 = 2.6F;
      float var11 = 2.45F;
      float var7 = 2.35F;
      PetGeo.blob(var0, Pets.head("bee"), 0.0F, 0.0F, 0.0F, var10, var11, var7, 18, 11);
      Pets.face(var0, var2, "blob", 0.0F, 0.0F, 0.0F, var10, var11, var7, 1.0F, 0.0F);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 0.8F, var11 - 0.3F, 0.3F);
         float var9 = Math.max(0.0F, 1.0F - var2.ear);
         var0.rotZ(-var8 * (18.0F + var9 * 40.0F) + PetGeo.sin(var2.t * 5.0F + var8) * 6.0F);
         var0.rotX(-12.0F + var9 * 30.0F);
         var0.cylinder(Pets.col(3811882), 0.09F, 0.09F, 0.0F, 1.4F, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
         PetGeo.blob(var0, Pets.gl(3811882), 0.0F, 1.55F, 0.0F, 0.36F, 0.36F, 0.36F, 7, 4);
         var0.pop();
      }

      var0.pop();

      for (byte var12 = -1; var12 <= 1; var12 += 2) {
         PetGeo.blob(var0, Pets.col(3811882), var12 * 0.8F, -1.9F, -0.4F, 0.22F, 0.5F, 0.22F, 6, 3);
      }

      Pets.fx(var0, var2, 3.8F);
      var0.pop();
   }

   static void bat(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, 4.6F * var2.hover + PetGeo.sin(var2.t * 3.0F) * 0.3F - var2.flap * 0.45F, 0.0F);
      var0.rotX(var2.sp * 14.0F);
      float var3 = var2.flap * (var2.is("sleepy") ? 8.0F : (var2.is("excited") ? 48.0F : 36.0F + var2.sp * 10.0F));

      for (byte var4 = -1; var4 <= 1; var4 += 2) {
         var0.push();
         var0.translate(var4 * 1.9F, 0.7F, -0.5F);
         var0.scale(var4, 1.0F, 1.0F);
         var0.rotY(22.0F);
         var0.rotZ(-8.0F + var3);
         var0.scale(1.0F + var2.gr * 0.4F);
         var0.planeTL("pet_bat_wing", 4.6F, 2.3F, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         PetGeo.blob(var0, Pets.col(4864618), var8 * 0.8F, -2.5F, 0.0F, 0.3F, 0.35F, 0.3F, 6, 3);
      }

      var0.push();
      Pets.headTurn(var0, var2);
      float var9 = 2.8F;
      float var5 = 2.65F;
      float var6 = 2.55F;
      PetGeo.blob(var0, Pets.head("bat"), 0.0F, 0.0F, 0.0F, var9, var5, var6, 18, 12);
      Pets.face(var0, var2, "gem", 0.0F, 0.0F, 0.0F, var9, var5, var6, 1.0F, 0.05F);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         var0.push();
         var0.translate(var7 * 0.32F, -0.95F, var6 * 0.93F);
         var0.rotX(180.0F);
         PetGeo.cone(var0, Pets.col(16777215), 0.14F, 0.38F, 5, false);
         var0.pop();
      }

      for (byte var10 = -1; var10 <= 1; var10 += 2) {
         Pets.earPointy(var0, var2, Pets.col(7232660), Pets.col(16757712), var10 * 1.55F, 1.9F, -0.2F, var10, 1.05F, 2.2F, 16.0F);
      }

      var0.pop();
      Pets.fx(var0, var2, 3.6F);
      var0.pop();
   }

   static void ghostPet(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      var0.translate(0.0F, 2.4F * var2.hover + 0.8F + PetGeo.sin(var2.t * 2.0F) * 0.5F, 0.0F);
      int var3 = var0.color;
      var0.color(-385875969);

      for (int var4 = 0; var4 < 8; var4++) {
         double var5 = (Math.PI * 2) * var4 / 8.0 + 0.2;
         float var7 = PetGeo.sin(var2.t * 4.0F + var4 * 1.3F) * 0.3F;
         PetGeo.blob(var0, Pets.fur("ghost"), PetGeo.cos(var5) * 2.05F, -0.2F + var7, PetGeo.sin(var5) * 1.85F, 1.0F, 1.0F, 1.0F, 7, 4);
      }

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 2.8F, 1.6F, 0.4F);
         var0.rotZ(-var8 * (30.0F + (!var2.is("excited") && !var2.is("happy") ? 0.0F : PetGeo.sin(var2.t * 8.0F) * 25.0F) - (var2.is("sad") ? 30 : 0)));
         PetGeo.blob(var0, Pets.fur("ghost"), 0.0F, -0.6F, 0.0F, 0.55F, 0.95F, 0.55F, 7, 4);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 2.1F, 0.0F);
      Pets.headTurn(var0, var2);
      float var9 = 3.0F;
      float var10 = 3.1F;
      float var6 = 2.8F;
      PetGeo.blob(var0, Pets.fur("ghost"), 0.0F, 0.0F, 0.0F, var9, var10, var6, 18, 12);
      var0.color(var3);
      Pets.face(var0, var2, "blob", 0.0F, 0.0F, 0.0F, var9, var10, var6, 1.0F, 0.1F);
      var0.pop();
      var0.color(var3);
      Pets.fx(var0, var2, 6.4F);
      var0.pop();
   }

   static void jelly(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      float var3 = PetGeo.sin(var2.t * (var2.is("excited") ? 6 : 3));
      var0.translate(0.0F, 3.9F * var2.hover + 0.6F + PetGeo.sin(var2.t * 1.8F) * 0.6F + var3 * 0.2F, 0.0F);
      int var4 = var0.color;
      boolean var5 = var0.glow;
      var0.color(-788529153).glow(true);

      for (int var6 = 0; var6 < 6; var6++) {
         double var7 = (Math.PI * 2) * var6 / 6.0 + 0.5;
         float var9 = PetGeo.cos(var7) * 1.6F;
         float var10 = PetGeo.sin(var7) * 1.6F;
         float var11 = (3.4F - var6 % 2 * 0.6F) * (1.0F + var2.gr * 0.45F);
         float var12 = 0.0F;
         float var13 = 0.0F;

         for (int var14 = 0; var14 < 5; var14++) {
            float var15 = var14 / 5.0F;
            float var16 = (var14 + 1) / 5.0F;
            float var17 = PetGeo.sin(var2.t * 3.0F + var6 + var15 * 4.0F) * 0.45F * var15;
            float var18 = PetGeo.sin(var2.t * 3.0F + var6 + var16 * 4.0F) * 0.45F * var16;
            float var19 = 0.3F * (1.0F - var15 * 0.6F);
            float var20 = 0.3F * (1.0F - var16 * 0.6F);
            float var21 = -var15 * var11;
            float var22 = -var16 * var11;

            for (int var23 = 0; var23 < 2; var23++) {
               float var24 = var23 == 0 ? 1.0F : 0.0F;
               float var25 = var23 == 0 ? 0.0F : 1.0F;
               var0.quad(
                  "pet_jelly",
                  new float[]{var9 + var17 - var19 * var24, var21, var10 - var19 * var25},
                  new float[]{var9 + var17 + var19 * var24, var21, var10 + var19 * var25},
                  new float[]{var9 + var18 + var20 * var24, var22, var10 + var20 * var25},
                  new float[]{var9 + var18 - var20 * var24, var22, var10 - var20 * var25},
                  new float[]{0.2F, 0.6F + var15 * 0.3F, 0.3F, 0.6F + var15 * 0.3F, 0.3F, 0.6F + var16 * 0.3F, 0.2F, 0.6F + var16 * 0.3F}
               );
            }

            var12 = var18;
            var13 = var22;
         }

         PetGeo.blob(var0, "pet_jelly", var9 + var12, var13, var10, 0.2F, 0.2F, 0.2F, 5, 3);
      }

      for (int var26 = 0; var26 < 10; var26++) {
         double var28 = (Math.PI * 2) * var26 / 10.0;
         PetGeo.blob(var0, "pet_jelly", PetGeo.cos(var28) * 2.55F, 0.35F, PetGeo.sin(var28) * 2.55F, 0.6F, 0.45F, 0.6F, 6, 3);
      }

      var0.color(-587202561);
      var0.push();
      var0.translate(0.0F, 0.45F, 0.0F);
      Pets.headTurn(var0, var2);
      float var27 = 3.05F * (1.0F + var3 * 0.04F);
      float var29 = 2.9F * (1.0F - var3 * 0.05F);
      float var8 = 3.0F * (1.0F + var3 * 0.04F);
      PetGeo.blob2(var0, "pet_jelly", 0.0F, 0.0F, 0.0F, var27, var29, 0.55F, var8, 16, 10);
      var0.color(var4).glow(false);
      Pets.face(var0, var2, "blob", 0.0F, 0.9F, 0.0F, var27, var29 * 0.72F, var8, 1.0F, -0.05F);
      var0.pop();
      var0.color(var4).glow(var5);
      Pets.fx(var0, var2, 4.4F);
      var0.pop();
   }

   static void fish(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      float var3 = 4.25F + PetGeo.sin(var2.t * 1.6F) * 0.3F;
      float var4 = 3.9F;
      var0.push();
      var0.translate(0.0F, var3 + PetGeo.sin(var2.t * 2.2F) * 0.35F, 0.3F);
      var0.rotY(PetGeo.sin(var2.t * 0.8F) * 14.0F + var2.headYaw * 0.8F);
      var0.rotX(var2.headPitch * 0.6F);
      var0.rotZ(var2.headRoll * 0.8F);
      float var5 = 2.35F;
      float var6 = 2.15F;
      float var7 = 2.5F;
      var0.push();
      var0.translate(0.0F, 0.0F, -var7 + 0.2F);
      var0.rotY(90.0F + var2.tail * 1.4F + PetGeo.sin(var2.t * 7.0F) * 18.0F);
      var0.plane("pet_fin", 1.2F, 0.0F, 2.4F, 2.6F, 0.0F, 0.0F, 1.0F, 1.0F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, var6 - 0.25F, -0.5F);
      var0.rotX(-20.0F + PetGeo.sin(var2.t * 3.0F) * 6.0F);
      PetGeo.blob(var0, Pets.col(16756848), 0.0F, 0.45F, 0.0F, 0.22F, 0.75F, 1.0F, 6, 4);
      var0.pop();

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * (var5 - 0.2F), -0.5F, 0.1F);
         var0.rotZ(-var8 * (50.0F + PetGeo.sin(var2.t * 6.0F + var8) * 18.0F));
         PetGeo.blob(var0, Pets.col(16756848), 0.0F, -0.6F, 0.0F, 0.18F, 0.7F, 0.45F, 6, 4);
         var0.pop();
      }

      PetGeo.blob(var0, Pets.fur("fish"), 0.0F, 0.0F, 0.0F, var5, var6, var7, 16, 10);
      Pets.face(var0, var2, "blue", 0.0F, 0.0F, 0.0F, var5, var6, var7, 0.95F, 0.05F);
      var0.pop();
      PetGeo.blob(var0, "pet_bubble", 0.0F, var3, 0.0F, var4, var4, var4, 18, 11);

      for (int var10 = 0; var10 < 2; var10++) {
         float var9 = ((var2.t * 0.4F + var10 * 0.5F) % 1.0F + 1.0F) % 1.0F;
         PetGeo.blob(
            var0, "pet_bubble", 1.2F - var10 * 2.1F, var3 + var4 + var9 * 2.5F, 0.3F, 0.3F + var10 * 0.12F, 0.3F + var10 * 0.12F, 0.3F + var10 * 0.12F, 6, 4
         );
      }

      Pets.fx(var0, var2, var3 + var4 + 0.8F);
      var0.pop();
   }

   static void cloud(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      float var3 = 2.2F * var2.hover + 1.0F + PetGeo.sin(var2.t * 1.7F) * 0.45F;
      var0.translate(0.0F, var3, 0.0F);
      int var4 = var0.color;
      boolean var5 = var2.is("angry");
      var0.color(var5 ? -4933432 : (var2.is("sad") ? -2301202 : -1));
      String var6 = Pets.fur("cloud");
      var0.push();
      Pets.headTurn(var0, var2);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         PetGeo.blob2(var0, var6, var7 * 2.6F, 0.8F, -0.2F, 1.9F, 1.7F, 0.9F, 1.8F, 10, 6);
         PetGeo.blob2(var0, var6, var7 * 1.15F, 3.0F, -0.6F, 1.75F, 1.6F, 1.2F, 1.6F, 10, 6);
      }

      PetGeo.blob2(var0, var6, 0.0F, 1.6F, -1.4F, 2.3F, 2.2F, 1.2F, 1.6F, 10, 6);
      float var16 = 3.1F;
      float var8 = 2.5F;
      float var9 = 2.6F;
      PetGeo.blob2(var0, var6, 0.0F, 1.6F, 0.0F, var16, var8, 1.1F, var9, 16, 10);
      var0.color(var4);
      Pets.face(var0, var2, "blob", 0.0F, 1.6F, 0.0F, var16, var8, var9, 0.95F, -0.08F);
      var0.pop();
      if (!var2.is("sad") && !var5) {
         if (var2.is("happy") || var2.is("excited")) {
            for (int var17 = 0; var17 < 2; var17++) {
               double var18 = var2.t * 1.5 + var17 * Math.PI;
               PetGeo.sprite(var0, "pet_star", PetGeo.cos(var18) * 3.8F, 2.4F + PetGeo.sin(var18 * 2.0) * 0.8F, PetGeo.sin(var18) * 2.0F, 0.9F, -520093697);
            }
         }
      } else {
         for (int var10 = 0; var10 < 4; var10++) {
            float var11 = ((var2.t * 1.3F + var10 * 0.27F) % 1.0F + 1.0F) % 1.0F;
            int var12 = (int)(230.0F * (1.0F - var11));
            float var13 = -1.8F + var10 * 1.2F;
            float var14 = 0.5F - var11 * var3 * 0.9F;
            float var15 = 0.4F * (var10 % 2);
            if (var5 && var10 == 1) {
               PetGeo.sprite(var0, "pet_star", var13, var14, var15, 1.2F, var12 << 24 | 16777215);
            } else {
               var0.color(var12 << 24 | 16777215).glow(true);
               PetGeo.blob(var0, Pets.gl(8374527), var13, var14, var15, 0.16F, 0.36F, 0.16F, 6, 3);
               var0.color(var4).glow(false);
            }
         }
      }

      Pets.fx(var0, var2, 5.6F);
      var0.pop();
   }

   static void slime(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      float[] var3 = PetRig.hop(var2);
      var2.y = var2.y + var3[0] * 2.0F;
      var2.stepBob = 0.0F;
      var0.push();
      Pets.root(var0, var2);
      float var4 = PetGeo.sin(var2.t * 3.0F) * 0.04F - var3[1] * 0.16F;
      float var5 = 3.7F * (1.0F + var4);
      float var6 = 3.0F * (1.0F - var4);
      float var7 = 3.4F * (1.0F + var4);
      var0.push();
      var0.translate(0.0F, var6 * 0.92F, 0.0F);
      PetGeo.blob(var0, Pets.gl(4173411), 0.6F, -0.4F, -0.9F, 1.45F, 1.25F, 1.35F, 7, 4);
      Pets.headTurn(var0, var2);
      int var8 = var0.color;
      var0.color(-922746881);
      PetGeo.blob2(var0, Pets.gl(9236648), 0.0F, 0.0F, 0.0F, var5, var6, var6 * 0.85F, var7, 18, 12);
      var0.color(var8);
      Pets.face(var0, var2, "blob", 0.0F, 0.0F, 0.0F, var5, var6, var7, 1.0F, 0.02F);
      var0.pop();
      Pets.fx(var0, var2, var6 * 1.9F + 1.2F);
      var0.pop();
   }

   static void mushroom(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      feet2(var0, var2, Pets.col(15257512), 0.95F, 0.1F, 0.55F);
      float var3 = var2.lie * 0.4F;
      float var4 = 2.35F;
      float var5 = 2.3F;
      float var6 = 2.15F;
      var0.push();
      var0.translate(0.0F, 2.45F - var3, 0.0F);
      var0.rotZ(PetGeo.sin(var2.t * 11.0F) * 5.0F * var2.sp);
      PetGeo.blob(var0, Pets.fur("mush"), 0.0F, 0.0F, 0.0F, var4, var5, var6, 17, 11);
      Pets.face(var0, var2, "blob", 0.0F, 0.0F, 0.0F, var4, var5, var6, 1.0F, 0.02F);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         var0.push();
         var0.translate(var7 * 2.2F, -0.2F, 0.2F);
         var0.rotZ(-var7 * (25.0F + (var2.is("excited") ? Math.abs(PetGeo.sin(var2.t * 7.0F)) * 60.0F : 0.0F) - (var2.is("sad") ? 15 : 0)));
         PetGeo.blob(var0, Pets.col(16773596), 0.0F, -0.55F, 0.0F, 0.35F, 0.7F, 0.35F, 6, 4);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 2.0F, -0.1F);
      var0.rotY(var2.headYaw * 0.4F);
      var0.rotX(var2.headPitch * 0.6F - 6.0F);
      var0.rotZ(var2.headRoll * 0.8F + PetGeo.sin(var2.t * 2.1F) * 3.0F);
      PetGeo.blob2(var0, "pet_cap", 0.0F, 0.0F, 0.0F, 3.9F, 2.5F, 0.75F, 3.7F, 16, 10);
      var0.pop();
      var0.pop();
      Pets.fx(var0, var2, 7.4F);
      var0.pop();
   }

   static void brickBuddy(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var2.y = var2.y + Math.abs(PetGeo.sin(var2.t * 6.0F)) * 0.8F * var2.sp;
      var0.push();
      Pets.root(var0, var2);
      String var3 = Pets.gl(16764163);
      String var4 = "pet_brick:E3000B";

      for (byte var5 = -1; var5 <= 1; var5 += 2) {
         var0.push();
         var0.translate(var5 * 1.3F, 0.95F, 0.2F);
         var0.rotX(var5 * var2.walk);
         PetGeo.blob(var0, var3, 0.0F, -0.45F, 0.2F, 0.6F, 0.55F, 0.8F, 7, 4);
         var0.pop();
      }

      var0.push();
      var0.rotY(var2.headYaw * 0.5F);
      var0.translate(0.0F, 3.0F, 0.0F);
      var0.rotX(var2.headPitch * 0.5F);
      var0.rotZ(var2.headRoll * 0.7F);
      var0.translate(0.0F, -3.0F, 0.0F);
      PetGeo.rbox(var0, var4, -3.0F, 0.8F, -3.0F, 3.0F, 5.0F, 3.0F, 0.4F);

      for (byte var7 = -1; var7 <= 1; var7 += 2) {
         for (byte var6 = -1; var6 <= 1; var6 += 2) {
            var0.push();
            var0.translate(var7 * 1.5F, 5.0F, var6 * 1.5F);
            var0.cylinder(var4, 1.0F, 1.0F, -0.05F, 0.75F, 12, true, 0.2F, 0.1F, 0.8F, 0.9F);
            var0.pop();
         }
      }

      Pets.faceFlat(var0, var2, "blob", 0.0F, 2.9F, 3.02F, 5.4F, 4.05F);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 3.0F, 3.0F, 0.3F);
         float var9 = var2.is("excited")
            ? 50.0F + PetGeo.sin(var2.t * 9.0F) * 40.0F
            : (var2.is("happy") ? 20.0F + PetGeo.sin(var2.t * 4.0F) * 12.0F : (var2.is("sad") ? -5.0F : 12.0F));
         var0.rotZ(-var8 * var9);
         PetGeo.blob(var0, var3, var8 * 0.25F, -0.7F, 0.0F, 0.42F, 0.85F, 0.42F, 7, 4);
         var0.pop();
      }

      var0.pop();
      Pets.fx(var0, var2, 7.2F);
      var0.pop();
   }

   static void robot(G var0, Cos.A var1) {
      Pets.P var2 = Pets.pose(var1);
      var0.push();
      Pets.root(var0, var2);
      String var3 = "pet_metal:C9D6F0";
      String var4 = "pet_metal:6A7A96";
      String var5 = Pets.gl(8370431);

      for (byte var6 = -1; var6 <= 1; var6 += 2) {
         var0.push();
         var0.translate(var6 * 1.25F, 0.9F, 0.0F);
         var0.rotX(var6 * var2.walk);
         PetGeo.blob(var0, var4, 0.0F, -0.45F, 0.25F, 0.75F, 0.5F, 1.0F, 7, 4);
         var0.pop();
      }

      PetGeo.rbox(var0, var3, -2.0F, 1.0F, -1.7F, 2.0F, 3.9F, 1.7F, 0.45F);
      PetGeo.sprite(var0, "pet_heart", 0.0F, 2.45F, 1.74F, 1.3F, -1);

      for (byte var8 = -1; var8 <= 1; var8 += 2) {
         var0.push();
         var0.translate(var8 * 2.2F, 3.4F, 0.0F);
         float var7 = var2.is("excited")
            ? 120.0F + PetGeo.sin(var2.t * 10.0F) * 30.0F
            : (var2.is("happy") ? 20.0F + PetGeo.sin(var2.t * 4.0F) * 10.0F : (var2.is("sad") ? 2.0F : 10.0F));
         var0.rotX(-var8 * var2.walk);
         var0.rotZ(-var8 * var7);
         var0.cylinder(var4, 0.3F, 0.3F, -1.4F, 0.0F, 8, false, 0.0F, 0.0F, 1.0F, 1.0F);
         PetGeo.blob(var0, var5, 0.0F, -1.6F, 0.0F, 0.5F, 0.5F, 0.5F, 7, 4);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 6.35F - var2.lie * 0.5F, 0.2F);
      Pets.headTurn(var0, var2);
      PetGeo.rbox(var0, var3, -3.1F, -2.3F, -2.5F, 3.1F, 2.3F, 2.5F, 0.6F);
      PetGeo.flat(var0, "pet_screen", 0.0F, -0.05F, 2.52F, 5.2F, 3.7F);
      Pets.faceFlat(var0, var2, "led", 0.0F, -0.05F, 2.55F, 4.9F, 3.68F);

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         var0.push();
         var0.translate(var9 * 3.1F, 0.0F, 0.0F);
         var0.rotZ(90.0F);
         var0.cylinder(var5, 0.85F, 0.85F, -0.35F, 0.35F, 10, true, 0.0F, 0.0F, 1.0F, 1.0F);
         var0.pop();
      }

      var0.push();
      var0.translate(0.0F, 2.3F, 0.0F);
      var0.rotZ(PetGeo.sin(var2.t * 3.0F) * 10.0F + (var2.is("sad") ? 35 : 0));
      var0.cylinder(var4, 0.1F, 0.1F, 0.0F, 1.3F, 6, false, 0.0F, 0.0F, 1.0F, 1.0F);
      boolean var10 = var0.glow;
      var0.glow(true);
      int var11 = var2.is("love") ? 16740296 : (var2.is("angry") ? 16734810 : (var2.is("sleepy") ? 6978303 : 7336959));
      PetGeo.blob(var0, Pets.gl(var11), 0.0F, 1.6F, 0.0F, 0.48F, 0.48F, 0.48F, 7, 4);
      var0.glow(var10);
      var0.pop();
      var0.pop();
      Pets.fx(var0, var2, 10.2F);
      var0.pop();
   }
}
