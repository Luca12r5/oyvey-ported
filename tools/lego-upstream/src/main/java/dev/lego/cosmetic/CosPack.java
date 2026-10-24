package dev.lego.cosmetic;

import static dev.lego.cosmetic.Cos3Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/**
 * Kosmetik-Paket 4: hats, face items, back items, auras and wings - real 3D models built from lathes, boxes,
 * spheres and tori, most of them in several colorways. Head space: head x -4..4, y 0..8, face at z = +4.
 * Body space: neck at y = 0, back at z = -2, feet at y = -24.
 */
final class CosPack {
    private CosPack() { }

    private static final Rarity C = Rarity.COMMON, R = Rarity.RARE, E = Rarity.EPIC, L = Rarity.LEGENDARY;

    static void register() {
        hats();
        faces();
        backs();
        auras();
        wings();
        CapePack.register();
    }

    private static void hat(String id, String name, Rarity r, Cos.Model m) { Cos.add("hat_p4_" + id, name, Slot.HAT, r, m); }
    private static void face(String id, String name, Rarity r, Cos.Model m) { Cos.add("face_p4_" + id, name, Slot.FACE, r, m); }
    private static void back(String id, String name, Rarity r, Cos.Model m) { Cos.add("back_p4_" + id, name, Slot.BACK, r, m); }
    private static void aura(String id, String name, Rarity r, Cos.Model m) { Cos.add("aura_p4_" + id, name, Slot.AURA, r, m); }
    private static void wing(String id, String name, Rarity r, Cos.Model m) { Cos.add("wings_p4_" + id, name, Slot.WINGS, r, m); }

    private static int dark(int c, float k) { return 0xFF000000 | Capes.mix(c & 0xFFFFFF, 0x000000, k); }
    private static int light(int c, float k) { return 0xFF000000 | Capes.mix(c & 0xFFFFFF, 0xFFFFFF, k); }

    // ================================================================== HATS
    private static void hats() {
        int[] beanie = {0xFFD7263D, 0xFF2E86DE, 0xFF27AE60, 0xFFF39C12};
        String[] beanieN = {"Rote Wollmütze", "Blaue Wollmütze", "Grüne Wollmütze", "Orange Wollmütze"};
        for (int i = 0; i < 4; i++) { int c = beanie[i]; hat("beanie" + i, beanieN[i], C, (g, a) -> beanie(g, a, c)); }
        int[] cap = {0xFFE3000B, 0xFF1B1B22, 0xFF006CB7, 0xFFFFFFFF};
        String[] capN = {"Basecap Rot", "Basecap Schwarz", "Basecap Blau", "Basecap Weiß"};
        for (int i = 0; i < 4; i++) { int c = cap[i]; boolean lg = i == 1; hat("cap" + i, capN[i], C, (g, a) -> cap(g, c, lg)); }
        int[] party = {0xFFFF4FA3, 0xFF4FC3FF, 0xFFFFD23F};
        String[] partyN = {"Partyhut Pink", "Partyhut Hellblau", "Partyhut Gelb"};
        for (int i = 0; i < 3; i++) { int c = party[i]; hat("party" + i, partyN[i], R, (g, a) -> partyHat(g, a, c)); }
        hat("chef", "Sterne-Kochmütze", R, CosPack::chefHat);
        int[] cow = {0xFF8B5A2B, 0xFF2B2B2B, 0xFFE8D5B0};
        String[] cowN = {"Cowboyhut Braun", "Cowboyhut Schwarz", "Cowboyhut Sand"};
        for (int i = 0; i < 3; i++) { int c = cow[i]; hat("cowboy" + i, cowN[i], R, (g, a) -> cowboy(g, c)); }
        hat("tophat_red", "Zylinder Rot", R, (g, a) -> topHat(g, 0xFFB0121B, 0xFF1B1B22));
        hat("tophat_white", "Zylinder Weiß", E, (g, a) -> topHat(g, 0xFFF4F4F4, 0xFFE3000B));
        hat("fez_red", "Fes Rot", R, (g, a) -> fez(g, a, 0xFFC0141C));
        hat("fez_blue", "Fes Blau", R, (g, a) -> fez(g, a, 0xFF1F4FB0));
        hat("sombrero", "Sombrero", E, CosPack::sombrero);
        hat("grad_black", "Doktorhut", R, (g, a) -> gradCap(g, a, 0xFF1B1B22, 0xFFFFC53D));
        hat("grad_blue", "Abschlusshut Blau", R, (g, a) -> gradCap(g, a, 0xFF183A8C, 0xFFFFFFFF));
        hat("hardhat_y", "Bauhelm Gelb", C, (g, a) -> hardHat(g, 0xFFFFC107));
        hat("hardhat_w", "Bauhelm Weiß", C, (g, a) -> hardHat(g, 0xFFF2F2F2));
        hat("viking", "Wikingerhelm Eisen", E, (g, a) -> viking(g, 0xFF9AA3AD, 0xFFF2E8D0));
        hat("viking_gold", "Goldener Wikingerhelm", L, (g, a) -> viking(g, 0xFFE8B92C, 0xFFFFFFFF));
        hat("propeller", "Propellermütze Rot-Gelb", R, (g, a) -> propeller(g, a, 0xFFE3000B, 0xFFFFCD03));
        hat("propeller2", "Propellermütze Bunt", R, (g, a) -> propeller(g, a, 0xFF2E86DE, 0xFF27AE60));
        int[][] flowers = {{0xFFFF6FB5, 0xFFFFFFFF}, {0xFFFFD23F, 0xFFFF8A00}, {0xFF9B6BFF, 0xFF7CF7FF}};
        String[] flowerN = {"Blumenkranz Rosa", "Sonnenblumenkranz", "Blumenkranz Lila"};
        for (int i = 0; i < 3; i++) { int[] f = flowers[i]; hat("flowers" + i, flowerN[i], E, (g, a) -> flowerCrown(g, a, f[0], f[1])); }
        hat("antlers", "Hirschgeweih", E, (g, a) -> antlers(g, 0xFFB08A5A, false));
        hat("reindeer", "Rudolph-Geweih", R, (g, a) -> antlers(g, 0xFF8A5A30, true));
        int[] bows = {0xFFFF4FA3, 0xFFE3000B, 0xFF4FC3FF};
        String[] bowN = {"Haarschleife Pink", "Haarschleife Rot", "Haarschleife Blau"};
        for (int i = 0; i < 3; i++) { int c = bows[i]; hat("bow" + i, bowN[i], C, (g, a) -> bow(g, a, c)); }
        int[] berets = {0xFF1B1B22, 0xFFB0121B, 0xFF2D6A4F};
        String[] beretN = {"Baskenmütze Schwarz", "Baskenmütze Rot", "Baskenmütze Grün"};
        for (int i = 0; i < 3; i++) { int c = berets[i]; hat("beret" + i, beretN[i], C, (g, a) -> beret(g, c)); }
        hat("bucket0", "Fischerhut Beige", C, (g, a) -> bucketHat(g, 0xFFD8C8A0));
        hat("bucket1", "Fischerhut Tarn", C, (g, a) -> bucketHat(g, 0xFF5A6B3A));
        hat("pirate", "Piratenhut", E, CosPack::pirateHat);
        hat("police", "Polizeimütze", R, CosPack::policeCap);
        hat("knight", "Ritterhelm", E, (g, a) -> knight(g, 0xFFB8C0CA, 0xFFE3000B));
        hat("knight_gold", "Goldener Ritterhelm", L, (g, a) -> knight(g, 0xFFE8B92C, 0xFF3A7AFF));
        int[] wiz = {0xFF2A1A6A, 0xFF0F5A3A, 0xFF7A0F2A};
        String[] wizN = {"Zauberhut Nachtblau", "Zauberhut Smaragd", "Zauberhut Weinrot"};
        for (int i = 0; i < 3; i++) { int c = wiz[i]; hat("wizard" + i, wizN[i], E, (g, a) -> wizard(g, a, c)); }
        hat("tiara", "Tiara", E, (g, a) -> tiara(g, a, 0xFFE0E6F0, 0xFF7CF7FF));
        hat("tiara_rose", "Rosen-Tiara", E, (g, a) -> tiara(g, a, 0xFFE8B92C, 0xFFFF4FA3));
        hat("bear_ears", "Bärenohren", C, (g, a) -> roundEars(g, 0xFF7A4A28, 0xFFE0B48A, 2.0f));
        hat("panda_ears", "Pandaohren", C, (g, a) -> roundEars(g, 0xFF1B1B22, 0xFF3A3A44, 2.0f));
        hat("mouse_ears", "Mäuseohren", R, (g, a) -> roundEars(g, 0xFF1B1B22, 0xFFFF9AC0, 2.8f));
        hat("fox_ears", "Fuchsohren-Reif", R, (g, a) -> pointEars(g, 0xFFE8742A, 0xFFFFFFFF));
        hat("wolf_ears", "Wolfsohren", R, (g, a) -> pointEars(g, 0xFF6A7080, 0xFFD0D4DC));
        hat("unicorn", "Einhorn-Horn", L, CosPack::unicorn);
        hat("alien", "Alien-Fühler", R, (g, a) -> antennae(g, a, 0xFF39D353));
        hat("bee", "Bienen-Fühler", C, (g, a) -> antennae(g, a, 0xFF1B1B22));
        int[] halos = {0xFFFF5A5A, 0xFF5AF0FF, 0xFFB15CFF};
        String[] haloN = {"Roter Heiligenschein", "Eisiger Heiligenschein", "Violetter Heiligenschein"};
        for (int i = 0; i < 3; i++) { int c = halos[i]; hat("halo" + i, haloN[i], E, (g, a) -> halo(g, a, c)); }
        hat("orbit_gold", "Goldener Orbit", L, (g, a) -> orbitCrown(g, a, 0xFFFFD23F));
        hat("orbit_ice", "Eis-Orbit", L, (g, a) -> orbitCrown(g, a, 0xFF8AE8FF));
        hat("mushroom_red", "Fliegenpilz", R, (g, a) -> mushroom(g, 0xFFD7262E, true));
        hat("mushroom_brown", "Steinpilz", R, (g, a) -> mushroom(g, 0xFF8A5A30, false));
        hat("chick", "Küken auf dem Kopf", E, CosPack::chick);
        hat("diving", "Taucherhelm", E, CosPack::divingHelmet);
        int[] brick = {0xFFE3000B, 0xFF006CB7, 0xFFFFCD03};
        String[] brickN = {"Lego-Stein-Hut Rot", "Lego-Stein-Hut Blau", "Lego-Stein-Hut Gelb"};
        for (int i = 0; i < 3; i++) { int c = brick[i]; hat("brick" + i, brickN[i], R, (g, a) -> brickHat(g, c)); }
    }

    static void beanie(G g, A a, int c) {
        g.color(c);
        lathe(g, "hd_knit", new float[]{4.5f, 4.55f, 4.4f, 3.7f, 2.4f, 0}, new float[]{7.0f, 8.2f, 9.6f, 10.9f, 11.7f, 12.0f}, 20);
        g.color(dark(c, 0.15f));
        lathe(g, "hd_knit", new float[]{4.45f, 4.75f, 4.75f, 4.45f}, new float[]{6.0f, 6.2f, 7.8f, 8.0f}, 20);
        g.color(0xFFFFFFFF);
        ball(g, "hd_fluff", 0, 12.6f + sin(a.time * 3) * 0.08f, 0, 1.25f, 10);
    }

    static void cap(G g, int c, boolean logo) {
        g.color(c);
        lathe(g, "hd_cotton", new float[]{4.45f, 4.5f, 4.2f, 3.2f, 1.6f, 0}, new float[]{6.4f, 7.6f, 8.9f, 9.9f, 10.4f, 10.5f}, 20);
        g.push();
        g.translate(0, 6.5f, 3.6f);
        g.rotX(-8);
        g.color(dark(c, 0.12f));
        extrude(g, "hd_cotton", new float[][]{{-3.6f, 0}, {3.6f, 0}, {3.0f, 3.2f}, {1.5f, 4.0f}, {-1.5f, 4.0f}, {-3.0f, 3.2f}}, -0.15f, 0.15f);
        g.pop();
        g.color(light(c, 0.3f));
        ball(g, "hd_cotton", 0, 10.45f, 0, 0.45f, 6);
        if (logo) { g.color(0xFFE3000B); cbox(g, "hd_brick", 0, 8.6f, 4.15f, 1.6f, 1.0f, 0.3f, 0.1f); }
    }

    static void partyHat(G g, A a, int c) {
        g.push();
        g.translate(1.2f, 8, 0);
        g.rotZ(-12);
        g.color(c);
        lathe(g, "hd_party", new float[]{2.6f, 2.0f, 1.2f, 0.4f, 0}, new float[]{0, 2.2f, 4.6f, 6.8f, 7.6f}, 18);
        g.color(0xFFFFFFFF);
        ball(g, "hd_fluff", 0, 7.9f, 0, 0.9f, 8);
        g.color(light(c, 0.5f));
        g.torus("hd_fluff", 2.6f, 0.35f, 18, 6, 0, 0, 1, 1);
        g.pop();
    }

