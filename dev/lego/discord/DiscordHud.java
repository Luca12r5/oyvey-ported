package dev.lego.discord;

import dev.lego.ui.Gx;

public final class DiscordHud {
   private static final int BLURPLE = -10983950;
   private static final int RED = -1228219;
   private static final int GREEN = -12868259;
   private static final double[][] BTN = new double[2][4];
   private static int lastS = 1;

   private DiscordHud() {
   }

   public static void render(int var0, int var1) {
      if (DiscordControl.hudOn()) {
         int var2 = Gx.S;
         lastS = var2;
         float var3 = DiscordControl.sc();
         int var4 = Math.round(20 * var2 * var3);
         int var5 = Math.round(72 * var2 * var3);
         int var6 = Math.round(4 * var2 * var3);
         int var7 = Math.round((float)(var0 * DiscordControl.fx()));
         int var8 = Math.round((float)(var1 * DiscordControl.fy()));
         String[] var9 = new String[]{"Mikro", "Ton"};
         boolean[] var10 = new boolean[]{DiscordControl.muted, DiscordControl.deafened};

         for (int var11 = 0; var11 < 2; var11++) {
            int var12 = var8 + var11 * (var4 + var6);
            int var13 = var10[var11] ? -1228219 : -12868259;
            Gx.rect(var7, var12, var5, var4, var4 / 2, -1072557288);
            Gx.outline(var7, var12, var5, var4, var4 / 2, Math.max(1, var2 / 2), var10[var11] ? 1726825029 : 872415231);
            Gx.circle(var7 + var4 / 2, var12 + var4 / 2, Math.round(var4 * 0.28F), var13);
            Gx.textMid(var9[var11] + (var10[var11] ? " aus" : ""), var7 + var4 * 0.95F, var12 + var4 / 2.0F, 7.4F * var2 * var3, 3, -855051);
            BTN[var11][0] = (double)var7 / var2;
            BTN[var11][1] = (double)var12 / var2;
            BTN[var11][2] = (double)(var7 + var5) / var2;
            BTN[var11][3] = (double)(var12 + var4) / var2;
         }

         Gx.text("Discord", var7 + 2, var8 - Math.round(9 * var2 * var3), 6.2F * var2 * var3, 2, -10983950);
      }
   }

   public static boolean click(double var0, double var2, int var4) {
      if (DiscordControl.hudOn() && var4 == 0) {
         for (int var5 = 0; var5 < 2; var5++) {
            if (var0 >= BTN[var5][0] && var0 <= BTN[var5][2] && var2 >= BTN[var5][1] && var2 <= BTN[var5][3]) {
               if (var5 == 0) {
                  DiscordControl.toggleMute();
               } else {
                  DiscordControl.toggleDeafen();
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
