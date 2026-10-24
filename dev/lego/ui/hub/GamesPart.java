package dev.lego.ui.hub;

import dev.lego.games.Arcade;
import dev.lego.games.DuelView;
import dev.lego.games.Duels;
import dev.lego.games.GameInfo;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GamesPart implements HubView.Part {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;
   private String filter = "";
   private boolean filterFocus = false;

   @Override
   public void shown() {
      Arcade.registerAll();
   }

   @Override
   public boolean key(int key, int mods) {
      if (this.filterFocus) {
         if (key == 259 && !this.filter.isEmpty()) {
            this.filter = this.filter.substring(0, this.filter.length() - 1);
            this.scroll = this.target = 0.0F;
            return true;
         }

         if (key == 256) {
            if (!this.filter.isEmpty()) {
               this.filter = "";
               this.scroll = this.target = 0.0F;
            } else {
               this.filterFocus = false;
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public boolean chr(String s) {
      if (this.filterFocus && s != null && !s.isEmpty()) {
         this.filter = this.filter + s;
         this.scroll = this.target = 0.0F;
         return true;
      } else {
         return false;
      }
   }

   private List<GameInfo> filtered() {
      if (this.filter.isEmpty()) {
         return GameInfo.ALL;
      } else {
         String q = this.filter.toLowerCase(Locale.ROOT);
         List<GameInfo> out = new ArrayList<>();

         for (GameInfo g : GameInfo.ALL) {
            if (g.name.toLowerCase(Locale.ROOT).contains(q)) {
               out.add(g);
            }
         }

         return out;
      }
   }

   @Override
   public void draw(HubView v, int x, int y, int w, int h) {
      int pad = v.p(22.0);
      int top = v.header(x + pad, y + v.p(20.0), "Minispiele", GameInfo.ALL.size() + " Spiele für Zwischendurch – mit Rekorden");
      int dbw = v.p(120.0);
      int dbh = v.p(30.0);
      int dbx = x + w - pad - dbw;
      int dby = y + v.p(22.0);
      int inv = Duels.invites().size();
      boolean dh = v.hover(dbx, dby, dbw, dbh);
      Kit.button("duels", inv > 0 ? "Duelle (" + inv + ")" : "Duelle", "users", dbx, dby, dbw, dbh, v.P, true, dh);
      if (inv > 0) {
         float pulse = 0.6F + 0.4F * (float)Math.sin(System.currentTimeMillis() / 220.0);
         Gx.circle(dbx + dbw - v.p(3.0), dby + v.p(3.0), v.p(5.0), Gx.withAlpha(-15043, pulse));
      }

      Kit.hit(dbx, dby, dbw, dbh).click(() -> v.host.open(new DuelView()));
      int sbh = v.p(28.0);
      int sbw = w - pad * 2;
      int sby = top + v.p(2.0);
      v.searchBox("games", x + pad, sby, sbw, sbh, this.filter, this.filterFocus);
      Kit.hit(x + pad, sby, sbw, sbh).click(() -> this.filterFocus = true);
      if (!this.filter.isEmpty()) {
         Kit.hit(x + pad + sbw - sbh, sby, sbh, sbh).click(() -> {
            this.filter = "";
            this.scroll = this.target = 0.0F;
         });
      }

      List<GameInfo> games = this.filtered();
      int ly = sby + sbh + v.p(8.0);
      int lh = y + h - v.p(16.0) - ly;
      int cols = 3;
      int gap = v.p(12.0);
      int cw = (w - pad * 2 - gap * (cols - 1)) / cols;
      int ch = v.p(132.0);
      int n = games.size();
      int rows = (n + cols - 1) / cols;
      this.max = Math.max(0, rows * (ch + gap) - gap - lh + v.p(4.0));
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(x + pad - v.p(6.0), ly, w - pad * 2 + v.p(12.0), lh);
      Kit.clip(x + pad - v.p(6.0), ly, w - pad * 2 + v.p(12.0), lh);
      Kit.hit(x, ly, w, lh).scroll(ax -> this.target = this.target - ax.floatValue() * v.p(60.0));
      float appear = v.sectionT(650.0F);

      for (int i = 0; i < n; i++) {
         int cx = x + pad + i % cols * (cw + gap);
         int cy = Math.round(ly + v.p(2.0) + i / cols * (ch + gap) - this.scroll);
         if (cy + ch >= ly && cy <= ly + lh) {
            float a = Ease.outCubic(Ease.clamp((appear * 650.0F - Math.min(i, 10) * 30) / 260.0F));
            Gx.pushAlpha(a);
            Gx.push();
            Gx.translate(0.0F, v.pf(10.0) * (1.0F - a));
            this.card(v, games.get(i), cx, cy, cw, ch);
            Gx.pop();
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (this.max > 0.0F) {
         int trackH = lh - v.p(8.0);
         int thumbH = Math.max(v.p(24.0), Math.round(trackH * lh / (lh + this.max)));
         int thumbY = ly + v.p(4.0) + Math.round((trackH - thumbH) * (this.scroll / this.max));
         Gx.rect(x + w - v.p(10.0), thumbY, v.p(3.0), thumbH, v.p(2.0), Style.strokeHi);
      }
   }

   private void card(HubView v, GameInfo g, int x, int y, int w, int h) {
      boolean modern = UiSettings.guiStyle == 1;
      boolean hv = v.hover(x, y, w, h);
      float hov = Ease.to("game:" + g.id, hv ? 1.0F : 0.0F, 14.0F);
      int r = v.p(modern ? 16.0 : 13.0);
      if (!modern && hov > 0.01F) {
         Gx.shadow(x, y, w, h, r, v.p(8.0), 0.35F * hov);
      }

      Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * (modern ? 0.5F : 1.0F)));
      int ah = v.p(64.0);
      if (modern) {
         Gx.rect(x, y, w, ah, r, Gx.mix(g.color, Style.surface, 0.7F));
         Gx.icon(g.icon, x + w / 2.0F, y + ah / 2.0F, v.pf(28.0), Gx.mix(g.color, Style.text, 0.5F));
      } else {
         Gx.rectV(x, y, w, ah + r, r, Gx.mix(g.color, -16777216, 0.35F), Gx.mix(g.color, -16777216, 0.75F));
         Gx.fill(x, y + ah, w, r, Gx.mix(Style.surface, Style.surfaceHover, hov));

         for (int i = 0; i < 5; i++) {
            float bxp = x + w * (0.08F + i * 0.21F);
            float byp = y + v.pf(8.0) + i % 2 * v.pf(22.0);
            Gx.rect(Math.round(bxp), Math.round(byp), v.p(18.0), v.p(9.0), v.p(2.0), Gx.withAlpha(-1, 0.05F + 0.02F * (i % 3)));
         }

         Gx.glow(x + w / 2, y + ah / 2, Math.round(ah * 0.9F), Gx.withAlpha(g.color, 0.35F + 0.2F * hov));
         Gx.push();
         Gx.scaleAt(x + w / 2.0F, y + ah / 2.0F, 1.0F + 0.12F * hov);
         Gx.icon(g.icon, x + w / 2.0F, y + ah / 2.0F, v.pf(28.0), -1);
         Gx.pop();
      }

      Gx.outline(
         x,
         y,
         w,
         h,
         r,
         Math.max(1, v.p(modern ? 0.6 : 0.7)),
         modern ? Gx.mix(Style.stroke, Style.strokeHi, hov) : Gx.mix(Style.stroke, Gx.withAlpha(g.color, 0.8F), hov)
      );
      Gx.text(Fonts.ellipsize(g.name, 3, v.pf(10.0), w - v.p(24.0)), x + v.p(12.0), y + ah + v.pf(8.0), v.pf(10.0), 3, Style.text);
      Gx.text(Fonts.ellipsize(g.tagline, 1, v.pf(7.4F), w - v.p(24.0)), x + v.p(12.0), y + ah + v.pf(24.0), v.pf(7.4F), 1, Style.sub);
      String best = GameInfo.hasBest(g.id) ? "Rekord: " + format(g, GameInfo.best(g.id)) : "Noch nicht gespielt";
      Gx.icon("trophy", x + v.p(17.0), y + h - v.pf(14.0), v.pf(9.0), GameInfo.hasBest(g.id) ? -15043 : Style.muted);
      Gx.textMid(best, x + v.p(26.0), y + h - v.pf(14.0), v.pf(7.2F), 2, Style.sub);
      int pbs = v.p(24.0);
      int pbx = x + w - pbs - v.p(10.0);
      int pby = y + h - pbs - v.p(8.0);
      Gx.circle(
         pbx + pbs / 2,
         pby + pbs / 2,
         pbs / 2,
         modern ? Gx.mix(Style.surface2, Style.accent, 0.2F + 0.6F * hov) : Gx.mix(Style.surface2, g.color, 0.35F + 0.65F * hov)
      );
      Gx.icon("play", pbx + pbs / 2.0F + v.pf(1.0), pby + pbs / 2.0F, v.pf(10.0), hov > 0.5F ? -15724528 : Style.text);
      Kit.hit(x, y, w, h).click(() -> v.host.open(g.factory.get()));
   }

   static String format(GameInfo g, long v) {
      if (!g.higherIsBetter) {
         return v + " ms";
      } else {
         return g.id.equals("cps") ? String.format(Locale.ROOT, "%.1f CPS", v / 10.0) : String.valueOf(v);
      }
   }
}
