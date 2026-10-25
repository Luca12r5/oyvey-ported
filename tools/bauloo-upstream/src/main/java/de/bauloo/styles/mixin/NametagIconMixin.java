package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesPresence;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_897.class, priority = 2000)
public class NametagIconMixin {
   @Inject(method = "method_62354", at = @At("TAIL"))
   private void baulooStyles$tornado(class_1297 entity, class_10017 state, float tickProgress, CallbackInfo ci) {
      if (state.field_53337 != null && entity instanceof class_1657) {
         class_2561 icon = StylesPresence.icon(entity.method_5667());
         if (icon != null) {
            state.field_53337 = class_2561.method_43473().method_10852(icon).method_27693(" ").method_10852(state.field_53337);
         }
      }
   }
}
