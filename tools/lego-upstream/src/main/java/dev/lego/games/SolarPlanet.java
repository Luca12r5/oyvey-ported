package dev.lego.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.lego.ui.Gx;

/**
 * One celestial body of Solar Smash: procedural surface maps, damage (craters, heat, cracks, floods ...),
 * population / integrity statistics and a CPU sphere renderer that writes a few rows ("slabs") per frame.
 */
final class SolarPlanet {
    static final int SUN = 0, TERRA = 1, MARS = 2, GAS = 3, ICE = 4, LAVA = 5, OCEAN = 6, DESERT = 7, MOON = 8;

    // paint styles
    static final int S_BURN = 0, S_FIRE = 1, S_ICE = 2, S_GOLD = 3, S_TOXIC = 4, S_CRYSTAL = 5, S_GOO = 6, S_FLOOD = 7, S_SAND = 8,
            S_CREAM = 9, S_CONFETTI = 10, S_LAVA = 11, S_ANNI = 12, S_SHAKE = 13, S_EMP = 14, S_LEGO = 15, S_COW = 16, S_PIZZA = 17,
            S_DONUT = 18, S_PAINT = 19, S_RIFT = 20, S_PIANO = 21, S_CRACK = 22;

    // ================================================================== maps
    static final class Maps {
        final int W, H, shift, cW, cH;
        final int[] alb;
        final byte[] hgt, heat, dmg, city, cloud, heat0;
        final float[] cosRow;
        int W1, H1, shift1;
        int[] alb1;
        byte[] heat1, city1;
        double popW, areaW;
        byte sea;
        final int TS = 64, tx, ty;
        final boolean[] hot, dirty;
        final float[] coolT;
        int hotPtr, dirtyCount;

        Maps(int W, int H, boolean clouds, boolean lava) {
            this.W = W; this.H = H;
            shift = Integer.numberOfTrailingZeros(W);
            alb = new int[W * H];
            hgt = new byte[W * H];
            heat = new byte[W * H];
            dmg = new byte[W * H];
            city = new byte[W * H];
            heat0 = lava ? new byte[W * H] : null;
            cW = W >= 1024 ? W / 2 : W;
            cH = H * cW / W;
            cloud = clouds ? new byte[cW * cH] : null;
            cosRow = new float[H];
            for (int y = 0; y < H; y++) cosRow[y] = (float) Math.max(0.004, Math.cos((0.5 - (y + 0.5) / H) * Math.PI));
            tx = Math.max(1, W / TS);
            ty = Math.max(1, H / TS);
            hot = new boolean[tx * ty];
            dirty = new boolean[tx * ty];
            coolT = new float[tx * ty];
        }

        void finish() {
            double pw = 0, aw = 0;
            for (int y = 0; y < H; y++) {
                double c = cosRow[y], s = 0;
                for (int x = 0; x < W; x++) s += city[y * W + x] & 255;
                pw += s * c;
                aw += c * W * 255.0;
            }
            popW = Math.max(1, pw);
            areaW = aw;
            if (W > 1024) {
                W1 = W / 2; H1 = H / 2; shift1 = shift - 1;
                alb1 = new int[W1 * H1];
                heat1 = new byte[W1 * H1];
                city1 = new byte[W1 * H1];
                for (int t = 0; t < tx * ty; t++) mip(t);
            } else {
                W1 = W; H1 = H; shift1 = shift;
                alb1 = alb; heat1 = heat; city1 = city;
            }
            for (int t = 0; t < hot.length; t++) if (heat0 != null) hot[t] = false;
        }

        /** Rebuilds the half-resolution copy of one tile. */
        void mip(int t) {
            dirty[t] = false;
            if (alb1 == alb) return;
            int x0 = (t % tx) * TS, y0 = (t / tx) * TS;
            for (int y = y0; y < y0 + TS && y < H; y += 2)
                for (int x = x0; x < x0 + TS && x < W; x += 2) {
                    int i = y * W + x, j = i + W;
                    int a = alb[i], b = alb[i + 1], c = alb[j], d = alb[j + 1];
                    int sa = a >>> 24, sb = b >>> 24, sc = c >>> 24, sd = d >>> 24;
                    int s = (sa == 1 || sb == 1 || sc == 1 || sd == 1) ? 1 : ((sa + sb + sc + sd) >> 2);
                    if (s == 1 && !(sa == 1 && sb == 1 && sc == 1 && sd == 1) && (sa + sb + sc + sd) < 8) s = 1;
                    int r = (((a >> 16) & 255) + ((b >> 16) & 255) + ((c >> 16) & 255) + ((d >> 16) & 255)) >> 2;
                    int g = (((a >> 8) & 255) + ((b >> 8) & 255) + ((c >> 8) & 255) + ((d >> 8) & 255)) >> 2;
                    int bl = ((a & 255) + (b & 255) + (c & 255) + (d & 255)) >> 2;
                    int o = (y >> 1) * W1 + (x >> 1);
                    alb1[o] = (s << 24) | (r << 16) | (g << 8) | bl;
                    int h = Math.max(Math.max(heat[i] & 255, heat[i + 1] & 255), Math.max(heat[j] & 255, heat[j + 1] & 255));
                    int h2 = ((heat[i] & 255) + (heat[i + 1] & 255) + (heat[j] & 255) + (heat[j + 1] & 255)) >> 2;
                    heat1[o] = (byte) ((h + h2) >> 1);
                    city1[o] = (byte) (((city[i] & 255) + (city[i + 1] & 255) + (city[j] & 255) + (city[j + 1] & 255)) >> 2);
                }
        }

        void markRect(int x0, int y0, int x1, int y1, boolean heatToo, float now) {
            int ty0 = Math.max(0, y0 / TS), ty1 = Math.min(ty - 1, y1 / TS);
            if (x1 - x0 >= W - 1) { x0 = 0; x1 = W - 1; }
            for (int yy = ty0; yy <= ty1; yy++)
                for (int xx = x0 / TS - (x0 < 0 ? 1 : 0); xx <= Math.floorDiv(x1, TS); xx++) {
                    int t = yy * tx + Math.floorMod(xx, tx);
                    if (!dirty[t]) { dirty[t] = true; dirtyCount++; }
                    if (heatToo && !hot[t]) { hot[t] = true; coolT[t] = now; }
                }
        }
    }

