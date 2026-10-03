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

public final class EmotesPart implements HubView.Part {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;
   private String toast = null;
   private long toastAt = 0L;

   @Override
   public void draw(HubView var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.p(22.0);
      int var7 = var1.header(var2 + var6, var3 + var1.p(20.0), "Emotes", "Klick = abspielen   •   Stern = ins Emote-Rad (Taste B halten)");
      int var8 = var1.p(176.0);
      int var9 = var2 + var6;
      int var10 = var4 - var6 * 2 - var8 - var1.p(16.0);
      int var11 = var7 + var1.p(4.0);
      int var12 = var3 + var5 - var1.p(16.0) - var11;
      byte var13 = 4;
      int var14 = var1.p(10.0);
      int var15 = (var10 - var14 * (var13 - 1)) / var13;
      int var16 = var1.p(118.0);
      int var17 = EmoteLib.ALL.size();
      int var18 = (var17 + var13 - 1) / var13;
      this.max = Math.max(0, var18 * (var16 + var14) - var14 - var12 + var1.p(4.0));
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var9 - var1.p(6.0), var11, var10 + var1.p(12.0), var12);
      Kit.clip(var9 - var1.p(6.0), var11, var10 + var1.p(12.0), var12);
      Kit.hit(var9, var11, var10, var12).scroll(var2x -> this.target = this.target - var2x.floatValue() * var1.p(50.0));
      float var19 = var1.sectionT(600.0F);

