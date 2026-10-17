package dev.lego.cosmetic;

import dev.lego.core.Mc;
import dev.lego.perf.Performance;
import dev.lego.ui.Tx;
import dev.lego.visual.AmbientTex;
import dev.lego.visual.R3;
import java.util.Random;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

/**
 * World effects for the visual-only vehicles: exhaust smoke, boost flames and backfire, wheel dust, tyre smoke and
 * skid marks when drifting, landing dust rings, head and tail lights with light beams at night, brake lights, police
 * strobes, a UFO tractor beam, hover glow, magic trails (broom, carpet), snow spray and wind streaks at speed.
 * Vehicle space is the same as in {@link Vehicles}: px (1/16 block), x = right, y = up, z = forward. Only the local
 * player's vehicle gets effects; nothing here changes movement.
 */
public final class VehicleFx {
   private VehicleFx() {
   }

   private static final int GLOW = 0, SMOKE = 1, SPARK = 2, SNOW = 3;
   private static final String[] TEX = {"glow", "ash", "glitter", "snow"};
   private static final Identifier[] IDS = new Identifier[TEX.length];
   private static boolean texReady;

   // Particle pool (ring buffer). Positions in blocks, velocities in blocks per second.
   private static final int MAX = 768;
   private static final double[] X = new double[MAX], Y = new double[MAX], Z = new double[MAX];
   private static final float[] VX = new float[MAX], VY = new float[MAX], VZ = new float[MAX];
   private static final float[] AGE = new float[MAX], LIFE = new float[MAX], S0 = new float[MAX], S1 = new float[MAX];
   private static final float[] GRAV = new float[MAX], DRAG = new float[MAX];
   private static final int[] COL = new int[MAX];
   private static final byte[] KIND = new byte[MAX];
   private static int next;

   // Skid marks: x0, z0, x1, z1, y, age.
   private static final int SK = 320;
   private static final double[][] SKID = new double[SK][6];
   private static int skidIdx;
   private static final double[][] lastTyre = new double[4][];

   static {
      for (double[] s : SKID) s[5] = 10.0;
   }

   private static String id = "";
   private static Profile prof;
   private static boolean wasAir;
   private static double airVy;
   private static float lastSpeed;
   private static float accel;
   private static float pitch;
   private static float brake;
   private static boolean wasBoost;
   private static long lastNs;
   private static final Random R = new Random();

   /** special effect kinds */
   private static final int NONE = 0, SIREN = 1, TAXI = 2, UFO = 3, HOVER = 4, CLOUD = 5, CARPET = 6, BROOM = 7, SNOWMO = 8, LED = 9;

   private record Profile(
      float[][] exhaust, int smoke, boolean flames, float[][] tyres, float dust, float[][] head, float[][] tail, int special, boolean heavy, boolean fast, boolean car
   ) {
   }

   private static float[] p(float x, float y, float z) {
      return new float[]{x, y, z};
   }

   private static float[][] pair(float x, float y, float z) {
      return new float[][]{p(-x, y, z), p(x, y, z)};
   }

   private static final float[][] NO = new float[0][];

