package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.StylesUiConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
   targets = {"walksy.customhitboxes.render.vertex.VertexRenderer"},
   remap = false,
   priority = 2000
)
public class CustomHitboxesColorMixin {
   @Unique
   private static boolean baulooStyles$logged;

   @ModifyExpressionValue(
      method = {"drawOutlineBox", "drawFilledBox"},
      require = 0,
      remap = false,
      at = {@At(
         value = "INVOKE",
         remap = false,
         target = "Lmain/walksy/lib/core/config/local/options/type/WalksyLibColor;getRed()I"
      )}
   )
   private static int baulooStyles$red(int red) {
      if (!baulooStyles$logged) {
         baulooStyles$logged = true;
         BaulooStylesClient.LOGGER.info("[HitboxColor] CustomHitboxes outline hooked, custom={}", StylesUiConfig.hitboxCustom);
      }

      return StylesUiConfig.hitboxCustom ? StylesUiConfig.hitboxColor >> 16 & 0xFF : red;
   }

   @ModifyExpressionValue(
      method = {"drawOutlineBox", "drawFilledBox"},
      require = 0,
      remap = false,
      at = {@At(
         value = "INVOKE",
         remap = false,
         target = "Lmain/walksy/lib/core/config/local/options/type/WalksyLibColor;getGreen()I"
      )}
   )
   private static int baulooStyles$green(int green) {
      return StylesUiConfig.hitboxCustom ? StylesUiConfig.hitboxColor >> 8 & 0xFF : green;
   }

   @ModifyExpressionValue(
      method = {"drawOutlineBox", "drawFilledBox"},
      require = 0,
      remap = false,
      at = {@At(
         value = "INVOKE",
         remap = false,
         target = "Lmain/walksy/lib/core/config/local/options/type/WalksyLibColor;getBlue()I"
      )}
   )
   private static int baulooStyles$blue(int blue) {
      return StylesUiConfig.hitboxCustom ? StylesUiConfig.hitboxColor & 0xFF : blue;
   }
}
