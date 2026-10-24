package dev.lego.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import net.minecraft.class_2960;
import net.minecraft.class_746;

/** Many kinds of things flying around in the world (HD sprites, own simulation, no vanilla particles). */
public final class WorldFx {
    private WorldFx() { }

    /** How a kind of particle looks and moves. */
    static final class Kind {
        String id, name, desc, icon;
        String[] sprites;               // >1 = animation frames (wing flap)
        float flapHz = 0;
        int defCount;
        float radius = 16, hMin = 0.5f, hMax = 4;
        float size = 0.3f, speed = 1;
        int[] colors = {0xFFFFFF};
        boolean glow, rotate, night, day, sky;  // sky = high up (birds)
        float rise, fall, wander = 1, life = 12;
        boolean trail, swirl;
        Kind flap(float hz) { flapHz = hz; return this; }
        Kind sky() { sky = true; return this; }
        Kind wander(float w) { wander = w; return this; }
        Kind life(float l) { life = l; return this; }
        Kind night() { night = true; return this; }
        Kind day() { day = true; return this; }
        Kind rotate() { rotate = true; return this; }
        Kind glow() { glow = true; return this; }
        Kind trail() { trail = true; return this; }
        Kind rise(float r) { rise = r; return this; }
        Kind fall(float f) { fall = f; return this; }
        Kind swirl() { swirl = true; return this; }
    }

    public static final List<Fly> ALL = new ArrayList<>();

