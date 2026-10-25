package de.bauloo.styles.mixin;

import de.bauloo.styles.StylesUiConfig;
import net.minecraft.class_10509;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_600;
import net.minecraft.class_811;
import net.minecraft.class_9323;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_10509.class)
public class ShieldOpacityMixin {
   @Unique
   private boolean baulooStyles$seeThrough;

   @Inject(
      method = "method_65707(Lnet/minecraft/class_9323;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;IIZI)V",
      at = @At("HEAD")
   )
   private void baulooStyles$start(
      class_9323 data, class_811 context, class_4587 matrices, class_11659 queue, int light, int overlay, boolean glint, int seed, CallbackInfo ci
   ) {
      StylesUiConfig.load();
      this.baulooStyles$seeThrough = (context.method_29998() || context == class_811.field_4323 || context == class_811.field_4320)
         && StylesUiConfig.shieldOpacity < 100;
   }

   @Redirect(
      method = "method_65707(Lnet/minecraft/class_9323;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;IIZI)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_600;method_23500(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;")
   )
   private class_1921 baulooStyles$layer(class_600 model, class_2960 texture) {
      return this.baulooStyles$seeThrough ? class_12249.method_76000(texture) : model.method_23500(texture);
   }

   @ModifyArg(
      method = "method_65707(Lnet/minecraft/class_9323;Lnet/minecraft/class_811;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;IIZI)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11659;method_73494(Lnet/minecraft/class_630;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IILnet/minecraft/class_1058;ZZILnet/minecraft/class_11683$class_11792;I)V"
      ),
      index = 8
   )
   private int baulooStyles$tint(int tint) {
      if (StylesUiConfig.shieldCustom) {
         tint = tint & 0xFF000000 | StylesUiConfig.shieldColor & 16777215;
      }

      if (!this.baulooStyles$seeThrough) {
         return tint;
      }

      int alpha = Math.round(255 * Math.max(15, StylesUiConfig.shieldOpacity) / 100.0F);
      return alpha << 24 | tint & 16777215;
   }
}
