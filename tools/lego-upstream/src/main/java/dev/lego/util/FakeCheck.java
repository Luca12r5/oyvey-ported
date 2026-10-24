package dev.lego.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.visual.R3;
import net.minecraft.class_1657;
import net.minecraft.class_746;

/**
 * Fake-Check: compares online player names against a watch list using Levenshtein distance.
 * Players with suspiciously similar names get a red "FAKE" label above their head.
 */
public final class FakeCheck extends Module {

    final Setting.Text names = add(new Setting.Text("Namen", "xJoru,LetsHugo,BastiGHG,PapaPlatte,xyzoom", 500, "Kommagetrennt"));
    final Setting.Num threshold = add(new Setting.Num("Toleranz", 1, 4, 1, 2, " Zeichen"));
    final Setting.Action manage = add(new Setting.Action("Verwalten", "Verwalten", () -> LegoScreen.show(new FakeCheckView())));

    private final List<String> watchList = new ArrayList<>();
    private long lastParse = 0;

    public FakeCheck() {
        super("fakecheck", "Fake-Check", "Erkennt gefälschte Spielernamen und zeigt FAKE an", Category.UTILITY);
        icon("shield");
    }

    private void parseNames() {
        long now = System.currentTimeMillis();
        if (now - lastParse < 2000 && !watchList.isEmpty()) return;
        lastParse = now;
        watchList.clear();
        String raw = names.get();
        if (raw == null || raw.isEmpty()) return;
        for (String n : raw.split(",")) {
            String t = n.trim();
            if (!t.isEmpty()) watchList.add(t.toLowerCase(Locale.ROOT));
        }
    }

    public boolean isFake(String name) {
        if (!enabled || name == null || name.isEmpty()) return false;
        parseNames();
        String low = name.toLowerCase(Locale.ROOT);
        int thr = threshold.getI();
        for (String w : watchList) {
            if (w.equals(low)) continue;
            int d = levenshtein(low, w);
            if (d > 0 && d <= thr) return true;
            if (low.length() == w.length() && visualDist(low, w) <= thr) return true;
        }
        return false;
    }

    public void renderWorld(float td) {
        if (!enabled) return;
        class_746 me = Mc.player();
        if (me == null) return;
        parseNames();
        for (class_1657 p : Mc.players()) {
            if (p == me) continue;
            String name = Mc.name(p);
            if (!isFake(name)) continue;
            double[] pos = Mc.lerpPos(p, td);
            double x = pos[0], y = pos[1] + Mc.height(p) + 0.7, z = pos[2];
            double dx = x - R3.camX(), dy = y - R3.camY(), dz = z - R3.camZ();
            if (dx * dx + dy * dy + dz * dz > 64 * 64) continue;
            R3.billboard(fakeTexture(), x, y, z, 0.6, 0xFFFF2222);
        }
    }

    private static net.minecraft.class_2960 fakeTexId;
    private static net.minecraft.class_2960 fakeTexture() {
        if (fakeTexId != null) return fakeTexId;
        fakeTexId = net.minecraft.class_2960.method_60655("legoclient", "fake_label");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(128, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new java.awt.Color(255, 30, 30, 220));
        g.fillRoundRect(0, 0, 128, 32, 8, 8);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        g.setColor(java.awt.Color.WHITE);
        java.awt.FontMetrics fm = g.getFontMetrics();
        g.drawString("FAKE", (128 - fm.stringWidth("FAKE")) / 2, 23);
        g.dispose();
        int[] px = img.getRGB(0, 0, 128, 32, null, 0, 128);
        net.minecraft.class_310.method_1551().execute(() -> {
            try { dev.lego.ui.Tx.register(fakeTexId, px, 128, 32, true); } catch (Throwable ignored) { }
        });
        return fakeTexId;
    }

    private static int levenshtein(String a, String b) {
        int m = a.length(), n = b.length();
        if (m == 0) return n;
        if (n == 0) return m;
        int[] prev = new int[n + 1], curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;
        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[n];
    }

    private static int visualDist(String a, String b) {
        int d = 0;
        for (int i = 0; i < a.length(); i++) {
            char ca = a.charAt(i), cb = b.charAt(i);
            if (ca != cb && !visuallySimilar(ca, cb)) d++;
        }
        return d;
    }

    private static boolean visuallySimilar(char a, char b) {
        String pair = "" + Math.min(a, b) + Math.max(a, b);
        return "1l".equals(pair) || "1i".equals(pair) || "il".equals(pair) || "0o".equals(pair)
                || "ij".equals(pair) || "mn".equals(pair) || "uv".equals(pair) || "cl".equals(pair);
    }

    // ================================================================== management view
    public final class FakeCheckView extends View {
        private String input = "";
        private float scroll = 0, target = 0;

        @Override public float designW() { return 500; }
        @Override public float designH() { return 380; }

