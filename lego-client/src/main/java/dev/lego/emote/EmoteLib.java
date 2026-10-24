package dev.lego.emote;

import dev.lego.core.Modules;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EmoteLib {
   public static final int HEAD = 0;
   public static final int BODY = 3;
   public static final int RARM = 6;
   public static final int LARM = 9;
   public static final int RLEG = 12;
   public static final int LLEG = 15;
   public static final int N = 18;
   public static final List<EmoteLib.Emote> ALL = new ArrayList<>();
   public static final String[] DEFAULT_WHEEL = new String[]{"wave", "dance", "dab", "heart", "gg", "floss", "tpose", "laugh"};
   public static final int WHEEL_MAX = 8;
   public static final List<String> WHEEL = new ArrayList<>(Arrays.asList(DEFAULT_WHEEL));

   private EmoteLib() {
   }

   public static void addGenerated(String id, String name, String icon, float dur, boolean loop, String bubble, float spin, EmoteLib.PoseFn fn) {
      ALL.add(new EmoteLib.Emote(id, name, icon, dur, loop, bubble, spin, fn));
   }

   private static void add(String id, String name, String icon, float dur, boolean loop, String bubble, float spin, EmoteLib.PoseFn fn) {
      addGenerated(id, name, icon, dur, loop, bubble, spin, fn);
   }

   public static EmoteLib.Emote get(String id) {
      for (EmoteLib.Emote e : ALL) {
         if (e.id.equals(id)) {
            return e;
         }
      }

      return null;
   }

   public static int index(String id) {
      for (int i = 0; i < ALL.size(); i++) {
         if (ALL.get(i).id.equals(id)) {
            return i;
         }
      }

      return -1;
   }

   static float s(double v) {
      return (float)Math.sin(v);
   }

   static float c(double v) {
      return (float)Math.cos(v);
   }

   static void set(float[] p, int part, double pitch, double yaw, double roll) {
      p[part] = (float)pitch;
      p[part + 1] = (float)yaw;
      p[part + 2] = (float)roll;
   }

   static void pitch(float[] p, int part, double v) {
      p[part] = (float)v;
   }

   static float kf(float t, double... k) {
      if (t <= k[0]) {
         return (float)k[1];
      } else {
         for (int i = 2; i < k.length; i += 2) {
            if (t <= k[i]) {
               double u = (t - k[i - 2]) / Math.max(1.0E-4, k[i] - k[i - 2]);
               u = u * u * (3.0 - 2.0 * u);
               return (float)(k[i - 1] + (k[i + 1] - k[i - 1]) * u);
            }
         }

         return (float)k[k.length - 1];
      }
   }

   static float loop(float t, float period) {
      return t % period;
   }

   static float hit(float t, float t0, float up, float down) {
      if (t < t0) {
         return 0.0F;
      } else {
         return t < t0 + up ? (t - t0) / up : (float)Math.exp(-(t - t0 - up) / down);
      }
   }

   public static boolean isCutscene(String id) {
      return id.startsWith("cs_");
   }

   static float ease(float x) {
      x = Math.max(0.0F, Math.min(1.0F, x));
      return x * x * (3.0F - 2.0F * x);
   }

   public static boolean inWheel(String id) {
      return WHEEL.contains(id);
   }

   public static boolean toggleWheel(String id) {
      if (WHEEL.remove(id)) {
         Modules.scheduleSave();
         return true;
      } else if (WHEEL.size() >= 8) {
         return false;
      } else {
         WHEEL.add(id);
         Modules.scheduleSave();
         return true;
      }
   }

   public static void registerPersist() {
      Modules.persist("emoteWheel", new Modules.Persist() {
         @Override
         public Object save() {
            return new ArrayList<>(EmoteLib.WHEEL);
         }

         @Override
         public void load(Object o) {
            if (o instanceof List) {
               EmoteLib.WHEEL.clear();

               for (Object x : (List)o) {
                  if (x instanceof String && EmoteLib.get((String)x) != null && EmoteLib.WHEEL.size() < 8) {
                     EmoteLib.WHEEL.add((String)x);
                  }
               }
            }
         }
      });
   }

   static double tri(double v) {
      double x = v - Math.floor(v);
      return x < 0.5 ? x * 2.0 : 2.0 - x * 2.0;
   }

   static {
      add("wave", "Winken", "emote", 3.0F, false, null, 0.0F, (t, p) -> set(p, 6, -2.75, 0.0, 0.25 + s(t * 9.0F) * 0.45));
      add("dance", "Tanzen", "music", 6.0F, true, null, 220.0F, (t, p) -> {
         float a = s(t * 8.0F);
         set(p, 6, -1.6 - a * 1.2, 0.0, 0.3);
         set(p, 9, -1.6 + a * 1.2, 0.0, -0.3);
         set(p, 12, a * 0.4, 0.0, 0.05);
         set(p, 15, -a * 0.4, 0.0, -0.05);
         set(p, 0, a * 0.2, s(t * 4.0F) * 0.3, 0.0);
      });
      add("tpose", "T-Pose", "user", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, 0.0, 0.0, 1.5708);
         set(p, 9, 0.0, 0.0, -1.5708);
         set(p, 0, 0.0, 0.0, 0.0);
      });
      add("clap", "Klatschen", "sparkle", 3.0F, false, null, 0.0F, (t, p) -> {
         float k = Math.abs(s(t * 7.0F)) * 0.45F;
         set(p, 6, -1.35, 0.0, -0.05 - k);
         set(p, 9, -1.35, 0.0, 0.05 + k);
      });
      add("dab", "Dab", "star", 2.2F, false, null, 0.0F, (t, p) -> {
         set(p, 0, 0.55, -0.4, 0.0);
         set(p, 6, -2.3, 0.0, -0.9);
         set(p, 9, -1.25, 0.0, -0.6);
      });
      add("salute", "Salutieren", "shield", 2.5F, false, null, 0.0F, (t, p) -> set(p, 6, -2.2, 0.0, -0.75));
      add("cheer", "Jubeln", "trophy", 3.0F, false, null, 0.0F, (t, p) -> {
         float b = Math.abs(s(t * 6.0F)) * 0.3F;
         set(p, 6, -2.9 + b, 0.0, 0.35);
         set(p, 9, -2.9 + b, 0.0, -0.35);
         set(p, 0, -0.25, 0.0, 0.0);
      });
      add("facepalm", "Facepalm", "user", 2.5F, false, null, 0.0F, (t, p) -> {
         set(p, 0, 0.45, 0.0, 0.0);
         set(p, 6, -2.05, 0.0, -0.45);
      });
      add("heart", "Herz", "heart", 2.8F, false, "heart", 0.0F, (t, p) -> {
         set(p, 6, -2.4, 0.0, -0.35);
         set(p, 9, -2.4, 0.0, 0.35);
      });
      add("gg", "GG", "gg", 2.6F, false, "gg", 0.0F, (t, p) -> set(p, 6, -2.2, 0.0, 0.3 + s(t * 6.0F) * 0.2));
      add("laugh", "Lachen", "gg", 2.6F, false, "laugh", 0.0F, (t, p) -> {
         float b = s(t * 14.0F) * 0.12F;
         set(p, 0, -0.35 + b, 0.0, 0.0);
         set(p, 6, -0.4, 0.0, 0.25);
         set(p, 9, -0.4, 0.0, -0.25);
      });
      add("fire", "Feuer", "flame", 2.6F, false, "fire", 0.0F, (t, p) -> {
         set(p, 6, -1.5, 0.3, 0.0);
         set(p, 9, -1.5, -0.3, 0.0);
      });
      add("floss", "Floss", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         float a = s(t * 9.0F);
         float h = s(t * 18.0F);
         set(p, 6, 0.25 * a, 0.0, 0.35 + a * 0.55);
         set(p, 9, -0.25 * a, 0.0, -0.35 + a * 0.55);
         set(p, 3, 0.0, 0.0, h * 0.08);
         set(p, 12, 0.0, 0.0, -h * 0.05);
         set(p, 15, 0.0, 0.0, -h * 0.05);
      });
      add("zombie", "Zombie", "skull", 5.0F, true, null, 0.0F, (t, p) -> {
         float w = s(t * 3.0F) * 0.12F;
         set(p, 6, -1.57 + w, 0.08, 0.0);
         set(p, 9, -1.57 - w, -0.08, 0.0);
         set(p, 0, 0.25, s(t * 1.5) * 0.3, s(t * 1.3) * 0.2);
      });
      add("bow", "Verbeugen", "user", 2.6F, false, null, 0.0F, (t, p) -> {
         float k = (float)Math.sin(Math.min(1.0, t / 2.6) * Math.PI);
         set(p, 3, 0.55 * k, 0.0, 0.0);
         set(p, 0, 0.6 * k, 0.0, 0.0);
         set(p, 6, -0.2 * k, 0.0, 0.1);
         set(p, 9, 0.6 * k, 0.0, -0.1);
      });
      add("shrug", "Achselzucken", "info", 2.4F, false, null, 0.0F, (t, p) -> {
         float k = Math.abs(s(t * 2.6));
         set(p, 6, -0.5, 0.9, 1.1 + k * 0.15);
         set(p, 9, -0.5, -0.9, -1.1 - k * 0.15);
         set(p, 0, 0.1, 0.0, 0.25);
      });
      add("headbang", "Headbang", "music", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 11.0F);
         set(p, 0, 0.35 + k * 0.45, 0.0, 0.0);
         set(p, 6, -2.8, 0.0, 0.3 + k * 0.1);
         set(p, 9, -0.3, 0.0, -0.1);
      });
      add("guitar", "Luftgitarre", "music", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 14.0F);
         set(p, 6, -0.7 + k * 0.25, -0.4, -0.3);
         set(p, 9, -1.2, 0.6, 0.2);
         set(p, 0, 0.2 + Math.abs(k) * 0.15, -0.3, 0.0);
      });
      add("robot", "Roboter", "chip", 6.0F, true, null, 0.0F, (t, p) -> {
         int step = (int)Math.floor(t * 2.5) % 4;
         double[][] frames = new double[][]{{-1.57, 0.0, 0.0, 0.0}, {0.0, 0.0, -1.57, 0.0}, {-1.57, 0.8, -1.57, -0.8}, {0.0, 1.2, 0.0, -1.2}};
         double[] f = frames[step];
         set(p, 6, f[0], 0.0, f[1]);
         set(p, 9, f[2], 0.0, f[3]);
         set(p, 0, 0.0, step % 2 == 0 ? 0.5 : -0.5, 0.0);
      });
      add("propeller", "Propeller", "reset", 4.0F, true, null, 0.0F, (t, p) -> {
         float a = t * 12.0F;
         set(p, 6, -1.57 + s(a) * 1.4, 0.0, c(a) * 0.6 + 0.6);
         set(p, 9, -1.57 - s(a) * 1.4, 0.0, -c(a) * 0.6 - 0.6);
      });
      add("think", "Denken", "info", 3.0F, false, "think", 0.0F, (t, p) -> {
         set(p, 6, -2.1, -0.3, -0.5);
         set(p, 9, -0.8, 0.5, 0.2);
         set(p, 0, -0.2, 0.25, 0.15);
      });
      add("cry", "Weinen", "rain", 3.0F, false, "cry", 0.0F, (t, p) -> {
         float b = s(t * 12.0F) * 0.06F;
         set(p, 0, 0.35 + b, 0.0, 0.0);
         set(p, 6, -2.0, 0.0, -0.35);
         set(p, 9, -2.0, 0.0, 0.35);
      });
      add("meditate", "Meditieren", "sun", 8.0F, true, "zen", 0.0F, (t, p) -> {
         float br = s(t * 1.6) * 0.06F;
         set(p, 6, -0.9 + br, -0.4, 0.5);
         set(p, 9, -0.9 + br, 0.4, -0.5);
         set(p, 0, 0.1 + br, 0.0, 0.0);
      });
      add("box", "Boxen", "target", 4.0F, true, null, 0.0F, (t, p) -> {
         float a = s(t * 10.0F);
         float b = s(t * 10.0F + Math.PI);
         set(p, 6, -1.3 - Math.max(0.0F, a) * 0.3, 0.3 - Math.max(0.0F, a) * 0.3, -0.2);
         set(p, 9, -1.3 - Math.max(0.0F, b) * 0.3, -0.3 + Math.max(0.0F, b) * 0.3, 0.2);
         set(p, 0, 0.15, s(t * 5.0F) * 0.15, 0.0);
         set(p, 3, 0.0, s(t * 5.0F) * 0.12, 0.0);
      });
      add("point", "Zeigen", "arrow", 2.5F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -1.6, 0.1, 0.0);
         set(p, 0, 0.0, 0.1, 0.0);
      });
      add("swim", "Schwimmen", "rain", 5.0F, true, null, 0.0F, (t, p) -> {
         float a = t * 5.0F;
         set(p, 6, -1.57 + s(a) * 1.6, 0.0, 0.3);
         set(p, 9, -1.57 + s(a + Math.PI) * 1.6, 0.0, -0.3);
         set(p, 12, s(a * 2.0F) * 0.35, 0.0, 0.0);
         set(p, 15, -s(a * 2.0F) * 0.35, 0.0, 0.0);
      });
      add("chicken", "Hühnertanz", "music", 5.0F, true, null, 0.0F, (t, p) -> {
         float a = Math.abs(s(t * 8.0F));
         set(p, 6, 0.0, -0.3, 0.2 + a * 0.9);
         set(p, 9, 0.0, 0.3, -0.2 - a * 0.9);
         set(p, 0, -0.1 + s(t * 16.0F) * 0.12, 0.0, 0.0);
         set(p, 12, -a * 0.5, 0.0, 0.0);
      });
      add("sleep", "Schlafen", "moon", 6.0F, true, "sleep", 0.0F, (t, p) -> {
         float br = s(t * 1.4) * 0.05F;
         set(p, 0, 0.55 + br, 0.0, 0.35);
         set(p, 6, 0.1, 0.0, 0.05);
         set(p, 9, 0.1, 0.0, -0.05);
      });
      add("hype", "Hype", "bolt", 4.0F, true, null, 0.0F, (t, p) -> {
         float a = Math.abs(s(t * 9.0F));
         set(p, 6, -3.0 + a * 0.6, 0.0, 0.5);
         set(p, 9, -3.0 + a * 0.6, 0.0, -0.5);
         set(p, 0, -0.3, s(t * 9.0F) * 0.2, 0.0);
         set(p, 12, -a * 0.3, 0.0, 0.1);
         set(p, 15, a * 0.3, 0.0, -0.1);
      });
      add(
         "karate",
         "Karate-Kombo",
         "hit",
         4.6F,
         false,
         null,
         0.0F,
         (t, p) -> {
            set(
               p,
               6,
               kf(
                  t,
                  0.0,
                  0.0,
                  0.35,
                  0.35,
                  0.45,
                  0.35,
                  0.55,
                  -1.62,
                  0.85,
                  -1.62,
                  0.95,
                  0.35,
                  1.25,
                  0.35,
                  1.35,
                  -1.62,
                  1.6,
                  -1.62,
                  1.8,
                  -1.25,
                  2.3,
                  -1.25,
                  2.55,
                  -1.3,
                  2.75,
                  -3.0,
                  2.9,
                  -1.35,
                  3.3,
                  -1.35,
                  3.6,
                  0.0
               ),
               kf(t, 0.45, 0.0, 0.55, -0.14, 0.85, -0.14, 0.95, 0.0, 1.25, 0.0, 1.35, -0.14, 1.6, -0.14, 1.8, 0.0),
               kf(t, 0.0, 0.05, 0.35, 0.12, 1.6, 0.12, 1.8, -0.1, 2.55, -0.1, 2.75, 0.25, 2.9, -0.12, 3.3, -0.12, 3.6, 0.05)
            );
            set(
               p,
               9,
               kf(
                  t,
                  0.0,
                  0.0,
                  0.35,
                  -1.35,
                  0.55,
                  0.35,
                  0.85,
                  0.35,
                  0.95,
                  -1.62,
                  1.25,
                  -1.62,
                  1.35,
                  0.35,
                  1.6,
                  0.35,
                  1.8,
                  -1.25,
                  1.95,
                  0.35,
                  2.4,
                  0.35,
                  2.6,
                  -1.1,
                  3.3,
                  -1.1,
                  3.6,
                  0.0
               ),
               kf(t, 0.85, 0.0, 0.95, 0.14, 1.25, 0.14, 1.35, 0.0),
               kf(t, 0.0, -0.05, 0.35, -0.1, 1.8, 0.1, 1.95, -0.7, 2.4, -0.7, 2.6, -0.05, 3.6, -0.05)
            );
            set(
               p,
               12,
               kf(t, 0.0, 0.0, 0.35, 0.32, 1.85, 0.32, 2.05, -1.65, 2.3, -1.6, 2.55, 0.32, 3.3, 0.32, 3.6, 0.0),
               0.0,
               kf(t, 0.0, 0.0, 0.35, 0.06, 3.6, 0.02)
            );
            set(p, 15, kf(t, 0.0, 0.0, 0.35, -0.28, 1.85, -0.28, 2.05, 0.12, 2.55, -0.28, 3.3, -0.28, 3.6, 0.0), 0.0, kf(t, 0.0, 0.0, 0.35, -0.06, 3.6, -0.02));
            set(
               p,
               3,
               kf(t, 0.0, 0.0, 0.35, 0.06, 1.85, 0.06, 2.05, -0.14, 2.4, -0.14, 2.6, 0.06, 2.9, 0.1, 3.3, 0.06, 3.6, 0.0, 3.8, 0.45, 4.25, 0.45, 4.6, 0.0),
               0.0,
               0.0
            );
            set(
               p,
               0,
               kf(t, 0.0, 0.0, 0.35, 0.12, 2.75, 0.12, 2.9, -0.18, 3.3, 0.0, 3.8, 0.15, 4.6, 0.0),
               kf(t, 0.55, 0.0, 0.6, 0.05, 1.0, -0.05, 1.4, 0.05, 1.8, 0.0),
               0.0
            );
         }
      );
      add("yoga_tree", "Yoga-Baum", "leaf", 8.0F, true, "zen", 0.0F, (t, p) -> {
         float up = ease(t / 1.2F);
         float grow = ease((t - 1.2F) / 1.4F);
         float sway = s(t * 1.3) * 0.035F * grow;
         float br = s(t * 1.7) * 0.03F;
         set(p, 6, -1.3 * (1.0F - grow) - 3.0 * grow + br, -0.42 * (1.0F - grow), -0.3 * (1.0F - grow) + 0.2 * grow + sway);
         set(p, 9, -1.3 * (1.0F - grow) - 3.0 * grow + br, 0.42 * (1.0F - grow), 0.3 * (1.0F - grow) - 0.2 * grow + sway);
         set(p, 12, -0.35 * up, 0.35 * up, 0.95 * up + sway * 0.5);
         set(p, 15, 0.0, 0.0, -0.03 + sway * 0.4);
         set(p, 3, 0.0, 0.0, sway * 0.4);
         set(p, 0, -0.12 * grow + br, 0.0, sway * 0.8);
      });
      add("bunny", "Hasensprung", "jump", 4.8F, true, null, 0.0F, (t, p) -> {
         float u = loop(t, 0.6F) / 0.6F;
         float air = s(u * Math.PI);
         float land = (float)Math.max(0.0, Math.cos(u * Math.PI * 2.0)) * 0.5F;
         int hop = (int)(t / 0.6F);
         float tilt = (hop % 2 == 0 ? 1 : -1) * 0.14F;
         set(p, 6, -1.35 - 0.22 * air, -0.28, -0.1);
         set(p, 9, -1.35 - 0.22 * air, 0.28, 0.1);
         set(p, 12, 0.55 * air - 0.12 * land, 0.0, 0.02);
         set(p, 15, 0.55 * air - 0.12 * land, 0.0, -0.02);
         set(p, 3, 0.12 * land - 0.04 * air, 0.0, 0.0);
         set(p, 0, -0.18 * air + 0.1 * land, 0.0, tilt * air);
      });
      add("zombie_walk", "Zombie-Gang", "skull", 6.4F, true, null, 0.0F, (t, p) -> {
         float w = s(t * Math.PI * 2.0 / 1.6);
         float lurch = s(t * Math.PI * 2.0 / 1.6 + 0.6);
         float twitch = loop(t, 3.2F) < 0.18F ? s(t * 70.0F) * 0.25F : 0.0F;
         set(p, 6, -1.45 + s(t * 2.1) * 0.1, 0.06, -0.05 + lurch * 0.06);
         set(p, 9, -1.72 + s(t * 2.1 + 1.5) * 0.1, -0.06, 0.05 + lurch * 0.06);
         set(p, 12, 0.38 * w, 0.0, 0.04);
         set(p, 15, -0.38 * w, 0.0, -0.04);
         set(p, 3, 0.1, 0.0, lurch * 0.05);
         set(p, 0, 0.28 + s(t * 1.7) * 0.1, twitch, s(t * 1.9) * 0.38);
      });
      add("ballet", "Ballett-Drehung", "sparkle", 8.0F, true, null, 240.0F, (t, p) -> {
         float u = loop(t, 4.0F);
         float ara = kf(u, 0.0, 0.0, 1.6, 0.0, 2.1, 1.0, 3.5, 1.0, 4.0, 0.0);
         float br = s(t * 3.0F) * 0.04F;
         set(p, 6, -2.85 * (1.0F - ara) - 1.75 * ara + br, 0.0, 0.36 * (1.0F - ara) + 0.05 * ara);
         set(p, 9, -2.85 * (1.0F - ara) - 0.25 * ara + br, 0.0, -0.36 * (1.0F - ara) - 1.35 * ara);
         set(p, 12, -0.55 * (1.0F - ara), 0.25 * (1.0F - ara), 0.7 * (1.0F - ara) + 0.02 * ara);
         set(p, 15, 0.95 * ara, 0.0, -0.03);
         set(p, 3, 0.18 * ara, 0.0, 0.0);
         set(p, 0, -0.22 * (1.0F - ara) - 0.3 * ara, 0.0, 0.12 * (1.0F - ara));
      });
      add("star_jump", "Jubel-Sprung", "trophy", 4.8F, true, null, 0.0F, (t, p) -> {
         float u = loop(t, 1.2F);
         float crouch = kf(u, 0.0, 0.0, 0.3, 1.0, 0.42, 0.0, 1.0, 0.0, 1.2, 0.0);
         float star = kf(u, 0.3, 0.0, 0.48, 1.0, 0.72, 1.0, 0.95, 0.0);
         float land = kf(u, 0.9, 0.0, 0.98, 1.0, 1.2, 0.0);
         set(p, 6, 0.55 * crouch - 2.9 * star, 0.0, 0.1 * (1.0F - star) - 0.5 * star);
         set(p, 9, 0.55 * crouch - 2.9 * star, 0.0, -0.1 * (1.0F - star) + 0.5 * star);
         set(p, 12, -0.1 * star, 0.0, 0.45 * star);
         set(p, 15, -0.1 * star, 0.0, -0.45 * star);
         set(p, 3, 0.28 * crouch + 0.12 * land - 0.08 * star, 0.0, 0.0);
         set(p, 0, -0.15 * crouch - 0.35 * star, 0.0, 0.0);
      });
      add("deep_bow", "Tiefe Verbeugung", "crown", 3.8F, false, null, 0.0F, (t, p) -> {
         float sweep = kf(t, 0.0, 0.0, 0.55, 1.0, 0.9, 0.0);
         float bow = kf(t, 0.6, 0.0, 1.3, 1.0, 2.8, 1.0, 3.5, 0.0);
         set(p, 6, -0.7 * sweep - 1.1 * bow, -0.65 * bow, 1.35 * sweep - 0.1 * bow);
         set(p, 9, 0.55 * bow, 0.0, -0.1 - 0.9 * bow);
         set(p, 12, 0.1 * bow, 0.0, 0.03);
         set(p, 15, -0.1 * bow, 0.0, -0.03);
         set(p, 3, 0.75 * bow, 0.0, 0.0);
         set(p, 0, -0.15 * sweep + 0.1 * bow, 0.0, 0.0);
      });
      add(
         "snowball",
         "Schneeball werfen",
         "snow",
         3.4F,
         false,
         null,
         0.0F,
         (t, p) -> {
            float pack = t > 0.6F && t < 1.25F ? s(t * 22.0F) * 0.12F : 0.0F;
            set(
               p,
               6,
               kf(t, 0.0, 0.0, 0.35, -0.75, 0.6, -0.75, 0.85, -1.25, 1.25, -1.25, 1.6, -3.25, 1.72, -3.25, 1.86, -1.2, 2.1, -0.4, 2.6, -0.4, 3.2, 0.0),
               kf(t, 0.6, 0.0, 0.85, -0.3, 1.25, -0.3, 1.6, 0.0),
               kf(t, 0.35, 0.05, 0.85, -0.2, 1.25, -0.2, 1.6, -0.25, 1.86, -0.1, 2.1, -0.5, 2.6, -0.5, 3.2, 0.05) + pack
            );
            set(
               p,
               9,
               kf(t, 0.0, 0.0, 0.35, -0.75, 0.6, -0.75, 0.85, -1.25, 1.25, -1.25, 1.6, -1.65, 1.8, -1.6, 1.95, 0.3, 2.6, 0.3, 3.2, 0.0),
               kf(t, 0.6, 0.0, 0.85, 0.3, 1.25, 0.3, 1.6, 0.0),
               kf(t, 0.35, -0.05, 0.85, 0.2, 1.25, 0.2, 1.6, -0.1, 1.95, -0.2, 3.2, -0.05) - pack
            );
            set(p, 3, kf(t, 0.0, 0.0, 0.35, 0.55, 0.6, 0.55, 0.85, 0.0, 1.6, -0.14, 1.8, 0.22, 2.3, 0.12, 3.2, 0.0), 0.0, 0.0);
            set(p, 12, kf(t, 0.6, 0.0, 1.2, 0.35, 1.8, 0.35, 2.0, -0.05, 3.2, 0.0), 0.0, 0.04);
            set(p, 15, kf(t, 0.6, 0.0, 1.2, -0.3, 1.8, -0.3, 2.0, 0.15, 3.2, 0.0), 0.0, -0.04);
            set(
               p,
               0,
               kf(t, 0.0, 0.0, 0.35, 0.0, 0.6, 0.2, 0.85, 0.45, 1.25, 0.45, 1.5, -0.05, 2.2, -0.05, 2.9, -0.1, 3.4, 0.0),
               0.0,
               kf(t, 2.4, 0.0, 2.7, 0.2, 3.4, 0.0)
            );
         }
      );
      add("xmas_dance", "Weihnachts-Tanz", "gift", 8.0F, true, "heart", 0.0F, (t, p) -> {
         float u = loop(t, 4.0F);
         float jing = s(t * 34.0F) * 0.12F;
         float step = s(t * Math.PI * 2.0);
         float rUp = kf(u, 0.0, 0.0, 0.2, 1.0, 0.9, 1.0, 1.1, 0.0, 1.9, 0.0, 2.1, 1.0, 2.9, 1.0, 3.1, 0.0);
         float lUp = kf(u, 0.9, 0.0, 1.1, 1.0, 2.9, 1.0, 3.1, 0.0);
         float clap = kf(u, 3.0, 0.0, 3.15, 1.0, 3.85, 1.0, 4.0, 0.0);
         float cl = Math.abs(s(t * 12.0F)) * 0.3F * clap;
         set(p, 6, -2.65 * rUp + jing * rUp - 1.35 * clap, -0.3 * clap + cl, 0.2 - 0.5 * rUp + 0.2 * rUp * lUp - 0.2 * clap);
         set(p, 9, -2.65 * lUp - jing * lUp - 1.35 * clap, 0.3 * clap - cl, -0.2 + 0.5 * lUp - 0.2 * rUp * lUp + 0.2 * clap);
         set(p, 12, 0.0, 0.0, 0.05 + Math.max(0.0F, step) * 0.28);
         set(p, 15, 0.0, 0.0, -0.05 - Math.max(0.0F, -step) * 0.28);
         set(p, 3, 0.0, 0.0, step * 0.05);
         set(p, 0, -0.1, s(t * Math.PI) * 0.18, step * 0.16);
      });
      add(
         "unwrap",
         "Geschenk auspacken",
         "gift",
         4.8F,
         false,
         "heart",
         0.0F,
         (t, p) -> {
            float hold = kf(t, 0.0, 0.0, 0.35, 1.0, 2.3, 1.0, 2.5, 0.0);
            float shake = t > 0.5F && t < 1.4F ? s(t * 26.0F) * 0.1F : 0.0F;
            float rip = kf(t, 1.55, 0.0, 1.68, 1.0, 1.85, 0.0, 1.98, 1.0, 2.2, 0.2);
            float gasp = kf(t, 2.3, 0.0, 2.5, 1.0, 2.9, 1.0, 3.1, 0.0);
            float lift = kf(t, 2.95, 0.0, 3.3, 1.0);
            float bob = Math.abs(s(t * 7.0F)) * 0.18F * lift;
            set(
               p,
               6,
               -1.1 * hold + shake - 0.2 * rip - 2.3 * gasp - 2.95 * lift + bob,
               -0.22 * hold + 0.75 * rip,
               -0.2 * hold + 0.45 * rip + 0.45 * gasp + 0.12 * lift
            );
            set(
               p,
               9,
               -1.1 * hold + shake - 0.2 * rip - 2.3 * gasp - 2.95 * lift + bob,
               0.22 * hold - 0.75 * rip,
               0.2 * hold - 0.45 * rip - 0.45 * gasp - 0.12 * lift
            );
            set(p, 3, 0.06 * hold - 0.06 * gasp - 0.08 * lift, 0.0, 0.0);
            set(p, 0, 0.42 * hold - 0.3 * gasp - 0.35 * lift, 0.0, kf(t, 0.4, 0.0, 0.7, 0.22, 1.4, 0.22, 1.6, 0.0));
            set(p, 12, -0.2 * lift * Math.abs(s(t * 7.0F)), 0.0, 0.04);
         }
      );
      add("shiver", "Zittern vor Kälte", "snow", 6.0F, true, null, 0.0F, (t, p) -> {
         float big = (float)Math.exp(-Math.pow((loop(t, 2.6F) - 1.3F) / 0.18F, 2.0));
         float tr = s(t * 58.0F) * (0.025F + 0.07F * big);
         float tr2 = s(t * 47.0F + 1.0F) * (0.02F + 0.05F * big);
         set(p, 6, -1.08 + tr2, -0.55, -0.35 + tr);
         set(p, 9, -0.92 - tr2, 0.55, 0.35 - tr);
         set(p, 12, 0.0, 0.0, -0.06 + tr * 0.4);
         set(p, 15, 0.0, 0.0, 0.06 - tr * 0.4);
         set(p, 3, 0.12 + big * 0.05, 0.0, tr * 0.3);
         set(p, 0, 0.28 + tr2 * 0.6, tr * 0.5, tr);
      });
      add("chill", "Chillen", "sun", 8.0F, true, null, 0.0F, (t, p) -> {
         float in = ease(t / 0.8F);
         float nod = s(t * 3.4) * 0.07F;
         set(p, 6, -3.2 * in, 0.0, 0.62 * in + 0.05 * (1.0F - in));
         set(p, 9, -3.2 * in, 0.0, -0.62 * in - 0.05 * (1.0F - in));
         set(p, 3, -0.1 * in, 0.0, 0.0);
         set(p, 0, (-0.14 + nod) * in, s(t * 0.55) * 0.18, 0.08 * in);
         set(p, 12, -0.05 * in, 0.0, -0.08 * in);
         set(p, 15, 0.05 * in, 0.0, 0.12 * in + Math.max(0.0F, s(t * 3.4)) * 0.05);
      });
      add("fingerguns", "Fingerpistolen", "target", 3.6F, false, "fire", 0.0F, (t, p) -> {
         float aim = kf(t, 0.0, 0.0, 0.35, 1.0, 2.35, 1.0, 2.6, 0.0);
         float blow = kf(t, 2.4, 0.0, 2.7, 1.0, 3.3, 1.0, 3.6, 0.0);
         float r1 = hit(t, 0.7F, 0.05F, 0.12F) + hit(t, 1.5F, 0.05F, 0.12F);
         float l1 = hit(t, 1.1F, 0.05F, 0.12F);
         float shrug = kf(t, 1.8, 0.0, 2.0, 1.0, 2.3, 0.0);
         set(p, 6, -1.52 * aim - 0.4 * r1 - 2.1 * blow, (-0.1 + 0.08 * shrug) * aim - 0.35 * blow, 0.35 * blow);
         set(p, 9, -1.52 * aim - 0.4 * l1, 0.1 * aim, 0.0);
         set(p, 3, -0.07 * aim, 0.0, 0.0);
         set(p, 0, -0.05 * aim + 0.12 * blow, -0.12 * aim, 0.2 * aim - 0.1 * blow);
         set(p, 12, 0.0, 0.0, 0.1);
         set(p, 15, -0.12 * aim, 0.0, -0.06);
      });
      add("thinker", "Denker-Pose", "info", 8.0F, true, "think", 0.0F, (t, p) -> {
         float in = ease(t / 0.9F);
         float hmm = s(t * 1.1) * 0.05F;
         set(p, 6, -2.05 * in, -0.35 * in, 0.2 * in);
         set(p, 9, -0.85 * in, 0.45 * in, 0.2 * in);
         set(p, 3, 0.32 * in, 0.0, 0.0);
         set(p, 0, (-0.12 + hmm) * in, 0.1 * in, 0.12 * in);
         set(p, 12, -0.45 * in, 0.0, 0.05);
         set(p, 15, 0.08 * in, 0.0, -0.05);
      });
      add("drums", "Schlagzeug", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         float bar = loop(t, 2.0F);
         float crash = kf(bar, 1.55, 0.0, 1.65, 1.0, 1.85, 1.0, 2.0, 0.0);
         float r = Math.max(0.0F, s(t * 15.0F));
         float l = Math.max(0.0F, s(t * 15.0F + Math.PI));
         set(p, 6, -0.95 - 0.32 * r * (1.0F - crash) - 1.1 * crash, 0.0, -0.18 * (1.0F - crash) - 0.5 * crash);
         set(p, 9, -0.95 - 0.32 * l, 0.0, 0.18);
         set(p, 12, -0.28 * Math.max(0.0F, s(t * 7.5)), 0.0, 0.05);
         set(p, 3, 0.08, 0.0, 0.0);
         set(p, 0, 0.15 + Math.abs(s(t * 7.5)) * 0.2 - 0.25 * crash, 0.0, 0.0);
      });
      add(
         "macarena",
         "Macarena",
         "music",
         9.6F,
         true,
         null,
         0.0F,
         (t, p) -> {
            double[][] R = new double[][]{
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.5, -0.95, 0.0},
               {-1.5, -0.95, 0.0},
               {-3.05, 0.0, 0.55},
               {-3.05, 0.0, 0.55},
               {-0.45, 0.0, -0.45},
               {-0.45, 0.0, -0.45},
               {0.45, 0.0, 0.12},
               {0.45, 0.0, 0.12}
            };
            double[][] L = new double[][]{
               {0.0, 0.0, -0.05},
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.57, 0.0, 0.0},
               {-1.62, 0.95, 0.0},
               {-1.62, 0.95, 0.0},
               {-3.05, 0.0, -0.55},
               {-3.05, 0.0, -0.55},
               {-0.45, 0.0, 0.45},
               {-0.45, 0.0, 0.45},
               {0.45, 0.0, -0.12}
            };
            double[] rest = new double[]{0.0, 0.0, 0.05};
            double[] restL = new double[]{0.0, 0.0, -0.05};
            float beat = 0.4F;
            float u = loop(t, beat * 12.0F);
            int i = (int)(u / beat) % 12;
            int pi = (i + 11) % 12;
            float k = ease((u - i * beat) / (beat * 0.45F));
            double[] r0 = t < beat ? rest : R[pi];
            double[] l0 = t < beat ? restL : L[pi];
            double pop = i != 2 && i != 3 ? 0.0 : s(k * Math.PI) * 0.15;
            set(p, 6, r0[0] + (R[i][0] - r0[0]) * k - pop, r0[1] + (R[i][1] - r0[1]) * k, r0[2] + (R[i][2] - r0[2]) * k);
            set(p, 9, l0[0] + (L[i][0] - l0[0]) * k - pop, l0[1] + (L[i][1] - l0[1]) * k, l0[2] + (L[i][2] - l0[2]) * k);
            float hip = s(t * Math.PI / beat);
            set(p, 3, 0.0, 0.0, hip * 0.05);
            set(p, 12, 0.0, 0.0, 0.05 + Math.max(0.0F, hip) * 0.12);
            set(p, 15, 0.0, 0.0, -0.05 - Math.max(0.0F, -hip) * 0.12);
            set(p, 0, 0.0, 0.0, hip * 0.12);
         }
      );
      add("penguin", "Pinguin-Watscheln", "snow", 6.0F, true, null, 0.0F, (t, p) -> {
         float w = s(t * 6.5);
         float flap = Math.max(0.0F, s(t * 13.0F)) * 0.22F;
         set(p, 6, 0.08, 0.0, 0.28 + flap);
         set(p, 9, 0.08, 0.0, -0.28 - flap);
         set(p, 12, 0.0, 0.0, 0.04 + Math.max(0.0F, w) * 0.2);
         set(p, 15, 0.0, 0.0, -0.04 - Math.max(0.0F, -w) * 0.2);
         set(p, 3, 0.0, 0.0, w * 0.09);
         set(p, 0, -0.12 + Math.abs(w) * 0.06, s(t * 1.2) * 0.25, -w * 0.14);
      });
      add("superhero", "Superheld", "bolt", 4.4F, false, null, 0.0F, (t, p) -> {
         float in = kf(t, 0.0, 0.0, 0.4, 1.0);
         float fly = kf(t, 2.0, 0.0, 2.35, 1.0, 3.9, 1.0, 4.4, 0.0);
         float wind = s(t * 5.0F) * 0.03F;
         set(p, 6, 0.18 * in * (1.0F - fly) - 3.05 * fly, -0.35 * in * (1.0F - fly), 0.55 * in * (1.0F - fly) - 0.08 * fly + wind);
         set(p, 9, 0.18 * in, 0.35 * in, -0.55 * in);
         set(p, 3, -0.07 * in, 0.0, 0.0);
         set(p, 0, -0.2 * in - 0.25 * fly, -0.25 * in * (1.0F - fly), 0.0);
         set(p, 12, -0.3 * fly, 0.0, 0.14 * in * (1.0F - fly));
         set(p, 15, 0.0, 0.0, -0.14 * in);
      });
      add("cs_landing", "Heldenlandung", "bolt", 5.5F, false, null, 0.0F, (t, p) -> {
         float land = ease((t - 0.9F) / 0.25F);
         float up = ease((t - 3.6F) / 0.8F);
         float crouch = land * (1.0F - up);
         set(p, 3, 0.55 * crouch, 0.0, 0.0);
         set(p, 0, -0.35 * crouch - 0.2 * up, 0.0, 0.0);
         set(p, 6, -0.3 * crouch + -2.9 * up, 0.0, 0.15);
         set(p, 9, -1.1 * crouch + 0.1, -0.3 * crouch, -0.4 * crouch - 0.1);
         set(p, 12, -1.35 * crouch, 0.25 * crouch, 0.0);
         set(p, 15, 0.9 * crouch, -0.1 * crouch, 0.0);
      });
      add("cs_victory", "Siegesfeier", "trophy", 6.5F, false, "gg", 0.0F, (t, p) -> {
         float b = Math.abs(s(t * 6.0F)) * 0.25F;
         set(p, 6, -2.95 + b, 0.0, 0.35);
         set(p, 9, -2.95 + b, 0.0, -0.35);
         set(p, 0, -0.3, s(t * 2.0F) * 0.25, 0.0);
         set(p, 12, -Math.abs(s(t * 6.0F)) * 0.25, 0.0, 0.05);
      });
      add("cs_portal", "Portal-Beschwörung", "sparkle", 6.5F, false, null, 0.0F, (t, p) -> {
         float r = ease(t / 1.2F);
         float w = s(t * 3.0F) * 0.08F;
         set(p, 6, -1.6 - 1.2 * r + w, 0.2, 0.5 * r);
         set(p, 9, -1.6 - 1.2 * r - w, -0.2, -0.5 * r);
         set(p, 0, -0.45 * r, 0.0, 0.0);
         set(p, 3, -0.08 * r, 0.0, 0.0);
      });
      add("cs_power", "Energie-Aufladung", "flame", 6.5F, false, "fire", 0.0F, (t, p) -> {
         float charge = ease(t / 3.5F);
         float burst = ease((t - 3.8F) / 0.3F);
         float shake = s(t * 60.0F) * 0.03F * charge * (1.0F - burst);
         set(p, 6, -0.4 * (1.0F - burst) - 2.9 * burst, 0.3 * (1.0F - burst), 0.5 + shake + 0.3 * burst);
         set(p, 9, -0.4 * (1.0F - burst) - 2.9 * burst, -0.3 * (1.0F - burst), -0.5 - shake - 0.3 * burst);
         set(p, 3, 0.25 * charge * (1.0F - burst) - 0.1 * burst, 0.0, shake);
         set(p, 0, 0.3 * charge * (1.0F - burst) - 0.45 * burst, 0.0, 0.0);
         set(p, 12, -0.25 * charge, 0.0, 0.2 * charge);
         set(p, 15, 0.1 * charge, 0.0, -0.2 * charge);
      });
      add("cs_selfie", "Selfie", "camera", 4.5F, false, "heart", 0.0F, (t, p) -> {
         float r = ease(t / 0.6F);
         set(p, 6, -1.9 * r, -0.4 * r, -0.1);
         set(p, 9, -2.3 * r, 0.1, 0.65 * r);
         set(p, 0, -0.15 * r, -0.25 * r, 0.22 * r);
      });
      add("cs_xmas", "Weihnachtszauber", "gift", 8.0F, false, null, 0.0F, (t, p) -> {
         float look = ease((t - 0.6F) / 0.8F);
         float cheer = ease((t - 3.0F) / 0.4F);
         float hop = Math.max(0.0F, s((t - 3.2F) * 9.0F)) * (t > 3.2F && t < 3.55F ? 1 : 0);
         set(p, 0, -0.45 * look * (1.0 - cheer * 0.5), 0.0, 0.0);
         double wave = s(t * 8.0F) * 0.25 * cheer;
         set(p, 6, -0.2 - 2.7 * cheer + wave, 0.0, 0.35 * cheer);
         set(p, 9, -0.2 - 2.7 * cheer - wave, 0.0, -0.35 * cheer);
         set(p, 3, -0.1 * cheer, 0.0, 0.0);
         set(p, 12, -0.3 * hop, 0.0, 0.05);
         set(p, 15, -0.3 * hop, 0.0, -0.05);
      });
      add("cs_darkaura", "Dunkle Aura", "moon", 7.6F, false, null, 0.0F, (t, p) -> {
         float raise = ease((t - 2.7F) / 1.1F);
         float slash = ease((t - 5.2F) / 0.18F);
         float after = ease((t - 5.6F) / 0.8F);
         float breathe = s(t * 2.2) * 0.03F;
         set(p, 0, 0.4 * (1.0F - raise) + breathe * 0.5 - 0.12 * after, 0.0, 0.0);
         set(p, 3, 0.12 * (1.0F - raise) + 0.05 * slash * (1.0F - after), 0.0, 0.0);
         double arm = t < 5.2F ? -0.25 - 0.3 * raise : -2.4 + 2.9 * slash - 0.2 * after;
         if (t >= 5.0F && t < 5.2F) {
            arm = -0.55 - 1.85 * ease((t - 5.0F) / 0.2F);
         }

         set(p, 6, arm, -0.35 * slash * (1.0F - after), 0.12 + 0.5 * slash * (1.0F - after));
         set(p, 9, -0.2 + breathe, 0.1, -0.12);
         set(p, 12, -0.08 * slash, 0.0, 0.06);
         set(p, 15, 0.12 * slash, 0.0, -0.06);
      });
      add(
         "cs_evillaugh",
         "Böses Lachen",
         "eye",
         10.0F,
         false,
         null,
         0.0F,
         (t, p) -> {
            float amp = kf(
               t, 0.0, 0.015, 1.25, 0.02, 1.4, 0.05, 2.5, 0.04, 2.62, 0.085, 3.9, 0.07, 4.3, 0.0, 5.95, 0.0, 6.05, 0.1, 9.1, 0.1, 9.6, 0.02, 10.0, 0.0
            );
            float sh = s(t * 24.0F) * amp;
            float sh2 = s(t * 24.0F + 1.3) * amp;
            set(
               p,
               0,
               kf(t, 0.0, 0.2, 0.8, 0.38, 2.58, 0.38, 2.62, -0.45, 3.9, -0.5, 4.36, 0.0, 5.95, 0.0, 6.05, -0.35, 6.4, -0.72, 9.1, -0.72, 9.8, -0.1) + sh,
               0.0,
               kf(t, 0.0, 0.0, 0.8, 0.12, 2.58, 0.12, 2.62, -0.08, 3.9, -0.08, 4.36, 0.0, 5.95, 0.0, 6.3, 0.1, 9.1, 0.1, 9.8, 0.0) + sh2 * 0.5
            );
            set(
               p,
               3,
               kf(t, 0.0, 0.05, 0.8, 0.15, 2.58, 0.15, 2.62, -0.08, 3.9, -0.1, 4.36, 0.0, 5.95, 0.0, 6.1, -0.18, 9.1, -0.18, 9.8, 0.0) + sh2 * 0.5,
               0.0,
               0.0
            );
            set(
               p,
               9,
               kf(t, 0.0, 0.0, 0.8, -2.35, 2.58, -2.35, 2.62, -2.75, 3.9, -2.75, 4.3, -0.15, 5.95, -0.15, 6.15, -0.5, 9.1, -0.5, 9.8, -0.1) + sh,
               kf(t, 0.0, 0.0, 0.8, 0.35, 3.9, 0.35, 4.3, 0.0),
               kf(t, 0.0, -0.05, 0.8, -0.3, 3.9, -0.3, 4.3, -0.08, 5.95, -0.08, 6.15, -1.95, 9.1, -1.95, 9.8, -0.3) - sh
            );
            set(
               p,
               6,
               kf(t, 0.0, 0.0, 0.8, -0.8, 3.9, -0.8, 4.3, -0.1, 5.95, -0.1, 6.15, -0.5, 9.1, -0.5, 9.8, -0.1) + sh2,
               kf(t, 0.0, 0.0, 0.8, -0.55, 3.9, -0.55, 4.3, 0.0),
               kf(t, 0.0, 0.05, 0.8, 0.0, 3.9, 0.0, 4.3, 0.08, 5.95, 0.08, 6.15, 1.95, 9.1, 1.95, 9.8, 0.3) + sh
            );
            set(p, 12, kf(t, 5.95, 0.0, 6.15, -0.12, 9.8, 0.0), 0.0, kf(t, 0.0, 0.03, 5.95, 0.03, 6.15, 0.16, 9.8, 0.04));
            set(p, 15, kf(t, 5.95, 0.0, 6.15, 0.1, 9.8, 0.0), 0.0, kf(t, 0.0, -0.03, 5.95, -0.03, 6.15, -0.16, 9.8, -0.04));
         }
      );
      add("cs_starfall", "Sternenregen", "star", 7.0F, false, null, 0.0F, (t, p) -> {
         float r = ease((t - 0.5F) / 1.5F);
         set(p, 6, -0.2 - 2.3 * r, 0.0, 0.9 * r);
         set(p, 9, -0.2 - 2.3 * r, 0.0, -0.9 * r);
         set(p, 0, -0.75 * r, s(t * 0.8) * 0.2 * r, 0.0);
         set(p, 3, -0.12 * r, 0.0, 0.0);
      });
      add("x_victory", "Sieg", "trophy", 2.6F, false, null, 0.0F, (t, p) -> {
         float b = s(t * 5.0F) * 0.1F;
         set(p, 6, -2.7 + b, 0.0, 0.6);
         set(p, 9, -2.7 + b, 0.0, -0.6);
         set(p, 0, -0.3, 0.0, 0.0);
      });
      add("x_think", "Nachdenken", "info", 3.5F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -1.9, 0.0, -0.75);
         set(p, 9, -0.7, 0.0, 0.5);
         set(p, 0, 0.2, 0.2 + s(t * 1.5) * 0.1, 0.2);
      });
      add("x_yawn", "Gähnen", "moon", 3.2F, false, null, 0.0F, (t, p) -> {
         double k = Math.sin(Math.min(1.0, t / 3.2) * Math.PI);
         set(p, 6, -3.0 * k, 0.0, 0.3 * k);
         set(p, 9, -3.0 * k, 0.0, -0.3 * k);
         set(p, 0, -0.4 * k, 0.0, 0.0);
      });
      add("x_jacks", "Hampelmann", "jump", 4.0F, true, null, 0.0F, (t, p) -> {
         double k = tri(t * 1.6);
         set(p, 6, 0.0, 0.0, 0.2 + k * 2.6);
         set(p, 9, 0.0, 0.0, -0.2 - k * 2.6);
         set(p, 12, 0.0, 0.0, k * 0.4);
         set(p, 15, 0.0, 0.0, -k * 0.4);
      });
      add("x_chicken", "Huhn", "paw", 5.0F, true, null, 0.0F, (t, p) -> {
         double k = Math.abs(s(t * 10.0F));
         set(p, 6, 0.2, 0.0, 0.3 + k * 0.6);
         set(p, 9, 0.2, 0.0, -0.3 - k * 0.6);
         set(p, 0, s(t * 5.0F) * 0.3, 0.0, 0.0);
      });
      add("x_swim", "Kraulen", "wing", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.57 + c(t * 5.0F) * 1.6, 0.0, 0.1);
         set(p, 9, -1.57 - c(t * 5.0F) * 1.6, 0.0, -0.1);
         set(p, 0, 0.0, s(t * 2.5) * 0.4, 0.0);
      });
      add("x_karate", "Karate", "bolt", 3.0F, false, null, 0.0F, (t, p) -> {
         double k = tri(t * 1.3);
         set(p, 6, -2.6 + k * 2.2, 0.0, 0.2);
         set(p, 9, -0.9, 0.0, -0.4);
         set(p, 12, -0.5 * k, 0.0, 0.0);
      });
      add("x_sword", "Schwerthieb", "sword", 2.6F, false, null, 0.0F, (t, p) -> {
         double k = Math.min(1.0, t / 0.6);
         double sw = t < 0.6 ? -2.9 * k : -2.9 + Math.min(1.0, (t - 0.6) / 0.25) * 2.6;
         set(p, 6, sw, 0.0, -0.3);
         set(p, 3, 0.0, s(t * 2.0F) * 0.2, 0.0);
      });
      add("x_archer", "Bogenschütze", "target", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 9, -1.57, 0.1, 0.0);
         set(p, 6, -1.57, -0.9, 0.0);
         set(p, 0, 0.0, 0.6, 0.0);
      });
      add("x_conductor", "Dirigent", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.0 + s(t * 5.0F) * 0.6, 0.0, -0.3 + c(t * 5.0F) * 0.4);
         set(p, 9, -1.3, 0.0, 0.2);
         set(p, 0, -0.1, s(t * 2.5) * 0.2, 0.0);
      });
      add("x_piano", "Klavier", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.0 + s(t * 11.0F) * 0.08, -0.25 + s(t * 2.0F) * 0.15, 0.0);
         set(p, 9, -1.0 + s(t * 13.0F) * 0.08, 0.25 + s(t * 2.3) * 0.15, 0.0);
         set(p, 0, 0.35, s(t * 1.5) * 0.15, 0.0);
      });
      add("x_violin", "Geige", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 9, -1.8, 0.7, 0.0);
         set(p, 6, -1.3, -0.4 + s(t * 6.0F) * 0.45, 0.0);
         set(p, 0, 0.2, 0.4, 0.3);
      });
      add("x_dj", "DJ", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.4, 0.0, -0.9);
         set(p, 9, -1.1, 0.4 + s(t * 10.0F) * 0.3, 0.0);
         set(p, 0, 0.25 + Math.abs(s(t * 8.0F)) * 0.2, 0.0, 0.2);
      });
      add("x_phone", "Telefonieren", "chat", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.3, 0.0, -1.0);
         set(p, 0, 0.1, s(t * 1.2) * 0.3, 0.2);
         set(p, 9, s(t * 2.0F) * 0.3, 0.0, -0.1);
      });
      add("x_look", "Umschauen", "eye", 4.0F, false, null, 0.0F, (t, p) -> {
         set(p, 0, -0.1, s(t * 1.6) * 0.9, 0.0);
         set(p, 6, -2.6, 0.0, -0.9);
      });
      add("x_nod", "Nicken", "check", 2.0F, false, null, 0.0F, (t, p) -> set(p, 0, 0.25 + s(t * 10.0F) * 0.3, 0.0, 0.0));
      add("x_no", "Kopfschütteln", "x", 2.0F, false, null, 0.0F, (t, p) -> set(p, 0, 0.05, s(t * 12.0F) * 0.5, 0.0));
      add("x_thumbs", "Daumen hoch", "star", 2.4F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -1.3, 0.0, -0.3);
         set(p, 0, 0.0, 0.0, -0.15);
      });
      add("x_flex", "Muskeln", "boost", 3.0F, false, null, 0.0F, (t, p) -> {
         double k = 0.15 * Math.abs(s(t * 4.0F));
         set(p, 6, -1.0 - k, 0.0, 1.57);
         set(p, 9, -1.0 - k, 0.0, -1.57);
         set(p, 0, -0.15, 0.0, 0.0);
      });
      add("x_plane", "Flugzeug", "wing", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, 0.0, 0.0, 1.57);
         set(p, 9, 0.0, 0.0, -1.57);
         set(p, 3, 0.0, 0.0, s(t * 2.0F) * 0.35);
         set(p, 0, 0.0, 0.0, s(t * 2.0F) * 0.3);
      });
      add("x_heli", "Helikopter", "sky", 4.0F, true, null, 520.0F, (t, p) -> {
         set(p, 6, 0.0, 0.0, 1.57);
         set(p, 9, 0.0, 0.0, -1.57);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("x_ballet", "Ballett", "sparkle", 4.0F, true, null, 300.0F, (t, p) -> {
         set(p, 6, -3.0, 0.0, -0.35);
         set(p, 9, -3.0, 0.0, 0.35);
         set(p, 15, -0.6, 0.0, -0.3);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("x_hula", "Hula", "sun", 6.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 6.0F);
         set(p, 3, 0.0, 0.0, k * 0.15);
         set(p, 6, -0.5, 0.0, 1.2 + k * 0.3);
         set(p, 9, -0.5, 0.0, -1.2 + k * 0.3);
         set(p, 12, 0.0, 0.0, -k * 0.1);
         set(p, 15, 0.0, 0.0, -k * 0.1);
      });
      add("x_moonwalk", "Moonwalk", "moon", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 7.0F);
         set(p, 12, k * 0.5, 0.0, 0.0);
         set(p, 15, -k * 0.5, 0.0, 0.0);
         set(p, 6, -k * 0.3, 0.0, 0.15);
         set(p, 9, k * 0.3, 0.0, -0.15);
         set(p, 0, 0.1, 0.0, 0.0);
      });
      add("x_runningman", "Running Man", "run", 5.0F, true, null, 0.0F, (t, p) -> {
         double k = tri(t * 2.4) * 2.0 - 1.0;
         set(p, 12, -0.9 * Math.max(0.0, k), 0.0, 0.0);
         set(p, 15, -0.9 * Math.max(0.0, -k), 0.0, 0.0);
         set(p, 6, -1.0 - 0.4 * k, 0.0, 0.2);
         set(p, 9, -1.0 + 0.4 * k, 0.0, -0.2);
      });
      add("x_disco", "Disco", "star", 6.0F, true, null, 0.0F, (t, p) -> {
         boolean up = (int)(t * 2.5) % 2 == 0;
         set(p, 6, up ? -2.9 : -0.2, 0.0, up ? 0.5 : -0.5);
         set(p, 9, 0.1, 0.0, -0.2);
         set(p, 3, 0.0, 0.0, up ? -0.08 : 0.08);
         set(p, 0, up ? -0.3 : 0.2, 0.0, 0.0);
      });
      add("x_wave2", "Beidhändig winken", "emote", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.75, 0.0, 0.3 + s(t * 9.0F) * 0.45);
         set(p, 9, -2.75, 0.0, -0.3 - s(t * 9.0F) * 0.45);
      });
      add("x_ovation", "Standing Ovation", "trophy", 4.0F, true, null, 0.0F, (t, p) -> {
         double k = Math.abs(s(t * 8.0F)) * 0.4;
         set(p, 6, -2.6, 0.0, -0.05 - k);
         set(p, 9, -2.6, 0.0, 0.05 + k);
         set(p, 0, -0.3, 0.0, 0.0);
      });
      add("x_angry", "Wütend", "flame", 3.0F, false, "fire", 0.0F, (t, p) -> {
         float sh = s(t * 25.0F) * 0.06F;
         set(p, 6, 0.2, 0.0, 0.25 + sh);
         set(p, 9, 0.2, 0.0, -0.25 - sh);
         set(p, 0, 0.2, sh, 0.0);
         set(p, 3, 0.05, 0.0, sh);
      });
      add("x_scared", "Erschrocken", "alert", 3.0F, false, null, 0.0F, (t, p) -> {
         float sh = s(t * 30.0F) * 0.05F;
         set(p, 6, -1.9 + sh, 0.0, -0.4);
         set(p, 9, -1.9 - sh, 0.0, 0.4);
         set(p, 0, -0.3, 0.0, 0.0);
         set(p, 3, -0.1, 0.0, sh);
      });
      add("x_shy", "Schüchtern", "heart", 4.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 2.0F);
         set(p, 6, 0.4, 0.0, 0.25);
         set(p, 9, 0.4, 0.0, -0.25);
         set(p, 0, 0.45, k * 0.25, k * 0.1);
         set(p, 3, 0.0, 0.0, k * 0.05);
      });
      add("x_kiss", "Kusshand", "heart", 2.6F, false, "heart", 0.0F, (t, p) -> {
         double k = Math.min(1.0, t / 1.2);
         set(p, 6, -2.3 + k * 0.9, 0.0, -0.6 + k * 0.6);
         set(p, 0, -0.1, 0.0, 0.15);
      });
      add("x_pray", "Beten", "star", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.25, 0.0, -0.45);
         set(p, 9, -1.25, 0.0, 0.45);
         set(p, 0, 0.45, 0.0, 0.0);
      });
      add("x_stretch", "Dehnen", "boost", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 2.0F);
         set(p, 6, -2.9, 0.0, 0.15);
         set(p, 9, -2.9, 0.0, -0.15);
         set(p, 3, 0.0, 0.0, k * 0.3);
         set(p, 0, 0.0, 0.0, k * 0.3);
      });
      add("x_lookup", "Ausschau halten", "sun", 3.5F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.5, 0.0, -0.8);
         set(p, 0, -0.5, s(t * 1.2) * 0.4, 0.0);
      });
      add("x_hello", "Hallo!", "emote", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.9, 0.0, 0.6 + s(t * 11.0F) * 0.35);
         set(p, 9, -2.9, 0.0, -0.6 - s(t * 11.0F + 1.0F) * 0.35);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("x_come", "Herkommen", "user", 3.0F, false, null, 0.0F, (t, p) -> set(p, 6, -1.4 + Math.abs(s(t * 6.0F)) * -0.6, 0.0, -0.1));
      add("x_stop", "Stopp", "x", 2.4F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -1.57, 0.0, 0.0);
         set(p, 0, 0.05, 0.0, 0.0);
      });
      add("x_mummy", "Mumie", "skull", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.57, 0.05, 0.0);
         set(p, 9, -1.57, -0.05, 0.0);
         set(p, 3, 0.0, 0.0, s(t * 2.0F) * 0.1);
         set(p, 0, 0.1, 0.0, s(t * 2.0F) * 0.15);
      });
      add("x_penguin", "Pinguin", "paw", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 7.0F);
         set(p, 6, 0.0, 0.0, 0.35 + Math.abs(k) * 0.3);
         set(p, 9, 0.0, 0.0, -0.35 - Math.abs(k) * 0.3);
         set(p, 3, 0.0, 0.0, k * 0.12);
         set(p, 0, 0.0, 0.0, k * 0.15);
      });
      add("x_crab", "Krabbe", "paw", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 8.0F);
         set(p, 6, -2.2, 0.0, 1.0 + k * 0.2);
         set(p, 9, -2.2, 0.0, -1.0 + k * 0.2);
         set(p, 12, 0.0, 0.0, 0.3 + k * 0.1);
         set(p, 15, 0.0, 0.0, -0.3 + k * 0.1);
      });
      add("x_monkey", "Affe", "paw", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.9, 0.0, -0.3 + s(t * 9.0F) * 0.2);
         set(p, 9, -0.6, 0.0, -0.9 + s(t * 9.0F) * 0.2);
         set(p, 0, 0.1, s(t * 4.0F) * 0.3, 0.0);
      });
      add("x_fish", "Angeln", "flag", 5.0F, false, null, 0.0F, (t, p) -> {
         double k = t < 1.0F ? -2.8 * t : (t < 1.3 ? -2.8 + (t - 1.0F) / 0.3 * 2.0 : -0.8 + s(t * 2.0F) * 0.05);
         set(p, 6, k, -0.1, 0.0);
         set(p, 9, -1.0, 0.3, 0.0);
      });
      add("x_axe", "Holz hacken", "wand", 5.0F, true, null, 0.0F, (t, p) -> {
         double k = tri(t * 1.4);
         set(p, 6, -2.9 + k * 2.4, 0.0, 0.0);
         set(p, 9, -2.9 + k * 2.4, 0.0, 0.0);
         set(p, 3, 0.2 * k, 0.0, 0.0);
      });
      add("x_mine", "Abbauen", "brick", 5.0F, true, null, 0.0F, (t, p) -> {
         double k = tri(t * 2.2);
         set(p, 6, -2.5 + k * 1.8, 0.2, 0.0);
         set(p, 0, 0.3, 0.0, 0.0);
      });
      add("x_drink", "Trinken", "potion", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.0, 0.0, -0.7);
         set(p, 0, -0.5 + s(t * 6.0F) * 0.05, 0.0, 0.0);
      });
      add("x_eat", "Essen", "food", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.6 - Math.abs(s(t * 6.0F)) * 0.5, 0.0, -0.6);
         set(p, 0, 0.15 + Math.abs(s(t * 6.0F)) * 0.1, 0.0, 0.0);
      });
      add("x_sneeze", "Niesen", "alert", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = t < 1.2 ? -0.4 * t / 1.2 : (t < 1.4 ? -0.4 + (t - 1.2) / 0.2 * 1.0 : 0.6 - (t - 1.4) * 0.5);
         set(p, 0, k, 0.0, 0.0);
         set(p, 3, k * 0.25, 0.0, 0.0);
         set(p, 6, -1.9, 0.0, -0.6);
      });
      add("x_lap", "Ehrenrunde", "trophy", 4.0F, true, null, 180.0F, (t, p) -> {
         set(p, 6, -2.9, 0.0, 0.2 + s(t * 6.0F) * 0.2);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("x_royal", "Königlich verbeugen", "crown", 3.0F, false, null, 0.0F, (t, p) -> {
         double k = Math.sin(Math.min(1.0F, t / 3.0F) * Math.PI);
         set(p, 3, 0.7 * k, 0.0, 0.0);
         set(p, 0, 0.6 * k, 0.0, 0.0);
         set(p, 6, -1.2 * k, 0.0, -0.6 * k);
         set(p, 9, 0.5 * k, 0.0, -0.4 * k);
         set(p, 12, 0.3 * k, 0.0, 0.0);
      });
      add("x_horse", "Pferdetanz", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         float b = Math.abs(s(t * 8.0F));
         set(p, 6, -1.3 - b * 0.2, 0.0, -0.4);
         set(p, 9, -1.3 - b * 0.2, 0.0, 0.4);
         set(p, 12, -0.5 * b, 0.0, 0.1);
         set(p, 15, -0.5 * (1.0F - b), 0.0, -0.1);
         set(p, 0, 0.1 + b * 0.15, 0.0, 0.0);
      });
      add("x_snowball", "Weitwurf", "snow", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = t < 0.8 ? t / 0.8 : Math.max(0.0, 1.0 - (t - 0.8) / 0.3);
         set(p, 6, -2.6 * k + (t > 0.8 ? -0.9 : 0.0), 0.0, -0.2);
         set(p, 3, 0.0, -0.3 * k, 0.0);
      });
      add("x_peek", "Gucken", "eye", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 3, 0.0, 0.0, s(Math.min(1.0, t / 0.6) * Math.PI / 2.0) * 0.35);
         set(p, 0, 0.0, 0.0, 0.35);
         set(p, 6, -2.4, 0.0, -0.9);
      });
      add("x_wipe", "Schweiß abwischen", "sun", 2.6F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.6, 0.3 + s(t * 4.0F) * 0.5, -0.8);
         set(p, 0, -0.15, 0.0, 0.0);
      });
      add("x_tired", "Erschöpft", "hourglass", 4.0F, true, null, 0.0F, (t, p) -> {
         float br = s(t * 4.0F) * 0.06F;
         set(p, 3, 0.45 + br, 0.0, 0.0);
         set(p, 0, 0.3, 0.0, 0.0);
         set(p, 6, -0.6, 0.0, 0.1);
         set(p, 9, -0.6, 0.0, -0.1);
      });
      add("x_shuffle", "Shuffle", "music", 6.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 10.0F);
         set(p, 12, -Math.max(0.0F, k) * 0.7, 0.0, 0.15);
         set(p, 15, -Math.max(0.0F, -k) * 0.7, 0.0, -0.15);
         set(p, 6, -0.4, 0.0, 0.3 + Math.abs(k) * 0.2);
         set(p, 9, -0.4, 0.0, -0.3 - Math.abs(k) * 0.2);
         set(p, 0, 0.1 + Math.abs(k) * 0.1, 0.0, 0.0);
      });
      add("x_noodle", "Wackelarme", "wand", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.2 + s(t * 7.0F) * 0.9, 0.0, 1.0 + c(t * 9.0F) * 0.5);
         set(p, 9, -1.2 + c(t * 7.0F) * 0.9, 0.0, -1.0 - s(t * 9.0F) * 0.5);
         set(p, 0, 0.0, s(t * 3.0F) * 0.3, s(t * 5.0F) * 0.2);
      });
      add("x_rowing", "Rudern", "flag", 5.0F, true, null, 0.0F, (t, p) -> {
         double k = tri(t * 0.9);
         set(p, 6, -1.57 + k * 1.0, 0.0, -0.15);
         set(p, 9, -1.57 + k * 1.0, 0.0, 0.15);
         set(p, 3, 0.35 * (1.0 - k), 0.0, 0.0);
      });
      add("x_juggle", "Jonglieren", "sparkle", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -1.3 - Math.max(0.0F, s(t * 8.0F)) * 0.6, 0.0, -0.1);
         set(p, 9, -1.3 - Math.max(0.0F, -s(t * 8.0F)) * 0.6, 0.0, 0.1);
         set(p, 0, -0.4, s(t * 4.0F) * 0.2, 0.0);
      });
      add("x_magic", "Zaubern", "wand", 3.0F, false, null, 0.0F, (t, p) -> {
         double k = Math.min(1.0, t / 1.2);
         set(p, 6, -1.5 - k * 1.4, 0.0, -0.2 + s(t * 14.0F) * 0.15);
         set(p, 9, -1.0, 0.0, 0.4);
         set(p, 0, -0.2 * k, 0.0, 0.0);
      });
      add("x_spin", "Drehen", "refresh", 3.0F, false, null, 600.0F, (t, p) -> {
         set(p, 6, -0.6, 0.0, 1.2);
         set(p, 9, -0.6, 0.0, -1.2);
         set(p, 0, -0.25, 0.0, 0.0);
      });
      add("y_robo2", "Roboterarme", "chip", 5.0F, true, null, 0.0F, (t, p) -> {
         int st = (int)(t * 4.0F) % 4;
         set(p, 6, st < 2 ? -1.57 : 0.0, 0.0, st % 2 == 0 ? 0.0 : 1.57);
         set(p, 9, st >= 2 ? -1.57 : 0.0, 0.0, st % 2 == 1 ? 0.0 : -1.57);
         set(p, 0, 0.0, st * 0.25 - 0.4, 0.0);
      });
      add("y_wave_low", "Leise winken", "emote", 2.4F, false, null, 0.0F, (t, p) -> set(p, 6, -1.3, 0.0, 0.2 + s(t * 10.0F) * 0.25));
      add("y_bye", "Tschüss", "emote", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.8, 0.0, 0.4 + s(t * 7.0F) * 0.5);
         set(p, 0, 0.0, 0.3, 0.0);
      });
      add("y_clapslow", "Langsamer Applaus", "sparkle", 4.0F, true, null, 0.0F, (t, p) -> {
         double k = Math.max(0.0F, s(t * 2.6)) * 0.45;
         set(p, 6, -1.35, 0.0, -0.05 - k);
         set(p, 9, -1.35, 0.0, 0.05 + k);
         set(p, 0, 0.1, 0.0, 0.15);
      });
      add("y_hands_hips", "Hände in die Hüften", "user", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -0.2, 0.0, 0.75);
         set(p, 9, -0.2, 0.0, -0.75);
         set(p, 0, -0.1, 0.0, 0.0);
      });
      add("y_arms_cross", "Arme verschränken", "user", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -1.25, 0.0, -0.75);
         set(p, 9, -1.15, 0.0, 0.75);
         set(p, 0, 0.05, -0.2, 0.0);
      });
      add("y_tap", "Ungeduldig tippen", "clock", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -0.2, 0.0, 0.75);
         set(p, 9, -0.2, 0.0, -0.75);
         set(p, 12, -Math.max(0.0F, s(t * 12.0F)) * 0.35, 0.0, 0.0);
      });
      add("y_scratch", "Kopf kratzen", "info", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.6, 0.0, -0.9 + s(t * 14.0F) * 0.1);
         set(p, 0, 0.1, 0.0, 0.25);
      });
      add("y_peace", "Peace", "star", 2.6F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.4, 0.0, 0.15);
         set(p, 0, 0.0, 0.0, -0.2);
      });
      add("y_ok", "Alles okay", "check", 2.4F, false, null, 0.0F, (t, p) -> set(p, 6, -1.5, -0.3, -0.2));
      add("y_shush", "Psst", "mute", 2.6F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.1, 0.0, -0.55);
         set(p, 0, 0.15, 0.0, 0.0);
      });
      add("y_listen", "Lauschen", "sound", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.6, 0.3, -1.1);
         set(p, 0, 0.05, 0.0, 0.35);
      });
      add("y_celebrate", "Feiern", "trophy", 5.0F, true, null, 0.0F, (t, p) -> {
         double b = Math.abs(s(t * 8.0F));
         set(p, 6, -2.6 - b * 0.3, 0.0, 0.6);
         set(p, 9, -2.6 - b * 0.3, 0.0, -0.6);
         set(p, 12, -0.3 * b, 0.0, 0.0);
         set(p, 0, -0.3, 0.0, 0.0);
      });
      add("y_fistpump", "Faust hoch", "trophy", 2.6F, false, null, 0.0F, (t, p) -> {
         double k = Math.abs(s(t * 7.0F));
         set(p, 6, -1.6 - k * 1.2, 0.0, 0.1);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("y_sad", "Traurig", "rain", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 0, 0.7, 0.0, s(t * 1.2) * 0.1);
         set(p, 6, 0.15, 0.0, 0.05);
         set(p, 9, 0.15, 0.0, -0.05);
         set(p, 3, 0.15, 0.0, 0.0);
      });
      add("y_sigh", "Seufzen", "cloud", 2.6F, false, null, 0.0F, (t, p) -> {
         double k = Math.sin(Math.min(1.0, t / 2.6) * Math.PI);
         set(p, 0, -0.3 * k + 0.4 * (1.0 - k), 0.0, 0.0);
         set(p, 6, 0.2 * k, 0.0, 0.3 * k);
         set(p, 9, 0.2 * k, 0.0, -0.3 * k);
      });
      add("y_excited", "Aufgeregt", "bolt", 4.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 16.0F);
         set(p, 6, -1.8 + k * 0.3, 0.0, 0.4);
         set(p, 9, -1.8 - k * 0.3, 0.0, -0.4);
         set(p, 0, -0.15, k * 0.15, 0.0);
      });
      add("y_bored", "Gelangweilt", "hourglass", 6.0F, true, null, 0.0F, (t, p) -> {
         set(p, 0, 0.35, s(t * 0.8) * 0.5, 0.2);
         set(p, 6, 0.1, 0.0, 0.1);
         set(p, 9, 0.1, 0.0, -0.1);
      });
      add("y_confused", "Verwirrt", "info", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 0, 0.05, s(t * 3.0F) * 0.4, 0.35);
         set(p, 6, -0.6, 0.8, 1.0);
         set(p, 9, -0.6, -0.8, -1.0);
      });
      add("y_surprise", "Überrascht", "alert", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = Math.min(1.0F, t * 4.0F);
         set(p, 6, -2.3 * k, 0.0, 0.6 * k);
         set(p, 9, -2.3 * k, 0.0, -0.6 * k);
         set(p, 0, -0.35 * k, 0.0, 0.0);
      });
      add("y_love", "Verliebt", "heart", 4.0F, true, "heart", 0.0F, (t, p) -> {
         float k = s(t * 3.0F);
         set(p, 6, -1.7, 0.0, -0.6);
         set(p, 9, -1.7, 0.0, 0.6);
         set(p, 0, -0.1, 0.0, k * 0.25);
         set(p, 3, 0.0, 0.0, k * 0.08);
      });
      add("y_wink", "Zwinkern", "eye", 2.2F, false, null, 0.0F, (t, p) -> {
         set(p, 0, 0.0, 0.2, 0.3);
         set(p, 6, -1.6, -0.3, -0.2);
      });
      add("y_hug", "Umarmen", "heart", 3.0F, false, "heart", 0.0F, (t, p) -> {
         double k = Math.min(1.0F, t * 2.0F);
         set(p, 6, -1.5, -0.7 * k, -0.2);
         set(p, 9, -1.5, 0.7 * k, 0.2);
         set(p, 0, 0.1, 0.0, 0.15);
      });
      add("y_highfive", "High Five", "emote", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = t < 1.0F ? t : 1.0;
         set(p, 6, -2.6 * k, -0.2, 0.2);
      });
      add("y_fistbump", "Fist Bump", "hit", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = t < 0.6 ? t / 0.6 : 1.0;
         set(p, 6, -1.57 * k, -0.1, 0.0);
      });
      add("y_signal", "Winken (Notruf)", "alert", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.9, 0.0, 0.4 + s(t * 6.0F) * 0.6);
         set(p, 9, -2.9, 0.0, -0.4 - s(t * 6.0F) * 0.6);
      });
      add("y_salute2", "Militärgruß", "shield", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.2, 0.3, -0.85);
         set(p, 0, -0.05, 0.0, 0.0);
         set(p, 9, 0.0, 0.0, -0.05);
      });
      add("y_kungfu", "Kung-Fu", "bolt", 4.0F, true, null, 0.0F, (t, p) -> {
         boolean a = (int)(t * 2.0F) % 2 == 0;
         set(p, 6, a ? -1.57 : -0.5, 0.0, a ? 0.0 : 0.9);
         set(p, 9, a ? -0.5 : -1.57, 0.0, a ? -0.9 : 0.0);
         set(p, 12, a ? -0.9 : 0.0, 0.0, 0.1);
      });
      add("y_ninja", "Ninja-Pose", "sword", 3.0F, false, null, 0.0F, (t, p) -> {
         set(p, 6, -2.0, 0.5, -0.6);
         set(p, 9, -1.0, -0.3, 0.4);
         set(p, 3, 0.2, 0.4, 0.0);
         set(p, 0, 0.0, -0.4, 0.0);
         set(p, 15, -0.5, 0.0, -0.2);
      });
      add("y_wizard", "Zauberspruch", "wand", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.6 + s(t * 5.0F) * 0.3, 0.0, -0.3 + c(t * 5.0F) * 0.3);
         set(p, 9, -2.6 - s(t * 5.0F) * 0.3, 0.0, 0.3 - c(t * 5.0F) * 0.3);
         set(p, 0, -0.3, 0.0, 0.0);
      });
      add("y_lasso", "Lasso", "ring", 4.0F, true, null, 0.0F, (t, p) -> {
         set(p, 6, -2.6, s(t * 9.0F) * 0.6, c(t * 9.0F) * 0.5);
         set(p, 9, -0.4, 0.0, -0.3);
         set(p, 0, -0.2, 0.0, 0.0);
      });
      add("y_skate", "Skaten", "speed", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 2.0F);
         set(p, 3, 0.1, 0.3, k * 0.1);
         set(p, 6, -0.4, 0.0, 1.0 + k * 0.2);
         set(p, 9, -0.4, 0.0, -1.0 + k * 0.2);
         set(p, 12, -0.3, 0.3, 0.0);
         set(p, 15, 0.2, 0.3, 0.0);
      });
      add("y_surf", "Surfen", "rain", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 2.4);
         set(p, 3, 0.15, -0.5, k * 0.12);
         set(p, 6, 0.0, 0.0, 1.3 + k * 0.2);
         set(p, 9, 0.0, 0.0, -1.3 + k * 0.2);
         set(p, 12, -0.4, -0.5, 0.0);
         set(p, 15, 0.3, -0.5, 0.0);
      });
      add("y_ski", "Skifahren", "snow", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 3.0F);
         set(p, 3, 0.35, k * 0.3, 0.0);
         set(p, 6, -0.6, 0.0, 0.3);
         set(p, 9, -0.6, 0.0, -0.3);
         set(p, 12, -0.5, k * 0.3, 0.0);
         set(p, 15, -0.5, k * 0.3, 0.0);
         set(p, 0, -0.3, 0.0, 0.0);
      });
      add("y_bike", "Radfahren", "speed", 5.0F, true, null, 0.0F, (t, p) -> {
         set(p, 12, -1.0 + s(t * 8.0F) * 0.6, 0.0, 0.0);
         set(p, 15, -1.0 - s(t * 8.0F) * 0.6, 0.0, 0.0);
         set(p, 6, -1.3, 0.0, 0.0);
         set(p, 9, -1.3, 0.0, 0.0);
         set(p, 3, 0.3, 0.0, 0.0);
      });
      add("y_climb", "Klettern", "jump", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 5.0F);
         set(p, 6, -2.7 + k * 0.5, 0.0, 0.1);
         set(p, 9, -2.7 - k * 0.5, 0.0, -0.1);
         set(p, 12, -0.6 * Math.max(0.0F, k), 0.0, 0.0);
         set(p, 15, -0.6 * Math.max(0.0F, -k), 0.0, 0.0);
      });
      add("y_stomp", "Stampfen", "hit", 4.0F, true, null, 0.0F, (t, p) -> {
         double k = Math.max(0.0F, s(t * 6.0F));
         set(p, 12, -1.0 * k, 0.0, 0.0);
         set(p, 6, -0.5, 0.0, 0.6);
         set(p, 9, -0.5, 0.0, -0.6);
         set(p, 0, 0.2 * k, 0.0, 0.0);
      });
      add("y_kick", "Kicken", "hit", 2.4F, false, null, 0.0F, (t, p) -> {
         double k = t < 0.5 ? t / 0.5 * 0.6 : (t < 0.8 ? 0.6 - (t - 0.5) / 0.3 * 2.0 : -1.4 + (t - 0.8) * 0.8);
         set(p, 12, Math.min(0.6, k), 0.0, 0.0);
         set(p, 9, -0.8, 0.0, -0.4);
         set(p, 6, 0.4, 0.0, 0.3);
      });
      add("y_bow3", "Knicks", "crown", 2.6F, false, null, 0.0F, (t, p) -> {
         double k = Math.sin(Math.min(1.0, t / 2.6) * Math.PI);
         set(p, 12, 0.5 * k, 0.0, 0.0);
         set(p, 15, -0.3 * k, 0.0, 0.0);
         set(p, 6, 0.0, 0.0, 0.45 * k);
         set(p, 9, 0.0, 0.0, -0.45 * k);
         set(p, 0, 0.35 * k, 0.0, 0.0);
      });
      add("y_67", "67", "star", 5.0F, true, null, 0.0F, (t, p) -> {
         float k = s(t * 7.0F);
         set(p, 6, -1.25 + k * 0.45, 0.15, -0.15);
         set(p, 9, -1.25 - k * 0.45, -0.15, 0.15);
         set(p, 0, 0.05, s(t * 3.5) * 0.15, k * 0.05);
         set(p, 3, 0.0, 0.0, k * 0.04);
      });
      add("y_freeze", "Einfrieren", "snow", 3.0F, false, null, 0.0F, (t, p) -> {
         float sh = s(t * 40.0F) * 0.03F;
         set(p, 6, -1.0 + sh, 0.0, -0.7);
         set(p, 9, -1.0 - sh, 0.0, 0.7);
         set(p, 0, 0.25, sh, 0.0);
         set(p, 3, 0.05, 0.0, sh);
      });
      EmotePack.addAll();
      BakedEmotes.addAll();
   }

   public static final class Emote {
      public final String id;
      public final String name;
      public final String icon;
      public final float duration;
      public final boolean loop;
      public final EmoteLib.PoseFn fn;
      public final String bubble;
      public final float spin;
      /** Baked launcher emote (moves, turns and scales the parts), or null. */
      public BakedEmotes.Clip clip;

      Emote(String id, String name, String icon, float duration, boolean loop, String bubble, float spin, EmoteLib.PoseFn fn) {
         this.id = id;
         this.name = name;
         this.icon = icon;
         this.duration = duration;
         this.loop = loop;
         this.bubble = bubble;
         this.spin = spin;
         this.fn = fn;
      }

      public float[] pose(float t) {
         float[] p = new float[18];
         Arrays.fill(p, Float.NaN);
         this.fn.pose(t, p);
         return p;
      }
   }

   public interface PoseFn {
      void pose(float var1, float[] var2);
   }
}