   private static Profile profile(String v) {
      float[][] carWheels = pair(10.6F, 0.0F, -18.5F);
      float[][] carHead = pair(8.6F, 7.2F, 31.0F);
      float[][] carTail = pair(9.5F, 8.0F, -29.6F);
      float[][] carPipe = {p(-5.0F, 3.6F, -31.0F), p(5.0F, 3.6F, -31.0F)};
      if (v.startsWith("veh_car")) return new Profile(carPipe, 0xA9A9AE, true, carWheels, 1.0F, carHead, carTail, NONE, false, true, true);
      return switch (v) {
         case "veh_police" -> new Profile(carPipe, 0xA9A9AE, false, carWheels, 1.0F, carHead, carTail, SIREN, false, true, true);
         case "veh_taxi" -> new Profile(carPipe, 0xA9A9AE, false, carWheels, 1.0F, carHead, carTail, TAXI, false, false, true);
         case "veh_limo" -> new Profile(pair(5.0F, 3.6F, -40.0F), 0xA9A9AE, false, pair(10.6F, 0.0F, -27.0F), 1.0F, carHead, pair(9.5F, 8.0F, -39.0F), NONE, false, false, true);
         case "veh_cabrio" -> new Profile(carPipe, 0xA9A9AE, false, carWheels, 1.0F, carHead, carTail, NONE, false, true, true);
         case "veh_lambo", "veh_hotrod" -> new Profile(pair(4.0F, 3.4F, -31.0F), 0x8D8D94, true, carWheels, 1.1F, carHead, carTail, NONE, false, true, true);
         case "veh_f1" -> new Profile(new float[][]{p(0.0F, 6.0F, -30.0F)}, 0x9A9AA0, true, pair(11.0F, 0.0F, -18.0F), 1.2F, NO, new float[][]{p(0.0F, 5.5F, -31.0F)}, NONE, false, true, true);
         case "veh_monster" -> new Profile(pair(6.0F, 24.0F, -9.0F), 0x3A3A40, true, new float[][]{p(-11.0F, 0.0F, -18.0F), p(11.0F, 0.0F, -18.0F), p(-11.0F, 0.0F, 18.0F), p(11.0F, 0.0F, 18.0F)}, 2.0F, pair(8.0F, 16.0F, 26.0F), pair(9.0F, 15.0F, -25.0F), NONE, true, false, true);
         case "veh_kart" -> new Profile(new float[][]{p(0.0F, 5.0F, -14.0F)}, 0x9A9AA0, false, pair(9.2F, 0.0F, -8.0F), 0.9F, NO, NO, NONE, false, true, true);
         case "veh_bobby" -> new Profile(NO, 0, false, pair(6.0F, 0.0F, -8.0F), 0.5F, NO, NO, NONE, false, false, true);
         case "veh_quad" -> new Profile(new float[][]{p(4.0F, 7.0F, -15.0F)}, 0x8D8D94, false, pair(8.0F, 0.0F, -10.0F), 1.5F, pair(4.0F, 10.0F, 15.0F), pair(5.0F, 9.0F, -15.0F), NONE, true, false, false);
         case "veh_snowmobile" -> new Profile(new float[][]{p(3.0F, 7.0F, -18.0F)}, 0x9A9AA0, false, NO, 0.0F, new float[][]{p(0.0F, 10.0F, 18.0F)}, new float[][]{p(0.0F, 9.0F, -18.0F)}, SNOWMO, false, false, false);
         case "veh_moped", "veh_scooter" -> new Profile(v.equals("veh_moped") ? new float[][]{p(3.0F, 4.0F, -12.0F)} : NO, 0x9A9AA0, false, new float[][]{p(0.0F, 0.0F, -8.0F)}, 0.6F, new float[][]{p(0.0F, 14.0F, 13.0F)}, new float[][]{p(0.0F, 11.0F, -12.0F)}, NONE, false, false, false);
         case "veh_moto" -> new Profile(new float[][]{p(4.0F, 5.0F, -15.0F)}, 0x8D8D94, true, new float[][]{p(0.0F, 0.0F, -10.0F)}, 0.8F, new float[][]{p(0.0F, 13.0F, 15.0F)}, new float[][]{p(0.0F, 11.0F, -14.0F)}, NONE, false, true, false);
         case "veh_bike", "veh_rennrad", "veh_bmx", "veh_einrad", "veh_dreirad", "veh_kickscooter" -> new Profile(NO, 0, false, new float[][]{p(0.0F, 0.0F, -4.2F)}, 0.45F, NO, NO, NONE, false, false, false);
         case "veh_skate", "veh_longboard", "veh_inline", "veh_surfboard" -> new Profile(NO, 0, false, new float[][]{p(0.0F, 0.0F, -6.0F)}, 0.35F, NO, NO, NONE, false, false, false);
         case "veh_segway" -> new Profile(NO, 0, false, NO, 0.3F, NO, NO, LED, false, false, false);
         case "veh_hover" -> new Profile(NO, 0, false, NO, 0.0F, NO, NO, HOVER, false, true, false);
         case "veh_ufo" -> new Profile(NO, 0, false, NO, 0.0F, NO, NO, UFO, false, true, false);
         case "veh_cloud" -> new Profile(NO, 0, false, NO, 0.0F, NO, NO, CLOUD, false, false, false);
         case "veh_carpet" -> new Profile(NO, 0, false, NO, 0.0F, NO, NO, CARPET, false, false, false);
         case "veh_broom" -> new Profile(NO, 0, false, NO, 0.0F, NO, NO, BROOM, false, true, false);
         default -> new Profile(NO, 0, false, NO, 0.4F, NO, NO, NONE, false, false, false);
      };
   }

