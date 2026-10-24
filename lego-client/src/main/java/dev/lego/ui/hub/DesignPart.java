package dev.lego.ui.hub;

import dev.lego.core.Lang;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.BgFx;
import dev.lego.ui.BgImage;
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
   private final HubSearch search = new HubSearch();
   private HubView view;

   @Override
   public void draw(HubView v, int x, int y, int w, int h) {
      int pad = v.p(24.0);
      int top = v.header(x + pad, y + v.p(20.0), "Design & Client", Lang.get("ui.design.sub", "Thema, Farbe, HUD, UI-Größe und Animations-Tempo"));
      this.view = v;
      // Design has no list of its own: its search looks through every module (opens Mods).
      int ssw = v.p(170.0), ssh = v.p(24.0);
      this.search.draw(v, "design", x + w - pad - ssw, y + v.p(22.0), ssw, ssh, true);
      int ly = top + v.p(4.0);
      int lh = y + h - v.p(14.0) - ly;
      this.max = Math.max(0, this.contentH(v) - lh);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(x, ly, w, lh);
      Kit.clip(x, ly, w, lh);
      Kit.hit(x, ly, w, lh).scroll(a -> this.target = this.target - a.floatValue() * v.p(44.0));
      int cy = Math.round(ly - this.scroll);
      int cw = w - pad * 2;
      cy = this.label(v, Lang.get("ui.d.bg", "CLICK-GUI HINTERGRUND"), x + pad, cy);
      String[] names = BgFx.NAMES;
      int bc = 5;
      int bg = v.p(8.0);
      int bw = (cw - bg * (bc - 1)) / bc;
      int bh = v.p(30.0);

      for (int i = 0; i < names.length; i++) {
         int bx = x + pad + i % bc * (bw + bg);
         int by = cy + i / bc * (bh + bg);
         boolean sel = UiSettings.bgStyle == i;
         boolean hv = v.hover(bx, by, bw, bh);
         float hf = Ease.to("bgs:" + i, sel ? 1.0F : (hv ? 0.45F : 0.0F), 16.0F);
         Gx.rect(bx, by, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(-1, 0.05F), Style.accent, hf * (sel ? 1.0F : 0.35F)));
         Gx.outline(bx, by, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(-1, 0.1F));
         Gx.textCenter(names[i], bx + bw / 2.0F, by + bh / 2.0F, v.pf(8.0), 3, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
         int idx = i;
         Kit.hit(bx, by, bw, bh).click(() -> {
            UiSettings.bgStyle = idx;
            Modules.scheduleSave();
         });
      }

      cy += (names.length + bc - 1) / bc * (bh + bg) + v.p(4.0);
      cy = this.sliderRow(
            v,
            Lang.get("ui.d.glass", "Glas-Deckkraft"),
            "glass",
            (UiSettings.glass - 0.15) / 0.85,
            Math.round(UiSettings.glass * 100.0) + "%",
            f -> UiSettings.glass = Math.round((0.15 + f * 0.85) * 100.0) / 100.0,
            x + pad,
            cy,
            cw,
            v.p(44.0)
         )
         + v.p(8.0);
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.line", "Leuchtende Linie oben am Fenster"),
            "guiline",
            UiSettings.guiLine,
            () -> UiSettings.guiLine = !UiSettings.guiLine,
            x + pad,
            cy,
            cw,
            v.p(44.0)
         )
         + v.p(8.0);
      cy = this.sliderRow(
            v,
            Lang.get("ui.d.bgint", "Animations-Stärke"),
            "bgint",
            (UiSettings.bgIntensity - 0.1) / 0.9,
            Math.round(UiSettings.bgIntensity * 100.0) + "%",
            f -> UiSettings.bgIntensity = Math.round((0.1 + f * 0.9) * 100.0) / 100.0,
            x + pad,
            cy,
            cw,
            v.p(44.0)
         )
         + v.p(8.0);
      names = UiSettings.GUI_STYLES;
      bc = names.length;
      bg = v.p(8.0);
      bw = (cw - bg * (bc - 1)) / bc;
      bh = v.p(30.0);

      for (int i = 0; i < names.length; i++) {
         int bx = x + pad + i * (bw + bg);
         boolean sel = UiSettings.guiStyle == i;
         boolean hv = v.hover(bx, cy, bw, bh);
         float hf = Ease.to("guis:" + i, sel ? 1.0F : (hv ? 0.45F : 0.0F), 16.0F);
         Gx.rect(bx, cy, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(-1, 0.05F), Style.accent, hf * (sel ? 1.0F : 0.35F)));
         Gx.outline(bx, cy, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(-1, 0.1F));
         Gx.textCenter(names[i], bx + bw / 2.0F, cy + bh / 2.0F, v.pf(8.0), 3, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
         int idx = i;
         Kit.hit(bx, cy, bw, bh).click(() -> {
            UiSettings.guiStyle = idx;
            Modules.scheduleSave();
         });
      }

      cy += bh + v.p(12.0);
      names = Lang.LANGUAGES;
      bc = names.length;
      bg = v.p(8.0);
      bw = (cw - bg * (bc - 1)) / bc;
      bh = v.p(30.0);
      Gx.icon("globe", x + pad, cy + bh / 2.0F, v.pf(10.0), Style.sub);
      Gx.textMid("Sprache / Language", x + pad + v.p(16.0), cy + v.pf(bh / 2.0F), v.pf(8.0), 2, Style.text);
      cy += bh + v.p(4.0);

      for (int i = 0; i < names.length; i++) {
         int bx = x + pad + i * (bw + bg);
         boolean sel = UiSettings.language == i;
         boolean hv = v.hover(bx, cy, bw, bh);
         float hf = Ease.to("lang:" + i, sel ? 1.0F : (hv ? 0.45F : 0.0F), 16.0F);
         Gx.rect(bx, cy, bw, bh, bh / 2, Gx.mix(Gx.withAlpha(-1, 0.05F), Style.accent, hf * (sel ? 1.0F : 0.35F)));
         Gx.outline(bx, cy, bw, bh, bh / 2, Math.max(1, v.p(0.6)), sel ? Style.accent : Gx.withAlpha(-1, 0.1F));
         Gx.textCenter(names[i], bx + bw / 2.0F, cy + bh / 2.0F, v.pf(8.0), 3, sel ? Style.accentText : Gx.mix(Style.sub, Style.text, hf));
         int idx = i;
         Kit.hit(bx, cy, bw, bh).click(() -> {
            UiSettings.language = idx;
            Lang.setLanguage(idx);
            BgFx.updateNames();
            Style.updateThemeNames();
            Setting.Color.updateNames();
            Modules.scheduleSave();
         });
      }

      cy += bh + v.p(12.0);
      String lbl = UiSettings.bgImage.isEmpty() ? Lang.get("ui.d.nobg", "Kein Hintergrundbild") : UiSettings.bgImage;
      bc = v.p(30.0);
      Gx.rect(x + pad, cy, cw, bc, v.p(8.0), Gx.withAlpha(-1, 0.05F));
      Gx.outline(x + pad, cy, cw, bc, v.p(8.0), Math.max(1, v.p(0.6)), Style.stroke);
      Gx.icon("image", x + pad + v.p(12.0), cy + bc / 2.0F, v.pf(12.0), Style.sub);
      Gx.textMid(lbl, x + pad + v.p(28.0), cy + v.pf(bc / 2.0F), v.pf(7.5), 2, Style.text);
      boolean hv = v.hover(x + pad, cy, cw, bc);
      if (hv) {
         Gx.rect(x + pad, cy, cw, bc, v.p(8.0), Gx.withAlpha(Style.accent, 0.08F));
      }

      Kit.hit(x + pad, cy, cw, bc).click(() -> BgImage.pick());
      if (!UiSettings.bgImage.isEmpty()) {
         bw = v.p(24.0);
         boolean ch = v.hover(x + pad + cw - bw - v.p(4.0), cy + v.p(3.0), bw, bc - v.p(6.0));
         Gx.icon("x", x + pad + cw - bw / 2.0F - v.p(4.0), cy + bc / 2.0F, v.pf(10.0), ch ? Style.danger : Style.sub);
         Kit.hit(x + pad + cw - bw - v.p(4.0), cy + v.p(3.0), bw, bc - v.p(6.0)).click(() -> {
            UiSettings.bgImage = "";
            BgImage.clear();
            Modules.scheduleSave();
         });
      }

      cy += bc + v.p(8.0);
      cy += v.p(12.0);
      cy = this.label(v, Lang.get("ui.d.theme", "THEMA"), x + pad, cy);
      int cols = 4;
      bc = v.p(10.0);
      int tw = (cw - bc * (cols - 1)) / cols;
      bw = v.p(66.0);

      for (int i = 0; i < Style.THEMES.length; i++) {
         int tx = x + pad + i % cols * (tw + bc);
         int ty = cy + i / cols * (bw + v.p(24.0));
         this.themeCard(v, i, tx, ty, tw, bw);
      }

      cy += (Style.THEMES.length + cols - 1) / cols * (bw + v.p(24.0)) + v.p(8.0);
      cy = this.label(v, Lang.get("ui.d.accent", "AKZENTFARBE"), x + pad, cy);
      bh = v.p(26.0);
      int g = v.p(12.0);

      for (int i = 0; i < Setting.Color.PALETTE.length; i++) {
         int px = x + pad + i * (bh + g);
         boolean sel = Modules.accent == i;
         boolean hs = v.hover(px, cy, bh, bh);
         float sc = Ease.to("acc:" + i, sel ? 1.12F : (hs ? 1.08F : 1.0F), 16.0F);
         Gx.push();
         Gx.scaleAt(px + bh / 2.0F, cy + bh / 2.0F, sc);
         if (i == Setting.Color.PALETTE.length - 1) {
            Gx.image(ModsPart.rainbow(bh), px, cy, bh, bh, -1);
         } else {
            Gx.circle(px + bh / 2, cy + bh / 2, bh / 2, Setting.Color.PALETTE[i]);
         }

         if (sel) {
            Gx.ringAt(px + bh / 2, cy + bh / 2, bh / 2 + v.p(4.0), Math.max(1, v.p(1.6)), Style.text);
            Gx.icon("check", px + bh / 2.0F, cy + bh / 2.0F, v.pf(12.0), Style.brightness(Setting.Color.PALETTE[i]) > 0.6 ? -15658735 : -1);
         }

         Gx.pop();
         int idx = i;
         Kit.hit(px - g / 2, cy - v.p(4.0), bh + g, bh + v.p(8.0)).click(() -> {
            Modules.accent = idx;
            Modules.scheduleSave();
         });
      }

      cy += bh + v.p(22.0);
      cy = this.label(v, "HUD", x + pad, cy);
      int rh = v.p(44.0);
      int rg = v.p(8.0);
      this.rowBg(v, x + pad, cy, cw, rh, "hudstyle");
      Gx.textMid(Lang.get("ui.d.hudstyle", "HUD-Stil"), x + pad + v.p(16.0), cy + rh / 2.0F, v.pf(9.0), 2, Style.text);
      this.segmented(v, "hudstyle", x + pad + cw - v.p(16.0), cy + rh / 2, UiSettings.HUD_STYLES, UiSettings.hudStyle, i -> UiSettings.hudStyle = i);
      cy += rh + rg;
      this.rowBg(v, x + pad, cy, cw, rh, "hudop");
      Gx.textMid(Lang.get("ui.d.hudop", "Hintergrund-Deckkraft"), x + pad + v.p(16.0), cy + rh / 2.0F, v.pf(9.0), 2, Style.text);
      int bwx = v.p(58.0);
      int bhx = v.p(22.0);
      int right = x + pad + cw - v.p(16.0);
      Gx.rect(right - bwx, cy + rh / 2 - bhx / 2, bwx, bhx, v.p(7.0), Style.surface2);
      Gx.textCenter(Math.round(UiSettings.hudOpacity * 100.0) + "%", right - bwx / 2.0F, cy + rh / 2.0F, v.pf(8.2F), 3, Style.text);
      int sw = v.p(220.0);
      int sx = right - bwx - v.p(14.0) - sw;
      Kit.slider("hudop", sx, cy + rh / 2 - v.p(9.0), sw, v.p(18.0), (float)UiSettings.hudOpacity, v.P);
      Kit.hit(sx - v.p(6.0), cy, sw + v.p(12.0), rh).drag((pxx, py) -> {
         UiSettings.hudOpacity = Math.round(Math.max(0.0, Math.min(1.0, (double)(pxx - sx) / sw)) * 100.0) / 100.0;
         Modules.scheduleSave();
      });
      cy += rh + rg;
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.hudicons", "Icons in der HUD"),
            "hudicons",
            UiSettings.hudIcons,
            () -> UiSettings.hudIcons = !UiSettings.hudIcons,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.toggleRow(
            v, Lang.get("ui.d.shadow", "Schatten"), "hudshadow", UiSettings.hudShadow, () -> UiSettings.hudShadow = !UiSettings.hudShadow, x + pad, cy, cw, rh
         )
         + rg;
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.accentline", "Akzent-Linie links"),
            "hudline",
            UiSettings.hudAccentLine,
            () -> UiSettings.hudAccentLine = !UiSettings.hudAccentLine,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy += v.p(12.0);
      cy = this.label(v, Lang.get("ui.d.menu", "MENÜ"), x + pad, cy);
      cy = this.toggleRow(v, Lang.get("ui.d.sounds", "Klick-Sounds"), "sounds", UiSettings.sounds, () -> {
         UiSettings.sounds = !UiSettings.sounds;
         Sound.uiSounds = UiSettings.sounds;
      }, x + pad, cy, cw, rh) + rg;
      cy = this.toggleRow(
            v, Lang.get("ui.d.blur", "Hintergrund verschwimmen (Blur)"), "blur", UiSettings.blur, () -> UiSettings.blur = !UiSettings.blur, x + pad, cy, cw, rh
         )
         + rg;
      cy += v.p(12.0);
      cy = this.label(v, "CLIENT", x + pad, cy);
      cy = this.sliderRow(
            v,
            Lang.get("ui.d.uiscale", "UI-Größe"),
            "uiscale",
            (UiSettings.uiScale - 0.6) / 0.7,
            Math.round(UiSettings.uiScale * 100.0) + "%",
            f -> UiSettings.uiScale = Math.round((0.6 + f * 0.7) * 20.0) / 20.0,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.sliderRow(
            v,
            "Rundung",
            "roundness",
            UiSettings.roundness / 2.5,
            Math.round(UiSettings.roundness * 100.0) + "%",
            f -> Gx.round = (float)(UiSettings.roundness = Math.round(f * 2.5 * 10.0) / 10.0),
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.sliderRow(
            v,
            Lang.get("ui.d.animspeed", "Animations-Tempo"),
            "animspeed",
            (UiSettings.animSpeed - 0.5) / 2.5,
            String.format(Locale.ROOT, "%.1fx", UiSettings.animSpeed),
            f -> UiSettings.animSpeed = Math.round((0.5 + f * 2.5) * 10.0) / 10.0,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.toggleRow(
            v, Lang.get("ui.d.anims", "Animationen"), "anims", UiSettings.animations, () -> UiSettings.animations = !UiSettings.animations, x + pad, cy, cw, rh
         )
         + rg;
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.intro", "Start-Animation (Lego-Steine)"),
            "intro",
            UiSettings.intro,
            () -> UiSettings.intro = !UiSettings.intro,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.xmas", "Weihnachts-Deko (Schnee, Lichter, Mützen)"),
            "xmas",
            UiSettings.xmas,
            () -> UiSettings.xmas = !UiSettings.xmas,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      cy = this.toggleRow(
            v,
            Lang.get("ui.d.halloween", "Halloween-Deko (Fledermäuse, Kürbisse, Nebel)"),
            "halloween",
            UiSettings.halloween,
            () -> UiSettings.halloween = !UiSettings.halloween,
            x + pad,
            cy,
            cw,
            rh
         )
         + rg;
      Kit.unclip();
      Gx.unclip();
   }

   private int sliderRow(HubView v, String label, String key, double frac, String value, DoubleConsumer set, int x, int y, int w, int h) {
      this.rowBg(v, x, y, w, h, key);
      Gx.textMid(label, x + v.p(16.0), y + h / 2.0F, v.pf(9.0), 2, Style.text);
      int bw = v.p(58.0);
      int bh = v.p(22.0);
      int right = x + w - v.p(16.0);
      Gx.rect(right - bw, y + h / 2 - bh / 2, bw, bh, v.p(7.0), Style.surface2);
      Gx.textCenter(value, right - bw / 2.0F, y + h / 2.0F, v.pf(8.2F), 3, Style.text);
      int sw = v.p(220.0);
      int sx = right - bw - v.p(14.0) - sw;
      Kit.slider(key, sx, y + h / 2 - v.p(9.0), sw, v.p(18.0), (float)Math.max(0.0, Math.min(1.0, frac)), v.P);
      Kit.hit(sx - v.p(6.0), y, sw + v.p(12.0), h).drag((px, py) -> {
         set.accept(Math.max(0.0, Math.min(1.0, (double)(px - sx) / sw)));
         Modules.scheduleSave();
      });
      return y + h;
   }

   private int contentH(HubView v) {
      int rh = v.p(44.0);
      int rg = v.p(8.0);
      int bgRows = (BgFx.NAMES.length + 4) / 5;
      int themeRows = (Style.THEMES.length + 3) / 4;
      int h = 0;
      h += v.p(22.0) + bgRows * (v.p(30.0) + v.p(8.0)) + v.p(4.0);
      h += rh + v.p(8.0);
      h += rh + v.p(8.0);
      h += rh + v.p(8.0);
      h += v.p(30.0) + v.p(12.0);
      h += v.p(30.0) + v.p(8.0);
      h += v.p(12.0);
      h += v.p(22.0) + themeRows * (v.p(66.0) + v.p(24.0)) + v.p(8.0);
      h += v.p(22.0) + v.p(26.0) + v.p(22.0);
      h += v.p(22.0) + 5 * (rh + rg);
      h += v.p(12.0);
      h += v.p(22.0) + 2 * (rh + rg);
      h += v.p(12.0);
      h += v.p(22.0) + 6 * (rh + rg);
      return h + v.p(60.0);
   }

   private int label(HubView v, String s, int x, int y) {
      Gx.textSpaced(s, x + Gx.spacedWidth(s, v.pf(7.2F), 3, v.pf(1.4F)) / 2.0F, y + v.pf(7.0), v.pf(7.2F), 3, Style.muted, v.pf(1.4F));
      return y + v.p(22.0);
   }

   private void rowBg(HubView v, int x, int y, int w, int h, String key) {
      boolean modern = UiSettings.guiStyle == 1;
      float hov = Ease.to("drow:" + key, v.hover(x, y, w, h) ? 1.0F : 0.0F, 14.0F);
      if (modern) {
         Gx.outline(x, y, w, h, v.p(12.0), Math.max(1, v.p(0.6)), Gx.mix(Style.stroke, Style.strokeHi, hov));
      } else {
         Gx.rect(x, y, w, h, v.p(10.0), Gx.mix(Style.surface, Style.surfaceHover, hov * 0.6F));
      }
   }

   private int toggleRow(HubView v, String label, String key, boolean on, Runnable flip, int x, int y, int w, int h) {
      this.rowBg(v, x, y, w, h, key);
      Gx.textMid(label, x + v.p(16.0), y + h / 2.0F, v.pf(9.0), 2, Style.text);
      int tw = v.p(32.0);
      int th = v.p(18.0);
      Kit.toggle("d:" + key, x + w - v.p(16.0) - tw, y + (h - th) / 2, tw, th, on, v.P);
      Kit.hit(x, y, w, h).click(() -> {
         flip.run();
         Modules.scheduleSave();
      });
      return y + h;
   }

   private void segmented(HubView v, String key, int right, int cy, String[] opts, int sel, IntConsumer pick) {
      float fs = v.pf(7.8F);
      int pad = v.p(12.0);
      int h = v.p(26.0);
      int total = 0;
      int[] ws = new int[opts.length];

      for (int i = 0; i < opts.length; i++) {
         ws[i] = Math.round(Gx.width(opts[i], fs, 2)) + pad * 2;
         total += ws[i];
      }

      int x = right - total;
      Gx.rect(x - v.p(3.0), cy - h / 2 - v.p(3.0), total + v.p(6.0), h + v.p(6.0), v.p(9.0), Style.surface2);
      int[] xs = new int[opts.length];
      int ox = x;

      for (int i = 0; i < opts.length; i++) {
         xs[i] = ox;
         ox += ws[i];
      }

      float ax = Ease.to("dseg:" + key, xs[sel], 18.0F, xs[sel]);
      float aw = Ease.to("dsegw:" + key, ws[sel], 18.0F, ws[sel]);
      Gx.rect(Math.round(ax), cy - h / 2, Math.round(aw), h, v.p(7.0), Style.accent);

      for (int i = 0; i < opts.length; i++) {
         Gx.textCenter(opts[i], xs[i] + ws[i] / 2.0F, cy, fs, 2, i == sel ? Style.accentText : Style.sub);
         int idx = i;
         Kit.hit(xs[i], cy - h / 2, ws[i], h).click(() -> {
            pick.accept(idx);
            Modules.scheduleSave();
         });
      }
   }

   private void themeCard(HubView v, int i, int x, int y, int w, int h) {
      boolean modern = UiSettings.guiStyle == 1;
      String id = Style.THEMES[i];
      boolean sel = id.equals(Modules.theme);
      boolean hv = v.hover(x, y, w, h);
      float hov = Ease.to("theme:" + i, hv ? 1.0F : 0.0F, 14.0F);
      int[] pal = Style.palette(id);
      if (!modern) {
         Gx.push();
         Gx.translate(0.0F, -v.pf(2.0) * hov);
      }

      if (!modern) {
         Gx.shadow(x, y, w, h, v.p(11.0), v.p(6.0), 0.3F + 0.2F * hov);
      }

      Gx.rect(x, y, w, h, v.p(11.0), pal[0] | 0xFF000000);
      if ("glass".equals(id)) {
         Gx.rectV(x, y, w, h, v.p(11.0), 872415231, 234881023);
      }

      int ix = x + v.p(10.0);
      int iy = y + v.p(10.0);
      Gx.rect(ix, iy, v.p(16.0), h - v.p(20.0), v.p(5.0), pal[1] | 0xFF000000);
      int cx = ix + v.p(22.0);
      int cw = x + w - v.p(10.0) - cx;
      Gx.rect(cx, iy, cw, v.p(18.0), v.p(5.0), pal[1] | 0xFF000000);
      Gx.rect(cx + v.p(5.0), iy + v.p(6.0), cw / 2, v.p(3.0), v.p(2.0), pal[3]);
      Gx.rect(cx, iy + v.p(24.0), cw, h - v.p(44.0), v.p(5.0), pal[1] | 0xFF000000);
      Gx.rect(cx + cw - v.p(20.0), iy + v.p(30.0), v.p(14.0), v.p(8.0), v.p(4.0), Style.accent);
      Gx.rect(cx + v.p(5.0), iy + v.p(31.0), cw / 3, v.p(3.0), v.p(2.0), pal[4]);
      Gx.outline(x, y, w, h, v.p(modern ? 14.0 : 11.0), Math.max(1, v.p(sel ? 1.6 : 0.7)), sel ? Style.accent : Gx.mix(Style.stroke, Style.strokeHi, hov));
      if (!modern) {
         Gx.pop();
      }

      Gx.textCenter(Style.THEME_NAMES[i], x + w / 2.0F, y + h + v.pf(11.0), v.pf(8.0), sel ? 3 : 2, sel ? Style.text : Style.sub);
      Kit.hit(x, y, w, h + v.p(20.0)).click(() -> {
         Modules.theme = id;
         Modules.scheduleSave();
      });
   }

   @Override
   public boolean key(int key, int mods) {
      return this.search.key(key, mods);
   }

   @Override
   public boolean chr(String s) {
      if (!this.search.chr(s)) return false;
      if (this.view != null) this.view.searchModules(this.search.text);
      return true;
   }
}
