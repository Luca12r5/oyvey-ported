package dev.lego.cosmetic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.lego.cosmetic.Cos.Rarity;
import dev.lego.cosmetic.Cos.Slot;

import static dev.lego.cosmetic.PetGeo.blob;
import static dev.lego.cosmetic.PetGeo.cone;
import static dev.lego.cosmetic.PetGeo.cos;
import static dev.lego.cosmetic.PetGeo.sin;

/** Cute pets with emotions (registered into Cos, slot PET). Pet space: px, origin at the pet's feet, x right, y up, pet faces +z. */
public final class Pets {
    private Pets() { }

    public static final String[] EMOTIONS = {"happy", "love", "sleepy", "sad", "excited", "angry", "curious", "dizzy"};

    private static final Map<String, Float> HEIGHT = new ConcurrentHashMap<>();

    /** Height of the pet in px (for the emotion bubble), at medium growth. */
    public static float height(String id) { return height(id, 0.5f); }

    /** Height of the pet in px at the given growth (0 = baby .. 1 = adult). */
    public static float height(String id, float growth) {
        float gq = Math.round(Math.max(0, Math.min(1, growth)) * 20) / 20f;
        String key = id + "@" + gq;
        Float h = HEIGHT.get(key);
        if (h != null) return h;
        Cos.Item it = Cos.get(id);
        float v = 10;
        if (it != null && it.slot == Slot.PET) {
            final float[] max = {0};
            G g = new G((tex, p, uv, argb, glow, nx, ny, nz) -> {
                if (tex.equals("pet_star") || tex.equals("pet_heart")) return;   // sparkles / hearts do not count
                for (int i = 1; i < 12; i += 3) max[0] = Math.max(max[0], p[i]);
            });
            Cos.A a = new Cos.A();
            a.emotion = "happy";
            a.time = 0.35f;   // happy bob is near its rest there
            a.growth = gq;
            a.level = 1 + Math.round(gq * 4);
            try { it.model.render(g, a); v = Math.max(3, max[0]); } catch (RuntimeException ignored) { }
            HEIGHT.put(key, v);
        }
        return v;
    }

