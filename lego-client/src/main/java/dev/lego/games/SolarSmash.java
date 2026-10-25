package dev.lego.games;

import java.util.ArrayList;
import java.util.List;

import dev.lego.games.SolarTools.Tool;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.Sound;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;

/**
 * "Solar Smash": a high resolution solar system, click a planet and try 100+ tools on it
 * (lasers, meteors, UFOs, natural disasters, black holes, fun stuff ...). Full screen, own loop.
 */
public final class SolarSmash extends View {
    private final List<SolarPlanet> bodies = new ArrayList<>();
    private SolarPlanet focus;
    private SolarFx fx;
    /** 0 = system view, 1 = planet view (animated) */
    private float zoom, zoomTarget;
    private int cat = 0, tool = 0;
    private boolean paused, slow, hideUi, drawerOpen = true;
    private float sim;
    private boolean mouseHeld;
    private long destroyedCount;
    private final SolarPlanet.Disc big = new SolarPlanet.Disc("big");
    private final java.util.Map<SolarPlanet, SolarPlanet.Disc> small = new java.util.HashMap<>();
    // focus planet position in the system view (for the zoom transition)
    private float fsx, fsy, fsr;

    public SolarSmash() {
        //                          type                name        kind                   population       temp   atmo col   atmo size tilt  seed minor
        add(new SolarPlanet(SolarPlanet.SUN, "Helios", "Stern", 0, 5500, 0xFFFFB040, 1f, 2.2f, 0.1f, 1, false), 0, 0);
        add(new SolarPlanet(SolarPlanet.LAVA, "Vulkanis", "Lavaplanet", 40_000_000L, 420, 0xFFFF6A2A, 0.6f, 0.55f, 0.2f, 7, false), 3.4f, 0.9f);
        add(new SolarPlanet(SolarPlanet.DESERT, "Dunaria", "Wüstenwelt", 900_000_000L, 48, 0xFFFFC890, 0.7f, 0.7f, 0.3f, 13, false), 4.6f, 2.4f);
        add(new SolarPlanet(SolarPlanet.TERRA, "Terra", "Erdähnlich", 8_100_000_000L, 15, 0xFF6AB4FF, 1f, 0.85f, 0.41f, 3, false), 5.9f, 4.1f);
        add(new SolarPlanet(SolarPlanet.MARS, "Karos", "Rostwelt", 2_000_000L, -60, 0xFFFF9A6A, 0.35f, 0.62f, 0.44f, 21, false), 7.1f, 5.6f);
        add(new SolarPlanet(SolarPlanet.OCEAN, "Aquaris", "Wasserwelt", 2_400_000_000L, 22, 0xFF4AD0FF, 1.1f, 0.9f, 0.2f, 31, false), 8.5f, 1.4f);
        SolarPlanet gas = new SolarPlanet(SolarPlanet.GAS, "Jovara", "Gasriese", 0, -140, 0xFFFFD8A8, 1.2f, 1.45f, 0.35f, 41, false);
        gas.ringed = true;
        add(gas, 10.3f, 3.3f);
        add(new SolarPlanet(SolarPlanet.ICE, "Glacia", "Eisplanet", 350_000_000L, -95, 0xFFBFE8FF, 0.8f, 0.75f, 0.5f, 51, false), 12.0f, 5.0f);
        add(new SolarPlanet(SolarPlanet.MOON, "Selene", "Mond", 0, -20, 0xFFD8D4CC, 0f, 0.4f, 0.1f, 61, true), 13.3f, 0.3f);
        for (SolarPlanet b : bodies) b.request(false);
    }

    private void add(SolarPlanet b, float orbit, float angle) {
        b.orbitR = orbit;
        b.orbitA = angle;
        b.orbitW = orbit <= 0 ? 0 : (float) (0.25 / Math.pow(orbit, 1.2));
        bodies.add(b);
    }

    @Override public boolean blur() { return false; }
    @Override public float designW() { return 900; }
    @Override public float designH() { return 520; }
    @Override public float openMs() { return 380; }

