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
   private String subError;

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
      Gx.translate(0.0F, this.pf(14.0) * (1.0F - t));
      this.bench(x0, y0, w, h);
      int top = this.p(64.0);
      int bottom = this.p(52.0);
      this.bricks(x0, y0, w, top, t);
      int bodyY = y0 + top;
      int bodyH = h - top - bottom;
      if (this.sub != null) {
         this.sub.embed(this);
         // No clip around the page: the cosmetics page draws the player as a picture-in-picture
         // entity, which must not sit inside a scissor of ours (the old hub had none either).
         try {
            if (this.subError == null) {
               this.sub.drawPart(x0 + this.p(10.0), bodyY, w - this.p(20.0), bodyH);
            }
         } catch (Throwable e) {
            this.subError = e.toString();
            dev.lego.LegoClient.LOG("Werkbank: Seite " + page + ": " + e);
         }
         if (this.subError != null) {
            Gx.icon("warning", x0 + w / 2.0F, bodyY + bodyH / 2.0F - this.p(14.0), this.pf(24.0), Style.danger);
            Gx.textCenter("Diese Seite hatte einen Fehler", x0 + w / 2.0F, bodyY + bodyH / 2.0F + this.p(12.0), this.pf(9.0), 2, TEXT);
            Gx.textCenter(Fonts.ellipsize(this.subError, 1, this.pf(7.0), w - this.p(80.0)), x0 + w / 2.0F, bodyY + bodyH / 2.0F + this.p(28.0), this.pf(7.0), 1, MUTED);
         }
      } else {
         float ins = Ease.to("bench:ins", this.inspect != null ? 1.0F : 0.0F, 14.0F);
         int toolW = Math.round(this.p(270.0) * ins);
         this.wall(x0 + this.p(14.0), bodyY + this.p(6.0), w - this.p(28.0) - toolW, bodyH - this.p(12.0));
         if (toolW > this.p(4.0)) {
            this.toolbox(x0 + w - toolW - this.p(8.0), bodyY + this.p(2.0), toolW, bodyH - this.p(4.0), ins);
         }
      }
      this.planBar(x0, y0 + h - bottom, w, bottom);
      if (this.askClose) {
         this.closeDialog(x0, y0, w, h);
      }
      Gx.pop();
      Gx.popAlpha();
   }

   // Clean dark palette: calm grey surfaces, one accent colour (Style.accent) for what is on.
   static final int PANEL = 0xF5141519;
   static final int CARD = 0xFF1C1E23;
   static final int CARD_HI = 0xFF24272D;
   static final int LINE = 0x14FFFFFF;
   static final int TEXT = 0xFFECEEF2;
   static final int SUB = 0xFF9BA1AB;
   static final int MUTED = 0xFF6B717C;

   /** The panel: one calm dark surface with a thin edge. */
   private void bench(int x, int y, int w, int h) {
      int r = this.p(12.0);
      Gx.shadow(x, y, w, h, r, this.p(24.0), 0.55F);
      Gx.rect(x, y, w, h, r, PANEL);
      Gx.outline(x, y, w, h, r, Math.max(1, this.p(0.7)), LINE);
   }

   /** Top: name, the categories as flat tabs, search and close. */
   private void bricks(int x0, int y0, int w, int top, float t) {
      int logo = this.p(22.0);
      int lx = x0 + this.p(18.0);
      int ly = y0 + (top - logo) / 2;
      Gx.rect(lx, ly, logo, logo, this.p(6.0), Style.accent);
      Gx.icon("brick", lx + logo / 2.0F, ly + logo / 2.0F, this.pf(13.0), Style.accentText);
      Gx.text("LEGO", lx + logo + this.p(8.0), y0 + top / 2.0F - this.p(9.0), this.pf(9.5F), 3, TEXT);
      Gx.text("Werkbank", lx + logo + this.p(8.0), y0 + top / 2.0F + this.p(1.5), this.pf(6.8F), 1, SUB);
      Kit.hit(x0 + this.p(10.0), y0, this.p(100.0), top).click(() -> this.leave(() -> this.host.open(new MenuView())));
      List<Category> cats = categories();
      int bx = x0 + this.p(118.0);
      int bh = this.p(28.0);
      int by = y0 + (top - bh) / 2;
      float fs = this.pf(7.6F);
      for (int i = 0; i < cats.size(); i++) {
         Category c = cats.get(i);
         String label = c.displayTitle();
         String n = String.valueOf(count(c));
         int bw = Math.round(Gx.width(label, fs, 2) + Gx.width(n, this.pf(6.4F), 2)) + this.p(42.0);
         boolean act = this.sub == null && this.search.isBlank() && c == category;
         boolean hv = this.hover(bx, by, bw, bh);
         float f = Ease.to("bench:tab" + i, act ? 1.0F : 0.0F, 16.0F);
         float hf = Ease.to("bench:tabh" + i, hv ? 1.0F : 0.0F, 16.0F);
         if (f > 0.01F) {
            Gx.rect(bx, by, bw, bh, bh / 2, Gx.withAlpha(Style.accent, f));
         } else if (hf > 0.01F) {
            Gx.rect(bx, by, bw, bh, bh / 2, Gx.withAlpha(-1, 0.06F * hf));
         }
         int fg = Gx.mix(Gx.mix(SUB, TEXT, hf), Style.accentText, f);
         Gx.icon(c.icon, bx + this.p(15.0), by + bh / 2.0F, this.pf(10.5), fg);
         float tw = Gx.textMid(label, bx + this.p(26.0), by + bh / 2.0F, fs, 2, fg);
         Gx.textMid(n, bx + this.p(31.0) + tw, by + bh / 2.0F, this.pf(6.4F), 2, Gx.withAlpha(fg, 0.55F));
         Kit.hit(bx, by, bw, bh).click(() -> {
            category = c;
            page = null;
            this.sub = null;
            this.search = "";
            this.inspect = null;
            this.gridTarget = 0.0F;
            this.openCategory(c);
         });
         bx += bw + this.p(4.0);
      }
      // search (right)
      int sw = this.p(140.0);
      int sh = this.p(28.0);
      int sx = x0 + w - sw - this.p(52.0);
      int sy = y0 + (top - sh) / 2;
      boolean shv = this.hover(sx, sy, sw, sh);
      float sf = Ease.to("bench:sf", this.searchFocus ? 1.0F : (shv ? 0.4F : 0.0F), 14.0F);
      Gx.rect(sx, sy, sw, sh, sh / 2, Gx.mix(CARD, CARD_HI, sf));
      if (this.searchFocus) {
         Gx.outline(sx, sy, sw, sh, sh / 2, Math.max(1, this.p(0.8)), Gx.withAlpha(Style.accent, 0.8F));
      }
      Gx.icon("search", sx + this.p(14.0), sy + sh / 2.0F, this.pf(10.0), Gx.mix(MUTED, Style.accent, sf));
      String shown = this.search.isEmpty() ? (this.searchFocus ? "" : Lang.get("ui.search", "Suchen…")) : Fonts.ellipsize(this.search, 2, this.pf(7.6F), sw - this.p(36.0));
      float tw = Gx.textMid(shown, sx + this.p(26.0), sy + sh / 2.0F, this.pf(7.6F), 2, this.search.isEmpty() ? MUTED : TEXT);
      if (this.searchFocus && System.currentTimeMillis() / 500L % 2L == 0L) {
         Gx.fill(Math.round(sx + this.p(27.0) + (this.search.isEmpty() ? 0 : tw)), sy + this.p(8.0), Math.max(1, this.p(0.9)), sh - this.p(16.0), Style.accent);
      }
      Kit.hit(sx, sy, sw, sh).click(() -> {
         this.searchFocus = true;
         this.sub = null;
      }).right(() -> this.search = "");
      // close
      int cs = this.p(28.0);
      int cx = x0 + w - cs - this.p(14.0);
      int cy = y0 + (top - cs) / 2;
      boolean chv = this.hover(cx, cy, cs, cs);
      float cf = Ease.to("bench:close", chv ? 1.0F : 0.0F, 16.0F);
      Gx.rect(cx, cy, cs, cs, cs / 2, Gx.mix(CARD, Style.danger, 0.85F * cf));
      Gx.icon("x", cx + cs / 2.0F, cy + cs / 2.0F, this.pf(11.0), Gx.mix(SUB, -1, cf));
      Kit.hit(cx, cy, cs, cs).click(this::onEscape);
      Gx.fill(x0 + this.p(16.0), y0 + top - 1, w - this.p(32.0), Math.max(1, this.p(0.6)), LINE);
   }

   /** Categories with a menu of their own (Custom TxT) open it instead of the module wall. */
   private void openCategory(Category c) {
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
         Gx.text(mods.size() + " Treffer für \"" + this.search + "\"", x, y - this.p(2.0), this.pf(7.0), 2, SUB);
      }
      Kit.hit(x, y, w, h).scroll(a -> this.gridTarget -= (float)(a * this.p(60.0)));
      if (mods.isEmpty()) {
         Gx.icon("search", x + w / 2.0F, y + h / 2.0F - this.p(12.0), this.pf(26.0), MUTED);
         Gx.textCenter("Nichts gefunden", x + w / 2.0F, y + h / 2.0F + this.p(14.0), this.pf(9.0), 2, SUB);
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
      int yy = y - Math.round(this.pf(2.0) * hf + this.pf(3.0) * snap);
      int r = this.p(10.0);
      Gx.rect(x, yy, w, h, r, Gx.mix(CARD, CARD_HI, Math.max(hf, sel)));
      if (of > 0.01F) {
         Gx.rectV(x, yy, w, Math.round(h * 0.6F), r, Gx.withAlpha(Style.accent, 0.10F * of), 0x00000000);
      }
      int edge = planned ? Gx.withAlpha(Style.accent, 0.95F) : (sel > 0.01F ? Gx.withAlpha(Style.accent, 0.6F * sel) : LINE);
      Gx.outline(x, yy, w, h, r, Math.max(1, this.p(planned ? 1.2 : 0.7)), edge);
      // icon
      String icon = m.icon != null ? m.icon : m.category.icon;
      int ic = this.p(32.0);
      int icx = x + this.p(14.0);
      int icy = yy + this.p(14.0);
      Gx.rect(icx, icy, ic, ic, this.p(9.0), Gx.mix(Gx.withAlpha(-1, 0.06F), Style.accent, of));
      Gx.icon(icon, icx + ic / 2.0F, icy + ic / 2.0F, this.pf(16.0), Gx.mix(SUB, Style.accentText, of));
      // switch (top right)
      if (m.isToggleable()) {
         int tw = this.p(26.0);
         int th = this.p(14.0);
         Kit.toggle("bench:" + m.id, x + w - tw - this.p(12.0), yy + this.p(14.0), tw, th, on, this.P);
      }
      // text
      float nfs = this.pf(9.0F);
      Gx.text(Fonts.ellipsize(m.displayName(), 2, nfs, w - this.p(28.0)), x + this.p(14.0), yy + this.p(56.0), nfs, 2, TEXT);
      List<String> lines = Fonts.wrap(m.displayDesc(), 1, this.pf(6.9F), w - this.p(28.0), 2);
      for (int k = 0; k < lines.size(); k++) {
         Gx.text(lines.get(k), x + this.p(14.0), yy + this.p(72.0) + k * this.p(10.5), this.pf(6.9F), 1, MUTED);
      }
      // footer: planned marker + settings button
      boolean was = m.enabled;
      if (planned) {
         String state = on != was ? (on ? "Wird eingeschaltet" : "Wird ausgeschaltet") : "Geändert";
         Gx.circle(x + this.p(17.0), yy + h - this.p(14.0), this.p(2.5), Style.accent);
         Gx.textMid(state, x + this.p(24.0), yy + h - this.p(14.0), this.pf(6.4F), 2, Style.accent);
      }
      int gs = this.p(20.0);
      int gx = x + w - gs - this.p(9.0);
      int gy = yy + h - gs - this.p(5.0);
      boolean ghv = this.hover(gx, gy, gs, gs);
      if (!m.settings.isEmpty()) {
         if (ghv) {
            Gx.rect(gx, gy, gs, gs, gs / 2, Gx.withAlpha(-1, 0.08F));
         }
         Gx.icon("dots", gx + gs / 2.0F, gy + gs / 2.0F, this.pf(10.0), ghv ? TEXT : MUTED);
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
      Gx.rect(x, y, w, h, this.p(10.0), CARD);
      Gx.outline(x, y, w, h, this.p(10.0), Math.max(1, this.p(0.7)), LINE);
      int pad = this.p(14.0);
      int cx = x + pad;
      int cw = w - pad * 2;
      int cy = y + this.p(18.0);
      Gx.icon(m.icon != null ? m.icon : m.category.icon, cx + this.p(9.0), cy + this.p(9.0), this.pf(15.0), Style.accent);
      Gx.text(Fonts.ellipsize(m.displayName(), 3, this.pf(10.0), cw - this.p(52.0)), cx + this.p(24.0), cy, this.pf(10.0), 3, TEXT);
      Gx.text(m.category.displayTitle(), cx + this.p(24.0), cy + this.p(13.0), this.pf(6.6F), 1, MUTED);
      int xs = this.p(20.0);
      Kit.hit(x + w - xs - this.p(8.0), cy - this.p(2.0), xs, xs).click(() -> this.inspect = null);
      Gx.icon("x", x + w - xs / 2.0F - this.p(8.0), cy + xs / 2.0F - this.p(2.0), this.pf(10.0), SUB);
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
            Gx.rect(cx - this.p(6.0), ry + this.p(3.0), this.p(2.5), this.p(14.0), this.p(1.0), Style.accent);
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
         Gx.text("Keine Einstellungen", cx, ry + this.p(4.0), this.pf(7.4F), 1, MUTED);
         ry += this.p(20.0);
      }
      this.toolMax = Math.max(0, ry - start - listH + this.p(8.0));
      Kit.unclip();
      Gx.unclip();
      Gx.popAlpha();
   }

   private int label(String text, int x, int y, int w, String value) {
      Gx.text(Fonts.ellipsize(text, 2, this.pf(7.8F), w - this.p(60.0)), x, y + this.p(4.0), this.pf(7.8F), 2, TEXT);
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
         Gx.textCenter(Fonts.ellipsize(o, 2, fs, ow - this.p(8.0)), px + ow / 2.0F, py + ph / 2.0F, fs, 2, act ? Style.accentText : SUB);
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
      Gx.rect(x, fy, w, fh, this.p(4.0), CARD_HI);
      Gx.outline(x, fy, w, fh, this.p(4.0), Math.max(1, this.p(0.7)), focus ? Style.accent : LINE);
      String v = tx.value.isEmpty() && !focus ? (tx.hint == null ? "" : tx.hint) : tx.value;
      float tw = Gx.textMid(Fonts.ellipsize(v, 2, this.pf(7.4F), w - this.p(14.0)), x + this.p(7.0), fy + fh / 2.0F, this.pf(7.4F), 2, tx.value.isEmpty() ? MUTED : TEXT);
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
      Gx.textCenter(v, bx + bw / 2.0F, y + this.p(1.0) + bh / 2.0F, this.pf(6.8F), 2, this.binding == m ? Style.accent : SUB);
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
         Gx.rect(bx, yy, bw, bh, bh / 2, CARD_HI);
         Gx.outline(bx, yy, bw, bh, bh / 2, Math.max(1, this.p(0.8)), Gx.withAlpha(Style.accent, 0.5F));
         Gx.icon("list", bx + this.p(16.0), yy + bh / 2.0F, this.pf(11.0), Style.accent);
         Gx.text("Bauplan", bx + this.p(28.0), yy + this.p(6.0), this.pf(7.0F), 2, TEXT);
         Gx.text(changes.size() + (changes.size() == 1 ? " Änderung" : " Änderungen"), bx + this.p(26.0), yy + this.p(18.0), this.pf(6.2F), 1, SUB);
         String applyLabel = "ANWENDEN (" + changes.size() + ")";
         int aw = Math.round(Gx.width(applyLabel, this.pf(8.0F), 3)) + this.p(26.0);
         int ax = bx + bw - aw - this.p(6.0);
         int ah = bh - this.p(8.0);
         int ay = yy + this.p(4.0);
         boolean ahv = this.hover(ax, ay, aw, ah);
         float af = Ease.to("bench:apply", ahv ? 1.0F : 0.0F, 16.0F);
         float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 300.0));
         Gx.glow(ax + aw / 2, ay + ah / 2, this.p(34.0), Gx.withAlpha(Style.accent, 0.08F + 0.06F * pulse));
         Gx.rect(ax, ay, aw, ah, ah / 2, Gx.mix(Style.accent, -1, 0.12F * af));
         Gx.textCenter(applyLabel, ax + aw / 2.0F, ay + ah / 2.0F, this.pf(8.0F), 3, Style.accentText);
         Kit.hit(ax, ay, aw, ah).click(this::apply);
         String dropLabel = "Verwerfen";
         int dw = Math.round(Gx.width(dropLabel, this.pf(7.4F), 2)) + this.p(20.0);
         int dx = ax - dw - this.p(6.0);
         boolean dhv = this.hover(dx, ay, dw, ah);
         Gx.rect(dx, ay, dw, ah, ah / 2, Gx.withAlpha(-1, dhv ? 0.12F : 0.05F));
         Gx.textCenter(dropLabel, dx + dw / 2.0F, ay + ah / 2.0F, this.pf(7.4F), 2, SUB);
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
            Gx.textMid(c.label(), cx + this.p(8.0), cy + ch / 2.0F, fs, 2, TEXT);
            boolean xh = this.hover(cx + cw - this.p(16.0), cy, this.p(16.0), ch);
            Gx.icon("x", cx + cw - this.p(9.0), cy + ch / 2.0F, this.pf(7.0), xh ? TEXT : MUTED);
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
         Gx.rect(bx, y, bw, bh, bh / 2, act ? Style.accent : Gx.withAlpha(-1, 0.04F + 0.07F * f));
         int fg = act ? Style.accentText : Gx.mix(SUB, TEXT, f);
         Gx.icon(it[1], bx + this.p(13.0), y + bh / 2.0F, this.pf(11.0), fg);
         Gx.textMid(it[0], bx + this.p(23.0), y + bh / 2.0F, fs, 3, fg);
         String target = it[2];
         Kit.hit(bx, y, bw, bh).click(() -> this.openPage(target));
         bx += bw + this.p(5.0);
      }
      String hint = "Klick = auswählen · Rechtsklick = Werkzeug · ANWENDEN lädt alles gleichzeitig";
      float hfs = this.pf(6.2F);
      String fit = Fonts.ellipsize(hint, 1, hfs, x + w - bx - this.p(8.0));
      Gx.textRight(fit, x + w - this.p(4.0), y + bh / 2.0F, hfs, 1, MUTED);
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
            this.subError = null;
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
      Gx.rect(dx, dy, dw, dh, this.p(12.0), CARD);
      Gx.outline(dx, dy, dw, dh, this.p(12.0), Math.max(1, this.p(0.7)), LINE);
      int n = DRAFT.count();
      Gx.textCenter(n + (n == 1 ? " Änderung" : " Änderungen") + " noch nicht angewendet", dx + dw / 2.0F, dy + this.p(30.0), this.pf(9.0), 3, TEXT);
      Gx.textCenter("Jetzt alles gleichzeitig laden?", dx + dw / 2.0F, dy + this.p(48.0), this.pf(7.4F), 1, SUB);
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
