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
   public void draw(HubView var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.p(18.0);
      int var7 = var1.p(128.0);
      int var8 = var2 + var6;
      int var9 = var3 + var1.p(18.0);
      Gx.text("Kosmetik", var8 + var1.p(4.0), var9, var1.pf(15.0), 3, Style.text);
      Gx.text(Cos.equippedCount() + " ausgerüstet", var8 + var1.p(4.0), var9 + var1.pf(21.0), var1.pf(7.6F), 1, Style.sub);
      int var10 = var9 + var1.p(40.0);
      ArrayList var11 = new ArrayList();
      var11.add(null);

      for (Cos.Slot var15 : Cos.Slot.values()) {
         var11.add(var15);
      }

      int var45 = var1.p(29.0);
      int var46 = var11.indexOf(filter);
      float var47 = Ease.to("cos:cat", var10 + var46 * var45, 18.0F, var10 + var46 * var45);
      Gx.rect(var8, Math.round(var47), var7, var45 - var1.p(3.0), var1.p(8.0), Gx.withAlpha(Style.accent, 0.16F));

      for (int var48 = 0; var48 < var11.size(); var48++) {
         Cos.Slot var16 = (Cos.Slot)var11.get(var48);
         int var17 = var10 + var48 * var45;
         boolean var18 = var1.hover(var8, var17, var7, var45 - var1.p(3.0));
         boolean var19 = var16 == filter;
         float var20 = Ease.to("cos:ch" + var48, var18 ? 1.0F : 0.0F, 14.0F);
         if (!var19 && var20 > 0.01F) {
            Gx.rect(var8, var17, var7, var45 - var1.p(3.0), var1.p(8.0), Gx.withAlpha(Style.surfaceHover, var20));
         }

         int var21 = var19 ? Style.accent : Gx.mix(Style.sub, Style.text, var20);
         Gx.icon(var16 == null ? "grid" : var16.icon, var8 + var1.p(15.0), var17 + (var45 - var1.p(3.0)) / 2.0F, var1.pf(12.0), var21);
         String var22 = var16 == null ? "Alle" : var16.title;
         Gx.textMid(var22, var8 + var1.p(28.0), var17 + (var45 - var1.p(3.0)) / 2.0F, var1.pf(8.4F), var19 ? 3 : 2, var19 ? Style.text : var21);
         int var23 = var16 == null ? Cos.ALL.size() : Cos.of(var16).size();
         Cos.Item var24 = var16 == null ? null : Cos.equipped(var16);
         if (var24 != null) {
            Gx.circle(var8 + var7 - var1.p(26.0), var17 + (var45 - var1.p(3.0)) / 2, var1.p(2.6), Style.ok);
         }

         Gx.textRight(String.valueOf(var23), var8 + var7 - var1.p(10.0), var17 + (var45 - var1.p(3.0)) / 2.0F, var1.pf(7.2F), 2, Style.muted);
         Kit.hit(var8, var17, var7, var45 - var1.p(3.0)).click(() -> {
            filter = var16;
            this.target = this.scroll = 0.0F;
            this.changedAt = System.currentTimeMillis();
         });
      }

      int var49 = var3 + var5 - var1.p(46.0);
      int var50 = var1.p(28.0);
      boolean var51 = var1.hover(var8, var49, var7, var50);
      Kit.button("cos:none", "Alles ablegen", "trash", var8, var49, var7, var50, var1.P, false, var51);
      Kit.hit(var8, var49, var7, var50).click(Cos::unequipAll);
      int var52 = var1.p(172.0);
      int var53 = var2 + var4 - var6 - var52;
      int var54 = var3 + var1.p(18.0);
      int var55 = var5 - var1.p(36.0);
      Gx.rectV(var53, var54, var52, var55, var1.p(14.0), Gx.mix(Style.surface, Style.accent, 0.08F), Style.surface);
      Gx.outline(var53, var54, var52, var55, var1.p(14.0), Math.max(1, var1.p(0.6)), Style.stroke);
      Gx.glow(var53 + var52 / 2, var54 + var55 / 2 - var1.p(30.0), Math.round(var52 * 0.6F), Gx.withAlpha(Style.accent, 0.16F));
      Gx.circle(var53 + var52 / 2, var54 + var55 - var1.p(92.0), var1.p(44.0), Gx.withAlpha(-16777216, 0.25F));
      if (!this.dragging) {
         this.yawVel = this.yawVel * (float)Math.exp(-Gx.dt * 3.0F);
         this.yaw = this.yaw + (this.yawVel + 12.0F) * Gx.dt;
      }

      int var56 = var54 + var1.p(34.0);
      int var57 = var55 - var1.p(120.0);
      boolean var25 = Env.playerPreview.draw(var53, var56, var52, var57, this.yaw, 0.0F);
      if (!var25) {
         Cos.Item var26 = this.selected != null ? this.selected : (Cos.ALL.isEmpty() ? null : Cos.ALL.get(0));
         if (var26 != null) {
            int var27 = Math.min(var52, var57);
            float var28 = Math.round(this.yaw / 15.0F) * 15.0F;
            Gx.Img var29 = Thumbs.get(
               "cosbig:" + var26.id + ":" + (int)var28, var27, var27, () -> Raster.render(var26, var27, var27, var28, 10.0F, 1.2F, true, 0)
            );
            if (var29 != null) {
               Gx.image(var29, var53 + (var52 - var27) / 2, var56 + (var57 - var27) / 2, var27, var27, -1);
            }
         }
      }

      Kit.hit(var53, var56, var52, var57).drag((var2x, var3x) -> {
         if (!this.dragging) {
            this.dragging = true;
            this.lastMx = var2x;
         }

         float var4x = var2x - this.lastMx;
         this.yaw = this.yaw + var4x / var1.P * 1.6F;
         this.yawVel = var4x / var1.P * 30.0F;
         this.lastMx = var2x;
      });
      Gx.textCenter("VORSCHAU", var53 + var52 / 2.0F, var54 + var1.p(16.0), var1.pf(6.8F), 3, Style.muted);
      int var58 = var54 + var55 - var1.p(80.0);
      Gx.text("Ausgerüstet", var53 + var1.p(12.0), var58, var1.pf(8.0), 3, Style.text);
      int var59 = var58 + var1.p(16.0);
      int var60 = var53 + var1.p(12.0);
      int var61 = 0;

      for (Cos.Slot var33 : Cos.Slot.values()) {
         Cos.Item var34 = Cos.equipped(var33);
         if (var34 != null) {
            if (var61 >= 6) {
               break;
            }

            float var35 = var1.pf(6.8F);
            String var36 = Fonts.ellipsize(var34.name, 2, var35, var52 - var1.p(40.0));
            int var37 = Math.round(Gx.width(var36, var35, 2)) + var1.p(22.0);
            int var38 = var1.p(16.0);
            if (var60 + var37 > var53 + var52 - var1.p(10.0)) {
               var60 = var53 + var1.p(12.0);
               var59 += var38 + var1.p(4.0);
            }

            if (var59 + var38 > var54 + var55 - var1.p(6.0)) {
               break;
            }

            Gx.rect(var60, var59, var37, var38, var38 / 2, Gx.withAlpha(var34.rarity.color, 0.2F));
            Gx.circle(var60 + var1.p(8.0), var59 + var38 / 2, var1.p(2.4), var34.rarity.color);
            Gx.textMid(var36, var60 + var1.p(14.0), var59 + var38 / 2.0F, var35, 2, Style.text);
            var60 += var37 + var1.p(4.0);
            var61++;
         }
      }

      if (var61 == 0) {
         Gx.text("Noch nichts – klick auf ein Item!", var53 + var1.p(12.0), var59, var1.pf(7.0), 1, Style.muted);
      }

      int var62 = var8 + var7 + var1.p(16.0);
      int var63 = var53 - var1.p(14.0) - var62;
      int var64 = var3 + var1.p(18.0);
      int var65 = var5 - var1.p(36.0);
      List var66 = this.items();
      byte var67 = 2;
      int var68 = var1.p(10.0);
      int var69 = (var63 - var68) / var67;
      int var70 = var1.p(150.0);
      int var39 = (var66.size() + var67 - 1) / var67;
      this.max = Math.max(0, var39 * (var70 + var68) - var68 - var65);
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var62 - var1.p(4.0), var64, var63 + var1.p(8.0), var65);
      Kit.clip(var62 - var1.p(4.0), var64, var63 + var1.p(8.0), var65);
      Kit.hit(var62, var64, var63, var65).scroll(var2x -> this.target = this.target - var2x.floatValue() * var1.p(60.0));
      float var40 = Math.min(1.0F, (float)(System.currentTimeMillis() - this.changedAt) * UiSettings.anim() / 600.0F);

      for (int var41 = 0; var41 < var66.size(); var41++) {
         int var42 = var62 + var41 % var67 * (var69 + var68);
         int var43 = Math.round(var64 + var41 / var67 * (var70 + var68) - this.scroll);
         if (var43 + var70 >= var64 && var43 <= var64 + var65) {
            float var44 = Ease.outCubic(Ease.clamp((var40 * 600.0F - Math.min(var41, 10) * 25) / 260.0F));
            Gx.pushAlpha(var44);
            Gx.push();
            Gx.translate(0.0F, var1.pf(10.0) * (1.0F - var44));
            this.card(var1, (Cos.Item)var66.get(var41), var42, var43, var69, var70);
            Gx.pop();
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (this.max > 0.0F) {
         int var71 = var65 - var1.p(8.0);
         int var72 = Math.max(var1.p(24.0), Math.round(var71 * var65 / (var65 + this.max)));
         int var73 = var64 + var1.p(4.0) + Math.round((var71 - var72) * (this.scroll / this.max));
         Gx.rect(var62 + var63 + var1.p(3.0), var73, var1.p(3.0), var72, var1.p(2.0), Style.strokeHi);
      }
   }

   private void card(HubView var1, Cos.Item var2, int var3, int var4, int var5, int var6) {
      boolean var7 = var1.hover(var3, var4, var5, var6);
      boolean var8 = Cos.isEquipped(var2);
      float var9 = Ease.to("cos:" + var2.id, var7 ? 1.0F : 0.0F, 14.0F);
      float var10 = Ease.to("coson:" + var2.id, var8 ? 1.0F : 0.0F, 12.0F);
      int var11 = var1.p(12.0);
      if (var9 > 0.01F) {
         Gx.shadow(var3, var4, var5, var6, var11, var1.p(6.0), 0.3F * var9);
      }

      Gx.rectV(var3, var4, var5, var6, var11, Gx.mix(Style.surface, Gx.mix(Style.surface, var2.rarity.color, 0.18F), 0.6F + 0.4F * var9), Style.surface);
      Gx.outline(
         var3,
         var4,
         var5,
         var6,
         var11,
         Math.max(1, var1.p(var10 > 0.5F ? 1.3 : 0.7)),
         Gx.mix(Gx.mix(Style.stroke, Gx.withAlpha(var2.rarity.color, 0.6F), var9), Style.ok, var10)
      );
      int var12 = var6 - var1.p(46.0);
      int var13 = var3 + (var5 - var12) / 2;
      int var14 = var4 + var1.p(6.0);
      Gx.glow(var3 + var5 / 2, var14 + var12 / 2, Math.round(var12 * 0.5F), Gx.withAlpha(var2.rarity.color, 0.18F + 0.12F * var9));
      boolean var16 = var2.slot == Cos.Slot.WINGS || var2.slot == Cos.Slot.CAPE || var2.slot == Cos.Slot.BACK;
      Gx.Img var17 = Thumbs.get("cos:" + var2.id, var12, var12, () -> Raster.render(var2, var12, var12, var16 ? 155.0F : 28.0F, 12.0F, 1.25F, true, 0));
      Gx.push();
      Gx.scaleAt(var3 + var5 / 2.0F, var14 + var12 / 2.0F, 1.0F + 0.05F * var9);
      if (var17 != null) {
         Gx.image(var17, var13, var14, var12, var12, -1);
      } else {
         EmotesPart.shimmer(var1, var13 + var12 / 4, var14 + var12 / 4, var12 / 2, var12 / 2);
      }

      Gx.pop();
      float var18 = var1.pf(5.8F);
      String var19 = var2.rarity.title.toUpperCase(Locale.ROOT);
      int var20 = Math.round(Gx.spacedWidth(var19, var18, 3, var1.pf(0.6F))) + var1.p(12.0);
      int var21 = var1.p(13.0);
      Gx.rect(var3 + var1.p(7.0), var4 + var1.p(7.0), var20, var21, var21 / 2, Gx.withAlpha(var2.rarity.color, 0.22F));
      Gx.textSpaced(var19, var3 + var1.p(7.0) + var20 / 2.0F, var4 + var1.p(7.0) + var21 / 2.0F, var18, 3, var2.rarity.color, var1.pf(0.6F));
      Gx.textCenter(
         Fonts.ellipsize(var2.name, 3, var1.pf(8.8F), var5 - var1.p(14.0)), var3 + var5 / 2.0F, var4 + var6 - var1.p(33.0), var1.pf(8.8F), 3, Style.text
      );
      int var22 = var3 + var1.p(10.0);
      int var23 = var4 + var6 - var1.p(24.0);
      int var24 = var5 - var1.p(20.0);
      int var25 = var1.p(18.0);
      boolean var26 = var1.hover(var22, var23, var24, var25);
      int var27 = var8
         ? Gx.mix(Gx.withAlpha(Style.ok, 0.22F), Gx.withAlpha(Style.danger, 0.25F), var26 ? 1.0F : 0.0F)
         : Gx.mix(Style.surface2, Style.accent, var26 ? 1.0F : 0.0F);
      Gx.rect(var22, var23, var24, var25, var25 / 2, var27);
      String var28 = var8 ? (var26 ? "ABLEGEN" : "AUSGERÜSTET") : "AUSRÜSTEN";
      int var29 = var8 ? (var26 ? Style.danger : Style.ok) : (var26 ? Style.accentText : Style.sub);
      if (var8 && !var26) {
         Gx.icon(
            "check",
            var22 + var24 / 2.0F - Gx.spacedWidth(var28, var1.pf(6.4F), 3, var1.pf(0.8F)) / 2.0F - var1.pf(7.0),
            var23 + var25 / 2.0F,
            var1.pf(8.0),
            var29
         );
      }

      Gx.textSpaced(var28, var22 + var24 / 2.0F + (var8 && !var26 ? var1.pf(4.0) : 0.0F), var23 + var25 / 2.0F, var1.pf(6.4F), 3, var29, var1.pf(0.8F));
      Kit.hit(var3, var4, var5, var6).click(() -> {
         Cos.toggle(var2);
         this.selected = var2;
         Sound.pop();
      });
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 == -1 || var1 < 0) {
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
