package dev.lego.ui.hub;

import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public final class DesignPart implements HubView.Part {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;

   @Override
   public void draw(HubView var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.p(24.0);
      int var7 = var1.header(var2 + var6, var3 + var1.p(20.0), "Design & Client", "Thema, Farbe, HUD, UI-Größe und Animations-Tempo");
      int var8 = var7 + var1.p(4.0);
      int var9 = var3 + var5 - var1.p(14.0) - var8;
      this.max = Math.max(0, this.contentH(var1) - var9);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var2, var8, var4, var9);
      Kit.clip(var2, var8, var4, var9);
      Kit.hit(var2, var8, var4, var9).scroll(var2x -> this.target = this.target - var2x.floatValue() * var1.p(44.0));
      int var10 = Math.round(var8 - this.scroll);
      int var11 = var4 - var6 * 2;
      var10 = this.label(var1, "THEMA", var2 + var6, var10);
      byte var12 = 4;
      int var13 = var1.p(10.0);
      int var14 = (var11 - var13 * (var12 - 1)) / var12;
      int var15 = var1.p(66.0);

      for (int var16 = 0; var16 < Style.THEMES.length; var16++) {
         int var17 = var2 + var6 + var16 % var12 * (var14 + var13);
         int var18 = var10 + var16 / var12 * (var15 + var1.p(24.0));
         this.themeCard(var1, var16, var17, var18, var14, var15);
      }

      var10 += (Style.THEMES.length + var12 - 1) / var12 * (var15 + var1.p(24.0)) + var1.p(8.0);
      var10 = this.label(var1, "AKZENTFARBE", var2 + var6, var10);
      int var46 = var1.p(26.0);
      int var47 = var1.p(12.0);

      for (int var48 = 0; var48 < Setting.Color.PALETTE.length; var48++) {
         int var19 = var2 + var6 + var48 * (var46 + var47);
         boolean var20 = Modules.accent == var48;
         boolean var21 = var1.hover(var19, var10, var46, var46);
         float var22 = Ease.to("acc:" + var48, var20 ? 1.12F : (var21 ? 1.08F : 1.0F), 16.0F);
         Gx.push();
         Gx.scaleAt(var19 + var46 / 2.0F, var10 + var46 / 2.0F, var22);
         if (var48 == Setting.Color.PALETTE.length - 1) {
            Gx.image(ModsPart.rainbow(var46), var19, var10, var46, var46, -1);
         } else {
            Gx.circle(var19 + var46 / 2, var10 + var46 / 2, var46 / 2, Setting.Color.PALETTE[var48]);
         }

         if (var20) {
            Gx.ringAt(var19 + var46 / 2, var10 + var46 / 2, var46 / 2 + var1.p(4.0), Math.max(1, var1.p(1.6)), Style.text);
            Gx.icon("check", var19 + var46 / 2.0F, var10 + var46 / 2.0F, var1.pf(12.0), Style.brightness(Setting.Color.PALETTE[var48]) > 0.6 ? -15658735 : -1);
         }

         Gx.pop();
         int var23 = var48;
         Kit.hit(var19 - var47 / 2, var10 - var1.p(4.0), var46 + var47, var46 + var1.p(8.0)).click(() -> {
            Modules.accent = var23;
            Modules.scheduleSave();
         });
      }

      var10 += var46 + var1.p(22.0);
      var10 = this.label(var1, "HUD", var2 + var6, var10);
      int var49 = var1.p(44.0);
      int var50 = var1.p(8.0);
      this.rowBg(var1, var2 + var6, var10, var11, var49, "hudstyle");
      Gx.textMid("HUD-Stil", var2 + var6 + var1.p(16.0), var10 + var49 / 2.0F, var1.pf(9.0), 2, Style.text);
      this.segmented(
         var1,
         "hudstyle",
         var2 + var6 + var11 - var1.p(16.0),
         var10 + var49 / 2,
         UiSettings.HUD_STYLES,
         UiSettings.hudStyle,
         var0 -> UiSettings.hudStyle = var0
      );
      var10 += var49 + var50;
      this.rowBg(var1, var2 + var6, var10, var11, var49, "hudop");
      Gx.textMid("Hintergrund-Deckkraft", var2 + var6 + var1.p(16.0), var10 + var49 / 2.0F, var1.pf(9.0), 2, Style.text);
      int var51 = var1.p(58.0);
      int var52 = var1.p(22.0);
      int var53 = var2 + var6 + var11 - var1.p(16.0);
      Gx.rect(var53 - var51, var10 + var49 / 2 - var52 / 2, var51, var52, var1.p(7.0), Style.surface2);
      Gx.textCenter(Math.round(UiSettings.hudOpacity * 100.0) + "%", var53 - var51 / 2.0F, var10 + var49 / 2.0F, var1.pf(8.2F), 3, Style.text);
      int var54 = var1.p(220.0);
      int var24 = var53 - var51 - var1.p(14.0) - var54;
      Kit.slider("hudop", var24, var10 + var49 / 2 - var1.p(9.0), var54, var1.p(18.0), (float)UiSettings.hudOpacity, var1.P);
      Kit.hit(var24 - var1.p(6.0), var10, var54 + var1.p(12.0), var49).drag((var2x, var3x) -> {
         UiSettings.hudOpacity = Math.round(Math.max(0.0, Math.min(1.0, (double)(var2x - var24) / var54)) * 100.0) / 100.0;
         Modules.scheduleSave();
      });
      var10 += var49 + var50;
      var10 = this.toggleRow(
            var1, "Icons in der HUD", "hudicons", UiSettings.hudIcons, () -> UiSettings.hudIcons = !UiSettings.hudIcons, var2 + var6, var10, var11, var49
         )
         + var50;
      var10 = this.toggleRow(
            var1, "Schatten", "hudshadow", UiSettings.hudShadow, () -> UiSettings.hudShadow = !UiSettings.hudShadow, var2 + var6, var10, var11, var49
         )
         + var50;
      var10 = this.toggleRow(
            var1,
            "Akzent-Linie links",
            "hudline",
            UiSettings.hudAccentLine,
            () -> UiSettings.hudAccentLine = !UiSettings.hudAccentLine,
            var2 + var6,
            var10,
            var11,
            var49
         )
         + var50;
      var10 += var1.p(12.0);
      var10 = this.label(var1, "MENÜ", var2 + var6, var10);
      var10 = this.toggleRow(var1, "Klick-Sounds", "sounds", UiSettings.sounds, () -> {
         UiSettings.sounds = !UiSettings.sounds;
         Sound.uiSounds = UiSettings.sounds;
      }, var2 + var6, var10, var11, var49) + var50;
      var10 = this.toggleRow(
            var1, "Hintergrund verschwimmen (Blur)", "blur", UiSettings.blur, () -> UiSettings.blur = !UiSettings.blur, var2 + var6, var10, var11, var49
         )
         + var50;
      var10 += var1.p(12.0);
      var10 = this.label(var1, "CLIENT", var2 + var6, var10);
      var10 = this.sliderRow(
            var1,
            "UI-Größe",
            "uiscale",
            (UiSettings.uiScale - 0.6) / 0.7,
            Math.round(UiSettings.uiScale * 100.0) + "%",
            var0 -> UiSettings.uiScale = Math.round((0.6 + var0 * 0.7) * 20.0) / 20.0,
            var2 + var6,
            var10,
            var11,
            var49
         )
         + var50;
      var10 = this.sliderRow(
            var1,
            "Animations-Tempo",
            "animspeed",
            (UiSettings.animSpeed - 0.5) / 2.5,
            String.format(Locale.ROOT, "%.1fx", UiSettings.animSpeed),
            var0 -> UiSettings.animSpeed = Math.round((0.5 + var0 * 2.5) * 10.0) / 10.0,
            var2 + var6,
            var10,
            var11,
            var49
         )
         + var50;
      var10 = this.toggleRow(
            var1, "Animationen", "anims", UiSettings.animations, () -> UiSettings.animations = !UiSettings.animations, var2 + var6, var10, var11, var49
         )
         + var50;
      var10 = this.toggleRow(
            var1,
            "Weihnachts-Deko (Schnee, Lichter, Mützen)",
            "xmas",
            UiSettings.xmas,
            () -> UiSettings.xmas = !UiSettings.xmas,
            var2 + var6,
            var10,
            var11,
            var49
         )
         + var50;
      Kit.unclip();
      Gx.unclip();
   }

   private int sliderRow(HubView var1, String var2, String var3, double var4, String var6, DoubleConsumer var7, int var8, int var9, int var10, int var11) {
      this.rowBg(var1, var8, var9, var10, var11, var3);
      Gx.textMid(var2, var8 + var1.p(16.0), var9 + var11 / 2.0F, var1.pf(9.0), 2, Style.text);
      int var12 = var1.p(58.0);
      int var13 = var1.p(22.0);
      int var14 = var8 + var10 - var1.p(16.0);
      Gx.rect(var14 - var12, var9 + var11 / 2 - var13 / 2, var12, var13, var1.p(7.0), Style.surface2);
      Gx.textCenter(var6, var14 - var12 / 2.0F, var9 + var11 / 2.0F, var1.pf(8.2F), 3, Style.text);
      int var15 = var1.p(220.0);
      int var16 = var14 - var12 - var1.p(14.0) - var15;
      Kit.slider(var3, var16, var9 + var11 / 2 - var1.p(9.0), var15, var1.p(18.0), (float)Math.max(0.0, Math.min(1.0, var4)), var1.P);
      Kit.hit(var16 - var1.p(6.0), var9, var15 + var1.p(12.0), var11).drag((var3x, var4x) -> {
         var7.accept(Math.max(0.0, Math.min(1.0, (double)(var3x - var16) / var15)));
         Modules.scheduleSave();
      });
      return var9 + var11;
   }

   private int contentH(HubView var1) {
      int var2 = (Style.THEMES.length + 3) / 4;
      return var1.p(22.0)
         + var2 * var1.p(90.0)
         + var1.p(8.0)
         + var1.p(22.0)
         + var1.p(48.0)
         + var1.p(22.0)
         + 5 * var1.p(52.0)
         + var1.p(12.0)
         + var1.p(22.0)
         + 2 * var1.p(52.0)
         + var1.p(12.0)
         + var1.p(22.0)
         + 3 * var1.p(52.0)
         + var1.p(10.0);
   }

   private int label(HubView var1, String var2, int var3, int var4) {
      Gx.textSpaced(
         var2, var3 + Gx.spacedWidth(var2, var1.pf(7.2F), 3, var1.pf(1.4F)) / 2.0F, var4 + var1.pf(7.0), var1.pf(7.2F), 3, Style.muted, var1.pf(1.4F)
      );
      return var4 + var1.p(22.0);
   }

   private void rowBg(HubView var1, int var2, int var3, int var4, int var5, String var6) {
      float var7 = Ease.to("drow:" + var6, var1.hover(var2, var3, var4, var5) ? 1.0F : 0.0F, 14.0F);
      Gx.rect(var2, var3, var4, var5, var1.p(10.0), Gx.mix(Style.surface, Style.surfaceHover, var7 * 0.6F));
   }

   private int toggleRow(HubView var1, String var2, String var3, boolean var4, Runnable var5, int var6, int var7, int var8, int var9) {
      this.rowBg(var1, var6, var7, var8, var9, var3);
      Gx.textMid(var2, var6 + var1.p(16.0), var7 + var9 / 2.0F, var1.pf(9.0), 2, Style.text);
      int var10 = var1.p(32.0);
      int var11 = var1.p(18.0);
      Kit.toggle("d:" + var3, var6 + var8 - var1.p(16.0) - var10, var7 + (var9 - var11) / 2, var10, var11, var4, var1.P);
      Kit.hit(var6, var7, var8, var9).click(() -> {
         var5.run();
         Modules.scheduleSave();
      });
      return var7 + var9;
   }

   private void segmented(HubView var1, String var2, int var3, int var4, String[] var5, int var6, IntConsumer var7) {
      float var8 = var1.pf(7.8F);
      int var9 = var1.p(12.0);
      int var10 = var1.p(26.0);
      int var11 = 0;
      int[] var12 = new int[var5.length];

      for (int var13 = 0; var13 < var5.length; var13++) {
         var12[var13] = Math.round(Gx.width(var5[var13], var8, 2)) + var9 * 2;
         var11 += var12[var13];
      }

      int var20 = var3 - var11;
      Gx.rect(var20 - var1.p(3.0), var4 - var10 / 2 - var1.p(3.0), var11 + var1.p(6.0), var10 + var1.p(6.0), var1.p(9.0), Style.surface2);
      int[] var14 = new int[var5.length];
      int var15 = var20;

      for (int var16 = 0; var16 < var5.length; var16++) {
         var14[var16] = var15;
         var15 += var12[var16];
      }

      float var21 = Ease.to("dseg:" + var2, var14[var6], 18.0F, var14[var6]);
      float var17 = Ease.to("dsegw:" + var2, var12[var6], 18.0F, var12[var6]);
      Gx.rect(Math.round(var21), var4 - var10 / 2, Math.round(var17), var10, var1.p(7.0), Style.accent);

      for (int var18 = 0; var18 < var5.length; var18++) {
         Gx.textCenter(var5[var18], var14[var18] + var12[var18] / 2.0F, var4, var8, 2, var18 == var6 ? Style.accentText : Style.sub);
         int var19 = var18;
         Kit.hit(var14[var18], var4 - var10 / 2, var12[var18], var10).click(() -> {
            var7.accept(var19);
            Modules.scheduleSave();
         });
      }
   }

   private void themeCard(HubView var1, int var2, int var3, int var4, int var5, int var6) {
      String var7 = Style.THEMES[var2];
      boolean var8 = var7.equals(Modules.theme);
      boolean var9 = var1.hover(var3, var4, var5, var6);
      float var10 = Ease.to("theme:" + var2, var9 ? 1.0F : 0.0F, 14.0F);
      int[] var11 = Style.palette(var7);
      Gx.push();
      Gx.translate(0.0F, -var1.pf(2.0) * var10);
      Gx.shadow(var3, var4, var5, var6, var1.p(11.0), var1.p(6.0), 0.3F + 0.2F * var10);
      Gx.rect(var3, var4, var5, var6, var1.p(11.0), var11[0] | 0xFF000000);
      if ("glass".equals(var7)) {
         Gx.rectV(var3, var4, var5, var6, var1.p(11.0), 872415231, 234881023);
      }

      int var12 = var3 + var1.p(10.0);
      int var13 = var4 + var1.p(10.0);
      Gx.rect(var12, var13, var1.p(16.0), var6 - var1.p(20.0), var1.p(5.0), var11[1] | 0xFF000000);
      int var14 = var12 + var1.p(22.0);
      int var15 = var3 + var5 - var1.p(10.0) - var14;
      Gx.rect(var14, var13, var15, var1.p(18.0), var1.p(5.0), var11[1] | 0xFF000000);
      Gx.rect(var14 + var1.p(5.0), var13 + var1.p(6.0), var15 / 2, var1.p(3.0), var1.p(2.0), var11[3]);
      Gx.rect(var14, var13 + var1.p(24.0), var15, var6 - var1.p(44.0), var1.p(5.0), var11[1] | 0xFF000000);
      Gx.rect(var14 + var15 - var1.p(20.0), var13 + var1.p(30.0), var1.p(14.0), var1.p(8.0), var1.p(4.0), Style.accent);
      Gx.rect(var14 + var1.p(5.0), var13 + var1.p(31.0), var15 / 3, var1.p(3.0), var1.p(2.0), var11[4]);
      Gx.outline(var3, var4, var5, var6, var1.p(11.0), Math.max(1, var1.p(var8 ? 1.6 : 0.7)), var8 ? Style.accent : Gx.mix(Style.stroke, Style.strokeHi, var10));
      Gx.pop();
      Gx.textCenter(Style.THEME_NAMES[var2], var3 + var5 / 2.0F, var4 + var6 + var1.pf(11.0), var1.pf(8.0), var8 ? 3 : 2, var8 ? Style.text : Style.sub);
      Kit.hit(var3, var4, var5, var6 + var1.p(20.0)).click(() -> {
         Modules.theme = var7;
         Modules.scheduleSave();
      });
   }
}
