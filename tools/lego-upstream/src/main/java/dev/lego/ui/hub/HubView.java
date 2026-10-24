package dev.lego.ui.hub;

import dev.lego.ui.Art;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Halloween;
import dev.lego.ui.Kit;
import dev.lego.ui.MenuView;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.Xmas;

/** The main client window: icon sidebar + switchable sections (Mods, Kosmetik, Emotes, Spiele, Design). */
public final class HubView extends View {
    public enum Section {
        MODS("mods", "MODS"), COSMETICS("shirt", "KOSMETIK"), EMOTES("emote", "EMOTES"), GAMES("gamepad", "SPIELE"),
        HUD("layout", "HUD"), DESIGN("palette", "DESIGN");
        public final String icon, label;
        Section(String icon, String label) { this.icon = icon; this.label = label; }
    }

    public interface Part {
        void draw(HubView v, int x, int y, int w, int h);
        default boolean key(int key, int mods) { return false; }
        default boolean chr(String s) { return false; }
        default void shown() { }
        default void tick() { }
    }

    private Section section;
    private Part part;
    private long switchedAt = System.currentTimeMillis();

    public HubView() { this(Section.MODS); }

    public HubView(Section s) { select(s); }

    public Section section() { return section; }

    public void select(Section s) {
        if (s == Section.HUD) {
            if (host != null) { host.closeThen(Env.openHudEditor); return; }
            s = Section.MODS;
        }
        if (s == section) return;
        section = s;
        switchedAt = System.currentTimeMillis();
        switch (s) {
            case COSMETICS: part = new CosmeticsPart(); break;
            case EMOTES: part = new EmotesPart(); break;
            case GAMES: part = new GamesPart(); break;
            case DESIGN: part = new DesignPart(); break;
            default: part = new ModsPart(); break;
        }
        part.shown();
    }

    /** 0..1 progress since the section was switched (for content animations). */
    public float sectionT(float ms) { return Math.min(1f, (System.currentTimeMillis() - switchedAt) * dev.lego.ui.UiSettings.anim() / ms); }

    @Override public float designW() { return 740; }
    @Override public float designH() { return 450; }
    @Override public float openMs() { return 380; }

    /** One glass window: shadow, translucent body, the chosen background animation inside, top light, rim. */
    private void glassPanel(int x, int y, int w, int h, int r, float t, boolean anim) {
        float glass = (float) dev.lego.ui.UiSettings.glass;
        Gx.shadow(x, y, w, h, r, p(16), 0.5f);
        Gx.rectV(x, y, w, h, r, Gx.withAlpha(Gx.mix(Style.bg, 0xFF2A2140, 0.22f), 0.94f * glass + 0.04f), Gx.withAlpha(Style.bg, 0.97f * glass + 0.03f));
        if (anim) dev.lego.ui.BgFx.drawIn(x + r / 3, y + r / 3, w - 2 * (r / 3), h - 2 * (r / 3), t * 0.9f);
        Gx.rectV(x, y, w, Math.min(h, p(70)), r, Gx.withAlpha(0xFFFFFFFF, 0.05f), 0x00FFFFFF);
        Gx.outline(x, y, w, h, r, Math.max(1, p(0.7)), Gx.withAlpha(0xFFFFFFFF, 0.12f));
    }

