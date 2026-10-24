package dev.lego.cosmetic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Pets {
   public static final String[] EMOTIONS = new String[]{"happy", "love", "sleepy", "sad", "excited", "angry", "curious", "dizzy"};
   private static final Map<String, Float> HEIGHT = new ConcurrentHashMap<>();

   private Pets() {
   }

   public static float height(String var0) {
      return height(var0, 0.5F);
   }

   public static float height(String var0, float var1) {
      float var2 = Math.round(Math.max(0.0F, Math.min(1.0F, var1)) * 20.0F) / 20.0F;
      String var3 = var0 + "@" + var2;
      Float var4 = HEIGHT.get(var3);
      if (var4 != null) {
         return var4;
      } else {
         Cos.Item var5 = Cos.get(var0);
         float var6 = 10.0F;
         if (var5 != null && var5.slot == Cos.Slot.PET) {
            float[] var7 = new float[]{0.0F};
            G var8 = new G((var1x, var2x, var3x, var4x, var5x, var6x, var7x, var8x) -> {
               if (!var1x.equals("pet_star") && !var1x.equals("pet_heart")) {
                  for (byte var9x = 1; var9x < 12; var9x += 3) {
                     var7[0] = Math.max(var7[0], var2x[var9x]);
                  }
               }
            });
            Cos.A var9 = new Cos.A();
            var9.emotion = "happy";
            var9.time = 0.35F;
            var9.growth = var2;
            var9.level = 1 + Math.round(var2 * 4.0F);

            try {
               var5.model.render(var8, var9);
               var6 = Math.max(3.0F, var7[0]);
            } catch (RuntimeException var11) {
            }

            HEIGHT.put(var3, var6);
         }

         return var6;
      }
   }

   static void registerAll() {
      Cos.add("pet_cat", "Kätzchen", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::cat);
      Cos.add("pet_shiba", "Shiba", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::shiba);
      Cos.add("pet_bunny", "Häschen", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::bunny);
      Cos.add("pet_fox", "Füchschen", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::fox);
      Cos.add("pet_redpanda", "Roter Panda", Cos.Slot.PET, Cos.Rarity.EPIC, PetModels::redPanda);
      Cos.add("pet_panda", "Pandabär", Cos.Slot.PET, Cos.Rarity.RARE, PetModels::panda);
      Cos.add("pet_bear", "Teddybär", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::bear);
      Cos.add("pet_hamster", "Hamster", Cos.Slot.PET, Cos.Rarity.COMMON, PetModels::hamster);
      Cos.add("pet_penguin", "Pinguin", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::penguin, 1.55F, 3.4F));
      Cos.add("pet_chick", "Küken", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::chick, 1.5F, 3.6F));
      Cos.add("pet_owl", "Eule", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::owl, 1.6F, 3.4F));
      Cos.add("pet_frog", "Fröschchen", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::frog, 1.55F, 3.8F));
      Cos.add("pet_axolotl", "Axolotl", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::axolotl, 1.55F, 4.0F));
      Cos.add("pet_turtle", "Schildkröte", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::turtle, 1.6F, 4.2F));
      Cos.add("pet_bee", "Bienchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::bee, 1.45F, 3.4F));
      Cos.add("pet_bat", "Fledermaus", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::bat, 1.5F, 3.6F));
      Cos.add("pet_slime", "Schleimi", Cos.Slot.PET, Cos.Rarity.COMMON, grow(PetModels::slime, 1.6F, 4.2F));
      Cos.add("pet_ghost", "Geistchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::ghostPet, 1.5F, 3.8F));
      Cos.add("pet_jelly", "Qualle", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::jelly, 1.45F, 3.8F));
      Cos.add("pet_fish", "Blubberfisch", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::fish, 1.4F, 4.4F));
      Cos.add("pet_mushroom", "Pilzchen", Cos.Slot.PET, Cos.Rarity.RARE, grow(PetModels::mushroom, 1.55F, 4.2F));
      Cos.add("pet_brick", "Stein-Freund", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::brickBuddy, 1.5F, 4.4F));
      Cos.add("pet_cloud", "Wölkchen", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::cloud, 1.5F, 4.8F));
      Cos.add("pet_robot", "Robo", Cos.Slot.PET, Cos.Rarity.EPIC, grow(PetModels::robot, 1.45F, 3.8F));
      Cos.add("pet_dragon", "Mini-Drache", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetModels::miniDragon);
      Cos.add("pet_unicorn", "Einhorn", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetModels::unicorn);
      Cos.add("pet_wolf", "Wolf", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::wolf);
      Cos.add("pet_retriever", "Golden Retriever", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::retriever);
      Cos.add("pet_arcticfox", "Polarfuchs", Cos.Slot.PET, Cos.Rarity.RARE, PetReal::arcticFox);
      Cos.add("pet_horse", "Pferd", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::horse);
      Cos.add("pet_deer", "Hirsch", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::deer);
      Cos.add("pet_tiger", "Tiger", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::tiger);
      Cos.add("pet_eagle", "Adler", Cos.Slot.PET, Cos.Rarity.EPIC, PetReal::eagle);
      Cos.add("pet_lion", "Löwe", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetReal::lion);
      Cos.add("pet_drake", "Drache", Cos.Slot.PET, Cos.Rarity.LEGENDARY, PetReal::drake);
   }

   static Cos.Model grow(Cos.Model var0, float var1, float var2) {
      return (var3, var4) -> {
         float var5 = Math.max(0.0F, Math.min(1.0F, var4.growth));
         float var6 = var4.preview ? 1.0F : 0.92F + (var1 - 0.92F) * var5;
         var3.push();
         var3.scale(var6);

         try {
            var0.render(var3, var4);
         } finally {
            var3.pop();
         }

         if (var4.level >= 5 && !var4.preview) {
            Pets.P var7 = new Pets.P();
            var7.t = var4.time;
            var7.lvl = var4.level;
            PetRig.sparkles(var3, var7, var2 * var6);
         }
      };
   }

   static String emotion(String var0) {
      if (var0 != null) {
         for (String var4 : EMOTIONS) {
            if (var4.equals(var0)) {
               return var4;
            }
         }
      }

      return "happy";
   }

   static Pets.P pose(Cos.A var0) {
      Pets.P var1 = new Pets.P();
      float var2 = var1.t = var0.time;
      float var3 = var1.sp = Math.max(0.0F, Math.min(1.0F, var0.speed));
      var1.emo = emotion(var0.emotion);
      var1.lookX = Math.max(-1.0F, Math.min(1.0F, var0.lookX));
      var1.lookY = Math.max(-1.0F, Math.min(1.0F, var0.lookY));
      float var4 = var2 * 11.0F;
      var1.walk = PetGeo.sin(var4) * 38.0F * var3;
      var1.breathe = PetGeo.sin(var2 * 2.2F);
      var1.stepBob = Math.abs(PetGeo.sin(var4)) * 0.45F * var3;
      var1.gr = Math.max(0.0F, Math.min(1.0F, var0.growth));
      var1.lvl = Math.max(1, Math.min(5, var0.level));
      var1.preview = var0.preview;
      var1.sy = 1.0F + var1.breathe * 0.018F;
      var1.sx = 1.0F - var1.breathe * 0.008F;
      var1.headYaw = var1.lookX * 24.0F;
      var1.headPitch = -var1.lookY * 14.0F;
      var1.earTwitch = (float)Math.pow(Math.max(0.0, Math.sin(var2 * 0.9)), 40.0) * 14.0F;
      float var5 = 18.0F;
      float var6 = 7.0F;
      var1.flapSpd = 13.0F;
      String var7 = var1.emo;
      switch (var7) {
         case "happy":
            var1.headRoll = PetGeo.sin(var2 * 1.7F) * 5.0F;
            var1.y = var1.y + Math.abs(PetGeo.sin(var2 * 3.4F)) * 0.2F;
            var5 = 26.0F;
            var6 = 10.0F;
            break;
         case "love":
            var1.rotZ = PetGeo.sin(var2 * 2.3F) * 7.0F;
            var1.headRoll = PetGeo.sin(var2 * 2.3F + 0.5F) * 10.0F;
            var5 = 22.0F;
            var6 = 5.0F;
            break;
         case "sleepy":
            var1.lie = 1.0F - Math.min(1.0F, var3 * 2.0F);
            var1.breathe = PetGeo.sin(var2 * 1.2F);
            var1.sy = 1.0F + var1.breathe * 0.035F;
            var1.sx = 1.0F - var1.breathe * 0.012F;
            var1.headPitch = var1.headPitch + 7.0F * var1.lie;
            var1.headRoll = 11.0F * var1.lie;
            var1.ear = 0.35F;
            var5 = 3.0F;
            var6 = 1.0F;
            var1.tailLift = -25.0F;
            var1.hover = 0.45F;
            var1.flapSpd = 5.0F;
            break;
         case "sad":
            var1.headPitch += 14.0F;
            var1.ear = 0.0F;
            var5 = 4.0F;
            var6 = 2.0F;
            var1.tailLift = -35.0F;
            var1.y -= 0.08F;
            var1.sy *= 0.97F;
            var1.hover = 0.7F;
            var1.flapSpd = 7.0F;
            break;
         case "excited":
            float var9 = Math.abs(PetGeo.sin(var2 * 6.8F));
            var1.y += var9 * 2.0F;
            var1.sy *= 1.0F + (var9 - 0.35F) * 0.16F;
            var1.sx *= 1.0F - (var9 - 0.35F) * 0.08F;
            var1.headRoll = PetGeo.sin(var2 * 6.8F) * 6.0F;
            var1.ear = 1.1F;
            var5 = 36.0F;
            var6 = 20.0F;
            var1.hover = 1.1F;
            var1.flapSpd = 22.0F;
            break;
         case "angry":
            var1.rootX = PetGeo.sin(var2 * 40.0F) * 0.2F;
            var1.headPitch += 7.0F;
            var1.ear = 0.45F;
            var5 = 12.0F;
            var6 = 24.0F;
            var1.sx *= 1.05F;
            var1.sy *= 0.97F;
            var1.flapSpd = 18.0F;
            break;
         case "curious":
            var1.headRoll = 18.0F + PetGeo.sin(var2 * 1.4F) * 4.0F;
            var1.headYaw = var1.headYaw + PetGeo.sin(var2 * 0.6F) * 12.0F;
            var1.headPitch -= 5.0F;
            var1.ear = 1.12F;
            var5 = 12.0F;
            var6 = 3.0F;
            var1.tailLift = 10.0F;
            break;
         case "dizzy":
            var1.rotZ = PetGeo.sin(var2 * 3.1F) * 9.0F;
            var1.rotX = PetGeo.cos(var2 * 3.1F) * 6.0F;
            var1.headRoll = PetGeo.sin(var2 * 3.1F + 1.2F) * 14.0F;
            var1.headYaw = var1.headYaw + PetGeo.cos(var2 * 3.1F) * 10.0F;
            var1.ear = 0.6F;
            var5 = 8.0F;
            var6 = 2.0F;
      }

      var1.tail = PetGeo.sin(var2 * var6) * var5;
      PetRig.gait(var1);
      var1.flap = PetGeo.sin(var2 * var1.flapSpd);
      boolean var10 = var1.is("sleepy") && var3 < 0.3F && (var2 * 0.3F % 1.0F + 1.0F) % 1.0F < 0.78F;
      var1.closed = var0.blink > 0.5F || var10;
      return var1;
   }

   static void root(G var0, Pets.P var1) {
      var0.translate(var1.rootX, var1.y + var1.stepBob, 0.0F);
      if (var1.rotZ != 0.0F) {
         var0.rotZ(var1.rotZ);
      }

      if (var1.rotX != 0.0F) {
         var0.rotX(var1.rotX);
      }

      var0.scale(var1.sx, var1.sy, var1.sx);
   }

   static void headTurn(G var0, Pets.P var1) {
      var0.rotY(var1.headYaw);
      var0.rotX(var1.headPitch);
      var0.rotZ(var1.headRoll);
   }

   static String fur(String var0) {
      return "pet_fur:" + var0;
   }

   static String head(String var0) {
      return "pet_head:" + var0;
   }

   static String col(int var0) {
      return "pet_c:" + String.format("%06X", var0 & 16777215);
   }

   static String gl(int var0) {
      return "pet_gl:" + String.format("%06X", var0 & 16777215);
   }

   static void face(G var0, Pets.P var1, String var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
      boolean var11 = var0.glow;
      int var12 = var0.color;
      boolean var13 = var2.equals("led");
      var0.color(-1).glow(var13);
      PetGeo.decal(var0, "pet_face:" + var2 + ":" + var1.emo, var3, var4, var5, var6, var7, var8, 0.0F, var10, var9, 0.0F, 0.0F, 1.0F, 1.0F, 1.035F);
      PetGeo.decal(
         var0, eyes(var2, var1), var3, var4, var5, var6, var7, var8, var1.lookX * 0.09F, var10 + var1.lookY * 0.06F, var9, 0.0F, 0.0F, 1.0F, 1.0F, 1.05F
      );
      var0.color(var12).glow(var11);
   }

   static void faceFlat(G var0, Pets.P var1, String var2, float var3, float var4, float var5, float var6, float var7) {
      boolean var8 = var0.glow;
      int var9 = var0.color;
      var0.color(-1).glow(var2.equals("led"));
      PetGeo.flat(var0, "pet_face:" + var2 + ":" + var1.emo, var3, var4, var5, var6, var7);
      PetGeo.flat(var0, eyes(var2, var1), var3 + var1.lookX * var6 * 0.05F, var4 + var1.lookY * var7 * 0.05F, var5 + 0.03F, var6, var7);
      var0.color(var9).glow(var8);
   }

   static String eyes(String var0, Pets.P var1) {
      return "pet_eyes:" + var0 + ":" + var1.emo + ":" + (var1.closed ? 1 : 0);
   }

   static void nose(G var0, int var1, float var2, float var3, float var4, float var5, float var6) {
      PetGeo.blob(var0, gl(var1), 0.0F, PetGeo.sin(var5) * var3, PetGeo.cos(var5) * var4 * 0.99F, var6 * 1.25F, var6 * 0.85F, var6 * 0.8F, 6, 4);
   }

   static void earPointy(G var0, Pets.P var1, String var2, String var3, float var4, float var5, float var6, int var7, float var8, float var9, float var10) {
      earPointy(var0, var1, var2, var3, null, var4, var5, var6, var7, var8, var9, var10);
   }

   static void earPointy(
      G var0, Pets.P var1, String var2, String var3, String var4, float var5, float var6, float var7, int var8, float var9, float var10, float var11
   ) {
      var0.push();
      var0.translate(var5, var6, var7);
      float var12 = Math.max(0.0F, 1.0F - var1.ear);
      float var13 = Math.max(0.0F, var1.ear - 1.0F) * 60.0F;
      var0.rotZ(-var8 * (var11 + var12 * 58.0F - var13 + var1.earTwitch * (var8 > 0 ? 1 : 0)));
      var0.rotX(-var12 * 22.0F);
      var0.push();
      var0.scale(1.0F, 1.0F, 0.42F);
      PetGeo.cone(var0, var2, var9, var10, 8, false);
      if (var4 != null) {
         var0.translate(0.0F, var10 * 0.6F, 0.0F);
         PetGeo.cone(var0, var4, var9 * 0.43F, var10 * 0.41F, 8, false);
      }

      var0.pop();
      if (var3 != null) {
         var0.push();
         var0.translate(0.0F, var10 * 0.08F, var9 * 0.13F);
         var0.scale(1.0F, 1.0F, 0.42F);
         PetGeo.cone(var0, var3, var9 * 0.62F, var10 * 0.76F, 7, false);
         var0.pop();
      }

      var0.pop();
   }

   static void earRound(G var0, Pets.P var1, String var2, String var3, float var4, float var5, float var6, int var7, float var8) {
      var0.push();
      var0.translate(var4, var5, var6);
      float var9 = Math.max(0.0F, 1.0F - var1.ear);
      var0.rotZ(-var7 * (15.0F + var9 * 35.0F + var1.earTwitch * (var7 > 0 ? 1 : 0)));
      var0.rotX(-var9 * 15.0F);
      PetGeo.blob(var0, var2, 0.0F, var8 * 0.55F, 0.0F, var8, var8, var8 * 0.55F, 7, 4);
      if (var3 != null) {
         PetGeo.blob(var0, var3, 0.0F, var8 * 0.5F, var8 * 0.26F, var8 * 0.62F, var8 * 0.62F, var8 * 0.33F, 5, 2);
      }

      var0.pop();
   }

   static void tail(G var0, Pets.P var1, String[] var2, float[] var3, float var4, float var5, float var6, float var7) {
      var0.push();
      var0.rotY(var1.tail * var7 + var1.tailSway);
      var0.rotX(var6 + var1.tailLift);

      for (int var8 = 0; var8 < var3.length; var8++) {
         var0.rotX(var5);
         var0.rotY(var1.tail * 0.35F * var7);
         PetGeo.blob(var0, var2[Math.min(var8, var2.length - 1)], 0.0F, 0.0F, -var4 * 0.5F, var3[var8], var3[var8], var4 * 0.62F + var3[var8] * 0.3F, 7, 4);
         var0.translate(0.0F, 0.0F, -var4 * 0.82F);
      }

      var0.pop();
   }

   static void leg(G var0, String var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      var0.push();
      var0.translate(var2, var5 - var7, var3);
      var0.rotX(var6);
      PetGeo.blob(var0, var1, 0.0F, -var5 * 0.45F, 0.0F, var4, var5 * 0.56F, var4 * 1.05F, 6, 4);
      var0.pop();
   }

   static void legs4(G var0, Pets.P var1, String var2, float var3, float var4, float var5, float var6, float var7) {
      float var8 = var1.lie * var7 * 0.72F;

      for (byte var9 = -1; var9 <= 1; var9 += 2) {
         leg(var0, var2, var9 * var3, var4, var6, var7, var9 * var1.walk - var1.lie * 72.0F, var8);
         leg(var0, var2, var9 * var3, var5, var6, var7, -var9 * var1.walk + var1.lie * 72.0F, var8);
      }
   }

   static void fx(G var0, Pets.P var1, float var2) {
      if (var1.is("dizzy")) {
         for (int var3 = 0; var3 < 3; var3++) {
            double var4 = var1.t * 3.2 + var3 * Math.PI * 2.0 / 3.0;
            PetGeo.sprite(var0, "pet_star", PetGeo.cos(var4) * 2.8F, var2 + 0.4F + PetGeo.sin(var4 * 2.0) * 0.2F, PetGeo.sin(var4) * 2.8F, 1.5F, -1);
         }
      } else if (var1.is("love")) {
         for (int var6 = 0; var6 < 2; var6++) {
            float var7 = ((var1.t * 0.55F + var6 * 0.5F) % 1.0F + 1.0F) % 1.0F;
            int var5 = (int)(255.0 * Math.sin(var7 * Math.PI));
            PetGeo.sprite(
               var0,
               "pet_heart",
               (var6 == 0 ? 1.6F : -1.8F) + PetGeo.sin(var7 * 6.0F + var6) * 0.5F,
               var2 + var7 * 3.2F,
               0.5F,
               1.2F + 0.5F * var7,
               var5 << 24 | 16777215
            );
         }
      }
   }

   static void quad4(G var0, Pets.P var1, Pets.Q4 var2) {
      float var3 = var1.gr;
      float var4 = var1.preview ? 0.9F : 0.88F + 0.46F * var3;
      float var5 = 1.0F + 1.25F * var3;
      float var6 = 1.0F + 0.4F * var3;
      float var7 = 1.0F - 0.3F * var3;
      float var8 = var2.legH * var5;
      float var9 = var8 - var2.legH;
      float var10 = var2.bodyRz * var6;
      float var11 = var2.bodyY + var9;
      float var12 = var2.bodyZ * var6;
      float var13 = var2.legZf * var6;
      float var14 = var2.legZb * var6;
      float var15 = var2.headY + var9 - var2.headRy * (1.0F - var7) * 0.9F + 0.35F * var3;
      float var16 = var2.headZ + var10 * 0.3F * var3;
      float var17 = var11 - var2.bodyRy;
      var1.stepBob = 0.0F;
      var0.push();
      var0.scale(var4);
      root(var0, var1);
      PetRig.Frame var18 = PetRig.frame(var1, var8, var13, var14, var8, var17);
      float var19 = var2.legR * (1.0F - 0.12F * var3);
      PetRig.LegStyle var20 = new PetRig.LegStyle(var3 > 0.3F ? var2.body : var2.leg, var2.leg, var2.leg, var19, var19 * 0.88F, var19 * 0.95F);
      var20.pawLen = 1.15F;
      PetRig.legs(var0, var1, var18, var20, var20, var2.legX, var13, var14, var8, var8, 0.52F, 0.5F);
      var0.push();
      var18.apply(var0);
      var0.rotZ(var1.roll);
      var0.push();
      var0.translate(0.0F, var11, var12);
      var0.scale(1.0F, 1.0F, var1.stretch);
      PetGeo.blob(var0, var2.body, 0.0F, 0.0F, 0.0F, var2.bodyRx, var2.bodyRy, var10, 10, 5);
      if (var2.bodyExtra != null) {
         var2.bodyExtra.draw(var0, var1);
      }

      if (var3 > 0.3F) {
         float var21 = Math.min(1.0F, (var3 - 0.3F) / 0.5F);
         PetGeo.blob(
            var0, var2.body, 0.0F, var2.bodyRy * 0.25F, var10 * 0.72F, var2.bodyRx * 0.62F * var21, var2.bodyRy * 0.62F * var21, var10 * 0.32F * var21, 7, 4
         );
      }

      if (var2.tail != null) {
         var0.push();
         var0.translate(0.0F, -var2.bodyRy * 0.1F + 0.2F * var3, -var10 * 0.88F);
         var0.scale(1.0F + 0.35F * var3);
         var2.tail.draw(var0, var1);
         var0.pop();
      }

      var0.pop();
      if (var3 > 0.15F) {
         float var24 = (var11 + var15) * 0.5F;
         float var22 = (var12 + var10 * 0.55F + var16) * 0.5F;
         PetGeo.blob(var0, var2.body, 0.0F, var24, var22, var2.bodyRx * 0.6F, (var15 - var11) * 0.42F, var2.bodyRx * 0.62F, 8, 4);
      }

      var0.push();
      var0.translate(0.0F, var11 + var2.bodyRy * 0.62F + 0.2F * var3, var12 + var10 * 0.5F + 0.2F * var3);
      var0.rotX(28.0F);
      PetRig.collar(var0, var1, var2.bodyRx * (0.66F + 0.05F * var3), 0.24F);
      var0.pop();
      var0.push();
      var0.translate(0.0F, var15 - var1.lie * 0.35F - var1.bob * var8 * 0.5F, var16);
      headTurn(var0, var1);
      var0.rotX(var1.nod - var1.sit * 6.0F);
      var0.scale(var7);
      String var25 = PetRig.style(var2.style, var3);
      PetGeo.blob(var0, var2.head, 0.0F, 0.0F, 0.0F, var2.headRx, var2.headRy, var2.headRz, 14, 9);
      face(var0, var1, var25, 0.0F, 0.0F, 0.0F, var2.headRx, var2.headRy, var2.headRz, var2.span, var2.faceEl);
      if (var2.nose >= 0) {
         nose(var0, var2.nose, var2.headRx, var2.headRy, var2.headRz, var2.noseEl, var2.noseR * (1.0F + 0.4F * var3));
      }

      if (var3 > 0.4F) {
         float var26 = Math.min(1.0F, (var3 - 0.4F) / 0.4F);

         for (byte var23 = -1; var23 <= 1; var23 += 2) {
            var0.push();
            var0.translate(var23 * var2.headRx * 0.9F, -var2.headRy * 0.35F, -0.2F);
            var0.rotZ(-var23 * 115);
            var0.scale(1.0F, 1.0F, 0.5F);
            PetGeo.cone(var0, var2.head, 0.7F * var26, 1.3F * var26, 5, false);
            var0.pop();
         }
      }

      if (var2.ears != null) {
         var0.push();
         var0.scale(1.0F + 0.12F * var3);
         var2.ears.draw(var0, var1);
         var0.pop();
      }

      if (var2.headExtra != null) {
         var2.headExtra.draw(var0, var1);
      }

      var0.pop();
      fx(var0, var1, var15 + var2.headRy * var7 + 1.2F);
      var0.pop();
      PetRig.sparkles(var0, var1, Math.max(var2.bodyRx, var10) + 1.2F);
      var0.pop();
   }

   static final class P {
      float t;
      float sp;
      float lookX;
      float lookY;
      String emo;
      boolean closed;
      float y;
      float rootX;
      float rotZ;
      float rotX;
      float sx = 1.0F;
      float sy = 1.0F;
      float headYaw;
      float headPitch;
      float headRoll;
      float ear = 1.0F;
      float earTwitch;
      float tail;
      float tailLift;
      float walk;
      float lie;
      float breathe;
      float hover = 1.0F;
      float flap;
      float flapSpd;
      float gr;
      int lvl = 3;
      boolean preview;
      float walkW;
      float runW;
      float idle;
      float cw;
      float cr;
      float bob;
      float roll;
      float pitch;
      float stretch = 1.0F;
      float nod;
      float tailSway;
      float waddle;
      float waddleBob;
      float sit;
      float bow;
      float sniff;
      float crouch;
      float stepBob;

      boolean is(String var1) {
         return this.emo.equals(var1);
      }
   }

   interface Part {
      void draw(G var1, Pets.P var2);
   }

   static final class Q4 {
      String body;
      String head;
      String leg;
      String style = "std";
      float bodyY = 2.55F;
      float bodyRx = 2.2F;
      float bodyRy = 1.95F;
      float bodyRz = 2.45F;
      float bodyZ = -0.35F;
      float headY = 6.1F;
      float headZ = 0.95F;
      float headRx = 3.35F;
      float headRy = 2.95F;
      float headRz = 2.8F;
      float legR = 0.72F;
      float legH = 1.15F;
      float legX = 1.2F;
      float legZf = 1.05F;
      float legZb = -1.75F;
      float span = 1.0F;
      float faceEl = 0.0F;
      int nose = -1;
      float noseR = 0.3F;
      float noseEl = -0.14F;
      Pets.Part ears;
      Pets.Part tail;
      Pets.Part headExtra;
      Pets.Part bodyExtra;
   }
}
