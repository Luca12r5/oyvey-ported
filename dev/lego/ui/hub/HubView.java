package dev.lego.ui.hub;

import dev.lego.ui.Art;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.MenuView;
import dev.lego.ui.Style;
import dev.lego.ui.UiSettings;
import dev.lego.ui.View;
import dev.lego.ui.Xmas;
import java.util.function.IntConsumer;

public final class HubView extends View {
   private HubView.Section section;
   private HubView.Part part;
   private long switchedAt = System.currentTimeMillis();

   public HubView() {
      this(HubView.Section.MODS);
   }

   public HubView(HubView.Section var1) {
      this.select(var1);
   }

   public HubView.Section section() {
      return this.section;
   }

   public void select(HubView.Section var1) {
      if (var1 == HubView.Section.HUD) {
         if (this.host != null) {
            this.host.closeThen(Env.openHudEditor);
            return;
         }

         var1 = HubView.Section.MODS;
      }

      if (var1 != this.section) {
         this.section = var1;
         this.switchedAt = System.currentTimeMillis();
         switch (var1) {
            case COSMETICS:
               this.part = new CosmeticsPart();
               break;
            case EMOTES:
               this.part = new EmotesPart();
               break;
            case GAMES:
               this.part = new GamesPart();
               break;
            case HUD:
            default:
               this.part = new ModsPart();
               break;
            case DESIGN:
               this.part = new DesignPart();
         }

         this.part.shown();
      }
   }

   public float sectionT(float var1) {
      return Math.min(1.0F, (float)(System.currentTimeMillis() - this.switchedAt) * UiSettings.anim() / var1);
   }

   @Override
   public float designW() {
      return 720.0F;
   }

   @Override
   public float designH() {
      return 440.0F;
   }

   @Override
   public float openMs() {
      return 380.0F;
   }

