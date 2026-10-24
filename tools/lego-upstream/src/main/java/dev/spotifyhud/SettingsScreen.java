package dev.spotifyhud;

import java.awt.Color;
import java.util.List;

import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

/**
 * Settings (key H or the gear on the HUD). Custom-drawn card with tabs: Design, Animation, Lyrics, HUD, Konto.
 * The HUD stays visible and interactive behind it as live preview.
 * class_437 Screen, class_11909 Click, class_11908 KeyInput, class_11905 CharInput
 */
public final class SettingsScreen extends net.minecraft.class_437 {
    private static final int KEY_ESCAPE = 256, KEY_ENTER = 257, KEY_TAB = 258, KEY_BACKSPACE = 259, KEY_DELETE = 261,
            KEY_KP_ENTER = 335, KEY_A = 65, KEY_V = 86, KEY_C = 67, MOD_CONTROL = 0x2;

    private final Config cfg;
    private final Media media;
    private final SpotifyApi api;
    private final HudInteraction interaction;
    private final TexSlot slot = new TexSlot(class_2960.method_60655("spotifyhud", "settings_panel"));
    private final SettingsUi ui = new SettingsUi();
    private static int lastTab = 0;
    private int shownPort;

    public SettingsScreen(Config cfg, Media media, HudRenderer hud) {
        super(class_2561.method_43470("Spotify HUD"));
        this.cfg = cfg;
        this.media = media;
        this.api = media.api();
        this.interaction = new HudInteraction(cfg, hud);
        ui.tab = lastTab;
        ui.clientIdText = cfg.clientId == null ? "" : cfg.clientId;
        ui.portText = String.valueOf(cfg.redirectPort);
        shownPort = cfg.redirectPort;
    }

    @Override
    public void method_25426() { } // init(): everything is custom-drawn

    // ------------------------------------------------------------------ layout helpers
    private int[] card() {
        int sw = this.field_22789, sh = this.field_22790;
        double k = Math.min(1.0, Math.min((sw - 10) / SettingsUi.CW, (sh - 10) / SettingsUi.CH));
        int w = (int) Math.round(SettingsUi.CW * k), h = (int) Math.round(SettingsUi.CH * k);
        return new int[]{(sw - w) / 2, (sh - h) / 2, w, h};
    }

