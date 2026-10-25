package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public class InventoryBackgroundMixin {
   @Inject(method = "method_25420", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$clear(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      if (StylesUiConfig.invClearBackground && client.field_1687 != null && this instanceof class_465) {
         client.field_1705.method_72738();
         ci.cancel();
      }
   }
}
