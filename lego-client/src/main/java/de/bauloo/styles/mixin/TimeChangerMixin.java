package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class})
public class TimeChangerMixin {
   @Inject(
      method = {"getDayTime"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void baulooStyles$time(CallbackInfoReturnable<Long> cir) {
      int time = StylesUiConfig.timeOverride;
      if (time >= 0 && (Object)this instanceof ClientLevel) {
         cir.setReturnValue((long)time);
      }
   }
}