    // ================================================================== body
    final int type;
    final String name, kind;
    final long pop;
    final float baseTemp, atmo0;
    final int atmoCol, cloudCol, cityCol;
    final float size, tilt;
    final long seed;
    final boolean inhabited, minor;
    float rotSpeed, cloudSpeed;
    // system orbit
    float orbitR, orbitA, orbitW;
    SolarPlanet parent;
    // local moon orbit (planet view, in units of parent radius)
    float moonDist, moonA, moonW, moonIncl = 0.28f;
    final List<SolarPlanet> moons = new ArrayList<>();
    // ring
    boolean ringed;
    final float[] ringHp = new float[72];
    int ringVer = 0;
    int[] ringCols = {0xFFE4D2B0, 0xFFB08A60, 0xFFF2E6CC};
    // maps
    volatile Maps hi, thumb;
    private volatile boolean hiPending, thumbPending;
    private int genToken = 0;
    double rot, crot;
    int ver = 0;
    // stats
    double killW, gAlive = 1, dmgW;
    float core, atmo, tempOff, lightsOff, dark, scale = 1, shake;
    boolean destroyed;
    float destroyedT, rebuildT = 9, hitFlash;
    double deadShown, aliveShown;
    int crackSeed = 1;
    // crater palette
    int floorCol, scorchCol, ejectaCol, coreCol;

    SolarPlanet(int type, String name, String kind, long pop, float temp, int atmoCol, float atmo, float size, float tilt, long seed, boolean minor) {
        this.type = type; this.name = name; this.kind = kind; this.pop = pop; this.baseTemp = temp; this.atmoCol = atmoCol; this.atmo0 = atmo;
        this.size = size; this.tilt = tilt; this.seed = seed; this.minor = minor;
        this.inhabited = pop > 0;
        this.atmo = atmo;
        rotSpeed = (float) (Math.PI * 2 / (type == GAS ? 70 : type == SUN ? 160 : 120));
        cloudSpeed = rotSpeed * 1.25f;
        cloudCol = type == MARS ? 0xFFEBD2B8 : type == LAVA ? 0xFF4E4744 : type == DESERT ? 0xFFF4E6D0 : 0xFFFFFFFF;
        cityCol = type == OCEAN ? 0xFF9FE8FF : type == ICE ? 0xFFBFE0FF : 0xFFFFC478;
        switch (type) {
            case ICE: floorCol = 0xFF1F3550; scorchCol = 0xFF40546A; ejectaCol = 0xFFD8ECFA; coreCol = 0xFF3A6AA8; break;
            case GAS: floorCol = 0xFF3B2416; scorchCol = 0xFF5A3A24; ejectaCol = 0xFFF0DCC0; coreCol = 0xFF7A2A10; break;
            case SUN: floorCol = 0xFF2A0C04; scorchCol = 0xFF8A2A08; ejectaCol = 0xFFFFF4C0; coreCol = 0xFFFFFFE0; break;
            case MOON: floorCol = 0xFF3A3836; scorchCol = 0xFF55524E; ejectaCol = 0xFFD8D4CC; coreCol = 0xFF5A1A0A; break;
            case LAVA: floorCol = 0xFF1A0E0A; scorchCol = 0xFF2A1A14; ejectaCol = 0xFF5A4238; coreCol = 0xFF8A2008; break;
            default: floorCol = 0xFF2C2420; scorchCol = 0xFF221B17; ejectaCol = 0xFF9A8B78; coreCol = 0xFF6A180A;
        }
        java.util.Arrays.fill(ringHp, 1f);
    }

    Maps maps() { Maps m = hi; return m != null ? m : thumb; }

    /** Starts background generation of the thumbnail and (optionally) the full map. */
    void request(boolean full) {
        if (thumb == null && !thumbPending) {
            thumbPending = true;
            final int tok = genToken;
            SolarTex.POOL.execute(() -> {
                Maps m = SolarTex.generate(type, seed, 256, 128, inhabited, false);
                if (tok == genToken) { thumb = m; ver++; }
                thumbPending = false;
            });
        }
        if (full && hi == null && !hiPending && !destroyed) {
            hiPending = true;
            final int tok = genToken;
            final int w = type == SUN || minor ? 1024 : 2048;
            SolarTex.POOL.execute(() -> {
                Maps m = SolarTex.generate(type, seed, w, w / 2, inhabited, true);
                if (tok == genToken) { hi = m; ver++; }
                hiPending = false;
            });
        }
    }

    boolean loading() { return hi == null; }

    boolean damaged() { return killW > 0 || dmgW > 0 || core > 0 || gAlive < 1 || destroyed || atmo < atmo0 - 0.001f || scale < 1; }

    /** Frees the big maps of an untouched body. */
    void release() { if (!damaged() && !hiPending) hi = null; }

    void repair() {
        genToken++;
        hi = null;
        hiPending = false;
        killW = 0; dmgW = 0; gAlive = 1; core = 0; atmo = atmo0; tempOff = 0; lightsOff = 0; dark = 0; scale = 1; shake = 0;
        if (destroyed) rebuildT = 0;
        destroyed = false;
        destroyedT = 0;
        java.util.Arrays.fill(ringHp, 1f);
        ringVer++;
        ver++;
        for (SolarPlanet m : moons) m.repair();
    }

    // ================================================================== stats
    double alive() {
        if (pop <= 0 || destroyed) return 0;
        Maps m = hi;
        double loc = m == null ? 1 : Math.max(0, 1 - killW / m.popW);
        return pop * loc * Math.max(0, gAlive);
    }

    double dead() { return pop - alive(); }

    float integrity() {
        if (destroyed) return 0;
        Maps m = hi;
        double d = m == null ? 0 : dmgW / m.areaW * 2.6;
        return (float) Math.max(0, 1 - d - core);
    }

    float temp() { return baseTemp + tempOff; }

    void killGlobal(double frac) { gAlive *= Math.max(0, 1 - frac); }

    // ================================================================== geometry helpers
    /** Screen offset (unit disc, y down) -> {lon, lat}. */
    double[] unproject(double x, double y) {
        double r2 = x * x + y * y;
        if (r2 > 0.9995) { double k = 0.9997 / Math.sqrt(r2); x *= k; y *= k; r2 = 0.9994; }
        double ct = Math.cos(tilt), st = Math.sin(tilt);
        double xr = x * ct + y * st, yr = -x * st + y * ct;
        double z = Math.sqrt(Math.max(0, 1 - r2));
        return new double[]{Math.atan2(xr, z) + rot, Math.asin(Math.max(-1, Math.min(1, -yr)))};
    }

    /** {lon, lat} -> screen offset (unit disc) + z (visible if z > 0). */
    double[] project(double lon, double lat) {
        double l = lon - rot;
        double xr = Math.cos(lat) * Math.sin(l), yr = -Math.sin(lat), z = Math.cos(lat) * Math.cos(l);
        double ct = Math.cos(tilt), st = Math.sin(tilt);
        return new double[]{xr * ct - yr * st, xr * st + yr * ct, z};
    }