    static void registerAll() {
        Cos.add("pet_cat", "Kätzchen", Slot.PET, Rarity.COMMON, PetModels::cat);
        Cos.add("pet_shiba", "Shiba", Slot.PET, Rarity.RARE, PetModels::shiba);
        Cos.add("pet_bunny", "Häschen", Slot.PET, Rarity.COMMON, PetModels::bunny);
        Cos.add("pet_fox", "Füchschen", Slot.PET, Rarity.RARE, PetModels::fox);
        Cos.add("pet_redpanda", "Roter Panda", Slot.PET, Rarity.EPIC, PetModels::redPanda);
        Cos.add("pet_panda", "Pandabär", Slot.PET, Rarity.RARE, PetModels::panda);
        Cos.add("pet_bear", "Teddybär", Slot.PET, Rarity.COMMON, PetModels::bear);
        Cos.add("pet_hamster", "Hamster", Slot.PET, Rarity.COMMON, PetModels::hamster);
        Cos.add("pet_penguin", "Pinguin", Slot.PET, Rarity.RARE, grow(PetModels::penguin, 1.55f, 3.4f));
        Cos.add("pet_chick", "Küken", Slot.PET, Rarity.COMMON, grow(PetModels::chick, 1.5f, 3.6f));
        Cos.add("pet_owl", "Eule", Slot.PET, Rarity.RARE, grow(PetModels::owl, 1.6f, 3.4f));
        Cos.add("pet_frog", "Fröschchen", Slot.PET, Rarity.COMMON, grow(PetModels::frog, 1.55f, 3.8f));
        Cos.add("pet_axolotl", "Axolotl", Slot.PET, Rarity.EPIC, grow(PetModels::axolotl, 1.55f, 4.0f));
        Cos.add("pet_turtle", "Schildkröte", Slot.PET, Rarity.RARE, grow(PetModels::turtle, 1.6f, 4.2f));
        Cos.add("pet_bee", "Bienchen", Slot.PET, Rarity.RARE, grow(PetModels::bee, 1.45f, 3.4f));
        Cos.add("pet_bat", "Fledermaus", Slot.PET, Rarity.EPIC, grow(PetModels::bat, 1.5f, 3.6f));
        Cos.add("pet_slime", "Schleimi", Slot.PET, Rarity.COMMON, grow(PetModels::slime, 1.6f, 4.2f));
        Cos.add("pet_ghost", "Geistchen", Slot.PET, Rarity.RARE, grow(PetModels::ghostPet, 1.5f, 3.8f));
        Cos.add("pet_jelly", "Qualle", Slot.PET, Rarity.EPIC, grow(PetModels::jelly, 1.45f, 3.8f));
        Cos.add("pet_fish", "Blubberfisch", Slot.PET, Rarity.EPIC, grow(PetModels::fish, 1.4f, 4.4f));
        Cos.add("pet_mushroom", "Pilzchen", Slot.PET, Rarity.RARE, grow(PetModels::mushroom, 1.55f, 4.2f));
        Cos.add("pet_brick", "Stein-Freund", Slot.PET, Rarity.EPIC, grow(PetModels::brickBuddy, 1.5f, 4.4f));
        Cos.add("pet_cloud", "Wölkchen", Slot.PET, Rarity.EPIC, grow(PetModels::cloud, 1.5f, 4.8f));
        Cos.add("pet_robot", "Robo", Slot.PET, Rarity.EPIC, grow(PetModels::robot, 1.45f, 3.8f));
        Cos.add("pet_dragon", "Mini-Drache", Slot.PET, Rarity.LEGENDARY, PetModels::miniDragon);
        Cos.add("pet_unicorn", "Einhorn", Slot.PET, Rarity.LEGENDARY, PetModels::unicorn);
        // realistic pets (grow from a chibi baby into a proper animal)
        Cos.add("pet_wolf", "Wolf", Slot.PET, Rarity.RARE, PetReal::wolf);
        Cos.add("pet_retriever", "Golden Retriever", Slot.PET, Rarity.RARE, PetReal::retriever);
        Cos.add("pet_arcticfox", "Polarfuchs", Slot.PET, Rarity.RARE, PetReal::arcticFox);
        Cos.add("pet_horse", "Pferd", Slot.PET, Rarity.EPIC, PetReal::horse);
        Cos.add("pet_deer", "Hirsch", Slot.PET, Rarity.EPIC, PetReal::deer);
        Cos.add("pet_tiger", "Tiger", Slot.PET, Rarity.EPIC, PetReal::tiger);
        Cos.add("pet_eagle", "Adler", Slot.PET, Rarity.EPIC, PetReal::eagle);
        Cos.add("pet_lion", "Löwe", Slot.PET, Rarity.LEGENDARY, PetReal::lion);
        Cos.add("pet_drake", "Drache", Slot.PET, Rarity.LEGENDARY, PetReal::drake);
    }

    /**
     * Growth for the simple (non quadruped) pets: uniform scale from 0.92 (baby) to max (adult) plus the level 5 sparkle ring.
     * Their models add their own proportion changes (longer legs, bigger wings / ears / fins) via P.gr.
     */
    static Cos.Model grow(Cos.Model m, float max, float ring) {
        return (g, a) -> {
            float k = Math.max(0, Math.min(1, a.growth));
            float s = a.preview ? 1 : 0.92f + (max - 0.92f) * k;
            g.push();
            g.scale(s);
            try { m.render(g, a); } finally { g.pop(); }
            if (a.level >= 5 && !a.preview) {
                P p = new P();
                p.t = a.time;
                p.lvl = a.level;
                PetRig.sparkles(g, p, ring * s);
            }
        };
    }

    // ================================================================== pose (emotion + animation state)
    /** Per-frame pose derived from Cos.A. */
    static final class P {
        float t, sp, lookX, lookY;
        String emo;
        boolean closed;
        // root
        float y, rootX, rotZ, rotX, sx = 1, sy = 1;
        // head
        float headYaw, headPitch, headRoll;
        /** ears: 1 = up, 0 = drooping, >1 perked. */
        float ear = 1, earTwitch;
        float tail, tailLift, walk, lie, breathe;
        /** flyers: hover height factor, wing flap -1..1 */
        float hover = 1, flap, flapSpd;
        // ---- growth
        /** growth 0 (baby) .. 1 (adult), level 1..5. */
        float gr;
        int lvl = 3;
        boolean preview;
        // ---- gait (see PetRig.gait)
        float walkW, runW, idle, cw, cr, bob, roll, pitch, stretch = 1, nod, tailSway, waddle, waddleBob;
        float sit, bow, sniff, crouch;
        /** legacy step bob (added in root() for the simple pets). */
        float stepBob;
        boolean is(String e) { return emo.equals(e); }
    }

