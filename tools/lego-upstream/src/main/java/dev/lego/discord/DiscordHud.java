package dev.lego.discord;

import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;

/**
 * Discord card in the style of the Spotify HUD: Discord badge, status line and two round buttons (microphone,
 * headphones). Click the buttons while the chat is open. "Kompakt" shows only the two buttons.
 */
public final class DiscordHud {
    private DiscordHud() { }

    private static final int BLURPLE = 0xFF5865F2, RED = 0xFFED4245, GREEN = 0xFF3BA55D, BG = 0xD0111214, BG2 = 0xFF1E1F22;
    /** Button bounds in GUI units (for the chat click). */
    private static final double[][] BTN = new double[2][4];

    public static void render(int W, int H) {
        if (!DiscordControl.hudOn()) return;
        int S = Gx.S;
        float sc = DiscordControl.sc();
        float k = S * sc;
        boolean compact = DiscordControl.compactHud();
        boolean mic = !(DiscordControl.muted || DiscordControl.deafened), ear = !DiscordControl.deafened;
        int w = Math.round((compact ? 52 : 132) * k), h = Math.round(34 * k);
        int x = Math.round((float) (W * DiscordControl.fx())), y = Math.round((float) (H * DiscordControl.fy()));
        Gx.shadow(x, y, w, h, Math.round(10 * k), Math.round(6 * k), 0.35f);
        Gx.rect(x, y, w, h, Math.round(10 * k), BG);
        Gx.outline(x, y, w, h, Math.round(10 * k), Math.max(1, S / 2), 0x22FFFFFF);
        int bx = x + Math.round(6 * k);
        if (!compact) {
            int lr = Math.round(11 * k), lx = x + Math.round(17 * k), ly = y + h / 2;
            Gx.circle(lx, ly, lr, BLURPLE);
            // simple Discord controller face
            Gx.rect(lx - Math.round(6 * k), ly - Math.round(3 * k), Math.round(12 * k), Math.round(7 * k), Math.round(3 * k), 0xFFFFFFFF);
            Gx.circle(lx - Math.round(2.5f * k), ly, Math.max(1, Math.round(1.3f * k)), BLURPLE);
            Gx.circle(lx + Math.round(2.5f * k), ly, Math.max(1, Math.round(1.3f * k)), BLURPLE);
            Gx.text("Discord", x + Math.round(33 * k), y + Math.round(6 * k), 7.6f * k, Fonts.BOLD, 0xFFF2F3F5);
            String st = !ear ? "Taub geschaltet" : !mic ? "Stumm geschaltet" : "Sprachchat aktiv";
            Gx.text(st, x + Math.round(33 * k), y + Math.round(18 * k), 6.0f * k, Fonts.MEDIUM, !ear || !mic ? RED : GREEN);
            bx = x + w - Math.round(46 * k);
        }
        int br = Math.round(10 * k);
        boolean[] on = {mic, ear};
        String[] icon = {mic ? "sound" : "mute", ear ? "volume" : "mute"};
        for (int i = 0; i < 2; i++) {
            int cx = bx + br + i * Math.round(22 * k), cy = y + h / 2;
            float pulse = Ease.to("dc:" + i, on[i] ? 0 : 1, 10);
            Gx.circle(cx, cy, br, Gx.mix(BG2, RED, pulse * 0.85f));
            Gx.icon(icon[i], cx, cy, 9.5f * k, on[i] ? 0xFFDBDEE1 : 0xFFFFFFFF);
            if (!on[i]) Gx.rect(cx - Math.round(7 * k), cy - Math.max(1, S / 2), Math.round(14 * k), Math.max(1, S), 1, 0xFFFFFFFF);
            BTN[i][0] = (cx - br) / (double) S; BTN[i][1] = (cy - br) / (double) S; BTN[i][2] = (cx + br) / (double) S; BTN[i][3] = (cy + br) / (double) S;
        }
    }

    /** @return true when a button was hit (click consumed). Only called from the chat screen. */
    public static boolean click(double mx, double my, int button) {
        if (!DiscordControl.hudOn() || button != 0) return false;
        for (int i = 0; i < 2; i++) {
            if (mx >= BTN[i][0] && mx <= BTN[i][2] && my >= BTN[i][1] && my <= BTN[i][3]) {
                if (i == 0) DiscordControl.toggleMute(); else DiscordControl.toggleDeafen();
                return true;
            }
        }
        return false;
    }
}
