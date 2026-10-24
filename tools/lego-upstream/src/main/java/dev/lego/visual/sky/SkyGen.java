package dev.lego.visual.sky;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Procedural high resolution skies (no Minecraft classes). Output: cube strip in OptiFine layout (3 x 2 faces):
 * row 0 = bottom, top, south; row 1 = west, north, east. Every pixel is computed from its 3D view direction,
 * so the cube is seamless.
 *
 * <p>Rendering pipeline (fast): the smooth HDR sky of every face is evaluated on a coarse grid (about 512 x 512 per
 * face, independent of the output size) and bilinearly upsampled; point-like details (stars, light motes) are splatted
 * at full resolution; the result is tone mapped with a soft shoulder, gamma corrected via a table and dithered.
 * Presets live in {@link SkyGen2} (day / sunset) and {@link SkyGen3} (night / space).
 */
public final class SkyGen {
    private SkyGen() { }

    public static final String[] IDS = {
            "milkyway", "nebula", "planets", "aurora", "sunset", "storm", "summer",
            "goldenhour", "cottoncandy", "sunrise", "overcast", "thunder", "anime", "alien", "synthwave", "oceandusk",
            "bloodmoon", "eclipse", "auroraviolet", "winter", "twilight",
            "rednebula", "spiral", "blackhole", "void"};
    public static final String[] NAMES = {
            "Sternennacht", "Galaxie-Nebel", "Planeten", "Polarlicht", "Sonnenuntergang", "Gewitternacht", "Sommertag",
            "Goldene Stunde", "Zuckerwatte-Abend", "Nebliger Sonnenaufgang", "Regentag", "Gewitterfront", "Anime-Himmel",
            "Fremder Planet", "Synthwave", "Meeresdämmerung",
            "Blutmond", "Sonnenfinsternis", "Violettes Polarlicht", "Winternacht", "Feen-Dämmerung",
            "Roter Nebel", "Spiralgalaxie", "Schwarzes Loch", "Endleere"};

    /** Direction of face pixel (u,v in 0..1) for face index 0..5 (bottom, top, south, west, north, east). */
    public static void dir(int face, double u, double v, double[] out) {
        double x, y, z;
        switch (face) {
            case 0: x = -1 + 2 * u; y = -1; z = -1 + 2 * v; break;           // bottom
            case 1: x = -1 + 2 * u; y = 1; z = 1 - 2 * v; break;             // top
            case 2: x = 1 - 2 * u; y = 1 - 2 * v; z = 1; break;              // south (+z)
            case 3: x = -1; y = 1 - 2 * v; z = 1 - 2 * u; break;             // west (-x)
            case 4: x = -1 + 2 * u; y = 1 - 2 * v; z = -1; break;            // north (-z)
            default: x = 1; y = 1 - 2 * v; z = -1 + 2 * u; break;            // east (+x)
        }
        double l = Math.sqrt(x * x + y * y + z * z);
        out[0] = x / l; out[1] = y / l; out[2] = z / l;
    }

    /** Inverse of {@link #dir}: writes face-local (u,v) for a direction; returns false if the direction is behind the face. */
    static boolean faceUV(int face, double x, double y, double z, double[] uv) {
        double a;
        switch (face) {
            case 0: a = -y; if (a <= 1e-6) return false; uv[0] = (x / a + 1) * 0.5; uv[1] = (z / a + 1) * 0.5; return true;
            case 1: a = y; if (a <= 1e-6) return false; uv[0] = (x / a + 1) * 0.5; uv[1] = (1 - z / a) * 0.5; return true;
            case 2: a = z; if (a <= 1e-6) return false; uv[0] = (1 - x / a) * 0.5; uv[1] = (1 - y / a) * 0.5; return true;
            case 3: a = -x; if (a <= 1e-6) return false; uv[0] = (1 - z / a) * 0.5; uv[1] = (1 - y / a) * 0.5; return true;
            case 4: a = -z; if (a <= 1e-6) return false; uv[0] = (x / a + 1) * 0.5; uv[1] = (1 - y / a) * 0.5; return true;
            default: a = x; if (a <= 1e-6) return false; uv[0] = (z / a + 1) * 0.5; uv[1] = (1 - y / a) * 0.5; return true;
        }
    }

    // ================================================================== public metadata

    /** "day", "sunset", "night" or "space" (used for time-of-day scheduling). */
    public static String kind(String id) {
        return sky(id).kind;
    }

    private static final ConcurrentHashMap<String, Integer> FOG = new ConcurrentHashMap<>();

    /** Average tone-mapped sky colour just above the horizon as 0xRRGGBB (for tinting the world fog). */
    public static int fogColor(String id) {
        return FOG.computeIfAbsent(id == null ? "" : id, SkyGen::computeFog);
    }

    private static int computeFog(String id) {
        Sky s = sky(id);
        C c = new C();
        double r = 0, g = 0, b = 0;
        int cnt = 0;
        for (int i = 0; i < 64; i++) {
            double az = i * Math.PI * 2 / 64;
            for (int j = 0; j < 4; j++) {
                double el = 0.01 + j * 0.03;
                double ce = Math.cos(el);
                s.evalFull(Math.cos(az) * ce, Math.sin(el), Math.sin(az) * ce, c);
                r += tone8(c.r * s.exposure); g += tone8(c.g * s.exposure); b += tone8(c.b * s.exposure);
                cnt++;
            }
        }
        int ri = (int) Math.round(r / cnt), gi = (int) Math.round(g / cnt), bi = (int) Math.round(b / cnt);
        return (clamp8(ri) << 16) | (clamp8(gi) << 8) | clamp8(bi);
    }

