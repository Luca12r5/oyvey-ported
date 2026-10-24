package dev.lego.visual.sky;

import java.util.Random;

import static dev.lego.visual.sky.SkyGen.*;

/** Night and space presets for {@link SkyGen}. */
final class SkyGen3 {
    private SkyGen3() { }

    static Sky make(String id) {
        switch (id) {
            case "milkyway": return milkyway();
            case "nebula": return nebula();
            case "planets": return planets();
            case "aurora": return aurora();
            case "storm": return storm();
            case "bloodmoon": return bloodMoon();
            case "eclipse": return eclipse();
            case "auroraviolet": return auroraViolet();
            case "winter": return winter();
            case "twilight": return twilight();
            case "rednebula": return redNebula();
            case "spiral": return spiral();
            case "blackhole": return blackHole();
            case "void": return voidSky();
            default: return null;
        }
    }

    // ================================================================== building blocks

    /** A round celestial body (moon / planet) with a precomputed tangent frame. */
    static final class Body {
        final double[] d, ex, ey;
        final double rad, sinR, cosOut;
        double glow = 0.0, glowW = 2.5, albedo = 0.6, seed = 0, earthshine = 0.015;
        double[] grad = null;       // blood moon: brightness gradient direction (world space)

        Body(double[] dir, double rad, double roll) {
            d = dir;
            this.rad = rad;
            sinR = Math.sin(rad);
            double[][] f = frame(dir, roll);
            ex = f[0]; ey = f[1];
            cosOut = Math.cos(Math.min(Math.PI, rad * 14));
        }
    }

    /** Textured moon: light = sun direction for the phase (null = full moon). */
    static void moon(C c, double x, double y, double z, Body m, double[] tint, double bright, double[] light) {
        double cosA = x * m.d[0] + y * m.d[1] + z * m.d[2];
        if (cosA < m.cosOut) return;
        double ang = Math.sqrt(Math.max(0, 2 * (1 - cosA)));
        double rr = ang / m.rad;
        if (m.glow > 0) {
            double g = m.glow * bright * Math.exp(-Math.max(0, rr - 1) / m.glowW);
            add(c, tint, g);
        }
        double aa = Math.max(0.02, 0.004 / m.rad);
        if (rr > 1 + aa) return;
        double px = (x * m.ex[0] + y * m.ex[1] + z * m.ex[2]) / m.sinR;
        double py = (x * m.ey[0] + y * m.ey[1] + z * m.ey[2]) / m.sinR;
        double q = px * px + py * py;
        double pz = Math.sqrt(Math.max(0, 1 - Math.min(1, q)));
        double nx = m.ex[0] * px + m.ey[0] * py - m.d[0] * pz;
        double ny = m.ex[1] * px + m.ey[1] * py - m.d[1] * pz;
        double nz = m.ex[2] * px + m.ey[2] * py - m.d[2] * pz;
        double s = m.seed;
        double maria = smooth(-0.05, 0.3, fbmL(nx * 1.8 + s, ny * 1.8, nz * 1.8, 4));
        double crater = smooth(0.55, 0.85, ridged(nx * 6 + s, ny * 6, nz * 6, 3));
        double fine = fbmL(nx * 14, ny * 14 + s, nz * 14, 3);
        double alb = m.albedo * (1 - 0.42 * maria) * (0.9 + 0.25 * fine) * (1 + 0.25 * crater);
        double lam;
        if (light != null) {
            double ld = nx * light[0] + ny * light[1] + nz * light[2];
            lam = smooth(-0.04, 0.25, ld) * (0.35 + 0.65 * Math.max(0, ld));
        } else lam = 0.55 + 0.45 * pz;
        if (m.grad != null) {
            double gd = nx * m.grad[0] + ny * m.grad[1] + nz * m.grad[2];
            lam *= 0.3 + 0.7 * smooth(-0.9, 1.0, gd);
        }
        double k = alb * (lam * bright + m.earthshine);
        double e = smooth(1 + aa, 1 - aa, rr);
        c.r += (c.r * 0.35 + tint[0] * k - c.r) * e;
        c.g += (c.g * 0.35 + tint[1] * k - c.g) * e;
        c.b += (c.b * 0.35 + tint[2] * k - c.b) * e;
        c.vis *= 1 - e;
    }

    /** Milky way band; returns the band strength (0..1). */
    static double milky(C c, double x, double y, double z, double[] N, double[] core, double K) {
        double dist = x * N[0] + y * N[1] + z * N[2];
        if (Math.abs(dist) > 0.7) return 0;
        double w = fbmL(x * 3 + 1, y * 3, z * 3, 4);
        double q = (dist + w * 0.07) / 0.17;
        double band = Math.exp(-q * q);
        double cc = x * core[0] + y * core[1] + z * core[2];
        double bul = Math.exp(-(1 - cc) / 0.09) * Math.exp(-dist * dist / 0.03);
        double s = fbmL(x * 7 + 3, y * 7, z * 7, 6);
        double cl = clamp01(0.45 + s * 1.1);
        double r = ridged(x * 5 + 2, y * 5, z * 5, 5);
        double q2 = dist / 0.06;
        double dust = smooth(0.35, 0.8, r) * Math.exp(-q2 * q2) + 0.5 * smooth(0.1, 0.5, fbmL(x * 9, y * 9 + 5, z * 9, 4)) * band;
        dust = clamp01(dust);
        double light = (band * (0.25 + cl) + bul * 1.4) * (1 - 0.8 * dust);
        double warm = clamp01(bul * 1.5);
        c.r += K * light * (0.55 + 0.5 * warm);
        c.g += K * light * (0.62 + 0.22 * warm);
        c.b += K * light * (0.88 - 0.25 * warm);
        return band;
    }

    /** Cheap band strength for star placement. */
    static double milkyDensity(double x, double y, double z, double[] N) {
        double dist = x * N[0] + y * N[1] + z * N[2];
        double w = fbmL(x * 3 + 1, y * 3, z * 3, 4);
        double q = (dist + w * 0.07) / 0.17;
        return Math.exp(-q * q);
    }

