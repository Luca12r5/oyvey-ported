package dev.lego.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lego.visual.Visuals;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {SkyRenderer.class}
)
public abstract class SkyRenderingMixin {
   @Inject(
      method = {"renderSkyDisc(I)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void lego$top(int var1, CallbackInfo var2) {
      try {
         if (Visuals.renderSkyPass()) {
            var2.cancel();
         }
      } catch (Throwable var4) {
      }
   }

   @Inject(
      method = {"renderSunriseAndSunset(Lcom/mojang/blaze3d/vertex/PoseStack;FI)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void lego$glow(PoseStack var1, float var2, int var3, CallbackInfo var4) {
      try {
         if (Visuals.skyReplaced()) {
            var4.cancel();
         }
      } catch (Throwable var6) {
      }
   }

   @Inject(
      method = {"renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void lego$bodies(PoseStack var1, float var2, float var3, float var4, MoonPhase var5, float var6, float var7, CallbackInfo var8) {
      try {
         if (Visuals.skyReplaced()) {
            var8.cancel();
         }
      } catch (Throwable var10) {
      }
   }

   @Inject(
      method = {"renderDarkDisc()V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void lego$dark(CallbackInfo var1) {
      try {
         if (Visuals.skyReplaced()) {
            var1.cancel();
         }
      } catch (Throwable var3) {
      }
   }
}
