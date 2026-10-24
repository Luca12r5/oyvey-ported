package dev.lego.discord;

import dev.lego.ui.Ease;
import dev.lego.ui.Gx;

public final class DiscordHud {
   private static final int BLURPLE = -10983950;
   private static final int RED = -1228219;
   private static final int GREEN = -12868259;
   private static final int BG = -804187628;
   private static final int BG2 = -14803166;
   private static final double[][] BTN = new double[2][4];

   private DiscordHud() {
   }

   public static void render(int W, int H) {
      if (DiscordControl.hudOn()) {
         int S = Gx.S;
         float sc = DiscordControl.sc();
         float k = S * sc;
         boolean compact = DiscordControl.compactHud();
         boolean mic = !DiscordControl.muted && !DiscordControl.deafened;
         boolean ear = !DiscordControl.deafened;
         int w = Math.round((compact ? 52 : 132) * k);
         int h = Math.round(34.0F * k);
         int x = Math.round((float)(W * DiscordControl.fx()));
         int y = Math.round((float)(H * DiscordControl.fy()));
         Gx.shadow(x, y, w, h, Math.round(10.0F * k), Math.round(6.0F * k), 0.35F);
         Gx.rect(x, y, w, h, Math.round(10.0F * k), -804187628);
         Gx.outline(x, y, w, h, Math.round(10.0F * k), Math.max(1, S / 2), 587202559);
         int bx = x + Math.round(6.0F * k);
         if (!compact) {
            int lr = Math.round(11.0F * k);
            int lx = x + Math.round(17.0F * k);
            int ly = y + h / 2;
            Gx.circle(lx, ly, lr, -10983950);
            Gx.rect(lx - Math.round(6.0F * k), ly - Math.round(3.0F * k), Math.round(12.0F * k), Math.round(7.0F * k), Math.round(3.0F * k), -1);
            Gx.circle(lx - Math.round(2.5F * k), ly, Math.max(1, Math.round(1.3F * k)), -10983950);
            Gx.circle(lx + Math.round(2.5F * k), ly, Math.max(1, Math.round(1.3F * k)), -10983950);
            Gx.text("Discord", x + Math.round(33.0F * k), y + Math.round(6.0F * k), 7.6F * k, 3, -855051);
            String st = !ear ? "Taub geschaltet" : (!mic ? "Stumm geschaltet" : "Sprachchat aktiv");
            Gx.text(st, x + Math.round(33.0F * k), y + Math.round(18.0F * k), 6.0F * k, 2, ear && mic ? -12868259 : -1228219);
            bx = x + w - Math.round(46.0F * k);
         }

         int br = Math.round(10.0F * k);
         boolean[] on = new boolean[]{mic, ear};
         String[] icon = new String[]{mic ? "sound" : "mute", ear ? "volume" : "mute"};

         for (int i = 0; i < 2; i++) {
            int cx = bx + br + i * Math.round(22.0F * k);
            int cy = y + h / 2;
            float pulse = Ease.to("dc:" + i, on[i] ? 0.0F : 1.0F, 10.0F);
            Gx.circle(cx, cy, br, Gx.mix(-14803166, -1228219, pulse * 0.85F));
            Gx.icon(icon[i], cx, cy, 9.5F * k, on[i] ? -2367775 : -1);
            if (!on[i]) {
               Gx.rect(cx - Math.round(7.0F * k), cy - Math.max(1, S / 2), Math.round(14.0F * k), Math.max(1, S), 1, -1);
            }

            BTN[i][0] = (double)(cx - br) / S;
            BTN[i][1] = (double)(cy - br) / S;
            BTN[i][2] = (double)(cx + br) / S;
            BTN[i][3] = (double)(cy + br) / S;
         }
      }
   }

   public static boolean click(double mx, double my, int button) {
      if (DiscordControl.hudOn() && button == 0) {
         for (int i = 0; i < 2; i++) {
            if (mx >= BTN[i][0] && mx <= BTN[i][2] && my >= BTN[i][1] && my <= BTN[i][3]) {
               if (i == 0) {
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
