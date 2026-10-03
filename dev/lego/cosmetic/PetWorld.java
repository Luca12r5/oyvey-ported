package dev.lego.cosmetic;

import dev.lego.core.Mc;
import dev.lego.emote.Bubbles;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PetWorld {
   private static double x;
   private static double y;
   private static double z;
   private static double px;
   private static double py;
   private static double pz;
   private static float yaw;
   private static float lastYaw;
   private static boolean placed;
   private static float speed;
   private static String emotion = "happy";
   private static String shownEmotion = "happy";
   private static long emotionUntil;
   private static long bubbleAt;
   private static long lastActive = System.currentTimeMillis();
   private static long nextBlink;
   private static long blinkAt;
   private static long curiousAt;
   private static float lastHealth = -1.0F;
   private static long nextCurious = System.currentTimeMillis() + 20000L;
   private static float spinFrom;
   private static int lastLevel = -1;
   private static final Map<String, Identifier> BUBBLES = new HashMap<>();

   private PetWorld() {
   }

   static float growth() {
      LocalPlayer var0 = Mc.player();
      int var1 = var0 == null ? 0 : var0.experienceLevel;
      return Math.max(0.0F, Math.min(1.0F, var1 / 40.0F));
   }

   static int level() {
      LocalPlayer var0 = Mc.player();
      int var1 = var0 == null ? 0 : var0.experienceLevel;
      return var1 >= 35 ? 5 : (var1 >= 25 ? 4 : (var1 >= 15 ? 3 : (var1 >= 5 ? 2 : 1)));
   }

   public static void onHit() {
      set("excited", 1800L);
   }

   public static void onKill() {
      set("excited", 3500L);
   }

   private static void set(String var0, long var1) {
      emotion = var0;
      emotionUntil = System.currentTimeMillis() + var1;
   }

   public static void tick() {
      LocalPlayer var0 = Mc.player();
      Cos.Item var1 = Cos.equipped(Cos.Slot.PET);
      if (var0 != null && var1 != null) {
         long var2 = System.currentTimeMillis();
         px = x;
         py = y;
         pz = z;
         lastYaw = yaw;
         double var4 = Math.toRadians(Mc.bodyYaw(var0));
         double var6 = -Math.sin(var4);
         double var8 = Math.cos(var4);
         double var10 = -Math.cos(var4);
         double var12 = -Math.sin(var4);
         double var14 = 0.85 + growth() * 0.45;
         double var16 = Mc.x(var0) + var10 * var14 - var6 * (0.55 + growth() * 0.4);
         double var18 = Mc.z(var0) + var12 * var14 - var8 * (0.55 + growth() * 0.4);
         int var20 = level();
         if (lastLevel > 0 && var20 > lastLevel) {
            set("excited", 4000L);
            Toasts.show("paw", "Dein Haustier ist gewachsen!", "Stufe " + var20 + " von 5", -15043, 4000L);
         }

         lastLevel = var20;
         double var21 = ground(var16, Mc.y(var0) + 0.6, var18, Mc.y(var0));
         if (!placed || (x - var16) * (x - var16) + (z - var18) * (z - var18) > 144.0 || Math.abs(y - var21) > 8.0) {
            px = var16;
            x = var16;
            py = var21;
            y = var21;
            pz = var18;
            z = var18;
            yaw = lastYaw = Mc.yaw(var0);
            placed = true;
         }

         double var23 = var16 - x;
         double var25 = var18 - z;
         double var27 = Math.sqrt(var23 * var23 + var25 * var25);
         double var29 = var27 > 0.25 ? Math.min(0.35, 0.08 + var27 * 0.06) : 0.0;
         x += var23 * var29;
         z += var25 * var29;
         y = y + (var21 - y) * 0.35;
         double var31 = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz));
         speed = speed + ((float)Math.min(1.0, var31 * 5.0) - speed) * 0.3F;
         float var33;
         if (var31 > 0.02) {
            var33 = (float)Math.toDegrees(Math.atan2(-(x - px), z - pz));
         } else {
            var33 = (float)Math.toDegrees(Math.atan2(-(Mc.x(var0) - x), Mc.z(var0) - z));
         }

         float var34 = wrap(var33 - yaw);
         yaw += var34 * 0.25F;
         float var35 = Mc.health(var0);
         if (lastHealth >= 0.0F && var35 < lastHealth - 0.01F) {
            set(var0.getLastHurtByMob() != null ? "angry" : "sad", 3000L);
         }

         lastHealth = var35;
         double var36 = Math.sqrt(Math.pow(Mc.x(var0) - Mc.lastX(var0), 2.0) + Math.pow(Mc.z(var0) - Mc.lastZ(var0), 2.0));
         if (var36 > 0.01 || Mc.isSneaking(var0) || Mc.mouseDown(0)) {
            lastActive = var2;
         }

         if (var2 > emotionUntil) {
            String var38;
            if (var35 <= Mc.maxHealth(var0) * 0.3F) {
               var38 = "sad";
            } else if (var0.fallDistance > 4.0) {
               var38 = "dizzy";
            } else if (Mc.isSneaking(var0) && var31 < 0.05) {
               var38 = "love";
            } else if (var2 - lastActive > 25000L) {
               var38 = "sleepy";
            } else if (var0.isSprinting() && var36 > 0.2) {
               var38 = "excited";
            } else if (var2 > nextCurious) {
               var38 = "curious";
               emotionUntil = var2 + 3500L;
               nextCurious = var2 + 25000L + (long)(Math.random() * 30000.0);
            } else {
               var38 = "happy";
            }

            emotion = var38;
         }

         if (!emotion.equals(shownEmotion)) {
            shownEmotion = emotion;
            bubbleAt = var2;
         }

         if (var2 > nextBlink) {
            blinkAt = var2;
            nextBlink = var2 + 2200L + (long)(Math.random() * 3200.0);
         }
      } else {
         placed = false;
      }
   }

   private static float wrap(float var0) {
      var0 %= 360.0F;
      if (var0 > 180.0F) {
         var0 -= 360.0F;
      }

      if (var0 < -180.0F) {
         var0 += 360.0F;
      }

      return var0;
   }

   private static double ground(double var0, double var2, double var4, double var6) {
      ClientLevel var8 = Mc.world();
      if (var8 == null) {
         return var6;
      } else {
         MutableBlockPos var9 = new MutableBlockPos();
         int var10 = (int)Math.floor(var0);
         int var11 = (int)Math.floor(var4);

         for (int var12 = (int)Math.floor(var2); var12 > var2 - 6.0; var12--) {
            var9.set(var10, var12, var11);
            BlockState var13 = var8.getBlockState(var9);
            VoxelShape var14 = var13.getCollisionShape(var8, var9);
            if (!var14.isEmpty()) {
               double var15 = var12 + var14.bounds().maxY;
               if (var15 <= var2 + 0.01) {
                  return var15;
               }
            }
         }

         return var6;
      }
   }

   public static void render(float var0) {
      Cos.Item var1 = Cos.equipped(Cos.Slot.PET);
      if (var1 != null && placed && Mc.player() != null) {
         double var2 = px + (x - px) * var0;
         double var4 = py + (y - py) * var0;
         double var6 = pz + (z - pz) * var0;
         float var8 = lastYaw + wrap(yaw - lastYaw) * var0;
         long var9 = System.currentTimeMillis();
         Cos.A var11 = new Cos.A();
         var11.time = (float)(var9 % 3600000L) / 1000.0F;
         var11.speed = speed;
         var11.growth = growth();
         var11.level = level();
         var11.emotion = shownEmotion;
         float var12 = (float)(var9 - blinkAt) / 140.0F;
         var11.blink = var12 < 1.0F ? 1.0F - Math.abs(var12 * 2.0F - 1.0F) : 0.0F;
         double var13 = R3.camX() - var2;
         double var15 = R3.camZ() - var6;
         double var17 = R3.camY() - (var4 + 0.4);
         double var19 = Math.toRadians(var8);
         double var21 = var13 * -Math.sin(var19) + var15 * Math.cos(var19);
         double var23 = var13 * -Math.cos(var19) + var15 * -Math.sin(var19);
         double var25 = Math.atan2(var23, Math.max(0.1, var21));
         var11.lookX = (float)Math.max(-1.0, Math.min(1.0, var25 / 1.2));
         var11.lookY = (float)Math.max(-1.0, Math.min(1.0, Math.atan2(var17, Math.sqrt(var13 * var13 + var15 * var15)) / 1.0));
         int var27 = CosRender.light(var2, var4 + 0.3, var6);
         CosRender.drawWorld(var1.model, var11, var2, var4, var6, var8, 0.0F, 0.0F, 0.95F, var27);
         long var28 = var9 - bubbleAt;
         String var30 = bubbleFor(shownEmotion);
         if (var30 != null && var28 < 2600L) {
            Identifier var31 = BUBBLES.get(var30);
            if (var31 == null) {
               var31 = Identifier.fromNamespaceAndPath("legoclient", "pet_bubble/" + var30);

               try {
                  Tx.register(var31, Bubbles.paint(var30, 128), 128, 128, true);
               } catch (Throwable var38) {
               }

               BUBBLES.put(var30, var31);
            }

            float var32 = Math.min(1.0F, (float)var28 / 250.0F);
            float var33 = var28 > 2200L ? 1.0F - (float)(var28 - 2200L) / 400.0F : 1.0F;
            double var34 = 1.0 + 0.25 * Math.sin(Math.min(1.0, var28 / 300.0) * Math.PI);
            double var36 = Pets.height(var1.id, var11.growth) / 16.0 * 0.95 + 0.35 + Math.sin(var9 / 300.0) * 0.03;
            R3.billboard(var31, var2, var4 + var36, var6, 0.42 * var32 * var34, R3.argb(16777215, Math.max(0.0F, var33)));
         }
      }
   }

   private static String bubbleFor(String var0) {
      switch (var0) {
         case "love":
            return "heart";
         case "sleepy":
            return "sleep";
         case "sad":
            return "cry";
         case "curious":
            return "think";
         case "excited":
            return "laugh";
         case "angry":
            return "fire";
         case "dizzy":
            return "zen";
         default:
            return null;
      }
   }
}
