package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.SoundEngine.PlayResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SoundManager.class})
public class ExplosionSoundMixin {
   @Inject(
      method = {"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$mute(SoundInstance sound, CallbackInfoReturnable<PlayResult> cir) {
      if (StylesUiConfig.noExplosionSound
         && sound != null
         && "minecraft".equals(sound.getIdentifier().getNamespace())
         && "entity.generic.explode".equals(sound.getIdentifier().getPath())) {
         cir.setReturnValue(PlayResult.NOT_STARTED);
      }
   }
}