    /** Aurora curtains (ray-marched through a stack of height slices). */
    static final class Aurora {
        double[] low = rgb(0x3CFF8A), high = rgb(0xB03C9A);
        double bright = 1, scale = 0.35, width = 0.04, seed = 0, h0 = 1, h1 = 3.2, rays = 1, cover = 0.2, north = 1;
        int steps = 36;
    }

    /**
     * Aurora curtains: vertical sheets standing on the curve f(p) = 0 of the horizontal plane, between heights h0..h1.
     * The view ray is split into segments; every sign change of f along the ray is a crossing of a sheet, whose
     * brightness grows when the ray runs along the sheet (bright folds) and fades with height.
     */
    static void aurora(C c, double x, double y, double z, Aurora A) {
        if (y < 0.004) return;
        double fade = smooth(0.004, 0.15, y);
        double hl = Math.sqrt(x * x + z * z) + 1e-9;
        double mask = smooth(-0.35, 0.25, fbmL(x / hl * 1.3 + A.seed, 0.5, z / hl * 1.3, 2) + A.cover + 0.4 * (-z / hl) * A.north);
        if (mask <= 0.003) return;
        double inv = A.scale / (y + 0.25);
        double segLen = hl * (A.h1 - A.h0) * inv * 0.9;           // ray length through the aurora layer in noise units
        int S = (int) Math.max(6, Math.min(A.steps, Math.ceil(segLen / 0.06)));
        double dh = (A.h1 - A.h0) / S;
        double pf = auroraF(x * A.h0 * inv, z * A.h0 * inv, A), ph = A.h0;
        double glow = Math.exp(-pf * pf / 0.008);
        double sr = 0, sg = 0, sb = 0;
        for (int i = 1; i <= S; i++) {
            double h = A.h0 + dh * i;
            double f = auroraF(x * h * inv, z * h * inv, A);
            double u = 0, wt = 0;
            if ((pf <= 0) != (f <= 0)) { u = pf / (pf - f); wt = 1; }
            if (wt > 0.002) {
                double hs = ph + dh * u;
                double t = (hs - A.h0) / (A.h1 - A.h0);
                double slope = Math.abs(f - pf) / dh;
                double b = A.width / Math.sqrt(slope * slope + A.width * A.width);
                double px = x * hs * inv, pz = z * hs * inv;
                double r = 0.25 + 0.75 * Math.abs(noise(px * 9 + A.seed, 3.7, pz * 9)) * A.rays + (1 - A.rays) * 0.75;
                double prof = (Math.exp(-t * 2.6) * smooth(0.0, 0.06, t) + 0.12 * Math.exp(-(t - 0.6) * (t - 0.6) / 0.04)) * smooth(1.0, 0.75, t);
                double tt = Math.pow(t, 0.8);
                double w = b * r * prof * wt;
                sr += (A.low[0] + (A.high[0] - A.low[0]) * tt) * w;
                sg += (A.low[1] + (A.high[1] - A.low[1]) * tt) * w;
                sb += (A.low[2] + (A.high[2] - A.low[2]) * tt) * w;
            }
            pf = f; ph = h;
        }
        double k = A.bright * fade * mask;
        double gk = 0.03 * glow * k;
        c.r += sr * k + A.low[0] * gk; c.g += sg * k + A.low[1] * gk; c.b += sb * k + A.low[2] * gk;
    }

    private static double auroraF(double px, double pz, Aurora A) {
        return fbmL(px * 0.9 + A.seed, A.seed * 0.7, pz * 0.9, 2.5);
    }

    /** Generic night sky. */
    static class Night extends Sky {
        double[] zen = rgb(0x03060E), hor = rgb(0x101A2C);
        double hW = 0.25;
        double[] air = null; double airK = 0, airW = 0.08;
        double[] lp = null, lpDir = null; double lpK = 0;
        double[] mwN = null, mwCore = null; double mwK = 0.05;
        Body moon; double[] moonTint = rgb(0xFFFFFF), moonLight = null; double moonBright = 1;
        double halo22 = 0;
        Aurora au;
        Clouds c1; double[] cloudLight = rgb(0x8090B0);
        double[] mist = null; double mistW = 0.04, mistK = 0;
        double starDens = 1;

        Night() { kind = "night"; stars = 60000; starMin = 0.035; starMax = 3.5; }

        @Override
        void sky(double x, double y, double z, C c) {
            double yy = y > 0 ? y : 0;
            double t = Math.exp(-yy / hW);
            c.r = zen[0] + (hor[0] - zen[0]) * t; c.g = zen[1] + (hor[1] - zen[1]) * t; c.b = zen[2] + (hor[2] - zen[2]) * t;
            if (air != null) add(c, air, airK * Math.exp(-yy / airW));
            if (lp != null) {
                double hl = Math.sqrt(x * x + z * z) + 1e-9;
                double a = (x * lpDir[0] + z * lpDir[2]) / hl;
                a = Math.max(0, a);
                add(c, lp, lpK * a * a * a * Math.exp(-yy / 0.1));
            }
            if (mwN != null) milky(c, x, y, z, mwN, mwCore, mwK);
            if (au != null) aurora(c, x, y, z, au);
            extra(x, y, z, c);
            if (moon != null) {
                if (halo22 > 0) {
                    double cosA = x * moon.d[0] + y * moon.d[1] + z * moon.d[2];
                    double ang = Math.acos(Math.max(-1, Math.min(1, cosA)));
                    double q = (ang - 0.384) / 0.016;
                    double hh = Math.exp(-q * q) * halo22 + Math.exp(-Math.max(0, ang - 0.384) / 0.12) * halo22 * 0.25 * smooth(0.36, 0.40, ang);
                    c.r += hh * (1.1 - 0.25 * smooth(-1, 1, q)); c.g += hh; c.b += hh * (0.85 + 0.35 * smooth(-1, 1, q));
                }
                moon(c, x, y, z, moon, moonTint, moonBright, moonLight);
            }
            if (c1 != null) clouds(c, x, y, z, c1, moon != null ? moon.d : sun, cloudLight);
            if (mist != null) mixTo(c, mist, mistK * Math.exp(-yy / mistW));
            // atmospheric extinction of stars near the horizon
            c.vis *= smooth(-0.01, 0.2, y);
        }

