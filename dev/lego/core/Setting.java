package dev.lego.core;

import java.util.Locale;

public abstract class Setting {
   public final String name;

   protected Setting(String name) {
      this.name = name;
   }

   public String displayName() {
      String t = Lang.get("s." + this.name);
      return t != null ? t : this.name;
   }

   public abstract Object save();

   public abstract void load(Object var1);

   public static final class Action extends Setting {
      public final Runnable action;
      public final String label;

      public Action(String name, String label, Runnable action) {
         super(name);
         this.label = label;
         this.action = action;
      }

      @Override
      public Object save() {
         return null;
      }

      @Override
      public void load(Object o) {
      }
   }

   public static final class Bool extends Setting {
      public boolean value;

      public Bool(String name, boolean def) {
         super(name);
         this.value = def;
      }

      public boolean get() {
         return this.value;
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object o) {
         if (o instanceof Boolean) {
            this.value = (Boolean)o;
         }
      }
   }

   public static final class Color extends Setting {
      public static final int[] PALETTE = new int[]{
         -14756000, -1900533, -13053, -16749385, -16735270, -6599222, -36939, -30208, -1, -8587265, -4653233, -16777201
      };
      private static final String[] NAMES_DE = new String[]{
         "Grün", "Rot", "Gelb", "Blau", "Hellblau", "Lila", "Pink", "Orange", "Weiß", "Eis", "Lime", "Regenbogen"
      };
      public static String[] NAMES = NAMES_DE;
      public int index;

      public static void updateNames() {
         String[] n = new String[NAMES_DE.length];

         for (int i = 0; i < n.length; i++) {
            n[i] = Lang.get("color." + i, NAMES_DE[i]);
         }

         NAMES = n;
      }

      public Color(String name, int def) {
         super(name);
         this.index = def;
      }

      public boolean rainbow() {
         return this.index == PALETTE.length - 1;
      }

      public int argb(double phase) {
         if (!this.rainbow()) {
            return PALETTE[this.index];
         } else {
            double t = System.currentTimeMillis() / 2600.0 + phase;
            return 0xFF000000 | java.awt.Color.HSBtoRGB((float)(t - Math.floor(t)), 0.75F, 1.0F) & 16777215;
         }
      }

      public int argb() {
         return this.argb(0.0);
      }

      @Override
      public Object save() {
         return (long)this.index;
      }

      @Override
      public void load(Object o) {
         if (o instanceof Number) {
            this.index = Math.max(0, Math.min(PALETTE.length - 1, ((Number)o).intValue()));
         }
      }
   }

   public static final class Mode extends Setting {
      public final String[] options;
      public int index;

      public Mode(String name, int def, String... options) {
         super(name);
         this.options = options;
         this.index = def;
      }

      public String get() {
         return this.options[Math.max(0, Math.min(this.options.length - 1, this.index))];
      }

      public boolean is(String s) {
         return this.get().equals(s);
      }

      public void cycle(int d) {
         this.index = Math.floorMod(this.index + d, this.options.length);
      }

      @Override
      public Object save() {
         return this.get();
      }

      @Override
      public void load(Object o) {
         if (o instanceof String) {
            for (int i = 0; i < this.options.length; i++) {
               if (this.options[i].equals(o)) {
                  this.index = i;
               }
            }
         }
      }
   }

   public static final class Num extends Setting {
      public final double min;
      public final double max;
      public final double step;
      public final String unit;
      public double value;

      public Num(String name, double min, double max, double step, double def, String unit) {
         super(name);
         this.min = min;
         this.max = max;
         this.step = step;
         this.value = def;
         this.unit = unit;
      }

      public double get() {
         return this.value;
      }

      public float getF() {
         return (float)this.value;
      }

      public int getI() {
         return (int)Math.round(this.value);
      }

      public void set(double v) {
         v = Math.max(this.min, Math.min(this.max, v));
         if (this.step > 0.0) {
            v = Math.round((v - this.min) / this.step) * this.step + this.min;
         }

         this.value = Math.round(v * 1000.0) / 1000.0;
      }

      public double frac() {
         return this.max > this.min ? (this.value - this.min) / (this.max - this.min) : 0.0;
      }

      public void setFrac(double f) {
         this.set(this.min + Math.max(0.0, Math.min(1.0, f)) * (this.max - this.min));
      }

      public String display() {
         String v = this.step >= 1.0 ? String.valueOf(Math.round(this.value)) : String.format(Locale.ROOT, this.step >= 0.1 ? "%.1f" : "%.2f", this.value);
         return v + (this.unit == null ? "" : this.unit);
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object o) {
         if (o instanceof Number) {
            this.set(((Number)o).doubleValue());
         }
      }
   }

   public static final class Text extends Setting {
      public String value;
      public final int max;
      public final String hint;

      public Text(String name, String def, int max, String hint) {
         super(name);
         this.value = def;
         this.max = max;
         this.hint = hint;
      }

      public String get() {
         return this.value;
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object o) {
         if (o instanceof String) {
            this.value = ((String)o).length() > this.max ? ((String)o).substring(0, this.max) : (String)o;
         }
      }
   }
}
