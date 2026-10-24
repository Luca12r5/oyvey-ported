package dev.lego.games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.lego.games.SolarTools.Kind;
import dev.lego.games.SolarTools.Tool;
import dev.lego.ui.Gx;
import dev.lego.ui.Sound;

/**
 * Effect engines of Solar Smash. Coordinates are "planet space": origin = planet centre, 1 = planet radius, y down.
 * Everything only draws cached sprites (tinted / scaled) - no painting per frame.
 */
final class SolarFx {
    final SolarPlanet p;
    final Random rnd = new Random();
    float time;
    /** multiplier from the mouse wheel */
    float sizeMul = 1;

    SolarFx(SolarPlanet p) { this.p = p; }

    // ================================================================== objects
    static final class Part { float x, y, vx, vy, life, max, size; int color; int sprite; boolean glow; }
    static final class Shot { Tool t; float x, y, sx, sy, tx, ty, k, dur, delay; double lon, lat; boolean hit; }
    static final class Ship { Tool t; float ang, rad, bob, life, fireT, beamT; float bx, by; double blon, blat; }
    static final class Area { Tool t; double lon, lat; float age; float tick; }
    static final class Glob { Tool t; float age, tick; }
    static final class Hole { Tool t; float x, y, age; }
    static final class Ring { float x, y, age, max, size; int color; }

    final List<Part> parts = new ArrayList<>();
    final List<Shot> shots = new ArrayList<>();
    final List<Ship> ships = new ArrayList<>();
    final List<Area> areas = new ArrayList<>();
    final List<Glob> globs = new ArrayList<>();
    final List<Hole> holes = new ArrayList<>();
    final List<Ring> rings = new ArrayList<>();
    /** screen tint from global effects */
    int tint;
    float tintA, flash;

    // beam state (held mouse)
    Tool beam;
    float beamX, beamY, beamTick, beamSnd;
    boolean beamOn;

    void clear() { parts.clear(); shots.clear(); ships.clear(); areas.clear(); globs.clear(); holes.clear(); rings.clear(); beamOn = false; tintA = 0; }

    // ================================================================== input
    /** Target on the visible disc (planet space) -> {lon, lat} or null. */
    double[] surface(float x, float y) {
        float s = p.scale;
        if (s <= 0.02f || p.destroyed) return null;
        double ux = x / s, uy = y / s;
        if (ux * ux + uy * uy > 1) return null;
        return p.unproject(ux, uy);
    }

    void fire(Tool t, float x, float y) {
        if (p.destroyed && t.kind != Kind.HOLE) return;
        switch (t.kind) {
            case BEAM: beam = t; beamOn = true; beamX = x; beamY = y; beamTick = 0; break;
            case SHOT: shot(t, x, y, 0); Sound.play(t.sound.equals("minecraft:entity.generic.explode") ? "minecraft:entity.firework_rocket.launch" : "minecraft:entity.firework_rocket.launch", 0.7f, 0.4f); break;
            case RAIN:
                for (int i = 0; i < t.n; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2, r = Math.sqrt(rnd.nextDouble()) * 0.45 * (t.n > 30 ? 1.6 : 1);
                    shot(t, (float) (x + Math.cos(a) * r), (float) (y + Math.sin(a) * r), t.dur * i / Math.max(1, t.n) + rnd.nextFloat() * 0.1f);
                }
                Sound.play("minecraft:entity.firework_rocket.launch", 0.6f, 0.4f);
                break;
            case SWARM:
                for (int i = 0; i < t.n; i++) {
                    Ship s = new Ship();
                    s.t = t;
                    s.ang = (float) (i * Math.PI * 2 / t.n + rnd.nextFloat() * 0.4f);
                    s.rad = (t.sprite == 1 ? 1.55f : 1.2f + rnd.nextFloat() * 0.45f) + 1.8f;   // fly in from far away
                    s.bob = rnd.nextFloat() * 6;
                    s.life = t.dur;
                    s.fireT = 0.8f + rnd.nextFloat();
                    ships.add(s);
                }
                Sound.play(t.sound, 0.8f, 0.6f);
                break;
            case AREA: {
                double[] ll = surface(x, y);
                if (ll == null) return;
                Area a = new Area(); a.t = t; a.lon = ll[0]; a.lat = ll[1];
                areas.add(a);
                Sound.play(t.sound, 0.7f, 0.7f);
                break;
            }
            case GLOBAL: {
                Glob g = new Glob(); g.t = t;
                globs.add(g);
                tint = t.color;
                Sound.play(t.sound, 0.6f, 0.8f);
                break;
            }
            case HOLE: {
                Hole h = new Hole(); h.t = t;
                float d = (float) Math.hypot(x, y);
                if (d < 1.4f) { float k = 1.6f / Math.max(0.01f, d); x *= k; y *= k; if (d < 0.01f) { x = 1.6f; y = 0; } }
                h.x = x; h.y = y;
                holes.add(h);
                Sound.play(t.sound, 0.5f, 0.9f);
                break;
            }
            case SPLIT: {
                double[] ll = surface(x, y);
                if (ll == null) return;
                int n = Math.round(3 * t.width);
                for (int i = 0; i < n; i++)
                    p.crack(ll[0] + (rnd.nextDouble() - 0.5) * 0.4, ll[1] + (rnd.nextDouble() - 0.5) * 0.3, 260 * t.width * sizeMul, 3 + 2 * t.width, t.heat, t.kill * 0.3f, rnd);
                p.core += 0.06f * t.width;
                p.shake = Math.min(1, p.shake + 0.5f * t.width);
                flash(x, y, 0.4f * t.width, t.color);
                Sound.play(t.sound, 0.6f, 0.9f);
                if (t.width >= 5) p.core += 0.5f;   // Planetenspalter
                break;
            }
            default:
        }
    }

