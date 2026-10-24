package dev.lego.visual.sky;

import static dev.lego.visual.sky.SkyGen.*;

/** Day and sunset presets for {@link SkyGen}. */
final class SkyGen2 {
    private SkyGen2() { }

    static Sky make(String id) {
        switch (id) {
            case "summer": return summer();
            case "sunset": return sunset();
            case "goldenhour": return goldenHour();
            case "cottoncandy": return cottonCandy();
            case "sunrise": return sunrise();
            case "overcast": return overcast();
            case "thunder": return thunder();
            case "anime": return anime();
            case "alien": return alien();
            case "synthwave": return synthwave();
            case "oceandusk": return oceanDusk();
            default: return null;
        }
    }

    /** Generic physically-inspired daylight / twilight sky with sun, glow, clouds, towers and mist. */
    static class Day extends Sky {
        double[] zen = rgb(0x2B67C8), hor = rgb(0xC4DAF0), horSun = null, horAnti = null;
        double hW = 0.26, hW2 = 0.0;       // gradient width (second, wider component optional)
        double[] mid = null; double midW = 0.5;
        double sunR = 0.0095, disk = 40, b1 = 0.22, w1 = 0.14, b2 = 1.4, w2 = 0.03;
        double[] glow = null; double glowK = 0, glowPow = 6, glowH = 0.12;
        double[] belt = null, shadow = null; double beltK = 0, shadowK = 0;   // belt of Venus / earth shadow (anti-sun)
        double[] mist = null; double mistW = 0.04, mistK = 0;
        double[] post = null; double postK = 0, postW = 0.25;                  // glow drawn over the clouds (overcast sun)
        Clouds c1, c2;
        Towers tw;
        SkyGen3.Body moon;
        double[] moonTint = rgb(0xFFFFFF);

        @Override
        void sky(double x, double y, double z, C c) {
            double yy = y > 0 ? y : 0;
            double hl = Math.sqrt(x * x + z * z) + 1e-9;
            double shl = Math.sqrt(sun[0] * sun[0] + sun[2] * sun[2]) + 1e-9;
            double sh = (x * sun[0] + z * sun[2]) / (hl * shl);
            double s01 = 0.5 + 0.5 * sh;
            double hr = hor[0], hg = hor[1], hb = hor[2];
            if (horSun != null) {
                double a = s01 * s01;
                double[] lo = horAnti != null ? horAnti : hor;
                hr = lo[0] + (horSun[0] - lo[0]) * a; hg = lo[1] + (horSun[1] - lo[1]) * a; hb = lo[2] + (horSun[2] - lo[2]) * a;
            }
            double t = Math.exp(-yy / hW);
            if (hW2 > 0) t = 0.6 * t + 0.4 * Math.exp(-yy / hW2);
            double zr = zen[0], zg = zen[1], zb = zen[2];
            if (mid != null) {
                double m = Math.exp(-yy / midW);
                zr += (mid[0] - zr) * m; zg += (mid[1] - zg) * m; zb += (mid[2] - zb) * m;
            }
            c.r = zr + (hr - zr) * t; c.g = zg + (hg - zg) * t; c.b = zb + (hb - zb) * t;
            double anti = (1 - s01) * (1 - s01);
            if (shadow != null) mixTo(c, shadow, shadowK * anti * Math.exp(-yy / 0.035));
            if (belt != null) {
                double q = (yy - 0.09) / 0.07;
                add(c, belt, beltK * anti * Math.exp(-q * q));
            }
            if (glow != null) {
                double sp = Math.pow(s01, glowPow);
                add(c, glow, glowK * sp * Math.exp(-yy / glowH));
            }
            double cosA = x * sun[0] + y * sun[1] + z * sun[2];
            if (disk > 0 || b1 > 0 || b2 > 0) sunDisk(c, cosA, sunR, sunCol, disk, b1, w1, b2, w2);
            if (moon != null) SkyGen3.moon(c, x, y, z, moon, moonTint, 1.0, sun);
            extra(x, y, z, c);
            if (tw != null) towers(c, x, y, z, tw, sun);
            if (c2 != null) clouds(c, x, y, z, c2, sun, sunCol);
            if (c1 != null) clouds(c, x, y, z, c1, sun, sunCol);
            if (post != null && cosA > 0) {
                double a2 = 2 * (1 - cosA);
                add(c, post, postK * Math.exp(-a2 / (postW * postW)));
            }
            if (mist != null) mixTo(c, mist, mistK * Math.exp(-yy / mistW));
        }