    static String emotion(String e) {
        if (e != null) for (String s : EMOTIONS) if (s.equals(e)) return s;
        return "happy";
    }

    static P pose(Cos.A a) {
        P p = new P();
        float t = p.t = a.time;
        float sp = p.sp = Math.max(0, Math.min(1, a.speed));
        p.emo = emotion(a.emotion);
        p.lookX = Math.max(-1, Math.min(1, a.lookX));
        p.lookY = Math.max(-1, Math.min(1, a.lookY));
        float ph = t * 11f;
        p.walk = sin(ph) * 38 * sp;
        p.breathe = sin(t * 2.2f);
        p.stepBob = Math.abs(sin(ph)) * 0.45f * sp;
        p.gr = Math.max(0, Math.min(1, a.growth));
        p.lvl = Math.max(1, Math.min(5, a.level));
        p.preview = a.preview;
        p.sy = 1 + p.breathe * 0.018f;
        p.sx = 1 - p.breathe * 0.008f;
        p.headYaw = p.lookX * 24;
        p.headPitch = -p.lookY * 14;
        p.earTwitch = (float) Math.pow(Math.max(0, Math.sin(t * 0.9)), 40) * 14;
        float tailAmp = 18, tailSpd = 7;
        p.flapSpd = 13;
        switch (p.emo) {
            case "happy":
                p.headRoll = sin(t * 1.7f) * 5;
                p.y += Math.abs(sin(t * 3.4f)) * 0.2f;
                tailAmp = 26; tailSpd = 10;
                break;
            case "love":
                p.rotZ = sin(t * 2.3f) * 7;
                p.headRoll = sin(t * 2.3f + 0.5f) * 10;
                tailAmp = 22; tailSpd = 5;
                break;
            case "sleepy": {
                p.lie = 1 - Math.min(1, sp * 2);
                p.breathe = sin(t * 1.2f);
                p.sy = 1 + p.breathe * 0.035f;
                p.sx = 1 - p.breathe * 0.012f;
                p.headPitch += 7 * p.lie;
                p.headRoll = 11 * p.lie;
                p.ear = 0.35f;
                tailAmp = 3; tailSpd = 1;
                p.tailLift = -25;
                p.hover = 0.45f;
                p.flapSpd = 5;
                break;
            }
            case "sad":
                p.headPitch += 14;
                p.ear = 0;
                tailAmp = 4; tailSpd = 2;
                p.tailLift = -35;
                p.y -= 0.08f;
                p.sy *= 0.97f;
                p.hover = 0.7f;
                p.flapSpd = 7;
                break;
            case "excited": {
                float h = Math.abs(sin(t * 6.8f));
                p.y += h * 2.0f;
                p.sy *= 1 + (h - 0.35f) * 0.16f;
                p.sx *= 1 - (h - 0.35f) * 0.08f;
                p.headRoll = sin(t * 6.8f) * 6;
                p.ear = 1.1f;
                tailAmp = 36; tailSpd = 20;
                p.hover = 1.1f;
                p.flapSpd = 22;
                break;
            }
            case "angry":
                p.rootX = sin(t * 40) * 0.2f;
                p.headPitch += 7;
                p.ear = 0.45f;
                tailAmp = 12; tailSpd = 24;
                p.sx *= 1.05f;
                p.sy *= 0.97f;
                p.flapSpd = 18;
                break;
            case "curious":
                p.headRoll = 18 + sin(t * 1.4f) * 4;
                p.headYaw += sin(t * 0.6f) * 12;
                p.headPitch -= 5;
                p.ear = 1.12f;
                tailAmp = 12; tailSpd = 3;
                p.tailLift = 10;
                break;
            case "dizzy":
                p.rotZ = sin(t * 3.1f) * 9;
                p.rotX = cos(t * 3.1f) * 6;
                p.headRoll = sin(t * 3.1f + 1.2f) * 14;
                p.headYaw += cos(t * 3.1f) * 10;
                p.ear = 0.6f;
                tailAmp = 8; tailSpd = 2;
                break;
            default: break;
        }
        p.tail = sin(t * tailSpd) * tailAmp;
        PetRig.gait(p);
        p.flap = sin(t * p.flapSpd);
        boolean doze = p.is("sleepy") && sp < 0.3f && ((t * 0.3f) % 1f + 1f) % 1f < 0.78f;
        p.closed = a.blink > 0.5f || doze;
        return p;
    }

