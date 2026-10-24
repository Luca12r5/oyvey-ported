package dev.lego.cosmetic;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** 3D models of all cosmetics (cosmetic space, see {@link G}). */
final class Models {
    private Models() { }

    static void registerAll() {
        // ---------------------------------------------------------------- capes (HD textures, most of them animated)
        Cos.cape("lego", "Lego-Umhang", Rarity.RARE);
        Cos.cape("galaxy", "Galaxie", Rarity.EPIC);
        Cos.cape("flames", "Flammen", Rarity.RARE);
        Cos.cape("ice", "Eiskristall", Rarity.RARE);
        Cos.cape("aurora", "Polarlicht", Rarity.EPIC);
        Cos.cape("sunset", "Retro-Sonnenuntergang", Rarity.RARE);
        Cos.cape("carbon", "Carbon", Rarity.COMMON);
        Cos.cape("neon", "Neon-Grid", Rarity.EPIC);
        Cos.cape("spotify", "Spotify", Rarity.LEGENDARY);
        Cos.cape("sakura", "Kirschblüte", Rarity.EPIC);
        Cos.cape("lava", "Lavastrom", Rarity.EPIC);
        Cos.cape("ocean", "Tiefsee", Rarity.RARE);
        Cos.cape("matrix", "Matrix-Code", Rarity.EPIC);
        Cos.cape("storm", "Gewitter", Rarity.EPIC);
        Cos.cape("rainbow", "Regenbogen-Seide", Rarity.RARE);
        Cos.cape("snow", "Winternacht", Rarity.RARE);
        Cos.cape("fireflies", "Glühwürmchen", Rarity.EPIC);
        Cos.cape("heart", "Herzschlag", Rarity.RARE);
        Cos.cape("enchant", "Verzaubert", Rarity.LEGENDARY);
        Cos.cape("void", "Schwarzes Loch", Rarity.LEGENDARY);
        Cos.cape("royal", "Königsmantel", Rarity.LEGENDARY);
        Cos.cape("dragon", "Drachenschuppen", Rarity.EPIC);
        Cos.cape("camo", "Tarnmuster", Rarity.COMMON);
        Cos.cape("bricks", "Bausteine", Rarity.COMMON);
        Cos.cape("racing", "Zielflagge", Rarity.COMMON);
        Cos.cape("moon", "Mondnacht", Rarity.RARE);
        // ---------------------------------------------------------------- wings
        Wings.register();
        // ---------------------------------------------------------------- hats
        Cos.add("hat_crown", "Königskrone", Slot.HAT, Rarity.LEGENDARY, HdHats::crown);
        Cos.add("hat_halo", "Heiligenschein", Slot.HAT, Rarity.EPIC, HdHats::halo);
        Cos.add("hat_tophat", "Zylinder", Slot.HAT, Rarity.RARE, HdHats::tophat);
        Cos.add("hat_santa", "Weihnachtsmütze", Slot.HAT, Rarity.RARE, HdHats::santa);
        Cos.add("hat_witch", "Hexenhut", Slot.HAT, Rarity.EPIC, HdHats::witch);
        Cos.add("hat_cat", "Katzenohren", Slot.HAT, Rarity.COMMON, HdHats::catEars);
        Cos.add("hat_bunny", "Hasenohren", Slot.HAT, Rarity.COMMON, HdHats::bunnyEars);
        Cos.add("hat_horns", "Teufelshörner", Slot.HAT, Rarity.EPIC, HdHats::horns);
        Cos.add("hat_headphones", "Kopfhörer", Slot.HAT, Rarity.RARE, HdHats::headphones);
        Models2.register();
        // ---------------------------------------------------------------- face
        Cos.add("face_sunglasses", "Sonnenbrille", Slot.FACE, Rarity.RARE, HdFace::sunglasses);
        Cos.add("face_pixel", "Pixel-Brille", Slot.FACE, Rarity.COMMON, HdFace::pixelGlasses);
        Models3.registerFace();
        // ---------------------------------------------------------------- back
        Cos.add("back_lego", "Lego-Rucksack", Slot.BACK, Rarity.EPIC, HdBack::legoBackpack);
        Cos.add("back_sword", "Kristallschwert", Slot.BACK, Rarity.RARE, HdBack::backSword);
        Models3.registerBack();
        // ---------------------------------------------------------------- auras
        Auras.register();
        // ---------------------------------------------------------------- premium 3D items
        Cos3Models.registerAll();
        // ---------------------------------------------------------------- Paket 4
        CosPack.register();
    }