        void extra(double x, double y, double z, C c) { }

        @Override
        double starDensity(double x, double y, double z) {
            double h = smooth(-0.02, 0.3, y);
            if (mwN == null) return h * 0.35 * starDens;
            return h * Math.min(1, (0.25 + 0.75 * milkyDensity(x, y, z, mwN)) * starDens);
        }
    }

    /** Space: full sphere, no horizon. */
    abstract static class Space extends Sky {
        Space() { kind = "space"; below = SPACE; stars = 70000; starMin = 0.04; starMax = 4; }

        @Override
        double starDensity(double x, double y, double z) { return 0.4; }
    }

    /** Emission nebula: soft glow + bright filaments + dark dust lanes. a/b = glow colours (mixed by hue noise), cc = filaments, core = hot spots. */
    static void nebulaLayer(C c, double x, double y, double z, double seed, double scale, double[] a, double[] b, double[] cc, double[] core, double K, double dustK) {
        double wx = fbmL(x * 1.3 + 3.1 + seed, y * 1.3, z * 1.3, 3), wy = fbmL(x * 1.3, y * 1.3 + 7.7, z * 1.3 + seed, 3), wz = fbmL(x * 1.3 + seed, y * 1.3, z * 1.3 + 1.3, 3);
        double X = (x + wx * 0.4) * scale, Y = (y + wy * 0.4) * scale, Z = (z + wz * 0.4) * scale;
        double D = smooth(-0.05, 0.6, fbmL(X * 1.5, Y * 1.5, Z * 1.5 + seed, 6) + 0.12);
        if (D <= 0.001) return;
        double fil = ridged(X * 4 + seed, Y * 4, Z * 4, 5);
        double f = smooth(0.5, 0.95, fil) * D;
        double hue = clamp01(0.5 + fbmL(X * 0.8 + 7, Y * 0.8, Z * 0.8 + seed, 3) * 1.6);
        double gl = D * D * 0.55 + D * 0.12;
        double ra = a[0] + (b[0] - a[0]) * hue, ga = a[1] + (b[1] - a[1]) * hue, ba = a[2] + (b[2] - a[2]) * hue;
        double k = K;
        c.r += k * (ra * gl + (cc[0] * 0.6 + ra * 0.6) * f);
        c.g += k * (ga * gl + (cc[1] * 0.6 + ga * 0.6) * f);
        c.b += k * (ba * gl + (cc[2] * 0.6 + ba * 0.6) * f);
        double hot = Math.max(0, D - 0.75) / 0.25;
        add(c, core, k * hot * hot * 0.8);
        double dust = smooth(0.45, 0.85, ridged(X * 2.3 + 3, Y * 2.3 + seed, Z * 2.3, 4)) * smooth(0.1, 0.6, D);
        double dk = 1 - dust * dustK;
        mul(c, dk);
        c.vis *= 0.4 + 0.6 * dk;
    }

    // ================================================================== night presets

    static Sky milkyway() {
        Night s = new Night();
        s.zen = rgb(0x02050D); s.hor = rgb(0x0E1A2E); s.hW = 0.28;
        s.air = rgb(0x0E2A1C); s.airK = 0.6; s.airW = 0.07;
        s.lp = rgb(0x3A2410); s.lpDir = norm(-0.7, 0, 0.7); s.lpK = 0.5;
        s.mwN = norm(0.35, 0.62, 0.70); s.mwCore = coreOn(s.mwN, norm(0.75, 0.2, -0.6)); s.mwK = 0.075;
        s.stars = 90000;
        s.ground = rgb(0x05070B); s.groundFar = rgb(0x0E1622); s.groundFall = 0.2;
        return s;
    }

    static Sky aurora() {
        Night s = new Night();
        s.zen = rgb(0x020710); s.hor = rgb(0x0B2230); s.hW = 0.3;
        s.mwN = norm(-0.5, 0.55, 0.65); s.mwCore = coreOn(s.mwN, norm(0.3, 0.3, 0.9)); s.mwK = 0.025;
        Aurora a = new Aurora();
        a.low = rgb(0x2EFF86); a.high = rgb(0xB04CC8); a.bright = 0.55; a.seed = 2.7; a.width = 0.14;
        s.au = a;
        s.mist = rgb(0x0E3030); s.mistK = 0.35; s.mistW = 0.03; s.detail = 0.85;
        s.ground = rgb(0x121C26); s.groundFar = rgb(0x1E3A3C); s.groundFall = 0.25;
        return s;
    }

    static Sky auroraViolet() {
        Night s = new Night();
        s.zen = rgb(0x05030F); s.hor = rgb(0x1C1030); s.hW = 0.3;
        Aurora a = new Aurora();
        a.low = rgb(0xFF3FA0); a.high = rgb(0x5A40FF); a.bright = 0.55; a.seed = 7.3; a.width = 0.14; a.cover = 0.0;
        s.au = a;
        s.stars = 70000;
        s.below = OCEAN;
        s.water = rgb(0x020309); s.waves = 0.3; s.glitter = 0; s.detail = 0.85;
        return s;
    }

