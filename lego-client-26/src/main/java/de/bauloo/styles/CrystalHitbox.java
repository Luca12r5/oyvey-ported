package de.bauloo.styles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.CollectSubmits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            (CollectSubmits)context -> {
               try {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc.level == null || !invisibleStyleActive(mc)) {
                     return;
                  }

                  PoseStack matrices = context.poseStack();
                  SubmitNodeCollector collector = context.submitNodeCollector();
                  if (matrices == null || collector == null) {
                     return;
                  }

                  Vec3 cam = context.levelState().cameraRenderState.pos;
                  if (cam == null) {
                     return;
                  }

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof EndCrystal) {
                        AABB box = entity.getBoundingBox();
                        matrices.pushPose();
                        matrices.translate(-cam.x, -cam.y, -cam.z);
                        collector.submitShapeOutline(
                           matrices, Shapes.create(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ), RenderTypes.lines(), color(), 2.0F, false
                        );
                        matrices.popPose();
                     }
                  }
               } catch (Throwable var8) {
               }
            }
         );
   }
}
