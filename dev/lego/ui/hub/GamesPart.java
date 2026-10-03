package dev.lego.ui.hub;

import dev.lego.games.Arcade;
import dev.lego.games.GameInfo;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Style;
import java.util.Locale;

public final class GamesPart implements HubView.Part {
   private float scroll = 0.0F;
   private float target = 0.0F;
   private float max = 0.0F;

   @Override
   public void shown() {
      Arcade.registerAll();
   }

   @Override
   public void draw(HubView var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.p(22.0);
      int var7 = var1.header(var2 + var6, var3 + var1.p(20.0), "Minispiele", GameInfo.ALL.size() + " Spiele für Zwischendurch – mit Rekorden");
      int var8 = var7 + var1.p(4.0);
      int var9 = var3 + var5 - var1.p(16.0) - var8;
      byte var10 = 3;
      int var11 = var1.p(12.0);
      int var12 = (var4 - var6 * 2 - var11 * (var10 - 1)) / var10;
      int var13 = var1.p(132.0);
      int var14 = GameInfo.ALL.size();
      int var15 = (var14 + var10 - 1) / var10;
      this.max = Math.max(0, var15 * (var13 + var11) - var11 - var9 + var1.p(4.0));
      this.target = Math.max(0.0F, Math.min(this.max, this.target));
      this.scroll = this.scroll + (this.target - this.scroll) * (1.0F - (float)Math.exp(-Gx.dt * 16.0F));
      Gx.clip(var2 + var6 - var1.p(6.0), var8, var4 - var6 * 2 + var1.p(12.0), var9);
      Kit.clip(var2 + var6 - var1.p(6.0), var8, var4 - var6 * 2 + var1.p(12.0), var9);
      Kit.hit(var2, var8, var4, var9).scroll(var2x -> this.target = this.target - var2x.floatValue() * var1.p(60.0));
      float var16 = var1.sectionT(650.0F);

      for (int var17 = 0; var17 < var14; var17++) {
         int var18 = var2 + var6 + var17 % var10 * (var12 + var11);
         int var19 = Math.round(var8 + var1.p(2.0) + var17 / var10 * (var13 + var11) - this.scroll);
         if (var19 + var13 >= var8 && var19 <= var8 + var9) {
            float var20 = Ease.outCubic(Ease.clamp((var16 * 650.0F - Math.min(var17, 10) * 30) / 260.0F));
            Gx.pushAlpha(var20);
            Gx.push();
            Gx.translate(0.0F, var1.pf(10.0) * (1.0F - var20));
            this.card(var1, GameInfo.ALL.get(var17), var18, var19, var12, var13);
            Gx.pop();
            Gx.popAlpha();
         }
      }

      Kit.unclip();
      Gx.unclip();
      if (this.max > 0.0F) {
         int var21 = var9 - var1.p(8.0);
         int var22 = Math.max(var1.p(24.0), Math.round(var21 * var9 / (var9 + this.max)));
         int var23 = var8 + var1.p(4.0) + Math.round((var21 - var22) * (this.scroll / this.max));
         Gx.rect(var2 + var4 - var1.p(10.0), var23, var1.p(3.0), var22, var1.p(2.0), Style.strokeHi);
      }
   }

   private void card(HubView var1, GameInfo var2, int var3, int var4, int var5, int var6) {
      boolean var7 = var1.hover(var3, var4, var5, var6);
      float var8 = Ease.to("game:" + var2.id, var7 ? 1.0F : 0.0F, 14.0F);
      int var9 = var1.p(13.0);
      if (var8 > 0.01F) {
         Gx.shadow(var3, var4, var5, var6, var9, var1.p(8.0), 0.35F * var8);
      }

      Gx.rect(var3, var4, var5, var6, var9, Gx.mix(Style.surface, Style.surfaceHover, var8));
      int var10 = var1.p(64.0);
      Gx.rectV(var3, var4, var5, var10 + var9, var9, Gx.mix(var2.color, -16777216, 0.35F), Gx.mix(var2.color, -16777216, 0.75F));
      Gx.fill(var3, var4 + var10, var5, var9, Gx.mix(Style.surface, Style.surfaceHover, var8));

      for (int var11 = 0; var11 < 5; var11++) {
         float var12 = var3 + var5 * (0.08F + var11 * 0.21F);
         float var13 = var4 + var1.pf(8.0) + var11 % 2 * var1.pf(22.0);
         Gx.rect(Math.round(var12), Math.round(var13), var1.p(18.0), var1.p(9.0), var1.p(2.0), Gx.withAlpha(-1, 0.05F + 0.02F * (var11 % 3)));
      }

      Gx.glow(var3 + var5 / 2, var4 + var10 / 2, Math.round(var10 * 0.9F), Gx.withAlpha(var2.color, 0.35F + 0.2F * var8));
      Gx.push();
      Gx.scaleAt(var3 + var5 / 2.0F, var4 + var10 / 2.0F, 1.0F + 0.12F * var8);
      Gx.icon(var2.icon, var3 + var5 / 2.0F, var4 + var10 / 2.0F, var1.pf(28.0), -1);
      Gx.pop();
      Gx.outline(var3, var4, var5, var6, var9, Math.max(1, var1.p(0.7)), Gx.mix(Style.stroke, Gx.withAlpha(var2.color, 0.8F), var8));
      Gx.text(Fonts.ellipsize(var2.name, 3, var1.pf(10.0), var5 - var1.p(24.0)), var3 + var1.p(12.0), var4 + var10 + var1.pf(8.0), var1.pf(10.0), 3, Style.text);
      Gx.text(
         Fonts.ellipsize(var2.tagline, 1, var1.pf(7.4F), var5 - var1.p(24.0)), var3 + var1.p(12.0), var4 + var10 + var1.pf(24.0), var1.pf(7.4F), 1, Style.sub
      );
      String var15 = GameInfo.hasBest(var2.id) ? "Rekord: " + format(var2, GameInfo.best(var2.id)) : "Noch nicht gespielt";
      Gx.icon("trophy", var3 + var1.p(17.0), var4 + var6 - var1.pf(14.0), var1.pf(9.0), GameInfo.hasBest(var2.id) ? -15043 : Style.muted);
      Gx.textMid(var15, var3 + var1.p(26.0), var4 + var6 - var1.pf(14.0), var1.pf(7.2F), 2, Style.sub);
      int var16 = var1.p(24.0);
      int var17 = var3 + var5 - var16 - var1.p(10.0);
      int var14 = var4 + var6 - var16 - var1.p(8.0);
      Gx.circle(var17 + var16 / 2, var14 + var16 / 2, var16 / 2, Gx.mix(Style.surface2, var2.color, 0.35F + 0.65F * var8));
      Gx.icon("play", var17 + var16 / 2.0F + var1.pf(1.0), var14 + var16 / 2.0F, var1.pf(10.0), var8 > 0.5F ? -15724528 : Style.text);
      Kit.hit(var3, var4, var5, var6).click(() -> var1.host.open(var2.factory.get()));
   }

   static String format(GameInfo var0, long var1) {
      if (!var0.higherIsBetter) {
         return var1 + " ms";
      } else {
         return var0.id.equals("cps") ? String.format(Locale.ROOT, "%.1f CPS", var1 / 10.0) : String.valueOf(var1);
      }
   }
}