        /** Hook for additional features painted before the clouds. */
        void extra(double x, double y, double z, C c) { }
    }

    // ------------------------------------------------------------------ presets

    static Sky summer() {
        Day s = new Day();
        s.kind = "day";
        s.sun = azel(150, 58);
        s.sunCol = rgb(0xFFF6E4);
        s.zen = rgb(0x2560C4); s.hor = rgb(0xC9DEF2); s.hW = 0.22; s.hW2 = 0.55;
        s.disk = 45; s.b1 = 0.25; s.w1 = 0.16; s.b2 = 1.3; s.w2 = 0.03;
        Clouds c = new Clouds();
        c.height = 1; c.scale = 0.75; c.cov = 0.10; c.soft = 0.20; c.warp = 0.9; c.seed = 2.3; c.fog = 14;
        c.lit = mulc(rgb(0xFFFFFF), 1.15); c.shade = rgb(0x7D8CA6); c.thickDark = 0.5;
        s.c1 = c;
        Clouds ci = new Clouds();
        ci.height = 3; ci.scale = 0.35; ci.stretch = 3.5; ci.cov = 0.18; ci.soft = 0.35; ci.warp = 1.4; ci.seed = 8.1; ci.oct = 5;
        ci.lit = rgb(0xF4F8FF); ci.shade = rgb(0xC5D3E8); ci.opacity = 0.55; ci.thickDark = 0.1; ci.fog = 30;
        s.c2 = ci;
        Towers t = new Towers();
        t.h0 = 0.01; t.h1 = 0.10; t.freq = 2.6; t.cut = 0.05; t.bill = 9; t.seed = 4.2;
        t.lit = mulc(rgb(0xFFFFFF), 1.1); t.shade = rgb(0x8394B0);
        s.tw = t;
        s.mist = rgb(0xD6E4F2); s.mistK = 0.35; s.mistW = 0.03;
        s.ground = rgb(0x3F5160); s.groundFar = rgb(0xA9BDD0); s.groundFall = 0.25;
        return s;
    }

    static Sky sunset() {
        Day s = new Day();
        s.kind = "sunset";
        s.sun = azel(0, 2.2);
        s.sunCol = rgb(0xFFD7A0);
        s.zen = rgb(0x26306E); s.mid = rgb(0x8A5A9A); s.midW = 0.35;
        s.hor = rgb(0xE2786A); s.horSun = rgb(0xFF9A3C); s.horAnti = rgb(0xB06C8A); s.hW = 0.10;
        s.glow = rgb(0xFFB060); s.glowK = 1.4; s.glowPow = 5; s.glowH = 0.09;
        s.belt = rgb(0xE38AA0); s.beltK = 0.25; s.shadow = rgb(0x3A3F6A); s.shadowK = 0.5;
        s.sunR = 0.014; s.disk = 22; s.b1 = 0.9; s.w1 = 0.10; s.b2 = 2.5; s.w2 = 0.035;
        Clouds c = new Clouds();
        c.height = 1.3; c.scale = 0.9; c.cov = 0.05; c.soft = 0.28; c.warp = 1.1; c.seed = 5.7; c.fog = 10; c.stretch = 1.8;
        c.lit = rgb(0xFFA46A); c.shade = rgb(0x5B3A68); c.thickDark = 0.6; c.silver = 2.5; c.lightSide = 1.3;
        s.c1 = c;
        Clouds ci = new Clouds();
        ci.height = 3; ci.scale = 0.3; ci.stretch = 4; ci.cov = 0.15; ci.soft = 0.35; ci.seed = 1.9; ci.oct = 5; ci.warp = 1.5;
        ci.lit = rgb(0xFFB690); ci.shade = rgb(0xB06A8A); ci.opacity = 0.7; ci.thickDark = 0.1; ci.fog = 25;
        s.c2 = ci;
        s.below = OCEAN;
        s.water = rgb(0x120C24); s.waves = 1.0; s.glitter = 2.2; s.rough = 0.09;
        return s;
    }

