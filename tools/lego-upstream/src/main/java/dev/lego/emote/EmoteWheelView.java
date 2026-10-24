package dev.lego.emote;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import dev.lego.cosmetic.Raster;
import dev.lego.ui.Ease;
import dev.lego.ui.Env;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;
import dev.lego.ui.Style;
import dev.lego.ui.Thumbs;
import dev.lego.ui.View;

/** Radial emote wheel: hold the key, point at an emote, release (or click) to play it. */
public final class EmoteWheelView extends View {
    private final int holdKey;
    private int sel = -1, lastSel = -1;
    private boolean played;

    public EmoteWheelView(int holdKey) { this.holdKey = holdKey; }

    @Override public boolean blur() { return false; }
    @Override public boolean backdrop() { return false; }
    @Override public float openMs() { return 220; }
    @Override public float closeMs() { return 140; }

    private int count() { return EmoteLib.WHEEL.size(); }

    @Override
    public void draw() {
        Style.update();
        float o = Ease.outCubic(open);
        int R = p(118), r0 = Math.round(R * 0.42f);
        int cx = W / 2, cy = H / 2;

        // background dim
        Gx.pushAlpha(o);
        Gx.Img dim = Gx.painted("wheel:dim", 256, 256, g -> {
            g.setPaint(new RadialGradientPaint(new Point2D.Float(128, 128), 181, new float[]{0f, 1f},
                    new Color[]{new Color(0, 0, 0, 150), new Color(0, 0, 0, 60)}));
            g.fillRect(0, 0, 256, 256);
        });
        if (dim != null) Gx.image(dim, 0, 0, W, H, 0xFFFFFFFF);
        Gx.popAlpha();

        int n = count();
        // selection from the mouse angle
        float dx = mx - cx, dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        sel = -1;
        if (n > 0 && dist > r0 * 0.55) {
            double ang = Math.atan2(dy, dx) + Math.PI / 2 + Math.PI / n;
            ang = (ang % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2);
            sel = Math.min(n - 1, (int) (ang / (Math.PI * 2 / Math.max(1, n))));
        }
        if (sel != lastSel && sel >= 0) Sound.click();
        lastSel = sel;

        Gx.push();
        Gx.scaleAt(cx, cy, 0.85f + 0.15f * Ease.outBack(open));
        Gx.pushAlpha(o);

        final int size = R * 2 + p(24), pad = p(12);
        final int nn = Math.max(1, n);
        final float P0 = P;
        int base = Style.surface, accent = Style.accent;
        Gx.shadow(cx - R, cy - R, R * 2, R * 2, R, p(18), 0.55f);
        Gx.Img wheel = Gx.painted("wheel:base:" + nn + ":" + size + ":" + Integer.toHexString(base) + ":" + Integer.toHexString(Style.stroke), size, size, g -> {
            double c = size / 2.0;
            Area ring = new Area(new Ellipse2D.Double(c - R, c - R, R * 2, R * 2));
            ring.subtract(new Area(new Ellipse2D.Double(c - r0, c - r0, r0 * 2, r0 * 2)));
            g.setColor(new Color(base, true));
            g.fill(ring);
            g.setColor(new Color(Style.stroke, true));
            g.setStroke(new BasicStroke(Math.max(1, P0 * 0.8f)));
            for (int i = 0; i < nn; i++) {
                double a = -Math.PI / 2 + Math.PI * 2 * (i + 0.5) / nn;
                g.drawLine((int) (c + Math.cos(a) * (r0 + 2)), (int) (c + Math.sin(a) * (r0 + 2)), (int) (c + Math.cos(a) * (R - 2)), (int) (c + Math.sin(a) * (R - 2)));
            }
            g.draw(new Ellipse2D.Double(c - R + 0.5, c - R + 0.5, R * 2 - 1, R * 2 - 1));
            g.draw(new Ellipse2D.Double(c - r0, c - r0, r0 * 2, r0 * 2));
        });
        if (wheel != null) Gx.image(wheel, cx - size / 2, cy - size / 2, size, size, 0xFFFFFFFF);

        // highlighted wedges (smoothly faded per segment)
        for (int i = 0; i < n; i++) {
            float h = Ease.to("wheel:h" + i, i == sel ? 1 : 0, 16);
            if (h < 0.01f) continue;
            final int idx = i;
            Gx.Img wedge = Gx.painted("wheel:w" + nn + ":" + idx + ":" + size, size, size, g -> {
                double c = size / 2.0;
                double start = 90 - 360.0 * (idx - 0.5) / nn;
                Area a = new Area(new Arc2D.Double(c - R - pad * 0.4, c - R - pad * 0.4, (R + pad * 0.4) * 2, (R + pad * 0.4) * 2, start, -360.0 / nn, Arc2D.PIE));
                a.subtract(new Area(new Ellipse2D.Double(c - r0, c - r0, r0 * 2, r0 * 2)));
                g.setColor(Color.WHITE);
                g.fill(a);
            });
            if (wedge != null) Gx.image(wedge, cx - size / 2, cy - size / 2, size, size, Gx.withAlpha(accent, 0.28f * h));
        }

        // items
        for (int i = 0; i < n; i++) {
            EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(i));
            if (e == null) continue;
            float h = Ease.to("wheel:h" + i, i == sel ? 1 : 0, 16);
            double a = -Math.PI / 2 + Math.PI * 2 * i / n;
            float mid = (R + r0) / 2f + pf(4) * h;
            float ix = cx + (float) Math.cos(a) * mid, iy = cy + (float) Math.sin(a) * mid;
            int ts = Math.round(pf(40) * (1 + 0.15f * h));
            final int px = Math.max(16, p(44));
            Gx.Img th = Thumbs.get("emote:w:" + e.id, px, px, () -> Raster.renderPose(e.pose(Math.min(e.duration * 0.4f, 1.1f)), px, px, 22, 8, 0x00000000));
            if (th != null) Gx.image(th, Math.round(ix - ts / 2f), Math.round(iy - ts / 2f - pf(5)), ts, ts, 0xFFFFFFFF);
            else Gx.icon(e.icon, ix, iy - pf(5), pf(16), Gx.mix(Style.sub, accent, h));
            float fs = pf(6.8f);
            Gx.textCenter(Fonts.ellipsize(e.name, Fonts.BOLD, fs, (R - r0) * 0.95f), ix, iy + pf(19), fs, Fonts.BOLD, Gx.mix(Style.sub, Style.text, h));
        }