    void release() { beamOn = false; }

    private void shot(Tool t, float x, float y, float delay) {
        Shot s = new Shot();
        s.t = t;
        double a = -2.3 + rnd.nextDouble() * 0.6;          // come in from the upper left (where the sun is not)
        if (t.cat == 1 || t.cat == 7) a = -1.9 - rnd.nextDouble() * 1.2;
        float from = 3.2f;
        s.sx = (float) (x + Math.cos(a) * from); s.sy = (float) (y + Math.sin(a) * from);
        s.tx = x; s.ty = y; s.x = s.sx; s.y = s.sy;
        s.dur = Math.max(0.15f, 1.2f / t.speed);
        s.delay = delay;
        shots.add(s);
    }

    // ================================================================== update
    void update(float dt) {
        time += dt;
        flash = Math.max(0, flash - dt * 2.5f);
        if (globs.isEmpty()) tintA = Math.max(0, tintA - dt * 0.6f);
        // beam
        if (beamOn && beam != null && !p.destroyed) {
            beamTick -= dt;
            beamSnd -= dt;
            if (beamSnd <= 0) { Sound.play(beam.sound, 1.4f + rnd.nextFloat() * 0.2f, 0.25f); beamSnd = 0.35f; }
            if (beamTick <= 0) {
                beamTick = 0.05f / Math.max(0.5f, beam.speed);
                for (int i = 0; i < beam.n; i++) {
                    float off = beam.n == 1 ? 0 : (i - (beam.n - 1) / 2f) * 0.12f;
                    double[] ll = surface(beamX + off, beamY);
                    if (ll != null) {
                        float r = beam.r * sizeMul * (0.85f + rnd.nextFloat() * 0.3f);
                        int col = beam.style == SolarPlanet.S_PAINT ? hue(time) : beam.color;
                        p.paint(ll[0], ll[1], r, beam.style, beam.depth * 0.35f, beam.heat, beam.kill * 0.2f, 1.6f, col);
                        sparks(beamX + off, beamY, 2, col, 0.6f);
                    }
                }
            }
        }
        // shots
        for (int i = shots.size() - 1; i >= 0; i--) {
            Shot s = shots.get(i);
            if (s.delay > 0) { s.delay -= dt; continue; }
            s.k += dt / s.dur;
            float k = Math.min(1, s.k);
            s.x = s.sx + (s.tx - s.sx) * k; s.y = s.sy + (s.ty - s.sy) * k;
            if (rnd.nextFloat() < 0.8f) trail(s);
            float d2 = s.x * s.x + s.y * s.y, rr = p.scale * p.scale;
            if (k >= 1 || (d2 < rr * 0.98f && k > 0.5f)) {
                impact(s.t, s.x, s.y);
                shots.remove(i);
            }
        }
        // ships
        for (int i = ships.size() - 1; i >= 0; i--) {
            Ship s = ships.get(i);
            s.life -= dt;
            float target = s.t.sprite == 1 ? 1.55f : 1.25f + (i % 5) * 0.08f;
            if (s.life > 0) s.rad += (target - s.rad) * Math.min(1, dt * 1.4f);
            else s.rad += dt * 2.5f;
            s.ang += dt * (s.t.sprite == 1 ? 0.12f : 0.35f + (i % 3) * 0.1f) * (i % 2 == 0 ? 1 : -1);
            s.bob += dt;
            if (s.rad > 4) { ships.remove(i); continue; }
            s.fireT -= dt * Math.max(0.6f, s.t.speed);
            if (s.beamT > 0) {
                s.beamT -= dt;
                if (!p.destroyed) {
                    p.paint(s.blon, s.blat, s.t.r * sizeMul * (s.t.sprite == 1 ? 1.4f : 1), s.t.style, s.t.depth * 0.3f, s.t.heat, s.t.kill * 0.25f, 1.5f, s.t.color);
                    double[] pr = p.project(s.blon, s.blat);
                    if (pr[2] <= 0) s.beamT = 0;
                    else { s.bx = (float) pr[0] * p.scale; s.by = (float) pr[1] * p.scale; if (rnd.nextFloat() < 0.5f) sparks(s.bx, s.by, 1, s.t.color, 0.4f); }
                }
            } else if (s.fireT <= 0 && s.life > 0.5f && Math.abs(s.rad - target) < 0.2f && !p.destroyed) {
                // pick a visible spot under the ship
                float sx = (float) Math.cos(s.ang) * 0.75f + (rnd.nextFloat() - 0.5f) * 0.4f, sy = (float) Math.sin(s.ang) * 0.75f + (rnd.nextFloat() - 0.5f) * 0.4f;
                double[] ll = surface(sx * p.scale, sy * p.scale);
                if (ll != null) { s.blon = ll[0]; s.blat = ll[1]; s.beamT = s.t.sprite == 1 ? 2.2f : 0.45f; s.bx = sx; s.by = sy; }
                s.fireT = s.t.sprite == 1 ? 3f : 1.1f + rnd.nextFloat();
            }
        }
        // areas (spread over time)
        for (int i = areas.size() - 1; i >= 0; i--) {
            Area a = areas.get(i);
            a.age += dt;
            a.tick -= dt;
            if (a.tick <= 0 && !p.destroyed) {
                a.tick = 0.12f;
                float f = Math.min(1, a.age / a.t.dur);
                double sp = a.t.r / 2048.0 * Math.PI * 2 * sizeMul;
                for (int k = 0; k < 3; k++) {
                    double ang = rnd.nextDouble() * Math.PI * 2, d = Math.sqrt(rnd.nextDouble()) * sp * f;
                    p.paint(a.lon + Math.cos(ang) * d / Math.max(0.2, Math.cos(a.lat)), a.lat + Math.sin(ang) * d, a.t.r * 0.35f * sizeMul, a.t.style, a.t.depth * 0.4f, a.t.heat, a.t.kill * 0.3f, 1.4f, a.t.color);
                }
                double[] pr = p.project(a.lon, a.lat);
                if (pr[2] > 0) sparks((float) pr[0] * p.scale, (float) pr[1] * p.scale, 3, a.t.color, 0.8f);
            }
            if (a.age > a.t.dur) areas.remove(i);
        }
        // globals
        for (int i = globs.size() - 1; i >= 0; i--) {
            Glob g = globs.get(i);
            g.age += dt;
            g.tick -= dt;
            tintA = Math.min(0.35f, tintA + dt * 0.4f);
            if (g.tick <= 0 && !p.destroyed) {
                g.tick = 0.2f;
                float share = g.t.kill * 0.2f / Math.max(1, g.t.dur * 5) * 5;
                p.killGlobal(share * 0.2f);
                int st = g.t.style;
                if (st == SolarPlanet.S_FIRE) p.tempOff += 4;
                if (st == SolarPlanet.S_ICE) p.tempOff -= 5;
                if (st == SolarPlanet.S_EMP) p.lightsOff = Math.min(1, p.lightsOff + 0.25f);
                if (st == SolarPlanet.S_FIRE && g.t.kill > 0.5f) p.atmo = Math.max(0, p.atmo - 0.03f);
                if (st == SolarPlanet.S_ICE || st == SolarPlanet.S_FLOOD || st == SolarPlanet.S_FIRE) {
                    for (int k = 0; k < 4; k++) {
                        double lon = rnd.nextDouble() * Math.PI * 2, lat = (rnd.nextDouble() - 0.5) * (st == SolarPlanet.S_ICE ? 2.6 : 1.8);
                        p.paint(lon, lat, 40 + rnd.nextFloat() * 40, st, 3, st == SolarPlanet.S_FIRE ? 90 : 0, 0.1f, 1.2f, g.t.color);
                    }
                }
            }
            if (g.age > g.t.dur) globs.remove(i);
        }
        // holes
        for (int i = holes.size() - 1; i >= 0; i--) {
            Hole h = holes.get(i);
            h.age += dt;
            boolean white = h.t.color == 0xFFFFFFFF;
            float pull = h.t.width * dt * (white ? 0 : 0.07f);
            if (!p.destroyed && h.age < h.t.dur) {
                p.scale = Math.max(0, p.scale - pull * (0.4f + Math.min(1, h.age / 3)));
                p.killGlobal(white ? dt * 0.02 : dt * 0.06 * h.t.width);
                p.shake = Math.min(1, p.shake + dt * 0.3f);
                if (white) p.tempOff += dt * 8;
                // matter spirals into the hole
                if (rnd.nextFloat() < 0.8f) {
                    Part q = part();
                    double a = rnd.nextDouble() * Math.PI * 2;
                    q.x = (float) Math.cos(a) * p.scale; q.y = (float) Math.sin(a) * p.scale;
                    float dx = h.x - q.x, dy = h.y - q.y, dl = (float) Math.hypot(dx, dy);
                    q.vx = dx / dl * 0.9f - dy / dl * 0.4f; q.vy = dy / dl * 0.9f + dx / dl * 0.4f;
                    if (white) { q.vx = -q.vx; q.vy = -q.vy; }
                    q.max = q.life = 1.4f; q.size = 0.02f + rnd.nextFloat() * 0.03f; q.color = white ? 0xFFFFF4D0 : p.avgColor(); q.sprite = 1;
                }
                if (p.scale < 0.06f) explode();
            }
            if (h.age > h.t.dur + 2) holes.remove(i);
        }
        // particles
        for (int i = parts.size() - 1; i >= 0; i--) {
            Part q = parts.get(i);
            q.life -= dt;
            if (q.life <= 0) { parts.remove(i); continue; }
            q.x += q.vx * dt; q.y += q.vy * dt;
            q.vx *= 1 - dt * 0.6f; q.vy *= 1 - dt * 0.6f;
        }
        for (int i = rings.size() - 1; i >= 0; i--) { Ring r = rings.get(i); r.age += dt; if (r.age > r.max) rings.remove(i); }
        // destruction check
        if (!p.destroyed && p.hi != null && p.integrity() <= 0.01f) explode();
    }

