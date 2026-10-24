package dev.lego.cosmetic;

import static dev.lego.cosmetic.Capes.col;
import static dev.lego.cosmetic.Capes.fbm;
import static dev.lego.cosmetic.Capes.hash;
import static dev.lego.cosmetic.Capes.mix;
import static dev.lego.cosmetic.Capes.shade;
import static dev.lego.cosmetic.Tex2.argb;
import static dev.lego.cosmetic.Tex2.img;
import static dev.lego.cosmetic.Tex2.pix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/** Textures of hats, face items, back items and aura particles. */
final class Tex3 {
    private Tex3() { }

    static CTex.T paint(String name) {
        switch (name) {
            // ------------------------------------------------------------ tintable materials (white based)
            case "plastic": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | mix(0xFFFFFF, 0xC8CCD4, v * 0.8 + Math.abs(u - 0.3) * 0.3));
            case "satin": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xF2F2F2, 0.86 + 0.14 * Math.sin((u * 3 + v) * Math.PI * 2)));
            case "cloth": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xF4F4F4, 0.93 + 0.07 * hash(x, y, 3) - ((x + y) % 4 == 0 ? 0.04 : 0)));
            case "knit": return pix(64, 64, (u, v, x, y) -> {
                double fx = (x % 8) / 8.0, fy = (y % 8) / 8.0;
                double vv = Math.abs(fx - 0.5) * 2;
                double stitch = 1 - Math.abs(fy - vv * 0.5 - 0.25) * 2.2;
                return 0xFF000000 | shade(0xF4F4F4, 0.72 + 0.3 * Math.max(0, stitch) + 0.05 * hash(x, y, 2));
            });
            case "leather": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0x8A5A32, 0x5A3418, v), 0.85 + 0.25 * fbm(u * 10, v * 10, 0, 0, 4, 3)));
            case "leather_dark": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0x3A2618, 0x1E120A, v), 0.85 + 0.25 * fbm(u * 10, v * 10, 0, 0, 4, 3)));
            case "steel": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0xE8ECF2, 0x7A8494, v * 0.7 + 0.3 * Math.abs(Math.sin(u * 6))), 0.95 + 0.08 * hash(x, y / 3, 5)));
            case "iron": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0xB0B6C0, 0x4A505A, v), 0.85 + 0.25 * fbm(u * 8, v * 8, 0, 0, 3, 9)));
            case "wood": return pix(64, 64, (u, v, x, y) -> {
                double r = Math.sin((u * 3 + fbm(u * 2, v * 8, 0, 0, 3, 4) * 2) * Math.PI * 4);
                return 0xFF000000 | mix(0xB0703A, 0x6A3A18, 0.5 + 0.5 * r);
            });
            case "ivory": return pix(32, 64, (u, v, x, y) -> 0xFF000000 | shade(mix(0xFFF6E0, 0xC8B48A, v), 0.92 + 0.1 * Math.sin(v * 40)));
            case "gloss": return img(64, 64, g -> {
                g.setPaint(new RadialGradientPaint(22, 18, 50, new float[]{0f, 0.2f, 1f}, new Color[]{Color.WHITE, new Color(0xF4F4F4), new Color(0xA8A8A8)}));
                g.fillRect(0, 0, 64, 64);
            });
            // ------------------------------------------------------------ hats
            case "fox_ear": return img(64, 64, g -> {
                Path2D o = new Path2D.Double();
                o.moveTo(2, 62); o.quadTo(10, 22, 32, 2); o.quadTo(54, 22, 62, 62); o.closePath();
                g.setPaint(new GradientPaint(0, 0, new Color(0xFF8A2A), 0, 64, new Color(0xD85A10)));
                g.fill(o);
                g.setClip(o);
                g.setColor(new Color(0x2A1A12));
                g.fill(new Ellipse2D.Double(10, -8, 44, 28));
                g.setClip(null);
                Path2D in = new Path2D.Double();
                in.moveTo(14, 62); in.quadTo(20, 32, 32, 18); in.quadTo(44, 32, 50, 62); in.closePath();
                g.setPaint(new GradientPaint(0, 18, new Color(0xFFF6EE), 0, 62, new Color(0xFFD8C0)));
                g.fill(in);
            });
            case "cap_segments": return pix(128, 64, (u, v, x, y) -> {
                int[] c = {0xE3000B, 0xFFCD03, 0x006CB7, 0x00A03A};
                int seg = (int) (u * 8) % 4;
                return 0xFF000000 | shade(c[seg], 0.85 + 0.2 * (1 - v) - (Math.abs((u * 8) % 1 - 0.5) > 0.47 ? 0.25 : 0));
            });
            case "party_cone": return pix(64, 128, (u, v, x, y) -> {
                boolean st = ((u * 4 + v * 3) % 1 + 1) % 1 < 0.5;
                int c = st ? 0xFF3A8A : 0x3AC8FF;
                if (hash(x / 6, y / 6, 7) > 0.9) c = 0xFFE24A;
                return 0xFF000000 | shade(c, 0.85 + 0.15 * v);
            });
            case "wizard_cone": return img(64, 128, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0x3A2AAA), 0, 128, new Color(0x160C5A)));
                g.fillRect(0, 0, 64, 128);
                Random r = new Random(3);
                for (int i = 0; i < 14; i++) star(g, r.nextDouble() * 64, r.nextDouble() * 128, 3 + r.nextDouble() * 4, 0xFFE27A);
                g.setColor(col(0xFFE27A, 0.8));
                for (int i = 0; i < 30; i++) g.fill(new Ellipse2D.Double(r.nextDouble() * 64, r.nextDouble() * 128, 1.5, 1.5));
            });
            case "mushroom_cap": return img(128, 64, g -> {
                g.setPaint(new GradientPaint(0, 0, new Color(0xFF3A2A), 0, 64, new Color(0xB00A0A)));
                g.fillRect(0, 0, 128, 64);
                Random r = new Random(5);
                for (int i = 0; i < 14; i++) {
                    double x = (i % 7) * 18.3 + r.nextDouble() * 6, y = 8 + (i / 7) * 26 + r.nextDouble() * 8, s = 7 + r.nextDouble() * 6;
                    g.setColor(new Color(0xFFF8F0));
                    g.fill(new Ellipse2D.Double(x, y, s, s * 0.8));
                }
            });
            case "gill": return pix(64, 16, (u, v, x, y) -> 0xFF000000 | shade(0xF4E6D0, 0.8 + 0.2 * Math.abs(Math.sin(u * 60))));
            case "vine": return img(128, 32, g -> {
                g.setColor(new Color(0x3A7A2A));
                g.fillRect(0, 12, 128, 8);
                Random r = new Random(2);
                for (int i = 0; i < 18; i++) {
                    double x = i * 7.3, y = 16;
                    AffineTransform o = g.getTransform();
                    g.translate(x, y);
                    g.rotate((i % 2 == 0 ? -1 : 1) * (0.6 + r.nextDouble() * 0.4));
                    g.setPaint(new GradientPaint(0, 0, new Color(0x7AD84A), 14, 0, new Color(0x2A8A2A)));
                    g.fill(new Ellipse2D.Double(0, -4, 15, 8));
                    g.setTransform(o);
                }
            });
            case "flower_pink": return flower(0xFF8AB8, 0xFFE0EE, 0xFFD23A);
            case "flower_white": return flower(0xF0F0FF, 0xFFFFFF, 0xFFC83A);
            case "flower_yellow": return flower(0xFFD23A, 0xFFF4A0, 0xE0602A);
            case "flower_blue": return flower(0x6AB8FF, 0xD8F0FF, 0xFFF4A0);
            // ------------------------------------------------------------ face
            case "heart_glasses": return img(256, 96, g -> {
                for (int s = 0; s < 2; s++) {
                    double cx = s == 0 ? 64 : 192;
                    Path2D h = CapeArt2.heartPath(cx, 50, 104);
                    g.setColor(new Color(0xE0115F));
                    g.fill(h);
                    Path2D in = CapeArt2.heartPath(cx, 50, 84);
                    g.setPaint(new GradientPaint(0, 10, new Color(0xFF7AB0), 0, 90, new Color(0xC0104A)));
                    g.fill(in);
                    g.setColor(col(0xFFFFFF, 0.7));
                    g.fill(new Ellipse2D.Double(cx - 30, 24, 18, 10));
                }
                g.setColor(new Color(0xE0115F));
                g.fillRect(110, 30, 36, 8);
            });
            case "star_glasses": return img(256, 96, g -> {
                for (int s = 0; s < 2; s++) {
                    double cx = s == 0 ? 64 : 192;
                    Path2D st = starPath(cx, 50, 48, 22);
                    g.setColor(new Color(0xE8A010));
                    g.fill(st);
                    g.setPaint(new GradientPaint(0, 10, new Color(0xFFF4A0), 0, 90, new Color(0xFFB020)));
                    g.fill(starPath(cx, 51, 40, 18));
                    g.setColor(col(0x7A3A00, 0.5));
                    g.fill(new Ellipse2D.Double(cx - 16, 36, 32, 28));
                }
                g.setColor(new Color(0xE8A010));
                g.fillRect(104, 40, 48, 7);
            });
            case "monocle_glass": return img(64, 64, g -> {
                g.setPaint(new RadialGradientPaint(24, 22, 40, new float[]{0f, 1f}, new Color[]{col(0xFFFFFF, 0.55), col(0xB8E0FF, 0.25)}));
                g.fill(new Ellipse2D.Double(2, 2, 60, 60));
                g.setColor(col(0xFFFFFF, 0.8));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new java.awt.geom.Arc2D.Double(12, 12, 40, 40, 100, 70, java.awt.geom.Arc2D.OPEN));
            });
            case "mustache": return img(128, 48, g -> {
                Path2D m = new Path2D.Double();
                m.moveTo(64, 14);
                m.curveTo(80, 2, 96, 8, 104, 22); m.curveTo(110, 30, 122, 30, 126, 18); m.curveTo(126, 38, 106, 46, 90, 38);
                m.curveTo(80, 34, 70, 30, 64, 26);
                m.curveTo(58, 30, 48, 34, 38, 38); m.curveTo(22, 46, 2, 38, 2, 18); m.curveTo(6, 30, 18, 30, 24, 22); m.curveTo(32, 8, 48, 2, 64, 14);
                g.setPaint(new GradientPaint(0, 4, new Color(0x6A4228), 0, 44, new Color(0x2A160A)));
                g.fill(m);
                g.setClip(m);
                g.setColor(col(0xB08058, 0.35));
                g.setStroke(new BasicStroke(1.2f));
                for (int i = 0; i < 26; i++) {
                    double x = 6 + i * 4.6;
                    g.draw(new QuadCurve2D.Double(64 + (x - 64) * 0.3, 14, x, 20, x + (x < 64 ? -6 : 6), 40));
                }
            });
            case "visor": return img(256, 64, g -> {
                g.setPaint(new LinearGradientPaint(0, 0, 0, 64, new float[]{0f, 0.5f, 1f}, new Color[]{col(0x1A0A3A, 0.92), col(0x2A1A6A, 0.85), col(0x0A0620, 0.95)}));
                g.fill(new RoundRectangle2D.Double(0, 4, 256, 56, 30, 30));
                g.setColor(col(0x2AE6FF, 0.25));
                for (int y = 8; y < 60; y += 4) g.fillRect(10, y, 236, 1);
                g.setPaint(new GradientPaint(0, 4, col(0xFFFFFF, 0.35), 0, 26, col(0xFFFFFF, 0)));
                g.fill(new RoundRectangle2D.Double(8, 6, 240, 20, 16, 16));
            });
            case "mask_masq": return img(256, 128, g -> {
                Path2D m = new Path2D.Double();
                m.moveTo(128, 40); m.curveTo(150, 20, 220, 10, 252, 30); m.curveTo(250, 70, 220, 110, 170, 104); m.curveTo(150, 100, 138, 86, 128, 80);
                m.curveTo(118, 86, 106, 100, 86, 104); m.curveTo(36, 110, 6, 70, 4, 30); m.curveTo(36, 10, 106, 20, 128, 40); m.closePath();
                g.setPaint(new LinearGradientPaint(0, 0, 256, 128, new float[]{0f, 0.5f, 1f}, new Color[]{new Color(0x5A1A8A), new Color(0x8A2AC8), new Color(0x3A0A6A)}));
                g.fill(m);
                g.setClip(m);
                g.setColor(col(0xFFD27A, 0.5));
                g.setStroke(new BasicStroke(2f));
                for (int i = 0; i < 6; i++) g.draw(new QuadCurve2D.Double(0, 20 + i * 18, 128, 60 + i * 10, 256, 20 + i * 18));
                g.setClip(null);
                g.setColor(new Color(0xFFD27A));
                g.setStroke(new BasicStroke(4f));
                g.draw(m);
                // eye holes (transparent)
                g.setComposite(java.awt.AlphaComposite.Clear);
                g.fill(new Ellipse2D.Double(56, 44, 50, 30));
                g.fill(new Ellipse2D.Double(150, 44, 50, 30));
                g.setComposite(java.awt.AlphaComposite.SrcOver);
                g.setColor(new Color(0xFFD27A));
                g.setStroke(new BasicStroke(3f));
                g.draw(new Ellipse2D.Double(56, 44, 50, 30));
                g.draw(new Ellipse2D.Double(150, 44, 50, 30));
                Random r = new Random(4);
                for (int i = 0; i < 14; i++) {
                    g.setColor(col(0xFFFFFF, 0.8));
                    g.fill(new Ellipse2D.Double(20 + r.nextDouble() * 216, 20 + r.nextDouble() * 80, 3, 3));
                }
            });
            case "feather_plume": return Tex2.feather(0xFFFFFF, 0xE0A0FF, 0x8A2AC8, 0xFFFFFF, "none", false);
            // ------------------------------------------------------------ back
            case "shield_face": return img(128, 128, g -> {
                g.setPaint(new RadialGradientPaint(50, 44, 80, new float[]{0f, 1f}, new Color[]{new Color(0x3A6AE0), new Color(0x0E2A7A)}));
                g.fillRect(0, 0, 128, 128);
                g.setColor(new Color(0xE8E8F0));
                Path2D c = new Path2D.Double();
                c.moveTo(64, 0); c.lineTo(76, 0); c.lineTo(76, 128); c.lineTo(52, 128); c.lineTo(52, 0); c.closePath();
                g.fill(new java.awt.geom.Rectangle2D.Double(54, 0, 20, 128));
                g.fill(new java.awt.geom.Rectangle2D.Double(0, 54, 128, 20));
                g.setPaint(new RadialGradientPaint(64, 64, 22, new float[]{0f, 1f}, new Color[]{new Color(0xFFF4B8), new Color(0xC8961A)}));
                g.fill(new Ellipse2D.Double(42, 42, 44, 44));
                g.setColor(new Color(0x8A5A0A));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(42, 42, 44, 44));
                dev.lego.ui.Art.brick(g, 48, 44, 32, new Color(0xE3000B));
            });
            case "guitar_body": return img(128, 128, g -> {
                g.setPaint(new RadialGradientPaint(64, 60, 70, new float[]{0f, 0.5f, 1f}, new Color[]{new Color(0xFFD27A), new Color(0xE0602A), new Color(0x7A1A0A)}));
                g.fillRect(0, 0, 128, 128);
                g.setColor(new Color(0x1A0A04));
                g.fill(new Ellipse2D.Double(48, 40, 32, 32));
                g.setColor(new Color(0xE8D8B0));
                g.setStroke(new BasicStroke(2.5f));
                g.draw(new Ellipse2D.Double(44, 36, 40, 40));
                g.setColor(new Color(0x2A1A0A));
                g.fillRect(44, 92, 40, 8);
            });
            case "strings": return pix(16, 64, (u, v, x, y) -> (x % 4 == 1) ? 0xFFE8E8F0 : 0x00FFFFFF);
            case "teddy_face": return img(64, 64, g -> {
                g.setColor(new Color(0xB07A48));
                g.fillRect(0, 0, 64, 64);
                g.setColor(new Color(0xE8C898));
                g.fill(new Ellipse2D.Double(18, 30, 28, 22));
                g.setColor(new Color(0x1A0E08));
                g.fill(new Ellipse2D.Double(16, 18, 9, 10));
                g.fill(new Ellipse2D.Double(39, 18, 9, 10));
                g.fill(new Ellipse2D.Double(27, 32, 10, 7));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new QuadCurve2D.Double(26, 44, 32, 49, 38, 44));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(18, 19, 3, 3));
                g.fill(new Ellipse2D.Double(41, 19, 3, 3));
                g.setColor(col(0xFF7A9A, 0.5));
                g.fill(new Ellipse2D.Double(8, 32, 9, 6));
                g.fill(new Ellipse2D.Double(47, 32, 9, 6));
            });
            case "fur_brown": return pix(64, 64, (u, v, x, y) -> 0xFF000000 | shade(0xB07A48, 0.82 + 0.3 * fbm(u * 16, v * 16, 0, 0, 3, 6)));
            case "rocket_body": return pix(64, 128, (u, v, x, y) -> {
                int c = 0xF4F6FA;
                if (v > 0.3 && v < 0.36) c = 0xE3000B;
                if (v > 0.75 && v < 0.8) c = 0xE3000B;
                double sh = 0.75 + 0.3 * Math.sin(u * Math.PI);
                if (Math.abs((u * 8) % 1 - 0.5) > 0.48) sh *= 0.9;
                return 0xFF000000 | shade(c, sh);
            });
            case "window": return img(32, 32, g -> {
                g.setColor(new Color(0x9AA2B0));
                g.fill(new Ellipse2D.Double(0, 0, 32, 32));
                g.setPaint(new RadialGradientPaint(12, 12, 16, new float[]{0f, 1f}, new Color[]{new Color(0xB8F0FF), new Color(0x1A5AA8)}));
                g.fill(new Ellipse2D.Double(4, 4, 24, 24));
            });
            case "sword_blade": return img(32, 128, g -> {
                g.setPaint(new LinearGradientPaint(0, 0, 32, 0, new float[]{0f, 0.5f, 0.52f, 1f}, new Color[]{new Color(0xDFFBFF), new Color(0x7FE8FF), new Color(0x2A9AD8), new Color(0x1A5A9A)}));
                g.fillRect(0, 0, 32, 128);
                g.setColor(col(0xFFFFFF, 0.8));
                g.fillRect(14, 0, 3, 128);
                Random r = new Random(3);
                g.setColor(col(0xE0FFFF, 0.9));
                for (int i = 0; i < 6; i++) {
                    double y = 20 + i * 16;
                    g.draw(new java.awt.geom.Line2D.Double(8, y, 12 + r.nextDouble() * 8, y + 6));
                }
            });
            case "arrow_fletch": return img(32, 64, g -> {
                Path2D f = new Path2D.Double();
                f.moveTo(16, 0); f.lineTo(30, 20); f.lineTo(30, 62); f.lineTo(16, 48); f.lineTo(2, 62); f.lineTo(2, 20); f.closePath();
                g.setPaint(new GradientPaint(0, 0, new Color(0xFFFFFF), 0, 64, new Color(0xE0405A)));
                g.fill(f);
            });
            // ------------------------------------------------------------ aura sprites
            case "petal": return img(32, 32, g -> {
                Path2D p = new Path2D.Double();
                p.moveTo(16, 30); p.curveTo(30, 22, 28, 6, 19, 2); p.lineTo(16, 7); p.lineTo(13, 2); p.curveTo(4, 6, 2, 22, 16, 30);
                g.setPaint(new GradientPaint(0, 30, new Color(0xFF8FB4), 0, 2, new Color(0xFFF0F6)));
                g.fill(p);
            });
            case "note": return img(64, 64, g -> {
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(6, 40, 20, 15));
                g.fill(new Ellipse2D.Double(36, 34, 20, 15));
                g.setStroke(new BasicStroke(4f));
                g.drawLine(24, 47, 24, 10);
                g.drawLine(54, 41, 54, 4);
                g.setStroke(new BasicStroke(8f));
                g.drawLine(24, 12, 54, 6);
            });
            case "bubble": return img(64, 64, g -> {
                g.setPaint(new RadialGradientPaint(32, 32, 30, new float[]{0f, 0.75f, 0.95f, 1f}, new Color[]{col(0xB8F0FF, 0.08), col(0xB8E8FF, 0.25), col(0xFFFFFF, 0.8), col(0xFFFFFF, 0)}));
                g.fill(new Ellipse2D.Double(1, 1, 62, 62));
                g.setColor(col(0xFFFFFF, 0.9));
                g.fill(new Ellipse2D.Double(16, 13, 12, 8));
                g.setColor(col(0xFF8AE0, 0.25));
                g.setStroke(new BasicStroke(2f));
                g.draw(new java.awt.geom.Arc2D.Double(6, 6, 52, 52, 200, 80, java.awt.geom.Arc2D.OPEN));
            });
            case "leaf": return img(48, 48, g -> {
                Path2D l = new Path2D.Double();
                l.moveTo(4, 44); l.curveTo(4, 14, 24, 4, 44, 4); l.curveTo(44, 26, 30, 44, 4, 44);
                g.setPaint(new GradientPaint(4, 44, new Color(0xE0501A), 44, 4, new Color(0xFFC03A)));
                g.fill(l);
                g.setColor(col(0x7A2A0A, 0.7));
                g.setStroke(new BasicStroke(1.5f));
                g.draw(new QuadCurve2D.Double(4, 44, 22, 24, 40, 8));
            });
            case "rune_circle": return img(256, 256, g -> {
                g.translate(128, 128);
                int c = 0xC88AFF;
                for (int i = 4; i >= 1; i--) {
                    g.setColor(col(c, 0.1 * (5 - i)));
                    g.setStroke(new BasicStroke(i * 3f));
                    g.draw(new Ellipse2D.Double(-120, -120, 240, 240));
                    g.draw(new Ellipse2D.Double(-96, -96, 192, 192));
                }
                g.setColor(col(0xF0DCFF, 0.95));
                g.setStroke(new BasicStroke(2.5f));
                g.draw(new Ellipse2D.Double(-120, -120, 240, 240));
                g.draw(new Ellipse2D.Double(-96, -96, 192, 192));
                g.draw(new Ellipse2D.Double(-40, -40, 80, 80));
                Random r = new Random(7);
                for (int i = 0; i < 20; i++) {
                    AffineTransform o = g.getTransform();
                    g.rotate(Math.PI * 2 * i / 20);
                    g.translate(0, -108);
                    for (int s = 0; s < 3; s++) g.drawLine(r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5, r.nextInt(3) * 5 - 5);
                    g.setTransform(o);
                }
                for (int t = 0; t < 2; t++) {
                    Path2D p = new Path2D.Double();
                    for (int i = 0; i < 3; i++) {
                        double a = t * Math.PI / 3 + i * Math.PI * 2 / 3;
                        if (i == 0) p.moveTo(Math.cos(a) * 94, Math.sin(a) * 94); else p.lineTo(Math.cos(a) * 94, Math.sin(a) * 94);
                    }
                    p.closePath();
                    g.draw(p);
                }
            });
            case "glyph": return img(32, 32, g -> {
                g.setColor(col(0xE8C8FF, 1));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(8, 6, 8, 26); g.drawLine(8, 16, 24, 8); g.drawLine(16, 12, 24, 26);
            });
            case "ring_glow": return pix(256, 16, (u, v, x, y) -> argb(0xFFFFFF, Math.pow(1 - Math.abs(v - 0.5) * 2, 1.6)));
            case "void_orb": return img(64, 64, g -> {
                g.setPaint(new RadialGradientPaint(32, 32, 32, new float[]{0f, 0.35f, 0.55f, 1f}, new Color[]{new Color(0x0A0012), new Color(0x1A0030), col(0xB04AFF, 0.9), col(0x6A1AC8, 0)}));
                g.fillRect(0, 0, 64, 64);
            });
            default: return null;
        }
    }

    static void star(Graphics2D g, double x, double y, double r, int rgb) {
        g.setColor(new Color(rgb));
        g.fill(starPath(x, y, r, r * 0.45));
    }

    static Path2D starPath(double cx, double cy, double R, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? R : r;
            if (i == 0) p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }
        p.closePath();
        return p;
    }

    static CTex.T flower(int petal, int light, int center) {
        return img(64, 64, g -> {
            g.translate(32, 32);
            for (int i = 0; i < 6; i++) {
                AffineTransform o = g.getTransform();
                g.rotate(Math.PI * 2 * i / 6);
                g.setPaint(new GradientPaint(0, 0, new Color(petal), 0, -28, new Color(light)));
                g.fill(new Ellipse2D.Double(-8, -30, 16, 28));
                g.setTransform(o);
            }
            g.setPaint(new RadialGradientPaint(-2, -2, 10, new float[]{0f, 1f}, new Color[]{new Color(0xFFF6C0), new Color(center)}));
            g.fill(new Ellipse2D.Double(-8, -8, 16, 16));
        });
    }
}
