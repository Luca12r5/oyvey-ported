package dev.lego.perf;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Gx;
import dev.lego.ui.Toasts;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class Performance {
   public static Performance.FpsBoost boost;
   public static Performance.EntityCulling culling;
   public static Performance.ParticleLimit particles;
   public static Performance.BackgroundFps background;
   public static Performance.FreeMemory memory;
   public static Performance.Monitor monitor;
   public static Performance.Guard guard;

   private Performance() {
   }

   public static boolean lowMode() {
      return guard != null && guard.enabled && guard.low;
   }

   public static void registerAll() {
      boost = Modules.register(new Performance.FpsBoost());
      culling = Modules.register(new Performance.EntityCulling());
      particles = Modules.register(new Performance.ParticleLimit());
      background = Modules.register(new Performance.BackgroundFps());
      memory = Modules.register(new Performance.FreeMemory());
      monitor = Modules.register(new Performance.Monitor());
      guard = Modules.register(new Performance.Guard());
   }

   public static final class BackgroundFps extends Module {
      final Setting.Num fps = this.add(new Setting.Num("FPS im Hintergrund", 5.0, 60.0, 5.0, 15.0, ""));

      BackgroundFps() {
         super("backgroundfps", "Hintergrund-FPS", "Weniger FPS, wenn Minecraft nicht im Vordergrund ist (spart Strom & Hitze)", Category.PERFORMANCE);
         this.icon("fps");
         this.enabled = true;
         this.fresh();
      }

      public int limit(int var1) {
         return this.enabled && !Mc.windowFocused() ? Math.min(var1, this.fps.getI()) : var1;
      }
   }

   public static final class EntityCulling extends Module {
      final Setting.Bool occlusion = this.add(new Setting.Bool("Hinter Wänden nicht rendern", true));
      final Setting.Num itemDist = this.add(new Setting.Num("Items / XP bis", 8.0, 64.0, 4.0, 32.0, " Blöcke"));
      final Setting.Num mobDist = this.add(new Setting.Num("Mobs bis", 16.0, 128.0, 8.0, 96.0, " Blöcke"));
      final Setting.Bool keepPlayers = this.add(new Setting.Bool("Spieler immer rendern", true));
      private final Map<Integer, long[]> cache = new HashMap<>();
      private long frame = 0L;

      EntityCulling() {
         super("entityculling", "Entity-Culling", "Rendert Mobs/Items nicht, wenn sie weit weg oder hinter Blöcken sind", Category.PERFORMANCE);
         this.icon("entity");
         this.fresh();
      }

      @Override
      public void tick() {
         this.frame++;
         if (this.frame % 200L == 0L) {
            this.cache.entrySet().removeIf(var1 -> this.frame - var1.getValue()[0] > 100L);
         }
      }

      public boolean shouldRender(Entity var1, double var2, double var4, double var6) {
         if (var1 == Mc.player()) {
            return true;
         } else if (var1 instanceof Player && this.keepPlayers.get()) {
            return true;
         } else if (var1.isCurrentlyGlowing()) {
            return true;
         } else {
            double var8 = Mc.x(var1) - var2;
            double var10 = Mc.y(var1) + Mc.height(var1) / 2.0F - var4;
            double var12 = Mc.z(var1) - var6;
            double var14 = var8 * var8 + var10 * var10 + var12 * var12;
            String var16 = Mc.typeId(var1);
            boolean var17 = var16.equals("item") || var16.equals("experience_orb") || var16.equals("arrow");
            boolean var18 = Performance.boost != null && Performance.boost.extreme();
            double var19 = var17
               ? (var18 ? Math.min(16.0, this.itemDist.get()) : this.itemDist.get())
               : (var18 ? Math.min(48.0, this.mobDist.get()) : this.mobDist.get());
            if (!(var1 instanceof Player) && var14 > var19 * var19) {
               return false;
            } else if (this.occlusion.get() && !(var14 < 16.0)) {
               float var21 = Mc.width(var1);
               float var22 = Mc.height(var1);
               if (!(var21 > 3.0F) && !(var22 > 4.0F)) {
                  int var23 = Mc.id(var1);
                  long[] var24 = this.cache.get(var23);
                  if (var24 != null && this.frame - var24[0] < 4L) {
                     return var24[1] == 1L;
                  } else {
                     ClientLevel var25 = Mc.world();
                     boolean var26 = var25 == null
                        || visible(var25, var2, var4, var6, Mc.x(var1), Mc.y(var1) + var22 * 0.9, Mc.z(var1))
                        || visible(var25, var2, var4, var6, Mc.x(var1), Mc.y(var1) + var22 * 0.2, Mc.z(var1))
                        || visible(var25, var2, var4, var6, Mc.x(var1) + var21 / 2.0F, Mc.y(var1) + var22 / 2.0F, Mc.z(var1) + var21 / 2.0F)
                        || visible(var25, var2, var4, var6, Mc.x(var1) - var21 / 2.0F, Mc.y(var1) + var22 / 2.0F, Mc.z(var1) - var21 / 2.0F);
                     this.cache.put(var23, new long[]{this.frame, var26 ? 1L : 0L});
                     return var26;
                  }
               } else {
                  return true;
               }
            } else {
               return true;
            }
         }
      }

      private static boolean visible(ClientLevel var0, double var1, double var3, double var5, double var7, double var9, double var11) {
         double var13 = var7 - var1;
         double var15 = var9 - var3;
         double var17 = var11 - var5;
         int var19 = (int)Math.floor(var1);
         int var20 = (int)Math.floor(var3);
         int var21 = (int)Math.floor(var5);
         int var22 = (int)Math.floor(var7);
         int var23 = (int)Math.floor(var9);
         int var24 = (int)Math.floor(var11);
         int var25 = var13 > 0.0 ? 1 : -1;
         int var26 = var15 > 0.0 ? 1 : -1;
         int var27 = var17 > 0.0 ? 1 : -1;
         double var28 = var13 == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / var13);
         double var30 = var15 == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / var15);
         double var32 = var17 == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / var17);
         double var34 = var13 == 0.0 ? Double.MAX_VALUE : (var25 > 0 ? var19 + 1 - var1 : var1 - var19) * var28;
         double var36 = var15 == 0.0 ? Double.MAX_VALUE : (var26 > 0 ? var20 + 1 - var3 : var3 - var20) * var30;
         double var38 = var17 == 0.0 ? Double.MAX_VALUE : (var27 > 0 ? var21 + 1 - var5 : var5 - var21) * var32;
         MutableBlockPos var40 = new MutableBlockPos();

         for (int var41 = 0; var41 < 256; var41++) {
            if (var19 == var22 && var20 == var23 && var21 == var24) {
               return true;
            }

            if (var34 < var36 && var34 < var38) {
               var19 += var25;
               var34 += var28;
            } else if (var36 < var38) {
               var20 += var26;
               var36 += var30;
            } else {
               var21 += var27;
               var38 += var32;
            }

            if (var19 == var22 && var20 == var23 && var21 == var24) {
               return true;
            }

            var40.set(var19, var20, var21);
            if (var0.getBlockState(var40).isSolidRender()) {
               return false;
            }
         }

         return true;
      }
   }

   public static final class FpsBoost extends Module {
      final Setting.Mode level = this.add(new Setting.Mode("Stufe", 1, "Leicht", "Stark", "Maximum", "EXTREM"));
      final Setting.Bool clouds = this.add(new Setting.Bool("Wolken aus", true));
      final Setting.Bool shadows = this.add(new Setting.Bool("Entity-Schatten aus", true));
      private final Map<String, String> saved = new HashMap<>();
      private final Map<String, Object[]> opts = new LinkedHashMap<>();
      private static final Performance.FpsBoost.Parse INT = var0 -> (int)Double.parseDouble(var0);
      private static final Performance.FpsBoost.Parse BOOL = Boolean::parseBoolean;
      private static final Performance.FpsBoost.Parse DBL = Double::parseDouble;

      FpsBoost() {
         super(
            "fpsboost",
            "FPS-Boost",
            "VSync aus, FPS unbegrenzt, Grafik auf Leistung · Stufe EXTREM holt das Maximum raus · Ausschalten stellt alles zurück",
            Category.PERFORMANCE
         );
         this.icon("boost");
         this.fresh();
         Modules.persist("fpsboost", new Modules.Persist() {
            @Override
            public Object save() {
               return new LinkedHashMap<>(FpsBoost.this.saved);
            }

            @Override
            public void load(Object var1) {
               if (var1 instanceof Map) {
                  FpsBoost.this.saved.clear();

                  for (Entry var3 : ((Map)var1).entrySet()) {
                     FpsBoost.this.saved.put((String)var3.getKey(), String.valueOf(var3.getValue()));
                  }
               }
            }
         });
      }

      private void set(String var1, OptionInstance var2, Object var3, Performance.FpsBoost.Parse var4) {
         if (var2 != null) {
            this.opts.put(var1, new Object[]{var2, var4});
            Object var5 = var2.get();
            if (!this.saved.containsKey(var1)) {
               this.saved.put(var1, var5 instanceof Enum ? ((Enum)var5).name() : String.valueOf(var5));
            }

            try {
               var2.set(var3);
            } catch (Throwable var7) {
               LegoClient.LOG("FPS-Boost " + var1 + ": " + var7);
            }
         }
      }

      private void capInt(String var1, OptionInstance var2, int var3) {
         if (var2 != null) {
            Object var4 = var2.get();
            if (var4 instanceof Integer && (Integer)var4 > var3) {
               this.set(var1, var2, var3, INT);
            }
         }
      }

      @Override
      public void onEnable() {
         if (Mc.mc() != null && Mc.options() != null) {
            this.apply();
            Toasts.show("boost", "FPS-Boost an", this.level.get() + " · VSync aus, FPS unbegrenzt · wird beim Ausschalten zurückgesetzt", -14756000, 3500L);
         }
      }

      void apply() {
         Options var1 = Mc.options();

         try {
            int var2 = this.level.index;
            this.set("vsync", var1.enableVsync(), Boolean.FALSE, BOOL);
            this.set("maxFps", var1.framerateLimit(), 260, INT);
            this.set("particles", var1.particles(), var2 == 0 ? ParticleStatus.DECREASED : ParticleStatus.MINIMAL, var0 -> ParticleStatus.valueOf(var0));
            if (this.clouds.get()) {
               this.set("clouds", var1.cloudStatus(), var2 == 0 ? CloudStatus.FAST : CloudStatus.OFF, var0 -> CloudStatus.valueOf(var0));
            }

            if (this.shadows.get()) {
               this.set("shadows", var1.entityShadows(), Boolean.FALSE, BOOL);
            }

            this.set("blend", var1.biomeBlendRadius(), var2 == 0 ? 2 : (var2 == 1 ? 1 : 0), INT);
            this.set("menuBlur", var1.menuBackgroundBlurriness(), 0, INT);
            this.set("improvedTransparency", var1.improvedTransparency(), Boolean.FALSE, BOOL);
            this.capInt("weather", var1.weatherRadius(), var2 == 0 ? 8 : 5);
            this.capInt("anisotropy", var1.maxAnisotropyBit(), var2 == 0 ? 2 : 1);
            if (var2 >= 3) {
               this.set("particles", var1.particles(), ParticleStatus.MINIMAL, var0 -> ParticleStatus.valueOf(var0));
               this.set("clouds", var1.cloudStatus(), CloudStatus.OFF, var0 -> CloudStatus.valueOf(var0));
               this.set("shadows", var1.entityShadows(), Boolean.FALSE, BOOL);
               this.set("chunkFade", var1.chunkSectionFadeInTime(), 0.0, DBL);
               this.capInt("weather", var1.weatherRadius(), 3);
               this.capInt("renderDist", var1.renderDistance(), 6);
               this.capInt("simDist", var1.simulationDistance(), 5);
               this.capInt("cloudDist", var1.cloudRange(), 2);
               this.set("entityDist", var1.entityDistanceScaling(), 0.5, DBL);
               this.set("ao", var1.ambientOcclusion(), Boolean.FALSE, BOOL);
               this.set("cutoutLeaves", var1.cutoutLeaves(), Boolean.FALSE, BOOL);
               this.capInt("mipmaps", var1.mipmapLevels(), 0);
               this.capInt("anisotropy", var1.maxAnisotropyBit(), 1);
            } else if (var2 >= 1) {
               this.set("entityDist", var1.entityDistanceScaling(), var2 == 1 ? 0.75 : 0.5, DBL);
               this.set("ao", var1.ambientOcclusion(), Boolean.FALSE, BOOL);
               this.set("cutoutLeaves", var1.cutoutLeaves(), Boolean.FALSE, BOOL);
               this.capInt("mipmaps", var1.mipmapLevels(), var2 == 1 ? 2 : 0);
               this.capInt("renderDist", var1.renderDistance(), var2 == 1 ? 12 : 8);
               this.capInt("simDist", var1.simulationDistance(), var2 == 1 ? 8 : 5);
               this.capInt("cloudDist", var1.cloudRange(), 8);
            }

            var1.save();
            Modules.scheduleSave();
         } catch (Throwable var3) {
            LegoClient.LOG("FPS-Boost: " + var3);
         }
      }

      public float fxScale() {
         return !this.enabled ? 1.0F : (this.level.index == 0 ? 0.8F : (this.level.index == 1 ? 0.55F : (this.level.index == 2 ? 0.3F : 0.0F)));
      }

      public boolean extreme() {
         return this.enabled && this.level.index >= 3;
      }

      public double blockEntityDist() {
         return !this.enabled ? 0.0 : (this.level.index >= 3 ? 24.0 : (this.level.index == 2 ? 40.0 : 0.0));
      }

      @Override
      public void onDisable() {
         if (Mc.mc() != null && Mc.options() != null) {
            Options var1 = Mc.options();
            LinkedHashMap var2 = new LinkedHashMap<>(this.opts);
            Object[][] var3 = new Object[][]{
               {"vsync", var1.enableVsync(), BOOL},
               {"maxFps", var1.framerateLimit(), INT},
               {"particles", var1.particles(), (Performance.FpsBoost.Parse)var0 -> ParticleStatus.valueOf(var0)},
               {"clouds", var1.cloudStatus(), (Performance.FpsBoost.Parse)var0 -> CloudStatus.valueOf(var0)},
               {"shadows", var1.entityShadows(), BOOL},
               {"blend", var1.biomeBlendRadius(), INT},
               {"menuBlur", var1.menuBackgroundBlurriness(), INT},
               {"improvedTransparency", var1.improvedTransparency(), BOOL},
               {"weather", var1.weatherRadius(), INT},
               {"anisotropy", var1.maxAnisotropyBit(), INT},
               {"entityDist", var1.entityDistanceScaling(), DBL},
               {"ao", var1.ambientOcclusion(), BOOL},
               {"cutoutLeaves", var1.cutoutLeaves(), BOOL},
               {"chunkFade", var1.chunkSectionFadeInTime(), DBL},
               {"mipmaps", var1.mipmapLevels(), INT},
               {"renderDist", var1.renderDistance(), INT},
               {"simDist", var1.simulationDistance(), INT},
               {"cloudDist", var1.cloudRange(), INT}
            };

            for (Object[] var7 : var3) {
               var2.putIfAbsent((String)var7[0], new Object[]{var7[1], var7[2]});
            }

            for (Entry var11 : this.saved.entrySet()) {
               Object[] var12 = (Object[])var2.get(var11.getKey());
               if (var12 != null && var12[0] != null) {
                  try {
                     ((OptionInstance)var12[0]).set(((Performance.FpsBoost.Parse)var12[1]).of((String)var11.getValue()));
                  } catch (Throwable var9) {
                     LegoClient.LOG("FPS-Boost restore " + (String)var11.getKey() + ": " + var9);
                  }
               }
            }

            try {
               var1.save();
            } catch (Throwable var8) {
            }

            this.saved.clear();
            Modules.scheduleSave();
            Toasts.show("boost", "FPS-Boost aus", "Grafik-Einstellungen wiederhergestellt", -7696487, 2500L);
         }
      }

      private interface Parse {
         Object of(String var1);
      }
   }

   public static final class FreeMemory extends Module {
      FreeMemory() {
         super("freememory", "RAM freigeben", "Räumt ungenutzten Arbeitsspeicher auf (bei Rucklern nach langem Spielen)", Category.PERFORMANCE);
         this.icon("memory");
         this.fresh();
         this.add(new Setting.Action("Jetzt", "RAM freigeben", Performance.FreeMemory::run));
      }

      @Override
      public boolean isToggleable() {
         return false;
      }

      public static void run() {
         Runtime var0 = Runtime.getRuntime();
         long var1 = var0.totalMemory() - var0.freeMemory();
         new Thread(
               () -> {
                  System.gc();
                  long var3 = var0.totalMemory() - var0.freeMemory();
                  long var5 = Math.max(0L, var1 - var3) / 1048576L;
                  Toasts.show(
                     "memory", "RAM freigegeben", var5 + " MB frei · " + var3 / 1048576L + " / " + var0.maxMemory() / 1048576L + " MB belegt", -16735270, 3500L
                  );
               },
               "Lego-GC"
            )
            .start();
      }
   }

   public static final class Guard extends Module {
      final Setting.Bool memory = this.add(new Setting.Bool("RAM-Wächter", true));
      final Setting.Bool lowFps = this.add(new Setting.Bool("Bei Rucklern Extras pausieren", true));
      final Setting.Num limit = this.add(new Setting.Num("Pausieren unter", 10.0, 60.0, 5.0, 25.0, " FPS"));
      volatile boolean low;
      private long lowSince = 0L;
      private long okSince = 0L;
      private long nextGc = 0L;
      private long nextCheck = 0L;

      Guard() {
         super(
            "guard",
            "Freeze-Schutz",
            "Gibt bei knappem RAM Speicher frei und pausiert Extras bei Rucklern, damit Spiel und Client nicht einfrieren oder abstürzen",
            Category.PERFORMANCE
         );
         this.icon("shield");
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         long var1 = System.currentTimeMillis();
         if (var1 >= this.nextCheck) {
            this.nextCheck = var1 + 500L;
            if (this.memory.get()) {
               Runtime var3 = Runtime.getRuntime();
               long var4 = var3.totalMemory() - var3.freeMemory();
               long var6 = var3.maxMemory();
               if (var4 > var6 * 0.92 && var1 >= this.nextGc) {
                  this.nextGc = var1 + 45000L;
                  new Thread(System::gc, "Lego-Guard-GC").start();
                  Toasts.show("memory", "RAM fast voll", var4 / 1048576L + " / " + var6 / 1048576L + " MB - Speicher wird aufgeräumt", -13053, 3500L);
               }
            }

            if (!this.lowFps.get()) {
               this.low = false;
            } else {
               int var8 = Mc.fps();
               if (var8 > 0 && var8 < this.limit.getI() && Mc.windowFocused()) {
                  this.okSince = 0L;
                  if (this.lowSince == 0L) {
                     this.lowSince = var1;
                  }

                  if (!this.low && var1 - this.lowSince > 3000L) {
                     this.low = true;
                     Toasts.show("boost", "Extras pausiert", "Wegen niedriger FPS - kommt zurück, sobald es wieder flüssig ist", -13053, 3000L);
                  }
               } else {
                  this.lowSince = 0L;
                  if (this.low) {
                     if (this.okSince == 0L) {
                        this.okSince = var1;
                     }

                     if (var1 - this.okSince > 5000L) {
                        this.low = false;
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onDisable() {
         this.low = false;
      }
   }

   public static final class Monitor extends Module {
      Monitor() {
         super("perfmonitor", "Leistungs-Monitor", "Zeigt, wie viele Millisekunden pro Bild jeder Teil des Clients kostet", Category.PERFORMANCE);
         this.icon("gauge");
         this.fresh();
      }

      public void draw(int var1, int var2) {
         int var3 = Gx.S;
         float var4 = 7.4F * var3;
         Map var5 = Perf.sections();
         int var6 = Math.round((float)(150 * var3));
         int var7 = Math.round((float)(11 * var3));
         int var8 = var7 * (var5.size() + 2) + Math.round((float)(8 * var3));
         int var9 = var1 - var6 - Math.round((float)(8 * var3));
         int var10 = var2 / 2 - var8 / 2;
         Gx.rect(var9, var10, var6, var8, Math.round((float)(6 * var3)), -938865904);
         int var11 = Mc.fps();
         Gx.text(
            "FPS " + var11 + "  ·  " + String.format(Locale.ROOT, "%.1f ms", 1000.0 / Math.max(1, var11)),
            var9 + 6 * var3,
            var10 + 4 * var3,
            var4,
            3,
            var11 >= 60 ? -14756000 : (var11 >= 30 ? -13053 : -45730)
         );
         int var12 = var10 + 4 * var3 + var7 + 2 * var3;

         for (Entry var14 : var5.entrySet()) {
            double var15 = ((double[])var14.getValue())[0];
            int var17 = var15 < 1.0 ? -4670008 : (var15 < 4.0 ? -13053 : -45730);
            Gx.text((String)var14.getKey(), var9 + 6 * var3, var12, var4, 2, -1512980);
            Gx.textRight(String.format(Locale.ROOT, "%.2f ms", var15), var9 + var6 - 6 * var3, var12 + var4 * 0.45F, var4, 3, var17);
            var12 += var7;
         }
      }
   }

   public static final class ParticleLimit extends Module {
      final Setting.Num perTick = this.add(new Setting.Num("Max. neue pro Tick", 20.0, 400.0, 10.0, 120.0, ""));
      private int count = 0;
      private long tickId;

      ParticleLimit() {
         super("particlelimit", "Partikel-Limit", "Begrenzt Partikel-Explosionen (TNT, Farmen) - weniger Ruckler", Category.PERFORMANCE);
         this.icon("particles");
         this.fresh();
      }

      @Override
      public void tick() {
         this.count = 0;
      }

      public boolean allow() {
         long var1 = System.currentTimeMillis() / 50L;
         if (var1 != this.tickId) {
            this.tickId = var1;
            this.count = 0;
         }

         boolean var3 = Performance.boost != null && Performance.boost.extreme() || Performance.lowMode();
         return !this.enabled && !var3 ? true : ++this.count <= (this.enabled ? (var3 ? Math.min(40, this.perTick.getI()) : this.perTick.getI()) : 40);
      }
   }
}