    // ================================================================== capes (only drawn in previews; in game the vanilla cape shows the texture)
    static void capePreview(G g, A a) { }

    static void capeGeometry(G g, A a, String tex) {
        String t = Capes.frameName(tex, a.time);
        g.push();
        g.translate(0, 0, -2.25f);
        g.rotX(a.sneak ? 22 : 6 + a.move * 20 + (float) Math.sin(a.time * 1.3) * 1.5f);
        // 4 rows, each slightly more swung back: cloth like curve
        float W = 512, H = 256;
        float fu0 = 8 / W, fu1 = 88 / W, iu0 = 96 / W, iu1 = 176 / W;
        int rows = 4;
        float y = 0, z = 0;
        for (int r = 0; r < rows; r++) {
            float ang = (float) Math.toRadians(r * 3.0 + Math.sin(a.time * 1.7 - r * 0.8) * 1.2);
            float h = 16f / rows;
            float y1 = y - h * (float) Math.cos(ang), z1 = z - h * (float) Math.sin(ang);
            float v0 = (8 + 128f * r / rows) / H, v1 = (8 + 128f * (r + 1) / rows) / H;
            // outer face (seen from behind, un-mirrored)
            g.quad(t, new float[]{-5, y, z - 1}, new float[]{5, y, z - 1}, new float[]{5, y1, z1 - 1}, new float[]{-5, y1, z1 - 1}, new float[]{fu0, v0, fu1, v0, fu1, v1, fu0, v1});
            // inner face
            g.quad(t, new float[]{5, y, z}, new float[]{-5, y, z}, new float[]{-5, y1, z1}, new float[]{5, y1, z1}, new float[]{iu0, v0, iu1, v0, iu1, v1, iu0, v1});
            // side edges
            g.quad(t, new float[]{-5, y, z}, new float[]{-5, y, z - 1}, new float[]{-5, y1, z1 - 1}, new float[]{-5, y1, z1}, new float[]{0, v0, 8 / W, v0, 8 / W, v1, 0, v1});
            g.quad(t, new float[]{5, y, z - 1}, new float[]{5, y, z}, new float[]{5, y1, z1}, new float[]{5, y1, z1 - 1}, new float[]{88 / W, v0, 96 / W, v0, 96 / W, v1, 88 / W, v1});
            if (r == rows - 1) g.quad(t, new float[]{-5, y1, z1}, new float[]{5, y1, z1}, new float[]{5, y1, z1 - 1}, new float[]{-5, y1, z1 - 1}, new float[]{88 / W, 0, 168 / W, 0, 168 / W, 8 / H, 88 / W, 8 / H});
            y = y1;
            z = z1;
        }
        g.quad(t, new float[]{-5, 0, 0}, new float[]{5, 0, 0}, new float[]{5, 0, -1}, new float[]{-5, 0, -1}, new float[]{8 / W, 0, 88 / W, 0, 88 / W, 8 / H, 8 / W, 8 / H});
        g.pop();
    }

    // ================================================================== hats (head space: top of head at y = 8)
    static void crown(G g, A a) {
        g.push();
        g.translate(0, 8.05f, 0);
        g.rotZ(-6);
        g.cylinder("gold", 4.55f, 4.55f, 0, 2.3f, 20, false, 0, 0, 1, 1);
        g.cylinder("gold", 4.35f, 4.35f, 0, 2.3f, 20, false, 0, 0, 1, 1);
        g.ring("gold", 4.35f, 4.55f, 2.3f, 20, 0, 0, 1, 1);
        for (int i = 0; i < 8; i++) {
            g.push();
            g.rotY(i * 45 + 22.5f);
            g.translate(0, 2.3f, 4.45f);
            float hh = i % 2 == 0 ? 3.2f : 2.3f;
            g.quad("gold", new float[]{-1.4f, 0, 0}, new float[]{1.4f, 0, 0}, new float[]{0.05f, hh, 0}, new float[]{-0.05f, hh, 0}, new float[]{0, 1, 1, 1, 0.5f, 0, 0.5f, 0});
            g.push();
            g.translate(0, hh + 0.35f, 0);
            g.sphere("gems", 0.55f, 8, 5, (i % 4) * 0.25f, 0, (i % 4) * 0.25f + 0.25f, 1);
            g.pop();
            g.pop();
        }
        for (int i = 0; i < 4; i++) {
            g.push();
            g.rotY(i * 90);
            g.translate(0, 1.15f, 4.62f);
            g.box("gems", -0.6f, -0.6f, -0.2f, 0.6f, 0.6f, 0.25f, (i % 4) * 0.25f, 0, (i % 4) * 0.25f + 0.25f, 1);
            g.pop();
        }
        g.pop();
    }

