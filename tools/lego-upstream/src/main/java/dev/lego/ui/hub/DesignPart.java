package dev.lego.ui.hub;

import dev.lego.core.Lang;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.BgImage;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;

/** Themes, accent color, HUD look and menu options. */
public final class DesignPart implements HubView.Part {
    private float scroll = 0, target = 0, max = 0;

    @Override
    public void draw(HubView v, int x, int y, int w, int h) {
        int pad = v.p(24);
        int top = v.header(x + pad, y + v.p(20), "Design & Client", Lang.get("ui.design.sub", "Thema, Farbe, HUD, UI-Größe und Animations-Tempo"));
        int ly = top + v.p(4), lh = y + h - v.p(14) - ly;
        max = Math.max(0, contentH(v) - lh);
        target = Math.max(0, Math.min(max, target));
        scroll += (target - scroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(x, ly, w, lh);
        Kit.clip(x, ly, w, lh);
        Kit.hit(x, ly, w, lh).scroll(a -> target -= a.floatValue() * v.p(44));
        int cy = Math.round(ly - scroll);
        int cw = w - pad * 2;

        // ---------------------------------------------------------------- click gui: background animation + glass
        cy = label(v, Lang.get("ui.d.bg", "CLICK-GUI HINTERGRUND"), x + pad, cy);
        {
            String[] names = dev.lego.ui.BgFx.NAMES;
            int bc = 5, bg = v.p(8), bw = (cw - bg * (bc - 1)) / bc, bh = v.p(30);
            for (int i = 0; i < names.length; i++) {
                int bx = x + pad + (i % bc) * (bw + bg), by = cy + (i / bc) * (bh + bg);
                boolean sel = UiSettings.bgStyle == i, hv = v.hover(bx, by, bw, bh);
                float hf = Ease.to("bgs:" + i, sel ? 1 : (hv ? 0.45f : 0), 16);
                Gx.rect(bx, by, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(0xFFFFFFFF, 0.05f), Style.accent, hf * (sel ? 1f : 0.35f)));
                Gx.outline(bx, by, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(0xFFFFFFFF, 0.10f));
                Gx.textCenter(names[i], bx + bw / 2f, by + bh / 2f, v.pf(8), Fonts.BOLD, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
                final int idx = i;
                Kit.hit(bx, by, bw, bh).click(() -> { UiSettings.bgStyle = idx; Modules.scheduleSave(); });
            }
            cy += ((names.length + bc - 1) / bc) * (bh + bg) + v.p(4);
        }
        cy = sliderRow(v, Lang.get("ui.d.glass", "Glas-Deckkraft"), "glass", (UiSettings.glass - 0.15) / 0.85, Math.round(UiSettings.glass * 100) + "%",
                f -> UiSettings.glass = Math.round((0.15 + f * 0.85) * 100) / 100.0, x + pad, cy, cw, v.p(44)) + v.p(8);
        cy = toggleRow(v, Lang.get("ui.d.line", "Leuchtende Linie oben am Fenster"), "guiline", UiSettings.guiLine, () -> UiSettings.guiLine = !UiSettings.guiLine, x + pad, cy, cw, v.p(44)) + v.p(8);
        cy = sliderRow(v, Lang.get("ui.d.bgint", "Animations-Stärke"), "bgint", (UiSettings.bgIntensity - 0.1) / 0.9, Math.round(UiSettings.bgIntensity * 100) + "%",
                f -> UiSettings.bgIntensity = Math.round((0.1 + f * 0.9) * 100) / 100.0, x + pad, cy, cw, v.p(44)) + v.p(8);
        // GUI style
        {
            String[] styles = UiSettings.GUI_STYLES;
            int bc = styles.length, bg = v.p(8), bw = (cw - bg * (bc - 1)) / bc, bh = v.p(30);
            for (int i = 0; i < styles.length; i++) {
                int bx = x + pad + i * (bw + bg), by = cy;
                boolean sel = UiSettings.guiStyle == i, hv = v.hover(bx, by, bw, bh);
                float hf = Ease.to("guis:" + i, sel ? 1 : (hv ? 0.45f : 0), 16);
                Gx.rect(bx, by, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(0xFFFFFFFF, 0.05f), Style.accent, hf * (sel ? 1f : 0.35f)));
                Gx.outline(bx, by, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(0xFFFFFFFF, 0.10f));
                Gx.textCenter(styles[i], bx + bw / 2f, by + bh / 2f, v.pf(8), Fonts.BOLD, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
                final int idx = i;
                Kit.hit(bx, by, bw, bh).click(() -> { UiSettings.guiStyle = idx; Modules.scheduleSave(); });
            }
            cy += bh + v.p(12);
        }
        // language
        {
            String[] langs = dev.lego.core.Lang.LANGUAGES;
            int bc = langs.length, bg = v.p(8), bw = (cw - bg * (bc - 1)) / bc, bh = v.p(30);
            Gx.icon("globe", x + pad, cy + bh / 2f, v.pf(10), Style.sub);
            Gx.textMid("Sprache / Language", x + pad + v.p(16), cy + v.pf(bh / 2f), v.pf(8), Fonts.MEDIUM, Style.text);
            cy += bh + v.p(4);
            for (int i = 0; i < langs.length; i++) {
                int bx = x + pad + i * (bw + bg), by = cy;
                boolean sel = UiSettings.language == i, hv = v.hover(bx, by, bw, bh);
                float hf = Ease.to("lang:" + i, sel ? 1 : (hv ? 0.45f : 0), 16);
                Gx.rect(bx, by, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(0xFFFFFFFF, 0.05f), Style.accent, hf * (sel ? 1f : 0.35f)));
                Gx.outline(bx, by, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(0xFFFFFFFF, 0.10f));
                Gx.textCenter(langs[i], bx + bw / 2f, by + bh / 2f, v.pf(8), Fonts.BOLD, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
                final int idx = i;
                Kit.hit(bx, by, bw, bh).click(() -> { UiSettings.language = idx; dev.lego.core.Lang.setLanguage(idx); dev.lego.ui.BgFx.updateNames(); dev.lego.ui.Style.updateThemeNames(); dev.lego.core.Setting.Color.updateNames(); Modules.scheduleSave(); });
            }
            cy += bh + v.p(12);
        }
        // background image
        {
            String lbl = UiSettings.bgImage.isEmpty() ? Lang.get("ui.d.nobg", "Kein Hintergrundbild") : UiSettings.bgImage;
            int bh = v.p(30);
            Gx.rect(x + pad, cy, cw, bh, v.p(8), Gx.withAlpha(0xFFFFFFFF, 0.05f));
            Gx.outline(x + pad, cy, cw, bh, v.p(8), Math.max(1, v.p(0.6)), Style.stroke);
            Gx.icon("image", x + pad + v.p(12), cy + bh / 2f, v.pf(12), Style.sub);
            Gx.textMid(lbl, x + pad + v.p(28), cy + v.pf(bh / 2f), v.pf(7.5f), Fonts.MEDIUM, Style.text);
            boolean hv = v.hover(x + pad, cy, cw, bh);
            if (hv) Gx.rect(x + pad, cy, cw, bh, v.p(8), Gx.withAlpha(Style.accent, 0.08f));
            Kit.hit(x + pad, cy, cw, bh).click(() -> BgImage.pick());
            if (!UiSettings.bgImage.isEmpty()) {
                int cbw = v.p(24);
                boolean ch = v.hover(x + pad + cw - cbw - v.p(4), cy + v.p(3), cbw, bh - v.p(6));
                Gx.icon("x", x + pad + cw - cbw / 2f - v.p(4), cy + bh / 2f, v.pf(10), ch ? Style.danger : Style.sub);
                Kit.hit(x + pad + cw - cbw - v.p(4), cy + v.p(3), cbw, bh - v.p(6)).click(() -> { UiSettings.bgImage = ""; BgImage.clear(); Modules.scheduleSave(); });
            }
            cy += bh + v.p(8);
        }
        cy += v.p(12);
        // ---------------------------------------------------------------- themes
        cy = label(v, Lang.get("ui.d.theme", "THEMA"), x + pad, cy);
        int cols = 4, gap = v.p(10);
        int tw = (cw - gap * (cols - 1)) / cols, th = v.p(66);
        for (int i = 0; i < Style.THEMES.length; i++) {
            int tx = x + pad + (i % cols) * (tw + gap), ty = cy + (i / cols) * (th + v.p(24));
            themeCard(v, i, tx, ty, tw, th);
        }
        cy += ((Style.THEMES.length + cols - 1) / cols) * (th + v.p(24)) + v.p(8);

        // ---------------------------------------------------------------- accent
        cy = label(v, Lang.get("ui.d.accent", "AKZENTFARBE"), x + pad, cy);
        int d = v.p(26), g = v.p(12);
        for (int i = 0; i < Setting.Color.PALETTE.length; i++) {
            int px = x + pad + i * (d + g);
            boolean sel = Modules.accent == i;
            boolean hs = v.hover(px, cy, d, d);
            float sc = Ease.to("acc:" + i, sel ? 1.12f : (hs ? 1.08f : 1f), 16);
            Gx.push();
            Gx.scaleAt(px + d / 2f, cy + d / 2f, sc);
            if (i == Setting.Color.PALETTE.length - 1) Gx.image(ModsPart.rainbow(d), px, cy, d, d, 0xFFFFFFFF);
            else Gx.circle(px + d / 2, cy + d / 2, d / 2, Setting.Color.PALETTE[i]);
            if (sel) {
                Gx.ringAt(px + d / 2, cy + d / 2, d / 2 + v.p(4), Math.max(1, v.p(1.6)), Style.text);
                Gx.icon("check", px + d / 2f, cy + d / 2f, v.pf(12), Style.brightness(Setting.Color.PALETTE[i]) > 0.6 ? 0xFF111111 : 0xFFFFFFFF);
            }
            Gx.pop();
            final int idx = i;
            Kit.hit(px - g / 2, cy - v.p(4), d + g, d + v.p(8)).click(() -> { Modules.accent = idx; Modules.scheduleSave(); });
        }
        cy += d + v.p(22);

        // ---------------------------------------------------------------- HUD
        cy = label(v, "HUD", x + pad, cy);
        int rh = v.p(44), rg = v.p(8);
        rowBg(v, x + pad, cy, cw, rh, "hudstyle");
        Gx.textMid(Lang.get("ui.d.hudstyle", "HUD-Stil"), x + pad + v.p(16), cy + rh / 2f, v.pf(9), Fonts.MEDIUM, Style.text);
        segmented(v, "hudstyle", x + pad + cw - v.p(16), cy + rh / 2, UiSettings.HUD_STYLES, UiSettings.hudStyle, i -> UiSettings.hudStyle = i);
        cy += rh + rg;
        // opacity
        rowBg(v, x + pad, cy, cw, rh, "hudop");
        Gx.textMid(Lang.get("ui.d.hudop", "Hintergrund-Deckkraft"), x + pad + v.p(16), cy + rh / 2f, v.pf(9), Fonts.MEDIUM, Style.text);
        int bw = v.p(58), bh = v.p(22), right = x + pad + cw - v.p(16);
        Gx.rect(right - bw, cy + rh / 2 - bh / 2, bw, bh, v.p(7), Style.surface2);
        Gx.textCenter(Math.round(UiSettings.hudOpacity * 100) + "%", right - bw / 2f, cy + rh / 2f, v.pf(8.2f), Fonts.BOLD, Style.text);
        int sw = v.p(220), sx = right - bw - v.p(14) - sw;
        final int fcy = cy;
        Kit.slider("hudop", sx, cy + rh / 2 - v.p(9), sw, v.p(18), (float) UiSettings.hudOpacity, v.P);
        Kit.hit(sx - v.p(6), fcy, sw + v.p(12), rh).drag((px, py) -> {
            UiSettings.hudOpacity = Math.round(Math.max(0, Math.min(1, (px - sx) / (double) sw)) * 100) / 100.0;
            Modules.scheduleSave();
        });
        cy += rh + rg;
        cy = toggleRow(v, Lang.get("ui.d.hudicons", "Icons in der HUD"), "hudicons", UiSettings.hudIcons, () -> UiSettings.hudIcons = !UiSettings.hudIcons, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.shadow", "Schatten"), "hudshadow", UiSettings.hudShadow, () -> UiSettings.hudShadow = !UiSettings.hudShadow, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.accentline", "Akzent-Linie links"), "hudline", UiSettings.hudAccentLine, () -> UiSettings.hudAccentLine = !UiSettings.hudAccentLine, x + pad, cy, cw, rh) + rg;
        cy += v.p(12);

        // ---------------------------------------------------------------- menu
        cy = label(v, Lang.get("ui.d.menu", "MENÜ"), x + pad, cy);
        cy = toggleRow(v, Lang.get("ui.d.sounds", "Klick-Sounds"), "sounds", UiSettings.sounds, () -> { UiSettings.sounds = !UiSettings.sounds; Sound.uiSounds = UiSettings.sounds; }, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.blur", "Hintergrund verschwimmen (Blur)"), "blur", UiSettings.blur, () -> UiSettings.blur = !UiSettings.blur, x + pad, cy, cw, rh) + rg;

        cy += v.p(12);
        // ---------------------------------------------------------------- client
        cy = label(v, "CLIENT", x + pad, cy);
        cy = sliderRow(v, Lang.get("ui.d.uiscale", "UI-Größe"), "uiscale", (UiSettings.uiScale - 0.6) / 0.7, Math.round(UiSettings.uiScale * 100) + "%",
                f -> UiSettings.uiScale = Math.round((0.6 + f * 0.7) * 20) / 20.0, x + pad, cy, cw, rh) + rg;
        cy = sliderRow(v, Lang.get("ui.d.animspeed", "Animations-Tempo"), "animspeed", (UiSettings.animSpeed - 0.5) / 2.5, String.format(java.util.Locale.ROOT, "%.1fx", UiSettings.animSpeed),
                f -> UiSettings.animSpeed = Math.round((0.5 + f * 2.5) * 10) / 10.0, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.anims", "Animationen"), "anims", UiSettings.animations, () -> UiSettings.animations = !UiSettings.animations, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.intro", "Start-Animation (Lego-Steine)"), "intro", UiSettings.intro, () -> UiSettings.intro = !UiSettings.intro, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.xmas", "Weihnachts-Deko (Schnee, Lichter, Mützen)"), "xmas", UiSettings.xmas, () -> UiSettings.xmas = !UiSettings.xmas, x + pad, cy, cw, rh) + rg;
        cy = toggleRow(v, Lang.get("ui.d.halloween", "Halloween-Deko (Fledermäuse, Kürbisse, Nebel)"), "halloween", UiSettings.halloween, () -> UiSettings.halloween = !UiSettings.halloween, x + pad, cy, cw, rh) + rg;

        Kit.unclip();
        Gx.unclip();
    }

    private int sliderRow(HubView v, String label, String key, double frac, String value, java.util.function.DoubleConsumer set, int x, int y, int w, int h) {
        rowBg(v, x, y, w, h, key);
        Gx.textMid(label, x + v.p(16), y + h / 2f, v.pf(9), Fonts.MEDIUM, Style.text);
        int bw = v.p(58), bh = v.p(22), right = x + w - v.p(16);
        Gx.rect(right - bw, y + h / 2 - bh / 2, bw, bh, v.p(7), Style.surface2);
        Gx.textCenter(value, right - bw / 2f, y + h / 2f, v.pf(8.2f), Fonts.BOLD, Style.text);
        int sw = v.p(220), sx = right - bw - v.p(14) - sw;
        Kit.slider(key, sx, y + h / 2 - v.p(9), sw, v.p(18), (float) Math.max(0, Math.min(1, frac)), v.P);
        Kit.hit(sx - v.p(6), y, sw + v.p(12), h).drag((px, py) -> {
            set.accept(Math.max(0, Math.min(1, (px - sx) / (double) sw)));
            Modules.scheduleSave();
        });
        return y + h;
    }

    private int contentH(HubView v) {
        int rh = v.p(44), rg = v.p(8);
        int bgRows = (dev.lego.ui.BgFx.NAMES.length + 4) / 5;
        int themeRows = (Style.THEMES.length + 3) / 4;
        int h = 0;
        h += v.p(22) + bgRows * (v.p(30) + v.p(8)) + v.p(4);
        h += rh + v.p(8);
        h += rh + v.p(8);
        h += rh + v.p(8);
        h += v.p(30) + v.p(12);
        h += v.p(30) + v.p(8);
        h += v.p(12);
        h += v.p(22) + themeRows * (v.p(66) + v.p(24)) + v.p(8);
        h += v.p(22) + v.p(26) + v.p(22);
        h += v.p(22) + 5 * (rh + rg);
        h += v.p(12);
        h += v.p(22) + 2 * (rh + rg);
        h += v.p(12);
        h += v.p(22) + 6 * (rh + rg);
        h += v.p(60);
        return h;
    }

    private int label(HubView v, String s, int x, int y) {
        Gx.textSpaced(s, x + Gx.spacedWidth(s, v.pf(7.2f), Fonts.BOLD, v.pf(1.4f)) / 2f, y + v.pf(7), v.pf(7.2f), Fonts.BOLD, Style.muted, v.pf(1.4f));
        return y + v.p(22);
    }

    private void rowBg(HubView v, int x, int y, int w, int h, String key) {
        boolean modern = UiSettings.guiStyle == 1;
        float hov = Ease.to("drow:" + key, v.hover(x, y, w, h) ? 1 : 0, 14);
        if (modern) {
            Gx.outline(x, y, w, h, v.p(12), Math.max(1, v.p(0.6)), Gx.mix(Style.stroke, Style.strokeHi, hov));
        } else {
            Gx.rect(x, y, w, h, v.p(10), Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6f));
        }
    }

