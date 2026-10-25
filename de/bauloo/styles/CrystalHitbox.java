package de.bauloo.styles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

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

   private static boolean invisibleStyleActive(Minecraft mc) {
      long now = System.currentTimeMillis();
      if (now - invisibleCheckedAt < 1000L) {
         return invisibleActive;
      } else {
         invisibleCheckedAt = now;

         try {
            invisibleActive = mc.getResourcePackRepository().getSelectedIds().contains("bauloo_styles:crystal_invisible");
         } catch (Throwable var4) {
            invisibleActive = false;
         }

         return invisibleActive;
      }
   }

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES
         .register(
            context -> {
               try {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.level == null || !invisibleStyleActive(mc)) {
                     return;
                  }

                  PoseStack matrices = context.matrices();
                  MultiBufferSource consumers = context.consumers();
                  if (matrices == null || consumers == null) {
                     return;
                  }

                  Vec3 cam = context.worldState().cameraRenderState.pos;
                  if (cam == null) {
                     return;
                  }

                  VertexConsumer buffer = consumers.getBuffer(RenderTypes.LINES);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof EndCrystal) {
                        AABB box = entity.getBoundingBox();
                        ShapeRenderer.renderShape(
                           matrices, buffer, Shapes.create(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ), -cam.x, -cam.y, -cam.z, color(), 1.0F
                        );
                     }
                  }
               } catch (Throwable var9) {
               }
            }
         );
   }
}