    /** Root transform: hop / sway / shake / wobble / squash. */
    static void root(G g, P p) {
        g.translate(p.rootX, p.y + p.stepBob, 0);
        if (p.rotZ != 0) g.rotZ(p.rotZ);
        if (p.rotX != 0) g.rotX(p.rotX);
        g.scale(p.sx, p.sy, p.sx);
    }

    static void headTurn(G g, P p) {
        g.rotY(p.headYaw);
        g.rotX(p.headPitch);
        g.rotZ(p.headRoll);
    }

    // ================================================================== shared parts
    static String fur(String k) { return "pet_fur:" + k; }
    static String head(String k) { return "pet_head:" + k; }
    static String col(int rgb) { return "pet_c:" + String.format("%06X", rgb & 0xFFFFFF); }
    static String gl(int rgb) { return "pet_gl:" + String.format("%06X", rgb & 0xFFFFFF); }

    /** Face (mouth/brows/blush layer + eye layer that follows lookX/lookY) on the front of an ellipsoid. */
    static void face(G g, P p, String style, float cx, float cy, float cz, float rx, float ry, float rz, float span, float el0) {
        boolean og = g.glow;
        int oc = g.color;
        boolean led = style.equals("led");
        g.color(0xFFFFFFFF).glow(led);
        PetGeo.decal(g, "pet_face:" + style + ":" + p.emo, cx, cy, cz, rx, ry, rz, 0, el0, span, 0, 0, 1, 1, 1.035f);
        PetGeo.decal(g, eyes(style, p), cx, cy, cz, rx, ry, rz, p.lookX * 0.09f, el0 + p.lookY * 0.06f, span, 0, 0, 1, 1, 1.05f);
        g.color(oc).glow(og);
    }

    /** Face on a flat front (brick, robot screen). */
    static void faceFlat(G g, P p, String style, float cx, float cy, float z, float w, float h) {
        boolean og = g.glow;
        int oc = g.color;
        g.color(0xFFFFFFFF).glow(style.equals("led"));
        PetGeo.flat(g, "pet_face:" + style + ":" + p.emo, cx, cy, z, w, h);
        PetGeo.flat(g, eyes(style, p), cx + p.lookX * w * 0.05f, cy + p.lookY * h * 0.05f, z + 0.03f, w, h);
        g.color(oc).glow(og);
    }

    static String eyes(String style, P p) { return "pet_eyes:" + style + ":" + p.emo + ":" + (p.closed ? 1 : 0); }

    /** Nose bump on the front of a head ellipsoid. */
    static void nose(G g, int rgb, float rx, float ry, float rz, float el, float r) {
        blob(g, gl(rgb), 0, sin(el) * ry, cos(el) * rz * 0.99f, r * 1.25f, r * 0.85f, r * 0.8f, 6, 4);
    }

    /** Pointy ear with a pink inner cone (head space, base at (x,y,z)). */
    static void earPointy(G g, P p, String tex, String inner, float x, float y, float z, int side, float w, float h, float tilt) {
        earPointy(g, p, tex, inner, null, x, y, z, side, w, h, tilt);
    }

    static void earPointy(G g, P p, String tex, String inner, String tip, float x, float y, float z, int side, float w, float h, float tilt) {
        g.push();
        g.translate(x, y, z);
        float droop = Math.max(0, 1 - p.ear);
        float perk = Math.max(0, p.ear - 1) * 60;
        g.rotZ(-side * (tilt + droop * 58 - perk + p.earTwitch * (side > 0 ? 1 : 0)));
        g.rotX(-droop * 22);
        g.push();
        g.scale(1, 1, 0.42f);
        cone(g, tex, w, h, 8, false);
        if (tip != null) {
            g.translate(0, h * 0.6f, 0);
            cone(g, tip, w * 0.43f, h * 0.41f, 8, false);
        }
        g.pop();
        if (inner != null) {
            g.push();
            g.translate(0, h * 0.08f, w * 0.13f);
            g.scale(1, 1, 0.42f);
            cone(g, inner, w * 0.62f, h * 0.76f, 7, false);
            g.pop();
        }
        g.pop();
    }