    private int toggleRow(HubView v, String label, String key, boolean on, Runnable flip, int x, int y, int w, int h) {
        rowBg(v, x, y, w, h, key);
        Gx.textMid(label, x + v.p(16), y + h / 2f, v.pf(9), Fonts.MEDIUM, Style.text);
        int tw = v.p(32), th = v.p(18);
        Kit.toggle("d:" + key, x + w - v.p(16) - tw, y + (h - th) / 2, tw, th, on, v.P);
        Kit.hit(x, y, w, h).click(() -> { flip.run(); Modules.scheduleSave(); });
        return y + h;
    }

    private void segmented(HubView v, String key, int right, int cy, String[] opts, int sel, java.util.function.IntConsumer pick) {
        float fs = v.pf(7.8f);
        int pad = v.p(12), h = v.p(26);
        int total = 0;
        int[] ws = new int[opts.length];
        for (int i = 0; i < opts.length; i++) { ws[i] = Math.round(Gx.width(opts[i], fs, Fonts.MEDIUM)) + pad * 2; total += ws[i]; }
        int x = right - total;
        Gx.rect(x - v.p(3), cy - h / 2 - v.p(3), total + v.p(6), h + v.p(6), v.p(9), Style.surface2);
        int[] xs = new int[opts.length];
        int ox = x;
        for (int i = 0; i < opts.length; i++) { xs[i] = ox; ox += ws[i]; }
        float ax = Ease.to("dseg:" + key, xs[sel], 18, xs[sel]), aw = Ease.to("dsegw:" + key, ws[sel], 18, ws[sel]);
        Gx.rect(Math.round(ax), cy - h / 2, Math.round(aw), h, v.p(7), Style.accent);
        for (int i = 0; i < opts.length; i++) {
            Gx.textCenter(opts[i], xs[i] + ws[i] / 2f, cy, fs, Fonts.MEDIUM, i == sel ? Style.accentText : Style.sub);
            final int idx = i;
            Kit.hit(xs[i], cy - h / 2, ws[i], h).click(() -> { pick.accept(idx); Modules.scheduleSave(); });
        }
    }

