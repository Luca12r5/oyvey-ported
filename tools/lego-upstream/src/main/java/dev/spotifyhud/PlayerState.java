package dev.spotifyhud;

/** Immutable snapshot of the playback state shown by the HUD. */
public final class PlayerState {
    public final boolean hasTrack;
    public final String trackId;      // null for episodes / local files
    public final String uri;
    public final String title;
    public final String artist;
    public final String artUrl;
    public final long durationMs;
    public final long progressMs;
    public final long sampledAt;      // System.currentTimeMillis() when progressMs was valid
    public final boolean playing;
    public final boolean shuffle;
    public final String repeat;       // off | context | track
    public final int volume;          // 0..100, -1 unknown
    public final boolean liked;

    public PlayerState(boolean hasTrack, String trackId, String uri, String title, String artist, String artUrl,
                       long durationMs, long progressMs, long sampledAt, boolean playing, boolean shuffle,
                       String repeat, int volume, boolean liked) {
        this.hasTrack = hasTrack;
        this.trackId = trackId;
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.artUrl = artUrl;
        this.durationMs = durationMs;
        this.progressMs = progressMs;
        this.sampledAt = sampledAt;
        this.playing = playing;
        this.shuffle = shuffle;
        this.repeat = repeat;
        this.volume = volume;
        this.liked = liked;
    }

    public static final PlayerState EMPTY = new PlayerState(false, null, null, "", "", null, 0, 0,
            System.currentTimeMillis(), false, false, "off", -1, false);

    /** Interpolated current progress. */
    public long currentProgress() {
        long p = progressMs;
        if (playing) p += System.currentTimeMillis() - sampledAt;
        if (durationMs > 0) p = Math.min(p, durationMs);
        return Math.max(0, p);
    }

    public PlayerState withPlaying(boolean v) {
        long now = System.currentTimeMillis();
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, currentProgress(), now, v, shuffle, repeat, volume, liked);
    }
    public PlayerState withProgress(long ms) {
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, ms, System.currentTimeMillis(), playing, shuffle, repeat, volume, liked);
    }
    public PlayerState withShuffle(boolean v) {
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, progressMs, sampledAt, playing, v, repeat, volume, liked);
    }
    public PlayerState withRepeat(String v) {
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, progressMs, sampledAt, playing, shuffle, v, volume, liked);
    }
    public PlayerState withVolume(int v) {
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, progressMs, sampledAt, playing, shuffle, repeat, v, liked);
    }
    public PlayerState withLiked(boolean v) {
        return new PlayerState(hasTrack, trackId, uri, title, artist, artUrl, durationMs, progressMs, sampledAt, playing, shuffle, repeat, volume, v);
    }

    public static String fmt(long ms) {
        long s = Math.max(0, ms) / 1000;
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, sec);
        return String.format("%d:%02d", m, sec);
    }
}
