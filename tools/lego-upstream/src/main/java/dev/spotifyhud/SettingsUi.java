package dev.spotifyhud;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of the settings card (design units 360 x 230). Built fresh every frame; the painter draws it and the
 * screen hit-tests it. No AWT / Minecraft classes here.
 */
final class SettingsUi {
    static final double CW = 360, CH = 230;
    static final String[] TABS = {"Design", "Animation", "Lyrics", "HUD", "Konto"};

    enum Kind { TAB, CLOSE, THEME, ANIM, TOGGLE, SLIDER, BUTTON, FIELD }

    static final class W {
        final String id;
        final Kind kind;
        final double x, y, w, h;
        String label = "";
        String sub = "";          // secondary text (value / description)
        String ref = "";          // theme id / anim id
        boolean on, selected, enabled = true;
        double value;             // sliders 0..1
        W(String id, Kind kind, double x, double y, double w, double h) {
            this.id = id; this.kind = kind; this.x = x; this.y = y; this.w = w; this.h = h;
        }
        boolean contains(double px, double py) { return px >= x && py >= y && px < x + w && py < y + h; }
    }

    int tab = 0;
    String hover;
    String focused;              // text field with keyboard focus
    String dragging;             // slider being dragged
    boolean selectAll;
    String clientIdText = "";
    String portText = "";

    // slider mappings ---------------------------------------------------------------
    static double scaleToSlider(double s) { return (s - 0.5) / 2.5; }
    static double sliderToScale(double v) { return Math.round((0.5 + v * 2.5) * 20) / 20.0; }
    static double offsetToSlider(int ms) { return (ms + 2000) / 4000.0; }
    static int sliderToOffset(double v) { return (int) (Math.round((v * 4000 - 2000) / 25.0) * 25); }
    static double intensityToSlider(double i) { return (i - 0.2) / 0.8; }
    static double sliderToIntensity(double v) { return Math.round((0.2 + v * 0.8) * 20) / 20.0; }

    List<W> build(Config cfg, SpotifyApi api) {
        List<W> l = new ArrayList<>();
        W close = new W("close", Kind.CLOSE, CW - 30, 9, 20, 20);
        l.add(close);
        for (int i = 0; i < TABS.length; i++) {
            W t = new W("tab" + i, Kind.TAB, 12, 46 + i * 27, 82, 23);
            t.label = TABS[i];
            t.selected = tab == i;
            l.add(t);
        }
        double cx = 108, cw = 240;
        switch (tab) {
            case 0: { // Design
                for (int i = 0; i < Theme.IDS.length; i++) {
                    int col = i % 4, row = i / 4;
                    W c = new W("theme:" + Theme.IDS[i], Kind.THEME, cx + col * 61, 64 + row * 58, 56, 52);
                    c.label = Theme.NAMES[i];
                    c.ref = Theme.IDS[i];
                    c.selected = Theme.IDS[i].equals(cfg.theme);
                    l.add(c);
                }
                break;
            }
            case 1: { // Animation
                for (int i = 0; i < Anim.IDS.length; i++) {
                    int col = i % 4, row = i / 4;
                    W c = new W("anim:" + Anim.IDS[i], Kind.ANIM, cx + col * 61, 62 + row * 48, 56, 44);
                    c.label = Anim.NAMES[i];
                    c.ref = Anim.IDS[i];
                    c.selected = Anim.IDS[i].equals(cfg.animation);
                    l.add(c);
                }
                W s = new W("intensity", Kind.SLIDER, cx, 160, cw, 20);
                s.label = "Stärke";
                s.value = intensityToSlider(cfg.animIntensity);
                s.sub = Math.round(cfg.animIntensity * 100) + " %";
                l.add(s);
                W t = new W("animOnLyrics", Kind.TOGGLE, cx, 184, cw, 18);
                t.label = "Auch hinter den Lyrics";
                t.on = cfg.animOnLyrics;
                l.add(t);
                break;
            }
            case 2: { // Lyrics
                l.add(toggle("lyrics", "Lyrics anzeigen", "Taste K oder Rechtsklick aufs HUD", cfg.lyrics, cx, 56, cw));
                l.add(toggle("karaoke", "Wort-Highlight", "Die Zeile und das gesungene Wort leuchten", cfg.karaoke, cx, 84, cw));
                l.add(toggle("wordGrow", "Aktives Wort größer", "Das Wort wächst beim Singen etwas", cfg.wordGrow, cx, 112, cw));
                W s = new W("offset", Kind.SLIDER, cx, 142, cw, 20);
                s.label = "Sync";
                s.value = offsetToSlider(cfg.lyricsOffsetMs);
                s.sub = (cfg.lyricsOffsetMs > 0 ? "+" : "") + cfg.lyricsOffsetMs + " ms";
                l.add(s);
                W m = new W("off-", Kind.BUTTON, cx, 170, 56, 18); m.label = "-50 ms"; l.add(m);
                W z = new W("off0", Kind.BUTTON, cx + 60, 170, 56, 18); z.label = "Reset"; l.add(z);
                W p = new W("off+", Kind.BUTTON, cx + 120, 170, 56, 18); p.label = "+50 ms"; l.add(p);
                break;
            }
            case 3: { // HUD
                l.add(toggle("visible", "HUD anzeigen", "Blendet das komplette Spotify HUD ein/aus", cfg.visible, cx, 56, cw));
                l.add(toggle("hideIdle", "Ausblenden, wenn nichts läuft", "Nur sichtbar, solange ein Song erkannt wird", cfg.hideWhenIdle, cx, 84, cw));
                W s = new W("scale", Kind.SLIDER, cx, 120, cw, 20);
                s.label = "Größe";
                s.value = scaleToSlider(cfg.scale);
                s.sub = Math.round(cfg.scale * 100) + " %";
                l.add(s);
                W r = new W("reset", Kind.BUTTON, cx, 152, 150, 20);
                r.label = "Position & Größe zurücksetzen";
                l.add(r);
                break;
            }
            default: { // Konto
                W f = new W("clientId", Kind.FIELD, cx, 84, 176, 20);
                f.label = "Client-ID";
                f.sub = clientIdText;
                f.selected = "clientId".equals(focused);
                l.add(f);
                W p = new W("port", Kind.FIELD, cx + 182, 84, 58, 20);
                p.label = "Port";
                p.sub = portText;
                p.selected = "port".equals(focused);
                l.add(p);
                W login = new W("login", Kind.BUTTON, cx, 112, 76, 20);
                login.label = api.isLoggedIn() ? "Abmelden" : (api.authRunning() ? "Abbrechen" : "Anmelden");
                login.selected = !api.isLoggedIn() && !api.authRunning();
                l.add(login);
                W d = new W("dashboard", Kind.BUTTON, cx + 82, 112, 76, 20); d.label = "Dashboard"; l.add(d);
                W u = new W("copyUri", Kind.BUTTON, cx + 164, 112, 76, 20); u.label = "URI kopieren"; l.add(u);
            }
        }
        return l;
    }

    private static W toggle(String id, String label, String sub, boolean on, double x, double y, double w) {
        W t = new W(id, Kind.TOGGLE, x, y, w, 24);
        t.label = label;
        t.sub = sub;
        t.on = on;
        return t;
    }

    static W hit(List<W> ws, double x, double y) {
        for (int i = ws.size() - 1; i >= 0; i--) if (ws.get(i).contains(x, y)) return ws.get(i);
        return null;
    }
}