    // ================================================================== damage
    private float now;

    /**
     * Damages the surface around lon/lat. r = crater radius in texels of a 2048 map, depth = damage added at the centre (0..255+),
     * heat = glow added, kill = fraction of people killed inside, scorch = outer burn radius as a multiple of r.
     */
    void paint(double lon, double lat, float r, int style, float depth, float heatAmt, float kill, float scorch, int color) {
        Maps m = hi;
        if (m == null || destroyed) return;
        final int W = m.W, H = m.H;
        float k = W / 2048f;
        r = Math.max(0.7f, r * k);
        scorch = Math.max(1, scorch);
        float ro = r * scorch;
        double cyD = (0.5 - lat / Math.PI) * H - 0.5;
        double cxD = (lon / (Math.PI * 2)) * W - 0.5;
        cxD = cxD - Math.floor(cxD / W) * W;
        int y0 = (int) Math.max(0, Math.floor(cyD - ro)), y1 = (int) Math.min(H - 1, Math.ceil(cyD + ro));
        double kw = 0, dw = 0;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        final int[] alb = m.alb;
        final byte[] dmg = m.dmg, heat = m.heat, city = m.city, h0 = m.heat0;
        final int sea = m.sea & 255;
        final byte[] HASH = SolarTex.HASH;
        final boolean star = type == SUN;
        for (int y = y0; y <= y1; y++) {
            float cl = m.cosRow[y];
            float dy = (float) (y - cyD);
            float q = ro * ro - dy * dy;
            if (q < 0) continue;
            float half = (float) Math.sqrt(q) / cl;
            if (half > W / 2f) half = W / 2f;
            int xa = (int) Math.floor(cxD - half), xb = (int) Math.ceil(cxD + half);
            if (xa < minX) minX = xa;
            if (xb > maxX) maxX = xb;
            double rowK = 0, rowD = 0;
            for (int xx = xa; xx <= xb; xx++) {
                int x = xx & (W - 1);
                float dx = (float) (xx - cxD) * cl;
                float d0 = (float) Math.sqrt(dx * dx + dy * dy) / r;
                int hc = HASH[(((xx >> 2) * 31 + (y >> 2) * 977) + (int) (lon * 91)) & 65535] & 255;
                int hf = HASH[(x * 7 + y * 1031) & 65535] & 255;
                float d = d0 * (1 + (hc - 128) * 0.0017f + (hf - 128) * 0.0005f);
                if (d >= scorch) continue;
                int i = (y << m.shift) | x;
                int a = alb[i];
                int sa = a >>> 24;
                int c = a & 0xFFFFFF;
                int dm = dmg[i] & 255, ht = heat[i] & 255, ct = city[i] & 255;
                int ndm = dm, nht = ht, nct = ct, nsa = sa;
                float in = 1 - d;
                switch (style) {
                    case S_BURN: case S_ANNI: case S_LAVA: case S_FIRE: case S_CRACK: case S_RIFT: case S_SHAKE: {
                        if (style == S_SHAKE) {
                            float t = 1 - d / scorch;
                            nct = (int) (ct * (1 - kill * t));
                            c = SolarTex.mix(c, scorchCol, t * 0.12f * depth / 60f);
                            ndm = Math.min(255, dm + (int) (depth * 0.08f * t));
                            break;
                        }
                        if (d < 1) {
                            float prof = style == S_ANNI ? 1 : (float) Math.sqrt(in);
                            ndm = Math.min(255, dm + (int) (depth * prof * (style == S_FIRE ? 0.12f : 1)));
                            if (style == S_FIRE) c = SolarTex.mix(c, scorchCol, Math.min(1, in * 1.4f) * 0.85f);
                            else {
                                int fl = SolarTex.mix(floorCol, SolarTex.shade(floorCol, 1.5), hf / 255.0);
                                c = SolarTex.mix(c, fl, Math.min(1, in * 2.4f) * Math.min(1, depth / 90f + 0.3f));
                                if (d > 0.72 && style != S_ANNI && style != S_CRACK) c = SolarTex.mix(c, ejectaCol, (1 - Math.abs(d - 0.9f) / 0.18f) * 0.35f);
                            }
                            if (style == S_LAVA) { c = SolarTex.mix(c, 0xFF2A1510, 0.7f); nht = Math.min(255, nht + (int) (heatAmt * in)); }
                            if (style == S_RIFT) c = SolarTex.mix(c, 0xFF1A0630, in);
                            nsa = 0;
                            nct = (int) (ct * (1 - Math.min(1, kill * 1.2f)));
                        } else {
                            float t = 1 - (d - 1) / (scorch - 1);
                            float s = t * t * Math.min(1, depth / 60f + 0.35f) * (style == S_FIRE ? 1f : 0.8f);
                            c = SolarTex.mix(c, style == S_RIFT ? 0xFF2A0E44 : scorchCol, s);
                            nct = (int) (ct * (1 - kill * Math.min(1, t * 1.3f)));
                            if (sa > 1) nsa = (int) (sa * (1 - t * 0.6f));
                        }
                        if (ndm > 200) c = SolarTex.mix(c, coreCol, (ndm - 200) / 55f * (star ? 0.4f : 1));
                        if (ndm >= 250 && !star) nsa = 1;
                        float hh = heatAmt * (float) Math.pow(Math.max(0, 1.05f - d / scorch), 1.6);
                        if (style == S_FIRE) hh *= (hf & 3) == 0 ? 1.4f : 0.7f;
                        nht = Math.min(255, Math.max(nht, (int) (ht + hh)));
                        break;
                    }
                    case S_ICE: {
                        float t = Math.min(1, (scorch - d) / scorch * 1.6f);
                        int ic = SolarTex.mix(0xFFE8F4FC, 0xFFA8CCE8, hf / 255.0);
                        c = SolarTex.mix(c, ic, t * 0.92f);
                        nht = (int) (ht * (1 - t));
                        nsa = Math.max(2, (int) (sa + (120 - sa) * t));
                        nct = (int) (ct * (1 - kill * t));
                        ndm = Math.min(255, dm + (int) (depth * 0.15f * t));
                        break;
                    }
                    case S_GOLD: case S_TOXIC: case S_CRYSTAL: case S_GOO: case S_SAND: case S_CREAM: case S_PAINT: {
                        float t = Math.min(1, (scorch - d) / scorch * 2.2f);
                        int tc;
                        switch (style) {
                            case S_GOLD: tc = SolarTex.mix(0xFFFFD86A, 0xFFB8861C, hf / 255.0); nsa = Math.max(2, (int) (sa + (200 - sa) * t)); break;
                            case S_TOXIC: tc = SolarTex.mix(0xFF7CF23A, 0xFF1E8A3C, hc / 255.0); nsa = 0; break;
                            case S_CRYSTAL: tc = SolarTex.mix(0xFFD6A8FF, 0xFF6A2AC8, hf / 255.0); nsa = Math.max(2, (int) (sa + (255 - sa) * t)); break;
                            case S_GOO: tc = SolarTex.mix(0xFF9AA2AE, 0xFF4A5058, hf / 255.0); nsa = Math.max(2, (int) (sa + (140 - sa) * t)); break;
                            case S_SAND: tc = SolarTex.mix(0xFFE2C184, 0xFFB08850, hf / 255.0); nsa = (int) (sa * (1 - t)); break;
                            case S_CREAM: tc = (hc & 64) != 0 ? 0xFFFFF6F8 : SolarTex.mix(0xFFFFB6D0, 0xFFFF7FAE, hf / 255.0); nsa = (int) (sa * (1 - t)); break;
                            default: tc = SolarTex.mix(color, SolarTex.shade(color, 0.75), hf / 400.0); nsa = (int) (sa * (1 - t));
                        }
                        c = SolarTex.mix(c, tc, t);
                        if (nsa == 1) nsa = 2;
                        nct = (int) (ct * (1 - kill * t));
                        ndm = Math.min(255, dm + (int) (depth * t * 0.4f));
                        if (heatAmt > 0) nht = Math.min(255, (int) (ht + heatAmt * t * in));
                        break;
                    }
                    case S_FLOOD: {
                        int hg = m.hgt[i] & 255;
                        if (sa > 150 && d > 0.2f) break; // already water
                        if (hg > sea + depth && d > 0.35f) break;
                        float t = Math.min(1, (scorch - d) / scorch * 2.5f);
                        c = SolarTex.mix(c, SolarTex.mix(0xFF2E6E8A, 0xFF5A6E5A, hf / 400.0), t * 0.85f);
                        nsa = Math.max(2, (int) (sa + (200 - sa) * t));
                        nct = (int) (ct * (1 - kill * t));
                        nht = 0;
                        break;
                    }
                    case S_CONFETTI: {
                        float t = Math.min(1, (scorch - d) / scorch * 2f);
                        if (((hf ^ hc) & 3) != 0) break;
                        int[] cs = {0xFFFF3B6B, 0xFFFFD21F, 0xFF3BD1FF, 0xFF7CF23A, 0xFFB45CFF, 0xFFFF8A00};
                        c = SolarTex.mix(c, cs[hf % cs.length], t);
                        nct = (int) (ct * (1 - kill * t));
                        break;
                    }
                    case S_EMP: {
                        float t = 1 - d / scorch;
                        nct = (int) (ct * (1 - kill * t));
                        break;
                    }
                    case S_LEGO: {
                        // 2x4 brick footprint stuck in the ground
                        float ux = dx / r, uy = dy / r;
                        if (Math.abs(ux) < 0.95f && Math.abs(uy) < 0.5f) {
                            float sx = (ux + 0.95f) / 0.475f, sy = (uy + 0.5f) / 0.5f;
                            float fx = sx - (int) sx - 0.5f, fy = sy - (int) sy - 0.5f;
                            boolean stud = fx * fx + fy * fy < 0.09f;
                            boolean ring = fx * fx + fy * fy < 0.13f;
                            c = stud ? SolarTex.shade(color, 1.18) : ring ? SolarTex.shade(color, 0.72) : color;
                            if (Math.abs(ux) > 0.9f || Math.abs(uy) > 0.45f) c = SolarTex.shade(color, 0.62);
                            nsa = 150;
                            nct = 0;
                            nht = 0;
                            ndm = Math.min(255, dm + 40);
                        } else if (d < scorch) {
                            float t = 1 - (d - 1) / (scorch - 1);
                            c = SolarTex.mix(c, scorchCol, Math.max(0, t) * 0.5f);
                            nct = (int) (ct * (1 - kill * Math.max(0, t)));
                        }
                        break;
                    }
                    case S_COW: case S_PIZZA: case S_DONUT: case S_PIANO: {
                        float t = Math.min(1, (scorch - d) / scorch * 2.2f);
                        int tc;
                        if (style == S_COW) tc = hc < 110 ? 0xFF1C1A1A : 0xFFF4F1EA;
                        else if (style == S_PIZZA) tc = d > 0.85f ? 0xFFD59A4A : hc > 200 ? 0xFFB0251A : 0xFFFFD260;
                        else if (style == S_DONUT) { if (d < 0.45f) { tc = floorCol; t = in * 0.5f; } else tc = d > 0.9f ? 0xFFC98A4E : ((hf & 15) == 0 ? 0xFF5FD4FF : 0xFFFF7FC0); }
                        else tc = ((int) ((dx / r + 2) * 7) & 1) == 0 ? 0xFFF4F4F0 : 0xFF16161A;
                        c = SolarTex.mix(c, tc, t);
                        nsa = style == S_PIANO ? 120 : 0;
                        nct = (int) (ct * (1 - kill * t));
                        ndm = Math.min(255, dm + (int) (depth * t * 0.5f));
                        break;
                    }
                    default: break;
                }
                if (h0 != null) nht = Math.max(nht, h0[i] & 255);
                if (ndm >= 180) nht = Math.max(nht, Math.min(215, (ndm - 180) * 3));
                if (nsa == 1 && ndm < 250) nsa = 0;
                alb[i] = (nsa << 24) | (c & 0xFFFFFF);
                if (ndm != dm) { dmg[i] = (byte) ndm; rowD += ndm - dm; }
                if (nht != ht) heat[i] = (byte) nht;
                if (nct < ct) { city[i] = (byte) nct; rowK += ct - nct; }
            }
            kw += rowK * cl;
            dw += rowD * cl;
        }
        // clouds burn off
        if (m.cloud != null && (style <= S_FIRE || style == S_ANNI || style == S_LAVA) && depth > 20) {
            int cw = m.cW, ch = m.cH;
            float cr = ro * cw / W * 1.2f;
            double ccy = (0.5 - lat / Math.PI) * ch, ccx = lon / (Math.PI * 2) * cw;
            for (int y = (int) Math.max(0, ccy - cr); y <= Math.min(ch - 1, ccy + cr); y++) {
                float cl = (float) Math.max(0.02, Math.cos((0.5 - (y + 0.5) / ch) * Math.PI));
                float dy = (float) (y - ccy);
                float half = (float) Math.sqrt(Math.max(0, cr * cr - dy * dy)) / cl;
                if (half > cw / 2f) half = cw / 2f;
                for (int xx = (int) (ccx - half); xx <= (int) (ccx + half); xx++) {
                    float dx = (float) (xx - ccx) * cl;
                    float d = (float) Math.sqrt(dx * dx + dy * dy) / cr;
                    if (d >= 1) continue;
                    int i = y * cw + Math.floorMod(xx, cw);
                    m.cloud[i] = (byte) ((m.cloud[i] & 255) * Math.min(1, d * d * 1.2f));
                }
            }
        }
        if (minX <= maxX) m.markRect(minX, y0, maxX, y1, true, now);
        killW += kw;
        dmgW += dw;
        ver++;
    }

