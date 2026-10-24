package dev.lego.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/** Line icons on a 24x24 grid (scaled to the requested pixel size). */
public final class Icons {
    private Icons() { }

    private static Graphics2D g;
    private static double k;

    private static void stroke(double w) { g.setStroke(new BasicStroke((float) (w * k), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); }
    private static void line(double x1, double y1, double x2, double y2) { g.draw(new Line2D.Double(x1 * k, y1 * k, x2 * k, y2 * k)); }
    private static void circ(double cx, double cy, double r) { g.draw(new Ellipse2D.Double((cx - r) * k, (cy - r) * k, 2 * r * k, 2 * r * k)); }
    private static void disc(double cx, double cy, double r) { g.fill(new Ellipse2D.Double((cx - r) * k, (cy - r) * k, 2 * r * k, 2 * r * k)); }
    private static void rr(double x, double y, double w, double h, double r) { g.draw(new RoundRectangle2D.Double(x * k, y * k, w * k, h * k, 2 * r * k, 2 * r * k)); }
    private static void rrf(double x, double y, double w, double h, double r) { g.fill(new RoundRectangle2D.Double(x * k, y * k, w * k, h * k, 2 * r * k, 2 * r * k)); }
    private static void arc(double cx, double cy, double r, double start, double ext) {
        g.draw(new Arc2D.Double((cx - r) * k, (cy - r) * k, 2 * r * k, 2 * r * k, start, ext, Arc2D.OPEN));
    }
    private static Path2D.Double p(double... xy) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(xy[0] * k, xy[1] * k);
        for (int i = 2; i < xy.length; i += 2) path.lineTo(xy[i] * k, xy[i + 1] * k);
        return path;
    }
    private static void poly(double... xy) { g.draw(p(xy)); }
    private static void polyC(double... xy) { Path2D.Double path = p(xy); path.closePath(); g.draw(path); }
    private static void polyF(double... xy) { Path2D.Double path = p(xy); path.closePath(); g.fill(path); }

