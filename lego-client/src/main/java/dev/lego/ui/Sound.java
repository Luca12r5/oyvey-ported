package dev.lego.ui;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class Sound {
   public static boolean uiSounds = true;
   private static final Map<String, SoundEvent> EVENTS = new HashMap<>();
   private static long lastClick = 0L;

   private Sound() {
   }

   public static void play(String id, float pitch, float volume) {
      try {
         SoundEvent ev = EVENTS.computeIfAbsent(id, k -> SoundEvent.createVariableRangeEvent(Identifier.parse(k)));
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, volume));
      } catch (Throwable var4) {
      }
   }

   public static void click() {
      if (uiSounds) {
         long now = System.currentTimeMillis();
         if (now - lastClick >= 40L) {
            lastClick = now;
            play("minecraft:ui.button.click", 1.35F, 0.18F);
         }
      }
   }

   public static void pop() {
      if (uiSounds) {
         play("minecraft:entity.item.pickup", 1.6F, 0.25F);
      }
   }

   public static void open() {
      if (uiSounds) {
         play("minecraft:block.note_block.chime", 1.8F, 0.12F);
      }
   }
}