    static Sky storm() {
        final double[][] cells = {azel(40, 25), azel(160, 14), azel(290, 35), azel(250, 60)};
        final double[] flash = rgb(0xB8B4FF);
        Night s = new Night() {
            @Override
            void extra(double x, double y, double z, C c) {
                // distant lightning illuminating the horizon
                double hl = Math.sqrt(x * x + z * z) + 1e-9;
                double a = Math.max(0, (x * 0.8 - z * 0.6) / hl);
                add(c, flash, 0.05 * a * a * a * a * Math.exp(-Math.max(0, y) / 0.08));
            }

            @Override
            void sky(double x, double y, double z, C c) {
                super.sky(x, y, z, c);
                if (y <= 0.0) return;
                // glow inside the cloud deck
                double g = 0;
                for (double[] q : cells) {
                    double cosA = x * q[0] + y * q[1] + z * q[2];
                    double a2 = 2 * (1 - cosA);
                    g += Math.exp(-a2 / 0.06);
                }
                if (g > 0.003) {
                    double hl = 1.0 / (y + 0.05);
                    double n = fbmL(x * hl * 1.2, 5.5, z * hl * 1.2, 5);
                    double k = g * smooth(-0.3, 0.5, n) * smooth(0.0, 0.1, y);
                    add(c, flash, k * 0.45);
                }
            }
        };
        s.stars = 0;
        s.zen = rgb(0x0C0F16); s.hor = rgb(0x262C38); s.hW = 0.25;
        Clouds c = new Clouds();
        c.height = 0.8; c.scale = 0.7; c.cov = -0.15; c.soft = 0.45; c.warp = 1.3; c.seed = 4.1; c.fog = 6;
        c.lit = rgb(0x505A6E); c.shade = rgb(0x090B10); c.thickDark = 0.75; c.silver = 0; c.lightSide = 1.6;
        s.c1 = c;
        s.sun = azel(40, 25);
        s.mist = rgb(0x1E232C); s.mistK = 0.55; s.mistW = 0.05;
        s.ground = rgb(0x07090C); s.groundFar = rgb(0x1A1E26); s.groundFall = 0.2;
        return s;
    }

    static Sky bloodMoon() {
        Night s = new Night();
        s.zen = rgb(0x05030A); s.hor = rgb(0x2A1012); s.hW = 0.22;
        s.air = rgb(0x3A0E08); s.airK = 0.3; s.airW = 0.05;
        s.mwN = norm(0.2, 0.7, -0.68); s.mwCore = coreOn(s.mwN, norm(-0.8, 0.3, 0.1)); s.mwK = 0.02;
        Body m = new Body(azel(160, 24), 0.09, 25);
        m.glow = 0.035; m.glowW = 2.5; m.albedo = 0.6; m.seed = 3; m.grad = norm(0.3, 1, 0.2); m.earthshine = 0.0;
        s.moon = m; s.moonTint = rgb(0xFF5A2C); s.moonBright = 1.5;
        s.mist = rgb(0x2A0C0C); s.mistK = 0.5; s.mistW = 0.04;
        s.stars = 50000;
        s.ground = rgb(0x0C0506); s.groundFar = rgb(0x24100E); s.groundFall = 0.2;
        return s;
    }

    static Sky eclipse() {
        final double[] sunD = azel(165, 36);
        final double R = 0.034;
        final double[] cor = rgb(0xEFEAFF), prom = rgb(0xFF3A6A), band = rgb(0xFF9A4A), band2 = rgb(0xE8C890);
        final double[][] fr = frame(sunD, 0);
        Night s = new Night() {
            @Override
            void extra(double x, double y, double z, C c) {
                // 360 degree sunset on the horizon (outside the umbra)
                double yy = Math.max(0, y);
                double hl = Math.sqrt(x * x + z * z) + 1e-9;
                double var = 0.6 + 0.4 * fbmL(x / hl * 2, 1.1, z / hl * 2, 3) + 0.3 * ((x * sunD[0] + z * sunD[2]) / hl);
                add(c, band, 0.55 * var * Math.exp(-yy / 0.035));
                add(c, band2, 0.12 * Math.exp(-yy / 0.12));
                double cosA = x * sunD[0] + y * sunD[1] + z * sunD[2];
                if (cosA < 0.6) return;
                double ang = Math.sqrt(Math.max(0, 2 * (1 - cosA)));
                double r = ang / R;
                double px = x * fr[0][0] + y * fr[0][1] + z * fr[0][2], py = x * fr[1][0] + y * fr[1][1] + z * fr[1][2];
                double pl = Math.sqrt(px * px + py * py) + 1e-12;
                double ca = px / pl, sa = py / pl;
                if (r > 0.99) {
                    double st = 0.45 + 0.9 * Math.max(0, fbmL(ca * 2.5, sa * 2.5, r * 0.35, 4) + 0.2);
                    double st2 = Math.pow(Math.abs(noise(ca * 9, sa * 9, 1.3)), 0.5);
                    double corona = 1.4 * Math.pow(r, -2.6) * (0.3 + st * 0.9 + 0.3 * st2) + 0.05 * Math.pow(r, -1.0);
                    add(c, cor, corona * smooth(0.99, 1.02, r));
                    // prominences
                    if (r < 1.12) {
                        double p = smooth(0.25, 0.55, fbmL(ca * 6 + 4, sa * 6, 2.2, 3)) * smooth(1.12, 1.0, r);
                        add(c, prom, p * 2.2);
                    }
                    // diamond ring bead
                    double bx = ca - Math.cos(2.3), by = sa - Math.sin(2.3);
                    double bd = (bx * bx + by * by) * 60 + (r - 1) * (r - 1) * 900;
                    add(c, cor, 12 * Math.exp(-bd) + 0.25 * Math.exp(-(bx * bx + by * by) * 3 - (r - 1) * 1.2));
                }
                double e = smooth(1.0, 0.985, r);
                c.r *= 1 - e * 0.97; c.g *= 1 - e * 0.97; c.b *= 1 - e * 0.97;
                c.vis *= 1 - e;
            }
        };
        s.zen = rgb(0x0B1532); s.hor = rgb(0x24324E); s.hW = 0.3;
        s.stars = 30000; s.starDens = 0.6;
        s.ground = rgb(0x07080C); s.groundFar = rgb(0x2A2226); s.groundFall = 0.07;
        return s;
    }

    static Sky winter() {
        Night s = new Night();
        s.zen = rgb(0x030918); s.hor = rgb(0x1C3452); s.hW = 0.28;
        Body m = new Body(azel(205, 34), 0.018, 0);
        m.glow = 0.07; m.glowW = 2.2; m.albedo = 0.9; m.seed = 1.5;
        s.moon = m; s.moonTint = rgb(0xEAF0FF); s.moonBright = 2.2;
        s.halo22 = 0.035;
        Clouds c = new Clouds();
        c.height = 3; c.scale = 0.3; c.stretch = 4; c.cov = 0.22; c.soft = 0.35; c.warp = 1.5; c.seed = 3.7; c.oct = 5;
        c.lit = rgb(0x4A5C7E); c.shade = rgb(0x121C30); c.thickDark = 0.2; c.silver = 1.0; c.opacity = 0.6; c.fog = 25;
        s.c1 = c; s.cloudLight = rgb(0x8098C8);
        s.mist = rgb(0x2E4868); s.mistK = 0.55; s.mistW = 0.04;
        s.stars = 70000; s.starDens = 0.9;
        s.ground = rgb(0x2C3A52); s.groundFar = rgb(0x405A7C); s.groundFall = 0.3; s.groundNoise = 0.35;
        return s;
    }