   @Override
   public void draw() {
      boolean var1 = this.host != null && this.host.closing();
      float var2 = var1 ? this.open : Ease.outQuint(this.open);
      Gx.fill(0, 0, this.W, this.H, Gx.rgba(0, 0.25F * this.open));
      int var3 = this.p(66.0);
      int var4 = this.p(10.0);
      int var5 = this.p(644.0);
      int var6 = this.p(440.0);
      int var7 = var3 + var4 + var5;
      int var8 = (this.W - var7) / 2;
      int var9 = (this.H - var6) / 2;
      int var10 = this.W / 2;
      int var11 = this.H / 2;
      Gx.pushAlpha(var2);
      Gx.push();
      Gx.scaleAt(var10, var11, 0.94F + 0.06F * var2);
      Gx.translate(0.0F, this.pf(10.0) * (1.0F - var2));
      Gx.push();
      Gx.translate(-this.pf(24.0) * (1.0F - var2), 0.0F);
      Gx.shadow(var8, var9, var3, var6, this.p(16.0), this.p(10.0), 0.45F);
      Gx.rect(var8, var9, var3, var6, this.p(16.0), Style.bg);
      Gx.outline(var8, var9, var3, var6, this.p(16.0), Math.max(1, this.p(0.6)), Style.stroke);
      int var12 = this.p(34.0);
      Gx.image(Art.brickImg(var12, Xmas.on() ? -1890757 : Style.accent), var8 + (var3 - var12) / 2, var9 + this.p(14.0), var12, var12, -1);
      Xmas.hat(var8 + var3 / 2.0F + this.p(2.0), var9 + this.p(20.0), this.p(30.0), 0.0F);
      Kit.hit(var8, var9 + this.p(10.0), var3, this.p(40.0)).click(() -> this.host.open(new MenuView()));
      HubView.Section[] var13 = HubView.Section.values();
      int var14 = this.p(54.0);
      int var15 = var9 + this.p(64.0);
      int var16 = this.section.ordinal();
      float var17 = Ease.to("hub:side", var15 + var16 * var14, 18.0F, var15 + var16 * var14);
      Gx.rect(var8 + this.p(8.0), Math.round(var17) + this.p(3.0), var3 - this.p(16.0), var14 - this.p(6.0), this.p(11.0), Gx.withAlpha(Style.accent, 0.16F));
      Gx.rect(var8 + this.p(2.0), Math.round(var17) + this.p(15.0), this.p(3.0), var14 - this.p(30.0), this.p(2.0), Style.accent);

      for (int var18 = 0; var18 < var13.length; var18++) {
         HubView.Section var19 = var13[var18];
         int var20 = var15 + var18 * var14;
         boolean var21 = this.hover(var8, var20, var3, var14);
         float var22 = Ease.to("hub:si" + var18, var21 ? 1.0F : 0.0F, 14.0F);
         boolean var23 = var19 == this.section;
         int var24 = var23 ? Style.accent : Gx.mix(Style.sub, Style.text, var22);
         Gx.icon(var19.icon, var8 + var3 / 2.0F, var20 + this.p(20.0), this.pf(19.0F + var22 * 1.5F), var24);
         Gx.textCenter(var19.label, var8 + var3 / 2.0F, var20 + this.p(40.0), this.pf(6.2F), 3, var23 ? Style.text : Gx.mix(Style.muted, Style.sub, var22));
         Kit.hit(var8, var20, var3, var14).click(() -> this.select(var19));
      }

      int var25 = var9 + var6 - this.p(46.0);
      boolean var26 = this.hover(var8, var25, var3, this.p(40.0));
      Gx.icon("x", var8 + var3 / 2.0F, var25 + this.p(20.0), this.pf(16.0), var26 ? Style.danger : Style.muted);
      Kit.hit(var8, var25, var3, this.p(40.0)).click(() -> this.host.close());
      Xmas.snowCap(var8, var9, var3, this.P);
      Gx.pop();
      int var27 = var8 + var3 + var4;
      Gx.shadow(var27, var9, var5, var6, this.p(16.0), this.p(12.0), 0.5F);
      Gx.rect(var27, var9, var5, var6, this.p(16.0), Style.bg);
      Gx.outline(var27, var9, var5, var6, this.p(16.0), Math.max(1, this.p(0.6)), Style.stroke);
      if (Xmas.on()) {
         Gx.gradientV(var27 + this.p(16.0), var9 + Math.max(1, this.p(0.6)), var5 - this.p(32.0), this.p(40.0), Gx.withAlpha(-1890757, 0.1F), 0);
      }

      float var28 = Ease.outCubic(this.sectionT(260.0F));
      Gx.pushAlpha(var28);
      Gx.push();
      Gx.translate(0.0F, this.pf(8.0) * (1.0F - var28));
      this.part.draw(this, var27, var9, var5, var6);
      Gx.pop();
      Gx.popAlpha();
      Xmas.snowCap(var27, var9, var5, this.P);
      Xmas.lights(var8 - this.p(10.0), var9 - this.p(34.0), var27 + var5 + this.p(10.0), this.P, this.p(14.0), 0);
      Gx.pop();
      Gx.popAlpha();
   }

   @Override
   public boolean key(int var1, int var2) {
      return this.part.key(var1, var2);
   }

   @Override
   public boolean chr(String var1) {
      return this.part.chr(var1);
   }

   @Override
   public void tick() {
      this.part.tick();
   }

   public int header(int var1, int var2, String var3, String var4) {
      Gx.text(var3, var1, var2, this.pf(17.0), 3, Style.text);
      if (var4 != null) {
         Gx.text(var4, var1, var2 + this.pf(24.0), this.pf(8.2F), 1, Style.sub);
      }

      return var2 + this.p(var4 != null ? 44.0 : 28.0);
   }

