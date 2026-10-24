package dev.lego.cosmetic;

import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Visual-only vehicles (you still walk, it just looks like you ride). Vehicle space: px (1/16 block), origin on the
 * ground below the player, x = right, y = up, z = forward. A.wheel = wheel angle (deg), A.lean = turning -1..1,
 * A.move = speed 0..1. The models live in VehCar (car, kart), VehRide (scooters, bikes) and VehBoard (boards, UFO,
 * cloud); geometry helpers in VehGeo, textures in VehTex.
 */
public final class Vehicles {
    private Vehicles() { }

    /**
     * How the player stands / sits.
     * STAND: standing on a board. HOLD: standing, both hands on a handlebar. SIT: sitting, legs forward (car, kart).
     * KICK: one foot on the deck, the other kicking, hands on the bar. PEDAL: sitting on a saddle, pedalling
     * (feet follow {@link #pedals}). RIDE: sitting astride, leaning forward, hands on the bars.
     */
    public enum Stance { STAND, HOLD, SIT, KICK, PEDAL, RIDE }

    static void registerAll() {
        Cos.add("veh_skate", "Skateboard", Slot.VEHICLE, Rarity.RARE, VehBoard::skateboard);
        Cos.add("veh_hover", "Hoverboard", Slot.VEHICLE, Rarity.LEGENDARY, VehBoard::hoverboard);
        Cos.add("veh_kickscooter", "Tretroller", Slot.VEHICLE, Rarity.COMMON, VehRide::kickScooter);
        Cos.add("veh_scooter", "E-Scooter", Slot.VEHICLE, Rarity.EPIC, VehRide::eScooter);
        Cos.add("veh_bike", "Fahrrad", Slot.VEHICLE, Rarity.RARE, VehRide::bike);
        Cos.add("veh_moped", "Motorroller", Slot.VEHICLE, Rarity.EPIC, VehRide::moped);
        Cos.add("veh_moto", "Motorrad", Slot.VEHICLE, Rarity.LEGENDARY, VehRide::motorbike);
        Cos.add("veh_car", "Sportwagen", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0xE3242B, 0xF2F2F2));
        Cos.add("veh_car_blue", "Sportwagen Blau", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0x1F6BFF, 0xF2F2F2));
        Cos.add("veh_kart", "Go-Kart", Slot.VEHICLE, Rarity.EPIC, VehCar::kart);
        Cos.add("veh_ufo", "UFO", Slot.VEHICLE, Rarity.LEGENDARY, VehBoard::ufo);
        Cos.add("veh_cloud", "Wolke", Slot.VEHICLE, Rarity.EPIC, VehBoard::cloud);
        // car color variants
        Cos.add("veh_car_black", "Sportwagen Schwarz", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0x1A1A1E, 0xE8E8E8));
        Cos.add("veh_car_white", "Sportwagen Weiß", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0xF2F2F2, 0x1A1A1E));
        Cos.add("veh_car_yellow", "Sportwagen Gelb", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0xFFCD03, 0x1A1A1E));
        Cos.add("veh_car_green", "Sportwagen Grün", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0x2FA58C, 0xF2F2F2));
        Cos.add("veh_car_pink", "Sportwagen Pink", Slot.VEHICLE, Rarity.LEGENDARY, (g, a) -> VehCar.car(g, a, 0xFF69B4, 0xF2F2F2));
        // new vehicles
        Cos.add("veh_police", "Polizeiwagen", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::police);
        Cos.add("veh_taxi", "Taxi", Slot.VEHICLE, Rarity.EPIC, VehExtra::taxi);
        Cos.add("veh_bobby", "Bobby Car", Slot.VEHICLE, Rarity.RARE, VehExtra::bobby);
        Cos.add("veh_monster", "Monster Truck", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::monster);
        Cos.add("veh_f1", "Formel 1", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::f1);
        Cos.add("veh_quad", "Quad", Slot.VEHICLE, Rarity.EPIC, VehExtra::quad);
        Cos.add("veh_limo", "Limousine", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::limo);
        Cos.add("veh_longboard", "Longboard", Slot.VEHICLE, Rarity.RARE, VehExtra::longboard);
        Cos.add("veh_surfboard", "Surfbrett", Slot.VEHICLE, Rarity.EPIC, VehExtra::surfboard);
        Cos.add("veh_carpet", "Fliegender Teppich", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::carpet);
        Cos.add("veh_broom", "Hexenbesen", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::broom);
        Cos.add("veh_segway", "Segway", Slot.VEHICLE, Rarity.EPIC, VehExtra::segway);
        Cos.add("veh_snowmobile", "Schneemobil", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::snowmobile);
        Cos.add("veh_dreirad", "Dreirad", Slot.VEHICLE, Rarity.RARE, VehExtra::dreirad);
        Cos.add("veh_inline", "Inline Skates", Slot.VEHICLE, Rarity.EPIC, VehExtra::inline);
        Cos.add("veh_einrad", "Einrad", Slot.VEHICLE, Rarity.RARE, VehExtra::einrad);
        Cos.add("veh_rennrad", "Rennrad", Slot.VEHICLE, Rarity.EPIC, VehExtra::rennrad);
        Cos.add("veh_bmx", "BMX", Slot.VEHICLE, Rarity.RARE, VehExtra::bmx);
        Cos.add("veh_cabrio", "Cabrio", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::cabrio);
        Cos.add("veh_hotrod", "Hot Rod", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::hotrod);
        Cos.add("veh_lambo", "Lamborghini", Slot.VEHICLE, Rarity.LEGENDARY, VehExtra::lambo);
    }

    public static Stance stance(String id) {
        switch (id) {
            case "veh_scooter": case "veh_segway": return Stance.HOLD;
            case "veh_kickscooter": return Stance.KICK;
            case "veh_car": case "veh_car_blue": case "veh_car_black": case "veh_car_white":
            case "veh_car_yellow": case "veh_car_green": case "veh_car_pink":
            case "veh_kart": case "xm_veh_sleigh":
            case "veh_police": case "veh_taxi": case "veh_bobby":
            case "veh_monster": case "veh_f1":
            case "veh_limo": case "veh_cabrio": case "veh_hotrod":
            case "veh_lambo": return Stance.SIT;
            case "veh_bike": case "veh_rennrad": case "veh_bmx": case "veh_dreirad":
            case "veh_einrad": return Stance.PEDAL;
            case "veh_moped": case "veh_moto": case "veh_quad": case "veh_snowmobile": return Stance.RIDE;
            default: return Stance.STAND;
        }
    }

    /**
     * Pixels the player model is raised (negative = lowered into a seat). Standing: height of the feet above the
     * ground. Sitting (SIT / PEDAL / RIDE): the hips (leg pivot, 12 px above the feet) end up at lift + 12.
     */
    public static float lift(String id, float time) {
        switch (id) {
            case "veh_skate": return VehBoard.SKATE_TOP;
            case "veh_hover": return VehBoard.HOVER_TOP + hoverBob(time);
            case "veh_kickscooter": return VehRide.KICK_TOP;
            case "veh_scooter": return VehRide.ESC_TOP;
            case "veh_bike": case "veh_rennrad": return VehRide.BIKE_SEAT - 12;
            case "veh_bmx": return 10.3f - 12;
            case "veh_moped": return VehRide.MOPED_SEAT - 12;
            case "veh_moto": return VehRide.MOTO_SEAT - 12;
            case "veh_car": case "veh_car_blue": case "veh_car_black": case "veh_car_white":
            case "veh_car_yellow": case "veh_car_green": case "veh_car_pink":
            case "veh_police": case "veh_taxi": case "veh_cabrio": case "veh_hotrod":
                return VehCar.CAR_HIP - 12;
            case "veh_kart": case "veh_f1": return VehCar.KART_HIP - 12;
            case "xm_veh_sleigh": return XmasPets.SLEIGH_HIP - 12;
            case "veh_ufo": return VehBoard.UFO_TOP + hoverBob(time) * 1.4f;
            case "veh_cloud": return VehBoard.CLOUD_TOP + cloudBob(time);
            case "veh_bobby": return VehExtra.BOBBY_HIP - 12;
            case "veh_monster": return VehExtra.MONSTER_HIP - 12;
            case "veh_quad": return VehExtra.QUAD_SEAT - 12;
            case "veh_limo": return VehCar.CAR_HIP - 12;
            case "veh_lambo": return VehCar.CAR_HIP - 12;
            case "veh_snowmobile": return VehExtra.SNOW_SEAT - 12;
            case "veh_dreirad": return VehExtra.TRIKE_SEAT - 12;
            case "veh_einrad": return 16.0f - 12;
            case "veh_longboard": return VehExtra.LONG_TOP;
            case "veh_surfboard": return VehExtra.SURF_TOP;
            case "veh_carpet": return VehExtra.CARPET_TOP;
            case "veh_broom": return 4.0f;
            case "veh_segway": return VehExtra.SEGWAY_TOP;
            case "veh_inline": return 1.5f;
            default: return 0;
        }
    }

    /** Where the player's hips are along z (px). All vehicles are built around the player, so this is 0. */
    public static float seatZ(String id) {
        return 0;
    }

    /** Handlebar / steering wheel grip height above the ground (px), or NaN if there is nothing to hold. */
    public static float handY(String id) {
        float[] h = hands(id);
        return h == null ? Float.NaN : h[1];
    }

    /** Handlebar / steering wheel grip position along z (px, forward). NaN if there is nothing to hold. */
    public static float handZ(String id) {
        float[] h = hands(id);
        return h == null ? Float.NaN : h[2];
    }

    /** Half distance between the two grips (px, the right hand is at +x). NaN if there is nothing to hold. */
    public static float handX(String id) {
        float[] h = hands(id);
        return h == null ? Float.NaN : h[0];
    }

    private static float[] hands(String id) {
        switch (id) {
            case "veh_scooter": return VehRide.ESC_HAND;
            case "veh_kickscooter": return VehRide.KICK_HAND;
            case "veh_bike": case "veh_rennrad": return VehRide.BIKE_HAND;
            case "veh_bmx": return new float[]{5.0f, 14.5f, 7.8f};
            case "veh_moped": return VehRide.MOPED_HAND;
            case "veh_moto": return VehRide.MOTO_HAND;
            case "veh_car": case "veh_car_blue": case "veh_car_black": case "veh_car_white":
            case "veh_car_yellow": case "veh_car_green": case "veh_car_pink":
            case "veh_police": case "veh_taxi": case "veh_cabrio": case "veh_hotrod":
            case "veh_limo": case "veh_lambo": return VehCar.CAR_HAND;
            case "veh_kart": case "veh_f1": return VehCar.KART_HAND;
            case "veh_monster": return VehExtra.MONSTER_HAND;
            case "veh_quad": return VehExtra.QUAD_HAND;
            case "veh_snowmobile": return VehExtra.SNOW_HAND;
            case "veh_segway": return VehExtra.SEGWAY_HAND;
            case "veh_bobby": return new float[]{4.5f, 8.2f, 7.0f};
            case "veh_dreirad": return VehExtra.TRIKE_HAND;
            default: return null;
        }
    }

    /** Crank angle (deg) of the bicycle for a given wheel angle (rolling forward = positive). */
    public static float pedalAngle(float wheel) { return wheel * VehRide.GEAR; }

    /**
     * Pedal / foot peg positions {right{x,y,z}, left{x,y,z}} in vehicle space (px) for the given wheel angle, or null.
     * Bicycle: the pedals turn with the wheel. Moped / motorbike: fixed foot rests.
     */
    public static float[][] pedals(String id, float wheel) {
        switch (id) {
            case "veh_bike": case "veh_rennrad": case "veh_bmx": {
                double c = Math.toRadians(pedalAngle(wheel));
                float L = VehRide.CRANK, y = VehRide.BB[1], z = VehRide.BB[2];
                return new float[][]{{VehRide.PEDAL_X, y - L * (float) Math.cos(c), z - L * (float) Math.sin(c)},
                        {-VehRide.PEDAL_X, y + L * (float) Math.cos(c), z + L * (float) Math.sin(c)}};
            }
            case "veh_dreirad": case "veh_einrad": {
                double c = Math.toRadians(pedalAngle(wheel));
                float L = VehRide.CRANK, y = VehRide.BB[1], z = VehRide.BB[2];
                return new float[][]{{VehRide.PEDAL_X, y - L * (float) Math.cos(c), z - L * (float) Math.sin(c)},
                        {-VehRide.PEDAL_X, y + L * (float) Math.cos(c), z + L * (float) Math.sin(c)}};
            }
            case "veh_moped": return new float[][]{{2.2f, 3.6f, 4.5f}, {-2.2f, 3.6f, 4.5f}};
            case "veh_moto": return new float[][]{{3.6f, 6.2f, -3.2f}, {-3.6f, 6.2f, -3.2f}};
            case "veh_quad": return new float[][]{{3.5f, 5.0f, -2.0f}, {-3.5f, 5.0f, -2.0f}};
            case "veh_snowmobile": return new float[][]{{3.5f, 4.5f, 0}, {-3.5f, 4.5f, 0}};
            default: return null;
        }
    }

    static float hoverBob(float t) { return (float) Math.sin(t * 2.4) * 0.45f; }
    static float cloudBob(float t) { return (float) Math.sin(t * 1.6) * 0.6f; }

    // ================================================================== textures
    static CTex.T tex(String name) { return VehTex.tex(name); }
}