    private void impact(Tool t, float x, float y) {
        double[] ll = surface(x, y);
        float big = Math.min(3f, t.r / 40f) * sizeMul;
        if (ll != null) {
            p.paint(ll[0], ll[1], t.r * sizeMul, t.style, t.depth, t.heat, t.kill, t.scorch, t.color);
            p.shake = Math.min(1, p.shake + 0.12f + big * 0.25f);
            p.hitFlash = Math.min(1, p.hitFlash + 0.3f * big);
        } else if (p.ringed) p.damageRing(Math.atan2(y, x), 0.4f * big, 2 + big);
        flash(x, y, 0.08f + 0.12f * big, t.style == SolarPlanet.S_ICE ? 0xFFDFF4FF : t.cat == 6 ? t.color : 0xFFFFD08A);
        sparks(x, y, 8 + Math.round(big * 10), t.style == SolarPlanet.S_ICE ? 0xFFE8F6FF : p.avgColor(), 0.6f + big * 0.4f);
        Ring r = new Ring(); r.x = x; r.y = y; r.max = 0.6f + big * 0.2f; r.size = 0.12f + big * 0.25f; r.color = t.color; rings.add(r);
        Sound.play(t.sound, Math.max(0.5f, 1.2f - big * 0.25f), Math.min(1f, 0.35f + big * 0.25f));
    }

