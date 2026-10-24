package dev.lego.util;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.Tx;
import dev.lego.ui.View;
import dev.lego.visual.R3;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class FakeCheck extends Module {
   final Setting.Text names = this.add(new Setting.Text("Namen", "xJoru,LetsHugo,BastiGHG,PapaPlatte,xyzoom", 500, "Kommagetrennt"));
   final Setting.Num threshold = this.add(new Setting.Num("Toleranz", 1.0, 4.0, 1.0, 2.0, " Zeichen"));
   final Setting.Action manage = this.add(new Setting.Action("Verwalten", "Verwalten", () -> LegoScreen.show(new FakeCheck.FakeCheckView())));
   private final List<String> watchList = new ArrayList<>();
   private long lastParse = 0L;
   private static Identifier fakeTexId;

   public FakeCheck() {
      super("fakecheck", "Fake-Check", "Erkennt gefälschte Spielernamen und zeigt FAKE an", Category.UTILITY);
      this.icon("shield");
   }

   private void parseNames() {
      long now = System.currentTimeMillis();
      if (now - this.lastParse >= 2000L || this.watchList.isEmpty()) {
         this.lastParse = now;
         this.watchList.clear();
         String raw = this.names.get();
         if (raw != null && !raw.isEmpty()) {
            for (String n : raw.split(",")) {
               String t = n.trim();
               if (!t.isEmpty()) {
                  this.watchList.add(t.toLowerCase(Locale.ROOT));
               }
            }
         }
      }
   }

   public boolean isFake(String name) {
      if (this.enabled && name != null && !name.isEmpty()) {
         this.parseNames();
         String low = name.toLowerCase(Locale.ROOT);
         int thr = this.threshold.getI();

         for (String w : this.watchList) {
            if (!w.equals(low)) {
               int d = levenshtein(low, w);
               if (d > 0 && d <= thr) {
                  return true;
               }

               if (low.length() == w.length() && visualDist(low, w) <= thr) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public void renderWorld(float td) {
      if (this.enabled) {
         LocalPlayer me = Mc.player();
         if (me != null) {
            this.parseNames();

            for (Player p : Mc.players()) {
               if (p != me) {
                  String name = Mc.name(p);
                  if (this.isFake(name)) {
                     double[] pos = Mc.lerpPos(p, td);
                     double x = pos[0];
                     double y = pos[1] + Mc.height(p) + 0.7;
                     double z = pos[2];
                     double dx = x - R3.camX();
                     double dy = y - R3.camY();
                     double dz = z - R3.camZ();
                     if (!(dx * dx + dy * dy + dz * dz > 4096.0)) {
                        R3.billboard(fakeTexture(), x, y, z, 0.6, -56798);
                     }
                  }
               }
            }
         }
      }
   }

   private static Identifier fakeTexture() {
      if (fakeTexId != null) {
         return fakeTexId;
      } else {
         fakeTexId = Identifier.fromNamespaceAndPath("legoclient", "fake_label");
         BufferedImage img = new BufferedImage(128, 32, 2);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor(new Color(255, 30, 30, 220));
         g.fillRoundRect(0, 0, 128, 32, 8, 8);
         g.setFont(new Font("SansSerif", 1, 22));
         g.setColor(Color.WHITE);
         FontMetrics fm = g.getFontMetrics();
         g.drawString("FAKE", (128 - fm.stringWidth("FAKE")) / 2, 23);
         g.dispose();
         int[] px = img.getRGB(0, 0, 128, 32, null, 0, 128);
         Minecraft.getInstance().execute(() -> {
            try {
               Tx.register(fakeTexId, px, 128, 32, true);
            } catch (Throwable var2x) {
            }
         });
         return fakeTexId;
      }
   }

   private static int levenshtein(String a, String b) {
      int m = a.length();
      int n = b.length();
      if (m == 0) {
         return n;
      } else if (n == 0) {
         return m;
      } else {
         int[] prev = new int[n + 1];
         int[] curr = new int[n + 1];
         int j = 0;

         while (j <= n) {
            prev[j] = j++;
         }

         for (int i = 1; i <= m; i++) {
            curr[0] = i;

            for (int jx = 1; jx <= n; jx++) {
               int cost = a.charAt(i - 1) == b.charAt(jx - 1) ? 0 : 1;
               curr[jx] = Math.min(Math.min(curr[jx - 1] + 1, prev[jx] + 1), prev[jx - 1] + cost);
            }

            int[] tmp = prev;
            prev = curr;
            curr = tmp;
         }

         return prev[n];
      }
   }

   private static int visualDist(String a, String b) {
      int d = 0;

      for (int i = 0; i < a.length(); i++) {
         char ca = a.charAt(i);
         char cb = b.charAt(i);
         if (ca != cb && !visuallySimilar(ca, cb)) {
            d++;
         }
      }

      return d;
   }

   private static boolean visuallySimilar(char a, char b) {
      String pair = "" + Math.min((int)a, (int)b) + Math.max((int)a, (int)b);
      return "1l".equals(pair)
         || "1i".equals(pair)
         || "il".equals(pair)
         || "0o".equals(pair)
         || "ij".equals(pair)
         || "mn".equals(pair)
         || "uv".equals(pair)
         || "cl".equals(pair);
   }

   public final class FakeCheckView extends View {
      private String input = "";
      private float scroll = 0.0F;
      private float target = 0.0F;

      @Override
      public float designW() {
         return 500.0F;
      }

      @Override
      public float designH() {
         return 380.0F;
      }

      @Override
      public void draw() {
         float t = Ease.outQuint(this.open);
         Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.35F * this.open));
         int pw = this.p(500.0);
         int ph = this.p(380.0);
         int px = (this.W - pw) / 2;
         int py = (this.H - ph) / 2;
         Gx.pushAlpha(t);
         Gx.push();
         Gx.scaleAt(this.W / 2.0F, this.H / 2.0F, 0.94F + 0.06F * t);
         Gx.shadow(px, py, pw, ph, this.p(18.0), this.p(14.0), 0.55F);
         Gx.rect(px, py, pw, ph, this.p(18.0), Style.bg);
         Gx.outline(px, py, pw, ph, this.p(18.0), Math.max(1, this.p(0.6)), Style.stroke);
         int tb = py + this.p(14.0);
         int bs = this.p(30.0);
         boolean hb = this.hover(px + this.p(14.0), tb, bs, bs);
         Gx.rect(px + this.p(14.0), tb, bs, bs, this.p(9.0), hb ? Style.surfaceHover : Style.surface);
         Gx.icon("back", px + this.p(14.0) + bs / 2.0F - this.pf(1.0), tb + bs / 2.0F, this.pf(14.0), Style.text);
         Kit.hit(px + this.p(14.0), tb, bs, bs).click(() -> {
            if (this.host != null) {
               this.host.close();
            }
         });
         Gx.text("Fake-Check", px + this.p(54.0), tb + this.pf(1.0), this.pf(14.0), 3, Style.text);
         Gx.text("Überwachte Namen – ähnliche Spieler werden als FAKE markiert", px + this.p(54.0), tb + this.pf(19.0), this.pf(7.4F), 1, Style.sub);
         int cx = px + this.p(16.0);
         int cw = pw - this.p(32.0);
         int y = tb + this.p(46.0);
         int ih = this.p(32.0);
         Gx.rect(cx, y, cw - this.p(80.0), ih, this.p(10.0), Style.surface);
         Gx.outline(cx, y, cw - this.p(80.0), ih, this.p(10.0), Math.max(1, this.p(0.5)), Style.stroke);
         Gx.text(
            this.input.isEmpty() ? "Name eingeben..." : this.input,
            cx + this.p(10.0),
            y + this.pf(8.0),
            this.pf(10.0),
            1,
            this.input.isEmpty() ? Style.muted : Style.text
         );
         int abw = this.p(70.0);
         int abx = cx + cw - abw;
         boolean ah = this.hover(abx, y, abw, ih);
         Kit.button("add", "Hinzufügen", "plus", abx, y, abw, ih, this.P, true, ah);
         Kit.hit(abx, y, abw, ih).click(this::addName);
         y += ih + this.p(12.0);
         FakeCheck.this.parseNames();
         int ly = y;
         int lh = py + ph - this.p(14.0) - y;
         int rh = this.p(36.0);
         int gap = this.p(6.0);
         Gx.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
         Kit.clip(cx - this.p(4.0), y, cw + this.p(8.0), lh);
         float max = Math.max(0, FakeCheck.this.watchList.size() * (rh + gap) - gap - lh);
         this.target = Math.max(0.0F, Math.min(max, this.target));
         this.scroll = this.scroll + (this.target - this.scroll) * Math.min(1.0F, Gx.dt * 16.0F);
         Kit.hit(cx, y, cw, lh).scroll(a -> this.target = this.target - a.floatValue() * this.p(60.0));

         for (int i = 0; i < FakeCheck.this.watchList.size(); i++) {
            int ry = Math.round(ly + i * (rh + gap) - this.scroll);
            if (ry + rh >= ly && ry <= ly + lh) {
               Gx.rect(cx, ry, cw, rh, this.p(10.0), Style.surface);
               Gx.text(FakeCheck.this.watchList.get(i), cx + this.p(14.0), ry + this.pf(9.0), this.pf(10.0), 2, Style.text);
               int dbw = this.p(30.0);
               int dbx = cx + cw - dbw - this.p(8.0);
               int dby = ry + (rh - this.p(24.0)) / 2;
               boolean dh = this.hover(dbx, dby, dbw, this.p(24.0));
               Gx.rect(dbx, dby, dbw, this.p(24.0), this.p(8.0), dh ? 1090466611 : 553595699);
               Gx.icon("x", dbx + dbw / 2.0F, dby + this.pf(12.0), this.pf(10.0), -48060);
               int idx = i;
               Kit.hit(dbx, dby, dbw, this.p(24.0)).click(() -> this.removeName(idx));
            }
         }

         if (FakeCheck.this.watchList.isEmpty()) {
            Gx.icon("shield", px + pw / 2.0F, ly + this.p(40.0), this.pf(22.0), Style.muted);
            Gx.textCenter("Keine Namen in der Liste", px + pw / 2.0F, ly + this.pf(72.0), this.pf(9.0), 2, Style.sub);
         }

         Kit.unclip();
         Gx.unclip();
         Gx.pop();
         Gx.popAlpha();
      }

      private void addName() {
         if (!this.input.isEmpty()) {
            String cur = FakeCheck.this.names.get();
            FakeCheck.this.names.value = cur.isEmpty() ? this.input : cur + "," + this.input;
            this.input = "";
            FakeCheck.this.lastParse = 0L;
            Modules.scheduleSave();
         }
      }

      private void removeName(int idx) {
         FakeCheck.this.parseNames();
         if (idx >= 0 && idx < FakeCheck.this.watchList.size()) {
            FakeCheck.this.watchList.remove(idx);
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < FakeCheck.this.watchList.size(); i++) {
               if (i > 0) {
                  sb.append(",");
               }

               sb.append(FakeCheck.this.watchList.get(i));
            }

            FakeCheck.this.names.value = sb.toString();
            FakeCheck.this.lastParse = 0L;
            Modules.scheduleSave();
         }
      }

      @Override
      public boolean key(int key, int mods) {
         if (key == 256) {
            if (this.host != null) {
               this.host.close();
            }

            return true;
         } else if (key == 259 && !this.input.isEmpty()) {
            this.input = this.input.substring(0, this.input.length() - 1);
            return true;
         } else if (key == 257) {
            this.addName();
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean chr(String s) {
         if (s.length() == 1 && this.input.length() < 20) {
            this.input = this.input + s;
            return true;
         } else {
            return false;
         }
      }
   }
}
