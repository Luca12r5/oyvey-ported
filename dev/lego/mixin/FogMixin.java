package dev.lego.mixin;

import dev.lego.visual.Visuals;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FogRenderer.class},
   remap = false
)
public abstract class FogMixin {
   @Inject(
      method = {"method_62185(Lnet/minecraft/class_4184;FLnet/minecraft/class_638;IF)Lorg/joml/Vector4f;"},
      at = {@At("RETURN")},
      remap = false,
      require = 0
   )
   private void lego$fog(Camera camera, float td, ClientLevel world, int viewDistance, float darkness, CallbackInfoReturnable<Vector4f> cir) {
      try {
         if (Visuals.sky == null || !Visuals.sky.enabled) {
            return;
         }

         if (camera.getFluidInCamera() != FogType.NONE && camera.getFluidInCamera() != FogType.ATMOSPHERIC) {
            return;
         }

         Vector4f v = cir.getReturnValue();
         if (v == null) {
            return;
         }

         double density = Visuals.sky.fogDensity.get();
         if (density > 0.0) {
            int fc = Visuals.sky.fogColor.argb() & 16777215;
            float fr = (fc >> 16 & 0xFF) / 255.0F;
            float fg = (fc >> 8 & 0xFF) / 255.0F;
            float fb = (fc & 0xFF) / 255.0F;
            float k = (float)(density / 100.0);
            v.x = v.x + (fr - v.x) * k;
            v.y = v.y + (fg - v.y) * k;
            v.z = v.z + (fb - v.z) * k;
         } else if (Visuals.sky.fog.get()) {
            int c = Visuals.sky.fogTint();
            if (c == 0) {
               return;
            }

            float k = 0.8F;
            v.x = v.x + ((c >> 16 & 0xFF) / 255.0F - v.x) * k;
            v.y = v.y + ((c >> 8 & 0xFF) / 255.0F - v.y) * k;
            v.z = v.z + ((c & 0xFF) / 255.0F - v.z) * k;
         }
      } catch (Throwable var15) {
      }
   }
}