    public static void registerAll() {
        add(k("birds", "Vogelschwarm", "Schwärme von Vögeln ziehen über den Himmel", "wing", new String[]{"bird0", "bird1"}, 18, 45, 18, 28, 0.9f, 7, 0xFFFFFF)
                .flap(7).sky().wander(0.2f).life(20));
        add(k("bats", "Fledermäuse", "Fledermäuse flattern nachts um dich", "moon", new String[]{"bat0", "bat1"}, 10, 14, 2, 7, 0.45f, 4, 0xFFFFFF).flap(11).night().wander(2.2f));
        add(k("dragonflies", "Libellen", "Schillernde Libellen schwirren (tagsüber)", "wing", new String[]{"dragonfly"}, 8, 14, 0.4f, 2.2f, 0.32f, 3.5f, 0xFFFFFF).day().wander(3f).rotate());
        add(k("wisps", "Irrlichter", "Leuchtende magische Lichter schweben umher", "sparkle", new String[]{"core"}, 24, 18, 0.5f, 4, 0.22f, 1.2f, 0x7CF7FF, 0xB06BFF, 0x6BFFB0).glow().trail().wander(1.4f));
        add(k("dandelion", "Pusteblumen", "Pusteblumen-Samen tanzen im Wind", "leaf", new String[]{"seed"}, 40, 18, 0.5f, 6, 0.28f, 0.8f, 0xFFFFFF).rotate().wander(0.8f).rise(0.08f));
        add(k("embers", "Glutfunken", "Glühende Funken steigen auf", "flame", new String[]{"core"}, 60, 12, 0, 2, 0.08f, 1, 0xFFB02E, 0xFF6A1E, 0xFFE070).glow().rise(0.9f).wander(0.6f).life(5));
        add(k("pollen", "Blütenpollen", "Goldener Pollen glitzert in der Luft", "sun", new String[]{"core"}, 140, 10, 0.3f, 3, 0.05f, 0.4f, 0xFFE58A, 0xFFF6C8).glow().wander(0.5f).day());
        add(k("fairies", "Feen", "Kleine Feen mit Glitzerspur fliegen herum", "wand", new String[]{"core"}, 10, 12, 0.8f, 3.5f, 0.2f, 2.4f, 0xFF9CE8, 0x9CE8FF, 0xE8FF9C).glow().trail().wander(2.6f));
        add(k("notes", "Musiknoten", "Bunte Musiknoten schweben nach oben", "music", new String[]{"note"}, 20, 8, 0.3f, 2.5f, 0.3f, 0.6f, 0xFF4D8D, 0x4DD2FF, 0xFFD23F, 0x7CFF6B).rise(0.4f).wander(0.4f).life(7));
        add(k("hearts", "Herzen", "Herzen steigen sanft um dich auf", "heart", new String[]{"heart"}, 16, 7, 0.2f, 2, 0.25f, 0.5f, 0xFF4D6D, 0xFF8AB6).rise(0.45f).wander(0.3f).life(6));
        add(k("shards", "Kristallsplitter", "Schwebende, drehende Kristallsplitter", "gem", new String[]{"shard"}, 16, 12, 0.8f, 4, 0.3f, 0.3f, 0xBFE3FF, 0xE3BFFF).rotate().wander(0.3f).glow());
        add(k("confetti", "Konfetti", "Buntes Konfetti regnet herab", "sparkle", new String[]{"confetti"}, 120, 12, 4, 12, 0.12f, 1, 0xFF4D6D, 0x4DD2FF, 0xFFD23F, 0x7CFF6B, 0xB06BFF).rotate().fall(1.1f).wander(1.2f).life(8));
        add(k("leafstorm", "Blätterwirbel", "Bunte Blätter wirbeln im Wind um dich", "leaf", new String[]{"leafswirl"}, 50, 12, 0.3f, 5, 0.3f, 2.5f, 0xFFFFFF).rotate().wander(0.4f).swirl());
        // ---- Paket 4
        add(k("snowfall2", "Schneeflocken-Wirbel", "Dicke Schneeflocken tanzen um dich herum", "snow", new String[]{"snow"}, 90, 14, 0.5f, 7, 0.14f, 1, 0xFFFFFF).rotate().fall(0.5f).wander(0.7f).life(10));
        add(k("ashfall", "Aschregen", "Graue Asche rieselt vom Himmel", "cloud", new String[]{"ash"}, 110, 14, 3, 10, 0.1f, 1, 0xB0B0B0, 0x707070).rotate().fall(0.6f).wander(0.5f).life(12));
        add(k("glitter", "Glitzerregen", "Funkelnder Glitzer fällt langsam herab", "sparkle", new String[]{"glitter"}, 120, 12, 2, 8, 0.07f, 1, 0xFFE58A, 0xFFFFFF, 0xBFE3FF).glow().fall(0.35f).wander(0.4f).life(10));
        add(k("stardust", "Sternenstaub", "Feiner Sternenstaub schwebt nachts um dich", "star", new String[]{"stardust"}, 80, 14, 0.5f, 6, 0.12f, 0.5f, 0xFFFFFF, 0xCFE0FF, 0xFFE8B0).glow().night().wander(0.5f).life(14));
        add(k("lanternsred", "Rote Laternen", "Rote Himmelslaternen steigen auf", "light", new String[]{"lantern"}, 14, 20, 1, 6, 0.55f, 0.4f, 0xFF6A5A, 0xFF9A6A).glow().rise(0.35f).wander(0.25f).life(20).night());
        add(k("bubblesrise", "Aufsteigende Blasen", "Schimmernde Blasen steigen vom Boden auf", "rain", new String[]{"bubble"}, 30, 10, 0, 2, 0.25f, 0.5f, 0xFFFFFF, 0xBFEFFF).rise(0.6f).wander(0.3f).life(8));
        add(k("bluebutterflies", "Blaue Schmetterlinge", "Blau schimmernde Schmetterlinge flattern um dich", "wing", new String[]{"butterfly0", "butterfly1"}, 10, 10, 0.3f, 3, 0.35f, 1.6f, 0x6AB8FF, 0x3A7AFF).flap(9).wander(2f).day());
        add(k("pinkbutterflies", "Rosa Schmetterlinge", "Rosa Schmetterlinge tanzen über die Wiese", "wing", new String[]{"butterfly0", "butterfly1"}, 10, 10, 0.3f, 3, 0.35f, 1.6f, 0xFF9AC0, 0xFF6AA0).flap(9).wander(2f).day());
        add(k("goldfireflies", "Goldene Glühwürmchen", "Warm leuchtende Glühwürmchen in der Nacht", "light", new String[]{"core"}, 40, 14, 0.4f, 3.5f, 0.09f, 0.6f, 0xFFD23F, 0xFFB030).glow().night().wander(1f).life(9));
        add(k("bluefireflies", "Blaue Glühwürmchen", "Kühle blaue Lichter schweben nachts herum", "light", new String[]{"core"}, 40, 14, 0.4f, 3.5f, 0.09f, 0.6f, 0x7CF7FF, 0x4FA8FF).glow().night().wander(1f).life(9));
        add(k("greenspores", "Waldsporen", "Grüne Sporen schweben wie im Märchenwald", "leaf", new String[]{"core"}, 70, 12, 0.3f, 4, 0.06f, 0.4f, 0xB8FF4F, 0x6AE07A).glow().wander(0.4f).rise(0.05f).life(12));
        add(k("crimsonspores", "Karmesin-Sporen", "Rote Sporen wie im Karmesinwald", "flame", new String[]{"core"}, 70, 12, 0.3f, 4, 0.06f, 0.4f, 0xFF3A3A, 0xFF7A4A).glow().wander(0.4f).rise(0.05f).life(12));
        add(k("soulfire", "Seelenfeuer", "Blaue Seelenflammen-Funken steigen auf", "flame", new String[]{"core"}, 50, 10, 0, 2, 0.08f, 1, 0x4FE0FF, 0x2AA8FF).glow().rise(0.9f).wander(0.6f).life(5));
        add(k("petalrain", "Rosenblätter", "Rote Rosenblätter fallen sanft", "heart", new String[]{"petal"}, 50, 12, 3, 9, 0.2f, 1, 0xFF4D6D, 0xD7263D).rotate().fall(0.45f).wander(0.9f).life(12));
        add(k("autumn", "Herbstblätter", "Orange und rote Blätter fallen von oben", "leaf", new String[]{"leaf"}, 60, 14, 3, 10, 0.25f, 1, 0xE08A2A, 0xC8402A, 0xF0C040).rotate().fall(0.55f).wander(1f).life(12));
        add(k("goldconfetti", "Goldkonfetti", "Goldenes Konfetti für besondere Momente", "trophy", new String[]{"confetti"}, 120, 12, 4, 12, 0.12f, 1, 0xFFD23F, 0xFFF0A0, 0xE8B92C).rotate().fall(1.1f).wander(1.2f).life(8));
        add(k("lovehearts", "Liebesherzen", "Viele kleine Herzen schweben um dich", "heart", new String[]{"heart"}, 30, 6, 0.2f, 2.5f, 0.18f, 0.5f, 0xFF2A5A, 0xFF8AC0, 0xFFFFFF).rise(0.3f).wander(0.5f).life(7));
        add(k("crystals2", "Eiskristalle", "Glitzernde Eiskristalle drehen sich in der Luft", "gem", new String[]{"shard"}, 18, 12, 0.8f, 4, 0.3f, 0.3f, 0x9FE8FF, 0xFFFFFF).rotate().wander(0.3f).glow());
        add(k("magicnotes", "Leuchtende Noten", "Leuchtende Musiknoten kreisen um dich", "music", new String[]{"note"}, 20, 6, 0.5f, 2.8f, 0.25f, 1.2f, 0xFFD23F, 0x7CF7FF).glow().swirl().life(8));
        add(k("seeds2", "Löwenzahn-Wind", "Viele Löwenzahn-Samen fliegen im Wind vorbei", "leaf", new String[]{"seed"}, 70, 20, 0.5f, 7, 0.24f, 1.5f, 0xFFFFFF).rotate().wander(1.6f).rise(0.04f).life(14));
    }