    /** A crack: random walk of thin burning lines. */
    void crack(double lon, double lat, float len, float width, float heatAmt, float kill, java.util.Random r) {
        double ang = r.nextDouble() * Math.PI * 2;
        double step = width * 0.9 / 2048.0 * Math.PI * 2;
        int n = (int) (len / Math.max(0.5, width * 0.9));
        for (int i = 0; i < n; i++) {
            ang += (r.nextDouble() - 0.5) * 0.7;
            double cl = Math.max(0.1, Math.cos(lat));
            lon += Math.cos(ang) * step / cl;
            lat += Math.sin(ang) * step;
            if (lat > 1.5 || lat < -1.5) { ang = -ang; lat = Math.max(-1.5, Math.min(1.5, lat)); }
            float w = width * (1 - i / (float) n * 0.6f);
            paint(lon, lat, w, S_CRACK, 230, heatAmt, kill, 2.2f, 0);
            if (r.nextFloat() < 0.04f && n > 20) crack(lon, lat, len * 0.35f, width * 0.7f, heatAmt, kill, r);
        }
    }

    /** Runs f(x, y, index) over rows [y0, y1) of the full map (global effects). */
    interface TexelFn { void apply(Maps m, int x, int y, int i); }

    void rowsPass(int y0, int y1, TexelFn f) {
        Maps m = hi;
        if (m == null || destroyed) return;
        y0 = Math.max(0, y0); y1 = Math.min(m.H, y1);
        if (y1 <= y0) return;
        long before = cityRows(m, y0, y1);
        for (int y = y0; y < y1; y++) for (int x = 0; x < m.W; x++) f.apply(m, x, y, (y << m.shift) | x);
        long after = cityRows(m, y0, y1);
        killW += Math.max(0, before - after) * (double) m.cosRow[(y0 + y1) / 2];
        m.markRect(0, y0, m.W - 1, y1 - 1, true, now);
        ver++;
    }

