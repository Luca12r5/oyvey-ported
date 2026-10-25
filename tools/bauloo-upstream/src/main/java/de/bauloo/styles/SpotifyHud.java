package de.bauloo.styles;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_11719.class_11721;
import org.joml.Matrix3x2fStack;

public final class SpotifyHud {
   private static final long POPUP_MS = 5000L;
   private static final long FADE_MS = 400L;
   private static double worldFactor;
   public static final int W = 200;
   public static final int H = 46;
   private static final class_2960 CIRCLE = class_2960.method_60655("bauloo_styles", "textures/gui/hud_circle.png");
   private static final class_2960 GLOW = class_2960.method_60655("bauloo_styles", "textures/gui/hud_glow.png");
   private static final class_2960 FONT = class_2960.method_60655("bauloo_styles", "hud");

   private SpotifyHud() {
   }

   public static void register() {
      SpotifyNowPlaying.start();
      HudElementRegistry.addLast(class_2960.method_60655("bauloo_styles", "spotify"), (ctx, tickCounter) -> render(ctx));
   }

   private static void render(class_332 ctx) {
      class_310 mc = class_310.method_1551();
      StylesUiConfig.load();
      String title = SpotifyNowPlaying.title;
      if (StylesUiConfig.spotifyHud && title != null && !mc.field_1690.field_1842) {
         float alpha = 1.0F;
         if (StylesUiConfig.spotifyPopup) {
            long age = System.currentTimeMillis() - SpotifyNowPlaying.changedAt;
            if (age > 5000L) {
               return;
            }

            alpha = Math.min(1.0F, (float)Math.min(age, 5000L - age) / 400.0F);
         }

         double factor = mc.method_22683().method_4495();
         if (mc.field_1755 == null || worldFactor <= 0.0) {
            worldFactor = factor;
         }

         float k = (float)(worldFactor / factor);
         float size = StylesUiConfig.spotifyScale * k;
         int sw = ctx.method_51421();
         int sh = ctx.method_51443();
         int cardW = Math.round(200.0F * size);
         int cardH = Math.round(46.0F * size);
         int x = StylesUiConfig.spotifyX < 0.0F ? Math.round(6.0F * k) : Math.round(StylesUiConfig.spotifyX * Math.max(0, sw - cardW));
         int y = StylesUiConfig.spotifyY < 0.0F ? Math.round(6.0F * k) : Math.round(StylesUiConfig.spotifyY * Math.max(0, sh - cardH));
         Matrix3x2fStack m = ctx.method_51448();
         m.pushMatrix();
         m.translate(x, y);
         m.scale(size, size);
         draw(ctx, mc.field_1772, 0, 0, title, SpotifyNowPlaying.artist, alpha);
         m.popMatrix();
      }
   }

   public static int w() {
      return Math.round(200.0F * StylesUiConfig.spotifyScale);
   }

   public static int h() {
      return Math.round(46.0F * StylesUiConfig.spotifyScale);
   }

   public static int x(int screenW) {
      return StylesUiConfig.spotifyX < 0.0F ? 6 : Math.round(StylesUiConfig.spotifyX * Math.max(0, screenW - w()));
   }

   public static int y(int screenH) {
      return StylesUiConfig.spotifyY < 0.0F ? 6 : Math.round(StylesUiConfig.spotifyY * Math.max(0, screenH - h()));
   }

   public static void drawScaled(class_332 ctx, class_327 font, int x, int y, String title, String artist, float alpha) {
      Matrix3x2fStack m = ctx.method_51448();
      m.pushMatrix();
      m.translate(x, y);
      m.scale(StylesUiConfig.spotifyScale, StylesUiConfig.spotifyScale);
      draw(ctx, font, 0, 0, title, artist, alpha);
      m.popMatrix();
   }

