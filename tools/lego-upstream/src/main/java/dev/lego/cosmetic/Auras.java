package dev.lego.cosmetic;

import static dev.lego.cosmetic.Geo.*;

import dev.lego.cosmetic.Cos.A;
import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

/** Animated particle auras (body space: neck at y = 0, feet at y = -24). */
final class Auras {
    private Auras() { }

    static void register() {
        Cos.add("aura_fire", "Feuer-Aura", Slot.AURA, Rarity.LEGENDARY, HdAuras::fire);
        Cos.add("aura_frost", "Frost-Aura", Slot.AURA, Rarity.EPIC, HdAuras::frost);
        Cos.add("aura_hearts", "Herz-Aura", Slot.AURA, Rarity.RARE, HdAuras::hearts);
        Cos.add("aura_orbit", "Sternen-Orbit", Slot.AURA, Rarity.LEGENDARY, HdAuras::orbit);
        Cos.add("aura_storm", "Gewitter-Aura", Slot.AURA, Rarity.LEGENDARY, HdAuras::storm);
        Cos.add("aura_sakura", "Kirschblüten-Wind", Slot.AURA, Rarity.EPIC, HdAuras::sakura);
        Cos.add("aura_music", "Musik-Aura", Slot.AURA, Rarity.RARE, HdAuras::music);
        Cos.add("aura_bubbles", "Seifenblasen", Slot.AURA, Rarity.COMMON, HdAuras::bubbles);
        Cos.add("aura_rainbow", "Regenbogen-Ring", Slot.AURA, Rarity.EPIC, HdAuras::rainbowRing);
        Cos.add("aura_void", "Leere-Aura", Slot.AURA, Rarity.LEGENDARY, HdAuras::voidAura);
        Cos.add("aura_sparkle", "Glitzerstaub", Slot.AURA, Rarity.RARE, HdAuras::sparkle);
        Cos.add("aura_leaves", "Herbstlaub", Slot.AURA, Rarity.COMMON, HdAuras::leaves);
        Cos.add("aura_runes", "Runenkreis", Slot.AURA, Rarity.LEGENDARY, HdAuras::runes);
        Cos.add("aura_bricks", "Stein-Orbit", Slot.AURA, Rarity.EPIC, HdAuras::bricks);
    }

    private static float fract(float v) { return v - (float) Math.floor(v); }

    /** Soft glowing ground ring under the feet. */
    private static void groundGlow(G g, int argb, float r) {
        g.color(argb);
        g.push();
        g.translate(0, -23.9f, 0);
        g.rotX(-90);
        g.plane("orb", 0, 0, r * 2, r * 2, 0, 0, 1, 1);
        g.pop();
    }