    static Sky twilight() {
        Night s = new Night() {
            @Override
            void moteColor(Random r, double[] o) {
                double t = r.nextDouble();
                if (t < 0.55) { o[0] = 1.3; o[1] = 0.95; o[2] = 0.45; }
                else if (t < 0.8) { o[0] = 0.45; o[1] = 1.0; o[2] = 1.3; }
                else { o[0] = 1.25; o[1] = 0.55; o[2] = 1.2; }
            }

            @Override
            double moteDensity(double x, double y, double z) { return 0.08 + 0.92 * Math.exp(-Math.abs(y + 0.04) / 0.14); }
        };
        s.zen = rgb(0x080620); s.hor = rgb(0x8E4E8E); s.hW = 0.12;
        s.air = rgb(0x3A1E70); s.airK = 0.25; s.airW = 0.35;
        s.lp = rgb(0xFF9A7A); s.lpDir = norm(-1, 0, -0.3); s.lpK = 0.6;
        Body m = new Body(azel(250, 30), 0.03, 0);
        m.glow = 0.03; m.glowW = 1.5; m.albedo = 0.9; m.seed = 5;
        s.moon = m; s.moonTint = rgb(0xFFF0DA); s.moonBright = 2.0; s.moonLight = azel(290, -10);
        Clouds c = new Clouds();
        c.height = 1.4; c.scale = 0.5; c.stretch = 2.5; c.cov = 0.16; c.soft = 0.3; c.warp = 1.4; c.seed = 11.2; c.fog = 12;
        c.lit = rgb(0xE08AC0); c.shade = rgb(0x2E2060); c.thickDark = 0.35; c.silver = 0.5; c.opacity = 0.85;
        s.c1 = c; s.cloudLight = rgb(0xFFB0D0);
        s.sun = azel(290, -10);
        s.stars = 50000; s.starDens = 0.8;
        s.motes = 1400; s.moteBright = 0.7; s.moteSize = 1.4;
        s.ground = rgb(0x0A0818); s.groundFar = rgb(0x3A2452); s.groundFall = 0.2;
        return s;
    }

    // ================================================================== space presets

    static Sky nebula() {
        final double[] a = rgb(0x3A3AC8), b = rgb(0xB03AB8), cc = rgb(0x7AD0FF), core = rgb(0xFFE6D0), bg = rgb(0x02030A);
        final double[] mwN = norm(0.2, 0.9, 0.3);
        Space s = new Space() {
            @Override
            void sky(double x, double y, double z, C c) {
                set(c, bg);
                milky(c, x, y, z, mwN, coreOn(mwN, norm(1, 0, 0)), 0.02);
                nebulaLayer(c, x, y, z, 0.0, 1.0, a, b, cc, core, 0.5, 0.85);
            }

            @Override
            double starDensity(double x, double y, double z) { return 0.3 + 0.5 * milkyDensity(x, y, z, mwN); }
        };
        s.stars = 90000; s.detail = 0.8;
        return s;
    }

    static Sky redNebula() {
        final double[] a = rgb(0xC0202A), b = rgb(0xFF7A30), cc = rgb(0xFFB070), core = rgb(0xFFF0C8), bg = rgb(0x040204);
        final double[] cl = azel(30, 15), blue = rgb(0x9AC0FF);
        Space s = new Space() {
            @Override
            void sky(double x, double y, double z, C c) {
                set(c, bg);
                nebulaLayer(c, x, y, z, 4.2, 1.1, a, b, cc, core, 0.55, 0.9);
                // young blue cluster glow
                double cosA = x * cl[0] + y * cl[1] + z * cl[2];
                double a2 = 2 * (1 - cosA);
                add(c, blue, 0.35 * Math.exp(-a2 / 0.004) + 0.08 * Math.exp(-a2 / 0.05));
                // dark globules
                double g = fbmL(x * 2.2 + 2, y * 2.2, z * 2.2 + 9, 5);
                double k = smooth(0.28, 0.45, g);
                mul(c, 1 - 0.9 * k);
                c.vis *= 1 - 0.95 * k;
            }

            @Override
            double starDensity(double x, double y, double z) {
                double cosA = x * cl[0] + y * cl[1] + z * cl[2];
                return Math.min(1, 0.3 + 0.9 * Math.exp(-2 * (1 - cosA) / 0.02));
            }
        };
        s.stars = 80000; s.detail = 0.9;
        return s;
    }

