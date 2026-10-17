package dev.lego.cosmetic;

import dev.lego.core.Mc;
import dev.lego.emote.Emotes;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;

public final class VehicleWorld {
   private static float wheel;
   private static float lastWheel;
   private static float lean;
   private static float speed;
   private static float lastBodyYaw;
   private static double lx;
   private static double lz;

   private VehicleWorld() {
   }

   public static Cos.Item active() {
      Cos.Item var0 = Cos.equipped(Cos.Slot.VEHICLE);
      if (var0 == null) {
         return null;
      } else {
         LocalPlayer var1 = Mc.player();
         return var1 != null && !var1.isPassenger() && !var1.isInWater() && !var1.isFallFlying() && !var1.isSleeping() && !Mc.isSpectator(var1) ? var0 : null;
      }
   }

   public static float speed() {
      return active() == null ? 0.0F : speed;
   }

   public static void tick() {
      LocalPlayer var0 = Mc.player();
      if (var0 != null) {
         double var1 = Mc.x(var0) - lx;
         double var3 = Mc.z(var0) - lz;
         lx = Mc.x(var0);
         lz = Mc.z(var0);
         double var5 = Math.sqrt(var1 * var1 + var3 * var3);
         if (var5 > 3.0) {
            var5 = 0.0;
         }

         lastWheel = wheel;
         wheel += (float)(var5 * 16.0 / (Math.PI * 6) * 360.0);
         speed = speed + ((float)Math.min(1.0, var5 / 0.28) - speed) * 0.25F;
         float var7 = Mc.bodyYaw(var0);
         float var8 = var7 - lastBodyYaw;

         while (var8 > 180.0F) {
            var8 -= 360.0F;
         }

         while (var8 < -180.0F) {
            var8 += 360.0F;
         }

         lastBodyYaw = var7;
         lean = lean + (Math.max(-1.0F, Math.min(1.0F, var8 / 9.0F)) * Math.min(1.0F, speed * 2.0F) - lean) * 0.2F;
         VehicleFx.tick(active(), var0, speed, lean);
      }
   }

   public static void pose(ModelPart var0, ModelPart var1, ModelPart var2, ModelPart var3, ModelPart var4, ModelPart var5) {
      Cos.Item var6 = active();
      if (var6 != null) {
         float var7 = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
         float var8 = Vehicles.lift(var6.id, var7);
         Vehicles.Stance var9 = Vehicles.stance(var6.id);
         boolean var10 = Emotes.active();
         if (!var10) {
            float var11 = Vehicles.handY(var6.id);
            float var12 = Vehicles.handZ(var6.id);
            boolean var13 = !Float.isNaN(var11) && !Float.isNaN(var12);
            float var14 = var13 ? (float)(-Math.atan2(var12, var8 + 22.0F - var11)) : -0.9F;
            float var15 = var13 ? Vehicles.handX(var6.id) : 5.0F;
            float var16 = var13 ? (float)Math.atan2(5.0F - (Float.isNaN(var15) ? 5.0F : var15), 11.0) : 0.0F;
            switch (var9) {
               case SIT:
                  var4.xRot = -1.45F;
                  var4.yRot = 0.14F;
                  var4.zRot = 0.05F;
                  var5.xRot = -1.45F;
                  var5.yRot = -0.14F;
                  var5.zRot = -0.05F;
                  var2.xRot = var14 + lean * 0.2F;
                  var2.yRot = -0.15F;
                  var2.zRot = 0.0F;
                  var3.xRot = var14 - lean * 0.2F;
                  var3.yRot = 0.15F;
                  var3.zRot = 0.0F;
                  break;
               case PEDAL:
                  double var24 = Math.toRadians(Vehicles.pedalAngle(wheel));
                  var4.xRot = -1.05F + (float)Math.sin(var24) * 0.38F;
                  var4.yRot = 0.05F;
                  var4.zRot = 0.0F;
                  var5.xRot = -1.05F - (float)Math.sin(var24) * 0.38F;
                  var5.yRot = -0.05F;
                  var5.zRot = 0.0F;
                  var2.xRot = var14;
                  var2.yRot = 0.0F;
                  var2.zRot = -var16;
                  var3.xRot = var14;
                  var3.yRot = 0.0F;
                  var3.zRot = var16;
                  break;
               case RIDE:
                  var4.xRot = -1.25F;
                  var4.yRot = 0.22F;
                  var4.zRot = 0.08F;
                  var5.xRot = -1.25F;
                  var5.yRot = -0.22F;
                  var5.zRot = -0.08F;
                  var2.xRot = var14;
                  var2.yRot = 0.0F;
                  var2.zRot = -var16 + lean * 0.05F;
                  var3.xRot = var14;
                  var3.yRot = 0.0F;
                  var3.zRot = var16 + lean * 0.05F;
                  var1.zRot = -lean * 0.12F;
                  break;
               case KICK:
                  double var17 = Math.toRadians(wheel * 0.35);
                  float var19 = speed > 0.05F ? (float)Math.sin(var17) * 0.75F * Math.min(1.0F, speed * 2.0F) : 0.0F;
                  var4.xRot = 0.05F;
                  var4.yRot = 0.03F;
                  var4.zRot = 0.03F;
                  var5.xRot = var19;
                  var5.yRot = -0.05F;
                  var5.zRot = -0.06F;
                  var2.xRot = var14;
                  var2.yRot = 0.0F;
                  var2.zRot = -var16;
                  var3.xRot = var14;
                  var3.yRot = 0.0F;
                  var3.zRot = var16;
                  break;
               case HOLD:
                  var2.xRot = var14;
                  var2.yRot = 0.0F;
                  var2.zRot = -var16;
                  var3.xRot = var14;
                  var3.yRot = 0.0F;
                  var3.zRot = var16;
               default:
                  var4.xRot = 0.08F;
                  var4.yRot = 0.05F;
                  var4.zRot = 0.04F + lean * 0.05F;
                  var5.xRot = -0.12F;
                  var5.yRot = -0.05F;
                  var5.zRot = -0.04F + lean * 0.05F;
                  var1.zRot = -lean * 0.06F;
            }
         }

         for (ModelPart var23 : new ModelPart[]{var0, var1, var2, var3, var4, var5}) {
            var23.y -= var8;
         }
      }
   }

   public static void render(float var0) {
      Cos.Item var1 = active();
      LocalPlayer var2 = Mc.player();
      if (var1 != null && var2 != null) {
         double[] var3 = Mc.lerpPos(var2, var0);
         float var4 = Mc.lastBodyYaw(var2) + wrap(Mc.bodyYaw(var2) - Mc.lastBodyYaw(var2)) * var0;
         Cos.A var5 = new Cos.A();
         var5.time = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
         var5.move = speed;
         var5.wheel = lastWheel + (wheel - lastWheel) * var0;
         var5.lean = lean;
         int var6 = CosRender.light(var3[0], var3[1] + 0.5, var3[2]);
         float var7 = Vehicles.stance(var1.id) == Vehicles.Stance.SIT ? VehicleFx.pitch() : 0.0F;
         CosRender.drawWorld(var1.model, var5, var3[0], var3[1], var3[2], var4, var7, 0.0F, 1.0F, var6);
         VehicleFx.render(var3[0], var3[1], var3[2], var4, var5.time, var6, speed);
      } else {
         VehicleFx.tick(null, null, 0.0F, 0.0F);
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
}
