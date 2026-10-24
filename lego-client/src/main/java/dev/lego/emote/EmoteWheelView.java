package dev.lego.emote;

import dev.lego.cosmetic.Raster;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Thumbs;
import dev.lego.ui.View;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Point2D.Float;
import java.util.Locale;

public final class EmoteWheelView extends View {
   private final int holdKey;
   private int sel = -1;
   private int lastSel = -1;
   private boolean played;

   public EmoteWheelView(int holdKey) {
      this.holdKey = holdKey;
   }

   @Override
   public boolean blur() {
      return false;
   }

   @Override
   public boolean backdrop() {
      return false;
   }

   @Override
   public float openMs() {
      return 220.0F;
   }

   @Override
   public float closeMs() {
      return 140.0F;
   }

   private int count() {
      return EmoteLib.WHEEL.size();
   }

   @Override
   public void draw() {
      Style.update();
      float o = Ease.outCubic(this.open);
      int R = this.p(118.0);
      int r0 = Math.round(R * 0.42F);
      int cx = this.W / 2;
      int cy = this.H / 2;
      Gx.pushAlpha(o);
      Gx.Img dim = Gx.painted(
         "wheel:dim",
         256,
         256,
         g -> {
            g.setPaint(
               new RadialGradientPaint(new Float(128.0F, 128.0F), 181.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(0, 0, 0, 150), new Color(0, 0, 0, 60)})
            );
            g.fillRect(0, 0, 256, 256);
         }
      );
      if (dim != null) {
         Gx.image(dim, 0, 0, this.W, this.H, -1);
      }

      Gx.popAlpha();
      int n = this.count();
      float dx = this.mx - cx;
      float dy = this.my - cy;
      double dist = Math.sqrt(dx * dx + dy * dy);
      this.sel = -1;
      if (n > 0 && dist > r0 * 0.55) {
         double ang = Math.atan2(dy, dx) + (Math.PI / 2) + Math.PI / n;
         ang = (ang % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
         this.sel = Math.min(n - 1, (int)(ang / ((Math.PI * 2) / Math.max(1, n))));
      }

      if (this.sel != this.lastSel && this.sel >= 0) {
         Sound.click();
      }

      this.lastSel = this.sel;
      Gx.push();
      Gx.scaleAt(cx, cy, 0.85F + 0.15F * Ease.outBack(this.open));
      Gx.pushAlpha(o);
      int size = R * 2 + this.p(24.0);
      int pad = this.p(12.0);
      int nn = Math.max(1, n);
      float P0 = this.P;
      int base = Style.surface;
      int accent = Style.accent;
      Gx.shadow(cx - R, cy - R, R * 2, R * 2, R, this.p(18.0), 0.55F);
      Gx.Img wheel = Gx.painted(
         "wheel:base:" + nn + ":" + size + ":" + Integer.toHexString(base) + ":" + Integer.toHexString(Style.stroke),
         size,
         size,
         g -> {
            double c = size / 2.0;
            Area ring = new Area(new Double(c - R, c - R, R * 2, R * 2));
            ring.subtract(new Area(new Double(c - r0, c - r0, r0 * 2, r0 * 2)));
            g.setColor(new Color(base, true));
            g.fill(ring);
            g.setColor(new Color(Style.stroke, true));
            g.setStroke(new BasicStroke(Math.max(1.0F, P0 * 0.8F)));

            for (int i = 0; i < nn; i++) {
               double a = (-Math.PI / 2) + (Math.PI * 2) * (i + 0.5) / nn;
               g.drawLine(
                  (int)(c + Math.cos(a) * (r0 + 2)), (int)(c + Math.sin(a) * (r0 + 2)), (int)(c + Math.cos(a) * (R - 2)), (int)(c + Math.sin(a) * (R - 2))
               );
            }

            g.draw(new Double(c - R + 0.5, c - R + 0.5, R * 2 - 1, R * 2 - 1));
            g.draw(new Double(c - r0, c - r0, r0 * 2, r0 * 2));
         }
      );
      if (wheel != null) {
         Gx.image(wheel, cx - size / 2, cy - size / 2, size, size, -1);
      }

      for (int i = 0; i < n; i++) {
         float h = Ease.to("wheel:h" + i, i == this.sel ? 1.0F : 0.0F, 16.0F);
         int fi = i;
         if (!(h < 0.01F)) {
            Gx.Img wedge = Gx.painted(
               "wheel:w" + nn + ":" + i + ":" + size,
               size,
               size,
               g -> {
                  double c = size / 2.0;
                  double start = 90.0 - 360.0 * (fi - 0.5) / nn;
                  Area a = new Area(
                     new java.awt.geom.Arc2D.Double(c - R - pad * 0.4, c - R - pad * 0.4, (R + pad * 0.4) * 2.0, (R + pad * 0.4) * 2.0, start, -360.0 / nn, 2)
                  );
                  a.subtract(new Area(new Double(c - r0, c - r0, r0 * 2, r0 * 2)));
                  g.setColor(Color.WHITE);
                  g.fill(a);
               }
            );
            if (wedge != null) {
               Gx.image(wedge, cx - size / 2, cy - size / 2, size, size, Gx.withAlpha(accent, 0.28F * h));
            }
         }
      }

      for (int ix = 0; ix < n; ix++) {
         EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(ix));
         if (e != null) {
            float h = Ease.to("wheel:h" + ix, ix == this.sel ? 1.0F : 0.0F, 16.0F);
            double a = (-Math.PI / 2) + (Math.PI * 2) * ix / n;
            float mid = (R + r0) / 2.0F + this.pf(4.0) * h;
            float ixx = cx + (float)Math.cos(a) * mid;
            float iy = cy + (float)Math.sin(a) * mid;
            int ts = Math.round(this.pf(40.0) * (1.0F + 0.15F * h));
            int px = Math.max(16, this.p(44.0));
            Gx.Img th = Thumbs.get("emote:w:" + e.id, px, px, () -> Raster.renderPose(e.pose(Math.min(e.duration * 0.4F, 1.1F)), px, px, 22.0F, 8.0F, 0));
            if (th != null) {
               Gx.image(th, Math.round(ixx - ts / 2.0F), Math.round(iy - ts / 2.0F - this.pf(5.0)), ts, ts, -1);
            } else {
               Gx.icon(e.icon, ixx, iy - this.pf(5.0), this.pf(16.0), Gx.mix(Style.sub, accent, h));
            }

            float fs = this.pf(6.8F);
            Gx.textCenter(Fonts.ellipsize(e.name, 3, fs, (R - r0) * 0.95F), ixx, iy + this.pf(19.0), fs, 3, Gx.mix(Style.sub, Style.text, h));
         }
      }

