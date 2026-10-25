package de.bauloo.styles;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents.AfterEntities;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9974;

public final class CrystalHitbox {
   private static final int COLOR = -855638017;
   private static final String INVISIBLE_PACK = "bauloo_styles:crystal_invisible";
   private static boolean invisibleActive;
   private static long invisibleCheckedAt;

   private CrystalHitbox() {
   }

   private static int color() {
      StylesUiConfig.load();
      return StylesUiConfig.crystalBoxCustom ? -872415232 | StylesUiConfig.crystalBoxColor : -855638017;
   }

   private static boolean invisibleStyleActive(class_310 mc) {
      long now = System.currentTimeMillis();
      if (now - invisibleCheckedAt < 1000L) {
         return invisibleActive;
      }

      invisibleCheckedAt = now;

      try {
         invisibleActive = mc.method_1520().method_29210().contains("bauloo_styles:crystal_invisible");
      } catch (Throwable t) {
         invisibleActive = false;
      }

      return invisibleActive;
   }

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES
         .register(
            (AfterEntities)context -> {
               try {
                  class_310 mc = class_310.method_1551();
                  if (mc.field_1687 == null || !invisibleStyleActive(mc)) {
                     return;
                  }

                  class_4587 matrices = context.matrices();
                  class_4597 consumers = context.consumers();
                  if (matrices == null || consumers == null) {
                     return;
                  }

                  class_243 cam = context.worldState().field_63082.field_63078;
                  if (cam == null) {
                     return;
                  }

                  class_4588 buffer = consumers.method_73477(class_12249.field_64042);

                  for (class_1297 entity : mc.field_1687.method_18112()) {
                     if (entity instanceof class_1511) {
                        class_238 box = entity.method_5829();
                        class_9974.method_62296(
                           matrices,
                           buffer,
                           class_259.method_31943(box.field_1323, box.field_1322, box.field_1321, box.field_1320, box.field_1325, box.field_1324),
                           -cam.field_1352,
                           -cam.field_1351,
                           -cam.field_1350,
                           color(),
                           1.0F
                        );
                     }
                  }
               } catch (Throwable var9) {
               }
            }
         );
   }
}
