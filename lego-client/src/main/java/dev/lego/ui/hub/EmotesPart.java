package dev.lego.ui.hub;

import dev.lego.cosmetic.Raster;
import dev.lego.emote.EmoteLib;
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

public final class EmotesPart implements HubView.Part {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;
   private String toast = null;
   private String query = "";
   private boolean focus = false;
   private long toastAt = 0L;

   private List<EmoteLib.Emote> filtered() {
      if (this.query.isBlank()) {
         return EmoteLib.ALL;
      } else {
         String q = this.query.toLowerCase(Locale.ROOT);
         List<EmoteLib.Emote> out = new ArrayList<>();

         for (EmoteLib.Emote e : EmoteLib.ALL) {
            if (e.name.toLowerCase(Locale.ROOT).contains(q) || e.id.toLowerCase(Locale.ROOT).contains(q)) {
               out.add(e);
            }
         }

         return out;
      }
   }

   @Override
   public void draw(HubView v, int x, int y, int w, int h) {
      int pad = v.p(22.0);
      int top = v.header(x + pad, y + v.p(20.0), "Emotes", "Klick = abspielen   •   Stern = ins Emote-Rad (Taste B halten)");
      int wheelW = v.p(176.0);
      int sbw = v.p(200.0);
      int sbh = v.p(28.0);
      int sbx = x + w - pad - sbw;
      int sby = y + v.p(20.0);
      v.searchBox("emotes", sbx, sby, sbw, sbh, this.query, this.focus);
      Kit.hit(sbx, sby, sbw, sbh).click(() -> {
         if (!this.query.isEmpty() && v.mx >= sbx + sbw - sbh) {
            this.query = "";
            this.target = 0.0F;
         } else {
            this.focus = true;
         }
      });
      List<EmoteLib.Emote> list = this.filtered();
      int gx = x + pad;
      int gw = w - pad * 2 - wheelW - v.p(16.0);
      int ly = top + v.p(4.0);
      int lh = y + h - v.p(16.0) - ly;
      int cols = 4;
      int gap = v.p(10.0);
      int cw = (gw - gap * (cols - 1)) / cols;
      int ch = v.p(118.0);
      int n = list.size();
      int rows = (n + cols - 1) / cols;
      this.max = Math.max(0, rows * (ch + gap) - gap - lh + v.p(4.0));
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(gx - v.p(6.0), ly, gw + v.p(12.0), lh);
      Kit.clip(gx - v.p(6.0), ly, gw + v.p(12.0), lh);
      Kit.hit(gx, ly, gw, lh).scroll(a -> this.target = this.target - a.floatValue() * v.p(50.0));
      float appear = v.sectionT(600.0F);

      for (int i = 0; i < n; i++) {
         int cx = gx + i % cols * (cw + gap);
         int cy = Math.round(ly + v.p(2.0) + i / cols * (ch + gap) - this.scroll);
         if (cy + ch >= ly && cy <= ly + lh) {
            float a = Ease.outCubic(Ease.clamp((appear * 600.0F - Math.min(i, 10) * 18) / 250.0F));
            Gx.pushAlpha(a);
            this.card(v, list.get(i), cx, cy, cw, ch);
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (n == 0) {
         Gx.textCenter("Kein Emote gefunden für \"" + this.query + "\"", gx + gw / 2.0F, ly + v.p(40.0), v.pf(9.0), 2, Style.sub);
      }

      if (this.max > 0.0F) {
         int trackH = lh - v.p(8.0);
         int thumbH = Math.max(v.p(24.0), Math.round(trackH * lh / (lh + this.max)));
         int thumbY = ly + v.p(4.0) + Math.round((trackH - thumbH) * (this.scroll / this.max));
         Gx.rect(gx + gw + v.p(4.0), thumbY, v.p(3.0), thumbH, v.p(2.0), Style.strokeHi);
      }

      int wx = x + w - pad - wheelW;
      Gx.rect(wx, ly, wheelW, lh, v.p(14.0), Style.surface);
      Gx.outline(wx, ly, wheelW, lh, v.p(14.0), Math.max(1, v.p(0.6)), Style.stroke);
      Gx.text("Emote-Rad", wx + v.p(14.0), ly + v.p(12.0), v.pf(10.0), 3, Style.text);
      Gx.text(EmoteLib.WHEEL.size() + " / 8 Plätze", wx + v.p(14.0), ly + v.p(29.0), v.pf(7.6F), 1, Style.sub);
      int r = Math.min(wheelW / 2 - v.p(14.0), v.p(72.0));
      int ccx = wx + wheelW / 2;
      int ccy = ly + v.p(52.0) + r;
      Gx.circle(ccx, ccy, r, Style.surface2);
      Gx.circle(ccx, ccy, Math.round(r * 0.36F), Style.bg);

      for (int ix = 0; ix < 8; ix++) {
         double ang = (-Math.PI / 2) + (Math.PI * 2) * ix / 8.0;
         float ixx = ccx + (float)Math.cos(ang) * r * 0.69F;
         float iy = ccy + (float)Math.sin(ang) * r * 0.69F;
         if (ix < EmoteLib.WHEEL.size()) {
            EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(ix));
            boolean hv = v.hover(Math.round(ixx - v.pf(14.0)), Math.round(iy - v.pf(14.0)), v.p(28.0), v.p(28.0));
            Gx.circle(Math.round(ixx), Math.round(iy), v.p(13.0), hv ? Gx.withAlpha(Style.danger, 0.25F) : Gx.withAlpha(Style.accent, 0.18F));
            Gx.icon(hv ? "x" : e.icon, ixx, iy, v.pf(12.0), hv ? Style.danger : Style.accent);
            String id = e.id;
            Kit.hit(Math.round(ixx - v.pf(14.0)), Math.round(iy - v.pf(14.0)), v.p(28.0), v.p(28.0)).click(() -> EmoteLib.toggleWheel(id));
         } else {
            Gx.ringAt(Math.round(ixx), Math.round(iy), v.p(12.0), Math.max(1, v.p(0.8)), Style.strokeHi);
            Gx.icon("plus", ixx, iy, v.pf(9.0), Style.muted);
         }
      }

      Gx.icon("emote", ccx, ccy, v.pf(18.0), Style.sub);
      int ty = ccy + r + v.p(16.0);
      String[] tips = new String[]{"Taste B halten = Rad öffnen", "Maus auf Emote, loslassen", "Bewegen bricht Loop-Emotes ab"};

      for (String t : tips) {
         Gx.circle(wx + v.p(18.0), ty + v.p(5.0), v.p(2.0), Style.accent);
         Gx.text(Fonts.ellipsize(t, 1, v.pf(7.4F), wheelW - v.p(34.0)), wx + v.p(26.0), ty, v.pf(7.4F), 1, Style.sub);
         ty += v.p(15.0);
      }

      if (this.toast != null && System.currentTimeMillis() - this.toastAt < 1800L) {
         float ta = Math.min(1.0F, (float)(1800L - (System.currentTimeMillis() - this.toastAt)) / 300.0F);
         Gx.pushAlpha(ta);
         float fs = v.pf(8.4F);
         int tw = Math.round(Gx.width(this.toast, fs, 2)) + v.p(28.0);
         int th = v.p(26.0);
         int tx0 = x + (w - tw) / 2;
         int ty0 = y + h - th - v.p(18.0);
         Gx.shadow(tx0, ty0, tw, th, th / 2, v.p(6.0), 0.4F);
         Gx.rect(tx0, ty0, tw, th, th / 2, Style.light ? -15000542 : -789258);
         Gx.textCenter(this.toast, tx0 + tw / 2.0F, ty0 + th / 2.0F, fs, 2, Style.light ? -1 : -15658474);
         Gx.popAlpha();
      }
   }

   private void card(HubView v, EmoteLib.Emote e, int x, int y, int w, int h) {
      boolean modern = UiSettings.guiStyle == 1;
      boolean hv = v.hover(x, y, w, h);
      float hov = Ease.to("emo:" + e.id, hv ? 1.0F : 0.0F, 14.0F);
      boolean fav = EmoteLib.inWheel(e.id);
      if (!modern) {
         Gx.push();
         Gx.translate(0.0F, -v.pf(2.0) * hov);
      }

      if (!modern && hov > 0.01F) {
         Gx.shadow(x, y, w, h, v.p(12.0), v.p(6.0), 0.3F * hov);
      }

      int r = v.p(modern ? 16.0 : 12.0);
      Gx.rect(x, y, w, h, r, Gx.mix(Style.surface, Style.surfaceHover, hov * (modern ? 0.5F : 1.0F)));
      Gx.outline(
         x,
         y,
         w,
         h,
         r,
         Math.max(1, v.p(modern ? 0.6 : 0.7)),
         modern ? Gx.mix(Style.stroke, Style.strokeHi, hov) : Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.7F), hov)
      );
      if (modern && fav) {
         Gx.rect(x, y + v.p(8.0), Math.max(2, v.p(3.0)), h - v.p(16.0), v.p(2.0), -15043);
      }

