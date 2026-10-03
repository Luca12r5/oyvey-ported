package dev.lego.visual.sky;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class SkyExtras {
   public static SkyExtras.SkyPerson person;

   private SkyExtras() {
   }

   public static void registerAll() {
      person = Modules.register(new SkyExtras.SkyPerson());
   }

   public static final class SkyPerson extends Module {
      final Setting.Mode background = this.add(new Setting.Mode("Hintergrund", 0, "Automatisch entfernen", "Bild ist schon freigestellt", "Nicht entfernen"));
      final Setting.Num tolerance = this.add(new Setting.Num("Toleranz (Hintergrund)", 10.0, 120.0, 5.0, 48.0, ""));
      final Setting.Mode look = this.add(new Setting.Mode("Look", 1, "Normal", "Leuchtend", "Geist", "Sonnenuntergang"));
      final Setting.Num azimuth = this.add(new Setting.Num("Richtung", 0.0, 355.0, 5.0, 180.0, "°"));
      final Setting.Num elevation = this.add(new Setting.Num("Höhe", 0.0, 70.0, 1.0, 22.0, "°"));
      final Setting.Num size = this.add(new Setting.Num("Größe", 20.0, 140.0, 5.0, 75.0, "°"));
      final Setting.Num opacity = this.add(new Setting.Num("Deckkraft", 10.0, 100.0, 5.0, 85.0, "%"));
      final Setting.Mode when = this.add(new Setting.Mode("Zeigen", 0, "Immer", "Nur am Tag", "Nur am Abend", "Nur nachts"));
      final Setting.Bool fadeBottom = this.add(new Setting.Bool("Unten in die Wolken ausblenden", true));
      final Setting.Bool bob = this.add(new Setting.Bool("Sanft schweben", true));
      private final AtomicReference<int[][]> pending = new AtomicReference<>();
      private volatile boolean working;
      private String wantKey;
      private String loadedKey;
      private Identifier tex;
      private Identifier glowTex;
      private int tw = 1;
      private int th = 1;
      private int index = 0;
      private long settingsHash;

      SkyPerson() {
         super("skyperson", "Sky-Figur", "Ein PNG von einem Menschen (oder was du willst) riesig am Himmel - Hintergrund wird entfernt", Category.VISUALS);
         this.icon("image");
         this.fresh();
         this.add(new Setting.Action("Bild", "Nächstes Bild", () -> {
            this.index++;
            this.loadedKey = null;
         }));
         this.add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
            Path var0 = folder();
            Mc.clipboard(var0.toString());
            Toasts.show("copy", "Ordner kopiert", "PNG/JPG hier ablegen: " + var0);
         }));
         this.add(new Setting.Action("Neu freistellen", "Neu berechnen", () -> this.loadedKey = null));
      }

      static Path folder() {
         Path var0 = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("sky_people");

         try {
            Files.createDirectories(var0);
         } catch (Throwable var2) {
         }

         return var0;
      }

      @Override
      public void onDisable() {
         if (this.tex != null) {
            Tx.destroy(this.tex);
         }

         if (this.glowTex != null) {
            Tx.destroy(this.glowTex);
         }

         this.tex = this.glowTex = null;
         this.loadedKey = null;
      }

      private void ensure() {
         long var1 = this.background.index * 1000L + this.tolerance.getI() + (this.fadeBottom.get() ? 100000 : 0);
         String var3 = this.index + "|" + var1;
         int[][] var4 = this.pending.getAndSet(null);
         if (var4 != null) {
            if (this.tex != null) {
               Tx.destroy(this.tex);
            }

            if (this.glowTex != null) {
               Tx.destroy(this.glowTex);
            }

            this.tex = this.glowTex = null;
            if (var4[0].length > 1) {
               this.tw = var4[2][0];
               this.th = var4[2][1];
               this.tex = Identifier.fromNamespaceAndPath("legoclient", "sky/person_" + (System.nanoTime() & 16777215L));
               this.glowTex = Identifier.fromNamespaceAndPath("legoclient", "sky/person_glow_" + (System.nanoTime() & 16777215L));
               Tx.register(this.tex, var4[0], this.tw, this.th, true);
               Tx.register(this.glowTex, var4[1], this.tw, this.th, true);
            }

            this.loadedKey = this.wantKey;
         }

         if (!var3.equals(this.loadedKey) && !this.working) {
            this.wantKey = var3;
            this.working = true;
            int var5 = this.background.index;
            int var6 = this.tolerance.getI();
            int var7 = this.index;
            boolean var8 = this.fadeBottom.get();
            Thread var9 = new Thread(() -> {
               try {
                  this.pending.set(process(var7, var5, var6, var8));
               } catch (Throwable var9x) {
                  LegoClient.LOG("Sky-Figur: " + var9x);
                  this.pending.set(new int[][]{{0}});
               } finally {
                  this.working = false;
               }
            }, "LegoClient-SkyPerson");
            var9.setDaemon(true);
            var9.start();
         }
      }

      private static int[][] process(int var0, int var1, int var2, boolean var3) throws Exception {
         ArrayList var4 = new ArrayList();
         File[] var5 = folder().toFile().listFiles();
         if (var5 != null) {
            for (File var9 : var5) {
               String var10 = var9.getName().toLowerCase(Locale.ROOT);
               if (var10.endsWith(".png") || var10.endsWith(".jpg") || var10.endsWith(".jpeg")) {
                  var4.add(var9);
               }
            }
         }

         var4.sort(Comparator.comparing(File::getName));
         if (var4.isEmpty()) {
            Toasts.show("image", "Sky-Figur: kein Bild", "Leg ein PNG in " + folder(), 0, 6000L);
            return new int[][]{{0}};
         } else {
            File var25 = (File)var4.get(Math.floorMod(var0, var4.size()));
            BufferedImage var26 = ImageIO.read(var25);
            if (var26 == null) {
               throw new IllegalStateException("Bild nicht lesbar: " + var25.getName());
            } else {
               int var27 = var26.getWidth();
               int var28 = var26.getHeight();
               double var29 = Math.min(1.0, 1024.0 / Math.max(var27, var28));
               int var12 = Math.max(8, (int)Math.round(var27 * var29));
               int var13 = Math.max(8, (int)Math.round(var28 * var29));
               BufferedImage var14 = new BufferedImage(var12, var13, 2);
               Graphics2D var15 = var14.createGraphics();
               var15.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
               var15.drawImage(var26, 0, 0, var12, var13, null);
               var15.dispose();
               int[] var16 = var14.getRGB(0, 0, var12, var13, null, 0, var12);
               int var17 = 0;

               for (int var21 : var16) {
                  if (var21 >>> 24 < 250) {
                     var17++;
                  }
               }

               boolean var30 = var17 > var16.length * 0.02;
               if (var1 == 0 && !var30) {
                  removeBackground(var16, var12, var13, var2);
               } else if (var1 == 2) {
                  for (int var31 = 0; var31 < var16.length; var31++) {
                     var16[var31] |= -16777216;
                  }
               }

               if (var3) {
                  int var32 = (int)(var13 * 0.78);

                  for (int var34 = var32; var34 < var13; var34++) {
                     double var37 = 1.0 - (double)(var34 - var32) / (var13 - var32);
                     var37 = var37 * var37 * (3.0 - 2.0 * var37);

                     for (int var23 = 0; var23 < var12; var23++) {
                        int var24 = var16[var34 * var12 + var23];
                        var16[var34 * var12 + var23] = (int)((var24 >>> 24) * var37) << 24 | var24 & 16777215;
                     }
                  }
               }

               float[] var33 = new float[var12 * var13];

               for (int var35 = 0; var35 < var16.length; var35++) {
                  var33[var35] = (var16[var35] >>> 24) / 255.0F;
               }

               int var36 = Math.max(4, var12 / 40);

               for (int var39 = 0; var39 < 3; var39++) {
                  var33 = blur(var33, var12, var13, var36);
               }

               int[] var40 = new int[var12 * var13];

               for (int var22 = 0; var22 < var40.length; var22++) {
                  var40[var22] = (int)Math.min(255.0, var33[var22] * 255.0F * 1.4) << 24 | 16777215;
               }

               Toasts.show("image", "Sky-Figur bereit", var25.getName() + (var1 == 0 && !var30 ? " · Hintergrund entfernt" : ""), 0, 3000L);
               return new int[][]{var16, var40, {var12, var13}};
            }
         }
      }

      static void removeBackground(int[] var0, int var1, int var2, int var3) {
         ArrayList var4 = new ArrayList();

         for (int var5 = 0; var5 < var1; var5++) {
            var4.add(var0[var5]);
            var4.add(var0[(var2 - 1) * var1 + var5]);
         }

         for (int var31 = 0; var31 < var2; var31++) {
            var4.add(var0[var31 * var1]);
            var4.add(var0[var31 * var1 + var1 - 1]);
         }

         int[] var32 = new int[var4.size()];
         int[] var6 = new int[var4.size()];
         int[] var7 = new int[var4.size()];

         for (int var8 = 0; var8 < var4.size(); var8++) {
            int var9 = (Integer)var4.get(var8);
            var32[var8] = var9 >> 16 & 0xFF;
            var6[var8] = var9 >> 8 & 0xFF;
            var7[var8] = var9 & 0xFF;
         }

         Arrays.sort(var32);
         Arrays.sort(var6);
         Arrays.sort(var7);
         int var33 = var32[var32.length / 2];
         int var34 = var6[var6.length / 2];
         int var10 = var7[var7.length / 2];
         boolean[] var11 = new boolean[var1 * var2];
         ArrayDeque var12 = new ArrayDeque();
         double var13 = var3;
         double var15 = var3 * 1.4;

         for (int var17 = 0; var17 < var1; var17++) {
            seed(var0, var11, var12, var17, 0, var1, var33, var34, var10, var15);
            seed(var0, var11, var12, var17, var2 - 1, var1, var33, var34, var10, var15);
         }

         for (int var35 = 0; var35 < var2; var35++) {
            seed(var0, var11, var12, 0, var35, var1, var33, var34, var10, var15);
            seed(var0, var11, var12, var1 - 1, var35, var1, var33, var34, var10, var15);
         }

         int[] var36 = new int[]{1, -1, 0, 0};
         int[] var18 = new int[]{0, 0, 1, -1};

         while (!var12.isEmpty()) {
            int var19 = (Integer)var12.poll();
            int var20 = var19 % var1;
            int var21 = var19 / var1;

            for (int var22 = 0; var22 < 4; var22++) {
               int var23 = var20 + var36[var22];
               int var24 = var21 + var18[var22];
               if (var23 >= 0 && var24 >= 0 && var23 < var1 && var24 < var2) {
                  int var25 = var24 * var1 + var23;
                  if (!var11[var25]) {
                     double var26 = dist(var0[var25], var33, var34, var10);
                     if (var26 < var13 || dist(var0[var25], var0[var19]) < var13 * 0.3 && var26 < var15 && dist(var0[var19], var33, var34, var10) < var13 * 0.6
                        )
                      {
                        var11[var25] = true;
                        var12.add(var25);
                     }
                  }
               }
            }
         }

         int[] var37 = new int[var1 * var2];
         Arrays.fill(var37, -1);
         ArrayList var38 = new ArrayList();

         for (int var39 = 0; var39 < var1 * var2; var39++) {
            if (!var11[var39] && var37[var39] < 0) {
               int var41 = var38.size();
               int var45 = 0;
               var37[var39] = var41;
               var12.add(var39);

               while (!var12.isEmpty()) {
                  int var49 = (Integer)var12.poll();
                  var45++;
                  int var52 = var49 % var1;
                  int var54 = var49 / var1;

                  for (int var27 = 0; var27 < 4; var27++) {
                     int var28 = var52 + var36[var27];
                     int var29 = var54 + var18[var27];
                     if (var28 >= 0 && var29 >= 0 && var28 < var1 && var29 < var2) {
                        int var30 = var29 * var1 + var28;
                        if (!var11[var30] && var37[var30] < 0) {
                           var37[var30] = var41;
                           var12.add(var30);
                        }
                     }
                  }
               }

               var38.add(var45);
            }
         }

         int var40 = 0;

         for (int var46 : (Iterable<Integer>) (Iterable<?>) (var38)) {
            var40 = Math.max(var40, var46);
         }

         float[] var43 = new float[var1 * var2];

         for (int var47 = 0; var47 < var1 * var2; var47++) {
            var43[var47] = !var11[var47] && ((Integer)var38.get(var37[var47])).intValue() >= Math.max(40.0, var40 * 0.04) ? 1.0F : 0.0F;
         }

         var43 = blur(var43, var1, var2, 1);

         for (int var48 = 0; var48 < var1 * var2; var48++) {
            float var50 = var43[var48];
            var50 = Math.max(0.0F, Math.min(1.0F, (var50 - 0.2F) / 0.75F));
            int var53 = var0[var48];
            int var55 = var53 >> 16 & 0xFF;
            int var56 = var53 >> 8 & 0xFF;
            int var57 = var53 & 0xFF;
            if (var50 > 0.02F && var50 < 0.98F) {
               var55 = clamp((var55 - var33 * (1.0F - var50)) / var50);
               var56 = clamp((var56 - var34 * (1.0F - var50)) / var50);
               var57 = clamp((var57 - var10 * (1.0F - var50)) / var50);
            }

            var0[var48] = (int)(var50 * 255.0F) << 24 | var55 << 16 | var56 << 8 | var57;
         }
      }

      private static void seed(int[] var0, boolean[] var1, ArrayDeque<Integer> var2, int var3, int var4, int var5, int var6, int var7, int var8, double var9) {
         int var11 = var4 * var5 + var3;
         if (!var1[var11] && dist(var0[var11], var6, var7, var8) < var9) {
            var1[var11] = true;
            var2.add(var11);
         }
      }

      private static double dist(int var0, int var1, int var2, int var3) {
         int var4 = (var0 >> 16 & 0xFF) - var1;
         int var5 = (var0 >> 8 & 0xFF) - var2;
         int var6 = (var0 & 0xFF) - var3;
         return Math.sqrt(var4 * var4 + var5 * var5 + var6 * var6);
      }

      private static double dist(int var0, int var1) {
         return dist(var0, var1 >> 16 & 0xFF, var1 >> 8 & 0xFF, var1 & 0xFF);
      }

      private static int clamp(float var0) {
         return Math.max(0, Math.min(255, Math.round(var0)));
      }

      private static float[] blur(float[] var0, int var1, int var2, int var3) {
         float[] var4 = new float[var1 * var2];
         float[] var5 = new float[var1 * var2];

         for (int var6 = 0; var6 < var2; var6++) {
            float var7 = 0.0F;

            for (int var8 = -var3; var8 <= var3; var8++) {
               var7 += var0[var6 * var1 + Math.max(0, Math.min(var1 - 1, var8))];
            }

            for (int var11 = 0; var11 < var1; var11++) {
               var4[var6 * var1 + var11] = var7 / (2 * var3 + 1);
               var7 += var0[var6 * var1 + Math.min(var1 - 1, var11 + var3 + 1)] - var0[var6 * var1 + Math.max(0, var11 - var3)];
            }
         }

         for (int var9 = 0; var9 < var1; var9++) {
            float var10 = 0.0F;

            for (int var12 = -var3; var12 <= var3; var12++) {
               var10 += var4[Math.max(0, Math.min(var2 - 1, var12)) * var1 + var9];
            }

            for (int var13 = 0; var13 < var2; var13++) {
               var5[var13 * var1 + var9] = var10 / (2 * var3 + 1);
               var10 += var4[Math.min(var2 - 1, var13 + var3 + 1) * var1 + var9] - var4[Math.max(0, var13 - var3) * var1 + var9];
            }
         }

         return var5;
      }

      public void render() {
         this.ensure();
         if (this.tex != null) {
            double[] var1 = CustomSky.dayWeights(Mc.timeOfDay());

            double var2 = switch (this.when.index) {
               case 1 -> var1[0];
               case 2 -> var1[1];
               case 3 -> var1[2];
               default -> 1.0;
            };
            if (!(var2 < 0.01)) {
               double var4 = 256.0;

               try {
                  var4 = Mc.mc().gameRenderer.getDepthFar();
               } catch (Throwable var31) {
               }

               double var6 = R3.skyRadius > 0.0 ? R3.skyRadius * 0.85 : Math.max(60.0, var4 * 0.42);
               double var8 = System.currentTimeMillis() / 1000.0;
               double var10 = Math.toRadians(this.elevation.get() + (this.bob.get() ? Math.sin(var8 * 0.35) * 1.2 : 0.0));
               double var12 = Math.toRadians(this.azimuth.get());
               double[] var14 = new double[]{Math.sin(var12) * Math.cos(var10), Math.sin(var10), -Math.cos(var12) * Math.cos(var10)};
               double[] var15 = new double[]{Math.cos(var12), 0.0, Math.sin(var12)};
               double[] var16 = new double[]{
                  var14[1] * var15[2] - var14[2] * var15[1], var14[2] * var15[0] - var14[0] * var15[2], var14[0] * var15[1] - var14[1] * var15[0]
               };
               if (var16[1] < 0.0) {
                  var16[0] = -var16[0];
                  var16[1] = -var16[1];
                  var16[2] = -var16[2];
               }

               double var17 = var6 * Math.tan(Math.toRadians(this.size.get()) / 2.0);
               double var19 = var17 * this.tw / this.th;
               double var21 = R3.camX() + var14[0] * var6;
               double var23 = R3.camY() + var14[1] * var6;
               double var25 = R3.camZ() + var14[2] * var6;
               double var27 = this.opacity.get() / 100.0 * var2;
               int var29;
               int var30;
               switch (this.look.index) {
                  case 1:
                     var29 = R3.argb(16777215, var27);
                     var30 = R3.argb(16773590, var27 * 0.45);
                     break;
                  case 2:
                     var29 = R3.argb(12572927, var27 * 0.55);
                     var30 = R3.argb(10274559, var27 * 0.55);
                     break;
                  case 3:
                     var29 = R3.argb(16762784, var27 * 0.9);
                     var30 = R3.argb(16747082, var27 * 0.5);
                     break;
                  default:
                     var29 = R3.argb(16777215, var27);
                     var30 = 0;
               }

               if (var30 != 0) {
                  quad(R3.skyLayer(this.glowTex), var21, var23, var25, var15, var16, var19 * 1.12, var17 * 1.12, var30);
               }

               quad(R3.skyLayer(this.tex), var21, var23, var25, var15, var16, var19, var17, var29);
            }
         }
      }

      private static void quad(RenderType var0, double var1, double var3, double var5, double[] var7, double[] var8, double var9, double var11, int var13) {
         double[] var14 = new double[]{
            var1 - var7[0] * var9 + var8[0] * var11, var3 - var7[1] * var9 + var8[1] * var11, var5 - var7[2] * var9 + var8[2] * var11
         };
         double[] var15 = new double[]{
            var1 + var7[0] * var9 + var8[0] * var11, var3 + var7[1] * var9 + var8[1] * var11, var5 + var7[2] * var9 + var8[2] * var11
         };
         double[] var16 = new double[]{
            var1 + var7[0] * var9 - var8[0] * var11, var3 + var7[1] * var9 - var8[1] * var11, var5 + var7[2] * var9 - var8[2] * var11
         };
         double[] var17 = new double[]{
            var1 - var7[0] * var9 - var8[0] * var11, var3 - var7[1] * var9 - var8[1] * var11, var5 - var7[2] * var9 - var8[2] * var11
         };
         R3.skyQuad(var0, var14, var15, var16, var17, new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F}, var13);
      }
   }
}
