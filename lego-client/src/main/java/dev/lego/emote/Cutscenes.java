package dev.lego.emote;

import dev.lego.core.Mc;
import dev.lego.ui.Ease;
import dev.lego.ui.Sound;
import dev.lego.ui.Tx;
import dev.lego.visual.AmbientTex;
import dev.lego.visual.R3;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public final class Cutscenes {
   private static String id;
   private static long start;
   private static float dur;
   private static double ox;
   private static double oy;
   private static double oz;
   private static float baseYaw;
   private static CameraType savedPerspective;
   private static final String BELL = "block.note_block.bell";
   private static final String CHIME = "block.amethyst_block.chime";
   private static final String TWINKLE = "entity.firework_rocket.twinkle";
   private static final String BEAT = "entity.warden.heartbeat";
   private static final String THUNDER = "entity.lightning_bolt.thunder";
   private static final String LAUGH = "entity.witch.celebrate";
   private static final double NE = 0.891;
   private static final double NG = 1.059;
   private static final double NC = 0.707;
   private static final double ND = 0.794;
   private static final Map<String, Cutscenes.Cue[]> CUES = new HashMap<>();
   private static int cue;
   private static Identifier core;
   private static Identifier glow;
   private static Identifier star;
   public static final float[] DA_CUTS;
   public static final float[] EL_CUTS;
   private static Identifier bat0;
   private static Identifier bat1;
   public static final float[] EL_BOLTS;
   private static Identifier page;
   private static Identifier ash;
   private static final int[] XB;
   private static final Map<String, Identifier> TEX;

   private Cutscenes() {
   }

   public static boolean active() {
      return id != null;
   }

   public static String current() {
      return id;
   }

   public static float time() {
      return id == null ? 0.0F : (float)(System.currentTimeMillis() - start) / 1000.0F;
   }

   public static float bars() {
      if (id == null) {
         return 0.0F;
      } else {
         float t = time();
         return Ease.outCubic(Math.min(1.0F, t / 0.5F)) * Math.min(1.0F, Math.max(0.0F, (dur - t) / 0.5F));
      }
   }

   public static String title() {
      EmoteLib.Emote e = id == null ? null : EmoteLib.get(id);
      return e == null ? "" : e.name;
   }

   static void begin(EmoteLib.Emote e) {
      LocalPlayer p = Mc.player();
      if (p != null) {
         end();
         id = e.id;
         dur = e.duration;
         start = System.currentTimeMillis();
         ox = Mc.x(p);
         oy = Mc.y(p);
         oz = Mc.z(p);
         baseYaw = Mc.bodyYaw(p);
         savedPerspective = Mc.perspective();
         Mc.perspective(CameraType.THIRD_PERSON_BACK);
         cue = 0;
         sounds();
      }
   }

   private static Cutscenes.Cue c(double t, String id, double pitch, double vol) {
      return new Cutscenes.Cue((float)t, "minecraft:" + id, (float)pitch, (float)vol);
   }

   public static void sounds() {
      if (id != null) {
         Cutscenes.Cue[] cs = CUES.get(id);
         if (cs != null) {
            float t = time();

            while (cue < cs.length && cs[cue].t <= t) {
               Cutscenes.Cue q = cs[cue++];
               if (t - q.t < 0.6F) {
                  Sound.play(q.id, q.pitch, q.vol);
               }
            }
         }
      }
   }

   static void end() {
      if (id != null) {
         id = null;

         try {
            if (savedPerspective != null) {
               Mc.perspective(savedPerspective);
            }
         } catch (Throwable var1) {
         }

         savedPerspective = null;
      }
   }

   public static boolean camera(float td, double[] out) {
      if (id == null) {
         return false;
      } else {
         LocalPlayer p = Mc.player();
         if (p == null) {
            return false;
         } else {
            double[] pos = Mc.lerpPos(p, td);
            sounds();
            if ("cs_darkaura".equals(id)) {
               return darkAuraCam(pos, time(), out);
            } else if ("cs_evillaugh".equals(id)) {
               return evilCam(pos, time(), out);
            } else {
               double tx = pos[0];
               double ty = pos[1] + 1.1;
               double tz = pos[2];
               float t = time();
               double th = Math.toRadians(baseYaw);
               double fx = -Math.sin(th);
               double fz = Math.cos(th);
               double rx = -Math.cos(th);
               double rz = -Math.sin(th);
               String cx = id;
               double dist;
               double ang;
               double h;
               switch (cx) {
                  case "cs_landing": {
                     float a = Ease.inOutCubic(Math.min(1.0F, t / 1.6F));
                     dist = 9.0 - 5.5 * a;
                     ang = Math.toRadians(160.0F - 150.0F * a);
                     h = 7.0F * (1.0F - a) - 0.6 * a + 0.3 * Math.max(0.0, t - 3.5);
                     if (t > 0.9 && t < 1.3) {
                        h += Math.sin((t - 0.9) * 60.0) * 0.06 * (1.3 - t) / 0.4;
                     }
                     break;
                  }
                  case "cs_victory":
                     dist = 3.6;
                     ang = t / dur * Math.PI * 2.0 + 0.3;
                     h = 0.6 + Math.sin(t * 0.9) * 0.4;
                     break;
                  case "cs_portal": {
                     float a = Ease.inOutCubic(Math.min(1.0F, t / dur));
                     dist = 8.0 - 4.5 * a;
                     ang = Math.toRadians(20.0F - 40.0F * a);
                     h = -0.7 + 0.4 * a;
                     break;
                  }
                  case "cs_power": {
                     float a = Math.min(1.0F, t / 3.8F);
                     dist = 5.0F - 2.0F * a;
                     ang = t * 0.9;
                     h = 0.2;
                     if (t > 3.8 && t < 4.4) {
                        dist += 2.5 * Ease.outCubic((t - 3.8F) / 0.6F);
                     } else if (t >= 4.4) {
                        dist += 2.5;
                     }

                     double shake = t < 3.8 ? a * 0.05 : Math.max(0.0, 0.2 - (t - 3.8) * 0.2);
                     h += Math.sin(t * 47.0F) * shake;
                     ang += Math.cos(t * 53.0F) * shake * 0.3;
                     break;
                  }
                  case "cs_xmas": {
                     float a = Ease.inOutCubic(Math.min(1.0F, t / 3.0F));
                     float b = Ease.inOutCubic(Math.max(0.0F, Math.min(1.0F, (t - 3.2F) / 3.5F)));
                     dist = 2.2 + 3.8 * a + 1.5 * b;
                     ang = Math.toRadians(10.0F + 25.0F * a + 70.0F * b);
                     h = 0.1 + 1.2 * a + 1.4 * b;
                     ty += 0.8 * a;
                     if (t > 3.2 && t < 3.6) {
                        h += Math.sin((t - 3.2) * 70.0) * 0.04;
                     }
                     break;
                  }
                  case "cs_selfie":
                     dist = 1.6;
                     ang = Math.toRadians(-18.0);
                     h = 0.35 + Math.sin(t * 1.5) * 0.03;
                     break;
                  case "cs_starfall":
                     float up = Ease.inOutCubic(Math.min(1.0F, t / 2.5F));
                     float back = Ease.inOutCubic(Math.max(0.0F, (t - 4.8F) / 1.6F));
                     dist = 3.2 + 3.0F * up - 2.0F * back;
                     ang = Math.toRadians(30.0F + t * 8.0F);
                     h = -0.4 - 0.4 * up;
                     ty += 6.0F * up * (1.0F - back);
                     break;
                  default:
                     if (!id.startsWith("cs_gen_")) {
                        return false;
                     }

                     float cycle = t % 6.8F;
                     float cut = (float)Math.floor(cycle / 1.7F);
                     double[] ds = new double[]{3.0, 5.5, 2.2, 7.0};
                     double[] hs = new double[]{0.3, 2.2, -0.5, 1.1};
                     dist = ds[(int)cut];
                     ang = Math.toRadians(-35.0F + cycle * 70.0F + cut * 85.0F);
                     h = hs[(int)cut] + Math.sin(t * 3.0) * 0.08;
               }

               double cx2 = tx + (fx * Math.cos(ang) + rx * Math.sin(ang)) * dist;
               double cz = tz + (fz * Math.cos(ang) + rz * Math.sin(ang)) * dist;
               double cy = pos[1] + 1.3 + h;
               double dx = tx - cx2;
               double dy = ty - cy;
               double dz = tz - cz;
               out[0] = cx2;
               out[1] = cy;
               out[2] = cz;
               out[3] = Math.toDegrees(Math.atan2(-dx, dz));
               out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
               return true;
            }
         }
      }
   }

   public static void renderFx(float td) {
      if (id != null) {
         LocalPlayer p = Mc.player();
         if (p != null) {
            if (core == null) {
               core = tex("core");
               glow = tex("glow");
               star = tex("glitter");
            }

            double[] pos = Mc.lerpPos(p, td);
            double x = pos[0];
            double y = pos[1];
            double z = pos[2];
            float t = time();
            String var10 = id;
            switch (var10) {
               case "cs_landing":
                  if (t < 1.0) {
                     double fy = y + 10.0 * (1.0 - t / 1.0);
                     R3.line(x, fy + 3.0, z, x, fy, z, 0.35, R3.argb(10475775, 0.0), R3.argb(16777215, 0.8));
                  }

                  double s = t - 1.0;
                  if (s > 0.0 && s < 1.6) {
                     double r = 0.4 + s * 5.0;
                     double a = 1.0 - s / 1.6;
                     R3.ring(x, y + 0.05, z, r * 0.85, r, 48, R3.argb(12576511, 0.0), R3.argb(16777215, 0.8 * a), 0.0);
                     R3.ring(x, y + 0.04, z, 0.0, r * 0.85, 48, R3.argb(8370431, 0.25 * a), R3.argb(8370431, 0.0), 0.0);

                     for (int i = 0; i < 40; i++) {
                        double an = i * 2.399;
                        double sp = 1.5 + i % 7 * 0.4;
                        double dx = Math.cos(an) * sp * s;
                        double dz = Math.sin(an) * sp * s;
                        double dy = 1.8 * s - 2.2 * s * s;
                        if (dy < 0.0) {
                           dy = 0.0;
                        }

                        R3.billboard(glow, x + dx, y + 0.1 + dy, z + dz, 0.35, R3.argb(13154458, 0.55 * a));
                     }
                  }
                  break;
               case "cs_victory":
                  for (int k = 0; k < 6; k++) {
                     fireworks(x, y, z, t - 0.4 - k * 0.85, k);
                  }
                  break;
               case "cs_portal":
                  double a = Math.min(1.0, t / 1.5) * Math.min(1.0, (dur - t) / 0.6);
                  double rise = Math.min(1.0, t / 3.0);

                  for (int ring = 0; ring < 3; ring++) {
                     double ry = y + 0.05 + ring * 0.8 * rise;
                     R3.ring(x, ry, z, 1.2 + ring * 0.2, 1.4 + ring * 0.2, 48, R3.argb(13134847, 0.7 * a), R3.argb(5975039, 0.2 * a), t * (1.5 + ring));
                  }

                  for (int i = 0; i < 60; i++) {
                     double an = i * 0.61 + t * 2.2;
                     double r = 1.3 + Math.sin(i * 1.7 + t) * 0.2;
                     double hy = (i * 0.37 + t * 0.8) % 3.0;
                     R3.glowBillboard(core, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.12, R3.argb(14721279, 0.9 * a));
                  }

                  if (t > 2.5) {
                     double b = Math.min(1.0, (t - 2.5) / 0.6) * a;
                     R3.line(x, y, z, x, y + 60.0, z, 1.6 * b, R3.argb(14267391, 0.6 * b), R3.argb(8076287, 0.0));
                     R3.line(x, y, z, x, y + 60.0, z, 0.5 * b, R3.argb(16777215, 0.9 * b), R3.argb(16777215, 0.0));
                  }
                  break;
               case "cs_power":
                  double charge = Math.min(1.0, t / 3.8);
                  boolean burst = t > 3.8;

                  for (int i = 0; i < 70; i++) {
                     double an = i * 2.399 + t * 3.0F;
                     double life = (i * 0.173 + t * 1.6) % 1.0;
                     double r = 0.55 + 0.25 * Math.sin(i + t * 4.0F);
                     double hy = life * 2.4;
                     double a1 = (1.0 - life) * charge * (burst ? Math.max(0.0, 1.0 - (t - 3.8) / 1.5) : 1.0);
                     int c = i % 3 == 0 ? 16774048 : 16756782;
                     R3.glowBillboard(glow, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.5 * (1.0 - life * 0.5), R3.argb(c, 0.55 * a1));
                  }

                  if (burst) {
                     double s2 = t - 3.8;
                     double a2 = Math.max(0.0, 1.0 - s2 / 1.2);
                     R3.ring(x, y + 1.0, z, s2 * 8.0, s2 * 8.0 + 0.6, 64, R3.argb(16769162, 0.0), R3.argb(16777215, a2), 0.0);
                     R3.glowBillboard(glow, x, y + 1.0, z, 2.0 + s2 * 12.0, R3.argb(16765040, 0.6 * a2));
                  }

                  if (!burst && (int)(t * 7.0F) % 3 == 0) {
                     double an = Math.floor(t * 7.0F) * 1.3;
                     double x1 = x + Math.cos(an) * 0.9;
                     double z1 = z + Math.sin(an) * 0.9;
                     R3.line(
                        x1,
                        y + 2.2,
                        z1,
                        x + Math.cos(an + 0.4) * 0.6,
                        y + 0.8,
                        z + Math.sin(an + 0.4) * 0.6,
                        0.06,
                        R3.argb(16777215, 0.9),
                        R3.argb(16769136, 0.9)
                     );
                  }
                  break;
               case "cs_selfie":
                  if (t > dur - 0.9 && t < dur - 0.5) {
                     double a3 = 1.0 - Math.abs((t - (dur - 0.7)) / 0.2);
                     R3.glowBillboard(glow, x, y + 1.4, z, 6.0, R3.argb(16777215, Math.max(0.0, a3)));
                  }

                  for (int i = 0; i < 6; i++) {
                     double an = t * 1.5 + i;
                     double hy = 1.6 + (t * 0.5 + i * 0.3) % 1.2;
                     R3.billboardRot(star, x + Math.cos(an) * 0.7, y + hy, z + Math.sin(an) * 0.7, 0.18, t * 3.0F + i, R3.argb(16770290, 0.9));
                  }
                  break;
               case "cs_xmas":
                  xmasFx(x, y, z, t);
                  break;
               case "cs_darkaura":
                  darkAuraFx(x, y, z, t);
                  break;
               case "cs_evillaugh":
                  evilFx(x, y, z, t);
                  break;
               case "cs_starfall":
                  for (int i = 0; i < 14; i++) {
                     double period = 2.2;
                     double local = (t + i * 0.37) % period / period;
                     double an = i * 2.1;
                     double r = 6 + i % 5 * 3;
                     double sx = x + Math.cos(an) * r;
                     double sz = z + Math.sin(an) * r;
                     double sy = y + 30.0 - local * 26.0;
                     double hx = sx - local * 4.0;
                     R3.line(hx + 3.0, sy + 3.0, sz, hx, sy, sz, 0.18, R3.argb(11061503, 0.0), R3.argb(16777215, 0.9 * (1.0 - local)));
                     R3.glowBillboard(core, hx, sy, sz, 0.8, R3.argb(14214911, 0.9 * (1.0 - local)));
                  }

                  for (int i = 0; i < 30; i++) {
                     double an = i * 2.399 + t * 0.5;
                     double r = 0.6 + i % 5 * 0.35;
                     double hy = (i * 0.21 + t * 0.4) % 2.5;
                     R3.billboardRot(star, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.1, t + i, R3.argb(16777215, 0.8));
                  }
                  break;
               default:
                  if (id.startsWith("cs_gen_")) {
                     double a4 = Math.min(1.0, t / 0.6) * Math.min(1.0, Math.max(0.0, (dur - t) / 0.6));

                     for (int i = 0; i < 48; i++) {
                        double an = i * 2.399 + t * 1.8;
                        double r = 0.8 + i % 8 * 0.35;
                        double yy = (i * 0.17 + t * 0.9) % 2.8;
                        R3.glowBillboard(glow, x + Math.cos(an) * r, y + yy, z + Math.sin(an) * r, 0.18 + i % 3 * 0.05, R3.argb(11065599, 0.65 * a4));
                     }

                     double pulse = Math.max(0.0, 1.0 - Math.min(1.0, t / 1.0));
                     R3.ring(
                        x,
                        y + 0.15,
                        z,
                        1.0 + Math.sin(t * 4.0F) * 0.2,
                        2.0 + Math.sin(t * 4.0F) * 0.3,
                        48,
                        R3.argb(16777215, 0.0),
                        R3.argb(10190079, 0.45 * a4 * pulse),
                        t * 1.2
                     );
                  }
            }
         }
      }
   }

   private static void fireworks(double x, double y, double z, double s, int k) {
      if (!(s < 0.0) && !(s > 2.2)) {
         int[] cols = new int[]{16731501, 16765503, 4182783, 10181631, 6029211, 16747069};
         int c = cols[k % cols.length];
         double an0 = k * 1.9;
         double cx = x + Math.cos(an0) * 4.0;
         double cz = z + Math.sin(an0) * 4.0;
         double cy = y + 7.0 + k % 3;
         if (s < 0.5) {
            double f = s / 0.5;
            R3.line(cx, y + f * (cy - y) - 1.2, cz, cx, y + f * (cy - y), cz, 0.12, R3.argb(16767392, 0.0), R3.argb(16777215, 0.9));
         } else {
            double e = s - 0.5;
            double a = Math.max(0.0, 1.0 - e / 1.7);

            for (int i = 0; i < 48; i++) {
               double u = (i + 0.5) / 48.0;
               double phi = Math.acos(1.0 - 2.0 * u);
               double th = i * 2.399;
               double sp = 4.5 * (1.0 - Math.exp(-e * 3.0));
               double px = cx + Math.sin(phi) * Math.cos(th) * sp;
               double py = cy + Math.cos(phi) * sp - e * e * 1.2;
               double pz = cz + Math.sin(phi) * Math.sin(th) * sp;
               R3.glowBillboard(core, px, py, pz, 0.35, R3.argb(c, a));
            }

            R3.glowBillboard(glow, cx, cy, cz, 6.0 * a, R3.argb(c, 0.3 * a));
         }
      }
   }

   private static float[] cuts() {
      if ("cs_darkaura".equals(id)) {
         return DA_CUTS;
      } else {
         return "cs_evillaugh".equals(id) ? EL_CUTS : null;
      }
   }

   public static int shot() {
      float[] cs = cuts();
      if (cs == null) {
         return -1;
      } else {
         float t = time();
         int s = 0;

         for (int i = 0; i < cs.length; i++) {
            if (t >= cs[i]) {
               s = i;
            }
         }

         return s;
      }
   }

   public static float shotTime() {
      int s = shot();
      return s < 0 ? 0.0F : time() - cuts()[s];
   }

   public static float lockYaw() {
      return baseYaw;
   }

   public static boolean locksYaw() {
      return "cs_darkaura".equals(id) || "cs_evillaugh".equals(id);
   }

   static double[] eye(double x, double y, double z) {
      double th = Math.toRadians(baseYaw);
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double rx = -Math.cos(th);
      double rz = -Math.sin(th);
      return new double[]{x + fx * 0.245 + rx * 0.117, y + 1.611, z + fz * 0.245 + rz * 0.117};
   }

   private static boolean darkAuraCam(double[] pos, float t, double[] out) {
      double th = Math.toRadians(baseYaw);
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double rx = -Math.cos(th);
      double rz = -Math.sin(th);
      double x = pos[0];
      double y = pos[1];
      double z = pos[2];
      int s = shot();
      float l = shotTime();
      double cx;
      double cy;
      double cz;
      double lx;
      double ly;
      double lz;
      switch (s) {
         case 0: {
            double d = 5.6 - l * 0.55;
            double a = Math.toRadians(28.0F - l * 8.0F);
            cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d;
            cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
            cy = y + 0.35;
            lx = x;
            ly = y + 1.25;
            lz = z;
            break;
         }
         case 1:
            double side = 0.9 - l * 0.5;
            cx = x + fx * 1.5 + rx * side;
            cz = z + fz * 1.5 + rz * side;
            cy = y + 0.22;
            lx = x + rx * 0.1;
            ly = y + 0.2;
            lz = z + rz * 0.1;
            break;
         case 2: {
            double d = 1.35 - l * 0.12;
            cx = x + rx * d + fx * 0.15;
            cz = z + rz * d + fz * 0.15;
            cy = y + 1.55 + l * 0.04;
            lx = x + fx * 0.1;
            ly = y + 1.55;
            lz = z + fz * 0.1;
            break;
         }
         case 3: {
            double[] e = eye(x, y, z);
            double d = 0.55 - Math.min(1.0, l / 1.3) * 0.14;
            double tr = Math.max(0.0, l - 0.35) * 0.0025;
            cx = e[0] + fx * d + Math.sin(t * 61.0F) * tr;
            cz = e[2] + fz * d + Math.cos(t * 57.0F) * tr;
            cy = e[1] + Math.sin(t * 53.0F) * tr;
            lx = e[0];
            ly = e[1];
            lz = e[2];
            break;
         }
         case 4: {
            double sh = Math.max(0.0, 0.18 - l * 0.2);
            double a = Math.toRadians(-20.0);
            cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * 2.6 + Math.sin(t * 71.0F) * sh;
            cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * 2.6;
            cy = y + 1.2 + Math.cos(t * 67.0F) * sh;
            lx = x;
            ly = y + 1.15;
            lz = z;
            break;
         }
         default: {
            float k = Ease.inOutCubic(Math.min(1.0F, l / 1.5F));
            double d = 2.8 + 3.6 * k;
            double a = Math.toRadians(-20.0F + 50.0F * k);
            cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d;
            cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
            cy = y + 1.2 + 1.6 * k;
            lx = x;
            ly = y + 1.1;
            lz = z;
         }
      }

      double dx = lx - cx;
      double dy = ly - cy;
      double dz = lz - cz;
      out[0] = cx;
      out[1] = cy;
      out[2] = cz;
      out[3] = Math.toDegrees(Math.atan2(-dx, dz));
      out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
      return true;
   }

   private static void darkAuraFx(double x, double y, double z, float t) {
      if (bat0 == null) {
         bat0 = tex("bat0");
         bat1 = tex("bat1");
      }

      double fadeIn = Math.min(1.0, t / 0.6);
      double fadeOut = Math.min(1.0, Math.max(0.0, (dur - t) / 0.8));
      double a0 = fadeIn * fadeOut;
      boolean burst = t > DA_CUTS[4];
      double bt = burst ? t - DA_CUTS[4] : 0.0;

      for (int i = 0; i < 110; i++) {
         double seed = i * 2.399;
         double life = (i * 0.137 + t * (0.28 + i % 5 * 0.04)) % 1.0;
         double r = 0.35 + i % 7 * 0.26 + life * 0.8 + bt * 1.5;
         double an = seed + t * 0.4 * ((i & 1) == 0 ? 1 : -1) + life * 1.3;
         double hy = life * (2.4 + i % 4 * 0.4);
         double sz = 0.5 + life * 1.3 + i % 3 * 0.2;
         double a = Math.sin(life * Math.PI) * a0 * (burst ? Math.max(0.35, 1.0 - bt * 0.5) : 1.0);
         int c = i % 6 == 0 ? 3801096 : (i % 4 == 0 ? 1442822 : 328196);
         R3.billboard(glow, x + Math.cos(an) * r, y + 0.05 + hy, z + Math.sin(an) * r, sz, R3.argb(c, 0.62 * a));
      }

      R3.ring(x, y + 0.03, z, 0.0, 2.4 + bt * 3.0, 48, R3.argb(0, 0.7 * a0), R3.argb(0, 0.0), 0.0);
      R3.ring(x, y + 0.04, z, 1.5, 1.62, 64, R3.argb(16715824, 0.55 * a0), R3.argb(8388624, 0.25 * a0), t * 0.6);

      for (int i = 0; i < 26; i++) {
         double life = (i * 0.31 + t * 0.5) % 1.0;
         double an = i * 1.7 + t * 0.8;
         double r = 0.5 + i % 5 * 0.35;
         R3.glowBillboard(core, x + Math.cos(an) * r, y + life * 3.0, z + Math.sin(an) * r, 0.06, R3.argb(16722490, 0.9 * (1.0 - life) * a0));
      }

      for (int i = 0; i < 30; i++) {
         double sp = 1.1 + i % 5 * 0.25;
         double an = i * 2.1 + t * sp * (i % 3 == 0 ? -1 : 1);
         double r = 1.3 + i % 6 * 0.45 + Math.sin(t * 2.0F + i) * 0.25 + bt * bt * 4.0;
         double hy = 0.4 + i % 7 * 0.45 + Math.sin(t * 3.1 + i * 1.3) * 0.25 + bt * 1.2;
         Identifier f = (int)(t * 13.0F + i) % 2 == 0 ? bat0 : bat1;
         R3.billboard(f, x + Math.cos(an) * r, y + hy, z + Math.sin(an) * r, 0.34 + i % 3 * 0.08, R3.argb(1706514, 0.95 * a0));
      }

      if (t > DA_CUTS[2] + 0.6) {
         double e = Math.min(1.0, (t - DA_CUTS[2] - 0.6) / 0.5) * fadeOut;
         double flick = 0.85 + 0.15 * Math.sin(t * 31.0F);
         double[] ep = eye(x, y, z);
         R3.glowBillboard(glow, ep[0], ep[1], ep[2], 0.32 * e * flick, R3.argb(16714270, 0.8 * e));
         R3.glowBillboard(core, ep[0], ep[1], ep[2], 0.07 * e, R3.argb(16765136, e));
         if (shot() >= 4) {
            R3.line(
               ep[0],
               ep[1],
               ep[2],
               ep[0] - Math.cos(Math.toRadians(baseYaw)) * 0.6,
               ep[1] + 0.05,
               ep[2] - Math.sin(Math.toRadians(baseYaw)) * 0.6,
               0.04,
               R3.argb(16715824, 0.8 * e),
               R3.argb(16715824, 0.0)
            );
         }
      }

      if (burst && bt < 0.7) {
         double k = bt / 0.7;
         double th = Math.toRadians(baseYaw);
         double fx = -Math.sin(th);
         double fz = Math.cos(th);
         double rx = -Math.cos(th);
         double rz = -Math.sin(th);

         for (int s = 0; s < 3; s++) {
            double off = (s - 1) * 0.35;
            double px = 0.0;
            double py = 0.0;
            double pz = 0.0;

            for (int j = 0; j <= 16; j++) {
               double u = j / 16.0 * Math.min(1.0, k * 3.0);
               double an = -1.2 + u * 2.4;
               double qx = x + fx * (1.0 + off * 0.3) + rx * Math.sin(an) * 1.6;
               double qy = y + 1.2 + Math.cos(an) * 0.9 + off;
               double qz = z + fz * (1.0 + off * 0.3) + rz * Math.sin(an) * 1.6;
               if (j > 0) {
                  R3.line(px, py, pz, qx, qy, qz, 0.09 * (1.0 - k), R3.argb(16715824, 0.9 * (1.0 - k)), R3.argb(16765136, 0.9 * (1.0 - k)));
               }

               px = qx;
               py = qy;
               pz = qz;
            }
         }
      }
   }

   private static float[] pose() {
      EmoteLib.Emote e = EmoteLib.get(id);
      return e == null ? null : e.pose(time());
   }

   static double[] eyeAt(double x, double y, double z) {
      float[] p = pose();
      double hp = p != null && !Float.isNaN(p[0]) ? p[0] : 0.0;
      double b = p != null && !Float.isNaN(p[3]) ? p[3] : 0.0;
      double th = Math.toRadians(baseYaw);
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double rx = -Math.cos(th);
      double rz = -Math.sin(th);
      double neckUp = 0.75 + 0.75 * Math.cos(b);
      double neckFwd = 0.75 * Math.sin(b);
      double a = hp + b;
      double up = neckUp + 0.111 * Math.cos(a) - 0.245 * Math.sin(a);
      double fwd = neckFwd + 0.245 * Math.cos(a) + 0.111 * Math.sin(a);
      return new double[]{x + fx * fwd + rx * 0.117, y + up, z + fz * fwd + rz * 0.117};
   }

   private static boolean evilCam(double[] pos, float t, double[] out) {
      double th = Math.toRadians(baseYaw);
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double rx = -Math.cos(th);
      double rz = -Math.sin(th);
      double x = pos[0];
      double y = pos[1];
      double z = pos[2];
      int s = shot();
      float l = shotTime();
      double cx;
      double cy;
      double cz;
      double lx;
      double ly;
      double lz;
      switch (s) {
         case 0: {
            float k = Ease.inOutCubic(Math.min(1.0F, l / 2.6F));
            double d = 5.4 - 2.3 * k;
            double a = Math.toRadians(24.0F - 16.0F * k);
            cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d;
            cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
            cy = y + 0.9 + 0.15 * k;
            lx = x;
            ly = y + 1.35 + 0.1 * k;
            lz = z;
            break;
         }
         case 1: {
            double d = 1.05 - Math.min(1.0, l / 1.8) * 0.15;
            double tr = 0.012;
            cx = x + fx * d + rx * 0.18 + Math.sin(t * 43.0F) * tr;
            cz = z + fz * d + rz * 0.18 + Math.cos(t * 39.0F) * tr;
            cy = y + 0.95 + Math.sin(t * 47.0F) * tr;
            lx = x + fx * 0.05;
            ly = y + 1.72;
            lz = z + fz * 0.05;
            break;
         }
         case 2: {
            double[] e = eyeAt(x, y, z);
            double d = 0.52 - Math.min(1.0, l / 1.5) * 0.14;
            double tr = Math.max(0.0, l - 0.5) * 0.0028;
            cx = e[0] + fx * d + Math.sin(t * 61.0F) * tr;
            cz = e[2] + fz * d + Math.cos(t * 57.0F) * tr;
            cy = e[1] + Math.sin(t * 53.0F) * tr;
            lx = e[0];
            ly = e[1];
            lz = e[2];
            break;
         }
         default: {
            float k = Ease.inOutCubic(Math.min(1.0F, l / 1.4F));
            float spin = Ease.inOutCubic(Math.min(1.0F, l / 4.0F));
            double d = 2.6 + 3.0 * k;
            double a = 8.168140899333462 * spin + Math.toRadians(10.0);
            double sh = Math.max(0.0, 0.12 - l * 0.12) + bolt(t) * 0.06;
            cx = x + (fx * Math.cos(a) + rx * Math.sin(a)) * d + Math.sin(t * 71.0F) * sh;
            cz = z + (fz * Math.cos(a) + rz * Math.sin(a)) * d;
            cy = y + 0.45 + 1.3 * k + Math.cos(t * 67.0F) * sh;
            lx = x;
            ly = y + 1.25;
            lz = z;
         }
      }

      double dx = lx - cx;
      double dy = ly - cy;
      double dz = lz - cz;
      out[0] = cx;
      out[1] = cy;
      out[2] = cz;
      out[3] = Math.toDegrees(Math.atan2(-dx, dz));
      out[4] = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
      return true;
   }

   public static float bolt(float t) {
      float f = 0.0F;

      for (float b : EL_BOLTS) {
         if (t >= b && t < b + 0.45F) {
            f = Math.max(f, (float)Math.exp(-(t - b) * 9.0F) * (0.6F + 0.4F * (float)Math.abs(Math.sin((t - b) * 60.0F))));
         }
      }

      return f;
   }

   public static double heartbeat(float t) {
      double v = 0.0;

      for (Cutscenes.Cue q : CUES.get("cs_evillaugh")) {
         if (q.id().endsWith("heartbeat")) {
            double d = t - q.t();
            if (d >= 0.0 && d < 0.7) {
               v = Math.max(v, Math.exp(-d * 9.0) + 0.6 * (d > 0.22 ? Math.exp(-(d - 0.22) * 9.0) : 0.0));
            }
         }
      }

      return Math.min(1.0, v);
   }

   private static void arc(double x1, double y1, double z1, double x2, double y2, double z2, long seed, int segs, double jit, double w, double a) {
      Random r = new Random(seed);
      double px = x1;
      double py = y1;
      double pz = z1;

      for (int i = 1; i <= segs; i++) {
         double u = (double)i / segs;
         double j = i == segs ? 0.0 : jit * Math.sin(u * Math.PI);
         double qx = x1 + (x2 - x1) * u + (r.nextDouble() - 0.5) * 2.0 * j;
         double qy = y1 + (y2 - y1) * u + (r.nextDouble() - 0.5) * j;
         double qz = z1 + (z2 - z1) * u + (r.nextDouble() - 0.5) * 2.0 * j;
         R3.line(px, py, pz, qx, qy, qz, w * 3.0, R3.argb(16714270, 0.35 * a), R3.argb(16714270, 0.35 * a));
         R3.line(px, py, pz, qx, qy, qz, w, R3.argb(16769252, a), R3.argb(16736368, a));
         if (i == segs / 2 && r.nextBoolean()) {
            double bx = qx + (r.nextDouble() - 0.5) * jit * 3.0;
            double by = qy - jit * 1.5;
            double bz = qz + (r.nextDouble() - 0.5) * jit * 3.0;
            R3.line(qx, qy, qz, bx, by, bz, w * 0.6, R3.argb(16744592, a), R3.argb(16715824, 0.0));
         }

         px = qx;
         py = qy;
         pz = qz;
      }
   }

   private static void evilFx(double x, double y, double z, float t) {
      if (page == null) {
         page = tex("confetti");
         ash = tex("ash");
      }

      double fadeIn = Math.min(1.0, t / 0.6);
      double fadeOut = Math.min(1.0, Math.max(0.0, (dur - t) / 0.8));
      double a0 = fadeIn * fadeOut;
      double hb = heartbeat(t);
      int s = shot();
      boolean wide = s >= 3;
      double wt = wide ? t - EL_CUTS[3] : 0.0;

      for (int i = 0; i < 80; i++) {
         double life = (i * 0.137 + t * (0.32 + i % 5 * 0.05)) % 1.0;
         double an = i * 2.399 + t * 0.5 * ((i & 1) == 0 ? 1 : -1) + life * 1.6;
         double r = 0.3 + i % 6 * 0.14 + life * 0.5 + hb * 0.25 + (wide ? Math.min(1.0, wt) * 0.6 : 0.0);
         double sz = 0.45 + life * 1.1 + hb * 0.3;
         double a = Math.sin(life * Math.PI) * a0 * (0.75 + 0.25 * hb);
         int c = i % 5 == 0 ? 4849676 : (i % 3 == 0 ? 1704710 : 262402);
         R3.billboard(glow, x + Math.cos(an) * r, y + 0.05 + life * (2.2 + i % 3 * 0.3), z + Math.sin(an) * r, sz, R3.argb(c, 0.6 * a));
      }

      R3.glowBillboard(glow, x, y + 1.0, z, 2.6 + hb * 0.8, R3.argb(11534362, (0.18 + 0.22 * hb) * a0));
      R3.ring(x, y + 0.03, z, 0.0, 2.6, 48, R3.argb(0, 0.75 * a0), R3.argb(0, 0.0), 0.0);
      R3.ring(x, y + 0.04, z, 1.3 + hb * 0.12, 1.4 + hb * 0.16, 64, R3.argb(16715824, (0.45 + 0.4 * hb) * a0), R3.argb(8388624, 0.2 * a0), -t * 0.4);
      R3.ring(x, y + 0.045, z, 1.85, 1.9, 64, R3.argb(16715824, 0.3 * a0), R3.argb(16715824, 0.1 * a0), t * 0.25);
      if (hb > 0.05) {
         R3.ring(x, y + 0.05, z, 0.4 + (1.0 - hb) * 2.5, 0.5 + (1.0 - hb) * 2.6, 48, R3.argb(16719936, 0.0), R3.argb(16719936, 0.5 * hb * a0), 0.0);
      }

      for (int i = 0; i < 90; i++) {
         double life = (i * 0.083 + t * (0.09 + i % 4 * 0.015)) % 1.0;
         double an = i * 2.399;
         double r = 0.5 + i % 11 * 0.45;
         double ax = x + Math.cos(an) * r + Math.sin(t * 0.8 + i) * 0.3;
         double az = z + Math.sin(an) * r + Math.cos(t * 0.7 + i) * 0.3;
         R3.billboardRot(
            ash, ax, y + 5.5 - life * 5.6, az, 0.06 + i % 3 * 0.03, t * 0.6 + i, R3.argb(i % 7 == 0 ? 5904926 : 2763310, 0.85 * a0 * Math.min(1.0, life * 5.0))
         );
      }

      int np = wide ? 44 : 12;

      for (int i = 0; i < np; i++) {
         double sz = 0.2 + i % 4 * 0.04;
         double px;
         double py;
         double pz;
         double ang;
         if (!wide) {
            double life = (i * 0.27 + t * 0.11) % 1.0;
            double an = i * 2.1;
            double r = 1.4 + i % 4 * 0.6;
            px = x + Math.cos(an) * r + Math.sin(t * 1.3 + i) * 0.35;
            pz = z + Math.sin(an) * r + Math.cos(t * 1.1 + i) * 0.35;
            py = y + 3.6 - life * 3.5;
            ang = Math.sin(t * 2.0F + i) * 1.2;
         } else {
            double k = Math.min(1.0, wt / 0.6);
            double sp = 1.6 + i % 5 * 0.3;
            double an = i * 2.399 + wt * sp * (1.0 + 0.8 * Math.max(0.0, 1.0 - wt));
            double r = 0.5 + k * (1.1 + i % 7 * 0.35) + Math.sin(wt * 2.0 + i) * 0.2;
            px = x + Math.cos(an) * r;
            pz = z + Math.sin(an) * r;
            py = y + 0.3 + (i * 0.23 + wt * (0.35 + i % 3 * 0.1)) % 1.0 * 3.4 * k + (1.0 - k) * 1.0;
            ang = wt * (4 + i % 5) + i;
         }

         double a = a0 * (wide ? 1.0 : Math.min(1.0, t / 1.0));
         R3.glowBillboard(page, px, py, pz, sz * 1.25, R3.argb(16715824, 0.35 * a));
         R3.billboardRot(page, px, py, pz, sz, ang, R3.argb(657416, 0.95 * a));
      }

      for (float b : EL_BOLTS) {
         float d = t - b;
         if (!(d < 0.0F) && !(d > 0.4F)) {
            double fl = Math.exp(-d * 7.0F) * (0.5 + 0.5 * Math.abs(Math.sin(d * 55.0F)));
            double an = b * 2.3;
            double r = b == EL_BOLTS[0] ? 7.0 : 3.2;
            double sx = x + Math.cos(an) * r;
            double sz2 = z + Math.sin(an) * r;
            arc(sx + 1.5, y + 18.0, sz2 - 1.0, sx, y, sz2, (long)(b * 100.0F) + (long)(d * 20.0F), 14, 0.9, 0.12, fl * a0);
            R3.glowBillboard(glow, sx, y + 0.3, sz2, 3.0 * fl, R3.argb(16719936, 0.6 * fl * a0));
         }
      }

      if (s == 1 || wide) {
         int n = wide ? 3 : 1;

         for (int k = 0; k < n; k++) {
            long seed = (long)(t * 14.0F) * 7L + k;
            Random r = new Random(seed);
            if (!(r.nextDouble() > (wide ? 0.75 : 0.4))) {
               double an = r.nextDouble() * Math.PI * 2.0;
               double an2 = an + 0.6 + r.nextDouble();
               double r1 = 0.6 + r.nextDouble() * 0.5;
               double r2 = 0.8 + r.nextDouble() * 0.9;
               arc(
                  x + Math.cos(an) * r1,
                  y + 1.4 + r.nextDouble() * 0.9,
                  z + Math.sin(an) * r1,
                  x + Math.cos(an2) * r2,
                  y + 0.1 + r.nextDouble() * 0.5,
                  z + Math.sin(an2) * r2,
                  seed,
                  7,
                  0.22,
                  0.035,
                  0.9 * a0
               );
            }
         }
      }

      if (t > EL_CUTS[2] - 0.1) {
         double e = Math.min(1.0, (t - EL_CUTS[2] + 0.1) / 0.5) * fadeOut;
         double flick = 0.85 + 0.15 * Math.sin(t * 31.0F);
         double[] ep = eyeAt(x, y, z);
         R3.glowBillboard(glow, ep[0], ep[1], ep[2], (0.32 + 0.1 * hb) * e * flick, R3.argb(16714270, 0.85 * e));
         R3.glowBillboard(core, ep[0], ep[1], ep[2], 0.07 * e, R3.argb(16765136, e));
         if (wide) {
            double th = Math.toRadians(baseYaw);
            double rx = -Math.cos(th);
            double rz = -Math.sin(th);
            R3.line(ep[0], ep[1], ep[2], ep[0] + rx * 0.7, ep[1] + 0.08, ep[2] + rz * 0.7, 0.045, R3.argb(16715824, 0.85 * e), R3.argb(16715824, 0.0));
         }
      }
   }

   private static void cone(double x, double y0, double z, double r, double h, int seg, int cBottom, int cTop) {
      for (int i = 0; i < seg; i++) {
         double a0 = i * Math.PI * 2.0 / seg;
         double a1 = (i + 1) * Math.PI * 2.0 / seg;
         double x0 = x + Math.cos(a0) * r;
         double z0 = z + Math.sin(a0) * r;
         double x1 = x + Math.cos(a1) * r;
         double z1 = z + Math.sin(a1) * r;
         int shade = i % 2 == 0
            ? cBottom
            : R3.argb((cBottom >> 16 & 0xFF) * 9 / 10 << 16 | (cBottom >> 8 & 0xFF) * 9 / 10 << 8 | (cBottom & 0xFF) * 9 / 10, (cBottom >>> 24) / 255.0);
         R3.quad(x0, y0, z0, x1, y0, z1, x, y0 + h, z, x, y0 + h, z, shade, shade, cTop, cTop);
         R3.quad(x0, y0, z0, x1, y0, z1, x, y0 + 0.05, z, x, y0 + 0.05, z, cBottom, cBottom, cBottom, cBottom);
      }
   }

   private static void box(double x0, double y0, double z0, double x1, double y1, double z1, int top, int side) {
      R3.quad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, top);
      R3.quad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, side);
      R3.quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, side);
      R3.quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, side);
      R3.quad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, side);
   }

   private static void xmasFx(double x, double y, double z, float t) {
      double th = Math.toRadians(baseYaw);
      double fx = -Math.sin(th);
      double fz = Math.cos(th);
      double out = Math.min(1.0, Math.max(0.0, (dur - t) / 0.8));
      double tx = x - fx * 2.2;
      double tz = z - fz * 2.2;
      double grow = Ease.outBack(Math.max(0.0F, Math.min(1.0F, (t - 0.5F) / 2.0F)));
      if (grow > 0.01) {
         double sc = grow;
         box(tx - 0.15 * grow, y, tz - 0.15 * grow, tx + 0.15 * grow, y + 0.5 * grow, tz + 0.15 * grow, R3.argb(5913118, out), R3.argb(4861462, out));
         double[][] tiers = new double[][]{{0.45, 1.3, 1.2}, {1.05, 1.05, 1.0}, {1.6, 0.8, 0.85}, {2.1, 0.5, 0.7}};

         for (double[] tr : tiers) {
            cone(tx, y + tr[0] * sc, tz, tr[1] * sc, tr[2] * sc, 20, R3.argb(944692, out), R3.argb(3127391, out));
         }

         for (double[] tr : tiers) {
            R3.ring(tx, y + tr[0] * sc + 0.02, tz, tr[1] * sc * 0.82, tr[1] * sc * 0.98, 28, R3.argb(16777215, 0.55 * out), R3.argb(15266559, 0.2 * out), 0.0);
         }

         double lit = Math.max(0.0, Math.min(1.0, (t - 2.2) / 1.0));
         int n = 46;

         for (int i = 0; i < n * lit; i++) {
            double u = (double)i / n;
            double hy = 0.5 + u * 2.3;
            double r = (1.3 - u * 1.15) * 0.98;
            double an = u * Math.PI * 9.0 + 0.4;
            double tw = 0.6 + 0.4 * Math.sin(t * 5.0F + i * 1.7);
            int c = XB[i % XB.length];
            double px = tx + Math.cos(an) * r * sc;
            double py = y + hy * sc;
            double pz = tz + Math.sin(an) * r * sc;
            R3.glowBillboard(glow, px, py, pz, 0.28 * tw, R3.argb(c, 0.55 * out));
            R3.glowBillboard(core, px, py, pz, 0.07, R3.argb(c, out));
         }

         double st = Math.max(0.0, Math.min(1.0, (t - 3.1) / 0.3));
         if (st > 0.0) {
            double ty = y + 2.95 * sc;
            double pul = 0.85 + 0.15 * Math.sin(t * 6.0F);
            R3.glowBillboard(glow, tx, ty, tz, 1.4 * st * pul, R3.argb(16766571, 0.8 * out));
            R3.billboardRot(star, tx, ty, tz, 0.55 * st, t * 0.8, R3.argb(16773808, out));
            double b = t - 3.1;
            if (b > 0.0 && b < 1.4) {
               double a = 1.0 - b / 1.4;
               R3.ring(tx, ty, tz, b * 4.0, b * 4.0 + 0.25, 48, R3.argb(16769162, 0.0), R3.argb(16774864, 0.8 * a), 0.0);

               for (int i = 0; i < 40; i++) {
                  double u = (i + 0.5) / 40.0;
                  double phi = Math.acos(1.0 - 2.0 * u);
                  double an = i * 2.399;
                  double sp = 3.5 * (1.0 - Math.exp(-b * 3.0));
                  R3.glowBillboard(
                     core,
                     tx + Math.sin(phi) * Math.cos(an) * sp,
                     ty + Math.cos(phi) * sp - b * b * 0.8,
                     tz + Math.sin(phi) * Math.sin(an) * sp,
                     0.18,
                     R3.argb(XB[i % XB.length], a)
                  );
               }
            }
         }

         int[][] gc = new int[][]{{14033464, 16040274}, {1739342, 14886459}, {1982074, 16040274}, {15249978, 11867436}, {7024560, 16050640}};

         for (int i = 0; i < 5; i++) {
            double gt = Math.max(0.0, Math.min(1.0, (t - 3.5 - i * 0.18) / 0.35));
            if (!(gt <= 0.0)) {
               double pop = Ease.outBack((float)gt);
               double an = i * 1.25 + 0.3;
               double r = 1.25;
               double gx = tx + Math.cos(an) * r;
               double gz = tz + Math.sin(an) * r;
               double s2 = (0.22 + i % 2 * 0.06) * pop;
               int bc = R3.argb(gc[i][0], out);
               int rc = R3.argb(gc[i][1], out);
               box(gx - s2, y, gz - s2, gx + s2, y + s2 * 1.6, gz + s2, bc, bc);
               box(gx - s2 * 1.02, y + s2 * 1.6, gz - s2 * 0.18, gx + s2 * 1.02, y + s2 * 1.62, gz + s2 * 0.18, rc, rc);
               box(gx - s2 * 0.18, y + s2 * 1.6, gz - s2 * 1.02, gx + s2 * 0.18, y + s2 * 1.62, gz + s2 * 1.02, rc, rc);
            }
         }
      }

      for (int ix = 0; ix < 140; ix++) {
         double life = (ix * 0.071 + t * 0.12) % 1.0;
         double an = ix * 2.399;
         double r = 0.6 + ix % 9 * 0.6;
         double sx = x + Math.cos(an) * r + Math.sin(t + ix) * 0.2;
         double sz = z + Math.sin(an) * r;
         R3.glowBillboard(core, sx, y + 6.0 - life * 6.2, sz, 0.05 + ix % 3 * 0.02, R3.argb(16777215, 0.85 * out * Math.min(1.0, life * 4.0)));
      }

      double sl = (t - 4.3) / 2.6;
      if (sl > 0.0 && sl < 1.0) {
         double rx = -Math.cos(th);
         double rz = -Math.sin(th);
         double sx = x - rx * (14.0 - 28.0 * sl) - fx * 8.0;
         double sz = z - rz * (14.0 - 28.0 * sl) - fz * 8.0;
         double sy = y + 9.0 + Math.sin(sl * Math.PI) * 2.0;
         R3.glowBillboard(glow, sx, sy, sz, 1.2, R3.argb(16769952, 0.8 * out));
         R3.glowBillboard(core, sx, sy, sz, 0.35, R3.argb(16724032, out));

         for (int k = 1; k < 30; k++) {
            double q = sl - k * 0.012;
            if (q < 0.0) {
               break;
            }

            double px = x - rx * (14.0 - 28.0 * q) - fx * 8.0;
            double pz = z - rz * (14.0 - 28.0 * q) - fz * 8.0;
            double py = y + 9.0 + Math.sin(q * Math.PI) * 2.0;
            R3.glowBillboard(star, px, py + Math.sin(k * 1.3 + t * 9.0F) * 0.1, pz, 0.25 * (1.0 - k / 30.0), R3.argb(XB[k % XB.length], (1.0 - k / 30.0) * out));
         }
      }
   }

   private static Identifier tex(String id) {
      return TEX.computeIfAbsent(id, k -> {
         Identifier t = Identifier.fromNamespaceAndPath("legoclient", "cutscene/" + k);

         try {
            Tx.register(t, AmbientTex.paint(k), 128, 128, true);
         } catch (Throwable var3) {
         }

         return t;
      });
   }

   static {
      CUES.put(
         "cs_darkaura",
         new Cutscenes.Cue[]{
            c(0.0, "ambient.cave", 0.8, 0.6),
            c(0.1, "entity.bat.takeoff", 0.8, 0.5),
            c(0.9, "entity.bat.takeoff", 1.0, 0.35),
            c(1.7, "entity.warden.heartbeat", 1.0, 0.9),
            c(2.5, "entity.warden.heartbeat", 1.0, 0.9),
            c(3.3, "entity.warden.heartbeat", 1.05, 0.95),
            c(3.9, "block.bell.resonate", 0.6, 0.55),
            c(3.95, "entity.warden.heartbeat", 1.1, 1.0),
            c(4.6, "entity.warden.heartbeat", 1.15, 1.0),
            c(5.2, "entity.player.attack.sweep", 0.7, 1.0),
            c(5.22, "entity.lightning_bolt.thunder", 1.4, 0.45),
            c(5.29, "entity.player.attack.sweep", 0.85, 1.0),
            c(5.3, "entity.bat.takeoff", 0.7, 0.6),
            c(5.38, "entity.player.attack.sweep", 1.0, 1.0),
            c(6.1, "entity.bat.takeoff", 0.9, 0.35)
         }
      );
      CUES.put(
         "cs_xmas",
         new Cutscenes.Cue[]{
            c(0.5, "entity.firework_rocket.twinkle", 1.2, 0.35),
            c(0.6, "block.note_block.bell", 0.891, 0.7),
            c(0.85, "block.note_block.bell", 0.891, 0.7),
            c(1.1, "block.note_block.bell", 0.891, 0.7),
            c(1.6, "block.note_block.bell", 0.891, 0.7),
            c(1.85, "block.note_block.bell", 0.891, 0.7),
            c(2.1, "block.note_block.bell", 0.891, 0.7),
            c(2.2, "block.amethyst_block.chime", 1.2, 0.5),
            c(2.5, "block.amethyst_block.chime", 1.4, 0.5),
            c(2.6, "block.note_block.bell", 0.891, 0.7),
            c(2.85, "block.note_block.bell", 1.059, 0.7),
            c(3.1, "block.note_block.bell", 0.707, 0.7),
            c(3.1, "block.amethyst_block.chime", 1.5, 0.8),
            c(3.12, "entity.firework_rocket.twinkle", 1.0, 0.7),
            c(3.35, "block.note_block.bell", 0.794, 0.7),
            c(3.6, "block.note_block.bell", 0.891, 0.8),
            c(3.5, "block.amethyst_block.chime", 1.0, 0.45),
            c(3.68, "block.amethyst_block.chime", 1.12, 0.45),
            c(3.86, "block.amethyst_block.chime", 1.26, 0.45),
            c(4.04, "block.amethyst_block.chime", 1.41, 0.45),
            c(4.22, "block.amethyst_block.chime", 1.5, 0.45),
            c(4.4, "entity.firework_rocket.twinkle", 1.3, 0.5),
            c(4.4, "block.note_block.bell", 1.782, 0.35),
            c(4.6, "block.note_block.bell", 1.588, 0.35),
            c(4.8, "block.note_block.bell", 1.782, 0.35),
            c(5.3, "entity.firework_rocket.twinkle", 1.1, 0.45),
            c(6.2, "entity.firework_rocket.twinkle", 1.4, 0.4)
         }
      );
      CUES.put(
         "cs_landing",
         new Cutscenes.Cue[]{
            c(0.05, "item.elytra.flying", 1.3, 0.35),
            c(1.0, "entity.generic.explode", 1.3, 0.55),
            c(1.02, "block.anvil.land", 0.5, 0.45),
            c(3.7, "entity.player.levelup", 1.2, 0.4)
         }
      );
      CUES.put(
         "cs_victory",
         new Cutscenes.Cue[]{
            c(0.1, "ui.toast.challenge_complete", 1.0, 0.5),
            c(0.4, "entity.firework_rocket.launch", 1.0, 0.6),
            c(0.9, "entity.firework_rocket.large_blast", 1.0, 0.7),
            c(1.3, "entity.firework_rocket.twinkle", 1.0, 0.5),
            c(1.25, "entity.firework_rocket.launch", 1.1, 0.6),
            c(1.75, "entity.firework_rocket.blast", 1.0, 0.7),
            c(2.15, "entity.firework_rocket.twinkle", 1.2, 0.5),
            c(2.1, "entity.firework_rocket.launch", 0.95, 0.6),
            c(2.6, "entity.firework_rocket.large_blast", 0.9, 0.7),
            c(3.0, "entity.firework_rocket.twinkle", 0.9, 0.5),
            c(2.95, "entity.firework_rocket.launch", 1.05, 0.6),
            c(3.45, "entity.firework_rocket.blast", 1.1, 0.7),
            c(3.85, "entity.firework_rocket.twinkle", 1.3, 0.5),
            c(3.8, "entity.firework_rocket.launch", 1.0, 0.6),
            c(4.3, "entity.firework_rocket.large_blast", 1.0, 0.7),
            c(4.7, "entity.firework_rocket.twinkle", 1.1, 0.5),
            c(4.65, "entity.firework_rocket.launch", 1.1, 0.6),
            c(5.15, "entity.firework_rocket.blast", 1.0, 0.7),
            c(5.55, "entity.firework_rocket.twinkle", 1.2, 0.5)
         }
      );
      CUES.put(
         "cs_portal",
         new Cutscenes.Cue[]{c(0.2, "block.portal.trigger", 1.4, 0.3), c(2.5, "block.end_portal.spawn", 1.0, 0.45), c(2.5, "block.beacon.activate", 0.8, 0.6)}
      );
      CUES.put(
         "cs_power",
         new Cutscenes.Cue[]{
            c(0.2, "block.beacon.power_select", 0.7, 0.6),
            c(2.0, "block.beacon.ambient", 1.2, 0.7),
            c(3.2, "entity.creeper.primed", 0.6, 0.5),
            c(3.8, "entity.generic.explode", 0.9, 0.7),
            c(3.8, "entity.lightning_bolt.impact", 1.0, 0.6)
         }
      );
      CUES.put(
         "cs_selfie",
         new Cutscenes.Cue[]{c(0.3, "block.note_block.chime", 1.6, 0.35), c(3.75, "ui.button.click", 2.0, 0.6), c(3.8, "block.dispenser.dispense", 1.8, 0.35)}
      );
      CUES.put(
         "cs_starfall",
         new Cutscenes.Cue[]{
            c(0.5, "block.beacon.activate", 1.4, 0.35),
            c(1.0, "block.amethyst_block.chime", 0.9, 0.5),
            c(2.1, "block.amethyst_block.chime", 1.2, 0.5),
            c(3.2, "block.amethyst_block.chime", 1.0, 0.5),
            c(4.3, "block.amethyst_block.chime", 1.35, 0.5),
            c(5.2, "entity.firework_rocket.twinkle", 1.2, 0.4),
            c(5.6, "block.amethyst_block.chime", 1.5, 0.5)
         }
      );
      CUES.put(
         "cs_evillaugh",
         new Cutscenes.Cue[]{
            c(0.0, "ambient.cave", 0.7, 0.7),
            c(0.05, "entity.wither.ambient", 0.5, 0.15),
            c(0.4, "entity.warden.heartbeat", 0.9, 1.0),
            c(1.2, "entity.warden.heartbeat", 0.9, 1.0),
            c(1.3, "entity.witch.ambient", 0.55, 0.6),
            c(2.0, "entity.warden.heartbeat", 0.95, 1.0),
            c(2.6, "entity.lightning_bolt.thunder", 1.2, 0.35),
            c(2.65, "entity.witch.celebrate", 0.6, 0.9),
            c(3.4, "entity.warden.heartbeat", 1.0, 1.0),
            c(4.4, "block.bell.resonate", 0.5, 0.8),
            c(4.45, "entity.warden.heartbeat", 1.0, 1.0),
            c(4.7, "entity.wither.ambient", 0.45, 0.22),
            c(5.1, "entity.warden.heartbeat", 1.05, 1.0),
            c(5.7, "entity.warden.heartbeat", 1.1, 1.0),
            c(6.0, "entity.lightning_bolt.thunder", 0.9, 0.8),
            c(6.0, "entity.lightning_bolt.impact", 0.8, 0.6),
            c(6.05, "entity.witch.celebrate", 0.7, 1.0),
            c(6.1, "item.book.page_turn", 0.7, 0.8),
            c(6.45, "item.book.page_turn", 0.9, 0.7),
            c(6.9, "entity.witch.celebrate", 0.8, 1.0),
            c(7.1, "entity.lightning_bolt.thunder", 1.3, 0.3),
            c(7.75, "entity.witch.celebrate", 0.92, 1.0),
            c(8.3, "entity.lightning_bolt.thunder", 1.1, 0.4),
            c(8.3, "entity.wither.ambient", 0.55, 0.2),
            c(9.3, "block.bell.resonate", 0.4, 0.5)
         }
      );

      for (Cutscenes.Cue[] cs : CUES.values()) {
         Arrays.sort(cs, (a, b) -> Float.compare(a.t, b.t));
      }

      DA_CUTS = new float[]{0.0F, 1.7F, 2.7F, 3.9F, 5.2F, 6.1F};
      EL_CUTS = new float[]{0.0F, 2.6F, 4.4F, 6.0F};
      EL_BOLTS = new float[]{2.6F, 6.0F, 7.1F, 8.3F};
      XB = new int[]{16726862, 16763210, 3924106, 4892927, 16743381};
      TEX = new HashMap<>();
   }

   private record Cue(float t, String id, float pitch, float vol) {
   }
}