    // ------------------------------------------------------------------ builder
    private static Kind k(String id, String name, String desc, String icon, String[] sprites, int count, float radius, float hMin, float hMax, float size, float speed, int... colors) {
        Kind k = new Kind();
        k.id = id; k.name = name; k.desc = desc; k.icon = icon; k.sprites = sprites; k.defCount = count; k.radius = radius;
        k.hMin = hMin; k.hMax = hMax; k.size = size; k.speed = speed; k.colors = colors;
        return k;
    }

    private static void add(Kind k) { ALL.add(Modules.register(new Fly(k))); }

    // ------------------------------------------------------------------ module + simulation
    public static final class Fly extends Module {
        final Kind k;
        final Setting.Num amount;
        final Setting.Num size;
        private final List<double[]> ps = new ArrayList<>();   // x y z vx vy vz age life seed color
        private final Random rnd = new Random();
        private long last;

        Fly(Kind k) {
            super("fx_" + k.id, k.name, k.desc, Category.WORLD_FX);
            this.k = k;
            icon(k.icon);
            fresh();
            amount = add(new Setting.Num("Menge", 10, 300, 10, 100, "%"));
            size = add(new Setting.Num("Größe", 0.5, 2, 0.1, 1, "x"));
        }

        @Override public void onDisable() { ps.clear(); }