      for (int var20 = 0; var20 < var17; var20++) {
         int var21 = var9 + var20 % var13 * (var15 + var14);
         int var22 = Math.round(var11 + var1.p(2.0) + var20 / var13 * (var16 + var14) - this.scroll);
         if (var22 + var16 >= var11 && var22 <= var11 + var12) {
            float var23 = Ease.outCubic(Ease.clamp((var19 * 600.0F - Math.min(var20, 10) * 18) / 250.0F));
            Gx.pushAlpha(var23);
            this.card(var1, EmoteLib.ALL.get(var20), var21, var22, var15, var16);
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (this.max > 0.0F) {
         int var33 = var12 - var1.p(8.0);
         int var35 = Math.max(var1.p(24.0), Math.round(var33 * var12 / (var12 + this.max)));
         int var36 = var11 + var1.p(4.0) + Math.round((var33 - var35) * (this.scroll / this.max));
         Gx.rect(var9 + var10 + var1.p(4.0), var36, var1.p(3.0), var35, var1.p(2.0), Style.strokeHi);
      }

      int var34 = var2 + var4 - var6 - var8;
      Gx.rect(var34, var11, var8, var12, var1.p(14.0), Style.surface);
      Gx.outline(var34, var11, var8, var12, var1.p(14.0), Math.max(1, var1.p(0.6)), Style.stroke);
      Gx.text("Emote-Rad", var34 + var1.p(14.0), var11 + var1.p(12.0), var1.pf(10.0), 3, Style.text);
      Gx.text(EmoteLib.WHEEL.size() + " / 8 Plätze", var34 + var1.p(14.0), var11 + var1.p(29.0), var1.pf(7.6F), 1, Style.sub);
      int var37 = Math.min(var8 / 2 - var1.p(14.0), var1.p(72.0));
      int var38 = var34 + var8 / 2;
      int var24 = var11 + var1.p(52.0) + var37;
      Gx.circle(var38, var24, var37, Style.surface2);
      Gx.circle(var38, var24, Math.round(var37 * 0.36F), Style.bg);

      for (int var25 = 0; var25 < 8; var25++) {
         double var26 = (-Math.PI / 2) + (Math.PI * 2) * var25 / 8.0;
         float var28 = var38 + (float)Math.cos(var26) * var37 * 0.69F;
         float var29 = var24 + (float)Math.sin(var26) * var37 * 0.69F;
         if (var25 < EmoteLib.WHEEL.size()) {
            EmoteLib.Emote var30 = EmoteLib.get(EmoteLib.WHEEL.get(var25));
            boolean var31 = var1.hover(Math.round(var28 - var1.pf(14.0)), Math.round(var29 - var1.pf(14.0)), var1.p(28.0), var1.p(28.0));
            Gx.circle(Math.round(var28), Math.round(var29), var1.p(13.0), var31 ? Gx.withAlpha(Style.danger, 0.25F) : Gx.withAlpha(Style.accent, 0.18F));
            Gx.icon(var31 ? "x" : var30.icon, var28, var29, var1.pf(12.0), var31 ? Style.danger : Style.accent);
            String var32 = var30.id;
            Kit.hit(Math.round(var28 - var1.pf(14.0)), Math.round(var29 - var1.pf(14.0)), var1.p(28.0), var1.p(28.0)).click(() -> EmoteLib.toggleWheel(var32));
         } else {
            Gx.ringAt(Math.round(var28), Math.round(var29), var1.p(12.0), Math.max(1, var1.p(0.8)), Style.strokeHi);
            Gx.icon("plus", var28, var29, var1.pf(9.0), Style.muted);
         }
      }

      Gx.icon("emote", var38, var24, var1.pf(18.0), Style.sub);
      int var39 = var24 + var37 + var1.p(16.0);
      String[] var40 = new String[]{"Taste B halten = Rad öffnen", "Maus auf Emote, loslassen", "Bewegen bricht Loop-Emotes ab"};

      for (String var46 : var40) {
         Gx.circle(var34 + var1.p(18.0), var39 + var1.p(5.0), var1.p(2.0), Style.accent);
         Gx.text(Fonts.ellipsize(var46, 1, var1.pf(7.4F), var8 - var1.p(34.0)), var34 + var1.p(26.0), var39, var1.pf(7.4F), 1, Style.sub);
         var39 += var1.p(15.0);
      }

      if (this.toast != null && System.currentTimeMillis() - this.toastAt < 1800L) {
         float var41 = Math.min(1.0F, (float)(1800L - (System.currentTimeMillis() - this.toastAt)) / 300.0F);
         Gx.pushAlpha(var41);
         float var43 = var1.pf(8.4F);
         int var45 = Math.round(Gx.width(this.toast, var43, 2)) + var1.p(28.0);
         int var47 = var1.p(26.0);
         int var48 = var2 + (var4 - var45) / 2;
         int var49 = var3 + var5 - var47 - var1.p(18.0);
         Gx.shadow(var48, var49, var45, var47, var47 / 2, var1.p(6.0), 0.4F);
         Gx.rect(var48, var49, var45, var47, var47 / 2, Style.light ? -15000542 : -789258);
         Gx.textCenter(this.toast, var48 + var45 / 2.0F, var49 + var47 / 2.0F, var43, 2, Style.light ? -1 : -15658474);
         Gx.popAlpha();
      }
   }

   private void card(HubView var1, EmoteLib.Emote var2, int var3, int var4, int var5, int var6) {
      boolean var7 = var1.hover(var3, var4, var5, var6);
      float var8 = Ease.to("emo:" + var2.id, var7 ? 1.0F : 0.0F, 14.0F);
      boolean var9 = EmoteLib.inWheel(var2.id);
      Gx.push();
      Gx.translate(0.0F, -var1.pf(2.0) * var8);
      if (var8 > 0.01F) {
         Gx.shadow(var3, var4, var5, var6, var1.p(12.0), var1.p(6.0), 0.3F * var8);
      }

      Gx.rect(var3, var4, var5, var6, var1.p(12.0), Gx.mix(Style.surface, Style.surfaceHover, var8));
      Gx.outline(var3, var4, var5, var6, var1.p(12.0), Math.max(1, var1.p(0.7)), Gx.mix(Style.stroke, Gx.withAlpha(Style.accent, 0.7F), var8));
      int var10 = var6 - var1.p(34.0);
      int var11 = var3 + (var5 - var10) / 2;
      int var12 = var4 + var1.p(4.0);
      Gx.glow(var3 + var5 / 2, var12 + var10 / 2, Math.round(var10 * 0.55F), Gx.withAlpha(Style.accent, 0.1F + 0.12F * var8));
      int var13 = Math.max(16, var10);
      Gx.Img var14 = Thumbs.get(
         "emote:" + var2.id, var13, var13, () -> Raster.renderPose(var2.pose(Math.min(var2.duration * 0.4F, 1.1F)), var13, var13, 22.0F, 8.0F, 0)
      );
      if (var14 != null) {
         Gx.image(var14, var11, var12, var10, var10, -1);
      } else {
         shimmer(var1, var11 + var10 / 4, var12 + var10 / 4, var10 / 2, var10 / 2);
      }

      Gx.textCenter(
         Fonts.ellipsize(var2.name, 3, var1.pf(8.6F), var5 - var1.p(30.0)), var3 + var5 / 2.0F, var4 + var6 - var1.p(16.0), var1.pf(8.6F), 3, Style.text
      );
      if (var2.loop) {
         Gx.textCenter("LOOP", var3 + var5 / 2.0F, var4 + var6 - var1.p(5.0), var1.pf(5.6F), 3, Style.muted);
      } else if (EmoteLib.isCutscene(var2.id)) {
         Gx.textCenter("CUTSCENE", var3 + var5 / 2.0F, var4 + var6 - var1.p(5.0), var1.pf(5.6F), 3, Style.accent);
      }

      int var15 = var1.p(22.0);
      int var16 = var3 + var5 - var15 - var1.p(5.0);
      int var17 = var4 + var1.p(5.0);
      boolean var18 = var1.hover(var16, var17, var15, var15);
      float var19 = Ease.to("emofav:" + var2.id, var9 ? 1.0F : 0.0F, 16.0F);
      Gx.circle(var16 + var15 / 2, var17 + var15 / 2, var15 / 2, Gx.mix(Gx.withAlpha(Style.bg, 0.6F), Gx.withAlpha(-15043, 0.22F), var19));
      Gx.push();
      Gx.scaleAt(var16 + var15 / 2.0F, var17 + var15 / 2.0F, 1.0F + 0.25F * (1.0F - Math.abs(var19 * 2.0F - 1.0F)) + (var18 ? 0.08F : 0.0F));
      Gx.icon(var9 ? "star-fill" : "star", var16 + var15 / 2.0F, var17 + var15 / 2.0F, var1.pf(11.0), var9 ? -15043 : (var18 ? Style.text : Style.muted));
      Gx.pop();
      Gx.pop();
      Kit.hit(var3, var4, var5, var6).click(() -> {
         int var2x = EmoteLib.index(var2.id);
         var1.host.closeThen(() -> Env.playEmote.accept(var2x));
      });
      Kit.hit(var16, var17, var15, var15).click(() -> {
         if (!EmoteLib.toggleWheel(var2.id)) {
            this.toast = "Emote-Rad ist voll (max. 8)";
            this.toastAt = System.currentTimeMillis();
         } else {
            Sound.pop();
         }
      });
   }

   static void shimmer(HubView var0, int var1, int var2, int var3, int var4) {
      float var5 = (float)(System.currentTimeMillis() % 1200L) / 1200.0F;
      Gx.rect(var1, var2, var3, var4, var0.p(8.0), Gx.withAlpha(Style.surface2, 0.6F + 0.4F * (float)Math.sin(var5 * Math.PI)));
   }
}
