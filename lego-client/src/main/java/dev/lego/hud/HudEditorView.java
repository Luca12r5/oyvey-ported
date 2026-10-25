package dev.lego.hud;

import dev.lego.LegoClient;
import dev.lego.core.Modules;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.McBackend;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;
import dev.spotifyhud.SpotifyHudMod;
import java.util.List;

public final class HudEditorView extends View {
   private boolean panelOpen = false;
   private float listScroll = 0.0F;
   private float listTarget = 0.0F;
   private boolean draggingHud = false;

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
   public float designW() {
      return 640.0F;
   }

   @Override
   public float designH() {
      return 380.0F;
   }

   private int gw() {
      return this.W / Gx.S;
   }

   private int gh() {
      return this.H / Gx.S;
   }

   @Override
   public void draw() {
      SpotifyHudMod.editorOpen = true;
      Kit.hit(0, 0, this.W, this.H).id("hud");
      float t = this.open;
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.22F * t));
      int step = Math.max(8, Math.round((float)(20 * Gx.S)));

      for (int x = 0; x < this.W; x += step) {
         Gx.fill(x, 0, 1, this.H, Gx.rgba(16777215, 0.035F * t));
      }

      for (int y = 0; y < this.H; y += step) {
         Gx.fill(0, y, this.W, 1, Gx.rgba(16777215, 0.035F * t));
      }

      Gx.fill(this.W / 2, 0, Math.max(1, Gx.S / 2), this.H, Gx.rgba(16777215, 0.1F * t));
      Gx.fill(0, this.H / 2, this.W, Math.max(1, Gx.S / 2), Gx.rgba(16777215, 0.1F * t));
      int gmx = Math.round(this.mx / Gx.S);
      int gmy = Math.round(this.my / Gx.S);
      if (Gx.B instanceof McBackend) {
         Gx.push();
         Gx.scale(Gx.S);

         try {
            HudManager.render(((McBackend)Gx.B).ctx(), this.gw(), this.gh(), true, gmx, gmy);
            SpotifyHudMod.editorRender(((McBackend)Gx.B).ctx(), gmx, gmy, this.gw(), this.gh());
         } catch (Throwable var19) {
            LegoClient.LOG("HUD-Editor: " + var19);
         }

         Gx.pop();
      }

      if (HudManager.snapX >= 0.0) {
         Gx.fill((int)Math.round(HudManager.snapX * Gx.S), 0, Math.max(1, Gx.S / 2), this.H, Style.accent);
      }

      if (HudManager.snapY >= 0.0) {
         Gx.fill(0, (int)Math.round(HudManager.snapY * Gx.S), this.W, Math.max(1, Gx.S / 2), Style.accent);
      }

      String[] hints = new String[]{"Ziehen = verschieben", "Mausrad = Größe", "Rechtsklick = zurücksetzen", "R = alle zurücksetzen", "ESC = speichern"};
      float fs = this.pf(7.4F);
      float total = 0.0F;

      for (String h : hints) {
         total += Gx.width(h, fs, 2);
      }

      total += (hints.length - 1) * this.pf(14.0) + this.pf(28.0);
      int hx = Math.round(this.W / 2.0F - total / 2.0F);
      int hy = this.p(10.0);
      int hh = this.p(24.0);
      Gx.pushAlpha(t);
      Gx.shadow(hx, hy, Math.round(total), hh, hh / 2, this.p(6.0), 0.35F);
      Gx.rect(hx, hy, Math.round(total), hh, hh / 2, -435088874);
      Gx.outline(hx, hy, Math.round(total), hh, hh / 2, Math.max(1, this.p(0.6)), 587202559);
      float x = hx + this.pf(14.0);

      for (int i = 0; i < hints.length; i++) {
         x += Gx.textMid(hints[i], x, hy + hh / 2.0F, fs, 2, -419430401);
         if (i < hints.length - 1) {
            Gx.circle(Math.round(x + this.pf(7.0)), hy + hh / 2, Math.max(1, this.p(1.3)), Style.accent);
            x += this.pf(14.0);
         }
      }

      int bs = this.p(28.0);
      int bx = this.W - bs - this.p(12.0);
      int by = this.p(10.0);
      boolean hb = this.hover(bx, by, bs, bs);
      Gx.rect(bx, by, bs, bs, this.p(8.0), hb ? -265934800 : -435088874);
      Gx.icon("list", bx + bs / 2.0F, by + bs / 2.0F, this.pf(13.0), this.panelOpen ? Style.accent : -1);
      Kit.hit(bx, by, bs, bs).click(() -> this.panelOpen = !this.panelOpen);
      int bx2 = bx - bs - this.p(6.0);
      boolean hb2 = this.hover(bx2, by, bs, bs);
      Gx.rect(bx2, by, bs, bs, this.p(8.0), hb2 ? -265934800 : -435088874);
      Gx.icon("mods", bx2 + bs / 2.0F, by + bs / 2.0F, this.pf(13.0), -1);
      Kit.hit(bx2, by, bs, bs).click(() -> {
         this.save();
         this.host.open(dev.lego.ui.bench.BenchView.at(null));
      });
      Gx.popAlpha();
      float po = Ease.to("hudedit:panel", this.panelOpen ? 1.0F : 0.0F, 14.0F);
      if (po > 0.01F) {
         this.drawPanel(po);
      }
   }

   private void drawPanel(float po) {
      int pw = this.p(190.0);
      int ph = this.H - this.p(60.0);
      int py = this.p(46.0);
      int px = Math.round(this.W - this.p(12.0) - pw + (1.0F - Ease.outCubic(po)) * (pw + this.p(20.0)));
      Gx.pushAlpha(po);
      Gx.shadow(px, py, pw, ph, this.p(12.0), this.p(8.0), 0.45F);
      Gx.rect(px, py, pw, ph, this.p(12.0), -233762282);
      Gx.outline(px, py, pw, ph, this.p(12.0), Math.max(1, this.p(0.6)), 587202559);
      Gx.text("HUD-Module", px + this.p(14.0), py + this.p(12.0), this.pf(10.0), 3, -1);
      List<HudModule> mods = HudManager.modules();
      int rowH = this.p(26.0);
      int ly = py + this.p(36.0);
      int lh = ph - this.p(44.0);
      float max = Math.max(0, mods.size() * rowH - lh);
      this.listTarget = Math.max(0.0F, Math.min(max, this.listTarget));
      this.listScroll = this.listScroll + (this.listTarget - this.listScroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(px, ly, pw, lh);
      Kit.clip(px, ly, pw, lh);
      Kit.hit(px, ly, pw, lh).scroll(a -> this.listTarget = this.listTarget - a.floatValue() * this.p(30.0));

      for (int i = 0; i < mods.size(); i++) {
         HudModule m = mods.get(i);
         int ry = Math.round(ly + i * rowH - this.listScroll);
         if (ry + rowH >= ly && ry <= ly + lh) {
            boolean hv = this.hover(px + this.p(6.0), ry, pw - this.p(12.0), rowH - this.p(2.0));
            if (hv) {
               Gx.rect(px + this.p(6.0), ry, pw - this.p(12.0), rowH - this.p(2.0), this.p(7.0), 352321535);
            }

            Gx.icon(m.icon, px + this.p(18.0), ry + (rowH - this.p(2.0)) / 2.0F, this.pf(10.0), m.enabled ? Style.accent : -7696487);
            Gx.textMid(
               Fonts.ellipsize(m.name, 2, this.pf(7.8F), pw - this.p(80.0)),
               px + this.p(30.0),
               ry + (rowH - this.p(2.0)) / 2.0F,
               this.pf(7.8F),
               2,
               m.enabled ? -1 : -7696487
            );
            Kit.toggle("hudp:" + m.id, px + pw - this.p(40.0), ry + (rowH - this.p(2.0)) / 2 - this.p(7.0), this.p(26.0), this.p(14.0), m.enabled, this.P);
            Kit.hit(px + this.p(6.0), ry, pw - this.p(12.0), rowH - this.p(2.0)).click(() -> {
               m.toggle();
               Modules.scheduleSave();
            });
         }
      }

      Kit.unclip();
      Gx.unclip();
      Gx.popAlpha();
   }

   @Override
   public boolean mouseDown(float x, float y, int button, boolean dbl) {
      Kit.Region r = Kit.at(x, y);
      if (r != null && !"hud".equals(r.idOrNull())) {
         return Kit.down(x, y, button);
      } else {
         int gx = Math.round(x / Gx.S);
         int gy = Math.round(y / Gx.S);
         if (SpotifyHudMod.editorDown(gx, gy, button, this.gw(), this.gh())) {
            this.draggingHud = true;
            return true;
         } else if (HudManager.mouseDown(gx, gy, button, this.gw(), this.gh())) {
            this.draggingHud = true;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean mouseDrag(float x, float y, int button) {
      if (Kit.dragging()) {
         return Kit.drag(x, y);
      } else {
         double gx = x / Gx.S;
         double gy = y / Gx.S;
         return SpotifyHudMod.editorDrag(gx, gy, this.gw(), this.gh()) ? true : HudManager.mouseDrag(gx, gy, this.gw(), this.gh(), true);
      }
   }

   @Override
   public boolean mouseUp(float x, float y, int button) {
      Kit.up();
      SpotifyHudMod.editorUp();
      HudManager.mouseUp();
      this.draggingHud = false;
      return true;
   }

   @Override
   public boolean scroll(float x, float y, double amount) {
      if (Kit.scroll(x, y, amount)) {
         return true;
      } else {
         double gx = x / Gx.S;
         double gy = y / Gx.S;
         return SpotifyHudMod.editorScroll(gx, gy, amount, this.gw(), this.gh()) ? true : HudManager.scroll(gx, gy, amount, this.gw(), this.gh());
      }
   }

   @Override
   public boolean key(int key, int mods) {
      if (key == 82) {
         HudManager.resetAll();
         return true;
      } else {
         return false;
      }
   }

   private void save() {
      Modules.save();
   }

   @Override
   public void onEscape() {
      this.save();
      this.host.close();
   }

   @Override
   public void removed() {
      SpotifyHudMod.editorOpen = false;
      HudManager.mouseUp();
      this.save();
   }
}
