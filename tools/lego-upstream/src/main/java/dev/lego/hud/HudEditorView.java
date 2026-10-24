package dev.lego.hud;

import java.util.List;

import dev.lego.core.Modules;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.McBackend;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import dev.lego.ui.hub.HubView;
import dev.spotifyhud.SpotifyHudMod;

/** HUD editor: drag, scroll = size, right click = reset one, R = reset all, snapping guides, module list. */
public final class HudEditorView extends View {
    private boolean panelOpen = false;
    private float listScroll = 0, listTarget = 0;
    private boolean draggingHud = false;

    @Override public boolean blur() { return false; }
    @Override public boolean backdrop() { return false; }
    @Override public float openMs() { return 220; }
    @Override public float designW() { return 640; }
    @Override public float designH() { return 380; }

    private int gw() { return W / Gx.S; }
    private int gh() { return H / Gx.S; }

    @Override
    public void draw() {
        SpotifyHudMod.editorOpen = true;
        Kit.hit(0, 0, W, H).id("hud");      // background layer (HUD elements), widgets are registered above it
        float t = open;
        Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.22f * t));
        // grid
        int step = Math.max(8, Math.round(20 * Gx.S));
        for (int x = 0; x < W; x += step) Gx.fill(x, 0, 1, H, Gx.rgba(0xFFFFFF, 0.035f * t));
        for (int y = 0; y < H; y += step) Gx.fill(0, y, W, 1, Gx.rgba(0xFFFFFF, 0.035f * t));
        Gx.fill(W / 2, 0, Math.max(1, Gx.S / 2), H, Gx.rgba(0xFFFFFF, 0.10f * t));
        Gx.fill(0, H / 2, W, Math.max(1, Gx.S / 2), Gx.rgba(0xFFFFFF, 0.10f * t));

        // HUD itself (GUI units)
        int gmx = Math.round(mx / Gx.S), gmy = Math.round(my / Gx.S);
        if (Gx.B instanceof McBackend) {
            Gx.push();
            Gx.scale(Gx.S);
            try {
                HudManager.render(((McBackend) Gx.B).ctx(), gw(), gh(), true, gmx, gmy);
                SpotifyHudMod.editorRender(((McBackend) Gx.B).ctx(), gmx, gmy, gw(), gh());
            } catch (Throwable e) { dev.lego.LegoClient.LOG("HUD-Editor: " + e); }
            Gx.pop();
        }
        // snap guides
        if (HudManager.snapX >= 0) Gx.fill((int) Math.round(HudManager.snapX * Gx.S), 0, Math.max(1, Gx.S / 2), H, Style.accent);
        if (HudManager.snapY >= 0) Gx.fill(0, (int) Math.round(HudManager.snapY * Gx.S), W, Math.max(1, Gx.S / 2), Style.accent);

        // top hint (like Essential)
        String[] hints = {"Ziehen = verschieben", "Mausrad = Größe", "Rechtsklick = zurücksetzen", "R = alle zurücksetzen", "ESC = speichern"};
        float fs = pf(7.4f);
        float total = 0;
        for (String h : hints) total += Gx.width(h, fs, Fonts.MEDIUM);
        total += (hints.length - 1) * pf(14) + pf(28);
        int hx = Math.round(W / 2f - total / 2f), hy = p(10), hh = p(24);
        Gx.pushAlpha(t);
        Gx.shadow(hx, hy, Math.round(total), hh, hh / 2, p(6), 0.35f);
        Gx.rect(hx, hy, Math.round(total), hh, hh / 2, 0xE6111216);
        Gx.outline(hx, hy, Math.round(total), hh, hh / 2, Math.max(1, p(0.6)), 0x22FFFFFF);
        float x = hx + pf(14);
        for (int i = 0; i < hints.length; i++) {
            x += Gx.textMid(hints[i], x, hy + hh / 2f, fs, Fonts.MEDIUM, 0xE6FFFFFF);
            if (i < hints.length - 1) { Gx.circle(Math.round(x + pf(7)), hy + hh / 2, Math.max(1, p(1.3)), Style.accent); x += pf(14); }
        }

        // buttons (top right): modules panel + back to menu
        int bs = p(28);
        int bx = W - bs - p(12), by = p(10);
        boolean hb = hover(bx, by, bs, bs);
        Gx.rect(bx, by, bs, bs, p(8), hb ? 0xF0262830 : 0xE6111216);
        Gx.icon("list", bx + bs / 2f, by + bs / 2f, pf(13), panelOpen ? Style.accent : 0xFFFFFFFF);
        Kit.hit(bx, by, bs, bs).click(() -> panelOpen = !panelOpen);
        int bx2 = bx - bs - p(6);
        boolean hb2 = hover(bx2, by, bs, bs);
        Gx.rect(bx2, by, bs, bs, p(8), hb2 ? 0xF0262830 : 0xE6111216);
        Gx.icon("mods", bx2 + bs / 2f, by + bs / 2f, pf(13), 0xFFFFFFFF);
        Kit.hit(bx2, by, bs, bs).click(() -> { save(); host.open(new HubView()); });
        Gx.popAlpha();

        // module side panel
        float po = Ease.to("hudedit:panel", panelOpen ? 1 : 0, 14);
        if (po > 0.01f) drawPanel(po);

    }

    private void drawPanel(float po) {
        int pw = p(190), ph = H - p(60);
        int py = p(46);
        int px = Math.round(W - p(12) - pw + (1 - Ease.outCubic(po)) * (pw + p(20)));
        Gx.pushAlpha(po);
        Gx.shadow(px, py, pw, ph, p(12), p(8), 0.45f);
        Gx.rect(px, py, pw, ph, p(12), 0xF2111216);
        Gx.outline(px, py, pw, ph, p(12), Math.max(1, p(0.6)), 0x22FFFFFF);
        Gx.text("HUD-Module", px + p(14), py + p(12), pf(10), Fonts.BOLD, 0xFFFFFFFF);
        List<HudModule> mods = HudManager.modules();
        int rowH = p(26);
        int ly = py + p(36), lh = ph - p(44);
        float max = Math.max(0, mods.size() * rowH - lh);
        listTarget = Math.max(0, Math.min(max, listTarget));
        listScroll += (listTarget - listScroll) * (1 - (float) Math.exp(-Gx.dt * 16));
        Gx.clip(px, ly, pw, lh);
        Kit.clip(px, ly, pw, lh);
        Kit.hit(px, ly, pw, lh).scroll(a -> listTarget -= a.floatValue() * p(30));
        for (int i = 0; i < mods.size(); i++) {
            HudModule m = mods.get(i);
            int ry = Math.round(ly + i * rowH - listScroll);
            if (ry + rowH < ly || ry > ly + lh) continue;
            boolean hv = hover(px + p(6), ry, pw - p(12), rowH - p(2));
            if (hv) Gx.rect(px + p(6), ry, pw - p(12), rowH - p(2), p(7), 0x14FFFFFF);
            Gx.icon(m.icon, px + p(18), ry + (rowH - p(2)) / 2f, pf(10), m.enabled ? Style.accent : 0xFF8A8F99);
            Gx.textMid(Fonts.ellipsize(m.name, Fonts.MEDIUM, pf(7.8f), pw - p(80)), px + p(30), ry + (rowH - p(2)) / 2f, pf(7.8f), Fonts.MEDIUM, m.enabled ? 0xFFFFFFFF : 0xFF8A8F99);
            Kit.toggle("hudp:" + m.id, px + pw - p(40), ry + (rowH - p(2)) / 2 - p(7), p(26), p(14), m.enabled, P);
            Kit.hit(px + p(6), ry, pw - p(12), rowH - p(2)).click(() -> { m.toggle(); Modules.scheduleSave(); });
        }
        Kit.unclip();
        Gx.unclip();
        Gx.popAlpha();
    }

    // ------------------------------------------------------------------ input
    @Override
    public boolean mouseDown(float x, float y, int button, boolean dbl) {
        Kit.Region r = Kit.at(x, y);
        if (r != null && !"hud".equals(r.idOrNull())) return Kit.down(x, y, button);
        int gx = Math.round(x / Gx.S), gy = Math.round(y / Gx.S);
        if (SpotifyHudMod.editorDown(gx, gy, button, gw(), gh())) { draggingHud = true; return true; }
        if (HudManager.mouseDown(gx, gy, button, gw(), gh())) { draggingHud = true; return true; }
        return false;
    }

    @Override
    public boolean mouseDrag(float x, float y, int button) {
        if (Kit.dragging()) return Kit.drag(x, y);
        double gx = x / Gx.S, gy = y / Gx.S;
        if (SpotifyHudMod.editorDrag(gx, gy, gw(), gh())) return true;
        return HudManager.mouseDrag(gx, gy, gw(), gh(), true);
    }

    @Override
    public boolean mouseUp(float x, float y, int button) {
        Kit.up();
        SpotifyHudMod.editorUp();
        HudManager.mouseUp();
        draggingHud = false;
        return true;
    }

    @Override
    public boolean scroll(float x, float y, double amount) {
        if (Kit.scroll(x, y, amount)) return true;
        double gx = x / Gx.S, gy = y / Gx.S;
        if (SpotifyHudMod.editorScroll(gx, gy, amount, gw(), gh())) return true;
        return HudManager.scroll(gx, gy, amount, gw(), gh());
    }

    @Override
    public boolean key(int key, int mods) {
        if (key == 82) { HudManager.resetAll(); return true; }      // R
        return false;
    }

    private void save() {
        Modules.save();
    }

    @Override public void onEscape() { save(); host.close(); }

    @Override public void removed() { SpotifyHudMod.editorOpen = false; HudManager.mouseUp(); save(); }
}