      int ts = h - v.p(34.0);
      int tx = x + (w - ts) / 2;
      int ty = y + v.p(4.0);
      if (!modern) {
         Gx.glow(x + w / 2, ty + ts / 2, Math.round(ts * 0.55F), Gx.withAlpha(Style.accent, 0.1F + 0.12F * hov));
      }

      int px = Math.max(16, ts);
      Gx.Img img = Thumbs.get("emote:" + e.id, px, px, () -> Raster.renderPose(e.pose(Math.min(e.duration * 0.4F, 1.1F)), px, px, 22.0F, 8.0F, 0));
      if (img != null) {
         Gx.image(img, tx, ty, ts, ts, -1);
      } else {
         shimmer(v, tx + ts / 4, ty + ts / 4, ts / 2, ts / 2);
      }

      Gx.textCenter(Fonts.ellipsize(e.name, 3, v.pf(8.6F), w - v.p(30.0)), x + w / 2.0F, y + h - v.p(16.0), v.pf(8.6F), 3, Style.text);
      if (e.loop) {
         Gx.textCenter("LOOP", x + w / 2.0F, y + h - v.p(5.0), v.pf(5.6F), 3, Style.muted);
      } else if (EmoteLib.isCutscene(e.id)) {
         Gx.textCenter("CUTSCENE", x + w / 2.0F, y + h - v.p(5.0), v.pf(5.6F), 3, Style.accent);
      }