    static void fire(G g, A a) {
        g.glow(true);
        groundGlow(g, 0x99FF7A2A, 11);
        int n = 14;
        for (int i = 0; i < n; i++) {
            float ph = fract(a.time * 1.2f + i * 0.37f);
            double ang = Math.PI * 2 * i / n + a.time * 0.5f;
            float r = 7.2f + sin(i * 1.7) * 1.2f - ph * 1.5f;
            float s = 5.2f * (1 - ph * 0.75f);
            g.color(argb((int) (255 * Math.sin(ph * Math.PI)), 0xFFFFFF));
            g.cross("flame", cos(ang) * r, -24 + ph * 13 + s / 2, sin(ang) * r, s * 0.65f, s, 0, 0, 1, 1);
        }
        for (int i = 0; i < 12; i++) {
            float ph = fract(a.time * 0.6f + i * 0.29f);
            double ang = i * 2.1 + a.time * 0.8f;
            g.color(argb((int) (255 * (1 - ph)), 0xFFFFFF));
            sprite(g, "orb", cos(ang) * (6 + ph * 3), -22 + ph * 26, sin(ang) * (6 + ph * 3), 1.2f * (1 - ph) + 0.4f);
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void frost(G g, A a) {
        g.glow(true);
        groundGlow(g, 0x887CF7FF, 11);
        int n = 12;
        for (int i = 0; i < n; i++) {
            double ang = Math.PI * 2 * i / n + a.time * 0.7f;
            float ph = fract(a.time * 0.18f + i * 0.41f);
            float y = -23 + ph * 26;
            float r = 8.5f + sin(i * 2.3) * 0.8f;
            float s = 2.6f + (i % 3) * 0.8f;
            g.push();
            g.translate(cos(ang) * r, y, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90);
            g.rotZ(a.time * 60 + i * 30);
            g.color(argb((int) (240 * Math.min(1, Math.sin(ph * Math.PI) * 1.6)), 0xFFFFFF));
            g.plane("snowflake", 0, 0, s, s, 0, 0, 1, 1);
            g.pop();
        }
        for (int i = 0; i < 16; i++) {
            double ang = i * 2.4 - a.time * 0.4;
            float tw = 0.5f + 0.5f * sin(a.time * 4 + i * 1.3);
            g.color(argb((int) (255 * tw), 0xFFFFFF));
            sprite(g, "spark", cos(ang) * (6 + i % 4), -22 + (i * 1.7f) % 24, sin(ang) * (6 + i % 4), 0.8f + tw);
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void hearts(G g, A a) {
        g.glow(true);
        int n = 8;
        for (int i = 0; i < n; i++) {
            float ph = fract(a.time * 0.3f + i / (float) n);
            double ang = Math.PI * 2 * i / n + a.time * 0.6f;
            float r = 8.5f + sin(a.time * 1.5 + i) * 0.8f;
            g.push();
            g.translate(cos(ang) * r, -22 + ph * 26, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90);
            g.rotZ(sin(a.time * 3 + i) * 12);
            float beat = 1 + 0.12f * (float) Math.max(0, Math.sin(a.time * 7 + i));
            float s = (2.8f + (i % 3) * 0.6f) * beat;
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 1.8)), 0xFFFFFF));
            g.plane("heart", 0, 0, s, s, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void orbit(G g, A a) {
        g.glow(true);
        String[] tex = {"orb_purple", "orb_cyan", "orb"};
        for (int i = 0; i < 3; i++) {
            g.push();
            g.translate(0, -9, 0);
            g.rotX(25 + i * 55);
            g.rotY(a.time * (70 + i * 25) + i * 120);
            for (int k = 0; k < 8; k++) {
                g.push();
                g.rotY(-k * 7);
                g.translate(11, 0, 0);
                g.color(argb((int) (240 * (1 - k / 8f)), 0xFFFFFF));
                float s = 3.6f * (1 - k * 0.1f);
                sprite(g, tex[i], 0, 0, 0, s);
                if (k == 0) {
                    g.color(0xFFFFFFFF);
                    sprite(g, "spark", 0, 0, 0, 2.6f);
                }
                g.pop();
            }
            g.pop();
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void storm(G g, A a) {
        g.glow(true);
        // dark swirling cloud ring
        for (int i = 0; i < 10; i++) {
            double ang = Math.PI * 2 * i / 10 + a.time * 0.6;
            g.color(0x663A4A6A);
            sprite(g, "orb_cyan", cos(ang) * 8, 4 + sin(a.time * 2 + i) * 0.6f, sin(ang) * 8, 6);
        }
        int n = 5;
        for (int i = 0; i < n; i++) {
            float cycle = fract(a.time * 1.3f + i * 0.37f);
            if (cycle > 0.3f) continue;
            double ang = Math.PI * 2 * ((i * 0.61f + Math.floor(a.time * 1.3f + i * 0.37f) * 0.29f) % 1.0);
            float r = 7 + (i % 2) * 2;
            g.color(argb((int) (255 * (1 - cycle / 0.3f)), 0xFFFFFF));
            g.cross("bolt", cos(ang) * r, -10, sin(ang) * r, 6, 26, 0, 0, 1, 1);
        }
        for (int i = 0; i < 10; i++) {
            double ang = Math.PI * 2 * i / 10 - a.time * 1.2f;
            g.color(0x99FFFFFF);
            sprite(g, "orb_cyan", cos(ang) * 8, -23.5f + sin(a.time * 3 + i) * 0.6f, sin(ang) * 8, 2.2f);
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void sakura(G g, A a) {
        int n = 18;
        for (int i = 0; i < n; i++) {
            float ph = fract(a.time * 0.22f + i * 0.137f);
            double ang = i * 2.39 + a.time * 0.9 + ph * 3;
            float r = 6 + (i % 4) * 1.4f;
            g.push();
            g.translate(cos(ang) * r, 6 - ph * 30, sin(ang) * r);
            g.rotY(a.time * 90 + i * 40);
            g.rotX(a.time * 70 + i * 25);
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 2)), 0xFFFFFF));
            g.plane("petal", 0, 0, 2.8f, 2.8f, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    static void music(G g, A a) {
        g.glow(true);
        int[] cols = {0xFF6AE0FF, 0xFFFF6AD0, 0xFFFFE24A, 0xFF8AFF6A};
        for (int i = 0; i < 9; i++) {
            float ph = fract(a.time * 0.35f + i / 9f);
            double ang = Math.PI * 2 * i / 9 + a.time * 0.5;
            float r = 8 + sin(ph * 6 + i) * 1;
            g.push();
            g.translate(cos(ang) * r, -18 + ph * 24, sin(ang) * r);
            g.rotY((float) Math.toDegrees(-ang) + 90);
            g.rotZ(sin(a.time * 5 + i) * 15);
            int c = cols[i % 4];
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 2)), c));
            g.plane("note", 0, 0, 3, 3, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void bubbles(G g, A a) {
        for (int i = 0; i < 14; i++) {
            float ph = fract(a.time * 0.2f + i * 0.173f);
            double ang = i * 2.1 + ph * 2;
            float r = 6 + (i % 3) * 2;
            float s = 2.4f + (i % 4) * 0.9f;
            float wob = 1 + 0.06f * sin(a.time * 6 + i);
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 2.5)), 0xFFFFFF));
            g.push();
            g.translate(cos(ang) * r + sin(a.time * 2 + i) * 0.8f, -23 + ph * 30, sin(ang) * r);
            g.scale(wob, 1 / wob, wob);
            sprite3(g, "bubble", 0, 0, 0, s);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    static void rainbowRing(G g, A a) {
        g.glow(true);
        int seg = 36;
        for (int ring = 0; ring < 2; ring++) {
            g.push();
            g.translate(0, ring == 0 ? -12 + sin(a.time * 1.5) * 2 : -20 + sin(a.time * 1.5 + 2) * 1.5f, 0);
            g.rotX(ring == 0 ? 8 * sin(a.time) : -6);
            float R = ring == 0 ? 10 : 8.5f;
            for (int i = 0; i < seg; i++) {
                double a0 = Math.PI * 2 * i / seg, a1 = Math.PI * 2 * (i + 1) / seg;
                g.color(hue(i / (float) seg + a.time * 0.2f * (ring == 0 ? 1 : -1), 0.65, 1));
                float h = 0.9f;
                g.quad("ring_glow", new float[]{cos(a0) * R, h, sin(a0) * R}, new float[]{cos(a1) * R, h, sin(a1) * R}, new float[]{cos(a1) * R, -h, sin(a1) * R}, new float[]{cos(a0) * R, -h, sin(a0) * R},
                        new float[]{0, 0, 1, 0, 1, 1, 0, 1});
            }
            g.pop();
        }
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8 + a.time * 1.2;
            float tw = 0.5f + 0.5f * sin(a.time * 4 + i);
            g.color(argb((int) (255 * tw), 0xFFFFFF));
            sprite(g, "spark", cos(an) * 10, -12 + sin(a.time * 1.5) * 2, sin(an) * 10, 1.4f + tw);
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void voidAura(G g, A a) {
        g.glow(true);
        groundGlow(g, 0xAA8A2AFF, 10);
        for (int i = 0; i < 16; i++) {
            float ph = fract(a.time * 0.35f + i / 16f);
            double ang = i * 2.39 - ph * 5 - a.time * 0.4;
            float r = 11 * (1 - ph) + 2;
            g.color(argb((int) (230 * Math.min(1, ph * 3) * (1 - ph * 0.6f)), 0xFFFFFF));
            sprite(g, "void_orb", cos(ang) * r, -22 + ph * 20 + sin(i) * 2, sin(ang) * r, 2.4f * (1 - ph * 0.5f));
        }
        g.push();
        g.translate(0, -23.7f, 0);
        g.rotY(-a.time * 50);
        g.color(0xCCB04AFF);
        g.ring("ring_glow", 7.5f, 9.5f, 0, 28, 0, 0, 1, 1);
        g.pop();
        g.color(0xFFFFFFFF).glow(false);
    }

    static void sparkle(G g, A a) {
        g.glow(true);
        int[] cols = {0xFFFFFFFF, 0xFFFFE8A0, 0xFFB8E8FF, 0xFFFFB8E8};
        for (int i = 0; i < 22; i++) {
            float ph = fract(a.time * 0.25f + i * 0.0913f);
            double ang = i * 2.39 + a.time * 0.3;
            float r = 5 + (i * 7 % 5);
            float tw = 0.4f + 0.6f * Math.abs(sin(a.time * 3 + i * 1.7));
            int c = cols[i % 4];
            g.color(argb((int) (255 * tw * Math.min(1, Math.sin(ph * Math.PI) * 2)), c));
            sprite(g, "spark", cos(ang) * r, 4 - ph * 28, sin(ang) * r, 0.8f + tw * 1.4f);
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void leaves(G g, A a) {
        int[] cols = {0xFFFFFFFF, 0xFFFFD0A0, 0xFFFFA0A0, 0xFFD8FF9A};
        for (int i = 0; i < 16; i++) {
            float ph = fract(a.time * 0.18f + i * 0.157f);
            double ang = i * 2.2 + a.time * 0.7 + ph * 2.5;
            float r = 7 + (i % 3) * 1.5f;
            g.push();
            g.translate(cos(ang) * r, 4 - ph * 28, sin(ang) * r);
            g.rotY(a.time * 80 + i * 50);
            g.rotZ(sin(a.time * 2 + i) * 40);
            g.color(argb((int) (255 * Math.min(1, Math.sin(ph * Math.PI) * 2.5)), cols[i % 4]));
            g.plane("leaf", 0, 0, 2.3f, 2.3f, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF);
    }

    static void runes(G g, A a) {
        g.glow(true);
        float pulse = 0.8f + 0.2f * sin(a.time * 2.5);
        g.push();
        g.translate(0, -23.8f, 0);
        g.rotY(a.time * 25);
        g.rotX(-90);
        g.color(argb((int) (255 * pulse), 0xFFFFFF));
        g.plane("rune_circle", 0, 0, 22, 22, 0, 0, 1, 1);
        g.pop();
        for (int i = 0; i < 8; i++) {
            float ph = fract(a.time * 0.3f + i / 8f);
            double an = Math.PI * 2 * i / 8 + a.time * 0.4;
            g.push();
            g.translate(cos(an) * 8.5f, -23 + ph * 16, sin(an) * 8.5f);
            g.rotY((float) Math.toDegrees(-an) + 90);
            g.color(argb((int) (255 * Math.sin(ph * Math.PI)), 0xFFFFFF));
            g.plane("glyph", 0, 0, 1.8f, 1.8f, 0, 0, 1, 1);
            g.pop();
        }
        g.color(0xFFFFFFFF).glow(false);
    }

    static void bricks(G g, A a) {
        String[] tex = {"lego_red", "lego_yellow", "lego_blue", "lego_green"};
        for (int i = 0; i < 8; i++) {
            double an = Math.PI * 2 * i / 8 + a.time * 0.8;
            float y = -10 + sin(a.time * 1.6 + i * 0.8) * 3;
            g.push();
            g.translate(cos(an) * 10, y, sin(an) * 10);
            g.rotY(a.time * 60 + i * 45);
            g.rotX(sin(a.time + i) * 25);
            String t = tex[i % 4];
            g.box(t, -1.6f, -0.7f, -0.8f, 1.6f, 0.7f, 0.8f, 0, 0, 1, 1);
            for (int s = -1; s <= 1; s += 2) {
                g.push();
                g.translate(s * 0.8f, 0.7f, 0);
                g.cylinder(t, 0.45f, 0.45f, 0, 0.45f, 8, true, 0.1f, 0.1f, 0.9f, 0.9f);
                g.pop();
            }
            g.pop();
        }
    }
}
