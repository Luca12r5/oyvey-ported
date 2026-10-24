package dev.spotifyhud;

public final class PlayerState {
   public final boolean hasTrack;
   public final String trackId;
   public final String uri;
   public final String title;
   public final String artist;
   public final String artUrl;
   public final long durationMs;
   public final long progressMs;
   public final long sampledAt;
   public final boolean playing;
   public final boolean shuffle;
   public final String repeat;
   public final int volume;
   public final boolean liked;
   public static final PlayerState EMPTY = new PlayerState(false, null, null, "", "", null, 0L, 0L, System.currentTimeMillis(), false, false, "off", -1, false);

   public PlayerState(
      boolean hasTrack,
      String trackId,
      String uri,
      String title,
      String artist,
      String artUrl,
      long durationMs,
      long progressMs,
      long sampledAt,
      boolean playing,
      boolean shuffle,
      String repeat,
      int volume,
      boolean liked
   ) {
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

   public long currentProgress() {
      long p = this.progressMs;
      if (this.playing) {
         p += System.currentTimeMillis() - this.sampledAt;
      }

      if (this.durationMs > 0L) {
         p = Math.min(p, this.durationMs);
      }

      return Math.max(0L, p);
   }

   public PlayerState withPlaying(boolean v) {
      long now = System.currentTimeMillis();
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.currentProgress(),
         now,
         v,
         this.shuffle,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withProgress(long ms) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         ms,
         System.currentTimeMillis(),
         this.playing,
         this.shuffle,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withShuffle(boolean v) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.progressMs,
         this.sampledAt,
         this.playing,
         v,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withRepeat(String v) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.progressMs,
         this.sampledAt,
         this.playing,
         this.shuffle,
         v,
         this.volume,
         this.liked
      );
   }

   public PlayerState withVolume(int v) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.progressMs,
         this.sampledAt,
         this.playing,
         this.shuffle,
         this.repeat,
         v,
         this.liked
      );
   }

   public PlayerState withLiked(boolean v) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.progressMs,
         this.sampledAt,
         this.playing,
         this.shuffle,
         this.repeat,
         this.volume,
         v
      );
   }

   public static String fmt(long ms) {
      long s = Math.max(0L, ms) / 1000L;
      long h = s / 3600L;
      long m = s % 3600L / 60L;
      long sec = s % 60L;
      return h > 0L ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%d:%02d", m, sec);
   }
}
