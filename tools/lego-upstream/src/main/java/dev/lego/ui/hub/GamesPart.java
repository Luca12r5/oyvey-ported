package dev.lego.ui.hub;

import dev.lego.games.Arcade;
import dev.lego.games.GameInfo;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Style;

import java.util.ArrayList;
import java.util.List;

/** Mini game launcher. */
public final class GamesPart implements HubView.Part {
    private float scroll = 0, target = 0, max = 0;
    private String filter = "";
    private boolean filterFocus = false;

    @Override public void shown() { Arcade.registerAll(); }

    @Override
    public boolean key(int key, int mods) {
        if (filterFocus) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !filter.isEmpty()) {
                filter = filter.substring(0, filter.length() - 1);
                scroll = target = 0;
                return true;
            }
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                if (!filter.isEmpty()) { filter = ""; scroll = target = 0; }
                else filterFocus = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean chr(String s) {
        if (filterFocus && s != null && !s.isEmpty()) {
            filter += s;
            scroll = target = 0;
            return true;
        }
        return false;
    }

    private List<GameInfo> filtered() {
        if (filter.isEmpty()) return GameInfo.ALL;
        String q = filter.toLowerCase(java.util.Locale.ROOT);
        List<GameInfo> out = new ArrayList<>();
        for (GameInfo g : GameInfo.ALL) if (g.name.toLowerCase(java.util.Locale.ROOT).contains(q)) out.add(g);
        return out;
    }

    @Override
    public void draw(HubView v, int x, int y, int w, int h) {
        int pad = v.p(22);
        int top = v.header(x + pad, y + v.p(20), "Minispiele", GameInfo.ALL.size() + " Spiele für Zwischendurch – mit Rekorden");
        // duels against other Lego users
        int dbw = v.p(120), dbh = v.p(30), dbx = x + w - pad - dbw, dby = y + v.p(22);
        int inv = dev.lego.games.Duels.invites().size();
        boolean dh = v.hover(dbx, dby, dbw, dbh);
        Kit.button("duels", inv > 0 ? "Duelle (" + inv + ")" : "Duelle", "users", dbx, dby, dbw, dbh, v.P, true, dh);
        if (inv > 0) {
            float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 220.0);
            Gx.circle(dbx + dbw - v.p(3), dby + v.p(3), v.p(5), Gx.withAlpha(0xFFFFC53D, pulse));
        }
        Kit.hit(dbx, dby, dbw, dbh).click(() -> v.host.open(new dev.lego.games.DuelView()));

        // search bar
        int sbh = v.p(28), sbw = w - pad * 2, sby = top + v.p(2);
        v.searchBox("games", x + pad, sby, sbw, sbh, filter, filterFocus);
        Kit.hit(x + pad, sby, sbw, sbh).click(() -> filterFocus = true);
        if (!filter.isEmpty()) Kit.hit(x + pad + sbw - sbh, sby, sbh, sbh).click(() -> { filter = ""; scroll = target = 0; });

        List<GameInfo> games = filtered();
        int ly = sby + sbh + v.p(8), lh = y + h - v.p(16) - ly;
        int cols = 3, gap = v.p(12);
        int cw = (w - pad * 2 - gap * (cols - 1)) / cols, ch = v.p(132);
        int n = games.size();
        int rows = (n + cols - 1) / cols;
        max = Math.max(0, rows * (ch + gap) - gap - lh + v.p(4));
        target = Math.max(0, Math.min(max, target));
        scroll += (target - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(x + pad - v.p(6), ly, w - pad * 2 + v.p(12), lh);
        Kit.clip(x + pad - v.p(6), ly, w - pad * 2 + v.p(12), lh);
        Kit.hit(x, ly, w, lh).scroll(a -> target -= a.floatValue() * v.p(60));
        float appear = v.sectionT(650);
        for (int i = 0; i < n; i++) {
            int cx = x + pad + (i % cols) * (cw + gap), cy = Math.round(ly + v.p(2) + (i / cols) * (ch + gap) - scroll);
            if (cy + ch < ly || cy > ly + lh) continue;
            float a = Ease.outCubic(Ease.clamp((appear * 650 - Math.min(i, 10) * 30) / 260f));
            Gx.pushAlpha(a);
            Gx.push();
            Gx.translate(0, v.pf(10) * (1 - a));
            card(v, games.get(i), cx, cy, cw, ch);
            Gx.pop();
            Gx.popAlpha();
        }
        Kit.unclip();
        Gx.unclip();
        if (max > 0) {
            int trackH = lh - v.p(8), thumbH = Math.max(v.p(24), Math.round(trackH * lh / (float) (lh + max)));
            int thumbY = ly + v.p(4) + Math.round((trackH - thumbH) * (scroll / max));
            Gx.rect(x + w - v.p(10), thumbY, v.p(3), thumbH, v.p(2), Style.strokeHi);
        }
    }