    private void flash(float x, float y, float size, int color) {
        Part q = part();
        q.x = x; q.y = y; q.max = q.life = 0.45f; q.size = size; q.color = color; q.sprite = 0; q.glow = true;
    }

    private void sparks(float x, float y, int n, int color, float speed) {
        for (int i = 0; i < n; i++) {
            Part q = part();
            double a = rnd.nextDouble() * Math.PI * 2, v = (0.2 + rnd.nextDouble()) * speed;
            q.x = x; q.y = y; q.vx = (float) (Math.cos(a) * v); q.vy = (float) (Math.sin(a) * v);
            q.max = q.life = 0.4f + rnd.nextFloat() * 0.6f; q.size = 0.008f + rnd.nextFloat() * 0.018f; q.color = color; q.sprite = rnd.nextFloat() < 0.5f ? 1 : 2;
            q.glow = q.sprite == 2;
        }
    }

    private void trail(Shot s) {
        Part q = part();
        q.x = s.x + (rnd.nextFloat() - 0.5f) * 0.02f; q.y = s.y + (rnd.nextFloat() - 0.5f) * 0.02f;
        q.max = q.life = 0.35f + rnd.nextFloat() * 0.3f;
        q.size = 0.02f + Math.min(0.08f, s.t.r / 1500f);
        q.color = s.t.style == SolarPlanet.S_ICE ? 0xFFBFE8FF : s.t.cat == 6 ? 0xFFFFFFFF : 0xFFFF9A4A;
        q.sprite = 2; q.glow = true;
    }

