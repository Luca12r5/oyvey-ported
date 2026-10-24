package dev.spotifyhud;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class SettingsScreen extends Screen {
   private static final int KEY_ESCAPE = 256;
   private static final int KEY_ENTER = 257;
   private static final int KEY_TAB = 258;
   private static final int KEY_BACKSPACE = 259;
   private static final int KEY_DELETE = 261;
   private static final int KEY_KP_ENTER = 335;
   private static final int KEY_A = 65;
   private static final int KEY_V = 86;
   private static final int KEY_C = 67;
   private static final int MOD_CONTROL = 2;
   private final Config cfg;
   private final Media media;
   private final SpotifyApi api;
   private final HudInteraction interaction;
   private final TexSlot slot = new TexSlot(Identifier.fromNamespaceAndPath("spotifyhud", "settings_panel"));
   private final SettingsUi ui = new SettingsUi();
   private static int lastTab = 0;
   private int shownPort;

   public SettingsScreen(Config cfg, Media media, HudRenderer hud) {
      super(Component.literal("Spotify HUD"));
      this.cfg = cfg;
      this.media = media;
      this.api = media.api();
      this.interaction = new HudInteraction(cfg, hud);
      this.ui.tab = lastTab;
      this.ui.clientIdText = cfg.clientId == null ? "" : cfg.clientId;
      this.ui.portText = String.valueOf(cfg.redirectPort);
      this.shownPort = cfg.redirectPort;
   }

   @Override
   public void init() {
   }

   private int[] card() {
      int sw = this.width;
      int sh = this.height;
      double k = Math.min(1.0, Math.min((sw - 10) / 360.0, (sh - 10) / 230.0));
      int w = (int)Math.round(360.0 * k);
      int h = (int)Math.round(230.0 * k);
      return new int[]{(sw - w) / 2, (sh - h) / 2, w, h};
   }

   private static boolean inside(double mx, double my, int[] b) {
      return mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3];
   }

   private static double dx(double mx, int[] b) {
      return (mx - b[0]) * 360.0 / b[2];
   }

   private static double dy(double my, int[] b) {
      return (my - b[1]) * 230.0 / b[3];
   }

   @Override
   public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
   }

   @Override
   public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
      if (this.cfg.redirectPort != this.shownPort && !"port".equals(this.ui.focused)) {
         this.shownPort = this.cfg.redirectPort;
         this.ui.portText = String.valueOf(this.shownPort);
      }

      int sw = this.width;
      int sh = this.height;
      ctx.fill(0, 0, sw, sh, 1711276032);
      this.interaction.render(ctx, mouseX, mouseY, sw, sh);
      int[] b = this.card();
      List<SettingsUi.W> ws = this.ui.build(this.cfg, this.api);
      SettingsUi.W hov = inside(mouseX, mouseY, b) ? SettingsUi.hit(ws, dx(mouseX, b), dy(mouseY, b)) : null;
      this.ui.hover = hov != null ? hov.id : null;
      SettingsPainter.Info info = this.info();
      double t = HudRenderer.animTime();
      StringBuilder key = new StringBuilder(256);

      for (SettingsUi.W w : ws) {
         key.append(w.id).append(w.on).append(w.selected).append(Math.round(w.value * 1000.0)).append(w.label).append(w.sub).append(';');
      }

      key.append(this.ui.hover)
         .append('|')
         .append(this.ui.focused)
         .append(this.ui.dragging)
         .append(this.ui.selectAll)
         .append('|')
         .append(info.detect)
         .append(info.account)
         .append(info.latency)
         .append(info.redirect)
         .append(info.caretOn)
         .append('|')
         .append(this.cfg.theme)
         .append(System.identityHashCode(info.art))
         .append('|')
         .append(this.ui.tab == 1 ? HudRenderer.frame(t) : 0L);
      Minecraft mc = Minecraft.getInstance();
      int s = Math.max(1, mc.getWindow().getGuiScale());
      int pw = b[2] * s;
      int ph = b[3] * s;

      try {
         this.slot.draw(ctx, mc, b[0], b[1], b[2], b[3], pw, ph, key.toString(), () -> SettingsPainter.render(pw, ph, this.ui, ws, this.cfg, info, t));
      } catch (Throwable var19) {
         ctx.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], -267251182);
         ctx.drawCenteredString(this.font, "Spotify HUD - Einstellungen (Grafik nicht verfügbar)", sw / 2, b[1] + 10, -1);
      }
   }

   private SettingsPainter.Info info() {
      SettingsPainter.Info i = new SettingsPainter.Info();
      SmtcBridge smtc = this.media.smtc();
      String src = this.media.sourceName();
      if (src != null) {
         i.detect = "Erkannt: " + src;
         i.detectColor = new Color(30, 215, 96);
      } else if (smtc.fatal() != null) {
         i.detect = "Windows-Erkennung: " + smtc.fatal();
         i.detectColor = new Color(241, 94, 108);
      } else if (!smtc.available()) {
         i.detect = "Song-Erkennung startet...";
         i.detectColor = new Color(255, 209, 102);
      } else {
         i.detect = "Kein Song erkannt - starte Spotify";
         i.detectColor = new Color(255, 209, 102);
      }

      String st = this.api.status();
      if (this.api.isLoggedIn()) {
         i.account = System.currentTimeMillis() - this.api.statusTime() > 8000L ? "Verbunden - das Herz speichert Songs" : st;
         i.accountColor = new Color(30, 215, 96);
      } else if (this.api.authRunning()) {
         i.account = st;
         i.accountColor = new Color(255, 209, 102);
      } else {
         boolean recent = System.currentTimeMillis() - this.api.statusTime() < 60000L;
         i.account = recent && st != null && !st.isEmpty() ? st : "Nicht verbunden";
         i.accountColor = recent ? new Color(255, 209, 102) : new Color(160, 160, 166);
      }

      i.latency = this.media.latencyMs();
      i.redirect = this.cfg.redirectUri();
      i.art = this.media.art();
      i.caretOn = this.ui.focused != null && System.currentTimeMillis() / 530L % 2L == 0L;
      return i;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      double mx = click.x();
      double my = click.y();
      int[] b = this.card();
      if (inside(mx, my, b)) {
         if (click.button() != 0) {
            return true;
         } else {
            List<SettingsUi.W> ws = this.ui.build(this.cfg, this.api);
            SettingsUi.W w = SettingsUi.hit(ws, dx(mx, b), dy(my, b));
            if (w == null || w.kind != SettingsUi.Kind.FIELD) {
               this.unfocus();
            }

            if (w != null) {
               this.activate(w, dx(mx, b));
            }

            return true;
         }
      } else {
         this.unfocus();
         return this.interaction.mouseDown(mx, my, click.button(), this.width, this.height) ? true : super.mouseClicked(click, doubled);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double ddx, double ddy) {
      if (this.ui.dragging != null) {
         int[] b = this.card();

         for (SettingsUi.W w : this.ui.build(this.cfg, this.api)) {
            if (w.id.equals(this.ui.dragging)) {
               this.setSlider(w, dx(click.x(), b), false);
            }
         }

         return true;
      } else {
         return this.interaction.mouseDrag(click.x(), click.y(), this.width, this.height) ? true : super.mouseDragged(click, ddx, ddy);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      if (this.ui.dragging != null) {
         this.ui.dragging = null;
         this.cfg.save();
         return true;
      } else {
         return this.interaction.mouseUp() ? true : super.mouseReleased(click);
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
      int[] b = this.card();
      if (inside(mx, my, b)) {
         List<SettingsUi.W> ws = this.ui.build(this.cfg, this.api);
         SettingsUi.W w = SettingsUi.hit(ws, dx(mx, b), dy(my, b));
         if (w != null && w.kind == SettingsUi.Kind.SLIDER && vertical != 0.0) {
            double step = "offset".equals(w.id) ? 0.00625 : 0.02;
            this.applySlider(w.id, Math.max(0.0, Math.min(1.0, w.value + (vertical > 0.0 ? step : -step))));
            SpotifyHudMod.scheduleSave();
         }

         return true;
      } else {
         return this.interaction.scroll(mx, my, vertical, this.width, this.height) ? true : super.mouseScrolled(mx, my, horizontal, vertical);
      }
   }

   private void activate(SettingsUi.W w, double x) {
      switch (w.kind) {
         case CLOSE:
            this.onClose();
            return;
         case TAB:
            this.ui.tab = Integer.parseInt(w.id.substring(3));
            lastTab = this.ui.tab;
            return;
         case THEME:
            this.cfg.theme = w.ref;
            this.cfg.save();
            return;
         case ANIM:
            this.cfg.animation = w.ref;
            this.cfg.save();
            return;
         case TOGGLE:
            this.toggle(w.id);
            this.cfg.save();
            return;
         case SLIDER:
            this.ui.dragging = w.id;
            this.setSlider(w, x, false);
            return;
         case FIELD:
            this.ui.focused = w.id;
            this.ui.selectAll = false;
            return;
         case BUTTON:
            this.button(w.id);
            return;
      }
   }

   private void toggle(String id) {
      switch (id) {
         case "lyrics":
            this.cfg.lyrics = !this.cfg.lyrics;
            break;
         case "karaoke":
            this.cfg.karaoke = !this.cfg.karaoke;
            break;
         case "wordGrow":
            this.cfg.wordGrow = !this.cfg.wordGrow;
            break;
         case "visible":
            this.cfg.visible = !this.cfg.visible;
            break;
         case "hideIdle":
            this.cfg.hideWhenIdle = !this.cfg.hideWhenIdle;
            break;
         case "animOnLyrics":
            this.cfg.animOnLyrics = !this.cfg.animOnLyrics;
      }
   }

   private void setSlider(SettingsUi.W w, double x, boolean save) {
      double v = Math.max(0.0, Math.min(1.0, (x - w.x) / w.w));
      this.applySlider(w.id, v);
      if (save) {
         this.cfg.save();
      }
   }

   private void applySlider(String id, double v) {
      switch (id) {
         case "intensity":
            this.cfg.animIntensity = SettingsUi.sliderToIntensity(v);
            break;
         case "offset":
            this.cfg.lyricsOffsetMs = SettingsUi.sliderToOffset(v);
            break;
         case "scale":
            this.cfg.scale = SettingsUi.sliderToScale(v);
      }
   }

   private void button(String id) {
      switch (id) {
         case "off-":
            this.cfg.lyricsOffsetMs = Math.max(-5000, this.cfg.lyricsOffsetMs - 50);
            this.cfg.save();
            break;
         case "off+":
            this.cfg.lyricsOffsetMs = Math.min(5000, this.cfg.lyricsOffsetMs + 50);
            this.cfg.save();
            break;
         case "off0":
            this.cfg.lyricsOffsetMs = 0;
            this.cfg.save();
            break;
         case "reset":
            this.cfg.posX = 0.5;
            this.cfg.posY = 0.012;
            this.cfg.scale = 1.0;
            this.cfg.save();
            this.media.toast("Position & Größe zurückgesetzt");
            break;
         case "dashboard":
            open("https://developer.spotify.com/dashboard");
            break;
         case "copyUri":
            this.applyPort();
            SpotifyHudMod.copyToClipboard(this.cfg.redirectUri());
            this.media.toast("Redirect-URI kopiert: " + this.cfg.redirectUri());
            break;
         case "login":
            if (this.api.isLoggedIn()) {
               this.api.logout();
            } else if (this.api.authRunning()) {
               this.api.cancelLogin();
            } else {
               this.cfg.clientId = this.ui.clientIdText.trim();
               this.applyPort();
               this.cfg.save();
               String url = this.api.startLogin();
               if (url != null) {
                  open(url);
               }
            }
      }
   }

   private void applyPort() {
      try {
         int p = Integer.parseInt(this.ui.portText.trim());
         if (p >= 1024 && p <= 65535 && p != this.cfg.redirectPort) {
            this.cfg.redirectPort = p;
            this.cfg.save();
         }
      } catch (NumberFormatException var2) {
      }

      this.ui.portText = String.valueOf(this.cfg.redirectPort);
      this.shownPort = this.cfg.redirectPort;
   }

   private void unfocus() {
      if (this.ui.focused != null) {
         if ("clientId".equals(this.ui.focused)) {
            this.cfg.clientId = this.ui.clientIdText.trim();
            this.cfg.save();
         }

         if ("port".equals(this.ui.focused)) {
            this.applyPort();
         }

         this.ui.focused = null;
         this.ui.selectAll = false;
      }
   }

   private static void open(String url) {
      try {
         Util.getPlatform().openUri(url);
      } catch (Throwable var2) {
         SpotifyHudMod.LOG("Browser konnte nicht geöffnet werden: " + var2);
      }
   }

   private String text() {
      return "port".equals(this.ui.focused) ? this.ui.portText : this.ui.clientIdText;
   }

   private void setText(String s) {
      if ("port".equals(this.ui.focused)) {
         this.ui.portText = s.length() > 5 ? s.substring(0, 5) : s;
      } else {
         this.ui.clientIdText = s.length() > 64 ? s.substring(0, 64) : s;
      }
   }

   private boolean accept(char c) {
      return !"port".equals(this.ui.focused) ? Character.isLetterOrDigit(c) : c >= '0' && c <= '9';
   }

   @Override
   public boolean keyPressed(KeyEvent key) {
      if (this.ui.focused == null) {
         return super.keyPressed(key);
      } else {
         int k = key.key();
         boolean ctrl = (key.modifiers() & 2) != 0;
         String t = this.text();
         if (k == 256 || k == 257 || k == 335 || k == 258) {
            this.unfocus();
            return true;
         } else if (k != 259 && k != 261) {
            if (ctrl && k == 65) {
               this.ui.selectAll = true;
               return true;
            } else if (ctrl && k == 67) {
               SpotifyHudMod.copyToClipboard(t);
               return true;
            } else if (ctrl && k == 86) {
               String clip = "";

               try {
                  clip = Minecraft.getInstance().keyboardHandler.getClipboard();
               } catch (Throwable var11) {
               }

               StringBuilder sb = new StringBuilder();
               if (clip != null) {
                  for (char c : clip.trim().toCharArray()) {
                     if (this.accept(c)) {
                        sb.append(c);
                     }
                  }
               }

               this.setText(this.ui.selectAll ? sb.toString() : t + sb);
               this.ui.selectAll = false;
               return true;
            } else {
               return true;
            }
         } else {
            if (this.ui.selectAll) {
               this.setText("");
            } else if (!t.isEmpty()) {
               this.setText(ctrl ? "" : t.substring(0, t.length() - 1));
            }

            this.ui.selectAll = false;
            return true;
         }
      }
   }

   @Override
   public boolean charTyped(CharacterEvent chr) {
      if (this.ui.focused == null) {
         return super.charTyped(chr);
      } else {
         String s = chr.codepointAsString();
         if (s == null) {
            return true;
         } else {
            StringBuilder sb = new StringBuilder();

            for (char c : s.toCharArray()) {
               if (this.accept(c)) {
                  sb.append(c);
               }
            }

            if (sb.length() == 0) {
               return true;
            } else {
               this.setText(this.ui.selectAll ? sb.toString() : this.text() + sb);
               this.ui.selectAll = false;
               return true;
            }
         }
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void removed() {
      this.interaction.mouseUp();
      this.unfocus();
      this.cfg.clientId = this.ui.clientIdText.trim();
      this.applyPort();
      this.cfg.save();
   }
}
