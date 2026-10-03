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

   public SettingsScreen(Config var1, Media var2, HudRenderer var3) {
      super(Component.literal("Spotify HUD"));
      this.cfg = var1;
      this.media = var2;
      this.api = var2.api();
      this.interaction = new HudInteraction(var1, var3);
      this.ui.tab = lastTab;
      this.ui.clientIdText = var1.clientId == null ? "" : var1.clientId;
      this.ui.portText = String.valueOf(var1.redirectPort);
      this.shownPort = var1.redirectPort;
   }

   @Override
   public void init() {
   }

   private int[] card() {
      int var1 = this.width;
      int var2 = this.height;
      double var3 = Math.min(1.0, Math.min((var1 - 10) / 360.0, (var2 - 10) / 230.0));
      int var5 = (int)Math.round(360.0 * var3);
      int var6 = (int)Math.round(230.0 * var3);
      return new int[]{(var1 - var5) / 2, (var2 - var6) / 2, var5, var6};
   }

   private static boolean inside(double var0, double var2, int[] var4) {
      return var0 >= var4[0] && var2 >= var4[1] && var0 < var4[0] + var4[2] && var2 < var4[1] + var4[3];
   }

   private static double dx(double var0, int[] var2) {
      return (var0 - var2[0]) * 360.0 / var2[2];
   }

   private static double dy(double var0, int[] var2) {
      return (var0 - var2[1]) * 230.0 / var2[3];
   }

   @Override
   public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
   }

   @Override
   public void render(GuiGraphics var1, int var2, int var3, float var4) {
      if (this.cfg.redirectPort != this.shownPort && !"port".equals(this.ui.focused)) {
         this.shownPort = this.cfg.redirectPort;
         this.ui.portText = String.valueOf(this.shownPort);
      }

      int var5 = this.width;
      int var6 = this.height;
      var1.fill(0, 0, var5, var6, 1711276032);
      this.interaction.render(var1, var2, var3, var5, var6);
      int[] var7 = this.card();
      List var8 = this.ui.build(this.cfg, this.api);
      SettingsUi.W var9 = inside(var2, var3, var7) ? SettingsUi.hit(var8, dx(var2, var7), dy(var3, var7)) : null;
      this.ui.hover = var9 != null ? var9.id : null;
      SettingsPainter.Info var10 = this.info();
      double var11 = HudRenderer.animTime();
      StringBuilder var13 = new StringBuilder(256);

      for (SettingsUi.W var15 : (Iterable<SettingsUi.W>) (Iterable<?>) (var8)) {
         var13.append(var15.id)
            .append(var15.on)
            .append(var15.selected)
            .append(Math.round(var15.value * 1000.0))
            .append(var15.label)
            .append(var15.sub)
            .append(';');
      }

      var13.append(this.ui.hover)
         .append('|')
         .append(this.ui.focused)
         .append(this.ui.dragging)
         .append(this.ui.selectAll)
         .append('|')
         .append(var10.detect)
         .append(var10.account)
         .append(var10.latency)
         .append(var10.redirect)
         .append(var10.caretOn)
         .append('|')
         .append(this.cfg.theme)
         .append(System.identityHashCode(var10.art))
         .append('|')
         .append(this.ui.tab == 1 ? HudRenderer.frame(var11) : 0L);
      Minecraft var20 = Minecraft.getInstance();
      int var21 = Math.max(1, var20.getWindow().getGuiScale());
      int var16 = var7[2] * var21;
      int var17 = var7[3] * var21;

      try {
         this.slot
            .draw(
               var1,
               var20,
               var7[0],
               var7[1],
               var7[2],
               var7[3],
               var16,
               var17,
               var13.toString(),
               () -> SettingsPainter.render(var16, var17, this.ui, var8, this.cfg, var10, var11)
            );
      } catch (Throwable var19) {
         var1.fill(var7[0], var7[1], var7[0] + var7[2], var7[1] + var7[3], -267251182);
         var1.drawCenteredString(this.font, "Spotify HUD - Einstellungen (Grafik nicht verfügbar)", var5 / 2, var7[1] + 10, -1);
      }
   }

   private SettingsPainter.Info info() {
      SettingsPainter.Info var1 = new SettingsPainter.Info();
      SmtcBridge var2 = this.media.smtc();
      String var3 = this.media.sourceName();
      if (var3 != null) {
         var1.detect = "Erkannt: " + var3;
         var1.detectColor = new Color(30, 215, 96);
      } else if (var2.fatal() != null) {
         var1.detect = "Windows-Erkennung: " + var2.fatal();
         var1.detectColor = new Color(241, 94, 108);
      } else if (!var2.available()) {
         var1.detect = "Song-Erkennung startet...";
         var1.detectColor = new Color(255, 209, 102);
      } else {
         var1.detect = "Kein Song erkannt - starte Spotify";
         var1.detectColor = new Color(255, 209, 102);
      }

      String var4 = this.api.status();
      if (this.api.isLoggedIn()) {
         var1.account = System.currentTimeMillis() - this.api.statusTime() > 8000L ? "Verbunden - das Herz speichert Songs" : var4;
         var1.accountColor = new Color(30, 215, 96);
      } else if (this.api.authRunning()) {
         var1.account = var4;
         var1.accountColor = new Color(255, 209, 102);
      } else {
         boolean var5 = System.currentTimeMillis() - this.api.statusTime() < 60000L;
         var1.account = var5 && var4 != null && !var4.isEmpty() ? var4 : "Nicht verbunden";
         var1.accountColor = var5 ? new Color(255, 209, 102) : new Color(160, 160, 166);
      }

      var1.latency = this.media.latencyMs();
      var1.redirect = this.cfg.redirectUri();
      var1.art = this.media.art();
      var1.caretOn = this.ui.focused != null && System.currentTimeMillis() / 530L % 2L == 0L;
      return var1;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      int[] var7 = this.card();
      if (inside(var3, var5, var7)) {
         if (var1.button() != 0) {
            return true;
         } else {
            List var8 = this.ui.build(this.cfg, this.api);
            SettingsUi.W var9 = SettingsUi.hit(var8, dx(var3, var7), dy(var5, var7));
            if (var9 == null || var9.kind != SettingsUi.Kind.FIELD) {
               this.unfocus();
            }

            if (var9 != null) {
               this.activate(var9, dx(var3, var7));
            }

            return true;
         }
      } else {
         this.unfocus();
         return this.interaction.mouseDown(var3, var5, var1.button(), this.width, this.height) ? true : super.mouseClicked(var1, var2);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
      if (this.ui.dragging != null) {
         int[] var6 = this.card();

         for (SettingsUi.W var9 : this.ui.build(this.cfg, this.api)) {
            if (var9.id.equals(this.ui.dragging)) {
               this.setSlider(var9, dx(var1.x(), var6), false);
            }
         }

         return true;
      } else {
         return this.interaction.mouseDrag(var1.x(), var1.y(), this.width, this.height) ? true : super.mouseDragged(var1, var2, var4);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent var1) {
      if (this.ui.dragging != null) {
         this.ui.dragging = null;
         this.cfg.save();
         return true;
      } else {
         return this.interaction.mouseUp() ? true : super.mouseReleased(var1);
      }
   }

   @Override
   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      int[] var9 = this.card();
      if (inside(var1, var3, var9)) {
         List var10 = this.ui.build(this.cfg, this.api);
         SettingsUi.W var11 = SettingsUi.hit(var10, dx(var1, var9), dy(var3, var9));
         if (var11 != null && var11.kind == SettingsUi.Kind.SLIDER && var7 != 0.0) {
            double var12 = "offset".equals(var11.id) ? 0.00625 : 0.02;
            this.applySlider(var11.id, Math.max(0.0, Math.min(1.0, var11.value + (var7 > 0.0 ? var12 : -var12))));
            SpotifyHudMod.scheduleSave();
         }

         return true;
      } else {
         return this.interaction.scroll(var1, var3, var7, this.width, this.height) ? true : super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   private void activate(SettingsUi.W var1, double var2) {
      switch (var1.kind) {
         case CLOSE:
            this.onClose();
            return;
         case TAB:
            this.ui.tab = Integer.parseInt(var1.id.substring(3));
            lastTab = this.ui.tab;
            return;
         case THEME:
            this.cfg.theme = var1.ref;
            this.cfg.save();
            return;
         case ANIM:
            this.cfg.animation = var1.ref;
            this.cfg.save();
            return;
         case TOGGLE:
            this.toggle(var1.id);
            this.cfg.save();
            return;
         case SLIDER:
            this.ui.dragging = var1.id;
            this.setSlider(var1, var2, false);
            return;
         case FIELD:
            this.ui.focused = var1.id;
            this.ui.selectAll = false;
            return;
         case BUTTON:
            this.button(var1.id);
            return;
      }
   }

   private void toggle(String var1) {
      switch (var1) {
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

   private void setSlider(SettingsUi.W var1, double var2, boolean var4) {
      double var5 = Math.max(0.0, Math.min(1.0, (var2 - var1.x) / var1.w));
      this.applySlider(var1.id, var5);
      if (var4) {
         this.cfg.save();
      }
   }

   private void applySlider(String var1, double var2) {
      switch (var1) {
         case "intensity":
            this.cfg.animIntensity = SettingsUi.sliderToIntensity(var2);
            break;
         case "offset":
            this.cfg.lyricsOffsetMs = SettingsUi.sliderToOffset(var2);
            break;
         case "scale":
            this.cfg.scale = SettingsUi.sliderToScale(var2);
      }
   }

   private void button(String var1) {
      switch (var1) {
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
               String var4 = this.api.startLogin();
               if (var4 != null) {
                  open(var4);
               }
            }
      }
   }

   private void applyPort() {
      try {
         int var1 = Integer.parseInt(this.ui.portText.trim());
         if (var1 >= 1024 && var1 <= 65535 && var1 != this.cfg.redirectPort) {
            this.cfg.redirectPort = var1;
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

   private static void open(String var0) {
      try {
         Util.getPlatform().openUri(var0);
      } catch (Throwable var2) {
         SpotifyHudMod.LOG("Browser konnte nicht geöffnet werden: " + var2);
      }
   }

   private String text() {
      return "port".equals(this.ui.focused) ? this.ui.portText : this.ui.clientIdText;
   }

   private void setText(String var1) {
      if ("port".equals(this.ui.focused)) {
         this.ui.portText = var1.length() > 5 ? var1.substring(0, 5) : var1;
      } else {
         this.ui.clientIdText = var1.length() > 64 ? var1.substring(0, 64) : var1;
      }
   }

   private boolean accept(char var1) {
      return !"port".equals(this.ui.focused) ? Character.isLetterOrDigit(var1) : var1 >= '0' && var1 <= '9';
   }

   @Override
   public boolean keyPressed(KeyEvent var1) {
      if (this.ui.focused == null) {
         return super.keyPressed(var1);
      } else {
         int var2 = var1.key();
         boolean var3 = (var1.modifiers() & 2) != 0;
         String var4 = String.valueOf(this.text());
         if (var2 == 256 || var2 == 257 || var2 == 335 || var2 == 258) {
            this.unfocus();
            return true;
         } else if (var2 != 259 && var2 != 261) {
            if (var3 && var2 == 65) {
               this.ui.selectAll = true;
               return true;
            } else if (var3 && var2 == 67) {
               SpotifyHudMod.copyToClipboard((String)var4);
               return true;
            } else if (var3 && var2 == 86) {
               String var5 = "";

               try {
                  var5 = Minecraft.getInstance().keyboardHandler.getClipboard();
               } catch (Throwable var11) {
               }

               StringBuilder var6 = new StringBuilder();
               if (var5 != null) {
                  for (char var10 : var5.trim().toCharArray()) {
                     if (this.accept(var10)) {
                        var6.append(var10);
                     }
                  }
               }

               this.setText(this.ui.selectAll ? var6.toString() : var4 + var6);
               this.ui.selectAll = false;
               return true;
            } else {
               return true;
            }
         } else {
            if (this.ui.selectAll) {
               this.setText("");
            } else if (!var4.isEmpty()) {
               this.setText(var3 ? "" : var4.substring(0, var4.length() - 1));
            }

            this.ui.selectAll = false;
            return true;
         }
      }
   }

   @Override
   public boolean charTyped(CharacterEvent var1) {
      if (this.ui.focused == null) {
         return super.charTyped(var1);
      } else {
         String var2 = var1.codepointAsString();
         if (var2 == null) {
            return true;
         } else {
            StringBuilder var3 = new StringBuilder();

            for (char var7 : var2.toCharArray()) {
               if (this.accept(var7)) {
                  var3.append(var7);
               }
            }

            if (var3.length() == 0) {
               return true;
            } else {
               this.setText(this.ui.selectAll ? var3.toString() : this.text() + var3);
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
