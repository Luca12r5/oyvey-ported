package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.cosmetic.VehicleWorld;
import dev.lego.visual.Visuals;
import dev.lego.visual.Visuals2;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {GameRenderer.class},
   remap = false
)
public abstract class GameRendererMixin {
   @Inject(
      method = {"method_3196(Lnet/minecraft/class_4184;FZ)F"},
      at = {@At("RETURN")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$zoom(Camera var1, float var2, boolean var3, CallbackInfoReturnable<Float> var4) {
      try {
         float var5 = (Float)var4.getReturnValue();
         float var6 = VehicleWorld.speed();
         if (var6 > 0.01F) {
            var5 *= 1.0F + 0.07F * var6;
         }

         if (Visuals.zoom != null) {
            var5 = Visuals.zoom.apply(var5);
         }

         var4.setReturnValue(var5);
      } catch (Throwable var7) {
      }
   }

   @Inject(
      method = {"method_3198(Lnet/minecraft/class_4587;F)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$tilt(PoseStack var1, float var2, CallbackInfo var3) {
      try {
         if (Visuals2.camera != null) {
            float var4 = Visuals2.camera.roll();
            if (Math.abs(var4) > 0.001F) {
               var1.mulPose(new Quaternionf().rotateZ((float)Math.toRadians(var4)));
            }
         }

         if (Visuals.noHurtCam != null && Visuals.noHurtCam.enabled) {
            var3.cancel();
         }
      } catch (Throwable var5) {
      }
   }
}
