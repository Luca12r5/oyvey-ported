package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.CrystalAge;
import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {EndCrystalRenderer.class},
   priority = 500
)
public class CrystalSpinMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;F)V"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$spinSpeed(EndCrystal crystal, EndCrystalRenderState state, float tickProgress, CallbackInfo ci) {
      ((CrystalAge)state).baulooStyles$setRealAge(state.ageInTicks);
      if (StylesUiConfig.crystalSpin != 100) {
         state.ageInTicks = state.ageInTicks * CrystalAge.factor();
      }
   }

   @Inject(
      method = {"submit(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"},
      at = {@At("HEAD")}
   )
   private void baulooStyles$rememberOffset(
      EndCrystalRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci
   ) {
      float real = ((CrystalAge)state).baulooStyles$realAge();
      CrystalAge.Offset.current = Float.isNaN(real) ? 0.0F : real - state.ageInTicks;
   }

   @ModifyVariable(
      method = {"getY"},
      at = @At("HEAD"),
      argsOnly = true
   )
   private static float baulooStyles$normalBob(float age) {
      return age + CrystalAge.Offset.current;
   }
}
