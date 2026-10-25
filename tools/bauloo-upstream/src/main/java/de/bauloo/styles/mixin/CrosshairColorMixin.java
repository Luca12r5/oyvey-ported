package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import de.bauloo.styles.StylesCrosshair;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_329.class)
public class CrosshairColorMixin {
   @WrapOperation(
      method = "method_1736",
      at = @At(
         value = "INVOKE",
         ordinal = 0,
         target = "Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"
      )
   )
   private void baulooStyles$crosshairColor(class_332 ctx, RenderPipeline pipeline, class_2960 texture, int x, int y, int w, int h, Operation<Void> original) {
      StylesUiConfig.load();
      if (StylesUiConfig.crosshairCustom) {
         StylesCrosshair.draw(ctx, texture, x, y, w, h);
      } else {
         original.call(new Object[]{ctx, pipeline, texture, x, y, w, h});
      }
   }
}