        @Override
        public void draw() {
            float t = dev.lego.ui.Ease.outQuint(open);
            Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.35f * open));
            int pw = p(500), ph = p(380), px = (W - pw) / 2, py = (H - ph) / 2;
            Gx.pushAlpha(t);
            Gx.push();
            Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
            Gx.shadow(px, py, pw, ph, p(18), p(14), 0.55f);
            Gx.rect(px, py, pw, ph, p(18), Style.bg);
            Gx.outline(px, py, pw, ph, p(18), Math.max(1, p(0.6)), Style.stroke);

            int tb = py + p(14), bs = p(30);
            boolean hb = hover(px + p(14), tb, bs, bs);
            Gx.rect(px + p(14), tb, bs, bs, p(9), hb ? Style.surfaceHover : Style.surface);
            Gx.icon("back", px + p(14) + bs / 2f - pf(1), tb + bs / 2f, pf(14), Style.text);
            Kit.hit(px + p(14), tb, bs, bs).click(() -> { if (host != null) host.close(); });
            Gx.text("Fake-Check", px + p(54), tb + pf(1), pf(14), Fonts.BOLD, Style.text);
            Gx.text("Überwachte Namen – ähnliche Spieler werden als FAKE markiert", px + p(54), tb + pf(19), pf(7.4f), Fonts.REGULAR, Style.sub);

            int cx = px + p(16), cw = pw - p(32);
            int y = tb + p(46);

            int ih = p(32);
            Gx.rect(cx, y, cw - p(80), ih, p(10), Style.surface);
            Gx.outline(cx, y, cw - p(80), ih, p(10), Math.max(1, p(0.5)), Style.stroke);
            Gx.text(input.isEmpty() ? "Name eingeben..." : input, cx + p(10), y + pf(8), pf(10), Fonts.REGULAR, input.isEmpty() ? Style.muted : Style.text);

            int abw = p(70), abx = cx + cw - abw;
            boolean ah = hover(abx, y, abw, ih);
            Kit.button("add", "Hinzufügen", "plus", abx, y, abw, ih, P, true, ah);
            Kit.hit(abx, y, abw, ih).click(this::addName);

            y += ih + p(12);
            parseNames();
            int ly = y, lh = py + ph - p(14) - ly;
            int rh = p(36), gap = p(6);
            Gx.clip(cx - p(4), ly, cw + p(8), lh);
            Kit.clip(cx - p(4), ly, cw + p(8), lh);
            float max = Math.max(0, watchList.size() * (rh + gap) - gap - lh);
            target = Math.max(0, Math.min(max, target));
            scroll += (target - scroll) * Math.min(1f, Gx.dt * 16);
            Kit.hit(cx, ly, cw, lh).scroll(a -> target -= a.floatValue() * p(60));

            for (int i = 0; i < watchList.size(); i++) {
                int ry = Math.round(ly + i * (rh + gap) - scroll);
                if (ry + rh < ly || ry > ly + lh) continue;
                Gx.rect(cx, ry, cw, rh, p(10), Style.surface);
                Gx.text(watchList.get(i), cx + p(14), ry + pf(9), pf(10), Fonts.MEDIUM, Style.text);
                int dbw = p(30);
                int dbx = cx + cw - dbw - p(8), dby = ry + (rh - p(24)) / 2;
                boolean dh = hover(dbx, dby, dbw, p(24));
                Gx.rect(dbx, dby, dbw, p(24), p(8), dh ? 0x40FF3333 : 0x20FF3333);
                Gx.icon("x", dbx + dbw / 2f, dby + pf(12), pf(10), 0xFFFF4444);
                final int idx = i;
                Kit.hit(dbx, dby, dbw, p(24)).click(() -> removeName(idx));
            }

            if (watchList.isEmpty()) {
                Gx.icon("shield", px + pw / 2f, ly + p(40), pf(22), Style.muted);
                Gx.textCenter("Keine Namen in der Liste", px + pw / 2f, ly + pf(72), pf(9), Fonts.MEDIUM, Style.sub);
            }
            Kit.unclip();
            Gx.unclip();
            Gx.pop();
            Gx.popAlpha();
        }

        private void addName() {
            if (input.isEmpty()) return;
            String cur = names.get();
            names.value = cur.isEmpty() ? input : cur + "," + input;
            input = "";
            lastParse = 0;
            Modules.scheduleSave();
        }

        private void removeName(int idx) {
            parseNames();
            if (idx < 0 || idx >= watchList.size()) return;
            watchList.remove(idx);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < watchList.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(watchList.get(i));
            }
            names.value = sb.toString();
            lastParse = 0;
            Modules.scheduleSave();
        }

        @Override public boolean key(int key, int mods) {
            if (key == 256) { if (host != null) host.close(); return true; }
            if (key == 259 && !input.isEmpty()) { input = input.substring(0, input.length() - 1); return true; }
            if (key == 257) { addName(); return true; }
            return false;
        }

        @Override public boolean chr(String s) {
            if (s.length() == 1 && input.length() < 20) { input += s; return true; }
            return false;
        }
    }
}