    private List<Tool> tools() { return SolarTools.of(cat); }
    private Tool current() { List<Tool> l = tools(); return l.get(Math.max(0, Math.min(l.size() - 1, tool))); }

    private void focus(SolarPlanet b, float sx, float sy, float sr) {
        if (focus != b) {
            if (focus != null) focus.release();
            focus = b;
            fx = new SolarFx(b);
        }
        b.request(true);
        fsx = sx; fsy = sy; fsr = sr;
        zoomTarget = 1;
        Sound.play("minecraft:block.beacon.activate", 1.6f, 0.4f);
    }

    // ================================================================== frame
    @Override
    public void draw() {
        float dt = Math.min(0.05f, Gx.dt) * (paused ? 0 : slow ? 0.25f : 1);
        sim += dt;
        zoom += (zoomTarget - zoom) * Math.min(1, Gx.dt * 4.5f);
        if (zoomTarget == 0 && zoom < 0.01f) zoom = 0;
        for (SolarPlanet b : bodies) {
            b.orbitA += b.orbitW * dt * (zoom > 0.5f ? 0.1f : 1);
            b.update(dt, sim);
        }
        if (fx != null && zoom > 0.3f) fx.update(dt);
        if (focus != null && focus.destroyed && focus.destroyedT > 0.1f && focus.destroyedT < 0.2f) destroyedCount++;

        background();
        float sysA = 1 - Ease.inOutCubic(Math.min(1, zoom * 1.6f));
        if (sysA > 0.01f) { Gx.pushAlpha(sysA); drawSystem(); Gx.popAlpha(); }
        if (focus != null && zoom > 0.02f) drawPlanet();
        if (fx != null && fx.tintA > 0.01f) Gx.fill(0, 0, W, H, Gx.withAlpha(fx.tint, fx.tintA * 0.35f));
        if (fx != null && fx.flash > 0.01f) Gx.fill(0, 0, W, H, Gx.withAlpha(0xFFFFF4E0, fx.flash * 0.8f));
        if (!hideUi) hud();
        else Gx.textRight("H = Oberfläche einblenden", W - p(16), H - p(14), pf(7), Fonts.MEDIUM, 0x66FFFFFF);
    }

    private void background() {
        Gx.fill(0, 0, W, H, 0xFF03040A);
        Gx.Img neb = Gx.get("solar:neb", 0, () -> SolarTex.nebula(1280, 720, 5));
        float par = zoom * 0.03f;
        if (neb != null) Gx.image(neb, Math.round(-W * (0.02f + par)), Math.round(-H * (0.02f + par)), Math.round(W * (1.04f + par * 2)), Math.round(H * (1.04f + par * 2)), 0xFFFFFFFF);
        Gx.Img s1 = Gx.get("solar:stars1", 0, () -> SolarTex.stars(1024, 900, 0.8f, 9)), s2 = Gx.get("solar:stars2", 0, () -> SolarTex.stars(512, 160, 1f, 19));
        if (s1 != null) for (int x = 0; x < W; x += 1024) for (int y = 0; y < H; y += 1024) Gx.image(s1, x, y, 1024, 1024, 0xFFFFFFFF);
        float tw = 0.75f + 0.25f * (float) Math.sin(sim * 1.3);
        if (s2 != null) for (int x = -((int) (zoom * 60) % 1024); x < W; x += 1024) for (int y = 0; y < H; y += 1024) Gx.image(s2, x, y, 1024, 1024, Gx.withAlpha(0xFFFFFFFF, tw));
    }

