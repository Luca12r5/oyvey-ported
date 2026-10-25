package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.client.ClientClockManager;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientClockManager.class})
public class TimeChangerMixin {
   @Inject(
      method = {"getTotalTicks"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void baulooStyles$time(Holder<WorldClock> definition, CallbackInfoReturnable<Long> cir) {
      int time = StylesUiConfig.timeOverride;
      if (time >= 0) {
         long total = cir.getReturnValueJ();
         cir.setReturnValue(total - Math.floorMod(total, 24000L) + time);
      }
   }
}