    private void themeCard(HubView v, int i, int x, int y, int w, int h) {
        boolean modern = UiSettings.guiStyle == 1;
        String id = Style.THEMES[i];
        boolean sel = id.equals(Modules.theme);
        boolean hv = v.hover(x, y, w, h);
        float hov = Ease.to("theme:" + i, hv ? 1 : 0, 14);
        int[] pal = Style.palette(id);
        if (!modern) { Gx.push(); Gx.translate(0, -v.pf(2) * hov); }
        if (!modern) Gx.shadow(x, y, w, h, v.p(11), v.p(6), 0.3f + 0.2f * hov);
        Gx.rect(x, y, w, h, v.p(11), pal[0] | 0xFF000000);
        if ("glass".equals(id)) Gx.rectV(x, y, w, h, v.p(11), 0x33FFFFFF, 0x0DFFFFFF);
        // mini UI
        int ix = x + v.p(10), iy = y + v.p(10);
        Gx.rect(ix, iy, v.p(16), h - v.p(20), v.p(5), pal[1] | 0xFF000000);
        int cx = ix + v.p(22), cw = x + w - v.p(10) - cx;
        Gx.rect(cx, iy, cw, v.p(18), v.p(5), pal[1] | 0xFF000000);
        Gx.rect(cx + v.p(5), iy + v.p(6), cw / 2, v.p(3), v.p(2), pal[3]);
        Gx.rect(cx, iy + v.p(24), cw, h - v.p(44), v.p(5), pal[1] | 0xFF000000);
        Gx.rect(cx + cw - v.p(20), iy + v.p(30), v.p(14), v.p(8), v.p(4), Style.accent);
        Gx.rect(cx + v.p(5), iy + v.p(31), cw / 3, v.p(3), v.p(2), pal[4]);
        Gx.outline(x, y, w, h, v.p(modern ? 14 : 11), Math.max(1, v.p(sel ? 1.6 : 0.7)), sel ? Style.accent : Gx.mix(Style.stroke, Style.strokeHi, hov));
        if (!modern) Gx.pop();
        Gx.textCenter(Style.THEME_NAMES[i], x + w / 2f, y + h + v.pf(11), v.pf(8), sel ? Fonts.BOLD : Fonts.MEDIUM, sel ? Style.text : Style.sub);
        Kit.hit(x, y, w, h + v.p(20)).click(() -> { Modules.theme = id; Modules.scheduleSave(); });
    }
}
