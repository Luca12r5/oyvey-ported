package dev.lego.games;

import dev.lego.ui.Sound;

final class PackArcade {
   private PackArcade() {
   }

   static void boom() {
      Sound.play("minecraft:entity.generic.explode", 1.4F, 0.25F);
   }

   static void pop() {
      Sound.play("minecraft:entity.chicken.egg", 1.5F, 0.3F);
   }
}