   public static void draw(class_332 ctx, class_327 font, int x, int y, String title, String artist, float alpha) {
      int a = Math.round(alpha * 255.0F);
      if (a >= 8) {
         String key = artist + "\n" + title;
         boolean hasCover = key.equals(SpotifyCover.coverKey);
         int color = hasCover && SpotifyCover.color >= 0 ? SpotifyCover.color : StylesUiConfig.accent & 16777215;
         int cs = 42;
         int cy = y + 2;
         ctx.method_25293(class_10799.field_56883, GLOW, x - 7, cy - 5, 0.0F, 0.0F, cs + 14, cs + 14, 64, 64, 64, 64, Math.round(alpha * 176.0F) << 24);
         if (hasCover) {
            ctx.method_25293(class_10799.field_56883, SpotifyCover.TEXTURE, x, cy, 0.0F, 0.0F, cs, cs, 300, 300, 300, 300, a << 24 | 16777215);
         } else {
            roundRect(ctx, x, cy, cs, cs, 6, Math.round(alpha * 192.0F) << 24 | color);
            equaliser(ctx, x + cs / 2 - 7, cy + cs / 2 + 7, 4, 4, 14, a << 24 | 16777215);
         }

         int lx = x + cs + 6;
         ctx.method_25294(lx, cy + 1, lx + 1, cy + cs - 1, a << 24 | color);
         int tx = lx + 6;
         int textMax = x + 200 - tx;
         Matrix3x2fStack m = ctx.method_51448();
         m.pushMatrix();
         m.translate(tx, cy + 1);
         m.scale(0.6F, 0.6F);
         ctx.method_27535(font, styled("NOW PLAYING"), 0, 0, a << 24 | color);
         m.popMatrix();
         m.pushMatrix();
         m.translate(tx, cy + 9);
         m.scale(1.25F, 1.25F);
         ctx.method_27535(font, trim(font, title, Math.round(textMax / 1.25F)), 0, 0, a << 24 | 16777215);
         m.popMatrix();
         ctx.method_27535(font, trim(font, artist == null ? "" : artist, textMax), tx, cy + 22, a << 24 | 15131886);
         boolean current = key.equals(SpotifyNowPlaying.artist + "\n" + SpotifyNowPlaying.title);
         boolean exact = current && SpotifyNowPlaying.timeline;
         long length = exact ? SpotifyNowPlaying.timelineLengthMs : SpotifyCover.durationMs;
         boolean known = exact || current && SpotifyNowPlaying.knownStart && length > 0L;
         if (known) {
            long played = exact ? SpotifyNowPlaying.timelineMs() : Math.min(length, SpotifyNowPlaying.elapsedMs());
            m.pushMatrix();
            m.translate(tx, cy + 34);
            m.scale(0.7F, 0.7F);
            ctx.method_27535(font, styled(time(played) + "  /  " + time(length)), 0, 0, a << 24 | 14210786);
            m.popMatrix();
         }
      }
   }

   private static String time(long ms) {
      long s = ms / 1000L;
      return s / 60L + ":" + String.format("%02d", s % 60L);
   }

   private static void equaliser(class_332 ctx, int x, int baseY, int bars, int step, int maxH, int color) {
      long now = System.currentTimeMillis();

      for (int i = 0; i < bars; i++) {
         double wave = Math.sin(now / 170.0 + i * 1.7) * 0.5 + 0.5;
         int bh = 2 + (int)Math.round(wave * (maxH - 2));
         ctx.method_25294(x + i * step, baseY - bh, x + i * step + step - 1, baseY, color);
      }
   }

   private static void roundRect(class_332 ctx, int x, int y, int w, int h, int r, int color) {
      ctx.method_25293(class_10799.field_56883, CIRCLE, x, y, 0.0F, 0.0F, r, r, 32, 32, 64, 64, color);
      ctx.method_25293(class_10799.field_56883, CIRCLE, x + w - r, y, 32.0F, 0.0F, r, r, 32, 32, 64, 64, color);
      ctx.method_25293(class_10799.field_56883, CIRCLE, x, y + h - r, 0.0F, 32.0F, r, r, 32, 32, 64, 64, color);
      ctx.method_25293(class_10799.field_56883, CIRCLE, x + w - r, y + h - r, 32.0F, 32.0F, r, r, 32, 32, 64, 64, color);
      ctx.method_25294(x + r, y, x + w - r, y + h, color);
      ctx.method_25294(x, y + r, x + r, y + h - r, color);
      ctx.method_25294(x + w - r, y + r, x + w, y + h - r, color);
   }

   private static class_2561 styled(String value) {
      return class_2561.method_43470(value).method_27694(style -> style.method_27704(new class_11721(FONT)));
   }

   private static class_2561 trim(class_327 font, String value, int max) {
      class_2561 full = styled(value);
      if (font.method_27525(full) <= max) {
         return full;
      }

      String cut = value;

      while (cut.length() > 1 && font.method_27525(styled(cut + "...")) > max) {
         cut = cut.substring(0, cut.length() - 1);
      }

      return styled(cut.trim() + "...");
   }
}
