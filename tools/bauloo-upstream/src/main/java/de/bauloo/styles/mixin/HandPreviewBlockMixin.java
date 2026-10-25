package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.bauloo.styles.HandPreview;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_759.class)
public class HandPreviewBlockMixin {
   private static final String METHOD = "renderFirstPersonItem";
   @Unique
   private boolean baulooStyles$blocking;

   @Inject(method = "method_3228", at = @At("HEAD"))
   private void baulooStyles$start(
      class_742 player,
      float tickProgress,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equipProgress,
      class_4587 matrices,
      class_11659 queue,
      int light,
      CallbackInfo ci
   ) {
      class_1799 preview = HandPreview.stack();
      this.baulooStyles$blocking = hand == class_1268.field_5808
         && preview != null
         && preview.method_31574(class_1802.field_8255)
         && stack.method_31574(class_1802.field_8255);
   }

   @WrapOperation(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6115()Z"))
   private boolean baulooStyles$using(class_742 player, Operation<Boolean> original) {
      return this.baulooStyles$blocking || (Boolean)original.call(new Object[]{player});
   }

   @WrapOperation(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6014()I"))
   private int baulooStyles$timeLeft(class_742 player, Operation<Integer> original) {
      return this.baulooStyles$blocking ? 72000 : (Integer)original.call(new Object[]{player});
   }

   @WrapOperation(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6058()Lnet/minecraft/class_1268;"))
   private class_1268 baulooStyles$activeHand(class_742 player, Operation<class_1268> original) {
      return this.baulooStyles$blocking ? class_1268.field_5808 : (class_1268)original.call(new Object[]{player});
   }
}