    static void chefHat(G g, A a) {
        g.color(0xFFF8F8F8);
        g.cylinder("hd_cotton", 4.3f, 4.6f, 7.0f, 10.5f, 20, false, 0, 0, 1, 1);
        for (int i = 0; i < 6; i++) {
            double an = Math.PI * 2 * i / 6;
            ball(g, "hd_cotton", cos(an) * 2.6f, 11.6f, sin(an) * 2.6f, 2.6f, 10);
        }
        ball(g, "hd_cotton", 0, 12.4f, 0, 2.8f, 10);
    }

    static void cowboy(G g, int c) {
        g.color(c);
        lathe(g, "hd_leather", new float[]{7.6f, 7.4f, 5.6f, 4.6f, 4.6f, 4.6f}, new float[]{8.6f, 8.0f, 7.3f, 7.4f, 7.5f, 7.6f}, 24);
        lathe(g, "hd_leather", new float[]{4.5f, 4.4f, 4.0f, 2.2f, 0}, new float[]{7.4f, 10.5f, 11.4f, 11.0f, 10.4f}, 20);
        g.color(dark(c, 0.55f));
        g.cylinder("hd_hatband", 4.52f, 4.47f, 7.6f, 8.4f, 20, false, 0, 0, 1, 1);
    }

    static void topHat(G g, int c, int band) {
        g.color(c);
        lathe(g, "hd_felt", new float[]{6.2f, 6.0f, 4.4f, 4.4f}, new float[]{8.3f, 7.8f, 7.4f, 7.6f}, 24);
        g.cylinder("hd_felt", 4.1f, 4.4f, 7.6f, 14.2f, 22, false, 0, 0, 1, 1);
        g.ring("hd_felt", 0, 4.4f, 14.2f, 22, 0, 0, 1, 1);
        g.color(band);
        g.cylinder("hd_satin", 4.48f, 4.44f, 8.0f, 9.1f, 22, false, 0, 0, 1, 1);
    }

    static void fez(G g, A a, int c) {
        g.color(c);
        g.cylinder("hd_felt", 3.5f, 2.7f, 7.6f, 11.4f, 20, false, 0, 0, 1, 1);
        g.ring("hd_felt", 0, 2.7f, 11.4f, 20, 0, 0, 1, 1);
        g.color(0xFF1B1B22);
        float sw = sin(a.time * 2) * 8;
        g.push();
        g.translate(0, 11.4f, 0);
        g.rotZ(70 + sw);
        Geo.tube(g, "hd_cotton", new float[]{0, 0, 0}, new float[]{0, 2.6f, 0}, 0.12f, 0.12f, 5);
        g.translate(0, 2.6f, 0);
        lathe(g, "hd_cotton", new float[]{0, 0.5f, 0.2f}, new float[]{-1.3f, -0.2f, 0.2f}, 8);
        g.pop();
    }

    static void sombrero(G g, A a) {
        g.color(0xFFE8C77A);
        lathe(g, "hd_wood", new float[]{10.5f, 10.2f, 7.0f, 4.6f}, new float[]{8.8f, 8.2f, 7.6f, 7.6f}, 28);
        lathe(g, "hd_wood", new float[]{4.5f, 4.2f, 3.4f, 1.8f, 0}, new float[]{7.5f, 11.0f, 12.6f, 13.4f, 13.5f}, 22);
        g.color(0xFFE3000B);
        g.torus("hd_cotton", 10.3f, 0.35f, 28, 6, 0, 0, 1, 1);
        g.color(0xFF2E9E4A);
        g.cylinder("hd_cotton", 4.46f, 4.3f, 8.0f, 9.0f, 22, false, 0, 0, 1, 1);
    }

    static void gradCap(G g, A a, int c, int tassel) {
        g.color(c);
        lathe(g, "hd_felt", new float[]{4.4f, 4.4f, 4.2f}, new float[]{7.0f, 9.4f, 9.6f}, 20);
        g.push();
        g.translate(0, 9.7f, 0);
        g.rotY(45);
        cbox(g, "hd_felt", 0, 0, 0, 10.5f, 0.4f, 10.5f, 0.08f);
        g.pop();
        g.color(tassel);
        ball(g, "hd_satin", 0, 10.0f, 0, 0.45f, 6);
        float sw = sin(a.time * 2.2f) * 0.5f;
        Geo.chain(g, "hd_satin", new float[][]{{0, 10.0f, 0}, {3.4f, 10.0f, -0.4f}, {4.2f + sw * 0.2f, 8.6f, -0.6f}, {4.3f + sw * 0.3f, 7.0f, -0.6f}}, new float[]{0.12f, 0.12f, 0.12f, 0.12f}, 5);
        lathe(g, "hd_satin", new float[]{0.2f, 0.55f, 0.5f, 0}, new float[]{7.2f, 6.6f, 5.4f, 5.2f}, 8);
    }

    static void hardHat(G g, int c) {
        g.color(c);
        lathe(g, "hd_plastic", new float[]{5.6f, 5.4f, 4.6f, 4.6f, 4.3f, 3.2f, 1.4f, 0}, new float[]{7.2f, 6.8f, 6.9f, 8.6f, 10.0f, 11.1f, 11.7f, 11.8f}, 22);
        g.color(dark(c, 0.1f));
        cbox(g, "hd_plastic", 0, 10.0f, 0, 1.1f, 3.6f, 9.4f, 0.4f);
        g.color(0xFF1B1B22);
        g.cylinder("hd_rubber", 4.62f, 4.62f, 7.2f, 7.6f, 22, false, 0, 0, 1, 1);
    }

    static void viking(G g, int metal, int horn) {
        g.color(metal);
        lathe(g, "hd_steel", new float[]{4.7f, 4.6f, 4.2f, 3.0f, 1.2f, 0}, new float[]{6.4f, 8.6f, 10.2f, 11.4f, 12.0f, 12.1f}, 22);
        g.color(dark(metal, 0.25f));
        g.torus("hd_steel", 4.65f, 0.35f, 22, 6, 0, 0, 1, 1);
        cbox(g, "hd_steel", 0, 7.5f, 4.55f, 0.8f, 3.8f, 0.5f, 0.15f);
        g.color(horn);
        for (int s = -1; s <= 1; s += 2)
            Geo.chain(g, "hd_horn", new float[][]{{s * 4.2f, 9.0f, 0}, {s * 6.0f, 9.6f, 0}, {s * 7.0f, 11.4f, 0.3f}, {s * 7.2f, 13.6f, 0.6f}}, new float[]{1.1f, 0.85f, 0.5f, 0.08f}, 8);
    }

    static void propeller(G g, A a, int c1, int c2) {
        for (int i = 0; i < 4; i++) {
            g.color(i % 2 == 0 ? c1 : c2);
            g.push();
            g.rotY(i * 90);
            lathe(g, "hd_cotton", new float[]{4.45f, 4.4f, 3.8f, 2.4f, 0}, new float[]{6.4f, 8.2f, 9.9f, 11.0f, 11.3f}, 6, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFCD03);
        g.cylinder("hd_plastic", 0.25f, 0.25f, 11.2f, 12.6f, 6, true, 0, 0, 1, 1);
        g.push();
        g.translate(0, 12.6f, 0);
        g.rotY(a.time * 720);
        for (int i = 0; i < 2; i++) {
            g.color(i == 0 ? 0xFF2E86DE : 0xFFE3000B);
            g.push();
            g.rotY(i * 180);
            g.rotX(14);
            cbox(g, "hd_plastic", 2.2f, 0, 0, 4.0f, 0.15f, 1.0f, 0.06f);
            g.pop();
        }
        g.pop();
    }

    static void flowerCrown(G g, A a, int petal, int center) {
        g.color(0xFF3E8E41);
        g.push();
        g.translate(0, 8.0f, 0);
        g.torus("hd_vine", 4.3f, 0.32f, 22, 6, 0, 0, 1, 1);
        for (int i = 0; i < 9; i++) {
            double an = Math.PI * 2 * i / 9;
            float x = cos(an) * 4.4f, z = sin(an) * 4.4f;
            g.push();
            g.translate(x, 0.3f, z);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.rotX(-70);
            int col = i % 3 == 0 ? center : petal;
            g.color(col);
            for (int k = 0; k < 5; k++) {
                g.push();
                g.rotY(k * 72);
                Geo.ellipsoid(g, "hd_petal", 0, 0, 0.75f, 0.45f, 0.18f, 0.8f, 6, 3);
                g.pop();
            }
            g.color(i % 3 == 0 ? petal : center);
            ball(g, "hd_softglow", 0, 0.15f, 0, 0.35f, 6);
            g.pop();
            g.color(0xFF4FAE52);
            g.push();
            g.translate(cos(an + 0.35) * 4.5f, 0, sin(an + 0.35) * 4.5f);
            g.rotY((float) -Math.toDegrees(an));
            Geo.ellipsoid(g, "hd_leaf", 0, 0, 0, 0.5f, 0.1f, 0.25f, 5, 3);
            g.pop();
        }
        g.pop();
    }

    static void antlers(G g, int c, boolean nose) {
        g.color(c);
        for (int s = -1; s <= 1; s += 2) {
            Geo.chain(g, "hd_horn", new float[][]{{s * 2.2f, 7.8f, 0}, {s * 2.8f, 9.6f, -0.2f}, {s * 3.6f, 11.6f, -0.4f}, {s * 4.0f, 13.6f, -0.2f}}, new float[]{0.45f, 0.4f, 0.32f, 0.18f}, 6);
            Geo.chain(g, "hd_horn", new float[][]{{s * 3.0f, 10.0f, -0.2f}, {s * 4.6f, 11.0f, 0}, {s * 5.2f, 12.4f, 0.2f}}, new float[]{0.3f, 0.24f, 0.12f}, 6);
            Geo.chain(g, "hd_horn", new float[][]{{s * 3.6f, 11.8f, -0.4f}, {s * 2.6f, 13.0f, -0.4f}, {s * 2.4f, 14.0f, -0.3f}}, new float[]{0.26f, 0.2f, 0.1f}, 6);
        }
        if (nose) { g.color(0xFFFF2A2A).glow(true); ball(g, "hd_gloss", 0, 3.0f, 4.4f, 0.75f, 10); g.glow(false); }
    }

    static void bow(G g, A a, int c) {
        g.push();
        g.translate(2.2f, 8.2f, 0.5f);
        g.rotZ(-20);
        g.color(c);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.scale(s, 1, 1);
            lathe(g, "hd_satin", new float[]{0.3f, 1.4f, 1.3f, 0}, new float[]{0, 0.6f, 2.4f, 3.0f}, 10);
            g.pop();
            g.push();
            g.rotZ(s * 90);
            lathe(g, "hd_satin", new float[]{0.3f, 1.2f, 1.1f, 0}, new float[]{0, 0.5f, 2.2f, 2.6f}, 10);
            g.pop();
        }
        g.color(dark(c, 0.15f));
        ball(g, "hd_satin", 0, 0, 0, 0.7f, 8);
        g.pop();
    }

    static void beret(G g, int c) {
        g.push();
        g.translate(0.6f, 0, 0);
        g.rotZ(-8);
        g.color(c);
        lathe(g, "hd_felt", new float[]{4.4f, 4.7f, 5.2f, 4.6f, 2.6f, 0}, new float[]{7.2f, 7.6f, 8.6f, 9.6f, 10.1f, 10.2f}, 22);
        g.color(dark(c, 0.2f));
        g.cylinder("hd_felt", 0.2f, 0.12f, 10.1f, 10.9f, 6, true, 0, 0, 1, 1);
        g.pop();
    }

    static void bucketHat(G g, int c) {
        g.color(c);
        lathe(g, "hd_cotton", new float[]{6.4f, 6.2f, 4.6f, 4.4f, 4.0f, 0}, new float[]{6.0f, 6.4f, 7.6f, 10.0f, 10.6f, 10.7f}, 22);
        g.color(dark(c, 0.2f));
        g.cylinder("hd_stitch", 4.52f, 4.42f, 7.8f, 8.4f, 22, false, 0, 0, 1, 1);
    }

    static void pirateHat(G g, A a) {
        g.color(0xFF1B1B22);
        lathe(g, "hd_felt", new float[]{4.5f, 4.4f, 3.8f, 0}, new float[]{7.2f, 9.6f, 10.6f, 10.8f}, 20);
        for (int i = 0; i < 3; i++) {
            g.push();
            g.rotY(i * 120);
            g.translate(0, 8.0f, 3.2f);
            g.rotX(-62);
            extrude(g, "hd_felt", new float[][]{{-5.0f, 0}, {5.0f, 0}, {3.2f, 3.6f}, {-3.2f, 3.6f}}, -0.15f, 0.15f);
            g.pop();
        }
        g.color(0xFFF4F4F4);
        g.push();
        g.translate(0, 9.6f, 4.15f);
        ball(g, "hd_bone", 0, 0.4f, 0, 0.8f, 8);
        g.rotZ(45);
        cbox(g, "hd_bone", 0, -0.7f, 0, 0.4f, 2.4f, 0.3f, 0.1f);
        g.rotZ(-90);
        cbox(g, "hd_bone", 0, -0.7f, 0, 0.4f, 2.4f, 0.3f, 0.1f);
        g.pop();
        g.color(0xFFE8B92C);
        g.torus("hd_gold", 4.45f, 0.15f, 22, 5, 0, 0, 1, 1);
    }