   public void searchBox(String var1, int var2, int var3, int var4, int var5, String var6, boolean var7) {
      boolean var8 = this.hover(var2, var3, var4, var5);
      float var9 = Ease.to("sb:" + var1, var7 ? 1.0F : (var8 ? 0.4F : 0.0F), 14.0F);
      Gx.rect(var2, var3, var4, var5, var5 / 2, Gx.mix(Style.surface, Style.surface2, var9));
      Gx.outline(var2, var3, var4, var5, var5 / 2, Math.max(1, this.p(0.7)), Gx.mix(Style.stroke, Style.accent, var9));
      Gx.icon("search", var2 + this.p(15.0), var3 + var5 / 2.0F, this.pf(11.0), Gx.mix(Style.muted, Style.accent, var9));
      float var10 = this.pf(8.4F);
      if (var6.isEmpty()) {
         Gx.textMid("Suchen…", var2 + this.p(27.0), var3 + var5 / 2.0F, var10, 1, Style.muted);
      } else {
         String var11 = Fonts.ellipsize(var6, 2, var10, var4 - this.p(50.0));
         float var12 = Gx.textMid(var11, var2 + this.p(27.0), var3 + var5 / 2.0F, var10, 2, Style.text);
         if (var7 && System.currentTimeMillis() / 500L % 2L == 0L) {
            Gx.fill(Math.round(var2 + this.p(28.0) + var12), var3 + var5 / 2 - this.p(6.0), Math.max(1, this.p(0.9)), this.p(12.0), Style.accent);
         }

         boolean var13 = this.hover(var2 + var4 - var5, var3, var5, var5);
         Gx.icon("x", var2 + var4 - var5 / 2.0F, var3 + var5 / 2.0F, this.pf(9.0), var13 ? Style.text : Style.muted);
      }
   }

   public void tabs(String var1, int var2, int var3, String[] var4, int var5, IntConsumer var6) {
      float var7 = this.pf(7.8F);
      float var8 = this.pf(1.1F);
      int var9 = this.p(12.0);
      int var10 = this.p(26.0);
      int var11 = this.p(4.0);
      int[] var12 = new int[var4.length];
      int[] var13 = new int[var4.length];
      int var14 = var2;

      for (int var15 = 0; var15 < var4.length; var15++) {
         int var16 = Math.round(Gx.spacedWidth(var4[var15], var7, 3, var8)) + var9 * 2;
         var12[var15] = var14;
         var13[var15] = var16;
         var14 += var16 + var11;
      }

      float var23 = Ease.to("tabx:" + var1, var12[var5], 18.0F, var12[var5]);
      float var24 = Ease.to("tabw:" + var1, var13[var5], 18.0F, var13[var5]);
      Gx.rect(Math.round(var23), var3, Math.round(var24), var10, var10 / 2, Style.accent);

      for (int var17 = 0; var17 < var4.length; var17++) {
         boolean var18 = this.hover(var12[var17], var3, var13[var17], var10) && var17 != var5;
         float var19 = Ease.to("tabh:" + var1 + var17, var18 ? 1.0F : 0.0F, 14.0F);
         if (var19 > 0.01F) {
            Gx.rect(var12[var17], var3, var13[var17], var10, var10 / 2, Gx.withAlpha(Style.surfaceHover, var19));
         }

         float var20 = Math.max(0.0F, 1.0F - Math.abs(var23 - var12[var17]) / Math.max(1, var13[var17]));
         int var21 = Gx.mix(Gx.mix(Style.sub, Style.text, var19), Style.accentText, var20);
         Gx.textSpaced(var4[var17], var12[var17] + var13[var17] / 2.0F, var3 + var10 / 2.0F, var7, 3, var21, var8);
         int var22 = var17;
         Kit.hit(var12[var17], var3, var13[var17], var10).click(() -> var6.accept(var22));
      }
   }

   public interface Part {
      void draw(HubView var1, int var2, int var3, int var4, int var5);

      default boolean key(int var1, int var2) {
         return false;
      }

      default boolean chr(String var1) {
         return false;
      }

      default void shown() {
      }

      default void tick() {
      }
   }

   public static enum Section {
      MODS("mods", "MODS"),
      COSMETICS("shirt", "KOSMETIK"),
      EMOTES("emote", "EMOTES"),
      GAMES("gamepad", "SPIELE"),
      HUD("layout", "HUD"),
      DESIGN("palette", "DESIGN");

      public final String icon;
      public final String label;

      private Section(String nullxx, String nullxxx) {
         this.icon = nullxx;
         this.label = nullxxx;
      }
   }
}
