package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.visual.Visuals2;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ScreenEffectRenderer.class},
   remap = false
)
public abstract class FireOverlayMixin {
   private static boolean lego$pushed;

   @Inject(
      method = {"method_23070(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1058;)V"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private static void lego$low(PoseStack var0, MultiBufferSource var1, TextureAtlasSprite var2, CallbackInfo var3) {
      lego$pushed = false;

      try {
         if (Visuals2.lowFire != null && Visuals2.lowFire.enabled) {
            var0.pushPose();
            var0.translate(0.0, -Visuals2.lowFire.height(), 0.0);
            lego$pushed = true;
         }
      } catch (Throwable var5) {
      }
   }

   @Inject(
      method = {"method_23070(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1058;)V"},
      at = {@At("RETURN")},
      remap = false,
      require = 0
   )
   private static void lego$lowEnd(PoseStack var0, MultiBufferSource var1, TextureAtlasSprite var2, CallbackInfo var3) {
      if (lego$pushed) {
         lego$pushed = false;

         try {
            var0.popPose();
         } catch (Throwable var5) {
         }
      }
   }
}
