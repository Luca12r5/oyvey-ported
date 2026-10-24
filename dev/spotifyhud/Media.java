package dev.spotifyhud;

public final class Media {
   private final SmtcBridge smtc;
   private final SpotifyApi api;
   private volatile String toast = "";
   private volatile long toastAt = 0L;

   Media(SmtcBridge smtc, SpotifyApi api) {
      this.smtc = smtc;
      this.api = api;
      api.setErrorListener(this::toast);
   }

   public SmtcBridge smtc() {
      return this.smtc;
   }

   public SpotifyApi api() {
      return this.api;
   }

   public void toast(String s) {
      this.toast = s;
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

   private boolean apiMatches(PlayerState s) {
      if (!this.api.isLoggedIn()) {
         return false;
      } else {
         PlayerState a = this.api.state();
         return a.hasTrack && a.title != null && a.title.equalsIgnoreCase(s.title);
      }
   }

   public PlayerState state() {
      if (!this.smtc.active()) {
         return this.api.isLoggedIn() ? this.api.state() : PlayerState.EMPTY;
      } else {
         PlayerState s = this.smtc.state();
         if (this.apiMatches(s)) {
            PlayerState a = this.api.state();
            if (s.durationMs <= 0L && a.durationMs > 0L) {
               s = new PlayerState(
                  true,
                  a.trackId,
                  a.uri,
                  s.title,
                  s.artist,
                  a.artUrl,
                  a.durationMs,
                  a.progressMs,
                  a.sampledAt,
                  s.playing,
                  s.shuffle,
                  s.repeat,
                  s.volume,
                  a.liked
               );
            } else {
               s = s.withLiked(a.liked);
            }
         }

         return s;
      }
   }

   public Object art() {
      if (this.smtc.active()) {
         Object a = this.smtc.art();
         if (a == null && this.apiMatches(this.smtc.state())) {
            a = this.api.artImage();
         }

         return a;
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
         String app = this.smtc.app();
         if (this.smtc.isSpotify()) {
            return "Spotify (automatisch erkannt)";
         } else {
            String n = app;
            int i = app.lastIndexOf(33);
            if (i >= 0) {
               n = app.substring(i + 1);
            }

            i = n.lastIndexOf(92);
            if (i >= 0) {
               n = n.substring(i + 1);
            }

            return (n.isEmpty() ? "Windows-Medien" : n) + " (automatisch erkannt)";
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

   public void seek(long ms) {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.canSeek()) {
            this.smtc.seek(ms);
         } else if (this.api.isLoggedIn()) {
            this.api.seek(ms);
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

   public void setVolume(int v, boolean immediate) {
      if (!this.noSource()) {
         if (this.smtc.active() && this.smtc.volumeSupported()) {
            this.smtc.setVolume(v);
         } else if (this.api.isLoggedIn()) {
            this.api.setVolume(v, immediate);
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

   void onBridgeFailed(String cmd) {
      boolean api = this.api.isLoggedIn();
      if (cmd.startsWith("shuffle")) {
         if (api) {
            this.api.toggleShuffle();
         } else {
            this.toast("Zufall wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)");
         }
      } else if (cmd.startsWith("repeat")) {
         if (api) {
            this.api.cycleRepeat();
         } else {
            this.toast("Wiederholen wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)");
         }
      } else if (cmd.startsWith("seek ")) {
         if (api) {
            try {
               this.api.seek(Long.parseLong(cmd.substring(5).trim()));
            } catch (NumberFormatException var4) {
            }
         } else {
            this.toast("Spulen wurde abgelehnt");
         }
      } else if (!cmd.startsWith("vol") && !cmd.equals("mute")) {
         this.toast("Aktion wurde abgelehnt (" + cmd + ")");
      } else if (api) {
         this.toast("Lautstärke über Windows fehlgeschlagen");
      } else {
         this.toast("Lautstärke: Spotify-Audio nicht gefunden");
      }
   }
}
