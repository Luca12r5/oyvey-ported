package de.bauloo.styles.mixin;

import de.bauloo.styles.HandPreview;
import net.minecraft.class_11659;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_759.class)
public class HandPreviewMixin {
   @Shadow
   private class_1799 field_4047;
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;
   @Unique
   private class_1799 baulooStyles$realMainHand;
   @Unique
   private float baulooStyles$realProgress;
   @Unique
   private float baulooStyles$realLastProgress;

   @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V", at = @At("HEAD"))
   private void baulooStyles$showPreview(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
      class_1799 preview = HandPreview.stack();
      if (preview != null) {
         this.baulooStyles$realMainHand = this.field_4047;
         this.baulooStyles$realProgress = this.field_4043;
         this.baulooStyles$realLastProgress = this.field_4053;
         this.field_4047 = preview;
         this.field_4043 = 1.0F;
         this.field_4053 = 1.0F;
      }
   }

   @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V", at = @At("RETURN"))
   private void baulooStyles$restore(float tickProgress, class_4587 matrices, class_11659 queue, class_746 player, int light, CallbackInfo ci) {
      if (this.baulooStyles$realMainHand != null) {
         this.field_4047 = this.baulooStyles$realMainHand;
         this.field_4043 = this.baulooStyles$realProgress;
         this.field_4053 = this.baulooStyles$realLastProgress;
         this.baulooStyles$realMainHand = null;
      }
   }
}
