package dev.lego.util;

import java.util.ArrayList;
import java.util.List;

import dev.lego.core.Category;
import dev.lego.core.Mc;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import dev.lego.ui.Ease;
import dev.lego.ui.Fonts;
import dev.lego.ui.Gx;
import dev.lego.ui.Kit;
import dev.lego.ui.LegoScreen;
import dev.lego.ui.Style;
import dev.lego.ui.View;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_746;

/**
 * Fight Preset: save inventory layouts, then see ghost items + green/red overlays in the inventory
 * showing what's correct and what's missing/wrong.
 */
public final class FightPreset extends Module {

    final Setting.Action manage = add(new Setting.Action("Presets verwalten", "Öffnen", () -> LegoScreen.show(new PresetListView())));

    private static final List<Preset> presets = new ArrayList<>();
    private static int active = -1;

    public FightPreset() {
        super("fightpreset", "Kampf-Presets", "Inventar-Vorlagen: zeigt im Inventar, wo was hin muss", Category.UTILITY);
        icon("shield");
    }

    public static Preset activePreset() {
        if (active < 0 || active >= presets.size()) return null;
        return presets.get(active);
    }

    public void renderInventoryOverlay(class_332 ctx) {
        if (!enabled) return;
        Preset p = activePreset();
        if (p == null) return;
        class_746 me = Mc.player();
        if (me == null) return;
        org.joml.Matrix3x2fStack mat = ctx.method_51448();
        for (int i = 0; i < 41; i++) {
            String expected = p.slots[i];
            if (expected == null || expected.isEmpty()) continue;
            class_1799 current = Mc.invStack(i);
            String currentKey = Mc.empty(current) ? "" : Mc.itemKey(current);
            int[] pos = slotScreenPos(i, ctx.method_51421(), ctx.method_51443());
            if (pos == null) continue;
            int sx = pos[0], sy = pos[1], sw = 16;
            boolean correct = currentKey.endsWith("." + expected) || currentKey.equals(expected);
            boolean empty = Mc.empty(current);
            int overlay = correct ? Gx.rgba(0x00CC44, 0.25f) : Gx.rgba(0xFF2222, 0.2f);
            Gx.fill(sx, sy, sw, sw, overlay);
            if (empty) {
                class_1799 ghost = p.ghostStack(i);
                if (ghost != null) {
                    mat.pushMatrix();
                    Gx.pushAlpha(0.3f);
                    mat.translate(sx, sy);
                    ctx.method_51427(ghost, 0, 0);
                    Gx.popAlpha();
                    mat.popMatrix();
                }
            }
        }
    }

    private static int[] slotScreenPos(int slot, int screenW, int screenH) {
        int gw = 176, gh = 166;
        int gx = (screenW - gw) / 2, gy = (screenH - gh) / 2;
        if (slot >= 0 && slot <= 8) return new int[]{gx + 8 + slot * 18, gy + 142};
        if (slot >= 9 && slot <= 35) { int r = (slot - 9) / 9, c = (slot - 9) % 9; return new int[]{gx + 8 + c * 18, gy + 84 + r * 18}; }
        if (slot == 36) return new int[]{gx + 8, gy + 62};
        if (slot == 37) return new int[]{gx + 8, gy + 44};
        if (slot == 38) return new int[]{gx + 8, gy + 26};
        if (slot == 39) return new int[]{gx + 8, gy + 8};
        if (slot == 40) return new int[]{gx + 77, gy + 62};
        return null;
    }

    public static void saveCurrentInventory(String name) {
        class_746 me = Mc.player();
        if (me == null) return;
        Preset p = new Preset(name);
        for (int i = 0; i < 41; i++) {
            class_1799 s = Mc.invStack(i);
            if (!Mc.empty(s)) {
                p.slots[i] = Mc.itemKey(s);
                String key = Mc.itemKey(s);
                int dot = key.lastIndexOf('.');
                p.itemIds[i] = dot >= 0 ? key.substring(dot + 1) : key;
            }
        }
        presets.add(p);
        active = presets.size() - 1;
        Modules.scheduleSave();
    }

    // ================================================================== preset data
    public static final class Preset {
        public String name;
        public final String[] slots = new String[41];
        public final String[] itemIds = new String[41];
        Preset(String name) { this.name = name; }
        public class_1799 ghostStack(int i) {
            if (itemIds[i] == null) return null;
            return Mc.icon(itemIds[i]);
        }
    }

    // ================================================================== preset list view
    public final class PresetListView extends View {
        private String newName = "";
        private float scroll = 0, target = 0;

        @Override public float designW() { return 520; }
        @Override public float designH() { return 400; }

