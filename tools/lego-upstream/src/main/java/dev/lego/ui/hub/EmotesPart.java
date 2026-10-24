package dev.lego.ui.hub;

import dev.lego.cosmetic.Raster;
import dev.lego.emote.EmoteLib;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Thumbs;

/** Emote gallery with 3D pose previews + emote wheel favourites. */
public final class EmotesPart implements HubView.Part {
    private float scroll = 0, target = 0, max = 0;
    private String toast = null;
    private String query = "";
    private boolean focus = false;

    private java.util.List<EmoteLib.Emote> filtered() {
        if (query.isBlank()) return EmoteLib.ALL;
        String q = query.toLowerCase(java.util.Locale.ROOT);
        java.util.List<EmoteLib.Emote> out = new java.util.ArrayList<>();
        for (EmoteLib.Emote e : EmoteLib.ALL) if (e.name.toLowerCase(java.util.Locale.ROOT).contains(q) || e.id.toLowerCase(java.util.Locale.ROOT).contains(q)) out.add(e);
        return out;
    }
    private long toastAt = 0;

    @Override
    public void draw(HubView v, int x, int y, int w, int h) {
        int pad = v.p(22);
        int top = v.header(x + pad, y + v.p(20), "Emotes", "Klick = abspielen   •   Stern = ins Emote-Rad (Taste B halten)");
        int wheelW = v.p(176);
        // search field (top right)
        int sbw = v.p(200), sbh = v.p(28), sbx = x + w - pad - sbw, sby = y + v.p(20);
        v.searchBox("emotes", sbx, sby, sbw, sbh, query, focus);
        Kit.hit(sbx, sby, sbw, sbh).click(() -> {
            if (!query.isEmpty() && v.mx >= sbx + sbw - sbh) { query = ""; target = 0; } else focus = true;
        });
        java.util.List<EmoteLib.Emote> list = filtered();
        int gx = x + pad, gw = w - pad * 2 - wheelW - v.p(16);
        int ly = top + v.p(4), lh = y + h - v.p(16) - ly;

        // ---------------------------------------------------------------- grid
        int cols = 4, gap = v.p(10);
        int cw = (gw - gap * (cols - 1)) / cols, ch = v.p(118);
        int n = list.size();
        int rows = (n + cols - 1) / cols;
        max = Math.max(0, rows * (ch + gap) - gap - lh + v.p(4));
        target = Math.max(0, Math.min(max, target));
        scroll += (target - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(gx - v.p(6), ly, gw + v.p(12), lh);
        Kit.clip(gx - v.p(6), ly, gw + v.p(12), lh);
        Kit.hit(gx, ly, gw, lh).scroll(a -> target -= a.floatValue() * v.p(50));
        float appear = v.sectionT(600);
        for (int i = 0; i < n; i++) {
            int cx = gx + (i % cols) * (cw + gap), cy = Math.round(ly + v.p(2) + (i / cols) * (ch + gap) - scroll);
            if (cy + ch < ly || cy > ly + lh) continue;
            float a = Ease.outCubic(Ease.clamp((appear * 600 - Math.min(i, 10) * 18) / 250f));
            Gx.pushAlpha(a);
            card(v, list.get(i), cx, cy, cw, ch);
            Gx.popAlpha();
        }
        Kit.unclip();
        Gx.unclip();
        if (n == 0) Gx.textCenter("Kein Emote gefunden für \"" + query + "\"", gx + gw / 2f, ly + v.p(40), v.pf(9), Fonts.MEDIUM, Style.sub);
        if (max > 0) {
            int trackH = lh - v.p(8), thumbH = Math.max(v.p(24), Math.round(trackH * lh / (float) (lh + max)));
            int thumbY = ly + v.p(4) + Math.round((trackH - thumbH) * (scroll / max));
            Gx.rect(gx + gw + v.p(4), thumbY, v.p(3), thumbH, v.p(2), Style.strokeHi);
        }

        // ---------------------------------------------------------------- wheel preview
        int wx = x + w - pad - wheelW, wy = ly;
        Gx.rect(wx, wy, wheelW, lh, v.p(14), Style.surface);
        Gx.outline(wx, wy, wheelW, lh, v.p(14), Math.max(1, v.p(0.6)), Style.stroke);
        Gx.text("Emote-Rad", wx + v.p(14), wy + v.p(12), v.pf(10), Fonts.BOLD, Style.text);
        Gx.text(EmoteLib.WHEEL.size() + " / " + EmoteLib.WHEEL_MAX + " Plätze", wx + v.p(14), wy + v.p(29), v.pf(7.6f), Fonts.REGULAR, Style.sub);
        int r = Math.min(wheelW / 2 - v.p(14), v.p(72));
        int ccx = wx + wheelW / 2, ccy = wy + v.p(52) + r;
        Gx.circle(ccx, ccy, r, Style.surface2);
        Gx.circle(ccx, ccy, Math.round(r * 0.36f), Style.bg);
        for (int i = 0; i < EmoteLib.WHEEL_MAX; i++) {
            double ang = -Math.PI / 2 + Math.PI * 2 * i / EmoteLib.WHEEL_MAX;
            float ix = ccx + (float) Math.cos(ang) * r * 0.69f, iy = ccy + (float) Math.sin(ang) * r * 0.69f;
            if (i < EmoteLib.WHEEL.size()) {
                EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(i));
                boolean hv = v.hover(Math.round(ix - v.pf(14)), Math.round(iy - v.pf(14)), v.p(28), v.p(28));
                Gx.circle(Math.round(ix), Math.round(iy), v.p(13), hv ? Gx.withAlpha(Style.danger, 0.25f) : Gx.withAlpha(Style.accent, 0.18f));
                Gx.icon(hv ? "x" : e.icon, ix, iy, v.pf(12), hv ? Style.danger : Style.accent);
                final String id = e.id;
                Kit.hit(Math.round(ix - v.pf(14)), Math.round(iy - v.pf(14)), v.p(28), v.p(28)).click(() -> EmoteLib.toggleWheel(id));
            } else {
                Gx.ringAt(Math.round(ix), Math.round(iy), v.p(12), Math.max(1, v.p(0.8)), Style.strokeHi);
                Gx.icon("plus", ix, iy, v.pf(9), Style.muted);
            }
        }
        Gx.icon("emote", ccx, ccy, v.pf(18), Style.sub);
        int ty = ccy + r + v.p(16);
        String[] tips = {"Taste B halten = Rad öffnen", "Maus auf Emote, loslassen", "Bewegen bricht Loop-Emotes ab"};
        for (String t : tips) {
            Gx.circle(wx + v.p(18), ty + v.p(5), v.p(2), Style.accent);
            Gx.text(Fonts.ellipsize(t, Fonts.REGULAR, v.pf(7.4f), wheelW - v.p(34)), wx + v.p(26), ty, v.pf(7.4f), Fonts.REGULAR, Style.sub);
            ty += v.p(15);
        }

        if (toast != null && System.currentTimeMillis() - toastAt < 1800) {
            float ta = Math.min(1, (1800 - (System.currentTimeMillis() - toastAt)) / 300f);
            Gx.pushAlpha(ta);
            float fs = v.pf(8.4f);
            int tw = Math.round(Gx.width(toast, fs, Fonts.MEDIUM)) + v.p(28), th = v.p(26);
            int tx0 = x + (w - tw) / 2, ty0 = y + h - th - v.p(18);
            Gx.shadow(tx0, ty0, tw, th, th / 2, v.p(6), 0.4f);
            Gx.rect(tx0, ty0, tw, th, th / 2, Style.light ? 0xFF1B1C22 : 0xFFF3F4F6);
            Gx.textCenter(toast, tx0 + tw / 2f, ty0 + th / 2f, fs, Fonts.MEDIUM, Style.light ? 0xFFFFFFFF : 0xFF111216);
            Gx.popAlpha();
        }
    }

