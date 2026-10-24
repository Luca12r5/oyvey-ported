package dev.lego.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** HD sprites for the ambient visuals (pure Java2D, 128 px). */
public final class AmbientTex {
    private AmbientTex() { }

    public static final int S = 128;

    public static int[] paint(String id) {
        BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        dev.lego.ui.Gx.hints(g);
        switch (id) {
            case "snow": snow(g); break;
            case "petal": petal(g); break;
            case "leaf": leaf(g); break;
            case "ash": soft(g, new Color(90, 90, 96, 230), 0.55f); break;
            case "glitter": star(g, new Color(255, 255, 255), 0.95f); break;
            case "stardust": star(g, new Color(190, 220, 255), 0.7f); break;
            case "glow": soft(g, Color.WHITE, 1f); break;
            case "core": core(g); break;
            case "lantern": lantern(g); break;
            case "bubble": bubble(g); break;
            case "butterfly0": butterfly(g, 1f); break;
            case "butterfly1": butterfly(g, 0.35f); break;
            case "bird0": bird(g, 1f); break;
            case "bird1": bird(g, -0.6f); break;
            case "bat0": bat(g, 1f); break;
            case "bat1": bat(g, 0.3f); break;
            case "dragonfly": dragonfly(g); break;
            case "seed": seed(g); break;
            case "note": note(g); break;
            case "heart": heart(g); break;
            case "shard": shard(g); break;
            case "confetti": g.setColor(Color.WHITE); g.fillRoundRect(40, 24, 48, 80, 10, 10); break;
            case "leafswirl": leaf(g); break;
            default: soft(g, Color.WHITE, 1f);
        }
        g.dispose();
        int[] px = img.getRGB(0, 0, S, S, null, 0, S);
        // bleed colour into transparent pixels (no dark fringes with linear filtering)
        for (int i = 0; i < px.length; i++) if ((px[i] >>> 24) == 0) px[i] = 0x00FFFFFF & avgNeighbour(px, i);
        return px;
    }

    private static int avgNeighbour(int[] px, int i) {
        int x = i % S, y = i / S;
        for (int r = 1; r < 4; r++)
            for (int dy = -r; dy <= r; dy++)
                for (int dx = -r; dx <= r; dx++) {
                    int xx = x + dx, yy = y + dy;
                    if (xx < 0 || yy < 0 || xx >= S || yy >= S) continue;
                    int c = px[yy * S + xx];
                    if ((c >>> 24) > 0) return c;
                }
        return 0xFFFFFF;
    }

