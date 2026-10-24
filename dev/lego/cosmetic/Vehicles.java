package dev.lego.cosmetic;

public final class Vehicles {
   private Vehicles() {
   }

   static void registerAll() {
      Cos.add("veh_skate", "Skateboard", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehBoard::skateboard);
      Cos.add("veh_hover", "Hoverboard", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehBoard::hoverboard);
      Cos.add("veh_kickscooter", "Tretroller", Cos.Slot.VEHICLE, Cos.Rarity.COMMON, VehRide::kickScooter);
      Cos.add("veh_scooter", "E-Scooter", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehRide::eScooter);
      Cos.add("veh_bike", "Fahrrad", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehRide::bike);
      Cos.add("veh_moped", "Motorroller", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehRide::moped);
      Cos.add("veh_moto", "Motorrad", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehRide::motorbike);
      Cos.add("veh_car", "Sportwagen", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (var0, var1) -> VehCar.car(var0, var1, 14885931, 15921906));
      Cos.add("veh_car_blue", "Sportwagen Blau", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (var0, var1) -> VehCar.car(var0, var1, 2059263, 15921906));
      Cos.add("veh_kart", "Go-Kart", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehCar::kart);
      Cos.add("veh_ufo", "UFO", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehBoard::ufo);
      Cos.add("veh_cloud", "Wolke", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehBoard::cloud);
   }

   public static Vehicles.Stance stance(String var0) {
      switch (var0) {
         case "veh_scooter":
            return Vehicles.Stance.HOLD;
         case "veh_kickscooter":
            return Vehicles.Stance.KICK;
         case "veh_car":
         case "veh_car_blue":
         case "veh_kart":
         case "xm_veh_sleigh":
            return Vehicles.Stance.SIT;
         case "veh_bike":
            return Vehicles.Stance.PEDAL;
         case "veh_moped":
         case "veh_moto":
            return Vehicles.Stance.RIDE;
         default:
            return Vehicles.Stance.STAND;
      }
   }

   public static float lift(String var0, float var1) {
      switch (var0) {
         case "veh_skate":
            return 3.05F;
         case "veh_hover":
            return 5.1F + hoverBob(var1);
         case "veh_kickscooter":
            return 1.95F;
         case "veh_scooter":
            return 2.85F;
         case "veh_bike":
            return 1.0F;
         case "veh_moped":
            return 0.6000004F;
         case "veh_moto":
            return 1.1999998F;
         case "veh_car":
         case "veh_car_blue":
            return -6.5F;
         case "veh_kart":
            return -8.4F;
         case "xm_veh_sleigh":
            return -4.0F;
         case "veh_ufo":
            return 6.4F + hoverBob(var1) * 1.4F;
         case "veh_cloud":
            return 6.0F + cloudBob(var1);
         default:
            return 0.0F;
      }
   }

   public static float seatZ(String var0) {
      return 0.0F;
   }

   public static float handY(String var0) {
      float[] var1 = hands(var0);
      return var1 == null ? Float.NaN : var1[1];
   }

   public static float handZ(String var0) {
      float[] var1 = hands(var0);
      return var1 == null ? Float.NaN : var1[2];
   }

   public static float handX(String var0) {
      float[] var1 = hands(var0);
      return var1 == null ? Float.NaN : var1[0];
   }

   private static float[] hands(String var0) {
      switch (var0) {
         case "veh_scooter":
            return VehRide.ESC_HAND;
         case "veh_kickscooter":
            return VehRide.KICK_HAND;
         case "veh_bike":
            return VehRide.BIKE_HAND;
         case "veh_moped":
            return VehRide.MOPED_HAND;
         case "veh_moto":
            return VehRide.MOTO_HAND;
         case "veh_car":
         case "veh_car_blue":
            return VehCar.CAR_HAND;
         case "veh_kart":
            return VehCar.KART_HAND;
         default:
            return null;
      }
   }

   public static float pedalAngle(float var0) {
      return var0 * 0.45F;
   }

   public static float[][] pedals(String var0, float var1) {
      switch (var0) {
         case "veh_bike":
            double var4 = Math.toRadians(pedalAngle(var1));
            float var6 = 2.6F;
            float var7 = VehRide.BB[1];
            float var8 = VehRide.BB[2];
            return new float[][]{
               {2.7F, var7 - var6 * (float)Math.cos(var4), var8 - var6 * (float)Math.sin(var4)},
               {-2.7F, var7 + var6 * (float)Math.cos(var4), var8 + var6 * (float)Math.sin(var4)}
            };
         case "veh_moped":
            return new float[][]{{2.2F, 3.6F, 4.5F}, {-2.2F, 3.6F, 4.5F}};
         case "veh_moto":
            return new float[][]{{3.6F, 6.2F, -3.2F}, {-3.6F, 6.2F, -3.2F}};
         default:
            return null;
      }
   }

   static float hoverBob(float var0) {
      return (float)Math.sin(var0 * 2.4) * 0.45F;
   }

   static float cloudBob(float var0) {
      return (float)Math.sin(var0 * 1.6) * 0.6F;
   }

   static CTex.T tex(String var0) {
      return VehTex.tex(var0);
   }

   public static enum Stance {
      STAND,
      HOLD,
      SIT,
      KICK,
      PEDAL,
      RIDE;
   }
}
