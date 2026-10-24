package dev.lego.hud;

import dev.lego.core.Mc;
import dev.lego.core.Modules;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_746;

/** Paket 2: extra HUD panels - each shows real game data. */
public final class HudModules2 {
    private HudModules2() { }

    public static void registerAll() {
        Modules.register(new InvCounter());
        Modules.register(new Gaps());
        Modules.register(new Nearby());
        Modules.register(new Closest());
        Modules.register(new Entities());
        Modules.register(new FreeSlots());
        Modules.register(new Deaths());
    }

    private static int pos = 0;
    private static double fy() { return 0.25 + (pos++ % 14) * 0.045; }

    static final class InvCounter extends HudModules.Simple {
        private static final String[][] ITEMS = {
                {"Totems", "totem_of_undying"}, {"Perlen", "ender_pearl"}, {"Gaps", "golden_apple"}, {"Gods", "enchanted_golden_apple"},
                {"Kristalle", "end_crystal"}, {"Obsi", "obsidian"}, {"Pfeile", "arrow", "spectral_arrow", "tipped_arrow"}, {"XP", "experience_bottle"},
                {"Raketen", "firework_rocket"}, {"Tränke", "potion", "splash_potion", "lingering_potion"}, {"Anker", "respawn_anchor"}, {"Glowstone", "glowstone"}};
        private final dev.lego.core.Setting.Bool[] show = new dev.lego.core.Setting.Bool[ITEMS.length];

        InvCounter() {
            super("h2_invcounter", "Inventar-Zähler", "Zählt in EINEM Panel, was du im Inventar hast: Totems, Perlen, Gaps, Kristalle, Pfeile ... (in den Einstellungen wählen)", "backpack", 0.9, 0.3);
            fresh();
            for (int i = 0; i < ITEMS.length; i++) show[i] = add(new dev.lego.core.Setting.Bool(ITEMS[i][0], i < 4));
        }

        @Override void compute() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ITEMS.length; i++) {
                if (!show[i].get()) continue;
                int n = 0;
                for (int k = 1; k < ITEMS[i].length; k++) n += Mc.countItem(ITEMS[i][k]);
                if (sb.length() > 0) sb.append("  ");
                sb.append(n).append(" ").append(ITEMS[i][0]);
            }
            value = sb.length() == 0 ? "-" : sb.toString();
            unit = "";
        }
    }

    static final class Gaps extends HudModules.Simple {
        Gaps() { super("h2_gaps", "Goldäpfel", "Goldäpfel | verzauberte Goldäpfel", "food", 0.9, fy()); fresh(); }
        @Override void compute() { value = Mc.countItem("golden_apple") + " | " + Mc.countItem("enchanted_golden_apple"); unit = "Gaps"; }
    }

    static final class Nearby extends HudModules.Simple {
        Nearby() { super("h2_nearby", "Spieler in der Nähe", "Wie viele Spieler im Umkreis von 64 Blöcken sind", "users", 0.9, fy()); fresh(); }
        @Override void compute() {
            class_746 me = Mc.player();
            int n = 0;
            if (me != null) for (class_1657 p : Mc.players()) if (p != me && dist(me, p) <= 64) n++;
            value = String.valueOf(n); unit = "Spieler";
        }
    }

    static final class Closest extends HudModules.Simple {
        Closest() { super("h2_closest", "Nächster Spieler", "Name und Entfernung des nächsten Spielers", "user", 0.9, fy()); fresh(); }
        @Override void compute() {
            class_746 me = Mc.player();
            class_1657 best = null;
            double bd = Double.MAX_VALUE;
            if (me != null) for (class_1657 p : Mc.players()) { if (p == me) continue; double d = dist(me, p); if (d < bd) { bd = d; best = p; } }
            value = best == null ? "-" : Mc.name(best);
            unit = best == null ? "" : Math.round(bd) + "m";
        }
    }

    static final class Entities extends HudModules.Simple {
        Entities() { super("h2_entities", "Entitäten", "Geladene Entitäten um dich (Lag-Check)", "entity", 0.9, fy()); fresh(); }
        @Override void compute() { value = String.valueOf(Mc.entities().size()); unit = "Entitäten"; }
    }

    static final class FreeSlots extends HudModules.Simple {
        FreeSlots() { super("h2_freeslots", "Freie Plätze", "Freie Plätze im Inventar", "backpack", 0.9, fy()); fresh(); }
        @Override void compute() {
            int free = 0, n = Math.min(36, Mc.invSize());
            for (int i = 0; i < n; i++) if (Mc.empty(Mc.invStack(i))) free++;
            value = String.valueOf(free); unit = "frei";
        }
    }

    static final class Deaths extends HudModules.Simple {
        private boolean wasDead;
        private int deaths;
        Deaths() { super("h2_deaths", "Tode", "Wie oft du in dieser Sitzung gestorben bist", "skull", 0.9, fy()); fresh(); }
        @Override void compute() {
            class_746 me = Mc.player();
            boolean dead = me != null && Mc.isDead(me);
            if (dead && !wasDead) deaths++;
            wasDead = dead;
            value = String.valueOf(deaths); unit = "Tode";
        }
    }

    static double dist(class_1297 a, class_1297 b) {
        double dx = Mc.x(a) - Mc.x(b), dy = Mc.y(a) - Mc.y(b), dz = Mc.z(a) - Mc.z(b);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
