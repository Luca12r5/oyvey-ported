package de.bauloo.styles.mixin;

import de.bauloo.styles.Watermark;
import net.minecraft.class_332;
import net.minecraft.class_344;
import net.minecraft.class_8666;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_344.class)
public abstract class SocialBadgeMixin {
   @Shadow
   @Final
   protected class_8666 field_45356;

   @Inject(method = "method_75752", at = @At("HEAD"), cancellable = true)
   private void baulooStyles$badge(class_332 ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.field_45356.comp_1604().method_12832().contains("recipe_book") && Watermark.drawBadge(ctx, (class_344)this)) {
         ci.cancel();
      }
   }
}