    private Part part() {
        if (parts.size() > 1400) parts.remove(0);
        Part q = new Part();
        parts.add(q);
        return q;
    }

    /** The planet blows up: fragments, shock wave, flash. */
    void explode() {
        if (p.destroyed) return;
        p.destroyed = true;
        p.destroyedT = 0;
        p.gAlive = 0;
        int col = p.avgColor();
        for (int i = 0; i < 260; i++) {
            Part q = part();
            double a = rnd.nextDouble() * Math.PI * 2, r = Math.sqrt(rnd.nextDouble()) * p.scale, v = 0.3 + rnd.nextDouble() * 1.6;
            q.x = (float) (Math.cos(a) * r); q.y = (float) (Math.sin(a) * r);
            q.vx = (float) (Math.cos(a) * v); q.vy = (float) (Math.sin(a) * v);
            q.max = q.life = 3 + rnd.nextFloat() * 4;
            q.size = 0.02f + rnd.nextFloat() * rnd.nextFloat() * 0.18f;
            q.color = rnd.nextFloat() < 0.25f ? 0xFFFF8A2A : col;
            q.sprite = rnd.nextFloat() < 0.25f ? 2 : 1;
            q.glow = q.sprite == 2;
        }
        for (int k = 0; k < 3; k++) { Ring r = new Ring(); r.max = 2.5f + k; r.size = 3 + k * 1.5f; r.color = k == 0 ? 0xFFFFE0A0 : 0xFFFF8A4A; rings.add(r); }
        flash(0, 0, 3, 0xFFFFF0D0);
        flash = 1;
        Sound.play("minecraft:entity.generic.explode", 0.5f, 1f);
        Sound.play("minecraft:entity.warden.sonic_boom", 0.6f, 0.8f);
        ships.clear(); areas.clear(); globs.clear(); beamOn = false;
    }

    // ================================================================== drawing
    static int hue(float t) { return 0xFF000000 | (Color.HSBtoRGB(t * 0.4f % 1f, 0.8f, 1f) & 0xFFFFFF); }

