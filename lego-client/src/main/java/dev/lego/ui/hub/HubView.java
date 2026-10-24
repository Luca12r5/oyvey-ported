package dev.lego.ui.hub;

import dev.lego.ui.Art;
import dev.lego.ui.BgFx;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Halloween;
import dev.lego.ui.Kit;
import dev.lego.ui.MenuView;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;
import dev.lego.ui.View;
import dev.lego.ui.Xmas;
import java.util.function.IntConsumer;

public final class HubView extends View {
   private HubView.Section section;
   private HubView.Part part;
   private long switchedAt = System.currentTimeMillis();

   public HubView() {
      this(HubView.Section.MODS);
   }

   public HubView(HubView.Section s) {
      this.select(s);
   }

   public HubView.Section section() {
      return this.section;
   }

   public void select(HubView.Section s) {
      if (s == HubView.Section.HUD) {
         if (this.host != null) {
            this.host.closeThen(Env.openHudEditor);
            return;
         }

         s = HubView.Section.MODS;
      }

      if (s != this.section) {
         this.section = s;
         this.switchedAt = System.currentTimeMillis();
         switch (s) {
            case COSMETICS:
               this.part = new CosmeticsPart();
               break;
            case EMOTES:
               this.part = new EmotesPart();
               break;
            case GAMES:
               this.part = new GamesPart();
               break;
            case HUD:
            default:
               this.part = new ModsPart();
               break;
            case DESIGN:
               this.part = new DesignPart();
         }

         this.part.shown();
      }
   }

   public float sectionT(float ms) {
      return Math.min(1.0F, (float)(System.currentTimeMillis() - this.switchedAt) * UiSettings.anim() / ms);
   }

   @Override
   public float designW() {
      return 740.0F;
   }

   @Override
   public float designH() {
      return 450.0F;
   }

   @Override
   public float openMs() {
      return 380.0F;
   }

   private void glassPanel(int x, int y, int w, int h, int r, float t, boolean anim) {
      float glass = (float)UiSettings.glass;
      Gx.shadow(x, y, w, h, r, this.p(16.0), 0.5F);
      Gx.rectV(x, y, w, h, r, Gx.withAlpha(Gx.mix(Style.bg, -14016192, 0.22F), 0.94F * glass + 0.04F), Gx.withAlpha(Style.bg, 0.97F * glass + 0.03F));
      if (anim) {
         BgFx.drawIn(x + r / 3, y + r / 3, w - 2 * (r / 3), h - 2 * (r / 3), t * 0.9F);
      }

      Gx.rectV(x, y, w, Math.min(h, this.p(70.0)), r, Gx.withAlpha(-1, 0.05F), 16777215);
      Gx.outline(x, y, w, h, r, Math.max(1, this.p(0.7)), Gx.withAlpha(-1, 0.12F));
   }