    private static boolean inside(double mx, double my, int[] b) {
        return mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3];
    }

    private static double dx(double mx, int[] b) { return (mx - b[0]) * SettingsUi.CW / b[2]; }
    private static double dy(double my, int[] b) { return (my - b[1]) * SettingsUi.CH / b[3]; }

    // ------------------------------------------------------------------ rendering
    @Override
    public void method_25420(class_332 ctx, int mouseX, int mouseY, float delta) { } // no blur

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        if (cfg.redirectPort != shownPort && !"port".equals(ui.focused)) {
            shownPort = cfg.redirectPort;
            ui.portText = String.valueOf(shownPort);
        }
        int sw = this.field_22789, sh = this.field_22790;
        ctx.method_25294(0, 0, sw, sh, 0x66000000);
        interaction.render(ctx, mouseX, mouseY, sw, sh);    // live preview (under the card)

        int[] b = card();
        List<SettingsUi.W> ws = ui.build(cfg, api);
        SettingsUi.W hov = inside(mouseX, mouseY, b) ? SettingsUi.hit(ws, dx(mouseX, b), dy(mouseY, b)) : null;
        ui.hover = hov != null ? hov.id : null;

        SettingsPainter.Info info = info();
        double t = HudRenderer.animTime();
        StringBuilder key = new StringBuilder(256);
        for (SettingsUi.W w : ws) key.append(w.id).append(w.on).append(w.selected).append(Math.round(w.value * 1000)).append(w.label).append(w.sub).append(';');
        key.append(ui.hover).append('|').append(ui.focused).append(ui.dragging).append(ui.selectAll).append('|')
                .append(info.detect).append(info.account).append(info.latency).append(info.redirect).append(info.caretOn)
                .append('|').append(cfg.theme).append(System.identityHashCode(info.art))
                .append('|').append(ui.tab == 1 ? HudRenderer.frame(t) : 0);

        class_310 mc = class_310.method_1551();
        int s = Math.max(1, mc.method_22683().method_4495());
        int pw = b[2] * s, ph = b[3] * s;
        try {
            slot.draw(ctx, mc, b[0], b[1], b[2], b[3], pw, ph, key.toString(),
                    () -> SettingsPainter.render(pw, ph, ui, ws, cfg, info, t));
        } catch (Throwable err) {
            // AWT not available: minimal fallback
            ctx.method_25294(b[0], b[1], b[0] + b[2], b[1] + b[3], 0xF0121212);
            ctx.method_25300(this.field_22793, "Spotify HUD - Einstellungen (Grafik nicht verfügbar)", sw / 2, b[1] + 10, 0xFFFFFFFF);
        }
    }

    private SettingsPainter.Info info() {
        SettingsPainter.Info i = new SettingsPainter.Info();
        SmtcBridge smtc = media.smtc();
        String src = media.sourceName();
        if (src != null) { i.detect = "Erkannt: " + src; i.detectColor = new Color(30, 215, 96); }
        else if (smtc.fatal() != null) { i.detect = "Windows-Erkennung: " + smtc.fatal(); i.detectColor = new Color(241, 94, 108); }
        else if (!smtc.available()) { i.detect = "Song-Erkennung startet..."; i.detectColor = new Color(255, 209, 102); }
        else { i.detect = "Kein Song erkannt - starte Spotify"; i.detectColor = new Color(255, 209, 102); }
        String st = api.status();
        if (api.isLoggedIn()) {
            i.account = System.currentTimeMillis() - api.statusTime() > 8000 ? "Verbunden - das Herz speichert Songs" : st;
            i.accountColor = new Color(30, 215, 96);
        } else if (api.authRunning()) {
            i.account = st;
            i.accountColor = new Color(255, 209, 102);
        } else {
            boolean recent = System.currentTimeMillis() - api.statusTime() < 60000;
            i.account = recent && st != null && !st.isEmpty() ? st : "Nicht verbunden";
            i.accountColor = recent ? new Color(255, 209, 102) : new Color(160, 160, 166);
        }
        i.latency = media.latencyMs();
        i.redirect = cfg.redirectUri();
        i.art = media.art();
        i.caretOn = ui.focused != null && (System.currentTimeMillis() / 530) % 2 == 0;
        return i;
    }

    // ------------------------------------------------------------------ mouse
    @Override
    public boolean method_25402(class_11909 click, boolean doubled) { // mouseClicked
        double mx = click.comp_4798(), my = click.comp_4799();
        int[] b = card();
        if (inside(mx, my, b)) {
            if (click.method_74245() != 0) return true;
            List<SettingsUi.W> ws = ui.build(cfg, api);
            SettingsUi.W w = SettingsUi.hit(ws, dx(mx, b), dy(my, b));
            if (w == null || w.kind != SettingsUi.Kind.FIELD) unfocus();
            if (w != null) activate(w, dx(mx, b));
            return true;
        }
        unfocus();
        if (interaction.mouseDown(mx, my, click.method_74245(), this.field_22789, this.field_22790)) return true;
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25403(class_11909 click, double ddx, double ddy) { // mouseDragged
        if (ui.dragging != null) {
            int[] b = card();
            List<SettingsUi.W> ws = ui.build(cfg, api);
            for (SettingsUi.W w : ws) if (w.id.equals(ui.dragging)) setSlider(w, dx(click.comp_4798(), b), false);
            return true;
        }
        if (interaction.mouseDrag(click.comp_4798(), click.comp_4799(), this.field_22789, this.field_22790)) return true;
        return super.method_25403(click, ddx, ddy);
    }

    @Override
    public boolean method_25406(class_11909 click) { // mouseReleased
        if (ui.dragging != null) {
            ui.dragging = null;
            cfg.save();
            return true;
        }
        if (interaction.mouseUp()) return true;
        return super.method_25406(click);
    }

    @Override
    public boolean method_25401(double mx, double my, double horizontal, double vertical) { // mouseScrolled
        int[] b = card();
        if (inside(mx, my, b)) {
            List<SettingsUi.W> ws = ui.build(cfg, api);
            SettingsUi.W w = SettingsUi.hit(ws, dx(mx, b), dy(my, b));
            if (w != null && w.kind == SettingsUi.Kind.SLIDER && vertical != 0) {
                double step = "offset".equals(w.id) ? 25 / 4000.0 : 0.02;
                applySlider(w.id, Math.max(0, Math.min(1, w.value + (vertical > 0 ? step : -step))));
                SpotifyHudMod.scheduleSave();
            }
            return true;
        }
        if (interaction.scroll(mx, my, vertical, this.field_22789, this.field_22790)) return true;
        return super.method_25401(mx, my, horizontal, vertical);
    }

    private void activate(SettingsUi.W w, double x) {
        switch (w.kind) {
            case CLOSE: this.method_25419(); return;
            case TAB: ui.tab = Integer.parseInt(w.id.substring(3)); lastTab = ui.tab; return;
            case THEME: cfg.theme = w.ref; cfg.save(); return;
            case ANIM: cfg.animation = w.ref; cfg.save(); return;
            case TOGGLE: toggle(w.id); cfg.save(); return;
            case SLIDER: ui.dragging = w.id; setSlider(w, x, false); return;
            case FIELD: ui.focused = w.id; ui.selectAll = false; return;
            case BUTTON: button(w.id); return;
            default:
        }
    }

    private void toggle(String id) {
        switch (id) {
            case "lyrics": cfg.lyrics = !cfg.lyrics; break;
            case "karaoke": cfg.karaoke = !cfg.karaoke; break;
            case "wordGrow": cfg.wordGrow = !cfg.wordGrow; break;
            case "visible": cfg.visible = !cfg.visible; break;
            case "hideIdle": cfg.hideWhenIdle = !cfg.hideWhenIdle; break;
            case "animOnLyrics": cfg.animOnLyrics = !cfg.animOnLyrics; break;
            default:
        }
    }

    private void setSlider(SettingsUi.W w, double x, boolean save) {
        double v = Math.max(0, Math.min(1, (x - w.x) / w.w));
        applySlider(w.id, v);
        if (save) cfg.save();
    }

    private void applySlider(String id, double v) {
        switch (id) {
            case "intensity": cfg.animIntensity = SettingsUi.sliderToIntensity(v); break;
            case "offset": cfg.lyricsOffsetMs = SettingsUi.sliderToOffset(v); break;
            case "scale": cfg.scale = SettingsUi.sliderToScale(v); break;
            default:
        }
    }

    private void button(String id) {
        switch (id) {
            case "off-": cfg.lyricsOffsetMs = Math.max(-5000, cfg.lyricsOffsetMs - 50); cfg.save(); break;
            case "off+": cfg.lyricsOffsetMs = Math.min(5000, cfg.lyricsOffsetMs + 50); cfg.save(); break;
            case "off0": cfg.lyricsOffsetMs = 0; cfg.save(); break;
            case "reset":
                cfg.posX = Config.DEFAULT_X;
                cfg.posY = Config.DEFAULT_Y;
                cfg.scale = 1.0;
                cfg.save();
                media.toast("Position & Größe zurückgesetzt");
                break;
            case "dashboard": open("https://developer.spotify.com/dashboard"); break;
            case "copyUri":
                applyPort();
                SpotifyHudMod.copyToClipboard(cfg.redirectUri());
                media.toast("Redirect-URI kopiert: " + cfg.redirectUri());
                break;
            case "login": {
                if (api.isLoggedIn()) { api.logout(); break; }
                if (api.authRunning()) { api.cancelLogin(); break; }
                cfg.clientId = ui.clientIdText.trim();
                applyPort();
                cfg.save();
                String url = api.startLogin();
                if (url != null) open(url);
                break;
            }
            default:
        }
    }

    private void applyPort() {
        try {
            int p = Integer.parseInt(ui.portText.trim());
            if (p >= 1024 && p <= 65535 && p != cfg.redirectPort) { cfg.redirectPort = p; cfg.save(); }
        } catch (NumberFormatException ignored) { }
        ui.portText = String.valueOf(cfg.redirectPort);
        shownPort = cfg.redirectPort;
    }

    private void unfocus() {
        if (ui.focused == null) return;
        if ("clientId".equals(ui.focused)) { cfg.clientId = ui.clientIdText.trim(); cfg.save(); }
        if ("port".equals(ui.focused)) applyPort();
        ui.focused = null;
        ui.selectAll = false;
    }

    private static void open(String url) {
        try {
            class_156.method_668().method_670(url);
        } catch (Throwable t) {
            SpotifyHudMod.LOG("Browser konnte nicht geöffnet werden: " + t);
        }
    }

    // ------------------------------------------------------------------ keyboard (text fields)
    private String text() { return "port".equals(ui.focused) ? ui.portText : ui.clientIdText; }

    private void setText(String s) {
        if ("port".equals(ui.focused)) ui.portText = s.length() > 5 ? s.substring(0, 5) : s;
        else ui.clientIdText = s.length() > 64 ? s.substring(0, 64) : s;
    }

    private boolean accept(char c) {
        if ("port".equals(ui.focused)) return c >= '0' && c <= '9';
        return Character.isLetterOrDigit(c);
    }

    @Override
    public boolean method_25404(class_11908 key) { // keyPressed
        if (ui.focused == null) return super.method_25404(key);
        int k = key.comp_4795();
        boolean ctrl = (key.comp_4797() & MOD_CONTROL) != 0;
        String t = text();
        if (k == KEY_ESCAPE || k == KEY_ENTER || k == KEY_KP_ENTER || k == KEY_TAB) { unfocus(); return true; }
        if (k == KEY_BACKSPACE || k == KEY_DELETE) {
            if (ui.selectAll) setText("");
            else if (!t.isEmpty()) setText(ctrl ? "" : t.substring(0, t.length() - 1));
            ui.selectAll = false;
            return true;
        }
        if (ctrl && k == KEY_A) { ui.selectAll = true; return true; }
        if (ctrl && k == KEY_C) { SpotifyHudMod.copyToClipboard(t); return true; }
        if (ctrl && k == KEY_V) {
            String clip = "";
            try { clip = class_310.method_1551().field_1774.method_1460(); } catch (Throwable ignored) { }
            StringBuilder sb = new StringBuilder();
            if (clip != null) for (char c : clip.trim().toCharArray()) if (accept(c)) sb.append(c);
            setText(ui.selectAll ? sb.toString() : t + sb);
            ui.selectAll = false;
            return true;
        }
        return true; // swallow other keys while typing (e.g. don't trigger key binds)
    }

    @Override
    public boolean method_25400(class_11905 chr) { // charTyped
        if (ui.focused == null) return super.method_25400(chr);
        String s = chr.method_74226();
        if (s == null) return true;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) if (accept(c)) sb.append(c);
        if (sb.length() == 0) return true;
        setText(ui.selectAll ? sb.toString() : text() + sb);
        ui.selectAll = false;
        return true;
    }

    @Override
    public boolean method_25421() { return false; } // shouldPause

    @Override
    public void method_25432() { // removed
        interaction.mouseUp();
        unfocus();
        cfg.clientId = ui.clientIdText.trim();
        applyPort();
        cfg.save();
    }
}