    private static Gx.Img glowImg() { return Gx.get("solar:fx:flare", 0, () -> SolarTex.flare(128)); }
    private static Gx.Img softImg() { return Gx.get("solar:fx:soft", 0, () -> SolarTex.softRing(256)); }
    private static Gx.Img rockImg(int i) { final int k = i & 3; return Gx.get("solar:fx:rock" + k, 0, () -> SolarTex.rock(64, 11 + k, 0xFFFFFFFF)); }
    private static Gx.Img haloImg() { return Gx.get("solar:fx:halo", 0, () -> SolarTex.halo(256)); }
    private static Gx.Img swirlImg() { return Gx.get("solar:fx:swirl", 0, () -> SolarTex.swirl(256, 3)); }
    private static Gx.Img accImg() { return Gx.get("solar:fx:acc", 0, () -> SolarTex.accretion(512, 192)); }

    static Gx.Img shipImg(int kind) {
        final int k = kind;
        if (k == 5) return Gx.painted("solar:fx:sat", 96, 96, g -> SolarTex.satellite(g, 96, 0xFFFF6A3A));
        if (k == 6) return Gx.painted("solar:fx:sleigh", 128, 96, SolarFx::paintSleigh);
        return Gx.painted("solar:fx:ufo" + k, 160, 160, g -> SolarTex.ufo(g, 160, k, 0xFF6CFF9C));
    }

    static Gx.Img projImg(Tool t) {
        if (t.sprite >= 0 && t.cat == 6) { final int k = t.sprite; return Gx.painted("solar:fx:fun" + k, 128, 128, g -> SolarTex.fun(g, 128, k, t.color)); }
        if (t.sprite == -2) return Gx.painted("solar:fx:walker", 128, 128, g -> SolarTex.walker(g, 128, 0xFF8AFF6C));
        if (t.sprite == -3) return Gx.painted("solar:fx:gift", 96, 96, SolarFx::paintGift);
        if (t.sprite == -4) return Gx.painted("solar:fx:snowman", 96, 96, SolarFx::paintSnowman);
        return null;
    }

    void drawBack(float cx, float cy, float R) {
        // black holes behind the planet (accretion disc)
        for (Hole h : holes) {
            float grow = Math.min(1, h.age / 1.2f) * (h.age > h.t.dur ? Math.max(0, 1 - (h.age - h.t.dur) / 2) : 1);
            float s = R * 0.9f * h.t.width * grow;
            boolean white = h.t.color == 0xFFFFFFFF;
            float hx = cx + h.x * R, hy = cy + h.y * R;
            Gx.Img acc = accImg();
            if (acc != null) Gx.image(acc, Math.round(hx - s * 2), Math.round(hy - s * 0.75f), Math.round(s * 4), Math.round(s * 1.5f), white ? 0xFFFFFFFF : 0xFFFFB070);
            Gx.Img sw = swirlImg();
            if (sw != null) Gx.image(sw, Math.round(hx - s * 1.3f), Math.round(hy - s * 1.3f), Math.round(s * 2.6f), Math.round(s * 2.6f), white ? 0xCCFFFFFF : 0x99B070FF);
            Gx.glow(Math.round(hx), Math.round(hy), Math.round(s * 1.6f), white ? 0x88FFFFFF : 0x66FF8A3A);
            if (!white) Gx.circle(Math.round(hx), Math.round(hy), Math.max(2, Math.round(s * 0.42f)), 0xFF000000);
            else Gx.circle(Math.round(hx), Math.round(hy), Math.max(2, Math.round(s * 0.3f)), 0xFFFFFFFF);
        }
    }

