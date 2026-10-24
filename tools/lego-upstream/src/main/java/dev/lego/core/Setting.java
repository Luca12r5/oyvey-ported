package dev.lego.core;

/** Module settings shown in the click GUI and saved in config/legoclient.json. */
public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }

    public String displayName() {
        String t = Lang.get("s." + name);
        return t != null ? t : name;
    }

    public abstract Object save();
    public abstract void load(Object o);

    // ------------------------------------------------------------------ types
    public static final class Bool extends Setting {
        public boolean value;
        public Bool(String name, boolean def) { super(name); value = def; }
        public boolean get() { return value; }
        @Override public Object save() { return value; }
        @Override public void load(Object o) { if (o instanceof Boolean) value = (Boolean) o; }
    }

    public static final class Num extends Setting {
        public final double min, max, step;
        public final String unit;
        public double value;
        public Num(String name, double min, double max, double step, double def, String unit) {
            super(name); this.min = min; this.max = max; this.step = step; this.value = def; this.unit = unit;
        }
        public double get() { return value; }
        public float getF() { return (float) value; }
        public int getI() { return (int) Math.round(value); }
        public void set(double v) {
            v = Math.max(min, Math.min(max, v));
            if (step > 0) v = Math.round((v - min) / step) * step + min;
            value = Math.round(v * 1000.0) / 1000.0;
        }
        public double frac() { return max > min ? (value - min) / (max - min) : 0; }
        public void setFrac(double f) { set(min + Math.max(0, Math.min(1, f)) * (max - min)); }
        public String display() {
            String v = step >= 1 ? String.valueOf((long) Math.round(value)) : String.format(java.util.Locale.ROOT, step >= 0.1 ? "%.1f" : "%.2f", value);
            return v + (unit == null ? "" : unit);
        }
        @Override public Object save() { return value; }
        @Override public void load(Object o) { if (o instanceof Number) set(((Number) o).doubleValue()); }
    }

    public static final class Mode extends Setting {
        public final String[] options;
        public int index;
        public Mode(String name, int def, String... options) { super(name); this.options = options; this.index = def; }
        public String get() { return options[Math.max(0, Math.min(options.length - 1, index))]; }
        public boolean is(String s) { return get().equals(s); }
        public void cycle(int d) { index = Math.floorMod(index + d, options.length); }
        @Override public Object save() { return get(); }
        @Override public void load(Object o) {
            if (o instanceof String) for (int i = 0; i < options.length; i++) if (options[i].equals(o)) index = i;
        }
    }

    /** Color from the Lego palette (index) - "Regenbogen" cycles through all colors. */
    public static final class Color extends Setting {
        public static final int[] PALETTE = {
                0xFF1ED760, 0xFFE3000B, 0xFFFFCD03, 0xFF006CB7, 0xFF00A3DA, 0xFF9B4DCA,
                0xFFFF6FB5, 0xFFFF8A00, 0xFFFFFFFF, 0xFF7CF7FF, 0xFFB8FF4F, 0xFF00000F};
        private static final String[] NAMES_DE = {"Grün", "Rot", "Gelb", "Blau", "Hellblau", "Lila", "Pink", "Orange", "Weiß", "Eis", "Lime", "Regenbogen"};
        public static String[] NAMES = NAMES_DE;
        public static void updateNames() {
            String[] n = new String[NAMES_DE.length];
            for (int i = 0; i < n.length; i++) n[i] = Lang.get("color." + i, NAMES_DE[i]);
            NAMES = n;
        }
        public int index;
        public Color(String name, int def) { super(name); index = def; }
        public boolean rainbow() { return index == PALETTE.length - 1; }
        /** ARGB color; rainbow depends on time and an optional phase offset. */
        public int argb(double phase) {
            if (!rainbow()) return PALETTE[index];
            double t = System.currentTimeMillis() / 2600.0 + phase;
            return 0xFF000000 | (java.awt.Color.HSBtoRGB((float) (t - Math.floor(t)), 0.75f, 1f) & 0xFFFFFF);
        }
        public int argb() { return argb(0); }
        @Override public Object save() { return (long) index; }
        @Override public void load(Object o) { if (o instanceof Number) index = Math.max(0, Math.min(PALETTE.length - 1, ((Number) o).intValue())); }
    }

    /** Free text (chat message, names, word list ...). */
    public static final class Text extends Setting {
        public String value;
        public final int max;
        public final String hint;
        public Text(String name, String def, int max, String hint) { super(name); value = def; this.max = max; this.hint = hint; }
        public String get() { return value; }
        @Override public Object save() { return value; }
        @Override public void load(Object o) { if (o instanceof String) value = ((String) o).length() > max ? ((String) o).substring(0, max) : (String) o; }
    }

    /** A button (no value) - e.g. "Spiel starten", "Einstellungen öffnen". */
    public static final class Action extends Setting {
        public final Runnable action;
        public final String label;
        public Action(String name, String label, Runnable action) { super(name); this.label = label; this.action = action; }
        @Override public Object save() { return null; }
        @Override public void load(Object o) { }
    }
}