    public static void paint(Graphics2D gg, String name, int size, Color c) {
        g = gg;
        k = size / 24.0;
        g.setColor(c);
        stroke(2);
        switch (name) {
            case "mods": case "grid":
                rr(3, 3, 7, 7, 1.5); rr(14, 3, 7, 7, 1.5); rr(3, 14, 7, 7, 1.5); rr(14, 14, 7, 7, 1.5); break;
            case "shirt": case "cosmetics":
                polyC(8, 3, 4, 5, 2, 10, 5.5, 11.5, 6, 21, 18, 21, 18.5, 11.5, 22, 10, 20, 5, 16, 3, 14.5, 5, 9.5, 5); break;
            case "emote": case "dance":
                circ(13, 4, 2); poly(4, 11, 9, 8, 14, 9, 17, 13); poly(12, 9, 10, 15, 14, 18, 14, 22); poly(10, 15, 6, 22); poly(17, 13, 21, 12); break;
            case "gamepad": case "games":
                g.draw(new RoundRectangle2D.Double(2 * k, 7 * k, 20 * k, 11 * k, 9 * k, 9 * k));
                line(6.5, 12.5, 10.5, 12.5); line(8.5, 10.5, 8.5, 14.5); disc(15, 11.5, 1.1); disc(18, 13.5, 1.1); break;
            case "layout": case "hud":
                rr(3, 3, 18, 18, 2.5); line(3, 9, 21, 9); line(9, 9, 9, 21); break;
            case "palette": case "design":
                g.draw(new Path2D.Double() {{
                    moveTo(12 * k, 3 * k);
                    curveTo(6.5 * k, 3 * k, 2.5 * k, 7 * k, 2.5 * k, 12 * k);
                    curveTo(2.5 * k, 17.5 * k, 7 * k, 21.5 * k, 12 * k, 21.5 * k);
                    curveTo(13.5 * k, 21.5 * k, 14 * k, 20.5 * k, 13.5 * k, 19.3 * k);
                    curveTo(12.8 * k, 17.6 * k, 13.8 * k, 16 * k, 15.5 * k, 16 * k);
                    lineTo(17.5 * k, 16 * k);
                    curveTo(20 * k, 16 * k, 21.5 * k, 14.5 * k, 21.5 * k, 12 * k);
                    curveTo(21.5 * k, 7 * k, 17.5 * k, 3 * k, 12 * k, 3 * k);
                }});
                disc(7.5, 11, 1.3); disc(10.5, 7, 1.3); disc(15.5, 7.5, 1.3); break;
            case "search":
                circ(10.5, 10.5, 6.5); line(15.5, 15.5, 21, 21); break;
            case "x": case "close":
                line(6, 6, 18, 18); line(18, 6, 6, 18); break;
            case "back": case "chevron-left":
                poly(15, 5, 8, 12, 15, 19); break;
            case "chevron-right":
                poly(9, 5, 16, 12, 9, 19); break;
            case "chevron-down":
                poly(5, 9, 12, 16, 19, 9); break;
            case "chevron-up":
                poly(5, 15, 12, 8, 19, 15); break;
            case "dots":
                disc(5, 12, 1.8); disc(12, 12, 1.8); disc(19, 12, 1.8); break;
            case "gear": case "settings": {
                circ(12, 12, 3.2);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    line(12 + Math.cos(a) * 6.3, 12 + Math.sin(a) * 6.3, 12 + Math.cos(a) * 9, 12 + Math.sin(a) * 9);
                }
                circ(12, 12, 6.6);
                break;
            }
            case "sparkle": case "visuals":
                polyF(12, 2, 14, 10, 22, 12, 14, 14, 12, 22, 10, 14, 2, 12, 10, 10);
                polyF(19, 2.5, 19.8, 4.2, 21.5, 5, 19.8, 5.8, 19, 7.5, 18.2, 5.8, 16.5, 5, 18.2, 4.2); break;
            case "bolt":
                polyF(13, 2, 4, 14, 11, 14, 10, 22, 20, 9, 13, 9); break;
            case "wifi":
                arc(12, 19, 15, 45, 90); arc(12, 19, 10, 45, 90); arc(12, 19, 5, 45, 90); disc(12, 19, 1.4); break;
            case "mouse":
                rr(6, 2.5, 12, 19, 6); line(12, 6.5, 12, 10); break;
            case "pin": case "map-pin":
                g.draw(new Path2D.Double() {{
                    moveTo(12 * k, 21.5 * k);
                    curveTo(12 * k, 21.5 * k, 19 * k, 15 * k, 19 * k, 9.5 * k);
                    curveTo(19 * k, 5.5 * k, 16 * k, 2.5 * k, 12 * k, 2.5 * k);
                    curveTo(8 * k, 2.5 * k, 5 * k, 5.5 * k, 5 * k, 9.5 * k);
                    curveTo(5 * k, 15 * k, 12 * k, 21.5 * k, 12 * k, 21.5 * k);
                }});
                circ(12, 9.5, 2.5); break;
            case "compass":
                circ(12, 12, 9.5); polyF(15.5, 8.5, 13.5, 13.5, 8.5, 15.5, 10.5, 10.5); break;
            case "clock":
                circ(12, 12, 9.5); poly(12, 7, 12, 12, 15.5, 14); break;
            case "hourglass":
                line(6, 2.5, 18, 2.5); line(6, 21.5, 18, 21.5); poly(7, 2.5, 7, 6, 12, 12, 7, 18, 7, 21.5); poly(17, 2.5, 17, 6, 12, 12, 17, 18, 17, 21.5); break;
            case "car": case "vehicle":
                g.draw(new Path2D.Double() {{
                    moveTo(3 * k, 15 * k); lineTo(4.5 * k, 10 * k); lineTo(8 * k, 6.5 * k); lineTo(16 * k, 6.5 * k); lineTo(19.5 * k, 10 * k);
                    lineTo(21 * k, 15 * k); lineTo(21 * k, 17 * k); lineTo(3 * k, 17 * k); closePath();
                }});
                line(4.5, 10.5, 19.5, 10.5);
                disc(7.5, 17.5, 2.2); disc(16.5, 17.5, 2.2);
                break;
            case "speed": case "gauge":
                arc(12, 14, 9, -20, 220); line(12, 14, 16.5, 9); disc(12, 14, 1.6); break;
            case "chip": case "memory": case "cpu":
                rr(5.5, 5.5, 13, 13, 2); rr(9, 9, 6, 6, 1);
                for (int i = 0; i < 3; i++) {
                    double o = 9 + i * 3;
                    line(o, 2, o, 5.5); line(o, 18.5, o, 22); line(2, o, 5.5, o); line(18.5, o, 22, o);
                }
                break;
            case "globe": case "server":
                circ(12, 12, 9.5); g.draw(new Ellipse2D.Double(7.5 * k, 2.5 * k, 9 * k, 19 * k)); line(2.5, 12, 21.5, 12); break;
            case "shield":
                g.draw(new Path2D.Double() {{
                    moveTo(12 * k, 2.5 * k); lineTo(20 * k, 5.5 * k); lineTo(20 * k, 11 * k);
                    curveTo(20 * k, 16.5 * k, 16.5 * k, 20 * k, 12 * k, 21.5 * k);
                    curveTo(7.5 * k, 20 * k, 4 * k, 16.5 * k, 4 * k, 11 * k);
                    lineTo(4 * k, 5.5 * k); closePath();
                }});
                break;
            case "potion": case "flask":
                poly(9, 2.5, 9, 9, 4.5, 18, 5.5, 21, 18.5, 21, 19.5, 18, 15, 9, 15, 2.5); line(7.5, 2.5, 16.5, 2.5); line(6.5, 15, 17.5, 15); break;
            case "keys": case "keyboard":
                rr(2, 5, 20, 14, 2.5);
                for (int i = 0; i < 4; i++) { disc(6 + i * 4, 9.5, 0.9); disc(6 + i * 4, 13, 0.9); }
                line(8, 16, 16, 16); break;
            case "sun":
                circ(12, 12, 4.2);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    line(12 + Math.cos(a) * 7, 12 + Math.sin(a) * 7, 12 + Math.cos(a) * 9.5, 12 + Math.sin(a) * 9.5);
                }
                break;
            case "moon":
                g.draw(new Path2D.Double() {{
                    moveTo(20 * k, 14.5 * k);
                    curveTo(18.5 * k, 18.5 * k, 15 * k, 21 * k, 11 * k, 21 * k);
                    curveTo(6 * k, 21 * k, 3 * k, 17 * k, 3 * k, 12.5 * k);
                    curveTo(3 * k, 8 * k, 6 * k, 4.5 * k, 10 * k, 3.5 * k);
                    curveTo(8 * k, 9.5 * k, 13 * k, 16.5 * k, 20 * k, 14.5 * k);
                }});
                break;
            case "cloud": case "sky":
                g.draw(new Path2D.Double() {{
                    moveTo(7 * k, 19 * k);
                    curveTo(3.5 * k, 19 * k, 2 * k, 16.5 * k, 2.5 * k, 14 * k);
                    curveTo(3 * k, 11.5 * k, 5.5 * k, 10.5 * k, 7 * k, 11 * k);
                    curveTo(7.5 * k, 7 * k, 10.5 * k, 5 * k, 13.5 * k, 5 * k);
                    curveTo(17.5 * k, 5 * k, 20 * k, 8 * k, 19.5 * k, 11.5 * k);
                    curveTo(22 * k, 12 * k, 22.5 * k, 15 * k, 21.5 * k, 16.5 * k);
                    curveTo(20.8 * k, 18.2 * k, 19.5 * k, 19 * k, 17.5 * k, 19 * k);
                    closePath();
                }});
                break;
            case "leaf": case "biome":
                g.draw(new Path2D.Double() {{
                    moveTo(4 * k, 20 * k);
                    curveTo(4 * k, 10 * k, 10 * k, 4 * k, 20 * k, 4 * k);
                    curveTo(20 * k, 14 * k, 14 * k, 20 * k, 4 * k, 20 * k);
                }});
                line(4, 20, 13, 11); break;
            case "target": case "crosshair":
                circ(12, 12, 9); circ(12, 12, 4.5); disc(12, 12, 1.3); break;
            case "food":
                g.draw(new Ellipse2D.Double(10 * k, 3 * k, 11 * k, 11 * k)); line(12, 12, 5.5, 18.5); circ(4.5, 19.5, 1.8); break;
            case "heart":
                g.draw(heart()); break;
            case "heart-fill":
                g.fill(heart()); break;
            case "pulse":
                poly(2, 12, 7, 12, 9.5, 5, 14, 19, 16.5, 12, 22, 12); break;
            case "list":
                line(9, 6, 21, 6); line(9, 12, 21, 12); line(9, 18, 21, 18); disc(4.5, 6, 1.2); disc(4.5, 12, 1.2); disc(4.5, 18, 1.2); break;
            case "gem": case "crystal":
                polyC(6, 3, 18, 3, 22, 9, 12, 21.5, 2, 9); poly(2, 9, 22, 9); poly(9, 3, 7.5, 9, 12, 21.5, 16.5, 9, 15, 3); break;
            case "play":
                polyF(7, 4, 20, 12, 7, 20); break;
            case "pause":
                rrf(6, 4, 4, 16, 1); rrf(14, 4, 4, 16, 1); break;
            case "star":
                polyC(12, 2.5, 14.9, 8.6, 21.5, 9.3, 16.6, 13.8, 17.9, 20.4, 12, 17.1, 6.1, 20.4, 7.4, 13.8, 2.5, 9.3, 9.1, 8.6); break;
            case "star-fill":
                polyF(12, 2.5, 14.9, 8.6, 21.5, 9.3, 16.6, 13.8, 17.9, 20.4, 12, 17.1, 6.1, 20.4, 7.4, 13.8, 2.5, 9.3, 9.1, 8.6); break;
            case "trophy":
                poly(7, 3, 17, 3, 17, 9); g.draw(new Arc2D.Double(7 * k, 3 * k, 10 * k, 12 * k, 180, 180, Arc2D.OPEN)); line(7, 3, 7, 9);
                arc(5, 7, 2.5, 90, 180); arc(19, 7, 2.5, 270, 180); line(12, 15, 12, 18.5); line(8, 21, 16, 21); line(9.5, 18.5, 14.5, 18.5); break;
            case "eye":
                g.draw(new Path2D.Double() {{
                    moveTo(2 * k, 12 * k);
                    curveTo(5 * k, 6 * k, 9 * k, 4.5 * k, 12 * k, 4.5 * k);
                    curveTo(15 * k, 4.5 * k, 19 * k, 6 * k, 22 * k, 12 * k);
                    curveTo(19 * k, 18 * k, 15 * k, 19.5 * k, 12 * k, 19.5 * k);
                    curveTo(9 * k, 19.5 * k, 5 * k, 18 * k, 2 * k, 12 * k);
                }});
                circ(12, 12, 3); break;
            case "user":
                circ(12, 8, 4); g.draw(new Arc2D.Double(4 * k, 14 * k, 16 * k, 14 * k, 0, 180, Arc2D.OPEN)); break;
            case "users":
                circ(9, 8, 3.5); g.draw(new Arc2D.Double(2 * k, 14 * k, 14 * k, 12 * k, 0, 180, Arc2D.OPEN));
                arc(16.5, 8, 3, -80, 170); g.draw(new Arc2D.Double(13 * k, 14 * k, 10 * k, 12 * k, 0, 110, Arc2D.OPEN)); break;
            case "flame": case "fire":
                g.draw(new Path2D.Double() {{
                    moveTo(12 * k, 22 * k);
                    curveTo(7 * k, 22 * k, 4.5 * k, 18.5 * k, 4.5 * k, 15 * k);
                    curveTo(4.5 * k, 10 * k, 9 * k, 8 * k, 10 * k, 2 * k);
                    curveTo(14 * k, 5 * k, 15 * k, 8 * k, 14.5 * k, 11 * k);
                    curveTo(16 * k, 10.5 * k, 17 * k, 9 * k, 17.5 * k, 8 * k);
                    curveTo(19 * k, 10 * k, 19.5 * k, 12.5 * k, 19.5 * k, 15 * k);
                    curveTo(19.5 * k, 18.5 * k, 17 * k, 22 * k, 12 * k, 22 * k);
                }});
                break;
            case "snow":
                for (int i = 0; i < 3; i++) {
                    double a = Math.PI * i / 3;
                    double dx = Math.cos(a) * 9.5, dy = Math.sin(a) * 9.5;
                    line(12 - dx, 12 - dy, 12 + dx, 12 + dy);
                }
                circ(12, 12, 2); break;
            case "wing": case "wings":
                g.draw(new Path2D.Double() {{
                    moveTo(3 * k, 6 * k);
                    curveTo(10 * k, 5 * k, 17 * k, 7 * k, 21 * k, 18 * k);
                    curveTo(16 * k, 16.5 * k, 12 * k, 17.5 * k, 9 * k, 19 * k);
                    curveTo(9.5 * k, 16.5 * k, 8 * k, 15 * k, 6 * k, 14.5 * k);
                    curveTo(6.5 * k, 12.5 * k, 5.5 * k, 10.5 * k, 3.5 * k, 10 * k);
                    closePath();
                }});
                line(8, 9, 16.5, 16); break;
            case "crown": case "hat":
                polyC(3, 8, 7.5, 12, 12, 5, 16.5, 12, 21, 8, 19, 18, 5, 18); line(5, 21, 19, 21); break;
            case "glasses":
                circ(6.5, 14, 4); circ(17.5, 14, 4); arc(12, 14, 1.8, 30, 120); line(2.5, 13, 1.5, 9); line(21.5, 13, 22.5, 9); break;
            case "paw": case "pet":
                disc(6, 10, 2); disc(10, 6, 2); disc(14, 6, 2); disc(18, 10, 2);
                g.fill(new Ellipse2D.Double(7 * k, 11.5 * k, 10 * k, 9 * k)); break;
            case "backpack":
                rr(5, 6, 14, 16, 4); poly(9, 6, 9, 3, 15, 3, 15, 6); rr(8.5, 13, 7, 5, 1.5); break;
            case "aura":
                circ(12, 12, 4); arc(12, 12, 8.5, 20, 110); arc(12, 12, 8.5, 200, 110); disc(19.5, 5.5, 1.1); disc(4.5, 18.5, 1.1); break;
            case "music":
                poly(9, 18, 9, 5, 20, 3, 20, 16); circ(6.5, 18, 2.5); circ(17.5, 16, 2.5); break;
            case "brick": case "lego":
                rrf(2, 9, 20, 11, 2); rrf(5, 5, 5, 5, 1.2); rrf(14, 5, 5, 5, 1.2); break;
            case "trash":
                line(3, 6, 21, 6); poly(8, 6, 8, 3, 16, 3, 16, 6); poly(5, 6, 6, 21, 18, 21, 19, 6); line(10, 10, 10, 17); line(14, 10, 14, 17); break;
            case "reset": case "refresh":
                arc(12, 12, 8.5, 60, 290); polyF(3.5, 3.5, 3.5, 10, 10, 10); break;
            case "check":
                poly(4, 12.5, 9.5, 18, 20, 6); break;
            case "plus":
                line(12, 4, 12, 20); line(4, 12, 20, 12); break;
            case "minus":
                line(4, 12, 20, 12); break;
            case "lock":
                rr(4.5, 10.5, 15, 11, 2.5); arc(12, 7.5, 4.5, 0, 180); line(7.5, 7.5, 7.5, 10.5); line(16.5, 7.5, 16.5, 10.5); break;
            case "bell":
                g.draw(new Path2D.Double() {{
                    moveTo(6 * k, 16 * k); lineTo(6 * k, 10.5 * k);
                    curveTo(6 * k, 7 * k, 8.7 * k, 4 * k, 12 * k, 4 * k);
                    curveTo(15.3 * k, 4 * k, 18 * k, 7 * k, 18 * k, 10.5 * k);
                    lineTo(18 * k, 16 * k); lineTo(20 * k, 18 * k); lineTo(4 * k, 18 * k); closePath();
                }});
                arc(12, 19.5, 2.2, 180, 180); break;
            case "sword":
                line(4, 20, 17, 7); poly(17, 7, 21, 3, 21.5, 2.5, 20.8, 6.8); line(6, 14, 10, 18); line(3, 21, 5.5, 18.5); break;
            case "sprint": case "run":
                circ(14.5, 4, 2); poly(7, 10, 11, 7.5, 15, 8.5, 17, 12, 20, 12); poly(13.5, 8.5, 11.5, 14, 15.5, 16.5, 14.5, 21.5); poly(11.5, 14, 8, 19, 4, 19); break;
            case "sneak": case "crouch":
                circ(9, 5, 2); poly(9, 8, 12, 13, 18, 13, 18, 20); poly(12, 13, 10, 20, 6, 20); poly(9, 8, 5, 12); break;
            case "camera": case "freelook":
                rr(2.5, 7, 19, 13, 2.5); poly(8, 7, 9.5, 4, 14.5, 4, 16, 7); circ(12, 13.5, 3.8); break;
            case "chat":
                g.draw(new Path2D.Double() {{
                    moveTo(4 * k, 5 * k); lineTo(20 * k, 5 * k); lineTo(20 * k, 16 * k); lineTo(10 * k, 16 * k);
                    lineTo(5 * k, 20.5 * k); lineTo(5.5 * k, 16 * k); lineTo(4 * k, 16 * k); closePath();
                }});
                break;
            case "filter":
                polyC(3, 4, 21, 4, 14, 12.5, 14, 20, 10, 18, 10, 12.5); break;
            case "info":
                circ(12, 12, 9.5); line(12, 11, 12, 17); disc(12, 7.5, 1.2); break;
            case "gift":
                rr(3, 8, 18, 5, 1); rr(4.5, 13, 15, 8.5, 1); line(12, 8, 12, 21.5);
                arc(9, 6, 2.6, 0, 270); arc(15, 6, 2.6, -90, 270); break;
            case "image":
                rr(3, 4, 18, 16, 2.5); circ(8.5, 9.5, 1.8); poly(3, 17, 9, 12, 13, 15.5, 16, 13, 21, 17.5); break;
            case "waypoint": case "flag":
                line(5, 21, 5, 3); polyC(5, 4, 18, 4, 15.5, 8.5, 18, 13, 5, 13); break;
            case "skull": case "kill":
                g.draw(new Path2D.Double() {{
                    moveTo(7 * k, 20 * k); lineTo(7 * k, 17 * k);
                    curveTo(4.5 * k, 15.5 * k, 3.5 * k, 13 * k, 3.5 * k, 10.5 * k);
                    curveTo(3.5 * k, 6 * k, 7.3 * k, 2.5 * k, 12 * k, 2.5 * k);
                    curveTo(16.7 * k, 2.5 * k, 20.5 * k, 6 * k, 20.5 * k, 10.5 * k);
                    curveTo(20.5 * k, 13 * k, 19.5 * k, 15.5 * k, 17 * k, 17 * k);
                    lineTo(17 * k, 20 * k); closePath();
                }});
                disc(8.8, 11, 2); disc(15.2, 11, 2); line(10.5, 20, 10.5, 17.5); line(13.5, 20, 13.5, 17.5); break;
            case "wand":
                line(3, 21, 14, 10); line(12.5, 11.5, 14.5, 13.5 - 0.5);
                polyF(17, 2, 18, 5, 21, 6, 18, 7, 17, 10, 16, 7, 13, 6, 16, 5); break;
            case "totem":
                rr(6.5, 2.5, 11, 9, 3); disc(9.5, 7, 1); disc(14.5, 7, 1); poly(8, 11.5, 8, 15, 4, 13, 4, 16, 8, 18.5, 8, 21.5, 16, 21.5, 16, 18.5, 20, 16, 20, 13, 16, 15, 16, 11.5); break;
            case "circle-dot": case "jump":
                g.draw(new Ellipse2D.Double(2 * k, 13 * k, 20 * k, 8 * k)); poly(12, 3, 12, 11); poly(9, 6, 12, 3, 15, 6); break;
            case "trail":
                g.draw(new Path2D.Double() {{ moveTo(3 * k, 19 * k); curveTo(8 * k, 19 * k, 9 * k, 5 * k, 15 * k, 5 * k); }});
                disc(18, 5, 2.5); disc(9.5, 12.5, 0.9); disc(5.5, 17.5, 0.8); break;
            case "hit":
                polyC(12, 2, 14, 9, 21, 7, 16, 12, 21, 17, 14, 15, 12, 22, 10, 15, 3, 17, 8, 12, 3, 7, 10, 9); break;
            case "zoom":
                circ(10.5, 10.5, 6.5); line(15.5, 15.5, 21, 21); line(10.5, 7.5, 10.5, 13.5); line(7.5, 10.5, 13.5, 10.5); break;
            case "rain": case "weather":
                g.draw(new Path2D.Double() {{
                    moveTo(7 * k, 14 * k);
                    curveTo(4 * k, 14 * k, 2.5 * k, 12 * k, 3 * k, 10 * k);
                    curveTo(3.5 * k, 8 * k, 5.5 * k, 7.5 * k, 7 * k, 8 * k);
                    curveTo(7.5 * k, 5 * k, 10 * k, 3 * k, 13 * k, 3 * k);
                    curveTo(17 * k, 3 * k, 19.5 * k, 6 * k, 19 * k, 9 * k);
                    curveTo(21 * k, 9.5 * k, 21.5 * k, 12 * k, 20.5 * k, 13 * k);
                    curveTo(20 * k, 13.7 * k, 19 * k, 14 * k, 18 * k, 14 * k);
                    closePath();
                }});
                line(8, 17, 7, 20); line(12, 17, 11, 20); line(16, 17, 15, 20); break;
            case "box": case "outline":
                polyC(12, 2.5, 20.5, 7, 20.5, 17, 12, 21.5, 3.5, 17, 3.5, 7); poly(3.5, 7, 12, 11.5, 20.5, 7); line(12, 11.5, 12, 21.5); break;
            case "number": case "damage":
                line(9, 3, 7, 21); line(17, 3, 15, 21); line(4, 9, 21, 9); line(3, 15, 20, 15); break;
            case "film": case "cinema":
                rr(3, 3, 18, 18, 2); line(7, 3, 7, 21); line(17, 3, 17, 21); line(3, 12, 21, 12); line(3, 7.5, 7, 7.5); line(3, 16.5, 7, 16.5); line(17, 7.5, 21, 7.5); line(17, 16.5, 21, 16.5); break;
            case "vignette":
                rr(2.5, 4.5, 19, 15, 3); g.draw(new Ellipse2D.Double(6 * k, 7.5 * k, 12 * k, 9 * k)); break;
            case "ring": case "rings":
                g.draw(new Ellipse2D.Double(3 * k, 14 * k, 18 * k, 7 * k)); circ(12, 7, 3); line(12, 10, 12, 14); break;
            case "arrow": case "projectile":
                line(4, 20, 20, 4); poly(13, 4, 20, 4, 20, 11); poly(4, 16, 4, 20, 8, 20); break;
            case "firefly":
                disc(12, 12, 2.2); circ(12, 12, 5); disc(5, 6, 1); disc(19, 7, 1); disc(17, 19, 1); disc(6, 18, 0.8); break;
            case "tilt":
                rr(4, 6, 16, 12, 2); arc(12, 12, 10.5, 30, 40); arc(12, 12, 10.5, 210, 40); break;
            case "hitmarker":
                line(4, 4, 9, 9); line(20, 4, 15, 9); line(4, 20, 9, 15); line(20, 20, 15, 15); break;
            case "fps": case "boost":
                polyF(13, 2, 4, 14, 11, 14, 10, 22, 20, 9, 13, 9); break;
            case "entity":
                rr(6, 3, 12, 10, 2); rr(8, 13, 8, 8, 1.5); disc(10, 8, 1); disc(14, 8, 1); break;
            case "particles":
                disc(6, 6, 1.5); disc(12, 4, 1); disc(18, 7, 1.8); disc(8, 13, 1.2); disc(16, 14, 1); disc(11, 19, 1.8); disc(19, 19, 1.1); disc(4, 18, 0.9); break;
            case "pause-bell": case "break":
                circ(12, 13, 8); poly(12, 9, 12, 13, 15, 15); line(4, 4, 7, 2); line(20, 4, 17, 2); break;
            case "macro":
                rr(3, 6, 18, 12, 2.5); line(7, 10, 9, 12); line(7, 14, 9, 12); line(11, 14, 16, 14); break;
            case "clipboard": case "copy":
                rr(8, 8, 13, 13, 2); poly(16, 8, 16, 5, 13.5, 3, 5, 3, 3, 5, 3, 13.5, 5, 16, 8, 16); break;
            case "warning": case "alert":
                polyC(12, 3, 22, 20.5, 2, 20.5); line(12, 9.5, 12, 14.5); disc(12, 17.5, 1.1); break;
            case "armor":
                polyC(7, 3, 12, 5, 17, 3, 21, 6, 19, 10, 17, 9, 17, 21, 7, 21, 7, 9, 5, 10, 3, 6); break;
            case "respawn":
                arc(12, 12, 8.5, 90, 300); polyF(12, 1.5, 12, 7, 16.5, 4.2); break;
            case "gg":
                circ(12, 12, 9.5); arc(12, 13, 5, 200, 140); disc(9, 9.5, 1.1); disc(15, 9.5, 1.1); break;
            case "tv":
                rr(2.5, 4, 19, 13, 2); line(8, 21, 16, 21); line(12, 17, 12, 21); break;
            case "forward":
                poly(9, 5, 16, 12, 9, 19); break;
            case "home":
                polyC(3.5, 11, 12, 3.5, 20.5, 11, 20.5, 20.5, 14.5, 20.5, 14.5, 14, 9.5, 14, 9.5, 20.5, 3.5, 20.5); break;
            case "pip":
                rr(2.5, 4.5, 19, 15, 2); rrf(12, 12, 7.5, 5.5, 1); break;
            case "expand":
                poly(3, 9, 3, 3, 9, 3); poly(15, 3, 21, 3, 21, 9); poly(21, 15, 21, 21, 15, 21); poly(9, 21, 3, 21, 3, 15); break;
            case "shrink":
                poly(9, 3, 9, 9, 3, 9); poly(21, 9, 15, 9, 15, 3); poly(15, 21, 15, 15, 21, 15); poly(3, 15, 9, 15, 9, 21); break;
            case "bookmark":
                polyC(6, 3, 18, 3, 18, 21, 12, 16.5, 6, 21); break;
            case "light": case "ambilight":
                rr(5, 7, 14, 10, 1.5); line(2, 4, 4, 6); line(22, 4, 20, 6); line(2, 20, 4, 18); line(22, 20, 20, 18); line(12, 2, 12, 4.5); line(12, 22, 12, 19.5); break;
            case "mute":
                polyC(3, 9, 7, 9, 12, 4.5, 12, 19.5, 7, 15, 3, 15); line(15.5, 9, 21, 14.5); line(21, 9, 15.5, 14.5); break;
            case "sound": case "volume":
                polyC(3, 9, 7, 9, 12, 4.5, 12, 19.5, 7, 15, 3, 15); arc(12, 12, 5, -45, 90); arc(12, 12, 9, -50, 100); break;
            case "tag": case "profile":
                polyC(3, 3, 12, 3, 21, 12, 12, 21, 3, 12); disc(8, 8, 1.4); break;
            case "logo": {
                // 2x2 Lego brick in perspective
                polyF(3, 11, 12, 15.5, 21, 11, 21, 16, 12, 20.5, 3, 16);
                polyC(3, 11, 12, 6.5, 21, 11, 12, 15.5);
                break;
            }
            default:
                circ(12, 12, 8);
        }
        g = null;
    }

    private static GeneralPath heart() {
        GeneralPath h = new GeneralPath();
        h.moveTo(12 * k, 20.5 * k);
        h.curveTo(12 * k, 20.5 * k, 2.5 * k, 14.5 * k, 2.5 * k, 8.5 * k);
        h.curveTo(2.5 * k, 5.5 * k, 4.8 * k, 3.5 * k, 7.5 * k, 3.5 * k);
        h.curveTo(9.5 * k, 3.5 * k, 11 * k, 4.6 * k, 12 * k, 6.2 * k);
        h.curveTo(13 * k, 4.6 * k, 14.5 * k, 3.5 * k, 16.5 * k, 3.5 * k);
        h.curveTo(19.2 * k, 3.5 * k, 21.5 * k, 5.5 * k, 21.5 * k, 8.5 * k);
        h.curveTo(21.5 * k, 14.5 * k, 12 * k, 20.5 * k, 12 * k, 20.5 * k);
        h.closePath();
        return h;
    }
}
