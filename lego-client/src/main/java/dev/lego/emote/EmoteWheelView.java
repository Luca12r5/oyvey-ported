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

   public EmoteWheelView(int var1) {
      this.holdKey = var1;
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
      float var1 = Ease.outCubic(this.open);
      int var2 = this.p(118.0);
      int var3 = Math.round(var2 * 0.42F);
      int var4 = this.W / 2;
      int var5 = this.H / 2;
      Gx.pushAlpha(var1);
      Gx.Img var6 = Gx.painted(
         "wheel:dim",
         256,
         256,
         var0 -> {
            var0.setPaint(
               new RadialGradientPaint(new Float(128.0F, 128.0F), 181.0F, new float[]{0.0F, 1.0F}, new Color[]{new Color(0, 0, 0, 150), new Color(0, 0, 0, 60)})
            );
            var0.fillRect(0, 0, 256, 256);
         }
      );
      if (var6 != null) {
         Gx.image(var6, 0, 0, this.W, this.H, -1);
      }

      Gx.popAlpha();
      int var7 = this.count();
      float var8 = this.mx - var4;
      float var9 = this.my - var5;
      double var10 = Math.sqrt(var8 * var8 + var9 * var9);
      this.sel = -1;
      if (var7 > 0 && var10 > var3 * 0.55) {
         double var12 = Math.atan2(var9, var8) + (Math.PI / 2) + Math.PI / var7;
         var12 = (var12 % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
         this.sel = Math.min(var7 - 1, (int)(var12 / ((Math.PI * 2) / Math.max(1, var7))));
      }

      if (this.sel != this.lastSel && this.sel >= 0) {
         Sound.click();
      }

      this.lastSel = this.sel;
      Gx.push();
      Gx.scaleAt(var4, var5, 0.85F + 0.15F * Ease.outBack(this.open));
      Gx.pushAlpha(var1);
      int var32 = var2 * 2 + this.p(24.0);
      int var13 = this.p(12.0);
      int var14 = Math.max(1, var7);
      float var15 = this.P;
      int var16 = Style.surface;
      int var17 = Style.accent;
      Gx.shadow(var4 - var2, var5 - var2, var2 * 2, var2 * 2, var2, this.p(18.0), 0.55F);
      Gx.Img var18 = Gx.painted(
         "wheel:base:" + var14 + ":" + var32 + ":" + Integer.toHexString(var16) + ":" + Integer.toHexString(Style.stroke),
         var32,
         var32,
         var6x -> {
            double var7x = var32 / 2.0;
            Area var9x = new Area(new Double(var7x - var2, var7x - var2, var2 * 2, var2 * 2));
            var9x.subtract(new Area(new Double(var7x - var3, var7x - var3, var3 * 2, var3 * 2)));
            var6x.setColor(new Color(var16, true));
            var6x.fill(var9x);
            var6x.setColor(new Color(Style.stroke, true));
            var6x.setStroke(new BasicStroke(Math.max(1.0F, var15 * 0.8F)));

            for (int var10x = 0; var10x < var14; var10x++) {
               double var11 = (-Math.PI / 2) + (Math.PI * 2) * (var10x + 0.5) / var14;
               var6x.drawLine(
                  (int)(var7x + Math.cos(var11) * (var3 + 2)),
                  (int)(var7x + Math.sin(var11) * (var3 + 2)),
                  (int)(var7x + Math.cos(var11) * (var2 - 2)),
                  (int)(var7x + Math.sin(var11) * (var2 - 2))
               );
            }

            var6x.draw(new Double(var7x - var2 + 0.5, var7x - var2 + 0.5, var2 * 2 - 1, var2 * 2 - 1));
            var6x.draw(new Double(var7x - var3, var7x - var3, var3 * 2, var3 * 2));
         }
      );
      if (var18 != null) {
         Gx.image(var18, var4 - var32 / 2, var5 - var32 / 2, var32, var32, -1);
      }

      for (int var19 = 0; var19 < var7; var19++) {
         float var20 = Ease.to("wheel:h" + var19, var19 == this.sel ? 1.0F : 0.0F, 16.0F);
         if (!(var20 < 0.01F)) {
            final int slotIndex = var19;
            Gx.Img var22 = Gx.painted(
               "wheel:w" + var14 + ":" + var19 + ":" + var32,
               var32,
               var32,
               var6x -> {
                  double var7x = var32 / 2.0;
                  double var9x = 90.0 - 360.0 * (slotIndex - 0.5) / var14;
                  Area var11 = new Area(
                     new java.awt.geom.Arc2D.Double(
                        var7x - var2 - var13 * 0.4,
                        var7x - var2 - var13 * 0.4,
                        (var2 + var13 * 0.4) * 2.0,
                        (var2 + var13 * 0.4) * 2.0,
                        var9x,
                        -360.0 / var14,
                        2
                     )
                  );
                  var11.subtract(new Area(new Double(var7x - var3, var7x - var3, var3 * 2, var3 * 2)));
                  var6x.setColor(Color.WHITE);
                  var6x.fill(var11);
               }
            );
            if (var22 != null) {
               Gx.image(var22, var4 - var32 / 2, var5 - var32 / 2, var32, var32, Gx.withAlpha(var17, 0.28F * var20));
            }
         }
      }

      for (int var33 = 0; var33 < var7; var33++) {
         EmoteLib.Emote var36 = EmoteLib.get(EmoteLib.WHEEL.get(var33));
         if (var36 != null) {
            float var21 = Ease.to("wheel:h" + var33, var33 == this.sel ? 1.0F : 0.0F, 16.0F);
            double var39 = (-Math.PI / 2) + (Math.PI * 2) * var33 / var7;
            float var24 = (var2 + var3) / 2.0F + this.pf(4.0) * var21;
            float var25 = var4 + (float)Math.cos(var39) * var24;
            float var26 = var5 + (float)Math.sin(var39) * var24;
            int var27 = Math.round(this.pf(40.0) * (1.0F + 0.15F * var21));
            int var28 = Math.max(16, this.p(44.0));
            Gx.Img var29 = Thumbs.get(
               "emote:w:" + var36.id, var28, var28, () -> Raster.renderPose(var36.pose(Math.min(var36.duration * 0.4F, 1.1F)), var28, var28, 22.0F, 8.0F, 0)
            );
            if (var29 != null) {
               Gx.image(var29, Math.round(var25 - var27 / 2.0F), Math.round(var26 - var27 / 2.0F - this.pf(5.0)), var27, var27, -1);
            } else {
               Gx.icon(var36.icon, var25, var26 - this.pf(5.0), this.pf(16.0), Gx.mix(Style.sub, var17, var21));
            }

            float var30 = this.pf(6.8F);
            Gx.textCenter(
               Fonts.ellipsize(var36.name, 3, var30, (var2 - var3) * 0.95F), var25, var26 + this.pf(19.0), var30, 3, Gx.mix(Style.sub, Style.text, var21)
            );
         }
      }

      Gx.circle(var4, var5, var3 - this.p(4.0), Style.bg);
      if (this.sel >= 0 && this.sel < var7) {
         EmoteLib.Emote var34 = EmoteLib.get(EmoteLib.WHEEL.get(this.sel));
         if (var34 != null) {
            Gx.glow(var4, var5, var3, Gx.withAlpha(var17, 0.18F));
            Gx.textCenter(var34.name, var4, var5 - this.pf(4.0), this.pf(10.0), 3, Style.text);
            Gx.textCenter(
               var34.loop ? "Loop · bewegen stoppt" : String.format(Locale.ROOT, "%.1f s", var34.duration),
               var4,
               var5 + this.pf(10.0),
               this.pf(6.6F),
               2,
               Style.sub
            );
         }
      } else if (var7 == 0) {
         Gx.textCenter("Keine Favoriten", var4, var5 - this.pf(4.0), this.pf(8.4F), 3, Style.text);
         Gx.textCenter("Stern im Emote-Menü", var4, var5 + this.pf(9.0), this.pf(6.4F), 1, Style.sub);
      } else {
         Gx.icon("emote", var4, var5 - this.pf(6.0), this.pf(20.0), Style.sub);
         Gx.textCenter("Emote wählen", var4, var5 + this.pf(14.0), this.pf(6.8F), 2, Style.muted);
      }

      Gx.popAlpha();
      Gx.pop();
      Gx.pushAlpha(var1);
      String var35 = "Maus zeigen · loslassen oder klicken = abspielen · Esc = abbrechen";
      float var37 = this.pf(7.2F);
      int var38 = Math.round(Gx.width(var35, var37, 2)) + this.p(24.0);
      int var40 = this.p(22.0);
      int var23 = var4 - var38 / 2;
      int var41 = var5 + var2 + this.p(22.0);
      Gx.rect(var23, var41, var38, var40, var40 / 2, Gx.withAlpha(Style.bg, 0.85F));
      Gx.textCenter(var35, var4, var41 + var40 / 2.0F, var37, 2, Style.sub);
      Gx.popAlpha();
   }

   private void playSelected() {
      if (!this.played && this.host != null) {
         this.played = true;
         if (this.sel >= 0 && this.sel < this.count()) {
            int var1 = EmoteLib.index(EmoteLib.WHEEL.get(this.sel));
            this.host.closeThen(() -> Env.playEmote.accept(var1));
         } else {
            this.host.close();
         }
      }
   }

   @Override
   public boolean mouseDown(float var1, float var2, int var3, boolean var4) {
      if (var3 == 0) {
         this.playSelected();
      } else if (this.host != null) {
         this.host.close();
      }

      return true;
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 == -1 - this.holdKey) {
         this.playSelected();
         return true;
      } else {
         return false;
      }
   }
}
