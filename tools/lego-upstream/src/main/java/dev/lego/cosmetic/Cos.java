package dev.lego.cosmetic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cosmetic registry + equipped items (client side, only visible for you). */
public final class Cos {
    private Cos() { }

    public enum Slot {
        CAPE("Umhänge", "shirt", false), WINGS("Flügel", "wing", false), HAT("Hüte", "crown", true), FACE("Gesicht", "glasses", true),
        BACK("Rücken", "backpack", false), AURA("Auren", "aura", false), PET("Haustiere", "paw", false), VEHICLE("Fahrzeuge", "car", false);
        public final String title, icon;
        /** true = attached to the head, false = to the body. */
        public final boolean head;
        Slot(String title, String icon, boolean head) { this.title = title; this.icon = icon; this.head = head; }
    }

    public enum Rarity {
        COMMON("Gewöhnlich", 0xFF9AA3B2), RARE("Selten", 0xFF3FA9FF), EPIC("Episch", 0xFFB15CFF), LEGENDARY("Legendär", 0xFFFFB02E);
        public final String title;
        public final int color;
        Rarity(String t, int c) { title = t; color = c; }
    }

    /** Animation state for models. */
    public static final class A {
        public float time;        // seconds
        public boolean sneak;
        public float move;        // 0..1 walking amount
        public boolean preview;   // GUI thumbnail
        // ---- pets (world space companions) ----
        /** Current emotion: happy, love, sleepy, sad, excited, angry, curious, dizzy. */
        public String emotion = "happy";
        /** Eye closedness 0 (open) .. 1 (closed) for blinking. */
        public float blink;
        /** Where the pet looks, -1..1 (x = right, y = up), for pupils / head tilt. */
        public float lookX, lookY;
        /** Pet movement speed 0..1 (walking / flying animation). */
        public float speed;
        /** Pet level 1..5 (grows with your XP level). */
        public int level = 3;
        /** Smooth growth 0 (baby) .. 1 (fully grown). */
        public float growth = 0.5f;
        // ---- vehicles ----
        /** Wheel rotation in degrees (accumulated from the distance driven). */
        public float wheel;
        /** Turn amount -1 (left) .. 1 (right). */
        public float lean;
    }

    public interface Model { void render(G g, A a); }

    public static final class Item {
        public final String id, name;
        public final Slot slot;
        public final Rarity rarity;
        public final Model model;
        /** Cape texture name (cape items only). */
        public final String cape;
        Item(String id, String name, Slot slot, Rarity rarity, Model model, String cape) {
            this.id = id; this.name = name; this.slot = slot; this.rarity = rarity; this.model = model; this.cape = cape;
        }
    }

    public static final List<Item> ALL = new ArrayList<>();
    private static final Map<Slot, String> EQUIPPED = new EnumMap<>(Slot.class);
    public static volatile int version = 0;

    public static void add(String id, String name, Slot s, Rarity r, Model m) { ALL.add(new Item(id, name, s, r, m, null)); }
    static void cape(String id, String name, Rarity r) { ALL.add(new Item("cape_" + id, name, Slot.CAPE, r, Models::capePreview, "cape_" + id)); }

    public static synchronized void init() {
        if (!ALL.isEmpty()) return;
        Models.registerAll();
        Pets.registerAll();
        Vehicles.registerAll();
        XmasCos.registerAll();
    }

    public static Item get(String id) {
        for (Item i : ALL) if (i.id.equals(id)) return i;
        return null;
    }

    public static List<Item> of(Slot s) {
        List<Item> out = new ArrayList<>();
        for (Item i : ALL) if (i.slot == s) out.add(i);
        return out;
    }

    public static Item equipped(Slot s) { String id = EQUIPPED.get(s); return id == null ? null : get(id); }
    public static boolean isEquipped(Item i) { return i.id.equals(EQUIPPED.get(i.slot)); }
    public static boolean anyEquipped() { return !EQUIPPED.isEmpty(); }

    public static void toggle(Item i) {
        if (isEquipped(i)) EQUIPPED.remove(i.slot); else EQUIPPED.put(i.slot, i.id);
        version++;
        dev.lego.core.Modules.scheduleSave();
    }

    public static void unequipAll() { EQUIPPED.clear(); version++; dev.lego.core.Modules.scheduleSave(); }

    public static int equippedCount() { return EQUIPPED.size(); }

    public static void register() {
        init();
        dev.lego.core.Modules.persist("cosmetics", new dev.lego.core.Modules.Persist() {
            @Override public Object save() {
                Map<String, Object> m = new LinkedHashMap<>();
                for (Map.Entry<Slot, String> e : EQUIPPED.entrySet()) m.put(e.getKey().name(), e.getValue());
                return m;
            }
            @SuppressWarnings("unchecked")
            @Override public void load(Object o) {
                if (!(o instanceof Map)) return;
                EQUIPPED.clear();
                for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet()) {
                    try {
                        Slot s = Slot.valueOf(e.getKey());
                        if (e.getValue() instanceof String && get((String) e.getValue()) != null) EQUIPPED.put(s, (String) e.getValue());
                    } catch (IllegalArgumentException ignored) { }
                }
                version++;
            }
        });
    }

    /** Per-slot transform hook (e.g. push items outwards when armor is worn). */
    public static java.util.function.BiConsumer<G, Slot> slotTransform = null;

    /** Renders all equipped items of the given anchor (head or body) in cosmetic space. */
    /** Renders a list of cosmetic ids (another Lego user, from Lego-Sync). */
    public static void renderList(G g, A a, boolean head, java.util.List<String> ids) {
        for (String id : ids) {
            Item it = get(id);
            if (it == null || it.slot.head != head || it.slot == Slot.CAPE || it.slot == Slot.PET || it.slot == Slot.VEHICLE) continue;
            g.push();
            g.color(0xFFFFFFFF).glow(false);
            try { it.model.render(g, a); } finally { g.pop(); }
        }
    }

    public static void renderEquipped(G g, A a, boolean head) {
        for (Slot s : Slot.values()) {
            if (s.head != head || s == Slot.CAPE || s == Slot.PET || s == Slot.VEHICLE) continue;
            Item it = equipped(s);
            if (it == null) continue;
            g.push();
            g.color(0xFFFFFFFF).glow(false);
            if (slotTransform != null) slotTransform.accept(g, s);
            try { it.model.render(g, a); } finally { g.pop(); }
        }
    }
}
