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
   private void lego$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
      try {
         float fov = cir.getReturnValue();
         float vs = VehicleWorld.speed();
         if (vs > 0.01F) {
            fov *= 1.0F + 0.07F * vs;
         }

         if (Visuals.customFov != null) {
            fov = Visuals.customFov.apply(fov);
         }

         if (Visuals2.camera != null) {
            fov = Visuals2.camera.fovModify(fov);
         }

         if (Visuals.zoom != null) {
            fov = Visuals.zoom.apply(fov);
         }

         cir.setReturnValue(fov);
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
   private void lego$tilt(PoseStack matrices, float tickDelta, CallbackInfo ci) {
      try {
         if (Visuals2.camera != null) {
            float roll = Visuals2.camera.roll();
            if (Math.abs(roll) > 0.001F) {
               matrices.mulPose(new Quaternionf().rotateZ((float)Math.toRadians(roll)));
            }
         }

         if (Visuals.noHurtCam != null && Visuals.noHurtCam.enabled) {
            ci.cancel();
         }
      } catch (Throwable var5) {
      }
   }
}
