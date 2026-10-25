package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.StylesPresence;
import net.minecraft.class_2561;
import net.minecraft.class_355;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = class_355.class, priority = 1500)
public class TabIconMixin {
   @WrapOperation(
      method = "method_1919",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_355;method_1918(Lnet/minecraft/class_640;)Lnet/minecraft/class_2561;")
   )
   private class_2561 baulooStyles$tornado(class_355 self, class_640 entry, Operation<class_2561> original) {
      class_2561 name = (class_2561)original.call(new Object[]{self, entry});
      class_2561 icon = StylesPresence.icon(entry.method_2966().id());
      return (class_2561)(icon == null ? name : class_2561.method_43473().method_10852(icon).method_27693(" ").method_10852(name));
   }
}