    /** Round ear (bear / panda / hamster). */
    static void earRound(G g, P p, String tex, String inner, float x, float y, float z, int side, float r) {
        g.push();
        g.translate(x, y, z);
        float droop = Math.max(0, 1 - p.ear);
        g.rotZ(-side * (15 + droop * 35 + p.earTwitch * (side > 0 ? 1 : 0)));
        g.rotX(-droop * 15);
        blob(g, tex, 0, r * 0.55f, 0, r, r, r * 0.55f, 7, 4);
        if (inner != null) blob(g, inner, 0, r * 0.5f, r * 0.26f, r * 0.62f, r * 0.62f, r * 0.33f, 5, 2);
        g.pop();
    }

    /** Tail made of blobs (base at the current origin, grows along -z). */
    static void tail(G g, P p, String[] tex, float[] r, float segLen, float curl, float lift, float wag) {
        g.push();
        g.rotY(p.tail * wag + p.tailSway);
        g.rotX(lift + p.tailLift);
        for (int i = 0; i < r.length; i++) {
            g.rotX(curl);
            g.rotY(p.tail * 0.35f * wag);
            blob(g, tex[Math.min(i, tex.length - 1)], 0, 0, -segLen * 0.5f, r[i], r[i], segLen * 0.62f + r[i] * 0.3f, 7, 4);
            g.translate(0, 0, -segLen * 0.82f);
        }
        g.pop();
    }

    /** One stubby leg: pivot at the top (y = h), swings around x. */
    static void leg(G g, String tex, float x, float z, float r, float h, float swing, float drop) {
        g.push();
        g.translate(x, h - drop, z);
        g.rotX(swing);
        blob(g, tex, 0, -h * 0.45f, 0, r, h * 0.56f, r * 1.05f, 6, 4);
        g.pop();
    }

    /** Four legs with walk cycle and lying down (sleepy). */
    static void legs4(G g, P p, String tex, float x, float zf, float zb, float r, float h) {
        float drop = p.lie * h * 0.72f;
        for (int s = -1; s <= 1; s += 2) {
            leg(g, tex, s * x, zf, r, h, s * p.walk - p.lie * 72, drop);
            leg(g, tex, s * x, zb, r, h, -s * p.walk + p.lie * 72, drop);
        }
    }

    /** Floating hearts (love) / orbiting stars (dizzy) above the head. */
    static void fx(G g, P p, float top) {
        if (p.is("dizzy")) {
            for (int k = 0; k < 3; k++) {
                double an = p.t * 3.2 + k * Math.PI * 2 / 3;
                PetGeo.sprite(g, "pet_star", cos(an) * 2.8f, top + 0.4f + sin(an * 2) * 0.2f, sin(an) * 2.8f, 1.5f, 0xFFFFFFFF);
            }
        } else if (p.is("love")) {
            for (int k = 0; k < 2; k++) {
                float ph = ((p.t * 0.55f + k * 0.5f) % 1f + 1f) % 1f;
                int al = (int) (255 * Math.sin(ph * Math.PI));
                PetGeo.sprite(g, "pet_heart", (k == 0 ? 1.6f : -1.8f) + sin(ph * 6 + k) * 0.5f, top + ph * 3.2f, 0.5f, 1.2f + 0.5f * ph, (al << 24) | 0xFFFFFF);
            }
        }
    }

    // ================================================================== standard four-legged chibi
    interface Part { void draw(G g, P p); }

    /** Config of a four-legged chibi. */
    static final class Q4 {
        String body, head, leg, style = "std";
        float bodyY = 2.55f, bodyRx = 2.2f, bodyRy = 1.95f, bodyRz = 2.45f, bodyZ = -0.35f;
        float headY = 6.1f, headZ = 0.95f, headRx = 3.35f, headRy = 2.95f, headRz = 2.8f;
        float legR = 0.72f, legH = 1.15f, legX = 1.2f, legZf = 1.05f, legZb = -1.75f;
        float span = 1.0f, faceEl = 0f;
        int nose = -1;
        float noseR = 0.3f, noseEl = -0.14f;
        Part ears, tail, headExtra, bodyExtra;
    }

