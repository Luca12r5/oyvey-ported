package dev.lego.cosmetic;

import dev.lego.cosmetic.Pets.P;

import static dev.lego.cosmetic.PetGeo.blob;
import static dev.lego.cosmetic.PetGeo.cos;
import static dev.lego.cosmetic.PetGeo.sin;

/**
 * Animation rig shared by all pets: quadruped gait (trot / gallop with foot trajectories and two bone IK legs),
 * body frame (bob, roll, pitch, sit, lie, play bow), biped waddle, hop and flap helpers, growth accessories.
 * Everything is derived from Cos.A (time, speed, emotion, growth, level) only - no hidden state.
 */
final class PetRig {
    private PetRig() { }

    /** Step frequencies are constant (a.time is large, a speed dependent frequency would make the phase jump). */
    static final float WALK_HZ = 1.55f, RUN_HZ = 2.35f, WADDLE_HZ = 2.1f, HOP_HZ = 1.45f;
    /** Leg order: 0 = front left, 1 = front right, 2 = hind left, 3 = hind right. Trot: diagonal pairs. */
    static final float[] WALK_OFF = {0f, 0.5f, 0.53f, 0.03f};
    /** Rotary gallop: hind pair lands first, then the front pair. */
    static final float[] RUN_OFF = {0.5f, 0.6f, 0f, 0.1f};
    static final float WALK_DUTY = 0.6f, RUN_DUTY = 0.38f;

    static float clamp01(float v) { return v < 0 ? 0 : v > 1 ? 1 : v; }
    static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    static float frac(float x) { return x - (float) Math.floor(x); }
    static float smooth(float e0, float e1, float x) { float t = clamp01((x - e0) / (e1 - e0)); return t * t * (3 - 2 * t); }
    /** 1 inside [a, b] of a periodic phase x (0..1) with soft ramps. */
    static float window(float x, float a, float b, float ramp) { return smooth(a, a + ramp, x) * (1 - smooth(b - ramp, b, x)); }

    // ================================================================== pose part (called from Pets.pose)
    static void gait(P p) {
        float t = p.t, sp = p.sp;
        p.walkW = clamp01(sp / 0.5f);
        p.runW = smooth(0.62f, 0.92f, sp);
        p.idle = 1 - smooth(0.03f, 0.2f, sp);
        p.cw = frac(t * WALK_HZ);
        p.cr = frac(t * RUN_HZ);
        float ww = p.walkW * (1 - p.runW);
        double aw = Math.PI * 2 * p.cw, ar = Math.PI * 2 * p.cr;
        // body motion (bob in leg lengths, angles in degrees)
        p.bob = ww * 0.045f * sin(aw * 2 + 0.6) + p.runW * (0.13f * sin(ar - 0.9) - 0.02f);
        p.roll = ww * 2.6f * sin(aw) + p.runW * 1.2f * sin(ar);
        p.pitch = p.runW * 8.5f * sin(ar + 1.25);
        p.stretch = 1 + p.runW * 0.075f * sin(ar - 0.35);
        p.nod = ww * 5.5f * sin(aw * 2 + 1.7) - p.pitch * 0.75f;
        p.tailSway = ww * 11 * sin(aw + 0.7) + p.runW * 6 * sin(ar);
        p.tailLift += p.runW * 16;
        // waddle (bipeds): roll towards the standing foot
        double wd = Math.PI * 2 * frac(t * WADDLE_HZ);
        p.waddle = p.walkW * 9 * sin(wd) + p.idle * 1.2f * sin(t * 1.3f);
        p.waddleBob = p.walkW * 0.35f * Math.abs(sin(wd));
        // idle behaviours
        float id = p.idle;
        int cycle = (int) Math.floor(t / 11.3f);
        float glance = window(frac(t / 11.3f), 0.12f, 0.5f, 0.1f) * id;
        p.headYaw += glance * 34 * ((cycle & 1) == 0 ? 1 : -1);
        p.headRoll += glance * 5 * ((cycle & 1) == 0 ? 1 : -1);
        p.sniff = window(frac(t / 17.1f + 0.3f), 0.55f, 0.78f, 0.06f) * id;
        p.headPitch += p.sniff * (20 + sin(t * 15) * 3);
        boolean calm = p.is("happy") || p.is("love") || p.is("curious") || p.is("sad");
        p.sit = calm ? window(frac(t / 37f), 0.32f, 0.86f, 0.05f) * id : 0;
        p.sniff *= 1 - p.sit;
        if (p.is("excited")) {
            p.bow = window(frac(t / 4.3f), 0.08f, 0.46f, 0.1f) * id;
            p.y *= 1 - p.bow;
        }
        if (p.is("angry")) p.crouch = 1;
        p.lie *= 1 - p.sit;
    }

