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

   public static void clip(int var0, int var1, int var2, int var3) {
      clip = new int[]{var0, var1, var2, var3};
   }

   public static void unclip() {
      clip = null;
   }

   public static Kit.Region hit(int var0, int var1, int var2, int var3) {
      int var4 = var0;
      int var5 = var1;
      int var6 = var2;
      int var7 = var3;
      if (clip != null) {
         int var8 = Math.min(var0 + var2, clip[0] + clip[2]);
         int var9 = Math.min(var1 + var3, clip[1] + clip[3]);
         var4 = Math.max(var0, clip[0]);
         var5 = Math.max(var1, clip[1]);
         var6 = Math.max(0, var8 - var4);
         var7 = Math.max(0, var9 - var5);
      }

      Kit.Region var10 = new Kit.Region(var4, var5, var6, var7);
      REGIONS.add(var10);
      return var10;
   }

   public static Kit.Region at(float var0, float var1) {
      for (int var2 = REGIONS.size() - 1; var2 >= 0; var2--) {
         if (REGIONS.get(var2).in(var0, var1)) {
            return REGIONS.get(var2);
         }
      }

      return null;
   }

   public static boolean down(float var0, float var1, int var2) {
      for (int var3 = REGIONS.size() - 1; var3 >= 0; var3--) {
         Kit.Region var4 = REGIONS.get(var3);
         if (var4.in(var0, var1)) {
            if (var2 == 1 && var4.right != null) {
               var4.right.run();
               Sound.click();
               return true;
            }

            if (var2 == 0 && var4.drag != null) {
               active = var4;
               var4.drag.accept(var0, var1);
               return true;
            }

            if (var2 == 0 && var4.click != null) {
               var4.click.run();
               Sound.click();
               return true;
            }

            if (var4.click != null || var4.right != null || var4.drag != null) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean dragging() {
      return active != null;
   }

   public static boolean drag(float var0, float var1) {
      if (active == null) {
         return false;
      } else {
         active.drag.accept(var0, var1);
         return true;
      }
   }

   public static boolean up() {
      boolean var0 = active != null;
      active = null;
      return var0;
   }

   public static boolean scroll(float var0, float var1, double var2) {
      for (int var4 = REGIONS.size() - 1; var4 >= 0; var4--) {
         Kit.Region var5 = REGIONS.get(var4);
         if (var5.scroll != null && var5.in(var0, var1)) {
            var5.scroll.accept(var2);
            return true;
         }
      }

      return false;
   }

   public static void toggle(String var0, int var1, int var2, int var3, int var4, boolean var5, float var6) {
      float var7 = Ease.to("tg:" + var0, var5 ? 1.0F : 0.0F, 16.0F);
      int var8 = Gx.mix(Style.light ? -2762529 : -12960441, Style.accent, var7);
      Gx.rect(var1, var2, var3, var4, var4 / 2, var8);
      if (Xmas.on() && var7 > 0.01F) {
         Gx.pushAlpha(var7);
         Xmas.candy(var1, var2, var3, var4, 0.0F);
         Gx.popAlpha();
      }

      int var9 = Math.max(2, Math.round(var4 * 0.14F));
      int var10 = var4 - var9 * 2;
      int var11 = Math.round(var1 + var9 + (var3 - var9 * 2 - var10) * Ease.outCubic(var7));
      Gx.shadow(var11, var2 + var9, var10, var10, var10 / 2, Math.max(1, Math.round(var6 * 1.2F)), 0.25F);
      Gx.circle(var11 + var10 / 2, var2 + var4 / 2, var10 / 2, -1);
   }

   public static void slider(String var0, int var1, int var2, int var3, int var4, float var5, float var6) {
      float var7 = Ease.to("sl:" + var0, var5, 22.0F);
      int var8 = Math.max(2, Math.round(var6 * 3.2F));
      int var9 = var2 + var4 / 2 - var8 / 2;
      Gx.rect(var1, var9, var3, var8, var8 / 2, Style.light ? -2499357 : -13420993);
      int var10 = Math.round(var3 * var7);
      Gx.rect(var1, var9, Math.max(var8, var10), var8, var8 / 2, Style.accent);
      int var11 = Math.round(var6 * 11.0F);
      int var12 = var1 + var10 - var11 / 2;
      Gx.shadow(var12, var2 + var4 / 2 - var11 / 2, var11, var11, var11 / 2, Math.max(1, Math.round(var6 * 1.5F)), 0.3F);
      Gx.circle(var12 + var11 / 2, var2 + var4 / 2, var11 / 2, -1);
      Gx.circle(var12 + var11 / 2, var2 + var4 / 2, Math.max(1, var11 / 5), Style.accent);
   }

   public static void button(String var0, String var1, String var2, int var3, int var4, int var5, int var6, float var7, boolean var8, boolean var9) {
      float var10 = Ease.to("bt:" + var0, var9 ? 1.0F : 0.0F, 14.0F);
      int var11 = var8 ? Style.accent : Style.surface2;
      int var12 = var8 ? Gx.mix(var11, -1, 0.12F * var10) : Gx.mix(var11, Style.surfaceHover, var10);
      Gx.rect(var3, var4, var5, var6, Math.round(var7 * 7.0F), var12);
      if (!var8) {
         Gx.outline(var3, var4, var5, var6, Math.round(var7 * 7.0F), Math.max(1, Math.round(var7 * 0.6F)), Gx.mix(Style.stroke, Style.strokeHi, var10));
      }

      int var13 = var8 ? Style.accentText : Style.text;
      float var14 = var7 * 8.6F;
      float var15 = Gx.width(var1, var14, 2);
      float var16 = var2 == null ? 0.0F : var7 * 11.0F + var7 * 5.0F;
      float var17 = var3 + (var5 - var15 - var16) / 2.0F;
      if (var2 != null) {
         Gx.icon(var2, var17 + var7 * 5.5F, var4 + var6 / 2.0F, var7 * 11.0F, var13);
      }

      Gx.textMid(var1, var17 + var16, var4 + var6 / 2.0F, var14, 2, var13);
   }

   public static void tooltip(String var0, float var1, float var2, float var3) {
      float var4 = var3 * 8.0F;
      int var5 = Math.round(Gx.width(var0, var4, 2) + var3 * 14.0F);
      int var6 = Math.round(var3 * 18.0F);
      int var7 = Math.round(var1 - var5 / 2.0F);
      int var8 = Math.round(var2 - var6);
      Gx.shadow(var7, var8, var5, var6, Math.round(var3 * 6.0F), Math.round(var3 * 4.0F), 0.35F);
      Gx.rect(var7, var8, var5, var6, Math.round(var3 * 6.0F), Style.light ? -15000542 : -789258);
      Gx.textCenter(var0, var7 + var5 / 2.0F, var8 + var6 / 2.0F, var4, 2, Style.light ? -1 : -15658474);
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

      Region(int var1, int var2, int var3, int var4) {
         this.x = var1;
         this.y = var2;
         this.w = var3;
         this.h = var4;
      }

      public Kit.Region click(Runnable var1) {
         this.click = var1;
         return this;
      }

      public Kit.Region right(Runnable var1) {
         this.right = var1;
         return this;
      }

      public Kit.Region drag(BiConsumer<Float, Float> var1) {
         this.drag = var1;
         return this;
      }

      public Kit.Region scroll(Consumer<Double> var1) {
         this.scroll = var1;
         return this;
      }

      public Kit.Region id(String var1) {
         this.id = var1;
         return this;
      }

      public String idOrNull() {
         return this.id;
      }

      boolean in(float var1, float var2) {
         return var1 >= this.x && var2 >= this.y && var1 < this.x + this.w && var2 < this.y + this.h;
      }
   }
}