    // ================================================================== system view
    private void drawSystem() {
        float cx = W * 0.5f, cy = H * 0.52f;
        float unit = Math.min(W, H) / 30f;
        SolarPlanet hoverB = null;
        float hx = 0, hy = 0, hr = 0;
        int budget = 120_000;
        Gx.Img orb = Gx.get("solar:orbit", 0, () -> SolarTex.orbitRing(1024, 1.2f));
        for (SolarPlanet b : bodies) {
            if (b.orbitR <= 0 || orb == null) continue;
            float rx = b.orbitR * unit * 1.25f, ry = rx * 0.42f;
            Gx.image(orb, Math.round(cx - rx), Math.round(cy - ry), Math.round(rx * 2), Math.round(ry * 2), 0x40FFFFFF);
        }
        // back to front by y
        List<SolarPlanet> order = new ArrayList<>(bodies);
        order.sort((a, c) -> Float.compare((float) Math.sin(a.orbitA) * a.orbitR, (float) Math.sin(c.orbitA) * c.orbitR));
        for (SolarPlanet b : order) {
            float rx = b.orbitR * unit * 1.25f, ry = rx * 0.42f;
            float x = cx + (float) Math.cos(b.orbitA) * rx, y = cy + (float) Math.sin(b.orbitA) * ry;
            float r = b.size * unit * (b.type == SolarPlanet.SUN ? 1.4f : 0.9f) * (0.85f + 0.15f * (float) Math.sin(b.orbitA));
            if (b.type == SolarPlanet.SUN) sunGlow(x, y, r);
            SolarPlanet.Disc dc = small.computeIfAbsent(b, k -> new SolarPlanet.Disc("s" + k.name));
            int D = Math.max(16, Math.round(r * 2));
            dc.setup(D, b.tilt, b.type == SolarPlanet.SUN);
            float lx = cx - x, ly = cy - y, ll = (float) Math.hypot(lx, ly) + 0.001f;
            if (b.type == SolarPlanet.SUN) dc.light(0, 0, 1); else dc.light(lx / ll * 0.95f, ly / ll * 0.95f, 0.3f);
            if (b.refresh(dc, false, budget, false)) budget -= 0; else budget = 0;
            if (b.destroyed) debrisRing(x, y, r);
            else {
                if (b.atmo > 0.05f) halo(x, y, r, b.atmoCol, 0.35f * b.atmo);
                SolarPlanet.drawDisc(dc, x, y, r, 0xFFFFFFFF);
            }
            if (hover(Math.round(x - r - p(6)), Math.round(y - r - p(6)), Math.round(r * 2 + p(12)), Math.round(r * 2 + p(12)))) { hoverB = b; hx = x; hy = y; hr = r; }
            final SolarPlanet fb = b;
            final float fx0 = x, fy0 = y, fr0 = r;
            Kit.hit(Math.round(x - r - p(6)), Math.round(y - r - p(6)), Math.round(r * 2 + p(12)), Math.round(r * 2 + p(12))).click(() -> focus(fb, fx0, fy0, fr0));
        }
        if (hoverB != null) {
            Gx.ringAt(Math.round(hx), Math.round(hy), Math.round(hr + p(5)), Math.max(1, p(1)), 0xAAFFFFFF);
            label(hoverB.name.toUpperCase(java.util.Locale.ROOT), hoverB.kind + (hoverB.pop > 0 ? " · " + big(hoverB.alive()) + " Bewohner" : ""), hx, hy - hr - p(14));
        }
        Gx.textSpaced("SOLAR SMASH", W / 2f, p(34), pf(22), Fonts.BOLD, 0xFFFFFFFF, pf(6));
        Gx.textSpaced("KLICK AUF EINEN PLANETEN", W / 2f, p(58), pf(7.4f), Fonts.MEDIUM, 0x99FFFFFF, pf(2.4f));
    }

    private void label(String t, String sub, float cx, float bottom) {
        float w = Math.max(Gx.spacedWidth(t, pf(9), Fonts.BOLD, pf(2)), Gx.width(sub, pf(7), Fonts.MEDIUM)) + p(24);
        int x = Math.round(cx - w / 2), y = Math.round(bottom - p(34));
        Gx.rect(x, y, Math.round(w), p(32), p(9), 0xCC0A0D18);
        Gx.outline(x, y, Math.round(w), p(32), p(9), Math.max(1, p(0.6)), 0x33FFFFFF);
        Gx.textSpaced(t, cx, y + p(11), pf(9), Fonts.BOLD, 0xFFFFFFFF, pf(2));
        Gx.textCenter(sub, cx, y + p(23), pf(7), Fonts.MEDIUM, 0xAAFFFFFF);
    }

