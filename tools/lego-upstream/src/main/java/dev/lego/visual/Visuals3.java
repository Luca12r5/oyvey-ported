package dev.lego.visual;

import java.util.ArrayList;
import java.util.List;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import net.minecraft.class_2338;
import net.minecraft.class_746;

/** Gooba-ported visuals: floating cubes, world grid, totem ghost, trajectories. */
public final class Visuals3 {
    private Visuals3() { }

    public static FloatingCubes floatingCubes;
    public static WorldGrid worldGrid;
    public static TotemGhost totemGhost;
    public static Trajectories trajectories;
    public static ChunkBorders chunkBorders;
    public static SpeedLines speedLines;
    public static AntiVanish antiVanish;
    public static SpatialInventory spatialInventory;

    public static void registerAll() {
        floatingCubes = Modules.register(new FloatingCubes());
        worldGrid = Modules.register(new WorldGrid());
        totemGhost = Modules.register(new TotemGhost());
        trajectories = Modules.register(new Trajectories());
        chunkBorders = Modules.register(new ChunkBorders());
        speedLines = Modules.register(new SpeedLines());
        antiVanish = Modules.register(new AntiVanish());
        spatialInventory = Modules.register(new SpatialInventory());
    }

    static void render(float td) {
        if (floatingCubes.enabled) floatingCubes.render(td);
        if (worldGrid.enabled) worldGrid.render();
        if (chunkBorders.enabled) chunkBorders.render();
        if (trajectories.enabled) trajectories.render(td);
    }

    public static void renderHud(int W, int H) {
        if (speedLines != null && speedLines.enabled) speedLines.draw(W, H);
        if (antiVanish != null && antiVanish.enabled) antiVanish.renderHud(W, H);
    }

    // ================================================================== floating cubes
    public static final class FloatingCubes extends Module {
        final Setting.Num count = add(new Setting.Num("Anzahl", 30, 200, 10, 80, ""));
        final Setting.Num cubeSize = add(new Setting.Num("Größe", 0.08, 0.5, 0.02, 0.22, ""));
        final Setting.Mode shape = add(new Setting.Mode("Form", 0, "Würfel", "Diamant", "Stern"));
        final Setting.Num alpha = add(new Setting.Num("Transparenz", 10, 100, 5, 85, "%"));
        final Setting.Bool edges = add(new Setting.Bool("Kanten anzeigen", true));
        final Setting.Color color = add(new Setting.Color("Farbe", 0));

        private final List<Cube> cubes = new ArrayList<>();

        FloatingCubes() { super("floatingcubes", "Schwebende Formen", "Leuchtende 3D-Formen mit Physik schweben um dich herum", Category.VISUALS); icon("box"); fresh(); }

        @Override public void tick() {
            class_746 me = Mc.player();
            if (me == null) return;
            long now = System.currentTimeMillis();
            cubes.removeIf(c -> c.expired(now));
            while (cubes.size() < count.getI()) {
                double r = 24;
                double x = Mc.x(me) + (Math.random() - 0.5) * r * 2;
                double z = Mc.z(me) + (Math.random() - 0.5) * r * 2;
                double y = Mc.y(me) + Mc.height(me) + Math.random() * r;
                double vx = (Mc.velocity(me)[0] + (Math.random() - 0.5) * 0.6) * 0.05;
                double vy = (Math.random() * 0.44 - 0.01) * 0.05;
                double vz = (Mc.velocity(me)[2] + (Math.random() - 0.5) * 0.6) * 0.05;
                cubes.add(new Cube(x, y, z, vx, vy, vz, now, (float)(Math.random() * 100)));
            }
            for (Cube c : cubes) c.update(now);
        }

        void render(float td) {
            double sz = cubeSize.get();
            double a = alpha.get() / 100.0;
            boolean showEdges = edges.get();
            String sh = shape.get();
            for (Cube c : cubes) {
                if (c.opacity <= 0.01) continue;
                double dx = c.x - R3.camX(), dy = c.y - R3.camY(), dz = c.z - R3.camZ();
                if (dx * dx + dy * dy + dz * dz > 96 * 96) continue;
                int rgb = color.argb(c.rotOff * 0.01) & 0xFFFFFF;
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
                } else if ("Stern".equals(sh)) {
                    double t = System.currentTimeMillis() / 1000.0 + c.rotOff;
                    int cf = R3.argb(rgb, 0.6 * oa);
                    for (int i = 0; i < 3; i++) {
                        double angle = t * 1.5 + i * Math.PI / 3;
                        double cos = Math.cos(angle) * sz, sin = Math.sin(angle) * sz;
                        R3.line(c.x - cos, c.y - sz, c.z - sin, c.x + cos, c.y + sz, c.z + sin, 0.03, cf, cf);
                    }
                    R3.cube(c.x, c.y, c.z, sz * 0.5, R3.argb(rgb, 0.4 * oa));
                } else {
                    R3.cube(c.x, c.y, c.z, sz, R3.argb(rgb, 0.7 * oa));
                    if (showEdges) R3.boxEdges(c.x - sz, c.y - sz, c.z - sz, c.x + sz, c.y + sz, c.z + sz, 0.015, R3.argb(rgb, oa));
                }
            }
        }