    // ================================================================== body frame (pitch around the hind hip + drop)
    static final class Frame {
        float pu, dy, pivY, pivZ, c = 1, s;
        void set(float pitchUp) { pu = pitchUp; c = cos(Math.toRadians(pu)); s = sin(Math.toRadians(pu)); }
        float y(float y, float z) { return c * (y - pivY) + s * (z - pivZ) + pivY + dy; }
        float z(float y, float z) { return -s * (y - pivY) + c * (z - pivZ) + pivZ; }
        void apply(G g) {
            g.translate(0, dy + pivY, pivZ);
            g.rotX(-pu);
            g.translate(0, -pivY, -pivZ);
        }
    }

    /**
     * @param hipH  standing hip / shoulder height
     * @param zs    shoulder z, zh hip z
     * @param L     leg length
     * @param belly height of the body bottom when standing
     */
    static Frame frame(P p, float hipH, float zs, float zh, float L, float belly) {
        Frame f = new Frame();
        f.pivY = hipH;
        f.pivZ = zh;
        float span = Math.max(0.5f, zs - zh);
        float dropSit = Math.max(0, hipH - Math.max(L * 0.4f, belly * 0.4f));
        float sitAng = (float) Math.toDegrees(Math.asin(Math.min(0.75f, dropSit / span)));
        float bowDrop = Math.max(0, hipH - L * 0.45f) * 0.8f;
        float bowAng = (float) Math.toDegrees(Math.asin(Math.min(0.6f, bowDrop / span)));
        float dropLie = Math.max(0, belly - 0.08f);
        f.set(p.pitch * (1 - p.sit) + sitAng * p.sit - bowAng * p.bow);
        f.dy = p.bob * L - dropSit * p.sit - dropLie * p.lie - p.crouch * L * 0.1f;
        return f;
    }

    // ================================================================== legs
    /** Foot target offset for leg i: out[0] = dz, out[1] = lift, out[2] = swing amount (0..1), in leg lengths. */
    static void foot(P p, int i, float[] out) {
        float move = (1 - p.sit) * (1 - p.lie) * (1 - p.bow);
        float ww = p.walkW * (1 - p.runW) * move, rw = p.runW * move;
        float z = 0, y = 0, sw = 0;
        if (ww > 0.001f) {
            float[] o = step(frac(p.cw + WALK_OFF[i]), WALK_DUTY, 0.62f, 0.26f);
            z += o[0] * ww; y += o[1] * ww; sw += o[2] * ww;
        }
        if (rw > 0.001f) {
            float[] o = step(frac(p.cr + RUN_OFF[i]), RUN_DUTY, 1.2f, 0.42f);
            z += o[0] * rw; y += o[1] * rw; sw += o[2] * rw;
        }
        out[0] = z; out[1] = y; out[2] = sw;
    }

    /** One step cycle: stance slides the planted foot backwards, swing lifts it forward on an arc. */
    private static float[] step(float c, float duty, float stride, float lift) {
        if (c < duty) {
            float u = c / duty;
            return new float[]{stride * (0.5f - u), 0, 0};
        }
        float u = (c - duty) / (1 - duty);
        float sm = u * u * (3 - 2 * u);
        return new float[]{stride * (-0.5f + sm), lift * sin(Math.PI * u), sin(Math.PI * u)};
    }

