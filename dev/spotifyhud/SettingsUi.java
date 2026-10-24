package dev.spotifyhud;

import java.util.ArrayList;
import java.util.List;

final class SettingsUi {
   static final double CW = 360.0;
   static final double CH = 230.0;
   static final String[] TABS = new String[]{"Design", "Animation", "Lyrics", "HUD", "Konto"};
   int tab = 0;
   String hover;
   String focused;
   String dragging;
   boolean selectAll;
   String clientIdText = "";
   String portText = "";

   static double scaleToSlider(double s) {
      return (s - 0.5) / 2.5;
   }

   static double sliderToScale(double v) {
      return Math.round((0.5 + v * 2.5) * 20.0) / 20.0;
   }

   static double offsetToSlider(int ms) {
      return (ms + 2000) / 4000.0;
   }

   static int sliderToOffset(double v) {
      return (int)(Math.round((v * 4000.0 - 2000.0) / 25.0) * 25L);
   }

   static double intensityToSlider(double i) {
      return (i - 0.2) / 0.8;
   }

   static double sliderToIntensity(double v) {
      return Math.round((0.2 + v * 0.8) * 20.0) / 20.0;
   }

   List<SettingsUi.W> build(Config cfg, SpotifyApi api) {
      List<SettingsUi.W> l = new ArrayList<>();
      SettingsUi.W close = new SettingsUi.W("close", SettingsUi.Kind.CLOSE, 330.0, 9.0, 20.0, 20.0);
      l.add(close);

      for (int i = 0; i < TABS.length; i++) {
         SettingsUi.W t = new SettingsUi.W("tab" + i, SettingsUi.Kind.TAB, 12.0, 46 + i * 27, 82.0, 23.0);
         t.label = TABS[i];
         t.selected = this.tab == i;
         l.add(t);
      }

      double cx = 108.0;
      double cw = 240.0;
      switch (this.tab) {
         case 0:
            for (int i = 0; i < Theme.IDS.length; i++) {
               int col = i % 4;
               int row = i / 4;
               SettingsUi.W c = new SettingsUi.W("theme:" + Theme.IDS[i], SettingsUi.Kind.THEME, cx + col * 61, 64 + row * 58, 56.0, 52.0);
               c.label = Theme.NAMES[i];
               c.ref = Theme.IDS[i];
               c.selected = Theme.IDS[i].equals(cfg.theme);
               l.add(c);
            }
            break;
         case 1: {
            for (int i = 0; i < Anim.IDS.length; i++) {
               int col = i % 4;
               int row = i / 4;
               SettingsUi.W c = new SettingsUi.W("anim:" + Anim.IDS[i], SettingsUi.Kind.ANIM, cx + col * 61, 62 + row * 48, 56.0, 44.0);
               c.label = Anim.NAMES[i];
               c.ref = Anim.IDS[i];
               c.selected = Anim.IDS[i].equals(cfg.animation);
               l.add(c);
            }

            SettingsUi.W s = new SettingsUi.W("intensity", SettingsUi.Kind.SLIDER, cx, 160.0, cw, 20.0);
            s.label = "Stärke";
            s.value = intensityToSlider(cfg.animIntensity);
            s.sub = Math.round(cfg.animIntensity * 100.0) + " %";
            l.add(s);
            SettingsUi.W t = new SettingsUi.W("animOnLyrics", SettingsUi.Kind.TOGGLE, cx, 184.0, cw, 18.0);
            t.label = "Auch hinter den Lyrics";
            t.on = cfg.animOnLyrics;
            l.add(t);
            break;
         }
         case 2: {
            l.add(toggle("lyrics", "Lyrics anzeigen", "Taste K oder Rechtsklick aufs HUD", cfg.lyrics, cx, 56.0, cw));
            l.add(toggle("karaoke", "Wort-Highlight", "Die Zeile und das gesungene Wort leuchten", cfg.karaoke, cx, 84.0, cw));
            l.add(toggle("wordGrow", "Aktives Wort größer", "Das Wort wächst beim Singen etwas", cfg.wordGrow, cx, 112.0, cw));
            SettingsUi.W s = new SettingsUi.W("offset", SettingsUi.Kind.SLIDER, cx, 142.0, cw, 20.0);
            s.label = "Sync";
            s.value = offsetToSlider(cfg.lyricsOffsetMs);
            s.sub = (cfg.lyricsOffsetMs > 0 ? "+" : "") + cfg.lyricsOffsetMs + " ms";
            l.add(s);
            SettingsUi.W m = new SettingsUi.W("off-", SettingsUi.Kind.BUTTON, cx, 170.0, 56.0, 18.0);
            m.label = "-50 ms";
            l.add(m);
            SettingsUi.W z = new SettingsUi.W("off0", SettingsUi.Kind.BUTTON, cx + 60.0, 170.0, 56.0, 18.0);
            z.label = "Reset";
            l.add(z);
            SettingsUi.W p = new SettingsUi.W("off+", SettingsUi.Kind.BUTTON, cx + 120.0, 170.0, 56.0, 18.0);
            p.label = "+50 ms";
            l.add(p);
            break;
         }
         case 3: {
            l.add(toggle("visible", "HUD anzeigen", "Blendet das komplette Spotify HUD ein/aus", cfg.visible, cx, 56.0, cw));
            l.add(toggle("hideIdle", "Ausblenden, wenn nichts läuft", "Nur sichtbar, solange ein Song erkannt wird", cfg.hideWhenIdle, cx, 84.0, cw));
            SettingsUi.W s = new SettingsUi.W("scale", SettingsUi.Kind.SLIDER, cx, 120.0, cw, 20.0);
            s.label = "Größe";
            s.value = scaleToSlider(cfg.scale);
            s.sub = Math.round(cfg.scale * 100.0) + " %";
            l.add(s);
            SettingsUi.W r = new SettingsUi.W("reset", SettingsUi.Kind.BUTTON, cx, 152.0, 150.0, 20.0);
            r.label = "Position & Größe zurücksetzen";
            l.add(r);
            break;
         }
         default: {
            SettingsUi.W f = new SettingsUi.W("clientId", SettingsUi.Kind.FIELD, cx, 84.0, 176.0, 20.0);
            f.label = "Client-ID";
            f.sub = this.clientIdText;
            f.selected = "clientId".equals(this.focused);
            l.add(f);
            SettingsUi.W p = new SettingsUi.W("port", SettingsUi.Kind.FIELD, cx + 182.0, 84.0, 58.0, 20.0);
            p.label = "Port";
            p.sub = this.portText;
            p.selected = "port".equals(this.focused);
            l.add(p);
            SettingsUi.W login = new SettingsUi.W("login", SettingsUi.Kind.BUTTON, cx, 112.0, 76.0, 20.0);
            login.label = api.isLoggedIn() ? "Abmelden" : (api.authRunning() ? "Abbrechen" : "Anmelden");
            login.selected = !api.isLoggedIn() && !api.authRunning();
            l.add(login);
            SettingsUi.W d = new SettingsUi.W("dashboard", SettingsUi.Kind.BUTTON, cx + 82.0, 112.0, 76.0, 20.0);
            d.label = "Dashboard";
            l.add(d);
            SettingsUi.W u = new SettingsUi.W("copyUri", SettingsUi.Kind.BUTTON, cx + 164.0, 112.0, 76.0, 20.0);
            u.label = "URI kopieren";
            l.add(u);
         }
      }

      return l;
   }

