package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.perf.Performance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public final class WorldFx {
   public static final List<WorldFx.Fly> ALL = new ArrayList<>();

   private WorldFx() {
   }

   public static void registerAll() {
      add(
         k(
               "birds",
               "Vogelschwarm",
               "Schwärme von Vögeln ziehen über den Himmel",
               "wing",
               new String[]{"bird0", "bird1"},
               18,
               45.0F,
               18.0F,
               28.0F,
               0.9F,
               7.0F,
               16777215
            )
            .flap(7.0F)
            .sky()
            .wander(0.2F)
            .life(20.0F)
      );
      add(
         k("bats", "Fledermäuse", "Fledermäuse flattern nachts um dich", "moon", new String[]{"bat0", "bat1"}, 10, 14.0F, 2.0F, 7.0F, 0.45F, 4.0F, 16777215)
            .flap(11.0F)
            .night()
            .wander(2.2F)
      );
      add(
         k(
               "dragonflies",
               "Libellen",
               "Schillernde Libellen schwirren (tagsüber)",
               "wing",
               new String[]{"dragonfly"},
               8,
               14.0F,
               0.4F,
               2.2F,
               0.32F,
               3.5F,
               16777215
            )
            .day()
            .wander(3.0F)
            .rotate()
      );
      add(
         k(
               "wisps",
               "Irrlichter",
               "Leuchtende magische Lichter schweben umher",
               "sparkle",
               new String[]{"core"},
               24,
               18.0F,
               0.5F,
               4.0F,
               0.22F,
               1.2F,
               8189951,
               11561983,
               7077808
            )
            .glow()
            .trail()
            .wander(1.4F)
      );
      add(
         k("dandelion", "Pusteblumen", "Pusteblumen-Samen tanzen im Wind", "leaf", new String[]{"seed"}, 40, 18.0F, 0.5F, 6.0F, 0.28F, 0.8F, 16777215)
            .rotate()
            .wander(0.8F)
            .rise(0.08F)
      );
      add(
         k(
               "embers",
               "Glutfunken",
               "Glühende Funken steigen auf",
               "flame",
               new String[]{"core"},
               60,
               12.0F,
               0.0F,
               2.0F,
               0.08F,
               1.0F,
               16756782,
               16738846,
               16769136
            )
            .glow()
            .rise(0.9F)
            .wander(0.6F)
            .life(5.0F)
      );
      add(
         k(
               "pollen",
               "Blütenpollen",
               "Goldener Pollen glitzert in der Luft",
               "sun",
               new String[]{"core"},
               140,
               10.0F,
               0.3F,
               3.0F,
               0.05F,
               0.4F,
               16770442,
               16774856
            )
            .glow()
            .wander(0.5F)
            .day()
      );
      add(
         k(
               "fairies",
               "Feen",
               "Kleine Feen mit Glitzerspur fliegen herum",
               "wand",
               new String[]{"core"},
               10,
               12.0F,
               0.8F,
               3.5F,
               0.2F,
               2.4F,
               16751848,
               10283263,
               15269788
            )
            .glow()
            .trail()
            .wander(2.6F)
      );
      add(
         k(
               "notes",
               "Musiknoten",
               "Bunte Musiknoten schweben nach oben",
               "music",
               new String[]{"note"},
               20,
               8.0F,
               0.3F,
               2.5F,
               0.3F,
               0.6F,
               16731533,
               5100287,
               16765503,
               8191851
            )
            .rise(0.4F)
            .wander(0.4F)
            .life(7.0F)
      );
      add(
         k("hearts", "Herzen", "Herzen steigen sanft um dich auf", "heart", new String[]{"heart"}, 16, 7.0F, 0.2F, 2.0F, 0.25F, 0.5F, 16731501, 16747190)
            .rise(0.45F)
            .wander(0.3F)
            .life(6.0F)
      );
      add(
         k(
               "shards",
               "Kristallsplitter",
               "Schwebende, drehende Kristallsplitter",
               "gem",
               new String[]{"shard"},
               16,
               12.0F,
               0.8F,
               4.0F,
               0.3F,
               0.3F,
               12575743,
               14925823
            )
            .rotate()
            .wander(0.3F)
            .glow()
      );
      add(
         k(
               "confetti",
               "Konfetti",
               "Buntes Konfetti regnet herab",
               "sparkle",
               new String[]{"confetti"},
               120,
               12.0F,
               4.0F,
               12.0F,
               0.12F,
               1.0F,
               16731501,
               5100287,
               16765503,
               8191851,
               11561983
            )
            .rotate()
            .fall(1.1F)
            .wander(1.2F)
            .life(8.0F)
      );
      add(
         k(
               "leafstorm",
               "Blätterwirbel",
               "Bunte Blätter wirbeln im Wind um dich",
               "leaf",
               new String[]{"leafswirl"},
               50,
               12.0F,
               0.3F,
               5.0F,
               0.3F,
               2.5F,
               16777215
            )
            .rotate()
            .wander(0.4F)
            .swirl()
      );
   }

   private static WorldFx.Kind k(
      String var0, String var1, String var2, String var3, String[] var4, int var5, float var6, float var7, float var8, float var9, float var10, int... var11
   ) {
      WorldFx.Kind var12 = new WorldFx.Kind();
      var12.id = var0;
      var12.name = var1;
      var12.desc = var2;
      var12.icon = var3;
      var12.sprites = var4;
      var12.defCount = var5;
      var12.radius = var6;
      var12.hMin = var7;
      var12.hMax = var8;
      var12.size = var9;
      var12.speed = var10;
      var12.colors = var11;
      return var12;
   }

   private static void add(WorldFx.Kind var0) {
      ALL.add(Modules.register(new WorldFx.Fly(var0)));
   }

   static void render() {
      for (WorldFx.Fly var1 : ALL) {
         if (var1.enabled) {
            var1.render();
         }
      }
   }

   public static final class Fly extends Module {
      final WorldFx.Kind k;
      final Setting.Num amount;
      final Setting.Num size;
      private final List<double[]> ps = new ArrayList<>();
      private final Random rnd = new Random();
      private long last;

      Fly(WorldFx.Kind var1) {
         super("fx_" + var1.id, var1.name, var1.desc, Category.VISUALS);
         this.k = var1;
         this.icon(var1.icon);
         this.fresh();
         this.amount = this.add(new Setting.Num("Menge", 10.0, 300.0, 10.0, 100.0, "%"));
         this.size = this.add(new Setting.Num("Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      }

      @Override
      public void onDisable() {
         this.ps.clear();
      }

      void render() {
         LocalPlayer var1 = Mc.player();
         if (var1 != null) {
            double var2 = 1.0;
            if (this.k.night) {
               var2 = Ambient.night();
            }

            if (this.k.day) {
               var2 = 1.0 - Ambient.night();
            }

            if (!(var2 < 0.02)) {
               long var4 = System.nanoTime();
               float var6 = this.last == 0L ? 0.016F : Math.min(0.1F, (float)(var4 - this.last) / 1.0E9F);
               this.last = var4;
               double var7 = Mc.x(var1);
               double var9 = Mc.y(var1);
               double var11 = Mc.z(var1);
               int var13 = Math.max(
                  1, Math.round(this.k.defCount * this.amount.getF() / 100.0F * (Performance.boost == null ? 1.0F : Performance.boost.fxScale()))
               );
               int var14 = 0;

               while (this.ps.size() < var13 && var14++ < 6) {
                  this.ps.add(this.spawn(var7, var9, var11, this.ps.isEmpty()));
               }

               double var15 = System.currentTimeMillis() / 1000.0;
               Identifier[] var17 = new Identifier[this.k.sprites.length];

               for (int var18 = 0; var18 < var17.length; var18++) {
                  var17[var18] = Ambient.tex(this.k.sprites[var18]);
               }

               Identifier var36 = this.k.glow ? Ambient.tex("glow") : null;

               for (int var19 = this.ps.size() - 1; var19 >= 0; var19--) {
                  double[] var20 = this.ps.get(var19);
                  var20[6] += var6;
                  double var21 = var20[8];
                  double var23 = this.k.wander * this.k.speed;
                  if (this.k.sky) {
                     double var25 = var21 * 0.01 + var15 * 0.03;
                     var20[3] = Math.cos(var25) * this.k.speed;
                     var20[5] = Math.sin(var25) * this.k.speed;
                     var20[4] = Math.sin(var15 * 0.5 + var21) * 0.3;
                  } else if (this.k.swirl) {
                     double var37 = Math.atan2(var20[2] - var11, var20[0] - var7) + 1.2;
                     var20[3] += (Math.cos(var37) * this.k.speed - var20[3]) * var6;
                     var20[5] += (Math.sin(var37) * this.k.speed - var20[5]) * var6;
                     var20[4] += (Math.sin(var15 * 1.3 + var21) * 0.6 - var20[4]) * var6;
                  } else {
                     var20[3] += (Math.sin(var15 * 0.7 * this.k.speed + var21) * var23 - var20[3]) * var6 * 1.5;
                     var20[5] += (Math.cos(var15 * 0.6 * this.k.speed + var21 * 1.3) * var23 - var20[5]) * var6 * 1.5;
                     var20[4] += (Math.sin(var15 * 0.9 + var21 * 2.0) * 0.35 * var23 + this.k.rise - this.k.fall - var20[4]) * var6 * 1.5;
                  }

                  var20[0] += var20[3] * var6;
                  var20[1] += var20[4] * var6;
                  var20[2] += var20[5] * var6;
                  double var38 = var20[0] - var7;
                  double var27 = var20[2] - var11;
                  if (!(var20[6] > var20[7]) && !(var38 * var38 + var27 * var27 > this.k.radius * 1.4 * (this.k.radius * 1.4)) && var19 < var13) {
                     double var29 = Math.min(1.0, Math.min(var20[6] / 1.2, (var20[7] - var20[6]) / 1.2)) * var2;
                     int var31 = (int)var20[9];
                     double var32 = this.k.size * this.size.get() * (0.75 + var21 % 1.0 * 0.5);
                     int var34 = var17.length > 1 ? (int)((var15 * this.k.flapHz + var21) % var17.length) : 0;
                     if (var36 != null) {
                        R3.glowBillboard(var36, var20[0], var20[1], var20[2], var32 * 3.2, R3.argb(var31, 0.35 * var29));
                     }

                     if (this.k.trail) {
                        for (int var35 = 1; var35 <= 4; var35++) {
                           R3.glowBillboard(
                              var17[0],
                              var20[0] - var20[3] * 0.06 * var35,
                              var20[1] - var20[4] * 0.06 * var35,
                              var20[2] - var20[5] * 0.06 * var35,
                              var32 * (1.0 - var35 * 0.18),
                              R3.argb(var31, 0.4 * var29 * (1.0 - var35 * 0.2))
                           );
                        }
                     }

                     if (this.k.glow) {
                        R3.glowBillboard(var17[var34], var20[0], var20[1], var20[2], var32, R3.argb(var31, var29));
                     } else if (this.k.rotate) {
                        R3.billboardRot(var17[var34], var20[0], var20[1], var20[2], var32, var15 * 1.4 + var21, R3.argb(var31, var29));
                     } else {
                        R3.billboard(var17[var34], var20[0], var20[1], var20[2], var32, R3.argb(var31, var29));
                     }
                  } else {
                     this.ps.remove(var19);
                  }
               }
            }
         }
      }

      private double[] spawn(double var1, double var3, double var5, boolean var7) {
         double var8 = this.rnd.nextDouble() * Math.PI * 2.0;
         double var10 = Math.sqrt(this.rnd.nextDouble()) * this.k.radius;
         double var12 = var1 + Math.cos(var8) * var10;
         double var14 = var5 + Math.sin(var8) * var10;
         int var16 = Ambient.top(var12, var14);
         double var17 = this.k.sky ? var3 : (!(var16 > var3 + 6.0) && !(var16 < var3 - 10.0) ? var16 : var3);
         double var19 = var17 + this.k.hMin + this.rnd.nextDouble() * (this.k.hMax - this.k.hMin);
         return new double[]{
            var12,
            var19,
            var14,
            0.0,
            0.0,
            0.0,
            var7 ? this.rnd.nextDouble() * this.k.life * 0.5 : 0.0,
            this.k.life * (0.7 + this.rnd.nextDouble() * 0.6),
            this.rnd.nextDouble() * 1000.0,
            this.k.colors[this.rnd.nextInt(this.k.colors.length)]
         };
      }
   }

   static final class Kind {
      String id;
      String name;
      String desc;
      String icon;
      String[] sprites;
      float flapHz = 0.0F;
      int defCount;
      float radius = 16.0F;
      float hMin = 0.5F;
      float hMax = 4.0F;
      float size = 0.3F;
      float speed = 1.0F;
      int[] colors = new int[]{16777215};
      boolean glow;
      boolean rotate;
      boolean night;
      boolean day;
      boolean sky;
      float rise;
      float fall;
      float wander = 1.0F;
      float life = 12.0F;
      boolean trail;
      boolean swirl;

      WorldFx.Kind flap(float var1) {
         this.flapHz = var1;
         return this;
      }

      WorldFx.Kind sky() {
         this.sky = true;
         return this;
      }

      WorldFx.Kind wander(float var1) {
         this.wander = var1;
         return this;
      }

      WorldFx.Kind life(float var1) {
         this.life = var1;
         return this;
      }

      WorldFx.Kind night() {
         this.night = true;
         return this;
      }

      WorldFx.Kind day() {
         this.day = true;
         return this;
      }

      WorldFx.Kind rotate() {
         this.rotate = true;
         return this;
      }

      WorldFx.Kind glow() {
         this.glow = true;
         return this;
      }

      WorldFx.Kind trail() {
         this.trail = true;
         return this;
      }

      WorldFx.Kind rise(float var1) {
         this.rise = var1;
         return this;
      }

      WorldFx.Kind fall(float var1) {
         this.fall = var1;
         return this;
      }

      WorldFx.Kind swirl() {
         this.swirl = true;
         return this;
      }
   }
}
