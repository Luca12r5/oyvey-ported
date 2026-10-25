package de.bauloo.styles.mixin;

import de.bauloo.styles.AnchorGlow;
import de.bauloo.styles.AnchorPixelGlow;
import de.bauloo.styles.FireColor;
import de.bauloo.styles.ShieldColor;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.SpriteLoader.Preparations;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TextureAtlas.class})
public class AnchorAtlasMixin {
   @Shadow
   @Final
   private Identifier field_21749;

   @Inject(
      method = {"upload"},
      at = {@At("TAIL")}
   )
   private void baulooStyles$anchorGlow(Preparations result, CallbackInfo ci) {
      if (TextureAtlas.LOCATION_BLOCKS.equals(this.field_21749)) {
         AnchorGlow.applyLive();
         AnchorPixelGlow.applyLive();
         FireColor.atlasRebuilt();
      } else if (Sheets.SHIELD_SHEET.equals(this.field_21749)) {
         ShieldColor.atlasRebuilt();
      }
   }
}