    // ================================================================== generation

    private static volatile ForkJoinPool POOL;

    private static ForkJoinPool pool() {
        ForkJoinPool p = POOL;
        if (p == null) {
            synchronized (SkyGen.class) {
                if (POOL == null) {
                    int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                    POOL = new ForkJoinPool(threads, fp -> {
                        ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(fp);
                        t.setName("LegoClient-SkyGen-" + t.getPoolIndex());
                        t.setDaemon(true);
                        t.setPriority(Thread.MIN_PRIORITY + 1);
                        return t;
                    }, null, false);
                }
                p = POOL;
            }
        }
        return p;
    }

    static void parallel(int count, IntConsumer body) {
        try {
            pool().submit(() -> IntStream.range(0, count).parallel().forEach(body)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable c = e.getCause();
            if (c instanceof RuntimeException) throw (RuntimeException) c;
            throw new RuntimeException(c);
        }
    }

    /** Renders a sky as a 3n x 2n ARGB strip (multi-threaded). */
    public static int[] generate(String id, int n) {
        final Sky s = sky(id);
        final int W = n * 3, H = n * 2;
        // coarse grid: about 512 samples per face edge (fewer for very expensive presets), never more than n
        final int G = Math.max(16, Math.min(n, (int) Math.round(512 * s.detail))), GP = G + 1;
        final float ex = (float) s.exposure;
        // 1) coarse HDR grid (r,g,b,starVisibility) for every face; edge samples are shared exactly between faces
        final float[][] grid = new float[6][GP * GP * 4];
        parallel(6 * GP, idx -> {
            int f = idx / GP, j = idx % GP;
            float[] gr = grid[f];
            C c = new C();
            double[] d = new double[3];
            double v = j / (double) G;
            int o = j * GP * 4;
            for (int i = 0; i < GP; i++, o += 4) {
                dir(f, i / (double) G, v, d);
                s.evalFull(d[0], d[1], d[2], c);
                gr[o] = (float) (c.r * ex);
                gr[o + 1] = (float) (c.g * ex);
                gr[o + 2] = (float) (c.b * ex);
                gr[o + 3] = (float) Math.max(0, Math.min(1, c.vis));
            }
        });
        // 1b) cells covered by fine regions (evaluated per output pixel)
        final boolean[][] fineCell = new boolean[6][];
        if (s.fine != null && s.fine.length > 0) {
            double[] d = new double[3];
            double margin = 3.5 / G;
            for (int f = 0; f < 6; f++) {
                boolean[] fc = null;
                for (int j = 0; j < G; j++)
                    for (int i = 0; i < G; i++) {
                        dir(f, (i + 0.5) / G, (j + 0.5) / G, d);
                        for (double[] q : s.fine) {
                            double cosA = d[0] * q[0] + d[1] * q[1] + d[2] * q[2];
                            if (cosA > Math.cos(q[3] + margin)) {
                                if (fc == null) fc = new boolean[G * G];
                                fc[j * G + i] = true;
                                break;
                            }
                        }
                    }
                fineCell[f] = fc;
            }
        }
        // 2) point sources
        final Stars st = Stars.build(s, n);
        // 3) upsample + stars + tone map + dither
        final int[] px = new int[W * H];
        final double scale = G / (double) n;
        parallel(H, row -> {
            C fc = null;
            double[] fd = null;
            int faceRow = (row / n) * 3;
            int y = row % n;
            double gy = (y + 0.5) * scale;
            int j0 = Math.min(G - 1, (int) gy);
            float fy = (float) (gy - j0);
            int cy = y / Stars.CS;
            int base = row * W;
            for (int col = 0; col < W; col++) {
                int f = faceRow + col / n;
                int x = col % n;
                double gx = (x + 0.5) * scale;
                int i0 = Math.min(G - 1, (int) gx);
                float fx = (float) (gx - i0);
                float[] gr = grid[f];
                int o00 = (j0 * GP + i0) * 4, o10 = o00 + 4, o01 = o00 + GP * 4, o11 = o01 + 4;
                float w00 = (1 - fx) * (1 - fy), w10 = fx * (1 - fy), w01 = (1 - fx) * fy, w11 = fx * fy;
                float r = gr[o00] * w00 + gr[o10] * w10 + gr[o01] * w01 + gr[o11] * w11;
                float g = gr[o00 + 1] * w00 + gr[o10 + 1] * w10 + gr[o01 + 1] * w01 + gr[o11 + 1] * w11;
                float b = gr[o00 + 2] * w00 + gr[o10 + 2] * w10 + gr[o01 + 2] * w01 + gr[o11 + 2] * w11;
                float fineVis = -1;
                boolean[] fcell = fineCell[f];
                if (fcell != null && fcell[j0 * G + i0]) {
                    if (fc == null) { fc = new C(); fd = new double[3]; }
                    dir(f, (x + 0.5) / n, (y + 0.5) / n, fd);
                    s.evalFull(fd[0], fd[1], fd[2], fc);
                    r = (float) (fc.r * ex); g = (float) (fc.g * ex); b = (float) (fc.b * ex);
                    fineVis = (float) Math.max(0, Math.min(1, fc.vis));
                }
                if (st != null) {
                    int cell = (f * st.nc + cy) * st.nc + x / Stars.CS;
                    int a0 = st.start[cell], a1 = st.start[cell + 1];
                    if (a0 != a1) {
                        float vis = fineVis >= 0 ? fineVis : gr[o00 + 3] * w00 + gr[o10 + 3] * w10 + gr[o01 + 3] * w01 + gr[o11 + 3] * w11;
                        float sr = 0, sg = 0, sb = 0, mr = 0, mg = 0, mb = 0;
                        float[] q = st.data;
                        for (int k = a0; k < a1; k++) {
                            int e = st.item[k] * Stars.STRIDE;
                            float dx = x - q[e], dy = y - q[e + 1];
                            float d2 = dx * dx + dy * dy;
                            float i2 = q[e + 5];
                            float arg = d2 * i2;
                            float amp = (arg < 12 ? q[e + 6] * (float) Math.exp(-arg) : 0) + q[e + 7] * (float) Math.exp(-arg * 0.06f);
                            if (q[e + 8] > 0) { sr += q[e + 2] * amp; sg += q[e + 3] * amp; sb += q[e + 4] * amp; }
                            else { mr += q[e + 2] * amp; mg += q[e + 3] * amp; mb += q[e + 4] * amp; }
                        }
                        r += sr * vis + mr; g += sg * vis + mg; b += sb * vis + mb;
                    }
                }
                int h = (col * 73856093) ^ (row * 19349663);
                h ^= h >>> 13; h *= 0x5bd1e995; h ^= h >>> 15;
                float d1 = ((h & 1023) - (h >>> 10 & 1023)) * (1f / 1024f);   // triangular dither, +-1 LSB
                float d2 = (((h >>> 20) & 1023) - (h & 1023)) * (1f / 1024f);
                float d3 = (((h >>> 5) & 1023) - ((h >>> 20) & 1023)) * (1f / 1024f);
                int ri = clamp8((int) (tone(r) + 0.5f + d1));
                int gi = clamp8((int) (tone(g) + 0.5f + d2));
                int bi = clamp8((int) (tone(b) + 0.5f + d3));
                px[base + col] = 0xFF000000 | (ri << 16) | (gi << 8) | bi;
            }
        });
        return px;
    }

    // ------------------------------------------------------------------ tone mapping
    private static final int LUT_N = 4096;
    private static final float[] GAMMA = new float[LUT_N + 2];
    static {
        for (int i = 0; i <= LUT_N + 1; i++) GAMMA[i] = (float) (255.0 * Math.pow(Math.min(1.0, i / (double) LUT_N), 1 / 2.2));
    }

    private static final float KNEE = 0.62f;

    /** Linear HDR -> 0..255 (float, not rounded): linear toe, exponential shoulder, gamma 2.2. */
    static float tone(float v) {
        if (v <= 0) return 0;
        if (v > KNEE) v = KNEE + (1 - KNEE) * (1 - (float) Math.exp(-(v - KNEE) / (1 - KNEE)));
        float p = v * LUT_N;
        int i = (int) p;
        if (i >= LUT_N) return GAMMA[LUT_N];
        float t = p - i;
        return GAMMA[i] + (GAMMA[i + 1] - GAMMA[i]) * t;
    }

    static int tone8(double v) { return clamp8(Math.round(tone((float) v))); }

    private static int clamp8(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

    // ================================================================== presets

    /** Colour of a view direction (linear HDR, without the splatted stars). */
    public interface Preset { void color(double[] d, double[] out); }

    private static final ThreadLocal<C> TL = ThreadLocal.withInitial(C::new);

    public static Preset preset(String id) {
        final Sky s = sky(id);
        return (d, out) -> {
            C c = TL.get();
            s.evalFull(d[0], d[1], d[2], c);
            out[0] = c.r * s.exposure; out[1] = c.g * s.exposure; out[2] = c.b * s.exposure;
        };
    }

    static Sky sky(String id) {
        Sky s = null;
        if (s == null) s = SkyGen2.make(id);
        if (s == null) s = SkyGen3.make(id);
        if (s == null) s = SkyGen3.make("milkyway");
        return s;
    }

    /** Per-sample scratch/result record (one per worker row, never shared). */
    static final class C {
        double r, g, b, vis;
        double t0, t1, t2, t3;
    }

    interface F3 { double f(double x, double y, double z); }

    /** Lower hemisphere treatment. */
    static final int SPACE = 0, GROUND = 1, OCEAN = 2, CLOUDSEA = 3;

    /** A sky preset: {@link #sky} paints the upper hemisphere, {@link #evalFull} adds a seamless lower hemisphere. */
    abstract static class Sky {
        String kind = "day";
        double exposure = 1;
        double detail = 1;          // coarse grid density factor (1 = ~512 samples per face edge)
        int below = GROUND;
        // ground
        double[] ground = rgb(0x1A1E26), groundFar = null;
        double groundFall = 0.22, groundNoise = 0;
        boolean starsBelow = false;
        // ocean
        double[] water = rgb(0x071522);
        double waves = 1, rough = 0.10, glitter = 0, mirror = 1;
        // cloud sea
        Clouds sea;
        double[] seaGap = rgb(0x5C7394);
        // sun (for glitter / cloud lighting)
        double[] sun = norm(0, 1, 0), sunCol = rgb(0xFFF4DC);
        // stars
        int stars = 0;
        double starMin = 0.05, starMax = 4, starSize = 1, starHalo = 1;
        long starSeed = 7;
        // small regions (sun / moon discs, corona) evaluated per output pixel instead of the coarse grid: {x, y, z, angular radius}
        double[][] fine;
        // light motes / particles (not occluded, may float below the horizon)
        int motes = 0;
        double moteBright = 1, moteSize = 1.6;

        double moteDensity(double x, double y, double z) { return 1; }

        void moteColor(Random r, double[] o) { o[0] = 1.3; o[1] = 1.0; o[2] = 0.5; }

        abstract void sky(double x, double y, double z, C c);

        /** Probability that a star candidate at this direction is kept (0..1). */
        double starDensity(double x, double y, double z) { return starDensityFn != null ? starDensityFn.f(x, y, z) : smooth(-0.02, 0.28, y); }

        F3 starDensityFn;

        /** Star colour (linear, ~1) and magnitude hook. */
        void starColor(Random r, double[] o) {
            double t = r.nextDouble();
            if (t < 0.18) { o[0] = 0.70; o[1] = 0.82; o[2] = 1.25; }
            else if (t < 0.45) { o[0] = 0.92; o[1] = 0.96; o[2] = 1.08; }
            else if (t < 0.75) { o[0] = 1.06; o[1] = 1.0; o[2] = 0.90; }
            else if (t < 0.93) { o[0] = 1.18; o[1] = 0.95; o[2] = 0.72; }
            else { o[0] = 1.25; o[1] = 0.78; o[2] = 0.58; }
        }

        final void evalFull(double x, double y, double z, C c) {
            c.r = c.g = c.b = 0; c.vis = 1;
            if (below == SPACE || y >= 0) { sky(x, y, z, c); return; }
            switch (below) {
                case OCEAN: ocean(x, y, z, c); break;
                case CLOUDSEA: cloudSea(x, y, z, c); break;
                default: groundBelow(x, y, z, c); break;
            }
        }

        /** Sky exactly at the horizon in the azimuth of (x,z). */
        final void horizon(double x, double z, C c) {
            double hl = Math.sqrt(x * x + z * z);
            if (hl < 1e-9) { x = 1; z = 0; hl = 1; }
            c.r = c.g = c.b = 0; c.vis = 1;
            sky(x / hl, 0, z / hl, c);
        }

        void groundBelow(double x, double y, double z, C c) {
            horizon(x, z, c);
            double t = 1 - Math.exp(y / groundFall);        // y < 0
            double[] g = ground;
            double gr = g[0], gg = g[1], gb = g[2];
            if (groundFar != null) {
                double k = Math.exp(y / (groundFall * 2.5));
                gr += (groundFar[0] - gr) * k; gg += (groundFar[1] - gg) * k; gb += (groundFar[2] - gb) * k;
            }
            if (groundNoise > 0) {
                double k = 1 / (-y + 0.02);
                double px = x * k, pz = z * k;
                double tex = 1 + groundNoise * smooth(0.0, 0.3, -y) * fbmL(px * 0.6, 2.7, pz * 0.6, Math.max(2, 5 - log2(1 + k * 0.3)));
                gr *= tex; gg *= tex; gb *= tex;
            }
            c.r += (gr - c.r) * t; c.g += (gg - c.g) * t; c.b += (gb - c.b) * t;
            c.vis = starsBelow ? 1 : Math.max(0, 1 - t * 8) * c.vis;
        }

        void ocean(double x, double y, double z, C c) {
            double ny = -y;                                   // > 0
            double k = 1 / (ny + 0.015);
            double px = x * k, pz = z * k;
            double near = smooth(0.0, 0.25, ny);
            double amp = waves * 0.06 * near;
            double rx = x, ry = ny, rz = z;
            if (amp > 0) {
                double s = 0.9;
                double nx = noise(px * s, 0.37, pz * s) + 0.5 * noise(px * s * 2.7, 1.7, pz * s * 2.7);
                double nz = noise(px * s + 11.3, 4.1, pz * s) + 0.5 * noise(px * s * 2.7 + 5.1, 2.9, pz * s * 2.7);
                rx += nx * amp; rz += nz * amp;
                ry += ny * 0.3 * noise(px * 0.7, 8.3, pz * 0.7) * near;
            }
            ry = Math.max(0.0, ry) * mirror;
            double l = Math.sqrt(rx * rx + ry * ry + rz * rz);
            c.r = c.g = c.b = 0; c.vis = 1;
            sky(rx / l, ry / l, rz / l, c);
            double m = 1 - ny;
            double m2 = m * m;
            double F = 0.03 + 0.97 * m2 * m2 * m;
            double far = Math.exp(-ny / 0.012);            // exact continuity at the horizon
            F = F + (1 - F) * far;
            c.r = water[0] + (c.r - water[0]) * F;
            c.g = water[1] + (c.g - water[1]) * F;
            c.b = water[2] + (c.b - water[2]) * F;
            if (glitter > 0) {
                // half vector between mirrored view dir and sun -> required facet slope
                double hx = sun[0] - x, hy = ny + sun[1], hz = sun[2] - z;
                if (hy > 1e-4 && sun[1] > -0.05) {
                    double t2 = (hx * hx + hz * hz) / (hy * hy);
                    double gl = Math.exp(-t2 / (rough * rough)) * glitter;
                    if (gl > 1e-4) {
                        double sp = 0.55 + 0.9 * Math.max(0, noise(px * 6, 3.3, pz * 6 * 0.3));
                        gl *= sp * (1 - far);
                        c.r += sunCol[0] * gl; c.g += sunCol[1] * gl; c.b += sunCol[2] * gl;
                    }
                }
            }
            c.vis = 0;
        }

        void cloudSea(double x, double y, double z, C c) {
            horizon(x, z, c);
            double hr = c.r, hg = c.g, hb = c.b;
            double ny = -y;
            Clouds P = sea;
            double k = P.height / (ny + 0.02);
            double px = x * k * P.scale, pz = z * k * P.scale;
            double oct = Math.max(2, P.oct - log2(1 + k * P.scale * 0.35));
            double wx = fbmL(px * 0.35, P.seed, pz * 0.35, 3), wz = fbmL(px * 0.35 + 5.2, P.seed + 3, pz * 0.35 + 1.3, 3);
            double qx = px + wx * P.warp, qz = pz + wz * P.warp;
            double n = fbmL(qx, P.seed + 10, qz, oct);
            double dens = smooth(P.cov, P.cov + P.soft, n);
            // lit from above: sun-facing slopes brighter
            double n2 = fbmL(qx + sun[0] * 0.3, P.seed + 10, qz + sun[2] * 0.3, Math.max(2, oct - 2));
            double slope = clamp01((n - n2) * 2.5 + 0.55);
            double[] L = P.lit, S = P.shade;
            double lr = S[0] + (L[0] - S[0]) * slope, lg = S[1] + (L[1] - S[1]) * slope, lb = S[2] + (L[2] - S[2]) * slope;
            double cr = seaGap[0] + (lr - seaGap[0]) * dens, cg = seaGap[1] + (lg - seaGap[1]) * dens, cb = seaGap[2] + (lb - seaGap[2]) * dens;
            double haze = Math.exp(-ny / P.fog);
            c.r = cr + (hr - cr) * haze; c.g = cg + (hg - cg) * haze; c.b = cb + (hb - cb) * haze;
            c.vis = 0;
        }
    }

    // ================================================================== shared building blocks

    /** Parameters for a cloud layer projected on a plane (above or below the viewer). */
    static final class Clouds {
        double height = 1, scale = 1, cov = 0.05, soft = 0.3, warp = 0.8, oct = 6, seed = 0, fog = 12, opacity = 1;
        double stretch = 1;           // >1 = elongated along x (cirrus / streaks)
        double lightSide = 1;         // sun-side edge brightening strength
        double silver = 1;            // forward scattering near the sun
        double thickDark = 0.55;      // darker bases in thick parts
        double[] lit = rgb(0xFFFFFF), shade = rgb(0x8E9AB0);
    }

    /** Overhead cloud layer composited onto c (uses sun direction s). */
    static void clouds(C c, double x, double y, double z, Clouds P, double[] s, double[] sunCol) {
        if (y < 0.003) return;
        double fade = smooth(0.003, 0.09, y);
        double k = P.height / (y + 0.03);
        double px = x * k * P.scale / P.stretch, pz = z * k * P.scale;
        double dist = k * P.scale;
        double oct = Math.max(2, P.oct - log2(1 + dist * 0.35));
        double wx = fbmL(px * 0.35, P.seed, pz * 0.35, 3), wz = fbmL(px * 0.35 + 5.2, P.seed + 3, pz * 0.35 + 1.3, 3);
        double qx = px + wx * P.warp, qz = pz + wz * P.warp;
        double n = fbmL(qx, P.seed + 10, qz, oct);
        double dens = smooth(P.cov, P.cov + P.soft, n);
        if (dens <= 0.002) return;
        double shx = s[0], shz = s[2], hl = Math.sqrt(shx * shx + shz * shz) + 1e-6;
        double off = 0.22 * Math.min(1, hl * 1.5);
        double n2 = fbmL(qx + shx / hl * off, P.seed + 10, qz + shz / hl * off, Math.max(2, oct - 2));
        double edge = clamp01((n - n2) * 3.0 + 0.5);
        double thick = smooth(P.cov, P.cov + P.soft * 2.8, n);
        double light = (0.62 + (edge - 0.5) * 0.9 * P.lightSide) * (1 - P.thickDark * thick);
        light = clamp01(light);
        double[] L = P.lit, S = P.shade;
        double cr = S[0] + (L[0] - S[0]) * light, cg = S[1] + (L[1] - S[1]) * light, cb = S[2] + (L[2] - S[2]) * light;
        double sd = x * s[0] + y * s[1] + z * s[2];
        if (sd > 0 && P.silver > 0) {
            double sd2 = sd * sd, sd4 = sd2 * sd2;
            double sv = sd4 * sd4 * (1 - thick) * P.silver;
            cr += sunCol[0] * sv; cg += sunCol[1] * sv; cb += sunCol[2] * sv;
        }
        double air = 1 - Math.exp(-dist / P.fog);
        cr += (c.r - cr) * air; cg += (c.g - cg) * air; cb += (c.b - cb) * air;
        double a = dens * fade * P.opacity;
        c.r += (cr - c.r) * a; c.g += (cg - c.g) * a; c.b += (cb - c.b) * a;
        c.vis *= 1 - Math.min(1, a * 1.4);
    }

    /** Distant cumulus towers standing on the horizon (seen from the side: lit tops, darker hazy bases). */
    static final class Towers {
        double h0 = 0.02, h1 = 0.16, freq = 2.2, cut = 0.0, bill = 7, puff = 1, soft = 0.015, seed = 0, haze = 0.035;
        double sunSide = 0.35;
        double[] lit = rgb(0xFFFFFF), shade = rgb(0x8C9AB2);
    }

    static void towers(C c, double x, double y, double z, Towers T, double[] s) {
        double top = T.h0 + T.h1;
        if (y > top + 0.08 || y < -0.02) return;
        double hl = Math.sqrt(x * x + z * z) + 1e-9;
        double ux = x / hl, uz = z / hl;
        double prof = fbmL(ux * T.freq, T.seed, uz * T.freq, 4);
        double hgt = T.h0 + T.h1 * smooth(T.cut, T.cut + 0.35, prof);
        if (y > hgt + 0.07) return;
        double b = fbmL(ux * T.bill, y * T.bill * 1.25 + T.seed * 3.1, uz * T.bill, 5);
        double v = hgt - y + b * 0.06 * T.puff;
        double a = smooth(0, T.soft, v);
        if (a <= 0.002) return;
        double b2 = fbmL(ux * T.bill, (y - 0.012) * T.bill * 1.25 + T.seed * 3.1, uz * T.bill, 3);
        double edge = clamp01((b - b2) * 4 + 0.5);
        double rel = clamp01(y / Math.max(0.01, hgt + 0.03));
        double shx = s[0], shz = s[2], sl = Math.sqrt(shx * shx + shz * shz) + 1e-6;
        double side = (ux * shx + uz * shz) / sl;
        double light = smooth(-0.1, 1.0, rel * 0.9 + (edge - 0.5) * 0.8 + b * 0.5) * (1 - T.sunSide + T.sunSide * (0.5 + 0.5 * side));
        light = clamp01(light);
        double[] L = T.lit, S = T.shade;
        double cr = S[0] + (L[0] - S[0]) * light, cg = S[1] + (L[1] - S[1]) * light, cb = S[2] + (L[2] - S[2]) * light;
        double hz = Math.exp(-Math.max(0, y) / T.haze);
        cr += (c.r - cr) * hz; cg += (c.g - cg) * hz; cb += (c.b - cb) * hz;
        c.r += (cr - c.r) * a; c.g += (cg - c.g) * a; c.b += (cb - c.b) * a;
        c.vis *= 1 - a;
    }

    /** Sun disk with limb darkening plus layered bloom. cosA = dot(dir, sunDir). */
    static void sunDisk(C c, double cosA, double radius, double[] col, double disk, double bloom1, double w1, double bloom2, double w2) {
        double a2 = 2 * (1 - cosA);                        // ~ angle^2
        double ang = Math.sqrt(Math.max(0, a2));
        double rr = ang / radius;
        double k = 0;
        if (rr < 1.15) {
            double e = smooth(1.12, 0.92, rr);
            double mu = Math.sqrt(Math.max(0, 1 - Math.min(1, rr * rr)));
            k += disk * e * (0.55 + 0.45 * mu);
        }
        k += bloom1 * Math.exp(-ang / w1) + bloom2 * Math.exp(-a2 / (w2 * w2));
        c.r += col[0] * k; c.g += col[1] * k; c.b += col[2] * k;
        if (rr < 1.1) c.vis *= smooth(0.8, 1.1, rr);
    }

    // ================================================================== star splatting

    static final class Stars {
        static final int CS = 16, STRIDE = 9;
        int nc;
        int[] start, item;
        float[] data;        // per entry: x, y, r, g, b, 1/(2 sigma^2), amp, haloAmp, occludable

        static Stars build(Sky s, int n) {
            if (s.stars <= 0 && s.motes <= 0) return null;
            Random r = new Random(s.starSeed * 7919L + 17);
            double res = Math.sqrt(n / 1024.0);
            float ex = (float) s.exposure;
            int nc = (n + CS - 1) / CS;
            FloatList ents = new FloatList();
            IntList faceOf = new IntList();
            double[] col = new double[3], uv = new double[2];
            int total = s.stars + s.motes;
            for (int i = 0; i < total; i++) {
                boolean mote = i >= s.stars;
                double zz = r.nextDouble() * 2 - 1, ph = r.nextDouble() * Math.PI * 2, rq = Math.sqrt(1 - zz * zz);
                double x = rq * Math.cos(ph), y = zz, z = rq * Math.sin(ph);
                double u0 = r.nextDouble(), u1 = r.nextDouble();
                double sig, amp, halo;
                if (!mote) {
                    if (u0 > s.starDensity(x, y, z)) continue;
                    // power law brightness: many faint, few bright
                    double b = Math.min(s.starMax, s.starMin * Math.pow(Math.max(1e-6, u1), -0.72));
                    s.starColor(r, col);
                    sig = (0.36 + 0.16 * Math.log(b / s.starMin + 1)) * s.starSize * res;
                    amp = b;
                    halo = b > 1.0 ? 0.010 * b * s.starHalo : 0;
                } else {
                    if (u0 > s.moteDensity(x, y, z)) continue;
                    s.moteColor(r, col);
                    double b = s.moteBright * (0.25 + 1.2 * Math.pow(u1, 3));
                    sig = (0.8 + s.moteSize * Math.pow(r.nextDouble(), 3)) * res;
                    amp = b;
                    halo = b * 0.10;
                }
                double rad = sig * (halo > 0 ? 12.0 : 3.6);
                rad = Math.min(rad, CS * 3.0);
                float i2 = (float) (0.5 / (sig * sig));
                for (int f = 0; f < 6; f++) {
                    if (!faceUV(f, x, y, z, uv)) continue;
                    double fx = uv[0] * n - 0.5, fy = uv[1] * n - 0.5;
                    if (fx < -rad || fy < -rad || fx > n - 1 + rad || fy > n - 1 + rad) continue;
                    ents.add((float) fx); ents.add((float) fy);
                    ents.add((float) col[0] * ex); ents.add((float) col[1] * ex); ents.add((float) col[2] * ex);
                    ents.add(i2); ents.add((float) amp); ents.add((float) halo); ents.add(mote ? 0f : 1f);
                    faceOf.add(f);
                    faceOf.add((int) Math.ceil(rad));
                }
            }
            Stars st = new Stars();
            st.nc = nc;
            st.data = ents.toArray();
            int cells = 6 * nc * nc;
            int[] cnt = new int[cells + 1];
            int m = st.data.length / STRIDE;
            for (int pass = 0; pass < 2; pass++) {
                if (pass == 1) {
                    int acc = 0;
                    for (int c = 0; c < cells; c++) { int t = cnt[c]; cnt[c] = acc; acc += t; }
                    cnt[cells] = acc;
                    st.start = cnt.clone();
                    st.item = new int[acc];
                }
                for (int e = 0; e < m; e++) {
                    int f = faceOf.get(e * 2), rad = faceOf.get(e * 2 + 1);
                    float fx = st.data[e * STRIDE], fy = st.data[e * STRIDE + 1];
                    int x0 = Math.max(0, (int) Math.floor((fx - rad) / CS)), x1 = Math.min(nc - 1, (int) Math.floor((fx + rad) / CS));
                    int y0 = Math.max(0, (int) Math.floor((fy - rad) / CS)), y1 = Math.min(nc - 1, (int) Math.floor((fy + rad) / CS));
                    for (int cy = y0; cy <= y1; cy++)
                        for (int cx = x0; cx <= x1; cx++) {
                            int c = (f * nc + cy) * nc + cx;
                            if (pass == 0) cnt[c]++;
                            else st.item[cnt[c]++] = e;
                        }
                }
            }
            return st;
        }
    }

    static final class FloatList {
        float[] a = new float[4096]; int n;
        void add(float v) { if (n == a.length) a = java.util.Arrays.copyOf(a, n * 2); a[n++] = v; }
        float[] toArray() { return java.util.Arrays.copyOf(a, n); }
    }

    static final class IntList {
        int[] a = new int[1024]; int n;
        void add(int v) { if (n == a.length) a = java.util.Arrays.copyOf(a, n * 2); a[n++] = v; }
        int get(int i) { return a[i]; }
    }

    // ================================================================== noise
    private static final int[] P = new int[512];
    static {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        Random r = new Random(1337);
        for (int i = 255; i > 0; i--) { int j = r.nextInt(i + 1); int t = p[i]; p[i] = p[j]; p[j] = t; }
        for (int i = 0; i < 512; i++) P[i] = p[i & 255];
    }

    private static int fl(double v) { int i = (int) v; return v < i ? i - 1 : i; }

    private static double grad(int h, double x, double y, double z) {
        switch (h & 15) {
            case 0: return x + y; case 1: return -x + y; case 2: return x - y; case 3: return -x - y;
            case 4: return x + z; case 5: return -x + z; case 6: return x - z; case 7: return -x - z;
            case 8: return y + z; case 9: return -y + z; case 10: return y - z; case 11: return -y - z;
            case 12: return y + x; case 13: return -y + z; case 14: return y - x; default: return -y - z;
        }
    }

    /** Improved Perlin noise, range about -1..1. */
    public static double noise(double x, double y, double z) {
        int xi = fl(x), yi = fl(y), zi = fl(z);
        x -= xi; y -= yi; z -= zi;
        int X = xi & 255, Y = yi & 255, Z = zi & 255;
        double u = x * x * x * (x * (x * 6 - 15) + 10), v = y * y * y * (y * (y * 6 - 15) + 10), w = z * z * z * (z * (z * 6 - 15) + 10);
        int A = P[X] + Y, AA = P[A] + Z, AB = P[A + 1] + Z, B = P[X + 1] + Y, BA = P[B] + Z, BB = P[B + 1] + Z;
        double x1 = x - 1, y1 = y - 1, z1 = z - 1;
        double a0 = grad(P[AA], x, y, z), a1 = grad(P[BA], x1, y, z);
        double b0 = grad(P[AB], x, y1, z), b1 = grad(P[BB], x1, y1, z);
        double c0 = grad(P[AA + 1], x, y, z1), c1 = grad(P[BA + 1], x1, y, z1);
        double d0 = grad(P[AB + 1], x, y1, z1), d1 = grad(P[BB + 1], x1, y1, z1);
        double l0 = a0 + u * (a1 - a0), l1 = b0 + u * (b1 - b0), l2 = c0 + u * (c1 - c0), l3 = d0 + u * (d1 - d0);
        double m0 = l0 + v * (l1 - l0), m1 = l2 + v * (l3 - l2);
        return m0 + w * (m1 - m0);
    }

    public static double fbm(double x, double y, double z, int oct) { return fbmL(x, y, z, oct); }

    /** fbm with a fractional octave count (smooth level of detail, no popping). */
    static double fbmL(double x, double y, double z, double oct) {
        double s = 0, a = 0.5;
        int n = (int) oct;
        double frac = oct - n;
        for (int i = 0; i < n; i++) {
            s += a * noise(x, y, z);
            // rotate + scale between octaves to hide the lattice
            double nx = x * 1.62 + z * 1.18, nz = z * 1.62 - x * 1.18;
            x = nx + 1.7; z = nz - 3.1; y = y * 2.01 + 0.73;
            a *= 0.5;
        }
        if (frac > 0.001) s += a * frac * noise(x, y, z);
        return s;
    }

    public static double ridged(double x, double y, double z, int oct) {
        double s = 0, a = 0.5;
        for (int i = 0; i < oct; i++) {
            double n = 1 - Math.abs(noise(x, y, z));
            s += a * n * n;
            double nx = x * 1.62 + z * 1.18, nz = z * 1.62 - x * 1.18;
            x = nx + 1.7; z = nz - 3.1; y = y * 2.01 + 0.73;
            a *= 0.5;
        }
        return s;
    }

    // ================================================================== small math helpers
    static double smooth(double e0, double e1, double x) { double t = (x - e0) / (e1 - e0); t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }
    static double clamp01(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    static double log2(double v) { return Math.log(v) * 1.4426950408889634; }
    static double mix(double a, double b, double t) { return a + (b - a) * t; }

    /** sRGB hex -> linear rgb triple. */
    static double[] rgb(int hex) {
        return new double[]{Math.pow(((hex >> 16) & 255) / 255.0, 2.2), Math.pow(((hex >> 8) & 255) / 255.0, 2.2), Math.pow((hex & 255) / 255.0, 2.2)};
    }

    static double[] norm(double x, double y, double z) { double l = Math.sqrt(x * x + y * y + z * z); return new double[]{x / l, y / l, z / l}; }

    /** Direction from azimuth (deg, 0 = north/-z, 90 = east/+x) and elevation (deg). */
    static double[] azel(double azDeg, double elDeg) {
        double a = Math.toRadians(azDeg), e = Math.toRadians(elDeg);
        return new double[]{Math.sin(a) * Math.cos(e), Math.sin(e), -Math.cos(a) * Math.cos(e)};
    }

    static void add(C c, double[] col, double k) { c.r += col[0] * k; c.g += col[1] * k; c.b += col[2] * k; }
    static void set(C c, double[] col) { c.r = col[0]; c.g = col[1]; c.b = col[2]; }
    static void mixTo(C c, double[] col, double t) { c.r += (col[0] - c.r) * t; c.g += (col[1] - c.g) * t; c.b += (col[2] - c.b) * t; }
    static void mul(C c, double k) { c.r *= k; c.g *= k; c.b *= k; }

    static double[] cross(double[] a, double[] b) {
        double[] r = {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
        double l = Math.sqrt(r[0] * r[0] + r[1] * r[1] + r[2] * r[2]);
        return new double[]{r[0] / l, r[1] / l, r[2] / l};
    }

    /** Tangent frame around a direction: returns {ex, ey} (ey points roughly "up" in the sky). */
    static double[][] frame(double[] d, double rollDeg) {
        double[] up = Math.abs(d[1]) < 0.95 ? new double[]{0, 1, 0} : new double[]{1, 0, 0};
        double[] ex = cross(up, d), ey = cross(d, ex);
        double a = Math.toRadians(rollDeg), ca = Math.cos(a), sa = Math.sin(a);
        double[] rx = {ex[0] * ca + ey[0] * sa, ex[1] * ca + ey[1] * sa, ex[2] * ca + ey[2] * sa};
        double[] ry = {ey[0] * ca - ex[0] * sa, ey[1] * ca - ex[1] * sa, ey[2] * ca - ex[2] * sa};
        return new double[][]{rx, ry};
    }

    /** Hash -> 0..1. */
    static double hash(int x, int y, int z, int k) {
        long h = x * 374761393L + y * 668265263L + z * 2147483647L + k * 144665L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (double) 0xFFFFFF;
    }
}
