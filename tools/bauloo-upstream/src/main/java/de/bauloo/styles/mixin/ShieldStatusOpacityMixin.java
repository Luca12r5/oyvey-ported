package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.StylesUiConfig;
import de.bauloo.styles.TextureAlpha;
import net.minecraft.class_11659;
import net.minecraft.class_11701;
import net.minecraft.class_4587;
import net.minecraft.class_600;
import net.minecraft.class_811;
import net.minecraft.class_9323;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "walksy/shieldstatus/render/ShieldItemModelRenderer", remap = false)
public class ShieldStatusOpacityMixin {
   @Unique
   private static volatile float baulooStyles$alpha = 1.0F;
   @Unique
   private static boolean baulooStyles$logged;
   @Unique
   private static boolean baulooStyles$colorLogged;

   @Inject(method = "render", at = @At("HEAD"), require = 0, remap = false)
   private void baulooStyles$start(
      class_9323 data,
      class_600 model,
      class_11701 sprites,
      class_811 context,
      class_4587 matrices,
      class_11659 queue,
      int light,
      int overlay,
      int seed,
      boolean glint,
      CallbackInfo ci
   ) {
      StylesUiConfig.load();
      boolean hand = context.method_29998() || context == class_811.field_4323 || context == class_811.field_4320;
      baulooStyles$alpha = hand && StylesUiConfig.shieldOpacity < 100 ? Math.max(15, StylesUiConfig.shieldOpacity) / 100.0F : 1.0F;
   }

   @ModifyExpressionValue(
      method = "render",
      require = 0,
      remap = false,
      at = @At(value = "FIELD", remap = false, target = "Lwalksy/shieldstatus/config/Config;grayscaleTexture:Z")
   )
   private boolean baulooStyles$grey(boolean original) {
      return original || StylesUiConfig.shieldCustom && !TextureAlpha.shieldHasHoles();
   }

   @Unique
   private static int baulooStyles$withAlpha(int argb) {
      if (StylesUiConfig.shieldCustom) {
         int r = argb >> 16 & 0xFF;
         int g = argb >> 8 & 0xFF;
         int b = argb & 0xFF;
         if (!baulooStyles$colorLogged) {
            baulooStyles$colorLogged = true;
            BaulooStylesClient.LOGGER.info("[ShieldColor] ShieldStatus colour {}", Integer.toHexString(argb));
         }

         boolean warning = r > g + 40 && r > b + 40;
         if (!warning) {
            argb = argb & 0xFF000000 | StylesUiConfig.shieldColor & 16777215;
         }
      }

      float alpha = baulooStyles$alpha;
      if (alpha >= 1.0F) {
         return argb;
      }

      int a = Math.round((argb >>> 24 & 0xFF) * alpha);
      return a << 24 | argb & 16777215;
   }

   @ModifyArg(
      method = {"lambda$render$0", "lambda$render$2"},
      require = 0,
      remap = false,
      index = 4,
      at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/class_630;method_22699(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V")
   )
   private static int baulooStyles$partColor(int color) {
      if (!baulooStyles$logged) {
         baulooStyles$logged = true;
         BaulooStylesClient.LOGGER.info("Shield opacity hooked into ShieldStatus (alpha {})", baulooStyles$alpha);
      }

      return baulooStyles$withAlpha(color);
   }

   @ModifyArg(
      method = "lambda$render$1",
      require = 0,
      remap = false,
      index = 11,
      at = @At(
         value = "INVOKE",
         remap = false,
         target = "Lwalksy/shieldstatus/render/ShieldItemModelRenderer;renderCanvas(Lnet/minecraft/class_4587;Lnet/minecraft/class_11701;Lnet/minecraft/class_4588;IILnet/minecraft/class_630;ZLnet/minecraft/class_1767;Lnet/minecraft/class_9307;ZZI)V"
      )
   )
   private int baulooStyles$canvasColor(int color) {
      return baulooStyles$withAlpha(color);
   }
}
