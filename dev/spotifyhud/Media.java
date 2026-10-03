package dev.spotifyhud;

public final class Media {
   private final SmtcBridge smtc;
   private final SpotifyApi api;
   private volatile String toast = "";
   private volatile long toastAt = 0L;

   Media(SmtcBridge var1, SpotifyApi var2) {
      this.smtc = var1;
      this.api = var2;
      var2.setErrorListener(this::toast);
   }

   public SmtcBridge smtc() {
      return this.smtc;
   }

   public SpotifyApi api() {
      return this.api;
   }

   public void toast(String var1) {
      this.toast = var1;
      this.toastAt = System.currentTimeMillis();
   }

   public String toast() {
      return this.toast;
   }

   public long toastAt() {
      return this.toastAt;
   }

   public boolean usingSmtc() {
      return this.smtc.active();
   }

   void tick() {
      this.api.setLightMode(this.smtc.active());
   }

   private boolean apiMatches(PlayerState var1) {
      if (!this.api.isLoggedIn()) {
         return false;
      } else {
         PlayerState var2 = this.api.state();
         return var2.hasTrack && var2.title != null && var2.title.equalsIgnoreCase(var1.title);
      }
   }

   public PlayerState state() {
      if (!this.smtc.active()) {
         return this.api.isLoggedIn() ? this.api.state() : PlayerState.EMPTY;
      } else {
         PlayerState var1 = this.smtc.state();
         if (this.apiMatches(var1)) {
            PlayerState var2 = this.api.state();
            if (var1.durationMs <= 0L && var2.durationMs > 0L) {
               var1 = new PlayerState(
                  true,
                  var2.trackId,
                  var2.uri,
                  var1.title,
                  var1.artist,
                  var2.artUrl,
                  var2.durationMs,
                  var2.progressMs,
                  var2.sampledAt,
                  var1.playing,
                  var1.shuffle,
                  var1.repeat,
                  var1.volume,
                  var2.liked
               );
            } else {
               var1 = var1.withLiked(var2.liked);
            }
         }

         return var1;
      }
   }

   public Object art() {
      if (this.smtc.active()) {
         Object var1 = this.smtc.art();
         if (var1 == null && this.apiMatches(this.smtc.state())) {
            var1 = this.api.artImage();
         }

         return var1;
      } else {
         return this.api.isLoggedIn() ? this.api.artImage() : null;
      }
   }

   public int artVersion() {
      return this.smtc.artVersion() * 31 + this.api.artVersion();
   }

   public long latencyMs() {
      return this.smtc.active() ? this.smtc.latencyMs() : this.api.latencyMs();
   }

   public String sourceName() {
      if (this.smtc.active()) {
         String var1 = this.smtc.app();
         if (this.smtc.isSpotify()) {
            return "Spotify (automatisch erkannt)";
         } else {
            String var2 = var1;
            int var3 = var1.lastIndexOf(33);
            if (var3 >= 0) {
               var2 = var1.substring(var3 + 1);
            }

            var3 = var2.lastIndexOf(92);
            if (var3 >= 0) {
               var2 = var2.substring(var3 + 1);
            }

            return (var2.isEmpty() ? "Windows-Medien" : var2) + " (automatisch erkannt)";
         }
      } else {
         return this.api.isLoggedIn() && this.api.state().hasTrack ? "Spotify-Konto" : null;
      }
   }

   private boolean noSource() {
      if (!this.smtc.active() && !this.api.isLoggedIn()) {
         this.toast("Kein Song erkannt - starte Spotify");
         return true;
      } else {
         return false;
      }
   }

   public void togglePlay() {
      if (!this.noSource()) {
         if (this.smtc.active()) {
            this.smtc.togglePlay();
         } else {
            this.api.togglePlay();
         }
      }
   }

   public void next() {
      if (!this.noSource()) {
         if (this.smtc.active()) {
            this.smtc.next();
         } else {
            this.api.next();
         }
      }
   }

   public void previous() {
      if (!this.noSource()) {
         if (this.smtc.active()) {
            this.smtc.previous();
         } else {
            this.api.previous();
         }
      }
   }

   public void seek(long var1) {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.canSeek()) {
            this.smtc.seek(var1);
         } else if (this.api.isLoggedIn()) {
            this.api.seek(var1);
         } else {
            this.toast("Spulen wird von dieser App nicht unterstützt");
         }
      }
   }

   public void toggleShuffle() {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.canShuffle()) {
            this.smtc.toggleShuffle();
         } else if (this.api.isLoggedIn()) {
            this.api.toggleShuffle();
         } else {
            this.toast("Zufall geht über Windows nicht - Spotify-Konto verbinden (H)");
         }
      }
   }

   public void cycleRepeat() {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.canRepeat()) {
            this.smtc.cycleRepeat();
         } else if (this.api.isLoggedIn()) {
            this.api.cycleRepeat();
         } else {
            this.toast("Wiederholen geht über Windows nicht - Spotify-Konto verbinden (H)");
         }
      }
   }

   public void setVolume(int var1, boolean var2) {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.volumeSupported()) {
            this.smtc.setVolume(var1);
         } else if (this.api.isLoggedIn()) {
            this.api.setVolume(var1, var2);
         } else {
            this.toast("Lautstärke nicht verfügbar");
         }
      }
   }

   public void toggleMute() {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.volumeSupported()) {
            this.smtc.toggleMute();
         } else if (this.api.isLoggedIn()) {
            this.api.toggleMute();
         }
      }
   }

   public void toggleLike() {
      if (!this.api.isLoggedIn()) {
         this.toast("Für das Herz: H drücken und Spotify-Konto verbinden");
      } else if (this.smtc.active() && !this.apiMatches(this.smtc.state())) {
         this.toast("Song wird noch mit Spotify abgeglichen - gleich nochmal");
      } else {
         this.api.toggleLike();
      }
   }

   void onBridgeFailed(String var1) {
      boolean var2 = this.api.isLoggedIn();
      if (var1.startsWith("shuffle")) {
         if (var2) {
            this.api.toggleShuffle();
         } else {
            this.toast("Zufall wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)");
         }
      } else if (var1.startsWith("repeat")) {
         if (var2) {
            this.api.cycleRepeat();
         } else {
            this.toast("Wiederholen wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)");
         }
      } else if (var1.startsWith("seek ")) {
         if (var2) {
            try {
               this.api.seek(Long.parseLong(var1.substring(5).trim()));
            } catch (NumberFormatException var4) {
            }
         } else {
            this.toast("Spulen wurde abgelehnt");
         }
      } else if (!var1.startsWith("vol") && !var1.equals("mute")) {
         this.toast("Aktion wurde abgelehnt (" + var1 + ")");
      } else if (var2) {
         this.toast("Lautstärke über Windows fehlgeschlagen");
      } else {
         this.toast("Lautstärke: Spotify-Audio nicht gefunden");
      }
   }
}