    private void sunGlow(float x, float y, float r) {
        Gx.Img co = Gx.get("solar:corona", 0, () -> SolarTex.corona(512, 3));
        float pulse = 1 + 0.03f * (float) Math.sin(sim * 2);
        if (co != null) { int d = Math.round(r * 5.2f * pulse); Gx.image(co, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d, 0xFFFFFFFF); }
        Gx.glow(Math.round(x), Math.round(y), Math.round(r * 3.2f), 0x55FFB040);
    }

    private void halo(float x, float y, float r, int col, float a) {
        Gx.Img h = Gx.get("solar:halo", 0, () -> SolarTex.halo(256));
        if (h == null) return;
        int d = Math.round(r * 2.5f);
        Gx.image(h, Math.round(x - d / 2f), Math.round(y - d / 2f), d, d, Gx.withAlpha(col, Math.min(1, a)));
    }

    private void debrisRing(float x, float y, float r) {
        Gx.Img rk = Gx.get("solar:fx:rock1", 0, () -> SolarTex.rock(64, 12, 0xFFFFFFFF));
        if (rk == null) return;
        for (int i = 0; i < 18; i++) {
            double a = i * 0.7 + sim * 0.2, d = r * (0.6 + (i % 5) * 0.2);
            int s = Math.max(2, Math.round(r * (0.12f + (i % 3) * 0.06f)));
            Gx.image(rk, (int) Math.round(x + Math.cos(a) * d - s / 2.0), (int) Math.round(y + Math.sin(a) * d * 0.6 - s / 2.0), s, s, 0xFF8A7A6A);
        }
    }

    // ================================================================== planet view
    private float pcx() { return W * 0.46f; }
    private float pcy() { return H * 0.46f; }
    private float pR() { return Math.min(W * 0.23f, H * 0.3f); }

    private void drawPlanet() {
        SolarPlanet b = focus;
        float k = Ease.inOutCubic(zoom);
        float shake = b.shake * p(6);
        float cx = fsx + (pcx() - fsx) * k + (fx.rnd.nextFloat() - 0.5f) * shake, cy = fsy + (pcy() - fsy) * k + (fx.rnd.nextFloat() - 0.5f) * shake;
        float R = fsr + (pR() - fsr) * k;
        Gx.pushAlpha(Math.min(1, zoom * 2));
        // distant sun in the corner (light source)
        if (b.type != SolarPlanet.SUN) sunGlow(-W * 0.08f, -H * 0.12f, Math.min(W, H) * 0.12f);
        fx.drawBack(cx, cy, R);
        float Rs = R * b.scale;
        if (!b.destroyed) {
            if (b.atmo > 0.02f) halo(cx, cy, Rs, b.atmoCol, 0.55f * b.atmo / Math.max(0.3f, b.atmo0));
            if (b.type == SolarPlanet.SUN) sunGlow(cx, cy, Rs);
            Gx.Img[] ring = null;
            if (b.ringed && b.ringAlive() > 0.02f) {
                int rw = Math.max(8, Math.round(Rs * 2.35f)), rh = Math.max(8, Math.round(rw * 0.62f));
                ring = b.ringImgs(rw, rh, Rs / 2, -0.55f, -0.45f, 0.7f);
                Gx.image(ring[0], Math.round(cx - rw), Math.round(cy - rh), rw * 2, rh * 2, 0xFFFFFFFF);
            }
            int D = Math.max(32, Math.min(1400, Math.round(Rs * 2)));
            big.setup(D, b.tilt, b.type == SolarPlanet.SUN);
            if (b.type == SolarPlanet.SUN) big.light(0, 0, 1); else big.light(-0.62f, -0.42f, 0.66f);
            b.refresh(big, true, 170_000, false);
            int col = b.hitFlash > 0.01f ? Gx.mix(0xFFFFFFFF, 0xFFFFD0B0, b.hitFlash) : 0xFFFFFFFF;
            SolarPlanet.drawDisc(big, cx, cy, Rs, col);
            if (ring != null) Gx.image(ring[1], Math.round(cx - ring[1].w), Math.round(cy - ring[1].h), ring[1].w * 2, ring[1].h * 2, 0xFFFFFFFF);
            if (b.loading() && zoom > 0.9f) Gx.textCenter("Oberfläche wird in 4K erzeugt …", cx, cy + Rs + p(18), pf(8), Fonts.MEDIUM, 0xAAFFFFFF);
        }
        fx.drawFront(cx, cy, R);
        // aim cursor
        if (zoom > 0.95f && !overUi()) {
            Tool t = current();
            float s = Math.max(p(6), R * Math.min(0.5f, t.r / 1024f) * fx.sizeMul);
            Gx.ringAt(Math.round(mx), Math.round(my), Math.round(s), Math.max(1, p(0.8)), Gx.withAlpha(t.color, 0.8f));
            Gx.fill(Math.round(mx) - p(4), Math.round(my), p(8), Math.max(1, p(0.8)), 0xCCFFFFFF);
            Gx.fill(Math.round(mx), Math.round(my) - p(4), Math.max(1, p(0.8)), p(8), 0xCCFFFFFF);
        }
        Gx.popAlpha();
        if (mouseHeld && fx.beamOn) { fx.beamX = (mx - cx) / R; fx.beamY = (my - cy) / R; }
    }

