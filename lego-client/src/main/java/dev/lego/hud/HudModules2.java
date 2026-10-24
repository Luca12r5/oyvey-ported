package dev.lego.hud;

import dev.lego.core.Mc;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class HudModules2 {
   private static int pos = 0;

   private HudModules2() {
   }

   public static void registerAll() {
      Modules.register(new HudModules2.InvCounter());
      Modules.register(new HudModules2.Gaps());
      // "Nearby players" and "closest player" are not registered: they show players through walls (radar).
      Modules.register(new HudModules2.Entities());
      Modules.register(new HudModules2.FreeSlots());
      Modules.register(new HudModules2.Deaths());
   }

   private static double fy() {
      return 0.25 + pos++ % 14 * 0.045;
   }

   static double dist(Entity a, Entity b) {
      double dx = Mc.x(a) - Mc.x(b);
      double dy = Mc.y(a) - Mc.y(b);
      double dz = Mc.z(a) - Mc.z(b);
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   static final class Closest extends HudModules.Simple {
      Closest() {
         super("h2_closest", "Nächster Spieler", "Name und Entfernung des nächsten Spielers", "user", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         LocalPlayer me = Mc.player();
         Player best = null;
         double bd = Double.MAX_VALUE;
         if (me != null) {
            for (Player p : Mc.players()) {
               if (p != me) {
                  double d = HudModules2.dist(me, p);
                  if (d < bd) {
                     bd = d;
                     best = p;
                  }
               }
            }
         }

         this.value = best == null ? "-" : Mc.name(best);
         this.unit = best == null ? "" : Math.round(bd) + "m";
      }
   }

   static final class Deaths extends HudModules.Simple {
      private boolean wasDead;
      private int deaths;

      Deaths() {
         super("h2_deaths", "Tode", "Wie oft du in dieser Sitzung gestorben bist", "skull", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         LocalPlayer me = Mc.player();
         boolean dead = me != null && Mc.isDead(me);
         if (dead && !this.wasDead) {
            this.deaths++;
         }

         this.wasDead = dead;
         this.value = String.valueOf(this.deaths);
         this.unit = "Tode";
      }
   }

   static final class Entities extends HudModules.Simple {
      Entities() {
         super("h2_entities", "Entitäten", "Geladene Entitäten um dich (Lag-Check)", "entity", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         this.value = String.valueOf(Mc.entities().size());
         this.unit = "Entitäten";
      }
   }

   static final class FreeSlots extends HudModules.Simple {
      FreeSlots() {
         super("h2_freeslots", "Freie Plätze", "Freie Plätze im Inventar", "backpack", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         int free = 0;
         int n = Math.min(36, Mc.invSize());

         for (int i = 0; i < n; i++) {
            if (Mc.empty(Mc.invStack(i))) {
               free++;
            }
         }

         this.value = String.valueOf(free);
         this.unit = "frei";
      }
   }

   static final class Gaps extends HudModules.Simple {
      Gaps() {
         super("h2_gaps", "Goldäpfel", "Goldäpfel | verzauberte Goldäpfel", "food", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         this.value = Mc.countItem("golden_apple") + " | " + Mc.countItem("enchanted_golden_apple");
         this.unit = "Gaps";
      }
   }

   static final class InvCounter extends HudModules.Simple {
      private static final String[][] ITEMS = new String[][]{
         {"Totems", "totem_of_undying"},
         {"Perlen", "ender_pearl"},
         {"Gaps", "golden_apple"},
         {"Gods", "enchanted_golden_apple"},
         {"Kristalle", "end_crystal"},
         {"Obsi", "obsidian"},
         {"Pfeile", "arrow", "spectral_arrow", "tipped_arrow"},
         {"XP", "experience_bottle"},
         {"Raketen", "firework_rocket"},
         {"Tränke", "potion", "splash_potion", "lingering_potion"},
         {"Anker", "respawn_anchor"},
         {"Glowstone", "glowstone"}
      };
      private final Setting.Bool[] show = new Setting.Bool[ITEMS.length];

      InvCounter() {
         super(
            "h2_invcounter",
            "Inventar-Zähler",
            "Zählt in EINEM Panel, was du im Inventar hast: Totems, Perlen, Gaps, Kristalle, Pfeile ... (in den Einstellungen wählen)",
            "backpack",
            0.9,
            0.3
         );
         this.fresh();

         for (int i = 0; i < ITEMS.length; i++) {
            this.show[i] = this.add(new Setting.Bool(ITEMS[i][0], i < 4));
         }
      }

      @Override
      void compute() {
         StringBuilder sb = new StringBuilder();

         for (int i = 0; i < ITEMS.length; i++) {
            if (this.show[i].get()) {
               int n = 0;

               for (int k = 1; k < ITEMS[i].length; k++) {
                  n += Mc.countItem(ITEMS[i][k]);
               }

               if (sb.length() > 0) {
                  sb.append("  ");
               }

               sb.append(n).append(" ").append(ITEMS[i][0]);
            }
         }

         this.value = sb.length() == 0 ? "-" : sb.toString();
         this.unit = "";
      }
   }

   static final class Nearby extends HudModules.Simple {
      Nearby() {
         super("h2_nearby", "Spieler in der Nähe", "Wie viele Spieler im Umkreis von 64 Blöcken sind", "users", 0.9, HudModules2.fy());
         this.fresh();
      }

      @Override
      void compute() {
         LocalPlayer me = Mc.player();
         int n = 0;
         if (me != null) {
            for (Player p : Mc.players()) {
               if (p != me && HudModules2.dist(me, p) <= 64.0) {
                  n++;
               }
            }
         }

         this.value = String.valueOf(n);
         this.unit = "Spieler";
      }
   }
}
