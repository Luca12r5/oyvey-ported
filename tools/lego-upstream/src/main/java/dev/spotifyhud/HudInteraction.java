package dev.spotifyhud;

import net.minecraft.class_332;

/**
 * Mouse handling for the HUD, shared by the chat screen and the settings screen:
 * left click = buttons / seek / volume / drag to move, mouse wheel = resize (on the volume bar: volume),
 * right click = lyrics on/off.
 */
final class HudInteraction {
    private enum Drag { NONE, PENDING_MOVE, MOVE, SEEK, VOLUME }

    private final Config cfg;
    private final HudRenderer hud;

    private Drag drag = Drag.NONE;
    private double offX, offY, downX, downY;
    private double previewProgress = -1;
    private int previewVolume = -1;

    HudInteraction(Config cfg, HudRenderer hud) {
        this.cfg = cfg;
        this.hud = hud;
    }

    boolean dragging() { return drag != Drag.NONE; }

    private int[] hb(int sw, int sh) { return hud.bounds(sw, sh, cfg); }

    private int[] lb(int[] hb, int sw, int sh) {
        return SpotifyHudMod.lyricsVisible() ? hud.lyricsBounds(hb, sw, sh, cfg) : null;
    }

    private static boolean inside(double mx, double my, int[] b) {
        return b != null && mx >= b[0] && my >= b[1] && mx < b[0] + b[2] && my < b[1] + b[3];
    }

    static HudLayout.Region regionAt(double mx, double my, int[] b) {
        if (!inside(mx, my, b)) return null;
        double dx = (mx - b[0]) * HudLayout.W / (double) b[2];
        double dy = (my - b[1]) * HudLayout.H / (double) b[3];
        return HudLayout.hit(dx, dy);
    }

    private static double designX(double mx, int[] b) {
        return (mx - b[0]) * HudLayout.W / (double) b[2];
    }

    boolean isOver(double mx, double my, int sw, int sh) {
        int[] h = hb(sw, sh);
        return inside(mx, my, h) || inside(mx, my, lb(h, sw, sh));
    }

    /** @return true if the click was consumed by the HUD */
    boolean mouseDown(double mx, double my, int button, int sw, int sh) {
        if (!cfg.visible) return false;
        int[] h = hb(sw, sh);
        int[] l = lb(h, sw, sh);
        boolean onLyrics = inside(mx, my, l);
        HudLayout.Region r = regionAt(mx, my, h);
        if (r == null && !onLyrics) return false;
        if (button == 1) {                       // right click: lyrics on/off
            SpotifyHudMod.toggleLyrics();
            return true;
        }
        if (button != 0) return true;
        Media media = SpotifyHudMod.media();
        if (onLyrics) r = HudLayout.Region.PANEL;
        switch (r) {
            case PREV: media.previous(); break;
            case PLAY: media.togglePlay(); break;
            case NEXT: media.next(); break;
            case REPEAT: media.cycleRepeat(); break;
            case SHUFFLE: media.toggleShuffle(); break;
            case LIKE: media.toggleLike(); break;
            case SETTINGS: SpotifyHudMod.requestSettings(); break;
            case SPEAKER: media.toggleMute(); break;
            case PROGRESS: {
                PlayerState st = media.state();
                if (st.hasTrack && st.durationMs > 0) {
                    drag = Drag.SEEK;
                    previewProgress = HudLayout.progressFraction(designX(mx, h));
                } else startMove(mx, my, h);
                break;
            }
            case VOLUME:
                drag = Drag.VOLUME;
                previewVolume = (int) Math.round(HudLayout.volumeFraction(designX(mx, h)) * 100);
                break;
            default:
                startMove(mx, my, h);
        }
        return true;
    }

    private void startMove(double mx, double my, int[] h) {
        drag = Drag.PENDING_MOVE;
        offX = mx - h[0];
        offY = my - h[1];
        downX = mx;
        downY = my;
    }

    boolean mouseDrag(double mx, double my, int sw, int sh) {
        if (drag == Drag.NONE) return false;
        int[] h = hb(sw, sh);
        switch (drag) {
            case SEEK: previewProgress = HudLayout.progressFraction(designX(mx, h)); break;
            case VOLUME: previewVolume = (int) Math.round(HudLayout.volumeFraction(designX(mx, h)) * 100); break;
            case PENDING_MOVE:
                if (Math.abs(mx - downX) + Math.abs(my - downY) < 2) break;
                drag = Drag.MOVE;
                // fall through
            case MOVE: {
                int freeX = sw - h[2], freeY = sh - h[3];
                cfg.posX = freeX > 0 ? Config.clamp01((mx - offX) / freeX) : 0.5;
                cfg.posY = freeY > 0 ? Config.clamp01((my - offY) / freeY) : 0;
                break;
            }
            default: break;
        }
        return true;
    }

    boolean mouseUp() {
        if (drag == Drag.NONE) return false;
        Media media = SpotifyHudMod.media();
        switch (drag) {
            case SEEK:
                if (previewProgress >= 0) media.seek((long) (previewProgress * media.state().durationMs));
                break;
            case VOLUME:
                if (previewVolume >= 0) media.setVolume(previewVolume, true);
                break;
            case MOVE:
                cfg.save();
                break;
            default: break;
        }
        drag = Drag.NONE;
        previewProgress = -1;
        previewVolume = -1;
        return true;
    }

    /** Mouse wheel over the HUD: resize (over the volume bar: change volume). */
    boolean scroll(double mx, double my, double vertical, int sw, int sh) {
        if (!cfg.visible || vertical == 0 || !isOver(mx, my, sw, sh)) return false;
        int[] h = hb(sw, sh);
        HudLayout.Region r = regionAt(mx, my, h);
        if (r == HudLayout.Region.VOLUME) {
            Media media = SpotifyHudMod.media();
            int cur = media.state().volume;
            if (cur >= 0) media.setVolume(cur + (vertical > 0 ? 5 : -5), false);
            return true;
        }
        // keep the point under the cursor roughly in place while resizing
        double relX = (mx - h[0]) / (double) h[2], relY = (my - h[1]) / (double) h[3];
        double ns = Math.round(Math.max(0.5, Math.min(3.0, cfg.scale + (vertical > 0 ? 0.05 : -0.05))) * 100.0) / 100.0;
        if (ns == cfg.scale) return true;
        cfg.scale = ns;
        int[] n = hb(sw, sh);
        double newX = mx - relX * n[2], newY = my - relY * n[3];
        int freeX = sw - n[2], freeY = sh - n[3];
        cfg.posX = freeX > 0 ? Config.clamp01(newX / freeX) : 0.5;
        cfg.posY = freeY > 0 ? Config.clamp01(newY / freeY) : 0;
        SpotifyHudMod.scheduleSave();
        return true;
    }

    /** Draws HUD (+ lyrics + toast) in interactive mode. */
    void render(class_332 ctx, int mx, int my, int sw, int sh) {
        if (!cfg.visible) return;
        int[] h = hb(sw, sh);
        int[] l = lb(h, sw, sh);
        HudLayout.Region hover = null;
        boolean lyricsHover = false;
        if (drag == Drag.NONE || drag == Drag.PENDING_MOVE) {
            hover = regionAt(mx, my, h);
            lyricsHover = inside(mx, my, l);
        } else if (drag == Drag.MOVE) {
            hover = HudLayout.Region.PANEL;
            lyricsHover = true;
        }
        SpotifyHudMod.drawAll(ctx, sw, sh, true, hover, lyricsHover, previewProgress, previewVolume);
    }
}