   /** Body pitch in degrees for cars: squats when accelerating, dives when braking. */
   public static float pitch() {
      return prof != null && prof.car() ? pitch : 0.0F;
   }

   // ------------------------------------------------------------------ spawning

   private static void spawn(int kind, double x, double y, double z, double vx, double vy, double vz, float life, float s0, float s1, int argb, float grav, float drag) {
      int i = next;
      next = (next + 1) % MAX;
      X[i] = x;
      Y[i] = y;
      Z[i] = z;
      VX[i] = (float)vx;
      VY[i] = (float)vy;
      VZ[i] = (float)vz;
      AGE[i] = 0.0F;
      LIFE[i] = life;
      S0[i] = s0;
      S1[i] = s1;
      COL[i] = argb;
      KIND[i] = (byte)kind;
      GRAV[i] = grav;
      DRAG[i] = drag;
   }

   private static float rnd(float a) {
      return (R.nextFloat() - 0.5F) * 2.0F * a;
   }

   /** Vehicle-space px point to world blocks. */
   private static double[] w(double bx, double by, double bz, float yaw, float x, float y, float z) {
      double r = Math.toRadians(yaw);
      double fx = -Math.sin(r), fz = Math.cos(r), rx = -Math.cos(r), rz = -Math.sin(r);
      return new double[]{bx + (x * rx + z * fx) / 16.0, by + y / 16.0, bz + (x * rz + z * fz) / 16.0};
   }

   private static int dustColor() {
      String b = "";
      try {
         b = String.valueOf(Mc.biome()).toLowerCase();
      } catch (Throwable ignored) {
      }
      if (b.contains("snow") || b.contains("frozen") || b.contains("ice") || b.contains("grove") || b.contains("peaks")) return 0xF2F6FF;
      if (b.contains("desert") || b.contains("beach") || b.contains("badlands")) return 0xD9C38C;
      if (b.contains("nether") || b.contains("crimson") || b.contains("warped") || b.contains("basalt")) return 0x5A3236;
      return 0x8E7A5C;
   }