    private static long cityRows(Maps m, int y0, int y1) {
        long s = 0;
        for (int i = y0 * m.W; i < y1 * m.W; i++) s += m.city[i] & 255;
        return s;
    }

    // ================================================================== ring
    void damageRing(double ang, float amount, float spread) {
        int n = ringHp.length;
        int c = (int) Math.floor((ang + Math.PI) / (Math.PI * 2) * n);
        int s = Math.max(0, (int) spread);
        for (int k = -s; k <= s; k++) {
            int i = Math.floorMod(c + k, n);
            ringHp[i] = Math.max(0, ringHp[i] - amount * (1 - Math.abs(k) / (float) (s + 1)));
        }
        ringVer++;
    }

    float ringAlive() {
        if (!ringed) return 0;
        float s = 0;
        for (float h : ringHp) s += h;
        return s / ringHp.length;
    }

    private int ringBuilt = -1;
    private long ringBuiltAt;
    private String ringKey;

    /** Ring image pair {back, front} (half resolution; tilt + planet shadow baked). */
    Gx.Img[] ringImgs(int w, int h, float rPx, float Lx, float Ly, float Lz) {
        String key = "solar:ring:" + name + ":" + w + "x" + h;
        final int fw = w, fh = h;
        Gx.Img back = Gx.get(key + ":b", 0, () -> new int[][]{new int[fw * fh], {fw, fh}});
        Gx.Img front = Gx.get(key + ":f", 0, () -> new int[][]{new int[fw * fh], {fw, fh}});
        long t = System.currentTimeMillis();
        if (!key.equals(ringKey) || (ringBuilt != ringVer && t - ringBuiltAt > 350)) {
            int[] b = new int[w * h], f = new int[w * h];
            buildRing(b, f, w, h, rPx, Lx, Ly, Lz);
            Gx.B.update(back, b);
            Gx.B.update(front, f);
            ringBuilt = ringVer;
            ringBuiltAt = t;
            ringKey = key;
        }
        return new Gx.Img[]{back, front};
    }

    static final float RING_IN = 1.28f, RING_OUT = 2.3f, RING_FLAT = 0.26f;