    static Sky planets() {
        final double[] bg = rgb(0x010208), neb1 = rgb(0x1A3C8A), neb2 = rgb(0x5A1E7A);
        final double[] P = azel(35, 22);
        final double pr = 0.26, Rp = Math.sin(pr);
        final double[] Nr = norm(0.25, 0.95, 0.35);           // ring plane normal
        final double[] light = azel(250, 25);
        final double[] c1 = rgb(0xE8D2A8), c2 = rgb(0xC08A5A), c3 = rgb(0x8A5A3A), c4 = rgb(0xF2E8D0), ringC = rgb(0xD8C8A8), atm = rgb(0xFFE2B0);
        final Body m1 = new Body(azel(300, 34), 0.045, 0), m2 = new Body(azel(95, 55), 0.02, 0), m3 = new Body(azel(200, 12), 0.012, 0);
        m1.albedo = 0.7; m1.seed = 2; m2.albedo = 0.9; m2.seed = 7; m3.albedo = 0.5; m3.seed = 11;
        final double[] m1t = rgb(0xD8D0C8), m2t = rgb(0xCFE4FF), m3t = rgb(0xFF9A70);
        Space s = new Space() {
            @Override
            void sky(double x, double y, double z, C c) {
                set(c, bg);
                double n = smooth(0.1, 0.8, fbmL(x * 2 + 4, y * 2, z * 2, 5));
                add(c, neb1, n * 0.12);
                add(c, neb2, smooth(0.3, 0.9, fbmL(x * 3, y * 3 + 2, z * 3, 4)) * 0.1);
                moon(c, x, y, z, m1, m1t, 1.3, light);
                moon(c, x, y, z, m2, m2t, 1.4, light);
                moon(c, x, y, z, m3, m3t, 1.2, light);
                ringed(c, x, y, z);
            }

            void ringed(C c, double x, double y, double z) {
                double b = x * P[0] + y * P[1] + z * P[2];
                if (b < 0.3) return;
                // sphere
                double disc = b * b - (1 - Rp * Rp);
                double tP = Double.MAX_VALUE, edge = 0;
                double hx = 0, hy = 0, hz = 0;
                double rim = Math.sqrt(Math.max(0, disc)) / Rp;
                if (disc > -0.0004 * Rp) {
                    edge = smooth(-0.004, 0.012, disc / Rp);
                    tP = b - Math.sqrt(Math.max(0, disc));
                    hx = x * tP; hy = y * tP; hz = z * tP;
                }
                // ring intersection
                double dn = x * Nr[0] + y * Nr[1] + z * Nr[2];
                double tR = Math.abs(dn) > 1e-5 ? (P[0] * Nr[0] + P[1] * Nr[1] + P[2] * Nr[2]) / dn : -1;
                double ringA = 0, ringL = 0;
                if (tR > 0) {
                    double rx = x * tR - P[0], ry = y * tR - P[1], rz = z * tR - P[2];
                    double rr = Math.sqrt(rx * rx + ry * ry + rz * rz) / Rp;
                    if (rr > 1.25 && rr < 2.35) {
                        ringA = ringDensity(rr);
                        // planet shadow on the rings
                        double ox = x * tR, oy = y * tR, oz = z * tR;
                        double sh = sphereHit(ox, oy, oz, light, P, Rp);
                        double lam = 0.35 + 0.65 * Math.abs(light[0] * Nr[0] + light[1] * Nr[1] + light[2] * Nr[2]);
                        ringL = lam * (1 - 0.92 * sh);
                    }
                }
                boolean ringFront = ringA > 0 && tR < tP;
                if (edge > 0) {
                    double nx = (hx - P[0]) / Rp, ny = (hy - P[1]) / Rp, nz = (hz - P[2]) / Rp;
                    double lat = nx * Nr[0] + ny * Nr[1] + nz * Nr[2];
                    double w = fbmL(nx * 3, ny * 3, nz * 3, 4) * 0.12;
                    double bands = Math.sin((lat + w) * 22) * 0.5 + 0.5;
                    double bands2 = Math.sin((lat + w * 1.5) * 57 + 1.3) * 0.5 + 0.5;
                    double storm = fbmL(nx * 8, ny * 8 + 3, nz * 8, 4);
                    double[] ca = bands > 0.5 ? c1 : c2;
                    double t = Math.abs(bands - 0.5) * 2;
                    double r0 = mix(c2[0], ca[0], t), g0 = mix(c2[1], ca[1], t), b0 = mix(c2[2], ca[2], t);
                    r0 = mix(r0, c3[0], bands2 * 0.35); g0 = mix(g0, c3[1], bands2 * 0.35); b0 = mix(b0, c3[2], bands2 * 0.35);
                    double st = smooth(0.3, 0.5, storm) * 0.5;
                    r0 = mix(r0, c4[0], st); g0 = mix(g0, c4[1], st); b0 = mix(b0, c4[2], st);
                    double ld = nx * light[0] + ny * light[1] + nz * light[2];
                    double lam = smooth(-0.1, 0.3, ld) * (0.25 + 0.75 * Math.max(0, ld));
                    // ring shadow on the planet
                    double dnl = light[0] * Nr[0] + light[1] * Nr[1] + light[2] * Nr[2];
                    if (Math.abs(dnl) > 1e-4) {
                        double st2 = ((P[0] - hx) * Nr[0] + (P[1] - hy) * Nr[1] + (P[2] - hz) * Nr[2]) / dnl;
                        if (st2 > 0) {
                            double qx = hx + light[0] * st2 - P[0], qy = hy + light[1] * st2 - P[1], qz = hz + light[2] * st2 - P[2];
                            double qr = Math.sqrt(qx * qx + qy * qy + qz * qz) / Rp;
                            if (qr > 1.25 && qr < 2.35) lam *= 1 - 0.8 * ringDensity(qr);
                        }
                    }
                    double fres = Math.pow(1 - Math.min(1, rim), 4);
                    double k = 1.3 * lam;
                    double pr0 = r0 * k + atm[0] * fres * lam * 0.6, pg0 = g0 * k + atm[1] * fres * lam * 0.6, pb0 = b0 * k + atm[2] * fres * lam * 0.6;
                    pr0 += 0.004; pg0 += 0.004; pb0 += 0.006;
                    c.r += (pr0 - c.r) * edge; c.g += (pg0 - c.g) * edge; c.b += (pb0 - c.b) * edge;
                    c.vis *= 1 - edge;
                }
                if (ringA > 0 && (ringFront || edge < 1)) {
                    double a = ringA * (ringFront ? 1 : (1 - edge));
                    double k = 1.2 * ringL;
                    c.r += (ringC[0] * k - c.r) * a; c.g += (ringC[1] * k - c.g) * a; c.b += (ringC[2] * k - c.b) * a;
                    c.vis *= 1 - a;
                }
            }
        };
        s.stars = 70000;
        return s;
    }

    static double ringDensity(double rr) {
        double d = 0.35 + 0.35 * noise(rr * 18, 1.1, 0.5) + 0.2 * noise(rr * 55, 2.3, 0.1);
        d *= smooth(1.25, 1.32, rr) * smooth(2.35, 2.25, rr);
        d *= 1 - 0.9 * smooth(0.035, 0.0, Math.abs(rr - 1.95));     // Cassini division
        d *= 1 - 0.5 * smooth(1.55, 1.45, rr);                         // faint inner C ring
        return clamp01(d * 1.4);
    }