   /** Called every client tick (20/s) from {@link VehicleWorld#tick()}. */
   static void tick(Cos.Item item, LocalPlayer pl, float speed, float lean) {
      if (item == null || pl == null) {
         id = "";
         prof = null;
         wasBoost = false;
         for (int i = 0; i < lastTyre.length; i++) lastTyre[i] = null;
         return;
      }
      if (!item.id.equals(id)) {
         id = item.id;
         prof = profile(id);
      }
      Profile pr = prof;
      boolean low = Performance.lowMode();
      double bx = Mc.x(pl), by = Mc.y(pl), bz = Mc.z(pl);
      float yaw = Mc.bodyYaw(pl);
      double yr = Math.toRadians(yaw);
      double fx = -Math.sin(yr), fz = Math.cos(yr);
      boolean ground = Mc.onGround(pl);
      double vy = Mc.velocity(pl)[1];
      boolean sprint = pl.isSprinting();
      accel = accel + (speed - lastSpeed - accel) * 0.5F;
      lastSpeed = speed;
      pitch += (Math.max(-3.5F, Math.min(3.5F, accel * 70.0F)) - pitch) * 0.25F;
      if (accel < -0.012F) brake = 0.6F;
      boolean boost = sprint && speed > 0.6F && pr.fast();
      float density = low ? 0.4F : 1.0F;
      // backwards drift of the exhaust relative to the moving vehicle (blocks/s)
      double mv = speed * 5.6;

      // Exhaust smoke, boost flames, backfire.
      for (float[] e : pr.exhaust()) {
         double[] q = w(bx, by, bz, yaw, e[0], e[1], e[2]);
         if (R.nextFloat() < (0.3F + speed * 0.9F) * density) {
            boolean stack = pr.heavy() && e[1] > 18.0F;
            double up = stack ? 0.9 : 0.25;
            spawn(SMOKE, q[0] + rnd(0.04F), q[1], q[2] + rnd(0.04F), -fx * (0.6 + mv * 0.2) + rnd(0.15F), up + R.nextFloat() * 0.25, -fz * (0.6 + mv * 0.2) + rnd(0.15F),
               1.3F + R.nextFloat() * 0.9F, stack ? 0.25F : 0.12F, stack ? 1.3F : 0.7F, R3.argb(pr.smoke(), stack ? 0.5 : 0.32), -0.15F, 1.2F);
         }
         if (boost && pr.flames()) {
            for (int k = 0; k < 2; k++) {
               spawn(GLOW, q[0], q[1], q[2], -fx * (3.0 + R.nextFloat() * 2.0) + rnd(0.3F), rnd(0.3F), -fz * (3.0 + R.nextFloat() * 2.0) + rnd(0.3F),
                  0.16F + R.nextFloat() * 0.08F, 0.34F, 0.06F, R3.argb(k == 0 ? 0xFF8A2A : 0x58A8FF, 0.9), 0.0F, 2.0F);
            }
         }
         if (wasBoost && !boost && pr.flames()) {
            // Backfire: a bright pop and a few sparks when you let go.
            spawn(GLOW, q[0], q[1], q[2], -fx * 1.5, 0.1, -fz * 1.5, 0.22F, 0.9F, 0.2F, R3.argb(0xFFB040, 1.0), 0.0F, 3.0F);
            for (int k = 0; k < 8; k++) {
               spawn(SPARK, q[0], q[1], q[2], -fx * 3.0 + rnd(2.0F), 0.8 + R.nextFloat() * 1.5, -fz * 3.0 + rnd(2.0F), 0.45F + R.nextFloat() * 0.3F, 0.12F, 0.04F, R3.argb(0xFFD27A, 1.0), 9.0F, 0.6F);
            }
            spawn(SMOKE, q[0], q[1], q[2], -fx, 0.4, -fz, 1.6F, 0.25F, 0.9F, R3.argb(0x5A5A60, 0.5), -0.1F, 1.0F);
         }
      }
      wasBoost = boost;

      // Wheel dust.
      int dust = dustColor();
      if (ground && speed > 0.22F && pr.dust() > 0.0F) {
         for (float[] t : pr.tyres()) {
            if (R.nextFloat() < speed * 0.7F * density) {
               double[] q = w(bx, by, bz, yaw, t[0], t[1], t[2]);
               float sz = 0.18F * pr.dust();
               spawn(SMOKE, q[0] + rnd(0.1F), q[1] + 0.05, q[2] + rnd(0.1F), -fx * mv * 0.25 + rnd(0.4F), 0.35 + R.nextFloat() * 0.4, -fz * mv * 0.25 + rnd(0.4F),
                  0.9F + R.nextFloat() * 0.6F, sz, sz * 4.0F, R3.argb(dust, 0.35), 0.2F, 1.6F);
            }
         }
      }

      // Drifting: tyre smoke and skid marks on the rear tyres.
      boolean drift = ground && pr.car() && Math.abs(lean) > 0.5F && speed > 0.45F;
      float[][] ty = pr.tyres();
      for (int i = 0; i < Math.min(lastTyre.length, ty.length); i++) {
         double[] q = w(bx, by, bz, yaw, ty[i][0], 0.0F, ty[i][2]);
         if (drift) {
            if (lastTyre[i] != null) {
               double[] s = SKID[skidIdx];
               skidIdx = (skidIdx + 1) % SK;
               s[0] = lastTyre[i][0];
               s[1] = lastTyre[i][2];
               s[2] = q[0];
               s[3] = q[2];
               s[4] = Math.floor(by * 16.0 + 0.5) / 16.0 + 0.012;
               s[5] = 0.0;
            }
            lastTyre[i] = q;
            if (R.nextFloat() < 0.8F * density) {
               spawn(SMOKE, q[0], q[1] + 0.1, q[2], rnd(0.4F), 0.3 + R.nextFloat() * 0.3, rnd(0.4F), 1.6F + R.nextFloat(), 0.25F, 1.4F, R3.argb(0xE6E6EA, 0.42), -0.05F, 1.0F);
            }
         } else {
            lastTyre[i] = null;
         }
      }

      // Landing: dust ring, sparks on heavy vehicles.
      if (!ground) {
         wasAir = true;
         airVy = Math.min(airVy, vy);
      } else {
         if (wasAir && airVy < -0.35 && pr.dust() > 0.0F) {
            int n = low ? 8 : 18;
            for (int k = 0; k < n; k++) {
               double a = Math.PI * 2 * k / n;
               spawn(SMOKE, bx + Math.cos(a) * 0.4, by + 0.05, bz + Math.sin(a) * 0.4, Math.cos(a) * 2.2, 0.25, Math.sin(a) * 2.2, 1.0F + R.nextFloat() * 0.5F,
                  0.25F * pr.dust(), 0.9F * pr.dust(), R3.argb(dust, 0.45), 0.0F, 2.2F);
            }
            if (pr.heavy()) {
               for (int k = 0; k < 14; k++) spawn(SPARK, bx + rnd(0.6F), by + 0.1, bz + rnd(0.6F), rnd(3.0F), 1.5 + R.nextFloat() * 2.0, rnd(3.0F), 0.5F + R.nextFloat() * 0.3F, 0.1F, 0.03F, R3.argb(0xFFC060, 1.0), 9.0F, 0.5F);
            }
         }
         wasAir = false;
         airVy = 0.0;
      }

      // Specials.
      switch (pr.special()) {
         case UFO -> {
            for (int k = 0; k < (low ? 1 : 3); k++) {
               double a = R.nextFloat() * Math.PI * 2, r = R.nextFloat() * 0.45;
               spawn(GLOW, bx + Math.cos(a) * r, by + 0.02, bz + Math.sin(a) * r, 0.0, 0.5 + R.nextFloat() * 0.3, 0.0, 0.5F, 0.12F, 0.05F, R3.argb(0x7CFF9B, 0.85), 0.0F, 0.0F);
            }
         }
         case HOVER -> {
            for (int k = 0; k < (low ? 1 : 2); k++) {
               double[] q = w(bx, by, bz, yaw, rnd(6.0F), 2.0F, rnd(9.0F));
               spawn(GLOW, q[0], q[1], q[2], rnd(0.2F) - fx * mv * 0.2, -0.6, rnd(0.2F) - fz * mv * 0.2, 0.35F, 0.12F, 0.02F, R3.argb(0x4FC8FF, 0.9), 0.0F, 1.0F);
            }
         }
         case CLOUD -> {
            if (R.nextFloat() < 0.6F * density) {
               spawn(SMOKE, bx + rnd(0.5F), by + 0.1, bz + rnd(0.5F), -fx * mv * 0.2 + rnd(0.1F), -0.05, -fz * mv * 0.2 + rnd(0.1F), 2.0F, 0.4F, 1.0F, R3.argb(0xFFFFFF, 0.5), 0.0F, 1.0F);
            }
         }
         case CARPET -> {
            for (int k = 0; k < (low ? 1 : 3); k++) {
               double[] q = w(bx, by, bz, yaw, rnd(9.0F), 2.0F, -12.0F + rnd(2.0F));
               spawn(SPARK, q[0], q[1], q[2], -fx * 0.3 + rnd(0.15F), -0.15 + rnd(0.1F), -fz * 0.3 + rnd(0.15F), 1.0F + R.nextFloat() * 0.6F, 0.14F, 0.02F, R3.argb(R.nextBoolean() ? 0xFFD54A : 0xFF9F3A, 1.0), 0.3F, 0.4F);
            }
         }
         case BROOM -> {
            for (int k = 0; k < (low ? 1 : 3); k++) {
               double[] q = w(bx, by, bz, yaw, rnd(2.0F), 8.0F + rnd(2.0F), -15.0F);
               int c = switch (R.nextInt(3)) {
                  case 0 -> 0xB46CFF;
                  case 1 -> 0x6CFF8A;
                  default -> 0xFF8A2A;
               };
               spawn(SPARK, q[0], q[1], q[2], -fx * (0.4 + mv * 0.15) + rnd(0.25F), rnd(0.25F), -fz * (0.4 + mv * 0.15) + rnd(0.25F), 0.9F + R.nextFloat() * 0.6F, 0.16F, 0.02F, R3.argb(c, 1.0), 0.0F, 0.6F);
            }
         }
         case SNOWMO -> {
            if (ground && speed > 0.15F) {
               for (int k = 0; k < (low ? 2 : 5); k++) {
                  double[] q = w(bx, by, bz, yaw, rnd(5.0F), 1.0F, -18.0F);
                  spawn(SNOW, q[0], q[1], q[2], -fx * (1.5 + mv * 0.4) + rnd(0.6F), 1.2 + R.nextFloat() * 1.2, -fz * (1.5 + mv * 0.4) + rnd(0.6F), 0.9F + R.nextFloat() * 0.4F, 0.1F, 0.16F, R3.argb(0xFFFFFF, 0.9), 6.0F, 0.8F);
               }
            }
         }
         default -> {
         }
      }
   }

