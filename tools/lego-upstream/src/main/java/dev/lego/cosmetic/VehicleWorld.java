package dev.lego.cosmetic;

import dev.lego.core.Mc;
import dev.lego.emote.Emotes;
import dev.lego.visual.R3;
import net.minecraft.class_2960;
import net.minecraft.class_630;
import net.minecraft.class_746;

/** Minecraft side of the visual vehicles: animation state, player pose, world rendering + small effects. */
public final class VehicleWorld {
    private VehicleWorld() { }

    private static float wheel, lastWheel, lean, speed;
    private static float lastBodyYaw;
    private static double lx, lz;
    private static final double[][] PUFFS = new double[24][5];   // x,y,z,age,kind
    private static int puffIdx;

    /** The equipped vehicle if it should be shown right now. */
    public static Cos.Item active() {
        Cos.Item it = Cos.equipped(Cos.Slot.VEHICLE);
        if (it == null) return null;
        class_746 p = Mc.player();
        if (p == null || p.method_5765() || p.method_5799() || p.method_6128() || p.method_6113() || Mc.isSpectator(p)) return null;
        return it;
    }

    public static float speed() { return active() == null ? 0 : speed; }

    private static float kmh;

    /** Real speed in km/h (blocks per tick x 20 x 3.6), smoothed. */
    public static int kmh() { return active() == null ? 0 : Math.round(kmh); }

    /** Texture of the scooter display showing the real speed (cached per value). */
    static String displayTex() { return "veh_display@" + Math.min(199, kmh()); }

