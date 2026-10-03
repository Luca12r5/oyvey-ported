package dev.lego.mixin;

import dev.lego.emote.Cutscenes;
import dev.lego.util.Utility;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {Camera.class}
)
public abstract class CameraMixin {
   private final double[] lego$cam = new double[5];

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void setPosition(double var1, double var3, double var5);

   @Inject(
      method = {"setup(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZZF)V"},
      at = {@At("TAIL")},
      require = 0
   )
   private void lego$cutscene(Level var1, Entity var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
      try {
         if (Cutscenes.camera(var5, this.lego$cam)) {
            this.setRotation((float)this.lego$cam[3], (float)this.lego$cam[4]);
            this.setPosition(this.lego$cam[0], this.lego$cam[1], this.lego$cam[2]);
         }
      } catch (Throwable var8) {
      }
   }

   @ModifyArg(
      method = {"setup(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZZF)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
      ),
      index = 0,
      require = 0
   )
   private float lego$yaw(float var1) {
      return Utility.freelook != null && Utility.freelook.active() ? Utility.freelook.yaw() : var1;
   }

   @ModifyArg(
      method = {"setup(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZZF)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
      ),
      index = 1,
      require = 0
   )
   private float lego$pitch(float var1) {
      return Utility.freelook != null && Utility.freelook.active() ? Utility.freelook.pitch() : var1;
   }
}
