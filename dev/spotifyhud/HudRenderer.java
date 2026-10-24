package dev.spotifyhud;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

final class HudRenderer {
   private final TexSlot hudSlot = new TexSlot(Identifier.fromNamespaceAndPath("spotifyhud", "hud_panel"));
   private final TexSlot lyricsSlot = new TexSlot(Identifier.fromNamespaceAndPath("spotifyhud", "lyrics_panel"));
   private boolean awtFailed;
   private int lastLyricIndex = Integer.MIN_VALUE;
   private String lastLyricKey = "";
   private long lyricChangedAt = 0L;
   private static final long T0 = System.currentTimeMillis();

   int[] bounds(int var1, int var2, Config var3) {
      double var4 = effectiveScale(var1, var3);
      int var6 = Math.max(20, (int)Math.round(207.0 * var4));
      int var7 = Math.max(7, (int)Math.round(70.0 * var4));
      int var8 = (int)Math.round(Config.clamp01(var3.posX) * Math.max(0, var1 - var6));
      int var9 = (int)Math.round(Config.clamp01(var3.posY) * Math.max(0, var2 - var7));
      return new int[]{var8, var9, var6, var7};
   }

   private static double effectiveScale(int var0, Config var1) {
      double var2 = var1.scale;
      if (207.0 * var2 > var0 - 2) {
         var2 = (var0 - 2) / 207.0;
      }

      return var2;
   }

   int[] lyricsBounds(int[] var1, int var2, int var3, Config var4) {
      double var5 = var1[2] / 207.0;
      int var7 = Math.max(6, (int)Math.round(40.0 * var5));
      int var8 = Math.max(1, (int)Math.round(3.0 * var5));
      int var9 = var1[1] + var1[3] + var8;
      if (var9 + var7 > var3) {
         var9 = var1[1] - var8 - var7;
      }

      return new int[]{var1[0], var9, var1[2], var7};
   }

   void render(GuiGraphics var1, int var2, int var3, int var4, int var5, HudPainter.View var6) {
      Minecraft var7 = Minecraft.getInstance();
      int var8 = Math.max(1, var7.getWindow().getGuiScale());
      int var9 = var4 * var8;
      int var10 = var5 * var8;
      if (!this.awtFailed && var9 > 0 && var10 > 0 && var9 <= 8192 && var10 <= 8192) {
         try {
            HudPainter.Style var11 = style();
            double var12 = animTime();
            String var14 = key(var6, var9, var10);
            String var15 = var14 + "|" + var11.theme + "|" + (Anim.active(var11.anim) ? var11.anim + frame(var12) + var11.intensity : "");
            this.hudSlot.draw(var1, var7, var2, var3, var4, var5, var9, var10, var15, () -> HudPainter.render(var6, var9, var10, var11, var12, var14));
            return;
         } catch (Throwable var16) {
            this.awtFailed = true;
            SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + var16);
         }
      }

