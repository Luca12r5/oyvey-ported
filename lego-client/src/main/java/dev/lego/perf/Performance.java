package dev.lego.perf;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Gx;
import dev.lego.ui.Toasts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
   public static Performance.Moltron moltron;
   public static Performance.EntityCulling culling;
   public static Performance.ParticleLimit particles;
   public static Performance.BackgroundFps background;
   public static Performance.FreeMemory memory;
   public static Performance.Monitor monitor;
   public static Performance.Guard guard;

   private Performance() {
   }

   public static boolean lowMode() {
      return guard != null && guard.enabled && guard.low || potato();
   }

   public static boolean potato() {
      return moltron != null && moltron.enabled;
   }

   public static void registerAll() {
      boost = Modules.register(new Performance.FpsBoost());
      moltron = Modules.register(new Performance.Moltron());
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

      public int limit(int vanilla) {
         return this.enabled && !Mc.windowFocused() ? Math.min(vanilla, this.fps.getI()) : vanilla;
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
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         this.frame++;
         if (this.frame % 200L == 0L) {
            this.cache.entrySet().removeIf(e -> this.frame - e.getValue()[0] > 100L);
         }
      }

      public boolean shouldRender(Entity e, double cx, double cy, double cz) {
         if (e == Mc.player()) {
            return true;
         } else if (e instanceof Player && this.keepPlayers.get()) {
            return true;
         } else if (e.isCurrentlyGlowing()) {
            return true;
         } else {
            double dx = Mc.x(e) - cx;
            double dy = Mc.y(e) + Mc.height(e) / 2.0F - cy;
            double dz = Mc.z(e) - cz;
            double d2 = dx * dx + dy * dy + dz * dz;
            String t = Mc.typeId(e);
            boolean small = t.equals("item") || t.equals("experience_orb") || t.equals("arrow");
            boolean ext = Performance.boost != null && Performance.boost.extreme();
            double max = small
               ? (ext ? Math.min(16.0, this.itemDist.get()) : this.itemDist.get())
               : (ext ? Math.min(48.0, this.mobDist.get()) : this.mobDist.get());
            if (!(e instanceof Player) && d2 > max * max) {
               return false;
            } else if (this.occlusion.get() && !(d2 < 16.0)) {
               float w = Mc.width(e);
               float h = Mc.height(e);
               if (!(w > 3.0F) && !(h > 4.0F)) {
                  int id = Mc.id(e);
                  long[] c = this.cache.get(id);
                  if (c != null && this.frame - c[0] < 4L) {
                     return c[1] == 1L;
                  } else {
                     ClientLevel world = Mc.world();
                     boolean vis = world == null
                        || visible(world, cx, cy, cz, Mc.x(e), Mc.y(e) + h * 0.9, Mc.z(e))
                        || visible(world, cx, cy, cz, Mc.x(e), Mc.y(e) + h * 0.2, Mc.z(e))
                        || visible(world, cx, cy, cz, Mc.x(e) + w / 2.0F, Mc.y(e) + h / 2.0F, Mc.z(e) + w / 2.0F)
                        || visible(world, cx, cy, cz, Mc.x(e) - w / 2.0F, Mc.y(e) + h / 2.0F, Mc.z(e) - w / 2.0F);
                     this.cache.put(id, new long[]{this.frame, vis ? 1L : 0L});
                     return vis;
                  }
               } else {
                  return true;
               }
            } else {
               return true;
            }
         }
      }

      private static boolean visible(ClientLevel w, double x0, double y0, double z0, double x1, double y1, double z1) {
         double dx = x1 - x0;
         double dy = y1 - y0;
         double dz = z1 - z0;
         int x = (int)Math.floor(x0);
         int y = (int)Math.floor(y0);
         int z = (int)Math.floor(z0);
         int ex = (int)Math.floor(x1);
         int ey = (int)Math.floor(y1);
         int ez = (int)Math.floor(z1);
         int sx = dx > 0.0 ? 1 : -1;
         int sy = dy > 0.0 ? 1 : -1;
         int sz = dz > 0.0 ? 1 : -1;
         double tdx = dx == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
         double tdy = dy == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dy);
         double tdz = dz == 0.0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);
         double tmx = dx == 0.0 ? Double.MAX_VALUE : (sx > 0 ? x + 1 - x0 : x0 - x) * tdx;
         double tmy = dy == 0.0 ? Double.MAX_VALUE : (sy > 0 ? y + 1 - y0 : y0 - y) * tdy;
         double tmz = dz == 0.0 ? Double.MAX_VALUE : (sz > 0 ? z + 1 - z0 : z0 - z) * tdz;
         MutableBlockPos pos = new MutableBlockPos();

         for (int i = 0; i < 256; i++) {
            if (x == ex && y == ey && z == ez) {
               return true;
            }

            if (tmx < tmy && tmx < tmz) {
               x += sx;
               tmx += tdx;
            } else if (tmy < tmz) {
               y += sy;
               tmy += tdy;
            } else {
               z += sz;
               tmz += tdz;
            }

            if (x == ex && y == ey && z == ez) {
               return true;
            }

            pos.set(x, y, z);
            if (w.getBlockState(pos).isSolidRender()) {
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
      final Setting.Bool bobbing = this.add(new Setting.Bool("View-Bobbing aus", false));
      final Setting.Bool fovEffect = this.add(new Setting.Bool("FOV-Effekte aus", true));
      private final Map<String, String> saved = new HashMap<>();
      private final Map<String, Object[]> opts = new LinkedHashMap<>();
      private static final Performance.FpsBoost.Parse INT = s -> (int)Double.parseDouble(s);
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
            public void load(Object o) {
               if (o instanceof Map) {
                  FpsBoost.this.saved.clear();

                  for (Entry<String, Object> e : ((Map<String, Object>)o).entrySet()) {
                     FpsBoost.this.saved.put(e.getKey(), String.valueOf(e.getValue()));
                  }
               }
            }
         });
      }

      private void set(String key, OptionInstance opt, Object value, Performance.FpsBoost.Parse parse) {
         if (opt != null) {
            this.opts.put(key, new Object[]{opt, parse});
            Object cur = opt.get();
            if (!this.saved.containsKey(key)) {
               this.saved.put(key, cur instanceof Enum ? ((Enum)cur).name() : String.valueOf(cur));
            }

            try {
               opt.set(value);
            } catch (Throwable var7) {
               LegoClient.LOG("FPS-Boost " + key + ": " + var7);
            }
         }
      }

      private void capInt(String key, OptionInstance opt, int max) {
         if (opt != null) {
            Object cur = opt.get();
            if (cur instanceof Integer && (Integer)cur > max) {
               this.set(key, opt, max, INT);
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
         Options o = Mc.options();

         try {
            int l = this.level.index;
            this.set("vsync", o.enableVsync(), Boolean.FALSE, BOOL);
            this.set("maxFps", o.framerateLimit(), 260, INT);
            this.set("particles", o.particles(), l == 0 ? ParticleStatus.DECREASED : ParticleStatus.MINIMAL, s -> ParticleStatus.valueOf(s));
            if (this.clouds.get()) {
               this.set("clouds", o.cloudStatus(), l == 0 ? CloudStatus.FAST : CloudStatus.OFF, s -> CloudStatus.valueOf(s));
            }

            if (this.shadows.get()) {
               this.set("shadows", o.entityShadows(), Boolean.FALSE, BOOL);
            }

            if (this.bobbing.get() || l >= 2) {
               this.set("bobView", o.toggleCrouch(), Boolean.FALSE, BOOL);
            }

            if (this.fovEffect.get()) {
               this.set("fovEffects", o.fovEffectScale(), 0.0, DBL);
            }

            this.set("blend", o.biomeBlendRadius(), l == 0 ? 2 : (l == 1 ? 1 : 0), INT);
            this.set("menuBlur", o.menuBackgroundBlurriness(), 0, INT);
            this.set("improvedTransparency", o.improvedTransparency(), Boolean.FALSE, BOOL);
            this.capInt("weather", o.weatherRadius(), l == 0 ? 8 : 5);
            this.capInt("anisotropy", o.maxAnisotropyBit(), l == 0 ? 2 : 1);
            if (l >= 3) {
               this.set("particles", o.particles(), ParticleStatus.MINIMAL, s -> ParticleStatus.valueOf(s));
               this.set("clouds", o.cloudStatus(), CloudStatus.OFF, s -> CloudStatus.valueOf(s));
               this.set("shadows", o.entityShadows(), Boolean.FALSE, BOOL);
               this.set("chunkFade", o.chunkSectionFadeInTime(), 0.0, DBL);
               this.capInt("weather", o.weatherRadius(), 3);
               this.capInt("renderDist", o.renderDistance(), 6);
               this.capInt("simDist", o.simulationDistance(), 5);
               this.capInt("cloudDist", o.cloudRange(), 2);
               this.set("entityDist", o.entityDistanceScaling(), 0.5, DBL);
               this.set("ao", o.ambientOcclusion(), Boolean.FALSE, BOOL);
               this.set("cutoutLeaves", o.cutoutLeaves(), Boolean.FALSE, BOOL);
               this.capInt("mipmaps", o.mipmapLevels(), 0);
               this.capInt("anisotropy", o.maxAnisotropyBit(), 1);
            } else if (l >= 1) {
               this.set("entityDist", o.entityDistanceScaling(), l == 1 ? 0.75 : 0.5, DBL);
               this.set("ao", o.ambientOcclusion(), Boolean.FALSE, BOOL);
               this.set("cutoutLeaves", o.cutoutLeaves(), Boolean.FALSE, BOOL);
               this.capInt("mipmaps", o.mipmapLevels(), l == 1 ? 2 : 0);
               this.capInt("renderDist", o.renderDistance(), l == 1 ? 12 : 8);
               this.capInt("simDist", o.simulationDistance(), l == 1 ? 8 : 5);
               this.capInt("cloudDist", o.cloudRange(), 8);
            }

            o.save();
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
            Options o = Mc.options();
            Map<String, Object[]> all = new LinkedHashMap<>(this.opts);
            Object[][] known = new Object[][]{
               {"vsync", o.enableVsync(), BOOL},
               {"maxFps", o.framerateLimit(), INT},
               {"particles", o.particles(), (Performance.FpsBoost.Parse)x -> ParticleStatus.valueOf(x)},
               {"clouds", o.cloudStatus(), (Performance.FpsBoost.Parse)x -> CloudStatus.valueOf(x)},
               {"shadows", o.entityShadows(), BOOL},
               {"bobView", o.toggleCrouch(), BOOL},
               {"fovEffects", o.fovEffectScale(), DBL},
               {"blend", o.biomeBlendRadius(), INT},
               {"menuBlur", o.menuBackgroundBlurriness(), INT},
               {"improvedTransparency", o.improvedTransparency(), BOOL},
               {"weather", o.weatherRadius(), INT},
               {"anisotropy", o.maxAnisotropyBit(), INT},
               {"entityDist", o.entityDistanceScaling(), DBL},
               {"ao", o.ambientOcclusion(), BOOL},
               {"cutoutLeaves", o.cutoutLeaves(), BOOL},
               {"chunkFade", o.chunkSectionFadeInTime(), DBL},
               {"mipmaps", o.mipmapLevels(), INT},
               {"renderDist", o.renderDistance(), INT},
               {"simDist", o.simulationDistance(), INT},
               {"cloudDist", o.cloudRange(), INT}
            };

            for (Object[] k : known) {
               all.putIfAbsent((String)k[0], new Object[]{k[1], k[2]});
            }

            for (Entry<String, String> e : this.saved.entrySet()) {
               Object[] op = all.get(e.getKey());
               if (op != null && op[0] != null) {
                  try {
                     ((OptionInstance)op[0]).set(((Performance.FpsBoost.Parse)op[1]).of(e.getValue()));
                  } catch (Throwable var9) {
                     LegoClient.LOG("FPS-Boost restore " + e.getKey() + ": " + var9);
                  }
               }
            }

            try {
               o.save();
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
         Runtime r = Runtime.getRuntime();
         long before = r.totalMemory() - r.freeMemory();
         new Thread(
               () -> {
                  System.gc();
                  long after = r.totalMemory() - r.freeMemory();
                  long freed = Math.max(0L, before - after) / 1048576L;
                  Toasts.show(
                     "memory", "RAM freigegeben", freed + " MB frei · " + after / 1048576L + " / " + r.maxMemory() / 1048576L + " MB belegt", -16735270, 3500L
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
         long now = System.currentTimeMillis();
         if (now >= this.nextCheck) {
            this.nextCheck = now + 500L;
            if (this.memory.get()) {
               Runtime r = Runtime.getRuntime();
               long used = r.totalMemory() - r.freeMemory();
               long max = r.maxMemory();
               if (used > max * 0.92 && now >= this.nextGc) {
                  this.nextGc = now + 45000L;
                  new Thread(System::gc, "Lego-Guard-GC").start();
                  Toasts.show("memory", "RAM fast voll", used / 1048576L + " / " + max / 1048576L + " MB - Speicher wird aufgeräumt", -13053, 3500L);
               }
            }

            if (!this.lowFps.get()) {
               this.low = false;
            } else {
               int fps = Mc.fps();
               if (fps > 0 && fps < this.limit.getI() && Mc.windowFocused()) {
                  this.okSince = 0L;
                  if (this.lowSince == 0L) {
                     this.lowSince = now;
                  }

                  if (!this.low && now - this.lowSince > 3000L) {
                     this.low = true;
                     Toasts.show("boost", "Extras pausiert", "Wegen niedriger FPS - kommt zurück, sobald es wieder flüssig ist", -13053, 3000L);
                  }
               } else {
                  this.lowSince = 0L;
                  if (this.low) {
                     if (this.okSince == 0L) {
                        this.okSince = now;
                     }

                     if (now - this.okSince > 5000L) {
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

   public static final class Moltron extends Module {
      final Setting.Bool killVisuals = this.add(new Setting.Bool("Visuelle Module pausieren", true));
      final Setting.Num renderDist = this.add(new Setting.Num("Render-Distanz", 2.0, 6.0, 1.0, 2.0, " Chunks"));
      final Setting.Num simDist = this.add(new Setting.Num("Sim-Distanz", 2.0, 5.0, 1.0, 2.0, " Chunks"));
      final Setting.Num entityDist = this.add(new Setting.Num("Entity-Distanz", 0.25, 1.0, 0.25, 0.25, "x"));
      private final Map<String, String> saved = new HashMap<>();
      private final Set<String> disabledMods = new HashSet<>();
      private static final Set<String> KEEP = Set.of(
         "fullbright", "zoom", "nohurtcam", "lowfire", "crosshair", "timechanger", "clearweather", "blockoutline", "damagenum"
      );
      private final Map<String, Object[]> opts = new LinkedHashMap<>();
      private static final Performance.Moltron.Parse INT = s -> (int)Double.parseDouble(s);
      private static final Performance.Moltron.Parse BOOL = Boolean::parseBoolean;
      private static final Performance.Moltron.Parse DBL = Double::parseDouble;

      Moltron() {
         super("moltron", "Moltron", "Potato-Grafik: alles auf Minimum, visuelle Module aus, GPU-Last minimal · macht aus 200 FPS 500+", Category.PERFORMANCE);
         this.icon("boost");
         this.fresh();
         Modules.persist("moltron", new Modules.Persist() {
            @Override
            public Object save() {
               Map<String, Object> m = new LinkedHashMap<>();
               m.put("opts", new LinkedHashMap<>(Moltron.this.saved));
               m.put("mods", new ArrayList<>(Moltron.this.disabledMods));
               return m;
            }

            @Override
            public void load(Object o) {
               if (o instanceof Map<?, ?> m) {
                  Moltron.this.saved.clear();
                  Moltron.this.disabledMods.clear();
                  Object os = m.get("opts");
                  if (os instanceof Map) {
                     for (Entry<String, Object> e : ((Map<String, Object>)os).entrySet()) {
                        Moltron.this.saved.put(e.getKey(), String.valueOf(e.getValue()));
                     }
                  }

                  Object ms = m.get("mods");
                  if (ms instanceof List) {
                     for (Object v : (List)ms) {
                        Moltron.this.disabledMods.add(String.valueOf(v));
                     }
                  }
               }
            }
         });
      }

      private void set(String key, OptionInstance opt, Object value, Performance.Moltron.Parse parse) {
         if (opt != null) {
            this.opts.put(key, new Object[]{opt, parse});
            Object cur = opt.get();
            if (!this.saved.containsKey(key)) {
               this.saved.put(key, cur instanceof Enum ? ((Enum)cur).name() : String.valueOf(cur));
            }

            try {
               opt.set(value);
            } catch (Throwable var7) {
               LegoClient.LOG("Moltron " + key + ": " + var7);
            }
         }
      }

      @Override
      public void onEnable() {
         if (Mc.mc() != null && Mc.options() != null) {
            Options o = Mc.options();

            try {
               this.set("vsync", o.enableVsync(), Boolean.FALSE, BOOL);
               this.set("maxFps", o.framerateLimit(), 260, INT);
               this.set("particles", o.particles(), ParticleStatus.MINIMAL, s -> ParticleStatus.valueOf(s));
               this.set("clouds", o.cloudStatus(), CloudStatus.OFF, s -> CloudStatus.valueOf(s));
               this.set("shadows", o.entityShadows(), Boolean.FALSE, BOOL);
               this.set("bobView", o.toggleCrouch(), Boolean.FALSE, BOOL);
               this.set("fovEffects", o.fovEffectScale(), 0.0, DBL);
               this.set("blend", o.biomeBlendRadius(), 0, INT);
               this.set("menuBlur", o.menuBackgroundBlurriness(), 0, INT);
               this.set("improvedTransparency", o.improvedTransparency(), Boolean.FALSE, BOOL);
               this.set("weather", o.weatherRadius(), 0, INT);
               this.set("anisotropy", o.maxAnisotropyBit(), 1, INT);
               this.set("chunkFade", o.chunkSectionFadeInTime(), 0.0, DBL);
               this.set("renderDist", o.renderDistance(), this.renderDist.getI(), INT);
               this.set("simDist", o.simulationDistance(), this.simDist.getI(), INT);
               this.set("cloudDist", o.cloudRange(), 2, INT);
               this.set("entityDist", o.entityDistanceScaling(), this.entityDist.get(), DBL);
               this.set("ao", o.ambientOcclusion(), Boolean.FALSE, BOOL);
               this.set("cutoutLeaves", o.cutoutLeaves(), Boolean.FALSE, BOOL);
               this.set("mipmaps", o.mipmapLevels(), 0, INT);
               o.save();
            } catch (Throwable var4) {
               LegoClient.LOG("Moltron: " + var4);
            }

            if (this.killVisuals.get()) {
               this.disabledMods.clear();

               for (Module m : Modules.ALL) {
                  if (m != this && m.enabled && (m.category == Category.VISUALS || m.category == Category.WORLD_FX) && !KEEP.contains(m.id)) {
                     this.disabledMods.add(m.id);
                     m.setEnabled(false);
                  }
               }

               if (Performance.boost != null && Performance.boost.enabled) {
                  this.disabledMods.add(Performance.boost.id);
                  Performance.boost.setEnabled(false);
               }
            }

            new Thread(System::gc, "Moltron-GC").start();
            Modules.scheduleSave();
            Toasts.show(
               "moltron",
               "MOLTRON AN",
               "Potato-Grafik · RD " + this.renderDist.getI() + " · " + this.disabledMods.size() + " Module pausiert",
               -14756000,
               4000L
            );
         }
      }

      @Override
      public void onDisable() {
         if (Mc.mc() != null && Mc.options() != null) {
            Options o = Mc.options();
            Map<String, Object[]> all = new LinkedHashMap<>(this.opts);
            Object[][] known = new Object[][]{
               {"vsync", o.enableVsync(), BOOL},
               {"maxFps", o.framerateLimit(), INT},
               {"particles", o.particles(), (Performance.Moltron.Parse)x -> ParticleStatus.valueOf(x)},
               {"clouds", o.cloudStatus(), (Performance.Moltron.Parse)x -> CloudStatus.valueOf(x)},
               {"shadows", o.entityShadows(), BOOL},
               {"bobView", o.toggleCrouch(), BOOL},
               {"fovEffects", o.fovEffectScale(), DBL},
               {"blend", o.biomeBlendRadius(), INT},
               {"menuBlur", o.menuBackgroundBlurriness(), INT},
               {"improvedTransparency", o.improvedTransparency(), BOOL},
               {"weather", o.weatherRadius(), INT},
               {"anisotropy", o.maxAnisotropyBit(), INT},
               {"entityDist", o.entityDistanceScaling(), DBL},
               {"ao", o.ambientOcclusion(), BOOL},
               {"cutoutLeaves", o.cutoutLeaves(), BOOL},
               {"chunkFade", o.chunkSectionFadeInTime(), DBL},
               {"mipmaps", o.mipmapLevels(), INT},
               {"renderDist", o.renderDistance(), INT},
               {"simDist", o.simulationDistance(), INT},
               {"cloudDist", o.cloudRange(), INT}
            };

            for (Object[] k : known) {
               all.putIfAbsent((String)k[0], new Object[]{k[1], k[2]});
            }

            for (Entry<String, String> e : this.saved.entrySet()) {
               Object[] op = all.get(e.getKey());
               if (op != null && op[0] != null) {
                  try {
                     ((OptionInstance)op[0]).set(((Performance.Moltron.Parse)op[1]).of(e.getValue()));
                  } catch (Throwable var9) {
                     LegoClient.LOG("Moltron restore " + e.getKey() + ": " + var9);
                  }
               }
            }

            try {
               o.save();
            } catch (Throwable var8) {
            }

            int reEnabled = 0;

            for (String id : this.disabledMods) {
               Module m = Modules.get(id);
               if (m != null && !m.enabled) {
                  m.setEnabled(true);
                  reEnabled++;
               }
            }

            this.saved.clear();
            this.disabledMods.clear();
            Modules.scheduleSave();
            Toasts.show("moltron", "MOLTRON AUS", "Grafik zurückgesetzt · " + reEnabled + " Module reaktiviert", -7696487, 3000L);
         } else {
            this.disabledMods.clear();
            this.saved.clear();
         }
      }

      public double blockEntityDist() {
         return this.enabled ? 16.0 : 0.0;
      }

      public void drawHud(int W, int H) {
         if (this.enabled) {
            int S = Gx.S;
            float fs = 8.0F * S;
            int fps = Mc.fps();
            String txt = "POTATO  " + fps + " FPS";
            int tw = Math.round(Gx.width(txt, fs, 3)) + Math.round((float)(16 * S));
            int h = Math.round((float)(18 * S));
            int r = Math.round((float)(6 * S));
            int x = W - tw - Math.round((float)(8 * S));
            int y = Math.round((float)(8 * S));
            Gx.rect(x, y, tw, h, r, -871296751);
            int col = fps >= 240 ? -14756000 : (fps >= 120 ? -15043 : (fps >= 60 ? -27392 : -45730));
            Gx.rect(x, y, Math.round((float)(4 * S)), h, r, col);
            Gx.textCenter(txt, x + tw / 2.0F + Math.round((float)(2 * S)), y + h / 2.0F, fs, 3, col);
         }
      }

      private interface Parse {
         Object of(String var1);
      }
   }

   public static final class Monitor extends Module {
      Monitor() {
         super("perfmonitor", "Leistungs-Monitor", "Zeigt, wie viele Millisekunden pro Bild jeder Teil des Clients kostet", Category.PERFORMANCE);
         this.icon("gauge");
         this.fresh();
      }

      public void draw(int W, int H) {
         int S = Gx.S;
         float fs = 7.4F * S;
         Map<String, double[]> m = Perf.sections();
         int w = Math.round((float)(150 * S));
         int lh = Math.round((float)(11 * S));
         int h = lh * (m.size() + 2) + Math.round((float)(8 * S));
         int x = W - w - Math.round((float)(8 * S));
         int y = H / 2 - h / 2;
         Gx.rect(x, y, w, h, Math.round((float)(6 * S)), -938865904);
         int fps = Mc.fps();
         Gx.text(
            "FPS " + fps + "  ·  " + String.format(Locale.ROOT, "%.1f ms", 1000.0 / Math.max(1, fps)),
            x + 6 * S,
            y + 4 * S,
            fs,
            3,
            fps >= 60 ? -14756000 : (fps >= 30 ? -13053 : -45730)
         );
         int yy = y + 4 * S + lh + 2 * S;

         for (Entry<String, double[]> e : m.entrySet()) {
            double v = e.getValue()[0];
            int col = v < 1.0 ? -4670008 : (v < 4.0 ? -13053 : -45730);
            Gx.text(e.getKey(), x + 6 * S, yy, fs, 2, -1512980);
            Gx.textRight(String.format(Locale.ROOT, "%.2f ms", v), x + w - 6 * S, yy + fs * 0.45F, fs, 3, col);
            yy += lh;
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
         this.enabled = true;
         this.fresh();
      }

      @Override
      public void tick() {
         this.count = 0;
      }

      public boolean allow() {
         long id = System.currentTimeMillis() / 50L;
         if (id != this.tickId) {
            this.tickId = id;
            this.count = 0;
         }

         boolean ext = Performance.boost != null && Performance.boost.extreme() || Performance.lowMode();
         return !this.enabled && !ext ? true : ++this.count <= (this.enabled ? (ext ? Math.min(40, this.perTick.getI()) : this.perTick.getI()) : 40);
      }
   }
}