    private void buildRing(int[] back, int[] front, int w, int h, float rPx, float Lx, float Ly, float Lz) {
        double cx = w / 2.0, cy = h / 2.0;
        double ct = Math.cos(tilt), st = Math.sin(tilt);
        double inc = Math.asin(RING_FLAT), ci = Math.cos(inc);
        double lx = Lx * ct + Ly * st, ly = -Lx * st + Ly * ct, lz = Lz;
        int n = ringHp.length;
        int[] cols = ringCols;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - cx), dy = (y + 0.5 - cy);
                double ux = (dx * ct + dy * st) / rPx, uy = (-dx * st + dy * ct) / rPx;
                double uz = uy / RING_FLAT;
                double r = Math.sqrt(ux * ux + uz * uz);
                if (r < RING_IN || r > RING_OUT) continue;
                double t = (r - RING_IN) / (RING_OUT - RING_IN);
                double band = 0.5 + 0.5 * Math.sin(t * 41) * Math.sin(t * 13 + 1.3) + 0.25 * Math.sin(t * 97);
                band = Math.max(0.05, Math.min(1, band));
                if (t > 0.56 && t < 0.62) band *= 0.08;
                if (t > 0.86 && t < 0.885) band *= 0.25;
                double a = band * Math.min(1, t * 9) * Math.min(1, (1 - t) * 12) * 0.9;
                int c = SolarTex.mix(cols[0], cols[1], t);
                c = SolarTex.mix(c, cols[2], SolarTex.clamp01(Math.sin(t * 23) * 0.5 + 0.2));
                double ang = Math.atan2(uz, ux);
                int s = (int) ((ang + Math.PI) / (Math.PI * 2) * n) % n;
                double hp = ringHp[s];
                if (hp < 0.999) {
                    int tr = (int) (t * 7);
                    double hsh = (SolarTex.HASH[(s * 7 + tr * 131) & 65535] & 255) / 255.0;
                    a *= SolarTex.clamp01(hp * 1.8 - hsh * 0.8);
                    c = SolarTex.mix(c, 0xFF3A2A20, (1 - hp) * 0.5);
                }
                // 3D point in view space (y down, z to viewer) and the planet's shadow
                double px = ux, py = uz * RING_FLAT, pz = -uz * ci;
                double pl = px * lx + py * ly + pz * lz;
                if (pl < 0) {
                    double qx = px - pl * lx, qy = py - pl * ly, qz = pz - pl * lz;
                    double dd = qx * qx + qy * qy + qz * qz;
                    if (dd < 1) a *= 0.18 + 0.82 * SolarTex.sstep(0.9, 1, dd);
                }
                // lit side a bit brighter
                c = SolarTex.shade(c, 0.75 + 0.35 * SolarTex.clamp01(-ang * 0.2 + 0.5));
                if (a < 0.01) continue;
                int v = ((int) (Math.min(1, a) * 255) << 24) | (c & 0xFFFFFF);
                if (uy < 0) back[y * w + x] = v; else front[y * w + x] = v;
            }
    }

    // ================================================================== update
    void update(float dt, float simTime) {
        now = simTime;
        rot += rotSpeed * dt;
        crot += cloudSpeed * dt;
        if (rot > Math.PI * 2) rot -= Math.PI * 2;
        if (crot > Math.PI * 2) crot -= Math.PI * 2;
        tempOff *= (float) Math.pow(0.985, dt);
        lightsOff = Math.max(0, lightsOff - dt / 25f);
        shake = Math.max(0, shake - dt * 1.6f);
        hitFlash = Math.max(0, hitFlash - dt * 3);
        if (destroyed) destroyedT += dt;
        rebuildT += dt;
        Maps m = hi;
        if (m == null) return;
        // cooling of hot tiles (round robin, bounded work per frame)
        int budget = 10, nt = m.hot.length;
        for (int k = 0; k < nt && budget > 0; k++) {
            int t = m.hotPtr = (m.hotPtr + 1) % nt;
            if (!m.hot[t]) continue;
            float el = simTime - m.coolT[t];
            if (el < 0.12f) continue;
            budget--;
            m.coolT[t] = simTime;
            if (!cool(m, t, el)) m.hot[t] = false;
            m.mip(t);
            ver++;
        }
        // mip rebuild of painted tiles
        if (m.dirtyCount > 0) {
            int b2 = 40;
            for (int t = 0; t < nt && b2 > 0; t++) if (m.dirty[t]) { m.mip(t); b2--; m.dirtyCount--; }
            if (b2 > 0) m.dirtyCount = 0;
        }
    }

    private boolean cool(Maps m, int t, float el) {
        int x0 = (t % m.tx) * m.TS, y0 = (t / m.tx) * m.TS;
        boolean any = false;
        float rate = 16 * el;
        for (int y = y0; y < y0 + m.TS && y < m.H; y++)
            for (int x = x0; x < x0 + m.TS && x < m.W; x++) {
                int i = (y << m.shift) | x;
                int h = m.heat[i] & 255;
                if (h == 0) continue;
                int dm = m.dmg[i] & 255;
                int fl = dm >= 180 ? Math.min(215, (dm - 180) * 3) : 0;
                if (m.heat0 != null) fl = Math.max(fl, m.heat0[i] & 255);
                if (h <= fl) continue;
                int nh = Math.max(fl, h - (int) (rate * (1 + h / 110f) + 0.5f));
                m.heat[i] = (byte) nh;
                if (nh > fl) any = true;
            }
        return any;
    }

    // ================================================================== sphere rendering
    static final int[] HEAT = new int[256];
    static {
        for (int h = 0; h < 256; h++) {
            double t = h / 255.0;
            int c = SolarTex.ramp(t, new double[]{0, 0.15, 0.4, 0.7, 1}, new int[]{0xFF000000, 0xFF5A0A00, 0xFFD2380A, 0xFFFF9A2A, 0xFFFFF4C8});
            HEAT[h] = c & 0xFFFFFF;
        }
    }

    /** Pixel geometry of a disc of diameter D (normals in screen space, lat/lon with the body's tilt). */
    static final class Sphere {
        final int D;
        final float tilt;
        final int[] x0, x1, rowStart;
        final char[] lat, lon;
        final float[] nx, ny, nz;
        final byte[] cov;
        final int n;

        Sphere(int D, float tilt) {
            this.D = D;
            this.tilt = tilt;
            x0 = new int[D]; x1 = new int[D]; rowStart = new int[D + 1];
            double R = D / 2.0;
            int cnt = 0;
            for (int y = 0; y < D; y++) {
                double yy = y + 0.5 - R;
                double hw = Math.sqrt(Math.max(0, (R + 0.5) * (R + 0.5) - yy * yy));
                int a = (int) Math.floor(R - hw), b = (int) Math.ceil(R + hw) - 1;
                a = Math.max(0, a); b = Math.min(D - 1, b);
                x0[y] = a; x1[y] = b;
                rowStart[y] = cnt;
                if (b >= a) cnt += b - a + 1;
            }
            rowStart[D] = cnt;
            n = cnt;
            lat = new char[n]; lon = new char[n]; nx = new float[n]; ny = new float[n]; nz = new float[n]; cov = new byte[n];
            double ct = Math.cos(tilt), st = Math.sin(tilt);
            int i = 0;
            for (int y = 0; y < D; y++)
                for (int x = x0[y]; x <= x1[y]; x++, i++) {
                    double fx = (x + 0.5 - R) / R, fy = (y + 0.5 - R) / R;
                    double rp = Math.sqrt(fx * fx + fy * fy) * R;
                    double cv = Math.max(0, Math.min(1, R - rp + 0.5));
                    cov[i] = (byte) (int) (cv * 127);
                    double r2 = fx * fx + fy * fy;
                    if (r2 > 0.9999) { double k = 0.99995 / Math.sqrt(r2); fx *= k; fy *= k; r2 = 0.9999; }
                    double fz = Math.sqrt(1 - r2);
                    nx[i] = (float) fx; ny[i] = (float) fy; nz[i] = (float) fz;
                    double xr = fx * ct + fy * st, yr = -fx * st + fy * ct;
                    double la = Math.asin(Math.max(-1, Math.min(1, -yr)));
                    double lo = Math.atan2(xr, fz);
                    lat[i] = (char) Math.max(0, Math.min(65535, (int) ((0.5 - la / Math.PI) * 65536)));
                    lon[i] = (char) (((int) Math.round(lo / (Math.PI * 2) * 65536)) & 0xFFFF);
                }
        }
    }

    private static final Map<Long, Sphere> SPHERES = new HashMap<>();

    static Sphere sphere(int D, float tilt) {
        long key = ((long) D << 32) ^ Float.floatToIntBits(tilt);
        Sphere s = SPHERES.get(key);
        if (s == null) {
            if (SPHERES.size() > 40) SPHERES.entrySet().removeIf(e -> e.getValue().D > 300);
            s = new Sphere(D, tilt);
            SPHERES.put(key, s);
        }
        return s;
    }

    /** A rendered disc (split into horizontal slabs, each its own texture). */
    static final class Disc {
        final String key;
        int D, slabs, slabH;
        int[][] buf;
        Sphere sp;
        int[] shade;
        byte[] rim;
        float lx = 9, ly, lz;
        int lver = 0;
        int[] slabL;
        boolean[] need;
        int next;
        int lastRot = Integer.MIN_VALUE, lastCrot = Integer.MIN_VALUE, lastVer = -1;
        SolarPlanet lastBody;
        Maps lastMaps;
        boolean lastHi, star;
        int uploads;

        Disc(String key) { this.key = key; }

        void setup(int D, float tilt, boolean star) {
            if (sp != null && this.D == D && sp.tilt == tilt && this.star == star) return;
            this.D = D;
            this.star = star;
            sp = sphere(D, tilt);
            slabs = D <= 200 ? 1 : D <= 420 ? 2 : D <= 760 ? 4 : 8;
            slabH = (D + slabs - 1) / slabs;
            buf = new int[slabs][D * slabH];
            shade = new int[sp.n];
            rim = new byte[sp.n];
            slabL = new int[slabs];
            java.util.Arrays.fill(slabL, -1);
            need = new boolean[slabs];
            java.util.Arrays.fill(need, true);
            lx = 9;
            lastVer = -1;
        }

        void light(float x, float y, float z) {
            if (Math.abs(x - lx) + Math.abs(y - ly) + Math.abs(z - lz) < 0.004f) return;
            lx = x; ly = y; lz = z;
            lver++;
            java.util.Arrays.fill(need, true);
        }

        Gx.Img img(int s) {
            final int[] b = buf[s];
            final int w = D, h = slabH;
            return Gx.get("solar:" + key + ":" + D + ":" + s + ":" + slabs, 0, () -> new int[][]{b, {w, h}});
        }
    }

    /**
     * Brings a disc up to date for this body: marks slabs when rotation / damage / light changed and renders
     * at most pxBudget pixels (whole slabs). Returns true if everything is current.
     */
    boolean refresh(Disc dc, boolean hiLevel, int pxBudget, boolean force) {
        Maps m = maps();
        if (m == null) return false;
        boolean useHi = hiLevel && m.alb1 != m.alb && m == hi;
        int mw = useHi ? m.W : m.W1;
        int r16 = (int) (rot / (Math.PI * 2) * 65536) & 0xFFFF;
        int c16 = (int) (crot / (Math.PI * 2) * 65536) & 0xFFFF;
        int texStep = Math.max(1, 65536 / mw);
        boolean changed = dc.lastBody != this || dc.lastMaps != m || dc.lastHi != useHi || dc.lastVer != ver
                || Math.abs((short) (r16 - dc.lastRot)) >= texStep || Math.abs((short) (c16 - dc.lastCrot)) >= texStep * 2;
        if (changed || force) {
            java.util.Arrays.fill(dc.need, true);
            dc.lastBody = this; dc.lastMaps = m; dc.lastHi = useHi; dc.lastVer = ver; dc.lastRot = r16; dc.lastCrot = c16;
        }
        int done = 0;
        boolean all = true;
        for (int k = 0; k < dc.slabs; k++) {
            int s = (dc.next + k) % dc.slabs;
            if (!dc.need[s]) continue;
            int r0 = s * dc.slabH, r1 = Math.min(dc.D, r0 + dc.slabH);
            int px = dc.sp.rowStart[r1] - dc.sp.rowStart[r0];
            if (!force && done > 0 && done + px > pxBudget) { all = false; continue; }
            render(dc, s, m, useHi, r16, c16);
            dc.need[s] = false;
            Gx.B.update(dc.img(s), dc.buf[s]);
            dc.uploads++;
            done += px;
            dc.next = (s + 1) % dc.slabs;
        }
        return all;
    }

    private void shadeSlab(Disc dc, int r0, int r1) {
        Sphere sp = dc.sp;
        float Lx = dc.lx, Ly = dc.ly, Lz = dc.lz;
        float hx = Lx, hy = Ly, hz = Lz + 1;
        float hl = (float) Math.sqrt(hx * hx + hy * hy + hz * hz);
        hx /= hl; hy /= hl; hz /= hl;
        boolean star = dc.star;
        for (int i = sp.rowStart[r0]; i < sp.rowStart[r1]; i++) {
            float nx = sp.nx[i], ny = sp.ny[i], nz = sp.nz[i];
            int cv = sp.cov[i];
            int lit, night = 0, spec = 0, rm = 0;
            if (star) {
                lit = (int) ((0.42f + 0.62f * Math.pow(nz, 0.45)) * 256);
            } else {
                float d = nx * Lx + ny * Ly + nz * Lz;
                float l = Math.max(0, (d + 0.05f) / 1.05f);
                lit = (int) ((0.035f + 0.965f * Math.pow(l, 0.85)) * 256);
                night = (int) (SolarTex.sstep(0.1, -0.14, d) * 255);
                float sh = nx * hx + ny * hy + nz * hz;
                spec = sh > 0 ? (int) (Math.pow(sh, 70) * 200 + Math.pow(sh, 12) * 26) : 0;
                double rr = Math.pow(1 - nz, 2.2) * (0.12 + 0.88 * SolarTex.sstep(-0.3, 0.55, d));
                rm = (int) Math.min(255, rr * 300);
            }
            dc.shade[i] = Math.min(511, lit) | (night << 9) | (Math.min(255, spec) << 17) | (cv << 25);
            dc.rim[i] = (byte) rm;
        }
    }

    private void render(Disc dc, int s, Maps m, boolean useHi, int r16, int c16) {
        Sphere sp = dc.sp;
        int D = dc.D, r0 = s * dc.slabH, r1 = Math.min(D, r0 + dc.slabH);
        if (dc.slabL[s] != dc.lver) { shadeSlab(dc, r0, r1); dc.slabL[s] = dc.lver; }
        final int[] alb = useHi ? m.alb : m.alb1;
        final byte[] heat = useHi ? m.heat : m.heat1, city = useHi ? m.city : m.city1, cloud = m.cloud;
        final int mw = useHi ? m.W : m.W1, mh = useHi ? m.H : m.H1, sh = useHi ? m.shift : m.shift1;
        final int cw = m.cW, chh = m.cH;
        final char[] lat = sp.lat, lon = sp.lon;
        final float[] nz = sp.nz;
        final int[] shade = dc.shade;
        final byte[] rim = dc.rim;
        final int[] out = dc.buf[s];
        final int[] HP = HEAT;
        float a = Math.max(0, Math.min(1, atmo));
        final int atR = (int) (((atmoCol >> 16) & 255) * a), atG = (int) (((atmoCol >> 8) & 255) * a), atB = (int) ((atmoCol & 255) * a);
        int cc = SolarTex.mix(cloudCol, 0xFF4A4848, dark);
        final int clR = (cc >> 16) & 255, clG = (cc >> 8) & 255, clB = cc & 255;
        final int cloudMul = (int) (256 * Math.min(1, a * 1.1f));
        final int cityMul = (int) (256 * Math.max(0, 1 - lightsOff) * Math.sqrt(Math.max(0, gAlive)) * (destroyed ? 0 : 1));
        final int ciR = (cityCol >> 16) & 255, ciG = (cityCol >> 8) & 255, ciB = cityCol & 255;
        final boolean star = dc.star;
        // crater relief: lighting from the damage depth map (only the full resolution map has it)
        final byte[] dm = useHi ? m.dmg : null;
        final float blx = dc.lx, bly = dc.ly;
        final int wmask = mw - 1;
        java.util.Arrays.fill(out, 0);
        for (int r = r0; r < r1; r++) {
            int xs = sp.x0[r], xe = sp.x1[r];
            if (xe < xs) continue;
            int i = sp.rowStart[r];
            int o = (r - r0) * D + xs;
            for (int x = xs; x <= xe; x++, i++, o++) {
                int la = lat[i], lo = lon[i];
                int row = (la * mh) >>> 16;
                int col = (((lo + r16) & 0xFFFF) * mw) >>> 16;
                int idx = (row << sh) | col;
                int c = alb[idx];
                int sa = c >>> 24;
                int shd = shade[i];
                int cov = (shd >>> 25) << 1;
                if (sa == 1 && nz[i] < 0.42f) { cov = cov * (int) Math.max(0, (nz[i] - 0.3f) * 2000) >> 8; if (cov <= 0) continue; if (cov > 254) cov = 254; }
                int lit = shd & 511;
                if (dm != null && !star) {
                    int d0 = dm[idx] & 255;
                    int rowI = idx >> sh, colI = idx & wmask;
                    int dl = dm[(rowI << sh) | ((colI - 4) & wmask)] & 255, dr = dm[(rowI << sh) | ((colI + 4) & wmask)] & 255;
                    int du = rowI > 3 ? dm[idx - 4 * mw] & 255 : d0, dd = rowI < mh - 4 ? dm[idx + 4 * mw] & 255 : d0;
                    if ((d0 | dl | dr | du | dd) != 0) {
                        // surface height = -depth -> normal tilts towards (dr-dl, dd-du)
                        float bump = ((dr - dl) * blx + (dd - du) * bly) * 6f;
                        lit = (int) (lit * (1f - d0 / 330f) + bump * Math.max(0.25f, lit / 300f));
                        if (lit < 0) lit = 0;
                        if (lit > 480) lit = 480;
                    }
                }
                int R = ((c >> 16) & 255) * lit >> 8, G = ((c >> 8) & 255) * lit >> 8, B = (c & 255) * lit >> 8;
                int h = heat[idx] & 255;
                if (star) {
                    if (h > 0) { int e = HP[h]; R += (e >> 16) & 255; G += (e >> 8) & 255; B += e & 255; }
                } else {
                    int sp2 = (shd >>> 17) & 255;
                    if (sa > 1 && sp2 > 0) { int k = sp2 * sa >> 8; R += k; G += k; B += k + (k >> 2); }
                    int cl = 0;
                    if (cloud != null) {
                        int crow = (la * chh) >>> 16, ccol = (((lo + c16) & 0xFFFF) * cw) >>> 16;
                        cl = ((cloud[crow * cw + ccol] & 255) * cloudMul) >> 8;
                        if (cl > 0) {
                            int cr = clR * lit >> 8, cg = clG * lit >> 8, cb = clB * lit >> 8;
                            R += (cr - R) * cl >> 8; G += (cg - G) * cl >> 8; B += (cb - B) * cl >> 8;
                        }
                    }
                    int night = (shd >>> 9) & 255;
                    if (night > 0) {
                        int ct = city[idx] & 255;
                        if (ct > 0) {
                            int k = (ct * night >> 8) * cityMul >> 8;
                            k = k * (300 - cl) >> 8;
                            R += ciR * k >> 8; G += ciG * k >> 8; B += ciB * k >> 8;
                        }
                    }
                    if (h > 0) { int e = HP[h]; int f = 256 - (cl >> 1); R += ((e >> 16) & 255) * f >> 8; G += ((e >> 8) & 255) * f >> 8; B += (e & 255) * f >> 8; }
                    int rm = rim[i] & 255;
                    if (rm > 0) { R += atR * rm >> 8; G += atG * rm >> 8; B += atB * rm >> 8; }
                }
                if (R > 255) R = 255;
                if (G > 255) G = 255;
                if (B > 255) B = 255;
                out[o] = (cov << 24) | (R << 16) | (G << 8) | B;
            }
        }
    }

    /** Draws a refreshed disc at centre (cx, cy) with radius r (pixels). */
    static void drawDisc(Disc dc, float cx, float cy, float r, int argb) {
        if (dc.sp == null) return;
        float k = 2 * r / dc.D;
        int x = Math.round(cx - r), w = Math.round(cx + r) - x;
        float top = cy - r;
        for (int s = 0; s < dc.slabs; s++) {
            int ya = Math.round(top + s * dc.slabH * k), yb = Math.round(top + (s + 1) * dc.slabH * k);
            if (yb <= ya) continue;
            Gx.image(dc.img(s), x, ya, w, yb - ya, argb);
        }
    }

    /** Full disc image (for explosion fragments): D x D pixels. */
    static int[] snapshot(Disc dc) {
        int[] px = new int[dc.D * dc.D];
        for (int s = 0; s < dc.slabs; s++) {
            int r0 = s * dc.slabH, rows = Math.min(dc.slabH, dc.D - r0);
            System.arraycopy(dc.buf[s], 0, px, r0 * dc.D, rows * dc.D);
        }
        return px;
    }

    /** Average colour of the surface (debris tint). */
    int avgColor() {
        Maps m = maps();
        if (m == null) return 0xFF808080;
        long r = 0, g = 0, b = 0; int n = 0;
        for (int i = 0; i < m.alb1.length; i += 97) { int c = m.alb1[i]; r += (c >> 16) & 255; g += (c >> 8) & 255; b += c & 255; n++; }
        return 0xFF000000 | (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
    }
}
