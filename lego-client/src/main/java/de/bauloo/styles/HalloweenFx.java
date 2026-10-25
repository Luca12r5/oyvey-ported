package de.bauloo.styles;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class HalloweenFx {
   private static final Identifier PUMPKIN = tex("halloween_pumpkin");
   private static final Identifier BAT = tex("halloween_bat");
   private static final Identifier WEB = tex("halloween_web");
   private static final Identifier GLOW = tex("halloween_moon");
   private static final Identifier LEAF = tex("halloween_leaf");
   private static final Identifier GHOST = tex("halloween_ghost");
   private static final Identifier GRAVE = tex("halloween_grave");
   private static final Identifier SPIDER = tex("halloween_spider");
   private static final int LEAVES = 14;
   private static final int EMBERS = 10;
   private static final int[] LEAF_COLORS = new int[]{14711339, 12597547, 9329195, 15906106};
   private static final float[][] BATS = new float[][]{
      {0.1F, 10.0F, 22.0F, 0.0F}, {0.3F, 4.0F, 15.0F, 1.7F}, {0.46F, 16.0F, 12.0F, 3.1F}, {0.7F, 7.0F, 24.0F, 4.4F}, {0.88F, 2.0F, 16.0F, 5.6F}
   };

   private HalloweenFx() {
   }

   private static Identifier tex(String name) {
      return Identifier.fromNamespaceAndPath("bauloo_styles", "textures/gui/" + name + ".png");
   }

   private static float time() {
      return (float)(System.nanoTime() / 1000000L % 3600000L) / 1000.0F;
   }

   private static float rand(int i, int k) {
      double v = Math.sin(i * 12.9898 + k * 78.233) * 43758.5453;
      return (float)(v - Math.floor(v));
   }

   private static int argb(float alpha, int rgb) {
      return Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) << 24 | rgb & 16777215;
   }

   public static void moon(GuiGraphics ctx, int x, int y, int w, int headerH) {
      ctx.enableScissor(x + 1, y + 1, x + w - 1, y + headerH);
      ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, x - 26, y - 34, 0.0F, 0.0F, 96, 96, 256, 256, 256, 256, 1728027196);
      int mx = x + w * 58 / 100;
      ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, mx - 70, y - 70, 0.0F, 0.0F, 140, 140, 256, 256, 256, 256, 1090484766);
      ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, mx - 34, y - 40, 0.0F, 0.0F, 68, 68, 256, 256, 256, 256, -1929400224);
      ctx.disableScissor();
   }

   public static void pumpkin(GuiGraphics ctx, int x, int y, int size) {
      ctx.blit(RenderPipelines.GUI_TEXTURED, PUMPKIN, x, y, 0.0F, 0.0F, size, size, 128, 128, 128, 128);
   }

   public static void bats(GuiGraphics ctx, int left, int right, int top) {
      if (right - left >= 80) {
         float t = time();
         Matrix3x2fStack m = ctx.pose();

         for (float[] b : BATS) {
            float x = left + (right - left) * b[0] + (float)Math.sin(t * 0.35F + b[3]) * 18.0F;
            float y = top + b[1] + (float)Math.sin(t * 1.2F + b[3]) * 2.5F;
            float flap = 0.45F + 0.55F * Math.abs((float)Math.sin(t * 8.0F + b[3]));
            int w = Math.round(b[2]);
            int h = Math.max(2, w / 2);
            m.pushMatrix();
            m.translate(x, y);
            m.scale(1.0F, flap);
            ctx.blit(RenderPipelines.GUI_TEXTURED, BAT, -w / 2 - 1, -h / 2 - 1, 0.0F, 0.0F, w + 2, h + 2, 128, 64, 128, 64, 1442810399);
            ctx.blit(RenderPipelines.GUI_TEXTURED, BAT, -w / 2, -h / 2, 0.0F, 0.0F, w, h, 128, 64, 128, 64, -266925540);
            m.popMatrix();
         }
      }
   }

   public static void behind(GuiGraphics ctx, int fx, int fy, int fw, int headerH, int mainX, int mainW, int footerY) {
      float t = time();
      ctx.fillGradient(fx + 1, fy + headerH, fx + fw - 1, fy + headerH + 80, 446389503, 10181887);
      ctx.fillGradient(fx + 1, footerY - 120, fx + fw - 1, footerY, 16739860, 620719636);
      int gh = Math.max(16, Math.min(30, mainW / 30));
      ctx.blit(RenderPipelines.GUI_TEXTURED, GRAVE, mainX, footerY - gh, 0.0F, 0.0F, mainW, gh, 1024, 96, 1024, 96);
      float[] at = new float[]{0.21F, 0.53F, 0.81F};
      int[] sizes = new int[]{10, 13, 9};

      for (int i = 0; i < at.length; i++) {
         int s = sizes[i];
         int px = mainX + Math.round(mainW * at[i]);
         float flicker = 0.78F + 0.22F * (float)Math.sin(t * 3.1F + i * 1.9F) * (float)Math.sin(t * 7.3F + i);
         ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, px - s, footerY - s - s, 0.0F, 0.0F, s * 3, s * 3, 256, 256, 256, 256, argb(0.35F * flicker, 16747039));
         ctx.blit(RenderPipelines.GUI_TEXTURED, PUMPKIN, px, footerY - s - 2, 0.0F, 0.0F, s, s, 128, 128, 128, 128, argb(flicker, 16777215));
      }

      ctx.fillGradient(mainX, footerY - 60, mainX + mainW, footerY, 10181887, 781933823);
      float cycle = t % 26.0F;
      if (cycle < 11.0F) {
         float p = cycle / 11.0F;
         float alpha = 0.6F * (float)Math.sin(Math.PI * p);
         float gx = mainX + mainW * (0.12F + 0.7F * p);
         float gy = footerY - 62 + (float)Math.sin(t * 1.8F) * 4.0F;
         Matrix3x2fStack m = ctx.pose();
         m.pushMatrix();
         m.translate(gx, gy);
         ctx.blit(RenderPipelines.GUI_TEXTURED, GHOST, -15, -17, 0.0F, 0.0F, 30, 35, 96, 112, 96, 112, argb(alpha, 16777215));
         m.popMatrix();
      }
   }

   public static void front(GuiGraphics ctx, int fx, int fy, int fw, int fh, int headerH, int footerY, boolean side, int sideX, int sideW) {
      float t = time();
      int rim = 1509919263;
      ctx.fill(fx + 5, fy, fx + fw - 5, fy + 1, rim);
      ctx.fill(fx + 5, fy + fh - 1, fx + fw - 5, fy + fh, rim);
      ctx.fill(fx, fy + 5, fx + 1, fy + fh - 5, rim);
      ctx.fill(fx + fw - 1, fy + 5, fx + fw, fy + fh - 5, rim);
      int web = Math.min(80, fh / 5);
      Matrix3x2fStack m = ctx.pose();
      m.pushMatrix();
      m.translate(fx + fw - 1, fy + 1);
      m.scale(-1.0F, 1.0F);
      ctx.blit(RenderPipelines.GUI_TEXTURED, WEB, 0, 0, 0.0F, 0.0F, web, web, 256, 256, 256, 256, 1509949439);
      m.popMatrix();
      int small = web * 3 / 4;
      m.pushMatrix();
      m.translate(fx + 1, fy + fh - 1);
      m.scale(1.0F, -1.0F);
      ctx.blit(RenderPipelines.GUI_TEXTURED, WEB, 0, 0, 0.0F, 0.0F, small, small, 256, 256, 256, 256, 989855743);
      m.popMatrix();
      ctx.blit(RenderPipelines.GUI_TEXTURED, WEB, fx + 1, fy + 1, 0.0F, 0.0F, small * 2 / 3, small * 2 / 3, 256, 256, 256, 256, 788529151);
      if (side) {
         float sx = sideX + sideW - 16;
         float sy = footerY - 140 + (float)Math.sin(t * 0.7F) * 35.0F;
         int top = fy + headerH;
         ctx.fill(Math.round(sx), top, Math.round(sx) + 1, Math.round(sy) - 4, 1441916159);
         m.pushMatrix();
         m.translate(sx + 0.5F, sy);
         m.rotate((float)Math.sin(t * 1.4F) * 0.15F);
         ctx.blit(RenderPipelines.GUI_TEXTURED, SPIDER, -8, -6, 0.0F, 0.0F, 16, 16, 64, 64, 64, 64);
         m.popMatrix();
      }

      int areaTop = fy + headerH;
      int areaH = footerY - areaTop;
      if (areaH > 40) {
         for (int i = 0; i < 14; i++) {
            float speed = 14.0F + rand(i, 1) * 16.0F;
            float span = areaH + 30;
            float y = areaTop - 15 + (t * speed + rand(i, 2) * span) % span;
            float baseX = fx + 10 + rand(i, 3) * (fw - 20);
            float x = baseX + (float)Math.sin(t * (0.6F + rand(i, 4) * 0.5F) + i) * 16.0F;
            float rot = (float)Math.sin(t * 1.3F + i * 2.1F) * 0.9F + t * (rand(i, 5) - 0.5F);
            int size = 6 + Math.round(rand(i, 6) * 5.0F);
            float fade = Math.min(1.0F, (footerY - y) / 30.0F);
            if (!(fade <= 0.0F)) {
               m.pushMatrix();
               m.translate(x, y);
               m.rotate(rot);
               ctx.blit(
                  RenderPipelines.GUI_TEXTURED,
                  LEAF,
                  -size / 2,
                  -size / 2,
                  0.0F,
                  0.0F,
                  size,
                  size,
                  64,
                  64,
                  64,
                  64,
                  argb(0.8F * fade, LEAF_COLORS[i % LEAF_COLORS.length])
               );
               m.popMatrix();
            }
         }
      }

      for (int ix = 0; ix < 10; ix++) {
         float speed = 10.0F + rand(ix, 7) * 12.0F;
         float rise = Math.min(180.0F, areaH * 0.6F);
         float p = (t * speed + rand(ix, 8) * rise) % rise / rise;
         float x = fx + 20 + rand(ix, 9) * (fw - 40) + (float)Math.sin(t * 1.7F + ix) * 6.0F;
         float y = footerY - 6 - p * rise;
         int s = 3 + ix % 2;
         m.pushMatrix();
         m.translate(x, y);
         ctx.blit(RenderPipelines.GUI_TEXTURED, GLOW, -s * 2, -s * 2, 0.0F, 0.0F, s * 4, s * 4, 256, 256, 256, 256, argb(0.9F * (1.0F - p), 16757325));
         m.popMatrix();
      }
   }
}
