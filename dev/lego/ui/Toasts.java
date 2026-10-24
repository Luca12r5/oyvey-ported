package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;

public final class Toasts {
   private static final List<Toasts.T> LIST = new ArrayList<>();

   private Toasts() {
   }

   public static void show(String var0, String var1, String var2) {
      show(var0, var1, var2, 0, 3500L);
   }

   public static void show(String var0, String var1, String var2, int var3, long var4) {
      synchronized (LIST) {
         LIST.add(new Toasts.T(var0, var1, var2 == null ? "" : var2, var3, var4));

         while (LIST.size() > 4) {
            LIST.remove(0);
         }
      }
   }

   public static boolean active() {
      synchronized (LIST) {
         return !LIST.isEmpty();
      }
   }

   public static void render(int var0, int var1) {
      ArrayList var2;
      synchronized (LIST) {
         LIST.removeIf(var0x -> System.currentTimeMillis() - var0x.at > var0x.dur + 400L);
         var2 = new ArrayList<>(LIST);
      }

      if (!var2.isEmpty()) {
         Style.update();
         float var21 = Math.max(1.0F, Math.min(var0 / 680.0F, var1 / 420.0F));
         int var4 = Math.round(210.0F * var21);
         int var5 = Math.round(40.0F * var21);
         int var6 = Math.round(6.0F * var21);
         float var7 = Math.round(12.0F * var21);

         for (int var8 = var2.size() - 1; var8 >= 0; var8--) {
            Toasts.T var9 = (Toasts.T)var2.get(var8);
            long var10 = System.currentTimeMillis() - var9.at;
            float var12 = Ease.outBack(Math.min(1.0F, (float)var10 / 380.0F));
            float var13 = var10 > var9.dur ? Ease.clamp((float)(var10 - var9.dur) / 400.0F) : 0.0F;
            if (var9.y < 0.0F) {
               var9.y = var7;
            }

            var9.y = var9.y + (var7 - var9.y) * Math.min(1.0F, Gx.dt * 12.0F);
            int var14 = Math.round(var0 - var4 - 12.0F * var21 + (1.0F - var12) * (var4 + 20.0F * var21) + var13 * (var4 + 20.0F * var21));
            int var15 = Math.round(var9.y);
            int var16 = var9.color == 0 ? Style.accent : var9.color;
            Gx.pushAlpha(1.0F - var13);
            Gx.shadow(var14, var15, var4, var5, Math.round(10.0F * var21), Math.round(6.0F * var21), 0.45F);
            Gx.rect(var14, var15, var4, var5, Math.round(10.0F * var21), -233696488);
            Gx.outline(var14, var15, var4, var5, Math.round(10.0F * var21), Math.max(1, Math.round(0.6F * var21)), 536870911);
            int var17 = Math.round(26.0F * var21);
            Gx.rect(var14 + Math.round(7.0F * var21), var15 + (var5 - var17) / 2, var17, var17, Math.round(8.0F * var21), Gx.withAlpha(var16, 0.2F));
            Gx.icon(var9.icon, var14 + Math.round(7.0F * var21) + var17 / 2.0F, var15 + var5 / 2.0F, 14.0F * var21, var16);
            float var18 = var14 + 40.0F * var21;
            Gx.text(Fonts.ellipsize(var9.title, 3, 8.6F * var21, var4 - 48.0F * var21), var18, var15 + 7.0F * var21, 8.6F * var21, 3, -1);
            Gx.text(Fonts.ellipsize(var9.text, 1, 7.4F * var21, var4 - 48.0F * var21), var18, var15 + 21.0F * var21, 7.4F * var21, 1, -6117197);
            float var19 = Math.max(0.0F, 1.0F - (float)var10 / (float)var9.dur);
            Gx.rect(
               var14 + Math.round(10.0F * var21),
               var15 + var5 - Math.round(3.0F * var21),
               Math.round((var4 - 20.0F * var21) * var19),
               Math.max(1, Math.round(1.5F * var21)),
               1,
               Gx.withAlpha(var16, 0.7F)
            );
            Gx.popAlpha();
            var7 += var5 + var6;
         }
      }
   }

   private static final class T {
      final String icon;
      final String title;
      final String text;
      final int color;
      final long at = System.currentTimeMillis();
      final long dur;
      float y = -1.0F;

      T(String var1, String var2, String var3, int var4, long var5) {
         this.icon = var1;
         this.title = var2;
         this.text = var3;
         this.color = var4;
         this.dur = var5;
      }
   }
}
