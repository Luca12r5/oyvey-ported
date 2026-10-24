package dev.lego.cosmetic;

import static dev.lego.cosmetic.Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** Additional hats (head space: head box x/z -4..4, y 0..8, face at z = +4). */
final class Models2 {
    private Models2() { }

    static void register() {
        Cos.add("hat_fox", "Fuchsohren", Slot.HAT, Rarity.COMMON, HdHats::foxEars);
        Cos.add("hat_cowboy", "Cowboyhut", Slot.HAT, Rarity.RARE, HdHats::cowboy);
        Cos.add("hat_beanie", "Bommelmütze", Slot.HAT, Rarity.COMMON, HdHats::beanie);
        Cos.add("hat_flowers", "Blumenkranz", Slot.HAT, Rarity.RARE, HdHats::flowerCrown);
        Cos.add("hat_viking", "Wikingerhelm", Slot.HAT, Rarity.EPIC, HdHats::viking);
        Cos.add("hat_propeller", "Propellermütze", Slot.HAT, Rarity.RARE, HdHats::propeller);
        Cos.add("hat_chef", "Kochmütze", Slot.HAT, Rarity.COMMON, HdHats::chef);
        Cos.add("hat_party", "Partyhut", Slot.HAT, Rarity.COMMON, HdHats::party);
        Cos.add("hat_wizard", "Zaubererhut", Slot.HAT, Rarity.LEGENDARY, HdHats::wizard);
        Cos.add("hat_alien", "Alien-Antennen", Slot.HAT, Rarity.RARE, HdHats::alien);
        Cos.add("hat_bow", "Große Schleife", Slot.HAT, Rarity.COMMON, HdHats::bow);
        Cos.add("hat_mushroom", "Pilzhut", Slot.HAT, Rarity.EPIC, HdHats::mushroom);
    }

