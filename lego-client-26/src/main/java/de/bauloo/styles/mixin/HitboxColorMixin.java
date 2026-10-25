package de.bauloo.styles.mixin;

import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(
   value = {EntityHitboxDebugRenderer.class},
   priority = 2000
)
public class HitboxColorMixin {
   @Unique
   private static boolean baulooStyles$logged;

   @ModifyArg(
      method = {"showHitboxes"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/gizmos/GizmoStyle;stroke(I)Lnet/minecraft/gizmos/GizmoStyle;",
         ordinal = 0
      )}
   )
   private int baulooStyles$hitboxColor(int color) {
      StylesUiConfig.load();
      if (!baulooStyles$logged) {
         baulooStyles$logged = true;
         BaulooStylesClient.LOGGER.info("[HitboxColor] vanilla hitbox drawn, custom={}", StylesUiConfig.hitboxCustom);
      }

      return StylesUiConfig.hitboxCustom ? 0xFF000000 | StylesUiConfig.hitboxColor : color;
   }
}
