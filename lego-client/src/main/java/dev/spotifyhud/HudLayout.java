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

   public static HudLayout.Region hit(double x, double y) {
      if (x < 0.0 || y < 0.0 || x > 207.0 || y > 70.0) {
         return null;
      } else if (near(x, y, 199.2, 8.2, 5.5)) {
         return HudLayout.Region.SETTINGS;
      } else {
         if (y >= 50.0 && y <= 64.5) {
            if (near(x, y, 14.0, 57.0, 5.5)) {
               return HudLayout.Region.SPEAKER;
            }

            if (x >= 19.5 && x <= 60.5) {
               return HudLayout.Region.VOLUME;
            }

            if (near(x, y, 88.8, 57.0, 6.5)) {
               return HudLayout.Region.PREV;
            }

            if (near(x, y, 103.3, 57.0, 6.5)) {
               return HudLayout.Region.PLAY;
            }

            if (near(x, y, 118.0, 57.0, 6.5)) {
               return HudLayout.Region.NEXT;
            }

            if (near(x, y, 167.2, 57.0, 6.5)) {
               return HudLayout.Region.REPEAT;
            }

            if (near(x, y, 181.3, 57.0, 6.5)) {
               return HudLayout.Region.SHUFFLE;
            }

            if (near(x, y, 195.8, 57.0, 6.5)) {
               return HudLayout.Region.LIKE;
            }
         }

         if (x >= 51.0 && x <= 200.5 && y >= 32.6 && y <= 44.0) {
            return HudLayout.Region.PROGRESS;
         } else {
            return x >= 9.0 && x <= 46.0 && y >= 9.0 && y <= 46.0 ? HudLayout.Region.ART : HudLayout.Region.PANEL;
         }
      }
   }

   private static boolean near(double x, double y, double cx, double cy, double r) {
      return Math.abs(x - cx) <= r && Math.abs(y - cy) <= r;
   }

   public static double progressFraction(double x) {
      return Math.max(0.0, Math.min(1.0, (x - 52.5) / 146.5));
   }

   public static double volumeFraction(double x) {
      return Math.max(0.0, Math.min(1.0, (x - 21.0) / 37.5));
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