    static Sky goldenHour() {
        Day s = new Day();
        s.kind = "sunset";
        s.sun = azel(250, 7);
        s.sunCol = rgb(0xFFE2A8);
        s.zen = rgb(0x3561A8); s.hor = rgb(0xE9D6BE); s.horSun = rgb(0xFFCB7E); s.horAnti = rgb(0xB9C6DA); s.hW = 0.2; s.hW2 = 0.5;
        s.glow = rgb(0xFFC070); s.glowK = 0.9; s.glowPow = 4; s.glowH = 0.18;
        s.sunR = 0.011; s.disk = 30; s.b1 = 0.55; s.w1 = 0.14; s.b2 = 2.0; s.w2 = 0.035;
        Clouds c = new Clouds();
        c.height = 1; c.scale = 0.8; c.cov = 0.14; c.soft = 0.22; c.warp = 1.0; c.seed = 3.3; c.fog = 12;
        c.lit = mulc(rgb(0xFFD49A), 1.25); c.shade = rgb(0x6E6A8A); c.thickDark = 0.55; c.silver = 2.0; c.lightSide = 1.4;
        s.c1 = c;
        Clouds ci = new Clouds();
        ci.height = 3.5; ci.scale = 0.28; ci.stretch = 5; ci.cov = 0.12; ci.soft = 0.4; ci.seed = 6.1; ci.oct = 5; ci.warp = 1.6;
        ci.lit = rgb(0xFFE0B0); ci.shade = rgb(0xC9B4B4); ci.opacity = 0.6; ci.thickDark = 0.1; ci.fog = 28;
        s.c2 = ci;
        s.below = CLOUDSEA;
        Clouds sea = new Clouds();
        sea.height = 1; sea.scale = 0.9; sea.cov = -0.3; sea.soft = 0.3; sea.warp = 1.2; sea.seed = 9.4; sea.fog = 0.06;
        sea.lit = mulc(rgb(0xFFE3B8), 1.05); sea.shade = rgb(0x8E86A0);
        s.sea = sea; s.seaGap = rgb(0x5A6282);
        return s;
    }

    static Sky cottonCandy() {
        Day s = new Day();
        s.kind = "sunset";
        s.sun = azel(290, -1.5);
        s.sunCol = rgb(0xFFC8B0);
        s.zen = rgb(0x4E5FB0); s.mid = rgb(0xA78AD0); s.midW = 0.45;
        s.hor = rgb(0xF6B6C8); s.horSun = rgb(0xFFC4A0); s.horAnti = rgb(0xD2A6D8); s.hW = 0.16;
        s.glow = rgb(0xFFB0A0); s.glowK = 0.8; s.glowPow = 4; s.glowH = 0.12;
        s.belt = rgb(0xFF9EC0); s.beltK = 0.35;
        s.disk = 0; s.b1 = 0.5; s.w1 = 0.2; s.b2 = 0.6; s.w2 = 0.06;
        Clouds c = new Clouds();
        c.height = 1.1; c.scale = 0.7; c.cov = 0.08; c.soft = 0.26; c.warp = 1.0; c.seed = 7.2; c.fog = 12;
        c.lit = mulc(rgb(0xFFB5D2), 1.2); c.shade = rgb(0x8C7CC2); c.thickDark = 0.45; c.silver = 1.5; c.lightSide = 1.2;
        s.c1 = c;
        Clouds ci = new Clouds();
        ci.height = 3; ci.scale = 0.3; ci.stretch = 3; ci.cov = 0.1; ci.soft = 0.4; ci.seed = 2.6; ci.oct = 5; ci.warp = 1.5;
        ci.lit = rgb(0xFFD0E0); ci.shade = rgb(0xC6A6DC); ci.opacity = 0.65; ci.thickDark = 0.1; ci.fog = 25;
        s.c2 = ci;
        s.below = OCEAN;
        s.water = rgb(0x1E1838); s.waves = 0.7; s.glitter = 0.6; s.rough = 0.12;
        return s;
    }

