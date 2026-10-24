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

   static double scaleToSlider(double var0) {
      return (var0 - 0.5) / 2.5;
   }

   static double sliderToScale(double var0) {
      return Math.round((0.5 + var0 * 2.5) * 20.0) / 20.0;
   }

   static double offsetToSlider(int var0) {
      return (var0 + 2000) / 4000.0;
   }

   static int sliderToOffset(double var0) {
      return (int)(Math.round((var0 * 4000.0 - 2000.0) / 25.0) * 25L);
   }

   static double intensityToSlider(double var0) {
      return (var0 - 0.2) / 0.8;
   }

   static double sliderToIntensity(double var0) {
      return Math.round((0.2 + var0 * 0.8) * 20.0) / 20.0;
   }

   List<SettingsUi.W> build(Config var1, SpotifyApi var2) {
      ArrayList var3 = new ArrayList();
      SettingsUi.W var4 = new SettingsUi.W("close", SettingsUi.Kind.CLOSE, 330.0, 9.0, 20.0, 20.0);
      var3.add(var4);

      for (int var5 = 0; var5 < TABS.length; var5++) {
         SettingsUi.W var6 = new SettingsUi.W("tab" + var5, SettingsUi.Kind.TAB, 12.0, 46 + var5 * 27, 82.0, 23.0);
         var6.label = TABS[var5];
         var6.selected = this.tab == var5;
         var3.add(var6);
      }

      double var14 = 108.0;
      double var7 = 240.0;
      switch (this.tab) {
         case 0:
            for (int var19 = 0; var19 < Theme.IDS.length; var19++) {
               int var24 = var19 % 4;
               int var27 = var19 / 4;
               SettingsUi.W var30 = new SettingsUi.W("theme:" + Theme.IDS[var19], SettingsUi.Kind.THEME, var14 + var24 * 61, 64 + var27 * 58, 56.0, 52.0);
               var30.label = Theme.NAMES[var19];
               var30.ref = Theme.IDS[var19];
               var30.selected = Theme.IDS[var19].equals(var1.theme);
               var3.add(var30);
            }
            break;
         case 1:
            for (int var17 = 0; var17 < Anim.IDS.length; var17++) {
               int var22 = var17 % 4;
               int var26 = var17 / 4;
               SettingsUi.W var29 = new SettingsUi.W("anim:" + Anim.IDS[var17], SettingsUi.Kind.ANIM, var14 + var22 * 61, 62 + var26 * 48, 56.0, 44.0);
               var29.label = Anim.NAMES[var17];
               var29.ref = Anim.IDS[var17];
               var29.selected = Anim.IDS[var17].equals(var1.animation);
               var3.add(var29);
            }

            SettingsUi.W var18 = new SettingsUi.W("intensity", SettingsUi.Kind.SLIDER, var14, 160.0, var7, 20.0);
            var18.label = "Stärke";
            var18.value = intensityToSlider(var1.animIntensity);
            var18.sub = Math.round(var1.animIntensity * 100.0) + " %";
            var3.add(var18);
            SettingsUi.W var23 = new SettingsUi.W("animOnLyrics", SettingsUi.Kind.TOGGLE, var14, 184.0, var7, 18.0);
            var23.label = "Auch hinter den Lyrics";
            var23.on = var1.animOnLyrics;
            var3.add(var23);
            break;
         case 2:
            var3.add(toggle("lyrics", "Lyrics anzeigen", "Taste K oder Rechtsklick aufs HUD", var1.lyrics, var14, 56.0, var7));
            var3.add(toggle("karaoke", "Wort-Highlight", "Die Zeile und das gesungene Wort leuchten", var1.karaoke, var14, 84.0, var7));
            var3.add(toggle("wordGrow", "Aktives Wort größer", "Das Wort wächst beim Singen etwas", var1.wordGrow, var14, 112.0, var7));
            SettingsUi.W var16 = new SettingsUi.W("offset", SettingsUi.Kind.SLIDER, var14, 142.0, var7, 20.0);
            var16.label = "Sync";
            var16.value = offsetToSlider(var1.lyricsOffsetMs);
            var16.sub = (var1.lyricsOffsetMs > 0 ? "+" : "") + var1.lyricsOffsetMs + " ms";
            var3.add(var16);
            SettingsUi.W var21 = new SettingsUi.W("off-", SettingsUi.Kind.BUTTON, var14, 170.0, 56.0, 18.0);
            var21.label = "-50 ms";
            var3.add(var21);
            SettingsUi.W var25 = new SettingsUi.W("off0", SettingsUi.Kind.BUTTON, var14 + 60.0, 170.0, 56.0, 18.0);
            var25.label = "Reset";
            var3.add(var25);
            SettingsUi.W var28 = new SettingsUi.W("off+", SettingsUi.Kind.BUTTON, var14 + 120.0, 170.0, 56.0, 18.0);
            var28.label = "+50 ms";
            var3.add(var28);
            break;
         case 3:
            var3.add(toggle("visible", "HUD anzeigen", "Blendet das komplette Spotify HUD ein/aus", var1.visible, var14, 56.0, var7));
            var3.add(toggle("hideIdle", "Ausblenden, wenn nichts läuft", "Nur sichtbar, solange ein Song erkannt wird", var1.hideWhenIdle, var14, 84.0, var7));
            SettingsUi.W var15 = new SettingsUi.W("scale", SettingsUi.Kind.SLIDER, var14, 120.0, var7, 20.0);
            var15.label = "Größe";
            var15.value = scaleToSlider(var1.scale);
            var15.sub = Math.round(var1.scale * 100.0) + " %";
            var3.add(var15);
            SettingsUi.W var20 = new SettingsUi.W("reset", SettingsUi.Kind.BUTTON, var14, 152.0, 150.0, 20.0);
            var20.label = "Position & Größe zurücksetzen";
            var3.add(var20);
            break;
         default:
            SettingsUi.W var9 = new SettingsUi.W("clientId", SettingsUi.Kind.FIELD, var14, 84.0, 176.0, 20.0);
            var9.label = "Client-ID";
            var9.sub = this.clientIdText;
            var9.selected = "clientId".equals(this.focused);
            var3.add(var9);
            SettingsUi.W var10 = new SettingsUi.W("port", SettingsUi.Kind.FIELD, var14 + 182.0, 84.0, 58.0, 20.0);
            var10.label = "Port";
            var10.sub = this.portText;
            var10.selected = "port".equals(this.focused);
            var3.add(var10);
            SettingsUi.W var11 = new SettingsUi.W("login", SettingsUi.Kind.BUTTON, var14, 112.0, 76.0, 20.0);
            var11.label = var2.isLoggedIn() ? "Abmelden" : (var2.authRunning() ? "Abbrechen" : "Anmelden");
            var11.selected = !var2.isLoggedIn() && !var2.authRunning();
            var3.add(var11);
            SettingsUi.W var12 = new SettingsUi.W("dashboard", SettingsUi.Kind.BUTTON, var14 + 82.0, 112.0, 76.0, 20.0);
            var12.label = "Dashboard";
            var3.add(var12);
            SettingsUi.W var13 = new SettingsUi.W("copyUri", SettingsUi.Kind.BUTTON, var14 + 164.0, 112.0, 76.0, 20.0);
            var13.label = "URI kopieren";
            var3.add(var13);
      }

      return var3;
   }

   private static SettingsUi.W toggle(String var0, String var1, String var2, boolean var3, double var4, double var6, double var8) {
      SettingsUi.W var10 = new SettingsUi.W(var0, SettingsUi.Kind.TOGGLE, var4, var6, var8, 24.0);
      var10.label = var1;
      var10.sub = var2;
      var10.on = var3;
      return var10;
   }

   static SettingsUi.W hit(List<SettingsUi.W> var0, double var1, double var3) {
      for (int var5 = var0.size() - 1; var5 >= 0; var5--) {
         if (((SettingsUi.W)var0.get(var5)).contains(var1, var3)) {
            return (SettingsUi.W)var0.get(var5);
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

      W(String var1, SettingsUi.Kind var2, double var3, double var5, double var7, double var9) {
         this.id = var1;
         this.kind = var2;
         this.x = var3;
         this.y = var5;
         this.w = var7;
         this.h = var9;
      }

      boolean contains(double var1, double var3) {
         return var1 >= this.x && var3 >= this.y && var1 < this.x + this.w && var3 < this.y + this.h;
      }
   }
}
