package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.perf.Performance;
import dev.lego.visual.Visuals;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {SkyRenderer.class},
   remap = false
)
public abstract class SkyRenderingMixin {
   @Inject(
      method = {"method_62302(I)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$top(int color, CallbackInfo ci) {
      try {
         if (Visuals.renderSkyPass()) {
            ci.cancel();
         }
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"method_62306(Lnet/minecraft/class_4587;FI)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$glow(PoseStack m, float a, int c, CallbackInfo ci) {
      try {
         if (Visuals.skyReplaced() || Performance.potato()) {
            ci.cancel();
         }
      } catch (Throwable var6) {
      }
   }

   @Inject(
      method = {"method_62307(Lnet/minecraft/class_4587;FFFLnet/minecraft/class_12131;FF)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$bodies(PoseStack m, float a, float b, float c, MoonPhase phase, float d, float e, CallbackInfo ci) {
      try {
         if (Visuals.skyReplaced() || Performance.potato()) {
            ci.cancel();
         }
      } catch (Throwable var10) {
      }
   }

   @Inject(
      method = {"method_62305()V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$dark(CallbackInfo ci) {
      try {
         if (Visuals.skyReplaced()) {
            ci.cancel();
         }
      } catch (Throwable var3) {
      }
   }
}