   // ------------------------------------------------------------------ rendering

   private static boolean tex() {
      if (texReady) return IDS[0] != null;
      texReady = true;
      try {
         for (int i = 0; i < TEX.length; i++) {
            Identifier t = Identifier.fromNamespaceAndPath("legoclient", "vehicle/fx_" + TEX[i]);
            Tx.register(t, AmbientTex.paint(TEX[i]), 128, 128, true);
            IDS[i] = t;
         }
      } catch (Throwable e) {
         IDS[0] = null;
      }
      return IDS[0] != null;
   }

   private static float fade(float k) {
      return k < 0.12F ? k / 0.12F : 1.0F - (k - 0.12F) / 0.88F;
   }

   private static void glow(double x, double y, double z, double size, int rgb, double a) {
      if (a > 0.004) R3.glowBillboard(IDS[GLOW], x, y, z, size, R3.argb(rgb, Math.min(1.0, a)));
   }

   /** Called every frame from {@link VehicleWorld#render(float)} after the vehicle model. */
   static void render(double bx, double by, double bz, float yaw, float time, int light, float speed) {
      if (!tex()) return;
      long now = System.nanoTime();
      float dt = lastNs == 0L ? 0.016F : Math.min(0.1F, (now - lastNs) / 1.0E9F);
      lastNs = now;
      brake = Math.max(0.0F, brake - dt);

      // Particles.
      for (int i = 0; i < MAX; i++) {
         if (AGE[i] >= LIFE[i]) continue;
         AGE[i] += dt;
         float d = Math.max(0.0F, 1.0F - DRAG[i] * dt);
         VX[i] *= d;
         VZ[i] *= d;
         VY[i] = VY[i] * d - GRAV[i] * dt;
         X[i] += VX[i] * dt;
         Y[i] += VY[i] * dt;
         Z[i] += VZ[i] * dt;
         float k = Math.min(1.0F, AGE[i] / LIFE[i]);
         double size = S0[i] + (S1[i] - S0[i]) * k;
         int a = (int)((COL[i] >>> 24) * fade(k));
         if (a <= 1) continue;
         int c = a << 24 | COL[i] & 0xFFFFFF;
         int kind = KIND[i];
         if (kind == GLOW || kind == SPARK) R3.glowBillboard(IDS[kind], X[i], Y[i], Z[i], size, c);
         else R3.billboard(IDS[kind], X[i], Y[i], Z[i], size, c);
      }

      // Skid marks fade out over 10 s.
      for (double[] s : SKID) {
         if (s[5] >= 10.0) continue;
         s[5] += dt;
         double dx = s[2] - s[0], dz = s[3] - s[1];
         double len = Math.sqrt(dx * dx + dz * dz);
         if (len < 1.0E-4 || len > 2.0) continue;
         double nx = -dz / len * 0.07, nz = dx / len * 0.07;
         int c = R3.argb(0x141416, 0.6 * (1.0 - s[5] / 10.0));
         R3.quad(s[0] - nx, s[4], s[1] - nz, s[2] - nx, s[4], s[3] - nz, s[2] + nx, s[4], s[3] + nz, s[0] + nx, s[4], s[1] + nz, c);
         R3.quad(s[0] + nx, s[4], s[1] + nz, s[2] + nx, s[4], s[3] + nz, s[2] - nx, s[4], s[3] - nz, s[0] - nx, s[4], s[1] - nz, c);
      }

      Profile pr = prof;
      if (pr == null || id.isEmpty()) return;
      int sky = light >> 20 & 15, block = light >> 4 & 15;
      boolean dark = VehExtra.isNight() || sky < 8 && block < 8;
      double r = Math.toRadians(yaw);
      double fx = -Math.sin(r), fz = Math.cos(r);

      // Head lights: halo, hot core, a soft volumetric beam and a light pool on the ground.
      if (dark) {
         for (float[] h : pr.head()) {
            double[] q = w(bx, by, bz, yaw, h[0], h[1], h[2]);
            glow(q[0], q[1], q[2], 0.6, 0xFFF1D0, 0.8);
            glow(q[0], q[1], q[2], 0.22, 0xFFFFFF, 1.0);
            for (int k = 0; k < 7; k++) {
               double t = 0.5 * (k + 1);
               glow(q[0] + fx * t, q[1] - t * 0.06, q[2] + fz * t, 0.45 + 0.28 * k, 0xFFE9B8, 0.16 * (1.0 - k / 7.0));
            }
         }
         if (pr.head().length > 0) glow(bx + fx * 3.5, by + 0.06, bz + fz * 3.5, 3.0, 0xFFE9B8, 0.12);
      }

      // Tail lights, brighter when braking.
      for (float[] t : pr.tail()) {
         double[] q = w(bx, by, bz, yaw, t[0], t[1], t[2]);
         double a = brake > 0.0F ? 0.95 : (dark ? 0.55 : 0.0);
         glow(q[0], q[1], q[2], brake > 0.0F ? 0.55 : 0.35, 0xFF2424, a);
         if (brake > 0.0F && dark) glow(q[0] - fx * 0.6, by + 0.06, q[2] - fz * 0.6, 1.6, 0xFF2424, 0.12);
      }

      switch (pr.special()) {
         case SIREN -> {
            int ph = (int)(time * 7.0F) % 6;
            boolean red = ph == 0 || ph == 2, blue = ph == 3 || ph == 5;
            double[] lq = w(bx, by, bz, yaw, -4.0F, 18.5F, -2.0F), rq = w(bx, by, bz, yaw, 4.0F, 18.5F, -2.0F);
            glow(lq[0], lq[1], lq[2], red ? 1.6 : 0.35, 0xFF2030, red ? 0.9 : 0.35);
            glow(rq[0], rq[1], rq[2], blue ? 1.6 : 0.35, 0x2A5BFF, blue ? 0.9 : 0.35);
            if (red || blue) glow(bx, by + 0.06, bz, 6.0, red ? 0xFF2030 : 0x2A5BFF, dark ? 0.16 : 0.07);
         }
         case TAXI -> {
            double[] q = w(bx, by, bz, yaw, 0.0F, 18.5F, -2.0F);
            glow(q[0], q[1], q[2], 0.7, 0xFFD33A, dark ? 0.7 : 0.25);
         }
         case UFO -> {
            double hy = by + VehBoard.UFO_Y / 16.0;
            for (int k = 0; k < 10; k++) {
               double a = time * 2.0 + Math.PI * 2 * k / 10;
               int c = hsv((float)(k / 10.0 + time * 0.1));
               glow(bx + Math.cos(a) * 0.9, hy + 0.05, bz + Math.sin(a) * 0.9, 0.3, c, 0.85);
            }
            for (int k = 0; k < 6; k++) glow(bx, by + 0.05 + k * 0.04, bz, 1.0 - k * 0.08, 0x7CFF9B, 0.1);
         }
         case HOVER -> glow(bx, by + 0.08, bz, 1.6 + 0.2 * Math.sin(time * 6.0), 0x4FC8FF, 0.28);
         case LED -> {
            double[] q = w(bx, by, bz, yaw, 0.0F, 3.0F, 4.5F);
            glow(q[0], q[1], q[2], 0.3, 0x4FC8FF, 0.5 + 0.3 * Math.sin(time * 4.0));
         }
         default -> {
         }
      }

      // Wind streaks at high speed.
      if (pr.fast() && speed > 0.8F && !Performance.lowMode()) {
         float s = (speed - 0.8F) / 0.2F;
         for (int i = 0; i < 6; i++) {
            float ph = (time * 2.2F + i / 6.0F) % 1.0F;
            float side = (i & 1) == 0 ? -1.0F : 1.0F;
            float x = side * (16.0F + i % 3 * 4.0F), y = 6.0F + i * 7 % 14, z = 34.0F - ph * 90.0F;
            double[] a = w(bx, by, bz, yaw, x, y, z), b = w(bx, by, bz, yaw, x, y, z - 16.0F);
            int c = R3.argb(0xFFFFFF, 0.22 * s * Math.sin(ph * Math.PI));
            R3.line(a[0], a[1], a[2], b[0], b[1], b[2], 0.025, c, c & 0xFFFFFF);
         }
      }
   }

   private static int hsv(float h) {
      return java.awt.Color.HSBtoRGB(h - (float)Math.floor(h), 0.65F, 1.0F) & 0xFFFFFF;
   }
}
