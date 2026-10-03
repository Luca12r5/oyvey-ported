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
      float var1 = this.open;
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.22F * var1));
      int var2 = Math.max(8, Math.round((float)(20 * Gx.S)));

      for (int var3 = 0; var3 < this.W; var3 += var2) {
         Gx.fill(var3, 0, 1, this.H, Gx.rgba(16777215, 0.035F * var1));
      }

      for (int var20 = 0; var20 < this.H; var20 += var2) {
         Gx.fill(0, var20, this.W, 1, Gx.rgba(16777215, 0.035F * var1));
      }

      Gx.fill(this.W / 2, 0, Math.max(1, Gx.S / 2), this.H, Gx.rgba(16777215, 0.1F * var1));
      Gx.fill(0, this.H / 2, this.W, Math.max(1, Gx.S / 2), Gx.rgba(16777215, 0.1F * var1));
      int var21 = Math.round(this.mx / Gx.S);
      int var4 = Math.round(this.my / Gx.S);
      if (Gx.B instanceof McBackend) {
         Gx.push();
         Gx.scale(Gx.S);

         try {
            HudManager.render(((McBackend)Gx.B).ctx(), this.gw(), this.gh(), true, var21, var4);
            SpotifyHudMod.editorRender(((McBackend)Gx.B).ctx(), var21, var4, this.gw(), this.gh());
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

      String[] var5 = new String[]{"Ziehen = verschieben", "Mausrad = Größe", "Rechtsklick = zurücksetzen", "R = alle zurücksetzen", "ESC = speichern"};
      float var6 = this.pf(7.4F);
      float var7 = 0.0F;

      for (String var11 : var5) {
         var7 += Gx.width(var11, var6, 2);
      }

      var7 += (var5.length - 1) * this.pf(14.0) + this.pf(28.0);
      int var23 = Math.round(this.W / 2.0F - var7 / 2.0F);
      int var24 = this.p(10.0);
      int var25 = this.p(24.0);
      Gx.pushAlpha(var1);
      Gx.shadow(var23, var24, Math.round(var7), var25, var25 / 2, this.p(6.0), 0.35F);
      Gx.rect(var23, var24, Math.round(var7), var25, var25 / 2, -435088874);
      Gx.outline(var23, var24, Math.round(var7), var25, var25 / 2, Math.max(1, this.p(0.6)), 587202559);
      float var26 = var23 + this.pf(14.0);

      for (int var12 = 0; var12 < var5.length; var12++) {
         var26 += Gx.textMid(var5[var12], var26, var24 + var25 / 2.0F, var6, 2, -419430401);
         if (var12 < var5.length - 1) {
            Gx.circle(Math.round(var26 + this.pf(7.0)), var24 + var25 / 2, Math.max(1, this.p(1.3)), Style.accent);
            var26 += this.pf(14.0);
         }
      }

      int var27 = this.p(28.0);
      int var13 = this.W - var27 - this.p(12.0);
      int var14 = this.p(10.0);
      boolean var15 = this.hover(var13, var14, var27, var27);
      Gx.rect(var13, var14, var27, var27, this.p(8.0), var15 ? -265934800 : -435088874);
      Gx.icon("list", var13 + var27 / 2.0F, var14 + var27 / 2.0F, this.pf(13.0), this.panelOpen ? Style.accent : -1);
      Kit.hit(var13, var14, var27, var27).click(() -> this.panelOpen = !this.panelOpen);
      int var16 = var13 - var27 - this.p(6.0);
      boolean var17 = this.hover(var16, var14, var27, var27);
      Gx.rect(var16, var14, var27, var27, this.p(8.0), var17 ? -265934800 : -435088874);
      Gx.icon("mods", var16 + var27 / 2.0F, var14 + var27 / 2.0F, this.pf(13.0), -1);
      Kit.hit(var16, var14, var27, var27).click(() -> {
         this.save();
         this.host.open(new HubView());
      });
      Gx.popAlpha();
      float var18 = Ease.to("hudedit:panel", this.panelOpen ? 1.0F : 0.0F, 14.0F);
      if (var18 > 0.01F) {
         this.drawPanel(var18);
      }
   }

   private void drawPanel(float var1) {
      int var2 = this.p(190.0);
      int var3 = this.H - this.p(60.0);
      int var4 = this.p(46.0);
      int var5 = Math.round(this.W - this.p(12.0) - var2 + (1.0F - Ease.outCubic(var1)) * (var2 + this.p(20.0)));
      Gx.pushAlpha(var1);
      Gx.shadow(var5, var4, var2, var3, this.p(12.0), this.p(8.0), 0.45F);
      Gx.rect(var5, var4, var2, var3, this.p(12.0), -233762282);
      Gx.outline(var5, var4, var2, var3, this.p(12.0), Math.max(1, this.p(0.6)), 587202559);
      Gx.text("HUD-Module", var5 + this.p(14.0), var4 + this.p(12.0), this.pf(10.0), 3, -1);
      List var6 = HudManager.modules();
      int var7 = this.p(26.0);
      int var8 = var4 + this.p(36.0);
      int var9 = var3 - this.p(44.0);
      float var10 = Math.max(0, var6.size() * var7 - var9);
      this.listTarget = Math.max(0.0F, Math.min(var10, this.listTarget));
      this.listScroll = this.listScroll + (this.listTarget - this.listScroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var5, var8, var2, var9);
      Kit.clip(var5, var8, var2, var9);
      Kit.hit(var5, var8, var2, var9).scroll(var1x -> this.listTarget = this.listTarget - var1x.floatValue() * this.p(30.0));

      for (int var11 = 0; var11 < var6.size(); var11++) {
         HudModule var12 = (HudModule)var6.get(var11);
         int var13 = Math.round(var8 + var11 * var7 - this.listScroll);
         if (var13 + var7 >= var8 && var13 <= var8 + var9) {
            boolean var14 = this.hover(var5 + this.p(6.0), var13, var2 - this.p(12.0), var7 - this.p(2.0));
            if (var14) {
               Gx.rect(var5 + this.p(6.0), var13, var2 - this.p(12.0), var7 - this.p(2.0), this.p(7.0), 352321535);
            }

            Gx.icon(var12.icon, var5 + this.p(18.0), var13 + (var7 - this.p(2.0)) / 2.0F, this.pf(10.0), var12.enabled ? Style.accent : -7696487);
            Gx.textMid(
               Fonts.ellipsize(var12.name, 2, this.pf(7.8F), var2 - this.p(80.0)),
               var5 + this.p(30.0),
               var13 + (var7 - this.p(2.0)) / 2.0F,
               this.pf(7.8F),
               2,
               var12.enabled ? -1 : -7696487
            );
            Kit.toggle(
               "hudp:" + var12.id,
               var5 + var2 - this.p(40.0),
               var13 + (var7 - this.p(2.0)) / 2 - this.p(7.0),
               this.p(26.0),
               this.p(14.0),
               var12.enabled,
               this.P
            );
            Kit.hit(var5 + this.p(6.0), var13, var2 - this.p(12.0), var7 - this.p(2.0)).click(() -> {
               var12.toggle();
               Modules.scheduleSave();
            });
         }
      }

      Kit.unclip();
      Gx.unclip();
      Gx.popAlpha();
   }

   @Override
   public boolean mouseDown(float var1, float var2, int var3, boolean var4) {
      Kit.Region var5 = Kit.at(var1, var2);
      if (var5 != null && !"hud".equals(var5.idOrNull())) {
         return Kit.down(var1, var2, var3);
      } else {
         int var6 = Math.round(var1 / Gx.S);
         int var7 = Math.round(var2 / Gx.S);
         if (SpotifyHudMod.editorDown(var6, var7, var3, this.gw(), this.gh())) {
            this.draggingHud = true;
            return true;
         } else if (HudManager.mouseDown(var6, var7, var3, this.gw(), this.gh())) {
            this.draggingHud = true;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean mouseDrag(float var1, float var2, int var3) {
      if (Kit.dragging()) {
         return Kit.drag(var1, var2);
      } else {
         double var4 = var1 / Gx.S;
         double var6 = var2 / Gx.S;
         return SpotifyHudMod.editorDrag(var4, var6, this.gw(), this.gh()) ? true : HudManager.mouseDrag(var4, var6, this.gw(), this.gh(), true);
      }
   }

   @Override
   public boolean mouseUp(float var1, float var2, int var3) {
      Kit.up();
      SpotifyHudMod.editorUp();
      HudManager.mouseUp();
      this.draggingHud = false;
      return true;
   }

   @Override
   public boolean scroll(float var1, float var2, double var3) {
      if (Kit.scroll(var1, var2, var3)) {
         return true;
      } else {
         double var5 = var1 / Gx.S;
         double var7 = var2 / Gx.S;
         return SpotifyHudMod.editorScroll(var5, var7, var3, this.gw(), this.gh()) ? true : HudManager.scroll(var5, var7, var3, this.gw(), this.gh());
      }
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 == 82) {
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
