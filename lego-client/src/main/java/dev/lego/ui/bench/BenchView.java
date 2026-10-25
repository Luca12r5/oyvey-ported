package dev.lego.ui.bench;

import dev.lego.core.Category;
import dev.lego.core.Lang;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Art;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Halloween;
import dev.lego.ui.Keys;
import dev.lego.ui.Kit;
import dev.lego.ui.MenuView;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Toasts;
import dev.lego.ui.UiSettings;
import dev.lego.ui.View;
import dev.lego.ui.Xmas;
import dev.lego.ui.hub.HubView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * LEGO Werkbank, the click GUI: categories as bricks across the top, the modules as a wall of
 * square tiles, a tool box with the settings that slides in from the right and, at the bottom,
 * the build plan. Nothing is switched on the spot: clicks only plan the changes (see {@link Draft}),
 * ANWENDEN puts them all in at once.
 */
public final class BenchView extends View {
   /** The plan survives closing and reopening, like the pending picks in the Bauloo menu. */
   static final Draft DRAFT = new Draft();
   private static Category category = Category.HUD;
   private static String page;

   private Module inspect;
   private float gridScroll;
   private float gridTarget;
   private float gridMax;
   private float toolScroll;
   private float toolTarget;
   private float toolMax;
   private float planScroll;
   private String search = "";
   private boolean searchFocus;
   private Setting.Text editing;
   private Module binding;
   private boolean askClose;
   private long appliedAt;
   private final Set<Module> applied = new HashSet<>();
   private HubView sub;

   public BenchView() {
      if (page != null) {
         this.openPage(page);
      }
   }

   /** Opens on a category (CI shots and other callers). */
   public static BenchView on(Category c) {
      category = c;
      page = null;
      return new BenchView();
   }

   /** Opens on a page of the dock (COSMETICS, EMOTES, GAMES, DESIGN, HUD) or the modules (MODS / null). */
   public static BenchView at(String target) {
      page = target == null || target.equals("MODS") ? null : target;
      return new BenchView();
   }

   /** The planned changes (CI and tests). */
   public static Draft draft() {
      return DRAFT;
   }

   private static List<Category> categories() {
      List<Category> out = new ArrayList<>();
      for (Category c : Category.values()) {
         if (c.listed) {
            out.add(c);
         }
      }
      return out;
   }

   private List<Module> modules() {
      List<Module> out = new ArrayList<>();
      String q = this.search.toLowerCase(Locale.ROOT).trim();
      for (Module m : Modules.ALL) {
         if (m.hidden) {
            continue;
         }
         if (q.isEmpty()) {
            if (m.category == category) {
               out.add(m);
            }
         } else if (m.category.listed && (m.displayName().toLowerCase(Locale.ROOT).contains(q) || m.displayDesc().toLowerCase(Locale.ROOT).contains(q))) {
            out.add(m);
         }
      }
      return out;
   }

   private static int count(Category c) {
      int n = 0;
      for (Module m : Modules.ALL) {
         if (m.category == c && !m.hidden) {
            n++;
         }
      }
      return n;
   }

   @Override
   public float designW() {
      return 800.0F;
   }

   @Override
   public float designH() {
      return 480.0F;
   }

   @Override
   public float openMs() {
      return 420.0F;
   }

   // ---------------------------------------------------------------- drawing