    private static void soft(Graphics2D g, Color c, float strength) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(64, 64), 62, new float[]{0f, 0.35f, 1f},
                new Color[]{new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * strength)),
                        new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * strength * 0.45f)),
                        new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}, MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, S, S);
    }

    private static void core(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(64, 64), 62, new float[]{0f, 0.12f, 0.3f, 1f},
                new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 230), new Color(255, 255, 255, 70), new Color(255, 255, 255, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fillRect(0, 0, S, S);
    }

    private static void snow(Graphics2D g) {
        soft(g, new Color(210, 230, 255, 120), 0.8f);
        g.translate(64, 64);
        g.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 6; i++) {
            AffineTransform at = g.getTransform();
            g.rotate(Math.PI / 3 * i);
            g.setColor(new Color(255, 255, 255, 250));
            g.drawLine(0, 0, 0, -46);
            g.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(0, -18, -11, -29); g.drawLine(0, -18, 11, -29);
            g.drawLine(0, -32, -8, -40); g.drawLine(0, -32, 8, -40);
            g.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setTransform(at);
        }
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(-7, -7, 14, 14));
    }

    private static void petal(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(64, 112);
        p.curveTo(18, 88, 22, 30, 52, 16);
        p.quadTo(64, 30, 76, 16);
        p.curveTo(106, 30, 110, 88, 64, 112);
        p.closePath();
        g.setPaint(new GradientPaint(64, 110, new Color(255, 150, 190), 64, 16, new Color(255, 225, 236)));
        g.fill(p);
        g.setColor(new Color(255, 120, 170, 120));
        g.setStroke(new BasicStroke(2.2f));
        g.draw(new java.awt.geom.QuadCurve2D.Double(64, 104, 60, 70, 64, 34));
        g.setColor(new Color(255, 255, 255, 90));
        g.fill(new Ellipse2D.Double(46, 36, 16, 30));
    }

    private static void leaf(Graphics2D g) {
        Path2D p = new Path2D.Double();
        p.moveTo(64, 10);
        p.curveTo(104, 34, 110, 80, 64, 116);
        p.curveTo(18, 80, 24, 34, 64, 10);
        p.closePath();
        g.setPaint(new GradientPaint(30, 20, new Color(255, 196, 64), 100, 110, new Color(214, 64, 28)));
        g.fill(p);
        g.setColor(new Color(120, 40, 10, 170));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(64, 18, 64, 122);
        for (int i = 0; i < 4; i++) {
            int y = 36 + i * 18;
            g.drawLine(64, y + 8, 44 + i * 2, y - 4);
            g.drawLine(64, y + 8, 84 - i * 2, y - 4);
        }
    }

    private static void star(Graphics2D g, Color c, float glow) {
        soft(g, new Color(c.getRed(), c.getGreen(), c.getBlue(), 160), glow);
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double a = Math.PI / 4 * i - Math.PI / 2, r = i % 2 == 0 ? 60 : 9;
            double x = 64 + Math.cos(a) * r, y = 64 + Math.sin(a) * r;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        g.setColor(new Color(255, 255, 255, 250));
        g.fill(p);
    }

    private static void lantern(Graphics2D g) {
        soft(g, new Color(255, 150, 60, 200), 0.9f);
        RoundRectangle2D body = new RoundRectangle2D.Double(38, 26, 52, 70, 18, 18);
        g.setPaint(new GradientPaint(64, 26, new Color(255, 214, 140), 64, 96, new Color(255, 120, 40)));
        g.fill(body);
        g.setColor(new Color(255, 250, 220, 230));
        g.fill(new Ellipse2D.Double(52, 54, 24, 34));
        g.setColor(new Color(170, 60, 20, 170));
        g.setStroke(new BasicStroke(2.4f));
        for (int i = 1; i < 4; i++) g.drawLine(38 + i * 13, 30, 38 + i * 13, 92);
        g.setColor(new Color(120, 60, 30));
        g.fillRect(44, 94, 40, 5);
    }

    private static void bubble(Graphics2D g) {
        g.setPaint(new RadialGradientPaint(new Point2D.Float(64, 64), 58, new float[]{0f, 0.78f, 0.92f, 1f},
                new Color[]{new Color(255, 255, 255, 12), new Color(200, 230, 255, 40), new Color(255, 190, 255, 170), new Color(160, 255, 230, 0)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Ellipse2D.Double(6, 6, 116, 116));
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Double(34, 28, 22, 14));
        g.setColor(new Color(255, 255, 255, 120));
        g.fill(new Ellipse2D.Double(80, 84, 10, 7));
    }

    private static void bird(Graphics2D g, float up) {
        g.setColor(new Color(40, 44, 56));
        g.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        p.moveTo(10, 64 - 30 * up); p.quadTo(38, 64 - 34 * up, 64, 66); p.quadTo(90, 64 - 34 * up, 118, 64 - 30 * up);
        g.draw(p);
        g.fill(new Ellipse2D.Double(56, 58, 16, 14));
    }

    private static void bat(Graphics2D g, float open) {
        g.translate(64, 64);
        g.setColor(new Color(28, 20, 36));
        for (int s = -1; s <= 1; s += 2) {
            AffineTransform at = g.getTransform();
            g.scale(s, open);
            Path2D w = new Path2D.Double();
            w.moveTo(4, -6); w.lineTo(30, -30); w.lineTo(58, -18); w.lineTo(50, 4); w.lineTo(40, -2); w.lineTo(32, 10); w.lineTo(22, 2); w.lineTo(12, 12); w.closePath();
            g.fill(w);
            g.setTransform(at);
        }
        g.fill(new Ellipse2D.Double(-9, -12, 18, 26));
        g.setColor(new Color(255, 70, 70));
        g.fill(new Ellipse2D.Double(-5, -6, 3, 3)); g.fill(new Ellipse2D.Double(2, -6, 3, 3));
    }

    private static void dragonfly(Graphics2D g) {
        g.setColor(new Color(170, 230, 255, 150));
        g.fill(new Ellipse2D.Double(12, 40, 50, 16)); g.fill(new Ellipse2D.Double(66, 40, 50, 16));
        g.fill(new Ellipse2D.Double(16, 60, 46, 14)); g.fill(new Ellipse2D.Double(66, 60, 46, 14));
        g.setPaint(new GradientPaint(64, 30, new Color(40, 220, 190), 64, 120, new Color(30, 90, 200)));
        g.fill(new RoundRectangle2D.Double(58, 30, 12, 90, 12, 12));
        g.fill(new Ellipse2D.Double(54, 22, 20, 18));
    }

    private static void seed(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 230));
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 14; i++) {
            double a = Math.PI * 2 * i / 14;
            g.drawLine(64, 52, (int) (64 + Math.cos(a) * 40), (int) (52 + Math.sin(a) * 40));
        }
        g.setColor(new Color(230, 230, 220));
        g.drawLine(64, 52, 64, 118);
        g.setColor(new Color(150, 120, 80));
        g.fill(new Ellipse2D.Double(59, 110, 10, 14));
    }

    private static void note(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(24, 82, 34, 26));
        g.fill(new Ellipse2D.Double(72, 70, 34, 26));
        g.setStroke(new BasicStroke(8f));
        g.drawLine(54, 94, 54, 22); g.drawLine(102, 82, 102, 14);
        g.fill(new java.awt.Polygon(new int[]{50, 106, 106, 50}, new int[]{18, 8, 26, 36}, 4));
    }

    private static void heart(Graphics2D g) {
        Path2D h = new Path2D.Double();
        h.moveTo(64, 112);
        h.curveTo(10, 76, 12, 22, 44, 22);
        h.curveTo(56, 22, 62, 32, 64, 40);
        h.curveTo(66, 32, 72, 22, 84, 22);
        h.curveTo(116, 22, 118, 76, 64, 112);
        g.setColor(Color.WHITE);
        g.fill(h);
    }

    private static void shard(Graphics2D g) {
        soft(g, new Color(200, 220, 255, 120), 0.6f);
        Path2D p = new Path2D.Double();
        p.moveTo(64, 8); p.lineTo(92, 56); p.lineTo(70, 120); p.lineTo(40, 64); p.closePath();
        g.setPaint(new GradientPaint(40, 10, new Color(255, 255, 255), 90, 120, new Color(150, 200, 255)));
        g.fill(p);
        g.setColor(new Color(255, 255, 255, 180));
        g.drawLine(64, 8, 70, 120);
    }

    private static void butterfly(Graphics2D g, float open) {
        g.translate(64, 64);
        for (int side = -1; side <= 1; side += 2) {
            AffineTransform at = g.getTransform();
            g.scale(side * open, 1);
            Path2D up = new Path2D.Double();
            up.moveTo(2, -4); up.curveTo(20, -52, 62, -48, 54, -14); up.curveTo(48, 2, 20, 6, 2, 2); up.closePath();
            Path2D lo = new Path2D.Double();
            lo.moveTo(2, 2); lo.curveTo(30, 6, 48, 30, 32, 46); lo.curveTo(18, 56, 6, 30, 2, 8); lo.closePath();
            g.setPaint(new GradientPaint(0, -40, new Color(90, 200, 255), 50, 10, new Color(150, 80, 255)));
            g.fill(up);
            g.setPaint(new GradientPaint(0, 0, new Color(120, 90, 255), 30, 48, new Color(255, 120, 220)));
            g.fill(lo);
            g.setColor(new Color(20, 16, 40, 200));
            g.setStroke(new BasicStroke(3f));
            g.draw(up);
            g.draw(lo);
            g.setColor(new Color(255, 255, 255, 220));
            g.fill(new Ellipse2D.Double(30, -34, 9, 9));
            g.setTransform(at);
        }
        g.setColor(new Color(30, 24, 40));
        g.fill(new RoundRectangle2D.Double(-3, -22, 6, 50, 6, 6));
    }
}
