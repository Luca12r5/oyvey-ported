package dev.lego.ui.hub;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Keys;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;
import java.awt.Color;
import java.awt.geom.Arc2D.Double;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModsPart implements HubView.Part {
   private static final String[] TABS = new String[]{"ALLE", "HUD", "VISUALS", "UTILITY", "LEISTUNG", "SPOTIFY"};
   private static final Category[] TAB_CATS = new Category[]{null, Category.HUD, Category.VISUALS, Category.UTILITY, Category.PERFORMANCE, Category.SPOTIFY};
   private static int tab = 0;
   private String search = "";
   private boolean searchFocus = false;
   private float scroll = 0.0F;
   private float scrollTarget = 0.0F;
   private float maxScroll = 0.0F;
   private float sScroll = 0.0F;
   private float sScrollTarget = 0.0F;
   private float sMaxScroll = 0.0F;
   private Module open;
   private Module shownOpen;
   private Module binding;
   private Setting.Text editing;
   private long listChangedAt = System.currentTimeMillis();

   private List<Module> list() {
      ArrayList var1 = new ArrayList();
      String var2 = this.search.toLowerCase(Locale.ROOT).trim();

      for (Module var4 : Modules.ALL) {
         if (var4.category.listed) {
            if (!var2.isEmpty()) {
               if (var4.name.toLowerCase(Locale.ROOT).contains(var2)
                  || var4.description.toLowerCase(Locale.ROOT).contains(var2)
                  || var4.category.title.toLowerCase(Locale.ROOT).contains(var2)) {
                  var1.add(var4);
               }
            } else if (TAB_CATS[tab] == null || var4.category == TAB_CATS[tab]) {
               var1.add(var4);
            }
         }
      }

      return var1;
   }

   private void setTab(int var1) {
      if (var1 != tab || !this.search.isEmpty()) {
         tab = var1;
         this.search = "";
         this.scroll = this.scrollTarget = 0.0F;
         this.listChangedAt = System.currentTimeMillis();
      }
   }

   @Override
   public void draw(HubView var1, int var2, int var3, int var4, int var5) {
      float var6 = Ease.to("mods:sp", this.open != null ? 1.0F : 0.0F, 13.0F);
      if (this.open != null) {
         this.shownOpen = this.open;
      }

      if (var6 < 0.01F && this.open == null) {
         this.shownOpen = null;
      }

      if (var6 < 0.99F) {
         Gx.pushAlpha(1.0F - var6);
         Gx.push();
         Gx.translate(-var1.pf(40.0) * var6, 0.0F);
         this.drawList(var1, var2, var3, var4, var5, var6 < 0.5F);
         Gx.pop();
         Gx.popAlpha();
      }

      if (var6 > 0.01F && this.shownOpen != null) {
         Gx.pushAlpha(var6);
         Gx.push();
         Gx.translate(var1.pf(60.0) * (1.0F - Ease.outCubic(var6)), 0.0F);
         this.drawSettings(var1, this.shownOpen, var2, var3, var4, var5, var6 > 0.5F);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private void drawList(HubView var1, int var2, int var3, int var4, int var5, boolean var6) {
      int var7 = var1.p(22.0);
      int var8 = var2 + var7;
      int var9 = var3 + var1.p(20.0);
      var1.tabs("mods", var8, var9, TABS, this.search.isEmpty() ? tab : 0, this::setTab);
      int var10 = var1.p(168.0);
      int var11 = var1.p(26.0);
      int var12 = var2 + var4 - var7 - var10;
      var1.searchBox("mods", var12, var9, var10, var11, this.search, this.searchFocus);
      if (var6) {
         Kit.hit(var12, var9, var10, var11).click(() -> {
            if (!this.search.isEmpty() && var1.mx > var12 + var10 - var11) {
               this.search = "";
               this.listChangedAt = System.currentTimeMillis();
            }

            this.searchFocus = true;
         });
      }

      int var13 = var9 + var11 + var1.p(16.0);
      int var14 = var3 + var5 - var1.p(40.0) - var13;
      List var15 = this.list();
      int var16 = var1.p(8.0);
      int var17 = (var4 - var7 * 2) / var1.p(300.0) >= 3 ? 3 : 2;
      int var18 = (var4 - var7 * 2 - var16 * (var17 - 1)) / var17;
      int var19 = var1.p(54.0);
      int var20 = (var15.size() + var17 - 1) / var17;
      this.maxScroll = Math.max(0, var20 * (var19 + var16) - var16 - var14 + var1.p(4.0));
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
      this.scroll = this.scroll + (this.scrollTarget - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var2 + var7 - var1.p(6.0), var13, var4 - var7 * 2 + var1.p(12.0), var14);
      Kit.clip(var2, var13, var4, var14);
      if (var6) {
         Kit.hit(var2, var13, var4, var14).scroll(var2x -> this.scrollTarget = this.scrollTarget - var2x.floatValue() * var1.p(48.0));
      }

      float var21 = (float)(System.currentTimeMillis() - this.listChangedAt) * UiSettings.anim();
      int var22 = Math.max(0, (int)Math.floor(this.scroll / Math.max(1, var19 + var16)));

      for (int var23 = 0; var23 < var15.size(); var23++) {
         int var24 = var23 % var17;
         int var25 = var23 / var17;
         int var26 = var8 + var24 * (var18 + var16);
         int var27 = Math.round(var13 + var1.p(2.0) + var25 * (var19 + var16) - this.scroll);
         if (var27 + var19 >= var13 - var1.p(4.0) && var27 <= var13 + var14 + var1.p(4.0)) {
            int var28 = Math.max(0, (var25 - var22) * var17 + var24);
            float var29 = Ease.outCubic(Ease.clamp((var21 - Math.min(var28, 12) * 22.0F) / 260.0F));
            Gx.pushAlpha(var29);
            Gx.push();
            Gx.translate(0.0F, var1.pf(8.0) * (1.0F - var29));
            this.card(var1, (Module)var15.get(var23), var26, var27, var18, var19, var6);
            Gx.pop();
            Gx.popAlpha();
         }
      }

      if (var15.isEmpty()) {
         Gx.icon("search", var2 + var4 / 2.0F, var13 + var14 / 2.0F - var1.pf(14.0), var1.pf(26.0), Style.muted);
         Gx.textCenter("Keine Module gefunden", var2 + var4 / 2.0F, var13 + var14 / 2.0F + var1.pf(14.0), var1.pf(9.0), 2, Style.sub);
      }

      Kit.unclip();
      Gx.unclip();
      if (this.scroll > 1.0F) {
         Gx.gradientV(var2 + var1.p(1.0), var13, var4 - var1.p(2.0), var1.p(14.0), Style.bg, Style.bg & 16777215);
      }

      if (this.scroll < this.maxScroll - 1.0F) {
         Gx.gradientV(var2 + var1.p(1.0), var13 + var14 - var1.p(14.0), var4 - var1.p(2.0), var1.p(14.0), Style.bg & 16777215, Style.bg);
      }

      if (this.maxScroll > 0.0F) {
         int var30 = var14 - var1.p(8.0);
         int var32 = Math.max(var1.p(24.0), Math.round(var30 * var14 / (var14 + this.maxScroll)));
         int var34 = var13 + var1.p(4.0) + Math.round((var30 - var32) * (this.scroll / this.maxScroll));
         Gx.rect(var2 + var4 - var1.p(9.0), var34, var1.p(3.0), var32, var1.p(2.0), Style.strokeHi);
      }

      int var31 = var3 + var5 - var1.p(40.0);
      Gx.fill(var2 + var1.p(1.0), var31, var4 - var1.p(2.0), Math.max(1, var1.p(0.6)), Style.stroke);
      int var33 = 0;
      int var35 = 0;

      for (Module var38 : Modules.ALL) {
         if (var38.category.listed) {
            var35++;
            if (var38.enabled && var38.isToggleable()) {
               var33++;
            }
         }
      }

      float var37 = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 400.0);
      Gx.circle(var8 + var1.p(4.0), var31 + var1.p(20.0), var1.p(3.4), Gx.withAlpha(Style.ok, var37));
      Gx.textMid(var33 + " aktiv", var8 + var1.p(13.0), var31 + var1.pf(20.0), var1.pf(8.0), 3, Style.text);
      Gx.textMid(
         "von " + var35 + " Modulen",
         var8 + var1.p(13.0) + Gx.width(var33 + " aktiv", var1.pf(8.0), 3) + var1.p(5.0),
         var31 + var1.pf(20.0),
         var1.pf(8.0),
         1,
         Style.muted
      );
      Gx.textRight("Klick = an/aus   •   Rechtsklick = Einstellungen", var2 + var4 - var7, var31 + var1.pf(20.0), var1.pf(7.4F), 1, Style.muted);
   }

   private void card(HubView var1, Module var2, int var3, int var4, int var5, int var6, boolean var7) {
      boolean var8 = var7 && var1.hover(var3, var4, var5, var6);
      float var9 = Ease.to("card:" + var2.id, var8 ? 1.0F : 0.0F, 14.0F);
      float var10 = Ease.to("cardon:" + var2.id, var2.enabled ? 1.0F : 0.0F, 12.0F);
      int var11 = var1.p(10.0);
      Gx.push();
      Gx.translate(0.0F, -var1.pf(1.5) * var9);
      if (var9 > 0.01F || var10 > 0.01F) {
         Gx.shadow(var3, var4, var5, var6, var11, var1.p(7.0), 0.18F * var10 + 0.3F * var9);
      }

      int var12 = Gx.mix(Style.surface, Style.surfaceHover, var9);
      Gx.rectV(var3, var4, var5, var6, var11, Gx.mix(var12, -1, 0.035F + 0.02F * var9), Gx.mix(var12, -16777216, 0.12F));
      if (var10 > 0.01F) {
         Gx.pushAlpha(var10);
         Gx.rect(var3, var4, var5, var6, var11, Gx.withAlpha(Style.accent, 0.06F));
         Gx.rect(var3 + var1.p(3.0), var4 + var1.p(14.0), Math.max(2, var1.p(2.2)), var6 - var1.p(28.0), var1.p(2.0), Style.accent);
         Gx.glow(var3 + var1.p(2.0), var4 + var6 / 2, var1.p(18.0), Gx.withAlpha(Style.accent, 0.25F));
         Gx.popAlpha();
      }

      Gx.outline(
         var3,
         var4,
         var5,
         var6,
         var11,
         Math.max(1, var1.p(0.7)),
         Gx.mix(Gx.mix(Style.stroke, Style.strokeHi, var9), Gx.withAlpha(Style.accent, 0.3F + 0.3F * var9), var10)
      );
      Gx.fill(var3 + var11, var4 + Math.max(1, var1.p(0.7)), var5 - var11 * 2, Math.max(1, var1.p(0.6)), Gx.withAlpha(-1, 0.06F + 0.04F * var9));
      int var13 = var1.p(34.0);
      int var14 = var3 + var1.p(12.0);
      int var15 = var4 + (var6 - var13) / 2;
      if (var10 > 0.01F) {
         Gx.glow(var14 + var13 / 2, var15 + var13 / 2, Math.round(var13 * 0.9F), Gx.withAlpha(Style.accent, 0.22F * var10));
      }

      Gx.rect(var14, var15, var13, var13, var1.p(11.0), Gx.mix(Style.surface2, Style.accent, var10 * 0.92F));
      Gx.outline(var14, var15, var13, var13, var1.p(11.0), Math.max(1, var1.p(0.6)), Gx.withAlpha(-1, 0.08F + 0.1F * var10));
      Gx.icon(var2.icon, var14 + var13 / 2.0F, var15 + var13 / 2.0F, var1.pf(17.0F + var9), Gx.mix(Style.sub, -1, Math.max(var10, var9 * 0.5F)));
      int var16 = var14 + var13 + var1.p(12.0);
      int var17 = var3 + var5 - var1.p(12.0);
      int var18 = var1.p(32.0);
      int var19 = var1.p(18.0);
      int var20 = var17 - var18;
      int var21 = var4 + (var6 - var19) / 2;
      boolean var22 = !var2.settings.isEmpty();
      int var23 = var1.p(24.0);
      int var24 = var20 - var1.p(8.0) - var23;
      int var25 = var4 + (var6 - var23) / 2;
      int var26 = (var22 ? var24 : var20) - var1.p(8.0) - var16;
      float var27 = var1.pf(9.4F);
      String var28 = Fonts.ellipsize(var2.name, 3, var27, var26 - (var2.fresh ? var1.p(30.0) : 0));
      float var29 = Gx.text(var28, var16, var4 + var1.pf(9.0), var27, 3, Style.text);
      if (var2.fresh) {
         int var30 = Math.round(var16 + var29 + var1.pf(6.0));
         int var31 = var4 + var1.p(10.0);
         int var32 = var1.p(26.0);
         int var33 = var1.p(12.0);
         Gx.rect(var30, var31, var32, var33, var33 / 2, -15043);
         Gx.textCenter("NEU", var30 + var32 / 2.0F, var31 + var33 / 2.0F, var1.pf(6.2F), 3, -15068416);
      }

      List var34 = Fonts.wrap(var2.description, 1, var1.pf(7.2F), var26, 2);

      for (int var35 = 0; var35 < var34.size(); var35++) {
         Gx.text((String)var34.get(var35), var16, var4 + var1.pf(25.0F + var35 * 10.5F), var1.pf(7.2F), 1, Style.sub);
      }

      if (var2.isToggleable()) {
         Kit.toggle(var2.id, var20, var21, var18, var19, var2.enabled, var1.P);
      } else {
         Gx.rect(var20 - var1.p(10.0), var21 - var1.p(2.0), var18 + var1.p(10.0), var19 + var1.p(4.0), var1.p(8.0), Style.accent);
         Gx.icon("play", var20 - var1.p(10.0) + (var18 + var1.p(10.0)) / 2.0F, var21 + var19 / 2.0F, var1.pf(9.0), Style.accentText);
      }

      if (var22) {
         boolean var36 = var7 && var1.hover(var24, var25, var23, var23);
         float var37 = Ease.to("gear:" + var2.id, var36 ? 1.0F : 0.0F, 16.0F);
         Gx.rect(var24, var25, var23, var23, var1.p(7.0), Gx.withAlpha(Style.surface2, 0.6F + 0.4F * var37));
         Gx.push();
         Gx.icon("dots", var24 + var23 / 2.0F, var25 + var23 / 2.0F, var1.pf(13.0), Gx.mix(Style.sub, Style.text, var37));
         Gx.pop();
      }

      Gx.pop();
      if (var7) {
         Kit.hit(var3, var4, var5, var6).click(() -> {
            if (var2.isToggleable()) {
               var2.toggle();
               Modules.scheduleSave();
            } else {
               this.openSettings(var2);
            }
         }).right(() -> this.openSettings(var2));
         if (var22) {
            Kit.hit(var24, var25, var23, var23).click(() -> this.openSettings(var2));
         }
      }
   }

   private void openSettings(Module var1) {
      this.open = var1;
      this.binding = null;
      this.editing = null;
      this.sScroll = this.sScrollTarget = 0.0F;
      this.searchFocus = false;
      Sound.pop();
   }

   private void drawSettings(HubView var1, Module var2, int var3, int var4, int var5, int var6, boolean var7) {
      int var8 = var1.p(22.0);
      int var9 = var4 + var1.p(18.0);
      int var10 = var1.p(30.0);
      boolean var11 = var7 && var1.hover(var3 + var8, var9, var10, var10);
      float var12 = Ease.to("set:back", var11 ? 1.0F : 0.0F, 16.0F);
      Gx.rect(var3 + var8, var9, var10, var10, var1.p(9.0), Gx.mix(Style.surface, Style.surfaceHover, var12));
      Gx.icon("back", var3 + var8 + var10 / 2.0F - var1.pf(1.0), var9 + var10 / 2.0F, var1.pf(14.0), Style.text);
      if (var7) {
         Kit.hit(var3 + var8, var9, var10, var10).click(() -> {
            this.open = null;
            this.binding = null;
            this.editing = null;
         });
      }

      int var13 = var1.p(42.0);
      int var14 = var3 + var8 + var10 + var1.p(12.0);
      Gx.rect(var14, var9 - var1.p(6.0), var13, var13, var1.p(12.0), Gx.withAlpha(Style.accent, 0.18F));
      Gx.icon(var2.icon, var14 + var13 / 2.0F, var9 - var1.p(6.0) + var13 / 2.0F, var1.pf(21.0), Style.accent);
      int var15 = var14 + var13 + var1.p(12.0);
      Gx.text(var2.name, var15, var9 - var1.pf(4.0), var1.pf(14.5), 3, Style.text);
      Gx.text(
         Fonts.ellipsize(var2.description, 1, var1.pf(8.0), var5 - (var15 - var3) - var1.p(190.0)), var15, var9 + var1.pf(18.0), var1.pf(8.0), 1, Style.sub
      );
      int var16 = var3 + var5 - var8;
      if (var2.isToggleable()) {
         int var17 = var1.p(40.0);
         int var18 = var1.p(22.0);
         Kit.toggle(var2.id + ":big", var16 - var17, var9 + var1.p(4.0), var17, var18, var2.enabled, var1.P);
         if (var7) {
            Kit.hit(var16 - var17, var9 + var1.p(4.0), var17, var18).click(() -> {
               var2.toggle();
               Modules.scheduleSave();
            });
         }

         String var19 = this.binding == var2 ? "Taste drücken…" : "Taste: " + Keys.name(var2.key);
         float var20 = var1.pf(7.6F);
         int var21 = Math.round(Gx.width(var19, var20, 2)) + var1.p(20.0);
         int var22 = var1.p(22.0);
         int var23 = var16 - var17 - var1.p(10.0) - var21;
         int var24 = var9 + var1.p(4.0);
         boolean var25 = var7 && var1.hover(var23, var24, var21, var22);
         float var26 = Ease.to("set:key", !var25 && this.binding != var2 ? 0.0F : 1.0F, 14.0F);
         Gx.rect(var23, var24, var21, var22, var22 / 2, Gx.mix(Style.surface, Style.surfaceHover, var26));
         Gx.outline(var23, var24, var21, var22, var22 / 2, Math.max(1, var1.p(0.6)), this.binding == var2 ? Style.accent : Style.stroke);
         Gx.textCenter(var19, var23 + var21 / 2.0F, var24 + var22 / 2.0F, var20, 2, this.binding == var2 ? Style.accent : Style.sub);
         if (var7) {
            Kit.hit(var23, var24, var21, var22).click(() -> this.binding = this.binding == var2 ? null : var2).right(() -> {
               var2.key = -1;
               this.binding = null;
               Modules.scheduleSave();
            });
         }
      }

      int var30 = var9 + var1.p(48.0);
      int var31 = var4 + var6 - var1.p(48.0) - var30;
      Gx.fill(var3 + var8, var30 - var1.p(8.0), var5 - var8 * 2, Math.max(1, var1.p(0.6)), Style.stroke);
      int var32 = var1.p(8.0);
      int var33 = 0;

      for (Setting var36 : var2.settings) {
         var33 += this.rowH(var1, var36) + var32;
      }

      this.sMaxScroll = Math.max(0, var33 - var31);
      this.sScrollTarget = Math.max(0.0F, Math.min(this.sMaxScroll, this.sScrollTarget));
      this.sScroll = this.sScroll + (this.sScrollTarget - this.sScroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var3, var30, var5, var31);
      Kit.clip(var3, var30, var5, var31);
      if (var7) {
         Kit.hit(var3, var30, var5, var31).scroll(var2x -> this.sScrollTarget = this.sScrollTarget - var2x.floatValue() * var1.p(40.0));
      }

      int var35 = Math.round(var30 - this.sScroll);
      if (var2.settings.isEmpty()) {
         Gx.textCenter("Dieses Modul hat keine Einstellungen.", var3 + var5 / 2.0F, var30 + var1.pf(40.0), var1.pf(9.0), 2, Style.sub);
      }

      for (int var37 = 0; var37 < var2.settings.size(); var37++) {
         Setting var39 = var2.settings.get(var37);
         int var41 = this.rowH(var1, var39);
         if (var35 + var41 >= var30 && var35 <= var30 + var31) {
            this.row(var1, var2, var39, var3 + var8, var35, var5 - var8 * 2, var41, var7, var37);
         }

         var35 += var41 + var32;
      }

      Kit.unclip();
      Gx.unclip();
      int var38 = var4 + var6 - var1.p(40.0);
      Gx.fill(var3 + var1.p(1.0), var38, var5 - var1.p(2.0), Math.max(1, var1.p(0.6)), Style.stroke);
      String var40 = "Standard wiederherstellen";
      int var42 = Math.round(Gx.width(var40, var1.pf(7.8F), 2)) + var1.p(34.0);
      int var43 = var1.p(24.0);
      int var44 = var3 + var5 - var8 - var42;
      int var27 = var38 + var1.p(8.0);
      boolean var28 = var7 && var1.hover(var44, var27, var42, var43);
      float var29 = Ease.to("set:reset", var28 ? 1.0F : 0.0F, 14.0F);
      Gx.rect(var44, var27, var42, var43, var43 / 2, Gx.mix(Style.surface, Style.surfaceHover, var29));
      Gx.icon("reset", var44 + var1.p(14.0), var27 + var43 / 2.0F, var1.pf(10.0), Gx.mix(Style.sub, Style.danger, var29));
      Gx.textMid(var40, var44 + var1.p(24.0), var27 + var43 / 2.0F, var1.pf(7.8F), 2, Style.sub);
      if (var7) {
         Kit.hit(var44, var27, var42, var43).click(() -> {
            var2.resetSettings();
            Modules.scheduleSave();
         });
      }

      Gx.textMid(var2.category.title + "  •  " + var2.settings.size() + " Einstellungen", var3 + var8, var38 + var1.pf(20.0), var1.pf(7.6F), 1, Style.muted);
   }

   private int rowH(HubView var1, Setting var2) {
      return var2 instanceof Setting.Color ? var1.p(66.0) : var1.p(44.0);
   }

   private void row(HubView var1, Module var2, Setting var3, int var4, int var5, int var6, int var7, boolean var8, int var9) {
      String var10 = var2.id + ":" + var3.name;
      boolean var11 = var8 && var1.hover(var4, var5, var6, var7);
      float var12 = Ease.to("row:" + var10, var11 ? 1.0F : 0.0F, 14.0F);
      Gx.rect(var4, var5, var6, var7, var1.p(10.0), Gx.mix(Style.surface, Style.surfaceHover, var12 * 0.6F));
      float var13 = var1.pf(9.0);
      int var14 = var3 instanceof Setting.Color ? var5 + var1.p(20.0) : var5 + var7 / 2;
      Gx.textMid(var3.name, var4 + var1.p(16.0), var14, var13, 2, Style.text);
      int var15 = var4 + var6 - var1.p(16.0);
      if (var3 instanceof Setting.Bool var16) {
         int var17 = var1.p(32.0);
         int var18 = var1.p(18.0);
         Kit.toggle(var10, var15 - var17, var14 - var18 / 2, var17, var18, var16.value, var1.P);
         if (var8) {
            Kit.hit(var4, var5, var6, var7).click(() -> {
               var16.value = !var16.value;
               Modules.scheduleSave();
            });
         }
      } else if (var3 instanceof Setting.Num var30) {
         String var35 = var30.display();
         float var40 = var1.pf(8.2F);
         int var19 = var1.p(58.0);
         int var20 = var1.p(58.0);
         int var21 = var1.p(22.0);
         Gx.rect(var15 - var20, var14 - var21 / 2, var20, var21, var1.p(7.0), Style.surface2);
         Gx.textCenter(var35, var15 - var20 / 2.0F, var14, var40, 3, Style.text);
         int var22 = Math.min(var1.p(220.0), var6 / 2);
         int var23 = var15 - var19 - var1.p(14.0) - var22;
         Kit.slider(var10, var23, var14 - var1.p(9.0), var22, var1.p(18.0), (float)var30.frac(), var1.P);
         if (var8) {
            Kit.hit(var23 - var1.p(6.0), var5, var22 + var1.p(12.0), var7).drag((var3x, var4x) -> {
               var30.setFrac((double)(var3x - var23) / var22);
               Modules.scheduleSave();
            });
         }
      } else if (var3 instanceof Setting.Mode var31) {
         float var36 = var1.pf(7.8F);
         int var41 = var1.p(10.0);
         int var45 = 0;

         for (String var64 : var31.options) {
            var45 += Math.round(Gx.width(var64, var36, 2)) + var41 * 2;
         }

         int var50 = var1.p(26.0);
         if (var31.options.length <= 5 && var45 <= var6 * 0.62F) {
            int var56 = var15 - var45;
            Gx.rect(var56 - var1.p(3.0), var14 - var50 / 2 - var1.p(3.0), var45 + var1.p(6.0), var50 + var1.p(6.0), var1.p(9.0), Style.surface2);
            int var61 = var56;
            int[] var66 = new int[var31.options.length];
            int[] var69 = new int[var31.options.length];

            for (int var72 = 0; var72 < var31.options.length; var72++) {
               var66[var72] = var61;
               var69[var72] = Math.round(Gx.width(var31.options[var72], var36, 2)) + var41 * 2;
               var61 += var69[var72];
            }

            float var73 = Ease.to("segx:" + var10, var66[var31.index], 18.0F, var66[var31.index]);
            float var26 = Ease.to("segw:" + var10, var69[var31.index], 18.0F, var69[var31.index]);
            Gx.rect(Math.round(var73), var14 - var50 / 2, Math.round(var26), var50, var1.p(7.0), Style.accent);

            for (int var27 = 0; var27 < var31.options.length; var27++) {
               boolean var28 = var27 == var31.index;
               Gx.textCenter(var31.options[var27], var66[var27] + var69[var27] / 2.0F, var14, var36, 2, var28 ? Style.accentText : Style.sub);
               int var29 = var27;
               if (var8) {
                  Kit.hit(var66[var27], var14 - var50 / 2, var69[var27], var50).click(() -> {
                     var31.index = var29;
                     Modules.scheduleSave();
                  });
               }
            }
         } else {
            int var55 = var1.p(190.0);
            int var65 = var15 - var55;
            Gx.rect(var65, var14 - var50 / 2, var55, var50, var1.p(8.0), Style.surface2);
            boolean var24 = var8 && var1.hover(var65, var14 - var50 / 2, var50, var50);
            boolean var25 = var8 && var1.hover(var65 + var55 - var50, var14 - var50 / 2, var50, var50);
            Gx.icon("chevron-left", var65 + var50 / 2.0F, var14, var1.pf(11.0), var24 ? Style.accent : Style.sub);
            Gx.icon("chevron-right", var65 + var55 - var50 / 2.0F, var14, var1.pf(11.0), var25 ? Style.accent : Style.sub);
            Gx.textCenter(Fonts.ellipsize(var31.get(), 2, var36, var55 - var50 * 2), var65 + var55 / 2.0F, var14, var36, 2, Style.text);
            Gx.textRight(var31.index + 1 + "/" + var31.options.length, var65 - var1.p(10.0), var14, var1.pf(7.0), 1, Style.muted);
            if (var8) {
               Kit.hit(var65, var14 - var50 / 2, var55 / 2, var50).click(() -> {
                  var31.cycle(-1);
                  Modules.scheduleSave();
               });
               Kit.hit(var65 + var55 / 2, var14 - var50 / 2, var55 - var55 / 2, var50).click(() -> {
                  var31.cycle(1);
                  Modules.scheduleSave();
               });
            }
         }
      } else if (var3 instanceof Setting.Color var32) {
         int var37 = Setting.Color.PALETTE.length;
         int var42 = var1.p(20.0);
         int var46 = var1.p(9.0);
         int var51 = var4 + var1.p(16.0);
         int var57 = var5 + var1.p(34.0);

         for (int var62 = 0; var62 < var37; var62++) {
            int var67 = var51 + var62 * (var42 + var46);
            boolean var70 = var32.index == var62;
            boolean var74 = var8 && var1.hover(var67, var57, var42, var42);
            float var76 = Ease.to("sw:" + var10 + var62, var70 ? 1.12F : (var74 ? 1.08F : 1.0F), 16.0F);
            Gx.push();
            Gx.scaleAt(var67 + var42 / 2.0F, var57 + var42 / 2.0F, var76);
            if (var62 == var37 - 1) {
               Gx.image(rainbow(var42), var67, var57, var42, var42, -1);
            } else {
               Gx.circle(var67 + var42 / 2, var57 + var42 / 2, var42 / 2, Setting.Color.PALETTE[var62]);
            }

            if (var70) {
               Gx.ringAt(var67 + var42 / 2, var57 + var42 / 2, var42 / 2 + var1.p(3.0), Math.max(1, var1.p(1.4)), Style.text);
            }

            Gx.pop();
            int var77 = var62;
            if (var8) {
               Kit.hit(var67 - var46 / 2, var57 - var1.p(4.0), var42 + var46, var42 + var1.p(8.0)).click(() -> {
                  var32.index = var77;
                  Modules.scheduleSave();
               });
            }
         }

         Gx.textRight(Setting.Color.NAMES[var32.index], var15, var5 + var1.p(20.0), var1.pf(8.0), 2, Style.sub);
      } else if (var3 instanceof Setting.Text var33) {
         boolean var38 = this.editing == var33;
         float var43 = var1.pf(8.0);
         int var47 = Math.min(var1.p(260.0), var6 / 2);
         int var52 = var1.p(26.0);
         int var58 = var15 - var47;
         float var63 = Ease.to("txt:" + var10, var38 ? 1.0F : 0.0F, 14.0F);
         Gx.rect(var58, var14 - var52 / 2, var47, var52, var1.p(8.0), Gx.mix(Style.surface2, Style.surfaceHover, var63));
         Gx.outline(var58, var14 - var52 / 2, var47, var52, var1.p(8.0), Math.max(1, var1.p(0.7)), Gx.mix(Style.stroke, Style.accent, var63));
         String var68 = var33.value.isEmpty() && !var38 ? var33.hint : var33.value;
         String var71 = var68;

         while (Gx.width(var71, var43, 2) > var47 - var1.p(24.0) && var71.length() > 1) {
            var71 = var71.substring(1);
         }

         float var75 = Gx.textMid(var71, var58 + var1.p(10.0), var14, var43, 2, var33.value.isEmpty() && !var38 ? Style.muted : Style.text);
         if (var38 && System.currentTimeMillis() / 500L % 2L == 0L) {
            Gx.fill(Math.round(var58 + var1.p(11.0) + var75), var14 - var1.p(6.0), Math.max(1, var1.p(0.9)), var1.p(12.0), Style.accent);
         }

         if (var8) {
            Kit.hit(var58, var14 - var52 / 2, var47, var52).click(() -> this.editing = var38 ? null : var33);
         }
      } else if (var3 instanceof Setting.Action var34) {
         float var39 = var1.pf(8.0);
         int var44 = Math.round(Gx.width(var34.label, var39, 3)) + var1.p(30.0);
         int var48 = var1.p(26.0);
         int var53 = var15 - var44;
         boolean var59 = var8 && var1.hover(var53, var14 - var48 / 2, var44, var48);
         Kit.button(var10, var34.label, null, var53, var14 - var48 / 2, var44, var48, var1.P, true, var59);
         if (var8) {
            Kit.hit(var53, var14 - var48 / 2, var44, var48).click(() -> {
               try {
                  var34.action.run();
               } catch (Throwable var2x) {
                  LegoClient.LOG("Aktion: " + var2x);
               }
            });
         }
      }
   }

   static Gx.Img rainbow(int var0) {
      return Gx.painted("rainbow", var0, var0, var1 -> {
         for (byte var2 = 0; var2 < 360; var2 += 3) {
            var1.setColor(Color.getHSBColor(var2 / 360.0F, 0.75F, 1.0F));
            var1.fill(new Double(0.0, 0.0, var0, var0, var2, 4.0, 2));
         }
      });
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 < 0) {
         return false;
      } else if (this.editing != null) {
         if (var1 == 259 && !this.editing.value.isEmpty()) {
            this.editing.value = this.editing.value.substring(0, this.editing.value.length() - 1);
         } else if (var1 == 256 || var1 == 257 || var1 == 335) {
            this.editing = null;
         } else if (var1 == 86 && (var2 & 2) != 0) {
            String var3 = Env.clipboard.get();
            if (var3 != null) {
               this.editing.value = (this.editing.value + var3.replace('\n', ' '))
                  .substring(0, Math.min(this.editing.max, this.editing.value.length() + var3.length()));
            }
         }

         Modules.scheduleSave();
         return true;
      } else if (this.binding != null) {
         if (var1 != 256 && var1 != 259 && var1 != 261) {
            this.binding.key = var1;
         } else {
            this.binding.key = -1;
         }

         this.binding = null;
         Modules.scheduleSave();
         return true;
      } else if (this.open != null) {
         if (var1 == 256) {
            this.open = null;
            this.editing = null;
            return true;
         } else {
            return false;
         }
      } else if (var1 == 259 && !this.search.isEmpty()) {
         this.search = this.search.substring(0, this.search.length() - 1);
         this.listChangedAt = System.currentTimeMillis();
         this.scrollTarget = 0.0F;
         return true;
      } else if (var1 != 256 || this.search.isEmpty() && !this.searchFocus) {
         if (var1 == 258) {
            this.setTab((tab + 1) % TABS.length);
            return true;
         } else {
            return false;
         }
      } else {
         this.search = "";
         this.searchFocus = false;
         this.listChangedAt = System.currentTimeMillis();
         return true;
      }
   }

   @Override
   public boolean chr(String var1) {
      if (this.editing != null && var1 != null) {
         if (this.editing.value.length() + var1.length() <= this.editing.max) {
            this.editing.value = this.editing.value + var1;
         }

         Modules.scheduleSave();
         return true;
      } else if (this.binding == null && this.open == null && var1 != null && !var1.isEmpty()) {
         if (this.search.length() > 24) {
            return true;
         } else {
            this.search = this.search + var1;
            this.searchFocus = true;
            this.scrollTarget = 0.0F;
            this.listChangedAt = System.currentTimeMillis();
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean bindMouse(int var1) {
      if (this.binding != null && var1 >= 2) {
         this.binding.key = var1;
         this.binding = null;
         Modules.scheduleSave();
         return true;
      } else {
         return false;
      }
   }
}
