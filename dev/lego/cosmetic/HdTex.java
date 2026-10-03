package dev.lego.cosmetic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Ellipse2D.Double;
import java.util.Random;

final class HdTex {
   private HdTex() {
   }

   static double frac(double var0) {
      return var0 - Math.floor(var0);
   }

   static CTex.T paint(String var0) {
      switch (var0) {
         case "hd_felt":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 12.0, var2 * 12.0, 12, 12, 4, 1001);
               double var8 = Capes.fbm(var0x * 64.0, var2 * 64.0, 64, 64, 2, 1002);
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.12 * var6 + 0.08 * var8 + 0.03 * (Capes.hash(var4, var5, 1003) - 0.5));
            });
         case "hd_velvet":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 8.0, var2 * 8.0, 8, 8, 3, 1011);
               double var8 = Capes.fbm(var0x * 48.0, var2 * 48.0, 48, 48, 2, 1012);
               return 0xFF000000 | Capes.shade(16777215, 0.74 + 0.16 * var6 + 0.1 * var8);
            });
         case "hd_cotton":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               boolean var6 = (var4 / 2 + var5 / 2) % 2 == 0;
               double var7 = Capes.fbm(var0x * 10.0, var2 * 10.0, 10, 10, 3, 1021);
               return 0xFF000000 | Capes.shade(16777215, (var6 ? 0.97 : 0.9) * (0.9 + 0.1 * var7));
            });
         case "hd_knit":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = var0x * 24.0;
               double var8 = var2 * 16.0;
               double var10 = frac(var6) - 0.5;
               double var12 = frac(var8 + Math.abs(var10) * 0.9);
               double var14 = 0.72 + 0.28 * Math.sin(Math.PI * var12) * (1.0 - Math.abs(var10) * 1.2);
               double var16 = Math.abs(var10) > 0.44 ? 0.8 : 1.0;
               return 0xFF000000 | Capes.shade(16777215, var14 * var16 * (0.94 + 0.08 * Capes.fbm(var0x * 6.0, var2 * 6.0, 6, 6, 2, 1031)));
            });
         case "hd_fur":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 40.0, var2 * 5.0, 40, 5, 3, 1041);
               double var8 = Capes.fbm(var0x * 10.0, var2 * 10.0, 10, 10, 3, 1042);
               return 0xFF000000 | Capes.shade(16777215, 0.76 + 0.2 * var6 + 0.08 * var8);
            });
         case "hd_fluff":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 16.0, var2 * 16.0, 16, 16, 4, 1051);
               double var8 = 0.5 + 0.5 * Math.sin(var0x * 50.0 + var6 * 8.0 + Math.cos(var2 * 50.0 + var6 * 7.0));
               return 0xFF000000 | Capes.shade(16777215, 0.8 + 0.12 * var6 + 0.08 * var8);
            });
         case "hd_satin":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               double var6 = 0.72 + 0.22 * Math.pow(Math.max(0.0, Math.sin(var0x * Math.PI * 2.0 + var2 * 3.0)), 4.0) + 0.06 * Math.sin(var2 * 50.0);
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, var6));
            });
         case "hd_leather":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 20.0, var2 * 20.0, 20, 20, 4, 1061);
               double var8 = Math.abs(Capes.fbm(var0x * 32.0, var2 * 32.0, 32, 32, 2, 1062) - 0.5);
               double var10 = var8 < 0.03 ? 0.86 : 1.0;
               double var12 = Capes.fbm(var0x * 3.0, var2 * 3.0, 3, 3, 3, 1063);
               return 0xFF000000 | Capes.shade(16777215, (0.8 + 0.14 * var6 + 0.08 * var12) * var10);
            });
         case "hd_stitch":
            return Tex2.pix(128, 16, (var0x, var2, var4, var5) -> {
               boolean var6 = var4 % 8 < 5 && Math.abs(var2 - 0.5) < 0.22;
               double var7 = 0.72 + 0.1 * Capes.fbm(var0x * 16.0, var2, 16, 1, 2, 1071);
               return 0xFF000000 | (var6 ? 16049864 : Capes.shade(16777215, var7));
            });
         case "hd_gold":
            return metal(16774856, 15778378, 11565082, 5912072, 1081);
         case "hd_silver":
            return metal(16777215, 13949668, 9082022, 3818064, 1082);
         case "hd_steel":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 2.0, var2 * 90.0, 2, 90, 2, 1091);
               double var8 = 0.5 + 0.5 * Math.sin(var2 * Math.PI * 2.0 * 1.5 + 0.8);
               return 0xFF000000 | Capes.shade(16777215, 0.62 + 0.26 * var8 + 0.12 * var6);
            });
         case "hd_chrome":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               double var6 = 1.0 - var2;
               int var8 = var6 > 0.52 ? Capes.mix(12572927, 16777215, (var6 - 0.52) / 0.48) : Capes.ramp(var6 / 0.52, 2764342, 6975612, 15265012);
               return 0xFF000000 | Capes.add(var8, 16777215, Math.exp(-Math.pow((var6 - 0.55) / 0.02, 2.0)) * 0.6);
            });
         case "hd_plastic":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(16777215, 0.9 + 0.06 * Capes.fbm(var0x * 4.0, var2 * 4.0, 4, 4, 2, 1101) + 0.02 * (Capes.hash(var4, var5, 1102) - 0.5))
            );
         case "hd_rubber":
            return Tex2.pix(
               64, 64, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(16777215, 0.78 + 0.1 * Capes.fbm(var0x * 24.0, var2 * 24.0, 24, 24, 2, 1111))
            );
         case "hd_gem":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.atan2(var2 - 0.5, var0x - 0.5);
               double var8 = Math.hypot(var0x - 0.5, var2 - 0.5);
               int var10 = (int)Math.floor((var6 + Math.PI) / (Math.PI / 4)) + (var8 > 0.3 ? 8 : 0);
               double var11 = 0.62 + 0.38 * Capes.hash(var10, 3, 1121);
               double var13 = Math.exp(-(Math.pow(var0x - 0.34, 2.0) + Math.pow(var2 - 0.3, 2.0)) / 0.003);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, var11), 16777215, var13);
            });
         case "hd_glass":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> {
                  double var6 = Math.exp(-Math.pow((var0x * 1.2 + var2 * 0.8 - 0.55) / 0.06, 2.0)) * 0.6
                     + Math.exp(-Math.pow((var0x * 1.2 + var2 * 0.8 - 0.78) / 0.025, 2.0)) * 0.5;
                  return Tex2.argb(Capes.add(12577023, 16777215, var6), 0.2 + var6 * 0.7);
               }
            );
         case "hd_lens":
            return Tex2.pix(
               128,
               64,
               (var0x, var2, var4, var5) -> {
                  int var6 = Capes.ramp(var2, 3820154, 1448490, 657938, 2759226);
                  double var7 = Math.exp(-Math.pow((var0x * 0.9 - var2 * 0.55 - 0.08) / 0.05, 2.0)) * 0.55
                     + Math.exp(-Math.pow((var0x * 0.9 - var2 * 0.55 - 0.22) / 0.018, 2.0)) * 0.4;
                  double var9 = Math.exp(-Math.pow(var2 / 0.18, 2.0)) * 0.25;
                  return Tex2.argb(Capes.add(var6, 16777215, var7 + var9), 0.93);
               }
            );
         case "hd_bone":
            return Tex2.pix(64, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 6.0, var2 * 14.0, 6, 14, 3, 1131);
               int var8 = Capes.mix(16051416, 13154458, var2 * 0.6 + var6 * 0.3);
               return 0xFF000000 | Capes.shade(var8, 0.9 + 0.12 * Math.sin(var2 * Math.PI * 2.0 * 9.0 + var6 * 3.0));
            });
         case "hd_horn":
            return Tex2.pix(64, 256, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 6.0, var2 * 30.0, 6, 30, 3, 1141);
               double var8 = 0.82 + 0.18 * Math.pow(Math.abs(Math.sin(var2 * Math.PI * 14.0 + var6 * 1.5)), 0.6);
               int var10 = Capes.ramp(var2, 2753542, 6950418, 11804702, 15750190);
               return 0xFF000000 | Capes.add(Capes.shade(var10, var8 * (0.85 + 0.25 * var6)), 16760970, Math.pow(var2, 6.0) * 0.25);
            });
         case "hd_ivory":
            return Tex2.pix(64, 256, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 6.0, var2 * 24.0, 6, 24, 3, 1151);
               double var8 = 0.86 + 0.14 * Math.pow(Math.abs(Math.sin(var2 * Math.PI * 10.0 + var6 * 1.2)), 0.5);
               int var10 = Capes.ramp(var2, 9073232, 14207138, 16774880);
               return 0xFF000000 | Capes.shade(var10, var8);
            });
         case "hd_wood":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 3.0, var2 * 14.0, 3, 14, 4, 1161);
               double var8 = 0.5 + 0.5 * Math.sin((var0x * 26.0 + var6 * 7.0) * Math.PI);
               return 0xFF000000 | Capes.shade(Capes.ramp(var8, 6961690, 10641966, 13142602), 0.9 + 0.12 * var6);
            });
         case "hd_maple":
            return Tex2.pix(128, 256, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 4.0, var2 * 30.0, 4, 30, 3, 1171);
               double var8 = 0.5 + 0.5 * Math.sin(var2 * 120.0 + var6 * 9.0);
               double var10 = Math.hypot((var0x - 0.5) * 1.6, (var2 - 0.55) * 1.1);
               int var12 = Capes.mix(16756778, 14700570, Capes.smooth(0.2, 0.55, var10));
               var12 = Capes.mix(var12, 2755078, Capes.smooth(0.45, 0.72, var10));
               return 0xFF000000 | Capes.shade(var12, 0.86 + 0.18 * var8);
            });
         case "hd_brick":
            return Tex2.pix(
               64,
               64,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(
                     16777215, 0.93 + 0.04 * Capes.fbm(var0x * 3.0, var2 * 3.0, 3, 3, 2, 1181) + (Capes.hash(var4, var5, 1182) > 0.995 ? -0.06 : 0.0)
                  )
            );
         case "hd_lego_logo":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(Color.WHITE);
               var0x.fillRect(0, 0, 64, 64);
               var0x.setColor(new Color(13158600));
               var0x.setFont(new Font("SansSerif", 3, 15));
               var0x.drawString("LEGO", 12, 38);
            });
         case "hd_petal":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var0x - 0.5) / 0.03, 2.0)) * 0.12 + 0.05 * Math.sin(var0x * 40.0) * (1.0 - var2);
               double var8 = 0.72 + 0.28 * var2;
               return 0xFF000000 | Capes.shade(16777215, Math.min(1.0, var8 - var6 + 0.06 * Capes.fbm(var0x * 8.0, var2 * 8.0, 8, 8, 2, 1191)));
            });
         case "hd_leaf":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var2 - 0.5) / 0.03, 2.0));
               double var8 = Math.exp(-Math.pow(frac(var0x * 5.0 + Math.abs(var2 - 0.5) * 3.0) - 0.5, 2.0) / 0.004) * 0.15;
               int var10 = Capes.mix(3050036, 8047444, 0.3 + 0.5 * Capes.fbm(var0x * 5.0, var2 * 5.0, 5, 5, 3, 1201));
               return 0xFF000000 | Capes.add(Capes.shade(var10, 0.95 - var8), 13168800, var6 * 0.5);
            });
         case "hd_leafw":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.exp(-Math.pow((var2 - 0.5) / 0.03, 2.0));
               double var8 = Math.exp(-Math.pow(frac(var0x * 5.0 + Math.abs(var2 - 0.5) * 3.0) - 0.5, 2.0) / 0.004) * 0.15;
               double var10 = Capes.fbm(var0x * 5.0, var2 * 5.0, 5, 5, 3, 1202);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.78 + 0.18 * var10 - var8), 16777215, var6 * 0.25);
            });
         case "hd_vine":
            return Tex2.pix(
               64,
               32,
               (var0x, var2, var4, var5) -> 0xFF000000
                  | Capes.shade(Capes.mix(3828258, 6986298, Capes.fbm(var0x * 10.0, var2 * 4.0, 10, 4, 3, 1211)), 0.85 + 0.15 * Math.sin(var0x * 60.0))
            );
         case "hd_mush":
            return Tex2.pix(256, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 10.0, var2 * 6.0, 10, 6, 3, 1221);
               return 0xFF000000 | Capes.shade(Capes.mix(15741994, 10097168, Math.pow(var2, 1.5) * 0.8), 0.9 + 0.12 * var6);
            });
         case "hd_gill":
            return Tex2.pix(
               256, 32, (var0x, var2, var4, var5) -> 0xFF000000 | Capes.shade(15787730, 0.78 + 0.22 * Math.abs(Math.sin(var0x * Math.PI * 64.0)) - 0.2 * var2)
            );
         case "hd_party":
            return Tex2.img(256, 128, var0x -> {
               var0x.setColor(new Color(2799856));
               var0x.fillRect(0, 0, 256, 128);
               int[] var1 = new int[]{16726666, 16769610, 8014591, 3858554};
               var0x.setStroke(new BasicStroke(12.0F));

               for (int var2 = -4; var2 < 12; var2++) {
                  var0x.setColor(new Color(var1[(var2 + 8) % 4]));
                  var0x.drawLine(var2 * 32, 128, var2 * 32 + 96, 0);
               }

               Random var6 = new Random(1231L);

               for (int var3 = 0; var3 < 70; var3++) {
                  var0x.setColor(var3 % 2 == 0 ? Color.WHITE : new Color(16773258));
                  double var4 = 3.0 + var6.nextDouble() * 4.0;
                  var0x.fill(new Double(var6.nextDouble() * 256.0, var6.nextDouble() * 128.0, var4, var4));
               }
            });
         case "hd_wizard":
            return Tex2.img(256, 256, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(3812016), 0.0F, 256.0F, new Color(1708128)));
               var0x.fillRect(0, 0, 256, 256);
               Random var1 = new Random(1241L);

               for (int var2 = 0; var2 < 26; var2++) {
                  double var3 = var1.nextDouble() * 256.0;
                  double var5 = 20.0 + var1.nextDouble() * 220.0;
                  double var7 = 7.0 + var1.nextDouble() * 10.0;
                  var0x.setColor(new Color(16767050));
                  if (var2 % 3 == 0) {
                     java.awt.geom.Path2D.Double var9 = new java.awt.geom.Path2D.Double();
                     var9.append(new Double(var3 - var7, var5 - var7, var7 * 2.0, var7 * 2.0), false);
                     var0x.fill(var9);
                     var0x.setColor(new Color(2760330));
                     var0x.fill(new Double(var3 - var7 * 0.55, var5 - var7 * 1.1, var7 * 2.0, var7 * 2.0));
                  } else {
                     var0x.fill(XmasTex.starShape(var3, var5, var7, var7 * 0.42, 5));
                  }
               }

               for (int var10 = 0; var10 < 200; var10++) {
                  var0x.setColor(Capes.col(16777215, 0.3 + var1.nextDouble() * 0.5));
                  var0x.fill(new Double(var1.nextDouble() * 256.0, var1.nextDouble() * 256.0, 1.4, 1.4));
               }
            });
         case "hd_witchband":
            return Tex2.img(256, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(5955706), 0.0F, 32.0F, new Color(1735226)));
               var0x.fillRect(0, 0, 256, 32);
               var0x.setColor(Capes.col(0, 0.25));

               for (byte var1 = 0; var1 < 256; var1 += 4) {
                  var0x.drawLine(var1, 0, var1, 32);
               }

               var0x.setColor(Capes.col(16777215, 0.35));
               var0x.fillRect(0, 3, 256, 2);
            });
         case "hd_hatband":
            return Tex2.img(256, 32, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(14692410), 0.0F, 32.0F, new Color(7997970)));
               var0x.fillRect(0, 0, 256, 32);
               var0x.setColor(Capes.col(0, 0.22));

               for (byte var1 = 0; var1 < 32; var1 += 3) {
                  var0x.drawLine(0, var1, 256, var1);
               }

               var0x.setColor(Capes.col(16777215, 0.3));
               var0x.fillRect(0, 4, 256, 2);
            });
         case "hd_teddyface":
            return Tex2.img(128, 128, var0x -> {
               var0x.setColor(new Color(11565640));
               var0x.fillRect(0, 0, 128, 128);
               var0x.setColor(new Color(1707528));
               var0x.fill(new Double(32.0, 40.0, 16.0, 18.0));
               var0x.fill(new Double(80.0, 40.0, 16.0, 18.0));
               var0x.setColor(Color.WHITE);
               var0x.fill(new Double(36.0, 43.0, 6.0, 6.0));
               var0x.fill(new Double(84.0, 43.0, 6.0, 6.0));
               var0x.setColor(Capes.col(16743066, 0.5));
               var0x.fill(new Double(20.0, 64.0, 20.0, 12.0));
               var0x.fill(new Double(88.0, 64.0, 20.0, 12.0));
            });
         case "hd_target":
            return Tex2.img(256, 256, var0x -> {
               var0x.setColor(new Color(1985208));
               var0x.fillRect(0, 0, 256, 256);
               var0x.setColor(new Color(15921910));
               var0x.fillRect(0, 0, 128, 128);
               var0x.fillRect(128, 128, 128, 128);
               var0x.setColor(Capes.col(0, 0.15));
               Random var1 = new Random(1251L);

               for (int var2 = 0; var2 < 400; var2++) {
                  var0x.fill(new Double(var1.nextDouble() * 256.0, var1.nextDouble() * 256.0, 2.0, 2.0));
               }
            });
         case "hd_rocket":
            return Tex2.img(128, 256, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 128.0F, 0.0F, new Color(15001838)));
               var0x.fillRect(0, 0, 128, 256);
               var0x.setColor(Capes.col(0, 0.18));
               var0x.setStroke(new BasicStroke(1.4F));

               for (byte var1 = 30; var1 < 256; var1 += 46) {
                  var0x.drawLine(0, var1, 128, var1);
               }

               for (byte var3 = 12; var3 < 256; var3 += 23) {
                  for (byte var2 = 4; var2 < 128; var2 += 16) {
                     var0x.fill(new Double(var2, var3, 2.4, 2.4));
                  }
               }

               var0x.setColor(new Color(14163496));
               var0x.fillRect(0, 200, 128, 14);
            });
         case "hd_note":
            return Tex2.img(64, 64, var0x -> {
               var0x.setColor(Color.WHITE);
               var0x.fillRect(0, 0, 64, 64);
            });
         case "hd_rune":
            return Tex2.img(512, 64, var0x -> {
               var0x.setColor(new Color(0, 0, 0, 0));
               var0x.fillRect(0, 0, 512, 64);
               var0x.setStroke(new BasicStroke(3.2F, 1, 1));
               Random var1 = new Random(1261L);

               for (int var2 = 0; var2 < 16; var2++) {
                  double var3 = var2 * 32 + 16;
                  java.awt.geom.Path2D.Double var5 = new java.awt.geom.Path2D.Double();
                  int var6 = 2 + var1.nextInt(3);

                  for (int var7 = 0; var7 < var6; var7++) {
                     var5.moveTo(var3 + (var1.nextDouble() - 0.5) * 18.0, 12.0 + var1.nextDouble() * 10.0);
                     var5.lineTo(var3 + (var1.nextDouble() - 0.5) * 18.0, 36.0 + var1.nextDouble() * 16.0);
                  }

                  var5.moveTo(var3 - 9.0, 32.0);
                  var5.lineTo(var3 + 9.0, 26.0 + var1.nextDouble() * 12.0);
                  var0x.setColor(Capes.col(13148415, 0.35));
                  var0x.setStroke(new BasicStroke(8.0F, 1, 1));
                  var0x.draw(var5);
                  var0x.setColor(Color.WHITE);
                  var0x.setStroke(new BasicStroke(3.0F, 1, 1));
                  var0x.draw(var5);
               }
            });
         case "hd_bubble":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = frac(var0x * 2.0 + var2 * 1.3 + 0.2 * Math.sin(var2 * 9.0));
               int var8 = Capes.hsv(var6, 0.45, 1.0);
               double var9 = Math.exp(-(Math.pow(var0x - 0.3, 2.0) + Math.pow(var2 - 0.3, 2.0)) / 0.004);
               return Tex2.argb(Capes.add(var8, 16777215, var9), 0.28 + var9 * 0.7);
            });
         case "hd_softglow":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.5, var2 - 0.5) * 2.0;
               return Tex2.argb(16777215, Math.pow(Math.max(0.0, 1.0 - var6), 2.2));
            });
         case "hd_trail":
            return Tex2.pix(128, 16, (var0x, var2, var4, var5) -> Tex2.argb(16777215, Math.pow(var0x, 1.6) * Math.pow(Math.sin(Math.PI * var2), 1.5)));
         case "hd_gloss":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Math.hypot(var0x - 0.32, var2 - 0.3);
               return 0xFF000000 | Capes.add(Capes.shade(16777215, 0.8 + 0.2 * (1.0 - var2)), 16777215, Math.exp(-var6 * var6 / 0.004) * 0.8);
            });
         case "hd_foxear":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 24.0, var2 * 4.0, 24, 4, 3, 1311);
               double var8 = Capes.smooth(0.42, 0.18, var2 + (var6 - 0.5) * 0.12);
               int var10 = Capes.mix(Capes.mix(15759912, 14177818, var2), 2759188, var8);
               return 0xFF000000 | Capes.shade(var10, 0.82 + 0.25 * var6);
            });
         case "hd_mustache":
            return Tex2.pix(64, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 60.0, var2 * 4.0, 60, 4, 2, 1271);
               return 0xFF000000 | Capes.shade(16777215, 0.7 + 0.3 * var6);
            });
         case "hd_mask":
            return Tex2.img(128, 64, var0x -> {
               var0x.setPaint(new GradientPaint(0.0F, 0.0F, new Color(16777215), 0.0F, 64.0F, new Color(14211296)));
               var0x.fillRect(0, 0, 128, 64);
               var0x.setColor(Capes.col(0, 0.1));
               Random var1 = new Random(1281L);

               for (int var2 = 0; var2 < 90; var2++) {
                  double var3 = var1.nextDouble() * 128.0;
                  double var5 = var1.nextDouble() * 64.0;
                  var0x.draw(new Double(var3, var5, 5.0, 3.0));
               }

               var0x.setColor(Capes.col(16777215, 0.8));

               for (int var7 = 0; var7 < 40; var7++) {
                  var0x.fill(new Double(var1.nextDouble() * 128.0, var1.nextDouble() * 64.0, 1.5, 1.5));
               }
            });
         case "hd_screen":
            return Tex2.pix(256, 64, (var0x, var2, var4, var5) -> {
               double var6 = var5 % 3 == 0 ? 0.75 : 1.0;
               boolean var8 = var4 % 16 == 0 || var5 % 16 == 0;
               int var9 = Capes.mix(656918, 2755130, var2);
               if (var8) {
                  var9 = Capes.add(var9, 16726666, 0.18);
               }

               double var10 = Math.exp(-Math.pow((var2 - 0.08) / 0.05, 2.0)) * 0.35;
               return 0xFF000000 | Capes.add(Capes.shade(var9, var6), 16743088, var10);
            });
         case "hd_ninja":
            return Tex2.pix(128, 64, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 16.0, var2 * 8.0, 16, 8, 3, 1291);
               double var8 = 0.9 + 0.1 * Math.sin(var2 * 30.0 + var6 * 4.0);
               return 0xFF000000 | Capes.shade(16777215, (0.78 + 0.16 * var6) * var8);
            });
         case "hd_quiver":
            return Tex2.pix(128, 128, (var0x, var2, var4, var5) -> {
               double var6 = Capes.fbm(var0x * 16.0, var2 * 16.0, 16, 16, 3, 1301);
               boolean var8 = Math.abs(var2 - 0.15) < 0.04 || Math.abs(var2 - 0.85) < 0.04;
               double var9 = 0.5 + 0.5 * Math.sin(var0x * 60.0 + var2 * 30.0);
               int var11 = var8 ? 15251786 : Capes.mix(6961690, 9064488, var6);
               if (!var8 && Math.abs(var2 - 0.5) < 0.2) {
                  var11 = Capes.shade(var11, 0.85 + 0.15 * var9);
               }

               return 0xFF000000 | var11;
            });
         default:
            return null;
      }
   }

   static CTex.T metal(int var0, int var1, int var2, int var3, int var4) {
      return Tex2.pix(128, 128, (var5, var7, var9, var10) -> {
         double var11 = 0.5 + 0.5 * Math.sin(var7 * Math.PI * 2.0 * 1.2 + Math.sin(var5 * Math.PI * 2.0) * 0.6);
         double var13 = Capes.fbm(var5 * 3.0, var7 * 80.0, 3, 80, 2, var4);
         int var15 = Capes.ramp(Math.min(1.0, var11 * 0.85 + var13 * 0.2), var3, var2, var1, var0);
         double var16 = Capes.hash(var9, var10, var4 + 1) > 0.997 ? 0.6 : 0.0;
         return 0xFF000000 | Capes.add(var15, 16777215, var16 + Math.exp(-Math.pow((var11 - 0.95) / 0.05, 2.0)) * 0.35);
      });
   }
}