    private void card(HubView v, GameInfo g, int x, int y, int w, int h) {
        boolean modern = dev.lego.ui.UiSettings.guiStyle == 1;
        boolean hv = v.hover(x, y, w, h);
        float hov = Ease.to("game:" + g.id, hv ? 1 : 0, 14);
        int r = v.p(modern ? 16 : 13);
        if (!modern && hov > 0.01f) Gx.shadow(x, y, w, h, r, v.p(8), 0.35f * hov);
        Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * (modern ? 0.5f : 1f)));
        // art header
        int ah = v.p(64);
        if (modern) {
            Gx.rect(x, y, w, ah, r, Gx.mix(g.color, Style.surface, 0.7f));
            Gx.icon(g.icon, x + w / 2f, y + ah / 2f, v.pf(28), Gx.mix(g.color, Style.text, 0.5f));
        } else {
            Gx.rectV(x, y, w, ah + r, r, Gx.mix(g.color, 0xFF000000, 0.35f), Gx.mix(g.color, 0xFF000000, 0.75f));
            Gx.fill(x, y + ah, w, r, Gx.mix(Style.surface, Style.surfaceHover, hov));
            for (int i = 0; i < 5; i++) {
                float bxp = x + w * (0.08f + i * 0.21f), byp = y + v.pf(8) + (i % 2) * v.pf(22);
                Gx.rect(Math.round(bxp), Math.round(byp), v.p(18), v.p(9), v.p(2), Gx.withAlpha(0xFFFFFFFF, 0.05f + 0.02f * (i % 3)));
            }
            Gx.glow(x + w / 2, y + ah / 2, Math.round(ah * 0.9f), Gx.withAlpha(g.color, 0.35f + 0.2f * hov));
            Gx.push();
            Gx.scaleAt(x + w / 2f, y + ah / 2f, 1 + 0.12f * hov);
            Gx.icon(g.icon, x + w / 2f, y + ah / 2f, v.pf(28), 0xFFFFFFFF);
            Gx.pop();
        }
        Gx.outline(x, y, w, h, r, Math.max(1, v.p(modern ? 0.6 : 0.7)), modern ? Gx.mix(Style.stroke, Style.strokeHi, hov) : Gx.mix(Style.stroke, Gx.withAlpha(g.color, 0.8f), hov));
        // text
        Gx.text(Fonts.ellipsize(g.name, Fonts.BOLD, v.pf(10), w - v.p(24)), x + v.p(12), y + ah + v.pf(8), v.pf(10), Fonts.BOLD, Style.text);
        Gx.text(Fonts.ellipsize(g.tagline, Fonts.REGULAR, v.pf(7.4f), w - v.p(24)), x + v.p(12), y + ah + v.pf(24), v.pf(7.4f), Fonts.REGULAR, Style.sub);
        String best = GameInfo.hasBest(g.id) ? "Rekord: " + format(g, GameInfo.best(g.id)) : "Noch nicht gespielt";
        Gx.icon("trophy", x + v.p(17), y + h - v.pf(14), v.pf(9), GameInfo.hasBest(g.id) ? 0xFFFFC53D : Style.muted);
        Gx.textMid(best, x + v.p(26), y + h - v.pf(14), v.pf(7.2f), Fonts.MEDIUM, Style.sub);
        // play button
        int pbs = v.p(24), pbx = x + w - pbs - v.p(10), pby = y + h - pbs - v.p(8);
        Gx.circle(pbx + pbs / 2, pby + pbs / 2, pbs / 2, modern ? Gx.mix(Style.surface2, Style.accent, 0.2f + 0.6f * hov) : Gx.mix(Style.surface2, g.color, 0.35f + 0.65f * hov));
        Gx.icon("play", pbx + pbs / 2f + v.pf(1), pby + pbs / 2f, v.pf(10), hov > 0.5f ? 0xFF101010 : Style.text);
        Kit.hit(x, y, w, h).click(() -> v.host.open(g.factory.get()));
    }

    static String format(GameInfo g, long v) {
        if (!g.higherIsBetter) return v + " ms";
        if (g.id.equals("cps")) return String.format(java.util.Locale.ROOT, "%.1f CPS", v / 10.0);
        return String.valueOf(v);
    }
}