    /** Soft test whether the ray (origin o, dir l) hits sphere (center p, radius R): 0..1. */
    static double sphereHit(double ox, double oy, double oz, double[] l, double[] p, double R) {
        double vx = p[0] - ox, vy = p[1] - oy, vz = p[2] - oz;
        double t = vx * l[0] + vy * l[1] + vz * l[2];
        if (t < 0) return 0;
        double d2 = vx * vx + vy * vy + vz * vz - t * t;
        return smooth(R * R * 1.03, R * R * 0.97, d2);
    }

    static Sky spiral() {
        final double[] G = azel(20, 28);
        final double[][] fr = frame(G, 35);
        final double Rg = 0.95, inc = Math.cos(Math.toRadians(58));
        final double[] bulgeC = rgb(0xFFE6BC), armC = rgb(0xA8C4FF), hii = rgb(0xFF6AB0), dustC = rgb(0x1A0E08), bg = rgb(0x020308);
        final double[] G2 = azel(210, -30);
        final double[][] fr2 = frame(G2, -20);
        final double[] neb = rgb(0x3A1E6A);
        Space s = new Space() {
            @Override
            void sky(double x, double y, double z, C c) {
                set(c, bg);
                add(c, neb, 0.12 * smooth(0.1, 0.8, fbmL(x * 2 + 1, y * 2, z * 2 + 5, 5)));
                galaxy(c, x, y, z, G, fr, Rg, inc, 1.0, 0.0);
                galaxy(c, x, y, z, G2, fr2, 0.16, Math.cos(Math.toRadians(70)), 0.6, 3.0);
            }

            void galaxy(C c, double x, double y, double z, double[] g, double[][] f, double R, double inc, double K, double seed) {
                double cosA = x * g[0] + y * g[1] + z * g[2];
                if (cosA < Math.cos(R * 1.6)) return;
                double px = x * f[0][0] + y * f[0][1] + z * f[0][2], py = x * f[1][0] + y * f[1][1] + z * f[1][2];
                double a = px / R, bq = py / (R * inc);
                double r = Math.sqrt(a * a + bq * bq) + 1e-9;
                if (r > 1.6) return;
                double th = Math.atan2(bq, a);
                double warp = fbmL(a * 3 + seed, bq * 3, 1.7 + seed, 4);
                double phase = th - Math.log(r) / 0.32 + warp * 0.9;
                double arm = Math.pow(0.5 + 0.5 * Math.cos(2 * phase), 2.5);
                double armD = Math.pow(0.5 + 0.5 * Math.cos(2 * (phase + 0.45)), 6);
                double disk = Math.exp(-r / 0.3) * smooth(1.4, 0.6, r);
                double bulge = Math.exp(-(r * r) / 0.006) * 3 + Math.exp(-r / 0.06) * 1.0;
                double grain = 0.75 + 0.5 * fbmL(a * 25 + seed, bq * 25, 3.3, 3);
                double lumA = disk * (0.25 + 1.4 * arm * grain);
                double knots = smooth(0.25, 0.55, fbmL(a * 14, bq * 14 + seed, 7.1, 3)) * arm * disk * 2;
                double dust = armD * smooth(0.05, 0.25, r) * smooth(1.1, 0.4, r) * 0.75;
                double k = 0.45 * K;
                c.r += k * ((armC[0] * lumA + bulgeC[0] * bulge) + hii[0] * knots * 0.5);
                c.g += k * ((armC[1] * lumA + bulgeC[1] * bulge) + hii[1] * knots * 0.5);
                c.b += k * ((armC[2] * lumA + bulgeC[2] * bulge) + hii[2] * knots * 0.5);
                double dd = 1 - dust;
                c.r = c.r * dd + dustC[0] * dust * 0.2; c.g = c.g * dd + dustC[1] * dust * 0.2; c.b = c.b * dd + dustC[2] * dust * 0.2;
                c.vis *= 1 - 0.6 * smooth(0.0, 0.6, disk + bulge * 0.3);
            }
        };
        s.stars = 70000;
        return s;
    }

