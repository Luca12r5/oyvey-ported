package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import de.bauloo.styles.StylesCrosshair;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Gui.class})
public class CrosshairColorMixin {
   @WrapOperation(
      method = {"renderCrosshair"},
      at = {@At(
         value = "INVOKE",
         ordinal = 0,
         target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
      )}
   )
   private void baulooStyles$crosshairColor(GuiGraphics ctx, RenderPipeline pipeline, Identifier texture, int x, int y, int w, int h, Operation<Void> original) {
      StylesUiConfig.load();
      if (StylesUiConfig.crosshairCustom) {
         StylesCrosshair.draw(ctx, texture, x, y, w, h);
      } else {
         original.call(ctx, pipeline, texture, x, y, w, h);
      }
   }
}
