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
      long now = System.nanoTime();
      float dt = last == 0L ? 0.016F : Math.min(0.1F, (float)(now - last) / 1.0E9F);
      last = now;
      if (aurora.enabled) {
         aurora.render();
      }

      if (rainbow.enabled) {
         rainbow.render();
      }

      if (lanterns.enabled) {
         lanterns.render(dt);
      }

      if (weather.enabled) {
         weather.render(dt);
      }

      if (motes.enabled) {
         motes.render(dt);
      }

      if (bubbles.enabled) {
         bubbles.render(dt);
      }

      if (butterflies.enabled) {
         butterflies.render(dt);
      }

      if (fireflies.enabled) {
         fireflies.render(dt);
      }
   }

   static Identifier tex(String id) {
      Identifier t = TEX.get(id);
      if (t == null) {
         t = Identifier.fromNamespaceAndPath("legoclient", "ambient/" + id);

         try {
            Tx.register(t, AmbientTex.paint(id), 128, 128, true);
         } catch (Throwable var3) {
            LegoClient.LOG("Ambient: " + var3);
         }

         TEX.put(id, t);
      }

      return t;
   }

   static int top(double x, double z) {
      ClientLevel w = Mc.world();
      if (w == null) {
         return -64;
      } else {
         try {
            return w.getHeight(Types.MOTION_BLOCKING, (int)Math.floor(x), (int)Math.floor(z));
         } catch (Throwable var6) {
            return -64;
         }
      }
   }

   static boolean air(double x, double y, double z) {
      ClientLevel w = Mc.world();
      if (w == null) {
         return true;
      } else {
         try {
            return w.getBlockState(BlockPos.containing(x, y, z)).getCollisionShape(w, BlockPos.containing(x, y, z)).isEmpty();
         } catch (Throwable var8) {
            return true;
         }
      }
   }

   static double night() {
      long t = Math.floorMod(Mc.timeOfDay(), 24000L);
      if (t >= 13200L && t <= 22800L) {
         return 1.0;
      } else if (t > 12200L && t < 13200L) {
         return (t - 12200L) / 1000.0;
      } else {
         return t > 22800L && t < 23800L ? 1.0 - (t - 22800L) / 1000.0 : 0.0;
      }
   }

   public static final class Aurora extends Module {
      final Setting.Mode palette = this.add(new Setting.Mode("Farben", 0, "Grün", "Lila & Pink", "Regenbogen"));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 60.0, "%"));

      Aurora() {
         super("aurora", "Nordlicht", "Wehende Polarlicht-Vorhänge am Nachthimmel (echt 3D)", Category.WORLD_FX);
         this.icon("sky");
         this.fresh();
      }

      void render() {
         double vis = Ambient.night() * this.strength.get() / 100.0;
         if (!(vis < 0.01)) {
            double t = System.currentTimeMillis() / 1000.0;
            double cx = R3.camX();
            double cy = R3.camY();
            double cz = R3.camZ();
            double far = 160.0;

            try {
               far = Math.min(far, dev.lego.compat.McCompat.depthFar(Mc.mc().gameRenderer) * 0.45);
            } catch (Throwable var50) {
            }

            for (int band = 0; band < 3; band++) {
               double z0 = -far * (0.55 + band * 0.12);
               double base = cy + far * (0.32 + band * 0.05);
               double h = far * (0.22 + band * 0.04);
               int seg = 64;

               for (int i = 0; i < seg; i++) {
                  double f0 = (double)i / seg;
                  double f1 = (double)(i + 1) / seg;
                  double x0 = (f0 - 0.5) * far * 2.4;
                  double x1 = (f1 - 0.5) * far * 2.4;
                  double w0 = Math.sin(f0 * 9.0 + t * 0.35 + band) * far * 0.08 + Math.sin(f0 * 23.0 - t * 0.6) * far * 0.02;
                  double w1 = Math.sin(f1 * 9.0 + t * 0.35 + band) * far * 0.08 + Math.sin(f1 * 23.0 - t * 0.6) * far * 0.02;
                  double i0 = 0.55 + 0.45 * Math.sin(f0 * 14.0 + t * 0.9 + band * 2);
                  double i1 = 0.55 + 0.45 * Math.sin(f1 * 14.0 + t * 0.9 + band * 2);
                  double edge0 = Math.sin(f0 * Math.PI);
                  double edge1 = Math.sin(f1 * Math.PI);
                  int top0 = this.col(f0, band, true);
                  int bot0 = this.col(f0, band, false);
                  int top1 = this.col(f1, band, true);
                  int bot1 = this.col(f1, band, false);
                  double a0 = vis * i0 * edge0 * 0.55;
                  double a1 = vis * i1 * edge1 * 0.55;
                  R3.quad(
                     cx + x0,
                     base,
                     cz + z0 + w0,
                     cx + x1,
                     base,
                     cz + z0 + w1,
                     cx + x1,
                     base + h,
                     cz + z0 + w1 * 1.3,
                     cx + x0,
                     base + h,
                     cz + z0 + w0 * 1.3,
                     R3.argb(bot0, a0),
                     R3.argb(bot1, a1),
                     R3.argb(top1, 0.0),
                     R3.argb(top0, 0.0)
                  );
               }
            }
         }
      }

      private int col(double f, int band, boolean top) {
         switch (this.palette.index) {
            case 1:
               return top ? 16732120 : 10181631;
            case 2:
               return Color.HSBtoRGB((float)(f * 1.5 + band * 0.2 + System.currentTimeMillis() / 20000.0), 0.7F, 1.0F) & 16777215;
            default:
               return top ? 9067519 : 4063136;
         }
      }
   }

   public static final class Bubbles extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 5.0, 100.0, 5.0, 30.0, ""));
      private final Ambient.Pool pool = new Ambient.Pool(100);

      Bubbles() {
         super("bubbles", "Seifenblasen", "Schillernde Seifenblasen schweben um dich", Category.WORLD_FX);
         this.icon("aura");
         this.fresh();
      }

      void render(float dt) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double px = Mc.x(me);
            double py = Mc.y(me);
            double pz = Mc.z(me);

            while (this.pool.n < this.count.getI()) {
               double[] q = this.pool.add();
               q[0] = px + (Ambient.RND.nextDouble() - 0.5) * 14.0;
               q[1] = py + Ambient.RND.nextDouble() * 2.0;
               q[2] = pz + (Ambient.RND.nextDouble() - 0.5) * 14.0;
               q[6] = 0.0;
               q[7] = 6.0 + Ambient.RND.nextDouble() * 8.0;
               q[8] = Ambient.RND.nextDouble() * 1000.0;
               q[9] = 0.5 + Ambient.RND.nextDouble();
            }

            double t = System.currentTimeMillis() / 1000.0;
            Identifier b = Ambient.tex("bubble");

            for (int i = this.pool.n - 1; i >= 0; i--) {
               double[] q = this.pool.p[i];
               q[6] += dt;
               q[1] += 0.35 * dt;
               q[0] += Math.sin(t * 0.8 + q[8]) * 0.3 * dt;
               q[2] += Math.cos(t * 0.7 + q[8]) * 0.3 * dt;
               if (!(q[6] > q[7]) && i < this.count.getI()) {
                  double fade = Math.min(1.0, Math.min(q[6] / 0.8, (q[7] - q[6]) / 0.3));
                  double wob = 1.0 + Math.sin(t * 5.0 + q[8]) * 0.04;
                  R3.billboard(b, q[0], q[1], q[2], 0.45 * q[9] * wob, R3.argb(16777215, fade));
               } else {
                  this.pool.remove(i);
               }
            }
         }
      }
   }

   public static final class Butterflies extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 2.0, 40.0, 1.0, 10.0, ""));
      private final Ambient.Pool pool = new Ambient.Pool(40);

      Butterflies() {
         super("butterflies", "Schmetterlinge", "Bunte Schmetterlinge flattern um dich herum (tagsüber)", Category.WORLD_FX);
         this.icon("wing");
         this.fresh();
      }

      void render(float dt) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double day = 1.0 - Ambient.night();
            if (!(day < 0.01)) {
               double px = Mc.x(me);
               double py = Mc.y(me);
               double pz = Mc.z(me);

               while (this.pool.n < this.count.getI()) {
                  double[] q = this.pool.add();
                  q[0] = px + (Ambient.RND.nextDouble() - 0.5) * 16.0;
                  q[1] = py + 0.5 + Ambient.RND.nextDouble() * 2.0;
                  q[2] = pz + (Ambient.RND.nextDouble() - 0.5) * 16.0;
                  q[6] = 0.0;
                  q[7] = 15.0 + Ambient.RND.nextDouble() * 20.0;
                  q[8] = Ambient.RND.nextDouble() * 1000.0;
                  q[9] = 0.7 + Ambient.RND.nextDouble() * 0.5;
               }

               double t = System.currentTimeMillis() / 1000.0;
               Identifier f0 = Ambient.tex("butterfly0");
               Identifier f1 = Ambient.tex("butterfly1");

               for (int i = this.pool.n - 1; i >= 0; i--) {
                  double[] q = this.pool.p[i];
                  q[6] += dt;
                  double s = q[8];
                  q[0] += Math.sin(t * 0.6 + s) * 0.9 * dt;
                  q[1] += Math.sin(t * 2.2 + s) * 0.5 * dt;
                  q[2] += Math.cos(t * 0.5 + s * 1.3) * 0.9 * dt;
                  double dx = q[0] - px;
                  double dz = q[2] - pz;
                  if (!(q[6] > q[7]) && !(dx * dx + dz * dz > 324.0) && i < this.count.getI()) {
                     double fade = Math.min(1.0, Math.min(q[6], q[7] - q[6])) * day;
                     boolean open = Math.sin(t * 16.0 + s) > 0.0;
                     R3.billboardRot(open ? f0 : f1, q[0], q[1], q[2], 0.32 * q[9], Math.sin(t * 0.8 + s) * 0.3, R3.argb(16777215, fade));
                  } else {
                     this.pool.remove(i);
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
         super("fireflies", "Glühwürmchen", "Leuchtende Glühwürmchen schwirren um dich herum", Category.WORLD_FX);
         this.icon("firefly");
         this.fresh();
      }

      void render(float dt) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double vis = this.nightOnly.get() ? Ambient.night() : 1.0;
            if (!(vis <= 0.01)) {
               double px = Mc.x(me);
               double py = Mc.y(me);
               double pz = Mc.z(me);
               int tries = 0;

               while (this.pool.n < this.count.getI() && tries++ < 8) {
                  double x = px + (Ambient.RND.nextDouble() - 0.5) * 28.0;
                  double z = pz + (Ambient.RND.nextDouble() - 0.5) * 28.0;
                  int ground = Ambient.top(x, z);
                  double y = (!(ground > py + 6.0) && !(ground < py - 8.0) ? ground : py) + 0.4 + Ambient.RND.nextDouble() * 2.6;
                  if (Ambient.air(x, y, z)) {
                     double[] q = this.pool.add();
                     q[0] = x;
                     q[1] = y;
                     q[2] = z;
                     q[6] = 0.0;
                     q[7] = 12.0 + Ambient.RND.nextDouble() * 20.0;
                     q[8] = Ambient.RND.nextDouble() * 1000.0;
                     q[9] = 0.7 + Ambient.RND.nextDouble() * 0.6;
                     q[3] = q[4] = q[5] = 0.0;
                  }
               }

               double t = System.currentTimeMillis() / 1000.0;
               Identifier glow = Ambient.tex("glow");
               Identifier core = Ambient.tex("core");

               for (int i = this.pool.n - 1; i >= 0; i--) {
                  double[] q = this.pool.p[i];
                  q[6] += dt;
                  double dx = q[0] - px;
                  double dz = q[2] - pz;
                  if (!(q[6] > q[7]) && !(dx * dx + dz * dz > 400.0) && i < this.count.getI()) {
                     double s = q[8];
                     q[3] += (Math.sin(t * 0.7 + s) * 0.9 - q[3]) * dt;
                     q[4] += (Math.sin(t * 0.9 + s * 1.3) * 0.35 - q[4]) * dt;
                     q[5] += (Math.cos(t * 0.6 + s * 0.7) * 0.9 - q[5]) * dt;
                     double nx = q[0] + q[3] * dt;
                     double ny = q[1] + q[4] * dt;
                     double nz = q[2] + q[5] * dt;
                     if (Ambient.air(nx, ny, nz)) {
                        q[0] = nx;
                        q[1] = ny;
                        q[2] = nz;
                     } else {
                        q[3] = -q[3];
                        q[4] = Math.abs(q[4]);
                        q[5] = -q[5];
                     }

                     double blink = 0.35 + 0.65 * Math.pow(Math.max(0.0, Math.sin(t * 1.7 + s * 3.0)), 2.0);
                     double fade = Math.min(1.0, Math.min(q[6] / 1.2, (q[7] - q[6]) / 1.5)) * vis;
                     int rgb = this.color.argb(s) & 16777215;
                     double sz = this.size.get() * q[9];
                     R3.glowBillboard(glow, q[0], q[1], q[2], 0.75 * sz, R3.argb(rgb, 0.45 * blink * fade));
                     R3.glowBillboard(core, q[0], q[1], q[2], 0.16 * sz, R3.argb(16777200, 0.95 * blink * fade));
                  } else {
                     this.pool.remove(i);
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
         super("lanterns", "Himmelslaternen", "Leuchtende Laternen steigen langsam in den Himmel", Category.WORLD_FX);
         this.icon("flame");
         this.fresh();
      }

      void render(float dt) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double vis = this.nightOnly.get() ? Ambient.night() : 1.0;
            if (!(vis <= 0.01)) {
               double px = Mc.x(me);
               double py = Mc.y(me);
               double pz = Mc.z(me);

               while (this.pool.n < this.count.getI()) {
                  double a = Ambient.RND.nextDouble() * Math.PI * 2.0;
                  double r = 20.0 + Ambient.RND.nextDouble() * 70.0;
                  double[] q = this.pool.add();
                  q[0] = px + Math.cos(a) * r;
                  q[2] = pz + Math.sin(a) * r;
                  q[1] = py - 4.0 + Ambient.RND.nextDouble() * 50.0;
                  q[6] = 0.0;
                  q[7] = 40.0 + Ambient.RND.nextDouble() * 40.0;
                  q[8] = Ambient.RND.nextDouble() * 1000.0;
                  q[9] = 0.8 + Ambient.RND.nextDouble() * 0.5;
               }

               double t = System.currentTimeMillis() / 1000.0;
               Identifier lt = Ambient.tex("lantern");
               Identifier glow = Ambient.tex("glow");

               for (int i = this.pool.n - 1; i >= 0; i--) {
                  double[] q = this.pool.p[i];
                  q[6] += dt;
                  q[1] += (0.45 + 0.1 * Math.sin(q[8])) * dt;
                  q[0] += Math.sin(t * 0.2 + q[8]) * 0.25 * dt;
                  q[2] += Math.cos(t * 0.17 + q[8]) * 0.25 * dt;
                  double dx = q[0] - px;
                  double dz = q[2] - pz;
                  if (!(q[6] > q[7]) && !(q[1] > py + 120.0) && !(dx * dx + dz * dz > 12100.0) && i < this.count.getI()) {
                     double fade = Math.min(1.0, Math.min(q[6] / 3.0, (q[7] - q[6]) / 6.0)) * vis;
                     double flick = 0.85 + 0.15 * Math.sin(t * 9.0 + q[8]);
                     R3.glowBillboard(glow, q[0], q[1], q[2], 3.2 * q[9], R3.argb(16751168, 0.3 * fade * flick));
                     R3.billboard(lt, q[0], q[1], q[2], 1.3 * q[9], R3.argb(16777215, fade));
                  } else {
                     this.pool.remove(i);
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
         super("motes", "Lichtstaub", "Feiner leuchtender Staub schwebt in der Luft (sehr cinematic)", Category.WORLD_FX);
         this.icon("sparkle");
         this.fresh();
      }

      void render(float dt) {
         double cx = R3.camX();
         double cy = R3.camY();
         double cz = R3.camZ();

         while (this.pool.n < this.count.getI()) {
            double[] q = this.pool.add();
            q[0] = cx + (Ambient.RND.nextDouble() - 0.5) * 16.0;
            q[1] = cy + (Ambient.RND.nextDouble() - 0.5) * 8.0;
            q[2] = cz + (Ambient.RND.nextDouble() - 0.5) * 16.0;
            q[6] = 0.0;
            q[7] = 6.0 + Ambient.RND.nextDouble() * 8.0;
            q[8] = Ambient.RND.nextDouble() * 1000.0;
            q[9] = 0.5 + Ambient.RND.nextDouble();
         }

         double t = System.currentTimeMillis() / 1000.0;
         Identifier core = Ambient.tex("core");

         for (int i = this.pool.n - 1; i >= 0; i--) {
            double[] q = this.pool.p[i];
            q[6] += dt;
            q[0] += Math.sin(t * 0.3 + q[8]) * 0.08 * dt;
            q[1] += (0.05 + Math.sin(t * 0.5 + q[8]) * 0.05) * dt;
            q[2] += Math.cos(t * 0.35 + q[8]) * 0.08 * dt;
            double dx = q[0] - cx;
            double dz = q[2] - cz;
            if (!(q[6] > q[7]) && !(dx * dx + dz * dz > 144.0) && i < this.count.getI()) {
               double fade = Math.min(1.0, Math.min(q[6] / 1.5, (q[7] - q[6]) / 1.5));
               double tw = 0.5 + 0.5 * Math.sin(t * 2.3 + q[8] * 5.0);
               R3.glowBillboard(core, q[0], q[1], q[2], 0.07 * q[9], R3.argb(this.color.argb(q[8]) & 16777215, 0.8 * fade * tw));
            } else {
               this.pool.remove(i);
            }
         }
      }
   }

   static final class Pool {
      final double[][] p;
      int n;

      Pool(int max) {
         this.p = new double[max][10];
      }

      double[] add() {
         return this.n >= this.p.length ? null : this.p[this.n++];
      }

      void remove(int i) {
         double[] t = this.p[i];
         this.p[i] = this.p[this.n - 1];
         this.p[this.n - 1] = t;
         this.n--;
      }
   }

   public static final class Rainbow extends Module {
      final Setting.Mode when = this.add(new Setting.Mode("Zeigen", 0, "Nach Regen", "Immer (am Tag)"));
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 55.0, "%"));
      private long rainEnded = 0L;
      private boolean wasRaining;

      Rainbow() {
         super("rainbow", "Regenbogen", "Ein echter Regenbogen gegenüber der Sonne - nach Regen oder immer", Category.WORLD_FX);
         this.icon("rain");
         this.fresh();
      }

      @Override
      public void tick() {
         boolean r = false;

         try {
            r = Mc.world().getRainLevel(1.0F) > 0.2F;
         } catch (Throwable var3) {
         }

         if (this.wasRaining && !r) {
            this.rainEnded = System.currentTimeMillis();
         }

         this.wasRaining = r;
      }

      void render() {
         double day = 1.0 - Ambient.night();
         if (!(day < 0.05)) {
            double vis = day * this.strength.get() / 100.0;
            if (this.when.index == 0) {
               long since = System.currentTimeMillis() - this.rainEnded;
               double rain = this.wasRaining ? 0.5 : (since < 240000L ? 1.0 - since / 240000.0 : 0.0);
               vis *= rain;
            }

            if (!(vis < 0.01)) {
               long tod = Math.floorMod(Mc.timeOfDay(), 24000L);
               double sunAng = tod / 24000.0 * Math.PI * 2.0;
               double east = Math.cos(sunAng) >= 0.0 ? 1.0 : -1.0;
               double far = 180.0;

               try {
                  far = Math.min(far, dev.lego.compat.McCompat.depthFar(Mc.mc().gameRenderer) * 0.45);
               } catch (Throwable var29) {
               }

               double cx = R3.camX() - east * far * 0.9;
               double cy = R3.camY() - far * 0.25;
               double cz = R3.camZ();
               double[] u = new double[]{0.0, 0.0, 1.0};
               double[] w = new double[]{0.0, 1.0, 0.0};
               int[] cols = new int[]{16726843, 16751150, 16771130, 5102170, 3909119, 5987327, 10636287};
               double r0 = far * 0.62;
               double band = far * 0.018;

               for (int i = 0; i < cols.length; i++) {
                  double ra = r0 + (cols.length - 1 - i) * band;
                  arc(cx, cy, cz, u, w, ra, ra + band, R3.argb(cols[i], 0.35 * vis));
               }
            }
         }
      }

      private static void arc(double x, double y, double z, double[] u, double[] w, double r0, double r1, int c) {
         int seg = 48;

         for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * i / seg;
            double a1 = Math.PI * (i + 1) / seg;
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            R3.quad(
               x + (u[0] * c0 + w[0] * s0) * r0,
               y + (u[1] * c0 + w[1] * s0) * r0,
               z + (u[2] * c0 + w[2] * s0) * r0,
               x + (u[0] * c0 + w[0] * s0) * r1,
               y + (u[1] * c0 + w[1] * s0) * r1,
               z + (u[2] * c0 + w[2] * s0) * r1,
               x + (u[0] * c1 + w[0] * s1) * r1,
               y + (u[1] * c1 + w[1] * s1) * r1,
               z + (u[2] * c1 + w[2] * s1) * r1,
               x + (u[0] * c1 + w[0] * s1) * r0,
               y + (u[1] * c1 + w[1] * s1) * r0,
               z + (u[2] * c1 + w[2] * s1) * r0,
               R3.argb(c, 0.0) | 0,
               c,
               c,
               R3.argb(c, 0.0)
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
         super("snowfall", "Schnee & Blüten", "HD-Schnee, Kirschblüten, Herbstlaub, Glitzer ... fallen um dich herum", Category.WORLD_FX);
         this.icon("snow");
         this.fresh();
      }

      void render(float dt) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            double cx = R3.camX();
            double cy = R3.camY();
            double cz = R3.camZ();
            int max = this.amount.getI();
            this.spawnAcc += dt * max / 5.0F;

            while (this.spawnAcc >= 1.0F && this.pool.n < max) {
               this.spawnAcc--;
               double x = cx + (Ambient.RND.nextDouble() - 0.5) * 36.0;
               double z = cz + (Ambient.RND.nextDouble() - 0.5) * 36.0;
               double y = cy + 4.0 + Ambient.RND.nextDouble() * 14.0;
               if (!this.roof.get() || !(Ambient.top(x, z) > y)) {
                  double[] q = this.pool.add();
                  if (q == null) {
                     break;
                  }

                  q[0] = x;
                  q[1] = y;
                  q[2] = z;
                  q[3] = 0.0;
                  q[4] = -(0.8 + Ambient.RND.nextDouble() * 0.9) * (this.type.index == 3 ? 0.6 : 1.0);
                  q[5] = 0.0;
                  q[6] = 0.0;
                  q[7] = 8.0 + Ambient.RND.nextDouble() * 6.0;
                  q[8] = Ambient.RND.nextDouble() * 1000.0;
                  q[9] = 0.6 + Ambient.RND.nextDouble() * 0.8;
               }
            }

            if (this.spawnAcc > 5.0F) {
               this.spawnAcc = 5.0F;
            }

            double t = System.currentTimeMillis() / 1000.0;
            Identifier tx = Ambient.tex(TEXS[this.type.index]);
            boolean spin = this.type.index == 1 || this.type.index == 2;
            double w = this.wind.get();

            for (int i = this.pool.n - 1; i >= 0; i--) {
               double[] q = this.pool.p[i];
               q[6] += dt;
               double sway = Math.sin(t * 1.3 + q[8]) * 0.6 * (spin ? 1.6 : 1.0);
               q[0] += (w * 0.8 + sway) * dt;
               q[2] += (Math.cos(t * 0.9 + q[8] * 1.7) * 0.4 + w * 0.3) * dt;
               q[1] += q[4] * dt;
               double dx = q[0] - cx;
               double dz = q[2] - cz;
               if (!(q[6] > q[7]) && !(q[1] < cy - 12.0) && !(dx * dx + dz * dz > 484.0) && (!this.roof.get() || !(q[1] < Ambient.top(q[0], q[2])))) {
                  double fade = Math.min(1.0, Math.min(q[6] / 0.6, (q[7] - q[6]) / 0.8));
                  double s = 0.12 * q[9] * this.size.get() * (this.type.index != 4 && this.type.index != 5 ? 1.0 : 0.8);
                  int col = this.type.index != 4 && this.type.index != 5
                     ? R3.argb(16777215, fade * 0.95)
                     : R3.argb(16777215, fade * (0.5 + 0.5 * Math.sin(t * 6.0 + q[8])));
                  if (spin) {
                     R3.billboardRot(tx, q[0], q[1], q[2], s * 1.3, t * 1.6 + q[8], col);
                  } else {
                     R3.billboardRot(tx, q[0], q[1], q[2], s, q[8], col);
                  }
               } else {
                  this.pool.remove(i);
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
