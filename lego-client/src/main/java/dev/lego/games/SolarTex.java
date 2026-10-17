package dev.lego.games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.IntStream;

/**
 * Solar Smash: procedural noise, planet surface maps (generated off the render thread) and all sprites.
 * Everything here is painted exactly once and cached; nothing in this class runs per frame.
 */
final class SolarTex {
    private SolarTex() { }

    // ================================================================== worker pool
    static final ForkJoinPool POOL = new ForkJoinPool(Math.max(1, Math.min(3, Runtime.getRuntime().availableProcessors() - 1)), p -> {
        ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
        t.setDaemon(true);
        t.setName("solar-gen-" + t.getPoolIndex());
        t.setPriority(Thread.MIN_PRIORITY + 1);
        return t;
    }, null, false);

    static void rows(int h, java.util.function.IntConsumer row) {
        try { POOL.submit(() -> IntStream.range(0, h).parallel().forEach(row)).get(); }
        catch (Exception e) { for (int y = 0; y < h; y++) row.accept(y); }
    }

    // ================================================================== noise
    private static final int[] PERM = new int[512];
    static final byte[] HASH = new byte[65536];
    static {
        Random r = new Random(1337);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 255; i > 0; i--) { int j = r.nextInt(i + 1); int t = p[i]; p[i] = p[j]; p[j] = t; }
        for (int i = 0; i < 512; i++) PERM[i] = p[i & 255];
        r.nextBytes(HASH);
    }

    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double grad(int h, double x, double y, double z) {
        int hh = h & 15;
        double u = hh < 8 ? x : y, v = hh < 4 ? y : (hh == 12 || hh == 14 ? x : z);
        return ((hh & 1) == 0 ? u : -u) + ((hh & 2) == 0 ? v : -v);
    }

    /** Improved Perlin noise, roughly -1..1. */
    static double noise(double x, double y, double z) {
        int X = (int) Math.floor(x), Y = (int) Math.floor(y), Z = (int) Math.floor(z);
        x -= X; y -= Y; z -= Z;
        X &= 255; Y &= 255; Z &= 255;
        double u = fade(x), v = fade(y), w = fade(z);
        int A = PERM[X] + Y, AA = PERM[A] + Z, AB = PERM[A + 1] + Z, B = PERM[X + 1] + Y, BA = PERM[B] + Z, BB = PERM[B + 1] + Z;
        return lerp(w, lerp(v, lerp(u, grad(PERM[AA], x, y, z), grad(PERM[BA], x - 1, y, z)),
                        lerp(u, grad(PERM[AB], x, y - 1, z), grad(PERM[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad(PERM[AA + 1], x, y, z - 1), grad(PERM[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(PERM[AB + 1], x, y - 1, z - 1), grad(PERM[BB + 1], x - 1, y - 1, z - 1))));
    }

    static double fbm(double x, double y, double z, int oct) {
        double s = 0, a = 0.5, f = 1;
        for (int i = 0; i < oct; i++) { s += a * noise(x * f, y * f, z * f); a *= 0.5; f *= 2.03; }
        return s;
    }

    /** Ridged multifractal, 0..~1 (sharp ridges = high values). */
    static double ridge(double x, double y, double z, int oct) {
        double s = 0, a = 0.5, f = 1, w = 1;
        for (int i = 0; i < oct; i++) {
            double n = 1 - Math.abs(noise(x * f, y * f, z * f));
            n *= n;
            n *= w;
            w = Math.min(1, n * 2);
            s += a * n;
            a *= 0.5; f *= 2.07;
        }
        return s;
    }

    static double sstep(double a, double b, double x) { double t = Math.max(0, Math.min(1, (x - a) / (b - a))); return t * t * (3 - 2 * t); }
    static double clamp01(double x) { return x < 0 ? 0 : (x > 1 ? 1 : x); }

    static int mix(int a, int b, double t) {
        if (t <= 0) return a;
        if (t >= 1) return (a & 0xFF000000) | (b & 0xFFFFFF);
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255, br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return (a & 0xFF000000) | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    static int shade(int c, double k) {
        int r = (int) Math.min(255, ((c >> 16) & 255) * k), g = (int) Math.min(255, ((c >> 8) & 255) * k), b = (int) Math.min(255, (c & 255) * k);
        return (c & 0xFF000000) | (Math.max(0, r) << 16) | (Math.max(0, g) << 8) | Math.max(0, b);
    }

    /** Colour ramp: stops = {pos0, col0, pos1, col1, ...} with positions 0..1. */
    static int ramp(double t, double[] pos, int[] col) {
        if (t <= pos[0]) return col[0];
        for (int i = 1; i < pos.length; i++) if (t <= pos[i]) return mix(col[i - 1], col[i], (t - pos[i - 1]) / (pos[i] - pos[i - 1]));
        return col[col.length - 1];
    }

    // ================================================================== planet maps
    /** Generates all surface maps of a body (runs on the worker pool). */
    static SolarPlanet.Maps generate(int type, long seed, int W, int H, boolean inhabited, boolean full) {
        SolarPlanet.Maps m = new SolarPlanet.Maps(W, H, type != SolarPlanet.GAS && type != SolarPlanet.SUN && type != SolarPlanet.MOON, type == SolarPlanet.LAVA);
        final double ox = (seed % 1000) * 0.137, oy = ((seed / 1000) % 1000) * 0.071, oz = ((seed / 7) % 997) * 0.113;
        float[] hs = new float[W * H], ms = new float[W * H], ds = new float[W * H];
        double[] cl = new double[W], sl = new double[W];
        for (int x = 0; x < W; x++) { double lon = (x + 0.5) / W * Math.PI * 2; cl[x] = Math.cos(lon); sl[x] = Math.sin(lon); }
        final int oct = W >= 1024 ? 8 : W >= 512 ? 6 : 5;
        // pass 1: height, moisture, detail
        rows(H, y -> {
            double lat = (0.5 - (y + 0.5) / H) * Math.PI, cy = Math.cos(lat), py = Math.sin(lat);
            for (int x = 0; x < W; x++) {
                double px = cy * cl[x], pz = cy * sl[x];
                int i = y * W + x;
                double h, mo = 0, de;
                switch (type) {
                    case SolarPlanet.TERRA: case SolarPlanet.OCEAN: {
                        double w = fbm(px * 1.1 + 5.3 + ox, py * 1.1 + 1.7, pz * 1.1 + 9.2 + oz, 3) * 0.6;
                        h = fbm(px * 1.35 + w + ox, py * 1.35 - w * 0.5 + oy, pz * 1.35 + w * 0.8 + oz, oct);
                        double rr = ridge(px * 3.1 + oy, py * 3.1, pz * 3.1 + ox, 5);
                        h += Math.max(0, h) * rr * 0.45;
                        mo = fbm(px * 2.3 + 11 + oz, py * 2.3, pz * 2.3 - 4, 4);
                        de = fbm(px * 11 + 3, py * 11, pz * 11 + 8 + ox, 3);
                        break;
                    }
                    case SolarPlanet.MARS: {
                        double w = fbm(px * 1.4 + ox, py * 1.4 + 3, pz * 1.4, 3) * 0.5;
                        h = fbm(px * 1.6 + w + oy, py * 1.6, pz * 1.6 - w, oct);
                        double can = ridge(px * 2.4 + 7, py * 2.4, pz * 2.4 + 1, 5);
                        h -= Math.pow(can, 3) * 0.35;
                        mo = fbm(px * 2.8 + 20, py * 2.8, pz * 2.8 + ox, 4);
                        de = fbm(px * 14, py * 14 + 5, pz * 14, 3);
                        break;
                    }
                    case SolarPlanet.MOON: {
                        h = fbm(px * 1.8 + ox, py * 1.8 + oy, pz * 1.8, oct) * 0.6;
                        mo = fbm(px * 1.2 + 40 + oz, py * 1.2, pz * 1.2, 4);
                        de = fbm(px * 16, py * 16, pz * 16 + 9, 3);
                        break;
                    }
                    case SolarPlanet.ICE: {
                        h = fbm(px * 1.5 + ox, py * 1.5, pz * 1.5 + oz, oct) * 0.7;
                        mo = ridge(px * 3.5 + 2, py * 3.5 + oy, pz * 3.5, 6);
                        de = fbm(px * 12, py * 12, pz * 12, 3);
                        break;
                    }
                    case SolarPlanet.LAVA: {
                        h = fbm(px * 1.7 + ox, py * 1.7, pz * 1.7 + oz, oct);
                        mo = ridge(px * 2.6 + 3 + oy, py * 2.6, pz * 2.6 - 2, 6);
                        de = fbm(px * 13, py * 13 + 2, pz * 13, 3);
                        break;
                    }
                    case SolarPlanet.DESERT: {
                        double w = fbm(px * 1.2 + ox, py * 1.2, pz * 1.2 + 2, 3) * 0.8;
                        h = fbm(px * 1.5 + w, py * 1.5 + oy, pz * 1.5 - w, oct) * 0.8;
                        double dune = ridge(px * 7 + w * 3, py * 16 + w * 2, pz * 7 - w * 3, 3);
                        mo = dune;
                        h -= Math.pow(ridge(px * 2.1 + 9, py * 2.1, pz * 2.1, 5), 4) * 0.4;
                        de = fbm(px * 15, py * 15, pz * 15 + 1, 3);
                        break;
                    }
                    case SolarPlanet.GAS: {
                        double w = fbm(px * 2.2 + ox, py * 2.2, pz * 2.2, 4);
                        h = py * 5.5 + w * 1.1 + fbm(px * 3, py * 18, pz * 3, 4) * 0.5;
                        mo = fbm(px * 5 + w, py * 30, pz * 5 - w, 4);
                        de = fbm(px * 9, py * 9 + 4, pz * 9, 3);
                        break;
                    }
                    default: { // SUN
                        double g = Math.abs(noise(px * 38 + ox, py * 38, pz * 38)) + 0.5 * Math.abs(noise(px * 80, py * 80, pz * 80 + 3));
                        h = g;
                        mo = fbm(px * 3 + ox, py * 3, pz * 3, 4);
                        de = fbm(px * 9, py * 9, pz * 9 + 5, 3);
                    }
                }
                hs[i] = (float) h; ms[i] = (float) mo; ds[i] = (float) de;
            }
        });
        // natural craters (moons, mars, ice)
        if (type == SolarPlanet.MOON || type == SolarPlanet.MARS || type == SolarPlanet.ICE) {
            Random r = new Random(seed * 31 + 7);
            int n = type == SolarPlanet.MOON ? 900 : type == SolarPlanet.MARS ? 260 : 90;
            for (int k = 0; k < n; k++) {
                double lat = Math.asin(r.nextDouble() * 2 - 1), lon = r.nextDouble() * Math.PI * 2;
                double rad = W / 2048.0 * (3 + Math.pow(r.nextDouble(), 5) * (type == SolarPlanet.MOON ? 90 : 55));
                stampCrater(hs, W, H, lon, lat, rad, 0.25 + r.nextDouble() * 0.2);
            }
        }
        // pass 2: colours + hillshade
        final double kShade = W / 2048.0;
        rows(H, y -> {
            double lat = (0.5 - (y + 0.5) / H) * Math.PI, alat = Math.abs(lat), cyl = Math.cos(lat), py = Math.sin(lat);
            int ym = Math.max(0, y - 1), yp = Math.min(H - 1, y + 1);
            for (int x = 0; x < W; x++) {
                int i = y * W + x;
                double px = cyl * cl[x], pz = cyl * sl[x];
                double h = hs[i], mo = ms[i], de = ds[i];
                double gx = hs[y * W + ((x + 1) & (W - 1))] - hs[y * W + ((x - 1) & (W - 1))];
                double gy = hs[yp * W + x] - hs[ym * W + x];
                double sh = 1 + (-gx * 0.8 + gy * 0.6) * 9 / Math.max(0.25, kShade * 2.2);
                int c; int spec = 0, city = 0, heat0 = 0;
                double hb = h;
                switch (type) {
                    case SolarPlanet.TERRA: {
                        double sea = 0.02;
                        boolean ice = alat > 1.22 - de * 0.25 + Math.max(0, h) * 0.3;
                        if (h < sea) {
                            double d = sea - h;
                            c = ramp(d, new double[]{0, 0.025, 0.12, 0.35}, new int[]{0xFF3AA7B8, 0xFF1C74A6, 0xFF0F3F7E, 0xFF081E4B});
                            c = mix(c, 0xFF0A2A5A, clamp01(de * 0.4 + 0.1));
                            spec = 230;
                            sh = 1;
                            if (ice) { c = mix(0xFFDDE8F0, 0xFFB9CCDC, clamp01(de + 0.5)); spec = 60; }
                        } else {
                            double e = h - sea;
                            double temp = 1 - alat / 1.45 - e * 1.4;
                            double wet = mo + 0.25 - Math.abs(Math.abs(lat) - 0.42) * 0.9 + (alat < 0.2 ? 0.25 : 0);
                            int green = mix(0xFF4E8A34, 0xFF1F4A1A, clamp01(wet * 1.6 + 0.35 + de * 0.4));
                            int dry = mix(0xFFC9A76A, 0xFFA7824C, clamp01(de + 0.5));
                            c = mix(dry, green, sstep(-0.05, 0.2, wet));
                            if (temp < 0.35) c = mix(c, 0xFF7C7A5E, sstep(0.35, 0.18, temp));
                            if (e < 0.012) c = mix(c, 0xFFD9C79A, 0.6);
                            c = mix(c, mix(0xFF6B5D4E, 0xFF94897C, clamp01(de + 0.5)), sstep(0.2, 0.34, e));
                            c = mix(c, 0xFFF4F6F8, sstep(0.36, 0.45, e + (1 - temp) * 0.1));
                            if (ice) c = mix(0xFFE9F0F5, 0xFFCAD8E4, clamp01(de + 0.5));
                            if (inhabited && !ice && temp > 0.3 && e < 0.26) {
                                double cn = fbm(px * 7 + 3, py * 7, pz * 7 + 1.7, 3) + 0.5 * noise(px * 30, py * 30, pz * 30 + 4.2);
                                double v = cn + (e < 0.04 ? 0.22 : 0) + wet * 0.15 - 0.18;
                                if (v > 0) city = (int) Math.min(255, Math.pow(v * 3.2, 1.6) * 255);
                            }
                        }
                        hb = h;
                        break;
                    }
                    case SolarPlanet.OCEAN: {
                        double sea = 0.33;
                        if (h < sea) {
                            double d = sea - h;
                            c = ramp(d, new double[]{0, 0.03, 0.18, 0.6}, new int[]{0xFF45C9D0, 0xFF1C8DB9, 0xFF0E4E96, 0xFF06225E});
                            c = mix(c, 0xFF0B3F7A, clamp01(mo * 0.8 + 0.3));
                            spec = 240;
                            sh = 1;
                            if (inhabited) {
                                double cn = noise(px * 16, py * 16, pz * 16 + 8.1) + noise(px * 56, py * 56, pz * 56 + 2.2) * 0.5;
                                if (cn > 0.42 && alat < 1.0) city = (int) Math.min(255, (cn - 0.42) * 900);
                            }
                        } else {
                            double e = h - sea;
                            c = e < 0.015 ? 0xFFE6D7A6 : mix(0xFF3E8C3A, 0xFF2A5E26, clamp01(de + 0.5));
                            if (inhabited) city = (int) Math.min(255, 120 + de * 200);
                        }
                        if (alat > 1.3 - de * 0.2) { c = mix(0xFFE8F1F6, 0xFFC4D6E3, clamp01(de + 0.5)); spec = 40; }
                        break;
                    }
                    case SolarPlanet.MARS: {
                        c = ramp(clamp01(h * 1.2 + 0.5), new double[]{0, 0.35, 0.6, 1}, new int[]{0xFF5A2616, 0xFF9A4526, 0xFFC4683A, 0xFFDC9460});
                        c = mix(c, 0xFF3C2A24, sstep(0.1, 0.35, mo) * 0.7);
                        c = mix(c, 0xFFE0A070, clamp01(de * 0.6) * 0.3);
                        if (alat > 1.32 - de * 0.2) c = mix(0xFFF2EEEA, 0xFFD8CFC6, clamp01(de + 0.5));
                        if (inhabited && alat < 0.6) {
                            double cn = noise(px * 10 + 5, py * 10, pz * 10 + 0.3);
                            double d2 = noise(px * 46, py * 46, pz * 46 + 3.3);
                            if (cn > 0.5 && d2 > 0.05) city = (int) Math.min(255, (cn - 0.5) * 1400 * (d2 + 0.3));
                        }
                        break;
                    }
                    case SolarPlanet.MOON: {
                        c = mix(0xFF9C9A95, 0xFFBDBAB3, clamp01(h + 0.5));
                        c = mix(c, 0xFF4E4D52, sstep(0.05, 0.3, mo) * 0.85);
                        c = shade(c, 0.92 + de * 0.25);
                        if (inhabited) {
                            double cn = noise(px * 13 + 1, py * 13, pz * 13 + 7.3);
                            if (cn > 0.62 && alat < 1.0) city = (int) Math.min(255, (cn - 0.62) * 1500);
                        }
                        break;
                    }
                    case SolarPlanet.ICE: {
                        c = mix(0xFFEAF3FA, 0xFFA9C8E0, clamp01(h * 1.3 + 0.5));
                        c = mix(c, 0xFF2F6597, sstep(0.72, 0.95, mo) * 0.85);
                        c = mix(c, 0xFFFFFFFF, clamp01(de * 0.5) * 0.4);
                        spec = 70;
                        if (inhabited) {
                            double cn = noise(px * 16 + 9, py * 16, pz * 16 + 1.1);
                            if (cn > 0.6 && alat < 1.1) city = (int) Math.min(255, (cn - 0.6) * 1300);
                        }
                        break;
                    }
                    case SolarPlanet.LAVA: {
                        c = mix(0xFF231815, 0xFF4A3A33, clamp01(h + 0.5));
                        c = shade(c, 0.85 + de * 0.4);
                        double crack = sstep(0.8, 0.98, mo);
                        double lake = sstep(-0.12, -0.3, h);
                        double hot = Math.max(crack, lake);
                        if (hot > 0.02) { c = mix(c, 0xFF7A1E08, hot); heat0 = (int) (hot * (170 + de * 80)); }
                        break;
                    }
                    case SolarPlanet.DESERT: {
                        c = ramp(clamp01(h + 0.55), new double[]{0, 0.35, 0.65, 1}, new int[]{0xFF7A4526, 0xFFB67A42, 0xFFDDB36E, 0xFFEFD199});
                        c = shade(c, 0.86 + mo * 0.28);
                        boolean oasis = h < -0.22 && de > 0.0;
                        if (oasis) c = mix(c, 0xFF4F7E38, 0.7);
                        if (alat > 1.38) c = mix(c, 0xFFF1E9DF, 0.8);
                        if (inhabited && alat < 1.0) {
                            double cn = noise(px * 8 + 2, py * 8, pz * 8 + 6.6) + (h < -0.1 ? 0.3 : 0);
                            if (cn > 0.35) city = (int) Math.min(255, (cn - 0.35) * 700);
                        }
                        break;
                    }
                    case SolarPlanet.GAS: {
                        double t = h;
                        double b = Math.sin(t * 2.2) * 0.5 + 0.5, b2 = Math.sin(t * 5.3 + 1) * 0.5 + 0.5;
                        c = mix(0xFFE9D8B8, 0xFFB98556, b);
                        c = mix(c, 0xFF8A5A3A, b2 * 0.35);
                        c = mix(c, 0xFFF5EEDD, clamp01(mo * 1.2) * 0.4);
                        c = shade(c, 0.94 + de * 0.12);
                        // great storm
                        double lon = (x + 0.5) / W * Math.PI * 2;
                        double dx = Math.atan2(Math.sin(lon - 1.9), Math.cos(lon - 1.9)) / 0.33, dy = (lat + 0.38) / 0.12;
                        double dd = Math.sqrt(dx * dx + dy * dy);
                        if (dd < 1.6) {
                            double sw = Math.sin(dd * 9 + Math.atan2(dy, dx) * 2) * 0.5 + 0.5;
                            c = mix(c, mix(0xFFC2542C, 0xFFE8A070, sw * 0.5 + dd * 0.3), sstep(1.6, 0.6, dd));
                        }
                        sh = 1;
                        break;
                    }
                    default: { // SUN
                        c = ramp(clamp01(h * 0.9), new double[]{0, 0.3, 0.6, 1}, new int[]{0xFFB83A0C, 0xFFF0801E, 0xFFFFC04A, 0xFFFFF0B8});
                        c = mix(c, 0xFFFFF6D0, clamp01(de * 0.8) * 0.4);
                        double spot = sstep(0.36, 0.46, mo);
                        if (spot > 0) c = mix(c, 0xFF3A1206, spot * 0.85);
                        sh = 1;
                    }
                }
                if (type != SolarPlanet.GAS && type != SolarPlanet.SUN) c = shade(c, Math.max(0.55, Math.min(1.4, sh)));
                m.alb[i] = (spec << 24) | (c & 0xFFFFFF);
                if (m.alb[i] >>> 24 == 1) m.alb[i] = (2 << 24) | (c & 0xFFFFFF);
                m.hgt[i] = (byte) Math.max(0, Math.min(255, (int) ((hb + 1) * 127.5)));
                m.city[i] = (byte) city;
                if (m.heat0 != null) { m.heat0[i] = (byte) heat0; m.heat[i] = (byte) heat0; }
            }
        });
        if (type == SolarPlanet.TERRA) m.sea = (byte) (int) ((0.02 + 1) * 127.5);
        else if (type == SolarPlanet.OCEAN) m.sea = (byte) (int) ((0.33 + 1) * 127.5);
        else m.sea = 0;
        // clouds
        if (m.cloud != null) {
            final int cW = m.cW, cH = m.cH;
            double cover = type == SolarPlanet.OCEAN ? 0.1 : type == SolarPlanet.TERRA ? 0.02 : type == SolarPlanet.DESERT ? -0.28 : type == SolarPlanet.MARS ? -0.2 : type == SolarPlanet.LAVA ? -0.05 : -0.12;
            rows(cH, y -> {
                double lat = (0.5 - (y + 0.5) / cH) * Math.PI, cy = Math.cos(lat), py = Math.sin(lat);
                for (int x = 0; x < cW; x++) {
                    double lon = (x + 0.5) / cW * Math.PI * 2, px = cy * Math.cos(lon), pz = cy * Math.sin(lon);
                    double w = fbm(px * 1.7 + 2 + oz, py * 2.4 + 7, pz * 1.7 + 1, 3) * 1.1;
                    double v = fbm(px * 2.6 + w, py * 4.2 - w * 0.4, pz * 2.6 - w, W >= 1024 ? 7 : 5);
                    v += Math.cos(lat * 6) * 0.06 + cover;
                    double a = sstep(0.0, 0.34, v);
                    m.cloud[y * cW + x] = (byte) (int) (a * 235);
                }
            });
        }
        m.finish();
        return m;
    }

    private static void stampCrater(float[] hs, int W, int H, double lon, double lat, double rad, double depth) {
        int cy = (int) ((0.5 - lat / Math.PI) * H);
        double cx = lon / (Math.PI * 2) * W;
        int ry = (int) Math.ceil(rad * 1.6);
        double dh = depth * Math.min(1, rad / (W / 2048.0 * 30) + 0.25) * 0.5;
        for (int y = Math.max(0, cy - ry); y <= Math.min(H - 1, cy + ry); y++) {
            double la = (0.5 - (y + 0.5) / H) * Math.PI, cl = Math.max(0.05, Math.cos(la));
            double dy = y - cy;
            int rx = (int) Math.min(W / 2, Math.ceil(ry / cl));
            for (int xx = (int) cx - rx; xx <= (int) cx + rx; xx++) {
                double dx = (xx - cx) * cl;
                double d = Math.sqrt(dx * dx + dy * dy) / rad;
                if (d > 1.6) continue;
                int i = y * W + (xx & (W - 1));
                double v = d < 1 ? -(1 - d * d) * dh + 0.35 * dh : 0.35 * dh * Math.exp(-(d - 1) * (d - 1) / 0.04);
                if (d < 1) v = Math.min(v, 0.35 * dh * Math.exp(-(d - 1) * (d - 1) / 0.04) - (1 - d * d) * dh);
                hs[i] += (float) v;
            }
        }
    }

    // ================================================================== background
    /** Deep space nebula, opaque. */
    static int[][] nebula(int w, int h, long seed) {
        int[] px = new int[w * h];
        final double s = 2.6 / h, o = (seed % 97) * 1.37;
        rows(h, y -> {
            for (int x = 0; x < w; x++) {
                double fx = x * s + o, fy = y * s;
                double wx = fbm(fx * 0.7, fy * 0.7, 3.3, 3) * 1.3;
                double n1 = fbm(fx + wx, fy - wx * 0.5, 0.5, 6);
                double n2 = fbm(fx * 1.6 - 4 + wx, fy * 1.6 + 2, 7.1, 5);
                double n3 = fbm(fx * 3.1 + 9, fy * 3.1 + wx, 1.9, 4);
                double band = Math.exp(-Math.pow((y / (double) h - 0.42 - (x / (double) w - 0.5) * 0.35 + n1 * 0.2) / 0.28, 2));
                double a = sstep(-0.05, 0.55, n1 + band * 0.35) * (0.45 + band * 0.55);
                double b = sstep(0.0, 0.5, n2) * 0.8;
                int c = 0xFF03040A;
                c = mix(c, 0xFF14113A, 0.8);
                c = mix(c, 0xFF2A1655, a * 0.75);
                c = mix(c, 0xFF0E4A6E, b * a * 0.8);
                c = mix(c, 0xFF8A2E6B, sstep(0.25, 0.7, n2 + n1 * 0.5) * a * 0.55);
                c = mix(c, 0xFFE0A0B8, sstep(0.45, 0.85, n1 + n3 * 0.3) * a * 0.22);
                double dust = sstep(0.1, 0.45, n3 - n1 * 0.4) * band;
                c = mix(c, 0xFF020205, dust * 0.65);
                double vig = Math.pow(Math.hypot(x / (double) w - 0.5, (y / (double) h - 0.5) * 1.2), 2) * 1.2;
                c = shade(c, 1 - Math.min(0.6, vig));
                int hsh = HASH[(x * 31 + y * 131) & 65535] & 7;
                px[y * w + x] = (c & 0xFFFFFF) + (hsh <= 1 ? 0x010101 * hsh : 0) | 0xFF000000;
            }
        });
        return new int[][]{px, {w, h}};
    }

    /** Transparent tile with small stars. */
    static int[][] stars(int size, int count, float bright, long seed) {
        int[] px = new int[size * size];
        Random r = new Random(seed);
        for (int k = 0; k < count; k++) {
            float x = r.nextFloat() * size, y = r.nextFloat() * size;
            float rad = 0.45f + (float) Math.pow(r.nextFloat(), 6) * 1.5f;
            float a = bright * (0.25f + r.nextFloat() * 0.75f);
            int col = r.nextFloat() < 0.15f ? 0xFFD9B8 : r.nextFloat() < 0.3f ? 0xB8CCFF : 0xFFFFFF;
            int R = (int) Math.ceil(rad * 2.5f);
            for (int yy = -R; yy <= R; yy++)
                for (int xx = -R; xx <= R; xx++) {
                    int X = ((int) x + xx) & (size - 1), Y = ((int) y + yy) & (size - 1);
                    double dx = (int) x + xx + 0.5 - x, dy = (int) y + yy + 0.5 - y;
                    double v = a * Math.exp(-(dx * dx + dy * dy) / (rad * rad * 0.9));
                    if (v < 0.01) continue;
                    int i = Y * size + X;
                    int old = px[i] >>> 24;
                    int na = (int) Math.min(255, old + v * 255);
                    px[i] = (na << 24) | col;
                }
        }
        return new int[][]{px, {size, size}};
    }

    /** Four-pointed flare with a soft core (white). */
    static int[][] flare(int d) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / c, dy = (y + 0.5 - c) / c;
                double r = Math.sqrt(dx * dx + dy * dy);
                double v = Math.pow(Math.max(0, 1 - r), 3) * 0.9;
                v += Math.exp(-Math.abs(dy) * 60) * Math.pow(Math.max(0, 1 - Math.abs(dx)), 2) * 0.8;
                v += Math.exp(-Math.abs(dx) * 60) * Math.pow(Math.max(0, 1 - Math.abs(dy)), 2) * 0.8;
                v += Math.exp(-r * r * 90) * 0.8;
                px[y * d + x] = ((int) (Math.min(1, v) * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    /** Sun corona with streaks (white, tinted when drawn). */
    static int[][] corona(int d, long seed) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        rows(d, y -> {
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / c, dy = (y + 0.5 - c) / c;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r >= 1) continue;
                double ang = Math.atan2(dy, dx);
                double st = fbm(Math.cos(ang) * 3 + seed, Math.sin(ang) * 3, r * 1.5, 4);
                double st2 = Math.abs(noise(Math.cos(ang) * 14, Math.sin(ang) * 14, seed * 0.3));
                double base = Math.pow(1 - r, 2.6);
                double v = base * (0.55 + 0.45 * sstep(-0.2, 0.4, st)) + Math.pow(1 - r, 5) * 0.4;
                v *= 0.7 + 0.5 * st2;
                px[y * d + x] = ((int) (Math.min(1, v) * 255) << 24) | 0xFFFFFF;
            }
        });
        return new int[][]{px, {d, d}};
    }

    /** Thin soft ellipse line (drawn stretched as an orbit). */
    static int[][] orbitRing(int d, float thick) {
        int[] px = new int[d * d];
        double c = d / 2.0, R = c - thick * 2;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double r = Math.hypot(x + 0.5 - c, y + 0.5 - c);
                double v = Math.exp(-Math.pow((r - R) / thick, 2));
                if (v > 0.003) px[y * d + x] = ((int) (v * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    /** Glow ring (white): soft band at radius 0.8. */
    static int[][] softRing(int d) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double r = Math.hypot(x + 0.5 - c, y + 0.5 - c) / c;
                double v = Math.exp(-Math.pow((r - 0.82) / 0.07, 2)) + Math.exp(-Math.pow((r - 0.82) / 0.02, 2)) * 0.6;
                if (r > 0.99) v = 0;
                px[y * d + x] = ((int) (Math.min(1, v) * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    /** Atmosphere halo around a disc of radius 0.72*d/2 (white; tinted). */
    static int[][] halo(int d) {
        int[] px = new int[d * d];
        double c = d / 2.0, R = 0.72;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double r = Math.hypot(x + 0.5 - c, y + 0.5 - c) / c;
                double v;
                if (r < R) v = Math.pow(r / R, 14) * 0.9;
                else v = Math.pow(Math.max(0, 1 - (r - R) / (1 - R)), 2.4);
                px[y * d + x] = ((int) (Math.min(1, v) * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    /** Rocky asteroid (lit from the upper left), with alpha. */
    static int[][] rock(int d, long seed, int base) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        double so = seed * 1.7;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / (c * 0.9), dy = (y + 0.5 - c) / (c * 0.9);
                double ang = Math.atan2(dy, dx);
                double rr = 0.78 + 0.16 * fbm(Math.cos(ang) * 1.5 + so, Math.sin(ang) * 1.5, so, 3) * 2;
                double r = Math.sqrt(dx * dx + dy * dy) / rr;
                if (r >= 1) continue;
                double nz = Math.sqrt(Math.max(0, 1 - r * r));
                double nx = dx / rr, ny = dy / rr;
                double bump = fbm(nx * 4 + so, ny * 4, nz * 4, 4);
                double l = Math.max(0, -nx * 0.6 - ny * 0.5 + nz * 0.62 + bump * 0.5);
                double cr = noise(nx * 7 + so, ny * 7, nz * 7);
                int col = shade(base, 0.12 + l * 1.05);
                if (cr > 0.35) col = shade(col, 0.75);
                double edge = Math.min(1, (1 - r) * d * 0.5);
                px[y * d + x] = ((int) (edge * 255) << 24) | (col & 0xFFFFFF);
            }
        return new int[][]{px, {d, d}};
    }

    /** Planet ring (both halves in one image): back half = rows above the equator line. */
    static int[] ringPixels(int w, int h, float inner, float outer, int[] cols, float[] sectorHp, float tilt, double sunX) {
        int[] px = new int[w * h];
        double cx = w / 2.0, cy = h / 2.0, rx = w / 2.0, ry = h / 2.0;
        double ct = Math.cos(tilt), st = Math.sin(tilt);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx), dy = (y + 0.5 - cy);
                double ux = dx * ct + dy * st, uy = -dx * st + dy * ct;
                double r = Math.sqrt(Math.pow(ux / rx, 2) + Math.pow(uy / (ry), 2));
                if (r < inner || r > outer) continue;
                double t = (r - inner) / (outer - inner);
                double band = 0.55 + 0.45 * Math.sin(t * 38) * Math.sin(t * 11 + 1.3);
                if (t > 0.58 && t < 0.63) band *= 0.15;
                if (t > 0.86 && t < 0.88) band *= 0.3;
                double a = band * Math.min(1, t * 8) * Math.min(1, (1 - t) * 10) * 0.85;
                int c = mix(cols[0], cols[1], t);
                c = mix(c, cols[2], clamp01(Math.sin(t * 23) * 0.5 + 0.2));
                if (sectorHp != null) {
                    double ang = Math.atan2(uy / ry, ux / rx);
                    int s = (int) ((ang + Math.PI) / (Math.PI * 2) * sectorHp.length) % sectorHp.length;
                    double hp = sectorHp[s];
                    int tr = (int) (t * 6);
                    double hsh = (HASH[(s * 7 + tr * 131) & 65535] & 255) / 255.0;
                    a *= clamp01(hp * 1.6 - hsh * 0.6);
                    if (hp < 0.99) c = mix(c, 0xFF3A2A20, (1 - hp) * 0.4);
                }
                // planet shadow (sun from sunX side)
                if (uy > 0 && Math.abs(ux / rx) < 0.43 * (1 / 1.0) && ux * sunX < 0) a *= 0.35;
                if (a < 0.01) continue;
                px[y * w + x] = ((int) (a * 255) << 24) | (c & 0xFFFFFF);
            }
        return px;
    }

    /** Black hole accretion disc (white-hot inner edge, orange outside). */
    static int[][] accretion(int w, int h) {
        int[] px = new int[w * h];
        double cx = w / 2.0, cy = h / 2.0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx) / cx, dy = (y + 0.5 - cy) / cy;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r > 1 || r < 0.26) continue;
                double t = (r - 0.26) / 0.74;
                double ang = Math.atan2(dy, dx);
                double sw = 0.6 + 0.4 * noise(Math.cos(ang) * 3 + t * 6, Math.sin(ang) * 3, t * 4);
                double a = Math.pow(1 - t, 1.6) * sw * Math.min(1, t * 12);
                int c = ramp(t, new double[]{0, 0.12, 0.4, 1}, new int[]{0xFFFFFFFF, 0xFFFFE2A0, 0xFFFF8A2A, 0xFF8A1E08});
                if (dx > 0) c = shade(c, 0.8); else a = Math.min(1, a * 1.25);
                px[y * w + x] = ((int) (clamp01(a) * 255) << 24) | (c & 0xFFFFFF);
            }
        return new int[][]{px, {w, h}};
    }

    /** Swirling portal (white; tinted). */
    static int[][] swirl(int d, int arms) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / c, dy = (y + 0.5 - c) / c;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r >= 1) continue;
                double ang = Math.atan2(dy, dx);
                double s = Math.sin(ang * arms + Math.log(r + 0.02) * 7) * 0.5 + 0.5;
                double a = Math.pow(1 - r, 1.2) * (0.35 + 0.65 * s) * Math.min(1, r * 5);
                px[y * d + x] = ((int) (clamp01(a) * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    /** Glowing network of cracks on a disc (white; used before a planet bursts). */
    static int[][] cracks(int d, long seed) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / c, dy = (y + 0.5 - c) / c;
                double r = Math.sqrt(dx * dx + dy * dy);
                if (r >= 1) continue;
                double n = ridge(dx * 2.6 + seed, dy * 2.6, 0.5 + seed * 0.1, 4);
                double v = sstep(0.72, 0.95, n);
                double e = Math.min(1, (1 - r) * d * 0.5);
                px[y * d + x] = ((int) (v * e * 255) << 24) | 0xFFFFFF;
            }
        return new int[][]{px, {d, d}};
    }

    // ================================================================== vector sprites (Java2D, painted once)
    static Color C(int argb) { return new Color(argb, true); }

    static void missile(Graphics2D g, int s, double ang, int body, int tip, int fin, float len) {
        g.translate(s / 2.0, s / 2.0);
        g.rotate(ang);
        double L = s * 0.42 * len, W = s * 0.075;
        g.setColor(C(fin));
        g.fill(poly(-L * 0.9, -W * 0.9, -L * 1.05, -W * 2.6, -L * 0.55, -W, -L * 0.55, W, -L * 1.05, W * 2.6, -L * 0.9, W * 0.9));
        g.setPaint(new GradientPaint(0, (float) -W, C(body), 0, (float) W, C(SolarTex.shade(body, 0.55))));
        g.fill(new RoundRectangle2D.Double(-L * 0.9, -W, L * 1.5, W * 2, W, W));
        g.setColor(C(tip));
        g.fill(poly(L * 0.58, -W, L, 0, L * 0.58, W));
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new RoundRectangle2D.Double(-L * 0.8, -W * 0.8, L * 1.3, W * 0.6, W * 0.5, W * 0.5));
    }

    static Path2D.Double poly(double... xy) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) p.lineTo(xy[i], xy[i + 1]);
        p.closePath();
        return p;
    }

    static void ellipse(Graphics2D g, double cx, double cy, double rx, double ry) { g.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); }

    /** Flying saucer: kind 0 scout, 1 mothership, 2 bomber, 3 cruiser, 4 lander. */
    static void ufo(Graphics2D g, int s, int kind, int glow) {
        double c = s / 2.0;
        if (kind == 1) {
            g.setPaint(new RadialGradientPaint((float) c, (float) (c * 0.8), (float) (s * 0.5f), new float[]{0, 1}, new Color[]{C(0xFF5A6474), C(0xFF1C2028)}));
            ellipse(g, c, c, s * 0.48, s * 0.3);
            g.setColor(C(0xFF0E1116));
            ellipse(g, c, c + s * 0.02, s * 0.3, s * 0.17);
            g.setColor(C(glow));
            for (int i = 0; i < 16; i++) {
                double a = i / 16.0 * Math.PI * 2;
                ellipse(g, c + Math.cos(a) * s * 0.4, c + Math.sin(a) * s * 0.23, s * 0.014, s * 0.014);
            }
            g.setPaint(new RadialGradientPaint((float) c, (float) c, (float) (s * 0.16f), new float[]{0, 1}, new Color[]{C(0xFFFFFFFF), C(glow & 0x00FFFFFF)}));
            ellipse(g, c, c, s * 0.16, s * 0.1);
            g.setColor(C(0x55FFFFFF));
            g.setStroke(new BasicStroke(s * 0.006f));
            for (int i = 1; i <= 3; i++) g.draw(new Ellipse2D.Double(c - s * 0.12 * i - s * 0.04, c - s * 0.07 * i - s * 0.02, (s * 0.12 * i + s * 0.04) * 2, (s * 0.07 * i + s * 0.02) * 2));
            return;
        }
        if (kind == 2) {
            g.setColor(C(0xFF2A2F3A));
            g.fill(poly(c, c - s * 0.3, c + s * 0.42, c + s * 0.18, c, c + s * 0.06, c - s * 0.42, c + s * 0.18));
            g.setColor(C(glow));
            ellipse(g, c, c - s * 0.05, s * 0.06, s * 0.06);
            ellipse(g, c - s * 0.3, c + s * 0.12, s * 0.03, s * 0.03);
            ellipse(g, c + s * 0.3, c + s * 0.12, s * 0.03, s * 0.03);
            return;
        }
        if (kind == 3) {
            g.setPaint(new GradientPaint(0, (float) (c - s * 0.1), C(0xFF7A7F8C), 0, (float) (c + s * 0.12), C(0xFF2A2D35)));
            g.fill(poly(c - s * 0.46, c, c - s * 0.2, c - s * 0.12, c + s * 0.36, c - s * 0.08, c + s * 0.47, c, c + s * 0.36, c + s * 0.1, c - s * 0.2, c + s * 0.12));
            g.setColor(C(glow));
            for (int i = 0; i < 5; i++) ellipse(g, c - s * 0.2 + i * s * 0.12, c, s * 0.022, s * 0.022);
            g.setColor(C(0xFFFFFFFF));
            ellipse(g, c - s * 0.44, c, s * 0.03, s * 0.05);
            return;
        }
        // scout / lander
        double sc = kind == 4 ? 0.85 : 1;
        g.setPaint(new GradientPaint(0, (float) (c - s * 0.1), C(0xFFB8C2D0), 0, (float) (c + s * 0.15), C(0xFF3A4150)));
        ellipse(g, c, c + s * 0.05, s * 0.46 * sc, s * 0.15 * sc);
        g.setPaint(new RadialGradientPaint((float) (c - s * 0.05), (float) (c - s * 0.12), (float) (s * 0.25), new float[]{0, 1}, new Color[]{C(0xFFE8FFF4), C(glow)}));
        g.fill(new Arc2D.Double(c - s * 0.2 * sc, c - s * 0.2 * sc, s * 0.4 * sc, s * 0.4 * sc, 0, 180, Arc2D.CHORD));
        g.setColor(C(glow));
        for (int i = 0; i < 5; i++) ellipse(g, c - s * 0.3 * sc + i * s * 0.15 * sc, c + s * 0.08, s * 0.03, s * 0.03);
        if (kind == 4) {
            g.setColor(C(0xFF8A93A3));
            g.setStroke(new BasicStroke(s * 0.03f));
            g.draw(new java.awt.geom.Line2D.Double(c - s * 0.25, c + s * 0.12, c - s * 0.34, c + s * 0.3));
            g.draw(new java.awt.geom.Line2D.Double(c + s * 0.25, c + s * 0.12, c + s * 0.34, c + s * 0.3));
        }
    }

    static void walker(Graphics2D g, int s, int glow) {
        double c = s / 2.0;
        g.setStroke(new BasicStroke(s * 0.045f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(C(0xFF9AA2B0));
        g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c - s * 0.32, c + s * 0.42));
        g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c + s * 0.32, c + s * 0.42));
        g.draw(new java.awt.geom.Line2D.Double(c, c - s * 0.12, c + s * 0.04, c + s * 0.46));
        g.setPaint(new GradientPaint(0, (float) (c - s * 0.3), C(0xFFC8D0DC), 0, (float) (c - s * 0.02), C(0xFF4A5260)));
        ellipse(g, c, c - s * 0.18, s * 0.2, s * 0.12);
        g.setColor(C(glow));
        ellipse(g, c, c - s * 0.16, s * 0.05, s * 0.04);
    }

    static void satellite(Graphics2D g, int s, int glow) {
        double c = s / 2.0;
        g.setColor(C(0xFF2B4A8A));
        g.fill(new java.awt.Rectangle.Double(c - s * 0.46, c - s * 0.08, s * 0.28, s * 0.16));
        g.fill(new java.awt.Rectangle.Double(c + s * 0.18, c - s * 0.08, s * 0.28, s * 0.16));
        g.setColor(C(0xFF6A8AC8));
        for (int i = 0; i < 3; i++) {
            g.fill(new java.awt.Rectangle.Double(c - s * 0.44 + i * s * 0.09, c - s * 0.06, s * 0.07, s * 0.12));
            g.fill(new java.awt.Rectangle.Double(c + s * 0.2 + i * s * 0.09, c - s * 0.06, s * 0.07, s * 0.12));
        }
        g.setPaint(new GradientPaint(0, (float) (c - s * 0.12), C(0xFFE0E4EA), 0, (float) (c + s * 0.12), C(0xFF6A707A)));
        g.fill(new RoundRectangle2D.Double(c - s * 0.13, c - s * 0.13, s * 0.26, s * 0.26, s * 0.06, s * 0.06));
        g.setColor(C(glow));
        ellipse(g, c, c, s * 0.06, s * 0.06);
    }

    /** Planet-killer battle station (original design: "Sternenbrecher"). */
    static int[][] station(int d) {
        int[] px = new int[d * d];
        double c = d / 2.0;
        for (int y = 0; y < d; y++)
            for (int x = 0; x < d; x++) {
                double dx = (x + 0.5 - c) / (c * 0.96), dy = (y + 0.5 - c) / (c * 0.96);
                double r2 = dx * dx + dy * dy;
                if (r2 >= 1) continue;
                double nz = Math.sqrt(1 - r2);
                double l = Math.max(0, -dx * 0.55 - dy * 0.45 + nz * 0.7);
                int col = 0xFF8C929C;
                // panel grid
                double lat = Math.asin(dy), lon = Math.atan2(dx, nz);
                boolean seam = Math.abs(Math.sin(lat * 14)) < 0.07 || Math.abs(Math.sin(lon * 18)) < 0.05;
                if (seam) col = 0xFF5A606A;
                int hsh = HASH[((int) (lat * 40) * 97 + (int) (lon * 50) * 13) & 65535] & 255;
                if (hsh < 40) col = mix(col, 0xFF6C727C, 0.8);
                if (Math.abs(dy) < 0.035) col = 0xFF2C3038;
                // dish
                double ddx = dx + 0.38, ddy = dy + 0.36, dr = Math.sqrt(ddx * ddx + ddy * ddy);
                if (dr < 0.24) {
                    col = mix(0xFF3A4048, 0xFF6A7078, dr / 0.24);
                    if (Math.abs(Math.sin(dr * 60)) < 0.3) col = shade(col, 0.8);
                    if (dr < 0.04) col = 0xFF9DFF6A;
                }
                col = shade(col, 0.15 + l * 1.0);
                if (hsh > 250 && l < 0.3) col = 0xFFFFE9A0;
                double e = Math.min(1, (1 - Math.sqrt(r2)) * d * 0.48);
                px[y * d + x] = ((int) (e * 255) << 24) | (col & 0xFFFFFF);
            }
        return new int[][]{px, {d, d}};
    }

    /** Fun projectiles: 0 lego, 1 cow, 2 cake, 3 duck, 4 bowling, 5 pizza, 6 donut, 7 piano, 8 minifig, 9 gummy (tinted), 10 disco ball. */
    static void fun(Graphics2D g, int s, int kind, int col) {
        double c = s / 2.0, k = s / 100.0;
        switch (kind) {
            case 0: { // 2x4 brick seen from above (slight perspective)
                g.setColor(C(shade(col, 0.6)));
                g.fill(new RoundRectangle2D.Double(c - 44 * k, c - 22 * k, 88 * k, 50 * k, 8 * k, 8 * k));
                g.setColor(C(col));
                g.fill(new RoundRectangle2D.Double(c - 44 * k, c - 26 * k, 88 * k, 46 * k, 8 * k, 8 * k));
                for (int i = 0; i < 4; i++)
                    for (int j = 0; j < 2; j++) {
                        double sx = c - 33 * k + i * 22 * k, sy = c - 14 * k + j * 22 * k;
                        g.setColor(C(shade(col, 0.7)));
                        ellipse(g, sx, sy + 2 * k, 8 * k, 8 * k);
                        g.setColor(C(shade(col, 1.22)));
                        ellipse(g, sx, sy, 8 * k, 8 * k);
                    }
                break;
            }
            case 1: { // cow
                g.setColor(C(0xFFF4F1EA));
                ellipse(g, c, c + 4 * k, 36 * k, 24 * k);
                g.setColor(C(0xFF1C1A1A));
                ellipse(g, c - 12 * k, c, 10 * k, 8 * k);
                ellipse(g, c + 14 * k, c + 12 * k, 9 * k, 7 * k);
                ellipse(g, c + 4 * k, c - 10 * k, 6 * k, 5 * k);
                g.setColor(C(0xFFF4F1EA));
                ellipse(g, c + 38 * k, c - 8 * k, 14 * k, 12 * k);
                g.setColor(C(0xFFF2A5B0));
                ellipse(g, c + 46 * k, c - 4 * k, 8 * k, 6 * k);
                g.setColor(C(0xFF1C1A1A));
                ellipse(g, c + 36 * k, c - 12 * k, 2.5 * k, 2.5 * k);
                g.setColor(C(0xFFD9CFC0));
                g.fill(poly(c + 30 * k, c - 18 * k, c + 26 * k, c - 30 * k, c + 34 * k, c - 20 * k));
                g.setColor(C(0xFF3A3434));
                for (int i = 0; i < 4; i++) g.fill(new java.awt.Rectangle.Double(c - 26 * k + i * 15 * k, c + 22 * k, 5 * k, 14 * k));
                break;
            }
            case 2: { // cake
                g.setColor(C(0xFF8A4A2A));
                ellipse(g, c, c + 14 * k, 40 * k, 16 * k);
                g.fill(new java.awt.Rectangle.Double(c - 40 * k, c - 12 * k, 80 * k, 26 * k));
                g.setColor(C(0xFFFFD7E4));
                ellipse(g, c, c - 12 * k, 40 * k, 16 * k);
                g.fill(new java.awt.Rectangle.Double(c - 40 * k, c - 12 * k, 80 * k, 8 * k));
                g.setColor(C(0xFFFFFFFF));
                for (int i = 0; i < 8; i++) ellipse(g, c - 35 * k + i * 10 * k, c - 4 * k, 5 * k, 5 * k);
                g.setColor(C(0xFFE3000B));
                ellipse(g, c - 14 * k, c - 16 * k, 5 * k, 5 * k);
                ellipse(g, c + 12 * k, c - 10 * k, 5 * k, 5 * k);
                g.setColor(C(0xFF5FB8FF));
                g.fill(new java.awt.Rectangle.Double(c - 2 * k, c - 36 * k, 4 * k, 20 * k));
                g.setColor(C(0xFFFFD34A));
                ellipse(g, c, c - 40 * k, 4 * k, 6 * k);
                break;
            }
            case 3: { // rubber duck
                g.setColor(C(0xFFFFD21F));
                ellipse(g, c - 4 * k, c + 12 * k, 34 * k, 22 * k);
                ellipse(g, c + 16 * k, c - 14 * k, 18 * k, 18 * k);
                g.setColor(C(0xFFFF8A00));
                g.fill(poly(c + 30 * k, c - 16 * k, c + 46 * k, c - 10 * k, c + 30 * k, c - 6 * k));
                g.setColor(C(0xFF1A1A1A));
                ellipse(g, c + 20 * k, c - 18 * k, 3.5 * k, 3.5 * k);
                g.setColor(C(0xFFFFE680));
                ellipse(g, c - 12 * k, c + 6 * k, 14 * k, 8 * k);
                break;
            }
            case 4: { // bowling ball
                g.setPaint(new RadialGradientPaint((float) (c - 14 * k), (float) (c - 14 * k), (float) (50 * k), new float[]{0, 1}, new Color[]{C(0xFF6A4ACB), C(0xFF140A30)}));
                ellipse(g, c, c, 44 * k, 44 * k);
                g.setColor(C(0xFF0A0614));
                ellipse(g, c + 8 * k, c - 16 * k, 5 * k, 5 * k);
                ellipse(g, c + 20 * k, c - 8 * k, 5 * k, 5 * k);
                ellipse(g, c + 14 * k, c + 6 * k, 6 * k, 6 * k);
                break;
            }
            case 5: { // pizza
                g.setColor(C(0xFFD59A4A));
                ellipse(g, c, c, 44 * k, 44 * k);
                g.setColor(C(0xFFE84A24));
                ellipse(g, c, c, 38 * k, 38 * k);
                g.setColor(C(0xFFFFDC6A));
                ellipse(g, c, c, 35 * k, 35 * k);
                g.setColor(C(0xFFB0251A));
                double[][] pp = {{-14, -12}, {12, -18}, {18, 8}, {-6, 16}, {-20, 6}, {2, -2}};
                for (double[] q : pp) ellipse(g, c + q[0] * k, c + q[1] * k, 7 * k, 7 * k);
                g.setColor(C(0xFF3E8A2E));
                ellipse(g, c + 8 * k, c + 18 * k, 3 * k, 2 * k);
                ellipse(g, c - 16 * k, c - 22 * k, 3 * k, 2 * k);
                break;
            }
            case 6: { // donut
                g.setColor(C(0xFFC98A4E));
                ellipse(g, c, c, 44 * k, 44 * k);
                g.setColor(C(0xFFFF7FC0));
                ellipse(g, c, c - 2 * k, 38 * k, 36 * k);
                int[] sp = {0xFFFFFFFF, 0xFF5FD4FF, 0xFFFFE14D, 0xFF7CF77C};
                Random r = new Random(4);
                for (int i = 0; i < 26; i++) {
                    double a = r.nextDouble() * Math.PI * 2, rr = 20 + r.nextDouble() * 14;
                    g.setColor(C(sp[i % 4]));
                    g.fill(new java.awt.Rectangle.Double(c + Math.cos(a) * rr * k, c + Math.sin(a) * rr * k - 2 * k, 5 * k, 2 * k));
                }
                g.setComposite(java.awt.AlphaComposite.Clear);
                ellipse(g, c, c, 13 * k, 13 * k);
                g.setComposite(java.awt.AlphaComposite.SrcOver);
                break;
            }
            case 7: { // piano (top view)
                g.setColor(C(0xFF16161A));
                g.fill(new RoundRectangle2D.Double(c - 40 * k, c - 28 * k, 80 * k, 56 * k, 10 * k, 10 * k));
                g.setColor(C(0xFFF4F4F0));
                g.fill(new java.awt.Rectangle.Double(c - 38 * k, c + 10 * k, 76 * k, 14 * k));
                g.setColor(C(0xFF16161A));
                for (int i = 0; i < 14; i++) if (i % 7 != 2 && i % 7 != 6) g.fill(new java.awt.Rectangle.Double(c - 36 * k + i * 5.4 * k, c + 10 * k, 3 * k, 8 * k));
                g.setColor(C(0xFF3A3A44));
                g.fill(new java.awt.Rectangle.Double(c - 34 * k, c - 22 * k, 68 * k, 26 * k));
                break;
            }
            case 8: { // minifig
                g.setColor(C(0xFFFFD21F));
                ellipse(g, c, c - 26 * k, 11 * k, 11 * k);
                g.fill(new java.awt.Rectangle.Double(c - 4 * k, c - 38 * k, 8 * k, 4 * k));
                g.setColor(C(0xFF1A1A1A));
                ellipse(g, c - 4 * k, c - 28 * k, 1.8 * k, 1.8 * k);
                ellipse(g, c + 4 * k, c - 28 * k, 1.8 * k, 1.8 * k);
                g.setStroke(new BasicStroke((float) (1.6 * k)));
                g.draw(new Arc2D.Double(c - 5 * k, c - 27 * k, 10 * k, 7 * k, 200, 140, Arc2D.OPEN));
                g.setColor(C(col));
                g.fill(poly(c - 14 * k, c - 14 * k, c + 14 * k, c - 14 * k, c + 17 * k, c + 12 * k, c - 17 * k, c + 12 * k));
                g.setColor(C(shade(col, 0.8)));
                g.fill(new RoundRectangle2D.Double(c - 24 * k, c - 12 * k, 8 * k, 22 * k, 6 * k, 6 * k));
                g.fill(new RoundRectangle2D.Double(c + 16 * k, c - 12 * k, 8 * k, 22 * k, 6 * k, 6 * k));
                g.setColor(C(0xFF0055BF));
                g.fill(new java.awt.Rectangle.Double(c - 16 * k, c + 12 * k, 32 * k, 26 * k));
                g.setColor(C(0xFF003E8C));
                g.fill(new java.awt.Rectangle.Double(c - 1 * k, c + 18 * k, 2 * k, 20 * k));
                break;
            }
            case 9: { // gummy bear
                g.setColor(C(col));
                ellipse(g, c, c + 8 * k, 20 * k, 24 * k);
                ellipse(g, c, c - 20 * k, 15 * k, 13 * k);
                ellipse(g, c - 12 * k, c - 32 * k, 6 * k, 6 * k);
                ellipse(g, c + 12 * k, c - 32 * k, 6 * k, 6 * k);
                ellipse(g, c - 20 * k, c - 2 * k, 8 * k, 6 * k);
                ellipse(g, c + 20 * k, c - 2 * k, 8 * k, 6 * k);
                ellipse(g, c - 12 * k, c + 30 * k, 9 * k, 7 * k);
                ellipse(g, c + 12 * k, c + 30 * k, 9 * k, 7 * k);
                g.setColor(new Color(255, 255, 255, 110));
                ellipse(g, c - 6 * k, c - 24 * k, 4 * k, 3 * k);
                ellipse(g, c - 7 * k, c + 2 * k, 5 * k, 8 * k);
                break;
            }
            default: { // disco ball
                g.setColor(C(0xFF9AA0AA));
                ellipse(g, c, c, 42 * k, 42 * k);
                Random r = new Random(7);
                for (int yy = -40; yy < 40; yy += 7)
                    for (int xx = -40; xx < 40; xx += 7) {
                        if (xx * xx + yy * yy > 40 * 40) continue;
                        int b = 120 + r.nextInt(135);
                        g.setColor(new Color(b, b, Math.min(255, b + 20)));
                        g.fill(new java.awt.Rectangle.Double(c + xx * k, c + yy * k, 6 * k, 6 * k));
                    }
            }
        }
    }
}