    /**
     * Chibi quadruped with growth: the baby is the classic chibi, the adult gets ~1.7x bigger with longer legs and body,
     * a smaller head on a neck, fur tufts, a collar (level 4+) and a sparkle ring (level 5). Legs use the gait rig.
     */
    static void quad4(G g, P p, Q4 q) {
        float k = p.gr;
        float S = p.preview ? 0.9f : 0.88f + 0.46f * k;
        float legF = 1 + 1.25f * k, lenF = 1 + 0.4f * k, headF = 1 - 0.3f * k;
        float legH = q.legH * legF, lift = legH - q.legH;
        float bodyRz = q.bodyRz * lenF, bodyY = q.bodyY + lift, bodyZ = q.bodyZ * lenF;
        float zf = q.legZf * lenF, zb = q.legZb * lenF;
        float headY = q.headY + lift - q.headRy * (1 - headF) * 0.9f + 0.35f * k, headZ = q.headZ + bodyRz * 0.3f * k;
        float belly = bodyY - q.bodyRy;
        p.stepBob = 0;
        g.push();
        g.scale(S);
        root(g, p);
        PetRig.Frame f = PetRig.frame(p, legH, zf, zb, legH, belly);
        // legs (two bones, IK, trot / gallop)
        float lr = q.legR * (1 - 0.12f * k);
        PetRig.LegStyle ls = new PetRig.LegStyle(k > 0.3f ? q.body : q.leg, q.leg, q.leg, lr, lr * 0.88f, lr * 0.95f);
        ls.pawLen = 1.15f;
        PetRig.legs(g, p, f, ls, ls, q.legX, zf, zb, legH, legH, 0.52f, 0.5f);
        g.push();
        f.apply(g);
        g.rotZ(p.roll);
        // body
        g.push();
        g.translate(0, bodyY, bodyZ);
        g.scale(1, 1, p.stretch);
        blob(g, q.body, 0, 0, 0, q.bodyRx, q.bodyRy, bodyRz, 10, 5);
        if (q.bodyExtra != null) q.bodyExtra.draw(g, p);
        if (k > 0.3f) {
            // chest fluff (juvenile / adult)
            float cf = Math.min(1, (k - 0.3f) / 0.5f);
            blob(g, q.body, 0, q.bodyRy * 0.25f, bodyRz * 0.72f, q.bodyRx * 0.62f * cf, q.bodyRy * 0.62f * cf, bodyRz * 0.32f * cf, 7, 4);
        }
        if (q.tail != null) {
            g.push();
            g.translate(0, -q.bodyRy * 0.1f + 0.2f * k, -bodyRz * 0.88f);
            g.scale(1 + 0.35f * k);
            q.tail.draw(g, p);
            g.pop();
        }
        g.pop();
        // neck (grows in)
        if (k > 0.15f) {
            float ny = (bodyY + headY) * 0.5f, nz = (bodyZ + bodyRz * 0.55f + headZ) * 0.5f;
            blob(g, q.body, 0, ny, nz, q.bodyRx * 0.6f, (headY - bodyY) * 0.42f, q.bodyRx * 0.62f, 8, 4);
        }
        // collar on the neck
        g.push();
        g.translate(0, bodyY + q.bodyRy * 0.62f + 0.2f * k, bodyZ + bodyRz * 0.5f + 0.2f * k);
        g.rotX(28);
        PetRig.collar(g, p, q.bodyRx * (0.66f + 0.05f * k), 0.24f);
        g.pop();
        // head
        g.push();
        g.translate(0, headY - p.lie * 0.35f - p.bob * legH * 0.5f, headZ);
        headTurn(g, p);
        g.rotX(p.nod - p.sit * 6);
        g.scale(headF);
        String style = PetRig.style(q.style, k);
        blob(g, q.head, 0, 0, 0, q.headRx, q.headRy, q.headRz, 14, 9);
        face(g, p, style, 0, 0, 0, q.headRx, q.headRy, q.headRz, q.span, q.faceEl);
        if (q.nose >= 0) nose(g, q.nose, q.headRx, q.headRy, q.headRz, q.noseEl, q.noseR * (1 + 0.4f * k));
        if (k > 0.4f) {
            // cheek tufts
            float tf = Math.min(1, (k - 0.4f) / 0.4f);
            for (int s = -1; s <= 1; s += 2) {
                g.push();
                g.translate(s * q.headRx * 0.9f, -q.headRy * 0.35f, -0.2f);
                g.rotZ(-s * 115);
                g.scale(1, 1, 0.5f);
                cone(g, q.head, 0.7f * tf, 1.3f * tf, 5, false);
                g.pop();
            }
        }
        if (q.ears != null) {
            g.push();
            g.scale(1 + 0.12f * k);
            q.ears.draw(g, p);
            g.pop();
        }
        if (q.headExtra != null) q.headExtra.draw(g, p);
        g.pop();
        fx(g, p, headY + q.headRy * headF + 1.2f);
        g.pop();
        PetRig.sparkles(g, p, Math.max(q.bodyRx, bodyRz) + 1.2f);
        g.pop();
    }
}