    static Sky sunrise() {
        Day s = new Day();
        s.kind = "sunset";
        s.sun = azel(90, 3.5);
        s.sunCol = rgb(0xFFE6BE);
        s.zen = rgb(0x5A82C0); s.hor = rgb(0xE6D8CC); s.horSun = rgb(0xFFD8A2); s.horAnti = rgb(0xAFBCD2); s.hW = 0.2; s.hW2 = 0.45;
        s.glow = rgb(0xFFD49A); s.glowK = 1.0; s.glowPow = 4; s.glowH = 0.15;
        s.sunR = 0.011; s.disk = 26; s.b1 = 0.8; s.w1 = 0.16; s.b2 = 2.2; s.w2 = 0.045;
        Clouds ci = new Clouds();
        ci.height = 3; ci.scale = 0.3; ci.stretch = 4; ci.cov = 0.14; ci.soft = 0.4; ci.seed = 4.4; ci.oct = 5; ci.warp = 1.6;
        ci.lit = rgb(0xFFE2C4); ci.shade = rgb(0xC0B8C8); ci.opacity = 0.6; ci.thickDark = 0.1; ci.fog = 28;
        s.c2 = ci;
        s.mist = rgb(0xF2E4D8); s.mistK = 0.5; s.mistW = 0.035;
        s.below = CLOUDSEA;
        Clouds sea = new Clouds();
        sea.height = 1; sea.scale = 0.45; sea.cov = -0.3; sea.soft = 0.55; sea.warp = 1.4; sea.seed = 2.2; sea.fog = 0.09; sea.oct = 5;
        sea.lit = rgb(0xFFE6CC); sea.shade = rgb(0xA9A4B6);
        s.sea = sea; s.seaGap = rgb(0x6E7488);
        return s;
    }

    static Sky overcast() {
        Day s = new Day();
        s.kind = "day";
        s.sun = azel(200, 35);
        s.zen = rgb(0x7E8791); s.hor = rgb(0xA9AFB6); s.hW = 0.3;
        s.disk = 0; s.b1 = 0; s.b2 = 0;
        Clouds c = new Clouds();
        c.height = 1; c.scale = 0.55; c.cov = -0.45; c.soft = 0.55; c.warp = 1.2; c.seed = 1.1; c.fog = 9; c.oct = 6;
        c.lit = rgb(0xB9BFC7); c.shade = rgb(0x575F6A); c.thickDark = 0.6; c.silver = 0.2; c.lightSide = 0.8;
        s.c2 = c;
        Clouds scud = new Clouds();
        scud.height = 0.5; scud.scale = 0.9; scud.cov = 0.12; scud.soft = 0.3; scud.warp = 1.4; scud.seed = 6.6; scud.fog = 7;
        scud.lit = rgb(0x8B929B); scud.shade = rgb(0x464C55); scud.thickDark = 0.5; scud.opacity = 0.8; scud.silver = 0;
        s.c1 = scud;
        s.post = rgb(0xFFF8EC); s.postK = 0.12; s.postW = 0.35;
        s.mist = rgb(0x9CA3AB); s.mistK = 0.6; s.mistW = 0.05;
        s.ground = rgb(0x323832); s.groundFar = rgb(0x8E959C); s.groundFall = 0.22;
        return s;
    }

