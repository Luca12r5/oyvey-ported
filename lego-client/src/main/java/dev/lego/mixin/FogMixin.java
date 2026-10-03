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
   value = {FogRenderer.class}
)
public abstract class FogMixin {
   @Inject(
      method = {"computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;"},
      at = {@At("RETURN")},
      require = 0
   )
   private void lego$fog(Camera var1, float var2, ClientLevel var3, int var4, float var5, CallbackInfoReturnable<Vector4f> var6) {
      try {
         if (Visuals.sky == null || !Visuals.sky.enabled || var1.getFluidInCamera() != FogType.NONE && var1.getFluidInCamera() != FogType.ATMOSPHERIC) {
            return;
         }

         int var7 = Visuals.sky.fogTint();
         if (var7 == 0) {
            return;
         }

         Vector4f var8 = (Vector4f)var6.getReturnValue();
         if (var8 == null) {
            return;
         }

         float var9 = 0.8F;
         var8.x = var8.x + ((var7 >> 16 & 0xFF) / 255.0F - var8.x) * var9;
         var8.y = var8.y + ((var7 >> 8 & 0xFF) / 255.0F - var8.y) * var9;
         var8.z = var8.z + ((var7 & 0xFF) / 255.0F - var8.z) * var9;
      } catch (Throwable var10) {
      }
   }
}