    static void halo(G g, A a) {
        float bob = (float) Math.sin(a.time * 2.2f) * 0.45f;
        g.push();
        g.translate(0, 11.4f + bob, 0);
        g.rotX(-10);
        g.rotY(a.time * 40);
        g.glow(true);
        g.torus("halo", 4.5f, 0.5f, 28, 8, 0, 0, 1, 1);
        float pulse = 0.75f + 0.25f * (float) Math.sin(a.time * 3);
        g.color(((int) (110 * pulse) << 24) | 0xFFE9A8);
        g.torus("orb", 4.5f, 1.4f, 20, 6, 0.2f, 0.2f, 0.8f, 0.8f);
        for (int i = 0; i < 5; i++) {
            double an = Math.PI * 2 * i / 5;
            float tw = 0.5f + 0.5f * (float) Math.sin(a.time * 4 + i * 1.9);
            g.color(((int) (255 * tw) << 24) | 0xFFFFFF);
            Geo.sprite(g, "spark", (float) Math.cos(an) * 4.5f, 0.3f, (float) Math.sin(an) * 4.5f, 1 + tw * 1.4f);
        }
        g.color(0xFFFFFFFF).glow(false);
        g.pop();
    }

    static void tophat(G g, A a) {
        g.push();
        g.translate(0, 7.95f, 0);
        g.rotZ(-4);
        g.cylinder("felt_black", 6.9f, 6.9f, 0, 0.55f, 24, true, 0, 0, 1, 1);
        g.cylinder("felt_black", 4.15f, 4.3f, 0.55f, 8.2f, 24, true, 0, 0, 1, 1);
        g.cylinder("band_red", 4.25f, 4.33f, 0.6f, 2.2f, 24, false, 0, 0, 1, 1);
        g.pop();
    }

    static void santa(G g, A a) {
        g.push();
        g.translate(0, 7.4f, 0);
        g.cylinder("felt_red", 4.35f, 3.1f, 1.4f, 6.2f, 20, false, 0, 0, 1, 1);
        g.push();
        g.translate(0, 6.2f, 0);
        float sway = (float) Math.sin(a.time * 1.6f) * 5;
        g.rotZ(-55 + sway);
        g.rotX(-10);
        g.cylinder("felt_red", 3.1f, 0.5f, 0, 5.5f, 16, false, 0, 0, 1, 1);
        g.translate(0, 5.6f, 0);
        g.sphere("fur_white", 1.35f, 12, 8, 0, 0, 1, 1);
        g.pop();
        g.cylinder("fur_white", 4.75f, 4.75f, 0, 1.9f, 24, true, 0, 0, 1, 1);
        g.pop();
    }

    static void witch(G g, A a) {
        g.push();
        g.translate(0, 7.8f, 0);
        g.rotZ(-6);
        g.cylinder("felt_purple", 8.2f, 8.2f, 0, 0.45f, 28, true, 0, 0, 1, 1);
        g.cylinder("felt_purple", 4.3f, 3.1f, 0.45f, 5.2f, 22, false, 0, 0, 1, 1);
        g.cylinder("band_green", 4.28f, 4.0f, 0.5f, 1.8f, 22, false, 0, 0, 1, 1);
        g.push();
        g.translate(0, 1.15f, 4.2f);
        g.box("gold", -1.1f, -0.8f, 0, 1.1f, 0.8f, 0.35f, 0, 0, 1, 1);
        g.pop();
        g.translate(0, 5.2f, 0);
        g.rotZ(-18);
        g.rotX(8);
        g.cylinder("felt_purple", 3.1f, 1.5f, 0, 4.2f, 18, false, 0, 0, 1, 1);
        g.translate(0, 4.2f, 0);
        g.rotZ(-35);
        g.cylinder("felt_purple", 1.5f, 0.1f, 0, 3.6f, 14, false, 0, 0, 1, 1);
        g.pop();
    }