        @Override
        public void draw() {
            float t = Ease.outQuint(open);
            Gx.fill(0, 0, W, H, Gx.rgba(0x000000, 0.35f * open));
            int pw = p(520), ph = p(400), px = (W - pw) / 2, py = (H - ph) / 2;
            Gx.pushAlpha(t);
            Gx.push();
            Gx.scaleAt(W / 2f, H / 2f, 0.94f + 0.06f * t);
            Gx.shadow(px, py, pw, ph, p(18), p(14), 0.55f);
            Gx.rect(px, py, pw, ph, p(18), Style.bg);
            Gx.outline(px, py, pw, ph, p(18), Math.max(1, p(0.6)), Style.stroke);

            int tb = py + p(14), bs = p(30);
            boolean hb = hover(px + p(14), tb, bs, bs);
            Gx.rect(px + p(14), tb, bs, bs, p(9), hb ? Style.surfaceHover : Style.surface);
            Gx.icon("back", px + p(14) + bs / 2f - pf(1), tb + bs / 2f, pf(14), Style.text);
            Kit.hit(px + p(14), tb, bs, bs).click(() -> { if (host != null) host.close(); });
            Gx.text("Kampf-Presets", px + p(54), tb + pf(1), pf(14), Fonts.BOLD, Style.text);
            Gx.text("Inventar-Vorlagen speichern und aktivieren", px + p(54), tb + pf(19), pf(7.4f), Fonts.REGULAR, Style.sub);

            int cx = px + p(16), cw = pw - p(32);
            int y = tb + p(46);

            int ih = p(32);
            Gx.rect(cx, y, cw - p(160), ih, p(10), Style.surface);
            Gx.outline(cx, y, cw - p(160), ih, p(10), Math.max(1, p(0.5)), Style.stroke);
            Gx.text(newName.isEmpty() ? "Preset-Name..." : newName, cx + p(10), y + pf(8), pf(10), Fonts.REGULAR, newName.isEmpty() ? Style.muted : Style.text);

            int sbw = p(70), sbx = cx + cw - p(150);
            boolean sh = hover(sbx, y, sbw, ih);
            Kit.button("save", "Speichern", "save", sbx, y, sbw, ih, P, true, sh);
            Kit.hit(sbx, y, sbw, ih).click(() -> {
                String n = newName.isEmpty() ? "Preset " + (presets.size() + 1) : newName;
                saveCurrentInventory(n);
                newName = "";
            });

            int dbw = p(80), dbx = cx + cw - dbw;
            boolean dh = active >= 0 && active < presets.size() && hover(dbx, y, dbw, ih);
            Kit.button("deact", "Deaktivieren", "x", dbx, y, dbw, ih, P, active >= 0, dh);
            Kit.hit(dbx, y, dbw, ih).click(() -> { active = -1; });

            y += ih + p(14);
            int ly = y, lh = py + ph - p(14) - ly;
            int rh = p(50), gap = p(6);
            float max = Math.max(0, presets.size() * (rh + gap) - gap - lh);
            target = Math.max(0, Math.min(max, target));
            scroll += (target - scroll) * Math.min(1f, Gx.dt * 16);
            Gx.clip(cx - p(4), ly, cw + p(8), lh);
            Kit.clip(cx - p(4), ly, cw + p(8), lh);
            Kit.hit(cx, ly, cw, lh).scroll(a -> target -= a.floatValue() * p(60));

            for (int i = 0; i < presets.size(); i++) {
                int ry = Math.round(ly + i * (rh + gap) - scroll);
                if (ry + rh < ly || ry > ly + lh) continue;
                Preset pr = presets.get(i);
                boolean isActive = i == active;
                Gx.rect(cx, ry, cw, rh, p(12), isActive ? Gx.withAlpha(Style.accent, 0.12f) : Style.surface);
                if (isActive) Gx.outline(cx, ry, cw, rh, p(12), Math.max(1, p(0.7)), Style.accent);
                Gx.text(pr.name, cx + p(14), ry + pf(8), pf(10), Fonts.BOLD, Style.text);
                int items = 0;
                for (String s : pr.slots) if (s != null && !s.isEmpty()) items++;
                Gx.text(items + " Items", cx + p(14), ry + pf(24), pf(7.5f), Fonts.REGULAR, Style.sub);

                int abw = p(70), abh = p(26);
                int aby = ry + (rh - abh) / 2;
                int abx = cx + cw - abw * 2 - p(14);
                boolean asel = !isActive && hover(abx, aby, abw, abh);
                Kit.button("sel" + i, isActive ? "Aktiv" : "Wählen", null, abx, aby, abw, abh, P, !isActive, asel);
                final int idx = i;
                Kit.hit(abx, aby, abw, abh).click(() -> active = idx);

                int rbx = cx + cw - abw - p(6);
                boolean rh2 = hover(rbx, aby, abw, abh);
                Gx.rect(rbx, aby, abw, abh, p(8), rh2 ? 0x40FF3333 : 0x20FF3333);
                Gx.textCenter("Löschen", rbx + abw / 2f, aby + abh / 2f, pf(8), Fonts.MEDIUM, 0xFFFF4444);
                Kit.hit(rbx, aby, abw, abh).click(() -> {
                    presets.remove(idx);
                    if (active >= presets.size()) active = presets.size() - 1;
                    Modules.scheduleSave();
                });
            }

            if (presets.isEmpty()) {
                Gx.icon("shield", px + pw / 2f, ly + p(30), pf(22), Style.muted);
                Gx.textCenter("Keine Presets gespeichert", px + pw / 2f, ly + pf(60), pf(9), Fonts.MEDIUM, Style.sub);
                Gx.textCenter("Rüste dich aus und klick 'Speichern'", px + pw / 2f, ly + pf(76), pf(7.5f), Fonts.REGULAR, Style.muted);
            }
            Kit.unclip();
            Gx.unclip();
            Gx.pop();
            Gx.popAlpha();
        }

        @Override public boolean key(int key, int mods) {
            if (key == 256) { if (host != null) host.close(); return true; }
            if (key == 259 && !newName.isEmpty()) { newName = newName.substring(0, newName.length() - 1); return true; }
            return false;
        }

        @Override public boolean chr(String s) {
            if (s.length() == 1 && newName.length() < 24) { newName += s; return true; }
            return false;
        }
    }
}