    static Sky blackHole() {
        final double[] B = azel(0, 18);
        final double[][] fr = frame(B, -8);
        final double rs = 0.10, E = rs * 1.9;
        final double inc = Math.cos(Math.toRadians(76));
        final double[] mwN = norm(0.1, 0.5, 0.86);
        final double[] neb = rgb(0x2A2A7A), neb2 = rgb(0x6A2A5A), bg = rgb(0x010106);
        final double[] hot = rgb(0xFFF4E0), warm = rgb(0xFFA040), cool = rgb(0xC0D8FF);
        Space s = new Space() {
            void background(double x, double y, double z, C c) {
                set(c, bg);
                milky(c, x, y, z, mwN, coreOn(mwN, B), 0.04);
                add(c, neb, 0.15 * smooth(0.0, 0.8, fbmL(x * 2.2 + 5, y * 2.2, z * 2.2, 5)));
                add(c, neb2, 0.1 * smooth(0.2, 0.9, fbmL(x * 3.1, y * 3.1 + 5, z * 3.1, 5)));
            }

            @Override
            void sky(double x, double y, double z, C c) {
                double cosA = x * B[0] + y * B[1] + z * B[2];
                double px = x * fr[0][0] + y * fr[0][1] + z * fr[0][2], py = x * fr[1][0] + y * fr[1][1] + z * fr[1][2];
                double th = Math.acos(Math.max(-1, Math.min(1, cosA)));
                double pl = Math.sqrt(px * px + py * py) + 1e-12;
                double ux = px / pl, uy = py / pl;
                // gravitational lensing of the background
                double q = th / rs;
                if (q > 1.0) {
                    double th2 = th - E * E / th;
                    double st = Math.sin(th2), ct = Math.cos(th2);
                    double dx = B[0] * ct + (fr[0][0] * ux + fr[1][0] * uy) * st;
                    double dy = B[1] * ct + (fr[0][1] * ux + fr[1][1] * uy) * st;
                    double dz = B[2] * ct + (fr[0][2] * ux + fr[1][2] * uy) * st;
                    background(dx, dy, dz, c);
                } else { c.r = c.g = c.b = 0; }
                c.vis *= smooth(1.6, 3.0, q);
                // shadow edge darkening + photon ring
                double shadow = smooth(1.03, 0.99, q);
                mul(c, 1 - shadow);
                double pr = (q - 1.06) / 0.035;
                double ring = Math.exp(-pr * pr);
                // lensed image of the far side of the disk (arc around the shadow)
                double lq = (q - 1.3) / (0.16 + 0.22 * Math.abs(uy));
                double arc = Math.exp(-lq * lq) * (0.35 + 0.65 * uy * uy) * smooth(1.02, 1.12, q);
                double beamL = 1 + 0.4 * ux;
                double texA = 0.6 + 0.4 * fbmL(ux * 4, uy * 4, q * 6, 3);
                double hk = 1.5 * arc * texA * beamL * beamL + 2.2 * ring * beamL;
                double tr = hot[0] * 0.6 + warm[0] * 0.4, tg = hot[1] * 0.6 + warm[1] * 0.4, tb = hot[2] * 0.6 + warm[2] * 0.4;
                c.r += tr * hk; c.g += tg * hk; c.b += tb * hk;
                // primary accretion disk (nearly edge-on)
                double a = px / rs, bq = py / (rs * inc);
                double rd = Math.sqrt(a * a + bq * bq);
                boolean front = py < 0;
                if (rd > 2.2 && rd < 7.5 && (front || q > 1.02)) {
                    double phi = Math.atan2(bq, a);
                    double swirl = phi + 3.0 / Math.sqrt(rd);
                    double tex = 0.45 + 0.55 * smooth(-0.4, 0.5, fbmL(Math.cos(swirl) * rd * 0.9, Math.sin(swirl) * rd * 0.9, rd * 0.4, 5));
                    double streak = 0.75 + 0.25 * noise(rd * 3.5, 0.3, 0.7);
                    double prof = Math.pow(2.6 / rd, 2.0) * smooth(2.2, 2.9, rd) * smooth(7.5, 4.5, rd);
                    double dop = 1 + 0.4 * (a / rd);
                    double I = prof * tex * streak * dop * dop * dop * 1.6;
                    double tt = smooth(2.5, 7.5, rd);
                    double cr = mix(hot[0], warm[0], tt), cg = mix(hot[1], warm[1], tt), cb = mix(hot[2], warm[2], tt);
                    double bl = clamp01((dop - 1) * 0.8);
                    cr = mix(cr, cool[0], bl * 0.5); cg = mix(cg, cool[1], bl * 0.5); cb = mix(cb, cool[2], bl * 0.5);
                    double thick = smooth(1.0, 0.0, Math.abs(bq) / Math.max(1e-6, rd) * 0);
                    double al = clamp01(I * 1.4) * thick;
                    c.r = c.r * (1 - al * 0.5) + cr * I; c.g = c.g * (1 - al * 0.5) + cg * I; c.b = c.b * (1 - al * 0.5) + cb * I;
                    c.vis *= 1 - al;
                }
                // soft glow around everything
                add(c, warm, 0.025 * Math.exp(-Math.max(0, q - 1) / 1.2) * smooth(0.97, 1.05, q));
            }

            @Override
            double starDensity(double x, double y, double z) {
                double cosA = x * B[0] + y * B[1] + z * B[2];
                double th = Math.acos(Math.max(-1, Math.min(1, cosA)));
                return (0.3 + 0.5 * milkyDensity(x, y, z, mwN)) * smooth(rs * 1.5, rs * 3.0, th);
            }
        };
        s.stars = 80000; s.detail = 0.85;
        return s;
    }

    static Sky voidSky() {
        final double[] bg = rgb(0x06020C), v1 = rgb(0x5A1E8A), v2 = rgb(0xC050FF), v3 = rgb(0x1A0A3A), eye = rgb(0xE0B0FF);
        Space s = new Space() {
            @Override
            void sky(double x, double y, double z, C c) {
                set(c, bg);
                double wx = fbmL(x * 1.5 + 2, y * 1.5, z * 1.5, 3), wy = fbmL(x * 1.5, y * 1.5 + 6, z * 1.5, 3), wz = fbmL(x * 1.5, y * 1.5, z * 1.5 + 4, 3);
                double X = x + wx * 0.9, Y = y + wy * 0.9, Z = z + wz * 0.9;
                double n = fbmL(X * 2.5, Y * 2.5, Z * 2.5, 6);
                double r = ridged(X * 3.5 + 1, Y * 3.5, Z * 3.5, 5);
                double swirl = smooth(0.0, 0.6, n);
                double fil = smooth(0.6, 0.95, r) * swirl;
                add(c, v3, 0.5 * swirl);
                add(c, v1, 0.35 * swirl * swirl + 0.25 * fil);
                add(c, v2, 0.5 * fil * fil);
                // faint horizonless vertical gradient: darker above, violet haze below (the void)
                double below = smooth(0.2, -0.9, y);
                add(c, v1, 0.07 * below);
                // distant bright "eye"
                double[] E = EYE;
                double cosA = x * E[0] + y * E[1] + z * E[2];
                double a2 = 2 * (1 - cosA);
                add(c, eye, 0.9 * Math.exp(-a2 / 0.0015) + 0.1 * Math.exp(-a2 / 0.03));
            }

            @Override
            void moteColor(Random r, double[] o) {
                double t = r.nextDouble();
                if (t < 0.7) { o[0] = 0.9; o[1] = 0.5; o[2] = 1.3; } else { o[0] = 0.6; o[1] = 0.9; o[2] = 1.2; }
            }

            @Override
            double moteDensity(double x, double y, double z) { return 1; }
        };
        s.stars = 30000; s.starMax = 1.6;
        s.motes = 700; s.moteBright = 0.35; s.moteSize = 1.2;
        return s;
    }

    private static final double[] EYE = azel(300, 40);

    /** Direction in the plane with normal N closest to hint (galactic core). */
    static double[] coreOn(double[] N, double[] hint) {
        double d = hint[0] * N[0] + hint[1] * N[1] + hint[2] * N[2];
        return norm(hint[0] - N[0] * d, hint[1] - N[1] * d, hint[2] - N[2] * d);
    }
}