    static Sky thunder() {
        Day s = new Day() {
            final double[] aLit = mulc(rgb(0xFFF2DC), 1.15), aShade = rgb(0x5A6072), rain = rgb(0x4A515E), under = rgb(0x3A4050);

            @Override
            void extra(double x, double y, double z, C c) { anvil(x, y, z, c); }

            void anvil(double x, double y, double z, C c) {
                if (y > 0.8 || y < -0.02) return;
                double az = Math.atan2(x, -z);              // 0 = north, +pi/2 = east
                double d = az - Math.toRadians(55);
                d = Math.atan2(Math.sin(d), Math.cos(d));
                if (Math.abs(d) > 1.5) return;
                double el = Math.asin(Math.max(-1, Math.min(1, y)));
                double ca = Math.cos(d), sa = Math.sin(d);
                double eTop = 0.55;
                double fl = smooth(eTop - 0.34, eTop - 0.03, el);          // anvil flare
                double shift = 0.32 * fl * fl;
                double w = 0.40 - 0.10 * smooth(0.0, 0.3, el) + (0.85 - 0.30) * fl * fl;
                double dd = d - shift;
                double qq = dd / w;
                double top = eTop + 0.035 * (1 - qq * qq) - 0.03 * Math.abs(qq) * Math.abs(qq);
                // cauliflower billows in the tower, fibrous streaks in the anvil
                double bil = fbmL(ca * 7, el * 8 + 1.3, sa * 7, 5);
                double fib = fbmL(ca * 3, el * 30 + 4.1, sa * 3, 4);
                double b = bil * (1 - fl) + fib * fl * 0.6;
                double thin = fl * smooth(0.55, 1.0, Math.abs(qq));        // anvil edges are thin
                double v = Math.min(w - Math.abs(dd), (top - el) * 1.6) + b * 0.09 - thin * 0.03;
                double a = smooth(0, 0.025, v) * (1 - 0.6 * thin);
                // rain shafts under the storm (fade out at the horizon itself to stay seamless below)
                if (el < 0.12 && Math.abs(d) < 0.6) {
                    double r = smooth(0.12, 0.02, el) * smooth(0.0, 0.015, el) * smooth(0.6, 0.25, Math.abs(d))
                            * (0.55 + 0.45 * noise(d * 30, 0.5, el * 2));
                    mixTo(c, rain, r * 0.75);
                }
                if (a <= 0.002) return;
                double b2 = fbmL(ca * 7 - 0.03, (el - 0.02) * 8 + 1.3, sa * 7, 3);
                double edge = clamp01((bil - b2) * 3.5 + 0.5);
                double rel = clamp01(el / eTop);
                double light = 0.2 + 0.8 * smooth(0.0, 0.85, rel) + (edge - 0.5) * 0.6 * (1 - fl) + bil * 0.35;
                light = clamp01(light - 0.25 * smooth(0.25, 0.9, Math.abs(qq)) * fl);
                double cr = aShade[0] + (aLit[0] - aShade[0]) * light, cg = aShade[1] + (aLit[1] - aShade[1]) * light, cb = aShade[2] + (aLit[2] - aShade[2]) * light;
                double dark = smooth(0.14, 0.0, el) * 0.65;
                cr += (under[0] - cr) * dark; cg += (under[1] - cg) * dark; cb += (under[2] - cb) * dark;
                double hz = Math.exp(-Math.max(0, el) / 0.03);
                cr += (c.r - cr) * hz; cg += (c.g - cg) * hz; cb += (c.b - cb) * hz;
                c.r += (cr - c.r) * a; c.g += (cg - c.g) * a; c.b += (cb - c.b) * a;
                c.vis *= 1 - a;
            }
        };
        s.kind = "day";
        s.sun = azel(235, 24);
        s.sunCol = rgb(0xFFEBC8);
        s.zen = rgb(0x3E5F8E); s.hor = rgb(0xB4BEC6); s.horSun = rgb(0xE6D6BC); s.horAnti = rgb(0x8E9AA8); s.hW = 0.22;
        s.disk = 30; s.b1 = 0.3; s.w1 = 0.15; s.b2 = 1.4; s.w2 = 0.035;
        Clouds c = new Clouds();
        c.height = 0.8; c.scale = 0.8; c.cov = 0.14; c.soft = 0.25; c.warp = 1.2; c.seed = 3.9; c.fog = 10;
        c.lit = rgb(0xC8CCD4); c.shade = rgb(0x4A505C); c.thickDark = 0.6;
        s.c1 = c;
        s.mist = rgb(0x9AA4AE); s.mistK = 0.4; s.mistW = 0.03;
        s.ground = rgb(0x262C2A); s.groundFar = rgb(0x6E7880); s.groundFall = 0.2;
        return s;
    }

