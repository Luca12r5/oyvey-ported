package dev.lego.games;

import dev.lego.ui.Sound;

final class PackPuzzle {
   static final int[] PAL = new int[]{-1900533, -16749385, -13053, -16743125, -30208, -6599222};

   private PackPuzzle() {
   }

   static void click() {
      Sound.play("minecraft:ui.button.click", 1.3F, 0.2F);
   }

   static void good() {
      Sound.play("minecraft:entity.experience_orb.pickup", 1.3F, 0.3F);
   }

   static void bad() {
      Sound.play("minecraft:block.note_block.bass", 0.8F, 0.4F);
   }

   static void win() {
      Sound.play("minecraft:entity.player.levelup", 1.2F, 0.4F);
   }
}
