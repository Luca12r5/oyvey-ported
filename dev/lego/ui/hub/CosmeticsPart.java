package dev.lego.ui.hub;

import dev.lego.cosmetic.Cos;
import dev.lego.cosmetic.Raster;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Thumbs;
import dev.lego.ui.UiSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CosmeticsPart implements HubView.Part {
   private static Cos.Slot filter = null;
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;
   private float yaw = 200.0F;
   private float yawVel = 0.0F;
   private boolean dragging = false;
   private float lastMx;
   private Cos.Item selected;
   private long changedAt = System.currentTimeMillis();

   @Override
   public void shown() {
      Cos.init();
   }

   private List<Cos.Item> items() {
      return filter == null ? Cos.ALL : Cos.of(filter);
   }

   @Override
   public void draw(HubView v, int x, int y, int w, int h) {
      int pad = v.p(18.0);
      int catW = v.p(128.0);
      int cx0 = x + pad;
      int cy0 = y + v.p(18.0);
      Gx.text("Kosmetik", cx0 + v.p(4.0), cy0, v.pf(15.0), 3, Style.text);
      Gx.text(Cos.equippedCount() + " ausgerüstet", cx0 + v.p(4.0), cy0 + v.pf(21.0), v.pf(7.6F), 1, Style.sub);
      int ly = cy0 + v.p(40.0);
      List<Cos.Slot> cats = new ArrayList<>();
      cats.add(null);

      for (Cos.Slot s : Cos.Slot.values()) {
         cats.add(s);
      }

      int rowH = v.p(29.0);
      int sel = cats.indexOf(filter);
      float ay = Ease.to("cos:cat", ly + sel * rowH, 18.0F, ly + sel * rowH);
      Gx.rect(cx0, Math.round(ay), catW, rowH - v.p(3.0), v.p(8.0), Gx.withAlpha(Style.accent, 0.16F));

      for (int i = 0; i < cats.size(); i++) {
         Cos.Slot s = cats.get(i);
         int ry = ly + i * rowH;
         boolean hv = v.hover(cx0, ry, catW, rowH - v.p(3.0));
         boolean act = s == filter;
         float hvv = Ease.to("cos:ch" + i, hv ? 1.0F : 0.0F, 14.0F);
         if (!act && hvv > 0.01F) {
            Gx.rect(cx0, ry, catW, rowH - v.p(3.0), v.p(8.0), Gx.withAlpha(Style.surfaceHover, hvv));
         }

         int col = act ? Style.accent : Gx.mix(Style.sub, Style.text, hvv);
         Gx.icon(s == null ? "grid" : s.icon, cx0 + v.p(15.0), ry + (rowH - v.p(3.0)) / 2.0F, v.pf(12.0), col);
         String label = s == null ? "Alle" : s.title;
         Gx.textMid(label, cx0 + v.p(28.0), ry + (rowH - v.p(3.0)) / 2.0F, v.pf(8.4F), act ? 3 : 2, act ? Style.text : col);
         int cnt = s == null ? Cos.ALL.size() : Cos.of(s).size();
         Cos.Item eq = s == null ? null : Cos.equipped(s);
         if (eq != null) {
            Gx.circle(cx0 + catW - v.p(26.0), ry + (rowH - v.p(3.0)) / 2, v.p(2.6), Style.ok);
         }

         Gx.textRight(String.valueOf(cnt), cx0 + catW - v.p(10.0), ry + (rowH - v.p(3.0)) / 2.0F, v.pf(7.2F), 2, Style.muted);
         Kit.hit(cx0, ry, catW, rowH - v.p(3.0)).click(() -> {
            filter = s;
            this.target = this.scroll = 0.0F;
            this.changedAt = System.currentTimeMillis();
         });
      }

      int by = y + h - v.p(46.0);
      int bh = v.p(28.0);
      boolean hb = v.hover(cx0, by, catW, bh);
      Kit.button("cos:none", "Alles ablegen", "trash", cx0, by, catW, bh, v.P, false, hb);
      Kit.hit(cx0, by, catW, bh).click(Cos::unequipAll);
      int pw = v.p(172.0);
      int px = x + w - pad - pw;
      int py = y + v.p(18.0);
      int ph = h - v.p(36.0);
      Gx.rectV(px, py, pw, ph, v.p(14.0), Gx.mix(Style.surface, Style.accent, 0.08F), Style.surface);
      Gx.outline(px, py, pw, ph, v.p(14.0), Math.max(1, v.p(0.6)), Style.stroke);
      Gx.glow(px + pw / 2, py + ph / 2 - v.p(30.0), Math.round(pw * 0.6F), Gx.withAlpha(Style.accent, 0.16F));
      Gx.circle(px + pw / 2, py + ph - v.p(92.0), v.p(44.0), Gx.withAlpha(-16777216, 0.25F));
      if (!this.dragging) {
         this.yawVel = this.yawVel * (float)Math.exp(-Gx.dt * 3.0F);
         this.yaw = this.yaw + (this.yawVel + 12.0F) * Gx.dt;
      }

      int areaY = py + v.p(34.0);
      int areaH = ph - v.p(120.0);
      boolean drawn = Env.playerPreview.draw(px, areaY, pw, areaH, this.yaw, 0.0F);
      if (!drawn) {
         Cos.Item it = this.selected != null ? this.selected : (Cos.ALL.isEmpty() ? null : Cos.ALL.get(0));
         if (it != null) {
            int sx = Math.min(pw, areaH);
            float fy = Math.round(this.yaw / 15.0F) * 15.0F;
            Gx.Img img = Thumbs.get("cosbig:" + it.id + ":" + (int)fy, sx, sx, () -> Raster.render(it, s, s, fy, 10.0F, 1.2F, true, 0));
            if (img != null) {
               Gx.image(img, px + (pw - sx) / 2, areaY + (areaH - sx) / 2, sx, sx, -1);
            }
         }
      }

      Kit.hit(px, areaY, pw, areaH).drag((mx, my) -> {
         if (!this.dragging) {
            this.dragging = true;
            this.lastMx = mx;
         }

         float d = mx - this.lastMx;
         this.yaw = this.yaw + d / v.P * 1.6F;
         this.yawVel = d / v.P * 30.0F;
         this.lastMx = mx;
      });
      Gx.textCenter("VORSCHAU", px + pw / 2.0F, py + v.p(16.0), v.pf(6.8F), 3, Style.muted);
      int ey = py + ph - v.p(80.0);
      Gx.text("Ausgerüstet", px + v.p(12.0), ey, v.pf(8.0), 3, Style.text);
      int chipY = ey + v.p(16.0);
      int chipX = px + v.p(12.0);
      int n = 0;

      for (Cos.Slot sx : Cos.Slot.values()) {
         Cos.Item it = Cos.equipped(sx);
         if (it != null) {
            if (n >= 6) {
               break;
            }

            float fs = v.pf(6.8F);
            String t = Fonts.ellipsize(it.name, 2, fs, pw - v.p(40.0));
            int cw = Math.round(Gx.width(t, fs, 2)) + v.p(22.0);
            int chh = v.p(16.0);
            if (chipX + cw > px + pw - v.p(10.0)) {
               chipX = px + v.p(12.0);
               chipY += chh + v.p(4.0);
            }

            if (chipY + chh > py + ph - v.p(6.0)) {
               break;
            }

            Gx.rect(chipX, chipY, cw, chh, chh / 2, Gx.withAlpha(it.rarity.color, 0.2F));
            Gx.circle(chipX + v.p(8.0), chipY + chh / 2, v.p(2.4), it.rarity.color);
            Gx.textMid(t, chipX + v.p(14.0), chipY + chh / 2.0F, fs, 2, Style.text);
            chipX += cw + v.p(4.0);
            n++;
         }
      }

      if (n == 0) {
         Gx.text("Noch nichts – klick auf ein Item!", px + v.p(12.0), chipY, v.pf(7.0), 1, Style.muted);
      }

      int gx = cx0 + catW + v.p(16.0);
      int gw = px - v.p(14.0) - gx;
      int gy = y + v.p(18.0);
      int gh = h - v.p(36.0);
      List<Cos.Item> list = this.items();
      int cols = 2;
      int gap = v.p(10.0);
      int cwx = (gw - gap) / cols;
      int ch = v.p(150.0);
      int rows = (list.size() + cols - 1) / cols;
      this.max = Math.max(0, rows * (ch + gap) - gap - gh);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(gx - v.p(4.0), gy, gw + v.p(8.0), gh);
      Kit.clip(gx - v.p(4.0), gy, gw + v.p(8.0), gh);
      Kit.hit(gx, gy, gw, gh).scroll(ax -> this.target = this.target - ax.floatValue() * v.p(60.0));
      float appear = Math.min(1.0F, (float)(System.currentTimeMillis() - this.changedAt) * UiSettings.anim() / 600.0F);

      for (int i = 0; i < list.size(); i++) {
         int cx = gx + i % cols * (cwx + gap);
         int cy = Math.round(gy + i / cols * (ch + gap) - this.scroll);
         if (cy + ch >= gy && cy <= gy + gh) {
            float a = Ease.outCubic(Ease.clamp((appear * 600.0F - Math.min(i, 10) * 25) / 260.0F));
            Gx.pushAlpha(a);
            Gx.push();
            Gx.translate(0.0F, v.pf(10.0) * (1.0F - a));
            this.card(v, list.get(i), cx, cy, cwx, ch);
            Gx.pop();
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (this.max > 0.0F) {
         int trackH = gh - v.p(8.0);
         int thumbH = Math.max(v.p(24.0), Math.round(trackH * gh / (gh + this.max)));
         int thumbY = gy + v.p(4.0) + Math.round((trackH - thumbH) * (this.scroll / this.max));
         Gx.rect(gx + gw + v.p(3.0), thumbY, v.p(3.0), thumbH, v.p(2.0), Style.strokeHi);
      }
   }

   private void card(HubView v, Cos.Item it, int x, int y, int w, int h) {
      boolean modern = UiSettings.guiStyle == 1;
      boolean hv = v.hover(x, y, w, h);
      boolean eq = Cos.isEquipped(it);
      float hov = Ease.to("cos:" + it.id, hv ? 1.0F : 0.0F, 14.0F);
      float on = Ease.to("coson:" + it.id, eq ? 1.0F : 0.0F, 12.0F);
      int r = v.p(modern ? 16.0 : 12.0);
      if (!modern && hov > 0.01F) {
         Gx.shadow(x, y, w, h, r, v.p(6.0), 0.3F * hov);
      }

      if (modern) {
         Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * 0.5F));
         Gx.outline(x, y, w, h, r, Math.max(1, v.p(0.6)), Gx.mix(Style.stroke, Style.strokeHi, hov));
         if (eq) {
            Gx.rect(x, y + v.p(8.0), Math.max(2, v.p(3.0)), h - v.p(16.0), v.p(2.0), Style.ok);
         }
      } else {
         Gx.rectV(x, y, w, h, r, Gx.mix(Style.surface, Gx.mix(Style.surface, it.rarity.color, 0.18F), 0.6F + 0.4F * hov), Style.surface);
         Gx.outline(
            x, y, w, h, r, Math.max(1, v.p(on > 0.5F ? 1.3 : 0.7)), Gx.mix(Gx.mix(Style.stroke, Gx.withAlpha(it.rarity.color, 0.6F), hov), Style.ok, on)
         );
      }

      int ts = h - v.p(46.0);
      int tx = x + (w - ts) / 2;
      int ty = y + v.p(6.0);
      if (!modern) {
         Gx.glow(x + w / 2, ty + ts / 2, Math.round(ts * 0.5F), Gx.withAlpha(it.rarity.color, 0.18F + 0.12F * hov));
      }

      boolean back = it.slot == Cos.Slot.WINGS || it.slot == Cos.Slot.CAPE || it.slot == Cos.Slot.BACK;
      Gx.Img img = Thumbs.get("cos:" + it.id, ts, ts, () -> Raster.render(it, ts, ts, back ? 155.0F : 28.0F, 12.0F, 1.25F, true, 0));
      if (!modern) {
         Gx.push();
         Gx.scaleAt(x + w / 2.0F, ty + ts / 2.0F, 1.0F + 0.05F * hov);
      }

      if (img != null) {
         Gx.image(img, tx, ty, ts, ts, -1);
      } else {
         EmotesPart.shimmer(v, tx + ts / 4, ty + ts / 4, ts / 2, ts / 2);
      }

      if (!modern) {
         Gx.pop();
      }

      float bfs = v.pf(5.8F);
      String rt = it.rarity.title.toUpperCase(Locale.ROOT);
      int bw = Math.round(Gx.spacedWidth(rt, bfs, 3, v.pf(0.6F))) + v.p(12.0);
      int bh = v.p(13.0);
      Gx.rect(x + v.p(7.0), y + v.p(7.0), bw, bh, bh / 2, Gx.withAlpha(it.rarity.color, modern ? 0.14F : 0.22F));
      Gx.textSpaced(rt, x + v.p(7.0) + bw / 2.0F, y + v.p(7.0) + bh / 2.0F, bfs, 3, it.rarity.color, v.pf(0.6F));
      Gx.textCenter(Fonts.ellipsize(it.name, 3, v.pf(8.8F), w - v.p(14.0)), x + w / 2.0F, y + h - v.p(33.0), v.pf(8.8F), 3, Style.text);
      int bx = x + v.p(10.0);
      int bby = y + h - v.p(24.0);
      int bbw = w - v.p(20.0);
      int bbh = v.p(18.0);
      boolean hb = v.hover(bx, bby, bbw, bbh);
      int bg = eq
         ? Gx.mix(Gx.withAlpha(Style.ok, 0.22F), Gx.withAlpha(Style.danger, 0.25F), hb ? 1.0F : 0.0F)
         : Gx.mix(Style.surface2, Style.accent, hb ? 1.0F : 0.0F);
      Gx.rect(bx, bby, bbw, bbh, bbh / 2, bg);
      String lbl = eq ? (hb ? "ABLEGEN" : "AUSGERÜSTET") : "AUSRÜSTEN";
      int fg = eq ? (hb ? Style.danger : Style.ok) : (hb ? Style.accentText : Style.sub);
      if (eq && !hb) {
         Gx.icon("check", bx + bbw / 2.0F - Gx.spacedWidth(lbl, v.pf(6.4F), 3, v.pf(0.8F)) / 2.0F - v.pf(7.0), bby + bbh / 2.0F, v.pf(8.0), fg);
      }

      Gx.textSpaced(lbl, bx + bbw / 2.0F + (eq && !hb ? v.pf(4.0) : 0.0F), bby + bbh / 2.0F, v.pf(6.4F), 3, fg, v.pf(0.8F));
      Kit.hit(x, y, w, h).click(() -> {
         Cos.toggle(it);
         this.selected = it;
         Sound.pop();
      });
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == -1 || key < 0) {
         this.dragging = false;
      }

      return false;
   }

   @Override
   public void tick() {
      if (!Kit.dragging()) {
         this.dragging = false;
      }
   }
}