    // ================================================================== HUD
    private int uiTop, uiBottom;

    private boolean overUi() { return my > uiBottom || (my < uiTop) || (zoom > 0.5f && mx > W - p(250)); }

    private void hud() {
        uiTop = p(64);
        uiBottom = H - (zoom > 0.5f ? p(drawerOpen ? 132 : 44) : p(40));
        // top bar
        Gx.gradientV(0, 0, W, p(70), 0xCC000000, 0x00000000);
        int bx = p(16), by = p(14);
        button("back", zoom > 0.5f ? "System" : "Zurück", bx, by, () -> {
            if (zoomTarget > 0.5f) { zoomTarget = 0; fx.release(); }
            else host.open(dev.lego.ui.bench.BenchView.at("GAMES"));
        });
        if (zoom > 0.5f && focus != null) {
            button("reset", "Reparieren", bx + p(96), by, () -> { focus.repair(); fx.clear(); Sound.play("minecraft:block.beacon.activate", 1.2f, 0.5f); });
            button(slow ? "check" : "clock", slow ? "Zeitlupe an" : "Zeitlupe", bx + p(206), by, () -> slow = !slow);
            button(paused ? "play" : "pause", paused ? "Weiter" : "Pause", bx + p(316), by, () -> paused = !paused);
            button("eye", "UI aus (H)", bx + p(416), by, () -> hideUi = true);
            Gx.textSpaced(focus.name.toUpperCase(java.util.Locale.ROOT), W / 2f + p(40), p(30), pf(16), Fonts.BOLD, 0xFFFFFFFF, pf(5));
            Gx.textSpaced(focus.kind.toUpperCase(java.util.Locale.ROOT), W / 2f + p(40), p(50), pf(7), Fonts.MEDIUM, 0x99FFFFFF, pf(2.4f));
            info();
            drawer();
        } else {
            Gx.textRight(destroyedCount + " Planeten zerstört", W - p(18), p(28), pf(8), Fonts.MEDIUM, 0x99FFFFFF);
        }
    }

