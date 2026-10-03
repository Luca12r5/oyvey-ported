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
      boolean var1,
      String var2,
      String var3,
      String var4,
      String var5,
      String var6,
      long var7,
      long var9,
      long var11,
      boolean var13,
      boolean var14,
      String var15,
      int var16,
      boolean var17
   ) {
      this.hasTrack = var1;
      this.trackId = var2;
      this.uri = var3;
      this.title = var4;
      this.artist = var5;
      this.artUrl = var6;
      this.durationMs = var7;
      this.progressMs = var9;
      this.sampledAt = var11;
      this.playing = var13;
      this.shuffle = var14;
      this.repeat = var15;
      this.volume = var16;
      this.liked = var17;
   }

   public long currentProgress() {
      long var1 = this.progressMs;
      if (this.playing) {
         var1 += System.currentTimeMillis() - this.sampledAt;
      }

      if (this.durationMs > 0L) {
         var1 = Math.min(var1, this.durationMs);
      }

      return Math.max(0L, var1);
   }

   public PlayerState withPlaying(boolean var1) {
      long var2 = System.currentTimeMillis();
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         this.currentProgress(),
         var2,
         var1,
         this.shuffle,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withProgress(long var1) {
      return new PlayerState(
         this.hasTrack,
         this.trackId,
         this.uri,
         this.title,
         this.artist,
         this.artUrl,
         this.durationMs,
         var1,
         System.currentTimeMillis(),
         this.playing,
         this.shuffle,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withShuffle(boolean var1) {
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
         var1,
         this.repeat,
         this.volume,
         this.liked
      );
   }

   public PlayerState withRepeat(String var1) {
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
         var1,
         this.volume,
         this.liked
      );
   }

   public PlayerState withVolume(int var1) {
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
         var1,
         this.liked
      );
   }

   public PlayerState withLiked(boolean var1) {
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
         var1
      );
   }

   public static String fmt(long var0) {
      long var2 = Math.max(0L, var0) / 1000L;
      long var4 = var2 / 3600L;
      long var6 = var2 % 3600L / 60L;
      long var8 = var2 % 60L;
      return var4 > 0L ? String.format("%d:%02d:%02d", var4, var6, var8) : String.format("%d:%02d", var6, var8);
   }
}