    void drawFront(float cx, float cy, float R) {
        Gx.Img glow = glowImg(), soft = softImg();
        // rings (shock waves)
        for (Ring r : rings) {
            float k = r.age / r.max, s = R * r.size * (0.2f + k);
            if (soft != null) Gx.image(soft, Math.round(cx + r.x * R - s), Math.round(cy + r.y * R - s), Math.round(s * 2), Math.round(s * 2), Gx.withAlpha(r.color, (1 - k) * 0.9f));
        }
        // shots
        for (Shot s : shots) {
            if (s.delay > 0) continue;
            float x = cx + s.x * R, y = cy + s.y * R;
            float sz = R * Math.max(0.035f, Math.min(0.5f, s.t.r / 700f)) * sizeMul;
            Gx.Img pi = projImg(s.t);
            if (pi != null) { int d = Math.round(sz * 2.2f); Gx.image(pi, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d, 0xFFFFFFFF); }
            else {
                if (glow != null) { int d = Math.round(sz * 4); Gx.image(glow, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d, Gx.withAlpha(s.t.cat == 0 || s.t.cat == 5 ? s.t.color : 0xFFFF9A4A, 0.8f)); }
                boolean energy = s.t.cat == 4 || s.t.cat == 5 || s.t.style == SolarPlanet.S_GOLD;
                if (energy) Gx.circle(Math.round(x), Math.round(y), Math.max(2, Math.round(sz * 0.8f)), s.t.color);
                else { Gx.Img rk = rockImg(s.t.name.hashCode()); if (rk != null) { int d = Math.round(sz * 2); Gx.image(rk, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d, s.t.color); } }
            }
        }
        // beams from ships
        for (Ship s : ships) {
            float r = s.rad, bob = (float) Math.sin(s.bob * 2) * 0.03f;
            float x = cx + (float) Math.cos(s.ang) * r * R, y = cy + ((float) Math.sin(s.ang) * r + bob) * R;
            if (s.beamT > 0) beamLine(x, y, cx + s.bx * R, cy + s.by * R, R * 0.012f * s.t.width * (s.t.sprite == 1 ? 2.5f : 1), s.t.color);
            int d = Math.round(R * (s.t.sprite == 1 ? 0.55f : s.t.sprite == 5 ? 0.09f : 0.16f));
            Gx.Img im = shipImg(s.t.sprite);
            if (im != null) Gx.image(im, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d * (s.t.sprite == 6 ? 3 : 4) / 4, 0xFFFFFFFF);
            if (glow != null) Gx.image(glow, Math.round(x - d * 0.4f), Math.round(y + d * 0.1f), Math.round(d * 0.8f), Math.round(d * 0.5f), Gx.withAlpha(s.t.color, 0.35f));
        }
        // player's beam
        if (beamOn && beam != null && !p.destroyed) {
            float ox = cx - R * 2.6f, oy = cy - R * 1.7f;
            int col = beam.style == SolarPlanet.S_PAINT ? hue(time) : beam.color;
            for (int i = 0; i < beam.n; i++) {
                float off = beam.n == 1 ? 0 : (i - (beam.n - 1) / 2f) * 0.12f;
                beamLine(ox + off * R * 3, oy, cx + (beamX + off) * R, cy + beamY * R, R * 0.014f * beam.width * sizeMul, col);
            }
        }
        // areas: pulsing glow at the spot
        for (Area a : areas) {
            double[] pr = p.project(a.lon, a.lat);
            if (pr[2] <= 0 || glow == null) continue;
            float f = Math.min(1, a.age / a.t.dur), s = R * (0.1f + 0.3f * f) * (a.t.r / 80f) * sizeMul;
            float x = cx + (float) pr[0] * p.scale * R, y = cy + (float) pr[1] * p.scale * R;
            Gx.image(glow, Math.round(x - s), Math.round(y - s), Math.round(s * 2), Math.round(s * 2), Gx.withAlpha(a.t.color, 0.5f * (1 - f * 0.5f)));
        }
        // particles
        for (Part q : parts) {
            float k = q.life / q.max;
            float x = cx + q.x * R, y = cy + q.y * R, s = Math.max(1, q.size * R * (q.glow ? (0.6f + 0.6f * k) : 1));
            if (q.sprite == 0 && glow != null) { Gx.image(glow, Math.round(x - s), Math.round(y - s), Math.round(s * 2), Math.round(s * 2), Gx.withAlpha(q.color, Math.min(1, k * 1.5f))); continue; }
            if (q.sprite == 2 && glow != null) { Gx.image(glow, Math.round(x - s * 1.5f), Math.round(y - s * 1.5f), Math.round(s * 3), Math.round(s * 3), Gx.withAlpha(q.color, k)); continue; }
            Gx.Img rk = rockImg((int) (q.size * 1000));
            if (rk != null) Gx.image(rk, Math.round(x - s), Math.round(y - s), Math.round(s * 2), Math.round(s * 2), Gx.withAlpha(q.color, Math.min(1, k * 2)));
        }
    }

