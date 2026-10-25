package de.bauloo.styles.mixin;

import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_12155;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = class_12155.class, priority = 2000)
public class HitboxColorMixin {
   @Unique
   private static boolean baulooStyles$logged;

   @ModifyArg(
      method = "method_75432",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_12179;method_75535(I)Lnet/minecraft/class_12179;", ordinal = 0)
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
