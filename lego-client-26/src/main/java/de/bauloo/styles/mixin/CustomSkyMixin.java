package de.bauloo.styles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import de.bauloo.styles.CustomSky;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.3: the sun, moon and stars are drawn inside the sky's own render pass, where no second pass
 * can be opened. The custom sky layers are drawn right after the sky pass has closed instead.
 */
@Mixin({SkyRenderer.class})
public class CustomSkyMixin {
   @Inject(method = "render", at = @At("TAIL"), require = 0)
   private void baulooStyles$customSky(GpuBufferSlice fog, SkyRenderState state, CallbackInfo ci) {
      try {
         CustomSky.render(new PoseStack(), state.sunAngle);
      } catch (Throwable var10) {
         CustomSky.failed(var10);
      }
   }
}
