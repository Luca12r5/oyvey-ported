package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.visual.Visuals2;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** "Feuer tiefer" in 26.3: the fire overlay is submitted (submitFire) instead of drawn into a buffer. */
@Mixin(ScreenEffectRenderer.class)
public abstract class FireOverlayMixin {
   @Unique
   private static boolean lego$pushed;

   @Inject(method = "submitFire", at = @At("HEAD"), require = 0)
   private static void lego$low(PoseStack pose, SubmitNodeCollector out, TextureAtlasSprite sprite, CallbackInfo ci) {
      lego$pushed = false;
      try {
         if (Visuals2.lowFire != null && Visuals2.lowFire.enabled) {
            pose.pushPose();
            pose.translate(0.0, -Visuals2.lowFire.height(), 0.0);
            lego$pushed = true;
         }
      } catch (Throwable ignored) {
      }
   }

   @Inject(method = "submitFire", at = @At("RETURN"), require = 0)
   private static void lego$lowEnd(PoseStack pose, SubmitNodeCollector out, TextureAtlasSprite sprite, CallbackInfo ci) {
      if (!lego$pushed) return;
      lego$pushed = false;
      try {
         pose.popPose();
      } catch (Throwable ignored) {
      }
   }
}