   private static SettingsUi.W toggle(String id, String label, String sub, boolean on, double x, double y, double w) {
      SettingsUi.W t = new SettingsUi.W(id, SettingsUi.Kind.TOGGLE, x, y, w, 24.0);
      t.label = label;
      t.sub = sub;
      t.on = on;
      return t;
   }

   static SettingsUi.W hit(List<SettingsUi.W> ws, double x, double y) {
      for (int i = ws.size() - 1; i >= 0; i--) {
         if (ws.get(i).contains(x, y)) {
            return ws.get(i);
         }
      }

      return null;
   }

   static enum Kind {
      TAB,
      CLOSE,
      THEME,
      ANIM,
      TOGGLE,
      SLIDER,
      BUTTON,
      FIELD;
   }

   static final class W {
      final String id;
      final SettingsUi.Kind kind;
      final double x;
      final double y;
      final double w;
      final double h;
      String label = "";
      String sub = "";
      String ref = "";
      boolean on;
      boolean selected;
      boolean enabled = true;
      double value;

      W(String id, SettingsUi.Kind kind, double x, double y, double w, double h) {
         this.id = id;
         this.kind = kind;
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }

      boolean contains(double px, double py) {
         return px >= this.x && py >= this.y && px < this.x + this.w && py < this.y + this.h;
      }
   }
}
