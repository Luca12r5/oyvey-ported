package de.bauloo.styles.mixin;

import de.bauloo.styles.CrystalAge;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(
   targets = {"tektonikal/crystalchams/CrystalChams"},
   remap = false
)
public class CrystalChamsBobMixin {
   @ModifyVariable(
      method = {"getYOffset(FFFFF)F"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0,
      require = 0,
      remap = false
   )
   private static float baulooStyles$normalBob(float time) {
      if (StylesUiConfig.crystalSpin == 100) {
         return time;
      } else {
         float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
         return (time - delta) / CrystalAge.factor() + delta;
      }
   }
}