    private void button(String icon, String label, int x, int y, Runnable r) {
        float fs = pf(7.6f);
        int w = Math.round(Gx.width(label, fs, Fonts.BOLD)) + p(34), h = p(26);
        boolean hv = hover(x, y, w, h);
        float a = Ease.to("ss:b" + label, hv ? 1 : 0, 14);
        Gx.rect(x, y, w, h, h / 2, Gx.mix(0xB0141826, 0xE0262C44, a));
        Gx.outline(x, y, w, h, h / 2, Math.max(1, p(0.6)), Gx.mix(0x33FFFFFF, 0x88FFFFFF, a));
        Gx.icon(icon, x + p(14), y + h / 2f, pf(10), 0xFFFFFFFF);
        Gx.textMid(label, x + p(24), y + h / 2f, fs, Fonts.BOLD, 0xFFFFFFFF);
        Kit.hit(x, y, w, h).click(r);
    }

    private void info() {
        SolarPlanet b = focus;
        int w = p(230), x = W - w - p(16), y = p(78), h = p(220);
        Gx.rect(x, y, w, h, p(14), 0xC80A0D18);
        Gx.outline(x, y, w, h, p(14), Math.max(1, p(0.6)), 0x2AFFFFFF);
        b.aliveShown += (b.alive() - b.aliveShown) * Math.min(1, Gx.dt * 3);
        b.deadShown += (b.dead() - b.deadShown) * Math.min(1, Gx.dt * 3);
        int yy = y + p(18);
        cap("BEVÖLKERUNG", x + p(16), yy);
        Gx.text(b.pop <= 0 ? "unbewohnt" : fmt(b.aliveShown), x + p(16), yy + p(8), pf(15), Fonts.BOLD, 0xFF6CFF9C);
        yy += p(40);
        cap("AUSGELÖSCHT", x + p(16), yy);
        Gx.text(fmt(b.deadShown), x + p(16), yy + p(8), pf(13), Fonts.BOLD, 0xFFFF5A6A);
        yy += p(38);
        float surv = b.pop <= 0 ? 0 : (float) (b.aliveShown / b.pop);
        bar(x + p(16), yy, w - p(32), "Überlebende", b.pop <= 0 ? "–" : pct(surv), surv, 0xFF6CFF9C);
        yy += p(30);
        float integ = b.integrity();
        bar(x + p(16), yy, w - p(32), "Zerstörung", pct(1 - integ), 1 - integ, 0xFFFF8A3A);
        yy += p(30);
        Gx.textMid("Temperatur", x + p(16), yy + p(4), pf(7.2f), Fonts.MEDIUM, 0xAAFFFFFF);
        Gx.textRight(Math.round(b.temp()) + " °C", x + w - p(16), yy + p(4), pf(7.6f), Fonts.BOLD, 0xFFFFFFFF);
        yy += p(16);
        Gx.textMid("Atmosphäre", x + p(16), yy + p(4), pf(7.2f), Fonts.MEDIUM, 0xAAFFFFFF);
        Gx.textRight(b.atmo0 <= 0 ? "keine" : pct(b.atmo / b.atmo0), x + w - p(16), yy + p(4), pf(7.6f), Fonts.BOLD, 0xFFFFFFFF);
        if (b.destroyed) Gx.textSpaced("PLANET ZERSTÖRT", x + w / 2f, y + h - p(14), pf(7.6f), Fonts.BOLD, 0xFFFF5A6A, pf(2));
    }

    private void cap(String s, int x, int y) {
        float w = Gx.spacedWidth(s, pf(6.4f), Fonts.BOLD, pf(1.6f));
        Gx.textSpaced(s, x + w / 2f, y, pf(6.4f), Fonts.BOLD, 0x88FFFFFF, pf(1.6f));
    }

    private void bar(int x, int y, int w, String label, String val, float f, int col) {
        Gx.textMid(label, x, y + p(4), pf(7.2f), Fonts.MEDIUM, 0xAAFFFFFF);
        Gx.textRight(val, x + w, y + p(4), pf(7.6f), Fonts.BOLD, 0xFFFFFFFF);
        int bh = Math.max(3, p(4));
        Gx.rect(x, y + p(12), w, bh, bh / 2, 0x22FFFFFF);
        Gx.rect(x, y + p(12), Math.max(bh, Math.round(w * Math.max(0, Math.min(1, f)))), bh, bh / 2, col);
    }