    /** Leg look. */
    static final class LegStyle {
        String up, lo, paw;
        float r1, r2, pawR, pawLen = 1.3f;
        /** hoof = cylinder-ish dark hoof instead of a round paw. */
        boolean hoof;
        int seg = 6;
        LegStyle(String up, String lo, String paw, float r1, float r2, float pawR) { this.up = up; this.lo = lo; this.paw = paw; this.r1 = r1; this.r2 = r2; this.pawR = pawR; }
    }

    /**
     * Two bone leg from the hip (hx, hy, hz) to the ankle target (fz, fy) (root space), solved in the y-z plane.
     * @param bend +1 = joint bulges forward (front legs), -1 = backwards (hind legs / hock)
     */
    static void ikLeg(G g, LegStyle st, float hx, float hy, float hz, float fz, float fy, float L1, float L2, int bend, float curl) {
        float dz = fz - hz, dyv = fy - hy;
        float d = (float) Math.sqrt(dz * dz + dyv * dyv);
        float dMin = Math.abs(L1 - L2) + 0.08f * (L1 + L2), dMax = (L1 + L2) * 0.995f;
        d = Math.max(dMin, Math.min(dMax, d));
        double a = Math.atan2(dz, -dyv);
        double cosA = (L1 * L1 + d * d - L2 * L2) / (2 * L1 * d);
        double al = Math.acos(Math.max(-1, Math.min(1, cosA)));
        double t1 = a + bend * al;
        float jz = (float) Math.sin(t1) * L1, jy = -(float) Math.cos(t1) * L1;
        float ez = (float) Math.sin(a) * d, ey = -(float) Math.cos(a) * d;
        double t2 = Math.atan2(ez - jz, -(ey - jy));
        float d1 = (float) Math.toDegrees(t1), d2 = (float) Math.toDegrees(t2);
        g.push();
        g.translate(hx, hy, hz);
        g.rotX(-d1);
        int hs = Math.max(st.seg, 10);
        blob(g, st.up, 0, -L1 * 0.45f, 0, st.r1, L1 * 0.55f + st.r1 * 0.25f, st.r1 * 1.05f, hs, 5);
        g.translate(0, -L1, 0);
        g.rotX(-(d2 - d1));
        blob(g, st.lo, 0, -L2 * 0.5f, 0, st.r2, L2 * 0.55f + st.r2 * 0.2f, st.r2, hs - 1, 5);
        g.translate(0, -L2, 0);
        g.rotX(d2 + curl);
        if (st.hoof) {
            g.cylinder(st.paw, st.pawR * 0.95f, st.pawR * 1.08f, -st.pawR * 0.9f, st.pawR * 0.35f, 10, true, 0, 0, 1, 1);
        } else PetGeo.blob2(g, st.paw, 0, -st.pawR * 0.15f, st.pawR * 0.35f, st.pawR * 1.05f, st.pawR * 0.85f, st.pawR * 0.45f, st.pawR * st.pawLen, hs - 1, 5);
        g.pop();
    }

    /**
     * Four legs of a quadruped in root space.
     * @param f    body frame (hips / shoulders are moved with the body)
     * @param hipH standing hip height (ankle target height = pawR)
     */
    static void legs(G g, P p, Frame f, LegStyle front, LegStyle hind, float x, float zs, float zh, float hipH, float L, float frontRatio, float hindRatio) {
        float[] o = new float[3];
        for (int i = 0; i < 4; i++) {
            boolean fr = i < 2;
            int side = (i & 1) == 0 ? 1 : -1;
            LegStyle st = fr ? front : hind;
            float z0 = fr ? zs : zh;
            float hy = f.y(hipH, z0), hz = f.z(hipH, z0);
            foot(p, i, o);
            float tz = z0 + o[0] * L, ty = st.pawR * 0.85f + o[1] * L;
            // sit: hind feet tuck forward under the hips; lie: front paws stretch out, hind fold
            if (fr) {
                tz += p.lie * L * 0.75f + p.bow * L * 0.55f;
            } else {
                tz += p.sit * L * 0.45f + p.lie * L * 0.5f;
            }
            float ratio = fr ? frontRatio : hindRatio;
            float curl = fr ? o[2] * 45 : o[2] * 25;
            ikLeg(g, st, side * x, hy, hz, tz, ty, L * ratio, L * (1 - ratio), fr ? 1 : -1, curl);
        }
    }

