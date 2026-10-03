package dev.lego.cosmetic;

import dev.lego.core.Modules;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public final class Cos {
   public static final List<Cos.Item> ALL = new ArrayList<>();
   private static final Map<Cos.Slot, String> EQUIPPED = new EnumMap<>(Cos.Slot.class);
   public static volatile int version = 0;
   public static BiConsumer<G, Cos.Slot> slotTransform = null;

   private Cos() {
   }

   static void add(String var0, String var1, Cos.Slot var2, Cos.Rarity var3, Cos.Model var4) {
      ALL.add(new Cos.Item(var0, var1, var2, var3, var4, null));
   }

   static void cape(String var0, String var1, Cos.Rarity var2) {
      ALL.add(new Cos.Item("cape_" + var0, var1, Cos.Slot.CAPE, var2, Models::capePreview, "cape_" + var0));
   }

   public static synchronized void init() {
      if (ALL.isEmpty()) {
         Models.registerAll();
         Pets.registerAll();
         Vehicles.registerAll();
         XmasCos.registerAll();
      }
   }

   public static Cos.Item get(String var0) {
      for (Cos.Item var2 : ALL) {
         if (var2.id.equals(var0)) {
            return var2;
         }
      }

      return null;
   }

   public static List<Cos.Item> of(Cos.Slot var0) {
      ArrayList var1 = new ArrayList();

      for (Cos.Item var3 : ALL) {
         if (var3.slot == var0) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public static Cos.Item equipped(Cos.Slot var0) {
      String var1 = EQUIPPED.get(var0);
      return var1 == null ? null : get(var1);
   }

   public static boolean isEquipped(Cos.Item var0) {
      return var0.id.equals(EQUIPPED.get(var0.slot));
   }

   public static boolean anyEquipped() {
      return !EQUIPPED.isEmpty();
   }

   public static void toggle(Cos.Item var0) {
      if (isEquipped(var0)) {
         EQUIPPED.remove(var0.slot);
      } else {
         EQUIPPED.put(var0.slot, var0.id);
      }

      version++;
      Modules.scheduleSave();
   }

   public static void unequipAll() {
      EQUIPPED.clear();
      version++;
      Modules.scheduleSave();
   }

   public static int equippedCount() {
      return EQUIPPED.size();
   }

   public static void register() {
      init();
      Modules.persist("cosmetics", new Modules.Persist() {
         @Override
         public Object save() {
            LinkedHashMap var1 = new LinkedHashMap();

            for (Entry var3 : Cos.EQUIPPED.entrySet()) {
               var1.put(((Cos.Slot)var3.getKey()).name(), var3.getValue());
            }

            return var1;
         }

         @Override
         public void load(Object var1) {
            if (var1 instanceof Map) {
               Cos.EQUIPPED.clear();

               for (Entry var3 : (Iterable<Entry>) (Iterable<?>) (((Map)var1).entrySet())) {
                  try {
                     Cos.Slot var4 = Cos.Slot.valueOf((String)var3.getKey());
                     if (var3.getValue() instanceof String && Cos.get((String)var3.getValue()) != null) {
                        Cos.EQUIPPED.put(var4, (String)var3.getValue());
                     }
                  } catch (IllegalArgumentException var5) {
                  }
               }

               Cos.version++;
            }
         }
      });
   }

   public static void renderEquipped(G var0, Cos.A var1, boolean var2) {
      for (Cos.Slot var6 : Cos.Slot.values()) {
         if (var6.head == var2 && var6 != Cos.Slot.CAPE && var6 != Cos.Slot.PET && var6 != Cos.Slot.VEHICLE) {
            Cos.Item var7 = equipped(var6);
            if (var7 != null) {
               var0.push();
               var0.color(-1).glow(false);
               if (slotTransform != null) {
                  slotTransform.accept(var0, var6);
               }

               try {
                  var7.model.render(var0, var1);
               } finally {
                  var0.pop();
               }
            }
         }
      }
   }

   /** Like renderEquipped, but for an explicit slot->item map (other LEGO players). */
   public static void renderItems(G var0, Cos.A var1, boolean var2, Map<Cos.Slot, String> items) {
      for (Cos.Slot var6 : Cos.Slot.values()) {
         if (var6.head == var2 && var6 != Cos.Slot.CAPE && var6 != Cos.Slot.PET && var6 != Cos.Slot.VEHICLE) {
            Cos.Item var7 = items.get(var6) == null ? null : get(items.get(var6));
            if (var7 != null) {
               var0.push();
               var0.color(-1).glow(false);
               try {
                  var7.model.render(var0, var1);
               } finally {
                  var0.pop();
               }
            }
         }
      }
   }

   public static final class A {
      public float time;
      public boolean sneak;
      public float move;
      public boolean preview;
      public String emotion = "happy";
      public float blink;
      public float lookX;
      public float lookY;
      public float speed;
      public int level = 3;
      public float growth = 0.5F;
      public float wheel;
      public float lean;
   }

   public static final class Item {
      public final String id;
      public final String name;
      public final Cos.Slot slot;
      public final Cos.Rarity rarity;
      public final Cos.Model model;
      public final String cape;

      Item(String var1, String var2, Cos.Slot var3, Cos.Rarity var4, Cos.Model var5, String var6) {
         this.id = var1;
         this.name = var2;
         this.slot = var3;
         this.rarity = var4;
         this.model = var5;
         this.cape = var6;
      }
   }

   public interface Model {
      void render(G var1, Cos.A var2);
   }

   public static enum Rarity {
      COMMON("Gewöhnlich", -6642766),
      RARE("Selten", -12604929),
      EPIC("Episch", -5153537),
      LEGENDARY("Legendär", -20434);

      public final String title;
      public final int color;

      private Rarity(String nullxx, int nullxxx) {
         this.title = nullxx;
         this.color = nullxxx;
      }
   }

   public static enum Slot {
      CAPE("Umhänge", "shirt", false),
      WINGS("Flügel", "wing", false),
      HAT("Hüte", "crown", true),
      FACE("Gesicht", "glasses", true),
      BACK("Rücken", "backpack", false),
      AURA("Auren", "aura", false),
      PET("Haustiere", "paw", false),
      VEHICLE("Fahrzeuge", "car", false);

      public final String title;
      public final String icon;
      public final boolean head;

      private Slot(String nullxx, String nullxxx, boolean nullxxxx) {
         this.title = nullxx;
         this.icon = nullxxx;
         this.head = nullxxxx;
      }
   }
}