    private void drawer() {
        int h = p(drawerOpen ? 122 : 34), y = H - h - p(10), x = p(16), w = W - p(32) - p(246);
        Gx.rect(x, y, w, h, p(14), 0xD00A0D18);
        Gx.outline(x, y, w, h, p(14), Math.max(1, p(0.6)), 0x2AFFFFFF);
        // categories
        int cx = x + p(10), cy = y + p(6);
        for (int i = 0; i < SolarTools.CATS.length; i++) {
            String c = (i < 9 ? (i + 1) + "  " : "") + SolarTools.CATS[i];
            float fs = pf(SolarTools.CATS.length > 8 ? 6.4f : 7.2f);
            int cw = Math.round(Gx.width(c, fs, Fonts.BOLD)) + p(30), ch = p(22);
            boolean sel = i == cat, hv = hover(cx, cy, cw, ch);
            Gx.rect(cx, cy, cw, ch, ch / 2, sel ? 0xFFE3263B : hv ? 0x33FFFFFF : 0x14FFFFFF);
            Gx.icon(SolarTools.CAT_ICONS[i], cx + p(12), cy + ch / 2f, pf(9), 0xFFFFFFFF);
            Gx.textMid(c, cx + p(22), cy + ch / 2f, fs, Fonts.BOLD, sel ? 0xFFFFFFFF : 0xCCFFFFFF);
            final int idx = i;
            Kit.hit(cx, cy, cw, ch).click(() -> { cat = idx; tool = 0; drawerOpen = true; });
            cx += cw + p(6);
        }
        int tg = p(24);
        Gx.icon(drawerOpen ? "shrink" : "expand", x + w - p(16), y + p(17), pf(10), 0xAAFFFFFF);
        Kit.hit(x + w - tg - p(4), y + p(4), tg, tg).click(() -> drawerOpen = !drawerOpen);
        if (!drawerOpen) return;
        // tools of the category
        List<Tool> l = tools();
        int tw = p(92), th = p(78), ty = y + p(36), tx = x + p(10), gap = p(6);
        int per = Math.max(1, (w - p(20)) / (tw + gap));
        int page = tool / per;
        int start = page * per;
        Tool hoverT = null;
        int hoverX = 0;
        for (int i = start; i < Math.min(l.size(), start + per); i++) {
            Tool t = l.get(i);
            int bx = tx + (i - start) * (tw + gap);
            boolean sel = i == tool, hv = hover(bx, ty, tw, th);
            float a = Ease.to("ss:t" + t.name, sel ? 1 : hv ? 0.5f : 0, 16);
            Gx.rect(bx, ty, tw, th, p(10), Gx.mix(0x1AFFFFFF, Gx.withAlpha(t.color, 0.28f), a));
            Gx.outline(bx, ty, tw, th, p(10), Math.max(1, p(sel ? 1.2 : 0.6)), sel ? t.color : 0x22FFFFFF);
            Gx.glow(bx + tw / 2, ty + p(26), p(20), Gx.withAlpha(t.color, 0.25f + 0.25f * a));
            Gx.circle(bx + tw / 2, ty + p(26), p(12), Gx.mix(t.color, 0xFF000000, 0.35f));
            Gx.icon(iconOf(t), bx + tw / 2f, ty + p(26), pf(13), 0xFFFFFFFF);
            java.util.List<String> nl = Fonts.wrap(t.name, Fonts.BOLD, pf(6.8f), tw - p(8), 2);
            for (int k = 0; k < nl.size(); k++) Gx.textCenter(nl.get(k), bx + tw / 2f, ty + p(52 + k * 10), pf(6.8f), Fonts.BOLD, 0xFFFFFFFF);
            final int idx = i;
            Kit.hit(bx, ty, tw, th).click(() -> tool = idx);
            if (hv) { hoverT = t; hoverX = bx + tw / 2; }
        }
        int pages = (l.size() + per - 1) / per;
        if (pages > 1) {
            Gx.textRight((page + 1) + " / " + pages + "   ◀ ▶", x + w - p(44), y + p(17), pf(7), Fonts.MEDIUM, 0x99FFFFFF);
            Kit.hit(x + w - p(90), y + p(6), p(40), p(22)).click(() -> tool = ((tool / per + 1) % pages) * per);
        }
        if (hoverT != null) Kit.tooltip(hoverT.desc, hoverX, ty - p(4), P);
        Gx.textMid(l.size() + " Werkzeuge · " + SolarTools.ALL.size() + " insgesamt · Mausrad = Größe " + Math.round(fx.sizeMul * 100) + "%", x + p(10), y + h - p(8), pf(6.4f), Fonts.MEDIUM, 0x77FFFFFF);
    }

