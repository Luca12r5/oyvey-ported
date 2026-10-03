package dev.lego.core;

import java.util.Locale;

public abstract class Setting {
   public final String name;

   protected Setting(String var1) {
      this.name = var1;
   }

   public abstract Object save();

   public abstract void load(Object var1);

   public static final class Action extends Setting {
      public final Runnable action;
      public final String label;

      public Action(String var1, String var2, Runnable var3) {
         super(var1);
         this.label = var2;
         this.action = var3;
      }

      @Override
      public Object save() {
         return null;
      }

      @Override
      public void load(Object var1) {
      }
   }

   public static final class Bool extends Setting {
      public boolean value;

      public Bool(String var1, boolean var2) {
         super(var1);
         this.value = var2;
      }

      public boolean get() {
         return this.value;
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object var1) {
         if (var1 instanceof Boolean) {
            this.value = (Boolean)var1;
         }
      }
   }

   public static final class Color extends Setting {
      public static final int[] PALETTE = new int[]{
         -14756000, -1900533, -13053, -16749385, -16735270, -6599222, -36939, -30208, -1, -8587265, -4653233, -16777201
      };
      public static final String[] NAMES = new String[]{
         "Grün", "Rot", "Gelb", "Blau", "Hellblau", "Lila", "Pink", "Orange", "Weiß", "Eis", "Lime", "Regenbogen"
      };
      public int index;

      public Color(String var1, int var2) {
         super(var1);
         this.index = var2;
      }

      public boolean rainbow() {
         return this.index == PALETTE.length - 1;
      }

      public int argb(double var1) {
         if (!this.rainbow()) {
            return PALETTE[this.index];
         } else {
            double var3 = System.currentTimeMillis() / 2600.0 + var1;
            return 0xFF000000 | java.awt.Color.HSBtoRGB((float)(var3 - Math.floor(var3)), 0.75F, 1.0F) & 16777215;
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
      public void load(Object var1) {
         if (var1 instanceof Number) {
            this.index = Math.max(0, Math.min(PALETTE.length - 1, ((Number)var1).intValue()));
         }
      }
   }

   public static final class Mode extends Setting {
      public final String[] options;
      public int index;

      public Mode(String var1, int var2, String... var3) {
         super(var1);
         this.options = var3;
         this.index = var2;
      }

      public String get() {
         return this.options[Math.max(0, Math.min(this.options.length - 1, this.index))];
      }

      public boolean is(String var1) {
         return this.get().equals(var1);
      }

      public void cycle(int var1) {
         this.index = Math.floorMod(this.index + var1, this.options.length);
      }

      @Override
      public Object save() {
         return this.get();
      }

      @Override
      public void load(Object var1) {
         if (var1 instanceof String) {
            for (int var2 = 0; var2 < this.options.length; var2++) {
               if (this.options[var2].equals(var1)) {
                  this.index = var2;
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

      public Num(String var1, double var2, double var4, double var6, double var8, String var10) {
         super(var1);
         this.min = var2;
         this.max = var4;
         this.step = var6;
         this.value = var8;
         this.unit = var10;
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

      public void set(double var1) {
         var1 = Math.max(this.min, Math.min(this.max, var1));
         if (this.step > 0.0) {
            var1 = Math.round((var1 - this.min) / this.step) * this.step + this.min;
         }

         this.value = Math.round(var1 * 1000.0) / 1000.0;
      }

      public double frac() {
         return this.max > this.min ? (this.value - this.min) / (this.max - this.min) : 0.0;
      }

      public void setFrac(double var1) {
         this.set(this.min + Math.max(0.0, Math.min(1.0, var1)) * (this.max - this.min));
      }

      public String display() {
         String var1 = this.step >= 1.0 ? String.valueOf(Math.round(this.value)) : String.format(Locale.ROOT, this.step >= 0.1 ? "%.1f" : "%.2f", this.value);
         return var1 + (this.unit == null ? "" : this.unit);
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object var1) {
         if (var1 instanceof Number) {
            this.set(((Number)var1).doubleValue());
         }
      }
   }

   public static final class Text extends Setting {
      public String value;
      public final int max;
      public final String hint;

      public Text(String var1, String var2, int var3, String var4) {
         super(var1);
         this.value = var2;
         this.max = var3;
         this.hint = var4;
      }

      public String get() {
         return this.value;
      }

      @Override
      public Object save() {
         return this.value;
      }

      @Override
      public void load(Object var1) {
         if (var1 instanceof String) {
            this.value = ((String)var1).length() > this.max ? ((String)var1).substring(0, this.max) : (String)var1;
         }
      }
   }
}
