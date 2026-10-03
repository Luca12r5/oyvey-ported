package dev.lego.mixin;

import dev.lego.core.Mc;
import dev.lego.util.Utility;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {Entity.class},
   remap = false
)
public abstract class EntityMixin {
   @Inject(
      method = {"method_5872(DD)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false,
      require = 0
   )
   private void lego$look(double var1, double var3, CallbackInfo var5) {
      try {
         if ((Object)this == Mc.player() && Utility.freelook != null && Utility.freelook.active()) {
            Utility.freelook.look(var1, var3);
            var5.cancel();
         }
      } catch (Throwable var7) {
      }
   }
}
