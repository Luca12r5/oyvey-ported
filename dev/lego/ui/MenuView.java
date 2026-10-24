package dev.lego.ui;

import dev.lego.ui.hub.HubView;
import java.util.Locale;

public final class MenuView extends View {
   private static final String[][] QUICK = new String[][]{
      {"music", "Spotify – Songs & Playlists", "SPOTIFY"},
      {"shirt", "Kosmetik", "COSMETICS"},
      {"emote", "Emotes", "EMOTES"},
      {"gamepad", "Minispiele", "GAMES"},
      {"layout", "HUD bearbeiten", "HUD"},
      {"palette", "Design", "DESIGN"}
   };

   @Override
   public float openMs() {
      return 700.0F;
   }

   @Override
   public float closeMs() {
      return 200.0F;
   }

   @Override
   public float designW() {
      return 640.0F;
   }

   @Override
   public float designH() {
      return 400.0F;
   }

   @Override
   public void draw() {
      float t = this.open;
      boolean closing = this.host != null && this.host.closing();
      boolean seasonal = Halloween.on() || Xmas.on();
      Gx.gradientV(0, 0, this.W, this.H, Gx.rgba(0, (seasonal ? 0.1F : 0.3F) * t), Gx.rgba(0, (seasonal ? 0.3F : 0.55F) * t));
      int cx = this.W / 2;
      int cy = this.H / 2 - this.p(46.0);
      float gl = closing ? t : Ease.outCubic(Ease.stagger(t, 0, 0.0F, 0.7F));
      Gx.pushAlpha(gl);
      Gx.push();
      Gx.translate(0.0F, -this.pf(30.0) * (1.0F - gl));
      long now = System.currentTimeMillis();
      if (Halloween.on()) {
         Halloween.lanterns(-this.p(10.0), this.p(4.0), this.W + this.p(10.0), this.P, this.p(16.0), 3);
         float[] bxF = new float[]{0.1F, 0.25F, 0.5F, 0.75F, 0.9F};

         for (int i = 0; i < bxF.length; i++) {
            float len = this.pf(40 + i * 31 % 50);
            float sw = (float)Math.sin(now / 1300.0 + i * 1.5) * 0.06F;
            float ax = this.W * bxF[i];
            float ex = ax + (float)Math.sin(sw) * len;
            float ey = this.p(6.0) + (float)Math.cos(sw) * len;
            int seg = Math.max(1, this.p(5.0));
            int steps = Math.max(1, Math.round(len / seg));

            for (int k = 0; k < steps; k++) {
               float u = (float)k / steps;
               Gx.fill(Math.round(ax + (ex - ax) * u), Math.round(this.p(6.0) + (ey - this.p(6.0)) * u), Math.max(1, this.p(0.6)), seg, 1143608640);
            }

            Halloween.pumpkin(ex, ey + this.pf(10.0), this.pf(10 + i % 2 * 3));
         }
      } else {
         Xmas.lights(-this.p(10.0), this.p(4.0), this.W + this.p(10.0), this.P, this.p(16.0), 3);
         int[] bc = new int[]{-1890757, -736942, -12867329, -14703780, -5022465, -1890757};
         float[] bxF = new float[]{0.08F, 0.19F, 0.31F, 0.69F, 0.81F, 0.92F};

         for (int i = 0; i < bxF.length && Xmas.on(); i++) {
            float len = this.pf(50 + i * 37 % 60);
            float sw = (float)Math.sin(now / 1100.0 + i * 1.3) * 0.08F;
            float ax = this.W * bxF[i];
            float ex = ax + (float)Math.sin(sw) * len;
            float ey = this.p(6.0) + (float)Math.cos(sw) * len;
            int seg = Math.max(1, this.p(5.0));
            int steps = Math.max(1, Math.round(len / seg));

            for (int k = 0; k < steps; k++) {
               float u = (float)k / steps;
               Gx.fill(Math.round(ax + (ex - ax) * u), Math.round(this.p(6.0) + (ey - this.p(6.0)) * u), Math.max(1, this.p(0.6)), seg, 1726536104);
            }

            Xmas.bauble(ex, ey + this.pf(10.0), this.pf(11 + i % 2 * 3), bc[i]);
         }
      }

      Gx.pop();
      Gx.popAlpha();
      float a = closing ? t : Ease.outCubic(Ease.stagger(t, 0, 0.0F, 0.55F));
      float sc = closing ? 0.94F + 0.06F * t : 0.86F + 0.14F * Ease.outBack(Ease.stagger(t, 0, 0.0F, 0.6F));
      float pulse = 0.5F + 0.5F * (float)Math.sin(now / 900.0);
      int logoCol = Halloween.on() ? -38118 : (Xmas.on() ? -1890757 : Style.accent);
      Gx.pushAlpha(a);
      Gx.glow(cx, cy, this.p(170.0), Gx.withAlpha(logoCol, 0.1F + 0.05F * pulse));
      Gx.push();
      Gx.translate(0.0F, this.pf(14.0) * (1.0F - a));
      Gx.scaleAt(cx, cy, sc);
      float fs = this.pf(44.0);
      float wL = Gx.width("LEGO", fs, 3);
      float wC = Gx.width("CLIENT", fs, 0);
      int brick = this.p(62.0);
      int gap = this.p(14.0);
      float total = wL + gap + brick + gap + wC;
      float x = cx - total / 2.0F;
      Gx.textMid("LEGO", x, cy, fs, 3, -1);
      x += wL + gap;
      float bob = (float)Math.sin(now / 700.0) * this.pf(2.2F);
      Gx.glow(Math.round(x + brick / 2.0F), cy, brick, Gx.withAlpha(logoCol, 0.35F));
      Gx.image(Art.brickImg(brick, logoCol), Math.round(x), Math.round(cy - brick / 2.0F - this.pf(3.0) + bob), brick, brick, -1);
      if (Halloween.on()) {
         Halloween.witchHat(x + brick / 2.0F + this.pf(3.0), cy - brick / 2.0F + this.pf(12.0) + bob, brick * 0.95F, 0.0F);
      } else {
         Xmas.hat(x + brick / 2.0F + this.pf(3.0), cy - brick / 2.0F + this.pf(12.0) + bob, brick * 0.95F, 0.0F);
      }

      x += brick + gap;
      Gx.textMid("CLIENT", x, cy, fs, 0, -1);
      Gx.pop();
      if (Halloween.on()) {
         Gx.textSpaced("HALLOWEEN  EDITION", cx, cy + this.pf(38.0), this.pf(8.4F), 3, -38118, this.pf(3.2F));
         Gx.textSpaced(
            Halloween.countdown().toUpperCase(Locale.ROOT) + "   •   v" + Env.version, cx, cy + this.pf(54.0), this.pf(6.6F), 2, -1711276033, this.pf(1.4F)
         );
      } else if (Xmas.on()) {
         Gx.textSpaced("WINTER  EDITION", cx, cy + this.pf(38.0), this.pf(8.4F), 3, -736942, this.pf(3.2F));
         Gx.textSpaced(
            Xmas.countdown().toUpperCase(Locale.ROOT) + "   •   v" + Env.version, cx, cy + this.pf(54.0), this.pf(6.6F), 2, -1711276033, this.pf(1.4F)
         );
      } else {
         Gx.textSpaced("v" + Env.version + "   •   MINECRAFT " + Env.mcVersion, cx, cy + this.pf(40.0), this.pf(7.4F), 2, -1711276033, this.pf(1.6F));
      }

      Gx.popAlpha();
      float b1 = closing ? t : Ease.outCubic(Ease.stagger(t, 1, 0.09F, 0.5F));
      int bw = this.p(300.0);
      int bh = this.p(40.0);
      int bx = cx - bw / 2;
      int by = cy + this.p(74.0);
      boolean hov = this.hover(bx, by, bw, bh);
      float hv = Ease.to("menu:mod", hov ? 1.0F : 0.0F, 14.0F);
      Gx.pushAlpha(b1);
      Gx.push();
      Gx.translate(0.0F, this.pf(16.0) * (1.0F - b1));
      Gx.scaleAt(cx, by + bh / 2.0F, 1.0F + 0.025F * hv);
      Gx.shadow(bx, by, bw, bh, this.p(10.0), this.p(7.0), 0.45F);
      if (Halloween.on()) {
         if (hv > 0.01F) {
            Gx.glow(cx, by + bh / 2, this.p(150.0), Gx.withAlpha(-38118, 0.16F * hv));
         }

         Gx.rectV(bx, by, bw, bh, this.p(10.0), Gx.mix(-14020544, -12969899, hv), -15202264);
         Gx.outline(bx, by, bw, bh, this.p(10.0), Math.max(1, this.p(1.0)), Gx.mix(1442810688, -38118, hv));
         Halloween.cobweb(bx, by, bw, this.P * 0.85F);
         Gx.icon("skull", bx + this.p(28.0), by + bh / 2.0F + this.pf(1.0), this.pf(14.0), -38118);
         Gx.textSpaced("MOD MENU", cx, by + bh / 2.0F + this.pf(1.0), this.pf(11.5), 3, -1, this.pf(2.4F));
      } else if (Xmas.on()) {
         if (hv > 0.01F) {
            Gx.glow(cx, by + bh / 2, this.p(150.0), Gx.withAlpha(-736942, 0.14F * hv));
         }

         Gx.rectV(bx, by, bw, bh, this.p(10.0), Gx.mix(-1099186, -42388, hv), -6089686);
         Gx.outline(bx, by, bw, bh, this.p(10.0), Math.max(1, this.p(1.0)), Gx.mix(1442830730, -736942, hv));
         Xmas.snowCap(bx, by, bw, this.P * 0.85F);
         Gx.icon("gift", bx + this.p(28.0), by + bh / 2.0F + this.pf(1.0), this.pf(14.0), -1);
         Gx.textSpaced("MOD MENU", cx, by + bh / 2.0F + this.pf(1.0), this.pf(11.5), 3, -1, this.pf(2.4F));
      } else {
         Gx.rect(bx, by, bw, bh, this.p(8.0), Gx.mix(-653126888, -434496478, hv));
         Gx.outline(bx, by, bw, bh, this.p(8.0), Math.max(1, this.p(0.8)), Gx.mix(654311423, Style.accent, hv));
         if (hv > 0.01F) {
            Gx.glow(cx, by + bh / 2, this.p(120.0), Gx.withAlpha(Style.accent, 0.1F * hv));
         }

         Gx.textSpaced("MOD MENU", cx, by + bh / 2.0F, this.pf(11.0), 3, Gx.mix(-1, Style.accent, hv * 0.6F), this.pf(2.2F));
      }

      Gx.pop();
      Gx.popAlpha();
      Kit.hit(bx, by, bw, bh).click(() -> this.host.open(new HubView(HubView.Section.MODS)));
      int qs = this.p(46.0);
      int qg = this.p(12.0);
      int qx = cx - (QUICK.length * qs + (QUICK.length - 1) * qg) / 2;
      int qy = by + bh + this.p(18.0);
      int[][] giftCol = new int[][]{
         {-15037874, -736942},
         {-2743752, -736942},
         {-15037874, -1890757},
         {-14795142, -736942},
         {-1527238, -4909780},
         {-9752656, -726576},
         {-15766677, -736942}
      };
      String tip = null;
      int tipX = 0;

      for (int i = 0; i < QUICK.length; i++) {
         float bi = closing ? t : Ease.outBack(Ease.stagger(t, i + 2, 0.06F, 0.45F));
         int x0 = qx + i * (qs + qg);
         boolean h = this.hover(x0, qy - this.p(8.0), qs, qs + this.p(8.0));
         float hq = Ease.to("menu:q" + i, h ? 1.0F : 0.0F, 14.0F);
         Gx.pushAlpha(Math.min(1.0F, Math.max(0.0F, bi)));
         Gx.push();
         Gx.translate(0.0F, this.pf(18.0) * (1.0F - bi));
         if (Halloween.on()) {
            int[][] hwCol = new int[][]{
               {-12969899, -38118}, {-14020544, -7655482}, {-15058406, -12916643}, {-12972000, -3924450}, {-14020544, -14006}, {-12969899, -38118}
            };
            float wob = h ? (float)Math.sin(now / 90.0) * this.pf(0.8F) : 0.0F;
            Gx.translate(wob, 0.0F);
            Halloween.tombstone(x0, qy, qs, qs, hwCol[i][0], hwCol[i][1], this.pf(6.0) * hq, this.P);
            float iy = qy + qs * 0.5F - this.pf(6.0) * hq;
            Gx.circle(x0 + qs / 2, Math.round(iy), this.p(12.0), -654311424);
            Gx.icon(QUICK[i][0], x0 + qs / 2.0F, iy, this.pf(14.0), hwCol[i][1]);
         } else if (Xmas.on()) {
            float wob = h ? (float)Math.sin(now / 90.0) * this.pf(0.8F) : 0.0F;
            Gx.translate(wob, 0.0F);
            Xmas.gift(x0, qy, qs, qs, giftCol[i][0], giftCol[i][1], this.pf(6.0) * hq, this.P);
            float iy = qy + qs * 0.64F - this.pf(6.0) * hq;
            Gx.circle(x0 + qs / 2, Math.round(iy), this.p(12.0), -419430401);
            Gx.icon(QUICK[i][0], x0 + qs / 2.0F, iy, this.pf(14.0), giftCol[i][0]);
         } else {
            Gx.scaleAt(x0 + qs / 2.0F, qy + qs / 2.0F, 1.0F + 0.06F * hq);
            Gx.shadow(x0, qy, qs, qs, this.p(9.0), this.p(5.0), 0.35F);
            Gx.rect(x0, qy, qs, qs, this.p(9.0), Gx.mix(-653126888, -266592731, hq));
            Gx.outline(x0, qy, qs, qs, this.p(9.0), Math.max(1, this.p(0.8)), Gx.mix(587202559, Style.accent, hq));
            Gx.icon(QUICK[i][0], x0 + qs / 2.0F, qy + qs / 2.0F, this.pf(21.0), Gx.mix(-1512980, Style.accent, hq));
         }

         Gx.pop();
         Gx.popAlpha();
         String sec = QUICK[i][2];
         Kit.hit(x0, qy - this.p(8.0), qs, qs + this.p(8.0)).click(() -> this.openSection(sec));
         if (h) {
            tip = QUICK[i][1];
            tipX = x0 + qs / 2;
         }
      }

      if (tip != null) {
         Kit.tooltip(tip, tipX, qy - this.p(14.0), this.P);
      }

      float f = closing ? t : Ease.outCubic(Ease.stagger(t, 7, 0.06F, 0.5F));
      Gx.pushAlpha(f);
      Gx.textSpaced("ESC  ODER  RECHTS-SHIFT  ZUM  SCHLIESSEN", cx, this.H - this.pf(22.0), this.pf(6.6F), 2, 1728053247, this.pf(1.2F));
      String np = Env.nowPlaying.get();
      if (np != null && !np.isEmpty()) {
         float s = this.pf(8.0);
         String txt = Fonts.ellipsize(np, 2, s, this.pf(220.0));
         int w = Math.round(Gx.width(txt, s, 2) + this.pf(34.0));
         int hx = this.p(24.0);
         int x0x = this.p(16.0);
         int y0 = this.H - hx - this.p(14.0);
         Gx.rect(x0x, y0, w, hx, hx / 2, -1290661096);
         Gx.outline(x0x, y0, w, hx, hx / 2, Math.max(1, this.p(0.6)), 536870911);
         Gx.circle(x0x + this.p(12.0), y0 + hx / 2, this.p(6.5), -14756000);
         Gx.icon("music", x0x + this.p(12.0), y0 + hx / 2.0F, this.pf(7.5), -16053493);
         Gx.textMid(txt, x0x + this.p(24.0), y0 + hx / 2.0F, s, 2, -419430401);
      }

      Gx.popAlpha();
   }

   private void openSection(String sec) {
      if ("HUD".equals(sec)) {
         this.host.closeThen(Env.openHudEditor);
      } else if ("SPOTIFY".equals(sec)) {
         this.host.open(new SpotifyView());
      } else {
         this.host.open(new HubView(HubView.Section.valueOf(sec)));
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == 344) {
         this.host.close();
         return true;
      } else {
         return false;
      }
   }
}