    static void policeCap(G g, A a) {
        g.color(0xFF1C2A55);
        lathe(g, "hd_felt", new float[]{4.45f, 4.5f, 5.4f, 5.2f, 0}, new float[]{7.0f, 8.6f, 10.0f, 10.5f, 10.6f}, 22);
        g.color(0xFF1B1B22);
        g.push();
        g.translate(0, 7.2f, 3.6f);
        g.rotX(18);
        extrude(g, "hd_leather", new float[][]{{-3.4f, 0}, {3.4f, 0}, {2.4f, 2.4f}, {-2.4f, 2.4f}}, -0.12f, 0.12f);
        g.pop();
        g.color(0xFFE8B92C);
        g.push();
        g.translate(0, 9.0f, 4.75f);
        gem(g, "hd_gold", 0.9f, 0.4f, 0.4f, 6);
        g.pop();
        g.color(0xFF1B1B22);
        g.cylinder("hd_hatband", 4.55f, 4.55f, 7.4f, 8.2f, 22, false, 0, 0, 1, 1);
    }

    static void knight(G g, int metal, int plume) {
        g.color(metal);
        bbox(g, "hd_steel", -4.6f, -0.4f, -4.6f, 4.6f, 8.8f, 4.6f, 0.6f);
        g.color(dark(metal, 0.7f));
        for (int i = 0; i < 4; i++) cbox(g, "hd_steel", 0, 4.6f - i * 0.9f, 4.62f, 6.2f, 0.25f, 0.1f, 0.05f);
        g.color(dark(metal, 0.2f));
        cbox(g, "hd_steel", 0, 5.4f, 4.7f, 0.6f, 6.4f, 0.4f, 0.1f);
        g.color(plume);
        Geo.chain(g, "hd_fluff", new float[][]{{0, 8.8f, 1}, {0, 11.0f, 0}, {0, 12.0f, -2.2f}, {0, 11.0f, -4.6f}}, new float[]{0.6f, 1.0f, 0.9f, 0.3f}, 8);
    }

    static void wizard(G g, A a, int c) {
        g.color(c);
        lathe(g, "hd_wizard", new float[]{7.0f, 6.8f, 4.6f}, new float[]{8.0f, 7.6f, 7.4f}, 24);
        g.push();
        g.translate(0, 7.4f, 0);
        float t = a.time;
        float[][] pts = new float[7][];
        float[] rad = new float[7];
        for (int i = 0; i < 7; i++) {
            float f = i / 6f;
            pts[i] = new float[]{f * f * 2.6f + sin(t * 1.4 + f * 2) * 0.2f * f, f * 9.5f, -f * f * 1.2f};
            rad[i] = 4.5f * (1 - f) + 0.15f;
        }
        Geo.chain(g, "hd_wizard", pts, rad, 18);
        g.glow(true);
        for (int i = 0; i < 6; i++) {
            double an = i * 1.7 + t * 0.3;
            float y = 1.2f + i * 1.15f, r = 4.6f * (1 - y / 9.5f) + 0.1f;
            g.color(0xFFFFE27A);
            g.push();
            g.translate(cos(an) * r + (y / 9.5f) * (y / 9.5f) * 2.6f, y, sin(an) * r);
            gem(g, "white", 0.25f, 0.35f, 0.35f, 4);
            g.pop();
        }
        g.glow(false);
        g.pop();
    }