      int ss = v.p(22.0);
      int sx = x + w - ss - v.p(5.0);
      int sy = y + v.p(5.0);
      boolean hs = v.hover(sx, sy, ss, ss);
      float fv = Ease.to("emofav:" + e.id, fav ? 1.0F : 0.0F, 16.0F);
      Gx.circle(sx + ss / 2, sy + ss / 2, ss / 2, Gx.mix(Gx.withAlpha(Style.bg, 0.6F), Gx.withAlpha(-15043, 0.22F), fv));
      Gx.push();
      Gx.scaleAt(sx + ss / 2.0F, sy + ss / 2.0F, 1.0F + 0.25F * (1.0F - Math.abs(fv * 2.0F - 1.0F)) + (hs ? 0.08F : 0.0F));
      Gx.icon(fav ? "star-fill" : "star", sx + ss / 2.0F, sy + ss / 2.0F, v.pf(11.0), fav ? -15043 : (hs ? Style.text : Style.muted));
      Gx.pop();
      if (!modern) {
         Gx.pop();
      }

      Kit.hit(x, y, w, h).click(() -> {
         int idx = EmoteLib.index(e.id);
         v.host.closeThen(() -> Env.playEmote.accept(idx));
      });
      Kit.hit(sx, sy, ss, ss).click(() -> {
         if (!EmoteLib.toggleWheel(e.id)) {
            this.toast = "Emote-Rad ist voll (max. 8)";
            this.toastAt = System.currentTimeMillis();
         } else {
            Sound.pop();
         }
      });
   }

   static void shimmer(HubView v, int x, int y, int w, int h) {
      float t = (float)(System.currentTimeMillis() % 1200L) / 1200.0F;
      Gx.rect(x, y, w, h, v.p(8.0), Gx.withAlpha(Style.surface2, 0.6F + 0.4F * (float)Math.sin(t * Math.PI)));
   }

   @Override
   public boolean chr(String s) {
      if (!this.focus) {
         this.focus = true;
         this.query = "";
      }

      if (this.query.length() < 32) {
         this.query = this.query + s;
         this.target = 0.0F;
      }

      return true;
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == 259 && !this.query.isEmpty()) {
         this.query = this.query.substring(0, this.query.length() - 1);
         this.target = 0.0F;
         return true;
      } else if (key != 256 || !this.focus && this.query.isEmpty()) {
         return false;
      } else {
         this.query = "";
         this.focus = false;
         this.target = 0.0F;
         return true;
      }
   }
}
