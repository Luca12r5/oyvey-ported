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
      float var1 = this.open;
      boolean var2 = this.host != null && this.host.closing();
      Gx.gradientV(0, 0, this.W, this.H, Gx.rgba(0, (Xmas.on() ? 0.1F : 0.3F) * var1), Gx.rgba(0, (Xmas.on() ? 0.3F : 0.55F) * var1));
      int var3 = this.W / 2;
      int var4 = this.H / 2 - this.p(46.0);
      float var5 = var2 ? var1 : Ease.outCubic(Ease.stagger(var1, 0, 0.0F, 0.7F));
      Gx.pushAlpha(var5);
      Gx.push();
      Gx.translate(0.0F, -this.pf(30.0) * (1.0F - var5));
      Xmas.lights(-this.p(10.0), this.p(4.0), this.W + this.p(10.0), this.P, this.p(16.0), 3);
      long var6 = System.currentTimeMillis();
      int[] var8 = new int[]{-1890757, -736942, -12867329, -14703780, -5022465, -1890757};
      float[] var9 = new float[]{0.08F, 0.19F, 0.31F, 0.69F, 0.81F, 0.92F};

      for (int var10 = 0; var10 < var9.length && Xmas.on(); var10++) {
         float var11 = this.pf(50 + var10 * 37 % 60);
         float var12 = (float)Math.sin(var6 / 1100.0 + var10 * 1.3) * 0.08F;
         float var13 = this.W * var9[var10];
         float var14 = var13 + (float)Math.sin(var12) * var11;
         float var15 = this.p(6.0) + (float)Math.cos(var12) * var11;
         int var16 = Math.max(1, this.p(5.0));
         int var17 = Math.max(1, Math.round(var11 / var16));

         for (int var18 = 0; var18 < var17; var18++) {
            float var19 = (float)var18 / var17;
            Gx.fill(
               Math.round(var13 + (var14 - var13) * var19),
               Math.round(this.p(6.0) + (var15 - this.p(6.0)) * var19),
               Math.max(1, this.p(0.6)),
               var16,
               1726536104
            );
         }

         Xmas.bauble(var14, var15 + this.pf(10.0), this.pf(11 + var10 % 2 * 3), var8[var10]);
      }

      Gx.pop();
      Gx.popAlpha();
      float var44 = var2 ? var1 : Ease.outCubic(Ease.stagger(var1, 0, 0.0F, 0.55F));
      float var45 = var2 ? 0.94F + 0.06F * var1 : 0.86F + 0.14F * Ease.outBack(Ease.stagger(var1, 0, 0.0F, 0.6F));
      float var46 = 0.5F + 0.5F * (float)Math.sin(var6 / 900.0);
      int var47 = Xmas.on() ? -1890757 : Style.accent;
      Gx.pushAlpha(var44);
      Gx.glow(var3, var4, this.p(170.0), Gx.withAlpha(var47, 0.1F + 0.05F * var46));
      Gx.push();
      Gx.translate(0.0F, this.pf(14.0) * (1.0F - var44));
      Gx.scaleAt(var3, var4, var45);
      float var48 = this.pf(44.0);
      float var49 = Gx.width("LEGO", var48, 3);
      float var50 = Gx.width("CLIENT", var48, 0);
      int var51 = this.p(62.0);
      int var52 = this.p(14.0);
      float var53 = var49 + var52 + var51 + var52 + var50;
      float var20 = var3 - var53 / 2.0F;
      Gx.textMid("LEGO", var20, var4, var48, 3, -1);
      var20 += var49 + var52;
      float var21 = (float)Math.sin(var6 / 700.0) * this.pf(2.2F);
      Gx.glow(Math.round(var20 + var51 / 2.0F), var4, var51, Gx.withAlpha(var47, 0.35F));
      Gx.image(Art.brickImg(var51, var47), Math.round(var20), Math.round(var4 - var51 / 2.0F - this.pf(3.0) + var21), var51, var51, -1);
      Xmas.hat(var20 + var51 / 2.0F + this.pf(3.0), var4 - var51 / 2.0F + this.pf(12.0) + var21, var51 * 0.95F, 0.0F);
      var20 += var51 + var52;
      Gx.textMid("CLIENT", var20, var4, var48, 0, -1);
      Gx.pop();
      if (Xmas.on()) {
         Gx.textSpaced("WINTER  EDITION", var3, var4 + this.pf(38.0), this.pf(8.4F), 3, -736942, this.pf(3.2F));
         Gx.textSpaced(
            Xmas.countdown().toUpperCase(Locale.ROOT) + "   •   v" + Env.version, var3, var4 + this.pf(54.0), this.pf(6.6F), 2, -1711276033, this.pf(1.4F)
         );
      } else {
         Gx.textSpaced("v" + Env.version + "   •   MINECRAFT " + Env.mcVersion, var3, var4 + this.pf(40.0), this.pf(7.4F), 2, -1711276033, this.pf(1.6F));
      }

      Gx.popAlpha();
      float var22 = var2 ? var1 : Ease.outCubic(Ease.stagger(var1, 1, 0.09F, 0.5F));
      int var23 = this.p(300.0);
      int var24 = this.p(40.0);
      int var25 = var3 - var23 / 2;
      int var26 = var4 + this.p(74.0);
      boolean var27 = this.hover(var25, var26, var23, var24);
      float var28 = Ease.to("menu:mod", var27 ? 1.0F : 0.0F, 14.0F);
      Gx.pushAlpha(var22);
      Gx.push();
      Gx.translate(0.0F, this.pf(16.0) * (1.0F - var22));
      Gx.scaleAt(var3, var26 + var24 / 2.0F, 1.0F + 0.025F * var28);
      Gx.shadow(var25, var26, var23, var24, this.p(10.0), this.p(7.0), 0.45F);
      if (Xmas.on()) {
         if (var28 > 0.01F) {
            Gx.glow(var3, var26 + var24 / 2, this.p(150.0), Gx.withAlpha(-736942, 0.14F * var28));
         }

         Gx.rectV(var25, var26, var23, var24, this.p(10.0), Gx.mix(-1099186, -42388, var28), -6089686);
         Gx.outline(var25, var26, var23, var24, this.p(10.0), Math.max(1, this.p(1.0)), Gx.mix(1442830730, -736942, var28));
         Xmas.snowCap(var25, var26, var23, this.P * 0.85F);
         Gx.icon("gift", var25 + this.p(28.0), var26 + var24 / 2.0F + this.pf(1.0), this.pf(14.0), -1);
         Gx.textSpaced("MOD MENU", var3, var26 + var24 / 2.0F + this.pf(1.0), this.pf(11.5), 3, -1, this.pf(2.4F));
      } else {
         Gx.rect(var25, var26, var23, var24, this.p(8.0), Gx.mix(-653126888, -434496478, var28));
         Gx.outline(var25, var26, var23, var24, this.p(8.0), Math.max(1, this.p(0.8)), Gx.mix(654311423, Style.accent, var28));
         if (var28 > 0.01F) {
            Gx.glow(var3, var26 + var24 / 2, this.p(120.0), Gx.withAlpha(Style.accent, 0.1F * var28));
         }

         Gx.textSpaced("MOD MENU", var3, var26 + var24 / 2.0F, this.pf(11.0), 3, Gx.mix(-1, Style.accent, var28 * 0.6F), this.pf(2.2F));
      }

      Gx.pop();
      Gx.popAlpha();
      Kit.hit(var25, var26, var23, var24).click(() -> this.host.open(new HubView(HubView.Section.MODS)));
      int var29 = this.p(46.0);
      int var30 = this.p(12.0);
      int var31 = var3 - (QUICK.length * var29 + (QUICK.length - 1) * var30) / 2;
      int var32 = var26 + var24 + this.p(18.0);
      int[][] var33 = new int[][]{
         {-15037874, -736942},
         {-2743752, -736942},
         {-15037874, -1890757},
         {-14795142, -736942},
         {-1527238, -4909780},
         {-9752656, -726576},
         {-15766677, -736942}
      };
      String var34 = null;
      int var35 = 0;

      for (int var36 = 0; var36 < QUICK.length; var36++) {
         float var37 = var2 ? var1 : Ease.outBack(Ease.stagger(var1, var36 + 2, 0.06F, 0.45F));
         int var38 = var31 + var36 * (var29 + var30);
         boolean var39 = this.hover(var38, var32 - this.p(8.0), var29, var29 + this.p(8.0));
         float var40 = Ease.to("menu:q" + var36, var39 ? 1.0F : 0.0F, 14.0F);
         Gx.pushAlpha(Math.min(1.0F, Math.max(0.0F, var37)));
         Gx.push();
         Gx.translate(0.0F, this.pf(18.0) * (1.0F - var37));
         if (Xmas.on()) {
            float var41 = var39 ? (float)Math.sin(var6 / 90.0) * this.pf(0.8F) : 0.0F;
            Gx.translate(var41, 0.0F);
            Xmas.gift(var38, var32, var29, var29, var33[var36][0], var33[var36][1], this.pf(6.0) * var40, this.P);
            float var42 = var32 + var29 * 0.64F - this.pf(6.0) * var40;
            Gx.circle(var38 + var29 / 2, Math.round(var42), this.p(12.0), -419430401);
            Gx.icon(QUICK[var36][0], var38 + var29 / 2.0F, var42, this.pf(14.0), var33[var36][0]);
         } else {
            Gx.scaleAt(var38 + var29 / 2.0F, var32 + var29 / 2.0F, 1.0F + 0.06F * var40);
            Gx.shadow(var38, var32, var29, var29, this.p(9.0), this.p(5.0), 0.35F);
            Gx.rect(var38, var32, var29, var29, this.p(9.0), Gx.mix(-653126888, -266592731, var40));
            Gx.outline(var38, var32, var29, var29, this.p(9.0), Math.max(1, this.p(0.8)), Gx.mix(587202559, Style.accent, var40));
            Gx.icon(QUICK[var36][0], var38 + var29 / 2.0F, var32 + var29 / 2.0F, this.pf(21.0), Gx.mix(-1512980, Style.accent, var40));
         }

         Gx.pop();
         Gx.popAlpha();
         String var61 = QUICK[var36][2];
         Kit.hit(var38, var32 - this.p(8.0), var29, var29 + this.p(8.0)).click(() -> this.openSection(var61));
         if (var39) {
            var34 = QUICK[var36][1];
            var35 = var38 + var29 / 2;
         }
      }

      if (var34 != null) {
         Kit.tooltip(var34, var35, var32 - this.p(14.0), this.P);
      }

      float var56 = var2 ? var1 : Ease.outCubic(Ease.stagger(var1, 7, 0.06F, 0.5F));
      Gx.pushAlpha(var56);
      Gx.textSpaced("ESC  ODER  RECHTS-SHIFT  ZUM  SCHLIESSEN", var3, this.H - this.pf(22.0), this.pf(6.6F), 2, 1728053247, this.pf(1.2F));
      String var57 = Env.nowPlaying.get();
      if (var57 != null && !var57.isEmpty()) {
         float var58 = this.pf(8.0);
         String var59 = Fonts.ellipsize(var57, 2, var58, this.pf(220.0));
         int var60 = Math.round(Gx.width(var59, var58, 2) + this.pf(34.0));
         int var62 = this.p(24.0);
         int var63 = this.p(16.0);
         int var43 = this.H - var62 - this.p(14.0);
         Gx.rect(var63, var43, var60, var62, var62 / 2, -1290661096);
         Gx.outline(var63, var43, var60, var62, var62 / 2, Math.max(1, this.p(0.6)), 536870911);
         Gx.circle(var63 + this.p(12.0), var43 + var62 / 2, this.p(6.5), -14756000);
         Gx.icon("music", var63 + this.p(12.0), var43 + var62 / 2.0F, this.pf(7.5), -16053493);
         Gx.textMid(var59, var63 + this.p(24.0), var43 + var62 / 2.0F, var58, 2, -419430401);
      }

      Gx.popAlpha();
   }

   private void openSection(String var1) {
      if ("HUD".equals(var1)) {
         this.host.closeThen(Env.openHudEditor);
      } else if ("SPOTIFY".equals(var1)) {
         this.host.open(new SpotifyView());
      } else {
         this.host.open(new HubView(HubView.Section.valueOf(var1)));
      }
   }

   @Override
   public boolean key(int var1, int var2) {
      if (var1 == 344) {
         this.host.close();
         return true;
      } else {
         return false;
      }
   }
}