    static Sky anime() {
        Day s = new Day();
        s.kind = "day";
        s.sun = azel(200, 42);
        s.sunCol = rgb(0xFFFBEA);
        s.zen = rgb(0x1255D8); s.hor = rgb(0x9CD2FF); s.hW = 0.3; s.hW2 = 0.6;
        s.sunR = 0.012; s.disk = 45; s.b1 = 0.35; s.w1 = 0.2; s.b2 = 1.6; s.w2 = 0.05;
        Clouds c = new Clouds();
        c.height = 1; c.scale = 0.55; c.cov = 0.12; c.soft = 0.09; c.warp = 1.0; c.seed = 12.5; c.fog = 18; c.oct = 5;
        c.lit = mulc(rgb(0xFFFFFF), 1.3); c.shade = rgb(0x8EA6DA); c.thickDark = 0.35; c.lightSide = 1.5; c.silver = 1.2;
        s.c1 = c;
        Towers t = new Towers();
        t.h0 = 0.02; t.h1 = 0.36; t.freq = 1.7; t.cut = 0.12; t.bill = 5; t.puff = 1.4; t.seed = 7.7; t.soft = 0.008; t.haze = 0.05;
        t.lit = mulc(rgb(0xFFFFFF), 1.25); t.shade = rgb(0x7F9AD4); t.sunSide = 0.4;
        s.tw = t;
        s.below = CLOUDSEA;
        Clouds sea = new Clouds();
        sea.height = 1; sea.scale = 0.45; sea.cov = -0.05; sea.soft = 0.12; sea.warp = 1.1; sea.seed = 3.2; sea.fog = 0.07;
        sea.lit = mulc(rgb(0xFFFFFF), 1.15); sea.shade = rgb(0xA4B8E4);
        s.sea = sea; s.seaGap = rgb(0x3E78C8);
        return s;
    }

    static Sky alien() {
        final double[] sun2 = azel(145, 38);
        final double[] sun2c = rgb(0xCFE6FF);
        Day s = new Day() {
            @Override
            void extra(double x, double y, double z, C c) {
                double ca = x * sun2[0] + y * sun2[1] + z * sun2[2];
                sunDisk(c, ca, 0.006, sun2c, 60, 0.12, 0.08, 1.2, 0.02);
            }
        };
        s.kind = "day";
        s.sun = azel(215, 22);
        s.sunCol = rgb(0xFFC896);
        s.zen = rgb(0x0B4F5E); s.hor = rgb(0x8ADBC8); s.horSun = rgb(0xE8E0A8); s.horAnti = rgb(0x6CC0C0); s.hW = 0.25;
        s.glow = rgb(0xFFB070); s.glowK = 0.35; s.glowPow = 5; s.glowH = 0.2;
        s.sunR = 0.032; s.disk = 14; s.b1 = 0.5; s.w1 = 0.2; s.b2 = 1.0; s.w2 = 0.08;
        s.moon = new SkyGen3.Body(azel(20, 32), 0.13, 0);
        s.moonTint = rgb(0xE8D0E8);
        Clouds c = new Clouds();
        c.height = 1.4; c.scale = 0.6; c.stretch = 4; c.cov = 0.1; c.soft = 0.3; c.warp = 1.8; c.seed = 21.1; c.fog = 14;
        c.lit = mulc(rgb(0xF6D2E8), 1.1); c.shade = rgb(0x3F7C86); c.thickDark = 0.4; c.silver = 1.5;
        s.c1 = c;
        s.mist = rgb(0xA8E8D8); s.mistK = 0.35; s.mistW = 0.04;
        s.ground = rgb(0x173236); s.groundFar = rgb(0x6AB4A8); s.groundFall = 0.22;
        return s;
    }