      Gx.circle(cx, cy, r0 - this.p(4.0), Style.bg);
      if (this.sel >= 0 && this.sel < n) {
         EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(this.sel));
         if (e != null) {
            Gx.glow(cx, cy, r0, Gx.withAlpha(accent, 0.18F));
            Gx.textCenter(e.name, cx, cy - this.pf(4.0), this.pf(10.0), 3, Style.text);
            Gx.textCenter(
               e.loop ? "Loop · bewegen stoppt" : String.format(Locale.ROOT, "%.1f s", e.duration), cx, cy + this.pf(10.0), this.pf(6.6F), 2, Style.sub
            );
         }
      } else if (n == 0) {
         Gx.textCenter("Keine Favoriten", cx, cy - this.pf(4.0), this.pf(8.4F), 3, Style.text);
         Gx.textCenter("Stern im Emote-Menü", cx, cy + this.pf(9.0), this.pf(6.4F), 1, Style.sub);
      } else {
         Gx.icon("emote", cx, cy - this.pf(6.0), this.pf(20.0), Style.sub);
         Gx.textCenter("Emote wählen", cx, cy + this.pf(14.0), this.pf(6.8F), 2, Style.muted);
      }

      Gx.popAlpha();
      Gx.pop();
      Gx.pushAlpha(o);
      String hint = "Maus zeigen · loslassen oder klicken = abspielen · Esc = abbrechen";
      float fs = this.pf(7.2F);
      int hw = Math.round(Gx.width(hint, fs, 2)) + this.p(24.0);
      int hh = this.p(22.0);
      int hx = cx - hw / 2;
      int hy = cy + R + this.p(22.0);
      Gx.rect(hx, hy, hw, hh, hh / 2, Gx.withAlpha(Style.bg, 0.85F));
      Gx.textCenter(hint, cx, hy + hh / 2.0F, fs, 2, Style.sub);
      Gx.popAlpha();
   }

   private void playSelected() {
      if (!this.played && this.host != null) {
         this.played = true;
         if (this.sel >= 0 && this.sel < this.count()) {
            int idx = EmoteLib.index(EmoteLib.WHEEL.get(this.sel));
            this.host.closeThen(() -> Env.playEmote.accept(idx));
         } else {
            this.host.close();
         }
      }
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      if (button == 0) {
         this.playSelected();
      } else if (this.host != null) {
         this.host.close();
      }

      return true;
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == -1 - this.holdKey) {
         this.playSelected();
         return true;
      } else {
         return false;
      }
   }
}
