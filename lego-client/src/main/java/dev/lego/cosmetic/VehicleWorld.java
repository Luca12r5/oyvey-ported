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
   private static float kmh;

   private VehicleWorld() {
   }

   public static Cos.Item active() {
      Cos.Item it = Cos.equipped(Cos.Slot.VEHICLE);
      if (it == null) {
         return null;
      } else {
         LocalPlayer p = Mc.player();
         return p != null && !p.isPassenger() && !p.isInWater() && !p.isFallFlying() && !p.isSleeping() && !Mc.isSpectator(p) ? it : null;
      }
   }

   public static float speed() {
      return active() == null ? 0.0F : speed;
   }

   public static int kmh() {
      return active() == null ? 0 : Math.round(kmh);
   }

   static String displayTex() {
      return "veh_display@" + Math.min(199, kmh());
   }

   public static void tick() {
      LocalPlayer p = Mc.player();
      if (p != null) {
         double dx = Mc.x(p) - lx;
         double dz = Mc.z(p) - lz;
         lx = Mc.x(p);
         lz = Mc.z(p);
         double d = Math.sqrt(dx * dx + dz * dz);
         if (d > 3.0) {
            d = 0.0;
         }

         lastWheel = wheel;
         wheel += (float)(d * 16.0 / (Math.PI * 6) * 360.0);
         speed = speed + ((float)Math.min(1.0, d / 0.28) - speed) * 0.25F;
         kmh = kmh + ((float)(d * 20.0 * 3.6) - kmh) * 0.35F;
         float by = Mc.bodyYaw(p);
         float dy = by - lastBodyYaw;

         while (dy > 180.0F) {
            dy -= 360.0F;
         }

         while (dy < -180.0F) {
            dy += 360.0F;
         }

         lastBodyYaw = by;
         lean = lean + (Math.max(-1.0F, Math.min(1.0F, dy / 9.0F)) * Math.min(1.0F, speed * 2.0F) - lean) * 0.2F;
         VehicleFx.tick(active(), p, speed, lean);
      }
   }

   public static void pose(ModelPart head, ModelPart body, ModelPart rArm, ModelPart lArm, ModelPart rLeg, ModelPart lLeg) {
      Cos.Item it = active();
      if (it != null) {
         float t = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
         float lift = Vehicles.lift(it.id, t);
         Vehicles.Stance st = Vehicles.stance(it.id);
         boolean emote = Emotes.active();
         if (!emote) {
            float hy = Vehicles.handY(it.id);
            float hz = Vehicles.handZ(it.id);
            boolean hands = !Float.isNaN(hy) && !Float.isNaN(hz);
            float armPitch = hands ? (float)(-Math.atan2(hz, lift + 22.0F - hy)) : -0.9F;
            float hx = hands ? Vehicles.handX(it.id) : 5.0F;
            float armRoll = hands ? (float)Math.atan2(5.0F - (Float.isNaN(hx) ? 5.0F : hx), 11.0) : 0.0F;
            switch (st) {
               case SIT:
                  rLeg.xRot = -1.45F;
                  rLeg.yRot = 0.14F;
                  rLeg.zRot = 0.05F;
                  lLeg.xRot = -1.45F;
                  lLeg.yRot = -0.14F;
                  lLeg.zRot = -0.05F;
                  rArm.xRot = armPitch + lean * 0.2F;
                  rArm.yRot = -0.15F;
                  rArm.zRot = 0.0F;
                  lArm.xRot = armPitch - lean * 0.2F;
                  lArm.yRot = 0.15F;
                  lArm.zRot = 0.0F;
                  break;
               case PEDAL:
                  double crank = Math.toRadians(Vehicles.pedalAngle(wheel));
                  rLeg.xRot = -1.05F + (float)Math.sin(crank) * 0.38F;
                  rLeg.yRot = 0.05F;
                  rLeg.zRot = 0.0F;
                  lLeg.xRot = -1.05F - (float)Math.sin(crank) * 0.38F;
                  lLeg.yRot = -0.05F;
                  lLeg.zRot = 0.0F;
                  rArm.xRot = armPitch;
                  rArm.yRot = 0.0F;
                  rArm.zRot = -armRoll;
                  lArm.xRot = armPitch;
                  lArm.yRot = 0.0F;
                  lArm.zRot = armRoll;
                  break;
               case RIDE:
                  rLeg.xRot = -1.25F;
                  rLeg.yRot = 0.22F;
                  rLeg.zRot = 0.08F;
                  lLeg.xRot = -1.25F;
                  lLeg.yRot = -0.22F;
                  lLeg.zRot = -0.08F;
                  rArm.xRot = armPitch;
                  rArm.yRot = 0.0F;
                  rArm.zRot = -armRoll + lean * 0.05F;
                  lArm.xRot = armPitch;
                  lArm.yRot = 0.0F;
                  lArm.zRot = armRoll + lean * 0.05F;
                  body.zRot = -lean * 0.12F;
                  break;
               case KICK:
                  double ph = Math.toRadians(wheel * 0.35);
                  float kick = speed > 0.05F ? (float)Math.sin(ph) * 0.75F * Math.min(1.0F, speed * 2.0F) : 0.0F;
                  rLeg.xRot = 0.05F;
                  rLeg.yRot = 0.03F;
                  rLeg.zRot = 0.03F;
                  lLeg.xRot = kick;
                  lLeg.yRot = -0.05F;
                  lLeg.zRot = -0.06F;
                  rArm.xRot = armPitch;
                  rArm.yRot = 0.0F;
                  rArm.zRot = -armRoll;
                  lArm.xRot = armPitch;
                  lArm.yRot = 0.0F;
                  lArm.zRot = armRoll;
                  break;
               case HOLD:
                  rArm.xRot = armPitch;
                  rArm.yRot = 0.0F;
                  rArm.zRot = -armRoll;
                  lArm.xRot = armPitch;
                  lArm.yRot = 0.0F;
                  lArm.zRot = armRoll;
               default:
                  rLeg.xRot = 0.08F;
                  rLeg.yRot = 0.05F;
                  rLeg.zRot = 0.04F + lean * 0.05F;
                  lLeg.xRot = -0.12F;
                  lLeg.yRot = -0.05F;
                  lLeg.zRot = -0.04F + lean * 0.05F;
                  body.zRot = -lean * 0.06F;
            }
         }

         for (ModelPart part : new ModelPart[]{head, body, rArm, lArm, rLeg, lLeg}) {
            part.y -= lift;
         }
      }
   }

   public static void render(float td) {
      Cos.Item it = active();
      LocalPlayer p = Mc.player();
      if (it != null && p != null) {
         double[] pos = Mc.lerpPos(p, td);
         float by = Mc.lastBodyYaw(p) + wrap(Mc.bodyYaw(p) - Mc.lastBodyYaw(p)) * td;
         Cos.A a = new Cos.A();
         a.time = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
         a.move = speed;
         a.wheel = lastWheel + (wheel - lastWheel) * td;
         a.lean = lean;
         int light = CosRender.light(pos[0], pos[1] + 0.5, pos[2]);
         float pitch = Vehicles.stance(it.id) == Vehicles.Stance.SIT ? VehicleFx.pitch() : 0.0F;
         CosRender.drawWorld(it.model, a, pos[0], pos[1], pos[2], by, pitch, 0.0F, 1.0F, light);
         VehicleFx.render(pos[0], pos[1], pos[2], by, a.time, light, speed);
      } else {
         VehicleFx.tick(null, null, 0.0F, 0.0F);
      }
   }

   private static float wrap(float a) {
      a %= 360.0F;
      if (a > 180.0F) {
         a -= 360.0F;
      }

      if (a < -180.0F) {
         a += 360.0F;
      }

      return a;
   }
}
