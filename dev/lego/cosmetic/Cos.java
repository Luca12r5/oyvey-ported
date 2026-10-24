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

   public static void add(String id, String name, Cos.Slot s, Cos.Rarity r, Cos.Model m) {
      ALL.add(new Cos.Item(id, name, s, r, m, null));
   }

   static void cape(String id, String name, Cos.Rarity r) {
      ALL.add(new Cos.Item("cape_" + id, name, Cos.Slot.CAPE, r, Models::capePreview, "cape_" + id));
   }

   public static synchronized void init() {
      if (ALL.isEmpty()) {
         Models.registerAll();
         Pets.registerAll();
         Vehicles.registerAll();
         XmasCos.registerAll();
      }
   }

   public static Cos.Item get(String id) {
      for (Cos.Item i : ALL) {
         if (i.id.equals(id)) {
            return i;
         }
      }

      return null;
   }

   public static List<Cos.Item> of(Cos.Slot s) {
      List<Cos.Item> out = new ArrayList<>();

      for (Cos.Item i : ALL) {
         if (i.slot == s) {
            out.add(i);
         }
      }

      return out;
   }

   public static Cos.Item equipped(Cos.Slot s) {
      String id = EQUIPPED.get(s);
      return id == null ? null : get(id);
   }

   public static boolean isEquipped(Cos.Item i) {
      return i.id.equals(EQUIPPED.get(i.slot));
   }

   public static boolean anyEquipped() {
      return !EQUIPPED.isEmpty();
   }

   public static void toggle(Cos.Item i) {
      if (isEquipped(i)) {
         EQUIPPED.remove(i.slot);
      } else {
         EQUIPPED.put(i.slot, i.id);
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
            Map<String, Object> m = new LinkedHashMap<>();

            for (Entry<Cos.Slot, String> e : Cos.EQUIPPED.entrySet()) {
               m.put(e.getKey().name(), e.getValue());
            }

            return m;
         }

         @Override
         public void load(Object o) {
            if (o instanceof Map) {
               Cos.EQUIPPED.clear();

               for (Entry<String, Object> e : ((Map)o).entrySet()) {
                  try {
                     Cos.Slot s = Cos.Slot.valueOf(e.getKey());
                     if (e.getValue() instanceof String && Cos.get((String)e.getValue()) != null) {
                        Cos.EQUIPPED.put(s, (String)e.getValue());
                     }
                  } catch (IllegalArgumentException var5) {
                  }
               }

               Cos.version++;
            }
         }
      });
   }

   public static void renderList(G g, Cos.A a, boolean head, List<String> ids) {
      for (String id : ids) {
         Cos.Item it = get(id);
         if (it != null && it.slot.head == head && it.slot != Cos.Slot.CAPE && it.slot != Cos.Slot.PET && it.slot != Cos.Slot.VEHICLE) {
            g.push();
            g.color(-1).glow(false);

            try {
               it.model.render(g, a);
            } finally {
               g.pop();
            }
         }
      }
   }

   public static void renderEquipped(G g, Cos.A a, boolean head) {
      for (Cos.Slot s : Cos.Slot.values()) {
         if (s.head == head && s != Cos.Slot.CAPE && s != Cos.Slot.PET && s != Cos.Slot.VEHICLE) {
            Cos.Item it = equipped(s);
            if (it != null) {
               g.push();
               g.color(-1).glow(false);
               if (slotTransform != null) {
                  slotTransform.accept(g, s);
               }

               try {
                  it.model.render(g, a);
               } finally {
                  g.pop();
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

      Item(String id, String name, Cos.Slot slot, Cos.Rarity rarity, Cos.Model model, String cape) {
         this.id = id;
         this.name = name;
         this.slot = slot;
         this.rarity = rarity;
         this.model = model;
         this.cape = cape;
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

      private Rarity(String t, int c) {
         this.title = t;
         this.color = c;
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

      private Slot(String title, String icon, boolean head) {
         this.title = title;
         this.icon = icon;
         this.head = head;
      }
   }
}
