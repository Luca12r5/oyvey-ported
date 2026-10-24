package dev.spotifyhud;

/**
 * Combines both sources: automatic Windows media detection (primary, no login) and the optional Spotify account
 * (Web API; needed for the heart and used as fallback for actions Windows can't do).
 */
public final class Media {
    private final SmtcBridge smtc;
    private final SpotifyApi api;
    private volatile String toast = "";
    private volatile long toastAt = 0;

    Media(SmtcBridge smtc, SpotifyApi api) {
        this.smtc = smtc;
        this.api = api;
        api.setErrorListener(this::toast);
    }

    public SmtcBridge smtc() { return smtc; }
    public SpotifyApi api() { return api; }

    public void toast(String s) { toast = s; toastAt = System.currentTimeMillis(); }
    public String toast() { return toast; }
    public long toastAt() { return toastAt; }

    public boolean usingSmtc() { return smtc.active(); }

    /** Called once per client tick. */
    void tick() {
        api.setLightMode(smtc.active());
    }

    private boolean apiMatches(PlayerState s) {
        if (!api.isLoggedIn()) return false;
        PlayerState a = api.state();
        return a.hasTrack && a.title != null && a.title.equalsIgnoreCase(s.title);
    }

    public PlayerState state() {
        if (smtc.active()) {
            PlayerState s = smtc.state();
            if (apiMatches(s)) {
                PlayerState a = api.state();
                if (s.durationMs <= 0 && a.durationMs > 0) {
                    // Windows gave no timeline -> use Spotify's (latency compensated) timeline
                    s = new PlayerState(true, a.trackId, a.uri, s.title, s.artist, a.artUrl, a.durationMs, a.progressMs,
                            a.sampledAt, s.playing, s.shuffle, s.repeat, s.volume, a.liked);
                } else {
                    s = s.withLiked(a.liked);
                }
            }
            return s;
        }
        return api.isLoggedIn() ? api.state() : PlayerState.EMPTY;
    }

    public Object art() {
        if (smtc.active()) {
            Object a = smtc.art();
            if (a == null && apiMatches(smtc.state())) a = api.artImage();
            return a;
        }
        return api.isLoggedIn() ? api.artImage() : null;
    }

    public int artVersion() { return smtc.artVersion() * 31 + api.artVersion(); }

    /** Measured delay of the active source (bridge delay or half the API round trip). */
    public long latencyMs() { return smtc.active() ? smtc.latencyMs() : api.latencyMs(); }

    public String sourceName() {
        if (smtc.active()) {
            String app = smtc.app();
            if (smtc.isSpotify()) return "Spotify (automatisch erkannt)";
            String n = app;
            int i = n.lastIndexOf('!');
            if (i >= 0) n = n.substring(i + 1);
            i = n.lastIndexOf('\\');
            if (i >= 0) n = n.substring(i + 1);
            return (n.isEmpty() ? "Windows-Medien" : n) + " (automatisch erkannt)";
        }
        if (api.isLoggedIn() && api.state().hasTrack) return "Spotify-Konto";
        return null;
    }

    // ------------------------------------------------------------------ actions
    private boolean noSource() {
        if (smtc.active() || api.isLoggedIn()) return false;
        toast("Kein Song erkannt - starte Spotify");
        return true;
    }

    public void togglePlay() {
        if (noSource()) return;
        if (smtc.active()) smtc.togglePlay(); else api.togglePlay();
    }

    public void next() {
        if (noSource()) return;
        if (smtc.active()) smtc.next(); else api.next();
    }

    public void previous() {
        if (noSource()) return;
        if (smtc.active()) smtc.previous(); else api.previous();
    }

    public void seek(long ms) {
        if (noSource()) return;
        if (smtc.active() && smtc.canSeek()) smtc.seek(ms);
        else if (api.isLoggedIn()) api.seek(ms);
        else toast("Spulen wird von dieser App nicht unterstützt");
    }

    public void toggleShuffle() {
        if (noSource()) return;
        if (smtc.active() && smtc.canShuffle()) smtc.toggleShuffle();
        else if (api.isLoggedIn()) api.toggleShuffle();
        else toast("Zufall geht über Windows nicht - Spotify-Konto verbinden (H)");
    }

    public void cycleRepeat() {
        if (noSource()) return;
        if (smtc.active() && smtc.canRepeat()) smtc.cycleRepeat();
        else if (api.isLoggedIn()) api.cycleRepeat();
        else toast("Wiederholen geht über Windows nicht - Spotify-Konto verbinden (H)");
    }

    public void setVolume(int v, boolean immediate) {
        if (noSource()) return;
        if (smtc.active() && smtc.volumeSupported()) smtc.setVolume(v);
        else if (api.isLoggedIn()) api.setVolume(v, immediate);
        else toast("Lautstärke nicht verfügbar");
    }

    public void toggleMute() {
        if (noSource()) return;
        if (smtc.active() && smtc.volumeSupported()) smtc.toggleMute();
        else if (api.isLoggedIn()) api.toggleMute();
    }

    public void toggleLike() {
        if (!api.isLoggedIn()) {
            toast("Für das Herz: H drücken und Spotify-Konto verbinden");
            return;
        }
        if (smtc.active() && !apiMatches(smtc.state())) {
            toast("Song wird noch mit Spotify abgeglichen - gleich nochmal");
            return;
        }
        api.toggleLike();
    }

    /** The Windows bridge rejected a command: fall back to the Spotify account if possible. */
    void onBridgeFailed(String cmd) {
        boolean api = this.api.isLoggedIn();
        if (cmd.startsWith("shuffle")) { if (api) this.api.toggleShuffle(); else toast("Zufall wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)"); }
        else if (cmd.startsWith("repeat")) { if (api) this.api.cycleRepeat(); else toast("Wiederholen wird über Windows nicht unterstützt - Spotify-Konto verbinden (H)"); }
        else if (cmd.startsWith("seek ")) {
            if (api) { try { this.api.seek(Long.parseLong(cmd.substring(5).trim())); } catch (NumberFormatException ignored) { } }
            else toast("Spulen wurde abgelehnt");
        }
        else if (cmd.startsWith("vol") || cmd.equals("mute")) { if (api) toast("Lautstärke über Windows fehlgeschlagen"); else toast("Lautstärke: Spotify-Audio nicht gefunden"); }
        else toast("Aktion wurde abgelehnt (" + cmd + ")");
    }
}
