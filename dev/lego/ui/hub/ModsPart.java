package dev.lego.ui.hub;

import dev.lego.LegoClient;
import dev.lego.core.Category;
import dev.lego.core.Lang;
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
   private boolean renaming;
   private String renameText = "";
   private long listChangedAt = System.currentTimeMillis();
   private static boolean showHidden;

   private static String[] tabs() {
      return new String[]{Lang.get("ui.all", "ALLE"), "HUD", "VISUALS", "UTILITY", Lang.get("ui.perf", "LEISTUNG"), "SPOTIFY"};
   }

   private List<Module> list() {
      List<Module> out = new ArrayList<>();
      String q = this.search.toLowerCase(Locale.ROOT).trim();

      for (Module m : Modules.ALL) {
         if (m.category.listed) {
            if (!q.isEmpty()) {
               boolean renamed = m.customName != null && !m.customName.isEmpty();
               String dn = m.displayName().toLowerCase(Locale.ROOT);
               String dd = m.displayDesc().toLowerCase(Locale.ROOT);
               if (renamed
                  ? dn.contains(q) || dd.contains(q) || m.category.displayTitle().toLowerCase(Locale.ROOT).contains(q)
                  : m.name.toLowerCase(Locale.ROOT).contains(q)
                     || m.description.toLowerCase(Locale.ROOT).contains(q)
                     || dn.contains(q)
                     || dd.contains(q)
                     || m.category.displayTitle().toLowerCase(Locale.ROOT).contains(q)) {
                  out.add(m);
               }
            } else if (TAB_CATS[tab] == null || m.category == TAB_CATS[tab]) {
               out.add(m);
            }
         }
      }

      return out;
   }

   private void setTab(int i) {
      if (i != tab || !this.search.isEmpty()) {
         tab = i;
         this.search = "";
         this.scroll = this.scrollTarget = 0.0F;
         this.listChangedAt = System.currentTimeMillis();
      }
   }

   @Override
   public void draw(HubView v, int x, int y, int w, int h) {
      float sp = Ease.to("mods:sp", this.open != null ? 1.0F : 0.0F, 13.0F);
      if (this.open != null) {
         this.shownOpen = this.open;
      }

      if (sp < 0.01F && this.open == null) {
         this.shownOpen = null;
      }

      if (sp < 0.99F) {
         Gx.pushAlpha(1.0F - sp);
         Gx.push();
         Gx.translate(-v.pf(40.0) * sp, 0.0F);
         this.drawList(v, x, y, w, h, sp < 0.5F);
         Gx.pop();
         Gx.popAlpha();
      }

      if (sp > 0.01F && this.shownOpen != null) {
         Gx.pushAlpha(sp);
         Gx.push();
         Gx.translate(v.pf(60.0) * (1.0F - Ease.outCubic(sp)), 0.0F);
         this.drawSettings(v, this.shownOpen, x, y, w, h, sp > 0.5F);
         Gx.pop();
         Gx.popAlpha();
      }
   }

   private void drawList(HubView v, int x, int y, int w, int h, boolean interactive) {
      int pad = v.p(22.0);
      int tx = x + pad;
      int ty = y + v.p(20.0);
      v.tabs("mods", tx, ty, tabs(), this.search.isEmpty() ? tab : 0, this::setTab);
      int sw = v.p(168.0);
      int sh = v.p(26.0);
      int sx = x + w - pad - sw;
      v.searchBox("mods", sx, ty, sw, sh, this.search, this.searchFocus);
      if (interactive) {
         Kit.hit(sx, ty, sw, sh).click(() -> {
            if (!this.search.isEmpty() && v.mx > sx + sw - sh) {
               this.search = "";
               this.listChangedAt = System.currentTimeMillis();
            }

            this.searchFocus = true;
         });
      }

      int ly = ty + sh + v.p(16.0);
      int lh = y + h - v.p(40.0) - ly;
      List<Module> mods = this.list();
      int style = UiSettings.guiStyle;
      if (style >= 1) {
         int rowH = style == 2 ? v.p(44.0) : v.p(52.0);
         int headerH = v.p(28.0);
         int gap = style == 2 ? v.p(4.0) : v.p(6.0);
         boolean showHeaders = TAB_CATS[tab] == null && this.search.isEmpty();
         int totalH = 0;
         Category lastCat = null;

         for (Module m : mods) {
            if (showHeaders && m.category != lastCat) {
               if (lastCat != null) {
                  totalH += v.p(10.0);
               }

               totalH += headerH;
               lastCat = m.category;
            }

            totalH += rowH + gap;
         }

         this.maxScroll = Math.max(0, totalH - lh + v.p(20.0));
         this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
         this.scroll = this.scroll + (this.scrollTarget - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
         Gx.clip(x + pad - v.p(6.0), ly, w - pad * 2 + v.p(12.0), lh);
         Kit.clip(x, ly, w, lh);
         if (interactive) {
            Kit.hit(x, ly, w, lh).scroll(ax -> this.scrollTarget = this.scrollTarget - ax.floatValue() * v.p(48.0));
         }

         float el = (float)(System.currentTimeMillis() - this.listChangedAt) * UiSettings.anim();
         int cy = Math.round(ly + v.p(2.0) - this.scroll);
         lastCat = null;
         int vis = 0;

         for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            if (showHeaders && m.category != lastCat) {
               if (lastCat != null) {
                  cy += v.p(10.0);
               }

               if (cy + headerH >= ly - v.p(4.0) && cy <= ly + lh + v.p(4.0)) {
                  this.modernCatHeader(v, m.category, x + pad, cy, w - pad * 2, headerH);
               }

               cy += headerH;
               lastCat = m.category;
            }

            if (cy + rowH >= ly - v.p(4.0) && cy <= ly + lh + v.p(4.0)) {
               float a = Ease.outCubic(Ease.clamp((el - Math.min(vis, 12) * 22.0F) / 260.0F));
               Gx.pushAlpha(a);
               Gx.push();
               Gx.translate(0.0F, v.pf(6.0) * (1.0F - a));
               if (style == 2) {
                  this.glassRow(v, m, x + pad, cy, w - pad * 2, rowH, interactive);
               } else {
                  this.modernRow(v, m, x + pad, cy, w - pad * 2, rowH, interactive);
               }

               Gx.pop();
               Gx.popAlpha();
            }

            cy += rowH + gap;
            vis++;
         }

         if (mods.isEmpty()) {
            Gx.icon("search", x + w / 2.0F, ly + lh / 2.0F - v.pf(14.0), v.pf(26.0), Style.muted);
            Gx.textCenter(Lang.get("ui.nomods", "Keine Module gefunden"), x + w / 2.0F, ly + lh / 2.0F + v.pf(14.0), v.pf(9.0), 2, Style.sub);
         }

         Kit.unclip();
         Gx.unclip();
      } else {
         int gap = v.p(8.0);
         int cols = (w - pad * 2) / v.p(300.0) >= 3 ? 3 : 2;
         int cw = (w - pad * 2 - gap * (cols - 1)) / cols;
         int ch = v.p(54.0);
         int rows = (mods.size() + cols - 1) / cols;
         this.maxScroll = Math.max(0, rows * (ch + gap) - gap - lh + v.p(20.0));
         this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
         this.scroll = this.scroll + (this.scrollTarget - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
         Gx.clip(x + pad - v.p(6.0), ly, w - pad * 2 + v.p(12.0), lh);
         Kit.clip(x, ly, w, lh);
         if (interactive) {
            Kit.hit(x, ly, w, lh).scroll(ax -> this.scrollTarget = this.scrollTarget - ax.floatValue() * v.p(48.0));
         }

         float el = (float)(System.currentTimeMillis() - this.listChangedAt) * UiSettings.anim();
         int firstRow = Math.max(0, (int)Math.floor(this.scroll / Math.max(1, ch + gap)));

         for (int i = 0; i < mods.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cx = tx + col * (cw + gap);
            int cy = Math.round(ly + v.p(2.0) + row * (ch + gap) - this.scroll);
            if (cy + ch >= ly - v.p(4.0) && cy <= ly + lh + v.p(4.0)) {
               int vis = Math.max(0, (row - firstRow) * cols + col);
               float a = Ease.outCubic(Ease.clamp((el - Math.min(vis, 12) * 22.0F) / 260.0F));
               Gx.pushAlpha(a);
               Gx.push();
               Gx.translate(0.0F, v.pf(8.0) * (1.0F - a));
               this.card(v, mods.get(i), cx, cy, cw, ch, interactive);
               Gx.pop();
               Gx.popAlpha();
            }
         }

         if (mods.isEmpty()) {
            Gx.icon("search", x + w / 2.0F, ly + lh / 2.0F - v.pf(14.0), v.pf(26.0), Style.muted);
            Gx.textCenter(Lang.get("ui.nomods", "Keine Module gefunden"), x + w / 2.0F, ly + lh / 2.0F + v.pf(14.0), v.pf(9.0), 2, Style.sub);
         }

         Kit.unclip();
         Gx.unclip();
      }

      if (this.scroll > 1.0F) {
         Gx.gradientV(x + v.p(1.0), ly, w - v.p(2.0), v.p(14.0), Style.bg, Style.bg & 16777215);
      }

      if (this.scroll < this.maxScroll - 1.0F) {
         Gx.gradientV(x + v.p(1.0), ly + lh - v.p(14.0), w - v.p(2.0), v.p(14.0), Style.bg & 16777215, Style.bg);
      }

      if (this.maxScroll > 0.0F) {
         int trackH = lh - v.p(8.0);
         int thumbH = Math.max(v.p(24.0), Math.round(trackH * lh / (lh + this.maxScroll)));
         int thumbY = ly + v.p(4.0) + Math.round((trackH - thumbH) * (this.scroll / this.maxScroll));
         Gx.rect(x + w - v.p(9.0), thumbY, v.p(3.0), thumbH, v.p(2.0), Style.strokeHi);
      }

      int fy = y + h - v.p(40.0);
      Gx.fill(x + v.p(1.0), fy, w - v.p(2.0), Math.max(1, v.p(0.6)), Style.stroke);
      int active = 0;
      int total = 0;

      for (Module mx : Modules.ALL) {
         if (mx.category.listed) {
            total++;
            if (mx.enabled && mx.isToggleable()) {
               active++;
            }
         }
      }

      float pulse = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 400.0);
      Gx.circle(tx + v.p(4.0), fy + v.p(20.0), v.p(3.4), Gx.withAlpha(Style.ok, pulse));
      String activeL = active + " " + Lang.get("ui.active", "aktiv");
      Gx.textMid(activeL, tx + v.p(13.0), fy + v.pf(20.0), v.pf(8.0), 3, Style.text);
      Gx.textMid(
         Lang.get("ui.of", "von") + " " + total + " " + Lang.get("ui.modules", "Modulen"),
         tx + v.p(13.0) + Gx.width(activeL, v.pf(8.0), 3) + v.p(5.0),
         fy + v.pf(20.0),
         v.pf(8.0),
         1,
         Style.muted
      );
      Gx.textRight(Lang.get("ui.hint", "Klick = an/aus   •   Rechtsklick = Einstellungen"), x + w - pad, fy + v.pf(20.0), v.pf(7.4F), 1, Style.muted);
   }

   private void card(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
      boolean hv = interactive && v.hover(x, y, w, h);
      float hov = Ease.to("card:" + m.id, hv ? 1.0F : 0.0F, 14.0F);
      float on = Ease.to("cardon:" + m.id, m.enabled ? 1.0F : 0.0F, 12.0F);
      int r = v.p(10.0);
      Gx.push();
      Gx.translate(0.0F, -v.pf(1.5) * hov);
      if (hov > 0.01F || on > 0.01F) {
         Gx.shadow(x, y, w, h, r, v.p(7.0), 0.18F * on + 0.3F * hov);
      }

      int base = Gx.mix(Style.surface, Style.surfaceHover, hov);
      Gx.rectV(x, y, w, h, r, Gx.mix(base, -1, 0.035F + 0.02F * hov), Gx.mix(base, -16777216, 0.12F));
      if (on > 0.01F) {
         Gx.pushAlpha(on);
         Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.06F));
         Gx.rect(x + v.p(3.0), y + v.p(14.0), Math.max(2, v.p(2.2)), h - v.p(28.0), v.p(2.0), Style.accent);
         Gx.glow(x + v.p(2.0), y + h / 2, v.p(18.0), Gx.withAlpha(Style.accent, 0.25F));
         Gx.popAlpha();
      }

      Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.7F)), Gx.mix(Gx.mix(Style.stroke, Style.strokeHi, hov), Gx.withAlpha(Style.accent, 0.3F + 0.3F * hov), on));
      Gx.fill(x + r, y + Math.max(1, v.p(0.7)), w - r * 2, Math.max(1, v.p(0.6)), Gx.withAlpha(-1, 0.06F + 0.04F * hov));
      int is = v.p(34.0);
      int ix = x + v.p(12.0);
      int iy = y + (h - is) / 2;
      if (on > 0.01F) {
         Gx.glow(ix + is / 2, iy + is / 2, Math.round(is * 0.9F), Gx.withAlpha(Style.accent, 0.22F * on));
      }

      Gx.rect(ix, iy, is, is, v.p(11.0), Gx.mix(Style.surface2, Style.accent, on * 0.92F));
      Gx.outline(ix, iy, is, is, v.p(11.0), Math.max(1, v.p(0.6)), Gx.withAlpha(-1, 0.08F + 0.1F * on));
      Gx.icon(m.icon, ix + is / 2.0F, iy + is / 2.0F, v.pf(17.0F + hov), Gx.mix(Style.sub, -1, Math.max(on, hov * 0.5F)));
      int tx = ix + is + v.p(12.0);
      int right = x + w - v.p(12.0);
      int tw = v.p(32.0);
      int th = v.p(18.0);
      int togX = right - tw;
      int togY = y + (h - th) / 2;
      boolean hasSettings = !m.settings.isEmpty();
      int gearS = v.p(24.0);
      int gearX = togX - v.p(8.0) - gearS;
      int gearY = y + (h - gearS) / 2;
      int textMax = (hasSettings ? gearX : togX) - v.p(8.0) - tx;
      float nameSize = v.pf(9.4F);
      String name = Fonts.ellipsize(m.displayName(), 3, nameSize, textMax - (m.fresh ? v.p(30.0) : 0));
      float nw = Gx.text(name, tx, y + v.pf(9.0), nameSize, 3, Style.text);
      if (m.hidden) {
         int bx = Math.round(tx + nw + v.pf(6.0));
         int by = y + v.p(10.0);
         int bw = v.p(14.0);
         int bh = v.p(12.0);
         Gx.icon("eye_off", bx + bw / 2.0F, by + bh / 2.0F, v.pf(9.0), Style.muted);
      } else if (m.fresh) {
         int bx = Math.round(tx + nw + v.pf(6.0));
         int by = y + v.p(10.0);
         int bw = v.p(26.0);
         int bh = v.p(12.0);
         Gx.rect(bx, by, bw, bh, bh / 2, -15043);
         Gx.textCenter(Lang.get("ui.new", "NEU"), bx + bw / 2.0F, by + bh / 2.0F, v.pf(6.2F), 3, -15068416);
      }

      List<String> lines = Fonts.wrap(m.displayDesc(), 1, v.pf(7.2F), textMax, 2);

      for (int i = 0; i < lines.size(); i++) {
         Gx.text(lines.get(i), tx, y + v.pf(25.0F + i * 10.5F), v.pf(7.2F), 1, Style.sub);
      }

      if (m.isToggleable()) {
         Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
      } else {
         Gx.rect(togX - v.p(10.0), togY - v.p(2.0), tw + v.p(10.0), th + v.p(4.0), v.p(8.0), Style.accent);
         Gx.icon("play", togX - v.p(10.0) + (tw + v.p(10.0)) / 2.0F, togY + th / 2.0F, v.pf(9.0), Style.accentText);
      }

      if (hasSettings) {
         boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
         float hgv = Ease.to("gear:" + m.id, hg ? 1.0F : 0.0F, 16.0F);
         Gx.rect(gearX, gearY, gearS, gearS, v.p(7.0), Gx.withAlpha(Style.surface2, 0.6F + 0.4F * hgv));
         Gx.icon("dots", gearX + gearS / 2.0F, gearY + gearS / 2.0F, v.pf(13.0), Gx.mix(Style.sub, Style.text, hgv));
      }

      Gx.pop();
      if (interactive) {
         Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) {
               m.toggle();
               Modules.scheduleSave();
            } else {
               this.openSettings(m);
            }
         }).right(() -> this.openSettings(m));
         if (hasSettings) {
            Kit.hit(gearX, gearY, gearS, gearS).click(() -> this.openSettings(m));
         }
      }
   }

   private void modernCatHeader(HubView v, Category cat, int x, int y, int w, int h) {
      float fs = v.pf(7.2F);
      Gx.icon(cat.icon, x + v.p(4.0), y + h / 2.0F, v.pf(11.0), Style.muted);
      String title = cat.displayTitle().toUpperCase(Locale.ROOT);
      float tw = Gx.textMid(title, x + v.p(20.0), y + h / 2.0F, fs, 3, Style.muted);
      Gx.fill(Math.round(x + v.p(24.0) + tw), y + h / 2, Math.round(w - v.p(28.0) - tw), Math.max(1, v.p(0.5)), Gx.withAlpha(Style.stroke, 0.5F));
   }

   private void modernRow(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
      boolean hv = interactive && v.hover(x, y, w, h);
      float hov = Ease.to("mrow:" + m.id, hv ? 1.0F : 0.0F, 11.0F);
      float on = Ease.to("mron:" + m.id, m.enabled ? 1.0F : 0.0F, 10.0F);
      int r = v.p(16.0);
      Gx.push();
      float sc = 1.0F + 0.012F * hov;
      Gx.scaleAt(x + w / 2.0F, y + h / 2.0F, sc);
      Gx.translate(0.0F, -v.pf(2.5) * hov);
      if (hov > 0.02F || on > 0.01F) {
         Gx.shadow(x, y, w, h, r, v.p(12.0), 0.12F + 0.25F * hov + 0.15F * on);
      }

      Gx.rect(x, y, w, h, r, Gx.withAlpha(Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6F), 0.82F + 0.12F * hov));
      if (on > 0.01F) {
         Gx.pushAlpha(on);
         Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.06F + 0.06F * hov));
         Gx.gradientV(x, y, v.p(4.0), h, Gx.withAlpha(Style.accent, 0.9F), Gx.withAlpha(Style.accent, 0.3F));
         Gx.glow(x + v.p(3.0), y + h / 2, v.p(28.0), Gx.withAlpha(Style.accent, 0.2F));
         Gx.popAlpha();
      }

      Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.7)), Gx.mix(Gx.mix(Style.stroke, Style.strokeHi, hov), Gx.withAlpha(Style.accent, 0.4F), on));
      if (hov > 0.01F) {
         Gx.fill(x + r, y + Math.max(1, v.p(0.5)), w - r * 2, Math.max(1, v.p(0.5)), Gx.withAlpha(-1, 0.06F * hov));
      }

      int is = v.p(36.0);
      int ix = x + v.p(14.0);
      int iy = y + (h - is) / 2;
      int iconBg = Gx.mix(Style.surface2, Style.accent, on * 0.8F + hov * 0.2F);
      Gx.circle(ix + is / 2, iy + is / 2, is / 2, iconBg);
      if (on > 0.3F) {
         Gx.glow(ix + is / 2, iy + is / 2, v.p(22.0), Gx.withAlpha(Style.accent, 0.18F * on));
      }

      Gx.outline(ix, iy, is, is, is / 2, Math.max(1, v.p(0.5)), Gx.withAlpha(-1, 0.08F + 0.12F * on));
      Gx.icon(m.icon, ix + is / 2.0F, iy + is / 2.0F, v.pf(16.0F + hov * 1.2F), Gx.mix(Style.sub, -1, Math.max(on, hov * 0.6F)));
      int tx = ix + is + v.p(14.0);
      int right = x + w - v.p(14.0);
      int tw = v.p(34.0);
      int th = v.p(20.0);
      int togX = right - tw;
      int togY = y + (h - th) / 2;
      boolean hasSettings = !m.settings.isEmpty();
      int gearS = v.p(26.0);
      int gearX = togX - v.p(8.0) - gearS;
      int gearY = y + (h - gearS) / 2;
      int textMax = (hasSettings ? gearX : togX) - v.p(8.0) - tx;
      float nameSize = v.pf(10.0);
      String name = m.displayName();
      float nw = Gx.width(name, nameSize, 3);
      float descSize = v.pf(7.4F);
      String sep = "  ·  ";
      float sepW = Gx.width(sep, descSize, 1);
      if (nw + sepW + v.p(30.0) < textMax) {
         String eName = Fonts.ellipsize(name, 3, nameSize, textMax * 0.45F);
         nw = Gx.textMid(eName, tx, y + h / 2.0F - v.pf(1.0), nameSize, 3, Style.text);
         if (m.hidden) {
            Gx.icon("eye_off", Math.round(tx + nw + v.p(6.0)), y + h / 2, v.pf(9.0), Style.muted);
            nw += v.p(15.0);
         } else if (m.fresh) {
            int bx = Math.round(tx + nw + v.pf(7.0));
            int by = Math.round(y + h / 2.0F - v.pf(5.0));
            Gx.rect(bx, by, v.p(6.0), v.p(6.0), v.p(3.0), -15043);
            nw += v.p(16.0);
         }

         float sx2 = tx + nw;
         Gx.textMid(sep, sx2, y + h / 2.0F - v.pf(1.0), descSize, 1, Style.muted);
         int descMax = Math.round(textMax - nw - sepW);
         String desc = Fonts.ellipsize(m.displayDesc(), 1, descSize, descMax);
         Gx.textMid(desc, sx2 + sepW, y + h / 2.0F - v.pf(1.0), descSize, 1, Style.sub);
      } else {
         name = Fonts.ellipsize(name, 3, nameSize, textMax);
         Gx.textMid(name, tx, y + h / 2.0F - v.pf(1.0), nameSize, 3, Style.text);
      }

      String cat = m.category.displayTitle().toUpperCase(Locale.ROOT);
      Gx.text(cat, tx, y + h / 2.0F + v.pf(9.0), v.pf(6.0), 2, Gx.withAlpha(Style.muted, 0.7F));
      if (m.isToggleable()) {
         Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
      } else {
         Gx.rect(togX - v.p(8.0), togY - v.p(2.0), tw + v.p(8.0), th + v.p(4.0), v.p(10.0), Style.accent);
         Gx.icon("play", togX - v.p(8.0) + (tw + v.p(8.0)) / 2.0F, togY + th / 2.0F, v.pf(10.0), Style.accentText);
      }

      if (hasSettings) {
         boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
         float hgv = Ease.to("mgr:" + m.id, hg ? 1.0F : 0.0F, 14.0F);
         Gx.circle(gearX + gearS / 2, gearY + gearS / 2, gearS / 2, Gx.withAlpha(Style.surface2, 0.4F + 0.6F * hgv));
         Gx.icon("dots", gearX + gearS / 2.0F, gearY + gearS / 2.0F, v.pf(12.0), Gx.mix(Style.sub, Style.text, hgv));
      }

      Gx.pop();
      if (interactive) {
         Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) {
               m.toggle();
               Modules.scheduleSave();
            } else {
               this.openSettings(m);
            }
         }).right(() -> this.openSettings(m));
         if (hasSettings) {
            Kit.hit(gearX, gearY, gearS, gearS).click(() -> this.openSettings(m));
         }
      }
   }

   private void glassRow(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
      boolean hv = interactive && v.hover(x, y, w, h);
      float hov = Ease.to("grow:" + m.id, hv ? 1.0F : 0.0F, 12.0F);
      float on = Ease.to("gron:" + m.id, m.enabled ? 1.0F : 0.0F, 10.0F);
      int r = v.p(14.0);
      Gx.push();
      Gx.translate(0.0F, -v.pf(1.0) * hov);
      float glassAlpha = (float)UiSettings.glass;
      if (hov > 0.02F) {
         Gx.shadow(x, y, w, h, r, v.p(10.0), 0.08F * hov);
      }

      Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.surface, glassAlpha * (0.25F + 0.15F * hov)));
      Gx.fill(x + r, y + Math.max(1, v.p(0.4)), w - r * 2, Math.max(1, v.p(0.4)), Gx.withAlpha(-1, 0.06F + 0.04F * hov));
      if (on > 0.01F) {
         Gx.pushAlpha(on);
         Gx.rect(x, y, w, h, r, Gx.withAlpha(Style.accent, 0.04F));
         Gx.fill(x + v.p(1.0), y + v.p(8.0), Math.max(2, v.p(2.0)), h - v.p(16.0), Gx.withAlpha(Style.accent, 0.8F));
         Gx.glow(x + v.p(2.0), y + h / 2, v.p(18.0), Gx.withAlpha(Style.accent, 0.12F));
         Gx.popAlpha();
      }

      Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.4)), Gx.withAlpha(Gx.mix(-1, Style.accent, on * 0.5F), 0.1F + 0.08F * hov + 0.06F * on));
      int is = v.p(30.0);
      int ix = x + v.p(12.0);
      int iy = y + (h - is) / 2;
      Gx.rect(ix, iy, is, is, v.p(10.0), Gx.withAlpha(Gx.mix(Style.surface2, Style.accent, on * 0.7F), 0.4F + 0.2F * on));
      Gx.outline(ix, iy, is, is, v.p(10.0), Math.max(1, v.p(0.4)), Gx.withAlpha(-1, 0.06F));
      Gx.icon(m.icon, ix + is / 2.0F, iy + is / 2.0F, v.pf(14.0F + hov * 0.6F), Gx.mix(Gx.withAlpha(Style.text, 0.7F), -1, Math.max(on, hov * 0.4F)));
      int tx = ix + is + v.p(12.0);
      int right = x + w - v.p(12.0);
      int tw = v.p(32.0);
      int th = v.p(18.0);
      int togX = right - tw;
      int togY = y + (h - th) / 2;
      boolean hasSettings = !m.settings.isEmpty();
      int gearS = v.p(22.0);
      int gearX = togX - v.p(6.0) - gearS;
      int gearY = y + (h - gearS) / 2;
      int textMax = (hasSettings ? gearX : togX) - v.p(6.0) - tx;
      float nameSize = v.pf(9.0);
      String name = Fonts.ellipsize(m.displayName(), 3, nameSize, textMax - (m.fresh ? v.p(20.0) : 0));
      float nw = Gx.textMid(name, tx, y + h / 2.0F, nameSize, 3, Gx.withAlpha(Style.text, 0.92F));
      if (m.hidden) {
         Gx.icon("eye_off", Math.round(tx + nw + v.p(5.0)), y + h / 2, v.pf(8.0), Style.muted);
         nw += v.p(13.0);
      } else if (m.fresh) {
         Gx.circle(Math.round(tx + nw + v.p(6.0)), y + h / 2, v.p(2.5), -15043);
         nw += v.p(12.0);
      }

      float sx2 = tx + nw + v.p(8.0);
      String desc = Fonts.ellipsize(m.displayDesc(), 1, v.pf(7.0), Math.round(textMax - nw - v.p(10.0)));
      Gx.textMid(desc, sx2, y + h / 2.0F, v.pf(7.0), 1, Gx.withAlpha(Style.sub, 0.7F));
      if (m.isToggleable()) {
         Kit.toggle(m.id, togX, togY, tw, th, m.enabled, v.P);
      } else {
         Gx.rect(togX - v.p(6.0), togY - v.p(1.0), tw + v.p(6.0), th + v.p(2.0), v.p(9.0), Gx.withAlpha(Style.accent, 0.7F));
         Gx.icon("play", togX - v.p(6.0) + (tw + v.p(6.0)) / 2.0F, togY + th / 2.0F, v.pf(9.0), Style.accentText);
      }

      if (hasSettings) {
         boolean hg = interactive && v.hover(gearX, gearY, gearS, gearS);
         float hgv = Ease.to("ggr:" + m.id, hg ? 1.0F : 0.0F, 16.0F);
         Gx.rect(gearX, gearY, gearS, gearS, v.p(8.0), Gx.withAlpha(Style.surface2, 0.2F + 0.4F * hgv));
         Gx.icon("dots", gearX + gearS / 2.0F, gearY + gearS / 2.0F, v.pf(11.0), Gx.withAlpha(Gx.mix(Style.sub, Style.text, hgv), 0.8F));
      }

      Gx.pop();
      if (interactive) {
         Kit.hit(x, y, w, h).click(() -> {
            if (m.isToggleable()) {
               m.toggle();
               Modules.scheduleSave();
            } else {
               this.openSettings(m);
            }
         }).right(() -> this.openSettings(m));
         if (hasSettings) {
            Kit.hit(gearX, gearY, gearS, gearS).click(() -> this.openSettings(m));
         }
      }
   }

   private void openSettings(Module m) {
      this.open = m;
      this.binding = null;
      this.editing = null;
      this.sScroll = this.sScrollTarget = 0.0F;
      this.searchFocus = false;
      Sound.pop();
   }

   private void drawSettings(HubView v, Module m, int x, int y, int w, int h, boolean interactive) {
      int pad = v.p(22.0);
      int top = y + v.p(18.0);
      int bs = v.p(30.0);
      boolean hb = interactive && v.hover(x + pad, top, bs, bs);
      float hbv = Ease.to("set:back", hb ? 1.0F : 0.0F, 16.0F);
      Gx.rect(x + pad, top, bs, bs, v.p(9.0), Gx.mix(Style.surface, Style.surfaceHover, hbv));
      Gx.icon("back", x + pad + bs / 2.0F - v.pf(1.0), top + bs / 2.0F, v.pf(14.0), Style.text);
      if (interactive) {
         Kit.hit(x + pad, top, bs, bs).click(() -> {
            this.open = null;
            this.binding = null;
            this.editing = null;
         });
      }

      int is = v.p(42.0);
      int ix = x + pad + bs + v.p(12.0);
      Gx.rect(ix, top - v.p(6.0), is, is, v.p(12.0), Gx.withAlpha(Style.accent, 0.18F));
      Gx.icon(m.icon, ix + is / 2.0F, top - v.p(6.0) + is / 2.0F, v.pf(21.0), Style.accent);
      int tx = ix + is + v.p(12.0);
      Gx.text(m.displayName(), tx, top - v.pf(4.0), v.pf(14.5), 3, Style.text);
      Gx.text(Fonts.ellipsize(m.description, 1, v.pf(8.0), w - (tx - x) - v.p(190.0)), tx, top + v.pf(18.0), v.pf(8.0), 1, Style.sub);
      int right = x + w - pad;
      if (m.isToggleable()) {
         int tw = v.p(40.0);
         int th = v.p(22.0);
         Kit.toggle(m.id + ":big", right - tw, top + v.p(4.0), tw, th, m.enabled, v.P);
         if (interactive) {
            Kit.hit(right - tw, top + v.p(4.0), tw, th).click(() -> {
               m.toggle();
               Modules.scheduleSave();
            });
         }

         String kl = this.binding == m ? Lang.get("ui.presskey", "Taste drücken…") : Lang.get("ui.key", "Taste") + ": " + Keys.name(m.key);
         float kfs = v.pf(7.6F);
         int kw = Math.round(Gx.width(kl, kfs, 2)) + v.p(20.0);
         int kh = v.p(22.0);
         int kx = right - tw - v.p(10.0) - kw;
         int ky = top + v.p(4.0);
         boolean hk = interactive && v.hover(kx, ky, kw, kh);
         float hkv = Ease.to("set:key", !hk && this.binding != m ? 0.0F : 1.0F, 14.0F);
         Gx.rect(kx, ky, kw, kh, kh / 2, Gx.mix(Style.surface, Style.surfaceHover, hkv));
         Gx.outline(kx, ky, kw, kh, kh / 2, Math.max(1, v.p(0.6)), this.binding == m ? Style.accent : Style.stroke);
         Gx.textCenter(kl, kx + kw / 2.0F, ky + kh / 2.0F, kfs, 2, this.binding == m ? Style.accent : Style.sub);
         if (interactive) {
            Kit.hit(kx, ky, kw, kh).click(() -> this.binding = this.binding == m ? null : m).right(() -> {
               m.key = -1;
               this.binding = null;
               Modules.scheduleSave();
            });
         }

         String hl = this.renaming ? Lang.get("ui.done", "Fertig") : Lang.get("ui.rename", "Umbenennen");
         float hfs = v.pf(7.2F);
         int hw = Math.round(Gx.width(hl, hfs, 2)) + v.p(16.0);
         int hh = v.p(20.0);
         int hxb = kx - v.p(10.0) - hw;
         boolean hhov = interactive && v.hover(hxb, ky + v.p(1.0), hw, hh);
         float hhv = Ease.to("set:rename", hhov ? 1.0F : 0.0F, 14.0F);
         Gx.rect(hxb, ky + v.p(1.0), hw, hh, hh / 2, Gx.mix(Style.surface, Style.surfaceHover, hhv));
         Gx.outline(hxb, ky + v.p(1.0), hw, hh, hh / 2, Math.max(1, v.p(0.5)), this.renaming ? Style.accent : Style.stroke);
         Gx.textCenter(hl, hxb + hw / 2.0F, ky + v.p(1.0) + hh / 2.0F, hfs, 2, this.renaming ? Style.accent : Style.sub);
         if (interactive) {
            Kit.hit(hxb, ky + v.p(1.0), hw, hh).click(() -> {
               if (this.renaming) {
                  String t = this.renameText.trim();
                  m.customName = !t.isEmpty() && !t.equals(m.name) ? t : null;
                  Modules.scheduleSave();
                  this.renaming = false;
               } else {
                  this.renaming = true;
                  this.renameText = m.displayName();
               }
            });
         }

         String hideLbl = m.hidden ? Lang.get("ui.show", "Einblenden") : Lang.get("ui.hide", "Ausblenden");
         int hidW = Math.round(Gx.width(hideLbl, hfs, 2)) + v.p(16.0);
         int hidX = hxb - v.p(8.0) - hidW;
         boolean hidHov = interactive && v.hover(hidX, ky + v.p(1.0), hidW, hh);
         float hidF = Ease.to("set:hide", hidHov ? 1.0F : 0.0F, 14.0F);
         Gx.rect(hidX, ky + v.p(1.0), hidW, hh, hh / 2, Gx.mix(Style.surface, Style.surfaceHover, hidF));
         Gx.outline(hidX, ky + v.p(1.0), hidW, hh, hh / 2, Math.max(1, v.p(0.5)), m.hidden ? Style.accent : Style.stroke);
         Gx.textCenter(hideLbl, hidX + hidW / 2.0F, ky + v.p(1.0) + hh / 2.0F, hfs, 2, m.hidden ? Style.accent : Style.sub);
         if (interactive) {
            Kit.hit(hidX, ky + v.p(1.0), hidW, hh).click(() -> {
               m.hidden = !m.hidden;
               Modules.scheduleSave();
            });
         }

         if (this.renaming) {
            int ry = top + v.p(34.0);
            int rw = right - hxb - hw - v.p(20.0) - tx + tx;
            int rh = v.p(22.0);
            Gx.rect(tx, ry, rw, rh, v.p(6.0), Style.surface2);
            Gx.outline(tx, ry, rw, rh, v.p(6.0), Math.max(1, v.p(0.6)), Style.accent);
            float curBlink = System.currentTimeMillis() % 1000L < 500L ? 1.0F : 0.0F;
            float tw2 = Gx.text(this.renameText, tx + v.p(6.0), ry + rh / 2.0F, v.pf(8.5), 2, Style.text);
            if (curBlink > 0.0F) {
               Gx.fill(tx + v.p(6.0) + Math.round(tw2), ry + v.p(4.0), Math.max(1, v.p(1.0)), rh - v.p(8.0), Style.accent);
            }
         }
      }

      int ly = top + v.p(48.0);
      int lh = y + h - v.p(48.0) - ly;
      Gx.fill(x + pad, ly - v.p(8.0), w - pad * 2, Math.max(1, v.p(0.6)), Style.stroke);
      int rowGap = v.p(8.0);
      int total = 0;

      for (Setting s : m.settings) {
         total += this.rowH(v, s) + rowGap;
      }

      this.sMaxScroll = Math.max(0, total - lh + v.p(16.0));
      this.sScrollTarget = Math.max(0.0F, Math.min(this.sMaxScroll, this.sScrollTarget));
      this.sScroll = this.sScroll + (this.sScrollTarget - this.sScroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(x, ly, w, lh);
      Kit.clip(x, ly, w, lh);
      if (interactive) {
         Kit.hit(x, ly, w, lh).scroll(a -> this.sScrollTarget = this.sScrollTarget - a.floatValue() * v.p(40.0));
      }

      int ry = Math.round(ly - this.sScroll);
      if (m.settings.isEmpty()) {
         Gx.textCenter(Lang.get("ui.nosettings", "Dieses Modul hat keine Einstellungen."), x + w / 2.0F, ly + v.pf(40.0), v.pf(9.0), 2, Style.sub);
      }

      for (int i = 0; i < m.settings.size(); i++) {
         Setting s = m.settings.get(i);
         int rh = this.rowH(v, s);
         if (ry + rh >= ly && ry <= ly + lh) {
            this.row(v, m, s, x + pad, ry, w - pad * 2, rh, interactive, i);
         }

         ry += rh + rowGap;
      }

      Kit.unclip();
      Gx.unclip();
      int fy = y + h - v.p(40.0);
      Gx.fill(x + v.p(1.0), fy, w - v.p(2.0), Math.max(1, v.p(0.6)), Style.stroke);
      String rl = Lang.get("ui.reset", "Standard wiederherstellen");
      int rw = Math.round(Gx.width(rl, v.pf(7.8F), 2)) + v.p(34.0);
      int rh = v.p(24.0);
      int rx = x + w - pad - rw;
      int rby = fy + v.p(8.0);
      boolean hr = interactive && v.hover(rx, rby, rw, rh);
      float hrv = Ease.to("set:reset", hr ? 1.0F : 0.0F, 14.0F);
      Gx.rect(rx, rby, rw, rh, rh / 2, Gx.mix(Style.surface, Style.surfaceHover, hrv));
      Gx.icon("reset", rx + v.p(14.0), rby + rh / 2.0F, v.pf(10.0), Gx.mix(Style.sub, Style.danger, hrv));
      Gx.textMid(rl, rx + v.p(24.0), rby + rh / 2.0F, v.pf(7.8F), 2, Style.sub);
      if (interactive) {
         Kit.hit(rx, rby, rw, rh).click(() -> {
            m.resetSettings();
            Modules.scheduleSave();
         });
      }

      Gx.textMid(
         m.category.displayTitle() + "  •  " + m.settings.size() + " " + Lang.get("ui.settings", "Einstellungen"),
         x + pad,
         fy + v.pf(20.0),
         v.pf(7.6F),
         1,
         Style.muted
      );
   }

   private int rowH(HubView v, Setting s) {
      return s instanceof Setting.Color ? v.p(66.0) : v.p(44.0);
   }

   private void row(HubView v, Module m, Setting s, int x, int y, int w, int h, boolean interactive, int idx) {
      String key = m.id + ":" + s.name;
      boolean hv = interactive && v.hover(x, y, w, h);
      float hov = Ease.to("row:" + key, hv ? 1.0F : 0.0F, 14.0F);
      Gx.rect(x, y, w, h, v.p(10.0), Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6F));
      float fs = v.pf(9.0);
      int cy = s instanceof Setting.Color ? y + v.p(20.0) : y + h / 2;
      Gx.textMid(s.displayName(), x + v.p(16.0), cy, fs, 2, Style.text);
      int right = x + w - v.p(16.0);
      if (s instanceof Setting.Bool b) {
         int tw = v.p(32.0);
         int th = v.p(18.0);
         Kit.toggle(key, right - tw, cy - th / 2, tw, th, b.value, v.P);
         if (interactive) {
            Kit.hit(x, y, w, h).click(() -> {
               b.value = !b.value;
               Modules.scheduleSave();
            });
         }
      } else if (s instanceof Setting.Num n) {
         String val = n.display();
         float vfs = v.pf(8.2F);
         int valW = v.p(58.0);
         int bw = v.p(58.0);
         int bh = v.p(22.0);
         Gx.rect(right - bw, cy - bh / 2, bw, bh, v.p(7.0), Style.surface2);
         Gx.textCenter(val, right - bw / 2.0F, cy, vfs, 3, Style.text);
         int sw = Math.min(v.p(220.0), w / 2);
         int sx = right - valW - v.p(14.0) - sw;
         Kit.slider(key, sx, cy - v.p(9.0), sw, v.p(18.0), (float)n.frac(), v.P);
         if (interactive) {
            Kit.hit(sx - v.p(6.0), y, sw + v.p(12.0), h).drag((px, py) -> {
               n.setFrac((double)(px - sx) / sw);
               Modules.scheduleSave();
            });
         }
      } else if (s instanceof Setting.Mode md) {
         float mfs = v.pf(7.8F);
         int segPad = v.p(10.0);
         int totalW = 0;

         for (String o : md.options) {
            totalW += Math.round(Gx.width(o, mfs, 2)) + segPad * 2;
         }

         int segH = v.p(26.0);
         if (md.options.length <= 5 && totalW <= w * 0.62F) {
            int sx = right - totalW;
            Gx.rect(sx - v.p(3.0), cy - segH / 2 - v.p(3.0), totalW + v.p(6.0), segH + v.p(6.0), v.p(9.0), Style.surface2);
            int ox = sx;
            int[] xs = new int[md.options.length];
            int[] ws = new int[md.options.length];

            for (int i = 0; i < md.options.length; i++) {
               xs[i] = ox;
               ws[i] = Math.round(Gx.width(md.options[i], mfs, 2)) + segPad * 2;
               ox += ws[i];
            }

            float ax = Ease.to("segx:" + key, xs[md.index], 18.0F, xs[md.index]);
            float aw = Ease.to("segw:" + key, ws[md.index], 18.0F, ws[md.index]);
            Gx.rect(Math.round(ax), cy - segH / 2, Math.round(aw), segH, v.p(7.0), Style.accent);

            for (int i = 0; i < md.options.length; i++) {
               boolean sel = i == md.index;
               Gx.textCenter(md.options[i], xs[i] + ws[i] / 2.0F, cy, mfs, 2, sel ? Style.accentText : Style.sub);
               int oi = i;
               if (interactive) {
                  Kit.hit(xs[i], cy - segH / 2, ws[i], segH).click(() -> {
                     md.index = oi;
                     Modules.scheduleSave();
                  });
               }
            }
         } else {
            int bw = v.p(190.0);
            int bx = right - bw;
            Gx.rect(bx, cy - segH / 2, bw, segH, v.p(8.0), Style.surface2);
            boolean hl = interactive && v.hover(bx, cy - segH / 2, segH, segH);
            boolean hr = interactive && v.hover(bx + bw - segH, cy - segH / 2, segH, segH);
            Gx.icon("chevron-left", bx + segH / 2.0F, cy, v.pf(11.0), hl ? Style.accent : Style.sub);
            Gx.icon("chevron-right", bx + bw - segH / 2.0F, cy, v.pf(11.0), hr ? Style.accent : Style.sub);
            Gx.textCenter(Fonts.ellipsize(md.get(), 2, mfs, bw - segH * 2), bx + bw / 2.0F, cy, mfs, 2, Style.text);
            Gx.textRight(md.index + 1 + "/" + md.options.length, bx - v.p(10.0), cy, v.pf(7.0), 1, Style.muted);
            if (interactive) {
               Kit.hit(bx, cy - segH / 2, bw / 2, segH).click(() -> {
                  md.cycle(-1);
                  Modules.scheduleSave();
               });
               Kit.hit(bx + bw / 2, cy - segH / 2, bw - bw / 2, segH).click(() -> {
                  md.cycle(1);
                  Modules.scheduleSave();
               });
            }
         }
      } else if (s instanceof Setting.Color c) {
         int nx = Setting.Color.PALETTE.length;
         int d = v.p(20.0);
         int g = v.p(9.0);
         int sx = x + v.p(16.0);
         int sy = y + v.p(34.0);

         for (int ix = 0; ix < nx; ix++) {
            int px = sx + ix * (d + g);
            boolean sel = c.index == ix;
            boolean hs = interactive && v.hover(px, sy, d, d);
            float sc = Ease.to("sw:" + key + ix, sel ? 1.12F : (hs ? 1.08F : 1.0F), 16.0F);
            Gx.push();
            Gx.scaleAt(px + d / 2.0F, sy + d / 2.0F, sc);
            if (ix == nx - 1) {
               Gx.image(rainbow(d), px, sy, d, d, -1);
            } else {
               Gx.circle(px + d / 2, sy + d / 2, d / 2, Setting.Color.PALETTE[ix]);
            }

            if (sel) {
               Gx.ringAt(px + d / 2, sy + d / 2, d / 2 + v.p(3.0), Math.max(1, v.p(1.4)), Style.text);
            }

            Gx.pop();
            int ci = ix;
            if (interactive) {
               Kit.hit(px - g / 2, sy - v.p(4.0), d + g, d + v.p(8.0)).click(() -> {
                  c.index = ci;
                  Modules.scheduleSave();
               });
            }
         }

         Gx.textRight(Setting.Color.NAMES[c.index], right, y + v.p(20.0), v.pf(8.0), 2, Style.sub);
      } else if (s instanceof Setting.Text tx) {
         boolean focus = this.editing == tx;
         float tfs = v.pf(8.0);
         int bw = Math.min(v.p(260.0), w / 2);
         int bh = v.p(26.0);
         int bx = right - bw;
         float f = Ease.to("txt:" + key, focus ? 1.0F : 0.0F, 14.0F);
         Gx.rect(bx, cy - bh / 2, bw, bh, v.p(8.0), Gx.mix(Style.surface2, Style.surfaceHover, f));
         Gx.outline(bx, cy - bh / 2, bw, bh, v.p(8.0), Math.max(1, v.p(0.7)), Gx.mix(Style.stroke, Style.accent, f));
         String shown = tx.value.isEmpty() && !focus ? tx.hint : tx.value;
         String cut = shown;

         while (Gx.width(cut, tfs, 2) > bw - v.p(24.0) && cut.length() > 1) {
            cut = cut.substring(1);
         }

         float tw = Gx.textMid(cut, bx + v.p(10.0), cy, tfs, 2, tx.value.isEmpty() && !focus ? Style.muted : Style.text);
         if (focus && System.currentTimeMillis() / 500L % 2L == 0L) {
            Gx.fill(Math.round(bx + v.p(11.0) + tw), cy - v.p(6.0), Math.max(1, v.p(0.9)), v.p(12.0), Style.accent);
         }

         if (interactive) {
            Kit.hit(bx, cy - bh / 2, bw, bh).click(() -> this.editing = focus ? null : tx);
         }
      } else if (s instanceof Setting.Action ac) {
         float afs = v.pf(8.0);
         int bw = Math.round(Gx.width(ac.label, afs, 3)) + v.p(30.0);
         int bh = v.p(26.0);
         int bx = right - bw;
         boolean hbt = interactive && v.hover(bx, cy - bh / 2, bw, bh);
         Kit.button(key, ac.label, null, bx, cy - bh / 2, bw, bh, v.P, true, hbt);
         if (interactive) {
            Kit.hit(bx, cy - bh / 2, bw, bh).click(() -> {
               try {
                  ac.action.run();
               } catch (Throwable var2) {
                  LegoClient.LOG("Aktion: " + var2);
               }
            });
         }
      }
   }

   static Gx.Img rainbow(int d) {
      return Gx.painted("rainbow", d, d, g -> {
         for (int a = 0; a < 360; a += 3) {
            g.setColor(Color.getHSBColor(a / 360.0F, 0.75F, 1.0F));
            g.fill(new Double(0.0, 0.0, d, d, a, 4.0, 2));
         }
      });
   }

   @Override
   public boolean key(int key, int mods) {
      if (key < 0) {
         return false;
      } else if (this.editing != null) {
         if (key == 259 && !this.editing.value.isEmpty()) {
            this.editing.value = this.editing.value.substring(0, this.editing.value.length() - 1);
         } else if (key == 256 || key == 257 || key == 335) {
            this.editing = null;
         } else if (key == 86 && (mods & 2) != 0) {
            String clip = Env.clipboard.get();
            if (clip != null) {
               this.editing.value = (this.editing.value + clip.replace('\n', ' '))
                  .substring(0, Math.min(this.editing.max, this.editing.value.length() + clip.length()));
            }
         }

         Modules.scheduleSave();
         return true;
      } else if (this.binding != null) {
         if (key != 256 && key != 259 && key != 261) {
            this.binding.key = key;
         } else {
            this.binding.key = -1;
         }

         this.binding = null;
         Modules.scheduleSave();
         return true;
      } else if (this.renaming) {
         if (key == 259 && !this.renameText.isEmpty()) {
            this.renameText = this.renameText.substring(0, this.renameText.length() - 1);
            return true;
         } else if (key == 257 || key == 335) {
            Module m = this.open;
            if (m != null) {
               String t = this.renameText.trim();
               m.customName = !t.isEmpty() && !t.equals(m.name) ? t : null;
               Modules.scheduleSave();
            }

            this.renaming = false;
            return true;
         } else if (key == 256) {
            this.renaming = false;
            return true;
         } else {
            return true;
         }
      } else if (this.open != null) {
         if (key == 256) {
            this.open = null;
            this.editing = null;
            this.renaming = false;
            return true;
         } else {
            return false;
         }
      } else if (key == 259 && !this.search.isEmpty()) {
         this.search = this.search.substring(0, this.search.length() - 1);
         this.listChangedAt = System.currentTimeMillis();
         this.scrollTarget = 0.0F;
         return true;
      } else if (key != 256 || this.search.isEmpty() && !this.searchFocus) {
         if (key == 258) {
            this.setTab((tab + 1) % TAB_CATS.length);
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
   public boolean chr(String s) {
      if (this.editing != null && s != null) {
         if (this.editing.value.length() + s.length() <= this.editing.max) {
            this.editing.value = this.editing.value + s;
         }

         Modules.scheduleSave();
         return true;
      } else if (this.renaming && s != null && !s.isEmpty()) {
         if (this.renameText.length() + s.length() <= 24) {
            this.renameText = this.renameText + s;
         }

         return true;
      } else if (this.binding == null && this.open == null && s != null && !s.isEmpty()) {
         if (this.search.length() > 24) {
            return true;
         } else {
            this.search = this.search + s;
            this.searchFocus = true;
            this.scrollTarget = 0.0F;
            this.listChangedAt = System.currentTimeMillis();
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean bindMouse(int button) {
      if (this.binding != null && button >= 2) {
         this.binding.key = button;
         this.binding = null;
         Modules.scheduleSave();
         return true;
      } else {
         return false;
      }
   }
}