    static Sky synthwave() {
        Day s = new Day() {
            final double[] top = rgb(0xFFE45A), midc = rgb(0xFF8A3C), bot = rgb(0xFF2E86);

            @Override
            void extra(double x, double y, double z, C c) {
                // big retro sun with horizontal cut-outs
                double cz = -z, ang;
                double cosA = x * sun[0] + y * sun[1] + z * sun[2];
                ang = Math.acos(Math.max(-1, Math.min(1, cosA)));
                double R = 0.21;
                double gl = Math.exp(-Math.max(0, ang - R) / 0.10);
                add(c, bot, gl * 0.5);
                if (ang > R * 1.02 || cz <= 0) return;
                double v = (y - (sun[1] - R)) / (2 * R);    // 0 bottom .. 1 top
                double r1, g1, b1;
                if (v > 0.5) { double t = (v - 0.5) * 2; r1 = mix(midc[0], top[0], t); g1 = mix(midc[1], top[1], t); b1 = mix(midc[2], top[2], t); }
                else { double t = v * 2; r1 = mix(bot[0], midc[0], t); g1 = mix(bot[1], midc[1], t); b1 = mix(bot[2], midc[2], t); }
                double k = 1;
                if (v < 0.55) {
                    double freq = 9.0;
                    double ph = (v * freq) % 1.0;
                    double gap = (0.55 - v) / 0.55 * 0.55;
                    double ew = 0.06;
                    k = smooth(gap - ew, gap + ew, ph);
                }
                double e = smooth(R * 1.02, R * 0.985, ang) * k;
                double I = 2.2;
                c.r += (r1 * I - c.r) * e; c.g += (g1 * I - c.g) * e; c.b += (b1 * I - c.b) * e;
                c.vis *= 1 - e;
            }
        };
        s.kind = "sunset";
        s.sun = azel(0, 8);
        s.sunCol = rgb(0xFF6AA8);
        s.zen = rgb(0x07031C); s.mid = rgb(0x3A0E62); s.midW = 0.35;
        s.hor = rgb(0xFF3E8E); s.horSun = rgb(0xFF5A7A); s.horAnti = rgb(0xB0247E); s.hW = 0.07;
        s.glow = rgb(0xFF4F9A); s.glowK = 0.6; s.glowPow = 3; s.glowH = 0.2;
        s.disk = 0; s.b1 = 0; s.b2 = 0;
        Clouds c = new Clouds();
        c.height = 2; c.scale = 0.35; c.stretch = 6; c.cov = 0.22; c.soft = 0.25; c.warp = 0.6; c.seed = 4.8; c.fog = 20; c.oct = 4;
        c.lit = rgb(0xFF76C0); c.shade = rgb(0x5A1E7E); c.thickDark = 0.3; c.silver = 0; c.opacity = 0.85;
        s.c1 = c;
        s.stars = 26000; s.starMin = 0.05; s.starMax = 2.5; s.starSeed = 31;
        s.starDensityFn = (x, y, z) -> smooth(0.12, 0.55, y);
        s.below = OCEAN;
        s.water = rgb(0x0C0420); s.waves = 0.25; s.glitter = 0; s.mirror = 1;
        return s;
    }

    static Sky oceanDusk() {
        Day s = new Day() {
            @Override
            double starDensity(double x, double y, double z) { return smooth(0.12, 0.6, y) * 0.8; }
        };
        s.kind = "sunset";
        s.sun = azel(265, -5);
        s.sunCol = rgb(0xFF9A70);
        s.zen = rgb(0x0B1840); s.mid = rgb(0x1C3A70); s.midW = 0.4;
        s.hor = rgb(0x3D6C98); s.horSun = rgb(0xF08A62); s.horAnti = rgb(0x2E5078); s.hW = 0.09;
        s.glow = rgb(0xFF8A5A); s.glowK = 0.7; s.glowPow = 6; s.glowH = 0.07;
        s.belt = rgb(0x8A6A9A); s.beltK = 0.18;
        s.disk = 0; s.b1 = 0.3; s.w1 = 0.12; s.b2 = 0; s.w2 = 0.05;
        Clouds ci = new Clouds();
        ci.height = 3; ci.scale = 0.3; ci.stretch = 4; ci.cov = 0.14; ci.soft = 0.35; ci.seed = 9.9; ci.oct = 5; ci.warp = 1.6;
        ci.lit = rgb(0xF4A2A0); ci.shade = rgb(0x3A3C6A); ci.opacity = 0.7; ci.thickDark = 0.2; ci.fog = 25; ci.silver = 3; ci.lightSide = 1.6;
        s.c2 = ci;
        s.stars = 20000; s.starMin = 0.04; s.starMax = 2.0; s.starSeed = 5;
        s.below = OCEAN;
        s.water = rgb(0x03101C); s.waves = 1.1; s.glitter = 0.4; s.rough = 0.1;
        return s;
    }

    static double[] mulc(double[] c, double k) { return new double[]{c[0] * k, c[1] * k, c[2] * k}; }
}
