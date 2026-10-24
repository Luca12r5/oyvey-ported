package dev.lego.emote;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Speech-bubble textures above the head for some emotes (Java2D, 256 px). */
public final class Bubbles {
    private Bubbles() { }

    public static int[] paint(String id, int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        dev.lego.ui.Gx.hints(g);
        g.scale(s / 64.0, s / 64.0);
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new RoundRectangle2D.Double(5, 6, 56, 46, 24, 24));
        g.setColor(new Color(255, 255, 255, 245));
        g.fill(new RoundRectangle2D.Double(4, 4, 56, 46, 24, 24));
        Path2D tail = new Path2D.Double();
        tail.moveTo(26, 48); tail.lineTo(32, 60); tail.lineTo(38, 48); tail.closePath();
        g.fill(tail);
        switch (id) {
            case "heart": {
                g.setPaint(new GradientPaint(20, 12, new Color(255, 110, 140), 44, 42, new Color(220, 20, 60)));
                Path2D h = new Path2D.Double();
                h.moveTo(32, 43); h.curveTo(12, 30, 15, 11, 26, 12); h.curveTo(30, 12.5, 32, 16, 32, 18);
                h.curveTo(32, 16, 34, 12.5, 38, 12); h.curveTo(49, 11, 52, 30, 32, 43);
                g.fill(h);
                g.setColor(new Color(255, 255, 255, 150));
                g.fill(new Ellipse2D.Double(21, 16, 7, 5));
                break;
            }
            case "gg": text(g, "GG", new Color(30, 215, 96), 24f); break;
            case "laugh": face(g, true, false); break;
            case "cry": face(g, false, true); break;
            case "think": text(g, "?", new Color(0x3FA9FF), 30f); break;
            case "zen": {
                g.setColor(new Color(0x9B4DCA));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Double(20, 13, 24, 24));
                g.fill(new Arc2D.Double(20, 13, 24, 24, 90, 180, Arc2D.CHORD));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(29, 16, 6, 6));
                g.setColor(new Color(0x9B4DCA));
                g.fill(new Ellipse2D.Double(29, 28, 6, 6));
                break;
            }
            case "sleep": text(g, "Zzz", new Color(0x5B6BFF), 18f); break;
            default: { // fire
                g.setPaint(new GradientPaint(32, 10, new Color(255, 220, 60), 32, 44, new Color(255, 60, 20)));
                Path2D f = new Path2D.Double();
                f.moveTo(32, 44); f.curveTo(18, 44, 16, 30, 24, 22); f.curveTo(24, 28, 28, 30, 30, 28);
                f.curveTo(26, 20, 30, 12, 36, 8); f.curveTo(36, 16, 48, 22, 46, 34); f.curveTo(45, 41, 40, 44, 32, 44);
                g.fill(f);
            }
        }
        g.dispose();
        return img.getRGB(0, 0, s, s, null, 0, s);
    }

    private static void text(Graphics2D g, String s, Color c, float size) {
        g.setColor(c);
        g.setFont(dev.lego.ui.Fonts.get(dev.lego.ui.Fonts.BOLD, size));
        double w = g.getFontMetrics().getStringBounds(s, g).getWidth();
        g.drawString(s, (float) (32 - w / 2), 27 + size * 0.36f);
    }

    private static void face(Graphics2D g, boolean laugh, boolean cry) {
        g.setPaint(new RadialGradientPaint(28, 20, 22, new float[]{0f, 1f}, new Color[]{new Color(255, 225, 90), new Color(245, 180, 0)}));
        g.fill(new Ellipse2D.Double(16, 10, 32, 32));
        g.setColor(new Color(60, 40, 10));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (laugh) {
            g.draw(new Arc2D.Double(21, 18, 8, 6, 0, 180, Arc2D.OPEN));
            g.draw(new Arc2D.Double(35, 18, 8, 6, 0, 180, Arc2D.OPEN));
            g.fill(new Arc2D.Double(21, 21, 22, 16, 180, 180, Arc2D.CHORD));
        } else {
            g.fill(new Ellipse2D.Double(23, 20, 4, 5));
            g.fill(new Ellipse2D.Double(37, 20, 4, 5));
            g.draw(new Arc2D.Double(24, 30, 16, 10, 20, 140, Arc2D.OPEN));
        }
        if (laugh || cry) {
            g.setColor(new Color(90, 180, 255));
            g.fill(new Ellipse2D.Double(cry ? 22 : 14, cry ? 26 : 23, 5, 9));
            g.fill(new Ellipse2D.Double(cry ? 37 : 45, cry ? 26 : 23, 5, 9));
        }
    }
}
