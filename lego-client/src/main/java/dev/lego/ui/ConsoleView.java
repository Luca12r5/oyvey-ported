package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

/** In-game console in the client's Nocturne look: timestamps, coloured levels, warning/error filters, copy. */
public final class ConsoleView extends View {
   private static final String[] FILTERS = new String[]{"Alle", "Warnungen", "Fehler"};
   private static int filter = 0;
   private float scroll = 0.0F;
   private float scrollTarget = Float.MAX_VALUE;
   private float maxScroll = 0.0F;

   @Override
   public float designW() {
      return 760.0F;
   }

   @Override
   public float designH() {
      return 440.0F;
   }

   private static boolean shown(ConsoleLog.Entry e) {
      if (filter == 0) {
         return true;
      } else {
         boolean err = "ERROR".equals(e.level()) || "FATAL".equals(e.level());
         return filter == 2 ? err : err || "WARN".equals(e.level());
      }
   }

   private static int levelColor(String level) {
      switch (level) {
         case "ERROR":
         case "FATAL":
            return Style.danger;
         case "WARN":
            return 0xFFFBBF24;
         case "DEBUG":
         case "TRACE":
            return Style.muted;
         default:
            return Style.sub;
      }
   }

   @Override
   public void draw() {
      float o = this.open;
      Gx.gradientV(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * o), Gx.rgba(0, 0.6F * o));
      int pw = Math.min(this.W - this.p(40.0), this.p(760.0));
      int ph = Math.min(this.H - this.p(40.0), this.p(440.0));
      int x = (this.W - pw) / 2;
      int y = (this.H - ph) / 2;
      int r = this.p(16.0);
      float a = Ease.outCubic(o);
      Gx.pushAlpha(a);
      Gx.push();
      Gx.translate(0.0F, this.pf(16.0) * (1.0F - a));
      Gx.shadow(x, y, pw, ph, r, this.p(18.0), 0.5F);
      Gx.rectV(x, y, pw, ph, r, Style.bg2, Style.bg);
      Gx.outline(x, y, pw, ph, r, Math.max(1, this.p(0.8)), Gx.withAlpha(Style.accent, 0.45F));
      Gx.glow(x + this.p(30.0), y + this.p(24.0), this.p(60.0), Gx.withAlpha(Style.accent, 0.18F));
      Gx.icon("keyboard", x + this.pf(28.0), y + this.pf(26.0), this.pf(16.0), Style.accent);
      Gx.textMid("Konsole", x + this.p(44.0), y + this.pf(26.0), this.pf(12.0), 3, Style.text);
      List<ConsoleLog.Entry> all = ConsoleLog.snapshot();
      int warns = 0;
      int errs = 0;

      for (ConsoleLog.Entry e : all) {
         if ("WARN".equals(e.level())) {
            warns++;
         } else if ("ERROR".equals(e.level()) || "FATAL".equals(e.level())) {
            errs++;
         }
      }

      int bx = x + this.p(140.0);
      int by = y + this.p(14.0);
      int bh = this.p(24.0);

      for (int i = 0; i < FILTERS.length; i++) {
         String label = FILTERS[i] + (i == 1 ? " " + warns : i == 2 ? " " + errs : "");
         int bw = Math.round(Gx.width(label, this.pf(8.0), 3)) + this.p(22.0);
         boolean on = filter == i;
         boolean hot = this.hover(bx, by, bw, bh);
         Gx.rect(bx, by, bw, bh, bh / 2, on ? Style.accent : Gx.mix(Style.surface, Style.surfaceHover, hot ? 1.0F : 0.0F));
         Gx.textCenter(label, bx + bw / 2.0F, by + bh / 2.0F, this.pf(8.0), 3, on ? Style.accentText : Style.text);
         int idx = i;
         Kit.hit(bx, by, bw, bh).click(() -> {
            filter = idx;
            this.scrollTarget = Float.MAX_VALUE;
         });
         bx += bw + this.p(6.0);
      }