    static void foxEars(G g, A a) {
        float twitch = (float) Math.max(0, Math.sin(a.time * 2.7f) - 0.8f) * 70;
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.scale(s, 1, 1);
            g.translate(2.3f, 7.7f, 0.2f);
            g.rotZ(-16 - (s > 0 ? twitch : 0));
            g.rotX(-8);
            float w = 4.4f, h = 5.4f;
            // two halves angled like a real ear
            g.push();
            g.rotY(18);
            g.quad("fox_ear", new float[]{-w / 2, 0, 0}, new float[]{0, 0, 0}, new float[]{0, h, 0}, new float[]{-w / 2, h, 0}, new float[]{0, 1, 0.5f, 1, 0.5f, 0, 0, 0});
            g.pop();
            g.push();
            g.rotY(-18);
            g.quad("fox_ear", new float[]{0, 0, 0}, new float[]{w / 2, 0, 0}, new float[]{w / 2, h, 0}, new float[]{0, h, 0}, new float[]{0.5f, 1, 1, 1, 1, 0, 0.5f, 0});
            g.pop();
            g.pop();
        }
    }

    static void cowboy(G g, A a) {
        g.push();
        g.translate(0, 7.3f, 0);
        g.rotZ(-4);
        g.color(0xFFC89A6A);
        int seg = 28;
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float c0 = cos(a0), s0 = sin(a0), c1 = cos(a1), s1 = sin(a1);
            float ri = 4.6f, ro = 8.4f;
            float y0 = 2.2f * c0 * c0 - 0.4f * s0, y1 = 2.2f * c1 * c1 - 0.4f * s1;
            g.quad("leather", new float[]{c0 * ro, y0, s0 * ro * 1.08f}, new float[]{c1 * ro, y1, s1 * ro * 1.08f}, new float[]{c1 * ri, 0, s1 * ri}, new float[]{c0 * ri, 0, s0 * ri},
                    new float[]{i / (float) seg, 0, (i + 1f) / seg, 0, (i + 1f) / seg, 1, i / (float) seg, 1});
        }
        sqTube(g, "leather", 4.7f, 4.2f, 0, 5.2f, 5, 20, 0, 0, 1, 1);
        sqDome(g, "leather", 4.2f, 0.9f, 5.2f, 5, 20, 2, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.box("leather_dark", -0.4f, 5.6f, -3.2f, 0.4f, 6.2f, 3.2f, 0, 0, 1, 1);
        sqTube(g, "leather_dark", 4.78f, 4.68f, 0.3f, 1.4f, 5, 20, 0, 0, 1, 1);
        g.push();
        g.translate(0, 0.85f, 4.75f);
        g.color(0xFFFFE08A);
        g.box("gold", -0.8f, -0.6f, 0, 0.8f, 0.6f, 0.3f, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void beanie(G g, A a) {
        g.push();
        g.translate(0, 5.2f, 0);
        g.color(0xFF3A6AE0);
        sqDome(g, "knit", 4.75f, 4.4f, 1.8f, 5, 22, 5, 0, 0, 1, 1);
        sqTube(g, "knit", 4.75f, 4.75f, 0, 1.8f, 5, 22, 0, 0, 1, 1);
        g.color(0xFFF4F4F4);
        sqTube(g, "knit", 4.95f, 4.95f, -0.2f, 1.9f, 5, 22, 0, 0, 4, 1);
        float wob = sin(a.time * 3) * 6 + a.move * 10;
        g.translate(0, 6.1f, 0);
        g.rotZ(wob);
        g.rotX(sin(a.time * 2.2) * 5);
        g.color(0xFFFFFFFF);
        g.translate(0, 0.9f, 0);
        g.sphere("fur_white", 1.8f, 12, 8, 0, 0, 1, 1);
        g.pop();
    }

    static void flowerCrown(G g, A a) {
        g.push();
        g.translate(0, 8.15f, 0);
        g.torus("vine", 4.95f, 0.42f, 24, 5, 0, 0, 4, 1);
        String[] tex = {"flower_pink", "flower_white", "flower_yellow", "flower_blue"};
        int n = 12;
        for (int i = 0; i < n; i++) {
            double an = Math.PI * 2 * i / n;
            g.push();
            g.translate(cos(an) * 5.0f, 0.35f + 0.2f * sin(i * 1.7), sin(an) * 5.0f);
            g.rotY((float) -Math.toDegrees(an) + 90);
            g.rotX(-25 + sin(a.time * 2 + i) * 6);
            g.rotZ(a.time * 10 + i * 40);
            float s = 2.2f + 0.5f * ((i * 7) % 3) / 2f;
            g.plane(tex[i % 4], 0, 0, s, s, 0, 0, 1, 1);
            g.pop();
        }
        g.pop();
    }

    static void viking(G g, A a) {
        g.push();
        g.translate(0, 6.2f, 0);
        g.color(0xFFD8DCE4);
        sqDome(g, "iron", 4.8f, 4.2f, 0.6f, 5, 22, 5, 0, 0, 1, 1);
        g.color(0xFFE8B45A);
        sqTube(g, "steel", 4.9f, 4.9f, -0.4f, 1.1f, 5, 22, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        for (int i = 0; i < 12; i++) {
            float[] p = sq(Math.PI * 2 * i / 12, 4.95f, 5);
            g.push();
            g.translate(p[0], 0.35f, p[1]);
            g.sphere("steel", 0.28f, 5, 3, 0, 0, 1, 1);
            g.pop();
        }
        // nose guard
        g.color(0xFFE8B45A);
        g.box("steel", -0.5f, -2.4f, 4.6f, 0.5f, 0.4f, 5.05f, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        for (int s = 1; s >= -1; s -= 2) {
            float[][] pts = new float[7][];
            float[] rad = new float[7];
            for (int i = 0; i < 7; i++) {
                float t = i / 6f;
                pts[i] = new float[]{s * (4.4f + t * 4.2f), 1.8f + t * t * 5.5f - t * 0.6f, 0.3f - t * 1.4f};
                rad[i] = 1.25f * (1 - t) + 0.08f;
            }
            chain(g, "ivory", pts, rad, 8);
        }
        g.pop();
    }

    static void propeller(G g, A a) {
        g.push();
        g.translate(0, 6.3f, 0);
        sqDome(g, "cap_segments", 4.85f, 3.4f, 0, 5, 24, 5, 0, 0, 1, 1);
        // visor
        g.color(0xFF1E5AC8);
        int seg = 10;
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * (0.15 + 0.7 * i / seg), a1 = Math.PI * (0.15 + 0.7 * (i + 1) / seg);
            g.quad("plastic", new float[]{cos(a0) * 4.9f, 0.1f, sin(a0) * 4.9f}, new float[]{cos(a1) * 4.9f, 0.1f, sin(a1) * 4.9f},
                    new float[]{cos(a1) * 8.3f, -0.5f, sin(a1) * 8.3f}, new float[]{cos(a0) * 8.3f, -0.5f, sin(a0) * 8.3f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        g.color(0xFFFFCD03);
        g.translate(0, 3.35f, 0);
        g.cylinder("plastic", 0.35f, 0.35f, 0, 1.6f, 8, true, 0, 0, 1, 1);
        g.translate(0, 1.6f, 0);
        g.sphere("gloss", 0.7f, 8, 5, 0, 0, 1, 1);
        g.rotY(a.time * (a.preview ? 260 : 720 + a.move * 900));
        int[] cols = {0xFFE3000B, 0xFF006CB7, 0xFF00A03A};
        for (int b = 0; b < 3; b++) {
            g.push();
            g.rotY(b * 120);
            g.rotX(14);
            g.color(cols[b]);
            g.quad("plastic", new float[]{0.4f, 0, -0.9f}, new float[]{5.4f, 0, -1.2f}, new float[]{5.4f, 0, 1.2f}, new float[]{0.4f, 0, 0.9f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.pop();
        }
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void chef(G g, A a) {
        g.push();
        g.translate(0, 6.6f, 0);
        sqTube(g, "cloth", 4.8f, 4.9f, 0, 5.2f, 5, 22, 0, 0, 1, 1);
        g.color(0xFFE8E8EC);
        sqTube(g, "cloth", 4.95f, 4.95f, -0.1f, 1.3f, 5, 22, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        float puff = 1 + sin(a.time * 1.5) * 0.03f;
        for (int i = 0; i < 6; i++) {
            double an = Math.PI * 2 * i / 6 + 0.3;
            ellipsoid(g, "cloth", cos(an) * 2.9f, 6.2f, sin(an) * 2.9f, 2.9f * puff, 2.5f * puff, 2.9f * puff, 12, 8);
        }
        ellipsoid(g, "cloth", 0, 7.2f, 0, 3.2f * puff, 2.6f * puff, 3.2f * puff, 12, 8);
        g.pop();
    }

    static void party(G g, A a) {
        g.push();
        g.translate(1.2f, 7.9f, 0);
        g.rotZ(-14 + sin(a.time * 2.2) * 4);
        g.cylinder("party_cone", 3.3f, 0.12f, 0, 9, 18, false, 0, 1, 1, 0);
        g.color(0xFFFFE24A);
        g.ring("cloth", 3.0f, 3.5f, 0.1f, 18, 0, 0, 1, 1);
        g.torus("fur_white", 3.35f, 0.45f, 18, 5, 0, 0, 1, 1);
        g.translate(0, 9.1f, 0);
        g.color(0xFFFF6AB0);
        g.sphere("fur_white", 1.3f, 10, 7, 0, 0, 1, 1);
        g.pop();
        // confetti
        int[] cols = {0xFFFF3A8A, 0xFF3AC8FF, 0xFFFFE24A, 0xFF6AFF7A, 0xFFB06AFF};
        for (int i = 0; i < 12; i++) {
            float ph = (a.time * 0.4f + i / 12f) % 1f;
            double an = i * 2.4 + a.time * 0.5;
            g.push();
            g.translate(cos(an) * (4 + i % 3), 20 - ph * 16, sin(an) * (4 + i % 3));
            g.rotY(a.time * 200 + i * 40);
            g.rotX(a.time * 150 + i * 70);
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 2)), cols[i % 5]));
            g.plane("plastic", 0, 0, 0.8f, 0.5f, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    static void wizard(G g, A a) {
        g.push();
        g.translate(0, 7.8f, 0);
        g.rotZ(-5);
        g.color(0xFF2A1E8A);
        int seg = 28;
        for (int i = 0; i < seg; i++) {
            double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
            float d0 = -0.5f * (1 + sin(a0 * 3 + 1)) * 0.5f, d1 = -0.5f * (1 + sin(a1 * 3 + 1)) * 0.5f;
            g.quad("felt_purple", new float[]{cos(a0) * 9, d0, sin(a0) * 9}, new float[]{cos(a1) * 9, d1, sin(a1) * 9}, new float[]{cos(a1) * 4.3f, 0.2f, sin(a1) * 4.3f}, new float[]{cos(a0) * 4.3f, 0.2f, sin(a0) * 4.3f},
                    new float[]{0, 0, 1, 0, 1, 1, 0, 1});
        }
        g.color(0xFFFFFFFF);
        float r = 4.5f, y = 0.2f;
        float sway = sin(a.time * 1.4) * 4;
        for (int i = 0; i < 5; i++) {
            float r2 = r * (i < 4 ? 0.74f : 0.05f);
            float h = 3.0f - i * 0.25f;
            g.cylinder("wizard_cone", r, r2, 0, h, 18, false, 0, 1 - i / 5f, 1, 1 - (i + 1) / 5f);
            g.translate(0, h, 0);
            g.rotZ(-6 - i * 5 + sway * 0.3f);
            r = r2;
        }
        g.glow(true);
        float tw = 0.6f + 0.4f * sin(a.time * 5);
        g.color(argb((int) (255 * tw), 0xFFFFFF));
        sprite(g, "spark", 0, 0.4f, 0, 2.4f + tw);
        g.pop();
        g.push();
        g.translate(0, 8.4f, 0);
        g.color(0xFFFFE27A);
        g.cylinder("gold", 4.46f, 4.3f, 0, 1.3f, 20, false, 0, 0, 1, 1);
        for (int i = 0; i < 6; i++) {
            float ph = (a.time * 0.35f + i / 6f) % 1f;
            double an = i * 1.05 + a.time * 0.9;
            g.color(argb((int) (255 * Math.sin(ph * Math.PI)), 0xFFE8A0));
            sprite(g, "spark", cos(an) * (6 + ph * 2), 2 + ph * 9, sin(an) * (6 + ph * 2), 1.4f);
        }
        g.color(0xFFFFFFFF).glow(false);
        g.pop();
    }

    static void alien(G g, A a) {
        for (int s = 1; s >= -1; s -= 2) {
            float[][] pts = new float[6][];
            float[] rad = new float[6];
            for (int i = 0; i < 6; i++) {
                float t = i / 5f;
                float sw = sin(a.time * 3.2 + s + t * 1.5) * 1.4f * t * t + a.move * t * t * 1.5f;
                pts[i] = new float[]{s * (1.8f + t * 1.8f) + sw * 0.5f, 7.9f + t * 7, sw * 0.8f - t * t * 0.8f};
                rad[i] = 0.32f - t * 0.12f;
            }
            g.color(0xFF4ADE5A);
            chain(g, "plastic", pts, rad, 6);
            float[] tip = pts[5];
            float pulse = 0.8f + 0.2f * sin(a.time * 4 + s);
            g.push();
            g.translate(tip[0], tip[1] + 0.9f, tip[2]);
            g.color(0xFF7CFF6A);
            g.sphere("gloss", 1.1f, 10, 7, 0, 0, 1, 1);
            g.glow(true);
            g.color(argb((int) (200 * pulse), 0xFFFFFF));
            sprite(g, "orb", 0, 0, 0, 4.2f * pulse);
            g.glow(false);
            g.pop();
        }
        g.color(0xFF4ADE5A);
        g.box("plastic", -2.4f, 7.9f, -0.6f, 2.4f, 8.4f, 0.6f, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
    }

    static void bow(G g, A a) {
        g.push();
        g.translate(1.6f, 8.6f, 1.2f);
        g.rotZ(-14 + sin(a.time * 2) * 2);
        g.rotY(-10);
        g.color(0xFFFF5A9A);
        for (int s = 1; s >= -1; s -= 2) {
            g.push();
            g.translate(s * 2.2f, 0.3f, 0);
            g.rotZ(s * 18);
            g.scale(2.5f, 1.7f, 0.9f);
            g.sphere("satin", 1, 14, 9, 0, 0, 1, 1);
            g.pop();
            g.push();
            g.translate(s * 0.8f, -0.6f, 0.2f);
            g.rotZ(s * 25 + sin(a.time * 2.5 + s) * 5);
            g.quad("satin", new float[]{-0.7f, 0, 0}, new float[]{0.7f, 0, 0}, new float[]{0.9f, -3.2f, 0.4f}, new float[]{-0.4f, -3.0f, 0.4f}, new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            g.pop();
        }
        g.color(0xFFE0407A);
        g.sphere("satin", 0.95f, 10, 7, 0, 0, 1, 1);
        g.color(0xFFFFFFFF);
        g.pop();
    }

    static void mushroom(G g, A a) {
        g.push();
        float bob = sin(a.time * 2) * 0.15f;
        g.translate(0, 7.4f + bob, 0);
        g.rotZ(sin(a.time * 1.3) * 2);
        sqDome(g, "mushroom_cap", 6.4f, 4.6f, 0.6f, 2.6f, 26, 6, 0, 0, 1, 1);
        sqTube(g, "mushroom_cap", 6.4f, 6.4f, 0.1f, 0.6f, 2.6f, 26, 0, 0.9f, 1, 1);
        g.ring("gill", 4.2f, 6.4f, 0.1f, 26, 0, 0, 1, 1);
        g.pop();
    }
}
