package dev.spotifyhud;

/** Layout of the HUD in design units (the panel is 207 x 70 units, measured from the reference image). */
public final class HudLayout {
    private HudLayout() { }

    public static final int W = 207;
    public static final int H = 70;

    // album art
    public static final double ART_X = 9, ART_Y = 9, ART_S = 37;
    // text
    public static final double TEXT_X = 53, TEXT_RIGHT = 197;
    public static final double TITLE_BASE = 18.2, ARTIST_BASE = 29.0;
    // progress bar
    public static final double PROG_X = 52.5, PROG_Y = 36.6, PROG_W = 146.5, PROG_H = 3.4;
    // controls row
    public static final double ROW_Y = 57;
    public static final double SPEAKER_X = 14;
    public static final double VOL_X = 21, VOL_Y = 55.4, VOL_W = 37.5, VOL_H = 3.2;
    public static final double PREV_X = 88.8, PLAY_X = 103.3, NEXT_X = 118.0;
    public static final double REPEAT_X = 167.2, SHUFFLE_X = 181.3, LIKE_X = 195.8;

    public static final double GEAR_X = 199.2, GEAR_Y = 8.2;

    public enum Region { ART, PROGRESS, SPEAKER, VOLUME, PREV, PLAY, NEXT, REPEAT, SHUFFLE, LIKE, SETTINGS, PANEL }

    /** Hit test in design units. Returns null if outside the panel. */
    public static Region hit(double x, double y) {
        if (x < 0 || y < 0 || x > W || y > H) return null;
        if (near(x, y, GEAR_X, GEAR_Y, 5.5)) return Region.SETTINGS;
        if (y >= 50 && y <= 64.5) {
            if (near(x, y, SPEAKER_X, ROW_Y, 5.5)) return Region.SPEAKER;
            if (x >= VOL_X - 1.5 && x <= VOL_X + VOL_W + 2) return Region.VOLUME;
            if (near(x, y, PREV_X, ROW_Y, 6.5)) return Region.PREV;
            if (near(x, y, PLAY_X, ROW_Y, 6.5)) return Region.PLAY;
            if (near(x, y, NEXT_X, ROW_Y, 6.5)) return Region.NEXT;
            if (near(x, y, REPEAT_X, ROW_Y, 6.5)) return Region.REPEAT;
            if (near(x, y, SHUFFLE_X, ROW_Y, 6.5)) return Region.SHUFFLE;
            if (near(x, y, LIKE_X, ROW_Y, 6.5)) return Region.LIKE;
        }
        if (x >= PROG_X - 1.5 && x <= PROG_X + PROG_W + 1.5 && y >= PROG_Y - 4 && y <= PROG_Y + PROG_H + 4) return Region.PROGRESS;
        if (x >= ART_X && x <= ART_X + ART_S && y >= ART_Y && y <= ART_Y + ART_S) return Region.ART;
        return Region.PANEL;
    }

    private static boolean near(double x, double y, double cx, double cy, double r) {
        return Math.abs(x - cx) <= r && Math.abs(y - cy) <= r;
    }

    public static double progressFraction(double x) {
        return Math.max(0, Math.min(1, (x - PROG_X) / PROG_W));
    }

    public static double volumeFraction(double x) {
        return Math.max(0, Math.min(1, (x - VOL_X) / VOL_W));
    }
}
