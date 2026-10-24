package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Premium 3D cosmetics: registration, hats and face items (head space: head x -4..4, y 0..8, z -4..4).
 * Back items live in {@link Cos3Back}, auras and wings in {@link Cos3Aura}.
 */
final class Cos3Models {
    private Cos3Models() { }

    static void registerAll() {
        // ---------------------------------------------------------------- capes
        Cos.cape("c3void", "Leerenmantel", Rarity.LEGENDARY);
        Cos.cape("c3nether", "Netherglut", Rarity.EPIC);
        Cos.cape("c3heaven", "Himmelsgold", Rarity.LEGENDARY);
        Cos.cape("c3sunflower", "Sonnenblume", Rarity.RARE);
        Cos.cape("c3pumpkin", "Kürbislaterne", Rarity.EPIC);
        Cos.cape("c3ghost", "Geisterchen", Rarity.EPIC);
        Cos.cape("c3skull", "Leuchtschädel", Rarity.EPIC);
        Cos.cape("c3bolt_purple", "Neonblitz Lila", Rarity.EPIC);
        Cos.cape("c3bolt_blue", "Neonblitz Blau", Rarity.EPIC);
        Cos.cape("c3bolt_green", "Neonblitz Grün", Rarity.EPIC);
        Cos.cape("c3bolt_orange", "Neonblitz Orange", Rarity.EPIC);
        Cos.cape("c3embertree", "Glutbaum", Rarity.LEGENDARY);
        // ---------------------------------------------------------------- hats
        Cos.add("hat_c3_crystalcrown", "Schwebende Kristallkrone", Slot.HAT, Rarity.LEGENDARY, Cos3Models::crystalCrown);
        Cos.add("hat_c3_planetring", "Planetenring", Slot.HAT, Rarity.LEGENDARY, Cos3Models::planetRing);
        Cos.add("hat_c3_rayhalo", "Strahlenkranz", Slot.HAT, Rarity.EPIC, Cos3Models::rayHalo);
        Cos.add("hat_c3_flamecrown", "Flammenkrone", Slot.HAT, Rarity.LEGENDARY, Cos3Models::flameCrown);
        Cos.add("hat_c3_bubble", "Blasenhelm", Slot.HAT, Rarity.EPIC, Cos3Models::bubbleHelmet);
        Cos.add("hat_c3_catphones", "Katzen-Kopfhörer", Slot.HAT, Rarity.EPIC, Cos3Models::catPhones);
        Cos.add("hat_c3_snowglobe", "Schneekugel", Slot.HAT, Rarity.EPIC, Cos3Models::snowGlobe);
        Cos.add("hat_c3_pumpkin", "Kürbiskopf", Slot.HAT, Rarity.EPIC, Cos3Models::pumpkinHat);
        Cos.add("hat_c3_band_bolt", "Blitz-Bandana", Slot.HAT, Rarity.RARE, (g, a) -> bandana(g, a, 0));
        Cos.add("hat_c3_band_fire", "Flammen-Bandana", Slot.HAT, Rarity.RARE, (g, a) -> bandana(g, a, 1));
        Cos.add("hat_c3_band_ice", "Frost-Stirnband", Slot.HAT, Rarity.RARE, (g, a) -> bandana(g, a, 2));
        // ---------------------------------------------------------------- face
        Cos.add("face_c3_holovisor", "Holo-Visier", Slot.FACE, Rarity.LEGENDARY, Cos3Models::holoVisor);
        Cos.add("face_c3_neonshades", "Neon-Shades", Slot.FACE, Rarity.EPIC, Cos3Models::neonShades);
        // ---------------------------------------------------------------- back, auras, wings
        Cos3Back.register();
        Cos3Aura.register();
    }

    // ================================================================== shared pieces
    /** Teardrop flame (lathe) standing on the origin, height h, radius r. */
    static void flame(G g, float h, float r, int outer, int inner) {
        boolean gl = g.glow;
        int c = g.color;
        g.glow(true);
        g.color(outer);
        lathe(g, "c3_flamegrad", new float[]{0, r * 0.85f, r, r * 0.72f, r * 0.36f, 0}, new float[]{0, h * 0.09f, h * 0.24f, h * 0.48f, h * 0.74f, h}, 10);
        g.color(inner);
        lathe(g, "white", new float[]{0, r * 0.5f, r * 0.58f, r * 0.4f, 0}, new float[]{h * 0.04f, h * 0.12f, h * 0.26f, h * 0.46f, h * 0.68f}, 7);
        g.glow(gl);
        g.color(c);
    }

    /** Point on the rounded square loop (corner centers +-c, radius R) at angle a (radians, 0 = +x, PI/2 = +z). */
    static float[] loopPt(float c, float R, double a) {
        float ca = (float) Math.cos(a), sa = (float) Math.sin(a);
        float sx = Math.abs(ca) < 1e-4f ? 0 : Math.signum(ca), sz = Math.abs(sa) < 1e-4f ? 0 : Math.signum(sa);
        return new float[]{sx * c + R * ca, sz * c + R * sa};
    }

