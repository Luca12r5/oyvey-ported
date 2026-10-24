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
      Cos.add("veh_car", "Sportwagen", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 14885931, 15921906));
      Cos.add("veh_car_blue", "Sportwagen Blau", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 2059263, 15921906));
      Cos.add("veh_kart", "Go-Kart", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehCar::kart);
      Cos.add("veh_ufo", "UFO", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehBoard::ufo);
      Cos.add("veh_cloud", "Wolke", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehBoard::cloud);
      Cos.add("veh_car_black", "Sportwagen Schwarz", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 1710622, 15263976));
      Cos.add("veh_car_white", "Sportwagen Weiß", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 15921906, 1710622));
      Cos.add("veh_car_yellow", "Sportwagen Gelb", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 16764163, 1710622));
      Cos.add("veh_car_green", "Sportwagen Grün", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 3122572, 15921906));
      Cos.add("veh_car_pink", "Sportwagen Pink", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 16738740, 15921906));
      Cos.add("veh_police", "Polizeiwagen", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::police);
      Cos.add("veh_taxi", "Taxi", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::taxi);
      Cos.add("veh_bobby", "Bobby Car", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehExtra::bobby);
      Cos.add("veh_monster", "Monster Truck", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::monster);
      Cos.add("veh_f1", "Formel 1", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::f1);
      Cos.add("veh_quad", "Quad", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::quad);
      Cos.add("veh_limo", "Limousine", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::limo);
      Cos.add("veh_longboard", "Longboard", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehExtra::longboard);
      Cos.add("veh_surfboard", "Surfbrett", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::surfboard);
      Cos.add("veh_carpet", "Fliegender Teppich", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::carpet);
      Cos.add("veh_broom", "Hexenbesen", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::broom);
      Cos.add("veh_segway", "Segway", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::segway);
      Cos.add("veh_snowmobile", "Schneemobil", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::snowmobile);
      Cos.add("veh_dreirad", "Dreirad", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehExtra::dreirad);
      Cos.add("veh_inline", "Inline Skates", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::inline);
      Cos.add("veh_einrad", "Einrad", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehExtra::einrad);
      Cos.add("veh_rennrad", "Rennrad", Cos.Slot.VEHICLE, Cos.Rarity.EPIC, VehExtra::rennrad);
      Cos.add("veh_bmx", "BMX", Cos.Slot.VEHICLE, Cos.Rarity.RARE, VehExtra::bmx);
      Cos.add("veh_cabrio", "Cabrio", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::cabrio);
      Cos.add("veh_hotrod", "Hot Rod", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::hotrod);
      Cos.add("veh_lambo", "Lamborghini", Cos.Slot.VEHICLE, Cos.Rarity.LEGENDARY, VehExtra::lambo);
   }

   public static Vehicles.Stance stance(String id) {
      switch (id) {
         case "veh_scooter":
         case "veh_segway":
            return Vehicles.Stance.HOLD;
         case "veh_kickscooter":
            return Vehicles.Stance.KICK;
         case "veh_car":
         case "veh_car_blue":
         case "veh_car_black":
         case "veh_car_white":
         case "veh_car_yellow":
         case "veh_car_green":
         case "veh_car_pink":
         case "veh_kart":
         case "xm_veh_sleigh":
         case "veh_police":
         case "veh_taxi":
         case "veh_bobby":
         case "veh_monster":
         case "veh_f1":
         case "veh_limo":
         case "veh_cabrio":
         case "veh_hotrod":
         case "veh_lambo":
            return Vehicles.Stance.SIT;
         case "veh_bike":
         case "veh_rennrad":
         case "veh_bmx":
         case "veh_dreirad":
         case "veh_einrad":
            return Vehicles.Stance.PEDAL;
         case "veh_moped":
         case "veh_moto":
         case "veh_quad":
         case "veh_snowmobile":
            return Vehicles.Stance.RIDE;
         default:
            return Vehicles.Stance.STAND;
      }
   }

   public static float lift(String id, float time) {
      switch (id) {
         case "veh_skate":
            return 3.05F;
         case "veh_hover":
            return 5.1F + hoverBob(time);
         case "veh_kickscooter":
            return 1.95F;
         case "veh_scooter":
            return 2.85F;
         case "veh_bike":
         case "veh_rennrad":
            return 1.0F;
         case "veh_bmx":
            return -1.6999998F;
         case "veh_moped":
            return 0.6000004F;
         case "veh_moto":
            return 1.1999998F;
         case "veh_car":
         case "veh_car_blue":
         case "veh_car_black":
         case "veh_car_white":
         case "veh_car_yellow":
         case "veh_car_green":
         case "veh_car_pink":
         case "veh_police":
         case "veh_taxi":
         case "veh_cabrio":
         case "veh_hotrod":
            return -6.5F;
         case "veh_kart":
         case "veh_f1":
            return -8.4F;
         case "xm_veh_sleigh":
            return -4.0F;
         case "veh_ufo":
            return 6.4F + hoverBob(time) * 1.4F;
         case "veh_cloud":
            return 6.0F + cloudBob(time);
         case "veh_bobby":
            return -8.8F;
         case "veh_monster":
            return -3.0F;
         case "veh_quad":
            return -2.0F;
         case "veh_limo":
            return -6.5F;
         case "veh_lambo":
            return -6.5F;
         case "veh_snowmobile":
            return -2.1999998F;
         case "veh_dreirad":
            return -3.0F;
         case "veh_einrad":
            return 4.0F;
         case "veh_longboard":
            return 2.3F;
         case "veh_surfboard":
            return 2.5F;
         case "veh_carpet":
            return 2.0F;
         case "veh_broom":
            return 4.0F;
         case "veh_segway":
            return 2.8F;
         case "veh_inline":
            return 1.5F;
         default:
            return 0.0F;
      }
   }

   public static float seatZ(String id) {
      return 0.0F;
   }

   public static float handY(String id) {
      float[] h = hands(id);
      return h == null ? Float.NaN : h[1];
   }

   public static float handZ(String id) {
      float[] h = hands(id);
      return h == null ? Float.NaN : h[2];
   }

   public static float handX(String id) {
      float[] h = hands(id);
      return h == null ? Float.NaN : h[0];
   }

   private static float[] hands(String id) {
      switch (id) {
         case "veh_scooter":
            return VehRide.ESC_HAND;
         case "veh_kickscooter":
            return VehRide.KICK_HAND;
         case "veh_bike":
         case "veh_rennrad":
            return VehRide.BIKE_HAND;
         case "veh_bmx":
            return new float[]{5.0F, 14.5F, 7.8F};
         case "veh_moped":
            return VehRide.MOPED_HAND;
         case "veh_moto":
            return VehRide.MOTO_HAND;
         case "veh_car":
         case "veh_car_blue":
         case "veh_car_black":
         case "veh_car_white":
         case "veh_car_yellow":
         case "veh_car_green":
         case "veh_car_pink":
         case "veh_police":
         case "veh_taxi":
         case "veh_cabrio":
         case "veh_hotrod":
         case "veh_limo":
         case "veh_lambo":
            return VehCar.CAR_HAND;
         case "veh_kart":
         case "veh_f1":
            return VehCar.KART_HAND;
         case "veh_monster":
            return VehExtra.MONSTER_HAND;
         case "veh_quad":
            return VehExtra.QUAD_HAND;
         case "veh_snowmobile":
            return VehExtra.SNOW_HAND;
         case "veh_segway":
            return VehExtra.SEGWAY_HAND;
         case "veh_bobby":
            return new float[]{4.5F, 8.2F, 7.0F};
         case "veh_dreirad":
            return VehExtra.TRIKE_HAND;
         default:
            return null;
      }
   }

   public static float pedalAngle(float wheel) {
      return wheel * 0.45F;
   }

   public static float[][] pedals(String id, float wheel) {
      switch (id) {
         case "veh_bike":
         case "veh_rennrad":
         case "veh_bmx": {
            double c = Math.toRadians(pedalAngle(wheel));
            float L = 2.6F;
            float y = VehRide.BB[1];
            float z = VehRide.BB[2];
            return new float[][]{
               {2.7F, y - L * (float)Math.cos(c), z - L * (float)Math.sin(c)}, {-2.7F, y + L * (float)Math.cos(c), z + L * (float)Math.sin(c)}
            };
         }
         case "veh_dreirad":
         case "veh_einrad": {
            double c = Math.toRadians(pedalAngle(wheel));
            float L = 2.6F;
            float y = VehRide.BB[1];
            float z = VehRide.BB[2];
            return new float[][]{
               {2.7F, y - L * (float)Math.cos(c), z - L * (float)Math.sin(c)}, {-2.7F, y + L * (float)Math.cos(c), z + L * (float)Math.sin(c)}
            };
         }
         case "veh_moped":
            return new float[][]{{2.2F, 3.6F, 4.5F}, {-2.2F, 3.6F, 4.5F}};
         case "veh_moto":
            return new float[][]{{3.6F, 6.2F, -3.2F}, {-3.6F, 6.2F, -3.2F}};
         case "veh_quad":
            return new float[][]{{3.5F, 5.0F, -2.0F}, {-3.5F, 5.0F, -2.0F}};
         case "veh_snowmobile":
            return new float[][]{{3.5F, 4.5F, 0.0F}, {-3.5F, 4.5F, 0.0F}};
         default:
            return null;
      }
   }

   static float hoverBob(float t) {
      return (float)Math.sin(t * 2.4) * 0.45F;
   }

   static float cloudBob(float t) {
      return (float)Math.sin(t * 1.6) * 0.6F;
   }

   static CTex.T tex(String name) {
      return VehTex.tex(name);
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
