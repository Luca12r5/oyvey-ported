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
      add(
         k(
               "snowfall2",
               "Schneeflocken-Wirbel",
               "Dicke Schneeflocken tanzen um dich herum",
               "snow",
               new String[]{"snow"},
               90,
               14.0F,
               0.5F,
               7.0F,
               0.14F,
               1.0F,
               16777215
            )
            .rotate()
            .fall(0.5F)
            .wander(0.7F)
            .life(10.0F)
      );
      add(
         k("ashfall", "Aschregen", "Graue Asche rieselt vom Himmel", "cloud", new String[]{"ash"}, 110, 14.0F, 3.0F, 10.0F, 0.1F, 1.0F, 11579568, 7368816)
            .rotate()
            .fall(0.6F)
            .wander(0.5F)
            .life(12.0F)
      );
      add(
         k(
               "glitter",
               "Glitzerregen",
               "Funkelnder Glitzer fällt langsam herab",
               "sparkle",
               new String[]{"glitter"},
               120,
               12.0F,
               2.0F,
               8.0F,
               0.07F,
               1.0F,
               16770442,
               16777215,
               12575743
            )
            .glow()
            .fall(0.35F)
            .wander(0.4F)
            .life(10.0F)
      );
      add(
         k(
               "stardust",
               "Sternenstaub",
               "Feiner Sternenstaub schwebt nachts um dich",
               "star",
               new String[]{"stardust"},
               80,
               14.0F,
               0.5F,
               6.0F,
               0.12F,
               0.5F,
               16777215,
               13623551,
               16771248
            )
            .glow()
            .night()
            .wander(0.5F)
            .life(14.0F)
      );
      add(
         k(
               "lanternsred",
               "Rote Laternen",
               "Rote Himmelslaternen steigen auf",
               "light",
               new String[]{"lantern"},
               14,
               20.0F,
               1.0F,
               6.0F,
               0.55F,
               0.4F,
               16738906,
               16751210
            )
            .glow()
            .rise(0.35F)
            .wander(0.25F)
            .life(20.0F)
            .night()
      );
      add(
         k(
               "bubblesrise",
               "Aufsteigende Blasen",
               "Schimmernde Blasen steigen vom Boden auf",
               "rain",
               new String[]{"bubble"},
               30,
               10.0F,
               0.0F,
               2.0F,
               0.25F,
               0.5F,
               16777215,
               12578815
            )
            .rise(0.6F)
            .wander(0.3F)
            .life(8.0F)
      );
      add(
         k(
               "bluebutterflies",
               "Blaue Schmetterlinge",
               "Blau schimmernde Schmetterlinge flattern um dich",
               "wing",
               new String[]{"butterfly0", "butterfly1"},
               10,
               10.0F,
               0.3F,
               3.0F,
               0.35F,
               1.6F,
               6994175,
               3832575
            )
            .flap(9.0F)
            .wander(2.0F)
            .day()
      );
      add(
         k(
               "pinkbutterflies",
               "Rosa Schmetterlinge",
               "Rosa Schmetterlinge tanzen über die Wiese",
               "wing",
               new String[]{"butterfly0", "butterfly1"},
               10,
               10.0F,
               0.3F,
               3.0F,
               0.35F,
               1.6F,
               16751296,
               16738976
            )
            .flap(9.0F)
            .wander(2.0F)
            .day()
      );
      add(
         k(
               "goldfireflies",
               "Goldene Glühwürmchen",
               "Warm leuchtende Glühwürmchen in der Nacht",
               "light",
               new String[]{"core"},
               40,
               14.0F,
               0.4F,
               3.5F,
               0.09F,
               0.6F,
               16765503,
               16756784
            )
            .glow()
            .night()
            .wander(1.0F)
            .life(9.0F)
      );
      add(
         k(
               "bluefireflies",
               "Blaue Glühwürmchen",
               "Kühle blaue Lichter schweben nachts herum",
               "light",
               new String[]{"core"},
               40,
               14.0F,
               0.4F,
               3.5F,
               0.09F,
               0.6F,
               8189951,
               5220607
            )
            .glow()
            .night()
            .wander(1.0F)
            .life(9.0F)
      );
      add(
         k(
               "greenspores",
               "Waldsporen",
               "Grüne Sporen schweben wie im Märchenwald",
               "leaf",
               new String[]{"core"},
               70,
               12.0F,
               0.3F,
               4.0F,
               0.06F,
               0.4F,
               12123983,
               7004282
            )
            .glow()
            .wander(0.4F)
            .rise(0.05F)
            .life(12.0F)
      );
      add(
         k(
               "crimsonspores",
               "Karmesin-Sporen",
               "Rote Sporen wie im Karmesinwald",
               "flame",
               new String[]{"core"},
               70,
               12.0F,
               0.3F,
               4.0F,
               0.06F,
               0.4F,
               16726586,
               16742986
            )
            .glow()
            .wander(0.4F)
            .rise(0.05F)
            .life(12.0F)
      );
      add(
         k(
               "soulfire",
               "Seelenfeuer",
               "Blaue Seelenflammen-Funken steigen auf",
               "flame",
               new String[]{"core"},
               50,
               10.0F,
               0.0F,
               2.0F,
               0.08F,
               1.0F,
               5234943,
               2795775
            )
            .glow()
            .rise(0.9F)
            .wander(0.6F)
            .life(5.0F)
      );
      add(
         k("petalrain", "Rosenblätter", "Rote Rosenblätter fallen sanft", "heart", new String[]{"petal"}, 50, 12.0F, 3.0F, 9.0F, 0.2F, 1.0F, 16731501, 14100029)
            .rotate()
            .fall(0.45F)
            .wander(0.9F)
            .life(12.0F)
      );
      add(
         k(
               "autumn",
               "Herbstblätter",
               "Orange und rote Blätter fallen von oben",
               "leaf",
               new String[]{"leaf"},
               60,
               14.0F,
               3.0F,
               10.0F,
               0.25F,
               1.0F,
               14715434,
               13123626,
               15777856
            )
            .rotate()
            .fall(0.55F)
            .wander(1.0F)
            .life(12.0F)
      );
      add(
         k(
               "goldconfetti",
               "Goldkonfetti",
               "Goldenes Konfetti für besondere Momente",
               "trophy",
               new String[]{"confetti"},
               120,
               12.0F,
               4.0F,
               12.0F,
               0.12F,
               1.0F,
               16765503,
               16773280,
               15251756
            )
            .rotate()
            .fall(1.1F)
            .wander(1.2F)
            .life(8.0F)
      );
      add(
         k(
               "lovehearts",
               "Liebesherzen",
               "Viele kleine Herzen schweben um dich",
               "heart",
               new String[]{"heart"},
               30,
               6.0F,
               0.2F,
               2.5F,
               0.18F,
               0.5F,
               16722522,
               16747200,
               16777215
            )
            .rise(0.3F)
            .wander(0.5F)
            .life(7.0F)
      );
      add(
         k(
               "crystals2",
               "Eiskristalle",
               "Glitzernde Eiskristalle drehen sich in der Luft",
               "gem",
               new String[]{"shard"},
               18,
               12.0F,
               0.8F,
               4.0F,
               0.3F,
               0.3F,
               10479871,
               16777215
            )
            .rotate()
            .wander(0.3F)
            .glow()
      );
      add(
         k(
               "magicnotes",
               "Leuchtende Noten",
               "Leuchtende Musiknoten kreisen um dich",
               "music",
               new String[]{"note"},
               20,
               6.0F,
               0.5F,
               2.8F,
               0.25F,
               1.2F,
               16765503,
               8189951
            )
            .glow()
            .swirl()
            .life(8.0F)
      );
      add(
         k(
               "seeds2",
               "Löwenzahn-Wind",
               "Viele Löwenzahn-Samen fliegen im Wind vorbei",
               "leaf",
               new String[]{"seed"},
               70,
               20.0F,
               0.5F,
               7.0F,
               0.24F,
               1.5F,
               16777215
            )
            .rotate()
            .wander(1.6F)
            .rise(0.04F)
            .life(14.0F)
      );
   }

   private static WorldFx.Kind k(
      String id,
      String name,
      String desc,
      String icon,
      String[] sprites,
      int count,
      float radius,
      float hMin,
      float hMax,
      float size,
      float speed,
      int... colors
   ) {
      WorldFx.Kind k = new WorldFx.Kind();
      k.id = id;
      k.name = name;
      k.desc = desc;
      k.icon = icon;
      k.sprites = sprites;
      k.defCount = count;
      k.radius = radius;
      k.hMin = hMin;
      k.hMax = hMax;
      k.size = size;
      k.speed = speed;
      k.colors = colors;
      return k;
   }

   private static void add(WorldFx.Kind k) {
      ALL.add(Modules.register(new WorldFx.Fly(k)));
   }

   static void render() {
      for (WorldFx.Fly f : ALL) {
         if (f.enabled) {
            f.render();
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

      Fly(WorldFx.Kind k) {
         super("fx_" + k.id, k.name, k.desc, Category.WORLD_FX);
         this.k = k;
         this.icon(k.icon);
         this.fresh();
         this.amount = this.add(new Setting.Num("Menge", 10.0, 300.0, 10.0, 100.0, "%"));
         this.size = this.add(new Setting.Num("Größe", 0.5, 2.0, 0.1, 1.0, "x"));
      }

      @Override
      public void onDisable() {
         this.ps.clear();
      }

      void render() {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double vis = 1.0;
            if (this.k.night) {
               vis = Ambient.night();
            }

            if (this.k.day) {
               vis = 1.0 - Ambient.night();
            }

            if (!(vis < 0.02)) {
               long now = System.nanoTime();
               float dt = this.last == 0L ? 0.016F : Math.min(0.1F, (float)(now - this.last) / 1.0E9F);
               this.last = now;
               double px = Mc.x(me);
               double py = Mc.y(me);
               double pz = Mc.z(me);
               int want = Math.max(
                  1, Math.round(this.k.defCount * this.amount.getF() / 100.0F * (Performance.boost == null ? 1.0F : Performance.boost.fxScale()))
               );
               int spawn = 0;

               while (this.ps.size() < want && spawn++ < 6) {
                  this.ps.add(this.spawn(px, py, pz, this.ps.isEmpty()));
               }

               double t = System.currentTimeMillis() / 1000.0;
               Identifier[] tex = new Identifier[this.k.sprites.length];

               for (int i = 0; i < tex.length; i++) {
                  tex[i] = Ambient.tex(this.k.sprites[i]);
               }

               Identifier glow = this.k.glow ? Ambient.tex("glow") : null;

               for (int i = this.ps.size() - 1; i >= 0; i--) {
                  double[] q = this.ps.get(i);
                  q[6] += dt;
                  double s = q[8];
                  double w = this.k.wander * this.k.speed;
                  if (this.k.sky) {
                     double dir = s * 0.01 + t * 0.03;
                     q[3] = Math.cos(dir) * this.k.speed;
                     q[5] = Math.sin(dir) * this.k.speed;
                     q[4] = Math.sin(t * 0.5 + s) * 0.3;
                  } else if (this.k.swirl) {
                     double a = Math.atan2(q[2] - pz, q[0] - px) + 1.2;
                     q[3] += (Math.cos(a) * this.k.speed - q[3]) * dt;
                     q[5] += (Math.sin(a) * this.k.speed - q[5]) * dt;
                     q[4] += (Math.sin(t * 1.3 + s) * 0.6 - q[4]) * dt;
                  } else {
                     q[3] += (Math.sin(t * 0.7 * this.k.speed + s) * w - q[3]) * dt * 1.5;
                     q[5] += (Math.cos(t * 0.6 * this.k.speed + s * 1.3) * w - q[5]) * dt * 1.5;
                     q[4] += (Math.sin(t * 0.9 + s * 2.0) * 0.35 * w + this.k.rise - this.k.fall - q[4]) * dt * 1.5;
                  }

                  q[0] += q[3] * dt;
                  q[1] += q[4] * dt;
                  q[2] += q[5] * dt;
                  double dx = q[0] - px;
                  double dz = q[2] - pz;
                  if (!(q[6] > q[7]) && !(dx * dx + dz * dz > this.k.radius * 1.4 * (this.k.radius * 1.4)) && i < want) {
                     double fade = Math.min(1.0, Math.min(q[6] / 1.2, (q[7] - q[6]) / 1.2)) * vis;
                     int rgb = (int)q[9];
                     double sz = this.k.size * this.size.get() * (0.75 + s % 1.0 * 0.5);
                     int frame = tex.length > 1 ? (int)((t * this.k.flapHz + s) % tex.length) : 0;
                     if (glow != null) {
                        R3.glowBillboard(glow, q[0], q[1], q[2], sz * 3.2, R3.argb(rgb, 0.35 * fade));
                     }

                     if (this.k.trail) {
                        for (int j = 1; j <= 4; j++) {
                           R3.glowBillboard(
                              tex[0],
                              q[0] - q[3] * 0.06 * j,
                              q[1] - q[4] * 0.06 * j,
                              q[2] - q[5] * 0.06 * j,
                              sz * (1.0 - j * 0.18),
                              R3.argb(rgb, 0.4 * fade * (1.0 - j * 0.2))
                           );
                        }
                     }

                     if (this.k.glow) {
                        R3.glowBillboard(tex[frame], q[0], q[1], q[2], sz, R3.argb(rgb, fade));
                     } else if (this.k.rotate) {
                        R3.billboardRot(tex[frame], q[0], q[1], q[2], sz, t * 1.4 + s, R3.argb(rgb, fade));
                     } else {
                        R3.billboard(tex[frame], q[0], q[1], q[2], sz, R3.argb(rgb, fade));
                     }
                  } else {
                     this.ps.remove(i);
                  }
               }
            }
         }
      }

      private double[] spawn(double px, double py, double pz, boolean first) {
         double a = this.rnd.nextDouble() * Math.PI * 2.0;
         double r = Math.sqrt(this.rnd.nextDouble()) * this.k.radius;
         double x = px + Math.cos(a) * r;
         double z = pz + Math.sin(a) * r;
         int ground = Ambient.top(x, z);
         double base = this.k.sky ? py : (!(ground > py + 6.0) && !(ground < py - 10.0) ? ground : py);
         double y = base + this.k.hMin + this.rnd.nextDouble() * (this.k.hMax - this.k.hMin);
         return new double[]{
            x,
            y,
            z,
            0.0,
            0.0,
            0.0,
            first ? this.rnd.nextDouble() * this.k.life * 0.5 : 0.0,
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

      WorldFx.Kind flap(float hz) {
         this.flapHz = hz;
         return this;
      }

      WorldFx.Kind sky() {
         this.sky = true;
         return this;
      }

      WorldFx.Kind wander(float w) {
         this.wander = w;
         return this;
      }

      WorldFx.Kind life(float l) {
         this.life = l;
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

      WorldFx.Kind rise(float r) {
         this.rise = r;
         return this;
      }

      WorldFx.Kind fall(float f) {
         this.fall = f;
         return this;
      }

      WorldFx.Kind swirl() {
         this.swirl = true;
         return this;
      }
   }
}
