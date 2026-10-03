package dev.lego.visual.sky;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Setting;
import dev.lego.ui.Toasts;
import dev.lego.ui.Tx;
import dev.lego.visual.R3;
import dev.spotifyhud.Anim;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class CustomSky extends Module {
   private static final String[] MODES = new String[SkyGen.NAMES.length + 3];
   private static final int[] RES = new int[]{768, 1024, 1536};
   public final Setting.Mode mode = this.add(new Setting.Mode("Himmel", 0, MODES));
   public final Setting.Mode quality = this.add(new Setting.Mode("Qualität", 1, "Normal", "HD", "Ultra 4K"));
   public final Setting.Mode daySky = this.add(new Setting.Mode("Auto: Tag-Himmel", idx("anime"), SkyGen.NAMES));
   public final Setting.Mode duskSky = this.add(new Setting.Mode("Auto: Abend-Himmel", idx("goldenhour"), SkyGen.NAMES));
   public final Setting.Mode nightSky = this.add(new Setting.Mode("Auto: Nacht-Himmel", idx("milkyway"), SkyGen.NAMES));
   public final Setting.Bool fog = this.add(new Setting.Bool("Nebel an Himmel anpassen", true));
   public final Setting.Num speed = this.add(new Setting.Num("Bewegung", 0.0, 4.0, 0.1, 1.0, "x"));
   public final Setting.Num brightness = this.add(new Setting.Num("Helligkeit", 0.2, 1.0, 0.05, 0.9, ""));
   public final Setting.Bool shooting = this.add(new Setting.Bool("Sternschnuppen", true));
   public final Setting.Bool nightOnly = this.add(new Setting.Bool("Nur nachts einblenden", false));
   public final Setting.Bool overworldOnly = this.add(new Setting.Bool("Nur Oberwelt", true));
   private final AtomicReference<int[]> pending = new AtomicReference<>();
   private volatile String generating = null;
   private String loadedKey = null;
   private String pendingKey = null;
   private int pendingW;
   private int pendingH;
   private Identifier tex = null;
   private boolean equirect = false;
   private int imageIndex = 0;
   private final LinkedHashMap<String, Identifier> loaded = new LinkedHashMap<>();
   private final Map<String, Boolean> equi = new HashMap<>();
   private static final Set<Identifier> LINEAR = new HashSet<>();

   private static int idx(String var0) {
      for (int var1 = 0; var1 < SkyGen.IDS.length; var1++) {
         if (SkyGen.IDS[var1].equals(var0)) {
            return var1;
         }
      }

      return 0;
   }

   public CustomSky() {
      super("sky", "Custom Sky", "Realistische HD-Himmel, Sky-Resourcepacks und eigene Bilder", Category.VISUALS);
      this.icon("cloud");
      this.fresh();
      this.add(new Setting.Action("Sky-Packs", "Neu laden", () -> {
         SkyPacks.invalidate();
         SkyPacks.layers();
         Toasts.show("cloud", "Custom Sky", SkyPacks.info());
      }));
      this.add(new Setting.Action("Eigene Bilder", "Nächstes Bild", () -> {
         this.imageIndex++;
         this.loadedKey = null;
      }));
      this.add(new Setting.Action("Bilder-Ordner", "Pfad kopieren", () -> {
         Path var0 = folder();
         Mc.clipboard(var0.toString());
         Toasts.show("copy", "Ordner kopiert", var0.toString());
      }));
   }

   public static Path folder() {
      Path var0 = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skies");

      try {
         Files.createDirectories(var0);
      } catch (Throwable var2) {
      }

      return var0;
   }

   private boolean builtIn() {
      return this.mode.index < SkyGen.IDS.length;
   }

   private boolean packs() {
      return this.mode.index == SkyGen.IDS.length;
   }

   private boolean auto() {
      return this.mode.index == SkyGen.IDS.length + 2;
   }

   @Override
   public void onDisable() {
      this.free();
   }

   private void free() {
      for (Identifier var2 : this.loaded.values()) {
         Tx.destroy(var2);
      }

      this.loaded.clear();
      this.tex = null;
      this.loadedKey = null;
   }

   private static Path cacheDir() {
      Path var0 = FabricLoader.getInstance().getConfigDir().resolve("legoclient").resolve("skycache");

      try {
         Files.createDirectories(var0);
      } catch (Throwable var2) {
      }

      return var0;
   }

   private Identifier texture(String var1) {
      Identifier var2 = this.loaded.get(var1);
      if (var2 != null) {
         return var2;
      } else {
         int[] var3 = this.pending.get();
         if (var3 != null && var1.equals(this.pendingKey)) {
            this.pending.set(null);
            var2 = Identifier.fromNamespaceAndPath("legoclient", "sky/" + var1.replace('@', '_').replace('.', '_').toLowerCase(Locale.ROOT));
            Tx.register(var2, var3, this.pendingW, this.pendingH, true);
            this.loaded.put(var1, var2);
            this.equi.put(var1, this.equirect);

            while (this.loaded.size() > 3) {
               String var4 = this.loaded.keySet().iterator().next();
               Tx.destroy(this.loaded.remove(var4));
            }

            return var2;
         } else if (this.generating != null) {
            return null;
         } else {
            this.generating = var1;
            Thread var5 = new Thread(() -> {
               try {
                  if (var1.startsWith("img@")) {
                     this.loadImage(var1);
                  } else {
                     String var2x = var1.substring(0, var1.indexOf(64));
                     int var3x = Integer.parseInt(var1.substring(var1.indexOf(64) + 1));
                     File var4x = cacheDir().resolve(var2x + "_" + var3x + "_v3.png").toFile();
                     int[] var5x = null;
                     if (var4x.isFile()) {
                        try {
                           BufferedImage var6x = ImageIO.read(var4x);
                           if (var6x != null && var6x.getWidth() == var3x * 3 && var6x.getHeight() == var3x * 2) {
                              var5x = var6x.getRGB(0, 0, var3x * 3, var3x * 2, null, 0, var3x * 3);
                           }
                        } catch (Throwable var13) {
                        }
                     }

                     if (var5x == null) {
                        var5x = SkyGen.generate(var2x, var3x);

                        try {
                           BufferedImage var16 = new BufferedImage(var3x * 3, var3x * 2, 1);
                           var16.setRGB(0, 0, var3x * 3, var3x * 2, var5x, 0, var3x * 3);
                           ImageIO.write(var16, "png", var4x);
                        } catch (Throwable var12) {
                        }
                     }

                     for (int var17 = 0; var17 < var5x.length; var17++) {
                        var5x[var17] |= -16777216;
                     }

                     this.pendingW = var3x * 3;
                     this.pendingH = var3x * 2;
                     this.equirect = false;
                     this.pendingKey = var1;
                     this.pending.set(var5x);
                  }
               } catch (Throwable var14) {
                  LegoClient.LOG("Sky-Generierung: " + var14);
               } finally {
                  this.generating = null;
               }
            }, "LegoClient-Sky");
            var5.setDaemon(true);
            var5.setPriority(1);
            var5.start();
            return null;
         }
      }
   }

   private String presetKey(int var1) {
      return SkyGen.IDS[Math.max(0, Math.min(SkyGen.IDS.length - 1, var1))] + "@" + RES[this.quality.index];
   }

   private List<Object[]> plan(long var1) {
      ArrayList var3 = new ArrayList();
      if (this.auto()) {
         double[] var4 = dayWeights(var1);
         int[] var5 = new int[]{this.daySky.index, this.duskSky.index, this.nightSky.index};

         for (int var6 = 0; var6 < 3; var6++) {
            if (var4[var6] > 0.001) {
               var3.add(new Object[]{this.presetKey(var5[var6]), var4[var6], SkyGen.IDS[var5[var6]]});
            }
         }

         var3.sort((var0, var1x) -> Double.compare((Double)var1x[1], (Double)var0[1]));
      } else if (this.builtIn()) {
         var3.add(new Object[]{this.presetKey(this.mode.index), 1.0, SkyGen.IDS[this.mode.index]});
      } else {
         var3.add(new Object[]{"img@" + this.imageIndex, 1.0, null});
      }

      return var3;
   }

   static double[] dayWeights(long var0) {
      var0 = Math.floorMod(var0, 24000L);
      double var2 = Math.max(bump(var0, 12600.0, 1400.0), bump(var0, 23300.0, 1200.0) + bump(var0 - 24000L, -700.0, 1200.0));
      double var4 = var0 >= 13600L && var0 <= 22400L
         ? 1.0
         : (var0 > 12600L && var0 < 13600L ? (var0 - 12600L) / 1000.0 : (var0 > 22400L && var0 < 23400L ? 1.0 - (var0 - 22400L) / 1000.0 : 0.0));
      double var6 = Math.max(0.0, 1.0 - var4 - var2);
      var4 = Math.max(0.0, var4 - var2 * 0.5);
      double var8 = var6 + var2 + var4;
      return new double[]{var6 / var8, var2 / var8, var4 / var8};
   }

   private static double bump(double var0, double var2, double var4) {
      double var6 = Math.abs(var0 - var2) / var4;
      return var6 >= 1.0 ? 0.0 : 0.5 + 0.5 * Math.cos(var6 * Math.PI);
   }

   public int fogTint() {
      if (this.enabled && this.fog.get() && !this.packs() && this.inOverworld()) {
         List var1 = this.plan(Mc.timeOfDay());
         double var2 = 0.0;
         double var4 = 0.0;
         double var6 = 0.0;
         double var8 = 0.0;

         for (Object[] var11 : var1) {
            if (var11[2] != null && this.loaded.containsKey((String)var11[0])) {
               int var12 = SkyGen.fogColor((String)var11[2]);
               double var13 = (Double)var11[1];
               var2 += (var12 >> 16 & 0xFF) * var13;
               var4 += (var12 >> 8 & 0xFF) * var13;
               var6 += (var12 & 0xFF) * var13;
               var8 += var13;
            }
         }

         return var8 <= 0.0 ? 0 : 0xFF000000 | (int)(var2 / var8) << 16 | (int)(var4 / var8) << 8 | (int)(var6 / var8);
      } else {
         return 0;
      }
   }

   public boolean replacesVanilla() {
      if (this.enabled && this.inOverworld()) {
         if (this.packs()) {
            return !SkyPacks.layers().isEmpty();
         } else {
            for (Object[] var2 : this.plan(Mc.timeOfDay())) {
               if (this.loaded.containsKey((String)var2[0])) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean inOverworld() {
      if (!this.overworldOnly.get()) {
         return true;
      } else {
         try {
            return Mc.world().dimension().identifier().getPath().equals("overworld");
         } catch (Throwable var2) {
            return true;
         }
      }
   }

   private void loadImage(String var1) throws Exception {
      ArrayList var2 = new ArrayList();
      File[] var3 = folder().toFile().listFiles();
      if (var3 != null) {
         for (File var7 : var3) {
            String var8 = var7.getName().toLowerCase(Locale.ROOT);
            if (var8.endsWith(".png") || var8.endsWith(".jpg") || var8.endsWith(".jpeg")) {
               var2.add(var7);
            }
         }
      }

      var2.sort(Comparator.comparing(File::getName));
      if (var2.isEmpty()) {
         Toasts.show("image", "Keine Bilder", "Leg Himmel-Bilder in " + folder());
         this.pendingW = 1;
         this.pendingH = 1;
         this.equirect = false;
         this.pendingKey = var1;
         this.pending.set(new int[]{0});
      } else {
         File var11 = (File)var2.get(Math.floorMod(this.imageIndex, var2.size()));
         BufferedImage var12 = ImageIO.read(var11);
         int var13 = var12.getWidth();
         int var14 = var12.getHeight();
         if (var13 > 8192) {
            int var15 = var14 * 8192 / var13;
            BufferedImage var9 = new BufferedImage(8192, var15, 2);
            Graphics2D var10 = var9.createGraphics();
            var10.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            var10.drawImage(var12, 0, 0, 8192, var15, null);
            var10.dispose();
            var12 = var9;
            var13 = 8192;
            var14 = var15;
         }

         this.equirect = Math.abs((double)var13 / var14 - 2.0) < 0.2;
         this.pendingW = var13;
         this.pendingH = var14;
         this.pendingKey = var1;
         this.pending.set(var12.getRGB(0, 0, var13, var14, null, 0, var13));
         Toasts.show("image", "Himmel geladen", var11.getName());
      }
   }

   public void render() {
      if (this.inOverworld()) {
         double var1 = 256.0;

         try {
            var1 = Mc.mc().gameRenderer.getDepthFar();
         } catch (Throwable var35) {
         }

         double var3 = Mc.mc().options.getEffectiveRenderDistance() * 16.0;
         double var5 = R3.skyRadius > 0.0 ? R3.skyRadius : Math.max(96.0, Math.min(var1 * 0.8, Math.max(var3 * 1.8, 140.0)));
         long var7 = Mc.timeOfDay() % 24000L;
         double var9 = 1.0;
         if (this.nightOnly.get()) {
            var9 = nightFactor(var7);
         }

         if (!(var9 <= 0.001)) {
            double var11 = this.brightness.get();
            if (this.packs()) {
               for (SkyPacks.Layer var14 : SkyPacks.layers()) {
                  double var37 = var14.alpha(var7) * var9;
                  if (!(var37 <= 0.003)) {
                     double var38 = var14.rotate ? celestial(var7) * 360.0 * var14.speed : 0.0;
                     int var39 = argb(var11 * (var14.add ? 0.9 : 1.0), var37 * (var14.add ? 0.8 : 1.0));
                     if (var14.strip != null) {
                        linearOnce(var14.strip);
                        drawCube(R3.skyLayer(var14.strip), null, var5, var14.axis, var38, var39);
                     } else if (var14.faces != null) {
                        RenderType[] var40 = new RenderType[6];

                        for (int var41 = 0; var41 < 6; var41++) {
                           if (var14.faces[var41] != null) {
                              linearOnce(var14.faces[var41]);
                              var40[var41] = R3.skyLayer(var14.faces[var41]);
                           }
                        }

                        drawCube(null, var40, var5, var14.axis, var38, var39);
                     }
                  }
               }
            } else {
               double var13 = System.currentTimeMillis() / 1000.0;
               double var15 = var13 * 0.6 * this.speed.get();
               boolean var17 = false;
               boolean var18 = false;

               for (Object[] var20 : this.plan(var7)) {
                  String var21 = (String)var20[0];
                  double var22 = (Double)var20[1];
                  String var24 = (String)var20[2];
                  Identifier var25 = this.texture(var21);
                  if (var25 != null) {
                     boolean var26 = var24 != null && (var24.equals("storm") || var24.equals("thunder"));
                     double var27 = var26 ? lightning(var13) : 0.0;
                     var17 |= var27 > 0.01;
                     String var29 = var24 == null ? "" : SkyGen.kind(var24);
                     var18 |= var22 > 0.4 && (var29.equals("night") || var29.equals("space"));
                     int var30 = argb(Math.min(1.0, var11 + var27 * 0.4), var9 * var22);
                     RenderType var31 = R3.skyLayer(var25);
                     if (Boolean.TRUE.equals(this.equi.get(var21))) {
                        drawSphere(var31, var5, var15, var30);
                     } else {
                        drawCube(var31, null, var5, new double[]{0.0, 1.0, 0.0}, var15, var30);
                     }

                     if (var27 > 0.01) {
                        int var32 = R3.argb(14542591, var27 * 0.35 * var9 * var22);
                        double var33 = var5 * 0.5;
                        R3.box(R3.camX() - var33, R3.camY() - var33 * 0.2, R3.camZ() - var33, R3.camX() + var33, R3.camY() + var33, R3.camZ() + var33, var32);
                     }
                  }
               }

               if (this.shooting.get() && var18) {
                  shootingStars(var5 * 0.97, var9);
               }
            }
         }
      }
   }

   private static void linearOnce(Identifier var0) {
      if (LINEAR.add(var0)) {
         try {
            Tx.linear(Tx.tm().getTexture(var0));
         } catch (Throwable var2) {
         }
      }
   }

   private static double celestial(long var0) {
      double var2 = var0 / 24000.0 - 0.25;
      var2 -= Math.floor(var2);
      double var4 = 0.5 - Math.cos(var2 * Math.PI) / 2.0;
      return (var2 * 2.0 + var4) / 3.0;
   }

   private static double nightFactor(long var0) {
      if (var0 >= 13500L && var0 <= 22500L) {
         return 1.0;
      } else if (var0 > 12000L && var0 < 13500L) {
         return (var0 - 12000L) / 1500.0;
      } else {
         return var0 > 22500L && var0 < 24000L ? 1.0 - (var0 - 22500L) / 1500.0 : 0.0;
      }
   }

   private static double lightning(double var0) {
      int var2 = (int)Math.floor(var0 / 7.0);
      double var3 = var0 - var2 * 7.0;
      if (Anim.rnd(var2, 77) > 0.55) {
         return 0.0;
      } else {
         double var5 = Anim.rnd(var2, 78) * 5.0;
         double var7 = var3 - var5;
         return !(var7 < 0.0) && !(var7 > 0.5) ? Math.max(0.0, Math.sin(var7 / 0.5 * Math.PI)) * (0.6 + 0.4 * Math.sin(var7 * 60.0)) : 0.0;
      }
   }

   private static int argb(double var0, double var2) {
      int var4 = (int)Math.max(0L, Math.min(255L, Math.round(var0 * 255.0)));
      int var5 = (int)Math.max(0L, Math.min(255L, Math.round(var2 * 255.0)));
      return var5 << 24 | var4 << 16 | var4 << 8 | var4;
   }

   private static double[] rot(double[] var0, double[] var1, double var2) {
      if (var2 == 0.0) {
         return var0;
      } else {
         double var4 = Math.sqrt(var1[0] * var1[0] + var1[1] * var1[1] + var1[2] * var1[2]);
         if (var4 < 1.0E-9) {
            return var0;
         } else {
            double var6 = var1[0] / var4;
            double var8 = var1[1] / var4;
            double var10 = var1[2] / var4;
            double var12 = Math.toRadians(var2);
            double var14 = Math.cos(var12);
            double var16 = Math.sin(var12);
            double var18 = var6 * var0[0] + var8 * var0[1] + var10 * var0[2];
            double var20 = var8 * var0[2] - var10 * var0[1];
            double var22 = var10 * var0[0] - var6 * var0[2];
            double var24 = var6 * var0[1] - var8 * var0[0];
            return new double[]{
               var0[0] * var14 + var20 * var16 + var6 * var18 * (1.0 - var14),
               var0[1] * var14 + var22 * var16 + var8 * var18 * (1.0 - var14),
               var0[2] * var14 + var24 * var16 + var10 * var18 * (1.0 - var14)
            };
         }
      }
   }

   private static void drawCube(RenderType var0, RenderType[] var1, double var2, double[] var4, double var5, int var7) {
      double[] var8 = new double[3];
      double var9 = R3.camX();
      double var11 = R3.camY();
      double var13 = R3.camZ();

      for (int var15 = 0; var15 < 6; var15++) {
         RenderType var16 = var0 != null ? var0 : (var1 == null ? null : var1[var15]);
         if (var16 != null) {
            int var17 = var15 % 3;
            int var18 = var15 / 3;
            byte var19 = 4;

            for (int var20 = 0; var20 < var19; var20++) {
               for (int var21 = 0; var21 < var19; var21++) {
                  double var22 = (double)var20 / var19;
                  double var24 = (double)(var20 + 1) / var19;
                  double var26 = (double)var21 / var19;
                  double var28 = (double)(var21 + 1) / var19;
                  double[][] var30 = new double[4][];
                  double[][] var31 = new double[][]{{var22, var26}, {var24, var26}, {var24, var28}, {var22, var28}};
                  float[] var32 = new float[8];

                  for (int var33 = 0; var33 < 4; var33++) {
                     SkyGen.dir(var15, var31[var33][0], var31[var33][1], var8);
                     double var34 = Math.max(Math.abs(var8[0]), Math.max(Math.abs(var8[1]), Math.abs(var8[2])));
                     double[] var36 = rot(new double[]{var8[0] / var34, var8[1] / var34, var8[2] / var34}, var4, var5);
                     var30[var33] = new double[]{var9 + var36[0] * var2 * 0.577, var11 + var36[1] * var2 * 0.577, var13 + var36[2] * var2 * 0.577};
                     if (var0 != null) {
                        double var37 = 2.4414062E-4F;
                        double var39 = Math.max(var37, Math.min(1.0 - var37, var31[var33][0]));
                        double var41 = Math.max(var37, Math.min(1.0 - var37, var31[var33][1]));
                        var32[var33 * 2] = (float)((var17 + var39) / 3.0);
                        var32[var33 * 2 + 1] = (float)((var18 + var41) / 2.0);
                     } else {
                        var32[var33 * 2] = (float)var31[var33][0];
                        var32[var33 * 2 + 1] = (float)var31[var33][1];
                     }
                  }

                  R3.skyQuad(var16, var30[0], var30[1], var30[2], var30[3], var32, var7);
               }
            }
         }
      }
   }

   private static void drawSphere(RenderType var0, double var1, double var3, int var5) {
      byte var6 = 48;
      byte var7 = 24;
      double var8 = R3.camX();
      double var10 = R3.camY();
      double var12 = R3.camZ();
      double var14 = var1 * 0.9;

      for (int var16 = 0; var16 < var6; var16++) {
         for (int var17 = 0; var17 < var7; var17++) {
            double var18 = (double)var16 / var6;
            double var20 = (double)(var16 + 1) / var6;
            double var22 = (double)var17 / var7;
            double var24 = (double)(var17 + 1) / var7;
            double[][] var26 = new double[][]{sph(var18, var22, var3), sph(var20, var22, var3), sph(var20, var24, var3), sph(var18, var24, var3)};

            for (double[] var30 : var26) {
               var30[0] = var8 + var30[0] * var14;
               var30[1] = var10 + var30[1] * var14;
               var30[2] = var12 + var30[2] * var14;
            }

            R3.skyQuad(
               var0,
               var26[0],
               var26[1],
               var26[2],
               var26[3],
               new float[]{(float)var18, (float)var22, (float)var20, (float)var22, (float)var20, (float)var24, (float)var18, (float)var24},
               var5
            );
         }
      }
   }

   private static double[] sph(double var0, double var2, double var4) {
      double var6 = var0 * Math.PI * 2.0 + Math.toRadians(var4);
      double var8 = (Math.PI / 2) - var2 * Math.PI;
      return new double[]{Math.cos(var8) * Math.sin(var6), Math.sin(var8), -Math.cos(var8) * Math.cos(var6)};
   }

   private static void shootingStars(double var0, double var2) {
      double var4 = R3.camX();
      double var6 = R3.camY();
      double var8 = R3.camZ();

      for (int var10 = 0; var10 < 2; var10++) {
         double var11 = var10 == 0 ? 3.1 : 4.7;
         double var13 = 1.1;
         double var15 = System.currentTimeMillis() / 1000.0 + var10 * 1.7;
         int var17 = (int)Math.floor(var15 / var11);
         double var18 = var15 - var17 * var11;
         if (!(var18 > var13) && !(Anim.rnd(var17, 90 + var10) > 0.7)) {
            double var20 = var18 / var13;
            double var22 = Anim.rnd(var17, 91 + var10) * Math.PI * 2.0;
            double var24 = 0.35 + Anim.rnd(var17, 92 + var10) * 0.45;
            double var26 = 0.12;
            double var28 = var20 < 0.2 ? var20 / 0.2 : (1.0 - var20) / 0.8;
            double var30 = var24 - var20 * 0.25;
            double var32 = var22 + var20 * 0.35;
            double[] var34 = dir(Math.asin(var30), var32);
            double[] var35 = dir(Math.asin(Math.min(1.0, var30 + var26 * 0.6)), var32 - var26);
            R3.line(
               var4 + var35[0] * var0,
               var6 + var35[1] * var0,
               var8 + var35[2] * var0,
               var4 + var34[0] * var0,
               var6 + var34[1] * var0,
               var8 + var34[2] * var0,
               var0 * 0.004,
               R3.argb(11585791, 0.0),
               R3.argb(16777215, 0.95 * var28 * var2)
            );
         }
      }
   }

   private static double[] dir(double var0, double var2) {
      double var4 = Math.cos(var0);
      return new double[]{Math.cos(var2) * var4, Math.sin(var0), Math.sin(var2) * var4};
   }

   static {
      System.arraycopy(SkyGen.NAMES, 0, MODES, 0, SkyGen.NAMES.length);
      MODES[SkyGen.NAMES.length] = "Resourcepack";
      MODES[SkyGen.NAMES.length + 1] = "Eigenes Bild";
      MODES[SkyGen.NAMES.length + 2] = "Automatisch (Tageszeit)";
   }
}