    static void tiara(G g, A a, int metal, int gem) {
        g.color(metal);
        g.push();
        g.translate(0, 7.6f, 0);
        for (int i = -4; i <= 4; i++) {
            double an = Math.PI / 2 + i * 0.22;
            g.push();
            g.translate(cos(an) * 4.4f, 0, sin(an) * 4.4f);
            g.rotY((float) -Math.toDegrees(an) + 90);
            float h = 1.6f - Math.abs(i) * 0.14f;
            extrude(g, "hd_silver", new float[][]{{-0.5f, 0}, {0.5f, 0}, {0.12f, h}, {-0.12f, h}}, -0.1f, 0.1f);
            g.pop();
        }
        g.torus("hd_silver", 4.4f, 0.16f, 24, 5, 0, 0, 1, 1);
        g.glow(true).color(gem);
        g.push();
        g.translate(0, 2.0f, 4.45f);
        gem(g, "hd_gem", 0.6f, 0.7f, 0.9f, 6);
        g.pop();
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.5f, 1.3f, 4.2f);
            gem(g, "hd_gem", 0.35f, 0.4f, 0.5f, 6);
            g.pop();
        }
        g.glow(false);
        g.pop();
    }

    static void roundEars(G g, int outer, int inner, float r) {
        g.color(0xFF1B1B22);
        g.torus("hd_plastic", 4.4f, 0.22f, 22, 5, 0, 0, 1, 1);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 3.2f, 8.4f + r * 0.6f, 0);
            g.rotZ(-s * 15);
            g.color(outer);
            Geo.ellipsoid(g, "hd_fur", 0, 0, 0, r, r, 0.7f, 12, 6);
            g.color(inner);
            Geo.ellipsoid(g, "hd_fur", 0, -0.1f, 0.35f, r * 0.62f, r * 0.62f, 0.45f, 10, 5);
            g.pop();
        }
    }

    static void pointEars(G g, int outer, int tip) {
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.8f, 7.8f, 0.2f);
            g.rotZ(-s * 18);
            g.color(outer);
            lathe(g, "hd_fur", new float[]{1.7f, 1.5f, 0.9f, 0.3f, 0}, new float[]{0, 1.2f, 2.8f, 4.0f, 4.4f}, 4);
            g.color(tip);
            g.translate(0, 3.3f, 0);
            lathe(g, "hd_fur", new float[]{0.62f, 0.3f, 0}, new float[]{0, 0.8f, 1.15f}, 4);
            g.pop();
        }
    }

    static void unicorn(G g, A a) {
        g.push();
        g.translate(0, 7.2f, 2.2f);
        g.rotX(28);
        g.glow(true);
        int n = 8;
        for (int i = 0; i < n; i++) {
            float f0 = i / (float) n, f1 = (i + 1f) / n;
            g.color(Geo.hue(a.time * 0.15 + f0 * 0.6, 0.45, 1));
            lathe(g, "hd_gloss", new float[]{1.0f * (1 - f0) + 0.05f, 1.0f * (1 - f1) + 0.05f}, new float[]{f0 * 7, f1 * 7}, 10);
        }
        g.glow(false);
        g.pop();
        g.color(0xFFFFFFFF);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 3.6f, 7.6f, 0.4f);
            g.rotZ(-s * 30);
            lathe(g, "hd_fur", new float[]{1.0f, 0.8f, 0.3f, 0}, new float[]{0, 1.0f, 2.2f, 2.5f}, 6);
            g.pop();
        }
    }

    static void antennae(G g, A a, int ball) {
        for (int s = -1; s <= 1; s += 2) {
            float sw = sin(a.time * 3 + s) * 0.4f;
            g.color(0xFF1B1B22);
            Geo.chain(g, "hd_rubber", new float[][]{{s * 1.8f, 7.8f, 0}, {s * 2.4f + sw * 0.3f, 10.0f, 0.2f}, {s * 3.2f + sw, 12.2f, 0.6f}}, new float[]{0.18f, 0.15f, 0.12f}, 5);
            g.color(ball).glow(true);
            ball(g, "hd_gloss", s * 3.2f + sw, 12.6f, 0.6f, 0.7f, 10);
            g.glow(false);
        }
    }

    static void halo(G g, A a, int c) {
        g.push();
        g.translate(0, 10.6f + sin(a.time * 2) * 0.3f, 0);
        g.rotX(8);
        g.glow(true).color(c);
        g.torus("hd_softglow", 3.4f, 0.32f, 28, 6, 0, 0, 1, 1);
        g.color(Cos3Geo.alpha(0.35, c & 0xFFFFFF));
        g.torus("hd_softglow", 3.4f, 0.8f, 28, 6, 0, 0, 1, 1);
        g.glow(false);
        g.pop();
    }

    static void orbitCrown(G g, A a, int c) {
        g.glow(true);
        for (int i = 0; i < 6; i++) {
            double an = a.time * 1.4 + Math.PI * 2 * i / 6;
            float y = 10.4f + sin(a.time * 2 + i) * 0.5f;
            g.color(c);
            g.push();
            g.translate(cos(an) * 4.8f, y, sin(an) * 4.8f);
            g.rotY(a.time * 90 + i * 30);
            gem(g, "hd_gem", 0.55f, 0.8f, 0.8f, 5);
            g.pop();
        }
        g.color(Cos3Geo.alpha(0.4, c & 0xFFFFFF));
        g.push();
        g.translate(0, 10.4f, 0);
        g.torus("hd_softglow", 4.8f, 0.12f, 32, 4, 0, 0, 1, 1);
        g.pop();
        g.glow(false);
    }

    static void mushroom(G g, int cap, boolean dots) {
        g.color(0xFFF2E8D0);
        g.cylinder("hd_mush", 1.6f, 1.9f, 7.6f, 9.6f, 14, false, 0, 0, 1, 1);
        g.color(cap);
        lathe(g, "hd_mush", new float[]{0, 5.6f, 6.0f, 5.4f, 3.8f, 1.6f, 0}, new float[]{9.2f, 9.3f, 10.0f, 11.2f, 12.6f, 13.4f, 13.5f}, 24);
        if (dots) {
            g.color(0xFFFFFFFF);
            for (int i = 0; i < 9; i++) {
                double an = i * 2.4;
                float rr = i == 0 ? 0 : 2.4f + (i % 3) * 1.0f;
                float y = rr < 0.5f ? 13.5f : (rr < 3 ? 12.9f : rr < 4 ? 12.2f : 11.3f);
                ball(g, "hd_mush", cos(an) * rr, y, sin(an) * rr, 0.55f, 6);
            }
        }
    }

    static void chick(G g, A a) {
        float hop = Math.abs(sin(a.time * 3)) * 0.4f;
        g.push();
        g.translate(0, 8.2f + hop, 0);
        g.rotY(sin(a.time * 0.7f) * 25);
        g.color(0xFFFFE04A);
        ball(g, "hd_fluff", 0, 1.6f, 0, 1.9f, 12);
        ball(g, "hd_fluff", 0, 3.8f, 0.6f, 1.3f, 12);
        g.color(0xFFFF8A00);
        g.push();
        g.translate(0, 3.7f, 1.8f);
        g.rotX(90);
        lathe(g, "hd_plastic", new float[]{0.4f, 0}, new float[]{0, 0.7f}, 6);
        g.pop();
        g.color(0xFF1B1B22);
        for (int s = -1; s <= 1; s += 2) ball(g, "hd_gloss", s * 0.55f, 4.1f, 1.7f, 0.2f, 5);
        g.color(0xFFF2C230);
        for (int s = -1; s <= 1; s += 2) Geo.ellipsoid(g, "hd_fluff", s * 1.8f, 1.8f, -0.2f, 0.4f, 1.0f, 1.2f, 6, 4);
        g.pop();
    }

    static void divingHelmet(G g, A a) {
        g.color(0xFFC8873A);
        lathe(g, "hd_gold", new float[]{5.4f, 5.6f, 5.6f, 5.2f, 4.0f, 2.0f, 0}, new float[]{-0.6f, 0.4f, 6.0f, 8.4f, 10.0f, 10.9f, 11.0f}, 24);
        for (int i = 0; i < 3; i++) {
            double an = Math.PI / 2 + (i - 1) * Math.PI / 2;
            g.push();
            g.translate(cos(an) * 5.3f, 4.6f, sin(an) * 5.3f);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.rotX(90);
            g.color(0xFFB8762A);
            g.torus("hd_gold", 1.9f, 0.35f, 18, 5, 0, 0, 1, 1);
            g.color(0x88A8E8FF);
            g.ring("hd_glass", 0, 1.9f, 0, 18, 0, 0, 1, 1);
            g.pop();
        }
    }

    static void brickHat(G g, int c) {
        g.color(c);
        bbox(g, "hd_brick", -4.6f, 7.4f, -4.6f, 4.6f, 11.4f, 4.6f, 0.35f);
        for (int ix = 0; ix < 2; ix++)
            for (int iz = 0; iz < 2; iz++) {
                g.push();
                g.translate(-2.3f + ix * 4.6f, 11.4f, -2.3f + iz * 4.6f);
                lathe(g, "hd_brick", new float[]{1.45f, 1.45f, 1.35f, 0}, new float[]{-0.02f, 1.0f, 1.15f, 1.18f}, 16);
                g.pop();
            }
    }

    // ================================================================== FACE
    private static void faces() {
        int[] rnd = {0xFF1B1B22, 0xFFE8B92C, 0xFFE3000B};
        String[] rndN = {"Runde Brille Schwarz", "Runde Brille Gold", "Runde Brille Rot"};
        for (int i = 0; i < 3; i++) { int c = rnd[i]; face("round" + i, rndN[i], C, (g, a) -> roundGlasses(g, c, 0x55C8E8FF)); }
        int[] stars = {0xFFFFD23F, 0xFFFF4FA3, 0xFF4FC3FF};
        String[] starN = {"Sternenbrille Gold", "Sternenbrille Pink", "Sternenbrille Blau"};
        for (int i = 0; i < 3; i++) { int c = stars[i]; face("star" + i, starN[i], R, (g, a) -> shapeGlasses(g, a, c, true)); }
        int[] hearts = {0xFFFF2A5A, 0xFFFF8AC0, 0xFFB15CFF};
        String[] heartN = {"Herzbrille Rot", "Herzbrille Rosa", "Herzbrille Lila"};
        for (int i = 0; i < 3; i++) { int c = hearts[i]; face("heart" + i, heartN[i], R, (g, a) -> shapeGlasses(g, a, c, false)); }
        face("monocle_gold", "Monokel Gold", E, (g, a) -> monocle(g, 0xFFE8B92C));
        face("monocle_silver", "Monokel Silber", E, (g, a) -> monocle(g, 0xFFD0D6DE));
        face("mustache_brown", "Schnurrbart Braun", C, (g, a) -> mustache(g, 0xFF5A3A1E, false));
        face("mustache_black", "Schnurrbart Schwarz", C, (g, a) -> mustache(g, 0xFF1B1B22, false));
        face("mustache_curl", "Zwirbelbart", R, (g, a) -> mustache(g, 0xFF3A2414, true));
        face("beard_brown", "Vollbart Braun", R, (g, a) -> beard(g, 0xFF5A3A1E));
        face("beard_white", "Weihnachtsbart", R, (g, a) -> beard(g, 0xFFF4F4F4));
        face("clown", "Rote Clownsnase", C, (g, a) -> { g.color(0xFFE3000B); ball(g, "hd_gloss", 0, 3.0f, 4.7f, 0.95f, 12); });
        face("eyepatch", "Augenklappe", R, CosPack::eyePatch);
        face("bandit_black", "Banditenmaske", R, (g, a) -> bandit(g, 0xFF1B1B22));
        face("bandit_red", "Superhelden-Maske", R, (g, a) -> bandit(g, 0xFFC0141C));
        face("vr_black", "VR-Brille", E, (g, a) -> vr(g, a, 0xFF22242C, 0xFF5AF0FF));
        face("vr_white", "VR-Brille Weiß", E, (g, a) -> vr(g, a, 0xFFF2F2F2, 0xFFFF5AF0));
        int[] ski = {0xFFFF8A00, 0xFF39D353, 0xFFB15CFF};
        String[] skiN = {"Skibrille Orange", "Skibrille Grün", "Skibrille Lila"};
        for (int i = 0; i < 3; i++) { int c = ski[i]; face("ski" + i, skiN[i], R, (g, a) -> skiGoggles(g, c)); }
        face("scuba", "Taucherbrille", R, CosPack::scuba);
        face("ninja_black", "Ninja-Maske Schwarz", R, (g, a) -> ninjaMask(g, 0xFF1B1B22));
        face("ninja_red", "Ninja-Maske Rot", R, (g, a) -> ninjaMask(g, 0xFF9A1018));
        face("whiskers", "Katzen-Schnurrhaare", C, CosPack::whiskers);
        face("3d", "3D-Brille", C, CosPack::glasses3d);
        face("nerd", "Nerd-Brille", C, (g, a) -> squareGlasses(g, 0xFF1B1B22, 0.55f));
        int[] shut = {0xFFFF4FA3, 0xFF39D353, 0xFFFFFFFF};
        String[] shutN = {"Shutter Shades Pink", "Shutter Shades Grün", "Shutter Shades Weiß"};
        for (int i = 0; i < 3; i++) { int c = shut[i]; face("shutter" + i, shutN[i], R, (g, a) -> shutterShades(g, c)); }
        face("laser_red", "Laseraugen Rot", L, (g, a) -> laserEyes(g, a, 0xFFFF2A2A));
        face("laser_blue", "Laseraugen Blau", L, (g, a) -> laserEyes(g, a, 0xFF3AA8FF));
        face("headset_black", "Gamer-Headset", E, (g, a) -> headset(g, a, 0xFF22242C, 0xFF39D353));
        face("headset_pink", "Gamer-Headset Pink", E, (g, a) -> headset(g, a, 0xFFFF8AC0, 0xFFFFFFFF));
        face("rose", "Rose im Mund", R, CosPack::rose);
        face("lolly", "Lolli", C, CosPack::lolly);
    }

    private static void temples(G g, int c) {
        g.color(c);
        for (int s = -1; s <= 1; s += 2) cbox(g, "hd_plastic", s * 4.35f, 4.3f, 1.4f, 0.22f, 0.35f, 6.0f, 0.05f);
    }

    static void roundGlasses(G g, int frame, int lens) {
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.0f, 4.2f, 4.4f);
            g.rotX(90);
            g.color(frame);
            g.torus("hd_plastic", 1.45f, 0.18f, 18, 5, 0, 0, 1, 1);
            g.color(lens);
            g.ring("hd_glass", 0, 1.4f, 0, 18, 0, 0, 1, 1);
            g.pop();
        }
        g.color(frame);
        Geo.chain(g, "hd_plastic", new float[][]{{-0.6f, 4.4f, 4.45f}, {0, 4.6f, 4.5f}, {0.6f, 4.4f, 4.45f}}, new float[]{0.14f, 0.14f, 0.14f}, 5);
        temples(g, frame);
    }

    static void squareGlasses(G g, int frame, float thick) {
        g.color(frame);
        for (int s = -1; s <= 1; s += 2) {
            float cx = s * 2.0f;
            cbox(g, "hd_plastic", cx, 5.4f, 4.45f, 3.4f, thick, 0.4f, 0.1f);
            cbox(g, "hd_plastic", cx, 3.0f, 4.45f, 3.4f, thick, 0.4f, 0.1f);
            cbox(g, "hd_plastic", cx - 1.5f, 4.2f, 4.45f, thick, 2.8f, 0.4f, 0.1f);
            cbox(g, "hd_plastic", cx + 1.5f, 4.2f, 4.45f, thick, 2.8f, 0.4f, 0.1f);
            g.color(0x55C8E8FF);
            cbox(g, "hd_glass", cx, 4.2f, 4.42f, 2.6f, 2.0f, 0.08f, 0.02f);
            g.color(frame);
        }
        cbox(g, "hd_plastic", 0, 4.6f, 4.45f, 1.0f, 0.35f, 0.3f, 0.08f);
        temples(g, frame);
    }

    static void shapeGlasses(G g, A a, int c, boolean star) {
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 2.1f, 4.3f, 4.3f);
            float pulse = 1 + 0.04f * sin(a.time * 4);
            g.scale(pulse);
            g.color(c);
            float[][] poly = star ? starPoly(1.95f, 0.85f, 5) : heartPoly(1.7f);
            extrude(g, "hd_plastic", poly, 0, 0.45f);
            g.color(0x99FFFFFF);
            float[][] in = star ? starPoly(1.35f, 0.6f, 5) : heartPoly(1.2f);
            extrude(g, "hd_glass", in, 0.46f, 0.5f);
            g.pop();
        }
        g.color(c);
        cbox(g, "hd_plastic", 0, 4.5f, 4.5f, 0.9f, 0.3f, 0.25f, 0.08f);
        temples(g, c);
    }

    static float[][] starPoly(float ro, float ri, int n) {
        float[][] p = new float[n * 2][];
        for (int i = 0; i < n * 2; i++) {
            double an = Math.PI / 2 + Math.PI * i / n;
            float r = i % 2 == 0 ? ro : ri;
            p[i] = new float[]{cos(an) * r, sin(an) * r};
        }
        return p;
    }

    static float[][] heartPoly(float s) {
        int n = 24;
        float[][] p = new float[n][];
        for (int i = 0; i < n; i++) {
            double t = Math.PI * 2 * i / n;
            float x = (float) (16 * Math.pow(Math.sin(t), 3)) / 16f, y = (float) (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 16f;
            p[n - 1 - i] = new float[]{x * s, y * s};
        }
        return p;
    }

    static void monocle(G g, int c) {
        g.push();
        g.translate(2.0f, 4.3f, 4.45f);
        g.rotX(90);
        g.color(c);
        g.torus("hd_gold", 1.5f, 0.2f, 20, 5, 0, 0, 1, 1);
        g.color(0x44E8F4FF);
        g.ring("hd_glass", 0, 1.45f, 0, 20, 0, 0, 1, 1);
        g.pop();
        g.color(c);
        Geo.chain(g, "hd_gold", new float[][]{{3.4f, 3.6f, 4.45f}, {3.9f, 2.0f, 4.2f}, {4.2f, 0.4f, 3.4f}, {4.3f, -0.6f, 2.0f}}, new float[]{0.07f, 0.07f, 0.07f, 0.07f}, 4);
    }

    static void mustache(G g, int c, boolean curl) {
        g.color(c);
        for (int s = -1; s <= 1; s += 2) {
            float[][] pts = curl
                    ? new float[][]{{0, 2.4f, 4.4f}, {s * 1.2f, 2.3f, 4.45f}, {s * 2.2f, 2.0f, 4.4f}, {s * 2.9f, 2.4f, 4.35f}, {s * 2.8f, 2.9f, 4.35f}}
                    : new float[][]{{0, 2.4f, 4.4f}, {s * 1.2f, 2.25f, 4.45f}, {s * 2.3f, 1.8f, 4.4f}, {s * 2.8f, 1.4f, 4.3f}};
            float[] rad = curl ? new float[]{0.5f, 0.45f, 0.32f, 0.2f, 0.1f} : new float[]{0.55f, 0.5f, 0.35f, 0.12f};
            Geo.chain(g, "hd_mustache", pts, rad, 6);
        }
    }

    static void beard(G g, int c) {
        g.color(c);
        for (int i = 0; i < 9; i++) {
            float x = -3.6f + i * 0.9f;
            float len = 3.2f - Math.abs(x) * 0.35f;
            Geo.ellipsoid(g, "hd_fur", x, 1.2f - len * 0.35f, 4.2f - Math.abs(x) * 0.12f, 0.75f, len * 0.6f, 0.55f, 8, 5);
        }
        Geo.ellipsoid(g, "hd_fur", 0, 2.3f, 4.45f, 2.2f, 0.45f, 0.4f, 8, 4);
    }

    static void eyePatch(G g, A a) {
        g.color(0xFF1B1B22);
        g.push();
        g.translate(2.0f, 4.3f, 4.35f);
        Geo.ellipsoid(g, "hd_leather", 0, 0, 0, 1.4f, 1.15f, 0.25f, 12, 4);
        g.pop();
        Geo.chain(g, "hd_leather", new float[][]{{-4.25f, 6.6f, 1.0f}, {-3.0f, 6.4f, 4.3f}, {0, 5.5f, 4.5f}, {3.4f, 4.6f, 4.4f}, {4.25f, 3.6f, 1.0f}}, new float[]{0.12f, 0.12f, 0.12f, 0.12f, 0.12f}, 4);
    }

    static void bandit(G g, int c) {
        g.color(c);
        g.cylinder("hd_satin", 4.35f, 4.35f, 3.4f, 5.3f, 24, false, 0, 0, 1, 1);
        g.color(0xFF000000);
        for (int s = -1; s <= 1; s += 2) Geo.ellipsoid(g, "hd_gloss", s * 1.9f, 4.35f, 4.36f, 0.9f, 0.55f, 0.06f, 10, 3);
        g.color(c);
        for (int s = -1; s <= 1; s += 2) Geo.chain(g, "hd_satin", new float[][]{{s * 0.6f, 4.3f, -4.4f}, {s * 1.2f, 3.4f, -5.4f}, {s * 1.6f, 2.0f, -5.8f}}, new float[]{0.35f, 0.3f, 0.15f}, 5);
    }

    static void vr(G g, A a, int body, int glow) {
        g.color(body);
        bbox(g, "hd_plastic", -3.8f, 2.6f, 4.0f, 3.8f, 6.0f, 6.4f, 0.6f);
        g.color(0xFF1B1B22);
        for (int s = -1; s <= 1; s += 2) cbox(g, "hd_rubber", s * 4.3f, 4.3f, 0.6f, 0.3f, 1.2f, 8.0f, 0.1f);
        g.color(glow).glow(true);
        float pulse = 0.6f + 0.4f * sin(a.time * 3);
        g.color(Cos3Geo.alpha(pulse, glow & 0xFFFFFF));
        cbox(g, "hd_softglow", 0, 4.3f, 6.45f, 5.6f, 0.5f, 0.08f, 0.02f);
        g.glow(false);
    }

    static void skiGoggles(G g, int lens) {
        g.color(0xFF22242C);
        g.cylinder("hd_rubber", 4.38f, 4.38f, 3.7f, 5.0f, 24, false, 0, 0, 1, 1);
        g.color(0xFFF2F2F2);
        bbox(g, "hd_plastic", -3.6f, 2.8f, 4.0f, 3.6f, 5.8f, 4.9f, 0.6f);
        g.color(lens).glow(true);
        bbox(g, "hd_lens", -3.2f, 3.1f, 4.85f, 3.2f, 5.5f, 5.05f, 0.5f);
        g.glow(false);
    }

    static void scuba(G g, A a) {
        g.color(0xFF2E86DE);
        bbox(g, "hd_rubber", -3.4f, 2.6f, 4.0f, 3.4f, 6.0f, 5.0f, 0.8f);
        g.color(0x66BFEFFF);
        bbox(g, "hd_glass", -2.9f, 3.0f, 4.95f, 2.9f, 5.6f, 5.1f, 0.6f);
        g.color(0xFF22242C);
        g.cylinder("hd_rubber", 4.38f, 4.38f, 4.0f, 4.8f, 24, false, 0, 0, 1, 1);
        g.color(0xFFFFCD03);
        Geo.chain(g, "hd_plastic", new float[][]{{3.6f, 1.6f, 4.4f}, {4.6f, 2.4f, 3.6f}, {4.8f, 6.0f, 3.0f}, {4.8f, 9.0f, 2.6f}}, new float[]{0.3f, 0.3f, 0.3f, 0.32f}, 6);
    }

    static void ninjaMask(G g, int c) {
        g.color(c);
        g.cylinder("hd_ninja", 4.4f, 4.4f, -0.2f, 3.4f, 24, false, 0, 0, 1, 1);
        g.cylinder("hd_ninja", 4.4f, 4.4f, 5.4f, 8.2f, 24, false, 0, 0, 1, 1);
        for (int s = -1; s <= 1; s += 2) Geo.chain(g, "hd_ninja", new float[][]{{s * 0.6f, 6.8f, -4.4f}, {s * 1.4f, 5.6f, -5.6f}, {s * 2.0f, 3.6f, -6.2f}}, new float[]{0.4f, 0.32f, 0.12f}, 5);
    }

    static void whiskers(G g, A a) {
        g.color(0xFFFF8AC0);
        for (int s = -1; s <= 1; s += 2) Geo.ellipsoid(g, "hd_softglow", s * 2.6f, 2.6f, 4.05f, 0.9f, 0.55f, 0.05f, 10, 3);
        g.color(0xFF1B1B22);
        ball(g, "hd_gloss", 0, 2.9f, 4.2f, 0.35f, 6);
        for (int s = -1; s <= 1; s += 2)
            for (int i = -1; i <= 1; i++)
                Geo.tube(g, "hd_plastic", new float[]{s * 1.0f, 2.6f + i * 0.25f, 4.15f}, new float[]{s * 4.6f, 2.6f + i * 0.7f, 4.4f}, 0.05f, 0.03f, 3);
    }

    static void glasses3d(G g, A a) {
        g.color(0xFFF4F4F4);
        bbox(g, "hd_plastic", -4.0f, 3.0f, 4.2f, 4.0f, 5.6f, 4.55f, 0.15f);
        g.color(0xDDFF2A2A);
        bbox(g, "hd_glass", -3.5f, 3.3f, 4.5f, -0.6f, 5.3f, 4.62f, 0.1f);
        g.color(0xDD2AD8FF);
        bbox(g, "hd_glass", 0.6f, 3.3f, 4.5f, 3.5f, 5.3f, 4.62f, 0.1f);
        temples(g, 0xFFF4F4F4);
    }

    static void shutterShades(G g, int c) {
        g.color(c);
        cbox(g, "hd_plastic", 0, 5.4f, 4.45f, 8.0f, 0.4f, 0.4f, 0.1f);
        cbox(g, "hd_plastic", 0, 3.0f, 4.45f, 8.0f, 0.4f, 0.4f, 0.1f);
        for (int i = 0; i < 5; i++) {
            float y = 3.4f + i * 0.45f;
            for (int s = -1; s <= 1; s += 2) cbox(g, "hd_plastic", s * 2.0f, y, 4.45f, 3.4f, 0.16f, 0.3f, 0.04f);
        }
        temples(g, c);
    }

    static void laserEyes(G g, A a, int c) {
        g.glow(true);
        float flick = 0.75f + 0.25f * sin(a.time * 20);
        for (int s = -1; s <= 1; s += 2) {
            g.color(c);
            ball(g, "hd_softglow", s * 2.0f, 4.2f, 4.15f, 0.65f, 8);
            g.color(Cos3Geo.alpha(0.55 * flick, c & 0xFFFFFF));
            Geo.tube(g, "hd_softglow", new float[]{s * 2.0f, 4.2f, 4.3f}, new float[]{s * 2.0f, 4.0f, 22f}, 0.35f, 0.2f, 6);
            g.color(Cos3Geo.alpha(0.9, 0xFFFFFF));
            Geo.tube(g, "hd_softglow", new float[]{s * 2.0f, 4.2f, 4.3f}, new float[]{s * 2.0f, 4.0f, 22f}, 0.12f, 0.08f, 5);
        }
        g.glow(false);
    }

    static void headset(G g, A a, int body, int light) {
        g.color(body);
        float[][] arc = new float[11][];
        float[] ar = new float[11];
        for (int i = 0; i <= 10; i++) {
            double an = Math.PI * i / 10;
            arc[i] = new float[]{-cos(an) * 4.75f, 4.4f + sin(an) * 4.6f, 0};
            ar[i] = 0.38f;
        }
        Geo.chain(g, "hd_plastic", arc, ar, 6);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 4.6f, 4.4f, 0);
            g.rotZ(90);
            g.color(body);
            g.cylinder("hd_plastic", 1.9f, 1.9f, -0.5f, 0.5f, 18, true, 0, 0, 1, 1);
            g.color(light).glow(true);
            g.torus("hd_softglow", 1.9f, 0.12f, 18, 4, 0, 0, 1, 1);
            g.glow(false);
            g.pop();
        }
        g.color(body);
        Geo.chain(g, "hd_plastic", new float[][]{{-4.7f, 3.6f, 0.6f}, {-4.6f, 2.0f, 2.6f}, {-3.4f, 1.6f, 4.0f}, {-1.4f, 1.8f, 4.6f}}, new float[]{0.15f, 0.14f, 0.14f, 0.14f}, 5);
        g.color(0xFF22242C);
        ball(g, "hd_rubber", -1.2f, 1.8f, 4.65f, 0.45f, 6);
    }

    static void rose(G g, A a) {
        g.color(0xFF2E7D32);
        Geo.tube(g, "hd_vine", new float[]{-0.6f, 1.9f, 4.4f}, new float[]{-4.6f, 2.3f, 5.6f}, 0.12f, 0.1f, 5);
        g.color(0xFF4FAE52);
        Geo.ellipsoid(g, "hd_leaf", -2.6f, 2.2f, 5.1f, 0.6f, 0.08f, 0.3f, 5, 3);
        g.color(0xFFD7263D);
        for (int i = 0; i < 6; i++) {
            g.push();
            g.translate(1.0f, 2.0f, 4.6f);
            g.rotY(i * 60);
            g.rotX(-30 - (i % 2) * 20);
            Geo.ellipsoid(g, "hd_petal", 0, 0, 0.45f, 0.45f, 0.65f, 0.4f, 6, 4);
            g.pop();
        }
    }

    static void lolly(G g, A a) {
        g.color(0xFFF4F4F4);
        Geo.tube(g, "hd_plastic", new float[]{0.4f, 2.0f, 4.3f}, new float[]{2.6f, 1.6f, 7.0f}, 0.1f, 0.1f, 5);
        g.push();
        g.translate(2.9f, 1.55f, 7.4f);
        g.rotY(a.time * 40);
        g.rotX(90);
        int n = 6;
        for (int i = 0; i < n; i++) {
            g.color(i % 2 == 0 ? 0xFFFF4FA3 : 0xFFFFFFFF);
            g.torus("hd_gloss", 0.3f + i * 0.22f, 0.13f, 14, 4, 0, 0, 1, 1);
        }
        g.pop();
    }

    // ================================================================== BACK
    private static void backs() {
        int[] packs = {0xFFE3000B, 0xFF006CB7, 0xFF27AE60, 0xFF9B4DCA};
        String[] packN = {"Schulrucksack Rot", "Schulrucksack Blau", "Wanderrucksack Grün", "Rucksack Lila"};
        for (int i = 0; i < 4; i++) { int c = packs[i]; back("pack" + i, packN[i], C, (g, a) -> backpack(g, a, c)); }
        back("guitar_ac", "Akustikgitarre", E, (g, a) -> guitar(g, a, 0xFFC8873A, false));
        back("guitar_el", "Rote E-Gitarre", E, (g, a) -> guitar(g, a, 0xFFE3000B, true));
        back("shield_wood", "Holzschild", R, (g, a) -> roundShield(g, 0xFF8A5A30, 0xFF9AA3AD, 0));
        back("shield_iron", "Eisenschild", R, (g, a) -> roundShield(g, 0xFFB8C0CA, 0xFF5A6270, 1));
        back("shield_gold", "Goldschild", E, (g, a) -> roundShield(g, 0xFFE8B92C, 0xFFB0121B, 2));
        back("quiver", "Leder-Köcher", R, CosPack::quiver);
        back("jetpack", "Jetpack Silber", L, (g, a) -> jetpack(g, a, 0xFFB8C0CA));
        back("jetpack_red", "Raketen-Jetpack", L, (g, a) -> jetpack(g, a, 0xFFE3000B));
        back("turtle", "Schildkrötenpanzer", E, CosPack::turtle);
        back("sword_iron", "Eisenschwert", C, (g, a) -> sword(g, 0xFFD8DEE6, 0xFF6A4A2A));
        back("sword_gold", "Goldschwert", R, (g, a) -> sword(g, 0xFFFFD23F, 0xFF3A2414));
        back("sword_dia", "Diamantschwert", E, (g, a) -> sword(g, 0xFF5AF0E8, 0xFF3A2414));
        back("axe", "Holzfälleraxt", R, (g, a) -> axe(g, 0xFFB8C0CA, false));
        back("axe_double", "Doppelaxt", E, (g, a) -> axe(g, 0xFFD8DEE6, true));
        back("surf_blue", "Surfbrett Blau", R, (g, a) -> board(g, 0xFF2E86DE, 0xFFFFFFFF, 1.0f));
        back("surf_sunset", "Surfbrett Sunset", R, (g, a) -> board(g, 0xFFFF8A00, 0xFFFF4FA3, 1.0f));
        back("skate", "Skateboard Street", R, (g, a) -> board(g, 0xFF22242C, 0xFFFFCD03, 0.62f));
        back("balloons", "Rote Luftballons", E, (g, a) -> balloons(g, a, false));
        back("balloons_mix", "Bunte Luftballons", E, (g, a) -> balloons(g, a, true));
        back("oxygen", "Sauerstoffflasche", R, CosPack::oxygen);
        back("gift", "Geschenk-Rucksack", R, CosPack::giftPack);
        back("teddy", "Teddy-Rucksack", E, CosPack::teddy);
        back("kite", "Drachen", R, CosPack::kiteBack);
        back("trident", "Dreizack", E, CosPack::trident);
        back("bow", "Langbogen", R, CosPack::longbow);
        back("flag_red", "Fahne Rot", C, (g, a) -> flag(g, a, 0xFFE3000B, 0xFFFFFFFF, false));
        back("flag_blue", "Fahne Blau", C, (g, a) -> flag(g, a, 0xFF006CB7, 0xFFFFCD03, false));
        back("flag_pirate", "Piratenflagge", R, (g, a) -> flag(g, a, 0xFF1B1B22, 0xFFF4F4F4, true));
        int[] bricks = {0xFFE3000B, 0xFF006CB7, 0xFF00852B};
        String[] brickN = {"2x2-Stein Rot", "2x2-Stein Blau", "2x2-Stein Grün"};
        for (int i = 0; i < 3; i++) { int c = bricks[i]; back("brick" + i, brickN[i], R, (g, a) -> brickBack(g, c)); }
        back("snowboard", "Snowboard", R, (g, a) -> board(g, 0xFF39D353, 0xFF22242C, 1.1f));
        back("paddle", "Paddel", C, CosPack::paddle);
        back("windmill", "Windrad", E, CosPack::windmill);
        back("clock", "Rücken-Uhr", E, CosPack::clockBack);
        back("rod", "Angelrute", C, CosPack::rodBack);
    }

    static void backpack(G g, A a, int c) {
        float bob = a.preview ? 0 : a.move * 0.15f * Math.abs(sin(a.time * 8));
        g.push();
        g.translate(0, -1.6f + bob, -2.1f);
        g.color(c);
        bbox(g, "hd_cotton", -3.4f, -9.0f, -3.4f, 3.4f, 0, 0, 0.9f);
        g.color(dark(c, 0.18f));
        bbox(g, "hd_cotton", -2.6f, -8.2f, -4.4f, 2.6f, -4.6f, -3.2f, 0.6f);
        g.color(0xFFD8DEE6);
        cbox(g, "hd_steel", 0, -4.8f, -4.45f, 1.4f, 0.3f, 0.2f, 0.05f);
        g.color(dark(c, 0.4f));
        for (int s = -1; s <= 1; s += 2) cbox(g, "hd_leather", s * 2.2f, -4.0f, 0.15f, 0.9f, 8.0f, 0.3f, 0.1f);
        g.pop();
    }

    static void guitar(G g, A a, int body, boolean electric) {
        g.push();
        g.translate(0, -6.0f, -2.6f);
        g.rotZ(32);
        g.color(body);
        if (electric) {
            extrude(g, "hd_plastic", new float[][]{{-3.2f, -5}, {3.0f, -5.2f}, {3.6f, -2.0f}, {2.0f, 0}, {3.2f, 2.6f}, {1.2f, 3.0f}, {-1.4f, 1.0f}, {-3.4f, 2.6f}, {-3.8f, -1.6f}}, -0.7f, 0.0f);
        } else {
            g.push();
            g.rotX(90);
            Geo.ellipsoid(g, "guitar_body", 0, 0.35f, -2.2f, 3.6f, 0.75f, 3.2f, 16, 6);
            Geo.ellipsoid(g, "guitar_body", 0, 0.35f, 1.6f, 2.8f, 0.75f, 2.5f, 16, 6);
            g.pop();
        }
        g.color(0xFF3A2414);
        cbox(g, "hd_wood", 0, 7.5f, -0.4f, 1.1f, 11.0f, 0.5f, 0.1f);
        cbox(g, "hd_wood", 0, 13.6f, -0.4f, 1.6f, 2.2f, 0.5f, 0.1f);
        g.color(0xFFE0E0E0);
        for (int i = -1; i <= 1; i++) Geo.tube(g, "hd_steel", new float[]{i * 0.3f, -3.2f, -0.75f}, new float[]{i * 0.3f, 13.0f, -0.7f}, 0.04f, 0.04f, 3);
        g.pop();
        g.color(0xFF3A2414);
        Geo.chain(g, "hd_leather", new float[][]{{-3.6f, -0.2f, 0.4f}, {-3.0f, 0.4f, -2.2f}, {2.6f, -10.0f, -2.6f}}, new float[]{0.2f, 0.2f, 0.2f}, 4);
    }

    static void roundShield(G g, int face, int rim, int emblem) {
        g.push();
        g.translate(0, -6.0f, -2.4f);
        g.rotX(90);
        g.color(face);
        lathe(g, emblem == 0 ? "hd_wood" : "hd_steel", new float[]{0, 5.4f, 5.6f, 0}, new float[]{0, 0, -0.9f, -1.6f}, 24);
        g.color(rim);
        g.push();
        g.translate(0, -0.4f, 0);
        g.torus("hd_steel", 5.5f, 0.35f, 24, 5, 0, 0, 1, 1);
        g.pop();
        g.color(rim);
        ball(g, "hd_steel", 0, -1.55f, 0, 1.2f, 10);
        if (emblem == 2) {
            g.color(0xFFB0121B);
            g.push();
            g.translate(0, -1.0f, 0);
            g.rotX(-90);
            extrude(g, "hd_satin", starPoly(3.4f, 1.4f, 5), 0.2f, 0.45f);
            g.pop();
        }
        g.pop();
    }

    static void quiver(G g, A a) {
        g.push();
        g.translate(1.0f, -5.4f, -3.0f);
        g.rotZ(-22);
        g.color(0xFF8A5A30);
        g.cylinder("hd_leather", 1.5f, 1.3f, -5.0f, 4.0f, 14, true, 0, 0, 1, 1);
        g.color(0xFFE8B92C);
        g.torus("hd_gold", 1.5f, 0.15f, 14, 4, 0, 0, 1, 1);
        for (int i = 0; i < 5; i++) {
            double an = i * 1.3;
            float x = cos(an) * 0.7f, z = sin(an) * 0.7f;
            g.color(0xFF6A4A2A);
            Geo.tube(g, "hd_wood", new float[]{x, 3.0f, z}, new float[]{x, 7.4f + (i % 2) * 0.6f, z}, 0.1f, 0.1f, 4);
            g.color(i % 2 == 0 ? 0xFFF4F4F4 : 0xFFE3000B);
            cbox(g, "hd_fluff", x, 6.8f + (i % 2) * 0.6f, z, 0.08f, 1.4f, 0.7f, 0.02f);
        }
        g.pop();
    }

    static void jetpack(G g, A a, int c) {
        g.push();
        g.translate(0, -2.0f, -2.2f);
        g.color(c);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.translate(s * 1.7f, 0, -1.6f);
            g.cylinder("hd_steel", 1.4f, 1.4f, -8.0f, -1.0f, 16, true, 0, 0, 1, 1);
            lathe(g, "hd_steel", new float[]{1.4f, 1.0f, 0}, new float[]{-1.0f, -0.2f, 0.2f}, 16);
            g.color(0xFF3A3E48);
            lathe(g, "hd_steel", new float[]{0.7f, 1.0f, 1.0f}, new float[]{-8.0f, -9.0f, -9.2f}, 12);
            g.glow(true);
            float fl = 0.8f + 0.2f * sin(a.time * 30 + s);
            g.color(0xFFFFB030);
            lathe(g, "c3_flamegrad", new float[]{0.9f, 0.7f, 0.3f, 0}, new float[]{-9.2f, -10.4f * fl, -11.6f * fl, -12.8f * fl}, 10);
            g.glow(false);
            g.color(c);
            g.pop();
        }
        g.color(dark(c, 0.35f));
        bbox(g, "hd_steel", -1.0f, -6.0f, -1.6f, 1.0f, -2.0f, 0, 0.3f);
        g.pop();
    }

    static void turtle(G g, A a) {
        g.push();
        g.translate(0, -6.4f, -2.2f);
        g.rotX(-90);
        g.scale(1, 1, 1.35f);
        g.color(0xFF3E7A2E);
        lathe(g, "hd_leather", new float[]{0, 4.4f, 4.6f, 4.3f, 3.2f, 1.6f, 0}, new float[]{0, 0, 0.6f, 1.6f, 2.8f, 3.4f, 3.5f}, 6);
        g.color(0xFF6AA84F);
        for (int i = 0; i < 6; i++) {
            double an = Math.PI * 2 * i / 6 + Math.PI / 6;
            g.push();
            g.translate(cos(an) * 2.4f, 2.6f, sin(an) * 2.4f);
            lathe(g, "hd_leather", new float[]{1.1f, 0.9f, 0}, new float[]{0, 0.4f, 0.55f}, 6);
            g.pop();
        }
        g.push();
        g.translate(0, 3.2f, 0);
        lathe(g, "hd_leather", new float[]{1.3f, 1.0f, 0}, new float[]{0, 0.35f, 0.45f}, 6);
        g.pop();
        g.pop();
    }

    static void sword(G g, int blade, int grip) {
        g.push();
        g.translate(0, -5.0f, -2.6f);
        g.rotZ(-35);
        g.color(blade);
        extrude(g, "hd_steel", new float[][]{{-0.75f, 0}, {0.75f, 0}, {0.75f, 11.5f}, {0, 13.2f}, {-0.75f, 11.5f}}, -0.22f, 0.22f);
        g.color(0xFF5A6270);
        cbox(g, "hd_steel", 0, -0.3f, 0, 4.0f, 0.6f, 0.8f, 0.2f);
        g.color(grip);
        g.cylinder("hd_leather", 0.4f, 0.4f, -3.6f, -0.6f, 8, false, 0, 0, 1, 1);
        g.color(0xFF5A6270);
        ball(g, "hd_steel", 0, -3.9f, 0, 0.6f, 8);
        g.pop();
    }

    static void axe(G g, int head, boolean dbl) {
        g.push();
        g.translate(0, -5.4f, -2.6f);
        g.rotZ(30);
        g.color(0xFF6A4A2A);
        g.cylinder("hd_wood", 0.4f, 0.4f, -6.0f, 7.0f, 8, true, 0, 0, 1, 1);
        g.color(head);
        for (int s = dbl ? -1 : 1; s <= 1; s += 2) {
            g.push();
            g.scale(s, 1, 1);
            extrude(g, "hd_steel", new float[][]{{0.3f, 3.6f}, {2.4f, 2.8f}, {3.6f, 4.0f}, {3.8f, 6.6f}, {2.4f, 7.6f}, {0.3f, 6.6f}}, -0.25f, 0.25f);
            g.pop();
        }
        g.pop();
    }

    static void board(G g, int c1, int c2, float len) {
        g.push();
        g.translate(0, -6.0f, -2.6f);
        g.rotZ(12);
        float h = 12 * len;
        g.color(c1);
        float[][] poly = new float[20][];
        for (int i = 0; i < 20; i++) {
            double an = Math.PI * 2 * i / 20;
            poly[i] = new float[]{cos(an) * 2.8f, sin(an) * h};
        }
        extrude(g, "hd_plastic", poly, -0.5f, 0);
        g.color(c2);
        cbox(g, "hd_plastic", 0, 0, -0.52f, 0.6f, h * 1.7f, 0.06f, 0.02f);
        if (len < 0.8f) {
            g.color(0xFF9AA3AD);
            for (int s = -1; s <= 1; s += 2) {
                cbox(g, "hd_steel", 0, s * h * 0.6f, -0.9f, 3.2f, 0.6f, 0.6f, 0.1f);
                g.color(0xFFF4F4F4);
                for (int k = -1; k <= 1; k += 2) {
                    g.push();
                    g.translate(k * 1.7f, s * h * 0.6f, -1.2f);
                    g.rotZ(90);
                    g.cylinder("hd_rubber", 0.5f, 0.5f, -0.3f, 0.3f, 10, true, 0, 0, 1, 1);
                    g.pop();
                }
                g.color(0xFF9AA3AD);
            }
        }
        g.pop();
    }

    static void balloons(G g, A a, boolean mix) {
        int[] cols = mix ? new int[]{0xFFE3000B, 0xFFFFCD03, 0xFF2E86DE, 0xFF27AE60, 0xFFFF4FA3} : new int[]{0xFFE3000B, 0xFFD01020, 0xFFFF2A3A, 0xFFE3000B, 0xFFB0101A};
        for (int i = 0; i < 5; i++) {
            double an = i * 1.25 - 2.5;
            float sw = sin(a.time * 1.5 + i) * 0.6f;
            float x = sin(an) * 4.5f + sw, y = 12 + (i % 2) * 2.4f + cos(a.time + i) * 0.3f, z = -6.5f - cos(an) * 1.2f;
            g.color(0xFFF4F4F4);
            Geo.tube(g, "hd_cotton", new float[]{0, -4.0f, -2.4f}, new float[]{x, y - 2.4f, z}, 0.04f, 0.04f, 3);
            g.color(cols[i]);
            Geo.ellipsoid(g, "hd_gloss", x, y, z, 2.0f, 2.4f, 2.0f, 12, 8);
        }
    }

    static void oxygen(G g, A a) {
        g.push();
        g.translate(0, -1.6f, -4.0f);
        g.color(0xFFFFCD03);
        g.cylinder("hd_steel", 1.8f, 1.8f, -10.0f, -1.2f, 18, false, 0, 0, 1, 1);
        lathe(g, "hd_steel", new float[]{1.8f, 1.4f, 0.5f, 0}, new float[]{-1.2f, -0.2f, 0.3f, 0.35f}, 18);
        lathe(g, "hd_steel", new float[]{1.8f, 1.2f, 0}, new float[]{-10.0f, -10.8f, -11.0f}, 18);
        g.color(0xFF5A6270);
        g.cylinder("hd_steel", 0.4f, 0.4f, 0.2f, 1.2f, 8, true, 0, 0, 1, 1);
        g.color(0xFF22242C);
        Geo.chain(g, "hd_rubber", new float[][]{{0, 1.2f, 0}, {2.4f, 1.8f, 1.0f}, {3.8f, 0.8f, 3.0f}, {4.2f, -1.6f, 4.2f}}, new float[]{0.25f, 0.25f, 0.25f, 0.25f}, 6);
        g.pop();
    }

    static void giftPack(G g, A a) {
        g.push();
        g.translate(0, -5.0f, -4.4f);
        g.color(0xFF2E86DE);
        cbox(g, "hd_party", 0, 0, 0, 7.0f, 8.0f, 4.4f, 0.3f);
        g.color(0xFFE3000B);
        cbox(g, "hd_satin", 0, 0, 0, 1.2f, 8.1f, 4.5f, 0.05f);
        cbox(g, "hd_satin", 0, 0, 0, 7.1f, 1.2f, 4.5f, 0.05f);
        g.translate(0, 4.2f, 0);
        for (int s = -1; s <= 1; s += 2) {
            g.push();
            g.rotZ(s * 50);
            lathe(g, "hd_satin", new float[]{0.3f, 1.2f, 1.0f, 0}, new float[]{0, 0.6f, 2.0f, 2.4f}, 10);
            g.pop();
        }
        g.pop();
    }

    static void teddy(G g, A a) {
        g.push();
        g.translate(0, -5.4f, -4.4f);
        g.color(0xFFA0683A);
        Geo.ellipsoid(g, "hd_fur", 0, -1.6f, 0, 3.0f, 3.4f, 2.4f, 14, 8);
        Geo.ellipsoid(g, "hd_fur", 0, 3.0f, -0.2f, 2.6f, 2.4f, 2.2f, 14, 8);
        for (int s = -1; s <= 1; s += 2) {
            ball(g, "hd_fur", s * 2.0f, 5.0f, -0.2f, 1.0f, 8);
            Geo.ellipsoid(g, "hd_fur", s * 3.0f, -0.6f, -0.6f, 1.0f, 1.8f, 1.0f, 8, 6);
            Geo.ellipsoid(g, "hd_fur", s * 1.6f, -4.8f, -0.8f, 1.1f, 1.0f, 1.4f, 8, 6);
        }
        g.color(0xFFE0B48A);
        Geo.ellipsoid(g, "hd_fur", 0, 2.4f, -2.2f, 1.2f, 0.9f, 0.6f, 10, 5);
        g.color(0xFF1B1B22);
        ball(g, "hd_gloss", 0, 2.8f, -2.75f, 0.35f, 6);
        for (int s = -1; s <= 1; s += 2) ball(g, "hd_gloss", s * 0.9f, 3.9f, -2.1f, 0.3f, 6);
        g.pop();
    }

    static void kiteBack(G g, A a) {
        g.push();
        g.translate(0, -2.0f, -3.0f);
        g.rotZ(8 + sin(a.time * 1.3f) * 4);
        float[][] d = {{0, 7}, {-4.4f, 0}, {0, -8}, {4.4f, 0}};
        for (int i = 0; i < 4; i++) {
            g.color(i % 2 == 0 ? 0xFFE3000B : 0xFFFFCD03);
            float[] p0 = d[i], p1 = d[(i + 1) % 4];
            extrude(g, "hd_satin", new float[][]{{0, 0}, p0, p1}, -0.1f, 0.1f);
        }
        g.color(0xFF3A2414);
        Geo.tube(g, "hd_wood", new float[]{0, 7, 0.15f}, new float[]{0, -8, 0.15f}, 0.12f, 0.12f, 4);
        Geo.tube(g, "hd_wood", new float[]{-4.4f, 0, 0.15f}, new float[]{4.4f, 0, 0.15f}, 0.12f, 0.12f, 4);
        g.color(0xFF2E86DE);
        for (int i = 0; i < 4; i++) {
            float y = -9.5f - i * 1.6f, x = sin(a.time * 3 + i) * 0.8f;
            g.push();
            g.translate(x, y, 0);
            g.rotZ(45);
            cbox(g, "hd_satin", 0, 0, 0, 0.9f, 0.9f, 0.08f, 0.02f);
            g.pop();
        }
        g.pop();
    }

    static void trident(G g, A a) {
        g.push();
        g.translate(0, -5.0f, -2.8f);
        g.rotZ(-28);
        g.color(0xFF2A9D8F);
        g.cylinder("hd_steel", 0.35f, 0.35f, -9.0f, 7.0f, 8, true, 0, 0, 1, 1);
        g.color(0xFF5AF0E8).glow(true);
        cbox(g, "hd_steel", 0, 7.2f, 0, 4.4f, 0.6f, 0.7f, 0.2f);
        for (int i = -1; i <= 1; i++) {
            g.push();
            g.translate(i * 1.9f, 7.4f, 0);
            lathe(g, "hd_steel", new float[]{0.35f, 0.35f, 0}, new float[]{0, 3.2f - Math.abs(i) * 0.6f, 4.6f - Math.abs(i) * 0.6f}, 6);
            g.pop();
        }
        g.glow(false);
        g.pop();
    }

    static void longbow(G g, A a) {
        g.push();
        g.translate(0, -6.0f, -2.6f);
        g.rotZ(20);
        g.color(0xFF8A5A30);
        float[][] pts = new float[9][];
        float[] rad = new float[9];
        for (int i = 0; i < 9; i++) {
            float f = (i - 4) / 4f;
            pts[i] = new float[]{0, f * 10, -(1 - f * f) * 2.6f};
            rad[i] = 0.35f - Math.abs(f) * 0.18f;
        }
        Geo.chain(g, "hd_wood", pts, rad, 6);
        g.color(0xFFF4F4F4);
        Geo.tube(g, "hd_cotton", new float[]{0, 10, 0}, new float[]{0, -10, 0}, 0.04f, 0.04f, 3);
        g.pop();
    }

    static void flag(G g, A a, int c1, int c2, boolean skull) {
        g.push();
        g.translate(2.2f, -8.0f, -2.6f);
        g.color(0xFF6A4A2A);
        g.cylinder("hd_wood", 0.22f, 0.22f, 0, 22, 8, true, 0, 0, 1, 1);
        g.color(0xFFE8B92C);
        ball(g, "hd_gold", 0, 22.2f, 0, 0.45f, 8);
        int seg = 8;
        for (int i = 0; i < seg; i++) {
            float f0 = i / (float) seg, f1 = (i + 1f) / seg;
            float z0 = sin(a.time * 4 - f0 * 4) * 0.8f * f0, z1 = sin(a.time * 4 - f1 * 4) * 0.8f * f1;
            float x0 = f0 * 9, x1 = f1 * 9;
            g.color(skull ? c1 : (f0 < 0.5f ? c1 : c2));
            g.quad("white", new float[]{x0, 21.6f, z0}, new float[]{x1, 21.6f, z1}, new float[]{x1, 15.6f, z1}, new float[]{x0, 15.6f, z0}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.quad("white", new float[]{x1, 21.6f, z1}, new float[]{x0, 21.6f, z0}, new float[]{x0, 15.6f, z0}, new float[]{x1, 15.6f, z1}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        if (skull) {
            float z = sin(a.time * 4 - 2.2f) * 0.8f * 0.5f;
            g.color(c2);
            ball(g, "hd_bone", 4.5f, 19.4f, z + 0.2f, 1.1f, 8);
            ball(g, "hd_bone", 4.5f, 19.4f, z - 0.2f, 1.1f, 8);
        }
        g.pop();
    }

    static void brickBack(G g, int c) {
        g.push();
        g.translate(0, -1.4f, -2.2f);
        g.color(c);
        bbox(g, "hd_brick", -3.4f, -8.0f, -4.0f, 3.4f, 0, 0, 0.3f);
        for (int ix = 0; ix < 2; ix++)
            for (int iy = 0; iy < 2; iy++) {
                g.push();
                g.translate(-1.7f + ix * 3.4f, -2.0f - iy * 4.0f, -4.0f);
                g.rotX(-90);
                lathe(g, "hd_brick", new float[]{1.2f, 1.2f, 1.1f, 0}, new float[]{-0.02f, 0.9f, 1.0f, 1.02f}, 14);
                g.pop();
            }
        g.pop();
    }

    static void paddle(G g, A a) {
        g.push();
        g.translate(0, -6.0f, -2.6f);
        g.rotZ(-25);
        g.color(0xFFC8873A);
        g.cylinder("hd_wood", 0.35f, 0.35f, -9.0f, 6.0f, 8, true, 0, 0, 1, 1);
        g.color(0xFFE3000B);
        g.push();
        g.translate(0, 9.0f, 0);
        Geo.ellipsoid(g, "hd_plastic", 0, 0, 0, 1.8f, 3.4f, 0.3f, 12, 6);
        g.pop();
        g.pop();
    }

    static void windmill(G g, A a) {
        g.push();
        g.translate(0, -2.0f, -2.6f);
        g.color(0xFFF4F4F4);
        g.cylinder("hd_plastic", 0.3f, 0.3f, -6.0f, 4.0f, 8, true, 0, 0, 1, 1);
        g.translate(0, 4.0f, -0.6f);
        g.rotZ(a.time * 160);
        for (int i = 0; i < 3; i++) {
            g.push();
            g.rotZ(i * 120);
            g.color(i == 0 ? 0xFFE3000B : 0xFFF4F4F4);
            extrude(g, "hd_plastic", new float[][]{{-0.2f, 0}, {0.6f, 0.4f}, {0.8f, 6.0f}, {0, 6.4f}, {-0.3f, 0.6f}}, -0.08f, 0.08f);
            g.pop();
        }
        g.color(0xFF9AA3AD);
        ball(g, "hd_steel", 0, 0, 0, 0.6f, 8);
        g.pop();
    }

    static void clockBack(G g, A a) {
        g.push();
        g.translate(0, -5.0f, -2.4f);
        g.rotX(90);
        g.color(0xFFE8B92C);
        lathe(g, "hd_gold", new float[]{0, 4.6f, 4.8f, 0}, new float[]{0, 0, -1.0f, -1.2f}, 24);
        g.color(0xFFF8F4E8);
        g.ring("hd_cotton", 0, 4.2f, -1.21f, 24, 0, 0, 1, 1);
        g.color(0xFF1B1B22);
        for (int i = 0; i < 12; i++) {
            double an = Math.PI * 2 * i / 12;
            g.push();
            g.translate(cos(an) * 3.6f, -1.25f, sin(an) * 3.6f);
            g.rotY((float) -Math.toDegrees(an));
            cbox(g, "hd_plastic", 0, 0, 0, i % 3 == 0 ? 0.8f : 0.45f, 0.06f, 0.15f, 0.02f);
            g.pop();
        }
        float sec = (float) ((System.currentTimeMillis() / 1000.0) % 60), min = (float) ((System.currentTimeMillis() / 60000.0) % 60);
        g.push();
        g.translate(0, -1.3f, 0);
        g.rotY(-min * 6);
        cbox(g, "hd_plastic", 0, 0, 1.5f, 0.3f, 0.06f, 3.0f, 0.02f);
        g.pop();
        g.color(0xFFE3000B);
        g.push();
        g.translate(0, -1.32f, 0);
        g.rotY(-sec * 6);
        cbox(g, "hd_plastic", 0, 0, 1.8f, 0.12f, 0.06f, 3.6f, 0.02f);
        g.pop();
        g.pop();
    }

    static void rodBack(G g, A a) {
        g.push();
        g.translate(1.0f, -6.0f, -2.6f);
        g.rotZ(-18);
        g.color(0xFF6A4A2A);
        Geo.chain(g, "hd_wood", new float[][]{{0, -6, 0}, {0, 4, 0}, {0.6f, 12, 0}, {1.6f, 17, 0}}, new float[]{0.35f, 0.25f, 0.16f, 0.08f}, 6);
        g.color(0xFFB8C0CA);
        g.push();
        g.translate(0.4f, -3.0f, 0);
        g.rotZ(90);
        g.cylinder("hd_steel", 0.9f, 0.9f, -0.4f, 0.4f, 12, true, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFF4F4F4);
        float sw = sin(a.time * 1.4f) * 0.8f;
        Geo.tube(g, "hd_cotton", new float[]{1.6f, 17, 0}, new float[]{2.4f + sw, 9, 0}, 0.03f, 0.03f, 3);
        g.color(0xFFE3000B);
        ball(g, "hd_gloss", 2.4f + sw, 8.6f, 0, 0.45f, 8);
        g.pop();
    }

    // ================================================================== AURAS
    /** Aura patterns. */
    private static final int ORBIT = 0, SPIRAL = 1, RAIN = 2, RISE = 3, RINGS = 4, CROWN = 5, HELIX = 6, BURST = 7;

    private static void auras() {
        Object[][] list = {
                {"heart_orbit", "Herz-Orbit", R, ORBIT, "heart", 0xFFFF4F7A},
                {"heart_rise", "Aufsteigende Herzen", R, RISE, "heart", 0xFFFF8AC0},
                {"heart_helix", "Herz-Helix", E, HELIX, "heart", 0xFFFF2A5A},
                {"heart_burst", "Herzexplosion", E, BURST, "heart", 0xFFFF4FA3},
                {"heart_crown", "Herzkrone", R, CROWN, "heart", 0xFFFF6F9A},
                {"heart_rain", "Herzregen", R, RAIN, "heart", 0xFFFFB0C8},
                {"snow_orbit", "Schneeflocken-Orbit", R, ORBIT, "snowflake", 0xFFCFF6FF},
                {"snow_rain", "Schneeflocken-Regen", R, RAIN, "snowflake", 0xFFFFFFFF},
                {"snow_spiral", "Frost-Spirale", E, SPIRAL, "snowflake", 0xFF8AE8FF},
                {"snow_crown", "Eiskranz", E, CROWN, "snowflake", 0xFFBFEFFF},
                {"snow_helix", "Blizzard-Helix", E, HELIX, "snowflake", 0xFFE0FAFF},
                {"snow_rings", "Frostringe", R, RINGS, "snowflake", 0xFF9FD8FF},
                {"flame_spiral", "Flammen-Spirale", E, SPIRAL, "flame", 0xFFFFFFFF},
                {"flame_crown", "Feuerkranz", L, CROWN, "flame", 0xFFFFFFFF},
                {"flame_helix", "Feuer-Helix", L, HELIX, "flame", 0xFFFFFFFF},
                {"flame_burst", "Flammenstoß", E, BURST, "flame", 0xFFFFFFFF},
                {"flame_rings", "Glutringe", E, RINGS, "flame", 0xFFFFC080},
                {"flame_orbit", "Feuerbälle", E, ORBIT, "flame", 0xFFFFFFFF},
                {"bolt_orbit", "Blitz-Orbit", E, ORBIT, "bolt", 0xFFFFF04A},
                {"bolt_rain", "Blitzregen", L, RAIN, "bolt", 0xFF9FD8FF},
                {"bolt_burst", "Elektroschock", L, BURST, "bolt", 0xFFFFFFFF},
                {"bolt_crown", "Donnerkrone", L, CROWN, "bolt", 0xFFFFE04A},
                {"bolt_helix", "Spannungsfeld", E, HELIX, "bolt", 0xFFB0F0FF},
                {"spark_gold", "Goldfunken", R, RISE, "spark", 0xFFFFD23F},
                {"spark_pink", "Pinke Funken", R, SPIRAL, "spark", 0xFFFF4FA3},
                {"spark_blue", "Blaue Funken", R, HELIX, "spark", 0xFF4FC3FF},
                {"spark_green", "Grüne Funken", R, RAIN, "spark", 0xFF39D353},
                {"spark_burst", "Feuerwerk", L, BURST, "spark", 0xFFFFFFFF},
                {"spark_rings", "Funkenringe", E, RINGS, "spark", 0xFFFFE27A},
                {"orb_purple_orbit", "Lila Seelen", E, ORBIT, "orb_purple", 0xFFFFFFFF},
                {"orb_purple_helix", "Mystische Helix", E, HELIX, "orb_purple", 0xFFFFFFFF},
                {"orb_cyan_orbit", "Cyan-Kugeln", R, ORBIT, "orb_cyan", 0xFFFFFFFF},
                {"orb_cyan_rise", "Geisterlichter", E, RISE, "orb_cyan", 0xFFFFFFFF},
                {"orb_red_spiral", "Blutmond-Spirale", L, SPIRAL, "orb", 0xFFFF2A2A},
                {"orb_green_rise", "Glühwürmchen-Schwarm", R, RISE, "orb", 0xFFB8FF4F},
                {"orb_gold_crown", "Goldene Lichterkrone", L, CROWN, "orb", 0xFFFFD23F},
                {"orb_white_rings", "Lichtringe", E, RINGS, "orb", 0xFFFFFFFF},
                {"orb_rainbow", "Regenbogen-Kugeln", L, ORBIT, "orb", 0},
                {"orb_rainbow_helix", "Regenbogen-Helix", L, HELIX, "orb", 0},
                {"heart_rainbow", "Regenbogen-Herzen", L, SPIRAL, "heart", 0},
                {"note_orbit", "Noten-Orbit", R, ORBIT, "hd_note", 0xFFFFFFFF},
                {"note_rise", "Melodie", R, RISE, "hd_note", 0xFF7CF7FF},
                {"bubble_rise", "Blubberblasen", C, RISE, "hd_bubble", 0xFFFFFFFF},
                {"bubble_orbit", "Blasen-Orbit", C, ORBIT, "hd_bubble", 0xFFBFEFFF},
                {"leaf_spiral", "Blätterwirbel", C, SPIRAL, "hd_leaf", 0xFF6AB04C},
                {"leaf_rain", "Blätterregen", C, RAIN, "hd_leaf", 0xFFE08A2A},
                {"petal_rain", "Blütenregen", R, RAIN, "hd_petal", 0xFFFF9AC0},
                {"rune_rings", "Runenringe", L, RINGS, "hd_rune", 0xFFB15CFF},
        };
        for (Object[] o : list) {
            int pat = (int) o[3], col = (int) o[5];
            String tex = (String) o[4];
            aura((String) o[0], (String) o[1], (Rarity) o[2], (g, a) -> particleAura(g, a, pat, tex, col));
        }
    }

    static void particleAura(G g, A a, int pat, String tex, int col) {
        g.glow(true);
        float t = a.time;
        boolean flame = tex.equals("flame");
        switch (pat) {
            case ORBIT -> {
                for (int i = 0; i < 10; i++) {
                    double an = t * 1.2 + Math.PI * 2 * i / 10;
                    float y = -12 + sin(t * 1.5 + i) * 4;
                    put(g, tex, col, i, t, cos(an) * 7.5f, y, sin(an) * 7.5f, 1.6f, 1, flame);
                }
            }
            case SPIRAL -> {
                for (int i = 0; i < 16; i++) {
                    float ph = fract(t * 0.35f + i / 16f);
                    double an = ph * Math.PI * 6 + t;
                    float r = 7 - ph * 3;
                    put(g, tex, col, i, t, cos(an) * r, -24 + ph * 30, sin(an) * r, 1.5f * (1 - ph * 0.5f), (float) Math.sin(ph * Math.PI), flame);
                }
            }
            case RAIN -> {
                for (int i = 0; i < 18; i++) {
                    float ph = fract(t * 0.45f + i * 0.137f);
                    float x = (fract(i * 0.618f) - 0.5f) * 16, z = (fract(i * 0.381f + 0.3f) - 0.5f) * 16;
                    put(g, tex, col, i, t, x + sin(t + i) * 0.6f, 14 - ph * 38, z, 1.3f, (float) Math.sin(ph * Math.PI), flame);
                }
            }
            case RISE -> {
                for (int i = 0; i < 16; i++) {
                    float ph = fract(t * 0.3f + i * 0.173f);
                    double an = i * 2.4;
                    float r = 4 + fract(i * 0.41f) * 4;
                    put(g, tex, col, i, t, cos(an) * r + sin(t * 2 + i) * 0.5f, -24 + ph * 34, sin(an) * r, 1.3f, (float) Math.sin(ph * Math.PI), flame);
                }
            }
            case RINGS -> {
                for (int ring = 0; ring < 3; ring++) {
                    float ph = fract(t * 0.4f + ring / 3f);
                    float r = 3 + ph * 8, y = -23.5f + ring * 0.1f;
                    for (int i = 0; i < 12; i++) {
                        double an = Math.PI * 2 * i / 12 + ring;
                        put(g, tex, col, i + ring * 12, t, cos(an) * r, y + ph * 2, sin(an) * r, 1.2f, 1 - ph, flame);
                    }
                }
            }
            case CROWN -> {
                for (int i = 0; i < 9; i++) {
                    double an = t * 0.8 + Math.PI * 2 * i / 9;
                    put(g, tex, col, i, t, cos(an) * 5.6f, 5.0f + sin(t * 3 + i) * 0.4f, sin(an) * 5.6f, 1.3f, 1, flame);
                }
            }
            case HELIX -> {
                for (int k = 0; k < 2; k++)
                    for (int i = 0; i < 12; i++) {
                        float f = i / 12f;
                        double an = t * 2 + f * Math.PI * 4 + k * Math.PI;
                        put(g, tex, col, i + k * 12, t, cos(an) * 6.5f, -24 + f * 32, sin(an) * 6.5f, 1.2f, 0.9f, flame);
                    }
            }
            default -> {
                float ph = fract(t * 0.5f);
                for (int i = 0; i < 18; i++) {
                    double an = i * 2.399, el = (fract(i * 0.37f) - 0.3f) * 1.6;
                    float r = 2 + ph * 12;
                    put(g, tex, col, i, t, (float) (Math.cos(an) * Math.cos(el)) * r, -10 + (float) Math.sin(el) * r, (float) (Math.sin(an) * Math.cos(el)) * r, 1.4f * (1 - ph * 0.6f), 1 - ph, flame);
                }
            }
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    private static void put(G g, String tex, int col, int i, float t, float x, float y, float z, float s, float alpha, boolean flame) {
        int c = col == 0 ? Geo.hue(t * 0.2 + i * 0.08, 0.75, 1) : col;
        g.color(Cos3Geo.alpha(Math.max(0, Math.min(1, alpha)) * ((c >>> 24) / 255.0), c & 0xFFFFFF));
        Geo.sprite(g, tex, x, y + (flame ? s * 0.6f : 0), z, flame ? s * 1.7f : s);
    }

    // ================================================================== WINGS
    private static void wings() {
        Object[][] fea = {
                {"sky", "Himmelsflügel", R, "FFFFFF_BFE6FF_6AB8FF_FFFFFF_none", false, 0},
                {"rose", "Rosenflügel", E, "FFE6EE_FF9AC0_D63A7A_FFFFFF_none", false, 0},
                {"mint", "Minzflügel", R, "F0FFF6_8AF0C0_2AA878_FFFFFF_none", false, 0},
                {"sun", "Sonnenflügel", L, "FFF6C0_FFC030_FF6A00_FFFFFF_flame", true, 2},
                {"night", "Nachtflügel", E, "3A3A6A_1A1A3A_06061A_A8B8FF_stars", true, 5},
                {"raven", "Rabenflügel", R, "3A3E48_1A1C22_050608_6A7080_rim", false, 3},
                {"parrot", "Papageienflügel", E, "FFE04A_39D353_2E86DE_FFFFFF_bars", false, 0},
                {"peacock", "Pfauenflügel", L, "7CF7D0_1E8A8A_0A3A6A_FFD23F_stars", true, 5},
                {"flamingo", "Flamingoflügel", R, "FFE0EA_FF8AB0_E04A7A_FFFFFF_none", false, 0},
                {"silver", "Silberflügel", E, "FFFFFF_D0D6DE_7A8494_FFFFFF_metal", false, 0},
                {"bronze", "Bronzeflügel", R, "FFE0B0_C8873A_6A3A12_FFF0D0_metal", false, 0},
                {"ice", "Frostfedern", E, "FFFFFF_BFF6FF_4ACFFF_FFFFFF_crystal", true, 5},
                {"lava", "Lavaflügel", L, "FFE07A_FF6A00_8A0A00_FFFFFF_fire", true, 2},
                {"toxic", "Giftflügel", E, "F0FFB0_8AE02A_2A6A0A_D0FF6A_rim", true, 3},
                {"violet", "Veilchenflügel", R, "F0E0FF_B88AFF_6A2AC8_FFFFFF_none", false, 0},
                {"candy", "Zuckerwatteflügel", R, "FFFFFF_FFC0E0_9AD8FF_FFFFFF_none", false, 4},
                {"eagle", "Adlerflügel", E, "F4E8D0_8A5A30_3A2414_F4F0E8_bars", false, 0},
                {"dove", "Taubenflügel", C, "FFFFFF_F0F0F0_C8C8D0_FFFFFF_none", false, 0},
                {"storm", "Sturmflügel", L, "E0E8FF_7A8AB0_2A3450_9FD8FF_stars", true, 5},
                {"emerald", "Smaragdflügel", L, "E0FFE8_3AD87A_0A6A3A_FFFFFF_crystal", true, 5},
        };
        for (Object[] o : fea) {
            String style = "x" + o[3];
            boolean gl = (boolean) o[4];
            int fx = (int) o[5];
            wing((String) o[0], (String) o[1], (Rarity) o[2], (g, a) -> Wings.feathered(g, a, style, 16, gl, fx));
        }
        Object[][] mem = {
                {"wyvern", "Wyvernflügel", E, "3A6A2A_7AC84A_0A2A0A_C8FF8A_0.9", 0xFF2A3A1A, 0, false},
                {"crimson", "Blutrote Schwingen", L, "8A0A1A_FF3A4A_2A0006_FFC0C8_0.9", 0xFF2A0A0A, 0xFFFF2A3A, true},
                {"abyss", "Abgrundflügel", L, "0A0A2A_2A2A6A_000008_6A8AFF_0.85", 0xFF101020, 0xFF4A6AFF, true},
                {"sand", "Wüstendrachen", R, "C8A060_F0D090_5A4020_FFF0C0_0.92", 0xFF5A4020, 0, false},
                {"gold_dragon", "Golddrachen", L, "B08A10_FFE04A_4A3000_FFFFC0_0.9", 0xFF6A4A0A, 0xFFFFD23F, true},
                {"ghost", "Geisterflügel", E, "C8D8FF_FFFFFF_6A7AB0_FFFFFF_0.55", 0xFFD0D8F0, 0xFFBFEFFF, true},
                {"plasma", "Plasmaflügel", L, "4A0A6A_FF4AF0_10001A_FFC0FF_0.75", 0xFF1A0A2A, 0xFFFF5AF0, true},
                {"magma", "Magmaflügel", L, "6A1000_FF8A1A_1A0400_FFE07A_0.95", 0xFF1A0A06, 0xFFFF8A1A, true},
                {"jade", "Jadedrachen", E, "1A6A5A_5AF0C8_062A20_C8FFF0_0.85", 0xFF0A2A20, 0xFF5AF0C8, true},
                {"moth", "Mottenflügel", R, "8A7A6A_D8C8B0_3A3028_F0E8D8_0.9", 0xFF4A4038, 0, false},
        };
        for (Object[] o : mem) {
            String style = "x" + o[3];
            int bone = (int) o[4], edge = (int) o[5];
            boolean gl = (boolean) o[6];
            wing((String) o[0], (String) o[1], (Rarity) o[2], (g, a) -> Wings.membrane(g, a, style, 18, bone, edge, gl));
        }
    }
}