   @Override
   public void draw() {
      boolean closing = this.host != null && this.host.closing();
      float t = closing ? this.open : Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.3F * this.open));
      int sw = this.p(70.0);
      int gap = this.p(10.0);
      int mw = this.p(652.0);
      int hh = this.p(446.0);
      int total = sw + gap + mw;
      int x0 = (this.W - total) / 2;
      int y0 = (this.H - hh) / 2;
      int cx = this.W / 2;
      int cy = this.H / 2;
      int R = this.p(18.0);
      Gx.pushAlpha(t);
      Gx.push();
      Gx.scaleAt(cx, cy, 0.95F + 0.05F * t);
      Gx.translate(0.0F, this.pf(10.0) * (1.0F - t));
      Gx.push();
      Gx.translate(-this.pf(24.0) * (1.0F - t), 0.0F);
      this.glassPanel(x0, y0, sw, hh, R, t, false);
      int logo = this.p(34.0);
      boolean hl = this.hover(x0, y0 + this.p(8.0), sw, this.p(46.0));
      float hlf = Ease.to("hub:logo", hl ? 1.0F : 0.0F, 14.0F);
      Gx.glow(x0 + sw / 2, y0 + this.p(14.0) + logo / 2, this.p(34.0), Gx.withAlpha(Style.accent, 0.1F + 0.12F * hlf));
      Gx.image(
         Art.brickImg(logo, Halloween.on() ? -38118 : (Xmas.on() ? -1890757 : Style.accent)),
         x0 + (sw - logo) / 2,
         y0 + this.p(14.0) - Math.round(this.pf(2.0) * hlf),
         logo,
         logo,
         -1
      );
      if (Halloween.on()) {
         Halloween.witchHat(x0 + sw / 2.0F + this.p(2.0), y0 + this.p(20.0) - this.pf(2.0) * hlf, this.p(30.0), 0.0F);
      } else {
         Xmas.hat(x0 + sw / 2.0F + this.p(2.0), y0 + this.p(20.0) - this.pf(2.0) * hlf, this.p(30.0), 0.0F);
      }

      Kit.hit(x0, y0 + this.p(8.0), sw, this.p(46.0)).click(() -> this.host.open(new MenuView()));
      Gx.fill(x0 + this.p(16.0), y0 + this.p(60.0), sw - this.p(32.0), Math.max(1, this.p(0.6)), Gx.withAlpha(-1, 0.08F));
      HubView.Section[] items = HubView.Section.values();
      boolean modern = UiSettings.guiStyle == 1;
      int itemH = this.p(56.0);
      int iy = y0 + this.p(68.0);
      int active = this.section.ordinal();
      float ay = Ease.to("hub:side", iy + active * itemH, 18.0F, iy + active * itemH);
      if (modern) {
         Gx.rect(x0 + this.p(20.0), Math.round(ay) + itemH - this.p(5.0), sw - this.p(40.0), Math.max(2, this.p(2.0)), this.p(1.0), Style.accent);
      } else {
         Gx.glow(x0 + sw / 2, Math.round(ay + itemH / 2.0F), this.p(38.0), Gx.withAlpha(Style.accent, 0.14F));
         Gx.rect(x0 + this.p(8.0), Math.round(ay) + this.p(3.0), sw - this.p(16.0), itemH - this.p(6.0), this.p(12.0), Gx.withAlpha(Style.accent, 0.18F));
         Gx.outline(
            x0 + this.p(8.0),
            Math.round(ay) + this.p(3.0),
            sw - this.p(16.0),
            itemH - this.p(6.0),
            this.p(12.0),
            Math.max(1, this.p(0.6)),
            Gx.withAlpha(Style.accent, 0.35F)
         );
         Gx.rect(x0 + this.p(2.0), Math.round(ay) + this.p(16.0), this.p(3.0), itemH - this.p(32.0), this.p(2.0), Style.accent);
      }

      for (int i = 0; i < items.length; i++) {
         HubView.Section s = items[i];
         int y = iy + i * itemH;
         boolean h = this.hover(x0, y, sw, itemH);
         float hv = Ease.to("hub:si" + i, h ? 1.0F : 0.0F, 14.0F);
         boolean act = s == this.section;
         if (modern) {
            int col = act ? Style.accent : Gx.mix(Style.muted, Style.text, hv);
            Gx.icon(s.icon, x0 + sw / 2.0F, y + itemH / 2.0F - this.pf(1.0) * hv, this.pf(20.0F + hv), col);
         } else {
            int col = act ? Style.accent : Gx.mix(Style.sub, Style.text, hv);
            Gx.icon(s.icon, x0 + sw / 2.0F, y + this.p(21.0) - this.pf(1.5) * hv, this.pf(18.0F + hv * 1.5F), col);
            Gx.textCenter(s.label, x0 + sw / 2.0F, y + this.p(41.0), this.pf(6.1F), 3, act ? Style.text : Gx.mix(Style.muted, Style.sub, hv));
         }

         Kit.hit(x0, y, sw, itemH).click(() -> this.select(s));
      }

      int by = y0 + hh - this.p(48.0);
      boolean hc = this.hover(x0 + this.p(12.0), by, sw - this.p(24.0), this.p(36.0));
      float hcf = Ease.to("hub:close", hc ? 1.0F : 0.0F, 16.0F);
      Gx.rect(x0 + this.p(12.0), by, sw - this.p(24.0), this.p(36.0), this.p(12.0), Gx.withAlpha(Style.danger, 0.18F * hcf));
      Gx.icon("x", x0 + sw / 2.0F, by + this.p(18.0), this.pf(15.0), Gx.mix(Style.muted, Style.danger, hcf));
      Kit.hit(x0 + this.p(12.0), by, sw - this.p(24.0), this.p(36.0)).click(() -> this.host.close());
      if (Halloween.on()) {
         Halloween.cobweb(x0, y0, sw, this.P);
      } else {
         Xmas.snowCap(x0, y0, sw, this.P);
      }

      Gx.pop();
      int mx0 = x0 + sw + gap;
      this.glassPanel(mx0, y0, mw, hh, R, t, true);
      if (UiSettings.guiLine) {
         int lw = this.p(180.0);
         float sweep = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 2600.0));
         int lx = mx0 + R + Math.round((mw - 2 * R - lw) * sweep);
         Gx.rect(lx, y0, lw, Math.max(1, this.p(1.4)), this.p(1.0), Gx.withAlpha(Style.accent, 0.85F));
         Gx.glow(lx + lw / 2, y0, this.p(60.0), Gx.withAlpha(Style.accent, 0.16F));
      }

      float st = Ease.outCubic(this.sectionT(260.0F));
      Gx.pushAlpha(st);
      Gx.push();
      Gx.translate(0.0F, this.pf(8.0) * (1.0F - st));
      this.part.draw(this, mx0, y0, mw, hh);
      Gx.pop();
      Gx.popAlpha();
      if (Halloween.on()) {
         Halloween.cobweb(mx0, y0, mw, this.P);
         Halloween.lanterns(x0 - this.p(10.0), y0 - this.p(34.0), mx0 + mw + this.p(10.0), this.P, this.p(14.0), 0);
      } else {
         Xmas.snowCap(mx0, y0, mw, this.P);
         Xmas.lights(x0 - this.p(10.0), y0 - this.p(34.0), mx0 + mw + this.p(10.0), this.P, this.p(14.0), 0);
      }

      Gx.pop();
      Gx.popAlpha();
   }

   @Override
   public boolean key(int key, int mods) {
      return this.part.key(key, mods);
   }

   @Override
   public boolean chr(String s) {
      return this.part.chr(s);
   }

   @Override
   public void tick() {
      this.part.tick();
   }

   public int header(int x, int y, String title, String sub) {
      Gx.text(title, x, y, this.pf(17.0), 3, Style.text);
      if (sub != null) {
         Gx.text(sub, x, y + this.pf(24.0), this.pf(8.2F), 1, Style.sub);
      }

      return y + this.p(sub != null ? 44.0 : 28.0);
   }

   public void searchBox(String key, int x, int y, int w, int h, String text, boolean focus) {
      this.searchBox(key, x, y, w, h, text, focus, "Suchen…");
   }

   public void searchBox(String key, int x, int y, int w, int h, String text, boolean focus, String placeholder) {
      boolean hv = this.hover(x, y, w, h);
      float f = Ease.to("sb:" + key, focus ? 1.0F : (hv ? 0.4F : 0.0F), 14.0F);
      Gx.rect(x, y, w, h, h / 2, Gx.mix(Style.surface, Style.surface2, f));
      Gx.outline(x, y, w, h, h / 2, Math.max(1, this.p(0.7)), Gx.mix(Style.stroke, Style.accent, f));
      Gx.icon("search", x + this.p(15.0), y + h / 2.0F, this.pf(11.0), Gx.mix(Style.muted, Style.accent, f));
      float fs = this.pf(8.4F);
      if (text.isEmpty()) {
         Gx.textMid(placeholder, x + this.p(27.0), y + h / 2.0F, fs, 1, Style.muted);
      } else {
         String shown = Fonts.ellipsize(text, 2, fs, w - this.p(50.0));
         float tw = Gx.textMid(shown, x + this.p(27.0), y + h / 2.0F, fs, 2, Style.text);
         if (focus && System.currentTimeMillis() / 500L % 2L == 0L) {
            Gx.fill(Math.round(x + this.p(28.0) + tw), y + h / 2 - this.p(6.0), Math.max(1, this.p(0.9)), this.p(12.0), Style.accent);
         }

         boolean hx = this.hover(x + w - h, y, h, h);
         Gx.icon("x", x + w - h / 2.0F, y + h / 2.0F, this.pf(9.0), hx ? Style.text : Style.muted);
      }
   }

   /** Empty search result: magnifier and "Nichts gefunden". */
   public void nothingFound(float cx, float cy) {
      Gx.icon("search", cx, cy - this.pf(14.0), this.pf(26.0), Style.muted);
      Gx.textCenter("Nichts gefunden", cx, cy + this.pf(12.0), this.pf(9.0), 2, Style.sub);
   }

   /** Opens Mods with this text in its search (Design has no list of its own: it searches every module). */
   public void searchModules(String text) {
      this.select(HubView.Section.MODS);
      if (this.part instanceof ModsPart m) m.searchFor(text);
   }

   public void tabs(String key, int x, int y, String[] labels, int selected, IntConsumer onPick) {
      float fs = this.pf(7.8F);
      float sp = this.pf(1.1F);
      int padX = this.p(12.0);
      int h = this.p(26.0);
      int gap = this.p(4.0);
      int[] xs = new int[labels.length];
      int[] ws = new int[labels.length];
      int cx = x;

      for (int i = 0; i < labels.length; i++) {
         int w = Math.round(Gx.spacedWidth(labels[i], fs, 3, sp)) + padX * 2;
         xs[i] = cx;
         ws[i] = w;
         cx += w + gap;
      }

      float ax = Ease.to("tabx:" + key, xs[selected], 18.0F, xs[selected]);
      float aw = Ease.to("tabw:" + key, ws[selected], 18.0F, ws[selected]);
      Gx.rect(Math.round(ax), y, Math.round(aw), h, h / 2, Style.accent);

      for (int i = 0; i < labels.length; i++) {
         boolean hv = this.hover(xs[i], y, ws[i], h) && i != selected;
         float hvv = Ease.to("tabh:" + key + i, hv ? 1.0F : 0.0F, 14.0F);
         if (hvv > 0.01F) {
            Gx.rect(xs[i], y, ws[i], h, h / 2, Gx.withAlpha(Style.surfaceHover, hvv));
         }

         float sel = Math.max(0.0F, 1.0F - Math.abs(ax - xs[i]) / Math.max(1, ws[i]));
         int col = Gx.mix(Gx.mix(Style.sub, Style.text, hvv), Style.accentText, sel);
         Gx.textSpaced(labels[i], xs[i] + ws[i] / 2.0F, y + h / 2.0F, fs, 3, col, sp);
         int idx = i;
         Kit.hit(xs[i], y, ws[i], h).click(() -> onPick.accept(idx));
      }
   }

   public interface Part {
      void draw(HubView var1, int var2, int var3, int var4, int var5);

      default boolean key(int key, int mods) {
         return false;
      }

      default boolean chr(String s) {
         return false;
      }

      default void shown() {
      }

      default void tick() {
      }
   }

   public static enum Section {
      MODS("mods", "MODS"),
      COSMETICS("shirt", "KOSMETIK"),
      EMOTES("emote", "EMOTES"),
      GAMES("gamepad", "SPIELE"),
      HUD("layout", "HUD"),
      DESIGN("palette", "DESIGN");

      public final String icon;
      public final String label;

      private Section(String icon, String label) {
         this.icon = icon;
         this.label = label;
      }
   }
}
