package dev.lego.ui.hub;

import java.util.ArrayList;
import java.util.List;

import dev.lego.cosmetic.Cos;
import dev.lego.cosmetic.Raster;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Thumbs;

/** Cosmetics wardrobe: categories, 3D item cards, live player preview. */
public final class CosmeticsPart implements HubView.Part {
    private static Cos.Slot filter = null;     // null = all
    private float scroll = 0, target = 0, max = 0;
    private float yaw = 200, yawVel = 0;
    private boolean dragging = false;
    private float lastMx;
    private Cos.Item selected;
    private long changedAt = System.currentTimeMillis();

    @Override public void shown() { Cos.init(); }

    private List<Cos.Item> items() {
        if (filter == null) return Cos.ALL;
        return Cos.of(filter);
    }

    @Override
    public void draw(HubView v, int x, int y, int w, int h) {
        int pad = v.p(18);
        // ---------------------------------------------------------------- categories
        int catW = v.p(128);
        int cx0 = x + pad, cy0 = y + v.p(18);
        Gx.text("Kosmetik", cx0 + v.p(4), cy0, v.pf(15), Fonts.BOLD, Style.text);
        Gx.text(Cos.equippedCount() + " ausgerüstet", cx0 + v.p(4), cy0 + v.pf(21), v.pf(7.6f), Fonts.REGULAR, Style.sub);
        int ly = cy0 + v.p(40);
        List<Cos.Slot> cats = new ArrayList<>();
        cats.add(null);
        for (Cos.Slot s : Cos.Slot.values()) cats.add(s);
        int rowH = v.p(29);
        int sel = cats.indexOf(filter);
        float ay = Ease.to("cos:cat", ly + sel * rowH, 18, ly + sel * rowH);
        Gx.rect(cx0, Math.round(ay), catW, rowH - v.p(3), v.p(8), Gx.withAlpha(Style.accent, 0.16f));
        for (int i = 0; i < cats.size(); i++) {
            Cos.Slot s = cats.get(i);
            int ry = ly + i * rowH;
            boolean hv = v.hover(cx0, ry, catW, rowH - v.p(3));
            boolean act = s == filter;
            float hvv = Ease.to("cos:ch" + i, hv ? 1 : 0, 14);
            if (!act && hvv > 0.01f) Gx.rect(cx0, ry, catW, rowH - v.p(3), v.p(8), Gx.withAlpha(Style.surfaceHover, hvv));
            int col = act ? Style.accent : Gx.mix(Style.sub, Style.text, hvv);
            Gx.icon(s == null ? "grid" : s.icon, cx0 + v.p(15), ry + (rowH - v.p(3)) / 2f, v.pf(12), col);
            String label = s == null ? "Alle" : s.title;
            Gx.textMid(label, cx0 + v.p(28), ry + (rowH - v.p(3)) / 2f, v.pf(8.4f), act ? Fonts.BOLD : Fonts.MEDIUM, act ? Style.text : col);
            int cnt = s == null ? Cos.ALL.size() : Cos.of(s).size();
            Cos.Item eq = s == null ? null : Cos.equipped(s);
            if (eq != null) Gx.circle(cx0 + catW - v.p(26), ry + (rowH - v.p(3)) / 2, v.p(2.6), Style.ok);
            Gx.textRight(String.valueOf(cnt), cx0 + catW - v.p(10), ry + (rowH - v.p(3)) / 2f, v.pf(7.2f), Fonts.MEDIUM, Style.muted);
            Kit.hit(cx0, ry, catW, rowH - v.p(3)).click(() -> { filter = s; target = scroll = 0; changedAt = System.currentTimeMillis(); });
        }
        // unequip all
        int by = y + h - v.p(46), bw = catW, bh = v.p(28);
        boolean hb = v.hover(cx0, by, bw, bh);
        Kit.button("cos:none", "Alles ablegen", "trash", cx0, by, bw, bh, v.P, false, hb);
        Kit.hit(cx0, by, bw, bh).click(Cos::unequipAll);

        // ---------------------------------------------------------------- preview panel (right)
        int pw = v.p(172);
        int px = x + w - pad - pw, py = y + v.p(18), ph = h - v.p(36);
        Gx.rectV(px, py, pw, ph, v.p(14), Gx.mix(Style.surface, Style.accent, 0.08f), Style.surface);
        Gx.outline(px, py, pw, ph, v.p(14), Math.max(1, v.p(0.6)), Style.stroke);
        Gx.glow(px + pw / 2, py + ph / 2 - v.p(30), Math.round(pw * 0.6f), Gx.withAlpha(Style.accent, 0.16f));
        // floor
        Gx.circle(px + pw / 2, py + ph - v.p(92), v.p(44), Gx.withAlpha(0xFF000000, 0.25f));
        if (!dragging) {
            yawVel *= (float) Math.exp(-Gx.dt * 3);
            yaw += (yawVel + 12) * Gx.dt;
        }
        int areaY = py + v.p(34), areaH = ph - v.p(120);
        boolean drawn = Env.playerPreview.draw(px, areaY, pw, areaH, yaw, 0);
        if (!drawn) {
            // offline / fallback: software render of the selected item
            Cos.Item it = selected != null ? selected : (Cos.ALL.isEmpty() ? null : Cos.ALL.get(0));
            if (it != null) {
                int s = Math.min(pw, areaH);
                final float fy = Math.round(yaw / 15f) * 15f;
                Gx.Img img = Thumbs.get("cosbig:" + it.id + ":" + (int) fy, s, s, () -> Raster.render(it, s, s, fy, 10, 1.2f, true, 0x00000000));
                if (img != null) Gx.image(img, px + (pw - s) / 2, areaY + (areaH - s) / 2, s, s, 0xFFFFFFFF);
            }
        }
        Kit.hit(px, areaY, pw, areaH).drag((mx, my) -> {
            if (!dragging) { dragging = true; lastMx = mx; }
            float d = mx - lastMx;
            yaw += d / v.P * 1.6f;
            yawVel = d / v.P * 30;
            lastMx = mx;
        });
        Gx.textCenter("VORSCHAU", px + pw / 2f, py + v.p(16), v.pf(6.8f), Fonts.BOLD, Style.muted);
        // equipped chips
        int ey = py + ph - v.p(80);
        Gx.text("Ausgerüstet", px + v.p(12), ey, v.pf(8), Fonts.BOLD, Style.text);
        int chipY = ey + v.p(16);
        int chipX = px + v.p(12);
        int n = 0;
        for (Cos.Slot s : Cos.Slot.values()) {
            Cos.Item it = Cos.equipped(s);
            if (it == null) continue;
            if (n >= 6) break;
            float fs = v.pf(6.8f);
            String t = Fonts.ellipsize(it.name, Fonts.MEDIUM, fs, pw - v.p(40));
            int cw = Math.round(Gx.width(t, fs, Fonts.MEDIUM)) + v.p(22), chh = v.p(16);
            if (chipX + cw > px + pw - v.p(10)) { chipX = px + v.p(12); chipY += chh + v.p(4); }
            if (chipY + chh > py + ph - v.p(6)) break;
            Gx.rect(chipX, chipY, cw, chh, chh / 2, Gx.withAlpha(it.rarity.color, 0.2f));
            Gx.circle(chipX + v.p(8), chipY + chh / 2, v.p(2.4), it.rarity.color);
            Gx.textMid(t, chipX + v.p(14), chipY + chh / 2f, fs, Fonts.MEDIUM, Style.text);
            chipX += cw + v.p(4);
            n++;
        }
        if (n == 0) Gx.text("Noch nichts – klick auf ein Item!", px + v.p(12), chipY, v.pf(7), Fonts.REGULAR, Style.muted);

        // ---------------------------------------------------------------- grid
        int gx = cx0 + catW + v.p(16), gw = px - v.p(14) - gx;
        int gy = y + v.p(18), gh = h - v.p(36);
        List<Cos.Item> list = items();
        int cols = 2, gap = v.p(10);
        int cw = (gw - gap) / cols, ch = v.p(150);
        int rows = (list.size() + cols - 1) / cols;
        max = Math.max(0, rows * (ch + gap) - gap - gh);
        target = Math.max(0, Math.min(max, target));
        scroll += (target - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(gx - v.p(4), gy, gw + v.p(8), gh);
        Kit.clip(gx - v.p(4), gy, gw + v.p(8), gh);
        Kit.hit(gx, gy, gw, gh).scroll(a -> target -= a.floatValue() * v.p(60));
        float appear = Math.min(1f, (System.currentTimeMillis() - changedAt) * dev.lego.ui.UiSettings.anim() / 600f);
        for (int i = 0; i < list.size(); i++) {
            int cx = gx + (i % cols) * (cw + gap), cy = Math.round(gy + (i / cols) * (ch + gap) - scroll);
            if (cy + ch < gy || cy > gy + gh) continue;
            float a = Ease.outCubic(Ease.clamp((appear * 600 - Math.min(i, 10) * 25) / 260f));
            Gx.pushAlpha(a);
            Gx.push();
            Gx.translate(0, v.pf(10) * (1 - a));
            card(v, list.get(i), cx, cy, cw, ch);
            Gx.pop();
            Gx.popAlpha();
        }
        Kit.unclip();
        Gx.unclip();
        if (max > 0) {
            int trackH = gh - v.p(8), thumbH = Math.max(v.p(24), Math.round(trackH * gh / (float) (gh + max)));
            int thumbY = gy + v.p(4) + Math.round((trackH - thumbH) * (scroll / max));
            Gx.rect(gx + gw + v.p(3), thumbY, v.p(3), thumbH, v.p(2), Style.strokeHi);
        }
    }

    private void card(HubView v, Cos.Item it, int x, int y, int w, int h) {
        boolean modern = dev.lego.ui.UiSettings.guiStyle == 1;
        boolean hv = v.hover(x, y, w, h);
        boolean eq = Cos.isEquipped(it);
        float hov = Ease.to("cos:" + it.id, hv ? 1 : 0, 14);
        float on = Ease.to("coson:" + it.id, eq ? 1 : 0, 12);
        int r = v.p(modern ? 16 : 12);
        if (!modern && hov > 0.01f) Gx.shadow(x, y, w, h, r, v.p(6), 0.3f * hov);
        if (modern) {
            Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * 0.5f));
            Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.6)), Gx.mix(Style.stroke, Style.strokeHi, hov));
            if (eq) Gx.rect(x, y + v.p(8), Math.max(2, v.p(3)), h - v.p(16), v.p(2), Style.ok);
        } else {
            Gx.rectV(x, y, w, h, r, Gx.mix(Style.surface, Gx.mix(Style.surface, it.rarity.color, 0.18f), 0.6f + 0.4f * hov), Style.surface);
            Gx.outline(x, y, w, h, r, Math.max(1, v.p(on > 0.5f ? 1.3 : 0.7)), Gx.mix(Gx.mix(Style.stroke, Gx.withAlpha(it.rarity.color, 0.6f), hov), Style.ok, on));
        }
        int ts = h - v.p(46);
        int tx = x + (w - ts) / 2, ty = y + v.p(6);
        if (!modern) Gx.glow(x + w / 2, ty + ts / 2, Math.round(ts * 0.5f), Gx.withAlpha(it.rarity.color, 0.18f + 0.12f * hov));
        final int px = ts;
        boolean back = it.slot == Cos.Slot.WINGS || it.slot == Cos.Slot.CAPE || it.slot == Cos.Slot.BACK;
        Gx.Img img = Thumbs.get("cos:" + it.id, px, px, () -> Raster.render(it, px, px, back ? 155 : 28, 12, 1.25f, true, 0x00000000));
        if (!modern) { Gx.push(); Gx.scaleAt(x + w / 2f, ty + ts / 2f, 1 + 0.05f * hov); }
        if (img != null) Gx.image(img, tx, ty, ts, ts, 0xFFFFFFFF);
        else EmotesPart.shimmer(v, tx + ts / 4, ty + ts / 4, ts / 2, ts / 2);
        if (!modern) Gx.pop();
        // rarity badge
        float bfs = v.pf(5.8f);
        String rt = it.rarity.title.toUpperCase(java.util.Locale.ROOT);
        int bw = Math.round(Gx.spacedWidth(rt, bfs, Fonts.BOLD, v.pf(0.6f))) + v.p(12), bh = v.p(13);
        Gx.rect(x + v.p(7), y + v.p(7), bw, bh, bh / 2, Gx.withAlpha(it.rarity.color, modern ? 0.14f : 0.22f));
        Gx.textSpaced(rt, x + v.p(7) + bw / 2f, y + v.p(7) + bh / 2f, bfs, Fonts.BOLD, it.rarity.color, v.pf(0.6f));
        // name + button
        Gx.textCenter(Fonts.ellipsize(it.name, Fonts.BOLD, v.pf(8.8f), w - v.p(14)), x + w / 2f, y + h - v.p(33), v.pf(8.8f), Fonts.BOLD, Style.text);
        int bx = x + v.p(10), bby = y + h - v.p(24), bbw = w - v.p(20), bbh = v.p(18);
        boolean hb = v.hover(bx, bby, bbw, bbh);
        int bg = eq ? Gx.mix(Gx.withAlpha(Style.ok, 0.22f), Gx.withAlpha(Style.danger, 0.25f), hb ? 1 : 0) : Gx.mix(Style.surface2, Style.accent, hb ? 1 : 0.0f);
        Gx.rect(bx, bby, bbw, bbh, bbh / 2, bg);
        String lbl = eq ? (hb ? "ABLEGEN" : "AUSGERÜSTET") : "AUSRÜSTEN";
        int fg = eq ? (hb ? Style.danger : Style.ok) : (hb ? Style.accentText : Style.sub);
        if (eq && !hb) Gx.icon("check", bx + bbw / 2f - Gx.spacedWidth(lbl, v.pf(6.4f), Fonts.BOLD, v.pf(0.8f)) / 2f - v.pf(7), bby + bbh / 2f, v.pf(8), fg);
        Gx.textSpaced(lbl, bx + bbw / 2f + (eq && !hb ? v.pf(4) : 0), bby + bbh / 2f, v.pf(6.4f), Fonts.BOLD, fg, v.pf(0.8f));
        Kit.hit(x, y, w, h).click(() -> {
            Cos.toggle(it);
            selected = it;
            Sound.pop();
        });
    }

    @Override
    public boolean key(int key, int mods) {
        if (key == -1 - 0 || key < 0) dragging = false;
        return false;
    }

    @Override public void tick() { if (!dev.lego.ui.Kit.dragging()) dragging = false; }
}