    // ================================================================== bipeds / hoppers / flyers
    /** Two waddling feet: steps forward, lifts, turns out a little. */
    static void feet(G g, P p, String tex, float x, float z, float r, float stride) {
        for (int s = -1; s <= 1; s += 2) {
            float c = frac(p.t * WADDLE_HZ + (s > 0 ? 0 : 0.5f));
            float[] o = step(c, 0.55f, stride, 0.55f);
            float w = p.walkW;
            g.push();
            g.translate(s * x, 0.3f + o[1] * w * r * 1.4f, z + o[0] * w * r * 2.2f);
            g.rotY(s * 12);
            g.rotX(-o[2] * w * 25);
            PetGeo.blob2(g, tex, 0, 0, 0.35f, r, 0.34f, 0.16f, r * 1.4f, 10, 5);
            g.pop();
        }
    }

    /** Hop cycle for hoppers: out[0] = height factor (0..1), out[1] = squash (-1 squash .. +1 stretch), out[2] = airborne 0..1. */
    static float[] hop(P p) {
        float amt = smooth(0.02f, 0.35f, p.sp);
        float c = frac(p.t * HOP_HZ);
        float h = 0, sq, air = 0;
        if (c < 0.22f) sq = -sin(Math.PI * c / 0.22f) * 0.9f;               // crouch / take off
        else if (c < 0.8f) {                                                   // airborne
            float u = (c - 0.22f) / 0.58f;
            h = sin(Math.PI * u);
            sq = (1 - u * 2) * 0.8f;
            air = sin(Math.PI * u);
        } else sq = -sin(Math.PI * (c - 0.8f) / 0.2f);                       // landing squash
        return new float[]{h * amt, sq * amt, air * amt};
    }

    /** Wing stroke: out[0] = wing angle (-1 down .. 1 up), out[1] = body lift (rises on the down stroke). */
    static float[] flap(P p, float hz) {
        double ph = p.t * Math.PI * 2 * hz;
        float w = sin(ph);
        return new float[]{w, -w * 0.5f + 0.25f * sin(ph * 2)};
    }

    // ================================================================== growth accessories
    /** Collar (level 4+) with a golden tag (level 5), at the current origin, ring in the xz plane. */
    static void collar(G g, P p, float r, float thick) {
        if (p.lvl < 4) return;
        g.push();
        g.torus(Pets.col(0xD8324A), r, thick, 12, 3, 0, 0, 1, 1);
        g.translate(0, -thick * 0.6f, r + thick * 0.4f);
        blob(g, Pets.gl(p.lvl >= 5 ? 0xFFC83A : 0xC8CCD8), 0, -thick * 1.4f, 0, thick * 1.5f, thick * 1.7f, thick * 0.6f, 6, 3);
        g.pop();
    }

    /** Subtle golden sparkle ring on the ground (level 5). */
    static void sparkles(G g, P p, float radius) {
        if (p.lvl < 5) return;
        int n = 7;
        for (int k = 0; k < n; k++) {
            double an = p.t * 0.6 + k * Math.PI * 2 / n;
            float tw = 0.5f + 0.5f * sin(p.t * 3.1f + k * 1.9f);
            int al = (int) (70 + 150 * tw);
            PetGeo.sprite(g, "pet_star", cos(an) * radius, 0.35f + 0.25f * sin(p.t * 1.7f + k), sin(an) * radius, 0.55f + 0.45f * tw, (al << 24) | 0xFFD65A);
        }
    }

    /** Eye style variant: adults get smaller, more realistic eyes. */
    static String style(String base, float gr) {
        int st = gr < 0.34f ? 0 : gr < 0.7f ? 1 : 2;
        return st == 0 ? base : base + "." + st;
    }
}