    /** Ring made of a closed rectangular profile (flat band) around y: radii r0..r1, heights y0..y1. */
    static void flatRing(G g, String t, float r0, float r1, float y0, float y1, int seg) {
        lathe(g, t, new float[]{r0, r1, r1, r0, r0}, new float[]{y0, y0, y1, y1, y0}, seg);
    }

    /** Sparkle: tiny glowing octahedron. */
    static void sparkle(G g, float x, float y, float z, float s, int argb) {
        boolean gl = g.glow;
        int c = g.color;
        g.glow(true).color(argb);
        g.push();
        g.translate(x, y, z);
        gem(g, "white", s * 0.5f, s, s, 4);
        g.pop();
        g.glow(gl).color(c);
    }

    // ================================================================== floating crystal crown
    static void crystalCrown(G g, A a) {
        float t = a.time, bob = sin(t * 2) * 0.35f;
        g.push();
        g.translate(0, 9.7f + bob, 0);
        g.rotY(t * 16);
        g.color(0xFFFFD36A);
        flatRing(g, "c3_metal", 4.3f, 5.0f, 0, 0.75f, 24);
        g.color(0xFFFFE9A8);
        g.push();
        g.translate(0, 0.75f, 0);
        g.torus("c3_metal", 4.65f, 0.2f, 24, 4, 0, 0, 1, 1);
        g.pop();
        for (int i = 0; i < 8; i++) {
            float ang = i * 45;
            boolean big = i % 2 == 0;
            g.push();
            g.rotY(ang);
            g.translate(4.65f, 0.55f, 0);
            g.rotZ(big ? 8 : 14);
            g.glow(true).color(big ? 0xE08AE8FF : 0xE0C88AFF);
            shard(g, "c3_crystal", big ? 0.62f : 0.45f, big ? 3.8f : 2.4f);
            g.glow(false);
            g.pop();
            // gem set into the band between the crystals
            g.push();
            g.rotY(ang + 22.5f);
            g.translate(5.0f, 0.38f, 0);
            g.rotZ(-90);
            g.glow(true).color(i % 2 == 0 ? 0xFFFF4A7A : 0xFF4AE8FF);
            gem(g, "c3_crystal", 0.3f, 0.28f, 0.12f, 6);
            g.glow(false);
            g.pop();
        }
        // center crystal hovering above, counter rotating
        g.push();
        g.translate(0, 3.2f + sin(t * 2.6f) * 0.3f, 0);
        g.rotY(-t * 50);
        g.glow(true).color(0xF0E8F4FF);
        gem(g, "c3_crystal", 1.0f, 1.9f, 1.5f, 6);
        g.pop();
        halo(g, 0, 3.2f, 0, 4.5f, 0x807AD8FF);
        g.pop();
        for (int i = 0; i < 5; i++) {
            float ph = fract(t * 0.35f + i * 0.2f), ang = i * 72 + t * 30;
            float x = cos(Math.toRadians(ang)) * 5.8f, z = sin(Math.toRadians(ang)) * 5.8f;
            sparkle(g, x, 9.2f + ph * 5, z, 0.35f * (1 - ph) + 0.1f, alpha(1 - ph, 0xD8F4FF));
        }
    }

