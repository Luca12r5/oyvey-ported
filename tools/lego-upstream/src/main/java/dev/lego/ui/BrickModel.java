package dev.lego.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A real 3D 2x2 Lego brick (body + four studs as 16-sided cylinders), rotated, lit (Lambert + ambient) and
 * depth sorted, rendered with Java2D into sprites. Rendered white/grey so each brick can be tinted with its colour.
 */
public final class BrickModel {
    private BrickModel() { }

    public static final int YAWS = 24, PITCHES = 7, SIZE = 112;
    public static final double[] PITCH = {-60, -30, 0, 22, 40, 62, 85};
    private static volatile int[][] frames;
    private static volatile boolean started;

    private record Face(double[][] p, double depth, double light) { }

    /** Starts rendering all frames in the background (call early). */
    public static void prepare() {
        if (started) return;
        started = true;
        Thread t = new Thread(() -> {
            int[][] f = new int[YAWS * PITCHES][];
            for (int pi = 0; pi < PITCHES; pi++) for (int yi = 0; yi < YAWS; yi++) f[pi * YAWS + yi] = render(SIZE, yi * 360.0 / YAWS, PITCH[pi]);
            frames = f;
        }, "Lego-Intro-Bricks");
        t.setDaemon(true);
        t.start();
    }

    public static boolean ready() { return frames != null; }

    private static final Gx.Img[] IMG = new Gx.Img[YAWS * PITCHES];
    private static int uploaded = 0;

    /** Uploads up to n sprites to the GPU (render thread); true when all are uploaded. Avoids hitches mid-animation. */
    public static boolean upload(int n) {
        if (frames == null) return false;
        while (n-- > 0 && uploaded < IMG.length) { final int idx = uploaded; IMG[idx] = Gx.get("brick3d:" + idx, 0, () -> new int[][]{frames[idx], {SIZE, SIZE}}); uploaded++; }
        return uploaded >= IMG.length;
    }

    /** Texture for a rotation (degrees). */
    public static Gx.Img sprite(double yaw, double pitch) {
        int yi = (int) Math.floorMod(Math.round(yaw / (360.0 / YAWS)), YAWS);
        int pi = 0;
        double best = 1e9;
        for (int i = 0; i < PITCHES; i++) { double d = Math.abs(PITCH[i] - pitch); if (d < best) { best = d; pi = i; } }
        final int idx = pi * YAWS + yi;
        Gx.Img img = IMG[idx];
        if (img == null) img = IMG[idx] = Gx.get("brick3d:" + idx, 0, () -> new int[][]{frames[idx], {SIZE, SIZE}});
        img.used = System.currentTimeMillis();
        return img;
    }

    static int[] render(int size, double yawDeg, double pitchDeg) {
        List<Face> faces = new ArrayList<>();
        double yaw = Math.toRadians(yawDeg), pitch = Math.toRadians(pitchDeg);
        box(faces, -1, 0, -1, 1, 1.2, 1, yaw, pitch);
        for (int sx = -1; sx <= 1; sx += 2) for (int sz = -1; sz <= 1; sz += 2) stud(faces, sx * 0.5, sz * 0.5, yaw, pitch);
        faces.sort((a, b) -> Double.compare(a.depth, b.depth));
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double sc = size / 4.6, cx = size / 2.0, cy = size / 2.0;
        for (Face f : faces) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < f.p.length; i++) {
                double x = cx + f.p[i][0] * sc, y = cy - f.p[i][1] * sc;
                if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            path.closePath();
            int v = (int) Math.max(0, Math.min(255, 70 + 185 * f.light));
            g.setColor(new Color(v, v, v));
            g.fill(path);
            g.setColor(new Color(0, 0, 0, 38));
            g.setStroke(new java.awt.BasicStroke((float) Math.max(0.6, size / 140.0)));
            g.draw(path);
        }
        g.dispose();
        return img.getRGB(0, 0, size, size, null, 0, size);
    }

    private static final double[] LIGHT = norm(-0.45, 0.85, 0.55);

    private static double[] norm(double x, double y, double z) { double l = Math.sqrt(x * x + y * y + z * z); return new double[]{x / l, y / l, z / l}; }

    /** Rotate around Y (yaw), then tilt towards the camera around X (pitch). Camera looks along -z. */
    private static double[] rot(double x, double y, double z, double yaw, double pitch) {
        double x1 = x * Math.cos(yaw) + z * Math.sin(yaw), z1 = -x * Math.sin(yaw) + z * Math.cos(yaw);
        double y0 = y - 0.6;    // rotate around the brick centre
        double y2 = y0 * Math.cos(pitch) - z1 * Math.sin(pitch), z2 = y0 * Math.sin(pitch) + z1 * Math.cos(pitch);
        double persp = 1 + z2 * 0.06;
        return new double[]{x1 * persp, y2 * persp, z2};
    }

    private static void add(List<Face> faces, double[][] local, double[] n, double yaw, double pitch) {
        double[][] p = new double[local.length][];
        double depth = 0;
        for (int i = 0; i < local.length; i++) { p[i] = rot(local[i][0], local[i][1], local[i][2], yaw, pitch); depth += p[i][2]; }
        double[] rn = rot(n[0], n[1] + 0.6, n[2], yaw, pitch);    // rot() shifts y by 0.6, undo for the normal
        if (rn[2] < -0.02) return;                                // back face
        double lambert = Math.max(0, rn[0] * LIGHT[0] + rn[1] * LIGHT[1] + rn[2] * LIGHT[2]);
        faces.add(new Face(p, depth / local.length, 0.32 + 0.68 * lambert));
    }

    private static void box(List<Face> f, double x0, double y0, double z0, double x1, double y1, double z1, double yaw, double pitch) {
        add(f, new double[][]{{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}}, new double[]{0, 1, 0}, yaw, pitch);
        add(f, new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y0, z0}, {x0, y0, z0}}, new double[]{0, -1, 0}, yaw, pitch);
        add(f, new double[][]{{x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}}, new double[]{0, 0, 1}, yaw, pitch);
        add(f, new double[][]{{x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}, {x1, y1, z0}}, new double[]{0, 0, -1}, yaw, pitch);
        add(f, new double[][]{{x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}, {x1, y1, z1}}, new double[]{1, 0, 0}, yaw, pitch);
        add(f, new double[][]{{x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}, {x0, y1, z0}}, new double[]{-1, 0, 0}, yaw, pitch);
    }

    private static void stud(List<Face> f, double cx, double cz, double yaw, double pitch) {
        int n = 16;
        double r = 0.31, y0 = 1.2, y1 = 1.42;
        double[][] cap = new double[n][];
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            double xa = cx + Math.cos(a0) * r, za = cz + Math.sin(a0) * r, xb = cx + Math.cos(a1) * r, zb = cz + Math.sin(a1) * r;
            double am = (a0 + a1) / 2;
            add(f, new double[][]{{xa, y0, za}, {xb, y0, zb}, {xb, y1, zb}, {xa, y1, za}}, new double[]{Math.cos(am), 0, Math.sin(am)}, yaw, pitch);
            cap[i] = new double[]{xa, y1, za};
        }
        add(f, cap, new double[]{0, 1, 0}, yaw, pitch);
    }
}