    private void card(HubView v, EmoteLib.Emote e, int x, int y, int w, int h) {
        boolean modern = dev.lego.ui.UiSettings.guiStyle == 1;
        boolean hv = v.hover(x, y, w, h);
        float hov = Ease.to("emo:" + e.id, hv ? 1 : 0, 14);
        boolean fav = EmoteLib.inWheel(e.id);
        if (!modern) { Gx.push(); Gx.translate(0, -v.pf(2) * hov); }
        if (!modern && hov > 0.01f) Gx.shadow(x, y, w, h, v.p(12), v.p(6), 0.3f * hov);
        int r = v.p(modern ? 16 : 12);
        Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * (modern ? 0.5f : 1f)));
        Gx.outline(x, y, w, h, r, Math.max(1, v.p(modern ? 0.6 : 0.7)), modern ? Gx.mix(Style.stroke, Style.strokeHi, hov) : Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.7f), hov));
        if (modern && fav) Gx.rect(x, y + v.p(8), Math.max(2, v.p(3)), h - v.p(16), v.p(2), 0xFFFFC53D);
        int ts = h - v.p(34);
        int tx = x + (w - ts) / 2, ty = y + v.p(4);
        if (!modern) Gx.glow(x + w / 2, ty + ts / 2, Math.round(ts * 0.55f), Gx.withAlpha(Style.accent, 0.10f + 0.12f * hov));
        final int px = Math.max(16, ts);
        Gx.Img img = Thumbs.get("emote:" + e.id, px, px, () -> Raster.renderPose(e.pose(Math.min(e.duration * 0.4f, 1.1f)), px, px, 22, 8, 0x00000000));
        if (img != null) Gx.image(img, tx, ty, ts, ts, 0xFFFFFFFF);
        else shimmer(v, tx + ts / 4, ty + ts / 4, ts / 2, ts / 2);
        Gx.textCenter(Fonts.ellipsize(e.name, Fonts.BOLD, v.pf(8.6f), w - v.p(30)), x + w / 2f, y + h - v.p(16), v.pf(8.6f), Fonts.BOLD, Style.text);
        if (e.loop) Gx.textCenter("LOOP", x + w / 2f, y + h - v.p(5), v.pf(5.6f), Fonts.BOLD, Style.muted);
        else if (EmoteLib.isCutscene(e.id)) Gx.textCenter("CUTSCENE", x + w / 2f, y + h - v.p(5), v.pf(5.6f), Fonts.BOLD, Style.accent);
        // star
        int ss = v.p(22), sx = x + w - ss - v.p(5), sy = y + v.p(5);
        boolean hs = v.hover(sx, sy, ss, ss);
        float fv = Ease.to("emofav:" + e.id, fav ? 1 : 0, 16);
        Gx.circle(sx + ss / 2, sy + ss / 2, ss / 2, Gx.mix(Gx.withAlpha(Style.bg, 0.6f), Gx.withAlpha(0xFFFFC53D, 0.22f), fv));
        Gx.push();
        Gx.scaleAt(sx + ss / 2f, sy + ss / 2f, 1 + 0.25f * (1 - Math.abs(fv * 2 - 1)) + (hs ? 0.08f : 0));
        Gx.icon(fav ? "star-fill" : "star", sx + ss / 2f, sy + ss / 2f, v.pf(11), fav ? 0xFFFFC53D : (hs ? Style.text : Style.muted));
        Gx.pop();
        if (!modern) Gx.pop();
        Kit.hit(x, y, w, h).click(() -> {
            int idx = EmoteLib.index(e.id);
            v.host.closeThen(() -> Env.playEmote.accept(idx));
        });
        Kit.hit(sx, sy, ss, ss).click(() -> {
            if (!EmoteLib.toggleWheel(e.id)) { toast = "Emote-Rad ist voll (max. 8)"; toastAt = System.currentTimeMillis(); }
            else Sound.pop();
        });
    }

    static void shimmer(HubView v, int x, int y, int w, int h) {
        float t = (System.currentTimeMillis() % 1200) / 1200f;
        Gx.rect(x, y, w, h, v.p(8), Gx.withAlpha(Style.surface2, 0.6f + 0.4f * (float) Math.sin(t * Math.PI)));
    }

    @Override public boolean chr(String s) {
        if (!focus) { focus = true; query = ""; }
        if (query.length() < 32) { query += s; target = 0; }
        return true;
    }

    @Override public boolean key(int key, int mods) {
        if (key == 259 && !query.isEmpty()) { query = query.substring(0, query.length() - 1); target = 0; return true; }
        if (key == 256 && (focus || !query.isEmpty())) { query = ""; focus = false; target = 0; return true; }
        return false;
    }
}