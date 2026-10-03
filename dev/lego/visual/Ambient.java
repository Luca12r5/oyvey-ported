package dev.lego.visual;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Tx;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class Ambient {
   public static Ambient.Weather weather;
   public static Ambient.Fireflies fireflies;
   public static Ambient.Lanterns lanterns;
   public static Ambient.Motes motes;
   public static Ambient.Bubbles bubbles;
   public static Ambient.Butterflies butterflies;
   public static Ambient.Rainbow rainbow;
   public static Ambient.Aurora aurora;
   private static long last = 0L;
   private static final Map<String, Identifier> TEX = new HashMap<>();
   static final Random RND = new Random();

   private Ambient() {
   }

   public static void registerAll() {
      weather = Modules.register(new Ambient.Weather());
      fireflies = Modules.register(new Ambient.Fireflies());
      lanterns = Modules.register(new Ambient.Lanterns());
      motes = Modules.register(new Ambient.Motes());
      bubbles = Modules.register(new Ambient.Bubbles());
      butterflies = Modules.register(new Ambient.Butterflies());
      rainbow = Modules.register(new Ambient.Rainbow());
      aurora = Modules.register(new Ambient.Aurora());
   }

   static void render() {
      long var0 = System.nanoTime();
      float var2 = last == 0L ? 0.016F : Math.min(0.1F, (float)(var0 - last) / 1.0E9F);
      last = var0;
      if (aurora.enabled) {
         aurora.render();
      }

      if (rainbow.enabled) {
         rainbow.render();
      }

      if (lanterns.enabled) {
         lanterns.render(var2);
      }

      if (weather.enabled) {
         weather.render(var2);
      }

      if (motes.enabled) {
         motes.render(var2);
      }

      if (bubbles.enabled) {
         bubbles.render(var2);
      }

      if (butterflies.enabled) {
         butterflies.render(var2);
      }

      if (fireflies.enabled) {
         fireflies.render(var2);
      }
   }

   static Identifier tex(String var0) {
      Identifier var1 = TEX.get(var0);
      if (var1 == null) {
         var1 = Identifier.fromNamespaceAndPath("legoclient", "ambient/" + var0);

         try {
            Tx.register(var1, AmbientTex.paint(var0), 128, 128, true);
         } catch (Throwable var3) {
            LegoClient.LOG("Ambient: " + var3);
         }

         TEX.put(var0, var1);
      }

      return var1;
   }

   static int top(double var0, double var2) {
      ClientLevel var4 = Mc.world();
      if (var4 == null) {
         return -64;
      } else {
         try {
            return var4.getHeight(Types.MOTION_BLOCKING, (int)Math.floor(var0), (int)Math.floor(var2));
         } catch (Throwable var6) {
            return -64;
         }
      }
   }

   static boolean air(double var0, double var2, double var4) {
      ClientLevel var6 = Mc.world();
      if (var6 == null) {
         return true;
      } else {
         try {
            return var6.getBlockState(BlockPos.containing(var0, var2, var4)).getCollisionShape(var6, BlockPos.containing(var0, var2, var4)).isEmpty();
         } catch (Throwable var8) {
            return true;
         }
      }
   }

   static double night() {
      long var0 = Math.floorMod(Mc.timeOfDay(), 24000L);
      if (var0 >= 13200L && var0 <= 22800L) {
         return 1.0;
      } else if (var0 > 12200L && var0 < 13200L) {
         return (var0 - 12200L) / 1000.0;
      } else {
         return var0 > 22800L && var0 < 23800L ? 1.0 - (var0 - 22800L) / 1000.0 : 0.0;
      }
   }

   public static final class Aurora extends Module {
      final Setting.Mode palette = this.add(new Setting.Mode("Farben", 0, "Grün", "Lila & Pink", "Regenbogen"));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 60.0, "%"));

      Aurora() {
         super("aurora", "Nordlicht", "Wehende Polarlicht-Vorhänge am Nachthimmel (echt 3D)", Category.VISUALS);
         this.icon("sky");
         this.fresh();
      }

      void render() {
         double var1 = Ambient.night() * this.strength.get() / 100.0;
         if (!(var1 < 0.01)) {
            double var3 = System.currentTimeMillis() / 1000.0;
            double var5 = R3.camX();
            double var7 = R3.camY();
            double var9 = R3.camZ();
            double var11 = 160.0;

            try {
               var11 = Math.min(var11, Mc.mc().gameRenderer.getDepthFar() * 0.45);
            } catch (Throwable var50) {
            }

            for (int var13 = 0; var13 < 3; var13++) {
               double var14 = -var11 * (0.55 + var13 * 0.12);
               double var16 = var7 + var11 * (0.32 + var13 * 0.05);
               double var18 = var11 * (0.22 + var13 * 0.04);
               byte var20 = 64;

               for (int var21 = 0; var21 < var20; var21++) {
                  double var22 = (double)var21 / var20;
                  double var24 = (double)(var21 + 1) / var20;
                  double var26 = (var22 - 0.5) * var11 * 2.4;
                  double var28 = (var24 - 0.5) * var11 * 2.4;
                  double var30 = Math.sin(var22 * 9.0 + var3 * 0.35 + var13) * var11 * 0.08 + Math.sin(var22 * 23.0 - var3 * 0.6) * var11 * 0.02;
                  double var32 = Math.sin(var24 * 9.0 + var3 * 0.35 + var13) * var11 * 0.08 + Math.sin(var24 * 23.0 - var3 * 0.6) * var11 * 0.02;
                  double var34 = 0.55 + 0.45 * Math.sin(var22 * 14.0 + var3 * 0.9 + var13 * 2);
                  double var36 = 0.55 + 0.45 * Math.sin(var24 * 14.0 + var3 * 0.9 + var13 * 2);
                  double var38 = Math.sin(var22 * Math.PI);
                  double var40 = Math.sin(var24 * Math.PI);
                  int var42 = this.col(var22, var13, true);
                  int var43 = this.col(var22, var13, false);
                  int var44 = this.col(var24, var13, true);
                  int var45 = this.col(var24, var13, false);
                  double var46 = var1 * var34 * var38 * 0.55;
                  double var48 = var1 * var36 * var40 * 0.55;
                  R3.quad(
                     var5 + var26,
                     var16,
                     var9 + var14 + var30,
                     var5 + var28,
                     var16,
                     var9 + var14 + var32,
                     var5 + var28,
                     var16 + var18,
                     var9 + var14 + var32 * 1.3,
                     var5 + var26,
                     var16 + var18,
                     var9 + var14 + var30 * 1.3,
                     R3.argb(var43, var46),
                     R3.argb(var45, var48),
                     R3.argb(var44, 0.0),
                     R3.argb(var42, 0.0)
                  );
               }
            }
         }
      }

      private int col(double var1, int var3, boolean var4) {
         switch (this.palette.index) {
            case 1:
               return var4 ? 16732120 : 10181631;
            case 2:
               return Color.HSBtoRGB((float)(var1 * 1.5 + var3 * 0.2 + System.currentTimeMillis() / 20000.0), 0.7F, 1.0F) & 16777215;
            default:
               return var4 ? 9067519 : 4063136;
         }
      }
   }

   public static final class Bubbles extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 5.0, 100.0, 5.0, 30.0, ""));
      private final Ambient.Pool pool = new Ambient.Pool(100);

      Bubbles() {
         super("bubbles", "Seifenblasen", "Schillernde Seifenblasen schweben um dich", Category.VISUALS);
         this.icon("aura");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = Mc.x(var2);
            double var5 = Mc.y(var2);
            double var7 = Mc.z(var2);

            while (this.pool.n < this.count.getI()) {
               double[] var9 = this.pool.add();
               var9[0] = var3 + (Ambient.RND.nextDouble() - 0.5) * 14.0;
               var9[1] = var5 + Ambient.RND.nextDouble() * 2.0;
               var9[2] = var7 + (Ambient.RND.nextDouble() - 0.5) * 14.0;
               var9[6] = 0.0;
               var9[7] = 6.0 + Ambient.RND.nextDouble() * 8.0;
               var9[8] = Ambient.RND.nextDouble() * 1000.0;
               var9[9] = 0.5 + Ambient.RND.nextDouble();
            }

            double var18 = System.currentTimeMillis() / 1000.0;
            Identifier var11 = Ambient.tex("bubble");

            for (int var12 = this.pool.n - 1; var12 >= 0; var12--) {
               double[] var13 = this.pool.p[var12];
               var13[6] += var1;
               var13[1] += 0.35 * var1;
               var13[0] += Math.sin(var18 * 0.8 + var13[8]) * 0.3 * var1;
               var13[2] += Math.cos(var18 * 0.7 + var13[8]) * 0.3 * var1;
               if (!(var13[6] > var13[7]) && var12 < this.count.getI()) {
                  double var14 = Math.min(1.0, Math.min(var13[6] / 0.8, (var13[7] - var13[6]) / 0.3));
                  double var16 = 1.0 + Math.sin(var18 * 5.0 + var13[8]) * 0.04;
                  R3.billboard(var11, var13[0], var13[1], var13[2], 0.45 * var13[9] * var16, R3.argb(16777215, var14));
               } else {
                  this.pool.remove(var12);
               }
            }
         }
      }
   }

   public static final class Butterflies extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 2.0, 40.0, 1.0, 10.0, ""));
      private final Ambient.Pool pool = new Ambient.Pool(40);

      Butterflies() {
         super("butterflies", "Schmetterlinge", "Bunte Schmetterlinge flattern um dich herum (tagsüber)", Category.VISUALS);
         this.icon("wing");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = 1.0 - Ambient.night();
            if (!(var3 < 0.01)) {
               double var5 = Mc.x(var2);
               double var7 = Mc.y(var2);
               double var9 = Mc.z(var2);

               while (this.pool.n < this.count.getI()) {
                  double[] var11 = this.pool.add();
                  var11[0] = var5 + (Ambient.RND.nextDouble() - 0.5) * 16.0;
                  var11[1] = var7 + 0.5 + Ambient.RND.nextDouble() * 2.0;
                  var11[2] = var9 + (Ambient.RND.nextDouble() - 0.5) * 16.0;
                  var11[6] = 0.0;
                  var11[7] = 15.0 + Ambient.RND.nextDouble() * 20.0;
                  var11[8] = Ambient.RND.nextDouble() * 1000.0;
                  var11[9] = 0.7 + Ambient.RND.nextDouble() * 0.5;
               }

               double var26 = System.currentTimeMillis() / 1000.0;
               Identifier var13 = Ambient.tex("butterfly0");
               Identifier var14 = Ambient.tex("butterfly1");

               for (int var15 = this.pool.n - 1; var15 >= 0; var15--) {
                  double[] var16 = this.pool.p[var15];
                  var16[6] += var1;
                  double var17 = var16[8];
                  var16[0] += Math.sin(var26 * 0.6 + var17) * 0.9 * var1;
                  var16[1] += Math.sin(var26 * 2.2 + var17) * 0.5 * var1;
                  var16[2] += Math.cos(var26 * 0.5 + var17 * 1.3) * 0.9 * var1;
                  double var19 = var16[0] - var5;
                  double var21 = var16[2] - var9;
                  if (!(var16[6] > var16[7]) && !(var19 * var19 + var21 * var21 > 324.0) && var15 < this.count.getI()) {
                     double var23 = Math.min(1.0, Math.min(var16[6], var16[7] - var16[6])) * var3;
                     boolean var25 = Math.sin(var26 * 16.0 + var17) > 0.0;
                     R3.billboardRot(
                        var25 ? var13 : var14, var16[0], var16[1], var16[2], 0.32 * var16[9], Math.sin(var26 * 0.8 + var17) * 0.3, R3.argb(16777215, var23)
                     );
                  } else {
                     this.pool.remove(var15);
                  }
               }
            }
         }
      }
   }

   public static final class Fireflies extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 10.0, 200.0, 10.0, 60.0, ""));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 10));
      final Setting.Bool nightOnly = this.add(new Setting.Bool("Nur nachts", false));
      final Setting.Num size = this.add(new Setting.Num("Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      private final Ambient.Pool pool = new Ambient.Pool(200);

      Fireflies() {
         super("fireflies", "Glühwürmchen", "Leuchtende Glühwürmchen schwirren um dich herum", Category.VISUALS);
         this.icon("firefly");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = this.nightOnly.get() ? Ambient.night() : 1.0;
            if (!(var3 <= 0.01)) {
               double var5 = Mc.x(var2);
               double var7 = Mc.y(var2);
               double var9 = Mc.z(var2);
               int var11 = 0;

               while (this.pool.n < this.count.getI() && var11++ < 8) {
                  double var12 = var5 + (Ambient.RND.nextDouble() - 0.5) * 28.0;
                  double var14 = var9 + (Ambient.RND.nextDouble() - 0.5) * 28.0;
                  int var16 = Ambient.top(var12, var14);
                  double var17 = (!(var16 > var7 + 6.0) && !(var16 < var7 - 8.0) ? var16 : var7) + 0.4 + Ambient.RND.nextDouble() * 2.6;
                  if (Ambient.air(var12, var17, var14)) {
                     double[] var19 = this.pool.add();
                     var19[0] = var12;
                     var19[1] = var17;
                     var19[2] = var14;
                     var19[6] = 0.0;
                     var19[7] = 12.0 + Ambient.RND.nextDouble() * 20.0;
                     var19[8] = Ambient.RND.nextDouble() * 1000.0;
                     var19[9] = 0.7 + Ambient.RND.nextDouble() * 0.6;
                     var19[3] = var19[4] = var19[5] = 0.0;
                  }
               }

               double var37 = System.currentTimeMillis() / 1000.0;
               Identifier var38 = Ambient.tex("glow");
               Identifier var15 = Ambient.tex("core");

               for (int var39 = this.pool.n - 1; var39 >= 0; var39--) {
                  double[] var40 = this.pool.p[var39];
                  var40[6] += var1;
                  double var18 = var40[0] - var5;
                  double var20 = var40[2] - var9;
                  if (!(var40[6] > var40[7]) && !(var18 * var18 + var20 * var20 > 400.0) && var39 < this.count.getI()) {
                     double var22 = var40[8];
                     var40[3] += (Math.sin(var37 * 0.7 + var22) * 0.9 - var40[3]) * var1;
                     var40[4] += (Math.sin(var37 * 0.9 + var22 * 1.3) * 0.35 - var40[4]) * var1;
                     var40[5] += (Math.cos(var37 * 0.6 + var22 * 0.7) * 0.9 - var40[5]) * var1;
                     double var24 = var40[0] + var40[3] * var1;
                     double var26 = var40[1] + var40[4] * var1;
                     double var28 = var40[2] + var40[5] * var1;
                     if (Ambient.air(var24, var26, var28)) {
                        var40[0] = var24;
                        var40[1] = var26;
                        var40[2] = var28;
                     } else {
                        var40[3] = -var40[3];
                        var40[4] = Math.abs(var40[4]);
                        var40[5] = -var40[5];
                     }

                     double var30 = 0.35 + 0.65 * Math.pow(Math.max(0.0, Math.sin(var37 * 1.7 + var22 * 3.0)), 2.0);
                     double var32 = Math.min(1.0, Math.min(var40[6] / 1.2, (var40[7] - var40[6]) / 1.5)) * var3;
                     int var34 = this.color.argb(var22) & 16777215;
                     double var35 = this.size.get() * var40[9];
                     R3.glowBillboard(var38, var40[0], var40[1], var40[2], 0.75 * var35, R3.argb(var34, 0.45 * var30 * var32));
                     R3.glowBillboard(var15, var40[0], var40[1], var40[2], 0.16 * var35, R3.argb(16777200, 0.95 * var30 * var32));
                  } else {
                     this.pool.remove(var39);
                  }
               }
            }
         }
      }

      @Override
      public void onDisable() {
         this.pool.n = 0;
      }
   }

   public static final class Lanterns extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 5.0, 120.0, 5.0, 40.0, ""));
      final Setting.Bool nightOnly = this.add(new Setting.Bool("Nur nachts", true));
      private final Ambient.Pool pool = new Ambient.Pool(120);

      Lanterns() {
         super("lanterns", "Himmelslaternen", "Leuchtende Laternen steigen langsam in den Himmel", Category.VISUALS);
         this.icon("flame");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = this.nightOnly.get() ? Ambient.night() : 1.0;
            if (!(var3 <= 0.01)) {
               double var5 = Mc.x(var2);
               double var7 = Mc.y(var2);
               double var9 = Mc.z(var2);

               while (this.pool.n < this.count.getI()) {
                  double var11 = Ambient.RND.nextDouble() * Math.PI * 2.0;
                  double var13 = 20.0 + Ambient.RND.nextDouble() * 70.0;
                  double[] var15 = this.pool.add();
                  var15[0] = var5 + Math.cos(var11) * var13;
                  var15[2] = var9 + Math.sin(var11) * var13;
                  var15[1] = var7 - 4.0 + Ambient.RND.nextDouble() * 50.0;
                  var15[6] = 0.0;
                  var15[7] = 40.0 + Ambient.RND.nextDouble() * 40.0;
                  var15[8] = Ambient.RND.nextDouble() * 1000.0;
                  var15[9] = 0.8 + Ambient.RND.nextDouble() * 0.5;
               }

               double var25 = System.currentTimeMillis() / 1000.0;
               Identifier var26 = Ambient.tex("lantern");
               Identifier var14 = Ambient.tex("glow");

               for (int var27 = this.pool.n - 1; var27 >= 0; var27--) {
                  double[] var16 = this.pool.p[var27];
                  var16[6] += var1;
                  var16[1] += (0.45 + 0.1 * Math.sin(var16[8])) * var1;
                  var16[0] += Math.sin(var25 * 0.2 + var16[8]) * 0.25 * var1;
                  var16[2] += Math.cos(var25 * 0.17 + var16[8]) * 0.25 * var1;
                  double var17 = var16[0] - var5;
                  double var19 = var16[2] - var9;
                  if (!(var16[6] > var16[7]) && !(var16[1] > var7 + 120.0) && !(var17 * var17 + var19 * var19 > 12100.0) && var27 < this.count.getI()) {
                     double var21 = Math.min(1.0, Math.min(var16[6] / 3.0, (var16[7] - var16[6]) / 6.0)) * var3;
                     double var23 = 0.85 + 0.15 * Math.sin(var25 * 9.0 + var16[8]);
                     R3.glowBillboard(var14, var16[0], var16[1], var16[2], 3.2 * var16[9], R3.argb(16751168, 0.3 * var21 * var23));
                     R3.billboard(var26, var16[0], var16[1], var16[2], 1.3 * var16[9], R3.argb(16777215, var21));
                  } else {
                     this.pool.remove(var27);
                  }
               }
            }
         }
      }
   }

   public static final class Motes extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 20.0, 400.0, 20.0, 140.0, ""));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 2));
      private final Ambient.Pool pool = new Ambient.Pool(400);

      Motes() {
         super("motes", "Lichtstaub", "Feiner leuchtender Staub schwebt in der Luft (sehr cinematic)", Category.VISUALS);
         this.icon("sparkle");
         this.fresh();
      }

      void render(float var1) {
         double var2 = R3.camX();
         double var4 = R3.camY();
         double var6 = R3.camZ();

         while (this.pool.n < this.count.getI()) {
            double[] var8 = this.pool.add();
            var8[0] = var2 + (Ambient.RND.nextDouble() - 0.5) * 16.0;
            var8[1] = var4 + (Ambient.RND.nextDouble() - 0.5) * 8.0;
            var8[2] = var6 + (Ambient.RND.nextDouble() - 0.5) * 16.0;
            var8[6] = 0.0;
            var8[7] = 6.0 + Ambient.RND.nextDouble() * 8.0;
            var8[8] = Ambient.RND.nextDouble() * 1000.0;
            var8[9] = 0.5 + Ambient.RND.nextDouble();
         }

         double var21 = System.currentTimeMillis() / 1000.0;
         Identifier var10 = Ambient.tex("core");

         for (int var11 = this.pool.n - 1; var11 >= 0; var11--) {
            double[] var12 = this.pool.p[var11];
            var12[6] += var1;
            var12[0] += Math.sin(var21 * 0.3 + var12[8]) * 0.08 * var1;
            var12[1] += (0.05 + Math.sin(var21 * 0.5 + var12[8]) * 0.05) * var1;
            var12[2] += Math.cos(var21 * 0.35 + var12[8]) * 0.08 * var1;
            double var13 = var12[0] - var2;
            double var15 = var12[2] - var6;
            if (!(var12[6] > var12[7]) && !(var13 * var13 + var15 * var15 > 144.0) && var11 < this.count.getI()) {
               double var17 = Math.min(1.0, Math.min(var12[6] / 1.5, (var12[7] - var12[6]) / 1.5));
               double var19 = 0.5 + 0.5 * Math.sin(var21 * 2.3 + var12[8] * 5.0);
               R3.glowBillboard(var10, var12[0], var12[1], var12[2], 0.07 * var12[9], R3.argb(this.color.argb(var12[8]) & 16777215, 0.8 * var17 * var19));
            } else {
               this.pool.remove(var11);
            }
         }
      }
   }

   static final class Pool {
      final double[][] p;
      int n;

      Pool(int var1) {
         this.p = new double[var1][10];
      }

      double[] add() {
         return this.n >= this.p.length ? null : this.p[this.n++];
      }

      void remove(int var1) {
         double[] var2 = this.p[var1];
         this.p[var1] = this.p[this.n - 1];
         this.p[this.n - 1] = var2;
         this.n--;
      }
   }

   public static final class Rainbow extends Module {
      final Setting.Mode when = this.add(new Setting.Mode("Zeigen", 0, "Nach Regen", "Immer (am Tag)"));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 55.0, "%"));
      private long rainEnded = 0L;
      private boolean wasRaining;

      Rainbow() {
         super("rainbow", "Regenbogen", "Ein echter Regenbogen gegenüber der Sonne - nach Regen oder immer", Category.VISUALS);
         this.icon("rain");
         this.fresh();
      }

      @Override
      public void tick() {
         boolean var1 = false;

         try {
            var1 = Mc.world().getRainLevel(1.0F) > 0.2F;
         } catch (Throwable var3) {
         }

         if (this.wasRaining && !var1) {
            this.rainEnded = System.currentTimeMillis();
         }

         this.wasRaining = var1;
      }

      void render() {
         double var1 = 1.0 - Ambient.night();
         if (!(var1 < 0.05)) {
            double var3 = var1 * this.strength.get() / 100.0;
            if (this.when.index == 0) {
               long var5 = System.currentTimeMillis() - this.rainEnded;
               double var7 = this.wasRaining ? 0.5 : (var5 < 240000L ? 1.0 - var5 / 240000.0 : 0.0);
               var3 *= var7;
            }

            if (!(var3 < 0.01)) {
               long var30 = Math.floorMod(Mc.timeOfDay(), 24000L);
               double var31 = var30 / 24000.0 * Math.PI * 2.0;
               double var9 = Math.cos(var31) >= 0.0 ? 1.0 : -1.0;
               double var11 = 180.0;

               try {
                  var11 = Math.min(var11, Mc.mc().gameRenderer.getDepthFar() * 0.45);
               } catch (Throwable var29) {
               }

               double var13 = R3.camX() - var9 * var11 * 0.9;
               double var15 = R3.camY() - var11 * 0.25;
               double var17 = R3.camZ();
               double[] var19 = new double[]{0.0, 0.0, 1.0};
               double[] var20 = new double[]{0.0, 1.0, 0.0};
               int[] var21 = new int[]{16726843, 16751150, 16771130, 5102170, 3909119, 5987327, 10636287};
               double var22 = var11 * 0.62;
               double var24 = var11 * 0.018;

               for (int var26 = 0; var26 < var21.length; var26++) {
                  double var27 = var22 + (var21.length - 1 - var26) * var24;
                  arc(var13, var15, var17, var19, var20, var27, var27 + var24, R3.argb(var21[var26], 0.35 * var3));
               }
            }
         }
      }

      private static void arc(double var0, double var2, double var4, double[] var6, double[] var7, double var8, double var10, int var12) {
         byte var13 = 48;

         for (int var14 = 0; var14 < var13; var14++) {
            double var15 = Math.PI * var14 / var13;
            double var17 = Math.PI * (var14 + 1) / var13;
            double var19 = Math.cos(var15);
            double var21 = Math.sin(var15);
            double var23 = Math.cos(var17);
            double var25 = Math.sin(var17);
            R3.quad(
               var0 + (var6[0] * var19 + var7[0] * var21) * var8,
               var2 + (var6[1] * var19 + var7[1] * var21) * var8,
               var4 + (var6[2] * var19 + var7[2] * var21) * var8,
               var0 + (var6[0] * var19 + var7[0] * var21) * var10,
               var2 + (var6[1] * var19 + var7[1] * var21) * var10,
               var4 + (var6[2] * var19 + var7[2] * var21) * var10,
               var0 + (var6[0] * var23 + var7[0] * var25) * var10,
               var2 + (var6[1] * var23 + var7[1] * var25) * var10,
               var4 + (var6[2] * var23 + var7[2] * var25) * var10,
               var0 + (var6[0] * var23 + var7[0] * var25) * var8,
               var2 + (var6[1] * var23 + var7[1] * var25) * var8,
               var4 + (var6[2] * var23 + var7[2] * var25) * var8,
               R3.argb(var12, 0.0) | 0,
               var12,
               var12,
               R3.argb(var12, 0.0)
            );
         }
      }
   }

   public static final class Weather extends Module {
      static final String[] TYPES = new String[]{"Schnee", "Kirschblüten", "Herbstlaub", "Asche", "Glitzer", "Sternenstaub"};
      static final String[] TEXS = new String[]{"snow", "petal", "leaf", "ash", "glitter", "stardust"};
      final Setting.Mode type = this.add(new Setting.Mode("Art", 0, TYPES));
      final Setting.Num amount = this.add(new Setting.Num("Menge", 50.0, 1500.0, 50.0, 500.0, ""));
      final Setting.Num size = this.add(new Setting.Num("Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      final Setting.Num wind = this.add(new Setting.Num("Wind", 0.0, 3.0, 0.1, 0.6, ""));
      final Setting.Bool roof = this.add(new Setting.Bool("Nicht unter Dächern", true));
      private final Ambient.Pool pool = new Ambient.Pool(1500);
      private float spawnAcc;

      Weather() {
         super("snowfall", "Schnee & Blüten", "HD-Schnee, Kirschblüten, Herbstlaub, Glitzer ... fallen um dich herum", Category.VISUALS);
         this.icon("snow");
         this.fresh();
      }

      void render(float var1) {
         LocalPlayer var2 = Mc.player();
         if (var2 != null) {
            double var3 = R3.camX();
            double var5 = R3.camY();
            double var7 = R3.camZ();
            int var9 = this.amount.getI();
            this.spawnAcc += var1 * var9 / 5.0F;

            while (this.spawnAcc >= 1.0F && this.pool.n < var9) {
               this.spawnAcc--;
               double var10 = var3 + (Ambient.RND.nextDouble() - 0.5) * 36.0;
               double var12 = var7 + (Ambient.RND.nextDouble() - 0.5) * 36.0;
               double var14 = var5 + 4.0 + Ambient.RND.nextDouble() * 14.0;
               if (!this.roof.get() || !(Ambient.top(var10, var12) > var14)) {
                  double[] var16 = this.pool.add();
                  if (var16 == null) {
                     break;
                  }

                  var16[0] = var10;
                  var16[1] = var14;
                  var16[2] = var12;
                  var16[3] = 0.0;
                  var16[4] = -(0.8 + Ambient.RND.nextDouble() * 0.9) * (this.type.index == 3 ? 0.6 : 1.0);
                  var16[5] = 0.0;
                  var16[6] = 0.0;
                  var16[7] = 8.0 + Ambient.RND.nextDouble() * 6.0;
                  var16[8] = Ambient.RND.nextDouble() * 1000.0;
                  var16[9] = 0.6 + Ambient.RND.nextDouble() * 0.8;
               }
            }

            if (this.spawnAcc > 5.0F) {
               this.spawnAcc = 5.0F;
            }

            double var29 = System.currentTimeMillis() / 1000.0;
            Identifier var30 = Ambient.tex(TEXS[this.type.index]);
            boolean var13 = this.type.index == 1 || this.type.index == 2;
            double var31 = this.wind.get();

            for (int var32 = this.pool.n - 1; var32 >= 0; var32--) {
               double[] var17 = this.pool.p[var32];
               var17[6] += var1;
               double var18 = Math.sin(var29 * 1.3 + var17[8]) * 0.6 * (var13 ? 1.6 : 1.0);
               var17[0] += (var31 * 0.8 + var18) * var1;
               var17[2] += (Math.cos(var29 * 0.9 + var17[8] * 1.7) * 0.4 + var31 * 0.3) * var1;
               var17[1] += var17[4] * var1;
               double var20 = var17[0] - var3;
               double var22 = var17[2] - var7;
               if (!(var17[6] > var17[7])
                  && !(var17[1] < var5 - 12.0)
                  && !(var20 * var20 + var22 * var22 > 484.0)
                  && (!this.roof.get() || !(var17[1] < Ambient.top(var17[0], var17[2])))) {
                  double var24 = Math.min(1.0, Math.min(var17[6] / 0.6, (var17[7] - var17[6]) / 0.8));
                  double var26 = 0.12 * var17[9] * this.size.get() * (this.type.index != 4 && this.type.index != 5 ? 1.0 : 0.8);
                  int var28 = this.type.index != 4 && this.type.index != 5
                     ? R3.argb(16777215, var24 * 0.95)
                     : R3.argb(16777215, var24 * (0.5 + 0.5 * Math.sin(var29 * 6.0 + var17[8])));
                  if (var13) {
                     R3.billboardRot(var30, var17[0], var17[1], var17[2], var26 * 1.3, var29 * 1.6 + var17[8], var28);
                  } else {
                     R3.billboardRot(var30, var17[0], var17[1], var17[2], var26, var17[8], var28);
                  }
               } else {
                  this.pool.remove(var32);
               }
            }
         }
      }

      @Override
      public void onDisable() {
         this.pool.n = 0;
      }
   }
}
