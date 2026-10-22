package dev.lego.cosmetic;

import dev.lego.core.Category;
import dev.lego.core.Module;
import dev.lego.core.Modules;
import dev.lego.core.Setting;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * How the equipped LEGO knife shows in your own hand (first person): which held items it
 * replaces (only the empty hand, swords, or swords and axes), the hand with fingers and its
 * animations. Purely visual: the real item stays in your hand and works as usual.
 */
public final class KnifeView extends Module {
   public static KnifeView instance;

   final Setting.Mode replaces = this.add(new Setting.Mode("Ersetzt", 0, "Leere Hand", "Schwerter", "Schwerter & Äxte"));
   final Setting.Bool fingers = this.add(new Setting.Bool("Hand mit Fingern", true));
   final Setting.Bool fingerAnim = this.add(new Setting.Bool("Finger-Animation", true));
   final Setting.Bool idle = this.add(new Setting.Bool("Leerlauf-Animation", true));

   /** Set while the knife is drawn instead of a held item (see ItemInHandKnifeMixin). */
   public static boolean replacing;

   private KnifeView() {
      super("knifeview", "Messer-Ansicht", "Dein LEGO-Messer in der Hand: was es ersetzt, Finger und Animationen (Taste R: ansehen / drehen)", Category.VISUALS);
      this.icon("sword");
      this.enabled = true;
      this.fresh();
   }

   public static void register() {
      instance = Modules.register(new KnifeView());
   }

   static boolean on() {
      return instance == null || instance.enabled;
   }

   /** Whether the knife is shown instead of this main-hand item. */
   public static boolean replaces(ItemStack stack) {
      if (!on() || Cos.equipped(Cos.Slot.KNIFE) == null) return false;
      if (stack.isEmpty()) return true;
      int mode = instance == null ? 0 : instance.replaces.index;
      if (mode >= 1 && stack.is(ItemTags.SWORDS)) return true;
      return mode >= 2 && stack.is(ItemTags.AXES);
   }

   static boolean showFingers() {
      return instance == null || instance.fingers.get();
   }

   static boolean animateFingers() {
      return instance == null || instance.fingerAnim.get();
   }

   static boolean idleMotion() {
      return instance == null || instance.idle.get();
   }
}
