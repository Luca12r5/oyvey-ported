package dev.lego.ui.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lego.core.Category;
import dev.lego.core.Lang;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Keys;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;

/** Mod list (2 columns, NoRisk style) + animated settings page. */
public final class ModsPart implements HubView.Part {
    private static String[] tabs() { return new String[]{Lang.get("ui.all", "ALLE"), "HUD", "VISUALS", "UTILITY", Lang.get("ui.perf", "LEISTUNG"), "SPOTIFY"}; }
    private static final Category[] TAB_CATS = {null, Category.HUD, Category.VISUALS, Category.UTILITY, Category.PERFORMANCE, Category.SPOTIFY};
    private static int tab = 0;
    private String search = "";
    private boolean searchFocus = false;
    private float scroll = 0, scrollTarget = 0, maxScroll = 0;
    private float sScroll = 0, sScrollTarget = 0, sMaxScroll = 0;
    private Module open, shownOpen;
    private Module binding;
    private Setting.Text editing;
    private boolean renaming;
    private String renameText = "";
    private long listChangedAt = System.currentTimeMillis();

    private static boolean showHidden;

    private List<Module> list() {
        List<Module> out = new ArrayList<>();
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : Modules.ALL) {
            if (!m.category.listed) continue;
            // all modules always visible
            if (!q.isEmpty()) {
                boolean renamed = m.customName != null && !m.customName.isEmpty();
                String dn = m.displayName().toLowerCase(Locale.ROOT);
                String dd = m.displayDesc().toLowerCase(Locale.ROOT);
                if (renamed ? (dn.contains(q) || dd.contains(q) || m.category.displayTitle().toLowerCase(Locale.ROOT).contains(q))
                        : (m.name.toLowerCase(Locale.ROOT).contains(q) || m.description.toLowerCase(Locale.ROOT).contains(q)
                        || dn.contains(q) || dd.contains(q) || m.category.displayTitle().toLowerCase(Locale.ROOT).contains(q))) out.add(m);
            } else if (TAB_CATS[tab] == null || m.category == TAB_CATS[tab]) out.add(m);
        }
        return out;
    }

    private void setTab(int i) {
        if (i == tab && search.isEmpty()) return;
        tab = i;
        search = "";
        scroll = scrollTarget = 0;
        listChangedAt = System.currentTimeMillis();
    }

    @Override
    public void draw(HubView v, int x, int y, int w, int h) {
        float sp = Ease.to("mods:sp", open != null ? 1 : 0, 13);
        if (open != null) shownOpen = open;
        if (sp < 0.01f && open == null) shownOpen = null;

        // list page
        if (sp < 0.99f) {
            Gx.pushAlpha(1 - sp);
            Gx.push();
            Gx.translate(-v.pf(40) * sp, 0);
            drawList(v, x, y, w, h, sp < 0.5f);
            Gx.pop();
            Gx.popAlpha();
        }
        // settings page
        if (sp > 0.01f && shownOpen != null) {
            Gx.pushAlpha(sp);
            Gx.push();
            Gx.translate(v.pf(60) * (1 - Ease.outCubic(sp)), 0);
            drawSettings(v, shownOpen, x, y, w, h, sp > 0.5f);
            Gx.pop();
            Gx.popAlpha();
        }
    }

    // ================================================================== list
    private void drawList(HubView v, int x, int y, int w, int h, boolean interactive) {
        int pad = v.p(22);
        int tx = x + pad, ty = y + v.p(20);
        v.tabs("mods", tx, ty, tabs(), search.isEmpty() ? tab : 0, this::setTab);
        int sw = v.p(168), sh = v.p(26);
        int sx = x + w - pad - sw;
        v.searchBox("mods", sx, ty, sw, sh, search, searchFocus);
        if (interactive) {
            Kit.hit(sx, ty, sw, sh).click(() -> {
                if (!search.isEmpty() && v.mx > sx + sw - sh) { search = ""; listChangedAt = System.currentTimeMillis(); }
                searchFocus = true;
            });
        }

        int ly = ty + sh + v.p(16), lh = y + h - v.p(40) - ly;
        List<Module> mods = list();
        int style = dev.lego.ui.UiSettings.guiStyle; // 0=Standard, 1=Modern, 2=Glass
        if (style >= 1) {
            int rowH = style == 2 ? v.p(44) : v.p(52), headerH = v.p(28), gap = style == 2 ? v.p(4) : v.p(6);
            boolean showHeaders = TAB_CATS[tab] == null && search.isEmpty();
            int totalH = 0;
            Category lastCat = null;
            for (Module m : mods) {
                if (showHeaders && m.category != lastCat) {
                    if (lastCat != null) totalH += v.p(10);
                    totalH += headerH;
                    lastCat = m.category;
                }
                totalH += rowH + gap;
            }
            maxScroll = Math.max(0, totalH - lh + v.p(20));
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
            scroll += (scrollTarget - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
            Gx.clip(x + pad - v.p(6), ly, w - pad * 2 + v.p(12), lh);
            Kit.clip(x, ly, w, lh);
            if (interactive) Kit.hit(x, ly, w, lh).scroll(a -> scrollTarget -= a.floatValue() * v.p(48));
            float el = (System.currentTimeMillis() - listChangedAt) * dev.lego.ui.UiSettings.anim();
            int cy = Math.round(ly + v.p(2) - scroll);
            lastCat = null;
            int vis = 0;
            for (int i = 0; i < mods.size(); i++) {
                Module m = mods.get(i);
                if (showHeaders && m.category != lastCat) {
                    if (lastCat != null) cy += v.p(10);
                    if (cy + headerH >= ly - v.p(4) && cy <= ly + lh + v.p(4))
                        modernCatHeader(v, m.category, x + pad, cy, w - pad * 2, headerH);
                    cy += headerH;
                    lastCat = m.category;
                }
                if (cy + rowH >= ly - v.p(4) && cy <= ly + lh + v.p(4)) {
                    float a = Ease.outCubic(Ease.clamp((el - Math.min(vis, 12) * 22f) / 260f));
                    Gx.pushAlpha(a);
                    Gx.push();
                    Gx.translate(0, v.pf(6) * (1 - a));
                    if (style == 2) glassRow(v, m, x + pad, cy, w - pad * 2, rowH, interactive);
                    else modernRow(v, m, x + pad, cy, w - pad * 2, rowH, interactive);
                    Gx.pop();
                    Gx.popAlpha();
                }
                cy += rowH + gap;
                vis++;
            }
            if (mods.isEmpty()) {
                Gx.icon("search", x + w / 2f, ly + lh / 2f - v.pf(14), v.pf(26), Style.muted);
                Gx.textCenter(Lang.get("ui.nomods", "Keine Module gefunden"), x + w / 2f, ly + lh / 2f + v.pf(14), v.pf(9), Fonts.MEDIUM, Style.sub);
            }
            Kit.unclip();
            Gx.unclip();
        } else {
            int gap = v.p(8);
            int cols = (w - pad * 2) / v.p(300) >= 3 ? 3 : 2;
            int cw = (w - pad * 2 - gap * (cols - 1)) / cols, ch = v.p(54);
            int rows = (mods.size() + cols - 1) / cols;
            maxScroll = Math.max(0, rows * (ch + gap) - gap - lh + v.p(20));
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
            scroll += (scrollTarget - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
            Gx.clip(x + pad - v.p(6), ly, w - pad * 2 + v.p(12), lh);
            Kit.clip(x, ly, w, lh);
            if (interactive) Kit.hit(x, ly, w, lh).scroll(a -> scrollTarget -= a.floatValue() * v.p(48));
            float el = (System.currentTimeMillis() - listChangedAt) * dev.lego.ui.UiSettings.anim();
            int firstRow = Math.max(0, (int) Math.floor(scroll / Math.max(1, ch + gap)));
            for (int i = 0; i < mods.size(); i++) {
                int col = i % cols, row = i / cols;
                int cx = tx + col * (cw + gap);
                int cy = Math.round(ly + v.p(2) + row * (ch + gap) - scroll);
                if (cy + ch < ly - v.p(4) || cy > ly + lh + v.p(4)) continue;
                int vis = Math.max(0, (row - firstRow) * cols + col);
                float a = Ease.outCubic(Ease.clamp((el - Math.min(vis, 12) * 22f) / 260f));
                Gx.pushAlpha(a);
                Gx.push();
                Gx.translate(0, v.pf(8) * (1 - a));
                card(v, mods.get(i), cx, cy, cw, ch, interactive);
                Gx.pop();
                Gx.popAlpha();
            }
            if (mods.isEmpty()) {
                Gx.icon("search", x + w / 2f, ly + lh / 2f - v.pf(14), v.pf(26), Style.muted);
                Gx.textCenter(Lang.get("ui.nomods", "Keine Module gefunden"), x + w / 2f, ly + lh / 2f + v.pf(14), v.pf(9), Fonts.MEDIUM, Style.sub);
            }
            Kit.unclip();
            Gx.unclip();
        }
        // fade edges
        if (scroll > 1) Gx.gradientV(x + v.p(1), ly, w - v.p(2), v.p(14), Style.bg, Style.bg & 0x00FFFFFF);
        if (scroll < maxScroll - 1) Gx.gradientV(x + v.p(1), ly + lh - v.p(14), w - v.p(2), v.p(14), Style.bg & 0x00FFFFFF, Style.bg);
        if (maxScroll > 0) {
            int trackH = lh - v.p(8), thumbH = Math.max(v.p(24), Math.round(trackH * lh / (float) (lh + maxScroll)));
            int thumbY = ly + v.p(4) + Math.round((trackH - thumbH) * (scroll / maxScroll));
            Gx.rect(x + w - v.p(9), thumbY, v.p(3), thumbH, v.p(2), Style.strokeHi);
        }

        // footer
        int fy = y + h - v.p(40);
        Gx.fill(x + v.p(1), fy, w - v.p(2), Math.max(1, v.p(0.6)), Style.stroke);
        int active = 0, total = 0;
        for (Module m : Modules.ALL) if (m.category.listed) { total++; if (m.enabled && m.isToggleable()) active++; }
        float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 400.0);
        Gx.circle(tx + v.p(4), fy + v.p(20), v.p(3.4), Gx.withAlpha(Style.ok, pulse));
        String activeL = active + " " + Lang.get("ui.active", "aktiv");
        Gx.textMid(activeL, tx + v.p(13), fy + v.pf(20), v.pf(8), Fonts.BOLD, Style.text);
        Gx.textMid(Lang.get("ui.of", "von") + " " + total + " " + Lang.get("ui.modules", "Modulen"), tx + v.p(13) + Gx.width(activeL, v.pf(8), Fonts.BOLD) + v.p(5), fy + v.pf(20), v.pf(8), Fonts.REGULAR, Style.muted);
        Gx.textRight(Lang.get("ui.hint", "Klick = an/aus   •   Rechtsklick = Einstellungen"), x + w - pad, fy + v.pf(20), v.pf(7.4f), Fonts.REGULAR, Style.muted);
    }

    private void card(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
        boolean hv = interactive && v.hover(x, y, w, h);
        float hov = Ease.to("card:" + m.id, hv ? 1 : 0, 14);
        float on = Ease.to("cardon:" + m.id, m.enabled ? 1 : 0, 12);
        int r = v.p(10);
        Gx.push();
        Gx.translate(0, -v.pf(1.5f) * hov);
        if (hov > 0.01f || on > 0.01f) Gx.shadow(x, y, w, h, r, v.p(7), 0.18f * on + 0.3f * hov);
        int base = Gx.mix(Style.surface, Style.surfaceHover, hov);
        Gx.rectV(x, y, w, h, r, Gx.mix(base, 0xFFFFFFFF, 0.035f + 0.02f * hov), Gx.mix(base, 0xFF000000, 0.12f));
        if (on > 0.01f) {
            Gx.pushAlpha(on);
            Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.06f));
            Gx.rect(x + v.p(3), y + v.p(14), Math.max(2, v.p(2.2)), h - v.p(28), v.p(2), Style.accent);
            Gx.glow(x + v.p(2), y + h / 2, v.p(18), Gx.withAlpha(Style.accent, 0.25f));
            Gx.popAlpha();
        }
        Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.7f)), Gx.mix(Gx.mix(Style.stroke, Style.strokeHi, hov), Gx.withAlpha(Style.accent, 0.30f + 0.3f * hov), on));
        Gx.fill(x + r, y + Math.max(1, v.p(0.7)), w - r * 2, Math.max(1, v.p(0.6)), Gx.withAlpha(0xFFFFFFFF, 0.06f + 0.04f * hov));

        int is = v.p(34);
        int ix = x + v.p(12), iy = y + (h - is) / 2;
        if (on > 0.01f) Gx.glow(ix + is / 2, iy + is / 2, Math.round(is * 0.9f), Gx.withAlpha(Style.accent, 0.22f * on));
        Gx.rect(ix, iy, is, is, v.p(11), Gx.mix(Style.surface2, Style.accent, on * 0.92f));
        Gx.outline(ix, iy, is, is, v.p(11), Math.max(1, v.p(0.6)), Gx.withAlpha(0xFFFFFFFF, 0.08f + 0.1f * on));
        Gx.icon(m.icon, ix + is / 2f, iy + is / 2f, v.pf(17 + hov), Gx.mix(Style.sub, 0xFFFFFFFF, Math.max(on, hov * 0.5f)));

        int tx = ix + is + v.p(12);
        int right = x + w - v.p(12);
        int tw = v.p(32), th = v.p(18);
        int togX = right - tw, togY = y + (h - th) / 2;
        boolean hasSettings = !m.settings.isEmpty();
        int gearS = v.p(24), gearX = togX - v.p(8) - gearS, gearY = y + (h - gearS) / 2;
        int textMax = (hasSettings ? gearX : togX) - v.p(8) - tx;
        float nameSize = v.pf(9.4f);
        String name = Fonts.ellipsize(m.displayName(), Fonts.BOLD, nameSize, textMax - (m.fresh ? v.p(30) : 0));
        float nw = Gx.text(name, tx, y + v.pf(9), nameSize, Fonts.BOLD, Style.text);
        if (m.hidden) {
            int bx = Math.round(tx + nw + v.pf(6)), by = y + v.p(10), bw = v.p(14), bh = v.p(12);
            Gx.icon("eye_off", bx + bw / 2f, by + bh / 2f, v.pf(9), Style.muted);
        } else if (m.fresh) {
            int bx = Math.round(tx + nw + v.pf(6)), by = y + v.p(10), bw = v.p(26), bh = v.p(12);
            Gx.rect(bx, by, bw, bh, bh / 2, 0xFFFFC53D);
            Gx.textCenter(Lang.get("ui.new", "NEU"), bx + bw / 2f, by + bh / 2f, v.pf(6.2f), Fonts.BOLD, 0xFF1A1300);
        }
        List<String> lines = Fonts.wrap(m.displayDesc(), Fonts.REGULAR, v.pf(7.2f), textMax, 2);
        for (int i = 0; i < lines.size(); i++) Gx.text(lines.get(i), tx, y + v.pf(25 + i * 10.5f), v.pf(7.2f), Fonts.REGULAR, Style.sub);

        if (m.isToggleable()) Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
        else {
            Gx.rect(togX - v.p(10), togY - v.p(2), tw + v.p(10), th + v.p(4), v.p(8), Style.accent);
            Gx.icon("play", togX - v.p(10) + (tw + v.p(10)) / 2f, togY + th / 2f, v.pf(9), Style.accentText);
        }
        if (hasSettings) {
            boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
            float hgv = Ease.to("gear:" + m.id, hg ? 1 : 0, 16);
            Gx.rect(gearX, gearY, gearS, gearS, v.p(7), Gx.withAlpha(Style.surface2, 0.6f + 0.4f * hgv));
            Gx.icon("dots", gearX + gearS / 2f, gearY + gearS / 2f, v.pf(13), Gx.mix(Style.sub, Style.text, hgv));
        }
        Gx.pop();
        if (!interactive) return;
        Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) { m.toggle(); Modules.scheduleSave(); }
            else openSettings(m);
        }).right(() -> openSettings(m));
        if (hasSettings) Kit.hit(gearX, gearY, gearS, gearS).click(() -> openSettings(m));
    }

    // ================================================================== modern list view
    private void modernCatHeader(HubView v, Category cat, int x, int y, int w, int h) {
        float fs = v.pf(7.2f);
        Gx.icon(cat.icon, x + v.p(4), y + h / 2f, v.pf(11), Style.muted);
        String title = cat.displayTitle().toUpperCase(Locale.ROOT);
        float tw = Gx.textMid(title, x + v.p(20), y + h / 2f, fs, Fonts.BOLD, Style.muted);
        Gx.fill(Math.round(x + v.p(24) + tw), y + h / 2, Math.round(w - v.p(28) - tw), Math.max(1, v.p(0.5)), Gx.withAlpha(Style.stroke, 0.5f));
    }

    private void modernRow(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
        boolean hv = interactive && v.hover(x, y, w, h);
        float hov = Ease.to("mrow:" + m.id, hv ? 1 : 0, 11);
        float on = Ease.to("mron:" + m.id, m.enabled ? 1 : 0, 10);
        int r = v.p(16);
        Gx.push();
        float sc = 1f + 0.012f * hov;
        Gx.scaleAt(x + w / 2f, y + h / 2f, sc);
        Gx.translate(0, -v.pf(2.5f) * hov);
        if (hov > 0.02f || on > 0.01f) Gx.shadow(x, y, w, h, r, v.p(12), 0.12f + 0.25f * hov + 0.15f * on);
        Gx.rect(x, y, w, h, r, Gx.withAlpha(Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6f), 0.82f + 0.12f * hov));
        if (on > 0.01f) {
            Gx.pushAlpha(on);
            Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.06f + 0.06f * hov));
            Gx.gradientV(x, y, v.p(4), h, Gx.withAlpha(Style.accent, 0.9f), Gx.withAlpha(Style.accent, 0.3f));
            Gx.glow(x + v.p(3), y + h / 2, v.p(28), Gx.withAlpha(Style.accent, 0.20f));
            Gx.popAlpha();
        }
        Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.7)), Gx.mix(Gx.mix(Style.stroke, Style.strokeHi, hov), Gx.withAlpha(Style.accent, 0.4f), on));
        if (hov > 0.01f) Gx.fill(x + r, y + Math.max(1, v.p(0.5)), w - r * 2, Math.max(1, v.p(0.5)), Gx.withAlpha(0xFFFFFFFF, 0.06f * hov));
        int is = v.p(36);
        int ix = x + v.p(14), iy = y + (h - is) / 2;
        int iconBg = Gx.mix(Style.surface2, Style.accent, on * 0.8f + hov * 0.2f);
        Gx.circle(ix + is / 2, iy + is / 2, is / 2, iconBg);
        if (on > 0.3f) Gx.glow(ix + is / 2, iy + is / 2, v.p(22), Gx.withAlpha(Style.accent, 0.18f * on));
        Gx.outline(ix, iy, is, is, is / 2, Math.max(1, v.p(0.5)), Gx.withAlpha(0xFFFFFFFF, 0.08f + 0.12f * on));
        Gx.icon(m.icon, ix + is / 2f, iy + is / 2f, v.pf(16 + hov * 1.2f), Gx.mix(Style.sub, 0xFFFFFFFF, Math.max(on, hov * 0.6f)));
        int tx = ix + is + v.p(14);
        int right = x + w - v.p(14);
        int tw = v.p(34), th = v.p(20);
        int togX = right - tw, togY = y + (h - th) / 2;
        boolean hasSettings = !m.settings.isEmpty();
        int gearS = v.p(26), gearX = togX - v.p(8) - gearS, gearY = y + (h - gearS) / 2;
        int textMax = (hasSettings ? gearX : togX) - v.p(8) - tx;
        float nameSize = v.pf(10);
        String name = m.displayName();
        float nw = Gx.width(name, nameSize, Fonts.BOLD);
        float descSize = v.pf(7.4f);
        String sep = "  ·  ";
        float sepW = Gx.width(sep, descSize, Fonts.REGULAR);
        if (nw + sepW + v.p(30) < textMax) {
            String eName = Fonts.ellipsize(name, Fonts.BOLD, nameSize, textMax * 0.45f);
            nw = Gx.textMid(eName, tx, y + h / 2f - v.pf(1), nameSize, Fonts.BOLD, Style.text);
            if (m.hidden) {
                Gx.icon("eye_off", Math.round(tx + nw + v.p(6)), y + h / 2, v.pf(9), Style.muted);
                nw += v.p(15);
            } else if (m.fresh) {
                int bx = Math.round(tx + nw + v.pf(7)), by = Math.round(y + h / 2f - v.pf(5));
                Gx.rect(bx, by, v.p(6), v.p(6), v.p(3), 0xFFFFC53D);
                nw += v.p(16);
            }
            float sx2 = tx + nw;
            Gx.textMid(sep, sx2, y + h / 2f - v.pf(1), descSize, Fonts.REGULAR, Style.muted);
            int descMax = Math.round(textMax - nw - sepW);
            String desc = Fonts.ellipsize(m.displayDesc(), Fonts.REGULAR, descSize, descMax);
            Gx.textMid(desc, sx2 + sepW, y + h / 2f - v.pf(1), descSize, Fonts.REGULAR, Style.sub);
        } else {
            name = Fonts.ellipsize(name, Fonts.BOLD, nameSize, textMax);
            Gx.textMid(name, tx, y + h / 2f - v.pf(1), nameSize, Fonts.BOLD, Style.text);
        }
        String cat = m.category.displayTitle().toUpperCase(Locale.ROOT);
        Gx.text(cat, tx, y + h / 2f + v.pf(9), v.pf(6), Fonts.MEDIUM, Gx.withAlpha(Style.muted, 0.7f));
        if (m.isToggleable()) Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
        else {
            Gx.rect(togX - v.p(8), togY - v.p(2), tw + v.p(8), th + v.p(4), v.p(10), Style.accent);
            Gx.icon("play", togX - v.p(8) + (tw + v.p(8)) / 2f, togY + th / 2f, v.pf(10), Style.accentText);
        }
        if (hasSettings) {
            boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
            float hgv = Ease.to("mgr:" + m.id, hg ? 1 : 0, 14);
            Gx.circle(gearX + gearS / 2, gearY + gearS / 2, gearS / 2, Gx.withAlpha(Style.surface2, 0.4f + 0.6f * hgv));
            Gx.icon("dots", gearX + gearS / 2f, gearY + gearS / 2f, v.pf(12), Gx.mix(Style.sub, Style.text, hgv));
        }
        Gx.pop();
        if (!interactive) return;
        Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) { m.toggle(); Modules.scheduleSave(); }
            else openSettings(m);
        }).right(() -> openSettings(m));
        if (hasSettings) Kit.hit(gearX, gearY, gearS, gearS).click(() -> openSettings(m));
    }

    private void glassRow(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
        boolean hv = interactive && v.hover(x, y, w, h);
        float hov = Ease.to("grow:" + m.id, hv ? 1 : 0, 12);
        float on = Ease.to("gron:" + m.id, m.enabled ? 1 : 0, 10);
        int r = v.p(14);
        Gx.push();
        Gx.translate(0, -v.pf(1) * hov);
        float glassAlpha = (float) dev.lego.ui.UiSettings.glass;
        if (hov > 0.02f) Gx.shadow(x, y, w, h, r, v.p(10), 0.08f * hov);
        Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.surface, glassAlpha * (0.25f + 0.15f * hov)));
        Gx.fill(x + r, y + Math.max(1, v.p(0.4)), w - r * 2, Math.max(1, v.p(0.4)), Gx.withAlpha(0xFFFFFFFF, 0.06f + 0.04f * hov));
        if (on > 0.01f) {
            Gx.pushAlpha(on);
            Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.04f));
            Gx.fill(x + v.p(1), y + v.p(8), Math.max(2, v.p(2)), h - v.p(16), Gx.withAlpha(Style.accent, 0.8f));
            Gx.glow(x + v.p(2), y + h / 2, v.p(18), Gx.withAlpha(Style.accent, 0.12f));
            Gx.popAlpha();
        }
        Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.4)), Gx.withAlpha(Gx.mix(0xFFFFFFFF, Style.accent, on * 0.5f), 0.10f + 0.08f * hov + 0.06f * on));
        int is = v.p(30);
        int ix = x + v.p(12), iy = y + (h - is) / 2;
        Gx.rect(ix, iy, is, is, v.p(10), Gx.withAlpha(Gx.mix(Style.surface2, Style.accent, on * 0.7f), 0.4f + 0.2f * on));
        Gx.outline(ix, iy, is, is, v.p(10), Math.max(1, v.p(0.4)), Gx.withAlpha(0xFFFFFFFF, 0.06f));
        Gx.icon(m.icon, ix + is / 2f, iy + is / 2f, v.pf(14 + hov * 0.6f), Gx.mix(Gx.withAlpha(Style.text, 0.7f), 0xFFFFFFFF, Math.max(on, hov * 0.4f)));
        int tx = ix + is + v.p(12);
        int right = x + w - v.p(12);
        int tw = v.p(32), th = v.p(18);
        int togX = right - tw, togY = y + (h - th) / 2;
        boolean hasSettings = !m.settings.isEmpty();
        int gearS = v.p(22), gearX = togX - v.p(6) - gearS, gearY = y + (h - gearS) / 2;
        int textMax = (hasSettings ? gearX : togX) - v.p(6) - tx;
        float nameSize = v.pf(9);
        String name = Fonts.ellipsize(m.displayName(), Fonts.BOLD, nameSize, textMax - (m.fresh ? v.p(20) : 0));
        float nw = Gx.textMid(name, tx, y + h / 2f, nameSize, Fonts.BOLD, Gx.withAlpha(Style.text, 0.92f));
        if (m.hidden) {
            Gx.icon("eye_off", Math.round(tx + nw + v.p(5)), y + h / 2, v.pf(8), Style.muted);
            nw += v.p(13);
        } else if (m.fresh) {
            Gx.circle(Math.round(tx + nw + v.p(6)), y + h / 2, v.p(2.5), 0xFFFFC53D);
            nw += v.p(12);
        }
        float sx2 = tx + nw + v.p(8);
        String desc = Fonts.ellipsize(m.displayDesc(), Fonts.REGULAR, v.pf(7), Math.round(textMax - nw - v.p(10)));
        Gx.textMid(desc, sx2, y + h / 2f, v.pf(7), Fonts.REGULAR, Gx.withAlpha(Style.sub, 0.7f));
        if (m.isToggleable()) Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
        else {
            Gx.rect(togX - v.p(6), togY - v.p(1), tw + v.p(6), th + v.p(2), v.p(9), Gx.withAlpha(Style.accent, 0.7f));
            Gx.icon("play", togX - v.p(6) + (tw + v.p(6)) / 2f, togY + th / 2f, v.pf(9), Style.accentText);
        }
        if (hasSettings) {
            boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
            float hgv = Ease.to("ggr:" + m.id, hg ? 1 : 0, 16);
            Gx.rect(gearX, gearY, gearS, gearS, v.p(8), Gx.withAlpha(Style.surface2, 0.2f + 0.4f * hgv));
            Gx.icon("dots", gearX + gearS / 2f, gearY + gearS / 2f, v.pf(11), Gx.withAlpha(Gx.mix(Style.sub, Style.text, hgv), 0.8f));
        }
        Gx.pop();
        if (!interactive) return;
        Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) { m.toggle(); Modules.scheduleSave(); }
            else openSettings(m);
        }).right(() -> openSettings(m));
        if (hasSettings) Kit.hit(gearX, gearY, gearS, gearS).click(() -> openSettings(m));
    }

    private void openSettings(Module m) {
        open = m;
        binding = null;
        editing = null;
        sScroll = sScrollTarget = 0;
        searchFocus = false;
        Sound.pop();
    }

    // ================================================================== settings page
    private void drawSettings(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
        int pad = v.p(22);
        int top = y + v.p(18);
        // back
        int bs = v.p(30);
        boolean hb = interactive && v.hover(x + pad, top, bs, bs);
        float hbv = Ease.to("set:back", hb ? 1 : 0, 16);
        Gx.rect(x + pad, top, bs, bs, v.p(9), Gx.mix(Style.surface, Style.surfaceHover, hbv));
        Gx.icon("back", x + pad + bs / 2f - v.pf(1), top + bs / 2f, v.pf(14), Style.text);
        if (interactive) Kit.hit(x + pad, top, bs, bs).click(() -> { open = null; binding = null; editing = null; });
        // title
        int is = v.p(42);
        int ix = x + pad + bs + v.p(12);
        Gx.rect(ix, top - v.p(6), is, is, v.p(12), Gx.withAlpha(Style.accent, 0.18f));
        Gx.icon(m.icon, ix + is / 2f, top - v.p(6) + is / 2f, v.pf(21), Style.accent);
        int tx = ix + is + v.p(12);
        Gx.text(m.displayName(), tx, top - v.pf(4), v.pf(14.5f), Fonts.BOLD, Style.text);
        Gx.text(Fonts.ellipsize(m.description, Fonts.REGULAR, v.pf(8), w - (tx - x) - v.p(190)), tx, top + v.pf(18), v.pf(8), Fonts.REGULAR, Style.sub);
        // toggle + key
        int right = x + w - pad;
        if (m.isToggleable()) {
            int tw = v.p(40), th = v.p(22);
            Kit.toggle(m.id + ":big", right - tw, top + v.p(4), tw, th, m.enabled, v.P);
            if (interactive) Kit.hit(right - tw, top + v.p(4), tw, th).click(() -> { m.toggle(); Modules.scheduleSave(); });
            String kl = binding == m ? Lang.get("ui.presskey", "Taste drücken…") : Lang.get("ui.key", "Taste") + ": " + Keys.name(m.key);
            float kfs = v.pf(7.6f);
            int kw = Math.round(Gx.width(kl, kfs, Fonts.MEDIUM)) + v.p(20), kh = v.p(22);
            int kx = right - tw - v.p(10) - kw, ky = top + v.p(4);
            boolean hk = interactive && v.hover(kx, ky, kw, kh);
            float hkv = Ease.to("set:key", hk || binding == m ? 1 : 0, 14);
            Gx.rect(kx, ky, kw, kh, kh / 2, Gx.mix(Style.surface, Style.surfaceHover, hkv));
            Gx.outline(kx, ky, kw, kh, kh / 2, Math.max(1, v.p(0.6)), binding == m ? Style.accent : Style.stroke);
            Gx.textCenter(kl, kx + kw / 2f, ky + kh / 2f, kfs, Fonts.MEDIUM, binding == m ? Style.accent : Style.sub);
            if (interactive) Kit.hit(kx, ky, kw, kh).click(() -> binding = binding == m ? null : m).right(() -> { m.key = -1; binding = null; Modules.scheduleSave(); });
            // rename button
            String hl = renaming ? Lang.get("ui.done", "Fertig") : Lang.get("ui.rename", "Umbenennen");
            float hfs = v.pf(7.2f);
            int hw = Math.round(Gx.width(hl, hfs, Fonts.MEDIUM)) + v.p(16), hh = v.p(20);
            int hxb = kx - v.p(10) - hw;
            boolean hhov = interactive && v.hover(hxb, ky + v.p(1), hw, hh);
            float hhv = Ease.to("set:rename", hhov ? 1 : 0, 14);
            Gx.rect(hxb, ky + v.p(1), hw, hh, hh / 2, Gx.mix(Style.surface, Style.surfaceHover, hhv));
            Gx.outline(hxb, ky + v.p(1), hw, hh, hh / 2, Math.max(1, v.p(0.5)), renaming ? Style.accent : Style.stroke);
            Gx.textCenter(hl, hxb + hw / 2f, ky + v.p(1) + hh / 2f, hfs, Fonts.MEDIUM, renaming ? Style.accent : Style.sub);
            if (interactive) Kit.hit(hxb, ky + v.p(1), hw, hh).click(() -> {
                if (renaming) {
                    String t = renameText.trim();
                    m.customName = t.isEmpty() || t.equals(m.name) ? null : t;
                    Modules.scheduleSave();
                    renaming = false;
                } else {
                    renaming = true;
                    renameText = m.displayName();
                }
            });
            // hide button
            String hideLbl = m.hidden ? Lang.get("ui.show", "Einblenden") : Lang.get("ui.hide", "Ausblenden");
            int hidW = Math.round(Gx.width(hideLbl, hfs, Fonts.MEDIUM)) + v.p(16);
            int hidX = hxb - v.p(8) - hidW;
            boolean hidHov = interactive && v.hover(hidX, ky + v.p(1), hidW, hh);
            float hidF = Ease.to("set:hide", hidHov ? 1 : 0, 14);
            Gx.rect(hidX, ky + v.p(1), hidW, hh, hh / 2, Gx.mix(Style.surface, Style.surfaceHover, hidF));
            Gx.outline(hidX, ky + v.p(1), hidW, hh, hh / 2, Math.max(1, v.p(0.5)), m.hidden ? Style.accent : Style.stroke);
            Gx.textCenter(hideLbl, hidX + hidW / 2f, ky + v.p(1) + hh / 2f, hfs, Fonts.MEDIUM, m.hidden ? Style.accent : Style.sub);
            if (interactive) Kit.hit(hidX, ky + v.p(1), hidW, hh).click(() -> { m.hidden = !m.hidden; Modules.scheduleSave(); });
            if (renaming) {
                int rx = tx, ry = top + v.p(34);
                int rw = right - hxb - hw - v.p(20) - rx + tx;
                int rh = v.p(22);
                Gx.rect(rx, ry, rw, rh, v.p(6), Style.surface2);
                Gx.outline(rx, ry, rw, rh, v.p(6), Math.max(1, v.p(0.6)), Style.accent);
                float curBlink = (System.currentTimeMillis() % 1000) < 500 ? 1 : 0;
                float tw2 = Gx.text(renameText, rx + v.p(6), ry + rh / 2f, v.pf(8.5f), Fonts.MEDIUM, Style.text);
                if (curBlink > 0) Gx.fill(rx + v.p(6) + Math.round(tw2), ry + v.p(4), Math.max(1, v.p(1)), rh - v.p(8), Style.accent);
            }
        }

        int ly = top + v.p(48), lh = y + h - v.p(48) - ly;
        Gx.fill(x + pad, ly - v.p(8), w - pad * 2, Math.max(1, v.p(0.6)), Style.stroke);
        // rows
        int rowGap = v.p(8);
        int total = 0;
        for (Setting s : m.settings) total += rowH(v, s) + rowGap;
        sMaxScroll = Math.max(0, total - lh + v.p(16));
        sScrollTarget = Math.max(0, Math.min(sMaxScroll, sScrollTarget));
        sScroll += (sScrollTarget - sScroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(x, ly, w, lh);
        Kit.clip(x, ly, w, lh);
        if (interactive) Kit.hit(x, ly, w, lh).scroll(a -> sScrollTarget -= a.floatValue() * v.p(40));
        int ry = Math.round(ly - sScroll);
        if (m.settings.isEmpty()) {
            Gx.textCenter(Lang.get("ui.nosettings", "Dieses Modul hat keine Einstellungen."), x + w / 2f, ly + v.pf(40), v.pf(9), Fonts.MEDIUM, Style.sub);
        }
        for (int i = 0; i < m.settings.size(); i++) {
            Setting s = m.settings.get(i);
            int rh = rowH(v, s);
            if (ry + rh >= ly && ry <= ly + lh) row(v, m, s, x + pad, ry, w - pad * 2, rh, interactive, i);
            ry += rh + rowGap;
        }
        Kit.unclip();
        Gx.unclip();

        // footer: reset
        int fy = y + h - v.p(40);
        Gx.fill(x + v.p(1), fy, w - v.p(2), Math.max(1, v.p(0.6)), Style.stroke);
        String rl = Lang.get("ui.reset", "Standard wiederherstellen");
        int rw = Math.round(Gx.width(rl, v.pf(7.8f), Fonts.MEDIUM)) + v.p(34), rh = v.p(24);
        int rx = x + w - pad - rw, rby = fy + v.p(8);
        boolean hr = interactive && v.hover(rx, rby, rw, rh);
        float hrv = Ease.to("set:reset", hr ? 1 : 0, 14);
        Gx.rect(rx, rby, rw, rh, rh / 2, Gx.mix(Style.surface, Style.surfaceHover, hrv));
        Gx.icon("reset", rx + v.p(14), rby + rh / 2f, v.pf(10), Gx.mix(Style.sub, Style.danger, hrv));
        Gx.textMid(rl, rx + v.p(24), rby + rh / 2f, v.pf(7.8f), Fonts.MEDIUM, Style.sub);
        if (interactive) Kit.hit(rx, rby, rw, rh).click(() -> { m.resetSettings(); Modules.scheduleSave(); });
        Gx.textMid(m.category.displayTitle() + "  •  " + m.settings.size() + " " + Lang.get("ui.settings", "Einstellungen"), x + pad, fy + v.pf(20), v.pf(7.6f), Fonts.REGULAR, Style.muted);
    }

    private int rowH(HubView v, Setting s) {
        if (s instanceof Setting.Color) return v.p(66);
        return v.p(44);
    }

    private void row(HubView v, Module m, Setting s, int x, int y, int w, int h, boolean interactive, int idx) {
        String key = m.id + ":" + s.name;
        boolean hv = interactive && v.hover(x, y, w, h);
        float hov = Ease.to("row:" + key, hv ? 1 : 0, 14);
        Gx.rect(x, y, w, h, v.p(10), Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6f));
        float fs = v.pf(9);
        int cy = s instanceof Setting.Color ? y + v.p(20) : y + h / 2;
        Gx.textMid(s.displayName(), x + v.p(16), cy, fs, Fonts.MEDIUM, Style.text);
        int right = x + w - v.p(16);

        if (s instanceof Setting.Bool) {
            Setting.Bool b = (Setting.Bool) s;
            int tw = v.p(32), th = v.p(18);
            Kit.toggle(key, right - tw, cy - th / 2, tw, th, b.value, v.P);
            if (interactive) Kit.hit(x, y, w, h).click(() -> { b.value = !b.value; Modules.scheduleSave(); });
        } else if (s instanceof Setting.Num) {
            Setting.Num n = (Setting.Num) s;
            String val = n.display();
            float vfs = v.pf(8.2f);
            int valW = v.p(58);
            int bw = v.p(58), bh = v.p(22);
            Gx.rect(right - bw, cy - bh / 2, bw, bh, v.p(7), Style.surface2);
            Gx.textCenter(val, right - bw / 2f, cy, vfs, Fonts.BOLD, Style.text);
            int sw = Math.min(v.p(220), w / 2), sx = right - valW - v.p(14) - sw;
            Kit.slider(key, sx, cy - v.p(9), sw, v.p(18), (float) n.frac(), v.P);
            if (interactive) Kit.hit(sx - v.p(6), y, sw + v.p(12), h).drag((px, py) -> {
                n.setFrac((px - sx) / (double) sw);
                Modules.scheduleSave();
            });
        } else if (s instanceof Setting.Mode) {
            Setting.Mode md = (Setting.Mode) s;
            float mfs = v.pf(7.8f);
            int segPad = v.p(10);
            int totalW = 0;
            for (String o : md.options) totalW += Math.round(Gx.width(o, mfs, Fonts.MEDIUM)) + segPad * 2;
            int segH = v.p(26);
            if (md.options.length <= 5 && totalW <= w * 0.62f) {
                int sx = right - totalW;
                Gx.rect(sx - v.p(3), cy - segH / 2 - v.p(3), totalW + v.p(6), segH + v.p(6), v.p(9), Style.surface2);
                int ox = sx;
                int[] xs = new int[md.options.length], ws = new int[md.options.length];
                for (int i = 0; i < md.options.length; i++) {
                    xs[i] = ox;
                    ws[i] = Math.round(Gx.width(md.options[i], mfs, Fonts.MEDIUM)) + segPad * 2;
                    ox += ws[i];
                }
                float ax = Ease.to("segx:" + key, xs[md.index], 18, xs[md.index]);
                float aw = Ease.to("segw:" + key, ws[md.index], 18, ws[md.index]);
                Gx.rect(Math.round(ax), cy - segH / 2, Math.round(aw), segH, v.p(7), Style.accent);
                for (int i = 0; i < md.options.length; i++) {
                    boolean sel = i == md.index;
                    Gx.textCenter(md.options[i], xs[i] + ws[i] / 2f, cy, mfs, Fonts.MEDIUM, sel ? Style.accentText : Style.sub);
                    final int oi = i;
                    if (interactive) Kit.hit(xs[i], cy - segH / 2, ws[i], segH).click(() -> { md.index = oi; Modules.scheduleSave(); });
                }
            } else {
                int bw = v.p(190), bh = segH;
                int bx = right - bw;
                Gx.rect(bx, cy - bh / 2, bw, bh, v.p(8), Style.surface2);
                boolean hl = interactive && v.hover(bx, cy - bh / 2, bh, bh), hr = interactive && v.hover(bx + bw - bh, cy - bh / 2, bh, bh);
                Gx.icon("chevron-left", bx + bh / 2f, cy, v.pf(11), hl ? Style.accent : Style.sub);
                Gx.icon("chevron-right", bx + bw - bh / 2f, cy, v.pf(11), hr ? Style.accent : Style.sub);
                Gx.textCenter(Fonts.ellipsize(md.get(), Fonts.MEDIUM, mfs, bw - bh * 2), bx + bw / 2f, cy, mfs, Fonts.MEDIUM, Style.text);
                Gx.textRight((md.index + 1) + "/" + md.options.length, bx - v.p(10), cy, v.pf(7), Fonts.REGULAR, Style.muted);
                if (interactive) {
                    Kit.hit(bx, cy - bh / 2, bw / 2, bh).click(() -> { md.cycle(-1); Modules.scheduleSave(); });
                    Kit.hit(bx + bw / 2, cy - bh / 2, bw - bw / 2, bh).click(() -> { md.cycle(1); Modules.scheduleSave(); });
                }
            }
        } else if (s instanceof Setting.Color) {
            Setting.Color c = (Setting.Color) s;
            int n = Setting.Color.PALETTE.length;
            int d = v.p(20), g = v.p(9);
            int sx = x + v.p(16), sy = y + v.p(34);
            for (int i = 0; i < n; i++) {
                int px = sx + i * (d + g);
                boolean sel = c.index == i;
                boolean hs = interactive && v.hover(px, sy, d, d);
                float sc = Ease.to("sw:" + key + i, sel ? 1.12f : (hs ? 1.08f : 1f), 16);
                Gx.push();
                Gx.scaleAt(px + d / 2f, sy + d / 2f, sc);
                if (i == n - 1) {
                    Gx.image(rainbow(d), px, sy, d, d, 0xFFFFFFFF);
                } else {
                    Gx.circle(px + d / 2, sy + d / 2, d / 2, Setting.Color.PALETTE[i]);
                }
                if (sel) Gx.ringAt(px + d / 2, sy + d / 2, d / 2 + v.p(3), Math.max(1, v.p(1.4)), Style.text);
                Gx.pop();
                final int ci = i;
                if (interactive) Kit.hit(px - g / 2, sy - v.p(4), d + g, d + v.p(8)).click(() -> { c.index = ci; Modules.scheduleSave(); });
            }
            Gx.textRight(Setting.Color.NAMES[c.index], right, y + v.p(20), v.pf(8), Fonts.MEDIUM, Style.sub);
        } else if (s instanceof Setting.Text) {
            Setting.Text tx = (Setting.Text) s;
            boolean focus = editing == tx;
            float tfs = v.pf(8);
            int bw = Math.min(v.p(260), w / 2), bh = v.p(26);
            int bx = right - bw;
            float f = Ease.to("txt:" + key, focus ? 1 : 0, 14);
            Gx.rect(bx, cy - bh / 2, bw, bh, v.p(8), Gx.mix(Style.surface2, Style.surfaceHover, f));
            Gx.outline(bx, cy - bh / 2, bw, bh, v.p(8), Math.max(1, v.p(0.7)), Gx.mix(Style.stroke, Style.accent, f));
            String shown = tx.value.isEmpty() && !focus ? tx.hint : tx.value;
            String cut = shown;
            while (Gx.width(cut, tfs, Fonts.MEDIUM) > bw - v.p(24) && cut.length() > 1) cut = cut.substring(1);
            float tw = Gx.textMid(cut, bx + v.p(10), cy, tfs, Fonts.MEDIUM, tx.value.isEmpty() && !focus ? Style.muted : Style.text);
            if (focus && (System.currentTimeMillis() / 500) % 2 == 0) Gx.fill(Math.round(bx + v.p(11) + tw), cy - v.p(6), Math.max(1, v.p(0.9)), v.p(12), Style.accent);
            if (interactive) Kit.hit(bx, cy - bh / 2, bw, bh).click(() -> editing = focus ? null : tx);
        } else if (s instanceof Setting.Action) {
            Setting.Action ac = (Setting.Action) s;
            float afs = v.pf(8);
            int bw = Math.round(Gx.width(ac.label, afs, Fonts.BOLD)) + v.p(30), bh = v.p(26);
            int bx = right - bw;
            boolean hbt = interactive && v.hover(bx, cy - bh / 2, bw, bh);
            Kit.button(key, ac.label, null, bx, cy - bh / 2, bw, bh, v.P, true, hbt);
            if (interactive) Kit.hit(bx, cy - bh / 2, bw, bh).click(() -> {
                try { ac.action.run(); } catch (Throwable t) { dev.lego.LegoClient.LOG("Aktion: " + t); }
            });
        }
    }

    static Gx.Img rainbow(int d) {
        return Gx.painted("rainbow", d, d, g -> {
            for (int a = 0; a < 360; a += 3) {
                g.setColor(java.awt.Color.getHSBColor(a / 360f, 0.75f, 1f));
                g.fill(new java.awt.geom.Arc2D.Double(0, 0, d, d, a, 4, java.awt.geom.Arc2D.PIE));
            }
        });
    }

    // ================================================================== keys
    @Override
    public boolean key(int key, int mods) {
        if (key < 0) return false;
        if (editing != null) {
            if (key == 259 && !editing.value.isEmpty()) editing.value = editing.value.substring(0, editing.value.length() - 1);
            else if (key == 256 || key == 257 || key == 335) editing = null;
            else if (key == 86 && (mods & 2) != 0) {        // Ctrl+V
                String clip = dev.lego.ui.Env.clipboard.get();
                if (clip != null) editing.value = (editing.value + clip.replace('\n', ' ')).substring(0, Math.min(editing.max, editing.value.length() + clip.length()));
            }
            Modules.scheduleSave();
            return true;
        }
        if (binding != null) {
            if (key == 256 || key == 259 || key == 261) binding.key = -1; else binding.key = key;
            binding = null;
            Modules.scheduleSave();
            return true;
        }
        if (renaming) {
            if (key == 259 && !renameText.isEmpty()) { renameText = renameText.substring(0, renameText.length() - 1); return true; }
            if (key == 257 || key == 335) { Module m = open; if (m != null) { String t = renameText.trim(); m.customName = t.isEmpty() || t.equals(m.name) ? null : t; Modules.scheduleSave(); } renaming = false; return true; }
            if (key == 256) { renaming = false; return true; }
            return true;
        }
        if (open != null) {
            if (key == 256) { open = null; editing = null; renaming = false; return true; }
            return false;
        }
        if (key == 259 && !search.isEmpty()) {        // backspace
            search = search.substring(0, search.length() - 1);
            listChangedAt = System.currentTimeMillis();
            scrollTarget = 0;
            return true;
        }
        if (key == 256 && (!search.isEmpty() || searchFocus)) { search = ""; searchFocus = false; listChangedAt = System.currentTimeMillis(); return true; }
        if (key == 258) { setTab((tab + 1) % TAB_CATS.length); return true; }
        return false;
    }

    @Override
    public boolean chr(String s) {
        if (editing != null && s != null) {
            if (editing.value.length() + s.length() <= editing.max) editing.value += s;
            Modules.scheduleSave();
            return true;
        }
        if (renaming && s != null && !s.isEmpty()) { if (renameText.length() + s.length() <= 24) renameText += s; return true; }
        if (binding != null || open != null || s == null || s.isEmpty()) return false;
        if (search.length() > 24) return true;
        search += s;
        searchFocus = true;
        scrollTarget = 0;
        listChangedAt = System.currentTimeMillis();
        return true;
    }

    // Mouse clicks while binding: bind mouse buttons 3/4 (side buttons)
    public boolean bindMouse(int button) {
        if (binding == null || button < 2) return false;
        binding.key = button;
        binding = null;
        Modules.scheduleSave();
        return true;
    }
}
