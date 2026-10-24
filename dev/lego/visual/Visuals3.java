package dev.lego.visual;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Gx;
import dev.lego.visual.shader.HandEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public final class Visuals3 {
   public static Visuals3.FloatingCubes floatingCubes;
   public static Visuals3.WorldGrid worldGrid;
   public static Visuals3.TotemGhost totemGhost;
   public static Visuals3.Trajectories trajectories;
   public static Visuals3.ChunkBorders chunkBorders;
   public static Visuals3.SpeedLines speedLines;
   public static AntiVanish antiVanish;
   public static SpatialInventory spatialInventory;

   private Visuals3() {
   }

   public static void registerAll() {
      floatingCubes = Modules.register(new Visuals3.FloatingCubes());
      worldGrid = Modules.register(new Visuals3.WorldGrid());
      totemGhost = Modules.register(new Visuals3.TotemGhost());
      trajectories = Modules.register(new Visuals3.Trajectories());
      chunkBorders = Modules.register(new Visuals3.ChunkBorders());
      speedLines = Modules.register(new Visuals3.SpeedLines());
      antiVanish = Modules.register(new AntiVanish());
      spatialInventory = Modules.register(new SpatialInventory());
   }

   static void render(float td) {
      if (floatingCubes.enabled) {
         floatingCubes.render(td);
      }

      if (worldGrid.enabled) {
         worldGrid.render();
      }

      if (chunkBorders.enabled) {
         chunkBorders.render();
      }

      if (trajectories.enabled) {
         trajectories.render(td);
      }
   }

   public static void renderHud(int W, int H) {
      if (speedLines != null && speedLines.enabled) {
         speedLines.draw(W, H);
      }

      if (antiVanish != null && antiVanish.enabled) {
         antiVanish.renderHud(W, H);
      }
   }

   public static final class ChunkBorders extends Module {
      final Setting.Num height = this.add(new Setting.Num("Höhe", 16.0, 128.0, 8.0, 48.0, " Blöcke"));
      final Setting.Num opacity = this.add(new Setting.Num("Deckkraft", 5.0, 100.0, 5.0, 35.0, "%"));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 0));
      final Setting.Bool fill = this.add(new Setting.Bool("Füllung", false));
      final Setting.Bool neighbors = this.add(new Setting.Bool("Nachbar-Chunks", true));

      ChunkBorders() {
         super("chunkborders", "Chunk-Grenzen", "Zeigt die 16×16-Chunk-Grenzen mit vertikalen Linien", Category.VISUALS);
         this.icon("grid");
         this.fresh();
      }

      void render() {
         LocalPlayer me = Mc.player();
         if (me != null) {
            int cx = (int)Math.floor(Mc.x(me)) >> 4;
            int cz = (int)Math.floor(Mc.z(me)) >> 4;
            double a = this.opacity.get() / 100.0;
            int rgb = this.color.argb() & 16777215;
            int h = this.height.getI();
            int by = (int)Mc.y(me) - h / 2;
            int ty = by + h;
            int range = this.neighbors.get() ? 1 : 0;

            for (int dx = -range; dx <= range; dx++) {
               for (int dz = -range; dz <= range; dz++) {
                  int bx = cx + dx << 4;
                  int bz = cz + dz << 4;
                  boolean own = dx == 0 && dz == 0;
                  double la = own ? a : a * 0.4;
                  int col = R3.argb(rgb, la);
                  int colFaint = R3.argb(rgb, la * 0.3);

                  for (int i = 0; i <= 16; i += 16) {
                     R3.line(bx + i, by, bz, bx + i, ty, bz, own ? 0.03 : 0.015, col, col);
                     R3.line(bx + i, by, bz + 16, bx + i, ty, bz + 16, own ? 0.03 : 0.015, col, col);
                     R3.line(bx, by, bz + i, bx, ty, bz + i, own ? 0.03 : 0.015, col, col);
                     R3.line(bx + 16, by, bz + i, bx + 16, ty, bz + i, own ? 0.03 : 0.015, col, col);
                  }

                  if (own) {
                     R3.line(bx, by, bz, bx + 16, by, bz, 0.02, col, col);
                     R3.line(bx, by, bz, bx, by, bz + 16, 0.02, col, col);
                     R3.line(bx + 16, by, bz, bx + 16, by, bz + 16, 0.02, col, col);
                     R3.line(bx, by, bz + 16, bx + 16, by, bz + 16, 0.02, col, col);
                     R3.line(bx, ty, bz, bx + 16, ty, bz, 0.02, col, col);
                     R3.line(bx, ty, bz, bx, ty, bz + 16, 0.02, col, col);
                     R3.line(bx + 16, ty, bz, bx + 16, ty, bz + 16, 0.02, col, col);
                     R3.line(bx, ty, bz + 16, bx + 16, ty, bz + 16, 0.02, col, col);
                  }

                  if (this.fill.get() && own) {
                     int cf = R3.argb(rgb, la * 0.06);
                     R3.quad(bx, by, bz, bx + 16, by, bz, bx + 16, by, bz + 16, bx, by, bz + 16, cf);
                     R3.quad(bx, ty, bz, bx + 16, ty, bz, bx + 16, ty, bz + 16, bx, ty, bz + 16, cf);
                  }
               }
            }
         }
      }
   }

   public static final class FloatingCubes extends Module {
      final Setting.Num count = this.add(new Setting.Num("Anzahl", 30.0, 200.0, 10.0, 80.0, ""));
      final Setting.Num cubeSize = this.add(new Setting.Num("Größe", 0.08, 0.5, 0.02, 0.22, ""));
      final Setting.Mode shape = this.add(new Setting.Mode("Form", 0, "Würfel", "Diamant", "Stern"));
      final Setting.Num alpha = this.add(new Setting.Num("Transparenz", 10.0, 100.0, 5.0, 85.0, "%"));
      final Setting.Bool edges = this.add(new Setting.Bool("Kanten anzeigen", true));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 0));
      private final List<Visuals3.FloatingCubes.Cube> cubes = new ArrayList<>();

      FloatingCubes() {
         super("floatingcubes", "Schwebende Formen", "Leuchtende 3D-Formen mit Physik schweben um dich herum", Category.VISUALS);
         this.icon("box");
         this.fresh();
      }

      @Override
      public void tick() {
         LocalPlayer me = Mc.player();
         if (me != null) {
            long now = System.currentTimeMillis();
            this.cubes.removeIf(cx -> cx.expired(now));

            while (this.cubes.size() < this.count.getI()) {
               double r = 24.0;
               double x = Mc.x(me) + (Math.random() - 0.5) * r * 2.0;
               double z = Mc.z(me) + (Math.random() - 0.5) * r * 2.0;
               double y = Mc.y(me) + Mc.height(me) + Math.random() * r;
               double vx = (Mc.velocity(me)[0] + (Math.random() - 0.5) * 0.6) * 0.05;
               double vy = (Math.random() * 0.44 - 0.01) * 0.05;
               double vz = (Mc.velocity(me)[2] + (Math.random() - 0.5) * 0.6) * 0.05;
               this.cubes.add(new Visuals3.FloatingCubes.Cube(x, y, z, vx, vy, vz, now, (float)(Math.random() * 100.0)));
            }

            for (Visuals3.FloatingCubes.Cube c : this.cubes) {
               c.update(now);
            }
         }
      }

      void render(float td) {
         double sz = this.cubeSize.get();
         double a = this.alpha.get() / 100.0;
         boolean showEdges = this.edges.get();
         String sh = this.shape.get();

         for (Visuals3.FloatingCubes.Cube c : this.cubes) {
            if (!(c.opacity <= 0.01)) {
               double dx = c.x - R3.camX();
               double dy = c.y - R3.camY();
               double dz = c.z - R3.camZ();
               if (!(dx * dx + dy * dy + dz * dz > 9216.0)) {
                  int rgb = this.color.argb(c.rotOff * 0.01) & 16777215;
                  double oa = a * c.opacity;
                  if ("Diamant".equals(sh)) {
                     double h = sz * 1.4;
                     int cf = R3.argb(rgb, 0.5 * oa);
                     int ct = R3.argb(rgb, 0.7 * oa);
                     R3.quad(c.x - sz, c.y, c.z, c.x, c.y + h, c.z, c.x, c.y, c.z - sz, c.x, c.y, c.z - sz, cf, ct, cf, cf);
                     R3.quad(c.x + sz, c.y, c.z, c.x, c.y + h, c.z, c.x, c.y, c.z - sz, c.x, c.y, c.z - sz, cf, ct, cf, cf);
                     R3.quad(c.x + sz, c.y, c.z, c.x, c.y + h, c.z, c.x, c.y, c.z + sz, c.x, c.y, c.z + sz, cf, ct, cf, cf);
                     R3.quad(c.x - sz, c.y, c.z, c.x, c.y + h, c.z, c.x, c.y, c.z + sz, c.x, c.y, c.z + sz, cf, ct, cf, cf);
                     R3.quad(c.x - sz, c.y, c.z, c.x, c.y - h, c.z, c.x, c.y, c.z - sz, c.x, c.y, c.z - sz, cf, ct, cf, cf);
                     R3.quad(c.x + sz, c.y, c.z, c.x, c.y - h, c.z, c.x, c.y, c.z - sz, c.x, c.y, c.z - sz, cf, ct, cf, cf);
                     R3.quad(c.x + sz, c.y, c.z, c.x, c.y - h, c.z, c.x, c.y, c.z + sz, c.x, c.y, c.z + sz, cf, ct, cf, cf);
                     R3.quad(c.x - sz, c.y, c.z, c.x, c.y - h, c.z, c.x, c.y, c.z + sz, c.x, c.y, c.z + sz, cf, ct, cf, cf);
                     if (showEdges) {
                        int ce = R3.argb(rgb, oa);
                        double w = 0.015;
                        R3.line(c.x, c.y + h, c.z, c.x + sz, c.y, c.z, w, ce, ce);
                        R3.line(c.x, c.y + h, c.z, c.x - sz, c.y, c.z, w, ce, ce);
                        R3.line(c.x, c.y + h, c.z, c.x, c.y, c.z + sz, w, ce, ce);
                        R3.line(c.x, c.y + h, c.z, c.x, c.y, c.z - sz, w, ce, ce);
                        R3.line(c.x, c.y - h, c.z, c.x + sz, c.y, c.z, w, ce, ce);
                        R3.line(c.x, c.y - h, c.z, c.x - sz, c.y, c.z, w, ce, ce);
                        R3.line(c.x, c.y - h, c.z, c.x, c.y, c.z + sz, w, ce, ce);
                        R3.line(c.x, c.y - h, c.z, c.x, c.y, c.z - sz, w, ce, ce);
                     }
                  } else if (!"Stern".equals(sh)) {
                     R3.cube(c.x, c.y, c.z, sz, R3.argb(rgb, 0.7 * oa));
                     if (showEdges) {
                        R3.boxEdges(c.x - sz, c.y - sz, c.z - sz, c.x + sz, c.y + sz, c.z + sz, 0.015, R3.argb(rgb, oa));
                     }
                  } else {
                     double t = System.currentTimeMillis() / 1000.0 + c.rotOff;
                     int cf = R3.argb(rgb, 0.6 * oa);

                     for (int i = 0; i < 3; i++) {
                        double angle = t * 1.5 + i * Math.PI / 3.0;
                        double cos = Math.cos(angle) * sz;
                        double sin = Math.sin(angle) * sz;
                        R3.line(c.x - cos, c.y - sz, c.z - sin, c.x + cos, c.y + sz, c.z + sin, 0.03, cf, cf);
                     }

                     R3.cube(c.x, c.y, c.z, sz * 0.5, R3.argb(rgb, 0.4 * oa));
                  }
               }
            }
         }
      }

      @Override
      public void onDisable() {
         this.cubes.clear();
      }

      private static final class Cube {
         double x;
         double y;
         double z;
         double vx;
         double vy;
         double vz;
         final long born;
         final float rotOff;
         float opacity;
         boolean fading;

         Cube(double x, double y, double z, double vx, double vy, double vz, long born, float rotOff) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.born = born;
            this.rotOff = rotOff;
         }

         void update(long now) {
            long age = now - this.born;
            if (age < 500L) {
               this.opacity = Math.min(1.0F, (float)age / 500.0F);
            } else if (!this.fading && age > 1500L) {
               this.fading = true;
            }

            if (this.fading) {
               this.opacity = Math.max(0.0F, this.opacity - 0.008F);
            }

            this.x = this.x + this.vx;
            this.y = this.y + this.vy;
            this.z = this.z + this.vz;
            this.vy -= 1.0E-4;
            if (Mc.world() != null) {
               BlockPos bp = BlockPos.containing(this.x, this.y, this.z);

               try {
                  if (!Mc.world().getBlockState(bp).isSolidRender()) {
                     this.vx = -this.vx * 0.85;
                     this.vy = -this.vy * 0.85;
                     this.vz = -this.vz * 0.85;
                  }
               } catch (Throwable var7) {
               }
            }
         }

         boolean expired(long now) {
            return this.fading && this.opacity <= 0.01F;
         }
      }
   }

   public static final class SpeedLines extends Module {
      final Setting.Num strength = this.add(new Setting.Num("Stärke", 10.0, 100.0, 5.0, 50.0, "%"));
      final Setting.Bool onlySprint = this.add(new Setting.Bool("Nur beim Sprinten", true));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 8));
      private float intensity = 0.0F;
      private final long[] seeds = new long[48];

      SpeedLines() {
         super("speedlines", "Speed Lines", "Anime-artige Geschwindigkeitsstreifen am Bildschirmrand", Category.VISUALS);
         Random rng = new Random(42L);

         for (int i = 0; i < this.seeds.length; i++) {
            this.seeds[i] = rng.nextLong();
         }

         this.icon("bolt");
         this.fresh();
      }

      void draw(int W, int H) {
         LocalPlayer p = Mc.player();
         if (p == null) {
            this.intensity = 0.0F;
         } else {
            double[] v = Mc.velocity(p);
            float spd = (float)Math.sqrt(v[0] * v[0] + v[2] * v[2]);
            boolean sprint = p.isSprinting();
            float target = 0.0F;
            if (!this.onlySprint.get() || sprint) {
               target = Math.min(1.0F, spd * 6.0F);
            }

            this.intensity = this.intensity + (target - this.intensity) * Math.min(1.0F, Gx.dt * (target > this.intensity ? 10.0F : 4.0F));
            if (!(this.intensity < 0.02F)) {
               float a = this.intensity * this.strength.getF() / 100.0F;
               int rgb = this.color.argb();
               int cx = W / 2;
               int cy = H / 2;
               float maxR = (float)Math.sqrt(cx * cx + cy * cy);
               int S = Gx.S;

               for (int i = 0; i < this.seeds.length; i++) {
                  Random r = new Random(this.seeds[i]);
                  double angle = r.nextDouble() * Math.PI * 2.0;
                  double rStart = 0.55 + r.nextDouble() * 0.25;
                  double rEnd = rStart + 0.15 + r.nextDouble() * 0.25;
                  float lineA = (float)(0.15 + r.nextDouble() * 0.5) * a;
                  float cos = (float)Math.cos(angle);
                  float sin = (float)Math.sin(angle);
                  int x1 = cx + Math.round(cos * maxR * (float)rStart);
                  int y1 = cy + Math.round(sin * maxR * (float)rStart);
                  int x2 = cx + Math.round(cos * maxR * (float)rEnd);
                  int y2 = cy + Math.round(sin * maxR * (float)rEnd);
                  int thick = Math.max(1, (int)(S * (0.5 + r.nextDouble() * 1.5)));
                  Gx.fill(Math.min(x1, x2), Math.min(y1, y2), Math.max(thick, Math.abs(x2 - x1)), Math.max(thick, Math.abs(y2 - y1)), Gx.withAlpha(rgb, lineA));
               }
            }
         }
      }
   }

   public static final class TotemGhost extends Module {
      final Setting.Num opacity = this.add(new Setting.Num("Durchsichtigkeit", 5.0, 95.0, 5.0, 70.0, "%"));
      final Setting.Bool onlyTotem = this.add(new Setting.Bool("Nur Totem", true));

      TotemGhost() {
         super("totemghost", "Item Transparent", "Macht das Totem in der Hand durchsichtig", Category.VISUALS);
         this.icon("ghost");
         this.fresh();
      }

      @Override
      public void onEnable() {
         HandEffect he = HandEffect.getInstance();
         he.setEnabled(true);
         he.setTransparencyEnabled(true);
         he.setTransparencyAlpha(1.0F - (float)(this.opacity.get() / 100.0));
         if (!Visuals2.shaderHandsEnabled()) {
            he.setOutlineEnabled(false);
         }
      }

      @Override
      public void onDisable() {
         HandEffect he = HandEffect.getInstance();
         he.setTransparencyEnabled(false);
         if (Visuals2.shaderHandsEnabled()) {
            he.setOutlineEnabled(true);
         } else {
            he.setEnabled(false);
         }
      }

      @Override
      public void tick() {
         if (this.enabled) {
            HandEffect he = HandEffect.getInstance();
            he.setTransparencyAlpha(1.0F - (float)(this.opacity.get() / 100.0));
         }
      }

      public boolean shouldBeTransparent(ItemStack stack) {
         if (!this.enabled) {
            return false;
         } else {
            return !this.onlyTotem.get() ? true : Mc.itemKey(stack).endsWith(".totem_of_undying");
         }
      }
   }

   public static final class Trajectories extends Module {
      final Setting.Color color = this.add(new Setting.Color("Farbe", 0));
      final Setting.Bool pearl = this.add(new Setting.Bool("Enderperlen", true));
      final Setting.Bool arrow = this.add(new Setting.Bool("Pfeile", true));
      final Setting.Bool trident = this.add(new Setting.Bool("Dreizack", true));

      Trajectories() {
         super("trajectories", "Flugbahnen", "Zeigt die vorhergesagte Flugbahn von Projektilen", Category.VISUALS);
         this.icon("arrow");
         this.fresh();
      }

      void render(float td) {
         LocalPlayer me = Mc.player();
         if (me != null && Mc.world() != null) {
            ItemStack held = me.getMainHandItem();
            if (!Mc.empty(held)) {
               String key = Mc.itemKey(held);
               if (this.pearl.get() && key.endsWith(".ender_pearl")) {
                  this.predictHeld(me, td, 1.5, 0.03, 0.99);
               } else if (!this.arrow.get() || !key.endsWith(".bow") && !key.endsWith(".crossbow")) {
                  if (this.trident.get() && key.endsWith(".trident")) {
                     this.predictHeld(me, td, 2.5, 0.05, 0.99);
                  }
               } else {
                  this.predictBow(me, td, held, key);
               }
            }

            ItemStack off = me.getOffhandItem();
            if (!Mc.empty(off)) {
               String key = Mc.itemKey(off);
               if (this.pearl.get() && key.endsWith(".ender_pearl")) {
                  this.predictHeld(me, td, 1.5, 0.03, 0.99);
               } else if (this.trident.get() && key.endsWith(".trident")) {
                  this.predictHeld(me, td, 2.5, 0.05, 0.99);
               }
            }
         }
      }

      private void predictHeld(LocalPlayer me, float td, double speed, double gravity, double drag) {
         double[] pos = Mc.lerpPos(me, td);
         double eyeY = pos[1] + Mc.height(me) * 0.85;
         float pitch = me.getXRot();
         float yaw = me.getYRot();
         double pr = Math.toRadians(pitch);
         double yr = Math.toRadians(yaw);
         double cp = Math.cos(-pr);
         double sp = Math.sin(-pr);
         double cy = Math.cos(-yr - Math.PI);
         double sy = Math.sin(-yr - Math.PI);
         double vx = sy * cp * speed;
         double vy = -sp * speed;
         double vz = cy * cp * speed;
         this.drawTrajectory(pos[0], eyeY, pos[2], vx, vy, vz, gravity, drag, me);
      }

      private void predictBow(LocalPlayer me, float td, ItemStack held, String key) {
         this.predictHeld(me, td, 3.0, 0.05, 0.99);
      }

      private void drawTrajectory(double sx, double sy, double sz, double vx, double vy, double vz, double gravity, double drag, LocalPlayer me) {
         int rgb = this.color.argb() & 16777215;
         int col = R3.argb(rgb, 0.85);
         double px = sx;
         double py = sy;
         double pz = sz;

         for (int i = 0; i < 300; i++) {
            double nx = px + vx * 0.25;
            double ny = py + vy * 0.25;
            double nz = pz + vz * 0.25;
            BlockPos bp = BlockPos.containing(nx, ny, nz);

            try {
               if (!Mc.world().getBlockState(bp).isSolidRender()) {
                  R3.ring(nx, ny + 0.05, nz, 0.0, 0.35, 24, col, R3.argb(rgb, 0.15), 0.0);
                  break;
               }
            } catch (Throwable var35) {
               break;
            }

            if (i % 2 == 0) {
               R3.line(px, py, pz, nx, ny, nz, 0.02, col, col);
            }

            px = nx;
            py = ny;
            pz = nz;
            vx *= Math.pow(drag, 0.25);
            vy = vy * Math.pow(drag, 0.25) - gravity * 0.25;
            vz *= Math.pow(drag, 0.25);
            if (ny < -80.0) {
               break;
            }
         }
      }
   }

   public static final class WorldGrid extends Module {
      final Setting.Num radius = this.add(new Setting.Num("Radius", 1.0, 12.0, 1.0, 4.0, " Zellen"));
      final Setting.Num height = this.add(new Setting.Num("Höhe", 1.0, 8.0, 1.0, 2.0, " Zellen"));
      final Setting.Num opacity = this.add(new Setting.Num("Deckkraft", 5.0, 100.0, 5.0, 22.0, "%"));
      final Setting.Color color = this.add(new Setting.Color("Farbe", 0));

      WorldGrid() {
         super("worldgrid", "Welt-Gitter", "Leuchtende 3D-Gitterlinien in der Welt (12-Block-Zellen)", Category.VISUALS);
         this.icon("grid");
         this.fresh();
      }

      void render() {
         LocalPlayer me = Mc.player();
         if (me != null) {
            int cx = cellOrigin(Mc.x(me));
            int cy = cellOrigin(Mc.y(me));
            int cz = cellOrigin(Mc.z(me));
            int r = this.radius.getI();
            int h = this.height.getI();
            double alpha = this.opacity.get() / 100.0;
            int rgb = this.color.argb() & 16777215;
            int col = R3.argb(rgb, alpha);
            int x0 = cx - r * 12;
            int x1 = cx + r * 12;
            int z0 = cz - r * 12;
            int z1 = cz + r * 12;
            int y0 = cy - (h - 1) * 12;
            int y1 = cy + h * 12;

            for (int gx = x0; gx <= x1; gx += 12) {
               for (int gz = z0; gz <= z1; gz += 12) {
                  R3.line(gx, y0, gz, gx, y1, gz, 0.02, col, col);
               }
            }

            for (int gy = y0; gy <= y1; gy += 12) {
               for (int gz = z0; gz <= z1; gz += 12) {
                  R3.line(x0, gy, gz, x1, gy, gz, 0.02, col, col);
               }
            }

            for (int gy = y0; gy <= y1; gy += 12) {
               for (int gx = x0; gx <= x1; gx += 12) {
                  R3.line(gx, gy, z0, gx, gy, z1, 0.02, col, col);
               }
            }
         }
      }

      private static int cellOrigin(double v) {
         return (int)Math.floor(v / 12.0) * 12;
      }
   }
}
