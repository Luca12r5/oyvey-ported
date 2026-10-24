package dev.spotifyhud;

public final class HudLayout {
   public static final int W = 207;
   public static final int H = 70;
   public static final double ART_X = 9.0;
   public static final double ART_Y = 9.0;
   public static final double ART_S = 37.0;
   public static final double TEXT_X = 53.0;
   public static final double TEXT_RIGHT = 197.0;
   public static final double TITLE_BASE = 18.2;
   public static final double ARTIST_BASE = 29.0;
   public static final double PROG_X = 52.5;
   public static final double PROG_Y = 36.6;
   public static final double PROG_W = 146.5;
   public static final double PROG_H = 3.4;
   public static final double ROW_Y = 57.0;
   public static final double SPEAKER_X = 14.0;
   public static final double VOL_X = 21.0;
   public static final double VOL_Y = 55.4;
   public static final double VOL_W = 37.5;
   public static final double VOL_H = 3.2;
   public static final double PREV_X = 88.8;
   public static final double PLAY_X = 103.3;
   public static final double NEXT_X = 118.0;
   public static final double REPEAT_X = 167.2;
   public static final double SHUFFLE_X = 181.3;
   public static final double LIKE_X = 195.8;
   public static final double GEAR_X = 199.2;
   public static final double GEAR_Y = 8.2;

   private HudLayout() {
   }

   public static HudLayout.Region hit(double var0, double var2) {
      if (var0 < 0.0 || var2 < 0.0 || var0 > 207.0 || var2 > 70.0) {
         return null;
      } else if (near(var0, var2, 199.2, 8.2, 5.5)) {
         return HudLayout.Region.SETTINGS;
      } else {
         if (var2 >= 50.0 && var2 <= 64.5) {
            if (near(var0, var2, 14.0, 57.0, 5.5)) {
               return HudLayout.Region.SPEAKER;
            }

            if (var0 >= 19.5 && var0 <= 60.5) {
               return HudLayout.Region.VOLUME;
            }

            if (near(var0, var2, 88.8, 57.0, 6.5)) {
               return HudLayout.Region.PREV;
            }

            if (near(var0, var2, 103.3, 57.0, 6.5)) {
               return HudLayout.Region.PLAY;
            }

            if (near(var0, var2, 118.0, 57.0, 6.5)) {
               return HudLayout.Region.NEXT;
            }

            if (near(var0, var2, 167.2, 57.0, 6.5)) {
               return HudLayout.Region.REPEAT;
            }

            if (near(var0, var2, 181.3, 57.0, 6.5)) {
               return HudLayout.Region.SHUFFLE;
            }

            if (near(var0, var2, 195.8, 57.0, 6.5)) {
               return HudLayout.Region.LIKE;
            }
         }

         if (var0 >= 51.0 && var0 <= 200.5 && var2 >= 32.6 && var2 <= 44.0) {
            return HudLayout.Region.PROGRESS;
         } else {
            return var0 >= 9.0 && var0 <= 46.0 && var2 >= 9.0 && var2 <= 46.0 ? HudLayout.Region.ART : HudLayout.Region.PANEL;
         }
      }
   }

   private static boolean near(double var0, double var2, double var4, double var6, double var8) {
      return Math.abs(var0 - var4) <= var8 && Math.abs(var2 - var6) <= var8;
   }

   public static double progressFraction(double var0) {
      return Math.max(0.0, Math.min(1.0, (var0 - 52.5) / 146.5));
   }

   public static double volumeFraction(double var0) {
      return Math.max(0.0, Math.min(1.0, (var0 - 21.0) / 37.5));
   }

   public static enum Region {
      ART,
      PROGRESS,
      SPEAKER,
      VOLUME,
      PREV,
      PLAY,
      NEXT,
      REPEAT,
      SHUFFLE,
      LIKE,
      SETTINGS,
      PANEL;
   }
}