    // ================================================================== planet ring around the head
    static void planetRing(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 5.2f, 0);
        g.rotZ(16);
        g.rotX(-9);
        g.push();
        g.rotY(t * 12);
        g.color(0xFFFFFFFF);
        g.ring("c3_ring", 6.7f, 9.8f, 0.14f, 48, 0, 0, 1, 1);
        g.ring("c3_ring", 6.7f, 9.8f, -0.14f, 48, 0, 0, 1, 1);
        g.color(0xFFE8D0A0);
        g.cylinder("white", 9.8f, 9.8f, -0.14f, 0.14f, 48, false, 0, 0, 1, 1);
        g.cylinder("white", 6.7f, 6.7f, -0.14f, 0.14f, 48, false, 0, 0, 1, 1);
        g.glow(true).color(0xC87AE8FF);
        g.torus("white", 6.45f, 0.09f, 40, 3, 0, 0, 1, 1);
        g.glow(false);
        g.pop();
        // moons outside the ring
        String[] tex = {"c3_planet_gas", "c3_planet_ice", "c3_planet_lava"};
        float[] rad = {1.15f, 0.85f, 0.95f}, sp = {26, -34, 40}, dist = {11.3f, 11.0f, 11.6f};
        for (int i = 0; i < 3; i++) {
            float ang = t * sp[i] + i * 120;
            g.push();
            g.rotY(ang);
            g.translate(dist[i], sin(t * 1.5f + i) * 0.4f, 0);
            g.rotY(-t * 60);
            g.color(0xFFFFFFFF);
            g.sphere(tex[i], rad[i], 14, 8, 0, 0, 1, 1);
            if (i == 2) halo(g, 0, 0, 0, rad[i] * 4, 0x60FF6A2A);
            if (i == 0) {
                g.rotZ(20);
                g.color(0xFFFFF0D8);
                g.ring("c3_ring", rad[i] * 1.35f, rad[i] * 2.0f, 0, 20, 0, 0, 1, 1);
            }
            g.pop();
        }
        g.pop();
    }

    // ================================================================== halo with light rays
    static void rayHalo(G g, A a) {
        float t = a.time, bob = sin(t * 1.8f) * 0.3f;
        g.push();
        g.translate(0, 11.8f + bob, -0.4f);
        g.rotX(8);
        g.glow(true).color(0xFFFFE27A);
        g.torus("c3_metal", 4.3f, 0.42f, 32, 6, 0, 0, 1, 1);
        g.color(0xB0FFF6D0);
        g.torus("white", 5.3f, 0.1f, 32, 3, 0, 0, 1, 1);
        for (int i = 0; i < 16; i++) {
            float ang = i * 22.5f + t * 14;
            float len = 2.2f + 1.8f * (0.5f + 0.5f * sin(t * 3 + i * 1.7f)) + (i % 2 == 0 ? 1.2f : 0);
            g.push();
            g.rotY(ang);
            g.color(alpha(0.75, i % 2 == 0 ? 0xFFF4B0 : 0xFFE08A));
            limbRay(g, 4.85f, len, i % 2 == 0 ? 0.26f : 0.18f);
            g.pop();
        }
        g.glow(false);
        g.pop();
        for (int i = 0; i < 4; i++) {
            float ph = fract(t * 0.4f + i * 0.25f), ang = i * 90 + t * 20;
            sparkle(g, cos(Math.toRadians(ang)) * 5.6f, 12.6f - ph * 2.5f, sin(Math.toRadians(ang)) * 5.6f - 0.4f, 0.3f, alpha(1 - ph, 0xFFF4C0));
        }
    }

    /** Radial light ray from radius r0 outwards along +x (tapered tube with fading texture). */
    private static void limbRay(G g, float r0, float len, float w) {
        Geo.tube(g, "c3_ray", new float[]{r0, 0, 0}, new float[]{r0 + len, 0, 0}, w, 0.02f, 4);
    }

    // ================================================================== crown of flames
    static void flameCrown(G g, A a) {
        float t = a.time;
        float c = 2.6f, r = 2.25f, th = 0.55f;
        g.color(0xFF34343E);
        band(g, "c3_metal", c, r, th, 6.4f, 8.7f, 0, 3);
        g.color(0xFFFFC04A);
        band(g, "c3_metal", c, r + th - 0.05f, 0.2f, 6.2f, 6.65f, 0, 3);
        band(g, "c3_metal", c, r + th - 0.05f, 0.2f, 8.45f, 8.9f, 0, 3);
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4;
            float[] p = loopPt(c, r + th * 0.5f, ang);
            // iron spike
            g.push();
            g.translate(p[0], 8.7f, p[1]);
            g.color(0xFF2A2A32);
            shard(g, "c3_metal", 0.42f, 1.5f);
            g.pop();
            // flame on top of every spike
            float fl = 1 + 0.2f * sin(t * 9 + i * 1.3f) + 0.1f * sin(t * 14.3f + i * 2.1f);
            g.push();
            g.translate(p[0], 9.6f, p[1]);
            g.rotZ(sin(t * 3 + i) * 6);
            g.scale(1, fl, 1);
            flame(g, i % 2 == 0 ? 3.6f : 2.8f, 0.85f, 0xE8FF7A1A, 0xF0FFF0A0);
            g.pop();
            // gems on the band
            float[] q = loopPt(c, r + th + 0.05f, ang + Math.PI / 8);
            g.push();
            g.translate(q[0], 7.55f, q[1]);
            g.rotY((float) -Math.toDegrees(ang + Math.PI / 8));
            g.rotZ(-90);
            g.glow(true).color(0xFFFF3A2A);
            gem(g, "c3_crystal", 0.42f, 0.34f, 0.14f, 6);
            g.glow(false);
            g.pop();
        }
        for (int i = 0; i < 9; i++) {
            float ph = fract(t * 0.7f + i * 0.111f);
            float[] p = loopPt(c, r + th * 0.5f, i * 0.7);
            sparkle(g, p[0] * (1 - ph * 0.3f), 11 + ph * 5, p[1] * (1 - ph * 0.3f), 0.28f, alpha((1 - ph) * 0.9, ph < 0.4 ? 0xFFE07A : 0xFF7A2A));
        }
    }

    // ================================================================== bubble helmet
    static void bubbleHelmet(G g, A a) {
        float t = a.time;
        g.color(0xFFE8ECF4);
        g.push();
        g.translate(0, 0.75f, 0);
        g.torus("c3_metal", 6.4f, 0.45f, 28, 6, 0, 0, 1, 1);
        for (int i = 0; i < 8; i++) {
            double an = i * Math.PI / 4 + Math.PI / 8;
            boolean on = ((int) Math.floor(t * 3) + i) % 4 != 0;
            g.glow(true).color(on ? (i % 2 == 0 ? 0xFF4AE8FF : 0xFFFF6AD8) : 0xFF2A3A44);
            ball(g, "white", (float) Math.cos(an) * 6.4f, 0.4f, (float) Math.sin(an) * 6.4f, 0.22f, 6);
        }
        g.glow(false);
        g.pop();
        // glass
        int n = 11;
        float R = 7.4f, cy = 4;
        float[] rr = new float[n + 1], yy = new float[n + 1];
        double p0 = Math.asin((0.75 - cy) / R);
        for (int i = 0; i <= n; i++) {
            double p = p0 + (Math.PI / 2 - p0) * i / n;
            rr[i] = (float) Math.cos(p) * R;
            yy[i] = cy + (float) Math.sin(p) * R;
        }
        rr[n] = 0;
        // antenna
        g.color(0xFFC8CED8);
        Geo.tube(g, "c3_metal", new float[]{0, 11.2f, 0}, new float[]{0, 13.6f, 0}, 0.16f, 0.12f, 6);
        g.glow(true).color(((int) (t * 2)) % 2 == 0 ? 0xFFFF4A6A : 0xFF7A1A2A);
        ball(g, "white", 0, 13.8f, 0, 0.42f, 8);
        halo(g, 0, 13.8f, 0, 2.2f, 0x70FF4A6A);
        g.glow(false).color(0xFFD8F6FF);
        g.push();
        g.rotY(t * 8);
        lathe(g, "c3_glass", rr, yy, 28, 0, 0, 1, 1);
        g.pop();
    }

    // ================================================================== cat ear headphones
    static void catPhones(G g, A a) {
        float t = a.time;
        int neon = 0xFF000000 | Capes.hsv(0.88 + 0.08 * Math.sin(t * 1.5), 0.55, 1);
        float[][] half = {{4.9f, 3.0f}, {4.95f, 6.0f}, {4.75f, 8.3f}, {3.4f, 9.2f}, {1.6f, 9.45f}, {0, 9.5f}};
        float[][] pts = new float[half.length * 2 - 1][];
        for (int i = 0; i < half.length; i++) {
            pts[i] = new float[]{half[i][0], half[i][1], 0};
            pts[pts.length - 1 - i] = new float[]{-half[i][0], half[i][1], 0};
        }
        float[] rad = new float[pts.length];
        java.util.Arrays.fill(rad, 0.4f);
        g.color(0xFFF2F0F6);
        Geo.chain(g, "c3_metal", pts, rad, 8);
        // cushion on the band
        float[][] cp = {{-3.0f, 9.35f, 0}, {-1.5f, 9.55f, 0}, {0, 9.6f, 0}, {1.5f, 9.55f, 0}, {3.0f, 9.35f, 0}};
        g.color(0xFF3A3A46);
        limb(g, "c3_fur", cp, new float[]{0.5f, 0.55f, 0.55f, 0.55f, 0.5f}, 8);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.scale(s, 1, 1);
            // cup
            g.color(0xFF34343E);
            bbox(g, "c3_fur", 4.4f, 1.5f, -2.1f, 5.4f, 5.9f, 2.1f, 0.4f);
            g.color(0xFFFFC8E0);
            bbox(g, "c3_metal", 5.3f, 1.2f, -2.4f, 6.6f, 6.2f, 2.4f, 0.55f);
            g.push();
            g.translate(6.62f, 3.7f, 0);
            g.rotZ(90);
            g.glow(true).color(neon);
            g.torus("white", 1.45f, 0.18f, 20, 4, 0, 0, 1, 1);
            g.color(0xFFFFFFFF);
            g.pop();
            // speaker grille disc in the middle of the cup
            g.glow(false).color(0xFFFFFFFF);
            g.push();
            g.translate(6.55f, 3.7f, 0);
            g.scale(0.25f, 1.0f, 1.0f);
            g.sphere("c3_grille", 1, 12, 6, 0, 0, 1, 1);
            g.pop();
            // ear
            g.push();
            g.translate(2.7f, 9.1f, 0);
            g.rotZ(-14);
            float[][] ear = {{-1.7f, 0}, {1.6f, 0}, {0.35f, 3.3f}};
            g.color(0xFFF2F0F6);
            extrude(g, "c3_metal", ear, -0.5f, 0.5f);
            float[][] in = {{-1.0f, 0.45f}, {0.95f, 0.45f}, {0.25f, 2.35f}};
            g.glow(true).color(neon);
            extrude(g, "white", in, 0.2f, 0.66f);
            extrude(g, "white", in, -0.66f, -0.2f);
            g.glow(false);
            g.pop();
            g.pop();
        }
        // little music notes floating up (glow)
        for (int i = 0; i < 2; i++) {
            float ph = fract(t * 0.45f + i * 0.5f);
            float s = i == 0 ? 1 : -1;
            g.push();
            g.translate(s * (7.6f + ph * 1.5f), 5 + ph * 6, sin(t * 2 + i) * 0.8f);
            g.glow(true).color(alpha((1 - ph), neon & 0xFFFFFF));
            ball(g, "white", 0, 0, 0, 0.42f, 6);
            Geo.tube(g, "white", new float[]{0.35f, 0, 0}, new float[]{0.35f, 1.6f, 0}, 0.1f, 0.1f, 4);
            bbox(g, "white", 0.3f, 1.3f, -0.1f, 1.2f, 1.65f, 0.1f, 0.05f);
            g.glow(false);
            g.pop();
        }
    }

    // ================================================================== snow globe hat
    static void snowGlobe(G g, A a) {
        float t = a.time;
        g.push();
        g.translate(0, 8.6f, 0);
        g.rotZ(sin(t * 1.4f) * 3);
        g.rotX(sin(t * 1.1f + 1) * 2);
        g.color(0xFF6A3E1E);
        lathe(g, "c3_lacquer", new float[]{0, 3.5f, 3.7f, 3.4f, 3.0f, 2.8f, 0}, new float[]{0, 0, 0.8f, 1.5f, 1.9f, 2.3f, 2.3f}, 20);
        g.color(0xFFFFD36A);
        g.push();
        g.translate(0, 1.55f, 0);
        g.torus("c3_metal", 3.42f, 0.2f, 22, 4, 0, 0, 1, 1);
        g.pop();
        float cy = 5.85f, R = 3.8f;
        // snowy ground
        g.color(0xFFF4F8FF);
        Geo.ellipsoid(g, "c3_fur", 0, 3.25f, 0, 2.55f, 0.75f, 2.55f, 14, 6);
        // pine tree
        g.color(0xFF2A7A4A);
        lathe(g, "c3_fur", new float[]{0, 1.7f, 0}, new float[]{3.6f, 4.3f, 6.2f}, 10);
        g.color(0xFF33905A);
        lathe(g, "c3_fur", new float[]{0, 1.35f, 0}, new float[]{5.2f, 5.8f, 7.4f}, 10);
        g.color(0xFF3AA868);
        lathe(g, "c3_fur", new float[]{0, 0.95f, 0}, new float[]{6.6f, 7.1f, 8.4f}, 10);
        g.push();
        g.translate(0, 8.55f, 0);
        g.rotY(t * 60);
        g.glow(true).color(0xFFFFE27A);
        gem(g, "white", 0.38f, 0.4f, 0.3f, 5);
        g.pop();
        halo(g, 0, 8.55f, 0, 1.8f, 0x90FFE27A);
        // fairy lights on the tree
        for (int i = 0; i < 7; i++) {
            float yy = 4.6f + i * 0.55f, rr = 1.65f - i * 0.2f, an = i * 2.4f;
            boolean on = ((int) (t * 3) + i) % 3 != 0;
            g.glow(true).color(on ? (i % 3 == 0 ? 0xFFFF4A4A : i % 3 == 1 ? 0xFF4AE8FF : 0xFFFFE04A) : 0xFF404040);
            ball(g, "white", cos(an) * rr, yy, sin(an) * rr, 0.13f, 4);
        }
        g.glow(false);
        // present
        g.color(0xFFE8304A);
        bbox(g, "white", 1.2f, 3.3f, 0.5f, 2.0f, 4.1f, 1.3f, 0.08f);
        g.color(0xFFFFE27A);
        bbox(g, "white", 1.51f, 3.28f, 0.48f, 1.69f, 4.14f, 1.32f, 0.03f);
        // swirling snow
        java.util.Random rn = new java.util.Random(5);
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 26; i++) {
            float h = rn.nextFloat(), an = rn.nextFloat() * 6.28f + t * (0.6f + rn.nextFloat() * 0.6f);
            float y = 3.6f + fract(h - t * 0.12f) * 5.4f;
            float dy = y - cy, maxR = (float) Math.sqrt(Math.max(0.2, (R - 0.5f) * (R - 0.5f) - dy * dy));
            float rr = maxR * (0.3f + 0.7f * rn.nextFloat());
            g.push();
            g.translate(cos(an) * rr, y, sin(an) * rr);
            g.rotY(an * 57 + t * 90);
            g.box("white", -0.1f, -0.1f, -0.1f, 0.1f, 0.1f, 0.1f, 0, 0, 1, 1);
            g.pop();
        }
        // glass
        g.color(0xFFE8F8FF);
        g.push();
        g.translate(0, cy, 0);
        g.rotY(20);
        g.sphere("c3_glass", R, 22, 12, 0, 0, 1, 1);
        g.pop();
        g.pop();
    }

    // ================================================================== pumpkin hat (carved face glows)
    static void pumpkinHat(G g, A a) {
        float t = a.time;
        float fl = 0.8f + 0.12f * sin(t * 11) + 0.08f * sin(t * 17.3f);
        g.push();
        g.translate(0, 8.4f, 0);
        g.rotZ(6);
        g.translate(0, 2.95f, 0);
        g.color(0xFFFF8A1A);
        for (int i = 0; i < 8; i++) {
            g.push();
            g.rotY(-i * 45);
            g.color(i % 2 == 0 ? 0xFFFF8A1A : 0xFFF07A12);
            Geo.ellipsoid(g, "c3_pumpkin", 1.6f, 0, 0, 2.1f, 2.7f, 1.8f, 12, 8);
            g.pop();
        }
        // stem + curly vine + leaf
        g.color(0xFF5A7A2A);
        limb(g, "c3_fur", new float[][]{{0, 2.3f, 0}, {0.1f, 3.2f, 0}, {0.5f, 3.9f, 0.1f}, {1.0f, 4.1f, 0.2f}}, new float[]{0.55f, 0.45f, 0.35f, 0.3f}, 7);
        g.color(0xFF3A8A2A);
        float[][] vine = new float[10][];
        for (int i = 0; i < 10; i++) {
            float an = i * 0.7f, rr = 1.1f - i * 0.08f;
            vine[i] = new float[]{-0.3f - cos(an) * rr, 2.8f + i * 0.08f, sin(an) * rr};
        }
        float[] vr = new float[10];
        java.util.Arrays.fill(vr, 0.12f);
        Geo.chain(g, "white", vine, vr, 4);
        g.push();
        g.translate(0.5f, 2.9f, -0.6f);
        g.rotX(-70);
        g.rotZ(30);
        g.color(0xFF4AA83A);
        extrude(g, "c3_fur", new float[][]{{0, 0}, {0.9f, 0.5f}, {1.8f, 0.1f}, {2.2f, -0.6f}, {1.1f, -0.7f}}, -0.08f, 0.08f);
        g.pop();
        // face: glowing inlays poking out of the front lobe
        g.glow(true).color(0xFF000000 | Capes.mix(0xFF7A10, 0xFFE87A, fl));
        float[][] eyeL = {{0.5f, 0.2f}, {1.9f, 0.2f}, {1.2f, 1.35f}};
        float[][] eyeR = {{-0.5f, 0.2f}, {-1.2f, 1.35f}, {-1.9f, 0.2f}};
        float[][] nose = {{0.3f, -0.25f}, {0, 0.25f}, {-0.3f, -0.25f}};
        float[][] mouth = {{2.0f, -0.55f}, {1.2f, -1.0f}, {0.8f, -0.75f}, {0.4f, -1.1f}, {0, -0.85f}, {-0.4f, -1.1f}, {-0.8f, -0.75f}, {-1.2f, -1.0f}, {-2.0f, -0.55f}, {-1.1f, -1.75f}, {0, -1.95f}, {1.1f, -1.75f}};
        extrude(g, "white", eyeL, 2.3f, 3.62f);
        extrude(g, "white", eyeR, 2.3f, 3.62f);
        extrude(g, "white", nose, 2.3f, 3.85f);
        extrude(g, "white", mouth, 2.3f, 3.72f);
        halo(g, 0, 0, 4.2f, 5f, alpha(0.35 * fl, 0xFF9A2A));
        g.glow(false);
        g.pop();
    }

    // ================================================================== bandanas
    /** kind 0 = green lightning (energy crackle), 1 = flames, 2 = frost. */
    static void bandana(G g, A a, int kind) {
        float t = a.time;
        String tex = kind == 0 ? "c3_band_bolt" : kind == 1 ? "c3_band_fire" : "c3_band_ice";
        int glowC = kind == 0 ? 0xFFB8FF6A : kind == 1 ? 0xFFFFB02A : 0xFFB8F0FF;
        float c = 2.5f, r = 2.4f, th = 0.35f, y0 = 4.5f, y1 = 6.9f;
        g.color(0xFFFFFFFF);
        band(g, tex, c, r, th, y0, y1, 0, 2);
        float out = c + r + th;
        // knot at the back
        g.push();
        g.translate(0, 5.75f, -out - 0.25f);
        g.rotZ(8);
        bbox(g, tex, -0.85f, -0.8f, -0.45f, 0.85f, 0.8f, 0.45f, 0.35f, new float[]{0.1f, 0.2f, 0.2f, 0.8f});
        g.pop();
        // flapping tails
        float wind = a.preview ? 0.4f : 0.25f + a.move * 1.2f;
        for (int s = -1; s <= 1; s += 2) {
            int n = 7;
            float[][] yz = new float[n][];
            float[] w = new float[n];
            for (int i = 0; i < n; i++) {
                float f = i / (float) (n - 1);
                float flap = sin(t * (4 + wind * 4) - f * 3.2f + (s > 0 ? 0 : 1.3f)) * (0.25f + wind * 0.5f) * f;
                float back = f * f * (1.2f + wind * 3.2f);
                yz[i] = new float[]{5.6f - f * (5.2f - wind * 1.8f), -out - 0.5f - f * 1.4f - back + flap};
                w[i] = 1.25f - f * 0.35f;
            }
            ribbon(g, tex, s * 0.72f, yz, w, 0.2f);
        }
        // 3D emblems on the front and the sides
        for (int e = 0; e < 3; e++) {
            double ang = Math.PI / 2 + (e - 1) * 0.9;
            float[] p = loopPt(c, r + th - 0.1f, ang);
            g.push();
            g.translate(p[0], 5.7f, p[1]);
            g.rotY((float) Math.toDegrees(Math.PI / 2 - ang));
            float pul = 0.75f + 0.25f * sin(t * 4 + e * 2);
            g.glow(true).color(alpha(pul, glowC));
            if (kind == 0) {
                float[][] bolt = {{0.35f, 1.0f}, {-0.45f, -0.05f}, {0.0f, -0.05f}, {-0.3f, -1.0f}, {0.5f, 0.2f}, {0.05f, 0.2f}, {0.5f, 1.0f}};
                extrude(g, "white", bolt, -0.1f, 0.28f);
            } else if (kind == 1) {
                g.push();
                g.translate(0, -0.9f, 0.15f);
                g.scale(1, 0.9f + 0.2f * sin(t * 10 + e), 1);
                g.glow(false);
                flame(g, 1.9f, 0.5f, 0xE0FF5A1A, 0xF0FFE07A);
                g.pop();
            } else {
                g.rotX(90);
                shard(g, "c3_crystal", 0.28f, 0.9f);
                g.rotY(60);
                g.push();
                g.rotZ(50);
                shard(g, "c3_crystal", 0.2f, 0.7f);
                g.pop();
                g.push();
                g.rotZ(-50);
                shard(g, "c3_crystal", 0.2f, 0.7f);
                g.pop();
            }
            g.glow(false);
            g.pop();
        }
        if (kind == 0) {
            // energy crackle jumping around the band
            int step = (int) Math.floor(t * 5);
            for (int i = 0; i < 2; i++) {
                double h = Capes.hash(step, i, 17);
                if (h < 0.3) continue;
                double a0 = Capes.hash(step, i, 18) * Math.PI * 2, a1 = a0 + 0.5 + h * 0.8;
                float[] p0 = loopPt(c, r + th + 0.45f, a0), p1 = loopPt(c, r + th + 0.45f, a1);
                arc(g, new float[]{p0[0], 5.7f + (float) (h - 0.5) * 1.5f, p0[1]}, new float[]{p1[0], 5.7f - (float) (h - 0.5) * 1.5f, p1[1]}, 0.6f, 0.06f, step * 3 + i, t, 0xC0B8FF6A);
            }
        } else if (kind == 1) {
            for (int i = 0; i < 5; i++) {
                float ph = fract(t * 0.8f + i * 0.2f);
                float[] p = loopPt(c, r + th, Math.PI / 2 + (i - 2) * 0.55);
                sparkle(g, p[0], 7.2f + ph * 3, p[1], 0.22f, alpha(1 - ph, ph < 0.3 ? 0xFFE07A : 0xFF6A1A));
            }
        } else {
            for (int i = 0; i < 6; i++) {
                float ph = fract(t * 0.3f + i / 6f), an = i * 60 + t * 25;
                sparkle(g, cos(Math.toRadians(an)) * 6.6f, 5.7f - ph * 2.5f + 1.2f, sin(Math.toRadians(an)) * 6.6f, 0.25f, alpha(1 - ph, 0xE8FAFF));
            }
        }
    }

    // ================================================================== face: holo visor
    /** Curved strip along a squircle (radius R, exponent n) from angle a0 to a1 (degrees), heights y0..y1, thickness th. */
    static void arcBand(G g, String tex, float R, float n, float a0, float a1, float y0, float y1, float th, int seg) {
        float[][] in = new float[seg + 1][], out = new float[seg + 1][];
        for (int i = 0; i <= seg; i++) {
            double a = Math.toRadians(a0 + (a1 - a0) * i / seg);
            in[i] = Geo.sq(a, R, n);
            out[i] = Geo.sq(a, R + th, n);
        }
        for (int i = 0; i < seg; i++) {
            float ua = (float) i / seg, ub = (float) (i + 1) / seg;
            g.quad(tex, new float[]{out[i][0], y1, out[i][1]}, new float[]{out[i + 1][0], y1, out[i + 1][1]}, new float[]{out[i + 1][0], y0, out[i + 1][1]}, new float[]{out[i][0], y0, out[i][1]},
                    new float[]{1 - ua, 0, 1 - ub, 0, 1 - ub, 1, 1 - ua, 1});
            g.quad(tex, new float[]{in[i + 1][0], y1, in[i + 1][1]}, new float[]{in[i][0], y1, in[i][1]}, new float[]{in[i][0], y0, in[i][1]}, new float[]{in[i + 1][0], y0, in[i + 1][1]},
                    new float[]{1 - ub, 0, 1 - ua, 0, 1 - ua, 1, 1 - ub, 1});
            g.quad(tex, new float[]{in[i][0], y1, in[i][1]}, new float[]{in[i + 1][0], y1, in[i + 1][1]}, new float[]{out[i + 1][0], y1, out[i + 1][1]}, new float[]{out[i][0], y1, out[i][1]},
                    new float[]{1 - ua, 0, 1 - ub, 0, 1 - ub, 0.05f, 1 - ua, 0.05f});
            g.quad(tex, new float[]{out[i][0], y0, out[i][1]}, new float[]{out[i + 1][0], y0, out[i + 1][1]}, new float[]{in[i + 1][0], y0, in[i + 1][1]}, new float[]{in[i][0], y0, in[i][1]},
                    new float[]{1 - ua, 0.95f, 1 - ub, 0.95f, 1 - ub, 1, 1 - ua, 1});
        }
        for (int e = 0; e <= seg; e += seg)
            g.quad(tex, new float[]{in[e][0], y1, in[e][1]}, new float[]{out[e][0], y1, out[e][1]}, new float[]{out[e][0], y0, out[e][1]}, new float[]{in[e][0], y0, in[e][1]},
                    new float[]{0, 0, 0.05f, 0, 0.05f, 1, 0, 1});
    }

    static void holoVisor(G g, A a) {
        float t = a.time;
        float R = 4.95f, n = 6;
        // glass
        g.glow(true).color(0xFFFFFFFF);
        arcBand(g, "c3_visor", R, n, -12, 192, 2.6f, 5.1f, 0.28f, 36);
        g.glow(false);
        // frame rims (top / bottom)
        for (int e = 0; e < 2; e++) {
            float y = e == 0 ? 5.18f : 2.52f;
            int seg = 28;
            float[][] pts = new float[seg + 1][];
            for (int i = 0; i <= seg; i++) {
                float[] p = Geo.sq(Math.toRadians(-14 + 208.0 * i / seg), R + 0.14f, n);
                pts[i] = new float[]{p[0], y, p[1]};
            }
            float[] rr = new float[seg + 1];
            java.util.Arrays.fill(rr, 0.17f);
            g.color(0xFF2A2E38);
            Geo.chain(g, "c3_metal", pts, rr, 5);
        }
        // temple pods
        for (int s = -1; s <= 1; s += 2) {
            g.color(0xFF2A2E38);
            bbox(g, "c3_metal", s > 0 ? 4.45f : -5.75f, 2.2f, -1.9f, s > 0 ? 5.75f : -4.45f, 5.5f, 0.6f, 0.4f);
            for (int i = 0; i < 3; i++) {
                boolean on = ((int) (t * 4) + i + (s > 0 ? 0 : 1)) % 3 != 0;
                g.glow(true).color(on ? 0xFF4AE8FF : 0xFF1A4A5A);
                ball(g, "white", s * 5.8f, 4.6f - i * 0.8f, -0.6f, 0.2f, 5);
            }
            g.glow(false);
        }
        // scanning bar
        double sa = Math.toRadians(90 + 78 * Math.sin(t * 1.8));
        float[] sp = Geo.sq(sa, R + 0.33f, n);
        g.push();
        g.translate(sp[0], 3.85f, sp[1]);
        g.rotY((float) Math.toDegrees(Math.PI / 2 - sa));
        g.glow(true).color(0xE0E8FFFF);
        bbox(g, "white", -0.09f, -1.2f, -0.05f, 0.09f, 1.2f, 0.07f, 0.02f);
        g.pop();
        halo(g, sp[0] * 1.02f, 3.85f, sp[1] * 1.02f, 1.6f, 0x604AE8FF);
        // floating HUD reticle in front of one eye
        g.push();
        g.translate(-2.0f, 3.9f, R + 0.95f);
        g.rotZ(t * 40);
        g.glow(true).color(0xB04AE8FF);
        g.plane("c3_hex", 0, 0, 1.8f, 1.8f, 0, 0, 1, 1);
        g.glow(false);
        g.pop();
    }

    // ================================================================== face: neon shades
    static void neonShades(G g, A a) {
        float t = a.time;
        int neon = 0xFF000000 | Capes.hsv(0.8 + 0.12 * Math.sin(t * 1.2), 0.75, 1);
        int seg = 18;
        for (int s = -1; s <= 1; s += 2) {
            float cx = s * 2.1f, cy = 3.75f;
            float[][] poly = new float[seg][];
            float[][] rim = new float[seg + 1][];
            for (int i = 0; i < seg; i++) {
                double an = Math.PI * 2 * i / seg;
                float[] q = Geo.sq(an, 1, 3.2f);
                float wx = 1.65f, wy = q[1] < 0 ? 1.25f : 1.0f;      // slightly deeper at the bottom
                float xx = q[0] * wx, yy = q[1] * wy;
                if (s < 0) xx = -xx;
                poly[i] = new float[]{cx + xx, cy + yy};
                rim[i] = new float[]{cx + xx * 1.07f, cy + yy * 1.08f, 4.5f};
            }
            if (s < 0) java.util.Collections.reverse(java.util.Arrays.asList(poly));
            rim[seg] = rim[0];
            g.color(0xE0FFFFFF);
            extrude(g, "c3_lens", poly, 4.36f, 4.6f);
            float[] rr = new float[seg + 1];
            java.util.Arrays.fill(rr, 0.2f);
            g.glow(true).color(neon);
            Geo.chain(g, "white", rim, rr, 5);
            // temple arm
            float[][] arm = {{s * 3.85f, 4.35f, 4.45f}, {s * 4.5f, 4.35f, 3.7f}, {s * 4.52f, 4.3f, 0}, {s * 4.52f, 3.6f, -2.2f}};
            Geo.chain(g, "white", arm, new float[]{0.19f, 0.18f, 0.16f, 0.14f}, 5);
            g.glow(false);
        }
        // bridge
        g.glow(true).color(neon);
        Geo.chain(g, "white", new float[][]{{0.45f, 4.25f, 4.5f}, {0, 4.45f, 4.5f}, {-0.45f, 4.25f, 4.5f}}, new float[]{0.2f, 0.2f, 0.2f}, 5);
        g.glow(false);
        // glint sweeping over the lenses
        float ph = fract(t * 0.35f);
        if (ph < 0.3f) {
            float x = 4 - ph / 0.3f * 8;
            sparkle(g, x, 4.3f, 4.75f, 0.35f, 0xE0FFFFFF);
        }
    }
}