      int cw = this.p(78.0);
      int cx = x + pw - this.p(16.0) - cw;
      boolean copyHot = this.hover(cx, by, cw, bh);
      Gx.rect(cx, by, cw, bh, this.p(8.0), Gx.mix(Style.surface, Style.surfaceHover, copyHot ? 1.0F : 0.0F));
      Gx.textCenter("Kopieren", cx + cw / 2.0F, by + bh / 2.0F, this.pf(8.0), 3, Style.text);
      Kit.hit(cx, by, cw, bh).click(() -> {
         StringBuilder sb = new StringBuilder();

         for (ConsoleLog.Entry e : ConsoleLog.snapshot()) {
            sb.append('[').append(e.time()).append("] [").append(e.source()).append('/').append(e.level()).append("] ").append(e.text()).append('\n');
         }

         try {
            Minecraft.getInstance().keyboardHandler.setClipboard(sb.toString());
            Toasts.show("list", "Konsole kopiert", ConsoleLog.snapshot().size() + " Zeilen in der Zwischenablage");
         } catch (Throwable var3) {
         }
      });
      int lx = x + this.p(14.0);
      int ly = y + this.p(52.0);
      int lw = pw - this.p(28.0);
      int lh = ph - this.p(66.0);
      Gx.rect(lx, ly, lw, lh, this.p(10.0), Gx.rgba(0, 0.45F));
      List<ConsoleLog.Entry> list = new ArrayList<>();

      for (ConsoleLog.Entry e : all) {
         if (shown(e)) {
            list.add(e);
         }
      }

      int line = this.p(13.0);
      float size = this.pf(7.4F);
      this.maxScroll = Math.max(0, list.size() * line - lh + this.p(12.0));
      this.scrollTarget = Math.max(0.0F, Math.min(this.maxScroll, this.scrollTarget));
      this.scroll = this.scroll + (this.scrollTarget - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 18.0F));
      Gx.clip(lx, ly, lw, lh);
      Kit.clip(lx, ly, lw, lh);
      Kit.hit(lx, ly, lw, lh).scroll(d -> this.scrollTarget = this.scrollTarget - d.floatValue() * this.p(39.0));
      int first = Math.max(0, (int)(this.scroll / line) - 1);
      int tx = lx + this.p(10.0);
      int lvx = tx + this.p(52.0);
      int srcx = lvx + this.p(46.0);
      int msgx = srcx + this.p(96.0);

      for (int i = first; i < list.size(); i++) {
         int yy = Math.round(ly + this.p(6.0) + i * line - this.scroll);
         if (yy > ly + lh) {
            break;
         }

         ConsoleLog.Entry e = list.get(i);
         int col = levelColor(e.level());
         boolean err = col == Style.danger;
         if (err) {
            Gx.fill(lx + this.p(2.0), yy, lw - this.p(4.0), line, Gx.withAlpha(Style.danger, 0.1F));
            Gx.fill(lx + this.p(2.0), yy, Math.max(1, this.p(2.0)), line, Style.danger);
         }

         float mid = yy + line / 2.0F;
         Gx.textMid(e.time(), tx, mid, size, 1, Style.muted);
         Gx.textMid(e.level(), lvx, mid, size, 3, col);
         Gx.textMid(Fonts.ellipsize(e.source(), 1, size, this.p(90.0)), srcx, mid, size, 1, Style.muted);
         Gx.textMid(Fonts.ellipsize(e.text(), 1, size, lx + lw - msgx - this.p(10.0)), msgx, mid, size, 1, err ? Gx.mix(Style.text, Style.danger, 0.4F) : Style.text);
      }

      if (list.isEmpty()) {
         Gx.textCenter(filter == 0 ? "Noch keine Meldungen" : "Keine passenden Meldungen", lx + lw / 2.0F, ly + lh / 2.0F, this.pf(9.0), 2, Style.sub);
      }

      Kit.unclip();
      Gx.unclip();
      Gx.textRight("F10 = Konsole   •   Esc = schließen", x + pw - this.p(16.0), y + ph - this.pf(7.0), this.pf(6.8F), 1, Style.muted);
      Gx.pop();
      Gx.popAlpha();
   }
}
