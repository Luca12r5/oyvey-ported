package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;

public final class Toasts {
   private static final List<Toasts.T> LIST = new ArrayList<>();

   private Toasts() {
   }

   public static void show(String icon, String title, String text) {
      show(icon, title, text, 0, 3500L);
   }

   public static void show(String icon, String title, String text, int color, long durMs) {
      synchronized (LIST) {
         LIST.add(new Toasts.T(icon, title, text == null ? "" : text, color, durMs));

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

   public static void render(int W, int H) {
      List<Toasts.T> copy;
      synchronized (LIST) {
         LIST.removeIf(txx -> System.currentTimeMillis() - txx.at > txx.dur + 400L);
         copy = new ArrayList<>(LIST);
      }

      if (!copy.isEmpty()) {
         Style.update();
         float P = Math.max(1.0F, Math.min(W / 680.0F, H / 420.0F));
         int w = Math.round(210.0F * P);
         int h = Math.round(40.0F * P);
         int gap = Math.round(6.0F * P);
         float targetY = Math.round(12.0F * P);

         for (int i = copy.size() - 1; i >= 0; i--) {
            Toasts.T t = copy.get(i);
            long age = System.currentTimeMillis() - t.at;
            float in = Ease.outBack(Math.min(1.0F, (float)age / 380.0F));
            float out = age > t.dur ? Ease.clamp((float)(age - t.dur) / 400.0F) : 0.0F;
            if (t.y < 0.0F) {
               t.y = targetY;
            }

            t.y = t.y + (targetY - t.y) * Math.min(1.0F, Gx.dt * 12.0F);
            int x = Math.round(W - w - 12.0F * P + (1.0F - in) * (w + 20.0F * P) + out * (w + 20.0F * P));
            int y = Math.round(t.y);
            int accent = t.color == 0 ? Style.accent : t.color;
            Gx.pushAlpha(1.0F - out);
            Gx.shadow(x, y, w, h, Math.round(10.0F * P), Math.round(6.0F * P), 0.45F);
            Gx.rect(x, y, w, h, Math.round(10.0F * P), -233696488);
            Gx.outline(x, y, w, h, Math.round(10.0F * P), Math.max(1, Math.round(0.6F * P)), 536870911);
            int is = Math.round(26.0F * P);
            Gx.rect(x + Math.round(7.0F * P), y + (h - is) / 2, is, is, Math.round(8.0F * P), Gx.withAlpha(accent, 0.2F));
            Gx.icon(t.icon, x + Math.round(7.0F * P) + is / 2.0F, y + h / 2.0F, 14.0F * P, accent);
            float tx = x + 40.0F * P;
            Gx.text(Fonts.ellipsize(t.title, 3, 8.6F * P, w - 48.0F * P), tx, y + 7.0F * P, 8.6F * P, 3, -1);
            Gx.text(Fonts.ellipsize(t.text, 1, 7.4F * P, w - 48.0F * P), tx, y + 21.0F * P, 7.4F * P, 1, -6117197);
            float frac = Math.max(0.0F, 1.0F - (float)age / (float)t.dur);
            Gx.rect(
               x + Math.round(10.0F * P),
               y + h - Math.round(3.0F * P),
               Math.round((w - 20.0F * P) * frac),
               Math.max(1, Math.round(1.5F * P)),
               1,
               Gx.withAlpha(accent, 0.7F)
            );
            Gx.popAlpha();
            targetY += h + gap;
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

      T(String icon, String title, String text, int color, long dur) {
         this.icon = icon;
         this.title = title;
         this.text = text;
         this.color = color;
         this.dur = dur;
      }
   }
}