        @Override public void onDisable() { cubes.clear(); }

        private static final class Cube {
            double x, y, z, vx, vy, vz;
            final long born;
            final float rotOff;
            float opacity;
            boolean fading;

            Cube(double x, double y, double z, double vx, double vy, double vz, long born, float rotOff) {
                this.x = x; this.y = y; this.z = z;
                this.vx = vx; this.vy = vy; this.vz = vz;
                this.born = born; this.rotOff = rotOff;
            }

            void update(long now) {
                long age = now - born;
                if (age < 500) opacity = Math.min(1, age / 500f);
                else if (!fading && age > 1500) { fading = true; }
                if (fading) opacity = Math.max(0, opacity - 0.008f);
                x += vx; y += vy; z += vz;
                vy -= 0.0001;
                if (Mc.world() != null) {
                    class_2338 bp = class_2338.method_49637(x, y, z);
                    try {
                        if (!Mc.world().method_8320(bp).method_26216()) {
                            vx = -vx * 0.85; vy = -vy * 0.85; vz = -vz * 0.85;
                        }
                    } catch (Throwable ignored) { }
                }
            }

            boolean expired(long now) { return fading && opacity <= 0.01f; }
        }
    }

    // ================================================================== world grid
    public static final class WorldGrid extends Module {
        final Setting.Num radius = add(new Setting.Num("Radius", 1, 12, 1, 4, " Zellen"));
        final Setting.Num height = add(new Setting.Num("Höhe", 1, 8, 1, 2, " Zellen"));
        final Setting.Num opacity = add(new Setting.Num("Deckkraft", 5, 100, 5, 22, "%"));
        final Setting.Color color = add(new Setting.Color("Farbe", 0));

        WorldGrid() { super("worldgrid", "Welt-Gitter", "Leuchtende 3D-Gitterlinien in der Welt (12-Block-Zellen)", Category.VISUALS); icon("grid"); fresh(); }

        void render() {
            class_746 me = Mc.player();
            if (me == null) return;
            int cx = cellOrigin(Mc.x(me)), cy = cellOrigin(Mc.y(me)), cz = cellOrigin(Mc.z(me));
            int r = radius.getI(), h = height.getI();
            double alpha = opacity.get() / 100.0;
            int rgb = color.argb() & 0xFFFFFF;
            int col = R3.argb(rgb, alpha);
            int x0 = cx - r * 12, x1 = cx + r * 12;
            int z0 = cz - r * 12, z1 = cz + r * 12;
            int y0 = cy - (h - 1) * 12, y1 = cy + h * 12;
            for (int gx = x0; gx <= x1; gx += 12)
                for (int gz = z0; gz <= z1; gz += 12)
                    R3.line(gx, y0, gz, gx, y1, gz, 0.02, col, col);
            for (int gy = y0; gy <= y1; gy += 12)
                for (int gz = z0; gz <= z1; gz += 12)
                    R3.line(x0, gy, gz, x1, gy, gz, 0.02, col, col);
            for (int gy = y0; gy <= y1; gy += 12)
                for (int gx = x0; gx <= x1; gx += 12)
                    R3.line(gx, gy, z0, gx, gy, z1, 0.02, col, col);
        }

        private static int cellOrigin(double v) { return (int) Math.floor(v / 12.0) * 12; }
    }

    // ================================================================== totem ghost (makes held totem transparent/see-through)
    public static final class TotemGhost extends Module {
        final Setting.Num opacity = add(new Setting.Num("Durchsichtigkeit", 5, 95, 5, 70, "%"));
        final Setting.Bool onlyTotem = add(new Setting.Bool("Nur Totem", true));

        TotemGhost() { super("totemghost", "Item Transparent", "Macht das Totem in der Hand durchsichtig", Category.VISUALS); icon("ghost"); fresh(); }

        @Override public void onEnable() {
            dev.lego.visual.shader.HandEffect he = dev.lego.visual.shader.HandEffect.getInstance();
            he.setEnabled(true);
            he.setTransparencyEnabled(true);
            he.setTransparencyAlpha(1f - (float) (opacity.get() / 100.0));
            if (!Visuals2.shaderHandsEnabled()) he.setOutlineEnabled(false);
        }

        @Override public void onDisable() {
            dev.lego.visual.shader.HandEffect he = dev.lego.visual.shader.HandEffect.getInstance();
            he.setTransparencyEnabled(false);
            if (Visuals2.shaderHandsEnabled()) he.setOutlineEnabled(true);
            else he.setEnabled(false);
        }

        @Override public void tick() {
            if (!enabled) return;
            dev.lego.visual.shader.HandEffect he = dev.lego.visual.shader.HandEffect.getInstance();
            he.setTransparencyAlpha(1f - (float) (opacity.get() / 100.0));
        }

        public boolean shouldBeTransparent(net.minecraft.class_1799 stack) {
            if (!enabled) return false;
            if (!onlyTotem.get()) return true;
            return Mc.itemKey(stack).endsWith(".totem_of_undying");
        }
    }

    // ================================================================== trajectories
    public static final class Trajectories extends Module {
        final Setting.Color color = add(new Setting.Color("Farbe", 0));
        final Setting.Bool pearl = add(new Setting.Bool("Enderperlen", true));
        final Setting.Bool arrow = add(new Setting.Bool("Pfeile", true));
        final Setting.Bool trident = add(new Setting.Bool("Dreizack", true));

        Trajectories() { super("trajectories", "Flugbahnen", "Zeigt die vorhergesagte Flugbahn von Projektilen", Category.VISUALS); icon("arrow"); fresh(); }

        void render(float td) {
            class_746 me = Mc.player();
            if (me == null || Mc.world() == null) return;
            net.minecraft.class_1799 held = me.method_6047();
            if (!Mc.empty(held)) {
                String key = Mc.itemKey(held);
                if (pearl.get() && key.endsWith(".ender_pearl")) predictHeld(me, td, 1.5, 0.03, 0.99);
                else if (arrow.get() && (key.endsWith(".bow") || key.endsWith(".crossbow"))) predictBow(me, td, held, key);
                else if (trident.get() && key.endsWith(".trident")) predictHeld(me, td, 2.5, 0.05, 0.99);
            }
            net.minecraft.class_1799 off = me.method_6079();
            if (!Mc.empty(off)) {
                String key = Mc.itemKey(off);
                if (pearl.get() && key.endsWith(".ender_pearl")) predictHeld(me, td, 1.5, 0.03, 0.99);
                else if (trident.get() && key.endsWith(".trident")) predictHeld(me, td, 2.5, 0.05, 0.99);
            }
        }

        private void predictHeld(class_746 me, float td, double speed, double gravity, double drag) {
            double[] pos = Mc.lerpPos(me, td);
            double eyeY = pos[1] + Mc.height(me) * 0.85;
            float pitch = me.method_36455(), yaw = me.method_36454();
            double pr = Math.toRadians(pitch), yr = Math.toRadians(yaw);
            double cp = Math.cos(-pr), sp = Math.sin(-pr);
            double cy = Math.cos(-yr - Math.PI), sy = Math.sin(-yr - Math.PI);
            double vx = sy * cp * speed, vy = -sp * speed, vz = cy * cp * speed;
            drawTrajectory(pos[0], eyeY, pos[2], vx, vy, vz, gravity, drag, me);
        }

        private void predictBow(class_746 me, float td, net.minecraft.class_1799 held, String key) {
            predictHeld(me, td, 3.0, 0.05, 0.99);
        }

        private void drawTrajectory(double sx, double sy, double sz, double vx, double vy, double vz, double gravity, double drag, class_746 me) {
            int rgb = color.argb() & 0xFFFFFF;
            int col = R3.argb(rgb, 0.85);
            double px = sx, py = sy, pz = sz;
            for (int i = 0; i < 300; i++) {
                double nx = px + vx * 0.25, ny = py + vy * 0.25, nz = pz + vz * 0.25;
                class_2338 bp = class_2338.method_49637(nx, ny, nz);
                try {
                    if (!Mc.world().method_8320(bp).method_26216()) {
                        R3.ring(nx, ny + 0.05, nz, 0.0, 0.35, 24, col, R3.argb(rgb, 0.15), 0);
                        break;
                    }
                } catch (Throwable ignored) { break; }
                if (i % 2 == 0) R3.line(px, py, pz, nx, ny, nz, 0.02, col, col);
                px = nx; py = ny; pz = nz;
                vx *= Math.pow(drag, 0.25);
                vy = vy * Math.pow(drag, 0.25) - gravity * 0.25;
                vz *= Math.pow(drag, 0.25);
                if (ny < -80) break;
            }
        }
    }

    // ================================================================== chunk borders
    public static final class ChunkBorders extends Module {
        final Setting.Num height = add(new Setting.Num("Höhe", 16, 128, 8, 48, " Blöcke"));
        final Setting.Num opacity = add(new Setting.Num("Deckkraft", 5, 100, 5, 35, "%"));
        final Setting.Color color = add(new Setting.Color("Farbe", 0));
        final Setting.Bool fill = add(new Setting.Bool("Füllung", false));
        final Setting.Bool neighbors = add(new Setting.Bool("Nachbar-Chunks", true));

        ChunkBorders() { super("chunkborders", "Chunk-Grenzen", "Zeigt die 16×16-Chunk-Grenzen mit vertikalen Linien", Category.VISUALS); icon("grid"); fresh(); }

        void render() {
            class_746 me = Mc.player();
            if (me == null) return;
            int cx = (int) Math.floor(Mc.x(me)) >> 4;
            int cz = (int) Math.floor(Mc.z(me)) >> 4;
            double a = opacity.get() / 100.0;
            int rgb = color.argb() & 0xFFFFFF;
            int h = height.getI();
            int by = (int) Mc.y(me) - h / 2;
            int ty = by + h;
            int range = neighbors.get() ? 1 : 0;
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    int bx = (cx + dx) << 4, bz = (cz + dz) << 4;
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
                    if (fill.get() && own) {
                        int cf = R3.argb(rgb, la * 0.06);
                        R3.quad(bx, by, bz, bx + 16, by, bz, bx + 16, by, bz + 16, bx, by, bz + 16, cf);
                        R3.quad(bx, ty, bz, bx + 16, ty, bz, bx + 16, ty, bz + 16, bx, ty, bz + 16, cf);
                    }
                }
            }
        }
    }

    // ================================================================== speed lines (anime-style screen overlay when sprinting)
    public static final class SpeedLines extends Module {
        final Setting.Num strength = add(new Setting.Num("Stärke", 10, 100, 5, 50, "%"));
        final Setting.Bool onlySprint = add(new Setting.Bool("Nur beim Sprinten", true));
        final Setting.Color color = add(new Setting.Color("Farbe", 8));

        private float intensity = 0;
        private final long[] seeds = new long[48];
        { java.util.Random rng = new java.util.Random(42); for (int i = 0; i < seeds.length; i++) seeds[i] = rng.nextLong(); }

        SpeedLines() { super("speedlines", "Speed Lines", "Anime-artige Geschwindigkeitsstreifen am Bildschirmrand", Category.VISUALS); icon("bolt"); fresh(); }

        void draw(int W, int H) {
            class_746 p = Mc.player();
            if (p == null) { intensity = 0; return; }
            double[] v = Mc.velocity(p);
            float spd = (float) Math.sqrt(v[0] * v[0] + v[2] * v[2]);
            boolean sprint = p.method_5624();
            float target = 0;
            if (!onlySprint.get() || sprint) target = Math.min(1f, spd * 6f);
            intensity += (target - intensity) * Math.min(1f, dev.lego.ui.Gx.dt * (target > intensity ? 10f : 4f));
            if (intensity < 0.02f) return;
            float a = intensity * strength.getF() / 100f;
            int rgb = color.argb();
            int cx = W / 2, cy = H / 2;
            float maxR = (float) Math.sqrt(cx * cx + cy * cy);
            int S = dev.lego.ui.Gx.S;
            for (int i = 0; i < seeds.length; i++) {
                java.util.Random r = new java.util.Random(seeds[i]);
                double angle = r.nextDouble() * Math.PI * 2;
                double rStart = 0.55 + r.nextDouble() * 0.25;
                double rEnd = rStart + 0.15 + r.nextDouble() * 0.25;
                float lineA = (float) (0.15 + r.nextDouble() * 0.5) * a;
                float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
                int x1 = cx + Math.round(cos * maxR * (float) rStart);
                int y1 = cy + Math.round(sin * maxR * (float) rStart);
                int x2 = cx + Math.round(cos * maxR * (float) rEnd);
                int y2 = cy + Math.round(sin * maxR * (float) rEnd);
                int thick = Math.max(1, (int) (S * (0.5 + r.nextDouble() * 1.5)));
                dev.lego.ui.Gx.fill(Math.min(x1, x2), Math.min(y1, y2),
                        Math.max(thick, Math.abs(x2 - x1)), Math.max(thick, Math.abs(y2 - y1)),
                        dev.lego.ui.Gx.withAlpha(rgb, lineA));
            }
        }
    }
}
