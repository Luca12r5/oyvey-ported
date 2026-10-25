package de.bauloo.styles.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import de.bauloo.styles.BaulooStylesClient;
import de.bauloo.styles.StylesUiConfig;
import de.bauloo.styles.TextureAlpha;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {"walksy/shieldstatus/render/ShieldItemModelRenderer"},
   remap = false
)
public class ShieldStatusOpacityMixin {
   @Unique
   private static volatile float baulooStyles$alpha = 1.0F;
   @Unique
   private static boolean baulooStyles$logged;
   @Unique
   private static boolean baulooStyles$colorLogged;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      require = 0,
      remap = false
   )
   private void baulooStyles$start(
      DataComponentMap data,
      ShieldModel model,
      MaterialSet sprites,
      ItemDisplayContext context,
      PoseStack matrices,
      SubmitNodeCollector queue,
      int light,
      int overlay,
      int seed,
      boolean glint,
      CallbackInfo ci
   ) {
      StylesUiConfig.load();
      boolean hand = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
      baulooStyles$alpha = hand && StylesUiConfig.shieldOpacity < 100 ? Math.max(15, StylesUiConfig.shieldOpacity) / 100.0F : 1.0F;
   }

   @ModifyExpressionValue(
      method = {"render"},
      require = 0,
      remap = false,
      at = {@At(
         value = "FIELD",
         remap = false,
         target = "Lwalksy/shieldstatus/config/Config;grayscaleTexture:Z"
      )}
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
      } else {
         int a = Math.round((argb >>> 24 & 0xFF) * alpha);
         return a << 24 | argb & 16777215;
      }
   }

   @ModifyArg(
      method = {"lambda$render$0", "lambda$render$2"},
      require = 0,
      remap = false,
      index = 4,
      at = @At(
         value = "INVOKE",
         remap = false,
         target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
      )
   )
   private static int baulooStyles$partColor(int color) {
      if (!baulooStyles$logged) {
         baulooStyles$logged = true;
         BaulooStylesClient.LOGGER.info("Shield opacity hooked into ShieldStatus (alpha {})", baulooStyles$alpha);
      }

      return baulooStyles$withAlpha(color);
   }

   @ModifyArg(
      method = {"lambda$render$1"},
      require = 0,
      remap = false,
      index = 11,
      at = @At(
         value = "INVOKE",
         remap = false,
         target = "Lwalksy/shieldstatus/render/ShieldItemModelRenderer;renderCanvas(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/MaterialSet;Lcom/mojang/blaze3d/vertex/VertexConsumer;IILnet/minecraft/client/model/geom/ModelPart;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;ZZI)V"
      )
   )
   private int baulooStyles$canvasColor(int color) {
      return baulooStyles$withAlpha(color);
   }
}