    private static String iconOf(Tool t) {
        switch (t.kind) {
            case BEAM: return "bolt";
            case SWARM: return "eye";
            case HOLE: return "moon";
            case GLOBAL: return "globe";
            case SPLIT: return "hit";
            case AREA: return "flame";
            case RAIN: return "rain";
            default: return t.cat == 6 ? "heart" : t.cat == 7 ? "gift" : "gem";
        }
    }

    // ================================================================== numbers
    private static String fmt(double v) {
        long n = Math.round(Math.max(0, v));
        return String.format(java.util.Locale.GERMANY, "%,d", n);
    }

    private static String big(double v) {
        if (v >= 1e9) return String.format(java.util.Locale.GERMANY, "%.1f Mrd.", v / 1e9);
        if (v >= 1e6) return String.format(java.util.Locale.GERMANY, "%.1f Mio.", v / 1e6);
        return fmt(v);
    }

    private static String pct(float f) { return String.format(java.util.Locale.GERMANY, "%.1f %%", Math.max(0, Math.min(1, f)) * 100); }

    // ================================================================== input
    @Override
    public boolean mouseDown(float x, float y, int button, boolean dbl) {
        if (Kit.down(x, y, button)) return true;
        if (zoom < 0.9f || focus == null || fx == null) return false;
        if (button == 1) { fx.release(); return true; }
        float R = pR();
        mouseHeld = true;
        fx.fire(current(), (x - pcx()) / R, (y - pcy()) / R);
        return true;
    }

    @Override
    public boolean mouseUp(float x, float y, int button) {
        mouseHeld = false;
        if (fx != null) fx.release();
        return Kit.up();
    }

    @Override
    public boolean scroll(float x, float y, double amount) {
        if (Kit.scroll(x, y, amount)) return true;
        if (fx != null) fx.sizeMul = Math.max(0.3f, Math.min(4f, fx.sizeMul * (amount > 0 ? 1.12f : 1 / 1.12f)));
        return true;
    }

    @Override
    public boolean key(int key, int mods) {
        if (key < 0) return false;
        if (key >= 49 && key <= 48 + Math.min(9, SolarTools.CATS.length)) { cat = key - 49; tool = 0; drawerOpen = true; return true; }
        if (key == 262) { tool = (tool + 1) % tools().size(); return true; }
        if (key == 263) { tool = (tool - 1 + tools().size()) % tools().size(); return true; }
        if (key == 32) { paused = !paused; return true; }
        if (key == 72) { hideUi = !hideUi; return true; }
        if (key == 82 && focus != null) { focus.repair(); fx.clear(); return true; }
        if (key == 256) {
            if (zoomTarget > 0.5f) { zoomTarget = 0; if (fx != null) fx.release(); }
            else host.open(dev.lego.ui.bench.BenchView.at("GAMES"));
            return true;
        }
        return false;
    }

    @Override public void onEscape() { }

    @Override
    public void removed() {
        long total = 0;
        for (SolarPlanet b : bodies) total += Math.round(b.dead() / 1_000_000);
        GameInfo.submit("solarsmash", destroyedCount * 1000 + total);
    }
}