    static void catEars(G g, A a) {
        float twitch = (float) Math.max(0, Math.sin(a.time * 3.1f) - 0.85f) * 60;
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.4f, 7.6f, 0.4f);
            g.rotZ(-14 - (s > 0 ? twitch : 0));
            g.plane("cat_ear", 0, 2.4f, 3.8f, 4.2f, 0, 0, 1, 1);
            g.pop();
        }
    }

    static void bunnyEars(G g, A a) {
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(1.8f, 7.8f, 0);
            float wob = (float) Math.sin(a.time * 2.4f + s) * 6 + a.move * 12;
            g.rotX(-8 - wob);
            g.rotZ(-10);
            g.plane("bunny_ear", 0, 4.6f, 3, 9.2f, 0, 0, 1, 1);
            g.pop();
        }
    }

    static void horns(G g, A a) {
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.6f, 7.6f, 1.2f);
            g.rotZ(-35);
            float r = 1.2f;
            for (int i = 0; i < 6; i++) {
                float r2 = r * 0.78f;
                g.cylinder("horn", r, r2, 0, 1.4f, 10, i == 5, 0, 1 - i / 6f, 1, 1 - (i + 1) / 6f);
                g.translate(0, 1.35f, 0);
                g.rotZ(14);
                g.rotX(-4);
                r = r2;
            }
            g.pop();
        }
    }

    static void headphones(G g, A a) {
        g.push();
        // band over the head
        int seg = 12;
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * i / seg, a1 = Math.PI * (i + 1) / seg;
            float R = 5.3f;
            float x0 = (float) Math.cos(a0) * R, y0 = 3.8f + (float) Math.sin(a0) * R * 0.95f;
            float x1 = (float) Math.cos(a1) * R, y1 = 3.8f + (float) Math.sin(a1) * R * 0.95f;
            float nx0 = (float) Math.cos(a0) * 0.8f, ny0 = (float) Math.sin(a0) * 0.8f;
            float nx1 = (float) Math.cos(a1) * 0.8f, ny1 = (float) Math.sin(a1) * 0.8f;
            float z0 = -0.9f, z1 = 0.9f;
            g.quad("headphone", new float[]{x0 + nx0, y0 + ny0, z0}, new float[]{x0 + nx0, y0 + ny0, z1}, new float[]{x1 + nx1, y1 + ny1, z1}, new float[]{x1 + nx1, y1 + ny1, z0}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.quad("headphone", new float[]{x0, y0, z1}, new float[]{x0, y0, z0}, new float[]{x1, y1, z0}, new float[]{x1, y1, z1}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.quad("headphone", new float[]{x0, y0, z1}, new float[]{x0 + nx0, y0 + ny0, z1}, new float[]{x1 + nx1, y1 + ny1, z1}, new float[]{x1, y1, z1}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.quad("headphone", new float[]{x0 + nx0, y0 + ny0, z0}, new float[]{x0, y0, z0}, new float[]{x1, y1, z0}, new float[]{x1 + nx1, y1 + ny1, z0}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.translate(s * 4.2f, 3.4f, 0);
            g.rotZ(s * -90);
            g.cylinder("headphone", 2.5f, 2.5f, 0, 1.8f, 18, true, 0, 0, 1, 1);
            g.translate(0, 1.82f, 0);
            g.cylinder("cushion", 2.2f, 2.2f, 0, 0.05f, 18, true, 0, 0, 1, 1);
            g.pop();
        }
        g.pop();
    }

    // ================================================================== face
    static void sunglasses(G g, A a) {
        g.push();
        g.translate(0, 3.6f, 4.12f);
        g.plane("sunglasses", 0, 0, 9, 2.25f, 0, 0, 1, 1);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.translate(s * 4.3f, 0.4f, 0);
            g.box("felt_black", -0.18f, -0.25f, -5.5f, 0.18f, 0.25f, 0, 0, 0, 1, 1);
            g.pop();
        }
        g.pop();
    }

    static void pixelGlasses(G g, A a) {
        g.push();
        g.translate(0, 3.5f, 4.1f);
        g.plane("pixelglasses", 0, 0, 8, 2, 0, 0, 1, 1);
        g.pop();
    }

    // ================================================================== back (body space: back at z = -2)
    static void legoBackpack(G g, A a) {
        g.push();
        g.translate(0, -1.4f, -2.1f);
        float[][] f = {{0, 0, 1, 1}, {0, 0, 1, 1}, {0, 0, 1, 1}, {0, 0, 1, 1}, {0, 0, 1, 1}, {0, 0, 1, 1}};
        g.box6("lego_red", -3.2f, -8.2f, -3.4f, 3.2f, 0, 0, f);
        for (int ix = 0; ix < 2; ix++)
            for (int iz = 0; iz < 2; iz++) {
                g.push();
                g.translate(-1.5f + ix * 3, 0, -0.85f - iz * 1.7f);
                g.cylinder("lego_red", 0.95f, 0.95f, 0, 0.9f, 14, true, 0.1f, 0.1f, 0.9f, 0.9f);
                g.pop();
            }
        g.box("lego_yellow", -2.2f, -6.6f, -3.55f, 2.2f, -4.4f, -3.3f, 0, 0, 1, 1);
        // straps
        for (int s = 1; s >= -1; s -= 2) {
            g.box("strap", s * 2.1f - 0.55f, -6f, 0, s * 2.1f + 0.55f, 0.3f, 0.35f, 0, 0, 1, 1);
            g.box("strap", s * 2.1f - 0.55f, 0, 0, s * 2.1f + 0.55f, 0.35f, 4.4f, 0, 0, 1, 1);
            g.box("strap", s * 2.1f - 0.55f, -5.5f, 4.1f, s * 2.1f + 0.55f, 0.35f, 4.45f, 0, 0, 1, 1);
        }
        g.pop();
    }

    static void backSword(G g, A a) {
        g.push();
        g.translate(0, -5.5f, -2.6f);
        g.rotZ(-40);
        // blade (diamond cross section: two angled boxes)
        float L = 14, w = 1.3f;
        g.glow(false);
        g.quad("sword_blade", new float[]{-w, 2, -0.1f}, new float[]{w, 2, -0.1f}, new float[]{w, 2 + L, -0.1f}, new float[]{-w, 2 + L, -0.1f}, new float[]{0, 1, 1, 1, 1, 0, 0, 0});
        g.quad("sword_blade", new float[]{-w, 2 + L, -0.1f}, new float[]{w, 2 + L, -0.1f}, new float[]{0, 4 + L, -0.1f}, new float[]{0, 4 + L, -0.1f}, new float[]{0, 0, 1, 0, 0.5f, 0, 0.5f, 0});
        g.quad("sword_blade", new float[]{-w, 2, -0.5f}, new float[]{w, 2, -0.5f}, new float[]{w, 2 + L, -0.5f}, new float[]{-w, 2 + L, -0.5f}, new float[]{0, 1, 1, 1, 1, 0, 0, 0});
        g.quad("sword_blade", new float[]{-w, 2 + L, -0.5f}, new float[]{w, 2 + L, -0.5f}, new float[]{0, 4 + L, -0.5f}, new float[]{0, 4 + L, -0.5f}, new float[]{0, 0, 1, 0, 0.5f, 0, 0.5f, 0});
        g.box("sword_blade", -w, 2, -0.5f, w, 2 + L, -0.1f, 0.4f, 0.1f, 0.6f, 0.9f);
        // glow rune shimmer
        g.glow(true);
        float sh = (a.time * 0.6f) % 1f;
        g.color(0xAA9AF6FF);
        g.box("orb", -w * 0.5f, 2 + L * sh, -0.55f, w * 0.5f, 2 + L * sh + 2, -0.05f, 0, 0, 1, 1);
        g.glow(false);
        // guard, grip, pommel
        g.color(0xFF5A3AA0);
        g.box("steel", -3.2f, 1.1f, -0.8f, 3.2f, 2.1f, 0.2f, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.box("leather_dark", -0.5f, -2.8f, -0.6f, 0.5f, 1.1f, 0, 0, 0, 1, 1);
        g.color(0xFFFFE08A);
        g.push();
        g.translate(0, -3.4f, -0.3f);
        g.sphere("gold", 0.85f, 10, 6, 0, 0, 1, 1);
        g.pop();
        g.push();
        g.translate(0, 1.6f, 0.25f);
        g.color(0xFFFFFFFF);
        g.sphere("gems", 0.5f, 8, 5, 0.25f, 0, 0.5f, 1);
        g.pop();
        g.color(0xFFFFFFFF);
        g.pop();
    }
}