    /** Glowing beam from (x0,y0) to (x1,y1) made from a row of glow sprites + a hot core. */
    static void beamLine(float x0, float y0, float x1, float y1, float w, int color) {
        Gx.Img glow = glowImg();
        if (glow == null) return;
        float dx = x1 - x0, dy = y1 - y0, len = (float) Math.hypot(dx, dy);
        float stepLen = Math.max(2, w * 1.2f);
        int n = Math.min(260, Math.max(2, Math.round(len / stepLen)));
        int d = Math.max(3, Math.round(w * 5)), dc = Math.max(2, Math.round(w * 1.6f));
        for (int i = 0; i <= n; i++) {
            float t = i / (float) n;
            int x = Math.round(x0 + dx * t), y = Math.round(y0 + dy * t);
            Gx.image(glow, x - d / 2, y - d / 2, d, d, Gx.withAlpha(color, 0.55f));
            Gx.image(glow, x - dc / 2, y - dc / 2, dc, dc, 0xE6FFFFFF);
        }
        int e = Math.round(w * 14);
        Gx.image(glow, Math.round(x1 - e / 2f), Math.round(y1 - e / 2f), e, e, Gx.withAlpha(color, 0.9f));
        Gx.image(glow, Math.round(x0 - e / 3f), Math.round(y0 - e / 3f), e * 2 / 3, e * 2 / 3, Gx.withAlpha(color, 0.8f));
    }

    // ------------------------------------------------------------------ small christmas sprites
    private static void paintGift(Graphics2D g) {
        g.setColor(new Color(0xD62238)); g.fill(new RoundRectangle2D.Double(14, 30, 68, 58, 8, 8));
        g.setColor(new Color(0xB3121F)); g.fill(new RoundRectangle2D.Double(10, 24, 76, 16, 6, 6));
        g.setColor(new Color(0xF4C152)); g.fillRect(42, 24, 12, 64); g.fillRect(10, 29, 76, 6);
        g.fill(new Ellipse2D.Double(26, 8, 22, 18)); g.fill(new Ellipse2D.Double(48, 8, 22, 18));
    }

    private static void paintSnowman(Graphics2D g) {
        g.setColor(new Color(0xF4F8FF)); g.fill(new Ellipse2D.Double(20, 44, 56, 50)); g.fill(new Ellipse2D.Double(30, 18, 36, 34));
        g.setColor(new Color(0x1A1A1A)); g.fillRect(32, 6, 32, 6); g.fillRect(38, -8, 20, 16);
        g.fill(new Ellipse2D.Double(40, 30, 5, 5)); g.fill(new Ellipse2D.Double(52, 30, 5, 5));
        g.setColor(new Color(0xFF8A2A)); g.fill(new java.awt.Polygon(new int[]{47, 62, 47}, new int[]{36, 40, 42}, 3));
        g.setColor(new Color(0xE3263B)); g.fillRect(30, 48, 36, 7);
    }

    private static void paintSleigh(Graphics2D g) {
        g.setColor(new Color(0xB08A5A));
        for (int i = 0; i < 3; i++) { g.fill(new Ellipse2D.Double(4 + i * 22, 30, 22, 12)); g.fillRect(8 + i * 22, 40, 3, 12); g.fillRect(20 + i * 22, 40, 3, 12); }
        g.setColor(new Color(0xE3263B)); g.fill(new Ellipse2D.Double(24, 32, 5, 5));
        g.setColor(new Color(0xB5152C)); g.fill(new RoundRectangle2D.Double(74, 30, 46, 26, 10, 10));
        g.setColor(new Color(0xF4C152)); g.fillRect(70, 58, 54, 4);
        g.setColor(new Color(0x7A5A3A)); g.fill(new Ellipse2D.Double(92, 12, 26, 26));
    }
}
