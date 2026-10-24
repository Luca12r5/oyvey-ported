package dev.lego.ui;

import dev.lego.ui.hub.HubView;

/** Landing menu (Right Shift): logo, MOD MENU button, quick icon buttons - like the NoRisk menu, Lego style. */
public final class MenuView extends View {
    private static final String[][] QUICK = {
            {"music", "Spotify – Songs & Playlists", "SPOTIFY"},
            {"shirt", "Kosmetik", "COSMETICS"},
            {"emote", "Emotes", "EMOTES"},
            {"gamepad", "Minispiele", "GAMES"},
            {"layout", "HUD bearbeiten", "HUD"},
            {"palette", "Design", "DESIGN"}};

    @Override public float openMs() { return 700; }
    @Override public float closeMs() { return 200; }
    @Override public float designW() { return 640; }
    @Override public float designH() { return 400; }

    @Override
    public void draw() {
        float t = open;
        boolean closing = host != null && host.closing();
        boolean seasonal = Halloween.on() || Xmas.on();
        Gx.gradientV(0, 0, W, H, Gx.rgba(0x000000, (seasonal ? 0.1f : 0.30f) * t), Gx.rgba(0x000000, (seasonal ? 0.3f : 0.55f) * t));
        int cx = W / 2, cy = H / 2 - p(46);

        // ---------------------------------------------------------------- garland / lanterns + hanging decorations
        float gl = closing ? t : Ease.outCubic(Ease.stagger(t, 0, 0, 0.7f));
        Gx.pushAlpha(gl);
        Gx.push();
        Gx.translate(0, -pf(30) * (1 - gl));
        long now = System.currentTimeMillis();
        if (Halloween.on()) {
            Halloween.lanterns(-p(10), p(4), W + p(10), P, p(16), 3);
            float[] bxF = {0.10f, 0.25f, 0.50f, 0.75f, 0.90f};
            for (int i = 0; i < bxF.length; i++) {
                float len = pf(40 + (i * 31 % 50)), sw = (float) Math.sin(now / 1300.0 + i * 1.5) * 0.06f;
                float ax = W * bxF[i], ex = ax + (float) Math.sin(sw) * len, ey = p(6) + (float) Math.cos(sw) * len;
                int seg = Math.max(1, p(5)), steps = Math.max(1, Math.round(len / seg));
                for (int k = 0; k < steps; k++) { float u = k / (float) steps; Gx.fill(Math.round(ax + (ex - ax) * u), Math.round(p(6) + (ey - p(6)) * u), Math.max(1, p(0.6)), seg, 0x442A1540); }
                Halloween.pumpkin(ex, ey + pf(10), pf(10 + i % 2 * 3));
            }
        } else {
            Xmas.lights(-p(10), p(4), W + p(10), P, p(16), 3);
            int[] bc = {Xmas.RED, Xmas.GOLD, 0xFF3BA8FF, Xmas.GREEN, 0xFFB35CFF, Xmas.RED};
            float[] bxF = {0.08f, 0.19f, 0.31f, 0.69f, 0.81f, 0.92f};
            for (int i = 0; i < bxF.length && Xmas.on(); i++) {
                float len = pf(50 + (i * 37 % 60)), sw = (float) Math.sin(now / 1100.0 + i * 1.3) * 0.08f;
                float ax = W * bxF[i], ex = ax + (float) Math.sin(sw) * len, ey = p(6) + (float) Math.cos(sw) * len;
                int seg = Math.max(1, p(5)), steps = Math.max(1, Math.round(len / seg));
                for (int k = 0; k < steps; k++) { float u = k / (float) steps; Gx.fill(Math.round(ax + (ex - ax) * u), Math.round(p(6) + (ey - p(6)) * u), Math.max(1, p(0.6)), seg, 0x66E8D9A8); }
                Xmas.bauble(ex, ey + pf(10), pf(11 + i % 2 * 3), bc[i]);
            }
        }
        Gx.pop();
        Gx.popAlpha();

        // ---------------------------------------------------------------- logo
        float a = closing ? t : Ease.outCubic(Ease.stagger(t, 0, 0, 0.55f));
        float sc = closing ? 0.94f + 0.06f * t : 0.86f + 0.14f * Ease.outBack(Ease.stagger(t, 0, 0, 0.6f));
        float pulse = 0.5f + 0.5f * (float) Math.sin(now / 900.0);
        int logoCol = Halloween.on() ? Halloween.ORANGE : Xmas.on() ? Xmas.RED : Style.accent;
        Gx.pushAlpha(a);
        Gx.glow(cx, cy, p(170), Gx.withAlpha(logoCol, 0.10f + 0.05f * pulse));
        Gx.push();
        Gx.translate(0, pf(14) * (1 - a));
        Gx.scaleAt(cx, cy, sc);
        float fs = pf(44);
        float wL = Gx.width("LEGO", fs, Fonts.BOLD), wC = Gx.width("CLIENT", fs, Fonts.LIGHT);
        int brick = p(62), gap = p(14);
        float total = wL + gap + brick + gap + wC;
        float x = cx - total / 2f;
        Gx.textMid("LEGO", x, cy, fs, Fonts.BOLD, 0xFFFFFFFF);
        x += wL + gap;
        float bob = (float) Math.sin(now / 700.0) * pf(2.2f);
        Gx.glow(Math.round(x + brick / 2f), cy, brick, Gx.withAlpha(logoCol, 0.35f));
        Gx.image(Art.brickImg(brick, logoCol), Math.round(x), Math.round(cy - brick / 2f - pf(3) + bob), brick, brick, 0xFFFFFFFF);
        if (Halloween.on()) Halloween.witchHat(x + brick / 2f + pf(3), cy - brick / 2f + pf(12) + bob, brick * 0.95f, 0);
        else Xmas.hat(x + brick / 2f + pf(3), cy - brick / 2f + pf(12) + bob, brick * 0.95f, 0);
        x += brick + gap;
        Gx.textMid("CLIENT", x, cy, fs, Fonts.LIGHT, 0xFFFFFFFF);
        Gx.pop();
        if (Halloween.on()) {
            Gx.textSpaced("HALLOWEEN  EDITION", cx, cy + pf(38), pf(8.4f), Fonts.BOLD, Halloween.ORANGE, pf(3.2f));
            Gx.textSpaced(Halloween.countdown().toUpperCase(java.util.Locale.ROOT) + "   •   v" + Env.version, cx, cy + pf(54), pf(6.6f), Fonts.MEDIUM, 0x99FFFFFF, pf(1.4f));
        } else if (Xmas.on()) {
            Gx.textSpaced("WINTER  EDITION", cx, cy + pf(38), pf(8.4f), Fonts.BOLD, Xmas.GOLD, pf(3.2f));
            Gx.textSpaced(Xmas.countdown().toUpperCase(java.util.Locale.ROOT) + "   •   v" + Env.version, cx, cy + pf(54), pf(6.6f), Fonts.MEDIUM, 0x99FFFFFF, pf(1.4f));
        } else Gx.textSpaced("v" + Env.version + "   •   MINECRAFT " + Env.mcVersion, cx, cy + pf(40), pf(7.4f), Fonts.MEDIUM, 0x99FFFFFF, pf(1.6f));
        Gx.popAlpha();

        // ---------------------------------------------------------------- MOD MENU
        float b1 = closing ? t : Ease.outCubic(Ease.stagger(t, 1, 0.09f, 0.5f));
        int bw = p(300), bh = p(40), bx = cx - bw / 2, by = cy + p(74);
        boolean hov = hover(bx, by, bw, bh);
        float hv = Ease.to("menu:mod", hov ? 1 : 0, 14);
        Gx.pushAlpha(b1);
        Gx.push();
        Gx.translate(0, pf(16) * (1 - b1));
        Gx.scaleAt(cx, by + bh / 2f, 1 + 0.025f * hv);
        Gx.shadow(bx, by, bw, bh, p(10), p(7), 0.45f);
        if (Halloween.on()) {
            if (hv > 0.01f) Gx.glow(cx, by + bh / 2, p(150), Gx.withAlpha(Halloween.ORANGE, 0.16f * hv));
            Gx.rectV(bx, by, bw, bh, p(10), Gx.mix(0xFF2A1040, 0xFF3A1855, hv), 0xFF180828);
            Gx.outline(bx, by, bw, bh, p(10), Math.max(1, p(1)), Gx.mix(0x55FF8B40, Halloween.ORANGE, hv));
            Halloween.cobweb(bx, by, bw, P * 0.85f);
            Gx.icon("skull", bx + p(28), by + bh / 2f + pf(1), pf(14), Halloween.ORANGE);
            Gx.textSpaced("MOD MENU", cx, by + bh / 2f + pf(1), pf(11.5f), Fonts.BOLD, 0xFFFFFFFF, pf(2.4f));
        } else if (Xmas.on()) {
            if (hv > 0.01f) Gx.glow(cx, by + bh / 2, p(150), Gx.withAlpha(Xmas.GOLD, 0.14f * hv));
            Gx.rectV(bx, by, bw, bh, p(10), Gx.mix(0xFFEF3A4E, 0xFFFF5A6C, hv), 0xFFA3142A);
            Gx.outline(bx, by, bw, bh, p(10), Math.max(1, p(1)), Gx.mix(0x55FFD98A, Xmas.GOLD, hv));
            Xmas.snowCap(bx, by, bw, P * 0.85f);
            Gx.icon("gift", bx + p(28), by + bh / 2f + pf(1), pf(14), 0xFFFFFFFF);
            Gx.textSpaced("MOD MENU", cx, by + bh / 2f + pf(1), pf(11.5f), Fonts.BOLD, 0xFFFFFFFF, pf(2.4f));
        } else {
            Gx.rect(bx, by, bw, bh, p(8), Gx.mix(0xD9121318, 0xE61A1C22, hv));
            Gx.outline(bx, by, bw, bh, p(8), Math.max(1, p(0.8)), Gx.mix(0x26FFFFFF, Style.accent, hv));
            if (hv > 0.01f) Gx.glow(cx, by + bh / 2, p(120), Gx.withAlpha(Style.accent, 0.10f * hv));
            Gx.textSpaced("MOD MENU", cx, by + bh / 2f, pf(11), Fonts.BOLD, Gx.mix(0xFFFFFFFF, Style.accent, hv * 0.6f), pf(2.2f));
        }
        Gx.pop();
        Gx.popAlpha();
        Kit.hit(bx, by, bw, bh).click(() -> host.open(new HubView(HubView.Section.MODS)));

        // ---------------------------------------------------------------- quick buttons (gift boxes)
        int qs = p(46), qg = p(12);
        int qx = cx - (QUICK.length * qs + (QUICK.length - 1) * qg) / 2, qy = by + bh + p(18);
        int[][] giftCol = {{0xFF1A8A4E, 0xFFF4C152}, {0xFFD62238, 0xFFF4C152}, {0xFF1A8A4E, 0xFFE3263B}, {0xFF1E3E7A, 0xFFF4C152}, {0xFFE8B23A, 0xFFB5152C}, {0xFF6B2FB0, 0xFFF4E9D0}, {0xFF0F6B6B, 0xFFF4C152}};
        String tip = null;
        int tipX = 0;
        for (int i = 0; i < QUICK.length; i++) {
            float bi = closing ? t : Ease.outBack(Ease.stagger(t, i + 2, 0.06f, 0.45f));
            int x0 = qx + i * (qs + qg);
            boolean h = hover(x0, qy - p(8), qs, qs + p(8));
            float hq = Ease.to("menu:q" + i, h ? 1 : 0, 14);
            Gx.pushAlpha(Math.min(1, Math.max(0, bi)));
            Gx.push();
            Gx.translate(0, pf(18) * (1 - bi));
            if (Halloween.on()) {
                int[][] hwCol = {{0xFF3A1855, 0xFFFF6B1A}, {0xFF2A1040, 0xFF8B2FC6}, {0xFF1A3A1A, 0xFF3AE85D}, {0xFF3A1020, 0xFFC41E1E}, {0xFF2A1040, 0xFFFFC94A}, {0xFF3A1855, 0xFFFF6B1A}};
                float wob = h ? (float) Math.sin(now / 90.0) * pf(0.8f) : 0;
                Gx.translate(wob, 0);
                Halloween.tombstone(x0, qy, qs, qs, hwCol[i][0], hwCol[i][1], pf(6) * hq, P);
                float iy = qy + qs * 0.5f - pf(6) * hq;
                Gx.circle(x0 + qs / 2, Math.round(iy), p(12), 0xD9000000);
                Gx.icon(QUICK[i][0], x0 + qs / 2f, iy, pf(14), hwCol[i][1]);
            } else if (Xmas.on()) {
                float wob = h ? (float) Math.sin(now / 90.0) * pf(0.8f) : 0;
                Gx.translate(wob, 0);
                Xmas.gift(x0, qy, qs, qs, giftCol[i][0], giftCol[i][1], pf(6) * hq, P);
                float iy = qy + qs * 0.64f - pf(6) * hq;
                Gx.circle(x0 + qs / 2, Math.round(iy), p(12), 0xE6FFFFFF);
                Gx.icon(QUICK[i][0], x0 + qs / 2f, iy, pf(14), giftCol[i][0]);
            } else {
                Gx.scaleAt(x0 + qs / 2f, qy + qs / 2f, 1 + 0.06f * hq);
                Gx.shadow(x0, qy, qs, qs, p(9), p(5), 0.35f);
                Gx.rect(x0, qy, qs, qs, p(9), Gx.mix(0xD9121318, 0xF01C1E25, hq));
                Gx.outline(x0, qy, qs, qs, p(9), Math.max(1, p(0.8)), Gx.mix(0x22FFFFFF, Style.accent, hq));
                Gx.icon(QUICK[i][0], x0 + qs / 2f, qy + qs / 2f, pf(21), Gx.mix(0xFFE8E9EC, Style.accent, hq));
            }
            Gx.pop();
            Gx.popAlpha();
            final String sec = QUICK[i][2];
            Kit.hit(x0, qy - p(8), qs, qs + p(8)).click(() -> openSection(sec));
            if (h) { tip = QUICK[i][1]; tipX = x0 + qs / 2; }
        }
        if (tip != null) Kit.tooltip(tip, tipX, qy - p(14), P);

        // ---------------------------------------------------------------- footer
        float f = closing ? t : Ease.outCubic(Ease.stagger(t, 7, 0.06f, 0.5f));
        Gx.pushAlpha(f);
        Gx.textSpaced("ESC  ODER  RECHTS-SHIFT  ZUM  SCHLIESSEN", cx, H - pf(22), pf(6.6f), Fonts.MEDIUM, 0x66FFFFFF, pf(1.2f));
        String np = Env.nowPlaying.get();
        if (np != null && !np.isEmpty()) {
            float s = pf(8);
            String txt = Fonts.ellipsize(np, Fonts.MEDIUM, s, pf(220));
            int w = Math.round(Gx.width(txt, s, Fonts.MEDIUM) + pf(34)), h = p(24);
            int x0 = p(16), y0 = H - h - p(14);
            Gx.rect(x0, y0, w, h, h / 2, 0xB3121318);
            Gx.outline(x0, y0, w, h, h / 2, Math.max(1, p(0.6)), 0x1FFFFFFF);
            Gx.circle(x0 + p(12), y0 + h / 2, p(6.5), 0xFF1ED760);
            Gx.icon("music", x0 + p(12), y0 + h / 2f, pf(7.5f), 0xFF0B0B0B);
            Gx.textMid(txt, x0 + p(24), y0 + h / 2f, s, Fonts.MEDIUM, 0xE6FFFFFF);
        }
        Gx.popAlpha();
    }

    private void openSection(String sec) {
        if ("HUD".equals(sec)) { host.closeThen(Env.openHudEditor); return; }
        if ("SPOTIFY".equals(sec)) { host.open(new SpotifyView()); return; }
        host.open(new HubView(HubView.Section.valueOf(sec)));
    }

    @Override
    public boolean key(int key, int mods) {
        if (key == 344) { host.close(); return true; }   // right shift toggles
        return false;
    }
}
