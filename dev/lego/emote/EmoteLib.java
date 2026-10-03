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

   private static void add(String var0, String var1, String var2, float var3, boolean var4, String var5, float var6, EmoteLib.PoseFn var7) {
      ALL.add(new EmoteLib.Emote(var0, var1, var2, var3, var4, var5, var6, var7));
   }

   public static EmoteLib.Emote get(String var0) {
      for (EmoteLib.Emote var2 : ALL) {
         if (var2.id.equals(var0)) {
            return var2;
         }
      }

      return null;
   }

   public static int index(String var0) {
      for (int var1 = 0; var1 < ALL.size(); var1++) {
         if (ALL.get(var1).id.equals(var0)) {
            return var1;
         }
      }

      return -1;
   }

   static float s(double var0) {
      return (float)Math.sin(var0);
   }

   static float c(double var0) {
      return (float)Math.cos(var0);
   }

   static void set(float[] var0, int var1, double var2, double var4, double var6) {
      var0[var1] = (float)var2;
      var0[var1 + 1] = (float)var4;
      var0[var1 + 2] = (float)var6;
   }

   static void pitch(float[] var0, int var1, double var2) {
      var0[var1] = (float)var2;
   }

   static float kf(float var0, double... var1) {
      if (var0 <= var1[0]) {
         return (float)var1[1];
      } else {
         for (byte var2 = 2; var2 < var1.length; var2 += 2) {
            if (var0 <= var1[var2]) {
               double var3 = (var0 - var1[var2 - 2]) / Math.max(1.0E-4, var1[var2] - var1[var2 - 2]);
               var3 = var3 * var3 * (3.0 - 2.0 * var3);
               return (float)(var1[var2 - 1] + (var1[var2 + 1] - var1[var2 - 1]) * var3);
            }
         }

         return (float)var1[var1.length - 1];
      }
   }

   static float loop(float var0, float var1) {
      return var0 % var1;
   }

   static float hit(float var0, float var1, float var2, float var3) {
      if (var0 < var1) {
         return 0.0F;
      } else {
         return var0 < var1 + var2 ? (var0 - var1) / var2 : (float)Math.exp(-(var0 - var1 - var2) / var3);
      }
   }

   public static boolean isCutscene(String var0) {
      return var0.startsWith("cs_");
   }

   static float ease(float var0) {
      var0 = Math.max(0.0F, Math.min(1.0F, var0));
      return var0 * var0 * (3.0F - 2.0F * var0);
   }

   public static boolean inWheel(String var0) {
      return WHEEL.contains(var0);
   }

   public static boolean toggleWheel(String var0) {
      if (WHEEL.remove(var0)) {
         Modules.scheduleSave();
         return true;
      } else if (WHEEL.size() >= 8) {
         return false;
      } else {
         WHEEL.add(var0);
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
         public void load(Object var1) {
            if (var1 instanceof List) {
               EmoteLib.WHEEL.clear();

               for (Object var3 : (List)var1) {
                  if (var3 instanceof String && EmoteLib.get((String)var3) != null && EmoteLib.WHEEL.size() < 8) {
                     EmoteLib.WHEEL.add((String)var3);
                  }
               }
            }
         }
      });
   }

   static {
      add("wave", "Winken", "emote", 3.0F, false, null, 0.0F, (var0, var1) -> set(var1, 6, -2.75, 0.0, 0.25 + s(var0 * 9.0F) * 0.45));
      add("dance", "Tanzen", "music", 6.0F, true, null, 220.0F, (var0, var1) -> {
         float var2 = s(var0 * 8.0F);
         set(var1, 6, -1.6 - var2 * 1.2, 0.0, 0.3);
         set(var1, 9, -1.6 + var2 * 1.2, 0.0, -0.3);
         set(var1, 12, var2 * 0.4, 0.0, 0.05);
         set(var1, 15, -var2 * 0.4, 0.0, -0.05);
         set(var1, 0, var2 * 0.2, s(var0 * 4.0F) * 0.3, 0.0);
      });
      add("tpose", "T-Pose", "user", 3.0F, false, null, 0.0F, (var0, var1) -> {
         set(var1, 6, 0.0, 0.0, 1.5708);
         set(var1, 9, 0.0, 0.0, -1.5708);
         set(var1, 0, 0.0, 0.0, 0.0);
      });
      add("clap", "Klatschen", "sparkle", 3.0F, false, null, 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 7.0F)) * 0.45F;
         set(var1, 6, -1.35, 0.0, -0.05 - var2);
         set(var1, 9, -1.35, 0.0, 0.05 + var2);
      });
      add("dab", "Dab", "star", 2.2F, false, null, 0.0F, (var0, var1) -> {
         set(var1, 0, 0.55, -0.4, 0.0);
         set(var1, 6, -2.3, 0.0, -0.9);
         set(var1, 9, -1.25, 0.0, -0.6);
      });
      add("salute", "Salutieren", "shield", 2.5F, false, null, 0.0F, (var0, var1) -> set(var1, 6, -2.2, 0.0, -0.75));
      add("cheer", "Jubeln", "trophy", 3.0F, false, null, 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 6.0F)) * 0.3F;
         set(var1, 6, -2.9 + var2, 0.0, 0.35);
         set(var1, 9, -2.9 + var2, 0.0, -0.35);
         set(var1, 0, -0.25, 0.0, 0.0);
      });
      add("facepalm", "Facepalm", "user", 2.5F, false, null, 0.0F, (var0, var1) -> {
         set(var1, 0, 0.45, 0.0, 0.0);
         set(var1, 6, -2.05, 0.0, -0.45);
      });
      add("heart", "Herz", "heart", 2.8F, false, "heart", 0.0F, (var0, var1) -> {
         set(var1, 6, -2.4, 0.0, -0.35);
         set(var1, 9, -2.4, 0.0, 0.35);
      });
      add("gg", "GG", "gg", 2.6F, false, "gg", 0.0F, (var0, var1) -> set(var1, 6, -2.2, 0.0, 0.3 + s(var0 * 6.0F) * 0.2));
      add("laugh", "Lachen", "gg", 2.6F, false, "laugh", 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 14.0F) * 0.12F;
         set(var1, 0, -0.35 + var2, 0.0, 0.0);
         set(var1, 6, -0.4, 0.0, 0.25);
         set(var1, 9, -0.4, 0.0, -0.25);
      });
      add("fire", "Feuer", "flame", 2.6F, false, "fire", 0.0F, (var0, var1) -> {
         set(var1, 6, -1.5, 0.3, 0.0);
         set(var1, 9, -1.5, -0.3, 0.0);
      });
      add("floss", "Floss", "music", 6.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 9.0F);
         float var3 = s(var0 * 18.0F);
         set(var1, 6, 0.25 * var2, 0.0, 0.35 + var2 * 0.55);
         set(var1, 9, -0.25 * var2, 0.0, -0.35 + var2 * 0.55);
         set(var1, 3, 0.0, 0.0, var3 * 0.08);
         set(var1, 12, 0.0, 0.0, -var3 * 0.05);
         set(var1, 15, 0.0, 0.0, -var3 * 0.05);
      });
      add("zombie", "Zombie", "skull", 5.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 3.0F) * 0.12F;
         set(var1, 6, -1.57 + var2, 0.08, 0.0);
         set(var1, 9, -1.57 - var2, -0.08, 0.0);
         set(var1, 0, 0.25, s(var0 * 1.5) * 0.3, s(var0 * 1.3) * 0.2);
      });
      add("bow", "Verbeugen", "user", 2.6F, false, null, 0.0F, (var0, var1) -> {
         float var2 = (float)Math.sin(Math.min(1.0, var0 / 2.6) * Math.PI);
         set(var1, 3, 0.55 * var2, 0.0, 0.0);
         set(var1, 0, 0.6 * var2, 0.0, 0.0);
         set(var1, 6, -0.2 * var2, 0.0, 0.1);
         set(var1, 9, 0.6 * var2, 0.0, -0.1);
      });
      add("shrug", "Achselzucken", "info", 2.4F, false, null, 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 2.6));
         set(var1, 6, -0.5, 0.9, 1.1 + var2 * 0.15);
         set(var1, 9, -0.5, -0.9, -1.1 - var2 * 0.15);
         set(var1, 0, 0.1, 0.0, 0.25);
      });
      add("headbang", "Headbang", "music", 5.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 11.0F);
         set(var1, 0, 0.35 + var2 * 0.45, 0.0, 0.0);
         set(var1, 6, -2.8, 0.0, 0.3 + var2 * 0.1);
         set(var1, 9, -0.3, 0.0, -0.1);
      });
      add("guitar", "Luftgitarre", "music", 5.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 14.0F);
         set(var1, 6, -0.7 + var2 * 0.25, -0.4, -0.3);
         set(var1, 9, -1.2, 0.6, 0.2);
         set(var1, 0, 0.2 + Math.abs(var2) * 0.15, -0.3, 0.0);
      });
      add("robot", "Roboter", "chip", 6.0F, true, null, 0.0F, (var0, var1) -> {
         int var2 = (int)Math.floor(var0 * 2.5) % 4;
         double[][] var3 = new double[][]{{-1.57, 0.0, 0.0, 0.0}, {0.0, 0.0, -1.57, 0.0}, {-1.57, 0.8, -1.57, -0.8}, {0.0, 1.2, 0.0, -1.2}};
         double[] var4 = var3[var2];
         set(var1, 6, var4[0], 0.0, var4[1]);
         set(var1, 9, var4[2], 0.0, var4[3]);
         set(var1, 0, 0.0, var2 % 2 == 0 ? 0.5 : -0.5, 0.0);
      });
      add("propeller", "Propeller", "reset", 4.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = var0 * 12.0F;
         set(var1, 6, -1.57 + s(var2) * 1.4, 0.0, c(var2) * 0.6 + 0.6);
         set(var1, 9, -1.57 - s(var2) * 1.4, 0.0, -c(var2) * 0.6 - 0.6);
      });
      add("think", "Denken", "info", 3.0F, false, "think", 0.0F, (var0, var1) -> {
         set(var1, 6, -2.1, -0.3, -0.5);
         set(var1, 9, -0.8, 0.5, 0.2);
         set(var1, 0, -0.2, 0.25, 0.15);
      });
      add("cry", "Weinen", "rain", 3.0F, false, "cry", 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 12.0F) * 0.06F;
         set(var1, 0, 0.35 + var2, 0.0, 0.0);
         set(var1, 6, -2.0, 0.0, -0.35);
         set(var1, 9, -2.0, 0.0, 0.35);
      });
      add("meditate", "Meditieren", "sun", 8.0F, true, "zen", 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 1.6) * 0.06F;
         set(var1, 6, -0.9 + var2, -0.4, 0.5);
         set(var1, 9, -0.9 + var2, 0.4, -0.5);
         set(var1, 0, 0.1 + var2, 0.0, 0.0);
      });
      add("box", "Boxen", "target", 4.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 10.0F);
         float var3 = s(var0 * 10.0F + Math.PI);
         set(var1, 6, -1.3 - Math.max(0.0F, var2) * 0.3, 0.3 - Math.max(0.0F, var2) * 0.3, -0.2);
         set(var1, 9, -1.3 - Math.max(0.0F, var3) * 0.3, -0.3 + Math.max(0.0F, var3) * 0.3, 0.2);
         set(var1, 0, 0.15, s(var0 * 5.0F) * 0.15, 0.0);
         set(var1, 3, 0.0, s(var0 * 5.0F) * 0.12, 0.0);
      });
      add("point", "Zeigen", "arrow", 2.5F, false, null, 0.0F, (var0, var1) -> {
         set(var1, 6, -1.6, 0.1, 0.0);
         set(var1, 0, 0.0, 0.1, 0.0);
      });
      add("swim", "Schwimmen", "rain", 5.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = var0 * 5.0F;
         set(var1, 6, -1.57 + s(var2) * 1.6, 0.0, 0.3);
         set(var1, 9, -1.57 + s(var2 + Math.PI) * 1.6, 0.0, -0.3);
         set(var1, 12, s(var2 * 2.0F) * 0.35, 0.0, 0.0);
         set(var1, 15, -s(var2 * 2.0F) * 0.35, 0.0, 0.0);
      });
      add("chicken", "Hühnertanz", "music", 5.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 8.0F));
         set(var1, 6, 0.0, -0.3, 0.2 + var2 * 0.9);
         set(var1, 9, 0.0, 0.3, -0.2 - var2 * 0.9);
         set(var1, 0, -0.1 + s(var0 * 16.0F) * 0.12, 0.0, 0.0);
         set(var1, 12, -var2 * 0.5, 0.0, 0.0);
      });
      add("sleep", "Schlafen", "moon", 6.0F, true, "sleep", 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 1.4) * 0.05F;
         set(var1, 0, 0.55 + var2, 0.0, 0.35);
         set(var1, 6, 0.1, 0.0, 0.05);
         set(var1, 9, 0.1, 0.0, -0.05);
      });
      add("hype", "Hype", "bolt", 4.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 9.0F));
         set(var1, 6, -3.0 + var2 * 0.6, 0.0, 0.5);
         set(var1, 9, -3.0 + var2 * 0.6, 0.0, -0.5);
         set(var1, 0, -0.3, s(var0 * 9.0F) * 0.2, 0.0);
         set(var1, 12, -var2 * 0.3, 0.0, 0.1);
         set(var1, 15, var2 * 0.3, 0.0, -0.1);
      });
      add(
         "karate",
         "Karate-Kombo",
         "hit",
         4.6F,
         false,
         null,
         0.0F,
         (var0, var1) -> {
            set(
               var1,
               6,
               kf(
                  var0,
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
               kf(var0, 0.45, 0.0, 0.55, -0.14, 0.85, -0.14, 0.95, 0.0, 1.25, 0.0, 1.35, -0.14, 1.6, -0.14, 1.8, 0.0),
               kf(var0, 0.0, 0.05, 0.35, 0.12, 1.6, 0.12, 1.8, -0.1, 2.55, -0.1, 2.75, 0.25, 2.9, -0.12, 3.3, -0.12, 3.6, 0.05)
            );
            set(
               var1,
               9,
               kf(
                  var0,
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
               kf(var0, 0.85, 0.0, 0.95, 0.14, 1.25, 0.14, 1.35, 0.0),
               kf(var0, 0.0, -0.05, 0.35, -0.1, 1.8, 0.1, 1.95, -0.7, 2.4, -0.7, 2.6, -0.05, 3.6, -0.05)
            );
            set(
               var1,
               12,
               kf(var0, 0.0, 0.0, 0.35, 0.32, 1.85, 0.32, 2.05, -1.65, 2.3, -1.6, 2.55, 0.32, 3.3, 0.32, 3.6, 0.0),
               0.0,
               kf(var0, 0.0, 0.0, 0.35, 0.06, 3.6, 0.02)
            );
            set(
               var1,
               15,
               kf(var0, 0.0, 0.0, 0.35, -0.28, 1.85, -0.28, 2.05, 0.12, 2.55, -0.28, 3.3, -0.28, 3.6, 0.0),
               0.0,
               kf(var0, 0.0, 0.0, 0.35, -0.06, 3.6, -0.02)
            );
            set(
               var1,
               3,
               kf(var0, 0.0, 0.0, 0.35, 0.06, 1.85, 0.06, 2.05, -0.14, 2.4, -0.14, 2.6, 0.06, 2.9, 0.1, 3.3, 0.06, 3.6, 0.0, 3.8, 0.45, 4.25, 0.45, 4.6, 0.0),
               0.0,
               0.0
            );
            set(
               var1,
               0,
               kf(var0, 0.0, 0.0, 0.35, 0.12, 2.75, 0.12, 2.9, -0.18, 3.3, 0.0, 3.8, 0.15, 4.6, 0.0),
               kf(var0, 0.55, 0.0, 0.6, 0.05, 1.0, -0.05, 1.4, 0.05, 1.8, 0.0),
               0.0
            );
         }
      );
      add("yoga_tree", "Yoga-Baum", "leaf", 8.0F, true, "zen", 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 1.2F);
         float var3 = ease((var0 - 1.2F) / 1.4F);
         float var4 = s(var0 * 1.3) * 0.035F * var3;
         float var5 = s(var0 * 1.7) * 0.03F;
         set(var1, 6, -1.3 * (1.0F - var3) - 3.0 * var3 + var5, -0.42 * (1.0F - var3), -0.3 * (1.0F - var3) + 0.2 * var3 + var4);
         set(var1, 9, -1.3 * (1.0F - var3) - 3.0 * var3 + var5, 0.42 * (1.0F - var3), 0.3 * (1.0F - var3) - 0.2 * var3 + var4);
         set(var1, 12, -0.35 * var2, 0.35 * var2, 0.95 * var2 + var4 * 0.5);
         set(var1, 15, 0.0, 0.0, -0.03 + var4 * 0.4);
         set(var1, 3, 0.0, 0.0, var4 * 0.4);
         set(var1, 0, -0.12 * var3 + var5, 0.0, var4 * 0.8);
      });
      add("bunny", "Hasensprung", "jump", 4.8F, true, null, 0.0F, (var0, var1) -> {
         float var2 = loop(var0, 0.6F) / 0.6F;
         float var3 = s(var2 * Math.PI);
         float var4 = (float)Math.max(0.0, Math.cos(var2 * Math.PI * 2.0)) * 0.5F;
         int var5 = (int)(var0 / 0.6F);
         float var6 = (var5 % 2 == 0 ? 1 : -1) * 0.14F;
         set(var1, 6, -1.35 - 0.22 * var3, -0.28, -0.1);
         set(var1, 9, -1.35 - 0.22 * var3, 0.28, 0.1);
         set(var1, 12, 0.55 * var3 - 0.12 * var4, 0.0, 0.02);
         set(var1, 15, 0.55 * var3 - 0.12 * var4, 0.0, -0.02);
         set(var1, 3, 0.12 * var4 - 0.04 * var3, 0.0, 0.0);
         set(var1, 0, -0.18 * var3 + 0.1 * var4, 0.0, var6 * var3);
      });
      add("zombie_walk", "Zombie-Gang", "skull", 6.4F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * Math.PI * 2.0 / 1.6);
         float var3 = s(var0 * Math.PI * 2.0 / 1.6 + 0.6);
         float var4 = loop(var0, 3.2F) < 0.18F ? s(var0 * 70.0F) * 0.25F : 0.0F;
         set(var1, 6, -1.45 + s(var0 * 2.1) * 0.1, 0.06, -0.05 + var3 * 0.06);
         set(var1, 9, -1.72 + s(var0 * 2.1 + 1.5) * 0.1, -0.06, 0.05 + var3 * 0.06);
         set(var1, 12, 0.38 * var2, 0.0, 0.04);
         set(var1, 15, -0.38 * var2, 0.0, -0.04);
         set(var1, 3, 0.1, 0.0, var3 * 0.05);
         set(var1, 0, 0.28 + s(var0 * 1.7) * 0.1, var4, s(var0 * 1.9) * 0.38);
      });
      add("ballet", "Ballett-Drehung", "sparkle", 8.0F, true, null, 240.0F, (var0, var1) -> {
         float var2 = loop(var0, 4.0F);
         float var3 = kf(var2, 0.0, 0.0, 1.6, 0.0, 2.1, 1.0, 3.5, 1.0, 4.0, 0.0);
         float var4 = s(var0 * 3.0F) * 0.04F;
         set(var1, 6, -2.85 * (1.0F - var3) - 1.75 * var3 + var4, 0.0, 0.36 * (1.0F - var3) + 0.05 * var3);
         set(var1, 9, -2.85 * (1.0F - var3) - 0.25 * var3 + var4, 0.0, -0.36 * (1.0F - var3) - 1.35 * var3);
         set(var1, 12, -0.55 * (1.0F - var3), 0.25 * (1.0F - var3), 0.7 * (1.0F - var3) + 0.02 * var3);
         set(var1, 15, 0.95 * var3, 0.0, -0.03);
         set(var1, 3, 0.18 * var3, 0.0, 0.0);
         set(var1, 0, -0.22 * (1.0F - var3) - 0.3 * var3, 0.0, 0.12 * (1.0F - var3));
      });
      add("star_jump", "Jubel-Sprung", "trophy", 4.8F, true, null, 0.0F, (var0, var1) -> {
         float var2 = loop(var0, 1.2F);
         float var3 = kf(var2, 0.0, 0.0, 0.3, 1.0, 0.42, 0.0, 1.0, 0.0, 1.2, 0.0);
         float var4 = kf(var2, 0.3, 0.0, 0.48, 1.0, 0.72, 1.0, 0.95, 0.0);
         float var5 = kf(var2, 0.9, 0.0, 0.98, 1.0, 1.2, 0.0);
         set(var1, 6, 0.55 * var3 - 2.9 * var4, 0.0, 0.1 * (1.0F - var4) - 0.5 * var4);
         set(var1, 9, 0.55 * var3 - 2.9 * var4, 0.0, -0.1 * (1.0F - var4) + 0.5 * var4);
         set(var1, 12, -0.1 * var4, 0.0, 0.45 * var4);
         set(var1, 15, -0.1 * var4, 0.0, -0.45 * var4);
         set(var1, 3, 0.28 * var3 + 0.12 * var5 - 0.08 * var4, 0.0, 0.0);
         set(var1, 0, -0.15 * var3 - 0.35 * var4, 0.0, 0.0);
      });
      add("deep_bow", "Tiefe Verbeugung", "crown", 3.8F, false, null, 0.0F, (var0, var1) -> {
         float var2 = kf(var0, 0.0, 0.0, 0.55, 1.0, 0.9, 0.0);
         float var3 = kf(var0, 0.6, 0.0, 1.3, 1.0, 2.8, 1.0, 3.5, 0.0);
         set(var1, 6, -0.7 * var2 - 1.1 * var3, -0.65 * var3, 1.35 * var2 - 0.1 * var3);
         set(var1, 9, 0.55 * var3, 0.0, -0.1 - 0.9 * var3);
         set(var1, 12, 0.1 * var3, 0.0, 0.03);
         set(var1, 15, -0.1 * var3, 0.0, -0.03);
         set(var1, 3, 0.75 * var3, 0.0, 0.0);
         set(var1, 0, -0.15 * var2 + 0.1 * var3, 0.0, 0.0);
      });
      add(
         "snowball",
         "Schneeball werfen",
         "snow",
         3.4F,
         false,
         null,
         0.0F,
         (var0, var1) -> {
            float var2 = var0 > 0.6F && var0 < 1.25F ? s(var0 * 22.0F) * 0.12F : 0.0F;
            set(
               var1,
               6,
               kf(var0, 0.0, 0.0, 0.35, -0.75, 0.6, -0.75, 0.85, -1.25, 1.25, -1.25, 1.6, -3.25, 1.72, -3.25, 1.86, -1.2, 2.1, -0.4, 2.6, -0.4, 3.2, 0.0),
               kf(var0, 0.6, 0.0, 0.85, -0.3, 1.25, -0.3, 1.6, 0.0),
               kf(var0, 0.35, 0.05, 0.85, -0.2, 1.25, -0.2, 1.6, -0.25, 1.86, -0.1, 2.1, -0.5, 2.6, -0.5, 3.2, 0.05) + var2
            );
            set(
               var1,
               9,
               kf(var0, 0.0, 0.0, 0.35, -0.75, 0.6, -0.75, 0.85, -1.25, 1.25, -1.25, 1.6, -1.65, 1.8, -1.6, 1.95, 0.3, 2.6, 0.3, 3.2, 0.0),
               kf(var0, 0.6, 0.0, 0.85, 0.3, 1.25, 0.3, 1.6, 0.0),
               kf(var0, 0.35, -0.05, 0.85, 0.2, 1.25, 0.2, 1.6, -0.1, 1.95, -0.2, 3.2, -0.05) - var2
            );
            set(var1, 3, kf(var0, 0.0, 0.0, 0.35, 0.55, 0.6, 0.55, 0.85, 0.0, 1.6, -0.14, 1.8, 0.22, 2.3, 0.12, 3.2, 0.0), 0.0, 0.0);
            set(var1, 12, kf(var0, 0.6, 0.0, 1.2, 0.35, 1.8, 0.35, 2.0, -0.05, 3.2, 0.0), 0.0, 0.04);
            set(var1, 15, kf(var0, 0.6, 0.0, 1.2, -0.3, 1.8, -0.3, 2.0, 0.15, 3.2, 0.0), 0.0, -0.04);
            set(
               var1,
               0,
               kf(var0, 0.0, 0.0, 0.35, 0.0, 0.6, 0.2, 0.85, 0.45, 1.25, 0.45, 1.5, -0.05, 2.2, -0.05, 2.9, -0.1, 3.4, 0.0),
               0.0,
               kf(var0, 2.4, 0.0, 2.7, 0.2, 3.4, 0.0)
            );
         }
      );
      add("xmas_dance", "Weihnachts-Tanz", "gift", 8.0F, true, "heart", 0.0F, (var0, var1) -> {
         float var2 = loop(var0, 4.0F);
         float var3 = s(var0 * 34.0F) * 0.12F;
         float var4 = s(var0 * Math.PI * 2.0);
         float var5 = kf(var2, 0.0, 0.0, 0.2, 1.0, 0.9, 1.0, 1.1, 0.0, 1.9, 0.0, 2.1, 1.0, 2.9, 1.0, 3.1, 0.0);
         float var6 = kf(var2, 0.9, 0.0, 1.1, 1.0, 2.9, 1.0, 3.1, 0.0);
         float var7 = kf(var2, 3.0, 0.0, 3.15, 1.0, 3.85, 1.0, 4.0, 0.0);
         float var8 = Math.abs(s(var0 * 12.0F)) * 0.3F * var7;
         set(var1, 6, -2.65 * var5 + var3 * var5 - 1.35 * var7, -0.3 * var7 + var8, 0.2 - 0.5 * var5 + 0.2 * var5 * var6 - 0.2 * var7);
         set(var1, 9, -2.65 * var6 - var3 * var6 - 1.35 * var7, 0.3 * var7 - var8, -0.2 + 0.5 * var6 - 0.2 * var5 * var6 + 0.2 * var7);
         set(var1, 12, 0.0, 0.0, 0.05 + Math.max(0.0F, var4) * 0.28);
         set(var1, 15, 0.0, 0.0, -0.05 - Math.max(0.0F, -var4) * 0.28);
         set(var1, 3, 0.0, 0.0, var4 * 0.05);
         set(var1, 0, -0.1, s(var0 * Math.PI) * 0.18, var4 * 0.16);
      });
      add(
         "unwrap",
         "Geschenk auspacken",
         "gift",
         4.8F,
         false,
         "heart",
         0.0F,
         (var0, var1) -> {
            float var2 = kf(var0, 0.0, 0.0, 0.35, 1.0, 2.3, 1.0, 2.5, 0.0);
            float var3 = var0 > 0.5F && var0 < 1.4F ? s(var0 * 26.0F) * 0.1F : 0.0F;
            float var4 = kf(var0, 1.55, 0.0, 1.68, 1.0, 1.85, 0.0, 1.98, 1.0, 2.2, 0.2);
            float var5 = kf(var0, 2.3, 0.0, 2.5, 1.0, 2.9, 1.0, 3.1, 0.0);
            float var6 = kf(var0, 2.95, 0.0, 3.3, 1.0);
            float var7 = Math.abs(s(var0 * 7.0F)) * 0.18F * var6;
            set(
               var1,
               6,
               -1.1 * var2 + var3 - 0.2 * var4 - 2.3 * var5 - 2.95 * var6 + var7,
               -0.22 * var2 + 0.75 * var4,
               -0.2 * var2 + 0.45 * var4 + 0.45 * var5 + 0.12 * var6
            );
            set(
               var1,
               9,
               -1.1 * var2 + var3 - 0.2 * var4 - 2.3 * var5 - 2.95 * var6 + var7,
               0.22 * var2 - 0.75 * var4,
               0.2 * var2 - 0.45 * var4 - 0.45 * var5 - 0.12 * var6
            );
            set(var1, 3, 0.06 * var2 - 0.06 * var5 - 0.08 * var6, 0.0, 0.0);
            set(var1, 0, 0.42 * var2 - 0.3 * var5 - 0.35 * var6, 0.0, kf(var0, 0.4, 0.0, 0.7, 0.22, 1.4, 0.22, 1.6, 0.0));
            set(var1, 12, -0.2 * var6 * Math.abs(s(var0 * 7.0F)), 0.0, 0.04);
         }
      );
      add("shiver", "Zittern vor Kälte", "snow", 6.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = (float)Math.exp(-Math.pow((loop(var0, 2.6F) - 1.3F) / 0.18F, 2.0));
         float var3 = s(var0 * 58.0F) * (0.025F + 0.07F * var2);
         float var4 = s(var0 * 47.0F + 1.0F) * (0.02F + 0.05F * var2);
         set(var1, 6, -1.08 + var4, -0.55, -0.35 + var3);
         set(var1, 9, -0.92 - var4, 0.55, 0.35 - var3);
         set(var1, 12, 0.0, 0.0, -0.06 + var3 * 0.4);
         set(var1, 15, 0.0, 0.0, 0.06 - var3 * 0.4);
         set(var1, 3, 0.12 + var2 * 0.05, 0.0, var3 * 0.3);
         set(var1, 0, 0.28 + var4 * 0.6, var3 * 0.5, var3);
      });
      add("chill", "Chillen", "sun", 8.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 0.8F);
         float var3 = s(var0 * 3.4) * 0.07F;
         set(var1, 6, -3.2 * var2, 0.0, 0.62 * var2 + 0.05 * (1.0F - var2));
         set(var1, 9, -3.2 * var2, 0.0, -0.62 * var2 - 0.05 * (1.0F - var2));
         set(var1, 3, -0.1 * var2, 0.0, 0.0);
         set(var1, 0, (-0.14 + var3) * var2, s(var0 * 0.55) * 0.18, 0.08 * var2);
         set(var1, 12, -0.05 * var2, 0.0, -0.08 * var2);
         set(var1, 15, 0.05 * var2, 0.0, 0.12 * var2 + Math.max(0.0F, s(var0 * 3.4)) * 0.05);
      });
      add("fingerguns", "Fingerpistolen", "target", 3.6F, false, "fire", 0.0F, (var0, var1) -> {
         float var2 = kf(var0, 0.0, 0.0, 0.35, 1.0, 2.35, 1.0, 2.6, 0.0);
         float var3 = kf(var0, 2.4, 0.0, 2.7, 1.0, 3.3, 1.0, 3.6, 0.0);
         float var4 = hit(var0, 0.7F, 0.05F, 0.12F) + hit(var0, 1.5F, 0.05F, 0.12F);
         float var5 = hit(var0, 1.1F, 0.05F, 0.12F);
         float var6 = kf(var0, 1.8, 0.0, 2.0, 1.0, 2.3, 0.0);
         set(var1, 6, -1.52 * var2 - 0.4 * var4 - 2.1 * var3, (-0.1 + 0.08 * var6) * var2 - 0.35 * var3, 0.35 * var3);
         set(var1, 9, -1.52 * var2 - 0.4 * var5, 0.1 * var2, 0.0);
         set(var1, 3, -0.07 * var2, 0.0, 0.0);
         set(var1, 0, -0.05 * var2 + 0.12 * var3, -0.12 * var2, 0.2 * var2 - 0.1 * var3);
         set(var1, 12, 0.0, 0.0, 0.1);
         set(var1, 15, -0.12 * var2, 0.0, -0.06);
      });
      add("thinker", "Denker-Pose", "info", 8.0F, true, "think", 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 0.9F);
         float var3 = s(var0 * 1.1) * 0.05F;
         set(var1, 6, -2.05 * var2, -0.35 * var2, 0.2 * var2);
         set(var1, 9, -0.85 * var2, 0.45 * var2, 0.2 * var2);
         set(var1, 3, 0.32 * var2, 0.0, 0.0);
         set(var1, 0, (-0.12 + var3) * var2, 0.1 * var2, 0.12 * var2);
         set(var1, 12, -0.45 * var2, 0.0, 0.05);
         set(var1, 15, 0.08 * var2, 0.0, -0.05);
      });
      add("drums", "Schlagzeug", "music", 6.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = loop(var0, 2.0F);
         float var3 = kf(var2, 1.55, 0.0, 1.65, 1.0, 1.85, 1.0, 2.0, 0.0);
         float var4 = Math.max(0.0F, s(var0 * 15.0F));
         float var5 = Math.max(0.0F, s(var0 * 15.0F + Math.PI));
         set(var1, 6, -0.95 - 0.32 * var4 * (1.0F - var3) - 1.1 * var3, 0.0, -0.18 * (1.0F - var3) - 0.5 * var3);
         set(var1, 9, -0.95 - 0.32 * var5, 0.0, 0.18);
         set(var1, 12, -0.28 * Math.max(0.0F, s(var0 * 7.5)), 0.0, 0.05);
         set(var1, 3, 0.08, 0.0, 0.0);
         set(var1, 0, 0.15 + Math.abs(s(var0 * 7.5)) * 0.2 - 0.25 * var3, 0.0, 0.0);
      });
      add(
         "macarena",
         "Macarena",
         "music",
         9.6F,
         true,
         null,
         0.0F,
         (var0, var1) -> {
            double[][] var2 = new double[][]{
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
            double[][] var3 = new double[][]{
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
            double[] var4 = new double[]{0.0, 0.0, 0.05};
            double[] var5 = new double[]{0.0, 0.0, -0.05};
            float var6 = 0.4F;
            float var7 = loop(var0, var6 * 12.0F);
            int var8 = (int)(var7 / var6) % 12;
            int var9 = (var8 + 11) % 12;
            float var10 = ease((var7 - var8 * var6) / (var6 * 0.45F));
            double[] var11 = var0 < var6 ? var4 : var2[var9];
            double[] var12 = var0 < var6 ? var5 : var3[var9];
            double var13 = var8 != 2 && var8 != 3 ? 0.0 : s(var10 * Math.PI) * 0.15;
            set(
               var1,
               6,
               var11[0] + (var2[var8][0] - var11[0]) * var10 - var13,
               var11[1] + (var2[var8][1] - var11[1]) * var10,
               var11[2] + (var2[var8][2] - var11[2]) * var10
            );
            set(
               var1,
               9,
               var12[0] + (var3[var8][0] - var12[0]) * var10 - var13,
               var12[1] + (var3[var8][1] - var12[1]) * var10,
               var12[2] + (var3[var8][2] - var12[2]) * var10
            );
            float var15 = s(var0 * Math.PI / var6);
            set(var1, 3, 0.0, 0.0, var15 * 0.05);
            set(var1, 12, 0.0, 0.0, 0.05 + Math.max(0.0F, var15) * 0.12);
            set(var1, 15, 0.0, 0.0, -0.05 - Math.max(0.0F, -var15) * 0.12);
            set(var1, 0, 0.0, 0.0, var15 * 0.12);
         }
      );
      add("penguin", "Pinguin-Watscheln", "snow", 6.0F, true, null, 0.0F, (var0, var1) -> {
         float var2 = s(var0 * 6.5);
         float var3 = Math.max(0.0F, s(var0 * 13.0F)) * 0.22F;
         set(var1, 6, 0.08, 0.0, 0.28 + var3);
         set(var1, 9, 0.08, 0.0, -0.28 - var3);
         set(var1, 12, 0.0, 0.0, 0.04 + Math.max(0.0F, var2) * 0.2);
         set(var1, 15, 0.0, 0.0, -0.04 - Math.max(0.0F, -var2) * 0.2);
         set(var1, 3, 0.0, 0.0, var2 * 0.09);
         set(var1, 0, -0.12 + Math.abs(var2) * 0.06, s(var0 * 1.2) * 0.25, -var2 * 0.14);
      });
      add("superhero", "Superheld", "bolt", 4.4F, false, null, 0.0F, (var0, var1) -> {
         float var2 = kf(var0, 0.0, 0.0, 0.4, 1.0);
         float var3 = kf(var0, 2.0, 0.0, 2.35, 1.0, 3.9, 1.0, 4.4, 0.0);
         float var4 = s(var0 * 5.0F) * 0.03F;
         set(var1, 6, 0.18 * var2 * (1.0F - var3) - 3.05 * var3, -0.35 * var2 * (1.0F - var3), 0.55 * var2 * (1.0F - var3) - 0.08 * var3 + var4);
         set(var1, 9, 0.18 * var2, 0.35 * var2, -0.55 * var2);
         set(var1, 3, -0.07 * var2, 0.0, 0.0);
         set(var1, 0, -0.2 * var2 - 0.25 * var3, -0.25 * var2 * (1.0F - var3), 0.0);
         set(var1, 12, -0.3 * var3, 0.0, 0.14 * var2 * (1.0F - var3));
         set(var1, 15, 0.0, 0.0, -0.14 * var2);
      });
      add("cs_landing", "Heldenlandung", "bolt", 5.5F, false, null, 0.0F, (var0, var1) -> {
         float var2 = ease((var0 - 0.9F) / 0.25F);
         float var3 = ease((var0 - 3.6F) / 0.8F);
         float var4 = var2 * (1.0F - var3);
         set(var1, 3, 0.55 * var4, 0.0, 0.0);
         set(var1, 0, -0.35 * var4 - 0.2 * var3, 0.0, 0.0);
         set(var1, 6, -0.3 * var4 + -2.9 * var3, 0.0, 0.15);
         set(var1, 9, -1.1 * var4 + 0.1, -0.3 * var4, -0.4 * var4 - 0.1);
         set(var1, 12, -1.35 * var4, 0.25 * var4, 0.0);
         set(var1, 15, 0.9 * var4, -0.1 * var4, 0.0);
      });
      add("cs_victory", "Siegesfeier", "trophy", 6.5F, false, "gg", 0.0F, (var0, var1) -> {
         float var2 = Math.abs(s(var0 * 6.0F)) * 0.25F;
         set(var1, 6, -2.95 + var2, 0.0, 0.35);
         set(var1, 9, -2.95 + var2, 0.0, -0.35);
         set(var1, 0, -0.3, s(var0 * 2.0F) * 0.25, 0.0);
         set(var1, 12, -Math.abs(s(var0 * 6.0F)) * 0.25, 0.0, 0.05);
      });
      add("cs_portal", "Portal-Beschwörung", "sparkle", 6.5F, false, null, 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 1.2F);
         float var3 = s(var0 * 3.0F) * 0.08F;
         set(var1, 6, -1.6 - 1.2 * var2 + var3, 0.2, 0.5 * var2);
         set(var1, 9, -1.6 - 1.2 * var2 - var3, -0.2, -0.5 * var2);
         set(var1, 0, -0.45 * var2, 0.0, 0.0);
         set(var1, 3, -0.08 * var2, 0.0, 0.0);
      });
      add("cs_power", "Energie-Aufladung", "flame", 6.5F, false, "fire", 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 3.5F);
         float var3 = ease((var0 - 3.8F) / 0.3F);
         float var4 = s(var0 * 60.0F) * 0.03F * var2 * (1.0F - var3);
         set(var1, 6, -0.4 * (1.0F - var3) - 2.9 * var3, 0.3 * (1.0F - var3), 0.5 + var4 + 0.3 * var3);
         set(var1, 9, -0.4 * (1.0F - var3) - 2.9 * var3, -0.3 * (1.0F - var3), -0.5 - var4 - 0.3 * var3);
         set(var1, 3, 0.25 * var2 * (1.0F - var3) - 0.1 * var3, 0.0, var4);
         set(var1, 0, 0.3 * var2 * (1.0F - var3) - 0.45 * var3, 0.0, 0.0);
         set(var1, 12, -0.25 * var2, 0.0, 0.2 * var2);
         set(var1, 15, 0.1 * var2, 0.0, -0.2 * var2);
      });
      add("cs_selfie", "Selfie", "camera", 4.5F, false, "heart", 0.0F, (var0, var1) -> {
         float var2 = ease(var0 / 0.6F);
         set(var1, 6, -1.9 * var2, -0.4 * var2, -0.1);
         set(var1, 9, -2.3 * var2, 0.1, 0.65 * var2);
         set(var1, 0, -0.15 * var2, -0.25 * var2, 0.22 * var2);
      });
      add("cs_xmas", "Weihnachtszauber", "gift", 8.0F, false, null, 0.0F, (var0, var1) -> {
         float var2 = ease((var0 - 0.6F) / 0.8F);
         float var3 = ease((var0 - 3.0F) / 0.4F);
         float var4 = Math.max(0.0F, s((var0 - 3.2F) * 9.0F)) * (var0 > 3.2F && var0 < 3.55F ? 1 : 0);
         set(var1, 0, -0.45 * var2 * (1.0 - var3 * 0.5), 0.0, 0.0);
         double var5 = s(var0 * 8.0F) * 0.25 * var3;
         set(var1, 6, -0.2 - 2.7 * var3 + var5, 0.0, 0.35 * var3);
         set(var1, 9, -0.2 - 2.7 * var3 - var5, 0.0, -0.35 * var3);
         set(var1, 3, -0.1 * var3, 0.0, 0.0);
         set(var1, 12, -0.3 * var4, 0.0, 0.05);
         set(var1, 15, -0.3 * var4, 0.0, -0.05);
      });
      add("cs_darkaura", "Dunkle Aura", "moon", 7.6F, false, null, 0.0F, (var0, var1) -> {
         float var2 = ease((var0 - 2.7F) / 1.1F);
         float var3 = ease((var0 - 5.2F) / 0.18F);
         float var4 = ease((var0 - 5.6F) / 0.8F);
         float var5 = s(var0 * 2.2) * 0.03F;
         set(var1, 0, 0.4 * (1.0F - var2) + var5 * 0.5 - 0.12 * var4, 0.0, 0.0);
         set(var1, 3, 0.12 * (1.0F - var2) + 0.05 * var3 * (1.0F - var4), 0.0, 0.0);
         double var6 = var0 < 5.2F ? -0.25 - 0.3 * var2 : -2.4 + 2.9 * var3 - 0.2 * var4;
         if (var0 >= 5.0F && var0 < 5.2F) {
            var6 = -0.55 - 1.85 * ease((var0 - 5.0F) / 0.2F);
         }

         set(var1, 6, var6, -0.35 * var3 * (1.0F - var4), 0.12 + 0.5 * var3 * (1.0F - var4));
         set(var1, 9, -0.2 + var5, 0.1, -0.12);
         set(var1, 12, -0.08 * var3, 0.0, 0.06);
         set(var1, 15, 0.12 * var3, 0.0, -0.06);
      });
      add(
         "cs_evillaugh",
         "Böses Lachen",
         "eye",
         10.0F,
         false,
         null,
         0.0F,
         (var0, var1) -> {
            float var2 = kf(
               var0, 0.0, 0.015, 1.25, 0.02, 1.4, 0.05, 2.5, 0.04, 2.62, 0.085, 3.9, 0.07, 4.3, 0.0, 5.95, 0.0, 6.05, 0.1, 9.1, 0.1, 9.6, 0.02, 10.0, 0.0
            );
            float var3 = s(var0 * 24.0F) * var2;
            float var4 = s(var0 * 24.0F + 1.3) * var2;
            set(
               var1,
               0,
               kf(var0, 0.0, 0.2, 0.8, 0.38, 2.58, 0.38, 2.62, -0.45, 3.9, -0.5, 4.36, 0.0, 5.95, 0.0, 6.05, -0.35, 6.4, -0.72, 9.1, -0.72, 9.8, -0.1) + var3,
               0.0,
               kf(var0, 0.0, 0.0, 0.8, 0.12, 2.58, 0.12, 2.62, -0.08, 3.9, -0.08, 4.36, 0.0, 5.95, 0.0, 6.3, 0.1, 9.1, 0.1, 9.8, 0.0) + var4 * 0.5
            );
            set(
               var1,
               3,
               kf(var0, 0.0, 0.05, 0.8, 0.15, 2.58, 0.15, 2.62, -0.08, 3.9, -0.1, 4.36, 0.0, 5.95, 0.0, 6.1, -0.18, 9.1, -0.18, 9.8, 0.0) + var4 * 0.5,
               0.0,
               0.0
            );
            set(
               var1,
               9,
               kf(var0, 0.0, 0.0, 0.8, -2.35, 2.58, -2.35, 2.62, -2.75, 3.9, -2.75, 4.3, -0.15, 5.95, -0.15, 6.15, -0.5, 9.1, -0.5, 9.8, -0.1) + var3,
               kf(var0, 0.0, 0.0, 0.8, 0.35, 3.9, 0.35, 4.3, 0.0),
               kf(var0, 0.0, -0.05, 0.8, -0.3, 3.9, -0.3, 4.3, -0.08, 5.95, -0.08, 6.15, -1.95, 9.1, -1.95, 9.8, -0.3) - var3
            );
            set(
               var1,
               6,
               kf(var0, 0.0, 0.0, 0.8, -0.8, 3.9, -0.8, 4.3, -0.1, 5.95, -0.1, 6.15, -0.5, 9.1, -0.5, 9.8, -0.1) + var4,
               kf(var0, 0.0, 0.0, 0.8, -0.55, 3.9, -0.55, 4.3, 0.0),
               kf(var0, 0.0, 0.05, 0.8, 0.0, 3.9, 0.0, 4.3, 0.08, 5.95, 0.08, 6.15, 1.95, 9.1, 1.95, 9.8, 0.3) + var3
            );
            set(var1, 12, kf(var0, 5.95, 0.0, 6.15, -0.12, 9.8, 0.0), 0.0, kf(var0, 0.0, 0.03, 5.95, 0.03, 6.15, 0.16, 9.8, 0.04));
            set(var1, 15, kf(var0, 5.95, 0.0, 6.15, 0.1, 9.8, 0.0), 0.0, kf(var0, 0.0, -0.03, 5.95, -0.03, 6.15, -0.16, 9.8, -0.04));
         }
      );
      add("cs_starfall", "Sternenregen", "star", 7.0F, false, null, 0.0F, (var0, var1) -> {
         float var2 = ease((var0 - 0.5F) / 1.5F);
         set(var1, 6, -0.2 - 2.3 * var2, 0.0, 0.9 * var2);
         set(var1, 9, -0.2 - 2.3 * var2, 0.0, -0.9 * var2);
         set(var1, 0, -0.75 * var2, s(var0 * 0.8) * 0.2 * var2, 0.0);
         set(var1, 3, -0.12 * var2, 0.0, 0.0);
      });
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

      Emote(String var1, String var2, String var3, float var4, boolean var5, String var6, float var7, EmoteLib.PoseFn var8) {
         this.id = var1;
         this.name = var2;
         this.icon = var3;
         this.duration = var4;
         this.loop = var5;
         this.bubble = var6;
         this.spin = var7;
         this.fn = var8;
      }

      public float[] pose(float var1) {
         float[] var2 = new float[18];
         Arrays.fill(var2, Float.NaN);
         this.fn.pose(var1, var2);
         return var2;
      }
   }

   public interface PoseFn {
      void pose(float var1, float[] var2);
   }
}