   @Override
   public void draw() {
      boolean closing = this.host != null && this.host.closing();
      float t = closing ? this.open : Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.45F * this.open));
      int w = this.p(800.0);
      int h = this.p(480.0);
      int x0 = (this.W - w) / 2;
      int y0 = (this.H - h) / 2;
      Gx.pushAlpha(t);
      Gx.push();
      Gx.translate(0.0F, this.pf(26.0) * (1.0F - t));
      this.bench(x0, y0, w, h);
      int top = this.p(64.0);
      int bottom = this.p(52.0);
      this.bricks(x0, y0, w, top, t);
      int bodyY = y0 + top;
      int bodyH = h - top - bottom;
      if (this.sub != null) {
         this.sub.embed(this);
         Gx.clip(x0 + this.p(10.0), bodyY, w - this.p(20.0), bodyH);
         Kit.clip(x0 + this.p(10.0), bodyY, w - this.p(20.0), bodyH);
         this.sub.drawPart(x0 + this.p(10.0), bodyY, w - this.p(20.0), bodyH);
         Kit.unclip();
         Gx.unclip();
      } else {
         float ins = Ease.to("bench:ins", this.inspect != null ? 1.0F : 0.0F, 14.0F);
         int toolW = Math.round(this.p(270.0) * ins);
         this.wall(x0 + this.p(14.0), bodyY + this.p(6.0), w - this.p(28.0) - toolW, bodyH - this.p(12.0));
         if (toolW > this.p(4.0)) {
            this.toolbox(x0 + w - toolW - this.p(8.0), bodyY + this.p(2.0), toolW, bodyH - this.p(4.0), ins);
         }
      }
      this.planBar(x0, y0 + h - bottom, w, bottom);
      if (Halloween.on()) {
         Halloween.cobweb(x0, y0, w, this.P);
      } else {
         Xmas.snowCap(x0, y0, w, this.P);
      }
      if (this.askClose) {
         this.closeDialog(x0, y0, w, h);
      }
      Gx.pop();
      Gx.popAlpha();
   }

   /** The work bench: dark plate with a stud pattern and a wooden frame. */
   private void bench(int x, int y, int w, int h) {
      int r = this.p(10.0);
      Gx.shadow(x, y, w, h, r, this.p(22.0), 0.6F);
      Gx.rect(x - this.p(5.0), y - this.p(5.0), w + this.p(10.0), h + this.p(10.0), r + this.p(4.0), Gx.mix(0xFF2A1D14, Style.bg, 0.35F));
      Gx.rectV(x, y, w, h, r, Gx.mix(0xFF1D1E24, Style.bg, 0.4F), Gx.mix(0xFF111216, Style.bg, 0.3F));
      int step = Math.max(6, this.p(16.0));
      Gx.image(Gx.painted("bench:studs:" + step, w, h, g -> {
         for (int sy = step / 2; sy < h; sy += step) {
            for (int sx = step / 2; sx < w; sx += step) {
               double d = step * 0.42;
               g.setColor(new Color(255, 255, 255, 9));
               g.fill(new Ellipse2D.Double(sx - d / 2, sy - d / 2, d, d));
               g.setColor(new Color(0, 0, 0, 30));
               g.setStroke(new BasicStroke(Math.max(1.0F, step / 16.0F)));
               g.draw(new Ellipse2D.Double(sx - d / 2, sy - d / 2 + 1, d, d));
            }
         }
      }), x, y, w, h, -1);
      Gx.outline(x, y, w, h, r, Math.max(1, this.p(0.8)), Gx.withAlpha(-1, 0.10F));
   }

   /** Top: logo, the categories as bricks, search and close. */
   private void bricks(int x0, int y0, int w, int top, float t) {
      int logo = this.p(30.0);
      Gx.image(Art.brickImg(logo, Halloween.on() ? -38118 : (Xmas.on() ? -1890757 : Style.accent)), x0 + this.p(16.0), y0 + (top - logo) / 2, logo, logo, -1);
      Gx.text("LEGO", x0 + this.p(52.0), y0 + this.p(16.0), this.pf(11.0), 3, Style.text);
      Gx.text("WERKBANK", x0 + this.p(52.0), y0 + this.p(31.0), this.pf(7.0), 2, Style.accent);
      Kit.hit(x0 + this.p(10.0), y0, this.p(110.0), top).click(() -> this.leave(() -> this.host.open(new MenuView())));
      List<Category> cats = categories();
      int bx = x0 + this.p(128.0);
      int bh = this.p(34.0);
      int by = y0 + (top - bh) / 2 + this.p(3.0);
      for (int i = 0; i < cats.size(); i++) {
         Category c = cats.get(i);
         String label = c.displayTitle().toUpperCase(Locale.ROOT);
         float fs = this.pf(7.2F);
         int bw = Math.round(Gx.width(label, fs, 3)) + this.p(44.0);
         boolean act = this.sub == null && this.search.isBlank() && c == category;
         boolean hv = this.hover(bx, by - this.p(6.0), bw, bh + this.p(6.0));
         float lift = Ease.to("bench:lift" + i, act ? 1.0F : (hv ? 0.45F : 0.0F), 16.0F);
         float drop = Ease.outBack(Ease.stagger(t, i, 0.06F, 0.5F));
         int yy = Math.round(by - this.pf(5.0) * lift - this.pf(30.0) * (1.0F - drop));
         int col = act ? Style.accent : Gx.mix(0xFF3A3D47, Style.surface2, 0.3F);
         this.brick(bx, yy, bw, bh, col, lift);
         int fg = act ? Style.accentText : Gx.mix(Style.sub, Style.text, lift);
         Gx.icon(c.icon, bx + this.p(15.0), yy + bh / 2.0F + this.p(1.0), this.pf(11.0), fg);
         Gx.textMid(label, bx + this.p(26.0), yy + bh / 2.0F + this.p(1.0), fs, 3, fg);
         String n = String.valueOf(count(c));
         Gx.textRight(n, bx + bw - this.p(8.0), yy + bh / 2.0F + this.p(1.0), this.pf(6.4F), 2, Gx.withAlpha(fg, 0.7F));
         Kit.hit(bx, by - this.p(6.0), bw, bh + this.p(6.0)).click(() -> {
            category = c;
            page = null;
            this.sub = null;
            this.search = "";
            this.inspect = null;
            this.gridTarget = 0.0F;
         });
         bx += bw + this.p(6.0);
      }
      // search (right)
      int sw = this.p(132.0);
      int sh = this.p(26.0);
      int sx = x0 + w - sw - this.p(48.0);
      int sy = y0 + (top - sh) / 2;
      boolean shv = this.hover(sx, sy, sw, sh);
      float sf = Ease.to("bench:sf", this.searchFocus ? 1.0F : (shv ? 0.4F : 0.0F), 14.0F);
      Gx.rect(sx, sy, sw, sh, this.p(5.0), Gx.mix(0xFF16171C, Style.surface, 0.3F));
      Gx.outline(sx, sy, sw, sh, this.p(5.0), Math.max(1, this.p(0.7)), Gx.mix(Style.stroke, Style.accent, sf));
      Gx.icon("search", sx + this.p(13.0), sy + sh / 2.0F, this.pf(10.0), Gx.mix(Style.muted, Style.accent, sf));
      String shown = this.search.isEmpty() ? (this.searchFocus ? "" : Lang.get("ui.search", "Suchen…")) : Fonts.ellipsize(this.search, 2, this.pf(7.6F), sw - this.p(34.0));
      float tw = Gx.textMid(shown, sx + this.p(24.0), sy + sh / 2.0F, this.pf(7.6F), 2, this.search.isEmpty() ? Style.muted : Style.text);
      if (this.searchFocus && System.currentTimeMillis() / 500L % 2L == 0L) {
         Gx.fill(Math.round(sx + this.p(25.0) + (this.search.isEmpty() ? 0 : tw)), sy + this.p(7.0), Math.max(1, this.p(0.9)), sh - this.p(14.0), Style.accent);
      }
      Kit.hit(sx, sy, sw, sh).click(() -> {
         this.searchFocus = true;
         this.sub = null;
      }).right(() -> this.search = "");
      // close
      int cs = this.p(28.0);
      int cx = x0 + w - cs - this.p(12.0);
      int cy = y0 + (top - cs) / 2;
      boolean chv = this.hover(cx, cy, cs, cs);
      float cf = Ease.to("bench:close", chv ? 1.0F : 0.0F, 16.0F);
      Gx.rect(cx, cy, cs, cs, this.p(6.0), Gx.withAlpha(Style.danger, 0.15F + 0.3F * cf));
      Gx.icon("x", cx + cs / 2.0F, cy + cs / 2.0F, this.pf(12.0), Gx.mix(Style.sub, -1, cf));
      Kit.hit(cx, cy, cs, cs).click(this::onEscape);
      Gx.fill(x0 + this.p(12.0), y0 + top - this.p(2.0), w - this.p(24.0), Math.max(1, this.p(0.6)), Gx.withAlpha(-1, 0.07F));
   }

   /** A LEGO brick seen from the front: body, top edge and two studs. */
   private void brick(int x, int y, int w, int h, int col, float lift) {
      int stud = this.p(9.0);
      int sh = this.p(4.0);
      Gx.shadow(x, y + sh, w, h, this.p(3.0), this.p(3.0 + 4.0 * lift), 0.35F + 0.2F * lift);
      for (int k = 0; k < 2; k++) {
         int sx = x + this.p(10.0) + k * (w - this.p(20.0) - stud);
         Gx.rect(sx, y - sh, stud, sh + this.p(2.0), this.p(2.0), Gx.mix(col, -1, 0.10F));
         Gx.fill(sx + this.p(1.0), y - sh, stud - this.p(2.0), Math.max(1, this.p(1.0)), Gx.withAlpha(-1, 0.25F));
      }
      Gx.rectV(x, y, w, h, this.p(3.0), Gx.mix(col, -1, 0.10F), Gx.mix(col, 0xFF000000, 0.22F));
      Gx.fill(x + this.p(2.0), y + this.p(1.0), w - this.p(4.0), Math.max(1, this.p(1.0)), Gx.withAlpha(-1, 0.22F));
      Gx.fill(x + this.p(2.0), y + h - Math.max(1, this.p(2.0)), w - this.p(4.0), Math.max(1, this.p(2.0)), Gx.withAlpha(0xFF000000, 0.25F));
   }

   /** The module wall. */
   private void wall(int x, int y, int w, int h) {
      List<Module> mods = this.modules();
      int gap = this.p(10.0);
      int cols = Math.max(2, (w + gap) / (this.p(140.0) + gap));
      int tw = (w - gap * (cols - 1)) / cols;
      int th = this.p(112.0);
      int rows = (mods.size() + cols - 1) / cols;
      int content = rows * (th + gap);
      this.gridMax = Math.max(0, content - h);
      this.gridTarget = Math.max(0.0F, Math.min(this.gridMax, this.gridTarget));
      this.gridScroll = Ease.lerp(this.gridScroll, this.gridTarget, 0.35F);
      if (!this.search.isBlank()) {
         Gx.text(mods.size() + " Treffer für \"" + this.search + "\"", x, y - this.p(2.0), this.pf(7.0), 2, Style.sub);
      }
      Kit.hit(x, y, w, h).scroll(a -> this.gridTarget -= (float)(a * this.p(60.0)));
      if (mods.isEmpty()) {
         Gx.icon("search", x + w / 2.0F, y + h / 2.0F - this.p(12.0), this.pf(26.0), Style.muted);
         Gx.textCenter("Nichts gefunden", x + w / 2.0F, y + h / 2.0F + this.p(14.0), this.pf(9.0), 2, Style.sub);
         return;
      }
      Gx.clip(x - this.p(6.0), y, w + this.p(12.0), h);
      Kit.clip(x - this.p(6.0), y, w + this.p(12.0), h);
      float since = (System.currentTimeMillis() - this.appliedAt) / 700.0F;
      for (int i = 0; i < mods.size(); i++) {
         Module m = mods.get(i);
         int tx = x + i % cols * (tw + gap);
         int ty = Math.round(y + this.p(4.0) + i / cols * (th + gap) - this.gridScroll);
         if (ty + th < y || ty > y + h) {
            continue;
         }
         float snap = this.applied.contains(m) && since < 1.0F ? 1.0F - since : 0.0F;
         this.tile(m, tx, ty, tw, th, snap);
      }
      Kit.unclip();
      Gx.unclip();
      if (this.gridMax > 0.0F) {
         int trackH = h - this.p(8.0);
         int knob = Math.max(this.p(24.0), Math.round(trackH * h / (float)(h + this.gridMax)));
         int ky = y + this.p(4.0) + Math.round((trackH - knob) * this.gridScroll / this.gridMax);
         Gx.rect(x + w + this.p(3.0), ky, this.p(3.0), knob, this.p(2.0), Gx.withAlpha(Style.accent, 0.6F));
      }
   }

   private void tile(Module m, int x, int y, int w, int h, float snap) {
      boolean on = DRAFT.on(m);
      boolean planned = DRAFT.changed(m);
      boolean hv = this.hover(x, y, w, h);
      String id = "bench:t:" + m.id;
      float hf = Ease.to(id, hv ? 1.0F : 0.0F, 16.0F);
      float of = Ease.to(id + ":on", on ? 1.0F : 0.0F, 14.0F);
      float sel = Ease.to(id + ":sel", this.inspect == m ? 1.0F : 0.0F, 14.0F);
      int lift = Math.round(this.pf(3.0) * hf - this.pf(4.0) * snap);
      int yy = y - lift;
      Gx.shadow(x, yy + this.p(3.0), w, h, this.p(6.0), this.p(4.0 + 4.0 * hf), 0.35F + 0.15F * hf);
      int base = Gx.mix(Gx.mix(0xFF262830, Style.surface, 0.25F), Gx.mix(0xFF262830, Style.accent, 0.22F), of);
      Gx.rectV(x, yy, w, h, this.p(6.0), Gx.mix(base, -1, 0.06F + 0.04F * hf), Gx.mix(base, 0xFF000000, 0.18F));
      Gx.fill(x + this.p(3.0), yy + this.p(1.0), w - this.p(6.0), Math.max(1, this.p(1.0)), Gx.withAlpha(-1, 0.14F + 0.1F * hf));
      if (sel > 0.01F || planned) {
         Gx.outline(x, yy, w, h, this.p(6.0), Math.max(1, this.p(1.2)), Gx.withAlpha(planned ? 0xFFFFC23D : Style.accent, Math.max(sel, planned ? 0.85F : 0.0F)));
      }
      // lamp
      int lr = this.p(4.5);
      int lx = x + w - this.p(13.0);
      int ly = yy + this.p(13.0);
      boolean was = m.enabled;
      int lamp;
      if (planned && on != was) {
         float pulse = (float)(0.55 + 0.45 * Math.sin(System.currentTimeMillis() / 160.0));
         lamp = Gx.withAlpha(on ? Style.ok : Style.danger, pulse);
      } else {
         lamp = on ? Style.ok : 0xFF4A4D57;
      }
      if (on) {
         Gx.glow(lx, ly, this.p(12.0), Gx.withAlpha(lamp, 0.35F));
      }
      if (m.isToggleable()) {
         Gx.circle(lx, ly, lr, lamp);
      }
      // icon
      String icon = m.icon != null ? m.icon : m.category.icon;
      int ic = this.p(34.0);
      int icx = x + this.p(14.0);
      int icy = yy + this.p(14.0);
      Gx.rect(icx, icy, ic, ic, this.p(8.0), Gx.withAlpha(on ? Style.accent : -1, on ? 0.9F : 0.07F));
      Gx.icon(icon, icx + ic / 2.0F, icy + ic / 2.0F, this.pf(18.0), on ? Style.accentText : Style.sub);
      // text
      float nfs = this.pf(8.6F);
      Gx.text(Fonts.ellipsize(m.displayName(), 3, nfs, w - this.p(24.0)), x + this.p(14.0), yy + this.p(56.0), nfs, 3, Style.text);
      List<String> lines = Fonts.wrap(m.displayDesc(), 1, this.pf(6.6F), w - this.p(26.0), 2);
      for (int k = 0; k < lines.size(); k++) {
         Gx.text(lines.get(k), x + this.p(14.0), yy + this.p(72.0) + k * this.p(10.0), this.pf(6.6F), 1, Style.muted);
      }
      // footer: state word + tool button
      String state = !m.isToggleable() ? "WERKZEUG" : planned && on != was ? (on ? "WIRD AN" : "WIRD AUS") : (on ? "AN" : "AUS");
      int stateCol = planned && on != was ? 0xFFFFC23D : (on ? Style.ok : Style.muted);
      Gx.text(state, x + this.p(14.0), yy + h - this.p(15.0), this.pf(6.2F), 3, stateCol);
      int gs = this.p(20.0);
      int gx = x + w - gs - this.p(8.0);
      int gy = yy + h - gs - this.p(7.0);
      boolean ghv = this.hover(gx, gy, gs, gs);
      if (!m.settings.isEmpty() || m.isToggleable()) {
         Gx.rect(gx, gy, gs, gs, this.p(5.0), Gx.withAlpha(-1, ghv ? 0.16F : 0.06F));
         Gx.icon("gear", gx + gs / 2.0F, gy + gs / 2.0F, this.pf(10.0), ghv ? Style.text : Style.sub);
      }
      Kit.hit(x, yy, w, h).click(() -> {
         if (m.isToggleable()) {
            DRAFT.toggle(m);
            Sound.pop();
         } else {
            this.openTools(m);
         }
      }).right(() -> this.openTools(m));
      Kit.hit(gx, gy, gs, gs).click(() -> this.openTools(m));
   }

   private void openTools(Module m) {
      if (this.inspect == m) {
         this.inspect = null;
         return;
      }
      this.inspect = m;
      DRAFT.refresh(m);
      this.toolTarget = 0.0F;
      this.toolScroll = 0.0F;
      this.editing = null;
      this.binding = null;
   }

   /** Right: the tool box with every setting of the chosen module. */
   private void toolbox(int x, int y, int w, int h, float ins) {
      Module m = this.inspect;
      if (m == null) {
         return;
      }
      Gx.pushAlpha(ins);
      Gx.shadow(x, y, w, h, this.p(8.0), this.p(10.0), 0.45F);
      Gx.rectV(x, y, w, h, this.p(8.0), Gx.mix(0xFF2B2E37, Style.surface2, 0.3F), Gx.mix(0xFF1B1D23, Style.surface, 0.3F));
      // yellow-black edge like a tool box lid
      Gx.rect(x, y, w, this.p(6.0), this.p(3.0), Gx.mix(0xFFFFC23D, Style.accent, 0.35F));
      int pad = this.p(14.0);
      int cx = x + pad;
      int cw = w - pad * 2;
      int cy = y + this.p(18.0);
      Gx.icon(m.icon != null ? m.icon : m.category.icon, cx + this.p(9.0), cy + this.p(9.0), this.pf(15.0), Style.accent);
      Gx.text(Fonts.ellipsize(m.displayName(), 3, this.pf(10.0), cw - this.p(52.0)), cx + this.p(24.0), cy, this.pf(10.0), 3, Style.text);
      Gx.text(m.category.displayTitle(), cx + this.p(24.0), cy + this.p(13.0), this.pf(6.6F), 1, Style.muted);
      int xs = this.p(20.0);
      Kit.hit(x + w - xs - this.p(8.0), cy - this.p(2.0), xs, xs).click(() -> this.inspect = null);
      Gx.icon("x", x + w - xs / 2.0F - this.p(8.0), cy + xs / 2.0F - this.p(2.0), this.pf(10.0), Style.sub);
      cy += this.p(32.0);
      int listTop = cy;
      int listH = y + h - this.p(10.0) - listTop;
      Kit.hit(x, listTop, w, listH).scroll(a -> this.toolTarget -= (float)(a * this.p(40.0)));
      this.toolTarget = Math.max(0.0F, Math.min(this.toolMax, this.toolTarget));
      this.toolScroll = Ease.lerp(this.toolScroll, this.toolTarget, 0.35F);
      Gx.clip(x, listTop, w, listH);
      Kit.clip(x, listTop, w, listH);
      int ry = Math.round(listTop - this.toolScroll);
      int start = ry;
      if (m.isToggleable()) {
         ry = this.rowBool("Modul an", DRAFT.on(m), m.id + ":on", cx, ry, cw, () -> DRAFT.toggle(m));
         ry = this.rowKey(m, cx, ry, cw);
      }
      for (Setting real : m.settings) {
         Setting s = DRAFT.edit(m, real);
         boolean changed = s != real && !java.util.Objects.equals(real.save(), s.save());
         if (changed) {
            Gx.rect(cx - this.p(6.0), ry + this.p(3.0), this.p(2.5), this.p(14.0), this.p(1.0), 0xFFFFC23D);
         }
         if (s instanceof Setting.Bool b) {
            ry = this.rowBool(s.displayName(), b.value, m.id + ":" + s.name, cx, ry, cw, () -> b.value = !b.value);
         } else if (s instanceof Setting.Num n) {
            ry = this.rowNum(n, m.id + ":" + s.name, cx, ry, cw);
         } else if (s instanceof Setting.Mode md) {
            ry = this.rowMode(md, m.id + ":" + s.name, cx, ry, cw);
         } else if (s instanceof Setting.Color col) {
            ry = this.rowColor(col, m.id + ":" + s.name, cx, ry, cw);
         } else if (s instanceof Setting.Text tx) {
            ry = this.rowText(tx, cx, ry, cw);
         } else if (s instanceof Setting.Action act) {
            ry = this.rowAction(act, m.id + ":" + s.name, cx, ry, cw);
         }
      }
      if (m.settings.isEmpty() && !m.isToggleable()) {
         Gx.text("Keine Einstellungen", cx, ry + this.p(4.0), this.pf(7.4F), 1, Style.muted);
         ry += this.p(20.0);
      }
      this.toolMax = Math.max(0, ry - start - listH + this.p(8.0));
      Kit.unclip();
      Gx.unclip();
      Gx.popAlpha();
   }

   private int label(String text, int x, int y, int w, String value) {
      Gx.text(Fonts.ellipsize(text, 2, this.pf(7.8F), w - this.p(60.0)), x, y + this.p(4.0), this.pf(7.8F), 2, Style.text);
      if (value != null) {
         Gx.textRight(value, x + w, y + this.p(9.0), this.pf(7.4F), 3, Style.accent);
      }
      return y;
   }

   private int rowBool(String name, boolean on, String key, int x, int y, int w, Runnable flip) {
      this.label(name, x, y, w, null);
      int tw = this.p(28.0);
      int th = this.p(15.0);
      Kit.toggle("bench:" + key, x + w - tw, y + this.p(2.0), tw, th, on, this.P);
      Kit.hit(x - this.p(4.0), y, w + this.p(8.0), this.p(20.0)).click(flip);
      return y + this.p(26.0);
   }

   private int rowNum(Setting.Num n, String key, int x, int y, int w) {
      this.label(n.displayName(), x, y, w, n.display());
      int sy = y + this.p(18.0);
      int sh = this.p(14.0);
      Kit.slider("bench:" + key, x, sy, w, sh, (float)n.frac(), this.P);
      Kit.hit(x - this.p(4.0), sy, w + this.p(8.0), sh).drag((px, py) -> n.setFrac((px - x) / (double)w)).scroll(a -> n.set(n.value + Math.signum(a) * (n.step > 0.0 ? n.step : (n.max - n.min) / 50.0)));
      return y + this.p(40.0);
   }

   private int rowMode(Setting.Mode md, String key, int x, int y, int w) {
      this.label(md.displayName(), x, y, w, null);
      int py = y + this.p(18.0);
      int ph = this.p(18.0);
      int px = x;
      float fs = this.pf(6.8F);
      for (int i = 0; i < md.options.length; i++) {
         String o = md.options[i];
         int ow = Math.min(w, Math.round(Gx.width(o, fs, 2)) + this.p(16.0));
         if (px + ow > x + w && px > x) {
            px = x;
            py += ph + this.p(5.0);
         }
         boolean act = md.index == i;
         boolean hv = this.hover(px, py, ow, ph);
         Gx.rect(px, py, ow, ph, this.p(4.0), act ? Style.accent : Gx.withAlpha(-1, hv ? 0.14F : 0.06F));
         Gx.textCenter(Fonts.ellipsize(o, 2, fs, ow - this.p(8.0)), px + ow / 2.0F, py + ph / 2.0F, fs, 2, act ? Style.accentText : Style.sub);
         int idx = i;
         Kit.hit(px, py, ow, ph).click(() -> md.index = idx);
         px += ow + this.p(5.0);
      }
      return py + ph + this.p(10.0);
   }

   private int rowColor(Setting.Color col, String key, int x, int y, int w) {
      this.label(col.displayName(), x, y, w, col.name());
      int sz = this.p(17.0);
      int gap = this.p(5.0);
      int per = Math.max(1, (w + gap) / (sz + gap));
      int cy = y + this.p(18.0);
      int n = Setting.Color.PALETTE.length + 1;
      for (int i = 0; i < n; i++) {
         int cx = x + i % per * (sz + gap);
         int yy = cy + i / per * (sz + gap);
         boolean act = col.index == i;
         if (i < Setting.Color.PALETTE.length) {
            int c = i == Setting.Color.PALETTE.length - 1 ? Gx.mix(0xFFFF4D6D, 0xFF4DA3FF, (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 600.0))) : Setting.Color.PALETTE[i];
            Gx.rect(cx, yy, sz, sz, this.p(4.0), c);
         } else {
            Gx.rect(cx, yy, sz, sz, this.p(4.0), col.customArgb());
            Gx.icon("plus", cx + sz / 2.0F, yy + sz / 2.0F, this.pf(9.0), -1);
         }
         if (act) {
            Gx.outline(cx - this.p(2.0), yy - this.p(2.0), sz + this.p(4.0), sz + this.p(4.0), this.p(5.0), Math.max(1, this.p(1.2)), -1);
         }
         int idx = i;
         Kit.hit(cx, yy, sz, sz).click(() -> col.index = idx);
      }
      int rows = (n + per - 1) / per;
      int end = cy + rows * (sz + gap);
      if (col.custom()) {
         // own colour: hue and brightness bars
         int bh = this.p(10.0);
         Gx.image(Gx.painted("bench:hue", 256, 8, g -> {
            for (int i = 0; i < 256; i++) {
               g.setColor(Color.getHSBColor(i / 256.0F, 1.0F, 1.0F));
               g.fillRect(i, 0, 1, 8);
            }
         }), x, end, w, bh, -1);
         int hk = x + Math.round(w * col.hue);
         Gx.rect(hk - this.p(2.0), end - this.p(2.0), this.p(4.0), bh + this.p(4.0), this.p(2.0), -1);
         Kit.hit(x, end - this.p(2.0), w, bh + this.p(4.0)).drag((px, py) -> col.hue = Math.max(0.0F, Math.min(0.999F, (px - x) / (float)w)));
         end += bh + this.p(8.0);
         int base = Color.HSBtoRGB(col.hue, 1.0F, 1.0F);
         Gx.image(Gx.painted("bench:sat" + Integer.toHexString(base), 128, 8, g -> {
            g.setPaint(new GradientPaint(0, 0, Color.WHITE, 128, 0, new Color(base)));
            g.fillRect(0, 0, 128, 8);
         }), x, end, w, bh, -1);
         int sk = x + Math.round(w * col.sat);
         Gx.rect(sk - this.p(2.0), end - this.p(2.0), this.p(4.0), bh + this.p(4.0), this.p(2.0), -1);
         Kit.hit(x, end - this.p(2.0), w, bh + this.p(4.0)).drag((px, py) -> col.sat = Math.max(0.0F, Math.min(1.0F, (px - x) / (float)w)));
         end += bh + this.p(8.0);
      }
      return end + this.p(6.0);
   }

   private int rowText(Setting.Text tx, int x, int y, int w) {
      this.label(tx.displayName(), x, y, w, null);
      int fy = y + this.p(17.0);
      int fh = this.p(20.0);
      boolean focus = this.editing == tx;
      Gx.rect(x, fy, w, fh, this.p(4.0), 0xFF15161B);
      Gx.outline(x, fy, w, fh, this.p(4.0), Math.max(1, this.p(0.7)), focus ? Style.accent : Style.stroke);
      String v = tx.value.isEmpty() && !focus ? (tx.hint == null ? "" : tx.hint) : tx.value;
      float tw = Gx.textMid(Fonts.ellipsize(v, 2, this.pf(7.4F), w - this.p(14.0)), x + this.p(7.0), fy + fh / 2.0F, this.pf(7.4F), 2, tx.value.isEmpty() ? Style.muted : Style.text);
      if (focus && System.currentTimeMillis() / 500L % 2L == 0L) {
         Gx.fill(Math.round(x + this.p(8.0) + (tx.value.isEmpty() ? 0 : tw)), fy + this.p(5.0), Math.max(1, this.p(0.9)), fh - this.p(10.0), Style.accent);
      }
      Kit.hit(x, fy, w, fh).click(() -> this.editing = focus ? null : tx);
      return fy + fh + this.p(10.0);
   }

   private int rowAction(Setting.Action act, String key, int x, int y, int w) {
      int bh = this.p(22.0);
      boolean hv = this.hover(x, y, w, bh);
      Kit.button("bench:" + key, act.label, "play", x, y, w, bh, this.P, false, hv);
      Kit.hit(x, y, w, bh).click(() -> {
         try {
            act.action.run();
         } catch (Throwable e) {
            dev.lego.LegoClient.LOG("Werkbank: " + act.name + ": " + e);
         }
      });
      return y + bh + this.p(10.0);
   }

   private int rowKey(Module m, int x, int y, int w) {
      int k = DRAFT.key(m);
      String v = this.binding == m ? "Taste drücken…" : (k < 0 ? "keine" : Keys.name(k));
      this.label("Taste", x, y, w, null);
      int bw = this.p(90.0);
      int bh = this.p(17.0);
      int bx = x + w - bw;
      Gx.rect(bx, y + this.p(1.0), bw, bh, this.p(4.0), this.binding == m ? Gx.withAlpha(Style.accent, 0.3F) : Gx.withAlpha(-1, 0.07F));
      Gx.textCenter(v, bx + bw / 2.0F, y + this.p(1.0) + bh / 2.0F, this.pf(6.8F), 2, this.binding == m ? Style.accent : Style.sub);
      Kit.hit(bx, y, bw, bh + this.p(2.0)).click(() -> this.binding = this.binding == m ? null : m).right(() -> {
         DRAFT.setKey(m, -1);
         this.binding = null;
      });
      return y + this.p(26.0);
   }

   /** Bottom: the build plan with every planned change, or the dock when nothing is planned. */
   private void planBar(int x0, int y, int w, int h) {
      int bx = x0 + this.p(10.0);
      int bw = w - this.p(20.0);
      int by = y + this.p(6.0);
      int bh = h - this.p(14.0);
      List<Draft.Change> changes = DRAFT.changes();
      float shown = Ease.to("bench:plan", changes.isEmpty() ? 0.0F : 1.0F, 14.0F);
      if (shown < 0.99F) {
         Gx.pushAlpha(1.0F - shown);
         this.dock(bx, by, bw, bh);
         Gx.popAlpha();
      }
      if (shown > 0.01F && !changes.isEmpty()) {
         Gx.pushAlpha(shown);
         int yy = by + Math.round(this.pf(12.0) * (1.0F - shown));
         Gx.rect(bx, yy, bw, bh, this.p(6.0), Gx.mix(0xFF2B2416, Style.surface2, 0.2F));
         Gx.outline(bx, yy, bw, bh, this.p(6.0), Math.max(1, this.p(0.8)), Gx.withAlpha(0xFFFFC23D, 0.55F));
         Gx.icon("list", bx + this.p(14.0), yy + bh / 2.0F, this.pf(12.0), 0xFFFFC23D);
         Gx.text("BAUPLAN", bx + this.p(26.0), yy + this.p(6.0), this.pf(6.6F), 3, 0xFFFFC23D);
         Gx.text(changes.size() + (changes.size() == 1 ? " Änderung" : " Änderungen"), bx + this.p(26.0), yy + this.p(18.0), this.pf(6.2F), 1, Style.sub);
         String applyLabel = "ANWENDEN (" + changes.size() + ")";
         int aw = Math.round(Gx.width(applyLabel, this.pf(8.0F), 3)) + this.p(26.0);
         int ax = bx + bw - aw - this.p(6.0);
         int ah = bh - this.p(8.0);
         int ay = yy + this.p(4.0);
         boolean ahv = this.hover(ax, ay, aw, ah);
         float af = Ease.to("bench:apply", ahv ? 1.0F : 0.0F, 16.0F);
         float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 300.0));
         Gx.glow(ax + aw / 2, ay + ah / 2, this.p(40.0), Gx.withAlpha(0xFFFFC23D, 0.12F + 0.1F * pulse));
         Gx.rect(ax, ay, aw, ah, this.p(5.0), Gx.mix(0xFFFFC23D, -1, 0.15F * af));
         Gx.textCenter(applyLabel, ax + aw / 2.0F, ay + ah / 2.0F, this.pf(8.0F), 3, 0xFF1A1408);
         Kit.hit(ax, ay, aw, ah).click(this::apply);
         String dropLabel = "Verwerfen";
         int dw = Math.round(Gx.width(dropLabel, this.pf(7.4F), 2)) + this.p(20.0);
         int dx = ax - dw - this.p(6.0);
         boolean dhv = this.hover(dx, ay, dw, ah);
         Gx.rect(dx, ay, dw, ah, this.p(5.0), Gx.withAlpha(-1, dhv ? 0.14F : 0.06F));
         Gx.textCenter(dropLabel, dx + dw / 2.0F, ay + ah / 2.0F, this.pf(7.4F), 2, Style.sub);
         Kit.hit(dx, ay, dw, ah).click(DRAFT::clear);
         // chips
         int cx0 = bx + this.p(92.0);
         int cMax = dx - this.p(8.0);
         Gx.clip(cx0, yy, cMax - cx0, bh);
         Kit.clip(cx0, yy, cMax - cx0, bh);
         int cx = Math.round(cx0 - this.planScroll);
         int ch = this.p(18.0);
         int cy = yy + (bh - ch) / 2;
         float fs = this.pf(6.6F);
         int total = 0;
         for (Draft.Change c : changes) {
            int cw = Math.round(Gx.width(c.label(), fs, 2)) + this.p(26.0);
            int col = c.direction() > 0 ? Style.ok : (c.direction() < 0 ? Style.danger : Style.accent);
            Gx.rect(cx, cy, cw, ch, ch / 2, Gx.withAlpha(col, 0.18F));
            Gx.outline(cx, cy, cw, ch, ch / 2, Math.max(1, this.p(0.6)), Gx.withAlpha(col, 0.6F));
            Gx.textMid(c.label(), cx + this.p(8.0), cy + ch / 2.0F, fs, 2, Style.text);
            boolean xh = this.hover(cx + cw - this.p(16.0), cy, this.p(16.0), ch);
            Gx.icon("x", cx + cw - this.p(9.0), cy + ch / 2.0F, this.pf(7.0), xh ? Style.text : Style.muted);
            Kit.hit(cx + cw - this.p(16.0), cy, this.p(16.0), ch).click(() -> DRAFT.drop(c));
            cx += cw + this.p(5.0);
            total += cw + this.p(5.0);
         }
         float maxScroll = Math.max(0, total - (cMax - cx0));
         this.planScroll = Math.max(0.0F, Math.min(maxScroll, this.planScroll));
         Kit.hit(cx0, yy, cMax - cx0, bh).scroll(a -> this.planScroll -= (float)(a * this.p(40.0)));
         Kit.unclip();
         Gx.unclip();
         Gx.popAlpha();
      }
   }

   /** Dock (nothing planned): the other pages, HUD editor and Visor, plus how the bench works. */
   private void dock(int x, int y, int w, int h) {
      String[][] items = {
         {"MODULE", "grid", null}, {"KOSMETIK", "shirt", "COSMETICS"}, {"EMOTES", "emote", "EMOTES"}, {"SPIELE", "gamepad", "GAMES"},
         {"DESIGN", "palette", "DESIGN"}, {"HUD", "layout", "HUD"}, {"VISOR", "glasses", "VISOR"}
      };
      int bx = x;
      int bh = h;
      float fs = this.pf(6.6F);
      for (String[] it : items) {
         int bw = Math.round(Gx.width(it[0], fs, 3)) + this.p(34.0);
         boolean act = it[2] == null ? this.sub == null : it[2].equals(page) && this.sub != null;
         boolean hv = this.hover(bx, y, bw, bh);
         float f = Ease.to("bench:dock" + it[0], act ? 1.0F : (hv ? 0.5F : 0.0F), 16.0F);
         Gx.rect(bx, y, bw, bh, this.p(6.0), act ? Gx.withAlpha(Style.accent, 0.9F) : Gx.withAlpha(-1, 0.05F + 0.08F * f));
         int fg = act ? Style.accentText : Gx.mix(Style.sub, Style.text, f);
         Gx.icon(it[1], bx + this.p(13.0), y + bh / 2.0F, this.pf(11.0), fg);
         Gx.textMid(it[0], bx + this.p(23.0), y + bh / 2.0F, fs, 3, fg);
         String target = it[2];
         Kit.hit(bx, y, bw, bh).click(() -> this.openPage(target));
         bx += bw + this.p(5.0);
      }
      String hint = "Klick = auswählen · Rechtsklick = Werkzeug · ANWENDEN lädt alles gleichzeitig";
      float hfs = this.pf(6.2F);
      String fit = Fonts.ellipsize(hint, 1, hfs, x + w - bx - this.p(8.0));
      Gx.textRight(fit, x + w - this.p(4.0), y + bh / 2.0F, hfs, 1, Style.muted);
   }

   private void openPage(String target) {
      if (target == null) {
         this.sub = null;
         page = null;
         return;
      }
      switch (target) {
         case "HUD" -> this.leave(() -> this.host.closeThen(Env.openHudEditor));
         case "VISOR" -> this.leave(() -> this.host.closeThen(dev.lego.compat.VisorBridge::openMenu));
         default -> {
            page = target;
            this.inspect = null;
            this.sub = new HubView(HubView.Section.valueOf(target));
         }
      }
   }

   private void closeDialog(int x0, int y0, int w, int h) {
      Gx.fill(x0, y0, w, h, Gx.rgba(0, 0.55F));
      int dw = this.p(300.0);
      int dh = this.p(124.0);
      int dx = x0 + (w - dw) / 2;
      int dy = y0 + (h - dh) / 2;
      Kit.hit(x0, y0, w, h).click(() -> {});
      Gx.shadow(dx, dy, dw, dh, this.p(8.0), this.p(16.0), 0.6F);
      Gx.rect(dx, dy, dw, dh, this.p(8.0), Gx.mix(0xFF23252C, Style.surface2, 0.3F));
      Gx.rect(dx, dy, dw, this.p(5.0), this.p(3.0), 0xFFFFC23D);
      int n = DRAFT.count();
      Gx.textCenter(n + (n == 1 ? " Änderung" : " Änderungen") + " noch nicht angewendet", dx + dw / 2.0F, dy + this.p(30.0), this.pf(9.0), 3, Style.text);
      Gx.textCenter("Jetzt alles gleichzeitig laden?", dx + dw / 2.0F, dy + this.p(48.0), this.pf(7.4F), 1, Style.sub);
      int bw = (dw - this.p(40.0)) / 3;
      int bh = this.p(26.0);
      int by = dy + dh - bh - this.p(16.0);
      String[] labels = {"Zurück", "Verwerfen", "Anwenden"};
      for (int i = 0; i < 3; i++) {
         int bx = dx + this.p(12.0) + i * (bw + this.p(8.0));
         boolean hv = this.hover(bx, by, bw, bh);
         Kit.button("bench:ask" + i, labels[i], null, bx, by, bw, bh, this.P, i == 2, hv);
         int idx = i;
         Kit.hit(bx, by, bw, bh).click(() -> {
            this.askClose = false;
            if (idx == 1) {
               DRAFT.clear();
               this.host.close();
            } else if (idx == 2) {
               this.apply();
               this.host.close();
            }
         });
      }
   }

   // ---------------------------------------------------------------- actions

   private void apply() {
      List<Module> touched = new ArrayList<>();
      for (Module m : Modules.ALL) {
         if (DRAFT.changed(m)) {
            touched.add(m);
         }
      }
      int n = DRAFT.apply();
      this.applied.clear();
      this.applied.addAll(touched);
      this.appliedAt = System.currentTimeMillis();
      this.editing = null;
      this.binding = null;
      Sound.open();
      Toasts.show("check", "Werkbank", n + (n == 1 ? " Änderung" : " Änderungen") + " angewendet");
   }

   /** Leaves the bench; asks first when changes are still planned. */
   private void leave(Runnable then) {
      if (!DRAFT.empty()) {
         this.askClose = true;
         return;
      }
      then.run();
   }

   @Override
   public void onEscape() {
      if (this.askClose) {
         this.askClose = false;
      } else if (this.searchFocus) {
         this.searchFocus = false;
      } else if (this.editing != null || this.binding != null) {
         this.editing = null;
         this.binding = null;
      } else if (this.inspect != null) {
         this.inspect = null;
      } else if (this.host != null) {
         this.leave(this.host::close);
      }
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      if (this.searchFocus) {
         this.searchFocus = false;
      }
      return super.mouseDown(x, y, button, dbl);
   }

   @Override
   public boolean key(int key, int mods) {
      if (this.sub != null && !this.askClose && this.sub.key(key, mods)) {
         return true;
      }
      if (key < 0) {
         return false;
      }
      if (this.binding != null) {
         DRAFT.setKey(this.binding, key == 256 || key == 259 || key == 261 ? -1 : key);
         this.binding = null;
         return true;
      }
      if (this.editing != null) {
         if (key == 259 && !this.editing.value.isEmpty()) {
            this.editing.value = this.editing.value.substring(0, this.editing.value.length() - 1);
         } else if (key == 256 || key == 257 || key == 335) {
            this.editing = null;
         } else if (key == 86 && (mods & 2) != 0) {
            String clip = Env.clipboard.get();
            if (clip != null) {
               String v = this.editing.value + clip.replace('\n', ' ');
               this.editing.value = v.substring(0, Math.min(this.editing.max, v.length()));
            }
         }
         return true;
      }
      if (this.searchFocus) {
         if (key == 259 && !this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
         } else if (key == 256 || key == 257 || key == 335) {
            this.searchFocus = false;
         }
         return key != 256;
      }
      if (key == 70 && (mods & 2) != 0) {
         this.searchFocus = true;
         this.sub = null;
         return true;
      }
      if ((key == 257 || key == 335) && !DRAFT.empty()) {
         this.apply();
         return true;
      }
      return false;
   }

   @Override
   public boolean chr(String s) {
      if (this.sub != null && this.sub.chr(s)) {
         return true;
      }
      if (this.editing != null) {
         if (this.editing.value.length() < this.editing.max) {
            this.editing.value = this.editing.value + s;
         }
         return true;
      }
      if (this.searchFocus || (this.binding == null && !s.isBlank() && this.sub == null)) {
         this.searchFocus = true;
         if (this.search.length() < 32) {
            this.search = this.search + s;
            this.gridTarget = 0.0F;
         }
         return true;
      }
      return false;
   }

   @Override
   public void tick() {
      if (this.sub != null) {
         this.sub.tick();
      }
   }

   /** CI: types into the search like a player would. */
   public void ciType(String s) {
      this.chr(s);
   }

   /** CI: opens the tool box of a module. */
   public void ciTools(Module m) {
      this.openTools(m);
   }

   /** CI: presses ANWENDEN. */
   public void ciApply() {
      this.apply();
   }
}
