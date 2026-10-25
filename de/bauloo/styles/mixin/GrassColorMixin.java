package de.bauloo.styles.mixin;

import de.bauloo.styles.GrassColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {BiomeColors.class},
   priority = 2000
)
public class GrassColorMixin {
   @Inject(
      method = {"getAverageColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void baulooStyles$grass(BlockAndTintGetter world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
      if (resolver == BiomeColors.GRASS_COLOR_RESOLVER) {
         GrassColor.mixinCalls++;
         if (GrassColor.current() >= 0) {
            cir.setReturnValue(GrassColor.shown());
         }
      }
   }

   @Inject(
      method = {"getAverageGrassColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void baulooStyles$grassFirst(BlockAndTintGetter world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      GrassColor.mixinCalls++;
      if (GrassColor.current() >= 0) {
         cir.setReturnValue(GrassColor.shown());
      }
   }
}