    public static void tick() {
        class_746 p = Mc.player();
        if (p == null) return;
        double dx = Mc.x(p) - lx, dz = Mc.z(p) - lz;
        lx = Mc.x(p); lz = Mc.z(p);
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d > 3) d = 0;                                     // teleport
        lastWheel = wheel;
        wheel += (float) (d * 16 / (2 * Math.PI * 3) * 360);   // ~3 px wheel radius
        speed += ((float) Math.min(1, d / 0.28) - speed) * 0.25f;
        kmh += ((float) (d * 20 * 3.6) - kmh) * 0.35f;
        float by = Mc.bodyYaw(p);
        float dy = by - lastBodyYaw;
        while (dy > 180) dy -= 360;
        while (dy < -180) dy += 360;
        lastBodyYaw = by;
        lean += (Math.max(-1, Math.min(1, dy / 9f)) * Math.min(1, speed * 2) - lean) * 0.2f;
        Cos.Item it = active();
        if (it != null && speed > 0.2f && Mc.onGround(p) || it != null && (it.id.equals("veh_hover") || it.id.equals("veh_ufo"))) {
            double[] q = PUFFS[puffIdx = (puffIdx + 1) % PUFFS.length];
            double th = Math.toRadians(by);
            double back = it.id.startsWith("veh_car") ? 1.7 : (it.id.equals("veh_kart") ? 1.0 : 0.7);
            q[0] = Mc.x(p) + Math.sin(th) * back + (Math.random() - 0.5) * 0.3;
            q[2] = Mc.z(p) - Math.cos(th) * back + (Math.random() - 0.5) * 0.3;
            q[1] = Mc.y(p) + (it.id.equals("veh_hover") || it.id.equals("veh_ufo") ? 0.1 : 0.25);
            q[3] = 0;
            q[4] = it.id.equals("veh_hover") ? 1 : it.id.equals("veh_ufo") ? 2 : it.id.equals("veh_cloud") ? 3 : 0;
        }
    }

    // ------------------------------------------------------------------ pose (PlayerModelMixin, your own model only)
    public static void pose(class_630 head, class_630 body, class_630 rArm, class_630 lArm, class_630 rLeg, class_630 lLeg) {
        Cos.Item it = active();
        if (it == null) return;
        float t = (System.currentTimeMillis() % 3_600_000L) / 1000f;
        float lift = Vehicles.lift(it.id, t);
        Vehicles.Stance st = Vehicles.stance(it.id);
        boolean emote = Emotes.active();
        if (!emote) {
            float hy = Vehicles.handY(it.id), hz = Vehicles.handZ(it.id);
            boolean hands = !Float.isNaN(hy) && !Float.isNaN(hz);
            // arm pitch that points the arm (length 12 px, shoulder 22 px above the feet) at the handlebar
            float armPitch = hands ? (float) -Math.atan2(hz, (lift + 22) - hy) : -0.9f;
            float hx = hands ? Vehicles.handX(it.id) : 5;
            float armRoll = hands ? (float) Math.atan2(5 - (Float.isNaN(hx) ? 5 : hx), 11) : 0;
            switch (st) {
                case SIT:
                    rLeg.field_3654 = -1.45f; rLeg.field_3675 = 0.14f; rLeg.field_3674 = 0.05f;
                    lLeg.field_3654 = -1.45f; lLeg.field_3675 = -0.14f; lLeg.field_3674 = -0.05f;
                    rArm.field_3654 = armPitch + lean * 0.2f; rArm.field_3675 = -0.15f; rArm.field_3674 = 0;
                    lArm.field_3654 = armPitch - lean * 0.2f; lArm.field_3675 = 0.15f; lArm.field_3674 = 0;
                    break;
                case PEDAL: {
                    double crank = Math.toRadians(Vehicles.pedalAngle(wheel));
                    rLeg.field_3654 = -1.05f + (float) Math.sin(crank) * 0.38f; rLeg.field_3675 = 0.05f; rLeg.field_3674 = 0;
                    lLeg.field_3654 = -1.05f - (float) Math.sin(crank) * 0.38f; lLeg.field_3675 = -0.05f; lLeg.field_3674 = 0;
                    rArm.field_3654 = armPitch; rArm.field_3675 = 0; rArm.field_3674 = -armRoll;
                    lArm.field_3654 = armPitch; lArm.field_3675 = 0; lArm.field_3674 = armRoll;
                    break;
                }
                case RIDE:
                    rLeg.field_3654 = -1.25f; rLeg.field_3675 = 0.22f; rLeg.field_3674 = 0.08f;
                    lLeg.field_3654 = -1.25f; lLeg.field_3675 = -0.22f; lLeg.field_3674 = -0.08f;
                    rArm.field_3654 = armPitch; rArm.field_3675 = 0; rArm.field_3674 = -armRoll + lean * 0.05f;
                    lArm.field_3654 = armPitch; lArm.field_3675 = 0; lArm.field_3674 = armRoll + lean * 0.05f;
                    body.field_3674 = -lean * 0.12f;
                    break;
                case KICK: {
                    // right foot on the deck, left foot kicks while moving
                    double ph = Math.toRadians(wheel * 0.35);
                    float kick = speed > 0.05f ? (float) Math.sin(ph) * 0.75f * Math.min(1, speed * 2) : 0;
                    rLeg.field_3654 = 0.05f; rLeg.field_3675 = 0.03f; rLeg.field_3674 = 0.03f;
                    lLeg.field_3654 = kick; lLeg.field_3675 = -0.05f; lLeg.field_3674 = -0.06f;
                    rArm.field_3654 = armPitch; rArm.field_3675 = 0; rArm.field_3674 = -armRoll;
                    lArm.field_3654 = armPitch; lArm.field_3675 = 0; lArm.field_3674 = armRoll;
                    break;
                }
                case HOLD:
                    rArm.field_3654 = armPitch; rArm.field_3675 = 0; rArm.field_3674 = -armRoll;
                    lArm.field_3654 = armPitch; lArm.field_3675 = 0; lArm.field_3674 = armRoll;
                    // fall through: legs like standing
                default:
                    rLeg.field_3654 = 0.08f; rLeg.field_3675 = 0.05f; rLeg.field_3674 = 0.04f + lean * 0.05f;
                    lLeg.field_3654 = -0.12f; lLeg.field_3675 = -0.05f; lLeg.field_3674 = -0.04f + lean * 0.05f;
                    body.field_3674 = -lean * 0.06f;
            }
        }
        // raise / lower the whole model
        for (class_630 part : new class_630[]{head, body, rArm, lArm, rLeg, lLeg}) part.field_3656 -= lift;
    }

    // ------------------------------------------------------------------ world rendering (inside R3 begin/end)
    private static class_2960 glowTex;

    public static void render(float td) {
        Cos.Item it = active();
        class_746 p = Mc.player();
        if (it == null || p == null) return;
        double[] pos = Mc.lerpPos(p, td);
        float by = Mc.lastBodyYaw(p) + wrap(Mc.bodyYaw(p) - Mc.lastBodyYaw(p)) * td;
        Cos.A a = new Cos.A();
        a.time = (System.currentTimeMillis() % 3_600_000L) / 1000f;
        a.move = speed;
        a.wheel = lastWheel + (wheel - lastWheel) * td;
        a.lean = lean;
        int light = CosRender.light(pos[0], pos[1] + 0.5, pos[2]);
        CosRender.drawWorld(it.model, a, pos[0], pos[1], pos[2], by, 0, 0, 1f, light);

        if (glowTex == null) {
            glowTex = class_2960.method_60655("legoclient", "vehicle/glow");
            try { dev.lego.ui.Tx.register(glowTex, dev.lego.visual.AmbientTex.paint("glow"), dev.lego.visual.AmbientTex.S, dev.lego.visual.AmbientTex.S, true); } catch (Throwable ignored) { }
        }
        for (double[] q : PUFFS) {
            if (q[3] > 1.2) continue;
            q[3] += 0.016;
            double k = q[3] / 1.2;
            int c;
            double size;
            switch ((int) q[4]) {
                case 1: c = R3.argb(0x24E0FF, 0.5 * (1 - k)); size = 0.25 + k * 0.2; break;
                case 2: c = R3.argb(0x7CFF6B, 0.45 * (1 - k)); size = 0.3 + k * 0.3; break;
                case 3: c = R3.argb(0xFFFFFF, 0.5 * (1 - k)); size = 0.4 + k * 0.5; break;
                default: c = R3.argb(0xB8B2A8, 0.35 * (1 - k)); size = 0.25 + k * 0.6;
            }
            R3.glowBillboard(glowTex, q[0], q[1] + k * 0.4, q[2], size, c);
        }
    }

    private static float wrap(float a) { a %= 360; if (a > 180) a -= 360; if (a < -180) a += 360; return a; }
}
