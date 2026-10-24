package dev.lego.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Kit {
   private static final List<Kit.Region> REGIONS = new ArrayList<>();
   private static Kit.Region active;
   private static int[] clip;

   private Kit() {
   }

   public static void frame() {
      REGIONS.clear();
      clip = null;
   }

   public static void clip(int x, int y, int w, int h) {
      clip = new int[]{x, y, w, h};
   }

   public static void unclip() {
      clip = null;
   }

   public static Kit.Region hit(int x, int y, int w, int h) {
      int X = x;
      int Y = y;
      int W = w;
      int H = h;
      if (clip != null) {
         int x2 = Math.min(x + w, clip[0] + clip[2]);
         int y2 = Math.min(y + h, clip[1] + clip[3]);
         X = Math.max(x, clip[0]);
         Y = Math.max(y, clip[1]);
         W = Math.max(0, x2 - X);
         H = Math.max(0, y2 - Y);
      }

      Kit.Region r = new Kit.Region(X, Y, W, H);
      REGIONS.add(r);
      return r;
   }

   public static Kit.Region at(float px, float py) {
      for (int i = REGIONS.size() - 1; i >= 0; i--) {
         if (REGIONS.get(i).in(px, py)) {
            return REGIONS.get(i);
         }
      }

      return null;
   }

   public static boolean down(float px, float py, int button) {
      for (int i = REGIONS.size() - 1; i >= 0; i--) {
         Kit.Region r = REGIONS.get(i);
         if (r.in(px, py)) {
            if (button == 1 && r.right != null) {
               r.right.run();
               Sound.click();
               return true;
            }

            if (button == 0 && r.drag != null) {
               active = r;
               r.drag.accept(px, py);
               return true;
            }

            if (button == 0 && r.click != null) {
               r.click.run();
               Sound.click();
               return true;
            }

            if (r.click != null || r.right != null || r.drag != null) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean dragging() {
      return active != null;
   }

   public static boolean drag(float px, float py) {
      if (active == null) {
         return false;
      } else {
         active.drag.accept(px, py);
         return true;
      }
   }

   public static boolean up() {
      boolean had = active != null;
      active = null;
      return had;
   }

   public static boolean scroll(float px, float py, double amount) {
      for (int i = REGIONS.size() - 1; i >= 0; i--) {
         Kit.Region r = REGIONS.get(i);
         if (r.scroll != null && r.in(px, py)) {
            r.scroll.accept(amount);
            return true;
         }
      }

      return false;
   }

   public static void toggle(String key, int x, int y, int w, int h, boolean on, float P) {
      float t = Ease.to("tg:" + key, on ? 1.0F : 0.0F, 16.0F);
      int track = Gx.mix(Style.light ? -2762529 : -12960441, Style.accent, t);
      Gx.rect(x, y, w, h, h / 2, track);
      if (Halloween.on() && t > 0.01F) {
         Gx.pushAlpha(t);
         Halloween.spookyStripes(x, y, w, h, 0.0F);
         Gx.popAlpha();
      } else if (Xmas.on() && t > 0.01F) {
         Gx.pushAlpha(t);
         Xmas.candy(x, y, w, h, 0.0F);
         Gx.popAlpha();
      }

      int pad = Math.max(2, Math.round(h * 0.14F));
      int d = h - pad * 2;
      int kx = Math.round(x + pad + (w - pad * 2 - d) * Ease.outCubic(t));
      Gx.shadow(kx, y + pad, d, d, d / 2, Math.max(1, Math.round(P * 1.2F)), 0.25F);
      Gx.circle(kx + d / 2, y + h / 2, d / 2, -1);
   }

   public static void slider(String key, int x, int y, int w, int h, float frac, float P) {
      float f = Ease.to("sl:" + key, frac, 22.0F);
      int th = Math.max(2, Math.round(P * 3.2F));
      int ty = y + h / 2 - th / 2;
      Gx.rect(x, ty, w, th, th / 2, Style.light ? -2499357 : -13420993);
      int fw = Math.round(w * f);
      Gx.rect(x, ty, Math.max(th, fw), th, th / 2, Style.accent);
      int kd = Math.round(P * 11.0F);
      int kx = x + fw - kd / 2;
      Gx.shadow(kx, y + h / 2 - kd / 2, kd, kd, kd / 2, Math.max(1, Math.round(P * 1.5F)), 0.3F);
      Gx.circle(kx + kd / 2, y + h / 2, kd / 2, -1);
      Gx.circle(kx + kd / 2, y + h / 2, Math.max(1, kd / 5), Style.accent);
   }

   public static void button(String key, String label, String icon, int x, int y, int w, int h, float P, boolean primary, boolean hovered) {
      float hv = Ease.to("bt:" + key, hovered ? 1.0F : 0.0F, 14.0F);
      int base = primary ? Style.accent : Style.surface2;
      int top = primary ? Gx.mix(base, -1, 0.12F * hv) : Gx.mix(base, Style.surfaceHover, hv);
      Gx.rect(x, y, w, h, Math.round(P * 7.0F), top);
      if (!primary) {
         Gx.outline(x, y, w, h, Math.round(P * 7.0F), Math.max(1, Math.round(P * 0.6F)), Gx.mix(Style.stroke, Style.strokeHi, hv));
      }

      int fg = primary ? Style.accentText : Style.text;
      float fs = P * 8.6F;
      float tw = Gx.width(label, fs, 2);
      float iw = icon == null ? 0.0F : P * 11.0F + P * 5.0F;
      float sx = x + (w - tw - iw) / 2.0F;
      if (icon != null) {
         Gx.icon(icon, sx + P * 5.5F, y + h / 2.0F, P * 11.0F, fg);
      }

      Gx.textMid(label, sx + iw, y + h / 2.0F, fs, 2, fg);
   }

   public static void tooltip(String text, float cx, float bottomY, float P) {
      float fs = P * 8.0F;
      int tw = Math.round(Gx.width(text, fs, 2) + P * 14.0F);
      int th = Math.round(P * 18.0F);
      int x = Math.round(cx - tw / 2.0F);
      int y = Math.round(bottomY - th);
      Gx.shadow(x, y, tw, th, Math.round(P * 6.0F), Math.round(P * 4.0F), 0.35F);
      Gx.rect(x, y, tw, th, Math.round(P * 6.0F), Style.light ? -15000542 : -789258);
      Gx.textCenter(text, x + tw / 2.0F, y + th / 2.0F, fs, 2, Style.light ? -1 : -15658474);
   }

   public static final class Region {
      public final int x;
      public final int y;
      public final int w;
      public final int h;
      Runnable click;
      Runnable right;
      BiConsumer<Float, Float> drag;
      Consumer<Double> scroll;
      String id;

      Region(int x, int y, int w, int h) {
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
      }

      public Kit.Region click(Runnable r) {
         this.click = r;
         return this;
      }

      public Kit.Region right(Runnable r) {
         this.right = r;
         return this;
      }

      public Kit.Region drag(BiConsumer<Float, Float> d) {
         this.drag = d;
         return this;
      }

      public Kit.Region scroll(Consumer<Double> s) {
         this.scroll = s;
         return this;
      }

      public Kit.Region id(String s) {
         this.id = s;
         return this;
      }

      public String idOrNull() {
         return this.id;
      }

      boolean in(float px, float py) {
         return px >= this.x && py >= this.y && px < this.x + this.w && py < this.y + this.h;
      }
   }
}
