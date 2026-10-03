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
         float var0 = time();
         return Ease.outCubic(Math.min(1.0F, var0 / 0.5F)) * Math.min(1.0F, Math.max(0.0F, (dur - var0) / 0.5F));
      }
   }

   public static String title() {
      EmoteLib.Emote var0 = id == null ? null : EmoteLib.get(id);
      return var0 == null ? "" : var0.name;
   }

   static void begin(EmoteLib.Emote var0) {
      LocalPlayer var1 = Mc.player();
      if (var1 != null) {
         end();
         id = var0.id;
         dur = var0.duration;
         start = System.currentTimeMillis();
         ox = Mc.x(var1);
         oy = Mc.y(var1);
         oz = Mc.z(var1);
         baseYaw = Mc.bodyYaw(var1);
         savedPerspective = Mc.perspective();
         Mc.perspective(CameraType.THIRD_PERSON_BACK);
         cue = 0;
         sounds();
      }
   }

   private static Cutscenes.Cue c(double var0, String var2, double var3, double var5) {
      return new Cutscenes.Cue((float)var0, "minecraft:" + var2, (float)var3, (float)var5);
   }

   public static void sounds() {
      if (id != null) {
         Cutscenes.Cue[] var0 = CUES.get(id);
         if (var0 != null) {
            float var1 = time();

            while (cue < var0.length && var0[cue].t <= var1) {
               Cutscenes.Cue var2 = var0[cue++];
               if (var1 - var2.t < 0.6F) {
                  Sound.play(var2.id, var2.pitch, var2.vol);
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

   public static boolean camera(float var0, double[] var1) {
      if (id == null) {
         return false;
      } else {
         LocalPlayer var2 = Mc.player();
         if (var2 == null) {
            return false;
         } else {
            double[] var3 = Mc.lerpPos(var2, var0);
            sounds();
            if ("cs_darkaura".equals(id)) {
               return darkAuraCam(var3, time(), var1);
            } else if ("cs_evillaugh".equals(id)) {
               return evilCam(var3, time(), var1);
            } else {
               double var4 = var3[0];
               double var6 = var3[1] + 1.1;
               double var8 = var3[2];
               float var10 = time();
               double var11 = Math.toRadians(baseYaw);
               double var13 = -Math.sin(var11);
               double var15 = Math.cos(var11);
               double var17 = -Math.cos(var11);
               double var19 = -Math.sin(var11);
               String var27 = id;
               double var21;
               double var23;
               double var25;
               switch (var27) {
                  case "cs_landing":
                     float var45 = Ease.inOutCubic(Math.min(1.0F, var10 / 1.6F));
                     var21 = 9.0 - 5.5 * var45;
                     var23 = Math.toRadians(160.0F - 150.0F * var45);
                     var25 = 7.0F * (1.0F - var45) - 0.6 * var45 + 0.3 * Math.max(0.0, var10 - 3.5);
                     if (var10 > 0.9 && var10 < 1.3) {
                        var25 += Math.sin((var10 - 0.9) * 60.0) * 0.06 * (1.3 - var10) / 0.4;
                     }
                     break;
                  case "cs_victory":
                     var21 = 3.6;
                     var23 = var10 / dur * Math.PI * 2.0 + 0.3;
                     var25 = 0.6 + Math.sin(var10 * 0.9) * 0.4;
                     break;
                  case "cs_portal":
                     float var44 = Ease.inOutCubic(Math.min(1.0F, var10 / dur));
                     var21 = 8.0 - 4.5 * var44;
                     var23 = Math.toRadians(20.0F - 40.0F * var44);
                     var25 = -0.7 + 0.4 * var44;
                     break;
                  case "cs_power":
                     float var43 = Math.min(1.0F, var10 / 3.8F);
                     var21 = 5.0F - 2.0F * var43;
                     var23 = var10 * 0.9;
                     var25 = 0.2;
                     if (var10 > 3.8 && var10 < 4.4) {
                        var21 += 2.5 * Ease.outCubic((var10 - 3.8F) / 0.6F);
                     } else if (var10 >= 4.4) {
                        var21 += 2.5;
                     }

                     double var48 = var10 < 3.8 ? var43 * 0.05 : Math.max(0.0, 0.2 - (var10 - 3.8) * 0.2);
                     var25 += Math.sin(var10 * 47.0F) * var48;
                     var23 += Math.cos(var10 * 53.0F) * var48 * 0.3;
                     break;
                  case "cs_xmas":
                     float var42 = Ease.inOutCubic(Math.min(1.0F, var10 / 3.0F));
                     float var47 = Ease.inOutCubic(Math.max(0.0F, Math.min(1.0F, (var10 - 3.2F) / 3.5F)));
                     var21 = 2.2 + 3.8 * var42 + 1.5 * var47;
                     var23 = Math.toRadians(10.0F + 25.0F * var42 + 70.0F * var47);
                     var25 = 0.1 + 1.2 * var42 + 1.4 * var47;
                     var6 += 0.8 * var42;
                     if (var10 > 3.2 && var10 < 3.6) {
                        var25 += Math.sin((var10 - 3.2) * 70.0) * 0.04;
                     }
                     break;
                  case "cs_selfie":
                     var21 = 1.6;
                     var23 = Math.toRadians(-18.0);
                     var25 = 0.35 + Math.sin(var10 * 1.5) * 0.03;
                     break;
                  case "cs_starfall":
                     float var29 = Ease.inOutCubic(Math.min(1.0F, var10 / 2.5F));
                     float var30 = Ease.inOutCubic(Math.max(0.0F, (var10 - 4.8F) / 1.6F));
                     var21 = 3.2 + 3.0F * var29 - 2.0F * var30;
                     var23 = Math.toRadians(30.0F + var10 * 8.0F);
                     var25 = -0.4 - 0.4 * var29;
                     var6 += 6.0F * var29 * (1.0F - var30);
                     break;
                  default:
                     return false;
               }

               double var41 = var4 + (var13 * Math.cos(var23) + var17 * Math.sin(var23)) * var21;
               double var46 = var8 + (var15 * Math.cos(var23) + var19 * Math.sin(var23)) * var21;
               double var31 = var3[1] + 1.3 + var25;
               double var33 = var4 - var41;
               double var35 = var6 - var31;
               double var37 = var8 - var46;
               var1[0] = var41;
               var1[1] = var31;
               var1[2] = var46;
               var1[3] = Math.toDegrees(Math.atan2(-var33, var37));
               var1[4] = -Math.toDegrees(Math.atan2(var35, Math.sqrt(var33 * var33 + var37 * var37)));
               return true;
            }
         }
      }
   }

   public static void renderFx(float var0) {
      if (id != null) {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            if (core == null) {
               core = tex("core");
               glow = tex("glow");
               star = tex("glitter");
            }

            double[] var2 = Mc.lerpPos(var1, var0);
            double var3 = var2[0];
            double var5 = var2[1];
            double var7 = var2[2];
            float var9 = time();
            String var10 = id;
            switch (var10) {
               case "cs_landing":
                  if (var9 < 1.0) {
                     double var37 = var5 + 10.0 * (1.0 - var9 / 1.0);
                     R3.line(var3, var37 + 3.0, var7, var3, var37, var7, 0.35, R3.argb(10475775, 0.0), R3.argb(16777215, 0.8));
                  }

                  double var38 = var9 - 1.0;
                  if (var38 > 0.0 && var38 < 1.6) {
                     double var42 = 0.4 + var38 * 5.0;
                     double var51 = 1.0 - var38 / 1.6;
                     R3.ring(var3, var5 + 0.05, var7, var42 * 0.85, var42, 48, R3.argb(12576511, 0.0), R3.argb(16777215, 0.8 * var51), 0.0);
                     R3.ring(var3, var5 + 0.04, var7, 0.0, var42 * 0.85, 48, R3.argb(8370431, 0.25 * var51), R3.argb(8370431, 0.0), 0.0);

                     for (int var57 = 0; var57 < 40; var57++) {
                        double var60 = var57 * 2.399;
                        double var62 = 1.5 + var57 % 7 * 0.4;
                        double var63 = Math.cos(var60) * var62 * var38;
                        double var64 = Math.sin(var60) * var62 * var38;
                        double var65 = 1.8 * var38 - 2.2 * var38 * var38;
                        if (var65 < 0.0) {
                           var65 = 0.0;
                        }

                        R3.billboard(glow, var3 + var63, var5 + 0.1 + var65, var7 + var64, 0.35, R3.argb(13154458, 0.55 * var51));
                     }
                  }
                  break;
               case "cs_victory":
                  for (int var36 = 0; var36 < 6; var36++) {
                     fireworks(var3, var5, var7, var9 - 0.4 - var36 * 0.85, var36);
                  }
                  break;
               case "cs_portal":
                  double var35 = Math.min(1.0, var9 / 1.5) * Math.min(1.0, (dur - var9) / 0.6);
                  double var41 = Math.min(1.0, var9 / 3.0);

                  for (int var48 = 0; var48 < 3; var48++) {
                     double var55 = var5 + 0.05 + var48 * 0.8 * var41;
                     R3.ring(
                        var3,
                        var55,
                        var7,
                        1.2 + var48 * 0.2,
                        1.4 + var48 * 0.2,
                        48,
                        R3.argb(13134847, 0.7 * var35),
                        R3.argb(5975039, 0.2 * var35),
                        var9 * (1.5 + var48)
                     );
                  }

                  for (int var49 = 0; var49 < 60; var49++) {
                     double var56 = var49 * 0.61 + var9 * 2.2;
                     double var59 = 1.3 + Math.sin(var49 * 1.7 + var9) * 0.2;
                     double var61 = (var49 * 0.37 + var9 * 0.8) % 3.0;
                     R3.glowBillboard(core, var3 + Math.cos(var56) * var59, var5 + var61, var7 + Math.sin(var56) * var59, 0.12, R3.argb(14721279, 0.9 * var35));
                  }

                  if (var9 > 2.5) {
                     double var50 = Math.min(1.0, (var9 - 2.5) / 0.6) * var35;
                     R3.line(var3, var5, var7, var3, var5 + 60.0, var7, 1.6 * var50, R3.argb(14267391, 0.6 * var50), R3.argb(8076287, 0.0));
                     R3.line(var3, var5, var7, var3, var5 + 60.0, var7, 0.5 * var50, R3.argb(16777215, 0.9 * var50), R3.argb(16777215, 0.0));
                  }
                  break;
               case "cs_power":
                  double var34 = Math.min(1.0, var9 / 3.8);
                  boolean var14 = var9 > 3.8;

                  for (int var45 = 0; var45 < 70; var45++) {
                     double var16 = var45 * 2.399 + var9 * 3.0F;
                     double var18 = (var45 * 0.173 + var9 * 1.6) % 1.0;
                     double var20 = 0.55 + 0.25 * Math.sin(var45 + var9 * 4.0F);
                     double var22 = var18 * 2.4;
                     double var24 = (1.0 - var18) * var34 * (var14 ? Math.max(0.0, 1.0 - (var9 - 3.8) / 1.5) : 1.0);
                     int var26 = var45 % 3 == 0 ? 16774048 : 16756782;
                     R3.glowBillboard(
                        glow,
                        var3 + Math.cos(var16) * var20,
                        var5 + var22,
                        var7 + Math.sin(var16) * var20,
                        0.5 * (1.0 - var18 * 0.5),
                        R3.argb(var26, 0.55 * var24)
                     );
                  }

                  if (var14) {
                     double var46 = var9 - 3.8;
                     double var53 = Math.max(0.0, 1.0 - var46 / 1.2);
                     R3.ring(var3, var5 + 1.0, var7, var46 * 8.0, var46 * 8.0 + 0.6, 64, R3.argb(16769162, 0.0), R3.argb(16777215, var53), 0.0);
                     R3.glowBillboard(glow, var3, var5 + 1.0, var7, 2.0 + var46 * 12.0, R3.argb(16765040, 0.6 * var53));
                  }

                  if (!var14 && (int)(var9 * 7.0F) % 3 == 0) {
                     double var47 = Math.floor(var9 * 7.0F) * 1.3;
                     double var54 = var3 + Math.cos(var47) * 0.9;
                     double var58 = var7 + Math.sin(var47) * 0.9;
                     R3.line(
                        var54,
                        var5 + 2.2,
                        var58,
                        var3 + Math.cos(var47 + 0.4) * 0.6,
                        var5 + 0.8,
                        var7 + Math.sin(var47 + 0.4) * 0.6,
                        0.06,
                        R3.argb(16777215, 0.9),
                        R3.argb(16769136, 0.9)
                     );
                  }
                  break;
               case "cs_selfie":
                  if (var9 > dur - 0.9 && var9 < dur - 0.5) {
                     double var32 = 1.0 - Math.abs((var9 - (dur - 0.7)) / 0.2);
                     R3.glowBillboard(glow, var3, var5 + 1.4, var7, 6.0, R3.argb(16777215, Math.max(0.0, var32)));
                  }

                  for (int var33 = 0; var33 < 6; var33++) {
                     double var40 = var9 * 1.5 + var33;
                     double var44 = 1.6 + (var9 * 0.5 + var33 * 0.3) % 1.2;
                     R3.billboardRot(
                        star, var3 + Math.cos(var40) * 0.7, var5 + var44, var7 + Math.sin(var40) * 0.7, 0.18, var9 * 3.0F + var33, R3.argb(16770290, 0.9)
                     );
                  }
                  break;
               case "cs_xmas":
                  xmasFx(var3, var5, var7, var9);
                  break;
               case "cs_darkaura":
                  darkAuraFx(var3, var5, var7, var9);
                  break;
               case "cs_evillaugh":
                  evilFx(var3, var5, var7, var9);
                  break;
               case "cs_starfall":
                  for (int var12 = 0; var12 < 14; var12++) {
                     double var13 = 2.2;
                     double var15 = (var9 + var12 * 0.37) % var13 / var13;
                     double var17 = var12 * 2.1;
                     double var19 = 6 + var12 % 5 * 3;
                     double var21 = var3 + Math.cos(var17) * var19;
                     double var23 = var7 + Math.sin(var17) * var19;
                     double var25 = var5 + 30.0 - var15 * 26.0;
                     double var27 = var21 - var15 * 4.0;
                     R3.line(var27 + 3.0, var25 + 3.0, var23, var27, var25, var23, 0.18, R3.argb(11061503, 0.0), R3.argb(16777215, 0.9 * (1.0 - var15)));
                     R3.glowBillboard(core, var27, var25, var23, 0.8, R3.argb(14214911, 0.9 * (1.0 - var15)));
                  }

                  for (int var31 = 0; var31 < 30; var31++) {
                     double var39 = var31 * 2.399 + var9 * 0.5;
                     double var43 = 0.6 + var31 % 5 * 0.35;
                     double var52 = (var31 * 0.21 + var9 * 0.4) % 2.5;
                     R3.billboardRot(
                        star, var3 + Math.cos(var39) * var43, var5 + var52, var7 + Math.sin(var39) * var43, 0.1, var9 + var31, R3.argb(16777215, 0.8)
                     );
                  }
            }
         }
      }
   }

   private static void fireworks(double var0, double var2, double var4, double var6, int var8) {
      if (!(var6 < 0.0) && !(var6 > 2.2)) {
         int[] var9 = new int[]{16731501, 16765503, 4182783, 10181631, 6029211, 16747069};
         int var10 = var9[var8 % var9.length];
         double var11 = var8 * 1.9;
         double var13 = var0 + Math.cos(var11) * 4.0;
         double var15 = var4 + Math.sin(var11) * 4.0;
         double var17 = var2 + 7.0 + var8 % 3;
         if (var6 < 0.5) {
            double var38 = var6 / 0.5;
            R3.line(
               var13,
               var2 + var38 * (var17 - var2) - 1.2,
               var15,
               var13,
               var2 + var38 * (var17 - var2),
               var15,
               0.12,
               R3.argb(16767392, 0.0),
               R3.argb(16777215, 0.9)
            );
         } else {
            double var19 = var6 - 0.5;
            double var21 = Math.max(0.0, 1.0 - var19 / 1.7);

            for (int var23 = 0; var23 < 48; var23++) {
               double var24 = (var23 + 0.5) / 48.0;
               double var26 = Math.acos(1.0 - 2.0 * var24);
               double var28 = var23 * 2.399;
               double var30 = 4.5 * (1.0 - Math.exp(-var19 * 3.0));
               double var32 = var13 + Math.sin(var26) * Math.cos(var28) * var30;
               double var34 = var17 + Math.cos(var26) * var30 - var19 * var19 * 1.2;
               double var36 = var15 + Math.sin(var26) * Math.sin(var28) * var30;
               R3.glowBillboard(core, var32, var34, var36, 0.35, R3.argb(var10, var21));
            }

            R3.glowBillboard(glow, var13, var17, var15, 6.0 * var21, R3.argb(var10, 0.3 * var21));
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
      float[] var0 = cuts();
      if (var0 == null) {
         return -1;
      } else {
         float var1 = time();
         int var2 = 0;

         for (int var3 = 0; var3 < var0.length; var3++) {
            if (var1 >= var0[var3]) {
               var2 = var3;
            }
         }

         return var2;
      }
   }

   public static float shotTime() {
      int var0 = shot();
      return var0 < 0 ? 0.0F : time() - cuts()[var0];
   }

   public static float lockYaw() {
      return baseYaw;
   }

   public static boolean locksYaw() {
      return "cs_darkaura".equals(id) || "cs_evillaugh".equals(id);
   }

   static double[] eye(double var0, double var2, double var4) {
      double var6 = Math.toRadians(baseYaw);
      double var8 = -Math.sin(var6);
      double var10 = Math.cos(var6);
      double var12 = -Math.cos(var6);
      double var14 = -Math.sin(var6);
      return new double[]{var0 + var8 * 0.245 + var12 * 0.117, var2 + 1.611, var4 + var10 * 0.245 + var14 * 0.117};
   }

   private static boolean darkAuraCam(double[] var0, float var1, double[] var2) {
      double var3 = Math.toRadians(baseYaw);
      double var5 = -Math.sin(var3);
      double var7 = Math.cos(var3);
      double var9 = -Math.cos(var3);
      double var11 = -Math.sin(var3);
      double var13 = var0[0];
      double var15 = var0[1];
      double var17 = var0[2];
      int var31 = shot();
      float var32 = shotTime();
      double var19;
      double var21;
      double var23;
      double var25;
      double var27;
      double var29;
      switch (var31) {
         case 0:
            double var43 = 5.6 - var32 * 0.55;
            double var46 = Math.toRadians(28.0F - var32 * 8.0F);
            var19 = var13 + (var5 * Math.cos(var46) + var9 * Math.sin(var46)) * var43;
            var23 = var17 + (var7 * Math.cos(var46) + var11 * Math.sin(var46)) * var43;
            var21 = var15 + 0.35;
            var25 = var13;
            var27 = var15 + 1.25;
            var29 = var17;
            break;
         case 1:
            double var42 = 0.9 - var32 * 0.5;
            var19 = var13 + var5 * 1.5 + var9 * var42;
            var23 = var17 + var7 * 1.5 + var11 * var42;
            var21 = var15 + 0.22;
            var25 = var13 + var9 * 0.1;
            var27 = var15 + 0.2;
            var29 = var17 + var11 * 0.1;
            break;
         case 2:
            double var41 = 1.35 - var32 * 0.12;
            var19 = var13 + var9 * var41 + var5 * 0.15;
            var23 = var17 + var11 * var41 + var7 * 0.15;
            var21 = var15 + 1.55 + var32 * 0.04;
            var25 = var13 + var5 * 0.1;
            var27 = var15 + 1.55;
            var29 = var17 + var7 * 0.1;
            break;
         case 3:
            double[] var40 = eye(var13, var15, var17);
            double var45 = 0.55 - Math.min(1.0, var32 / 1.3) * 0.14;
            double var48 = Math.max(0.0, var32 - 0.35) * 0.0025;
            var19 = var40[0] + var5 * var45 + Math.sin(var1 * 61.0F) * var48;
            var23 = var40[2] + var7 * var45 + Math.cos(var1 * 57.0F) * var48;
            var21 = var40[1] + Math.sin(var1 * 53.0F) * var48;
            var25 = var40[0];
            var27 = var40[1];
            var29 = var40[2];
            break;
         case 4:
            double var39 = Math.max(0.0, 0.18 - var32 * 0.2);
            double var35 = Math.toRadians(-20.0);
            var19 = var13 + (var5 * Math.cos(var35) + var9 * Math.sin(var35)) * 2.6 + Math.sin(var1 * 71.0F) * var39;
            var23 = var17 + (var7 * Math.cos(var35) + var11 * Math.sin(var35)) * 2.6;
            var21 = var15 + 1.2 + Math.cos(var1 * 67.0F) * var39;
            var25 = var13;
            var27 = var15 + 1.15;
            var29 = var17;
            break;
         default:
            float var33 = Ease.inOutCubic(Math.min(1.0F, var32 / 1.5F));
            double var34 = 2.8 + 3.6 * var33;
            double var36 = Math.toRadians(-20.0F + 50.0F * var33);
            var19 = var13 + (var5 * Math.cos(var36) + var9 * Math.sin(var36)) * var34;
            var23 = var17 + (var7 * Math.cos(var36) + var11 * Math.sin(var36)) * var34;
            var21 = var15 + 1.2 + 1.6 * var33;
            var25 = var13;
            var27 = var15 + 1.1;
            var29 = var17;
      }

      double var44 = var25 - var19;
      double var47 = var27 - var21;
      double var37 = var29 - var23;
      var2[0] = var19;
      var2[1] = var21;
      var2[2] = var23;
      var2[3] = Math.toDegrees(Math.atan2(-var44, var37));
      var2[4] = -Math.toDegrees(Math.atan2(var47, Math.sqrt(var44 * var44 + var37 * var37)));
      return true;
   }

   private static void darkAuraFx(double var0, double var2, double var4, float var6) {
      if (bat0 == null) {
         bat0 = tex("bat0");
         bat1 = tex("bat1");
      }

      double var7 = Math.min(1.0, var6 / 0.6);
      double var9 = Math.min(1.0, Math.max(0.0, (dur - var6) / 0.8));
      double var11 = var7 * var9;
      boolean var13 = var6 > DA_CUTS[4];
      double var14 = var13 ? var6 - DA_CUTS[4] : 0.0;

      for (int var16 = 0; var16 < 110; var16++) {
         double var17 = var16 * 2.399;
         double var19 = (var16 * 0.137 + var6 * (0.28 + var16 % 5 * 0.04)) % 1.0;
         double var21 = 0.35 + var16 % 7 * 0.26 + var19 * 0.8 + var14 * 1.5;
         double var23 = var17 + var6 * 0.4 * ((var16 & 1) == 0 ? 1 : -1) + var19 * 1.3;
         double var25 = var19 * (2.4 + var16 % 4 * 0.4);
         double var27 = 0.5 + var19 * 1.3 + var16 % 3 * 0.2;
         double var29 = Math.sin(var19 * Math.PI) * var11 * (var13 ? Math.max(0.35, 1.0 - var14 * 0.5) : 1.0);
         int var31 = var16 % 6 == 0 ? 3801096 : (var16 % 4 == 0 ? 1442822 : 328196);
         R3.billboard(glow, var0 + Math.cos(var23) * var21, var2 + 0.05 + var25, var4 + Math.sin(var23) * var21, var27, R3.argb(var31, 0.62 * var29));
      }

      R3.ring(var0, var2 + 0.03, var4, 0.0, 2.4 + var14 * 3.0, 48, R3.argb(0, 0.7 * var11), R3.argb(0, 0.0), 0.0);
      R3.ring(var0, var2 + 0.04, var4, 1.5, 1.62, 64, R3.argb(16715824, 0.55 * var11), R3.argb(8388624, 0.25 * var11), var6 * 0.6);

      for (int var48 = 0; var48 < 26; var48++) {
         double var52 = (var48 * 0.31 + var6 * 0.5) % 1.0;
         double var55 = var48 * 1.7 + var6 * 0.8;
         double var58 = 0.5 + var48 % 5 * 0.35;
         R3.glowBillboard(
            core, var0 + Math.cos(var55) * var58, var2 + var52 * 3.0, var4 + Math.sin(var55) * var58, 0.06, R3.argb(16722490, 0.9 * (1.0 - var52) * var11)
         );
      }

      for (int var49 = 0; var49 < 30; var49++) {
         double var53 = 1.1 + var49 % 5 * 0.25;
         double var56 = var49 * 2.1 + var6 * var53 * (var49 % 3 == 0 ? -1 : 1);
         double var59 = 1.3 + var49 % 6 * 0.45 + Math.sin(var6 * 2.0F + var49) * 0.25 + var14 * var14 * 4.0;
         double var60 = 0.4 + var49 % 7 * 0.45 + Math.sin(var6 * 3.1 + var49 * 1.3) * 0.25 + var14 * 1.2;
         Identifier var61 = (int)(var6 * 13.0F + var49) % 2 == 0 ? bat0 : bat1;
         R3.billboard(
            var61, var0 + Math.cos(var56) * var59, var2 + var60, var4 + Math.sin(var56) * var59, 0.34 + var49 % 3 * 0.08, R3.argb(1706514, 0.95 * var11)
         );
      }

      if (var6 > DA_CUTS[2] + 0.6) {
         double var50 = Math.min(1.0, (var6 - DA_CUTS[2] - 0.6) / 0.5) * var9;
         double var18 = 0.85 + 0.15 * Math.sin(var6 * 31.0F);
         double[] var20 = eye(var0, var2, var4);
         R3.glowBillboard(glow, var20[0], var20[1], var20[2], 0.32 * var50 * var18, R3.argb(16714270, 0.8 * var50));
         R3.glowBillboard(core, var20[0], var20[1], var20[2], 0.07 * var50, R3.argb(16765136, var50));
         if (shot() >= 4) {
            R3.line(
               var20[0],
               var20[1],
               var20[2],
               var20[0] - Math.cos(Math.toRadians(baseYaw)) * 0.6,
               var20[1] + 0.05,
               var20[2] - Math.sin(Math.toRadians(baseYaw)) * 0.6,
               0.04,
               R3.argb(16715824, 0.8 * var50),
               R3.argb(16715824, 0.0)
            );
         }
      }

      if (var13 && var14 < 0.7) {
         double var51 = var14 / 0.7;
         double var54 = Math.toRadians(baseYaw);
         double var57 = -Math.sin(var54);
         double var22 = Math.cos(var54);
         double var24 = -Math.cos(var54);
         double var26 = -Math.sin(var54);

         for (int var28 = 0; var28 < 3; var28++) {
            double var62 = (var28 - 1) * 0.35;
            double var63 = 0.0;
            double var33 = 0.0;
            double var35 = 0.0;

            for (int var37 = 0; var37 <= 16; var37++) {
               double var38 = var37 / 16.0 * Math.min(1.0, var51 * 3.0);
               double var40 = -1.2 + var38 * 2.4;
               double var42 = var0 + var57 * (1.0 + var62 * 0.3) + var24 * Math.sin(var40) * 1.6;
               double var44 = var2 + 1.2 + Math.cos(var40) * 0.9 + var62;
               double var46 = var4 + var22 * (1.0 + var62 * 0.3) + var26 * Math.sin(var40) * 1.6;
               if (var37 > 0) {
                  R3.line(
                     var63,
                     var33,
                     var35,
                     var42,
                     var44,
                     var46,
                     0.09 * (1.0 - var51),
                     R3.argb(16715824, 0.9 * (1.0 - var51)),
                     R3.argb(16765136, 0.9 * (1.0 - var51))
                  );
               }

               var63 = var42;
               var33 = var44;
               var35 = var46;
            }
         }
      }
   }

   private static float[] pose() {
      EmoteLib.Emote var0 = EmoteLib.get(id);
      return var0 == null ? null : var0.pose(time());
   }

   static double[] eyeAt(double var0, double var2, double var4) {
      float[] var6 = pose();
      double var7 = var6 != null && !Float.isNaN(var6[0]) ? var6[0] : 0.0;
      double var9 = var6 != null && !Float.isNaN(var6[3]) ? var6[3] : 0.0;
      double var11 = Math.toRadians(baseYaw);
      double var13 = -Math.sin(var11);
      double var15 = Math.cos(var11);
      double var17 = -Math.cos(var11);
      double var19 = -Math.sin(var11);
      double var21 = 0.75 + 0.75 * Math.cos(var9);
      double var23 = 0.75 * Math.sin(var9);
      double var25 = var7 + var9;
      double var27 = var21 + 0.111 * Math.cos(var25) - 0.245 * Math.sin(var25);
      double var29 = var23 + 0.245 * Math.cos(var25) + 0.111 * Math.sin(var25);
      return new double[]{var0 + var13 * var29 + var17 * 0.117, var2 + var27, var4 + var15 * var29 + var19 * 0.117};
   }

   private static boolean evilCam(double[] var0, float var1, double[] var2) {
      double var3 = Math.toRadians(baseYaw);
      double var5 = -Math.sin(var3);
      double var7 = Math.cos(var3);
      double var9 = -Math.cos(var3);
      double var11 = -Math.sin(var3);
      double var13 = var0[0];
      double var15 = var0[1];
      double var17 = var0[2];
      int var31 = shot();
      float var32 = shotTime();
      double var19;
      double var21;
      double var23;
      double var25;
      double var27;
      double var29;
      switch (var31) {
         case 0:
            float var43 = Ease.inOutCubic(Math.min(1.0F, var32 / 2.6F));
            double var46 = 5.4 - 2.3 * var43;
            double var49 = Math.toRadians(24.0F - 16.0F * var43);
            var19 = var13 + (var5 * Math.cos(var49) + var9 * Math.sin(var49)) * var46;
            var23 = var17 + (var7 * Math.cos(var49) + var11 * Math.sin(var49)) * var46;
            var21 = var15 + 0.9 + 0.15 * var43;
            var25 = var13;
            var27 = var15 + 1.35 + 0.1 * var43;
            var29 = var17;
            break;
         case 1:
            double var42 = 1.05 - Math.min(1.0, var32 / 1.8) * 0.15;
            double var47 = 0.012;
            var19 = var13 + var5 * var42 + var9 * 0.18 + Math.sin(var1 * 43.0F) * var47;
            var23 = var17 + var7 * var42 + var11 * 0.18 + Math.cos(var1 * 39.0F) * var47;
            var21 = var15 + 0.95 + Math.sin(var1 * 47.0F) * var47;
            var25 = var13 + var5 * 0.05;
            var27 = var15 + 1.72;
            var29 = var17 + var7 * 0.05;
            break;
         case 2:
            double[] var41 = eyeAt(var13, var15, var17);
            double var45 = 0.52 - Math.min(1.0, var32 / 1.5) * 0.14;
            double var36 = Math.max(0.0, var32 - 0.5) * 0.0028;
            var19 = var41[0] + var5 * var45 + Math.sin(var1 * 61.0F) * var36;
            var23 = var41[2] + var7 * var45 + Math.cos(var1 * 57.0F) * var36;
            var21 = var41[1] + Math.sin(var1 * 53.0F) * var36;
            var25 = var41[0];
            var27 = var41[1];
            var29 = var41[2];
            break;
         default:
            float var33 = Ease.inOutCubic(Math.min(1.0F, var32 / 1.4F));
            float var34 = Ease.inOutCubic(Math.min(1.0F, var32 / 4.0F));
            double var35 = 2.6 + 3.0 * var33;
            double var37 = 8.168140899333462 * var34 + Math.toRadians(10.0);
            double var39 = Math.max(0.0, 0.12 - var32 * 0.12) + bolt(var1) * 0.06;
            var19 = var13 + (var5 * Math.cos(var37) + var9 * Math.sin(var37)) * var35 + Math.sin(var1 * 71.0F) * var39;
            var23 = var17 + (var7 * Math.cos(var37) + var11 * Math.sin(var37)) * var35;
            var21 = var15 + 0.45 + 1.3 * var33 + Math.cos(var1 * 67.0F) * var39;
            var25 = var13;
            var27 = var15 + 1.25;
            var29 = var17;
      }

      double var44 = var25 - var19;
      double var48 = var27 - var21;
      double var50 = var29 - var23;
      var2[0] = var19;
      var2[1] = var21;
      var2[2] = var23;
      var2[3] = Math.toDegrees(Math.atan2(-var44, var50));
      var2[4] = -Math.toDegrees(Math.atan2(var48, Math.sqrt(var44 * var44 + var50 * var50)));
      return true;
   }

   public static float bolt(float var0) {
      float var1 = 0.0F;

      for (float var5 : EL_BOLTS) {
         if (var0 >= var5 && var0 < var5 + 0.45F) {
            var1 = Math.max(var1, (float)Math.exp(-(var0 - var5) * 9.0F) * (0.6F + 0.4F * (float)Math.abs(Math.sin((var0 - var5) * 60.0F))));
         }
      }

      return var1;
   }

   public static double heartbeat(float var0) {
      double var1 = 0.0;

      for (Cutscenes.Cue var6 : CUES.get("cs_evillaugh")) {
         if (var6.id().endsWith("heartbeat")) {
            double var7 = var0 - var6.t();
            if (var7 >= 0.0 && var7 < 0.7) {
               var1 = Math.max(var1, Math.exp(-var7 * 9.0) + 0.6 * (var7 > 0.22 ? Math.exp(-(var7 - 0.22) * 9.0) : 0.0));
            }
         }
      }

      return Math.min(1.0, var1);
   }

   private static void arc(
      double var0, double var2, double var4, double var6, double var8, double var10, long var12, int var14, double var15, double var17, double var19
   ) {
      Random var21 = new Random(var12);
      double var22 = var0;
      double var24 = var2;
      double var26 = var4;

      for (int var28 = 1; var28 <= var14; var28++) {
         double var29 = (double)var28 / var14;
         double var31 = var28 == var14 ? 0.0 : var15 * Math.sin(var29 * Math.PI);
         double var33 = var0 + (var6 - var0) * var29 + (var21.nextDouble() - 0.5) * 2.0 * var31;
         double var35 = var2 + (var8 - var2) * var29 + (var21.nextDouble() - 0.5) * var31;
         double var37 = var4 + (var10 - var4) * var29 + (var21.nextDouble() - 0.5) * 2.0 * var31;
         R3.line(var22, var24, var26, var33, var35, var37, var17 * 3.0, R3.argb(16714270, 0.35 * var19), R3.argb(16714270, 0.35 * var19));
         R3.line(var22, var24, var26, var33, var35, var37, var17, R3.argb(16769252, var19), R3.argb(16736368, var19));
         if (var28 == var14 / 2 && var21.nextBoolean()) {
            double var39 = var33 + (var21.nextDouble() - 0.5) * var15 * 3.0;
            double var41 = var35 - var15 * 1.5;
            double var43 = var37 + (var21.nextDouble() - 0.5) * var15 * 3.0;
            R3.line(var33, var35, var37, var39, var41, var43, var17 * 0.6, R3.argb(16744592, var19), R3.argb(16715824, 0.0));
         }

         var22 = var33;
         var24 = var35;
         var26 = var37;
      }
   }

   private static void evilFx(double var0, double var2, double var4, float var6) {
      if (page == null) {
         page = tex("confetti");
         ash = tex("ash");
      }

      double var7 = Math.min(1.0, var6 / 0.6);
      double var9 = Math.min(1.0, Math.max(0.0, (dur - var6) / 0.8));
      double var11 = var7 * var9;
      double var13 = heartbeat(var6);
      int var15 = shot();
      boolean var16 = var15 >= 3;
      double var17 = var16 ? var6 - EL_CUTS[3] : 0.0;

      for (int var19 = 0; var19 < 80; var19++) {
         double var20 = (var19 * 0.137 + var6 * (0.32 + var19 % 5 * 0.05)) % 1.0;
         double var22 = var19 * 2.399 + var6 * 0.5 * ((var19 & 1) == 0 ? 1 : -1) + var20 * 1.6;
         double var24 = 0.3 + var19 % 6 * 0.14 + var20 * 0.5 + var13 * 0.25 + (var16 ? Math.min(1.0, var17) * 0.6 : 0.0);
         double var26 = 0.45 + var20 * 1.1 + var13 * 0.3;
         double var28 = Math.sin(var20 * Math.PI) * var11 * (0.75 + 0.25 * var13);
         int var30 = var19 % 5 == 0 ? 4849676 : (var19 % 3 == 0 ? 1704710 : 262402);
         R3.billboard(
            glow,
            var0 + Math.cos(var22) * var24,
            var2 + 0.05 + var20 * (2.2 + var19 % 3 * 0.3),
            var4 + Math.sin(var22) * var24,
            var26,
            R3.argb(var30, 0.6 * var28)
         );
      }

      R3.glowBillboard(glow, var0, var2 + 1.0, var4, 2.6 + var13 * 0.8, R3.argb(11534362, (0.18 + 0.22 * var13) * var11));
      R3.ring(var0, var2 + 0.03, var4, 0.0, 2.6, 48, R3.argb(0, 0.75 * var11), R3.argb(0, 0.0), 0.0);
      R3.ring(
         var0,
         var2 + 0.04,
         var4,
         1.3 + var13 * 0.12,
         1.4 + var13 * 0.16,
         64,
         R3.argb(16715824, (0.45 + 0.4 * var13) * var11),
         R3.argb(8388624, 0.2 * var11),
         -var6 * 0.4
      );
      R3.ring(var0, var2 + 0.045, var4, 1.85, 1.9, 64, R3.argb(16715824, 0.3 * var11), R3.argb(16715824, 0.1 * var11), var6 * 0.25);
      if (var13 > 0.05) {
         R3.ring(
            var0,
            var2 + 0.05,
            var4,
            0.4 + (1.0 - var13) * 2.5,
            0.5 + (1.0 - var13) * 2.6,
            48,
            R3.argb(16719936, 0.0),
            R3.argb(16719936, 0.5 * var13 * var11),
            0.0
         );
      }

      for (int var39 = 0; var39 < 90; var39++) {
         double var41 = (var39 * 0.083 + var6 * (0.09 + var39 % 4 * 0.015)) % 1.0;
         double var48 = var39 * 2.399;
         double var53 = 0.5 + var39 % 11 * 0.45;
         double var60 = var0 + Math.cos(var48) * var53 + Math.sin(var6 * 0.8 + var39) * 0.3;
         double var64 = var4 + Math.sin(var48) * var53 + Math.cos(var6 * 0.7 + var39) * 0.3;
         R3.billboardRot(
            ash,
            var60,
            var2 + 5.5 - var41 * 5.6,
            var64,
            0.06 + var39 % 3 * 0.03,
            var6 * 0.6 + var39,
            R3.argb(var39 % 7 == 0 ? 5904926 : 2763310, 0.85 * var11 * Math.min(1.0, var41 * 5.0))
         );
      }

      int var40 = var16 ? 44 : 12;

      for (int var42 = 0; var42 < var40; var42++) {
         double var29 = 0.2 + var42 % 4 * 0.04;
         double var21;
         double var23;
         double var25;
         double var27;
         if (!var16) {
            double var31 = (var42 * 0.27 + var6 * 0.11) % 1.0;
            double var33 = var42 * 2.1;
            double var35 = 1.4 + var42 % 4 * 0.6;
            var21 = var0 + Math.cos(var33) * var35 + Math.sin(var6 * 1.3 + var42) * 0.35;
            var25 = var4 + Math.sin(var33) * var35 + Math.cos(var6 * 1.1 + var42) * 0.35;
            var23 = var2 + 3.6 - var31 * 3.5;
            var27 = Math.sin(var6 * 2.0F + var42) * 1.2;
         } else {
            double var68 = Math.min(1.0, var17 / 0.6);
            double var72 = 1.6 + var42 % 5 * 0.3;
            double var74 = var42 * 2.399 + var17 * var72 * (1.0 + 0.8 * Math.max(0.0, 1.0 - var17));
            double var37 = 0.5 + var68 * (1.1 + var42 % 7 * 0.35) + Math.sin(var17 * 2.0 + var42) * 0.2;
            var21 = var0 + Math.cos(var74) * var37;
            var25 = var4 + Math.sin(var74) * var37;
            var23 = var2 + 0.3 + (var42 * 0.23 + var17 * (0.35 + var42 % 3 * 0.1)) % 1.0 * 3.4 * var68 + (1.0 - var68) * 1.0;
            var27 = var17 * (4 + var42 % 5) + var42;
         }

         double var69 = var11 * (var16 ? 1.0 : Math.min(1.0, var6 / 1.0));
         R3.glowBillboard(page, var21, var23, var25, var29 * 1.25, R3.argb(16715824, 0.35 * var69));
         R3.billboardRot(page, var21, var23, var25, var29, var27, R3.argb(657416, 0.95 * var69));
      }

      for (float var52 : EL_BOLTS) {
         float var54 = var6 - var52;
         if (!(var54 < 0.0F) && !(var54 > 0.4F)) {
            double var57 = Math.exp(-var54 * 7.0F) * (0.5 + 0.5 * Math.abs(Math.sin(var54 * 55.0F)));
            double var61 = var52 * 2.3;
            double var65 = var52 == EL_BOLTS[0] ? 7.0 : 3.2;
            double var70 = var0 + Math.cos(var61) * var65;
            double var73 = var4 + Math.sin(var61) * var65;
            arc(var70 + 1.5, var2 + 18.0, var73 - 1.0, var70, var2, var73, (long)(var52 * 100.0F) + (long)(var54 * 20.0F), 14, 0.9, 0.12, var57 * var11);
            R3.glowBillboard(glow, var70, var2 + 0.3, var73, 3.0 * var57, R3.argb(16719936, 0.6 * var57 * var11));
         }
      }

      if (var15 == 1 || var16) {
         int var44 = var16 ? 3 : 1;

         for (int var47 = 0; var47 < var44; var47++) {
            long var50 = (long)(var6 * 14.0F) * 7L + var47;
            Random var55 = new Random(var50);
            if (!(var55.nextDouble() > (var16 ? 0.75 : 0.4))) {
               double var58 = var55.nextDouble() * Math.PI * 2.0;
               double var62 = var58 + 0.6 + var55.nextDouble();
               double var66 = 0.6 + var55.nextDouble() * 0.5;
               double var71 = 0.8 + var55.nextDouble() * 0.9;
               arc(
                  var0 + Math.cos(var58) * var66,
                  var2 + 1.4 + var55.nextDouble() * 0.9,
                  var4 + Math.sin(var58) * var66,
                  var0 + Math.cos(var62) * var71,
                  var2 + 0.1 + var55.nextDouble() * 0.5,
                  var4 + Math.sin(var62) * var71,
                  var50,
                  7,
                  0.22,
                  0.035,
                  0.9 * var11
               );
            }
         }
      }

      if (var6 > EL_CUTS[2] - 0.1) {
         double var45 = Math.min(1.0, (var6 - EL_CUTS[2] + 0.1) / 0.5) * var9;
         double var51 = 0.85 + 0.15 * Math.sin(var6 * 31.0F);
         double[] var56 = eyeAt(var0, var2, var4);
         R3.glowBillboard(glow, var56[0], var56[1], var56[2], (0.32 + 0.1 * var13) * var45 * var51, R3.argb(16714270, 0.85 * var45));
         R3.glowBillboard(core, var56[0], var56[1], var56[2], 0.07 * var45, R3.argb(16765136, var45));
         if (var16) {
            double var59 = Math.toRadians(baseYaw);
            double var63 = -Math.cos(var59);
            double var67 = -Math.sin(var59);
            R3.line(
               var56[0],
               var56[1],
               var56[2],
               var56[0] + var63 * 0.7,
               var56[1] + 0.08,
               var56[2] + var67 * 0.7,
               0.045,
               R3.argb(16715824, 0.85 * var45),
               R3.argb(16715824, 0.0)
            );
         }
      }
   }

   private static void cone(double var0, double var2, double var4, double var6, double var8, int var10, int var11, int var12) {
      for (int var13 = 0; var13 < var10; var13++) {
         double var14 = var13 * Math.PI * 2.0 / var10;
         double var16 = (var13 + 1) * Math.PI * 2.0 / var10;
         double var18 = var0 + Math.cos(var14) * var6;
         double var20 = var4 + Math.sin(var14) * var6;
         double var22 = var0 + Math.cos(var16) * var6;
         double var24 = var4 + Math.sin(var16) * var6;
         int var26 = var13 % 2 == 0
            ? var11
            : R3.argb((var11 >> 16 & 0xFF) * 9 / 10 << 16 | (var11 >> 8 & 0xFF) * 9 / 10 << 8 | (var11 & 0xFF) * 9 / 10, (var11 >>> 24) / 255.0);
         R3.quad(var18, var2, var20, var22, var2, var24, var0, var2 + var8, var4, var0, var2 + var8, var4, var26, var26, var12, var12);
         R3.quad(var18, var2, var20, var22, var2, var24, var0, var2 + 0.05, var4, var0, var2 + 0.05, var4, var11, var11, var11, var11);
      }
   }

   private static void box(double var0, double var2, double var4, double var6, double var8, double var10, int var12, int var13) {
      R3.quad(var0, var8, var4, var6, var8, var4, var6, var8, var10, var0, var8, var10, var12);
      R3.quad(var0, var2, var4, var6, var2, var4, var6, var8, var4, var0, var8, var4, var13);
      R3.quad(var0, var2, var10, var6, var2, var10, var6, var8, var10, var0, var8, var10, var13);
      R3.quad(var0, var2, var4, var0, var2, var10, var0, var8, var10, var0, var8, var4, var13);
      R3.quad(var6, var2, var4, var6, var2, var10, var6, var8, var10, var6, var8, var4, var13);
   }

   private static void xmasFx(double var0, double var2, double var4, float var6) {
      double var7 = Math.toRadians(baseYaw);
      double var9 = -Math.sin(var7);
      double var11 = Math.cos(var7);
      double var13 = Math.min(1.0, Math.max(0.0, (dur - var6) / 0.8));
      double var15 = var0 - var9 * 2.2;
      double var17 = var4 - var11 * 2.2;
      double var19 = Ease.outBack(Math.max(0.0F, Math.min(1.0F, (var6 - 0.5F) / 2.0F)));
      if (var19 > 0.01) {
         double var21 = var19;
         box(
            var15 - 0.15 * var19,
            var2,
            var17 - 0.15 * var19,
            var15 + 0.15 * var19,
            var2 + 0.5 * var19,
            var17 + 0.15 * var19,
            R3.argb(5913118, var13),
            R3.argb(4861462, var13)
         );
         double[][] var23 = new double[][]{{0.45, 1.3, 1.2}, {1.05, 1.05, 1.0}, {1.6, 0.8, 0.85}, {2.1, 0.5, 0.7}};

         for (double[] var27 : var23) {
            cone(var15, var2 + var27[0] * var21, var17, var27[1] * var21, var27[2] * var21, 20, R3.argb(944692, var13), R3.argb(3127391, var13));
         }

         for (double[] var58 : var23) {
            R3.ring(
               var15,
               var2 + var58[0] * var21 + 0.02,
               var17,
               var58[1] * var21 * 0.82,
               var58[1] * var21 * 0.98,
               28,
               R3.argb(16777215, 0.55 * var13),
               R3.argb(15266559, 0.2 * var13),
               0.0
            );
         }

         double var51 = Math.max(0.0, Math.min(1.0, (var6 - 2.2) / 1.0));
         byte var56 = 46;

         for (int var59 = 0; var59 < var56 * var51; var59++) {
            double var28 = (double)var59 / var56;
            double var30 = 0.5 + var28 * 2.3;
            double var32 = (1.3 - var28 * 1.15) * 0.98;
            double var34 = var28 * Math.PI * 9.0 + 0.4;
            double var36 = 0.6 + 0.4 * Math.sin(var6 * 5.0F + var59 * 1.7);
            int var38 = XB[var59 % XB.length];
            double var39 = var15 + Math.cos(var34) * var32 * var21;
            double var41 = var2 + var30 * var21;
            double var43 = var17 + Math.sin(var34) * var32 * var21;
            R3.glowBillboard(glow, var39, var41, var43, 0.28 * var36, R3.argb(var38, 0.55 * var13));
            R3.glowBillboard(core, var39, var41, var43, 0.07, R3.argb(var38, var13));
         }

         double var60 = Math.max(0.0, Math.min(1.0, (var6 - 3.1) / 0.3));
         if (var60 > 0.0) {
            double var29 = var2 + 2.95 * var21;
            double var31 = 0.85 + 0.15 * Math.sin(var6 * 6.0F);
            R3.glowBillboard(glow, var15, var29, var17, 1.4 * var60 * var31, R3.argb(16766571, 0.8 * var13));
            R3.billboardRot(star, var15, var29, var17, 0.55 * var60, var6 * 0.8, R3.argb(16773808, var13));
            double var33 = var6 - 3.1;
            if (var33 > 0.0 && var33 < 1.4) {
               double var35 = 1.0 - var33 / 1.4;
               R3.ring(var15, var29, var17, var33 * 4.0, var33 * 4.0 + 0.25, 48, R3.argb(16769162, 0.0), R3.argb(16774864, 0.8 * var35), 0.0);

               for (int var37 = 0; var37 < 40; var37++) {
                  double var75 = (var37 + 0.5) / 40.0;
                  double var40 = Math.acos(1.0 - 2.0 * var75);
                  double var42 = var37 * 2.399;
                  double var44 = 3.5 * (1.0 - Math.exp(-var33 * 3.0));
                  R3.glowBillboard(
                     core,
                     var15 + Math.sin(var40) * Math.cos(var42) * var44,
                     var29 + Math.cos(var40) * var44 - var33 * var33 * 0.8,
                     var17 + Math.sin(var40) * Math.sin(var42) * var44,
                     0.18,
                     R3.argb(XB[var37 % XB.length], var35)
                  );
               }
            }
         }

         int[][] var63 = new int[][]{{14033464, 16040274}, {1739342, 14886459}, {1982074, 16040274}, {15249978, 11867436}, {7024560, 16050640}};

         for (int var65 = 0; var65 < 5; var65++) {
            double var67 = Math.max(0.0, Math.min(1.0, (var6 - 3.5 - var65 * 0.18) / 0.35));
            if (!(var67 <= 0.0)) {
               double var69 = Ease.outBack((float)var67);
               double var72 = var65 * 1.25 + 0.3;
               double var74 = 1.25;
               double var77 = var15 + Math.cos(var72) * var74;
               double var79 = var17 + Math.sin(var72) * var74;
               double var80 = (0.22 + var65 % 2 * 0.06) * var69;
               int var45 = R3.argb(var63[var65][0], var13);
               int var46 = R3.argb(var63[var65][1], var13);
               box(var77 - var80, var2, var79 - var80, var77 + var80, var2 + var80 * 1.6, var79 + var80, var45, var45);
               box(
                  var77 - var80 * 1.02, var2 + var80 * 1.6, var79 - var80 * 0.18, var77 + var80 * 1.02, var2 + var80 * 1.62, var79 + var80 * 0.18, var46, var46
               );
               box(
                  var77 - var80 * 0.18, var2 + var80 * 1.6, var79 - var80 * 1.02, var77 + var80 * 0.18, var2 + var80 * 1.62, var79 + var80 * 1.02, var46, var46
               );
            }
         }
      }

      for (int var47 = 0; var47 < 140; var47++) {
         double var22 = (var47 * 0.071 + var6 * 0.12) % 1.0;
         double var52 = var47 * 2.399;
         double var57 = 0.6 + var47 % 9 * 0.6;
         double var62 = var0 + Math.cos(var52) * var57 + Math.sin(var6 + var47) * 0.2;
         double var66 = var4 + Math.sin(var52) * var57;
         R3.glowBillboard(core, var62, var2 + 6.0 - var22 * 6.2, var66, 0.05 + var47 % 3 * 0.02, R3.argb(16777215, 0.85 * var13 * Math.min(1.0, var22 * 4.0)));
      }

      double var48 = (var6 - 4.3) / 2.6;
      if (var48 > 0.0 && var48 < 1.0) {
         double var49 = -Math.cos(var7);
         double var54 = -Math.sin(var7);
         double var61 = var0 - var49 * (14.0 - 28.0 * var48) - var9 * 8.0;
         double var64 = var4 - var54 * (14.0 - 28.0 * var48) - var11 * 8.0;
         double var68 = var2 + 9.0 + Math.sin(var48 * Math.PI) * 2.0;
         R3.glowBillboard(glow, var61, var68, var64, 1.2, R3.argb(16769952, 0.8 * var13));
         R3.glowBillboard(core, var61, var68, var64, 0.35, R3.argb(16724032, var13));

         for (int var70 = 1; var70 < 30; var70++) {
            double var71 = var48 - var70 * 0.012;
            if (var71 < 0.0) {
               break;
            }

            double var73 = var0 - var49 * (14.0 - 28.0 * var71) - var9 * 8.0;
            double var76 = var4 - var54 * (14.0 - 28.0 * var71) - var11 * 8.0;
            double var78 = var2 + 9.0 + Math.sin(var71 * Math.PI) * 2.0;
            R3.glowBillboard(
               star,
               var73,
               var78 + Math.sin(var70 * 1.3 + var6 * 9.0F) * 0.1,
               var76,
               0.25 * (1.0 - var70 / 30.0),
               R3.argb(XB[var70 % XB.length], (1.0 - var70 / 30.0) * var13)
            );
         }
      }
   }

   private static Identifier tex(String var0) {
      return TEX.computeIfAbsent(var0, var0x -> {
         Identifier var1 = Identifier.fromNamespaceAndPath("legoclient", "cutscene/" + var0x);

         try {
            Tx.register(var1, AmbientTex.paint(var0x), 128, 128, true);
         } catch (Throwable var3) {
         }

         return var1;
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

      for (Cutscenes.Cue[] var1 : CUES.values()) {
         Arrays.sort(var1, (var0, var1x) -> Float.compare(var0.t, var1x.t));
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