        // centre
        Gx.circle(cx, cy, r0 - p(4), Style.bg);
        if (sel >= 0 && sel < n) {
            EmoteLib.Emote e = EmoteLib.get(EmoteLib.WHEEL.get(sel));
            if (e != null) {
                Gx.glow(cx, cy, r0, Gx.withAlpha(accent, 0.18f));
                Gx.textCenter(e.name, cx, cy - pf(4), pf(10), Fonts.BOLD, Style.text);
                Gx.textCenter(e.loop ? "Loop · bewegen stoppt" : String.format(java.util.Locale.ROOT, "%.1f s", e.duration), cx, cy + pf(10), pf(6.6f), Fonts.MEDIUM, Style.sub);
            }
        } else if (n == 0) {
            Gx.textCenter("Keine Favoriten", cx, cy - pf(4), pf(8.4f), Fonts.BOLD, Style.text);
            Gx.textCenter("Stern im Emote-Menü", cx, cy + pf(9), pf(6.4f), Fonts.REGULAR, Style.sub);
        } else {
            Gx.icon("emote", cx, cy - pf(6), pf(20), Style.sub);
            Gx.textCenter("Emote wählen", cx, cy + pf(14), pf(6.8f), Fonts.MEDIUM, Style.muted);
        }
        Gx.popAlpha();
        Gx.pop();

        // hint
        Gx.pushAlpha(o);
        String hint = "Maus zeigen · loslassen oder klicken = abspielen · Esc = abbrechen";
        float fs = pf(7.2f);
        int hw = Math.round(Gx.width(hint, fs, Fonts.MEDIUM)) + p(24), hh = p(22);
        int hx = cx - hw / 2, hy = cy + R + p(22);
        Gx.rect(hx, hy, hw, hh, hh / 2, Gx.withAlpha(Style.bg, 0.85f));
        Gx.textCenter(hint, cx, hy + hh / 2f, fs, Fonts.MEDIUM, Style.sub);
        Gx.popAlpha();
    }

    private void playSelected() {
        if (played || host == null) return;
        played = true;
        if (sel >= 0 && sel < count()) {
            int idx = EmoteLib.index(EmoteLib.WHEEL.get(sel));
            host.closeThen(() -> Env.playEmote.accept(idx));
        } else host.close();
    }

    @Override
    public boolean mouseDown(float x, float y, int button, boolean dbl) {
        if (button == 0) playSelected();
        else if (host != null) host.close();
        return true;
    }

    @Override
    public boolean key(int key, int mods) {
        if (key == -1 - holdKey) { playSelected(); return true; }   // key released
        return false;
    }
}
