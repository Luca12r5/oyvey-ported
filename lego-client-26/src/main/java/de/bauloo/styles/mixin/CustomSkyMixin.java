package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.CustomSky;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SkyRenderer.class})
public class CustomSkyMixin {
   @Inject(
      method = {"renderSunMoonAndStars"},
      at = {@At("HEAD")},
      require = 0
   )
   private void baulooStyles$customSky(
      PoseStack matrices, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float alpha, float starBrightness, CallbackInfo ci
   ) {
      try {
         CustomSky.render(matrices, sunAngle);
      } catch (Throwable var10) {
         CustomSky.failed(var10);
      }
   }
}