      this.renderFallback(var1, var7, var2, var3, var4, var5, var6);
   }

   static HudPainter.Style style() {
      Config var0 = SpotifyHudMod.config();
      HudPainter.Style var1 = new HudPainter.Style();
      var1.theme = var0.theme;
      var1.anim = var0.animation;
      var1.intensity = var0.animIntensity;
      var1.animOnLyrics = var0.animOnLyrics;
      var1.karaoke = var0.karaoke;
      var1.grow = var0.wordGrow;
      return var1;
   }

   static double animTime() {
      return (System.currentTimeMillis() - T0) / 1000.0;
   }

   static long frame(double var0) {
      return (long)(var0 * 30.0);
   }

   private static String key(HudPainter.View var0, int var1, int var2) {
      PlayerState var3 = var0.st;
      double var4 = var1 / 207.0;
      long var6 = var3.durationMs;
      long var8 = var0.progressPreview >= 0.0 ? (long)(var0.progressPreview * var6) : var3.currentProgress();
      double var10 = var0.progressPreview >= 0.0 ? var0.progressPreview : (var6 > 0L ? (double)var8 / var6 : 0.0);
      long var12 = Math.round(var10 * 146.5 * var4 * 2.0);
      StringBuilder var14 = new StringBuilder(160);
      var14.append(var1)
         .append('x')
         .append(var2)
         .append('|')
         .append(var0.title)
         .append('|')
         .append(var0.artist)
         .append('|')
         .append(var0.showTime)
         .append(var0.showControls)
         .append('|')
         .append(var0.showTime ? var8 / 1000L : -1L)
         .append('|')
         .append(var6 / 1000L)
         .append('|')
         .append(var12)
         .append('|')
         .append(var0.volumePreview >= 0 ? var0.volumePreview : var3.volume)
         .append('|')
         .append(var3.playing)
         .append(var3.shuffle)
         .append(var3.repeat)
         .append(var3.liked)
         .append('|')
         .append(var0.hover)
         .append(var0.interactive)
         .append(var0.progressPreview >= 0.0)
         .append(var0.volumePreview >= 0)
         .append('|')
         .append(SpotifyHudMod.media().artVersion())
         .append('|')
         .append(System.identityHashCode(var0.art));
      return var14.toString();
   }

   void renderLyrics(GuiGraphics var1, int[] var2, Lyrics.Result var3, PlayerState var4, Config var5, boolean var6, boolean var7) {
      Minecraft var8 = Minecraft.getInstance();
      HudPainter.LyricsView var9 = new HudPainter.LyricsView();
      var9.interactive = var6;
      var9.hover = var7;
      long var10 = System.currentTimeMillis();
      switch (var3.status) {
         case LOADING:
            var9.message = "Lyrics werden geladen...";
            break;
         case NOT_FOUND:
            var9.message = "Keine Lyrics gefunden";
            break;
         case INSTRUMENTAL:
            var9.message = "♪ Instrumental ♪";
            break;
         case ERROR:
            var9.message = "Lyrics-Server nicht erreichbar";
            break;
         case PLAIN:
            var9.message = "Nur ungesyncte Lyrics verfügbar";
            break;
         case NONE:
            var9.message = var4.hasTrack ? "Lyrics werden geladen..." : "Keine Wiedergabe";
            break;
         case SYNCED:
            List var12 = var3.lines;
            long var13 = var4.currentProgress() + var5.lyricsOffsetMs;
            int var15 = Lyrics.indexAt(var12, var13);
            if (!var3.key.equals(this.lastLyricKey)) {
               this.lastLyricKey = var3.key;
               this.lastLyricIndex = var15;
               this.lyricChangedAt = 0L;
            }

            if (var15 != this.lastLyricIndex) {
               this.lyricChangedAt = var15 == this.lastLyricIndex + 1 ? var10 : 0L;
               this.lastLyricIndex = var15;
            }

            var9.prev = var15 - 1 >= 0 ? text((Lyrics.Line)var12.get(var15 - 1)) : "";
            var9.next = var15 + 1 < var12.size() ? text((Lyrics.Line)var12.get(var15 + 1)) : "";
            if (var15 >= 0) {
               Lyrics.Line var16 = (Lyrics.Line)var12.get(var15);
               var9.curText = text(var16);
               var9.words = var16.words;
               int var17 = var16.wordAt(var13);
               var9.wordIdx = var17;
               var9.grow = var17 >= 0 ? Math.min(1.0, (var13 - var16.wordTimes[var17]) / 150.0) : 1.0;
            } else {
               var9.curText = "♪";
               var9.words = new String[0];
               var9.wordIdx = -1;
            }

            double var25 = this.lyricChangedAt == 0L ? 1.0 : (var10 - this.lyricChangedAt) / 260.0;
            var9.anim = Math.max(0.0, Math.min(1.0, var25));
      }

      var9.art = SpotifyHudMod.media().art();
      int var22 = Math.max(1, var8.getWindow().getGuiScale());
      int var23 = var2[2] * var22;
      int var14 = var2[3] * var22;
      if (!this.awtFailed && var23 > 0 && var14 > 0 && var23 <= 8192 && var14 <= 8192) {
         try {
            HudPainter.Style var24 = style();
            double var26 = animTime();
            String var18 = var23
               + "x"
               + var14
               + "|"
               + var9.message
               + "|"
               + var9.prev
               + "|"
               + var9.curText
               + "|"
               + var9.next
               + "|"
               + Math.round(var9.anim * 24.0)
               + "|"
               + var9.wordIdx
               + "|"
               + Math.round(var9.grow * 10.0)
               + "|"
               + var6
               + var7
               + "|"
               + var24.karaoke
               + var24.grow
               + "|"
               + System.identityHashCode(var9.art);
            String var19 = var18 + "|" + var24.theme + (Anim.active(var24.anim) && var24.animOnLyrics ? var24.anim + frame(var26) + var24.intensity : "");
            this.lyricsSlot
               .draw(
                  var1, var8, var2[0], var2[1], var2[2], var2[3], var23, var14, var19, () -> HudPainter.renderLyrics(var9, var23, var14, var24, var26, var18)
               );
         } catch (Throwable var21) {
            this.awtFailed = true;
            SpotifyHudMod.LOG("Java2D-Rendering nicht verfügbar, nutze Fallback: " + var21);
            lyricsFallback(var1, var8, var2, var9);
         }
      } else {
         lyricsFallback(var1, var8, var2, var9);
      }
   }

   private static String text(Lyrics.Line var0) {
      return var0.text.isEmpty() ? "♪" : var0.text;
   }

   private static void lyricsFallback(GuiGraphics var0, Minecraft var1, int[] var2, HudPainter.LyricsView var3) {
      Font var4 = var1.font;
      var0.fill(var2[0], var2[1], var2[0] + var2[2], var2[1] + var2[3], -367914478);
      int var5 = var2[0] + var2[2] / 2;
      if (var3.message != null) {
         var0.drawCenteredString(var4, trim(var4, var3.message, var2[2] - 8), var5, var2[1] + var2[3] / 2 - 4, -5000269);
      } else {
         var0.drawCenteredString(var4, trim(var4, var3.curText, var2[2] - 8), var5, var2[1] + var2[3] / 2 - 4, -1);
         var0.drawCenteredString(var4, trim(var4, var3.next, var2[2] - 8), var5, var2[1] + var2[3] / 2 + 7, -7697782);
      }
   }

   void renderToast(GuiGraphics var1, int[] var2, int[] var3, int var4, String var5, long var6) {
      long var8 = System.currentTimeMillis() - var6;
      if (var5 != null && !var5.isEmpty() && var8 <= 4000L) {
         Minecraft var10 = Minecraft.getInstance();
         Font var11 = var10.font;
         int var12 = var8 > 3400L ? (int)(255L * (4000L - var8) / 600.0) : 255;
         if (var12 >= 8) {
            int var13 = Math.max(var2[1] + var2[3], var3 != null ? var3[1] + var3[3] : 0);
            int var14 = var13 + 3;
            if (var14 + 12 > var4) {
               var14 = Math.min(var2[1], var3 != null ? var3[1] : var2[1]) - 13;
            }

            String var15 = trim(var11, var5, Math.max(120, var2[2] + 60));
            int var16 = var11.width(var15);
            int var17 = var2[0] + var2[2] / 2 - var16 / 2;
            var1.fill(var17 - 4, var14 - 2, var17 + var16 + 4, var14 + 10, var12 * 208 / 255 << 24 | 1184274);
            var1.drawString(var11, var15, var17, var14, var12 << 24 | 16777215);
         }
      }
   }

   private void renderFallback(GuiGraphics var1, Minecraft var2, int var3, int var4, int var5, int var6, HudPainter.View var7) {
      Font var8 = var2.font;
      double var9 = var5 / 207.0;
      var1.fill(var3, var4, var3 + var5, var4 + var6, -367914478);
      int var11 = var3 + (int)(9.0 * var9);
      int var12 = var4 + (int)(9.0 * var9);
      int var13 = (int)(37.0 * var9);
      var1.fill(var11, var12, var11 + var13, var12 + var13, -14145496);
      int var14 = var3 + (int)(53.0 * var9);
      int var15 = (int)(144.0 * var9);
      var1.drawString(var8, trim(var8, var7.title, var15), var14, var4 + (int)(10.0 * var9), -1381141, false);
      if (var7.showTime) {
         long var16 = var7.st.durationMs;
         long var18 = var7.progressPreview >= 0.0 ? (long)(var7.progressPreview * var16) : var7.st.currentProgress();
         String var20 = PlayerState.fmt(var18) + " / " + PlayerState.fmt(var16);
         int var21 = var8.width(var20);
         var1.drawString(var8, var20, var3 + (int)(197.0 * var9) - var21, var4 + (int)(22.0 * var9), -5789785, false);
         var15 -= var21 + 6;
      }

      var1.drawString(var8, trim(var8, var7.artist, var15), var14, var4 + (int)(22.0 * var9), -5000269, false);
      double var28 = var7.progressPreview >= 0.0
         ? var7.progressPreview
         : (var7.st.durationMs > 0L ? (double)var7.st.currentProgress() / var7.st.durationMs : 0.0);
      int var29 = var3 + (int)(52.5 * var9);
      int var19 = var4 + (int)(36.6 * var9);
      int var30 = (int)(146.5 * var9);
      int var31 = Math.max(2, (int)Math.round(3.4 * var9));
      var1.fill(var29, var19, var29 + var30, var19 + var31, -12697793);
      var1.fill(var29, var19, var29 + (int)(var30 * Math.max(0.0, Math.min(1.0, var28))), var19 + var31, -525833);
      if (var7.showControls) {
         int var22 = var7.volumePreview >= 0 ? var7.volumePreview : var7.st.volume;
         int var23 = var3 + (int)(21.0 * var9);
         int var24 = var4 + (int)(55.4 * var9);
         int var25 = (int)(37.5 * var9);
         int var26 = Math.max(2, (int)Math.round(3.2 * var9));
         var1.fill(var23, var24, var23 + var25, var24 + var26, -12697793);
         var1.fill(var23, var24, var23 + (int)(var25 * Math.max(0, var22) / 100.0), var24 + var26, -525833);
         int var27 = var4 + (int)(57.0 * var9) - 4;
         glyph(var1, var8, "⏮", 88.8, var3, var9, var27, -1);
         glyph(var1, var8, var7.st.playing ? "⏸" : "▶", 103.3, var3, var9, var27, -1);
         glyph(var1, var8, "⏭", 118.0, var3, var9, var27, -1);
         glyph(var1, var8, "↻", 167.2, var3, var9, var27, "off".equals(var7.st.repeat) ? -1 : -14756000);
         glyph(var1, var8, "⇄", 181.3, var3, var9, var27, var7.st.shuffle ? -14756000 : -1);
         glyph(var1, var8, "♥", 195.8, var3, var9, var27, var7.st.liked ? -14756000 : -1);
      }
   }

   private static void glyph(GuiGraphics var0, Font var1, String var2, double var3, int var5, double var6, int var8, int var9) {
      int var10 = var1.width(var2);
      var0.drawString(var1, var2, var5 + (int)(var3 * var6) - var10 / 2, var8, var9, false);
   }

   static String trim(Font var0, String var1, int var2) {
      if (var1 == null) {
         return "";
      } else if (var0.width(var1) <= var2) {
         return var1;
      } else {
         String var3 = "...";
         int var4 = var1.length();

         while (var4 > 0 && var0.width(var1.substring(0, var4) + var3) > var2) {
            var4--;
         }

         return var1.substring(0, var4) + var3;
      }
   }
}