        void render() {
            class_746 me = Mc.player();
            if (me == null) return;
            double vis = 1;
            if (k.night) vis = Ambient.night();
            if (k.day) vis = 1 - Ambient.night();
            if (vis < 0.02) return;
            long now = System.nanoTime();
            float dt = last == 0 ? 0.016f : Math.min(0.1f, (now - last) / 1e9f);
            last = now;
            double px = Mc.x(me), py = Mc.y(me), pz = Mc.z(me);
            int want = Math.max(1, Math.round(k.defCount * amount.getF() / 100f * (dev.lego.perf.Performance.boost == null ? 1f : dev.lego.perf.Performance.boost.fxScale())));
            int spawn = 0;
            while (ps.size() < want && spawn++ < 6) ps.add(spawn(px, py, pz, ps.isEmpty()));
            double t = System.currentTimeMillis() / 1000.0;
            class_2960[] tex = new class_2960[k.sprites.length];
            for (int i = 0; i < tex.length; i++) tex[i] = Ambient.tex(k.sprites[i]);
            class_2960 glow = k.glow ? Ambient.tex("glow") : null;
            for (int i = ps.size() - 1; i >= 0; i--) {
                double[] q = ps.get(i);
                q[6] += dt;
                double s = q[8];
                double w = k.wander * k.speed;
                if (k.sky) {
                    // flock: follows a slowly turning direction, wing flap
                    double dir = s * 0.01 + t * 0.03;
                    q[3] = Math.cos(dir) * k.speed;
                    q[5] = Math.sin(dir) * k.speed;
                    q[4] = Math.sin(t * 0.5 + s) * 0.3;
                } else if (k.swirl) {
                    double a = Math.atan2(q[2] - pz, q[0] - px) + 1.2;
                    q[3] += (Math.cos(a) * k.speed - q[3]) * dt;
                    q[5] += (Math.sin(a) * k.speed - q[5]) * dt;
                    q[4] += (Math.sin(t * 1.3 + s) * 0.6 - q[4]) * dt;
                } else {
                    q[3] += (Math.sin(t * 0.7 * k.speed + s) * w - q[3]) * dt * 1.5;
                    q[5] += (Math.cos(t * 0.6 * k.speed + s * 1.3) * w - q[5]) * dt * 1.5;
                    q[4] += ((Math.sin(t * 0.9 + s * 2) * 0.35 * w) + k.rise - k.fall - q[4]) * dt * 1.5;
                }
                q[0] += q[3] * dt; q[1] += q[4] * dt; q[2] += q[5] * dt;
                double dx = q[0] - px, dz = q[2] - pz;
                if (q[6] > q[7] || dx * dx + dz * dz > (k.radius * 1.4) * (k.radius * 1.4) || i >= want) { ps.remove(i); continue; }
                double fade = Math.min(1, Math.min(q[6] / 1.2, (q[7] - q[6]) / 1.2)) * vis;
                int rgb = (int) q[9];
                double sz = k.size * size.get() * (0.75 + (s % 1) * 0.5);
                int frame = tex.length > 1 ? (int) ((t * k.flapHz + s) % tex.length) : 0;
                if (glow != null) R3.glowBillboard(glow, q[0], q[1], q[2], sz * 3.2, R3.argb(rgb, 0.35 * fade));
                if (k.trail) for (int j = 1; j <= 4; j++)
                    R3.glowBillboard(tex[0], q[0] - q[3] * 0.06 * j, q[1] - q[4] * 0.06 * j, q[2] - q[5] * 0.06 * j, sz * (1 - j * 0.18), R3.argb(rgb, 0.4 * fade * (1 - j * 0.2)));
                if (k.glow) R3.glowBillboard(tex[frame], q[0], q[1], q[2], sz, R3.argb(rgb, fade));
                else if (k.rotate) R3.billboardRot(tex[frame], q[0], q[1], q[2], sz, t * 1.4 + s, R3.argb(rgb, fade));
                else R3.billboard(tex[frame], q[0], q[1], q[2], sz, R3.argb(rgb, fade));
            }
        }

        private double[] spawn(double px, double py, double pz, boolean first) {
            double a = rnd.nextDouble() * Math.PI * 2, r = Math.sqrt(rnd.nextDouble()) * k.radius;
            double x = px + Math.cos(a) * r, z = pz + Math.sin(a) * r;
            int ground = Ambient.top(x, z);
            double base = k.sky ? py : (ground > py + 6 || ground < py - 10 ? py : ground);
            double y = base + k.hMin + rnd.nextDouble() * (k.hMax - k.hMin);
            double[] q = new double[10];
            q[0] = x; q[1] = y; q[2] = z;
            q[6] = first ? rnd.nextDouble() * k.life * 0.5 : 0;
            q[7] = k.life * (0.7 + rnd.nextDouble() * 0.6);
            q[8] = rnd.nextDouble() * 1000;
            q[9] = k.colors[rnd.nextInt(k.colors.length)];
            return q;
        }
    }

    /** Inside R3 begin/end. */
    static void render() {
        for (Fly f : ALL) if (f.enabled) f.render();
    }
}