    @Override
    public void draw() {
        boolean closing = host != null && host.closing();
        float t = closing ? open : Ease.outQuint(open);
        Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.30f * open));
        int sw = p(70), gap = p(10), mw = p(652), hh = p(446);
        int total = sw + gap + mw;
        int x0 = (W - total) / 2, y0 = (H - hh) / 2;
        int cx = W / 2, cy = H / 2;
        int R = p(18);

        Gx.pushAlpha(t);
        Gx.push();
        Gx.scaleAt(cx, cy, 0.95f + 0.05f * t);
        Gx.translate(0, pf(10) * (1 - t));

        // ---------------------------------------------------------------- sidebar (floating, like the first version)
        Gx.push();
        Gx.translate(-pf(24) * (1 - t), 0);
        glassPanel(x0, y0, sw, hh, R, t, false);
        int logo = p(34);
        boolean hl = hover(x0, y0 + p(8), sw, p(46));
        float hlf = Ease.to("hub:logo", hl ? 1 : 0, 14);
        Gx.glow(x0 + sw / 2, y0 + p(14) + logo / 2, p(34), Gx.withAlpha(Style.accent, 0.10f + 0.12f * hlf));
        Gx.image(Art.brickImg(logo, Halloween.on() ? Halloween.ORANGE : Xmas.on() ? Xmas.RED : Style.accent), x0 + (sw - logo) / 2, y0 + p(14) - Math.round(pf(2) * hlf), logo, logo, 0xFFFFFFFF);
        if (Halloween.on()) Halloween.witchHat(x0 + sw / 2f + p(2), y0 + p(20) - pf(2) * hlf, p(30), 0);
        else Xmas.hat(x0 + sw / 2f + p(2), y0 + p(20) - pf(2) * hlf, p(30), 0);
        Kit.hit(x0, y0 + p(8), sw, p(46)).click(() -> host.open(new MenuView()));
        Gx.fill(x0 + p(16), y0 + p(60), sw - p(32), Math.max(1, p(0.6)), Gx.withAlpha(0xFFFFFFFF, 0.08f));
        Section[] items = Section.values();
        boolean modern = dev.lego.ui.UiSettings.guiStyle == 1;
        int itemH = p(56), iy = y0 + p(68);
        int active = section.ordinal();
        float ay = Ease.to("hub:side", iy + active * itemH, 18, iy + active * itemH);
        if (modern) {
            Gx.rect(x0 + p(20), Math.round(ay) + itemH - p(5), sw - p(40), Math.max(2, p(2)), p(1), Style.accent);
        } else {
            Gx.glow(x0 + sw / 2, Math.round(ay + itemH / 2f), p(38), Gx.withAlpha(Style.accent, 0.14f));
            Gx.rect(x0 + p(8), Math.round(ay) + p(3), sw - p(16), itemH - p(6), p(12), Gx.withAlpha(Style.accent, 0.18f));
            Gx.outline(x0 + p(8), Math.round(ay) + p(3), sw - p(16), itemH - p(6), p(12), Math.max(1, p(0.6)), Gx.withAlpha(Style.accent, 0.35f));
            Gx.rect(x0 + p(2), Math.round(ay) + p(16), p(3), itemH - p(32), p(2), Style.accent);
        }
        for (int i = 0; i < items.length; i++) {
            Section s = items[i];
            int y = iy + i * itemH;
            boolean h = hover(x0, y, sw, itemH);
            float hv = Ease.to("hub:si" + i, h ? 1 : 0, 14);
            boolean act = s == section;
            if (modern) {
                int col = act ? Style.accent : Gx.mix(Style.muted, Style.text, hv);
                Gx.icon(s.icon, x0 + sw / 2f, y + itemH / 2f - pf(1) * hv, pf(20 + hv), col);
            } else {
                int col = act ? Style.accent : Gx.mix(Style.sub, Style.text, hv);
                Gx.icon(s.icon, x0 + sw / 2f, y + p(21) - pf(1.5f) * hv, pf(18 + hv * 1.5f), col);
                Gx.textCenter(s.label, x0 + sw / 2f, y + p(41), pf(6.1f), Fonts.BOLD, act ? Style.text : Gx.mix(Style.muted, Style.sub, hv));
            }
            Kit.hit(x0, y, sw, itemH).click(() -> select(s));
        }
        int by = y0 + hh - p(48);
        boolean hc = hover(x0 + p(12), by, sw - p(24), p(36));
        float hcf = Ease.to("hub:close", hc ? 1 : 0, 16);
        Gx.rect(x0 + p(12), by, sw - p(24), p(36), p(12), Gx.withAlpha(Style.danger, 0.18f * hcf));
        Gx.icon("x", x0 + sw / 2f, by + p(18), pf(15), Gx.mix(Style.muted, Style.danger, hcf));
        Kit.hit(x0 + p(12), by, sw - p(24), p(36)).click(() -> host.close());
        if (Halloween.on()) Halloween.cobweb(x0, y0, sw, P);
        else Xmas.snowCap(x0, y0, sw, P);
        Gx.pop();

        // ---------------------------------------------------------------- main window
        int mx0 = x0 + sw + gap;
        glassPanel(mx0, y0, mw, hh, R, t, true);
        // accent light gliding along the top edge (can be switched off on the Design page)
        if (dev.lego.ui.UiSettings.guiLine) {
        int lw = p(180);
        float sweep = (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 2600.0));
        int lx = mx0 + R + Math.round((mw - 2 * R - lw) * sweep);
        Gx.rect(lx, y0, lw, Math.max(1, p(1.4)), p(1), Gx.withAlpha(Style.accent, 0.85f));
        Gx.glow(lx + lw / 2, y0, p(60), Gx.withAlpha(Style.accent, 0.16f));
        }
        float st = Ease.outCubic(sectionT(260));
        Gx.pushAlpha(st);
        Gx.push();
        Gx.translate(0, pf(8) * (1 - st));
        part.draw(this, mx0, y0, mw, hh);
        Gx.pop();
        Gx.popAlpha();
        if (Halloween.on()) { Halloween.cobweb(mx0, y0, mw, P); Halloween.lanterns(x0 - p(10), y0 - p(34), mx0 + mw + p(10), P, p(14), 0); }
        else { Xmas.snowCap(mx0, y0, mw, P); Xmas.lights(x0 - p(10), y0 - p(34), mx0 + mw + p(10), P, p(14), 0); }

        Gx.pop();
        Gx.popAlpha();
    }
    @Override public boolean key(int key, int mods) { return part.key(key, mods); }
    @Override public boolean chr(String s) { return part.chr(s); }
    @Override public void tick() { part.tick(); }

    // ------------------------------------------------------------------ shared drawing helpers for parts
    /** Section title + subtitle at the top-left of the main panel; returns the y below it. */
    public int header(int x, int y, String title, String sub) {
        Gx.text(title, x, y, pf(17), Fonts.BOLD, Style.text);
        if (sub != null) Gx.text(sub, x, y + pf(24), pf(8.2f), Fonts.REGULAR, Style.sub);
        return y + p(sub != null ? 44 : 28);
    }

    /** Search field; returns its hit region. */
    public void searchBox(String key, int x, int y, int w, int h, String text, boolean focus) {
        searchBox(key, x, y, w, h, text, focus, "Suchen…");
    }

    /** Search field with custom placeholder text. */
    public void searchBox(String key, int x, int y, int w, int h, String text, boolean focus, String placeholder) {
        boolean hv = hover(x, y, w, h);
        float f = Ease.to("sb:" + key, focus ? 1 : (hv ? 0.4f : 0), 14);
        Gx.rect(x, y, w, h, h / 2, Gx.mix(Style.surface, Style.surface2, f));
        Gx.outline(x, y, w, h, h / 2, Math.max(1, p(0.7)), Gx.mix(Style.stroke, Style.accent, f));
        Gx.icon("search", x + p(15), y + h / 2f, pf(11), Gx.mix(Style.muted, Style.accent, f));
        float fs = pf(8.4f);
        if (text.isEmpty()) {
            Gx.textMid(placeholder, x + p(27), y + h / 2f, fs, Fonts.REGULAR, Style.muted);
        } else {
            String shown = Fonts.ellipsize(text, Fonts.MEDIUM, fs, w - p(50));
            float tw = Gx.textMid(shown, x + p(27), y + h / 2f, fs, Fonts.MEDIUM, Style.text);
            if (focus && (System.currentTimeMillis() / 500) % 2 == 0) Gx.fill(Math.round(x + p(28) + tw), y + h / 2 - p(6), Math.max(1, p(0.9)), p(12), Style.accent);
            boolean hx = hover(x + w - h, y, h, h);
            Gx.icon("x", x + w - h / 2f, y + h / 2f, pf(9), hx ? Style.text : Style.muted);
        }
    }

    /** Tabs (pill style with sliding indicator). Returns nothing; calls onPick(i). */
    public void tabs(String key, int x, int y, String[] labels, int selected, java.util.function.IntConsumer onPick) {
        float fs = pf(7.8f);
        float sp = pf(1.1f);
        int padX = p(12), h = p(26), gap = p(4);
        int[] xs = new int[labels.length], ws = new int[labels.length];
        int cx = x;
        for (int i = 0; i < labels.length; i++) {
            int w = Math.round(Gx.spacedWidth(labels[i], fs, Fonts.BOLD, sp)) + padX * 2;
            xs[i] = cx;
            ws[i] = w;
            cx += w + gap;
        }
        float ax = Ease.to("tabx:" + key, xs[selected], 18, xs[selected]);
        float aw = Ease.to("tabw:" + key, ws[selected], 18, ws[selected]);
        Gx.rect(Math.round(ax), y, Math.round(aw), h, h / 2, Style.accent);
        for (int i = 0; i < labels.length; i++) {
            boolean hv = hover(xs[i], y, ws[i], h) && i != selected;
            float hvv = Ease.to("tabh:" + key + i, hv ? 1 : 0, 14);
            if (hvv > 0.01f) Gx.rect(xs[i], y, ws[i], h, h / 2, Gx.withAlpha(Style.surfaceHover, hvv));
            float sel = Math.max(0, 1 - Math.abs(ax - xs[i]) / Math.max(1, ws[i]));
            int col = Gx.mix(Gx.mix(Style.sub, Style.text, hvv), Style.accentText, sel);
            Gx.textSpaced(labels[i], xs[i] + ws[i] / 2f, y + h / 2f, fs, Fonts.BOLD, col, sp);
            final int idx = i;
            Kit.hit(xs[i], y, ws[i], h).click(() -> onPick.accept(idx));
        }
    }
}
